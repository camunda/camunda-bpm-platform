package com.camunda.fox.platform.impl.configuration;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;

import com.camunda.fox.platform.impl.configuration.spi.ProcessEngineConfigurationFactory;
import com.camunda.fox.platform.impl.service.ProcessEngineController;

/**
 * <p>Default {@link ProcessEngineConfigurationFactory}, returning a {@link SpringCmpeProcessEngineConfiguration}</p>
 * 
 * @author Daniel Meyer
 * @author nico.rehwaldt@camunda.com
 */
public class SpringCmpeProcessEngineConfigurationFactory implements ProcessEngineConfigurationFactory {

  protected ProcessEngineController processEngineController;
  
  public void setProcessEngineController(ProcessEngineController processEngineController) {
    this.processEngineController = processEngineController;
  }
  
  public ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
    return new SpringCmpeProcessEngineConfiguration(processEngineController);
  }

}
