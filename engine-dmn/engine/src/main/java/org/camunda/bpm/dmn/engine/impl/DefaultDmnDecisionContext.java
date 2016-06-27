/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.dmn.engine.impl;

import static org.camunda.commons.utils.EnsureUtil.ensureNotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionRuleResult;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.delegate.DmnDecisionEvaluationListener;
import org.camunda.bpm.dmn.engine.delegate.DmnDecisionTableEvaluationEvent;
import org.camunda.bpm.dmn.engine.delegate.DmnDecisionTableEvaluationListener;
import org.camunda.bpm.dmn.engine.delegate.DmnEvaluatedDecisionRule;
import org.camunda.bpm.dmn.engine.delegate.DmnEvaluatedInput;
import org.camunda.bpm.dmn.engine.delegate.DmnEvaluatedOutput;
import org.camunda.bpm.dmn.engine.impl.delegate.DmnDecisionEvaluationEventImpl;
import org.camunda.bpm.dmn.engine.impl.delegate.DmnDecisionTableEvaluationEventImpl;
import org.camunda.bpm.dmn.engine.impl.delegate.DmnEvaluatedDecisionRuleImpl;
import org.camunda.bpm.dmn.engine.impl.delegate.DmnEvaluatedInputImpl;
import org.camunda.bpm.dmn.engine.impl.delegate.DmnEvaluatedOutputImpl;
import org.camunda.bpm.dmn.engine.impl.el.VariableContextScriptBindings;
import org.camunda.bpm.dmn.engine.impl.spi.el.DmnScriptEngineResolver;
import org.camunda.bpm.dmn.engine.impl.spi.el.ElExpression;
import org.camunda.bpm.dmn.engine.impl.spi.el.ElProvider;
import org.camunda.bpm.dmn.feel.impl.FeelEngine;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.context.VariableContext;
import org.camunda.bpm.engine.variable.impl.context.CompositeVariableContext;
import org.camunda.bpm.engine.variable.impl.context.SingleVariableContext;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.commons.utils.StringUtil;

/**
 * Context which evaluates a decision on a given input
 */
public class DefaultDmnDecisionContext {

  protected static final DmnEngineLogger LOG = DmnEngineLogger.ENGINE_LOGGER;

  protected final List<DmnDecisionTableEvaluationListener> evaluationListeners;
  protected final List<DmnDecisionEvaluationListener> decisionEvaluationListeners;
  protected final DmnScriptEngineResolver scriptEngineResolver;
  protected final ElProvider elProvider;
  protected final FeelEngine feelEngine;
  protected final String inputExpressionExpressionLanguage;
  protected final String inputEntryExpressionLanguage;
  protected final String outputEntryExpressionLanguage;
  
  public DefaultDmnDecisionContext(DefaultDmnEngineConfiguration configuration) {
    evaluationListeners = configuration.getDecisionTableEvaluationListeners();
    decisionEvaluationListeners = configuration.getDecisionEvaluationListeners();
    scriptEngineResolver = configuration.getScriptEngineResolver();
    elProvider = configuration.getElProvider();
    feelEngine = configuration.getFeelEngine();

    inputExpressionExpressionLanguage = configuration.getDefaultInputExpressionExpressionLanguage();
    inputEntryExpressionLanguage = configuration.getDefaultInputEntryExpressionLanguage();
    outputEntryExpressionLanguage = configuration.getDefaultOutputEntryExpressionLanguage();
  }

  /**
   * Evaluate a decision with the given {@link VariableContext}
   *
   * @param decision the decision to evaluate
   * @param variableContext the available variable context
   * @return the result of the decision evaluation
   */

