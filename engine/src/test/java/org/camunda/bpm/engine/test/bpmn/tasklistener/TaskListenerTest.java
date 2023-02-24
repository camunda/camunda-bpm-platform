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
import static org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Attachment;
import org.camunda.bpm.engine.task.IdentityLinkType;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.bpmn.tasklistener.util.CompletingTaskListener;
import org.camunda.bpm.engine.test.bpmn.tasklistener.util.RecorderTaskListener;
import org.camunda.bpm.engine.test.bpmn.tasklistener.util.RecorderTaskListener.RecordedTaskEvent;
import org.camunda.bpm.engine.test.bpmn.tasklistener.util.TaskDeleteListener;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.commons.utils.IoUtil;
import org.joda.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * @author Joram Barrez
 */
public class TaskListenerTest extends AbstractTaskListenerTest {
  /*
  Testing use-cases when Task Events are thrown and caught by Task Listeners
   */

  @Before
  public void resetListenerCounters() {
    VariablesCollectingListener.reset();
  }

  // CREATE Task Listener tests

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/tasklistener/TaskListenerTest.bpmn20.xml"})
  public void testTaskCreateListener() {
    runtimeService.startProcessInstanceByKey("taskListenerProcess");
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("Schedule meeting", task.getName());
    assertEquals("TaskCreateListener is listening!", task.getDescription());
  }

  @Test
  public void testCompleteTaskInCreateEventTaskListener() {
    // given process with user task and task create listener
    BpmnModelInstance modelInstance =
        Bpmn.createExecutableProcess("startToEnd")
            .startEvent()
            .userTask()
            .camundaTaskListenerClass(TaskListener.EVENTNAME_CREATE, CompletingTaskListener.class.getName())
            .name("userTask")
            .endEvent().done();

    testRule.deploy(modelInstance);

    // when process is started and user task completed in task create listener
    runtimeService.startProcessInstanceByKey("startToEnd");

    // then task is successfully completed without an exception
    assertNull(taskService.createTaskQuery().singleResult());
  }

  @Test
  public void testCompleteTaskInCreateEventTaskListenerWithIdentityLinks() {
    // given process with user task, identity links and task create listener
    BpmnModelInstance modelInstance =
        Bpmn.createExecutableProcess("startToEnd")
            .startEvent()
            .userTask()
            .camundaTaskListenerClass(TaskListener.EVENTNAME_CREATE, CompletingTaskListener.class.getName())
            .name("userTask")
            .camundaCandidateUsers(Arrays.asList(new String[]{"users1", "user2"}))
            .camundaCandidateGroups(Arrays.asList(new String[]{"group1", "group2"}))
            .endEvent().done();

    testRule.deploy(modelInstance);

    // when process is started and user task completed in task create listener
    runtimeService.startProcessInstanceByKey("startToEnd");

    // then task is successfully completed without an exception
    assertNull(taskService.createTaskQuery().singleResult());
  }

  @Test
  public void testCompleteTaskInCreateEventListenerWithFollowingCallActivity() {
    final BpmnModelInstance subProcess = Bpmn.createExecutableProcess("subProc")
                                             .startEvent()
                                             .userTask("calledTask")
                                             .endEvent()
                                             .done();

    final BpmnModelInstance instance = Bpmn.createExecutableProcess("mainProc")
                                           .startEvent()
                                           .userTask("mainTask")
                                           .camundaTaskListenerClass(TaskListener.EVENTNAME_CREATE, CompletingTaskListener.class.getName())
                                           .callActivity().calledElement("subProc")
                                           .endEvent()
                                           .done();

    testRule.deploy(subProcess, instance);

    runtimeService.startProcessInstanceByKey("mainProc");
    Task task = taskService.createTaskQuery().singleResult();

    Assert.assertEquals(task.getTaskDefinitionKey(), "calledTask");
  }

  // COMPLETE Task Listener tests

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/tasklistener/TaskListenerTest.bpmn20.xml"})
  public void testTaskCompleteListener() {
    TaskDeleteListener.clear();
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskListenerProcess");
    assertEquals(null, runtimeService.getVariable(processInstance.getId(), "greeting"));
    assertEquals(null, runtimeService.getVariable(processInstance.getId(), "expressionValue"));

    // Completing first task will change the description
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    // Check that the completion did not execute the delete listener
    assertEquals(0, TaskDeleteListener.eventCounter);
    assertNull(TaskDeleteListener.lastTaskDefinitionKey);
    assertNull(TaskDeleteListener.lastDeleteReason);

    assertEquals("Hello from The Process", runtimeService.getVariable(processInstance.getId(), "greeting"));
    assertEquals("Act", runtimeService.getVariable(processInstance.getId(), "shortName"));
  }

