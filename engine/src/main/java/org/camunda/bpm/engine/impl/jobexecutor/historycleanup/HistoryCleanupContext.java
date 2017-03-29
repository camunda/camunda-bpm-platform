package org.camunda.bpm.engine.impl.jobexecutor.historycleanup;

/**
 * @author Svetlana Dorokhova.
 */
public class HistoryCleanupContext {

  private boolean executeAtOnce;

  public HistoryCleanupContext(boolean executeAtOnce) {
    this.executeAtOnce = executeAtOnce;
  }

  public boolean isExecuteAtOnce() {
    return executeAtOnce;
  }

  public void setExecuteAtOnce(boolean executeAtOnce) {
    this.executeAtOnce = executeAtOnce;
  }
}
