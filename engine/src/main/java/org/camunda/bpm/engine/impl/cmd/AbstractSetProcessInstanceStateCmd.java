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

import java.util.Collections;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.management.UpdateJobSuspensionStateBuilderImpl;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionManager;
import org.camunda.bpm.engine.impl.persistence.entity.ExternalTaskManager;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.impl.persistence.entity.TaskManager;
import org.camunda.bpm.engine.impl.runtime.UpdateProcessInstanceSuspensionStateBuilderImpl;

/**
 * @author Daniel Meyer
 * @author Joram Barrez
 * @author roman.smirnov
 */
public abstract class AbstractSetProcessInstanceStateCmd extends AbstractSetStateCmd {

  protected final String processInstanceId;
  protected String processDefinitionId;
  protected String processDefinitionKey;

  protected String processDefinitionTenantId;
  protected boolean isProcessDefinitionTenantIdSet = false;

  public AbstractSetProcessInstanceStateCmd(UpdateProcessInstanceSuspensionStateBuilderImpl builder) {
    super(true, null);

    this.processInstanceId = builder.getProcessInstanceId();
    this.processDefinitionId = builder.getProcessDefinitionId();
    this.processDefinitionKey = builder.getProcessDefinitionKey();
    this.processDefinitionTenantId = builder.getProcessDefinitionTenantId();
    this.isProcessDefinitionTenantIdSet = builder.isProcessDefinitionTenantIdSet();
  }

  @Override
  protected void checkParameters(CommandContext commandContext) {
    if(processInstanceId == null && processDefinitionId == null && processDefinitionKey == null) {
      throw new ProcessEngineException("ProcessInstanceId, ProcessDefinitionId nor ProcessDefinitionKey cannot be null.");
    }
  }

  @Override
  protected void checkAuthorization(CommandContext commandContext) {

    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      if (processInstanceId != null) {
        checker.checkUpdateProcessInstanceById(processInstanceId);
      } else

      if (processDefinitionId != null) {
        checker.checkUpdateProcessInstanceByProcessDefinitionId(processDefinitionId);
      } else

      if (processDefinitionKey != null) {
        checker.checkUpdateProcessInstanceByProcessDefinitionKey(processDefinitionKey);
      }
    }
  }

  @Override
  protected void updateSuspensionState(CommandContext commandContext, SuspensionState suspensionState) {
    ExecutionManager executionManager = commandContext.getExecutionManager();
    TaskManager taskManager = commandContext.getTaskManager();
    ExternalTaskManager externalTaskManager = commandContext.getExternalTaskManager();

    if (processInstanceId != null) {
      executionManager.updateExecutionSuspensionStateByProcessInstanceId(processInstanceId, suspensionState);
      taskManager.updateTaskSuspensionStateByProcessInstanceId(processInstanceId, suspensionState);
      externalTaskManager.updateExternalTaskSuspensionStateByProcessInstanceId(processInstanceId, suspensionState);

    } else if (processDefinitionId != null) {
      executionManager.updateExecutionSuspensionStateByProcessDefinitionId(processDefinitionId, suspensionState);
      taskManager.updateTaskSuspensionStateByProcessDefinitionId(processDefinitionId, suspensionState);
      externalTaskManager.updateExternalTaskSuspensionStateByProcessDefinitionId(processDefinitionId, suspensionState);

    } else if (isProcessDefinitionTenantIdSet) {
      executionManager.updateExecutionSuspensionStateByProcessDefinitionKeyAndTenantId(processDefinitionKey, processDefinitionTenantId, suspensionState);
      taskManager.updateTaskSuspensionStateByProcessDefinitionKeyAndTenantId(processDefinitionKey, processDefinitionTenantId, suspensionState);
      externalTaskManager.updateExternalTaskSuspensionStateByProcessDefinitionKeyAndTenantId(processDefinitionKey, processDefinitionTenantId, suspensionState);

    } else {
      executionManager.updateExecutionSuspensionStateByProcessDefinitionKey(processDefinitionKey, suspensionState);
      taskManager.updateTaskSuspensionStateByProcessDefinitionKey(processDefinitionKey, suspensionState);
      externalTaskManager.updateExternalTaskSuspensionStateByProcessDefinitionKey(processDefinitionKey, suspensionState);
    }
  }

  @Override
  protected void logUserOperation(CommandContext commandContext) {
    PropertyChange propertyChange = new PropertyChange(SUSPENSION_STATE_PROPERTY, null, getNewSuspensionState().getName());
    commandContext.getOperationLogManager()
      .logProcessInstanceOperation(getLogEntryOperation(), processInstanceId, processDefinitionId,
        processDefinitionKey, Collections.singletonList(propertyChange));
  }

  protected UpdateJobSuspensionStateBuilderImpl createJobCommandBuilder() {
    UpdateJobSuspensionStateBuilderImpl builder = new UpdateJobSuspensionStateBuilderImpl();

    if (processInstanceId != null) {
      builder.byProcessDefinitionId(processInstanceId);

    } else if (processDefinitionId != null) {
      builder.byProcessDefinitionId(processDefinitionId);

    } else if (processDefinitionKey != null) {
      builder.byProcessDefinitionKey(processDefinitionKey);

      if (isProcessDefinitionTenantIdSet && processDefinitionTenantId != null) {
        return builder.processDefinitionTenantId(processDefinitionTenantId);

      } else if (isProcessDefinitionTenantIdSet) {
        return builder.processDefinitionWithoutTenantId();
      }
    }
    return builder;
  }

  @Override
  protected AbstractSetJobStateCmd getNextCommand() {
    UpdateJobSuspensionStateBuilderImpl jobCommandBuilder = createJobCommandBuilder();

    return getNextCommand(jobCommandBuilder);
  }

  protected abstract AbstractSetJobStateCmd getNextCommand(UpdateJobSuspensionStateBuilderImpl jobCommandBuilder);

}
