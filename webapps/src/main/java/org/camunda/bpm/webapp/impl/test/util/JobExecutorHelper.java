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
package org.camunda.bpm.webapp.impl.test.util;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.test.AbstractProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;

/**
 * Provides the {@link JobExecutor} related features of {@link AbstractProcessEngineTestCase}.
 *
 * @author nico.rehwaldt
 */
public class JobExecutorHelper {

  private final ProcessEngineImpl processEngine;

  public JobExecutorHelper(ProcessEngine processEngine) {
    this.processEngine = (ProcessEngineImpl) processEngine;
  }

  public void waitForJobExecutorToProcessAllJobs(long maxMillisToWait) {

    JobExecutor jobExecutor = getProcessEngineConfiguration().getJobExecutor();
    jobExecutor.start();
    long intervalMillis = 1000;

    try {
      Timer timer = new Timer();
      InteruptTask task = new InteruptTask(Thread.currentThread());
      timer.schedule(task, maxMillisToWait);
      boolean areJobsAvailable = true;
      try {
        while (areJobsAvailable && !task.isTimeLimitExceeded()) {
          Thread.sleep(intervalMillis);
          try {
            areJobsAvailable = areJobsAvailable();
          } catch(Throwable t) {
            // Ignore, possible that exception occurs due to locking/updating of table on MSSQL when
            // isolation level doesn't allow READ of the table
          }
        }
      } catch (InterruptedException e) {
      } finally {
        timer.cancel();
      }
      if (areJobsAvailable) {
        throw new ProcessEngineException("time limit of " + maxMillisToWait + " was exceeded");
      }

    } finally {
      jobExecutor.shutdown();
    }
  }

  public void waitForJobExecutorOnCondition(long maxMillisToWait, Callable<Boolean> condition) {
    JobExecutor jobExecutor = getProcessEngineConfiguration().getJobExecutor();
    jobExecutor.start();
    long intervalMillis = 1000;

    try {
      Timer timer = new Timer();
      InteruptTask task = new InteruptTask(Thread.currentThread());
      timer.schedule(task, maxMillisToWait);
      boolean conditionIsViolated = true;
      try {
        while (conditionIsViolated && !task.isTimeLimitExceeded()) {
          Thread.sleep(intervalMillis);
          conditionIsViolated = !condition.call();
        }
      } catch (InterruptedException e) {
      } catch (Exception e) {
        throw new ProcessEngineException("Exception while waiting on condition: "+e.getMessage(), e);
      } finally {
        timer.cancel();
      }
      if (conditionIsViolated) {
        throw new ProcessEngineException("time limit of " + maxMillisToWait + " was exceeded");
      }

    } finally {
      jobExecutor.shutdown();
    }
  }

  public boolean areJobsAvailable() {
    List<Job> list = processEngine.getManagementService().createJobQuery().list();
    for (Job job : list) {
      if (job.getRetries() > 0 && (job.getDuedate() == null || ClockUtil.getCurrentTime().after(job.getDuedate()))) {
        return true;
      }
    }
    return false;
  }

  private ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
    return processEngine.getProcessEngineConfiguration();
  }

  private static class InteruptTask extends TimerTask {
    protected boolean timeLimitExceeded = false;
    protected Thread thread;
    public InteruptTask(Thread thread) {
      this.thread = thread;
    }
    public boolean isTimeLimitExceeded() {
      return timeLimitExceeded;
    }
    public void run() {
      timeLimitExceeded = true;
      thread.interrupt();
    }
  }
}
