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
package org.camunda.bpm.engine.test.jobexecutor;

import java.util.List;

import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.concurrency.ConcurrencyTestCase.ThreadControl;
import org.camunda.bpm.engine.test.jobexecutor.RecordingAcquireJobsRunnable.RecordedWaitEvent;

/**
 * @author Thorben Lindhauer
 *
 */
public class JobAcquisitionTest extends PluggableProcessEngineTestCase {

  protected static final int DEFAULT_NUM_JOBS_TO_ACQUIRE = 3;

  protected ControllableJobExecutor jobExecutor1;
  protected ControllableJobExecutor jobExecutor2;

  protected ThreadControl acquisitionThread1;
  protected ThreadControl acquisitionThread2;

  protected void setUp() throws Exception {
    // two job executors with the default settings
    jobExecutor1 = new ControllableJobExecutor((ProcessEngineImpl) processEngine);
    jobExecutor1.setMaxJobsPerAcquisition(DEFAULT_NUM_JOBS_TO_ACQUIRE);
    acquisitionThread1 = jobExecutor1.getAcquisitionThreadControl();

    jobExecutor2 = new ControllableJobExecutor((ProcessEngineImpl) processEngine);
    jobExecutor2.setMaxJobsPerAcquisition(DEFAULT_NUM_JOBS_TO_ACQUIRE);
    acquisitionThread2 = jobExecutor2.getAcquisitionThreadControl();
  }

  protected void tearDown() throws Exception {
    jobExecutor1.shutdown();
    jobExecutor2.shutdown();
    super.tearDown();
  }


  @Deployment(resources = "org/camunda/bpm/engine/test/jobexecutor/simpleAsyncProcess.bpmn20.xml")
  public void testJobLockingFailure() {
    int numberOfInstances = 3;

    // when starting a number of process instances
    for (int i = 0; i < numberOfInstances; i++) {
      runtimeService.startProcessInstanceByKey("simpleAsyncProcess").getId();
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
    assertEquals(0, managementService.createJobQuery().active().count());
    List<RecordedWaitEvent> jobExecutor1WaitEvents = jobExecutor1.getAcquireJobsRunnable().getWaitEvents();
    assertEquals(1, jobExecutor1WaitEvents.size());
    assertEquals(0, jobExecutor1WaitEvents.get(0).getWaitTime());

    // when continuing acquisition thread 2
    acquisitionThread2.makeContinueAndWaitForSync();

    // then its acquisition cycle fails with OLEs
    // but the acquisition thread immediately tries again
    List<RecordedWaitEvent> jobExecutor2WaitEvents = jobExecutor2.getAcquireJobsRunnable().getWaitEvents();
    assertEquals(1, jobExecutor2WaitEvents.size());
    assertEquals(0, jobExecutor2WaitEvents.get(0).getWaitTime());

  }
}
