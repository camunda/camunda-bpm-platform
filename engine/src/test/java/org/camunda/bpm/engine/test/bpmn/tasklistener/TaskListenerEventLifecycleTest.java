/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.bpmn.tasklistener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.bpmn.tasklistener.util.AssigneeAssignment;
import org.camunda.bpm.engine.test.bpmn.tasklistener.util.CandidateUserAssignment;
import org.camunda.bpm.engine.test.bpmn.tasklistener.util.CompletingTaskListener;
import org.camunda.bpm.engine.test.bpmn.tasklistener.util.RecorderTaskListener;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Test;

public class TaskListenerEventLifecycleTest extends AbstractTaskListenerTest{
  /*
  Testing Task Event chains to validate event lifecycle order

  Note: completing tasks inside TaskListeners breaks the global task event lifecycle. However,
  task events are still fired in the right order inside the listener "scope".
   */

  protected static final String[] TRACKED_EVENTS = {
      TaskListener.EVENTNAME_CREATE,
      TaskListener.EVENTNAME_UPDATE,
      TaskListener.EVENTNAME_ASSIGNMENT,
      TaskListener.EVENTNAME_COMPLETE,
      TaskListener.EVENTNAME_DELETE
  };

  // CREATE phase

  @Test
  public void shouldOnlyFireCreateAndAssignmentEventsWhenTaskIsCreated() {
    // given
    createAndDeployModelWithTaskEventsRecorderOnUserTaskWithAssignee("kermit", TRACKED_EVENTS);

    // when
    runtimeService.startProcessInstanceByKey("process");

    // then
    List<String> orderedEvents = RecorderTaskListener.getOrderedEvents();
    assertThat(orderedEvents.size()).isEqualTo(2);
    assertThat(orderedEvents).containsExactly(TaskListener.EVENTNAME_CREATE,
                                              TaskListener.EVENTNAME_ASSIGNMENT);
  }

  @Test
  public void shouldFireCompleteEventOnTaskCompletedInCreateListener() {
    // given
    BpmnModelInstance model = createModelWithTaskEventsRecorderOnAssignedUserTask(TRACKED_EVENTS,
                                                                                  null,
                                                                                  TaskListener.EVENTNAME_CREATE,
                                                                                  CompletingTaskListener.class);
    testRule.deploy(model);

    // when
    runtimeService.startProcessInstanceByKey("process");

    // then
    List<String> orderedEvents = RecorderTaskListener.getOrderedEvents();
    assertThat(orderedEvents.size()).isEqualTo(2);
    assertThat(orderedEvents).containsExactly(TaskListener.EVENTNAME_CREATE,
                                              TaskListener.EVENTNAME_COMPLETE);
  }

  @Test
  public void shouldFireCompleteEventOnTaskCompletedInAssignmentListenerWhenTaskCreated() {
    // given
    BpmnModelInstance model = createModelWithTaskEventsRecorderOnAssignedUserTask(TRACKED_EVENTS,
                                                                                  null,
                                                                                  TaskListener.EVENTNAME_ASSIGNMENT,
                                                                                  CompletingTaskListener.class);
    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey("process");
    Task task = taskService.createTaskQuery().singleResult();

    // when
    taskService.setAssignee(task.getId(), "gonzo");

    // then
    List<String> orderedEvents = RecorderTaskListener.getOrderedEvents();
    assertThat(orderedEvents.size()).isEqualTo(4);
    assertThat(orderedEvents).containsExactly(TaskListener.EVENTNAME_CREATE,
                                              TaskListener.EVENTNAME_UPDATE,
                                              TaskListener.EVENTNAME_ASSIGNMENT,
                                              TaskListener.EVENTNAME_COMPLETE);
  }

