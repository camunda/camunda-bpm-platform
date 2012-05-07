package com.camunda.fox.platform.jobexecutor.impl.ra.execution;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.Referenceable;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkManager;
import javax.resource.spi.work.WorkRejectedException;

import org.activiti.engine.impl.interceptor.CommandExecutor;

import com.camunda.fox.platform.jobexecutor.impl.PlatformExecuteJobsRunnable;
import com.camunda.fox.platform.jobexecutor.impl.PlatformJobExecutor;
import com.camunda.fox.platform.jobexecutor.impl.ra.PlatformJobExecutorConnector;


/**
 * {@link AbstractPlatformJobExecutor} implementation delegating to a JCA {@link WorkManager}.
 * 
 * @author Daniel Meyer
 * 
 */
public class JcaWorkManagerPlatformJobExecutor extends PlatformJobExecutor implements Referenceable {
  
  private static Logger logger = Logger.getLogger(JcaWorkManagerPlatformJobExecutor.class.getName());
  
  protected final WorkManager workManager;
  protected final PlatformJobExecutorConnector ra;
  
  public JcaWorkManagerPlatformJobExecutor(WorkManager workManager, PlatformJobExecutorConnector platformJobExecutorConnector) {
    this.workManager = workManager;
    this.ra = platformJobExecutorConnector;
  }

  public void executeJobs(List<String> jobIds, CommandExecutor commandExecutor) {
    try {
      workManager.scheduleWork(new JcaWorkRunnableAdapter(new JcaInflowExecuteJobsRunnable(jobIds, commandExecutor, ra)));
    } catch (WorkRejectedException e) {
      // simmulate caller runs policy (execute runnable in our thread)
      // TODO: add pluggable strategy
      new PlatformExecuteJobsRunnable(jobIds, commandExecutor).run();
    } catch (WorkException e) {
      logger.log(Level.WARNING, "WorkException while scheduling jobs for execution", e);
    }
  }

  public Object scheduleAcquisition(Runnable acquisitionRunnable) {
    try {
      workManager.scheduleWork(new JcaWorkRunnableAdapter(acquisitionRunnable));
      return null;
    } catch (WorkException e) {
      throw new RuntimeException("Could not schedule Job Acquisition Runnable: "+e.getMessage(), e);
    }
  }

  public void unscheduleAcquisition(Object scheduledAcquisition) {
    // unsupported
  }
  
  // javax.resource.Referenceable /////////////////////////

  protected Reference reference;
  
  public Reference getReference() throws NamingException {    
    return reference;
  }

  public void setReference(Reference reference) {
    this.reference = reference;        
  }  
  
  // getters / setters ////////////////////////////////////
  
  public WorkManager getWorkManager() {
    return workManager;
  }

  public PlatformJobExecutorConnector getPlatformJobExecutorConnector() {
    return ra;
  }
}
