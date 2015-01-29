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

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;

/**
 *
 * @author Kristin Polenz
 */
public class MessageNonInterruptingBoundaryEventTest extends PluggableProcessEngineTestCase {

  @Deployment
  public void testSingleNonInterruptingBoundaryMessageEvent() {
    runtimeService.startProcessInstanceByKey("process");

    assertEquals(2, runtimeService.createExecutionQuery().count());

    Task userTask = taskService.createTaskQuery().taskDefinitionKey("task").singleResult();
    assertNotNull(userTask);

    Execution execution = runtimeService.createExecutionQuery()
      .messageEventSubscriptionName("messageName")
      .singleResult();
    assertNotNull(execution);

    // 1. case: message received before completing the task

    runtimeService.messageEventReceived("messageName", execution.getId());
    // event subscription not removed
    execution = runtimeService.createExecutionQuery()
            .messageEventSubscriptionName("messageName")
            .singleResult();
    assertNotNull(execution);

    assertEquals(2, taskService.createTaskQuery().count());

    userTask = taskService.createTaskQuery().taskDefinitionKey("taskAfterMessage").singleResult();
    assertNotNull(userTask);
    assertEquals("taskAfterMessage", userTask.getTaskDefinitionKey());
    taskService.complete(userTask.getId());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    // send a message a second time
    runtimeService.messageEventReceived("messageName", execution.getId());
    // event subscription not removed
    execution = runtimeService.createExecutionQuery()
            .messageEventSubscriptionName("messageName")
            .singleResult();
    assertNotNull(execution);

    assertEquals(2, taskService.createTaskQuery().count());

    userTask = taskService.createTaskQuery().taskDefinitionKey("taskAfterMessage").singleResult();
    assertNotNull(userTask);
    assertEquals("taskAfterMessage", userTask.getTaskDefinitionKey());
    taskService.complete(userTask.getId());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    // now complete the user task with the message boundary event
    userTask = taskService.createTaskQuery().taskDefinitionKey("task").singleResult();
    assertNotNull(userTask);

    taskService.complete(userTask.getId());

    // event subscription removed
    execution = runtimeService.createExecutionQuery()
            .messageEventSubscriptionName("messageName")
            .singleResult();
    assertNull(execution);

    userTask = taskService.createTaskQuery().taskDefinitionKey("taskAfterTask").singleResult();
    assertNotNull(userTask);

    taskService.complete(userTask.getId());

    assertEquals(0, runtimeService.createProcessInstanceQuery().count());

    // 2nd. case: complete the user task cancels the message subscription

    runtimeService.startProcessInstanceByKey("process");

    userTask = taskService.createTaskQuery().taskDefinitionKey("task").singleResult();
    assertNotNull(userTask);
    taskService.complete(userTask.getId());

    execution = runtimeService.createExecutionQuery()
      .messageEventSubscriptionName("messageName")
      .singleResult();
    assertNull(execution);

    userTask = taskService.createTaskQuery().taskDefinitionKey("taskAfterTask").singleResult();
    assertNotNull(userTask);
    assertEquals("taskAfterTask", userTask.getTaskDefinitionKey());
    taskService.complete(userTask.getId());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }

  @Deployment
  public void testNonInterruptingEventInCombinationWithReceiveTask() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // when (1)
    runtimeService.correlateMessage("firstMessage");

    // then (1)
    assertEquals(1, taskService.createTaskQuery().count());

    Task task1 = taskService.createTaskQuery()
        .taskDefinitionKey("task1")
        .singleResult();
    assertNotNull(task1);

    Execution task1Execution = runtimeService
        .createExecutionQuery()
        .activityId("task1")
        .singleResult();

    assertEquals(processInstanceId, ((ExecutionEntity) task1Execution).getParentId());

    // when (2)
    runtimeService.correlateMessage("secondMessage");

    // then (2)
    assertEquals(2, taskService.createTaskQuery().count());

    task1 = taskService.createTaskQuery()
        .taskDefinitionKey("task1")
        .singleResult();
    assertNotNull(task1);

    task1Execution = runtimeService
        .createExecutionQuery()
        .activityId("task1")
        .singleResult();

    assertEquals(processInstanceId, ((ExecutionEntity) task1Execution).getParentId());