  @Test
  public void shouldFireCreateEventBeforeTimeoutEventWhenTaskCreated() {
    // given
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
    Calendar now = Calendar.getInstance();
    now.add(Calendar.HOUR, -1);
    BpmnModelInstance model = Bpmn.createExecutableProcess("process")
        .startEvent()
          .userTask("task")
            .camundaTaskListenerClass(TaskListener.EVENTNAME_CREATE, RecorderTaskListener.class)
            .camundaTaskListenerClassTimeoutWithDate(TaskListener.EVENTNAME_TIMEOUT,
                                                     RecorderTaskListener.class,
                                                     sdf.format(now.getTime()))
        .endEvent()
        .done();
    testRule.deploy(model);

    // when
    runtimeService.startProcessInstanceByKey("process");
    testRule.waitForJobExecutorToProcessAllJobs(0L);

    // then
    List<String> orderedEvents = RecorderTaskListener.getOrderedEvents();
    assertThat(orderedEvents.size()).isEqualTo(2);

    // the TIMEOUT event will always fire after the CREATE event, since the Timer Job can't be
    // picked up by the JobExecutor before it's committed. And it is committed in the same
    // transaction as the task creation phase.
    assertThat(orderedEvents).containsExactly(TaskListener.EVENTNAME_CREATE,
                                              TaskListener.EVENTNAME_TIMEOUT);
  }

  @Test
  public void shouldCancelTimeoutTaskListenerWhenTaskCompleted() {
    // given
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
    Calendar now = Calendar.getInstance();
    now.add(Calendar.MINUTE, 10);
    BpmnModelInstance model = Bpmn.createExecutableProcess("process")
                                  .startEvent()
                                  .userTask("task")
                                  .camundaTaskListenerClass(TaskListener.EVENTNAME_CREATE, RecorderTaskListener.class)
                                  .camundaTaskListenerClass(TaskListener.EVENTNAME_COMPLETE, RecorderTaskListener.class)
                                  .camundaTaskListenerClassTimeoutWithDate(TaskListener.EVENTNAME_TIMEOUT,
                                                                           RecorderTaskListener.class,
                                                                           sdf.format(now.getTime()))
                                  .endEvent()
                                  .done();
    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey("process");
    Task task = taskService.createTaskQuery().singleResult();

    // when
    long runningJobCount = managementService.createJobQuery().count();
    taskService.complete(task.getId());
    long completedJobCount = managementService.createJobQuery().count();

    // then
    LinkedList<String> orderedEvents = RecorderTaskListener.getOrderedEvents();
    assertThat(runningJobCount).isOne();
    assertThat(completedJobCount).isZero();
    assertThat(orderedEvents.size()).isEqualTo(2);
    assertThat(orderedEvents.getLast()).isEqualToIgnoringCase(TaskListener.EVENTNAME_COMPLETE);
  }

  // UPDATE phase

  @Test
  public void shouldFireUpdateEventOnPropertyChangeWhenTaskUpdated() {
    // given
    createAndDeployModelWithTaskEventsRecorderOnUserTask(TRACKED_EVENTS);
    runtimeService.startProcessInstanceByKey("process");
    Task task = taskService.createTaskQuery().singleResult();

    // when
    taskService.setOwner(task.getId(), "gonzo");

    // then
    List<String> orderedEvents = RecorderTaskListener.getOrderedEvents();
    // create event fired on task creation
    assertThat(orderedEvents.size()).isEqualTo(2);
    assertThat(orderedEvents).containsExactly(TaskListener.EVENTNAME_CREATE,
                                              TaskListener.EVENTNAME_UPDATE);
  }

  @Test
  public void shouldFireUpdateEventBeforeAssignmentEventOnSetAssigneeWhenTaskUpdated() {
    // given
    createAndDeployModelWithTaskEventsRecorderOnUserTask(TRACKED_EVENTS);
    runtimeService.startProcessInstanceByKey("process");
    Task task = taskService.createTaskQuery().singleResult();

    // when
    taskService.setAssignee(task.getId(), "gonzo");

    // then
    List<String> orderedEvents = RecorderTaskListener.getOrderedEvents();

    assertThat(orderedEvents.size()).isEqualTo(3);
    assertThat(orderedEvents).containsExactly(TaskListener.EVENTNAME_CREATE,
                                              TaskListener.EVENTNAME_UPDATE,
                                              TaskListener.EVENTNAME_ASSIGNMENT);
  }

