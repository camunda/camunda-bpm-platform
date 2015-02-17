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
package org.camunda.bpm.engine.test.history;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.history.HistoricJobLogQuery;
import org.camunda.bpm.engine.impl.jobexecutor.AsyncContinuationJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.ProcessEventJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerCatchIntermediateEventJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerExecuteNestedActivityJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerStartEventSubprocessJobHandler;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.api.runtime.FailingDelegate;
import org.camunda.bpm.engine.variable.Variables;

/**
 * @author Roman Smirnov
 *
 */
public class HistoricJobLogTest extends PluggableProcessEngineTestCase {

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testCreateHistoricJobLogProperties() {
    runtimeService.startProcessInstanceByKey("process");

    Job job = managementService
        .createJobQuery()
        .singleResult();

    HistoricJobLog historicJob = historyService
        .createHistoricJobLogQuery()
        .created()
        .singleResult();
    assertNotNull(historicJob);

    assertNotNull(historicJob.getTimestamp());

    assertNull(historicJob.getJobExceptionMessage());

    assertEquals(job.getId(), historicJob.getJobId());
    assertEquals(job.getJobDefinitionId(), historicJob.getJobDefinitionId());
    assertEquals("serviceTask", historicJob.getActivityId());
    assertEquals(MessageEntity.TYPE, historicJob.getJobType());
    assertEquals(AsyncContinuationJobHandler.TYPE, historicJob.getJobHandlerType());
    assertEquals(job.getDuedate(), historicJob.getJobDueDate());
    assertEquals(job.getRetries(), historicJob.getJobRetries());
    assertEquals(job.getExecutionId(), historicJob.getExecutionId());
    assertEquals(job.getProcessInstanceId(), historicJob.getProcessInstanceId());
    assertEquals(job.getProcessDefinitionId(), historicJob.getProcessDefinitionId());
    assertEquals(job.getProcessDefinitionKey(), historicJob.getProcessDefinitionKey());
    assertEquals(job.getDeploymentId(), historicJob.getDeploymentId());

    assertTrue(historicJob.isCreated());
    assertFalse(historicJob.isFailed());
    assertFalse(historicJob.isSuccessful());
    assertFalse(historicJob.isDeleted());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testFailedHistoricJobLogProperties() {
    runtimeService.startProcessInstanceByKey("process");

    Job job = managementService
        .createJobQuery()
        .singleResult();

    try {
      managementService.executeJob(job.getId());
      fail();
    } catch (Exception e) {
      // expected
    }

    HistoricJobLog historicJob = historyService
        .createHistoricJobLogQuery()
        .failed()
        .singleResult();
    assertNotNull(historicJob);

    assertNotNull(historicJob.getTimestamp());

    assertEquals(job.getId(), historicJob.getJobId());
    assertEquals(job.getJobDefinitionId(), historicJob.getJobDefinitionId());
    assertEquals("serviceTask", historicJob.getActivityId());
    assertEquals(MessageEntity.TYPE, historicJob.getJobType());
    assertEquals(AsyncContinuationJobHandler.TYPE, historicJob.getJobHandlerType());
    assertEquals(job.getDuedate(), historicJob.getJobDueDate());
    assertEquals(2, historicJob.getJobRetries());
    assertEquals(job.getExecutionId(), historicJob.getExecutionId());
    assertEquals(job.getProcessInstanceId(), historicJob.getProcessInstanceId());
    assertEquals(job.getProcessDefinitionId(), historicJob.getProcessDefinitionId());
    assertEquals(job.getProcessDefinitionKey(), historicJob.getProcessDefinitionKey());
    assertEquals(job.getDeploymentId(), historicJob.getDeploymentId());
    assertEquals(FailingDelegate.EXCEPTION_MESSAGE, historicJob.getJobExceptionMessage());

    assertFalse(historicJob.isCreated());
    assertTrue(historicJob.isFailed());
    assertFalse(historicJob.isSuccessful());
    assertFalse(historicJob.isDeleted());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testSuccessfulHistoricJobLogProperties() {
    runtimeService.startProcessInstanceByKey("process", Variables.createVariables().putValue("fail", false));

    Job job = managementService
        .createJobQuery()
        .singleResult();

    managementService.executeJob(job.getId());

    HistoricJobLog historicJob = historyService
        .createHistoricJobLogQuery()
        .successful()
        .singleResult();
    assertNotNull(historicJob);

    assertNotNull(historicJob.getTimestamp());

    assertNull(historicJob.getJobExceptionMessage());

    assertEquals(job.getId(), historicJob.getJobId());
    assertEquals(job.getJobDefinitionId(), historicJob.getJobDefinitionId());
    assertEquals("serviceTask", historicJob.getActivityId());
    assertEquals(MessageEntity.TYPE, historicJob.getJobType());
    assertEquals(AsyncContinuationJobHandler.TYPE, historicJob.getJobHandlerType());
    assertEquals(job.getDuedate(), historicJob.getJobDueDate());
    assertEquals(job.getRetries(), historicJob.getJobRetries());
    assertEquals(job.getExecutionId(), historicJob.getExecutionId());
    assertEquals(job.getProcessInstanceId(), historicJob.getProcessInstanceId());
    assertEquals(job.getProcessDefinitionId(), historicJob.getProcessDefinitionId());
    assertEquals(job.getProcessDefinitionKey(), historicJob.getProcessDefinitionKey());
    assertEquals(job.getDeploymentId(), historicJob.getDeploymentId());

    assertFalse(historicJob.isCreated());
    assertFalse(historicJob.isFailed());
    assertTrue(historicJob.isSuccessful());
    assertFalse(historicJob.isDeleted());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testDeletedHistoricJobLogProperties() {
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    Job job = managementService
        .createJobQuery()
        .singleResult();

    runtimeService.deleteProcessInstance(processInstanceId, null);

    HistoricJobLog historicJob = historyService
        .createHistoricJobLogQuery()
        .deleted()
        .singleResult();
    assertNotNull(historicJob);

    assertNotNull(historicJob.getTimestamp());

    assertNull(historicJob.getJobExceptionMessage());

    assertEquals(job.getId(), historicJob.getJobId());
    assertEquals(job.getJobDefinitionId(), historicJob.getJobDefinitionId());
    assertEquals("serviceTask", historicJob.getActivityId());
    assertEquals(MessageEntity.TYPE, historicJob.getJobType());
    assertEquals(AsyncContinuationJobHandler.TYPE, historicJob.getJobHandlerType());
    assertEquals(job.getDuedate(), historicJob.getJobDueDate());
    assertEquals(job.getRetries(), historicJob.getJobRetries());
    assertEquals(job.getExecutionId(), historicJob.getExecutionId());
    assertEquals(job.getProcessInstanceId(), historicJob.getProcessInstanceId());
    assertEquals(job.getProcessDefinitionId(), historicJob.getProcessDefinitionId());
    assertEquals(job.getProcessDefinitionKey(), historicJob.getProcessDefinitionKey());
    assertEquals(job.getDeploymentId(), historicJob.getDeploymentId());

    assertFalse(historicJob.isCreated());
    assertFalse(historicJob.isFailed());
    assertFalse(historicJob.isSuccessful());
    assertTrue(historicJob.isDeleted());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testAsyncBeforeJobHandlerType() {
    runtimeService.startProcessInstanceByKey("process");

    Job job = managementService
        .createJobQuery()
        .singleResult();

    HistoricJobLog historicJob = historyService
        .createHistoricJobLogQuery()
        .jobId(job.getId())
        .singleResult();

    assertNotNull(historicJob);

    assertNull(historicJob.getJobDueDate());

    assertEquals(job.getJobDefinitionId(), historicJob.getJobDefinitionId());
    assertEquals("serviceTask", historicJob.getActivityId());
    assertEquals(AsyncContinuationJobHandler.TYPE, historicJob.getJobHandlerType());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testAsyncAfterJobHandlerType() {
    runtimeService.startProcessInstanceByKey("process", Variables.createVariables().putValue("fail", false));

    Job job = managementService
        .createJobQuery()
        .singleResult();

    managementService.executeJob(job.getId());

    Job anotherJob = managementService
        .createJobQuery()
        .singleResult();

    assertFalse(job.getId().equals(anotherJob.getId()));

    HistoricJobLog historicJob = historyService
        .createHistoricJobLogQuery()
        .jobId(anotherJob.getId())
        .singleResult();

    assertNotNull(historicJob);

    assertNull(historicJob.getJobDueDate());

    assertEquals(anotherJob.getJobDefinitionId(), historicJob.getJobDefinitionId());
    assertEquals("serviceTask", historicJob.getActivityId());
    assertEquals(AsyncContinuationJobHandler.TYPE, historicJob.getJobHandlerType());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testStartTimerEvent.bpmn20.xml"})
  public void testStartTimerEventJobHandlerType() {
    Job job = managementService
        .createJobQuery()
        .singleResult();

    HistoricJobLog historicJob = historyService
        .createHistoricJobLogQuery()
        .jobId(job.getId())
        .singleResult();

    assertNotNull(historicJob);

    assertEquals(job.getId(), historicJob.getJobId());
    assertEquals(job.getJobDefinitionId(), historicJob.getJobDefinitionId());
    assertEquals("theStart", historicJob.getActivityId());
    assertEquals(TimerStartEventJobHandler.TYPE, historicJob.getJobHandlerType());
    assertEquals(job.getDuedate(), historicJob.getJobDueDate());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testStartTimerEventInsideEventSubProcess.bpmn20.xml"})
  public void testStartTimerEventInsideEventSubProcessJobHandlerType() {
    runtimeService.startProcessInstanceByKey("process");

    Job job = managementService
        .createJobQuery()
        .singleResult();

    HistoricJobLog historicJob = historyService
        .createHistoricJobLogQuery()
        .jobId(job.getId())
        .singleResult();

    assertNotNull(historicJob);

    assertEquals(job.getId(), historicJob.getJobId());
    assertEquals(job.getJobDefinitionId(), historicJob.getJobDefinitionId());
    assertEquals("subprocessStartEvent", historicJob.getActivityId());
    assertEquals(TimerStartEventSubprocessJobHandler.TYPE, historicJob.getJobHandlerType());
    assertEquals(job.getDuedate(), historicJob.getJobDueDate());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testIntermediateTimerEvent.bpmn20.xml"})
  public void testIntermediateTimerEventJobHandlerType() {
    runtimeService.startProcessInstanceByKey("process");

    Job job = managementService
        .createJobQuery()
        .singleResult();

    HistoricJobLog historicJob = historyService
        .createHistoricJobLogQuery()
        .jobId(job.getId())
        .singleResult();

    assertNotNull(historicJob);

    assertEquals(job.getId(), historicJob.getJobId());
    assertEquals(job.getJobDefinitionId(), historicJob.getJobDefinitionId());
    assertEquals("timer", historicJob.getActivityId());
    assertEquals(TimerCatchIntermediateEventJobHandler.TYPE, historicJob.getJobHandlerType());
    assertEquals(job.getDuedate(), historicJob.getJobDueDate());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testBoundaryTimerEvent.bpmn20.xml"})
  public void testBoundaryTimerEventJobHandlerType() {
    runtimeService.startProcessInstanceByKey("process");

    Job job = managementService
        .createJobQuery()
        .singleResult();

    HistoricJobLog historicJob = historyService
        .createHistoricJobLogQuery()
        .jobId(job.getId())
        .singleResult();

    assertNotNull(historicJob);

    assertEquals(job.getId(), historicJob.getJobId());
    assertEquals(job.getJobDefinitionId(), historicJob.getJobDefinitionId());
    assertEquals("timer", historicJob.getActivityId());
    assertEquals(TimerExecuteNestedActivityJobHandler.TYPE, historicJob.getJobHandlerType());
    assertEquals(job.getDuedate(), historicJob.getJobDueDate());
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/history/HistoricJobLogTest.testCatchingSignalEvent.bpmn20.xml",
      "org/camunda/bpm/engine/test/history/HistoricJobLogTest.testThrowingSignalEventAsync.bpmn20.xml"
  })
  public void testCatchingSignalEventJobHandlerType() {
    runtimeService.startProcessInstanceByKey("catchSignal");
    runtimeService.startProcessInstanceByKey("throwSignal");

    Job job = managementService
        .createJobQuery()
        .singleResult();

    HistoricJobLog historicJob = historyService
        .createHistoricJobLogQuery()
        .jobId(job.getId())
        .singleResult();

    assertNotNull(historicJob);

    assertNull(historicJob.getJobDueDate());

    assertEquals(job.getId(), historicJob.getJobId());
    assertEquals(job.getJobDefinitionId(), historicJob.getJobDefinitionId());
    assertEquals("signalEvent", historicJob.getActivityId());
    assertEquals(ProcessEventJobHandler.TYPE, historicJob.getJobHandlerType());
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/history/HistoricJobLogTest.testCatchingSignalEvent.bpmn20.xml",
      "org/camunda/bpm/engine/test/history/HistoricJobLogTest.testThrowingSignalEventAsync.bpmn20.xml"
  })
  public void testCatchingSignalEventActivityId() {
    // given + when (1)
    String processInstanceId = runtimeService.startProcessInstanceByKey("catchSignal").getId();
    runtimeService.startProcessInstanceByKey("throwSignal");

    String jobId = managementService.createJobQuery().singleResult().getId();

    // then (1)

    HistoricJobLog historicJob = historyService
        .createHistoricJobLogQuery()
        .jobId(jobId)
        .created()
        .singleResult();
    assertNotNull(historicJob);

    assertEquals("signalEvent", historicJob.getActivityId());

    // when (2)
    try {
      managementService.executeJob(jobId);
      fail();
    } catch (Exception e) {
      // expected
    }

    // then (2)
    historicJob = historyService
        .createHistoricJobLogQuery()
        .jobId(jobId)
        .failed()
        .singleResult();
    assertNotNull(historicJob);

    assertEquals("signalEvent", historicJob.getActivityId());

    // when (3)
    runtimeService.setVariable(processInstanceId, "fail", false);
    managementService.executeJob(jobId);

    // then (3)

    historicJob = historyService
        .createHistoricJobLogQuery()
        .jobId(jobId)
        .successful()
        .singleResult();
    assertNotNull(historicJob);

    assertEquals("signalEvent", historicJob.getActivityId());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testFailedJobEvents() {
    // given
    runtimeService.startProcessInstanceByKey("process");

    String jobId = managementService.createJobQuery().singleResult().getId();

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().jobId(jobId);
    HistoricJobLogQuery createdQuery = historyService.createHistoricJobLogQuery().jobId(jobId).created();
    HistoricJobLogQuery failedQuery = historyService.createHistoricJobLogQuery().jobId(jobId).failed().orderByJobRetries().desc();

    // there exists one historic job log entry
    assertEquals(1, query.count());
    assertEquals(1, createdQuery.count());
    assertEquals(0, failedQuery.count());

    // when (1)
    try {
      managementService.executeJob(jobId);
      fail();
    } catch (Exception e) {
      // expected
    }

    // then (1)
    assertEquals(2, query.count());
    assertEquals(1, createdQuery.count());
    assertEquals(1, failedQuery.count());

    HistoricJobLog createdJobLogEntry = createdQuery.singleResult();
    assertEquals(3, createdJobLogEntry.getJobRetries());

    HistoricJobLog failedJobLogEntry = failedQuery.singleResult();
    assertEquals(2, failedJobLogEntry.getJobRetries());

    // when (2)
    try {
      managementService.executeJob(jobId);
      fail();
    } catch (Exception e) {
      // expected
    }

    // then (2)
    assertEquals(3, query.count());
    assertEquals(1, createdQuery.count());
    assertEquals(2, failedQuery.count());

    createdJobLogEntry = createdQuery.singleResult();
    assertEquals(3, createdJobLogEntry.getJobRetries());

    failedJobLogEntry = failedQuery.list().get(0);
    assertEquals(2, failedJobLogEntry.getJobRetries());

    failedJobLogEntry = failedQuery.list().get(1);
    assertEquals(1, failedJobLogEntry.getJobRetries());

    // when (3)
    try {
      managementService.executeJob(jobId);
      fail();
    } catch (Exception e) {
      // expected
    }

    // then (3)
    assertEquals(4, query.count());
    assertEquals(1, createdQuery.count());
    assertEquals(3, failedQuery.count());

    createdJobLogEntry = createdQuery.singleResult();
    assertEquals(3, createdJobLogEntry.getJobRetries());

    failedJobLogEntry = failedQuery.list().get(0);
    assertEquals(2, failedJobLogEntry.getJobRetries());

    failedJobLogEntry = failedQuery.list().get(1);
    assertEquals(1, failedJobLogEntry.getJobRetries());

    failedJobLogEntry = failedQuery.list().get(2);
    assertEquals(0, failedJobLogEntry.getJobRetries());

    // when (4)
    try {
      managementService.executeJob(jobId);
      fail();
    } catch (Exception e) {
      // expected
    }

    // then (4)
    assertEquals(5, query.count());
    assertEquals(1, createdQuery.count());
    assertEquals(4, failedQuery.count());

    createdJobLogEntry = createdQuery.singleResult();
    assertEquals(3, createdJobLogEntry.getJobRetries());

    failedJobLogEntry = failedQuery.list().get(0);
    assertEquals(2, failedJobLogEntry.getJobRetries());

    failedJobLogEntry = failedQuery.list().get(1);
    assertEquals(1, failedJobLogEntry.getJobRetries());

    failedJobLogEntry = failedQuery.list().get(2);
    assertEquals(0, failedJobLogEntry.getJobRetries());

    failedJobLogEntry = failedQuery.list().get(3);
    assertEquals(0, failedJobLogEntry.getJobRetries());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testFailedJobEventsExecutedByJobExecutor() {
    // given
    runtimeService.startProcessInstanceByKey("process");

    String jobId = managementService.createJobQuery().singleResult().getId();

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().jobId(jobId);
    HistoricJobLogQuery createdQuery = historyService.createHistoricJobLogQuery().jobId(jobId).created();
    HistoricJobLogQuery failedQuery = historyService.createHistoricJobLogQuery().jobId(jobId).failed().orderByJobRetries().desc();

    // there exists one historic job log entry
    assertEquals(1, query.count());
    assertEquals(1, createdQuery.count());
    assertEquals(0, failedQuery.count());

    // when
    executeAvailableJobs();

    // then
    assertEquals(4, query.count());
    assertEquals(1, createdQuery.count());
    assertEquals(3, failedQuery.count());

    HistoricJobLog createdJobLogEntry = createdQuery.singleResult();
    assertEquals(3, createdJobLogEntry.getJobRetries());

    HistoricJobLog failedJobLogEntry = failedQuery.list().get(0);
    assertEquals(2, failedJobLogEntry.getJobRetries());

    failedJobLogEntry = failedQuery.list().get(1);
    assertEquals(1, failedJobLogEntry.getJobRetries());

    failedJobLogEntry = failedQuery.list().get(2);
    assertEquals(0, failedJobLogEntry.getJobRetries());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testSuccessfulJobEvent() {
    // given
    runtimeService.startProcessInstanceByKey("process", Variables.createVariables().putValue("fail", false));

    String jobId = managementService.createJobQuery().singleResult().getId();

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().jobId(jobId);
    HistoricJobLogQuery createdQuery = historyService.createHistoricJobLogQuery().jobId(jobId).created();
    HistoricJobLogQuery succeededQuery = historyService.createHistoricJobLogQuery().jobId(jobId).successful();

    // there exists one historic job log entry
    assertEquals(1, query.count());
    assertEquals(1, createdQuery.count());
    assertEquals(0, succeededQuery.count());

    // when
    managementService.executeJob(jobId);

    // then
    assertEquals(2, query.count());
    assertEquals(1, createdQuery.count());
    assertEquals(1, succeededQuery.count());

    HistoricJobLog createdJobLogEntry = createdQuery.singleResult();
    assertEquals(3, createdJobLogEntry.getJobRetries());

    HistoricJobLog succeededJobLogEntry = succeededQuery.singleResult();
    assertEquals(3, succeededJobLogEntry.getJobRetries());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testSuccessfulJobEventExecutedByJobExecutor() {
    // given
    runtimeService.startProcessInstanceByKey("process", Variables.createVariables().putValue("fail", false));

    String jobId = managementService.createJobQuery().singleResult().getId();

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().jobId(jobId);
    HistoricJobLogQuery createdQuery = historyService.createHistoricJobLogQuery().jobId(jobId).created();
    HistoricJobLogQuery succeededQuery = historyService.createHistoricJobLogQuery().jobId(jobId).successful();

    // there exists one historic job log entry
    assertEquals(1, query.count());
    assertEquals(1, createdQuery.count());
    assertEquals(0, succeededQuery.count());

    // when
    executeAvailableJobs();

    // then
    assertEquals(2, query.count());
    assertEquals(1, createdQuery.count());
    assertEquals(1, succeededQuery.count());

    HistoricJobLog createdJobLogEntry = createdQuery.singleResult();
    assertEquals(3, createdJobLogEntry.getJobRetries());

    HistoricJobLog succeededJobLogEntry = succeededQuery.singleResult();
    assertEquals(3, succeededJobLogEntry.getJobRetries());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testSuccessfulAndFailedJobEvents() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    String jobId = managementService.createJobQuery().singleResult().getId();

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().jobId(jobId);
    HistoricJobLogQuery createdQuery = historyService.createHistoricJobLogQuery().jobId(jobId).created();
    HistoricJobLogQuery failedQuery = historyService.createHistoricJobLogQuery().jobId(jobId).failed().orderByJobRetries().desc();
    HistoricJobLogQuery succeededQuery = historyService.createHistoricJobLogQuery().jobId(jobId).successful();

    // there exists one historic job log entry
    assertEquals(1, query.count());
    assertEquals(1, createdQuery.count());
    assertEquals(0, failedQuery.count());
    assertEquals(0, succeededQuery.count());

    // when (1)
    try {
      managementService.executeJob(jobId);
      fail();
    } catch (Exception e) {
      // expected
    }

    // then (1)
    assertEquals(2, query.count());
    assertEquals(1, createdQuery.count());
    assertEquals(1, failedQuery.count());
    assertEquals(0, succeededQuery.count());

    HistoricJobLog createdJobLogEntry = createdQuery.singleResult();
    assertEquals(3, createdJobLogEntry.getJobRetries());

    HistoricJobLog failedJobLogEntry = failedQuery.singleResult();
    assertEquals(2, failedJobLogEntry.getJobRetries());

    // when (2)
    try {
      managementService.executeJob(jobId);
      fail();
    } catch (Exception e) {
      // expected
    }

    // then (2)
    assertEquals(3, query.count());
    assertEquals(1, createdQuery.count());
    assertEquals(2, failedQuery.count());
    assertEquals(0, succeededQuery.count());

    createdJobLogEntry = createdQuery.singleResult();
    assertEquals(3, createdJobLogEntry.getJobRetries());

    failedJobLogEntry = failedQuery.list().get(0);
    assertEquals(2, failedJobLogEntry.getJobRetries());

    failedJobLogEntry = failedQuery.list().get(1);
    assertEquals(1, failedJobLogEntry.getJobRetries());

    // when (3)
    runtimeService.setVariable(processInstanceId, "fail", false);
    managementService.executeJob(jobId);

    // then (3)
    assertEquals(4, query.count());
    assertEquals(1, createdQuery.count());
    assertEquals(2, failedQuery.count());
    assertEquals(1, succeededQuery.count());

    createdJobLogEntry = createdQuery.singleResult();
    assertEquals(3, createdJobLogEntry.getJobRetries());

    failedJobLogEntry = failedQuery.list().get(0);
    assertEquals(2, failedJobLogEntry.getJobRetries());

    failedJobLogEntry = failedQuery.list().get(1);
    assertEquals(1, failedJobLogEntry.getJobRetries());

    HistoricJobLog succeededJobLogEntry = succeededQuery.singleResult();
    assertEquals(1, succeededJobLogEntry.getJobRetries());
  }

  @Deployment
  public void testTerminateEndEvent() {
    // given
    runtimeService.startProcessInstanceByKey("process").getId();

    String serviceTask1JobId = managementService.createJobQuery().activityId("serviceTask1").singleResult().getId();

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery();
    assertEquals(2, query.count());

    // serviceTask1
    HistoricJobLogQuery serviceTask1Query = historyService.createHistoricJobLogQuery().jobId(serviceTask1JobId);
    HistoricJobLogQuery serviceTask1CreatedQuery = historyService.createHistoricJobLogQuery().jobId(serviceTask1JobId).created();
    HistoricJobLogQuery serviceTask1DeletedQuery = historyService.createHistoricJobLogQuery().jobId(serviceTask1JobId).deleted();
    HistoricJobLogQuery serviceTask1SuccessfulQuery = historyService.createHistoricJobLogQuery().jobId(serviceTask1JobId).successful();

    assertEquals(1, serviceTask1Query.count());
    assertEquals(1, serviceTask1CreatedQuery.count());
    assertEquals(0, serviceTask1DeletedQuery.count());
    assertEquals(0, serviceTask1SuccessfulQuery.count());

    // serviceTask2
    String serviceTask2JobId = managementService.createJobQuery().activityId("serviceTask2").singleResult().getId();

    HistoricJobLogQuery serviceTask2Query = historyService.createHistoricJobLogQuery().jobId(serviceTask2JobId);
    HistoricJobLogQuery serviceTask2CreatedQuery = historyService.createHistoricJobLogQuery().jobId(serviceTask2JobId).created();
    HistoricJobLogQuery serviceTask2DeletedQuery = historyService.createHistoricJobLogQuery().jobId(serviceTask2JobId).deleted();
    HistoricJobLogQuery serviceTask2SuccessfulQuery = historyService.createHistoricJobLogQuery().jobId(serviceTask2JobId).successful();

    assertEquals(1, serviceTask2Query.count());
    assertEquals(1, serviceTask2CreatedQuery.count());
    assertEquals(0, serviceTask2DeletedQuery.count());
    assertEquals(0, serviceTask2SuccessfulQuery.count());

    // when
    managementService.executeJob(serviceTask1JobId);

    // then
    assertEquals(4, query.count());

    // serviceTas1
    assertEquals(2, serviceTask1Query.count());
    assertEquals(1, serviceTask1CreatedQuery.count());
    assertEquals(0, serviceTask1DeletedQuery.count());
    assertEquals(1, serviceTask1SuccessfulQuery.count());

    HistoricJobLog serviceTask1CreatedJobLogEntry = serviceTask1CreatedQuery.singleResult();
    assertEquals(3, serviceTask1CreatedJobLogEntry.getJobRetries());

    HistoricJobLog serviceTask1SuccessfulJobLogEntry = serviceTask1SuccessfulQuery.singleResult();
    assertEquals(3, serviceTask1SuccessfulJobLogEntry.getJobRetries());

    // serviceTask2
    assertEquals(2, serviceTask2Query.count());
    assertEquals(1, serviceTask2CreatedQuery.count());
    assertEquals(1, serviceTask2DeletedQuery.count());
    assertEquals(0, serviceTask2SuccessfulQuery.count());

    HistoricJobLog serviceTask2CreatedJobLogEntry = serviceTask2CreatedQuery.singleResult();
    assertEquals(3, serviceTask2CreatedJobLogEntry.getJobRetries());

    HistoricJobLog serviceTask2DeletedJobLogEntry = serviceTask2DeletedQuery.singleResult();
    assertEquals(3, serviceTask2DeletedJobLogEntry.getJobRetries());
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/history/HistoricJobLogTest.testSuperProcessWithCallActivity.bpmn20.xml",
      "org/camunda/bpm/engine/test/history/HistoricJobLogTest.testSubProcessWithErrorEndEvent.bpmn20.xml"
  })
  public void testErrorEndEventInterruptingCallActivity() {
    // given
    runtimeService.startProcessInstanceByKey("process").getId();

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery();
    assertEquals(2, query.count());

    // serviceTask1
    String serviceTask1JobId = managementService.createJobQuery().activityId("serviceTask1").singleResult().getId();

    HistoricJobLogQuery serviceTask1Query = historyService.createHistoricJobLogQuery().jobId(serviceTask1JobId);
    HistoricJobLogQuery serviceTask1CreatedQuery = historyService.createHistoricJobLogQuery().jobId(serviceTask1JobId).created();
    HistoricJobLogQuery serviceTask1DeletedQuery = historyService.createHistoricJobLogQuery().jobId(serviceTask1JobId).deleted();
    HistoricJobLogQuery serviceTask1SuccessfulQuery = historyService.createHistoricJobLogQuery().jobId(serviceTask1JobId).successful();

    assertEquals(1, serviceTask1Query.count());
    assertEquals(1, serviceTask1CreatedQuery.count());
    assertEquals(0, serviceTask1DeletedQuery.count());
    assertEquals(0, serviceTask1SuccessfulQuery.count());

    // serviceTask2
    String serviceTask2JobId = managementService.createJobQuery().activityId("serviceTask2").singleResult().getId();

    HistoricJobLogQuery serviceTask2Query = historyService.createHistoricJobLogQuery().jobId(serviceTask2JobId);
    HistoricJobLogQuery serviceTask2CreatedQuery = historyService.createHistoricJobLogQuery().jobId(serviceTask2JobId).created();
    HistoricJobLogQuery serviceTask2DeletedQuery = historyService.createHistoricJobLogQuery().jobId(serviceTask2JobId).deleted();
    HistoricJobLogQuery serviceTask2SuccessfulQuery = historyService.createHistoricJobLogQuery().jobId(serviceTask2JobId).successful();

    assertEquals(1, serviceTask2Query.count());
    assertEquals(1, serviceTask2CreatedQuery.count());
    assertEquals(0, serviceTask2DeletedQuery.count());
    assertEquals(0, serviceTask2SuccessfulQuery.count());

    // when
    managementService.executeJob(serviceTask1JobId);

    // then
    assertEquals(4, query.count());

    // serviceTask1
    assertEquals(2, serviceTask1Query.count());
    assertEquals(1, serviceTask1CreatedQuery.count());
    assertEquals(0, serviceTask1DeletedQuery.count());
    assertEquals(1, serviceTask1SuccessfulQuery.count());

    HistoricJobLog serviceTask1CreatedJobLogEntry = serviceTask1CreatedQuery.singleResult();
    assertEquals(3, serviceTask1CreatedJobLogEntry.getJobRetries());

    HistoricJobLog serviceTask1SuccessfulJobLogEntry = serviceTask1SuccessfulQuery.singleResult();
    assertEquals(3, serviceTask1SuccessfulJobLogEntry.getJobRetries());

    // serviceTask2
    assertEquals(2, serviceTask2Query.count());
    assertEquals(1, serviceTask2CreatedQuery.count());
    assertEquals(1, serviceTask2DeletedQuery.count());
    assertEquals(0, serviceTask2SuccessfulQuery.count());

    HistoricJobLog serviceTask2CreatedJobLogEntry = serviceTask2CreatedQuery.singleResult();
    assertEquals(3, serviceTask2CreatedJobLogEntry.getJobRetries());

    HistoricJobLog serviceTask2DeletedJobLogEntry = serviceTask2DeletedQuery.singleResult();
    assertEquals(3, serviceTask2DeletedJobLogEntry.getJobRetries());

    // there should be one task after the boundary event
    assertEquals(1, taskService.createTaskQuery().count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testDeletedJob() {
    // given
    runtimeService.startProcessInstanceByKey("process");

    String jobId = managementService.createJobQuery().singleResult().getId();

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().jobId(jobId);
    HistoricJobLogQuery createdQuery = historyService.createHistoricJobLogQuery().jobId(jobId).created();
    HistoricJobLogQuery deletedQuery = historyService.createHistoricJobLogQuery().jobId(jobId).deleted();

    // there exists one historic job log entry
    assertEquals(1, query.count());
    assertEquals(1, createdQuery.count());
    assertEquals(0, deletedQuery.count());

    // when
    managementService.deleteJob(jobId);

    // then
    assertEquals(2, query.count());
    assertEquals(1, createdQuery.count());
    assertEquals(1, deletedQuery.count());

    HistoricJobLog createdJobLogEntry = createdQuery.singleResult();
    assertEquals(3, createdJobLogEntry.getJobRetries());

    HistoricJobLog deletedJobLogEntry = deletedQuery.singleResult();
    assertEquals(3, deletedJobLogEntry.getJobRetries());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testDeletedProcessInstance() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    String jobId = managementService.createJobQuery().singleResult().getId();

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().jobId(jobId);
    HistoricJobLogQuery createdQuery = historyService.createHistoricJobLogQuery().jobId(jobId).created();
    HistoricJobLogQuery deletedQuery = historyService.createHistoricJobLogQuery().jobId(jobId).deleted();

    // there exists one historic job log entry
    assertEquals(1, query.count());
    assertEquals(1, createdQuery.count());
    assertEquals(0, deletedQuery.count());

    // when
    runtimeService.deleteProcessInstance(processInstanceId, null);

    // then
    assertEquals(2, query.count());
    assertEquals(1, createdQuery.count());
    assertEquals(1, deletedQuery.count());

    HistoricJobLog createdJobLogEntry = createdQuery.singleResult();
    assertEquals(3, createdJobLogEntry.getJobRetries());

    HistoricJobLog deletedJobLogEntry = deletedQuery.singleResult();
    assertEquals(3, deletedJobLogEntry.getJobRetries());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testExceptionStacktrace() {
    // given
    runtimeService.startProcessInstanceByKey("process");

    String jobId = managementService.createJobQuery().singleResult().getId();

    // when
    try {
      managementService.executeJob(jobId);
      fail();
    } catch (Exception e) {
      // expected
    }

    // then
    String failedHistoricJobLogId = historyService
        .createHistoricJobLogQuery()
        .failed()
        .singleResult()
        .getId();

    String stacktrace = historyService.getHistoricJobLogExceptionStacktrace(failedHistoricJobLogId);
    assertNotNull(stacktrace);
    assertTextPresent(FailingDelegate.EXCEPTION_MESSAGE, stacktrace);
  }

  public void testgetJobExceptionStacktraceUnexistingJobId() {
    try {
      historyService.getHistoricJobLogExceptionStacktrace("unexistingjob");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      assertTextPresent("No historic job log found with id unexistingjob", re.getMessage());
    }
  }

  public void testgetJobExceptionStacktraceNullJobId() {
    try {
      historyService.getHistoricJobLogExceptionStacktrace(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      assertTextPresent("historicJobLogId is null", re.getMessage());
    }
  }

  @Deployment
  public void testDifferentExceptions() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    String jobId = managementService.createJobQuery().singleResult().getId();

    // when (1)
    try {
      managementService.executeJob(jobId);
      fail();
    } catch (Exception e) {
      // expected
    }

    // then (1)
    HistoricJobLog serviceTask1FailedHistoricJobLog = historyService
        .createHistoricJobLogQuery()
        .failed()
        .singleResult();

    String serviceTask1FailedHistoricJobLogId = serviceTask1FailedHistoricJobLog.getId();

    assertEquals(FirstFailingDelegate.FIRST_EXCEPTION_MESSAGE, serviceTask1FailedHistoricJobLog.getJobExceptionMessage());

    String serviceTask1Stacktrace = historyService.getHistoricJobLogExceptionStacktrace(serviceTask1FailedHistoricJobLogId);
    assertNotNull(serviceTask1Stacktrace);
    assertTextPresent(FirstFailingDelegate.FIRST_EXCEPTION_MESSAGE, serviceTask1Stacktrace);
    assertTextPresent(FirstFailingDelegate.class.getName(), serviceTask1Stacktrace);

    // when (2)
    runtimeService.setVariable(processInstanceId, "firstFail", false);
    try {
      managementService.executeJob(jobId);
      fail();
    } catch (Exception e) {
      // expected
    }

    // then (2)
    HistoricJobLog serviceTask2FailedHistoricJobLog = historyService
        .createHistoricJobLogQuery()
        .failed()
        .orderByJobRetries()
        .desc()
        .list()
        .get(1);

    String serviceTask2FailedHistoricJobLogId = serviceTask2FailedHistoricJobLog.getId();

    assertEquals(SecondFailingDelegate.SECOND_EXCEPTION_MESSAGE, serviceTask2FailedHistoricJobLog.getJobExceptionMessage());

    String serviceTask2Stacktrace = historyService.getHistoricJobLogExceptionStacktrace(serviceTask2FailedHistoricJobLogId);
    assertNotNull(serviceTask2Stacktrace);
    assertTextPresent(SecondFailingDelegate.SECOND_EXCEPTION_MESSAGE, serviceTask2Stacktrace);
    assertTextPresent(SecondFailingDelegate.class.getName(), serviceTask2Stacktrace);

    assertFalse(serviceTask1Stacktrace.equals(serviceTask2Stacktrace));
  }

  @Deployment
  public void testThrowExceptionWithoutMessage() {
    // given
    runtimeService.startProcessInstanceByKey("process").getId();

    String jobId = managementService.createJobQuery().singleResult().getId();

    // when
    try {
      managementService.executeJob(jobId);
      fail();
    } catch (Exception e) {
      // expected
    }

    // then
    HistoricJobLog failedHistoricJobLog = historyService
        .createHistoricJobLogQuery()
        .failed()
        .singleResult();

    String failedHistoricJobLogId = failedHistoricJobLog.getId();

    assertNull(failedHistoricJobLog.getJobExceptionMessage());

    String stacktrace = historyService.getHistoricJobLogExceptionStacktrace(failedHistoricJobLogId);
    assertNotNull(stacktrace);
    assertTextPresent(ThrowExceptionWithoutMessageDelegate.class.getName(), stacktrace);
  }

}
