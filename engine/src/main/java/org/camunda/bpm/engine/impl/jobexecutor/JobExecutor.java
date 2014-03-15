/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine.impl.jobexecutor;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cmd.AcquireJobsCmd;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
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

  private static Logger log = Logger.getLogger(JobExecutor.class.getName());

  protected String name = "JobExecutor["+getClass().getName()+"]";
  protected List<ProcessEngineImpl> processEngines = new CopyOnWriteArrayList<ProcessEngineImpl>();
  protected Command<AcquiredJobs> acquireJobsCmd;
  protected AcquireJobsRunnable acquireJobsRunnable;
  protected RejectedJobsHandler rejectedJobsHandler;
  protected Thread jobAcquisitionThread;

  protected boolean isAutoActivate = false;
  protected boolean isActive = false;

  protected int maxJobsPerAcquisition = 3;
  protected int waitTimeInMillis = 5 * 1000;
  protected String lockOwner = UUID.randomUUID().toString();
  protected int lockTimeInMillis = 5 * 60 * 1000;

  public void start() {
    if (isActive) {
      return;
    }
    log.info("Starting up the JobExecutor["+getClass().getName()+"].");
    ensureInitialization();
    startExecutingJobs();
    isActive = true;
  }

  public synchronized void shutdown() {
    if (!isActive) {
      return;
    }
    log.info("Shutting down the JobExecutor["+getClass().getName()+"].");
    acquireJobsRunnable.stop();
    stopExecutingJobs();
    ensureCleanup();
    isActive = false;
  }

  protected void ensureInitialization() {
    acquireJobsCmd = new AcquireJobsCmd(this);
    acquireJobsRunnable = new SequentialJobAcquisitionRunnable(this);
  }

  protected void ensureCleanup() {
    acquireJobsCmd = null;
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

  // getters and setters //////////////////////////////////////////////////////

  public List<ProcessEngineImpl> getProcessEngines() {
    return processEngines;
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

  public String getName() {
    return name;
  }

  public Command<AcquiredJobs> getAcquireJobsCmd() {
    return acquireJobsCmd;
  }

  public void setAcquireJobsCmd(Command<AcquiredJobs> acquireJobsCmd) {
    this.acquireJobsCmd = acquireJobsCmd;
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
		} catch (InterruptedException e) {
			log.log(
					Level.WARNING,
					"Interrupted while waiting for the job Acquisition thread to terminate",
					e);
		}
		jobAcquisitionThread = null;
	}

  public AcquireJobsRunnable getAcquireJobsRunnable() {
    return acquireJobsRunnable;
  }

}
