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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;

/**
 * @author Daniel Meyer
 * @author Joram Barrez
 */
public abstract class AbstractSetProcessInstanceStateCmd implements Command<Void> {
    
  protected final String executionId;
  

  public AbstractSetProcessInstanceStateCmd(String executionId) {
    this.executionId = executionId;
  }

  public Void execute(CommandContext commandContext) {
    
    if(executionId == null) {
      throw new ProcessEngineException("ProcessInstanceId cannot be null.");
    }
    
    ExecutionEntity executionEntity = commandContext.getExecutionManager()
      .findExecutionById(executionId);
    
    if(executionEntity == null) {
      throw new ProcessEngineException("Cannot find processInstance for id '"+executionId+"'.");
    }
    
    if(!executionEntity.isProcessInstance()) {
      throw new ProcessEngineException("Cannot set suspension state for execution '"+executionId+"': not a process instance.");
    }
    
    executionEntity.setSuspensionState(getNewState().getStateCode());

    // All child executions are suspended
    commandContext.getExecutionManager().updateExecutionSuspensionStateByProcessInstanceId(executionId, getNewState());
    
    // All tasks are suspended
    commandContext.getTaskManager().updateTaskSuspensionStateByProcessInstanceId(executionId, getNewState());
    
    return null;
  }

  protected abstract SuspensionState getNewState();

}
