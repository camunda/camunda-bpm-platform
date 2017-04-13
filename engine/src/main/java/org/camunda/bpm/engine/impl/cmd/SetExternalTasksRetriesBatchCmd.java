package org.camunda.bpm.engine.impl.cmd;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.externaltask.ExternalTaskQuery;
import org.camunda.bpm.engine.impl.batch.BatchEntity;
import org.camunda.bpm.engine.impl.batch.BatchJobHandler;
import org.camunda.bpm.engine.impl.batch.externaltask.SetExternalTaskRetriesBatchConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

public class SetExternalTasksRetriesBatchCmd extends AbstractSetExternalTaskRetriesCmd<Batch> {

  protected int retries;
  
  public SetExternalTasksRetriesBatchCmd(List<String> externalTaskIds, ExternalTaskQuery externalTaskQuery, int retries) {
    super(externalTaskIds, externalTaskQuery);
    this.retries = retries;
  }

  @Override
  public Batch execute(CommandContext commandContext) {
    List<String> externalTaskIds = collectExternalTaskIds();

    ensureNotEmpty(BadUserRequestException.class, "externalTaskIds", externalTaskIds);
    
    commandContext.getAuthorizationManager().checkAuthorization(Permissions.CREATE, Resources.BATCH);
    
    writeUserOperationLog(commandContext,
        retries,
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
    BatchJobHandler<SetExternalTaskRetriesBatchConfiguration> batchJobHandler = getBatchJobHandler(processEngineConfiguration);

    SetExternalTaskRetriesBatchConfiguration configuration = new SetExternalTaskRetriesBatchConfiguration(new ArrayList<String>(processInstanceIds), retries);
    
    BatchEntity batch = new BatchEntity();
    batch.setType(batchJobHandler.getType());
    batch.setTotalJobs(calculateSize(processEngineConfiguration, configuration));
    batch.setBatchJobsPerSeed(processEngineConfiguration.getBatchJobsPerSeed());
    batch.setInvocationsPerBatchJob(processEngineConfiguration.getInvocationsPerBatchJob());
    batch.setConfigurationBytes(batchJobHandler.writeConfiguration(configuration));
    commandContext.getBatchManager().insert(batch);

    return batch;
  }
  
  protected int calculateSize(ProcessEngineConfigurationImpl engineConfiguration, SetExternalTaskRetriesBatchConfiguration batchConfiguration) {
    int invocationsPerBatchJob = engineConfiguration.getInvocationsPerBatchJob();
    int processInstanceCount = batchConfiguration.getIds().size();

    return (int) Math.ceil(processInstanceCount / invocationsPerBatchJob);
  }

  protected BatchJobHandler<SetExternalTaskRetriesBatchConfiguration> getBatchJobHandler(ProcessEngineConfigurationImpl processEngineConfiguration) {
    return (BatchJobHandler<SetExternalTaskRetriesBatchConfiguration>) processEngineConfiguration.getBatchHandlers().get(Batch.TYPE_SET_EXTERNAL_TASK_RETRIES);
  }
}
