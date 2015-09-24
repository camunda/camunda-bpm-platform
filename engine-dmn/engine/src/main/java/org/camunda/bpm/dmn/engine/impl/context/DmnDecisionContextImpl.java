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
import org.camunda.bpm.dmn.engine.DmnItemDefinition;
import org.camunda.bpm.dmn.engine.DmnRule;
import org.camunda.bpm.dmn.engine.DmnScriptEngineResolver;
import org.camunda.bpm.dmn.engine.context.DmnDecisionContext;
import org.camunda.bpm.dmn.engine.hitpolicy.DmnHitPolicyHandler;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionOutputImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionResultImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableResultImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableRuleImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableValueImpl;
import org.camunda.bpm.dmn.engine.impl.DmnEngineLogger;
import org.camunda.bpm.dmn.juel.JuelScriptEngineFactory;
import org.camunda.bpm.model.dmn.HitPolicy;

public class DmnDecisionContextImpl implements DmnDecisionContext {

  protected static final DmnEngineLogger LOG = DmnEngineLogger.ENGINE_LOGGER;

  public static final String DEFAULT_SCRIPT_LANGUAGE = JuelScriptEngineFactory.NAME;

  protected DmnScriptEngineResolver scriptEngineResolver;
  protected Map<HitPolicy, DmnHitPolicyHandler> hitPolicyHandlers;
  protected List<DmnDecisionTableListener> decisionTableListeners = new ArrayList<DmnDecisionTableListener>();

  public DmnScriptEngineResolver getScriptEngineResolver() {
    return scriptEngineResolver;
  }

  public void setScriptEngineResolver(DmnScriptEngineResolver scriptEngineResolver) {
    this.scriptEngineResolver = scriptEngineResolver;
  }

  public Map<HitPolicy, DmnHitPolicyHandler> getHitPolicyHandlers() {
    return hitPolicyHandlers;
  }

  public DmnHitPolicyHandler getHitPolicyHandler(HitPolicy hitPolicy) {
    if (hitPolicyHandlers == null) {
      return null;
    }
    else {
      return hitPolicyHandlers.get(hitPolicy);
    }
  }

