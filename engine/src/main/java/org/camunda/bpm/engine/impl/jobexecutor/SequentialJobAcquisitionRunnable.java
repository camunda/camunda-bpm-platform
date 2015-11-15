package org.camunda.bpm.engine.impl.jobexecutor;

import java.util.Iterator;
import java.util.List;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;


/**
 * <p>{@link AcquireJobsRunnable} able to serve multiple process engines.</p>
 *
 * <p>
 *   Continuously acquires jobs for all registered process engines until interruption.
 *   For every such <i>acquisition cycle</i>, jobs are acquired and submitted for execution.
 * </p>
 *
 * <p>
 *   For one cycle, all acquisition-related events (acquired jobs by engine, rejected jobs by engine,
 *   exceptions during acquisition, etc.) are collected in an instance of {@link JobAcquisitionContext}.
 *   The context is then handed to a {@link JobAcquisitionStrategy} that
 *   determines the there is before the next acquisition cycles begins and how many jobs
 *   are to be acquired next.
 * </p>
 *
 * @author Daniel Meyer
 */
public class SequentialJobAcquisitionRunnable extends AcquireJobsRunnable {

  private final JobExecutorLogger LOG = ProcessEngineLogger.JOB_EXECUTOR_LOGGER;

  protected JobAcquisitionContext acquisitionContext;

  public SequentialJobAcquisitionRunnable(JobExecutor jobExecutor) {
    super(jobExecutor);
    acquisitionContext = initializeAcquisitionContext();
  }

  public synchronized void run() {
    LOG.startingToAacquireJobs(jobExecutor.getName());

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
        LOG.exceptionDuringJobAcquisition(e);

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

    LOG.stoppedJobAcquisition(jobExecutor.getName());
  }

  protected JobAcquisitionContext initializeAcquisitionContext() {
    return new JobAcquisitionContext();
  }

  /**
   * Reconfigure the acquisition strategy based on the current cycle's acquisition context.
   * A strategy implementation may update internal data structure to calculate a different wait time
   * before the next cycle of acquisition is performed.
   */
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
    // submit those jobs that were acquired in previous cycles but could not be scheduled for execution
    List<List<String>> additionalJobs = context.getAdditionalJobsByEngine().get(currentProcessEngine.getName());
    if (additionalJobs != null) {
      for (List<String> jobBatch : additionalJobs) {
        jobExecutor.executeJobs(jobBatch, currentProcessEngine);
      }
    }

    // submit those jobs that were acquired in the current cycle
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
