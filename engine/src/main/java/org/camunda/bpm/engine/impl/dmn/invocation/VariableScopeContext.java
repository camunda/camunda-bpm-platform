/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.dmn.invocation;

import java.util.Set;

import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.variable.context.VariableContext;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * Implementation of the {@link VariableContext} interface backed
 * by a {@link VariableScope}.
 *
 * @author Daniel Meyer
 *
 */
public class VariableScopeContext implements VariableContext {

  protected final VariableScope variableScope;

  public VariableScopeContext(VariableScope variableScope) {
    this.variableScope = variableScope;
  }

  public TypedValue resolve(String variableName) {
    return variableScope.getVariableTyped(variableName);
  }

  public boolean containsVariable(String variableName) {
    return variableScope.hasVariable(variableName);
  }

  public Set<String> keySet() {
    return variableScope.getVariableNames();
  }

  public static VariableScopeContext wrap(VariableScope variableScope) {
    return new VariableScopeContext(variableScope);
  }

}
