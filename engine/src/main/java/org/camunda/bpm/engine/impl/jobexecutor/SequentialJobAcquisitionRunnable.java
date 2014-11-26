package org.camunda.bpm.engine.impl.jobexecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;


/**
 * <p>{@link AcquireJobsRunnable} able to serve multiple process engines.</p>
 *
 * @author Daniel Meyer
 */
public class SequentialJobAcquisitionRunnable extends AcquireJobsRunnable {

  private static Logger log = Logger.getLogger(AcquireJobsRunnable.class.getName());

  public SequentialJobAcquisitionRunnable(JobExecutor jobExecutor) {
    super(jobExecutor);
  }

  public synchronized void run() {
    log.info(jobExecutor.getName() + " starting to acquire jobs");

    int processEngineLoopCounter = 0;
    List<String> idleEngines = new ArrayList<String>();
    boolean jobExecutionFailed = false;

    while (!isInterrupted) {
      ProcessEngineImpl currentProcessEngine = null;
      int maxJobsPerAcquisition = jobExecutor.getMaxJobsPerAcquisition();

      try {

        List<ProcessEngineImpl> registeredProcessEngines = jobExecutor.getProcessEngines();

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
          final CommandExecutor commandExecutor = currentProcessEngine.getProcessEngineConfiguration()
              .getCommandExecutorTxRequired();

          AcquiredJobs acquiredJobs = commandExecutor.execute(jobExecutor.getAcquireJobsCmd());

          for (List<String> jobIds : acquiredJobs.getJobIdBatches()) {
            jobExecutor.executeJobs(jobIds, currentProcessEngine);
          }

          // add number of jobs which we attempted to acquire but could not obtain a lock for -> do not wait if we could not acquire jobs.
          int jobsAcquired = acquiredJobs.getJobIdBatches().size() + acquiredJobs.getNumberOfJobsFailedToLock();
          if (jobsAcquired < maxJobsPerAcquisition) {
            idleEngines.add(currentProcessEngine.getName());
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

      int numOfEngines = jobExecutor.getProcessEngines().size();
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
