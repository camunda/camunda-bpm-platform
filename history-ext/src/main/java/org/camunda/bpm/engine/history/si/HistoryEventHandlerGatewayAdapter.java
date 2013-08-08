package org.camunda.bpm.engine.history.si;

import org.camunda.bpm.engine.history.HistoryEventMessage;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.annotation.Gateway;
import org.springframework.util.Assert;

/**
 * Adapter wraps the {@link HistoryEvent} into an new created
 * {@link HistoryEventMessage} and uses the {@link Gateway} to send it to an
 * {@link MessageChannel}.
 * 
 * @author jbellmann
 * 
 */
public class HistoryEventHandlerGatewayAdapter implements HistoryEventHandler {

  private HistoryEventMessageGateway historyEventMessageGateway;

  @Override
  public void handleEvent(HistoryEvent historyEvent) {
    this.historyEventMessageGateway.send(new HistoryEventMessage(historyEvent));
  }

  public void setHistoryEventMessageGateway(HistoryEventMessageGateway historyEventMessageGateway) {
    Assert.notNull(historyEventMessageGateway, "HistoryEventMessageGateway should never be null");
    this.historyEventMessageGateway = historyEventMessageGateway;
  }
}
