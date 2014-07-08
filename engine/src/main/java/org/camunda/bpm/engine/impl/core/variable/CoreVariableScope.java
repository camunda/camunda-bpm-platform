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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.javax.el.ELContext;
import org.camunda.bpm.engine.runtime.VariableInstance;

/**
 * @author Daniel Meyer
 * @author Roman Smirnov
 * @author Sebastian Menski
 *
 */
public abstract class CoreVariableScope implements Serializable, VariableScope {

  private static final long serialVersionUID = 1L;

  protected abstract CoreVariableStore getVariableStore();

  // TODO: move this?
  protected ELContext cachedElContext;

  public abstract CoreVariableScope getParentVariableScope();

  public Map<String, Object> getVariables() {
    return collectVariables(new HashMap<String, Object>());
  }

  public Map<String, VariableInstance> getVariableInstances() {
    return collectVariableInstances(new HashMap<String, VariableInstance>(), null);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Map<String, VariableInstance> getVariableInstancesLocal() {
    CoreVariableStore variableStore = getVariableStore();
    return new HashMap<String, VariableInstance>((Map) variableStore.getVariableInstances());
  }

  public Map<String, VariableInstance> getVariableInstances(Collection<String> variableNames) {
    return collectVariableInstances(new HashMap<String, VariableInstance>(), variableNames);
  }

  public Map<String, VariableInstance> collectVariableInstances(Map<String, VariableInstance> variables, Collection<String> variableNames) {
    Map<String, VariableInstance> variableInstances = getVariableInstancesLocal();
    for (VariableInstance variable : variableInstances.values()) {
      if(!variables.containsKey(variable.getName())
         && (variableNames == null || variableNames.contains(variable.getName()))) {
        variables.put(variable.getName(), variable);
      }
    }
    CoreVariableScope parentScope = getParentVariableScope();
    if(parentScope != null && (variableNames == null || !variables.keySet().equals(variableNames))) {
      parentScope.collectVariableInstances(variables, variableNames);
    }
    return variables;
  }

  protected Map<String, Object> collectVariables(HashMap<String, Object> variables) {
    CoreVariableScope parentScope = getParentVariableScope();
    if (parentScope!=null) {
      variables.putAll(parentScope.collectVariables(variables));
    }
    for (CoreVariableInstance variableInstance: getVariableStore().getVariableInstancesValues()) {
      variables.put(variableInstance.getName(), variableInstance.getValue());
    }
    return variables;
  }

  public Object getVariable(String variableName) {
    CoreVariableInstance variableInstance = getVariableStore().getVariableInstance(variableName);
    if (variableInstance!=null) {
      return variableInstance.getValue();
    }
    CoreVariableScope parentScope = getParentVariableScope();
    if (parentScope!=null) {
      return parentScope.getVariable(variableName);
    }
    return null;
  }

  public Object getVariableLocal(String variableName) {
    CoreVariableInstance variableInstance = getVariableStore().getVariableInstance(variableName);
    if (variableInstance!=null) {
      return variableInstance.getValue();
    }
    return null;
  }

  public boolean hasVariables() {
    if (!getVariableStore().isEmpty()) {
      return true;
    }
    CoreVariableScope parentScope = getParentVariableScope();
    return parentScope != null && parentScope.hasVariables();
  }

  public boolean hasVariablesLocal() {
    return !getVariableStore().isEmpty();
  }

  public boolean hasVariable(String variableName) {
    if (hasVariableLocal(variableName)) {
      return true;
    }
    CoreVariableScope parentScope = getParentVariableScope();
    return parentScope != null && parentScope.hasVariable(variableName);
  }

  public boolean hasVariableLocal(String variableName) {
    return getVariableStore().containsVariableInstance(variableName);
  }

  protected Set<String> collectVariableNames(Set<String> variableNames) {
    CoreVariableScope parentScope = getParentVariableScope();
    if (parentScope!=null) {
      variableNames.addAll(parentScope.collectVariableNames(variableNames));
    }
    for (CoreVariableInstance variableInstance: getVariableStore().getVariableInstancesValues()) {
      variableNames.add(variableInstance.getName());
    }
    return variableNames;
  }

  public Set<String> getVariableNames() {
    return collectVariableNames(new HashSet<String>());
  }

  public Map<String, Object> getVariablesLocal() {
    Map<String, Object> variables = new HashMap<String, Object>();
    for (CoreVariableInstance variableInstance: getVariableStore().getVariableInstancesValues()) {
      variables.put(variableInstance.getName(), variableInstance.getValue());
    }
    return variables;
  }

  public Set<String> getVariableNamesLocal() {
    return getVariableStore().getVariableNames();
  }

  protected void createVariablesLocal(Map<String, ? extends Object> variables) {
    if (variables!=null) {
      for (Map.Entry<String, ? extends Object> entry: variables.entrySet()) {
        createVariableLocal(entry.getKey(), entry.getValue());
      }
    }
  }

  public void setVariables(Map<String, ? extends Object> variables) {
    if (variables!=null) {
      for (String variableName : variables.keySet()) {
        setVariable(variableName, variables.get(variableName));
      }
    }
  }

  public void setVariablesLocal(Map<String, ? extends Object> variables) {
    if (variables!=null) {
      for (String variableName : variables.keySet()) {
        setVariableLocal(variableName, variables.get(variableName));
      }
    }
  }

  public void removeVariables() {
    Set<String> variableNames = new HashSet<String>(getVariableStore().getVariableNames());
    for (String variableName: variableNames) {
      removeVariable(variableName);
    }
  }

  public void removeVariablesLocal() {
    List<String> variableNames = new ArrayList<String>(getVariableNamesLocal());
    for (String variableName: variableNames) {
      removeVariableLocal(variableName);
    }
  }

  public void removeVariables(Collection<String> variableNames) {
    if (variableNames != null) {
      for (String variableName : variableNames) {
        removeVariable(variableName);
      }
    }
  }

  public void removeVariablesLocal(Collection<String> variableNames) {
    if (variableNames != null) {
      for (String variableName : variableNames) {
        removeVariableLocal(variableName);
      }
    }
  }

  public void setVariable(String variableName, Object value) {
    setVariable(variableName, value, getSourceActivityVariableScope());
  }

  protected void setVariable(String variableName, Object value, CoreVariableScope sourceActivityExecution) {
    if (hasVariableLocal(variableName)) {
      setVariableLocal(variableName, value, sourceActivityExecution);
      return;
    }
    CoreVariableScope parentVariableScope = getParentVariableScope();
    if (parentVariableScope!=null) {
      if (sourceActivityExecution==null) {
        parentVariableScope.setVariable(variableName, value);
      } else {
        parentVariableScope.setVariable(variableName, value, sourceActivityExecution);
      }
      return;
    }
    createVariableLocal(variableName, value, sourceActivityExecution);
  }

  public void setVariableLocal(String variableName, Object value) {
    setVariableLocal(variableName, value, getSourceActivityVariableScope());
  }

  protected void setVariableLocal(String variableName, Object value, CoreVariableScope sourceActivityExecution) {
    CoreVariableInstance variableInstance = getVariableStore().getVariableInstance(variableName);
    if ((variableInstance != null) && (!variableInstance.isAbleToStore(value))) {
      // it seems that the type has changed -> clear the variable instance
      getVariableStore().clearForNewValue(variableInstance, value);
    }
    if (variableInstance == null) {
      createVariableLocal(variableName, value, sourceActivityExecution);
    } else {
      updateVariableInstance(variableInstance, value, sourceActivityExecution);
    }
  }

  protected void createVariableLocal(String variableName, Object value) {
    createVariableLocal(variableName, value, getSourceActivityVariableScope());
  }

  /** only called when a new variable is created on this variable scope.
   * This method is also responsible for propagating the creation of this
   * variable to the history. */
  protected void createVariableLocal(String variableName, Object value, CoreVariableScope sourceActivityExecution) {

    if (getVariableStore().containsVariableInstance(variableName)) {
      throw new ProcessEngineException("variable '"+variableName+"' already exists. Use setVariableLocal if you want to overwrite the value");
    }

    createVariableInstance(variableName, value, sourceActivityExecution);
  }

  public void removeVariable(String variableName) {
    removeVariable(variableName, getSourceActivityVariableScope());
  }

  protected void removeVariable(String variableName, CoreVariableScope sourceActivityExecution) {
    if (getVariableStore().containsVariableInstance(variableName)) {
      removeVariableLocal(variableName);
      return;
    }
    CoreVariableScope parentVariableScope = getParentVariableScope();
    if (parentVariableScope!=null) {
      if (sourceActivityExecution==null) {
        parentVariableScope.removeVariable(variableName);
      } else {
        parentVariableScope.removeVariable(variableName, sourceActivityExecution);
      }
    }
  }

  public void removeVariableLocal(String variableName) {
    removeVariableLocal(variableName, getSourceActivityVariableScope());
  }

  protected CoreVariableScope getSourceActivityVariableScope() {
    return this;
  }

  protected void removeVariableLocal(String variableName, CoreVariableScope sourceActivityExecution) {
    getVariableStore().removeVariableInstance(variableName, sourceActivityExecution);
  }

  protected void updateVariableInstance(CoreVariableInstance variableInstance, Object value, CoreVariableScope sourceActivityExecution) {
    // update variable instance
    getVariableStore().setVariableInstanceValue(variableInstance, value, sourceActivityExecution);
  }

  protected CoreVariableInstance createVariableInstance(String variableName, Object value, CoreVariableScope sourceActivityExecution) {
    return getVariableStore().createVariableInstance(variableName, value, sourceActivityExecution);
  }

  public ELContext getCachedElContext() {
    return cachedElContext;
  }
  public void setCachedElContext(ELContext cachedElContext) {
    this.cachedElContext = cachedElContext;
  }

}
