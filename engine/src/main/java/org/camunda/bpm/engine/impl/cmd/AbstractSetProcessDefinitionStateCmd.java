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
import org.camunda.bpm.engine.impl.jobexecutor.TimerChangeProcessDefinitionSuspensionStateJobHandler;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionManager;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;

/**
 * @author Daniel Meyer
 * @author Joram Barrez
 * @author roman.smirnov
 */
public abstract class AbstractSetProcessDefinitionStateCmd implements Command<Void> {

  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected ProcessDefinitionEntity processDefinitionEntity;
  protected boolean includeProcessInstances = false;
  protected Date executionDate;

  public AbstractSetProcessDefinitionStateCmd(ProcessDefinitionEntity processDefinitionEntity,
          boolean includeProcessInstances, Date executionDate) {
    // If process definition is already provided (eg. when command is called through the DeployCmd),
    // we can simply use the id of the entity and set is as processDefinitionId.
    this.processDefinitionEntity = processDefinitionEntity;
    this.processDefinitionId = processDefinitionEntity.getId();
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

    // Validation of input parameters
    if(processDefinitionId == null && processDefinitionKey == null) {
      throw new ProcessEngineException("Process definition id / key cannot be null");
    }

    if (executionDate == null) {
      // Process definition state is changed now
      updateSuspensionState(commandContext);
    } else {
      // Process definition state change is delayed
      scheduleSuspensionStateUpdate(commandContext);
    }

    return null;
  }

  protected void scheduleSuspensionStateUpdate(CommandContext commandContext) {
    TimerEntity timer = new TimerEntity();

    timer.setDuedate(executionDate);
    timer.setJobHandlerType(getDelayedExecutionJobHandlerType());

    String jobConfiguration = null;

    if (processDefinitionId != null) {
      jobConfiguration = TimerChangeProcessDefinitionSuspensionStateJobHandler
          .createJobHandlerConfigurationByProcessDefinitionId(processDefinitionId, includeProcessInstances);
    } else

    if (processDefinitionKey != null) {
      jobConfiguration = TimerChangeProcessDefinitionSuspensionStateJobHandler
          .createJobHandlerConfigurationByProcessDefinitionKey(processDefinitionKey, includeProcessInstances);
    }

    timer.setJobHandlerConfiguration(jobConfiguration);

    commandContext.getJobManager().schedule(timer);
  }

  protected void updateSuspensionState(CommandContext commandContext) {
    ProcessDefinitionManager processDefinitionManager = commandContext.getProcessDefinitionManager();

    SuspensionState suspensionState = getProcessDefinitionSuspensionState();

    if (processDefinitionId != null) {
      processDefinitionManager.updateProcessDefinitionSuspensionStateById(processDefinitionId, suspensionState);
    } else

    if (processDefinitionKey != null) {
      processDefinitionManager.updateProcessDefinitionSuspensionStateByKey(processDefinitionKey, suspensionState);
    }

    getSetJobDefinitionStateCmd().execute(commandContext);
    if (includeProcessInstances) {
      getSetProcessInstanceStateCmd().execute(commandContext);
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
   * Subclasses should return the type of the {@link AbstractSetJobDefinitionStateCmd} here.
   * It will be used to suspend or activate the {@link JobDefinition}s.
   */
  protected abstract AbstractSetJobDefinitionStateCmd getSetJobDefinitionStateCmd();

  /**
   * Subclasses should return the type of the {@link AbstractSetProcessInstanceStateCmd} here.
   * It will be used to suspend or activate the {@link ProcessInstance}s.
   */
  protected abstract AbstractSetProcessInstanceStateCmd getSetProcessInstanceStateCmd();

}
