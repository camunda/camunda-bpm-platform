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
package org.camunda.bpm.engine.impl.migration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.impl.ActivityExecutionTreeMapping;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cmd.GetActivityInstanceCmd;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.tree.ActivityStackCollector;
import org.camunda.bpm.engine.impl.tree.FlowScopeWalker;
import org.camunda.bpm.engine.impl.tree.TreeWalker.WalkCondition;
import org.camunda.bpm.engine.migration.MigrationInstruction;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.runtime.ActivityInstance;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrateProcessInstanceCmd implements Command<Void> {

  protected MigrationPlan migrationPlan;
  protected List<String> processInstanceIds;

  protected static final MigrationLogger LOGGER = ProcessEngineLogger.MIGRATION_LOGGER;


  public MigrateProcessInstanceCmd(MigrationPlan migrationPlan, List<String> processInstanceIds) {
    this.migrationPlan = migrationPlan;
    this.processInstanceIds = processInstanceIds;
  }

  public Void execute(CommandContext commandContext) {
    for (String processInstanceId : processInstanceIds) {
      migrateProcessInstance(commandContext, processInstanceId);
    }
    return null;
  }

  public Void migrateProcessInstance(CommandContext commandContext, String processInstanceId) {
    ProcessDefinitionEntity targetProcessDefinition = Context.getProcessEngineConfiguration()
        .getDeploymentCache().findDeployedProcessDefinitionById(migrationPlan.getTargetProcessDefinitionId());

    ExecutionEntity processInstance = commandContext.getExecutionManager().findExecutionById(processInstanceId);

    ActivityInstance activityInstanceTree = new GetActivityInstanceCmd(processInstanceId).execute(commandContext);
    List<MigratingActivityInstance> migratingInstances = new ArrayList<MigratingActivityInstance>();

    Set<ActivityInstance> unmappedLeafInstances = collectLeafInstances(activityInstanceTree);

    // 1. collect activity instances that are migrated
    for (MigrationInstruction instruction : migrationPlan.getInstructions()) {
      ActivityInstance[] instancesForSourceActivity =
          activityInstanceTree.getActivityInstances(instruction.getSourceActivityIds().get(0));

      for (ActivityInstance instance : instancesForSourceActivity) {
        MigratingActivityInstance migratingInstance = new MigratingActivityInstance();
        migratingInstance.activityInstanceId = instance.getId();
        migratingInstance.instruction = instruction;

        String taskExecutionId = instance.getExecutionIds()[0];
        List<TaskEntity> tasksByExecutionId = Context.getCommandContext().getTaskManager().findTasksByExecutionId(taskExecutionId);
        migratingInstance.userTask = tasksByExecutionId.get(0);
        migratingInstances.add(migratingInstance);

        unmappedLeafInstances.remove(instance);
      }
    }

    if (!unmappedLeafInstances.isEmpty()) {
      throw LOGGER.unmappedActivityInstances(processInstanceId, unmappedLeafInstances);
    }

    // 2. update process definition IDs
    processInstance.setProcessDefinition(targetProcessDefinition);

    for (MigratingActivityInstance migratingInstance : migratingInstances) {
      migratingInstance.userTask.setProcessDefinitionId(targetProcessDefinition.getId());
    }

    return null;
  }

  protected Set<ActivityInstance> collectLeafInstances(ActivityInstance activityInstanceTree) {
    if (activityInstanceTree.getChildActivityInstances().length == 0) {
      return Collections.singleton(activityInstanceTree);
    }
    else {
      Set<ActivityInstance> leafInstances = new HashSet<ActivityInstance>();
      for (ActivityInstance childInstance : activityInstanceTree.getChildActivityInstances()) {
        leafInstances.addAll(collectLeafInstances(childInstance));
      }

      return leafInstances;
    }
  }

  protected List<PvmActivity> collectFlowScopes(final ActivityImpl sourceActivity, final ActivityExecutionTreeMapping mapping) {
    ActivityStackCollector stackCollector = new ActivityStackCollector();
    FlowScopeWalker walker = new FlowScopeWalker(sourceActivity.isScope() ? sourceActivity : sourceActivity.getFlowScope());
    walker.addPreVisitor(stackCollector);

    // walk until a scope is reached for which executions exist
    walker.walkWhile(new WalkCondition<ScopeImpl>() {
      public boolean isFulfilled(ScopeImpl element) {
        return !mapping.getExecutions(element).isEmpty() || element == sourceActivity.getProcessDefinition();
      }
    });

    return stackCollector.getActivityStack();
  }

  public static class MigratingActivityInstance {
    protected String activityInstanceId;
    protected TaskEntity userTask;
    protected MigrationInstruction instruction;
  }


}
