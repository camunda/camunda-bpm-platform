package com.camunda.fox.platform.jobexecutor.impl.acquisition;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.jobexecutor.AcquireJobsRunnable;
import org.activiti.engine.impl.jobexecutor.AcquiredJobs;


/**
 * 
 * @author Daniel Meyer
 */
public class SequentialJobAcquisitionRunnable extends AcquireJobsRunnable {

  private static Logger log = Logger.getLogger(AcquireJobsRunnable.class.getName());

  protected final JobAcquisition jobAcquisition;

  public SequentialJobAcquisitionRunnable(JobAcquisition jobExecutor) {
    super(jobExecutor);
    this.jobAcquisition = jobExecutor;
  }

  public synchronized void run() {
    log.info(jobExecutor.getName() + " starting to acquire jobs");

    int processEngineLoopCounter = 0;
    List<String> idleEngines = new ArrayList<String>();

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
        }
      }
      
      int numOfEngines = jobAcquisition.getRegisteredProcessEngines().size();
      if ((numOfEngines == processEngineLoopCounter) 
              && (idleEngines.size() == numOfEngines) 
              && (!isJobAdded)) {
        
        long millisToWait = jobExecutor.getWaitTimeInMillis();
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
