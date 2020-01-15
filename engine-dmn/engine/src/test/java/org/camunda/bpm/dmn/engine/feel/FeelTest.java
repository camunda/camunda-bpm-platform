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
import org.camunda.bpm.dmn.feel.impl.FeelEngine;
import org.camunda.bpm.dmn.feel.impl.FeelException;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.feel.integration.CamundaFeelEngine;
import org.camunda.feel.integration.CamundaFeelEngineFactory;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.camunda.bpm.dmn.engine.util.DmnExampleVerifier.assertExample;

public class FeelTest extends DmnEngineTest {

  protected static final String FEEL_TEST_DMN = "FeelTest.dmn";
  protected static final String EMPTY_EXPRESSIONS_DMN = "org/camunda/bpm/dmn/engine/el/ExpressionLanguageTest.emptyExpressions.dmn";
  protected static final String DMN = "org/camunda/bpm/dmn/engine/el/FeelIntegrationTest.dmn";
  protected static final String DMN_12 = "org/camunda/bpm/dmn/engine/el/dmn12/FeelIntegrationTest.dmn";
  protected static final String DATE_AND_TIME_DMN = "org/camunda/bpm/dmn/engine/el/FeelIntegrationTest.testDateAndTimeIntegration.dmn";

  protected static final String DATE_TABLE_INPUT_CLAUSE_TYPE_DATE_CONVERSION =
    "DateConversionTable_InputClauseTypeDate.dmn11.dmn";

  protected static final String DATE_TABLE_INPUT_CLAUSE_TYPE_NON_CONVERSION =
    "DateConversionTable_NonInputClauseType.dmn11.dmn";

  protected FeelEngine scalaFeelEngine;

  @Override
  public DmnEngineConfiguration getDmnEngineConfiguration() {
    DefaultDmnEngineConfiguration configuration = new DefaultDmnEngineConfiguration();
    configuration.setFeelEngineFactory(new CamundaFeelEngineFactory());
    return configuration;
  }

