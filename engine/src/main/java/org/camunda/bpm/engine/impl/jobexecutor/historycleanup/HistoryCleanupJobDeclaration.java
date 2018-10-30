package org.camunda.bpm.engine.impl.jobexecutor.historycleanup;

import java.util.Date;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cmd.CommandLogger;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
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
  }


  @Override
  public EverLivingJobEntity reconfigure(HistoryCleanupContext context, EverLivingJobEntity job) {
    HistoryCleanupJobHandlerConfiguration configuration = resolveJobHandlerConfiguration(context);
    job.setJobHandlerConfiguration(configuration);
    return job;
  }

  @Override
  protected HistoryCleanupJobHandlerConfiguration resolveJobHandlerConfiguration(HistoryCleanupContext context) {
    HistoryCleanupJobHandlerConfiguration config = new HistoryCleanupJobHandlerConfiguration();
    config.setImmediatelyDue(context.isImmediatelyDue());
    config.setMinuteFrom(context.getMinuteFrom());
    config.setMinuteTo(context.getMinuteTo());
    return config;
  }

  @Override
  public Date resolveDueDate(HistoryCleanupContext context) {
    return resolveDueDate(context.isImmediatelyDue());
  }

  private Date resolveDueDate(boolean isImmediatelyDue) {
    CommandContext commandContext = Context.getCommandContext();
    if (isImmediatelyDue) {
      return ClockUtil.getCurrentTime();
    } else {
      final BatchWindow currentOrNextBatchWindow = commandContext.getProcessEngineConfiguration().getBatchWindowManager()
        .getCurrentOrNextBatchWindow(ClockUtil.getCurrentTime(), commandContext.getProcessEngineConfiguration());
      if (currentOrNextBatchWindow != null) {
        return currentOrNextBatchWindow.getStart();
      } else {
        return null;
      }
    }
  }
}