  // DELETE Task Listener tests

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/tasklistener/TaskListenerTest.bpmn20.xml"})
  public void testTaskDeleteListenerByProcessDeletion() {
    TaskDeleteListener.clear();
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskListenerProcess");

    assertEquals(0, TaskDeleteListener.eventCounter);
    assertNull(TaskDeleteListener.lastTaskDefinitionKey);
    assertNull(TaskDeleteListener.lastDeleteReason);

    // delete process instance to delete task
    Task task = taskService.createTaskQuery().singleResult();
    runtimeService.deleteProcessInstance(processInstance.getProcessInstanceId(), "test delete task listener");

    assertEquals(1, TaskDeleteListener.eventCounter);
    assertEquals(task.getTaskDefinitionKey(), TaskDeleteListener.lastTaskDefinitionKey);
    assertEquals("test delete task listener", TaskDeleteListener.lastDeleteReason);
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/tasklistener/TaskListenerTest.bpmn20.xml"})
  public void testTaskDeleteListenerByBoundaryEvent() {
    TaskDeleteListener.clear();
    runtimeService.startProcessInstanceByKey("taskListenerProcess");

    assertEquals(0, TaskDeleteListener.eventCounter);
    assertNull(TaskDeleteListener.lastTaskDefinitionKey);
    assertNull(TaskDeleteListener.lastDeleteReason);

    // correlate message to delete task
    Task task = taskService.createTaskQuery().singleResult();
    runtimeService.correlateMessage("message");

    assertEquals(1, TaskDeleteListener.eventCounter);
    assertEquals(task.getTaskDefinitionKey(), TaskDeleteListener.lastTaskDefinitionKey);
    assertEquals("deleted", TaskDeleteListener.lastDeleteReason);
  }

  @Test
  public void testActivityInstanceIdOnDeleteInCalledProcess() {
    // given
    RecorderTaskListener.clear();

    BpmnModelInstance callActivityProcess = Bpmn.createExecutableProcess("calling")
                                                .startEvent()
                                                .callActivity()
                                                .calledElement("called")
                                                .endEvent()
                                                .done();

    BpmnModelInstance calledProcess = Bpmn.createExecutableProcess("called")
                                          .startEvent()
                                          .userTask()
                                          .camundaTaskListenerClass(TaskListener.EVENTNAME_CREATE, RecorderTaskListener.class.getName())
                                          .camundaTaskListenerClass(TaskListener.EVENTNAME_DELETE, RecorderTaskListener.class.getName())
                                          .endEvent()
                                          .done();

    testRule.deploy(callActivityProcess, calledProcess);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("calling");

    // when
    runtimeService.deleteProcessInstance(processInstance.getId(), null);

    // then
    List<RecordedTaskEvent> recordedEvents = RecorderTaskListener.getRecordedEvents();
    assertEquals(2, recordedEvents.size());
    String createActivityInstanceId = recordedEvents.get(0).getActivityInstanceId();
    String deleteActivityInstanceId = recordedEvents.get(1).getActivityInstanceId();

    assertEquals(createActivityInstanceId, deleteActivityInstanceId);
  }

  @Test
  public void testVariableAccessOnDeleteInCalledProcess() {
    // given
    VariablesCollectingListener.reset();

    BpmnModelInstance callActivityProcess = Bpmn.createExecutableProcess("calling")
                                                .startEvent()
                                                .callActivity()
                                                .camundaIn("foo", "foo")
                                                .calledElement("called")
                                                .endEvent()
                                                .done();

    BpmnModelInstance calledProcess = Bpmn.createExecutableProcess("called")
                                          .startEvent()
                                          .userTask()
                                          .camundaTaskListenerClass(TaskListener.EVENTNAME_DELETE, VariablesCollectingListener.class.getName())
                                          .endEvent()
                                          .done();

    testRule.deploy(callActivityProcess, calledProcess);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("calling",
                                                                               Variables.createVariables().putValue("foo", "bar"));

    // when
    runtimeService.deleteProcessInstance(processInstance.getId(), null);

    // then
    VariableMap collectedVariables = VariablesCollectingListener.getCollectedVariables();
    assertNotNull(collectedVariables);
    assertEquals(1, collectedVariables.size());
    assertEquals("bar", collectedVariables.get("foo"));
  }

  // Expression & Scripts Task Listener tests

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/tasklistener/TaskListenerTest.bpmn20.xml"})
  public void testTaskListenerWithExpression() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskListenerProcess");
    assertEquals(null, runtimeService.getVariable(processInstance.getId(), "greeting2"));

    // Completing first task will change the description
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    assertEquals("Write meeting notes", runtimeService.getVariable(processInstance.getId(), "greeting2"));
  }

