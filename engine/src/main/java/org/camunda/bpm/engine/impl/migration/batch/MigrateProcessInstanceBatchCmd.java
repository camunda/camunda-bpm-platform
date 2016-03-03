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
package org.camunda.bpm.engine.impl.migration.batch;


import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.List;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.batch.BatchEntity;
import org.camunda.bpm.engine.impl.batch.BatchJobHandler;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.migration.MigrationLogger;
import org.camunda.bpm.engine.impl.migration.MigrationPlanExecutionBuilderImpl;
import org.camunda.bpm.engine.migration.MigrationPlan;

public class MigrateProcessInstanceBatchCmd implements Command<Batch> {

  protected static final MigrationLogger LOGGER = ProcessEngineLogger.MIGRATION_LOGGER;

  protected MigrationPlanExecutionBuilderImpl migrationPlanExecutionBuilder;

  public MigrateProcessInstanceBatchCmd(MigrationPlanExecutionBuilderImpl migrationPlanExecutionBuilder) {
    this.migrationPlanExecutionBuilder = migrationPlanExecutionBuilder;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Batch execute(CommandContext commandContext) {
    BatchEntity batch = createBatch(commandContext);

    batch.createSeedJobDefinition();
    batch.createMonitorJobDefinition();
    batch.createBatchJobDefinition();

    batch.fireHistoricStartEvent();

    batch.createSeedJob();

    return batch;
  }

  protected BatchEntity createBatch(CommandContext commandContext) {
    ProcessEngineConfigurationImpl processEngineConfiguration = commandContext.getProcessEngineConfiguration();
    BatchJobHandler<MigrationBatchConfiguration> batchJobHandler = getBatchJobHandler(processEngineConfiguration);

    MigrationBatchConfiguration configuration = createConfiguration();

    BatchEntity batch = new BatchEntity();
    batch.setType(batchJobHandler.getType());
    batch.setSize(configuration.getProcessInstanceIds().size());
    batch.setBatchJobsPerSeed(processEngineConfiguration.getBatchJobsPerSeed());
    batch.setInvocationsPerBatchJob(processEngineConfiguration.getInvocationsPerBatchJob());
    batch.setConfigurationBytes(batchJobHandler.writeConfiguration(configuration));
    commandContext.getBatchManager().insert(batch);

    return batch;
  }

  protected MigrationBatchConfiguration createConfiguration() {
    MigrationPlan migrationPlan = migrationPlanExecutionBuilder.getMigrationPlan();
    List<String> processInstanceIds = migrationPlanExecutionBuilder.getProcessInstanceIds();

    ensureNotNull(BadUserRequestException.class, "Migration plan cannot be null", "migration plan", migrationPlan);
    ensureNotEmpty(BadUserRequestException.class, "Process instance ids cannot be null or empty", "process instance ids", processInstanceIds);

    MigrationBatchConfiguration configuration = new MigrationBatchConfiguration();
    configuration.setMigrationPlan(migrationPlan);
    configuration.setProcessInstanceIds(processInstanceIds);
    return configuration;
  }

  @SuppressWarnings("unchecked")
  protected BatchJobHandler<MigrationBatchConfiguration> getBatchJobHandler(ProcessEngineConfigurationImpl processEngineConfiguration) {
    return (BatchJobHandler<MigrationBatchConfiguration>) processEngineConfiguration.getBatchHandlers().get(MigrationBatchJobHandler.TYPE);
  }

}
