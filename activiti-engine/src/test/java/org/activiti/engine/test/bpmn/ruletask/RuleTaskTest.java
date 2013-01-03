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

package org.activiti.engine.test.bpmn.ruletask;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;


/**
 * @author Bernd Ruecker
 */
public class RuleTaskTest extends PluggableActivitiTestCase {
  
  @Deployment
  public void testJavaDelegate() {
    DummyServiceTask.wasExecuted = false;
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("businessRuleTaskJavaDelegate");
    
    assertProcessEnded(processInstance.getId());
    assertTrue(DummyServiceTask.wasExecuted);
  }

}
