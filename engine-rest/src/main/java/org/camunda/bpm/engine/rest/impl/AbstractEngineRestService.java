package org.camunda.bpm.engine.rest.impl;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.spi.ProcessEngineProvider;

public abstract class AbstractEngineRestService {

  protected ProcessEngine processEngine;
  
  public AbstractEngineRestService() {
    processEngine = lookupProcessEngine(null);
  }
  
  public AbstractEngineRestService(String engineName) {
    processEngine = lookupProcessEngine(engineName);
  }
  
  protected ProcessEngine lookupProcessEngine(String engineName) {
    
    ServiceLoader<ProcessEngineProvider> serviceLoader = ServiceLoader.load(ProcessEngineProvider.class);
    Iterator<ProcessEngineProvider> iterator = serviceLoader.iterator();
    
    if(iterator.hasNext()) {
      ProcessEngineProvider provider = iterator.next();
      if (engineName == null) {
        return provider.getDefaultProcessEngine();
      } else {
        return provider.getProcessEngine(engineName);
      }
    } else {
      throw new RestException("Could not find an implementation of the "+ProcessEngineProvider.class+"- SPI");
    }

  }
  
}
