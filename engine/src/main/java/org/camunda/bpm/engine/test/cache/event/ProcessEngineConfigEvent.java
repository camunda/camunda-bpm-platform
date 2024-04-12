package org.camunda.bpm.engine.test.cache.event;

import org.camunda.bpm.engine.ProcessEngineConfiguration;

public interface ProcessEngineConfigEvent extends ProcessEngineEvent {

    ProcessEngineConfiguration config();
}
