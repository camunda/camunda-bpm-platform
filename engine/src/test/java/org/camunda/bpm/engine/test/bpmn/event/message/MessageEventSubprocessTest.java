/* Licensed under the Apache License, Version 2.0 (the "License");
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

package org.camunda.bpm.engine.test.bpmn.event.message;

import java.util.List;

import org.camunda.bpm.engine.impl.EventSubscriptionQueryImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ExecutionQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.TestExecutionListener;


/**
 * @author Daniel Meyer
 * @author Falko Menge
 * @author Danny Gräf
 */
public class MessageEventSubprocessTest extends PluggableProcessEngineTestCase {

  @Override
  protected void tearDown() throws Exception {
    try {
      super.tearDown();
    } finally {
      TestExecutionListener.reset();
    }
  }

  @Deployment
  public void testInterruptingUnderProcessDefinition() {
    testInterruptingUnderProcessDefinition(1);
  }

  /**
   * Checks if unused event subscriptions are properly deleted.
   */
  @Deployment
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
    assertProcessEnded(processInstance.getId());
    assertEquals(0, createEventSubscriptionQuery().count());
    assertEquals(0, runtimeService.createExecutionQuery().count());

    // now we start a new instance but this time we trigger the event subprocess:
    processInstance = runtimeService.startProcessInstanceByKey("process");
    runtimeService.messageEventReceived("newMessage", processInstance.getId());

