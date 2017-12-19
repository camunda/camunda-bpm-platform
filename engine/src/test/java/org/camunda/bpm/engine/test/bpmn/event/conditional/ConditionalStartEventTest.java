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

package org.camunda.bpm.engine.test.bpmn.event.conditional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.event.EventType;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.junit.Ignore;
import org.junit.Test;

public class ConditionalStartEventTest extends AbstractConditionalEventTestCase {

  private List<EventSubscription> eventSubscriptions;

  @Override
  public void checkIfProcessCanBeFinished() {
  }

  @Test
  public void testDeploymentCreatesSubscriptions() {
    String deploymentId = repositoryService
        .createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testSingleConditionalStartEvent.bpmn20.xml")
        .deploy()
        .getId();

    eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();

    assertEquals(1, eventSubscriptions.size());
    assertEquals(EventType.CONDITONAL.name(), eventSubscriptions.get(0).getEventType());

    repositoryService.deleteDeployment(deploymentId);

    eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();
    assertEquals(0, eventSubscriptions.size());
  }

  @Test
  public void testUpdateProcessVersionCancelsSubscriptions() {
    String deploymentId = repositoryService
        .createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testSingleConditionalStartEvent.bpmn20.xml")
        .deploy()
        .getId();

    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();

    assertEquals(1, eventSubscriptions.size());
    assertEquals(1, processDefinitions.size());

    String newDeploymentId = repositoryService
        .createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testSingleConditionalStartEvent.bpmn20.xml")
        .deploy()
        .getId();

    List<EventSubscription> newEventSubscriptions = runtimeService.createEventSubscriptionQuery().list();
    List<ProcessDefinition> newProcessDefinitions = repositoryService.createProcessDefinitionQuery().list();

    assertEquals(1, newEventSubscriptions.size());
    assertEquals(2, newProcessDefinitions.size());
    for (ProcessDefinition processDefinition : newProcessDefinitions) {
      if (processDefinition.getVersion() == 1) {
        for (EventSubscription subscription : newEventSubscriptions) {
          EventSubscriptionEntity subscriptionEntity = (EventSubscriptionEntity) subscription;
          assertFalse(subscriptionEntity.getConfiguration().equals(processDefinition.getId()));
        }
      } else {
        for (EventSubscription subscription : newEventSubscriptions) {
          EventSubscriptionEntity subscriptionEntity = (EventSubscriptionEntity) subscription;
          assertTrue(subscriptionEntity.getConfiguration().equals(processDefinition.getId()));
        }
      }
    }
    assertFalse(eventSubscriptions.equals(newEventSubscriptions));

    repositoryService.deleteDeployment(deploymentId);
    repositoryService.deleteDeployment(newDeploymentId);
  }

  @Test
  @Ignore("CAM-8635")
  public void testSameConditionFails() {
    try {
      repositoryService
          .createDeployment()
          .addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testTwoEqualConditionalStartEvent.bpmn20.xml")
          .deploy();
      fail("exception expected");
    } catch (ProcessEngineException e) {
      assertTrue(e.getMessage().contains("there already is a conditional event subscription for the same condition"));
    } finally {
      // clean db:
      List<org.camunda.bpm.engine.repository.Deployment> deployments = repositoryService.createDeploymentQuery().list();
      for (org.camunda.bpm.engine.repository.Deployment deployment : deployments) {
        repositoryService.deleteDeployment(deployment.getId(), true);
      }
      // Workaround for #CAM-4250: remove process definition of failed
      // deployment from deployment cache
      processEngineConfiguration.getDeploymentCache().getProcessDefinitionCache().clear();
    }
  }


}
