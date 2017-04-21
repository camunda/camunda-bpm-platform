package org.camunda.bpm.engine.impl.jobexecutor.historycleanup;

import java.util.Date;
import java.util.List;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cmd.CommandLogger;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobHandler;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.bpm.engine.management.Metrics;

/**
 * Job handler for history cleanup job.
 * @author Svetlana Dorokhova
 */
public class HistoryCleanupJobHandler implements JobHandler<HistoryCleanupJobHandlerConfiguration> {

  public static final String TYPE = "history-cleanup";

  private final static int MAX_BATCH_SIZE = 500;

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public void execute(HistoryCleanupJobHandlerConfiguration configuration, ExecutionEntity execution, CommandContext commandContext, String tenantId) {
    //find JobEntity
    JobEntity jobEntity = commandContext.getJobManager().findJobByHandlerType(getType());

    boolean rescheduled = false;

    if (configuration.isImmediatelyDue() || !HistoryCleanupHelper.isBatchWindowConfigured(commandContext)
        || HistoryCleanupHelper.isWithinBatchWindow(ClockUtil.getCurrentTime(), commandContext)) {
      //find data to delete
      List<String> processInstanceIds = getProcessInstanceIds(commandContext);
      if (!processInstanceIds.isEmpty() && processInstanceIds.size() >= getBatchSizeThreshold(commandContext)) {
        insertMetric(commandContext);

        //delete bunch of data
        commandContext.getHistoricProcessInstanceManager().deleteHistoricProcessInstanceByIds(processInstanceIds);

        //TODO svt does not work as the removal is not flushed to the database yet CAM-7604
        insertMetric(commandContext);

        //reschedule now
        commandContext.getJobManager().reschedule(jobEntity, ClockUtil.getCurrentTime());
        rescheduled = true;
        cancelCountEmptyRuns(configuration, jobEntity);
      } else {
        //still have something to delete
        if (!processInstanceIds.isEmpty()) {
          commandContext.getHistoricProcessInstanceManager().deleteHistoricProcessInstanceByIds(processInstanceIds);
        }
        //not enough data for cleanup was found
        if (HistoryCleanupHelper.isWithinBatchWindow(ClockUtil.getCurrentTime(), commandContext)) {
          //reschedule after some delay
          Date nextRunDate = configuration.getNextRunWithDelay(ClockUtil.getCurrentTime());
          if (HistoryCleanupHelper.isWithinBatchWindow(nextRunDate, commandContext)) {
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
    commandContext.getJobManager().reschedule(jobEntity, HistoryCleanupHelper.getNextRunWithinBatchWindow(ClockUtil.getCurrentTime(), commandContext));
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

  private void insertMetric(CommandContext commandContext) {
    long count = commandContext.getHistoricProcessInstanceManager().findHistoricProcessInstanceIdsForCleanupCount();
    commandContext.getProcessEngineConfiguration().getMetricsRegistry().markValue(Metrics.HISTORIC_PROCESS_INSTANCES_FOR_CLEANUP, count);
    commandContext.getProcessEngineConfiguration().getDbMetricsReporter().reportNow();
  }

  private List<String> getProcessInstanceIds(CommandContext commandContext) {
    return commandContext.getHistoricProcessInstanceManager().findHistoricProcessInstanceIdsForCleanup(getBatchSize(commandContext));
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

  public Integer getBatchSize(CommandContext commandContext) {
    int historyCleanupBatchSize = commandContext.getProcessEngineConfiguration().getHistoryCleanupBatchSize();
    return Math.min(historyCleanupBatchSize, MAX_BATCH_SIZE);
  }
}
