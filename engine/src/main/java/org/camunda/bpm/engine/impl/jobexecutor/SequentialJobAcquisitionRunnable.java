package org.camunda.bpm.engine.impl.jobexecutor;

import java.util.Iterator;
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

  private static Logger log = Logger.getLogger(SequentialJobAcquisitionRunnable.class.getName());

  protected JobAcquisitionContext acquisitionContext;

  public SequentialJobAcquisitionRunnable(JobExecutor jobExecutor) {
    super(jobExecutor);
    acquisitionContext = initializeAcquisitionContext();
  }

  public synchronized void run() {
    log.info(jobExecutor.getName() + " starting to acquire jobs");

    JobAcquisitionStrategy acquisitionStrategy = initializeAcquisitionStrategy();

    while (!isInterrupted) {
      acquisitionContext.reset();
      acquisitionContext.setAcquisitionTime(System.currentTimeMillis());

      Iterator<ProcessEngineImpl> engineIterator = jobExecutor.engineIterator();

      try {
        while (engineIterator.hasNext()) {
          ProcessEngineImpl currentProcessEngine = engineIterator.next();
          if (!jobExecutor.hasRegisteredEngine(currentProcessEngine)) {
            // if engine has been unregistered meanwhile
            continue;
          }

          AcquiredJobs acquiredJobs = acquireJobs(acquisitionContext, acquisitionStrategy, currentProcessEngine);
          executeJobs(acquisitionContext, currentProcessEngine, acquiredJobs);
        }
      } catch (Exception e) {
        log.log(Level.SEVERE, "exception during job acquisition: " + e.getMessage(), e);

        acquisitionContext.setAcquisitionException(e);
      }

      acquisitionContext.setJobAdded(isJobAdded);
      configureNextAcquisitionCycle(acquisitionContext, acquisitionStrategy);

      long waitTime = acquisitionStrategy.getWaitTime();
      // wait the requested wait time minus the time that acquisition itself took
      // this makes the intervals of job acquisition more constant and therefore predictable
      waitTime = Math.max(0, (acquisitionContext.getAcquisitionTime() + waitTime) - System.currentTimeMillis());

      suspendAcquisition(waitTime);
    }
    log.info(jobExecutor.getName() + " stopped job acquisition");
  }

  protected JobAcquisitionContext initializeAcquisitionContext() {
    return new JobAcquisitionContext();
  }

  protected void configureNextAcquisitionCycle(JobAcquisitionContext acquisitionContext, JobAcquisitionStrategy acquisitionStrategy) {
    acquisitionStrategy.reconfigure(acquisitionContext);
  }

  protected JobAcquisitionStrategy initializeAcquisitionStrategy() {
    return new BackoffJobAcquisitionStrategy(jobExecutor);
  }

  public JobAcquisitionContext getAcquisitionContext() {
    return acquisitionContext;

  }

  protected void executeJobs(JobAcquisitionContext context, ProcessEngineImpl currentProcessEngine, AcquiredJobs acquiredJobs) {
    // submit those jobs first that we weren't able to execute last cycle
    List<List<String>> additionalJobs = context.getAdditionalJobsByEngine().get(currentProcessEngine.getName());
    if (additionalJobs != null) {
      for (List<String> jobBatch : additionalJobs) {
        jobExecutor.executeJobs(jobBatch, currentProcessEngine);
      }
    }

    for (List<String> jobIds : acquiredJobs.getJobIdBatches()) {
      jobExecutor.executeJobs(jobIds, currentProcessEngine);
    }
  }

  protected AcquiredJobs acquireJobs(
      JobAcquisitionContext context,
      JobAcquisitionStrategy acquisitionStrategy,
      ProcessEngineImpl currentProcessEngine) {
    CommandExecutor commandExecutor = currentProcessEngine.getProcessEngineConfiguration()
        .getCommandExecutorTxRequired();

    int numJobsToAcquire = acquisitionStrategy.getNumJobsToAcquire(currentProcessEngine.getName());

    AcquiredJobs acquiredJobs = null;

    if (numJobsToAcquire > 0) {
      jobExecutor.logAcquisitionAttempt(currentProcessEngine);
      acquiredJobs = commandExecutor.execute(jobExecutor.getAcquireJobsCmd(numJobsToAcquire));
    }
    else {
      acquiredJobs = new AcquiredJobs(numJobsToAcquire);
    }

    context.submitAcquiredJobs(currentProcessEngine.getName(), acquiredJobs);

    jobExecutor.logAcquiredJobs(currentProcessEngine, acquiredJobs.size());
    jobExecutor.logAcquisitionFailureJobs(currentProcessEngine, acquiredJobs.getNumberOfJobsFailedToLock());

    return acquiredJobs;
  }

}
