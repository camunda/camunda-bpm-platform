package org.camunda.bpm.engine.impl.cmd;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.batch.BatchEntity;
import org.camunda.bpm.engine.impl.batch.BatchJobHandler;
import org.camunda.bpm.engine.impl.batch.SetRetriesBatchConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

public class SetExternalTasksRetriesBatchCmd extends AbstractSetExternalTaskRetriesCmd<Batch> {

  public SetExternalTasksRetriesBatchCmd(UpdateExternalTaskRetriesBuilderImpl builder) {
    super(builder);
  }

  @Override
  public Batch execute(CommandContext commandContext) {
    List<String> externalTaskIds = collectExternalTaskIds();

    ensureNotEmpty(BadUserRequestException.class, "externalTaskIds", externalTaskIds);

    commandContext.getAuthorizationManager().checkAuthorization(Permissions.CREATE, Resources.BATCH);

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

  protected BatchEntity createBatch(CommandContext commandContext, Collection<String> processInstanceIds) {
    ProcessEngineConfigurationImpl processEngineConfiguration = commandContext.getProcessEngineConfiguration();
    BatchJobHandler<SetRetriesBatchConfiguration> batchJobHandler = getBatchJobHandler(processEngineConfiguration);

    SetRetriesBatchConfiguration configuration = new SetRetriesBatchConfiguration(new ArrayList<String>(processInstanceIds), builder.getRetries());

    BatchEntity batch = new BatchEntity();
    batch.setType(batchJobHandler.getType());
    batch.setTotalJobs(calculateSize(processEngineConfiguration, configuration));
    batch.setBatchJobsPerSeed(processEngineConfiguration.getBatchJobsPerSeed());
    batch.setInvocationsPerBatchJob(processEngineConfiguration.getInvocationsPerBatchJob());
    batch.setConfigurationBytes(batchJobHandler.writeConfiguration(configuration));
    commandContext.getBatchManager().insert(batch);

    return batch;
  }

  protected int calculateSize(ProcessEngineConfigurationImpl engineConfiguration, SetRetriesBatchConfiguration batchConfiguration) {
    int invocationsPerBatchJob = engineConfiguration.getInvocationsPerBatchJob();
    int processInstanceCount = batchConfiguration.getIds().size();

    return (int) Math.ceil(processInstanceCount / invocationsPerBatchJob);
  }

  protected BatchJobHandler<SetRetriesBatchConfiguration> getBatchJobHandler(ProcessEngineConfigurationImpl processEngineConfiguration) {
    return (BatchJobHandler<SetRetriesBatchConfiguration>) processEngineConfiguration.getBatchHandlers().get(Batch.TYPE_SET_EXTERNAL_TASK_RETRIES);
  }
}
