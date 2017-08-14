package org.camunda.bpm.spring.boot.starter.configuration.impl;

import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.configuration.CamundaHistoryConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

public class DefaultHistoryConfiguration extends AbstractCamundaConfiguration implements CamundaHistoryConfiguration {

  @Autowired(required = false)
  protected HistoryEventHandler historyEventHandler;

  @Override
  public void preInit(SpringProcessEngineConfiguration configuration) {
    String historyLevel = camundaBpmProperties.getHistoryLevel();
    if (historyLevel != null) {
      configuration.setHistory(historyLevel);
    }
    if (historyEventHandler != null) {
      logger.debug("registered history event handler: {}", historyEventHandler.getClass());
      configuration.setHistoryEventHandler(historyEventHandler);
    }
  }

}
