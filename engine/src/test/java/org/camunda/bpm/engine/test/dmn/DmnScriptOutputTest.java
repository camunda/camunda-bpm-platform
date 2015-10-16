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

package org.camunda.bpm.engine.test.dmn;

import java.util.Collections;
import java.util.Map;

import org.camunda.bpm.dmn.engine.DmnDecisionOutput;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Before;

public class DmnScriptOutputTest extends PluggableProcessEngineTestCase {

  public static final String TEST_PROCESS = "org/camunda/bpm/engine/test/dmn/DmnScriptOutputTest.bpmn20.xml";
  public static final String TEST_DECISION = "org/camunda/bpm/engine/test/dmn/DmnScriptOutputTest.dmn10.xml";

  protected DmnDecisionResult ruleResult;
  protected DmnDecisionResult scriptResult;

  @Before
  public void setUp() {
    deploymentId = repositoryService.createDeployment()
      .addClasspathResource(TEST_PROCESS)
      .addClasspathResource(TEST_DECISION)
      .deploy().getId();
  }

  public void testNoOutput() {
    startTestProcess("no output");

    assertTrue("The decision result 'ruleResult' should be empty", ruleResult.isEmpty());
    assertTrue("The decision result 'scriptResult' should be empty", scriptResult.isEmpty());
  }

  @SuppressWarnings("unchecked")
  public void testEmptyOutput() {
    startTestProcess("empty output");

    assertFalse("The decision result 'ruleResult' should not be empty", ruleResult.isEmpty());
    assertFalse("The decision result 'scriptResult' should not be empty", scriptResult.isEmpty());

    DmnDecisionOutput decisionOutput = ruleResult.get(0);
    assertNull(decisionOutput.getFirstValue());

    decisionOutput = scriptResult.get(0);
    assertNull(decisionOutput.getFirstValue());
  }

  @SuppressWarnings("unchecked")
  public void testEmptyMap() {
    startTestProcess("empty map");

    assertEquals(2, ruleResult.size());
    assertEquals(2, scriptResult.size());

    for (DmnDecisionOutput output : ruleResult) {
      assertTrue("The decision output should be empty", output.isEmpty());
    }

    for (DmnDecisionOutput output : scriptResult) {
      assertTrue("The decision output should be empty", output.isEmpty());
    }
  }

  public void testSingleEntry() {
    startTestProcess("single entry");

    DmnDecisionOutput firstOutput = ruleResult.get(0);
    assertEquals("foo", firstOutput.getFirstValue());

    firstOutput = scriptResult.get(0);
    assertEquals("foo", firstOutput.getFirstValue());
  }

  @SuppressWarnings("unchecked")
  public void testMultipleEntries() {
    startTestProcess("multiple entries");

    DmnDecisionOutput firstOutput = ruleResult.get(0);
    assertEquals("foo", firstOutput.getValue("result1"));
    assertEquals("foo", firstOutput.getValue("result2"));

    firstOutput = scriptResult.get(0);
    assertEquals("foo", firstOutput.get("result1"));
    assertEquals("foo", firstOutput.get("result2"));
  }

  @SuppressWarnings("unchecked")
  public void testSingleEntryList() {
    startTestProcess("single entry list");

    assertEquals(2, ruleResult.size());
    assertEquals(2, scriptResult.size());

    for (DmnDecisionOutput output : ruleResult) {
      assertEquals("foo", output.getFirstValue());
    }

    for (DmnDecisionOutput output : scriptResult) {
      assertEquals("foo", output.getFirstValue());
    }
  }

  @SuppressWarnings("unchecked")
  public void testMultipleEntriesList() {
    startTestProcess("multiple entries list");

    assertEquals(2, ruleResult.size());
    assertEquals(2, scriptResult.size());

    for (Map<String, Object> output : ruleResult) {
      assertEquals(2, output.size());
      assertEquals("foo", output.get("result1"));
      assertEquals("foo", output.get("result2"));
    }

    for (Map<String, Object> output : scriptResult) {
      assertEquals(2, output.size());
      assertEquals("foo", output.get("result1"));
      assertEquals("foo", output.get("result2"));
    }
  }

  public ProcessInstance startTestProcess(String input) {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess", Collections.<String, Object>singletonMap("input", input));

    ruleResult = (DmnDecisionResult) runtimeService.getVariable(processInstance.getId(), "ruleResult");
    scriptResult = (DmnDecisionResult) runtimeService.getVariable(processInstance.getId(), "scriptResult");

    assertNotNull(ruleResult);
    assertNotNull(scriptResult);

    return processInstance;
  }

}
