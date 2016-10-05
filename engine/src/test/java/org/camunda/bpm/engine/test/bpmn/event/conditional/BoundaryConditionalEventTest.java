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

import java.util.List;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import static org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;
import static org.camunda.bpm.engine.test.bpmn.event.conditional.AbstractConditionalEventTestCase.TASK_MODEL;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

/**
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class BoundaryConditionalEventTest extends AbstractConditionalEventTestCase {

  protected static final String TASK_WITH_CONDITION = "Task with condition";
  protected static final String TASK_WITH_CONDITION_ID = "taskWithCondition";
  protected static final String TASK_IN_SUBPROCESS = "Task in Subprocess";

  @Test
  @Deployment
  public void testTrueCondition() {
    //given process with boundary conditional event
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_WITH_CONDITION, task.getName());
    assertEquals(1, conditionEventSubscriptionQuery.list().size());

    //when variable is set on task with condition
    taskService.setVariable(task.getId(), VARIABLE_NAME, 1);

    //then execution is at user task after boundary event
    task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_AFTER_CONDITION, task.getName());

    //and subscriptions are cleaned up
    assertEquals(0, conditionEventSubscriptionQuery.list().size());
  }

  @Test
  @Deployment
  public void testNonInterruptingTrueCondition() {
    //given process with boundary conditional event
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_WITH_CONDITION, task.getName());
    assertEquals(1, conditionEventSubscriptionQuery.list().size());

    //when variable is set on task with condition
    taskService.setVariable(task.getId(), VARIABLE_NAME, 1);

    //then execution is at user task after boundary event and in the task with the boundary event
    List<Task> tasklist = taskQuery.list();
    assertEquals(2, tasklist.size());

    //and subscriptions are still exist
    assertEquals(1, conditionEventSubscriptionQuery.list().size());
  }

  @Test
  @Deployment
  public void testFalseCondition() {
    //given process with boundary conditional event
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery();
    Task task = taskQuery.processInstanceId(procInst.getId()).singleResult();
    assertNotNull(task);
    assertEquals(TASK_WITH_CONDITION, task.getName());

    //when variable is set on task with condition
    taskService.setVariable(task.getId(), VARIABLE_NAME, 1);

    //then execution stays in task with boundary condition
    Execution execution = runtimeService.createExecutionQuery()
             .processInstanceId(procInst.getId())
             .activityId(TASK_WITH_CONDITION_ID)
             .singleResult();
    assertNotNull(execution);
  }

  @Test
  @Deployment
  public void testVariableCondition() {
    //given process with boundary conditional event
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_WITH_CONDITION, task.getName());

    //when local variable is set on task with condition
    taskService.setVariableLocal(task.getId(), VARIABLE_NAME, 1);

    //then execution should remain on task
    Execution execution = runtimeService.createExecutionQuery()
             .processInstanceId(procInst.getId())
             .activityId(TASK_WITH_CONDITION_ID)
             .singleResult();
    assertNotNull(execution);
  }

  @Test
  @Deployment(resources ={ "org/camunda/bpm/engine/test/bpmn/event/conditional/BoundaryConditionalEventTest.testVariableCondition.bpmn20.xml"})
  public void testVariableSetOnExecutionCondition() {
    //given process with boundary conditional event
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_WITH_CONDITION, task.getName());

    //when variable is set on task execution with condition
    taskService.setVariable(task.getId(), VARIABLE_NAME, 1);

    //then execution ends
    Execution execution = runtimeService.createExecutionQuery()
             .processInstanceId(procInst.getId())
             .activityId(TASK_WITH_CONDITION_ID)
             .singleResult();
    assertNull(execution);
  }

  @Test
  @Deployment
  public void testNonInterruptingVariableCondition() {
    //given process with boundary conditional event
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_WITH_CONDITION, task.getName());

    //when variable is set on task with condition
    taskService.setVariable(task.getId(), VARIABLE_NAME, 1);

    //then executions stay on user tasks
    List<Task> tasklist = taskQuery.list();
    assertEquals(2, tasklist.size());
  }


  @Test
  @Deployment(resources ={ "org/camunda/bpm/engine/test/bpmn/event/conditional/BoundaryConditionalEventTest.testVariableCondition.bpmn20.xml"})
  public void testWrongVariableCondition() {
    //given process with boundary conditional event
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_WITH_CONDITION, task.getName());
    assertEquals(1, conditionEventSubscriptionQuery.list().size());

    //when wrong variable is set on task with condition
    taskService.setVariable(task.getId(), VARIABLE_NAME + 1, 1);

    //then execution stays at user task with condition
    task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_WITH_CONDITION, task.getName());
    assertEquals(1, conditionEventSubscriptionQuery.list().size());

    //when correct variable is set
    taskService.setVariable(task.getId(), VARIABLE_NAME, 1);

    //then execution is on user task after condition
    task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_AFTER_CONDITION, task.getName());
    assertEquals(0, conditionEventSubscriptionQuery.list().size());
  }

  @Test
  @Deployment(resources ={ "org/camunda/bpm/engine/test/bpmn/event/conditional/BoundaryConditionalEventTest.testVariableCondition.bpmn20.xml"})
  public void testSetVariableOnTaskCondition() {
    //given process with boundary conditional event
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_WITH_CONDITION, task.getName());

    //when variable is set on task with condition
    taskService.setVariable(task.getId(), VARIABLE_NAME, 1);

    //then execution is at user task after boundary event
    task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_AFTER_CONDITION, task.getName());
  }

  @Test
  @Deployment(resources ={ "org/camunda/bpm/engine/test/bpmn/event/conditional/BoundaryConditionalEventTest.testNonInterruptingVariableCondition.bpmn20.xml"})
  public void testNonInterruptingSetVariableOnTaskCondition() {
    //given process with boundary conditional event
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_WITH_CONDITION, task.getName());

    //when variable is set on task with condition
    taskService.setVariable(task.getId(), VARIABLE_NAME, 1);

    //then execution is at user task after boundary event and in the task with the boundary event
    List<Task> tasklist = taskQuery.list();
    assertEquals(2, tasklist.size());
  }


  @Test
  @Deployment(resources ={ "org/camunda/bpm/engine/test/bpmn/event/conditional/BoundaryConditionalEventTest.testVariableCondition.bpmn20.xml"})
  public void testSetVariableOnExecutionCondition() {
    //given process with boundary conditional event
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_WITH_CONDITION, task.getName());

    //when variable is set on execution with condition
    runtimeService.setVariable(procInst.getId(), VARIABLE_NAME, 1);

    //then execution is at user task after boundary event
    task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_AFTER_CONDITION, task.getName());
  }

  @Test
  @Deployment(resources ={ "org/camunda/bpm/engine/test/bpmn/event/conditional/BoundaryConditionalEventTest.testNonInterruptingVariableCondition.bpmn20.xml"})
  public void testNonInterruptingSetVariableOnExecutionCondition() {
    //given process with boundary conditional event
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_WITH_CONDITION, task.getName());

    //when variable is set on execution with condition
    runtimeService.setVariable(procInst.getId(), VARIABLE_NAME, 1);

    //then execution is at user task after boundary event and in the task with the boundary event
    List<Task> tasklist = taskQuery.list();
    assertEquals(2, tasklist.size());
  }

  @Test
  @Deployment
  public void testParallelVariableCondition() {
    //given process with parallel user tasks and boundary conditional event
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    List<Task> tasks = taskQuery.list();
    assertEquals(2, tasks.size());
    assertEquals(2, conditionEventSubscriptionQuery.list().size());

    Task task = tasks.get(0);

    //when local variable is set on execution with condition
    runtimeService.setVariableLocal(task.getExecutionId(), VARIABLE_NAME, 1);

    //then boundary event is triggered of this task and task ends (subscription is deleted)
    //other execution stays in other task
    List<Execution> executions = runtimeService.createExecutionQuery()
             .processInstanceId(procInst.getId())
             .list();
    assertEquals(2, executions.size());

    tasks = taskQuery.list();
    assertEquals(1, tasks.size());

    Execution execution = runtimeService.createExecutionQuery()
             .processInstanceId(procInst.getId())
             .activityId(task.getId())
             .singleResult();
    assertNull(execution);
    assertEquals(1, conditionEventSubscriptionQuery.list().size());
  }

  @Test
  @Deployment(resources ={ "org/camunda/bpm/engine/test/bpmn/event/conditional/BoundaryConditionalEventTest.testParallelVariableCondition.bpmn20.xml"})
  public void testParallelSetVariableOnTaskCondition() {
    //given process with parallel user tasks and boundary conditional event
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    List<Task> tasks = taskQuery.list();
    assertEquals(2, tasks.size());

    Task task = tasks.get(0);

    //when variable is set on execution
    taskService.setVariable(task.getId(), VARIABLE_NAME, 1);

    //then both boundary event are triggered and process instance ends
    List<Execution> executions = runtimeService.createExecutionQuery()
             .processInstanceId(procInst.getId())
             .list();
    assertEquals(0, executions.size());

    tasks = taskQuery.list();
    assertEquals(0, tasks.size());
  }

  @Test
  @Deployment(resources ={ "org/camunda/bpm/engine/test/bpmn/event/conditional/BoundaryConditionalEventTest.testParallelVariableCondition.bpmn20.xml"})
  public void testParallelSetVariableOnExecutionCondition() {
    //given process with parallel user tasks and boundary conditional event
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    List<Task> tasks = taskQuery.list();
    assertEquals(2, tasks.size());

    //when variable is set on execution
    //taskService.setVariable(task.getId(), VARIABLE_NAME, 1);
    runtimeService.setVariable(procInst.getId(), VARIABLE_NAME, 1);

    //then both boundary events are triggered and process instance ends
    List<Execution> executions = runtimeService.createExecutionQuery()
             .processInstanceId(procInst.getId())
             .list();
    assertEquals(0, executions.size());

    tasks = taskQuery.list();
    assertEquals(0, tasks.size());
  }

  @Test
  @Deployment
  public void testSubProcessVariableCondition() {
    //given process with boundary conditional event on sub process
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_IN_SUBPROCESS, task.getName());
    assertEquals(1, conditionEventSubscriptionQuery.list().size());

    //when local variable is set on task with condition
    taskService.setVariableLocal(task.getId(), VARIABLE_NAME, 1);

    //then execution stays on user task
    List<Execution> executions = runtimeService.createExecutionQuery()
             .processInstanceId(procInst.getId())
             .list();
    assertEquals(2, executions.size());
    assertEquals(1, conditionEventSubscriptionQuery.list().size());
  }

  @Test
  @Deployment(resources ={ "org/camunda/bpm/engine/test/bpmn/event/conditional/BoundaryConditionalEventTest.testSubProcessVariableCondition.bpmn20.xml"})
  public void testSubProcessSetVariableOnTaskCondition() {
    //given process with boundary conditional event on sub process
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_IN_SUBPROCESS, task.getName());

    //when variable is set on task execution with condition
    taskService.setVariable(task.getId(), VARIABLE_NAME, 1);

    //then process instance ends
    List<Execution> executions = runtimeService.createExecutionQuery()
             .processInstanceId(procInst.getId())
             .list();
    assertEquals(0, executions.size());
  }

  @Test
  @Deployment(resources ={ "org/camunda/bpm/engine/test/bpmn/event/conditional/BoundaryConditionalEventTest.testSubProcessVariableCondition.bpmn20.xml"})
  public void testSubProcessSetVariableOnExecutionCondition() {
    //given process with boundary conditional event on sub process
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_IN_SUBPROCESS, task.getName());

    //when variable is set on task execution with condition
    runtimeService.setVariable(task.getExecutionId(), VARIABLE_NAME, 1);

    //then process instance ends
    List<Execution> executions = runtimeService.createExecutionQuery()
             .processInstanceId(procInst.getId())
             .list();
    assertEquals(0, executions.size());
  }

  @Test
  @Deployment
  public void testNonInterruptingSubProcessVariableCondition() {
    //given process with boundary conditional event on sub process
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_IN_SUBPROCESS, task.getName());

    //when variable is set on task with condition
    taskService.setVariable(task.getId(), VARIABLE_NAME, 1);

    //then execution stays on user task and is on task after condition
    List<Task> tasks = taskQuery.list();
    assertEquals(2, tasks.size());
  }

  @Test
  @Deployment
  public void testCleanUpConditionalEventSubscriptions() {
    //given process with boundary conditional event
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();

    assertEquals(1, conditionEventSubscriptionQuery.list().size());

    //when task is completed
    taskService.complete(task.getId());

    //then conditional subscription should be deleted
    assertEquals(0, conditionEventSubscriptionQuery.list().size());
  }

  @Test
  public void testVariableConditionWithVariableName() {

    //given process with boundary conditional event and defined variable name
    deployBoundaryEventProcessWithVarName(true);

    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);

    //when variable with name `variable1` is set on execution
    taskService.setVariable(task.getId(), VARIABLE_NAME+1, 1);

    //then nothing happens
    task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());
    assertEquals(1, conditionEventSubscriptionQuery.list().size());

    //when variable with name `variable` is set on execution
    taskService.setVariable(task.getId(), VARIABLE_NAME, 1);

    //then execution is at user task after conditional start event
    task = taskQuery.singleResult();
    assertEquals(TASK_AFTER_CONDITION, task.getName());
    assertEquals(0, conditionEventSubscriptionQuery.list().size());
  }


  @Test
  public void testVariableConditionWithVariableNameAndEvent() {

    //given process with boundary conditional event and defined variable name and event
    deployBoundaryEventProcessWithVarEvents(true, CONDITIONAL_VAR_EVENT_UPDATE);

    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);

    //when variable with name `variable` is set on execution
    taskService.setVariable(task.getId(), VARIABLE_NAME, 1);

    //then nothing happens
    task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());
    assertEquals(1, conditionEventSubscriptionQuery.list().size());

    //when variable with name `variable` is updated
    taskService.setVariable(task.getId(), VARIABLE_NAME, 1);

    //then execution is at user task after conditional start event
    task = taskQuery.singleResult();
    assertEquals(TASK_AFTER_CONDITION, task.getName());
    assertEquals(0, conditionEventSubscriptionQuery.list().size());
  }

  @Test
  public void testNonInterruptingVariableConditionWithVariableName() {

    //given process with non interrupting boundary conditional event and defined variable name
    deployBoundaryEventProcessWithVarName(false);

    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);

    //when variable with name `variable1` is set on execution
    taskService.setVariable(task.getId(), VARIABLE_NAME+1, 1);

    //then nothing happens
    task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());
    assertEquals(1, conditionEventSubscriptionQuery.list().size());

    //when variable with name `variable` is set, updated and deleted
    taskService.setVariable(task.getId(), VARIABLE_NAME, 1); //create
    taskService.setVariable(task.getId(), VARIABLE_NAME, 1); //update
    taskService.removeVariable(task.getId(), VARIABLE_NAME); //delete

    //then execution is for three times at user task after conditional start event
    List<Task> tasks = taskQuery.taskName(TASK_AFTER_CONDITION).list();
    assertEquals(3, tasks.size());
    assertEquals(1, conditionEventSubscriptionQuery.list().size());
  }

  @Test
  public void testNonInterruptingVariableConditionWithVariableNameAndEvents() {

    //given process with non interrupting boundary conditional event and defined variable name and events
    deployBoundaryEventProcessWithVarEvents(false, CONDITIONAL_VAR_EVENTS);

    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);

    //when variable with name `variable` is set, updated and deleted
    taskService.setVariable(task.getId(), VARIABLE_NAME, 1); //create
    taskService.setVariable(task.getId(), VARIABLE_NAME, 1); //update
    taskService.removeVariable(task.getId(), VARIABLE_NAME); //delete

    //then execution is for two times at user task after conditional start event
    List<Task> tasks = taskQuery.taskName(TASK_AFTER_CONDITION).list();
    assertEquals(2, tasks.size());
    assertEquals(1, conditionEventSubscriptionQuery.list().size());
  }

  protected void deployBoundaryEventProcessWithVarName(boolean interrupting) {
    final BpmnModelInstance modelInstance = modify(TASK_MODEL)
            .userTaskBuilder(TASK_BEFORE_CONDITION_ID)
            .boundaryEvent()
              .cancelActivity(interrupting)
              .conditionalEventDefinition(CONDITIONAL_EVENT)
                .condition(TRUE_CONDITION)
                .camundaVariableName(VARIABLE_NAME)
              .conditionalEventDefinitionDone()
              .userTask()
                .name(TASK_AFTER_CONDITION)
              .endEvent()
            .done();

    engine.manageDeployment(repositoryService.createDeployment().addModelInstance(CONDITIONAL_MODEL, modelInstance).deploy());
  }

  protected void deployBoundaryEventProcessWithVarEvents(boolean interrupting, String varEvent) {
    final BpmnModelInstance modelInstance = modify(TASK_MODEL)
            .userTaskBuilder(TASK_BEFORE_CONDITION_ID)
            .boundaryEvent()
              .cancelActivity(interrupting)
              .conditionalEventDefinition(CONDITIONAL_EVENT)
                .condition(CONDITION_EXPR)
                .camundaVariableName(VARIABLE_NAME)
                  .camundaVariableEvents(varEvent)
              .conditionalEventDefinitionDone()
              .userTask()
                .name(TASK_AFTER_CONDITION)
              .endEvent()
            .done();

    engine.manageDeployment(repositoryService.createDeployment().addModelInstance(CONDITIONAL_MODEL, modelInstance).deploy());
  }
}
