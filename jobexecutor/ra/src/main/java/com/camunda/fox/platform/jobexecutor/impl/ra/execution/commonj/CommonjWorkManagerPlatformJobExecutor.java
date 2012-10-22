package com.camunda.fox.platform.jobexecutor.impl.ra.execution.commonj;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.Referenceable;

import org.activiti.engine.impl.interceptor.CommandExecutor;

import com.camunda.fox.platform.jobexecutor.impl.PlatformExecuteJobsRunnable;
import com.camunda.fox.platform.jobexecutor.impl.PlatformJobExecutor;
import com.camunda.fox.platform.jobexecutor.impl.ra.PlatformJobExecutorConnector;
import com.camunda.fox.platform.jobexecutor.impl.ra.execution.JcaInflowExecuteJobsRunnable;
import commonj.work.WorkException;
import commonj.work.WorkManager;
import commonj.work.WorkRejectedException;

/**
 * {@link AbstractPlatformJobExecutor} implementation delegating to a CommonJ {@link WorkManager}.
 * 
 * @author Christian Lipphardt
 * 
 */
public class CommonjWorkManagerPlatformJobExecutor extends PlatformJobExecutor implements Referenceable {

  private static Logger logger = Logger.getLogger(CommonjWorkManagerPlatformJobExecutor.class.getName());
  
  protected String workManagerJndiName = "wm/fox-job-executor";
  protected WorkManager workManager;
  protected final PlatformJobExecutorConnector ra;
  
  protected WorkManager lookupWorkMananger() {
    try {
      InitialContext initialContext = new InitialContext();
      return (WorkManager) initialContext.lookup(workManagerJndiName);
    } catch (Exception e) {
      throw new RuntimeException("Error while starting JobExecutor: could not look up CommonJ WorkManager in Jndi: "+e.getMessage(), e);
    }   
  }
  
  public CommonjWorkManagerPlatformJobExecutor(PlatformJobExecutorConnector platformJobExecutorConnector) {
    ra = platformJobExecutorConnector;
  }

  public void executeJobs(List<String> jobIds, CommandExecutor commandExecutor) {
    try {
      workManager.schedule(new CommonjWorkRunnableAdapter(new JcaInflowExecuteJobsRunnable(jobIds, commandExecutor, ra)));
    } catch (WorkRejectedException e) {
      // simulate caller runs policy (execute runnable in our thread)
      // TODO: add pluggable strategy
      new PlatformExecuteJobsRunnable(jobIds, commandExecutor).run();
    } catch (WorkException e) {
      logger.log(Level.WARNING, "WorkException while scheduling jobs for execution", e);
    }
  }

  public Object scheduleAcquisition(Runnable acquisitionRunnable) {
    // initialize the workManager here, because we have access to the initial context
    // of the calling thread (application), so the jndi lookup is working -> see JCA 1.6 specification
    if(workManager == null) {
      workManager = lookupWorkMananger();
    }
      
    try {
      return workManager.schedule(new CommonjAcquireJobsRunnableAdapter(acquisitionRunnable));
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
