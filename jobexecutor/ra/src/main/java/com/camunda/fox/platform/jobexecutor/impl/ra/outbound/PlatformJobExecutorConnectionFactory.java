package com.camunda.fox.platform.jobexecutor.impl.ra.outbound;

import java.io.Serializable;

import javax.resource.Referenceable;
import javax.resource.ResourceException;

/**
 * 
 * @author Daniel Meyer
 *
 */
public interface PlatformJobExecutorConnectionFactory extends Serializable, Referenceable {

  public PlatformJobExecutorConnection getConnection() throws ResourceException;

}
