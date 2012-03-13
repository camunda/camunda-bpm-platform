package com.camunda.fox.platform.impl.jobexecutor.commonj;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.jobexecutor.ExecuteJobsRunnable;
import org.activiti.engine.impl.jobexecutor.JobExecutor;

import com.camunda.fox.platform.FoxPlatformException;
import com.camunda.fox.platform.impl.context.ProcessArchiveServicesSupport;
import com.camunda.fox.platform.impl.context.spi.ProcessArchiveServices;
import com.camunda.fox.platform.impl.jobexecutor.ContainerExecuteJobsRunnable;
import commonj.work.WorkException;
import commonj.work.WorkManager;
import commonj.work.WorkRejectedException;

/**
 * <p>{@link JobExecutor} implementation delegating to a CommonJ WorkManager</p>
 * 
 * <p><em>This implementation is intended to be used in environments where 
 * self-management of threads is not desirable and a CommonJ Work Management 
 * implementation is available (such as in IBM WebSphere 6.0+ and BEA WebLogic
 * 9.0+)</em></p> 
 * 
 * @author Daniel Meyer
 */
public class WorkManagerJobExecutor extends JobExecutor implements ProcessArchiveServicesSupport {
  
  private static Logger log = Logger.getLogger(WorkManagerJobExecutor.class.getName());
  
  protected String workManagerJndiName = "java:comp/env/wm/default";
  protected WorkManager workManager;
  protected ProcessArchiveServices processArchiveServices;
  
  protected void startExecutingJobs() {
    if(workManager == null) {
      workManager = lookupWorkMananger();
    }    
    try {
      workManager.schedule(new AcquireJobsRunnableAdapter(acquireJobsRunnable));      
    } catch (WorkException e) {
      throw new FoxPlatformException("Could not schedule job acquisition, giving up.", e);
    }
  }

  protected void stopExecutingJobs() {
    // nothing to do here.
  }
  
  @Override
  protected void executeJobs(List<String> jobIds) {
    ExecuteJobsRunnable executeJobsRunnable = new ContainerExecuteJobsRunnable(this,jobIds,processArchiveServices);
    try {
      
      workManager.schedule(new WorkAdapter(executeJobsRunnable));
      
    } catch (WorkRejectedException e) {
      log.log(Level.FINE, "Work was rejected by CommonJ WorkManager: " + jobIds , e);
      
      rejectedJobsHandler.jobsRejected(this, jobIds);
      
    } catch (WorkException e) {
      throw new ActivitiException("Could not submit work to CommonJ Work Mananger", e);
    }
  }
    
  protected WorkManager lookupWorkMananger() {
    try {
      InitialContext initialContext = new InitialContext();
      return (WorkManager) initialContext.lookup(workManagerJndiName);
    } catch (Exception e) {
      throw new ActivitiException("Error while starting JobExecutor: could not look up CommonJ WorkManager in Jndi: "+e.getMessage(), e);
    }   
  }
  
  // getters setters

  
  public String getWorkManagerJndiName() {
    return workManagerJndiName;
  }

  public void setWorkManagerJndiName(String workManagerJndiName) {
    this.workManagerJndiName = workManagerJndiName;
  }
    
  public WorkManager getWorkManager() {
    return workManager;
  }

  public void setWorkManager(WorkManager workManager) {
    this.workManager = workManager;
  }
  
  public void setProcessArchiveServices(ProcessArchiveServices processArchiveServices) {
    this.processArchiveServices = processArchiveServices;
  }
}
