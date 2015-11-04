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

package org.camunda.bpm.engine.test.dmn.businessruletask;

import java.util.Collections;

import org.camunda.bpm.dmn.engine.DmnDecisionOutput;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.Variables;

/**
 * Tests the decision result that is retrieved by an execution listener.
 *
 * @author Philipp Ossler
 */
public class DmnDecisionResultListenerTest extends PluggableProcessEngineTestCase {

  protected static final String TEST_PROCESS = "org/camunda/bpm/engine/test/dmn/result/DmnDecisionResultTest.bpmn20.xml";
  protected static final String TEST_DECISION = "org/camunda/bpm/engine/test/dmn/result/DmnDecisionResultTest.dmn11.xml";
  protected static final String TEST_DECISION_COLLECT_SUM = "org/camunda/bpm/engine/test/dmn/result/DmnDecisionResultCollectSumHitPolicyTest.dmn11.xml";
  protected static final String TEST_DECISION_COLLECT_COUNT = "org/camunda/bpm/engine/test/dmn/result/DmnDecisionResultCollectCountHitPolicyTest.dmn11.xml";

  protected DmnDecisionResult ruleResult;

  @Deployment(resources = { TEST_PROCESS, TEST_DECISION})
  public void testNoOutput() {
    startTestProcess("no output");

    assertTrue("The decision result 'ruleResult' should be empty", ruleResult.isEmpty());
  }

  @Deployment(resources = { TEST_PROCESS, TEST_DECISION})
  public void testEmptyOutput() {
    startTestProcess("empty output");

    assertFalse("The decision result 'ruleResult' should not be empty", ruleResult.isEmpty());

    DmnDecisionOutput decisionOutput = ruleResult.get(0);
    assertNull(decisionOutput.getFirstValue());
  }

  @Deployment(resources = { TEST_PROCESS, TEST_DECISION})
  public void testEmptyMap() {
    startTestProcess("empty map");

    assertEquals(2, ruleResult.size());

    for (DmnDecisionOutput output : ruleResult) {
      assertTrue("The decision output should be empty", output.isEmpty());
    }
  }

  @Deployment(resources = { TEST_PROCESS, TEST_DECISION})
  public void testSingleEntry() {
    startTestProcess("single entry");

    DmnDecisionOutput firstOutput = ruleResult.get(0);
    assertEquals("foo", firstOutput.getFirstValue());
    assertEquals(Variables.stringValue("foo"), firstOutput.getFirstValueTyped());
  }

  @Deployment(resources = { TEST_PROCESS, TEST_DECISION})
  public void testMultipleEntries() {
    startTestProcess("multiple entries");

    DmnDecisionOutput firstOutput = ruleResult.get(0);
    assertEquals("foo", firstOutput.get("result1"));
    assertEquals("bar", firstOutput.get("result2"));

    assertEquals(Variables.stringValue("foo"), firstOutput.getValueTyped("result1"));
    assertEquals(Variables.stringValue("bar"), firstOutput.getValueTyped("result2"));
  }

  @Deployment(resources = { TEST_PROCESS, TEST_DECISION})
  public void testSingleEntryList() {
    startTestProcess("single entry list");

    assertEquals(2, ruleResult.size());

    for (DmnDecisionOutput output : ruleResult) {
      assertEquals("foo", output.getFirstValue());
      assertEquals(Variables.stringValue("foo"), output.getFirstValueTyped());
    }
  }

  @Deployment(resources = { TEST_PROCESS, TEST_DECISION})
  public void testMultipleEntriesList() {
    startTestProcess("multiple entries list");

    assertEquals(2, ruleResult.size());

    for (DmnDecisionOutput output : ruleResult) {
      assertEquals(2, output.size());
      assertEquals("foo", output.get("result1"));
      assertEquals("bar", output.get("result2"));

      assertEquals(Variables.stringValue("foo"), output.getValueTyped("result1"));
      assertEquals(Variables.stringValue("bar"), output.getValueTyped("result2"));
    }
  }

  @Deployment(resources = { TEST_PROCESS, TEST_DECISION_COLLECT_COUNT })
  public void testCollectCountHitPolicyNoOutput() {
    startTestProcess("no output");

    assertEquals(1, ruleResult.size());
    DmnDecisionOutput firstOutput = ruleResult.get(0);

    assertEquals(0, firstOutput.getFirstValue());
    assertEquals(Variables.integerValue(0), firstOutput.getFirstValueTyped());
  }

  @Deployment(resources = { TEST_PROCESS, TEST_DECISION_COLLECT_SUM })
  public void testCollectSumHitPolicyNoOutput() {
    startTestProcess("no output");

    assertTrue("The decision result 'ruleResult' should be empty", ruleResult.isEmpty());
  }

  @Deployment(resources = { TEST_PROCESS, TEST_DECISION_COLLECT_SUM })
  public void testCollectSumHitPolicySingleEntry() {
    startTestProcess("single entry");

    assertEquals(1, ruleResult.size());
    DmnDecisionOutput firstOutput = ruleResult.get(0);

    assertEquals(12, firstOutput.getFirstValue());
    assertEquals(Variables.integerValue(12), firstOutput.getFirstValueTyped());
  }

  @Deployment(resources = { TEST_PROCESS, TEST_DECISION_COLLECT_SUM })
  public void testCollectSumHitPolicySingleEntryList() {
    startTestProcess("single entry list");

    assertEquals(1, ruleResult.size());
    DmnDecisionOutput firstOutput = ruleResult.get(0);

    assertEquals(33, firstOutput.getFirstValue());
    assertEquals(Variables.integerValue(33), firstOutput.getFirstValueTyped());
  }

  protected ProcessInstance startTestProcess(String input) {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess", Collections.<String, Object>singletonMap("input", input));

    // get the result from an execution listener that is invoked at the end of the business rule activity
    ruleResult = DecisionResultTestListener.getDecisionResult();
    assertNotNull(ruleResult);

    return processInstance;
  }

  @Override
  protected void tearDown() throws Exception {
    // reset the invoked execution listener
    DecisionResultTestListener.reset();
  }

}
