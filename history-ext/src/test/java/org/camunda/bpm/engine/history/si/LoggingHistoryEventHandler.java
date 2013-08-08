package org.camunda.bpm.engine.history.si;

import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Logs the {@link HistoryEvent}.
 * 
 * @author jbellmann
 * 
 */
public class LoggingHistoryEventHandler implements HistoryEventHandler {

  private static final Logger LOG = LoggerFactory.getLogger(LoggingHistoryEventHandler.class);

  @Override
  public void handleEvent(HistoryEvent historyEvent) {
    LOG.info("LOG-HISTORY : " + historyEvent.toString());
  }

}
