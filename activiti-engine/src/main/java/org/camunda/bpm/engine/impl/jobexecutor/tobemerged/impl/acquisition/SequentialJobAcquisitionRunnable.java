package org.camunda.bpm.engine.impl.jobexecutor.tobemerged.impl.acquisition;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.AcquireJobsRunnable;
import org.camunda.bpm.engine.impl.jobexecutor.AcquiredJobs;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;


/**
 * 
 * @author Daniel Meyer
 */
public class SequentialJobAcquisitionRunnable extends AcquireJobsRunnable {

  private static Logger log = Logger.getLogger(AcquireJobsRunnable.class.getName());

  protected final JobAcquisition jobAcquisition;
  
  public SequentialJobAcquisitionRunnable(JobExecutor jobAcquisition) {
    super(jobAcquisition);
    this.jobAcquisition = (JobAcquisition) jobAcquisition;
  }

  public synchronized void run() {
    log.info(jobExecutor.getName() + " starting to acquire jobs");

    int processEngineLoopCounter = 0;
    List<String> idleEngines = new ArrayList<String>();
    boolean jobExecutionFailed = false;

    while (!isInterrupted) {
      ProcessEngineConfigurationImpl currentProcessEngine = null;
      int maxJobsPerAcquisition = jobExecutor.getMaxJobsPerAcquisition();  
           
      try {

        List<ProcessEngineConfigurationImpl> registeredProcessEngines = jobAcquisition.getRegisteredProcessEngines();

        synchronized (registeredProcessEngines) {
          if (registeredProcessEngines.size() > 0) {
            if (registeredProcessEngines.size() <= processEngineLoopCounter) {
              processEngineLoopCounter = 0;
              isJobAdded = false;
              idleEngines.clear();              
            }
            currentProcessEngine = registeredProcessEngines.get(processEngineLoopCounter);
            processEngineLoopCounter++;
          }
        }

      } catch (Exception e) {
        log.log(Level.SEVERE, "exception while determining next process engine: " + e.getMessage(), e);
      }

      jobExecutionFailed = false;

      if (currentProcessEngine != null) {
        
        try {
          final CommandExecutor commandExecutor = currentProcessEngine.getCommandExecutorTxRequired();

          AcquiredJobs acquiredJobs = commandExecutor.execute(jobExecutor.getAcquireJobsCmd());

          for (List<String> jobIds : acquiredJobs.getJobIdBatches()) {
            jobAcquisition.executeJobs(jobIds, commandExecutor);
          }

          int jobsAcquired = acquiredJobs.getJobIdBatches().size();
          if (jobsAcquired < maxJobsPerAcquisition) {          
            idleEngines.add(currentProcessEngine.getProcessEngineName());
          } 
          
        } catch (Exception e) {
          log.log(Level.SEVERE, "exception during job acquisition: " + e.getMessage(), e);
          
          jobExecutionFailed = true;
          
          // if one of the engines fails: increase the wait time
          if(millisToWait == 0) {
            millisToWait = jobExecutor.getWaitTimeInMillis();
          } else {
            millisToWait *= waitIncreaseFactor;
            if (millisToWait > maxWait) {
              millisToWait = maxWait;
            }   
          }        
        }
      }
      
      int numOfEngines = jobAcquisition.getRegisteredProcessEngines().size();
      if(idleEngines.size() == numOfEngines) {
        // if we have determined that none of the registered engines currently have jobs -> wait
        millisToWait = jobExecutor.getWaitTimeInMillis();
      } else {
        if(!jobExecutionFailed) {
          millisToWait = 0;
        }
      }
      
      if (millisToWait > 0 && (!isJobAdded)) {
        
        try {
          log.fine("job acquisition thread sleeping for " + millisToWait + " millis");
          synchronized (MONITOR) {
            if(!isInterrupted) {
              isWaiting.set(true);
              MONITOR.wait(millisToWait);
            }
          }
          log.fine("job acquisition thread woke up");
          isJobAdded = false;
        } catch (InterruptedException e) {
          log.fine("job acquisition wait interrupted");
        } finally {
          isWaiting.set(false);
        }        
      } 
      
    }
    log.info(jobExecutor.getName() + " stopped job acquisition");
  }
  
  public boolean isJobAdded() {
    return isJobAdded;
  }

}
