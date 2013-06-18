package org.camunda.bpm.engine.history;

import org.camunda.bpm.engine.impl.history.event.HistoryEvent;

/**
 * Filters {@link HistoryEvent} objects.
 * 
 * @author jbellmann
 *
 */
public interface HistoryEventFilter {
  
  /**
   * Returns true if the {@link HistoryEvent} can pass.
   * 
   * @param historyEvent
   * @return
   */
  boolean canPass(HistoryEvent historyEvent);

}
