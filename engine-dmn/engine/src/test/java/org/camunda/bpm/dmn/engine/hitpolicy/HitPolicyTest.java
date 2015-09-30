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

package org.camunda.bpm.dmn.engine.hitpolicy;

import static org.assertj.core.api.Assertions.fail;
import static org.camunda.bpm.dmn.engine.test.asserts.DmnAssertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.dmn.engine.DmnDecisionOutput;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.impl.hitpolicy.DmnHitPolicyException;
import org.camunda.bpm.dmn.engine.test.DecisionResource;
import org.camunda.bpm.dmn.engine.test.DmnDecisionTest;
import org.junit.Ignore;
import org.junit.Test;

public class HitPolicyTest extends DmnDecisionTest {

  protected static final Double DOUBLE_MIN = -Double.MAX_VALUE;

  public static final String DEFAULT_SINGLE = "HitPolicyTest.default.single.dmn";
  public static final String DEFAULT_COMPOUND = "HitPolicyTest.default.compound.dmn";
  public static final String UNIQUE_SINGLE = "HitPolicyTest.unique.single.dmn";
  public static final String UNIQUE_COMPOUND = "HitPolicyTest.unique.compound.dmn";
  public static final String ANY_SINGLE = "HitPolicyTest.any.single.dmn";
  public static final String ANY_COMPOUND = "HitPolicyTest.any.compound.dmn";
  public static final String PRIORITY_SINGLE = "HitPolicyTest.priority.single.dmn";
  public static final String PRIORITY_COMPOUND = "HitPolicyTest.priority.compound.dmn";
  public static final String FIRST_SINGLE = "HitPolicyTest.first.single.dmn";
  public static final String FIRST_COMPOUND = "HitPolicyTest.first.compound.dmn";
  public static final String OUTPUT_ORDER_SINGLE = "HitPolicyTest.outputOrder.single.dmn";
  public static final String OUTPUT_ORDER_COMPOUND = "HitPolicyTest.outputOrder.compound.dmn";
  public static final String RULE_ORDER_SINGLE = "HitPolicyTest.ruleOrder.single.dmn";
  public static final String RULE_ORDER_COMPOUND = "HitPolicyTest.ruleOrder.compound.dmn";
  public static final String COLLECT_SINGLE = "HitPolicyTest.collect.single.dmn";
  public static final String COLLECT_COMPOUND = "HitPolicyTest.collect.compound.dmn";
  public static final String COLLECT_SUM_SINGLE = "HitPolicyTest.collect.sum.single.dmn";
  public static final String COLLECT_SUM_COMPOUND = "HitPolicyTest.collect.sum.compound.dmn";
  public static final String COLLECT_MIN_SINGLE = "HitPolicyTest.collect.min.single.dmn";
  public static final String COLLECT_MIN_COMPOUND = "HitPolicyTest.collect.min.compound.dmn";
  public static final String COLLECT_MAX_SINGLE = "HitPolicyTest.collect.max.single.dmn";
  public static final String COLLECT_MAX_COMPOUND = "HitPolicyTest.collect.max.compound.dmn";
  public static final String COLLECT_COUNT_SINGLE = "HitPolicyTest.collect.count.single.dmn";
  public static final String COLLECT_COUNT_COMPOUND = "HitPolicyTest.collect.count.compound.dmn";

  @Test
  @DecisionResource(resource = DEFAULT_SINGLE)
  public void testDefaultHitPolicySingleOutputNoMatchingRule() {
    DmnDecisionResult result = startDecision(false, false, false, "a", "b", "c");
    assertThat(result).isEmpty();
  }

