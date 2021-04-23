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
package org.camunda.bpm.engine.impl.migration;


import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotContainsNull;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.cmd.SetExecutionVariablesCmd;
import org.camunda.bpm.engine.impl.context.ProcessApplicationContextUtil;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.migration.instance.DeleteUnmappedInstanceVisitor;
import org.camunda.bpm.engine.impl.migration.instance.MigratingActivityInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingActivityInstanceVisitor;
import org.camunda.bpm.engine.impl.migration.instance.MigratingCompensationEventSubscriptionInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingEventScopeInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingProcessElementInstanceTopDownWalker;
import org.camunda.bpm.engine.impl.migration.instance.MigratingProcessInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingScopeInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingScopeInstanceBottomUpWalker;
import org.camunda.bpm.engine.impl.migration.instance.MigratingTransitionInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigrationCompensationInstanceVisitor;
import org.camunda.bpm.engine.impl.migration.instance.parser.MigratingInstanceParser;
import org.camunda.bpm.engine.impl.migration.validation.instance.MigratingActivityInstanceValidationReportImpl;
import org.camunda.bpm.engine.impl.migration.validation.instance.MigratingActivityInstanceValidator;
import org.camunda.bpm.engine.impl.migration.validation.instance.MigratingCompensationInstanceValidator;
import org.camunda.bpm.engine.impl.migration.validation.instance.MigratingProcessInstanceValidationReportImpl;
import org.camunda.bpm.engine.impl.migration.validation.instance.MigratingTransitionInstanceValidationReportImpl;
import org.camunda.bpm.engine.impl.migration.validation.instance.MigratingTransitionInstanceValidator;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
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
 * </ol>
 * @author Thorben Lindhauer
 */
public class MigrateProcessInstanceCmd extends AbstractMigrationCmd implements Command<Void> {

  protected static final MigrationLogger LOGGER = ProcessEngineLogger.MIGRATION_LOGGER;

  protected boolean skipJavaSerializationFormatCheck;

  public MigrateProcessInstanceCmd(MigrationPlanExecutionBuilderImpl migrationPlanExecutionBuilder,
                                   boolean skipJavaSerializationFormatCheck) {
    super(migrationPlanExecutionBuilder);
    this.skipJavaSerializationFormatCheck = skipJavaSerializationFormatCheck;
  }

  @Override
  public Void execute(final CommandContext commandContext) {
    final MigrationPlan migrationPlan = executionBuilder.getMigrationPlan();
    final Collection<String> processInstanceIds = collectProcessInstanceIds();

    ensureNotNull(BadUserRequestException.class,
        "Migration plan cannot be null", "migration plan", migrationPlan);
    ensureNotEmpty(BadUserRequestException.class,
        "Process instance ids cannot empty", "process instance ids", processInstanceIds);
    ensureNotContainsNull(BadUserRequestException.class,
        "Process instance ids cannot be null", "process instance ids", processInstanceIds);

    ProcessDefinitionEntity sourceDefinition = resolveSourceProcessDefinition(commandContext);
    final ProcessDefinitionEntity targetDefinition = resolveTargetProcessDefinition(commandContext);

    checkAuthorizations(commandContext, sourceDefinition, targetDefinition);

    writeUserOperationLog(commandContext, sourceDefinition, targetDefinition,
        processInstanceIds.size(), migrationPlan.getVariables(), false);

    commandContext.runWithoutAuthorization((Callable<Void>) () -> {
      for (String processInstanceId : processInstanceIds) {
        migrateProcessInstance(commandContext, processInstanceId, migrationPlan,
            targetDefinition, skipJavaSerializationFormatCheck);
      }
      return null;
    });

    return null;
  }

  public Void migrateProcessInstance(CommandContext commandContext,
                                     String processInstanceId,
                                     MigrationPlan migrationPlan,
                                     ProcessDefinitionEntity targetProcessDefinition,
                                     boolean skipJavaSerializationFormatCheck) {
    ensureNotNull(BadUserRequestException.class,
        "Process instance id cannot be null", "process instance id", processInstanceId);

    final ExecutionEntity processInstance = commandContext.getExecutionManager()
        .findExecutionById(processInstanceId);

    ensureProcessInstanceExist(processInstanceId, processInstance);
    ensureOperationAllowed(commandContext, processInstance, targetProcessDefinition);
    ensureSameProcessDefinition(processInstance, migrationPlan.getSourceProcessDefinitionId());

    MigratingProcessInstanceValidationReportImpl processInstanceReport =
        new MigratingProcessInstanceValidationReportImpl();

    // Initialize migration: match migration instructions to activity instances and collect required entities
    ProcessEngineImpl processEngine = commandContext.getProcessEngineConfiguration()
        .getProcessEngine();

    MigratingInstanceParser migratingInstanceParser = new MigratingInstanceParser(processEngine);

    final MigratingProcessInstance migratingProcessInstance =
        migratingInstanceParser.parse(processInstanceId, migrationPlan, processInstanceReport);

    validateInstructions(commandContext, migratingProcessInstance, processInstanceReport);

    if (processInstanceReport.hasFailures()) {
      throw LOGGER.failingMigratingProcessInstanceValidation(processInstanceReport);
    }

    executeInContext(() -> deleteUnmappedActivityInstances(migratingProcessInstance),
      migratingProcessInstance.getSourceDefinition());

    executeInContext(() -> migrateProcessInstance(migratingProcessInstance),
      migratingProcessInstance.getTargetDefinition());

    Map<String, ?> variables = migrationPlan.getVariables();
    if (variables != null) {
      // we don't need a context switch here since when setting an execution triggering variable,
      // a context switch is performed at a later point via command invocation context
      commandContext.executeWithOperationLogPrevented(
        new SetExecutionVariablesCmd(processInstanceId, variables,
            false, skipJavaSerializationFormatCheck));
    }

    return null;
  }

