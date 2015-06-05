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
package org.camunda.bpm.engine.test.metrics;

import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.management.Metrics;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.concurrency.ControllableThread;
import org.camunda.bpm.engine.variable.Variables;


/**
 * @author Thorben Lindhauer
 *
 */
public class JobExecutorMetricsTest extends AbstractMetricsTest {

  protected JobExecutor jobExecutor;

  protected void setUp() throws Exception {
    super.setUp();
    jobExecutor = processEngineConfiguration.getJobExecutor();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    processEngineConfiguration.setJobExecutor(jobExecutor);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/metrics/asyncServiceTaskProcess.bpmn20.xml")
  public void testJobAcquisitionMetricReporting() {

    // given
    for (int i = 0; i < 3; i++) {
      runtimeService.startProcessInstanceByKey("asyncServiceTaskProcess");
    }

    // when
    waitForJobExecutorToProcessAllJobs(5000);
    processEngineConfiguration.getDbMetricsReporter().reportNow();

    // then
    long acquisitionAttempts = managementService.createMetricsQuery().name(Metrics.JOB_ACQUISITION_ATTEMPT).sum();
    assertTrue(acquisitionAttempts >= 1);

    long acquiredJobs = managementService.createMetricsQuery()
        .name(Metrics.JOB_ACQUIRED_SUCCESS).sum();
    assertEquals(3, acquiredJobs);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/metrics/asyncServiceTaskProcess.bpmn20.xml")
  public void testCompetingJobAcquisitionMetricReporting() {
    // given
    for (int i = 0; i < 3; i++) {
      runtimeService.startProcessInstanceByKey("asyncServiceTaskProcess");
    }

    // replace job executor
    ControllableJobExecutor jobExecutor1 = new ControllableJobExecutor((ProcessEngineImpl) processEngine);
    ControllableJobExecutor jobExecutor2 = new ControllableJobExecutor((ProcessEngineImpl) processEngine);

    ControllableThread jobAcquisitionThread1 = jobExecutor1.getJobAcquisitionThread();
    ControllableThread jobAcquisitionThread2 = jobExecutor2.getJobAcquisitionThread();

    // when both executors are waiting to finish acquisition
    jobExecutor1.start();
    jobExecutor2.start();

    // thread 1 is able to acquire all jobs
    jobAcquisitionThread1.returnControlToControllableThreadAndWait();
    // thread 2 cannot acquire any jobs since they have been locked (and executed) by thread1 meanwhile
    jobAcquisitionThread2.returnControlToControllableThreadAndWait();

    processEngineConfiguration.getDbMetricsReporter().reportNow();

    // then
    long acquisitionAttempts = managementService.createMetricsQuery().name(Metrics.JOB_ACQUISITION_ATTEMPT).sum();
    // each job executor twice (since the controllable thread always waits when already acquiring jobs)
    assertEquals(2 + 2, acquisitionAttempts);

    long acquiredJobs = managementService.createMetricsQuery()
        .name(Metrics.JOB_ACQUIRED_SUCCESS).sum();
    assertEquals(3, acquiredJobs);

    long acquiredJobsFailure = managementService.createMetricsQuery()
        .name(Metrics.JOB_ACQUIRED_FAILURE).sum();
    assertEquals(3, acquiredJobsFailure);

    // cleanup
    jobExecutor1.shutdown();
    jobExecutor2.shutdown();

    processEngineConfiguration.getDbMetricsReporter().reportNow();
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/metrics/asyncServiceTaskProcess.bpmn20.xml")
  public void testJobExecutionMetricReporting() {
    // given
    for (int i = 0; i < 3; i++) {
      runtimeService.startProcessInstanceByKey("asyncServiceTaskProcess");
    }
    for (int i = 0; i < 2; i++) {
      runtimeService.startProcessInstanceByKey("asyncServiceTaskProcess",
          Variables.createVariables().putValue("fail", true));
    }

    // when
    waitForJobExecutorToProcessAllJobs(5000);

    // then
    long jobsSuccessful = managementService.createMetricsQuery().name(Metrics.JOB_SUCCESSFUL).sum();
    assertEquals(3, jobsSuccessful);

    long jobsFailed = managementService.createMetricsQuery().name(Metrics.JOB_FAILED).sum();
    // 2 jobs * 3 tries
    assertEquals(6, jobsFailed);

    long jobCandidatesForAcquisition = managementService.createMetricsQuery()
        .name(Metrics.JOB_ACQUIRED_SUCCESS).sum();
    assertEquals(3 + 6, jobCandidatesForAcquisition);
  }

  @Deployment
  public void testJobExecutionMetricExclusiveFollowUp() {
    // given
    for (int i = 0; i < 3; i++) {
      runtimeService.startProcessInstanceByKey("exclusiveServiceTasksProcess");
    }

    // when
    waitForJobExecutorToProcessAllJobs(5000);

    // then
    long jobsSuccessful = managementService.createMetricsQuery().name(Metrics.JOB_SUCCESSFUL).sum();
    assertEquals(6, jobsSuccessful);

    long jobsFailed = managementService.createMetricsQuery().name(Metrics.JOB_FAILED).sum();
    assertEquals(0, jobsFailed);

    // the respective follow-up jobs are exclusive and have been executed right away without
    // acquisition
    long jobCandidatesForAcquisition = managementService.createMetricsQuery()
        .name(Metrics.JOB_ACQUIRED_SUCCESS).sum();
    assertEquals(3, jobCandidatesForAcquisition);

    long exclusiveFollowupJobs = managementService.createMetricsQuery()
        .name(Metrics.JOB_LOCKED_EXCLUSIVE).sum();
    assertEquals(3, exclusiveFollowupJobs);
  }

}
