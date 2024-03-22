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

package org.camunda.bpm.engine.test.util;

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Supplier;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.ThreadPoolJobExecutor;

/**
 * Job Executor Utility class.
 */
public class JobExecutorWaitUtils {

  /**
   * Wait on the given job executor until it finishes with all jobs processing.
   *
   * @param maxMillisToWait   the max time (ms) to wait before throwing an exception
   * @param intervalMillis    the internal (ms) time to use for checking the job processing state periodically
   * @param jobExecutor       the job executor to use
   * @param managementService the management service to use for query purposes
   * @param shutdown          if true, the job executor will be shutdown at the end of this function
   */
  public static void waitForJobExecutorToProcessAllJobs(long maxMillisToWait,
                                                        long intervalMillis,
                                                        JobExecutor jobExecutor,
                                                        ManagementService managementService,
                                                        boolean shutdown) {
    try {
      waitForCondition(maxMillisToWait, intervalMillis, () -> !areJobsAvailable(managementService));
    } finally {
      if (shutdown) {
        jobExecutor.shutdown();
      }
    }
  }

  /**
   * Waits for the given job executor to finish until its thread pool is not active.
   *
   * @param maxMillisToWait the max milliseconds to wait before throwing an exception
   * @param intervalMillis  the milliseconds interval to use for periodic checking of the state
   * @param jobExecutor     the given job executor
   */
  public static void waitForJobExecutionRunnablesToFinish(long maxMillisToWait,
                                                          long intervalMillis,
                                                          JobExecutor jobExecutor) {
    waitForCondition(maxMillisToWait, intervalMillis,
        () -> ((ThreadPoolJobExecutor) jobExecutor).getThreadPoolExecutor().getActiveCount() == 0);
  }

  private static void waitForCondition(long maxMillisToWait, long intervalMillis, Supplier<Boolean> conditionSupplier) {
    boolean conditionFulfilled = false;
    Timer timer = new Timer();
    InterruptTask task = new InterruptTask(Thread.currentThread());
    timer.schedule(task, maxMillisToWait);
    try {
      while (!conditionFulfilled && !task.isTimeLimitExceeded()) {
        Thread.sleep(intervalMillis);
        conditionFulfilled = conditionSupplier.get();
      }
    } catch (InterruptedException e) {
    } finally {
      timer.cancel();
    }
    if (!conditionFulfilled) {
      throw new ProcessEngineException("time limit of " + maxMillisToWait + " was exceeded");
    }
  }

  private static boolean areJobsAvailable(ManagementService managementService) {
    return !managementService.createJobQuery().executable().list().isEmpty();
  }

  private static class InterruptTask extends TimerTask {
    protected boolean timeLimitExceeded = false;
    protected Thread thread;

    public InterruptTask(Thread thread) {
      this.thread = thread;
    }

    public boolean isTimeLimitExceeded() {
      return timeLimitExceeded;
    }

    @Override
    public void run() {
      timeLimitExceeded = true;
      thread.interrupt();
    }
  }

}
