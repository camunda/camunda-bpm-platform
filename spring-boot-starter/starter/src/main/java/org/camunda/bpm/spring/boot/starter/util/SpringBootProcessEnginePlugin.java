package org.camunda.bpm.spring.boot.starter.util;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.engine.spring.SpringProcessEnginePlugin;

import static org.camunda.bpm.spring.boot.starter.util.CamundaSpringBootUtil.processEngineImpl;
import static org.camunda.bpm.spring.boot.starter.util.CamundaSpringBootUtil.springProcessEngineConfiguration;

/**
 * Convenience class that specializes {@link AbstractProcessEnginePlugin} to
 * use {@link SpringProcessEngineConfiguration} (to save casting).
 */
public class SpringBootProcessEnginePlugin extends SpringProcessEnginePlugin {


  @Override
  public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    springProcessEngineConfiguration(processEngineConfiguration)
      .ifPresent(this::preInit);
  }

  @Override
  public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    springProcessEngineConfiguration(processEngineConfiguration)
      .ifPresent(this::postInit);
  }

  @Override
  public void postProcessEngineBuild(ProcessEngine processEngine) {
    processEngineImpl(processEngine).ifPresent(this::postProcessEngineBuild);
  }

  public void preInit(SpringProcessEngineConfiguration processEngineConfiguration) {

  }

  public void postInit(SpringProcessEngineConfiguration processEngineConfiguration) {

  }

  public void postProcessEngineBuild(ProcessEngineImpl processEngine) {

  }
}
