package com.camunda.fox.platform.jobexecutor.impl.ra.outbound;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionDefinition;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;
import javax.security.auth.Subject;

import org.camunda.bpm.engine.impl.jobexecutor.tobemerged.ra.outbound.PlatformJobExecutorConnection;
import org.camunda.bpm.engine.impl.jobexecutor.tobemerged.ra.outbound.PlatformJobExecutorConnectionFactory;


@ConnectionDefinition(
  connectionFactory = PlatformJobExecutorConnectionFactory.class, 
  connectionFactoryImpl = PlatformJobExecutorConnectionFactoryImpl.class, 
  connection = PlatformJobExecutorConnection.class, 
  connectionImpl = PlatformJobExecutorConnectionImpl.class
)
public class PlatformJobExecutorManagedConnectionFactory implements ManagedConnectionFactory, ResourceAdapterAssociation {

  private static final long serialVersionUID = 1L;
  
  protected ResourceAdapter ra;
  protected PrintWriter logwriter;
  
  public PlatformJobExecutorManagedConnectionFactory() {
  }

  public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException {
    return new PlatformJobExecutorConnectionFactoryImpl(this, cxManager);
  }

  public Object createConnectionFactory() throws ResourceException {
    throw new ResourceException("This resource adapter doesn't support non-managed environments");
  }

  public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
    return new PlatformJobExecutorManagedConnection(this);
  }
  
  @SuppressWarnings("rawtypes")
  public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
    ManagedConnection result = null;
    Iterator it = connectionSet.iterator();
    while (result == null && it.hasNext()) {
      ManagedConnection mc = (ManagedConnection) it.next();
      if (mc instanceof PlatformJobExecutorManagedConnection) {
        result = mc;
      }

    }
    return result;
  }

  public PrintWriter getLogWriter() throws ResourceException {
    return logwriter;
  }

  public void setLogWriter(PrintWriter out) throws ResourceException {
    logwriter = out;
  }

  public ResourceAdapter getResourceAdapter() {
    return ra;
  }

  public void setResourceAdapter(ResourceAdapter ra) {
    this.ra = ra;
  }

  @Override
  public int hashCode() {
    return 31;
  }

  @Override
  public boolean equals(Object other) {
    if (other == null)
      return false;
    if (other == this)
      return true;
    if (!(other instanceof PlatformJobExecutorManagedConnectionFactory))
      return false;
    return true;
  }

}