  public DmnHitPolicyHandler getHitPolicyHandlerChecked(HitPolicy hitPolicy) {
    DmnHitPolicyHandler hitPolicyHandler = getHitPolicyHandler(hitPolicy);
    if (hitPolicyHandler != null) {
      return hitPolicyHandler;
    }
    else {
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

  public DmnDecisionResult evaluateDecision(DmnDecision decision, Map<String, Object> variables) {
    if (decision instanceof DmnDecisionTable) {
      return evaluateDecisionTable((DmnDecisionTable) decision, variables);
    }
    else {
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
      decisionOutput.put(decisionTableResult.getCollectResultName(), decisionTableResult.getCollectResultValue());
      decisionResult.add(decisionOutput);
    }
    else {
      for (DmnDecisionTableRule matchingRule : decisionTableResult.getMatchingRules()) {
        DmnDecisionOutputImpl decisionOutput = new DmnDecisionOutputImpl();
        for (DmnDecisionTableValue outputValue : matchingRule.getOutputs().values()) {
          decisionOutput.put(outputValue.getOutputName(), outputValue.getValue());
        }
        decisionResult.add(decisionOutput);
      }
    }
    return decisionResult;
  }

  protected long calculateExecutedDecisionElements(DmnDecisionTable decisionTable) {
    return decisionTable.getClauses().size() * decisionTable.getRules().size();
  }

  protected Map<String, DmnDecisionTableValue> evaluateDecisionTableInputs(DmnDecisionTable decisionTable, Map<String, Object> variables, Map<String, Object> evaluationCache) {
    Map<String, DmnDecisionTableValue> inputs = new HashMap<String, DmnDecisionTableValue>();
    for (DmnClause clause : decisionTable.getClauses()) {
      if (clause.isInputClause()) {
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
      Object value = evaluateExpression(inputExpression, variables, evaluationCache);

      if (hasItemDefinitionWithTypeDefinition(inputExpression)) {
        value = inputExpression.getItemDefinition().getTypeDefinition().transform(value);
      }

      input.setValue(value);
    }
    return input;
  }

  protected boolean hasItemDefinitionWithTypeDefinition(DmnExpression expression) {
    DmnItemDefinition itemDefinition = expression.getItemDefinition();
    return itemDefinition != null && itemDefinition.getTypeDefinition() != null;
  }

  protected boolean isRuleApplicable(DmnRule rule, Map<String, Object> variables, Map<String, DmnDecisionTableValue> inputs, Map<String, Object> evaluationCache) {
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
        localVariables.put(inputValue.getOutputName(), inputValue.getValue());
      }

      boolean applicable = isExpressionApplicable(condition, localVariables, evaluationCache);
      clauseSatisfied.put(clauseKey, applicable);
    }

    // the rule is applicable if all involved clauses are satisfied
    return !clauseSatisfied.containsValue(false);
  }

  protected DmnDecisionTableRuleImpl evaluateMatchingRule(DmnRule rule, Map<String, Object> variables, Map<String, Object> evaluationCache) {
    DmnDecisionTableRuleImpl matchingRule = new DmnDecisionTableRuleImpl();
    matchingRule.setKey(rule.getKey());
    Map<String, DmnDecisionTableValue> ruleOutputs = evaluateRuleOutput(rule, variables, evaluationCache);
    matchingRule.setOutputs(ruleOutputs);
    return matchingRule;
  }

  protected Map<String, DmnDecisionTableValue> evaluateRuleOutput(DmnRule rule, Map<String, Object> variables, Map<String, Object> evaluationCache) {
    Map<String, DmnDecisionTableValue> outputs = new HashMap<String, DmnDecisionTableValue>();
    for (DmnClauseEntry conclusion : rule.getConclusions()) {
      DmnDecisionTableValueImpl output = new DmnDecisionTableValueImpl(conclusion.getClause());
      Object value = evaluateExpression(conclusion, variables, evaluationCache);

      if(hasOutputDefinitionWithTypeDefinition(conclusion)) {
        value = conclusion.getClause().getOutputDefinition().getTypeDefinition().transform(value);
      }

      output.setValue(value);
      outputs.put(output.getKey(), output);
    }
    return outputs;
  }

  protected boolean isExpressionApplicable(DmnExpression expression, Map<String, Object> variables, Map<String, Object> evaluationCache) {
    Object result = evaluateExpression(expression, variables, evaluationCache);
    return result != null && result.equals(true);
  }

  protected Object evaluateExpression(DmnExpression expression, Map<String, Object> variables, Map<String, Object> evaluationCache) {
    String expressionKey = expression.getKey();
    if (evaluationCache != null && evaluationCache.containsKey(expressionKey)) {
      return evaluationCache.get(expressionKey);
    }
    else {
      Object value = evaluateExpression(expression, variables);
      if (evaluationCache != null) {
        evaluationCache.put(expressionKey, value);
      }
      return value;
    }
  }

  protected boolean hasOutputDefinitionWithTypeDefinition(DmnClauseEntry conclusion) {
    DmnItemDefinition outputDefinition = conclusion.getClause().getOutputDefinition();
    return outputDefinition != null && outputDefinition.getTypeDefinition() != null;
  }

  protected Object evaluateExpression(DmnExpression expression, Map<String, Object> variables) {
    String expressionText = expression.getExpression();
    if (expressionText != null) {
      String expressionLanguage = expression.getExpressionLanguage();
      ScriptEngine scriptEngine = getScriptEngineForNameChecked(expressionLanguage);
      Bindings bindings = createBindings(scriptEngine, variables);

      try {
        return scriptEngine.eval(expressionText, bindings);
      } catch (ScriptException e) {
        throw LOG.unableToEvaluateExpression(expressionText, scriptEngine.getFactory().getLanguageName(), e);
      }
    }
    else {
      return null;
    }
  }

  protected ScriptEngine getScriptEngineForNameChecked(String expressionLanguage) {
    ScriptEngine scriptEngine = getScriptEngineForName(expressionLanguage);
    if (scriptEngine != null) {
      return scriptEngine;
    }
    else {
      throw LOG.noScriptEngineFoundForLanguage(expressionLanguage, DEFAULT_SCRIPT_LANGUAGE);
    }
  }

  protected ScriptEngine getScriptEngineForName(String expressionLanguage) {
    if (expressionLanguage == null) {
      expressionLanguage = DEFAULT_SCRIPT_LANGUAGE;
    }
    return scriptEngineResolver.getScriptEngineForLanguage(expressionLanguage);
  }

  protected Bindings createBindings(ScriptEngine scriptEngine, Map<String, Object> variables) {
    Bindings bindings = scriptEngine.createBindings();
    bindings.putAll(variables);
    return bindings;
  }

}
