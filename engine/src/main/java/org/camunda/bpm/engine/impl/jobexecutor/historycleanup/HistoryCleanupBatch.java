package org.camunda.bpm.engine.impl.jobexecutor.historycleanup;

import java.util.Collections;
import java.util.List;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

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

  /**
   * Size of the batch.
   */
  public int size() {
    return historicProcessInstanceIds.size() + historicDecisionInstanceIds.size() + historicCaseInstanceIds.size();
  }

  public void performCleanup() {
    final CommandContext commandContext = Context.getCommandContext();
    if (historicProcessInstanceIds.size() > 0) {
      commandContext.getHistoricProcessInstanceManager().deleteHistoricProcessInstanceByIds(historicProcessInstanceIds);
    }
    if (historicDecisionInstanceIds.size() > 0) {
      commandContext.getHistoricDecisionInstanceManager().deleteHistoricDecisionInstanceByIds(historicDecisionInstanceIds);
    }
    if (historicCaseInstanceIds.size() > 0){
      commandContext.getHistoricCaseInstanceManager().deleteHistoricCaseInstancesByIds(historicCaseInstanceIds);
    }
  }

}
