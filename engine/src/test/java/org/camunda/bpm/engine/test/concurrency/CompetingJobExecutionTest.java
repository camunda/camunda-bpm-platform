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

import java.util.List;

import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cmd.ExecuteJobsCmd;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.Deployment;
import org.slf4j.Logger;

/**
 * @author Thorben Lindhauer
 *
 */
public class CompetingJobExecutionTest extends PluggableProcessEngineTestCase {

private static Logger LOG = ProcessEngineLogger.TEST_LOGGER.getLogger();

  protected static ControllableThread activeThread;

  @Deployment
  public void testCompetingJobExecutionDefaultRetryStrategy() {
    // given an MI subprocess with two instances
    runtimeService.startProcessInstanceByKey("miParallelSubprocess");

    List<Job> currentJobs = managementService.createJobQuery().list();
    assertEquals(2, currentJobs.size());

    // when the jobs are executed in parallel
    JobExecutionThread threadOne = new JobExecutionThread(currentJobs.get(0).getId());
    threadOne.startAndWaitUntilControlIsReturned();

    JobExecutionThread threadTwo = new JobExecutionThread(currentJobs.get(1).getId());
    threadTwo.startAndWaitUntilControlIsReturned();

    // then the first committing thread succeeds
    LOG.debug("test thread notifies thread 1");
    threadOne.proceedAndWaitTillDone();
    assertNull(threadOne.exception);

    // then the second committing thread fails with an OptimisticLockingException
    // and the job retries have not been decremented
    LOG.debug("test thread notifies thread 2");
    threadTwo.proceedAndWaitTillDone();
    assertNotNull(threadTwo.exception);

    Job remainingJob = managementService.createJobQuery().singleResult();
    assertEquals(currentJobs.get(1).getRetries(), remainingJob.getRetries());

    assertNotNull(remainingJob.getExceptionMessage());

    JobEntity jobEntity = (JobEntity) remainingJob;
    assertNull(jobEntity.getLockOwner());

    // and there is no lock expiration time due to the default retry strategy
    assertNull(jobEntity.getLockExpirationTime());
  }

  @Deployment
  public void testCompetingJobExecutionFoxRetryStrategy() {
    // given an MI subprocess with two instances
    runtimeService.startProcessInstanceByKey("miParallelSubprocess");

    List<Job> currentJobs = managementService.createJobQuery().list();
    assertEquals(2, currentJobs.size());

    // when the jobs are executed in parallel
    JobExecutionThread threadOne = new JobExecutionThread(currentJobs.get(0).getId());
    threadOne.startAndWaitUntilControlIsReturned();

    JobExecutionThread threadTwo = new JobExecutionThread(currentJobs.get(1).getId());
    threadTwo.startAndWaitUntilControlIsReturned();

    // then the first committing thread succeeds
    LOG.debug("test thread notifies thread 1");
    threadOne.proceedAndWaitTillDone();
    assertNull(threadOne.exception);

    // then the second committing thread fails with an OptimisticLockingException
    // and the job retries have not been decremented
    LOG.debug("test thread notifies thread 2");
    threadTwo.proceedAndWaitTillDone();
    assertNotNull(threadTwo.exception);

    Job remainingJob = managementService.createJobQuery().singleResult();
    // retries are configured as R5/PT5M, so no decrement means 5 retries left
    assertEquals(5, remainingJob.getRetries());

    assertNotNull(remainingJob.getExceptionMessage());

    JobEntity jobEntity = (JobEntity) remainingJob;
    assertNull(jobEntity.getLockOwner());

    // and there is a custom lock expiration time
    assertNotNull(jobEntity.getLockExpirationTime());
  }

  public class JobExecutionThread extends ControllableThread {
    OptimisticLockingException exception;
    String jobId;

    JobExecutionThread(String jobId) {
      this.jobId = jobId;
    }

    @Override
    public synchronized void startAndWaitUntilControlIsReturned() {
      activeThread = this;
      super.startAndWaitUntilControlIsReturned();
    }
    public void run() {
      try {
        processEngineConfiguration
          .getCommandExecutorTxRequired()
          .execute(new ControlledCommand(activeThread, new ExecuteJobsCmd(jobId)));

      } catch (OptimisticLockingException e) {
        this.exception = e;
      }
      LOG.debug(getName()+" ends");
    }
  }
}
