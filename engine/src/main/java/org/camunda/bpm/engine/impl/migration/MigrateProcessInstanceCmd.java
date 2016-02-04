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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.migration.instance.MigratingActivityInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingActivityInstanceWalker;
import org.camunda.bpm.engine.impl.migration.instance.MigratingExecutionBranch;
import org.camunda.bpm.engine.impl.migration.instance.MigratingProcessInstance;
import org.camunda.bpm.engine.impl.migration.validation.AdditionalFlowScopeValidator;
import org.camunda.bpm.engine.impl.migration.validation.MigrationInstructionInstanceValidationReportImpl;
import org.camunda.bpm.engine.impl.migration.validation.MigrationInstructionInstanceValidator;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.tree.TreeVisitor;
import org.camunda.bpm.engine.impl.tree.TreeWalker.WalkCondition;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.runtime.ActivityInstance;

/**
 * How migration works:
 *
 * <ol>
 *   <li>Validate migration instructions.
 *   <li>Delete activity instances that are not going to be migrated, invoking execution listeners
 *       and io mappings. This is performed in a bottom-up fashion in the activity instance tree and ensures
 *       that the "upstream" tree is always consistent with respect to the old process definition.
 *   <li>Migrate and create activity instances. Creation invokes execution listeners
 *       and io mappings. This is performed in a top-down fashion in the activity instance tree and
 *       ensures that the "upstream" tree is always consistent with respect to the new process definition.
 *
 * @author Thorben Lindhauer
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

    ExecutionEntity processInstance = commandContext.getExecutionManager().findExecutionById(processInstanceId);

    ProcessDefinitionEntity targetProcessDefinition = Context.getProcessEngineConfiguration()
        .getDeploymentCache().findDeployedProcessDefinitionById(migrationPlan.getTargetProcessDefinitionId());

    // Initialize migration: match migration instructions to activity instances and collect required entities
    MigratingProcessInstance migratingProcessInstance = MigratingProcessInstance.initializeFrom(
        commandContext, migrationPlan, processInstance, targetProcessDefinition);

    validateInstructions(migratingProcessInstance);

    deleteUnmappedActivityInstances(migratingProcessInstance);

    migrateProcessInstance(migratingProcessInstance);

    return null;
  }

  /**
   * delete unmapped instances in a bottom-up fashion (similar to deleteCascade and regular BPMN execution)
   */
  protected void deleteUnmappedActivityInstances(MigratingProcessInstance migratingProcessInstance) {
    final Set<MigratingActivityInstance> visitedActivityInstances = new HashSet<MigratingActivityInstance>();
    Set<MigratingActivityInstance> leafInstances = collectLeafInstances(migratingProcessInstance);

    for (MigratingActivityInstance leafInstance : leafInstances) {
      MigratingActivityInstanceWalker walker = new MigratingActivityInstanceWalker(leafInstance);

      walker.addPreVisitor(new TreeVisitor<MigratingActivityInstance>() {

        @Override
        public void visit(MigratingActivityInstance currentInstance) {

          visitedActivityInstances.add(currentInstance);
          if (currentInstance.getTargetScope() == null) {
            Set<MigratingActivityInstance> children = currentInstance.getChildren();
            MigratingActivityInstance parent = currentInstance.getParent();

            // 1. detach children
            for (MigratingActivityInstance child : children) {
              child.detachState();
            }

            // 2. manipulate execution tree (i.e. remove this instance)
            currentInstance.remove();

            // 3. reconnect parent and children
            for (MigratingActivityInstance child : children) {
              child.attachState(parent.resolveRepresentativeExecution());
              parent.getChildren().add(child);
              child.setParent(parent);
            }
          }
        }
      });

      walker.walkUntil(new WalkCondition<MigratingActivityInstance>() {

        @Override
        public boolean isFulfilled(MigratingActivityInstance element) {
          // walk until top of instance tree is reached or until
          // a node is reached for which we have not yet visited every child
          return element == null || !visitedActivityInstances.containsAll(element.getChildren());
        }
      });
    }
  }

  protected Set<MigratingActivityInstance> collectLeafInstances(MigratingProcessInstance migratingProcessInstance) {
    Set<MigratingActivityInstance> leafInstances = new HashSet<MigratingActivityInstance>();

    for (MigratingActivityInstance migratingActivityInstance : migratingProcessInstance.getMigratingActivityInstances()) {
      if (migratingActivityInstance.getChildren().isEmpty()) {
        leafInstances.add(migratingActivityInstance);
      }
    }

    return leafInstances;
  }

  protected void validateInstructions(MigratingProcessInstance migratingProcessInstance) {

    List<MigrationInstructionInstanceValidator> validators = Arrays.<MigrationInstructionInstanceValidator>asList(new AdditionalFlowScopeValidator());
    MigrationInstructionInstanceValidationReportImpl validationReport = new MigrationInstructionInstanceValidationReportImpl(migratingProcessInstance);

    for (MigratingActivityInstance migratingActivityInstance : migratingProcessInstance.getMigratingActivityInstances()) {
      for (MigrationInstructionInstanceValidator validator : validators) {
        validator.validate(migratingProcessInstance, migratingActivityInstance, validationReport);
      }
    }

    if (validationReport.hasFailures()) {
      throw LOGGER.failingInstructionInstanceValidation(validationReport);
    }

  }

  /**
   * Migrate activity instances to their new activities and process definition. Creates new
   * scope instances as necessary.
   */
  protected void migrateProcessInstance(MigratingProcessInstance migratingProcessInstance) {
    MigratingActivityInstance rootActivityInstance =
        migratingProcessInstance.getMigratingInstance(migratingProcessInstance.getProcessInstanceId());

    MigratingExecutionBranch scopeExecutionContext = new MigratingExecutionBranch();
    scopeExecutionContext.visited(rootActivityInstance);

    migrateActivityInstance(migratingProcessInstance, scopeExecutionContext, rootActivityInstance);
  }

  protected void migrateActivityInstance(
      MigratingProcessInstance migratingProcessInstance,
      MigratingExecutionBranch migratingExecutionBranch,
      MigratingActivityInstance migratingActivityInstance) {

    ActivityInstance activityInstance = migratingActivityInstance.getActivityInstance();

    if (!activityInstance.getId().equals(activityInstance.getProcessInstanceId())) {
      final MigratingActivityInstance parentMigratingInstance = migratingActivityInstance.getParent();

      ScopeImpl targetFlowScope = migratingActivityInstance.getTargetScope().getFlowScope();
      ScopeImpl parentActivityInstanceTargetScope = parentMigratingInstance.getTargetScope();

      if (targetFlowScope != parentActivityInstanceTargetScope) {
        // create intermediate scopes

        ExecutionEntity flowScopeExecution = migratingActivityInstance.getFlowScopeExecution();

        // 1. detach activity instance
        migratingActivityInstance.detachState();

        // 2. manipulate execution tree
        ExecutionEntity targetExecution = migratingExecutionBranch.getExecution(targetFlowScope);

        if (targetExecution == null) {
          targetExecution = createMissingTargetFlowScopeExecution(flowScopeExecution, (PvmActivity) targetFlowScope);
          migratingExecutionBranch.registerExecution(targetFlowScope, targetExecution);
        }

        // 3. attach to newly created execution
        migratingActivityInstance.attachState(targetExecution);
      }
    }

    // 4. update state (e.g. activity id)
    migratingActivityInstance.migrateState();

    // 5. migrate instance state other than execution-tree structure
    migratingActivityInstance.migrateDependentEntities();

    // Let activity instances on the same level of subprocess share the same execution context
    // of newly created scope executions.
    // This ensures that newly created scope executions
    // * are reused to attach activity instances to when the activity instances share a
    //   common ancestor path to the process instance
    // * are not reused when activity instances are in unrelated branches of the execution tree
    migratingExecutionBranch = migratingExecutionBranch.copy();
    migratingExecutionBranch.visited(migratingActivityInstance);

    for (MigratingActivityInstance childInstance : migratingActivityInstance.getChildren()) {
      migrateActivityInstance(migratingProcessInstance, migratingExecutionBranch, childInstance);
    }

  }

  protected ExecutionEntity createMissingTargetFlowScopeExecution(ExecutionEntity parentScopeExecution, PvmActivity targetFlowScope) {
    ExecutionEntity newParentExecution = parentScopeExecution;
    if (!parentScopeExecution.getNonEventScopeExecutions().isEmpty() || parentScopeExecution.getActivity() != null) {
      newParentExecution = (ExecutionEntity) parentScopeExecution.createConcurrentExecution();
    }

    List<PvmActivity> scopesToInstantiate = new ArrayList<PvmActivity>();
    scopesToInstantiate.add(targetFlowScope);
    newParentExecution.createScopes(scopesToInstantiate);
    ExecutionEntity targetFlowScopeExecution = newParentExecution.getExecutions().get(0); // TODO: this does not work for more than one scope

    targetFlowScopeExecution.setActivity(null);

    return targetFlowScopeExecution;
  }

}
