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
package org.camunda.bpm.engine.test.bpmn.event.message;

import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.describeActivityInstanceTree;
import static org.camunda.bpm.engine.test.util.ExecutionAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ExecutionAssert.describeExecutionTree;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.camunda.bpm.engine.impl.EventSubscriptionQueryImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.ExecutionTree;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.engine.test.util.TestExecutionListener;
import org.junit.After;
import org.junit.Test;


/**
 * @author Daniel Meyer
 * @author Falko Menge
 * @author Danny Gräf
 */
public class MessageEventSubprocessTest extends PluggableProcessEngineTest {

  @After
  public void tearDown() throws Exception {
    try {

    } finally {
      TestExecutionListener.reset();
    }
  }

  @Deployment
  @Test
  public void testInterruptingUnderProcessDefinition() {
    testInterruptingUnderProcessDefinition(1);
  }

  /**
   * Checks if unused event subscriptions are properly deleted.
   */
  @Deployment
  @Test
  public void testTwoInterruptingUnderProcessDefinition() {
    testInterruptingUnderProcessDefinition(2);
  }

  private void testInterruptingUnderProcessDefinition(int expectedNumberOfEventSubscriptions) {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    // the process instance must have a message event subscription:
    Execution execution = runtimeService.createExecutionQuery()
        .executionId(processInstance.getId())
        .messageEventSubscriptionName("newMessage")
        .singleResult();
    assertNotNull(execution);
    assertEquals(expectedNumberOfEventSubscriptions, createEventSubscriptionQuery().count());
    assertEquals(1, runtimeService.createExecutionQuery().count());

    // if we trigger the usertask, the process terminates and the event subscription is removed:
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("task", task.getTaskDefinitionKey());
    taskService.complete(task.getId());
    testRule.assertProcessEnded(processInstance.getId());
    assertEquals(0, createEventSubscriptionQuery().count());
    assertEquals(0, runtimeService.createExecutionQuery().count());

    // now we start a new instance but this time we trigger the event subprocess:
    processInstance = runtimeService.startProcessInstanceByKey("process");
    runtimeService.messageEventReceived("newMessage", processInstance.getId());

    task = taskService.createTaskQuery().singleResult();
    assertEquals("eventSubProcessTask", task.getTaskDefinitionKey());
    taskService.complete(task.getId());
    testRule.assertProcessEnded(processInstance.getId());
    assertEquals(0, createEventSubscriptionQuery().count());
    assertEquals(0, runtimeService.createExecutionQuery().count());
  }

