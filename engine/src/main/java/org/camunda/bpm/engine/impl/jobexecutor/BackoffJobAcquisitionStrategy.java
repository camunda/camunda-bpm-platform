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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Determines the number of jobs to acquire and the time to wait between acquisition cycles
 * by an exponential backoff strategy.
 *
 * <p>Manages two kinds of backoff times:
 *   <ul>
 *     <li>idle time: Wait for a certain amount of time when no jobs are available
 *     <li>backoff time: Wait for a certain amount of time when jobs are available
 *       but could not successfully be acquired
 *   </ul>
 * Both times are calculated by applying an exponential backoff. This means, when the respective conditions
 * repeatedly hold, the time increases exponentially from one acquisition cycle to the next.
 *
 * <p>This implementation manages idle and backoff time in terms of levels. The initial backoff level is 0,
 * meaning that no backoff is applied. In case the condition for increasing backoff applies, the backoff
 * level is incremented. The actual time to wait is then computed as follows
 *
 * <pre>timeToWait = baseBackoffTime * (backoffFactor ^ (backoffLevel - 1))</pre>
 *
 * <p>Accordingly, the maximum possible backoff level is
 *
 * <pre>maximumLevel = floor( log( backoffFactor, maximumBackoffTime / baseBackoffTime) ) + 1</pre>
 * (where log(a, b) is the logarithm of b to the base of a)
 *
 * @author Thorben Lindhauer
 */
public class BackoffJobAcquisitionStrategy implements JobAcquisitionStrategy {

  public static long DEFAULT_EXECUTION_SATURATION_WAIT_TIME = 100;

  /*
   * all wait times are in milliseconds
   */

  /*
   * managing the idle level
   */
  protected long baseIdleWaitTime;
  protected float idleIncreaseFactor;
  protected int idleLevel;
  protected int maxIdleLevel;
  protected long maxIdleWaitTime;

  /*
   * managing the backoff level
   */
  protected long baseBackoffWaitTime;
  protected float backoffIncreaseFactor;
  protected int backoffLevel;
  protected int maxBackoffLevel;
  protected long maxBackoffWaitTime;
  protected boolean applyJitter = false;

  /*
   * Keeping a history of recent acquisitions without locking failure
   * for backoff level decrease
   */
  protected int numAcquisitionsWithoutLockingFailure = 0;
  protected int backoffDecreaseThreshold;

  protected int baseNumJobsToAcquire;

  protected Map<String, Integer> jobsToAcquire = new HashMap<String, Integer>();

  /*
   * Backing off when the execution resources (queue) are saturated
   * in order to not busy wait for free resources
   */
  protected boolean executionSaturated = false;
  protected long executionSaturationWaitTime = DEFAULT_EXECUTION_SATURATION_WAIT_TIME;

  public BackoffJobAcquisitionStrategy(
      long baseIdleWaitTime,
      float idleIncreaseFactor,
      long maxIdleTime,
      long baseBackoffWaitTime,
      float backoffIncreaseFactor,
      long maxBackoffTime,
      int backoffDecreaseThreshold,
      int baseNumJobsToAcquire) {

    this.baseIdleWaitTime = baseIdleWaitTime;
    this.idleIncreaseFactor = idleIncreaseFactor;
    this.idleLevel = 0;
    this.maxIdleWaitTime = maxIdleTime;

    this.baseBackoffWaitTime = baseBackoffWaitTime;
    this.backoffIncreaseFactor = backoffIncreaseFactor;
    this.backoffLevel = 0;
    this.maxBackoffWaitTime = maxBackoffTime;
    this.backoffDecreaseThreshold = backoffDecreaseThreshold;

    this.baseNumJobsToAcquire = baseNumJobsToAcquire;

    initializeMaxLevels();
  }

  public BackoffJobAcquisitionStrategy(JobExecutor jobExecutor) {
    this(jobExecutor.getWaitTimeInMillis(),
        jobExecutor.getWaitIncreaseFactor(),
        jobExecutor.getMaxWait(),
        jobExecutor.getBackoffTimeInMillis(),
        jobExecutor.getWaitIncreaseFactor(),
        jobExecutor.getMaxBackoff(),
        jobExecutor.getBackoffDecreaseThreshold(),
        jobExecutor.getMaxJobsPerAcquisition());
  }

  protected void initializeMaxLevels() {
    if (baseIdleWaitTime > 0 && maxIdleWaitTime > 0 && idleIncreaseFactor > 0 && maxIdleWaitTime >= baseIdleWaitTime) {
      // the maximum level that produces an idle time <= maxIdleTime:
      // see class docs for an explanation
      maxIdleLevel = (int) log(idleIncreaseFactor, maxIdleWaitTime / baseIdleWaitTime) + 1;

      // + 1 to get the minimum level that produces an idle time > maxIdleTime
      maxIdleLevel += 1;
    }
    else {
      maxIdleLevel = 0;
    }

    if (baseBackoffWaitTime > 0 && maxBackoffWaitTime > 0 && backoffIncreaseFactor > 0
        && maxBackoffWaitTime >= baseBackoffWaitTime) {
      // the maximum level that produces a backoff time < maxBackoffTime:
      // see class docs for an explanation
      maxBackoffLevel = (int) log(backoffIncreaseFactor, maxBackoffWaitTime / baseBackoffWaitTime) + 1;

      // + 1 to get the minimum level that produces a backoff time > maxBackoffTime
      maxBackoffLevel += 1;
    }
    else {
      maxBackoffLevel = 0;
    }
  }

