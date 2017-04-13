package org.camunda.bpm.engine.impl.batch.externaltask;

import java.util.List;

import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.batch.AbstractBatchJobHandler;
import org.camunda.bpm.engine.impl.batch.BatchJobConfiguration;
import org.camunda.bpm.engine.impl.batch.BatchJobContext;
import org.camunda.bpm.engine.impl.batch.BatchJobDeclaration;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobDeclaration;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;

public class SetExternalTaskRetriesJobHandler extends AbstractBatchJobHandler<SetExternalTaskRetriesBatchConfiguration> {

  public static final BatchJobDeclaration JOB_DECLARATION = new BatchJobDeclaration(Batch.TYPE_SET_EXTERNAL_TASK_RETRIES);
  
  @Override
  public String getType() {
    return Batch.TYPE_SET_EXTERNAL_TASK_RETRIES;
  }

  @Override
  public void execute(BatchJobConfiguration configuration, ExecutionEntity execution, CommandContext commandContext, String tenantId) {
    ByteArrayEntity configurationEntity = commandContext
        .getDbEntityManager()
        .selectById(ByteArrayEntity.class, configuration.getConfigurationByteArrayId());

    SetExternalTaskRetriesBatchConfiguration batchConfiguration = readConfiguration(configurationEntity.getBytes());

    boolean initialLegacyRestrictions = commandContext.isRestrictUserOperationLogToAuthenticatedUsers();
    commandContext.disableUserOperationLog();
    commandContext.setRestrictUserOperationLogToAuthenticatedUsers(true);
    try {
      commandContext.getProcessEngineConfiguration()
          .getExternalTaskService()
          .setRetriesSync(batchConfiguration.getIds(), null, batchConfiguration.getRetries());
    } finally {
      commandContext.enableUserOperationLog();
      commandContext.setRestrictUserOperationLogToAuthenticatedUsers(initialLegacyRestrictions);
    }

    commandContext.getByteArrayManager().delete(configurationEntity);
    
  }

  @Override
  public JobDeclaration<BatchJobContext, MessageEntity> getJobDeclaration() {
    return JOB_DECLARATION;
  }

  @Override
  protected SetExternalTaskRetriesBatchConfiguration createJobConfiguration(SetExternalTaskRetriesBatchConfiguration configuration,
      List<String> processIdsForJob) {
    return new SetExternalTaskRetriesBatchConfiguration(processIdsForJob, configuration.getRetries());
  }

  @Override
  protected SetExternalTaskRetriesBatchConfigurationJsonConverter getJsonConverterInstance() {
    return SetExternalTaskRetriesBatchConfigurationJsonConverter.INSTANCE;
  }

}
