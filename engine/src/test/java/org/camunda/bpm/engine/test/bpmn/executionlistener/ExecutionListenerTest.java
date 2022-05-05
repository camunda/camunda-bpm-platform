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
import static org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT;
import static org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.bpmn.event.conditional.SetVariableDelegate;
import org.camunda.bpm.engine.test.bpmn.executionlistener.CurrentActivityExecutionListener.CurrentActivity;
import org.camunda.bpm.engine.test.bpmn.executionlistener.RecorderExecutionListener.RecordedEvent;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.ProcessBuilder;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaExecutionListener;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import junit.framework.AssertionFailedError;

/**
 * @author Frederik Heremans
 */
public class ExecutionListenerTest {

  protected static final String PROCESS_KEY = "Process";

  protected static final String ERROR_CODE = "208";
  protected static final RuntimeException RUNTIME_EXCEPTION = new RuntimeException("Intended exception from delegate");

  public ProcessEngineRule processEngineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(processEngineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(processEngineRule).around(testRule);

  protected RuntimeService runtimeService;
  protected TaskService taskService;
  protected HistoryService historyService;
  protected ManagementService managementService;
  protected RepositoryService repositoryService;

  @Before
  public void clearRecorderListener() {
    RecorderExecutionListener.clear();
  }

  @Before
  public void initServices() {
    runtimeService = processEngineRule.getRuntimeService();
    taskService = processEngineRule.getTaskService();
    historyService = processEngineRule.getHistoryService();
    managementService = processEngineRule.getManagementService();
    repositoryService = processEngineRule.getRepositoryService();
  }

  @Before
  public void resetListener() {
    ThrowBPMNErrorDelegate.reset();
    ThrowRuntimeExceptionDelegate.reset();
  }

  public void assertProcessEnded(final String processInstanceId) {
    ProcessInstance processInstance = runtimeService
            .createProcessInstanceQuery()
            .processInstanceId(processInstanceId)
            .singleResult();

    if (processInstance != null) {
      throw new AssertionFailedError("Expected finished process instance '" + processInstanceId + "' but it was still in the db");
    }
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/executionlistener/ExecutionListenersProcess.bpmn20.xml"})
  public void testExecutionListenersOnAllPossibleElements() {

    // Process start executionListener will have executionListener class that sets 2 variables
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("executionListenersProcess", "businessKey123");

    String varSetInExecutionListener = (String) runtimeService.getVariable(processInstance.getId(), "variableSetInExecutionListener");
    assertNotNull(varSetInExecutionListener);
    assertEquals("firstValue", varSetInExecutionListener);

    // Check if business key was available in execution listener
    String businessKey = (String) runtimeService.getVariable(processInstance.getId(), "businessKeyInExecution");
    assertNotNull(businessKey);
    assertEquals("businessKey123", businessKey);

    // Transition take executionListener will set 2 variables
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(task);
    taskService.complete(task.getId());

    varSetInExecutionListener = (String) runtimeService.getVariable(processInstance.getId(), "variableSetInExecutionListener");

    assertNotNull(varSetInExecutionListener);
    assertEquals("secondValue", varSetInExecutionListener);

    ExampleExecutionListenerPojo myPojo = new ExampleExecutionListenerPojo();
    runtimeService.setVariable(processInstance.getId(), "myPojo", myPojo);

    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(task);
    taskService.complete(task.getId());

    // First usertask uses a method-expression as executionListener: ${myPojo.myMethod(execution.eventName)}
    ExampleExecutionListenerPojo pojoVariable = (ExampleExecutionListenerPojo) runtimeService.getVariable(processInstance.getId(), "myPojo");
    assertNotNull(pojoVariable.getReceivedEventName());
    assertEquals("end", pojoVariable.getReceivedEventName());

    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(task);
    taskService.complete(task.getId());

    testRule.assertProcessEnded(processInstance.getId());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/executionlistener/ExecutionListenersStartEndEvent.bpmn20.xml"})
  public void testExecutionListenersOnStartEndEvents() {
    RecorderExecutionListener.clear();

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("executionListenersProcess");
    testRule.assertProcessEnded(processInstance.getId());

    List<RecordedEvent> recordedEvents = RecorderExecutionListener.getRecordedEvents();
    assertEquals(4, recordedEvents.size());

    assertEquals("theStart", recordedEvents.get(0).getActivityId());
    assertEquals("Start Event", recordedEvents.get(0).getActivityName());
    assertEquals("Start Event Listener", recordedEvents.get(0).getParameter());
    assertEquals("end", recordedEvents.get(0).getEventName());
    assertThat(recordedEvents.get(0).isCanceled()).isFalse();

    assertEquals("noneEvent", recordedEvents.get(1).getActivityId());
    assertEquals("None Event", recordedEvents.get(1).getActivityName());
    assertEquals("Intermediate Catch Event Listener", recordedEvents.get(1).getParameter());
    assertEquals("end", recordedEvents.get(1).getEventName());
    assertThat(recordedEvents.get(1).isCanceled()).isFalse();

    assertEquals("signalEvent", recordedEvents.get(2).getActivityId());
    assertEquals("Signal Event", recordedEvents.get(2).getActivityName());
    assertEquals("Intermediate Throw Event Listener", recordedEvents.get(2).getParameter());
    assertEquals("start", recordedEvents.get(2).getEventName());
    assertThat(recordedEvents.get(2).isCanceled()).isFalse();

    assertEquals("theEnd", recordedEvents.get(3).getActivityId());
    assertEquals("End Event", recordedEvents.get(3).getActivityName());
    assertEquals("End Event Listener", recordedEvents.get(3).getParameter());
    assertEquals("start", recordedEvents.get(3).getEventName());
    assertThat(recordedEvents.get(3).isCanceled()).isFalse();

  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/executionlistener/ExecutionListenersFieldInjectionProcess.bpmn20.xml"})
  public void testExecutionListenerFieldInjection() {
    Map<String, Object> variables = new HashMap<>();
    variables.put("myVar", "listening!");

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("executionListenersProcess", variables);

    Object varSetByListener = runtimeService.getVariable(processInstance.getId(), "var");
    assertNotNull(varSetByListener);
    assertTrue(varSetByListener instanceof String);

    // Result is a concatenation of fixed injected field and injected expression
    assertEquals("Yes, I am listening!", varSetByListener);
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/executionlistener/ExecutionListenersCurrentActivity.bpmn20.xml"})
  public void testExecutionListenerCurrentActivity() {

    CurrentActivityExecutionListener.clear();

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("executionListenersProcess");
    testRule.assertProcessEnded(processInstance.getId());

    List<CurrentActivity> currentActivities = CurrentActivityExecutionListener.getCurrentActivities();
    assertEquals(3, currentActivities.size());

    assertEquals("theStart", currentActivities.get(0).getActivityId());
    assertEquals("Start Event", currentActivities.get(0).getActivityName());

    assertEquals("noneEvent", currentActivities.get(1).getActivityId());
    assertEquals("None Event", currentActivities.get(1).getActivityName());

    assertEquals("theEnd", currentActivities.get(2).getActivityId());
    assertEquals("End Event", currentActivities.get(2).getActivityName());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/executionlistener/ExecutionListenerTest.testOnBoundaryEvents.bpmn20.xml"})
  public void testOnBoundaryEvents() {
    RecorderExecutionListener.clear();

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    Job firstTimer = managementService.createJobQuery().timers().singleResult();

    managementService.executeJob(firstTimer.getId());

    Job secondTimer = managementService.createJobQuery().timers().singleResult();

    managementService.executeJob(secondTimer.getId());

    testRule.assertProcessEnded(processInstance.getId());

    List<RecordedEvent> recordedEvents = RecorderExecutionListener.getRecordedEvents();
    assertEquals(2, recordedEvents.size());

    assertEquals("timer1", recordedEvents.get(0).getActivityId());
    assertEquals("start boundary listener", recordedEvents.get(0).getParameter());
    assertEquals("start", recordedEvents.get(0).getEventName());
    assertThat(recordedEvents.get(0).isCanceled()).isFalse();

    assertEquals("timer2", recordedEvents.get(1).getActivityId());
    assertEquals("end boundary listener", recordedEvents.get(1).getParameter());
    assertEquals("end", recordedEvents.get(1).getEventName());
    assertThat(recordedEvents.get(1).isCanceled()).isFalse();
  }

  @Test
  @Deployment
  public void testScriptListener() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    assertTrue(processInstance.isEnded());


    if (processEngineRule.getProcessEngineConfiguration().getHistoryLevel().getId() >= HISTORYLEVEL_AUDIT) {
      HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();
      long count = query.count();
      assertEquals(5, count);

      HistoricVariableInstance variableInstance = null;
      String[] variableNames = new String[]{"start-start", "start-end", "start-take", "end-start", "end-end"};
      for (String variableName : variableNames) {
        variableInstance = query.variableName(variableName).singleResult();
        assertNotNull("Unable ot find variable with name '" + variableName + "'", variableInstance);
        assertTrue("Variable '" + variableName + "' should be set to true", (Boolean) variableInstance.getValue());
      }
    }
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/executionlistener/ExecutionListenerTest.testScriptResourceListener.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/executionlistener/executionListener.groovy"
  })
  public void testScriptResourceListener() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    assertTrue(processInstance.isEnded());

    if (processEngineRule.getProcessEngineConfiguration().getHistoryLevel().getId() >= HISTORYLEVEL_AUDIT) {
      HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();
      long count = query.count();
      assertEquals(5, count);

      HistoricVariableInstance variableInstance = null;
      String[] variableNames = new String[]{"start-start", "start-end", "start-take", "end-start", "end-end"};
      for (String variableName : variableNames) {
        variableInstance = query.variableName(variableName).singleResult();
        assertNotNull("Unable ot find variable with name '" + variableName + "'", variableInstance);
        assertTrue("Variable '" + variableName + "' should be set to true", (Boolean) variableInstance.getValue());
      }
    }
  }

  @Test
  @Deployment
  public void testExecutionListenerOnTerminateEndEvent() {
    RecorderExecutionListener.clear();

    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    List<RecordedEvent> recordedEvents = RecorderExecutionListener.getRecordedEvents();

    assertEquals(2, recordedEvents.size());

    assertEquals("start", recordedEvents.get(0).getEventName());
    assertEquals("end", recordedEvents.get(1).getEventName());

  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/executionlistener/ExecutionListenerTest.testOnCancellingBoundaryEvent.bpmn"})
  public void testOnCancellingBoundaryEvents() {
    RecorderExecutionListener.clear();

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    Job timer = managementService.createJobQuery().timers().singleResult();

    managementService.executeJob(timer.getId());

    testRule.assertProcessEnded(processInstance.getId());

    List<RecordedEvent> recordedEvents = RecorderExecutionListener.getRecordedEvents();
    assertThat(recordedEvents).hasSize(1);

    assertEquals("UserTask_1", recordedEvents.get(0).getActivityId());
    assertEquals("end", recordedEvents.get(0).getEventName());
    assertThat(recordedEvents.get(0).isCanceled()).isTrue();
  }

  private static final String MESSAGE = "cancelMessage";
  public static final BpmnModelInstance PROCESS_SERVICE_TASK_WITH_EXECUTION_START_LISTENER = Bpmn.createExecutableProcess(PROCESS_KEY)
          .startEvent()
          .parallelGateway("fork")
          .userTask("userTask1")
          .serviceTask("sendTask")
            .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_START, SendMessageDelegate.class.getName())
            .camundaExpression("${true}")
          .endEvent("endEvent")
            .camundaExecutionListenerClass(RecorderExecutionListener.EVENTNAME_START, RecorderExecutionListener.class.getName())
          .moveToLastGateway()
          .userTask("userTask2")
          .boundaryEvent("boundaryEvent")
          .message(MESSAGE)
          .endEvent("endBoundaryEvent")
          .moveToNode("userTask2")
          .endEvent()
          .done();

  @Test
  public void testServiceTaskExecutionListenerCall() {
    testRule.deploy(PROCESS_SERVICE_TASK_WITH_EXECUTION_START_LISTENER);
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask1").singleResult();
    taskService.complete(task.getId());

    assertEquals(0, taskService.createTaskQuery().list().size());
    List<RecordedEvent> recordedEvents = RecorderExecutionListener.getRecordedEvents();
    assertEquals(1, recordedEvents.size());
    assertEquals("endEvent", recordedEvents.get(0).getActivityId());
  }

  public static final BpmnModelInstance PROCESS_SERVICE_TASK_WITH_TWO_EXECUTION_START_LISTENER = modify(PROCESS_SERVICE_TASK_WITH_EXECUTION_START_LISTENER)
          .activityBuilder("sendTask")
          .camundaExecutionListenerClass(RecorderExecutionListener.EVENTNAME_START, RecorderExecutionListener.class.getName())
          .done();

  @Test
  public void testServiceTaskTwoExecutionListenerCall() {
    testRule.deploy(PROCESS_SERVICE_TASK_WITH_TWO_EXECUTION_START_LISTENER);
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask1").singleResult();
    taskService.complete(task.getId());

    assertEquals(0, taskService.createTaskQuery().list().size());
    List<RecordedEvent> recordedEvents = RecorderExecutionListener.getRecordedEvents();
    assertEquals(2, recordedEvents.size());
    assertEquals("sendTask", recordedEvents.get(0).getActivityId());
    assertEquals("endEvent", recordedEvents.get(1).getActivityId());
  }

  public static final BpmnModelInstance PROCESS_SERVICE_TASK_WITH_EXECUTION_START_LISTENER_AND_SUB_PROCESS = modify(Bpmn.createExecutableProcess(PROCESS_KEY)
          .startEvent()
          .userTask("userTask")
          .serviceTask("sendTask")
            .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_START, SendMessageDelegate.class.getName())
            .camundaExecutionListenerClass(RecorderExecutionListener.EVENTNAME_START, RecorderExecutionListener.class.getName())
            .camundaExpression("${true}")
          .endEvent("endEvent")
            .camundaExecutionListenerClass(RecorderExecutionListener.EVENTNAME_START, RecorderExecutionListener.class.getName())
          .done())
          .addSubProcessTo(PROCESS_KEY)
            .triggerByEvent()
            .embeddedSubProcess()
            .startEvent("startSubProcess")
              .interrupting(false)
              .camundaExecutionListenerClass(RecorderExecutionListener.EVENTNAME_START, RecorderExecutionListener.class.getName())
              .message(MESSAGE)
            .userTask("subProcessTask")
              .camundaExecutionListenerClass(RecorderExecutionListener.EVENTNAME_START, RecorderExecutionListener.class.getName())
            .endEvent("endSubProcess")
          .done();

  @Test
  public void testServiceTaskExecutionListenerCallAndSubProcess() {
    testRule.deploy(PROCESS_SERVICE_TASK_WITH_EXECUTION_START_LISTENER_AND_SUB_PROCESS);
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask").singleResult();
    taskService.complete(task.getId());

    assertEquals(1, taskService.createTaskQuery().list().size());

    List<RecordedEvent> recordedEvents = RecorderExecutionListener.getRecordedEvents();
    assertEquals(4, recordedEvents.size());
    assertEquals("startSubProcess", recordedEvents.get(0).getActivityId());
    assertEquals("subProcessTask", recordedEvents.get(1).getActivityId());
    assertEquals("sendTask", recordedEvents.get(2).getActivityId());
    assertEquals("endEvent", recordedEvents.get(3).getActivityId());
  }

  public static class SendMessageDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
      RuntimeService runtimeService = execution.getProcessEngineServices().getRuntimeService();
      runtimeService.correlateMessage(MESSAGE);
    }
  }

