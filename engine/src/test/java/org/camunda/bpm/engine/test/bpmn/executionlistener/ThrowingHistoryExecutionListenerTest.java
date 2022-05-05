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
package org.camunda.bpm.engine.test.bpmn.executionlistener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.camunda.bpm.engine.test.bpmn.executionlistener.ThrowingHistoryEventProducer.ERROR_CODE;
import static org.camunda.bpm.engine.test.bpmn.executionlistener.ThrowingHistoryEventProducer.EXCEPTION_MESSAGE;
import static org.junit.Assert.assertEquals;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.ProcessBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
public class ThrowingHistoryExecutionListenerTest {

  protected static final String PROCESS_KEY = "Process";
  protected static final String INTERNAL_ERROR_CODE = "208";
  protected static final ThrowingHistoryEventProducer HISTORY_PRODUCER = new ThrowingHistoryEventProducer();

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(config -> {
    config.setHistoryEventProducer(HISTORY_PRODUCER);
  });
  public ProcessEngineRule processEngineRule = new ProvidedProcessEngineRule(bootstrapRule);
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(processEngineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(processEngineRule).around(testRule);

  protected RuntimeService runtimeService;
  protected TaskService taskService;
  protected HistoryService historyService;
  protected ManagementService managementService;
  protected RepositoryService repositoryService;

  @Before
  public void initServices() {
    runtimeService = processEngineRule.getRuntimeService();
    taskService = processEngineRule.getTaskService();
    historyService = processEngineRule.getHistoryService();
    managementService = processEngineRule.getManagementService();
    repositoryService = processEngineRule.getRepositoryService();
  }

  @After
  public void reset() {
    HISTORY_PRODUCER.reset();
  }

  // UNCAUGHT EXCEPTION AFTER FAILED CUSTOM END LISTENER

  @Test
  public void shouldFailForExceptionInHistoryListenerAfterBpmnErrorInEndListenerWithErrorBoundary() {
    // given
    HISTORY_PRODUCER.failsWithException().failsAtActivity("throw");
    BpmnModelInstance model = createModelWithCatchInServiceTaskAndListener(ExecutionListener.EVENTNAME_END);
    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask1").singleResult();

    // when listeners are invoked
    assertThatThrownBy(() -> taskService.complete(task.getId()))
    // then
      .isInstanceOf(RuntimeException.class)
      .hasMessage(EXCEPTION_MESSAGE);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1L);
  }

