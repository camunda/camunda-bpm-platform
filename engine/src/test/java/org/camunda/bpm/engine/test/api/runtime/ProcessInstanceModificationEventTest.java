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
package org.camunda.bpm.engine.test.api.runtime;

import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.describeActivityInstanceTree;
import static org.camunda.bpm.engine.test.util.ExecutionAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ExecutionAssert.describeExecutionTree;

import java.util.List;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.ExecutionTree;
import org.camunda.bpm.engine.variable.Variables;

/**
 * @author Roman Smirnov
 *
 */
public class ProcessInstanceModificationEventTest extends PluggableProcessEngineTestCase {

  protected static final String INTERMEDIATE_TIMER_CATCH_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.intermediateTimerCatch.bpmn20.xml";
  protected static final String MESSAGE_START_EVENT_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.messageStart.bpmn20.xml";
  protected static final String TIMER_START_EVENT_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.timerStart.bpmn20.xml";
  protected static final String ONE_TASK_PROCESS = "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml";
  protected static final String TERMINATE_END_EVENT_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.terminateEnd.bpmn20.xml";
  protected static final String CANCEL_END_EVENT_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.cancelEnd.bpmn20.xml";

  @Deployment(resources = INTERMEDIATE_TIMER_CATCH_PROCESS)
  public void testStartBeforeIntermediateCatchEvent() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("intermediateCatchEvent")
      .execute();


    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("task")
        .activity("intermediateCatchEvent")
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child("task").concurrent().noScope().up()
          .child(null).concurrent().noScope()
            .child("intermediateCatchEvent").scope()
          .done());

    ActivityInstance catchEventInstance = getChildInstanceForActivity(updatedTree, "intermediateCatchEvent");

    // and there is a timer job
    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);
    assertEquals(catchEventInstance.getExecutionIds()[0], job.getExecutionId());

    completeTasksInOrder("task");
    executeAvailableJobs();
    assertProcessEnded(processInstanceId);

  }

  @Deployment(resources = MESSAGE_START_EVENT_PROCESS)
  public void testStartBeforeMessageStartEvent() {
    runtimeService.correlateMessage("startMessage");
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertNotNull(processInstance);

    EventSubscription startEventSubscription = runtimeService.createEventSubscriptionQuery().singleResult();
    assertNotNull(startEventSubscription);

    String processInstanceId = processInstance.getId();

    // when I start before the message start event
    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("theStart")
      .execute();

    // then there are two instances of "task"
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("task")
        .activity("task")
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child("task").concurrent().noScope().up()
          .child("task").concurrent().noScope()
        .done());

    // and there is only the message start event subscription
    EventSubscription subscription = runtimeService.createEventSubscriptionQuery().singleResult();
    assertNotNull(subscription);
    assertEquals(startEventSubscription.getId(), subscription.getId());

    completeTasksInOrder("task", "task");
    assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = TIMER_START_EVENT_PROCESS)
  public void testStartBeforeTimerStartEvent() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    Job startTimerJob = managementService.createJobQuery().singleResult();
    assertNotNull(startTimerJob);

    // when I start before the timer start event
    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("theStart")
      .execute();

    // then there are two instances of "task"
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("task")
        .activity("task")
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child("task").concurrent().noScope().up()
          .child("task").concurrent().noScope()
          .done());

    // and there is only one timer job
    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);
    assertEquals(startTimerJob.getId(), job.getId());

    completeTasksInOrder("task", "task");
    assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testStartBeforNoneStartEvent() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String processInstanceId = processInstance.getId();

    // when I start before the none start event
    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("theStart")
      .execute();

    // then there are two instances of "task"
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("theTask")
        .activity("theTask")
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child("theTask").concurrent().noScope().up()
          .child("theTask").concurrent().noScope()
          .done());

    // and the process can be ended as usual
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(2, tasks.size());

    for (Task task : tasks) {
      taskService.complete(task.getId());
    }

    assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testStartBeforeNoneEndEvent() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String processInstanceId = processInstance.getId();

    // when I start before the none end event
    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("theEnd")
      .execute();

    // then there is no effect
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("theTask")
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree("theTask").scope()
          .done());

    completeTasksInOrder("theTask");
    assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = TERMINATE_END_EVENT_PROCESS)
  public void testStartBeforeTerminateEndEvent() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    // when I start before the terminate end event
    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("terminateEnd")
      .execute();

    // then the process instance is terminated
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNull(updatedTree);
    assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = CANCEL_END_EVENT_PROCESS)
  public void testStartBeforeCancelEndEventConcurrent() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    Task txTask = taskService.createTaskQuery().singleResult();
    assertEquals("txTask", txTask.getTaskDefinitionKey());

    // when I start before the cancel end event
    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("cancelEnd")
      .execute();

    // then the subprocess instance is cancelled
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("afterCancellation")
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree("afterCancellation").scope()
        .done());

    Task afterCancellationTask = taskService.createTaskQuery().singleResult();
    assertNotNull(afterCancellationTask);
    assertFalse(txTask.getId().equals(afterCancellationTask.getId()));
    assertEquals("afterCancellation", afterCancellationTask.getTaskDefinitionKey());
  }

  @Deployment(resources = CANCEL_END_EVENT_PROCESS)
  public void testStartBeforeCancelEndEvent() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    // complete the transaction subprocess once
    Task txTask = taskService.createTaskQuery().singleResult();
    assertEquals("txTask", txTask.getTaskDefinitionKey());

    taskService.complete(txTask.getId(), Variables.createVariables().putValue("success", true));

    Task afterSuccessTask = taskService.createTaskQuery().singleResult();
    assertEquals("afterSuccess", afterSuccessTask.getTaskDefinitionKey());

    // when I start before the cancel end event
    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("cancelEnd")
      .execute();

    // then a new subprocess instance is created and immediately cancelled
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("afterCancellation")
        .activity("afterSuccess")
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope()
        .child("afterCancellation").concurrent().noScope().up()
        .child("afterSuccess").concurrent().noScope().up()
        .child("tx").scope().eventScope()
      .done());

    // the compensation for the completed tx has not been triggered
    assertEquals(0, taskService.createTaskQuery().taskDefinitionKey("undoTxTask").count());

    // complete the process
    Task afterCancellationTask = taskService.createTaskQuery().taskDefinitionKey("afterCancellation").singleResult();
    assertNotNull(afterCancellationTask);

    taskService.complete(afterCancellationTask.getId());
    taskService.complete(afterSuccessTask.getId());

    assertProcessEnded(processInstanceId);
  }

  protected ActivityInstance getChildInstanceForActivity(ActivityInstance activityInstance, String activityId) {
    for (ActivityInstance childInstance : activityInstance.getChildActivityInstances()) {
      if (childInstance.getActivityId().equals(activityId)) {
        return childInstance;
      }
    }

    return null;
  }

  protected void completeTasksInOrder(String... taskNames) {
    for (String taskName : taskNames) {
      // complete any task with that name
      List<Task> tasks = taskService.createTaskQuery().taskDefinitionKey(taskName).listPage(0, 1);
      assertTrue("task for activity " + taskName + " does not exist", !tasks.isEmpty());
      taskService.complete(tasks.get(0).getId());
    }
  }
}
