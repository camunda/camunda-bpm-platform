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
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerChangeJobDefinitionSuspensionStateJobHandler;
import org.camunda.bpm.engine.impl.persistence.entity.JobDefinitionManager;
import org.camunda.bpm.engine.impl.persistence.entity.JobManager;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity;
import org.camunda.bpm.engine.runtime.Job;

/**
 * @author Daniel Meyer
 * @author roman.smirnov
 */
public abstract class AbstractSetJobDefinitionStateCmd implements Command<Void>{

  protected String jobDefinitionId;
  protected String processDefinitionId;
  protected String processDefinitionKey;

  protected boolean includeJobs = false;
  protected Date executionDate;

  public AbstractSetJobDefinitionStateCmd(String jobDefinitionId, String processDefinitionId, String processDefinitionKey, boolean includeJobs, Date executionDate) {
    this.jobDefinitionId = jobDefinitionId;
    this.processDefinitionId = processDefinitionId;
    this.processDefinitionKey = processDefinitionKey;
    this.includeJobs = includeJobs;
    this.executionDate = executionDate;
  }

  public Void execute(CommandContext commandContext) {

    if (jobDefinitionId == null && processDefinitionId == null && processDefinitionKey == null) {
      throw new ProcessEngineException("Job definition id, process definition id nor process definition key cannot be null");
    }

    if (executionDate == null) {
      // Job definition suspension state is changed now
      updateSuspensionState(commandContext);
    } else {
      // Job definition suspension state change is delayed
      scheduleSuspensionStateUpdate(commandContext);
    }

    return null;
  }

  private void updateSuspensionState(CommandContext commandContext) {
    JobDefinitionManager jobDefinitionManager = commandContext.getJobDefinitionManager();
    JobManager jobManager = commandContext.getJobManager();

    SuspensionState suspensionState = getSuspensionState();

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

    if (includeJobs) {
      getSetJobStateCmd().execute(commandContext);
    }
  }

  private void scheduleSuspensionStateUpdate(CommandContext commandContext) {
    TimerEntity timer = new TimerEntity();

    timer.setDuedate(executionDate);
    timer.setJobHandlerType(getDelayedExecutionJobHandlerType());

    String jobConfiguration = null;

    if (jobDefinitionId != null) {
      jobConfiguration = TimerChangeJobDefinitionSuspensionStateJobHandler
          .createJobHandlerConfigurationByJobDefinitionId(jobDefinitionId, includeJobs);
    } else

    if (processDefinitionId != null) {
      jobConfiguration = TimerChangeJobDefinitionSuspensionStateJobHandler
          .createJobHandlerConfigurationByProcessDefinitionId(processDefinitionId, includeJobs);
    } else

    if (processDefinitionKey != null) {
      jobConfiguration = TimerChangeJobDefinitionSuspensionStateJobHandler
          .createJobHandlerConfigurationByProcessDefinitionKey(processDefinitionKey, includeJobs);
    }

    timer.setJobHandlerConfiguration(jobConfiguration);

    commandContext.getJobManager().schedule(timer);
  }

  /**
   * Subclasses should return the wanted {@link SuspensionState} here.
   */
  protected abstract SuspensionState getSuspensionState();

  /**
   * Subclasses should return the type of the {@link JobHandler} here. it will be used when
   * the user provides an execution date on which the actual state change will happen.
   */
  protected abstract String getDelayedExecutionJobHandlerType();

  /**
   * Subclasses should return the type of the {@link AbstractSetJobStateCmd} here.
   * It will be used to suspend or activate the {@link Job}s.
   */
  protected abstract AbstractSetJobStateCmd getSetJobStateCmd();

}
