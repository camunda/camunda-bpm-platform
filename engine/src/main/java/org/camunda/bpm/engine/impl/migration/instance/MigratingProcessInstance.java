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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.impl.ActivityExecutionTreeMapping;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.camunda.bpm.engine.impl.cmd.GetActivityInstanceCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.migration.MigrationLogger;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
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
    MigratingActivityInstance migratingInstance = null;
    if (sourceScope.isScope()) {
      migratingInstance = new MigratingScopeActivityInstance();
    }
    else {
      migratingInstance = new MigratingNonScopeActivityInstance();
    }

    migratingInstance.migrationInstruction = migrationInstruction;
    migratingInstance.activityInstance = activityInstance;
    migratingInstance.sourceScope = sourceScope;
    migratingInstance.targetScope = targetScope;
    migratingInstance.representativeExecution = scopeExecution;
    migratingActivityInstances.put(activityInstance.getId(), migratingInstance);

    return migratingInstance;
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

      List<MigrationInstruction> instructionCandidates = organizedInstructions.get(sourceActivity.getId());
      MigrationInstruction applyingInstruction = null;
      ActivityImpl targetActivity = null;

      if (instructionCandidates != null && instructionCandidates.size() > 0) {
        // TODO: this could be more than one when we support conditional instructions
        applyingInstruction = instructionCandidates.get(0);
        targetActivity = targetProcessDefinition.findActivity(applyingInstruction.getTargetActivityIds().get(0));

      }
      else {
        if (instance.getChildActivityInstances().length == 0) {
          unmappedLeafInstances.add(instance);
        }
      }

      MigratingActivityInstance migratingInstance = migratingProcessInstance.addActivityInstance(
        applyingInstruction,
        instance,
        sourceActivity,
        targetActivity,
        mapping.getExecution(instance));

      if (sourceActivity.getActivityBehavior() instanceof UserTaskActivityBehavior) {
        List<TaskEntity> tasks = migratingInstance.representativeExecution.getTasks();
        migratingInstance.addDependentInstance(new MigratingTaskInstance(tasks.get(0), migratingInstance));
      }
    }

    if (!unmappedLeafInstances.isEmpty()) {
      throw LOGGER.unmappedActivityInstances(processInstance.getId(), unmappedLeafInstances);
    }

    initializeParentChildRelationships(migratingProcessInstance);

    return migratingProcessInstance;
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
