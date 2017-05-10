package org.camunda.bpm.engine.impl.jobexecutor.historycleanup;

import java.util.ArrayList;
import java.util.List;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

/**
 * Batch of work for history cleanup.
 * @author Svetlana Dorokhova.
 */
public class HistoryCleanupBatch {

  private List<String> historicProcessInstanceIds = new ArrayList<String>();
  private List<String> historicDecisionInstanceIds = new ArrayList<String>();
  private List<String> historicCaseInstanceIds = new ArrayList<String>();

  public List<String> getHistoricProcessInstanceIds() {
    return historicProcessInstanceIds;
  }

  public void setHistoricProcessInstanceIds(List<String> historicProcessInstanceIds) {
    if (historicProcessInstanceIds != null) {
      this.historicProcessInstanceIds = historicProcessInstanceIds;
    } else {
      this.historicProcessInstanceIds = new ArrayList<String>();
    }
  }

  public List<String> getHistoricDecisionInstanceIds() {
    return historicDecisionInstanceIds;
  }

  public void setHistoricDecisionInstanceIds(List<String> historicDecisionInstanceIds) {
    if (historicDecisionInstanceIds != null) {
      this.historicDecisionInstanceIds = historicDecisionInstanceIds;
    } else {
      this.historicDecisionInstanceIds = new ArrayList<String>();
    }
  }

  public List<String> getHistoricCaseInstanceIds() {
    return historicCaseInstanceIds;
  }

  public void setHistoricCaseInstanceIds(List<String> historicCaseInstanceIds) {
    if (historicCaseInstanceIds != null) {
      this.historicCaseInstanceIds = historicCaseInstanceIds;
    } else {
      this.historicCaseInstanceIds = new ArrayList<String>();
    }
  }

  /**
   * Size of the batch.
   */
  public int size() {
    return historicProcessInstanceIds.size() + historicDecisionInstanceIds.size() + + historicCaseInstanceIds.size();
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
