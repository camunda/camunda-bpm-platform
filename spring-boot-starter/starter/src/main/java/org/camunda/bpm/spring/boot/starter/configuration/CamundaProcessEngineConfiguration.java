package org.camunda.bpm.spring.boot.starter.configuration;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;

public interface CamundaProcessEngineConfiguration extends ProcessEnginePlugin {

  @Override
  default void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
  }

  @Override
  default void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
  }

  @Override
  default void postProcessEngineBuild(ProcessEngine processEngine) {
  }

}
