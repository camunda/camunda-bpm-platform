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

package org.camunda.bpm.engine.test.bpmn.event.compensate;

import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.describeActivityInstanceTree;
import static org.camunda.bpm.engine.test.util.ExecutionAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ExecutionAssert.describeExecutionTree;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.event.EventType;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance;
import org.camunda.bpm.engine.test.bpmn.event.compensate.ReadLocalVariableListener.VariableEvent;
import org.camunda.bpm.engine.test.bpmn.event.compensate.helper.BookFlightService;
import org.camunda.bpm.engine.test.bpmn.event.compensate.helper.CancelFlightService;
import org.camunda.bpm.engine.test.bpmn.event.compensate.helper.GetVariablesDelegate;
import org.camunda.bpm.engine.test.bpmn.event.compensate.helper.SetVariablesDelegate;
import org.camunda.bpm.engine.test.util.ExecutionTree;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Assert;

/**
 * @author Daniel Meyer
 */
public class CompensateEventTest extends PluggableProcessEngineTestCase {

  public void testCompensateOrder() {
    //given two process models, only differ in order of the activities
    final String PROCESS_MODEL_WITH_REF_BEFORE = "org/camunda/bpm/engine/test/bpmn/event/compensate/compensation_reference-before.bpmn";
    final String PROCESS_MODEL_WITH_REF_AFTER = "org/camunda/bpm/engine/test/bpmn/event/compensate/compensation_reference-after.bpmn";

    //when model with ref before is deployed
    org.camunda.bpm.engine.repository.Deployment deployment1 = repositoryService.createDeployment()
            .addClasspathResource(PROCESS_MODEL_WITH_REF_BEFORE)
            .deploy();
    //then no problem will occure

    //when model with ref after is deployed
    org.camunda.bpm.engine.repository.Deployment deployment2 = repositoryService.createDeployment()
            .addClasspathResource(PROCESS_MODEL_WITH_REF_AFTER)
            .deploy();
    //then also no problem should occure

    //clean up
    repositoryService.deleteDeployment(deployment1.getId());
    repositoryService.deleteDeployment(deployment2.getId());
  }

  @Deployment
  public void testCompensateSubprocess() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

    assertEquals(5, runtimeService.getVariable(processInstance.getId(), "undoBookHotel"));

