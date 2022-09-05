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
package org.camunda.bpm.engine.test.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.commons.testing.ProcessEngineLoggingRule;
import org.camunda.commons.testing.WatchLogger;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.spi.ILoggingEvent;

public class ProcessDataLoggingContextTest {

  private static final String PROCESS = "process";
  private static final String B_KEY = "businessKey1";
  private static final String B_KEY2 = "businessKey2";
  private static final String FAILING_PROCESS = "failing-process";
  private static final String TENANT_ID = "testTenant";

  private static final String CMD_LOGGER = "org.camunda.bpm.engine.cmd";
  private static final String CONTEXT_LOGGER = "org.camunda.bpm.engine.context";
  private static final String JOBEXEC_LOGGER = "org.camunda.bpm.engine.jobexecutor";
  private static final String PVM_LOGGER = "org.camunda.bpm.engine.pvm";

  private static final String LOG_IDENT_FAILURE = "ENGINE-16004";

  private RuntimeContainerDelegate runtimeContainerDelegate = RuntimeContainerDelegate.INSTANCE.get();
  private boolean defaultEngineRegistered;

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(configuration ->
      configuration.setLoggingContextBusinessKey("businessKey"));

  @Rule
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);

  @Rule
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule();

  private RuntimeService runtimeService;
  private TaskService taskService;

  @Before
  public void setupServices() {
    runtimeService = engineRule.getRuntimeService();
    taskService = engineRule.getTaskService();
    defaultEngineRegistered = false;
  }

  @After
  public void resetClock() {
    ClockUtil.reset();
  }

  @After
  public void tearDown() {
    if (defaultEngineRegistered) {
      runtimeContainerDelegate.unregisterProcessEngine(engineRule.getProcessEngine());
    }
  }

  @After
  public void resetLogConfiguration() {
    engineRule.getProcessEngineConfiguration()
      .setLoggingContextActivityId("activityId")
      .setLoggingContextApplicationName("applicationName")
      .setLoggingContextBusinessKey("businessKey")
      .setLoggingContextProcessDefinitionId("processDefinitionId")
      .setLoggingContextProcessInstanceId("processInstanceId")
      .setLoggingContextTenantId("tenantId")
      .setLoggingContextEngineName("engineName");
  }

  @Test
  @WatchLogger(loggerNames = PVM_LOGGER, level = "DEBUG")
  public void shouldNotLogBusinessKeyIfNotConfigured() {
    // given
    engineRule.getProcessEngineConfiguration().setLoggingContextBusinessKey(null);
    manageDeployment(modelOneTaskProcess());
    // when
    ProcessInstance instance = runtimeService.startProcessInstanceByKey(PROCESS, B_KEY);
    taskService.complete(taskService.createTaskQuery().singleResult().getId());
    // then
    assertActivityLogs(instance, "ENGINE-200", Arrays.asList("start", "waitState", "end"), true, false, true, true, "default");
  }

  @Test
  @WatchLogger(loggerNames = PVM_LOGGER, level = "DEBUG")
  public void shouldNotLogDisabledProperties() {
    // given
    engineRule.getProcessEngineConfiguration()
      .setLoggingContextActivityId(null)
      .setLoggingContextBusinessKey(null)
      .setLoggingContextProcessDefinitionId("")
      .setLoggingContextEngineName(null);
    manageDeployment(modelOneTaskProcess());
    // when
    ProcessInstance instance = runtimeService.startProcessInstanceByKey(PROCESS, B_KEY);
    taskService.complete(taskService.createTaskQuery().singleResult().getId());
    // then
    assertActivityLogs(instance, "ENGINE-200", null, true, false, false, false, null);
  }

  @Test
  @WatchLogger(loggerNames = {PVM_LOGGER, CMD_LOGGER}, level = "DEBUG")
  public void shouldLogMdcPropertiesOnlyInActivityContext() {
    // given
    manageDeployment(modelOneTaskProcess());
    // when
    ProcessInstance instance = runtimeService.startProcessInstanceByKey(PROCESS, B_KEY);
    taskService.complete(taskService.createTaskQuery().singleResult().getId());
    // then activity context logs are present
    assertActivityLogsPresent(instance, Arrays.asList("start", "waitState", "end"));
    // other logs do not contain MDC properties
    assertActivityLogsPresentWithoutMdc("ENGINE-130");
  }

  @Test
  @WatchLogger(loggerNames = {PVM_LOGGER, CMD_LOGGER}, level = "DEBUG")
  public void shouldLogCustomMdcPropertiesOnlyInActivityContext() {
    // given
    engineRule.getProcessEngineConfiguration()
      .setLoggingContextActivityId("actId")
      .setLoggingContextApplicationName("appName")
      .setLoggingContextBusinessKey("busKey")
      .setLoggingContextProcessDefinitionId("defId")
      .setLoggingContextProcessInstanceId("instId")
      .setLoggingContextTenantId("tenId")
      .setLoggingContextEngineName("engName");
    manageDeployment(modelOneTaskProcess());
    // when
    ProcessInstance instance = runtimeService.startProcessInstanceByKey(PROCESS, B_KEY);
    taskService.complete(taskService.createTaskQuery().singleResult().getId());
    // then activity context logs are present
    assertActivityLogsPresent(instance, Arrays.asList("start", "waitState", "end"), "actId", "appName", "busKey", "defId", "instId", "tenId", "engName");
  }

  @Test
  @WatchLogger(loggerNames = {NestedLoggingDelegate.LOGGER_NAME}, level = "DEBUG")
  public void shouldLogCustomMdcPropertiesWithNestedCommand() {
    // given
    engineRule.getProcessEngineConfiguration()
      .setLoggingContextActivityId("actId")
      .setLoggingContextBusinessKey("busKey")
      .setLoggingContextProcessDefinitionId("defId")
      .setLoggingContextProcessInstanceId("instId")
      .setLoggingContextTenantId("tenId");

    manageDeployment(Bpmn.createExecutableProcess(PROCESS)
      .startEvent("start")
      .serviceTask("startProcess")
        .camundaClass(NestedLoggingDelegate.class.getName())
      .endEvent("end")
      .done());

    // when
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS, B_KEY);

    // then
    List<ILoggingEvent> customLogs = loggingRule.getLog();
    assertThat(customLogs).hasSize(2);

    for (ILoggingEvent logEvent : customLogs) {
      assertThat(logEvent.getMDCPropertyMap()).containsEntry("actId", "startProcess");
      assertThat(logEvent.getMDCPropertyMap()).containsEntry("busKey", B_KEY);
      assertThat(logEvent.getMDCPropertyMap()).containsEntry("defId", processInstance.getProcessDefinitionId());
      assertThat(logEvent.getMDCPropertyMap()).containsEntry("instId", processInstance.getId());
      assertThat(logEvent.getMDCPropertyMap()).containsEntry("tenId", processInstance.getTenantId());
    }
  }

  @Test
  @WatchLogger(loggerNames = PVM_LOGGER, level = "DEBUG")
  public void shouldLogMdcPropertiesForAsyncBeforeInTaskContext() {
    // given
    manageDeployment(Bpmn.createExecutableProcess(PROCESS)
        .startEvent("start")
        .userTask("waitState").camundaAsyncBefore()
        .endEvent("end")
        .done());
    // when
    ProcessInstance pi = runtimeService.startProcessInstanceByKey(PROCESS, B_KEY);
    testRule.waitForJobExecutorToProcessAllJobs();
    taskService.complete(taskService.createTaskQuery().singleResult().getId());
    // then
    assertActivityLogsPresent(pi, Arrays.asList("start", "waitState", "end"));
  }

  @Test
  @WatchLogger(loggerNames = PVM_LOGGER, level = "DEBUG")
  public void shouldLogMdcPropertiesForAsyncAfterInTaskContext() {
    // given
    manageDeployment(Bpmn.createExecutableProcess(PROCESS)
        .startEvent("start")
        .userTask("waitState").camundaAsyncAfter()
        .endEvent("end")
        .done());
    // when
    ProcessInstance pi = runtimeService.startProcessInstanceByKey(PROCESS, B_KEY);
    taskService.complete(taskService.createTaskQuery().singleResult().getId());
    testRule.waitForJobExecutorToProcessAllJobs();
    // then
    assertActivityLogsPresent(pi, Arrays.asList("start", "waitState", "end"));
  }

  @Test
  @WatchLogger(loggerNames = {JOBEXEC_LOGGER, PVM_LOGGER}, level = "DEBUG")
  public void shouldLogMdcPropertiesForTimerInTaskContext() {
    // given
    manageDeployment(Bpmn.createExecutableProcess(PROCESS)
        .startEvent("start")
        .intermediateCatchEvent("timer").timerWithDuration("PT10S")
        .userTask("waitState")
        .endEvent("end")
        .done());
    // when
    ProcessInstance pi = runtimeService.startProcessInstanceByKey(PROCESS, B_KEY);
    ClockUtil.offset(TimeUnit.MINUTES.toMillis(2L));
    testRule.waitForJobExecutorToProcessAllJobs();
    taskService.complete(taskService.createTaskQuery().singleResult().getId());
    // then
    assertActivityLogsPresent(pi, Arrays.asList("start", "timer", "waitState", "end"));
    // job executor logs do not contain MDC properties
    assertActivityLogsPresentWithoutMdc("ENGINE-140");
  }

  @Test
  @WatchLogger(loggerNames = CONTEXT_LOGGER, level = "ERROR")
  public void shouldLogFailureFromDelegateInTaskContext() {
    // given
    manageDeployment(modelDelegateFailure());
    ProcessInstance instance = runtimeService.startProcessInstanceByKey(PROCESS, B_KEY);
    // when
    try {
      taskService.complete(taskService.createTaskQuery().singleResult().getId());
      fail("Exception expected");
    } catch (Exception e) {
      // expected exception in the task listener that is not caught
    }
    // then
    assertFailureLogPresent(instance, "failingTask");
  }

  @Test
  @WatchLogger(loggerNames = CONTEXT_LOGGER, level = "ERROR")
  public void shouldLogFailureFromDelegateInTaskContextWithChangedBusinessKey() {
    // given
    manageDeployment(Bpmn.createExecutableProcess(PROCESS)
        .startEvent("start")
        .userTask("waitState")
        .serviceTask("bkeyChangingTask")
          .camundaClass(BusinessKeyChangeDelegate.class)
        .serviceTask("failingTask")
          .camundaClass(FailingDelegate.class)
        .endEvent("end")
        .done());
    ProcessInstance instance = runtimeService.startProcessInstanceByKey(PROCESS, B_KEY);
    // when
    try {
      taskService.complete(taskService.createTaskQuery().singleResult().getId());
      fail("Exception expected");
    } catch (Exception e) {
      // expected exception in the task listener that is not caught
    }
    // then
    assertFailureLogPresent(instance, LOG_IDENT_FAILURE, "failingTask", null, B_KEY2, 1);
  }

  @Test
  @WatchLogger(loggerNames = CONTEXT_LOGGER, level = "ERROR")
  public void shouldLogFailureFromCreateTaskListenerInTaskContext() {
    // given
    manageDeployment(Bpmn.createExecutableProcess(PROCESS)
        .startEvent("start")
        .userTask("waitState")
        .userTask("failingTask")
          .camundaTaskListenerClass(TaskListener.EVENTNAME_CREATE, FailingTaskListener.class)
        .endEvent("end")
        .done());
    ProcessInstance instance = runtimeService.startProcessInstanceByKey(PROCESS, B_KEY);
    // when
    try {
      taskService.complete(taskService.createTaskQuery().singleResult().getId());
      fail("Exception expected");
    } catch (Exception e) {
      // expected exception in the task listener that is not caught
    }
    assertFailureLogPresent(instance, "failingTask");
  }

  @Test
  @WatchLogger(loggerNames = CONTEXT_LOGGER, level = "ERROR")
  public void shouldLogFailureFromAssignTaskListenerInTaskContext() {
    // given
    manageDeployment(Bpmn.createExecutableProcess(PROCESS)
        .startEvent("start")
        .userTask("failingTask")
          .camundaTaskListenerClass(TaskListener.EVENTNAME_ASSIGNMENT, FailingTaskListener.class)
        .endEvent("end")
        .done());
    ProcessInstance instance = runtimeService.startProcessInstanceByKey(PROCESS, B_KEY);
    // when
    try {
      taskService.setAssignee(taskService.createTaskQuery().singleResult().getId(), "testUser");
      fail("Exception expected");
    } catch (Exception e) {
      // expected exception in the task listener that is not caught
    }
    assertFailureLogPresent(instance, "failingTask");
  }

  @Test
  @WatchLogger(loggerNames = CONTEXT_LOGGER, level = "ERROR")
  public void shouldLogFailureFromCompleteTaskListenerInTaskContext() {
    // given
    manageDeployment(Bpmn.createExecutableProcess(PROCESS)
        .startEvent("start")
        .userTask("failingTask")
          .camundaTaskListenerClass(TaskListener.EVENTNAME_COMPLETE, FailingTaskListener.class)
        .endEvent("end")
        .done());
    ProcessInstance instance = runtimeService.startProcessInstanceByKey(PROCESS, B_KEY);
    // when
    try {
      taskService.complete(taskService.createTaskQuery().singleResult().getId());
      fail("Exception expected");
    } catch (Exception e) {
      // expected exception in the task listener that is not caught
    }
    assertFailureLogPresent(instance, "failingTask");
  }

  @Test
  @WatchLogger(loggerNames = CONTEXT_LOGGER, level = "ERROR")
  public void shouldLogFailureFromDeleteTaskListenerInTaskContext() {
    // given
    manageDeployment(Bpmn.createExecutableProcess(PROCESS)
        .startEvent("start")
        .userTask("failingTask")
          .camundaTaskListenerClass(TaskListener.EVENTNAME_DELETE, FailingTaskListener.class)
        .endEvent("end")
        .done());
    ProcessInstance instance = runtimeService.startProcessInstanceByKey(PROCESS, B_KEY);
    // when
    try {
      runtimeService.deleteProcessInstance(instance.getId(), "cancel it");
      fail("Exception expected");
    } catch (Exception e) {
      // expected exception in the task listener that is not caught
    }
    assertFailureLogPresent(instance, "failingTask");
  }

  @Test
  @WatchLogger(loggerNames = CONTEXT_LOGGER, level = "ERROR")
  public void shouldLogFailureFromExecutionListenerInTaskContext() {
    // given
    manageDeployment(modelExecutionListenerFailure());
    ProcessInstance instance = runtimeService.startProcessInstanceByKey(PROCESS, B_KEY);
    // when
    try {
      taskService.complete(taskService.createTaskQuery().singleResult().getId());
      fail("Exception expected");
    } catch (Exception e) {
      // expected exception in the execution listener that is not caught
    }
    // then
    assertFailureLogPresent(instance, "failingTask");
  }

  @Test
  @WatchLogger(loggerNames = {CONTEXT_LOGGER, JOBEXEC_LOGGER}, level = "WARN")
  public void shouldLogFailureFromTimeoutTaskListenerInTaskContext() {
    // given
    manageDeployment(Bpmn.createExecutableProcess(PROCESS)
        .startEvent("start")
        .userTask("failingTask")
          .camundaTaskListenerClassTimeoutWithDuration("failure-listener", FailingTaskListener.class, "PT10S")
        .endEvent("end")
        .done());
    ProcessInstance instance = runtimeService.startProcessInstanceByKey(PROCESS, B_KEY);
    // when
    ClockUtil.offset(TimeUnit.MINUTES.toMillis(2L));
    testRule.waitForJobExecutorToProcessAllJobs();
    // then
    assertFailureLogPresent(instance, "failingTask", 3);
    assertFailureLogPresent(instance, "ENGINE-14006", "failingTask", null, instance.getBusinessKey(), 3);
  }

  @Test
  @WatchLogger(loggerNames = CONTEXT_LOGGER, level = "ERROR")
  public void shouldLogFailureFromParallelTasksInCorrectTaskContext() {
    // given
    manageDeployment(Bpmn.createExecutableProcess(PROCESS)
        .startEvent("start")
        .userTask("waitState")
        .parallelGateway("pSplit")
          .serviceTask("task")
            .camundaClass(NoneDelegate.class)
          .endEvent("end")
        .moveToLastGateway()
          .serviceTask("failingTask")
            .camundaClass(FailingDelegate.class)
          .endEvent("failingEnd")
        .done());
    ProcessInstance instance = runtimeService.startProcessInstanceByKey(PROCESS, B_KEY);
    // when
    try {
      taskService.complete(taskService.createTaskQuery().singleResult().getId());
      fail("Exception expected");
    } catch (Exception e) {
      // expected exception in the service task that is not caught
    }
    // then
    assertFailureLogPresent(instance, "failingTask");
  }

  @Test
  @WatchLogger(loggerNames = CONTEXT_LOGGER, level = "DEBUG")
  public void shouldLogFailureFromNestedDelegateInOuterContext() {
    // given
    manageDeployment("failing.bpmn", Bpmn.createExecutableProcess(FAILING_PROCESS)
        .startEvent("failing_start")
        .serviceTask("failing_task")
          .camundaClass(FailingDelegate.class)
        .endEvent("failing_end")
        .done());
    manageDeployment(Bpmn.createExecutableProcess(PROCESS)
        .startEvent("start")
        .userTask("waitState")
        .serviceTask("startProcess")
          .camundaClass(NestedStartDelegate.class.getName())
        .endEvent("end")
        .done());
    ProcessInstance instance = runtimeService.startProcessInstanceByKey(PROCESS, B_KEY);
    // when
    try {
      taskService.complete(taskService.createTaskQuery().singleResult().getId());
      fail("Exception expected");
    } catch (Exception e) {
      // expected exception in the nested delegate that is not caught
    }
    // then
    assertFailureLogPresent(instance, "startProcess");
    assertBpmnStacktraceLogPresent(instance);
  }

  @Test
  @WatchLogger(loggerNames = CONTEXT_LOGGER, level = "DEBUG")
  public void shouldLogFailureFromNestedExecutionListenerInOuterContext() {
    // given
    manageDeployment("failing.bpmn", Bpmn.createExecutableProcess(FAILING_PROCESS)
        .startEvent("failing_start")
        .serviceTask("failing_task")
          .camundaClass(NoneDelegate.class.getName())
          .camundaExecutionListenerClass("end", FailingExecutionListener.class)
        .endEvent("failing_end")
        .done());
    manageDeployment(Bpmn.createExecutableProcess(PROCESS)
        .startEvent("start")
        .userTask("waitState")
        .serviceTask("startProcess")
          .camundaClass(NestedStartDelegate.class.getName())
        .endEvent("end")
        .done());
    ProcessInstance instance = runtimeService.startProcessInstanceByKey(PROCESS, B_KEY);
    // when
    try {
      taskService.complete(taskService.createTaskQuery().singleResult().getId());
      fail("Exception expected");
    } catch (Exception e) {
      // expected exception in the nested delegate that is not caught
    }
    // then
    assertFailureLogPresent(instance, "startProcess");
    assertBpmnStacktraceLogPresent(instance);
  }

  @Test
  @WatchLogger(loggerNames = CONTEXT_LOGGER, level = "ERROR")
  public void shouldLogFailureFromMessageCorrelationListenerInEventContext() {
    // given
    manageDeployment(Bpmn.createExecutableProcess(PROCESS)
        .startEvent("start")
        .intermediateCatchEvent("message")
          .message("testMessage")
          .camundaExecutionListenerClass("end", FailingExecutionListener.class)
        .endEvent("end")
        .done());
    ProcessInstance instance = runtimeService.startProcessInstanceByKey(PROCESS, B_KEY);
    // when
    try {
      runtimeService.correlateMessage("testMessage");
      fail("Exception expected");
    } catch (Exception e) {
      // expected exception in the task listener that is not caught
    }
    // then
    assertFailureLogPresent(instance, "message");
  }

  @Test
  @WatchLogger(loggerNames = CONTEXT_LOGGER, level = "ERROR")
  public void shouldLogFailureFromEventSubprocessInSubprocessTaskContext() {
    // given
    testRule.deployForTenant(TENANT_ID, "org/camunda/bpm/engine/test/logging/ProcessDataLoggingContextTest.shouldLogFailureFromEventSubprocessInSubprocessTaskContext.bpmn20.xml");
    ProcessInstance instance = runtimeService.startProcessInstanceByKey(PROCESS);
    // when
    try {
      runtimeService.correlateMessage("testMessage");
      fail("Exception expected");
    } catch (Exception e) {
      // expected exception in the delegate that is not caught
    }
    // then
    assertFailureLogPresent(instance, "sub_failingTask");
  }

  @Test
  @WatchLogger(loggerNames = CONTEXT_LOGGER, level = "ERROR")
  public void shouldLogInternalFailureInTaskContext() {
    // given
    manageDeployment(Bpmn.createExecutableProcess(PROCESS)
        .startEvent("start")
        .userTask("waitState")
        .serviceTask("failingTask")
          .camundaDelegateExpression("${foo}")
        .endEvent("end")
        .done());
    ProcessInstance pi = runtimeService.startProcessInstanceByKey(PROCESS, B_KEY);
    // when
    try {
      taskService.complete(taskService.createTaskQuery().singleResult().getId());
      fail("Exception expected");
    } catch (Exception e) {
      // expected exception in the delegate resolution that is not caught
    }
    // then
    assertFailureLogPresent(pi, "failingTask");
  }

  @Test
  @WatchLogger(loggerNames = CONTEXT_LOGGER, level = "ERROR")
  public void shouldLogInputOutputMappingFailureInTaskContext() {
    // given
    manageDeployment(Bpmn.createExecutableProcess(PROCESS)
        .startEvent("start")
        .userTask("waitState")
        .serviceTask("failingTask")
          .camundaClass(NoneDelegate.class)
          .camundaInputParameter("foo", "${foooo}")
        .endEvent("end")
        .done());
    ProcessInstance pi = runtimeService.startProcessInstanceByKey(PROCESS, B_KEY);
    // when
    try {
      taskService.complete(taskService.createTaskQuery().singleResult().getId());
      fail("Exception expected");
    } catch (Exception e) {
      // expected exception in the delegate resolution that is not caught
    }
    // then
    assertFailureLogPresent(pi, "failingTask");
  }

  @Test
  @WatchLogger(loggerNames = CONTEXT_LOGGER, level = "ERROR")
  public void shouldLogFailureFromDelegateInTaskContextInPa() {
    // given
    registerProcessEngine();
    TestApplicationReusingExistingEngine application = new TestApplicationReusingExistingEngine() {
      @Override
      public void createDeployment(String processArchiveName, DeploymentBuilder deploymentBuilder) {
        deploymentBuilder.addModelInstance("test.bpmn", modelDelegateFailure()).tenantId(TENANT_ID);
      }
    };
    application.deploy();
    ProcessInstance instance = runtimeService.startProcessInstanceByKey(PROCESS, B_KEY);
    // when
    try {
      taskService.complete(taskService.createTaskQuery().singleResult().getId());
      fail("Exception expected");
    } catch (Exception e) {
      // expected exception in the task listener that is not caught
    }
    application.undeploy();
    assertFailureLogInApplication(instance, "failingTask", application.getName());
  }

  @Test
  @WatchLogger(loggerNames = CONTEXT_LOGGER, level = "ERROR")
  public void shouldLogFailureFromExecutionListenerInTaskContextInPa() {
    // given
    registerProcessEngine();
    TestApplicationReusingExistingEngine application = new TestApplicationReusingExistingEngine() {
      @Override
      public void createDeployment(String processArchiveName, DeploymentBuilder deploymentBuilder) {
        deploymentBuilder.addModelInstance("test.bpmn", modelExecutionListenerFailure()).tenantId(TENANT_ID);
      }
    };
    application.deploy();
    ProcessInstance instance = runtimeService.startProcessInstanceByKey(PROCESS, B_KEY);
    // when
    try {
      taskService.complete(taskService.createTaskQuery().singleResult().getId());
      fail("Exception expected");
    } catch (Exception e) {
      // expected exception in the execution listener that is not caught
    }
    application.undeploy();
    // then
    assertFailureLogInApplication(instance, "failingTask", application.getName());
  }

  protected void assertActivityLogsPresent(ProcessInstance instance, List<String> expectedActivities) {
    assertActivityLogs(instance, "ENGINE-200", expectedActivities, true, true, true, true, "default");
  }

  protected void assertActivityLogsPresentWithoutMdc(String filter) {
    assertActivityLogs(null, filter, null, false, false, false, false, "default");
  }

  protected void assertActivityLogs(ProcessInstance instance, String filter, List<String> expectedActivities, boolean isMdcPresent,
      boolean isBusinessKeyPresent, boolean isActivityIdPresent, boolean isDefinitionIdPresent, String engineName) {
    assertActivityLogs(instance, filter, isActivityIdPresent ? expectedActivities : null, null,
        isBusinessKeyPresent ? instance.getBusinessKey() : null, isDefinitionIdPresent ? instance.getProcessDefinitionId() : null, engineName,
        isMdcPresent, null);
  }

  protected void assertActivityLogsPresent(ProcessInstance instance, List<String> expectedActivities, String activityIdProperty,
      String appNameProperty, String businessKeyProperty, String definitionIdProperty, String instanceIdProperty, String tenantIdProperty, String engineNameProperty) {
    assertLogs(instance, "ENGINE-200", expectedActivities, null, instance.getBusinessKey(), instance.getProcessDefinitionId(), "default", true, null,
        activityIdProperty, appNameProperty, businessKeyProperty, definitionIdProperty, instanceIdProperty, tenantIdProperty, engineNameProperty);
  }

  protected void assertFailureLogPresent(ProcessInstance instance, String activityId) {
    assertFailureLogPresent(instance, activityId, 1);
  }

  protected void assertFailureLogPresent(ProcessInstance instance, String activityId, int numberOfFailureLogs) {
    assertFailureLogPresent(instance, LOG_IDENT_FAILURE, activityId, null, instance.getBusinessKey(), numberOfFailureLogs);
  }

  protected void assertFailureLogInApplication(ProcessInstance instance, String activityId, String application) {
    assertFailureLogPresent(instance, LOG_IDENT_FAILURE, activityId, application, instance.getBusinessKey(), 1);
  }

  protected void assertFailureLogPresent(ProcessInstance instance, String filter, String activityId, String appName,
      String businessKey, int numberOfFailureLogs) {
    assertActivityLogs(instance, filter, Arrays.asList(activityId), appName, businessKey, instance.getProcessDefinitionId(), "default", true,
        numberOfFailureLogs);
  }

  protected void assertActivityLogs(ProcessInstance instance, String filter, List<String> expectedActivities, String appName,
      String businessKey, String definitionId, String engineName, boolean isMdcPresent, Integer numberOfLogs) {
    assertLogs(instance, filter, expectedActivities, appName, businessKey, definitionId, engineName, isMdcPresent, numberOfLogs,
        "activityId", "applicationName", "businessKey", "processDefinitionId", "processInstanceId", "tenantId", "engineName");
  }

  protected void assertLogs(ProcessInstance instance, String filter, List<String> expectedActivities, String appName,
      String businessKey, String definitionId, String engineName, boolean isMdcPresent, Integer numberOfLogs, String activityIdProperty, String appNameProperty,
      String businessKeyProperty, String definitionIdProperty, String instanceIdProperty, String tenantIdProperty, String engineNameProperty) {
    boolean foundLogEntries = false;
    Set<String> passedActivities = new HashSet<>();
    List<ILoggingEvent> filteredLog = loggingRule.getFilteredLog(filter);
    if (numberOfLogs != null) {
      assertThat(numberOfLogs.intValue()).isEqualTo(filteredLog.size());
    }
    for (ILoggingEvent logEvent : filteredLog) {
      Map<String, String> mdcPropertyMap = logEvent.getMDCPropertyMap();
      if (isMdcPresent) {
        // PVM log contains MDC properties
        String activityIdMdc = mdcPropertyMap.get(activityIdProperty);
        String appNameMdc = mdcPropertyMap.get(appNameProperty);
        String businessKeyMdc = mdcPropertyMap.get(businessKeyProperty);
        String definitionIdMdc = mdcPropertyMap.get(definitionIdProperty);
        String instanceIdMdc = mdcPropertyMap.get(instanceIdProperty);
        String tenantIdMdc = mdcPropertyMap.get(tenantIdProperty);
        String engineNameMdc = mdcPropertyMap.get(engineNameProperty);

        if (expectedActivities != null) {
          assertThat(activityIdMdc).isNotNull();
          assertThat(expectedActivities.contains(activityIdMdc)).isTrue();
          passedActivities.add(activityIdMdc);
        } else {
          assertThat(activityIdMdc).isNull();
        }

        if (appName != null) {
          assertThat(appName).isEqualTo(appNameMdc);
        } else {
          assertThat(appNameMdc).isNull();
        }

        if (businessKey != null) {
          assertThat(businessKey).isEqualTo( businessKeyMdc);
        } else {
          assertThat(businessKeyMdc).isNull();
        }

        if (definitionId != null) {
          assertThat(definitionId).isEqualTo( definitionIdMdc);
        } else {
          assertThat(definitionIdMdc).isNull();
        }

        if(engineName != null) {
          assertThat(engineName).isEqualTo( engineNameMdc);
        } else {
          assertThat(engineNameMdc).isNull();
        }

        assertThat(instanceIdMdc).isNotNull();
        assertThat(instance.getId()).isEqualTo( instanceIdMdc);

        assertThat(tenantIdMdc).isNotNull();
        assertThat(instance.getTenantId()).isEqualTo( tenantIdMdc);


      } else {
        assertThat(mdcPropertyMap.isEmpty()).isTrue();
      }
      foundLogEntries = true;
    }
    assertThat(foundLogEntries).isTrue();
    if (expectedActivities != null) {
      assertThat(passedActivities).containsExactlyInAnyOrderElementsOf(expectedActivities);
    }
  }

  protected void assertBpmnStacktraceLogPresent(ProcessInstance instance) {
    List<ILoggingEvent> bpmnStacktraceLog = loggingRule.getFilteredLog("ENGINE-16006");
    assertThat(bpmnStacktraceLog.size()).isEqualTo(2);
    for (int i = 0; i < bpmnStacktraceLog.size(); i++) {
      ILoggingEvent logEvent = bpmnStacktraceLog.get(i);
      Map<String, String> mdcPropertyMap = logEvent.getMDCPropertyMap();
      assertThat(mdcPropertyMap.containsKey("activityId")).isTrue();
      assertThat(mdcPropertyMap.containsKey("applicationName")).isFalse();
      assertThat(mdcPropertyMap.containsKey("processDefinitionId")).isTrue();
      assertThat(mdcPropertyMap.containsKey("processInstanceId")).isTrue();
      assertThat(mdcPropertyMap.containsKey("tenantId")).isTrue();
      if (i == 0) {
        // first BPMN stack trace log corresponds to nested service task
        assertThat(mdcPropertyMap.containsKey("businessKey")).isFalse();
        assertThat(mdcPropertyMap.get("activityId")).isEqualTo("failing_task");
        assertThat(mdcPropertyMap.get("processDefinitionId")).isNotEqualTo(instance.getProcessDefinitionId());
        assertThat(mdcPropertyMap.get("processInstanceId")).isNotEqualTo(instance.getId());
        assertThat(instance.getTenantId()).isEqualTo(mdcPropertyMap.get("tenantId"));
      } else {
        // second BPMN stack trace log corresponds to outer service task
        assertThat(mdcPropertyMap.containsKey("businessKey")).isTrue();
        assertThat("startProcess").isEqualTo(mdcPropertyMap.get("activityId"));
        assertThat(instance.getBusinessKey()).isEqualTo(mdcPropertyMap.get("businessKey"));
        assertThat(instance.getProcessDefinitionId()).isEqualTo(mdcPropertyMap.get("processDefinitionId"));
        assertThat(instance.getId()).isEqualTo(mdcPropertyMap.get("processInstanceId"));
        assertThat(instance.getTenantId()).isEqualTo(mdcPropertyMap.get("tenantId"));
      }
    }
  }

  protected void manageDeployment(BpmnModelInstance model) {
    manageDeployment("test.bpmn", model);
  }

  protected void manageDeployment(String name, BpmnModelInstance model) {
    testRule.deployForTenant(TENANT_ID, model);
  }

  protected void registerProcessEngine() {
    runtimeContainerDelegate.registerProcessEngine(engineRule.getProcessEngine());
    defaultEngineRegistered = true;
  }

  protected BpmnModelInstance modelOneTaskProcess() {
    return Bpmn.createExecutableProcess(PROCESS)
        .startEvent("start")
        .userTask("waitState")
        .endEvent("end")
        .done();
  }

  protected BpmnModelInstance modelDelegateFailure() {
    return Bpmn.createExecutableProcess(PROCESS)
        .startEvent("start")
        .userTask("waitState")
        .serviceTask("failingTask")
          .camundaClass(FailingDelegate.class)
        .endEvent("end")
        .done();
  }

  protected BpmnModelInstance modelExecutionListenerFailure() {
    return Bpmn.createExecutableProcess(PROCESS)
        .startEvent("start")
        .userTask("failingTask")
          .camundaExecutionListenerClass("end", FailingExecutionListener.class)
        .endEvent("end")
        .done();
  }

  public static class NestedStartDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
      RuntimeService runtimeService = execution.getProcessEngine().getRuntimeService();
      runtimeService.startProcessInstanceByKey(FAILING_PROCESS, (String) null);
    }
  }

  public static class NestedLoggingDelegate implements JavaDelegate {

    public static final String LOGGER_NAME = "custom-logger";
    private final static Logger LOGGER = LoggerFactory.getLogger(LOGGER_NAME);

    @Override
    public void execute(DelegateExecution execution) throws Exception {

      RuntimeService runtimeService = execution.getProcessEngineServices().getRuntimeService();

      LOGGER.info("Before API call");
      // to reproduce CAM-12272, it is important to make an API call between the logging statements
      // (regardless if the call is meaningful)
      runtimeService.createProcessInstanceQuery().list();
      LOGGER.info("After API call");
    }

  }

  public static class FailingDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
      throw new IllegalArgumentException("I am always failing!");
    }
  }

  public static class NoneDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
      // nothing to do
    }
  }

  public static class BusinessKeyChangeDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
      execution.setProcessBusinessKey(B_KEY2);
    }
  }

  public static class FailingTaskListener implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
      throw new IllegalArgumentException("I am failing!");
    }
  }

  public static class FailingExecutionListener implements ExecutionListener {

    @Override
    public void notify(DelegateExecution execution) {
      throw new IllegalArgumentException("I am failing!");
    }
  }
}
