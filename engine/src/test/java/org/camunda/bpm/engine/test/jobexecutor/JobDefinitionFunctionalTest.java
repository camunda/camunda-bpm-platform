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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.AcquireJobsCmd;
import org.camunda.bpm.engine.impl.jobexecutor.AcquiredJobs;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;

/**
 * @author Daniel Meyer
 *
 */
public class JobDefinitionFunctionalTest {

  static Logger LOG = ProcessEngineLogger.TEST_LOGGER.getLogger();

  @Rule
  public ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  @Rule
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  protected RuntimeService runtimeService;
  protected ManagementService managementService;
  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  protected static final BpmnModelInstance SIMPLE_ASYNC_PROCESS = Bpmn.createExecutableProcess("simpleAsyncProcess")
      .startEvent()
      .serviceTask()
        .camundaExpression("${true}")
        .camundaAsyncBefore()
      .endEvent()
      .done();

  @Before
  public void initServices() {
    runtimeService = engineRule.getRuntimeService();
    managementService = engineRule.getManagementService();
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
  }

  @Test
  public void testCreateJobInstanceSuspended() {
    testRule.deploy(SIMPLE_ASYNC_PROCESS);

    // given suspended job definition:
    managementService.suspendJobDefinitionByProcessDefinitionKey("simpleAsyncProcess");

    // if I start a new instance
    runtimeService.startProcessInstanceByKey("simpleAsyncProcess");

    // then the new job instance is created as suspended:
    assertNotNull(managementService.createJobQuery().suspended().singleResult());
    assertNull(managementService.createJobQuery().active().singleResult());
  }

  @Test
  public void testCreateJobInstanceActive() {
    testRule.deploy(SIMPLE_ASYNC_PROCESS);

    // given that the job definition is not suspended:

    // if I start a new instance
    runtimeService.startProcessInstanceByKey("simpleAsyncProcess");

    // then the new job instance is created as active:
    assertNull(managementService.createJobQuery().suspended().singleResult());
    assertNotNull(managementService.createJobQuery().active().singleResult());
  }

  @Test
  public void testJobExecutorOnlyAcquiresActiveJobs() {
    testRule.deploy(SIMPLE_ASYNC_PROCESS);

    // given suspended job definition:
    managementService.suspendJobDefinitionByProcessDefinitionKey("simpleAsyncProcess");

    // if I start a new instance
    runtimeService.startProcessInstanceByKey("simpleAsyncProcess");

    // then the new job executor will not acquire the job:
    AcquiredJobs acquiredJobs = acquireJobs();
    assertEquals(0, acquiredJobs.size());

    // -------------------------

    // given a active job definition:
    managementService.activateJobDefinitionByProcessDefinitionKey("simpleAsyncProcess", true);

    // then the new job executor will not acquire the job:
    acquiredJobs = acquireJobs();
    assertEquals(1, acquiredJobs.size());
  }

  @Test
  public void testExclusiveJobs() {
    testRule.deploy(Bpmn.createExecutableProcess("testProcess")
        .startEvent()
        .serviceTask("task1")
          .camundaExpression("${true}")
          .camundaAsyncBefore()
        .serviceTask("task2")
          .camundaExpression("${true}")
          .camundaAsyncBefore()
        .endEvent()
        .done());

    JobDefinition jobDefinition = managementService.createJobDefinitionQuery()
      .activityIdIn("task2")
      .singleResult();

    // given that the second task is suspended
    managementService.suspendJobDefinitionById(jobDefinition.getId());

    // if I start a process instance
    runtimeService.startProcessInstanceByKey("testProcess");

    testRule.waitForJobExecutorToProcessAllJobs(10000);

    // then the second task is not executed
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());
    // there is a suspended job instance
    Job job = managementService.createJobQuery()
      .singleResult();
    assertEquals(job.getJobDefinitionId(), jobDefinition.getId());
    assertTrue(job.isSuspended());

    // if I unsuspend the job definition, the job is executed:
    managementService.activateJobDefinitionById(jobDefinition.getId(), true);

    testRule.waitForJobExecutorToProcessAllJobs(10000);

    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }

  protected AcquiredJobs acquireJobs() {
    JobExecutor jobExecutor = processEngineConfiguration.getJobExecutor();

    return processEngineConfiguration.getCommandExecutorTxRequired()
      .execute(new AcquireJobsCmd(jobExecutor));
  }

}
