package org.camunda.bpm.engine.impl;

import java.util.List;

import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.batch.AbstractBatchJobHandler;
import org.camunda.bpm.engine.impl.batch.BatchJobConfiguration;
import org.camunda.bpm.engine.impl.batch.BatchJobContext;
import org.camunda.bpm.engine.impl.batch.BatchJobDeclaration;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobDeclaration;
import org.camunda.bpm.engine.impl.json.ModificationBatchConfigurationJsonConverter;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;

public class ModificationBatchJobHandler extends AbstractBatchJobHandler<ModificationBatchConfiguration>{

  public static final BatchJobDeclaration JOB_DECLARATION = new BatchJobDeclaration(Batch.TYPE_PROCESS_INSTANCE_MODIFICATION);

  @Override
  public String getType() {
    return Batch.TYPE_PROCESS_INSTANCE_MODIFICATION;
  }

  @Override
  protected void postProcessJob(ModificationBatchConfiguration configuration, JobEntity job) {
    CommandContext commandContext = Context.getCommandContext();
    ProcessDefinitionEntity processDefinitionEntity = commandContext.getProcessEngineConfiguration().getDeploymentCache().findDeployedProcessDefinitionById(configuration.getProcessDefinitionId());
    job.setDeploymentId(processDefinitionEntity.getDeploymentId());
  }

  @Override
  public void execute(BatchJobConfiguration configuration, ExecutionEntity execution, CommandContext commandContext, String tenantId) {
    ByteArrayEntity configurationEntity = commandContext
        .getDbEntityManager()
        .selectById(ByteArrayEntity.class, configuration.getConfigurationByteArrayId());

    ModificationBatchConfiguration batchConfiguration = readConfiguration(configurationEntity.getBytes());

    ModificationBuilderImpl executionBuilder = (ModificationBuilderImpl) commandContext.getProcessEngineConfiguration()
        .getRuntimeService()
        .createModification(batchConfiguration.getProcessDefinitionId())
        .processInstanceIds(batchConfiguration.getIds());

    executionBuilder.setInstructions(batchConfiguration.getInstructions());

    if (batchConfiguration.isSkipCustomListeners()) {
      executionBuilder.skipCustomListeners();
    }
    if (batchConfiguration.isSkipIoMappings()) {
      executionBuilder.skipIoMappings();
    }

    executionBuilder.execute(false);

    commandContext.getByteArrayManager().delete(configurationEntity);

  }

  @Override
  public JobDeclaration<BatchJobContext, MessageEntity> getJobDeclaration() {
    return JOB_DECLARATION;
  }

  @Override
  protected ModificationBatchConfiguration createJobConfiguration(ModificationBatchConfiguration configuration, List<String> processIdsForJob) {
    return new ModificationBatchConfiguration(
        processIdsForJob,
        configuration.getProcessDefinitionId(),
        configuration.getInstructions(),
        configuration.isSkipCustomListeners(),
        configuration.isSkipIoMappings()
    );
  }


  @Override
  protected ModificationBatchConfigurationJsonConverter getJsonConverterInstance() {
    return ModificationBatchConfigurationJsonConverter.INSTANCE;
  }

  protected ProcessDefinitionEntity getProcessDefinition(CommandContext commandContext, String processDefinitionId) {
    return commandContext.getProcessEngineConfiguration()
        .getDeploymentCache()
        .findDeployedProcessDefinitionById(processDefinitionId);
  }

}
