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
import org.camunda.bpm.engine.impl.jobexecutor.TimerSuspendJobDefinitionHandler;
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

/**
 * @author roman.smirnov
 */
public class SuspendJobDefinitionTest extends PluggableProcessEngineTest {

  @After
  public void tearDown() throws Exception {
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        commandContext.getHistoricJobLogManager().deleteHistoricJobLogsByHandlerType(TimerSuspendJobDefinitionHandler.TYPE);
        return null;
      }
    });

  }

  // Test ManagementService#suspendJobDefinitionById() /////////////////////////

  @Test
  public void testSuspensionById_shouldThrowProcessEngineException() {
    try {
      managementService.suspendJobDefinitionById(null);
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }
  }

  @Test
  public void testSuspensionByIdAndSuspendJobsFlag_shouldThrowProcessEngineException() {
    try {
      managementService.suspendJobDefinitionById(null, false);
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }

    try {
      managementService.suspendJobDefinitionById(null, true);
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }
  }

  @Test
  public void testSuspensionByIdAndSuspendJobsFlagAndExecutionDate_shouldThrowProcessEngineException() {
    try {
      managementService.suspendJobDefinitionById(null, false, null);
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }

    try {
      managementService.suspendJobDefinitionById(null, true, null);
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }

    try {
      managementService.suspendJobDefinitionById(null, false, new Date());
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }

    try {
      managementService.suspendJobDefinitionById(null, true, new Date());
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testSuspensionById_shouldRetainJobs() {
    // given
    // a deployed process definition with asynchronous continuation

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // when
    // suspend the job definition
    managementService.suspendJobDefinitionById(jobDefinition.getId());

    // then
    // there exists a suspended job definition
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery().suspended();

    assertEquals(1, jobDefinitionQuery.count());

    JobDefinition suspendedJobDefinition = jobDefinitionQuery.singleResult();

    assertEquals(jobDefinition.getId(), suspendedJobDefinition.getId());

    // there does not exist any active job definition
    jobDefinitionQuery = managementService.createJobDefinitionQuery().active();
    assertTrue(jobDefinitionQuery.list().isEmpty());

    // the corresponding job is still active
    JobQuery jobQuery = managementService.createJobQuery().active();

    assertEquals(1, jobQuery.count());

    Job activeJob = jobQuery.singleResult();
    assertEquals(jobDefinition.getId(), activeJob.getJobDefinitionId());

    assertFalse(activeJob.isSuspended());

    jobQuery = managementService.createJobQuery().suspended();
    assertEquals(0, jobQuery.count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testSuspensionByIdAndSuspendJobsFlag_shouldRetainJobs() {
    // given
    // a deployed process definition with asynchronous continuation

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // when
    // suspend the job definition
    managementService.suspendJobDefinitionById(jobDefinition.getId(), false);

    // then
    // there exists a suspended job definition
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery().suspended();

    assertEquals(1, jobDefinitionQuery.count());

    JobDefinition suspendedJobDefinition = jobDefinitionQuery.singleResult();

    assertEquals(jobDefinition.getId(), suspendedJobDefinition.getId());
    assertTrue(suspendedJobDefinition.isSuspended());

    // the corresponding job is still active
    JobQuery jobQuery = managementService.createJobQuery().active();

    assertEquals(1, jobQuery.count());

    Job activeJob = jobQuery.singleResult();
    assertEquals(jobDefinition.getId(), activeJob.getJobDefinitionId());

    assertFalse(activeJob.isSuspended());

    jobQuery = managementService.createJobQuery().suspended();
    assertEquals(0, jobQuery.count());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testSuspensionByIdAndSuspendJobsFlag_shouldSuspendJobs() {
    // given
    // a deployed process definition with asynchronous continuation

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // when
    // suspend the job definition
    managementService.suspendJobDefinitionById(jobDefinition.getId(), true);

    // then
    // there exists a suspended job definition...
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery().suspended();

    assertEquals(1, jobDefinitionQuery.count());

    JobDefinition suspendedJobDefinition = jobDefinitionQuery.singleResult();

    assertEquals(jobDefinition.getId(), suspendedJobDefinition.getId());
    assertTrue(suspendedJobDefinition.isSuspended());

    // ...and a suspended job of the provided job definition
    JobQuery jobQuery = managementService.createJobQuery().suspended();

    assertEquals(1, jobQuery.count());

    Job suspendedJob = jobQuery.singleResult();
    assertEquals(jobDefinition.getId(), suspendedJob.getJobDefinitionId());
    assertTrue(suspendedJob.isSuspended());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testSuspensionById_shouldExecuteImmediatelyAndRetainJobs() {
    // given
    // a deployed process definition with asynchronous continuation

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // when
    // suspend the job definition
    managementService.suspendJobDefinitionById(jobDefinition.getId(), false, null);

    // then
    // there exists a suspended job definition
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery().suspended();

    assertEquals(1, jobDefinitionQuery.count());

    JobDefinition suspendedJobDefinition = jobDefinitionQuery.singleResult();

    assertEquals(jobDefinition.getId(), suspendedJobDefinition.getId());
    assertTrue(suspendedJobDefinition.isSuspended());

    // the corresponding job is still active
    JobQuery jobQuery = managementService.createJobQuery().active();

    assertEquals(1, jobQuery.count());

    Job activeJob = jobQuery.singleResult();
    assertEquals(jobDefinition.getId(), activeJob.getJobDefinitionId());

    assertFalse(activeJob.isSuspended());

    jobQuery = managementService.createJobQuery().suspended();
    assertEquals(0, jobQuery.count());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testSuspensionById_shouldExecuteImmediatelyAndSuspendJobs() {
    // given
    // a deployed process definition with asynchronous continuation

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // when
    // suspend the job definition
    managementService.suspendJobDefinitionById(jobDefinition.getId(), true, null);

    // then
    // there exists a suspended job definition...
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery().suspended();

    assertEquals(1, jobDefinitionQuery.count());

    JobDefinition suspendedJobDefinition = jobDefinitionQuery.singleResult();

    assertEquals(jobDefinition.getId(), suspendedJobDefinition.getId());
    assertTrue(suspendedJobDefinition.isSuspended());

    // ...and a suspended job of the provided job definition
    JobQuery jobQuery = managementService.createJobQuery().suspended();

    assertEquals(1, jobQuery.count());

    Job suspendedJob = jobQuery.singleResult();
    assertEquals(jobDefinition.getId(), suspendedJob.getJobDefinitionId());
    assertTrue(suspendedJob.isSuspended());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testSuspensionById_shouldExecuteDelayedAndRetainJobs() {
    // given
    // a deployed process definition with asynchronous continuation

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // when
    // suspend the job definition
    managementService.suspendJobDefinitionById(jobDefinition.getId(), false, oneWeekLater());

    // then
    // the job definition is still active
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();
    assertEquals(1, jobDefinitionQuery.active().count());
    assertEquals(0, jobDefinitionQuery.suspended().count());

    // there exists a job for the delayed suspension execution
    JobQuery jobQuery = managementService.createJobQuery();

    Job delayedSuspensionJob = jobQuery.timers().active().singleResult();
    assertNotNull(delayedSuspensionJob);
    String deploymentId = repositoryService.createProcessDefinitionQuery()
        .processDefinitionId(jobDefinition.getProcessDefinitionId()).singleResult().getDeploymentId();
    assertThat(delayedSuspensionJob.getDeploymentId()).isEqualTo(deploymentId);

    // execute job
    managementService.executeJob(delayedSuspensionJob.getId());

    // the job definition should be suspended
    assertEquals(0, jobDefinitionQuery.active().count());
    assertEquals(1, jobDefinitionQuery.suspended().count());

    JobDefinition suspendedJobDefinition = jobDefinitionQuery.suspended().singleResult();

    assertEquals(jobDefinition.getId(), suspendedJobDefinition.getId());
    assertTrue(suspendedJobDefinition.isSuspended());

    // the corresponding job is still active
    jobQuery = managementService.createJobQuery().active();

    assertEquals(1, jobQuery.count());

    Job activeJob = jobQuery.singleResult();
    assertEquals(jobDefinition.getId(), activeJob.getJobDefinitionId());

    assertFalse(activeJob.isSuspended());

    jobQuery = managementService.createJobQuery().suspended();
    assertEquals(0, jobQuery.count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testSuspensionById_shouldExecuteDelayedAndSuspendJobs() {
    // given
    // a deployed process definition with asynchronous continuation

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // when
    // suspend the job definition
    managementService.suspendJobDefinitionById(jobDefinition.getId(), true, oneWeekLater());

    // then
    // the job definition is still active
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();
    assertEquals(1, jobDefinitionQuery.active().count());
    assertEquals(0, jobDefinitionQuery.suspended().count());

    // there exists a job for the delayed suspension execution
    JobQuery jobQuery = managementService.createJobQuery();

    Job delayedSuspensionJob = jobQuery.timers().active().singleResult();
    assertNotNull(delayedSuspensionJob);
    String deploymentId = repositoryService.createProcessDefinitionQuery()
        .processDefinitionId(jobDefinition.getProcessDefinitionId()).singleResult().getDeploymentId();
    assertThat(delayedSuspensionJob.getDeploymentId()).isEqualTo(deploymentId);

    // execute job
    managementService.executeJob(delayedSuspensionJob.getId());

    // the job definition should be suspended
    assertEquals(0, jobDefinitionQuery.active().count());
    assertEquals(1, jobDefinitionQuery.suspended().count());

    JobDefinition suspendedJobDefinition = jobDefinitionQuery.suspended().singleResult();

    assertEquals(jobDefinition.getId(), suspendedJobDefinition.getId());
    assertTrue(suspendedJobDefinition.isSuspended());

    // the corresponding job is still suspended
    jobQuery = managementService.createJobQuery().suspended();

    assertEquals(1, jobQuery.count());

    Job suspendedJob = jobQuery.singleResult();
    assertEquals(jobDefinition.getId(), suspendedJob.getJobDefinitionId());

    assertTrue(suspendedJob.isSuspended());

    jobQuery = managementService.createJobQuery().active();
    assertEquals(0, jobQuery.count());
  }

  // Test ManagementService#suspendJobDefinitionByProcessDefinitionId() /////////////////////////

  @Test
  public void testSuspensionByProcessDefinitionId_shouldThrowProcessEngineException() {
    try {
      managementService.suspendJobDefinitionByProcessDefinitionId(null);
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }
  }

  @Test
  public void testSuspensionByProcessDefinitionIdAndSuspendJobsFlag_shouldThrowProcessEngineException() {
    try {
      managementService.suspendJobDefinitionByProcessDefinitionId(null, false);
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }

    try {
      managementService.suspendJobDefinitionByProcessDefinitionId(null, true);
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }
  }

  @Test
  public void testSuspensionByProcessDefinitionIdAndSuspendJobsFlagAndExecutionDate_shouldThrowProcessEngineException() {
    try {
      managementService.suspendJobDefinitionByProcessDefinitionId(null, false, null);
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }

    try {
      managementService.suspendJobDefinitionByProcessDefinitionId(null, true, null);
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }

    try {
      managementService.suspendJobDefinitionByProcessDefinitionId(null, false, new Date());
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }

    try {
      managementService.suspendJobDefinitionByProcessDefinitionId(null, true, new Date());
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testSuspensionByProcessDefinitionId_shouldRetainJobs() {
    // given
    // a deployed process definition with asynchronous continuation
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // when
    // suspend the job definition
    managementService.suspendJobDefinitionByProcessDefinitionId(processDefinition.getId());

    // then
    // there exists a suspended job definition
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery().suspended();

    assertEquals(1, jobDefinitionQuery.count());

    JobDefinition suspendedJobDefinition = jobDefinitionQuery.singleResult();

    assertEquals(jobDefinition.getId(), suspendedJobDefinition.getId());

    // there does not exist any active job definition
    jobDefinitionQuery = managementService.createJobDefinitionQuery().active();
    assertTrue(jobDefinitionQuery.list().isEmpty());

    // the corresponding job is still active
    JobQuery jobQuery = managementService.createJobQuery().active();

    assertEquals(1, jobQuery.count());

    Job activeJob = jobQuery.singleResult();
    assertEquals(jobDefinition.getId(), activeJob.getJobDefinitionId());

    assertFalse(activeJob.isSuspended());

    jobQuery = managementService.createJobQuery().suspended();
    assertEquals(0, jobQuery.count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testSuspensionByProcessDefinitionIdAndSuspendJobsFlag_shouldRetainJobs() {
    // given
    // a deployed process definition with asynchronous continuation
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // when
    // suspend the job definition
    managementService.suspendJobDefinitionByProcessDefinitionId(processDefinition.getId(), false);

    // then
    // there exists a suspended job definition
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery().suspended();

    assertEquals(1, jobDefinitionQuery.count());

    JobDefinition suspendedJobDefinition = jobDefinitionQuery.singleResult();

    assertEquals(jobDefinition.getId(), suspendedJobDefinition.getId());
    assertTrue(suspendedJobDefinition.isSuspended());

    // the corresponding job is still active
    JobQuery jobQuery = managementService.createJobQuery().active();

    assertEquals(1, jobQuery.count());

    Job activeJob = jobQuery.singleResult();
    assertEquals(jobDefinition.getId(), activeJob.getJobDefinitionId());

    assertFalse(activeJob.isSuspended());

    jobQuery = managementService.createJobQuery().suspended();
    assertEquals(0, jobQuery.count());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testSuspensionByProcessDefinitionIdAndSuspendJobsFlag_shouldSuspendJobs() {
    // given
    // a deployed process definition with asynchronous continuation
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // when
    // suspend the job definition
    managementService.suspendJobDefinitionByProcessDefinitionId(processDefinition.getId(), true);

    // then
    // there exists a suspended job definition...
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery().suspended();

    assertEquals(1, jobDefinitionQuery.count());

    JobDefinition suspendedJobDefinition = jobDefinitionQuery.singleResult();

    assertEquals(jobDefinition.getId(), suspendedJobDefinition.getId());
    assertTrue(suspendedJobDefinition.isSuspended());

    // ...and a suspended job of the provided job definition
    JobQuery jobQuery = managementService.createJobQuery().suspended();

    assertEquals(1, jobQuery.count());

    Job suspendedJob = jobQuery.singleResult();
    assertEquals(jobDefinition.getId(), suspendedJob.getJobDefinitionId());
    assertTrue(suspendedJob.isSuspended());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testSuspensionByProcessDefinitionId_shouldExecuteImmediatelyAndRetainJobs() {
    // given
    // a deployed process definition with asynchronous continuation
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // when
    // suspend the job definition
    managementService.suspendJobDefinitionByProcessDefinitionId(processDefinition.getId(), false, null);

    // then
    // there exists a suspended job definition
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery().suspended();

    assertEquals(1, jobDefinitionQuery.count());

    JobDefinition suspendedJobDefinition = jobDefinitionQuery.singleResult();

    assertEquals(jobDefinition.getId(), suspendedJobDefinition.getId());
    assertTrue(suspendedJobDefinition.isSuspended());

    // the corresponding job is still active
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.suspended().count());
    assertEquals(1, jobQuery.active().count());

    Job activeJob = jobQuery.active().singleResult();
    assertEquals(jobDefinition.getId(), activeJob.getJobDefinitionId());

    assertFalse(activeJob.isSuspended());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testSuspensionByProcessDefinitionId_shouldExecuteImmediatelyAndSuspendJobs() {
    // given
    // a deployed process definition with asynchronous continuation
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // when
    // suspend the job definition
    managementService.suspendJobDefinitionByProcessDefinitionId(processDefinition.getId(), true, null);

    // then
    // there exists a suspended job definition...
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery().suspended();

    assertEquals(1, jobDefinitionQuery.count());

    JobDefinition suspendedJobDefinition = jobDefinitionQuery.singleResult();

    assertEquals(jobDefinition.getId(), suspendedJobDefinition.getId());
    assertTrue(suspendedJobDefinition.isSuspended());

    // ...and a suspended job of the provided job definition
    JobQuery jobQuery = managementService.createJobQuery().suspended();

    assertEquals(1, jobQuery.count());

    Job suspendedJob = jobQuery.singleResult();
    assertEquals(jobDefinition.getId(), suspendedJob.getJobDefinitionId());
    assertTrue(suspendedJob.isSuspended());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testSuspensionByProcessDefinitionId_shouldExecuteDelayedAndRetainJobs() {
    // given
    // a deployed process definition with asynchronous continuation
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // when
    // suspend the job definition
    managementService.suspendJobDefinitionByProcessDefinitionId(processDefinition.getId(), false, oneWeekLater());

    // then
    // the job definition is still active
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();
    assertEquals(1, jobDefinitionQuery.active().count());
    assertEquals(0, jobDefinitionQuery.suspended().count());

    // there exists a job for the delayed suspension execution
    JobQuery jobQuery = managementService.createJobQuery();

    Job delayedSuspensionJob = jobQuery.timers().active().singleResult();
    assertNotNull(delayedSuspensionJob);
    assertThat(delayedSuspensionJob.getDeploymentId()).isEqualTo(processDefinition.getDeploymentId());

    // execute job
    managementService.executeJob(delayedSuspensionJob.getId());

    // the job definition should be suspended
    assertEquals(0, jobDefinitionQuery.active().count());
    assertEquals(1, jobDefinitionQuery.suspended().count());

    JobDefinition suspendedJobDefinition = jobDefinitionQuery.suspended().singleResult();

    assertEquals(jobDefinition.getId(), suspendedJobDefinition.getId());
    assertTrue(suspendedJobDefinition.isSuspended());

    // the corresponding job is still active
    jobQuery = managementService.createJobQuery().active();

    assertEquals(1, jobQuery.count());

    Job activeJob = jobQuery.singleResult();
    assertEquals(jobDefinition.getId(), activeJob.getJobDefinitionId());

    assertFalse(activeJob.isSuspended());

    jobQuery = managementService.createJobQuery().suspended();
    assertEquals(0, jobQuery.count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testSuspensionByProcessDefinitionId_shouldExecuteDelayedAndSuspendJobs() {
    // given
    // a deployed process definition with asynchronous continuation
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // when
    // suspend the job definition
    managementService.suspendJobDefinitionByProcessDefinitionId(processDefinition.getId(), true, oneWeekLater());

    // then
    // the job definition is still active
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();
    assertEquals(1, jobDefinitionQuery.active().count());
    assertEquals(0, jobDefinitionQuery.suspended().count());

    // there exists a job for the delayed suspension execution
    JobQuery jobQuery = managementService.createJobQuery();

    Job delayedSuspensionJob = jobQuery.timers().active().singleResult();
    assertNotNull(delayedSuspensionJob);
    assertThat(delayedSuspensionJob.getDeploymentId()).isEqualTo(processDefinition.getDeploymentId());

    // execute job
    managementService.executeJob(delayedSuspensionJob.getId());

    // the job definition should be suspended
    assertEquals(0, jobDefinitionQuery.active().count());
    assertEquals(1, jobDefinitionQuery.suspended().count());

    JobDefinition suspendedJobDefinition = jobDefinitionQuery.suspended().singleResult();

    assertEquals(jobDefinition.getId(), suspendedJobDefinition.getId());
    assertTrue(suspendedJobDefinition.isSuspended());

    // the corresponding job is suspended
    jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.active().count());
    assertEquals(1, jobQuery.suspended().count());

    Job suspendedJob = jobQuery.suspended().singleResult();

    assertEquals(jobDefinition.getId(), suspendedJob.getJobDefinitionId());
    assertTrue(suspendedJob.isSuspended());
  }

  // Test ManagementService#suspendJobDefinitionByProcessDefinitionKey() /////////////////////////

  @Test
  public void testSuspensionByProcessDefinitionKey_shouldThrowProcessEngineException() {
    try {
      managementService.suspendJobDefinitionByProcessDefinitionKey(null);
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }
  }

  @Test
  public void testSuspensionByProcessDefinitionKeyAndSuspendJobsFlag_shouldThrowProcessEngineException() {
    try {
      managementService.suspendJobDefinitionByProcessDefinitionKey(null, false);
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }

    try {
      managementService.suspendJobDefinitionByProcessDefinitionKey(null, true);
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }
  }

  @Test
  public void testSuspensionByProcessDefinitionKeyAndSuspendJobsFlagAndExecutionDate_shouldThrowProcessEngineException() {
    try {
      managementService.suspendJobDefinitionByProcessDefinitionKey(null, false, null);
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }

    try {
      managementService.suspendJobDefinitionByProcessDefinitionKey(null, true, null);
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }

    try {
      managementService.suspendJobDefinitionByProcessDefinitionKey(null, false, new Date());
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }

    try {
      managementService.suspendJobDefinitionByProcessDefinitionKey(null, true, new Date());
      fail("A ProcessEngineException was expected.");
    } catch (ProcessEngineException e) {
    }

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testSuspensionByProcessDefinitionKey_shouldRetainJobs() {
    // given
    // a deployed process definition with asynchronous continuation
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // when
    // suspend the job definition
    managementService.suspendJobDefinitionByProcessDefinitionKey(processDefinition.getKey());

    // then
    // there exists a suspended job definition
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery().suspended();

    assertEquals(1, jobDefinitionQuery.count());

    JobDefinition suspendedJobDefinition = jobDefinitionQuery.singleResult();

    assertEquals(jobDefinition.getId(), suspendedJobDefinition.getId());

    // there does not exist any active job definition
    jobDefinitionQuery = managementService.createJobDefinitionQuery().active();
    assertTrue(jobDefinitionQuery.list().isEmpty());

    // the corresponding job is still active
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.suspended().count());
    assertEquals(1, jobQuery.active().count());

    Job activeJob = jobQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), activeJob.getJobDefinitionId());
    assertFalse(activeJob.isSuspended());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testSuspensionByProcessDefinitionKeyAndSuspendJobsFlag_shouldRetainJobs() {
    // given
    // a deployed process definition with asynchronous continuation
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // when
    // suspend the job definition
    managementService.suspendJobDefinitionByProcessDefinitionKey(processDefinition.getKey(), false);

    // then
    // there exists a suspended job definition
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery().suspended();

    assertEquals(1, jobDefinitionQuery.count());

    JobDefinition suspendedJobDefinition = jobDefinitionQuery.singleResult();

    assertEquals(jobDefinition.getId(), suspendedJobDefinition.getId());
    assertTrue(suspendedJobDefinition.isSuspended());

    // the corresponding job is still active
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.suspended().count());
    assertEquals(1, jobQuery.active().count());

    Job activeJob = jobQuery.active().singleResult();
    assertEquals(jobDefinition.getId(), activeJob.getJobDefinitionId());

    assertFalse(activeJob.isSuspended());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testSuspensionByProcessDefinitionKeyAndSuspendJobsFlag_shouldSuspendJobs() {
    // given
    // a deployed process definition with asynchronous continuation
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // when
    // suspend the job definition
    managementService.suspendJobDefinitionByProcessDefinitionKey(processDefinition.getKey(), true);

    // then
    // there exists a suspended job definition...
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery().suspended();

    assertEquals(1, jobDefinitionQuery.count());

    JobDefinition suspendedJobDefinition = jobDefinitionQuery.singleResult();

    assertEquals(jobDefinition.getId(), suspendedJobDefinition.getId());
    assertTrue(suspendedJobDefinition.isSuspended());

    // ...and a suspended job of the provided job definition
    JobQuery jobQuery = managementService.createJobQuery().suspended();

    assertEquals(1, jobQuery.count());

    Job suspendedJob = jobQuery.singleResult();
    assertEquals(jobDefinition.getId(), suspendedJob.getJobDefinitionId());
    assertTrue(suspendedJob.isSuspended());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testSuspensionByProcessDefinitionKey_shouldExecuteImmediatelyAndRetainJobs() {
    // given
    // a deployed process definition with asynchronous continuation
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // when
    // suspend the job definition
    managementService.suspendJobDefinitionByProcessDefinitionKey(processDefinition.getKey(), false, null);

    // then
    // there exists a suspended job definition
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery().suspended();

    assertEquals(1, jobDefinitionQuery.count());

    JobDefinition suspendedJobDefinition = jobDefinitionQuery.singleResult();

    assertEquals(jobDefinition.getId(), suspendedJobDefinition.getId());
    assertTrue(suspendedJobDefinition.isSuspended());

    // the corresponding job is still active
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.suspended().count());
    assertEquals(1, jobQuery.active().count());

    Job activeJob = jobQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), activeJob.getJobDefinitionId());
    assertFalse(activeJob.isSuspended());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testSuspensionByProcessDefinitionKey_shouldExecuteImmediatelyAndSuspendJobs() {
    // given
    // a deployed process definition with asynchronous continuation
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // when
    // suspend the job definition
    managementService.suspendJobDefinitionByProcessDefinitionKey(processDefinition.getKey(), true, null);

    // then
    // there exists a suspended job definition...
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery().suspended();

    assertEquals(1, jobDefinitionQuery.count());

    JobDefinition suspendedJobDefinition = jobDefinitionQuery.singleResult();

    assertEquals(jobDefinition.getId(), suspendedJobDefinition.getId());
    assertTrue(suspendedJobDefinition.isSuspended());

    // ...and a suspended job of the provided job definition
    JobQuery jobQuery = managementService.createJobQuery().suspended();

    assertEquals(1, jobQuery.count());

    Job suspendedJob = jobQuery.singleResult();
    assertEquals(jobDefinition.getId(), suspendedJob.getJobDefinitionId());
    assertTrue(suspendedJob.isSuspended());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testSuspensionByProcessDefinitionKey_shouldExecuteDelayedAndRetainJobs() {
    // given
    // a deployed process definition with asynchronous continuation
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // when
    // suspend the job definition
    managementService.suspendJobDefinitionByProcessDefinitionKey(processDefinition.getKey(), false, oneWeekLater());

    // then
    // the job definition is still active
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();
    assertEquals(1, jobDefinitionQuery.active().count());
    assertEquals(0, jobDefinitionQuery.suspended().count());

    // there exists a job for the delayed suspension execution
    JobQuery jobQuery = managementService.createJobQuery();

    Job delayedSuspensionJob = jobQuery.timers().active().singleResult();
    assertNotNull(delayedSuspensionJob);
    assertThat(delayedSuspensionJob.getDeploymentId()).isEqualTo(processDefinition.getDeploymentId());

    // execute job
    managementService.executeJob(delayedSuspensionJob.getId());

    // the job definition should be suspended
    assertEquals(0, jobDefinitionQuery.active().count());
    assertEquals(1, jobDefinitionQuery.suspended().count());

    JobDefinition suspendedJobDefinition = jobDefinitionQuery.suspended().singleResult();

    assertEquals(jobDefinition.getId(), suspendedJobDefinition.getId());
    assertTrue(suspendedJobDefinition.isSuspended());

    // the corresponding job is still active
    jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.suspended().count());
    assertEquals(1, jobQuery.active().count());

    Job activeJob = jobQuery.active().singleResult();

    assertEquals(jobDefinition.getId(), activeJob.getJobDefinitionId());
    assertFalse(activeJob.isSuspended());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testSuspensionByProcessDefinitionKey_shouldExecuteDelayedAndSuspendJobs() {
    // given
    // a deployed process definition with asynchronous continuation
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // a job definition (which was created for the asynchronous continuation)
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // when
    // suspend the job definition
    managementService.suspendJobDefinitionByProcessDefinitionKey(processDefinition.getKey(), true, oneWeekLater());

    // then
    // the job definition is still active
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();
    assertEquals(1, jobDefinitionQuery.active().count());
    assertEquals(0, jobDefinitionQuery.suspended().count());

    // there exists a job for the delayed suspension execution
    JobQuery jobQuery = managementService.createJobQuery();

    Job delayedSuspensionJob = jobQuery.timers().active().singleResult();
    assertNotNull(delayedSuspensionJob);
    assertThat(delayedSuspensionJob.getDeploymentId()).isEqualTo(processDefinition.getDeploymentId());

    // execute job
    managementService.executeJob(delayedSuspensionJob.getId());

    // the job definition should be suspended
    assertEquals(0, jobDefinitionQuery.active().count());
    assertEquals(1, jobDefinitionQuery.suspended().count());

    JobDefinition suspendedJobDefinition = jobDefinitionQuery.suspended().singleResult();

    assertEquals(jobDefinition.getId(), suspendedJobDefinition.getId());
    assertTrue(suspendedJobDefinition.isSuspended());

    // the corresponding job is suspended
    jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.active().count());
    assertEquals(1, jobQuery.suspended().count());

    Job suspendedJob = jobQuery.suspended().singleResult();

    assertEquals(jobDefinition.getId(), suspendedJob.getJobDefinitionId());
    assertTrue(suspendedJob.isSuspended());
  }

  // Test ManagementService#suspendJobDefinitionByProcessDefinitionKey() with multiple process definition
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

    // when
    // suspend the job definition
    managementService.suspendJobDefinitionByProcessDefinitionKey(key);

    // then
    // all job definitions are suspended
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();
    assertEquals(3, jobDefinitionQuery.suspended().count());
    assertEquals(0, jobDefinitionQuery.active().count());

    // but the jobs are still active
    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(0, jobQuery.suspended().count());
    assertEquals(3, jobQuery.active().count());

    // Clean DB
    for (org.camunda.bpm.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

  @Test
  public void testMultipleSuspensionByProcessDefinitionKeyAndSuspendJobsFlag_shouldRetainJobs() {
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

    // when
    // suspend the job definition
    managementService.suspendJobDefinitionByProcessDefinitionKey(key, false);

    // then
    // all job definitions are suspended
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();
    assertEquals(3, jobDefinitionQuery.suspended().count());
    assertEquals(0, jobDefinitionQuery.active().count());

    // but the jobs are still active
    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(0, jobQuery.suspended().count());
    assertEquals(3, jobQuery.active().count());

    // Clean DB
    for (org.camunda.bpm.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

  @Test
  public void testMultipleSuspensionByProcessDefinitionKeyAndSuspendJobsFlag_shouldSuspendJobs() {
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

    // when
    // suspend the job definition
    managementService.suspendJobDefinitionByProcessDefinitionKey(key, true);

    // then
    // all job definitions are suspended
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();
    assertEquals(3, jobDefinitionQuery.suspended().count());
    assertEquals(0, jobDefinitionQuery.active().count());

    // and the jobs too
    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(3, jobQuery.suspended().count());
    assertEquals(0, jobQuery.active().count());

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

    // when
    // suspend the job definition
    managementService.suspendJobDefinitionByProcessDefinitionKey(key, false, null);

    // then
    // all job definitions are suspended
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();
    assertEquals(3, jobDefinitionQuery.suspended().count());
    assertEquals(0, jobDefinitionQuery.active().count());

    // but the jobs are still active
    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(0, jobQuery.suspended().count());
    assertEquals(3, jobQuery.active().count());

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

    // when
    // suspend the job definition
    managementService.suspendJobDefinitionByProcessDefinitionKey(key, true, null);

    // then
    // all job definitions are suspended
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();
    assertEquals(3, jobDefinitionQuery.suspended().count());
    assertEquals(0, jobDefinitionQuery.active().count());

    // and the jobs too
    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(3, jobQuery.suspended().count());
    assertEquals(0, jobQuery.active().count());

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

    // when
    // suspend the job definition
    managementService.suspendJobDefinitionByProcessDefinitionKey(key, false, oneWeekLater());

    // then
    // the job definition is still active
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();
    assertEquals(3, jobDefinitionQuery.active().count());
    assertEquals(0, jobDefinitionQuery.suspended().count());

    // there exists a job for the delayed suspension execution
    JobQuery jobQuery = managementService.createJobQuery();

    Job delayedSuspensionJob = jobQuery.timers().active().singleResult();
    assertNotNull(delayedSuspensionJob);
    String expectedDeploymentId = repositoryService.createProcessDefinitionQuery()
        .orderByProcessDefinitionVersion().desc().list().get(0).getDeploymentId();
    assertThat(delayedSuspensionJob.getDeploymentId()).isEqualTo(expectedDeploymentId);

    // execute job
    managementService.executeJob(delayedSuspensionJob.getId());

    assertEquals(0, jobDefinitionQuery.active().count());
    assertEquals(3, jobDefinitionQuery.suspended().count());

    // but the jobs are still active
    jobQuery = managementService.createJobQuery();
    assertEquals(0, jobQuery.suspended().count());
    assertEquals(3, jobQuery.active().count());

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

    // when
    // suspend the job definition
    managementService.suspendJobDefinitionByProcessDefinitionKey(key, true, oneWeekLater());

    // then
    // the job definitions are still active
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery();
    assertEquals(3, jobDefinitionQuery.active().count());
    assertEquals(0, jobDefinitionQuery.suspended().count());

    // there exists a job for the delayed suspension execution
    JobQuery jobQuery = managementService.createJobQuery();

    Job delayedSuspensionJob = jobQuery.timers().active().singleResult();
    assertNotNull(delayedSuspensionJob);
    String expectedDeploymentId = repositoryService.createProcessDefinitionQuery()
        .orderByProcessDefinitionVersion().desc().list().get(0).getDeploymentId();
    assertThat(delayedSuspensionJob.getDeploymentId()).isEqualTo(expectedDeploymentId);

    // execute job
    managementService.executeJob(delayedSuspensionJob.getId());

    // the job definition should be suspended
    assertEquals(0, jobDefinitionQuery.active().count());
    assertEquals(3, jobDefinitionQuery.suspended().count());

    // the corresponding jobs are suspended
    jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.active().count());
    assertEquals(3, jobQuery.suspended().count());

    // Clean DB
    for (org.camunda.bpm.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testSuspensionByIdUsingBuilder() {
    // given
    // a deployed process definition with asynchronous continuation

    // a running process instance with a failed job
    runtimeService.startProcessInstanceByKey("suspensionProcess",
        Variables.createVariables().putValue("fail", true));

    // a job definition (which was created for the asynchronous continuation)
    JobDefinitionQuery query = managementService.createJobDefinitionQuery();
    JobDefinition jobDefinition = query.singleResult();
    assertFalse(jobDefinition.isSuspended());

    // when
    // suspend the job definition
    managementService
      .updateJobDefinitionSuspensionState()
      .byJobDefinitionId(jobDefinition.getId())
      .suspend();

    // then
    // there exists a suspended job definition
    assertEquals(1, query.suspended().count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testSuspensionByProcessDefinitionIdUsingBuilder() {
    // given
    // a deployed process definition with asynchronous continuation

    // a running process instance with a failed job
    runtimeService.startProcessInstanceByKey("suspensionProcess",
        Variables.createVariables().putValue("fail", true));

    // a job definition (which was created for the asynchronous continuation)
    JobDefinitionQuery query = managementService.createJobDefinitionQuery();
    JobDefinition jobDefinition = query.singleResult();
    assertFalse(jobDefinition.isSuspended());

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // when
    // suspend the job definition
    managementService
      .updateJobDefinitionSuspensionState()
      .byProcessDefinitionId(processDefinition.getId())
      .suspend();

    // then
    // there exists a suspended job definition
    assertEquals(1, query.suspended().count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testSuspensionByProcessDefinitionKeyUsingBuilder() {
    // given
    // a deployed process definition with asynchronous continuation

    // a running process instance with a failed job
    runtimeService.startProcessInstanceByKey("suspensionProcess",
        Variables.createVariables().putValue("fail", true));

    // a job definition (which was created for the asynchronous continuation)
    JobDefinitionQuery query = managementService.createJobDefinitionQuery();
    JobDefinition jobDefinition = query.singleResult();
    assertFalse(jobDefinition.isSuspended());

    // when
    // suspend the job definition
    managementService
      .updateJobDefinitionSuspensionState()
      .byProcessDefinitionKey("suspensionProcess")
      .suspend();

    // then
    // there exists a suspended job definition
    assertEquals(1, query.suspended().count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testSuspensionJobDefinitionIncludeJobsdUsingBuilder() {
    // given
    // a deployed process definition with asynchronous continuation

    // a running process instance with a failed job
    runtimeService.startProcessInstanceByKey("suspensionProcess",
        Variables.createVariables().putValue("fail", true));

    // a job definition (which was created for the asynchronous continuation)
    JobDefinitionQuery query = managementService.createJobDefinitionQuery();
    JobDefinition jobDefinition = query.singleResult();
    assertFalse(jobDefinition.isSuspended());

    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(0, jobQuery.suspended().count());
    assertEquals(1, jobQuery.active().count());


    // when
    // suspend the job definition and the job
    managementService
      .updateJobDefinitionSuspensionState()
      .byJobDefinitionId(jobDefinition.getId())
      .includeJobs(true)
      .suspend();

    // then
    // there exists a suspended job definition and job
    assertEquals(1, query.suspended().count());

    assertEquals(1, jobQuery.suspended().count());
    assertEquals(0, jobQuery.active().count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  @Test
  public void testDelayedSuspensionUsingBuilder() {
    // given
    // a deployed process definition with asynchronous continuation

    // a running process instance with a failed job
    runtimeService.startProcessInstanceByKey("suspensionProcess",
        Variables.createVariables().putValue("fail", true));

    // a job definition (which was created for the asynchronous continuation)
    JobDefinitionQuery query = managementService.createJobDefinitionQuery();
    JobDefinition jobDefinition = query.singleResult();

    // when
    // suspend the job definition in one week
    managementService
      .updateJobDefinitionSuspensionState()
      .byJobDefinitionId(jobDefinition.getId())
      .executionDate(oneWeekLater())
      .suspend();

    // then
    // the job definition is still active
    assertEquals(1, query.active().count());
    assertEquals(0, query.suspended().count());

    // there exists a job for the delayed suspension execution
    Job delayedSuspensionJob = managementService.createJobQuery().timers().active().singleResult();
    assertNotNull(delayedSuspensionJob);
    String expectedDeploymentId = repositoryService.createProcessDefinitionQuery()
        .processDefinitionId(jobDefinition.getProcessDefinitionId()).singleResult().getDeploymentId();
    assertThat(delayedSuspensionJob.getDeploymentId()).isEqualTo(expectedDeploymentId);

    // execute job
    managementService.executeJob(delayedSuspensionJob.getId());

    // the job definition should be suspended
    assertEquals(0, query.active().count());
    assertEquals(1, query.suspended().count());
  }

  protected Date oneWeekLater() {
    Date startTime = new Date();
    ClockUtil.setCurrentTime(startTime);
    long oneWeekFromStartTime = startTime.getTime() + (7 * 24 * 60 * 60 * 1000);
    return new Date(oneWeekFromStartTime);
  }

}
