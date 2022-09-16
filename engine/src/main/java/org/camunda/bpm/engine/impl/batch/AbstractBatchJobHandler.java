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
package org.camunda.bpm.engine.impl.batch;

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.entitymanager.OptimisticLockingListener;
import org.camunda.bpm.engine.impl.db.entitymanager.OptimisticLockingResult;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbEntityOperation;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobDeclaration;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayManager;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobManager;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.JsonUtil;
import com.google.gson.JsonElement;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Common methods for batch job handlers based on list of ids, providing serialization, configuration instantiation, etc.
 *
 * @author Askar Akhmerov
 */
public abstract class AbstractBatchJobHandler<T extends BatchConfiguration> implements BatchJobHandler<T>, OptimisticLockingListener {

  public abstract JobDeclaration<BatchJobContext, MessageEntity> getJobDeclaration();

  @Override
  public boolean createJobs(BatchEntity batch) {
    T configuration = readConfiguration(batch.getConfigurationBytes());
    String deploymentId = null;

    DeploymentMappings idMappings = configuration.getIdMappings();
    boolean deploymentAware = idMappings != null && !idMappings.isEmpty();

    List<String> ids = configuration.getIds();

    if (deploymentAware) {
      sanitizeMappings(idMappings, ids);
      DeploymentMapping mappingToProcess = idMappings.get(0);
      ids = mappingToProcess.getIds(ids);
      deploymentId = mappingToProcess.getDeploymentId();
    }

    int batchJobsPerSeed = batch.getBatchJobsPerSeed();
    int invocationsPerBatchJob = batch.getInvocationsPerBatchJob();

    int numberOfItemsToProcess = Math.min(invocationsPerBatchJob * batchJobsPerSeed, ids.size());

    // view of process instances to process
    List<String> processIds = ids.subList(0, numberOfItemsToProcess);
    createJobEntities(batch, configuration, deploymentId, processIds, invocationsPerBatchJob);
    if (deploymentAware) {
      if (ids.isEmpty()) {
        // all ids of the deployment are handled
        idMappings.remove(0);
      } else {
        idMappings.get(0).removeIds(numberOfItemsToProcess);
      }
    }

    // update batch configuration
    batch.setConfigurationBytes(writeConfiguration(configuration));

    return deploymentAware ? idMappings.isEmpty() : ids.isEmpty();
  }

  @Override
  public final void execute(final BatchJobConfiguration configuration,
                            final ExecutionEntity execution,
                            final CommandContext commandContext,
                            final String tenantId) {

    // load handler config
    String byteArrayId = configuration.getConfigurationByteArrayId();
    ByteArrayEntity byteArray = commandContext.getDbEntityManager().selectById(ByteArrayEntity.class, byteArrayId);
    byte[] configurationByteArray = byteArray.getBytes();
    T batchConfiguration = readConfiguration(configurationByteArray);

    // load batch
    String batchId = batchConfiguration.getBatchId();
    final BatchEntity batch = commandContext.getBatchManager().findBatchById(batchId);

    // set executionStartTime & fire historic update event
    if (batch != null && batch.getExecutionStartTime() == null) {
      // batches with multiple jobs could trigger an OptimisticLockingException, register listener to ignore that
      commandContext.getDbEntityManager().registerOptimisticLockingListener(this);

      final Date executionStartTime = ClockUtil.now();
      batch.setExecutionStartTime(executionStartTime);
      batch.fireHistoricUpdateEvent();
    }

    executeHandler(batchConfiguration, execution, commandContext, tenantId);

    commandContext.getByteArrayManager().delete(byteArray);
  }

  protected abstract void executeHandler(final T configuration,
                                         final ExecutionEntity execution,
                                         final CommandContext commandContext,
                                         final String tenantId);

