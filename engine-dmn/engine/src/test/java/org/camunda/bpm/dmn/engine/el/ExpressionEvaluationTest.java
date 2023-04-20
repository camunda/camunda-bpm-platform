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
package org.camunda.bpm.dmn.engine.el;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.test.DecisionResource;
import org.camunda.bpm.dmn.engine.test.DmnEngineTest;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Test;

public class ExpressionEvaluationTest extends DmnEngineTest {

    protected static final String DMN_INPUT_VARIABLE = "org/camunda/bpm/dmn/engine/el/ExpressionEvaluationTest.inputVariableName.dmn";
    protected static final String DMN_OVERRIDE_INPUT_VARIABLE = "org/camunda/bpm/dmn/engine/el/ExpressionEvaluationTest.overrideInputVariableName.dmn";
    protected static final String DMN_VARIABLE_CONTEXT = "org/camunda/bpm/dmn/engine/el/ExpressionEvaluationTest.variableContext.dmn";
    protected static final String DMN_VARIABLE_CONTEXT_WITH_INPUT_VARIABLE = "org/camunda/bpm/dmn/engine/el/ExpressionEvaluationTest.variableContextWithInputVariable.dmn";

    @Test
    @DecisionResource(resource = DMN_INPUT_VARIABLE)
    public void testHasInputVariableName() {
      DmnDecisionResult decisionResult = dmnEngine.evaluateDecision(decision, Variables.createVariables().putValue("inVar", 2));

      assertThat((boolean) decisionResult.getSingleEntry()).isEqualTo(true);
    }

    @Test
    @DecisionResource(resource = DMN_OVERRIDE_INPUT_VARIABLE)
    public void testOverrideInputVariableName() {
      DmnDecisionResult decisionResult = dmnEngine.evaluateDecision(decision, Variables.createVariables().putValue("inVar", 2));

      assertThat((boolean) decisionResult.getSingleEntry()).isEqualTo(true);
    }

    @Test
    @DecisionResource(resource = DMN_VARIABLE_CONTEXT)
    public void testHasVariableContext() {
      DmnDecisionResult decisionResult = dmnEngine.evaluateDecision(decision, Variables.createVariables().putValue("inVar", 3));

      assertThat((boolean) decisionResult.getSingleEntry()).isEqualTo(true);
    }

    @Test
    @DecisionResource(resource = DMN_VARIABLE_CONTEXT_WITH_INPUT_VARIABLE)
    public void testHasInputVariableNameInVariableContext() {
      DmnDecisionResult decisionResult = dmnEngine.evaluateDecision(decision, Variables.createVariables().putValue("inVar", 3));

      assertThat((boolean) decisionResult.getSingleEntry()).isEqualTo(true);
    }

}
