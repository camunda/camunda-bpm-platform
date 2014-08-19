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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.PersistentVariableInstance;
import org.camunda.bpm.engine.delegate.PersistentVariableScope;

/**
 * @author Thorben Lindhauer
 */
public abstract class CorePersistentVariableScope extends CoreVariableScope<PersistentVariableInstance>
  implements PersistentVariableScope {

  private static final long serialVersionUID = 1L;

  public void setVariableFromSerialized(String variableName, Object value, String variableTypeName, Map<String, Object> configuration) {
    setVariableFromSerialized(variableName, value, variableTypeName, configuration, getSourceActivityVariableScope());
  }

  protected void setVariableFromSerialized(String variableName, Object value, String variableTypeName,
      Map<String, Object> configuration, CoreVariableScope<PersistentVariableInstance> sourceActivityExecution) {
    if (hasVariableLocal(variableName)) {
      setVariableLocalFromSerialized(variableName, value, variableTypeName, configuration, sourceActivityExecution);
      return;
    }

    CorePersistentVariableScope parentVariableScope = getParentVariableScope();
    if (parentVariableScope!=null) {
      if (sourceActivityExecution==null) {
        parentVariableScope.setVariableFromSerialized(variableName, value, variableTypeName, configuration);
      } else {
        parentVariableScope.setVariableFromSerialized(variableName, value, variableTypeName, configuration, sourceActivityExecution);
      }
      return;
    }

    createVariableLocalFromSerialized(variableName, value, variableTypeName, configuration, sourceActivityExecution);
  }

  protected void createVariableLocalFromSerialized(String variableName, Object value, String variableTypeName,
      Map<String, Object> configuration, CoreVariableScope<PersistentVariableInstance> sourceActivityExecution) {

    if (getVariableStore().containsVariableInstance(variableName)) {
      throw new ProcessEngineException("variable '"+variableName+"' already exists. Use setVariableLocal if you want to overwrite the value");
    }

    getVariableStore().createVariableInstanceFromSerialized(variableName, value, variableTypeName, configuration, sourceActivityExecution);
  }

  public void setVariableLocalFromSerialized(String variableName, Object value, String variableTypeName, Map<String, Object> configuration) {
    setVariableLocalFromSerialized(variableName, value, variableTypeName, configuration, getSourceActivityVariableScope());
  }

  protected void setVariableLocalFromSerialized(String variableName, Object value, String variableTypeName,
      Map<String, Object> configuration, CoreVariableScope<PersistentVariableInstance> sourceActivityExecution) {
    getVariableStore().createOrUpdateVariableFromSerialized(variableName, value, variableTypeName, configuration, sourceActivityExecution);
  }

  public abstract CorePersistentVariableScope getParentVariableScope();

  protected abstract CorePersistentVariableStore getVariableStore();
}
