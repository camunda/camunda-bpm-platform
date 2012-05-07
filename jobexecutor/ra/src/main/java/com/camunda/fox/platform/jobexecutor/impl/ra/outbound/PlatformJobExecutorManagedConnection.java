package com.camunda.fox.platform.jobexecutor.impl.ra.outbound;

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

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.jobexecutor.JobExecutor;

import com.camunda.fox.platform.jobexecutor.api.PlatformJobExecutorService;
import com.camunda.fox.platform.jobexecutor.impl.ra.PlatformJobExecutorConnector;
import com.camunda.fox.platform.jobexecutor.spi.JobAcquisitionConfiguration;

/**
 * 
 * @author Daniel Meyer
 * 
 */
public class PlatformJobExecutorManagedConnection implements ManagedConnection {

  protected PrintWriter logwriter;

  protected PlatformJobExecutorManagedConnectionFactory mcf;
  protected List<ConnectionEventListener> listeners;
  protected PlatformJobExecutorConnectionImpl connection;
  
  protected PlatformJobExecutorService delegate;
  
  public PlatformJobExecutorManagedConnection() {
  }

  public PlatformJobExecutorManagedConnection(PlatformJobExecutorManagedConnectionFactory mcf) {
    this.mcf = mcf;
    this.logwriter = null;
    this.listeners = Collections.synchronizedList(new ArrayList<ConnectionEventListener>(1));
    this.connection = null;
    PlatformJobExecutorConnector ra = (PlatformJobExecutorConnector) mcf.getResourceAdapter();
    delegate = ra.getPlatformJobExecutor();
  }

  public Object getConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
    connection = new PlatformJobExecutorConnectionImpl(this, mcf);
    return connection;
  }

  public void associateConnection(Object connection) throws ResourceException {
    if (connection == null) {
      throw new ResourceException("Null connection handle");
    }
    if (!(connection instanceof PlatformJobExecutorConnectionImpl)) {
      throw new ResourceException("Wrong connection handle");
    }
    this.connection = (PlatformJobExecutorConnectionImpl) connection;
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

  void closeHandle(PlatformJobExecutorConnection handle) {
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

  public JobExecutor startJobAcquisition(JobAcquisitionConfiguration configuration) {
    return delegate.startJobAcquisition(configuration);
  }

  public void stopJobAcquisition(String jobAcquisitionName) {
    delegate.stopJobAcquisition(jobAcquisitionName);
  }

  public JobExecutor registerProcessEngine(ProcessEngineConfigurationImpl processEngineConfiguration, String acquisitionName) {
    return delegate.registerProcessEngine(processEngineConfiguration, acquisitionName);
  }

  public void unregisterProcessEngine(ProcessEngineConfigurationImpl processEngineConfiguration, String acquisitionName) {
    delegate.unregisterProcessEngine(processEngineConfiguration, acquisitionName);
  }

  public JobExecutor getJobAcquisitionByName(String name) {
    return delegate.getJobAcquisitionByName(name);
  }

  public List<JobExecutor> getJobAcquisitions() {
    return delegate.getJobAcquisitions();
  }
  

}
