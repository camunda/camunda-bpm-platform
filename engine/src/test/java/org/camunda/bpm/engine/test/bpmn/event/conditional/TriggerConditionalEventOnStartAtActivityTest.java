/*
 * Copyright 2016 camunda services GmbH.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.camunda.bpm.engine.test.bpmn.event.conditional;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class TriggerConditionalEventOnStartAtActivityTest extends AbstractConditionalEventTestCase {

  @Test
  public void testTriggerGlobalEventSubProcess() {
    //given
    deployConditionalEventSubProcess(TASK_MODEL, CONDITIONAL_EVENT_PROCESS_KEY, true);

    //when
    runtimeService.createProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY)
      .startBeforeActivity(TASK_BEFORE_CONDITION_ID)
      .setVariable(VARIABLE_NAME, "1")
      .executeWithVariablesInReturn();

    //then
    tasksAfterVariableIsSet = taskService.createTaskQuery().list();
    assertEquals(1, tasksAfterVariableIsSet.size());
    assertEquals(TASK_AFTER_CONDITION_ID, tasksAfterVariableIsSet.get(0).getTaskDefinitionKey());
  }


  @Test
  public void testNonInterruptingTriggerGlobalEventSubProcess() {
    //given
    deployConditionalEventSubProcess(TASK_MODEL, CONDITIONAL_EVENT_PROCESS_KEY, false);

    //when
    runtimeService.createProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY)
      .startBeforeActivity(TASK_BEFORE_CONDITION_ID)
      .setVariable(VARIABLE_NAME, "1")
      .executeWithVariablesInReturn();

    //then
    tasksAfterVariableIsSet = taskService.createTaskQuery().list();
    assertEquals(2, tasksAfterVariableIsSet.size());
    assertEquals(1, taskService.createTaskQuery().taskName(TASK_AFTER_CONDITION).count());
    assertEquals(1, conditionEventSubscriptionQuery.count());
  }


  @Test
  public void testTriggerInnerEventSubProcess() {
    //given
    BpmnModelInstance modelInstance =  Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .subProcess(SUB_PROCESS_ID)
      .embeddedSubProcess()
        .startEvent()
        .userTask(TASK_BEFORE_CONDITION_ID)
        .name(TASK_BEFORE_CONDITION)
        .endEvent()
      .subProcessDone()
      .endEvent()
      .done();
    deployConditionalEventSubProcess(modelInstance, SUB_PROCESS_ID, true);


    //when
    runtimeService.createProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY)
      .startBeforeActivity(TASK_BEFORE_CONDITION_ID)
      .setVariable(VARIABLE_NAME, "1")
      .executeWithVariablesInReturn();

    //then
    tasksAfterVariableIsSet = taskService.createTaskQuery().list();
    assertEquals(1, tasksAfterVariableIsSet.size());
    assertEquals(TASK_AFTER_CONDITION_ID, tasksAfterVariableIsSet.get(0).getTaskDefinitionKey());
  }

  @Test
  public void testNonInterruptingTriggerInnerEventSubProcess() {
    //given
    BpmnModelInstance modelInstance =  Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .subProcess(SUB_PROCESS_ID)
      .embeddedSubProcess()
      .startEvent()
      .userTask(TASK_BEFORE_CONDITION_ID)
      .name(TASK_BEFORE_CONDITION)
      .endEvent()
      .subProcessDone()
      .endEvent()
      .done();
    deployConditionalEventSubProcess(modelInstance, SUB_PROCESS_ID, false);


    //when
    runtimeService.createProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY)
      .startBeforeActivity(TASK_BEFORE_CONDITION_ID)
      .setVariable(VARIABLE_NAME, "1")
      .executeWithVariablesInReturn();

    //then
    tasksAfterVariableIsSet = taskService.createTaskQuery().list();
    assertEquals(2, tasksAfterVariableIsSet.size());
    assertEquals(1, taskService.createTaskQuery().taskName(TASK_AFTER_CONDITION).count());
    assertEquals(1, conditionEventSubscriptionQuery.count());
  }

  @Test
  public void testTriggerGlobalEventSubProcessFromInnerSubProcess() {
    //given
    BpmnModelInstance modelInstance =  Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .subProcess(SUB_PROCESS_ID)
      .embeddedSubProcess()
      .startEvent()
      .userTask(TASK_BEFORE_CONDITION_ID)
      .name(TASK_BEFORE_CONDITION)
      .endEvent()
      .subProcessDone()
      .endEvent()
      .done();
    deployConditionalEventSubProcess(modelInstance, CONDITIONAL_EVENT_PROCESS_KEY, true);

    //when
    runtimeService.createProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY)
      .startBeforeActivity(TASK_BEFORE_CONDITION_ID)
      .setVariable(VARIABLE_NAME, "1")
      .executeWithVariablesInReturn();

    //then
    tasksAfterVariableIsSet = taskService.createTaskQuery().list();
    assertEquals(1, tasksAfterVariableIsSet.size());
    assertEquals(TASK_AFTER_CONDITION_ID, tasksAfterVariableIsSet.get(0).getTaskDefinitionKey());
  }

  @Test
  public void testNonInterruptingTriggerGlobalEventSubProcessFromInnerSubProcess() {
    //given
    BpmnModelInstance modelInstance =  Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .subProcess(SUB_PROCESS_ID)
      .embeddedSubProcess()
        .startEvent()
        .userTask(TASK_BEFORE_CONDITION_ID)
        .name(TASK_BEFORE_CONDITION)
        .endEvent()
      .subProcessDone()
      .endEvent()
      .done();
    deployConditionalEventSubProcess(modelInstance, CONDITIONAL_EVENT_PROCESS_KEY, false);

    //when
    runtimeService.createProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY)
      .startBeforeActivity(TASK_BEFORE_CONDITION_ID)
      .setVariable(VARIABLE_NAME, "1")
      .executeWithVariablesInReturn();

    //then
    tasksAfterVariableIsSet = taskService.createTaskQuery().list();
    assertEquals(2, tasksAfterVariableIsSet.size());
    assertEquals(1, taskService.createTaskQuery().taskName(TASK_AFTER_CONDITION).count());
    assertEquals(1, conditionEventSubscriptionQuery.count());
  }


  @Test
  public void testTriggerGlobalAndInnerEventSubProcessFromInnerSubProcess() {
    //given
    BpmnModelInstance modelInstance =  Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .subProcess(SUB_PROCESS_ID)
      .embeddedSubProcess()
      .startEvent()
      .userTask(TASK_BEFORE_CONDITION_ID)
      .name(TASK_BEFORE_CONDITION)
      .endEvent()
      .subProcessDone()
      .endEvent()
      .done();
    modelInstance = addConditionalEventSubProcess(modelInstance, SUB_PROCESS_ID, TASK_AFTER_CONDITION_ID + 1, true);
    deployConditionalEventSubProcess(modelInstance, CONDITIONAL_EVENT_PROCESS_KEY, true);

    //when
    runtimeService.createProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY)
      .startBeforeActivity(TASK_BEFORE_CONDITION_ID)
      .setVariable(VARIABLE_NAME, "1")
      .executeWithVariablesInReturn();

    //then
    tasksAfterVariableIsSet = taskService.createTaskQuery().list();
    assertEquals(1, tasksAfterVariableIsSet.size());
    assertEquals(TASK_AFTER_CONDITION_ID, tasksAfterVariableIsSet.get(0).getTaskDefinitionKey());
  }


  @Test
  public void testNonInterruptingTriggerGlobalAndInnerEventSubProcessFromInnerSubProcess() {
    //given
    BpmnModelInstance modelInstance =  Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .subProcess(SUB_PROCESS_ID)
      .embeddedSubProcess()
        .startEvent()
        .userTask(TASK_BEFORE_CONDITION_ID)
        .name(TASK_BEFORE_CONDITION)
        .endEvent()
      .subProcessDone()
      .endEvent()
      .done();
    modelInstance = addConditionalEventSubProcess(modelInstance, SUB_PROCESS_ID, TASK_AFTER_CONDITION_ID + 1, false);
    deployConditionalEventSubProcess(modelInstance, CONDITIONAL_EVENT_PROCESS_KEY, false);

    //when
    runtimeService.createProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY)
      .startBeforeActivity(TASK_BEFORE_CONDITION_ID)
      .setVariable(VARIABLE_NAME, "1")
      .executeWithVariablesInReturn();

    //then
    tasksAfterVariableIsSet = taskService.createTaskQuery().list();
    assertEquals(3, tasksAfterVariableIsSet.size());
    assertEquals(2, taskService.createTaskQuery().taskName(TASK_AFTER_CONDITION).count());
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey(TASK_AFTER_CONDITION_ID).count());
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey(TASK_AFTER_CONDITION_ID + 1).count());
    assertEquals(2, conditionEventSubscriptionQuery.count());
  }


  @Test
  public void testTriggerBoundaryEvent() {
    //given
    deployConditionalBoundaryEventProcess(TASK_MODEL, TASK_BEFORE_CONDITION_ID, true);

    //when
    runtimeService.createProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY)
      .startBeforeActivity(TASK_BEFORE_CONDITION_ID)
      .setVariable(VARIABLE_NAME, "1")
      .executeWithVariablesInReturn();

    //then
    tasksAfterVariableIsSet = taskService.createTaskQuery().list();
    assertEquals(1, tasksAfterVariableIsSet.size());
    assertEquals(TASK_AFTER_CONDITION_ID, tasksAfterVariableIsSet.get(0).getTaskDefinitionKey());
  }

  @Test
  public void testNonInterruptingTriggerBoundaryEvent() {
    //given
    deployConditionalBoundaryEventProcess(TASK_MODEL, TASK_BEFORE_CONDITION_ID, false);

    //when
    runtimeService.createProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY)
      .startBeforeActivity(TASK_BEFORE_CONDITION_ID)
      .setVariable(VARIABLE_NAME, "1")
      .executeWithVariablesInReturn();

    //then
    tasksAfterVariableIsSet = taskService.createTaskQuery().list();
    assertEquals(2, tasksAfterVariableIsSet.size());
    assertEquals(1, taskService.createTaskQuery().taskName(TASK_AFTER_CONDITION).count());
    assertEquals(1, conditionEventSubscriptionQuery.count());
  }


  @Test
  public void testTriggerBoundaryEventFromInnerSubProcess() {
    //given
    BpmnModelInstance modelInstance =  Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .subProcess(SUB_PROCESS_ID)
      .embeddedSubProcess()
      .startEvent()
      .userTask(TASK_BEFORE_CONDITION_ID)
      .name(TASK_BEFORE_CONDITION)
      .endEvent()
      .subProcessDone()
      .endEvent()
      .done();
    deployConditionalBoundaryEventProcess(modelInstance, SUB_PROCESS_ID, true);


    //when
    runtimeService.createProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY)
      .startBeforeActivity(TASK_BEFORE_CONDITION_ID)
      .setVariable(VARIABLE_NAME, "1")
      .executeWithVariablesInReturn();

    //then
    tasksAfterVariableIsSet = taskService.createTaskQuery().list();
    assertEquals(1, tasksAfterVariableIsSet.size());
    assertEquals(TASK_AFTER_CONDITION_ID, tasksAfterVariableIsSet.get(0).getTaskDefinitionKey());
  }

  @Test
  public void testNonInterruptingTriggerBoundaryEventFromInnerSubProcess() {
    //given
    BpmnModelInstance modelInstance =  Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .subProcess(SUB_PROCESS_ID)
      .embeddedSubProcess()
      .startEvent()
      .userTask(TASK_BEFORE_CONDITION_ID)
      .name(TASK_BEFORE_CONDITION)
      .endEvent()
      .subProcessDone()
      .endEvent()
      .done();
    deployConditionalBoundaryEventProcess(modelInstance, SUB_PROCESS_ID, false);


    //when
    runtimeService.createProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY)
      .startBeforeActivity(TASK_BEFORE_CONDITION_ID)
      .setVariable(VARIABLE_NAME, "1")
      .executeWithVariablesInReturn();

    //then
    tasksAfterVariableIsSet = taskService.createTaskQuery().list();
    assertEquals(2, tasksAfterVariableIsSet.size());
    assertEquals(1, taskService.createTaskQuery().taskName(TASK_AFTER_CONDITION).count());
    assertEquals(1, conditionEventSubscriptionQuery.count());
  }

  @Test
  public void testTriggerUserAndSubProcessBoundaryEventFromInnerSubProcess() {
    //given
    BpmnModelInstance modelInstance =  Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .subProcess(SUB_PROCESS_ID)
      .embeddedSubProcess()
        .startEvent()
        .userTask(TASK_BEFORE_CONDITION_ID)
          .name(TASK_BEFORE_CONDITION)
        .endEvent()
      .subProcessDone()
      .endEvent()
      .done();
    modelInstance = addConditionalBoundaryEvent(modelInstance, TASK_BEFORE_CONDITION_ID, TASK_AFTER_CONDITION_ID + 1, true);
    deployConditionalBoundaryEventProcess(modelInstance, SUB_PROCESS_ID, true);


    //when
    runtimeService.createProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY)
      .startBeforeActivity(TASK_BEFORE_CONDITION_ID)
      .setVariable(VARIABLE_NAME, "1")
      .executeWithVariablesInReturn();

    //then
    tasksAfterVariableIsSet = taskService.createTaskQuery().list();
    assertEquals(1, tasksAfterVariableIsSet.size());
    assertEquals(TASK_AFTER_CONDITION_ID, tasksAfterVariableIsSet.get(0).getTaskDefinitionKey());
  }


  @Test
  public void testNonInterruptingTriggerUserAndSubProcessBoundaryEventFromInnerSubProcess() {
    //given
    BpmnModelInstance modelInstance =  Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .subProcess(SUB_PROCESS_ID)
      .embeddedSubProcess()
        .startEvent()
        .userTask(TASK_BEFORE_CONDITION_ID)
        .name(TASK_BEFORE_CONDITION)
        .endEvent()
      .subProcessDone()
      .endEvent()
      .done();
    modelInstance = addConditionalBoundaryEvent(modelInstance, TASK_BEFORE_CONDITION_ID, TASK_AFTER_CONDITION_ID + 1, false);
    deployConditionalBoundaryEventProcess(modelInstance, SUB_PROCESS_ID, false);


    //when
    runtimeService.createProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY)
      .startBeforeActivity(TASK_BEFORE_CONDITION_ID)
      .setVariable(VARIABLE_NAME, "1")
      .executeWithVariablesInReturn();

    //then
    tasksAfterVariableIsSet = taskService.createTaskQuery().list();
    assertEquals(3, tasksAfterVariableIsSet.size());
    assertEquals(2, taskService.createTaskQuery().taskName(TASK_AFTER_CONDITION).count());
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey(TASK_AFTER_CONDITION_ID+1).count());
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey(TASK_AFTER_CONDITION_ID).count());
    assertEquals(2, conditionEventSubscriptionQuery.count());
  }

  @Test
  public void testTriggerMixedProcess() {
    //given
    BpmnModelInstance modelInstance =  Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .subProcess(SUB_PROCESS_ID)
      .embeddedSubProcess()
      .startEvent()
      .userTask(TASK_BEFORE_CONDITION_ID)
      .name(TASK_BEFORE_CONDITION)
      .endEvent()
      .subProcessDone()
      .endEvent()
      .done();
    boolean isInterrupting = true;
    modelInstance = addConditionalBoundaryEvent(modelInstance, TASK_BEFORE_CONDITION_ID, TASK_AFTER_CONDITION_ID + 1, isInterrupting);
    modelInstance = addConditionalBoundaryEvent(modelInstance, SUB_PROCESS_ID, TASK_AFTER_CONDITION_ID + 2, isInterrupting);
    modelInstance = addConditionalEventSubProcess(modelInstance, SUB_PROCESS_ID, TASK_AFTER_CONDITION_ID + 3, isInterrupting);
    deployConditionalEventSubProcess(modelInstance, CONDITIONAL_EVENT_PROCESS_KEY, isInterrupting);

    //when
    runtimeService.createProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY)
      .startBeforeActivity(TASK_BEFORE_CONDITION_ID)
      .setVariable(VARIABLE_NAME, "1")
      .executeWithVariablesInReturn();

    //then
    tasksAfterVariableIsSet = taskService.createTaskQuery().list();
    assertEquals(1, tasksAfterVariableIsSet.size());
    assertEquals(TASK_AFTER_CONDITION_ID, tasksAfterVariableIsSet.get(0).getTaskDefinitionKey());
  }


  @Test
  public void testNonInterruptingTriggerMixedProcess() {
    //given
    BpmnModelInstance modelInstance =  Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .subProcess(SUB_PROCESS_ID)
      .embeddedSubProcess()
      .startEvent()
      .userTask(TASK_BEFORE_CONDITION_ID)
      .name(TASK_BEFORE_CONDITION)
      .endEvent()
      .subProcessDone()
      .endEvent()
      .done();
    boolean isInterrupting = false;
    modelInstance = addConditionalBoundaryEvent(modelInstance, TASK_BEFORE_CONDITION_ID, TASK_AFTER_CONDITION_ID + 1, isInterrupting);
    modelInstance = addConditionalBoundaryEvent(modelInstance, SUB_PROCESS_ID, TASK_AFTER_CONDITION_ID + 2, isInterrupting);
    modelInstance = addConditionalEventSubProcess(modelInstance, SUB_PROCESS_ID, TASK_AFTER_CONDITION_ID + 3, isInterrupting);
    deployConditionalEventSubProcess(modelInstance, CONDITIONAL_EVENT_PROCESS_KEY, isInterrupting);

    //when
    runtimeService.createProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY)
      .startBeforeActivity(TASK_BEFORE_CONDITION_ID)
      .setVariable(VARIABLE_NAME, "1")
      .executeWithVariablesInReturn();

    //then
    tasksAfterVariableIsSet = taskService.createTaskQuery().list();
    assertEquals(5, tasksAfterVariableIsSet.size());
    assertEquals(4, taskService.createTaskQuery().taskName(TASK_AFTER_CONDITION).count());
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey(TASK_AFTER_CONDITION_ID+1).count());
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey(TASK_AFTER_CONDITION_ID+2).count());
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey(TASK_AFTER_CONDITION_ID+3).count());
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey(TASK_AFTER_CONDITION_ID).count());
    assertEquals(4, conditionEventSubscriptionQuery.count());
  }

  @Test
  @Ignore
  public void testTwoInstructions() {
    //given
    BpmnModelInstance modelInstance =  Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent("start")
      .subProcess(SUB_PROCESS_ID)
      .embeddedSubProcess()
        .startEvent()
        .userTask(TASK_BEFORE_CONDITION_ID)
        .name(TASK_BEFORE_CONDITION)
        .endEvent()
      .subProcessDone()
      .endEvent()
      .moveToNode("start")
      .subProcess(SUB_PROCESS_ID + 1)
      .embeddedSubProcess()
        .startEvent()
        .userTask(TASK_BEFORE_CONDITION_ID + 1)
        .name(TASK_BEFORE_CONDITION + 1)
        .endEvent()
      .subProcessDone()
      .endEvent()
      .done();
    boolean isInterrupting = true;
    modelInstance = addConditionalBoundaryEvent(modelInstance, SUB_PROCESS_ID, TASK_AFTER_CONDITION_ID, isInterrupting);
    modelInstance = addConditionalBoundaryEvent(modelInstance, SUB_PROCESS_ID + 1, TASK_AFTER_CONDITION_ID + 1, isInterrupting);
    engine.manageDeployment(repositoryService.createDeployment().addModelInstance(CONDITIONAL_MODEL, modelInstance).deploy());

    //when
    runtimeService.createProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY)
      .startBeforeActivity(TASK_BEFORE_CONDITION_ID)
      .setVariable(VARIABLE_NAME, "1")
      .startBeforeActivity(TASK_BEFORE_CONDITION_ID + 1)
      .executeWithVariablesInReturn();

    //then
    tasksAfterVariableIsSet = taskService.createTaskQuery().list();
    assertTaskNames(tasksAfterVariableIsSet, TASK_AFTER_CONDITION_ID, TASK_AFTER_CONDITION_ID + 1);
  }


}
