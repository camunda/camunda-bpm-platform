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

package org.camunda.bpm.engine.impl.batch.deletion;

import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.ProcessInstanceQueryImpl;
import org.camunda.bpm.engine.impl.batch.AbstractBatchJobHandler;
import org.camunda.bpm.engine.impl.batch.BatchEntity;
import org.camunda.bpm.engine.impl.batch.BatchJobConfiguration;
import org.camunda.bpm.engine.impl.batch.BatchJobContext;
import org.camunda.bpm.engine.impl.batch.BatchJobDeclaration;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobDeclaration;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayManager;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobManager;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Askar Akhmerov
 */
public class DeleteProcessInstancesJobHandler extends AbstractBatchJobHandler<DeleteProcessInstanceBatchConfiguration> {

  public static final BatchJobDeclaration JOB_DECLARATION = new BatchJobDeclaration(Batch.TYPE_PROCESS_INSTANCE_DELETION);

  @Override
  public String getType() {
    return Batch.TYPE_PROCESS_INSTANCE_DELETION;
  }

  protected DeleteProcessInstanceBatchConfigurationJsonConverter getJsonConverterInstance() {
    return DeleteProcessInstanceBatchConfigurationJsonConverter.INSTANCE;
  }

  @Override
  public JobDeclaration<BatchJobContext, MessageEntity> getJobDeclaration() {
    return JOB_DECLARATION;
  }

  @Override
  protected DeleteProcessInstanceBatchConfiguration createJobConfiguration(DeleteProcessInstanceBatchConfiguration configuration, List<String> processIdsForJob) {
    return new DeleteProcessInstanceBatchConfiguration(processIdsForJob, configuration.getDeleteReason(), configuration.isSkipCustomListeners(), configuration.isSkipSubprocesses());
  }

  @Override
  public void execute(BatchJobConfiguration configuration, ExecutionEntity execution, CommandContext commandContext, String tenantId) {
    ByteArrayEntity configurationEntity = commandContext
        .getDbEntityManager()
        .selectById(ByteArrayEntity.class, configuration.getConfigurationByteArrayId());

    DeleteProcessInstanceBatchConfiguration batchConfiguration = readConfiguration(configurationEntity.getBytes());

    boolean initialLegacyRestrictions = commandContext.isRestrictUserOperationLogToAuthenticatedUsers();
    commandContext.disableUserOperationLog();
    commandContext.setRestrictUserOperationLogToAuthenticatedUsers(true);
    try {
      commandContext.getProcessEngineConfiguration()
          .getRuntimeService()
          .deleteProcessInstances(batchConfiguration.getIds(), batchConfiguration.deleteReason, batchConfiguration.isSkipCustomListeners(), true, batchConfiguration.isSkipSubprocesses());
    } finally {
      commandContext.enableUserOperationLog();
      commandContext.setRestrictUserOperationLogToAuthenticatedUsers(initialLegacyRestrictions);
    }

    commandContext.getByteArrayManager().delete(configurationEntity);
  }

  @Override
  public boolean createJobs(BatchEntity batch) {
    DeleteProcessInstanceBatchConfiguration configuration = readConfiguration(batch.getConfigurationBytes());

    List<String> ids = configuration.getIds();
    final CommandContext commandContext = Context.getCommandContext();

    int batchJobsPerSeed = batch.getBatchJobsPerSeed();
    int invocationsPerBatchJob = batch.getInvocationsPerBatchJob();

    int numberOfItemsToProcess = Math.min(invocationsPerBatchJob * batchJobsPerSeed, ids.size());
    // view of process instances to process
    final List<String> processIds = ids.subList(0, numberOfItemsToProcess);

    List<String> deploymentIds = commandContext.runWithoutAuthorization(new Callable<List<String>>() {
      @Override
      public List<String> call() throws Exception {
        return commandContext.getDeploymentManager().findDeploymentIdsByProcessInstances(processIds);
      }
    });

    for (final String deploymentId : deploymentIds) {

      List<String> processIdsPerDeployment = commandContext.runWithoutAuthorization(new Callable<List<String>>() {
        @Override
        public List<String> call() throws Exception {
          final ProcessInstanceQueryImpl processInstanceQueryToBeProcess = new ProcessInstanceQueryImpl();
          processInstanceQueryToBeProcess.processInstanceIds(new HashSet<String>(processIds)).deploymentId(deploymentId);
          return commandContext.getExecutionManager().findProcessInstancesIdsByQueryCriteria(processInstanceQueryToBeProcess);
        }
      });

      processIds.removeAll(processIdsPerDeployment);

      createJobEntities(batch, configuration, deploymentId, processIdsPerDeployment, invocationsPerBatchJob);
    }

    // when there are non existing process instance ids
    if (!processIds.isEmpty()) {
      createJobEntities(batch, configuration, null, processIds, invocationsPerBatchJob);
    }

    return ids.isEmpty();
  }

  protected void createJobEntities(BatchEntity batch, DeleteProcessInstanceBatchConfiguration configuration, String deploymentId,
      List<String> processInstancesToHandle, int invocationsPerBatchJob) {


    CommandContext commandContext = Context.getCommandContext();
    ByteArrayManager byteArrayManager = commandContext.getByteArrayManager();
    JobManager jobManager = commandContext.getJobManager();

    int createdJobs = 0;
    while (!processInstancesToHandle.isEmpty()) {
      int lastIdIndex = Math.min(invocationsPerBatchJob, processInstancesToHandle.size());
      // view of process instances for this job
      List<String> idsForJob = processInstancesToHandle.subList(0, lastIdIndex);

      DeleteProcessInstanceBatchConfiguration jobConfiguration = createJobConfiguration(configuration, idsForJob);
      ByteArrayEntity configurationEntity = saveConfiguration(byteArrayManager, jobConfiguration);

      JobEntity job = createBatchJob(batch, configurationEntity);
      job.setDeploymentId(deploymentId);

      jobManager.insertAndHintJobExecutor(job);
      createdJobs++;

      idsForJob.clear();
    }

    // update created jobs for batch
    batch.setJobsCreated(batch.getJobsCreated() + createdJobs);

    // update batch configuration
    batch.setConfigurationBytes(writeConfiguration(configuration));
  }
}
