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
package org.camunda.bpm.engine.impl.core.variable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.delegate.CoreVariableInstance;
import org.camunda.bpm.engine.impl.variable.AbstractVariableStore;

public abstract class MapBasedVariableStore<T extends CoreVariableInstance> extends AbstractVariableStore<T> {

  protected Map<String, T> variables = new HashMap<String, T>();

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Collection<T> getVariableInstancesValues() {
    return (Collection) variables.values();
  }

  public T getVariableInstance(String variableName) {
    return variables.get(variableName);
  }

  public Set<String> getVariableNames() {
    return variables.keySet();
  }

  public boolean isEmpty() {
    return variables.isEmpty();
  }

  public boolean containsVariableInstance(String variableName) {
    return variables.containsKey(variableName);
  }

  public T removeVariableInstance(String variableName, CoreVariableScope<T> sourceActivityExecution) {
    return variables.remove(variableName);
  }

  public Map<String, T> getVariableInstances() {
    return variables;
  }
}
