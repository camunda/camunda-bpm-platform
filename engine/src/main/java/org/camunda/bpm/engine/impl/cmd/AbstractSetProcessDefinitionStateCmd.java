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

import java.util.concurrent.Callable;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerChangeProcessDefinitionSuspensionStateJobHandler;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionManager;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.impl.repository.UpdateProcessDefinitionSuspensionStateBuilderImpl;
import org.camunda.bpm.engine.management.JobDefinition;

/**
 * @author Daniel Meyer
 * @author Joram Barrez
 * @author roman.smirnov
 */
public abstract class AbstractSetProcessDefinitionStateCmd extends AbstractSetStateCmd {

  protected String processDefinitionId;
  protected String processDefinitionKey;

  protected String tenantId;
  protected boolean isTenantIdSet = false;

  public AbstractSetProcessDefinitionStateCmd(UpdateProcessDefinitionSuspensionStateBuilderImpl builder) {
    super(
        builder.isIncludeProcessInstances(),
        builder.getExecutionDate());

    this.processDefinitionId = builder.getProcessDefinitionId();
    this.processDefinitionKey = builder.getProcessDefinitionKey();

    this.isTenantIdSet = builder.isTenantIdSet();
    this.tenantId = builder.getProcessDefinitionTenantId();
  }

  @Override
  protected void checkParameters(CommandContext commandContext) {
    // Validation of input parameters
    if(processDefinitionId == null && processDefinitionKey == null) {
      throw new ProcessEngineException("Process definition id / key cannot be null");
    }
  }

  @Override
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

  @Override
  protected void updateSuspensionState(final CommandContext commandContext, SuspensionState suspensionState) {
    ProcessDefinitionManager processDefinitionManager = commandContext.getProcessDefinitionManager();

    if (processDefinitionId != null) {
      processDefinitionManager.updateProcessDefinitionSuspensionStateById(processDefinitionId, suspensionState);

    } else if (isTenantIdSet) {
      processDefinitionManager.updateProcessDefinitionSuspensionStateByKeyAndTenantId(processDefinitionKey, tenantId, suspensionState);

    } else {
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

  @Override
  protected String getJobHandlerConfiguration() {
    String jobConfiguration = null;

    if (processDefinitionId != null) {
      jobConfiguration = TimerChangeProcessDefinitionSuspensionStateJobHandler
          .createJobHandlerConfigurationByProcessDefinitionId(processDefinitionId, isIncludeSubResources());

    } else if (isTenantIdSet) {
      jobConfiguration = TimerChangeProcessDefinitionSuspensionStateJobHandler
          .createJobHandlerConfigurationByProcessDefinitionKeyAndTenantId(processDefinitionKey, tenantId, isIncludeSubResources());

    } else {
      jobConfiguration = TimerChangeProcessDefinitionSuspensionStateJobHandler
          .createJobHandlerConfigurationByProcessDefinitionKey(processDefinitionKey, isIncludeSubResources());
    }

    return jobConfiguration;
  }

  @Override
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
  @Override
  protected abstract String getDelayedExecutionJobHandlerType();

  /**
   * Subclasses should return the type of the {@link AbstractSetJobDefinitionStateCmd} here.
   * It will be used to suspend or activate the {@link JobDefinition}s.
   */
  protected abstract AbstractSetJobDefinitionStateCmd getSetJobDefinitionStateCmd();

  @Override
  protected abstract AbstractSetProcessInstanceStateCmd getNextCommand();
}
