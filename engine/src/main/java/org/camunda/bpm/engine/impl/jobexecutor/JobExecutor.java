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
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.management.Metrics;
import org.camunda.bpm.engine.runtime.Job;

/**
 * <p>Interface to the component responsible for performing
 * background work ({@link Job Jobs}).</p>
 *
 * <p>The {@link JobExecutor} is capable of dispatching to multiple process engines,
 * ie. multiple process engines can share a single Thread Pool for performing Background
 * Work. </p>
 *
 * <p>In clustered situations, you can have multiple Job Executors running against the
 * same queue + pending job list.</p>
 *
 * @author Daniel Meyer
 */
public abstract class JobExecutor {

  private final static JobExecutorLogger LOG = ProcessEngineLogger.JOB_EXECUTOR_LOGGER;

  protected String name = "JobExecutor["+getClass().getName()+"]";
  protected List<ProcessEngineImpl> processEngines = new CopyOnWriteArrayList<>();
  protected AcquireJobsCommandFactory acquireJobsCmdFactory;
  protected AcquireJobsRunnable acquireJobsRunnable;
  protected RejectedJobsHandler rejectedJobsHandler;
  protected Thread jobAcquisitionThread;

  protected boolean isAutoActivate = false;
  protected boolean isActive = false;

  protected int maxJobsPerAcquisition = 3;

  // waiting when job acquisition is idle
  protected int waitTimeInMillis = 5 * 1000;
  protected float waitIncreaseFactor = 2;
  protected long maxWait = 60 * 1000;

  // backoff when job acquisition fails to lock all jobs
  protected int backoffTimeInMillis = 0;
  protected long maxBackoff = 0;

  /**
   * The number of job acquisition cycles without locking failures
   * until the backoff level is reduced.
   */
  protected int backoffDecreaseThreshold = 100;

  protected String lockOwner = UUID.randomUUID().toString();
  protected int lockTimeInMillis = 5 * 60 * 1000;

  public void start() {
    if (isActive) {
      return;
    }
    LOG.startingUpJobExecutor(getClass().getName());
    ensureInitialization();
    startExecutingJobs();
    isActive = true;
  }

  public synchronized void shutdown() {
    if (!isActive) {
      return;
    }
    LOG.shuttingDownTheJobExecutor(getClass().getName());
    acquireJobsRunnable.stop();
    stopExecutingJobs();
    ensureCleanup();
    isActive = false;
  }

  protected void ensureInitialization() {
  if (acquireJobsCmdFactory == null) {
    acquireJobsCmdFactory =  new DefaultAcquireJobsCommandFactory(this);
  }
    acquireJobsRunnable = new SequentialJobAcquisitionRunnable(this);
  }

  protected void ensureCleanup() {
    acquireJobsCmdFactory = null;
    acquireJobsRunnable = null;
  }

  public void jobWasAdded() {
    if(isActive) {
      acquireJobsRunnable.jobWasAdded();
    }
  }

  public synchronized void registerProcessEngine(ProcessEngineImpl processEngine) {
    processEngines.add(processEngine);

    // when we register the first process engine, start the jobexecutor
    if(processEngines.size() == 1 && isAutoActivate) {
      start();
    }
  }

  public synchronized void unregisterProcessEngine(ProcessEngineImpl processEngine) {
    processEngines.remove(processEngine);

    // if we unregister the last process engine, auto-shutdown the jobexecutor
    if(processEngines.isEmpty() && isActive) {
      shutdown();
    }
  }

  protected abstract void startExecutingJobs();
  protected abstract void stopExecutingJobs();
  public abstract void executeJobs(List<String> jobIds, ProcessEngineImpl processEngine);

  /**
   * Deprecated: use {@link #executeJobs(List, ProcessEngineImpl)} instead
   * @param jobIds
   */
  @Deprecated
  public void executeJobs(List<String> jobIds) {
    if(!processEngines.isEmpty()) {
      executeJobs(jobIds, processEngines.get(0));
    }
  }

  public void logAcquisitionAttempt(ProcessEngineImpl engine) {
    if (engine.getProcessEngineConfiguration().isMetricsEnabled()) {
      engine.getProcessEngineConfiguration()
        .getMetricsRegistry()
        .markOccurrence(Metrics.JOB_ACQUISITION_ATTEMPT);
    }
  }

  public void logAcquiredJobs(ProcessEngineImpl engine, int numJobs) {
    if (engine != null && engine.getProcessEngineConfiguration().isMetricsEnabled()) {
      engine.getProcessEngineConfiguration()
        .getMetricsRegistry()
        .markOccurrence(Metrics.JOB_ACQUIRED_SUCCESS, numJobs);
    }
  }

  public void logAcquisitionFailureJobs(ProcessEngineImpl engine, int numJobs) {
    if (engine != null && engine.getProcessEngineConfiguration().isMetricsEnabled()) {
      engine.getProcessEngineConfiguration()
        .getMetricsRegistry()
        .markOccurrence(Metrics.JOB_ACQUIRED_FAILURE, numJobs);
    }
  }

  public void logRejectedExecution(ProcessEngineImpl engine, int numJobs) {
    if (engine != null && engine.getProcessEngineConfiguration().isMetricsEnabled()) {
      engine.getProcessEngineConfiguration()
        .getMetricsRegistry()
        .markOccurrence(Metrics.JOB_EXECUTION_REJECTED, numJobs);
    }
  }

