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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.feel.helper.TestPojo;
import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.test.DecisionResource;
import org.camunda.bpm.dmn.engine.test.DmnEngineTest;
import org.camunda.bpm.dmn.feel.impl.FeelException;
import org.camunda.bpm.dmn.feel.impl.scala.ScalaFeelEngineFactory;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class BreakingScalaFeelBehaviorTest extends DmnEngineTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Override
  public DmnEngineConfiguration getDmnEngineConfiguration() {
    DefaultDmnEngineConfiguration configuration = new DefaultDmnEngineConfiguration();
    configuration.setFeelEngineFactory(new ScalaFeelEngineFactory());
    return configuration;
  }

  // https://jira.camunda.com/browse/CAM-11304
  @Test
  @DecisionResource(resource = "breaking_unary_test_compare_short_untyped.dmn")
  public void shouldCompareShortUntyped() {
    variables.putValue("numberInput", (short)5);

    assertThatDecisionTableResult()
      .hasSingleResult()
      .hasSingleEntry(true);
  }

  @Ignore("CAM-11269")
  @Test
  @DecisionResource(resource = "breaking_unary_test_boolean.dmn")
  public void shouldEqualBoolean() {
    DefaultDmnEngineConfiguration configuration = (DefaultDmnEngineConfiguration) getDmnEngineConfiguration();
    DmnEngine engine = configuration.buildEngine();

    DmnDecisionResult decisionResult = engine.evaluateDecision(decision, Variables.createVariables());

    assertThat((String)decisionResult.getSingleEntry()).isEqualTo("foo");
  }

  @Test
  @DecisionResource(resource = "breaking_compare_date_with_time_zone_untyped.dmn")
  public void shouldEvaluateTimezoneComparisonWithTypedValue() {
    // given
    variables.putValue("date1", Variables.dateValue(new Date()));

    // then
    thrown.expect(FeelException.class);
    thrown.expectMessage("can not be compared to ValDateTime(2019-09-12T13:00+02:00[Europe/Berlin])");

    // when
    assertThatDecisionTableResult()
      .hasSingleResult()
      .hasSingleEntryTyped(Variables.stringValue("foo"));
  }

  @Test
  @DecisionResource(resource = "breaking_compare_date_with_time_zone_untyped.dmn")
  public void shouldEvaluateTimezoneComparisonWithDate() {
    variables.putValue("date1", new Date());

    // then
    thrown.expect(FeelException.class);
    thrown.expectMessage("can not be compared to ValDateTime(2019-09-12T13:00+02:00[Europe/Berlin])");

    // when
    assertThatDecisionTableResult()
      .hasSingleResult()
      .hasSingleEntryTyped(Variables.stringValue("foo"));
  }

  @Test
  @DecisionResource(resource = "breaking_single_quotes.dmn")
  public void shouldUseSingleQuotesInStringLiterals() {
    // given
    DefaultDmnEngineConfiguration configuration = (DefaultDmnEngineConfiguration) getDmnEngineConfiguration();
    DmnEngine engine = configuration.buildEngine();

    // then
    thrown.expect(FeelException.class);
    thrown.expectMessage("failed to parse expression ''Hello World'': " +
      "Expected (\"not\" | positiveUnaryTests | unaryTests):1:1, found \"'Hello Wor\"");

    // when
    engine.evaluateDecision(decision, Variables.createVariables().putValue("input", "Hello World"));
  }

  @Ignore("CAM-11319")
  @Test
  @DecisionResource(resource = "breaking_pojo_comparison.dmn")
  public void shouldComparePojo() {
    // given
    variables.putValue("pojoOne", new TestPojo())
      .putValue("pojoTwo", new TestPojo());

    // then
    assertThatDecisionTableResult()
      .hasSingleResult()
      .hasSingleEntry("foo");
  }

}
