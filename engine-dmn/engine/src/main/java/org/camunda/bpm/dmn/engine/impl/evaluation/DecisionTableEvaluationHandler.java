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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.DmnDecisionResultEntries;
import org.camunda.bpm.dmn.engine.delegate.DmnDecisionLogicEvaluationEvent;
import org.camunda.bpm.dmn.engine.delegate.DmnDecisionTableEvaluationEvent;
import org.camunda.bpm.dmn.engine.delegate.DmnDecisionTableEvaluationListener;
import org.camunda.bpm.dmn.engine.delegate.DmnEvaluatedDecisionRule;
import org.camunda.bpm.dmn.engine.delegate.DmnEvaluatedInput;
import org.camunda.bpm.dmn.engine.delegate.DmnEvaluatedOutput;
import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionResultEntriesImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionResultImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableInputImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableOutputImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableRuleImpl;
import org.camunda.bpm.dmn.engine.impl.DmnExpressionImpl;
import org.camunda.bpm.dmn.engine.impl.delegate.DmnDecisionTableEvaluationEventImpl;
import org.camunda.bpm.dmn.engine.impl.delegate.DmnEvaluatedDecisionRuleImpl;
import org.camunda.bpm.dmn.engine.impl.delegate.DmnEvaluatedInputImpl;
import org.camunda.bpm.dmn.engine.impl.delegate.DmnEvaluatedOutputImpl;
import org.camunda.bpm.dmn.feel.impl.FeelEngine;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.context.VariableContext;
import org.camunda.bpm.engine.variable.impl.context.CompositeVariableContext;
import org.camunda.bpm.engine.variable.value.TypedValue;

public class DecisionTableEvaluationHandler implements DmnDecisionLogicEvaluationHandler {

  protected final ExpressionEvaluationHandler expressionEvaluationHandler;
  protected final FeelEngine feelEngine;

  protected final List<DmnDecisionTableEvaluationListener> evaluationListeners;

  protected final String inputExpressionExpressionLanguage;
  protected final String inputEntryExpressionLanguage;
  protected final String outputEntryExpressionLanguage;

  public DecisionTableEvaluationHandler(DefaultDmnEngineConfiguration configuration) {
    expressionEvaluationHandler = new ExpressionEvaluationHandler(configuration);
    feelEngine = configuration.getFeelEngine();

    evaluationListeners = configuration.getDecisionTableEvaluationListeners();

    inputExpressionExpressionLanguage = configuration.getDefaultInputExpressionExpressionLanguage();
    inputEntryExpressionLanguage = configuration.getDefaultInputEntryExpressionLanguage();
    outputEntryExpressionLanguage = configuration.getDefaultOutputEntryExpressionLanguage();
  }

  @Override
  public DmnDecisionLogicEvaluationEvent evaluate(DmnDecision decision, VariableContext variableContext) {
    DmnDecisionTableEvaluationEventImpl evaluationResult = new DmnDecisionTableEvaluationEventImpl();
    evaluationResult.setDecisionTable(decision);

    DmnDecisionTableImpl decisionTable = (DmnDecisionTableImpl) decision.getDecisionLogic();
    evaluationResult.setExecutedDecisionElements(calculateExecutedDecisionElements(decisionTable));

    evaluateDecisionTable(decisionTable, variableContext, evaluationResult);

    // apply hit policy
    decisionTable.getHitPolicyHandler().apply(evaluationResult);

    // notify listeners
    for (DmnDecisionTableEvaluationListener evaluationListener : evaluationListeners) {
      evaluationListener.notify(evaluationResult);
    }

    return evaluationResult;
  }

  protected long calculateExecutedDecisionElements(DmnDecisionTableImpl decisionTable) {
    return (decisionTable.getInputs().size() + decisionTable.getOutputs().size()) * decisionTable.getRules().size();
  }

