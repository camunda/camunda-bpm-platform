package org.camunda.bpm.engine.history.si;

import org.camunda.bpm.engine.history.HistoryEventMessage;

/**
 * 
 * @author jbellmann
 * 
 * @param <T>
 */
public interface HistoryEventMessageTransformer<T> {

  T transformFromHistoryEventMessage(HistoryEventMessage historyEventMessage);

  HistoryEventMessage transformToHistoryEventMessage(String source);

}
