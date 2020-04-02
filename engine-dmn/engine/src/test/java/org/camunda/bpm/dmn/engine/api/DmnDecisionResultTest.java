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
package org.camunda.bpm.dmn.engine.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Fail;
import org.camunda.bpm.dmn.engine.DmnDecisionResultEntries;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionResultException;
import org.camunda.bpm.dmn.engine.test.DecisionResource;
import org.camunda.bpm.dmn.engine.test.DmnEngineTest;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.junit.Test;

public class DmnDecisionResultTest extends DmnEngineTest {

  public static final String NO_OUTPUT_VALUE = "noOutputValue";
  public static final String SINGLE_OUTPUT_VALUE = "singleOutputValue";
  public static final String MULTIPLE_OUTPUT_VALUES = "multipleOutputValues";

  public static final String RESULT_TEST_DMN = "DmnResultTest.dmn";
  public static final String RESULT_TEST_WITH_TYPES_DMN = "DmnResultTypedTest.dmn";
  public static final String RESULT_TEST_WITH_SINGLE_UNNAMED_OUTPUT_DMN = "DmnResultTest.testSingleOutputNoName.dmn";

  @Test
  @DecisionResource(resource = RESULT_TEST_DMN)
  public void testNoResult() {
    DmnDecisionResult results = evaluateWithMatchingRules();

    assertThat(results).isEmpty();
    assertThat(results.getFirstResult()).isNull();
    assertThat(results.getSingleResult()).isNull();

    assertThat((Object) results.getSingleEntry()).isNull();
    assertThat((Object) results.getSingleEntryTyped()).isNull();
  }

  @Test
  @DecisionResource(resource = RESULT_TEST_DMN)
  public void testSingleResult() {
    DmnDecisionResult results = evaluateWithMatchingRules(SINGLE_OUTPUT_VALUE);

    assertThat(results).hasSize(1);
    assertSingleOutputValue(results.get(0));
    assertSingleOutputValue(results.getFirstResult());
    assertSingleOutputValue(results.getSingleResult());

    assertThat((String) results.getSingleEntry()).isEqualTo("singleValue");
  }

  @Test
  @DecisionResource(resource = RESULT_TEST_DMN)
  public void testMultipleResults() {
    DmnDecisionResult decisionResult = evaluateWithMatchingRules(NO_OUTPUT_VALUE, SINGLE_OUTPUT_VALUE, MULTIPLE_OUTPUT_VALUES);
    assertThat(decisionResult).hasSize(3);

    DmnDecisionResultEntries ruleResult = decisionResult.get(0);
    assertNoOutputValue(ruleResult);
    ruleResult = decisionResult.get(1);
    assertSingleOutputValue(ruleResult);
    ruleResult = decisionResult.get(2);
    assertMultipleOutputValues(ruleResult);

    ruleResult = decisionResult.getFirstResult();
    assertNoOutputValue(ruleResult);

    try {
      decisionResult.getSingleResult();
      failBecauseExceptionWasNotThrown(DmnDecisionResultException.class);
    }
    catch (DmnDecisionResultException e){
      assertThat(e)
        .hasMessageStartingWith("DMN-01011")
        .hasMessageContaining("singleValue")
        .hasMessageContaining("multipleValues1")
        .hasMessageContaining("multipleValues2");
    }

    try {
      decisionResult.getSingleEntry();
      failBecauseExceptionWasNotThrown(DmnDecisionResultException.class);
    }
    catch (DmnDecisionResultException e){
      assertThat(e)
        .hasMessageStartingWith("DMN-01011")
        .hasMessageContaining("singleValue")
        .hasMessageContaining("multipleValues1")
        .hasMessageContaining("multipleValues2");
    }
  }

  @Test
  @DecisionResource(resource = RESULT_TEST_DMN)
  public void testNoOutputValue() {
    DmnDecisionResult decisionResult = evaluateWithMatchingRules(NO_OUTPUT_VALUE);
    assertThat(decisionResult).hasSize(1);

    assertNoOutputValue(decisionResult.getFirstResult());

    assertThat((Object) decisionResult.getSingleEntry()).isNull();
  }

  @Test
  @DecisionResource(resource = RESULT_TEST_DMN)
  public void testSingleOutputValue() {
    DmnDecisionResult decisionResult = evaluateWithMatchingRules(SINGLE_OUTPUT_VALUE);
    assertThat(decisionResult).hasSize(1);

    assertSingleOutputValue(decisionResult.getFirstResult());

    assertThat((String) decisionResult.getSingleEntry()).isEqualTo("singleValue");
  }

