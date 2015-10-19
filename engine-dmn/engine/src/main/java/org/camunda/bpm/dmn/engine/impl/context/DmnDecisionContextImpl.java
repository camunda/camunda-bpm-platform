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

package org.camunda.bpm.dmn.engine.impl.context;

import static org.camunda.commons.utils.EnsureUtil.ensureNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.camunda.bpm.dmn.engine.DmnClause;
import org.camunda.bpm.dmn.engine.DmnClauseEntry;
import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.DmnDecisionTable;
import org.camunda.bpm.dmn.engine.DmnDecisionTableListener;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.DmnDecisionTableRule;
import org.camunda.bpm.dmn.engine.DmnDecisionTableValue;
import org.camunda.bpm.dmn.engine.DmnExpression;
import org.camunda.bpm.dmn.engine.DmnRule;
import org.camunda.bpm.dmn.engine.DmnScriptEngineResolver;
import org.camunda.bpm.dmn.engine.context.DmnDecisionContext;
import org.camunda.bpm.dmn.engine.hitpolicy.DmnHitPolicyHandler;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionOutputImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionResultImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableResultImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableRuleImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableValueImpl;
import org.camunda.bpm.dmn.engine.impl.DmnEngineConfigurationImpl;
import org.camunda.bpm.dmn.engine.impl.DmnEngineLogger;
import org.camunda.bpm.dmn.feel.FeelEngine;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.NumberValue;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.bpm.model.dmn.HitPolicy;
import org.camunda.commons.utils.StringUtil;

public class DmnDecisionContextImpl implements DmnDecisionContext {

  protected static final DmnEngineLogger LOG = DmnEngineLogger.ENGINE_LOGGER;

  protected static final String TYPED_INPUT_VALUE_POSTFIX = "_typed";

  protected DmnScriptEngineResolver scriptEngineResolver;
  protected FeelEngine feelEngine;
  protected Map<HitPolicy, DmnHitPolicyHandler> hitPolicyHandlers;
  protected List<DmnDecisionTableListener> decisionTableListeners = new ArrayList<DmnDecisionTableListener>();

  protected String defaultAllowedValueExpressionLanguage;
  protected String defaultInputEntryExpressionLanguage;
  protected String defaultInputExpressionExpressionLanguage;
  protected String defaultOutputEntryExpressionLanguage;

  public DmnScriptEngineResolver getScriptEngineResolver() {
    return scriptEngineResolver;
  }

  public void setScriptEngineResolver(DmnScriptEngineResolver scriptEngineResolver) {
    this.scriptEngineResolver = scriptEngineResolver;
  }

  public FeelEngine getFeelEngine() {
    return feelEngine;
  }

  public void setFeelEngine(FeelEngine feelEngine) {
    this.feelEngine = feelEngine;
  }

  public Map<HitPolicy, DmnHitPolicyHandler> getHitPolicyHandlers() {
    return hitPolicyHandlers;
  }

  public DmnHitPolicyHandler getHitPolicyHandler(HitPolicy hitPolicy) {
    if (hitPolicyHandlers == null) {
      return null;
    } else {
      return hitPolicyHandlers.get(hitPolicy);
    }
  }

  public DmnHitPolicyHandler getHitPolicyHandlerChecked(HitPolicy hitPolicy) {
    DmnHitPolicyHandler hitPolicyHandler = getHitPolicyHandler(hitPolicy);
    if (hitPolicyHandler != null) {
      return hitPolicyHandler;
    } else {
      throw LOG.unableToFindHitPolicyHandlerFor(hitPolicy);
    }
  }

  public void setHitPolicyHandlers(Map<HitPolicy, DmnHitPolicyHandler> hitPolicyHandlers) {
    this.hitPolicyHandlers = hitPolicyHandlers;
  }

  public List<DmnDecisionTableListener> getDecisionTableListeners() {
    return decisionTableListeners;
  }

  public void setDecisionTableListeners(List<DmnDecisionTableListener> decisionTableListeners) {
    this.decisionTableListeners = decisionTableListeners;
  }

  public String getDefaultAllowedValueExpressionLanguage() {
    return defaultAllowedValueExpressionLanguage;
  }

  public void setDefaultAllowedValueExpressionLanguage(String defaultAllowedValueExpressionLanguage) {
    this.defaultAllowedValueExpressionLanguage = defaultAllowedValueExpressionLanguage;
  }

  public String getDefaultInputEntryExpressionLanguage() {
    return defaultInputEntryExpressionLanguage;
  }

  public void setDefaultInputEntryExpressionLanguage(String defaultInputEntryExpressionLanguage) {
    this.defaultInputEntryExpressionLanguage = defaultInputEntryExpressionLanguage;
  }