  protected void evaluateDecisionTable(DmnDecisionTableImpl decisionTable, VariableContext variableContext, DmnDecisionTableEvaluationEventImpl evaluationResult) {
    int inputSize = decisionTable.getInputs().size();
    List<DmnDecisionTableRuleImpl> matchingRules = new ArrayList<DmnDecisionTableRuleImpl>(decisionTable.getRules());
    for (int inputIdx = 0; inputIdx < inputSize; inputIdx++) {
      // evaluate input
      DmnDecisionTableInputImpl input = decisionTable.getInputs().get(inputIdx);
      DmnEvaluatedInput evaluatedInput = evaluateInput(input, variableContext);
      evaluationResult.getInputs().add(evaluatedInput);

      // compose local variable context out of global variable context enhanced with the value of the current input.
      VariableContext localVariableContext = getLocalVariableContext(input, evaluatedInput, variableContext);

      // filter rules applicable with this input
      matchingRules = evaluateInputForAvailableRules(inputIdx, input, matchingRules, localVariableContext);
    }

    setEvaluationOutput(decisionTable, matchingRules, variableContext, evaluationResult);
  }

  protected DmnEvaluatedInput evaluateInput(DmnDecisionTableInputImpl input, VariableContext variableContext) {
    DmnEvaluatedInputImpl evaluatedInput = new DmnEvaluatedInputImpl(input);

    DmnExpressionImpl expression = input.getExpression();
    if (expression != null) {
      Object value = evaluateInputExpression(expression, variableContext);
      TypedValue typedValue = expression.getTypeDefinition().transform(value);
      evaluatedInput.setValue(typedValue);
    }
    else {
      evaluatedInput.setValue(Variables.untypedNullValue());
    }

    return evaluatedInput;
  }

  protected List<DmnDecisionTableRuleImpl> evaluateInputForAvailableRules(int conditionIdx, DmnDecisionTableInputImpl input, List<DmnDecisionTableRuleImpl> availableRules, VariableContext variableContext) {
    List<DmnDecisionTableRuleImpl> matchingRules = new ArrayList<DmnDecisionTableRuleImpl>();
    for (DmnDecisionTableRuleImpl availableRule : availableRules) {
      DmnExpressionImpl condition = availableRule.getConditions().get(conditionIdx);
      if (isConditionApplicable(input, condition, variableContext)) {
        matchingRules.add(availableRule);
      }
    }
    return matchingRules;
  }

  protected boolean isConditionApplicable(DmnDecisionTableInputImpl input, DmnExpressionImpl condition, VariableContext variableContext) {
    Object result = evaluateInputEntry(input, condition, variableContext);
    return result != null && result.equals(true);
  }

  protected void setEvaluationOutput(DmnDecisionTableImpl decisionTable, List<DmnDecisionTableRuleImpl> matchingRules, VariableContext variableContext, DmnDecisionTableEvaluationEventImpl evaluationResult) {
    List<DmnDecisionTableOutputImpl> decisionTableOutputs = decisionTable.getOutputs();

    List<DmnEvaluatedDecisionRule> evaluatedDecisionRules = new ArrayList<DmnEvaluatedDecisionRule>();
    for (DmnDecisionTableRuleImpl matchingRule : matchingRules) {
      DmnEvaluatedDecisionRule evaluatedRule = evaluateMatchingRule(decisionTableOutputs, matchingRule, variableContext);
      evaluatedDecisionRules.add(evaluatedRule);
    }
    evaluationResult.setMatchingRules(evaluatedDecisionRules);
  }

  protected DmnEvaluatedDecisionRule evaluateMatchingRule(List<DmnDecisionTableOutputImpl> decisionTableOutputs, DmnDecisionTableRuleImpl matchingRule, VariableContext variableContext) {
    DmnEvaluatedDecisionRuleImpl evaluatedDecisionRule = new DmnEvaluatedDecisionRuleImpl(matchingRule);
    Map<String, DmnEvaluatedOutput> outputEntries = evaluateOutputEntries(decisionTableOutputs, matchingRule, variableContext);
    evaluatedDecisionRule.setOutputEntries(outputEntries);

    return evaluatedDecisionRule;
  }

  protected VariableContext getLocalVariableContext(DmnDecisionTableInputImpl input, DmnEvaluatedInput evaluatedInput, VariableContext variableContext) {
    if (isNonEmptyExpression(input.getExpression())) {
      String inputVariableName = evaluatedInput.getInputVariable();

      return CompositeVariableContext.compose(
        Variables.createVariables()
            .putValue("inputVariableName", inputVariableName)
            .putValueTyped(inputVariableName, evaluatedInput.getValue())
            .asVariableContext(),
        variableContext
      );
    } else {
      return variableContext;
    }
  }

  protected boolean isNonEmptyExpression(DmnExpressionImpl expression) {
    return expression != null && expression.getExpression() != null && !expression.getExpression().trim().isEmpty();
  }