  @Test
  public void shouldFireCompleteEventOnTaskCompletedInUpdateListener() {
    // given
    BpmnModelInstance model = createModelWithTaskEventsRecorderOnAssignedUserTask(TRACKED_EVENTS,
                                                                                  null,
                                                                                  TaskListener.EVENTNAME_UPDATE,
                                                                                  CompletingTaskListener.class);
    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey("process");
    Task task = taskService.createTaskQuery().singleResult();

    // when
    taskService.setPriority(task.getId(), 3000);

    // then
    List<String> orderedEvents = RecorderTaskListener.getOrderedEvents();

    // assignment event should not be processed
    assertThat(orderedEvents.size()).isEqualTo(3);
    assertThat(orderedEvents).containsExactly(TaskListener.EVENTNAME_CREATE,
                                              TaskListener.EVENTNAME_UPDATE,
                                              TaskListener.EVENTNAME_COMPLETE);
  }

  @Test
  public void shouldFireCompleteEventOnTaskCompletedInAssignmentListenerWhenTaskUpdated() {
    // given
    BpmnModelInstance model = createModelWithTaskEventsRecorderOnAssignedUserTask(TRACKED_EVENTS,
                                                                                  null,
                                                                                  TaskListener.EVENTNAME_ASSIGNMENT,
                                                                                  CompletingTaskListener.class);
    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey("process");
    Task task = taskService.createTaskQuery().singleResult();

    // when
    taskService.setAssignee(task.getId(), "kermit");

    // then
    List<String> orderedEvents = RecorderTaskListener.getOrderedEvents();

    assertThat(orderedEvents.size()).isEqualTo(4);
    assertThat(orderedEvents).containsExactly(TaskListener.EVENTNAME_CREATE,
                                              TaskListener.EVENTNAME_UPDATE,
                                              TaskListener.EVENTNAME_ASSIGNMENT,
                                              TaskListener.EVENTNAME_COMPLETE);
  }

  @Test
  public void shouldNotFireUpdateEventAfterCreateTaskListenerUpdatesProperties() {
    // given
    BpmnModelInstance process = createModelWithTaskEventsRecorderOnAssignedUserTask(TRACKED_EVENTS,
                                                                                  null,
                                                                                  TaskListener.EVENTNAME_CREATE,
                                                                                  ModifyingTaskListener.class);
    testRule.deploy(process);

    // when
    engineRule.getRuntimeService().startProcessInstanceByKey("process");

    // then
    List<String> orderedEvents = RecorderTaskListener.getOrderedEvents();

    assertThat(orderedEvents.size()).isEqualTo(2);
    // ASSIGNMENT Event is fired, since the ModifyingTaskListener sets an assignee, and the
    // ASSIGNMENT Event evaluation happens after the CREATE Event evaluation
    assertThat(orderedEvents).containsExactly(TaskListener.EVENTNAME_CREATE,
                                              TaskListener.EVENTNAME_ASSIGNMENT);
  }

