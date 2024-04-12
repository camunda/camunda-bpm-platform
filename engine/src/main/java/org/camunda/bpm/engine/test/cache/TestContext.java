package org.camunda.bpm.engine.test.cache;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;

import java.util.function.Consumer;

public class TestContext {

    private final ThreadLocal<ProcessEngineConfiguration> currentExecutionConfig = new ThreadLocal<>();
    private final ThreadLocal<ObjectChangeTracker<ProcessEngineConfiguration>> configTracker = new ThreadLocal<>();

    private final ThreadLocal<Consumer<ProcessEngineConfigurationImpl>> customConfig = new ThreadLocal<>();

    public void setCustomConfig(Consumer<ProcessEngineConfigurationImpl> config) {
        customConfig.set(config);
    }

    public Consumer<ProcessEngineConfigurationImpl> getCustomConfig() {
        return customConfig.get();
    }

    public void clearCustomConfig() {
        customConfig.remove();
    }

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