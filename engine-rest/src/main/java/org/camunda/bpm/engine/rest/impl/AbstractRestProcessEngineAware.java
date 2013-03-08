package org.camunda.bpm.engine.rest.impl;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.spi.impl.AbstractProcessEngineAware;

public abstract class AbstractRestProcessEngineAware extends AbstractProcessEngineAware {

  public AbstractRestProcessEngineAware() {
    super();
  }
  
  public AbstractRestProcessEngineAware(String engineName) {
    super(engineName);
  }
  
  protected ProcessEngine getProcessEngine() {
    if (processEngine == null) {
      throw new WebApplicationException(Status.BAD_REQUEST.getStatusCode());
    }
    return processEngine;
  }
}