  public String getDefaultInputExpressionExpressionLanguage() {
    return defaultInputExpressionExpressionLanguage;
  }

  public void setDefaultInputExpressionExpressionLanguage(String defaultInputExpressionExpressionLanguage) {
    this.defaultInputExpressionExpressionLanguage = defaultInputExpressionExpressionLanguage;
  }

  public String getDefaultOutputEntryExpressionLanguage() {
    return defaultOutputEntryExpressionLanguage;
  }

  public void setDefaultOutputEntryExpressionLanguage(String defaultOutputEntryExpressionLanguage) {
    this.defaultOutputEntryExpressionLanguage = defaultOutputEntryExpressionLanguage;
  }

  public DmnDecisionResult evaluateDecision(DmnDecision decision, Map<String, Object> variables) {
    if (decision instanceof DmnDecisionTable) {
      return evaluateDecisionTable((DmnDecisionTable) decision, variables);
    } else {
      throw LOG.decisionTypeNotSupported(decision);
    }
  }

  protected DmnDecisionResult evaluateDecisionTable(DmnDecisionTable decisionTable, Map<String, Object> variables) {
    Map<String, Object> evaluationCache = new HashMap<String, Object>();

    DmnDecisionTableResultImpl decisionTableResult = new DmnDecisionTableResultImpl();
    decisionTableResult.setExecutedDecisionElements(calculateExecutedDecisionElements(decisionTable));

    // evaluate inputs
    Map<String, DmnDecisionTableValue> inputs = evaluateDecisionTableInputs(decisionTable, variables, evaluationCache);
    decisionTableResult.setInputs(inputs);

    // evaluate rules
    List<DmnDecisionTableRule> matchingRules = decisionTableResult.getMatchingRules();
    for (DmnRule rule : decisionTable.getRules()) {
      if (isRuleApplicable(rule, variables, inputs, evaluationCache)) {
        DmnDecisionTableRuleImpl matchingRule = evaluateMatchingRule(rule, variables, evaluationCache);
        matchingRules.add(matchingRule);
      }
    }

    // generate result
    return generateDecisionTableResult(decisionTable, decisionTableResult);
  }

  protected DmnDecisionResult generateDecisionTableResult(DmnDecisionTable decisionTable, DmnDecisionTableResult decisionTableResult) {
    // call hit policy handler
    DmnHitPolicyHandler hitPolicyHandler = getHitPolicyHandlerChecked(decisionTable.getHitPolicy());
    decisionTableResult = hitPolicyHandler.apply(decisionTable, decisionTableResult);

    // notify listeners
    for (DmnDecisionTableListener decisionTableListener : decisionTableListeners) {
      decisionTableListener.notify(decisionTable, decisionTableResult);
    }

    // generate output
    return generateDecisionResult(decisionTableResult);
  }

  protected DmnDecisionResult generateDecisionResult(DmnDecisionTableResult decisionTableResult) {
    DmnDecisionResultImpl decisionResult = new DmnDecisionResultImpl();
    if (decisionTableResult.getCollectResultName() != null || decisionTableResult.getCollectResultValue() != null) {
      DmnDecisionOutputImpl decisionOutput = new DmnDecisionOutputImpl();
      NumberValue resultValue = Variables.numberValue(decisionTableResult.getCollectResultValue());
      decisionOutput.put(decisionTableResult.getCollectResultName(), resultValue.getValue());
      decisionResult.add(decisionOutput);
    } else {
      for (DmnDecisionTableRule matchingRule : decisionTableResult.getMatchingRules()) {
        DmnDecisionOutputImpl decisionOutput = new DmnDecisionOutputImpl();
        for (DmnDecisionTableValue outputValue : matchingRule.getOutputs().values()) {
          decisionOutput.put(outputValue.getOutputName(), outputValue.getValue().getValue());
        }
        decisionResult.add(decisionOutput);
      }
    }
    return decisionResult;
  }

  protected long calculateExecutedDecisionElements(DmnDecisionTable decisionTable) {
    return decisionTable.getClauses().size() * decisionTable.getRules().size();
  }

  protected Map<String, DmnDecisionTableValue> evaluateDecisionTableInputs(DmnDecisionTable decisionTable, Map<String, Object> variables,
      Map<String, Object> evaluationCache) {
    Map<String, DmnDecisionTableValue> inputs = new HashMap<String, DmnDecisionTableValue>();
    for (DmnClause clause : decisionTable.getClauses()) {
      if (clause.isInputClause() && isNonEmptyExpression(clause.getInputExpression())) {
        DmnDecisionTableValue input = evaluateInputClause(clause, variables, evaluationCache);
        inputs.put(input.getKey(), input);
      }
    }
    return inputs;
  }

