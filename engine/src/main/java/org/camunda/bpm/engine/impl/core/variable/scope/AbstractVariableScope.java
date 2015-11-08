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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.core.variable.CoreVariableInstance;
import org.camunda.bpm.engine.impl.core.variable.event.VariableEvent;
import org.camunda.bpm.engine.impl.core.variable.event.VariableEventDispatcher;
import org.camunda.bpm.engine.impl.javax.el.ELContext;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Daniel Meyer
 * @author Roman Smirnov
 * @author Sebastian Menski
 *
 */
public abstract class AbstractVariableScope implements Serializable, VariableScope, VariableEventDispatcher {

  private static final long serialVersionUID = 1L;

  // TODO: move this?
  protected ELContext cachedElContext;

  protected abstract CoreVariableStore getVariableStore();

  public abstract AbstractVariableScope getParentVariableScope();

  // get variable map /////////////////////////////////////////


  public String getVariableScopeKey() {
    return "scope";
  }

  public VariableMapImpl getVariables() {
    return getVariablesTyped();
  }

  public VariableMapImpl getVariablesTyped() {
    return getVariablesTyped(true);
  }

  public VariableMapImpl getVariablesTyped(boolean deserializeValues) {
    VariableMapImpl variableMap = new VariableMapImpl();
    collectVariables(variableMap, null, false, deserializeValues);
    return variableMap;
  }

  public VariableMapImpl getVariablesLocal() {
    return getVariablesLocalTyped();
  }

  public VariableMapImpl getVariablesLocalTyped() {
    return getVariablesLocalTyped(true);
  }

  public VariableMapImpl getVariablesLocalTyped(boolean deserializeObjectValues) {
    VariableMapImpl variables = new VariableMapImpl();
    collectVariables(variables, null, true, deserializeObjectValues);
    return variables;
  }

  public void collectVariables(VariableMapImpl resultVariables, Collection<String> variableNames, boolean isLocal, boolean deserializeValues) {
    boolean collectAll = (variableNames == null);

    Map<String, CoreVariableInstance> localVariables = getVariableInstancesLocal();
    for (Entry<String, CoreVariableInstance> var : localVariables.entrySet()) {
      if(!resultVariables.containsKey(var.getKey())
         && (collectAll || variableNames.contains(var.getKey()))) {
        resultVariables.put(var.getKey(), var.getValue().getTypedValue(deserializeValues));
      }
    }
    if(!isLocal) {
      AbstractVariableScope parentScope = getParentVariableScope();
      // Do not propagate to parent if all variables in 'variableNames' are already collected!
      if(parentScope != null && (collectAll || !resultVariables.keySet().equals(variableNames))) {
        parentScope.collectVariables(resultVariables, variableNames, isLocal, deserializeValues);
      }
    }
  }

  // get single variable /////////////////////////////////////

  public Object getVariable(String variableName) {
    return getVariable(variableName, true);
  }

  public Object getVariable(String variableName, boolean deserializeObjectValue) {
    return getValueFromVariableInstance(deserializeObjectValue, getVariableInstance(variableName));
  }

  public Object getVariableLocal(String variableName) {
    return getVariableLocal(variableName, true);
  }

  public Object getVariableLocal(String variableName, boolean deserializeObjectValue) {
    return getValueFromVariableInstance(deserializeObjectValue, getVariableInstanceLocal(variableName));
  }

  protected Object getValueFromVariableInstance(boolean deserializeObjectValue, CoreVariableInstance variableInstance) {
    if(variableInstance != null) {
      TypedValue typedValue = variableInstance.getTypedValue(deserializeObjectValue);
      if (typedValue != null) {
        return typedValue.getValue();
      }
    }
    return null;
  }

  public <T extends TypedValue> T getVariableTyped(String variableName) {
    return getVariableTyped(variableName, true);
  }

  public <T extends TypedValue> T getVariableTyped(String variableName, boolean deserializeValue) {
    return getTypedValueFromVariableInstance(deserializeValue, getVariableInstance(variableName));
  }

  public <T extends TypedValue> T getVariableLocalTyped(String variableName) {
    return getVariableLocalTyped(variableName, true);
  }

  public <T extends TypedValue> T getVariableLocalTyped(String variableName, boolean deserializeValue) {
    return getTypedValueFromVariableInstance(deserializeValue, getVariableInstanceLocal(variableName));
  }

  @SuppressWarnings("unchecked")
  private <T extends TypedValue> T getTypedValueFromVariableInstance(boolean deserializeValue, CoreVariableInstance variableInstance) {
    if(variableInstance != null) {
      return (T) variableInstance.getTypedValue(deserializeValue);
    }
    else {
      return null;
    }
  }

