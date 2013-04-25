package org.camunda.bpm.container.impl.threading.ra.outbound;

import java.io.Serializable;

import javax.resource.Referenceable;
import javax.resource.ResourceException;

/**
 * 
 * @author Daniel Meyer
 *
 */
public interface JcaExecutorServiceConnectionFactory extends Serializable, Referenceable {

  public JcaExecutorServiceConnection getConnection() throws ResourceException;

}
