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
package org.camunda.bpm.dmn.engine.impl.evaluation;

import java.util.Collections;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.DmnDecisionResultEntries;
import org.camunda.bpm.dmn.engine.delegate.DmnDecisionLiteralExpressionEvaluationEvent;
import org.camunda.bpm.dmn.engine.delegate.DmnDecisionLogicEvaluationEvent;
import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionLiteralExpressionImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionResultEntriesImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionResultImpl;
import org.camunda.bpm.dmn.engine.impl.DmnExpressionImpl;
import org.camunda.bpm.dmn.engine.impl.DmnVariableImpl;
import org.camunda.bpm.dmn.engine.impl.delegate.DmnDecisionLiteralExpressionEvaluationEventImpl;
import org.camunda.bpm.engine.variable.context.VariableContext;
import org.camunda.bpm.engine.variable.value.TypedValue;

public class DecisionLiteralExpressionEvaluationHandler implements DmnDecisionLogicEvaluationHandler {

  protected final ExpressionEvaluationHandler expressionEvaluationHandler;

  protected final String literalExpressionLanguage;

  public DecisionLiteralExpressionEvaluationHandler(DefaultDmnEngineConfiguration configuration) {
    expressionEvaluationHandler = new ExpressionEvaluationHandler(configuration);

    literalExpressionLanguage = configuration.getDefaultLiteralExpressionLanguage();
  }

  @Override
  public DmnDecisionLogicEvaluationEvent evaluate(DmnDecision decision, VariableContext variableContext) {
    DmnDecisionLiteralExpressionEvaluationEventImpl evaluationResult = new DmnDecisionLiteralExpressionEvaluationEventImpl();
    evaluationResult.setDecision(decision);
    evaluationResult.setExecutedDecisionElements(1);

    DmnDecisionLiteralExpressionImpl dmnDecisionLiteralExpression = (DmnDecisionLiteralExpressionImpl) decision.getDecisionLogic();
    DmnVariableImpl variable = dmnDecisionLiteralExpression.getVariable();
    DmnExpressionImpl expression = dmnDecisionLiteralExpression.getExpression();

    Object evaluateExpression = evaluateLiteralExpression(expression, variableContext);
    TypedValue typedValue = variable.getTypeDefinition().transform(evaluateExpression);

    evaluationResult.setOutputValue(typedValue);
    evaluationResult.setOutputName(variable.getName());

    return evaluationResult;
  }

  protected Object evaluateLiteralExpression(DmnExpressionImpl expression, VariableContext variableContext) {
    String expressionLanguage = expression.getExpressionLanguage();
    if (expressionLanguage == null) {
      expressionLanguage = literalExpressionLanguage;
    }
    return expressionEvaluationHandler.evaluateExpression(expressionLanguage, expression, variableContext);
  }

  @Override
  public DmnDecisionResult generateDecisionResult(DmnDecisionLogicEvaluationEvent event) {
    DmnDecisionLiteralExpressionEvaluationEvent evaluationEvent = (DmnDecisionLiteralExpressionEvaluationEvent) event;

    DmnDecisionResultEntriesImpl result = new DmnDecisionResultEntriesImpl();
    result.putValue(evaluationEvent.getOutputName(), evaluationEvent.getOutputValue());

    return new DmnDecisionResultImpl(Collections.<DmnDecisionResultEntries> singletonList(result));
  }

}
