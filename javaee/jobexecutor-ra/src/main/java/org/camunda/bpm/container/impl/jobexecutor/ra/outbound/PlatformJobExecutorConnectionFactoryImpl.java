package org.camunda.bpm.container.impl.jobexecutor.ra.outbound;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;

import org.camunda.bpm.engine.impl.jobexecutor.tobemerged.ra.outbound.PlatformJobExecutorConnection;
import org.camunda.bpm.engine.impl.jobexecutor.tobemerged.ra.outbound.PlatformJobExecutorConnectionFactory;


/**
 * 
 * @author Daniel Meyer
 *
 */
public class PlatformJobExecutorConnectionFactoryImpl implements PlatformJobExecutorConnectionFactory {

  private static final long serialVersionUID = 1L;

  protected Reference reference;
  protected PlatformJobExecutorManagedConnectionFactory mcf;
  protected ConnectionManager connectionManager;
  
  public PlatformJobExecutorConnectionFactoryImpl() {
  }
  
  public PlatformJobExecutorConnectionFactoryImpl(PlatformJobExecutorManagedConnectionFactory mcf, ConnectionManager cxManager) {
    this.mcf = mcf;
    this.connectionManager = cxManager;
  }

  @Override
  public PlatformJobExecutorConnection getConnection() throws ResourceException {
    return (PlatformJobExecutorConnection) connectionManager.allocateConnection(mcf, null);
  }

  @Override
  public Reference getReference() throws NamingException {
    return reference;
  }

  @Override
  public void setReference(Reference reference) {
    this.reference = reference;
  }

}
