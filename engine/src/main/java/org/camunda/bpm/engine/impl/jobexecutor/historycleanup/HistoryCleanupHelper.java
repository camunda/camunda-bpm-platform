package org.camunda.bpm.engine.impl.jobexecutor.historycleanup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

/**
 * @author Svetlana Dorokhova.
 */
public abstract class HistoryCleanupHelper {

  private static final SimpleDateFormat TIME_FORMAT_WITHOUT_SECONDS = new SimpleDateFormat("HH:mm");

  public static final SimpleDateFormat TIME_FORMAT_WITHOUT_SECONDS_WITH_TIMEZONE = new SimpleDateFormat("HH:mmZ");

  public static Date getNextRunWithinBatchWindow(Date date, CommandContext commandContext) {
    return getNextRunWithinBatchWindow(date, getBatchWindowStartTime(commandContext));
  }

  public static Date getNextRunWithinBatchWindow(Date date, Date batchWindowStartTime) {
    if (batchWindowStartTime != null) {
      Date todayPossibleRun = updateTime(date, batchWindowStartTime);
      if (todayPossibleRun.after(date)) {
        return todayPossibleRun;
      } else {
        //tomorrow
        return addDays(todayPossibleRun, 1);
      }
    } else {
      throw new ProcessEngineException("Batch window must be configured");
    }
  }

  public static Date getBatchWindowStartTime(CommandContext commandContext) {
    return commandContext.getProcessEngineConfiguration().getHistoryCleanupBatchWindowStartTimeAsDate();
  }

  public static Date getBatchWindowEndTime(CommandContext commandContext) {
    return commandContext.getProcessEngineConfiguration().getHistoryCleanupBatchWindowEndTimeAsDate();
  }

  public static boolean isBatchWindowConfigured(CommandContext commandContext) {
    return getBatchWindowStartTime(commandContext) != null;
  }

  /**
   * Checks if given date is within a batch window. Batch window start time is checked inclusively.
   * @param date
   * @return
   */
  public static boolean isWithinBatchWindow(Date date, CommandContext commandContext) {
    if (isBatchWindowConfigured(commandContext)) {
      Date todaysBatchWindowStartTime = updateTime(date, getBatchWindowStartTime(commandContext));
      Date todaysBatchWindowEndTime = updateTime(date, getBatchWindowEndTime(commandContext));
      if (todaysBatchWindowEndTime.after(todaysBatchWindowStartTime)) {
        //interval is within one day
        return (date.after(todaysBatchWindowStartTime) || date.equals(todaysBatchWindowStartTime)) && date.before(todaysBatchWindowEndTime);
      } else {
        return date.after(todaysBatchWindowStartTime) || date.equals(todaysBatchWindowStartTime) || date.before(todaysBatchWindowEndTime);
      }
    } else {
      return false;
    }
  }

  private static Date updateTime(Date now, Date newTime) {
    Calendar c = Calendar.getInstance();
    c.setTime(now);
    Calendar newTimeCalendar = Calendar.getInstance();
    newTimeCalendar.setTime(newTime);
    c.set(Calendar.HOUR_OF_DAY, newTimeCalendar.get(Calendar.HOUR_OF_DAY));
    c.set(Calendar.MINUTE, newTimeCalendar.get(Calendar.MINUTE));
    c.set(Calendar.SECOND, newTimeCalendar.get(Calendar.SECOND));
    c.set(Calendar.MILLISECOND, newTimeCalendar.get(Calendar.MILLISECOND));
    return c.getTime();
  }

  private static Date addDays(Date date, int amount) {
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    c.add(Calendar.DATE, amount);
    return c.getTime();
  }

  public static Date parseTimeConfiguration(String time) throws ParseException {
    try {
      return TIME_FORMAT_WITHOUT_SECONDS_WITH_TIMEZONE.parse(time);
    } catch (ParseException ex) {
      return TIME_FORMAT_WITHOUT_SECONDS.parse(time);
    }
  }

  private static Integer getHistoryCleanupBatchSize(CommandContext commandContext) {
    return commandContext.getProcessEngineConfiguration().getHistoryCleanupBatchSize();
  }

  /**
   * Creates next batch object for history cleanup. First searches for historic process instances ready for cleanup. If there is still some place left in batch
   * (configured batch size was not reached), searches for historic decision instances and also adds them to the batch.
   * @param commandContext
   * @return
   */
  public static HistoryCleanupBatch getNextBatch(CommandContext commandContext) {
    final Integer batchSize = getHistoryCleanupBatchSize(commandContext);
    HistoryCleanupBatch historyCleanupBatch = new HistoryCleanupBatch();

    //add process instance ids
    final List<String> historicProcessInstanceIds = commandContext.getHistoricProcessInstanceManager()
        .findHistoricProcessInstanceIdsForCleanup(batchSize);
    if (historicProcessInstanceIds.size() > 0) {
      historyCleanupBatch.setHistoricProcessInstanceIds(historicProcessInstanceIds);
    }

    //if batch is not full, add decision instance ids
    if (historyCleanupBatch.size() < batchSize && commandContext.getProcessEngineConfiguration().isDmnEnabled()) {
      final List<String> historicDecisionInstanceIds = commandContext.getHistoricDecisionInstanceManager()
          .findHistoricDecisionInstanceIdsForCleanup(batchSize - historyCleanupBatch.size());
      if (historicDecisionInstanceIds.size() > 0) {
        historyCleanupBatch.setHistoricDecisionInstanceIds(historicDecisionInstanceIds);
      }
    }

    //if batch is not full, add case instance ids
    if (historyCleanupBatch.size() < batchSize && commandContext.getProcessEngineConfiguration().isCmmnEnabled()) {
      final List<String> historicCaseInstanceIds = commandContext.getHistoricCaseInstanceManager()
          .findHistoricCaseInstanceIdsForCleanup(batchSize - historyCleanupBatch.size());
      if (historicCaseInstanceIds.size() > 0) {
        historyCleanupBatch.setHistoricCaseInstanceIds(historicCaseInstanceIds);
      }
    }

    return historyCleanupBatch;
  }
}