  @Test
  public void shouldNotFireUpdateEventAfterUpdateTaskListenerUpdatesProperties() {
    // given
    BpmnModelInstance process = createModelWithTaskEventsRecorderOnAssignedUserTask(TRACKED_EVENTS,
                                                                                    null,
                                                                                    TaskListener.EVENTNAME_UPDATE,
                                                                                    ModifyingTaskListener.class);
    testRule.deploy(process);
    engineRule.getRuntimeService().startProcessInstanceByKey("process");
    Task task = engineRule.getTaskService().createTaskQuery().singleResult();

    // when
    taskService.setPriority(task.getId(), 3000);

    // then
    // only the initial, first update event is expected
    List<String> orderedEvents = RecorderTaskListener.getOrderedEvents();

    assertThat(orderedEvents.size()).isEqualTo(3);
    // ASSIGNMENT Event is fired, since the ModifyingTaskListener sets an assignee, and the
    // ASSIGNMENT Event evaluation happens after the UPDATE Event evaluation
    assertThat(orderedEvents).containsExactly(TaskListener.EVENTNAME_CREATE,
                                              TaskListener.EVENTNAME_UPDATE,
                                              TaskListener.EVENTNAME_ASSIGNMENT);
  }

  @Test
  public void shouldNotFireUpdateEventAfterAssignmentTaskListenerUpdatesProperties() {
    // given
    BpmnModelInstance process = createModelWithTaskEventsRecorderOnAssignedUserTask(TRACKED_EVENTS,
                                                                                    null,
                                                                                    TaskListener.EVENTNAME_ASSIGNMENT,
                                                                                    ModifyingTaskListener.class);
    testRule.deploy(process);
    engineRule.getRuntimeService().startProcessInstanceByKey("process");
    Task task = engineRule.getTaskService().createTaskQuery().singleResult();

    // when
    taskService.setAssignee(task.getId(), "john");

    // then
    // only one update event is expected, from the initial assignment
    List<String> orderedEvents = RecorderTaskListener.getOrderedEvents();

    assertThat(orderedEvents.size()).isEqualTo(3);
    assertThat(orderedEvents).containsExactly(TaskListener.EVENTNAME_CREATE,
                                              TaskListener.EVENTNAME_UPDATE,
                                              TaskListener.EVENTNAME_ASSIGNMENT);
  }

  @Test
  public void shouldNotFireUpdateEventAfterCompleteTaskListenerUpdatesProperties() {
    // given
    BpmnModelInstance process = createModelWithTaskEventsRecorderOnAssignedUserTask(TRACKED_EVENTS,
                                                                                    null,
                                                                                    TaskListener.EVENTNAME_COMPLETE,
                                                                                    ModifyingTaskListener.class);
    testRule.deploy(process);
    engineRule.getRuntimeService().startProcessInstanceByKey("process");
    Task task = engineRule.getTaskService().createTaskQuery().singleResult();

    // when
    taskService.complete(task.getId());

    // then
    List<String> orderedEvents = RecorderTaskListener.getOrderedEvents();

    assertThat(orderedEvents.size()).isEqualTo(2);
    assertThat(orderedEvents).containsExactly(TaskListener.EVENTNAME_CREATE,
                                              TaskListener.EVENTNAME_COMPLETE);
  }

  @Test
  public void shouldNotFireUpdateEventAfterDeleteTaskListenerUpdatesProperties() {
    // given
    BpmnModelInstance process = createModelWithTaskEventsRecorderOnAssignedUserTask(TRACKED_EVENTS,
                                                                                    null,
                                                                                    TaskListener.EVENTNAME_DELETE,
                                                                                    ModifyingTaskListener.class);
    testRule.deploy(process);
    ProcessInstance processInstance = engineRule.getRuntimeService()
                                                .startProcessInstanceByKey("process");

    // when
    runtimeService.deleteProcessInstance(processInstance.getId(), "Trigger Delete Event");

    // then
    List<String> orderedEvents = RecorderTaskListener.getOrderedEvents();

    assertThat(orderedEvents.size()).isEqualTo(2);
    assertThat(orderedEvents).containsExactly(TaskListener.EVENTNAME_CREATE,
                                              TaskListener.EVENTNAME_DELETE);
  }

  // COMPLETE phase

