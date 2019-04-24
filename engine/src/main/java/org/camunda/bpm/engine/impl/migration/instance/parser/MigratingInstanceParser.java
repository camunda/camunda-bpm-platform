/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import org.camunda.bpm.engine.impl.migration.instance.MigratingProcessElementInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingProcessInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingTransitionInstance;
import org.camunda.bpm.engine.impl.migration.validation.instance.MigratingProcessInstanceValidationReportImpl;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExternalTaskEntity;
import org.camunda.bpm.engine.impl.persistence.entity.IncidentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.camunda.bpm.engine.impl.tree.TreeVisitor;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.TransitionInstance;

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
  protected MigratingInstanceParseHandler<TransitionInstance> transitionInstanceHandler =
      new TransitionInstanceHandler();
  protected MigratingInstanceParseHandler<EventSubscriptionEntity> compensationInstanceHandler =
      new CompensationInstanceHandler();

  protected MigratingDependentInstanceParseHandler<MigratingActivityInstance, List<JobEntity>> dependentActivityInstanceJobHandler =
      new ActivityInstanceJobHandler();
  protected MigratingDependentInstanceParseHandler<MigratingTransitionInstance, List<JobEntity>> dependentTransitionInstanceJobHandler =
      new TransitionInstanceJobHandler();
  protected MigratingDependentInstanceParseHandler<MigratingActivityInstance, List<EventSubscriptionEntity>> dependentEventSubscriptionHandler =
      new EventSubscriptionInstanceHandler();
  protected MigratingDependentInstanceParseHandler<MigratingProcessElementInstance, List<VariableInstanceEntity>> dependentVariableHandler =
      new VariableInstanceHandler();
  protected MigratingInstanceParseHandler<IncidentEntity> incidentHandler =
      new IncidentInstanceHandler();

  public MigratingInstanceParser(ProcessEngine engine) {
    this.engine = engine;
  }

  public MigratingProcessInstance parse(String processInstanceId, MigrationPlan migrationPlan, MigratingProcessInstanceValidationReportImpl processInstanceReport) {

    CommandContext commandContext = Context.getCommandContext();
    List<EventSubscriptionEntity> eventSubscriptions = fetchEventSubscriptions(commandContext, processInstanceId);
    List<ExecutionEntity> executions = fetchExecutions(commandContext, processInstanceId);
    List<ExternalTaskEntity> externalTasks = fetchExternalTasks(commandContext, processInstanceId);
    List<IncidentEntity> incidents = fetchIncidents(commandContext, processInstanceId);
    List<JobEntity> jobs = fetchJobs(commandContext, processInstanceId);
    List<TaskEntity> tasks = fetchTasks(commandContext, processInstanceId);
    List<VariableInstanceEntity> variables = fetchVariables(commandContext, processInstanceId);

    ExecutionEntity processInstance = commandContext.getExecutionManager().findExecutionById(processInstanceId);
    processInstance.restoreProcessInstance(executions, eventSubscriptions, variables, tasks, jobs, incidents, externalTasks);

    ProcessDefinitionEntity targetProcessDefinition = Context
      .getProcessEngineConfiguration()
      .getDeploymentCache()
      .findDeployedProcessDefinitionById(migrationPlan.getTargetProcessDefinitionId());
    List<JobDefinitionEntity> targetJobDefinitions = fetchJobDefinitions(commandContext, targetProcessDefinition.getId());

    final MigratingInstanceParseContext parseContext = new MigratingInstanceParseContext(this, migrationPlan, processInstance, targetProcessDefinition)
      .eventSubscriptions(eventSubscriptions)
      .externalTasks(externalTasks)
      .incidents(incidents)
      .jobs(jobs)
      .tasks(tasks)
      .targetJobDefinitions(targetJobDefinitions)
      .variables(variables);

    ActivityInstance activityInstance = engine.getRuntimeService().getActivityInstance(processInstanceId);

    ActivityInstanceWalker activityInstanceWalker = new ActivityInstanceWalker(activityInstance);

    activityInstanceWalker.addPreVisitor(new TreeVisitor<ActivityInstance>() {
      @Override
      public void visit(ActivityInstance obj) {
        activityInstanceHandler.handle(parseContext, obj);
      }
    });

    activityInstanceWalker.walkWhile();

    CompensationEventSubscriptionWalker compensateSubscriptionsWalker = new CompensationEventSubscriptionWalker(
        parseContext.getMigratingActivityInstances());

    compensateSubscriptionsWalker.addPreVisitor(new TreeVisitor<EventSubscriptionEntity>() {
      @Override
      public void visit(EventSubscriptionEntity obj) {
        compensationInstanceHandler.handle(parseContext, obj);
      }
    });

    compensateSubscriptionsWalker.walkWhile();

    for (IncidentEntity incidentEntity : incidents) {
      incidentHandler.handle(parseContext, incidentEntity);
    }

    parseContext.validateNoEntitiesLeft(processInstanceReport);

    return parseContext.getMigratingProcessInstance();
  }

  public MigratingInstanceParseHandler<ActivityInstance> getActivityInstanceHandler() {
    return activityInstanceHandler;
  }

  public MigratingInstanceParseHandler<TransitionInstance> getTransitionInstanceHandler() {
    return transitionInstanceHandler;
  }

  public MigratingDependentInstanceParseHandler<MigratingActivityInstance, List<EventSubscriptionEntity>> getDependentEventSubscriptionHandler() {
    return dependentEventSubscriptionHandler;
  }

  public MigratingDependentInstanceParseHandler<MigratingActivityInstance, List<JobEntity>> getDependentActivityInstanceJobHandler() {
    return dependentActivityInstanceJobHandler;
  }

  public MigratingDependentInstanceParseHandler<MigratingTransitionInstance, List<JobEntity>> getDependentTransitionInstanceJobHandler() {
    return dependentTransitionInstanceJobHandler;
  }

  public MigratingInstanceParseHandler<IncidentEntity> getIncidentHandler() {
    return incidentHandler;
  }

  public MigratingDependentInstanceParseHandler<MigratingProcessElementInstance, List<VariableInstanceEntity>> getDependentVariablesHandler() {
    return dependentVariableHandler;
  }

  protected List<ExecutionEntity> fetchExecutions(CommandContext commandContext, String processInstanceId) {
    return commandContext.getExecutionManager().findExecutionsByProcessInstanceId(processInstanceId);
  }

  protected List<EventSubscriptionEntity> fetchEventSubscriptions(CommandContext commandContext, String processInstanceId) {
    return commandContext.getEventSubscriptionManager().findEventSubscriptionsByProcessInstanceId(processInstanceId);
  }

  protected List<ExternalTaskEntity> fetchExternalTasks(CommandContext commandContext, String processInstanceId) {
    return commandContext.getExternalTaskManager().findExternalTasksByProcessInstanceId(processInstanceId);
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

  protected List<VariableInstanceEntity> fetchVariables(CommandContext commandContext, String processInstanceId) {
    return commandContext.getVariableInstanceManager().findVariableInstancesByProcessInstanceId(processInstanceId);
  }

}
