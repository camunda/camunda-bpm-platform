package org.camunda.bpm.container.impl.jobexecutor.ra;

import java.util.Iterator;
import java.util.ServiceLoader;
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

import org.camunda.bpm.container.impl.jobexecutor.ra.execution.AutoDetectWorkManagerPlatformJobExecutorFactory;
import org.camunda.bpm.container.impl.jobexecutor.ra.execution.spi.PlatformJobExecutorFactory;
import org.camunda.bpm.container.impl.jobexecutor.ra.inflow.JobExecutionHandlerActivation;
import org.camunda.bpm.container.impl.jobexecutor.ra.inflow.JobExecutionHandlerActivationSpec;
import org.camunda.bpm.engine.impl.jobexecutor.tobemerged.impl.PlatformJobExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.tobemerged.ra.inflow.JobExecutionHandler;


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
  protected PlatformJobExecutor platformJobExecutor;

  protected WorkManager workManager;
  
  public PlatformJobExecutorConnector() {
  }
  
  // RA-Lifecycle ///////////////////////////////////////////////////
  
  public void start(BootstrapContext ctx) throws ResourceAdapterInternalException {
    workManager = ctx.getWorkManager();
    
    ServiceLoader<PlatformJobExecutorFactory> serviceLoader = ServiceLoader.load(PlatformJobExecutorFactory.class);
    Iterator<PlatformJobExecutorFactory> iterator = serviceLoader.iterator();
    PlatformJobExecutorFactory platformJobExecutorFactory = null;
    if (iterator.hasNext()) {
      platformJobExecutorFactory = iterator.next();
      log.log(Level.INFO, "Using user-configured fox platform job executor factory: " + platformJobExecutorFactory.getClass().getName());
    } else {
      // try to auto-detect environment (commonJ or JCA)
      platformJobExecutorFactory = new AutoDetectWorkManagerPlatformJobExecutorFactory();
      log.log(Level.INFO, "Using default fox platform job executor factory: " + platformJobExecutorFactory.getClass().getName());
    }
    
    platformJobExecutor = platformJobExecutorFactory.createPlatformJobExecutor(this);
    platformJobExecutor.start();
    
    log.log(Level.INFO, "fox platform job executor started.");
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
  
  public PlatformJobExecutor getPlatformJobExecutor() {
    return platformJobExecutor;
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
    if (!(other instanceof PlatformJobExecutorConnector)) {
      return false;
    }
    return true;
  }

}
