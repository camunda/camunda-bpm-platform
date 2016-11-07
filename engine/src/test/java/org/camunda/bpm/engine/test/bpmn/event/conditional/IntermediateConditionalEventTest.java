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
import static org.junit.Assert.assertEquals;

import org.camunda.bpm.engine.SuspendedEntityInteractionException;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

/**
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class IntermediateConditionalEventTest extends AbstractConditionalEventTestCase {

  protected static final String EVENT_BASED_GATEWAY_ID = "egw";
  protected static final String PARALLEL_GATEWAY_ID = "parallelGateway";
  protected static final String TASK_BEFORE_SERVICE_TASK_ID = "taskBeforeServiceTask";
  protected static final String TASK_BEFORE_EVENT_BASED_GW_ID = "taskBeforeEGW";

  @Override
  public void checkIfProcessCanBeFinished() {
    //override since check is not needed in intermediate test suite
  }

  @Test
  @Deployment
  public void testFalseCondition() {
    //given process with intermediate conditional event
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery();
    Task task = taskQuery.processInstanceId(procInst.getId()).singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());

    //when task before condition is completed
    taskService.complete(task.getId());

    //then next wait state is on conditional event, since condition is false
    //and a condition event subscription is create
    Execution execution = runtimeService.createExecutionQuery()
             .processInstanceId(procInst.getId())
             .activityId(CONDITIONAL_EVENT)
             .singleResult();
    assertNotNull(execution);
    assertEquals(1, conditionEventSubscriptionQuery.list().size());
  }

  @Test
  @Deployment
  public void testTrueCondition() {
    //given process with intermediate conditional event
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());

    //when task before condition is completed
    taskService.complete(task.getId());

    //then next wait state is on user task after conditional event, since condition was true
    Execution execution = runtimeService.createExecutionQuery()
             .processInstanceId(procInst.getId())
             .activityId(CONDITIONAL_EVENT)
             .singleResult();
    assertNull(execution);

    task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_AFTER_CONDITION, task.getName());
  }

  @Test
  @Deployment
  public void testVariableValue() {
    //given process with intermediate conditional event and variable with wrong value
    Map<String, Object> variables = Variables.createVariables();
    variables.put(VARIABLE_NAME, 0);
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY, variables);

    //wait state is on conditional event, since condition is false
    Execution execution = runtimeService.createExecutionQuery()
             .processInstanceId(procInst.getId())
             .activityId(CONDITIONAL_EVENT)
             .singleResult();
    assertNotNull(execution);
    assertEquals(1, conditionEventSubscriptionQuery.list().size());

    //when variable is set to correct value
    runtimeService.setVariable(execution.getId(), VARIABLE_NAME, 1);

    //then process instance is completed, since condition was true
    execution = runtimeService.createExecutionQuery()
             .processInstanceId(procInst.getId())
             .activityId(CONDITIONAL_EVENT)
             .singleResult();
    assertNull(execution);

    procInst = runtimeService.createProcessInstanceQuery()
                             .processDefinitionKey(CONDITIONAL_EVENT_PROCESS_KEY)
                             .singleResult();
    assertNull(procInst);
    assertEquals(0, conditionEventSubscriptionQuery.list().size());
  }

  @Test
  @Deployment
  public void testParallelVariableValue() {
    //given process with intermediate conditional event and variable with wrong value
    Map<String, Object> variables = Variables.createVariables();
    variables.put(VARIABLE_NAME, 0);
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY, variables);
    Execution execution1 = runtimeService.createExecutionQuery()
             .processInstanceId(procInst.getId())
             .activityId(CONDITIONAL_EVENT + 1)
             .singleResult();

    Execution execution2 = runtimeService.createExecutionQuery()
             .processInstanceId(procInst.getId())
             .activityId(CONDITIONAL_EVENT + 2)
             .singleResult();
    assertEquals(2, conditionEventSubscriptionQuery.list().size());

    //when variable is set to correct value
    runtimeService.setVariable(execution1.getId(), VARIABLE_NAME, 1);

    //then execution of first conditional event is completed
    execution1 = runtimeService.createExecutionQuery()
             .processInstanceId(procInst.getId())
             .activityId(CONDITIONAL_EVENT + 1)
             .singleResult();
    assertNull(execution1);
    assertEquals(1, conditionEventSubscriptionQuery.list().size());

    //when second variable is set to correct value
    runtimeService.setVariable(execution2.getId(), VARIABLE_NAME, 2);

    //then execution and process instance is ended, since both conditions was true
    execution2 = runtimeService.createExecutionQuery()
             .processInstanceId(procInst.getId())
             .activityId(CONDITIONAL_EVENT + 2)
             .singleResult();
    assertNull(execution2);
    procInst = runtimeService.createProcessInstanceQuery()
                             .processDefinitionKey(CONDITIONAL_EVENT_PROCESS_KEY)
                             .singleResult();
    assertNull(procInst);
    assertEquals(0, conditionEventSubscriptionQuery.list().size());
  }


  @Test
  @Deployment
  public void testParallelVariableValueEqualConditions() {
    //given process with intermediate conditional event and variable with wrong value
    Map<String, Object> variables = Variables.createVariables();
    variables.put(VARIABLE_NAME, 0);
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY, variables);

    //when variable is set to correct value
    runtimeService.setVariable(procInst.getId(), VARIABLE_NAME, 1);

    //then process instance is ended, since both conditions are true
    procInst = runtimeService.createProcessInstanceQuery()
                             .processDefinitionKey(CONDITIONAL_EVENT_PROCESS_KEY)
                             .singleResult();
    assertNull(procInst);
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/event/conditional/IntermediateConditionalEventTest.testParallelVariableValue.bpmn20.xml"})
  public void testParallelVariableSetValueOnParent() {
    //given process with intermediate conditional event and variable with wrong value
    Map<String, Object> variables = Variables.createVariables();
    variables.put(VARIABLE_NAME, 0);
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY, variables);

    //when variable is set to correct value
    runtimeService.setVariable(procInst.getId(), VARIABLE_NAME, 1);

    //then execution of conditional event is completed
    Execution execution = runtimeService.createExecutionQuery()
             .processInstanceId(procInst.getId())
             .activityId(CONDITIONAL_EVENT + 1)
             .singleResult();
    assertNull(execution);

    //when second variable is set to correct value
    runtimeService.setVariable(procInst.getId(), VARIABLE_NAME, 2);

    //then execution and process instance is ended, since both conditions was true
    execution = runtimeService.createExecutionQuery()
             .processInstanceId(procInst.getId())
             .activityId(CONDITIONAL_EVENT + 2)
             .singleResult();
    assertNull(execution);
    procInst = runtimeService.createProcessInstanceQuery()
                             .processDefinitionKey(CONDITIONAL_EVENT_PROCESS_KEY)
                             .singleResult();
    assertNull(procInst);
  }

  @Test
  @Deployment
  public void testSubProcessVariableValue() {
    //given process with intermediate conditional event and variable with wrong value
    Map<String, Object> variables = Variables.createVariables();
    variables.put(VARIABLE_NAME, 0);
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY, variables);
    Execution execution = runtimeService.createExecutionQuery()
             .processInstanceId(procInst.getId())
             .activityId(CONDITIONAL_EVENT)
             .singleResult();
    assertNotNull(execution);

    //when variable is set to correct value
    runtimeService.setVariableLocal(execution.getId(), VARIABLE_NAME, 1);

    //then execution and process instance is ended, since condition was true
    execution = runtimeService.createExecutionQuery()
             .processInstanceId(procInst.getId())
             .activityId(CONDITIONAL_EVENT)
             .singleResult();
    assertNull(execution);
    procInst = runtimeService.createProcessInstanceQuery()
                             .processDefinitionKey(CONDITIONAL_EVENT_PROCESS_KEY)
                             .singleResult();
    assertNull(procInst);
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/event/conditional/IntermediateConditionalEventTest.testSubProcessVariableValue.bpmn20.xml"})
  public void testSubProcessVariableSetValueOnParent() {
    //given process with intermediate conditional event and variable with wrong value
    Map<String, Object> variables = Variables.createVariables();
    variables.put(VARIABLE_NAME, 0);
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY, variables);

    //when variable is set to correct value
    runtimeService.setVariable(procInst.getId(), VARIABLE_NAME, 1);

    //then process instance is ended, since condition was true
    procInst = runtimeService.createProcessInstanceQuery()
                             .processDefinitionKey(CONDITIONAL_EVENT_PROCESS_KEY)
                             .singleResult();
    assertNull(procInst);
  }

  @Test
  @Deployment
  public void testCleanUpConditionalEventSubscriptions() {
    //given process with intermediate conditional event and variable with wrong value
    Map<String, Object> variables = Variables.createVariables();
    variables.put(VARIABLE_NAME, 0);
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY, variables);

    //wait state is on conditional event, since condition is false
    Execution execution = runtimeService.createExecutionQuery()
             .processInstanceId(procInst.getId())
             .activityId(CONDITIONAL_EVENT)
             .singleResult();
    assertNotNull(execution);

    //condition subscription is created
    assertEquals(1, conditionEventSubscriptionQuery.list().size());

    //when variable is set to correct value
    runtimeService.setVariable(execution.getId(), VARIABLE_NAME, 1);

    //then execution is on next user task and the subscription is deleted
    Task task = taskService.createTaskQuery().processInstanceId(procInst.getId()).singleResult();
    assertNotNull(task);
    assertEquals(TASK_AFTER_CONDITION, task.getName());
    assertEquals(0, conditionEventSubscriptionQuery.list().size());

    //and task can be completed which ends process instance
    taskService.complete(task.getId());
    assertNull(taskService.createTaskQuery().singleResult());
    assertNull(runtimeService.createProcessInstanceQuery().singleResult());
  }

  @Test
  public void testVariableConditionWithVariableName() {

    //given process with boundary conditional event and defined variable name
    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
          .startEvent()
          .intermediateCatchEvent(CONDITIONAL_EVENT)
            .conditionalEventDefinition()
              .condition(CONDITION_EXPR)
              .camundaVariableName(VARIABLE_NAME)
            .conditionalEventDefinitionDone()
          .userTask()
            .name(TASK_AFTER_CONDITION)
          .endEvent()
          .done();

    engine.manageDeployment(repositoryService.createDeployment().addModelInstance(CONDITIONAL_MODEL, modelInstance).deploy());

    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Execution execution = runtimeService.createExecutionQuery()
             .processInstanceId(procInst.getId())
             .activityId(CONDITIONAL_EVENT)
             .singleResult();
    assertNotNull(execution);
    assertEquals(1, conditionEventSubscriptionQuery.list().size());

    //when variable with name `variable1` is set on execution
    runtimeService.setVariable(procInst.getId(), VARIABLE_NAME+1, 1);

    //then nothing happens
    execution = runtimeService.createExecutionQuery()
             .processInstanceId(procInst.getId())
             .activityId(CONDITIONAL_EVENT)
             .singleResult();
    assertNotNull(execution);
    assertEquals(1, conditionEventSubscriptionQuery.list().size());

    //when variable with name `variable` is set on execution
    runtimeService.setVariable(procInst.getId(), VARIABLE_NAME, 1);

    //then execution is at user task after conditional intermediate event
    Task task = taskQuery.singleResult();
    assertEquals(TASK_AFTER_CONDITION, task.getName());
    assertEquals(0, conditionEventSubscriptionQuery.list().size());

    //and task can be completed which ends process instance
    taskService.complete(task.getId());
    assertNull(taskService.createTaskQuery().singleResult());
    assertNull(runtimeService.createProcessInstanceQuery().singleResult());
  }

  @Test
  public void testVariableConditionWithVariableEvent() {

    //given process with boundary conditional event and defined variable name and event
    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
          .startEvent()
          .intermediateCatchEvent(CONDITIONAL_EVENT)
            .conditionalEventDefinition()
              .condition(CONDITION_EXPR)
              .camundaVariableEvents(CONDITIONAL_VAR_EVENT_UPDATE)
            .conditionalEventDefinitionDone()
          .userTask()
            .name(TASK_AFTER_CONDITION)
          .endEvent()
          .done();

    engine.manageDeployment(repositoryService.createDeployment().addModelInstance(CONDITIONAL_MODEL, modelInstance).deploy());

    Map<String, Object> variables = Variables.createVariables();
    variables.put(VARIABLE_NAME+1, 0);
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY, variables);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Execution execution = runtimeService.createExecutionQuery()
             .processInstanceId(procInst.getId())
             .activityId(CONDITIONAL_EVENT)
             .singleResult();
    assertNotNull(execution);
    assertEquals(1, conditionEventSubscriptionQuery.list().size());


    //when variable with name `variable` is set on execution
    runtimeService.setVariable(procInst.getId(), VARIABLE_NAME, 1);

    //then nothing happens
    execution = runtimeService.createExecutionQuery()
             .processInstanceId(procInst.getId())
             .activityId(CONDITIONAL_EVENT)
             .singleResult();
    assertNotNull(execution);
    assertEquals(1, conditionEventSubscriptionQuery.list().size());

    //when variable with name `variable1` is updated
    runtimeService.setVariable(procInst.getId(), VARIABLE_NAME+1, 1);

    //then execution is at user task after conditional intermediate event
    Task task = taskQuery.singleResult();
    assertEquals(TASK_AFTER_CONDITION, task.getName());
    assertEquals(0, conditionEventSubscriptionQuery.list().size());

    //and task can be completed which ends process instance
    taskService.complete(task.getId());
    assertNull(taskService.createTaskQuery().singleResult());
    assertNull(runtimeService.createProcessInstanceQuery().singleResult());
  }


  @Test
  public void testVariableConditionWithVariableNameAndEvent() {

    //given process with boundary conditional event and defined variable name and event
    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
          .startEvent()
          .intermediateCatchEvent(CONDITIONAL_EVENT)
            .conditionalEventDefinition()
              .condition(CONDITION_EXPR)
              .camundaVariableName(VARIABLE_NAME)
              .camundaVariableEvents(CONDITIONAL_VAR_EVENT_UPDATE)
            .conditionalEventDefinitionDone()
          .userTask()
            .name(TASK_AFTER_CONDITION)
          .endEvent()
          .done();

    engine.manageDeployment(repositoryService.createDeployment().addModelInstance(CONDITIONAL_MODEL, modelInstance).deploy());
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Execution execution = runtimeService.createExecutionQuery()
             .processInstanceId(procInst.getId())
             .activityId(CONDITIONAL_EVENT)
             .singleResult();
    assertNotNull(execution);
    assertEquals(1, conditionEventSubscriptionQuery.list().size());


    //when variable with name `variable` is set on execution
    runtimeService.setVariable(procInst.getId(), VARIABLE_NAME, 1);

    //then nothing happens
    execution = runtimeService.createExecutionQuery()
             .processInstanceId(procInst.getId())
             .activityId(CONDITIONAL_EVENT)
             .singleResult();
    assertNotNull(execution);
    assertEquals(1, conditionEventSubscriptionQuery.list().size());

    //when variable with name `variable` is updated
    runtimeService.setVariable(procInst.getId(), VARIABLE_NAME, 1);

    //then execution is at user task after conditional intermediate event
    Task task = taskQuery.singleResult();
    assertEquals(TASK_AFTER_CONDITION, task.getName());
    assertEquals(0, conditionEventSubscriptionQuery.list().size());

    //and task can be completed which ends process instance
    taskService.complete(task.getId());
    assertNull(taskService.createTaskQuery().singleResult());
    assertNull(runtimeService.createProcessInstanceQuery().singleResult());
  }

  @Test
  public void testSuspendedProcess() {

    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .intermediateCatchEvent(CONDITIONAL_EVENT)
        .conditionalEventDefinition()
          .condition(CONDITION_EXPR)
        .conditionalEventDefinitionDone()
      .endEvent().done();

    engine.manageDeployment(repositoryService.createDeployment().addModelInstance(CONDITIONAL_MODEL, modelInstance).deploy());
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
  public void testEventBasedGateway() {
    BpmnModelInstance modelInstance =
      Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
        .startEvent()
        .eventBasedGateway().id(EVENT_BASED_GATEWAY_ID)
        .intermediateCatchEvent(CONDITIONAL_EVENT)
          .conditionalEventDefinition()
          .condition(CONDITION_EXPR)
          .conditionalEventDefinitionDone()
        .userTask()
        .name(TASK_AFTER_CONDITION)
        .endEvent().done();

    engine.manageDeployment(repositoryService.createDeployment().addModelInstance(CONDITIONAL_MODEL, modelInstance).deploy());

    //given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    assertEquals(1, conditionEventSubscriptionQuery.list().size());
    Execution execution = runtimeService.createExecutionQuery()
      .processInstanceId(procInst.getId())
      .activityId(EVENT_BASED_GATEWAY_ID)
      .singleResult();
    assertNotNull(execution);

    //when variable is set on execution
    runtimeService.setVariable(procInst.getId(), VARIABLE_NAME, 1);

    //then execution is at user task after intermediate conditional event
    Task task = taskQuery.singleResult();
    assertEquals(TASK_AFTER_CONDITION, task.getName());
    assertEquals(0, conditionEventSubscriptionQuery.list().size());
  }

  @Test
  public void testEventBasedGatewayTrueCondition() {
    BpmnModelInstance modelInstance =
      Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
        .startEvent()
        .userTask(TASK_BEFORE_CONDITION_ID)
        .name(TASK_BEFORE_CONDITION)
        .eventBasedGateway()
        .id(EVENT_BASED_GATEWAY_ID)
        .intermediateCatchEvent(CONDITIONAL_EVENT)
        .conditionalEventDefinition()
        .condition(TRUE_CONDITION)
        .conditionalEventDefinitionDone()
        .userTask()
        .name(TASK_AFTER_CONDITION)
        .endEvent().done();

    engine.manageDeployment(repositoryService.createDeployment().addModelInstance(CONDITIONAL_MODEL, modelInstance).deploy());

    //given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();

    //when task before condition is completed
    taskService.complete(task.getId());

    //then next wait state is on user task after conditional event, since condition was true
    Execution execution = runtimeService.createExecutionQuery()
      .processInstanceId(procInst.getId())
      .activityId(EVENT_BASED_GATEWAY_ID)
      .singleResult();
    assertNull(execution);

    task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_AFTER_CONDITION, task.getName());
  }


  @Test
  public void testEventBasedGatewayWith2ConditionsOneIsTrue() {
    BpmnModelInstance modelInstance =
      Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
        .startEvent()
        .userTask(TASK_BEFORE_CONDITION_ID)
        .name(TASK_BEFORE_CONDITION)
        .eventBasedGateway()
        .id(EVENT_BASED_GATEWAY_ID)
        .intermediateCatchEvent()
        .conditionalEventDefinition()
        .condition(CONDITION_EXPR)
        .conditionalEventDefinitionDone()
        .userTask()
        .name(TASK_AFTER_CONDITION+1)
        .endEvent()
        .moveToLastGateway()
        .intermediateCatchEvent(CONDITIONAL_EVENT)
        .conditionalEventDefinition()
        .condition(TRUE_CONDITION)
        .conditionalEventDefinitionDone()
        .userTask()
        .name(TASK_AFTER_CONDITION+2)
        .endEvent()
        .done();

    engine.manageDeployment(repositoryService.createDeployment().addModelInstance(CONDITIONAL_MODEL, modelInstance).deploy());

    //given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();

    //when task before condition is completed
    taskService.complete(task.getId());

    //then next wait state is on user task after true conditional event
    Execution execution = runtimeService.createExecutionQuery()
      .processInstanceId(procInst.getId())
      .activityId(EVENT_BASED_GATEWAY_ID)
      .singleResult();
    assertNull(execution);

    task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_AFTER_CONDITION+2, task.getName());
  }

  @Test
  public void testEventBasedGatewayWith2VarConditions() {
    BpmnModelInstance modelInstance =
      Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
        .startEvent()
        .eventBasedGateway()
        .id(EVENT_BASED_GATEWAY_ID)
        .intermediateCatchEvent()
        .conditionalEventDefinition()
        .condition(CONDITION_EXPR)
        .conditionalEventDefinitionDone()
        .userTask()
        .name(TASK_AFTER_CONDITION+1)
        .endEvent()
        .moveToLastGateway()
        .intermediateCatchEvent(CONDITIONAL_EVENT)
        .conditionalEventDefinition()
        .condition("${var==2}")
        .conditionalEventDefinitionDone()
        .userTask()
        .name(TASK_AFTER_CONDITION+2)
        .endEvent()
        .done();

    engine.manageDeployment(repositoryService.createDeployment().addModelInstance(CONDITIONAL_MODEL, modelInstance).deploy());

    //given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Execution execution = runtimeService.createExecutionQuery()
      .processInstanceId(procInst.getId())
      .activityId(EVENT_BASED_GATEWAY_ID)
      .singleResult();
    assertNotNull(execution);

    //when wrong value of variable `var` is set
    runtimeService.setVariable(procInst.getId(), "var", 1);

    //then nothing happens
    execution = runtimeService.createExecutionQuery()
      .processInstanceId(procInst.getId())
      .activityId(EVENT_BASED_GATEWAY_ID)
      .singleResult();
    assertNotNull(execution);
    assertEquals(0, taskQuery.count());

    //when right value is set
    runtimeService.setVariable(procInst.getId(), "var", 2);

    //then next wait state is on user task after second conditional event
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_AFTER_CONDITION+2, task.getName());
  }

  protected void deployParallelProcessWithEventBasedGateway() {
    BpmnModelInstance modelInstance =
      Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
        .startEvent()
        .parallelGateway()
        .id(PARALLEL_GATEWAY_ID)
        .userTask(TASK_BEFORE_EVENT_BASED_GW_ID)
        .eventBasedGateway()
        .id(EVENT_BASED_GATEWAY_ID)
        .intermediateCatchEvent()
        .conditionalEventDefinition()
        .condition(CONDITION_EXPR)
        .conditionalEventDefinitionDone()
        .userTask()
        .name(TASK_AFTER_CONDITION)
        .endEvent()
        .moveToNode(PARALLEL_GATEWAY_ID)
        .userTask(TASK_BEFORE_SERVICE_TASK_ID)
        .serviceTask()
        .camundaClass(SetVariableDelegate.class.getName())
        .endEvent()
        .done();

    engine.manageDeployment(repositoryService.createDeployment().addModelInstance(CONDITIONAL_MODEL, modelInstance).deploy());
  }

  @Test
  public void testParallelProcessWithSetVariableBeforeReachingEventBasedGW() {
    deployParallelProcessWithEventBasedGateway();
    //given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task taskBeforeEGW = taskService.createTaskQuery().taskDefinitionKey(TASK_BEFORE_EVENT_BASED_GW_ID).singleResult();
    Task taskBeforeServiceTask = taskService.createTaskQuery().taskDefinitionKey(TASK_BEFORE_SERVICE_TASK_ID).singleResult();

    //when task before service task is completed and after that task before event based gateway
    taskService.complete(taskBeforeServiceTask.getId());
    taskService.complete(taskBeforeEGW.getId());

    //then variable is set before event based gateway is reached
    //on reaching event based gateway condition of conditional event is also evaluated to true
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_AFTER_CONDITION, task.getName());
    //completing this task ends process instance
    taskService.complete(task.getId());
    assertNull(taskQuery.singleResult());
    assertNull(runtimeService.createProcessInstanceQuery().singleResult());
  }

  @Test
  public void testParallelProcessWithSetVariableAfterReachingEventBasedGW() {
    deployParallelProcessWithEventBasedGateway();
    //given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task taskBeforeEGW = taskService.createTaskQuery().taskDefinitionKey(TASK_BEFORE_EVENT_BASED_GW_ID).singleResult();
    Task taskBeforeServiceTask = taskService.createTaskQuery().taskDefinitionKey(TASK_BEFORE_SERVICE_TASK_ID).singleResult();

    //when task before event based gateway is completed and after that task before service task
    taskService.complete(taskBeforeEGW.getId());
    taskService.complete(taskBeforeServiceTask.getId());

    //then event based gateway is reached and executions stays there
    //variable is set after reaching event based gateway
    //after setting variable the conditional event is triggered and evaluated to true
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_AFTER_CONDITION, task.getName());
    //completing this task ends process instance
    taskService.complete(task.getId());
    assertNull(taskQuery.singleResult());
    assertNull(runtimeService.createProcessInstanceQuery().singleResult());
  }
}
