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

import java.math.BigInteger;
import java.util.Date;
import java.util.Random;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.history.HistoricJobLogQuery;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.AsyncContinuationJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.MessageJobDeclaration;
import org.camunda.bpm.engine.impl.jobexecutor.ProcessEventJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerCatchIntermediateEventJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerExecuteNestedActivityJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerStartEventSubprocessJobHandler;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricJobLogEventEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.ExceptionUtil;
import org.camunda.bpm.engine.impl.util.StringUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.runtime.FailingDelegate;
import org.camunda.bpm.engine.variable.Variables;

/**
 * @author Roman Smirnov
 *
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoricJobLogTest extends PluggableProcessEngineTestCase {

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testCreateHistoricJobLogProperties() {
    runtimeService.startProcessInstanceByKey("process");

    Job job = managementService
        .createJobQuery()
        .singleResult();

    HistoricJobLog historicJob = historyService
        .createHistoricJobLogQuery()
        .creationLog()
        .singleResult();
    assertNotNull(historicJob);

    assertNotNull(historicJob.getTimestamp());

    assertNull(historicJob.getJobExceptionMessage());

    assertEquals(job.getId(), historicJob.getJobId());
    assertEquals(job.getJobDefinitionId(), historicJob.getJobDefinitionId());
    assertEquals("serviceTask", historicJob.getActivityId());
    assertEquals(AsyncContinuationJobHandler.TYPE, historicJob.getJobDefinitionType());
    assertEquals(MessageJobDeclaration.ASYNC_BEFORE, historicJob.getJobDefinitionConfiguration());
    assertEquals(job.getDuedate(), historicJob.getJobDueDate());
    assertEquals(job.getRetries(), historicJob.getJobRetries());
    assertEquals(job.getExecutionId(), historicJob.getExecutionId());
    assertEquals(job.getProcessInstanceId(), historicJob.getProcessInstanceId());
    assertEquals(job.getProcessDefinitionId(), historicJob.getProcessDefinitionId());
    assertEquals(job.getProcessDefinitionKey(), historicJob.getProcessDefinitionKey());
    assertEquals(job.getDeploymentId(), historicJob.getDeploymentId());
    assertEquals(job.getPriority(), historicJob.getJobPriority());

    assertTrue(historicJob.isCreationLog());
    assertFalse(historicJob.isFailureLog());
    assertFalse(historicJob.isSuccessLog());
    assertFalse(historicJob.isDeletionLog());
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
        .failureLog()
        .singleResult();
    assertNotNull(historicJob);

    assertNotNull(historicJob.getTimestamp());

    assertEquals(job.getId(), historicJob.getJobId());
    assertEquals(job.getJobDefinitionId(), historicJob.getJobDefinitionId());
    assertEquals("serviceTask", historicJob.getActivityId());
    assertEquals(AsyncContinuationJobHandler.TYPE, historicJob.getJobDefinitionType());
    assertEquals(MessageJobDeclaration.ASYNC_BEFORE, historicJob.getJobDefinitionConfiguration());
    assertEquals(job.getDuedate(), historicJob.getJobDueDate());
    assertEquals(3, historicJob.getJobRetries());
    assertEquals(job.getExecutionId(), historicJob.getExecutionId());
    assertEquals(job.getProcessInstanceId(), historicJob.getProcessInstanceId());
    assertEquals(job.getProcessDefinitionId(), historicJob.getProcessDefinitionId());
    assertEquals(job.getProcessDefinitionKey(), historicJob.getProcessDefinitionKey());
    assertEquals(job.getDeploymentId(), historicJob.getDeploymentId());
    assertEquals(FailingDelegate.EXCEPTION_MESSAGE, historicJob.getJobExceptionMessage());
    assertEquals(job.getPriority(), historicJob.getJobPriority());

    assertFalse(historicJob.isCreationLog());
    assertTrue(historicJob.isFailureLog());
    assertFalse(historicJob.isSuccessLog());
    assertFalse(historicJob.isDeletionLog());
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
        .successLog()
        .singleResult();
    assertNotNull(historicJob);

    assertNotNull(historicJob.getTimestamp());

    assertNull(historicJob.getJobExceptionMessage());

    assertEquals(job.getId(), historicJob.getJobId());
    assertEquals(job.getJobDefinitionId(), historicJob.getJobDefinitionId());
    assertEquals("serviceTask", historicJob.getActivityId());
    assertEquals(AsyncContinuationJobHandler.TYPE, historicJob.getJobDefinitionType());
    assertEquals(MessageJobDeclaration.ASYNC_BEFORE, historicJob.getJobDefinitionConfiguration());
    assertEquals(job.getDuedate(), historicJob.getJobDueDate());
    assertEquals(job.getRetries(), historicJob.getJobRetries());
    assertEquals(job.getExecutionId(), historicJob.getExecutionId());
    assertEquals(job.getProcessInstanceId(), historicJob.getProcessInstanceId());
    assertEquals(job.getProcessDefinitionId(), historicJob.getProcessDefinitionId());
    assertEquals(job.getProcessDefinitionKey(), historicJob.getProcessDefinitionKey());
    assertEquals(job.getDeploymentId(), historicJob.getDeploymentId());
    assertEquals(job.getPriority(), historicJob.getJobPriority());

    assertFalse(historicJob.isCreationLog());
    assertFalse(historicJob.isFailureLog());
    assertTrue(historicJob.isSuccessLog());
    assertFalse(historicJob.isDeletionLog());
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
        .deletionLog()
        .singleResult();
    assertNotNull(historicJob);

    assertNotNull(historicJob.getTimestamp());

    assertNull(historicJob.getJobExceptionMessage());

    assertEquals(job.getId(), historicJob.getJobId());
    assertEquals(job.getJobDefinitionId(), historicJob.getJobDefinitionId());
    assertEquals("serviceTask", historicJob.getActivityId());
    assertEquals(AsyncContinuationJobHandler.TYPE, historicJob.getJobDefinitionType());
    assertEquals(MessageJobDeclaration.ASYNC_BEFORE, historicJob.getJobDefinitionConfiguration());
    assertEquals(job.getDuedate(), historicJob.getJobDueDate());
    assertEquals(job.getRetries(), historicJob.getJobRetries());
    assertEquals(job.getExecutionId(), historicJob.getExecutionId());
    assertEquals(job.getProcessInstanceId(), historicJob.getProcessInstanceId());
    assertEquals(job.getProcessDefinitionId(), historicJob.getProcessDefinitionId());
    assertEquals(job.getProcessDefinitionKey(), historicJob.getProcessDefinitionKey());
    assertEquals(job.getDeploymentId(), historicJob.getDeploymentId());
    assertEquals(job.getPriority(), historicJob.getJobPriority());

    assertFalse(historicJob.isCreationLog());
    assertFalse(historicJob.isFailureLog());
    assertFalse(historicJob.isSuccessLog());
    assertTrue(historicJob.isDeletionLog());
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
    assertEquals(AsyncContinuationJobHandler.TYPE, historicJob.getJobDefinitionType());
    assertEquals(MessageJobDeclaration.ASYNC_BEFORE, historicJob.getJobDefinitionConfiguration());
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
    assertEquals(AsyncContinuationJobHandler.TYPE, historicJob.getJobDefinitionType());
    assertEquals(MessageJobDeclaration.ASYNC_AFTER, historicJob.getJobDefinitionConfiguration());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuationWithLongId.bpmn20.xml"})
  public void testSuccessfulHistoricJobLogEntryStoredForLongActivityId() {
    runtimeService.startProcessInstanceByKey("process", Variables.createVariables().putValue("fail", false));

    Job job = managementService
        .createJobQuery()
        .singleResult();

    managementService.executeJob(job.getId());

    HistoricJobLog historicJob = historyService
        .createHistoricJobLogQuery()
        .successLog()
        .singleResult();
    assertNotNull(historicJob);
    assertEquals("serviceTaskIdIsReallyLongAndItShouldBeMoreThan64CharsSoItWill" +
        "BlowAnyActivityIdColumnWhereSizeIs64OrLessSoWeAlignItTo255LikeEverywhereElse", historicJob.getActivityId());
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
    assertEquals(TimerStartEventJobHandler.TYPE, historicJob.getJobDefinitionType());
    assertEquals("CYCLE: 0 0/5 * * * ?", historicJob.getJobDefinitionConfiguration());
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
    assertEquals(TimerStartEventSubprocessJobHandler.TYPE, historicJob.getJobDefinitionType());
    assertEquals("DURATION: PT1M", historicJob.getJobDefinitionConfiguration());
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
    assertEquals(TimerCatchIntermediateEventJobHandler.TYPE, historicJob.getJobDefinitionType());
    assertEquals("DURATION: PT1M", historicJob.getJobDefinitionConfiguration());
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
    assertEquals(TimerExecuteNestedActivityJobHandler.TYPE, historicJob.getJobDefinitionType());
    assertEquals("DURATION: PT5M", historicJob.getJobDefinitionConfiguration());
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
    assertEquals(ProcessEventJobHandler.TYPE, historicJob.getJobDefinitionType());
    assertNull(historicJob.getJobDefinitionConfiguration());
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
        .creationLog()
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
        .failureLog()
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
        .successLog()
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
    HistoricJobLogQuery createdQuery = historyService.createHistoricJobLogQuery().jobId(jobId).creationLog();
    HistoricJobLogQuery failedQuery = historyService.createHistoricJobLogQuery().jobId(jobId).failureLog().orderByJobRetries().desc();

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
    assertEquals(3, failedJobLogEntry.getJobRetries());

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
    assertEquals(3, failedJobLogEntry.getJobRetries());

    failedJobLogEntry = failedQuery.list().get(1);
    assertEquals(2, failedJobLogEntry.getJobRetries());

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
    assertEquals(3, failedJobLogEntry.getJobRetries());

    failedJobLogEntry = failedQuery.list().get(1);
    assertEquals(2, failedJobLogEntry.getJobRetries());

    failedJobLogEntry = failedQuery.list().get(2);
    assertEquals(1, failedJobLogEntry.getJobRetries());

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
    assertEquals(3, failedJobLogEntry.getJobRetries());

    failedJobLogEntry = failedQuery.list().get(1);
    assertEquals(2, failedJobLogEntry.getJobRetries());

    failedJobLogEntry = failedQuery.list().get(2);
    assertEquals(1, failedJobLogEntry.getJobRetries());

    failedJobLogEntry = failedQuery.list().get(3);
    assertEquals(0, failedJobLogEntry.getJobRetries());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testFailedJobEventsExecutedByJobExecutor() {
    // given
    runtimeService.startProcessInstanceByKey("process");

    String jobId = managementService.createJobQuery().singleResult().getId();

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().jobId(jobId);
    HistoricJobLogQuery createdQuery = historyService.createHistoricJobLogQuery().jobId(jobId).creationLog();
    HistoricJobLogQuery failedQuery = historyService.createHistoricJobLogQuery().jobId(jobId).failureLog().orderByJobRetries().desc();

    // there exists one historic job log entry
    assertEquals(1, query.count());
    assertEquals(1, createdQuery.count());
    assertEquals(0, failedQuery.count());

    // when (1)
    executeAvailableJobs();

    // then (1)
    assertEquals(4, query.count());
    assertEquals(1, createdQuery.count());
    assertEquals(3, failedQuery.count());

    HistoricJobLog createdJobLogEntry = createdQuery.singleResult();
    assertEquals(3, createdJobLogEntry.getJobRetries());

    HistoricJobLog failedJobLogEntry = failedQuery.list().get(0);
    assertEquals(3, failedJobLogEntry.getJobRetries());

    failedJobLogEntry = failedQuery.list().get(1);
    assertEquals(2, failedJobLogEntry.getJobRetries());

    failedJobLogEntry = failedQuery.list().get(2);
    assertEquals(1, failedJobLogEntry.getJobRetries());

    // when (2)
    try {
      managementService.executeJob(jobId);
      fail();
    } catch (Exception e) {
      // expected
    }

    // then (2)
    assertEquals(5, query.count());
    assertEquals(1, createdQuery.count());
    assertEquals(4, failedQuery.count());

    createdJobLogEntry = createdQuery.singleResult();
    assertEquals(3, createdJobLogEntry.getJobRetries());

    failedJobLogEntry = failedQuery.list().get(0);
    assertEquals(3, failedJobLogEntry.getJobRetries());

    failedJobLogEntry = failedQuery.list().get(1);
    assertEquals(2, failedJobLogEntry.getJobRetries());

    failedJobLogEntry = failedQuery.list().get(2);
    assertEquals(1, failedJobLogEntry.getJobRetries());

    failedJobLogEntry = failedQuery.list().get(3);
    assertEquals(0, failedJobLogEntry.getJobRetries());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testSuccessfulJobEvent() {
    // given
    runtimeService.startProcessInstanceByKey("process", Variables.createVariables().putValue("fail", false));

    String jobId = managementService.createJobQuery().singleResult().getId();

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().jobId(jobId);
    HistoricJobLogQuery createdQuery = historyService.createHistoricJobLogQuery().jobId(jobId).creationLog();
    HistoricJobLogQuery succeededQuery = historyService.createHistoricJobLogQuery().jobId(jobId).successLog();

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
    HistoricJobLogQuery createdQuery = historyService.createHistoricJobLogQuery().jobId(jobId).creationLog();
    HistoricJobLogQuery succeededQuery = historyService.createHistoricJobLogQuery().jobId(jobId).successLog();

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
    HistoricJobLogQuery createdQuery = historyService.createHistoricJobLogQuery().jobId(jobId).creationLog();
    HistoricJobLogQuery failedQuery = historyService.createHistoricJobLogQuery().jobId(jobId).failureLog().orderByJobRetries().desc();
    HistoricJobLogQuery succeededQuery = historyService.createHistoricJobLogQuery().jobId(jobId).successLog();

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
    assertEquals(3, failedJobLogEntry.getJobRetries());

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
    assertEquals(3, failedJobLogEntry.getJobRetries());

    failedJobLogEntry = failedQuery.list().get(1);
    assertEquals(2, failedJobLogEntry.getJobRetries());

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
    assertEquals(3, failedJobLogEntry.getJobRetries());

    failedJobLogEntry = failedQuery.list().get(1);
    assertEquals(2, failedJobLogEntry.getJobRetries());

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
    HistoricJobLogQuery serviceTask1CreatedQuery = historyService.createHistoricJobLogQuery().jobId(serviceTask1JobId).creationLog();
    HistoricJobLogQuery serviceTask1DeletedQuery = historyService.createHistoricJobLogQuery().jobId(serviceTask1JobId).deletionLog();
    HistoricJobLogQuery serviceTask1SuccessfulQuery = historyService.createHistoricJobLogQuery().jobId(serviceTask1JobId).successLog();

    assertEquals(1, serviceTask1Query.count());
    assertEquals(1, serviceTask1CreatedQuery.count());
    assertEquals(0, serviceTask1DeletedQuery.count());
    assertEquals(0, serviceTask1SuccessfulQuery.count());

    // serviceTask2
    String serviceTask2JobId = managementService.createJobQuery().activityId("serviceTask2").singleResult().getId();

    HistoricJobLogQuery serviceTask2Query = historyService.createHistoricJobLogQuery().jobId(serviceTask2JobId);
    HistoricJobLogQuery serviceTask2CreatedQuery = historyService.createHistoricJobLogQuery().jobId(serviceTask2JobId).creationLog();
    HistoricJobLogQuery serviceTask2DeletedQuery = historyService.createHistoricJobLogQuery().jobId(serviceTask2JobId).deletionLog();
    HistoricJobLogQuery serviceTask2SuccessfulQuery = historyService.createHistoricJobLogQuery().jobId(serviceTask2JobId).successLog();

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
    HistoricJobLogQuery serviceTask1CreatedQuery = historyService.createHistoricJobLogQuery().jobId(serviceTask1JobId).creationLog();
    HistoricJobLogQuery serviceTask1DeletedQuery = historyService.createHistoricJobLogQuery().jobId(serviceTask1JobId).deletionLog();
    HistoricJobLogQuery serviceTask1SuccessfulQuery = historyService.createHistoricJobLogQuery().jobId(serviceTask1JobId).successLog();

    assertEquals(1, serviceTask1Query.count());
    assertEquals(1, serviceTask1CreatedQuery.count());
    assertEquals(0, serviceTask1DeletedQuery.count());
    assertEquals(0, serviceTask1SuccessfulQuery.count());

    // serviceTask2
    String serviceTask2JobId = managementService.createJobQuery().activityId("serviceTask2").singleResult().getId();

    HistoricJobLogQuery serviceTask2Query = historyService.createHistoricJobLogQuery().jobId(serviceTask2JobId);
    HistoricJobLogQuery serviceTask2CreatedQuery = historyService.createHistoricJobLogQuery().jobId(serviceTask2JobId).creationLog();
    HistoricJobLogQuery serviceTask2DeletedQuery = historyService.createHistoricJobLogQuery().jobId(serviceTask2JobId).deletionLog();
    HistoricJobLogQuery serviceTask2SuccessfulQuery = historyService.createHistoricJobLogQuery().jobId(serviceTask2JobId).successLog();

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
    HistoricJobLogQuery createdQuery = historyService.createHistoricJobLogQuery().jobId(jobId).creationLog();
    HistoricJobLogQuery deletedQuery = historyService.createHistoricJobLogQuery().jobId(jobId).deletionLog();

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
    HistoricJobLogQuery createdQuery = historyService.createHistoricJobLogQuery().jobId(jobId).creationLog();
    HistoricJobLogQuery deletedQuery = historyService.createHistoricJobLogQuery().jobId(jobId).deletionLog();

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
        .failureLog()
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
        .failureLog()
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
        .failureLog()
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
        .failureLog()
        .singleResult();

    String failedHistoricJobLogId = failedHistoricJobLog.getId();

    assertNull(failedHistoricJobLog.getJobExceptionMessage());

    String stacktrace = historyService.getHistoricJobLogExceptionStacktrace(failedHistoricJobLogId);
    assertNotNull(stacktrace);
    assertTextPresent(ThrowExceptionWithoutMessageDelegate.class.getName(), stacktrace);
  }

  @Deployment
  public void testThrowExceptionMessageTruncation() {
    // given
    String exceptionMessage = randomString(10000);
    ThrowExceptionWithOverlongMessageDelegate delegate =
        new ThrowExceptionWithOverlongMessageDelegate(exceptionMessage);

    runtimeService.startProcessInstanceByKey("process", Variables.createVariables().putValue("delegate", delegate));
    Job job = managementService.createJobQuery().singleResult();

    // when
    try {
      managementService.executeJob(job.getId());
      fail();
    } catch (Exception e) {
      // expected
    }

    // then
    HistoricJobLog failedHistoricJobLog = historyService
        .createHistoricJobLogQuery()
        .failureLog()
        .singleResult();

    assertNotNull(failedHistoricJobLog);
    assertEquals(exceptionMessage.substring(0, JobEntity.MAX_EXCEPTION_MESSAGE_LENGTH),
        failedHistoricJobLog.getJobExceptionMessage());
  }

  /**
   * returns a random of the given size using characters [0-1]
   */
  protected static String randomString(int numCharacters) {
    return new BigInteger(numCharacters, new Random()).toString(2);
  }


  public void testDeleteByteArray() {
    final String processDefinitionId = "myProcessDefition";

    processEngineConfiguration.getCommandExecutorTxRequiresNew().execute(new Command<Void>() {

      public Void execute(CommandContext commandContext) {

        for (int i = 0; i < 1234; i++) {
          HistoricJobLogEventEntity log = new HistoricJobLogEventEntity();
          log.setJobId(String.valueOf(i));
          log.setTimestamp(new Date());
          log.setJobDefinitionType(MessageEntity.TYPE);
          log.setProcessDefinitionId(processDefinitionId);


          byte[] aByteValue = StringUtil.toByteArray("abc");
          ByteArrayEntity byteArray = ExceptionUtil.createJobExceptionByteArray(aByteValue);
          log.setExceptionByteArrayId(byteArray.getId());

          commandContext
            .getHistoricJobLogManager()
            .insert(log);
        }

        return null;
      }

    });

    assertEquals(1234, historyService.createHistoricJobLogQuery().count());

    processEngineConfiguration.getCommandExecutorTxRequiresNew().execute(new Command<Void>() {

      public Void execute(CommandContext commandContext) {
        commandContext.getHistoricJobLogManager().deleteHistoricJobLogsByProcessDefinitionId(processDefinitionId);
        return null;
      }

    });

    assertEquals(0, historyService.createHistoricJobLogQuery().count());
  }

}
