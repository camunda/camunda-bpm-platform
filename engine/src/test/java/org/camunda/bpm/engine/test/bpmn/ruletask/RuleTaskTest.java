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

package org.camunda.bpm.engine.test.bpmn.ruletask;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;


/**
 * @author Bernd Ruecker
 */
public class RuleTaskTest extends PluggableProcessEngineTestCase {

  @Deployment
  public void testJavaDelegate() {
    DummyServiceTask.wasExecuted = false;
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("businessRuleTaskJavaDelegate");
    
    assertProcessEnded(processInstance.getId());
    assertTrue(DummyServiceTask.wasExecuted);
  }

}
