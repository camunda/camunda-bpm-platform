package com.camunda.fox.platform.impl.configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.activiti.engine.impl.interceptor.CommandContextInterceptor;
import org.activiti.engine.impl.interceptor.CommandInterceptor;
import org.activiti.engine.impl.interceptor.LogInterceptor;

import com.camunda.fox.platform.impl.service.ProcessEngineController;

/**
 * @author Daniel Meyer
 */
public class StandaloneCmpeProcessEngineConfiguration extends CmpeProcessEngineConfiguration {

  public StandaloneCmpeProcessEngineConfiguration(ProcessEngineController processEngineServiceBean) {
    super(processEngineServiceBean);
  }

  protected Collection< ? extends CommandInterceptor> getDefaultCommandInterceptorsTxRequired() {
    List<CommandInterceptor> defaultCommandInterceptorsTxRequired = new ArrayList<CommandInterceptor>();
    defaultCommandInterceptorsTxRequired.add(new LogInterceptor());
    defaultCommandInterceptorsTxRequired.add(new CommandContextInterceptor(commandContextFactory, this));
    return defaultCommandInterceptorsTxRequired;
  }
  
  protected Collection< ? extends CommandInterceptor> getDefaultCommandInterceptorsTxRequiresNew() {
    // assumes this is already initialized and in standalone cases the required and requires new are the same
    return commandInterceptorsTxRequired;
  }

}
