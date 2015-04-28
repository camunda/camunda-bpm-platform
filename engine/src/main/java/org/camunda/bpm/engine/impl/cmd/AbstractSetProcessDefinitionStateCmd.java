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

import java.util.Date;
import java.util.concurrent.Callable;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerChangeProcessDefinitionSuspensionStateJobHandler;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionManager;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.management.JobDefinition;

/**
 * @author Daniel Meyer
 * @author Joram Barrez
 * @author roman.smirnov
 */
public abstract class AbstractSetProcessDefinitionStateCmd extends AbstractSetStateCmd {

  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected ProcessDefinitionEntity processDefinitionEntity;
  protected Date executionDate;

  public AbstractSetProcessDefinitionStateCmd(ProcessDefinitionEntity processDefinitionEntity, boolean includeProcessInstances, Date executionDate) {
    super(includeProcessInstances, executionDate);
    // If process definition is already provided (eg. when command is called through the DeployCmd),
    // we can simply use the id of the entity and set is as processDefinitionId.
    this.processDefinitionEntity = processDefinitionEntity;
    this.processDefinitionId = processDefinitionEntity.getId();
    this.executionDate = executionDate;
  }

  public AbstractSetProcessDefinitionStateCmd(String processDefinitionId, String processDefinitionKey, boolean includeProcessInstances, Date executionDate) {
    super(includeProcessInstances, executionDate);
    this.processDefinitionId = processDefinitionId;
    this.processDefinitionKey = processDefinitionKey;
    this.executionDate = executionDate;
  }

  protected void checkParameters(CommandContext commandContext) {
    // Validation of input parameters
    if(processDefinitionId == null && processDefinitionKey == null) {
      throw new ProcessEngineException("Process definition id / key cannot be null");
    }
  }

  protected void checkAuthorization(CommandContext commandContext) {
    AuthorizationManager authorizationManager = commandContext.getAuthorizationManager();
    if (processDefinitionId != null) {
      authorizationManager.checkUpdateProcessDefinitionById(processDefinitionId);

      if (includeSubResources) {
        authorizationManager.checkUpdateProcessInstanceByProcessDefinitionId(processDefinitionId);
      }
    } else

    if (processDefinitionKey != null) {
      authorizationManager.checkUpdateProcessDefinitionByKey(processDefinitionKey);

      if (includeSubResources) {
        authorizationManager.checkUpdateProcessInstanceByProcessDefinitionKey(processDefinitionKey);
      }
    }
  }

  protected void updateSuspensionState(final CommandContext commandContext, SuspensionState suspensionState) {
    ProcessDefinitionManager processDefinitionManager = commandContext.getProcessDefinitionManager();

    if (processDefinitionId != null) {
      processDefinitionManager.updateProcessDefinitionSuspensionStateById(processDefinitionId, suspensionState);
    } else

    if (processDefinitionKey != null) {
      processDefinitionManager.updateProcessDefinitionSuspensionStateByKey(processDefinitionKey, suspensionState);
    }

    commandContext.runWithoutAuthorization(new Callable<Void>() {
      public Void call() throws Exception {
        AbstractSetJobDefinitionStateCmd jobDefinitionCmd = getSetJobDefinitionStateCmd();
        jobDefinitionCmd.disableLogUserOperation();
        jobDefinitionCmd.execute(commandContext);
        return null;
      }
    });
  }

  protected String getJobHandlerConfiguration() {
    String jobConfiguration = null;

    if (processDefinitionId != null) {
      jobConfiguration = TimerChangeProcessDefinitionSuspensionStateJobHandler
          .createJobHandlerConfigurationByProcessDefinitionId(processDefinitionId, isIncludeSubResources());
    } else

    if (processDefinitionKey != null) {
      jobConfiguration = TimerChangeProcessDefinitionSuspensionStateJobHandler
          .createJobHandlerConfigurationByProcessDefinitionKey(processDefinitionKey, isIncludeSubResources());
    }

    return jobConfiguration;
  }

  protected void logUserOperation(CommandContext commandContext) {
    PropertyChange propertyChange = new PropertyChange(SUSPENSION_STATE_PROPERTY, null, getNewSuspensionState().getName());
    commandContext.getOperationLogManager()
      .logProcessDefinitionOperation(getLogEntryOperation(), processDefinitionId, processDefinitionKey, propertyChange);
  }

  // ABSTRACT METHODS ////////////////////////////////////////////////////////////////////

  /**
   * Subclasses should return the type of the {@link JobHandler} here. it will be used when
   * the user provides an execution date on which the actual state change will happen.
   */
  protected abstract String getDelayedExecutionJobHandlerType();

  /**
   * Subclasses should return the type of the {@link AbstractSetJobDefinitionStateCmd} here.
   * It will be used to suspend or activate the {@link JobDefinition}s.
   */
  protected abstract AbstractSetJobDefinitionStateCmd getSetJobDefinitionStateCmd();

  protected abstract AbstractSetProcessInstanceStateCmd getNextCommand();
}