  protected DmnDecisionTableValue evaluateInputClause(DmnClause clause, Map<String, Object> variables, Map<String, Object> evaluationCache) {
    DmnDecisionTableValueImpl input = new DmnDecisionTableValueImpl(clause);
    DmnExpression inputExpression = clause.getInputExpression();

    if (inputExpression != null) {
      Object value = evaluateInputExpression(inputExpression, variables, evaluationCache);
      TypedValue typedValue = inputExpression.getItemDefinition().getTypeDefinition().transform(value);
      input.setValue(typedValue);

    } else {
      input.setValue(Variables.untypedNullValue());
    }
    return input;
  }

  protected boolean isRuleApplicable(DmnRule rule, Map<String, Object> variables, Map<String, DmnDecisionTableValue> inputs,
      Map<String, Object> evaluationCache) {
    Map<String, Boolean> clauseSatisfied = new HashMap<String, Boolean>();
    List<DmnClauseEntry> conditions = rule.getConditions();

    for (DmnClauseEntry condition : conditions) {
      String clauseKey = condition.getClause().getKey();
      Boolean alreadySatisfied = clauseSatisfied.get(clauseKey);
      if (alreadySatisfied != null && alreadySatisfied) {
        // skip condition if clause already satisfied
        continue;
      }

      // set temporary evaluation variable cache
      Map<String, Object> localVariables = new HashMap<String, Object>(variables);

      // set input clause variable
      if (inputs.containsKey(clauseKey)) {
        DmnDecisionTableValue inputValue = inputs.get(clauseKey);
        TypedValue typedValue = inputValue.getValue();
        // set variable as typed and as unboxed value
        localVariables.put(inputValue.getOutputName(), typedValue.getValue());
        localVariables.put(inputValue.getOutputName() + TYPED_INPUT_VALUE_POSTFIX, typedValue);
      }

      boolean applicable = isConditionApplicable(condition, localVariables, evaluationCache);
      clauseSatisfied.put(clauseKey, applicable);
    }

    // the rule is applicable if all involved clauses are satisfied
    return !clauseSatisfied.containsValue(false);
  }

  protected DmnDecisionTableRuleImpl evaluateMatchingRule(DmnRule rule, Map<String, Object> variables, Map<String, Object> evaluationCache) {
    DmnDecisionTableRuleImpl ruleResult = new DmnDecisionTableRuleImpl();
    ruleResult.setKey(rule.getKey());
    evaluateRuleOutput(ruleResult, rule, variables, evaluationCache);
    return ruleResult;
  }

  protected void evaluateRuleOutput(DmnDecisionTableRuleImpl ruleResult, DmnRule rule, Map<String, Object> variables, Map<String, Object> evaluationCache) {
    for (DmnClauseEntry conclusion : rule.getConclusions()) {
      if (isNonEmptyExpression(conclusion)) {
        DmnDecisionTableValueImpl output = new DmnDecisionTableValueImpl(conclusion.getClause());
        Object value = evaluateOutputEntry(conclusion, variables, evaluationCache);
        TypedValue typedValue = conclusion.getClause().getOutputDefinition().getTypeDefinition().transform(value);

        output.setValue(typedValue);
        ruleResult.addOutput(output);
      }
    }
  }

  protected boolean isConditionApplicable(DmnClauseEntry condition, Map<String, Object> variables, Map<String, Object> evaluationCache) {
    Object result = evaluateInputEntry(condition, variables, evaluationCache);
    return result != null && result.equals(true);
  }

  protected Object evaluateInputExpression(DmnExpression inputExpression, Map<String, Object> variables, Map<String, Object> evaluationCache) {
    ensureNotNull("evaluationCache", evaluationCache);
    String expressionKey = inputExpression.getKey();
    if (evaluationCache.containsKey(expressionKey)) {
      return evaluationCache.get(expressionKey);
    } else {
      Object value = evaluateInputExpression(inputExpression, variables);
      evaluationCache.put(expressionKey, value);
      return value;
    }
  }

  protected Object evaluateInputExpression(DmnExpression inputExpression, Map<String, Object> variables) {
    String expressionLanguage = inputExpression.getExpressionLanguage();
    if (expressionLanguage == null) {
      expressionLanguage = getDefaultInputExpressionExpressionLanguage();
    }
    if (isFeelExpressionLanguage(expressionLanguage)) {
      return evaluateFeelSimpleExpression(inputExpression, variables);
    }
    else {
      return evaluateExpression(expressionLanguage, inputExpression, variables);
    }
  }

