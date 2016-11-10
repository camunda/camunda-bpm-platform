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
package org.camunda.bpm.engine.test.bpmn.executionlistener;

import static org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.AssertionFailedError;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import static org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;

import org.camunda.bpm.engine.test.bpmn.event.conditional.SetVariableDelegate;
import org.camunda.bpm.engine.test.bpmn.executionlistener.CurrentActivityExecutionListener.CurrentActivity;
import org.camunda.bpm.engine.test.bpmn.executionlistener.RecorderExecutionListener.RecordedEvent;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Frederik Heremans
 */
public class ExecutionListenerTest {

  public ProcessEngineRule processEngineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule testHelper = new ProcessEngineTestRule(processEngineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(processEngineRule).around(testHelper);

  protected RuntimeService runtimeService;
  protected TaskService taskService;
  protected HistoryService historyService;
  protected ManagementService managementService;

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

    assertProcessEnded(processInstance.getId());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/executionlistener/ExecutionListenersStartEndEvent.bpmn20.xml"})
  public void testExecutionListenersOnStartEndEvents() {
    RecorderExecutionListener.clear();

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("executionListenersProcess");
    assertProcessEnded(processInstance.getId());

    List<RecordedEvent> recordedEvents = RecorderExecutionListener.getRecordedEvents();
    assertEquals(4, recordedEvents.size());

    assertEquals("theStart", recordedEvents.get(0).getActivityId());
    assertEquals("Start Event", recordedEvents.get(0).getActivityName());
    assertEquals("Start Event Listener", recordedEvents.get(0).getParameter());
    assertEquals("end", recordedEvents.get(0).getEventName());
    assertThat(recordedEvents.get(0).isCanceled(), is(false));

    assertEquals("noneEvent", recordedEvents.get(1).getActivityId());
    assertEquals("None Event", recordedEvents.get(1).getActivityName());
    assertEquals("Intermediate Catch Event Listener", recordedEvents.get(1).getParameter());
    assertEquals("end", recordedEvents.get(1).getEventName());
    assertThat(recordedEvents.get(1).isCanceled(), is(false));

    assertEquals("signalEvent", recordedEvents.get(2).getActivityId());
    assertEquals("Signal Event", recordedEvents.get(2).getActivityName());
    assertEquals("Intermediate Throw Event Listener", recordedEvents.get(2).getParameter());
    assertEquals("start", recordedEvents.get(2).getEventName());
    assertThat(recordedEvents.get(2).isCanceled(), is(false));

    assertEquals("theEnd", recordedEvents.get(3).getActivityId());
    assertEquals("End Event", recordedEvents.get(3).getActivityName());
    assertEquals("End Event Listener", recordedEvents.get(3).getParameter());
    assertEquals("start", recordedEvents.get(3).getEventName());
    assertThat(recordedEvents.get(3).isCanceled(), is(false));

  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/executionlistener/ExecutionListenersFieldInjectionProcess.bpmn20.xml"})
  public void testExecutionListenerFieldInjection() {
    Map<String, Object> variables = new HashMap<String, Object>();
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
    assertProcessEnded(processInstance.getId());

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

    assertProcessEnded(processInstance.getId());

    List<RecordedEvent> recordedEvents = RecorderExecutionListener.getRecordedEvents();
    assertEquals(2, recordedEvents.size());

    assertEquals("timer1", recordedEvents.get(0).getActivityId());
    assertEquals("start boundary listener", recordedEvents.get(0).getParameter());
    assertEquals("start", recordedEvents.get(0).getEventName());
    assertThat(recordedEvents.get(0).isCanceled(), is(false));

    assertEquals("timer2", recordedEvents.get(1).getActivityId());
    assertEquals("end boundary listener", recordedEvents.get(1).getParameter());
    assertEquals("end", recordedEvents.get(1).getEventName());
    assertThat(recordedEvents.get(1).isCanceled(), is(false));
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

    assertProcessEnded(processInstance.getId());

    List<RecordedEvent> recordedEvents = RecorderExecutionListener.getRecordedEvents();
    assertThat(recordedEvents, hasSize(1));

    assertEquals("UserTask_1", recordedEvents.get(0).getActivityId());
    assertEquals("end", recordedEvents.get(0).getEventName());
    assertThat(recordedEvents.get(0).isCanceled(), is(true));
  }

  private static final String MESSAGE = "cancelMessage";
  public static final BpmnModelInstance PROCESS_SERVICE_TASK_WITH_EXECUTION_START_LISTENER = Bpmn.createExecutableProcess("Process")
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
    testHelper.deploy(PROCESS_SERVICE_TASK_WITH_EXECUTION_START_LISTENER);
    runtimeService.startProcessInstanceByKey("Process");
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
    testHelper.deploy(PROCESS_SERVICE_TASK_WITH_TWO_EXECUTION_START_LISTENER);
    runtimeService.startProcessInstanceByKey("Process");
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask1").singleResult();
    taskService.complete(task.getId());

    assertEquals(0, taskService.createTaskQuery().list().size());
    List<RecordedEvent> recordedEvents = RecorderExecutionListener.getRecordedEvents();
    assertEquals(2, recordedEvents.size());
    assertEquals("sendTask", recordedEvents.get(0).getActivityId());
    assertEquals("endEvent", recordedEvents.get(1).getActivityId());
  }

  public static final BpmnModelInstance PROCESS_SERVICE_TASK_WITH_EXECUTION_START_LISTENER_AND_SUB_PROCESS = modify(Bpmn.createExecutableProcess("Process")
          .startEvent()
          .userTask("userTask")
          .serviceTask("sendTask")
            .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_START, SendMessageDelegate.class.getName())
            .camundaExecutionListenerClass(RecorderExecutionListener.EVENTNAME_START, RecorderExecutionListener.class.getName())
            .camundaExpression("${true}")
          .endEvent("endEvent")
            .camundaExecutionListenerClass(RecorderExecutionListener.EVENTNAME_START, RecorderExecutionListener.class.getName())
          .done())
          .addSubProcessTo("Process")
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
    testHelper.deploy(PROCESS_SERVICE_TASK_WITH_EXECUTION_START_LISTENER_AND_SUB_PROCESS);
    runtimeService.startProcessInstanceByKey("Process");
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

    testHelper.deploy(modelInstance);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey("conditionalProcessKey");
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());

    //when task is completed
    taskService.complete(taskQuery.singleResult().getId());

    //then end listener sets variable and triggers conditional event
    //end listener should called only once
    assertEquals(1, RecorderExecutionListener.getRecordedEvents().size());
  }
}
