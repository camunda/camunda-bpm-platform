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

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
@RunWith(Parameterized.class)
public class ConditionalEventWithSpecificVariableEventTest extends AbstractConditionalEventTestCase {

  private interface ConditionalProcessVarSpecification {
    BpmnModelInstance getProcessWithVarName(boolean interrupting, String condition);
    BpmnModelInstance getProcessWithVarNameAndEvents(boolean interrupting, String varEvent);
    BpmnModelInstance getProcessWithVarEvents(boolean interrupting, String varEvent);
  }

  @Parameterized.Parameters(name = "{0}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
      {
        //conditional boundary event
        new ConditionalProcessVarSpecification() {
        @Override
        public BpmnModelInstance getProcessWithVarName(boolean interrupting, String condition) {
          return modify(TASK_MODEL)
            .userTaskBuilder(TASK_BEFORE_CONDITION_ID)
            .boundaryEvent()
            .cancelActivity(interrupting)
            .conditionalEventDefinition(CONDITIONAL_EVENT)
            .condition(condition)
            .camundaVariableName(VARIABLE_NAME)
            .conditionalEventDefinitionDone()
            .userTask()
            .name(TASK_AFTER_CONDITION)
            .endEvent()
            .done();
        }

        @Override
        public BpmnModelInstance getProcessWithVarNameAndEvents(boolean interrupting, String varEvent) {
          return modify(TASK_MODEL)
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
        }

        @Override
        public BpmnModelInstance getProcessWithVarEvents(boolean interrupting, String varEvent) {
          return modify(TASK_MODEL)
            .userTaskBuilder(TASK_BEFORE_CONDITION_ID)
            .boundaryEvent()
            .cancelActivity(interrupting)
            .conditionalEventDefinition(CONDITIONAL_EVENT)
            .condition(CONDITION_EXPR)
            .camundaVariableEvents(varEvent)
            .conditionalEventDefinitionDone()
            .userTask()
            .name(TASK_AFTER_CONDITION)
            .endEvent()
            .done();
        }

          @Override
          public String toString() {
            return "ConditionalBoundaryEventWithVarEvents";
          }
        }},

      //conditional start event of event sub process
      {new ConditionalProcessVarSpecification() {
        @Override
        public BpmnModelInstance getProcessWithVarName(boolean interrupting, String condition) {
          return modify(TASK_MODEL)
            .addSubProcessTo(CONDITIONAL_EVENT_PROCESS_KEY)
            .triggerByEvent()
            .embeddedSubProcess()
            .startEvent()
            .interrupting(interrupting)
            .conditionalEventDefinition(CONDITIONAL_EVENT)
            .condition(condition)
            .camundaVariableName(VARIABLE_NAME)
            .conditionalEventDefinitionDone()
            .userTask()
            .name(TASK_AFTER_CONDITION)
            .endEvent()
            .done();
        }

        @Override
        public BpmnModelInstance getProcessWithVarNameAndEvents(boolean interrupting, String varEvent) {
          return modify(TASK_MODEL)
            .addSubProcessTo(CONDITIONAL_EVENT_PROCESS_KEY)
            .triggerByEvent()
            .embeddedSubProcess()
            .startEvent()
            .interrupting(interrupting)
            .conditionalEventDefinition(CONDITIONAL_EVENT)
            .condition(CONDITION_EXPR)
            .camundaVariableName(VARIABLE_NAME)
            .camundaVariableEvents(varEvent)
            .conditionalEventDefinitionDone()
            .userTask()
            .name(TASK_AFTER_CONDITION)
            .endEvent()
            .done();
        }

        @Override
        public BpmnModelInstance getProcessWithVarEvents(boolean interrupting, String varEvent) {
          return modify(TASK_MODEL)
            .addSubProcessTo(CONDITIONAL_EVENT_PROCESS_KEY)
            .triggerByEvent()
            .embeddedSubProcess()
            .startEvent()
            .interrupting(interrupting)
            .conditionalEventDefinition(CONDITIONAL_EVENT)
            .condition(CONDITION_EXPR)
            .camundaVariableEvents(varEvent)
            .conditionalEventDefinitionDone()
            .userTask()
            .name(TASK_AFTER_CONDITION)
            .endEvent()
            .done();
        }

        @Override
        public String toString() {
          return "ConditionalStartEventWithVarEvents";
        }
      }}
    });
  }


  @Parameterized.Parameter
  public ConditionalProcessVarSpecification specifier;

  @Test
  public void testVariableConditionWithVariableName() {

    //given process with boundary conditional event and defined variable name
    final BpmnModelInstance modelInstance = specifier.getProcessWithVarName(true, CONDITION_EXPR);
    engine.manageDeployment(repositoryService.createDeployment().addModelInstance(CONDITIONAL_MODEL, modelInstance).deploy());

    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);

    //when variable with name `variable1` is set on execution
    taskService.setVariable(task.getId(), VARIABLE_NAME + 1, 1);

    //then nothing happens
    task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());
    assertEquals(1, conditionEventSubscriptionQuery.list().size());

    //when variable with name `variable` is set on execution
    taskService.setVariable(task.getId(), VARIABLE_NAME, 1);

    //then execution is at user task after conditional event
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(TASK_AFTER_CONDITION, tasksAfterVariableIsSet.get(0).getName());
    assertEquals(0, conditionEventSubscriptionQuery.list().size());
  }

  @Test
  public void testVariableConditionWithVariableNameAndEvent() {

    //given process with boundary conditional event and defined variable name and event
    final BpmnModelInstance modelInstance = specifier.getProcessWithVarNameAndEvents(true, CONDITIONAL_VAR_EVENT_UPDATE);
    engine.manageDeployment(repositoryService.createDeployment().addModelInstance(CONDITIONAL_MODEL, modelInstance).deploy());

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

    //then execution is at user task after conditional event
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(TASK_AFTER_CONDITION, tasksAfterVariableIsSet.get(0).getName());
    assertEquals(0, conditionEventSubscriptionQuery.list().size());
  }

  @Test
  public void testNonInterruptingVariableConditionWithVariableName() {

    //given process with non interrupting boundary conditional event and defined variable name and true condition
    final BpmnModelInstance modelInstance = specifier.getProcessWithVarName(false, TRUE_CONDITION);
    engine.manageDeployment(repositoryService.createDeployment().addModelInstance(CONDITIONAL_MODEL, modelInstance).deploy());

    //when process is started
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());

    //then first event is triggered since condition is true
    List<Task> tasks = taskQuery.list();
    assertEquals(2, tasks.size());

    //when variable with name `variable1` is set on execution
    runtimeService.setVariable(procInst.getId(), VARIABLE_NAME + 1, 1);

    //then nothing happens
    tasks = taskQuery.list();
    assertEquals(2, tasks.size());
    assertEquals(1, conditionEventSubscriptionQuery.list().size());

    //when variable with name `variable` is set, updated and deleted
    runtimeService.setVariable(procInst.getId(), VARIABLE_NAME, 1); //create
    runtimeService.setVariable(procInst.getId(), VARIABLE_NAME, 1); //update
    runtimeService.removeVariable(procInst.getId(), VARIABLE_NAME); //delete

    //then execution is for four times at user task after conditional event
    //one from default behavior and three times from the variable events
    assertEquals(4, taskService.createTaskQuery().taskName(TASK_AFTER_CONDITION).count());
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(5, tasksAfterVariableIsSet.size());
    assertEquals(1, conditionEventSubscriptionQuery.list().size());
  }

  @Test
  public void testNonInterruptingVariableConditionWithVariableNameAndEvents() {

    //given process with non interrupting boundary conditional event and defined variable name and events
    final BpmnModelInstance modelInstance = specifier.getProcessWithVarNameAndEvents(false, CONDITIONAL_VAR_EVENTS);
    engine.manageDeployment(repositoryService.createDeployment().addModelInstance(CONDITIONAL_MODEL, modelInstance).deploy());

    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);

    //when variable with name `variable` is set, updated and deleted
    taskService.setVariable(task.getId(), VARIABLE_NAME, 1); //create
    taskService.setVariable(task.getId(), VARIABLE_NAME, 1); //update
    taskService.removeVariable(task.getId(), VARIABLE_NAME); //delete

    //then execution is for two times at user task after conditional start event
    assertEquals(2, taskService.createTaskQuery().taskName(TASK_AFTER_CONDITION).count());
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(3, tasksAfterVariableIsSet.size());
    assertEquals(1, conditionEventSubscriptionQuery.list().size());
  }


  @Test
  public void testVariableConditionWithVariableEvent() {

    //given process with boundary conditional event and defined variable event
    final BpmnModelInstance modelInstance = specifier.getProcessWithVarEvents(true, CONDITIONAL_VAR_EVENT_UPDATE);
    engine.manageDeployment(repositoryService.createDeployment().addModelInstance(CONDITIONAL_MODEL, modelInstance).deploy());

    Map<String, Object> variables = Variables.createVariables();
    variables.put(VARIABLE_NAME + 1, 0);
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY, variables);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);

    //when variable with name `variable` is set on execution
    runtimeService.setVariable(procInst.getId(), VARIABLE_NAME, 1);

    //then nothing happens
    task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());
    assertEquals(1, conditionEventSubscriptionQuery.list().size());

    //when variable with name `variable1` is updated
    runtimeService.setVariable(procInst.getId(), VARIABLE_NAME + 1, 1);

    //then execution is at user task after conditional intermediate event
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(TASK_AFTER_CONDITION, tasksAfterVariableIsSet.get(0).getName());
    assertEquals(0, conditionEventSubscriptionQuery.list().size());
  }

  @Test
  public void testNonInterruptingVariableConditionWithVariableEvent() {

    //given process with non interrupting boundary conditional event and defined variable event
    final BpmnModelInstance modelInstance = specifier.getProcessWithVarEvents(false, CONDITIONAL_VAR_EVENT_UPDATE);
    engine.manageDeployment(repositoryService.createDeployment().addModelInstance(CONDITIONAL_MODEL, modelInstance).deploy());

    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);

    //when variable with name `variable` is set
    taskService.setVariable(task.getId(), VARIABLE_NAME, 1); //create

    //then nothing happens
    task = taskQuery.singleResult();
    assertNotNull(task);

    //when variable is updated twice
    taskService.setVariable(task.getId(), VARIABLE_NAME, 1); //update
    taskService.setVariable(task.getId(), VARIABLE_NAME, 1); //update

    //then execution is for two times at user task after conditional event
    assertEquals(2, taskQuery.taskName(TASK_AFTER_CONDITION).count());
    tasksAfterVariableIsSet = taskService.createTaskQuery().list();
    assertEquals(3, tasksAfterVariableIsSet.size());
    assertEquals(1, conditionEventSubscriptionQuery.list().size());
  }

}