    task = taskService.createTaskQuery().singleResult();
    assertEquals("eventSubProcessTask", task.getTaskDefinitionKey());
    taskService.complete(task.getId());
    assertProcessEnded(processInstance.getId());
    assertEquals(0, createEventSubscriptionQuery().count());
    assertEquals(0, runtimeService.createExecutionQuery().count());
  }

  @Deployment
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

    if (processEngineConfiguration.getHistoryLevel() > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("taskInMainFlow").canceled().count());
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("startEventInSubProcess").finished().count());
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("taskInEventSubProcess").finished().count());
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("endEventInSubProcess").finished().count());
//    SEE: CAM-1755
//      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("eventSubProcess").finished().count());
    }

  }

  @Deployment
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

    if (processEngineConfiguration.getHistoryLevel() > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("startEventInSubProcess").finished().count());
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("taskInMainFlow").finished().count());
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("taskInEventSubProcess").finished().count());
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("endEventInSubProcess").finished().count());
//    SEE: CAM-1755
//     assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("eventSubProcess").finished().count());
    }
  }

  @Deployment
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

    if (processEngineConfiguration.getHistoryLevel() > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("taskInMainFlow").canceled().count());
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("startEventInSubProcess").finished().count());
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("taskInEventSubProcess").finished().count());
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("endEventInSubProcess").finished().count());
//    SEE: CAM-1755
//      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("eventSubProcess").finished().count());
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("subProcess").finished().count());
    }

  }

  @Deployment
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

    if (processEngineConfiguration.getHistoryLevel() > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("taskInMainFlow").finished().count());
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("startEventInSubProcess").finished().count());
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("taskInEventSubProcess").finished().count());
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("endEventInSubProcess").finished().count());
//    SEE: CAM-1755
//      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("eventSubProcess").finished().count());
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("subProcess").finished().count());
    }

  }

  @Deployment
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

    if (processEngineConfiguration.getHistoryLevel() > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("taskInMainFlow").finished().count());
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("taskInMainFlow").canceled().count());
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("startEventInSubProcess").finished().count());
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("taskInEventSubProcess").canceled().count());
//    SEE: CAM-1755
//      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("eventSubProcess").finished().count());
    }

  }

  @Deployment
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
    assertProcessEnded(processInstance.getId());
    assertEquals(0, createEventSubscriptionQuery().count());
    assertEquals(0, runtimeService.createExecutionQuery().count());

    // ###################### now we start a new instance but this time we trigger the event subprocess:
    processInstance = runtimeService.startProcessInstanceByKey("process");
    runtimeService.messageEventReceived("newMessage", processInstance.getId());

    assertEquals(2, taskService.createTaskQuery().count());

    // now let's first complete the task in the main flow:
    task = taskService.createTaskQuery().taskDefinitionKey("task").singleResult();
    taskService.complete(task.getId());
    // we still have 1 executions:
    assertEquals(1, runtimeService.createExecutionQuery().count());

    // now let's complete the task in the event subprocess
    task = taskService.createTaskQuery().taskDefinitionKey("eventSubProcessTask").singleResult();
    taskService.complete(task.getId());
    // done!
    assertProcessEnded(processInstance.getId());
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
    assertProcessEnded(processInstance.getId());
    assertEquals(0, runtimeService.createExecutionQuery().count());
  }

  @Deployment
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
    assertProcessEnded(processInstance.getId());
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
    // we still have 1 executions:
    assertEquals(1, runtimeService.createExecutionQuery().count());

    // now let's complete the task in the event subprocess
    task = taskService.createTaskQuery().taskDefinitionKey("eventSubProcessTask").singleResult();
    taskService.complete(task.getId());
    // done!
    assertProcessEnded(processInstance.getId());
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
    assertProcessEnded(processInstance.getId());
    assertEquals(0, runtimeService.createExecutionQuery().count());
  }

  @Deployment
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
    assertProcessEnded(processInstance.getId());
    assertEquals(0, createEventSubscriptionQuery().count());
    assertEquals(0, runtimeService.createExecutionQuery().count());

    // ###################### now we start a new instance but this time we trigger the event subprocess:
    processInstance = runtimeService.startProcessInstanceByKey("process");
    runtimeService.correlateMessage("newMessage");

    assertEquals(2, taskService.createTaskQuery().count());

    // now let's first complete the task in the main flow:
    task = taskService.createTaskQuery().taskDefinitionKey("task").singleResult();
    taskService.complete(task.getId());
    // we still have 2 executions:
    assertEquals(2, runtimeService.createExecutionQuery().count());

    // now let's complete the task in the event subprocess
    task = taskService.createTaskQuery().taskDefinitionKey("eventSubProcessTask").singleResult();
    taskService.complete(task.getId());
    // done!
    assertProcessEnded(processInstance.getId());
    assertEquals(0, runtimeService.createExecutionQuery().count());

    // #################### again, the other way around:

    processInstance = runtimeService.startProcessInstanceByKey("process");
    runtimeService.correlateMessage("newMessage");

    assertEquals(2, taskService.createTaskQuery().count());

    task = taskService.createTaskQuery().taskDefinitionKey("eventSubProcessTask").singleResult();
    taskService.complete(task.getId());
    // we still have 1 execution:
    assertEquals(2, runtimeService.createExecutionQuery().count());

    task = taskService.createTaskQuery().taskDefinitionKey("task").singleResult();
    taskService.complete(task.getId());
    // done!
    assertProcessEnded(processInstance.getId());
    assertEquals(0, runtimeService.createExecutionQuery().count());
  }

  @Deployment
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

      // check that the parent execution of the event sub process task execution is the parent
      // sub process
      String taskExecutionId = eventSubProcessTasks.get(i-1).getExecutionId();
      ExecutionEntity taskExecution = (ExecutionEntity) runtimeService.createExecutionQuery().executionId(taskExecutionId).singleResult();
      assertEquals(subProcess.getId(), taskExecution.getParentId());
    }

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
    assertProcessEnded(processInstance.getId());

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
    assertProcessEnded(processInstance.getId());
  }

  @Deployment
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
    assertProcessEnded(processInstance.getId());
  }

  @Deployment
  public void FAILING_testNonInterruptingWithParallelForkInsideEmbeddedSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    runtimeService.messageEventReceived("newMessage", runtimeService.createEventSubscriptionQuery().singleResult().getExecutionId());

    ExecutionQuery executionQuery = runtimeService.createExecutionQuery();

    String forkId = executionQuery
        .activityId("fork")
        .singleResult()
        .getId();

    Execution eventSubProcessTaskExecution = executionQuery
        .activityId("eventSubProcessTask")
        .singleResult();

    ExecutionEntity executionEntity = (ExecutionEntity) eventSubProcessTaskExecution;
    assertEquals(forkId, executionEntity.getParentId());

    List<Task> tasks = taskService.createTaskQuery().list();

    for (Task task : tasks) {
      taskService.complete(task.getId());
    }

    assertProcessEnded(processInstance.getId());

  }
}
