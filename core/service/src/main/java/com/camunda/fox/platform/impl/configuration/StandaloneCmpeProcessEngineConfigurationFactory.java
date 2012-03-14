package com.camunda.fox.platform.impl.configuration;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;

import com.camunda.fox.platform.impl.configuration.spi.ProcessEngineConfigurationFactory;
import com.camunda.fox.platform.impl.service.ProcessEngineController;

/**
 * @author Daniel Meyer
 */
public class StandaloneCmpeProcessEngineConfigurationFactory implements ProcessEngineConfigurationFactory {

  private ProcessEngineController processEngineServiceBean;

  @Override
  public ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
    return new StandaloneCmpeProcessEngineConfiguration(processEngineServiceBean);
  }

  @Override
  public void setProcessEngineServiceBean(ProcessEngineController processEngineServiceBean) {
    this.processEngineServiceBean = processEngineServiceBean;

  }

}