  @Test
  public void shouldFireCompleteEventLastWhenTaskCompleted() {
    // given
    createAndDeployModelWithTaskEventsRecorderOnUserTask(TRACKED_EVENTS);
    runtimeService.startProcessInstanceByKey("process");
    Task task = taskService.createTaskQuery().singleResult();

    // when
    taskService.complete(task.getId());

    // then
    LinkedList<String> orderedEvents = RecorderTaskListener.getOrderedEvents();

    assertThat(orderedEvents.size()).isEqualTo(2);
    assertThat(orderedEvents.getLast()).isEqualToIgnoringCase(TaskListener.EVENTNAME_COMPLETE);
  }

  @Test
  public void shouldNotFireUpdateEventOnPropertyChangesInCompleteListener() {
    // given
    BpmnModelInstance model = createModelWithTaskEventsRecorderOnAssignedUserTask(TRACKED_EVENTS,
                                                                                  null,
                                                                                  TaskListener.EVENTNAME_COMPLETE,
                                                                                  CandidateUserAssignment.class);
    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey("process");
    Task task = taskService.createTaskQuery().singleResult();

    // when
    taskService.complete(task.getId());

    // then
    LinkedList<String> orderedEvents = RecorderTaskListener.getOrderedEvents();

    assertThat(orderedEvents.size()).isEqualTo(2);
    assertThat(orderedEvents.getLast()).isEqualToIgnoringCase(TaskListener.EVENTNAME_COMPLETE);
  }

  @Test
  public void shouldNotFireAssignmentEventOnAssigneeChangesInCompleteListener() {
    // given
    BpmnModelInstance model = createModelWithTaskEventsRecorderOnAssignedUserTask(TRACKED_EVENTS,
                                                                                  null,
                                                                                  TaskListener.EVENTNAME_COMPLETE,
                                                                                  AssigneeAssignment.class);
    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey("process");
    Task task = taskService.createTaskQuery().singleResult();

    // when
    taskService.complete(task.getId());

    // then
    LinkedList<String> orderedEvents = RecorderTaskListener.getOrderedEvents();

    assertThat(orderedEvents.size()).isEqualTo(2);
    assertThat(orderedEvents.getLast()).isEqualToIgnoringCase(TaskListener.EVENTNAME_COMPLETE);
  }

  @Test
  public void shouldNotFireDeleteEventOnTaskDeletedInCompleteListener() {
    // given
    BpmnModelInstance model = createModelWithTaskEventsRecorderOnAssignedUserTask(TRACKED_EVENTS,
                                                                                  null,
                                                                                  TaskListener.EVENTNAME_COMPLETE,
                                                                                  TaskDeleteTaskListener.class);
    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey("process");
    Task task = taskService.createTaskQuery().singleResult();

    // when
    try {
      // when/then
      assertThatThrownBy(() -> taskService.complete(task.getId()))
        .isInstanceOf(ProcessEngineException.class)
        .hasMessageContaining("The task cannot be deleted because is part of a running process");
    } finally {
      // then
      LinkedList<String> orderedEvents = RecorderTaskListener.getOrderedEvents();

      assertThat(orderedEvents.size()).isEqualTo(2);
      assertThat(orderedEvents.getLast()).isEqualToIgnoringCase(TaskListener.EVENTNAME_COMPLETE);
    }
  }

  @Test
  public void shouldFireDeleteEventOnProcessInstanceDeletedInCompleteListener() {
    // given
    BpmnModelInstance model = createModelWithTaskEventsRecorderOnAssignedUserTask(TRACKED_EVENTS,
                                                                                  null,
                                                                                  TaskListener.EVENTNAME_COMPLETE,
                                                                                  ProcessInstanceDeleteTaskListener.class);
    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey("process");
    Task task = taskService.createTaskQuery().singleResult();

    // when
    taskService.complete(task.getId());

    // then
    LinkedList<String> orderedEvents = RecorderTaskListener.getOrderedEvents();

    assertThat(orderedEvents.size()).isEqualTo(3);
    assertThat(orderedEvents).containsExactly(TaskListener.EVENTNAME_CREATE,
                                              TaskListener.EVENTNAME_COMPLETE,
                                              TaskListener.EVENTNAME_DELETE);
  }

