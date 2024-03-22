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
import static org.camunda.bpm.engine.test.util.JobExecutorWaitUtils.waitForJobExecutorToProcessAllJobs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.jobexecutor.DefaultJobExecutor;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class ExclusiveJobAcquisitionTest {

  private static final long MAX_SECONDS_TO_WAIT_ON_JOBS = 60;

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(configuration ->
      configuration.setJobExecutor(new AssertJobExecutor())
  );

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  private ProcessEngineConfigurationImpl engineConfig;
  private RuntimeService runtimeService;
  private ManagementService managementService;

  private AssertJobExecutor jobExecutor;

  @Before
  public void setup() {
    this.engineConfig = engineRule.getProcessEngineConfiguration();
    this.runtimeService = engineRule.getRuntimeService();
    this.managementService = engineRule.getManagementService();

    this.jobExecutor = (AssertJobExecutor) engineConfig.getJobExecutor();
  }

  @After
  public void tearDown() {
    this.jobExecutor.clear();
    this.jobExecutor.shutdown();
  }

  @Test
  public void shouldNotApplyExclusiveAcquisitionWhenMultipleHierarchiesExclusiveJobsIsDisabled() {
    // given
    engineConfig.setJobExecutorActivate(false);
    engineConfig.setJobExecutorAcquireExclusiveOverProcessHierarchies(false); // disable the feature
    
    jobExecutor.setMaxJobsPerAcquisition(10);
    jobExecutor.setCorePoolSize(1);

    // A root process calling a subprocess
    var subModel = Bpmn.createExecutableProcess("subProcess")
        .startEvent()
        .scriptTask("scriptTask")
        .camundaAsyncBefore()
        .camundaExclusive(true) // with an exclusive script task
        .scriptFormat("javascript")
        .scriptText("console.log(execution.getJobs())")
        .endEvent()
        .done();

    var rootModel = Bpmn.createExecutableProcess("rootProcess")
        .startEvent()
        .callActivity("callActivity")
        .calledElement("subProcess")
        .multiInstance()
        .parallel()
        .cardinality("2") // and 2 spawned subprocesses by each process instance
        .multiInstanceDone()
        .endEvent()
        .done();

   testRule.deploy(subModel, rootModel);

    // when

    // two process instances
    var pi1 = runtimeService.startProcessInstanceByKey("rootProcess");
    var pi2 = runtimeService.startProcessInstanceByKey("rootProcess");

    // 4 jobs are created (2 for each root process due to cardinality)
    assertThat(managementService.createJobQuery().list()).hasSize(4);

    var pi1Jobs = assertProcessInstanceJobs(pi1, 2, "subProcess");
    var pi2Jobs = assertProcessInstanceJobs(pi2, 2, "subProcess");

    // the scheduler starts to acquire & execute the produced jobs
    jobExecutor.start();
    waitForJobExecutorToProcessAllJobs(MAX_SECONDS_TO_WAIT_ON_JOBS * 1000, 100, jobExecutor, managementService, true);

    var batch1_pi1_job1 = pi1Jobs.subList(0, 1);
    var batch2_pi1_job2 = pi1Jobs.subList(1, 2);

    var batch3_pi2_job1 = pi2Jobs.subList(0, 1);
    var batch4_pi2_job2 = pi2Jobs.subList(1, 2);

    // then assert that all jobs are executed in parallel into separate batches (no exclusiveness)
    jobExecutor.assertJobGroup(batch1_pi1_job1, batch2_pi1_job2, batch3_pi2_job1, batch4_pi2_job2);
  }

  @Test
  public void shouldApplyExclusiveAcquisitionWhenAcquireExclusiveOverProcessHierarchiesIsEnabled() {
    // given
    engineConfig.setJobExecutorActivate(false);
    engineConfig.setJobExecutorAcquireExclusiveOverProcessHierarchies(true); // enable the feature

    jobExecutor.setMaxJobsPerAcquisition(10);

    // given
    // A root process calling a subprocess
    var subModel = Bpmn.createExecutableProcess("subProcess")
        .startEvent()
        .scriptTask("scriptTask")
        .camundaAsyncBefore()
        .camundaExclusive(true) // with an exclusive script task
        .scriptFormat("javascript")
        .scriptText("console.log(execution.getJobs())")
        .endEvent()
        .done();

    var rootModel = Bpmn.createExecutableProcess("rootProcess")
        .startEvent()
        .callActivity("callActivity")
        .calledElement("subProcess")
        .multiInstance()
        .parallel()
        .cardinality("2") // and 2 spawned subprocesses by each process instance
        .multiInstanceDone()
        .endEvent()
        .done();

    testRule.deploy(subModel, rootModel);

    // when
    var pi1 = runtimeService.startProcessInstanceByKey("rootProcess");
    var pi2 = runtimeService.startProcessInstanceByKey("rootProcess");

    // 4 jobs are created (2 for each root process due to cardinality)
    assertThat(managementService.createJobQuery().list()).hasSize(4);

    var pi1Jobs = assertProcessInstanceJobs(pi1, 2, "subProcess");
    var pi2Jobs = assertProcessInstanceJobs(pi2, 2, "subProcess");

    // the scheduler starts to acquire & execute the produced jobs
    jobExecutor.start();
    waitForJobExecutorToProcessAllJobs(MAX_SECONDS_TO_WAIT_ON_JOBS * 1000, 100, jobExecutor, managementService, true);

    // then
    // the two process instance batches should have been executed separately to apply exclusiveness
    jobExecutor.assertJobGroup(pi1Jobs, pi2Jobs);
  }

  @Test
  public void shouldApplyExclusiveAcquisitionWhenAcquireExclusiveOverProcessHierarchiesIsEnabledMultiHierarchy() {
    // given
    engineConfig.setJobExecutorActivate(false);
    engineConfig.setJobExecutorAcquireExclusiveOverProcessHierarchies(true); // enable the feature

    jobExecutor.setMaxJobsPerAcquisition(10);

    // given a root process (1) with a subprocess (2) that spins up another subprocess (2)
    var subSubModel = Bpmn.createExecutableProcess("subSubProcess")
        .startEvent()
        .scriptTask("scriptTask")
        .camundaAsyncBefore()
        .camundaExclusive(true)
        .scriptFormat("javascript")
        .scriptText("console.log(execution.getJobs())")
        .endEvent()
        .done();

    var subModel = Bpmn.createExecutableProcess("subProcess")
        .startEvent()
        .callActivity("callActivity")
        .calledElement("subSubProcess")
        .multiInstance()
        .parallel()
        .cardinality("2")
        .multiInstanceDone()
        .endEvent()
        .done();

    var rootModel = Bpmn.createExecutableProcess("rootProcess")
        .startEvent()
        .callActivity("callActivity")
        .calledElement("subProcess")
        .multiInstance()
        .parallel()
        .cardinality("2")
        .multiInstanceDone()
        .endEvent()
        .done();

    testRule.deploy(subSubModel, subModel, rootModel);

    // when
    // the process instances are started
    var pi1 = runtimeService.startProcessInstanceByKey("rootProcess");
    var pi2 = runtimeService.startProcessInstanceByKey("rootProcess");

    // 4 jobs of each subSubProcess are created for each process instance
    assertThat(managementService.createJobQuery().list()).hasSize(8);

    var pi1Jobs = assertProcessInstanceJobs(pi1, 4, "subSubProcess");
    var pi2Jobs = assertProcessInstanceJobs(pi2, 4, "subSubProcess");

    // the scheduler starts to acquire & execute the produced jobs
    jobExecutor.start();
    waitForJobExecutorToProcessAllJobs(MAX_SECONDS_TO_WAIT_ON_JOBS * 1000, 100, jobExecutor, managementService, true);

    // then
    // the two process instance batches should have been executed separately to apply exclusiveness
    jobExecutor.assertJobGroup(pi1Jobs, pi2Jobs);
  }

  private List<String> assertProcessInstanceJobs(ProcessInstance pi, int nJobs, String pdKey) {
    var jobs = managementService.createJobQuery()
        .rootProcessInstanceId(pi.getId())
        .list();

    assertThat(jobs).hasSize(nJobs);

    jobs.forEach(job -> {
      assertThat(job.getProcessDefinitionKey()).isEqualTo(pdKey);
      assertThat(job.getRootProcessInstanceId()).isEqualTo(pi.getId());
    });

    return jobs.stream()
        .map(Job::getId)
        .collect(Collectors.toList());
  }

  /**
   * Assert Job Executor extends the DefaultJobExecutor to be able to assert the job batches.
   * Each batch is executed sequentially by the same thread.
   * <p>
   * If a batch contains 1 element, it means it can be executed in parallel with other batches.
   * In order for 2 jobs to be executed exclusively, they should exist in the same batch.
   */
  static class AssertJobExecutor extends DefaultJobExecutor {

    final List<Set<String>> jobBatches = new ArrayList<>();

    @SafeVarargs
    public final void assertJobGroup(List<String>... jobIds) {
      var jobGroups = asArrayOfSets(jobIds);

      assertThat(jobBatches).containsExactlyInAnyOrder(jobGroups);
    }

    @Override
    public void executeJobs(List<String> jobIds, ProcessEngineImpl processEngine) {
      super.executeJobs(jobIds, processEngine);

      System.out.println("jobIds = " + jobIds);
      jobBatches.add(new HashSet<>(jobIds));
    }

    public void clear() {
      jobBatches.clear();
    }
  }

  private static Set<String>[] asArrayOfSets(List<String>... jobIds) {
    List<Set<String>> result = new ArrayList<>();
    for (List<String> jobGroup : jobIds) {
      result.add(new HashSet<>(jobGroup));
    }


    return result.toArray(new Set[0]);
  }
}