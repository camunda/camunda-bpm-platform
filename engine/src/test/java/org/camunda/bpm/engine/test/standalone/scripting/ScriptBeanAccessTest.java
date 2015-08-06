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

package org.camunda.bpm.engine.test.standalone.scripting;

import org.camunda.bpm.engine.impl.test.ResourceProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;


/**
 * @author Tom Baeyens
 */
public class ScriptBeanAccessTest extends ResourceProcessEngineTestCase {

  public ScriptBeanAccessTest() {
    super("org/camunda/bpm/engine/test/standalone/scripting/camunda.cfg.xml");
  }

  @Deployment
  public void testConfigurationBeanAccess() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("ScriptBeanAccess");
    assertEquals("myValue", runtimeService.getVariable(pi.getId(), "myVariable"));
  }

}
