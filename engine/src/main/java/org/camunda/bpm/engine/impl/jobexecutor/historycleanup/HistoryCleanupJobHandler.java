package org.camunda.bpm.engine.impl.jobexecutor.historycleanup;

import java.util.Date;
import java.util.List;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cmd.CommandLogger;
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

  private final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  public static final String TYPE = "history-cleanup";

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public void execute(HistoryCleanupJobHandlerConfiguration configuration, ExecutionEntity execution, CommandContext commandContext, String tenantId) {
    //find JobEntity
    JobEntity jobEntity = commandContext.getJobManager().findJobByHandlerType(getType());

    boolean rescheduled = false;

    if (configuration.isExecuteAtOnce() || !configuration.isBatchWindowConfigured() || configuration.isWithinBatchWindow(ClockUtil.getCurrentTime())) {
      //find data to delete
      List<String> processInstanceIds = getProcessInstanceIds(configuration, commandContext);
      if (!processInstanceIds.isEmpty() && processInstanceIds.size() >= configuration.getBatchSizeThreshold()) {
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
        //no data for cleanup was found
        if (configuration.isWithinBatchWindow(ClockUtil.getCurrentTime())) {
          //reschedule after some delay
          Date nextRunDate = configuration.getNextRunWithDelay(ClockUtil.getCurrentTime());
          if (configuration.isWithinBatchWindow(nextRunDate)) {
            commandContext.getJobManager().reschedule(jobEntity, nextRunDate);
            rescheduled = true;
            incrementCountEmptyRuns(configuration, jobEntity);
          }
        }
      }
    }
    if (!rescheduled) {
      if (configuration.isBatchWindowConfigured()) {
        rescheduleRegularCall(configuration, commandContext, jobEntity);
      } else {
        //nothing more to do, suspend the job
        suspendJob(jobEntity);
      }
      cancelCountEmptyRuns(configuration, jobEntity);
    }
  }

  private void rescheduleRegularCall(HistoryCleanupJobHandlerConfiguration configuration, CommandContext commandContext, JobEntity jobEntity) {
    commandContext.getJobManager().reschedule(jobEntity, configuration.getNextRunWithinBatchWindow(ClockUtil.getCurrentTime()));
  }

  private void suspendJob(JobEntity jobEntity) {
    jobEntity.setSuspensionState(SuspensionState.SUSPENDED.getStateCode());
    jobEntity.update();
  }

  private void incrementCountEmptyRuns(HistoryCleanupJobHandlerConfiguration configuration, JobEntity jobEntity) {
    configuration.setCountEmptyRuns(configuration.getCountEmptyRuns() + 1);
    jobEntity.setJobHandlerConfiguration(configuration);
    jobEntity.update();
  }

  private void cancelCountEmptyRuns(HistoryCleanupJobHandlerConfiguration configuration, JobEntity jobEntity) {
    configuration.setCountEmptyRuns(0);
    jobEntity.setJobHandlerConfiguration(configuration);
    jobEntity.update();
  }

  private void insertMetric(CommandContext commandContext) {
    long count = commandContext.getHistoricProcessInstanceManager().findHistoricProcessInstanceIdsForCleanupCount();
    commandContext.getProcessEngineConfiguration().getMetricsRegistry().markValue(Metrics.HISTORIC_PROCESS_INSTANCES_FOR_CLEANUP, count);
    commandContext.getProcessEngineConfiguration().getDbMetricsReporter().reportNow();
  }

  private List<String> getProcessInstanceIds(HistoryCleanupJobHandlerConfiguration configuration, CommandContext commandContext) {
    return commandContext.getHistoricProcessInstanceManager().findHistoricProcessInstanceIdsForCleanup(configuration.getBatchSize());
  }

  @Override
  public HistoryCleanupJobHandlerConfiguration newConfiguration(String canonicalString) {
    JSONObject jsonObject = new JSONObject(canonicalString);
    return HistoryCleanupJobHandlerConfiguration.fromJson(jsonObject);
  }

  @Override
  public void onDelete(HistoryCleanupJobHandlerConfiguration configuration, JobEntity jobEntity) {
  }
}
