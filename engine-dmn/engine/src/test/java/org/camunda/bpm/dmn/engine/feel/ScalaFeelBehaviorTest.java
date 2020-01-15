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
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.feel.integration.CamundaFeelEngineFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class ScalaFeelBehaviorTest extends DmnEngineTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Override
  public DmnEngineConfiguration getDmnEngineConfiguration() {
    DefaultDmnEngineConfiguration configuration = new DefaultDmnEngineConfiguration();
    configuration.setFeelEngineFactory(new CamundaFeelEngineFactory());
    return configuration;
  }

  /**
   * Works with the Java FEEL engine because it ignores the time zone
   * when processing the time zoned-String in the expression for the resulting
   * Java Util Date
   *
   * Also works in the Scala FEEL engine because Java Util Dates (the input variable)
   * get a time zone attached there and the time zone is respected for the expression
   * string
   *
   * THIS MIGHT BREAK when we change the transformation behavior
   * in the Scala FEEL engine to not attach time zone information to
   * Java Util Dates
   */
  @Test
  @DecisionResource(resource = "FeelLegacy_compareDate_withTimeZone_non_typed.dmn")
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
  @DecisionResource(resource = "FeelLegacy_compareDate_withTimeZone_non_typed.dmn")
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
  @DecisionResource(resource = "FeelLegacy_SingleQuotes.dmn")
  public void shouldUseSingleQuotesInStringLiterals() {
    DefaultDmnEngineConfiguration configuration = (DefaultDmnEngineConfiguration) getDmnEngineConfiguration();
    DmnEngine engine = configuration.buildEngine();

    DmnDecisionResult decisionResult = engine.evaluateDecision(decision,
      Variables.createVariables().putValue("input", "Hello World"));

    assertThat((String)decisionResult.getSingleEntry()).isEqualTo("foo");
  }

}
