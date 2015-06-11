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

import java.util.List;
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
import org.camunda.dmn.engine.impl.DmnDecisionResultImpl;
import org.camunda.dmn.engine.impl.DmnEngineLogger;

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
    DmnDecisionResultImpl decisionResult = new DmnDecisionResultImpl();

    for (DmnRule rule : decision.getRules()) {
      if (rule.isApplicable(this)) {
        DmnDecisionOutput output = rule.getOutput(this);
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
    List<List<DmnExpression>> disjunctions = rule.getInputExpressions();
    for (List<DmnExpression> disjunction : disjunctions) {
      boolean disjunctionApplicable = false;
      for (DmnExpression expression : disjunction) {
        if (expression.isApplicable(this)) {
          disjunctionApplicable = true;
          break;
        }
      }

      if (!disjunctionApplicable) {
        return false;
      }
    }

    // if all disjunctions are applicable then the rules is applicable
    return true;
  }

  @SuppressWarnings("unchecked")
  public <T> T evaluate(DmnExpression expression) {
    String expressionLanguage = expression.getExpressionLanguage();
    String expressionText = expression.getExpression();
    ScriptEngine scriptEngine = getScriptEngineForName(expressionLanguage);
    Bindings bindings = createBindings(scriptEngine);

    try {
      return (T) scriptEngine.eval(expressionText, bindings);
    } catch (ScriptException e) {
      throw LOG.unableToEvaluateExpression(expressionText, scriptEngine.getFactory().getLanguageName(), e);
    } catch (ClassCastException e) {
      throw LOG.unableToCastExpressionResult(e);
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
