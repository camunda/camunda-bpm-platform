package org.camunda.bpm.engine.test.cache.listener;

import org.camunda.bpm.engine.test.cache.TestContext;
import org.camunda.bpm.engine.test.cache.event.ProcessEngineCustomConfigEvent;

public class CustomConfigListener implements ProcessEngineEventListener<ProcessEngineCustomConfigEvent> {

    @Override
    public void onEvent(ProcessEngineCustomConfigEvent event) {
        TestContext.getInstance()
                .setCustomConfig(event.customConfigurator());
    }
}