  protected <T> void executeInContext(final Runnable runnable,
                                      ProcessDefinitionEntity contextDefinition) {
    ProcessApplicationContextUtil.doContextSwitch(runnable, contextDefinition);
  }

  /**
   * delete unmapped instances in a bottom-up fashion (similar to deleteCascade and regular BPMN execution)
   */
  protected void deleteUnmappedActivityInstances(MigratingProcessInstance migratingProcessInstance) {
    boolean isSkipCustomListeners = executionBuilder.isSkipCustomListeners();
    boolean isSkipIoMappings = executionBuilder.isSkipIoMappings();

    final DeleteUnmappedInstanceVisitor visitor =
        new DeleteUnmappedInstanceVisitor(isSkipCustomListeners, isSkipIoMappings);

    Set<MigratingScopeInstance> leafInstances = collectLeafInstances(migratingProcessInstance);
    for (MigratingScopeInstance leafInstance : leafInstances) {
      MigratingScopeInstanceBottomUpWalker walker =
          new MigratingScopeInstanceBottomUpWalker(leafInstance);

      walker.addPreVisitor(visitor);

      walker.walkUntil(element -> {
        // walk until top of instance tree is reached or until
        // a node is reached for which we have not yet visited every child
        return element == null || !visitor.hasVisitedAll(element.getChildScopeInstances());
      });
    }
  }

  protected Set<MigratingScopeInstance> collectLeafInstances(MigratingProcessInstance migratingProcessInstance) {
    Set<MigratingScopeInstance> leafInstances = new HashSet<>();

    Collection<MigratingScopeInstance> migratingScopeInstances =
        migratingProcessInstance.getMigratingScopeInstances();

    for (MigratingScopeInstance migratingScopeInstance : migratingScopeInstances) {
      if (migratingScopeInstance.getChildScopeInstances().isEmpty()) {
        leafInstances.add(migratingScopeInstance);
      }
    }

    return leafInstances;
  }

  protected void validateInstructions(CommandContext commandContext,
                                      MigratingProcessInstance migratingProcessInstance,
                                      MigratingProcessInstanceValidationReportImpl processInstanceReport) {
    List<MigratingActivityInstanceValidator> migratingActivityInstanceValidators
      = commandContext.getProcessEngineConfiguration().getMigratingActivityInstanceValidators();
    List<MigratingTransitionInstanceValidator> migratingTransitionInstanceValidators
      = commandContext.getProcessEngineConfiguration().getMigratingTransitionInstanceValidators();
    List<MigratingCompensationInstanceValidator> migratingCompensationInstanceValidators =
        commandContext.getProcessEngineConfiguration().getMigratingCompensationInstanceValidators();

    Map<MigratingActivityInstance, MigratingActivityInstanceValidationReportImpl> instanceReports =
        new HashMap<>();

    Collection<MigratingActivityInstance> migratingActivityInstances =
        migratingProcessInstance.getMigratingActivityInstances();

    for (MigratingActivityInstance migratingActivityInstance : migratingActivityInstances) {
      MigratingActivityInstanceValidationReportImpl instanceReport =
          validateActivityInstance(migratingActivityInstance,
              migratingProcessInstance, migratingActivityInstanceValidators);
      instanceReports.put(migratingActivityInstance, instanceReport);
    }

    Collection<MigratingEventScopeInstance> migratingEventScopeInstances =
        migratingProcessInstance.getMigratingEventScopeInstances();
    for (MigratingEventScopeInstance migratingEventScopeInstance : migratingEventScopeInstances) {
      MigratingActivityInstance ancestorInstance =
          migratingEventScopeInstance.getClosestAncestorActivityInstance();

      validateEventScopeInstance(
          migratingEventScopeInstance,
          migratingProcessInstance,
          migratingCompensationInstanceValidators,
          instanceReports.get(ancestorInstance));
    }

    for (MigratingCompensationEventSubscriptionInstance migratingEventSubscriptionInstance
        : migratingProcessInstance.getMigratingCompensationSubscriptionInstances()) {
      MigratingActivityInstance ancestorInstance =
          migratingEventSubscriptionInstance.getClosestAncestorActivityInstance();

      validateCompensateSubscriptionInstance(
          migratingEventSubscriptionInstance,
          migratingProcessInstance,
          migratingCompensationInstanceValidators,
          instanceReports.get(ancestorInstance));
    }

    for (MigratingActivityInstanceValidationReportImpl instanceReport : instanceReports.values()) {
      if (instanceReport.hasFailures()) {
        processInstanceReport.addActivityInstanceReport(instanceReport);
      }
    }

    Collection<MigratingTransitionInstance> migratingTransitionInstances =
        migratingProcessInstance.getMigratingTransitionInstances();
    for (MigratingTransitionInstance migratingTransitionInstance : migratingTransitionInstances) {
      MigratingTransitionInstanceValidationReportImpl instanceReport =
          validateTransitionInstance(migratingTransitionInstance,
              migratingProcessInstance, migratingTransitionInstanceValidators);
      if (instanceReport.hasFailures()) {
        processInstanceReport.addTransitionInstanceReport(instanceReport);
      }
    }


  }