  @Test
  public void testEndExecutionListenerIsCalledOnlyOnce() {

    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("conditionalProcessKey")
      .startEvent()
      .userTask()
      .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_END, SetVariableDelegate.class.getName())
      .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_END, RecorderExecutionListener.class.getName())
      .endEvent()
      .done();

    modelInstance = modify(modelInstance)
      .addSubProcessTo("conditionalProcessKey")
      .triggerByEvent()
      .embeddedSubProcess()
      .startEvent()
      .interrupting(true)
      .conditionalEventDefinition()
      .condition("${variable == 1}")
      .conditionalEventDefinitionDone()
      .endEvent().done();

    testRule.deploy(modelInstance);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey("conditionalProcessKey");
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());

    //when task is completed
    taskService.complete(taskQuery.singleResult().getId());

    //then end listener sets variable and triggers conditional event
    //end listener should called only once
    assertEquals(1, RecorderExecutionListener.getRecordedEvents().size());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/executionlistener/ExecutionListenerTest.testMultiInstanceCancelation.bpmn20.xml")
  public void testMultiInstanceCancelationDoesNotAffectEndListener() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("MultiInstanceCancelation");
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(2).getId());

    // when
    taskService.complete(tasks.get(3).getId());

    // then
    testRule.assertProcessEnded(processInstance.getId());
    if (processEngineRule.getProcessEngineConfiguration().getHistoryLevel().getId() >= HISTORYLEVEL_AUDIT) {
      HistoricVariableInstance endVariable = historyService.createHistoricVariableInstanceQuery()
          .processInstanceId(processInstance.getId())
          .variableName("finished")
          .singleResult();
      assertNotNull(endVariable);
      assertNotNull(endVariable.getValue());
      assertTrue(Boolean.valueOf(String.valueOf(endVariable.getValue())));
    }
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/executionlistener/ExecutionListenerTest.testMultiInstanceCancelation.bpmn20.xml")
  public void testProcessInstanceCancelationNoticedInEndListener() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("MultiInstanceCancelation");
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(2).getId());

    // when
    runtimeService.deleteProcessInstance(processInstance.getId(), "myReason");

    // then
    testRule.assertProcessEnded(processInstance.getId());
    if (processEngineRule.getProcessEngineConfiguration().getHistoryLevel().getId() >= HISTORYLEVEL_AUDIT) {
      HistoricVariableInstance endVariable = historyService.createHistoricVariableInstanceQuery()
          .processInstanceId(processInstance.getId())
          .variableName("canceled")
          .singleResult();
      assertNotNull(endVariable);
      assertNotNull(endVariable.getValue());
      assertTrue(Boolean.valueOf(String.valueOf(endVariable.getValue())));
    }
  }

  @Test
  public void testThrowExceptionInStartListenerServiceTaskWithCatch() {
    // given
    BpmnModelInstance model = createModelWithCatchInServiceTaskAndListener(ExecutionListener.EVENTNAME_START, true);
    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask1").singleResult();

    // when listeners are invoked
    assertThatThrownBy(() -> taskService.complete(task.getId()))
    // then
      .isInstanceOf(RuntimeException.class)
      .hasMessage("Intended exception from delegate");
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1L);
  }

  @Test
  public void testThrowExceptionInEndListenerAndServiceTaskWithCatch() {
    // given
    BpmnModelInstance model = createModelWithCatchInServiceTaskAndListener(ExecutionListener.EVENTNAME_END, true);
    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask1").singleResult();

    // when listeners are invoked
    assertThatThrownBy(() -> taskService.complete(task.getId()))
    // then
      .isInstanceOf(RuntimeException.class)
      .hasMessage("Intended exception from delegate");
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1L);
  }

  @Test
  public void testThrowExceptionInEndListenerAndServiceTaskWithCatchException() {
    // given
    BpmnModelInstance model = createModelWithCatchInServiceTaskAndListener(ExecutionListener.EVENTNAME_END, true, true);
    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask1").singleResult();

    // when listeners are invoked
    taskService.complete(task.getId());

    // then
    verifyErrorGotCaught(true);
    verifyActivityCanceled("throw");
  }

  @Test
  public void testThrowExceptionInEndListenerAndSubprocessWithCatchException() {
    // given
    BpmnModelInstance model = createModelWithCatchInSubprocessAndListener(ExecutionListener.EVENTNAME_END, true, true);
    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask1").singleResult();

    // when listeners are invoked
    taskService.complete(task.getId());

    // then
    verifyErrorGotCaught(true);
    verifyActivityCanceled("throw");
  }

  @Test
  public void testThrowExceptionInEndListenerAndEventSubprocessWithCatchException() {
    // given
    BpmnModelInstance model = createModelWithCatchInEventSubprocessAndListener(ExecutionListener.EVENTNAME_END, true, true);
    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask1").singleResult();

    // when the listeners are invoked
    taskService.complete(task.getId());

    // then
    verifyErrorGotCaught(true);
    verifyActivityCanceled("throw");
  }

  @Test
  public void testThrowBpmnErrorInStartListenerServiceTaskWithCatch() {
    // given
    BpmnModelInstance model = createModelWithCatchInServiceTaskAndListener(ExecutionListener.EVENTNAME_START);
    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask1").singleResult();

    // when listeners are invoked
    taskService.complete(task.getId());

    // then
    verifyErrorGotCaught();
    verifyActivityCanceled("throw");
  }

  @Test
  public void testThrowBpmnErrorInStartListenerAndSubprocessWithCatch() {
    // given
    BpmnModelInstance model = createModelWithCatchInSubprocessAndListener(ExecutionListener.EVENTNAME_START);
    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask1").singleResult();

    // when listeners are invoked
    taskService.complete(task.getId());

    // then
    verifyErrorGotCaught();
    verifyActivityCanceled("throw");
  }

  @Test
  public void testThrowBpmnErrorInStartListenerAndEventSubprocessWithCatch() {
    // given
    BpmnModelInstance model = createModelWithCatchInEventSubprocessAndListener(ExecutionListener.EVENTNAME_START);
    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask1").singleResult();

    // when listeners are invoked
    taskService.complete(task.getId());

    // then
    verifyErrorGotCaught();
    verifyActivityCanceled("throw");
  }

  @Test
  public void testThrowBpmnErrorInEndListenerAndServiceTaskWithCatch() {
    // given
    BpmnModelInstance model = createModelWithCatchInServiceTaskAndListener(ExecutionListener.EVENTNAME_END);
    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask1").singleResult();

    // when listeners are invoked
    taskService.complete(task.getId());

    // then
    verifyErrorGotCaught();
    verifyActivityCanceled("throw");
  }

  @Test
  public void testThrowBpmnErrorInEndListenerAndSubprocessWithCatch() {
    // given
    BpmnModelInstance model = createModelWithCatchInSubprocessAndListener(ExecutionListener.EVENTNAME_END);
    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask1").singleResult();

    // when listeners are invoked
    taskService.complete(task.getId());

    // then
    verifyErrorGotCaught();
    verifyActivityCanceled("throw");
  }

  @Test
  public void testThrowBpmnErrorInEndListenerAndEventSubprocessWithCatch() {
    // given
    BpmnModelInstance model = createModelWithCatchInEventSubprocessAndListener(ExecutionListener.EVENTNAME_END);
    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask1").singleResult();

    // when the listeners are invoked
    taskService.complete(task.getId());

    // then
    verifyErrorGotCaught();
    verifyActivityCanceled("throw");
  }

  @Test
  public void testThrowBpmnErrorInTakeListenerAndEventSubprocessWithCatch() {
    // given
    ProcessBuilder processBuilder = Bpmn.createExecutableProcess(PROCESS_KEY);
    BpmnModelInstance model = processBuilder
        .startEvent()
        .userTask("userTask1")
        .sequenceFlowId("flow1")
        .userTask("afterListener")
        .endEvent()
        .done();

    CamundaExecutionListener listener = model.newInstance(CamundaExecutionListener.class);
    listener.setCamundaEvent(ExecutionListener.EVENTNAME_TAKE);
    listener.setCamundaClass(ThrowBPMNErrorDelegate.class.getName());
    model.<SequenceFlow>getModelElementById("flow1").builder().addExtensionElement(listener);

    processBuilder.eventSubProcess()
        .startEvent("errorEvent").error(ERROR_CODE)
        .userTask("afterCatch")
        .endEvent();

    testRule.deploy(model);

    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask1").singleResult();
    // when the listeners are invoked
    taskService.complete(task.getId());

    // then
    verifyErrorGotCaught();
  }

  @Test
  public void testThrowBpmnErrorInStartListenerOfStartEventAndEventSubprocessWithCatch() {
    // given
    ProcessBuilder processBuilder = Bpmn.createExecutableProcess(PROCESS_KEY);
    BpmnModelInstance model = processBuilder
        .startEvent()
        .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_START, ThrowBPMNErrorDelegate.class.getName())
        .userTask("afterListener")
        .endEvent()
        .done();

    processBuilder.eventSubProcess()
        .startEvent("errorEvent").error(ERROR_CODE)
        .userTask("afterCatch")
        .endEvent();

    testRule.deploy(model);
    // when the listeners are invoked
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);

    // then
    verifyErrorGotCaught();
  }

  @Test
  public void testThrowBpmnErrorInStartListenerOfStartEventAndSubprocessWithCatch() {
    // given
    BpmnModelInstance model = Bpmn.createExecutableProcess(PROCESS_KEY)
        .startEvent()
        .userTask("userTask1")
        .subProcess("sub")
          .embeddedSubProcess()
            .startEvent("inSub")
            .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_START, ThrowBPMNErrorDelegate.class.getName())
            .userTask("afterListener")
            .endEvent()
          .subProcessDone()
        .boundaryEvent("errorEvent")
        .error(ERROR_CODE)
        .userTask("afterCatch")
        .endEvent("endEvent")
        .moveToActivity("sub")
        .endEvent()
        .done();

    testRule.deploy(model);

    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask1").singleResult();
    // when the listeners are invoked
    taskService.complete(task.getId());

    // then
    verifyErrorGotCaught();
  }

  @Test
  public void testThrowBpmnErrorInEndListenerOfLastEventAndEventProcessWithCatch() {
    // given
    ProcessBuilder processBuilder = Bpmn.createExecutableProcess(PROCESS_KEY);
    BpmnModelInstance model = processBuilder
        .startEvent()
        .userTask("userTask1")
        .serviceTask("throw")
          .camundaExpression("${true}")
          .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_END, ThrowBPMNErrorDelegate.class.getName())
        .done();

    processBuilder.eventSubProcess()
        .startEvent("errorEvent").error(ERROR_CODE)
        .userTask("afterCatch")
        .endEvent();

    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask1").singleResult();
    // when the listeners are invoked
    taskService.complete(task.getId());

    // then
    Task afterCatch = taskService.createTaskQuery().singleResult();
    assertNotNull(afterCatch);
    assertEquals("afterCatch", afterCatch.getName());
    assertEquals(1, ThrowBPMNErrorDelegate.INVOCATIONS);

    // and completing this task ends the process instance
    taskService.complete(afterCatch.getId());

    assertEquals(0, runtimeService.createExecutionQuery().count());
    verifyActivityCanceled("throw");
  }

  @Test
  public void testThrowBpmnErrorInEndListenerOfLastEventAndServiceTaskWithCatch() {
    // given
    BpmnModelInstance model = Bpmn.createExecutableProcess(PROCESS_KEY)
        .startEvent()
        .userTask("userTask1")
        .serviceTask("throw")
          .camundaExpression("${true}")
          .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_END, ThrowBPMNErrorDelegate.class.getName())
        .boundaryEvent()
        .error(ERROR_CODE)
        .userTask("afterCatch")
        .endEvent()
        .done();

    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask1").singleResult();
    // when the listeners are invoked
    taskService.complete(task.getId());

    // then
    verifyErrorGotCaught();
    verifyActivityCanceled("throw");
  }

  @Test
  public void testThrowBpmnErrorInStartListenerOfLastEventAndServiceTaskWithCatch() {
    // given
    BpmnModelInstance model = Bpmn.createExecutableProcess(PROCESS_KEY)
        .startEvent()
        .userTask("userTask1")
        .serviceTask("throw")
          .camundaExpression("${true}")
          .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_START, ThrowBPMNErrorDelegate.class.getName())
        .boundaryEvent()
        .error(ERROR_CODE)
        .userTask("afterCatch")
        .endEvent()
        .done();

    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask1").singleResult();
    // when the listeners are invoked
    taskService.complete(task.getId());

    // then
    verifyErrorGotCaught();
    verifyActivityCanceled("throw");
  }

  @Test
  public void testThrowBpmnErrorInEndListenerOfLastEventAndSubprocessWithCatch() {
    // given
    BpmnModelInstance model = Bpmn.createExecutableProcess(PROCESS_KEY)
        .startEvent()
        .userTask("userTask1")
        .subProcess("sub")
          .embeddedSubProcess()
            .startEvent("inSub")
            .serviceTask("throw")
              .camundaExpression("${true}")
              .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_END, ThrowBPMNErrorDelegate.class.getName())
        .boundaryEvent()
        .error(ERROR_CODE)
        .userTask("afterCatch")
        .moveToActivity("sub")
        .userTask("afterSub")
        .endEvent()
        .done();

    testRule.deploy(model);

    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask1").singleResult();
    // when the listeners are invoked
    taskService.complete(task.getId());

    // then
    verifyErrorGotCaught();
    verifyActivityCanceled("throw");
  }

  @Test
  public void testThrowBpmnErrorInStartListenerOfLastEventAndSubprocessWithCatch() {
    // given
    BpmnModelInstance model = Bpmn.createExecutableProcess(PROCESS_KEY)
        .startEvent()
        .userTask("userTask1")
        .subProcess("sub")
          .embeddedSubProcess()
            .startEvent("inSub")
            .serviceTask("throw")
              .camundaExpression("${true}")
              .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_START, ThrowBPMNErrorDelegate.class.getName())
        .boundaryEvent()
        .error(ERROR_CODE)
        .userTask("afterCatch")
        .moveToActivity("sub")
        .userTask("afterSub")
        .endEvent()
        .done();

    testRule.deploy(model);

    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask1").singleResult();
    // when the listeners are invoked
    taskService.complete(task.getId());

    // then
    verifyErrorGotCaught();
    verifyActivityCanceled("throw");
  }

  @Test
  public void testThrowBpmnErrorInStartListenerServiceTaskAndEndListener() {
    // given
    BpmnModelInstance model = Bpmn.createExecutableProcess(PROCESS_KEY)
        .startEvent()
        .userTask("userTask1")
        .serviceTask("throw")
          .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_START, ThrowBPMNErrorDelegate.class.getName())
          .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_END, SetsVariableDelegate.class.getName())
          .camundaExpression("${true}")
        .boundaryEvent("errorEvent")
        .error(ERROR_CODE)
        .userTask("afterCatch")
        .endEvent("endEvent")
        .moveToActivity("throw")
        .userTask("afterService")
        .endEvent()
        .done();

    testRule.deploy(model);

    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask1").singleResult();
    // when the listeners are invoked
    taskService.complete(task.getId());

    // then
    verifyErrorGotCaught();
    // end listener is called
    assertEquals("bar", runtimeService.createVariableInstanceQuery().variableName("foo").singleResult().getValue());
    verifyActivityCanceled("throw");
  }

  @Test
  public void testThrowBpmnErrorInStartListenerOfStartEventAndCallActivity() {
    // given
    BpmnModelInstance subprocess = Bpmn.createExecutableProcess("subprocess")
        .startEvent()
        .userTask("userTask1")
        .serviceTask("throw")
          .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_START, ThrowBPMNErrorDelegate.class.getName())
          .camundaExpression("${true}")
        .userTask("afterService")
        .done();
    ProcessBuilder processBuilder = Bpmn.createExecutableProcess(PROCESS_KEY);
    BpmnModelInstance parent = processBuilder
        .startEvent()
        .callActivity()
          .calledElement("subprocess")
        .userTask("afterCallActivity")
        .done();

    processBuilder.eventSubProcess()
    .startEvent("errorEvent").error(ERROR_CODE)
      .userTask("afterCatch")
    .endEvent();

    testRule.deploy(parent, subprocess);

    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask1").singleResult();
    // when the listeners are invoked
    taskService.complete(task.getId());

    // then
    verifyErrorGotCaught();
    verifyActivityCanceled("throw");
  }

  @Test
  public void testThrowBpmnErrorInEndListenerInConcurrentExecutionAndEventSubprocessWithCatch() {
    // given
    ProcessBuilder processBuilder = Bpmn.createExecutableProcess(PROCESS_KEY);
    BpmnModelInstance model = processBuilder
        .startEvent()
        .parallelGateway("fork")
        .userTask("userTask1")
        .serviceTask("throw")
          .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_START, ThrowBPMNErrorDelegate.class.getName())
          .camundaExpression("${true}")
        .userTask("afterService")
        .endEvent()
        .moveToLastGateway()
        .userTask("userTask2")
        .done();
    processBuilder.eventSubProcess()
       .startEvent("errorEvent").error(ERROR_CODE)
         .userTask("afterCatch")
       .endEvent();

    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask1").singleResult();
    // when the listeners are invoked
    taskService.complete(task.getId());

    // then
    verifyErrorGotCaught();
    verifyActivityCanceled("throw");
  }

  @Test
  public void testThrowBpmnErrorInStartExpressionListenerAndEventSubprocessWithCatch() {
    // given
    processEngineRule.getProcessEngineConfiguration().getBeans().put("myListener", new ThrowBPMNErrorDelegate());

    ProcessBuilder processBuilder = Bpmn.createExecutableProcess(PROCESS_KEY);
    BpmnModelInstance model = processBuilder
        .startEvent()
        .userTask("userTask1")
        .serviceTask("throw")
          .camundaExecutionListenerExpression(ExecutionListener.EVENTNAME_START, "${myListener.notify(execution)}")
          .camundaExpression("${true}")
        .userTask("afterService")
        .endEvent()
        .done();
    processBuilder.eventSubProcess()
       .startEvent("errorEvent").error(ERROR_CODE)
         .userTask("afterCatch")
       .endEvent();

    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);

    // when listeners are invoked
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask1").singleResult();
    taskService.complete(task.getId());

    // then
    verifyErrorGotCaught();
    verifyActivityCanceled("throw");
  }

  @Test
  @Deployment
  public void testThrowBpmnErrorInEndScriptListenerAndSubprocessWithCatch() {
    // when the listeners are invoked
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);

    // then
    assertEquals(1, taskService.createTaskQuery().list().size());
    assertEquals("afterCatch", taskService.createTaskQuery().singleResult().getName());
    verifyActivityCanceled("task1");
  }

  @Test
  public void testThrowUncaughtBpmnErrorFromEndListenerShouldNotTriggerListenerAgain() {

    // given
    BpmnModelInstance model = Bpmn.createExecutableProcess(PROCESS_KEY)
        .startEvent()
        .userTask("userTask1")
        .serviceTask("throw")
          .camundaExpression("${true}")
          .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_END, ThrowBPMNErrorDelegate.class.getName())
        .endEvent()
        .done();

    testRule.deploy(model);

    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask1").singleResult();

    // when the listeners are invoked
    taskService.complete(task.getId());

    // then

    // the process has ended, because the error was not caught
    assertEquals(0, runtimeService.createExecutionQuery().count());

    // the listener was only called once
    assertEquals(1, ThrowBPMNErrorDelegate.INVOCATIONS);
    verifyActivityEnded("throw");
  }

  @Test
  public void testThrowUncaughtBpmnErrorFromStartListenerShouldNotTriggerListenerAgain() {

    // given
    BpmnModelInstance model = Bpmn.createExecutableProcess(PROCESS_KEY)
        .startEvent()
        .userTask("userTask1")
        .serviceTask("throw")
          .camundaExpression("${true}")
          .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_START, ThrowBPMNErrorDelegate.class.getName())
        .endEvent()
        .done();

    testRule.deploy(model);

    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask1").singleResult();

    // when the listeners are invoked
    taskService.complete(task.getId());

    // then

    // the process has ended, because the error was not caught
    assertEquals(0, runtimeService.createExecutionQuery().count());

    // the listener was only called once
    assertEquals(1, ThrowBPMNErrorDelegate.INVOCATIONS);
    verifyActivityEnded("throw");
  }

  @Test
  public void testThrowBpmnErrorInEndListenerMessageCorrelationShouldNotTriggerPropagation() {
    // given
    BpmnModelInstance model = Bpmn.createExecutableProcess(PROCESS_KEY)
        .startEvent()
        .userTask("userTask1")
        .subProcess("sub")
          .embeddedSubProcess()
            .startEvent("inSub")
            .userTask("taskWithListener")
            .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_END, ThrowBPMNErrorDelegate.class.getName())
            .boundaryEvent("errorEvent")
            .error(ERROR_CODE)
            .userTask("afterCatch")
            .endEvent()
          .subProcessDone()
        .boundaryEvent("message")
        .message("foo")
        .userTask("afterMessage")
        .endEvent("endEvent")
        .moveToActivity("sub")
        .endEvent()
        .done();

    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask1").singleResult();
    taskService.complete(task.getId());
    // assert
    assertEquals(1, taskService.createTaskQuery().list().size());
    assertEquals("taskWithListener", taskService.createTaskQuery().singleResult().getName());

    try {
      // when the listeners are invoked
      runtimeService.correlateMessage("foo");
      fail("Expected exception");
    } catch (Exception e) {
      // then
      assertTrue(e.getMessage().contains("business error"));
      assertEquals(1, ThrowBPMNErrorDelegate.INVOCATIONS);
    }
  }

  @Test
  public void testThrowBpmnErrorInStartListenerOnModificationShouldNotTriggerPropagation() {
    // given
    BpmnModelInstance model = Bpmn.createExecutableProcess(PROCESS_KEY)
        .startEvent()
        .userTask("userTask1")
        .subProcess("sub")
          .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_START, ThrowBPMNErrorDelegate.class.getName())
          .embeddedSubProcess()
          .startEvent("inSub")
          .serviceTask("throw")
            .camundaExpression("${true}")
          .boundaryEvent("errorEvent1")
          .error(ERROR_CODE)
          .subProcessDone()
        .boundaryEvent("errorEvent2")
        .error(ERROR_CODE)
        .userTask("afterCatch")
        .endEvent("endEvent")
        .moveToActivity("sub")
        .userTask("afterSub")
        .endEvent()
        .done();
    DeploymentWithDefinitions deployment = testRule.deploy(model);
    ProcessDefinition definition = deployment.getDeployedProcessDefinitions().get(0);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_KEY);

    // when/then
    assertThatThrownBy(() -> runtimeService.createModification(definition.getId()).startBeforeActivity("throw").processInstanceIds(processInstance.getId()).execute())
      .isInstanceOf(BpmnError.class)
      .hasMessageContaining("business error");
  }

  @Test
  public void testThrowBpmnErrorInProcessStartListenerShouldNotTriggerPropagation() {
    // given
    ProcessBuilder processBuilder = Bpmn.createExecutableProcess(PROCESS_KEY);
    BpmnModelInstance model = processBuilder
        .startEvent()
        .userTask("afterThrow")
        .endEvent()
        .done();

    processBuilder.eventSubProcess()
        .startEvent("errorEvent").error(ERROR_CODE)
        .userTask("afterCatch")
        .endEvent();

    CamundaExecutionListener listener = model.newInstance(CamundaExecutionListener.class);
    listener.setCamundaEvent(ExecutionListener.EVENTNAME_START);
    listener.setCamundaClass(ThrowBPMNErrorDelegate.class.getName());
    model.<org.camunda.bpm.model.bpmn.instance.Process>getModelElementById(PROCESS_KEY).builder().addExtensionElement(listener);

    testRule.deploy(model);

    try {
      // when listeners are invoked
      runtimeService.startProcessInstanceByKey(PROCESS_KEY);
      fail("Exception expected");
    } catch (Exception e) {
      // then
      assertTrue(e.getMessage().contains("business error"));
      assertEquals(1, ThrowBPMNErrorDelegate.INVOCATIONS);
    }
  }

  @Test
  public void testThrowBpmnErrorInProcessEndListenerShouldNotTriggerPropagation() {
    // given
    ProcessBuilder processBuilder = Bpmn.createExecutableProcess(PROCESS_KEY);
    BpmnModelInstance model = processBuilder
        .startEvent()
        .endEvent()
        .done();

    processBuilder.eventSubProcess()
        .startEvent("errorEvent").error(ERROR_CODE)
        .userTask("afterCatch")
        .endEvent();

    CamundaExecutionListener listener = model.newInstance(CamundaExecutionListener.class);
    listener.setCamundaEvent(ExecutionListener.EVENTNAME_END);
    listener.setCamundaClass(ThrowBPMNErrorDelegate.class.getName());
    model.<org.camunda.bpm.model.bpmn.instance.Process>getModelElementById(PROCESS_KEY).builder().addExtensionElement(listener);

    testRule.deploy(model);

    try {
      // when listeners are invoked
      runtimeService.startProcessInstanceByKey(PROCESS_KEY);
      fail("Exception expected");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("business error"));
      assertEquals(1, ThrowBPMNErrorDelegate.INVOCATIONS);
    }
  }

  protected BpmnModelInstance createModelWithCatchInServiceTaskAndListener(String eventName) {
    return createModelWithCatchInServiceTaskAndListener(eventName, false);
  }

  protected BpmnModelInstance createModelWithCatchInServiceTaskAndListener(String eventName, boolean throwException) {
    return createModelWithCatchInServiceTaskAndListener(eventName, throwException, false);
  }

  protected BpmnModelInstance createModelWithCatchInServiceTaskAndListener(String eventName, boolean throwException, boolean catchException) {
    return Bpmn.createExecutableProcess(PROCESS_KEY)
          .startEvent()
          .userTask("userTask1")
          .serviceTask("throw")
            .camundaExecutionListenerClass(eventName, throwException ? ThrowRuntimeExceptionDelegate.class : ThrowBPMNErrorDelegate.class)
            .camundaExpression("${true}")
          .boundaryEvent("errorEvent")
          .error(catchException ? RUNTIME_EXCEPTION.getClass().getName() : ERROR_CODE)
          .userTask("afterCatch")
          .endEvent("endEvent")
          .moveToActivity("throw")
          .userTask("afterService")
          .endEvent()
          .done();
  }

  protected BpmnModelInstance createModelWithCatchInSubprocessAndListener(String eventName) {
    return createModelWithCatchInSubprocessAndListener(eventName, false, false);
  }

  protected BpmnModelInstance createModelWithCatchInSubprocessAndListener(String eventName, boolean throwException, boolean catchException) {
    return Bpmn.createExecutableProcess(PROCESS_KEY)
          .startEvent()
          .userTask("userTask1")
          .subProcess("sub")
            .embeddedSubProcess()
            .startEvent("inSub")
            .serviceTask("throw")
              .camundaExecutionListenerClass(eventName, throwException ? ThrowRuntimeExceptionDelegate.class : ThrowBPMNErrorDelegate.class)
              .camundaExpression("${true}")
              .userTask("afterService")
              .endEvent()
            .subProcessDone()
          .boundaryEvent("errorEvent")
          .error(catchException ? RUNTIME_EXCEPTION.getClass().getName() : ERROR_CODE)
          .userTask("afterCatch")
          .endEvent("endEvent")
          .moveToActivity("sub")
          .userTask("afterSub")
          .endEvent()
          .done();
  }

  protected BpmnModelInstance createModelWithCatchInEventSubprocessAndListener(String eventName) {
    return createModelWithCatchInEventSubprocessAndListener(eventName, false, false);
  }

  protected BpmnModelInstance createModelWithCatchInEventSubprocessAndListener(String eventName, boolean throwException, boolean catchException) {
    ProcessBuilder processBuilder = Bpmn.createExecutableProcess(PROCESS_KEY);
    BpmnModelInstance model = processBuilder
        .startEvent()
        .userTask("userTask1")
        .serviceTask("throw")
          .camundaExecutionListenerClass(eventName, throwException ? ThrowRuntimeExceptionDelegate.class : ThrowBPMNErrorDelegate.class)
          .camundaExpression("${true}")
        .userTask("afterService")
        .endEvent()
        .done();
    processBuilder.eventSubProcess()
       .startEvent("errorEvent").error(catchException ? RUNTIME_EXCEPTION.getClass().getName() : ERROR_CODE)
         .userTask("afterCatch")
       .endEvent();
    return model;
  }

  protected void verifyErrorGotCaught() {
    verifyErrorGotCaught(false);
  }

  protected void verifyErrorGotCaught(boolean useExceptionDelegate) {
    assertEquals(1, taskService.createTaskQuery().list().size());
    assertEquals("afterCatch", taskService.createTaskQuery().singleResult().getName());
    assertEquals(1, useExceptionDelegate ? ThrowRuntimeExceptionDelegate.INVOCATIONS : ThrowBPMNErrorDelegate.INVOCATIONS);
  }

  protected void verifyActivityCanceled(String activityName) {
    if (processEngineRule.getProcessEngineConfiguration().getHistoryLevel().getId() >= HISTORYLEVEL_AUDIT) {
      assertThat(historyService.createHistoricActivityInstanceQuery()
          .activityName(activityName)
          .canceled()
          .count()).isEqualTo(1);
    }
  }

  protected void verifyActivityEnded(String activityName) {
    if (processEngineRule.getProcessEngineConfiguration().getHistoryLevel().getId() >= HISTORYLEVEL_AUDIT) {
      assertThat(historyService.createHistoricActivityInstanceQuery()
          .activityName(activityName)
          .completeScope()
          .count()).isEqualTo(1);
    }
  }

  public static class ThrowBPMNErrorDelegate implements ExecutionListener {

    public static int INVOCATIONS = 0;

    @Override
    public void notify(DelegateExecution execution) throws Exception {
      INVOCATIONS++;
      throw new BpmnError(ERROR_CODE, "business error");
    }

    public static void reset() {
      INVOCATIONS = 0;
    }
  }

  public static class ThrowRuntimeExceptionDelegate implements ExecutionListener {

    public static int INVOCATIONS = 0;

    @Override
    public void notify(DelegateExecution execution) throws Exception {
      INVOCATIONS++;
      throw RUNTIME_EXCEPTION;
    }

    public static void reset() {
      INVOCATIONS = 0;
    }
  }

  public static class SetsVariableDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
      execution.setVariable("foo", "bar");
    }
  }
}
