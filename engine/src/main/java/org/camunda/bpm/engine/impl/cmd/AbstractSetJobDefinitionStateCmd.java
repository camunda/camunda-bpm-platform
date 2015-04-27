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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerChangeJobDefinitionSuspensionStateJobHandler;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;
import org.camunda.bpm.engine.impl.persistence.entity.JobDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobDefinitionManager;
import org.camunda.bpm.engine.impl.persistence.entity.JobManager;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;

/**
 * @author Daniel Meyer
 * @author roman.smirnov
 */
public abstract class AbstractSetJobDefinitionStateCmd extends AbstractSetStateCmd {

  protected String jobDefinitionId;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected Date executionDate;

  public AbstractSetJobDefinitionStateCmd(String jobDefinitionId, String processDefinitionId, String processDefinitionKey, boolean includeJobs, Date executionDate) {
    super(includeJobs, executionDate);
    this.jobDefinitionId = jobDefinitionId;
    this.processDefinitionId = processDefinitionId;
    this.processDefinitionKey = processDefinitionKey;
    this.executionDate = executionDate;
  }

  protected void checkParameters(CommandContext commandContext) {
    if (jobDefinitionId == null && processDefinitionId == null && processDefinitionKey == null) {
      throw new ProcessEngineException("Job definition id, process definition id nor process definition key cannot be null");
    }
  }

  protected void checkAuthorization(CommandContext commandContext) {
    AuthorizationManager authorizationManager = commandContext.getAuthorizationManager();

    if (jobDefinitionId != null) {

      JobDefinitionManager jobDefinitionManager = commandContext.getJobDefinitionManager();
      JobDefinitionEntity jobDefinition = jobDefinitionManager.findById(jobDefinitionId);

      if (jobDefinition != null) {
        String processDefinitionKey = jobDefinition.getProcessDefinitionKey();
        authorizationManager.checkUpdateProcessDefinitionByKey(processDefinitionKey);

        if (includeSubResources) {
          authorizationManager.checkUpdateProcessInstanceByProcessDefinitionKey(processDefinitionKey);
        }
      }

    } else

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

  protected void updateSuspensionState(CommandContext commandContext, SuspensionState suspensionState) {
    JobDefinitionManager jobDefinitionManager = commandContext.getJobDefinitionManager();
    JobManager jobManager = commandContext.getJobManager();

    if (jobDefinitionId != null) {
      jobDefinitionManager.updateJobDefinitionSuspensionStateById(jobDefinitionId, suspensionState);
    } else

    if (processDefinitionId != null) {
      jobDefinitionManager.updateJobDefinitionSuspensionStateByProcessDefinitionId(processDefinitionId, suspensionState);
      jobManager.updateStartTimerJobSuspensionStateByProcessDefinitionId(processDefinitionId, suspensionState);
    } else

    if (processDefinitionKey != null) {
      jobDefinitionManager.updateJobDefinitionSuspensionStateByProcessDefinitionKey(processDefinitionKey, suspensionState);
      jobManager.updateStartTimerJobSuspensionStateByProcessDefinitionKey(processDefinitionKey, suspensionState);
    }

  }

  protected String getJobHandlerConfiguration() {
    String jobConfiguration = null;

    if (jobDefinitionId != null) {
      jobConfiguration = TimerChangeJobDefinitionSuspensionStateJobHandler
          .createJobHandlerConfigurationByJobDefinitionId(jobDefinitionId, isIncludeSubResources());
    } else

    if (processDefinitionId != null) {
      jobConfiguration = TimerChangeJobDefinitionSuspensionStateJobHandler
          .createJobHandlerConfigurationByProcessDefinitionId(processDefinitionId, isIncludeSubResources());
    } else

    if (processDefinitionKey != null) {
      jobConfiguration = TimerChangeJobDefinitionSuspensionStateJobHandler
          .createJobHandlerConfigurationByProcessDefinitionKey(processDefinitionKey, isIncludeSubResources());
    }

    return jobConfiguration;
  }

  protected void logUserOperation(CommandContext commandContext) {
    PropertyChange propertyChange = new PropertyChange(SUSPENSION_STATE_PROPERTY, null, getNewSuspensionState().getName());
    commandContext.getOperationLogManager().logJobDefinitionOperation(getLogEntryOperation(), jobDefinitionId,
      processDefinitionId, processDefinitionKey, propertyChange);
  }

  /**
   * Subclasses should return the type of the {@link JobHandler} here. it will be used when
   * the user provides an execution date on which the actual state change will happen.
   */
  protected abstract String getDelayedExecutionJobHandlerType();

  protected abstract AbstractSetJobStateCmd getNextCommand();

}
