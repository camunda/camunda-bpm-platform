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

package org.camunda.bpm.dmn.engine.impl.handler;

import org.camunda.bpm.dmn.engine.DmnClause;
import org.camunda.bpm.dmn.engine.handler.DmnElementHandlerContext;
import org.camunda.bpm.dmn.engine.impl.DmnExpressionImpl;
import org.camunda.bpm.model.dmn.instance.InputEntry;

public class DmnInputEntryHandler extends AbstractDmnClauseHandler<InputEntry> {

  protected void postProcessExpressionText(DmnElementHandlerContext context, InputEntry expression, DmnExpressionImpl dmnExpression) {
    if (hasJuelExpressionLanguage(dmnExpression) && !isExpression(dmnExpression)) {
      String expressionText = dmnExpression.getExpression();
      String variableName = getVariableName(context);
      if (startsWithOperator(expressionText)) {
        dmnExpression.setExpression("${" + variableName + expressionText + "}");
      }
      else if (isNumber(expressionText)) {
        dmnExpression.setExpression("${" + variableName + "==" + expressionText + "}");
      }
      else if (isBoolean(expressionText)) {
        Boolean booleanValue = Boolean.parseBoolean(expressionText.trim());
        dmnExpression.setExpression("${" + variableName + "==" + booleanValue + "}");
      }
      else {
        dmnExpression.setExpression("${" + variableName + "=='" + expressionText + "'}");
      }
    }
  }

  protected String getVariableName(DmnElementHandlerContext context) {
    DmnClause clause = (DmnClause) context.getParent();
    return clause.getOutputName();
  }

  protected boolean isNumber(String text) {
    try {
      Double.parseDouble(text);
      return true;
    }
    catch (NumberFormatException e) {
      return false;
    }
  }

  protected boolean isBoolean(String text) {
    return text != null && (text.trim().equalsIgnoreCase("true") || text.trim().equalsIgnoreCase("false"));
  }

  protected boolean startsWithOperator(String text) {
    char firstChar = text.trim().charAt(0);
    return !Character.isLetterOrDigit(firstChar);
  }

}
