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

package org.camunda.bpm.engine.test.bpmn.event.signal;

import java.util.List;
import org.camunda.bpm.engine.impl.event.EventType;

import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.EventSubscription;

/**
 * @author Philipp Ossler
 */
public class SignalEventDeploymentTest extends PluggableProcessEngineTestCase {

  private static final String SIGNAL_START_EVENT_PROCESS = "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTest.signalStartEvent.bpmn20.xml";
  private static final String SIGNAL_START_EVENT_PROCESS_NEW_VERSION = "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTest.signalStartEvent_v2.bpmn20.xml";

  public void testCreateEventSubscriptionOnDeployment() {
    deploymentId = repositoryService.createDeployment()
        .addClasspathResource(SIGNAL_START_EVENT_PROCESS)
        .deploy().getId();

    EventSubscription eventSubscription = runtimeService.createEventSubscriptionQuery().singleResult();
    assertNotNull(eventSubscription);

    assertEquals(EventType.SIGNAL.name(), eventSubscription.getEventType());
    assertEquals("alert", eventSubscription.getEventName());
    assertEquals("start", eventSubscription.getActivityId());
  }

  public void testUpdateEventSubscriptionOnDeployment(){
    deploymentId = repositoryService.createDeployment()
        .addClasspathResource(SIGNAL_START_EVENT_PROCESS)
        .deploy().getId();

    EventSubscription eventSubscription = runtimeService.createEventSubscriptionQuery().eventType("signal").singleResult();
    assertNotNull(eventSubscription);
    assertEquals("alert", eventSubscription.getEventName());

    // deploy a new version of the process with different signal name
    String newDeploymentId = repositoryService.createDeployment()
        .addClasspathResource(SIGNAL_START_EVENT_PROCESS_NEW_VERSION)
        .deploy().getId();

    ProcessDefinition newProcessDefinition = repositoryService.createProcessDefinitionQuery().latestVersion().singleResult();
    assertEquals(2, newProcessDefinition.getVersion());

    List<EventSubscription> newEventSubscriptions = runtimeService.createEventSubscriptionQuery().eventType("signal").list();
    // only one event subscription for the new version of the process definition
    assertEquals(1, newEventSubscriptions.size());

    EventSubscriptionEntity newEventSubscription = (EventSubscriptionEntity) newEventSubscriptions.iterator().next();
    assertEquals(newProcessDefinition.getId(), newEventSubscription.getConfiguration());
    assertEquals("abort", newEventSubscription.getEventName());

    // clean db
    repositoryService.deleteDeployment(newDeploymentId);
  }

  public void testAsyncSignalStartEventDeleteDeploymentWhileAsync() {
    // given a deployment
    org.camunda.bpm.engine.repository.Deployment deployment =
        repositoryService.createDeployment()
          .addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTest.signalStartEvent.bpmn20.xml")
          .addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.throwAlertSignalAsync.bpmn20.xml")
          .deploy();

    // and an active job for asynchronously triggering a signal start event
    runtimeService.startProcessInstanceByKey("throwSignalAsync");

    // then deleting the deployment succeeds
    repositoryService.deleteDeployment(deployment.getId(), true);

    assertEquals(0, repositoryService.createDeploymentQuery().count());

    int historyLevel = processEngineConfiguration.getHistoryLevel().getId();
    if (historyLevel >= HistoryLevel.HISTORY_LEVEL_FULL.getId()) {
      // and there are no job logs left
      assertEquals(0, historyService.createHistoricJobLogQuery().count());
    }

  }

}
