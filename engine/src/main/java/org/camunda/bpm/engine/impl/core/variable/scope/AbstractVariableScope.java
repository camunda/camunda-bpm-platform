/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.Set;

import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.variable.CoreVariableInstance;
import org.camunda.bpm.engine.impl.core.variable.VariableUtil;
import org.camunda.bpm.engine.impl.core.variable.event.VariableEvent;
import org.camunda.bpm.engine.impl.core.variable.event.VariableEventDispatcher;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.camunda.bpm.engine.impl.javax.el.ELContext;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
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

  protected abstract VariableStore<CoreVariableInstance> getVariableStore();
  protected abstract VariableInstanceFactory<CoreVariableInstance> getVariableInstanceFactory();
  protected abstract List<VariableInstanceLifecycleListener<CoreVariableInstance>> getVariableInstanceLifecycleListeners();

  public abstract AbstractVariableScope getParentVariableScope();

  public void initializeVariableStore(Map<String, Object> variables) {
    for (String variableName : variables.keySet()) {
      TypedValue value = Variables.untypedValue(variables.get(variableName));
      CoreVariableInstance variableValue = getVariableInstanceFactory().build(variableName, value, false);
      getVariableStore().addVariable(variableValue);
    }
  }

  // get variable map /////////////////////////////////////////


  @Override
  public String getVariableScopeKey() {
    return "scope";
  }

  @Override
  public VariableMapImpl getVariables() {
    return getVariablesTyped();
  }

  @Override
  public VariableMapImpl getVariablesTyped() {
    return getVariablesTyped(true);
  }

  @Override
  public VariableMapImpl getVariablesTyped(boolean deserializeValues) {
    VariableMapImpl variableMap = new VariableMapImpl();
    collectVariables(variableMap, null, false, deserializeValues);
    return variableMap;
  }

  @Override
  public VariableMapImpl getVariablesLocal() {
    return getVariablesLocalTyped();
  }

  @Override
  public VariableMapImpl getVariablesLocalTyped() {
    return getVariablesLocalTyped(true);
  }

  @Override
  public VariableMapImpl getVariablesLocalTyped(boolean deserializeObjectValues) {
    VariableMapImpl variables = new VariableMapImpl();
    collectVariables(variables, null, true, deserializeObjectValues);
    return variables;
  }

  public void collectVariables(VariableMapImpl resultVariables, Collection<String> variableNames, boolean isLocal, boolean deserializeValues) {
    boolean collectAll = (variableNames == null);

    List<CoreVariableInstance> localVariables = getVariableInstancesLocal(variableNames);
    for (CoreVariableInstance var : localVariables) {
      if(!resultVariables.containsKey(var.getName())
         && (collectAll || variableNames.contains(var.getName()))) {
        resultVariables.put(var.getName(), var.getTypedValue(deserializeValues));
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

  @Override
  public Object getVariable(String variableName) {
    return getVariable(variableName, true);
  }

  public Object getVariable(String variableName, boolean deserializeObjectValue) {
    return getValueFromVariableInstance(deserializeObjectValue, getVariableInstance(variableName));
  }

  @Override
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

  @Override
  public <T extends TypedValue> T getVariableTyped(String variableName) {
    return getVariableTyped(variableName, true);
  }

  @Override
  public <T extends TypedValue> T getVariableTyped(String variableName, boolean deserializeValue) {
    return getTypedValueFromVariableInstance(deserializeValue, getVariableInstance(variableName));
  }

  @Override
  public <T extends TypedValue> T getVariableLocalTyped(String variableName) {
    return getVariableLocalTyped(variableName, true);
  }

  @Override
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
    return getVariableStore().getVariable(name);
  }

  public List<CoreVariableInstance> getVariableInstancesLocal() {
    return getVariableStore().getVariables();
  }

  public List<CoreVariableInstance> getVariableInstancesLocal(Collection<String> variableNames) {
    return getVariableStore().getVariables(variableNames);
  }

  @Override
  public boolean hasVariables() {
    if (!getVariableStore().isEmpty()) {
      return true;
    }
    AbstractVariableScope parentScope = getParentVariableScope();
    return parentScope != null && parentScope.hasVariables();
  }

  @Override
  public boolean hasVariablesLocal() {
    return !getVariableStore().isEmpty();
  }

  @Override
  public boolean hasVariable(String variableName) {
    if (hasVariableLocal(variableName)) {
      return true;
    }
    AbstractVariableScope parentScope = getParentVariableScope();
    return parentScope != null && parentScope.hasVariable(variableName);
  }

  @Override
  public boolean hasVariableLocal(String variableName) {
    return getVariableStore().containsKey(variableName);
  }

  protected Set<String> collectVariableNames(Set<String> variableNames) {
    AbstractVariableScope parentScope = getParentVariableScope();
    if (parentScope!=null) {
      variableNames.addAll(parentScope.collectVariableNames(variableNames));
    }
    for (CoreVariableInstance variableInstance: getVariableStore().getVariables()) {
      variableNames.add(variableInstance.getName());
    }
    return variableNames;
  }

  @Override
  public Set<String> getVariableNames() {
    return collectVariableNames(new HashSet<String>());
  }

  @Override
  public Set<String> getVariableNamesLocal() {
    return getVariableStore().getKeys();
  }

  public void setVariables(Map<String, ?> variables, boolean skipJavaSerializationFormatCheck) {
    VariableUtil.setVariables(variables,
        (name, value) -> setVariable(name, value, skipJavaSerializationFormatCheck));
  }

  @Override
  public void setVariables(Map<String, ?> variables) {
    setVariables(variables, false);
  }

  public void setVariablesLocal(Map<String, ?> variables, boolean skipJavaSerializationFormatCheck) {
    VariableUtil.setVariables(variables,
        (name, value) -> setVariableLocal(name, value, skipJavaSerializationFormatCheck));
  }

  @Override
  public void setVariablesLocal(Map<String, ?> variables) {
    setVariablesLocal(variables, false);
  }

  @Override
  public void removeVariables() {
    for (CoreVariableInstance variableInstance : getVariableStore().getVariables()) {
      invokeVariableLifecycleListenersDelete(variableInstance, getSourceActivityVariableScope());
    }

    getVariableStore().removeVariables();
  }

  @Override
  public void removeVariablesLocal() {
    List<String> variableNames = new ArrayList<>(getVariableNamesLocal());
    for (String variableName: variableNames) {
      removeVariableLocal(variableName);
    }
  }

  @Override
  public void removeVariables(Collection<String> variableNames) {
    if (variableNames != null) {
      for (String variableName : variableNames) {
        removeVariable(variableName);
      }
    }
  }

  @Override
  public void removeVariablesLocal(Collection<String> variableNames) {
    if (variableNames != null) {
      for (String variableName : variableNames) {
        removeVariableLocal(variableName);
      }
    }
  }

  public void setVariable(String variableName, Object value, boolean skipJavaSerializationFormatCheck) {
    TypedValue typedValue = Variables.untypedValue(value);
    setVariable(variableName, typedValue, getSourceActivityVariableScope(), skipJavaSerializationFormatCheck);
  }

  @Override
  public void setVariable(String variableName, Object value) {
    setVariable(variableName, value, false);
  }

  protected void setVariable(String variableName,
                             TypedValue value,
                             AbstractVariableScope sourceActivityVariableScope,
                             boolean skipJavaSerializationFormatCheck) {
    if (hasVariableLocal(variableName)) {
      setVariableLocal(variableName, value, sourceActivityVariableScope, skipJavaSerializationFormatCheck);
      return;
    }
    AbstractVariableScope parentVariableScope = getParentVariableScope();
    if (parentVariableScope!=null) {
      if (sourceActivityVariableScope==null) {
        parentVariableScope.setVariable(variableName, value, skipJavaSerializationFormatCheck);
      } else {
        parentVariableScope.setVariable(variableName, value, sourceActivityVariableScope, skipJavaSerializationFormatCheck);
      }
      return;
    }

    setVariableLocal(variableName, value, sourceActivityVariableScope, skipJavaSerializationFormatCheck);
  }

  protected void setVariable(String variableName,
                             TypedValue value,
                             AbstractVariableScope sourceActivityVariableScope) {
    setVariable(variableName, value, sourceActivityVariableScope, false);
  }

  public void setVariableLocal(String variableName,
                               TypedValue value,
                               AbstractVariableScope sourceActivityExecution,
                               boolean skipJavaSerializationFormatCheck) {

    if (!skipJavaSerializationFormatCheck) {
      VariableUtil.checkJavaSerialization(variableName, value);
    }

    VariableStore<CoreVariableInstance> variableStore = getVariableStore();

    if (variableStore.containsKey(variableName)) {
      CoreVariableInstance existingInstance = variableStore.getVariable(variableName);

      TypedValue previousValue = existingInstance.getTypedValue(false);

      if (value.isTransient() != previousValue.isTransient()) {
        throw ProcessEngineLogger.CORE_LOGGER.transientVariableException(variableName);
      }

      existingInstance.setValue(value);
      invokeVariableLifecycleListenersUpdate(existingInstance, sourceActivityExecution);
    }
    else if (variableStore.isRemoved(variableName)) {

      CoreVariableInstance existingInstance = variableStore.getRemovedVariable(variableName);

      TypedValue previousValue = existingInstance.getTypedValue(false);

      if (value.isTransient() != previousValue.isTransient()) {
        throw ProcessEngineLogger.CORE_LOGGER.transientVariableException(variableName);
      }

      existingInstance.setValue(value);
      getVariableStore().addVariable(existingInstance);
      invokeVariableLifecycleListenersUpdate(existingInstance, sourceActivityExecution);

      if (!value.isTransient()) {
        DbEntityManager dbEntityManager = Context.getCommandContext().getDbEntityManager();
        dbEntityManager.undoDelete((VariableInstanceEntity) existingInstance);
      }
    }
    else {
      CoreVariableInstance variableValue = getVariableInstanceFactory().build(variableName, value, value.isTransient());
      getVariableStore().addVariable(variableValue);
      invokeVariableLifecycleListenersCreate(variableValue, sourceActivityExecution);
    }
  }

  protected void invokeVariableLifecycleListenersCreate(CoreVariableInstance variableInstance, AbstractVariableScope sourceScope) {
    invokeVariableLifecycleListenersCreate(variableInstance, sourceScope, getVariableInstanceLifecycleListeners());
  }

  protected void invokeVariableLifecycleListenersCreate(CoreVariableInstance variableInstance, AbstractVariableScope sourceScope,
      List<VariableInstanceLifecycleListener<CoreVariableInstance>> lifecycleListeners) {
    for (VariableInstanceLifecycleListener<CoreVariableInstance> lifecycleListener : lifecycleListeners) {
      lifecycleListener.onCreate(variableInstance, sourceScope);
    }
  }

  protected void invokeVariableLifecycleListenersDelete(CoreVariableInstance variableInstance, AbstractVariableScope sourceScope) {
    invokeVariableLifecycleListenersDelete(variableInstance, sourceScope, getVariableInstanceLifecycleListeners());
  }

  protected void invokeVariableLifecycleListenersDelete(CoreVariableInstance variableInstance, AbstractVariableScope sourceScope,
      List<VariableInstanceLifecycleListener<CoreVariableInstance>> lifecycleListeners) {
    for (VariableInstanceLifecycleListener<CoreVariableInstance> lifecycleListener : lifecycleListeners) {
      lifecycleListener.onDelete(variableInstance, sourceScope);
    }
  }

  protected void invokeVariableLifecycleListenersUpdate(CoreVariableInstance variableInstance, AbstractVariableScope sourceScope) {
    invokeVariableLifecycleListenersUpdate(variableInstance, sourceScope, getVariableInstanceLifecycleListeners());
  }

  protected void invokeVariableLifecycleListenersUpdate(CoreVariableInstance variableInstance, AbstractVariableScope sourceScope,
      List<VariableInstanceLifecycleListener<CoreVariableInstance>> lifecycleListeners) {
    for (VariableInstanceLifecycleListener<CoreVariableInstance> lifecycleListener : lifecycleListeners) {
      lifecycleListener.onUpdate(variableInstance, sourceScope);
    }
  }

  public void setVariableLocal(String variableName, Object value, boolean skipJavaSerializationFormatCheck) {
    TypedValue typedValue = Variables.untypedValue(value);
    setVariableLocal(variableName, typedValue, getSourceActivityVariableScope(), skipJavaSerializationFormatCheck);
  }

  @Override
  public void setVariableLocal(String variableName, Object value) {
    setVariableLocal(variableName, value, false);
  }

  @Override
  public void removeVariable(String variableName) {
    removeVariable(variableName, getSourceActivityVariableScope());
  }

  protected void removeVariable(String variableName, AbstractVariableScope sourceActivityExecution) {
    if (getVariableStore().containsKey(variableName)) {
      removeVariableLocal(variableName, sourceActivityExecution);
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

  @Override
  public void removeVariableLocal(String variableName) {
    removeVariableLocal(variableName, getSourceActivityVariableScope());
  }

  protected AbstractVariableScope getSourceActivityVariableScope() {
    return this;
  }

  protected void removeVariableLocal(String variableName, AbstractVariableScope sourceActivityExecution) {

    if (getVariableStore().containsKey(variableName)) {
      CoreVariableInstance variableInstance = getVariableStore().getVariable(variableName);

      invokeVariableLifecycleListenersDelete(variableInstance, sourceActivityExecution);
      getVariableStore().removeVariable(variableName);
    }

  }

  public ELContext getCachedElContext() {
    return cachedElContext;
  }
  public void setCachedElContext(ELContext cachedElContext) {
    this.cachedElContext = cachedElContext;
  }

  @Override
  public void dispatchEvent(VariableEvent variableEvent) {
    // default implementation does nothing
  }

}
