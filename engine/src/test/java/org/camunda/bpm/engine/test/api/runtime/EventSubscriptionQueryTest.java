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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.EventSubscriptionQueryImpl;
import org.camunda.bpm.engine.impl.event.EventType;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.EventSubscriptionQuery;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Assert;


/**
 * @author Daniel Meyer
 */
public class EventSubscriptionQueryTest extends PluggableProcessEngineTestCase {

  public void testQueryByEventSubscriptionId() {
    createExampleEventSubscriptions();

    List<EventSubscription> list = runtimeService.createEventSubscriptionQuery()
        .eventName("messageName2")
        .list();
    assertEquals(1, list.size());

    EventSubscription eventSubscription = list.get(0);

    EventSubscriptionQuery query = runtimeService.createEventSubscriptionQuery()
        .eventSubscriptionId(eventSubscription.getId());

    assertEquals(1, query.count());
    assertEquals(1, query.list().size());
    assertNotNull(query.singleResult());

    try {
      runtimeService.createEventSubscriptionQuery().eventSubscriptionId(null).list();
      fail("Expected ProcessEngineException");
    } catch (ProcessEngineException e) {
    }

    cleanDb();
  }

  public void testQueryByEventName() {

    createExampleEventSubscriptions();

    List<EventSubscription> list = runtimeService.createEventSubscriptionQuery()
      .eventName("messageName")
      .list();
    assertEquals(2, list.size());

    list = runtimeService.createEventSubscriptionQuery()
      .eventName("messageName2")
      .list();
    assertEquals(1, list.size());

    try {
      runtimeService.createEventSubscriptionQuery().eventName(null).list();
      fail("Expected ProcessEngineException");
    } catch (ProcessEngineException e) {
    }

    cleanDb();

  }

  public void testQueryByEventType() {

    createExampleEventSubscriptions();

    List<EventSubscription> list = runtimeService.createEventSubscriptionQuery()
      .eventType("signal")
      .list();
    assertEquals(1, list.size());

    list = runtimeService.createEventSubscriptionQuery()
      .eventType("message")
      .list();
    assertEquals(2, list.size());

    try {
      runtimeService.createEventSubscriptionQuery().eventType(null).list();
      fail("Expected ProcessEngineException");
    } catch (ProcessEngineException e) {
    }

    cleanDb();

  }

  public void testQueryByActivityId() {

    createExampleEventSubscriptions();

    List<EventSubscription> list = runtimeService.createEventSubscriptionQuery()
      .activityId("someOtherActivity")
      .list();
    assertEquals(1, list.size());

    list = runtimeService.createEventSubscriptionQuery()
      .activityId("someActivity")
      .eventType("message")
      .list();
    assertEquals(2, list.size());

    try {
      runtimeService.createEventSubscriptionQuery().activityId(null).list();
      fail("Expected ProcessEngineException");
    } catch (ProcessEngineException e) {
    }

    cleanDb();

  }

  @Deployment
  public void testQueryByExecutionId() {

    // starting two instances:
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("catchSignal");
    runtimeService.startProcessInstanceByKey("catchSignal");

    // test query by process instance id
    EventSubscription subscription = runtimeService.createEventSubscriptionQuery()
      .processInstanceId(processInstance.getId())
      .singleResult();
    assertNotNull(subscription);

    Execution executionWaitingForSignal = runtimeService.createExecutionQuery()
      .activityId("signalEvent")
      .processInstanceId(processInstance.getId())
      .singleResult();

    // test query by execution id
    EventSubscription signalSubscription = runtimeService.createEventSubscriptionQuery()
      .executionId(executionWaitingForSignal.getId())
      .singleResult();
    assertNotNull(signalSubscription);

    assertEquals(signalSubscription, subscription);

    try {
      runtimeService.createEventSubscriptionQuery().executionId(null).list();
      fail("Expected ProcessEngineException");
    } catch (ProcessEngineException e) {
    }

    cleanDb();

  }

  public void testQuerySorting() {
    createExampleEventSubscriptions();
    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery().orderByCreated().asc().list();
    Assert.assertEquals(3, eventSubscriptions.size());

    Assert.assertTrue(eventSubscriptions.get(0).getCreated().compareTo(eventSubscriptions.get(1).getCreated()) < 0);
    Assert.assertTrue(eventSubscriptions.get(1).getCreated().compareTo(eventSubscriptions.get(2).getCreated()) < 0);

    cleanDb();
  }

  @Deployment
  public void testMultipleEventSubscriptions() {
    String message = "cancelation-requested";

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    assertTrue(areJobsAvailable());

    long eventSubscriptionCount = runtimeService.createEventSubscriptionQuery().count();
    assertEquals(2, eventSubscriptionCount);

    EventSubscription messageEvent = runtimeService.createEventSubscriptionQuery().eventType("message").singleResult();
    assertEquals(message, messageEvent.getEventName());

    EventSubscription compensationEvent = runtimeService.createEventSubscriptionQuery().eventType("compensate").singleResult();
    assertNull(compensationEvent.getEventName());

    runtimeService.createMessageCorrelation(message).processInstanceId(processInstance.getId()).correlate();

    assertProcessEnded(processInstance.getId());
  }


  protected void createExampleEventSubscriptions() {
    processEngineConfiguration.getCommandExecutorTxRequired()
    .execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        Calendar calendar = new GregorianCalendar();


        EventSubscriptionEntity messageEventSubscriptionEntity1 = new EventSubscriptionEntity(EventType.MESSAGE);
        messageEventSubscriptionEntity1.setEventName("messageName");
        messageEventSubscriptionEntity1.setActivityId("someActivity");
        calendar.set(2001, 1, 1);
        messageEventSubscriptionEntity1.setCreated(calendar.getTime());
        messageEventSubscriptionEntity1.insert();

        EventSubscriptionEntity messageEventSubscriptionEntity2 = new EventSubscriptionEntity(EventType.MESSAGE);
        messageEventSubscriptionEntity2.setEventName("messageName");
        messageEventSubscriptionEntity2.setActivityId("someActivity");
        calendar.set(2000, 1, 1);
        messageEventSubscriptionEntity2.setCreated(calendar.getTime());
        messageEventSubscriptionEntity2.insert();

        EventSubscriptionEntity signalEventSubscriptionEntity3 = new EventSubscriptionEntity(EventType.SIGNAL);
        signalEventSubscriptionEntity3.setEventName("messageName2");
        signalEventSubscriptionEntity3.setActivityId("someOtherActivity");
        calendar.set(2002, 1, 1);
        signalEventSubscriptionEntity3.setCreated(calendar.getTime());
        signalEventSubscriptionEntity3.insert();

        return null;
      }
    });
  }

  protected void cleanDb() {
    processEngineConfiguration.getCommandExecutorTxRequired()
    .execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        final List<EventSubscription> subscriptions = new EventSubscriptionQueryImpl().list();
        for (EventSubscription eventSubscriptionEntity : subscriptions) {
          ((EventSubscriptionEntity) eventSubscriptionEntity).delete();
        }
        return null;
      }
    });

  }


}
