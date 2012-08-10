package com.camunda.fox.cockpit.spi.engine.impl;

import com.camunda.fox.cockpit.spi.engine.ConfigurableProcessEngineLookup;
import com.camunda.fox.cockpit.spi.engine.ProcessEngines;
import com.camunda.fox.cockpit.persistence.CockpitQueryCommandExecutor;
import java.io.Serializable;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import org.activiti.engine.ProcessEngine;

/**
 *
 * @author nico.rehwaldt
 */
@SessionScoped
public class PlatformProcessEngineLookup implements ConfigurableProcessEngineLookup, Serializable {
  
  @Inject
  private ProcessEngines processEnginesProvider;
  
  protected ProcessEngine engine; 
  protected CockpitCommandExecutor executor;
  
  @Override
  public ProcessEngine getProcessEngine() {
    if (engine == null) {
      initUsing(getDefaultProcessEngine());
    }
    return engine;
  }
  
  @Override
  public boolean isConfigurable() {
    return true;
  }
  
  @Override
  public void setProcessEngine(ProcessEngine engine) {
    initUsing(engine);
  }
  
  @Produces
  @RequestScoped
  public CockpitCommandExecutor getCockpitCommandExecutor() {
    if (executor == null) {
      initUsing(getDefaultProcessEngine());
    }
    return executor;
  }

  private ProcessEngine getDefaultProcessEngine() {
    return processEnginesProvider.getDefaultProcessEngine();
  }
  
  private void initUsing(ProcessEngine engine) {
    this.engine = engine;
    this.executor = CockpitCommandExecutor.createFromEngine(engine);
  }

  @Override
  public void ungetProcessEngine() {
    // Platform
  }
}
