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

import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.batch.AbstractBatchJobHandler;
import org.camunda.bpm.engine.impl.batch.BatchJobConfiguration;
import org.camunda.bpm.engine.impl.batch.BatchJobContext;
import org.camunda.bpm.engine.impl.batch.BatchJobDeclaration;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobDeclaration;
import org.camunda.bpm.engine.impl.json.MigrationBatchConfigurationJsonConverter;
import org.camunda.bpm.engine.impl.migration.MigrationPlanExecutionBuilderImpl;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.migration.MigrationPlanExecutionBuilder;

import java.util.List;

/**
 * Job handler for batch migration jobs. The batch migration job
 * migrates a list of process instances.
 */
public class MigrationBatchJobHandler extends AbstractBatchJobHandler<MigrationBatchConfiguration> {

  public static final BatchJobDeclaration JOB_DECLARATION = new BatchJobDeclaration(Batch.TYPE_PROCESS_INSTANCE_MIGRATION);

  public String getType() {
    return Batch.TYPE_PROCESS_INSTANCE_MIGRATION;
  }

  public JobDeclaration<BatchJobContext, MessageEntity> getJobDeclaration() {
    return JOB_DECLARATION;
  }

  protected MigrationBatchConfigurationJsonConverter getJsonConverterInstance() {
    return MigrationBatchConfigurationJsonConverter.INSTANCE;
  }

  @Override
  protected MigrationBatchConfiguration createJobConfiguration(MigrationBatchConfiguration configuration, List<String> processIdsForJob) {
    return new MigrationBatchConfiguration(
        processIdsForJob,
        configuration.getMigrationPlan(),
        configuration.isSkipCustomListeners(),
        configuration.isSkipIoMappings()
    );
  }

  @Override
  protected void postProcessJob(MigrationBatchConfiguration configuration, JobEntity job) {
    CommandContext commandContext = Context.getCommandContext();
    String sourceProcessDefinitionId = configuration.getMigrationPlan().getSourceProcessDefinitionId();

    ProcessDefinitionEntity processDefinition = getProcessDefinition(commandContext, sourceProcessDefinitionId);
    job.setDeploymentId(processDefinition.getDeploymentId());
  }

  @Override
  public void execute(BatchJobConfiguration configuration, ExecutionEntity execution, CommandContext commandContext, String tenantId) {
    ByteArrayEntity configurationEntity = commandContext
        .getDbEntityManager()
        .selectById(ByteArrayEntity.class, configuration.getConfigurationByteArrayId());

    MigrationBatchConfiguration batchConfiguration = readConfiguration(configurationEntity.getBytes());

    MigrationPlanExecutionBuilder executionBuilder = commandContext.getProcessEngineConfiguration()
        .getRuntimeService()
        .newMigration(batchConfiguration.getMigrationPlan())
        .processInstanceIds(batchConfiguration.getIds());

    if (batchConfiguration.isSkipCustomListeners()) {
      executionBuilder.skipCustomListeners();
    }
    if (batchConfiguration.isSkipIoMappings()) {
      executionBuilder.skipIoMappings();
    }

    // uses internal API in order to skip writing user operation log (CommandContext#disableUserOperationLog
    // is not sufficient with legacy engine config setting "restrictUserOperationLogToAuthenticatedUsers" = false)
    ((MigrationPlanExecutionBuilderImpl) executionBuilder).execute(false);

    commandContext.getByteArrayManager().delete(configurationEntity);
  }

  protected ProcessDefinitionEntity getProcessDefinition(CommandContext commandContext, String processDefinitionId) {
    return commandContext.getProcessEngineConfiguration()
        .getDeploymentCache()
        .findDeployedProcessDefinitionById(processDefinitionId);
  }

}
