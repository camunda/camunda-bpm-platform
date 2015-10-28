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

package org.camunda.bpm.dmn.feel.impl.el;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.el.VariableMapper;

import org.camunda.bpm.dmn.feel.impl.FeelEngineLogger;
import org.camunda.bpm.dmn.feel.impl.FeelLogger;
import org.camunda.bpm.engine.variable.VariableContext;
import org.camunda.bpm.engine.variable.value.TypedValue;

public class FeelTypedVariableMapper extends VariableMapper {

  public static final FeelEngineLogger LOG = FeelLogger.ENGINE_LOGGER;

  protected ExpressionFactory expressionFactory;
  protected VariableContext varCtx;

  public FeelTypedVariableMapper(ExpressionFactory expressionFactory, VariableContext varCtx) {
    this.expressionFactory = expressionFactory;
    this.varCtx = varCtx;
  }

  public ValueExpression resolveVariable(String variable) {
    if (varCtx.containsVariable(variable)) {
      Object value = unpackVariable(variable);
      return expressionFactory.createValueExpression(value, Object.class);
    }
    else {
      throw LOG.unknownVariable(variable);
    }
  }

  public ValueExpression setVariable(String variable, ValueExpression expression) {
    throw LOG.variableMapperIsReadOnly();
  }

  public Object unpackVariable(String variable) {
    TypedValue valueTyped = varCtx.resolve(variable);
    if(valueTyped != null) {
      return valueTyped.getValue();
    }
    return null;
  }

}
