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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.camunda.bpm.engine.impl.batch.BatchJobContext;
import org.camunda.bpm.engine.impl.cfg.TransactionListener;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;
import org.camunda.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;

public class ProcessSetRemovalTimeResultHandler implements TransactionListener {

  protected SetRemovalTimeBatchConfiguration batchJobConfiguration;
  protected Integer chunkSize;
  protected CommandExecutor commandExecutor;
  protected ProcessSetRemovalTimeJobHandler jobHandler;
  protected String jobId;
  protected Map<Class<? extends DbEntity>, DbOperation> operations;

  public ProcessSetRemovalTimeResultHandler(SetRemovalTimeBatchConfiguration batchJobConfiguration,
      Integer chunkSize,
      CommandExecutor commandExecutor,
      ProcessSetRemovalTimeJobHandler jobHandler,
      String jobId,
      Map<Class<? extends DbEntity>, DbOperation> operations) {
    this.batchJobConfiguration = batchJobConfiguration;
    this.chunkSize = chunkSize;
    this.commandExecutor = commandExecutor;
    this.jobHandler = jobHandler;
    this.jobId = jobId;
    this.operations = operations;
  }

  @Override
  public void execute(CommandContext commandContext) {
    // use the new command executor since the command context might already have been closed/finished
    commandExecutor.execute(context -> {
        JobEntity job = context.getJobManager().findJobById(jobId);
        Set<String> entitiesToUpdate = getEntitiesToUpdate(operations, chunkSize);
        if (entitiesToUpdate.isEmpty() && !operations.containsKey(HistoricProcessInstanceEventEntity.class)) {
          // update the process instance last to avoid orphans
          entitiesToUpdate = new HashSet<>();
          entitiesToUpdate.add(HistoricProcessInstanceEventEntity.class.getName());
        }
        if (entitiesToUpdate.isEmpty()) {
          job.delete(true);
        } else {
          // save batch job configuration
          batchJobConfiguration.setEntities(entitiesToUpdate);
          ByteArrayEntity newConfiguration = saveConfiguration(batchJobConfiguration, context);
          BatchJobContext newBatchContext = new BatchJobContext(null, newConfiguration);
          ProcessSetRemovalTimeJobHandler.JOB_DECLARATION.reconfigure(newBatchContext, (MessageEntity) job);
          // reschedule job
          context.getJobManager().reschedule(job, ClockUtil.getCurrentTime());
        }
        return null;
    });
  }

  protected ByteArrayEntity saveConfiguration(SetRemovalTimeBatchConfiguration configuration, CommandContext context) {
    ByteArrayEntity configurationEntity = new ByteArrayEntity();
    configurationEntity.setBytes(jobHandler.writeConfiguration(configuration));
    context.getByteArrayManager().insert(configurationEntity);
    return configurationEntity;
  }

  protected static Set<String> getEntitiesToUpdate(Map<Class<? extends DbEntity>, DbOperation> operations, int chunkSize) {
    return operations.entrySet().stream()
        .filter(op -> op.getValue().getRowsAffected() == chunkSize)
        .map(op -> op.getKey().getName())
        .collect(Collectors.toSet());
  }
}
