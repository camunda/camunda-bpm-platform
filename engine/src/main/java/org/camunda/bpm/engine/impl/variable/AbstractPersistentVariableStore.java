/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.engine.impl.variable;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.delegate.PersistentVariableInstance;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.variable.CoreVariableScope;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.camunda.bpm.engine.impl.history.producer.HistoryEventProducer;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;

/**
 * @author Sebastian Menski
 */
public abstract class AbstractPersistentVariableStore extends AbstractVariableStore<PersistentVariableInstance> implements Serializable {

  private static final long serialVersionUID = 1L;

  protected Map<String, VariableInstanceEntity> variableInstances = null;

  protected abstract List<VariableInstanceEntity> loadVariableInstances();
  protected abstract void initializeVariableInstanceBackPointer(VariableInstanceEntity variableInstance);

  public void ensureVariableInstancesInitialized() {
    if (variableInstances==null) {
      variableInstances = new HashMap<String, VariableInstanceEntity>();
      CommandContext commandContext = Context.getCommandContext();
      ensureNotNull("lazy loading outside command context", "commandContext", commandContext);
      List<VariableInstanceEntity> variableInstancesList = loadVariableInstances();
      for (VariableInstanceEntity variableInstance : variableInstancesList) {
        variableInstances.put(variableInstance.getName(), variableInstance);
      }
    }
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Map<String, PersistentVariableInstance> getVariableInstances() {
    ensureVariableInstancesInitialized();
    return (Map) variableInstances;
  }

  public void setVariableInstances(Map<String, VariableInstanceEntity> variableInstances) {
    this.variableInstances = variableInstances;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Collection<PersistentVariableInstance> getVariableInstancesValues() {
    ensureVariableInstancesInitialized();
    return (Collection) variableInstances.values();
  }

  public PersistentVariableInstance getVariableInstance(String variableName) {
    ensureVariableInstancesInitialized();
    return variableInstances.get(variableName);
  }

  public Set<String> getVariableNames() {
    ensureVariableInstancesInitialized();
    return variableInstances.keySet();
  }

  public boolean isEmpty() {
    ensureVariableInstancesInitialized();
    return variableInstances.isEmpty();
  }

  public boolean containsVariableInstance(String variableName) {
    ensureVariableInstancesInitialized();
    return variableInstances.containsKey(variableName);
  }

  public PersistentVariableInstance removeVariableInstance(String variableName, CoreVariableScope<PersistentVariableInstance> sourceActivityExecution) {
    ensureVariableInstancesInitialized();
    VariableInstanceEntity variable = variableInstances.remove(variableName);
    if(variable != null) {
      variable.delete();
      variable.setValue(null);

      if(isAutoFireHistoryEvents()) {
        fireHistoricVariableInstanceDelete(variable, sourceActivityExecution);
      }
    }
    return variable;
  }

  public void setVariableInstanceValue(PersistentVariableInstance variableInstance, Object value, CoreVariableScope<PersistentVariableInstance> sourceActivityExecution) {
    VariableInstanceEntity variableInstanceEntity = (VariableInstanceEntity) variableInstance;

    if (!canStoreValue(variableInstanceEntity, value)) {
      clearForNewValue(variableInstanceEntity, value);
    }

    variableInstanceEntity.setValue(value);

    // fire UPDATE event
    if(isAutoFireHistoryEvents()) {
      fireHistoricVariableInstanceUpdate(variableInstanceEntity, sourceActivityExecution);
    }
  }

  protected boolean canStoreValue(VariableInstanceEntity variableInstance, Object value) {
    return variableInstance.isAbleToStore(value);
  }

  public VariableInstanceEntity createVariableInstance(String variableName, Object value, CoreVariableScope<PersistentVariableInstance> sourceActivityExecution) {
    VariableType type = getNewVariableType(value);

    // create variable instance
    VariableInstanceEntity variableInstance = VariableInstanceEntity.createAndInsert(variableName, type, value);
    initializeVariableInstanceBackPointer(variableInstance);
    variableInstances.put(variableName, variableInstance);

    // fire CREATE event
    if(isAutoFireHistoryEvents()) {
      fireHistoricVariableInstanceCreate(variableInstance, sourceActivityExecution);
    }

    return variableInstance;
  }

  protected boolean isAutoFireHistoryEvents() {
    return true;
  }

  protected VariableType getNewVariableType(Object newValue) {
    // fetch available variable types
    VariableTypes variableTypes = Context
        .getProcessEngineConfiguration()
        .getVariableTypes();

    // get the corresponding variable type for the new value
    return variableTypes.findVariableType(newValue);
  }

  public void clearForNewValue(PersistentVariableInstance variableInstance, Object newValue) {
    VariableInstanceEntity variableInstanceEntity = (VariableInstanceEntity) variableInstance;

    VariableType oldType = variableInstanceEntity.getType();
    VariableType newType = getNewVariableType(newValue);

    // clear the variable only if the types are different
    if (oldType.getTypeName().equals(newType.getTypeName())) {
      return;
    }

    if (variableInstanceEntity.getByteArrayValueId() == null) {
      // reset the current value temporarily to null
      variableInstanceEntity.setValue(null);
    } else {
      // the type has changed from (SerializableType||ByteArrayType) -> another type:
      // the next apparently useless line is probably to ensure consistency in the DbSqlSession
      // cache, but should be checked and docced here (or removed if it turns out to be unnecessary)
      variableInstanceEntity.getByteArrayValue();
      Context
        .getCommandContext()
        .getByteArrayManager()
        .deleteByteArrayById(variableInstanceEntity.getByteArrayValueId());
      variableInstanceEntity.setByteArrayValueId(null);
    }

    // set the new type
    variableInstanceEntity.setType(newType);
  }

  public void removeVariablesWithoutFiringEvents() {
    ensureVariableInstancesInitialized();
    for (VariableInstanceEntity variable : variableInstances.values()) {
      variable.delete();
    }
    variableInstances.clear();
  }

  public void fireHistoricVariableInstanceDelete(VariableInstanceEntity variableInstance, CoreVariableScope<PersistentVariableInstance> sourceActivityExecution) {

    int historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
    if (historyLevel >=ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {

      final ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
      final HistoryEventHandler eventHandler = processEngineConfiguration.getHistoryEventHandler();
      final HistoryEventProducer eventProducer = processEngineConfiguration.getHistoryEventProducer();

      HistoryEvent evt = eventProducer.createHistoricVariableDeleteEvt(variableInstance, sourceActivityExecution);
      eventHandler.handleEvent(evt);

    }

  }

  public void fireHistoricVariableInstanceCreate(VariableInstanceEntity variableInstance, CoreVariableScope<PersistentVariableInstance> sourceActivityExecution) {

    int historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
    if (historyLevel >=ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {

      final ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
      final HistoryEventHandler eventHandler = processEngineConfiguration.getHistoryEventHandler();
      final HistoryEventProducer eventProducer = processEngineConfiguration.getHistoryEventProducer();

      HistoryEvent evt = eventProducer.createHistoricVariableCreateEvt(variableInstance, sourceActivityExecution);
      eventHandler.handleEvent(evt);

    }

  }

  public void fireHistoricVariableInstanceUpdate(VariableInstanceEntity variableInstance, CoreVariableScope<PersistentVariableInstance> sourceActivityExecution) {

    int historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
    if (historyLevel >=ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {

      final ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
      final HistoryEventHandler eventHandler = processEngineConfiguration.getHistoryEventHandler();
      final HistoryEventProducer eventProducer = processEngineConfiguration.getHistoryEventProducer();

      HistoryEvent evt = eventProducer.createHistoricVariableUpdateEvt(variableInstance, sourceActivityExecution);
      eventHandler.handleEvent(evt);

    }

  }

}
