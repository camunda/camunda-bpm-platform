package org.camunda.bpm.engine.impl.jobexecutor.historycleanup;

/**
 * @author Svetlana Dorokhova.
 */
public class HistoryCleanupContext {

  private boolean immediatelyDue;

  public HistoryCleanupContext(boolean immediatelyDue) {
    this.immediatelyDue = immediatelyDue;
  }

  public boolean isImmediatelyDue() {
    return immediatelyDue;
  }

  public void setImmediatelyDue(boolean immediatelyDue) {
    this.immediatelyDue = immediatelyDue;
  }
}
