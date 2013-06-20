package org.camunda.bpm.engine.history.si;

import org.camunda.bpm.engine.history.HistoryEventMessage;

/**
 * Simple Gateway for {@link HistoryEventMessage}s.
 * 
 * @author jbellmann
 * 
 */
public interface HistoryEventMessageGateway {

  void send(HistoryEventMessage historyEventMessage);

}
