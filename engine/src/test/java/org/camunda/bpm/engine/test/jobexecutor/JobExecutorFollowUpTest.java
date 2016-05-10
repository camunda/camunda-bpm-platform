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

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.concurrency.ConcurrencyTestCase.ThreadControl;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * Test cases for handling of new jobs created while a job is executed
 *
 * @author Thorben Lindhauer
 */
public class JobExecutorFollowUpTest {

  protected static final BpmnModelInstance TWO_TASKS_PROCESS = Bpmn.createExecutableProcess("process")
    .startEvent()
    .serviceTask("serviceTask1")
      .camundaAsyncBefore()
      .camundaClass(SyncDelegate.class.getName())
    .serviceTask("serviceTask2")
      .camundaAsyncBefore()
      .camundaClass(SyncDelegate.class.getName())
    .endEvent()
    .done();

  protected static final BpmnModelInstance CALL_ACTIVITY_PROCESS = Bpmn.createExecutableProcess("callActivityProcess")
      .startEvent()
      .callActivity("callActivity")
        .camundaAsyncBefore()
        .calledElement("oneTaskProcess")
      .endEvent()
      .done();

  protected static final BpmnModelInstance ONE_TASK_PROCESS = Bpmn.createExecutableProcess("oneTaskProcess")
      .startEvent()
      .userTask("serviceTask")
        .camundaAsyncBefore()
      .endEvent()
      .done();

  protected ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule() {
    @Override
    public ProcessEngineConfiguration configureEngine(ProcessEngineConfigurationImpl configuration) {
      return configuration.setJobExecutor(buildControllableJobExecutor());
    }
  };
  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  protected ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);

  protected static ControllableJobExecutor buildControllableJobExecutor() {
    ControllableJobExecutor jobExecutor = new ControllableJobExecutor();
    jobExecutor.setMaxJobsPerAcquisition(2);
    jobExecutor.proceedAndWaitOnShutdown(false);
    return jobExecutor;
  }

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(bootstrapRule).around(engineRule).around(testHelper);

  protected ControllableJobExecutor jobExecutor;
  protected ThreadControl acquisitionThread;
  protected static ThreadControl executionThread;

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
  public void testExecuteExclusiveFollowUpJobInSameProcessInstance() {
    testHelper.deploy(TWO_TASKS_PROCESS);

    // given
    // a process instance with a single job
    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceByKey("process");

    jobExecutor.start();

    // and first job acquisition that acquires the job
    acquisitionThread.waitForSync();
    acquisitionThread.makeContinueAndWaitForSync();
    // and first job execution
    acquisitionThread.makeContinue();

    // waiting inside delegate
    executionThread.waitForSync();

    // completing delegate
    executionThread.makeContinueAndWaitForSync();

    // then
    // the follow-up job should be executed right away
    // i.e., there is a transition instance for the second service task
    ActivityInstance activityInstance = engineRule.getRuntimeService().getActivityInstance(processInstance.getId());
    Assert.assertEquals(1, activityInstance.getTransitionInstances("serviceTask2").length);

    // and the corresponding job is locked
    JobEntity followUpJob = (JobEntity) engineRule.getManagementService().createJobQuery().singleResult();
    Assert.assertNotNull(followUpJob);
    Assert.assertNotNull(followUpJob.getLockOwner());
    Assert.assertNotNull(followUpJob.getLockExpirationTime());

    // and the job can be completed successfully such that the process instance ends
    executionThread.makeContinue();
    acquisitionThread.waitForSync();

    // and the process instance has finished
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testExecuteExclusiveFollowUpJobInDifferentProcessInstance() {
    testHelper.deploy(CALL_ACTIVITY_PROCESS, ONE_TASK_PROCESS);

    // given
    // a process instance with a single job
    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceByKey("callActivityProcess");

    jobExecutor.start();

    // and first job acquisition that acquires the job
    acquisitionThread.waitForSync();
    acquisitionThread.makeContinueAndWaitForSync();
    // and job is executed
    acquisitionThread.makeContinueAndWaitForSync();

    // then
    // the called instance has been created
    ProcessInstance calledInstance = engineRule.getRuntimeService()
      .createProcessInstanceQuery()
      .superProcessInstanceId(processInstance.getId())
      .singleResult();
    Assert.assertNotNull(calledInstance);

    // and there is a transition instance for the service task
    ActivityInstance activityInstance = engineRule.getRuntimeService().getActivityInstance(calledInstance.getId());
    Assert.assertEquals(1, activityInstance.getTransitionInstances("serviceTask").length);

    // but the corresponding job is not locked
    JobEntity followUpJob = (JobEntity) engineRule.getManagementService().createJobQuery().singleResult();
    Assert.assertNotNull(followUpJob);
    Assert.assertNull(followUpJob.getLockOwner());
    Assert.assertNull(followUpJob.getLockExpirationTime());
  }


  public static class SyncDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
      executionThread.sync();
    }
  }
}
