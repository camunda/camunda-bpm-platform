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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensurePositive;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.event.HistoryEventProcessor;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.history.producer.HistoryEventProducer;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.cache.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionManager;
import org.camunda.bpm.engine.impl.persistence.entity.IncidentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.runtime.ProcessInstance;


/**
 * {@link Command} that changes the process definition version of an existing
 * process instance.
 *
 * Warning: This command will NOT perform any migration magic and simply set the
 * process definition version in the database, assuming that the user knows,
 * what he or she is doing.
 *
 * This is only useful for simple migrations. The new process definition MUST
 * have the exact same activity id to make it still run.
 *
 * Furthermore, activities referenced by sub-executions and jobs that belong to
 * the process instance MUST exist in the new process definition version.
 *
 * The command will fail, if there is already a {@link ProcessInstance} or
 * {@link HistoricProcessInstance} using the new process definition version and
 * the same business key as the {@link ProcessInstance} that is to be migrated.
 *
 * If the process instance is not currently waiting but actively running, then
 * this would be a case for optimistic locking, meaning either the version
 * update or the "real work" wins, i.e., this is a race condition.
 *
 * @see http://forums.activiti.org/en/viewtopic.php?t=2918
 * @author Falko Menge
 * @author Ingo Richtsmeier
 */
public class SetProcessDefinitionVersionCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;

  private final String processInstanceId;
  private final Integer processDefinitionVersion;

  public SetProcessDefinitionVersionCmd(String processInstanceId, Integer processDefinitionVersion) {
    ensureNotEmpty("The process instance id is mandatory", "processInstanceId", processInstanceId);
    ensureNotNull("The process definition version is mandatory", "processDefinitionVersion", processDefinitionVersion);
    ensurePositive("The process definition version must be positive", "processDefinitionVersion", processDefinitionVersion.longValue());
    this.processInstanceId = processInstanceId;
    this.processDefinitionVersion = processDefinitionVersion;
  }

  public Void execute(CommandContext commandContext) {
    ProcessEngineConfigurationImpl configuration = commandContext.getProcessEngineConfiguration();

    // check that the new process definition is just another version of the same
    // process definition that the process instance is using
    ExecutionManager executionManager = commandContext.getExecutionManager();
    final ExecutionEntity processInstance = executionManager.findExecutionById(processInstanceId);
    if (processInstance == null) {
      throw new ProcessEngineException("No process instance found for id = '" + processInstanceId + "'.");
    } else if (!processInstance.isProcessInstanceExecution()) {
      throw new ProcessEngineException(
        "A process instance id is required, but the provided id " +
        "'"+processInstanceId+"' " +
        "points to a child execution of process instance " +
        "'"+processInstance.getProcessInstanceId()+"'. " +
        "Please invoke the "+getClass().getSimpleName()+" with a root execution id.");
    }
    ProcessDefinitionImpl currentProcessDefinitionImpl = processInstance.getProcessDefinition();

    DeploymentCache deploymentCache = configuration.getDeploymentCache();
    ProcessDefinitionEntity currentProcessDefinition;
    if (currentProcessDefinitionImpl instanceof ProcessDefinitionEntity) {
      currentProcessDefinition = (ProcessDefinitionEntity) currentProcessDefinitionImpl;
    } else {
      currentProcessDefinition = deploymentCache.findDeployedProcessDefinitionById(currentProcessDefinitionImpl.getId());
    }

    ProcessDefinitionEntity newProcessDefinition = deploymentCache
      .findDeployedProcessDefinitionByKeyVersionAndTenantId(currentProcessDefinition.getKey(), processDefinitionVersion, currentProcessDefinition.getTenantId());

    validateAndSwitchVersionOfExecution(commandContext, processInstance, newProcessDefinition);

    HistoryLevel historyLevel = configuration.getHistoryLevel();
    if(historyLevel.isHistoryEventProduced(HistoryEventTypes.PROCESS_INSTANCE_UPDATE, processInstance)) {
      HistoryEventProcessor.processHistoryEvents(new HistoryEventProcessor.HistoryEventCreator() {
        @Override
        public HistoryEvent createHistoryEvent(HistoryEventProducer producer) {
          return producer.createProcessInstanceUpdateEvt(processInstance);
        }
      });
    }

    // switch all sub-executions of the process instance to the new process definition version
    List<ExecutionEntity> childExecutions = executionManager
      .findExecutionsByProcessInstanceId(processInstanceId);
    for (ExecutionEntity executionEntity : childExecutions) {
      validateAndSwitchVersionOfExecution(commandContext, executionEntity, newProcessDefinition);
    }

    // switch all jobs to the new process definition version
    List<JobEntity> jobs = commandContext.getJobManager().findJobsByProcessInstanceId(processInstanceId);
    List<JobDefinitionEntity> currentJobDefinitions =
        commandContext.getJobDefinitionManager().findByProcessDefinitionId(currentProcessDefinition.getId());
    List<JobDefinitionEntity> newVersionJobDefinitions =
        commandContext.getJobDefinitionManager().findByProcessDefinitionId(newProcessDefinition.getId());

    Map<String, String> jobDefinitionMapping = getJobDefinitionMapping(currentJobDefinitions, newVersionJobDefinitions);
    for (JobEntity jobEntity : jobs) {
      switchVersionOfJob(jobEntity, newProcessDefinition, jobDefinitionMapping);
    }

    // switch all incidents to the new process definition version
    List<IncidentEntity> incidents = commandContext.getIncidentManager().findIncidentsByProcessInstance(processInstanceId);
    for (IncidentEntity incidentEntity : incidents) {
      switchVersionOfIncident(commandContext, incidentEntity, newProcessDefinition);
    }

    // add an entry to the op log
    PropertyChange change = new PropertyChange("processDefinitionVersion", currentProcessDefinition.getVersion(), processDefinitionVersion);
    commandContext.getOperationLogManager().logProcessInstanceOperation(
        UserOperationLogEntry.OPERATION_TYPE_MODIFY_PROCESS_INSTANCE,
        processInstanceId,
        null,
        null,
        Collections.singletonList(change));

    return null;
  }

  protected Map<String, String> getJobDefinitionMapping(List<JobDefinitionEntity> currentJobDefinitions, List<JobDefinitionEntity> newVersionJobDefinitions) {
    Map<String, String> mapping = new HashMap<String, String>();

    for (JobDefinitionEntity currentJobDefinition : currentJobDefinitions) {
      for (JobDefinitionEntity newJobDefinition : newVersionJobDefinitions) {
        if (jobDefinitionsMatch(currentJobDefinition, newJobDefinition)) {
          mapping.put(currentJobDefinition.getId(), newJobDefinition.getId());
          break;
        }
      }
    }

    return mapping;
  }

  protected boolean jobDefinitionsMatch(JobDefinitionEntity currentJobDefinition, JobDefinitionEntity newJobDefinition) {
    boolean activitiesMatch = currentJobDefinition.getActivityId().equals(newJobDefinition.getActivityId());

    boolean typesMatch =
        (currentJobDefinition.getJobType() == null && newJobDefinition.getJobType() == null)
          ||
        (currentJobDefinition.getJobType() != null
          && currentJobDefinition.getJobType().equals(newJobDefinition.getJobType()));

    boolean configurationsMatch =
        (currentJobDefinition.getJobConfiguration() == null && newJobDefinition.getJobConfiguration() == null)
          ||
        (currentJobDefinition.getJobConfiguration() != null
          && currentJobDefinition.getJobConfiguration().equals(newJobDefinition.getJobConfiguration()));

    return activitiesMatch && typesMatch && configurationsMatch;
  }

  protected void switchVersionOfJob(JobEntity jobEntity, ProcessDefinitionEntity newProcessDefinition, Map<String, String> jobDefinitionMapping) {
    jobEntity.setProcessDefinitionId(newProcessDefinition.getId());
    jobEntity.setDeploymentId(newProcessDefinition.getDeploymentId());

    String newJobDefinitionId = jobDefinitionMapping.get(jobEntity.getJobDefinitionId());
    jobEntity.setJobDefinitionId(newJobDefinitionId);
  }

  protected void switchVersionOfIncident(CommandContext commandContext, IncidentEntity incidentEntity, ProcessDefinitionEntity newProcessDefinition) {
    incidentEntity.setProcessDefinitionId(newProcessDefinition.getId());
  }

  protected void validateAndSwitchVersionOfExecution(CommandContext commandContext, ExecutionEntity execution, ProcessDefinitionEntity newProcessDefinition) {
    // check that the new process definition version contains the current activity
    if (execution.getActivity() != null) {
      String activityId = execution.getActivity().getId();
      PvmActivity newActivity = newProcessDefinition.findActivity(activityId);

      if (newActivity == null) {
        throw new ProcessEngineException(
          "The new process definition " +
          "(key = '" + newProcessDefinition.getKey() + "') " +
          "does not contain the current activity " +
          "(id = '" + activityId + "') " +
          "of the process instance " +
          "(id = '" + processInstanceId + "').");
        }

        // clear cached activity so that outgoing transitions are refreshed
        execution.setActivity(newActivity);
      }

    // switch the process instance to the new process definition version
    execution.setProcessDefinition(newProcessDefinition);

    // and change possible existing tasks (as the process definition id is stored there too)
    List<TaskEntity> tasks = commandContext.getTaskManager().findTasksByExecutionId(execution.getId());
    for (TaskEntity taskEntity : tasks) {
      taskEntity.setProcessDefinitionId(newProcessDefinition.getId());
    }
  }

}
