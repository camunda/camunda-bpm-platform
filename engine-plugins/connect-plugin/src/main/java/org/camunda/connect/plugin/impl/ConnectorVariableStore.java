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
package org.camunda.connect.plugin.impl;

import org.camunda.bpm.engine.impl.core.variable.CoreVariableInstance;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.bpm.engine.impl.core.variable.scope.MapBasedVariableStore;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Daniel Meyer
 *
 */
public class ConnectorVariableStore extends MapBasedVariableStore {

  public static class ConnectorParamVariable implements CoreVariableInstance {

    protected String name;
    protected TypedValue value;

    public ConnectorParamVariable(String name, TypedValue value) {
      this.name = name;
      this.value = value;
    }

    public String getName() {
      return name;
    }

    public TypedValue getTypedValue(boolean deserializeObjectValue) {
      return value;
    }
  }

  @Override
  public void setVariableValue(CoreVariableInstance variableInstance, TypedValue value, AbstractVariableScope sourceActivityExecution) {
    ((ConnectorParamVariable)variableInstance).value = value;
  }

  @Override
  public ConnectorParamVariable createVariableInstance(String variableName, TypedValue value, AbstractVariableScope sourceActivityExecution) {
    ConnectorParamVariable variableInstance = new ConnectorParamVariable(variableName, value);
    variables.put(variableName, variableInstance);
    return variableInstance;
  }

  @Override
  public void createTransientVariable(String variableName, TypedValue value, AbstractVariableScope sourceActivityExecution) {
    // all variables are transient in this store
    createVariableInstance(variableName, value, sourceActivityExecution);
  }

}
