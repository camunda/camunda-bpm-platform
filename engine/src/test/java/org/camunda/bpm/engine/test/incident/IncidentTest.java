package org.camunda.bpm.engine.test.incident;

import java.util.List;

import org.camunda.bpm.engine.impl.incident.FailedJobIncidentHandler;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;

public class IncidentTest extends PluggableProcessEngineTestCase {
  
  @Deployment(resources = {"org/camunda/bpm/engine/test/incident/IncidentTest.testShouldCreateOneIncident.bpmn"})
  public void testShouldCreateOneIncident() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingProcess");

    waitForJobExecutorToProcessAllJobs(6000, 500);
    
    Incident incident = runtimeService.createIncidentQuery().processInstanceId(processInstance.getId()).singleResult();

    assertNotNull(incident);
    
    assertNotNull(incident.getId());
    assertNotNull(incident.getIncidentTimestamp());
    assertEquals(FailedJobIncidentHandler.INCIDENT_HANDLER_TYPE, incident.getIncidentType());
    assertEquals(processInstance.getId(), incident.getExecutionId());
    assertEquals("theServiceTask", incident.getActivityId());
    assertEquals(processInstance.getId(), incident.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), incident.getProcessDefinitionId());
    assertNull(incident.getCauseIncidentId());
    assertNull(incident.getRootCauseIncidentId());
    
    Job job = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();
    
    assertNotNull(job);
    
    assertEquals(job.getId(), incident.getConfiguration());   
  }
  
  @Deployment(resources = {"org/camunda/bpm/engine/test/incident/IncidentTest.testShouldCreateOneIncident.bpmn"})
  public void testShouldCreateSecondIncidentAfterSetRetries() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingProcess");

    waitForJobExecutorToProcessAllJobs(6000, 500);
    
    List<Incident> incidents = runtimeService.createIncidentQuery().processInstanceId(processInstance.getId()).list();
    
    assertFalse(incidents.isEmpty());
    assertTrue(incidents.size() == 1);
    
    Job job = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();
    
    assertNotNull(job);
    
    managementService.setJobRetries(job.getId(), 1);
    
    waitForJobExecutorToProcessAllJobs(6000, 500);
    
    incidents = runtimeService.createIncidentQuery().processInstanceId(processInstance.getId()).list();
    
    assertFalse(incidents.isEmpty());
    assertTrue(incidents.size() == 2);
  }
  
  @Deployment(resources = {"org/camunda/bpm/engine/test/incident/IncidentTest.testShouldCreateOneIncidentForNestedExecution.bpmn"})
  public void testShouldCreateOneIncidentForNestedExecution() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingProcessWithNestedExecutions");

    waitForJobExecutorToProcessAllJobs(6000, 500);
    
    Incident incident = runtimeService.createIncidentQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(incident);
    
    Job job = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(job);
    
    String executionIdOfNestedFailingExecution = job.getExecutionId();
    
    assertFalse(processInstance.getId() == executionIdOfNestedFailingExecution);
    
    assertNotNull(incident.getId());
    assertNotNull(incident.getIncidentTimestamp());
    assertEquals(FailedJobIncidentHandler.INCIDENT_HANDLER_TYPE, incident.getIncidentType());
    assertEquals(executionIdOfNestedFailingExecution, incident.getExecutionId());
    assertEquals("theServiceTask", incident.getActivityId());
    assertEquals(processInstance.getId(), incident.getProcessInstanceId());
    assertNull(incident.getCauseIncidentId());
    assertNull(incident.getRootCauseIncidentId());
    assertEquals(job.getId(), incident.getConfiguration());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/incident/IncidentTest.testShouldCreateRecursiveIncidents.bpmn", 
      "org/camunda/bpm/engine/test/incident/IncidentTest.testShouldCreateOneIncident.bpmn"})
  public void testShouldCreateRecursiveIncidents() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callFailingProcess");

    waitForJobExecutorToProcessAllJobs(6000, 500);
    
    List<Incident> incidents = runtimeService.createIncidentQuery().list();
    assertFalse(incidents.isEmpty());
    assertTrue(incidents.size() == 2);
    
    ProcessInstance failingProcess = runtimeService.createProcessInstanceQuery().processDefinitionKey("failingProcess").singleResult();
    assertNotNull(failingProcess);
    
    Incident causeIncident = runtimeService.createIncidentQuery().processDefinitionId(failingProcess.getProcessDefinitionId()).singleResult();
    assertNotNull(causeIncident);
    
    Job job = managementService.createJobQuery().executionId(causeIncident.getExecutionId()).singleResult();
    assertNotNull(job);
    
    assertNotNull(causeIncident.getId());
    assertNotNull(causeIncident.getIncidentTimestamp());
    assertEquals(FailedJobIncidentHandler.INCIDENT_HANDLER_TYPE, causeIncident.getIncidentType());
    assertEquals(job.getExecutionId(), causeIncident.getExecutionId());
    assertEquals("theServiceTask", causeIncident.getActivityId());
    assertEquals(failingProcess.getId(), causeIncident.getProcessInstanceId());
    assertNull(causeIncident.getCauseIncidentId());
    assertNull(causeIncident.getRootCauseIncidentId());
    assertEquals(job.getId(), causeIncident.getConfiguration());
    
    Incident recursiveCreatedIncident = runtimeService.createIncidentQuery().causeIncidentId(causeIncident.getId()).singleResult();
    assertNotNull(recursiveCreatedIncident);
    
    Execution theCallActivityExecution = runtimeService.createExecutionQuery().activityId("theCallActivity").singleResult();
    assertNotNull(theCallActivityExecution);
    
    assertNotNull(recursiveCreatedIncident.getId());
    assertNotNull(recursiveCreatedIncident.getIncidentTimestamp());
    assertEquals(FailedJobIncidentHandler.INCIDENT_HANDLER_TYPE, recursiveCreatedIncident.getIncidentType());
    assertEquals(theCallActivityExecution.getId(), recursiveCreatedIncident.getExecutionId());
    assertEquals("theCallActivity", recursiveCreatedIncident.getActivityId());
    assertEquals(processInstance.getId(), recursiveCreatedIncident.getProcessInstanceId());
    assertEquals(causeIncident.getId(), recursiveCreatedIncident.getCauseIncidentId());
    assertEquals(causeIncident.getId(), recursiveCreatedIncident.getRootCauseIncidentId());
    assertNull(recursiveCreatedIncident.getConfiguration());
  }
  
  @Deployment(resources = {"org/camunda/bpm/engine/test/incident/IncidentTest.testShouldCreateRecursiveIncidentsForNestedCallActivity.bpmn",
  		"org/camunda/bpm/engine/test/incident/IncidentTest.testShouldCreateRecursiveIncidents.bpmn", 
  "org/camunda/bpm/engine/test/incident/IncidentTest.testShouldCreateOneIncident.bpmn"})
  public void testShouldCreateRecursiveIncidentsForNestedCallActivity() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callingFailingCallActivity");

    waitForJobExecutorToProcessAllJobs(6000, 500);
    
    List<Incident> incidents = runtimeService.createIncidentQuery().list();
    assertFalse(incidents.isEmpty());
    assertTrue(incidents.size() == 3);
    
    // Root Cause Incident
    ProcessInstance failingProcess = runtimeService.createProcessInstanceQuery().processDefinitionKey("failingProcess").singleResult();
    assertNotNull(failingProcess);
    
    Incident rootCauseIncident = runtimeService.createIncidentQuery().processDefinitionId(failingProcess.getProcessDefinitionId()).singleResult();
    assertNotNull(rootCauseIncident);
    
    Job job = managementService.createJobQuery().executionId(rootCauseIncident.getExecutionId()).singleResult();
    assertNotNull(job);
    
    assertNotNull(rootCauseIncident.getId());
    assertNotNull(rootCauseIncident.getIncidentTimestamp());
    assertEquals(FailedJobIncidentHandler.INCIDENT_HANDLER_TYPE, rootCauseIncident.getIncidentType());
    assertEquals(job.getExecutionId(), rootCauseIncident.getExecutionId());
    assertEquals("theServiceTask", rootCauseIncident.getActivityId());
    assertEquals(failingProcess.getId(), rootCauseIncident.getProcessInstanceId());
    assertNull(rootCauseIncident.getCauseIncidentId());
    assertNull(rootCauseIncident.getRootCauseIncidentId());
    assertEquals(job.getId(), rootCauseIncident.getConfiguration());
    
    // Cause Incident
    ProcessInstance callFailingProcess = runtimeService.createProcessInstanceQuery().processDefinitionKey("callFailingProcess").singleResult();
    assertNotNull(callFailingProcess);
    
    Incident causeIncident = runtimeService.createIncidentQuery().processDefinitionId(callFailingProcess.getProcessDefinitionId()).singleResult();
    assertNotNull(causeIncident);
    
    Execution theCallActivityExecution = runtimeService.createExecutionQuery().activityId("theCallActivity").singleResult();
    assertNotNull(theCallActivityExecution);
    
    assertNotNull(causeIncident.getId());
    assertNotNull(causeIncident.getIncidentTimestamp());
    assertEquals(FailedJobIncidentHandler.INCIDENT_HANDLER_TYPE, causeIncident.getIncidentType());
    assertEquals(theCallActivityExecution.getId(), causeIncident.getExecutionId());
    assertEquals("theCallActivity", causeIncident.getActivityId());
    assertEquals(callFailingProcess.getId(), causeIncident.getProcessInstanceId());
    assertEquals(rootCauseIncident.getId(), causeIncident.getCauseIncidentId());
    assertEquals(rootCauseIncident.getId(), causeIncident.getRootCauseIncidentId());
    assertNull(causeIncident.getConfiguration());
    
    // Top level incident of the startet process
    
    Incident topLevelIncident = runtimeService.createIncidentQuery().processDefinitionId(processInstance.getProcessDefinitionId()).singleResult();
    assertNotNull(topLevelIncident);
    
    Execution theCallingCallActivity = runtimeService.createExecutionQuery().activityId("theCallingCallActivity").singleResult();
    assertNotNull(theCallingCallActivity);
    
    assertNotNull(topLevelIncident.getId());
    assertNotNull(topLevelIncident.getIncidentTimestamp());
    assertEquals(FailedJobIncidentHandler.INCIDENT_HANDLER_TYPE, topLevelIncident.getIncidentType());
    assertEquals(theCallingCallActivity.getId(), topLevelIncident.getExecutionId());
    assertEquals("theCallingCallActivity", topLevelIncident.getActivityId());
    assertEquals(processInstance.getId(), topLevelIncident.getProcessInstanceId());
    assertEquals(causeIncident.getId(), topLevelIncident.getCauseIncidentId());
    assertEquals(rootCauseIncident.getId(), topLevelIncident.getRootCauseIncidentId());
    assertNull(topLevelIncident.getConfiguration());
  }
  
}
