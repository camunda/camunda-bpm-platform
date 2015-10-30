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

package org.camunda.bpm.dmn.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.dmn.engine.test.DecisionResource;
import org.camunda.bpm.dmn.engine.test.DmnDecisionTest;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.junit.Test;

public class DmnResultTest extends DmnDecisionTest {

  public static final String NO_OUTPUT_VALUE = "noOutputValue";
  public static final String SINGLE_OUTPUT_VALUE = "singleOutputValue";
  public static final String MULTIPLE_OUTPUT_VALUES = "multipleOutputValues";

  public static final String RESULT_TEST_DMN = "DmnResultTest.dmn";
  public static final String RESULT_TEST_WITH_TYPES_DMN = "DmnResultTypedTest.dmn";

  @Test
  @DecisionResource(resource = RESULT_TEST_DMN)
  public void testNoResult() {
    DmnDecisionResult decisionResult = evaluateWithMatchingRules();
    assertThat(decisionResult).isEmpty();

    DmnDecisionOutput output = decisionResult.getFirstOutput();
    assertThat(output).isNull();

    output = decisionResult.getSingleOutput();
    assertThat(output).isNull();
  }

  @Test
  @DecisionResource(resource = RESULT_TEST_DMN)
  public void testSingleResult() {
    DmnDecisionResult decisionResult = evaluateWithMatchingRules(SINGLE_OUTPUT_VALUE);
    assertThat(decisionResult).hasSize(1);

    DmnDecisionOutput output = decisionResult.get(0);
    assertSingleOutputValue(output);

    output = decisionResult.getFirstOutput();
    assertSingleOutputValue(output);

    output = decisionResult.getSingleOutput();
    assertSingleOutputValue(output);
  }