  protected MigratingActivityInstanceValidationReportImpl validateActivityInstance(
      MigratingActivityInstance migratingActivityInstance,
      MigratingProcessInstance migratingProcessInstance,
      List<MigratingActivityInstanceValidator> migratingActivityInstanceValidators) {
    MigratingActivityInstanceValidationReportImpl instanceReport =
        new MigratingActivityInstanceValidationReportImpl(migratingActivityInstance);
    for (MigratingActivityInstanceValidator migratingActivityInstanceValidator :
        migratingActivityInstanceValidators) {
      migratingActivityInstanceValidator.validate(migratingActivityInstance,
          migratingProcessInstance, instanceReport);
    }
    return instanceReport;
  }

  protected MigratingTransitionInstanceValidationReportImpl validateTransitionInstance(
      MigratingTransitionInstance migratingTransitionInstance,
      MigratingProcessInstance migratingProcessInstance,
      List<MigratingTransitionInstanceValidator> migratingTransitionInstanceValidators) {
    MigratingTransitionInstanceValidationReportImpl instanceReport =
        new MigratingTransitionInstanceValidationReportImpl(migratingTransitionInstance);
    for (MigratingTransitionInstanceValidator migratingTransitionInstanceValidator :
        migratingTransitionInstanceValidators) {
      migratingTransitionInstanceValidator.validate(migratingTransitionInstance,
          migratingProcessInstance, instanceReport);
    }
    return instanceReport;
  }

  protected void validateEventScopeInstance(MigratingEventScopeInstance eventScopeInstance,
      MigratingProcessInstance migratingProcessInstance,
      List<MigratingCompensationInstanceValidator> migratingTransitionInstanceValidators,
      MigratingActivityInstanceValidationReportImpl instanceReport
    ) {
    for (MigratingCompensationInstanceValidator validator : migratingTransitionInstanceValidators) {
      validator.validate(eventScopeInstance, migratingProcessInstance, instanceReport);
    }
  }

  protected void validateCompensateSubscriptionInstance(
      MigratingCompensationEventSubscriptionInstance eventSubscriptionInstance,
      MigratingProcessInstance migratingProcessInstance,
      List<MigratingCompensationInstanceValidator> migratingTransitionInstanceValidators,
      MigratingActivityInstanceValidationReportImpl instanceReport
    ) {
    for (MigratingCompensationInstanceValidator validator : migratingTransitionInstanceValidators) {
      validator.validate(eventSubscriptionInstance, migratingProcessInstance, instanceReport);
    }
  }

  /**
   * Migrate activity instances to their new activities and process definition. Creates new
   * scope instances as necessary.
   */
  protected void migrateProcessInstance(MigratingProcessInstance migratingProcessInstance) {
    MigratingActivityInstance rootActivityInstance = migratingProcessInstance.getRootInstance();

    MigratingProcessElementInstanceTopDownWalker walker =
        new MigratingProcessElementInstanceTopDownWalker(rootActivityInstance);

    walker.addPreVisitor(
        new MigratingActivityInstanceVisitor(
            executionBuilder.isSkipCustomListeners(),
            executionBuilder.isSkipIoMappings()));
    walker.addPreVisitor(new MigrationCompensationInstanceVisitor());

    walker.walkUntil();
  }

  protected void ensureProcessInstanceExist(String processInstanceId,
                                            ExecutionEntity processInstance) {
    if (processInstance == null) {
      throw LOGGER.processInstanceDoesNotExist(processInstanceId);
    }
  }

  protected void ensureSameProcessDefinition(ExecutionEntity processInstance,
                                             String processDefinitionId) {
    if (!processDefinitionId.equals(processInstance.getProcessDefinitionId())) {
      throw LOGGER.processDefinitionOfInstanceDoesNotMatchMigrationPlan(processInstance,
          processDefinitionId);
    }
  }

  protected void ensureOperationAllowed(CommandContext commandContext,
                                        ExecutionEntity processInstance,
                                        ProcessDefinitionEntity targetProcessDefinition) {
    List<CommandChecker> commandCheckers = commandContext.getProcessEngineConfiguration()
        .getCommandCheckers();
    for(CommandChecker checker : commandCheckers) {
      checker.checkMigrateProcessInstance(processInstance, targetProcessDefinition);
    }
  }

}