  public DmnDecisionTableResult evaluateDecision(DmnDecision decision, VariableContext variableContext) {

    if(decision.getKey() == null 
      || ((DmnDecisionImpl)decision).getRelatedDecisionTable() == null) {
      throw LOG.unableToFindAnyDecisionTable();
    }
    long executedDecisions = 0L;
    VariableMap variableMap = buildVariableMapFromVariableContext(variableContext);
    
    Map<String, DmnDecision> requiredDecisions = new LinkedHashMap<String, DmnDecision>();
    buildDecisionTree(decision, requiredDecisions);
    
    List<DmnDecisionTableEvaluationEvent> evaluatedEvents = new ArrayList<DmnDecisionTableEvaluationEvent>();
    DmnDecisionTableEvaluationEvent rootEvaluatedEvent = null;
    DmnDecisionTableResult evaluatedResult = null;
    
    for (Map.Entry<String, DmnDecision> entry:requiredDecisions.entrySet()) {
      DmnDecision evaluateDecision = entry.getValue();
      DmnDecisionTableEvaluationEventImpl evaluatedEvent = evaluateDecisionTable(evaluateDecision, variableMap.asVariableContext());
      
      if(decision == evaluateDecision) {
        rootEvaluatedEvent = evaluatedEvent;
      } else {
        evaluatedEvents.add(evaluatedEvent);  
      }
      executedDecisions += evaluatedEvent.getExecutedDecisionElements();
      evaluatedResult = generateDecisionTableResult(((DmnDecisionImpl)decision).getRelatedDecisionTable(), evaluatedEvent); 
      if(decision != evaluateDecision) {
        addResultToVariableContext(evaluatedResult, variableMap);
      }
    }

    generateDecisionEvaluationEvent(rootEvaluatedEvent, evaluatedEvents, executedDecisions);
    return evaluatedResult;  
   
  }
  
  /**
   * Evaluate a decision table with the given {@link VariableContext}
   *
   * @param decisionTable the decision table to evaluate
   * @param variableContext the available variable context
   * @return the result of the decision evaluation
   */
  public DmnDecisionTableEvaluationEventImpl evaluateDecisionTable(DmnDecision decision, VariableContext variableContext) {
    DmnDecisionTableEvaluationEventImpl evaluationResult = new DmnDecisionTableEvaluationEventImpl();
    evaluationResult.setDecisionTable(decision);
    DmnDecisionTableImpl decisionTable = ((DmnDecisionImpl)decision).getRelatedDecisionTable();
    evaluationResult.setExecutedDecisionElements(calculateExecutedDecisionElements(decisionTable));

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
    return evaluationResult;
  }

  protected void generateDecisionEvaluationEvent(DmnDecisionTableEvaluationEvent rootEvaluatedResult, List<DmnDecisionTableEvaluationEvent> evaluatedEvents, long requiredDecisions) {
    DmnDecisionEvaluationEventImpl decisionEvaluationEvent = new DmnDecisionEvaluationEventImpl();
    decisionEvaluationEvent.setDecisionResult(rootEvaluatedResult);
    decisionEvaluationEvent.setEvaluatedDecisions(requiredDecisions);
    decisionEvaluationEvent.setRequiredDecisions(evaluatedEvents);

    for (DmnDecisionEvaluationListener evaluationListener : decisionEvaluationListeners) {
      evaluationListener.notify(decisionEvaluationEvent);
    }
  }

  protected void addResultToVariableContext(DmnDecisionTableResult evaluatedResult, VariableMap variableMap)
  {
    List<Map<String, Object>> resultList = evaluatedResult.getResultList();

    if(resultList.isEmpty()) {
      return;
    } else if(resultList.size() == 1) {
      variableMap.putAll(evaluatedResult.getSingleResult());
    } else {
      Set<String> outputs = new HashSet<String>();
    
      for (Map<String, Object> resultMap : resultList) {
        outputs.addAll(resultMap.keySet());
      }
      
      for (String output : outputs) {
        List<Object> values = evaluatedResult.collectEntries(output);
        variableMap.put(output, values);
      }
    }
  }

  protected VariableMap buildVariableMapFromVariableContext(VariableContext variableContext) {
    
    VariableMap variableMap = Variables.createVariables();

    Set<String> variables = variableContext.keySet();
    for(String variable: variables) {
      variableMap.put(variable, variableContext.resolve(variable));
    }
    
    return variableMap;
  }

