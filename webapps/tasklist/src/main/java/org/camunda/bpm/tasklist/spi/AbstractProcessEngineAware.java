package org.camunda.bpm.tasklist.spi;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.exception.RestException;

public abstract class AbstractProcessEngineAware {

  protected ProcessEngine processEngine;
  
  public AbstractProcessEngineAware() {
    initialize();
  }
  
  protected void initialize() {
    processEngine = lookupProcessEngine();
  }

  protected ProcessEngine lookupProcessEngine() {
    
    ServiceLoader<TasklistProcessEngineProvider> serviceLoader = ServiceLoader.load(TasklistProcessEngineProvider.class);
    Iterator<TasklistProcessEngineProvider> iterator = serviceLoader.iterator();
    
    if(iterator.hasNext()) {
      TasklistProcessEngineProvider provider = iterator.next();
      return provider.getProcessEngine();      
    } else {
      throw new RestException("Could not find an implementation of the "+TasklistProcessEngineProvider.class+"- SPI");
    }

  }
  
}
