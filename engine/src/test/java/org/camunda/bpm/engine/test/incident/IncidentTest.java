/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.incident;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.incident.FailedJobIncidentHandler;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;

public class IncidentTest extends PluggableProcessEngineTestCase {
  
  @Deployment(resources = {"org/camunda/bpm/engine/test/incident/IncidentTest.testShouldCreateOneIncident.bpmn"})
  public void testShouldCreateOneIncident() {
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingProcess");

    waitForJobExecutorToProcessAllJobs(15000);
    
    Incident incident = runtimeService.createIncidentQuery().processInstanceId(processInstance.getId()).singleResult();

    assertNotNull(incident);
    
    assertNotNull(incident.getId());
    assertNotNull(incident.getIncidentTimestamp());
    assertEquals(FailedJobIncidentHandler.INCIDENT_HANDLER_TYPE, incident.getIncidentType());
    assertEquals("Exception expected.", incident.getIncidentMessage());
    assertEquals(processInstance.getId(), incident.getExecutionId());
    assertEquals("theServiceTask", incident.getActivityId());
    assertEquals(processInstance.getId(), incident.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), incident.getProcessDefinitionId());
    assertEquals(incident.getId(), incident.getCauseIncidentId());
    assertEquals(incident.getId(), incident.getRootCauseIncidentId());
    
    Job job = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();
    
    assertNotNull(job);
    
