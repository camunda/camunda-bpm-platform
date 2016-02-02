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
import java.util.List;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.migration.instance.MigratingActivityInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingExecutionBranch;
import org.camunda.bpm.engine.impl.migration.instance.MigratingProcessInstance;
import org.camunda.bpm.engine.impl.migration.validation.AdditionalFlowScopeValidator;
import org.camunda.bpm.engine.impl.migration.validation.MigrationInstructionInstanceValidationReport;
import org.camunda.bpm.engine.impl.migration.validation.MigrationInstructionInstanceValidator;
import org.camunda.bpm.engine.impl.migration.validation.RemoveFlowScopeValidator;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
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

    ExecutionEntity processInstance = commandContext.getExecutionManager().findExecutionById(processInstanceId);

    ProcessDefinitionEntity targetProcessDefinition = Context.getProcessEngineConfiguration()
        .getDeploymentCache().findDeployedProcessDefinitionById(migrationPlan.getTargetProcessDefinitionId());

    // Initialize migration: match migration instructions to activity instances and collect required entities
    MigratingProcessInstance migratingProcessInstance = MigratingProcessInstance.initializeFrom(
        commandContext, migrationPlan, processInstance, targetProcessDefinition);

    validateInstructions(migratingProcessInstance);

    migrateProcessInstance(migratingProcessInstance);

    return null;
  }

  protected void validateInstructions(MigratingProcessInstance migratingProcessInstance) {

    List<MigrationInstructionInstanceValidator> validators = Arrays.asList(new AdditionalFlowScopeValidator(),
        new RemoveFlowScopeValidator());
    MigrationInstructionInstanceValidationReport validationReport = new MigrationInstructionInstanceValidationReport(migratingProcessInstance);

    for (MigratingActivityInstance migratingActivityInstance : migratingProcessInstance.getMigratingActivityInstances()) {
      for (MigrationInstructionInstanceValidator validator : validators) {
        validator.validate(migratingProcessInstance, migratingActivityInstance, validationReport);
      }
    }

    if (validationReport.hasFailures()) {
      throw LOGGER.failingInstructionInstanceValidation(validationReport);
    }

  }

  protected void migrateProcessInstance(MigratingProcessInstance migratingProcessInstance) {
    MigratingActivityInstance rootActivityInstance =
        migratingProcessInstance.getMigratingInstance(migratingProcessInstance.getProcessInstanceId());

    migrateActivityInstance(migratingProcessInstance, new MigratingExecutionBranch(), rootActivityInstance);
  }

  protected void migrateActivityInstance(
      MigratingProcessInstance migratingProcessInstance,
      MigratingExecutionBranch migratingExecutionBranch,
      MigratingActivityInstance migratingActivityInstance) {

    ActivityInstance activityInstance = migratingActivityInstance.getActivityInstance();

    if (!activityInstance.getId().equals(activityInstance.getProcessInstanceId())) {
      final MigratingActivityInstance parentMigratingInstance = migratingProcessInstance.getMigratingInstance(
          activityInstance.getParentActivityInstanceId());

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
        else {
          targetExecution = (ExecutionEntity) targetExecution.createConcurrentExecution();
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

    for (ActivityInstance childInstance : activityInstance.getChildActivityInstances()) {
      MigratingActivityInstance migratingChildInstance = migratingProcessInstance.getMigratingInstance(childInstance.getId());
      migrateActivityInstance(migratingProcessInstance, migratingExecutionBranch, migratingChildInstance);
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
