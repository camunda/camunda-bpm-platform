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

package org.camunda.dmn.juel;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.el.VariableMapper;
import javax.script.ScriptContext;

public class JuelScriptContextVariableMapper extends VariableMapper {

  protected ScriptContext context;
  protected ExpressionFactory expressionFactory;

  public JuelScriptContextVariableMapper(ScriptContext context, ExpressionFactory expressionFactory) {
    this.context = context;
    this.expressionFactory = expressionFactory;
  }

  public ValueExpression resolveVariable(String variableName) {
    int scope = context.getAttributesScope(variableName);
    if (scope != -1) {
      Object value = context.getAttribute(variableName, scope);
      if (value instanceof ValueExpression) {
        // Just return the existing ValueExpression
        return (ValueExpression) value;
      }
      else {
        // Create a new ValueExpression based on the variable value
        return expressionFactory.createValueExpression(value, Object.class);
      }
    }
    else {
      return null;
    }
  }

  public ValueExpression setVariable(String name, ValueExpression value) {
    ValueExpression previousValue = resolveVariable(name);
    context.setAttribute(name, value, ScriptContext.ENGINE_SCOPE);
    return previousValue;
  }

}
