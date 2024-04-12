package org.camunda.bpm.engine.test.cache.listener;

import org.camunda.bpm.engine.test.cache.TestContext;
import org.camunda.bpm.engine.test.cache.event.ProcessEngineEvent;

import java.util.EnumSet;

import static org.camunda.bpm.engine.test.cache.event.ProcessEngineEvent.Type.CACHE_HIT;
import static org.camunda.bpm.engine.test.cache.event.ProcessEngineEvent.Type.CREATED;

public class ProcessEngineEventAssignConfigListener implements ProcessEngineEventListener {

    private static final EnumSet<ProcessEngineEvent.Type> ALLOWED_EVENTS = EnumSet.of(CREATED, CACHE_HIT);

    @Override
    public void onEvent(ProcessEngineEvent event) {
        if (isNotAllowed(event)) {
            return;
        }

        TestContext.getInstance()
                .setExecutionConfig(event.config());
    }

    private boolean isNotAllowed(ProcessEngineEvent event) {
        return !ALLOWED_EVENTS.contains(event.type());
    }
}
