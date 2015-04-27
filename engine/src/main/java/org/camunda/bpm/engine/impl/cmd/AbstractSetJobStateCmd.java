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
import org.camunda.bpm.engine.impl.persistence.entity.JobDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobDefinitionManager;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobManager;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;

/**
 * @author roman.smirnov
 */
public abstract class AbstractSetJobStateCmd extends AbstractSetStateCmd {

  protected String jobId;
  protected String jobDefinitionId;
  protected String processInstanceId;
  protected String processDefinitionId;
  protected String processDefinitionKey;

  public AbstractSetJobStateCmd(String jobId, String jobDefinitionId, String processInstanceId, String processDefinitionId, String processDefinitionKey) {
    super(false, null);
    this.jobId = jobId;
    this.jobDefinitionId = jobDefinitionId;
    this.processInstanceId = processInstanceId;
    this.processDefinitionId = processDefinitionId;
    this.processDefinitionKey = processDefinitionKey;
  }

  protected void checkParameters(CommandContext commandContext) {
    if(jobId == null && jobDefinitionId == null && processInstanceId == null && processDefinitionId == null && processDefinitionKey == null) {
      throw new ProcessEngineException("Job id, job definition id, process instance id, process definition id nor process definition key cannot be null");
    }
  }

  protected void checkAuthorization(CommandContext commandContext) {
    AuthorizationManager authorizationManager = commandContext.getAuthorizationManager();

    if (jobId != null) {

      JobManager jobManager = commandContext.getJobManager();
      JobEntity job = jobManager.findJobById(jobId);

      if (job != null) {

        String processInstanceId = job.getProcessInstanceId();
        if (processInstanceId != null) {
          authorizationManager.checkUpdateProcessInstanceById(processInstanceId);
        }
        else {
          // start timer job is not assigned to a specific process
          // instance, that's why we have to check whether there
          // exists a UPDATE_INSTANCES permission on process definition or
          // a UPDATE permission on any process instance
          String processDefinitionKey = job.getProcessDefinitionKey();
          if (processDefinitionKey != null) {
            authorizationManager.checkUpdateProcessInstanceByProcessDefinitionKey(processDefinitionKey);
          }
        }
        // if (processInstanceId == null && processDefinitionKey == null):
        // job is not assigned to any process instance nor process definition
        // then it is always possible to activate/suspend the corresponding job
        // -> no authorization check necessary
      }
    } else

    if (jobDefinitionId != null) {

      JobDefinitionManager jobDefinitionManager = commandContext.getJobDefinitionManager();
      JobDefinitionEntity jobDefinition = jobDefinitionManager.findById(jobDefinitionId);

      if (jobDefinition != null) {
        String processDefinitionKey = jobDefinition.getProcessDefinitionKey();
        authorizationManager.checkUpdateProcessInstanceByProcessDefinitionKey(processDefinitionKey);
      }

    } else

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
    JobManager jobManager = commandContext.getJobManager();

    if (jobId != null) {
      jobManager.updateJobSuspensionStateById(jobId, suspensionState);
    } else

    if (jobDefinitionId != null) {
      jobManager.updateJobSuspensionStateByJobDefinitionId(jobDefinitionId, suspensionState);
    } else

    if (processInstanceId != null) {
        jobManager.updateJobSuspensionStateByProcessInstanceId(processInstanceId, suspensionState);
    } else

    if (processDefinitionId != null) {
      jobManager.updateJobSuspensionStateByProcessDefinitionId(processDefinitionId, suspensionState);
    } else

    if (processDefinitionKey != null) {
      jobManager.updateJobSuspensionStateByProcessDefinitionKey(processDefinitionKey, suspensionState);
    }
  }

  protected void logUserOperation(CommandContext commandContext) {
    PropertyChange propertyChange = new PropertyChange(SUSPENSION_STATE_PROPERTY, null, getNewSuspensionState().getName());
    commandContext.getOperationLogManager().logJobOperation(getLogEntryOperation(), jobId, jobDefinitionId,
      processInstanceId, processDefinitionId, processDefinitionKey, propertyChange);
  }

}
