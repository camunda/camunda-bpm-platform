package com.camunda.fox.platform.jobexecutor.impl.ra;

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
import javax.transaction.xa.XAResource;

import com.camunda.fox.platform.jobexecutor.impl.PlatformJobExecutor;
import com.camunda.fox.platform.jobexecutor.impl.ra.execution.JcaWorkManagerPlatformJobExecutor;
import com.camunda.fox.platform.jobexecutor.impl.ra.inflow.JobExecutionHandlerActivation;
import com.camunda.fox.platform.jobexecutor.impl.ra.inflow.JobExecutionHandlerActivationSpec;
import com.camunda.fox.platform.jobexecutor.ra.inflow.JobExecutionHandler;

/**
 * <p>The {@link ResourceAdapter} responsible for bootstrapping the {@link PlatformJobExecutor}</p>
 * 
 * @author Daniel Meyer
 */
@Connector(
  reauthenticationSupport = false, 
  transactionSupport = TransactionSupport.TransactionSupportLevel.NoTransaction
)
public class PlatformJobExecutorConnector implements ResourceAdapter {

  private static Logger log = Logger.getLogger(PlatformJobExecutorConnector.class.getName());

  protected JobExecutionHandlerActivation jobHandlerActivation;
  protected JcaWorkManagerPlatformJobExecutor platformJobExecutor;

  public PlatformJobExecutorConnector() {
  }
  
  // RA-Lifecycle ///////////////////////////////////////////////////
  
  public void start(BootstrapContext ctx) throws ResourceAdapterInternalException {
    platformJobExecutor = new JcaWorkManagerPlatformJobExecutor(ctx.getWorkManager(),this);
    platformJobExecutor.start();
    
    log.log(Level.INFO, "platform job executor started");
  }

  public void stop() {
    platformJobExecutor.stop();
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
  
  public JcaWorkManagerPlatformJobExecutor getPlatformJobExecutor() {
    return platformJobExecutor;
  }
   
  public JobExecutionHandlerActivation getJobHandlerActivation() {
    return jobHandlerActivation;
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
    if (!(other instanceof PlatformJobExecutorConnector)) {
      return false;
    }
    return true;
  }

}
