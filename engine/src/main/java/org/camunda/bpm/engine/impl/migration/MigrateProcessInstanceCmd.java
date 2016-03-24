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
import org.camunda.bpm.engine.impl.migration.instance.MigratingActivityInstanceBranch;
import org.camunda.bpm.engine.impl.migration.instance.MigratingActivityInstanceWalker;
import org.camunda.bpm.engine.impl.migration.instance.MigratingProcessElementInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingProcessInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingTransitionInstance;
import org.camunda.bpm.engine.impl.migration.instance.parser.MigratingInstanceParser;
import org.camunda.bpm.engine.impl.migration.validation.instance.MigratingActivityInstanceValidationReportImpl;
import org.camunda.bpm.engine.impl.migration.validation.instance.MigratingActivityInstanceValidator;
import org.camunda.bpm.engine.impl.migration.validation.instance.MigratingProcessInstanceValidationReportImpl;
import org.camunda.bpm.engine.impl.migration.validation.instance.MigratingTransitionInstanceValidationReportImpl;
import org.camunda.bpm.engine.impl.migration.validation.instance.MigratingTransitionInstanceValidator;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;
import org.camunda.bpm.engine.impl.tree.FlowScopeWalker;
import org.camunda.bpm.engine.impl.tree.ReferenceWalker;
import org.camunda.bpm.engine.impl.tree.TreeVisitor;
import org.camunda.bpm.engine.migration.MigrationPlan;

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
    final Set<MigratingProcessElementInstance> visitedInstances = new HashSet<MigratingProcessElementInstance>();
    Set<MigratingActivityInstance> leafInstances = collectLeafActivityInstances(migratingProcessInstance);

    for (MigratingActivityInstance leafInstance : leafInstances) {
      MigratingActivityInstanceWalker walker = new MigratingActivityInstanceWalker(leafInstance);

      walker.addPreVisitor(new TreeVisitor<MigratingActivityInstance>() {

        @Override
        public void visit(MigratingActivityInstance currentInstance) {

          visitedInstances.add(currentInstance);
          if (!currentInstance.migrates()) {
            Set<MigratingProcessElementInstance> children = new HashSet<MigratingProcessElementInstance>(currentInstance.getChildren());
            MigratingActivityInstance parent = currentInstance.getParent();

            // 1. detach children
            currentInstance.detachChildren();

            // 2. manipulate execution tree (i.e. remove this instance)
            currentInstance.remove();

            // 3. reconnect parent and children
            for (MigratingProcessElementInstance child : children) {
              child.attachState(parent);
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
          return element == null || !visitedInstances.containsAll(element.getChildActivityInstances());
        }
      });
    }
  }

  protected Set<MigratingActivityInstance> collectLeafActivityInstances(MigratingProcessInstance migratingProcessInstance) {
    Set<MigratingActivityInstance> leafInstances = new HashSet<MigratingActivityInstance>();

    for (MigratingActivityInstance migratingActivityInstance : migratingProcessInstance.getMigratingActivityInstances()) {
      if (migratingActivityInstance.getChildActivityInstances().isEmpty()) {
        leafInstances.add(migratingActivityInstance);
      }
    }

    return leafInstances;
  }

  protected void validateInstructions(CommandContext commandContext, MigratingProcessInstance migratingProcessInstance, MigratingProcessInstanceValidationReportImpl processInstanceReport) {
    List<MigratingActivityInstanceValidator> migratingActivityInstanceValidators = commandContext.getProcessEngineConfiguration().getMigratingActivityInstanceValidators();
    List<MigratingTransitionInstanceValidator> migratingTransitionInstanceValidators = commandContext.getProcessEngineConfiguration().getMigratingTransitionInstanceValidators();

    for (MigratingActivityInstance migratingActivityInstance : migratingProcessInstance.getMigratingActivityInstances()) {
      MigratingActivityInstanceValidationReportImpl instanceReport = validateActivityInstance(migratingActivityInstance, migratingProcessInstance, migratingActivityInstanceValidators);
      if (instanceReport.hasFailures()) {
        processInstanceReport.addActivityInstanceReport(instanceReport);
      }
    }

    for (MigratingTransitionInstance migratingTransitionInstance : migratingProcessInstance.getMigratingTransitionInstances()) {
      MigratingTransitionInstanceValidationReportImpl instanceReport = validateTransitionInstance(migratingTransitionInstance, migratingProcessInstance, migratingTransitionInstanceValidators);
      if (instanceReport.hasFailures()) {
        processInstanceReport.addTransitionInstanceReport(instanceReport);
      }
    }

  }

  protected MigratingActivityInstanceValidationReportImpl validateActivityInstance(MigratingActivityInstance migratingActivityInstance,
      MigratingProcessInstance migratingProcessInstance,
      List<MigratingActivityInstanceValidator> migratingActivityInstanceValidators) {
    MigratingActivityInstanceValidationReportImpl instanceReport = new MigratingActivityInstanceValidationReportImpl(migratingActivityInstance);
    for (MigratingActivityInstanceValidator migratingActivityInstanceValidator : migratingActivityInstanceValidators) {
      migratingActivityInstanceValidator.validate(migratingActivityInstance, migratingProcessInstance, instanceReport);
    }
    return instanceReport;
  }

  protected MigratingTransitionInstanceValidationReportImpl validateTransitionInstance(MigratingTransitionInstance migratingTransitionInstance,
      MigratingProcessInstance migratingProcessInstance,
      List<MigratingTransitionInstanceValidator> migratingTransitionInstanceValidators) {
    MigratingTransitionInstanceValidationReportImpl instanceReport = new MigratingTransitionInstanceValidationReportImpl(migratingTransitionInstance);
    for (MigratingTransitionInstanceValidator migratingTransitionInstanceValidator : migratingTransitionInstanceValidators) {
      migratingTransitionInstanceValidator.validate(migratingTransitionInstance, migratingProcessInstance, instanceReport);
    }
    return instanceReport;
  }

  /**
   * Migrate activity instances to their new activities and process definition. Creates new
   * scope instances as necessary.
   */
  protected void migrateProcessInstance(MigratingProcessInstance migratingProcessInstance) {
    MigratingActivityInstance rootActivityInstance = migratingProcessInstance.getRootInstance();

    MigratingActivityInstanceBranch scopeExecutionContext = new MigratingActivityInstanceBranch();
    scopeExecutionContext.visited(rootActivityInstance);

    migrateActivityInstance(scopeExecutionContext, rootActivityInstance);
  }

  protected void migrateActivityInstance(
    MigratingActivityInstanceBranch migratingInstanceBranch,
    MigratingActivityInstance migratingActivityInstance) {

    migrateProcessElementInstance(migratingActivityInstance, migratingInstanceBranch);

    // Let activity instances on the same level of subprocess share the same execution context
    // of newly created scope executions.
    // This ensures that newly created scope executions
    // * are reused to attach activity instances to when the activity instances share a
    //   common ancestor path to the process instance
    // * are not reused when activity instances are in unrelated branches of the execution tree
    migratingInstanceBranch = migratingInstanceBranch.copy();
    migratingInstanceBranch.visited(migratingActivityInstance);

    Set<MigratingActivityInstance> childActivityInstances = new HashSet<MigratingActivityInstance>(migratingActivityInstance.getChildActivityInstances());
    Set<MigratingTransitionInstance> childTransitionInstances = new HashSet<MigratingTransitionInstance>(migratingActivityInstance.getChildTransitionInstances());

    for (MigratingTransitionInstance childInstance : childTransitionInstances) {
      migrateTransitionInstance(migratingInstanceBranch, childInstance);
    }

    for (MigratingActivityInstance childInstance : childActivityInstances) {
      migrateActivityInstance(migratingInstanceBranch, childInstance);
    }

  }

  protected void migrateTransitionInstance(
      MigratingActivityInstanceBranch migratingInstanceBranch,
      MigratingTransitionInstance migratingTransitionInstance) {

    migrateProcessElementInstance(migratingTransitionInstance, migratingInstanceBranch);
  }

  protected void migrateProcessElementInstance(MigratingProcessElementInstance migratingInstance, MigratingActivityInstanceBranch migratingInstanceBranch) {
    final MigratingActivityInstance parentMigratingInstance = migratingInstance.getParent();

    ScopeImpl sourceScope = migratingInstance.getSourceScope();
    ScopeImpl targetScope = migratingInstance.getTargetScope();
    ScopeImpl targetFlowScope = targetScope.getFlowScope();
    ScopeImpl parentActivityInstanceTargetScope = parentMigratingInstance != null ? parentMigratingInstance.getTargetScope() : null;

    if (sourceScope != sourceScope.getProcessDefinition() && targetFlowScope != parentActivityInstanceTargetScope) {
      // create intermediate scopes

      // 1. manipulate execution tree

      // determine the list of ancestor scopes (parent, grandparent, etc.) for which
      //     no executions exist yet
      List<ScopeImpl> nonExistingScopes = collectNonExistingFlowScopes(targetFlowScope, migratingInstanceBranch);

      // get the closest ancestor scope that is instantiated already
      ScopeImpl existingScope = nonExistingScopes.isEmpty() ?
          targetFlowScope :
          nonExistingScopes.get(0).getFlowScope();

      // and its scope instance
      MigratingActivityInstance ancestorScopeInstance = migratingInstanceBranch.getInstance(existingScope);

      // Instantiate the scopes as children of the scope execution
      instantiateScopes(ancestorScopeInstance, migratingInstanceBranch, nonExistingScopes);

      MigratingActivityInstance targetFlowScopeInstance = migratingInstanceBranch.getInstance(targetFlowScope);

      // 2. detach instance
      // The order of steps 1 and 2 avoids intermediate execution tree compaction
      // which in turn could overwrite some dependent instances (e.g. variables)
      migratingInstance.detachState();

      // 3. attach to newly created activity instance
      migratingInstance.attachState(targetFlowScopeInstance);
    }

    // 4. update state (e.g. activity id)
    migratingInstance.migrateState();

    // 5. migrate instance state other than execution-tree structure
    migratingInstance.migrateDependentEntities();
  }

  /**
   * Returns a list of flow scopes from the given scope until a scope is reached that is already present in the given
   * {@link MigratingActivityInstanceBranch} (exclusive). The order of the returned list is top-down, i.e. the highest scope
   * is the first element of the list.
   */
  protected List<ScopeImpl> collectNonExistingFlowScopes(ScopeImpl scope, final MigratingActivityInstanceBranch migratingExecutionBranch) {
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
        return migratingExecutionBranch.hasInstance(element);
      }
    });

    return result;
  }

  /**
   * Creates scope executions for the given list of scopes;
   * Registers these executions with the migrating execution branch;
   *
   * @param ancestorScopeInstance the instance for the scope that the scopes to instantiate
   *   are subordinates to
   * @param executionBranch the migrating execution branch that manages scopes and their executions
   * @param scopesToInstantiate a list of hierarchical scopes to instantiate, ordered top-down
   */
  protected void instantiateScopes(MigratingActivityInstance ancestorScopeInstance,
      MigratingActivityInstanceBranch executionBranch, List<ScopeImpl> scopesToInstantiate) {

    if (scopesToInstantiate.isEmpty()) {
      return;
    }

    ExecutionEntity newParentExecution = ancestorScopeInstance.createAttachableExecution();

    Map<PvmActivity, PvmExecutionImpl> createdExecutions =
        newParentExecution.instantiateScopes((List) scopesToInstantiate);

    for (ScopeImpl scope : scopesToInstantiate) {
      ExecutionEntity createdExecution = (ExecutionEntity) createdExecutions.get(scope);
      createdExecution.setActivity(null);
      executionBranch.visited(new MigratingActivityInstance(scope, createdExecution));
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
