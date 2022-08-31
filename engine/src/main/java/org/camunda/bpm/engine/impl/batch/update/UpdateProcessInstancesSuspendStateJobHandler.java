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
package org.camunda.bpm.engine.impl.batch.update;

import java.util.List;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.UpdateProcessInstancesSuspensionStateBuilderImpl;
import org.camunda.bpm.engine.impl.batch.AbstractBatchJobHandler;
import org.camunda.bpm.engine.impl.batch.BatchJobContext;
import org.camunda.bpm.engine.impl.batch.BatchJobDeclaration;
import org.camunda.bpm.engine.impl.cmd.UpdateProcessInstancesSuspendStateCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.JobDeclaration;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;

public class UpdateProcessInstancesSuspendStateJobHandler extends AbstractBatchJobHandler<UpdateProcessInstancesSuspendStateBatchConfiguration> {

  public static final BatchJobDeclaration JOB_DECLARATION = new BatchJobDeclaration(Batch.TYPE_PROCESS_INSTANCE_UPDATE_SUSPENSION_STATE);

  @Override
  public String getType() {
    return Batch.TYPE_PROCESS_INSTANCE_UPDATE_SUSPENSION_STATE;
  }

  protected UpdateProcessInstancesSuspendStateBatchConfigurationJsonConverter getJsonConverterInstance() {
    return UpdateProcessInstancesSuspendStateBatchConfigurationJsonConverter.INSTANCE;
  }

  @Override
  public JobDeclaration<BatchJobContext, MessageEntity> getJobDeclaration() {
    return JOB_DECLARATION;
  }

  @Override
  protected UpdateProcessInstancesSuspendStateBatchConfiguration createJobConfiguration(UpdateProcessInstancesSuspendStateBatchConfiguration configuration, List<String> processIdsForJob) {
    return new UpdateProcessInstancesSuspendStateBatchConfiguration(processIdsForJob, configuration.getSuspended());
  }

  @Override
  public void executeHandler(UpdateProcessInstancesSuspendStateBatchConfiguration batchConfiguration,
                             ExecutionEntity execution,
                             CommandContext commandContext,
                             String tenantId) {

    CommandExecutor commandExecutor = commandContext.getProcessEngineConfiguration()
        .getCommandExecutorTxRequired();
    commandContext.executeWithOperationLogPrevented(new UpdateProcessInstancesSuspendStateCmd(
        commandExecutor,
        new UpdateProcessInstancesSuspensionStateBuilderImpl(batchConfiguration.getIds()),
        batchConfiguration.getSuspended()));
  }

}
