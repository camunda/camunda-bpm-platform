package org.camunda.bpm.engine.impl.batch;

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.variable.mapping.value.ConstantValueProvider;
import org.camunda.bpm.engine.impl.core.variable.mapping.value.ParameterValueProvider;
import org.camunda.bpm.engine.impl.jobexecutor.JobDeclaration;
import org.camunda.bpm.engine.impl.jobexecutor.JobHandlerConfiguration;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;


public class AbstractProcessInstanceBatchJobDeclaration extends JobDeclaration<BatchJobContext, MessageEntity> {

  public AbstractProcessInstanceBatchJobDeclaration(String jobHandlerType) {
    super(jobHandlerType);
  }

  @Override
  protected ExecutionEntity resolveExecution(BatchJobContext context) {
    return null;
  }

  @Override
  protected MessageEntity newJobInstance(BatchJobContext context) {
    return new MessageEntity();
  }

  @Override
  protected JobHandlerConfiguration resolveJobHandlerConfiguration(BatchJobContext context) {
    return new BatchJobConfiguration(context.getConfiguration().getId());
  }

  @Override
  protected String resolveJobDefinitionId(BatchJobContext context) {
    return context.getBatch().getBatchJobDefinitionId();
  }

  public ParameterValueProvider getJobPriorityProvider() {
    long batchJobPriority = Context.getProcessEngineConfiguration()
      .getBatchJobPriority();
    return new ConstantValueProvider(batchJobPriority);
  }

}
