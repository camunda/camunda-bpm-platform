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

import org.camunda.bpm.model.dmn.instance.Expression;
import org.camunda.bpm.model.dmn.instance.LiteralExpression;
import org.camunda.dmn.engine.DmnExpressionException;
import org.camunda.dmn.engine.DmnParseException;

public class DmnExpressionLogger extends DmnLogger {

  public DmnParseException expressionTypeNotSupported(Expression expression) {
    return new DmnParseException(exceptionMessage("001", "Expression from type '{}' are not supported.", expression.getClass().getSimpleName()));
  }

  public DmnParseException expressionDoesNotContainText(LiteralExpression expression) {
    return new DmnParseException(exceptionMessage("002", "Expression '{}' does not contain text element", expression.getId()));
  }

  public DmnExpressionException unableToEvaluateExpression(String expression, String expressionLanguage, Throwable cause) {
    return new DmnExpressionException(exceptionMessage("003", "Unable to evaluate expression for language '{}': '{}'", expressionLanguage, expression), cause);
  }


  public DmnExpressionException unableToCastExpressionResult(Throwable cause) {
    return new DmnExpressionException(exceptionMessage("004", "Unable to cast result to expected type"), cause);
  }
}
