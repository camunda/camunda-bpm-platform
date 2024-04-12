package org.camunda.bpm.engine.test.cache.event;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;

import java.util.function.Consumer;

import static org.camunda.bpm.engine.test.cache.event.ProcessEngineEvent.Type.CUSTOM_ENGINE_CONFIG;

public class ProcessEngineCustomConfigEvent implements ProcessEngineEvent {

    private final ProcessEngine engine;
    private final Consumer<ProcessEngineConfigurationImpl> customConfigurator;

    public ProcessEngineCustomConfigEvent(ProcessEngine engine, Consumer<ProcessEngineConfigurationImpl> customConfigurator) {
        this.engine = engine;
        this.customConfigurator = customConfigurator;
    }

    @Override
    public ProcessEngine engine() {
        return engine;
    }

    public Consumer<ProcessEngineConfigurationImpl> customConfigurator() {
        return customConfigurator;
    }

    @Override
    public Type type() {
        return CUSTOM_ENGINE_CONFIG;
    }

    @Override
    public String toString() {
        return "ProcessEngineCustomConfigEvent [engine=" + engine.getName() + ", customConfigurator=" + customConfigurator + "]";
    }
}