  // getters and setters //////////////////////////////////////////////////////

  public List<ProcessEngineImpl> getProcessEngines() {
    return processEngines;
  }

  /**
   * Must return an iterator of registered process engines
   * that is independent of concurrent modifications
   * to the underlying data structure of engines.
   */
  public Iterator<ProcessEngineImpl> engineIterator() {
    // a CopyOnWriteArrayList's iterator is safe in the presence
    // of modifications
    return processEngines.iterator();
  }

  public boolean hasRegisteredEngine(ProcessEngineImpl engine) {
    return processEngines.contains(engine);
  }

  /**
   * Deprecated: use {@link #getProcessEngines()} instead
   */
  @Deprecated
  public CommandExecutor getCommandExecutor() {
    if(processEngines.isEmpty()) {
      return null;
    } else {
      return processEngines.get(0).getProcessEngineConfiguration().getCommandExecutorTxRequired();
    }
  }

  /**
   * Deprecated: use {@link #registerProcessEngine(ProcessEngineImpl)} instead
   * @param commandExecutorTxRequired
   */
  @Deprecated
  public void setCommandExecutor(CommandExecutor commandExecutorTxRequired) {

  }

  public int getWaitTimeInMillis() {
    return waitTimeInMillis;
  }

  public void setWaitTimeInMillis(int waitTimeInMillis) {
    this.waitTimeInMillis = waitTimeInMillis;
  }

  public int getBackoffTimeInMillis() {
    return backoffTimeInMillis;
  }

  public void setBackoffTimeInMillis(int backoffTimeInMillis) {
    this.backoffTimeInMillis = backoffTimeInMillis;
  }

  public int getLockTimeInMillis() {
    return lockTimeInMillis;
  }

  public void setLockTimeInMillis(int lockTimeInMillis) {
    this.lockTimeInMillis = lockTimeInMillis;
  }

  public String getLockOwner() {
    return lockOwner;
  }

  public void setLockOwner(String lockOwner) {
    this.lockOwner = lockOwner;
  }

  public boolean isAutoActivate() {
    return isAutoActivate;
  }

  public void setProcessEngines(List<ProcessEngineImpl> processEngines) {
    this.processEngines = processEngines;
  }

  public void setAutoActivate(boolean isAutoActivate) {
    this.isAutoActivate = isAutoActivate;
  }

  public int getMaxJobsPerAcquisition() {
    return maxJobsPerAcquisition;
  }

  public void setMaxJobsPerAcquisition(int maxJobsPerAcquisition) {
    this.maxJobsPerAcquisition = maxJobsPerAcquisition;
  }

  public float getWaitIncreaseFactor() {
    return waitIncreaseFactor;
  }

  public void setWaitIncreaseFactor(float waitIncreaseFactor) {
    this.waitIncreaseFactor = waitIncreaseFactor;
  }

  public long getMaxWait() {
    return maxWait;
  }

  public void setMaxWait(long maxWait) {
    this.maxWait = maxWait;
  }

  public long getMaxBackoff() {
    return maxBackoff;
  }

  public void setMaxBackoff(long maxBackoff) {
    this.maxBackoff = maxBackoff;
  }

  public int getBackoffDecreaseThreshold() {
    return backoffDecreaseThreshold;
  }

  public void setBackoffDecreaseThreshold(int backoffDecreaseThreshold) {
    this.backoffDecreaseThreshold = backoffDecreaseThreshold;
  }

  public String getName() {
    return name;
  }

  public Command<AcquiredJobs> getAcquireJobsCmd(int numJobs) {
    return acquireJobsCmdFactory.getCommand(numJobs);
  }

  public AcquireJobsCommandFactory getAcquireJobsCmdFactory() {
    return acquireJobsCmdFactory;
  }

  public void setAcquireJobsCmdFactory(AcquireJobsCommandFactory acquireJobsCmdFactory) {
    this.acquireJobsCmdFactory = acquireJobsCmdFactory;
  }

  public boolean isActive() {
    return isActive;
  }

  public RejectedJobsHandler getRejectedJobsHandler() {
    return rejectedJobsHandler;
  }

  public void setRejectedJobsHandler(RejectedJobsHandler rejectedJobsHandler) {
    this.rejectedJobsHandler = rejectedJobsHandler;
  }

  protected void startJobAcquisitionThread() {
		if (jobAcquisitionThread == null) {
			jobAcquisitionThread = new Thread(acquireJobsRunnable, getName());
			jobAcquisitionThread.start();
		}
	}

	protected void stopJobAcquisitionThread() {
		try {
			jobAcquisitionThread.join();
		}
		catch (InterruptedException e) {
		  LOG.interruptedWhileShuttingDownjobExecutor(e);
		}
		jobAcquisitionThread = null;
	}

  public AcquireJobsRunnable getAcquireJobsRunnable() {
    return acquireJobsRunnable;
  }

  public Runnable getExecuteJobsRunnable(List<String> jobIds, ProcessEngineImpl processEngine) {
    return new ExecuteJobsRunnable(jobIds, processEngine);
  }

}
