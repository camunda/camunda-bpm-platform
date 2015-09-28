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
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionManager;
import org.camunda.bpm.engine.impl.persistence.entity.ExternalTaskManager;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.impl.persistence.entity.TaskManager;

/**
 * @author Daniel Meyer
 * @author Joram Barrez
 * @author roman.smirnov
 */
public abstract class AbstractSetProcessInstanceStateCmd extends AbstractSetStateCmd {

  protected final String processInstanceId;
  protected String processDefinitionId;
  protected String processDefinitionKey;

  public AbstractSetProcessInstanceStateCmd(String processInstanceId, String processDefinitionId, String processDefinitionKey) {
    super(true, null);
    this.processInstanceId = processInstanceId;
    this.processDefinitionId = processDefinitionId;
    this.processDefinitionKey = processDefinitionKey;
  }

  protected void checkParameters(CommandContext commandContext) {
    if(processInstanceId == null && processDefinitionId == null && processDefinitionKey == null) {
      throw new ProcessEngineException("ProcessInstanceId, ProcessDefinitionId nor ProcessDefinitionKey cannot be null.");
    }
  }

  protected void checkAuthorization(CommandContext commandContext) {
    AuthorizationManager authorizationManager = commandContext.getAuthorizationManager();
    if (processInstanceId != null) {
      authorizationManager.checkUpdateProcessInstanceById(processInstanceId);
    } else

    if (processDefinitionId != null) {
      authorizationManager.checkUpdateProcessInstanceByProcessDefinitionId(processDefinitionId);
    } else

    if (processDefinitionKey != null) {
      authorizationManager.checkUpdateProcessInstanceByProcessDefinitionKey(processDefinitionKey);
    }
  }

  protected void updateSuspensionState(CommandContext commandContext, SuspensionState suspensionState) {
    ExecutionManager executionManager = commandContext.getExecutionManager();
    TaskManager taskManager = commandContext.getTaskManager();
    ExternalTaskManager externalTaskManager = commandContext.getExternalTaskManager();

    if (processInstanceId != null) {
      executionManager.updateExecutionSuspensionStateByProcessInstanceId(processInstanceId, suspensionState);
      taskManager.updateTaskSuspensionStateByProcessInstanceId(processInstanceId, suspensionState);
      externalTaskManager.updateExternalTaskSuspensionState(processInstanceId, null, null, suspensionState);
    } else

    if (processDefinitionId != null) {
      executionManager.updateExecutionSuspensionStateByProcessDefinitionId(processDefinitionId, suspensionState);
      taskManager.updateTaskSuspensionStateByProcessDefinitionId(processDefinitionId, suspensionState);
      externalTaskManager.updateExternalTaskSuspensionState(null, processDefinitionId, null, suspensionState);
    } else

    if (processDefinitionKey != null) {
      executionManager.updateExecutionSuspensionStateByProcessDefinitionKey(processDefinitionKey, suspensionState);
      taskManager.updateTaskSuspensionStateByProcessDefinitionKey(processDefinitionKey, suspensionState);
      externalTaskManager.updateExternalTaskSuspensionState(null, null, processDefinitionKey, suspensionState);

    }
  }

  protected void logUserOperation(CommandContext commandContext) {
    PropertyChange propertyChange = new PropertyChange(SUSPENSION_STATE_PROPERTY, null, getNewSuspensionState().getName());
    commandContext.getOperationLogManager()
      .logProcessInstanceOperation(getLogEntryOperation(), processInstanceId, processDefinitionId,
        processDefinitionKey, propertyChange);
  }

  protected abstract AbstractSetJobStateCmd getNextCommand();

}
