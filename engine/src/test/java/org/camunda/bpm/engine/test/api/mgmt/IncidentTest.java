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
package org.camunda.bpm.engine.test.api.mgmt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.IncidentQuery;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.model.bpmn.Bpmn;

public class IncidentTest extends PluggableProcessEngineTestCase {

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/IncidentTest.testShouldCreateOneIncident.bpmn"})
  public void testShouldCreateOneIncident() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingProcess");

    executeAvailableJobs();

    Incident incident = runtimeService.createIncidentQuery().processInstanceId(processInstance.getId()).singleResult();

    assertNotNull(incident);

    assertNotNull(incident.getId());
    assertNotNull(incident.getIncidentTimestamp());
    assertEquals(Incident.FAILED_JOB_HANDLER_TYPE, incident.getIncidentType());
    assertEquals(AlwaysFailingDelegate.MESSAGE, incident.getIncidentMessage());
    assertEquals(processInstance.getId(), incident.getExecutionId());
    assertEquals("theServiceTask", incident.getActivityId());
    assertEquals(processInstance.getId(), incident.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), incident.getProcessDefinitionId());
    assertEquals(incident.getId(), incident.getCauseIncidentId());
    assertEquals(incident.getId(), incident.getRootCauseIncidentId());

    Job job = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();

    assertNotNull(job);

    assertEquals(job.getId(), incident.getConfiguration());
    assertEquals(job.getJobDefinitionId(), incident.getJobDefinitionId());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/IncidentTest.testShouldCreateOneIncident.bpmn"})
  public void testShouldCreateOneIncidentAfterSetRetries() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingProcess");

    executeAvailableJobs();

    List<Incident> incidents = runtimeService.createIncidentQuery().processInstanceId(processInstance.getId()).list();

    assertFalse(incidents.isEmpty());
    assertTrue(incidents.size() == 1);

    Job job = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();

    assertNotNull(job);

    // set job retries to 1 -> should fail again and a second incident should be created
    managementService.setJobRetries(job.getId(), 1);

    executeAvailableJobs();

    incidents = runtimeService.createIncidentQuery().processInstanceId(processInstance.getId()).list();

    // There is still one incident
    assertFalse(incidents.isEmpty());
    assertTrue(incidents.size() == 1);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/IncidentTest.testShouldCreateOneIncident.bpmn"})
  public void testShouldCreateOneIncidentAfterExecuteJob() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingProcess");

    executeAvailableJobs();

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

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/IncidentTest.testShouldCreateOneIncidentForNestedExecution.bpmn"})
  public void testShouldCreateOneIncidentForNestedExecution() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingProcessWithNestedExecutions");

    executeAvailableJobs();

    Incident incident = runtimeService.createIncidentQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(incident);

    Job job = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(job);

    String executionIdOfNestedFailingExecution = job.getExecutionId();

    assertFalse(processInstance.getId() == executionIdOfNestedFailingExecution);

    assertNotNull(incident.getId());
    assertNotNull(incident.getIncidentTimestamp());
    assertEquals(Incident.FAILED_JOB_HANDLER_TYPE, incident.getIncidentType());
    assertEquals(AlwaysFailingDelegate.MESSAGE, incident.getIncidentMessage());
    assertEquals(executionIdOfNestedFailingExecution, incident.getExecutionId());
    assertEquals("theServiceTask", incident.getActivityId());
    assertEquals(processInstance.getId(), incident.getProcessInstanceId());
    assertEquals(incident.getId(), incident.getCauseIncidentId());
    assertEquals(incident.getId(), incident.getRootCauseIncidentId());
    assertEquals(job.getId(), incident.getConfiguration());
    assertEquals(job.getJobDefinitionId(), incident.getJobDefinitionId());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/IncidentTest.testShouldCreateRecursiveIncidents.bpmn",
      "org/camunda/bpm/engine/test/api/mgmt/IncidentTest.testShouldCreateOneIncident.bpmn"})
  public void testShouldCreateRecursiveIncidents() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callFailingProcess");

    executeAvailableJobs();

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
    assertEquals(Incident.FAILED_JOB_HANDLER_TYPE, causeIncident.getIncidentType());
    assertEquals(AlwaysFailingDelegate.MESSAGE, causeIncident.getIncidentMessage());
    assertEquals(job.getExecutionId(), causeIncident.getExecutionId());
    assertEquals("theServiceTask", causeIncident.getActivityId());
    assertEquals(failingProcess.getId(), causeIncident.getProcessInstanceId());
    assertEquals(causeIncident.getId(), causeIncident.getCauseIncidentId());
    assertEquals(causeIncident.getId(), causeIncident.getRootCauseIncidentId());
    assertEquals(job.getId(), causeIncident.getConfiguration());
    assertEquals(job.getJobDefinitionId(), causeIncident.getJobDefinitionId());

    // Recursive created incident
    Incident recursiveCreatedIncident = runtimeService.createIncidentQuery().processDefinitionId(callProcess.getProcessDefinitionId()).singleResult();
    assertNotNull(recursiveCreatedIncident);

    Execution theCallActivityExecution = runtimeService.createExecutionQuery().activityId("theCallActivity").singleResult();
    assertNotNull(theCallActivityExecution);

    assertNotNull(recursiveCreatedIncident.getId());
    assertNotNull(recursiveCreatedIncident.getIncidentTimestamp());
    assertEquals(Incident.FAILED_JOB_HANDLER_TYPE, recursiveCreatedIncident.getIncidentType());
    assertNull(recursiveCreatedIncident.getIncidentMessage());
    assertEquals(theCallActivityExecution.getId(), recursiveCreatedIncident.getExecutionId());
    assertEquals("theCallActivity", recursiveCreatedIncident.getActivityId());
    assertEquals(processInstance.getId(), recursiveCreatedIncident.getProcessInstanceId());
    assertEquals(causeIncident.getId(), recursiveCreatedIncident.getCauseIncidentId());
    assertEquals(causeIncident.getId(), recursiveCreatedIncident.getRootCauseIncidentId());
    assertNull(recursiveCreatedIncident.getConfiguration());
    assertNull(recursiveCreatedIncident.getJobDefinitionId());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/IncidentTest.testShouldCreateRecursiveIncidentsForNestedCallActivity.bpmn",
  		"org/camunda/bpm/engine/test/api/mgmt/IncidentTest.testShouldCreateRecursiveIncidents.bpmn",
  "org/camunda/bpm/engine/test/api/mgmt/IncidentTest.testShouldCreateOneIncident.bpmn"})
  public void testShouldCreateRecursiveIncidentsForNestedCallActivity() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callingFailingCallActivity");

    executeAvailableJobs();

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
    assertEquals(Incident.FAILED_JOB_HANDLER_TYPE, rootCauseIncident.getIncidentType());
    assertEquals(AlwaysFailingDelegate.MESSAGE, rootCauseIncident.getIncidentMessage());
    assertEquals(job.getExecutionId(), rootCauseIncident.getExecutionId());
    assertEquals("theServiceTask", rootCauseIncident.getActivityId());
    assertEquals(failingProcess.getId(), rootCauseIncident.getProcessInstanceId());
    assertEquals(rootCauseIncident.getId(), rootCauseIncident.getCauseIncidentId());
    assertEquals(rootCauseIncident.getId(), rootCauseIncident.getRootCauseIncidentId());
    assertEquals(job.getId(), rootCauseIncident.getConfiguration());
    assertEquals(job.getJobDefinitionId(), rootCauseIncident.getJobDefinitionId());

    // Cause Incident
    ProcessInstance callFailingProcess = runtimeService.createProcessInstanceQuery().processDefinitionKey("callFailingProcess").singleResult();
    assertNotNull(callFailingProcess);

    Incident causeIncident = runtimeService.createIncidentQuery().processDefinitionId(callFailingProcess.getProcessDefinitionId()).singleResult();
    assertNotNull(causeIncident);

    Execution theCallActivityExecution = runtimeService.createExecutionQuery().activityId("theCallActivity").singleResult();
    assertNotNull(theCallActivityExecution);

    assertNotNull(causeIncident.getId());
    assertNotNull(causeIncident.getIncidentTimestamp());
    assertEquals(Incident.FAILED_JOB_HANDLER_TYPE, causeIncident.getIncidentType());
    assertNull(causeIncident.getIncidentMessage());
    assertEquals(theCallActivityExecution.getId(), causeIncident.getExecutionId());
    assertEquals("theCallActivity", causeIncident.getActivityId());
    assertEquals(callFailingProcess.getId(), causeIncident.getProcessInstanceId());
    assertEquals(rootCauseIncident.getId(), causeIncident.getCauseIncidentId());
    assertEquals(rootCauseIncident.getId(), causeIncident.getRootCauseIncidentId());
    assertNull(causeIncident.getConfiguration());
    assertNull(causeIncident.getJobDefinitionId());

    // Top level incident of the startet process (recursive created incident for super super process instance)
    Incident topLevelIncident = runtimeService.createIncidentQuery().processDefinitionId(processInstance.getProcessDefinitionId()).singleResult();
    assertNotNull(topLevelIncident);

    Execution theCallingCallActivity = runtimeService.createExecutionQuery().activityId("theCallingCallActivity").singleResult();
    assertNotNull(theCallingCallActivity);

    assertNotNull(topLevelIncident.getId());
    assertNotNull(topLevelIncident.getIncidentTimestamp());
    assertEquals(Incident.FAILED_JOB_HANDLER_TYPE, topLevelIncident.getIncidentType());
    assertNull(topLevelIncident.getIncidentMessage());
    assertEquals(theCallingCallActivity.getId(), topLevelIncident.getExecutionId());
    assertEquals("theCallingCallActivity", topLevelIncident.getActivityId());
    assertEquals(processInstance.getId(), topLevelIncident.getProcessInstanceId());
    assertEquals(causeIncident.getId(), topLevelIncident.getCauseIncidentId());
    assertEquals(rootCauseIncident.getId(), topLevelIncident.getRootCauseIncidentId());
    assertNull(topLevelIncident.getConfiguration());
    assertNull(topLevelIncident.getJobDefinitionId());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/IncidentTest.testShouldCreateOneIncident.bpmn"})
  public void testShouldDeleteIncidentAfterJobHasBeenDeleted() {
    // start failing process
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingProcess");

    executeAvailableJobs();

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

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/IncidentTest.testShouldDeleteIncidentAfterJobWasSuccessfully.bpmn"})
  public void testShouldDeleteIncidentAfterJobWasSuccessfully() {
    // Start process instance
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("fail", true);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingProcessWithUserTask", parameters);

    executeAvailableJobs();

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

    executeAvailableJobs();

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

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/IncidentTest.testShouldCreateIncidentOnFailedStartTimerEvent.bpmn"})
  public void testShouldCreateIncidentOnFailedStartTimerEvent() {
    // After process start, there should be timer created
    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(1, jobQuery.count());

    Job job = jobQuery.singleResult();
    String jobId = job.getId();

    while(0 != job.getRetries()) {
      try {
        managementService.executeJob(jobId);
        fail();
      } catch (Exception e) {
        // expected
      }
      job = jobQuery.jobId(jobId).singleResult();

    }

    // job exists
    job = jobQuery.singleResult();
    assertNotNull(job);

    assertEquals(0, job.getRetries());

    // incident was created
    Incident incident = runtimeService.createIncidentQuery().configuration(job.getId()).singleResult();
    assertNotNull(incident);

    // manually delete job for timer start event
    managementService.deleteJob(job.getId());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/IncidentTest.testShouldCreateOneIncident.bpmn"})
  public void testDoNotCreateNewIncident() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingProcess");

    executeAvailableJobs();

    IncidentQuery query = runtimeService.createIncidentQuery().processInstanceId(processInstance.getId());
    Incident incident = query.singleResult();
    assertNotNull(incident);

    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // set retries to 1 by job definition id
    managementService.setJobRetriesByJobDefinitionId(jobDefinition.getId(), 1);

    // the incident still exists
    Incident tmp = query.singleResult();
    assertEquals(incident.getId(), tmp.getId());

    // execute the available job (should fail again)
    executeAvailableJobs();

    // the incident still exists and there
    // should be not a new incident
    assertEquals(1, query.count());
    tmp = query.singleResult();
    assertEquals(incident.getId(), tmp.getId());
  }

  @Deployment
  public void testIncidentUpdateAfterCompaction() {
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    executeAvailableJobs();

    Incident incident = runtimeService.createIncidentQuery().singleResult();
    assertNotNull(incident);
    assertNotSame(processInstanceId, incident.getExecutionId());

    runtimeService.correlateMessage("Message");

    incident = runtimeService.createIncidentQuery().singleResult();
    assertNotNull(incident);

    // incident updated with new execution id after execution tree is compacted
    assertEquals(processInstanceId, incident.getExecutionId());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/IncidentTest.testShouldCreateOneIncident.bpmn"})
  public void testDoNotSetNegativeRetries() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingProcess");

    executeAvailableJobs();

    // it exists a job with 0 retries and an incident
    Job job = managementService.createJobQuery().singleResult();
    assertEquals(0, job.getRetries());

    assertEquals(1, runtimeService.createIncidentQuery().count());

    // it should not be possible to set negative retries
    final JobEntity jobEntity = (JobEntity) job;
    processEngineConfiguration
      .getCommandExecutorTxRequired()
      .execute(new Command<Void>() {
        public Void execute(CommandContext commandContext) {
          jobEntity.setRetries(-100);
          return null;
        }
      });

    assertEquals(0, job.getRetries());

    // retries should still be 0 after execution this job again
    try {
      managementService.executeJob(job.getId());
      fail("Exception expected");
    }
    catch (ProcessEngineException e) {
      // expected
    }

    job = managementService.createJobQuery().singleResult();
    assertEquals(0, job.getRetries());

    // also no new incident was created
    assertEquals(1, runtimeService.createIncidentQuery().count());

    // it should not be possible to set the retries to a negative number with the management service
    try {
      managementService.setJobRetries(job.getId(), -200);
      fail("Exception expected");
    }
    catch (ProcessEngineException e) {
      // expected
    }

    try {
      managementService.setJobRetriesByJobDefinitionId(job.getJobDefinitionId(), -300);
      fail("Exception expected");
    }
    catch (ProcessEngineException e) {
      // expected
    }

  }

  @Deployment
  public void testActivityIdProperty() {
    executeAvailableJobs();

    Incident incident = runtimeService
      .createIncidentQuery()
      .singleResult();

    assertNotNull(incident);

    assertNotNull(incident.getActivityId());
    assertEquals("theStart", incident.getActivityId());
    assertNull(incident.getProcessInstanceId());
    assertNull(incident.getExecutionId());
  }

  public void testBoundaryEventIncidentActivityId() {
    deployment(Bpmn.createExecutableProcess("process")
        .startEvent()
        .userTask("userTask")
        .endEvent()
        .moveToActivity("userTask")
        .boundaryEvent("boundaryEvent")
        .timerWithDuration("PT5S")
        .endEvent()
        .done());

    // given
    runtimeService.startProcessInstanceByKey("process");
    Job timerJob = managementService.createJobQuery().singleResult();

    // when creating an incident
    managementService.setJobRetries(timerJob.getId(), 0);

    // then
    Incident incident = runtimeService.createIncidentQuery().singleResult();
    assertNotNull(incident);
    assertEquals("boundaryEvent", incident.getActivityId());
  }

}
