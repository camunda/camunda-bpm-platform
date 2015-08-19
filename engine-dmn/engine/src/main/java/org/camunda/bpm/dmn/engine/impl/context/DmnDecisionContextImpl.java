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

import org.camunda.bpm.dmn.engine.DmnClause;
import org.camunda.bpm.dmn.engine.DmnClauseEntry;
import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionOutput;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.DmnDecisionTable;
import org.camunda.bpm.dmn.engine.DmnExpression;
import org.camunda.bpm.dmn.engine.DmnRule;
import org.camunda.bpm.dmn.engine.ScriptEngineResolver;
import org.camunda.bpm.dmn.engine.context.DmnDecisionContext;
import org.camunda.bpm.dmn.engine.context.DmnVariableContext;
import org.camunda.bpm.dmn.engine.hitpolicy.DmnHitPolicyHandler;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionOutputImpl;
import org.camunda.bpm.dmn.engine.impl.DmnEngineLogger;
import org.camunda.bpm.dmn.juel.JuelScriptEngineFactory;
import org.camunda.bpm.model.dmn.HitPolicy;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DmnDecisionContextImpl implements DmnDecisionContext {

  protected static final DmnEngineLogger LOG = DmnEngineLogger.ENGINE_LOGGER;

  public static final String DEFAULT_SCRIPT_LANGUAGE = JuelScriptEngineFactory.NAME;

  protected DmnVariableContext variableContext;
  protected ScriptEngineResolver scriptEngineResolver;
  protected Map<HitPolicy, DmnHitPolicyHandler> hitPolicyHandlers;

  public void setVariableContext(DmnVariableContext variableContext) {
    this.variableContext = variableContext;
  }

  public DmnVariableContext getVariableContext() {
    return variableContext;
  }

  public DmnVariableContext getVariableContextChecked() {
    if (variableContext != null) {
      return variableContext;
    }
    else {
      throw LOG.noVariableContextSetInDecisionContext();
    }
  }

  public void setScriptEngineResolver(ScriptEngineResolver scriptEngineResolver) {
    this.scriptEngineResolver = scriptEngineResolver;
  }

  public ScriptEngineResolver getScriptEngineResolver() {
    return scriptEngineResolver;
  }

  public void setHitPolicyHandlers(Map<HitPolicy, DmnHitPolicyHandler> hitPolicyHandlers) {
    this.hitPolicyHandlers = hitPolicyHandlers;
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

  public DmnDecisionResult evaluate(DmnDecision decision) {
    return evaluate(decision, new HashMap<String, Object>());
  }

  public DmnDecisionResult evaluate(DmnDecision decision, Map<String, Object> evaluationCache) {
    if (!(decision instanceof DmnDecisionTable)) {
      throw LOG.decisionTypeNotSupported(decision);
    }

    DmnDecisionTable decisionTable = (DmnDecisionTable) decision;
    List<DmnRule> matchingRules = new ArrayList<DmnRule>();

    for (DmnRule rule : decisionTable.getRules()) {
      if (isApplicable(rule, evaluationCache)) {
        matchingRules.add(rule);
      }
    }

    return getDecisionOutput(decisionTable, matchingRules, evaluationCache);
  }

  protected DmnDecisionResult getDecisionOutput(DmnDecisionTable decisionTable, List<DmnRule> matchingRules, Map<String, Object> evaluationCache) {
    DmnHitPolicyHandler hitPolicyHandler = getHitPolicyHandlerChecked(decisionTable.getHitPolicy());
    List<DmnRule> outputRules = hitPolicyHandler.filterMatchingRules(decisionTable, matchingRules);
    List<DmnDecisionOutput> decisionOutputs = new ArrayList<DmnDecisionOutput>();

    for (DmnRule outputRule : outputRules) {
      DmnDecisionOutput output = getOutput(outputRule, evaluationCache);
      decisionOutputs.add(output);
    }

    return hitPolicyHandler.getDecisionResult(decisionTable, decisionOutputs);
  }

  public boolean isApplicable(DmnRule rule) {
    return isApplicable(rule, null);
  }

  public boolean isApplicable(DmnRule rule, Map<String, Object> evaluationCache) {
    Map<String, Boolean> clauseSatisfied = new HashMap<String, Boolean>();
    List<DmnClauseEntry> conditions = rule.getConditions();

    // save variable context
    DmnVariableContext originalVariableContext = getVariableContextChecked();

    for (DmnClauseEntry condition : conditions) {
      DmnClause clause = condition.getClause();
      Boolean alreadySatisfied = clauseSatisfied.get(clause.getKey());
      if (alreadySatisfied != null && alreadySatisfied) {
        // skip condition if clause already satisfied
        continue;
      }

      // set temporary evaluation variable cache
      DmnDelegatingVariableContext evaluationVariableContext = new DmnDelegatingVariableContext(originalVariableContext);
      setVariableContext(evaluationVariableContext);
      DmnExpression inputExpression = clause.getInputExpression();
      if (inputExpression != null) {
        Object inputExpressionResult = evaluate(inputExpression, evaluationCache);
        String outputName = clause.getOutputName();
        evaluationVariableContext.setVariable(outputName, inputExpressionResult);
      }

      boolean applicable = isApplicable(condition, evaluationCache);
      clauseSatisfied.put(clause.getKey(), applicable);
    }

    // reset variable context
    setVariableContext(originalVariableContext);

    // the rule is applicable if all involved clauses are satisfied
    return !clauseSatisfied.containsValue(false);
  }

  public <T> T evaluate(DmnExpression expression) {
    return evaluate(expression, null);
  }


  @SuppressWarnings("unchecked")
  public <T> T evaluate(DmnExpression expression, Map<String, Object> evaluationCache) {
    String expressionKey = expression.getKey();
    Object result;

    if (evaluationCache != null && evaluationCache.containsKey(expressionKey)) {
      result = evaluationCache.get(expressionKey);
    }
    else {
      result = evaluateExpression(expression);
    }

    try {
      return (T) result;
    } catch (ClassCastException e) {
      throw LOG.unableToCastExpressionResult(result, e);
    }
  }

  protected Object evaluateExpression(DmnExpression expression) {
    String expressionText = expression.getExpression();
    if (expressionText != null) {
      String expressionLanguage = expression.getExpressionLanguage();
      ScriptEngine scriptEngine = getScriptEngineForNameChecked(expressionLanguage);
      Bindings bindings = createBindings(scriptEngine);

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

  public boolean isApplicable(DmnExpression expression) {
    return isApplicable(expression, null);
  }

  public DmnDecisionOutput getOutput(DmnRule rule) {
    return getOutput(rule, null);
  }

  public DmnDecisionOutput getOutput(DmnRule rule, Map<String, Object> evaluationCache) {
    DmnDecisionOutputImpl output = new DmnDecisionOutputImpl();
    for (DmnClauseEntry conclusion : rule.getConclusions()) {
      Object result = evaluate(conclusion, evaluationCache);
      String outputName = conclusion.getClause().getOutputName();
      output.put(outputName, result);
    }
    return output;
  }

  public boolean isApplicable(DmnExpression expression, Map<String, Object> evaluationCache) {
    Object result = evaluate(expression, evaluationCache);
    return result != null && result.equals(true);
  }

  protected ScriptEngine getScriptEngineForNameChecked(String expressionLanguage) {
    ScriptEngine scriptEngine = getScriptEngineForName(expressionLanguage);
    if (scriptEngine != null) {
      return scriptEngine;
    }
    else {
      throw LOG.noScriptEngineFoundForLanguage(expressionLanguage);
    }
  }

  protected ScriptEngine getScriptEngineForName(String expressionLanguage) {
    if (expressionLanguage == null) {
      expressionLanguage = DEFAULT_SCRIPT_LANGUAGE;
    }
    return scriptEngineResolver.getScriptEngineForLanguage(expressionLanguage);
  }

  protected Bindings createBindings(ScriptEngine scriptEngine) {
    Bindings bindings = scriptEngine.createBindings();
    bindings.putAll(getVariableContextChecked().getVariables());
    return bindings;
  }

}
