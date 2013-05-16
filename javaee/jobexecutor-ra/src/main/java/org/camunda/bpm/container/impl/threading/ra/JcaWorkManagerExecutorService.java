package org.camunda.bpm.container.impl.threading.ra;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.Referenceable;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkManager;
import javax.resource.spi.work.WorkRejectedException;

import org.camunda.bpm.container.ExecutorService;
import org.camunda.bpm.container.impl.threading.ra.inflow.JcaInflowExecuteJobsRunnable;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;



/**
 * {@link AbstractPlatformJobExecutor} implementation delegating to a JCA {@link WorkManager}.
 * 
 * @author Daniel Meyer
 * 
 */
public class JcaWorkManagerExecutorService implements Referenceable, ExecutorService {
  
  public static int START_WORK_TIMEOUT = 1500;

  private static Logger logger = Logger.getLogger(JcaWorkManagerExecutorService.class.getName());
  
  protected final JcaExecutorServiceConnector ra;
  protected WorkManager workManager;
  
  public JcaWorkManagerExecutorService(JcaExecutorServiceConnector connector, WorkManager workManager) {
    this.workManager = workManager;
    this.ra = connector;
  }
  
  public boolean schedule(Runnable runnable, boolean isLongRunning) {
    if(isLongRunning) {
      return scheduleLongRunning(runnable);
      
    } else {
      return executeShortRunning(runnable);
      
    }
  }

  protected boolean scheduleLongRunning(Runnable runnable) {
    try {
      workManager.scheduleWork(new JcaWorkRunnableAdapter(runnable));
      return true;
      
    } catch (WorkException e) {
      logger.log(Level.WARNING, "Could not schedule : "+e.getMessage(), e);
      return false;
      
    }
  }
  
  protected boolean executeShortRunning(Runnable runnable) {
   
    try {      
      workManager.startWork(new JcaWorkRunnableAdapter(runnable), START_WORK_TIMEOUT, null, null);
      return true;
      
    } catch (WorkRejectedException e) {
      logger.log(Level.FINE, "WorkRejectedException while scheduling jobs for execution", e);      
      
    } catch (WorkException e) {
      logger.log(Level.WARNING, "WorkException while scheduling jobs for execution", e);
    }
    
    return false;
  }
  
  public Runnable getExecuteJobsRunnable(List<String> jobIds, ProcessEngineImpl processEngine) {
    return new JcaInflowExecuteJobsRunnable(jobIds, processEngine, ra);
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

  public JcaExecutorServiceConnector getPlatformJobExecutorConnector() {
    return ra;
  }

}
