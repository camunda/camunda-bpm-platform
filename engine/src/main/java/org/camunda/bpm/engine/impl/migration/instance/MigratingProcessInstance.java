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
package org.camunda.bpm.engine.impl.migration.instance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.impl.ActivityExecutionTreeMapping;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.parser.EventSubscriptionDeclaration;
import org.camunda.bpm.engine.impl.cmd.GetActivityInstanceCmd;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.TimerDeclarationImpl;
import org.camunda.bpm.engine.impl.migration.MigrationLogger;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.util.CollectionUtil;
import org.camunda.bpm.engine.migration.MigrationInstruction;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.runtime.ActivityInstance;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigratingProcessInstance {

  protected static final MigrationLogger LOGGER = ProcessEngineLogger.MIGRATION_LOGGER;

  protected String processInstanceId;
  protected Map<String, MigratingActivityInstance> migratingActivityInstances;

  public MigratingProcessInstance(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    this.migratingActivityInstances = new HashMap<String, MigratingActivityInstance>();
  }

  public Collection<MigratingActivityInstance> getMigratingActivityInstances() {
    return migratingActivityInstances.values();
  }

  public MigratingActivityInstance getMigratingInstance(String activityInstanceId) {
    return migratingActivityInstances.get(activityInstanceId);
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  protected MigratingActivityInstance addActivityInstance(
      MigrationInstruction migrationInstruction,
      ActivityInstance activityInstance,
      ScopeImpl sourceScope,
      ScopeImpl targetScope,
      ExecutionEntity scopeExecution) {

    MigratingActivityInstance migratingActivityInstance = new MigratingActivityInstance(
        activityInstance,
        migrationInstruction,
        sourceScope,
        targetScope,
        scopeExecution);

    migratingActivityInstances.put(activityInstance.getId(), migratingActivityInstance);

    return migratingActivityInstance;
  }


  /**
   * Returns a {@link MigratingProcessInstance}, a data structure that contains meta-data for the activity
   * instances that are migrated. Throws an exception if not all leaf activity instances can be migrated (e.g.
   * because no migration instruction applies to them)
   */
  public static MigratingProcessInstance initializeFrom(CommandContext commandContext,
      MigrationPlan migrationPlan,
      ExecutionEntity processInstance,
      ProcessDefinitionImpl targetProcessDefinition) {

    MigratingProcessInstance migratingProcessInstance = new MigratingProcessInstance(processInstance.getId());
    ProcessDefinitionImpl sourceProcessDefinition = processInstance.getProcessDefinition();
    final ActivityExecutionTreeMapping mapping = new ActivityExecutionTreeMapping(commandContext, processInstance.getId());

    ActivityInstance activityInstanceTree = new GetActivityInstanceCmd(processInstance.getId())
      .execute(commandContext);
    Set<ActivityInstance> activityInstances = flatten(activityInstanceTree);
    Set<ActivityInstance> unmappedLeafInstances = new HashSet<ActivityInstance>();

    // always create an entry for the root activity instance because it is implicitly always migrated
    migratingProcessInstance.addActivityInstance(
        null,
        activityInstanceTree,
        sourceProcessDefinition,
        targetProcessDefinition,
        processInstance);
    activityInstances.remove(activityInstanceTree);

    Map<String, List<MigrationInstruction>> organizedInstructions = organizeInstructionsBySourceScope(migrationPlan);

    for (ActivityInstance instance : activityInstances) {
      ActivityImpl sourceActivity = sourceProcessDefinition.findActivity(instance.getActivityId());

      MigrationInstruction applyingInstruction = findMigrationInstructionForActivityId(sourceActivity.getId(), organizedInstructions);
      ActivityImpl targetActivity = null;

      if (applyingInstruction != null) {
        targetActivity = findTargetActivityForInstruction(applyingInstruction, targetProcessDefinition);
      }
      else if (isLeafActivity(instance)) {
          unmappedLeafInstances.add(instance);
      }

      MigratingActivityInstance migratingInstance = migratingProcessInstance.addActivityInstance(
        applyingInstruction,
        instance,
        sourceActivity,
        targetActivity,
        mapping.getExecution(instance));

      if (migratingInstance.migrates()) {
        initializeDependentInstances(migratingInstance, sourceActivity, targetProcessDefinition, organizedInstructions);
      }

    }

    if (!unmappedLeafInstances.isEmpty()) {
      throw LOGGER.unmappedActivityInstances(processInstance.getId(), unmappedLeafInstances);
    }

    initializeParentChildRelationships(migratingProcessInstance);

    return migratingProcessInstance;
  }

  protected static boolean isLeafActivity(ActivityInstance instance) {
    return instance.getChildActivityInstances().length == 0;
  }

  protected static void initializeDependentInstances(MigratingActivityInstance migratingInstance, ActivityImpl sourceActivity, ProcessDefinitionImpl targetProcessDefinition, Map<String, List<MigrationInstruction>> organizedInstructions) {

    initializeDependentTaskInstances(migratingInstance, sourceActivity);
    initializeDependentEventSubscriptionInstances(migratingInstance, targetProcessDefinition, organizedInstructions);
    initializeDependentTimerJobInstances(migratingInstance, targetProcessDefinition, organizedInstructions);

  }

  protected static void initializeDependentTaskInstances(MigratingActivityInstance migratingInstance, ActivityImpl sourceActivity) {
    if (sourceActivity.getActivityBehavior() instanceof UserTaskActivityBehavior) {
      List<TaskEntity> tasks = migratingInstance.representativeExecution.getTasks();
      migratingInstance.addMigratingDependentInstance(new MigratingTaskInstance(tasks.get(0), migratingInstance));
    }
  }

  protected static void initializeDependentEventSubscriptionInstances(MigratingActivityInstance migratingInstance, ProcessDefinitionImpl targetProcessDefinition, Map<String, List<MigrationInstruction>> organizedInstructions) {
    List<String> migratedEventSubscriptionTargetActivityIds = new ArrayList<String>();

    for (EventSubscriptionEntity eventSubscription : migratingInstance.representativeExecution.getEventSubscriptions()) {
      MigrationInstruction eventSubscriptionMigrationInstruction = findMigrationInstructionForActivityId(eventSubscription.getActivityId(), organizedInstructions);
      if (eventSubscriptionMigrationInstruction != null) {
        // the event subscription is migrated
        ActivityImpl eventSubscriptionTargetActivity = findTargetActivityForInstruction(eventSubscriptionMigrationInstruction, targetProcessDefinition);
        migratedEventSubscriptionTargetActivityIds.add(eventSubscriptionTargetActivity.getId());
        migratingInstance.addMigratingDependentInstance(new MigratingEventSubscriptionInstance(eventSubscription, eventSubscriptionTargetActivity));

      } else {
        // the event subscription will be removed
        migratingInstance.addRemovingDependentInstance(new MigratingEventSubscriptionInstance(eventSubscription));

      }
    }

    List<EventSubscriptionDeclaration> emergingEventSubscriptionDeclarations = findEmergingEventSubscriptionDeclarations(migratingInstance, migratedEventSubscriptionTargetActivityIds);
    for (EventSubscriptionDeclaration emergingEventSubscriptionDeclaration : emergingEventSubscriptionDeclarations) {
      // the event subscription will be created
      migratingInstance.addEmergingDependentInstance(new MigratingEventSubscriptionInstance(emergingEventSubscriptionDeclaration));
    }

  }

  protected static List<EventSubscriptionDeclaration> findEmergingEventSubscriptionDeclarations(MigratingActivityInstance migratingInstance, List<String> migratedEventSubscriptionTargetActivityIds) {
    List<EventSubscriptionDeclaration> emergingEventSubscriptionDeclarations = new ArrayList<EventSubscriptionDeclaration>();

    for (EventSubscriptionDeclaration eventSubscriptionDeclaration : EventSubscriptionDeclaration.getDeclarationsForScope(migratingInstance.getTargetScope())) {
      if (!migratedEventSubscriptionTargetActivityIds.contains(eventSubscriptionDeclaration.getActivityId())) {
        emergingEventSubscriptionDeclarations.add(eventSubscriptionDeclaration);
      }
    }

    return emergingEventSubscriptionDeclarations;
  }

  protected static void initializeDependentTimerJobInstances(MigratingActivityInstance migratingInstance, ProcessDefinitionImpl targetProcessDefinition, Map<String, List<MigrationInstruction>> organizedInstructions) {
    Map<String, JobDefinitionEntity> jobDefinitionsByActivityId = collectJobDefinitionsForActivityIds(targetProcessDefinition.getId());

    List<String> migratedTimerJobTargetActivityIds = new ArrayList<String>();

    for (JobEntity job : migratingInstance.representativeExecution.getJobs()) {
      if (!isTimerJob(job)) {
        // skip non timer jobs
        continue;
      }

      MigrationInstruction timerJobMigrationInstruction = findMigrationInstructionForActivityId(job.getActivityId(), organizedInstructions);
      if (timerJobMigrationInstruction != null) {
        // the timer job is migrated
        ActivityImpl timerJobTargetActivity = findTargetActivityForInstruction(timerJobMigrationInstruction, targetProcessDefinition);
        migratedTimerJobTargetActivityIds.add(timerJobTargetActivity.getId());
        JobDefinitionEntity jobDefinitionEntity = jobDefinitionsByActivityId.get(timerJobTargetActivity.getActivityId());
        migratingInstance.addMigratingDependentInstance(new MigratingTimerJobInstance(job, jobDefinitionEntity, timerJobTargetActivity));

      }
      else {
        // the timer job is removed
        migratingInstance.addRemovingDependentInstance(new MigratingTimerJobInstance(job));

      }
    }

    for (TimerDeclarationImpl emergingTimerDeclaration: findEmergingTimerDeclarations(migratingInstance, migratedTimerJobTargetActivityIds)) {
      // the timer job will be created
      migratingInstance.addEmergingDependentInstance(new MigratingTimerJobInstance(emergingTimerDeclaration));
    }
  }

  protected static List<TimerDeclarationImpl> findEmergingTimerDeclarations(MigratingActivityInstance migratingInstance, List<String> migratedTimerJobTargetActivityIds) {
    List<TimerDeclarationImpl> emergingTimerDeclarations = new ArrayList<TimerDeclarationImpl>();

    for (TimerDeclarationImpl timerDeclaration : TimerDeclarationImpl.getDeclarationsForScope(migratingInstance.getTargetScope())) {
      if (!migratedTimerJobTargetActivityIds.contains(timerDeclaration.getActivityId())) {
        emergingTimerDeclarations.add(timerDeclaration);
      }
    }

    return emergingTimerDeclarations;
  }

  protected static Set<ActivityInstance> flatten(ActivityInstance activityInstance) {
    Set<ActivityInstance> instances = new HashSet<ActivityInstance>();

    instances.add(activityInstance);

    for (ActivityInstance childInstance : activityInstance.getChildActivityInstances()) {
      instances.addAll(flatten(childInstance));
    }

    return instances;
  }

  protected static Map<String, List<MigrationInstruction>> organizeInstructionsBySourceScope(MigrationPlan migrationPlan) {
    Map<String, List<MigrationInstruction>> organizedInstructions = new HashMap<String, List<MigrationInstruction>>();

    for (MigrationInstruction instruction : migrationPlan.getInstructions()) {
      CollectionUtil.addToMapOfLists(organizedInstructions, instruction.getSourceActivityIds().get(0), instruction);
    }

    return organizedInstructions;
  }

  protected static ActivityImpl findTargetActivityForInstruction(MigrationInstruction instruction, ProcessDefinitionImpl processDefinition) {
    String activityId = instruction.getTargetActivityIds().get(0);
    return processDefinition.findActivity(activityId);
  }

  protected static Map<String,JobDefinitionEntity> collectJobDefinitionsForActivityIds(String processDefinitionId) {
    List<JobDefinitionEntity> jobDefinitions = Context.getCommandContext().getJobDefinitionManager().findByProcessDefinitionId(processDefinitionId);
    Map<String, JobDefinitionEntity> jobDefinitionsByActivityId = new HashMap<String, JobDefinitionEntity>();
    for (JobDefinitionEntity jobDefinition : jobDefinitions) {
      jobDefinitionsByActivityId.put(jobDefinition.getActivityId(), jobDefinition);
    }

    return jobDefinitionsByActivityId;
  }

  protected static MigrationInstruction findMigrationInstructionForActivityId(String activityId, Map<String, List<MigrationInstruction>> organizedInstructions) {
    List<MigrationInstruction> migrationInstructions = organizedInstructions.get(activityId);
    if (migrationInstructions != null && !migrationInstructions.isEmpty()) {
      // TODO: this could be more than one when we support conditional instructions
      return migrationInstructions.get(0);
    }
    else {
      return null;
    }
  }

  protected static boolean isTimerJob(JobEntity job) {
    return job != null && job.getType().equals(TimerEntity.TYPE);
  }

  protected static void initializeParentChildRelationships(MigratingProcessInstance migratingProcessInstance) {
    for (MigratingActivityInstance migratingActivityInstance : migratingProcessInstance.migratingActivityInstances.values()) {
      ActivityInstance activityInstance = migratingActivityInstance.getActivityInstance();

      migratingActivityInstance.parentInstance = migratingProcessInstance.getMigratingInstance(activityInstance.getParentActivityInstanceId());
      migratingActivityInstance.childInstances = new HashSet<MigratingActivityInstance>();

      for (ActivityInstance childActivityInstance : activityInstance.getChildActivityInstances()) {
        migratingActivityInstance.childInstances.add(
            migratingProcessInstance.getMigratingInstance(childActivityInstance.getId()));
      }
    }
  }
}
