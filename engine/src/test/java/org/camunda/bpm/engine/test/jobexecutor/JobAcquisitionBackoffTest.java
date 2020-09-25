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
package org.camunda.bpm.engine.test.jobexecutor;

import java.util.List;

import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.concurrency.ConcurrencyTestHelper.ThreadControl;
import org.camunda.bpm.engine.test.jobexecutor.RecordingAcquireJobsRunnable.RecordedAcquisitionEvent;
import org.camunda.bpm.engine.test.jobexecutor.RecordingAcquireJobsRunnable.RecordedWaitEvent;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Thorben Lindhauer
 *
 */
public class JobAcquisitionBackoffTest {

  protected static final int BASE_BACKOFF_TIME = 1000;
  protected static final int MAX_BACKOFF_TIME = 5000;
  protected static final int BACKOFF_FACTOR = 2;
  protected static final int BACKOFF_DECREASE_THRESHOLD = 2;
  protected static final int DEFAULT_NUM_JOBS_TO_ACQUIRE = 3;

  @Rule
  public ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(configuration ->
      configuration.setJobExecutor(new ControllableJobExecutor()));
  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected ControllableJobExecutor jobExecutor1;
  protected ControllableJobExecutor jobExecutor2;

  protected ThreadControl acquisitionThread1;
  protected ThreadControl acquisitionThread2;

  @Before
  public void setUp() throws Exception {
    jobExecutor1 = (ControllableJobExecutor)
        ((ProcessEngineConfigurationImpl) engineRule.getProcessEngine().getProcessEngineConfiguration())
          .getJobExecutor();
    jobExecutor1.setMaxJobsPerAcquisition(DEFAULT_NUM_JOBS_TO_ACQUIRE);
    jobExecutor1.setBackoffTimeInMillis(BASE_BACKOFF_TIME);
    jobExecutor1.setMaxBackoff(MAX_BACKOFF_TIME);
    jobExecutor1.setBackoffDecreaseThreshold(BACKOFF_DECREASE_THRESHOLD);
    acquisitionThread1 = jobExecutor1.getAcquisitionThreadControl();

    jobExecutor2 = new ControllableJobExecutor((ProcessEngineImpl) engineRule.getProcessEngine());
    jobExecutor2.setMaxJobsPerAcquisition(DEFAULT_NUM_JOBS_TO_ACQUIRE);
    jobExecutor2.setBackoffTimeInMillis(BASE_BACKOFF_TIME);
    jobExecutor2.setMaxBackoff(MAX_BACKOFF_TIME);
    jobExecutor2.setBackoffDecreaseThreshold(BACKOFF_DECREASE_THRESHOLD);
    acquisitionThread2 = jobExecutor2.getAcquisitionThreadControl();
  }

