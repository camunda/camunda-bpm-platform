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

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.concurrency.ConcurrencyTestCase.ThreadControl;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
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
public class JobExecutorShutdownTest {

  protected static final BpmnModelInstance TWO_ASYNC_TASKS = Bpmn.createExecutableProcess("process")
      .startEvent()
      .serviceTask("task1")
      .camundaClass(SyncDelegate.class.getName())
      .camundaAsyncBefore()
      .camundaExclusive(true)
      .serviceTask("task2")
      .camundaClass(SyncDelegate.class.getName())
      .camundaAsyncBefore()
      .camundaExclusive(true)
      .endEvent()
      .done();

  protected static final BpmnModelInstance SINGLE_ASYNC_TASK = Bpmn.createExecutableProcess("process")
      .startEvent()
      .serviceTask("task1")
      .camundaClass(SyncDelegate.class.getName())
      .camundaAsyncBefore()
      .camundaExclusive(true)
      .endEvent()
      .done();

  protected ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule() {
    @Override
    public ProcessEngineConfiguration configureEngine(ProcessEngineConfigurationImpl configuration) {
      return configuration.setJobExecutor(buildControllableJobExecutor());
    }
  };
  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(bootstrapRule).around(engineRule);

  protected ControllableJobExecutor jobExecutor;
  protected ThreadControl acquisitionThread;
  protected static ThreadControl executionThread;

  protected static ControllableJobExecutor buildControllableJobExecutor() {
    ControllableJobExecutor jobExecutor = new ControllableJobExecutor();
    jobExecutor.setMaxJobsPerAcquisition(2);
    jobExecutor.proceedAndWaitOnShutdown(false);
    return jobExecutor;
  }

  @Before
  public void setUp() throws Exception {
    jobExecutor = (ControllableJobExecutor)
        ((ProcessEngineConfigurationImpl) engineRule.getProcessEngine().getProcessEngineConfiguration()).getJobExecutor();
    jobExecutor.setMaxJobsPerAcquisition(2);
    acquisitionThread = jobExecutor.getAcquisitionThreadControl();
    executionThread = jobExecutor.getExecutionThreadControl();
  }

  @After
  public void shutdownJobExecutor() {
    jobExecutor.shutdown();
  }

  @Test
  public void testConcurrentShutdownAndExclusiveFollowUpJob() {
    // given
    Deployment deployment = engineRule.getRepositoryService()
        .createDeployment()
        .addModelInstance("foo.bpmn", TWO_ASYNC_TASKS)
        .deploy();
    engineRule.manageDeployment(deployment);

    engineRule.getRuntimeService().startProcessInstanceByKey("process");

    Job firstAsyncJob = engineRule.getManagementService().createJobQuery().singleResult();

    jobExecutor.start();

    // wait before acquisition
    acquisitionThread.waitForSync();
    // wait for no more acquisition syncs
    acquisitionThread.ignoreFutureSyncs();
    acquisitionThread.makeContinue();

    // when waiting during execution of first job
    executionThread.waitForSync();

    // and shutting down the job executor
    jobExecutor.shutdown();

    // and continuing job execution
    executionThread.waitUntilDone();

    // then the current job has completed successfully
    Assert.assertEquals(0, engineRule.getManagementService().createJobQuery().jobId(firstAsyncJob.getId()).count());

    // but the exclusive follow-up job is not executed and is not locked
    JobEntity secondAsyncJob = (JobEntity) engineRule.getManagementService().createJobQuery().singleResult();
    Assert.assertNotNull(secondAsyncJob);
    Assert.assertFalse(secondAsyncJob.getId().equals(firstAsyncJob.getId()));
    Assert.assertNull(secondAsyncJob.getLockOwner());
    Assert.assertNull(secondAsyncJob.getLockExpirationTime());

  }

  @Test
  public void testShutdownAndMultipleLockedJobs() {
    // given
    Deployment deployment = engineRule.getRepositoryService()
        .createDeployment()
        .addModelInstance("foo.bpmn", SINGLE_ASYNC_TASK)
        .deploy();
    engineRule.manageDeployment(deployment);

    // add two jobs by starting two process instances
    engineRule.getRuntimeService().startProcessInstanceByKey("process");
    engineRule.getRuntimeService().startProcessInstanceByKey("process");

    jobExecutor.start();

    // wait before acquisition
    acquisitionThread.waitForSync();
    // wait for no more acquisition syncs
    acquisitionThread.ignoreFutureSyncs();

    acquisitionThread.makeContinue();

    // when waiting during execution of first job
    executionThread.waitForSync();

    // jobs must now be locked
    List<Job> lockedJobList = engineRule.getManagementService().createJobQuery().list();
    Assert.assertEquals(2, lockedJobList.size());
    for(Job job : lockedJobList) {
      JobEntity jobEntity = (JobEntity)job;
      Assert.assertNotNull(jobEntity.getLockOwner());
    }

    // shut down the job executor while first job is executing
    jobExecutor.shutdown();

    // then let first job continue
    executionThread.waitUntilDone();

    // check that only one job left, which is not executed nor locked
    JobEntity jobEntity = (JobEntity) engineRule.getManagementService().createJobQuery().singleResult();
    Assert.assertNotNull(jobEntity);
    Assert.assertTrue(lockedJobList.get(1).getId().equals(jobEntity.getId()) || lockedJobList.get(0).getId().equals(jobEntity.getId()));
    Assert.assertNull(jobEntity.getLockOwner());
    Assert.assertNull(jobEntity.getLockExpirationTime());
  }


  public static class SyncDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
      executionThread.sync();
    }

  }


}