    Task task2 = taskService.createTaskQuery()
        .taskDefinitionKey("task2")
        .singleResult();
    assertNotNull(task2);

    Execution task2Execution = runtimeService
        .createExecutionQuery()
        .activityId("task1")
        .singleResult();

    assertEquals(processInstanceId, ((ExecutionEntity) task2Execution).getParentId());

    assertEquals(0, runtimeService.createEventSubscriptionQuery().count());

    taskService.complete(task1.getId());
    taskService.complete(task2.getId());

    assertProcessEnded(processInstanceId);

  }

  @Deployment
  public void testNonInterruptingEventInCombinationWithReceiveTaskInsideSubProcess() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // when (1)
    runtimeService.correlateMessage("firstMessage");

    // then (1)
    assertEquals(1, taskService.createTaskQuery().count());

    Task task1 = taskService.createTaskQuery()
        .taskDefinitionKey("task1")
        .singleResult();
    assertNotNull(task1);

    Execution task1Execution = runtimeService
        .createExecutionQuery()
        .activityId("task1")
        .singleResult();

    assertFalse(processInstanceId.equals(((ExecutionEntity) task1Execution).getParentId()));

    // when (2)
    runtimeService.correlateMessage("secondMessage");

    // then (2)
    assertEquals(2, taskService.createTaskQuery().count());

    task1 = taskService.createTaskQuery()
        .taskDefinitionKey("task1")
        .singleResult();
    assertNotNull(task1);

    task1Execution = runtimeService
        .createExecutionQuery()
        .activityId("task1")
        .singleResult();

    assertFalse(processInstanceId.equals(((ExecutionEntity) task1Execution).getParentId()));

    Task task2 = taskService.createTaskQuery()
        .taskDefinitionKey("task2")
        .singleResult();
    assertNotNull(task2);

    Execution task2Execution = runtimeService
        .createExecutionQuery()
        .activityId("task1")
        .singleResult();

    assertFalse(processInstanceId.equals(((ExecutionEntity) task2Execution).getParentId()));

    assertTrue(((ExecutionEntity) task1Execution).getParentId().equals(((ExecutionEntity) task2Execution).getParentId()));

    assertEquals(0, runtimeService.createEventSubscriptionQuery().count());

    taskService.complete(task1.getId());
    taskService.complete(task2.getId());

    assertProcessEnded(processInstanceId);
  }

  @Deployment
  public void testNonInterruptingEventInCombinationWithUserTask() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // when (1)
    runtimeService.correlateMessage("firstMessage");

    // then (1)
    assertEquals(2, taskService.createTaskQuery().count());

    Task task1 = taskService.createTaskQuery()
        .taskDefinitionKey("task1")
        .singleResult();
    assertNotNull(task1);

    Execution task1Execution = runtimeService
        .createExecutionQuery()
        .activityId("task1")
        .singleResult();

    assertEquals(processInstanceId, ((ExecutionEntity) task1Execution).getParentId());

    Task innerTask = taskService.createTaskQuery()
        .taskDefinitionKey("innerTask")
        .singleResult();
    assertNotNull(innerTask);

    Execution task2Execution = runtimeService
        .createExecutionQuery()
        .activityId("innerTask")
        .singleResult();

    assertEquals(processInstanceId, ((ExecutionEntity) task2Execution).getParentId());

    // when (2)
    taskService.complete(innerTask.getId());

    // then (2)
    assertEquals(2, taskService.createTaskQuery().count());

    task1 = taskService.createTaskQuery()
        .taskDefinitionKey("task1")
        .singleResult();
    assertNotNull(task1);

    task1Execution = runtimeService
        .createExecutionQuery()
        .activityId("task1")
        .singleResult();

    assertEquals(processInstanceId, ((ExecutionEntity) task1Execution).getParentId());

    Task task2 = taskService.createTaskQuery()
        .taskDefinitionKey("task2")
        .singleResult();
    assertNotNull(task2);

    task2Execution = runtimeService
        .createExecutionQuery()
        .activityId("task2")
        .singleResult();

    assertEquals(processInstanceId, ((ExecutionEntity) task2Execution).getParentId());

    assertEquals(0, runtimeService.createEventSubscriptionQuery().count());

    taskService.complete(task1.getId());
    taskService.complete(task2.getId());

    assertProcessEnded(processInstanceId);
  }

  @Deployment
  public void testNonInterruptingEventInCombinationWithUserTaskInsideSubProcess() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // when (1)
    runtimeService.correlateMessage("firstMessage");

    // then (1)
    assertEquals(2, taskService.createTaskQuery().count());

    Task task1 = taskService.createTaskQuery()
        .taskDefinitionKey("task1")
        .singleResult();
    assertNotNull(task1);

    Execution task1Execution = runtimeService
        .createExecutionQuery()
        .activityId("task1")
        .singleResult();

    assertFalse(processInstanceId.equals(((ExecutionEntity) task1Execution).getParentId()));

    Task innerTask = taskService.createTaskQuery()
        .taskDefinitionKey("innerTask")
        .singleResult();
    assertNotNull(innerTask);

    Execution task2Execution = runtimeService
        .createExecutionQuery()
        .activityId("innerTask")
        .singleResult();

    assertFalse(processInstanceId.equals(((ExecutionEntity) task2Execution).getParentId()));

    assertTrue(((ExecutionEntity) task1Execution).getParentId().equals(((ExecutionEntity) task2Execution).getParentId()));

    // when (2)
    taskService.complete(innerTask.getId());

    // then (2)
    assertEquals(2, taskService.createTaskQuery().count());

    task1 = taskService.createTaskQuery()
        .taskDefinitionKey("task1")
        .singleResult();
    assertNotNull(task1);

    task1Execution = runtimeService
        .createExecutionQuery()
        .activityId("task1")
        .singleResult();

    assertFalse(processInstanceId.equals(((ExecutionEntity) task1Execution).getParentId()));

    Task task2 = taskService.createTaskQuery()
        .taskDefinitionKey("task2")
        .singleResult();
    assertNotNull(task2);

    task2Execution = runtimeService
        .createExecutionQuery()
        .activityId("task2")
        .singleResult();

    assertFalse(processInstanceId.equals(((ExecutionEntity) task2Execution).getParentId()));

    assertTrue(((ExecutionEntity) task1Execution).getParentId().equals(((ExecutionEntity) task2Execution).getParentId()));

    assertEquals(0, runtimeService.createEventSubscriptionQuery().count());

    taskService.complete(task1.getId());
    taskService.complete(task2.getId());

    assertProcessEnded(processInstanceId);
  }

  @Deployment
  public void testNonInterruptingWithUserTaskAndBoundaryEvent() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // when (1)
    runtimeService.correlateMessage("firstMessage");

    // then (1)
    assertEquals(2, taskService.createTaskQuery().count());

    Task task1 = taskService.createTaskQuery()
        .taskDefinitionKey("task1")
        .singleResult();
    assertNotNull(task1);

    Execution task1Execution = runtimeService
        .createExecutionQuery()
        .activityId("task1")
        .singleResult();

    assertEquals(processInstanceId, ((ExecutionEntity) task1Execution).getParentId());

    Task task2 = taskService.createTaskQuery()
        .taskDefinitionKey("innerTask")
        .singleResult();
    assertNotNull(task2);

    Execution task2Execution = runtimeService
        .createExecutionQuery()
        .activityId("innerTask")
        .singleResult();

    assertFalse(processInstanceId.equals(((ExecutionEntity) task2Execution).getParentId()));

    // when (2)
    taskService.complete(task2.getId());

    // then (2)
    assertEquals(2, taskService.createTaskQuery().count());

    task1 = taskService.createTaskQuery()
        .taskDefinitionKey("task1")
        .singleResult();
    assertNotNull(task1);

    task1Execution = runtimeService
        .createExecutionQuery()
        .activityId("task1")
        .singleResult();

    assertEquals(processInstanceId, ((ExecutionEntity) task1Execution).getParentId());

    task2 = taskService.createTaskQuery()
        .taskDefinitionKey("task2")
        .singleResult();
    assertNotNull(task2);

    task2Execution = runtimeService
        .createExecutionQuery()
        .activityId("tasks")
        .singleResult();

    assertEquals(processInstanceId, ((ExecutionEntity) task1Execution).getParentId());

    assertEquals(0, runtimeService.createEventSubscriptionQuery().count());

    taskService.complete(task1.getId());
    taskService.complete(task2.getId());

    assertProcessEnded(processInstanceId);
  }

}
