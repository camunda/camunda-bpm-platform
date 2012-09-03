package com.camunda.fox.platform.impl.configuration;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;

import com.camunda.fox.platform.impl.configuration.spi.ProcessEngineConfigurationFactory;
import com.camunda.fox.platform.impl.service.ProcessEngineController;

/**
 * <p>Default {@link ProcessEngineConfigurationFactory}, returning a {@link SpringTxCmpeProcessEngineConfiguration}</p>
 * 
 * @author Daniel Meyer
 * @author nico.rehwaldt@camunda.com
 */
public class SpringTxCmpeProcessEngineConfigurationFactory implements ProcessEngineConfigurationFactory {

  protected ProcessEngineController processEngineController;
  
  public void setProcessEngineController(ProcessEngineController processEngineController) {
    this.processEngineController = processEngineController;
  }
  
  public ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
    return new SpringTxCmpeProcessEngineConfiguration(processEngineController);
  }

}
