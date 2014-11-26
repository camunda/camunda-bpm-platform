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
package org.camunda.bpm.engine.impl.scripting.engine;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.Set;

import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.pvm.runtime.ExecutionImpl;


/**
 * Bindings implementation using an {@link ExecutionImpl} as 'back-end'.
 *
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class VariableScopeResolver implements Resolver {

  protected VariableScope variableScope;
  protected String variableScopeKey;

  public VariableScopeResolver(VariableScope variableScope) {
    ensureNotNull("variableScope", variableScope);
    variableScopeKey = variableScope.getVariableScopeKey();
    this.variableScope = variableScope;
  }

  public boolean containsKey(Object key) {
    return variableScopeKey.equals(key) || variableScope.hasVariable((String) key);
  }

  public Object get(Object key) {
    if (variableScopeKey.equals(key)) {
      return variableScope;
    }
    return variableScope.getVariable((String) key);
  }

  public Set<String> keySet() {
    // get variable names will return a new set instance
    Set<String> variableNames = variableScope.getVariableNames();
    variableNames.add(variableScopeKey);
    return variableNames;
  }
}
