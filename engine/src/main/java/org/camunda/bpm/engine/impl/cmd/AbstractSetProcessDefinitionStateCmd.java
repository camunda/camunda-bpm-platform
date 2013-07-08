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
package org.camunda.bpm.engine.impl.cmd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessDefinitionQueryImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerChangeProcessDefinitionSuspensionStateJobHandler;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionManager;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;

/**
 * @author Daniel Meyer
 * @author Joram Barrez
 */
public abstract class AbstractSetProcessDefinitionStateCmd implements Command<Void> {
  
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected ProcessDefinitionEntity processDefinitionEntity;
  protected boolean includeProcessInstances = false;
  protected Date executionDate;

  public AbstractSetProcessDefinitionStateCmd(ProcessDefinitionEntity processDefinitionEntity, 
          boolean includeProcessInstances, Date executionDate) {
    this.processDefinitionEntity = processDefinitionEntity;
    this.includeProcessInstances = includeProcessInstances;
    this.executionDate = executionDate;
  }
  
  public AbstractSetProcessDefinitionStateCmd(String processDefinitionId, String processDefinitionKey,
            boolean includeProcessInstances, Date executionDate) {
    this.processDefinitionId = processDefinitionId;
    this.processDefinitionKey = processDefinitionKey;
    this.includeProcessInstances = includeProcessInstances;
    this.executionDate = executionDate;
  }
  
  public Void execute(CommandContext commandContext) {
    
    List<ProcessDefinitionEntity> processDefinitions = findProcessDefinition(commandContext);
    
    if (executionDate != null) { // Process definition state change is delayed
      createTimerForDelayedExecution(commandContext, processDefinitions);
    } else { // Process definition state is changed now
      changeProcessDefinitionState(commandContext, processDefinitions);
    }

    return null;
  }

  protected List<ProcessDefinitionEntity> findProcessDefinition(CommandContext commandContext) {
    
    // If process definition is already provided (eg. when command is called through the DeployCmd) 
    // we don't need to do an extra database fetch and we can simply return it, wrapped in a list
    if (processDefinitionEntity != null) {
      return Arrays.asList(processDefinitionEntity);
    }
    
    // Validation of input parameters
    if(processDefinitionId == null && processDefinitionKey == null) {
      throw new ProcessEngineException("Process definition id / key cannot be null");
    }
    
    List<ProcessDefinitionEntity> processDefinitionEntities = new ArrayList<ProcessDefinitionEntity>();
    ProcessDefinitionManager processDefinitionManager = commandContext.getProcessDefinitionManager();
    
    if(processDefinitionId != null) {
      
      ProcessDefinitionEntity processDefinitionEntity = processDefinitionManager.findLatestProcessDefinitionById(processDefinitionId);
      if(processDefinitionEntity == null) {
        throw new ProcessEngineException("Cannot find process definition for id '"+processDefinitionId+"'");
      }
      processDefinitionEntities.add(processDefinitionEntity);
      
    } else {

      List<ProcessDefinition> processDefinitions = new ProcessDefinitionQueryImpl(commandContext)
        .processDefinitionKey(processDefinitionKey)
        .list();

      if(processDefinitions.size() == 0) {
        throw new ProcessEngineException("Cannot find process definition for key '"+processDefinitionKey+"'");
      }
      
      for (ProcessDefinition processDefinition : processDefinitions) {
        processDefinitionEntities.add((ProcessDefinitionEntity) processDefinition);
      }
      
    }
    return processDefinitionEntities;
  }
  
  protected void createTimerForDelayedExecution(CommandContext commandContext, List<ProcessDefinitionEntity> processDefinitions) {
    for (ProcessDefinitionEntity processDefinition : processDefinitions) {
      TimerEntity timer = new TimerEntity();
      timer.setDuedate(executionDate);
      timer.setJobHandlerType(getDelayedExecutionJobHandlerType());
      timer.setJobHandlerConfiguration(TimerChangeProcessDefinitionSuspensionStateJobHandler
              .createJobHandlerConfiguration(processDefinition.getId(), includeProcessInstances));
      commandContext.getJobManager().schedule(timer);
    }
  }
  
  protected void changeProcessDefinitionState(CommandContext commandContext, List<ProcessDefinitionEntity> processDefinitions) {
    for (ProcessDefinitionEntity processDefinition : processDefinitions) {
    
      processDefinition.setSuspensionState(getProcessDefinitionSuspensionState().getStateCode());
      
      // Suspend process instances and child executions and their tasks (if needed)
      if (includeProcessInstances) {
        commandContext.getExecutionManager().updateExecutionSuspensionStateByProcessDefinitionId(processDefinitionId, getProcessDefinitionSuspensionState());
        commandContext.getTaskManager().updateTaskSuspensionStateByProcessDefinitionId(processDefinitionId, getProcessDefinitionSuspensionState());
      }
      
    }
  }
  
  
  // ABSTRACT METHODS ////////////////////////////////////////////////////////////////////

  /**
   * Subclasses should return the wanted {@link SuspensionState} here.
   */
  protected abstract SuspensionState getProcessDefinitionSuspensionState();
  
  /**
   * Subclasses should return the type of the {@link JobHandler} here. it will be used when
   * the user provides an execution date on which the actual state change will happen.
   */
  protected abstract String getDelayedExecutionJobHandlerType();
  
  /**
   * Subclasses should return a {@link Command} implementation that matches the process definition
   * state change.
   */
  protected abstract AbstractSetProcessInstanceStateCmd getProcessInstanceChangeStateCmd(ProcessInstance processInstance); 
  
}
