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
package org.camunda.bpm.engine.impl.migration.batch;


import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotContainsNull;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.ArrayList;
import java.util.Collection;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.authorization.BatchPermissions;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.batch.BatchEntity;
import org.camunda.bpm.engine.impl.batch.BatchJobHandler;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.migration.AbstractMigrationCmd;
import org.camunda.bpm.engine.impl.migration.MigrationLogger;
import org.camunda.bpm.engine.impl.migration.MigrationPlanExecutionBuilderImpl;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.util.BatchUtil;
import org.camunda.bpm.engine.migration.MigrationPlan;

public class MigrateProcessInstanceBatchCmd extends AbstractMigrationCmd<Batch> {

  protected static final MigrationLogger LOGGER = ProcessEngineLogger.MIGRATION_LOGGER;

  public MigrateProcessInstanceBatchCmd(MigrationPlanExecutionBuilderImpl migrationPlanExecutionBuilder) {
    super(migrationPlanExecutionBuilder);
  }

  @Override
  public Batch execute(CommandContext commandContext) {

    MigrationPlan migrationPlan = executionBuilder.getMigrationPlan();
    Collection<String> processInstanceIds = collectProcessInstanceIds(commandContext);

    ensureNotNull(BadUserRequestException.class, "Migration plan cannot be null", "migration plan", migrationPlan);
    ensureNotEmpty(BadUserRequestException.class, "Process instance ids cannot empty", "process instance ids", processInstanceIds);
    ensureNotContainsNull(BadUserRequestException.class, "Process instance ids cannot be null", "process instance ids", processInstanceIds);

    ProcessDefinitionEntity sourceProcessDefinition = resolveSourceProcessDefinition(commandContext);
    ProcessDefinitionEntity targetProcessDefinition = resolveTargetProcessDefinition(commandContext);

    checkAuthorizations(commandContext,
        sourceProcessDefinition,
        targetProcessDefinition,
        processInstanceIds);
    writeUserOperationLog(commandContext,
        sourceProcessDefinition,
        targetProcessDefinition,
        processInstanceIds.size(),
        true);

    BatchEntity batch = createBatch(commandContext, migrationPlan, processInstanceIds, sourceProcessDefinition);

    batch.createSeedJobDefinition();
    batch.createMonitorJobDefinition();
    batch.createBatchJobDefinition();

    batch.fireHistoricStartEvent();

    batch.createSeedJob();

    return batch;
  }

  @Override
  protected void checkAuthorizations(CommandContext commandContext, ProcessDefinitionEntity sourceDefinition, ProcessDefinitionEntity targetDefinition,
                                     Collection<String> processInstanceIds) {

    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkCreateBatch(BatchPermissions.CREATE_BATCH_MIGRATE_PROCESS_INSTANCES);
    }

    super.checkAuthorizations(commandContext, sourceDefinition, targetDefinition, processInstanceIds);
  }

  protected BatchEntity createBatch(CommandContext commandContext,
                                    MigrationPlan migrationPlan,
                                    Collection<String> processInstanceIds,
                                    ProcessDefinitionEntity sourceProcessDefinition) {
    ProcessEngineConfigurationImpl processEngineConfiguration = commandContext.getProcessEngineConfiguration();
    BatchJobHandler<MigrationBatchConfiguration> batchJobHandler = getBatchJobHandler(processEngineConfiguration);

    MigrationBatchConfiguration configuration = new MigrationBatchConfiguration(
        new ArrayList<String>(processInstanceIds),
        migrationPlan,
        executionBuilder.isSkipCustomListeners(),
        executionBuilder.isSkipIoMappings());

    BatchEntity batch = new BatchEntity();
    batch.setType(batchJobHandler.getType());
    batch.setTotalJobs(BatchUtil.calculateBatchSize(processEngineConfiguration, configuration));
    batch.setBatchJobsPerSeed(processEngineConfiguration.getBatchJobsPerSeed());
    batch.setInvocationsPerBatchJob(processEngineConfiguration.getInvocationsPerBatchJob());
    batch.setConfigurationBytes(batchJobHandler.writeConfiguration(configuration));
    batch.setTenantId(sourceProcessDefinition.getTenantId());
    commandContext.getBatchManager().insertBatch(batch);

    return batch;
  }

  @SuppressWarnings("unchecked")
  protected BatchJobHandler<MigrationBatchConfiguration> getBatchJobHandler(ProcessEngineConfigurationImpl processEngineConfiguration) {
    return (BatchJobHandler<MigrationBatchConfiguration>) processEngineConfiguration.getBatchHandlers().get(Batch.TYPE_PROCESS_INSTANCE_MIGRATION);
  }

}
