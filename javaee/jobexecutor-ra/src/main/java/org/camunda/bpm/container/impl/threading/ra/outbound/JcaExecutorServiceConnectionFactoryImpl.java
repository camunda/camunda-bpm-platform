package org.camunda.bpm.container.impl.threading.ra.outbound;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;




/**
 * 
 * @author Daniel Meyer
 *
 */
public class JcaExecutorServiceConnectionFactoryImpl implements JcaExecutorServiceConnectionFactory {

  private static final long serialVersionUID = 1L;

  protected Reference reference;
  protected JcaExecutorServiceManagedConnectionFactory mcf;
  protected ConnectionManager connectionManager;
  
  public JcaExecutorServiceConnectionFactoryImpl() {
  }
  
  public JcaExecutorServiceConnectionFactoryImpl(JcaExecutorServiceManagedConnectionFactory mcf, ConnectionManager cxManager) {
    this.mcf = mcf;
    this.connectionManager = cxManager;
  }

  public JcaExecutorServiceConnection getConnection() throws ResourceException {
    return (JcaExecutorServiceConnection) connectionManager.allocateConnection(mcf, null);
  }

  public Reference getReference() throws NamingException {
    return reference;
  }

  public void setReference(Reference reference) {
    this.reference = reference;
  }

}
