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

import org.camunda.bpm.engine.delegate.CoreVariableInstance;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.javax.el.ELContext;

/**
 * @author Daniel Meyer
 * @author Roman Smirnov
 * @author Sebastian Menski
 *
 */
public abstract class CoreVariableScope<T extends CoreVariableInstance> implements Serializable, VariableScope<T>, VariableEventDispatcher {

  private static final long serialVersionUID = 1L;

  protected abstract CoreVariableStore<T> getVariableStore();

  // TODO: move this?
  protected ELContext cachedElContext;

  public abstract CoreVariableScope<T> getParentVariableScope();

  public Map<String, Object> getVariables() {
    return collectVariables(new HashMap<String, Object>());
  }

  public Map<String, T> getVariableInstances() {
    return collectVariableInstances(new HashMap<String, T>(), null);
  }

  public Map<String, T> getVariableInstancesLocal() {
    CoreVariableStore<T> variableStore = getVariableStore();
    return new HashMap<String, T>(variableStore.getVariableInstances());
  }

  public Map<String, T> getVariableInstances(Collection<String> variableNames) {
    return collectVariableInstances(new HashMap<String, T>(), variableNames);
  }

  public Map<String, T> collectVariableInstances(Map<String, T> variables, Collection<String> variableNames) {
    Map<String, T> variableInstances = getVariableInstancesLocal();
    for (T variable : variableInstances.values()) {
      if(!variables.containsKey(variable.getName())
         && (variableNames == null || variableNames.contains(variable.getName()))) {
        variables.put(variable.getName(), variable);
      }
    }
    CoreVariableScope<T> parentScope = getParentVariableScope();
    if(parentScope != null && (variableNames == null || !variables.keySet().equals(variableNames))) {
      parentScope.collectVariableInstances(variables, variableNames);
    }
    return variables;
  }

  protected Map<String, Object> collectVariables(HashMap<String, Object> variables) {
    CoreVariableScope<T> parentScope = getParentVariableScope();
    if (parentScope!=null) {
      variables.putAll(parentScope.collectVariables(variables));
    }
    for (CoreVariableInstance variableInstance: getVariableStore().getVariableInstancesValues()) {
      variables.put(variableInstance.getName(), variableInstance.getValue());
    }
    return variables;
  }

  public Object getVariable(String variableName) {
    T variableInstance = getVariableInstance(variableName);
    if (variableInstance != null) {
      return variableInstance.getValue();
    }
    return null;
  }

  public Object getVariableLocal(String variableName) {
    T variableInstance = getVariableInstanceLocal(variableName);
    if (variableInstance != null) {
      return variableInstance.getValue();
    }
    return null;
  }

  public T getVariableInstance(String variableName) {
    T variableInstance = getVariableInstanceLocal(variableName);
    if (variableInstance!=null) {
      return variableInstance;
    }
    CoreVariableScope<T> parentScope = getParentVariableScope();
    if (parentScope!=null) {
      return parentScope.getVariableInstance(variableName);
    }
    return null;
  }

  public T getVariableInstanceLocal(String name) {
    return getVariableStore().getVariableInstance(name);
  }

  public boolean hasVariables() {
    if (!getVariableStore().isEmpty()) {
      return true;
    }
    CoreVariableScope<T> parentScope = getParentVariableScope();
    return parentScope != null && parentScope.hasVariables();
  }

  public boolean hasVariablesLocal() {
    return !getVariableStore().isEmpty();
  }

  public boolean hasVariable(String variableName) {
    if (hasVariableLocal(variableName)) {
      return true;
    }
    CoreVariableScope<T> parentScope = getParentVariableScope();
    return parentScope != null && parentScope.hasVariable(variableName);
  }

  public boolean hasVariableLocal(String variableName) {
    return getVariableStore().containsVariableInstance(variableName);
  }

  protected Set<String> collectVariableNames(Set<String> variableNames) {
    CoreVariableScope<T> parentScope = getParentVariableScope();
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

  protected void setVariable(String variableName, Object value, CoreVariableScope<T> sourceActivityExecution) {
    if (hasVariableLocal(variableName)) {
      setVariableLocal(variableName, value, sourceActivityExecution);
      return;
    }
    CoreVariableScope<T> parentVariableScope = getParentVariableScope();
    if (parentVariableScope!=null) {
      if (sourceActivityExecution==null) {
        parentVariableScope.setVariable(variableName, value);
      } else {
        parentVariableScope.setVariable(variableName, value, sourceActivityExecution);
      }
      return;
    }
    setVariableLocal(variableName, value, sourceActivityExecution);
  }

  public void setVariableLocal(String variableName, Object value, CoreVariableScope<T> sourceActivityExecution) {
    getVariableStore().createOrUpdateVariable(variableName, value, sourceActivityExecution);
  }

  public void setVariableLocal(String variableName, Object value) {
    getVariableStore().createOrUpdateVariable(variableName, value, getSourceActivityVariableScope());

  }

  public void removeVariable(String variableName) {
    removeVariable(variableName, getSourceActivityVariableScope());
  }

  protected void removeVariable(String variableName, CoreVariableScope<T> sourceActivityExecution) {
    if (getVariableStore().containsVariableInstance(variableName)) {
      removeVariableLocal(variableName);
      return;
    }
    CoreVariableScope<T> parentVariableScope = getParentVariableScope();
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

  protected CoreVariableScope<T> getSourceActivityVariableScope() {
    return this;
  }

  protected void removeVariableLocal(String variableName, CoreVariableScope<T> sourceActivityExecution) {
    getVariableStore().removeVariableInstance(variableName, sourceActivityExecution);
  }

  public ELContext getCachedElContext() {
    return cachedElContext;
  }
  public void setCachedElContext(ELContext cachedElContext) {
    this.cachedElContext = cachedElContext;
  }

  public void dispatchEvent(VariableEvent variableEvent) {
    // default implementation does nothing
  }
}
