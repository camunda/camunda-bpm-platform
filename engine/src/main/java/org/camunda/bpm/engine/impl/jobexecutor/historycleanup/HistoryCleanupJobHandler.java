package org.camunda.bpm.engine.impl.jobexecutor.historycleanup;

import java.util.Date;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutorLogger;
import org.camunda.bpm.engine.impl.jobexecutor.JobHandler;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.json.JSONObject;

/**
 * Job handler for history cleanup job.
 * @author Svetlana Dorokhova
 */
public class HistoryCleanupJobHandler implements JobHandler<HistoryCleanupJobHandlerConfiguration> {

  private final static JobExecutorLogger LOG = ProcessEngineLogger.JOB_EXECUTOR_LOGGER;

  public static final String TYPE = "history-cleanup";

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public void execute(HistoryCleanupJobHandlerConfiguration configuration, ExecutionEntity execution, CommandContext commandContext, String tenantId) {
    JobEntity jobEntity = commandContext.getCurrentJob();
    boolean rescheduled = false;

    if (configuration.isImmediatelyDue()
        || (HistoryCleanupHelper.isBatchWindowConfigured(commandContext)
            && HistoryCleanupHelper.isWithinBatchWindow(ClockUtil.getCurrentTime(), commandContext.getProcessEngineConfiguration())) ) {
      //find data to delete
      final HistoryCleanupBatch nextBatch = HistoryCleanupHelper.getNextBatch(commandContext, configuration);
      if (nextBatch.size() >= getBatchSizeThreshold(commandContext)) {

        //delete bunch of data
        nextBatch.performCleanup();

        //reschedule now
        commandContext.getJobManager().reschedule(jobEntity, ClockUtil.getCurrentTime());
        rescheduled = true;
        cancelCountEmptyRuns(configuration, jobEntity);
      } else {
        //still have something to delete
        if (nextBatch.size() > 0) {
          nextBatch.performCleanup();
        }
        //not enough data for cleanup was found
        if (HistoryCleanupHelper.isWithinBatchWindow(ClockUtil.getCurrentTime(), commandContext.getProcessEngineConfiguration())) {
          //reschedule after some delay
          Date nextRunDate = configuration.getNextRunWithDelay(ClockUtil.getCurrentTime());
          if (HistoryCleanupHelper.isWithinBatchWindow(nextRunDate, commandContext.getProcessEngineConfiguration())) {
            commandContext.getJobManager().reschedule(jobEntity, nextRunDate);
            rescheduled = true;
            incrementCountEmptyRuns(configuration, jobEntity);
          }
        }
      }
    }
    if (!rescheduled) {
      if (HistoryCleanupHelper.isBatchWindowConfigured(commandContext)) {
        rescheduleRegularCall(commandContext, jobEntity);
      } else {
        //nothing more to do, suspend the job
        suspendJob(jobEntity);
      }
      cancelCountEmptyRuns(configuration, jobEntity);
    }
  }

  private void rescheduleRegularCall(CommandContext commandContext, JobEntity jobEntity) {
    final BatchWindow nextBatchWindow = commandContext.getProcessEngineConfiguration().getBatchWindowManager()
      .getNextBatchWindow(ClockUtil.getCurrentTime(), commandContext.getProcessEngineConfiguration());
    if (nextBatchWindow != null) {
      commandContext.getJobManager().reschedule(jobEntity, nextBatchWindow.getStart());
    } else {
      LOG.warnHistoryCleanupBatchWindowNotFound();
      suspendJob(jobEntity);
    }
  }

  private void suspendJob(JobEntity jobEntity) {
    jobEntity.setSuspensionState(SuspensionState.SUSPENDED.getStateCode());
  }

  private void incrementCountEmptyRuns(HistoryCleanupJobHandlerConfiguration configuration, JobEntity jobEntity) {
    configuration.setCountEmptyRuns(configuration.getCountEmptyRuns() + 1);
    jobEntity.setJobHandlerConfiguration(configuration);
  }

  private void cancelCountEmptyRuns(HistoryCleanupJobHandlerConfiguration configuration, JobEntity jobEntity) {
    configuration.setCountEmptyRuns(0);
    jobEntity.setJobHandlerConfiguration(configuration);
  }

  @Override
  public HistoryCleanupJobHandlerConfiguration newConfiguration(String canonicalString) {
    JSONObject jsonObject = new JSONObject(canonicalString);
    return HistoryCleanupJobHandlerConfiguration.fromJson(jsonObject);
  }

  @Override
  public void onDelete(HistoryCleanupJobHandlerConfiguration configuration, JobEntity jobEntity) {
  }

  public Integer getBatchSizeThreshold(CommandContext commandContext) {
    return commandContext.getProcessEngineConfiguration().getHistoryCleanupBatchThreshold();
  }

}