  protected void sanitizeMappings(DeploymentMappings idMappings, List<String> ids) {
    // for mixed version SeedJob execution, there might be ids that have been processed
    // without updating the mappings, this is corrected here,
    // see https://jira.camunda.com/browse/CAM-11188
    int elementsToRemove = idMappings.getOverallIdCount() - ids.size();
    if (elementsToRemove > 0) {
      for (Iterator<DeploymentMapping> iterator = idMappings.iterator(); iterator.hasNext();) {
        DeploymentMapping deploymentMapping = iterator.next();
        if (deploymentMapping.getCount() <= elementsToRemove) {
          iterator.remove();
          elementsToRemove -= deploymentMapping.getCount();
          if (elementsToRemove == 0) {
            break;
          }
        } else {
          deploymentMapping.removeIds(elementsToRemove);
          break;
        }
      }
    }
  }

  protected void createJobEntities(BatchEntity batch, T configuration, String deploymentId,
      List<String> processIds, int invocationsPerBatchJob) {

    if (processIds == null || processIds.isEmpty()) {
      return;
    }

    CommandContext commandContext = Context.getCommandContext();
    ByteArrayManager byteArrayManager = commandContext.getByteArrayManager();
    JobManager jobManager = commandContext.getJobManager();

    int createdJobs = 0;
    while (!processIds.isEmpty()) {
      int lastIdIndex = Math.min(invocationsPerBatchJob, processIds.size());
      // view of process instances for this job
      List<String> idsForJob = processIds.subList(0, lastIdIndex);

      T jobConfiguration = createJobConfiguration(configuration, idsForJob);

      jobConfiguration.setBatchId(batch.getId());

      ByteArrayEntity configurationEntity = saveConfiguration(byteArrayManager, jobConfiguration);

      JobEntity job = createBatchJob(batch, configurationEntity);
      job.setDeploymentId(deploymentId);
      postProcessJob(configuration, job, jobConfiguration);
      jobManager.insertAndHintJobExecutor(job);

      idsForJob.clear();
      createdJobs++;
    }

    // update created jobs for batch
    batch.setJobsCreated(batch.getJobsCreated() + createdJobs);
  }

  protected abstract T createJobConfiguration(T configuration, List<String> processIdsForJob);

  protected void postProcessJob(T configuration, JobEntity job, T jobConfiguration) {
    // do nothing as default
  }

  protected JobEntity createBatchJob(BatchEntity batch, ByteArrayEntity configuration) {
    BatchJobContext creationContext = new BatchJobContext(batch, configuration);
    return getJobDeclaration().createJobInstance(creationContext);
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
  public BatchJobConfiguration newConfiguration(String canonicalString) {
    return new BatchJobConfiguration(canonicalString);
  }

  @Override
  public void onDelete(BatchJobConfiguration configuration, JobEntity jobEntity) {
    String byteArrayId = configuration.getConfigurationByteArrayId();
    if (byteArrayId != null) {
      Context.getCommandContext().getByteArrayManager()
          .deleteByteArrayById(byteArrayId);
    }
  }

  protected ByteArrayEntity saveConfiguration(ByteArrayManager byteArrayManager, T jobConfiguration) {
    ByteArrayEntity configurationEntity = new ByteArrayEntity();
    configurationEntity.setBytes(writeConfiguration(jobConfiguration));
    byteArrayManager.insert(configurationEntity);
    return configurationEntity;
  }

  @Override
  public byte[] writeConfiguration(T configuration) {
    JsonElement jsonObject = getJsonConverterInstance().toJsonObject(configuration);

    return JsonUtil.asBytes(jsonObject);
  }

  @Override
  public T readConfiguration(byte[] serializedConfiguration) {
    return getJsonConverterInstance().toObject(JsonUtil.asObject(serializedConfiguration));
  }

  protected abstract AbstractBatchConfigurationObjectConverter<T> getJsonConverterInstance();

  @Override
  public Class<? extends DbEntity> getEntityType() {
    return BatchEntity.class;
  }

  @Override
  public OptimisticLockingResult failedOperation(final DbOperation operation) {
    if (operation instanceof DbEntityOperation) {
      return OptimisticLockingResult.IGNORE;
    }
    return OptimisticLockingResult.THROW;
  }

}