  @Test
  @DecisionResource(resource = RESULT_TEST_WITH_SINGLE_UNNAMED_OUTPUT_DMN)
  public void testSingleOutputNoName() {
    DmnDecisionResult decisionResult = dmnEngine.evaluateDecision(decision, variables);
    assertThat(decisionResult).hasSize(1);

    assertThat(decisionResult.getFirstResult()).hasSize(1);
    assertThat((String) decisionResult.getFirstResult().getSingleEntry()).isEqualTo("outputValue");
    assertThat(decisionResult.getFirstResult().get(null)).isEqualTo("outputValue");

    assertThat((String) decisionResult.getSingleEntry()).isEqualTo("outputValue");
  }

  @Test
  @DecisionResource(resource = RESULT_TEST_DMN)
  public void testMultipleOutputValues() {
    DmnDecisionResult decisionResult = evaluateWithMatchingRules(MULTIPLE_OUTPUT_VALUES);
    assertThat(decisionResult).hasSize(1);

    assertMultipleOutputValues(decisionResult.getFirstResult());

    try {
      decisionResult.getSingleEntry();
      failBecauseExceptionWasNotThrown(DmnDecisionResultException.class);
    }
    catch (DmnDecisionResultException e){
      assertThat(e)
        .hasMessageStartingWith("DMN-01010")
        .hasMessageContaining("multipleValues1")
        .hasMessageContaining("multipleValues2");
    }
  }

  @Test
  @DecisionResource(resource = RESULT_TEST_DMN)
  public void testCollectOutputValues() {
    DmnDecisionResult decisionResult = evaluateWithMatchingRules(NO_OUTPUT_VALUE, SINGLE_OUTPUT_VALUE, MULTIPLE_OUTPUT_VALUES);
    assertThat(decisionResult).hasSize(3);

    List<String> entryValues = decisionResult.collectEntries("firstOutput");
    assertThat(entryValues).containsExactly("singleValue", "multipleValues1");

    entryValues = decisionResult.collectEntries("secondOutput");
    assertThat(entryValues).containsExactly("multipleValues2");
  }

  @Test
  @DecisionResource(resource = RESULT_TEST_DMN)
  public void testOutputList() {
    DmnDecisionResult decisionResult = evaluateWithMatchingRules(SINGLE_OUTPUT_VALUE, MULTIPLE_OUTPUT_VALUES);

    List<Map<String, Object>> entryMapList = decisionResult.getResultList();
    assertThat(entryMapList).hasSize(2);

    Map<String, Object> firstResult = entryMapList.get(0);
    assertThat(firstResult).hasSize(1);
    assertThat(firstResult).containsEntry("firstOutput", "singleValue");

    Map<String, Object> secondResult = entryMapList.get(1);
    assertThat(secondResult).hasSize(2);
    assertThat(secondResult).containsEntry("firstOutput", "multipleValues1");
    assertThat(secondResult).containsEntry("secondOutput", "multipleValues2");
  }

  @Test
  @DecisionResource(resource = RESULT_TEST_DMN)
  public void testValueMap() {
    DmnDecisionResult decisionResult = evaluateWithMatchingRules(MULTIPLE_OUTPUT_VALUES);

    DmnDecisionResultEntries ruleResult = decisionResult.getSingleResult();
    assertThat(ruleResult).hasSize(2);

    Map<String, Object> entryMap = ruleResult.getEntryMap();
    assertThat(entryMap).hasSize(2);
    assertThat(entryMap).containsEntry("firstOutput", "multipleValues1");
    assertThat(entryMap).containsEntry("secondOutput", "multipleValues2");
  }

  @Test
  @DecisionResource(resource = RESULT_TEST_DMN)
  public void testSingleOutputUntypedValue() {
    DmnDecisionResult decisionResult = evaluateWithMatchingRules(SINGLE_OUTPUT_VALUE);
    assertThat(decisionResult).hasSize(1);

    DmnDecisionResultEntries ruleResult = decisionResult.getFirstResult();

    TypedValue typedEntry = ruleResult.getEntryTyped("firstOutput");
    assertThat(typedEntry).isEqualTo(Variables.untypedValue("singleValue"));

    typedEntry = ruleResult.getEntryTyped("secondOutput");
    assertThat(typedEntry).isNull();

    typedEntry = ruleResult.getFirstEntryTyped();
    assertThat(typedEntry).isEqualTo(Variables.untypedValue("singleValue"));

    typedEntry = ruleResult.getSingleEntryTyped();
    assertThat(typedEntry).isEqualTo(Variables.untypedValue("singleValue"));
  }

