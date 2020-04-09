/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.math.BigInteger;
import java.util.Date;
import java.util.Random;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.history.HistoricJobLogQuery;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.jobexecutor.AsyncContinuationJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.MessageJobDeclaration;
import org.camunda.bpm.engine.impl.jobexecutor.ProcessEventJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerCatchIntermediateEventJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerExecuteNestedActivityJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerStartEventSubprocessJobHandler;
import org.camunda.bpm.engine.impl.persistence.deploy.cache.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricJobLogEventEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.ExceptionUtil;
import org.camunda.bpm.engine.impl.util.StringUtil;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.repository.ResourceTypes;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.runtime.FailingDelegate;
import org.camunda.bpm.engine.test.util.ClockTestUtil;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Roman Smirnov
 *
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoricJobLogTest {

  protected static final String CUSTOM_HOSTNAME = "TEST_HOST";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected RuntimeService runtimeService;
  protected TaskService taskService;
  protected ManagementService managementService;
  protected HistoryService historyService;

  private boolean defaultEnsureJobDueDateSet;
  protected String defaultHostname;

  @Before
  public void init() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    runtimeService = engineRule.getRuntimeService();
    taskService = engineRule.getTaskService();
    managementService = engineRule.getManagementService();
    historyService = engineRule.getHistoryService();

    defaultEnsureJobDueDateSet = processEngineConfiguration.isEnsureJobDueDateNotNull();
    defaultHostname = processEngineConfiguration.getHostname();
    processEngineConfiguration.setHostname(CUSTOM_HOSTNAME);
  }

  @After
  public void tearDown() {
    processEngineConfiguration.setEnsureJobDueDateNotNull(defaultEnsureJobDueDateSet);
    processEngineConfiguration.setHostname(defaultHostname);
    ClockUtil.reset();
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  @Test
  public void testCreateHistoricJobLogProperties() {
    runtimeService.startProcessInstanceByKey("process");

    Job job = managementService
        .createJobQuery()
        .singleResult();

    HistoricJobLog historicJob = historyService
        .createHistoricJobLogQuery()
        .creationLog()
        .singleResult();
    assertThat(historicJob).isNotNull();

    assertThat(historicJob.getTimestamp()).isNotNull();

    assertThat(historicJob.getJobExceptionMessage()).isNull();

    assertThat(historicJob.getJobId()).isEqualTo(job.getId());
    assertThat(historicJob.getJobDefinitionId()).isEqualTo(job.getJobDefinitionId());
    assertThat(historicJob.getActivityId()).isEqualTo("serviceTask");
    assertThat(historicJob.getJobDefinitionType()).isEqualTo(AsyncContinuationJobHandler.TYPE);
    assertThat(historicJob.getJobDefinitionConfiguration()).isEqualTo(MessageJobDeclaration.ASYNC_BEFORE);
    assertThat(historicJob.getJobDueDate()).isEqualTo(job.getDuedate());
    assertThat(historicJob.getJobRetries()).isEqualTo(job.getRetries());
    assertThat(historicJob.getExecutionId()).isEqualTo(job.getExecutionId());
    assertThat(historicJob.getProcessInstanceId()).isEqualTo(job.getProcessInstanceId());
    assertThat(historicJob.getProcessDefinitionId()).isEqualTo(job.getProcessDefinitionId());
    assertThat(historicJob.getProcessDefinitionKey()).isEqualTo(job.getProcessDefinitionKey());
    assertThat(historicJob.getDeploymentId()).isEqualTo(job.getDeploymentId());
    assertThat(historicJob.getJobPriority()).isEqualTo(job.getPriority());
    assertThat(historicJob.getHostname()).containsIgnoringCase(CUSTOM_HOSTNAME);

    assertThat(historicJob.isCreationLog()).isTrue();
    assertThat(historicJob.isFailureLog()).isFalse();
    assertThat(historicJob.isSuccessLog()).isFalse();
    assertThat(historicJob.isDeletionLog()).isFalse();
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  @Test
  public void testFailedHistoricJobLogProperties() {
    runtimeService.startProcessInstanceByKey("process");

    JobEntity job = (JobEntity) managementService
        .createJobQuery()
        .singleResult();

    try {
      managementService.executeJob(job.getId());
      fail("exception expected");
    } catch (Exception e) {
      // expected
    }

    job = (JobEntity) managementService.createJobQuery().jobId(job.getId()).singleResult();

    HistoricJobLog historicJob = historyService
        .createHistoricJobLogQuery()
        .failureLog()
        .singleResult();
    assertThat(historicJob).isNotNull();

    assertThat(historicJob.getTimestamp()).isNotNull();

    assertThat(historicJob.getJobId()).isEqualTo(job.getId());
    assertThat(historicJob.getJobDefinitionId()).isEqualTo(job.getJobDefinitionId());
    assertThat(historicJob.getActivityId()).isEqualTo("serviceTask");
    assertThat(historicJob.getJobDefinitionType()).isEqualTo(AsyncContinuationJobHandler.TYPE);
    assertThat(historicJob.getJobDefinitionConfiguration()).isEqualTo(MessageJobDeclaration.ASYNC_BEFORE);
    assertThat(historicJob.getJobDueDate()).isEqualTo(job.getDuedate());
    assertThat(historicJob.getJobRetries()).isEqualTo(3);
    assertThat(historicJob.getExecutionId()).isEqualTo(job.getExecutionId());
    assertThat(historicJob.getProcessInstanceId()).isEqualTo(job.getProcessInstanceId());
    assertThat(historicJob.getProcessDefinitionId()).isEqualTo(job.getProcessDefinitionId());
    assertThat(historicJob.getProcessDefinitionKey()).isEqualTo(job.getProcessDefinitionKey());
    assertThat(historicJob.getDeploymentId()).isEqualTo(job.getDeploymentId());
    assertThat(historicJob.getJobExceptionMessage()).isEqualTo(FailingDelegate.EXCEPTION_MESSAGE);
    assertThat(historicJob.getJobPriority()).isEqualTo(job.getPriority());
    assertThat(historicJob.getHostname()).containsIgnoringCase(CUSTOM_HOSTNAME);
    assertThat(historicJob.getFailedActivityId()).isNotNull().isEqualTo(job.getFailedActivityId());

    assertThat(historicJob.isCreationLog()).isFalse();
    assertThat(historicJob.isFailureLog()).isTrue();
    assertThat(historicJob.isSuccessLog()).isFalse();
    assertThat(historicJob.isDeletionLog()).isFalse();
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  @Test
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
    assertThat(historicJob).isNotNull();

    assertThat(historicJob.getTimestamp()).isNotNull();

    assertThat(historicJob.getJobExceptionMessage()).isNull();

    assertThat(historicJob.getJobId()).isEqualTo(job.getId());
    assertThat(historicJob.getJobDefinitionId()).isEqualTo(job.getJobDefinitionId());
    assertThat(historicJob.getActivityId()).isEqualTo("serviceTask");
    assertThat(historicJob.getJobDefinitionType()).isEqualTo(AsyncContinuationJobHandler.TYPE);
    assertThat(historicJob.getJobDefinitionConfiguration()).isEqualTo(MessageJobDeclaration.ASYNC_BEFORE);
    assertThat(historicJob.getJobDueDate()).isEqualTo(job.getDuedate());
    assertThat(historicJob.getJobRetries()).isEqualTo(job.getRetries());
    assertThat(historicJob.getExecutionId()).isEqualTo(job.getExecutionId());
    assertThat(historicJob.getProcessInstanceId()).isEqualTo(job.getProcessInstanceId());
    assertThat(historicJob.getProcessDefinitionId()).isEqualTo(job.getProcessDefinitionId());
    assertThat(historicJob.getProcessDefinitionKey()).isEqualTo(job.getProcessDefinitionKey());
    assertThat(historicJob.getDeploymentId()).isEqualTo(job.getDeploymentId());
    assertThat(historicJob.getJobPriority()).isEqualTo(job.getPriority());
    assertThat(historicJob.getHostname()).containsIgnoringCase(CUSTOM_HOSTNAME);

    assertThat(historicJob.isCreationLog()).isFalse();
    assertThat(historicJob.isFailureLog()).isFalse();
    assertThat(historicJob.isSuccessLog()).isTrue();
    assertThat(historicJob.isDeletionLog()).isFalse();
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  @Test
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
    assertThat(historicJob).isNotNull();

    assertThat(historicJob.getTimestamp()).isNotNull();

    assertThat(historicJob.getJobExceptionMessage()).isNull();

    assertThat(historicJob.getJobId()).isEqualTo(job.getId());
    assertThat(historicJob.getJobDefinitionId()).isEqualTo(job.getJobDefinitionId());
    assertThat(historicJob.getActivityId()).isEqualTo("serviceTask");
    assertThat(historicJob.getJobDefinitionType()).isEqualTo(AsyncContinuationJobHandler.TYPE);
    assertThat(historicJob.getJobDefinitionConfiguration()).isEqualTo(MessageJobDeclaration.ASYNC_BEFORE);
    assertThat(historicJob.getJobDueDate()).isEqualTo(job.getDuedate());
    assertThat(historicJob.getJobRetries()).isEqualTo(job.getRetries());
    assertThat(historicJob.getExecutionId()).isEqualTo(job.getExecutionId());
    assertThat(historicJob.getProcessInstanceId()).isEqualTo(job.getProcessInstanceId());
    assertThat(historicJob.getProcessDefinitionId()).isEqualTo(job.getProcessDefinitionId());
    assertThat(historicJob.getProcessDefinitionKey()).isEqualTo(job.getProcessDefinitionKey());
    assertThat(historicJob.getDeploymentId()).isEqualTo(job.getDeploymentId());
    assertThat(historicJob.getJobPriority()).isEqualTo(job.getPriority());
    assertThat(historicJob.getHostname()).containsIgnoringCase(CUSTOM_HOSTNAME);

    assertThat(historicJob.isCreationLog()).isFalse();
    assertThat(historicJob.isFailureLog()).isFalse();
    assertThat(historicJob.isSuccessLog()).isFalse();
    assertThat(historicJob.isDeletionLog()).isTrue();
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  @Test
  public void testAsyncBeforeJobHandlerType() {
    processEngineConfiguration.setEnsureJobDueDateNotNull(false);

    runtimeService.startProcessInstanceByKey("process");

    Job job = managementService
        .createJobQuery()
        .singleResult();

    HistoricJobLog historicJob = historyService
        .createHistoricJobLogQuery()
        .jobId(job.getId())
        .singleResult();

    assertThat(historicJob).isNotNull();

    assertThat(historicJob.getJobDueDate()).isNull();

    assertThat(historicJob.getJobDefinitionId()).isEqualTo(job.getJobDefinitionId());
    assertThat(historicJob.getActivityId()).isEqualTo("serviceTask");
    assertThat(historicJob.getJobDefinitionType()).isEqualTo(AsyncContinuationJobHandler.TYPE);
    assertThat(historicJob.getJobDefinitionConfiguration()).isEqualTo(MessageJobDeclaration.ASYNC_BEFORE);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  @Test
  public void testAsyncBeforeJobHandlerTypeDueDateSet() {
    processEngineConfiguration.setEnsureJobDueDateNotNull(true);
    Date testDate = ClockTestUtil.setClockToDateWithoutMilliseconds();

    runtimeService.startProcessInstanceByKey("process");

    Job job = managementService
      .createJobQuery()
      .singleResult();

    HistoricJobLog historicJob = historyService
      .createHistoricJobLogQuery()
      .jobId(job.getId())
      .singleResult();

    assertThat(historicJob).isNotNull();

    assertThat(historicJob.getJobDueDate()).isEqualTo(testDate);

    assertThat(historicJob.getJobDefinitionId()).isEqualTo(job.getJobDefinitionId());
    assertThat(historicJob.getActivityId()).isEqualTo("serviceTask");
    assertThat(historicJob.getJobDefinitionType()).isEqualTo(AsyncContinuationJobHandler.TYPE);
    assertThat(historicJob.getJobDefinitionConfiguration()).isEqualTo(MessageJobDeclaration.ASYNC_BEFORE);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  @Test
  public void testAsyncAfterJobHandlerType() {
    processEngineConfiguration.setEnsureJobDueDateNotNull(false);

    runtimeService.startProcessInstanceByKey("process", Variables.createVariables().putValue("fail", false));

    Job job = managementService
        .createJobQuery()
        .singleResult();

    managementService.executeJob(job.getId());

    Job anotherJob = managementService
        .createJobQuery()
        .singleResult();

    assertThat(job.getId().equals(anotherJob.getId())).isFalse();

    HistoricJobLog historicJob = historyService
        .createHistoricJobLogQuery()
        .jobId(anotherJob.getId())
        .singleResult();

    assertThat(historicJob).isNotNull();

    assertThat(historicJob.getJobDueDate()).isNull();

    assertThat(historicJob.getJobDefinitionId()).isEqualTo(anotherJob.getJobDefinitionId());
    assertThat(historicJob.getActivityId()).isEqualTo("serviceTask");
    assertThat(historicJob.getJobDefinitionType()).isEqualTo(AsyncContinuationJobHandler.TYPE);
    assertThat(historicJob.getJobDefinitionConfiguration()).isEqualTo(MessageJobDeclaration.ASYNC_AFTER);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  @Test
  public void testAsyncAfterJobHandlerTypeDueDateSet() {
    processEngineConfiguration.setEnsureJobDueDateNotNull(true);
    Date testDate = ClockTestUtil.setClockToDateWithoutMilliseconds();

    runtimeService.startProcessInstanceByKey("process", Variables.createVariables().putValue("fail", false));

    Job job = managementService
      .createJobQuery()
      .singleResult();

    managementService.executeJob(job.getId());

    Job anotherJob = managementService
      .createJobQuery()
      .singleResult();

    assertThat(job.getId().equals(anotherJob.getId())).isFalse();

    HistoricJobLog historicJob = historyService
      .createHistoricJobLogQuery()
      .jobId(anotherJob.getId())
      .singleResult();

    assertThat(historicJob).isNotNull();

    assertThat(historicJob.getJobDueDate()).isEqualTo(testDate);

    assertThat(historicJob.getJobDefinitionId()).isEqualTo(anotherJob.getJobDefinitionId());
    assertThat(historicJob.getActivityId()).isEqualTo("serviceTask");
    assertThat(historicJob.getJobDefinitionType()).isEqualTo(AsyncContinuationJobHandler.TYPE);
    assertThat(historicJob.getJobDefinitionConfiguration()).isEqualTo(MessageJobDeclaration.ASYNC_AFTER);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuationWithLongId.bpmn20.xml"})
  @Test
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
    assertThat(historicJob).isNotNull();
    assertThat(historicJob.getActivityId())
        .isEqualToIgnoringCase("serviceTaskIdIsReallyLongAndItShouldBeMoreThan64CharsSoItWill" +
        "BlowAnyActivityIdColumnWhereSizeIs64OrLessSoWeAlignItTo255LikeEverywhereElse");
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testStartTimerEvent.bpmn20.xml"})
  @Test
  public void testStartTimerEventJobHandlerType() {
    Job job = managementService
        .createJobQuery()
        .singleResult();

    HistoricJobLog historicJob = historyService
        .createHistoricJobLogQuery()
        .jobId(job.getId())
        .singleResult();

    assertThat(historicJob).isNotNull();

    assertThat(historicJob.getJobId()).isEqualTo(job.getId());
    assertThat(historicJob.getJobDefinitionId()).isEqualTo(job.getJobDefinitionId());
    assertThat(historicJob.getActivityId()).isEqualTo("theStart");
    assertThat(historicJob.getJobDefinitionType()).isEqualTo(TimerStartEventJobHandler.TYPE);
    assertThat(historicJob.getJobDefinitionConfiguration()).isEqualTo("CYCLE: 0 0/5 * * * ?");
    assertThat(historicJob.getJobDueDate()).isEqualTo(job.getDuedate());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testStartTimerEventInsideEventSubProcess.bpmn20.xml"})
  @Test
  public void testStartTimerEventInsideEventSubProcessJobHandlerType() {
    runtimeService.startProcessInstanceByKey("process");

    Job job = managementService
        .createJobQuery()
        .singleResult();

    HistoricJobLog historicJob = historyService
        .createHistoricJobLogQuery()
        .jobId(job.getId())
        .singleResult();

    assertThat(historicJob).isNotNull();

    assertThat(historicJob.getJobId()).isEqualTo(job.getId());
    assertThat(historicJob.getJobDefinitionId()).isEqualTo(job.getJobDefinitionId());
    assertThat(historicJob.getActivityId()).isEqualTo("subprocessStartEvent");
    assertThat(historicJob.getJobDefinitionType()).isEqualTo(TimerStartEventSubprocessJobHandler.TYPE);
    assertThat(historicJob.getJobDefinitionConfiguration()).isEqualTo("DURATION: PT1M");
    assertThat(historicJob.getJobDueDate()).isEqualTo(job.getDuedate());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testIntermediateTimerEvent.bpmn20.xml"})
  @Test
  public void testIntermediateTimerEventJobHandlerType() {
    runtimeService.startProcessInstanceByKey("process");

    Job job = managementService
        .createJobQuery()
        .singleResult();

    HistoricJobLog historicJob = historyService
        .createHistoricJobLogQuery()
        .jobId(job.getId())
        .singleResult();

    assertThat(historicJob).isNotNull();

    assertThat(historicJob.getJobId()).isEqualTo(job.getId());
    assertThat(historicJob.getJobDefinitionId()).isEqualTo(job.getJobDefinitionId());
    assertThat(historicJob.getActivityId()).isEqualTo("timer");
    assertThat(historicJob.getJobDefinitionType()).isEqualTo(TimerCatchIntermediateEventJobHandler.TYPE);
    assertThat(historicJob.getJobDefinitionConfiguration()).isEqualTo("DURATION: PT1M");
    assertThat(historicJob.getJobDueDate()).isEqualTo(job.getDuedate());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testBoundaryTimerEvent.bpmn20.xml"})
  @Test
  public void testBoundaryTimerEventJobHandlerType() {
    runtimeService.startProcessInstanceByKey("process");

    Job job = managementService
        .createJobQuery()
        .singleResult();

    HistoricJobLog historicJob = historyService
        .createHistoricJobLogQuery()
        .jobId(job.getId())
        .singleResult();

    assertThat(historicJob).isNotNull();

    assertThat(historicJob.getJobId()).isEqualTo(job.getId());
    assertThat(historicJob.getJobDefinitionId()).isEqualTo(job.getJobDefinitionId());
    assertThat(historicJob.getActivityId()).isEqualTo("timer");
    assertThat(historicJob.getJobDefinitionType()).isEqualTo(TimerExecuteNestedActivityJobHandler.TYPE);
    assertThat(historicJob.getJobDefinitionConfiguration()).isEqualTo("DURATION: PT5M");
    assertThat(historicJob.getJobDueDate()).isEqualTo(job.getDuedate());
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/history/HistoricJobLogTest.testCatchingSignalEvent.bpmn20.xml",
      "org/camunda/bpm/engine/test/history/HistoricJobLogTest.testThrowingSignalEventAsync.bpmn20.xml"
  })
  @Test
  public void testCatchingSignalEventJobHandlerType() {
    processEngineConfiguration.setEnsureJobDueDateNotNull(false);

    runtimeService.startProcessInstanceByKey("catchSignal");
    runtimeService.startProcessInstanceByKey("throwSignal");

    Job job = managementService
        .createJobQuery()
        .singleResult();

    HistoricJobLog historicJob = historyService
        .createHistoricJobLogQuery()
        .jobId(job.getId())
        .singleResult();

    assertThat(historicJob).isNotNull();

    assertThat(historicJob.getJobDueDate()).isNull();

    assertThat(historicJob.getJobId()).isEqualTo(job.getId());
    assertThat(historicJob.getJobDefinitionId()).isEqualTo(job.getJobDefinitionId());
    assertThat(historicJob.getActivityId()).isEqualTo("signalEvent");
    assertThat(historicJob.getJobDefinitionType()).isEqualTo(ProcessEventJobHandler.TYPE);
    assertThat(historicJob.getJobDefinitionConfiguration()).isNull();
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/history/HistoricJobLogTest.testCatchingSignalEvent.bpmn20.xml",
    "org/camunda/bpm/engine/test/history/HistoricJobLogTest.testThrowingSignalEventAsync.bpmn20.xml"
  })
  @Test
  public void testCatchingSignalEventJobHandlerTypeDueDateSet() {
    processEngineConfiguration.setEnsureJobDueDateNotNull(true);
    Date testDate = ClockTestUtil.setClockToDateWithoutMilliseconds();

    runtimeService.startProcessInstanceByKey("catchSignal");
    runtimeService.startProcessInstanceByKey("throwSignal");

    Job job = managementService
      .createJobQuery()
      .singleResult();

    HistoricJobLog historicJob = historyService
      .createHistoricJobLogQuery()
      .jobId(job.getId())
      .singleResult();

    assertThat(historicJob).isNotNull();

    assertThat(historicJob.getJobDueDate()).isEqualTo(testDate);

    assertThat(historicJob.getJobId()).isEqualTo(job.getId());
    assertThat(historicJob.getJobDefinitionId()).isEqualTo(job.getJobDefinitionId());
    assertThat(historicJob.getActivityId()).isEqualTo("signalEvent");
    assertThat(historicJob.getJobDefinitionType()).isEqualTo(ProcessEventJobHandler.TYPE);
    assertThat(historicJob.getJobDefinitionConfiguration()).isNull();
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/history/HistoricJobLogTest.testCatchingSignalEvent.bpmn20.xml",
      "org/camunda/bpm/engine/test/history/HistoricJobLogTest.testThrowingSignalEventAsync.bpmn20.xml"
  })
  @Test
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
    assertThat(historicJob).isNotNull();

    assertThat(historicJob.getActivityId()).isEqualTo("signalEvent");

    // when (2)
    try {
      managementService.executeJob(jobId);
      fail("exception expected");
    } catch (Exception e) {
      // expected
    }

    // then (2)
    historicJob = historyService
        .createHistoricJobLogQuery()
        .jobId(jobId)
        .failureLog()
        .singleResult();
    assertThat(historicJob).isNotNull();

    assertThat(historicJob.getActivityId()).isEqualTo("signalEvent");

    // when (3)
    runtimeService.setVariable(processInstanceId, "fail", false);
    managementService.executeJob(jobId);

    // then (3)

    historicJob = historyService
        .createHistoricJobLogQuery()
        .jobId(jobId)
        .successLog()
        .singleResult();
    assertThat(historicJob).isNotNull();

    assertThat(historicJob.getActivityId()).isEqualTo("signalEvent");
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  @Test
  public void testFailedJobEvents() {
    // given
    runtimeService.startProcessInstanceByKey("process");

    String jobId = managementService.createJobQuery().singleResult().getId();

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().jobId(jobId);
    HistoricJobLogQuery createdQuery = historyService.createHistoricJobLogQuery().jobId(jobId).creationLog();
    HistoricJobLogQuery failedQuery = historyService.createHistoricJobLogQuery().jobId(jobId).failureLog().orderByJobRetries().desc();

    // there exists one historic job log entry
    assertThat(query.count()).isEqualTo(1);
    assertThat(createdQuery.count()).isEqualTo(1);
    assertThat(failedQuery.count()).isEqualTo(0);

    // when (1)
    try {
      managementService.executeJob(jobId);
      fail("exception expected");
    } catch (Exception e) {
      // expected
    }

    // then (1)
    assertThat(query.count()).isEqualTo(2);
    assertThat(createdQuery.count()).isEqualTo(1);
    assertThat(failedQuery.count()).isEqualTo(1);

    HistoricJobLog createdJobLogEntry = createdQuery.singleResult();
    assertThat(createdJobLogEntry.getJobRetries()).isEqualTo(3);

    HistoricJobLog failedJobLogEntry = failedQuery.singleResult();
    assertThat(failedJobLogEntry.getJobRetries()).isEqualTo(3);

    // when (2)
    try {
      managementService.executeJob(jobId);
      fail("exception expected");
    } catch (Exception e) {
      // expected
    }

    // then (2)
    assertThat(query.count()).isEqualTo(3);
    assertThat(createdQuery.count()).isEqualTo(1);
    assertThat(failedQuery.count()).isEqualTo(2);

    createdJobLogEntry = createdQuery.singleResult();
    assertThat(createdJobLogEntry.getJobRetries()).isEqualTo(3);

    failedJobLogEntry = failedQuery.list().get(0);
    assertThat(failedJobLogEntry.getJobRetries()).isEqualTo(3);

    failedJobLogEntry = failedQuery.list().get(1);
    assertThat(failedJobLogEntry.getJobRetries()).isEqualTo(2);

    // when (3)
    try {
      managementService.executeJob(jobId);
      fail("exception expected");
    } catch (Exception e) {
      // expected
    }

    // then (3)
    assertThat(query.count()).isEqualTo(4);
    assertThat(createdQuery.count()).isEqualTo(1);
    assertThat(failedQuery.count()).isEqualTo(3);

    createdJobLogEntry = createdQuery.singleResult();
    assertThat(createdJobLogEntry.getJobRetries()).isEqualTo(3);

    failedJobLogEntry = failedQuery.list().get(0);
    assertThat(failedJobLogEntry.getJobRetries()).isEqualTo(3);

    failedJobLogEntry = failedQuery.list().get(1);
    assertThat(failedJobLogEntry.getJobRetries()).isEqualTo(2);

    failedJobLogEntry = failedQuery.list().get(2);
    assertThat(failedJobLogEntry.getJobRetries()).isEqualTo(1);

    // when (4)
    try {
      managementService.executeJob(jobId);
      fail("exception expected");
    } catch (Exception e) {
      // expected
    }

    // then (4)
    assertThat(query.count()).isEqualTo(5);
    assertThat(createdQuery.count()).isEqualTo(1);
    assertThat(failedQuery.count()).isEqualTo(4);

    createdJobLogEntry = createdQuery.singleResult();
    assertThat(createdJobLogEntry.getJobRetries()).isEqualTo(3);

    failedJobLogEntry = failedQuery.list().get(0);
    assertThat(failedJobLogEntry.getJobRetries()).isEqualTo(3);

    failedJobLogEntry = failedQuery.list().get(1);
    assertThat(failedJobLogEntry.getJobRetries()).isEqualTo(2);

    failedJobLogEntry = failedQuery.list().get(2);
    assertThat(failedJobLogEntry.getJobRetries()).isEqualTo(1);

    failedJobLogEntry = failedQuery.list().get(3);
    assertThat(failedJobLogEntry.getJobRetries()).isEqualTo(0);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  @Test
  public void testFailedJobEventsExecutedByJobExecutor() {
    // given
    runtimeService.startProcessInstanceByKey("process");

    String jobId = managementService.createJobQuery().singleResult().getId();

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().jobId(jobId);
    HistoricJobLogQuery createdQuery = historyService.createHistoricJobLogQuery().jobId(jobId).creationLog();
    HistoricJobLogQuery failedQuery = historyService.createHistoricJobLogQuery().jobId(jobId).failureLog().orderByJobRetries().desc();

    // there exists one historic job log entry
    assertThat(query.count()).isEqualTo(1);
    assertThat(createdQuery.count()).isEqualTo(1);
    assertThat(failedQuery.count()).isEqualTo(0);

    // when (1)
    testRule.executeAvailableJobs();

    // then (1)
    assertThat(query.count()).isEqualTo(4);
    assertThat(createdQuery.count()).isEqualTo(1);
    assertThat(failedQuery.count()).isEqualTo(3);

    HistoricJobLog createdJobLogEntry = createdQuery.singleResult();
    assertThat(createdJobLogEntry.getJobRetries()).isEqualTo(3);

    HistoricJobLog failedJobLogEntry = failedQuery.list().get(0);
    assertThat(failedJobLogEntry.getJobRetries()).isEqualTo(3);

    failedJobLogEntry = failedQuery.list().get(1);
    assertThat(failedJobLogEntry.getJobRetries()).isEqualTo(2);

    failedJobLogEntry = failedQuery.list().get(2);
    assertThat(failedJobLogEntry.getJobRetries()).isEqualTo(1);

    // when (2)
    try {
      managementService.executeJob(jobId);
      fail("exception expected");
    } catch (Exception e) {
      // expected
    }

    // then (2)
    assertThat(query.count()).isEqualTo(5);
    assertThat(createdQuery.count()).isEqualTo(1);
    assertThat(failedQuery.count()).isEqualTo(4);

    createdJobLogEntry = createdQuery.singleResult();
    assertThat(createdJobLogEntry.getJobRetries()).isEqualTo(3);

    failedJobLogEntry = failedQuery.list().get(0);
    assertThat(failedJobLogEntry.getJobRetries()).isEqualTo(3);

    failedJobLogEntry = failedQuery.list().get(1);
    assertThat(failedJobLogEntry.getJobRetries()).isEqualTo(2);

    failedJobLogEntry = failedQuery.list().get(2);
    assertThat(failedJobLogEntry.getJobRetries()).isEqualTo(1);

    failedJobLogEntry = failedQuery.list().get(3);
    assertThat(failedJobLogEntry.getJobRetries()).isEqualTo(0);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  @Test
  public void testSuccessfulJobEvent() {
    // given
    runtimeService.startProcessInstanceByKey("process", Variables.createVariables().putValue("fail", false));

    String jobId = managementService.createJobQuery().singleResult().getId();

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().jobId(jobId);
    HistoricJobLogQuery createdQuery = historyService.createHistoricJobLogQuery().jobId(jobId).creationLog();
    HistoricJobLogQuery succeededQuery = historyService.createHistoricJobLogQuery().jobId(jobId).successLog();

    // there exists one historic job log entry
    assertThat(query.count()).isEqualTo(1);
    assertThat(createdQuery.count()).isEqualTo(1);
    assertThat(succeededQuery.count()).isEqualTo(0);

    // when
    managementService.executeJob(jobId);

    // then
    assertThat(query.count()).isEqualTo(2);
    assertThat(createdQuery.count()).isEqualTo(1);
    assertThat(succeededQuery.count()).isEqualTo(1);

    HistoricJobLog createdJobLogEntry = createdQuery.singleResult();
    assertThat(createdJobLogEntry.getJobRetries()).isEqualTo(3);

    HistoricJobLog succeededJobLogEntry = succeededQuery.singleResult();
    assertThat(succeededJobLogEntry.getJobRetries()).isEqualTo(3);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  @Test
  public void testSuccessfulJobEventExecutedByJobExecutor() {
    // given
    runtimeService.startProcessInstanceByKey("process", Variables.createVariables().putValue("fail", false));

    String jobId = managementService.createJobQuery().singleResult().getId();

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().jobId(jobId);
    HistoricJobLogQuery createdQuery = historyService.createHistoricJobLogQuery().jobId(jobId).creationLog();
    HistoricJobLogQuery succeededQuery = historyService.createHistoricJobLogQuery().jobId(jobId).successLog();

    // there exists one historic job log entry
    assertThat(query.count()).isEqualTo(1);
    assertThat(createdQuery.count()).isEqualTo(1);
    assertThat(succeededQuery.count()).isEqualTo(0);

    // when
    testRule.executeAvailableJobs();

    // then
    assertThat(query.count()).isEqualTo(2);
    assertThat(createdQuery.count()).isEqualTo(1);
    assertThat(succeededQuery.count()).isEqualTo(1);

    HistoricJobLog createdJobLogEntry = createdQuery.singleResult();
    assertThat(createdJobLogEntry.getJobRetries()).isEqualTo(3);

    HistoricJobLog succeededJobLogEntry = succeededQuery.singleResult();
    assertThat(succeededJobLogEntry.getJobRetries()).isEqualTo(3);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  @Test
  public void testSuccessfulAndFailedJobEvents() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    String jobId = managementService.createJobQuery().singleResult().getId();

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().jobId(jobId);
    HistoricJobLogQuery createdQuery = historyService.createHistoricJobLogQuery().jobId(jobId).creationLog();
    HistoricJobLogQuery failedQuery = historyService.createHistoricJobLogQuery().jobId(jobId).failureLog().orderByJobRetries().desc();
    HistoricJobLogQuery succeededQuery = historyService.createHistoricJobLogQuery().jobId(jobId).successLog();

    // there exists one historic job log entry
    assertThat(query.count()).isEqualTo(1);
    assertThat(createdQuery.count()).isEqualTo(1);
    assertThat(failedQuery.count()).isEqualTo(0);
    assertThat(succeededQuery.count()).isEqualTo(0);

    // when (1)
    try {
      managementService.executeJob(jobId);
      fail("exception expected");
    } catch (Exception e) {
      // expected
    }

    // then (1)
    assertThat(query.count()).isEqualTo(2);
    assertThat(createdQuery.count()).isEqualTo(1);
    assertThat(failedQuery.count()).isEqualTo(1);
    assertThat(succeededQuery.count()).isEqualTo(0);

    HistoricJobLog createdJobLogEntry = createdQuery.singleResult();
    assertThat(createdJobLogEntry.getJobRetries()).isEqualTo(3);

    HistoricJobLog failedJobLogEntry = failedQuery.singleResult();
    assertThat(failedJobLogEntry.getJobRetries()).isEqualTo(3);

    // when (2)
    try {
      managementService.executeJob(jobId);
      fail("exception expected");
    } catch (Exception e) {
      // expected
    }

    // then (2)
    assertThat(query.count()).isEqualTo(3);
    assertThat(createdQuery.count()).isEqualTo(1);
    assertThat(failedQuery.count()).isEqualTo(2);
    assertThat(succeededQuery.count()).isEqualTo(0);

    createdJobLogEntry = createdQuery.singleResult();
    assertThat(createdJobLogEntry.getJobRetries()).isEqualTo(3);

    failedJobLogEntry = failedQuery.list().get(0);
    assertThat(failedJobLogEntry.getJobRetries()).isEqualTo(3);

    failedJobLogEntry = failedQuery.list().get(1);
    assertThat(failedJobLogEntry.getJobRetries()).isEqualTo(2);

    // when (3)
    runtimeService.setVariable(processInstanceId, "fail", false);
    managementService.executeJob(jobId);

    // then (3)
    assertThat(query.count()).isEqualTo(4);
    assertThat(createdQuery.count()).isEqualTo(1);
    assertThat(failedQuery.count()).isEqualTo(2);
    assertThat(succeededQuery.count()).isEqualTo(1);

    createdJobLogEntry = createdQuery.singleResult();
    assertThat(createdJobLogEntry.getJobRetries()).isEqualTo(3);

    failedJobLogEntry = failedQuery.list().get(0);
    assertThat(failedJobLogEntry.getJobRetries()).isEqualTo(3);

    failedJobLogEntry = failedQuery.list().get(1);
    assertThat(failedJobLogEntry.getJobRetries()).isEqualTo(2);

    HistoricJobLog succeededJobLogEntry = succeededQuery.singleResult();
    assertThat(succeededJobLogEntry.getJobRetries()).isEqualTo(1);
  }

  @Deployment
  @Test
  public void testTerminateEndEvent() {
    // given
    runtimeService.startProcessInstanceByKey("process").getId();

    String serviceTask1JobId = managementService.createJobQuery().activityId("serviceTask1").singleResult().getId();

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery();
    assertThat(query.count()).isEqualTo(2);

    // serviceTask1
    HistoricJobLogQuery serviceTask1Query = historyService.createHistoricJobLogQuery().jobId(serviceTask1JobId);
    HistoricJobLogQuery serviceTask1CreatedQuery = historyService.createHistoricJobLogQuery().jobId(serviceTask1JobId).creationLog();
    HistoricJobLogQuery serviceTask1DeletedQuery = historyService.createHistoricJobLogQuery().jobId(serviceTask1JobId).deletionLog();
    HistoricJobLogQuery serviceTask1SuccessfulQuery = historyService.createHistoricJobLogQuery().jobId(serviceTask1JobId).successLog();

    assertThat(serviceTask1Query.count()).isEqualTo(1);
    assertThat(serviceTask1CreatedQuery.count()).isEqualTo(1);
    assertThat(serviceTask1DeletedQuery.count()).isEqualTo(0);
    assertThat(serviceTask1SuccessfulQuery.count()).isEqualTo(0);

    // serviceTask2
    String serviceTask2JobId = managementService.createJobQuery().activityId("serviceTask2").singleResult().getId();

    HistoricJobLogQuery serviceTask2Query = historyService.createHistoricJobLogQuery().jobId(serviceTask2JobId);
    HistoricJobLogQuery serviceTask2CreatedQuery = historyService.createHistoricJobLogQuery().jobId(serviceTask2JobId).creationLog();
    HistoricJobLogQuery serviceTask2DeletedQuery = historyService.createHistoricJobLogQuery().jobId(serviceTask2JobId).deletionLog();
    HistoricJobLogQuery serviceTask2SuccessfulQuery = historyService.createHistoricJobLogQuery().jobId(serviceTask2JobId).successLog();

    assertThat(serviceTask2Query.count()).isEqualTo(1);
    assertThat(serviceTask2CreatedQuery.count()).isEqualTo(1);
    assertThat(serviceTask2DeletedQuery.count()).isEqualTo(0);
    assertThat(serviceTask2SuccessfulQuery.count()).isEqualTo(0);

    // when
    managementService.executeJob(serviceTask1JobId);

    // then
    assertThat(query.count()).isEqualTo(4);

    // serviceTas1
    assertThat(serviceTask1Query.count()).isEqualTo(2);
    assertThat(serviceTask1CreatedQuery.count()).isEqualTo(1);
    assertThat(serviceTask1DeletedQuery.count()).isEqualTo(0);
    assertThat(serviceTask1SuccessfulQuery.count()).isEqualTo(1);

    HistoricJobLog serviceTask1CreatedJobLogEntry = serviceTask1CreatedQuery.singleResult();
    assertThat(serviceTask1CreatedJobLogEntry.getJobRetries()).isEqualTo(3);

    HistoricJobLog serviceTask1SuccessfulJobLogEntry = serviceTask1SuccessfulQuery.singleResult();
    assertThat(serviceTask1SuccessfulJobLogEntry.getJobRetries()).isEqualTo(3);

    // serviceTask2
    assertThat(serviceTask2Query.count()).isEqualTo(2);
    assertThat(serviceTask2CreatedQuery.count()).isEqualTo(1);
    assertThat(serviceTask2DeletedQuery.count()).isEqualTo(1);
    assertThat(serviceTask2SuccessfulQuery.count()).isEqualTo(0);

    HistoricJobLog serviceTask2CreatedJobLogEntry = serviceTask2CreatedQuery.singleResult();
    assertThat(serviceTask2CreatedJobLogEntry.getJobRetries()).isEqualTo(3);

    HistoricJobLog serviceTask2DeletedJobLogEntry = serviceTask2DeletedQuery.singleResult();
    assertThat(serviceTask2DeletedJobLogEntry.getJobRetries()).isEqualTo(3);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/history/HistoricJobLogTest.testSuperProcessWithCallActivity.bpmn20.xml",
      "org/camunda/bpm/engine/test/history/HistoricJobLogTest.testSubProcessWithErrorEndEvent.bpmn20.xml"
  })
  @Test
  public void testErrorEndEventInterruptingCallActivity() {
    // given
    runtimeService.startProcessInstanceByKey("process").getId();

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery();
    assertThat(query.count()).isEqualTo(2);

    // serviceTask1
    String serviceTask1JobId = managementService.createJobQuery().activityId("serviceTask1").singleResult().getId();

    HistoricJobLogQuery serviceTask1Query = historyService.createHistoricJobLogQuery().jobId(serviceTask1JobId);
    HistoricJobLogQuery serviceTask1CreatedQuery = historyService.createHistoricJobLogQuery().jobId(serviceTask1JobId).creationLog();
    HistoricJobLogQuery serviceTask1DeletedQuery = historyService.createHistoricJobLogQuery().jobId(serviceTask1JobId).deletionLog();
    HistoricJobLogQuery serviceTask1SuccessfulQuery = historyService.createHistoricJobLogQuery().jobId(serviceTask1JobId).successLog();

    assertThat(serviceTask1Query.count()).isEqualTo(1);
    assertThat(serviceTask1CreatedQuery.count()).isEqualTo(1);
    assertThat(serviceTask1DeletedQuery.count()).isEqualTo(0);
    assertThat(serviceTask1SuccessfulQuery.count()).isEqualTo(0);

    // serviceTask2
    String serviceTask2JobId = managementService.createJobQuery().activityId("serviceTask2").singleResult().getId();

    HistoricJobLogQuery serviceTask2Query = historyService.createHistoricJobLogQuery().jobId(serviceTask2JobId);
    HistoricJobLogQuery serviceTask2CreatedQuery = historyService.createHistoricJobLogQuery().jobId(serviceTask2JobId).creationLog();
    HistoricJobLogQuery serviceTask2DeletedQuery = historyService.createHistoricJobLogQuery().jobId(serviceTask2JobId).deletionLog();
    HistoricJobLogQuery serviceTask2SuccessfulQuery = historyService.createHistoricJobLogQuery().jobId(serviceTask2JobId).successLog();

    assertThat(serviceTask2Query.count()).isEqualTo(1);
    assertThat(serviceTask2CreatedQuery.count()).isEqualTo(1);
    assertThat(serviceTask2DeletedQuery.count()).isEqualTo(0);
    assertThat(serviceTask2SuccessfulQuery.count()).isEqualTo(0);

    // when
    managementService.executeJob(serviceTask1JobId);

    // then
    assertThat(query.count()).isEqualTo(4);

    // serviceTask1
    assertThat(serviceTask1Query.count()).isEqualTo(2);
    assertThat(serviceTask1CreatedQuery.count()).isEqualTo(1);
    assertThat(serviceTask1DeletedQuery.count()).isEqualTo(0);
    assertThat(serviceTask1SuccessfulQuery.count()).isEqualTo(1);

    HistoricJobLog serviceTask1CreatedJobLogEntry = serviceTask1CreatedQuery.singleResult();
    assertThat(serviceTask1CreatedJobLogEntry.getJobRetries()).isEqualTo(3);

    HistoricJobLog serviceTask1SuccessfulJobLogEntry = serviceTask1SuccessfulQuery.singleResult();
    assertThat(serviceTask1SuccessfulJobLogEntry.getJobRetries()).isEqualTo(3);

    // serviceTask2
    assertThat(serviceTask2Query.count()).isEqualTo(2);
    assertThat(serviceTask2CreatedQuery.count()).isEqualTo(1);
    assertThat(serviceTask2DeletedQuery.count()).isEqualTo(1);
    assertThat(serviceTask2SuccessfulQuery.count()).isEqualTo(0);

    HistoricJobLog serviceTask2CreatedJobLogEntry = serviceTask2CreatedQuery.singleResult();
    assertThat(serviceTask2CreatedJobLogEntry.getJobRetries()).isEqualTo(3);

    HistoricJobLog serviceTask2DeletedJobLogEntry = serviceTask2DeletedQuery.singleResult();
    assertThat(serviceTask2DeletedJobLogEntry.getJobRetries()).isEqualTo(3);

    // there should be one task after the boundary event
    assertThat(taskService.createTaskQuery().count()).isEqualTo(1);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  @Test
  public void testDeletedJob() {
    // given
    runtimeService.startProcessInstanceByKey("process");

    String jobId = managementService.createJobQuery().singleResult().getId();

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().jobId(jobId);
    HistoricJobLogQuery createdQuery = historyService.createHistoricJobLogQuery().jobId(jobId).creationLog();
    HistoricJobLogQuery deletedQuery = historyService.createHistoricJobLogQuery().jobId(jobId).deletionLog();

    // there exists one historic job log entry
    assertThat(query.count()).isEqualTo(1);
    assertThat(createdQuery.count()).isEqualTo(1);
    assertThat(deletedQuery.count()).isEqualTo(0);

    // when
    managementService.deleteJob(jobId);

    // then
    assertThat(query.count()).isEqualTo(2);
    assertThat(createdQuery.count()).isEqualTo(1);
    assertThat(deletedQuery.count()).isEqualTo(1);

    HistoricJobLog createdJobLogEntry = createdQuery.singleResult();
    assertThat(createdJobLogEntry.getJobRetries()).isEqualTo(3);

    HistoricJobLog deletedJobLogEntry = deletedQuery.singleResult();
    assertThat(deletedJobLogEntry.getJobRetries()).isEqualTo(3);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  @Test
  public void testDeletedProcessInstance() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    String jobId = managementService.createJobQuery().singleResult().getId();

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().jobId(jobId);
    HistoricJobLogQuery createdQuery = historyService.createHistoricJobLogQuery().jobId(jobId).creationLog();
    HistoricJobLogQuery deletedQuery = historyService.createHistoricJobLogQuery().jobId(jobId).deletionLog();

    // there exists one historic job log entry
    assertThat(query.count()).isEqualTo(1);
    assertThat(createdQuery.count()).isEqualTo(1);
    assertThat(deletedQuery.count()).isEqualTo(0);

    // when
    runtimeService.deleteProcessInstance(processInstanceId, null);

    // then
    assertThat(query.count()).isEqualTo(2);
    assertThat(createdQuery.count()).isEqualTo(1);
    assertThat(deletedQuery.count()).isEqualTo(1);

    HistoricJobLog createdJobLogEntry = createdQuery.singleResult();
    assertThat(createdJobLogEntry.getJobRetries()).isEqualTo(3);

    HistoricJobLog deletedJobLogEntry = deletedQuery.singleResult();
    assertThat(deletedJobLogEntry.getJobRetries()).isEqualTo(3);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  @Test
  public void testExceptionStacktrace() {
    // given
    runtimeService.startProcessInstanceByKey("process");

    String jobId = managementService.createJobQuery().singleResult().getId();

    // when
    try {
      managementService.executeJob(jobId);
      fail("exception expected");
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
    assertThat(stacktrace).isNotNull();
    assertThat(stacktrace).containsIgnoringCase(FailingDelegate.EXCEPTION_MESSAGE);
  }

  @Test
  public void shouldGetJobExceptionStacktraceUnexistingJobId() {
    try {
      historyService.getHistoricJobLogExceptionStacktrace("unexistingjob");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      assertThat(re.getMessage()).containsIgnoringCase("No historic job log found with id unexistingjob");
    }
  }

  @Test
  public void shouldGetJobExceptionStacktraceNullJobId() {
    try {
      historyService.getHistoricJobLogExceptionStacktrace(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      assertThat(re.getMessage()).containsIgnoringCase("historicJobLogId is null");
    }
  }

  @Deployment
  @Test
  public void testDifferentExceptions() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    String jobId = managementService.createJobQuery().singleResult().getId();

    // when (1)
    try {
      managementService.executeJob(jobId);
      fail("exception expected");
    } catch (Exception e) {
      // expected
    }

    // then (1)
    HistoricJobLog serviceTask1FailedHistoricJobLog = historyService
        .createHistoricJobLogQuery()
        .failureLog()
        .singleResult();

    String serviceTask1FailedHistoricJobLogId = serviceTask1FailedHistoricJobLog.getId();

    assertThat(serviceTask1FailedHistoricJobLog.getJobExceptionMessage()).isEqualTo(FirstFailingDelegate.FIRST_EXCEPTION_MESSAGE);

    String serviceTask1Stacktrace = historyService.getHistoricJobLogExceptionStacktrace(serviceTask1FailedHistoricJobLogId);
    assertThat(serviceTask1Stacktrace).isNotNull();
    assertThat(serviceTask1Stacktrace).containsIgnoringCase(FirstFailingDelegate.FIRST_EXCEPTION_MESSAGE);
    assertThat(serviceTask1Stacktrace).containsIgnoringCase(FirstFailingDelegate.class.getName());

    // when (2)
    runtimeService.setVariable(processInstanceId, "firstFail", false);
    try {
      managementService.executeJob(jobId);
      fail("exception expected");
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

    assertThat(serviceTask2FailedHistoricJobLog.getJobExceptionMessage()).isEqualTo(SecondFailingDelegate.SECOND_EXCEPTION_MESSAGE);

    String serviceTask2Stacktrace = historyService.getHistoricJobLogExceptionStacktrace(serviceTask2FailedHistoricJobLogId);
    assertThat(serviceTask2Stacktrace).isNotNull();
    assertThat(serviceTask2Stacktrace).containsIgnoringCase(SecondFailingDelegate.SECOND_EXCEPTION_MESSAGE);
    assertThat(serviceTask2Stacktrace).containsIgnoringCase(SecondFailingDelegate.class.getName());

    assertThat(serviceTask1Stacktrace.equals(serviceTask2Stacktrace)).isFalse();
  }

  @Deployment
  @Test
  public void testThrowExceptionWithoutMessage() {
    // given
    runtimeService.startProcessInstanceByKey("process").getId();

    String jobId = managementService.createJobQuery().singleResult().getId();

    // when
    try {
      managementService.executeJob(jobId);
      fail("exception expected");
    } catch (Exception e) {
      // expected
    }

    // then
    HistoricJobLog failedHistoricJobLog = historyService
        .createHistoricJobLogQuery()
        .failureLog()
        .singleResult();

    String failedHistoricJobLogId = failedHistoricJobLog.getId();

    assertThat(failedHistoricJobLog.getJobExceptionMessage()).isNull();

    String stacktrace = historyService.getHistoricJobLogExceptionStacktrace(failedHistoricJobLogId);
    assertThat(stacktrace).isNotNull();
    assertThat(stacktrace).containsIgnoringCase(ThrowExceptionWithoutMessageDelegate.class.getName());
  }

  @Deployment
  @Test
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
      fail("exception expected");
    } catch (Exception e) {
      // expected
    }

    // then
    HistoricJobLog failedHistoricJobLog = historyService
        .createHistoricJobLogQuery()
        .failureLog()
        .singleResult();

    assertThat(failedHistoricJobLog).isNotNull();
    assertThat(failedHistoricJobLog.getJobExceptionMessage())
        .isEqualToIgnoringCase(exceptionMessage.substring(0, StringUtil.DB_MAX_STRING_LENGTH));
  }

  @Test
  @Ignore
  public void testAsyncAfterJobDefinitionAfterEngineRestart() {
    // given
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("testProcess")
      .startEvent()
      .manualTask()
      .camundaAsyncBefore()
      .camundaAsyncAfter()
      .endEvent()
      .done();

    testRule.deploy(modelInstance);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    JobDefinition asyncBeforeJobDef = managementService.createJobDefinitionQuery()
        .jobConfiguration("async-before").singleResult();
    JobDefinition asyncAfterJobDef = managementService.createJobDefinitionQuery()
        .jobConfiguration("async-after").singleResult();

    // clearing the deployment cache as if the engine had restarted
    DeploymentCache deploymentCache = processEngineConfiguration.getDeploymentCache();
    deploymentCache.removeProcessDefinition(processInstance.getProcessDefinitionId());

    // when
    Job asyncBeforeJob = managementService.createJobQuery().singleResult();
    managementService.executeJob(asyncBeforeJob.getId());

    Job asyncAfterJob = managementService.createJobQuery().singleResult();
    managementService.executeJob(asyncAfterJob.getId());

    // then
    assertThat(asyncBeforeJob.getJobDefinitionId()).isEqualTo(asyncBeforeJobDef.getId());
    assertThat(asyncAfterJob.getJobDefinitionId()).isEqualTo(asyncAfterJobDef.getId());

    HistoricJobLog asyncBeforeLog = historyService.createHistoricJobLogQuery()
        .creationLog().jobId(asyncBeforeJob.getId()).singleResult();
    assertThat(asyncBeforeLog.getJobDefinitionId()).isEqualTo(asyncBeforeJobDef.getId());

    HistoricJobLog asyncAfterLog = historyService.createHistoricJobLogQuery()
        .creationLog().jobId(asyncAfterJob.getId()).singleResult();
    assertThat(asyncAfterLog.getJobDefinitionId()).isEqualTo(asyncAfterJobDef.getId());
  }

  /**
   * returns a random of the given size using characters [0-1]
   */
  protected static String randomString(int numCharacters) {
    return new BigInteger(numCharacters, new Random()).toString(2);
  }


  @Test
  public void testDeleteByteArray() {
    final String processDefinitionId = "myProcessDefition";

    processEngineConfiguration.getCommandExecutorTxRequiresNew().execute((Command<Void>) commandContext -> {

      for (int i = 0; i < 1234; i++) {
        HistoricJobLogEventEntity log = new HistoricJobLogEventEntity();
        log.setJobId(String.valueOf(i));
        log.setTimestamp(new Date());
        log.setJobDefinitionType(MessageEntity.TYPE);
        log.setProcessDefinitionId(processDefinitionId);


        byte[] aByteValue = StringUtil.toByteArray("abc");
        ByteArrayEntity byteArray = ExceptionUtil.createJobExceptionByteArray(aByteValue, ResourceTypes.HISTORY);
        log.setExceptionByteArrayId(byteArray.getId());

        commandContext
          .getHistoricJobLogManager()
          .insert(log);
      }

      return null;
    });

    assertThat(historyService.createHistoricJobLogQuery().count()).isEqualTo(1234);

    processEngineConfiguration.getCommandExecutorTxRequiresNew().execute((Command<Void>) commandContext -> {
      commandContext.getHistoricJobLogManager().deleteHistoricJobLogsByProcessDefinitionId(processDefinitionId);
      return null;
    });

    assertThat(historyService.createHistoricJobLogQuery().count()).isEqualTo(0);
  }

}
