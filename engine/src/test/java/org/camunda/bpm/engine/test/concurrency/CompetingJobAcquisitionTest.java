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

package org.camunda.bpm.engine.test.concurrency;

import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cmd.AcquireJobsCmd;
import org.camunda.bpm.engine.impl.jobexecutor.AcquiredJobs;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.test.Deployment;
import org.slf4j.Logger;


/**
 * @author Tom Baeyens
 */
public class CompetingJobAcquisitionTest extends PluggableProcessEngineTestCase {

private static Logger LOG = ProcessEngineLogger.TEST_LOGGER.getLogger();

  Thread testThread = Thread.currentThread();
  static ControllableThread activeThread;
  static String jobId;

  public class JobAcquisitionThread extends ControllableThread {
    OptimisticLockingException exception;
    AcquiredJobs jobs;

    @Override
    public synchronized void startAndWaitUntilControlIsReturned() {
      activeThread = this;
      super.startAndWaitUntilControlIsReturned();
    }
    public void run() {
      try {
        JobExecutor jobExecutor = processEngineConfiguration.getJobExecutor();
        jobs = (AcquiredJobs) processEngineConfiguration
          .getCommandExecutorTxRequired()
          .execute(new ControlledCommand(activeThread, new AcquireJobsCmd(jobExecutor)));

      } catch (OptimisticLockingException e) {
        this.exception = e;
      }
      LOG.debug(getName()+" ends");
    }
  }

  @Deployment
  public void testCompetingJobAcquisitions() throws Exception {
    runtimeService.startProcessInstanceByKey("CompetingJobAcquisitionProcess");

    LOG.debug("test thread starts thread one");
    JobAcquisitionThread threadOne = new JobAcquisitionThread();
    threadOne.startAndWaitUntilControlIsReturned();

    LOG.debug("test thread continues to start thread two");
    JobAcquisitionThread threadTwo = new JobAcquisitionThread();
    threadTwo.startAndWaitUntilControlIsReturned();

    LOG.debug("test thread notifies thread 1");
    threadOne.proceedAndWaitTillDone();
    assertNull(threadOne.exception);
    // the job was acquired
    assertEquals(1, threadOne.jobs.size());

    LOG.debug("test thread notifies thread 2");
    threadTwo.proceedAndWaitTillDone();
    // the acquisition did NOT fail
    assertNull(threadTwo.exception);
    // but the job was not acquired
    assertEquals(0, threadTwo.jobs.size());

  }

}
