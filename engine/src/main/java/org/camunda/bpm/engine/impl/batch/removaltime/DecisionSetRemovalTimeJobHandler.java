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
import org.camunda.bpm.engine.impl.history.event.HistoricDecisionInstanceEntity;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobDeclaration;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;
import org.camunda.bpm.engine.repository.DecisionDefinition;

import java.util.Date;
import java.util.List;

import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_REMOVAL_TIME_STRATEGY_END;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_REMOVAL_TIME_STRATEGY_START;

/**
 * @author Tassilo Weidner
 */
public class DecisionSetRemovalTimeJobHandler extends AbstractBatchJobHandler<SetRemovalTimeBatchConfiguration> {

  public static final BatchJobDeclaration JOB_DECLARATION = new BatchJobDeclaration(Batch.TYPE_DECISION_SET_REMOVAL_TIME);

  public void executeHandler(SetRemovalTimeBatchConfiguration batchConfiguration,
                             ExecutionEntity execution,
                             CommandContext commandContext,
                             String tenantId) {

    if (isDmnEnabled(commandContext)) {

      for (String instanceId : batchConfiguration.getIds()) {

        HistoricDecisionInstanceEntity instance = findDecisionInstanceById(instanceId, commandContext);

        if (instance != null) {
          if (batchConfiguration.isHierarchical()) {

            String rootDecisionInstanceId = getRootDecisionInstance(instance);

            HistoricDecisionInstanceEntity rootInstance = findDecisionInstanceById(rootDecisionInstanceId, commandContext);

            Date removalTime = getOrCalculateRemovalTime(batchConfiguration, rootInstance, commandContext);

            addRemovalTimeToHierarchy(rootDecisionInstanceId, removalTime, commandContext);

          } else {
            Date removalTime = getOrCalculateRemovalTime(batchConfiguration, instance, commandContext);

            if (removalTime != instance.getRemovalTime()) {
              addRemovalTime(instanceId, removalTime, commandContext);

            }
          }
        }
      }
    }
  }

  protected String getRootDecisionInstance(HistoricDecisionInstanceEntity instance) {
    return instance.getRootDecisionInstanceId() == null ? instance.getId() : instance.getRootDecisionInstanceId();
  }

  protected Date getOrCalculateRemovalTime(SetRemovalTimeBatchConfiguration batchConfiguration, HistoricDecisionInstanceEntity instance, CommandContext commandContext) {
    if (batchConfiguration.hasRemovalTime()) {
      return batchConfiguration.getRemovalTime();

    } else if (hasBaseTime(commandContext)){
      return calculateRemovalTime(instance, commandContext);

    } else {
      return null;

    }
  }

  protected void addRemovalTimeToHierarchy(String instanceId, Date removalTime, CommandContext commandContext) {
    commandContext.getHistoricDecisionInstanceManager()
      .addRemovalTimeToDecisionsByRootDecisionInstanceId(instanceId, removalTime);
  }

  protected void addRemovalTime(String instanceId, Date removalTime, CommandContext commandContext) {
    commandContext.getHistoricDecisionInstanceManager()
      .addRemovalTimeToDecisionsByDecisionInstanceId(instanceId, removalTime);
  }

  protected boolean hasBaseTime(CommandContext commandContext) {
    return isStrategyStart(commandContext) || isStrategyEnd(commandContext);
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

  protected DecisionDefinition findDecisionDefinitionById(String decisionDefinitionId, CommandContext commandContext) {
    return commandContext.getProcessEngineConfiguration()
      .getDeploymentCache()
      .findDeployedDecisionDefinitionById(decisionDefinitionId);
  }

  protected boolean isDmnEnabled(CommandContext commandContext) {
    return commandContext.getProcessEngineConfiguration().isDmnEnabled();
  }

  protected Date calculateRemovalTime(HistoricDecisionInstanceEntity decisionInstance, CommandContext commandContext) {
    DecisionDefinition decisionDefinition = findDecisionDefinitionById(decisionInstance.getDecisionDefinitionId(), commandContext);

    return commandContext.getProcessEngineConfiguration()
      .getHistoryRemovalTimeProvider()
      .calculateRemovalTime(decisionInstance, decisionDefinition);
  }

  protected ByteArrayEntity findByteArrayById(String byteArrayId, CommandContext commandContext) {
    return commandContext.getDbEntityManager()
      .selectById(ByteArrayEntity.class, byteArrayId);
  }

  protected HistoricDecisionInstanceEntity findDecisionInstanceById(String instanceId, CommandContext commandContext) {
    return commandContext.getHistoricDecisionInstanceManager()
      .findHistoricDecisionInstance(instanceId);
  }

  public JobDeclaration<BatchJobContext, MessageEntity> getJobDeclaration() {
    return JOB_DECLARATION;
  }

  protected SetRemovalTimeBatchConfiguration createJobConfiguration(SetRemovalTimeBatchConfiguration configuration, List<String> decisionInstanceIds) {
    return new SetRemovalTimeBatchConfiguration(decisionInstanceIds)
      .setRemovalTime(configuration.getRemovalTime())
      .setHasRemovalTime(configuration.hasRemovalTime())
      .setHierarchical(configuration.isHierarchical());
  }

  protected SetRemovalTimeJsonConverter getJsonConverterInstance() {
    return SetRemovalTimeJsonConverter.INSTANCE;
  }

  public String getType() {
    return Batch.TYPE_DECISION_SET_REMOVAL_TIME;
  }

}
