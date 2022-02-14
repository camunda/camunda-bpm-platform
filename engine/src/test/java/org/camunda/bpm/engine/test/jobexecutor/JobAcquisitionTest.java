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
import org.camunda.bpm.engine.test.jobexecutor.RecordingAcquireJobsRunnable.RecordedWaitEvent;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Thorben Lindhauer
 *
 */
public class JobAcquisitionTest {

  protected static final int DEFAULT_NUM_JOBS_TO_ACQUIRE = 3;

  protected ControllableJobExecutor jobExecutor1;
  protected ControllableJobExecutor jobExecutor2;

  protected ThreadControl acquisitionThread1;
  protected ThreadControl acquisitionThread2;

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(configuration ->
      configuration.setJobExecutor(new ControllableJobExecutor()));
  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Before
  public void setUp() throws Exception {
    // two job executors with the default settings
    jobExecutor1 = (ControllableJobExecutor)
        ((ProcessEngineConfigurationImpl) engineRule.getProcessEngine().getProcessEngineConfiguration())
        .getJobExecutor();
    jobExecutor1.setMaxJobsPerAcquisition(DEFAULT_NUM_JOBS_TO_ACQUIRE);
    acquisitionThread1 = jobExecutor1.getAcquisitionThreadControl();

    jobExecutor2 = new ControllableJobExecutor((ProcessEngineImpl) engineRule.getProcessEngine());
    jobExecutor2.setMaxJobsPerAcquisition(DEFAULT_NUM_JOBS_TO_ACQUIRE);
    acquisitionThread2 = jobExecutor2.getAcquisitionThreadControl();
  }

  @After
  public void tearDown() throws Exception {
    jobExecutor1.shutdown();
    jobExecutor2.shutdown();
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/jobexecutor/simpleAsyncProcess.bpmn20.xml")
  public void testJobLockingFailure() {
    int numberOfInstances = 3;

    // when starting a number of process instances
    for (int i = 0; i < numberOfInstances; i++) {
      engineRule.getRuntimeService().startProcessInstanceByKey("simpleAsyncProcess").getId();
    }

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
    Assert.assertEquals(0, engineRule.getManagementService().createJobQuery().active().count());
    List<RecordedWaitEvent> jobExecutor1WaitEvents = jobExecutor1.getAcquireJobsRunnable().getWaitEvents();
    Assert.assertEquals(1, jobExecutor1WaitEvents.size());
    Assert.assertEquals(0, jobExecutor1WaitEvents.get(0).getTimeBetweenAcquisitions());

    // when continuing acquisition thread 2
    acquisitionThread2.makeContinueAndWaitForSync();

    // then its acquisition cycle fails with OLEs
    // but the acquisition thread immediately tries again
    List<RecordedWaitEvent> jobExecutor2WaitEvents = jobExecutor2.getAcquireJobsRunnable().getWaitEvents();
    Assert.assertEquals(1, jobExecutor2WaitEvents.size());

    if (testRule.isOptimisticLockingExceptionSuppressible()) {
      Assert.assertEquals(0, jobExecutor2WaitEvents.get(0).getTimeBetweenAcquisitions());
    } else {
      // In CRDB, Job Acquisition failures result in a complete rollback and retry of the transaction.
      // This causes the BackoffJobAcquisitionStrategy to propose an Idle time of 5000ms.
      Assert.assertEquals(5000, jobExecutor2WaitEvents.get(0).getTimeBetweenAcquisitions());
    }
  }
}
