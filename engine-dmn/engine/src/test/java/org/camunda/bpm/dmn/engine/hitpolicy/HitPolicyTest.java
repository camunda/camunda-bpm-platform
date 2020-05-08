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
package org.camunda.bpm.dmn.engine.hitpolicy;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;

import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.camunda.bpm.dmn.engine.test.asserts.DmnEngineTestAssertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.dmn.engine.DmnDecisionRuleResult;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.impl.hitpolicy.DmnHitPolicyException;
import org.camunda.bpm.dmn.engine.impl.transform.DmnTransformException;
import org.camunda.bpm.dmn.engine.test.DecisionResource;
import org.camunda.bpm.dmn.engine.test.DmnEngineTest;
import org.camunda.bpm.dmn.engine.test.asserts.DmnDecisionTableResultAssert;
import org.junit.Test;

public class HitPolicyTest extends DmnEngineTest {

  protected static final Double DOUBLE_MIN = -Double.MAX_VALUE;

  public static final String DEFAULT_SINGLE = "org/camunda/bpm/dmn/engine/hitpolicy/HitPolicyTest.default.single.dmn";
  public static final String DEFAULT_COMPOUND = "org/camunda/bpm/dmn/engine/hitpolicy/HitPolicyTest.default.compound.dmn";
  public static final String UNIQUE_SINGLE = "org/camunda/bpm/dmn/engine/hitpolicy/HitPolicyTest.unique.single.dmn";
  public static final String UNIQUE_COMPOUND = "org/camunda/bpm/dmn/engine/hitpolicy/HitPolicyTest.unique.compound.dmn";
  public static final String ANY_SINGLE = "org/camunda/bpm/dmn/engine/hitpolicy/HitPolicyTest.any.single.dmn";
  public static final String ANY_COMPOUND = "org/camunda/bpm/dmn/engine/hitpolicy/HitPolicyTest.any.compound.dmn";
  public static final String PRIORITY_SINGLE = "org/camunda/bpm/dmn/engine/hitpolicy/HitPolicyTest.priority.single.dmn";
  public static final String FIRST_SINGLE = "org/camunda/bpm/dmn/engine/hitpolicy/HitPolicyTest.first.single.dmn";
  public static final String FIRST_COMPOUND = "org/camunda/bpm/dmn/engine/hitpolicy/HitPolicyTest.first.compound.dmn";
  public static final String OUTPUT_ORDER_SINGLE = "org/camunda/bpm/dmn/engine/hitpolicy/HitPolicyTest.outputOrder.single.dmn";
  public static final String RULE_ORDER_SINGLE = "org/camunda/bpm/dmn/engine/hitpolicy/HitPolicyTest.ruleOrder.single.dmn";
  public static final String RULE_ORDER_COMPOUND = "org/camunda/bpm/dmn/engine/hitpolicy/HitPolicyTest.ruleOrder.compound.dmn";
  public static final String COLLECT_SINGLE = "org/camunda/bpm/dmn/engine/hitpolicy/HitPolicyTest.collect.single.dmn";
  public static final String COLLECT_COMPOUND = "org/camunda/bpm/dmn/engine/hitpolicy/HitPolicyTest.collect.compound.dmn";
  public static final String COLLECT_SUM_SINGLE = "org/camunda/bpm/dmn/engine/hitpolicy/HitPolicyTest.collect.sum.single.dmn";
  public static final String COLLECT_SUM_COMPOUND = "org/camunda/bpm/dmn/engine/hitpolicy/HitPolicyTest.collect.sum.compound.dmn";
  public static final String COLLECT_MIN_SINGLE = "org/camunda/bpm/dmn/engine/hitpolicy/HitPolicyTest.collect.min.single.dmn";
  public static final String COLLECT_MIN_COMPOUND = "org/camunda/bpm/dmn/engine/hitpolicy/HitPolicyTest.collect.min.compound.dmn";
  public static final String COLLECT_MAX_SINGLE = "org/camunda/bpm/dmn/engine/hitpolicy/HitPolicyTest.collect.max.single.dmn";
  public static final String COLLECT_MAX_COMPOUND = "org/camunda/bpm/dmn/engine/hitpolicy/HitPolicyTest.collect.max.compound.dmn";
  public static final String COLLECT_COUNT_SINGLE = "org/camunda/bpm/dmn/engine/hitpolicy/HitPolicyTest.collect.count.single.dmn";
  public static final String COLLECT_COUNT_COMPOUND = "org/camunda/bpm/dmn/engine/hitpolicy/HitPolicyTest.collect.count.compound.dmn";

  @Test
  @DecisionResource(resource = DEFAULT_SINGLE)
  public void testDefaultHitPolicySingleOutputNoMatchingRule() {
    assertThatDecisionTableResult(false, false, false, "a", "b", "c")
      .isEmpty();
  }

  @Test
  @DecisionResource(resource = DEFAULT_SINGLE)
  public void testDefaultHitPolicySingleOutputSingleMatchingRule() {
    assertThatDecisionTableResult(true, false, false, "a", "b", "c")
      .hasSingleResult()
      .hasSingleEntry("a");

    assertThatDecisionTableResult(false, true, false, "a", "b", "c")
      .hasSingleResult()
      .hasSingleEntry("b");

    assertThatDecisionTableResult(false, false, true, "a", "b", "c")
      .hasSingleResult()
      .hasSingleEntry("c");
  }

  @Test
  @DecisionResource(resource = DEFAULT_SINGLE)
  public void testDefaultHitPolicySingleOutputMultipleMatchingRules() {
    try {
      evaluateDecisionTable(true, true, false, "a", "b", "c");
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03001");
    }

    try {
      evaluateDecisionTable(true, false, true, "a", "b", "c");
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03001");
    }

    try {
      evaluateDecisionTable(false, true, true, "a", "b", "c");
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03001");
    }

    try {
      evaluateDecisionTable(true, true, true, "a", "b", "c");
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03001");
    }
  }

  @Test
  @DecisionResource(resource = DEFAULT_COMPOUND)
  public void testDefaultHitPolicyCompoundOutputNoMatchingRule() {
    assertThatDecisionTableResult(false, false, false, "a", "b", "c")
      .isEmpty();
  }

  @Test
  @DecisionResource(resource = DEFAULT_COMPOUND)
  public void testDefaultHitPolicyCompoundOutputSingleMatchingRule() {
    assertThatDecisionTableResult(true, false, false, "a", "b", "c")
      .hasSingleResult()
      .containsOnly(entry("out1", "a"), entry("out2", "a"), entry("out3", "a"));

    assertThatDecisionTableResult(false, true, false, "a", "b", "c")
      .hasSingleResult()
      .containsOnly(entry("out1", "b"), entry("out2", "b"), entry("out3", "b"));

    assertThatDecisionTableResult(false, false, true, "a", "b", "c")
      .hasSingleResult()
      .containsOnly(entry("out1", "c"), entry("out2", "c"), entry("out3", "c"));
  }

