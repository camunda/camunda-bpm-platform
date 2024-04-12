package org.camunda.bpm.engine.test.cache;

import org.camunda.bpm.engine.ProcessEngineConfiguration;

public class TestContext {

    private final ThreadLocal<ProcessEngineConfiguration> currentExecutionConfig = new ThreadLocal<>();
    private final ThreadLocal<ObjectChangeTracker<ProcessEngineConfiguration>> configTracker = new ThreadLocal<>();

    public void setExecutionConfig(ProcessEngineConfiguration config) {
        currentExecutionConfig.set(config);
    }

    public ProcessEngineConfiguration getCurrentExecutionConfig() {
        return currentExecutionConfig.get();
    }

    public void clearCurrentExecutionConfig() {
        currentExecutionConfig.remove();
    }

    public ObjectChangeTracker<ProcessEngineConfiguration> getConfigTracker() {
        return configTracker.get();
    }

    public void setConfigTracker(ObjectChangeTracker<ProcessEngineConfiguration> tracker) {
        configTracker.set(tracker);
    }

    // Lazy Holder idiom

    private static class SingletonHolder {
        private static final TestContext INSTANCE = new TestContext();
    }

    public static TestContext getInstance() {
        return SingletonHolder.INSTANCE;
    }

}