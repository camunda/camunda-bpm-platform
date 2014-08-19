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
package org.camunda.bpm.engine.impl.variable;

import org.camunda.bpm.engine.delegate.CoreVariableInstance;
import org.camunda.bpm.engine.impl.core.variable.CoreVariableScope;
import org.camunda.bpm.engine.impl.core.variable.CoreVariableStore;

public abstract class AbstractVariableStore<T extends CoreVariableInstance> implements CoreVariableStore<T> {

  public void createOrUpdateVariable(String variableName, Object value, CoreVariableScope<T> sourceActivityExecution) {
    T variableInstance = getVariableInstance(variableName);

    if (variableInstance == null) {
      createVariableInstance(variableName, value, sourceActivityExecution);
    } else {
      setVariableInstanceValue(variableInstance, value, sourceActivityExecution);
    }
  }

  protected abstract void setVariableInstanceValue(T variableInstance, Object value, CoreVariableScope<T> sourceActivityExecution);
}
