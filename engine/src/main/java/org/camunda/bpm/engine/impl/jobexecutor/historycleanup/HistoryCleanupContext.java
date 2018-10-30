package org.camunda.bpm.engine.impl.jobexecutor.historycleanup;

/**
 * @author Svetlana Dorokhova.
 */
public class HistoryCleanupContext {

  private boolean immediatelyDue;
  private int minuteFrom;
  private int minuteTo;

  public HistoryCleanupContext(boolean immediatelyDue, int minuteFrom, int minuteTo) {
    this.immediatelyDue = immediatelyDue;
    this.minuteFrom = minuteFrom;
    this.minuteTo = minuteTo;
  }

  public HistoryCleanupContext(int minuteFrom, int minuteTo) {
    this.minuteFrom = minuteFrom;
    this.minuteTo = minuteTo;
  }

  public boolean isImmediatelyDue() {
    return immediatelyDue;
  }

  public void setImmediatelyDue(boolean immediatelyDue) {
    this.immediatelyDue = immediatelyDue;
  }

  public int getMinuteFrom() {
    return minuteFrom;
  }

  public void setMinuteFrom(int minuteFrom) {
    this.minuteFrom = minuteFrom;
  }

  public int getMinuteTo() {
    return minuteTo;
  }

  public void setMinuteTo(int minuteTo) {
    this.minuteTo = minuteTo;
  }
}
