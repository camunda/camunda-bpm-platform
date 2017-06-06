/* Licensed under the Apache License, Version 2.0 (the "License");
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.SuspensionBuilderImpl;
import org.camunda.bpm.engine.impl.batch.BatchConfiguration;
import org.camunda.bpm.engine.impl.batch.BatchEntity;
import org.camunda.bpm.engine.impl.batch.BatchJobHandler;
import org.camunda.bpm.engine.impl.batch.suspension.SuspendProcessInstanceBatchConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;


import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;

public class SuspendProcessInstancesBatchCmd extends AbstractSuspendProcessInstancesCmd<Batch> {

  protected CommandExecutor commandExecutor;
  protected SuspensionBuilderImpl builder;
  protected List<String> processInstanceIds;
  protected boolean suspending;

  public SuspendProcessInstancesBatchCmd(CommandExecutor commandExecutor, SuspensionBuilderImpl builder, boolean suspending) {
    super(commandExecutor, builder);
    this.commandExecutor = commandExecutor;
    this.builder = builder;
    this.suspending = suspending;
  }

  public Batch execute(CommandContext commandContext) {
    Collection<String> processInstanceIds = collectProcessInstanceIds();

    ensureNotEmpty(BadUserRequestException.class, "processInstanceIds", processInstanceIds);
    checkAuthorizations(commandContext);
    writeUserOperationLog(commandContext, processInstanceIds.size(), true, suspending);
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
    batch.setTotalJobs(calculateSize(processEngineConfiguration, (SuspendProcessInstanceBatchConfiguration) configuration));
    batch.setBatchJobsPerSeed(processEngineConfiguration.getBatchJobsPerSeed());
    batch.setInvocationsPerBatchJob(processEngineConfiguration.getInvocationsPerBatchJob());
    batch.setConfigurationBytes(batchJobHandler.writeConfiguration((SuspendProcessInstanceBatchConfiguration)configuration));
    commandContext.getBatchManager().insert(batch);

    return batch;
  }

  protected int calculateSize(ProcessEngineConfigurationImpl engineConfiguration, SuspendProcessInstanceBatchConfiguration batchConfiguration) {
    int invocationsPerBatchJob = engineConfiguration.getInvocationsPerBatchJob();
    int processInstanceCount = batchConfiguration.getIds().size();

    return (int) Math.ceil(processInstanceCount / invocationsPerBatchJob);
  }

  protected BatchConfiguration getAbstractIdsBatchConfiguration(List<String> processInstanceIds) {
    return new SuspendProcessInstanceBatchConfiguration(processInstanceIds, builder.getSuspendState());
  }

  protected BatchJobHandler<SuspendProcessInstanceBatchConfiguration> getBatchJobHandler(ProcessEngineConfigurationImpl processEngineConfiguration) {
    Map<String, BatchJobHandler<?>> batchHandlers = processEngineConfiguration.getBatchHandlers();
    return (BatchJobHandler<SuspendProcessInstanceBatchConfiguration>) batchHandlers.get(Batch.TYPE_PROCESS_INSTANCE_SUSPENSION);
  }

  protected void checkAuthorizations(CommandContext commandContext) {
    commandContext.getAuthorizationManager().checkAuthorization(Permissions.UPDATE_INSTANCE, Resources.BATCH);
  }
}
