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
package org.camunda.bpm.engine.impl.connector;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.impl.core.variable.CoreVariableInstance;
import org.camunda.bpm.engine.impl.core.variable.CoreVariableScope;
import org.camunda.bpm.engine.impl.core.variable.CoreVariableStore;

/**
 * @author Daniel Meyer
 *
 */
public class ConnectorVariableStore implements CoreVariableStore {

  public static class ConnectorParamVariable implements CoreVariableInstance {

    protected String name;
    protected Object value;

    public ConnectorParamVariable(String name, Object value) {
      this.name = name;
      this.value = value;
    }

    public String getName() {
      return name;
    }

    public Object getValue() {
      return value;
    }

    public boolean isAbleToStore(Object value) {
      return true;
    }
  }

  protected Map<String, ConnectorParamVariable> variables = new HashMap<String, ConnectorParamVariable>();

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Collection<CoreVariableInstance> getVariableInstancesValues() {
    return (Collection) variables.values();
  }

  public CoreVariableInstance getVariableInstance(String variableName) {
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

  public CoreVariableInstance removeVariableInstance(String variableName, CoreVariableScope sourceActivityExecution) {
    return variables.remove(variableName);
  }

  public void setVariableInstanceValue(CoreVariableInstance variableInstance, Object value, CoreVariableScope sourceActivityExecution) {
    ((ConnectorParamVariable)variableInstance).value = value;
  }

  public CoreVariableInstance createVariableInstance(String variableName, Object value, CoreVariableScope sourceActivityExecution) {
    ConnectorParamVariable variableInstance = new ConnectorParamVariable(variableName, value);
    variables.put(variableName, variableInstance);
    return variableInstance;
  }

  public void clearForNewValue(CoreVariableInstance variableInstance, Object newValue) {
    ((ConnectorParamVariable)variableInstance).value = null;
  }

  public Map<String, ConnectorParamVariable> getVariables() {
    return variables;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Map<String, CoreVariableInstance> getVariableInstances() {
    return (Map) variables;
  }

}
