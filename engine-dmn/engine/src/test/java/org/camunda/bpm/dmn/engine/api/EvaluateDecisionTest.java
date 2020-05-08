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

import static org.assertj.core.api.Assertions.entry;

import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.test.DecisionResource;
import org.camunda.bpm.dmn.engine.test.DmnEngineTest;
import org.junit.Test;

public class EvaluateDecisionTest extends DmnEngineTest {

  public static final String NO_INPUT_DMN = "org/camunda/bpm/dmn/engine/api/NoInput.dmn";
  public static final String ONE_RULE_DMN = "org/camunda/bpm/dmn/engine/api/OneRule.dmn";
  public static final String EXAMPLE_DMN = "org/camunda/bpm/dmn/engine/api/Example.dmn";
  public static final String DATA_TYPE_DMN = "org/camunda/bpm/dmn/engine/api/DataType.dmn";

  public static final String DMN12_NO_INPUT_DMN = "org/camunda/bpm/dmn/engine/api/dmn12/NoInput.dmn";
  public static final String DMN13_NO_INPUT_DMN = "org/camunda/bpm/dmn/engine/api/dmn13/NoInput.dmn";

  @Override
  public DmnEngineConfiguration getDmnEngineConfiguration() {
    return new DefaultDmnEngineConfiguration()
      .enableFeelLegacyBehavior(true);
  }

  @Test
  @DecisionResource(resource = NO_INPUT_DMN)
  public void shouldEvaluateRuleWithoutInput() {
    assertThatDecisionTableResult()
      .hasSingleResult()
      .hasSingleEntry("ok");
  }

  @Test
  @DecisionResource(resource = ONE_RULE_DMN)
  public void shouldEvaluateSingleRule() {
    variables.putValue("input", "ok");

    assertThatDecisionTableResult()
      .hasSingleResult()
      .hasSingleEntry("ok");

    variables.putValue("input", "notok");

    assertThatDecisionTableResult()
      .isEmpty();
  }

  @Test
  @DecisionResource(resource = EXAMPLE_DMN)
  public void shouldEvaluateExample() {
    variables.put("status", "bronze");
    variables.put("sum", 200);

    assertThatDecisionTableResult()
      .hasSingleResult()
      .containsOnly(entry("result", "notok"), entry("reason", "work on your status first, as bronze you're not going to get anything"));

    variables.put("status", "silver");

    assertThatDecisionTableResult()
      .hasSingleResult()
      .containsOnly(entry("result", "ok"), entry("reason", "you little fish will get what you want"));

    variables.put("sum", 1200);

    assertThatDecisionTableResult()
      .hasSingleResult()
      .containsOnly(entry("result", "notok"), entry("reason", "you took too much man, you took too much!"));

    variables.put("status", "gold");
    variables.put("sum", 200);


    assertThatDecisionTableResult()
      .hasSingleResult()
      .containsOnly(entry("result", "ok"), entry("reason", "you get anything you want"));
  }

  @Test
  @DecisionResource(resource = DATA_TYPE_DMN)
  public void shouldDetectDataTypes() {
    variables.put("boolean", true);
    variables.put("integer", 9000);
    variables.put("double", 13.37);

    assertThatDecisionTableResult()
      .hasSingleResult()
      .hasSingleEntry(true);

    variables.put("boolean", false);
    variables.put("integer", 10000);
    variables.put("double", 21.42);

    assertThatDecisionTableResult()
      .hasSingleResult()
      .hasSingleEntry(true);

    variables.put("boolean", true);
    variables.put("integer", -9000);
    variables.put("double", -13.37);

    assertThatDecisionTableResult()
      .hasSingleResult()
      .hasSingleEntry(true);
  }

  @Test
  @DecisionResource(resource = DMN12_NO_INPUT_DMN)
  public void shouldEvaluateRuleWithoutInput_Dmn12() {
    assertThatDecisionTableResult()
      .hasSingleResult()
      .hasSingleEntry("ok");
  }

  @Test
  @DecisionResource(resource = DMN13_NO_INPUT_DMN)
  public void shouldEvaluateRuleWithoutInput_Dmn13() {
    assertThatDecisionTableResult()
      .hasSingleResult()
      .hasSingleEntry("ok");
  }

}

