package org.camunda.bpm.engine.test.cache.event;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;

public interface ProcessEngineEvent {

    ProcessEngine engine();
    Type type();

    enum Type {
        CREATED, CACHE_HIT, CUSTOM_ENGINE_CONFIG
    }
}