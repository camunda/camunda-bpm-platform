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
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Before;

public class DmnScriptOutputTest extends PluggableProcessEngineTestCase {

  public static final String TEST_PROCESS = "org/camunda/bpm/engine/test/dmn/DmnScriptOutputTest.bpmn20.xml";
  public static final String TEST_DECISION = "org/camunda/bpm/engine/test/dmn/DmnScriptOutputTest.dmn10.xml";

  @Before
  public void setUp() {
    deploymentId = repositoryService.createDeployment()
      .addClasspathResource(TEST_PROCESS)
      .addClasspathResource(TEST_DECISION)
      .deploy().getId();
  }

  public void testNoOutput() {
    ProcessInstance processInstance = startTestProcess("no output");

    Object ruleResult = runtimeService.getVariable(processInstance.getId(), "ruleResult");
    Object scriptResult = runtimeService.getVariable(processInstance.getId(), "scriptResult");

    assertNull(ruleResult);
    assertNull(scriptResult);
  }

  @SuppressWarnings("unchecked")
  public void testEmptyOutput() {
    ProcessInstance processInstance = startTestProcess("empty output");

    Map<String, Object> ruleResult = (Map<String, Object>) runtimeService.getVariable(processInstance.getId(), "ruleResult");
    Map<String, Object> scriptResult = (Map<String, Object>) runtimeService.getVariable(processInstance.getId(), "scriptResult");

    assertNotNull(ruleResult);
    assertNotNull(scriptResult);

    assertTrue(ruleResult.isEmpty());
    assertTrue(scriptResult.isEmpty());
  }

  @SuppressWarnings("unchecked")
  public void testEmptyMap() {
    ProcessInstance processInstance = startTestProcess("empty map");

    List<Object> ruleResult = (List<Object>) runtimeService.getVariable(processInstance.getId(), "ruleResult");
    List<Object> scriptResult = (List<Object>) runtimeService.getVariable(processInstance.getId(), "scriptResult");

    assertNotNull(ruleResult);
    assertNotNull(scriptResult);

    assertEquals(2, ruleResult.size());
    assertEquals(2, scriptResult.size());

    for (Object output : ruleResult) {
      assertNull(output);
    }

    for (Object output : scriptResult) {
      assertNull(output);
    }
  }

  public void testSingleEntry() {
    ProcessInstance processInstance = startTestProcess("single entry");

    String ruleResult = (String) runtimeService.getVariable(processInstance.getId(), "ruleResult");
    String scriptResult = (String) runtimeService.getVariable(processInstance.getId(), "scriptResult");

    assertNotNull(ruleResult);
    assertNotNull(scriptResult);

    assertEquals("foo", ruleResult);
    assertEquals("foo", scriptResult);
  }

  @SuppressWarnings("unchecked")
  public void testMultipleEntries() {
    ProcessInstance processInstance = startTestProcess("multiple entries");

    Map<String, Object> ruleResult = (Map<String, Object>) runtimeService.getVariable(processInstance.getId(), "ruleResult");
    Map<String, Object> scriptResult = (Map<String, Object>) runtimeService.getVariable(processInstance.getId(), "scriptResult");

    assertNotNull(ruleResult);
    assertNotNull(scriptResult);

    assertEquals("foo", ruleResult.get("result1"));
    assertEquals("foo", ruleResult.get("result2"));

    assertEquals("foo", scriptResult.get("result1"));
    assertEquals("foo", scriptResult.get("result2"));
  }

  @SuppressWarnings("unchecked")
  public void testSingleEntryList() {
    ProcessInstance processInstance = startTestProcess("single entry list");

    List<Object> ruleResult = (List<Object>) runtimeService.getVariable(processInstance.getId(), "ruleResult");
    List<Object> scriptResult = (List<Object>) runtimeService.getVariable(processInstance.getId(), "scriptResult");

    assertNotNull(ruleResult);
    assertNotNull(scriptResult);

    assertEquals(2, ruleResult.size());
    assertEquals(2, scriptResult.size());

    for (Object output : ruleResult) {
      assertEquals("foo", output);
    }

    for (Object output : scriptResult) {
      assertEquals("foo", output);
    }
  }

  @SuppressWarnings("unchecked")
  public void testMultipleEntriesList() {
    ProcessInstance processInstance = startTestProcess("multiple entries list");

    List<Map<String, Object>> ruleResult = (List<Map<String, Object>>) runtimeService.getVariable(processInstance.getId(), "ruleResult");
    List<Map<String, Object>> scriptResult = (List<Map<String, Object>>) runtimeService.getVariable(processInstance.getId(), "scriptResult");

    assertNotNull(ruleResult);
    assertNotNull(scriptResult);

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
    return runtimeService.startProcessInstanceByKey("testProcess", Collections.<String, Object>singletonMap("input", input));
  }

}
