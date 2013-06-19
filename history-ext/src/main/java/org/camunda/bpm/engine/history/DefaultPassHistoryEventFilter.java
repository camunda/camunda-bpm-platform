package org.camunda.bpm.engine.history;

import org.camunda.bpm.engine.impl.history.event.HistoryEvent;

/**
 * All {@link HistoryEvent}s can pass.
 * 
 * @author jbellmann
 * 
 */
public final class DefaultPassHistoryEventFilter implements HistoryEventFilter {

  /**
   * Always returns true.
   */
  @Override
  public boolean canPass(HistoryEvent historyEvent) {
    return true;
  }

}
