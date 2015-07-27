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

import org.camunda.bpm.dmn.engine.DmnTransformException;
import org.camunda.bpm.model.dmn.instance.Expression;
import org.camunda.bpm.model.dmn.instance.LiteralExpression;

public class DmnExpressionLogger extends DmnLogger {

  public DmnTransformException expressionTypeNotSupported(Expression expression) {
    return new DmnTransformException(exceptionMessage("001", "Expression from type '{}' are not supported.", expression.getClass().getSimpleName()));
  }

  public DmnTransformException expressionDoesNotContainText(LiteralExpression expression) {
    return new DmnTransformException(exceptionMessage("002", "Expression '{}' does not contain text element", expression.getId()));
  }

}
