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
package org.camunda.bpm.engine.impl.cmd;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.authorization.BatchPermissions;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.batch.BatchEntity;
import org.camunda.bpm.engine.impl.batch.BatchJobHandler;
import org.camunda.bpm.engine.impl.batch.SetRetriesBatchConfiguration;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.util.BatchUtil;

public class SetExternalTasksRetriesBatchCmd extends AbstractSetExternalTaskRetriesCmd<Batch> {

  public SetExternalTasksRetriesBatchCmd(UpdateExternalTaskRetriesBuilderImpl builder) {
    super(builder);
  }

  @Override
  public Batch execute(CommandContext commandContext) {
    List<String> externalTaskIds = collectExternalTaskIds();

    ensureNotEmpty(BadUserRequestException.class, "externalTaskIds", externalTaskIds);

    checkPermissions(commandContext);

    writeUserOperationLog(commandContext,
        builder.getRetries(),
        externalTaskIds.size(),
        true);

    BatchEntity batch = createBatch(commandContext, externalTaskIds);

    batch.createSeedJobDefinition();
    batch.createMonitorJobDefinition();
    batch.createBatchJobDefinition();

    batch.fireHistoricStartEvent();

    batch.createSeedJob();

    return batch;
  }

  protected void checkPermissions(CommandContext commandContext) {
    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkCreateBatch(BatchPermissions.CREATE_BATCH_SET_EXTERNAL_TASK_RETRIES);
    }
  }

  protected BatchEntity createBatch(CommandContext commandContext, Collection<String> processInstanceIds) {
    ProcessEngineConfigurationImpl processEngineConfiguration = commandContext.getProcessEngineConfiguration();
    BatchJobHandler<SetRetriesBatchConfiguration> batchJobHandler = getBatchJobHandler(processEngineConfiguration);

    SetRetriesBatchConfiguration configuration = new SetRetriesBatchConfiguration(new ArrayList<String>(processInstanceIds), builder.getRetries());

    BatchEntity batch = new BatchEntity();
    batch.setType(batchJobHandler.getType());
    batch.setTotalJobs(BatchUtil.calculateBatchSize(processEngineConfiguration, configuration));
    batch.setBatchJobsPerSeed(processEngineConfiguration.getBatchJobsPerSeed());
    batch.setInvocationsPerBatchJob(processEngineConfiguration.getInvocationsPerBatchJob());
    batch.setConfigurationBytes(batchJobHandler.writeConfiguration(configuration));
    commandContext.getBatchManager().insertBatch(batch);

    return batch;
  }


  protected BatchJobHandler<SetRetriesBatchConfiguration> getBatchJobHandler(ProcessEngineConfigurationImpl processEngineConfiguration) {
    return (BatchJobHandler<SetRetriesBatchConfiguration>) processEngineConfiguration.getBatchHandlers().get(Batch.TYPE_SET_EXTERNAL_TASK_RETRIES);
  }
}