  @Deployment
  @Test
  public void testEventSubprocessListenersInvoked() {
    runtimeService.startProcessInstanceByKey("testProcess");

    runtimeService.correlateMessage("message");

    Task taskInEventSubProcess = taskService.createTaskQuery().singleResult();
    assertEquals("taskInEventSubProcess", taskInEventSubProcess.getTaskDefinitionKey());

    taskService.complete(taskInEventSubProcess.getId());

    List<String> collectedEvents = TestExecutionListener.collectedEvents;

    assertEquals("taskInMainFlow-start", collectedEvents.get(0));
    assertEquals("taskInMainFlow-end", collectedEvents.get(1));
    assertEquals("eventSubProcess-start", collectedEvents.get(2));
    assertEquals("startEventInSubProcess-start", collectedEvents.get(3));
    assertEquals("startEventInSubProcess-end", collectedEvents.get(4));
    assertEquals("taskInEventSubProcess-start", collectedEvents.get(5));
    assertEquals("taskInEventSubProcess-end", collectedEvents.get(6));
    assertEquals("eventSubProcess-end", collectedEvents.get(7));

    if (processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("taskInMainFlow").canceled().count());
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("startEventInSubProcess").finished().count());
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("taskInEventSubProcess").finished().count());
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("endEventInSubProcess").finished().count());
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("eventSubProcess").finished().count());
    }

  }

  @Deployment
  @Test
  public void testNonInterruptingEventSubprocessListenersInvoked() {
    runtimeService.startProcessInstanceByKey("testProcess");

    runtimeService.correlateMessage("message");

    Task taskInMainFlow = taskService.createTaskQuery().taskDefinitionKey("taskInMainFlow").singleResult();
    assertNotNull(taskInMainFlow);

    Task taskInEventSubProcess = taskService.createTaskQuery().taskDefinitionKey("taskInEventSubProcess").singleResult();
    assertNotNull(taskInEventSubProcess);

    taskService.complete(taskInMainFlow.getId());
    taskService.complete(taskInEventSubProcess.getId());

    List<String> collectedEvents = TestExecutionListener.collectedEvents;

    assertEquals("taskInMainFlow-start", collectedEvents.get(0));
    assertEquals("eventSubProcess-start", collectedEvents.get(1));
    assertEquals("startEventInSubProcess-start", collectedEvents.get(2));
    assertEquals("startEventInSubProcess-end", collectedEvents.get(3));
    assertEquals("taskInEventSubProcess-start", collectedEvents.get(4));
    assertEquals("taskInMainFlow-end", collectedEvents.get(5));
    assertEquals("taskInEventSubProcess-end", collectedEvents.get(6));
    assertEquals("eventSubProcess-end", collectedEvents.get(7));

    if (processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("startEventInSubProcess").finished().count());
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("taskInMainFlow").finished().count());
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("taskInEventSubProcess").finished().count());
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("endEventInSubProcess").finished().count());
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("eventSubProcess").finished().count());
    }
  }

  @Deployment
  @Test
  public void testNestedEventSubprocessListenersInvoked() {
    runtimeService.startProcessInstanceByKey("testProcess");

    runtimeService.correlateMessage("message");

    Task taskInEventSubProcess = taskService.createTaskQuery().singleResult();
    assertEquals("taskInEventSubProcess", taskInEventSubProcess.getTaskDefinitionKey());

    taskService.complete(taskInEventSubProcess.getId());

    List<String> collectedEvents = TestExecutionListener.collectedEvents;

    assertEquals("taskInMainFlow-start", collectedEvents.get(0));
    assertEquals("taskInMainFlow-end", collectedEvents.get(1));
    assertEquals("eventSubProcess-start", collectedEvents.get(2));
    assertEquals("startEventInSubProcess-start", collectedEvents.get(3));
    assertEquals("startEventInSubProcess-end", collectedEvents.get(4));
    assertEquals("taskInEventSubProcess-start", collectedEvents.get(5));
    assertEquals("taskInEventSubProcess-end", collectedEvents.get(6));
    assertEquals("eventSubProcess-end", collectedEvents.get(7));

    if (processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("taskInMainFlow").canceled().count());
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("startEventInSubProcess").finished().count());
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("taskInEventSubProcess").finished().count());
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("endEventInSubProcess").finished().count());
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("eventSubProcess").finished().count());
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("subProcess").finished().count());
    }

  }

  @Deployment
  @Test
  public void testNestedNonInterruptingEventSubprocessListenersInvoked() {
    runtimeService.startProcessInstanceByKey("testProcess");

    runtimeService.correlateMessage("message");

    Task taskInMainFlow = taskService.createTaskQuery().taskDefinitionKey("taskInMainFlow").singleResult();
    assertNotNull(taskInMainFlow);

    Task taskInEventSubProcess = taskService.createTaskQuery().taskDefinitionKey("taskInEventSubProcess").singleResult();
    assertNotNull(taskInEventSubProcess);

    taskService.complete(taskInMainFlow.getId());
    taskService.complete(taskInEventSubProcess.getId());

    List<String> collectedEvents = TestExecutionListener.collectedEvents;

    assertEquals("taskInMainFlow-start", collectedEvents.get(0));
    assertEquals("eventSubProcess-start", collectedEvents.get(1));
    assertEquals("startEventInSubProcess-start", collectedEvents.get(2));
    assertEquals("startEventInSubProcess-end", collectedEvents.get(3));
    assertEquals("taskInEventSubProcess-start", collectedEvents.get(4));
    assertEquals("taskInMainFlow-end", collectedEvents.get(5));
    assertEquals("taskInEventSubProcess-end", collectedEvents.get(6));
    assertEquals("eventSubProcess-end", collectedEvents.get(7));

    if (processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("taskInMainFlow").finished().count());
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("startEventInSubProcess").finished().count());
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("taskInEventSubProcess").finished().count());
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("endEventInSubProcess").finished().count());
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("eventSubProcess").finished().count());
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("subProcess").finished().count());
    }

  }

  @Deployment
  @Test
  public void testEventSubprocessBoundaryListenersInvoked() {
    runtimeService.startProcessInstanceByKey("testProcess");

    runtimeService.correlateMessage("message");

    Task taskInEventSubProcess = taskService.createTaskQuery().singleResult();
    assertEquals("taskInEventSubProcess", taskInEventSubProcess.getTaskDefinitionKey());

    runtimeService.correlateMessage("message2");

    List<String> collectedEvents = TestExecutionListener.collectedEvents;


    assertEquals("taskInMainFlow-start", collectedEvents.get(0));
    assertEquals("taskInMainFlow-end", collectedEvents.get(1));
    assertEquals("eventSubProcess-start", collectedEvents.get(2));
    assertEquals("startEventInSubProcess-start", collectedEvents.get(3));
    assertEquals("startEventInSubProcess-end", collectedEvents.get(4));
    assertEquals("taskInEventSubProcess-start", collectedEvents.get(5));
    assertEquals("taskInEventSubProcess-end", collectedEvents.get(6));
    assertEquals("eventSubProcess-end", collectedEvents.get(7));

    if (processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("taskInMainFlow").finished().count());
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("taskInMainFlow").canceled().count());
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("startEventInSubProcess").finished().count());
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("taskInEventSubProcess").canceled().count());
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("eventSubProcess").finished().count());
    }

  }

  @Deployment
  @Test
  public void testNonInterruptingUnderProcessDefinition() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    // the process instance must have a message event subscription:
    Execution execution = runtimeService.createExecutionQuery()
        .executionId(processInstance.getId())
        .messageEventSubscriptionName("newMessage")
        .singleResult();
    assertNotNull(execution);
    assertEquals(1, createEventSubscriptionQuery().count());
    assertEquals(1, runtimeService.createExecutionQuery().count());

    // if we trigger the usertask, the process terminates and the event subscription is removed:
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("task", task.getTaskDefinitionKey());
    taskService.complete(task.getId());
    testRule.assertProcessEnded(processInstance.getId());
    assertEquals(0, createEventSubscriptionQuery().count());
    assertEquals(0, runtimeService.createExecutionQuery().count());

    // ###################### now we start a new instance but this time we trigger the event subprocess:
    processInstance = runtimeService.startProcessInstanceByKey("process");
    runtimeService.messageEventReceived("newMessage", processInstance.getId());

    assertEquals(2, taskService.createTaskQuery().count());

    // now let's first complete the task in the main flow:
    task = taskService.createTaskQuery().taskDefinitionKey("task").singleResult();
    taskService.complete(task.getId());
    // we still have 2 executions (one for process instance, one for event subprocess):
    assertEquals(2, runtimeService.createExecutionQuery().count());

    // now let's complete the task in the event subprocess
    task = taskService.createTaskQuery().taskDefinitionKey("eventSubProcessTask").singleResult();
    taskService.complete(task.getId());
    // done!
    testRule.assertProcessEnded(processInstance.getId());
    assertEquals(0, runtimeService.createExecutionQuery().count());

    // #################### again, the other way around:

    processInstance = runtimeService.startProcessInstanceByKey("process");
    runtimeService.messageEventReceived("newMessage", processInstance.getId());

    assertEquals(2, taskService.createTaskQuery().count());

    task = taskService.createTaskQuery().taskDefinitionKey("eventSubProcessTask").singleResult();
    taskService.complete(task.getId());
    // we still have 1 execution:
    assertEquals(1, runtimeService.createExecutionQuery().count());

    task = taskService.createTaskQuery().taskDefinitionKey("task").singleResult();
    taskService.complete(task.getId());
    // done!
    testRule.assertProcessEnded(processInstance.getId());
    assertEquals(0, runtimeService.createExecutionQuery().count());
  }

  @Deployment
  @Test
  public void testNonInterruptingUnderProcessDefinitionScope() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    // the process instance must have a message event subscription:
    Execution execution = runtimeService.createExecutionQuery()
        .messageEventSubscriptionName("newMessage")
        .singleResult();
    assertNotNull(execution);
    assertEquals(1, createEventSubscriptionQuery().count());
    assertEquals(2, runtimeService.createExecutionQuery().count());

    // if we trigger the usertask, the process terminates and the event subscription is removed:
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("task", task.getTaskDefinitionKey());
    taskService.complete(task.getId());
    testRule.assertProcessEnded(processInstance.getId());
    assertEquals(0, createEventSubscriptionQuery().count());
    assertEquals(0, runtimeService.createExecutionQuery().count());

    // ###################### now we start a new instance but this time we trigger the event subprocess:
    processInstance = runtimeService.startProcessInstanceByKey("process");
    runtimeService.correlateMessage("newMessage");

    assertEquals(2, taskService.createTaskQuery().count());
    assertEquals(1, createEventSubscriptionQuery().count());

    // now let's first complete the task in the main flow:
    task = taskService.createTaskQuery().taskDefinitionKey("task").singleResult();
    taskService.complete(task.getId());
    // we still have 2 executions (one for process instance, one for subprocess scope):
    assertEquals(2, runtimeService.createExecutionQuery().count());

    // now let's complete the task in the event subprocess
    task = taskService.createTaskQuery().taskDefinitionKey("eventSubProcessTask").singleResult();
    taskService.complete(task.getId());
    // done!
    testRule.assertProcessEnded(processInstance.getId());
    assertEquals(0, runtimeService.createExecutionQuery().count());

    // #################### again, the other way around:

    processInstance = runtimeService.startProcessInstanceByKey("process");
    runtimeService.correlateMessage("newMessage");

    assertEquals(2, taskService.createTaskQuery().count());

    task = taskService.createTaskQuery().taskDefinitionKey("eventSubProcessTask").singleResult();
    taskService.complete(task.getId());
    // we still have 2 executions (usertask in main flow is scope):
    assertEquals(2, runtimeService.createExecutionQuery().count());

    task = taskService.createTaskQuery().taskDefinitionKey("task").singleResult();
    taskService.complete(task.getId());
    // done!
    testRule.assertProcessEnded(processInstance.getId());
    assertEquals(0, runtimeService.createExecutionQuery().count());
  }

  @Deployment
  @Test
  public void testNonInterruptingInEmbeddedSubprocess() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    // the process instance must have a message event subscription:
    Execution execution = runtimeService.createExecutionQuery()
        .messageEventSubscriptionName("newMessage")
        .singleResult();
    assertNotNull(execution);
    assertEquals(1, createEventSubscriptionQuery().count());

    // if we trigger the usertask, the process terminates and the event subscription is removed:
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("task", task.getTaskDefinitionKey());
    taskService.complete(task.getId());
    testRule.assertProcessEnded(processInstance.getId());
    assertEquals(0, createEventSubscriptionQuery().count());
    assertEquals(0, runtimeService.createExecutionQuery().count());

    // ###################### now we start a new instance but this time we trigger the event subprocess:
    processInstance = runtimeService.startProcessInstanceByKey("process");
    runtimeService.correlateMessage("newMessage");

    assertEquals(2, taskService.createTaskQuery().count());

    // now let's first complete the task in the main flow:
    task = taskService.createTaskQuery().taskDefinitionKey("task").singleResult();
    taskService.complete(task.getId());
    // we still have 3 executions:
    assertEquals(3, runtimeService.createExecutionQuery().count());

    // now let's complete the task in the event subprocess
    task = taskService.createTaskQuery().taskDefinitionKey("eventSubProcessTask").singleResult();
    taskService.complete(task.getId());
    // done!
    testRule.assertProcessEnded(processInstance.getId());
    assertEquals(0, runtimeService.createExecutionQuery().count());

    // #################### again, the other way around:

    processInstance = runtimeService.startProcessInstanceByKey("process");
    runtimeService.correlateMessage("newMessage");

    assertEquals(2, taskService.createTaskQuery().count());

    task = taskService.createTaskQuery().taskDefinitionKey("eventSubProcessTask").singleResult();
    taskService.complete(task.getId());
    // we still have 2 executions:
    assertEquals(2, runtimeService.createExecutionQuery().count());

    task = taskService.createTaskQuery().taskDefinitionKey("task").singleResult();
    taskService.complete(task.getId());
    // done!
    testRule.assertProcessEnded(processInstance.getId());
    assertEquals(0, runtimeService.createExecutionQuery().count());
  }

  @Deployment
  @Test
  public void testMultipleNonInterruptingInEmbeddedSubprocess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    // the process instance must have a message event subscription:
    Execution subProcess = runtimeService.createExecutionQuery()
        .messageEventSubscriptionName("newMessage")
        .singleResult();
    assertNotNull(subProcess);
    assertEquals(1, createEventSubscriptionQuery().count());

    Task subProcessTask = taskService.createTaskQuery().taskDefinitionKey("subProcessTask").singleResult();
    assertNotNull(subProcessTask);

    // start event sub process multiple times
    for (int i = 1; i < 3; i++) {
      runtimeService.messageEventReceived("newMessage", subProcess.getId());

      // check that now i event sub process tasks exist
      List<Task> eventSubProcessTasks = taskService.createTaskQuery().taskDefinitionKey("eventSubProcessTask").list();
      assertEquals(i, eventSubProcessTasks.size());
    }

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), processEngine);

    // check that the parent execution of the event sub process task execution is the event
    // sub process execution
    assertThat(executionTree)
        .matches(
            describeExecutionTree(null).scope()
                .child(null).scope()
                  .child("subProcessTask").concurrent().noScope().up()
                  .child(null).concurrent().noScope()
                    .child("eventSubProcessTask").scope().up().up()
                  .child(null).concurrent().noScope()
                    .child("eventSubProcessTask").scope()
                .done());

    // complete sub process task
    taskService.complete(subProcessTask.getId());

    // after complete the sub process task all task should be deleted because of the terminating end event
    assertEquals(0, taskService.createTaskQuery().count());

    // and the process instance should be ended
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }

  private EventSubscriptionQueryImpl createEventSubscriptionQuery() {
    return new EventSubscriptionQueryImpl(processEngineConfiguration.getCommandExecutorTxRequired());
  }

  @Deployment
  @Test
  public void testNonInterruptingInMultiParallelEmbeddedSubprocess() {
    // #################### I. start process and only complete the tasks
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    // assert execution tree: scope (process) > scope (subprocess) > 2 x subprocess + usertask
    assertEquals(6, runtimeService.createExecutionQuery().count());

    // expect: two subscriptions, one for each instance
    assertEquals(2, runtimeService.createEventSubscriptionQuery().count());

    // expect: two subprocess instances, i.e. two tasks created
    List<Task> tasks = taskService.createTaskQuery().list();
    // then: complete both tasks
    for (Task task : tasks) {
      assertEquals("subUserTask", task.getTaskDefinitionKey());
      taskService.complete(task.getId());
    }

    // expect: the event subscriptions are removed
    assertEquals(0, runtimeService.createEventSubscriptionQuery().count());

    // then: complete the last task of the main process
    taskService.complete(taskService.createTaskQuery().singleResult().getId());
    testRule.assertProcessEnded(processInstance.getId());

    // #################### II. start process and correlate messages to trigger subprocesses instantiation
    processInstance = runtimeService.startProcessInstanceByKey("process");
    for (EventSubscription es : runtimeService.createEventSubscriptionQuery().list()) {
      runtimeService.messageEventReceived("message", es.getExecutionId()); // trigger
    }

    // expect: both subscriptions are remaining and they can be re-triggered as long as the subprocesses are active
    assertEquals(2, runtimeService.createEventSubscriptionQuery().count());

    // expect: two additional task, one for each triggered process
    tasks = taskService.createTaskQuery().taskName("Message User Task").list();
    assertEquals(2, tasks.size());
    for (Task task : tasks) { // complete both tasks
      taskService.complete(task.getId());
    }

    // then: complete one subprocess
    taskService.complete(taskService.createTaskQuery().taskName("Sub User Task").list().get(0).getId());

    // expect: only the subscription of the second subprocess instance is left
    assertEquals(1, runtimeService.createEventSubscriptionQuery().count());

    // then: trigger the second subprocess again
    runtimeService.messageEventReceived("message",
        runtimeService.createEventSubscriptionQuery().singleResult().getExecutionId());

    // expect: one message subprocess task exist
    assertEquals(1, taskService.createTaskQuery().taskName("Message User Task").list().size());

    // then: complete all inner subprocess tasks
    tasks = taskService.createTaskQuery().list();
    for (Task task : tasks) {
      taskService.complete(task.getId());
    }

    // expect: no subscription is left
    assertEquals(0, runtimeService.createEventSubscriptionQuery().count());

    // then: complete the last task of the main process
    taskService.complete(taskService.createTaskQuery().singleResult().getId());
    testRule.assertProcessEnded(processInstance.getId());
  }

  @Deployment
  @Test
  public void testNonInterruptingInMultiSequentialEmbeddedSubprocess() {
    // start process and trigger the first message sub process
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    runtimeService.messageEventReceived("message", runtimeService.createEventSubscriptionQuery().singleResult().getExecutionId());

    // expect: one subscription is remaining for the first instance
    assertEquals(1, runtimeService.createEventSubscriptionQuery().count());

    // then: complete both tasks (subprocess and message subprocess)
    taskService.complete(taskService.createTaskQuery().taskName("Message User Task").singleResult().getId());
    taskService.complete(taskService.createTaskQuery().taskName("Sub User Task").list().get(0).getId());

    // expect: the second instance is started
    assertEquals(1, runtimeService.createEventSubscriptionQuery().count());

    // then: just complete this
    taskService.complete(taskService.createTaskQuery().taskName("Sub User Task").list().get(0).getId());

    // expect: no subscription is left
    assertEquals(0, runtimeService.createEventSubscriptionQuery().count());

    // then: complete the last task of the main process
    taskService.complete(taskService.createTaskQuery().singleResult().getId());
    testRule.assertProcessEnded(processInstance.getId());
  }

  @Deployment
  @Test
  public void testNonInterruptingWithParallelForkInsideEmbeddedSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    runtimeService.messageEventReceived("newMessage", runtimeService.createEventSubscriptionQuery().singleResult().getExecutionId());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), processEngine);

    assertThat(executionTree)
        .matches(
            describeExecutionTree(null).scope()
                .child(null).scope()
                .child("firstUserTask").concurrent().noScope().up()
                .child("secondUserTask").concurrent().noScope().up()
                .child(null).concurrent().noScope()
                    .child("eventSubProcessTask")
                .done());

    List<Task> tasks = taskService.createTaskQuery().list();

    for (Task task : tasks) {
      taskService.complete(task.getId());
    }

    testRule.assertProcessEnded(processInstance.getId());

  }

  @Deployment
  @Test
  public void testNonInterruptingWithReceiveTask() {
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // when (1)
    runtimeService.correlateMessage("firstMessage");

    // then (1)
    assertEquals(1, taskService.createTaskQuery().count());

    Task task1 = taskService.createTaskQuery()
        .taskDefinitionKey("eventSubProcessTask")
        .singleResult();
    assertNotNull(task1);

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    // check that the parent execution of the event sub process task execution is the event
    // sub process execution
    assertThat(executionTree)
        .matches(
          describeExecutionTree(null).scope()
            .child(null).concurrent().noScope()
              .child("receiveTask").scope().up().up()
            .child(null).concurrent().noScope()
              .child("eventSubProcessTask").scope()
            .done());

    // when (2)
    runtimeService.correlateMessage("secondMessage");

    // then (2)
    assertEquals(2, taskService.createTaskQuery().count());

    task1 = taskService.createTaskQuery()
        .taskDefinitionKey("eventSubProcessTask")
        .singleResult();
    assertNotNull(task1);

    Task task2 = taskService.createTaskQuery()
        .taskDefinitionKey("userTask")
        .singleResult();
    assertNotNull(task2);

    executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    // check that the parent execution of the event sub process task execution is the event
    // sub process execution
    assertThat(executionTree)
        .matches(
          describeExecutionTree(null).scope()
            .child("userTask").concurrent().noScope().up()
            .child(null).concurrent().noScope()
              .child("eventSubProcessTask").scope()
            .done());

    assertEquals(1, runtimeService.createEventSubscriptionQuery().count());

    taskService.complete(task1.getId());
    taskService.complete(task2.getId());

    testRule.assertProcessEnded(processInstanceId);
  }

  /**
   * CAM-3655
   */
  @Deployment
  @Test
  public void testNonInterruptingWithAsyncConcurrentTask() {
    // given a process instance with an asyncBefore user task
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // and a triggered non-interrupting subprocess with a user task
    runtimeService.correlateMessage("message");

    // then triggering the async job should be successful
    Job asyncJob = managementService.createJobQuery().singleResult();
    assertNotNull(asyncJob);
    managementService.executeJob(asyncJob.getId());

    // and there should be two tasks now that can be completed successfully
    assertEquals(2, taskService.createTaskQuery().count());
    Task processTask = taskService.createTaskQuery().taskDefinitionKey("userTask").singleResult();
    Task eventSubprocessTask = taskService.createTaskQuery().taskDefinitionKey("eventSubProcessTask").singleResult();

    assertNotNull(processTask);
    assertNotNull(eventSubprocessTask);

    taskService.complete(processTask.getId());
    taskService.complete(eventSubprocessTask.getId());


    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment
  @Test
  public void testNonInterruptingWithReceiveTaskInsideEmbeddedSubProcess() {
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // when (1)
    runtimeService.correlateMessage("firstMessage");

    // then (1)
    assertEquals(1, taskService.createTaskQuery().count());

    Task task1 = taskService.createTaskQuery()
        .taskDefinitionKey("eventSubProcessTask")
        .singleResult();
    assertNotNull(task1);

    Execution task1Execution = runtimeService
        .createExecutionQuery()
        .activityId("eventSubProcessTask")
        .singleResult();

    assertFalse(processInstanceId.equals(((ExecutionEntity) task1Execution).getParentId()));

    // when (2)
    runtimeService.correlateMessage("secondMessage");

    // then (2)
    assertEquals(2, taskService.createTaskQuery().count());

    task1 = taskService.createTaskQuery()
        .taskDefinitionKey("eventSubProcessTask")
        .singleResult();
    assertNotNull(task1);

    task1Execution = runtimeService
        .createExecutionQuery()
        .activityId("eventSubProcessTask")
        .singleResult();

    assertFalse(processInstanceId.equals(((ExecutionEntity) task1Execution).getParentId()));

    Task task2 = taskService.createTaskQuery()
        .taskDefinitionKey("userTask")
        .singleResult();
    assertNotNull(task2);

    Execution task2Execution = runtimeService
        .createExecutionQuery()
        .activityId("eventSubProcessTask")
        .singleResult();

    assertFalse(processInstanceId.equals(((ExecutionEntity) task2Execution).getParentId()));

    // both have the same parent (but it is not the process instance)
    assertTrue(((ExecutionEntity) task1Execution).getParentId().equals(((ExecutionEntity) task2Execution).getParentId()));

    assertEquals(1, runtimeService.createEventSubscriptionQuery().count());

    taskService.complete(task1.getId());
    taskService.complete(task2.getId());

    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment
  @Test
  public void testNonInterruptingWithUserTaskAndBoundaryEventInsideEmbeddedSubProcess() {
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // when
    runtimeService.correlateMessage("newMessage");

    // then
    assertEquals(2, taskService.createTaskQuery().count());

    Task task1 = taskService.createTaskQuery()
        .taskDefinitionKey("eventSubProcessTask")
        .singleResult();
    assertNotNull(task1);

    Execution task1Execution = runtimeService
        .createExecutionQuery()
        .activityId("eventSubProcessTask")
        .singleResult();

    assertFalse(processInstanceId.equals(((ExecutionEntity) task1Execution).getParentId()));

    Task task2 = taskService.createTaskQuery()
        .taskDefinitionKey("task")
        .singleResult();
    assertNotNull(task2);

    Execution task2Execution = runtimeService
        .createExecutionQuery()
        .activityId("eventSubProcessTask")
        .singleResult();

    assertFalse(processInstanceId.equals(((ExecutionEntity) task2Execution).getParentId()));

    // both have the same parent (but it is not the process instance)
    assertTrue(((ExecutionEntity) task1Execution).getParentId().equals(((ExecutionEntity) task2Execution).getParentId()));

    assertEquals(1, runtimeService.createEventSubscriptionQuery().count());

    taskService.complete(task1.getId());
    taskService.complete(task2.getId());

    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment
  @Test
  public void testNonInterruptingOutsideEmbeddedSubProcessWithReceiveTaskInsideEmbeddedSubProcess() {
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // when (1)
    runtimeService.correlateMessage("firstMessage");

    // then (1)
    assertEquals(1, taskService.createTaskQuery().count());

    Task task1 = taskService.createTaskQuery()
        .taskDefinitionKey("eventSubProcessTask")
        .singleResult();
    assertNotNull(task1);

    // when (2)
    runtimeService.correlateMessage("secondMessage");

    // then (2)
    assertEquals(2, taskService.createTaskQuery().count());

    task1 = taskService.createTaskQuery()
        .taskDefinitionKey("eventSubProcessTask")
        .singleResult();
    assertNotNull(task1);

    Task task2 = taskService.createTaskQuery()
        .taskDefinitionKey("userTask")
        .singleResult();
    assertNotNull(task2);

    assertEquals(1, runtimeService.createEventSubscriptionQuery().count());

    taskService.complete(task1.getId());
    taskService.complete(task2.getId());

    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment
  @Test
  public void testInterruptingActivityInstanceTree() {
    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = instance.getId();

    // when
    runtimeService.correlateMessage("newMessage");

    // then
    ActivityInstance tree = runtimeService.getActivityInstance(processInstanceId);
    assertThat(tree).hasStructure(
        describeActivityInstanceTree(instance.getProcessDefinitionId())
            .beginScope("subProcess")
              .beginScope("eventSubProcess")
                .activity("eventSubProcessTask")
              .endScope()
            .endScope()
            .done());
  }

  @Deployment
  @Test
  public void testNonInterruptingActivityInstanceTree() {
    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = instance.getId();

    // when
    runtimeService.correlateMessage("newMessage");

    // then
    ActivityInstance tree = runtimeService.getActivityInstance(processInstanceId);
    assertThat(tree).hasStructure(
        describeActivityInstanceTree(instance.getProcessDefinitionId())
            .beginScope("subProcess")
              .activity("innerTask")
              .beginScope("eventSubProcess")
                  .activity("eventSubProcessTask")
              .endScope()
            .endScope()
            .done());
  }

  @Deployment
  @Test
  public void testNonInterruptingWithTerminatingEndEvent() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName(), is("Inner User Task"));
    runtimeService.correlateMessage("message");

    Task eventSubprocessTask = taskService.createTaskQuery().taskName("Event User Task").singleResult();
    assertThat(eventSubprocessTask, is(notNullValue()));
    taskService.complete(eventSubprocessTask.getId());

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .beginScope("SubProcess_1")
            .activity("UserTask_1")
          .endScope()
        .endScope()
        .done()
    );
  }

  @Deployment
  @Test
  public void testExpressionInMessageNameInInterruptingSubProcessDefinition() {
    // given an process instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    // when receiving the message
    runtimeService.messageEventReceived("newMessage-foo", processInstance.getId());

    // the the subprocess is triggered and we can complete the task
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("eventSubProcessTask", task.getTaskDefinitionKey());
    taskService.complete(task.getId());
    testRule.assertProcessEnded(processInstance.getId());
  }

}
