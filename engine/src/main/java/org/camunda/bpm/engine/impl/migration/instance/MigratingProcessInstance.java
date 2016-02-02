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
    Set<ActivityInstance> unmappedInstances = collectLeafInstances(activityInstanceTree);

    // always create an entry for the root activity instance because it is implicitly always migrated
    migratingProcessInstance.addActivityInstance(
        activityInstanceTree,
        sourceProcessDefinition,
        targetProcessDefinition,
        processInstance);
    unmappedInstances.remove(activityInstanceTree);

    for (MigrationInstruction instruction : migrationPlan.getInstructions()) {
      ActivityInstance[] instancesForSourceActivity =
          activityInstanceTree.getActivityInstances(instruction.getSourceActivityIds().get(0));

      for (ActivityInstance instance : instancesForSourceActivity) {
        ActivityImpl sourceActivity = sourceProcessDefinition.findActivity(instance.getActivityId());
        ActivityImpl targetActivity = targetProcessDefinition.findActivity(instruction.getTargetActivityIds().get(0));
        MigratingActivityInstance migratingInstance = migratingProcessInstance.addActivityInstance(
            instance,
            sourceActivity,
            targetActivity,
            mapping.getExecution(instance));
        unmappedInstances.remove(instance);

        if (sourceActivity.getActivityBehavior() instanceof UserTaskActivityBehavior) {
          List<TaskEntity> tasks = migratingInstance.representativeExecution.getTasks();
          migratingInstance.addDependentInstance(new MigratingTaskInstance(tasks.get(0), migratingInstance));
        }
      }
    }

    if (!unmappedInstances.isEmpty()) {
      throw LOGGER.unmappedActivityInstances(processInstance.getId(), unmappedInstances);
    }

    return migratingProcessInstance;
  }

  protected static Set<ActivityInstance> collectLeafInstances(ActivityInstance activityInstanceTree) {
    Set<ActivityInstance> instances = new HashSet<ActivityInstance>();

    if (activityInstanceTree.getChildActivityInstances().length == 0) {
      instances.add(activityInstanceTree);
    }
    else {
      for (ActivityInstance childInstance : activityInstanceTree.getChildActivityInstances()) {
        instances.addAll(collectLeafInstances(childInstance));
      }
    }

    return instances;
  }
}
