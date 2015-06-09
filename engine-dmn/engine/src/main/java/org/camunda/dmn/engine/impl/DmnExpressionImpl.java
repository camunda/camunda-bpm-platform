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

package org.camunda.dmn.engine.impl;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.camunda.bpm.model.dmn.instance.Clause;
import org.camunda.bpm.model.dmn.instance.Expression;
import org.camunda.bpm.model.dmn.instance.LiteralExpression;
import org.camunda.bpm.model.dmn.instance.Text;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.dmn.engine.DmnDecisionContext;
import org.camunda.dmn.engine.DmnExpression;

public class DmnExpressionImpl implements DmnExpression {

  protected static final DmnExpressionLogger LOG = DmnLogger.EXPRESSION_LOGGER;

  protected String variableName;
  protected String expressionLanguage;
  protected String expression;

  public DmnExpressionImpl(Expression expression) {
    parseVariableName(expression);
    parseExpression(expression);
  }

  public String getVariableName() {
    return variableName;
  }

  public String getExpression() {
    return expression;
  }

  public String getExpressionLanguage() {
    return expressionLanguage;
  }

  public boolean isSatisfied(DmnDecisionContext context) {
    Object result = evaluate(context);
    return result != null && result.equals(true);
  }

  @SuppressWarnings("unchecked")
  public <T> T evaluate(DmnDecisionContext context) {
    ScriptEngine scriptEngine = context.getScriptEngineContextChecked().getScriptEngineForName(expressionLanguage);
    Bindings bindings = scriptEngine.createBindings();
    bindings.putAll(context.getVariables());

    try {
      return (T) scriptEngine.eval(expression, bindings);
    } catch (ScriptException e) {
      throw LOG.unableToEvaluateExpression(expression, scriptEngine.getFactory().getLanguageName(), e);
    } catch (ClassCastException e) {
      throw LOG.unableToCastExpressionResult(e);
    }
  }

  protected void parseExpression(Expression expression) {
    if (expression instanceof LiteralExpression) {
      parseLiteralExpression((LiteralExpression) expression);
    }
    else {
      throw LOG.expressionTypeNotSupported(expression);
    }
  }

  protected void parseLiteralExpression(LiteralExpression expression) {
    parseLiteralExpressionText(expression);
    parseExpressionLanguage(expression);
  }

  protected void parseLiteralExpressionText(LiteralExpression expression) {
    Text text = expression.getText();
    if (text != null) {
      this.expression = text.getTextContent().trim();
    }
    else {
      throw LOG.expressionDoesNotContainText(expression);
    }
  }

  protected void parseExpressionLanguage(LiteralExpression expression) {
    this.expressionLanguage = expression.getExpressionLanguage();
  }

  protected void parseVariableName(Expression expression) {
    variableName = expression.getName();
    if (variableName == null) {
      ModelElementInstance parentElement = expression.getParentElement();
      if (parentElement instanceof Clause) {
        variableName = ((Clause) parentElement).getName();
      }
    }
  }

}
