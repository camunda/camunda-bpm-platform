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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.delegate.VariableListener;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.variable.CoreVariableInstance;
import org.camunda.bpm.engine.impl.core.variable.event.VariableEvent;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableStore;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.camunda.bpm.engine.impl.history.producer.HistoryEventProducer;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Sebastian Menski
 */
public abstract class AbstractPersistentVariableStore extends AbstractVariableStore {

  protected static final EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

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
  public Map<String, CoreVariableInstance> getVariableInstances() {
    ensureVariableInstancesInitialized();
    return (Map) variableInstances;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Map<String, CoreVariableInstance> getVariableInstancesDirect() {
    return (Map) variableInstances;
  }

  public void setVariableInstances(Map<String, VariableInstanceEntity> variableInstances) {
    this.variableInstances = variableInstances;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Collection<CoreVariableInstance> getVariableInstancesValues() {
    ensureVariableInstancesInitialized();
    return (Collection) variableInstances.values();
  }

  public CoreVariableInstance getVariableInstance(String variableName) {
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

  public CoreVariableInstance removeVariableInstance(String variableName, AbstractVariableScope sourceActivityExecution) {
    ensureVariableInstancesInitialized();
    VariableInstanceEntity variable = variableInstances.remove(variableName);

    if(variable != null) {
      variable.incrementSequenceCounter();
      variable.delete();

      // fire DELETE event
      if(isAutoFireHistoryEvents()) {
        fireHistoricVariableInstanceDelete(variable, sourceActivityExecution);
      }
      fireVariableEvent(variable, VariableListener.DELETE, sourceActivityExecution);
    }
    return variable;
  }

  @Override
  public void setVariableValue(CoreVariableInstance variableInstance, TypedValue value, AbstractVariableScope sourceActivityExecution) {
    VariableInstanceEntity variableInstanceEntity = (VariableInstanceEntity) variableInstance;

    if(variableInstanceEntity.isTransient()) {
      throw LOG.updateTransientVariableException(variableInstanceEntity.getName());
    }

    variableInstanceEntity.setValue(value);
    variableInstanceEntity.incrementSequenceCounter();

    // fire UPDATE event
    if(isAutoFireHistoryEvents()) {
      fireHistoricVariableInstanceUpdate(variableInstanceEntity, sourceActivityExecution);
    }
    fireVariableEvent(variableInstanceEntity, VariableListener.UPDATE, sourceActivityExecution);
  }

  @Override
  public CoreVariableInstance createVariableInstance(String variableName, TypedValue value,
      AbstractVariableScope sourceActivityExecution) {

    // create variable instance
    VariableInstanceEntity variableInstance = VariableInstanceEntity.createAndInsert(variableName, value);
    initializeVariableInstanceBackPointer(variableInstance);
    variableInstances.put(variableName, variableInstance);

    // fire CREATE event
    if(isAutoFireHistoryEvents()) {
      fireHistoricVariableInstanceCreate(variableInstance, sourceActivityExecution);
    }
    fireVariableEvent(variableInstance, VariableListener.CREATE, sourceActivityExecution);

    return variableInstance;
  }

  protected boolean isAutoFireHistoryEvents() {
    return true;
  }

  public void removeVariablesWithoutFiringEvents() {
    ensureVariableInstancesInitialized();
    for (VariableInstanceEntity variable : variableInstances.values()) {
      variable.delete();
    }
    variableInstances.clear();
  }

  public static void fireHistoricVariableInstanceDelete(VariableInstanceEntity variableInstance, AbstractVariableScope sourceActivityExecution) {

    HistoryLevel historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
    if (historyLevel.isHistoryEventProduced(HistoryEventTypes.VARIABLE_INSTANCE_DELETE, variableInstance)) {

      final ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
      final HistoryEventHandler eventHandler = processEngineConfiguration.getHistoryEventHandler();
      final HistoryEventProducer eventProducer = processEngineConfiguration.getHistoryEventProducer();

      HistoryEvent evt = eventProducer.createHistoricVariableDeleteEvt(variableInstance, sourceActivityExecution);
      eventHandler.handleEvent(evt);

    }

  }

  public static void fireHistoricVariableInstanceCreate(VariableInstanceEntity variableInstance, AbstractVariableScope sourceActivityExecution) {

    HistoryLevel historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
    if (historyLevel.isHistoryEventProduced(HistoryEventTypes.VARIABLE_INSTANCE_CREATE, variableInstance)) {

      final ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
      final HistoryEventHandler eventHandler = processEngineConfiguration.getHistoryEventHandler();
      final HistoryEventProducer eventProducer = processEngineConfiguration.getHistoryEventProducer();

      HistoryEvent evt = eventProducer.createHistoricVariableCreateEvt(variableInstance, sourceActivityExecution);
      eventHandler.handleEvent(evt);

    }

  }

  public static void fireHistoricVariableInstanceUpdate(VariableInstanceEntity variableInstance, AbstractVariableScope sourceActivityExecution) {
    HistoryLevel historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
    if (historyLevel.isHistoryEventProduced(HistoryEventTypes.VARIABLE_INSTANCE_UPDATE, variableInstance)) {

      final ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
      final HistoryEventHandler eventHandler = processEngineConfiguration.getHistoryEventHandler();
      final HistoryEventProducer eventProducer = processEngineConfiguration.getHistoryEventProducer();

      HistoryEvent evt = eventProducer.createHistoricVariableUpdateEvt(variableInstance, sourceActivityExecution);
      eventHandler.handleEvent(evt);

    }
  }

  protected static void fireVariableEvent(VariableInstanceEntity variableInstance, String eventName, AbstractVariableScope sourceActivityExecution) {
    sourceActivityExecution.dispatchEvent(new VariableEvent(variableInstance, eventName, sourceActivityExecution));
  }

  public void createTransientVariable(String variableName, TypedValue value, AbstractVariableScope sourceActivityExecution) {
    // only create the variable instance but do not insert it into the data base
    VariableInstanceEntity variableInstance = VariableInstanceEntity.create(variableName, value);
    variableInstance.setTransient(true);
    initializeVariableInstanceBackPointer(variableInstance);

    ensureVariableInstancesInitialized();
    variableInstances.put(variableName, variableInstance);
  }

}