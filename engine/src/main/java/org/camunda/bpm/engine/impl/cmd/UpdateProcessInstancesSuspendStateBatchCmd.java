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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.authorization.BatchPermissions;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.UpdateProcessInstancesSuspensionStateBuilderImpl;
import org.camunda.bpm.engine.impl.batch.BatchConfiguration;
import org.camunda.bpm.engine.impl.batch.BatchEntity;
import org.camunda.bpm.engine.impl.batch.BatchJobHandler;
import org.camunda.bpm.engine.impl.batch.update.UpdateProcessInstancesSuspendStateBatchConfiguration;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.util.BatchUtil;
import org.camunda.bpm.engine.impl.util.EnsureUtil;

public class UpdateProcessInstancesSuspendStateBatchCmd extends AbstractUpdateProcessInstancesSuspendStateCmd<Batch> {

  public UpdateProcessInstancesSuspendStateBatchCmd(CommandExecutor commandExecutor, UpdateProcessInstancesSuspensionStateBuilderImpl builder, boolean suspending) {
    super(commandExecutor, builder, suspending);
  }

  public Batch execute(CommandContext commandContext) {
    Collection<String> processInstanceIds = collectProcessInstanceIds();

    EnsureUtil.ensureNotEmpty(BadUserRequestException.class, "No process instance ids given", "process Instance Ids", processInstanceIds);
    EnsureUtil.ensureNotContainsNull(BadUserRequestException.class, "Cannot be null.", "Process Instance ids", processInstanceIds);
    checkAuthorizations(commandContext);
    writeUserOperationLog(commandContext, processInstanceIds.size(), true);
    BatchEntity batch = createBatch(commandContext, processInstanceIds);

    batch.createSeedJobDefinition();
    batch.createMonitorJobDefinition();
    batch.createBatchJobDefinition();

    batch.fireHistoricStartEvent();

    batch.createSeedJob();
    return batch;
  }

  protected BatchEntity createBatch(CommandContext commandContext, Collection<String> processInstanceIds) {

    ProcessEngineConfigurationImpl processEngineConfiguration = commandContext.getProcessEngineConfiguration();
    BatchJobHandler batchJobHandler = getBatchJobHandler(processEngineConfiguration);

    BatchConfiguration configuration = getAbstractIdsBatchConfiguration(new ArrayList<String>(processInstanceIds));

    BatchEntity batch = new BatchEntity();

    batch.setType(batchJobHandler.getType());
    batch.setTotalJobs(BatchUtil.calculateBatchSize(processEngineConfiguration, (UpdateProcessInstancesSuspendStateBatchConfiguration) configuration));
    batch.setBatchJobsPerSeed(processEngineConfiguration.getBatchJobsPerSeed());
    batch.setInvocationsPerBatchJob(processEngineConfiguration.getInvocationsPerBatchJob());
    batch.setConfigurationBytes(batchJobHandler.writeConfiguration(configuration));
    commandContext.getBatchManager().insertBatch(batch);

    return batch;
  }

  protected BatchConfiguration getAbstractIdsBatchConfiguration(List<String> processInstanceIds) {
    return new UpdateProcessInstancesSuspendStateBatchConfiguration(processInstanceIds, suspending);
  }

  protected BatchJobHandler<UpdateProcessInstancesSuspendStateBatchConfiguration> getBatchJobHandler(ProcessEngineConfigurationImpl processEngineConfiguration) {
    Map<String, BatchJobHandler<?>> batchHandlers = processEngineConfiguration.getBatchHandlers();
    return (BatchJobHandler<UpdateProcessInstancesSuspendStateBatchConfiguration>) batchHandlers.get(Batch.TYPE_PROCESS_INSTANCE_UPDATE_SUSPENSION_STATE);
  }

  protected void checkAuthorizations(CommandContext commandContext) {
    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkCreateBatch(BatchPermissions.CREATE_BATCH_UPDATE_PROCESS_INSTANCES_SUSPEND);
    }
  }

}
