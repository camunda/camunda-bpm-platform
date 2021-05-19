/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.jobexecutor;

import java.util.Iterator;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.util.ClassLoaderUtil;


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

  protected final JobExecutorLogger LOG = ProcessEngineLogger.JOB_EXECUTOR_LOGGER;

  protected JobAcquisitionContext acquisitionContext;

  public SequentialJobAcquisitionRunnable(JobExecutor jobExecutor) {
    super(jobExecutor);
    acquisitionContext = initializeAcquisitionContext();
  }

  public synchronized void run() {
    LOG.startingToAcquireJobs(jobExecutor.getName());

    JobAcquisitionStrategy acquisitionStrategy = initializeAcquisitionStrategy();

    while (!isInterrupted) {
      acquisitionContext.reset();
      acquisitionContext.setAcquisitionTime(System.currentTimeMillis());


      Iterator<ProcessEngineImpl> engineIterator = jobExecutor.engineIterator();

      // See https://jira.camunda.com/browse/CAM-9913
      ClassLoader classLoaderBeforeExecution = ClassLoaderUtil.switchToProcessEngineClassloader();
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
      } finally {
        ClassLoaderUtil.setContextClassloader(classLoaderBeforeExecution);
      }

      acquisitionContext.setJobAdded(isJobAdded);
      configureNextAcquisitionCycle(acquisitionContext, acquisitionStrategy);
      //The clear had to be done after the configuration, since a hint can be
      //appear in the suspend and the flag shouldn't be cleaned in this case.
      //The loop will restart after suspend with the isJobAdded flag and
      //reconfigure with this flag
      clearJobAddedNotification();

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
        LOG.executeJobs(currentProcessEngine.getName(), jobBatch);

        jobExecutor.executeJobs(jobBatch, currentProcessEngine);
      }
    }

    // submit those jobs that were acquired in the current cycle
    for (List<String> jobIds : acquiredJobs.getJobIdBatches()) {
      LOG.executeJobs(currentProcessEngine.getName(), jobIds);

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

    LOG.acquiredJobs(currentProcessEngine.getName(), acquiredJobs);

    return acquiredJobs;
  }

}
