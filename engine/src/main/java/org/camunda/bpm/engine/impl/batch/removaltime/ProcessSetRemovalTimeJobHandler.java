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
package org.camunda.bpm.engine.impl.batch.removaltime;

import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_REMOVAL_TIME_STRATEGY_END;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_REMOVAL_TIME_STRATEGY_START;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.batch.AbstractBatchJobHandler;
import org.camunda.bpm.engine.impl.batch.BatchJobContext;
import org.camunda.bpm.engine.impl.batch.BatchJobDeclaration;
import org.camunda.bpm.engine.impl.cfg.TransactionListener;
import org.camunda.bpm.engine.impl.cfg.TransactionState;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.JobDeclaration;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;
import org.camunda.bpm.engine.repository.ProcessDefinition;

/**
 * @author Tassilo Weidner
 */
public class ProcessSetRemovalTimeJobHandler extends AbstractBatchJobHandler<SetRemovalTimeBatchConfiguration> {

  public static final BatchJobDeclaration JOB_DECLARATION = new BatchJobDeclaration(Batch.TYPE_PROCESS_SET_REMOVAL_TIME);
  public static final int MAX_CHUNK_SIZE = 500;

  @Override
  public void executeHandler(SetRemovalTimeBatchConfiguration configuration,
                             ExecutionEntity execution,
                             CommandContext commandContext,
                             String tenantId) {
    if (configuration.isUpdateInChunks()) {
      // only one instance allowed if enabled, see #calculateInvocationsPerBatchJob
      String instanceId = configuration.getIds().get(0);
      Set<String> entities = configuration.getEntities();
      Integer chunkSize = getUpdateChunkSize(configuration, commandContext);
      Map<Class<? extends DbEntity>, DbOperation> operations = addRemovalTimeToInstance(instanceId, configuration,
          chunkSize, entities, commandContext);
      MessageEntity currentJob = (MessageEntity) commandContext.getCurrentJob();
      registerTransactionHandler(configuration, operations, chunkSize, currentJob, commandContext);
      currentJob.setRepeat("true");
    } else {
      configuration.getIds().forEach(id ->
        addRemovalTimeToInstance(id, configuration, null, Collections.emptySet(), commandContext));
    }
  }

  protected Map<Class<? extends DbEntity>, DbOperation> addRemovalTimeToInstance(String instanceId,
      SetRemovalTimeBatchConfiguration configuration,
      Integer batchSize,
      Set<String> entities,
      CommandContext commandContext) {
    HistoricProcessInstanceEntity instance = findProcessInstanceById(instanceId, commandContext);
    if (instance != null) {
      if (configuration.isHierarchical() && hasHierarchy(instance)) {
        String rootProcessInstanceId = instance.getRootProcessInstanceId();
        HistoricProcessInstanceEntity rootInstance = findProcessInstanceById(rootProcessInstanceId, commandContext);
        Date removalTime = getOrCalculateRemovalTime(configuration, rootInstance, commandContext);
        return addRemovalTimeToHierarchy(rootProcessInstanceId, removalTime, batchSize, entities, commandContext);
      } else {
        Date removalTime = getOrCalculateRemovalTime(configuration, instance, commandContext);
        if (removalTime != instance.getRemovalTime()) {
          return addRemovalTime(instanceId, removalTime, batchSize, entities, commandContext);
        }
      }
    }
    return null;
  }

  protected Date getOrCalculateRemovalTime(SetRemovalTimeBatchConfiguration configuration,
      HistoricProcessInstanceEntity instance,
      CommandContext commandContext) {
    if (configuration.hasRemovalTime()) {
      return configuration.getRemovalTime();
    } else if (hasBaseTime(instance, commandContext)) {
      return calculateRemovalTime(instance, commandContext);
    } else {
      return null;
    }
  }

  protected Map<Class<? extends DbEntity>, DbOperation> addRemovalTimeToHierarchy(String rootProcessInstanceId,
      Date removalTime,
      Integer batchSize,
      Set<String> entities,
      CommandContext commandContext) {
    Map<Class<? extends DbEntity>, DbOperation> operations = commandContext.getHistoricProcessInstanceManager()
        .addRemovalTimeToProcessInstancesByRootProcessInstanceId(rootProcessInstanceId, removalTime, batchSize, entities);
    if (isDmnEnabled(commandContext)) {
      operations.putAll(commandContext.getHistoricDecisionInstanceManager()
        .addRemovalTimeToDecisionsByRootProcessInstanceId(rootProcessInstanceId, removalTime, batchSize, entities));
    }
    return operations;
  }

  protected Map<Class<? extends DbEntity>, DbOperation> addRemovalTime(String instanceId,
      Date removalTime,
      Integer batchSize,
      Set<String> entities,
      CommandContext commandContext) {
    Map<Class<? extends DbEntity>, DbOperation> operations = commandContext.getHistoricProcessInstanceManager()
      .addRemovalTimeById(instanceId, removalTime, batchSize, entities);
    if (isDmnEnabled(commandContext)) {
      operations.putAll(commandContext.getHistoricDecisionInstanceManager()
        .addRemovalTimeToDecisionsByProcessInstanceId(instanceId, removalTime, batchSize, entities));
    }
    return operations;
  }

