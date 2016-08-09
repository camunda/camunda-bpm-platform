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


import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.concurrency.ConcurrencyTestCase.ThreadControl;
import org.camunda.bpm.engine.test.jobexecutor.RecordingAcquireJobsRunnable.RecordedWaitEvent;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

/**
 * @author Thorben Lindhauer
 *
 */
public class JobAcquisitionBackoffIdleTest {

  public static final int BASE_IDLE_WAIT_TIME = 5000;
  public static final int MAX_IDLE_WAIT_TIME = 60000;

  protected ControllableJobExecutor jobExecutor;
  protected ThreadControl acquisitionThread;

  protected ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule() {
    public ProcessEngineConfiguration configureEngine(ProcessEngineConfigurationImpl configuration) {
      jobExecutor = new ControllableJobExecutor(true);
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

  protected void cycleJobAcquisitionToMaxIdleTime() {
    // cycle of job acquisition
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
    // => acquisition should increase idle time exponentially
    triggerReconfigurationAndNextCycle();
    assertJobExecutorWaitEvent(BASE_IDLE_WAIT_TIME * 8);

    // another cycle of job acquisition
    // => 0 jobs are acquired
    // => acquisition should increase to max idle time
    triggerReconfigurationAndNextCycle();
    assertJobExecutorWaitEvent(MAX_IDLE_WAIT_TIME);
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

    cycleJobAcquisitionToMaxIdleTime();
  }

  protected void initAcquisitionAndIdleToMaxTime() {
    // start job acquisition - waiting before acquiring jobs
    jobExecutor.start();
    acquisitionThread.waitForSync();

    //cycle acquistion till max idle time is reached
    cycleJobAcquisitionToMaxIdleTime();
  }

  protected  void cycleAcquisitionAndAssertAfterJobExecution(JobQuery jobQuery) {
    // another cycle of job acquisition after acuqisition idle was reseted
    // => 1 jobs are acquired
    triggerReconfigurationAndNextCycle();
    assertJobExecutorWaitEvent(0);

    // we have no timer to fire
    assertEquals(0, jobQuery.count());

    // and we are in the second state
    assertEquals(1L, engineRule.getTaskService().createTaskQuery().count());
    Task task = engineRule.getTaskService().createTaskQuery().orderByTaskName().desc().singleResult();
    assertEquals("Next Task", task.getName());
    // complete the task and end the execution
    engineRule.getTaskService().complete(task.getId());
  }

  public interface JobCreationInCycle {

    public ProcessInstance createJobAndContinueCycle();
  }

  public void testIdlingWithHint(JobCreationInCycle jobCreationInCycle) {
    initAcquisitionAndIdleToMaxTime();

    final Date startTime = new Date();
    ProcessInstance procInstance = jobCreationInCycle.createJobAndContinueCycle();

     // After process start, there should be 1 timer created
    Task task1 = engineRule.getTaskService().createTaskQuery().singleResult();
    assertEquals("Timer Task", task1.getName());
    //and one job
    JobQuery jobQuery = engineRule.getManagementService().createJobQuery().processInstanceId(
            procInstance.getId());
    Job job = jobQuery.singleResult();
    assertNotNull(job);

    // the hint of the added job resets the idle time
    // => 0 jobs are acquired so we had to wait BASE IDLE TIME
    //after this time we can acquire the timer
    triggerReconfigurationAndNextCycle();
    assertJobExecutorWaitEvent(BASE_IDLE_WAIT_TIME);

    //time is increased so timer is found
    ClockUtil.setCurrentTime(new Date(startTime.getTime() + BASE_IDLE_WAIT_TIME));
    //now we are able to acquire the job
    cycleAcquisitionAndAssertAfterJobExecution(jobQuery);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/jobexecutor/JobAcquisitionBackoffIdleTest.testShortTimerOnUserTaskWithExpression.bpmn20.xml")
  public void testIdlingWithHintOnSuspend() {
    testIdlingWithHint(new JobCreationInCycle() {
      @Override
      public ProcessInstance createJobAndContinueCycle() {
        //continue sync before acquire
        acquisitionThread.makeContinueAndWaitForSync();
        //continue sync after acquire
        acquisitionThread.makeContinueAndWaitForSync();

        //process is started with timer boundary event which should start after 3 seconds
        ProcessInstance procInstance = engineRule.getRuntimeService().startProcessInstanceByKey("timer-example");
        //release suspend sync
        acquisitionThread.makeContinueAndWaitForSync();
        //assert max idle time and clear events
        assertJobExecutorWaitEvent(MAX_IDLE_WAIT_TIME);

        //trigger continue and assert that new acquisition cycle was triggered right after the hint
        triggerReconfigurationAndNextCycle();
        assertJobExecutorWaitEvent(0);
        return procInstance;
      }
    });
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/jobexecutor/JobAcquisitionBackoffIdleTest.testShortTimerOnUserTaskWithExpression.bpmn20.xml")
  public void testIdlingWithHintOnAquisition() {
    testIdlingWithHint(new JobCreationInCycle() {
      @Override
      public ProcessInstance createJobAndContinueCycle() {
        //continue sync before acquire
        acquisitionThread.makeContinueAndWaitForSync();

        //process is started with timer boundary event which should start after 3 seconds
        ProcessInstance procInstance = engineRule.getRuntimeService().startProcessInstanceByKey("timer-example");

        //continue sync after acquire
        acquisitionThread.makeContinueAndWaitForSync();
        //release suspend sync
        acquisitionThread.makeContinueAndWaitForSync();
        //assert max idle time and clear events
        assertJobExecutorWaitEvent(0);
        return procInstance;
      }
    });
  }


  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/jobexecutor/JobAcquisitionBackoffIdleTest.testShortTimerOnUserTaskWithExpression.bpmn20.xml")
  public void testIdlingWithHintBeforeAquisition() {
    testIdlingWithHint(new JobCreationInCycle() {
      @Override
      public ProcessInstance createJobAndContinueCycle() {
        //process is started with timer boundary event which should start after 3 seconds
        ProcessInstance procInstance = engineRule.getRuntimeService().startProcessInstanceByKey("timer-example");

        //continue sync before acquire
        acquisitionThread.makeContinueAndWaitForSync();
        //continue sync after acquire
        acquisitionThread.makeContinueAndWaitForSync();
        //release suspend sync
        acquisitionThread.makeContinueAndWaitForSync();
        //assert max idle time and clear events
        assertJobExecutorWaitEvent(0);
        return procInstance;
      }
    });
  }

  protected void triggerReconfigurationAndNextCycle() {
    acquisitionThread.makeContinueAndWaitForSync();
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
