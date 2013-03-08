package org.camunda.bpm.engine.rest.impl;
package org.camunda.bpm.engine.rest.spi;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.spi.ProcessEngineProvider;

public abstract class AbstractEngineService {
public abstract class AbstractProcessEngineAware {

  protected ProcessEngine processEngine;
  
  public AbstractEngineService() {
  public AbstractProcessEngineAware() {
    initialize();
  }
  
  protected void initialize() {
    processEngine = lookupProcessEngine();
  }

  protected ProcessEngine lookupProcessEngine() {
    
    ServiceLoader<ProcessEngineProvider> serviceLoader = ServiceLoader.load(ProcessEngineProvider.class);
    Iterator<ProcessEngineProvider> iterator = serviceLoader.iterator();
    
    if(iterator.hasNext()) {
      ProcessEngineProvider provider = iterator.next();
      return provider.getProcessEngine();      
    } else {
      throw new RestException("Could not find an implementation of the "+ProcessEngineProvider.class+"- SPI");
    }

  }
  
}
