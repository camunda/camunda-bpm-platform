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
package org.camunda.bpm.engine.test.concurrency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.camunda.bpm.engine.CrdbTransactionRetryException;
import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.AcquireJobsCmd;
import org.camunda.bpm.engine.impl.jobexecutor.AcquiredJobs;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.slf4j.Logger;


/**
 * This test covers the behavior of two competing JobAcquisition threads.
 *
 * In the test:
 * 1. The first JobAcquisition thread is started.
 * 1.1. The first JobAcquisition thread attempts to acquire the job, and blocks.
 * 2. The second JobAcquisition thread is started.
 * 2.1. The second JobAcquisition thread attempts to acquire the job, and blocks.
 * 3. The first JobAcquisition thread unblocks, and successfully locks the acquired job in the DB.
 * 5. The second JobAcquisition thread unblocks, attempts to lock the acquired job in the DBm and receives an OptimisticLockingException.
 * 5.1. The OptimisticLockingListener on the second JobAcquisition thread handles
 *      the OptimisticLockingException by excluding the failed jobs.
 * 6. The second JobAcquisition thread acquires no jobs, but finishes without failing.
 *
 * @author Tom Baeyens
 */
public class CompetingJobAcquisitionTest {

  private static Logger LOG = ProcessEngineLogger.TEST_LOGGER.getLogger();

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected RuntimeService runtimeService;

  protected static ControllableThread activeThread;
  protected String databaseType;


  @Before
  public void initializeServices() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    databaseType = processEngineConfiguration.getDatabaseType();
    runtimeService = engineRule.getRuntimeService();
  }

  @Deployment
  @Test
  public void testCompetingJobAcquisitions() {
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

    if (testRule.isOptimisticLockingExceptionSuppressible()) {
      // the acquisition did NOT fail
      assertNull(threadTwo.exception);
      // but the job was not acquired
      assertEquals(0, threadTwo.jobs.size());
    } else {
      // on CockroachDb, the `commandRetries` property is 0 by default. So any retryable commands,
      // like the `FetchExternalTasksCmd` will not be retried, but report
      // a `CrdbTransactionRetryException` to the caller.
      assertThat(threadTwo.exception).isInstanceOf(CrdbTransactionRetryException.class);
    }
  }

  public class JobAcquisitionThread extends ControllableThread {
    OptimisticLockingException exception;
    AcquiredJobs jobs;

    @Override
    public synchronized void startAndWaitUntilControlIsReturned() {
      activeThread = this;
      super.startAndWaitUntilControlIsReturned();
    }

    @Override
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

}
