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
package org.camunda.bpm.engine.impl.migration.instance.parser;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.migration.instance.MigratingActivityInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingProcessInstance;
import org.camunda.bpm.engine.impl.migration.validation.instance.MigratingProcessInstanceValidationReportImpl;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.IncidentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.tree.TreeVisitor;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.runtime.ActivityInstance;

/**
 * Builds a {@link MigratingProcessInstance}, a data structure that contains meta-data for the activity
 * instances that are migrated.
 *
 * @author Thorben Lindhauer
 */
public class MigratingInstanceParser {

  protected ProcessEngine engine;

  protected MigratingInstanceParseHandler<ActivityInstance> activityInstanceHandler =
      new ActivityInstanceHandler();
  protected MigratingDependentInstanceParseHandler<MigratingActivityInstance, List<JobEntity>> dependentJobHandler =
      new JobInstanceHandler();
  protected MigratingDependentInstanceParseHandler<MigratingActivityInstance, List<EventSubscriptionEntity>> dependentEventSubscriptionHandler =
      new EventSubscriptionInstanceHandler();
  protected MigratingDependentInstanceParseHandler<MigratingActivityInstance, List<TaskEntity>> dependentTaskHandler =
      new TaskInstanceHandler();
  protected MigratingInstanceParseHandler<IncidentEntity> incidentHandler =
      new IncidentInstanceHandler();

  public MigratingInstanceParser(ProcessEngine engine) {
    this.engine = engine;
  }

  public MigratingProcessInstance parse(String processInstanceId, MigrationPlan migrationPlan, MigratingProcessInstanceValidationReportImpl processInstanceReport) {

    CommandContext commandContext = Context.getCommandContext();
    List<ExecutionEntity> executions = fetchExecutions(commandContext, processInstanceId);
    List<EventSubscriptionEntity> eventSubscriptions = fetchEventSubscriptions(commandContext, processInstanceId);
    List<TaskEntity> tasks = fetchTasks(commandContext, processInstanceId);
    List<JobEntity> jobs = fetchJobs(commandContext, processInstanceId);
    List<IncidentEntity> incidents = fetchIncidents(commandContext, processInstanceId);

    ExecutionEntity processInstance = commandContext.getExecutionManager().findExecutionById(processInstanceId);
    processInstance.restoreProcessInstance(executions, eventSubscriptions, null, tasks, jobs, incidents);

    ProcessDefinitionEntity targetProcessDefinition = Context
      .getProcessEngineConfiguration()
      .getDeploymentCache()
      .findDeployedProcessDefinitionById(migrationPlan.getTargetProcessDefinitionId());
    List<JobDefinitionEntity> targetJobDefinitions = fetchJobDefinitions(commandContext, targetProcessDefinition.getId());

    final MigratingInstanceParseContext parseContext = new MigratingInstanceParseContext(this, migrationPlan, processInstance, targetProcessDefinition)
      .jobs(jobs)
      .eventSubscriptions(eventSubscriptions)
      .incidents(incidents)
      .tasks(tasks)
      .targetJobDefinitions(targetJobDefinitions);

    ActivityInstance activityInstance = engine.getRuntimeService().getActivityInstance(processInstanceId);

    ActivityInstanceWalker activityInstanceWalker = new ActivityInstanceWalker(activityInstance);

    activityInstanceWalker.addPreVisitor(new TreeVisitor<ActivityInstance>() {
      @Override
      public void visit(ActivityInstance obj) {
        activityInstanceHandler.handle(parseContext, obj);
      }
    });

    activityInstanceWalker.walkWhile();

    for (IncidentEntity incidentEntity : incidents) {
      incidentHandler.handle(parseContext, incidentEntity);
    }

    parseContext.validateNoEntitiesLeft(processInstanceReport);

    return parseContext.getMigratingProcessInstance();
  }

  public MigratingInstanceParseHandler<ActivityInstance> getActivityInstanceHandler() {
    return activityInstanceHandler;
  }

  public MigratingDependentInstanceParseHandler<MigratingActivityInstance, List<EventSubscriptionEntity>> getDependentEventSubscriptionHandler() {
    return dependentEventSubscriptionHandler;
  }

  public MigratingDependentInstanceParseHandler<MigratingActivityInstance, List<JobEntity>> getDependentJobHandler() {
    return dependentJobHandler;
  }

  public MigratingDependentInstanceParseHandler<MigratingActivityInstance, List<TaskEntity>> getDependentTaskHandler() {
    return dependentTaskHandler;
  }

  public MigratingInstanceParseHandler<IncidentEntity> getIncidentHandler() {
    return incidentHandler;
  }

  protected List<ExecutionEntity> fetchExecutions(CommandContext commandContext, final String processInstanceId) {
    return commandContext.getExecutionManager().findExecutionsByProcessInstanceId(processInstanceId);
  }

  protected List<EventSubscriptionEntity> fetchEventSubscriptions(CommandContext commandContext, final String processInstanceId) {
    return commandContext.getEventSubscriptionManager().findEventSubscriptionsByProcessInstanceId(processInstanceId);
  }

  protected List<JobEntity> fetchJobs(CommandContext commandContext, String processInstanceId) {
    return commandContext.getJobManager().findJobsByProcessInstanceId(processInstanceId);
  }

  protected List<IncidentEntity> fetchIncidents(CommandContext commandContext, String processInstanceId) {
    return commandContext.getIncidentManager().findIncidentsByProcessInstance(processInstanceId);
  }

  protected List<TaskEntity> fetchTasks(CommandContext commandContext, String processInstanceId) {
    return commandContext.getTaskManager().findTasksByProcessInstanceId(processInstanceId);
  }

  protected List<JobDefinitionEntity> fetchJobDefinitions(CommandContext commandContext, String processDefinitionId) {
    return commandContext.getJobDefinitionManager().findByProcessDefinitionId(processDefinitionId);
  }
}
