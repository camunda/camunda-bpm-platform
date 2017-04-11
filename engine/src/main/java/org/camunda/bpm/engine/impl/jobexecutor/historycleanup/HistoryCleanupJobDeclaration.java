package org.camunda.bpm.engine.impl.jobexecutor.historycleanup;

import java.text.ParseException;
import java.util.Date;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.CommandLogger;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.jobexecutor.JobDeclaration;
import org.camunda.bpm.engine.impl.persistence.entity.EverLivingJobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;

/**
 * Job declaration for history cleanup.
 * @author Svetlana Dorokhova
 */
public class HistoryCleanupJobDeclaration extends JobDeclaration<HistoryCleanupContext, EverLivingJobEntity> {

  private final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  public HistoryCleanupJobDeclaration() {
    super(HistoryCleanupJobHandler.TYPE);
  }

  @Override
  protected ExecutionEntity resolveExecution(HistoryCleanupContext context) {
    return null;
  }

  @Override
  protected EverLivingJobEntity newJobInstance(HistoryCleanupContext context) {
    return new EverLivingJobEntity();
  }

  @Override
  protected void postInitialize(HistoryCleanupContext context, EverLivingJobEntity job) {
    updateDueDateFromConfiguration(job);
  }

  private void updateDueDateFromConfiguration(EverLivingJobEntity job) {
    //set due date
    HistoryCleanupJobHandlerConfiguration configuration = (HistoryCleanupJobHandlerConfiguration)job.getJobHandlerConfiguration();
    if (configuration.isExecuteAtOnce() || !configuration.isBatchWindowConfigured()) {
      job.setDuedate(ClockUtil.getCurrentTime());
    } else {
      job.setDuedate(configuration.getNextRunWithinBatchWindow(ClockUtil.getCurrentTime()));
    }
  }

  @Override
  public EverLivingJobEntity reconfigure(HistoryCleanupContext context, EverLivingJobEntity job) {
    HistoryCleanupJobHandlerConfiguration configuration = resolveJobHandlerConfiguration(context);
    job.setJobHandlerConfiguration(configuration);
    updateDueDateFromConfiguration(job);
    job.update();
    return job;
  }

  @Override
  protected HistoryCleanupJobHandlerConfiguration resolveJobHandlerConfiguration(HistoryCleanupContext context) {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getCommandContext().getProcessEngineConfiguration();
    Date batchWindowStartTime = null;
    if (processEngineConfiguration.getBatchWindowStartTime() != null) {
      try {
        batchWindowStartTime = HistoryCleanupJobHandlerConfiguration.parseTimeConfiguration(processEngineConfiguration.getBatchWindowStartTime());
      } catch (ParseException e) {
        LOG.warnWrongConfiguration("batchWindowStartTime");
      }
    }
    Date batchWindowEndTime = null;
    if (processEngineConfiguration.getBatchWindowEndTime() != null) {
      try {
        batchWindowEndTime = HistoryCleanupJobHandlerConfiguration.parseTimeConfiguration(processEngineConfiguration.getBatchWindowEndTime());
      } catch (ParseException e) {
        LOG.warnWrongConfiguration("batchWindowEndTime");
      }
    }
    HistoryCleanupJobHandlerConfiguration config = new HistoryCleanupJobHandlerConfiguration(batchWindowStartTime,
        batchWindowEndTime, processEngineConfiguration.getHistoryCleanupBatchSize());
    config.setBatchSizeThreshold(processEngineConfiguration.getHistoryCleanupBatchThreshold());
    config.setExecuteAtOnce(context.isExecuteAtOnce());
    return config;
  }
}
