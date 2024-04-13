package org.camunda.bpm.engine.test.cache.listener;

import org.camunda.bpm.engine.test.cache.TestContext;
import org.camunda.bpm.engine.test.cache.event.ProcessEngineCreatedEvent;

public class CreatedEventListener implements ProcessEngineEventListener<ProcessEngineCreatedEvent> {

    @Override
    public void onEvent(ProcessEngineCreatedEvent event) {
        TestContext.getInstance()
                .setExecutionConfig(event.config());
    }
}
