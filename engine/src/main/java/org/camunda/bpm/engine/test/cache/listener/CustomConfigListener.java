package org.camunda.bpm.engine.test.cache.listener;

import org.camunda.bpm.engine.test.cache.TestContext;
import org.camunda.bpm.engine.test.cache.event.ProcessEngineCustomConfigEvent;
import org.camunda.bpm.engine.test.cache.event.ProcessEngineEvent;

import java.util.EnumSet;

import static org.camunda.bpm.engine.test.cache.event.ProcessEngineEvent.Type.*;

public class CustomConfigListener implements ProcessEngineEventListener {

    private static final EnumSet<ProcessEngineEvent.Type> ALLOWED_EVENTS = EnumSet.of(CUSTOM_ENGINE_CONFIG);

    @Override
    public void onEvent(ProcessEngineEvent event) {
        if (isNotAllowed(event)) {
            return;
        }

        var e = (ProcessEngineCustomConfigEvent) event;

        TestContext.getInstance()
                .setCustomConfig(e.customConfigurator());
    }

    private boolean isNotAllowed(ProcessEngineEvent event) {
        return !ALLOWED_EVENTS.contains(event.type());
    }
}
