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
import org.camunda.bpm.engine.impl.batch.BatchJobContext;
import org.camunda.bpm.engine.impl.batch.BatchJobDeclaration;
import org.camunda.bpm.engine.impl.batch.history.HistoricBatchEntity;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobDeclaration;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;

import java.util.Date;
import java.util.List;

import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_REMOVAL_TIME_STRATEGY_END;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_REMOVAL_TIME_STRATEGY_START;

/**
 * @author Tassilo Weidner
 */
public class BatchSetRemovalTimeJobHandler extends AbstractBatchJobHandler<SetRemovalTimeBatchConfiguration> {

  public static final BatchJobDeclaration JOB_DECLARATION = new BatchJobDeclaration(Batch.TYPE_BATCH_SET_REMOVAL_TIME);

  public void executeHandler(SetRemovalTimeBatchConfiguration batchConfiguration,
                             ExecutionEntity execution,
                             CommandContext commandContext,
                             String tenantId) {

    for (String instanceId : batchConfiguration.getIds()) {

      HistoricBatchEntity instance = findBatchById(instanceId, commandContext);

      if (instance != null) {
        Date removalTime = getOrCalculateRemovalTime(batchConfiguration, instance, commandContext);

        if (removalTime != instance.getRemovalTime()) {
          addRemovalTime(instanceId, removalTime, commandContext);

        }
      }
    }
  }

  protected Date getOrCalculateRemovalTime(SetRemovalTimeBatchConfiguration batchConfiguration, HistoricBatchEntity instance, CommandContext commandContext) {
    if (batchConfiguration.hasRemovalTime()) {
      return batchConfiguration.getRemovalTime();

    } else if (hasBaseTime(instance, commandContext)) {
      return calculateRemovalTime(instance, commandContext);

    } else {
      return null;

    }
  }

  protected void addRemovalTime(String instanceId, Date removalTime, CommandContext commandContext) {
    commandContext.getHistoricBatchManager()
      .addRemovalTimeById(instanceId, removalTime);
  }

  protected boolean hasBaseTime(HistoricBatchEntity instance, CommandContext commandContext) {
    return isStrategyStart(commandContext) || (isStrategyEnd(commandContext) && isEnded(instance));
  }

  protected boolean isEnded(HistoricBatchEntity instance) {
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

  protected boolean isDmnEnabled(CommandContext commandContext) {
    return commandContext.getProcessEngineConfiguration().isDmnEnabled();
  }

  protected Date calculateRemovalTime(HistoricBatchEntity batch, CommandContext commandContext) {
    return commandContext.getProcessEngineConfiguration()
      .getHistoryRemovalTimeProvider()
      .calculateRemovalTime(batch);
  }

  protected ByteArrayEntity findByteArrayById(String byteArrayId, CommandContext commandContext) {
    return commandContext.getDbEntityManager()
      .selectById(ByteArrayEntity.class, byteArrayId);
  }

  protected HistoricBatchEntity findBatchById(String instanceId, CommandContext commandContext) {
    return commandContext.getHistoricBatchManager()
      .findHistoricBatchById(instanceId);
  }

  public JobDeclaration<BatchJobContext, MessageEntity> getJobDeclaration() {
    return JOB_DECLARATION;
  }

  protected SetRemovalTimeBatchConfiguration createJobConfiguration(SetRemovalTimeBatchConfiguration configuration, List<String> batchIds) {
    return new SetRemovalTimeBatchConfiguration(batchIds)
      .setRemovalTime(configuration.getRemovalTime())
      .setHasRemovalTime(configuration.hasRemovalTime());
  }

  protected SetRemovalTimeJsonConverter getJsonConverterInstance() {
    return SetRemovalTimeJsonConverter.INSTANCE;
  }

  public String getType() {
    return Batch.TYPE_BATCH_SET_REMOVAL_TIME;
  }

}