  @Test
  @DecisionResource(resource = DEFAULT_SINGLE)
  public void testDefaultHitPolicySingleOutputSingleMatchingRule() {
    DmnDecisionResult result = startDecision(true, false, false, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("a");

    result = startDecision(false, true, false, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("b");

    result = startDecision(false, false, true, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("c");
  }

  @Test
  @DecisionResource(resource = DEFAULT_SINGLE)
  public void testDefaultHitPolicySingleOutputMultipleMatchingRules() {
    try {
      startDecision(true, true, false, "a", "b", "c");
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03001");
    }

    try {
      startDecision(true, false, true, "a", "b", "c");
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03001");
    }

    try {
      startDecision(false, true, true, "a", "b", "c");
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03001");
    }

    try {
      startDecision(true, true, true, "a", "b", "c");
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03001");
    }
  }

  @Test
  @DecisionResource(resource = DEFAULT_COMPOUND)
  public void testDefaultHitPolicyCompoundOutputNoMatchingRule() {
    DmnDecisionResult result = startDecision(false, false, false, "a", "b", "c");
    assertThat(result).isEmpty();
  }

  @Test
  @DecisionResource(resource = DEFAULT_COMPOUND)
  public void testDefaultHitPolicyCompoundOutputSingleMatchingRule() {
    DmnDecisionResult result = startDecision(true, false, false, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "a").hasEntryWithValue("out2", "a").hasEntryWithValue("out3", "a");

    result = startDecision(false, true, false, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "b").hasEntryWithValue("out2", "b").hasEntryWithValue("out3", "b");

    result = startDecision(false, false, true, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "c").hasEntryWithValue("out2", "c").hasEntryWithValue("out3", "c");
  }

  @Test
  @DecisionResource(resource = DEFAULT_COMPOUND)
  public void testDefaultHitPolicyCompoundOutputMultipleMatchingRules() {
    try {
      startDecision(true, true, false, "a", "b", "c");
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03001");
    }

    try {
      startDecision(true, false, true, "a", "b", "c");
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03001");
    }

    try {
      startDecision(false, true, true, "a", "b", "c");
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03001");
    }

    try {
      startDecision(true, true, true, "a", "b", "c");
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03001");
    }
  }

  @Test
  @DecisionResource(resource = UNIQUE_SINGLE)
  public void testUniqueHitPolicySingleOutputNoMatchingRule() {
    DmnDecisionResult result = startDecision(false, false, false, "a", "b", "c");
    assertThat(result).isEmpty();
  }

  @Test
  @DecisionResource(resource = UNIQUE_SINGLE)
  public void testUniqueHitPolicySingleOutputSingleMatchingRule() {
    DmnDecisionResult result = startDecision(true, false, false, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("a");

    result = startDecision(false, true, false, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("b");

    result = startDecision(false, false, true, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("c");
  }

  @Test
  @DecisionResource(resource = UNIQUE_SINGLE)
  public void testUniqueHitPolicySingleOutputMultipleMatchingRules() {
    try {
      startDecision(true, true, false, "a", "b", "c");
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03001");
    }

    try {
      startDecision(true, false, true, "a", "b", "c");
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03001");
    }

    try {
      startDecision(false, true, true, "a", "b", "c");
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03001");
    }

    try {
      startDecision(true, true, true, "a", "b", "c");
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03001");
    }
  }

  @Test
  @DecisionResource(resource = UNIQUE_COMPOUND)
  public void testUniqueHitPolicyCompoundOutputNoMatchingRule() {
    DmnDecisionResult result = startDecision(false, false, false, "a", "b", "c");
    assertThat(result).isEmpty();
  }

  @Test
  @DecisionResource(resource = UNIQUE_COMPOUND)
  public void testUniqueHitPolicyCompoundOutputSingleMatchingRule() {
    DmnDecisionResult result = startDecision(true, false, false, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "a").hasEntryWithValue("out2", "a").hasEntryWithValue("out3", "a");

    result = startDecision(false, true, false, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "b").hasEntryWithValue("out2", "b").hasEntryWithValue("out3", "b");

    result = startDecision(false, false, true, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "c").hasEntryWithValue("out2", "c").hasEntryWithValue("out3", "c");
  }

  @Test
  @DecisionResource(resource = UNIQUE_COMPOUND)
  public void testUniqueHitPolicyCompoundOutputMultipleMatchingRules() {
    try {
      startDecision(true, true, false, "a", "b", "c");
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03001");
    }

    try {
      startDecision(true, false, true, "a", "b", "c");
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03001");
    }

    try {
      startDecision(false, true, true, "a", "b", "c");
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03001");
    }

    try {
      startDecision(true, true, true, "a", "b", "c");
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03001");
    }
  }

  @Test
  @DecisionResource(resource = ANY_SINGLE)
  public void testAnyHitPolicySingleOutputNoMatchingRule() {
    DmnDecisionResult result = startDecision(false, false, false, "a", "b", "c");
    assertThat(result).isEmpty();
  }

  @Test
  @DecisionResource(resource = ANY_SINGLE)
  public void testAnyHitPolicySingleOutputSingleMatchingRule() {
    DmnDecisionResult result = startDecision(true, false, false, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("a");

    result = startDecision(false, true, false, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("b");

    result = startDecision(false, false, true, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("c");
  }

  @Test
  @DecisionResource(resource = ANY_SINGLE)
  public void testAnyHitPolicySingleOutputMultipleMatchingRules() {
    try {
      startDecision(true, true, false, "a", "b", "c");
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03002");
    }

    try {
      startDecision(true, false, true, "a", "b", "c");
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03002");
    }

    try {
      startDecision(false, true, true, "a", "b", "c");
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03002");
    }

    try {
      startDecision(true, true, true, "a", "b", "c");
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03002");
    }

    DmnDecisionResult result = startDecision(true, true, false, "a", "a", "a");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("a");

    result = startDecision(true, false, true, "a", "a", "a");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("a");

    result = startDecision(false, true, true, "a", "a", "a");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("a");

    result = startDecision(true, true, true, "a", "a", "a");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("a");
  }

  @Test
  @DecisionResource(resource = ANY_COMPOUND)
  public void testAnyHitPolicyCompoundOutputNoMatchingRule() {
    DmnDecisionResult result = startDecision(false, false, false, "a", "b", "c");
    assertThat(result).isEmpty();
  }

  @Test
  @DecisionResource(resource = ANY_COMPOUND)
  public void testAnyHitPolicyCompoundOutputSingleMatchingRule() {
    DmnDecisionResult result = startDecision(true, false, false, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "a").hasEntryWithValue("out2", "a").hasEntryWithValue("out3", "a");

    result = startDecision(false, true, false, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "b").hasEntryWithValue("out2", "b").hasEntryWithValue("out3", "b");

    result = startDecision(false, false, true, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "c").hasEntryWithValue("out2", "c").hasEntryWithValue("out3", "c");
  }

  @Test
  @DecisionResource(resource = ANY_COMPOUND)
  public void testAnyHitPolicyCompoundOutputMultipleMatchingRules() {
    try {
      startDecision(true, true, false, "a", "b", "c");
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03002");
    }

    try {
      startDecision(true, false, true, "a", "b", "c");
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03002");
    }

    try {
      startDecision(false, true, true, "a", "b", "c");
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03002");
    }

    try {
      startDecision(true, true, true, "a", "b", "c");
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03002");
    }

    DmnDecisionResult result = startDecision(true, true, false, "a", "a", "a");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "a").hasEntryWithValue("out2", "a").hasEntryWithValue("out3", "a");

    result = startDecision(true, false, true, "a", "a", "a");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "a").hasEntryWithValue("out2", "a").hasEntryWithValue("out3", "a");

    result = startDecision(false, true, true, "a", "a", "a");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "a").hasEntryWithValue("out2", "a").hasEntryWithValue("out3", "a");

    result = startDecision(true, true, true, "a", "a", "a");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "a").hasEntryWithValue("out2", "a").hasEntryWithValue("out3", "a");
  }

  @Test
  @DecisionResource(resource = PRIORITY_SINGLE)
  public void testPriorityHitPolicySingleOutputNoMatchingRule() {
    DmnDecisionResult result = startDecision(false, false, false, "a", "b", "c");
    assertThat(result).isEmpty();
  }

  @Test
  @DecisionResource(resource = PRIORITY_SINGLE)
  public void testPriorityHitPolicySingleOutputSingleMatchingRule() {
    DmnDecisionResult result = startDecision(true, false, false, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("a");

    result = startDecision(false, true, false, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("b");

    result = startDecision(false, false, true, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("c");
  }

  @Test
  @Ignore
  @DecisionResource(resource = PRIORITY_SINGLE)
  public void testPriorityHitPolicySingleOutputMultipleMatchingRules() {
    DmnDecisionResult result = startDecision(true, true, false, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("a");

    result = startDecision(true, true, false, "c", "b", "a");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("b");

    result = startDecision(true, false, true, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("a");

    result = startDecision(true, false, true, "c", "b", "a");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("a");

    result = startDecision(false, true, true, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("b");

    result = startDecision(false, true, true, "c", "b", "a");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("b");

    result = startDecision(true, true, true, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("a");

    result = startDecision(true, true, true, "c", "b", "a");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("a");
  }

  @Test
  @DecisionResource(resource = PRIORITY_COMPOUND)
  public void testPriorityHitPolicyCompoundOutputNoMatchingRule() {
    DmnDecisionResult result = startDecision(false, false, false, "a", "b", "c");
    assertThat(result).isEmpty();
  }

  @Test
  @DecisionResource(resource = PRIORITY_COMPOUND)
  public void testPriorityHitPolicyCompoundOutputSingleMatchingRule() {
    DmnDecisionResult result = startDecision(true, false, false, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "a").hasEntryWithValue("out2", "a").hasEntryWithValue("out3", "a");

    result = startDecision(false, true, false, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "b").hasEntryWithValue("out2", "b").hasEntryWithValue("out3", "b");

    result = startDecision(false, false, true, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "c").hasEntryWithValue("out2", "c").hasEntryWithValue("out3", "c");
  }

  @Test
  @Ignore
  @DecisionResource(resource = PRIORITY_COMPOUND)
  public void testPriorityHitPolicyCompoundOutputMultipleMatchingRules() {
    DmnDecisionResult result = startDecision(true, true, false, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "a").hasEntryWithValue("out2", "a").hasEntryWithValue("out3", "a");

    result = startDecision(true, true, false, "c", "b", "a");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "b").hasEntryWithValue("out2", "b").hasEntryWithValue("out3", "b");

    result = startDecision(true, false, true, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "a").hasEntryWithValue("out2", "a").hasEntryWithValue("out3", "a");

    result = startDecision(true, false, true, "c", "b", "a");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "a").hasEntryWithValue("out2", "a").hasEntryWithValue("out3", "a");

    result = startDecision(false, true, true, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "b").hasEntryWithValue("out2", "b").hasEntryWithValue("out3", "b");

    result = startDecision(false, true, true, "c", "b", "a");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "b").hasEntryWithValue("out2", "b").hasEntryWithValue("out3", "b");

    result = startDecision(true, true, true, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "a").hasEntryWithValue("out2", "a").hasEntryWithValue("out3", "a");

    result = startDecision(true, true, true, "c", "b", "a");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "a").hasEntryWithValue("out2", "a").hasEntryWithValue("out3", "a");
  }

  @Test
  @DecisionResource(resource = FIRST_SINGLE)
  public void testFirstHitPolicySingleOutputNoMatchingRule() {
    DmnDecisionResult result = startDecision(false, false, false, "a", "b", "c");
    assertThat(result).isEmpty();
  }

  @Test
  @DecisionResource(resource = FIRST_SINGLE)
  public void testFirstHitPolicySingleOutputSingleMatchingRule() {
    DmnDecisionResult result = startDecision(true, false, false, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("a");

    result = startDecision(false, true, false, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("b");

    result = startDecision(false, false, true, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("c");
  }

  @Test
  @DecisionResource(resource = FIRST_SINGLE)
  public void testFirstHitPolicySingleOutputMultipleMatchingRules() {
    DmnDecisionResult result = startDecision(true, true, false, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("a");

    result = startDecision(true, true, false, "c", "b", "a");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("c");

    result = startDecision(true, false, true, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("a");

    result = startDecision(true, false, true, "c", "b", "a");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("c");

    result = startDecision(false, true, true, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("b");

    result = startDecision(false, true, true, "c", "b", "a");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("b");

    result = startDecision(true, true, true, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("a");

    result = startDecision(true, true, true, "c", "b", "a");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("c");
  }

  @Test
  @DecisionResource(resource = FIRST_COMPOUND)
  public void testFirstHitPolicyCompoundOutputNoMatchingRule() {
    DmnDecisionResult result = startDecision(false, false, false, "a", "b", "c");
    assertThat(result).isEmpty();
  }

  @Test
  @DecisionResource(resource = FIRST_COMPOUND)
  public void testFirstHitPolicyCompoundOutputSingleMatchingRule() {
    DmnDecisionResult result = startDecision(true, false, false, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "a").hasEntryWithValue("out2", "a").hasEntryWithValue("out3", "a");

    result = startDecision(false, true, false, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "b").hasEntryWithValue("out2", "b").hasEntryWithValue("out3", "b");

    result = startDecision(false, false, true, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "c").hasEntryWithValue("out2", "c").hasEntryWithValue("out3", "c");
  }

  @Test
  @DecisionResource(resource = FIRST_COMPOUND)
  public void testFirstHitPolicyCompoundOutputMultipleMatchingRules() {
    DmnDecisionResult result = startDecision(true, true, false, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "a").hasEntryWithValue("out2", "a").hasEntryWithValue("out3", "a");

    result = startDecision(true, true, false, "c", "b", "a");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "c").hasEntryWithValue("out2", "c").hasEntryWithValue("out3", "c");

    result = startDecision(true, false, true, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "a").hasEntryWithValue("out2", "a").hasEntryWithValue("out3", "a");

    result = startDecision(true, false, true, "c", "b", "a");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "c").hasEntryWithValue("out2", "c").hasEntryWithValue("out3", "c");

    result = startDecision(false, true, true, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "b").hasEntryWithValue("out2", "b").hasEntryWithValue("out3", "b");

    result = startDecision(false, true, true, "c", "b", "a");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "b").hasEntryWithValue("out2", "b").hasEntryWithValue("out3", "b");

    result = startDecision(true, true, true, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "a").hasEntryWithValue("out2", "a").hasEntryWithValue("out3", "a");

    result = startDecision(true, true, true, "c", "b", "a");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "c").hasEntryWithValue("out2", "c").hasEntryWithValue("out3", "c");
  }

  @Test
  @DecisionResource(resource = OUTPUT_ORDER_SINGLE)
  public void testOutputOrderHitPolicySingleOutputNoMatchingRule() {
    DmnDecisionResult result = startDecision(false, false, false, "a", "b", "c");
    assertThat(result).isEmpty();
  }

  @Test
  @DecisionResource(resource = OUTPUT_ORDER_SINGLE)
  public void testOutputOrderHitPolicySingleOutputSingleMatchingRule() {
    DmnDecisionResult result = startDecision(true, false, false, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("a");

    result = startDecision(false, true, false, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("b");

    result = startDecision(false, false, true, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("c");
  }

  @Test
  @Ignore
  @DecisionResource(resource = OUTPUT_ORDER_SINGLE)
  public void testOutputOrderHitPolicySingleOutputMultipleMatchingRules() {
    DmnDecisionResult result = startDecision(true, true, false, "a", "b", "c");
    assertThat(result).hasSize(2);
    assertThat(collectSingleOutputValues(result)).containsExactly("a", "b");

    result = startDecision(true, true, false, "c", "b", "a");
    assertThat(result).hasSize(2);
    assertThat(collectSingleOutputValues(result)).containsExactly("b", "c");

    result = startDecision(true, false, true, "a", "b", "c");
    assertThat(result).hasSize(2);
    assertThat(collectSingleOutputValues(result)).containsExactly("a", "c");

    result = startDecision(true, false, true, "c", "b", "a");
    assertThat(result).hasSize(2);
    assertThat(collectSingleOutputValues(result)).containsExactly("a", "c");

    result = startDecision(false, true, true, "a", "b", "c");
    assertThat(result).hasSize(2);
    assertThat(collectSingleOutputValues(result)).containsExactly("b", "c");

    result = startDecision(false, true, true, "c", "b", "a");
    assertThat(result).hasSize(2);
    assertThat(collectSingleOutputValues(result)).containsExactly("a", "b");

    result = startDecision(true, true, true, "a", "b", "c");
    assertThat(result).hasSize(3);
    assertThat(collectSingleOutputValues(result)).containsExactly("a", "b", "c");

    result = startDecision(true, true, true, "c", "b", "a");
    assertThat(result).hasSize(3);
    assertThat(collectSingleOutputValues(result)).containsExactly("a", "b", "c");
  }

  @Test
  @DecisionResource(resource = OUTPUT_ORDER_COMPOUND)
  public void testOutputOrderHitPolicyCompoundOutputNoMatchingRule() {
    DmnDecisionResult result = startDecision(false, false, false, "a", "b", "c");
    assertThat(result).isEmpty();
  }

  @Test
  @DecisionResource(resource = OUTPUT_ORDER_COMPOUND)
  public void testOutputOrderHitPolicyCompoundOutputSingleMatchingRule() {
    DmnDecisionResult result = startDecision(true, false, false, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "a").hasEntryWithValue("out2", "a").hasEntryWithValue("out3", "a");

    result = startDecision(false, true, false, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "b").hasEntryWithValue("out2", "b").hasEntryWithValue("out3", "b");

    result = startDecision(false, false, true, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "c").hasEntryWithValue("out2", "c").hasEntryWithValue("out3", "c");
  }

  @Test
  @Ignore
  @DecisionResource(resource = OUTPUT_ORDER_COMPOUND)
  public void testOutputOrderHitPolicyCompoundOutputMultipleMatchingRules() {
    DmnDecisionResult result = startDecision(true, true, false, "a", "b", "c");
    assertThat(result).hasSize(2);
    assertThat(result.get(0)).hasEntryWithValue("out1", "a").hasEntryWithValue("out2", "a").hasEntryWithValue("out3", "a");
    assertThat(result.get(1)).hasEntryWithValue("out1", "b").hasEntryWithValue("out2", "b").hasEntryWithValue("out3", "b");

    result = startDecision(true, true, false, "c", "b", "a");
    assertThat(result).hasSize(2);
    assertThat(result.get(0)).hasEntryWithValue("out1", "b").hasEntryWithValue("out2", "b").hasEntryWithValue("out3", "b");
    assertThat(result.get(1)).hasEntryWithValue("out1", "c").hasEntryWithValue("out2", "c").hasEntryWithValue("out3", "c");

    result = startDecision(true, false, true, "a", "b", "c");
    assertThat(result).hasSize(2);
    assertThat(result.get(0)).hasEntryWithValue("out1", "a").hasEntryWithValue("out2", "a").hasEntryWithValue("out3", "a");
    assertThat(result.get(1)).hasEntryWithValue("out1", "c").hasEntryWithValue("out2", "c").hasEntryWithValue("out3", "c");

    result = startDecision(true, false, true, "c", "b", "a");
    assertThat(result).hasSize(2);
    assertThat(result.get(0)).hasEntryWithValue("out1", "a").hasEntryWithValue("out2", "a").hasEntryWithValue("out3", "a");
    assertThat(result.get(1)).hasEntryWithValue("out1", "c").hasEntryWithValue("out2", "c").hasEntryWithValue("out3", "c");

    result = startDecision(false, true, true, "a", "b", "c");
    assertThat(result).hasSize(2);
    assertThat(result.get(0)).hasEntryWithValue("out1", "b").hasEntryWithValue("out2", "b").hasEntryWithValue("out3", "b");
    assertThat(result.get(1)).hasEntryWithValue("out1", "c").hasEntryWithValue("out2", "c").hasEntryWithValue("out3", "c");

    result = startDecision(false, true, true, "c", "b", "a");
    assertThat(result).hasSize(2);
    assertThat(result.get(0)).hasEntryWithValue("out1", "a").hasEntryWithValue("out2", "a").hasEntryWithValue("out3", "a");
    assertThat(result.get(1)).hasEntryWithValue("out1", "b").hasEntryWithValue("out2", "b").hasEntryWithValue("out3", "b");

    result = startDecision(true, true, true, "a", "b", "c");
    assertThat(result).hasSize(3);
    assertThat(result.get(0)).hasEntryWithValue("out1", "a").hasEntryWithValue("out2", "a").hasEntryWithValue("out3", "a");
    assertThat(result.get(1)).hasEntryWithValue("out1", "b").hasEntryWithValue("out2", "b").hasEntryWithValue("out3", "b");
    assertThat(result.get(2)).hasEntryWithValue("out1", "c").hasEntryWithValue("out2", "c").hasEntryWithValue("out3", "c");

    result = startDecision(true, true, true, "c", "b", "a");
    assertThat(result).hasSize(3);
    assertThat(result.get(0)).hasEntryWithValue("out1", "a").hasEntryWithValue("out2", "a").hasEntryWithValue("out3", "a");
    assertThat(result.get(1)).hasEntryWithValue("out1", "b").hasEntryWithValue("out2", "b").hasEntryWithValue("out3", "b");
    assertThat(result.get(2)).hasEntryWithValue("out1", "c").hasEntryWithValue("out2", "c").hasEntryWithValue("out3", "c");
  }

  @Test
  @DecisionResource(resource = RULE_ORDER_SINGLE)
  public void testRuleOrderHitPolicySingleOutputNoMatchingRule() {
    DmnDecisionResult result = startDecision(false, false, false, "a", "b", "c");
    assertThat(result).isEmpty();
  }

  @Test
  @DecisionResource(resource = RULE_ORDER_SINGLE)
  public void testRuleOrderHitPolicySingleOutputSingleMatchingRule() {
    DmnDecisionResult result = startDecision(true, false, false, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("a");

    result = startDecision(false, true, false, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("b");

    result = startDecision(false, false, true, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("c");
  }

  @Test
  @DecisionResource(resource = RULE_ORDER_SINGLE)
  public void testRuleOrderHitPolicySingleOutputMultipleMatchingRules() {
    DmnDecisionResult result = startDecision(true, true, false, "a", "b", "c");
    assertThat(result).hasSize(2);
    assertThat(collectSingleOutputValues(result)).containsExactly("a", "b");

    result = startDecision(true, true, false, "c", "b", "a");
    assertThat(result).hasSize(2);
    assertThat(collectSingleOutputValues(result)).containsExactly("c", "b");

    result = startDecision(true, false, true, "a", "b", "c");
    assertThat(result).hasSize(2);
    assertThat(collectSingleOutputValues(result)).containsExactly("a", "c");

    result = startDecision(true, false, true, "c", "b", "a");
    assertThat(result).hasSize(2);
    assertThat(collectSingleOutputValues(result)).containsExactly("c", "a");

    result = startDecision(false, true, true, "a", "b", "c");
    assertThat(result).hasSize(2);
    assertThat(collectSingleOutputValues(result)).containsExactly("b", "c");

    result = startDecision(false, true, true, "c", "b", "a");
    assertThat(result).hasSize(2);
    assertThat(collectSingleOutputValues(result)).containsExactly("b", "a");

    result = startDecision(true, true, true, "a", "b", "c");
    assertThat(result).hasSize(3);
    assertThat(collectSingleOutputValues(result)).containsExactly("a", "b", "c");

    result = startDecision(true, true, true, "c", "b", "a");
    assertThat(result).hasSize(3);
    assertThat(collectSingleOutputValues(result)).containsExactly("c", "b", "a");
  }

  @Test
  @DecisionResource(resource = RULE_ORDER_COMPOUND)
  public void testRuleOrderHitPolicyCompoundOutputNoMatchingRule() {
    DmnDecisionResult result = startDecision(false, false, false, "a", "b", "c");
    assertThat(result).isEmpty();
  }

  @Test
  @DecisionResource(resource = RULE_ORDER_COMPOUND)
  public void testRuleOrderHitPolicyCompoundOutputSingleMatchingRule() {
    DmnDecisionResult result = startDecision(true, false, false, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "a").hasEntryWithValue("out2", "a").hasEntryWithValue("out3", "a");

    result = startDecision(false, true, false, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "b").hasEntryWithValue("out2", "b").hasEntryWithValue("out3", "b");

    result = startDecision(false, false, true, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "c").hasEntryWithValue("out2", "c").hasEntryWithValue("out3", "c");
  }

  @Test
  @DecisionResource(resource = RULE_ORDER_COMPOUND)
  public void testRuleOrderHitPolicyCompoundOutputMultipleMatchingRules() {
    DmnDecisionResult result = startDecision(true, true, false, "a", "b", "c");
    assertThat(result).hasSize(2);
    assertThat(result.get(0)).hasEntryWithValue("out1", "a").hasEntryWithValue("out2", "a").hasEntryWithValue("out3", "a");
    assertThat(result.get(1)).hasEntryWithValue("out1", "b").hasEntryWithValue("out2", "b").hasEntryWithValue("out3", "b");

    result = startDecision(true, true, false, "c", "b", "a");
    assertThat(result).hasSize(2);
    assertThat(result.get(0)).hasEntryWithValue("out1", "c").hasEntryWithValue("out2", "c").hasEntryWithValue("out3", "c");
    assertThat(result.get(1)).hasEntryWithValue("out1", "b").hasEntryWithValue("out2", "b").hasEntryWithValue("out3", "b");

    result = startDecision(true, false, true, "a", "b", "c");
    assertThat(result).hasSize(2);
    assertThat(result.get(0)).hasEntryWithValue("out1", "a").hasEntryWithValue("out2", "a").hasEntryWithValue("out3", "a");
    assertThat(result.get(1)).hasEntryWithValue("out1", "c").hasEntryWithValue("out2", "c").hasEntryWithValue("out3", "c");

    result = startDecision(true, false, true, "c", "b", "a");
    assertThat(result).hasSize(2);
    assertThat(result.get(0)).hasEntryWithValue("out1", "c").hasEntryWithValue("out2", "c").hasEntryWithValue("out3", "c");
    assertThat(result.get(1)).hasEntryWithValue("out1", "a").hasEntryWithValue("out2", "a").hasEntryWithValue("out3", "a");

    result = startDecision(false, true, true, "a", "b", "c");
    assertThat(result).hasSize(2);
    assertThat(result.get(0)).hasEntryWithValue("out1", "b").hasEntryWithValue("out2", "b").hasEntryWithValue("out3", "b");
    assertThat(result.get(1)).hasEntryWithValue("out1", "c").hasEntryWithValue("out2", "c").hasEntryWithValue("out3", "c");

    result = startDecision(false, true, true, "c", "b", "a");
    assertThat(result).hasSize(2);
    assertThat(result.get(0)).hasEntryWithValue("out1", "b").hasEntryWithValue("out2", "b").hasEntryWithValue("out3", "b");
    assertThat(result.get(1)).hasEntryWithValue("out1", "a").hasEntryWithValue("out2", "a").hasEntryWithValue("out3", "a");

    result = startDecision(true, true, true, "a", "b", "c");
    assertThat(result).hasSize(3);
    assertThat(result.get(0)).hasEntryWithValue("out1", "a").hasEntryWithValue("out2", "a").hasEntryWithValue("out3", "a");
    assertThat(result.get(1)).hasEntryWithValue("out1", "b").hasEntryWithValue("out2", "b").hasEntryWithValue("out3", "b");
    assertThat(result.get(2)).hasEntryWithValue("out1", "c").hasEntryWithValue("out2", "c").hasEntryWithValue("out3", "c");

    result = startDecision(true, true, true, "c", "b", "a");
    assertThat(result).hasSize(3);
    assertThat(result.get(0)).hasEntryWithValue("out1", "c").hasEntryWithValue("out2", "c").hasEntryWithValue("out3", "c");
    assertThat(result.get(1)).hasEntryWithValue("out1", "b").hasEntryWithValue("out2", "b").hasEntryWithValue("out3", "b");
    assertThat(result.get(2)).hasEntryWithValue("out1", "a").hasEntryWithValue("out2", "a").hasEntryWithValue("out3", "a");
  }

  @Test
  @DecisionResource(resource = COLLECT_SINGLE)
  public void testCollectHitPolicySingleOutputNoMatchingRule() {
    DmnDecisionResult result = startDecision(false, false, false, "a", "b", "c");
    assertThat(result).isEmpty();
  }

  @Test
  @DecisionResource(resource = COLLECT_SINGLE)
  public void testCollectHitPolicySingleOutputSingleMatchingRule() {
    DmnDecisionResult result = startDecision(true, false, false, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("a");

    result = startDecision(false, true, false, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("b");

    result = startDecision(false, false, true, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasSingleEntryValue("c");
  }

  @Test
  @DecisionResource(resource = COLLECT_SINGLE)
  public void testCollectHitPolicySingleOutputMultipleMatchingRules() {
    DmnDecisionResult result = startDecision(true, true, false, "a", "b", "c");
    assertThat(result).hasSize(2);
    assertThat(collectSingleOutputValues(result)).containsOnlyOnce("a", "b");

    result = startDecision(true, false, true, "a", "b", "c");
    assertThat(result).hasSize(2);
    assertThat(collectSingleOutputValues(result)).containsOnlyOnce("a", "c");

    result = startDecision(false, true, true, "a", "b", "c");
    assertThat(result).hasSize(2);
    assertThat(collectSingleOutputValues(result)).containsOnlyOnce("b", "c");

    result = startDecision(true, true, true, "a", "b", "c");
    assertThat(result).hasSize(3);
    assertThat(collectSingleOutputValues(result)).containsOnlyOnce("a", "b", "c");
  }

  @Test
  @DecisionResource(resource = COLLECT_COMPOUND)
  public void testCollectHitPolicyCompoundOutputNoMatchingRule() {
    DmnDecisionResult result = startDecision(false, false, false, "a", "b", "c");
    assertThat(result).isEmpty();
  }

  @Test
  @DecisionResource(resource = COLLECT_COMPOUND)
  public void testCollectHitPolicyCompoundOutputSingleMatchingRule() {
    DmnDecisionResult result = startDecision(true, false, false, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "a").hasEntryWithValue("out2", "a").hasEntryWithValue("out3", "a");

    result = startDecision(false, true, false, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "b").hasEntryWithValue("out2", "b").hasEntryWithValue("out3", "b");

    result = startDecision(false, false, true, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasEntryWithValue("out1", "c").hasEntryWithValue("out2", "c").hasEntryWithValue("out3", "c");
  }

  @Test
  @DecisionResource(resource = COLLECT_SUM_SINGLE)
  public void testCollectSumHitPolicySingleOutputNoMatchingRule() {
    DmnDecisionResult result = startDecision(false, false, false, 10, 20L, 30.034);
    assertThat(result).isEmpty();
  }

  @Test
  @DecisionResource(resource = COLLECT_SUM_SINGLE)
  public void testCollectSumHitPolicySingleOutputSingleMatchingRule() {
    DmnDecisionResult result = startDecision(true, false, false, 10, 20L, 30.034);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(10);

    result = startDecision(false, true, false, 10, 20L, 30.034);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(20);

    result = startDecision(false, false, true, 10, 20L, 30.034);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(30.034);

    result = startDecision(true, false, false, Integer.MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(Integer.MAX_VALUE);

    result = startDecision(true, false, false, Integer.MIN_VALUE, Long.MIN_VALUE, DOUBLE_MIN);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(Integer.MIN_VALUE);

    result = startDecision(false, true, false, Integer.MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(Long.MAX_VALUE);

    result = startDecision(false, true, false, Integer.MIN_VALUE, Long.MIN_VALUE, DOUBLE_MIN);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(Long.MIN_VALUE);

    result = startDecision(false, false, true, Integer.MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(Double.MAX_VALUE);

    result = startDecision(false, false, true, Integer.MIN_VALUE, Long.MIN_VALUE, DOUBLE_MIN);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(DOUBLE_MIN);

    result = startDecision(true, false, false, (byte) 1, (short) 2, 3f);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(1);

    result = startDecision(false, true, false, (byte) 1, (short) 2, 3f);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(2);

    result = startDecision(false, false, true, (byte) 1, (short) 2, 3f);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(3.0);

    try {
      startDecision(false, false, true, 10, 20L, "c");
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03006");
    }
  }

  @Test
  @DecisionResource(resource = COLLECT_SUM_SINGLE)
  public void testCollectSumHitPolicySingleOutputMultipleMatchingRules() {
    DmnDecisionResult result = startDecision(true, true, false, 10, 20L, 30.034);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(30);

    result = startDecision(true, false, true, 10, 20L, 30.034);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(40.034);

    result = startDecision(false, true, true, 10, 20L, 30.034);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(50.034);

    result = startDecision(true, true, true, 10, 20L, 30.034);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(60.034);

    result = startDecision(true, true, false, Integer.MAX_VALUE, Long.MAX_VALUE - Integer.MAX_VALUE, Double.MAX_VALUE);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(Long.MAX_VALUE);

    result = startDecision(true, true, false, Integer.MIN_VALUE, Long.MIN_VALUE - Integer.MIN_VALUE, DOUBLE_MIN);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(Long.MIN_VALUE);

    result = startDecision(true, false, true, Integer.MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE - Integer.MAX_VALUE);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(Double.MAX_VALUE);

    result = startDecision(true, false, true, Integer.MIN_VALUE, Long.MIN_VALUE, DOUBLE_MIN - Integer.MIN_VALUE);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(DOUBLE_MIN);

    result = startDecision(false, true, true, Integer.MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE - Long.MAX_VALUE);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(Double.MAX_VALUE);

    result = startDecision(false, true, true, Integer.MIN_VALUE, Long.MIN_VALUE, DOUBLE_MIN - Long.MIN_VALUE);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(DOUBLE_MIN);

    result = startDecision(true, true, true, Integer.MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE - Integer.MAX_VALUE - Long.MAX_VALUE);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(Double.MAX_VALUE);

    result = startDecision(true, true, true, Integer.MIN_VALUE, Long.MIN_VALUE, DOUBLE_MIN - Integer.MIN_VALUE - Long.MIN_VALUE);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(DOUBLE_MIN);

    result = startDecision(true, true, false, (byte) 1, (short) 2, 3f);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(3);

    result = startDecision(true, false, true, (byte) 1, (short) 2, 3f);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(4.0);

    result = startDecision(false, true, true, (byte) 1, (short) 2, 3f);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(5.0);

    result = startDecision(true, true, true, (byte) 1, (short) 2, 3f);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(6.0);

    try {
      startDecision(true, true, true, 10, 20L, true);
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03006");
    }
  }

  @Test
  @DecisionResource(resource = COLLECT_SUM_COMPOUND)
  public void testCollectSumHitPolicyCompoundOutputNoMatchingRule() {
    DmnDecisionResult result = startDecision(false, false, false, "a", "b", "c");
    assertThat(result).isEmpty();
  }

  @Test
  @DecisionResource(resource = COLLECT_SUM_COMPOUND)
  public void testCollectSumHitPolicyCompoundOutputSingleMatchingRule() {
    try {
      startDecision(true, false, false, 1, 2L, 3d);
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03004");
    }

    try {
      startDecision(false, true, false, 1, 2L, 3d);
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03004");
    }

    try {
      startDecision(false, false, true, 1, 2L, 3d);
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03004");
    }
  }

  @Test
  @DecisionResource(resource = COLLECT_SUM_COMPOUND)
  public void testCollectSumHitPolicyCompoundOutputMultipleMatchingRules() {
    try {
      startDecision(true, true, false, 1, 2L, 3d);
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03004");
    }

    try {
      startDecision(true, false, true, 1, 2L, 3d);
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03004");
    }

    try {
      startDecision(false, true, true, 1, 2L, 3d);
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03004");
    }

    try {
      startDecision(true, true, true, 1, 2L, 3d);
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03004");
    }
  }

  @Test
  @DecisionResource(resource = COLLECT_MIN_SINGLE)
  public void testCollectMinHitPolicySingleOutputNoMatchingRule() {
    DmnDecisionResult result = startDecision(false, false, false, 10, 20L, 30.034);
    assertThat(result).isEmpty();
  }

  @Test
  @DecisionResource(resource = COLLECT_MIN_SINGLE)
  public void testCollectMinHitPolicySingleOutputSingleMatchingRule() {
    DmnDecisionResult result = startDecision(true, false, false, 10, 20L, 30.034);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(10);

    result = startDecision(false, true, false, 10, 20L, 30.034);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(20);

    result = startDecision(false, false, true, 10, 20L, 30.034);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(30.034);

    result = startDecision(true, false, false, Integer.MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(Integer.MAX_VALUE);

    result = startDecision(true, false, false, Integer.MIN_VALUE, Long.MIN_VALUE, DOUBLE_MIN);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(Integer.MIN_VALUE);

    result = startDecision(false, true, false, Integer.MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(Long.MAX_VALUE);

    result = startDecision(false, true, false, Integer.MIN_VALUE, Long.MIN_VALUE, DOUBLE_MIN);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(Long.MIN_VALUE);

    result = startDecision(false, false, true, Integer.MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(Double.MAX_VALUE);

    result = startDecision(false, false, true, Integer.MIN_VALUE, Long.MIN_VALUE, DOUBLE_MIN);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(DOUBLE_MIN);

    result = startDecision(true, false, false, (byte) 1, (short) 2, 3f);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(1);

    result = startDecision(false, true, false, (byte) 1, (short) 2, 3f);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(2);

    result = startDecision(false, false, true, (byte) 1, (short) 2, 3f);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(3.0);

    try {
      startDecision(false, false, true, 10, 20L, "c");
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03006");
    }
  }

  @Test
  @DecisionResource(resource = COLLECT_MIN_SINGLE)
  public void testCollectMinHitPolicySingleOutputMultipleMatchingRules() {
    DmnDecisionResult result = startDecision(true, true, false, 10, 20L, 30.034);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(10);

    result = startDecision(true, false, true, 10, 20L, 30.034);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(10.0);

    result = startDecision(false, true, true, 10, 20L, 30.034);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(20.0);

    result = startDecision(true, true, true, 10, 20L, 30.034);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(10.0);

    result = startDecision(true, true, false, Integer.MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE);
    assertThat(result).hasSingleOutput().hasSingleEntryValue((long) Integer.MAX_VALUE);

    result = startDecision(true, true, false, Integer.MIN_VALUE, Long.MIN_VALUE, DOUBLE_MIN);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(Long.MIN_VALUE);

    result = startDecision(true, false, true, Integer.MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE);
    assertThat(result).hasSingleOutput().hasSingleEntryValue((double) Integer.MAX_VALUE);

    result = startDecision(true, false, true, Integer.MIN_VALUE, Long.MIN_VALUE, DOUBLE_MIN);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(DOUBLE_MIN);

    result = startDecision(false, true, true, Integer.MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE);
    assertThat(result).hasSingleOutput().hasSingleEntryValue((double) Long.MAX_VALUE);

    result = startDecision(false, true, true, Integer.MIN_VALUE, Long.MIN_VALUE, DOUBLE_MIN);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(DOUBLE_MIN);

    result = startDecision(true, true, true, Integer.MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE);
    assertThat(result).hasSingleOutput().hasSingleEntryValue((double) Integer.MAX_VALUE);

    result = startDecision(true, true, true, Integer.MIN_VALUE, Long.MIN_VALUE, DOUBLE_MIN);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(DOUBLE_MIN);

    result = startDecision(true, true, false, (byte) 1, (short) 2, 3f);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(1);

    result = startDecision(true, false, true, (byte) 1, (short) 2, 3f);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(1.0);

    result = startDecision(false, true, true, (byte) 1, (short) 2, 3f);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(2.0);

    result = startDecision(true, true, true, (byte) 1, (short) 2, 3f);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(1.0);

    try {
      startDecision(true, true, true, 10, 20L, true);
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03006");
    }
  }

  @Test
  @DecisionResource(resource = COLLECT_MIN_COMPOUND)
  public void testCollectMinHitPolicyCompoundOutputNoMatchingRule() {
    DmnDecisionResult result = startDecision(false, false, false, "a", "b", "c");
    assertThat(result).isEmpty();
  }

  @Test
  @DecisionResource(resource = COLLECT_MIN_COMPOUND)
  public void testCollectMinHitPolicyCompoundOutputSingleMatchingRule() {
    try {
      startDecision(true, false, false, 1, 2L, 3d);
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03004");
    }

    try {
      startDecision(false, true, false, 1, 2L, 3d);
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03004");
    }

    try {
      startDecision(false, false, true, 1, 2L, 3d);
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03004");
    }
  }

  @Test
  @DecisionResource(resource = COLLECT_MIN_COMPOUND)
  public void testCollectMinHitPolicyCompoundOutputMultipleMatchingRules() {
    try {
      startDecision(true, true, false, 1, 2L, 3d);
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03004");
    }

    try {
      startDecision(true, false, true, 1, 2L, 3d);
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03004");
    }

    try {
      startDecision(false, true, true, 1, 2L, 3d);
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03004");
    }

    try {
      startDecision(true, true, true, 1, 2L, 3d);
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03004");
    }
  }

  @Test
  @DecisionResource(resource = COLLECT_MAX_SINGLE)
  public void testCollectMaxHitPolicySingleOutputNoMatchingRule() {
    DmnDecisionResult result = startDecision(false, false, false, 10, 20L, 30.034);
    assertThat(result).isEmpty();
  }

  @Test
  @DecisionResource(resource = COLLECT_MAX_SINGLE)
  public void testCollectMaxHitPolicySingleOutputSingleMatchingRule() {
    DmnDecisionResult result = startDecision(true, false, false, 10, 20L, 30.034);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(10);

    result = startDecision(false, true, false, 10, 20L, 30.034);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(20);

    result = startDecision(false, false, true, 10, 20L, 30.034);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(30.034);

    result = startDecision(true, false, false, Integer.MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(Integer.MAX_VALUE);

    result = startDecision(true, false, false, Integer.MIN_VALUE, Long.MIN_VALUE, DOUBLE_MIN);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(Integer.MIN_VALUE);

    result = startDecision(false, true, false, Integer.MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(Long.MAX_VALUE);

    result = startDecision(false, true, false, Integer.MIN_VALUE, Long.MIN_VALUE, DOUBLE_MIN);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(Long.MIN_VALUE);

    result = startDecision(false, false, true, Integer.MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(Double.MAX_VALUE);

    result = startDecision(false, false, true, Integer.MIN_VALUE, Long.MIN_VALUE, DOUBLE_MIN);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(DOUBLE_MIN);

    result = startDecision(true, false, false, (byte) 1, (short) 2, 3f);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(1);

    result = startDecision(false, true, false, (byte) 1, (short) 2, 3f);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(2);

    result = startDecision(false, false, true, (byte) 1, (short) 2, 3f);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(3.0);

    try {
      startDecision(false, false, true, 10, 20L, "c");
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03006");
    }
  }

  @Test
  @DecisionResource(resource = COLLECT_MAX_SINGLE)
  public void testCollectMaxHitPolicySingleOutputMultipleMatchingRules() {
    DmnDecisionResult result = startDecision(true, true, false, 10, 20L, 30.034);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(20);

    result = startDecision(true, false, true, 10, 20L, 30.034);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(30.034);

    result = startDecision(false, true, true, 10, 20L, 30.034);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(30.034);

    result = startDecision(true, true, true, 10, 20L, 30.034);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(30.034);

    result = startDecision(true, true, false, Integer.MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(Long.MAX_VALUE);

    result = startDecision(true, true, false, Integer.MIN_VALUE, Long.MIN_VALUE, DOUBLE_MIN);
    assertThat(result).hasSingleOutput().hasSingleEntryValue((long) Integer.MIN_VALUE);

    result = startDecision(true, false, true, Integer.MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(Double.MAX_VALUE);

    result = startDecision(true, false, true, Integer.MIN_VALUE, Long.MIN_VALUE, DOUBLE_MIN);
    assertThat(result).hasSingleOutput().hasSingleEntryValue((double) Integer.MIN_VALUE);

    result = startDecision(false, true, true, Integer.MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(Double.MAX_VALUE);

    result = startDecision(false, true, true, Integer.MIN_VALUE, Long.MIN_VALUE, DOUBLE_MIN);
    assertThat(result).hasSingleOutput().hasSingleEntryValue((double) Long.MIN_VALUE);

    result = startDecision(true, true, true, Integer.MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(Double.MAX_VALUE);

    result = startDecision(true, true, true, Integer.MIN_VALUE, Long.MIN_VALUE, DOUBLE_MIN);
    assertThat(result).hasSingleOutput().hasSingleEntryValue((double) Integer.MIN_VALUE);

    result = startDecision(true, true, false, (byte) 1, (short) 2, 3f);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(2);

    result = startDecision(true, false, true, (byte) 1, (short) 2, 3f);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(3.0);

    result = startDecision(false, true, true, (byte) 1, (short) 2, 3f);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(3.0);

    result = startDecision(true, true, true, (byte) 1, (short) 2, 3f);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(3.0);

    try {
      startDecision(true, true, true, 10, 20L, true);
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03006");
    }
  }

  @Test
  @DecisionResource(resource = COLLECT_MAX_COMPOUND)
  public void testCollectMaxHitPolicyCompoundOutputNoMatchingRule() {
    DmnDecisionResult result = startDecision(false, false, false, "a", "b", "c");
    assertThat(result).isEmpty();
  }

  @Test
  @DecisionResource(resource = COLLECT_MAX_COMPOUND)
  public void testCollectMaxHitPolicyCompoundOutputSingleMatchingRule() {
    try {
      startDecision(true, false, false, 1, 2L, 3d);
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03004");
    }

    try {
      startDecision(false, true, false, 1, 2L, 3d);
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03004");
    }

    try {
      startDecision(false, false, true, 1, 2L, 3d);
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03004");
    }
  }

  @Test
  @DecisionResource(resource = COLLECT_MAX_COMPOUND)
  public void testCollectMaxHitPolicyCompoundOutputMultipleMatchingRules() {
    try {
      startDecision(true, true, false, 1, 2L, 3d);
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03004");
    }

    try {
      startDecision(true, false, true, 1, 2L, 3d);
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03004");
    }

    try {
      startDecision(false, true, true, 1, 2L, 3d);
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03004");
    }

    try {
      startDecision(true, true, true, 1, 2L, 3d);
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03004");
    }
  }

  @Test
  @DecisionResource(resource = COLLECT_COUNT_SINGLE)
  public void testCollectCountHitPolicySingleOutputNoMatchingRule() {
    DmnDecisionResult result = startDecision(false, false, false, 10, "b", 30.034);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(0L);
  }

  @Test
  @DecisionResource(resource = COLLECT_COUNT_SINGLE)
  public void testCollectCountHitPolicySingleOutputSingleMatchingRule() {
    DmnDecisionResult result = startDecision(true, false, false, 10, "b", 30.034);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(1L);

    result = startDecision(false, true, false, 10, "b", 30.034);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(1L);

    result = startDecision(false, false, true, 10, "b", 30.034);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(1L);
  }

  @Test
  @DecisionResource(resource = COLLECT_COUNT_SINGLE)
  public void testCollectCountHitPolicySingleOutputMultipleMatchingRules() {
    DmnDecisionResult result = startDecision(true, true, false, 10, "b", 30.034);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(2L);

    result = startDecision(true, false, true, 10, "b", 30.034);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(2L);

    result = startDecision(false, true, true, 10, "b", 30.034);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(2L);

    result = startDecision(true, true, true, 10, "b", 30.034);
    assertThat(result).hasSingleOutput().hasSingleEntryValue(3L);
  }

  @Test
  @DecisionResource(resource = COLLECT_COUNT_COMPOUND)
  public void testCollectCountHitPolicyCompoundOutputNoMatchingRule() {
    DmnDecisionResult result = startDecision(false, false, false, "a", "b", "c");
    assertThat(result).hasSingleOutput().hasSingleEntryValue(0L);
  }

  @Test
  @DecisionResource(resource = COLLECT_COUNT_COMPOUND)
  public void testCollectCountHitPolicyCompoundOutputSingleMatchingRule() {
    try {
      startDecision(true, false, false, 1, 2L, 3d);
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03004");
    }

    try {
      startDecision(false, true, false, 1, 2L, 3d);
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03004");
    }

    try {
      startDecision(false, false, true, 1, 2L, 3d);
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03004");
    }
  }

  @Test
  @DecisionResource(resource = COLLECT_COUNT_COMPOUND)
  public void testCollectCountHitPolicyCompoundOutputMultipleMatchingRules() {
    try {
      startDecision(true, true, false, 1, 2L, 3d);
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03004");
    }

    try {
      startDecision(true, false, true, 1, 2L, 3d);
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03004");
    }

    try {
      startDecision(false, true, true, 1, 2L, 3d);
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03004");
    }

    try {
      startDecision(true, true, true, 1, 2L, 3d);
      fail("Decision should not be evaluable");
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03004");
    }
  }


  // helper methods

  public List<Object> collectSingleOutputValues(DmnDecisionResult result) {
    List<Object> values = new ArrayList<Object>();
    for (DmnDecisionOutput dmnDecisionOutput : result) {
      values.add(dmnDecisionOutput.getValue());
    }
    return values;
  }

  protected DmnDecisionResult startDecision(Boolean input1, Boolean input2, Boolean input3, Object output1, Object output2, Object output3) {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("input1", input1);
    variables.put("input2", input2);
    variables.put("input3", input3);
    variables.put("output1", output1);
    variables.put("output2", output2);
    variables.put("output3", output3);

    return engine.evaluate(decision, variables);
  }

}
