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
package org.camunda.bpm.engine.test.api.mgmt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.TimerActivateJobDefinitionHandler;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.management.JobDefinitionQuery;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.After;
import org.junit.Test;

public class ActivateJobDefinitionTest extends PluggableProcessEngineTest {

  @After
  public void tearDown() throws Exception {
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        commandContext.getHistoricJobLogManager().deleteHistoricJobLogsByHandlerType(TimerActivateJobDefinitionHandler.TYPE);
        return null;
      }
    });
  }

  // Test ManagementService#activateJobDefinitionById() /////////////////////////

  @Test
  public void testActivationById_shouldThrowProcessEngineException() {
    try {
      managementService.activateJobDefinitionById(null);
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }
  }

  @Test
  public void testActivationByIdAndActivateJobsFlag_shouldThrowProcessEngineException() {
    try {
      managementService.activateJobDefinitionById(null, false);
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }

    try {
      managementService.activateJobDefinitionById(null, true);
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }
  }

  @Test
  public void testActivationByIdAndActivateJobsFlagAndExecutionDate_shouldThrowProcessEngineException() {
    try {
      managementService.activateJobDefinitionById(null, false, null);
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }

    try {
      managementService.activateJobDefinitionById(null, true, null);
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }

    try {
      managementService.activateJobDefinitionById(null, false, new Date());
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }

    try {
      managementService.activateJobDefinitionById(null, true, new Date());
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testActivationById_shouldRetainJobs() {
    // given
    // a deployed process definition with asynchronous continuation

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();
    // ...which will be suspended with the corresponding jobs
    managementService.suspendJobDefinitionByProcessDefinitionKey("suspensionProcess", true);

    // when
    // activate the job definition
    managementService.activateJobDefinitionById(jobDefinition.getId());

    // then
    // there exists a active job definition
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(1, jobDefinitionQuery.active().count());
    assertEquals(0, jobDefinitionQuery.suspended().count());

    JobDefinition activeJobDefinition = jobDefinitionQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), activeJobDefinition.getId());

    // the corresponding job is still suspended
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.active().count());
    assertEquals(1, jobQuery.suspended().count());

    Job suspendedJob = jobQuery.singleResult();

    assertEquals(jobDefinition.getId(), suspendedJob.getJobDefinitionId());
    assertTrue(suspendedJob.isSuspended());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testActivationByIdAndActivateJobsFlag_shouldRetainJobs() {
    // given
    // a deployed process definition with asynchronous continuation

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();
    // ...which will be suspended with the corresponding jobs
    managementService.suspendJobDefinitionByProcessDefinitionKey("suspensionProcess", true);

    // when
    // activate the job definition
    managementService.activateJobDefinitionById(jobDefinition.getId(), false);

    // then
    // there exists a active job definition
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(0, jobDefinitionQuery.suspended().count());
    assertEquals(1, jobDefinitionQuery.active().count());

    JobDefinition activeJobDefinition = jobDefinitionQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), activeJobDefinition.getId());
    assertFalse(activeJobDefinition.isSuspended());

    // the corresponding job is still suspended
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.active().count());
    assertEquals(1, jobQuery.suspended().count());

    Job suspendedJob = jobQuery.singleResult();

    assertEquals(jobDefinition.getId(), suspendedJob.getJobDefinitionId());
    assertTrue(suspendedJob.isSuspended());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testActivationByIdAndActivateJobsFlag_shouldSuspendJobs() {
    // given
    // a deployed process definition with asynchronous continuation

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();
    // ...which will be suspended with the corresponding jobs
    managementService.suspendJobDefinitionByProcessDefinitionKey("suspensionProcess", true);

    // when
    // activate the job definition
    managementService.activateJobDefinitionById(jobDefinition.getId(), true);

    // then
    // there exists an active job definition...
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(0, jobDefinitionQuery.suspended().count());
    assertEquals(1, jobDefinitionQuery.active().count());

    JobDefinition activeJobDefinition = jobDefinitionQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), activeJobDefinition.getId());
    assertFalse(activeJobDefinition.isSuspended());

    // ...and a active job of the provided job definition
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.suspended().count());
    assertEquals(1, jobQuery.active().count());

    Job activeJob = jobQuery.singleResult();

    assertEquals(jobDefinition.getId(), activeJob.getJobDefinitionId());
    assertFalse(activeJob.isSuspended());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testActivationById_shouldExecuteImmediatelyAndRetainJobs() {
    // given
    // a deployed process definition with asynchronous continuation

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();
    // ...which will be suspended with the corresponding jobs
    managementService.suspendJobDefinitionByProcessDefinitionKey("suspensionProcess", true);

    // when
    // activate the job definition
    managementService.activateJobDefinitionById(jobDefinition.getId(), false, null);

    // then
    // there exists an active job definition
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(0, jobDefinitionQuery.suspended().count());
    assertEquals(1, jobDefinitionQuery.active().count());

    JobDefinition activeJobDefinition = jobDefinitionQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), activeJobDefinition.getId());
    assertFalse(activeJobDefinition.isSuspended());

    // the corresponding job is still suspended
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.active().count());
    assertEquals(1, jobQuery.suspended().count());

    Job suspendedJob = jobQuery.singleResult();

    assertEquals(jobDefinition.getId(), suspendedJob.getJobDefinitionId());
    assertTrue(suspendedJob.isSuspended());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testActivationById_shouldExecuteImmediatelyAndSuspendJobs() {
    // given
    // a deployed process definition with asynchronous continuation

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();
    // ...which will be suspended with the corresponding jobs
    managementService.suspendJobDefinitionByProcessDefinitionKey("suspensionProcess", true);

    // when
    // activate the job definition
    managementService.activateJobDefinitionById(jobDefinition.getId(), true, null);

    // then
    // there exists an active job definition...
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(0, jobDefinitionQuery.suspended().count());
    assertEquals(1, jobDefinitionQuery.active().count());

    JobDefinition activeJobDefinition = jobDefinitionQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), activeJobDefinition.getId());
    assertFalse(activeJobDefinition.isSuspended());

    // ...and an active job of the provided job definition
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.suspended().count());
    assertEquals(1, jobQuery.active().count());

    Job activeJob = jobQuery.singleResult();

    assertEquals(jobDefinition.getId(), activeJob.getJobDefinitionId());
    assertFalse(activeJob.isSuspended());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testActivationById_shouldExecuteDelayedAndRetainJobs() {
    // given
    // a deployed process definition with asynchronous continuation

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();
    // ...which will be suspended with the corresponding jobs
    managementService.suspendJobDefinitionByProcessDefinitionKey("suspensionProcess", true);

    // when
    // activate the job definition
    managementService.activateJobDefinitionById(jobDefinition.getId(), false, oneWeekLater());

    // then
    // the job definition is still suspended
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();
    assertEquals(0, jobDefinitionQuery.active().count());
    assertEquals(1, jobDefinitionQuery.suspended().count());

    // there exists a job for the delayed activation execution
    JobQuery jobQuery = managementService.createJobQuery();

    Job delayedActivationJob = jobQuery.timers().active().singleResult();
    assertNotNull(delayedActivationJob);
    String deploymentId = repositoryService.createProcessDefinitionQuery()
        .processDefinitionId(jobDefinition.getProcessDefinitionId()).singleResult().getDeploymentId();
    assertThat(delayedActivationJob.getDeploymentId()).isEqualTo(deploymentId);

    // execute job
    managementService.executeJob(delayedActivationJob.getId());

    // the job definition should be suspended
    assertEquals(1, jobDefinitionQuery.active().count());
    assertEquals(0, jobDefinitionQuery.suspended().count());

    JobDefinition activeJobDefinition = jobDefinitionQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), activeJobDefinition.getId());
    assertFalse(activeJobDefinition.isSuspended());

    // the corresponding job is still suspended
    jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.active().count());
    assertEquals(1, jobQuery.suspended().count());

    Job suspendedJob = jobQuery.singleResult();

    assertEquals(jobDefinition.getId(), suspendedJob.getJobDefinitionId());
    assertTrue(suspendedJob.isSuspended());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testActivationById_shouldExecuteDelayedAndSuspendJobs() {
    // given
    // a deployed process definition with asynchronous continuation

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();
    // ...which will be suspended with the corresponding jobs
    managementService.suspendJobDefinitionByProcessDefinitionKey("suspensionProcess", true);

    // when
    // activate the job definition
    managementService.activateJobDefinitionById(jobDefinition.getId(), true, oneWeekLater());

    // then
    // the job definition is still suspended
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();
    assertEquals(0, jobDefinitionQuery.active().count());
    assertEquals(1, jobDefinitionQuery.suspended().count());

    // there exists a job for the delayed activation execution
    JobQuery jobQuery = managementService.createJobQuery();

    Job delayedActivationJob = jobQuery.timers().active().singleResult();
    assertNotNull(delayedActivationJob);
    String deploymentId = repositoryService.createProcessDefinitionQuery()
        .processDefinitionId(jobDefinition.getProcessDefinitionId()).singleResult().getDeploymentId();
    assertThat(delayedActivationJob.getDeploymentId()).isEqualTo(deploymentId);

    // execute job
    managementService.executeJob(delayedActivationJob.getId());

    // the job definition should be active
    assertEquals(1, jobDefinitionQuery.active().count());
    assertEquals(0, jobDefinitionQuery.suspended().count());

    JobDefinition activeJobDefinition = jobDefinitionQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), activeJobDefinition.getId());
    assertFalse(activeJobDefinition.isSuspended());

    // the corresponding job is active
    jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.suspended().count());
    assertEquals(1, jobQuery.active().count());

    Job activeJob = jobQuery.singleResult();

    assertEquals(jobDefinition.getId(), activeJob.getJobDefinitionId());
    assertFalse(activeJob.isSuspended());
  }

  // Test ManagementService#activateJobDefinitionByProcessDefinitionId() /////////////////////////

  @Test
  public void testActivationByProcessDefinitionId_shouldThrowProcessEngineException() {
    try {
      managementService.activateJobDefinitionByProcessDefinitionId(null);
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }
  }

  @Test
  public void testActivationByProcessDefinitionIdAndActivateJobsFlag_shouldThrowProcessEngineException() {
    try {
      managementService.activateJobDefinitionByProcessDefinitionId(null, false);
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }

    try {
      managementService.activateJobDefinitionByProcessDefinitionId(null, true);
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }
  }

  @Test
  public void testActivationByProcessDefinitionIdAndActivateJobsFlagAndExecutionDate_shouldThrowProcessEngineException() {
    try {
      managementService.activateJobDefinitionByProcessDefinitionId(null, false, null);
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }

    try {
      managementService.activateJobDefinitionByProcessDefinitionId(null, true, null);
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }

    try {
      managementService.activateJobDefinitionByProcessDefinitionId(null, false, new Date());
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }

    try {
      managementService.activateJobDefinitionByProcessDefinitionId(null, true, new Date());
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testActivationByProcessDefinitionId_shouldRetainJobs() {
    // given
    // a deployed process definition with asynchronous continuation
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();
    // ...which will be suspended with the corresponding jobs
    managementService.suspendJobDefinitionByProcessDefinitionKey("suspensionProcess", true);

    // when
    // activate the job definition
    managementService.activateJobDefinitionByProcessDefinitionId(processDefinition.getId());

    // then
    // there exists a active job definition
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(0, jobDefinitionQuery.suspended().count());
    assertEquals(1, jobDefinitionQuery.active().count());

    JobDefinition activeJobDefinition = jobDefinitionQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), activeJobDefinition.getId());

    // the corresponding job is still suspended
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.active().count());
    assertEquals(1, jobQuery.suspended().count());

    Job suspendedJob = jobQuery.suspended().singleResult();

    assertEquals(jobDefinition.getId(), suspendedJob.getJobDefinitionId());
    assertTrue(suspendedJob.isSuspended());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testActivationByProcessDefinitionIdAndActivateJobsFlag_shouldRetainJobs() {
    // given
    // a deployed process definition with asynchronous continuation
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();
    // ...which will be suspended with the corresponding jobs
    managementService.suspendJobDefinitionByProcessDefinitionKey("suspensionProcess", true);

    // when
    // activate the job definition
    managementService.activateJobDefinitionByProcessDefinitionId(processDefinition.getId(), false);

    // then
    // there exists an active job definition
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(0, jobDefinitionQuery.suspended().count());
    assertEquals(1, jobDefinitionQuery.active().count());

    JobDefinition activeJobDefinition = jobDefinitionQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), activeJobDefinition.getId());
    assertFalse(activeJobDefinition.isSuspended());

    // the corresponding job is still suspended
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.active().count());
    assertEquals(1, jobQuery.suspended().count());

    Job suspendedJob = jobQuery.suspended().singleResult();

    assertEquals(jobDefinition.getId(), suspendedJob.getJobDefinitionId());
    assertTrue(suspendedJob.isSuspended());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testActivationByProcessDefinitionIdAndActivateJobsFlag_shouldSuspendJobs() {
    // given
    // a deployed process definition with asynchronous continuation
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();
    // ...which will be suspended with the corresponding jobs
    managementService.suspendJobDefinitionByProcessDefinitionKey("suspensionProcess", true);

    // when
    // activate the job definition
    managementService.activateJobDefinitionByProcessDefinitionId(processDefinition.getId(), true);

    // then
    // there exists an active job definition...
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(0, jobDefinitionQuery.suspended().count());
    assertEquals(1, jobDefinitionQuery.active().count());

    JobDefinition activeJobDefinition = jobDefinitionQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), activeJobDefinition.getId());
    assertFalse(activeJobDefinition.isSuspended());

    // ...and an active job of the provided job definition
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.suspended().count());
    assertEquals(1, jobQuery.active().count());

    Job activeJob = jobQuery.singleResult();
    assertEquals(jobDefinition.getId(), activeJob.getJobDefinitionId());
    assertFalse(activeJob.isSuspended());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testActivationByProcessDefinitionId_shouldExecuteImmediatelyAndRetainJobs() {
    // given
    // a deployed process definition with asynchronous continuation
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();
    // ...which will be suspended with the corresponding jobs
    managementService.suspendJobDefinitionByProcessDefinitionKey("suspensionProcess", true);

    // when
    // activate the job definition
    managementService.activateJobDefinitionByProcessDefinitionId(processDefinition.getId(), false, null);

    // then
    // there exists an active job definition
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(0, jobDefinitionQuery.suspended().count());
    assertEquals(1, jobDefinitionQuery.active().count());

    JobDefinition activeJobDefinition = jobDefinitionQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), activeJobDefinition.getId());
    assertFalse(activeJobDefinition.isSuspended());

    // the corresponding job is still suspended
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(1, jobQuery.suspended().count());
    assertEquals(0, jobQuery.active().count());

    Job suspendedJob = jobQuery.suspended().singleResult();

    assertEquals(jobDefinition.getId(), suspendedJob.getJobDefinitionId());
    assertTrue(suspendedJob.isSuspended());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testActivationByProcessDefinitionId_shouldExecuteImmediatelyAndSuspendJobs() {
    // given
    // a deployed process definition with asynchronous continuation
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();
    // ...which will be suspended with the corresponding jobs
    managementService.suspendJobDefinitionByProcessDefinitionKey("suspensionProcess", true);

    // when
    // activate the job definition
    managementService.activateJobDefinitionByProcessDefinitionId(processDefinition.getId(), true, null);

    // then
    // there exists an active job definition...
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(0, jobDefinitionQuery.suspended().count());
    assertEquals(1, jobDefinitionQuery.active().count());

    JobDefinition activeJobDefinition = jobDefinitionQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), activeJobDefinition.getId());
    assertFalse(activeJobDefinition.isSuspended());

    // ...and an active job of the provided job definition
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.suspended().count());
    assertEquals(1, jobQuery.active().count());

    Job activeJob = jobQuery.singleResult();

    assertEquals(jobDefinition.getId(), activeJob.getJobDefinitionId());
    assertFalse(activeJob.isSuspended());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testActivationByProcessDefinitionId_shouldExecuteDelayedAndRetainJobs() {
    // given
    // a deployed process definition with asynchronous continuation
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();
    // ...which will be suspended with the corresponding jobs
    managementService.suspendJobDefinitionByProcessDefinitionKey("suspensionProcess", true);

    // when
    // activate the job definition
    managementService.activateJobDefinitionByProcessDefinitionId(processDefinition.getId(), false, oneWeekLater());

    // then
    // the job definition is still suspended
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();
    assertEquals(0, jobDefinitionQuery.active().count());
    assertEquals(1, jobDefinitionQuery.suspended().count());

    // there exists a job for the delayed activation execution
    JobQuery jobQuery = managementService.createJobQuery();

    Job delayedActivationJob = jobQuery.timers().active().singleResult();
    assertNotNull(delayedActivationJob);
    assertThat(delayedActivationJob.getDeploymentId()).isEqualTo(processDefinition.getDeploymentId());

    // execute job
    managementService.executeJob(delayedActivationJob.getId());

    // the job definition should be active
    assertEquals(1, jobDefinitionQuery.active().count());
    assertEquals(0, jobDefinitionQuery.suspended().count());

    JobDefinition activeJobDefinition = jobDefinitionQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), activeJobDefinition.getId());
    assertFalse(activeJobDefinition.isSuspended());

    // the corresponding job is still suspended
    jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.active().count());
    assertEquals(1, jobQuery.suspended().count());

    Job suspendedJob = jobQuery.suspended().singleResult();

    assertEquals(jobDefinition.getId(), suspendedJob.getJobDefinitionId());
    assertTrue(suspendedJob.isSuspended());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testActivationByProcessDefinitionId_shouldExecuteDelayedAndSuspendJobs() {
    // given
    // a deployed process definition with asynchronous continuation
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();
    // ...which will be suspended with the corresponding jobs
    managementService.suspendJobDefinitionByProcessDefinitionKey("suspensionProcess", true);

    // when
    // activate the job definition
    managementService.activateJobDefinitionByProcessDefinitionId(processDefinition.getId(), true, oneWeekLater());

    // then
    // the job definition is still suspended
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();
    assertEquals(0, jobDefinitionQuery.active().count());
    assertEquals(1, jobDefinitionQuery.suspended().count());

    // there exists a job for the delayed activation execution
    JobQuery jobQuery = managementService.createJobQuery();

    Job delayedActivationJob = jobQuery.timers().active().singleResult();
    assertNotNull(delayedActivationJob);
    assertThat(delayedActivationJob.getDeploymentId()).isEqualTo(processDefinition.getDeploymentId());

    // execute job
    managementService.executeJob(delayedActivationJob.getId());

    // the job definition should be active
    assertEquals(1, jobDefinitionQuery.active().count());
    assertEquals(0, jobDefinitionQuery.suspended().count());

    JobDefinition activeJobDefinition = jobDefinitionQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), activeJobDefinition.getId());
    assertFalse(activeJobDefinition.isSuspended());

    // the corresponding job is active
    jobQuery = managementService.createJobQuery();

    assertEquals(1, jobQuery.active().count());
    assertEquals(0, jobQuery.suspended().count());

    Job activeJob = jobQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), activeJob.getJobDefinitionId());
    assertFalse(activeJob.isSuspended());
  }

  // Test ManagementService#activateJobDefinitionByProcessDefinitionKey() /////////////////////////

  @Test
  public void testActivationByProcessDefinitionKey_shouldThrowProcessEngineException() {
    try {
      managementService.activateJobDefinitionByProcessDefinitionKey(null);
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }
  }

  @Test
  public void testActivationByProcessDefinitionKeyAndActivateJobsFlag_shouldThrowProcessEngineException() {
    try {
      managementService.activateJobDefinitionByProcessDefinitionKey(null, false);
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }

    try {
      managementService.activateJobDefinitionByProcessDefinitionKey(null, true);
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }
  }

  @Test
  public void testActivationByProcessDefinitionKeyAndActivateJobsFlagAndExecutionDate_shouldThrowProcessEngineException() {
    try {
      managementService.activateJobDefinitionByProcessDefinitionKey(null, false, null);
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }

    try {
      managementService.activateJobDefinitionByProcessDefinitionKey(null, true, null);
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }

    try {
      managementService.activateJobDefinitionByProcessDefinitionKey(null, false, new Date());
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }

    try {
      managementService.activateJobDefinitionByProcessDefinitionKey(null, true, new Date());
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testActivationByProcessDefinitionKey_shouldRetainJobs() {
    // given
    // a deployed process definition with asynchronous continuation
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();
    // ...which will be suspended with the corresponding jobs
    managementService.suspendJobDefinitionByProcessDefinitionKey("suspensionProcess", true);

    // when
    // activate the job definition
    managementService.activateJobDefinitionByProcessDefinitionKey(processDefinition.getKey());

    // then
    // there exists a active job definition
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(0, jobDefinitionQuery.suspended().count());
    assertEquals(1, jobDefinitionQuery.active().count());

    JobDefinition activeJobDefinition = jobDefinitionQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), activeJobDefinition.getId());

    // the corresponding job is still suspended
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(1, jobQuery.suspended().count());
    assertEquals(0, jobQuery.active().count());

    Job suspendedJob = jobQuery.suspended().singleResult();

    assertEquals(jobDefinition.getId(), suspendedJob.getJobDefinitionId());
    assertTrue(suspendedJob.isSuspended());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testActivationByProcessDefinitionKeyAndActivateJobsFlag_shouldRetainJobs() {
    // given
    // a deployed process definition with asynchronous continuation
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();
    // ...which will be suspended with the corresponding jobs
    managementService.suspendJobDefinitionByProcessDefinitionKey("suspensionProcess", true);

    // when
    // activate the job definition
    managementService.activateJobDefinitionByProcessDefinitionKey(processDefinition.getKey(), false);

    // then
    // there exists an active job definition
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(0, jobDefinitionQuery.suspended().count());
    assertEquals(1, jobDefinitionQuery.active().count());

    JobDefinition activeJobDefinition = jobDefinitionQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), activeJobDefinition.getId());
    assertFalse(activeJobDefinition.isSuspended());

    // the corresponding job is still suspended
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(1, jobQuery.suspended().count());
    assertEquals(0, jobQuery.active().count());

    Job suspendedJob = jobQuery.suspended().singleResult();

    assertEquals(jobDefinition.getId(), suspendedJob.getJobDefinitionId());
    assertTrue(suspendedJob.isSuspended());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testActivationByProcessDefinitionKeyAndActivateJobsFlag_shouldSuspendJobs() {
    // given
    // a deployed process definition with asynchronous continuation
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();
    // ...which will be suspended with the corresponding jobs
    managementService.suspendJobDefinitionByProcessDefinitionKey("suspensionProcess", true);

    // when
    // activate the job definition
    managementService.activateJobDefinitionByProcessDefinitionKey(processDefinition.getKey(), true);

    // then
    // there exists an active job definition...
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(0, jobDefinitionQuery.suspended().count());
    assertEquals(1, jobDefinitionQuery.active().count());

    JobDefinition activeJobDefinition = jobDefinitionQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), activeJobDefinition.getId());
    assertFalse(activeJobDefinition.isSuspended());

    // ...and an active job of the provided job definition
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.suspended().count());
    assertEquals(1, jobQuery.active().count());

    Job suspendedJob = jobQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), suspendedJob.getJobDefinitionId());
    assertFalse(suspendedJob.isSuspended());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testActivationByProcessDefinitionKey_shouldExecuteImmediatelyAndRetainJobs() {
    // given
    // a deployed process definition with asynchronous continuation
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();
    // ...which will be suspended with the corresponding jobs
    managementService.suspendJobDefinitionByProcessDefinitionKey("suspensionProcess", true);

    // when
    // activate the job definition
    managementService.activateJobDefinitionByProcessDefinitionKey(processDefinition.getKey(), false, null);

    // then
    // there exists an active job definition
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(0, jobDefinitionQuery.suspended().count());
    assertEquals(1, jobDefinitionQuery.active().count());

    JobDefinition activeJobDefinition = jobDefinitionQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), activeJobDefinition.getId());
    assertFalse(activeJobDefinition.isSuspended());

    // the corresponding job is still suspended
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(1, jobQuery.suspended().count());
    assertEquals(0, jobQuery.active().count());

    Job suspendedJob = jobQuery.suspended().singleResult();

    assertEquals(jobDefinition.getId(), suspendedJob.getJobDefinitionId());
    assertTrue(suspendedJob.isSuspended());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testActivationByProcessDefinitionKey_shouldExecuteImmediatelyAndSuspendJobs() {
    // given
    // a deployed process definition with asynchronous continuation
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();
    // ...which will be suspended with the corresponding jobs
    managementService.suspendJobDefinitionByProcessDefinitionKey("suspensionProcess", true);

    // when
    // activate the job definition
    managementService.activateJobDefinitionByProcessDefinitionKey(processDefinition.getKey(), true, null);

    // then
    // there exists an active job definition...
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();

    assertEquals(0, jobDefinitionQuery.suspended().count());
    assertEquals(1, jobDefinitionQuery.active().count());

    JobDefinition activeJobDefinition = jobDefinitionQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), activeJobDefinition.getId());
    assertFalse(activeJobDefinition.isSuspended());

    // ...and an active job of the provided job definition
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.suspended().count());
    assertEquals(1, jobQuery.active().count());

    Job suspendedJob = jobQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), suspendedJob.getJobDefinitionId());
    assertFalse(suspendedJob.isSuspended());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testActivationByProcessDefinitionKey_shouldExecuteDelayedAndRetainJobs() {
    // given
    // a deployed process definition with asynchronous continuation
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();
    // ...which will be suspended with the corresponding jobs
    managementService.suspendJobDefinitionByProcessDefinitionKey("suspensionProcess", true);

    // when
    // activate the job definition
    managementService.activateJobDefinitionByProcessDefinitionKey(processDefinition.getKey(), false, oneWeekLater());

    // then
    // the job definition is still suspended
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();
    assertEquals(0, jobDefinitionQuery.active().count());
    assertEquals(1, jobDefinitionQuery.suspended().count());

    // there exists a job for the delayed activation execution
    JobQuery jobQuery = managementService.createJobQuery();

    Job delayedActivationJob = jobQuery.timers().active().singleResult();
    assertNotNull(delayedActivationJob);
    assertThat(delayedActivationJob.getDeploymentId()).isEqualTo(processDefinition.getDeploymentId());

    // execute job
    managementService.executeJob(delayedActivationJob.getId());

    // the job definition should be active
    assertEquals(1, jobDefinitionQuery.active().count());
    assertEquals(0, jobDefinitionQuery.suspended().count());

    JobDefinition activeJobDefinition = jobDefinitionQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), activeJobDefinition.getId());
    assertFalse(activeJobDefinition.isSuspended());

    // the corresponding job is still suspended
    jobQuery = managementService.createJobQuery();

    assertEquals(1, jobQuery.suspended().count());
    assertEquals(0, jobQuery.active().count());

    Job suspendedJob = jobQuery.suspended().singleResult();

    assertEquals(jobDefinition.getId(), suspendedJob.getJobDefinitionId());
    assertTrue(suspendedJob.isSuspended());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testActivationByProcessDefinitionKey_shouldExecuteDelayedAndSuspendJobs() {
    // given
    // a deployed process definition with asynchronous continuation
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();
    // ...which will be suspended with the corresponding jobs
    managementService.suspendJobDefinitionByProcessDefinitionKey("suspensionProcess", true);

    // when
    // activate the job definition
    managementService.activateJobDefinitionByProcessDefinitionKey(processDefinition.getKey(), true, oneWeekLater());

    // then
    // the job definition is still suspended
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();
    assertEquals(0, jobDefinitionQuery.active().count());
    assertEquals(1, jobDefinitionQuery.suspended().count());

    // there exists a job for the delayed activation execution
    JobQuery jobQuery = managementService.createJobQuery();

    Job delayedActivationJob = jobQuery.timers().active().singleResult();
    assertNotNull(delayedActivationJob);
    assertThat(delayedActivationJob.getDeploymentId()).isEqualTo(processDefinition.getDeploymentId());

    // execute job
    managementService.executeJob(delayedActivationJob.getId());

    // the job definition should be active
    assertEquals(1, jobDefinitionQuery.active().count());
    assertEquals(0, jobDefinitionQuery.suspended().count());

    JobDefinition activeJobDefinition = jobDefinitionQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), activeJobDefinition.getId());
    assertFalse(activeJobDefinition.isSuspended());

    // the corresponding job is active
    jobQuery = managementService.createJobQuery();

    assertEquals(1, jobQuery.active().count());
    assertEquals(0, jobQuery.suspended().count());

    Job activeJob = jobQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), activeJob.getJobDefinitionId());
    assertFalse(activeJob.isSuspended());

  }

  // Test ManagementService#activateJobDefinitionByProcessDefinitionKey() with multiple process definition
  // with same process definition key

  @Test
  public void testMultipleSuspensionByProcessDefinitionKey_shouldRetainJobs() {
    // given
    String key = "suspensionProcess";

    // Deploy three processes and start for each deployment a process instance
    // with a failed job
    int nrOfProcessDefinitions = 3;
    for (int i=0; i<nrOfProcessDefinitions; i++) {
      repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn").deploy();
      Map<String, Object> params = new HashMap<>();
      params.put("fail", Boolean.TRUE);
      runtimeService.startProcessInstanceByKey(key, params);
    }

    // a job definition (which was created for the asynchronous continuation)
    // ...which will be suspended with the corresponding jobs
    managementService.suspendJobDefinitionByProcessDefinitionKey("suspensionProcess", true);

    // when
    // activate the job definition
    managementService.activateJobDefinitionByProcessDefinitionKey(key);

    // then
    // all job definitions are active
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();
    assertEquals(0, jobDefinitionQuery.suspended().count());
    assertEquals(3, jobDefinitionQuery.active().count());

    // but the jobs are still suspended
    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(3, jobQuery.suspended().count());
    assertEquals(0, jobQuery.active().count());

    // Clean DB
    for (org.camunda.bpm.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

  @Test
  public void testMultipleSuspensionByProcessDefinitionKeyAndActivateJobsFlag_shouldRetainJobs() {
    // given
    String key = "suspensionProcess";

    // Deploy three processes and start for each deployment a process instance
    // with a failed job
    int nrOfProcessDefinitions = 3;
    for (int i=0; i<nrOfProcessDefinitions; i++) {
      repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn").deploy();
      Map<String, Object> params = new HashMap<>();
      params.put("fail", Boolean.TRUE);
      runtimeService.startProcessInstanceByKey(key, params);
    }

    // a job definition (which was created for the asynchronous continuation)
    // ...which will be suspended with the corresponding jobs
    managementService.suspendJobDefinitionByProcessDefinitionKey("suspensionProcess", true);

    // when
    // activate the job definition
    managementService.activateJobDefinitionByProcessDefinitionKey(key, false);

    // then
    // all job definitions are active
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();
    assertEquals(0, jobDefinitionQuery.suspended().count());
    assertEquals(3, jobDefinitionQuery.active().count());

    // but the jobs are still suspended
    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(3, jobQuery.suspended().count());
    assertEquals(0, jobQuery.active().count());

    // Clean DB
    for (org.camunda.bpm.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

  @Test
  public void testMultipleSuspensionByProcessDefinitionKeyAndActivateJobsFlag_shouldSuspendJobs() {
    // given
    String key = "suspensionProcess";

    // Deploy three processes and start for each deployment a process instance
    // with a failed job
    int nrOfProcessDefinitions = 3;
    for (int i=0; i<nrOfProcessDefinitions; i++) {
      repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn").deploy();
      Map<String, Object> params = new HashMap<>();
      params.put("fail", Boolean.TRUE);
      runtimeService.startProcessInstanceByKey(key, params);
    }

    // a job definition (which was created for the asynchronous continuation)
    // ...which will be suspended with the corresponding jobs
    managementService.suspendJobDefinitionByProcessDefinitionKey("suspensionProcess", true);

    // when
    // activate the job definition
    managementService.activateJobDefinitionByProcessDefinitionKey(key, true);

    // then
    // all job definitions are active
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();
    assertEquals(0, jobDefinitionQuery.suspended().count());
    assertEquals(3, jobDefinitionQuery.active().count());

    // and the jobs too
    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(0, jobQuery.suspended().count());
    assertEquals(3, jobQuery.active().count());

    // Clean DB
    for (org.camunda.bpm.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }

  }

  @Test
  public void testMultipleSuspensionByProcessDefinitionKey_shouldExecuteImmediatelyAndRetainJobs() {
    // given
    String key = "suspensionProcess";

    // Deploy three processes and start for each deployment a process instance
    // with a failed job
    int nrOfProcessDefinitions = 3;
    for (int i=0; i<nrOfProcessDefinitions; i++) {
      repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn").deploy();
      Map<String, Object> params = new HashMap<>();
      params.put("fail", Boolean.TRUE);
      runtimeService.startProcessInstanceByKey(key, params);
    }

    // a job definition (which was created for the asynchronous continuation)
    // ...which will be suspended with the corresponding jobs
    managementService.suspendJobDefinitionByProcessDefinitionKey("suspensionProcess", true);

    // when
    // activate the job definition
    managementService.activateJobDefinitionByProcessDefinitionKey(key, false, null);

    // then
    // all job definitions are active
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();
    assertEquals(0, jobDefinitionQuery.suspended().count());
    assertEquals(3, jobDefinitionQuery.active().count());

    // but the jobs are still suspended
    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(3, jobQuery.suspended().count());
    assertEquals(0, jobQuery.active().count());

    // Clean DB
    for (org.camunda.bpm.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }

  }

  @Test
  public void testMultipleSuspensionByProcessDefinitionKey_shouldExecuteImmediatelyAndSuspendJobs() {
    // given
    String key = "suspensionProcess";

    // Deploy three processes and start for each deployment a process instance
    // with a failed job
    int nrOfProcessDefinitions = 3;
    for (int i=0; i<nrOfProcessDefinitions; i++) {
      repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn").deploy();
      Map<String, Object> params = new HashMap<>();
      params.put("fail", Boolean.TRUE);
      runtimeService.startProcessInstanceByKey(key, params);
    }

    // a job definition (which was created for the asynchronous continuation)
    // ...which will be suspended with the corresponding jobs
    managementService.suspendJobDefinitionByProcessDefinitionKey("suspensionProcess", true);

    // when
    // activate the job definition
    managementService.activateJobDefinitionByProcessDefinitionKey(key, true, null);

    // then
    // all job definitions are active
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();
    assertEquals(0, jobDefinitionQuery.suspended().count());
    assertEquals(3, jobDefinitionQuery.active().count());

    // and the jobs too
    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(0, jobQuery.suspended().count());
    assertEquals(3, jobQuery.active().count());

    // Clean DB
    for (org.camunda.bpm.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }

  }

  @Test
  public void testMultipleSuspensionByProcessDefinitionKey_shouldExecuteDelayedAndRetainJobs() {
    // given
    String key = "suspensionProcess";

    // Deploy three processes and start for each deployment a process instance
    // with a failed job
    int nrOfProcessDefinitions = 3;
    for (int i=0; i<nrOfProcessDefinitions; i++) {
      repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn").deploy();
      Map<String, Object> params = new HashMap<>();
      params.put("fail", Boolean.TRUE);
      runtimeService.startProcessInstanceByKey(key, params);
    }

    // a job definition (which was created for the asynchronous continuation)
    // ...which will be suspended with the corresponding jobs
    managementService.suspendJobDefinitionByProcessDefinitionKey("suspensionProcess", true);

    // when
    // activate the job definition
    managementService.activateJobDefinitionByProcessDefinitionKey(key, false, oneWeekLater());

    // then
    // the job definition is still suspended
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();
    assertEquals(0, jobDefinitionQuery.active().count());
    assertEquals(3, jobDefinitionQuery.suspended().count());

    // there exists a job for the delayed activation execution
    JobQuery jobQuery = managementService.createJobQuery();

    Job delayedActivationJob = jobQuery.timers().active().singleResult();
    assertNotNull(delayedActivationJob);
    String expectedDeploymentId = repositoryService.createProcessDefinitionQuery()
        .orderByProcessDefinitionVersion().desc().list().get(0).getDeploymentId();
    assertThat(delayedActivationJob.getDeploymentId()).isEqualTo(expectedDeploymentId);

    // execute job
    managementService.executeJob(delayedActivationJob.getId());

    // the job definition should be active
    assertEquals(3, jobDefinitionQuery.active().count());
    assertEquals(0, jobDefinitionQuery.suspended().count());

    // but the jobs are still suspended
    jobQuery = managementService.createJobQuery();
    assertEquals(3, jobQuery.suspended().count());
    assertEquals(0, jobQuery.active().count());

    // Clean DB
    for (org.camunda.bpm.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }

  }

  @Test
  public void testMultipleSuspensionByProcessDefinitionKey_shouldExecuteDelayedAndSuspendJobs() {
    // given
    String key = "suspensionProcess";

    // Deploy three processes and start for each deployment a process instance
    // with a failed job
    int nrOfProcessDefinitions = 3;
    for (int i=0; i<nrOfProcessDefinitions; i++) {
      repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn").deploy();
      Map<String, Object> params = new HashMap<>();
      params.put("fail", Boolean.TRUE);
      runtimeService.startProcessInstanceByKey(key, params);
    }

    // a job definition (which was created for the asynchronous continuation)
    // ...which will be suspended with the corresponding jobs
    managementService.suspendJobDefinitionByProcessDefinitionKey("suspensionProcess", true);

    // when
    // activate the job definition
    managementService.activateJobDefinitionByProcessDefinitionKey(key, true, oneWeekLater());

    // then
    // the job definitions are still suspended
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();
    assertEquals(0, jobDefinitionQuery.active().count());
    assertEquals(3, jobDefinitionQuery.suspended().count());

    // there exists a job for the delayed activation execution
    JobQuery jobQuery = managementService.createJobQuery();

    Job delayedActivationJob = jobQuery.timers().active().singleResult();
    assertNotNull(delayedActivationJob);
    String expectedDeploymentId = repositoryService.createProcessDefinitionQuery()
        .orderByProcessDefinitionVersion().desc().list().get(0).getDeploymentId();
    assertThat(delayedActivationJob.getDeploymentId()).isEqualTo(expectedDeploymentId);

    // execute job
    managementService.executeJob(delayedActivationJob.getId());

    // the job definition should be active
    assertEquals(3, jobDefinitionQuery.active().count());
    assertEquals(0, jobDefinitionQuery.suspended().count());

    // the corresponding jobs are active
    jobQuery = managementService.createJobQuery();

    assertEquals(3, jobQuery.active().count());
    assertEquals(0, jobQuery.suspended().count());

    // Clean DB
    for (org.camunda.bpm.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testActivationByIdUsingBuilder() {
    // given
    // a deployed process definition with asynchronous continuation

    // a running process instance with a failed job
    runtimeService.startProcessInstanceByKey("suspensionProcess",
        Variables.createVariables().putValue("fail", true));

    // a job definition (which was created for the asynchronous continuation)
    // ...which will be suspended with the corresponding jobs
    managementService.suspendJobDefinitionByProcessDefinitionKey("suspensionProcess", true);

    JobDefinitionQuery query = managementService.createJobDefinitionQuery();
    JobDefinition jobDefinition = query.singleResult();

    assertEquals(0, query.active().count());
    assertEquals(1, query.suspended().count());

    // when
    // activate the job definition
    managementService
      .updateJobDefinitionSuspensionState()
      .byJobDefinitionId(jobDefinition.getId())
      .activate();

    // then
    // there exists a active job definition
    assertEquals(1, query.active().count());
    assertEquals(0, query.suspended().count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testActivationByProcessDefinitionIdUsingBuilder() {
    // given
    // a deployed process definition with asynchronous continuation

    // a running process instance with a failed job
    runtimeService.startProcessInstanceByKey("suspensionProcess",
        Variables.createVariables().putValue("fail", true));

    // a job definition (which was created for the asynchronous continuation)
    // ...which will be suspended with the corresponding jobs
    managementService.suspendJobDefinitionByProcessDefinitionKey("suspensionProcess", true);

    JobDefinitionQuery query = managementService.createJobDefinitionQuery();
    assertEquals(0, query.active().count());
    assertEquals(1, query.suspended().count());

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // when
    // activate the job definition
    managementService
      .updateJobDefinitionSuspensionState()
      .byProcessDefinitionId(processDefinition.getId())
      .activate();

    // then
    // there exists a active job definition
    assertEquals(1, query.active().count());
    assertEquals(0, query.suspended().count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testActivationByProcessDefinitionKeyUsingBuilder() {
    // given
    // a deployed process definition with asynchronous continuation

    // a running process instance with a failed job
    runtimeService.startProcessInstanceByKey("suspensionProcess",
        Variables.createVariables().putValue("fail", true));

    // a job definition (which was created for the asynchronous continuation)
    // ...which will be suspended with the corresponding jobs
    managementService.suspendJobDefinitionByProcessDefinitionKey("suspensionProcess", true);

    JobDefinitionQuery query = managementService.createJobDefinitionQuery();
    assertEquals(0, query.active().count());
    assertEquals(1, query.suspended().count());

    // when
    // activate the job definition
    managementService
      .updateJobDefinitionSuspensionState()
      .byProcessDefinitionKey("suspensionProcess")
      .activate();

    // then
    // there exists a active job definition
    assertEquals(1, query.active().count());
    assertEquals(0, query.suspended().count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testActivationJobDefinitionIncludingJobsUsingBuilder() {
    // given
    // a deployed process definition with asynchronous continuation

    // a running process instance with a failed job
    runtimeService.startProcessInstanceByKey("suspensionProcess",
        Variables.createVariables().putValue("fail", true));

    // a job definition (which was created for the asynchronous continuation)
    // ...which will be suspended with the corresponding jobs
    managementService.suspendJobDefinitionByProcessDefinitionKey("suspensionProcess", true);

    JobDefinitionQuery query = managementService.createJobDefinitionQuery();
    JobDefinition jobDefinition = query.singleResult();

    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(0, jobQuery.active().count());
    assertEquals(1, jobQuery.suspended().count());

    // when
    // activate the job definition
    managementService
      .updateJobDefinitionSuspensionState()
      .byJobDefinitionId(jobDefinition.getId())
      .includeJobs(true)
      .activate();

    // then
    // there exists a active job definition
    assertEquals(1, jobQuery.active().count());
    assertEquals(0, jobQuery.suspended().count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testDelayedActivationUsingBuilder() {
    // given
    // a deployed process definition with asynchronous continuation

    // a running process instance with a failed job
    runtimeService.startProcessInstanceByKey("suspensionProcess",
        Variables.createVariables().putValue("fail", true));

    // a job definition (which was created for the asynchronous continuation)
    // ...which will be suspended with the corresponding jobs
    managementService.suspendJobDefinitionByProcessDefinitionKey("suspensionProcess", true);

    JobDefinitionQuery query = managementService.createJobDefinitionQuery();
    JobDefinition jobDefinition = query.singleResult();

    // when
    // activate the job definition
    managementService
      .updateJobDefinitionSuspensionState()
      .byJobDefinitionId(jobDefinition.getId())
      .executionDate(oneWeekLater())
      .activate();

    // then
    // the job definition is still suspended
    assertEquals(0, query.active().count());
    assertEquals(1, query.suspended().count());

    // there exists a job for the delayed activation execution
    Job delayedActivationJob = managementService.createJobQuery().timers().active().singleResult();
    assertNotNull(delayedActivationJob);
    String expectedDeploymentId = repositoryService.createProcessDefinitionQuery()
        .processDefinitionId(jobDefinition.getProcessDefinitionId()).singleResult().getDeploymentId();
    assertThat(delayedActivationJob.getDeploymentId()).isEqualTo(expectedDeploymentId);

    // execute job
    managementService.executeJob(delayedActivationJob.getId());

    // the job definition should be suspended
    assertEquals(1, query.active().count());
    assertEquals(0, query.suspended().count());
  }

  protected Date oneWeekLater() {
    // one week from now
    Date startTime = new Date();
    ClockUtil.setCurrentTime(startTime);
    long oneWeekFromStartTime = startTime.getTime() + (7 * 24 * 60 * 60 * 1000);
    return new Date(oneWeekFromStartTime);
  }

}
