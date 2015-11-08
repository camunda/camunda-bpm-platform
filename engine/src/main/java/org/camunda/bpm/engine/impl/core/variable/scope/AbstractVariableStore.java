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
package org.camunda.bpm.engine.impl.core.variable.scope;

import org.camunda.bpm.engine.impl.core.variable.CoreVariableInstance;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 *
 * @author Thorben Lindhauer
 *
 */
public abstract class AbstractVariableStore implements CoreVariableStore {

  public void createOrUpdateVariable(String variableName, TypedValue value, AbstractVariableScope sourceActivityExecution) {
    CoreVariableInstance variableInstance = getVariableInstance(variableName);

    if (variableInstance == null) {
      createVariableInstance(variableName, value, sourceActivityExecution);
    } else {
      setVariableValue(variableInstance, value, sourceActivityExecution);
    }
  }

  protected abstract CoreVariableInstance createVariableInstance(String variableName, TypedValue value, AbstractVariableScope sourceActivityExecution);

  protected abstract void setVariableValue(CoreVariableInstance variableInstance, TypedValue value, AbstractVariableScope sourceActivityExecution);

}
