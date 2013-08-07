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

package org.camunda.bpm.engine.test.standalone.rules;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.impl.test.ResourceProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;


/**
 * @author Tijs Rademakers
 */
public class RulesDeployerTest extends ResourceProcessEngineTestCase {

  public RulesDeployerTest() {
    super("org/camunda/bpm/engine/test/standalone/rules/rules.camunda.cfg.xml");
  }
  
  @Override
  protected void closeDownProcessEngine() {
    processEngine.close();
    super.closeDownProcessEngine();
  }

  @SuppressWarnings("unchecked")
  @Deployment(
    resources={"org/camunda/bpm/engine/test/standalone/rules/rulesDeploymentTestProcess.bpmn20.xml",
            "org/camunda/bpm/engine/test/standalone/rules/simpleRule1.drl"})
  public void testRulesDeployment() {
    Map<String, Object> variableMap = new HashMap<String, Object>();
    Order order = new Order();
    order.setItemCount(2);
    variableMap.put("order", order);
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("rulesDeployment", variableMap);
    assertNotNull(processInstance);
    assertTrue(processInstance.getProcessDefinitionId().startsWith("rulesDeployment:1"));
    
    runtimeService.getVariable(processInstance.getId(), "order");
    assertTrue(order.isValid());
    
    Collection<Object> ruleOutputList = (Collection<Object>)
        runtimeService.getVariable(processInstance.getId(), "rulesOutput");
    assertNotNull(ruleOutputList);
    assertEquals(1, ruleOutputList.size());
    order = (Order) ruleOutputList.iterator().next();
    assertTrue(order.isValid());
  }
}
