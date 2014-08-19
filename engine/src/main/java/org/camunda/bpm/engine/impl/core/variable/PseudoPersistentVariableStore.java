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

import java.util.Map;

import org.camunda.bpm.engine.delegate.PersistentVariableInstance;

public class PseudoPersistentVariableStore extends MapBasedVariableStore<PersistentVariableInstance> implements CorePersistentVariableStore {

  public void setVariableInstanceValue(PersistentVariableInstance variableInstance, Object value,
      CoreVariableScope<PersistentVariableInstance> sourceActivityExecution) {
    ((PseudoPersistentVariableInstance) variableInstance).value = value;
  }

  public PersistentVariableInstance createVariableInstance(String variableName, Object value,
      CoreVariableScope<PersistentVariableInstance> sourceActivityExecution) {
    PseudoPersistentVariableInstance instance = new PseudoPersistentVariableInstance(variableName, value);
    variables.put(variableName, instance);
    return instance;
  }

  public PersistentVariableInstance createVariableInstanceFromSerialized(String variableName, Object value, String variableTypeName,
      Map<String, Object> configuration, CoreVariableScope<PersistentVariableInstance> sourceActivityExecution) {
    throw new UnsupportedOperationException("The variables of PseudoPersistentVariableStore are not truly serialized");
  }

  public void createOrUpdateVariableFromSerialized(String variableName, Object value, String variableTypeName, Map<String, Object> configuration,
      CoreVariableScope<PersistentVariableInstance> sourceActivityExecution) {
    throw new UnsupportedOperationException("The variables of PseudoPersistentVariableStore are not truly serialized");
  }

}