  @Test
  public void shouldNotFireCompleteEventOnTaskCompletedInCompleteListener() {
    // given
    BpmnModelInstance model = createModelWithTaskEventsRecorderOnAssignedUserTask(TRACKED_EVENTS,
                                                                                  null,
                                                                                  TaskListener.EVENTNAME_COMPLETE,
                                                                                  CompletingTaskListener.class);
    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey("process");
    Task task = taskService.createTaskQuery().singleResult();

    // when
    try {
      // when/then
      assertThatThrownBy(() -> taskService.complete(task.getId()))
        .isInstanceOf(ProcessEngineException.class)
        .hasMessageContaining("invalid task state");
    } finally {
      // then
      LinkedList<String> orderedEvents = RecorderTaskListener.getOrderedEvents();

      assertThat(orderedEvents.size()).isEqualTo(2);
      assertThat(orderedEvents.getFirst()).isEqualToIgnoringCase(TaskListener.EVENTNAME_CREATE);
      assertThat(orderedEvents.getLast()).isEqualToIgnoringCase(TaskListener.EVENTNAME_COMPLETE);
    }
  }

  // DELETE phase

  @Test
  public void shouldFireDeleteEventLastWhenProcessDeleted() {
    // given
    createAndDeployModelWithTaskEventsRecorderOnUserTask(TRACKED_EVENTS);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    // when
    runtimeService.deleteProcessInstance(processInstance.getId(), "Canceled!");

    // then
    LinkedList<String> orderedEvents = RecorderTaskListener.getOrderedEvents();

    assertThat(orderedEvents.size()).isEqualTo(2);
    assertThat(orderedEvents.getFirst()).isEqualToIgnoringCase(TaskListener.EVENTNAME_CREATE);
    assertThat(orderedEvents.getLast()).isEqualToIgnoringCase(TaskListener.EVENTNAME_DELETE);
  }

  @Test
  public void shouldNotFireUpdateEventOnPropertyChangesInDeleteListener() {
    // given
    BpmnModelInstance model = createModelWithTaskEventsRecorderOnAssignedUserTask(TRACKED_EVENTS,
                                                                                  null,
                                                                                  TaskListener.EVENTNAME_DELETE,
                                                                                  CandidateUserAssignment.class);
    testRule.deploy(model);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    // when
    runtimeService.deleteProcessInstance(processInstance.getId(), "Canceled!");

    // then
    LinkedList<String> orderedEvents = RecorderTaskListener.getOrderedEvents();

    assertThat(orderedEvents.size()).isEqualTo(2);
    assertThat(orderedEvents.getFirst()).isEqualToIgnoringCase(TaskListener.EVENTNAME_CREATE);
    assertThat(orderedEvents.getLast()).isEqualToIgnoringCase(TaskListener.EVENTNAME_DELETE);
  }

  @Test
  public void shouldNotFireAssignmentEventOnAssigneeChangesInDeleteListener() {
    // given
    BpmnModelInstance model = createModelWithTaskEventsRecorderOnAssignedUserTask(TRACKED_EVENTS,
                                                                                  null,
                                                                                  TaskListener.EVENTNAME_DELETE,
                                                                                  AssigneeAssignment.class);
    testRule.deploy(model);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    // when
    runtimeService.deleteProcessInstance(processInstance.getId(), "Canceled!");

    // then
    LinkedList<String> orderedEvents = RecorderTaskListener.getOrderedEvents();

    assertThat(orderedEvents.size()).isEqualTo(2);
    assertThat(orderedEvents.getFirst()).isEqualToIgnoringCase(TaskListener.EVENTNAME_CREATE);
    assertThat(orderedEvents.getLast()).isEqualToIgnoringCase(TaskListener.EVENTNAME_DELETE);
  }