  @After
  public void tearDown() throws Exception {
    jobExecutor1.shutdown();
    jobExecutor2.shutdown();
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/jobexecutor/simpleAsyncProcess.bpmn20.xml")
  public void testBackoffOnOptimisticLocking() {
    // when starting a number of process instances process instance
    for (int i = 0; i < 9; i++) {
      engineRule.getRuntimeService().startProcessInstanceByKey("simpleAsyncProcess").getId();
    }

    // ensure that both acquisition threads acquire the same jobs thereby provoking an optimistic locking exception
    JobAcquisitionTestHelper.suspendInstances(engineRule.getProcessEngine(), 6);

    // when starting job execution, both acquisition threads wait before acquiring something
    jobExecutor1.start();
    acquisitionThread1.waitForSync();
    jobExecutor2.start();
    acquisitionThread2.waitForSync();

    // when having both threads acquire jobs
    // then both wait before committing the acquiring transaction (AcquireJobsCmd)
    acquisitionThread1.makeContinueAndWaitForSync();
    acquisitionThread2.makeContinueAndWaitForSync();

    // when continuing acquisition thread 1
    acquisitionThread1.makeContinueAndWaitForSync();

    // then it has not performed waiting since it was able to acquire and execute all jobs
    List<RecordedWaitEvent> jobExecutor1WaitEvents = jobExecutor1.getAcquireJobsRunnable().getWaitEvents();
    Assert.assertEquals(1, jobExecutor1WaitEvents.size());
    Assert.assertEquals(0, jobExecutor1WaitEvents.get(0).getTimeBetweenAcquisitions());

    // when continuing acquisition thread 2, acquisition fails with an OLE
    acquisitionThread2.makeContinueAndWaitForSync();

    // and has performed backoff
    List<RecordedWaitEvent> jobExecutor2WaitEvents = jobExecutor2.getAcquireJobsRunnable().getWaitEvents();
    Assert.assertEquals(1, jobExecutor2WaitEvents.size());
    RecordedWaitEvent waitEvent = jobExecutor2WaitEvents.get(0);

    if (testRule.isOptimisticLockingExceptionSuppressible()) {
      // we don't know the exact wait time,
      // since there is random jitter applied
      JobAcquisitionTestHelper.assertInBetween(BASE_BACKOFF_TIME, BASE_BACKOFF_TIME + BASE_BACKOFF_TIME / 2, waitEvent.getTimeBetweenAcquisitions());
    } else {
      // In CRDB, Job Acquisition failures result in a complete rollback and retry of the transaction.
      // This causes the BackoffJobAcquisitionStrategy to propose an Idle time of 5000ms.
      Assert.assertEquals(5000, jobExecutor2WaitEvents.get(0).getTimeBetweenAcquisitions());
    }

    // when performing another cycle of acquisition
    JobAcquisitionTestHelper.activateInstances(engineRule.getProcessEngine(), 6);
    acquisitionThread1.makeContinueAndWaitForSync();
    acquisitionThread2.makeContinueAndWaitForSync();

    // and thread 1 again acquires all jobs successfully
    acquisitionThread1.makeContinueAndWaitForSync();

    // while thread 2 again fails with OLE
    acquisitionThread2.makeContinueAndWaitForSync();

    // then thread 1 has tried to acquired 3 jobs again
    List<RecordedAcquisitionEvent> jobExecutor1AcquisitionEvents = jobExecutor1.getAcquireJobsRunnable().getAcquisitionEvents();
    RecordedAcquisitionEvent secondAcquisitionAttempt = jobExecutor1AcquisitionEvents.get(1);
    Assert.assertEquals(3, secondAcquisitionAttempt.getNumJobsToAcquire());

    // and not waited
    jobExecutor1WaitEvents = jobExecutor1.getAcquireJobsRunnable().getWaitEvents();
    Assert.assertEquals(2, jobExecutor1WaitEvents.size());
    Assert.assertEquals(0, jobExecutor1WaitEvents.get(1).getTimeBetweenAcquisitions());

    List<RecordedAcquisitionEvent> jobExecutor2AcquisitionEvents = jobExecutor2.getAcquireJobsRunnable().getAcquisitionEvents();
    secondAcquisitionAttempt = jobExecutor2AcquisitionEvents.get(1);
    jobExecutor2WaitEvents = jobExecutor2.getAcquireJobsRunnable().getWaitEvents();
    RecordedWaitEvent secondWaitEvent = jobExecutor2WaitEvents.get(1);
    long expectedBackoffTime = BASE_BACKOFF_TIME * BACKOFF_FACTOR; // 1000 * 2^1

    if (testRule.isOptimisticLockingExceptionSuppressible()) {
      // then thread 2 has tried to acquire 6 jobs this time
      Assert.assertEquals(6, secondAcquisitionAttempt.getNumJobsToAcquire());
      // and again increased its backoff
      Assert.assertEquals(2, jobExecutor2WaitEvents.size());
      JobAcquisitionTestHelper.assertInBetween(expectedBackoffTime, expectedBackoffTime + expectedBackoffTime / 2, secondWaitEvent.getTimeBetweenAcquisitions());
    } else {
      // on CRDB, thread 2 has tried to acquire 3 jobs this time
      Assert.assertEquals(3, secondAcquisitionAttempt.getNumJobsToAcquire());
      // and again the BackoffJobAcquisitionStrategy propose an Idle time (of 10000ms this time)
      Assert.assertEquals(2, jobExecutor2WaitEvents.size());
      Assert.assertEquals(10000, secondWaitEvent.getTimeBetweenAcquisitions());
    }
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/jobexecutor/simpleAsyncProcess.bpmn20.xml")
  public void testBackoffDecrease() {
    // when starting a number of process instances process instance
    for (int i = 0; i < 15; i++) {
      engineRule.getRuntimeService().startProcessInstanceByKey("simpleAsyncProcess").getId();
    }

    // ensure that both acquisition threads acquire the same jobs thereby provoking an optimistic locking exception
    JobAcquisitionTestHelper.suspendInstances(engineRule.getProcessEngine(), 12);

    // when starting job execution, both acquisition threads wait before acquiring something
    jobExecutor1.start();
    acquisitionThread1.waitForSync();
    jobExecutor2.start();
    acquisitionThread2.waitForSync();

    // when continuing acquisition thread 1
    // then it is able to acquire and execute all jobs
    acquisitionThread1.makeContinueAndWaitForSync();

    // when continuing acquisition thread 2
    // acquisition fails with an OLE
    acquisitionThread2.makeContinueAndWaitForSync();

    jobExecutor1.shutdown();
    acquisitionThread1.waitUntilDone();
    acquisitionThread2.makeContinueAndWaitForSync();

    // such that acquisition thread 2 performs backoff
    List<RecordedWaitEvent> jobExecutor2WaitEvents = jobExecutor2.getAcquireJobsRunnable().getWaitEvents();
    Assert.assertEquals(1, jobExecutor2WaitEvents.size());

    // when in the next cycles acquisition thread2 successfully acquires jobs without OLE for n times
    JobAcquisitionTestHelper.activateInstances(engineRule.getProcessEngine(), 12);

    // backoff has not decreased yet;
    // on CRDB, an Idle wait time of 5000ms is introduced
    Assert.assertTrue(jobExecutor2WaitEvents.get(0).getTimeBetweenAcquisitions() > 0);
    acquisitionThread2.makeContinueAndWaitForSync(); // acquire
    acquisitionThread2.makeContinueAndWaitForSync(); // continue after acquisition with next cycle

    if (testRule.isOptimisticLockingExceptionSuppressible()) {
      for (int i = 1; i < BACKOFF_DECREASE_THRESHOLD; i++) {
        // backoff has not decreased yet
        Assert.assertTrue(jobExecutor2WaitEvents.get(i).getTimeBetweenAcquisitions() > 0);

        acquisitionThread2.makeContinueAndWaitForSync(); // acquire
        acquisitionThread2.makeContinueAndWaitForSync(); // continue after acquisition with next cycle
      }
    } else {
      for (int i = 1; i < BACKOFF_DECREASE_THRESHOLD; i++) {
        // on CRDB, thread2 immediately retries to acquire jobs
        // since there have been no failures, or jobs acquired since the first try
        Assert.assertTrue(jobExecutor2WaitEvents.get(i).getTimeBetweenAcquisitions() == 0);

        acquisitionThread2.makeContinueAndWaitForSync(); // acquire
        acquisitionThread2.makeContinueAndWaitForSync(); // continue after acquisition with next cycle
      }
    }

    // it decreases its backoff again
    long lastBackoff = jobExecutor2WaitEvents.get(BACKOFF_DECREASE_THRESHOLD).getTimeBetweenAcquisitions();
    Assert.assertEquals(0, lastBackoff);
  }


}