    runtimeService.signal(processInstance.getId());
    assertProcessEnded(processInstance.getId());

  }

  @Deployment
  public void testCompensateSubprocessInsideSubprocess() {
    String processInstanceId = runtimeService.startProcessInstanceByKey("compensateProcess").getId();

    completeTask("Book Hotel");
    completeTask("Book Flight");

    // throw compensation event
    completeTask("throw compensation");

    // execute compensation handlers
    completeTask("Cancel Hotel");
    completeTask("Cancel Flight");

    assertProcessEnded(processInstanceId);
  }

  @Deployment
  public void testCompensateParallelSubprocess() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

    assertEquals(5, runtimeService.getVariable(processInstance.getId(), "undoBookHotel"));

    Task singleResult = taskService.createTaskQuery().singleResult();
    taskService.complete(singleResult.getId());

    runtimeService.signal(processInstance.getId());
    assertProcessEnded(processInstance.getId());

  }

  @Deployment
  public void testCompensateParallelSubprocessCompHandlerWaitstate() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

    List<Task> compensationHandlerTasks = taskService.createTaskQuery().taskDefinitionKey("undoBookHotel").list();
    assertEquals(5, compensationHandlerTasks.size());

    ActivityInstance rootActivityInstance = runtimeService.getActivityInstance(processInstance.getId());
    List<ActivityInstance> compensationHandlerInstances = getInstancesForActivityId(rootActivityInstance, "undoBookHotel");
    assertEquals(5, compensationHandlerInstances.size());

    for (Task task : compensationHandlerTasks) {
      taskService.complete(task.getId());
    }

    Task singleResult = taskService.createTaskQuery().singleResult();
    taskService.complete(singleResult.getId());

    runtimeService.signal(processInstance.getId());
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/compensate/CompensateEventTest.testCompensateParallelSubprocessCompHandlerWaitstate.bpmn20.xml")
  public void testDeleteParallelSubprocessCompHandlerWaitstate() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

    // five inner tasks
    List<Task> compensationHandlerTasks = taskService.createTaskQuery().taskDefinitionKey("undoBookHotel").list();
    assertEquals(5, compensationHandlerTasks.size());

    // when
    runtimeService.deleteProcessInstance(processInstance.getId(), "");

    // then the process has been removed
    assertProcessEnded(processInstance.getId());
  }

  @Deployment
  public void testCompensateMiSubprocess() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

    assertEquals(5, runtimeService.getVariable(processInstance.getId(), "undoBookHotel"));

    runtimeService.signal(processInstance.getId());
    assertProcessEnded(processInstance.getId());

  }

  @Deployment
  public void testCompensateScope() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

    assertEquals(5, runtimeService.getVariable(processInstance.getId(), "undoBookHotel"));
    assertEquals(5, runtimeService.getVariable(processInstance.getId(), "undoBookFlight"));

    runtimeService.signal(processInstance.getId());
    assertProcessEnded(processInstance.getId());

  }

  // See: https://app.camunda.com/jira/browse/CAM-1410
  @Deployment
  public void testCompensateActivityRef() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

    assertEquals(5, runtimeService.getVariable(processInstance.getId(), "undoBookHotel"));
    assertNull(runtimeService.getVariable(processInstance.getId(), "undoBookFlight"));

    runtimeService.signal(processInstance.getId());
    assertProcessEnded(processInstance.getId());

  }

  /**
   * CAM-3628
   */
  @Deployment
  public void testCompensateSubprocessWithBoundaryEvent() {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("compensateProcess");

    Task compensationTask = taskService.createTaskQuery().singleResult();
    assertNotNull(compensationTask);
    assertEquals("undoSubprocess", compensationTask.getTaskDefinitionKey());

    taskService.complete(compensationTask.getId());
    runtimeService.signal(instance.getId());
    assertProcessEnded(instance.getId());
  }

  @Deployment
  public void testCompensateActivityInSubprocess() {
    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("compensateProcess");

    Task scopeTask = taskService.createTaskQuery().singleResult();
    taskService.complete(scopeTask.getId());

    // process has not yet thrown compensation
    // when throw compensation
    runtimeService.signal(instance.getId());
    // then
    Task compensationTask = taskService.createTaskQuery().singleResult();
    assertNotNull(compensationTask);
    assertEquals("undoScopeTask", compensationTask.getTaskDefinitionKey());

    taskService.complete(compensationTask.getId());
    runtimeService.signal(instance.getId());
    assertProcessEnded(instance.getId());
  }

  @Deployment
  public void testCompensateActivityInConcurrentSubprocess() {
    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("compensateProcess");

    Task scopeTask = taskService.createTaskQuery().taskDefinitionKey("scopeTask").singleResult();
    taskService.complete(scopeTask.getId());

    Task outerTask = taskService.createTaskQuery().taskDefinitionKey("outerTask").singleResult();
    taskService.complete(outerTask.getId());

    // process has not yet thrown compensation
    // when throw compensation
    runtimeService.signal(instance.getId());

    // then
    Task compensationTask = taskService.createTaskQuery().singleResult();
    assertNotNull(compensationTask);
    assertEquals("undoScopeTask", compensationTask.getTaskDefinitionKey());

    taskService.complete(compensationTask.getId());
    runtimeService.signal(instance.getId());
    assertProcessEnded(instance.getId());
  }

  @Deployment
  public void testCompensateConcurrentMiActivity() {
    String processInstanceId = runtimeService.startProcessInstanceByKey("compensateProcess").getId();

    // complete 4 of 5 user tasks
    completeTasks("Book Hotel", 4);

    // throw compensation event
    completeTaskWithVariable("Request Vacation", "accept", false);

    // should not compensate activity before multi instance activity is completed
    assertEquals(0, taskService.createTaskQuery().taskName("Cancel Hotel").count());

    // complete last open task and end process instance
    completeTask("Book Hotel");
    assertProcessEnded(processInstanceId);
  }

  @Deployment
  public void testCompensateConcurrentMiSubprocess() {
    String processInstanceId = runtimeService.startProcessInstanceByKey("compensateProcess").getId();

    // complete 4 of 5 user tasks
    completeTasks("Book Hotel", 4);

    // throw compensation event
    completeTaskWithVariable("Request Vacation", "accept", false);

    // should not compensate activity before multi instance activity is completed
    assertEquals(0, taskService.createTaskQuery().taskName("Cancel Hotel").count());

    // complete last open task and end process instance
    completeTask("Book Hotel");

    runtimeService.signal(processInstanceId);
    assertProcessEnded(processInstanceId);
  }

  @Deployment
  public void testCompensateActivityRefMiActivity() {
    String processInstanceId = runtimeService.startProcessInstanceByKey("compensateProcess").getId();

    completeTasks("Book Hotel", 5);

    // throw compensation event for activity
    completeTaskWithVariable("Request Vacation", "accept", false);

    // execute compensation handlers for each execution of the subprocess
    assertEquals(5, taskService.createTaskQuery().count());
    completeTasks("Cancel Hotel", 5);

    assertProcessEnded(processInstanceId);
  }

  @Deployment
  public void testCompensateActivityRefMiSubprocess() {
    String processInstanceId = runtimeService.startProcessInstanceByKey("compensateProcess").getId();

    completeTasks("Book Hotel", 5);

    // throw compensation event for activity
    completeTaskWithVariable("Request Vacation", "accept", false);

    // execute compensation handlers for each execution of the subprocess
    assertEquals(5, taskService.createTaskQuery().count());
    completeTasks("Cancel Hotel", 5);

    assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/event/compensate/CompensateEventTest.testCallActivityCompensationHandler.bpmn20.xml",
      "org/camunda/bpm/engine/test/bpmn/event/compensate/CompensationHandler.bpmn20.xml" })
  public void testCallActivityCompensationHandler() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

    if (!processEngineConfiguration.getHistory().equals(ProcessEngineConfiguration.HISTORY_NONE)) {
      assertEquals(5, historyService.createHistoricActivityInstanceQuery().activityId("undoBookHotel").count());
    }

    runtimeService.signal(processInstance.getId());
    assertProcessEnded(processInstance.getId());

    assertEquals(0, runtimeService.createProcessInstanceQuery().count());

    if (!processEngineConfiguration.getHistory().equals(ProcessEngineConfiguration.HISTORY_NONE)) {
      assertEquals(6, historyService.createHistoricProcessInstanceQuery().count());
    }

  }

  @Deployment
  public void testCompensateMiSubprocessVariableSnapshots() {
    // see referenced java delegates in the process definition.

    List<String> hotels = Arrays.asList("Rupert", "Vogsphere", "Milliways", "Taunton", "Ysolldins");

    SetVariablesDelegate.setValues(hotels);

    // SetVariablesDelegate take the first element of static list and set the value as local variable
    // GetVariablesDelegate read the variable and add the value to static list

    runtimeService.startProcessInstanceByKey("compensateProcess");

    if (!processEngineConfiguration.getHistory().equals(ProcessEngineConfiguration.HISTORY_NONE)) {
      assertEquals(5, historyService.createHistoricActivityInstanceQuery().activityId("undoBookHotel").count());
    }

    assertTrue(GetVariablesDelegate.values.containsAll(hotels));
  }

  @Deployment
  public void testCompensateMiSubprocessWithCompensationEventSubprocessVariableSnapshots() {
    // see referenced java delegates in the process definition.

    List<String> hotels = Arrays.asList("Rupert", "Vogsphere", "Milliways", "Taunton", "Ysolldins");

    SetVariablesDelegate.setValues(hotels);

    // SetVariablesDelegate take the first element of static list and set the value as local variable
    // GetVariablesDelegate read the variable and add the value to static list

    runtimeService.startProcessInstanceByKey("compensateProcess");

    if (!processEngineConfiguration.getHistory().equals(ProcessEngineConfiguration.HISTORY_NONE)) {
      assertEquals(5, historyService.createHistoricActivityInstanceQuery().activityId("undoBookHotel").count());
    }

    assertTrue(GetVariablesDelegate.values.containsAll(hotels));
  }

  /**
   * enable test case when bug is fixed
   *
   * @see https://app.camunda.com/jira/browse/CAM-4268
   */
  @Deployment
  public void FAILING_testCompensateMiSubprocessVariableSnapshotOfElementVariable() {
    Map<String, Object> variables = new HashMap<String, Object>();
    // multi instance collection
    List<String> flights = Arrays.asList("STS-14", "STS-28");
    variables.put("flights", flights);

    // see referenced java delegates in the process definition
    // java delegates read element variable (flight) and add the variable value
    // to a static list
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess", variables);

    if (!processEngineConfiguration.getHistory().equals(ProcessEngineConfiguration.HISTORY_NONE)) {
      assertEquals(flights.size(), historyService.createHistoricActivityInstanceQuery().activityId("undoBookFlight").count());
    }

    // java delegates should be invoked for each element in collection
    assertEquals(flights, BookFlightService.bookedFlights);
    assertEquals(flights, CancelFlightService.canceledFlights);

    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/event/compensate/CompensateEventTest.testCompensationTriggeredByEventSubProcessActivityRef.bpmn20.xml" })
  public void testCompensateActivityRefTriggeredByEventSubprocess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");
    assertProcessEnded(processInstance.getId());

    HistoricVariableInstanceQuery historicVariableInstanceQuery = historyService.createHistoricVariableInstanceQuery()
        .processInstanceId(processInstance.getId()).variableName("undoBookHotel");

    if (processEngineConfiguration.getHistoryLevel().getId() >= ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
      assertEquals(1, historicVariableInstanceQuery.count());
      assertEquals("undoBookHotel", historicVariableInstanceQuery.list().get(0).getVariableName());
      assertEquals(5, historicVariableInstanceQuery.list().get(0).getValue());

      assertEquals(0, historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).variableName("undoBookFlight").count());
    }
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/event/compensate/CompensateEventTest.testCompensationTriggeredByEventSubProcessInSubProcessActivityRef.bpmn20.xml" })
  public void testCompensateActivityRefTriggeredByEventSubprocessInSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");
    assertProcessEnded(processInstance.getId());

    HistoricVariableInstanceQuery historicVariableInstanceQuery = historyService.createHistoricVariableInstanceQuery()
        .processInstanceId(processInstance.getId()).variableName("undoBookHotel");

    if (processEngineConfiguration.getHistoryLevel().getId() >= ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
      assertEquals(1, historicVariableInstanceQuery.count());
      assertEquals("undoBookHotel", historicVariableInstanceQuery.list().get(0).getVariableName());
      assertEquals(5, historicVariableInstanceQuery.list().get(0).getValue());

      assertEquals(0, historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).variableName("undoBookFlight").count());
    }
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/event/compensate/CompensateEventTest.testCompensationInEventSubProcessActivityRef.bpmn20.xml" })
  public void testCompensateActivityRefInEventSubprocess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");
    assertProcessEnded(processInstance.getId());

    HistoricVariableInstanceQuery historicVariableInstanceQuery = historyService.createHistoricVariableInstanceQuery().variableName("undoBookSecondHotel");

    if (processEngineConfiguration.getHistoryLevel().getId() >= ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
      assertEquals(1, historicVariableInstanceQuery.count());
      assertEquals("undoBookSecondHotel", historicVariableInstanceQuery.list().get(0).getVariableName());
      assertEquals(5, historicVariableInstanceQuery.list().get(0).getValue());

      assertEquals(0, historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).variableName("undoBookFlight").count());

      assertEquals(0, historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).variableName("undoBookHotel").count());
    }
  }

  /**
   * enable test case when bug is fixed
   *
   * @see https://app.camunda.com/jira/browse/CAM-4304
   */
  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/event/compensate/CompensateEventTest.testCompensationInEventSubProcess.bpmn20.xml" })
  public void testCompensateInEventSubprocess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");
    assertProcessEnded(processInstance.getId());

    HistoricVariableInstanceQuery historicVariableInstanceQuery = historyService.createHistoricVariableInstanceQuery().variableName("undoBookSecondHotel");

    if (processEngineConfiguration.getHistoryLevel().getId() >= ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
      assertEquals(1, historicVariableInstanceQuery.count());
      assertEquals("undoBookSecondHotel", historicVariableInstanceQuery.list().get(0).getVariableName());
      assertEquals(5, historicVariableInstanceQuery.list().get(0).getValue());

      historicVariableInstanceQuery = historyService.createHistoricVariableInstanceQuery().variableName("undoBookFlight");

      assertEquals(1, historicVariableInstanceQuery.count());
      assertEquals(5, historicVariableInstanceQuery.list().get(0).getValue());

      historicVariableInstanceQuery = historyService.createHistoricVariableInstanceQuery().variableName("undoBookHotel");

      assertEquals(1, historicVariableInstanceQuery.count());
      assertEquals(5, historicVariableInstanceQuery.list().get(0).getValue());
    }
  }

  @Deployment
  public void testExecutionListeners() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("start", 0);
    variables.put("end", 0);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess", variables);

    int started = (Integer) runtimeService.getVariable(processInstance.getId(), "start");
    assertEquals(5, started);

    int ended = (Integer) runtimeService.getVariable(processInstance.getId(), "end");
    assertEquals(5, ended);

    int historyLevel = processEngineConfiguration.getHistoryLevel().getId();
    if (historyLevel > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      long finishedCount = historyService.createHistoricActivityInstanceQuery().activityId("undoBookHotel").finished().count();
      assertEquals(5, finishedCount);
    }
  }

  @Deployment
  public void testActivityInstanceTreeWithoutEventScope() {
    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = instance.getId();

    // when
    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);

    // then
    ActivityInstance tree = runtimeService.getActivityInstance(processInstanceId);
    assertThat(tree).hasStructure(
        describeActivityInstanceTree(instance.getProcessDefinitionId())
          .activity("task")
        .done());
  }

  @Deployment
  public void testConcurrentExecutionsAndPendingCompensation() {
    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = instance.getId();
    String taskId = taskService.createTaskQuery().taskDefinitionKey("innerTask").singleResult().getId();

    // when (1)
    taskService.complete(taskId);

    // then (1)
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);
    assertThat(executionTree).matches(
        describeExecutionTree(null)
        .scope()
          .child("task1").concurrent().noScope().up()
          .child("task2").concurrent().noScope().up()
          .child("subProcess").eventScope().scope().up()
        .done());

    ActivityInstance tree = runtimeService.getActivityInstance(processInstanceId);
    assertThat(tree).hasStructure(
        describeActivityInstanceTree(instance.getProcessDefinitionId())
          .activity("task1")
          .activity("task2")
        .done());

    // when (2)
    taskId = taskService.createTaskQuery().taskDefinitionKey("task1").singleResult().getId();
    taskService.complete(taskId);

    // then (2)
    executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);
    assertThat(executionTree).matches(
        describeExecutionTree("task2")
        .scope()
          .child("subProcess").eventScope().scope().up()
        .done());

    tree = runtimeService.getActivityInstance(processInstanceId);
    assertThat(tree).hasStructure(
        describeActivityInstanceTree(instance.getProcessDefinitionId())
          .activity("task2")
        .done());

    // when (3)
    taskId = taskService.createTaskQuery().taskDefinitionKey("task2").singleResult().getId();
    taskService.complete(taskId);

    // then (3)
    assertProcessEnded(processInstanceId);
  }

  @Deployment
  public void testCompensationEndEventWithScope() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

    if (!processEngineConfiguration.getHistory().equals(ProcessEngineConfiguration.HISTORY_NONE)) {
      assertEquals(5, historyService.createHistoricActivityInstanceQuery().activityId("undoBookHotel").count());
      assertEquals(5, historyService.createHistoricActivityInstanceQuery().activityId("undoBookFlight").count());
    }

    assertProcessEnded(processInstance.getId());
  }

  @Deployment
  public void testCompensationEndEventWithActivityRef() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

    if (!processEngineConfiguration.getHistory().equals(ProcessEngineConfiguration.HISTORY_NONE)) {
      assertEquals(5, historyService.createHistoricActivityInstanceQuery().activityId("undoBookHotel").count());
      assertEquals(0, historyService.createHistoricActivityInstanceQuery().activityId("undoBookFlight").count());
    }

    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/compensate/CompensateEventTest.activityWithCompensationEndEvent.bpmn20.xml")
  public void testActivityInstanceTreeForCompensationEndEvent(){
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
       describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .activity("end")
          .activity("undoBookHotel")
      .done());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/compensate/CompensateEventTest.compensationMiActivity.bpmn20.xml")
  public void testActivityInstanceTreeForMiActivity(){
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
       describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .activity("end")
          .beginMiBody("bookHotel")
            .activity("undoBookHotel")
            .activity("undoBookHotel")
            .activity("undoBookHotel")
            .activity("undoBookHotel")
            .activity("undoBookHotel")
      .done());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/compensate/CompensateEventTest.testCompensateParallelSubprocessCompHandlerWaitstate.bpmn20.xml")
  public void testActivityInstanceTreeForParallelMiActivityInSubprocess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("parallelTask")
        .activity("throwCompensate")
        .beginScope("scope")
          .beginMiBody("bookHotel")
            .activity("undoBookHotel")
            .activity("undoBookHotel")
            .activity("undoBookHotel")
            .activity("undoBookHotel")
            .activity("undoBookHotel")
        .done());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/compensate/CompensateEventTest.compensationMiSubprocess.bpmn20.xml")
  public void testActivityInstanceTreeForMiSubprocess(){
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

    completeTasks("Book Hotel", 5);
    // throw compensation event
    completeTask("throwCompensation");

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("throwingCompensation")
        .beginMiBody("scope")
          .activity("undoBookHotel")
          .activity("undoBookHotel")
          .activity("undoBookHotel")
          .activity("undoBookHotel")
          .activity("undoBookHotel")
      .done());
  }

  /**
   * CAM-4903
   */
  @Deployment
  public void FAILING_testActivityInstanceTreeForMiSubProcessDefaultHandler() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

    completeTasks("Book Hotel", 5);
    // throw compensation event
    completeTask("throwCompensation");

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("throwingCompensation")
        .beginMiBody("scope")
          .beginScope("scope")
            .activity("undoBookHotel")
          .endScope()
          .beginScope("scope")
            .activity("undoBookHotel")
          .endScope()
          .beginScope("scope")
            .activity("undoBookHotel")
          .endScope()
          .beginScope("scope")
            .activity("undoBookHotel")
          .endScope()
          .beginScope("scope")
            .activity("undoBookHotel")
      .done());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/compensate/CompensateEventTest.activityWithCompensationEndEvent.bpmn20.xml")
  public void testCancelProcessInstanceWithActiveCompensation() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

    // when
    runtimeService.deleteProcessInstance(processInstance.getId(), null);

    // then
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/event/compensate/CompensateEventTest.testCompensationEventSubProcess.bpmn20.xml" })
  public void testCompensationEventSubProcessWithScope() {
    String processInstanceId = runtimeService.startProcessInstanceByKey("bookingProcess").getId();

    completeTask("Book Flight");
    completeTask("Book Hotel");

    // throw compensation event for current scope (without activityRef)
    completeTaskWithVariable("Validate Booking", "valid", false);

    // first - compensate book flight
    assertEquals(1, taskService.createTaskQuery().count());
    completeTask("Cancel Flight");
    // second - compensate book hotel
    assertEquals(1, taskService.createTaskQuery().count());
    completeTask("Cancel Hotel");
    // third - additional compensation handler
    completeTask("Update Customer Record");

    assertProcessEnded(processInstanceId);
  }

  @Deployment
  public void testCompensationEventSubProcessWithActivityRef() {
    String processInstanceId = runtimeService.startProcessInstanceByKey("bookingProcess").getId();

    completeTask("Book Hotel");
    completeTask("Book Flight");

    // throw compensation event for specific scope (with activityRef = subprocess)
    completeTaskWithVariable("Validate Booking", "valid", false);

    // compensate the activity within this scope
    assertEquals(1, taskService.createTaskQuery().count());
    completeTask("Cancel Hotel");

    assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/event/compensate/CompensateEventTest.testCompensationEventSubProcess.bpmn20.xml" })
  public void testActivityInstanceTreeForCompensationEventSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("bookingProcess");

    completeTask("Book Flight");
    completeTask("Book Hotel");

    // throw compensation event
    completeTaskWithVariable("Validate Booking", "valid", false);

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
         describeActivityInstanceTree(processInstance.getProcessDefinitionId())
           .activity("throwCompensation")
           .beginScope("booking-subprocess")
             .activity("cancelFlight")
             .beginScope("compensationSubProcess")
               .activity("compensateFlight")
         .done());
  }

  @Deployment
  public void testCompensateMiSubprocessWithCompensationEventSubProcess() {
    Map<String, Object> variables = new HashMap<String, Object>();
    // multi instance collection
    variables.put("flights", Arrays.asList("STS-14", "STS-28"));

    String processInstanceId = runtimeService.startProcessInstanceByKey("bookingProcess", variables).getId();

    completeTask("Book Flight");
    completeTask("Book Hotel");

    completeTask("Book Flight");
    completeTask("Book Hotel");

    // throw compensation event
    completeTaskWithVariable("Validate Booking", "valid", false);

    // execute compensation handlers for each execution of the subprocess
    completeTasks("Cancel Flight", 2);
    completeTasks("Cancel Hotel", 2);
    completeTasks("Update Customer Record", 2);

    assertProcessEnded(processInstanceId);
  }

  @Deployment
  public void testCompensateParallelMiSubprocessWithCompensationEventSubProcess() {
    Map<String, Object> variables = new HashMap<String, Object>();
    // multi instance collection
    variables.put("flights", Arrays.asList("STS-14", "STS-28"));

    String processInstanceId = runtimeService.startProcessInstanceByKey("bookingProcess", variables).getId();

    completeTasks("Book Flight", 2);
    completeTasks("Book Hotel", 2);

    // throw compensation event
    completeTaskWithVariable("Validate Booking", "valid", false);

    // execute compensation handlers for each execution of the subprocess
    completeTasks("Cancel Flight", 2);
    completeTasks("Cancel Hotel", 2);
    completeTasks("Update Customer Record", 2);

    assertProcessEnded(processInstanceId);
  }

  @Deployment
  public void testCompensationEventSubprocessWithoutBoundaryEvents() {
    String processInstanceId = runtimeService.startProcessInstanceByKey("compensateProcess").getId();

    completeTask("Book Hotel");
    completeTask("Book Flight");

    // throw compensation event
    completeTask("throw compensation");

    // execute compensation handlers
    completeTask("Cancel Flight");
    completeTask("Cancel Hotel");

    assertProcessEnded(processInstanceId);
  }

  @Deployment
  public void testCompensationEventSubprocessReThrowCompensationEvent() {
    String processInstanceId = runtimeService.startProcessInstanceByKey("compensateProcess").getId();

    completeTask("Book Hotel");
    completeTask("Book Flight");

    // throw compensation event
    completeTask("throw compensation");

    // execute compensation handler and re-throw compensation event
    completeTask("Cancel Hotel");
    // execute compensation handler at subprocess
    completeTask("Cancel Flight");

    assertProcessEnded(processInstanceId);
  }

  @Deployment
  public void testCompensationEventSubprocessConsumeCompensationEvent() {
    String processInstanceId = runtimeService.startProcessInstanceByKey("compensateProcess").getId();

    completeTask("Book Hotel");
    completeTask("Book Flight");

    // throw compensation event
    completeTask("throw compensation");

    // execute compensation handler and consume compensation event
    completeTask("Cancel Hotel");
    // compensation handler at subprocess (Cancel Flight) should not be executed
    assertProcessEnded(processInstanceId);
  }

  @Deployment
  public void testSubprocessCompensationHandler() {

    // given a process instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("subProcessCompensationHandler");

    // when throwing compensation
    Task beforeCompensationTask = taskService.createTaskQuery().singleResult();
    taskService.complete(beforeCompensationTask.getId());

    // then the compensation handler has been activated
    // and the user task in the sub process can be successfully completed
    Task subProcessTask = taskService.createTaskQuery().singleResult();
    assertNotNull(subProcessTask);
    assertEquals("subProcessTask", subProcessTask.getTaskDefinitionKey());

    taskService.complete(subProcessTask.getId());

    // and the task following compensation can be successfully completed
    Task afterCompensationTask = taskService.createTaskQuery().singleResult();
    assertNotNull(afterCompensationTask);
    assertEquals("beforeEnd", afterCompensationTask.getTaskDefinitionKey());

    taskService.complete(afterCompensationTask.getId());

    // and the process has successfully ended
    assertProcessEnded(processInstance.getId());

  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/compensate/CompensateEventTest.testSubprocessCompensationHandler.bpmn20.xml")
  public void testSubprocessCompensationHandlerActivityInstanceTree() {

    // given a process instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("subProcessCompensationHandler");

    // when throwing compensation
    Task beforeCompensationTask = taskService.createTaskQuery().singleResult();
    taskService.complete(beforeCompensationTask.getId());

    // then the activity instance tree is correct
    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
       describeActivityInstanceTree(processInstance.getProcessDefinitionId())
         .activity("throwCompensate")
         .beginScope("compensationHandler")
           .activity("subProcessTask")
       .done());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/compensate/CompensateEventTest.testSubprocessCompensationHandler.bpmn20.xml")
  public void testSubprocessCompensationHandlerDeleteProcessInstance() {

    // given a process instance in compensation
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("subProcessCompensationHandler");
    Task beforeCompensationTask = taskService.createTaskQuery().singleResult();
    taskService.complete(beforeCompensationTask.getId());

    // when deleting the process instance
    runtimeService.deleteProcessInstance(processInstance.getId(), null);

    // then the process instance is ended
    assertProcessEnded(processInstance.getId());
  }

  /**
   * CAM-4387
   */
  @Deployment
  public void FAILING_testSubprocessCompensationHandlerWithEventSubprocess() {
    // given a process instance in compensation
    runtimeService.startProcessInstanceByKey("subProcessCompensationHandlerWithEventSubprocess");
    Task beforeCompensationTask = taskService.createTaskQuery().singleResult();
    taskService.complete(beforeCompensationTask.getId());

    // when the event subprocess is triggered that is defined as part of the compensation handler
    runtimeService.correlateMessage("Message");

    // then activity instance tree is correct
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    assertEquals("eventSubProcessTask", task.getTaskDefinitionKey());
  }

  /**
   * CAM-4387
   */
  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/compensate/CompensateEventTest.testSubprocessCompensationHandlerWithEventSubprocess.bpmn20.xml")
  public void FAILING_testSubprocessCompensationHandlerWithEventSubprocessActivityInstanceTree() {
    // given a process instance in compensation
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("subProcessCompensationHandlerWithEventSubprocess");
    Task beforeCompensationTask = taskService.createTaskQuery().singleResult();
    taskService.complete(beforeCompensationTask.getId());

    // when the event subprocess is triggered that is defined as part of the compensation handler
    runtimeService.correlateMessage("Message");

    // then the event subprocess has been triggered
    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .activity("throwCompensate")
          .beginScope("compensationHandler")
            .beginScope("eventSubProcess")
              .activity("eventSubProcessTask")
       .done());
  }

  /**
   * CAM-4387
   */
  @Deployment
  public void FAILING_testReceiveTaskCompensationHandler() {
    // given a process instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("receiveTaskCompensationHandler");

    // when triggering compensation
    Task beforeCompensationTask = taskService.createTaskQuery().singleResult();
    taskService.complete(beforeCompensationTask.getId());

    // then there is a message event subscription for the receive task compensation handler
    EventSubscription eventSubscription = runtimeService.createEventSubscriptionQuery().singleResult();
    assertNotNull(eventSubscription);
    assertEquals(EventType.MESSAGE, eventSubscription.getEventType());

    // and triggering the message completes compensation
    runtimeService.correlateMessage("Message");

    Task afterCompensationTask = taskService.createTaskQuery().singleResult();
    assertNotNull(afterCompensationTask);
    assertEquals("beforeEnd", afterCompensationTask.getTaskDefinitionKey());

    taskService.complete(afterCompensationTask.getId());

    // and the process has successfully ended
    assertProcessEnded(processInstance.getId());
  }

  @Deployment
  public void testConcurrentScopeCompensation() {
    // given a process instance with two concurrent tasks, one of which is waiting
    // before throwing compensation
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("concurrentScopeCompensation");
    Task beforeCompensationTask = taskService.createTaskQuery().taskDefinitionKey("beforeCompensationTask").singleResult();
    Task concurrentTask = taskService.createTaskQuery().taskDefinitionKey("concurrentTask").singleResult();

    // when throwing compensation such that two subprocesses are compensated
    taskService.complete(beforeCompensationTask.getId());

    // then both compensation handlers have been executed
    if (processEngineConfiguration.getHistoryLevel().getId() >= ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
      HistoricVariableInstanceQuery historicVariableInstanceQuery = historyService
          .createHistoricVariableInstanceQuery().variableName("compensateScope1Task");

      assertEquals(1, historicVariableInstanceQuery.count());
      assertEquals(1, historicVariableInstanceQuery.list().get(0).getValue());

      historicVariableInstanceQuery = historyService
          .createHistoricVariableInstanceQuery().variableName("compensateScope2Task");

      assertEquals(1, historicVariableInstanceQuery.count());
      assertEquals(1, historicVariableInstanceQuery.list().get(0).getValue());
    }

    // and after completing the concurrent task, the process instance ends successfully
    taskService.complete(concurrentTask.getId());
    assertProcessEnded(processInstance.getId());
  }

  @Deployment
  public void testLocalVariablesInEndExecutionListener() {
    // given
    SetLocalVariableListener setListener = new SetLocalVariableListener("foo", "bar");
    ReadLocalVariableListener readListener = new ReadLocalVariableListener("foo");

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process",
      Variables.createVariables()
        .putValue("setListener", setListener)
        .putValue("readListener", readListener));

    Task beforeCompensationTask = taskService.createTaskQuery().singleResult();

    // when executing the compensation handler
    taskService.complete(beforeCompensationTask.getId());

    // then the variable listener has been invoked and was able to read the variable on the end event
    readListener = (ReadLocalVariableListener) runtimeService.getVariable(processInstance.getId(), "readListener");

    Assert.assertEquals(1, readListener.getVariableEvents().size());

    VariableEvent event = readListener.getVariableEvents().get(0);
    Assert.assertEquals("foo", event.getVariableName());
    Assert.assertEquals("bar", event.getVariableValue());
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
  public void FAILING_testDeleteInstanceWithEventScopeExecution()
  {
    // given
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("foo")
    .startEvent("start")
    .subProcess("subProcess")
      .embeddedSubProcess()
        .startEvent("subProcessStart")
        .endEvent("subProcessEnd")
    .subProcessDone()
    .userTask("userTask")
    .done();

    modelInstance = ModifiableBpmnModelInstance.modify(modelInstance)
      .addSubProcessTo("subProcess")
      .id("eventSubProcess")
        .triggerByEvent()
        .embeddedSubProcess()
          .startEvent()
            .compensateEventDefinition()
            .compensateEventDefinitionDone()
          .endEvent()
      .done();

    deployment(modelInstance);

    long dayInMillis = 1000 * 60 * 60 * 24;
    Date date1 = new Date(10 * dayInMillis);
    ClockUtil.setCurrentTime(date1);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("foo");

    // when
    Date date2 = new Date(date1.getTime() + dayInMillis);
    ClockUtil.setCurrentTime(date2);
    runtimeService.deleteProcessInstance(processInstance.getId(), null);

    // then
    List<HistoricActivityInstance> historicActivityInstance = historyService.createHistoricActivityInstanceQuery()
        .orderByActivityId().asc().list();
    assertEquals(5, historicActivityInstance.size());

    assertEquals("start", historicActivityInstance.get(0).getActivityId());
    assertEquals(date1, historicActivityInstance.get(0).getEndTime());
    assertEquals("subProcess", historicActivityInstance.get(1).getActivityId());
    assertEquals(date1, historicActivityInstance.get(1).getEndTime());
    assertEquals("subProcessEnd", historicActivityInstance.get(2).getActivityId());
    assertEquals(date1, historicActivityInstance.get(2).getEndTime());
    assertEquals("subProcessStart", historicActivityInstance.get(3).getActivityId());
    assertEquals(date1, historicActivityInstance.get(3).getEndTime());
    assertEquals("userTask", historicActivityInstance.get(4).getActivityId());
    assertEquals(date2, historicActivityInstance.get(4).getEndTime());


  }

  private void completeTask(String taskName) {
    completeTasks(taskName, 1);
  }

  private void completeTasks(String taskName, int times) {
    List<Task> tasks = taskService.createTaskQuery().taskName(taskName).list();

    assertTrue("Actual there are " + tasks.size() + " open tasks with name '" + taskName + "'. Expected at least " + times, times <= tasks.size());

    Iterator<Task> taskIterator = tasks.iterator();
    for (int i = 0; i < times; i++) {
      Task task = taskIterator.next();
      taskService.complete(task.getId());
    }
  }

  private void completeTaskWithVariable(String taskName, String variable, Object value) {
    Task task = taskService.createTaskQuery().taskName(taskName).singleResult();
    assertNotNull("No open task with name '" + taskName + "'", task);

    Map<String, Object> variables = new HashMap<String, Object>();
    if (variable != null) {
      variables.put(variable, value);
    }

    taskService.complete(task.getId(), variables);
  }

}
