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
package org.camunda.bpm.dmn.engine.test.junit5;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.variable.Variables.createVariables;

import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.commons.utils.IoUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(DmnEngineExtension.class)
class DmnEngineExtensionTest {
  public static final String DMN_MINIMAL = "org/camunda/bpm/dmn/engine/test/junit5/DecisionWithLiteralExpression.dmn";

  DmnEngine dmnEngineField;

  @Test
  void shouldInjectClassField() {
    shouldEvaluateDecisionWithLiteralExpression(dmnEngineField);
  }

  @Test
  void shouldInjectMethodParameter(DmnEngine dmnEngineParam) {
    shouldEvaluateDecisionWithLiteralExpression(dmnEngineParam);
  }

  protected void shouldEvaluateDecisionWithLiteralExpression(DmnEngine dmnEngine) {
    DmnDecisionResult result = dmnEngine.evaluateDecision(
        dmnEngine.parseDecision("decision", IoUtil.fileAsStream(DMN_MINIMAL)),
        createVariables()
            .putValue("a", 2)
            .putValue("b", 3));

    assertThat(result.getSingleResult()).containsOnlyKeys("c");
    assertThat((int) result.getSingleEntry()).isEqualTo(5);
  }
}