  @Test
  @DecisionResource(resource = RESULT_TEST_DMN)
  public void testMultipleResults() {
    DmnDecisionResult decisionResult = evaluateWithMatchingRules(NO_OUTPUT_VALUE, SINGLE_OUTPUT_VALUE, MULTIPLE_OUTPUT_VALUES);
    assertThat(decisionResult).hasSize(3);

    DmnDecisionOutput output = decisionResult.get(0);
    assertNoOutputValue(output);
    output = decisionResult.get(1);
    assertSingleOutputValue(output);
    output = decisionResult.get(2);
    assertMultipleOutputValues(output);

    output = decisionResult.getFirstOutput();
    assertNoOutputValue(output);

    try {
      decisionResult.getSingleOutput();
      fail("Exception expected as decision result has more than one output");
    }
    catch (DmnResultException e){
      assertThat(e)
        .hasMessageStartingWith("DMN-01021")
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

    DmnDecisionOutput decisionOutput = decisionResult.getFirstOutput();
    assertNoOutputValue(decisionOutput);
  }

  @Test
  @DecisionResource(resource = RESULT_TEST_DMN)
  public void testSingleOutputValue() {
    DmnDecisionResult decisionResult = evaluateWithMatchingRules(SINGLE_OUTPUT_VALUE);
    assertThat(decisionResult).hasSize(1);

    DmnDecisionOutput decisionOutput = decisionResult.getFirstOutput();
    assertSingleOutputValue(decisionOutput);
  }

  @Test
  @DecisionResource(resource = RESULT_TEST_DMN)
  public void testMultipleOutputValues() {
    DmnDecisionResult decisionResult = evaluateWithMatchingRules(MULTIPLE_OUTPUT_VALUES);
    assertThat(decisionResult).hasSize(1);

    DmnDecisionOutput decisionOutput = decisionResult.getFirstOutput();
    assertMultipleOutputValues(decisionOutput);
  }

  @Test
  @DecisionResource(resource = RESULT_TEST_DMN)
  public void testCollectOutputValues() {
    DmnDecisionResult decisionResult = evaluateWithMatchingRules(NO_OUTPUT_VALUE, SINGLE_OUTPUT_VALUE, MULTIPLE_OUTPUT_VALUES);
    assertThat(decisionResult).hasSize(3);

    List<String> outputValues = decisionResult.collectOutputValues("firstOutput");
    assertThat(outputValues).containsExactly(null, "singleValue", "multipleValues1");

    outputValues = decisionResult.collectOutputValues("secondOutput");
    assertThat(outputValues).containsExactly(null, null, "multipleValues2");
  }

  @Test
  @DecisionResource(resource = RESULT_TEST_DMN)
  public void testSingleOutputUntypedValue() {
    DmnDecisionResult decisionResult = evaluateWithMatchingRules(SINGLE_OUTPUT_VALUE);
    assertThat(decisionResult).hasSize(1);

    DmnDecisionOutput decisionOutput = decisionResult.getFirstOutput();

    TypedValue typedValue = decisionOutput.getValueTyped("firstOutput");
    assertThat(typedValue).isEqualTo(Variables.untypedValue("singleValue"));

    typedValue = decisionOutput.getValueTyped("secondOutput");
    assertThat(typedValue).isNull();

    typedValue = decisionOutput.getFirstValueTyped();
    assertThat(typedValue).isEqualTo(Variables.untypedValue("singleValue"));

    typedValue = decisionOutput.getSingleValueTyped();
    assertThat(typedValue).isEqualTo(Variables.untypedValue("singleValue"));
  }

  @Test
  @DecisionResource(resource = RESULT_TEST_WITH_TYPES_DMN)
  public void testSingleOutputTypedValue() {
    DmnDecisionResult decisionResult = evaluateWithMatchingRules(SINGLE_OUTPUT_VALUE);
    assertThat(decisionResult).hasSize(1);

    DmnDecisionOutput decisionOutput = decisionResult.getFirstOutput();

    TypedValue typedValue = decisionOutput.getValueTyped("firstOutput");
    assertThat(typedValue).isEqualTo(Variables.stringValue("singleValue"));

    typedValue = decisionOutput.getValueTyped("secondOutput");
    assertThat(typedValue).isNull();

    typedValue = decisionOutput.getFirstValueTyped();
    assertThat(typedValue).isEqualTo(Variables.stringValue("singleValue"));

    typedValue = decisionOutput.getSingleValueTyped();
    assertThat(typedValue).isEqualTo(Variables.stringValue("singleValue"));
  }

  // helper methods

  protected DmnDecisionResult evaluateWithMatchingRules(String... matchingRules) {
    VariableMap variables = Variables.createVariables();
    List<String> matchingRulesList = Arrays.asList(matchingRules);
    variables.putValue(NO_OUTPUT_VALUE, matchingRulesList.contains(NO_OUTPUT_VALUE));
    variables.putValue(SINGLE_OUTPUT_VALUE, matchingRulesList.contains(SINGLE_OUTPUT_VALUE));
    variables.putValue(MULTIPLE_OUTPUT_VALUES, matchingRulesList.contains(MULTIPLE_OUTPUT_VALUES));
    return engine.evaluate(decision, variables);
  }

  protected void assertSingleOutputValue(DmnDecisionOutput decisionOutput) {
    assertThat(decisionOutput.size()).isEqualTo(1);

    String value = decisionOutput.getValue("firstOutput");
    assertThat(value).isEqualTo("singleValue");

    value = decisionOutput.getValue("secondOutput");
    assertThat(value).isNull();

    value = decisionOutput.getFirstValue();
    assertThat(value).isEqualTo("singleValue");

    value = decisionOutput.getSingleValue();
    assertThat(value).isEqualTo("singleValue");
  }

  protected void assertNoOutputValue(DmnDecisionOutput decisionOutput) {
    assertThat(decisionOutput.size()).isEqualTo(0);

    String value = decisionOutput.getValue("firstOutput");
    assertThat(value).isNull();

    value = decisionOutput.getValue("secondOutput");
    assertThat(value).isNull();

    value = decisionOutput.getFirstValue();
    assertThat(value).isNull();

    value = decisionOutput.getSingleValue();
    assertThat(value).isNull();
  }

  protected void assertMultipleOutputValues(DmnDecisionOutput decisionOutput) {
    assertThat(decisionOutput.size()).isEqualTo(2);

    String value = decisionOutput.getValue("firstOutput");
    assertThat(value).isEqualTo("multipleValues1");

    value = decisionOutput.getValue("secondOutput");
    assertThat(value).isEqualTo("multipleValues2");

    value = decisionOutput.getFirstValue();
    assertThat(value).isEqualTo("multipleValues1");

    try {
      decisionOutput.getSingleValue();
      fail("Expected exception as result as more than one value");
    }
    catch (DmnResultException e) {
      assertThat(e)
        .hasMessageStartingWith("DMN-01020")
        .hasMessageContaining("multipleValues1")
        .hasMessageContaining("multipleValues2");
    }
  }

}
