package org.camunda.bpm.engine.history.si;

import org.camunda.bpm.engine.history.HistoryEventMessage;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.integration.core.MessageHandler;
import org.springframework.util.Assert;

/**
 * 
 * Handles incoming {@link HistoryEventMessage}s.
 * 
 * @author jbellmann
 * 
 */
public class HistoryEventMessageMessageHandler implements MessageHandler {

  private HistoryEventHandler historyEventHandler;

  @Override
  public void handleMessage(Message<?> message) throws MessagingException {

    HistoryEventMessage historyEventmessage = (HistoryEventMessage) message.getPayload();
    historyEventHandler.handleEvent(historyEventmessage.getHistoryEvent());
  }

  public HistoryEventMessageMessageHandler() {
  }

  public HistoryEventMessageMessageHandler(HistoryEventHandler historyEventHandler) {
    Assert.notNull(historyEventHandler, "HistoryEventHandler should not be null");
    this.historyEventHandler = historyEventHandler;
  }

  public void setHistoryEventHandler(HistoryEventHandler historyEventHandler) {
    Assert.notNull(historyEventHandler, "HistoryEventHandler should not be null");
    this.historyEventHandler = historyEventHandler;
  }
}
