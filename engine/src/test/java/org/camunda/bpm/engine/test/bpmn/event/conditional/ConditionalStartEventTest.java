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

import java.util.List;

import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.junit.After;
import org.junit.Test;

public class ConditionalStartEventTest extends AbstractConditionalEventTestCase {

  private List<EventSubscription> eventSubscriptions;

  @Override
  public void checkIfProcessCanBeFinished() {
  }

  @After
  public void grumpy() {
//    for (final EventSubscription eventSubscription : eventSubscriptions) {
//      processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {
//        public Void execute(CommandContext commandContext) {
//          commandContext.
//          getEventSubscriptionManager()
//          .deleteEventSubscription((EventSubscriptionEntity) eventSubscription);
//
//          return null;
//        }
//      });
//    }

  }

  @Test
  public void testDeploymentCreatesSubscriptions() {
    String deploymentId = repositoryService
        .createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/conditional/ConditionalStartEventTest.testSingleMessageStartEvent.bpmn20.xml")
        .deploy()
        .getId();

    eventSubscriptions = runtimeService.createEventSubscriptionQuery().list();

    assertEquals(1, eventSubscriptions.size());

    repositoryService.deleteDeployment(deploymentId);
  }

}
