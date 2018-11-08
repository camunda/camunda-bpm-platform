package org.camunda.bpm.engine.impl.jobexecutor.historycleanup;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.management.Metrics;

/**
 * Batch of work for history cleanup.
 * @author Svetlana Dorokhova.
 */
public class HistoryCleanupBatch extends HistoryCleanupHandler {

  private List<String> historicProcessInstanceIds = Collections.emptyList();
  private List<String> historicDecisionInstanceIds = Collections.emptyList();
  private List<String> historicCaseInstanceIds = Collections.emptyList();
  private List<String> historicBatchIds = Collections.emptyList();

  public List<String> getHistoricProcessInstanceIds() {
    return historicProcessInstanceIds;
  }

  public void setHistoricProcessInstanceIds(List<String> historicProcessInstanceIds) {
    this.historicProcessInstanceIds = historicProcessInstanceIds;
  }

  public List<String> getHistoricDecisionInstanceIds() {
    return historicDecisionInstanceIds;
  }

  public void setHistoricDecisionInstanceIds(List<String> historicDecisionInstanceIds) {
    this.historicDecisionInstanceIds = historicDecisionInstanceIds;
  }

  public List<String> getHistoricCaseInstanceIds() {
    return historicCaseInstanceIds;
  }

  public void setHistoricCaseInstanceIds(List<String> historicCaseInstanceIds) {
    this.historicCaseInstanceIds = historicCaseInstanceIds;
  }

  public List<String> getHistoricBatchIds() {
    return historicBatchIds;
  }

  public void setHistoricBatchIds(List<String> historicBatchIds) {
    this.historicBatchIds = historicBatchIds;
  }

  /**
   * Size of the batch.
   */
  public int size() {
    return historicProcessInstanceIds.size() + historicDecisionInstanceIds.size() + historicCaseInstanceIds.size() + historicBatchIds.size();
  }

  public void performCleanup() {
    CommandContext commandContext = Context.getCommandContext();
    HistoryCleanupHelper.prepareNextBatch(this, commandContext);

    if (size() > 0) {
      if (historicProcessInstanceIds.size() > 0) {
        commandContext.getHistoricProcessInstanceManager().deleteHistoricProcessInstanceByIds(historicProcessInstanceIds);
      }
      if (historicDecisionInstanceIds.size() > 0) {
        commandContext.getHistoricDecisionInstanceManager().deleteHistoricDecisionInstanceByIds(historicDecisionInstanceIds);
      }
      if (historicCaseInstanceIds.size() > 0) {
        commandContext.getHistoricCaseInstanceManager().deleteHistoricCaseInstancesByIds(historicCaseInstanceIds);
      }
      if (historicBatchIds.size() > 0) {
        commandContext.getHistoricBatchManager().deleteHistoricBatchesByIds(historicBatchIds);
      }
    }
  }

  @Override
  protected Map<String, Long> reportMetrics() {
    Map<String, Long> reports = new HashMap<>();

    if (historicProcessInstanceIds.size() > 0) {
      reports.put(Metrics.HISTORY_CLEANUP_REMOVED_PROCESS_INSTANCES, (long) historicProcessInstanceIds.size());
    }
    if (historicDecisionInstanceIds.size() > 0) {
      reports.put(Metrics.HISTORY_CLEANUP_REMOVED_DECISION_INSTANCES, (long) historicDecisionInstanceIds.size());
    }
    if (historicCaseInstanceIds.size() > 0) {
      reports.put(Metrics.HISTORY_CLEANUP_REMOVED_CASE_INSTANCES, (long) historicCaseInstanceIds.size());
    }
    if (historicBatchIds.size() > 0) {
      reports.put(Metrics.HISTORY_CLEANUP_REMOVED_BATCH_OPERATIONS, (long) historicBatchIds.size());
    }

    return reports;
  }

  @Override
  boolean shouldRescheduleNow() {
    return size() >= getBatchSizeThreshold();
  }

  public Integer getBatchSizeThreshold() {
    return Context
        .getProcessEngineConfiguration()
        .getHistoryCleanupBatchThreshold();
  }

}
