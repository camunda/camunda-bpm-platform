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
package org.camunda.bpm.engine.test.bpmn.receivetask;

import java.util.List;

import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.impl.event.EventType;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;

/**
 * see https://app.camunda.com/jira/browse/CAM-1612
 *
 * @author Daniel Meyer
 * @author Danny Gräf
 * @author Falko Menge
 */
public class ReceiveTaskTest extends PluggableProcessEngineTestCase {

  private List<EventSubscription> getEventSubscriptionList() {
    return runtimeService.createEventSubscriptionQuery()
        .eventType(EventType.MESSAGE.name()).list();
  }

  private List<EventSubscription> getEventSubscriptionList(String activityId) {
    return runtimeService.createEventSubscriptionQuery()
        .eventType(EventType.MESSAGE.name()).activityId(activityId).list();
  }

  private String getExecutionId(String processInstanceId, String activityId) {
    return runtimeService.createExecutionQuery()
        .processInstanceId(processInstanceId).activityId(activityId).singleResult().getId();
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/receivetask/ReceiveTaskTest.simpleReceiveTask.bpmn20.xml")
  public void testReceiveTaskWithoutMessageReference() {

    // given: a process instance waiting in the receive task
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    // expect: there is no message event subscription created for a receive task without a message reference
    assertEquals(0, getEventSubscriptionList().size());

    // then: we can signal the waiting receive task
    runtimeService.signal(processInstance.getId());

    // expect: this ends the process instance
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/receivetask/ReceiveTaskTest.singleReceiveTask.bpmn20.xml")
  public void testSupportsLegacySignalingOnSingleReceiveTask() {

    // given: a process instance waiting in the receive task
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    // expect: there is a message event subscription for the task
    assertEquals(1, getEventSubscriptionList().size());

    // then: we can signal the waiting receive task
    runtimeService.signal(getExecutionId(processInstance.getId(), "waitState"));

    // expect: subscription is removed
    assertEquals(0, getEventSubscriptionList().size());

    // expect: this ends the process instance
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/receivetask/ReceiveTaskTest.singleReceiveTask.bpmn20.xml")
  public void testSupportsMessageEventReceivedOnSingleReceiveTask() {

    // given: a process instance waiting in the receive task
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    // expect: there is a message event subscription for the task
    List<EventSubscription> subscriptionList = getEventSubscriptionList();
    assertEquals(1, subscriptionList.size());
    EventSubscription subscription = subscriptionList.get(0);

    // then: we can trigger the event subscription
    runtimeService.messageEventReceived(subscription.getEventName(), subscription.getExecutionId());

    // expect: subscription is removed
    assertEquals(0, getEventSubscriptionList().size());

    // expect: this ends the process instance
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/receivetask/ReceiveTaskTest.singleReceiveTask.bpmn20.xml")
  public void testSupportsCorrelateMessageOnSingleReceiveTask() {

    // given: a process instance waiting in the receive task
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    // expect: there is a message event subscription for the task
    List<EventSubscription> subscriptionList = getEventSubscriptionList();
    assertEquals(1, subscriptionList.size());
    EventSubscription subscription = subscriptionList.get(0);

    // then: we can correlate the event subscription
    runtimeService.correlateMessage(subscription.getEventName());

    // expect: subscription is removed
    assertEquals(0, getEventSubscriptionList().size());

    // expect: this ends the process instance
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/receivetask/ReceiveTaskTest.singleReceiveTask.bpmn20.xml")
  public void testSupportsCorrelateMessageByBusinessKeyOnSingleReceiveTask() {

    // given: a process instance with business key 23 waiting in the receive task
    ProcessInstance processInstance23 = runtimeService.startProcessInstanceByKey("testProcess", "23");

    // given: a 2nd process instance with business key 42 waiting in the receive task
    ProcessInstance processInstance42 = runtimeService.startProcessInstanceByKey("testProcess", "42");

    // expect: there is two message event subscriptions for the tasks
    List<EventSubscription> subscriptionList = getEventSubscriptionList();
    assertEquals(2, subscriptionList.size());

    // then: we can correlate the event subscription to one of the process instances
    runtimeService.correlateMessage("newInvoiceMessage", "23");

    // expect: one subscription is removed
    assertEquals(1, getEventSubscriptionList().size());

    // expect: this ends the process instance with business key 23
    assertProcessEnded(processInstance23.getId());

    // expect: other process instance is still running
    assertEquals(1, runtimeService.createProcessInstanceQuery().processInstanceId(processInstance42.getId()).count());

    // then: we can correlate the event subscription to the other process instance
    runtimeService.correlateMessage("newInvoiceMessage", "42");

    // expect: subscription is removed
    assertEquals(0, getEventSubscriptionList().size());

    // expect: this ends the process instance
    assertProcessEnded(processInstance42.getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/receivetask/ReceiveTaskTest.multiSequentialReceiveTask.bpmn20.xml")
  public void testSupportsLegacySignalingOnSequentialMultiReceiveTask() {

    // given: a process instance waiting in the first receive tasks
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    // expect: there is a message event subscription for the first task
    List<EventSubscription> subscriptionList = getEventSubscriptionList();
    assertEquals(1, subscriptionList.size());
    EventSubscription subscription = subscriptionList.get(0);
    String firstSubscriptionId = subscription.getId();

    // then: we can signal the waiting receive task
    runtimeService.signal(getExecutionId(processInstance.getId(), "waitState"));

    // expect: there is a new subscription created for the second receive task instance
    subscriptionList = getEventSubscriptionList();
    assertEquals(1, subscriptionList.size());
    subscription = subscriptionList.get(0);
    assertFalse(firstSubscriptionId.equals(subscription.getId()));

    // then: we can signal the second waiting receive task
    runtimeService.signal(getExecutionId(processInstance.getId(), "waitState"));

    // expect: no event subscription left
    assertEquals(0, getEventSubscriptionList().size());

    // expect: one user task is created
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    // expect: this ends the process instance
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/receivetask/ReceiveTaskTest.multiSequentialReceiveTask.bpmn20.xml")
  public void testSupportsMessageEventReceivedOnSequentialMultiReceiveTask() {

    // given: a process instance waiting in the first receive tasks
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    // expect: there is a message event subscription for the first task
    List<EventSubscription> subscriptionList = getEventSubscriptionList();
    assertEquals(1, subscriptionList.size());
    EventSubscription subscription = subscriptionList.get(0);
    String firstSubscriptionId = subscription.getId();

    // then: we can trigger the event subscription
    runtimeService.messageEventReceived(subscription.getEventName(), subscription.getExecutionId());

    // expect: there is a new subscription created for the second receive task instance
    subscriptionList = getEventSubscriptionList();
    assertEquals(1, subscriptionList.size());
    subscription = subscriptionList.get(0);
    assertFalse(firstSubscriptionId.equals(subscription.getId()));

    // then: we can trigger the second event subscription
    runtimeService.messageEventReceived(subscription.getEventName(), subscription.getExecutionId());

    // expect: no event subscription left
    assertEquals(0, getEventSubscriptionList().size());

    // expect: one user task is created
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    // expect: this ends the process instance
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/receivetask/ReceiveTaskTest.multiSequentialReceiveTask.bpmn20.xml")
  public void testSupportsCorrelateMessageOnSequentialMultiReceiveTask() {

    // given: a process instance waiting in the first receive tasks
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    // expect: there is a message event subscription for the first task
    List<EventSubscription> subscriptionList = getEventSubscriptionList();
    assertEquals(1, subscriptionList.size());
    EventSubscription subscription = subscriptionList.get(0);
    String firstSubscriptionId = subscription.getId();

    // then: we can trigger the event subscription
    runtimeService.correlateMessage(subscription.getEventName());

    // expect: there is a new subscription created for the second receive task instance
    subscriptionList = getEventSubscriptionList();
    assertEquals(1, subscriptionList.size());
    subscription = subscriptionList.get(0);
    assertFalse(firstSubscriptionId.equals(subscription.getId()));

    // then: we can trigger the second event subscription
    runtimeService.correlateMessage(subscription.getEventName());

    // expect: no event subscription left
    assertEquals(0, getEventSubscriptionList().size());

    // expect: one user task is created
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    // expect: this ends the process instance
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/receivetask/ReceiveTaskTest.multiParallelReceiveTask.bpmn20.xml")
  public void testSupportsLegacySignalingOnParallelMultiReceiveTask() {

    // given: a process instance waiting in two receive tasks
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    // expect: there are two message event subscriptions
    List<EventSubscription> subscriptions = getEventSubscriptionList();
    assertEquals(2, subscriptions.size());

    // expect: there are two executions
    List<Execution> executions = runtimeService.createExecutionQuery()
        .processInstanceId(processInstance.getId()).activityId("waitState")
        .messageEventSubscriptionName("newInvoiceMessage").list();
    assertEquals(2, executions.size());

    // then: we can signal both waiting receive task
    runtimeService.signal(executions.get(0).getId());
    runtimeService.signal(executions.get(1).getId());

    // expect: both event subscriptions are removed
    assertEquals(0, getEventSubscriptionList().size());

    // expect: this ends the process instance
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/receivetask/ReceiveTaskTest.multiParallelReceiveTask.bpmn20.xml")
  public void testSupportsMessageEventReceivedOnParallelMultiReceiveTask() {

    // given: a process instance waiting in two receive tasks
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    // expect: there are two message event subscriptions
    List<EventSubscription> subscriptions = getEventSubscriptionList();
    assertEquals(2, subscriptions.size());

    // then: we can trigger both event subscriptions
    runtimeService.messageEventReceived(subscriptions.get(0).getEventName(), subscriptions.get(0).getExecutionId());
    runtimeService.messageEventReceived(subscriptions.get(1).getEventName(), subscriptions.get(1).getExecutionId());

    // expect: both event subscriptions are removed
    assertEquals(0, getEventSubscriptionList().size());

    // expect: this ends the process instance
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/receivetask/ReceiveTaskTest.multiParallelReceiveTask.bpmn20.xml")
  public void testNotSupportsCorrelateMessageOnParallelMultiReceiveTask() {

    // given: a process instance waiting in two receive tasks
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    // expect: there are two message event subscriptions
    List<EventSubscription> subscriptions = getEventSubscriptionList();
    assertEquals(2, subscriptions.size());

    // then: we can not correlate an event
    try {
      runtimeService.correlateMessage(subscriptions.get(0).getEventName());
      fail("should throw a mismatch");
    } catch (MismatchingMessageCorrelationException e) {
      // expected
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/receivetask/ReceiveTaskTest.multiParallelReceiveTaskCompensate.bpmn20.xml")
  public void testSupportsMessageEventReceivedOnParallelMultiReceiveTaskWithCompensation() {

    // given: a process instance waiting in two receive tasks
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    // expect: there are two message event subscriptions
    List<EventSubscription> subscriptions = getEventSubscriptionList();
    assertEquals(2, subscriptions.size());

    // then: we can trigger the first event subscription
    runtimeService.messageEventReceived(subscriptions.get(0).getEventName(), subscriptions.get(0).getExecutionId());

    // expect: after completing the first receive task there is one event subscription for compensation
    assertEquals(1, runtimeService.createEventSubscriptionQuery()
        .eventType(EventType.COMPENSATE.name()).count());

    // then: we can trigger the second event subscription
    runtimeService.messageEventReceived(subscriptions.get(1).getEventName(), subscriptions.get(1).getExecutionId());

    // expect: there are three event subscriptions for compensation (two subscriptions for tasks and one for miBody)
    assertEquals(3, runtimeService.createEventSubscriptionQuery()
        .eventType(EventType.COMPENSATE.name()).count());

    // expect: one user task is created
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    // expect: this ends the process instance
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/receivetask/ReceiveTaskTest.multiParallelReceiveTaskBoundary.bpmn20.xml")
  public void testSupportsMessageEventReceivedOnParallelMultiInstanceWithBoundary() {

    // given: a process instance waiting in two receive tasks
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    // expect: there are three message event subscriptions
    assertEquals(3, getEventSubscriptionList().size());

    // expect: there are two message event subscriptions for the receive tasks
    List<EventSubscription> subscriptions = getEventSubscriptionList("waitState");
    assertEquals(2, subscriptions.size());

    // then: we can trigger both receive task event subscriptions
    runtimeService.messageEventReceived(subscriptions.get(0).getEventName(), subscriptions.get(0).getExecutionId());
    runtimeService.messageEventReceived(subscriptions.get(1).getEventName(), subscriptions.get(1).getExecutionId());

    // expect: all subscriptions are removed (boundary subscription is removed too)
    assertEquals(0, getEventSubscriptionList().size());

    // expect: this ends the process instance
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/receivetask/ReceiveTaskTest.multiParallelReceiveTaskBoundary.bpmn20.xml")
  public void testSupportsMessageEventReceivedOnParallelMultiInstanceWithBoundaryEventReceived() {

    // given: a process instance waiting in two receive tasks
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    // expect: there are three message event subscriptions
    assertEquals(3, getEventSubscriptionList().size());

    // expect: there is one message event subscription for the boundary event
    List<EventSubscription> subscriptions = getEventSubscriptionList("cancel");
    assertEquals(1, subscriptions.size());
    EventSubscription subscription = subscriptions.get(0);

    // then: we can trigger the boundary subscription to cancel both tasks
    runtimeService.messageEventReceived(subscription.getEventName(), subscription.getExecutionId());

    // expect: all subscriptions are removed (receive task subscriptions too)
    assertEquals(0, getEventSubscriptionList().size());

    // expect: this ends the process instance
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/receivetask/ReceiveTaskTest.subProcessReceiveTask.bpmn20.xml")
  public void testSupportsMessageEventReceivedOnSubProcessReceiveTask() {

    // given: a process instance waiting in the sub-process receive task
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    // expect: there is a message event subscription for the task
    List<EventSubscription> subscriptionList = getEventSubscriptionList();
    assertEquals(1, subscriptionList.size());
    EventSubscription subscription = subscriptionList.get(0);

    // then: we can trigger the event subscription
    runtimeService.messageEventReceived(subscription.getEventName(), subscription.getExecutionId());

    // expect: subscription is removed
    assertEquals(0, getEventSubscriptionList().size());

    // expect: this ends the process instance
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/receivetask/ReceiveTaskTest.multiSubProcessReceiveTask.bpmn20.xml")
  public void testSupportsMessageEventReceivedOnMultiSubProcessReceiveTask() {

    // given: a process instance waiting in two parallel sub-process receive tasks
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    // expect: there are two message event subscriptions
    List<EventSubscription> subscriptions = getEventSubscriptionList();
    assertEquals(2, subscriptions.size());

    // then: we can trigger both receive task event subscriptions
    runtimeService.messageEventReceived(subscriptions.get(0).getEventName(), subscriptions.get(0).getExecutionId());
    runtimeService.messageEventReceived(subscriptions.get(1).getEventName(), subscriptions.get(1).getExecutionId());

    // expect: subscriptions are removed
    assertEquals(0, getEventSubscriptionList().size());

    // expect: this ends the process instance
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/receivetask/ReceiveTaskTest.parallelGatewayReceiveTask.bpmn20.xml")
  public void testSupportsMessageEventReceivedOnReceiveTaskBehindParallelGateway() {

    // given: a process instance waiting in two receive tasks
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    // expect: there are two message event subscriptions
    List<EventSubscription> subscriptions = getEventSubscriptionList();
    assertEquals(2, subscriptions.size());

    // then: we can trigger both receive task event subscriptions
    runtimeService.messageEventReceived(subscriptions.get(0).getEventName(), subscriptions.get(0).getExecutionId());
    runtimeService.messageEventReceived(subscriptions.get(1).getEventName(), subscriptions.get(1).getExecutionId());

    // expect: subscriptions are removed
    assertEquals(0, getEventSubscriptionList().size());

    // expect: this ends the process instance
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/receivetask/ReceiveTaskTest.parallelGatewayReceiveTask.bpmn20.xml")
  public void testSupportsCorrelateMessageOnReceiveTaskBehindParallelGateway() {

    // given: a process instance waiting in two receive tasks
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    // expect: there are two message event subscriptions
    List<EventSubscription> subscriptions = getEventSubscriptionList();
    assertEquals(2, subscriptions.size());

    // then: we can trigger both receive task event subscriptions
    runtimeService.correlateMessage(subscriptions.get(0).getEventName());
    runtimeService.correlateMessage(subscriptions.get(1).getEventName());

    // expect: subscriptions are removed
    assertEquals(0, getEventSubscriptionList().size());

    // expect: this ends the process instance
    assertProcessEnded(processInstance.getId());
  }

  @Deployment
  public void testWaitStateBehavior() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("receiveTask");
    Execution execution = runtimeService.createExecutionQuery()
      .processInstanceId(pi.getId())
      .activityId("waitState")
      .singleResult();
    assertNotNull(execution);

    runtimeService.signal(execution.getId());
    assertProcessEnded(pi.getId());
  }
}