  protected double log(double base, double value) {
    return Math.log10(value) / Math.log10(base);
  }

  @Override
  public void reconfigure(JobAcquisitionContext context) {
    reconfigureIdleLevel(context);
    reconfigureBackoffLevel(context);
    reconfigureNumberOfJobsToAcquire(context);
    executionSaturated = allSubmittedJobsRejected(context);
  }

  /**
   * @return true, if all acquired jobs (spanning all engines) were rejected for execution
   */
  protected boolean allSubmittedJobsRejected(JobAcquisitionContext context) {
    for (Map.Entry<String, AcquiredJobs> acquiredJobsForEngine : context.getAcquiredJobsByEngine().entrySet()) {
      String engineName = acquiredJobsForEngine.getKey();

      List<List<String>> acquiredJobBatches = acquiredJobsForEngine.getValue().getJobIdBatches();
      List<List<String>> resubmittedJobBatches = context.getAdditionalJobsByEngine().get(engineName);
      List<List<String>> rejectedJobBatches = context.getRejectedJobsByEngine().get(engineName);

      int numJobsSubmittedForExecution = acquiredJobBatches.size();
      if (resubmittedJobBatches != null) {
        numJobsSubmittedForExecution += resubmittedJobBatches.size();
      }

      int numJobsRejected = 0;
      if (rejectedJobBatches != null) {
        numJobsRejected += rejectedJobBatches.size();
      }

      // if not all jobs scheduled for execution have been rejected
      if (numJobsRejected == 0 || numJobsSubmittedForExecution > numJobsRejected) {
        return false;
      }
    }

    return true;
  }

  protected void reconfigureIdleLevel(JobAcquisitionContext context) {
    if (context.isJobAdded()) {
      idleLevel = 0;
    }
    else {
      if (context.areAllEnginesIdle() || context.getAcquisitionException() != null) {
        if (idleLevel < maxIdleLevel) {
          idleLevel++;
        }
      }
      else {
        idleLevel = 0;
      }
    }
  }

  protected void reconfigureBackoffLevel(JobAcquisitionContext context) {
    // if for any engine, jobs could not be locked due to optimistic locking, back off

    if (context.hasJobAcquisitionLockFailureOccurred()) {
      numAcquisitionsWithoutLockingFailure = 0;
      applyJitter = true;
      if (backoffLevel < maxBackoffLevel) {
        backoffLevel++;
      }
    }
    else {
      applyJitter = false;
      numAcquisitionsWithoutLockingFailure++;
      if (numAcquisitionsWithoutLockingFailure >= backoffDecreaseThreshold && backoffLevel > 0) {
        backoffLevel--;
        numAcquisitionsWithoutLockingFailure = 0;
      }
    }
  }

  protected void reconfigureNumberOfJobsToAcquire(JobAcquisitionContext context) {
    // calculate the number of jobs to acquire next time
    jobsToAcquire.clear();
    for (Map.Entry<String, AcquiredJobs> acquiredJobsEntry : context.getAcquiredJobsByEngine().entrySet()) {
      String engineName = acquiredJobsEntry.getKey();

      int numJobsToAcquire = (int) (baseNumJobsToAcquire * Math.pow(backoffIncreaseFactor, backoffLevel));
      List<List<String>> rejectedJobBatchesForEngine = context.getRejectedJobsByEngine().get(engineName);
      if (rejectedJobBatchesForEngine != null) {
        numJobsToAcquire -= rejectedJobBatchesForEngine.size();
      }
      numJobsToAcquire = Math.max(0, numJobsToAcquire);

      jobsToAcquire.put(engineName, numJobsToAcquire);
    }
  }

  @Override
  public long getWaitTime() {
    if (idleLevel > 0) {
      return calculateIdleTime();
    }
    else if (backoffLevel > 0) {
      return calculateBackoffTime();
    }
    else if (executionSaturated) {
      return executionSaturationWaitTime;
    }
    else {
      return 0;
    }
  }

  protected long calculateIdleTime() {
    if (idleLevel <= 0) {
      return 0;
    } else if (idleLevel >= maxIdleLevel) {
      return maxIdleWaitTime;
    }
    else {
      return (long) (baseIdleWaitTime * Math.pow(idleIncreaseFactor, idleLevel - 1));
    }
  }

  protected long calculateBackoffTime() {
    long backoffTime = 0;

    if (backoffLevel <= 0) {
      backoffTime = 0;
    } else if (backoffLevel >= maxBackoffLevel) {
      backoffTime = maxBackoffWaitTime;
    }
    else {
      backoffTime = (long) (baseBackoffWaitTime * Math.pow(backoffIncreaseFactor, backoffLevel - 1));
    }

    if (applyJitter) {
      // add a bounded random jitter to avoid multiple job acquisitions getting exactly the same
      // polling interval
      backoffTime += Math.random() * (backoffTime / 2);
    }

    return backoffTime;
  }

  @Override
  public int getNumJobsToAcquire(String processEngine) {
    Integer numJobsToAcquire = jobsToAcquire.get(processEngine);
    if (numJobsToAcquire != null) {
      return numJobsToAcquire;
    }
    else {
      return baseNumJobsToAcquire;
    }
  }
}
