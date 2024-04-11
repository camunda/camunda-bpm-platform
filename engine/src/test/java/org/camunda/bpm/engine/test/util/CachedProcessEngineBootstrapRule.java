package org.camunda.bpm.engine.test.util;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.junit.runner.Description;

import java.util.HashMap;
import java.util.Map;

public class CachedProcessEngineBootstrapRule extends ProcessEngineBootstrapRule {

    private static final Map<String, ProcessEngine> ENGINE_CACHE = new HashMap<>();

    private static ProcessEngineConfigurationImpl CURRENT_CONFIG;
    private static ObjectChangeTracker<ProcessEngineConfigurationImpl> CONFIG_TRACKER;

    public CachedProcessEngineBootstrapRule() {
        super();
    }

    @Override
    public ProcessEngine buildProcessEngine(ProcessEngineConfigurationImpl engineConfig) {
        CURRENT_CONFIG = engineConfig;

        return getOrCreate(engineConfig);
    }

    @Override
    protected void starting(Description description) {
        // This method will be called before each test method starts
        System.out.println("Starting test: " + description.getMethodName());

        CONFIG_TRACKER = ObjectChangeTracker.of(CURRENT_CONFIG);

        super.starting(description);
    }

    @Override
    protected void finished(Description description) {
        // This method will be called after each test method finishes
        System.out.println("Finishing test: " + description.getMethodName());

        super.finished(description);

        CONFIG_TRACKER.restoreFields();
        CONFIG_TRACKER.clear();
    }

    private static ProcessEngine getOrCreate(ProcessEngineConfigurationImpl engineConfig) {
        String name = engineConfig.getProcessEngineName();

        if (!ENGINE_CACHE.containsKey(name)) {
            var engine = engineConfig.buildProcessEngine();
            System.out.println("Put in the cache engine with name: " + name);
            ENGINE_CACHE.put(name, engine);
            return engine;
        }

        System.out.println("Fetched engine " + name + " from the cache");
        return ENGINE_CACHE.get(name);
    }
}