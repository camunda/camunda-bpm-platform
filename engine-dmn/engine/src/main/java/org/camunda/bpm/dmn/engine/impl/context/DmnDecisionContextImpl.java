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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.DmnDecisionTable;
import org.camunda.bpm.dmn.engine.DmnExpression;
import org.camunda.bpm.dmn.engine.impl.DmnEngineLogger;
import org.camunda.bpm.dmn.engine.DmnClause;
import org.camunda.bpm.dmn.engine.DmnClauseEntry;
import org.camunda.bpm.dmn.engine.DmnDecisionOutput;
import org.camunda.bpm.dmn.engine.DmnRule;
import org.camunda.bpm.dmn.engine.context.DmnDecisionContext;
import org.camunda.bpm.dmn.engine.context.DmnScriptContext;
import org.camunda.bpm.dmn.engine.context.DmnVariableContext;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionOutputEntryImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionOutputImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionResultImpl;

public class DmnDecisionContextImpl implements DmnDecisionContext {

  protected static final DmnEngineLogger LOG = DmnEngineLogger.ENGINE_LOGGER;

  protected DmnVariableContext variableContext;
  protected DmnScriptContext scriptContext;

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

  public void setScriptContext(DmnScriptContext scriptContext) {
    this.scriptContext = scriptContext;
  }

  public DmnScriptContext getScriptContext() {
    return scriptContext;
  }

  public DmnScriptContext getScriptContextChecked() {
    if (scriptContext != null) {
      return scriptContext;
    }
    else {
      throw LOG.noScriptContextSetInDecisionContext();
    }
  }

  public DmnDecisionResult evaluate(DmnDecisionTable decision) {
    return evaluate(decision, new HashMap<String, Object>());
  }

  public DmnDecisionResult evaluate(DmnDecisionTable decision, Map<String, Object> evaluationCache) {
    DmnDecisionResultImpl decisionResult = new DmnDecisionResultImpl();

    for (DmnRule rule : decision.getRules()) {
      if (isApplicable(rule, evaluationCache)) {
        DmnDecisionOutput output = getOutput(rule, evaluationCache);
        // TODO: notify Rule Listener
        decisionResult.addOutput(output);
      }
      else {
        // TODO: notify Rule Listener
      }
    }

    return decisionResult;
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
      String expressionLanguage = expression.getExpressionLanguage();
      String expressionText = expression.getExpression();
      ScriptEngine scriptEngine = getScriptEngineForNameChecked(expressionLanguage);
      Bindings bindings = createBindings(scriptEngine);

      try {
        result = scriptEngine.eval(expressionText, bindings);
      } catch (ScriptException e) {
        throw LOG.unableToEvaluateExpression(expressionText, scriptEngine.getFactory().getLanguageName(), e);
      }
    }

    try {
      return (T) result;
    } catch (ClassCastException e) {
      throw LOG.unableToCastExpressionResult(result, e);
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
      output.addEntry(new DmnDecisionOutputEntryImpl(outputName, result));
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
    if (expressionLanguage != null) {
      return getScriptContextChecked().getScriptEngineForName(expressionLanguage);
    }
    else {
      return getScriptContextChecked().getDefaultScriptEngine();
    }
  }

  protected Bindings createBindings(ScriptEngine scriptEngine) {
    Bindings bindings = scriptEngine.createBindings();
    bindings.putAll(getVariableContextChecked().getVariables());
    return bindings;
  }

}
