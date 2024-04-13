package org.camunda.bpm.engine.test.cache.listener;

import org.camunda.bpm.engine.test.cache.event.ProcessEngineEvent;
import org.camunda.bpm.engine.test.cache.event.ProcessEngineEvent.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.camunda.bpm.engine.test.cache.event.ProcessEngineEvent.Type.*;

public class ProcessEngineObserver {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessEngineObserver.class);

    private final Map<Type, ProcessEngineEventListener<? extends ProcessEngineEvent>> mappings;

    public ProcessEngineObserver() {
        mappings = new HashMap<>();

        mappings.put(CACHE_HIT, new CacheHitEventListener());
        mappings.put(CREATED, new CreatedEventListener());
        mappings.put(CUSTOM_ENGINE_CONFIG, new CustomConfigListener());
    }

    public void update(ProcessEngineEvent event) {
        LOG.info("Process Engine Event: {}", event);
        ProcessEngineEventListener<ProcessEngineEvent> listener = (ProcessEngineEventListener<ProcessEngineEvent>) mappings.get(event.type());
        listener.onEvent(event);
    }

}
