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
import java.util.Set;

import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.variable.CoreVariableInstance;
import org.camunda.bpm.engine.impl.core.variable.event.VariableEvent;
import org.camunda.bpm.engine.impl.core.variable.event.VariableEventDispatcher;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.camunda.bpm.engine.impl.javax.el.ELContext;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.camunda.bpm.engine.impl.persistence.entity.util.TypedValueField;
import org.camunda.bpm.engine.impl.variable.serializer.TypedValueSerializer;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.camunda.bpm.engine.variable.value.SerializableValue;
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
    return getVariableStore().getVariable(name);
  }

  public List<CoreVariableInstance> getVariableInstancesLocal() {
    return getVariableStore().getVariables();
  }

  public List<CoreVariableInstance> getVariableInstancesLocal(Collection<String> variableNames) {
    return getVariableStore().getVariables(variableNames);
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

  public Set<String> getVariableNames() {
    return collectVariableNames(new HashSet<String>());
  }

  public Set<String> getVariableNamesLocal() {
    return getVariableStore().getKeys();
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
    for (CoreVariableInstance variableInstance : getVariableStore().getVariables()) {
      invokeVariableLifecycleListenersDelete(variableInstance, getSourceActivityVariableScope());
    }

    getVariableStore().removeVariables();
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
      if (value.isTransient()) {
        throw ProcessEngineLogger.CORE_LOGGER.transientVariableException(variableName);
      }
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
    if (value.isTransient()) {
      setVariableLocalTransient(variableName, value, sourceActivityVariableScope);
    } else {
      setVariableLocal(variableName, value, sourceActivityVariableScope);
    }
  }

  public void setVariableLocal(String variableName, TypedValue value, AbstractVariableScope sourceActivityExecution) {

    checkJavaSerialization(variableName, value);

    VariableStore<CoreVariableInstance> variableStore = getVariableStore();

    if (variableStore.containsKey(variableName)) {
      CoreVariableInstance existingInstance = variableStore.getVariable(variableName);
      existingInstance.setValue(value);
      invokeVariableLifecycleListenersUpdate(existingInstance, sourceActivityExecution);
    }
    else if (variableStore.isRemoved(variableName)) {

      CoreVariableInstance existingInstance = variableStore.getRemovedVariable(variableName);

      existingInstance.setValue(value);
      getVariableStore().addVariable(existingInstance);
      invokeVariableLifecycleListenersUpdate(existingInstance, sourceActivityExecution);

      DbEntityManager dbEntityManager = Context.getCommandContext().getDbEntityManager();
      dbEntityManager.undoDelete((VariableInstanceEntity) existingInstance);
    }
    else {
      CoreVariableInstance variableValue = getVariableInstanceFactory().build(variableName, value, false);
      getVariableStore().addVariable(variableValue);
      invokeVariableLifecycleListenersCreate(variableValue, sourceActivityExecution);
    }
  }

  /**
   * Checks, if Java serialization will be used and if it is allowed to be used.
   * @param variableName
   * @param value
   */
  protected void checkJavaSerialization(String variableName, TypedValue value) {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    if (value instanceof SerializableValue && !processEngineConfiguration.isJavaSerializationFormatEnabled()) {

      SerializableValue serializableValue = (SerializableValue) value;

      // if Java serialization is prohibited
      if (!serializableValue.isDeserialized()) {

        String javaSerializationDataFormat = Variables.SerializationDataFormats.JAVA.getName();
        String requestedDataFormat = serializableValue.getSerializationDataFormat();

        if (requestedDataFormat == null) {
          // check if Java serializer will be used
          final TypedValueSerializer serializerForValue = TypedValueField.getSerializers()
              .findSerializerForValue(serializableValue, processEngineConfiguration.getFallbackSerializerFactory());
          if (serializerForValue != null) {
            requestedDataFormat = serializerForValue.getSerializationDataformat();
          }
        }

        if (javaSerializationDataFormat.equals(requestedDataFormat)) {
          throw ProcessEngineLogger.CORE_LOGGER.javaSerializationProhibitedException(variableName);
        }
      }
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

  public void setVariableLocal(String variableName, Object value) {
    TypedValue typedValue = Variables.untypedValue(value);
    setVariableLocal(variableName, typedValue, getSourceActivityVariableScope());

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

    checkJavaSerialization(variableName, typedValue);

    CoreVariableInstance coreVariableInstance = getVariableInstanceFactory().build(variableName, typedValue, true);
    getVariableStore().addVariable(coreVariableInstance);
  }

  public void setVariableLocalTransient(String variableName, Object value, AbstractVariableScope sourceActivityVariableScope) {
    VariableStore<CoreVariableInstance> variableStore = getVariableStore();
    setVariableLocalTransient(variableName, value);
    invokeVariableLifecycleListenersCreate(variableStore.getVariable(variableName), sourceActivityVariableScope);
  }

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

  public void dispatchEvent(VariableEvent variableEvent) {
    // default implementation does nothing
  }

}
