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

import java.util.Map;

import org.camunda.bpm.engine.SuspendedEntityInteractionException;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;
import static org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;

import org.junit.Test;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.engine.variable.Variables;

import static org.junit.Assert.*;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaExecutionListener;

/**
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class EventSubProcessStartConditionalEventTest extends AbstractConditionalEventTestCase {

  @Test
  @Deployment
  public void testTrueCondition() {
    //given process with event sub process conditional start event
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());
    assertEquals(1, conditionEventSubscriptionQuery.list().size());

    //when variable is set on task with condition
    taskService.setVariable(task.getId(), VARIABLE_NAME, 1);

    //then execution is at user task after conditional start event
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(TASK_AFTER_CONDITION, tasksAfterVariableIsSet.get(0).getName());
    assertEquals(0, conditionEventSubscriptionQuery.list().size());
  }

  @Test
  @Deployment
  public void testFalseCondition() {
    //given process with event sub process conditional start event
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());

    //when variable is set on task with condition
    taskService.setVariable(task.getId(), VARIABLE_NAME, 1);

    //then execution stays at user task
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(TASK_BEFORE_CONDITION, tasksAfterVariableIsSet.get(0).getName());
  }

  @Test
  @Deployment
  public void testVariableCondition() {
    //given process with event sub process conditional start event
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());

    //when variable is set on task with condition
    taskService.setVariable(task.getId(), VARIABLE_NAME, 1);

    //then execution is at user task after conditional start event
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(TASK_AFTER_CONDITION, tasksAfterVariableIsSet.get(0).getName());
  }

  @Test
  @Deployment(resources ={ "org/camunda/bpm/engine/test/bpmn/event/conditional/EventSubProcessStartConditionalEventTest.testVariableCondition.bpmn20.xml"})
  public void testWrongVariableCondition() {
    //given process with event sub process conditional start event
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());
    assertEquals(1, conditionEventSubscriptionQuery.list().size());

    //when variable is set on task with condition
    taskService.setVariable(task.getId(), VARIABLE_NAME+1, 1);

    //then execution stays at user task
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(TASK_BEFORE_CONDITION, tasksAfterVariableIsSet.get(0).getName());
    assertEquals(1, conditionEventSubscriptionQuery.list().size());
  }

  @Test
  @Deployment
  public void testNonInterruptingVariableCondition() {
    //given process with event sub process conditional start event
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());
    assertEquals(1, conditionEventSubscriptionQuery.list().size());

    //when variable is set on task with condition
    taskService.setVariable(task.getId(), VARIABLE_NAME, 1);

    //then execution is at user task after conditional start event
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(2, tasksAfterVariableIsSet.size());
    assertEquals(1, conditionEventSubscriptionQuery.list().size());
  }

  @Test
  @Deployment
  public void testSubProcessVariableCondition() {
    //given process with event sub process conditional start event and user task in sub process
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());

    //when local variable is set on task with condition
    taskService.setVariableLocal(task.getId(), VARIABLE_NAME, 1);

    //then execution stays at user task
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(TASK_BEFORE_CONDITION, tasksAfterVariableIsSet.get(0).getName());
  }

  @Test
  @Deployment(resources ={ "org/camunda/bpm/engine/test/bpmn/event/conditional/EventSubProcessStartConditionalEventTest.testSubProcessVariableCondition.bpmn20.xml"})
  public void testSubProcessSetVariableOnTaskCondition() {
    //given process with event sub process conditional start event and user task in sub process
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());

    //when variable is set on task, variable is propagated to process instance
    taskService.setVariable(task.getId(), VARIABLE_NAME, 1);

    //then execution is at user task after conditional start event
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(TASK_AFTER_CONDITION, tasksAfterVariableIsSet.get(0).getName());
  }

  @Test
  @Deployment(resources ={ "org/camunda/bpm/engine/test/bpmn/event/conditional/EventSubProcessStartConditionalEventTest.testSubProcessVariableCondition.bpmn20.xml"})
  public void testSubProcessSetVariableOnExecutionCondition() {
    //given process with event sub process conditional start event and user task in sub process
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());

    //when local variable is set on task execution
    runtimeService.setVariableLocal(task.getExecutionId(), VARIABLE_NAME, 1);

    //then execution stays at user task
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(TASK_BEFORE_CONDITION, tasksAfterVariableIsSet.get(0).getName());
  }


  protected void deployEventSubProcessWithVariableIsSetInDelegationCode(BpmnModelInstance model, boolean isInterrupting) {
    deployEventSubProcessWithVariableIsSetInDelegationCode(model, CONDITIONAL_EVENT_PROCESS_KEY, isInterrupting);
  }

  protected void deployEventSubProcessWithVariableIsSetInDelegationCode(BpmnModelInstance model, String parentId, boolean isInterrupting) {

    final BpmnModelInstance modelInstance = modify(model)
            .addSubProcessTo(parentId)
            .id("eventSubProcess")
            .triggerByEvent()
            .embeddedSubProcess()
            .startEvent()
            .interrupting(isInterrupting)
            .conditionalEventDefinition(CONDITIONAL_EVENT)
            .condition(CONDITION_EXPR)
            .conditionalEventDefinitionDone()
            .userTask("taskAfterCond")
            .name(TASK_AFTER_CONDITION)
            .endEvent().done();

    engine.manageDeployment(repositoryService.createDeployment().addModelInstance(CONDITIONAL_MODEL, modelInstance).deploy());
  }

  @Test
  public void testSetVariableInDelegate() {
    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
                                                  .startEvent().userTask().name(TASK_BEFORE_CONDITION)
                                                  .serviceTask()
                                                    .camundaClass(SetVariableDelegate.class.getName())
                                                  .endEvent().done();
    deployEventSubProcessWithVariableIsSetInDelegationCode(modelInstance, true);

    // given process with event sub process conditional start event and service task with delegate class which sets a variable
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());
    assertEquals(1, conditionEventSubscriptionQuery.list().size());

    //when task is completed
    taskService.complete(task.getId());

    //then service task with delegated code is called and variable is set
    //-> conditional event is triggered and execution stays at user task after condition
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(TASK_AFTER_CONDITION, tasksAfterVariableIsSet.get(0).getName());
    assertEquals(0, conditionEventSubscriptionQuery.list().size());
  }

  @Test
  public void testNonInterruptingSetVariableInDelegate() {
    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
                                                  .startEvent().userTask().name(TASK_BEFORE_CONDITION)
                                                  .serviceTask()
                                                    .camundaClass(SetVariableDelegate.class.getName())
                                                  .userTask()
                                                  .endEvent().done();
    deployEventSubProcessWithVariableIsSetInDelegationCode(modelInstance, false);

    // given process with event sub process conditional start event and service task with delegate class which sets a variable
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());
    assertEquals(1, conditionEventSubscriptionQuery.list().size());

    //when task before service task is completed
    taskService.complete(task.getId());

    //then service task with delegated code is called and variable is set
    //-> non interrupting conditional event is triggered
    //execution stays at user task after condition and after service task
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(2, tasksAfterVariableIsSet.size());
    assertEquals(1, conditionEventSubscriptionQuery.list().size());
  }

  @Test
  public void testSetVariableInDelegateWithSynchronousEvent() {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .userTask().name(TASK_BEFORE_CONDITION)
      .serviceTask().camundaClass(SetVariableDelegate.class.getName())
      .endEvent().done();

    modelInstance = modify(modelInstance)
      .addSubProcessTo(CONDITIONAL_EVENT_PROCESS_KEY)
      .triggerByEvent()
      .embeddedSubProcess()
      .startEvent()
      .interrupting(true)
      .conditionalEventDefinition(CONDITIONAL_EVENT)
      .condition(CONDITION_EXPR)
      .conditionalEventDefinitionDone()
      .endEvent().done();

    engine.manageDeployment(repositoryService.createDeployment().addModelInstance(CONDITIONAL_MODEL, modelInstance).deploy());

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();

    //when task is completed
    taskService.complete(task.getId());

    //then service task with delegated code is called and variable is set
    //-> conditional event is triggered and process instance ends
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(0, tasksAfterVariableIsSet.size());
    assertEquals(0, conditionEventSubscriptionQuery.list().size());
    assertNull(runtimeService.createProcessInstanceQuery().singleResult());
  }

  @Test
  public void testNonInterruptingSetVariableInDelegateWithSynchronousEvent() {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent().userTask().name(TASK_BEFORE_CONDITION)
      .serviceTask()
      .camundaClass(SetVariableDelegate.class.getName())
      .userTask()
      .endEvent().done();

    modelInstance = modify(modelInstance)
      .addSubProcessTo(CONDITIONAL_EVENT_PROCESS_KEY)
      .triggerByEvent()
      .embeddedSubProcess()
      .startEvent()
      .interrupting(false)
      .conditionalEventDefinition(CONDITIONAL_EVENT)
      .condition(CONDITION_EXPR)
      .conditionalEventDefinitionDone()
      .endEvent().done();

    engine.manageDeployment(repositoryService.createDeployment().addModelInstance(CONDITIONAL_MODEL, modelInstance).deploy());

    // given process with event sub process conditional start event and service task with delegate class which sets a variable
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());
    assertEquals(1, conditionEventSubscriptionQuery.list().size());

    //when task before service task is completed
    taskService.complete(task.getId());

    //then service task with delegated code is called and variable is set
    //-> non interrupting conditional event is triggered
    //execution stays at user task after service task
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(1, tasksAfterVariableIsSet.size());
    assertEquals(1, conditionEventSubscriptionQuery.list().size());
  }

  @Test
  public void testSetVariableInInputMapping() {
    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
                                                  .startEvent().userTask().name(TASK_BEFORE_CONDITION)
                                                  .serviceTask(TASK_WITH_CONDITION_ID)
                                                    .camundaInputParameter(VARIABLE_NAME, "1")
                                                    .camundaExpression(TRUE_CONDITION)
                                                  .userTask().name(TASK_AFTER_SERVICE_TASK)
                                                  .endEvent().done();
    deployEventSubProcessWithVariableIsSetInDelegationCode(modelInstance, true);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());

    //when task is completed
    taskService.complete(task.getId());

    //then service task with input mapping is called and variable is set
    //-> interrupting conditional event is not triggered
    //since variable is only locally
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(TASK_AFTER_SERVICE_TASK, tasksAfterVariableIsSet.get(0).getName());
  }

  @Test
  public void testNonInterruptingSetVariableInInputMapping() {
    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
                                                  .startEvent().userTask().name(TASK_BEFORE_CONDITION)
                                                  .serviceTask(TASK_WITH_CONDITION_ID)
                                                    .camundaInputParameter(VARIABLE_NAME, "1")
                                                    .camundaExpression(TRUE_CONDITION)
                                                  .userTask().name(TASK_AFTER_SERVICE_TASK)
                                                  .endEvent().done();
    deployEventSubProcessWithVariableIsSetInDelegationCode(modelInstance, false);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());

    //when task before service task is completed
    taskService.complete(task.getId());

    //then service task with input mapping is called and variable is set
    //-> non interrupting conditional event is not triggered
    //since variable is only locally
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(TASK_AFTER_SERVICE_TASK, tasksAfterVariableIsSet.get(0).getName());
  }

  @Test
  public void testSetVariableInExpression() {
    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
                                                  .startEvent().userTask().name(TASK_BEFORE_CONDITION)
                                                  .serviceTask(TASK_WITH_CONDITION_ID)
                                                    .camundaExpression("${execution.setVariable(\"variable\", 1)}")
                                                  .userTask().name(TASK_AFTER_SERVICE_TASK)
                                                  .endEvent().done();
    deployEventSubProcessWithVariableIsSetInDelegationCode(modelInstance, true);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());

    //when task is completed
    taskService.complete(task.getId());

    //then service task with expression is called and variable is set
    //-> interrupting conditional event is triggered
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(TASK_AFTER_CONDITION, tasksAfterVariableIsSet.get(0).getName());
  }

  @Test
  public void testNonInterruptingSetVariableInExpression() {
    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
                                                  .startEvent().userTask().name(TASK_BEFORE_CONDITION)
                                                  .serviceTask(TASK_WITH_CONDITION_ID)
                                                    .camundaExpression("${execution.setVariable(\"variable\", 1)}")
                                                  .userTask().name(TASK_AFTER_SERVICE_TASK)
                                                  .endEvent().done();
    deployEventSubProcessWithVariableIsSetInDelegationCode(modelInstance, false);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());

    //when task before service task is completed
    taskService.complete(task.getId());

    //then service task with expression is called and variable is set
    //-> non interrupting conditional event is triggered
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(2, tasksAfterVariableIsSet.size());
    assertEquals(1, conditionEventSubscriptionQuery.list().size());
  }

  @Test
  public void testSetVariableInInputMappingOfSubProcess() {
    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
                                                  .startEvent().userTask().name(TASK_BEFORE_CONDITION)
                                                  .subProcess(SUB_PROCESS_ID)
                                                    .camundaInputParameter(VARIABLE_NAME, "1")
                                                    .embeddedSubProcess()
                                                    .startEvent("startSubProcess")
                                                    .userTask().name(TASK_IN_SUB_PROCESS_ID)
                                                    .endEvent()
                                                  .subProcessDone()
                                                  .endEvent()
                                                  .done();
    deployEventSubProcessWithVariableIsSetInDelegationCode(modelInstance, SUB_PROCESS_ID, true);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());

    //when task is completed
    taskService.complete(task.getId());

    //then input mapping from sub process sets variable
    //-> interrupting conditional event is not triggered
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(TASK_IN_SUB_PROCESS_ID, tasksAfterVariableIsSet.get(0).getName());
  }

  @Test
  public void testNonInterruptingSetVariableInInputMappingOfSubProcess() {
    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
                                                  .startEvent().userTask().name(TASK_BEFORE_CONDITION)
                                                  .subProcess(SUB_PROCESS_ID)
                                                    .camundaInputParameter(VARIABLE_NAME, "1")
                                                    .embeddedSubProcess()
                                                    .startEvent()
                                                    .userTask().name(TASK_IN_SUB_PROCESS_ID)
                                                    .endEvent()
                                                  .subProcessDone()
                                                  .endEvent()
                                                  .done();
    deployEventSubProcessWithVariableIsSetInDelegationCode(modelInstance, SUB_PROCESS_ID, false);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());

    //when task before service task is completed
    taskService.complete(task.getId());

    //then input mapping from sub process sets variable
    //-> non interrupting conditional event is not triggered
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(1, tasksAfterVariableIsSet.size());
    assertEquals(1, conditionEventSubscriptionQuery.list().size());
  }

  @Test
  public void testSetVariableInOutputMapping() {
    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
                                                  .startEvent()
                                                  .userTask(TASK_BEFORE_CONDITION_ID)
                                                    .name(TASK_BEFORE_CONDITION)
                                                    .camundaOutputParameter(VARIABLE_NAME, "1")
                                                  .userTask()
                                                  .endEvent()
                                                  .done();
    deployEventSubProcessWithVariableIsSetInDelegationCode(modelInstance, true);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());

    //when task is completed
    taskService.complete(task.getId());

    //then output mapping from user task sets variable
    //-> interrupting conditional event is triggered
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(TASK_AFTER_CONDITION, tasksAfterVariableIsSet.get(0).getName());
  }

  @Test
  public void testNonInterruptingSetVariableInOutputMapping() {
    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
                                                  .startEvent()
                                                  .userTask(TASK_BEFORE_CONDITION_ID)
                                                    .name(TASK_BEFORE_CONDITION)
                                                    .camundaOutputParameter(VARIABLE_NAME, "1")
                                                  .userTask()
                                                  .endEvent()
                                                  .done();
    deployEventSubProcessWithVariableIsSetInDelegationCode(modelInstance, false);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());

    //when task is completed
    taskService.complete(task.getId());

    //then output mapping from user task sets variable
    //-> non interrupting conditional event is triggered
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(2, tasksAfterVariableIsSet.size());
    assertEquals(1, conditionEventSubscriptionQuery.list().size());
  }

  @Test
  public void testSetVariableInOutputMappingOfCallActivity() {
    engine.manageDeployment(repositoryService.createDeployment().addModelInstance(CONDITIONAL_MODEL, DELEGATED_PROCESS).deploy());

    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
                                                  .startEvent()
                                                  .userTask(TASK_BEFORE_CONDITION_ID)
                                                    .name(TASK_BEFORE_CONDITION)
                                                  .callActivity(TASK_WITH_CONDITION_ID)
                                                    .calledElement(DELEGATED_PROCESS_KEY)
                                                    .camundaOutputParameter(VARIABLE_NAME, "1")
                                                  .userTask()
                                                  .endEvent()
                                                  .done();
    deployEventSubProcessWithVariableIsSetInDelegationCode(modelInstance, true);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());

    //when task is completed
    taskService.complete(task.getId());

    //then output mapping from call activity sets variable
    //-> interrupting conditional event is triggered
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(TASK_AFTER_CONDITION, tasksAfterVariableIsSet.get(0).getName());
  }

  @Test
  public void testNonInterruptingSetVariableInOutputMappingOfCallActivity() {
    engine.manageDeployment(repositoryService.createDeployment().addModelInstance(CONDITIONAL_MODEL, DELEGATED_PROCESS).deploy());

    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
                                                  .startEvent()
                                                  .userTask(TASK_BEFORE_CONDITION_ID)
                                                    .name(TASK_BEFORE_CONDITION)
                                                  .callActivity(TASK_WITH_CONDITION_ID)
                                                    .calledElement(DELEGATED_PROCESS_KEY)
                                                    .camundaOutputParameter(VARIABLE_NAME, "1")
                                                  .userTask()
                                                  .endEvent()
                                                  .done();
    deployEventSubProcessWithVariableIsSetInDelegationCode(modelInstance, false);


    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());

    //when task is completed
    taskService.complete(task.getId());

    //then output mapping from call activity sets variable
    //-> non interrupting conditional event is triggered
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(2, tasksAfterVariableIsSet.size());
    assertEquals(1, conditionEventSubscriptionQuery.list().size());
  }

  @Test
  public void testSetVariableInOutMappingOfCallActivity() {
    engine.manageDeployment(repositoryService.createDeployment().addModelInstance(CONDITIONAL_MODEL, DELEGATED_PROCESS).deploy());

    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .userTask(TASK_BEFORE_CONDITION_ID)
      .name(TASK_BEFORE_CONDITION)
      .callActivity(TASK_WITH_CONDITION_ID)
      .calledElement(DELEGATED_PROCESS_KEY)
      .camundaOut(VARIABLE_NAME, VARIABLE_NAME)
      .userTask().name(TASK_AFTER_OUTPUT_MAPPING)
      .endEvent()
      .done();
    deployEventSubProcessWithVariableIsSetInDelegationCode(modelInstance, true);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());

    //when task is completed
    taskService.complete(task.getId());

    //then out mapping from call activity sets variable
    //-> interrupting conditional event is triggered
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(TASK_AFTER_CONDITION, tasksAfterVariableIsSet.get(0).getName());
  }

  @Test
  public void testNonInterruptingSetVariableInOutMappingOfCallActivity() {
    engine.manageDeployment(repositoryService.createDeployment().addModelInstance(CONDITIONAL_MODEL, DELEGATED_PROCESS).deploy());

    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .userTask(TASK_BEFORE_CONDITION_ID)
      .name(TASK_BEFORE_CONDITION)
      .callActivity(TASK_WITH_CONDITION_ID)
      .calledElement(DELEGATED_PROCESS_KEY)
      .camundaOut(VARIABLE_NAME, VARIABLE_NAME)
      .userTask().name(TASK_AFTER_OUTPUT_MAPPING)
      .endEvent()
      .done();
    deployEventSubProcessWithVariableIsSetInDelegationCode(modelInstance, false);


    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());

    //when task before service task is completed
    taskService.complete(task.getId());

    //then out mapping of call activity sets a variable
    //-> non interrupting conditional event is triggered
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(2, tasksAfterVariableIsSet.size());
    assertEquals(1, conditionEventSubscriptionQuery.count());
  }


  @Test
  public void testSetVariableInInMappingOfCallActivity() {
    engine.manageDeployment(repositoryService.createDeployment().addModelInstance(CONDITIONAL_MODEL, DELEGATED_PROCESS).deploy());

    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .userTask(TASK_BEFORE_CONDITION_ID)
      .name(TASK_BEFORE_CONDITION)
      .callActivity(TASK_WITH_CONDITION_ID)
      .calledElement(DELEGATED_PROCESS_KEY)
      .camundaIn(VARIABLE_NAME, VARIABLE_NAME)
      .userTask().name(TASK_AFTER_OUTPUT_MAPPING)
      .endEvent()
      .done();
    deployEventSubProcessWithVariableIsSetInDelegationCode(modelInstance, true);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());

    //when task is completed
    taskService.complete(task.getId());

    //then in mapping from call activity sets variable
    //-> interrupting conditional event is not triggered, since variable is only locally
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(TASK_AFTER_OUTPUT_MAPPING, tasksAfterVariableIsSet.get(0).getName());
  }

  @Test
  public void testNonInterruptingSetVariableInInMappingOfCallActivity() {
    engine.manageDeployment(repositoryService.createDeployment().addModelInstance(CONDITIONAL_MODEL, DELEGATED_PROCESS).deploy());

    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .userTask(TASK_BEFORE_CONDITION_ID)
      .name(TASK_BEFORE_CONDITION)
      .callActivity(TASK_WITH_CONDITION_ID)
      .calledElement(DELEGATED_PROCESS_KEY)
      .camundaIn(VARIABLE_NAME, VARIABLE_NAME)
      .userTask().name(TASK_AFTER_OUTPUT_MAPPING)
      .endEvent()
      .done();
    deployEventSubProcessWithVariableIsSetInDelegationCode(modelInstance, false);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());

    //when task is completed
    taskService.complete(task.getId());

    //then in mapping from call activity sets variable
    //-> interrupting conditional event is not triggered, since variable is only locally
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(TASK_AFTER_OUTPUT_MAPPING, tasksAfterVariableIsSet.get(0).getName());
  }

  @Test
  public void testSetVariableInCallActivity() {
    engine.manageDeployment(repositoryService.createDeployment().addModelInstance(CONDITIONAL_MODEL, DELEGATED_PROCESS).deploy());

    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
                                                  .startEvent()
                                                  .userTask(TASK_BEFORE_CONDITION_ID)
                                                    .name(TASK_BEFORE_CONDITION)
                                                  .callActivity(TASK_WITH_CONDITION_ID)
                                                    .calledElement(DELEGATED_PROCESS_KEY)
                                                  .userTask().name(TASK_AFTER_SERVICE_TASK)
                                                  .endEvent()
                                                  .done();
    deployEventSubProcessWithVariableIsSetInDelegationCode(modelInstance, true);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());

    //when task is completed
    taskService.complete(task.getId());

    //then service task in call activity sets variable
    //conditional event is not triggered
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(TASK_AFTER_SERVICE_TASK, tasksAfterVariableIsSet.get(0).getName());
  }

  @Test
  public void testNonInterruptingSetVariableInCallActivity() {
    engine.manageDeployment(repositoryService.createDeployment().addModelInstance(CONDITIONAL_MODEL, DELEGATED_PROCESS).deploy());

    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
                                                  .startEvent()
                                                  .userTask(TASK_BEFORE_CONDITION_ID)
                                                    .name(TASK_BEFORE_CONDITION)
                                                  .callActivity(TASK_WITH_CONDITION_ID)
                                                    .calledElement(DELEGATED_PROCESS_KEY)
                                                  .userTask().name(TASK_AFTER_SERVICE_TASK)
                                                  .endEvent()
                                                  .done();
    deployEventSubProcessWithVariableIsSetInDelegationCode(modelInstance, false);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());

    //when task is completed
    taskService.complete(task.getId());

    //then service task in call activity sets variable
    //conditional event is not triggered
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(TASK_AFTER_SERVICE_TASK, tasksAfterVariableIsSet.get(0).getName());
  }

  @Test
  public void testSetVariableInSubProcessInDelegatedCode() {

    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
                                                  .startEvent()
                                                  .userTask(TASK_BEFORE_CONDITION_ID)
                                                    .name(TASK_BEFORE_CONDITION)
                                                  .subProcess(SUB_PROCESS_ID)
                                                  .embeddedSubProcess()
                                                    .startEvent()
                                                    .serviceTask()
                                                    .camundaExpression(EXPR_SET_VARIABLE)
                                                    .userTask().name(TASK_AFTER_SERVICE_TASK)
                                                    .endEvent()
                                                  .subProcessDone()
                                                  .endEvent()
                                                  .done();
    deployEventSubProcessWithVariableIsSetInDelegationCode(modelInstance, SUB_PROCESS_ID, true);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());

    //when task is completed
    taskService.complete(task.getId());

    //then service task in sub process sets variable
    //conditional event is triggered
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(TASK_AFTER_CONDITION, tasksAfterVariableIsSet.get(0).getName());
  }

  @Test
  public void testNonInterruptingSetVariableInSubProcessInDelegatedCode() {

    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
                                                  .startEvent()
                                                  .userTask(TASK_BEFORE_CONDITION_ID)
                                                    .name(TASK_BEFORE_CONDITION)
                                                  .subProcess(SUB_PROCESS_ID)
                                                  .embeddedSubProcess()
                                                    .startEvent()
                                                    .serviceTask()
                                                    .camundaExpression(EXPR_SET_VARIABLE)
                                                    .userTask().name(TASK_AFTER_SERVICE_TASK)
                                                    .endEvent()
                                                  .subProcessDone()
                                                  .endEvent()
                                                  .done();
    deployEventSubProcessWithVariableIsSetInDelegationCode(modelInstance, SUB_PROCESS_ID, false);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());

    //when task is completed
    taskService.complete(task.getId());

    //then service task in sub process sets variable
    //conditional event is triggered
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(2, tasksAfterVariableIsSet.size());
    assertEquals(1, conditionEventSubscriptionQuery.list().size());
  }

  @Test
  public void testSetVariableInSubProcessInDelegatedCodeConditionOnPI() {

    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
                                                  .startEvent()
                                                  .userTask(TASK_BEFORE_CONDITION_ID)
                                                    .name(TASK_BEFORE_CONDITION)
                                                  .subProcess(SUB_PROCESS_ID)
                                                  .embeddedSubProcess()
                                                    .startEvent()
                                                    .serviceTask()
                                                    .camundaExpression(EXPR_SET_VARIABLE)
                                                    .userTask().name(TASK_AFTER_SERVICE_TASK)
                                                    .endEvent()
                                                  .subProcessDone()
                                                  .endEvent()
                                                  .done();
    deployEventSubProcessWithVariableIsSetInDelegationCode(modelInstance, true);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());

    //when task is completed
    taskService.complete(task.getId());

    //then service task in sub process sets variable
    //conditional event is triggered
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(TASK_AFTER_CONDITION, tasksAfterVariableIsSet.get(0).getName());
  }

  @Test
  public void testNonInterruptingSetVariableInSubProcessInDelegatedCodeConditionOnPI() {

    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
                                                  .startEvent()
                                                  .userTask(TASK_BEFORE_CONDITION_ID)
                                                    .name(TASK_BEFORE_CONDITION)
                                                  .subProcess(SUB_PROCESS_ID)
                                                  .embeddedSubProcess()
                                                    .startEvent()
                                                    .serviceTask()
                                                    .camundaExpression(EXPR_SET_VARIABLE)
                                                    .userTask().name(TASK_AFTER_SERVICE_TASK)
                                                    .endEvent()
                                                  .subProcessDone()
                                                  .endEvent()
                                                  .done();
    deployEventSubProcessWithVariableIsSetInDelegationCode(modelInstance, false);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());

    //when task is completed
    taskService.complete(task.getId());

    //then service task in sub process sets variable
    //conditional event is triggered
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(2, tasksAfterVariableIsSet.size());
    assertEquals(1, conditionEventSubscriptionQuery.list().size());
  }

  @Test
  @Deployment(resources ={ "org/camunda/bpm/engine/test/bpmn/event/conditional/EventSubProcessStartConditionalEventTest.testSubProcessVariableCondition.bpmn20.xml"})
  public void testSubProcessSetVariableOnProcessInstanceCondition() {
    //given process with event sub process conditional start event and user task in sub process
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());

    //when variable is set on process instance
    runtimeService.setVariable(procInst.getId(), VARIABLE_NAME, 1);

    //then execution is at user task after conditional start event
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(TASK_AFTER_CONDITION, tasksAfterVariableIsSet.get(0).getName());
  }

  @Test
  public void testSuspendedProcess() {

    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .userTask(TASK_WITH_CONDITION_ID)
      .endEvent().done();

    deployEventSubProcessWithVariableIsSetInDelegationCode(modelInstance, true);

    // given suspended process
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    runtimeService.suspendProcessInstanceById(procInst.getId());

    //when wrong variable is set
    runtimeService.setVariable(procInst.getId(), VARIABLE_NAME+1, 1);

    //then nothing happens
    assertTrue(runtimeService.createProcessInstanceQuery().singleResult().isSuspended());

    //when variable which triggers condition is set
    //then exception is expected
    try {
      runtimeService.setVariable(procInst.getId(), VARIABLE_NAME, 1);
      fail("Should fail!");
    } catch (SuspendedEntityInteractionException seie) {
      //expected
    }
    runtimeService.activateProcessInstanceById(procInst.getId());
    tasksAfterVariableIsSet = taskService.createTaskQuery().list();
  }

  @Test
  public void testNonInterruptingConditionalSuspendedProcess() {

    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .userTask(TASK_WITH_CONDITION_ID)
      .endEvent().done();


    deployEventSubProcessWithVariableIsSetInDelegationCode(modelInstance, false);

    // given suspended process
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    runtimeService.suspendProcessInstanceById(procInst.getId());

    //when wrong variable is set
    runtimeService.setVariable(procInst.getId(), VARIABLE_NAME+1, 1);

    //then nothing happens
    assertTrue(runtimeService.createProcessInstanceQuery().singleResult().isSuspended());

    //when variable which triggers condition is set
    //then exception is expected
    try {
      runtimeService.setVariable(procInst.getId(), VARIABLE_NAME, 1);
      fail("Should fail!");
    } catch (SuspendedEntityInteractionException seie) {
      //expected
    }
    runtimeService.activateProcessInstanceById(procInst.getId());
    tasksAfterVariableIsSet = taskService.createTaskQuery().list();
  }
}
