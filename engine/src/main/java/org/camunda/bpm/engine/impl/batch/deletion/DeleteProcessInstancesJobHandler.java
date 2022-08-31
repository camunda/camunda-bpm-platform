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
package org.camunda.bpm.engine.impl.batch.deletion;

import java.util.HashSet;
import java.util.List;

import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.ProcessInstanceQueryImpl;
import org.camunda.bpm.engine.impl.batch.AbstractBatchJobHandler;
import org.camunda.bpm.engine.impl.batch.BatchEntity;
import org.camunda.bpm.engine.impl.batch.BatchJobContext;
import org.camunda.bpm.engine.impl.batch.BatchJobDeclaration;
import org.camunda.bpm.engine.impl.batch.BatchElementConfiguration;
import org.camunda.bpm.engine.impl.cmd.DeleteProcessInstancesCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobDeclaration;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;

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
    return new DeleteProcessInstanceBatchConfiguration(processIdsForJob, null, configuration.getDeleteReason(), configuration.isSkipCustomListeners(), configuration.isSkipSubprocesses(), configuration.isFailIfNotExists());
  }

  @Override
  public void executeHandler(DeleteProcessInstanceBatchConfiguration batchConfiguration,
                             ExecutionEntity execution,
                             CommandContext commandContext,
                             String tenantId) {

    commandContext.executeWithOperationLogPrevented(
        new DeleteProcessInstancesCmd(
            batchConfiguration.getIds(),
            batchConfiguration.getDeleteReason(),
            batchConfiguration.isSkipCustomListeners(),
            true,
            batchConfiguration.isSkipSubprocesses(),
            batchConfiguration.isFailIfNotExists()));
  }

  @Override
  protected void createJobEntities(BatchEntity batch, DeleteProcessInstanceBatchConfiguration configuration, String deploymentId, List<String> processIds,
      int invocationsPerBatchJob) {
    // handle legacy batch entities (no up-front deployment mapping has been done)
    if (deploymentId == null && (configuration.getIdMappings() == null || configuration.getIdMappings().isEmpty())) {
      // create deployment mappings for the ids to process
      BatchElementConfiguration elementConfiguration = new BatchElementConfiguration();
      ProcessInstanceQueryImpl query = new ProcessInstanceQueryImpl();
      query.processInstanceIds(new HashSet<>(configuration.getIds()));
      elementConfiguration.addDeploymentMappings(query.listDeploymentIdMappings(), configuration.getIds());
      // create jobs by deployment id
      elementConfiguration.getMappings().forEach(mapping -> super.createJobEntities(batch, configuration, mapping.getDeploymentId(),
          mapping.getIds(processIds), invocationsPerBatchJob));
    } else {
      super.createJobEntities(batch, configuration, deploymentId, processIds, invocationsPerBatchJob);
    }
  }
}
