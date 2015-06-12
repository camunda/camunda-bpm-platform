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

package org.camunda.dmn.engine.impl.context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.camunda.dmn.engine.DmnDecision;
import org.camunda.dmn.engine.DmnDecisionOutput;
import org.camunda.dmn.engine.DmnDecisionResult;
import org.camunda.dmn.engine.DmnExpression;
import org.camunda.dmn.engine.DmnRule;
import org.camunda.dmn.engine.context.DmnDecisionContext;
import org.camunda.dmn.engine.context.DmnScriptContext;
import org.camunda.dmn.engine.context.DmnVariableContext;
import org.camunda.dmn.engine.impl.DmnDecisionOutputEntryImpl;
import org.camunda.dmn.engine.impl.DmnDecisionOutputImpl;
import org.camunda.dmn.engine.impl.DmnDecisionResultImpl;
import org.camunda.dmn.engine.impl.DmnEngineLogger;
import org.camunda.dmn.engine.impl.DmnRuleImpl;

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

  public DmnDecisionResult evaluate(DmnDecision decision) {
    return evaluate(decision, new HashMap<String, Object>());
  }

  public DmnDecisionResult evaluate(DmnDecision decision, Map<String, Object> evaluationCache) {
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
    // evaluate all conditions
    boolean applicable = true;
    Map<String, DmnExpression> inputExpressions = rule.getInputExpressions();
    Map<String, List<DmnExpression>> disjunctions = rule.getConditions();
    for (Map.Entry<String, List<DmnExpression>> disjunction : disjunctions.entrySet()) {
      boolean disjunctionApplicable = false;

      // set new variable context for this rule evaluation
      DmnVariableContext originalVariableContext = getVariableContextChecked();
      DmnDelegatingVariableContext evaluationVariableContext = new DmnDelegatingVariableContext(originalVariableContext);
      setVariableContext(evaluationVariableContext);

      String clauseId = disjunction.getKey();
      DmnExpression inputExpression = inputExpressions.get(clauseId);
      if (inputExpression != null) {
        Object result = evaluate(inputExpression, evaluationCache);
        String name = inputExpression.getVariableName();
        evaluationVariableContext.setVariable(name, result);
      }

      for (DmnExpression expression : disjunction.getValue()) {
        if (isApplicable(expression, evaluationCache)) {
          disjunctionApplicable = true;
          break;
        }
      }

      // reset variable context
      setVariableContext(originalVariableContext);

      if (!disjunctionApplicable) {
        applicable = false;
        break;
      }
    }

    return applicable;
  }

  public <T> T evaluate(DmnExpression expression) {
    return evaluate(expression, null);
  }

  @SuppressWarnings("unchecked")
  public <T> T evaluate(DmnExpression expression, Map<String, Object> evaluationCache) {
    String expressionId = expression.getId();
    Object result;

    if (evaluationCache != null && evaluationCache.containsKey(expressionId)) {
      result = evaluationCache.get(expressionId);
    }
    else {
      String expressionLanguage = expression.getExpressionLanguage();
      String expressionText = expression.getExpression();
      ScriptEngine scriptEngine = getScriptEngineForName(expressionLanguage);
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
    for (DmnExpression expression : rule.getConclusions()) {
      Object result = evaluate(expression, evaluationCache);
      String variableName = expression.getVariableName();
      output.addEntry(new DmnDecisionOutputEntryImpl(variableName, result));
    }
    return output;
  }

  public boolean isApplicable(DmnExpression expression, Map<String, Object> evaluationCache) {
    Object result = evaluate(expression, evaluationCache);
    return result != null && result.equals(true);
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
