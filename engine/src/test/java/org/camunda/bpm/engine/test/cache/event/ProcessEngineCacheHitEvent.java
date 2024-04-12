package org.camunda.bpm.engine.test.cache.event;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;

import static org.camunda.bpm.engine.test.cache.event.ProcessEngineEvent.Type.CACHE_HIT;

public class ProcessEngineCacheHitEvent implements ProcessEngineEvent {

    private final ProcessEngine engine;
    private final ProcessEngineConfiguration config;

    public ProcessEngineCacheHitEvent(ProcessEngine engine, ProcessEngineConfiguration config) {
        this.engine = engine;
        this.config = config;
    }

    public ProcessEngine engine() {
        return engine;
    }

    public ProcessEngineConfiguration config() {
        return config;
    }

    @Override
    public Type type() {
        return CACHE_HIT;
    }

    @Override
    public String toString() {
        return "ProcessEngineCacheHitEvent [engine=" + engine.getName() + ", config=" + config + "]";
    }
}
