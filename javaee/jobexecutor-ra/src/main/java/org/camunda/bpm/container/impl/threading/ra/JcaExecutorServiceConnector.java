package org.camunda.bpm.container.impl.threading.ra;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.Connector;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.TransactionSupport;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.WorkManager;
import javax.transaction.xa.XAResource;

import org.camunda.bpm.container.ExecutorService;
import org.camunda.bpm.container.impl.threading.jca.inflow.JobExecutionHandler;
import org.camunda.bpm.container.impl.threading.ra.inflow.JobExecutionHandlerActivation;
import org.camunda.bpm.container.impl.threading.ra.inflow.JobExecutionHandlerActivationSpec;
import org.camunda.bpm.container.impl.threading.ra.util.AutodetectWorkManagerExecutorService;


/**
 * <p>The {@link ResourceAdapter} responsible for bootstrapping the JcaExecutorService</p>
 * 
 * @author Daniel Meyer
 */
@Connector(
    reauthenticationSupport = false, 
    transactionSupport = TransactionSupport.TransactionSupportLevel.NoTransaction
  )
public class JcaExecutorServiceConnector implements ResourceAdapter {

  private static Logger log = Logger.getLogger(JcaExecutorServiceConnector.class.getName());

  protected JobExecutionHandlerActivation jobHandlerActivation;

  protected WorkManager workManager;

  protected ExecutorService executorService;
  
  public JcaExecutorServiceConnector() {
  }
  
  // RA-Lifecycle ///////////////////////////////////////////////////
  
  public void start(BootstrapContext ctx) throws ResourceAdapterInternalException {
    workManager = ctx.getWorkManager();
    
    executorService = AutodetectWorkManagerExecutorService.getExecutorService(this);
    
    log.log(Level.INFO, "camunda BPM executor service started.");
  }

  public void stop() {
    log.log(Level.INFO, "camunda BPM executor service stopped.");
    
  }
  
  // JobHandler activation / deactivation ///////////////////////////

  public void endpointActivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) throws ResourceException {
    if(jobHandlerActivation != null) {
      throw new ResourceException("The fox platform job executor can only service a single MessageEndpoint for job execution. " +
      		"Make sure not to deploy more than one MDB implementing the '"+JobExecutionHandler.class.getName()+"' interface.");
    }
    JobExecutionHandlerActivation activation = new JobExecutionHandlerActivation(this, endpointFactory, (JobExecutionHandlerActivationSpec) spec);
    activation.start();
    jobHandlerActivation = activation;
  }

  public void endpointDeactivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) {
    try {
      if(jobHandlerActivation != null) {
        jobHandlerActivation.stop();
      }
    } finally {
      jobHandlerActivation = null;
    }
  }

  // unsupported (No TX Support) ////////////////////////////////////////////
  
  public XAResource[] getXAResources(ActivationSpec[] specs) throws ResourceException {
    log.finest("getXAResources()");
    return null;
  }
  
  // getters ///////////////////////////////////////////////////////////////
  
  public ExecutorService getExecutorService() {
    return executorService;
  }
   
  public JobExecutionHandlerActivation getJobHandlerActivation() {
    return jobHandlerActivation;
  }
  
  public WorkManager getWorkManager() {
    return workManager;
  }
  
  // misc //////////////////////////////////////////////////////////////////

  @Override
  public int hashCode() {
    return 17;
  }

  @Override
  public boolean equals(Object other) {
    if (other == null) {
      return false;
    }
    if (other == this) {
      return true;
    }
    if (!(other instanceof JcaExecutorServiceConnector)) {
      return false;
    }
    return true;
  }

}