  protected void buildDecisionTree(DmnDecision decision, Map<String, DmnDecision> requiredDecisions) {
    for(DmnDecision dmnDecision : decision.getRequiredDecisions()){
      buildDecisionTree(dmnDecision, requiredDecisions);
    }
    requiredDecisions.put(decision.getKey(),decision);
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

  private VariableContext getLocalVariableContext(DmnDecisionTableInputImpl input, DmnEvaluatedInput evaluatedInput, VariableContext variableContext) {
    if (isNonEmptyExpression(input.getExpression())) {
      return CompositeVariableContext.compose(
        SingleVariableContext.singleVariable(evaluatedInput.getInputVariable(), evaluatedInput.getValue()),
        variableContext
      );
    }
    else {
      return variableContext;
    }
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

  protected DmnDecisionTableResult generateDecisionTableResult(DmnDecisionTableImpl decisionTable, DmnDecisionTableEvaluationEventImpl evaluationResult) {
    // apply hit policy
    DmnDecisionTableEvaluationEvent evaluationEvent = decisionTable.getHitPolicyHandler().apply(evaluationResult);

    // notify listeners
    for (DmnDecisionTableEvaluationListener evaluationListener : evaluationListeners) {
      evaluationListener.notify(evaluationEvent);
    }

    return generateDecisionTableResult(evaluationEvent);
  }

  protected DmnDecisionTableResult generateDecisionTableResult(DmnDecisionTableEvaluationEvent evaluationResult) {
    List<DmnDecisionRuleResult> ruleResults = new ArrayList<DmnDecisionRuleResult>();

    if (evaluationResult.getCollectResultName() != null || evaluationResult.getCollectResultValue() != null) {
      DmnDecisionRuleResultImpl ruleResult = new DmnDecisionRuleResultImpl();
      ruleResult.putValue(evaluationResult.getCollectResultName(), evaluationResult.getCollectResultValue());
      ruleResults.add(ruleResult);
    }
    else {
      for (DmnEvaluatedDecisionRule evaluatedRule : evaluationResult.getMatchingRules()) {
        DmnDecisionRuleResultImpl ruleResult = new DmnDecisionRuleResultImpl();
        for (DmnEvaluatedOutput evaluatedOutput : evaluatedRule.getOutputEntries().values()) {
          ruleResult.putValue(evaluatedOutput.getOutputName(), evaluatedOutput.getValue());
        }
        ruleResults.add(ruleResult);
      }
    }

    return new DmnDecisionTableResultImpl(ruleResults);
  }

  protected long calculateExecutedDecisionElements(DmnDecisionTableImpl decisionTable) {
    return (decisionTable.getInputs().size() + decisionTable.getOutputs().size()) * decisionTable.getRules().size();
  }

  protected Object evaluateInputExpression(DmnExpressionImpl expression, VariableContext variableContext) {
    String expressionLanguage = expression.getExpressionLanguage();
    if (expressionLanguage == null) {
      expressionLanguage = inputExpressionExpressionLanguage;
    }

    if (isFeelExpressionLanguage(expressionLanguage)) {
      return evaluateFeelSimpleExpression(expression, variableContext);
    }
    else {
      return evaluateExpression(expressionLanguage, expression, variableContext);
    }
  }

  private Object evaluateInputEntry(DmnDecisionTableInputImpl input, DmnExpressionImpl condition, VariableContext variableContext) {
    if (isNonEmptyExpression(condition)) {
      String expressionLanguage = condition.getExpressionLanguage();
      if (expressionLanguage == null) {
        expressionLanguage = inputEntryExpressionLanguage;
      }
      if (isFeelExpressionLanguage(expressionLanguage)) {
        return evaluateFeelSimpleUnaryTests(input, condition, variableContext);
      } else {
        return evaluateExpression(expressionLanguage, condition, variableContext);
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
    if (isFeelExpressionLanguage(expressionLanguage)) {
      return evaluateFeelSimpleExpression(conclusion, variableContext);
    }
    else {
      return evaluateExpression(expressionLanguage, conclusion, variableContext);
    }
  }

  protected TypedValue evaluateFeelSimpleExpression(DmnExpressionImpl expression, VariableContext variableContext) {
    String expressionText = expression.getExpression();
    if (expressionText != null) {
      return feelEngine.evaluateSimpleExpression(expressionText, variableContext);
    }
    else {
      return null;
    }
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

  protected Object evaluateExpression(String expressionLanguage, DmnExpressionImpl expression, VariableContext variableContext) {
    String expressionText = getExpressionTextForLanguage(expression, expressionLanguage);
    if (expressionText != null) {
      if(isElExpression(expressionLanguage)) {
        return evaluateElExpression(expressionLanguage, expressionText, variableContext);
      }
      else {
        return evaluateScriptExpression(expressionLanguage, variableContext, expressionText);
      }
    } else {
      return null;
    }
  }

  private Object evaluateScriptExpression(String expressionLanguage, VariableContext variableContext, String expressionText) {
    ScriptEngine scriptEngine = getScriptEngineForName(expressionLanguage);
    // wrap script engine bindings + variable context and pass enhanced
    // bindings to the script engine.
    Bindings bindings = VariableContextScriptBindings.wrap(scriptEngine.createBindings(), variableContext);
    bindings.put("variableContext", variableContext);

    try {
      return scriptEngine.eval(expressionText, bindings);
    }
    catch (ScriptException e) {
      throw LOG.unableToEvaluateExpression(expressionText, scriptEngine.getFactory().getLanguageName(), e);
    }
  }

  private Object evaluateElExpression(String expressionLanguage, String expressionText, VariableContext variableContext) {
    ElExpression elExpression = elProvider.createExpression(expressionText);
    try {
      return elExpression.getValue(variableContext);
    }
    // yes, we catch all exceptions
    catch(Exception e) {
      throw LOG.unableToEvaluateExpression(expressionText, expressionLanguage, e);
    }
  }

  // helper ///////////////////////////////////////////////////////////////////

  protected String getExpressionTextForLanguage(DmnExpressionImpl expression, String expressionLanguage) {
    String expressionText = expression.getExpression();
    if (expressionText != null) {
      if (DefaultDmnEngineConfiguration.JUEL_EXPRESSION_LANGUAGE.equals(expressionLanguage) && !StringUtil.isExpression(expressionText)) {
        return "${" + expressionText + "}";
      }
      else {
        return expressionText;
      }
    }
    else {
      return null;
    }
  }

  protected ScriptEngine getScriptEngineForName(String expressionLanguage) {
    ensureNotNull("expressionLanguage", expressionLanguage);
    ScriptEngine scriptEngine = scriptEngineResolver.getScriptEngineForLanguage(expressionLanguage);
    if (scriptEngine != null) {
      return scriptEngine;
    }
    else {
      throw LOG.noScriptEngineFoundForLanguage(expressionLanguage);
    }
  }

  protected boolean isElExpression(String expressionLanguage) {
    return DefaultDmnEngineConfiguration.JUEL_EXPRESSION_LANGUAGE.equals(expressionLanguage);
  }

  protected boolean isNonEmptyExpression(DmnExpressionImpl expression) {
    return expression != null && expression.getExpression() != null && !expression.getExpression().trim().isEmpty();
  }

  protected boolean isFeelExpressionLanguage(String expressionLanguage) {
    ensureNotNull("expressionLanguage", expressionLanguage);
    return expressionLanguage.equals(DefaultDmnEngineConfiguration.FEEL_EXPRESSION_LANGUAGE) ||
      expressionLanguage.toLowerCase().equals(DefaultDmnEngineConfiguration.FEEL_EXPRESSION_LANGUAGE_ALTERNATIVE);
  }

}
