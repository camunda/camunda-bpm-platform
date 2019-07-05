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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.AcquirableJobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobManager;
import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class AcquirableJobCacheTest {

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected ManagementService managementService;
  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected RuntimeService runtimeService;

  @Before
  public void setup() {
    managementService = engineRule.getManagementService();
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    runtimeService = engineRule.getRuntimeService();
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/metrics/asyncServiceTaskProcess.bpmn20.xml")
  public void testFetchJobEntityWhenAcquirableJobIsCached() {
    // given
    runtimeService.startProcessInstanceByKey("asyncServiceTaskProcess");

    try {
      // when
      fetchJobAfterCachedAcquirableJob();
      fail("expected exception");
    } catch (Exception e) {
      // then
      assertThat(e).isInstanceOf(ProcessEngineException.class);
      assertThat(e.getMessage())
          .contains("Could not lookup entity of type")
          .contains(AcquirableJobEntity.class.getSimpleName())
          .contains(JobEntity.class.getSimpleName());
    }
  }

  @Test
  public void testFetchTimerEntityWhenAcquirableJobIsCached() {
    // given
    BpmnModelInstance process = Bpmn.createExecutableProcess("startTimer")
        .startEvent()
        .userTask("userTask")
          .boundaryEvent()
          .timerWithDate("2016-02-11T12:13:14Z")
        .done();
    testRule.deploy(process);
    runtimeService.startProcessInstanceByKey("startTimer");
    Execution execution = runtimeService.createExecutionQuery().activityId("userTask").singleResult();

    try {
      // when
      fetchTimerJobAfterCachedAcquirableJob(execution.getId());
      fail("expected exception");
    } catch (Exception e) {
      // then
      assertThat(e).isInstanceOf(ProcessEngineException.class);
      assertThat(e.getMessage())
          .contains("Could not lookup entity of type")
          .contains(TimerEntity.class.getSimpleName())
          .contains(AcquirableJobEntity.class.getSimpleName());
    }
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/metrics/asyncServiceTaskProcess.bpmn20.xml")
  public void testFetchAcquirableJobWhenJobEntityIsCached() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("asyncServiceTaskProcess");

    // when
    AcquirableJobEntity job = fetchAcquirableJobAfterCachedJob(processInstance.getId());

    // then
    assertThat(job).isNotNull();
    assertThat(job.getProcessInstanceId()).isEqualTo(processInstance.getId());
  }

  @Test
  public void testFetchAcquirableJobWhenTimerEntityIsCached() {
    // given
    BpmnModelInstance process = Bpmn.createExecutableProcess("timer")
      .startEvent()
      .userTask("userTask")
        .boundaryEvent()
        .timerWithDate("2016-02-11T12:13:14Z")
      .done();
    testRule.deploy(process);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("timer");
    Execution execution = runtimeService.createExecutionQuery().activityId("userTask").singleResult();

    // when
    AcquirableJobEntity job = fetchAcquirableJobAfterCachedTimerEntity(execution.getId());

    // then
    assertThat(job).isNotNull();
    assertThat(job.getProcessInstanceId()).isEqualTo(processInstance.getId());
  }

  protected JobEntity fetchJobAfterCachedAcquirableJob() {
    return processEngineConfiguration.getCommandExecutorTxRequiresNew().execute(new Command<JobEntity>() {
      public JobEntity execute(CommandContext commandContext) {
        JobManager jobManager = commandContext.getJobManager();
        List<AcquirableJobEntity> acquirableJobs = jobManager.findNextJobsToExecute(new Page(0, 100));
        JobEntity job = jobManager.findJobById(acquirableJobs.get(0).getId());
        return job;
      }
    });
  }

  protected TimerEntity fetchTimerJobAfterCachedAcquirableJob(final String executionId) {
    return processEngineConfiguration.getCommandExecutorTxRequiresNew().execute(new Command<TimerEntity>() {
      public TimerEntity execute(CommandContext commandContext) {
        JobManager jobManager = commandContext.getJobManager();
        jobManager.findNextJobsToExecute(new Page(0, 100));
        List<TimerEntity> timerJobs = jobManager.findTimersByExecutionId(executionId);
        return timerJobs.get(0);
      }
    });
  }

  protected AcquirableJobEntity fetchAcquirableJobAfterCachedTimerEntity(final String executionId) {
    return processEngineConfiguration.getCommandExecutorTxRequiresNew().execute(new Command<AcquirableJobEntity>() {
      public AcquirableJobEntity execute(CommandContext commandContext) {
        JobManager jobManager = commandContext.getJobManager();
        jobManager.findTimersByExecutionId(executionId);
        List<AcquirableJobEntity> acquirableJob = jobManager.findNextJobsToExecute(new Page(0, 100));
        return acquirableJob.get(0);
      }
    });
  }

  protected AcquirableJobEntity fetchAcquirableJobAfterCachedJob(final String processInstanceId) {
    return processEngineConfiguration.getCommandExecutorTxRequiresNew().execute(new Command<AcquirableJobEntity>() {
      public AcquirableJobEntity execute(CommandContext commandContext) {
        JobManager jobManager = commandContext.getJobManager();
        jobManager.findJobsByProcessInstanceId(processInstanceId);
        List<AcquirableJobEntity> acquirableJobs = jobManager.findNextJobsToExecute(new Page(0, 100));
        return acquirableJobs.get(0);
      }
    });
  }
}