  protected boolean hasBaseTime(HistoricProcessInstanceEntity instance, CommandContext commandContext) {
    return isStrategyStart(commandContext) || (isStrategyEnd(commandContext) && isEnded(instance));
  }

  protected boolean isEnded(HistoricProcessInstanceEntity instance) {
    return instance.getEndTime() != null;
  }

  protected boolean isStrategyStart(CommandContext commandContext) {
    return HISTORY_REMOVAL_TIME_STRATEGY_START.equals(getHistoryRemovalTimeStrategy(commandContext));
  }

  protected boolean isStrategyEnd(CommandContext commandContext) {
    return HISTORY_REMOVAL_TIME_STRATEGY_END.equals(getHistoryRemovalTimeStrategy(commandContext));
  }

  protected boolean hasHierarchy(HistoricProcessInstanceEntity instance) {
    return instance.getRootProcessInstanceId() != null;
  }

  protected String getHistoryRemovalTimeStrategy(CommandContext commandContext) {
    return commandContext.getProcessEngineConfiguration()
      .getHistoryRemovalTimeStrategy();
  }

  protected ProcessDefinition findProcessDefinitionById(String processDefinitionId, CommandContext commandContext) {
    return commandContext.getProcessEngineConfiguration()
      .getDeploymentCache()
      .findDeployedProcessDefinitionById(processDefinitionId);
  }

  protected boolean isDmnEnabled(CommandContext commandContext) {
    return commandContext.getProcessEngineConfiguration().isDmnEnabled();
  }

  protected Date calculateRemovalTime(HistoricProcessInstanceEntity processInstance, CommandContext commandContext) {
    ProcessDefinition processDefinition = findProcessDefinitionById(processInstance.getProcessDefinitionId(), commandContext);

    return commandContext.getProcessEngineConfiguration()
      .getHistoryRemovalTimeProvider()
      .calculateRemovalTime(processInstance, processDefinition);
  }

  protected ByteArrayEntity findByteArrayById(String byteArrayId, CommandContext commandContext) {
    return commandContext.getDbEntityManager()
      .selectById(ByteArrayEntity.class, byteArrayId);
  }

  protected HistoricProcessInstanceEntity findProcessInstanceById(String instanceId, CommandContext commandContext) {
    return commandContext.getHistoricProcessInstanceManager()
      .findHistoricProcessInstance(instanceId);
  }

  protected void registerTransactionHandler(SetRemovalTimeBatchConfiguration configuration,
      Map<Class<? extends DbEntity>, DbOperation> operations,
      Integer chunkSize,
      MessageEntity currentJob,
      CommandContext commandContext) {
    CommandExecutor newCommandExecutor = commandContext.getProcessEngineConfiguration().getCommandExecutorTxRequiresNew();
    TransactionListener transactionResulthandler = createTransactionHandler(configuration, operations, chunkSize,
        currentJob, newCommandExecutor);
    commandContext.getTransactionContext().addTransactionListener(TransactionState.COMMITTED, transactionResulthandler);
  }

  protected int getUpdateChunkSize(SetRemovalTimeBatchConfiguration configuration, CommandContext commandContext) {
    return configuration.getChunkSize() == null
        ? commandContext.getProcessEngineConfiguration().getRemovalTimeUpdateChunkSize()
        : configuration.getChunkSize();
  }

  protected TransactionListener createTransactionHandler(SetRemovalTimeBatchConfiguration configuration,
      Map<Class<? extends DbEntity>, DbOperation> operations,
      Integer chunkSize,
      MessageEntity currentJob,
      CommandExecutor newCommandExecutor) {
    return new ProcessSetRemovalTimeResultHandler(configuration, chunkSize, newCommandExecutor, this,
        currentJob.getId(), operations);
  }

  @Override
  public JobDeclaration<BatchJobContext, MessageEntity> getJobDeclaration() {
    return JOB_DECLARATION;
  }

  @Override
  protected SetRemovalTimeBatchConfiguration createJobConfiguration(SetRemovalTimeBatchConfiguration configuration,
      List<String> processInstanceIds) {
    return new SetRemovalTimeBatchConfiguration(processInstanceIds)
      .setRemovalTime(configuration.getRemovalTime())
      .setHasRemovalTime(configuration.hasRemovalTime())
      .setHierarchical(configuration.isHierarchical())
      .setUpdateInChunks(configuration.isUpdateInChunks())
      .setChunkSize(configuration.getChunkSize());
  }

  @Override
  protected SetRemovalTimeJsonConverter getJsonConverterInstance() {
    return SetRemovalTimeJsonConverter.INSTANCE;
  }

  @Override
  public int calculateInvocationsPerBatchJob(String batchType, SetRemovalTimeBatchConfiguration configuration) {
    if (configuration.isUpdateInChunks()) {
      return 1;
    }
    return super.calculateInvocationsPerBatchJob(batchType, configuration);
  }

  @Override
  public String getType() {
    return Batch.TYPE_PROCESS_SET_REMOVAL_TIME;
  }
}
