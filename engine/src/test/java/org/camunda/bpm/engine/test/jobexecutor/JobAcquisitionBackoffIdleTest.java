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

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.concurrency.ConcurrencyTestCase.ThreadControl;
import org.camunda.bpm.engine.test.jobexecutor.RecordingAcquireJobsRunnable.RecordedWaitEvent;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Thorben Lindhauer
 *
 */
public class JobAcquisitionBackoffIdleTest {

  public static final int BASE_IDLE_WAIT_TIME = 5000;
  public static final int MAX_IDLE_WAIT_TIME = 30000;

  protected ControllableJobExecutor jobExecutor;
  protected ThreadControl acquisitionThread;

  protected ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule() {
    public ProcessEngineConfiguration configureEngine(ProcessEngineConfigurationImpl configuration) {
      jobExecutor = new ControllableJobExecutor();
      jobExecutor.setMaxJobsPerAcquisition(1);
      jobExecutor.setWaitTimeInMillis(BASE_IDLE_WAIT_TIME);
      jobExecutor.setMaxWait(MAX_IDLE_WAIT_TIME);
      acquisitionThread = jobExecutor.getAcquisitionThreadControl();

      return configuration.setJobExecutor(jobExecutor);
    }
  };
  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(bootstrapRule).around(engineRule);

  @After
  public void shutdownJobExecutor() {
    jobExecutor.shutdown();
  }

  /**
   * CAM-5073
   */
  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/jobexecutor/simpleAsyncProcess.bpmn20.xml")
  public void testIdlingAfterConcurrentJobAddedNotification() {
    // start job acquisition - waiting before acquiring jobs
    jobExecutor.start();
    acquisitionThread.waitForSync();

    // acquire jobs
    acquisitionThread.makeContinueAndWaitForSync();

    // issue a message added notification
    engineRule.getRuntimeService().startProcessInstanceByKey("simpleAsyncProcess");

    // complete job acquisition - trigger re-configuration
    // => due to the hint, the job executor should not become idle
    acquisitionThread.makeContinueAndWaitForSync();
    assertJobExecutorWaitEvent(0L);

    // another cycle of job acquisition
    // => acquires and executes the new job
    // => acquisition does not become idle because enough jobs could be acquired
    triggerReconfigurationAndNextCycle();
    assertJobExecutorWaitEvent(0L);

    // another cycle of job acquisition
    // => 0 jobs are acquired
    // => acquisition should become idle
    triggerReconfigurationAndNextCycle();
    assertJobExecutorWaitEvent(BASE_IDLE_WAIT_TIME);

    // another cycle of job acquisition
    // => 0 jobs are acquired
    // => acquisition should increase idle time
    triggerReconfigurationAndNextCycle();
    assertJobExecutorWaitEvent(BASE_IDLE_WAIT_TIME * 2);

    // another cycle of job acquisition
    // => 0 jobs are acquired
    // => acquisition should increase idle time exponentially
    triggerReconfigurationAndNextCycle();
    assertJobExecutorWaitEvent(BASE_IDLE_WAIT_TIME * 4);

    // another cycle of job acquisition
    // => 0 jobs are acquired
    // => max idle time is reached
    triggerReconfigurationAndNextCycle();
    assertJobExecutorWaitEvent(MAX_IDLE_WAIT_TIME);
  }

  protected void triggerReconfigurationAndNextCycle() {
    acquisitionThread.makeContinueAndWaitForSync();
    acquisitionThread.makeContinueAndWaitForSync();
  }

  protected void assertJobExecutorWaitEvent(long expectedTimeout) {
    List<RecordedWaitEvent> waitEvents = jobExecutor.getAcquireJobsRunnable().getWaitEvents();
    assertEquals(1, waitEvents.size());
    assertEquals(expectedTimeout, waitEvents.get(0).getTimeBetweenAcquisitions());

    // discard wait event if successfully asserted
    waitEvents.clear();
  }

}
