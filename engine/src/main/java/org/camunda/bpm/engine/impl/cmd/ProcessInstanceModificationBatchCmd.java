/*
 * Copyright © 2013-2019 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotContainsNull;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.authorization.BatchPermissions;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.ModificationBatchConfiguration;
import org.camunda.bpm.engine.impl.ModificationBuilderImpl;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.batch.BatchEntity;
import org.camunda.bpm.engine.impl.batch.BatchJobHandler;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;

public class ProcessInstanceModificationBatchCmd extends AbstractModificationCmd<Batch> {

  protected static final CommandLogger LOGGER = ProcessEngineLogger.CMD_LOGGER;

  public ProcessInstanceModificationBatchCmd(ModificationBuilderImpl modificationBuilderImpl) {
    super(modificationBuilderImpl);
  }

  @Override
  public Batch execute(CommandContext commandContext) {
    List<AbstractProcessInstanceModificationCommand> instructions = builder.getInstructions();
    Collection<String> processInstanceIds = collectProcessInstanceIds(commandContext);

    ensureNotEmpty(BadUserRequestException.class, "Modification instructions cannot be empty", instructions);
    ensureNotEmpty(BadUserRequestException.class, "Process instance ids cannot be empty", "Process instance ids", processInstanceIds);
    ensureNotContainsNull(BadUserRequestException.class, "Process instance ids cannot be null", "Process instance ids", processInstanceIds);

    checkPermissions(commandContext);

    ProcessDefinitionEntity processDefinition = getProcessDefinition(commandContext, builder.getProcessDefinitionId());
    ensureNotNull(BadUserRequestException.class, "Process definition id cannot be null", processDefinition);

    writeUserOperationLog(commandContext, processDefinition,
        processInstanceIds.size(),
        true);

    BatchEntity batch = createBatch(commandContext, instructions, processInstanceIds, processDefinition);
    batch.createSeedJobDefinition();
    batch.createMonitorJobDefinition();
    batch.createBatchJobDefinition();

    batch.fireHistoricStartEvent();

    batch.createSeedJob();
    return batch;
  }

  protected void checkPermissions(CommandContext commandContext) {
    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkCreateBatch(BatchPermissions.CREATE_BATCH_MODIFY_PROCESS_INSTANCES);
    }
  }

  protected BatchEntity createBatch(CommandContext commandContext, List<AbstractProcessInstanceModificationCommand> instructions,
      Collection<String> processInstanceIds, ProcessDefinitionEntity processDefinition) {

    ProcessEngineConfigurationImpl processEngineConfiguration = commandContext.getProcessEngineConfiguration();
    BatchJobHandler<ModificationBatchConfiguration> batchJobHandler = getBatchJobHandler(processEngineConfiguration);

    ModificationBatchConfiguration configuration = new ModificationBatchConfiguration(new ArrayList<String>(processInstanceIds), builder.getProcessDefinitionId(), instructions,
        builder.isSkipCustomListeners(), builder.isSkipIoMappings());

    BatchEntity batch = new BatchEntity();

    batch.setType(batchJobHandler.getType());
    batch.setTotalJobs(calculateSize(processEngineConfiguration, configuration));
    batch.setBatchJobsPerSeed(processEngineConfiguration.getBatchJobsPerSeed());
    batch.setInvocationsPerBatchJob(processEngineConfiguration.getInvocationsPerBatchJob());
    batch.setConfigurationBytes(batchJobHandler.writeConfiguration(configuration));
    batch.setTenantId(processDefinition.getTenantId());
    commandContext.getBatchManager().insertBatch(batch);

    return batch;
  }

  protected int calculateSize(ProcessEngineConfigurationImpl engineConfiguration, ModificationBatchConfiguration batchConfiguration) {
    int invocationsPerBatchJob = engineConfiguration.getInvocationsPerBatchJob();
    int processInstanceCount = batchConfiguration.getIds().size();

    return (int) Math.ceil(processInstanceCount / invocationsPerBatchJob);
  }

  @SuppressWarnings("unchecked")
  protected BatchJobHandler<ModificationBatchConfiguration> getBatchJobHandler(ProcessEngineConfigurationImpl processEngineConfiguration) {
    Map<String, BatchJobHandler<?>> batchHandlers = processEngineConfiguration.getBatchHandlers();
    return (BatchJobHandler<ModificationBatchConfiguration>) batchHandlers.get(Batch.TYPE_PROCESS_INSTANCE_MODIFICATION);
  }

}
