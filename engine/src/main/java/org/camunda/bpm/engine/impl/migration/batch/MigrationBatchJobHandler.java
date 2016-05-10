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

import java.io.ByteArrayOutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.batch.BatchEntity;
import org.camunda.bpm.engine.impl.batch.BatchJobConfiguration;
import org.camunda.bpm.engine.impl.batch.BatchJobHandler;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.instance.CoreExecution;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobDeclaration;
import org.camunda.bpm.engine.impl.json.MigrationBatchConfigurationJsonConverter;
import org.camunda.bpm.engine.impl.migration.MigrationPlanExecutionBuilderImpl;
import org.camunda.bpm.engine.impl.migration.batch.MigrationBatchJobDeclaration.BatchJobContext;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayManager;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobManager;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.impl.util.StringUtil;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.bpm.engine.impl.util.json.JSONTokener;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.migration.MigrationPlanExecutionBuilder;

/**
 * Job handler for batch migration jobs. The batch migration job
 * migrates a list of process instances.
 */
public class MigrationBatchJobHandler implements BatchJobHandler<MigrationBatchConfiguration> {

  public static final MigrationBatchJobDeclaration JOB_DECLARATION = new MigrationBatchJobDeclaration();

  public String getType() {
    return Batch.TYPE_PROCESS_INSTANCE_MIGRATION;
  }

  public JobDeclaration<?, MessageEntity> getJobDeclaration() {
    return JOB_DECLARATION;
  }

  public byte[] writeConfiguration(MigrationBatchConfiguration configuration) {
    JSONObject jsonObject = MigrationBatchConfigurationJsonConverter.INSTANCE.toJsonObject(configuration);

    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    Writer writer = StringUtil.writerForStream(outStream);

    jsonObject.write(writer);
    IoUtil.flushSilently(writer);

    return outStream.toByteArray();
  }

  public MigrationBatchConfiguration readConfiguration(byte[] serializedConfiguration) {
    Reader jsonReader = StringUtil.readerFromBytes(serializedConfiguration);
    return MigrationBatchConfigurationJsonConverter.INSTANCE.toObject(new JSONObject(new JSONTokener(jsonReader)));
  }

  public boolean createJobs(BatchEntity batch) {
    CommandContext commandContext = Context.getCommandContext();
    ByteArrayManager byteArrayManager = commandContext.getByteArrayManager();
    JobManager jobManager = commandContext.getJobManager();

    MigrationBatchConfiguration configuration = readConfiguration(batch.getConfigurationBytes());
    MigrationPlan migrationPlan = configuration.getMigrationPlan();
    String sourceDeploymentId = getProcessDefinition(commandContext, migrationPlan.getSourceProcessDefinitionId()).getDeploymentId();

    int batchJobsPerSeed = batch.getBatchJobsPerSeed();
    int invocationsPerBatchJob = batch.getInvocationsPerBatchJob();

    List<String> processInstanceIds = configuration.getProcessInstanceIds();
    int numberOfInstancesToProcess = Math.min(invocationsPerBatchJob * batchJobsPerSeed, processInstanceIds.size());
    // view of process instances to process
    List<String> processInstancesToProcess = processInstanceIds.subList(0, numberOfInstancesToProcess);

    int createdJobs = 0;
    while (!processInstancesToProcess.isEmpty()) {
      int lastIdIndex = Math.min(invocationsPerBatchJob, processInstancesToProcess.size());
      // view of process instances for this job
      List<String> idsForJob = processInstancesToProcess.subList(0, lastIdIndex);

      MigrationBatchConfiguration jobConfiguration = MigrationBatchConfiguration
          .create(migrationPlan, idsForJob, configuration.isSkipCustomListeners(), configuration.isSkipIoMappings());
      ByteArrayEntity configurationEntity = saveConfiguration(byteArrayManager, jobConfiguration);

      JobEntity job = createBatchJob(batch, configurationEntity);
      job.setDeploymentId(sourceDeploymentId);
      jobManager.insertAndHintJobExecutor(job);

      idsForJob.clear();
      createdJobs++;
    }

    // update created jobs for batch
    batch.setJobsCreated(batch.getJobsCreated() + createdJobs);

    // update batch configuration
    batch.setConfigurationBytes(writeConfiguration(configuration));

    return processInstanceIds.isEmpty();
  }

  protected ByteArrayEntity saveConfiguration(ByteArrayManager byteArrayManager, MigrationBatchConfiguration jobConfiguration) {
    ByteArrayEntity configurationEntity = new ByteArrayEntity();
    configurationEntity.setBytes(writeConfiguration(jobConfiguration));
    byteArrayManager.insert(configurationEntity);
    return configurationEntity;
  }

  protected JobEntity createBatchJob(BatchEntity batch, ByteArrayEntity configuration) {
    BatchJobContext creationContext = new BatchJobContext(batch, configuration);
    return JOB_DECLARATION.createJobInstance(creationContext);
  }

  @Override
  public void deleteJobs(BatchEntity batch) {
    List<JobEntity> jobs = Context.getCommandContext()
      .getJobManager()
      .findJobsByJobDefinitionId(batch.getBatchJobDefinitionId());

    for (JobEntity job : jobs) {
      job.delete();
    }
  }

  @Override
  public void execute(BatchJobConfiguration configuration, CoreExecution execution, CommandContext commandContext, String tenantId) {
    ByteArrayEntity configurationEntity = commandContext
        .getDbEntityManager()
        .selectById(ByteArrayEntity.class, configuration.getConfigurationByteArrayId());

    MigrationBatchConfiguration batchConfiguration = readConfiguration(configurationEntity.getBytes());

    MigrationPlanExecutionBuilder executionBuilder = commandContext.getProcessEngineConfiguration()
      .getRuntimeService()
      .newMigration(batchConfiguration.getMigrationPlan())
        .processInstanceIds(batchConfiguration.getProcessInstanceIds());

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

  @Override
  public BatchJobConfiguration newConfiguration(String canonicalString) {
    return new BatchJobConfiguration(canonicalString);
  }

  public void onDelete(BatchJobConfiguration configuration, JobEntity jobEntity) {
    String byteArrayId = configuration.getConfigurationByteArrayId();
    if (byteArrayId != null) {
      Context.getCommandContext().getByteArrayManager()
        .deleteByteArrayById(byteArrayId);
    }
  }

  protected ProcessDefinitionEntity getProcessDefinition(CommandContext commandContext, String processDefinitionId) {
    return commandContext.getProcessEngineConfiguration()
      .getDeploymentCache()
      .findDeployedProcessDefinitionById(processDefinitionId);
  }

}
