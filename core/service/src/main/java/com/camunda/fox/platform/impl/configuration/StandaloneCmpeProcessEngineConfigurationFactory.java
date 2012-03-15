package com.camunda.fox.platform.impl.configuration;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;

import com.camunda.fox.platform.impl.configuration.spi.ProcessEngineConfigurationFactory;
import com.camunda.fox.platform.impl.service.ProcessEngineController;

/**
 * @author Daniel Meyer
 */
public class StandaloneCmpeProcessEngineConfigurationFactory implements ProcessEngineConfigurationFactory {

  private ProcessEngineController processEngineServiceController;

  @Override
  public ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
    return new StandaloneCmpeProcessEngineConfiguration(processEngineServiceController);
  }

  @Override
  public void setProcessEngineController(ProcessEngineController processEngineServiceController) {
    this.processEngineServiceController = processEngineServiceController;

  }

}
