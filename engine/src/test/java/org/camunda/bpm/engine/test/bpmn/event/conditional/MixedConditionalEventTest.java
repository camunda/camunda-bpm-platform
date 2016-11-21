/*
 * Copyright 2016 camunda services GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
package org.camunda.bpm.engine.test.bpmn.event.conditional;

import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.UserTaskBuilder;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class MixedConditionalEventTest extends AbstractConditionalEventTestCase {

  protected static final String TASK_AFTER_CONDITIONAL_BOUNDARY_EVENT = "Task after conditional boundary event";
  protected static final String TASK_AFTER_CONDITIONAL_START_EVENT = "Task after conditional start event";
  protected static final String TASK_AFTER_COND_START_EVENT_IN_SUB_PROCESS = "Task after cond start event in sub process";
  protected static final String TASK_AFTER_COND_BOUN_EVENT_IN_SUB_PROCESS = "Task after cond bound event in sub process";

  protected BpmnModelInstance addBoundaryEvent(BpmnModelInstance modelInstance, String activityId, String userTaskName, boolean isInterrupting) {
    return modify(modelInstance)
      .activityBuilder(activityId)
      .boundaryEvent()
      .cancelActivity(isInterrupting)
      .conditionalEventDefinition()
      .condition(CONDITION_EXPR)
      .conditionalEventDefinitionDone()
      .userTask()
      .name(userTaskName)
      .endEvent()
      .done();
  }

  protected BpmnModelInstance addEventSubProcess(BpmnModelInstance model, String parentId, String userTaskName, boolean isInterrupting) {
    return modify(model)
      .addSubProcessTo(parentId)
      .triggerByEvent()
      .embeddedSubProcess()
      .startEvent()
      .interrupting(isInterrupting)
      .conditionalEventDefinition()
      .condition(CONDITION_EXPR)
      .conditionalEventDefinitionDone()
      .userTask()
      .name(userTaskName)
      .endEvent()
      .done();
  }

  protected void deployMixedProcess(BpmnModelInstance model, String parentId, boolean isInterrupting) {
    deployMixedProcess(model, parentId, TASK_WITH_CONDITION_ID, isInterrupting);
  }

  protected void deployMixedProcess(BpmnModelInstance model, String parentId, String activityId, boolean isInterrupting) {
    BpmnModelInstance modelInstance = addEventSubProcess(model, parentId, TASK_AFTER_CONDITIONAL_START_EVENT, isInterrupting);
    modelInstance = addBoundaryEvent(modelInstance, activityId, TASK_AFTER_CONDITIONAL_BOUNDARY_EVENT, isInterrupting);
    engine.manageDeployment(repositoryService.createDeployment().addModelInstance(CONDITIONAL_MODEL, modelInstance).deploy());
  }

  // io mapping ////////////////////////////////////////////////////////////////////////////////////////////////////////


  @Test
  public void testSetVariableOnInputMapping() {
    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .userTask(TASK_BEFORE_CONDITION_ID)
      .name(TASK_BEFORE_CONDITION)
      .userTask(TASK_WITH_CONDITION_ID)
      .camundaInputParameter(VARIABLE_NAME, "1")
      .name(TASK_WITH_CONDITION)
      .endEvent()
      .done();

    deployMixedProcess(modelInstance, CONDITIONAL_EVENT_PROCESS_KEY, true);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();

    //when task before is completed
    taskService.complete(task.getId());

    //then conditional boundary event should triggered with the default evaluation behavior
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(TASK_AFTER_CONDITIONAL_BOUNDARY_EVENT, tasksAfterVariableIsSet.get(0).getName());
  }

  @Test
  public void testSetVariableOnOutputMapping() {

    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .userTask(TASK_WITH_CONDITION_ID)
      .camundaOutputParameter(VARIABLE_NAME, "1")
      .name(TASK_WITH_CONDITION)
      .endEvent()
      .done();

    deployMixedProcess(modelInstance, CONDITIONAL_EVENT_PROCESS_KEY, true);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();

    //when task before is completed
    taskService.complete(task.getId());

    //then conditional boundary should not triggered but conditional start event
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(TASK_AFTER_CONDITIONAL_START_EVENT, tasksAfterVariableIsSet.get(0).getName());
  }


  @Test
  public void testNonInterruptingSetVariableOnInputMapping() {
    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .userTask(TASK_BEFORE_CONDITION_ID)
      .name(TASK_BEFORE_CONDITION)
      .userTask(TASK_WITH_CONDITION_ID)
      .camundaInputParameter(VARIABLE_NAME, "1")
      .name(TASK_WITH_CONDITION)
      .endEvent()
      .done();

    deployMixedProcess(modelInstance, CONDITIONAL_EVENT_PROCESS_KEY, false);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());

    //when task before is completed
    taskService.complete(taskQuery.singleResult().getId());

    //then conditional boundary event should not triggered also not conditional start event
    //since variable is only local
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(TASK_WITH_CONDITION, tasksAfterVariableIsSet.get(0).getName());
  }

  @Test
  public void testNonInterruptingSetVariableOnOutputMapping() {

    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .userTask(TASK_WITH_CONDITION_ID)
      .camundaOutputParameter(VARIABLE_NAME, "1")
      .name(TASK_WITH_CONDITION)
      .userTask().name(TASK_AFTER_OUTPUT_MAPPING)
      .endEvent()
      .done();

    deployMixedProcess(modelInstance, CONDITIONAL_EVENT_PROCESS_KEY, false);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());

    //when task before is completed
    taskService.complete(taskQuery.singleResult().getId());

    //then conditional boundary should not triggered but conditional start event
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(2, tasksAfterVariableIsSet.size());
    assertTaskNames(tasksAfterVariableIsSet,
      TASK_AFTER_CONDITIONAL_START_EVENT,
      TASK_AFTER_OUTPUT_MAPPING);
  }

  // sub process testing with event sub process and conditional start event and boundary event on user task
  // execution listener in sub process //////////////////////////////////////////////////////////////////////////////////

  @Test
  public void testSetVariableOnStartExecutionListenerInSubProcess() {
    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .subProcess(SUB_PROCESS_ID)
      .embeddedSubProcess()
      .startEvent()
      .userTask(TASK_BEFORE_CONDITION_ID)
      .name(TASK_BEFORE_CONDITION)
      .userTask(TASK_WITH_CONDITION_ID)
      .camundaExecutionListenerExpression(ExecutionListener.EVENTNAME_START, EXPR_SET_VARIABLE)
      .name(TASK_WITH_CONDITION)
      .endEvent()
      .subProcessDone()
      .endEvent()
      .done();

    deployMixedProcess(modelInstance, SUB_PROCESS_ID, true);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();

    //when task before is completed
    taskService.complete(task.getId());

    //then conditional boundary should not triggered but conditional start event
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(TASK_AFTER_CONDITIONAL_START_EVENT, tasksAfterVariableIsSet.get(0).getName());
  }

  @Test
  public void testSetVariableOnEndExecutionListenerInSubProcess() {

    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .subProcess(SUB_PROCESS_ID)
      .embeddedSubProcess()
      .startEvent()
      .userTask(TASK_WITH_CONDITION_ID)
      .camundaExecutionListenerExpression(ExecutionListener.EVENTNAME_END, EXPR_SET_VARIABLE)
      .name(TASK_WITH_CONDITION)
      .endEvent()
      .subProcessDone()
      .endEvent()
      .done();

    deployMixedProcess(modelInstance, SUB_PROCESS_ID, true);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();

    //when task before is completed
    taskService.complete(task.getId());

    //then conditional boundary should not triggered but conditional start event
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(TASK_AFTER_CONDITIONAL_START_EVENT, tasksAfterVariableIsSet.get(0).getName());
  }


  @Test
  public void testNonInterruptingSetVariableOnStartExecutionListenerInSubProcess() {
    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .subProcess(SUB_PROCESS_ID)
      .embeddedSubProcess()
      .startEvent()
      .userTask(TASK_BEFORE_CONDITION_ID)
      .name(TASK_BEFORE_CONDITION)
      .userTask(TASK_WITH_CONDITION_ID)
      .camundaExecutionListenerExpression(ExecutionListener.EVENTNAME_START, EXPR_SET_VARIABLE)
      .name(TASK_WITH_CONDITION)
      .endEvent()
      .subProcessDone()
      .endEvent()
      .done();

    deployMixedProcess(modelInstance, SUB_PROCESS_ID, false);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());

    //when task before is completed
    taskService.complete(taskQuery.singleResult().getId());

    //then conditional boundary should triggered via default evaluation behavior
    //and conditional start event via delayed events
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(3, tasksAfterVariableIsSet.size());
    assertTaskNames(tasksAfterVariableIsSet,
      TASK_AFTER_CONDITIONAL_START_EVENT,
      TASK_AFTER_CONDITIONAL_BOUNDARY_EVENT,
      TASK_WITH_CONDITION);
  }

  @Test
  public void testNonInterruptingSetVariableOnEndExecutionListenerInSubProcess() {

    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .subProcess(SUB_PROCESS_ID)
      .embeddedSubProcess()
      .startEvent()
      .userTask(TASK_WITH_CONDITION_ID)
      .camundaExecutionListenerExpression(ExecutionListener.EVENTNAME_END, EXPR_SET_VARIABLE)
      .name(TASK_WITH_CONDITION)
      .userTask().name(TASK_AFTER_OUTPUT_MAPPING)
      .endEvent()
      .subProcessDone()
      .endEvent()
      .done();

    deployMixedProcess(modelInstance, SUB_PROCESS_ID, false);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());

    //when task before is completed
    taskService.complete(taskQuery.singleResult().getId());

    //then conditional boundary should not triggered but conditional start event
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(2, tasksAfterVariableIsSet.size());
    assertTaskNames(tasksAfterVariableIsSet,
      TASK_AFTER_CONDITIONAL_START_EVENT,
      TASK_AFTER_OUTPUT_MAPPING);
  }

  // io mapping in sub process /////////////////////////////////////////////////////////////////////////////////////////


  @Test
  public void testSetVariableOnInputMappingInSubProcess() {
    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .subProcess(SUB_PROCESS_ID)
      .embeddedSubProcess()
      .startEvent()
      .userTask(TASK_BEFORE_CONDITION_ID)
      .name(TASK_BEFORE_CONDITION)
      .userTask(TASK_WITH_CONDITION_ID)
      .camundaInputParameter(VARIABLE_NAME, "1")
      .name(TASK_WITH_CONDITION)
      .endEvent()
      .subProcessDone()
      .endEvent()
      .done();

    deployMixedProcess(modelInstance, SUB_PROCESS_ID, true);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();

    //when task before is completed
    taskService.complete(task.getId());

    //then conditional boundary event should triggered via default evaluation behavior
    //but conditional start event should not
    //since variable is only local
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(TASK_AFTER_CONDITIONAL_BOUNDARY_EVENT, tasksAfterVariableIsSet.get(0).getName());
  }

  @Test
  public void testSetVariableOnOutputMappingInSubProcess() {

    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .subProcess(SUB_PROCESS_ID)
      .embeddedSubProcess()
      .startEvent()
      .userTask(TASK_WITH_CONDITION_ID)
      .camundaOutputParameter(VARIABLE_NAME, "1")
      .name(TASK_WITH_CONDITION)
      .endEvent()
      .subProcessDone()
      .endEvent()
      .done();

    deployMixedProcess(modelInstance, SUB_PROCESS_ID, true);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();

    //when task before is completed
    taskService.complete(task.getId());

    //then conditional boundary should not triggered but conditional start event
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(TASK_AFTER_CONDITIONAL_START_EVENT, tasksAfterVariableIsSet.get(0).getName());
  }


  @Test
  public void testNonInterruptingSetVariableOnInputMappingInSubProcess() {
    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .subProcess(SUB_PROCESS_ID)
      .embeddedSubProcess()
      .startEvent()
      .userTask(TASK_BEFORE_CONDITION_ID)
      .name(TASK_BEFORE_CONDITION)
      .userTask(TASK_WITH_CONDITION_ID)
      .camundaInputParameter(VARIABLE_NAME, "1")
      .name(TASK_WITH_CONDITION)
      .endEvent()
      .subProcessDone()
      .endEvent()
      .done();

    deployMixedProcess(modelInstance, SUB_PROCESS_ID, false);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());

    //when task before is completed
    taskService.complete(taskQuery.singleResult().getId());

    //then conditional boundary event should not triggered also not conditional start event
    //since variable is only local
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(TASK_WITH_CONDITION, tasksAfterVariableIsSet.get(0).getName());
  }

  @Test
  public void testNonInterruptingSetVariableOnOutputMappingInSubProcess() {

    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .subProcess(SUB_PROCESS_ID)
      .embeddedSubProcess()
      .startEvent()
      .userTask(TASK_WITH_CONDITION_ID)
      .camundaOutputParameter(VARIABLE_NAME, "1")
      .name(TASK_WITH_CONDITION)
      .userTask().name(TASK_AFTER_OUTPUT_MAPPING)
      .endEvent()
      .subProcessDone()
      .endEvent()
      .done();

    deployMixedProcess(modelInstance, SUB_PROCESS_ID, false);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());

    //when task before is completed
    taskService.complete(taskQuery.singleResult().getId());

    //then conditional boundary should not triggered but conditional start event
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(2, tasksAfterVariableIsSet.size());
    assertTaskNames(tasksAfterVariableIsSet,
      TASK_AFTER_CONDITIONAL_START_EVENT,
      TASK_AFTER_OUTPUT_MAPPING);
  }


  // sub process testing with event sub process on process instance and in sub process /////////////////////////////////
  // and conditional start event and boundary event on sub process /////////////////////////////////////////////////////
  // execution listener in sub process /////////////////////////////////////////////////////////////////////////////////

  @Test
  public void testSetVariableOnStartExecutionListenerInSubProcessWithBoundary() {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .subProcess(SUB_PROCESS_ID)
      .camundaExecutionListenerExpression(ExecutionListener.EVENTNAME_START, EXPR_SET_VARIABLE)
      .embeddedSubProcess()
      .startEvent()
      .userTask(TASK_WITH_CONDITION_ID)
      .name(TASK_WITH_CONDITION)
      .endEvent()
      .subProcessDone()
      .endEvent()
      .done();

    modelInstance = addEventSubProcess(modelInstance, SUB_PROCESS_ID, TASK_AFTER_COND_START_EVENT_IN_SUB_PROCESS, true);
    deployMixedProcess(modelInstance, CONDITIONAL_EVENT_PROCESS_KEY, SUB_PROCESS_ID, true);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());

    //when start listener sets variable

    //then conditional boundary should not triggered but conditional start event
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(TASK_AFTER_CONDITIONAL_START_EVENT, tasksAfterVariableIsSet.get(0).getName());
  }

  @Test
  public void testSetVariableOnEndExecutionListenerInSubProcessWithBoundary() {

    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .subProcess(SUB_PROCESS_ID)
      .embeddedSubProcess()
      .startEvent()
      .userTask(TASK_WITH_CONDITION_ID)
      .camundaExecutionListenerExpression(ExecutionListener.EVENTNAME_END, EXPR_SET_VARIABLE)
      .name(TASK_WITH_CONDITION)
      .endEvent()
      .subProcessDone()
      .endEvent()
      .done();

    modelInstance = addEventSubProcess(modelInstance, SUB_PROCESS_ID, TASK_AFTER_COND_START_EVENT_IN_SUB_PROCESS, true);
    deployMixedProcess(modelInstance, CONDITIONAL_EVENT_PROCESS_KEY, SUB_PROCESS_ID, true);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();

    //when task before is completed
    taskService.complete(task.getId());

    //then conditional boundary should not triggered but conditional start event
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(TASK_AFTER_CONDITIONAL_START_EVENT, tasksAfterVariableIsSet.get(0).getName());
  }

  @Test
  public void testNonInterruptingSetVariableOnStartExecutionListenerInSubProcessWithBoundary() {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .subProcess(SUB_PROCESS_ID)
      .camundaExecutionListenerExpression(ExecutionListener.EVENTNAME_START, EXPR_SET_VARIABLE)
      .embeddedSubProcess()
      .startEvent()
      .userTask(TASK_WITH_CONDITION_ID)
      .name(TASK_WITH_CONDITION)
      .endEvent()
      .subProcessDone()
      .endEvent()
      .done();

    modelInstance = addEventSubProcess(modelInstance, SUB_PROCESS_ID, TASK_AFTER_COND_START_EVENT_IN_SUB_PROCESS, false);
    deployMixedProcess(modelInstance, CONDITIONAL_EVENT_PROCESS_KEY, SUB_PROCESS_ID, false);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());

    //when start listener sets variable

    //then conditional boundary and event sub process inside the sub process should triggered via default evaluation behavior
    //and global conditional start event via delayed events
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(4, tasksAfterVariableIsSet.size());
    assertTaskNames(tasksAfterVariableIsSet,
      TASK_AFTER_CONDITIONAL_START_EVENT,
      TASK_AFTER_COND_START_EVENT_IN_SUB_PROCESS,
      TASK_AFTER_CONDITIONAL_BOUNDARY_EVENT,
      TASK_WITH_CONDITION);
  }

  @Test
  public void testNonInterruptingSetVariableOnEndExecutionListenerInSubProcessWithBoundary() {

    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .subProcess(SUB_PROCESS_ID)
      .embeddedSubProcess()
      .startEvent()
      .userTask(TASK_WITH_CONDITION_ID)
      .camundaExecutionListenerExpression(ExecutionListener.EVENTNAME_END, EXPR_SET_VARIABLE)
      .name(TASK_WITH_CONDITION)
      .userTask().name(TASK_AFTER_OUTPUT_MAPPING)
      .endEvent()
      .subProcessDone()
      .endEvent()
      .done();

    modelInstance = addEventSubProcess(modelInstance, SUB_PROCESS_ID, TASK_AFTER_COND_START_EVENT_IN_SUB_PROCESS, false);
    deployMixedProcess(modelInstance, CONDITIONAL_EVENT_PROCESS_KEY, SUB_PROCESS_ID, false);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());

    //when task before is completed
    taskService.complete(taskQuery.singleResult().getId());

    //then all conditional events are triggered
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(4, tasksAfterVariableIsSet.size());
  }

  // io mapping in sub process /////////////////////////////////////////////////////////////////////////////////////////


  @Test
  public void testSetVariableOnInputMappingInSubProcessWithBoundary() {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .subProcess(SUB_PROCESS_ID)
      .camundaInputParameter(VARIABLE_NAME, "1")
      .embeddedSubProcess()
      .startEvent()
      .userTask(TASK_WITH_CONDITION_ID)
      .name(TASK_WITH_CONDITION)
      .endEvent()
      .subProcessDone()
      .endEvent()
      .done();

    modelInstance = addEventSubProcess(modelInstance, SUB_PROCESS_ID, TASK_AFTER_COND_START_EVENT_IN_SUB_PROCESS, true);
    deployMixedProcess(modelInstance, CONDITIONAL_EVENT_PROCESS_KEY, SUB_PROCESS_ID, true);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());

    //when input mapping sets variable

    //then conditional boundary event should triggered from the default evaluation behavior
    // The event sub process inside the sub process should not since the scope is lower than from the boundary.
    // The global event sub process should not since the variable is only locally.
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(1, tasksAfterVariableIsSet.size());
    assertEquals(TASK_AFTER_CONDITIONAL_BOUNDARY_EVENT, tasksAfterVariableIsSet.get(0).getName());
  }

  @Test
  public void testSetVariableOnOutputMappingInSubProcessWithBoundary() {

    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .subProcess(SUB_PROCESS_ID)
      .embeddedSubProcess()
      .startEvent()
      .userTask(TASK_WITH_CONDITION_ID)
      .camundaOutputParameter(VARIABLE_NAME, "1")
      .name(TASK_WITH_CONDITION)
      .endEvent()
      .subProcessDone()
      .endEvent()
      .done();

    modelInstance = addEventSubProcess(modelInstance, SUB_PROCESS_ID, TASK_AFTER_COND_START_EVENT_IN_SUB_PROCESS, true);
    deployMixedProcess(modelInstance, CONDITIONAL_EVENT_PROCESS_KEY, SUB_PROCESS_ID, true);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();

    //when task before is completed
    taskService.complete(task.getId());

    //then conditional boundary should not triggered but conditional start event
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(TASK_AFTER_CONDITIONAL_START_EVENT, tasksAfterVariableIsSet.get(0).getName());
  }


  @Test
  public void testNonInterruptingSetVariableOnInputMappingInSubProcessWithBoundary() {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .subProcess(SUB_PROCESS_ID)
      .camundaInputParameter(VARIABLE_NAME, "1")
      .embeddedSubProcess()
      .startEvent()
      .userTask(TASK_WITH_CONDITION_ID)
      .name(TASK_WITH_CONDITION)
      .endEvent()
      .subProcessDone()
      .endEvent()
      .done();

    modelInstance = addEventSubProcess(modelInstance, SUB_PROCESS_ID, TASK_AFTER_COND_START_EVENT_IN_SUB_PROCESS, false);
    deployMixedProcess(modelInstance, CONDITIONAL_EVENT_PROCESS_KEY, SUB_PROCESS_ID, false);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());

    //when input mapping sets variable

    //then conditional boundary event should triggered and also conditional start event in sub process
    //via the default evaluation behavior but not the global event sub process
    //since variable is only local
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(3, tasksAfterVariableIsSet.size());
    assertTaskNames(tasksAfterVariableIsSet,
      TASK_AFTER_COND_START_EVENT_IN_SUB_PROCESS,
      TASK_AFTER_CONDITIONAL_BOUNDARY_EVENT,
      TASK_WITH_CONDITION);
  }

  @Test
  public void testNonInterruptingSetVariableOnOutputMappingInSubProcessWithBoundary() {

    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .subProcess(SUB_PROCESS_ID)
      .embeddedSubProcess()
      .startEvent()
      .userTask(TASK_WITH_CONDITION_ID)
      .camundaOutputParameter(VARIABLE_NAME, "1")
      .name(TASK_WITH_CONDITION)
      .userTask().name(TASK_AFTER_OUTPUT_MAPPING)
      .endEvent()
      .subProcessDone()
      .endEvent()
      .done();

    modelInstance = addEventSubProcess(modelInstance, SUB_PROCESS_ID, TASK_AFTER_COND_START_EVENT_IN_SUB_PROCESS, false);
    deployMixedProcess(modelInstance, CONDITIONAL_EVENT_PROCESS_KEY, SUB_PROCESS_ID, false);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());

    //when task before is completed
    taskService.complete(taskQuery.singleResult().getId());

    //then all conditional events are triggered
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(4, tasksAfterVariableIsSet.size());
  }


  //sub process with call activity and out mapping /////////////////////////////////////////////////////////////////////
  //conditional boundary on sub process and call activity //////////////////////////////////////////////////////////////
  //conditional start event event sub process on process instance level and on sub process /////////////////////////////

  @Test
  public void testSetVariableInOutMappingOfCallActivity() {
    engine.manageDeployment(repositoryService.createDeployment().addModelInstance(CONDITIONAL_MODEL, DELEGATED_PROCESS).deploy());

    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .userTask(TASK_BEFORE_CONDITION_ID)
      .name(TASK_BEFORE_CONDITION)
      .subProcess(SUB_PROCESS_ID)
      .embeddedSubProcess()
      .startEvent()
      .callActivity(TASK_WITH_CONDITION_ID)
      .calledElement(DELEGATED_PROCESS_KEY)
      .camundaOut(VARIABLE_NAME, VARIABLE_NAME)
      .userTask().name(TASK_AFTER_OUTPUT_MAPPING)
      .endEvent()
      .subProcessDone()
      .endEvent()
      .done();

    modelInstance = addEventSubProcess(modelInstance, SUB_PROCESS_ID, TASK_AFTER_COND_START_EVENT_IN_SUB_PROCESS, true);
    modelInstance = addBoundaryEvent(modelInstance, TASK_WITH_CONDITION_ID, TASK_AFTER_COND_BOUN_EVENT_IN_SUB_PROCESS, true);
    deployMixedProcess(modelInstance, CONDITIONAL_EVENT_PROCESS_KEY, SUB_PROCESS_ID, true);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());

    //when task is completed
    taskService.complete(task.getId());

    //then out mapping from call activity sets variable
    //-> interrupting conditional start event on process instance level is triggered
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(TASK_AFTER_CONDITIONAL_START_EVENT, tasksAfterVariableIsSet.get(0).getName());
  }

  @Test
  public void testNonInterruptingSetVariableInOutMappingOfCallActivity() {
    engine.manageDeployment(repositoryService.createDeployment().addModelInstance(CONDITIONAL_MODEL, DELEGATED_PROCESS).deploy());

    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .userTask(TASK_BEFORE_CONDITION_ID)
      .name(TASK_BEFORE_CONDITION)
      .subProcess(SUB_PROCESS_ID)
      .embeddedSubProcess()
      .startEvent()
      .callActivity(TASK_WITH_CONDITION_ID)
      .calledElement(DELEGATED_PROCESS_KEY)
      .camundaOut(VARIABLE_NAME, VARIABLE_NAME)
      .userTask().name(TASK_AFTER_OUTPUT_MAPPING)
      .endEvent()
      .subProcessDone()
      .endEvent()
      .done();

    modelInstance = addEventSubProcess(modelInstance, SUB_PROCESS_ID, TASK_AFTER_COND_START_EVENT_IN_SUB_PROCESS, false);
    modelInstance = addBoundaryEvent(modelInstance, TASK_WITH_CONDITION_ID, TASK_AFTER_COND_BOUN_EVENT_IN_SUB_PROCESS, false);
    deployMixedProcess(modelInstance, CONDITIONAL_EVENT_PROCESS_KEY, SUB_PROCESS_ID, false);


    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());

    //when task before service task is completed
    taskService.complete(task.getId());

    //then out mapping of call activity sets a variable
    //-> all non interrupting conditional events are triggered
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(5, tasksAfterVariableIsSet.size());
    //three subscriptions: event sub process in sub process and on process instance level and boundary event of sub process
    assertEquals(3, conditionEventSubscriptionQuery.count());
  }

  @Ignore
  @Deployment
  public void testCompensationWithConditionalEvents() {
    //given process with compensation and conditional events
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(task);
    assertEquals("Before Cancel", task.getName());

    //when task before cancel is completed
    taskService.complete(task.getId());

    //then compensation is triggered -> which triggers conditional events
    tasksAfterVariableIsSet = taskService.createTaskQuery().list();
    assertEquals(4, tasksAfterVariableIsSet.size());
  }


  @Test
  @Deployment
  public void testCompactedExecutionTree() {
    //given process with concurrent execution and conditional events
    runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    //when task before cancel is completed
    taskService.complete(taskService.createTaskQuery().taskName(TASK_BEFORE_CONDITION).singleResult().getId());

    //then conditional events are triggered
    tasksAfterVariableIsSet = taskService.createTaskQuery().list();
    assertEquals(1, tasksAfterVariableIsSet.size());
    assertEquals(TASK_AFTER_CONDITIONAL_START_EVENT, tasksAfterVariableIsSet.get(0).getName());
  }
}
