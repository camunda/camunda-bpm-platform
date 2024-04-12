package org.camunda.bpm.engine.test.cache.listener;

import org.camunda.bpm.engine.test.cache.event.ProcessEngineEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogListener implements ProcessEngineEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(LogListener.class);

    @Override
    public void onEvent(ProcessEngineEvent event) {
        LOG.info("Process Engine Event: {}", event);
    }
}
