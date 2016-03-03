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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.migration.instance.MigratingActivityInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingActivityInstanceWalker;
import org.camunda.bpm.engine.impl.migration.instance.MigratingExecutionBranch;
import org.camunda.bpm.engine.impl.migration.instance.MigratingProcessInstance;
import org.camunda.bpm.engine.impl.migration.instance.parser.MigratingInstanceParser;
import org.camunda.bpm.engine.impl.migration.validation.instance.MigratingActivityInstanceValidationReportImpl;
import org.camunda.bpm.engine.impl.migration.validation.instance.MigratingActivityInstanceValidator;
import org.camunda.bpm.engine.impl.migration.validation.instance.MigratingProcessInstanceValidationReportImpl;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;
import org.camunda.bpm.engine.impl.tree.FlowScopeWalker;
import org.camunda.bpm.engine.impl.tree.ReferenceWalker;
import org.camunda.bpm.engine.impl.tree.TreeVisitor;
import org.camunda.bpm.engine.impl.util.EnsureUtil;
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

  protected static final MigrationLogger LOGGER = ProcessEngineLogger.MIGRATION_LOGGER;

  protected MigrationPlanExecutionBuilderImpl migrationPlanExecutionBuilder;


  public MigrateProcessInstanceCmd(MigrationPlanExecutionBuilderImpl migrationPlanExecutionBuilder) {
    this.migrationPlanExecutionBuilder = migrationPlanExecutionBuilder;
  }

  public Void execute(CommandContext commandContext) {
    MigrationPlan migrationPlan = migrationPlanExecutionBuilder.getMigrationPlan();
    List<String> processInstanceIds = migrationPlanExecutionBuilder.getProcessInstanceIds();

    ensureNotNull(BadUserRequestException.class, "Migration plan cannot be null", "migration plan", migrationPlan);
    ensureNotNull(BadUserRequestException.class, "Process instance ids cannot be null", "process instance ids", processInstanceIds);

    ProcessDefinitionEntity targetProcessDefinition = commandContext.getProcessEngineConfiguration()
      .getDeploymentCache().findDeployedProcessDefinitionById(migrationPlan.getTargetProcessDefinitionId());

    for (String processInstanceId : processInstanceIds) {
      migrateProcessInstance(commandContext, processInstanceId, migrationPlan, targetProcessDefinition);
    }

    return null;
  }

  public Void migrateProcessInstance(CommandContext commandContext, String processInstanceId, MigrationPlan migrationPlan, ProcessDefinitionEntity targetProcessDefinition) {
    ensureNotNull(BadUserRequestException.class, "Process instance id cannot be null", "process instance id", processInstanceId);

    ExecutionEntity processInstance = commandContext.getExecutionManager().findExecutionById(processInstanceId);

    ensureProcessInstanceExist(processInstanceId, processInstance);
    ensureSameProcessDefinition(processInstance, migrationPlan.getSourceProcessDefinitionId());

    MigratingProcessInstanceValidationReportImpl processInstanceReport = new MigratingProcessInstanceValidationReportImpl();

    // Initialize migration: match migration instructions to activity instances and collect required entities
    MigratingInstanceParser migratingInstanceParser = new MigratingInstanceParser(Context.getProcessEngineConfiguration().getProcessEngine());
    MigratingProcessInstance migratingProcessInstance = migratingInstanceParser.parse(processInstance.getId(), migrationPlan, processInstanceReport);

    validateInstructions(commandContext, migratingProcessInstance, processInstanceReport);

    if (processInstanceReport.hasFailures()) {
      throw LOGGER.failingMigratingProcessInstanceValidation(processInstanceReport);
    }

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
          if (!currentInstance.migrates()) {
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
          else {
            currentInstance.removeUnmappedDependentInstances();
          }
        }
      });

      walker.walkUntil(new ReferenceWalker.WalkCondition<MigratingActivityInstance>() {

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

  protected void validateInstructions(CommandContext commandContext, MigratingProcessInstance migratingProcessInstance, MigratingProcessInstanceValidationReportImpl processInstanceReport) {
    List<MigratingActivityInstanceValidator> migratingActivityInstanceValidators = commandContext.getProcessEngineConfiguration().getMigratingActivityInstanceValidators();

    for (MigratingActivityInstance migratingActivityInstance : migratingProcessInstance.getMigratingActivityInstances()) {
      MigratingActivityInstanceValidationReportImpl instanceReport = validateActivityInstance(migratingActivityInstance, migratingProcessInstance, migratingActivityInstanceValidators);
      if (instanceReport.hasFailures()) {
        processInstanceReport.addInstanceReport(instanceReport);
      }
    }

  }

  protected MigratingActivityInstanceValidationReportImpl validateActivityInstance(MigratingActivityInstance migratingActivityInstance, MigratingProcessInstance migratingProcessInstance, List<MigratingActivityInstanceValidator> migratingActivityInstanceValidators) {
    MigratingActivityInstanceValidationReportImpl instanceReport = new MigratingActivityInstanceValidationReportImpl(migratingActivityInstance);
    for (MigratingActivityInstanceValidator migratingActivityInstanceValidator : migratingActivityInstanceValidators) {
      migratingActivityInstanceValidator.validate(migratingActivityInstance, migratingProcessInstance, instanceReport);
    }
    return instanceReport;
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

    migrateActivityInstance(scopeExecutionContext, rootActivityInstance);
  }

  protected void migrateActivityInstance(
    MigratingExecutionBranch migratingExecutionBranch,
    MigratingActivityInstance migratingActivityInstance) {

    ActivityInstance activityInstance = migratingActivityInstance.getActivityInstance();

    if (!activityInstance.getId().equals(activityInstance.getProcessInstanceId())) {
      final MigratingActivityInstance parentMigratingInstance = migratingActivityInstance.getParent();

      ScopeImpl targetScope = migratingActivityInstance.getTargetScope();
      ScopeImpl targetFlowScope = targetScope.getFlowScope();
      ScopeImpl parentActivityInstanceTargetScope = parentMigratingInstance.getTargetScope();

      if (targetFlowScope != parentActivityInstanceTargetScope) {
        // create intermediate scopes

        // 1. detach activity instance
        migratingActivityInstance.detachState();

        // 2. manipulate execution tree

        // determine the list of ancestor scopes (parent, grandparent, etc.) for which
        //     no executions exist yet
        List<ScopeImpl> nonExistingScopes = collectNonExistingFlowScopes(targetFlowScope, migratingExecutionBranch);

        // get the closest ancestor scope that is instantiated already
        ScopeImpl existingScope = nonExistingScopes.isEmpty() ?
            targetFlowScope :
            nonExistingScopes.get(0).getFlowScope();

        // and its scope execution
        ExecutionEntity ancestorScopeExecution = migratingExecutionBranch.getExecution(existingScope);

        // Instantiate the scopes as children of the scope execution
        instantiateScopes(ancestorScopeExecution, migratingExecutionBranch, nonExistingScopes);

        ExecutionEntity targetFlowScopeExecution = migratingExecutionBranch.getExecution(targetFlowScope);

        // 3. attach to newly created execution
        migratingActivityInstance.attachState(targetFlowScopeExecution);
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
      migrateActivityInstance(migratingExecutionBranch, childInstance);
    }

}

  /**
   * Returns a list of flow scopes from the given scope until a scope is reached that is already present in the given
   * {@link MigratingExecutionBranch} (exclusive). The order of the returned list is top-down, i.e. the highest scope
   * is the first element of the list.
   */
  protected List<ScopeImpl> collectNonExistingFlowScopes(ScopeImpl scope, final MigratingExecutionBranch migratingExecutionBranch) {
    FlowScopeWalker walker = new FlowScopeWalker(scope);
    final List<ScopeImpl> result = new LinkedList<ScopeImpl>();
    walker.addPreVisitor(new TreeVisitor<ScopeImpl>() {

      @Override
      public void visit(ScopeImpl obj) {
        result.add(0, obj);
      }
    });

    walker.walkWhile(new ReferenceWalker.WalkCondition<ScopeImpl>() {

      @Override
      public boolean isFulfilled(ScopeImpl element) {
        return migratingExecutionBranch.hasExecution(element);
      }
    });

    return result;
  }

  /**
   * Creates scope executions for the given list of scopes;
   * Registers these executions with the migrating execution branch;
   *
   * @param ancestorScopeExecution the execution for the scope that the scopes to instantiate
   *   are subordinates to
   * @param executionBranch the migrating execution branch that manages scopes and their executions
   * @param scopesToInstantiate a list of hierarchical scopes to instantiate, ordered top-down
   */
  protected void instantiateScopes(ExecutionEntity ancestorScopeExecution,
      MigratingExecutionBranch executionBranch, List<ScopeImpl> scopesToInstantiate) {

    if (scopesToInstantiate.isEmpty()) {
      return;
    }

    ExecutionEntity newParentExecution = ancestorScopeExecution;
    if (!ancestorScopeExecution.getNonEventScopeExecutions().isEmpty() || ancestorScopeExecution.getActivity() != null) {
      newParentExecution = (ExecutionEntity) ancestorScopeExecution.createConcurrentExecution();
    }

    Map<PvmActivity, PvmExecutionImpl> createdExecutions =
        newParentExecution.instantiateScopes((List) scopesToInstantiate);

    for (ScopeImpl scope : scopesToInstantiate) {
      ExecutionEntity createdExecution = (ExecutionEntity) createdExecutions.get(scope);
      createdExecution.setActivity(null);
      executionBranch.registerExecution(scope, createdExecution);

    }
  }

  protected void ensureProcessInstanceExist(String processInstanceId, ExecutionEntity processInstance) {
    if (processInstance == null) {
      throw LOGGER.processInstanceDoesNotExist(processInstanceId);
    }
  }

  protected void ensureSameProcessDefinition(ExecutionEntity processInstance, String processDefinitionId) {
    if (!processDefinitionId.equals(processInstance.getProcessDefinitionId())) {
      throw LOGGER.processDefinitionOfInstanceDoesNotMatchMigrationPlan(processInstance, processDefinitionId);
    }
  }

}