  @Test
  @DecisionResource(resource = DEFAULT_COMPOUND)
  public void testDefaultHitPolicyCompoundOutputMultipleMatchingRules() {
    try {
      evaluateDecisionTable(true, true, false, "a", "b", "c");
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03001");
    }

    try {
      evaluateDecisionTable(true, false, true, "a", "b", "c");
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03001");
    }

    try {
      evaluateDecisionTable(false, true, true, "a", "b", "c");
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03001");
    }

    try {
      evaluateDecisionTable(true, true, true, "a", "b", "c");
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03001");
    }
  }

  @Test
  @DecisionResource(resource = UNIQUE_SINGLE)
  public void testUniqueHitPolicySingleOutputNoMatchingRule() {
    assertThatDecisionTableResult(false, false, false, "a", "b", "c")
      .isEmpty();
  }

  @Test
  @DecisionResource(resource = UNIQUE_SINGLE)
  public void testUniqueHitPolicySingleOutputSingleMatchingRule() {
    assertThatDecisionTableResult(true, false, false, "a", "b", "c")
      .hasSingleResult()
      .hasSingleEntry("a");

    assertThatDecisionTableResult(false, true, false, "a", "b", "c")
      .hasSingleResult()
      .hasSingleEntry("b");

    assertThatDecisionTableResult(false, false, true, "a", "b", "c")
      .hasSingleResult()
      .hasSingleEntry("c");
  }

  @Test
  @DecisionResource(resource = UNIQUE_SINGLE)
  public void testUniqueHitPolicySingleOutputMultipleMatchingRules() {
    try {
      evaluateDecisionTable(true, true, false, "a", "b", "c");
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03001");
    }

    try {
      evaluateDecisionTable(true, false, true, "a", "b", "c");
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03001");
    }

    try {
      evaluateDecisionTable(false, true, true, "a", "b", "c");
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03001");
    }

    try {
      evaluateDecisionTable(true, true, true, "a", "b", "c");
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03001");
    }
  }

  @Test
  @DecisionResource(resource = UNIQUE_COMPOUND)
  public void testUniqueHitPolicyCompoundOutputNoMatchingRule() {
    assertThatDecisionTableResult(false, false, false, "a", "b", "c")
      .isEmpty();
  }

  @Test
  @DecisionResource(resource = UNIQUE_COMPOUND)
  public void testUniqueHitPolicyCompoundOutputSingleMatchingRule() {
    assertThatDecisionTableResult(true, false, false, "a", "b", "c")
      .hasSingleResult()
      .containsOnly(entry("out1", "a"), entry("out2", "a"), entry("out3", "a"));

    assertThatDecisionTableResult(false, true, false, "a", "b", "c")
      .hasSingleResult()
      .containsOnly(entry("out1", "b"), entry("out2", "b"), entry("out3", "b"));

    assertThatDecisionTableResult(false, false, true, "a", "b", "c")
      .hasSingleResult()
      .containsOnly(entry("out1", "c"), entry("out2", "c"), entry("out3", "c"));
  }

  @Test
  @DecisionResource(resource = UNIQUE_COMPOUND)
  public void testUniqueHitPolicyCompoundOutputMultipleMatchingRules() {
    try {
      evaluateDecisionTable(true, true, false, "a", "b", "c");
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03001");
    }

    try {
      evaluateDecisionTable(true, false, true, "a", "b", "c");
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03001");
    }

    try {
      evaluateDecisionTable(false, true, true, "a", "b", "c");
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03001");
    }

    try {
      evaluateDecisionTable(true, true, true, "a", "b", "c");
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03001");
    }
  }

  @Test
  @DecisionResource(resource = ANY_SINGLE)
  public void testAnyHitPolicySingleOutputNoMatchingRule() {
    assertThatDecisionTableResult(false, false, false, "a", "b", "c")
      .isEmpty();
  }

  @Test
  @DecisionResource(resource = ANY_SINGLE)
  public void testAnyHitPolicySingleOutputSingleMatchingRule() {
    assertThatDecisionTableResult(true, false, false, "a", "b", "c")
      .hasSingleResult()
      .hasSingleEntry("a");

    assertThatDecisionTableResult(false, true, false, "a", "b", "c")
      .hasSingleResult()
      .hasSingleEntry("b");

    assertThatDecisionTableResult(false, false, true, "a", "b", "c")
      .hasSingleResult()
      .hasSingleEntry("c");
  }

  @Test
  @DecisionResource(resource = ANY_SINGLE)
  public void testAnyHitPolicySingleOutputMultipleMatchingRules() {
    try {
      evaluateDecisionTable(true, true, false, "a", "b", "c");
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03002");
    }

    try {
      evaluateDecisionTable(true, false, true, "a", "b", "c");
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03002");
    }

    try {
      evaluateDecisionTable(false, true, true, "a", "b", "c");
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03002");
    }

    try {
      evaluateDecisionTable(true, true, true, "a", "b", "c");
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03002");
    }

    assertThatDecisionTableResult(true, true, false, "a", "a", "a")
      .hasSingleResult()
      .hasSingleEntry("a");

    assertThatDecisionTableResult(true, false, true, "a", "a", "a")
      .hasSingleResult()
      .hasSingleEntry("a");

    assertThatDecisionTableResult(false, true, true, "a", "a", "a")
      .hasSingleResult()
      .hasSingleEntry("a");

    assertThatDecisionTableResult(true, true, true, "a", "a", "a")
      .hasSingleResult()
      .hasSingleEntry("a");
  }

  @Test
  @DecisionResource(resource = ANY_COMPOUND)
  public void testAnyHitPolicyCompoundOutputNoMatchingRule() {
    assertThatDecisionTableResult(false, false, false, "a", "b", "c")
      .isEmpty();
  }

  @Test
  @DecisionResource(resource = ANY_COMPOUND)
  public void testAnyHitPolicyCompoundOutputSingleMatchingRule() {
    assertThatDecisionTableResult(true, false, false, "a", "b", "c")
      .hasSingleResult()
      .containsOnly(entry("out1", "a"), entry("out2", "a"), entry("out3", "a"));

    assertThatDecisionTableResult(false, true, false, "a", "b", "c")
      .hasSingleResult()
      .containsOnly(entry("out1", "b"), entry("out2", "b"), entry("out3", "b"));

    assertThatDecisionTableResult(false, false, true, "a", "b", "c")
      .hasSingleResult()
      .containsOnly(entry("out1", "c"), entry("out2", "c"), entry("out3", "c"));
  }

  @Test
  @DecisionResource(resource = ANY_COMPOUND)
  public void testAnyHitPolicyCompoundOutputMultipleMatchingRules() {
    try {
      evaluateDecisionTable(true, true, false, "a", "b", "c");
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    } catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03002");
    }

