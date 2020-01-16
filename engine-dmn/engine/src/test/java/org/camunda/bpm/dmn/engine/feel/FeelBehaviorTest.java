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
import org.camunda.bpm.dmn.feel.impl.juel.FeelEngineFactoryImpl;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.feel.integration.CamundaFeelEngineFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class FeelBehaviorTest extends DmnEngineTest {

  @Parameterized.Parameters
  public static List<String> data() {
    return Arrays.asList("juel", "feel");
  }

  protected static final String FEEL_TEST_DMN = "FeelTest.dmn";
  protected static final String EMPTY_EXPRESSIONS_DMN = "org/camunda/bpm/dmn/engine/el/ExpressionLanguageTest.emptyExpressions.dmn";
  protected static final String DMN = "org/camunda/bpm/dmn/engine/el/FeelIntegrationTest.dmn";
  protected static final String DMN_12 = "org/camunda/bpm/dmn/engine/el/dmn12/FeelIntegrationTest.dmn";
  protected static final String DATE_AND_TIME_DMN = "org/camunda/bpm/dmn/engine/el/FeelIntegrationTest.testDateAndTimeIntegration.dmn";

  protected static final String DATE_TABLE_INPUT_CLAUSE_TYPE_DATE_CONVERSION =
    "DateConversionTable_InputClauseTypeDate.dmn11.dmn";

  protected static final String DATE_TABLE_INPUT_CLAUSE_TYPE_NON_CONVERSION =
    "DateConversionTable_NonInputClauseType.dmn11.dmn";

  protected String engine;

  public FeelBehaviorTest(String engine) {
    this.engine = engine;
  }

  @Override
  public DmnEngineConfiguration getDmnEngineConfiguration() {
    DefaultDmnEngineConfiguration configuration = new DefaultDmnEngineConfiguration();
    if ("juel".equals(engine)) {
      configuration.setFeelEngineFactory(new FeelEngineFactoryImpl());

    } else if ("feel".equals(engine)) {
      configuration.setFeelEngineFactory(new CamundaFeelEngineFactory());

    }
    return configuration;
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

  public static class Person {

    Date birthday;

    public Person(Date age) {
      this.birthday = age;
    }

    public Date getBirthday() {
      return birthday;
    }

  }

}