  public CoreVariableInstance getVariableInstance(String variableName) {
    CoreVariableInstance variableInstance = getVariableInstanceLocal(variableName);
    if (variableInstance!=null) {
      return variableInstance;
    }
    AbstractVariableScope parentScope = getParentVariableScope();
    if (parentScope!=null) {
      return parentScope.getVariableInstance(variableName);
    }
    return null;
  }

  public CoreVariableInstance getVariableInstanceLocal(String name) {
    return getVariableStore().getVariableInstance(name);
  }

  public Map<String, CoreVariableInstance> getVariableInstancesLocal() {
    return getVariableStore().getVariableInstances();
  }

  public boolean hasVariables() {
    if (!getVariableStore().isEmpty()) {
      return true;
    }
    AbstractVariableScope parentScope = getParentVariableScope();
    return parentScope != null && parentScope.hasVariables();
  }

  public boolean hasVariablesLocal() {
    return !getVariableStore().isEmpty();
  }

  public boolean hasVariable(String variableName) {
    if (hasVariableLocal(variableName)) {
      return true;
    }
    AbstractVariableScope parentScope = getParentVariableScope();
    return parentScope != null && parentScope.hasVariable(variableName);
  }

  public boolean hasVariableLocal(String variableName) {
    return getVariableStore().containsVariableInstance(variableName);
  }

  protected Set<String> collectVariableNames(Set<String> variableNames) {
    AbstractVariableScope parentScope = getParentVariableScope();
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

  public Set<String> getVariableNamesLocal() {
    return getVariableStore().getVariableNames();
  }

  public void setVariables(Map<String, ? extends Object> variables) {
    if (variables!=null) {
      for (String variableName : variables.keySet()) {
        Object value = null;
        if (variables instanceof VariableMap) {
          value = ((VariableMap) variables).getValueTyped(variableName);
        }
        else {
          value = variables.get(variableName);
        }
        setVariable(variableName, value);
      }
    }
  }

  public void setVariablesLocal(Map<String, ? extends Object> variables) {
    if (variables!=null) {
      for (String variableName : variables.keySet()) {
        Object value = null;
        if (variables instanceof VariableMap) {
          value = ((VariableMap) variables).getValueTyped(variableName);
        }
        else {
          value = variables.get(variableName);
        }
        setVariableLocal(variableName, value);
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
    TypedValue typedValue = Variables.untypedValue(value);
    setVariable(variableName, typedValue, getSourceActivityVariableScope());
  }

  protected void setVariable(String variableName, TypedValue value, AbstractVariableScope sourceActivityVariableScope) {
    if (hasVariableLocal(variableName)) {
      setVariableLocal(variableName, value, sourceActivityVariableScope);
      return;
    }
    AbstractVariableScope parentVariableScope = getParentVariableScope();
    if (parentVariableScope!=null) {
      if (sourceActivityVariableScope==null) {
        parentVariableScope.setVariable(variableName, value);
      } else {
        parentVariableScope.setVariable(variableName, value, sourceActivityVariableScope);
      }
      return;
    }
    setVariableLocal(variableName, value, sourceActivityVariableScope);
  }

  public void setVariableLocal(String variableName, TypedValue value, AbstractVariableScope sourceActivityExecution) {
    getVariableStore().createOrUpdateVariable(variableName, value, sourceActivityExecution);
  }

  public void setVariableLocal(String variableName, Object value) {
    TypedValue typedValue = Variables.untypedValue(value);
    getVariableStore().createOrUpdateVariable(variableName, typedValue, getSourceActivityVariableScope());

  }

  /**
   * Sets a variable in the local scope. In contrast to
   * {@link #setVariableLocal(String, Object)}, the variable is transient that
   * means it will not be stored in the data base. For example, a transient
   * variable can be used for a result variable that is only available for
   * output mapping.
   */
  public void setVariableLocalTransient(String variableName, Object value) {
    TypedValue typedValue = Variables.untypedValue(value);
    getVariableStore().createTransientVariable(variableName, typedValue, getSourceActivityVariableScope());
  }

  public void removeVariable(String variableName) {
    removeVariable(variableName, getSourceActivityVariableScope());
  }

  protected void removeVariable(String variableName, AbstractVariableScope sourceActivityExecution) {
    if (getVariableStore().containsVariableInstance(variableName)) {
      removeVariableLocal(variableName);
      return;
    }
    AbstractVariableScope parentVariableScope = getParentVariableScope();
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

  protected AbstractVariableScope getSourceActivityVariableScope() {
    return this;
  }

  protected void removeVariableLocal(String variableName, AbstractVariableScope sourceActivityExecution) {
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