  @Test
  public void shouldFailForExceptionInHistoryListenerAfterBpmnErrorInEndListenerWithErrorBoundaryOnSubprocess() {
    // given
    HISTORY_PRODUCER.failsWithException().failsAtActivity("throw");
    BpmnModelInstance model = createModelWithCatchInSubprocessAndListener(ExecutionListener.EVENTNAME_END);
    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask1").singleResult();

    // when listeners are invoked
    assertThatThrownBy(() -> taskService.complete(task.getId()))
    // then
      .isInstanceOf(RuntimeException.class)
      .hasMessage(EXCEPTION_MESSAGE);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1L);
  }

  @Test
  public void shouldFailForExceptionInHistoryListenerAfterBpmnErrorInEndListenerWithErrorStartInEventSubprocess() {
    // given
    HISTORY_PRODUCER.failsWithException().failsAtActivity("throw");
    BpmnModelInstance model = createModelWithCatchInEventSubprocessAndListener(ExecutionListener.EVENTNAME_END);
    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask1").singleResult();

    // when listeners are invoked
    assertThatThrownBy(() -> taskService.complete(task.getId()))
    // then
      .isInstanceOf(RuntimeException.class)
      .hasMessage(EXCEPTION_MESSAGE);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1L);
  }

  // UNCAUGHT EXCEPTION AFTER FAILED CUSTOM START LISTENER

  @Test
  public void shouldFailForExceptionInHistoryListenerAfterBpmnErrorInStartListenerWithErrorBoundary() {
    // given
    HISTORY_PRODUCER.failsWithException().failsAtActivity("throw");
    BpmnModelInstance model = createModelWithCatchInServiceTaskAndListener(ExecutionListener.EVENTNAME_START);
    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask1").singleResult();

    // when listeners are invoked
    assertThatThrownBy(() -> taskService.complete(task.getId()))
    // then
      .isInstanceOf(RuntimeException.class)
      .hasMessage(EXCEPTION_MESSAGE);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1L);
  }

  @Test
  public void shouldFailForExceptionInHistoryListenerAfterBpmnErrorInStartListenerWithErrorBoundaryOnSubprocess() {
    // given
    HISTORY_PRODUCER.failsWithException().failsAtActivity("throw");
    BpmnModelInstance model = createModelWithCatchInSubprocessAndListener(ExecutionListener.EVENTNAME_START);
    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask1").singleResult();

    // when listeners are invoked
    assertThatThrownBy(() -> taskService.complete(task.getId()))
    // then
      .isInstanceOf(RuntimeException.class)
      .hasMessage(EXCEPTION_MESSAGE);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1L);
  }

  @Test
  public void shouldFailForExceptionInHistoryListenerAfterBpmnErrorInStartListenerWithErrorStartInEventSubprocess() {
    // given
    HISTORY_PRODUCER.failsWithException().failsAtActivity("throw");
    BpmnModelInstance model = createModelWithCatchInEventSubprocessAndListener(ExecutionListener.EVENTNAME_START);
    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask1").singleResult();

    // when listeners are invoked
    assertThatThrownBy(() -> taskService.complete(task.getId()))
    // then
      .isInstanceOf(RuntimeException.class)
      .hasMessage(EXCEPTION_MESSAGE);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1L);
  }

  // CAUGHT EXCEPTION AFTER FAILED CUSTOM END LISTENER
  // NOTE: it is fine to alter the result of these tests, see https://jira.camunda.com/browse/CAM-14408

  @Test
  public void shouldCatchBpmnErrorFromHistoryListenerAfterBpmnErrorInEndListenerWithErrorBoundary() {
    // given
    HISTORY_PRODUCER.failsAtActivity("throw");
    BpmnModelInstance model = createModelWithCatchInServiceTaskAndListener(ExecutionListener.EVENTNAME_END);
    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask1").singleResult();

    // when listeners are invoked
    taskService.complete(task.getId());

    // then
    verifyHistoryListenerErrorGotCaught();
    // and historic activity is still in running state since the history listener failed
    verifyActivityRunning("throw");
  }

  @Test
  public void shouldCatchBpmnErrorFromHistoryListenerAfterBpmnErrorInEndListenerWithErrorBoundaryOnSubprocess() {
    // given
    HISTORY_PRODUCER.failsAtActivity("throw");
    BpmnModelInstance model = createModelWithCatchInSubprocessAndListener(ExecutionListener.EVENTNAME_END);
    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask1").singleResult();

    // when listeners are invoked
    taskService.complete(task.getId());

    // then
    verifyHistoryListenerErrorGotCaught();
    // and historic activity is still in running state since the history listener failed
    verifyActivityRunning("throw");
  }

  @Test
  public void shouldCatchBpmnErrorFromHistoryListenerAfterBpmnErrorInEndListenerWithErrorStartInEventSubprocess() {
    // given
    HISTORY_PRODUCER.failsAtActivity("throw");
    BpmnModelInstance model = createModelWithCatchInEventSubprocessAndListener(ExecutionListener.EVENTNAME_END);
    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask1").singleResult();

    // when listeners are invoked
    taskService.complete(task.getId());

    // then
    verifyHistoryListenerErrorGotCaught();
    // and historic activity is still in running state since the history listener failed
    verifyActivityRunning("throw");
  }

  // CAUGHT EXCEPTION AFTER FAILED CUSTOM START LISTENER

  @Test
  public void shouldFailForBpmnErrorInHistoryListenerAfterBpmnErrorInStartListenerWithErrorBoundary() {
    // given
    HISTORY_PRODUCER.failsAtActivity("throw");
    BpmnModelInstance model = createModelWithCatchInServiceTaskAndListener(ExecutionListener.EVENTNAME_START);
    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask1").singleResult();

    // when listeners are invoked
    assertThatThrownBy(() -> taskService.complete(task.getId()))
    // then
      .isInstanceOf(BpmnError.class);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1L);
  }
  @Test
  public void shouldFailForBpmnErrorInHistoryListenerAfterBpmnErrorInStartListenerWithErrorBoundaryOnSubprocess() {
    // given
    HISTORY_PRODUCER.failsAtActivity("throw");
    BpmnModelInstance model = createModelWithCatchInSubprocessAndListener(ExecutionListener.EVENTNAME_START);
    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask1").singleResult();

    // when listeners are invoked
    assertThatThrownBy(() -> taskService.complete(task.getId()))
    // then
      .isInstanceOf(BpmnError.class);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1L);
  }
  @Test
  public void shouldFailForBpmnErrorInHistoryListenerAfterBpmnErrorInStartListenerWithErrorStartInEventSubprocess() {
    // given
    HISTORY_PRODUCER.failsAtActivity("throw");
    BpmnModelInstance model = createModelWithCatchInEventSubprocessAndListener(ExecutionListener.EVENTNAME_START);
    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask1").singleResult();

    // when listeners are invoked
    assertThatThrownBy(() -> taskService.complete(task.getId()))
    // then
      .isInstanceOf(BpmnError.class);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1L);
  }

  protected BpmnModelInstance createModelWithCatchInServiceTaskAndListener(String eventName) {
    return Bpmn.createExecutableProcess(PROCESS_KEY)
          .startEvent()
          .userTask("userTask1")
          .serviceTask("throw")
            .camundaExecutionListenerClass(eventName, ThrowBPMNErrorDelegate.class)
            .camundaExpression("${true}")
          .boundaryEvent("errorEvent")
            .error(INTERNAL_ERROR_CODE)
            .userTask("afterCatchInternal")
            .endEvent("endEventInternal")
          .moveToActivity("throw")
            .boundaryEvent("errorEventHistory")
            .error(ERROR_CODE)
            .userTask("afterCatchHistory")
            .endEvent("endEventHistory")
          .moveToActivity("throw")
          .userTask("afterService")
          .endEvent()
          .done();
  }

  protected BpmnModelInstance createModelWithCatchInSubprocessAndListener(String eventName) {
    return Bpmn.createExecutableProcess(PROCESS_KEY)
          .startEvent()
          .userTask("userTask1")
          .subProcess("sub")
            .embeddedSubProcess()
            .startEvent("inSub")
            .serviceTask("throw")
              .camundaExecutionListenerClass(eventName, ThrowBPMNErrorDelegate.class)
              .camundaExpression("${true}")
              .userTask("afterService")
              .endEvent()
            .subProcessDone()
          .boundaryEvent("errorEvent")
            .error(INTERNAL_ERROR_CODE)
            .userTask("afterCatch")
            .endEvent("endEvent")
          .moveToActivity("sub")
          .boundaryEvent("errorEventHistory")
            .error(ERROR_CODE)
            .userTask("afterCatchHistory")
            .endEvent("endEventHistory")
          .moveToActivity("sub")
          .userTask("afterSub")
          .endEvent()
          .done();
  }

  protected BpmnModelInstance createModelWithCatchInEventSubprocessAndListener(String eventName) {
    ProcessBuilder processBuilder = Bpmn.createExecutableProcess(PROCESS_KEY);
    BpmnModelInstance model = processBuilder
        .startEvent()
        .userTask("userTask1")
        .serviceTask("throw")
          .camundaExecutionListenerClass(eventName, ThrowBPMNErrorDelegate.class)
          .camundaExpression("${true}")
        .userTask("afterService")
        .endEvent()
        .done();
    processBuilder.eventSubProcess()
       .startEvent("errorEvent").error(INTERNAL_ERROR_CODE)
       .userTask("afterCatch")
       .endEvent();
    processBuilder.eventSubProcess()
      .startEvent("errorEventHistory").error(ERROR_CODE)
      .userTask("afterCatchHistory")
      .endEvent();
    return model;
  }

  protected void verifyHistoryListenerErrorGotCaught() {
    assertEquals(1, taskService.createTaskQuery().list().size());
    assertEquals("afterCatchHistory", taskService.createTaskQuery().singleResult().getName());
  }

  protected void verifyActivityRunning(String activityName) {
    assertThat(historyService.createHistoricActivityInstanceQuery()
        .activityName(activityName)
        .unfinished()
        .count()).isEqualTo(1);
  }

  public static class ThrowBPMNErrorDelegate implements ExecutionListener {

    @Override
    public void notify(DelegateExecution execution) throws Exception {
      throw new BpmnError(ERROR_CODE, "business error");
    }
  }

}