  @Test
  @DecisionResource(resource = RESULT_TEST_WITH_TYPES_DMN)
  public void testSingleOutputTypedValue() {
    DmnDecisionResult decisionResult = evaluateWithMatchingRules(SINGLE_OUTPUT_VALUE);
    assertThat(decisionResult).hasSize(1);

    DmnDecisionResultEntries ruleResult = decisionResult.getFirstResult();

    TypedValue typedValue = ruleResult.getEntryTyped("firstOutput");
    assertThat(typedValue).isEqualTo(Variables.stringValue("singleValue"));

    typedValue = ruleResult.getEntryTyped("secondOutput");
    assertThat(typedValue).isNull();

    typedValue = ruleResult.getFirstEntryTyped();
    assertThat(typedValue).isEqualTo(Variables.stringValue("singleValue"));

    typedValue = ruleResult.getSingleEntryTyped();
    assertThat(typedValue).isEqualTo(Variables.stringValue("singleValue"));
  }

  @Test
  @DecisionResource(resource = RESULT_TEST_DMN)
  public void testSingleEntryUntypedValue() {
    DmnDecisionResult decisionResult = evaluateWithMatchingRules(SINGLE_OUTPUT_VALUE);

    TypedValue typedValue = decisionResult.getSingleEntryTyped();
    assertThat(typedValue).isEqualTo(Variables.untypedValue("singleValue"));
  }

  @Test
  @DecisionResource(resource = RESULT_TEST_WITH_TYPES_DMN)
  public void testSingleEntryTypedValue() {
    DmnDecisionResult decisionResult = evaluateWithMatchingRules(SINGLE_OUTPUT_VALUE);

    TypedValue typedValue = decisionResult.getSingleEntryTyped();
    assertThat(typedValue).isEqualTo(Variables.stringValue("singleValue"));
  }

  // helper methods

  protected DmnDecisionResult evaluateWithMatchingRules(String... matchingRules) {
    List<String> matchingRulesList = Arrays.asList(matchingRules);
    variables.putValue(NO_OUTPUT_VALUE, matchingRulesList.contains(NO_OUTPUT_VALUE));
    variables.putValue(SINGLE_OUTPUT_VALUE, matchingRulesList.contains(SINGLE_OUTPUT_VALUE));
    variables.putValue(MULTIPLE_OUTPUT_VALUES, matchingRulesList.contains(MULTIPLE_OUTPUT_VALUES));

    return dmnEngine.evaluateDecision(decision, variables);
  }

  protected void assertSingleOutputValue(DmnDecisionResultEntries result) {
    assertThat(result.size()).isEqualTo(1);

    String value = (String) result.get("firstOutput");
    assertThat(value).isEqualTo("singleValue");

    value = (String) result.get("secondOutput");
    assertThat(value).isNull();

    value = result.getFirstEntry();
    assertThat(value).isEqualTo("singleValue");

    value = result.getSingleEntry();
    assertThat(value).isEqualTo("singleValue");
  }

  protected void assertNoOutputValue(DmnDecisionResultEntries result) {
    assertThat(result.size()).isEqualTo(0);

    String value = (String) result.get("firstOutput");
    assertThat(value).isNull();

    value = (String) result.get("secondOutput");
    assertThat(value).isNull();

    value = result.getFirstEntry();
    assertThat(value).isNull();

    value = result.getSingleEntry();
    assertThat(value).isNull();
  }

  protected void assertMultipleOutputValues(DmnDecisionResultEntries result) {
    assertThat(result.size()).isEqualTo(2);

    String value = (String) result.get("firstOutput");
    assertThat(value).isEqualTo("multipleValues1");

    value = (String) result.get("secondOutput");
    assertThat(value).isEqualTo("multipleValues2");

    value = result.getFirstEntry();
    assertThat(value).isEqualTo("multipleValues1");

    try {
      result.getSingleEntry();
      Fail.failBecauseExceptionWasNotThrown(DmnDecisionResultException.class);
    }
    catch (DmnDecisionResultException e) {
      assertThat(e)
        .hasMessageStartingWith("DMN-01010")
        .hasMessageContaining("multipleValues1")
        .hasMessageContaining("multipleValues2");
    }
  }

}
