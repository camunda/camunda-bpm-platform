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

import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.batch.AbstractBatchJobHandler;
import org.camunda.bpm.engine.impl.batch.BatchJobConfiguration;
import org.camunda.bpm.engine.impl.batch.BatchJobContext;
import org.camunda.bpm.engine.impl.batch.BatchJobDeclaration;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobDeclaration;
import org.camunda.bpm.engine.impl.json.JsonObjectConverter;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;
import org.camunda.bpm.engine.repository.ProcessDefinition;

import java.util.Date;
import java.util.List;

import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_REMOVAL_TIME_STRATEGY_END;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_REMOVAL_TIME_STRATEGY_START;

/**
 * @author Tassilo Weidner
 */
public class ProcessSetRemovalTimeJobHandler extends AbstractBatchJobHandler<SetRemovalTimeBatchConfiguration> {

  public static final BatchJobDeclaration JOB_DECLARATION = new BatchJobDeclaration(Batch.TYPE_PROCESS_SET_REMOVAL_TIME);

  public void execute(BatchJobConfiguration configuration, ExecutionEntity execution, CommandContext commandContext, String tenantId) {

    String byteArrayId = configuration.getConfigurationByteArrayId();
    byte[] configurationByteArray = findByteArrayById(byteArrayId, commandContext).getBytes();

    SetRemovalTimeBatchConfiguration batchConfiguration = readConfiguration(configurationByteArray);

    for (String instanceId : batchConfiguration.getIds()) {

      HistoricProcessInstanceEntity instance = findProcessInstanceById(instanceId, commandContext);

      if (!batchConfiguration.hasRemovalTime()) {
        if (canSetRemovalTime(instance, commandContext)) {
          addRemovalTime(instanceId, calculateRemovalTime(instance, commandContext), commandContext);

        }
      } else {
        addRemovalTime(instanceId, batchConfiguration.getRemovalTime(), commandContext);

      }
    }
  }

  protected void addRemovalTime(String instanceId, Date removalTime, CommandContext commandContext) {
    commandContext.getHistoricProcessInstanceManager()
      .addRemovalTimeById(instanceId, removalTime);

    if (isDmnEnabled(commandContext)) {
      commandContext.getHistoricDecisionInstanceManager()
        .addRemovalTimeToDecisionsByProcessInstanceId(instanceId, removalTime);
    }
  }

  protected boolean canSetRemovalTime(HistoricProcessInstanceEntity instance, CommandContext commandContext) {
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
      .findHistoricProcessInstanceByIdForRemovalTimeBatch(instanceId);
  }

  public JobDeclaration<BatchJobContext, MessageEntity> getJobDeclaration() {
    return JOB_DECLARATION;
  }

  protected SetRemovalTimeBatchConfiguration createJobConfiguration(SetRemovalTimeBatchConfiguration configuration, List<String> processInstanceIds) {
    return new SetRemovalTimeBatchConfiguration(processInstanceIds, configuration.getRemovalTime(), configuration.hasRemovalTime());
  }

  protected JsonObjectConverter<SetRemovalTimeBatchConfiguration> getJsonConverterInstance() {
    return SetRemovalTimeJsonConverter.INSTANCE;
  }

  public String getType() {
    return Batch.TYPE_PROCESS_SET_REMOVAL_TIME;
  }

}
