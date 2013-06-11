package org.camunda.bpm.container.impl.threading.ra.outbound;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

import org.camunda.bpm.container.ExecutorService;
import org.camunda.bpm.container.impl.threading.ra.JcaExecutorServiceConnector;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;


/**
 * 
 * @author Daniel Meyer
 * 
 */
public class JcaExecutorServiceManagedConnection implements ManagedConnection {

  protected PrintWriter logwriter;

  protected JcaExecutorServiceManagedConnectionFactory mcf;
  protected List<ConnectionEventListener> listeners;
  protected JcaExecutorServiceConnectionImpl connection;
  
  protected ExecutorService delegate;
  
  public JcaExecutorServiceManagedConnection() {
  }

  public JcaExecutorServiceManagedConnection(JcaExecutorServiceManagedConnectionFactory mcf) {
    this.mcf = mcf;
    this.logwriter = null;
    this.listeners = Collections.synchronizedList(new ArrayList<ConnectionEventListener>(1));
    this.connection = null;
    JcaExecutorServiceConnector ra = (JcaExecutorServiceConnector) mcf.getResourceAdapter();
    delegate = (ExecutorService) ra.getExecutorServiceWrapper().getExecutorService();
  }

  public Object getConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
    connection = new JcaExecutorServiceConnectionImpl(this, mcf);
    return connection;
  }

  public void associateConnection(Object connection) throws ResourceException {
    if (connection == null) {
      throw new ResourceException("Null connection handle");
    }
    if (!(connection instanceof JcaExecutorServiceConnectionImpl)) {
      throw new ResourceException("Wrong connection handle");
    }
    this.connection = (JcaExecutorServiceConnectionImpl) connection;
  }

  public void cleanup() throws ResourceException {
    // no-op
  }

  public void destroy() throws ResourceException {
    // no-op
  }

  public void addConnectionEventListener(ConnectionEventListener listener) {
    if (listener == null) {
      throw new IllegalArgumentException("Listener is null");
    }
    listeners.add(listener);
  }

  public void removeConnectionEventListener(ConnectionEventListener listener) {
    if (listener == null) {
      throw new IllegalArgumentException("Listener is null");
    }
    listeners.remove(listener);
  }

  void closeHandle(JcaExecutorServiceConnection handle) {
    ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
    event.setConnectionHandle(handle);
    for (ConnectionEventListener cel : listeners) {
      cel.connectionClosed(event);
    }

  }
  public PrintWriter getLogWriter() throws ResourceException {
    return logwriter;
  }

  public void setLogWriter(PrintWriter out) throws ResourceException {
    logwriter = out;
  }

  public LocalTransaction getLocalTransaction() throws ResourceException {
    throw new NotSupportedException("LocalTransaction not supported");
  }

  public XAResource getXAResource() throws ResourceException {
    throw new NotSupportedException("GetXAResource not supported not supported");
  }

  public ManagedConnectionMetaData getMetaData() throws ResourceException {
    return null;
  }

  // delegate methods /////////////////////////////////////////

  public boolean schedule(Runnable runnable, boolean isLongRunning) {
    return delegate.schedule(runnable, isLongRunning);
  }

  public Runnable getExecuteJobsRunnable(List<String> jobIds, ProcessEngineImpl processEngine) {
    return delegate.getExecuteJobsRunnable(jobIds, processEngine);
  }

}
