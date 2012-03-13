package com.camunda.fox.platform.impl.configuration;

import com.camunda.fox.platform.impl.AbstractProcessEngineService;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;

import com.camunda.fox.platform.impl.configuration.spi.ProcessEngineConfigurationFactory;

/**
 * <p>Default {@link ProcessEngineConfigurationFactory}, returning a {@link SpringCmpeProcessEngineConfiguration}</p>
 * 
 * @author Daniel Meyer
 * @author nico.rehwaldt@camunda.com
 */
public class SpringCmpeProcessEngineConfigurationFactory implements ProcessEngineConfigurationFactory {

  protected AbstractProcessEngineService processEngineServiceBean;

  public void setProcessEngineServiceBean(AbstractProcessEngineService processEngineServiceBean) {
    this.processEngineServiceBean = processEngineServiceBean;
  }

  public AbstractProcessEngineService getProcessEngineServiceBean() {
    return processEngineServiceBean;
  }
  
  public ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
    return new SpringCmpeProcessEngineConfiguration(processEngineServiceBean);
  }

}
