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
package org.camunda.bpm.engine.impl.batch.variables;

import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.batch.AbstractBatchJobHandler;
import org.camunda.bpm.engine.impl.batch.BatchConfiguration;
import org.camunda.bpm.engine.impl.batch.BatchJobContext;
import org.camunda.bpm.engine.impl.batch.BatchJobDeclaration;
import org.camunda.bpm.engine.impl.cmd.SetExecutionVariablesCmd;
import org.camunda.bpm.engine.impl.core.variable.VariableUtil;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobDeclaration;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;

import java.util.List;
import java.util.Map;

public class BatchSetVariablesHandler extends AbstractBatchJobHandler<BatchConfiguration> {

  public static final BatchJobDeclaration JOB_DECLARATION =
      new BatchJobDeclaration(Batch.TYPE_SET_VARIABLES);

  @Override
  public void executeHandler(BatchConfiguration batchConfiguration,
                             ExecutionEntity execution,
                             CommandContext commandContext,
                             String tenantId) {

    String batchId = batchConfiguration.getBatchId();
    Map<String, ?> variables = VariableUtil.findBatchVariablesSerialized(batchId, commandContext);

    List<String> processInstanceIds = batchConfiguration.getIds();

    for (String processInstanceId : processInstanceIds) {
      commandContext.executeWithOperationLogPrevented(
          new SetExecutionVariablesCmd(processInstanceId, variables, false, true));
    }
  }

  @Override
  public JobDeclaration<BatchJobContext, MessageEntity> getJobDeclaration() {
    return JOB_DECLARATION;
  }

  @Override
  protected BatchConfiguration createJobConfiguration(BatchConfiguration configuration,
                                                      List<String> processIdsForJob) {
    return new BatchConfiguration(processIdsForJob);
  }

  @Override
  protected SetVariablesJsonConverter getJsonConverterInstance() {
    return SetVariablesJsonConverter.INSTANCE;
  }

  @Override
  public String getType() {
    return Batch.TYPE_SET_VARIABLES;
  }

  @Override
  protected void postProcessJob(BatchConfiguration configuration, JobEntity job, BatchConfiguration jobConfiguration) {
    // if there is only one process instance to adjust, set its ID to the job so exclusive scheduling is possible
    if (jobConfiguration.getIds() != null && jobConfiguration.getIds().size() == 1) {
      job.setProcessInstanceId(jobConfiguration.getIds().get(0));
    }
  }

  protected ByteArrayEntity findByteArrayById(String byteArrayId, CommandContext commandContext) {
    return commandContext.getDbEntityManager()
        .selectById(ByteArrayEntity.class, byteArrayId);
  }

}