  @Test
  @Deployment
  public void testScriptListener() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    assertTrue((Boolean) runtimeService.getVariable(processInstance.getId(), "create"));

    taskService.setAssignee(task.getId(), "test");
    assertTrue((Boolean) runtimeService.getVariable(processInstance.getId(), "assignment"));

    taskService.complete(task.getId());
    assertTrue((Boolean) runtimeService.getVariable(processInstance.getId(), "complete"));

    task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    runtimeService.deleteProcessInstance(processInstance.getId(), "test");

    if (processEngineConfiguration.getHistoryLevel().getId() >= HISTORYLEVEL_AUDIT) {
      HistoricVariableInstance variable = historyService.createHistoricVariableInstanceQuery().variableName("delete").singleResult();
      assertNotNull(variable);
      assertTrue((Boolean) variable.getValue());
    }
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/tasklistener/TaskListenerTest.testScriptResourceListener.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/tasklistener/taskListener.groovy"
  })
  public void testScriptResourceListener() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    assertTrue((Boolean) runtimeService.getVariable(processInstance.getId(), "create"));

    taskService.setAssignee(task.getId(), "test");
    assertTrue((Boolean) runtimeService.getVariable(processInstance.getId(), "assignment"));

    taskService.complete(task.getId());
    assertTrue((Boolean) runtimeService.getVariable(processInstance.getId(), "complete"));

    task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    runtimeService.deleteProcessInstance(processInstance.getId(), "test");

    if (processEngineConfiguration.getHistoryLevel().getId() >= HISTORYLEVEL_AUDIT) {
      HistoricVariableInstance variable = historyService.createHistoricVariableInstanceQuery().variableName("delete").singleResult();
      assertNotNull(variable);
      assertTrue((Boolean) variable.getValue());
    }
  }

  // UPDATE Task Listener tests

  @Test
  public void testUpdateTaskListenerOnAssign() {
    // given
    createAndDeployModelWithTaskEventsRecorderOnUserTask(TaskListener.EVENTNAME_UPDATE);
    runtimeService.startProcessInstanceByKey("process");
    Task task = taskService.createTaskQuery().singleResult();

    // when
    taskService.setAssignee(task.getId(), "gonzo");
    taskService.setAssignee(task.getId(), "leelo");

    // then
    assertEquals(2, RecorderTaskListener.getEventCount(TaskListener.EVENTNAME_UPDATE));
  }

  @Test
  public void testUpdateTaskListenerOnOwnerSet() {
    // given
    createAndDeployModelWithTaskEventsRecorderOnUserTask(TaskListener.EVENTNAME_UPDATE);
    runtimeService.startProcessInstanceByKey("process");
    Task task = taskService.createTaskQuery().singleResult();

    // when
    taskService.setOwner(task.getId(), "gonzo");

    // then
    assertEquals(1, RecorderTaskListener.getEventCount(TaskListener.EVENTNAME_UPDATE));
  }

  @Test
  public void testUpdateTaskListenerOnUserIdLinkAdd() {
    // given
    createAndDeployModelWithTaskEventsRecorderOnUserTask(TaskListener.EVENTNAME_UPDATE);
    runtimeService.startProcessInstanceByKey("process");
    Task task = taskService.createTaskQuery().singleResult();

    // when
    taskService.addUserIdentityLink(task.getId(), "gonzo", IdentityLinkType.CANDIDATE);

    // then
    assertEquals(1, RecorderTaskListener.getEventCount(TaskListener.EVENTNAME_UPDATE));
  }

  @Test
  public void testUpdateTaskListenerOnUserIdLinkDelete() {
    // given
    createAndDeployModelWithTaskEventsRecorderOnUserTask(TaskListener.EVENTNAME_UPDATE);
    runtimeService.startProcessInstanceByKey("process");
    Task task = taskService.createTaskQuery().singleResult();
    taskService.addUserIdentityLink(task.getId(), "gonzo", IdentityLinkType.CANDIDATE);

    // when
    taskService.deleteUserIdentityLink(task.getId(), "gonzo", IdentityLinkType.CANDIDATE);

    // then
    assertEquals(2, RecorderTaskListener.getEventCount(TaskListener.EVENTNAME_UPDATE));
  }

  @Test
  public void testUpdateTaskListenerOnGroupIdLinkAdd() {
    // given
    createAndDeployModelWithTaskEventsRecorderOnUserTask(TaskListener.EVENTNAME_UPDATE);
    runtimeService.startProcessInstanceByKey("process");
    Task task = taskService.createTaskQuery().singleResult();

    // when
    taskService.addGroupIdentityLink(task.getId(), "admins", IdentityLinkType.CANDIDATE);

    // then
    assertEquals(1, RecorderTaskListener.getEventCount(TaskListener.EVENTNAME_UPDATE));
  }

  @Test
  public void testUpdateTaskListenerOnGroupIdLinkDelete() {
    // given
    createAndDeployModelWithTaskEventsRecorderOnUserTask(TaskListener.EVENTNAME_UPDATE);
    runtimeService.startProcessInstanceByKey("process");
    Task task = taskService.createTaskQuery().singleResult();
    taskService.addGroupIdentityLink(task.getId(), "admins", IdentityLinkType.CANDIDATE);

    // when
    taskService.deleteGroupIdentityLink(task.getId(), "admins", IdentityLinkType.CANDIDATE);

    // then
    assertEquals(2, RecorderTaskListener.getEventCount(TaskListener.EVENTNAME_UPDATE));
  }

  @Test
  public void testUpdateTaskListenerOnTaskResolve() {
    // given
    createAndDeployModelWithTaskEventsRecorderOnUserTask(TaskListener.EVENTNAME_UPDATE);
    runtimeService.startProcessInstanceByKey("process");
    Task task = taskService.createTaskQuery().singleResult();

    // when
    taskService.resolveTask(task.getId());

    // then
    assertEquals(1, RecorderTaskListener.getEventCount(TaskListener.EVENTNAME_UPDATE));
  }

  @Test
  public void testUpdateTaskListenerOnDelegate() {
    // given
    createAndDeployModelWithTaskEventsRecorderOnUserTask(TaskListener.EVENTNAME_UPDATE);
    runtimeService.startProcessInstanceByKey("process");
    Task task = taskService.createTaskQuery().singleResult();

    // when
    taskService.delegateTask(task.getId(), "gonzo");

    // then
    assertEquals(1, RecorderTaskListener.getEventCount(TaskListener.EVENTNAME_UPDATE));
  }

  @Test
  public void testUpdateTaskListenerOnClaim() {
    // given
    createAndDeployModelWithTaskEventsRecorderOnUserTask(TaskListener.EVENTNAME_UPDATE);
    runtimeService.startProcessInstanceByKey("process");
    Task task = taskService.createTaskQuery().singleResult();

    // when
    taskService.claim(task.getId(), "test");

    // then
    assertEquals(1, RecorderTaskListener.getEventCount(TaskListener.EVENTNAME_UPDATE));
  }

  @Test
  public void testUpdateTaskListenerOnPrioritySet() {
    // given
    createAndDeployModelWithTaskEventsRecorderOnUserTask(TaskListener.EVENTNAME_UPDATE);
    runtimeService.startProcessInstanceByKey("process");
    Task task = taskService.createTaskQuery().singleResult();

    // when
    taskService.setPriority(task.getId(), 3000);

    // then
    assertEquals(1, RecorderTaskListener.getEventCount(TaskListener.EVENTNAME_UPDATE));
  }

  @Test
  public void testUpdateTaskListenerOnTaskFormSubmit() {
    // given
    createAndDeployModelWithTaskEventsRecorderOnUserTask(TaskListener.EVENTNAME_UPDATE);
    engineRule.getRuntimeService().startProcessInstanceByKey("process");
    Task task = engineRule.getTaskService().createTaskQuery().singleResult();

    // when
    taskService.delegateTask(task.getId(), "john");
    processEngineConfiguration.getFormService().submitTaskForm(task.getId(), null);

    // then
    // first update event comes from delegating the task,
    // setting it's delegation state to PENDING
    assertEquals(2, RecorderTaskListener.getEventCount(TaskListener.EVENTNAME_UPDATE));
  }

  @Test
  public void testUpdateTaskListenerOnPropertyUpdate() {
    // given
    createAndDeployModelWithTaskEventsRecorderOnUserTask(TaskListener.EVENTNAME_UPDATE);
    runtimeService.startProcessInstanceByKey("process");
    Task task = taskService.createTaskQuery().singleResult();

    // when
    task.setDueDate(new Date());
    taskService.saveTask(task);

    // then
    assertEquals(1, RecorderTaskListener.getEventCount(TaskListener.EVENTNAME_UPDATE));
  }

  @Test
  public void testUpdateTaskListenerOnPropertyUpdateOnlyOnce() {
    // given
    createAndDeployModelWithTaskEventsRecorderOnUserTask(TaskListener.EVENTNAME_UPDATE);
    runtimeService.startProcessInstanceByKey("process");
    Task task = taskService.createTaskQuery().singleResult();

    // when
    task.setAssignee("test");
    task.setDueDate(new Date());
    task.setOwner("test");
    taskService.saveTask(task);

    // then
    assertEquals(1, RecorderTaskListener.getEventCount(TaskListener.EVENTNAME_UPDATE));
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
  @Test
  public void testUpdateTaskListenerOnCommentCreate() {
    // given
    createAndDeployModelWithTaskEventsRecorderOnUserTask(TaskListener.EVENTNAME_UPDATE);
    runtimeService.startProcessInstanceByKey("process");
    Task task = taskService.createTaskQuery().singleResult();

    // when
    taskService.createComment(task.getId(), null, "new comment");

    // then
    assertEquals(1, RecorderTaskListener.getTotalEventCount());
    assertEquals(1, RecorderTaskListener.getEventCount(TaskListener.EVENTNAME_UPDATE));
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
  @Test
  public void testUpdateTaskListenerOnCommentAdd() {
    // given
    createAndDeployModelWithTaskEventsRecorderOnUserTask(TaskListener.EVENTNAME_UPDATE);
    runtimeService.startProcessInstanceByKey("process");
    Task task = taskService.createTaskQuery().singleResult();

    // when
    taskService.addComment(task.getId(), null, "new comment");

    // then
    assertEquals(1, RecorderTaskListener.getTotalEventCount());
    assertEquals(1, RecorderTaskListener.getEventCount(TaskListener.EVENTNAME_UPDATE));
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
  @Test
  public void testUpdateTaskListenerOnAttachmentCreate() {
    // given
    createAndDeployModelWithTaskEventsRecorderOnUserTask(TaskListener.EVENTNAME_UPDATE);
    runtimeService.startProcessInstanceByKey("process");
    Task task = taskService.createTaskQuery().singleResult();

    // when
    taskService.createAttachment("foo", task.getId(), null, "bar", "baz", IoUtil.stringAsInputStream("foo"));

    // then
    assertEquals(1, RecorderTaskListener.getTotalEventCount());
    assertEquals(1, RecorderTaskListener.getEventCount(TaskListener.EVENTNAME_UPDATE));
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
  @Test
  public void testUpdateTaskListenerOnAttachmentUpdate() {
    // given
    createAndDeployModelWithTaskEventsRecorderOnUserTask(TaskListener.EVENTNAME_UPDATE);
    runtimeService.startProcessInstanceByKey("process");
    Task task = taskService.createTaskQuery().singleResult();

    Attachment attachment = taskService.createAttachment("foo", task.getId(), null, "bar", "baz", IoUtil.stringAsInputStream("foo"));
    attachment.setDescription("bla");
    attachment.setName("foo");

    // when
    taskService.saveAttachment(attachment);

    // then
    assertEquals(2, RecorderTaskListener.getTotalEventCount());
    assertEquals(2, RecorderTaskListener.getEventCount(TaskListener.EVENTNAME_UPDATE)); // create and update attachment
  }

  @Test
  public void testUpdateTaskListenerOnAttachmentDelete() {
    // given
    createAndDeployModelWithTaskEventsRecorderOnUserTask(TaskListener.EVENTNAME_UPDATE);
    runtimeService.startProcessInstanceByKey("process");
    Task task = taskService.createTaskQuery().singleResult();

    Attachment attachment = taskService.createAttachment("foo", task.getId(), null, "bar", "baz", IoUtil.stringAsInputStream("foo"));

    // when
    taskService.deleteAttachment(attachment.getId());

    // then
    assertEquals(2, RecorderTaskListener.getTotalEventCount());
    assertEquals(2, RecorderTaskListener.getEventCount(TaskListener.EVENTNAME_UPDATE)); // create and delete attachment
  }

  @Test
  public void testUpdateTaskListenerOnAttachmentDeleteWithTaskId() {
    // given
    createAndDeployModelWithTaskEventsRecorderOnUserTask(TaskListener.EVENTNAME_UPDATE);
    runtimeService.startProcessInstanceByKey("process");
    Task task = taskService.createTaskQuery().singleResult();

    Attachment attachment = taskService.createAttachment("foo", task.getId(), null, "bar", "baz", IoUtil.stringAsInputStream("foo"));

    // when
    taskService.deleteTaskAttachment(task.getId(), attachment.getId());

    // then
    assertEquals(2, RecorderTaskListener.getTotalEventCount());
    assertEquals(2, RecorderTaskListener.getEventCount(TaskListener.EVENTNAME_UPDATE)); // create and delete attachment
  }

  @Test
  public void testUpdateTaskListenerOnSetLocalVariable() {
    // given
    createAndDeployModelWithTaskEventsRecorderOnUserTask(TaskListener.EVENTNAME_UPDATE);
    runtimeService.startProcessInstanceByKey("process");
    Task task = taskService.createTaskQuery().singleResult();

    // when
    taskService.setVariableLocal(task.getId(), "foo", "bar");

    // then
    assertEquals(1, RecorderTaskListener.getTotalEventCount());
    assertEquals(1, RecorderTaskListener.getEventCount(TaskListener.EVENTNAME_UPDATE));
  }

  @Test
  public void testUpdateTaskListenerOnSetLocalVariables() {
    // given
    createAndDeployModelWithTaskEventsRecorderOnUserTask(TaskListener.EVENTNAME_UPDATE);
    runtimeService.startProcessInstanceByKey("process");
    Task task = taskService.createTaskQuery().singleResult();

    VariableMap variables = Variables.createVariables()
        .putValue("var1", "val1")
        .putValue("var2", "val2");

    // when
    taskService.setVariablesLocal(task.getId(), variables);

    // then
    // only a single invocation of the listener is triggered
    assertEquals(1, RecorderTaskListener.getTotalEventCount());
    assertEquals(1, RecorderTaskListener.getEventCount(TaskListener.EVENTNAME_UPDATE));
  }

  @Test
  public void testUpdateTaskListenerOnSetVariableInTaskScope() {
    // given
    createAndDeployModelWithTaskEventsRecorderOnUserTask(TaskListener.EVENTNAME_UPDATE);
    runtimeService.startProcessInstanceByKey("process");
    Task task = taskService.createTaskQuery().singleResult();
    taskService.setVariableLocal(task.getId(), "foo", "bar");

    // when
    taskService.setVariable(task.getId(), "foo", "bar");

    // then
    assertEquals(2, RecorderTaskListener.getTotalEventCount());
    assertEquals(2, RecorderTaskListener.getEventCount(TaskListener.EVENTNAME_UPDATE)); // local and non-local
  }

  @Test
  public void testUpdateTaskListenerOnSetVariableInHigherScope() {
    // given
    createAndDeployModelWithTaskEventsRecorderOnUserTask(TaskListener.EVENTNAME_UPDATE);
    runtimeService.startProcessInstanceByKey("process");
    Task task = taskService.createTaskQuery().singleResult();

    // when
    taskService.setVariable(task.getId(), "foo", "bar");

    // then
    assertEquals(0, RecorderTaskListener.getTotalEventCount());
    assertEquals(0, RecorderTaskListener.getEventCount(TaskListener.EVENTNAME_UPDATE));
  }

  @Test
  public void testUpdateTaskListenerInvokedBeforeConditionalEventsOnSetVariable() {
    // given
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("process")
      .startEvent()
      .userTask("task")
        .camundaTaskListenerClass(TaskListener.EVENTNAME_UPDATE, RecorderTaskListener.class)
      .boundaryEvent()
        .condition("${triggerBoundaryEvent}")
      .userTask("afterBoundaryEvent")
        .camundaTaskListenerClass(TaskListener.EVENTNAME_CREATE, RecorderTaskListener.class)
      .endEvent()
      .moveToActivity("task")
      .endEvent()
      .done();

    testRule.deploy(modelInstance);

    runtimeService.startProcessInstanceByKey("process");
    Task task = taskService.createTaskQuery().singleResult();
    taskService.setVariableLocal(task.getId(), "taskLocalVariable", "bar");
    RecorderTaskListener.clear();

    VariableMap variables = Variables.createVariables().putValue("triggerBoundaryEvent", true).putValue("taskLocalVariable", "baz");

    // when
    taskService.setVariables(task.getId(), variables);

    // then
    assertThat(RecorderTaskListener.getOrderedEvents()).containsExactly(TaskListener.EVENTNAME_UPDATE, TaskListener.EVENTNAME_CREATE);
  }

  @Test
  public void testAssignmentTaskListenerWhenSavingTask() {
    // given
    createAndDeployModelWithTaskEventsRecorderOnUserTask(TaskListener.EVENTNAME_ASSIGNMENT);
    runtimeService.startProcessInstanceByKey("process");
    Task task = taskService.createTaskQuery().singleResult();

    // when
    task.setAssignee("gonzo");
    taskService.saveTask(task);

    // then
    assertEquals(1, RecorderTaskListener.getEventCount(TaskListener.EVENTNAME_ASSIGNMENT));
  }

  // TIMEOUT listener tests

  @Test
  @Deployment
  public void testTimeoutTaskListenerDuration() {
    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("process");

    // when
    ClockUtil.offset(TimeUnit.MINUTES.toMillis(70L));
    testRule.waitForJobExecutorToProcessAllJobs(5000L);

    // then
    assertThat(runtimeService.getVariable(instance.getId(), "timeout-status")).isEqualTo("fired");
  }

  @Test
  @Deployment
  public void testTimeoutTaskListenerDate() throws ParseException {
    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("process");

    // when
    ClockUtil.setCurrentTime(new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss").parse("2019-09-09T13:00:00"));
    testRule.waitForJobExecutorToProcessAllJobs(5000L);

    // then
    assertThat(runtimeService.getVariable(instance.getId(), "timeout-status")).isEqualTo("fired");
  }

  @Test
  @Deployment
  public void testTimeoutTaskListenerCycle() {
    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("process");

    // when
    ClockUtil.offset(TimeUnit.MINUTES.toMillis(70L));
    testRule.waitForJobExecutorToProcessAllJobs(5000L);
    ClockUtil.offset(TimeUnit.MINUTES.toMillis(130L));
    testRule.waitForJobExecutorToProcessAllJobs(5000L);

    // then
    assertThat(runtimeService.getVariable(instance.getId(), "timeout-status")).isEqualTo("fired2");
  }

  @Test
  @Deployment
  public void testMultipleTimeoutTaskListeners() {
    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("process");

    // assume
    assertThat(managementService.createJobQuery().count()).isEqualTo(2L);

    // when
    ClockUtil.offset(TimeUnit.MINUTES.toMillis(70L));
    testRule.waitForJobExecutorToProcessAllJobs(5000L);

    // then
    assertThat(managementService.createJobQuery().count()).isEqualTo(1L);
    assertThat(runtimeService.getVariable(instance.getId(), "timeout-status")).isEqualTo("fired");
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/tasklistener/TaskListenerTest.testTimeoutTaskListenerDuration.bpmn20.xml")
  public void testTimeoutTaskListenerNotCalledWhenTaskCompleted() {
    // given
    JobQuery jobQuery = managementService.createJobQuery();
    TaskQuery taskQuery = taskService.createTaskQuery();
    runtimeService.startProcessInstanceByKey("process");

    // assume
    assertThat(jobQuery.count()).isEqualTo(1L);

    // when
    taskService.complete(taskQuery.singleResult().getId());

    // then
    HistoricVariableInstanceQuery variableQuery = historyService.createHistoricVariableInstanceQuery().variableName("timeout-status");
    assertThat(variableQuery.count()).isEqualTo(0L);
    assertThat(jobQuery.count()).isEqualTo(0L);
  }

  @Test
  @Deployment
  public void testTimeoutTaskListenerNotCalledWhenTaskCompletedByBoundaryEvent() {
    // given
    JobQuery jobQuery = managementService.createJobQuery();
    runtimeService.startProcessInstanceByKey("process");

    // assume
    assertThat(jobQuery.count()).isEqualTo(2L);

    // when the boundary event is triggered
    ClockUtil.offset(TimeUnit.MINUTES.toMillis(70L));
    testRule.waitForJobExecutorToProcessAllJobs(5000L);

    // then
    HistoricVariableInstanceQuery variableQuery = historyService.createHistoricVariableInstanceQuery().variableName("timeout-status");
    assertThat(variableQuery.count()).isEqualTo(0L);
    assertThat(jobQuery.count()).isEqualTo(0L);
  }

  @Test
  @Deployment
  public void testRecalculateTimeoutTaskListenerDuedateCreationDateBased() {
    // given
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process", Variables.putValue("duration", "PT1H"));

    JobQuery jobQuery = managementService.createJobQuery().processInstanceId(pi.getId());
    List<Job> jobs = jobQuery.list();
    assertEquals(1, jobs.size());
    Job job = jobs.get(0);
    Date oldDate = job.getDuedate();

    // when
    runtimeService.setVariable(pi.getId(), "duration", "PT15M");
    managementService.recalculateJobDuedate(job.getId(), true);

    // then
    Job jobUpdated = jobQuery.singleResult();
    assertEquals(job.getId(), jobUpdated.getId());
    assertNotEquals(oldDate, jobUpdated.getDuedate());
    assertTrue(oldDate.after(jobUpdated.getDuedate()));
    assertEquals(LocalDateTime.fromDateFields(jobUpdated.getCreateTime()).plusMinutes(15).toDate(), jobUpdated.getDuedate());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/tasklistener/TaskListenerTest.testRecalculateTimeoutTaskListenerDuedateCreationDateBased.bpmn20.xml")
  public void testRecalculateTimeoutTaskListenerDuedateCurrentDateBased() {
    // given
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process", Variables.putValue("duration", "PT1H"));

    JobQuery jobQuery = managementService.createJobQuery().processInstanceId(pi.getId());
    List<Job> jobs = jobQuery.list();
    assertEquals(1, jobs.size());
    Job job = jobs.get(0);
    Date oldDate = job.getDuedate();
    ClockUtil.offset(2000L);

    // when
    managementService.recalculateJobDuedate(job.getId(), false);

    // then
    Job jobUpdated = jobQuery.singleResult();
    assertEquals(job.getId(), jobUpdated.getId());
    assertNotEquals(oldDate, jobUpdated.getDuedate());
    assertTrue(oldDate.before(jobUpdated.getDuedate()));
  }

  @Test
  @Deployment
  public void testRecalculateTimeoutTaskListenerDuedateCreationDateBasedWithDefinedBoundaryEvent() {
    // given
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process", Variables.putValue("duration", "PT1H"));

    JobQuery jobQuery = managementService.createJobQuery()
        .processInstanceId(pi.getId())
        .activityId("userTask");
    List<Job> jobs = jobQuery.list();
    assertEquals(1, jobs.size());
    Job job = jobs.get(0);
    Date oldDate = job.getDuedate();

    // when
    runtimeService.setVariable(pi.getId(), "duration", "PT15M");
    managementService.recalculateJobDuedate(job.getId(), true);

    // then
    Job jobUpdated = jobQuery.singleResult();
    assertEquals(job.getId(), jobUpdated.getId());
    assertNotEquals(oldDate, jobUpdated.getDuedate());
    assertTrue(oldDate.after(jobUpdated.getDuedate()));
    assertEquals(LocalDateTime.fromDateFields(jobUpdated.getCreateTime()).plusMinutes(15).toDate(), jobUpdated.getDuedate());
  }

  // Helper methods


  public static class VariablesCollectingListener implements TaskListener {

    protected static VariableMap collectedVariables;

    @Override
    public void notify(DelegateTask delegateTask) {
      collectedVariables = delegateTask.getVariablesTyped();
    }

    public static VariableMap getCollectedVariables() {
      return collectedVariables;
    }

    public static void reset() {
      collectedVariables = null;
    }

  }
}
