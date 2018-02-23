/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.spin.plugin.variables;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionResultImpl;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.test.ResourceProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * The test is copied from the engine to check how JSON serialization will behave with DMN result object.
 *
 * @author Svetlana Dorokhova
 */
public class DmnBusinessRuleTaskResultMappingTest extends ResourceProcessEngineTestCase {

  protected static final String TEST_DECISION = "org/camunda/spin/plugin/DmnBusinessRuleTaskResultMappingTest.dmn11.xml";
  protected static final String CUSTOM_MAPPING_BPMN = "org/camunda/spin/plugin/DmnBusinessRuleTaskResultMappingTest.testCustomOutputMapping.bpmn20.xml";
  protected static final String SINGLE_ENTRY_BPMN = "org/camunda/spin/plugin/DmnBusinessRuleTaskResultMappingTest.testSingleEntry.bpmn20.xml";
  protected static final String DEFAULT_MAPPING_BPMN = "org/camunda/spin/plugin/DmnBusinessRuleTaskResultMappingTest.testDefaultMapping.bpmn20.xml";
  protected static final String STORE_DECISION_RESULT_BPMN = "org/camunda/spin/plugin/DmnBusinessRuleTaskResultMappingTest.testStoreDecisionResult.bpmn20.xml";

  public DmnBusinessRuleTaskResultMappingTest() {
    super("org/camunda/spin/plugin/json.camunda.cfg.xml");
  }

  @Deployment(resources = {STORE_DECISION_RESULT_BPMN, TEST_DECISION })
  public void testStoreDecisionResult() {
    ProcessInstance processInstance = startTestProcess("multiple entries");

    //deserialization is not working for this type of object -> deserializeValue parameter is false
    assertNotNull(runtimeService.getVariableTyped(processInstance.getId(), "result", false));
  }

  @Deployment(resources = {CUSTOM_MAPPING_BPMN, TEST_DECISION })
  public void testCustomOutputMapping() {
    ProcessInstance processInstance = startTestProcess("multiple entries");

    assertEquals("foo", runtimeService.getVariable(processInstance.getId(), "result1"));
    assertEquals(Variables.stringValue("foo"), runtimeService.getVariableTyped(processInstance.getId(), "result1"));

    assertEquals("bar", runtimeService.getVariable(processInstance.getId(), "result2"));
    assertEquals(Variables.stringValue("bar"), runtimeService.getVariableTyped(processInstance.getId(), "result2"));
  }

  @Deployment(resources = { SINGLE_ENTRY_BPMN, TEST_DECISION})
  public void testSingleEntryMapping() {
    ProcessInstance processInstance = startTestProcess("single entry");

    assertEquals("foo", runtimeService.getVariable(processInstance.getId(), "result"));
    assertEquals(Variables.stringValue("foo"), runtimeService.getVariableTyped(processInstance.getId(), "result"));
  }

  @Deployment(resources = { DEFAULT_MAPPING_BPMN, TEST_DECISION })
  public void testTransientDecisionResult() {
    // when a decision is evaluated and the result is stored in a transient variable "decisionResult"
    ProcessInstance processInstance = startTestProcess("single entry");

    // then the variable should not be available outside the business rule task
    assertNull(runtimeService.getVariable(processInstance.getId(), "decisionResult"));
    // and should not create an entry in history since it is not persistent
    assertNull(historyService.createHistoricVariableInstanceQuery().variableName("decisionResult").singleResult());
  }

  protected ProcessInstance startTestProcess(String input) {
    return runtimeService.startProcessInstanceByKey("testProcess", Collections.<String, Object>singletonMap("input", input));
  }

}