  private Object evaluateInputEntry(DmnClauseEntry inputEntry, Map<String, Object> variables, Map<String, Object> evaluationCache) {
    ensureNotNull("evaluationCache", evaluationCache);
    String expressionKey = inputEntry.getKey();
    if (evaluationCache.containsKey(expressionKey)) {
      return evaluationCache.get(expressionKey);
    } else {
      Object value = evaluateInputEntry(inputEntry, variables);
      evaluationCache.put(expressionKey, value);
      return value;
    }
  }

  protected Object evaluateInputEntry(DmnClauseEntry inputEntry, Map<String, Object> variables) {
    if (isNonEmptyExpression(inputEntry)) {
      String expressionLanguage = inputEntry.getExpressionLanguage();
      if (expressionLanguage == null) {
        expressionLanguage = getDefaultInputEntryExpressionLanguage();
      }
      if (isFeelExpressionLanguage(expressionLanguage)) {
        return evaluateFeelSimpleUnaryTests(inputEntry, variables);
      } else {
        return evaluateExpression(expressionLanguage, inputEntry, variables);
      }
    }
    else {
      return true; // input entries without expressions are true
    }
  }

  protected Object evaluateOutputEntry(DmnClauseEntry outputEntry, Map<String, Object> variables, Map<String, Object> evaluationCache) {
    ensureNotNull("evaluationCache", evaluationCache);
    String expressionKey = outputEntry.getKey();
    if (evaluationCache.containsKey(expressionKey)) {
      return evaluationCache.get(expressionKey);
    } else {
      Object value = evaluateOutputEntry(outputEntry, variables);
      evaluationCache.put(expressionKey, value);
      return value;
    }
  }

  protected Object evaluateOutputEntry(DmnClauseEntry outputEntry, Map<String, Object> variables) {
    String expressionLanguage = outputEntry.getExpressionLanguage();
    if (expressionLanguage == null) {
      expressionLanguage = getDefaultOutputEntryExpressionLanguage();
    }
    if (isFeelExpressionLanguage(expressionLanguage)) {
      return evaluateFeelSimpleExpression(outputEntry, variables);
    }
    else {
      return evaluateExpression(expressionLanguage, outputEntry, variables);
    }
  }

  protected Object evaluateFeelSimpleExpression(DmnExpression feelExpression, Map<String, Object> variables) {
    String feelSimpleExpression = feelExpression.getExpression();
    if (feelSimpleExpression != null) {
      return feelEngine.evaluateSimpleExpression(feelSimpleExpression, variables);
    }
    else {
      return null;
    }
  }

  protected Object evaluateFeelSimpleUnaryTests(DmnClauseEntry feelExpression, Map<String, Object> variables) {
    String feelSimpleUnaryTests = feelExpression.getExpression();
    if (feelSimpleUnaryTests != null) {
      String inputVariableName = feelExpression.getClause().getOutputName() + TYPED_INPUT_VALUE_POSTFIX;
      return feelEngine.evaluateSimpleUnaryTests(feelSimpleUnaryTests, inputVariableName, variables);
    }
    else {
      return null;
    }
  }

  protected Object evaluateExpression(String expressionLanguage, DmnExpression expression, Map<String, Object> variables) {
    String expressionText = getExpressionTextForLanguage(expression, expressionLanguage);
    if (expressionText != null) {
      ScriptEngine scriptEngine = getScriptEngineForName(expressionLanguage);
      Bindings bindings = scriptEngine.createBindings();
      bindings.putAll(variables);

      try {
        return scriptEngine.eval(expressionText, bindings);
      } catch (ScriptException e) {
        throw LOG.unableToEvaluateExpression(expressionText, scriptEngine.getFactory().getLanguageName(), e);
      }
    } else {
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

  protected String getExpressionTextForLanguage(DmnExpression expression, String expressionLanguage) {
    String expressionText = expression.getExpression();
    if (expressionText != null) {
      if (DmnEngineConfigurationImpl.JUEL_EXPRESSION_LANGUAGE.equals(expressionLanguage) && !StringUtil.isExpression(expressionText)) {
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

  protected boolean isNonEmptyExpression(DmnExpression expression) {
    return expression != null && expression.getExpression() != null && !expression.getExpression().trim().isEmpty();
  }

  protected boolean isFeelExpressionLanguage(String expressionLanguage) {
    ensureNotNull("expressionLanguage", expressionLanguage);
    return expressionLanguage.equals(DmnEngineConfigurationImpl.FEEL_EXPRESSION_LANGUAGE) ||
      expressionLanguage.toLowerCase().equals(DmnEngineConfigurationImpl.FEEL_EXPRESSION_LANGUAGE_ALTERNATIVE);
  }

}