    try {
      evaluateDecisionTable(true, false, true, "a", "b", "c");
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    } catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03002");
    }

    try {
      evaluateDecisionTable(false, true, true, "a", "b", "c");
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    } catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03002");
    }

    try {
      evaluateDecisionTable(true, true, true, "a", "b", "c");
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    } catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03002");
    }

    assertThatDecisionTableResult(true, true, false, "a", "a", "a")
      .hasSingleResult()
      .containsOnly(entry("out1", "a"), entry("out2", "a"), entry("out3", "a"));

    assertThatDecisionTableResult(true, false, true, "a", "a", "a")
      .hasSingleResult()
      .containsOnly(entry("out1", "a"), entry("out2", "a"), entry("out3", "a"));

    assertThatDecisionTableResult(false, true, true, "a", "a", "a")
      .hasSingleResult()
      .containsOnly(entry("out1", "a"), entry("out2", "a"), entry("out3", "a"));

    assertThatDecisionTableResult(true, true, true, "a", "a", "a")
      .hasSingleResult()
      .containsOnly(entry("out1", "a"), entry("out2", "a"), entry("out3", "a"));
  }

  @Test
  public void testPriorityHitPolicySingleOutputNoMatchingRule() {
    try {
      parseDecisionsFromFile(PRIORITY_SINGLE);
      failBecauseExceptionWasNotThrown(DmnTransformException.class);
    }
    catch (DmnTransformException e) {
      assertThat(e).hasMessageStartingWith("DMN-02004");
    }
  }

  @Test
  @DecisionResource(resource = FIRST_SINGLE)
  public void testFirstHitPolicySingleOutputNoMatchingRule() {
    assertThatDecisionTableResult(false, false, false, "a", "b", "c")
      .isEmpty();
  }

  @Test
  @DecisionResource(resource = FIRST_SINGLE)
  public void testFirstHitPolicySingleOutputSingleMatchingRule() {
    assertThatDecisionTableResult(true, false, false, "a", "b", "c")
      .hasSingleResult()
      .hasSingleEntry("a");

    assertThatDecisionTableResult(false, true, false, "a", "b", "c")
      .hasSingleResult()
      .hasSingleEntry("b");

    assertThatDecisionTableResult(false, false, true, "a", "b", "c")
      .hasSingleResult()
      .hasSingleEntry("c");
  }

  @Test
  @DecisionResource(resource = FIRST_SINGLE)
  public void testFirstHitPolicySingleOutputMultipleMatchingRules() {
    assertThatDecisionTableResult(true, true, false, "a", "b", "c")
      .hasSingleResult()
      .hasSingleEntry("a");

    assertThatDecisionTableResult(true, true, false, "c", "b", "a")
      .hasSingleResult()
      .hasSingleEntry("c");

    assertThatDecisionTableResult(true, false, true, "a", "b", "c")
      .hasSingleResult()
      .hasSingleEntry("a");

    assertThatDecisionTableResult(true, false, true, "c", "b", "a")
      .hasSingleResult()
      .hasSingleEntry("c");

    assertThatDecisionTableResult(false, true, true, "a", "b", "c")
      .hasSingleResult()
      .hasSingleEntry("b");

    assertThatDecisionTableResult(false, true, true, "c", "b", "a")
      .hasSingleResult()
      .hasSingleEntry("b");

    assertThatDecisionTableResult(true, true, true, "a", "b", "c")
      .hasSingleResult()
      .hasSingleEntry("a");

    assertThatDecisionTableResult(true, true, true, "c", "b", "a")
      .hasSingleResult()
      .hasSingleEntry("c");
  }

  @Test
  @DecisionResource(resource = FIRST_COMPOUND)
  public void testFirstHitPolicyCompoundOutputNoMatchingRule() {
    assertThatDecisionTableResult(false, false, false, "a", "b", "c")
      .isEmpty();
  }

  @Test
  @DecisionResource(resource = FIRST_COMPOUND)
  public void testFirstHitPolicyCompoundOutputSingleMatchingRule() {
    assertThatDecisionTableResult(true, false, false, "a", "b", "c")
      .hasSingleResult()
      .containsOnly(entry("out1", "a"), entry("out2", "a"), entry("out3", "a"));

    assertThatDecisionTableResult(false, true, false, "a", "b", "c")
      .hasSingleResult()
      .containsOnly(entry("out1", "b"), entry("out2", "b"), entry("out3", "b"));

    assertThatDecisionTableResult(false, false, true, "a", "b", "c")
      .hasSingleResult()
      .containsOnly(entry("out1", "c"), entry("out2", "c"), entry("out3", "c"));
  }

  @Test
  @DecisionResource(resource = FIRST_COMPOUND)
  public void testFirstHitPolicyCompoundOutputMultipleMatchingRules() {
    assertThatDecisionTableResult(true, true, false, "a", "b", "c")
      .hasSingleResult()
      .containsOnly(entry("out1", "a"), entry("out2", "a"), entry("out3", "a"));

    assertThatDecisionTableResult(true, true, false, "c", "b", "a")
      .hasSingleResult()
      .containsOnly(entry("out1", "c"), entry("out2", "c"), entry("out3", "c"));

    assertThatDecisionTableResult(true, false, true, "a", "b", "c")
      .hasSingleResult()
      .containsOnly(entry("out1", "a"), entry("out2", "a"), entry("out3", "a"));

    assertThatDecisionTableResult(true, false, true, "c", "b", "a")
      .hasSingleResult()
      .containsOnly(entry("out1", "c"), entry("out2", "c"), entry("out3", "c"));

    assertThatDecisionTableResult(false, true, true, "a", "b", "c")
      .hasSingleResult()
      .containsOnly(entry("out1", "b"), entry("out2", "b"), entry("out3", "b"));

    assertThatDecisionTableResult(false, true, true, "c", "b", "a")
      .hasSingleResult()
      .containsOnly(entry("out1", "b"), entry("out2", "b"), entry("out3", "b"));

    assertThatDecisionTableResult(true, true, true, "a", "b", "c")
      .hasSingleResult()
      .containsOnly(entry("out1", "a"), entry("out2", "a"), entry("out3", "a"));

    assertThatDecisionTableResult(true, true, true, "c", "b", "a")
      .hasSingleResult()
      .containsOnly(entry("out1", "c"), entry("out2", "c"), entry("out3", "c"));
  }

  @Test
  public void testOutputOrderHitPolicyNotSupported() {
    try {
      parseDecisionsFromFile(OUTPUT_ORDER_SINGLE);
      failBecauseExceptionWasNotThrown(DmnTransformException.class);
    }
    catch (DmnTransformException e) {
      assertThat(e).hasMessageStartingWith("DMN-02004");
    }
  }

  @Test
  @DecisionResource(resource = RULE_ORDER_SINGLE)
  public void testRuleOrderHitPolicySingleOutputNoMatchingRule() {
    assertThatDecisionTableResult(false, false, false, "a", "b", "c")
      .isEmpty();
  }

  @Test
  @DecisionResource(resource = RULE_ORDER_SINGLE)
  public void testRuleOrderHitPolicySingleOutputSingleMatchingRule() {
    assertThatDecisionTableResult(true, false, false, "a", "b", "c")
      .hasSingleResult()
      .hasSingleEntry("a");

    assertThatDecisionTableResult(false, true, false, "a", "b", "c")
      .hasSingleResult()
      .hasSingleEntry("b");

    assertThatDecisionTableResult(false, false, true, "a", "b", "c")
      .hasSingleResult()
      .hasSingleEntry("c");
  }

  @Test
  @DecisionResource(resource = RULE_ORDER_SINGLE)
  public void testRuleOrderHitPolicySingleOutputMultipleMatchingRules() {
    DmnDecisionTableResult results = evaluateDecisionTable(true, true, false, "a", "b", "c");
    assertThat(results).hasSize(2);
    assertThat(collectSingleOutputEntries(results)).containsExactly("a", "b");

    results = evaluateDecisionTable(true, true, false, "c", "b", "a");
    assertThat(results).hasSize(2);
    assertThat(collectSingleOutputEntries(results)).containsExactly("c", "b");

    results = evaluateDecisionTable(true, false, true, "a", "b", "c");
    assertThat(results).hasSize(2);
    assertThat(collectSingleOutputEntries(results)).containsExactly("a", "c");

    results = evaluateDecisionTable(true, false, true, "c", "b", "a");
    assertThat(results).hasSize(2);
    assertThat(collectSingleOutputEntries(results)).containsExactly("c", "a");

    results = evaluateDecisionTable(false, true, true, "a", "b", "c");
    assertThat(results).hasSize(2);
    assertThat(collectSingleOutputEntries(results)).containsExactly("b", "c");

    results = evaluateDecisionTable(false, true, true, "c", "b", "a");
    assertThat(results).hasSize(2);
    assertThat(collectSingleOutputEntries(results)).containsExactly("b", "a");

    results = evaluateDecisionTable(true, true, true, "a", "b", "c");
    assertThat(results).hasSize(3);
    assertThat(collectSingleOutputEntries(results)).containsExactly("a", "b", "c");

    results = evaluateDecisionTable(true, true, true, "c", "b", "a");
    assertThat(results).hasSize(3);
    assertThat(collectSingleOutputEntries(results)).containsExactly("c", "b", "a");
  }

  @Test
  @DecisionResource(resource = RULE_ORDER_COMPOUND)
  public void testRuleOrderHitPolicyCompoundOutputNoMatchingRule() {
    assertThatDecisionTableResult(false, false, false, "a", "b", "c")
      .isEmpty();
  }

  @Test
  @DecisionResource(resource = RULE_ORDER_COMPOUND)
  public void testRuleOrderHitPolicyCompoundOutputSingleMatchingRule() {
    assertThatDecisionTableResult(true, false, false, "a", "b", "c")
      .hasSingleResult()
      .containsOnly(entry("out1", "a"), entry("out2", "a"), entry("out3", "a"));

    assertThatDecisionTableResult(false, true, false, "a", "b", "c")
      .hasSingleResult()
      .containsOnly(entry("out1", "b"), entry("out2", "b"), entry("out3", "b"));

    assertThatDecisionTableResult(false, false, true, "a", "b", "c")
      .hasSingleResult()
      .containsOnly(entry("out1", "c"), entry("out2", "c"), entry("out3", "c"));
  }

  @Test
  @DecisionResource(resource = RULE_ORDER_COMPOUND)
  public void testRuleOrderHitPolicyCompoundOutputMultipleMatchingRules() {
    DmnDecisionTableResult results = evaluateDecisionTable(true, true, false, "a", "b", "c");
    assertThat(results).hasSize(2);
    assertThat(results.get(0)).containsOnly(entry("out1", "a"), entry("out2", "a"), entry("out3", "a"));
    assertThat(results.get(1)).containsOnly(entry("out1", "b"), entry("out2", "b"), entry("out3", "b"));

    results = evaluateDecisionTable(true, true, false, "c", "b", "a");
    assertThat(results).hasSize(2);
    assertThat(results.get(0)).containsOnly(entry("out1", "c"), entry("out2", "c"), entry("out3", "c"));
    assertThat(results.get(1)).containsOnly(entry("out1", "b"), entry("out2", "b"), entry("out3", "b"));

    results = evaluateDecisionTable(true, false, true, "a", "b", "c");
    assertThat(results).hasSize(2);
    assertThat(results.get(0)).containsOnly(entry("out1", "a"), entry("out2", "a"), entry("out3", "a"));
    assertThat(results.get(1)).containsOnly(entry("out1", "c"), entry("out2", "c"), entry("out3", "c"));

    results = evaluateDecisionTable(true, false, true, "c", "b", "a");
    assertThat(results).hasSize(2);
    assertThat(results.get(0)).containsOnly(entry("out1", "c"), entry("out2", "c"), entry("out3", "c"));
    assertThat(results.get(1)).containsOnly(entry("out1", "a"), entry("out2", "a"), entry("out3", "a"));

    results = evaluateDecisionTable(false, true, true, "a", "b", "c");
    assertThat(results).hasSize(2);
    assertThat(results.get(0)).containsOnly(entry("out1", "b"), entry("out2", "b"), entry("out3", "b"));
    assertThat(results.get(1)).containsOnly(entry("out1", "c"), entry("out2", "c"), entry("out3", "c"));

    results = evaluateDecisionTable(false, true, true, "c", "b", "a");
    assertThat(results).hasSize(2);
    assertThat(results.get(0)).containsOnly(entry("out1", "b"), entry("out2", "b"), entry("out3", "b"));
    assertThat(results.get(1)).containsOnly(entry("out1", "a"), entry("out2", "a"), entry("out3", "a"));

    results = evaluateDecisionTable(true, true, true, "a", "b", "c");
    assertThat(results).hasSize(3);
    assertThat(results.get(0)).containsOnly(entry("out1", "a"), entry("out2", "a"), entry("out3", "a"));
    assertThat(results.get(1)).containsOnly(entry("out1", "b"), entry("out2", "b"), entry("out3", "b"));
    assertThat(results.get(2)).containsOnly(entry("out1", "c"), entry("out2", "c"), entry("out3", "c"));

    results = evaluateDecisionTable(true, true, true, "c", "b", "a");
    assertThat(results).hasSize(3);
    assertThat(results.get(0)).containsOnly(entry("out1", "c"), entry("out2", "c"), entry("out3", "c"));
    assertThat(results.get(1)).containsOnly(entry("out1", "b"), entry("out2", "b"), entry("out3", "b"));
    assertThat(results.get(2)).containsOnly(entry("out1", "a"), entry("out2", "a"), entry("out3", "a"));
  }

  @Test
  @DecisionResource(resource = COLLECT_SINGLE)
  public void testCollectHitPolicySingleOutputNoMatchingRule() {
    assertThatDecisionTableResult(false, false, false, "a", "b", "c")
      .isEmpty();
  }

  @Test
  @DecisionResource(resource = COLLECT_SINGLE)
  public void testCollectHitPolicySingleOutputSingleMatchingRule() {
    assertThatDecisionTableResult(true, false, false, "a", "b", "c")
      .hasSingleResult()
      .hasSingleEntry("a");

    assertThatDecisionTableResult(false, true, false, "a", "b", "c")
      .hasSingleResult()
      .hasSingleEntry("b");

    assertThatDecisionTableResult(false, false, true, "a", "b", "c")
      .hasSingleResult()
      .hasSingleEntry("c");
  }

  @Test
  @DecisionResource(resource = COLLECT_SINGLE)
  public void testCollectHitPolicySingleOutputMultipleMatchingRules() {
    DmnDecisionTableResult results = evaluateDecisionTable(true, true, false, "a", "b", "c");
    assertThat(results).hasSize(2);
    assertThat(collectSingleOutputEntries(results)).containsOnlyOnce("a", "b");

    results = evaluateDecisionTable(true, false, true, "a", "b", "c");
    assertThat(results).hasSize(2);
    assertThat(collectSingleOutputEntries(results)).containsOnlyOnce("a", "c");

    results = evaluateDecisionTable(false, true, true, "a", "b", "c");
    assertThat(results).hasSize(2);
    assertThat(collectSingleOutputEntries(results)).containsOnlyOnce("b", "c");

    results = evaluateDecisionTable(true, true, true, "a", "b", "c");
    assertThat(results).hasSize(3);
    assertThat(collectSingleOutputEntries(results)).containsOnlyOnce("a", "b", "c");
  }

  @Test
  @DecisionResource(resource = COLLECT_COMPOUND)
  public void testCollectHitPolicyCompoundOutputNoMatchingRule() {
    assertThatDecisionTableResult(false, false, false, "a", "b", "c")
      .isEmpty();
  }

  @Test
  @DecisionResource(resource = COLLECT_COMPOUND)
  public void testCollectHitPolicyCompoundOutputSingleMatchingRule() {
    DmnDecisionTableResult results = evaluateDecisionTable(true, false, false, "a", "b", "c");
    assertThat(results)
      .hasSingleResult()
      .containsOnly(entry("out1", "a"), entry("out2", "a"), entry("out3", "a"));

    results = evaluateDecisionTable(false, true, false, "a", "b", "c");
    assertThat(results)
      .hasSingleResult()
      .containsOnly(entry("out1", "b"), entry("out2", "b"), entry("out3", "b"));

    results = evaluateDecisionTable(false, false, true, "a", "b", "c");
    assertThat(results)
      .hasSingleResult()
      .containsOnly(entry("out1", "c"), entry("out2", "c"), entry("out3", "c"));
  }

  @Test
  @DecisionResource(resource = COLLECT_SUM_SINGLE)
  public void testCollectSumHitPolicySingleOutputNoMatchingRule() {
    assertThatDecisionTableResult(false, false, false, 10, 20L, 30.034)
      .isEmpty();
  }

  @Test
  @DecisionResource(resource = COLLECT_SUM_SINGLE)
  public void testCollectSumHitPolicySingleOutputSingleMatchingRule() {
    assertThatDecisionTableResult(true, false, false, 10, 20L, 30.034)
      .hasSingleResult()
      .hasSingleEntry(10);

    assertThatDecisionTableResult(false, true, false, 10, 20L, 30.034)
      .hasSingleResult()
      .hasSingleEntry(20L);

    assertThatDecisionTableResult(false, false, true, 10, 20L, 30.034)
      .hasSingleResult()
      .hasSingleEntry(30.034);

    assertThatDecisionTableResult(true, false, false, MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE)
      .hasSingleResult()
      .hasSingleEntry(MAX_VALUE);

    assertThatDecisionTableResult(true, false, false, MIN_VALUE, Long.MIN_VALUE, DOUBLE_MIN)
      .hasSingleResult()
      .hasSingleEntry(MIN_VALUE);

    assertThatDecisionTableResult(false, true, false, MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE)
      .hasSingleResult()
      .hasSingleEntry(Long.MAX_VALUE);

    assertThatDecisionTableResult(false, true, false, MIN_VALUE, Long.MIN_VALUE, DOUBLE_MIN)
      .hasSingleResult()
      .hasSingleEntry(Long.MIN_VALUE);

    assertThatDecisionTableResult(false, false, true, MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE)
      .hasSingleResult()
      .hasSingleEntry(Double.MAX_VALUE);

    assertThatDecisionTableResult(false, false, true, MIN_VALUE, Long.MIN_VALUE, DOUBLE_MIN)
      .hasSingleResult()
      .hasSingleEntry(DOUBLE_MIN);

    assertThatDecisionTableResult(true, false, false, (byte) 1, (short) 2, 3f)
      .hasSingleResult()
      .hasSingleEntry(1L);

    assertThatDecisionTableResult(false, true, false, (byte) 1, (short) 2, 3f)
      .hasSingleResult()
      .hasSingleEntry(2L);

    assertThatDecisionTableResult(false, false, true, (byte) 1, (short) 2, 3f)
      .hasSingleResult()
      .hasSingleEntry(3.0);

    try {
      evaluateDecisionTable(false, false, true, 10, 20L, "c");
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    } catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03004");
    }
  }

  @Test
  @DecisionResource(resource = COLLECT_SUM_SINGLE)
  public void testCollectSumHitPolicySingleOutputMultipleMatchingRules() {
    assertThatDecisionTableResult(true, true, false, 10, 20L, 30.034)
      .hasSingleResult()
      .hasSingleEntry(30L);

    assertThatDecisionTableResult(true, false, true, 10, 20L, 30.034)
      .hasSingleResult()
      .hasSingleEntry(40.034);

    assertThatDecisionTableResult(false, true, true, 10, 20L, 30.034)
      .hasSingleResult()
      .hasSingleEntry(50.034);

    assertThatDecisionTableResult(true, true, true, 10, 20L, 30.034)
      .hasSingleResult()
      .hasSingleEntry(60.034);

    assertThatDecisionTableResult(true, true, false, MAX_VALUE, Long.MAX_VALUE - MAX_VALUE, Double.MAX_VALUE)
      .hasSingleResult()
      .hasSingleEntry(Long.MAX_VALUE);

    assertThatDecisionTableResult(true, true, false, MIN_VALUE, Long.MIN_VALUE - MIN_VALUE, DOUBLE_MIN)
      .hasSingleResult()
      .hasSingleEntry(Long.MIN_VALUE);

    assertThatDecisionTableResult(true, false, true, MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE - MAX_VALUE)
      .hasSingleResult()
      .hasSingleEntry(Double.MAX_VALUE);

    assertThatDecisionTableResult(true, false, true, MIN_VALUE, Long.MIN_VALUE, DOUBLE_MIN - MIN_VALUE)
      .hasSingleResult()
      .hasSingleEntry(DOUBLE_MIN);

    assertThatDecisionTableResult(false, true, true, MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE - Long.MAX_VALUE)
      .hasSingleResult()
      .hasSingleEntry(Double.MAX_VALUE);

    assertThatDecisionTableResult(false, true, true, MIN_VALUE, Long.MIN_VALUE, DOUBLE_MIN - Long.MIN_VALUE)
      .hasSingleResult()
      .hasSingleEntry(DOUBLE_MIN);

    assertThatDecisionTableResult(true, true, true, MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE - MAX_VALUE - Long.MAX_VALUE)
      .hasSingleResult()
      .hasSingleEntry(Double.MAX_VALUE);

    assertThatDecisionTableResult(true, true, true, MIN_VALUE, Long.MIN_VALUE, DOUBLE_MIN - MIN_VALUE - Long.MIN_VALUE)
      .hasSingleResult()
      .hasSingleEntry(DOUBLE_MIN);

    assertThatDecisionTableResult(true, true, false, (byte) 1, (short) 2, 3f)
      .hasSingleResult()
      .hasSingleEntry(3L);

    assertThatDecisionTableResult(true, false, true, (byte) 1, (short) 2, 3f)
      .hasSingleResult()
      .hasSingleEntry(4.0);

    assertThatDecisionTableResult(false, true, true, (byte) 1, (short) 2, 3f)
      .hasSingleResult()
      .hasSingleEntry(5.0);

    assertThatDecisionTableResult(true, true, true, (byte) 1, (short) 2, 3f)
      .hasSingleResult()
      .hasSingleEntry(6.0);

    try {
      evaluateDecisionTable(true, true, true, 10, 20L, true);
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    } catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03004");
    }
  }

  @Test
  @DecisionResource(resource = COLLECT_SUM_COMPOUND)
  public void testCollectSumHitPolicyCompoundOutputNoMatchingRule() {
    assertThatDecisionTableResult(false, false, false, "a", "b", "c")
      .isEmpty();
  }

  @Test
  @DecisionResource(resource = COLLECT_SUM_COMPOUND)
  public void testCollectSumHitPolicyCompoundOutputSingleMatchingRule() {
    try {
      evaluateDecisionTable(true, false, false, 1, 2L, 3d);
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03003");
    }

    try {
      evaluateDecisionTable(false, true, false, 1, 2L, 3d);
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03003");
    }

    try {
      evaluateDecisionTable(false, false, true, 1, 2L, 3d);
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03003");
    }
  }

  @Test
  @DecisionResource(resource = COLLECT_SUM_COMPOUND)
  public void testCollectSumHitPolicyCompoundOutputMultipleMatchingRules() {
    try {
      evaluateDecisionTable(true, true, false, 1, 2L, 3d);
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03003");
    }

    try {
      evaluateDecisionTable(true, false, true, 1, 2L, 3d);
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03003");
    }

    try {
      evaluateDecisionTable(false, true, true, 1, 2L, 3d);
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03003");
    }

    try {
      evaluateDecisionTable(true, true, true, 1, 2L, 3d);
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03003");
    }
  }

  @Test
  @DecisionResource(resource = COLLECT_MIN_SINGLE)
  public void testCollectMinHitPolicySingleOutputNoMatchingRule() {
    assertThatDecisionTableResult(false, false, false, 10, 20L, 30.034)
      .isEmpty();
  }

  @Test
  @DecisionResource(resource = COLLECT_MIN_SINGLE)
  public void testCollectMinHitPolicySingleOutputSingleMatchingRule() {
    assertThatDecisionTableResult(true, false, false, 10, 20L, 30.034)
      .hasSingleResult()
      .hasSingleEntry(10);

    assertThatDecisionTableResult(false, true, false, 10, 20L, 30.034)
      .hasSingleResult()
      .hasSingleEntry(20L);

    assertThatDecisionTableResult(false, false, true, 10, 20L, 30.034)
      .hasSingleResult()
      .hasSingleEntry(30.034);

    assertThatDecisionTableResult(true, false, false, MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE)
      .hasSingleResult()
      .hasSingleEntry(MAX_VALUE);

    assertThatDecisionTableResult(true, false, false, MIN_VALUE, Long.MIN_VALUE, DOUBLE_MIN)
      .hasSingleResult()
      .hasSingleEntry(MIN_VALUE);

    assertThatDecisionTableResult(false, true, false, MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE)
      .hasSingleResult()
      .hasSingleEntry(Long.MAX_VALUE);

    assertThatDecisionTableResult(false, true, false, MIN_VALUE, Long.MIN_VALUE, DOUBLE_MIN)
      .hasSingleResult()
      .hasSingleEntry(Long.MIN_VALUE);

    assertThatDecisionTableResult(false, false, true, MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE)
      .hasSingleResult()
      .hasSingleEntry(Double.MAX_VALUE);

    assertThatDecisionTableResult(false, false, true, MIN_VALUE, Long.MIN_VALUE, DOUBLE_MIN)
      .hasSingleResult()
      .hasSingleEntry(DOUBLE_MIN);

    assertThatDecisionTableResult(true, false, false, (byte) 1, (short) 2, 3f)
      .hasSingleResult()
      .hasSingleEntry(1L);

    assertThatDecisionTableResult(false, true, false, (byte) 1, (short) 2, 3f)
      .hasSingleResult()
      .hasSingleEntry(2L);

    assertThatDecisionTableResult(false, false, true, (byte) 1, (short) 2, 3f)
      .hasSingleResult()
      .hasSingleEntry(3.0);

    try {
      evaluateDecisionTable(false, false, true, 10, 20L, "c");
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    } catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03004");
    }
  }

  @Test
  @DecisionResource(resource = COLLECT_MIN_SINGLE)
  public void testCollectMinHitPolicySingleOutputMultipleMatchingRules() {
    assertThatDecisionTableResult(true, true, false, 10, 20L, 30.034)
      .hasSingleResult()
      .hasSingleEntry(10L);

    assertThatDecisionTableResult(true, false, true, 10, 20L, 30.034)
      .hasSingleResult()
      .hasSingleEntry(10.0);

    assertThatDecisionTableResult(false, true, true, 10, 20L, 30.034)
      .hasSingleResult()
      .hasSingleEntry(20.0);

    assertThatDecisionTableResult(true, true, true, 10, 20L, 30.034)
      .hasSingleResult()
      .hasSingleEntry(10.0);

    assertThatDecisionTableResult(true, true, false, MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE)
      .hasSingleResult()
      .hasSingleEntry((long) MAX_VALUE);

    assertThatDecisionTableResult(true, true, false, MIN_VALUE, Long.MIN_VALUE, DOUBLE_MIN)
      .hasSingleResult()
      .hasSingleEntry(Long.MIN_VALUE);

    assertThatDecisionTableResult(true, false, true, MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE)
      .hasSingleResult()
      .hasSingleEntry((double) MAX_VALUE);

    assertThatDecisionTableResult(true, false, true, MIN_VALUE, Long.MIN_VALUE, DOUBLE_MIN)
      .hasSingleResult()
      .hasSingleEntry(DOUBLE_MIN);

    assertThatDecisionTableResult(false, true, true, MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE)
      .hasSingleResult()
      .hasSingleEntry((double) Long.MAX_VALUE);

    assertThatDecisionTableResult(false, true, true, MIN_VALUE, Long.MIN_VALUE, DOUBLE_MIN)
      .hasSingleResult()
      .hasSingleEntry(DOUBLE_MIN);

    assertThatDecisionTableResult(true, true, true, MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE)
      .hasSingleResult()
      .hasSingleEntry((double) MAX_VALUE);

    assertThatDecisionTableResult(true, true, true, MIN_VALUE, Long.MIN_VALUE, DOUBLE_MIN)
      .hasSingleResult()
      .hasSingleEntry(DOUBLE_MIN);

    assertThatDecisionTableResult(true, true, false, (byte) 1, (short) 2, 3f)
      .hasSingleResult()
      .hasSingleEntry(1L);

    assertThatDecisionTableResult(true, false, true, (byte) 1, (short) 2, 3f)
      .hasSingleResult()
      .hasSingleEntry(1.0);

    assertThatDecisionTableResult(false, true, true, (byte) 1, (short) 2, 3f)
      .hasSingleResult()
      .hasSingleEntry(2.0);

    assertThatDecisionTableResult(true, true, true, (byte) 1, (short) 2, 3f)
      .hasSingleResult()
      .hasSingleEntry(1.0);

    try {
      evaluateDecisionTable(true, true, true, 10, 20L, true);
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    } catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03004");
    }
  }

  @Test
  @DecisionResource(resource = COLLECT_MIN_COMPOUND)
  public void testCollectMinHitPolicyCompoundOutputNoMatchingRule() {
    assertThatDecisionTableResult(false, false, false, "a", "b", "c")
      .isEmpty();
  }

  @Test
  @DecisionResource(resource = COLLECT_MIN_COMPOUND)
  public void testCollectMinHitPolicyCompoundOutputSingleMatchingRule() {
    try {
      evaluateDecisionTable(true, false, false, 1, 2L, 3d);
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03003");
    }

    try {
      evaluateDecisionTable(false, true, false, 1, 2L, 3d);
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03003");
    }

    try {
      evaluateDecisionTable(false, false, true, 1, 2L, 3d);
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03003");
    }
  }

  @Test
  @DecisionResource(resource = COLLECT_MIN_COMPOUND)
  public void testCollectMinHitPolicyCompoundOutputMultipleMatchingRules() {
    try {
      evaluateDecisionTable(true, true, false, 1, 2L, 3d);
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03003");
    }

    try {
      evaluateDecisionTable(true, false, true, 1, 2L, 3d);
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03003");
    }

    try {
      evaluateDecisionTable(false, true, true, 1, 2L, 3d);
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03003");
    }

    try {
      evaluateDecisionTable(true, true, true, 1, 2L, 3d);
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03003");
    }
  }

  @Test
  @DecisionResource(resource = COLLECT_MAX_SINGLE)
  public void testCollectMaxHitPolicySingleOutputNoMatchingRule() {
    assertThatDecisionTableResult(false, false, false, 10, 20L, 30.034)
      .isEmpty();
  }

  @Test
  @DecisionResource(resource = COLLECT_MAX_SINGLE)
  public void testCollectMaxHitPolicySingleOutputSingleMatchingRule() {
    assertThatDecisionTableResult(true, false, false, 10, 20L, 30.034)
      .hasSingleResult()
      .hasSingleEntry(10);

    assertThatDecisionTableResult(false, true, false, 10, 20L, 30.034)
      .hasSingleResult()
      .hasSingleEntry(20L);

    assertThatDecisionTableResult(false, false, true, 10, 20L, 30.034)
      .hasSingleResult()
      .hasSingleEntry(30.034);

    assertThatDecisionTableResult(true, false, false, MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE)
      .hasSingleResult()
      .hasSingleEntry(MAX_VALUE);

    assertThatDecisionTableResult(true, false, false, MIN_VALUE, Long.MIN_VALUE, DOUBLE_MIN)
      .hasSingleResult()
      .hasSingleEntry(MIN_VALUE);

    assertThatDecisionTableResult(false, true, false, MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE)
      .hasSingleResult()
      .hasSingleEntry(Long.MAX_VALUE);

    assertThatDecisionTableResult(false, true, false, MIN_VALUE, Long.MIN_VALUE, DOUBLE_MIN)
      .hasSingleResult()
      .hasSingleEntry(Long.MIN_VALUE);

    assertThatDecisionTableResult(false, false, true, MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE)
      .hasSingleResult()
      .hasSingleEntry(Double.MAX_VALUE);

    assertThatDecisionTableResult(false, false, true, MIN_VALUE, Long.MIN_VALUE, DOUBLE_MIN)
      .hasSingleResult()
      .hasSingleEntry(DOUBLE_MIN);

    assertThatDecisionTableResult(true, false, false, (byte) 1, (short) 2, 3f)
      .hasSingleResult()
      .hasSingleEntry(1L);

    assertThatDecisionTableResult(false, true, false, (byte) 1, (short) 2, 3f)
      .hasSingleResult()
      .hasSingleEntry(2L);

    assertThatDecisionTableResult(false, false, true, (byte) 1, (short) 2, 3f)
      .hasSingleResult()
      .hasSingleEntry(3.0);

    try {
      evaluateDecisionTable(false, false, true, 10, 20L, "c");
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    } catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03004");
    }
  }

  @Test
  @DecisionResource(resource = COLLECT_MAX_SINGLE)
  public void testCollectMaxHitPolicySingleOutputMultipleMatchingRules() {
    assertThatDecisionTableResult(true, true, false, 10, 20L, 30.034)
      .hasSingleResult()
      .hasSingleEntry(20L);

    assertThatDecisionTableResult(true, false, true, 10, 20L, 30.034)
      .hasSingleResult()
      .hasSingleEntry(30.034);

    assertThatDecisionTableResult(false, true, true, 10, 20L, 30.034)
      .hasSingleResult()
      .hasSingleEntry(30.034);

    assertThatDecisionTableResult(true, true, true, 10, 20L, 30.034)
      .hasSingleResult()
      .hasSingleEntry(30.034);

    assertThatDecisionTableResult(true, true, false, MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE)
      .hasSingleResult()
      .hasSingleEntry(Long.MAX_VALUE);

    assertThatDecisionTableResult(true, true, false, MIN_VALUE, Long.MIN_VALUE, DOUBLE_MIN)
      .hasSingleResult()
      .hasSingleEntry((long) MIN_VALUE);

    assertThatDecisionTableResult(true, false, true, MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE)
      .hasSingleResult()
      .hasSingleEntry(Double.MAX_VALUE);

    assertThatDecisionTableResult(true, false, true, MIN_VALUE, Long.MIN_VALUE, DOUBLE_MIN)
      .hasSingleResult()
      .hasSingleEntry((double) MIN_VALUE);

    assertThatDecisionTableResult(false, true, true, MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE)
      .hasSingleResult()
      .hasSingleEntry(Double.MAX_VALUE);

    assertThatDecisionTableResult(false, true, true, MIN_VALUE, Long.MIN_VALUE, DOUBLE_MIN)
      .hasSingleResult()
      .hasSingleEntry((double) Long.MIN_VALUE);

    assertThatDecisionTableResult(true, true, true, MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE)
      .hasSingleResult()
      .hasSingleEntry(Double.MAX_VALUE);

    assertThatDecisionTableResult(true, true, true, MIN_VALUE, Long.MIN_VALUE, DOUBLE_MIN)
      .hasSingleResult()
      .hasSingleEntry((double) MIN_VALUE);

    assertThatDecisionTableResult(true, true, false, (byte) 1, (short) 2, 3f)
      .hasSingleResult()
      .hasSingleEntry(2L);

    assertThatDecisionTableResult(true, false, true, (byte) 1, (short) 2, 3f)
      .hasSingleResult()
      .hasSingleEntry(3.0);

    assertThatDecisionTableResult(false, true, true, (byte) 1, (short) 2, 3f)
      .hasSingleResult()
      .hasSingleEntry(3.0);

    assertThatDecisionTableResult(true, true, true, (byte) 1, (short) 2, 3f)
      .hasSingleResult()
      .hasSingleEntry(3.0);

    try {
      evaluateDecisionTable(true, true, true, 10, 20L, true);
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    } catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03004");
    }
  }

  @Test
  @DecisionResource(resource = COLLECT_MAX_COMPOUND)
  public void testCollectMaxHitPolicyCompoundOutputNoMatchingRule() {
    assertThatDecisionTableResult(false, false, false, "a", "b", "c")
      .isEmpty();
  }

  @Test
  @DecisionResource(resource = COLLECT_MAX_COMPOUND)
  public void testCollectMaxHitPolicyCompoundOutputSingleMatchingRule() {
    try {
      evaluateDecisionTable(true, false, false, 1, 2L, 3d);
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03003");
    }

    try {
      evaluateDecisionTable(false, true, false, 1, 2L, 3d);
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03003");
    }

    try {
      evaluateDecisionTable(false, false, true, 1, 2L, 3d);
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03003");
    }
  }

  @Test
  @DecisionResource(resource = COLLECT_MAX_COMPOUND)
  public void testCollectMaxHitPolicyCompoundOutputMultipleMatchingRules() {
    try {
      evaluateDecisionTable(true, true, false, 1, 2L, 3d);
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03003");
    }

    try {
      evaluateDecisionTable(true, false, true, 1, 2L, 3d);
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03003");
    }

    try {
      evaluateDecisionTable(false, true, true, 1, 2L, 3d);
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03003");
    }

    try {
      evaluateDecisionTable(true, true, true, 1, 2L, 3d);
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03003");
    }
  }

  @Test
  @DecisionResource(resource = COLLECT_COUNT_SINGLE)
  public void testCollectCountHitPolicySingleOutputNoMatchingRule() {
    assertThatDecisionTableResult(false, false, false, 10, "b", 30.034)
      .hasSingleResult()
      .hasSingleEntry(0);
  }

  @Test
  @DecisionResource(resource = COLLECT_COUNT_SINGLE)
  public void testCollectCountHitPolicySingleOutputSingleMatchingRule() {
    assertThatDecisionTableResult(true, false, false, 10, "b", 30.034)
      .hasSingleResult()
      .hasSingleEntry(1);

    assertThatDecisionTableResult(false, true, false, 10, "b", 30.034)
      .hasSingleResult()
      .hasSingleEntry(1);

    assertThatDecisionTableResult(false, false, true, 10, "b", 30.034)
      .hasSingleResult()
      .hasSingleEntry(1);
  }

  @Test
  @DecisionResource(resource = COLLECT_COUNT_SINGLE)
  public void testCollectCountHitPolicySingleOutputMultipleMatchingRules() {
    assertThatDecisionTableResult(true, true, false, 10, "b", 30.034)
      .hasSingleResult()
      .hasSingleEntry(2);

    assertThatDecisionTableResult(true, false, true, 10, "b", 30.034)
      .hasSingleResult()
      .hasSingleEntry(2);

    assertThatDecisionTableResult(false, true, true, 10, "b", 30.034)
      .hasSingleResult()
      .hasSingleEntry(2);

    assertThatDecisionTableResult(true, true, true, 10, "b", 30.034)
      .hasSingleResult()
      .hasSingleEntry(3);
  }

  @Test
  @DecisionResource(resource = COLLECT_COUNT_COMPOUND)
  public void testCollectCountHitPolicyCompoundOutputNoMatchingRule() {
    assertThatDecisionTableResult(false, false, false, "a", "b", "c")
      .hasSingleResult()
      .hasSingleEntry(0);
  }

  @Test
  @DecisionResource(resource = COLLECT_COUNT_COMPOUND)
  public void testCollectCountHitPolicyCompoundOutputSingleMatchingRule() {
    try {
      evaluateDecisionTable(true, false, false, 1, 2L, 3d);
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03003");
    }

    try {
      evaluateDecisionTable(false, true, false, 1, 2L, 3d);
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03003");
    }

    try {
      evaluateDecisionTable(false, false, true, 1, 2L, 3d);
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03003");
    }
  }

  @Test
  @DecisionResource(resource = COLLECT_COUNT_COMPOUND)
  public void testCollectCountHitPolicyCompoundOutputMultipleMatchingRules() {
    try {
      evaluateDecisionTable(true, true, false, 1, 2L, 3d);
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03003");
    }

    try {
      evaluateDecisionTable(true, false, true, 1, 2L, 3d);
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03003");
    }

    try {
      evaluateDecisionTable(false, true, true, 1, 2L, 3d);
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03003");
    }

    try {
      evaluateDecisionTable(true, true, true, 1, 2L, 3d);
      failBecauseExceptionWasNotThrown(DmnHitPolicyException.class);
    }
    catch (DmnHitPolicyException e) {
      assertThat(e).hasMessageStartingWith("DMN-03003");
    }
  }

  // helper methods

  public List<Object> collectSingleOutputEntries(DmnDecisionTableResult results) {
    List<Object> values = new ArrayList<Object>();
    for (DmnDecisionRuleResult result : results) {
      values.add(result.getSingleEntry());
    }
    return values;
  }

  public DmnDecisionTableResult evaluateDecisionTable(Boolean input1, Boolean input2, Boolean input3, Object output1, Object output2, Object output3) {
    variables.put("input1", input1);
    variables.put("input2", input2);
    variables.put("input3", input3);
    variables.put("output1", output1);
    variables.put("output2", output2);
    variables.put("output3", output3);

    return evaluateDecisionTable();
  }

  public DmnDecisionTableResultAssert assertThatDecisionTableResult(Boolean input1, Boolean input2, Boolean input3, Object output1, Object output2, Object output3) {
    return assertThat(evaluateDecisionTable(input1, input2, input3, output1, output2, output3));
  }

}
