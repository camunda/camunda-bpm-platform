package org.camunda.bpm.engine.test.cache.listener;

import org.camunda.bpm.engine.test.cache.TestContext;
import org.camunda.bpm.engine.test.cache.event.ProcessEngineCacheHitEvent;

public class CacheHitEventListener implements ProcessEngineEventListener<ProcessEngineCacheHitEvent> {

    @Override
    public void onEvent(ProcessEngineCacheHitEvent event) {
        TestContext.getInstance()
                .setExecutionConfig(event.config());
    }
}
