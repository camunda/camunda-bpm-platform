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

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Daniel Meyer
 *
 */
public class ReceiveTaskTest extends PluggableProcessEngineTestCase {

  // https://app.camunda.com/jira/browse/CAM-1612
  @Deployment
  public void FAILING_testSupportsMessageEventReceived() {

    // given: a process instance waiting in the receive task
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    // then

    // there is a message event subscription for the message
    EventSubscription eventSubscription = runtimeService.createEventSubscriptionQuery()
      .eventType("message")
      .singleResult();
    assertNotNull(eventSubscription);

    // we can trigger the event subsrciption
    runtimeService.messageEventReceived(eventSubscription.getEventName(), eventSubscription.getExecutionId());

    // this ends the process instance
    assertProcessEnded(processInstance.getId());

  }

}
