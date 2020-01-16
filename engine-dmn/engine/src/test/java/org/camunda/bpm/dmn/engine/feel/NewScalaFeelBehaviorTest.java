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
package org.camunda.bpm.dmn.engine.feel;

import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.test.DecisionResource;
import org.camunda.bpm.dmn.engine.test.DmnEngineTest;
import org.camunda.bpm.dmn.feel.impl.FeelException;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.feel.integration.CamundaFeelEngine;
import org.camunda.feel.integration.CamundaFeelEngineFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.camunda.bpm.dmn.engine.feel.FeelBehaviorTest.DATE_TABLE_INPUT_CLAUSE_TYPE_NON_CONVERSION;
import static org.camunda.bpm.dmn.engine.feel.FeelBehaviorTest.DMN;
import static org.camunda.bpm.dmn.engine.feel.FeelBehaviorTest.DMN_12;
import static org.camunda.bpm.dmn.engine.feel.FeelBehaviorTest.EMPTY_EXPRESSIONS_DMN;
import static org.camunda.bpm.dmn.engine.util.DmnExampleVerifier.assertExample;

public class NewScalaFeelBehaviorTest extends DmnEngineTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Override
  public DmnEngineConfiguration getDmnEngineConfiguration() {
    DefaultDmnEngineConfiguration configuration = new DefaultDmnEngineConfiguration();
    configuration.setFeelEngineFactory(new CamundaFeelEngineFactory());
    return configuration;
  }

  protected CamundaFeelEngine scalaFeelEngine;

  @Before
  public void assignEngines() {
    scalaFeelEngine = new CamundaFeelEngine();
  }

  @Test
  @DecisionResource(resource = "DateConversionTable_InputExpression.dmn11.dmn")
  public void shouldEvaluateInputExpression() {
    // given
    getVariables()
      .putValue("date1", new Date())
      .putValue("date2", new Date());
    // when
    String foo = evaluateDecision().getSingleEntry();

    // then
    assertThat(foo).isEqualTo("bar");
  }

  @Test
  @DecisionResource(resource = "DateConversionTable_InputExpressionDateAndTime.dmn11.dmn")
  public void shouldEvaluateInputExpression2() {
    // given
    getVariables()
      .putValue("date1", new Date());


    // when
    String foo = evaluateDecision().getSingleEntry();

    // then
    assertThat(foo).isEqualTo("foo");
  }

  @Test
  @DecisionResource(resource = DATE_TABLE_INPUT_CLAUSE_TYPE_NON_CONVERSION)
  public void shouldEvaluateJodaDateWithTable_NonInputClauseType() {
    // given
    getVariables()
      .putValue("date1", org.joda.time.LocalDateTime.now());

    // when
    String foo = evaluateDecision().getSingleEntry();

    // then
    assertThat(foo).isEqualTo("not ok");
  }

  @Test
  @DecisionResource(resource = "FeelLegacy_compareDate_withTimeZone_non_typed.dmn")
  public void shouldEvaluateTimezoneComparisonWithZonedDateTime() {
    variables.putValue("date1", ZonedDateTime.now());

    assertThatDecisionTableResult()
      .hasSingleResult()
      .hasSingleEntryTyped(Variables.stringValue("bar"));
  }

  @Test
  @DecisionResource(resource = EMPTY_EXPRESSIONS_DMN, decisionKey = "decision2")
  public void testFailFeelUseOfEmptyInputExpression() {
    try {
      evaluateDecisionTable(dmnEngine);
      failBecauseExceptionWasNotThrown(FeelException.class);
    }
    catch (FeelException e) {
      assertThat(e).hasMessageContaining("failed to evaluate expression '10': no variable found for name 'cellInput'");
    }
  }

  @Test
  @DecisionResource(resource = "org/camunda/bpm/dmn/engine/el/ExpressionLanguageTest.script.dmn")
  public void testFeelExceptionDoesNotContainJuel() {
    try {
      assertExample(dmnEngine, decision);
      failBecauseExceptionWasNotThrown(FeelException.class);
    }
    catch (FeelException e) {
      assertThat(e).hasMessageContaining("failed to parse expression 'cellInput == \"bronze\"'");
    }
  }

  @Test
  @DecisionResource(resource = DMN_12)
  public void testFeelInputExpression_Dmn12() {
    testFeelInputExpression();
  }

  @Test
  @DecisionResource(resource = DMN)
  public void testFeelOutputEntry() {
    DefaultDmnEngineConfiguration configuration = (DefaultDmnEngineConfiguration) getDmnEngineConfiguration();
    configuration.setDefaultOutputEntryExpressionLanguage(DefaultDmnEngineConfiguration.FEEL_EXPRESSION_LANGUAGE);
    DmnEngine engine = configuration.buildEngine();

    variables.putValue("score", 3);

    assertThatDecisionTableResult(engine)
      .hasSingleResult()
      .hasSingleEntryTyped(Variables.stringValue("a"));
  }

  @Test
  @DecisionResource(resource = DMN)
  public void testFeelInputExpressionWithCustomEngine() {
    DefaultDmnEngineConfiguration configuration = (DefaultDmnEngineConfiguration) getDmnEngineConfiguration();
    configuration.setDefaultInputExpressionExpressionLanguage(DefaultDmnEngineConfiguration.FEEL_EXPRESSION_LANGUAGE);
    DmnEngine engine = configuration.buildEngine();

    DmnDecisionResult decisionResult = engine.evaluateDecision(decision, Variables.createVariables().putValue("score", 3));

    assertThat((String)decisionResult.getSingleEntry()).isEqualTo("a");
  }

  @Test
  @DecisionResource(resource = DMN)
  public void testFeelOutputEntryWithCustomEngine() {
    DefaultDmnEngineConfiguration configuration = (DefaultDmnEngineConfiguration) getDmnEngineConfiguration();
    configuration.setDefaultOutputEntryExpressionLanguage(DefaultDmnEngineConfiguration.FEEL_EXPRESSION_LANGUAGE);
    DmnEngine engine = configuration.buildEngine();

    DmnDecisionResult decisionResult = engine.evaluateDecision(decision, Variables.createVariables().putValue("score", 3));

    assertThat((String)decisionResult.getSingleEntry()).isEqualTo("a");
  }

  @Test
  @DecisionResource
  public void testUnaryTest() {
    variables.putValue("integerString", "45");

    assertThatDecisionTableResult()
      .hasSingleResult()
      .hasSingleEntryTyped(Variables.integerValue(45));
  }

  @Test
  @DecisionResource(resource = DMN)
  public void testFeelInputExpression() {
    DefaultDmnEngineConfiguration configuration = (DefaultDmnEngineConfiguration) getDmnEngineConfiguration();
    configuration.setDefaultInputExpressionExpressionLanguage(DefaultDmnEngineConfiguration.FEEL_EXPRESSION_LANGUAGE);
    DmnEngine engine = configuration.buildEngine();

    variables.putValue("score", 3);

    assertThatDecisionTableResult(engine)
      .hasSingleResult()
      .hasSingleEntryTyped(Variables.stringValue("a"));
  }

  @Test
  @DecisionResource(resource = DATE_TABLE_INPUT_CLAUSE_TYPE_NON_CONVERSION)
  public void shouldEvaluateLocalDateWithTable_NonInputClauseType() {
    // given
    getVariables()
      .putValue("date1", LocalDateTime.now());

    // when
    String foo = evaluateDecision().getSingleEntry();

    // then
    assertThat(foo).isEqualTo("not ok");
  }

  /* TODO: rewrite to blackbox test */
  @Test
  public void shouldEvaluateLocalDateWithScalaEngine_InputClauseTypeDate() {
    // given
    VariableMap variables = getVariables()
      .putValue("date1", LocalDateTime.now());

    // when
    boolean result = scalaFeelEngine.evaluateSimpleUnaryTests("<=date and time(\"2014-11-30T12:00:00\")",
      "date1", variables.asVariableContext());

    // then
    assertThat(result).isFalse();
  }

  /* TODO: rewrite to blackbox test */
  @Test
  public void shouldEvaluateJodaLocalDateWithScalaEngine_InputClauseTypeDate() {
    // given
    VariableMap variables = getVariables()
      .putValue("date1", org.joda.time.LocalDateTime.now());

    // when
    boolean result = scalaFeelEngine.evaluateSimpleUnaryTests("<=date and time(\"2014-11-30T12:00:00\")",
      "date1", variables.asVariableContext());

    // then
    assertThat(result).isFalse();
  }

  /* TODO: rewrite to blackbox test */
  @Test
  public void shouldEvaluateUtilDateWithScalaEngine_InputClauseTypeDate() {
    // given
    VariableMap variables = getVariables()
      .putValue("date1", new Date());

    // when
    boolean result = scalaFeelEngine.evaluateSimpleUnaryTests("<=date and time(\"2014-11-30T12:00:00\")",
      "date1", variables.asVariableContext());

    // then
    assertThat(result).isFalse();
  }

}
