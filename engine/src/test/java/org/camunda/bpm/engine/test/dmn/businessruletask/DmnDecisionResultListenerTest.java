/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.DmnDecisionResultEntries;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.After;
import org.junit.Test;

/**
 * Tests the decision result that is retrieved by an execution listener.
 *
 * @author Philipp Ossler
 */
public class DmnDecisionResultListenerTest extends PluggableProcessEngineTest {

  protected static final String TEST_PROCESS = "org/camunda/bpm/engine/test/dmn/result/DmnDecisionResultTest.bpmn20.xml";
  protected static final String TEST_DECISION = "org/camunda/bpm/engine/test/dmn/result/DmnDecisionResultTest.dmn11.xml";
  protected static final String TEST_DECISION_COLLECT_SUM = "org/camunda/bpm/engine/test/dmn/result/DmnDecisionResultCollectSumHitPolicyTest.dmn11.xml";
  protected static final String TEST_DECISION_COLLECT_COUNT = "org/camunda/bpm/engine/test/dmn/result/DmnDecisionResultCollectCountHitPolicyTest.dmn11.xml";

  protected DmnDecisionResult results;

  @Deployment(resources = { TEST_PROCESS, TEST_DECISION})
  @Test
  public void testNoOutput() {
    startTestProcess("no output");

    assertTrue("The decision result 'ruleResult' should be empty", results.isEmpty());
  }

  @Deployment(resources = { TEST_PROCESS, TEST_DECISION})
  @Test
  public void testEmptyOutput() {
    startTestProcess("empty output");

    assertFalse("The decision result 'ruleResult' should not be empty", results.isEmpty());

    DmnDecisionResultEntries decisionOutput = results.get(0);
    assertNull(decisionOutput.getFirstEntry());
  }

  @Deployment(resources = { TEST_PROCESS, TEST_DECISION})
  @Test
  public void testEmptyMap() {
    startTestProcess("empty map");

    assertEquals(2, results.size());

    for (DmnDecisionResultEntries output : results) {
      assertTrue("The decision output should be empty", output.isEmpty());
    }
  }

  @Deployment(resources = { TEST_PROCESS, TEST_DECISION})
  @Test
  public void testSingleEntry() {
    startTestProcess("single entry");

    DmnDecisionResultEntries firstOutput = results.get(0);
    assertEquals("foo", firstOutput.getFirstEntry());
    assertEquals(Variables.stringValue("foo"), firstOutput.getFirstEntryTyped());
  }

  @Deployment(resources = { TEST_PROCESS, TEST_DECISION})
  @Test
  public void testMultipleEntries() {
    startTestProcess("multiple entries");

    DmnDecisionResultEntries firstOutput = results.get(0);
    assertEquals("foo", firstOutput.get("result1"));
    assertEquals("bar", firstOutput.get("result2"));

    assertEquals(Variables.stringValue("foo"), firstOutput.getEntryTyped("result1"));
    assertEquals(Variables.stringValue("bar"), firstOutput.getEntryTyped("result2"));
  }

  @Deployment(resources = { TEST_PROCESS, TEST_DECISION})
  @Test
  public void testSingleEntryList() {
    startTestProcess("single entry list");

    assertEquals(2, results.size());

    for (DmnDecisionResultEntries output : results) {
      assertEquals("foo", output.getFirstEntry());
      assertEquals(Variables.stringValue("foo"), output.getFirstEntryTyped());
    }
  }

  @Deployment(resources = { TEST_PROCESS, TEST_DECISION})
  @Test
  public void testMultipleEntriesList() {
    startTestProcess("multiple entries list");

    assertEquals(2, results.size());

    for (DmnDecisionResultEntries output : results) {
      assertEquals(2, output.size());
      assertEquals("foo", output.get("result1"));
      assertEquals("bar", output.get("result2"));

      assertEquals(Variables.stringValue("foo"), output.getEntryTyped("result1"));
      assertEquals(Variables.stringValue("bar"), output.getEntryTyped("result2"));
    }
  }

  @Deployment(resources = { TEST_PROCESS, TEST_DECISION_COLLECT_COUNT })
  @Test
  public void testCollectCountHitPolicyNoOutput() {
    startTestProcess("no output");

    assertEquals(1, results.size());
    DmnDecisionResultEntries firstOutput = results.get(0);

    assertEquals(0, (int) firstOutput.getFirstEntry());
    assertEquals(Variables.integerValue(0), firstOutput.getFirstEntryTyped());
  }

  @Deployment(resources = { TEST_PROCESS, TEST_DECISION_COLLECT_SUM })
  @Test
  public void testCollectSumHitPolicyNoOutput() {
    startTestProcess("no output");

    assertTrue("The decision result 'ruleResult' should be empty", results.isEmpty());
  }

  @Deployment(resources = { TEST_PROCESS, TEST_DECISION_COLLECT_SUM })
  @Test
  public void testCollectSumHitPolicySingleEntry() {
    startTestProcess("single entry");

    assertEquals(1, results.size());
    DmnDecisionResultEntries firstOutput = results.get(0);

    assertEquals(12, (int) firstOutput.getFirstEntry());
    assertEquals(Variables.integerValue(12), firstOutput.getFirstEntryTyped());
  }

  @Deployment(resources = { TEST_PROCESS, TEST_DECISION_COLLECT_SUM })
  @Test
  public void testCollectSumHitPolicySingleEntryList() {
    startTestProcess("single entry list");

    assertEquals(1, results.size());
    DmnDecisionResultEntries firstOutput = results.get(0);

    assertEquals(33, (int) firstOutput.getFirstEntry());
    assertEquals(Variables.integerValue(33), firstOutput.getFirstEntryTyped());
  }

  protected ProcessInstance startTestProcess(String input) {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess", Collections.<String, Object>singletonMap("input", input));

    // get the result from an execution listener that is invoked at the end of the business rule activity
    results = DecisionResultTestListener.getDecisionResult();
    assertNotNull(results);

    return processInstance;
  }

  @After
  public void tearDown() throws Exception {
    // reset the invoked execution listener
    DecisionResultTestListener.reset();
  }

}