  @Before
  public void assignEngines() {
    scalaFeelEngine = new CamundaFeelEngine();
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
  @DecisionResource(resource = FEEL_TEST_DMN)
  public void testStringVariable() {
    variables.putValue("stringInput", "camunda");
    variables.putValue("numberInput", 13.37);
    variables.putValue("booleanInput", true);

    assertThatDecisionTableResult()
      .hasSingleResult()
      .hasSingleEntry(true);
  }

  @Test
  @DecisionResource(resource = DMN)
  public void testFeelInputEntry() {
    DmnDecisionResult decisionResult = dmnEngine.evaluateDecision(decision, Variables.createVariables().putValue("score", 3));
    assertThat((String)decisionResult.getSingleEntry()).isEqualTo("a");
  }

  @Test
  @DecisionResource(resource = DMN)
  public void testFeelInputEntryWithAlternativeName() {
    DefaultDmnEngineConfiguration configuration = (DefaultDmnEngineConfiguration) getDmnEngineConfiguration();
    configuration.setDefaultInputEntryExpressionLanguage("feel");
    DmnEngine dmnEngine = configuration.buildEngine();

    DmnDecisionResult decisionResult = dmnEngine.evaluateDecision(decision, Variables.createVariables().putValue("score", 3));
    assertThat((String)decisionResult.getSingleEntry()).isEqualTo("a");
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
  @DecisionResource(resource = DATE_AND_TIME_DMN)
  public void testDateAndTimeIntegration() {
    Date testDate = new Date(1445526087000L);
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    variables.putValue("dateString", format.format(testDate));

    assertThatDecisionTableResult()
      .hasSingleResult()
      .hasSingleEntryTyped(Variables.dateValue(testDate));
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

  /* TODO move to feel-scala-spin artifact
  @Ignore("SPIN handling has changed")
  @Test
  @DecisionResource(resource = "FeelLegacy_SPIN.dmn")
  public void shouldHandleSpinCorrectly() {
    DefaultDmnEngineConfiguration configuration = (DefaultDmnEngineConfiguration) getDmnEngineConfiguration();
    DmnEngine engine = configuration.buildEngine();

    DmnDecisionResult decisionResult = engine.evaluateDecision(decision,
        Variables.createVariables()
            .putValue("foo", Spin.JSON("{ \"foo\": 7}")));

    assertThat((String)decisionResult.getSingleEntry()).isEqualTo("foo");
  }

  @Test
  @DecisionResource(resource = "FeelLegacy_SPIN_Context.dmn")
  public void shouldUseContextConversionForSpinValue() {
    DefaultDmnEngineConfiguration configuration = (DefaultDmnEngineConfiguration) getDmnEngineConfiguration();
    DmnEngine engine = configuration.buildEngine();

    DmnDecisionResult decisionResult = engine.evaluateDecision(decision,
        Variables.createVariables()
            .putValue("foo", Spin.JSON("{ \"bar\": 7}")));

    assertThat((String)decisionResult.getSingleEntry()).isEqualTo("bar");
  }*/

  @Test
  @DecisionResource(resource = "FeelLegacy_equals_boolean.dmn")
  public void shouldEqualBoolean() {
    DefaultDmnEngineConfiguration configuration = (DefaultDmnEngineConfiguration) getDmnEngineConfiguration();
    DmnEngine engine = configuration.buildEngine();

    DmnDecisionResult decisionResult = engine.evaluateDecision(decision, Variables.createVariables());

    assertThat((String)decisionResult.getSingleEntry()).isEqualTo("foo");
  }

  @Test
  @DecisionResource(resource = "FeelLegacy_compareDates_non_typed.dmn")
  public void shouldCompareDates() {
    variables.putValue("date1", new Date());
    variables.putValue("date2", new Date());

    assertThatDecisionTableResult()
    .hasSingleResult()
    .hasSingleEntryTyped(Variables.stringValue("foo"));
  }

  @Test
  @DecisionResource(resource = "FeelLegacy_compareDates_non_typed.dmn")
  public void shouldCompareLocalDateTimes() {
    variables.putValue("date1", LocalDateTime.now());
    variables.putValue("date2", LocalDateTime.now());

    assertThatDecisionTableResult()
    .hasSingleResult()
    .hasSingleEntryTyped(Variables.stringValue("foo"));
  }

  @Test
  @DecisionResource(resource = "FeelLegacy_compareDates_non_typed.dmn")
  public void shouldCompareZonedDateTimes() {
    variables.putValue("date1", ZonedDateTime.now());
    variables.putValue("date2", ZonedDateTime.now());

    assertThatDecisionTableResult()
    .hasSingleResult()
    .hasSingleEntryTyped(Variables.stringValue("foo"));
  }

  @Test
  @DecisionResource(resource = "FeelLegacy_compareDates_non_typed.dmn")
  public void shouldCompareJodaLocalDateTimes() {
    variables.putValue("date1", org.joda.time.LocalDateTime.now());
    variables.putValue("date2", org.joda.time.LocalDateTime.now());

    assertThatDecisionTableResult()
    .hasSingleResult()
    .hasSingleEntryTyped(Variables.stringValue("foo"));
  }

  @Test
  @DecisionResource(resource = "FeelLegacy_compareDates_non_typed.dmn")
  public void shouldCompareJodaDateTimes() {
    variables.putValue("date1", org.joda.time.DateTime.now());
    variables.putValue("date2", org.joda.time.DateTime.now());

    assertThatDecisionTableResult()
    .hasSingleResult()
    .hasSingleEntryTyped(Variables.stringValue("foo"));
  }

  @Test
  @DecisionResource(resource = "FeelLegacy_compareDates_non_typed.dmn")
  public void shouldCompareTypedDateValues() {
    variables.putValue("date1", Variables.dateValue(new Date()));
    variables.putValue("date2", Variables.dateValue(new Date()));

    assertThatDecisionTableResult()
    .hasSingleResult()
    .hasSingleEntryTyped(Variables.stringValue("foo"));
  }

  @Test
  @DecisionResource(resource = "DateConversionLit.dmn11.xml")
  public void shouldEvaltuateToUtilDateWithLiteralExpression() {
    // given
    getVariables()
      .putValue("date1", new Date());

    // when
    Object foo = evaluateDecision().getSingleEntry();

    // then
    assertThat(foo).isInstanceOf(Date.class);
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

  @Test
  @DecisionResource(resource = DATE_TABLE_INPUT_CLAUSE_TYPE_NON_CONVERSION)
  public void shouldEvaluateUtilDateWithTable_NonInputClauseType() {
    // given
    getVariables()
      .putValue("date1", new Date());

    // when
    String foo = evaluateDecision().getSingleEntry();

    // then
    assertThat(foo).isEqualTo("not ok");
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

  @Test
  @DecisionResource(resource = DATE_TABLE_INPUT_CLAUSE_TYPE_DATE_CONVERSION)
  public void shouldEvaluateUtilDateWithTable_InputClauseTypeDate() {
    // given
    getVariables()
      .putValue("date1", new Date());

    // when
    String foo = evaluateDecision().getSingleEntry();

    // then
    assertThat(foo).isEqualTo("not ok");
  }

  @Test
  @DecisionResource(resource = DATE_TABLE_INPUT_CLAUSE_TYPE_DATE_CONVERSION)
  public void shouldEvaluateJodaDateWithTable_InputClauseTypeDate() {
    // given
    getVariables()
      .putValue("date1", org.joda.time.LocalDate.now());

    // when
    String foo = evaluateDecision().getSingleEntry();

    // then
    assertThat(foo).isEqualTo("not ok");
  }

  @Test
  @DecisionResource(resource = DATE_TABLE_INPUT_CLAUSE_TYPE_DATE_CONVERSION)
  public void shouldEvaluateStringDateWithTable_InputClauseTypeDate() {
    // given
    getVariables()
      .putValue("date1", "2019-08-22T22:22:22");

    // when
    String foo = evaluateDecision().getSingleEntry();

    // then
    assertThat(foo).isEqualTo("not ok");
  }

  @Test
  @DecisionResource(resource = "DateConversionTable_OutputTypeDate.dmn11.dmn")
  public void shouldOutputDateWithTable_OutputClauseTypeDate() {
    // given
    getVariables()
      .putValue("string1", "ok");

    // when
    Date foo = evaluateDecision().getSingleEntry();

    // then
    assertThat(foo).isEqualTo(new Date(1_543_575_600_000L));
  }

  @Test
  @DecisionResource(resource = "DateConversionTable_OutputFromVariableTypeDate.dmn11.dmn")
  public void shouldOutputDateResolveVariableWithTable_OutputClauseTypeDate() {
    // given
    Date date1 = new Date();
    getVariables()
      .putValue("string1", "ok")
      .putValue("date1", date1);

    // when
    Date foo = evaluateDecision().getSingleEntry();

    // then
    assertThat(foo).isEqualTo(date1);
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
  @DecisionResource(resource = "DateConversionTable_InputClausePerson.dmn11.dmn")
  public void shouldEvaluateInputClause_Object() {
    // given
    getVariables()
      .putValue("date1", new Date())
      .putValue("person", new Person(new Date()));

    // when
    String foo = evaluateDecision().getSingleEntry();

    // then
    assertThat(foo).isEqualTo("bar");
  }

  @Test
  @DecisionResource(resource = "DateConversionTable_OutputClauseDateBuiltinFunction.dmn11.dmn")
  public void shouldEvaluateOutputClause() {
    // given

    // when
    String foo = evaluateDecision().getSingleEntry();

    // then
    assertThat(foo).isEqualTo("bar");
  }

  @Test
  public void shouldEvaluateDateAndTime_Scala() throws ParseException {
    Date dateTime = new SimpleDateFormat("YYYY-MM-DD'T'HH:MM:ss")
      .parse("2015-12-12T22:12:53");

    getVariables()
      .putValue("input", dateTime);

    boolean input = scalaFeelEngine.evaluateSimpleUnaryTests("date and time(\"2015-12-12T22:12:53\")",
      "input", getVariables().asVariableContext());

    assertThat(input).isFalse();
  }

  public class Person {

    Date birthday;

    public Person(Date age) {
      this.birthday = age;
    }

    public Date getBirthday() {
      return birthday;
    }

  }

}