    assertEquals(job.getId(), incident.getConfiguration());   
  }
  
  @Deployment(resources = {"org/camunda/bpm/engine/test/incident/IncidentTest.testShouldCreateOneIncident.bpmn"})
  public void testShouldCreateOneIncidentAfterSetRetries() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingProcess");

    waitForJobExecutorToProcessAllJobs(15000);
    
    List<Incident> incidents = runtimeService.createIncidentQuery().processInstanceId(processInstance.getId()).list();
    
    assertFalse(incidents.isEmpty());
    assertTrue(incidents.size() == 1);
    
    Job job = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();
    
    assertNotNull(job);
    
    // set job retries to 1 -> should fail again and a second incident should be created
    managementService.setJobRetries(job.getId(), 1);
    
    waitForJobExecutorToProcessAllJobs(15000);
    
    incidents = runtimeService.createIncidentQuery().processInstanceId(processInstance.getId()).list();
    
    // There is still one incident
    assertFalse(incidents.isEmpty());
    assertTrue(incidents.size() == 1);
  }
  
  @Deployment(resources = {"org/camunda/bpm/engine/test/incident/IncidentTest.testShouldCreateOneIncident.bpmn"})
  public void testShouldCreateOneIncidentAfterExecuteJob() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingProcess");

    waitForJobExecutorToProcessAllJobs(15000);
    
    List<Incident> incidents = runtimeService.createIncidentQuery().processInstanceId(processInstance.getId()).list();
    
    assertFalse(incidents.isEmpty());
    assertTrue(incidents.size() == 1);
    
    Job job = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();
    
    assertNotNull(job);
    
    // set job retries to 1 -> should fail again and a second incident should be created
    try {
      managementService.executeJob(job.getId());
      fail("Exception was expected.");
    } catch (ProcessEngineException e) {
      // exception expected
    }
    
    incidents = runtimeService.createIncidentQuery().processInstanceId(processInstance.getId()).list();
    
    // There is still one incident
    assertFalse(incidents.isEmpty());
    assertTrue(incidents.size() == 1);
  }
  
  @Deployment(resources = {"org/camunda/bpm/engine/test/incident/IncidentTest.testShouldCreateOneIncidentForNestedExecution.bpmn"})
  public void testShouldCreateOneIncidentForNestedExecution() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingProcessWithNestedExecutions");

    waitForJobExecutorToProcessAllJobs(15000);
    
    Incident incident = runtimeService.createIncidentQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(incident);
    
    Job job = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(job);
    
    String executionIdOfNestedFailingExecution = job.getExecutionId();
    
    assertFalse(processInstance.getId() == executionIdOfNestedFailingExecution);
    
    assertNotNull(incident.getId());
    assertNotNull(incident.getIncidentTimestamp());
    assertEquals(FailedJobIncidentHandler.INCIDENT_HANDLER_TYPE, incident.getIncidentType());
    assertEquals("Exception expected.", incident.getIncidentMessage());
    assertEquals(executionIdOfNestedFailingExecution, incident.getExecutionId());
    assertEquals("theServiceTask", incident.getActivityId());
    assertEquals(processInstance.getId(), incident.getProcessInstanceId());
    assertEquals(incident.getId(), incident.getCauseIncidentId());
    assertEquals(incident.getId(), incident.getRootCauseIncidentId());
    assertEquals(job.getId(), incident.getConfiguration());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/incident/IncidentTest.testShouldCreateRecursiveIncidents.bpmn", 
      "org/camunda/bpm/engine/test/incident/IncidentTest.testShouldCreateOneIncident.bpmn"})
  public void testShouldCreateRecursiveIncidents() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callFailingProcess");

    waitForJobExecutorToProcessAllJobs(15000);
    
    List<Incident> incidents = runtimeService.createIncidentQuery().list();
    assertFalse(incidents.isEmpty());
    assertTrue(incidents.size() == 2);
    
    ProcessInstance failingProcess = runtimeService.createProcessInstanceQuery().processDefinitionKey("failingProcess").singleResult();
    assertNotNull(failingProcess);
    
    ProcessInstance callProcess = runtimeService.createProcessInstanceQuery().processDefinitionKey("callFailingProcess").singleResult();
    assertNotNull(callProcess);
    
    // Root cause incident
    Incident causeIncident = runtimeService.createIncidentQuery().processDefinitionId(failingProcess.getProcessDefinitionId()).singleResult();
    assertNotNull(causeIncident);
    
    Job job = managementService.createJobQuery().executionId(causeIncident.getExecutionId()).singleResult();
    assertNotNull(job);
    
    assertNotNull(causeIncident.getId());
    assertNotNull(causeIncident.getIncidentTimestamp());
    assertEquals(FailedJobIncidentHandler.INCIDENT_HANDLER_TYPE, causeIncident.getIncidentType());
    assertEquals("Exception expected.", causeIncident.getIncidentMessage());
    assertEquals(job.getExecutionId(), causeIncident.getExecutionId());
    assertEquals("theServiceTask", causeIncident.getActivityId());
    assertEquals(failingProcess.getId(), causeIncident.getProcessInstanceId());
    assertEquals(causeIncident.getId(), causeIncident.getCauseIncidentId());
    assertEquals(causeIncident.getId(), causeIncident.getRootCauseIncidentId());
    assertEquals(job.getId(), causeIncident.getConfiguration());
    
    // Recursive created incident
    Incident recursiveCreatedIncident = runtimeService.createIncidentQuery().processDefinitionId(callProcess.getProcessDefinitionId()).singleResult();
    assertNotNull(recursiveCreatedIncident);
    
    Execution theCallActivityExecution = runtimeService.createExecutionQuery().activityId("theCallActivity").singleResult();
    assertNotNull(theCallActivityExecution);
    
    assertNotNull(recursiveCreatedIncident.getId());
    assertNotNull(recursiveCreatedIncident.getIncidentTimestamp());
    assertEquals(FailedJobIncidentHandler.INCIDENT_HANDLER_TYPE, recursiveCreatedIncident.getIncidentType());
    assertNull(recursiveCreatedIncident.getIncidentMessage());
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

    waitForJobExecutorToProcessAllJobs(15000);
    
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
    assertEquals("Exception expected.", rootCauseIncident.getIncidentMessage());
    assertEquals(job.getExecutionId(), rootCauseIncident.getExecutionId());
    assertEquals("theServiceTask", rootCauseIncident.getActivityId());
    assertEquals(failingProcess.getId(), rootCauseIncident.getProcessInstanceId());
    assertEquals(rootCauseIncident.getId(), rootCauseIncident.getCauseIncidentId());
    assertEquals(rootCauseIncident.getId(), rootCauseIncident.getRootCauseIncidentId());
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
    assertNull(causeIncident.getIncidentMessage());
    assertEquals(theCallActivityExecution.getId(), causeIncident.getExecutionId());
    assertEquals("theCallActivity", causeIncident.getActivityId());
    assertEquals(callFailingProcess.getId(), causeIncident.getProcessInstanceId());
    assertEquals(rootCauseIncident.getId(), causeIncident.getCauseIncidentId());
    assertEquals(rootCauseIncident.getId(), causeIncident.getRootCauseIncidentId());
    assertNull(causeIncident.getConfiguration());
    
    // Top level incident of the startet process (recursive created incident for super super process instance)
    Incident topLevelIncident = runtimeService.createIncidentQuery().processDefinitionId(processInstance.getProcessDefinitionId()).singleResult();
    assertNotNull(topLevelIncident);
    
    Execution theCallingCallActivity = runtimeService.createExecutionQuery().activityId("theCallingCallActivity").singleResult();
    assertNotNull(theCallingCallActivity);
    
    assertNotNull(topLevelIncident.getId());
    assertNotNull(topLevelIncident.getIncidentTimestamp());
    assertEquals(FailedJobIncidentHandler.INCIDENT_HANDLER_TYPE, topLevelIncident.getIncidentType());
    assertNull(topLevelIncident.getIncidentMessage());
    assertEquals(theCallingCallActivity.getId(), topLevelIncident.getExecutionId());
    assertEquals("theCallingCallActivity", topLevelIncident.getActivityId());
    assertEquals(processInstance.getId(), topLevelIncident.getProcessInstanceId());
    assertEquals(causeIncident.getId(), topLevelIncident.getCauseIncidentId());
    assertEquals(rootCauseIncident.getId(), topLevelIncident.getRootCauseIncidentId());
    assertNull(topLevelIncident.getConfiguration());
  }
  
  @Deployment(resources = {"org/camunda/bpm/engine/test/incident/IncidentTest.testShouldCreateOneIncident.bpmn"})
  public void testShouldDeleteIncidentAfterJobHasBeenDeleted() {
    // start failing process
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingProcess");

    waitForJobExecutorToProcessAllJobs(15000);
    
    // get the job
    Job job = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(job);
    
    // there exists one incident to failed
    Incident incident = runtimeService.createIncidentQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(incident);
    
    // delete the job
    managementService.deleteJob(job.getId());
    
    // the incident has been deleted too.
    incident = runtimeService.createIncidentQuery().incidentId(incident.getId()).singleResult();
    assertNull(incident);
  }
  
  @Deployment(resources = {"org/camunda/bpm/engine/test/incident/IncidentTest.testShouldDeleteIncidentAfterJobWasSuccessfully.bpmn"})
  public void testShouldDeleteIncidentAfterJobWasSuccessfully() {
    // Start process instance
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("fail", true);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingProcessWithUserTask", parameters);
    
    waitForJobExecutorToProcessAllJobs(15000);

    // job exists
    Job job = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(job);
    
    // incident was created
    Incident incident = runtimeService.createIncidentQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(incident);
    
    // set execution variable from "true" to "false"
    runtimeService.setVariable(processInstance.getId(), "fail", new Boolean(false));
    
    // set retries of failed job to 1, with the change of the fail variable the job
    // will be executed successfully
    managementService.setJobRetries(job.getId(), 1);
    
    waitForJobExecutorToProcessAllJobs(15000);
    
    // Update process instance
    processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
    assertTrue(processInstance instanceof ExecutionEntity);
    
    // should stay in the user task
    ExecutionEntity exec = (ExecutionEntity) processInstance;
    assertEquals("theUserTask", exec.getActivityId());
    
    // there does not exist any incident anymore
    incident = runtimeService.createIncidentQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNull(incident);
  }
  
  @Deployment(resources = {"org/camunda/bpm/engine/test/incident/IncidentTest.testShouldCreateIncidentOnFailedStartTimerEvent.bpmn"})
  public void testShouldCreateIncidentOnFailedStartTimerEvent() {
    // After process start, there should be timer created
    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(1, jobQuery.count());
    
    ClockUtil.setCurrentTime(new Date(ClockUtil.getCurrentTime().getTime() + 302 * 1000));
    
    waitForJobExecutorToProcessAllJobs(15000);

    // job exists
    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);
    
    assertEquals(0, job.getRetries());
    
    // incident was created
    Incident incident = runtimeService.createIncidentQuery().configuration(job.getId()).singleResult();
    assertNotNull(incident);
    
    // manually delete job for timer start event
    managementService.deleteJob(job.getId());    
  }
  
}
