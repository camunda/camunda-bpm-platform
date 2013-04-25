package org.camunda.bpm.cockpit.plugin.core.persistence;

import java.io.Serializable;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;

/**
 * 
 * @author drobisch
 * @author nico.rehwaldt
 */
public class CockpitCommandExecutor implements Serializable {
  
  private static final long serialVersionUID = 1L;

  private CockpitQuerySessionFactory sessionFactory;
  
  public CockpitCommandExecutor() { }
  
  public CockpitCommandExecutor(ProcessEngineConfigurationImpl processEngineConfiguration, String mappingResourceName) {
    sessionFactory = new CockpitQuerySessionFactory();
    sessionFactory.initFromProcessEngineConfiguration(processEngineConfiguration, mappingResourceName);
  }
  
  public <T> T executeQueryCommand(Command<T> command) {
    return sessionFactory.getCommandExecutorTxRequired().execute(command);
  }
  
  /**
   * Create a new executor from the given engine
   * 
   * @param engine
   * @return 
   */
  public static CockpitCommandExecutor createFromEngine(ProcessEngine engine) {
    if (!(engine instanceof ProcessEngineImpl)) {
      throw new IllegalArgumentException("Argument must be an instance of " + ProcessEngineImpl.class.getName());
    }
    
    ProcessEngineConfigurationImpl processEngineConfiguration = ((ProcessEngineImpl) engine).getProcessEngineConfiguration();
    return new CockpitCommandExecutor(processEngineConfiguration, "org/camunda/bpm/cockpit/plugin/core/persistence/mappings.xml");
  }
}