  @Test
  public void shouldNotFireCompleteEventOnCompleteAttemptInDeleteListener() {
    // given
    BpmnModelInstance model = createModelWithTaskEventsRecorderOnAssignedUserTask(TRACKED_EVENTS,
                                                                                  null,
                                                                                  TaskListener.EVENTNAME_DELETE,
                                                                                  CompletingTaskListener.class);
    testRule.deploy(model);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    // when
    try {
      // when/then
      assertThatThrownBy(() -> runtimeService.deleteProcessInstance(processInstance.getId(), "Canceled!"))
        .isInstanceOf(ProcessEngineException.class)
        .hasMessageContaining("invalid task state");
    } finally {
      // then
      LinkedList<String> orderedEvents = RecorderTaskListener.getOrderedEvents();

      assertThat(orderedEvents.size()).isEqualTo(2);
      assertThat(orderedEvents.getFirst()).isEqualToIgnoringCase(TaskListener.EVENTNAME_CREATE);
      assertThat(orderedEvents.getLast()).isEqualToIgnoringCase(TaskListener.EVENTNAME_DELETE);
    }
  }

  @Test
  public void shouldNotFireDeleteEventOnTaskDeleteAttemptInDeleteListener() {
    // given
    BpmnModelInstance model = createModelWithTaskEventsRecorderOnAssignedUserTask(TRACKED_EVENTS,
                                                                                  null,
                                                                                  TaskListener.EVENTNAME_DELETE,
                                                                                  TaskDeleteTaskListener.class);
    testRule.deploy(model);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    // when
    try {
      // when/then
      assertThatThrownBy(() -> runtimeService.deleteProcessInstance(processInstance.getId(), "Canceled!"))
        .isInstanceOf(ProcessEngineException.class)
        .hasMessageContaining("The task cannot be deleted because is part of a running process");
    } finally {
      // then
      LinkedList<String> orderedEvents = RecorderTaskListener.getOrderedEvents();

      assertThat(orderedEvents.size()).isEqualTo(2);
      assertThat(orderedEvents.getFirst()).isEqualToIgnoringCase(TaskListener.EVENTNAME_CREATE);
      assertThat(orderedEvents.getLast()).isEqualToIgnoringCase(TaskListener.EVENTNAME_DELETE);
    }
  }

  @Test
  public void shouldNotFireDeleteEventOnProcessDeleteAttemptInDeleteListener() {
    // given
    BpmnModelInstance model = createModelWithTaskEventsRecorderOnAssignedUserTask(TRACKED_EVENTS,
                                                                                  null,
                                                                                  TaskListener.EVENTNAME_DELETE,
                                                                                  ProcessInstanceDeleteTaskListener.class);
    testRule.deploy(model);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    // when
    runtimeService.deleteProcessInstance(processInstance.getId(), "Canceled!");

    // then
    LinkedList<String> orderedEvents = RecorderTaskListener.getOrderedEvents();

    assertThat(orderedEvents.size()).isEqualTo(2);
    assertThat(orderedEvents.getFirst()).isEqualToIgnoringCase(TaskListener.EVENTNAME_CREATE);
    assertThat(orderedEvents.getLast()).isEqualToIgnoringCase(TaskListener.EVENTNAME_DELETE);
  }

  // HELPER methods and classes

  public static class ModifyingTaskListener implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
      delegateTask.setAssignee("demo");
      delegateTask.setOwner("john");
      delegateTask.setDueDate(new Date());
    }
  }

  public static class TaskDeleteTaskListener implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
      delegateTask.getProcessEngineServices().getTaskService().deleteTask(delegateTask.getId());
    }
  }

  public static class ProcessInstanceDeleteTaskListener implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
      delegateTask.getProcessEngineServices().getRuntimeService()
                  .deleteProcessInstance(delegateTask.getProcessInstanceId(), "Trigger a Task Delete event.");
    }
  }
}
