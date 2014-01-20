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

import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.impl.event.MessageEventHandler;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;

import java.util.List;

/**
 * see https://app.camunda.com/jira/browse/CAM-1612
 *
 * @author Daniel Meyer
 * @author Danny Gräf
 */
public class ReceiveTaskTest extends PluggableProcessEngineTestCase {

  private List<EventSubscription> getEventSubscriptionList() {
    return runtimeService.createEventSubscriptionQuery()
        .eventType(MessageEventHandler.EVENT_HANDLER_TYPE).list();
  }

  private List<EventSubscription> getEventSubscriptionList(String activityId) {
    return runtimeService.createEventSubscriptionQuery()
        .eventType(MessageEventHandler.EVENT_HANDLER_TYPE).activityId(activityId).list();
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

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/receivetask/ReceiveTaskTest.multiSequentialReceiveTask.bpmn20.xml")
  public void testSupportsMessageEventReceivedOnSequentialMultiReceiveTask() {

    // given: a process instance waiting in the first receive tasks
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    // then: there are one message event subscription
    List<EventSubscription> subscriptionList = getEventSubscriptionList();
    assertEquals(1, subscriptionList.size());
    EventSubscription subscription = subscriptionList.get(0);

    // then: we can trigger the event subscription
    runtimeService.messageEventReceived(subscription.getEventName(), subscription.getExecutionId());

    // expect: the next event subscription is created
    subscriptionList = getEventSubscriptionList();
    assertEquals(1, subscriptionList.size());
    subscription = subscriptionList.get(0);

    // then: we can trigger the second event subscription
    runtimeService.messageEventReceived(subscription.getEventName(), subscription.getExecutionId());

    // expect: now event subscription left
    assertEquals(0, getEventSubscriptionList().size());

    // expect: this ends the process instance
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/receivetask/ReceiveTaskTest.multiSequentialReceiveTask.bpmn20.xml")
  public void testSupportsCorrelateMessageOnSequentialMultiReceiveTask() {

    // given: a process instance waiting in the first receive tasks
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    // then: there are one message event subscription
    List<EventSubscription> subscriptionList = getEventSubscriptionList();
    assertEquals(1, subscriptionList.size());
    EventSubscription subscription = subscriptionList.get(0);

    // then: we can trigger the event subscription
    runtimeService.correlateMessage(subscription.getEventName());

    // expect: the next event subscription is created
    subscriptionList = getEventSubscriptionList();
    assertEquals(1, subscriptionList.size());
    subscription = subscriptionList.get(0);

    // then: we can trigger the second event subscription
    runtimeService.correlateMessage(subscription.getEventName());

    // expect: now event subscription left
    assertEquals(0, getEventSubscriptionList().size());

    // expect: this ends the process instance
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/receivetask/ReceiveTaskTest.multiParallelReceiveTask.bpmn20.xml")
  public void testSupportsMessageEventReceivedOnParallelMultiReceiveTask() {

    // given: a process instance waiting in two receive tasks
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    // then: there are two message event subscriptions
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

    // then: there are two message event subscriptions
    List<EventSubscription> subscriptions = getEventSubscriptionList();
    assertEquals(2, subscriptions.size());

    // expect: we can not correlate an event
    try {
      runtimeService.correlateMessage(subscriptions.get(0).getEventName());
      fail("should throw a mismatch");
    } catch (MismatchingMessageCorrelationException e) {
      // expected
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/receivetask/ReceiveTaskTest.boundaryReceiveTask.bpmn20.xml")
  public void testSupportsMessageEventReceivedOnParallelMultiInstanceWithBoundary() {

    // given: a process instance waiting in the receive task
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    // then: there are three message event subscriptions
    assertEquals(3, getEventSubscriptionList().size());

    // then: there are two message event subscriptions for the receive tasks
    List<EventSubscription> subscriptions = getEventSubscriptionList("waitState");
    assertEquals(2, subscriptions.size());

    // then: we can trigger both receive task event subscriptions
    runtimeService.messageEventReceived(subscriptions.get(0).getEventName(), subscriptions.get(0).getExecutionId());
    runtimeService.messageEventReceived(subscriptions.get(1).getEventName(), subscriptions.get(1).getExecutionId());

    // expect: all subscriptions are removed (boundary is removed too)
    assertEquals(0, getEventSubscriptionList().size());

    // expect: this ends the process instance
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/receivetask/ReceiveTaskTest.boundaryReceiveTask.bpmn20.xml")
  public void testSupportsMessageEventReceivedOnParallelMultiInstanceWithBoundaryEventReceived() {

    // given: a process instance waiting in the receive task
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    // then: there are three message event subscriptions
    assertEquals(3, getEventSubscriptionList().size());

    // then: there is one message event subscriptions for the boundary event
    List<EventSubscription> subscriptions = getEventSubscriptionList("cancelBoundary");
    assertEquals(1, subscriptions.size());

    runtimeService.messageEventReceived(subscriptions.get(0).getEventName(), subscriptions.get(0).getExecutionId());

    // expect: all subscriptions are removed (receive task descriptions too)
    assertEquals(0, getEventSubscriptionList().size());

    // expect: this ends the process instance
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/receivetask/ReceiveTaskTest.subProcessReceiveTask.bpmn20.xml")
  public void testSupportsMessageEventReceivedOnSubProcessReceiveTask() {

    // given: a process instance waiting in the inner process receive task
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    // then: there is a message event subscription for the task
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

    // given: a process instance waiting in the parallel inner process receive task
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    // then: there are two message event subscriptions
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
}