  protected Object evaluateInputExpression(DmnExpressionImpl expression, VariableContext variableContext) {
    String expressionLanguage = expression.getExpressionLanguage();
    if (expressionLanguage == null) {
      expressionLanguage = inputExpressionExpressionLanguage;
    }
    return expressionEvaluationHandler.evaluateExpression(expressionLanguage, expression, variableContext);
  }

  protected Object evaluateInputEntry(DmnDecisionTableInputImpl input, DmnExpressionImpl condition, VariableContext variableContext) {
    if (isNonEmptyExpression(condition)) {
      String expressionLanguage = condition.getExpressionLanguage();
      if (expressionLanguage == null) {
        expressionLanguage = inputEntryExpressionLanguage;
      }
      if (expressionEvaluationHandler.isFeelExpressionLanguage(expressionLanguage)) {
        return evaluateFeelSimpleUnaryTests(input, condition, variableContext);
      } else {
        return expressionEvaluationHandler.evaluateExpression(expressionLanguage, condition, variableContext);
      }
    }
    else {
      return true; // input entries without expressions are true
    }
  }

  protected Map<String, DmnEvaluatedOutput> evaluateOutputEntries(List<DmnDecisionTableOutputImpl> decisionTableOutputs, DmnDecisionTableRuleImpl matchingRule, VariableContext variableContext) {
    Map<String, DmnEvaluatedOutput> outputEntries = new LinkedHashMap<String, DmnEvaluatedOutput>();

    for (int outputIdx = 0; outputIdx < decisionTableOutputs.size(); outputIdx++) {
      // evaluate output entry, skip empty expressions
      DmnExpressionImpl conclusion = matchingRule.getConclusions().get(outputIdx);
      if (isNonEmptyExpression(conclusion)) {
        Object value = evaluateOutputEntry(conclusion, variableContext);

        // transform to output type
        DmnDecisionTableOutputImpl decisionTableOutput = decisionTableOutputs.get(outputIdx);
        TypedValue typedValue = decisionTableOutput.getTypeDefinition().transform(value);

        // set on result
        DmnEvaluatedOutputImpl evaluatedOutput = new DmnEvaluatedOutputImpl(decisionTableOutput, typedValue);
        outputEntries.put(decisionTableOutput.getOutputName(), evaluatedOutput);
      }
    }

    return outputEntries;
  }

  protected Object evaluateOutputEntry(DmnExpressionImpl conclusion, VariableContext variableContext) {
    String expressionLanguage = conclusion.getExpressionLanguage();
    if (expressionLanguage == null) {
      expressionLanguage = outputEntryExpressionLanguage;
    }
    return expressionEvaluationHandler.evaluateExpression(expressionLanguage, conclusion, variableContext);
  }

  protected Object evaluateFeelSimpleUnaryTests(DmnDecisionTableInputImpl input, DmnExpressionImpl condition, VariableContext variableContext) {
    String expressionText = condition.getExpression();
    if (expressionText != null) {
      return feelEngine.evaluateSimpleUnaryTests(expressionText, input.getInputVariable(), variableContext);
    }
    else {
      return null;
    }
  }

  @Override
  public DmnDecisionResult generateDecisionResult(DmnDecisionLogicEvaluationEvent event) {
    DmnDecisionTableEvaluationEvent evaluationResult = (DmnDecisionTableEvaluationEvent) event;

    List<DmnDecisionResultEntries> ruleResults = new ArrayList<DmnDecisionResultEntries>();

    if (evaluationResult.getCollectResultName() != null || evaluationResult.getCollectResultValue() != null) {
      DmnDecisionResultEntriesImpl ruleResult = new DmnDecisionResultEntriesImpl();
      ruleResult.putValue(evaluationResult.getCollectResultName(), evaluationResult.getCollectResultValue());
      ruleResults.add(ruleResult);
    }
    else {
      for (DmnEvaluatedDecisionRule evaluatedRule : evaluationResult.getMatchingRules()) {
        DmnDecisionResultEntriesImpl ruleResult = new DmnDecisionResultEntriesImpl();
        for (DmnEvaluatedOutput evaluatedOutput : evaluatedRule.getOutputEntries().values()) {
          ruleResult.putValue(evaluatedOutput.getOutputName(), evaluatedOutput.getValue());
        }
        ruleResults.add(ruleResult);
      }
    }

    return new DmnDecisionResultImpl(ruleResults);
  }

}
