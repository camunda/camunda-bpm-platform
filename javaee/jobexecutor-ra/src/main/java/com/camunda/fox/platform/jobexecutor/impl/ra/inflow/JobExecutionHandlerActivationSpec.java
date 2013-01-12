package com.camunda.fox.platform.jobexecutor.impl.ra.inflow;

import javax.resource.spi.Activation;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.InvalidPropertyException;
import javax.resource.spi.ResourceAdapter;

import com.camunda.fox.platform.jobexecutor.ra.inflow.JobExecutionHandler;

/**
 * 
 * @author Daniel Meyer
 */
@Activation(
  messageListeners = { JobExecutionHandler.class }
)
public class JobExecutionHandlerActivationSpec implements ActivationSpec {

  private ResourceAdapter ra;

  public void validate() throws InvalidPropertyException {
    // nothing to do (the endpoint has no activation properties)
  }

  public ResourceAdapter getResourceAdapter() {
    return ra;
  }

  public void setResourceAdapter(ResourceAdapter ra) {
    this.ra = ra;
  }

}
