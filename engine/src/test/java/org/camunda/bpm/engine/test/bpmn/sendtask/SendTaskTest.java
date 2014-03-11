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

package org.camunda.bpm.engine.test.bpmn.sendtask;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;


/**
 * @author Kristin Polenz
 */
public class SendTaskTest extends PluggableProcessEngineTestCase {

  @Deployment
  public void testJavaDelegate() {
    DummySendTask.wasExecuted = false;
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("sendTaskJavaDelegate");

    assertProcessEnded(processInstance.getId());
    assertTrue(DummySendTask.wasExecuted);
  }

  @Deployment
  public void testActivityName() {
    DummyActivityBehavior.wasExecuted = false;

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    runtimeService.signal(processInstance.getId());

    assertProcessEnded(processInstance.getId());

    assertTrue(DummyActivityBehavior.wasExecuted);

    assertNotNull(DummyActivityBehavior.currentActivityName);
    assertEquals("Task", DummyActivityBehavior.currentActivityName);

    assertNotNull(DummyActivityBehavior.currentActivityId);
    assertEquals("task", DummyActivityBehavior.currentActivityId);
  }

}
