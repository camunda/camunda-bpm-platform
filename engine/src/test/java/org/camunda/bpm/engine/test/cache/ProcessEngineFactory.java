package org.camunda.bpm.engine.test.cache;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.test.cache.event.ProcessEngineCacheHitEvent;
import org.camunda.bpm.engine.test.cache.event.ProcessEngineCreatedEvent;
import org.camunda.bpm.engine.test.cache.listener.ProcessEngineObserver;
import org.camunda.bpm.engine.test.cache.listener.ProcessEngineObserverFactory;

import java.util.HashMap;
import java.util.Map;

public class ProcessEngineFactory {

    private static final Map<String, ProcessEngine> ENGINE_CACHE = new HashMap<>();
    private static final ProcessEngineObserver OBSERVER = ProcessEngineObserverFactory.getInstance();

    public static ProcessEngine create(ProcessEngineConfigurationImpl engineConfig) {
        if (EnvironmentVariables.enableEngineCache()) {
            return getOrCreate(engineConfig);
        }

        return engineConfig.buildProcessEngine();
    }

    private static ProcessEngine getOrCreate(ProcessEngineConfigurationImpl engineConfig) {
        String name = engineConfig.getProcessEngineName();

        if (!ENGINE_CACHE.containsKey(name)) {
            var engine = engineConfig.buildProcessEngine();
            ENGINE_CACHE.put(name, engine);
            OBSERVER.update(new ProcessEngineCreatedEvent(engine, engineConfig));
            return engine;
        }

        var result = ENGINE_CACHE.get(name);
        OBSERVER.update(new ProcessEngineCacheHitEvent(result, engineConfig));
        return result;
    }
}