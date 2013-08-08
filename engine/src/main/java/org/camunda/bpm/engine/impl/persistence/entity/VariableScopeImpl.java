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
package org.camunda.bpm.engine.impl.persistence.entity;

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
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.camunda.bpm.engine.impl.history.producer.HistoryEventProducer;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.javax.el.ELContext;
import org.camunda.bpm.engine.impl.variable.VariableType;
import org.camunda.bpm.engine.impl.variable.VariableTypes;



/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public abstract class VariableScopeImpl implements Serializable, VariableScope {
  
  private static final long serialVersionUID = 1L;
  
  protected Map<String, VariableInstanceEntity> variableInstances = null;
  
  protected ELContext cachedElContext;

  protected String id = null;

  protected abstract List<VariableInstanceEntity> loadVariableInstances();
  protected abstract VariableScopeImpl getParentVariableScope();
  protected abstract void initializeVariableInstanceBackPointer(VariableInstanceEntity variableInstance);

  protected void ensureVariableInstancesInitialized() {
    if (variableInstances==null) {
      variableInstances = new HashMap<String, VariableInstanceEntity>();
      CommandContext commandContext = Context.getCommandContext();
      if (commandContext == null) {
        throw new ProcessEngineException("lazy loading outside command context");
      }
      List<VariableInstanceEntity> variableInstancesList = loadVariableInstances();
      for (VariableInstanceEntity variableInstance : variableInstancesList) {
        variableInstances.put(variableInstance.getName(), variableInstance);
      }
    }
  }
  
  public Map<String, Object> getVariables() {
    return collectVariables(new HashMap<String, Object>());
  }
  
  protected Map<String, Object> collectVariables(HashMap<String, Object> variables) {
    ensureVariableInstancesInitialized();
    VariableScopeImpl parentScope = getParentVariableScope();
    if (parentScope!=null) {
      variables.putAll(parentScope.collectVariables(variables));
    }
    for (VariableInstanceEntity variableInstance: variableInstances.values()) {
      variables.put(variableInstance.getName(), variableInstance.getValue());
    }
    return variables;
  }
  
  public Object getVariable(String variableName) {
    ensureVariableInstancesInitialized();
    VariableInstanceEntity variableInstance = variableInstances.get(variableName);
    if (variableInstance!=null) {
      return variableInstance.getValue();
    }
    VariableScope parentScope = getParentVariableScope();
    if (parentScope!=null) {
      return parentScope.getVariable(variableName);
    }
    return null;
  }
  
  public Object getVariableLocal(String variableName) {
    ensureVariableInstancesInitialized();
    VariableInstanceEntity variableInstance = variableInstances.get(variableName);
    if (variableInstance!=null) {
      return variableInstance.getValue();
    }
    return null;
  }
  
  public boolean hasVariables() {
    ensureVariableInstancesInitialized();
    if (!variableInstances.isEmpty()) {
      return true;
    }
    VariableScope parentScope = getParentVariableScope();
    if (parentScope!=null) {
      return parentScope.hasVariables();
    }
    return false;
  }

  public boolean hasVariablesLocal() {
    ensureVariableInstancesInitialized();
    return !variableInstances.isEmpty();
  }

  public boolean hasVariable(String variableName) {
    if (hasVariableLocal(variableName)) {
      return true;
    }
    VariableScope parentScope = getParentVariableScope();
    if (parentScope!=null) {
      return parentScope.hasVariable(variableName);
    }
    return false;
  }

  public boolean hasVariableLocal(String variableName) {
    ensureVariableInstancesInitialized();
    return variableInstances.containsKey(variableName);
  }

  protected Set<String> collectVariableNames(Set<String> variableNames) {
    ensureVariableInstancesInitialized();
    VariableScopeImpl parentScope = getParentVariableScope();
    if (parentScope!=null) {
      variableNames.addAll(parentScope.collectVariableNames(variableNames));
    }
    for (VariableInstanceEntity variableInstance: variableInstances.values()) {
      variableNames.add(variableInstance.getName());
    }
    return variableNames;
  }

  public Set<String> getVariableNames() {
    return collectVariableNames(new HashSet<String>());
  }
  
  public Map<String, Object> getVariablesLocal() {
    Map<String, Object> variables = new HashMap<String, Object>();
    ensureVariableInstancesInitialized();
    for (VariableInstanceEntity variableInstance: variableInstances.values()) {
      variables.put(variableInstance.getName(), variableInstance.getValue());
    }
    return variables;
  }

  public Set<String> getVariableNamesLocal() {
    ensureVariableInstancesInitialized();
    return variableInstances.keySet();
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
    ensureVariableInstancesInitialized();
    Set<String> variableNames = new HashSet<String>(variableInstances.keySet());
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
  
  public void deleteVariablesInstanceForLeavingScope() {
    List<String> variableNames = new ArrayList<String>(getVariableNamesLocal());
    for (String variableName: variableNames) {
      ensureVariableInstancesInitialized();
      VariableInstanceEntity variableInstance = variableInstances.remove(variableName);
      if (variableInstance != null) {
        variableInstance.delete();

//        fireHistoricVariableInstanceDelete(variableInstance, (ExecutionEntity) this);
//        
//        fireHistoricVariableInstanceUpdate(variableInstance, (ExecutionEntity)this);
      }
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
    setVariable(variableName, value, getSourceActivityExecution());
  }

  protected void setVariable(String variableName, Object value, ExecutionEntity sourceActivityExecution) {
    if (hasVariableLocal(variableName)) {
      setVariableLocal(variableName, value, sourceActivityExecution);
      return;
    } 
    VariableScopeImpl parentVariableScope = getParentVariableScope();
    if (parentVariableScope!=null) {
      if (sourceActivityExecution==null) {
        parentVariableScope.setVariable(variableName, value);
      } else {
        parentVariableScope.setVariable(variableName, value, sourceActivityExecution);
      }
      return;
    }
    createVariableLocal(variableName, value);
  }

  public void setVariableLocal(String variableName, Object value) {
    setVariableLocal(variableName, value, getSourceActivityExecution());
  }

  protected void setVariableLocal(String variableName, Object value, ExecutionEntity sourceActivityExecution) {
    ensureVariableInstancesInitialized();
    VariableInstanceEntity variableInstance = variableInstances.get(variableName);
    if ((variableInstance != null) && (!variableInstance.getType().isAbleToStore(value))) {
      // delete variable
      removeVariable(variableName);
      variableInstance = null;
    }
    if (variableInstance == null) {
      createVariableLocal(variableName, value);
    } else {
      updateVariableInstance(variableInstance, value, sourceActivityExecution);
    }
  }
  
  protected void createVariableLocal(String variableName, Object value) {
    createVariableLocal(variableName, value, getSourceActivityExecution());
  }

  /** only called when a new variable is created on this variable scope.
   * This method is also responsible for propagating the creation of this 
   * variable to the history. */
  protected void createVariableLocal(String variableName, Object value, ExecutionEntity sourceActivityExecution) {
    ensureVariableInstancesInitialized();
    
    if (variableInstances.containsKey(variableName)) {
      throw new ProcessEngineException("variable '"+variableName+"' already exists. Use setVariableLocal if you want to overwrite the value");
    }
    
    createVariableInstance(variableName, value, sourceActivityExecution);
  }

  public void removeVariable(String variableName) {
    removeVariable(variableName, getSourceActivityExecution());
  }

  protected void removeVariable(String variableName, ExecutionEntity sourceActivityExecution) {
    ensureVariableInstancesInitialized();
    if (variableInstances.containsKey(variableName)) {
      removeVariableLocal(variableName);
      return;
    }
    VariableScopeImpl parentVariableScope = getParentVariableScope();
    if (parentVariableScope!=null) {
      if (sourceActivityExecution==null) {
        parentVariableScope.removeVariable(variableName);
      } else {
        parentVariableScope.removeVariable(variableName, sourceActivityExecution);
      }
    }
  }

  public void removeVariableLocal(String variableName) {
    removeVariableLocal(variableName, getSourceActivityExecution());
  }

  protected ExecutionEntity getSourceActivityExecution() {
    return null;
  }
  
  protected void removeVariableLocal(String variableName, ExecutionEntity sourceActivityExecution) {
    ensureVariableInstancesInitialized();
    VariableInstanceEntity variableInstance = variableInstances.remove(variableName);
    if (variableInstance != null) {
      deleteVariableInstanceForExplicitUserCall(variableInstance, sourceActivityExecution);
    }
  }

  protected void deleteVariableInstanceForExplicitUserCall(VariableInstanceEntity variableInstance, ExecutionEntity sourceActivityExecution) {
    
    // delete variable instance
    variableInstance.delete();
    variableInstance.setValue(null);
    
    // fire DELETE event
    if(isAutoFireHistoryEvents()) {
      fireHistoricVariableInstanceDelete(variableInstance, sourceActivityExecution);
    }
  }

  protected void updateVariableInstance(VariableInstanceEntity variableInstance, Object value, ExecutionEntity sourceActivityExecution) {
    
    // update variable instance
    variableInstance.setValue(value);

    // fire UPDATE event
    if(isAutoFireHistoryEvents()) {
      fireHistoricVariableInstanceUpdate(variableInstance, sourceActivityExecution);
    }
  }

  protected VariableInstanceEntity createVariableInstance(String variableName, Object value, ExecutionEntity sourceActivityExecution) {
    
    // create variable instance
    VariableTypes variableTypes = Context
      .getProcessEngineConfiguration()
      .getVariableTypes();
    
    VariableType type = variableTypes.findVariableType(value);
 
    VariableInstanceEntity variableInstance = VariableInstanceEntity.createAndInsert(variableName, type, value);
    initializeVariableInstanceBackPointer(variableInstance);
    variableInstances.put(variableName, variableInstance);
    
    variableInstance.setValue(value);
    
    // fire CREATE event
    if(isAutoFireHistoryEvents()) {
      fireHistoricVariableInstanceCreate(variableInstance, sourceActivityExecution);
    }
    
    return variableInstance;
  }
  
  protected void fireHistoricVariableInstanceDelete(VariableInstanceEntity variableInstance, ExecutionEntity sourceActivityExecution) {

    int historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
    if (historyLevel >=ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
      
      final ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
      final HistoryEventHandler eventHandler = processEngineConfiguration.getHistoryEventHandler();
      final HistoryEventProducer eventProducer = processEngineConfiguration.getHistoryEventProducer();
      
      HistoryEvent evt = eventProducer.createHistoricVariableDeleteEvt(variableInstance, sourceActivityExecution);
      eventHandler.handleEvent(evt);
      
    }
    
  }
  
  protected void fireHistoricVariableInstanceCreate(VariableInstanceEntity variableInstance, ExecutionEntity sourceActivityExecution) {

    int historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
    if (historyLevel >=ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
      
      final ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
      final HistoryEventHandler eventHandler = processEngineConfiguration.getHistoryEventHandler();
      final HistoryEventProducer eventProducer = processEngineConfiguration.getHistoryEventProducer();
      
      HistoryEvent evt = eventProducer.createHistoricVariableCreateEvt(variableInstance, sourceActivityExecution);
      eventHandler.handleEvent(evt);
      
    }
    
  }

  protected void fireHistoricVariableInstanceUpdate(VariableInstanceEntity variableInstance, ExecutionEntity sourceActivityExecution) {

    int historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
    if (historyLevel >=ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
      
      final ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
      final HistoryEventHandler eventHandler = processEngineConfiguration.getHistoryEventHandler();
      final HistoryEventProducer eventProducer = processEngineConfiguration.getHistoryEventProducer();
      
      HistoryEvent evt = eventProducer.createHistoricVariableUpdateEvt(variableInstance, sourceActivityExecution);
      eventHandler.handleEvent(evt);
      
    }
    
  }
  
  /**
   * @return true if this variable scope should automatically fire history events for variable modifications.
   */
  protected boolean isAutoFireHistoryEvents() {
    return true;
  }
  
  // getters and setters //////////////////////////////////////////////////////

  public ELContext getCachedElContext() {
    return cachedElContext;
  }
  public void setCachedElContext(ELContext cachedElContext) {
    this.cachedElContext = cachedElContext;
  }
  
  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }
}
