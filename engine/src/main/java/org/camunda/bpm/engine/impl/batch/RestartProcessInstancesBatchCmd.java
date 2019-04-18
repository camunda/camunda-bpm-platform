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
package org.camunda.bpm.engine.impl.batch;

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
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.RestartProcessInstanceBuilderImpl;
import org.camunda.bpm.engine.impl.RestartProcessInstancesBatchConfiguration;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.AbstractProcessInstanceModificationCommand;
import org.camunda.bpm.engine.impl.cmd.AbstractRestartProcessInstanceCmd;
import org.camunda.bpm.engine.impl.cmd.CommandLogger;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.util.BatchUtil;

/**
 *
 * @author Anna Pazola
 *
 */
public class RestartProcessInstancesBatchCmd extends AbstractRestartProcessInstanceCmd<Batch>{

  private final CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  public RestartProcessInstancesBatchCmd(CommandExecutor commandExecutor, RestartProcessInstanceBuilderImpl builder) {
    super(commandExecutor, builder);
  }

  @Override
  public Batch execute(CommandContext commandContext) {
    List<AbstractProcessInstanceModificationCommand> instructions = builder.getInstructions();
    Collection<String> processInstanceIds = collectProcessInstanceIds();

    ensureNotEmpty(BadUserRequestException.class, "Restart instructions cannot be empty", "instructions", instructions);
    ensureNotEmpty(BadUserRequestException.class, "Process instance ids cannot be empty", "processInstanceIds", processInstanceIds);
    ensureNotContainsNull(BadUserRequestException.class, "Process instance ids cannot be null", "processInstanceIds", processInstanceIds);

    checkPermissions(commandContext);
    ProcessDefinitionEntity processDefinition = getProcessDefinition(commandContext, builder.getProcessDefinitionId());;

    ensureNotNull(BadUserRequestException.class, "Process definition cannot be null", processDefinition);
    ensureTenantAuthorized(commandContext, processDefinition);

    writeUserOperationLog(commandContext, processDefinition, processInstanceIds.size(), true);

    ArrayList<String> ids = new ArrayList<String>();
    ids.addAll(processInstanceIds);
    BatchEntity batch = createBatch(commandContext, instructions, ids, processDefinition);
    batch.createSeedJobDefinition();
    batch.createMonitorJobDefinition();
    batch.createBatchJobDefinition();

    batch.fireHistoricStartEvent();

    batch.createSeedJob();
    return batch;

  }

  protected void checkPermissions(CommandContext commandContext) {
    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkCreateBatch(BatchPermissions.CREATE_BATCH_RESTART_PROCESS_INSTANCES);
    }
  }

  protected BatchEntity createBatch(CommandContext commandContext, List<AbstractProcessInstanceModificationCommand> instructions,
      List<String> processInstanceIds, ProcessDefinitionEntity processDefinition) {
    ProcessEngineConfigurationImpl processEngineConfiguration = commandContext.getProcessEngineConfiguration();
    BatchJobHandler<RestartProcessInstancesBatchConfiguration> batchJobHandler = getBatchJobHandler(processEngineConfiguration);

    RestartProcessInstancesBatchConfiguration configuration = new RestartProcessInstancesBatchConfiguration(
        processInstanceIds, instructions, builder.getProcessDefinitionId(), builder.isInitialVariables(), builder.isSkipCustomListeners(), builder.isSkipIoMappings(), builder.isWithoutBusinessKey());

    BatchEntity batch = new BatchEntity();
    batch.setType(batchJobHandler.getType());
    batch.setTotalJobs(BatchUtil.calculateBatchSize(processEngineConfiguration, configuration));
    batch.setBatchJobsPerSeed(processEngineConfiguration.getBatchJobsPerSeed());
    batch.setInvocationsPerBatchJob(processEngineConfiguration.getInvocationsPerBatchJob());
    batch.setConfigurationBytes(batchJobHandler.writeConfiguration(configuration));
    batch.setTenantId(processDefinition.getTenantId());
    commandContext.getBatchManager().insertBatch(batch);

    return batch;
  }

  @SuppressWarnings("unchecked")
  protected BatchJobHandler<RestartProcessInstancesBatchConfiguration> getBatchJobHandler(ProcessEngineConfigurationImpl processEngineConfiguration) {
    Map<String, BatchJobHandler<?>> batchHandlers = processEngineConfiguration.getBatchHandlers();
    return (BatchJobHandler<RestartProcessInstancesBatchConfiguration>) batchHandlers.get(Batch.TYPE_PROCESS_INSTANCE_RESTART);
  }

  protected void ensureTenantAuthorized(CommandContext commandContext, ProcessDefinitionEntity processDefinition) {
    if (!commandContext.getTenantManager().isAuthenticatedTenant(processDefinition.getTenantId())) {
      throw LOG.exceptionCommandWithUnauthorizedTenant("restart process instances of process definition '" + processDefinition.getId() + "'");
    }
  }
}
