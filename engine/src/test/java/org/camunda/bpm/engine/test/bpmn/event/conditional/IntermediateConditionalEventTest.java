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
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Test;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import org.camunda.bpm.engine.ProcessEngineException;

/**
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class IntermediateConditionalEventTest extends AbstractConditionalEventTestCase {

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
  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/event/conditional/IntermediateConditionalEventTest.testVariableValue.bpmn20.xml"})
  public void testWithNoVariableValue() {
    //given process with intermediate conditional event and no variable
    //then exception is expected, since no variable with this name exist
    expectException.expect(ProcessEngineException.class);
    expectException.expectMessage("Unknown property used in expression: ${variable == 1}. Cause: Cannot resolve identifier 'variable'");
    runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
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
  }
}
