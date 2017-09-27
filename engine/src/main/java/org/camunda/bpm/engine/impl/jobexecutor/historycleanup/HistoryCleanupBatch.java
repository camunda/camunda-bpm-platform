package org.camunda.bpm.engine.impl.jobexecutor.historycleanup;

import java.util.Collections;
import java.util.List;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.management.Metrics;

/**
 * Batch of work for history cleanup.
 * @author Svetlana Dorokhova.
 */
public class HistoryCleanupBatch {

  /**
   * Maximum allowed batch size.
   */
  public final static int MAX_BATCH_SIZE = 500;

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
    final CommandContext commandContext = Context.getCommandContext();
    if (historicProcessInstanceIds.size() > 0) {
      commandContext.getHistoricProcessInstanceManager().deleteHistoricProcessInstanceByIds(historicProcessInstanceIds);
      recordValue(Metrics.HISTORY_CLEANUP_REMOVED_PROCESS_INSTANCES, historicProcessInstanceIds.size());
    }
    if (historicDecisionInstanceIds.size() > 0) {
      commandContext.getHistoricDecisionInstanceManager().deleteHistoricDecisionInstanceByIds(historicDecisionInstanceIds);
      recordValue(Metrics.HISTORY_CLEANUP_REMOVED_DECISION_INSTANCES, historicDecisionInstanceIds.size());
    }
    if (historicCaseInstanceIds.size() > 0) {
      commandContext.getHistoricCaseInstanceManager().deleteHistoricCaseInstancesByIds(historicCaseInstanceIds);
      recordValue(Metrics.HISTORY_CLEANUP_REMOVED_CASE_INSTANCES, historicCaseInstanceIds.size());
    }
    if (historicBatchIds.size() > 0) {
      commandContext.getHistoricBatchManager().deleteHistoricBatchesByIds(historicBatchIds);
      recordValue(Metrics.HISTORY_CLEANUP_REMOVED_BATCH_OPERATIONS, historicBatchIds.size());
    }
  }

  protected void recordValue(String name, long value) {
    final ProcessEngineConfigurationImpl processEngineConfiguration = Context.getCommandContext().getProcessEngineConfiguration();
    if (processEngineConfiguration.isHistoryCleanupMetricsEnabled()) {
      processEngineConfiguration.getDbMetricsReporter().reportValueAtOnce(name, value);
    }
  }

}
