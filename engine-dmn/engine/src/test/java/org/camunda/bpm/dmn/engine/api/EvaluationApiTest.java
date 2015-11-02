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
package org.camunda.bpm.dmn.engine.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.variable.Variables.createVariables;
import static org.camunda.bpm.engine.variable.Variables.emptyVariableContext;
import static org.junit.Assert.fail;

import java.util.Map;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionModel;
import org.camunda.bpm.dmn.engine.DmnExpressionException;
import org.camunda.bpm.dmn.engine.test.DmnDecisionTest;
import org.camunda.bpm.engine.variable.context.VariableContext;
import org.junit.Before;
import org.junit.Test;

/**
 * Simple api test making sure the api methods are there and accept the right parameters
 *
 * @author Daniel Meyer
 *
 */
public class EvaluationApiTest extends DmnDecisionTest {

  public static final String ONE_RULE_DMN = "org/camunda/bpm/dmn/engine/OneRule.dmn";

  protected DmnDecision decision;
  protected DmnDecisionModel decisionModel;

  @Before
  public void beforeTestParseDecision() {
    decisionModel = engine.parseDecisionModel(ONE_RULE_DMN);
    decision = decisionModel.getDecisions().get(0);
  }

  @Test
  public void shouldFailIfDecisionIsNull() {

    try {
      engine.evaluate((DmnDecision) null, emptyVariableContext());
      fail("Exception expected");
    }
    catch(IllegalArgumentException e) {
      assertThat(e).hasMessageStartingWith("UTILS-02001");
    }

    try {
      engine.evaluate((DmnDecision) null, createVariables());
      fail("Exception expected");
    }
    catch(IllegalArgumentException e) {
      assertThat(e).hasMessageStartingWith("UTILS-02001");
    }

  }

  @Test
  public void shouldFailIfDecisionModelIsNull() {

    try {
      engine.evaluate((DmnDecisionModel) null, emptyVariableContext());
      fail("Exception expected");
    }
    catch(IllegalArgumentException e) {
      assertThat(e).hasMessageStartingWith("UTILS-02001");
    }

    try {
      engine.evaluate((DmnDecisionModel) null, createVariables());
      fail("Exception expected");
    }
    catch(IllegalArgumentException e) {
      assertThat(e).hasMessageStartingWith("UTILS-02001");
    }

    try {
      engine.evaluate(null, "decision", emptyVariableContext());
      fail("Exception expected");
    }
    catch(IllegalArgumentException e) {
      assertThat(e).hasMessageStartingWith("UTILS-02001");
    }

    try {
      engine.evaluate(null, "decision", createVariables());
      fail("Exception expected");
    }
    catch(IllegalArgumentException e) {
      assertThat(e).hasMessageStartingWith("UTILS-02001");
    }

  }


  @Test
  public void shouldFailIfDecisionKeyIsNull() {

    try {
      engine.evaluate(decisionModel, null, emptyVariableContext());
      fail("Exception expected");
    }
    catch(IllegalArgumentException e) {
      assertThat(e).hasMessageStartingWith("UTILS-02001");
    }

    try {
      engine.evaluate(decisionModel, null, createVariables());
      fail("Exception expected");
    }
    catch(IllegalArgumentException e) {
      assertThat(e).hasMessageStartingWith("UTILS-02001");
    }

  }


  @Test
  public void shouldFailIfVariablesIsNull() {

    try {
      engine.evaluate(decision, (Map<String, Object>) null);
      fail("Exception expected");
    }
    catch(IllegalArgumentException e) {
      assertThat(e).hasMessageStartingWith("UTILS-02001");
    }

    try {
      engine.evaluate(decision, (VariableContext) null);
      fail("Exception expected");
    }
    catch(IllegalArgumentException e) {
      assertThat(e).hasMessageStartingWith("UTILS-02001");
    }

    try {
      engine.evaluate(decisionModel, (Map<String, Object>) null);
      fail("Exception expected");
    }
    catch(IllegalArgumentException e) {
      assertThat(e).hasMessageStartingWith("UTILS-02001");
    }

    try {
      engine.evaluate(decisionModel, (VariableContext) null);
      fail("Exception expected");
    }
    catch(IllegalArgumentException e) {
      assertThat(e).hasMessageStartingWith("UTILS-02001");
    }

    try {
      engine.evaluate(decisionModel, "decision", (Map<String, Object>) null);
      fail("Exception expected");
    }
    catch(IllegalArgumentException e) {
      assertThat(e).hasMessageStartingWith("UTILS-02001");
    }

    try {
      engine.evaluate(decisionModel, "decision", (VariableContext) null);
      fail("Exception expected");
    }
    catch(IllegalArgumentException e) {
      assertThat(e).hasMessageStartingWith("UTILS-02001");
    }

  }

  @Test
  public void shouldEvaluateDecisionWithVariableMap() {

    engine.evaluate(decision, createVariables().putValue("input", "someValue"));

  }


  @Test
  public void shouldNotEvaluateDecisionWithEmptyVariableMap() {

    try {
      engine.evaluate(decision, createVariables());
      fail("Exception expected");
    }
    catch(DmnExpressionException e) {
      // expected
    }

  }

  @Test
  public void shouldEvaluateDecisionWithVariableContext() {

    engine.evaluate(decision, createVariables().putValue("input", "someValue").asVariableContext());

  }

  @Test
  public void shouldNotEvaluateDecisionWithEmptyVariableContext() {

    try {
      engine.evaluate(decision, emptyVariableContext());
      fail("Exception expected");
    }
    catch(DmnExpressionException e) {
      // expected
    }
  }

  @Test
  public void shouldEvaluateDecisionModelWithVariableMap() {

    engine.evaluate(decisionModel, createVariables().putValue("input", "someValue"));

  }


  @Test
  public void shouldNotEvaluateDecisionModelWithEmptyVariableMap() {

    try {
      engine.evaluate(decisionModel, createVariables());
      fail("Exception expected");
    }
    catch(DmnExpressionException e) {
      // expected
    }

  }

  @Test
  public void shouldEvaluateDecisionModelWithVariableContext() {

    engine.evaluate(decisionModel, createVariables().putValue("input", "someValue").asVariableContext());

  }


  @Test
  public void shouldNotEvaluateDecisionModelWithEmptyVariableContext() {

    try {
      engine.evaluate(decisionModel, emptyVariableContext());
      fail("Exception expected");
    }
    catch(DmnExpressionException e) {
      // expected
    }

  }

  @Test
  public void shouldEvaluateDecisionModelByKeyWithVariableMap() {

    engine.evaluate(decisionModel, "decision", createVariables().putValue("input", "someValue"));

  }


  @Test
  public void shouldNotEvaluateDecisionModelByKeyWithEmptyVariableMap() {

    try {
      engine.evaluate(decisionModel, "decision", createVariables());
      fail("Exception expected");
    }
    catch(DmnExpressionException e) {
      // expected
    }

  }

  @Test
  public void shouldEvaluateDecisionModelByKeyWithVariableContext() {

    engine.evaluate(decisionModel, "decision", createVariables().putValue("input", "someValue").asVariableContext());

  }


  @Test
  public void shouldNotEvaluateDecisionModelByKeyWithEmptyVariableContext() {

    try {
      engine.evaluate(decisionModel, "decision", emptyVariableContext());
      fail("Exception expected");
    }
    catch(DmnExpressionException e) {
      // expected
    }

  }


}
