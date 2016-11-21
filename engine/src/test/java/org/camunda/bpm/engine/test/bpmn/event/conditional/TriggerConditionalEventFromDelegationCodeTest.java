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

import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaExecutionListener;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
@RunWith(Parameterized.class)
public class TriggerConditionalEventFromDelegationCodeTest extends AbstractConditionalEventTestCase {

  private interface ConditionalEventProcessSpecifier {
    Class getDelegateClass();
    int getExpectedInterruptingCount();
    int getExpectedNonInterruptingCount();
    String getCondition();
  }


  @Parameterized.Parameters(name = "{index}: {0}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
      {new ConditionalEventProcessSpecifier() {
        @Override
        public Class getDelegateClass() {
          return SetVariableDelegate.class;
        }

        @Override
        public int getExpectedInterruptingCount() {
          return 1;
        }

        @Override
        public int getExpectedNonInterruptingCount() {
          return 1;
        }

        @Override
        public String getCondition() {
          return CONDITION_EXPR;
        }

        @Override
        public String toString() {
          return "SetSingleVariableInDelegate";
        }
      }
      }, {
      new ConditionalEventProcessSpecifier() {
        @Override
        public Class getDelegateClass() {
          return SetMultipleSameVariableDelegate.class;
        }

        @Override
        public int getExpectedInterruptingCount() {
          return 1;
        }

        @Override
        public int getExpectedNonInterruptingCount() {
          return 3;
        }


        @Override
        public String getCondition() {
          return "${variable2 == 1}";
        }

        @Override
        public String toString() {
          return "SetMultipleVariableInDelegate";
        }
      }}});
  }

  @Parameterized.Parameter
  public ConditionalEventProcessSpecifier specifier;

  @Test
  public void testSetVariableInStartListener() {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .userTask(TASK_BEFORE_CONDITION_ID)
      .name(TASK_BEFORE_CONDITION)
      .userTask(TASK_WITH_CONDITION_ID)
      .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_START, specifier.getDelegateClass().getName())
      .endEvent()
      .done();
    deployConditionalEventSubProcess(modelInstance, CONDITIONAL_EVENT_PROCESS_KEY, specifier.getCondition(), true);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());

    //when task is completed
    taskService.complete(task.getId());

    //then start listener sets variable
    //conditional event is triggered
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(specifier.getExpectedInterruptingCount(), taskQuery.taskName(TASK_AFTER_CONDITION).count());
  }

  @Test
  public void testNonInterruptingSetVariableInStartListener() {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .userTask(TASK_BEFORE_CONDITION_ID)
      .name(TASK_BEFORE_CONDITION)
      .userTask(TASK_WITH_CONDITION_ID)
      .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_START, specifier.getDelegateClass().getName())
      .name(TASK_WITH_CONDITION)
      .endEvent()
      .done();
    deployConditionalEventSubProcess(modelInstance, CONDITIONAL_EVENT_PROCESS_KEY, specifier.getCondition(), false);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());

    //when task is completed
    taskService.complete(taskQuery.singleResult().getId());

    //then start listener sets variable
    //non interrupting boundary event is triggered
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(1 + specifier.getExpectedNonInterruptingCount(), tasksAfterVariableIsSet.size());
    assertEquals(specifier.getExpectedNonInterruptingCount(), taskQuery.taskName(TASK_AFTER_CONDITION).count());
  }

  @Test
  public void testSetVariableInTakeListener() {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .userTask(TASK_BEFORE_CONDITION_ID)
      .name(TASK_BEFORE_CONDITION)
      .sequenceFlowId(FLOW_ID)
      .userTask(TASK_WITH_CONDITION_ID)
      .endEvent()
      .done();
    CamundaExecutionListener listener = modelInstance.newInstance(CamundaExecutionListener.class);
    listener.setCamundaEvent(ExecutionListener.EVENTNAME_TAKE);
    listener.setCamundaClass(specifier.getDelegateClass().getName());
    modelInstance.<SequenceFlow>getModelElementById(FLOW_ID).builder().addExtensionElement(listener);
    deployConditionalEventSubProcess(modelInstance, CONDITIONAL_EVENT_PROCESS_KEY, specifier.getCondition(), true);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());

    //when task is completed
    taskService.complete(task.getId());

    //then take listener sets variable
    //conditional event is triggered
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(specifier.getExpectedInterruptingCount(), taskQuery.taskName(TASK_AFTER_CONDITION).count());
  }

  @Test
  public void testNonInterruptingSetVariableInTakeListener() {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .userTask(TASK_BEFORE_CONDITION_ID)
      .name(TASK_BEFORE_CONDITION)
      .sequenceFlowId(FLOW_ID)
      .userTask(TASK_WITH_CONDITION_ID)
      .endEvent()
      .done();
    CamundaExecutionListener listener = modelInstance.newInstance(CamundaExecutionListener.class);
    listener.setCamundaEvent(ExecutionListener.EVENTNAME_TAKE);
    listener.setCamundaClass(specifier.getDelegateClass().getName());
    modelInstance.<SequenceFlow>getModelElementById(FLOW_ID).builder().addExtensionElement(listener);
    deployConditionalEventSubProcess(modelInstance, CONDITIONAL_EVENT_PROCESS_KEY, specifier.getCondition(), false);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());

    //when task is completed
    taskService.complete(task.getId());

    //then take listener sets variable
    //non interrupting boundary event is triggered
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(1 + specifier.getExpectedNonInterruptingCount(), tasksAfterVariableIsSet.size());
    assertEquals(specifier.getExpectedNonInterruptingCount(), taskQuery.taskName(TASK_AFTER_CONDITION).count());
  }

  @Test
  public void testSetVariableInTakeListenerWithAsyncBefore() {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .userTask(TASK_BEFORE_CONDITION_ID)
      .name(TASK_BEFORE_CONDITION)
      .sequenceFlowId(FLOW_ID)
      .userTask(TASK_WITH_CONDITION_ID).camundaAsyncBefore()
      .endEvent()
      .done();
    CamundaExecutionListener listener = modelInstance.newInstance(CamundaExecutionListener.class);
    listener.setCamundaEvent(ExecutionListener.EVENTNAME_TAKE);
    listener.setCamundaClass(specifier.getDelegateClass().getName());
    modelInstance.<SequenceFlow>getModelElementById(FLOW_ID).builder().addExtensionElement(listener);
    deployConditionalEventSubProcess(modelInstance, CONDITIONAL_EVENT_PROCESS_KEY, specifier.getCondition(), true);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertNotNull(task);
    assertEquals(TASK_BEFORE_CONDITION, task.getName());

    //when task is completed
    taskService.complete(task.getId());

    //then take listener sets variable
    //conditional event is triggered
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(specifier.getExpectedInterruptingCount(), taskQuery.taskName(TASK_AFTER_CONDITION).count());
  }

  @Test
  public void testNonInterruptingSetVariableInTakeListenerWithAsyncBefore() {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .userTask(TASK_BEFORE_CONDITION_ID)
      .name(TASK_BEFORE_CONDITION)
      .sequenceFlowId(FLOW_ID)
      .userTask(TASK_WITH_CONDITION_ID).camundaAsyncBefore()
      .endEvent()
      .done();
    CamundaExecutionListener listener = modelInstance.newInstance(CamundaExecutionListener.class);
    listener.setCamundaEvent(ExecutionListener.EVENTNAME_TAKE);
    listener.setCamundaClass(specifier.getDelegateClass().getName());
    modelInstance.<SequenceFlow>getModelElementById(FLOW_ID).builder().addExtensionElement(listener);
    deployConditionalEventSubProcess(modelInstance, CONDITIONAL_EVENT_PROCESS_KEY, specifier.getCondition(), false);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());

    //when task is completed
    taskService.complete(taskQuery.singleResult().getId());

    //then take listener sets variable
    //non interrupting boundary event is triggered
    assertEquals(specifier.getExpectedNonInterruptingCount(),  taskService.createTaskQuery().taskName(TASK_AFTER_CONDITION).count());

    //and job was created
    Job job = engine.getManagementService().createJobQuery().singleResult();
    assertNotNull(job);


    //when job is executed task is created
    engine.getManagementService().executeJob(job.getId());
    //when all tasks are completed
    assertEquals(specifier.getExpectedNonInterruptingCount() + 1, taskQuery.count());
    for (Task task : taskQuery.list()) {
      taskService.complete(task.getId());
    }

    //then no task exist and process instance is ended
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(0, tasksAfterVariableIsSet.size());
    assertNull(runtimeService.createProcessInstanceQuery().singleResult());
  }

  @Test
  public void testSetVariableInEndListener() {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .userTask(TASK_BEFORE_CONDITION_ID)
      .name(TASK_BEFORE_CONDITION)
      .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_END, specifier.getDelegateClass().getName())
      .userTask(TASK_WITH_CONDITION_ID)
      .endEvent()
      .done();
    deployConditionalEventSubProcess(modelInstance, CONDITIONAL_EVENT_PROCESS_KEY, specifier.getCondition(), true);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();

    //when task is completed
    taskService.complete(task.getId());

    //then end listener sets variable
    //conditional event is triggered
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(specifier.getExpectedInterruptingCount(), taskQuery.taskName(TASK_AFTER_CONDITION).count());
  }

  @Test
  public void testNonInterruptingSetVariableInEndListener() {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .userTask(TASK_BEFORE_CONDITION_ID)
      .name(TASK_BEFORE_CONDITION)
      .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_END, specifier.getDelegateClass().getName())
      .userTask(TASK_WITH_CONDITION_ID)
      .name(TASK_WITH_CONDITION)
      .endEvent()
      .done();
    deployConditionalEventSubProcess(modelInstance, CONDITIONAL_EVENT_PROCESS_KEY, specifier.getCondition(), false);

    // given
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());

    //when task is completed
    taskService.complete(taskQuery.singleResult().getId());

    //then end listener sets variable
    //non interrupting event is triggered
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(1 + specifier.getExpectedNonInterruptingCount(), tasksAfterVariableIsSet.size());
    assertEquals(specifier.getExpectedNonInterruptingCount(), taskQuery.taskName(TASK_AFTER_CONDITION).count());
  }

  @Test
  public void testSetVariableInStartAndEndListener() {
    //given process with start and end listener on user task
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(CONDITIONAL_EVENT_PROCESS_KEY)
      .startEvent()
      .userTask(TASK_BEFORE_CONDITION_ID)
      .name(TASK_BEFORE_CONDITION)
      .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_START, specifier.getDelegateClass().getName())
      .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_END, specifier.getDelegateClass().getName())
      .userTask(TASK_WITH_CONDITION_ID)
      .endEvent()
      .done();
    deployConditionalEventSubProcess(modelInstance, CONDITIONAL_EVENT_PROCESS_KEY, specifier.getCondition(), true);

    //when process is started
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey(CONDITIONAL_EVENT_PROCESS_KEY);

    //then start listener sets variable and
    //execution stays in task after conditional event in event sub process
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(procInst.getId());
    Task task = taskQuery.singleResult();
    assertEquals(TASK_AFTER_CONDITION, task.getName());
    tasksAfterVariableIsSet = taskQuery.list();
    assertEquals(specifier.getExpectedInterruptingCount(), taskQuery.taskName(TASK_AFTER_CONDITION).count());
  }
}
