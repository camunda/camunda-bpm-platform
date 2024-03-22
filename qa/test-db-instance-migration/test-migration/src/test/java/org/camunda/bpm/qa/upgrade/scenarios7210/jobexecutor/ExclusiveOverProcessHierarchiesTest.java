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

package org.camunda.bpm.qa.upgrade.scenarios7210.jobexecutor;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.AcquireJobsCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.AcquiredJobs;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.qa.upgrade.Origin;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.camunda.bpm.qa.upgrade.UpgradeTestRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Origin("7.20.0")
public class ExclusiveOverProcessHierarchiesTest {

    @Rule
    public final UpgradeTestRule engineRule = new UpgradeTestRule();

    RuntimeService runtimeService;
    ManagementService managementService;
    HistoryService historyService;

    JobExecutor jobExecutor;
    CommandExecutor commandExecutor;

    ProcessEngineConfigurationImpl engineConfig;
    JobState jobState;
    boolean isJobExecutorAcquireExclusiveOverProcessHierarchies;

    @Before
    public void init() {
        var engine = engineRule.getProcessEngine();
        var engineConfig = (ProcessEngineConfigurationImpl) engine.getProcessEngineConfiguration();

        this.runtimeService = engine.getRuntimeService();
        this.managementService = engine.getManagementService();
        this.historyService = engine.getHistoryService();

        this.jobExecutor = engineConfig.getJobExecutor();
        this.commandExecutor = engineConfig.getCommandExecutorTxRequired();
        this.engineConfig = engineConfig;

        storeOriginalStateBeforeTest(engine);
    }

    @After
    public void tearDown() {
        restoreOriginalStateAfterTest();
    }

    @Test
    @ScenarioUnderTest("createRootProcessInstancesWithHierarchies.1")
    public void shouldPerformLegacyAcquisitionWhenMultipleHierarchiesExclusiveJobsIsDisabled() {
        // given

        // The default configuration where exclusiveOverProcessHierarchies flag should be disabled by default
        assertThat(engineConfig.isJobExecutorAcquireExclusiveOverProcessHierarchies()).isFalse();

        // a deployed root Process which delegates to a subProcess (cardinality 2). See ExclusiveOverProcessHierarchiesScenario
        var rootProcesses = runtimeService.createProcessInstanceQuery()
                .processDefinitionKey("rootProcess_7.20")
                .processInstanceBusinessKey("withMultipleHierarchies")
                .list();

        assertThat(rootProcesses).hasSize(2);

        // 4 jobs are created (2 for each root process due to cardinality)
        var jobs = managementService.createJobQuery()
                .processDefinitionKey("subProcess_7.20")
                .list();

        assertThat(jobs).hasSize(4);

        // all 4 jobs of the root process instances should have null rootProcessInstanceId since it didn't exist in the schema
        assertThat(jobs.get(0).getRootProcessInstanceId()).isNull();
        assertThat(jobs.get(1).getRootProcessInstanceId()).isNull();
        assertThat(jobs.get(2).getRootProcessInstanceId()).isNull();
        assertThat(jobs.get(3).getRootProcessInstanceId()).isNull();

        var piJobs = assertProcessInstanceJobs(4, "subProcess_7.20", null);

        int allJobsSize = getAllJobsSize();

        // when

        // The result of the AcquireJobsCmd (Component responsible for the job acquisition)
        AcquiredJobs result = commandExecutor.execute(new AcquireJobsCmd(jobExecutor, allJobsSize));

        var jobIdBatches = result.getJobIdBatches();

        var job1 = piJobs.subList(0, 1);
        var job2 = piJobs.subList(1, 2);
        var job3 = piJobs.subList(2, 3);
        var job4 = piJobs.subList(3, 4);

        // then

        // Contains the subprocess job ids in separate lists (batches)
        // They will be executed in parallel as the legacy acquisition behaviour of multiple hierarchies
        assertThat(jobIdBatches).contains(job1, job2, job3, job4);
    }

    @Test
    @ScenarioUnderTest("createRootProcessInstancesWithHierarchies.2")
    public void shouldPerformExclusiveAcquisitionWhenMultipleHierarchiesExclusiveJobsIsEnabled() {
        // given

        // The feature is enabled
        engineConfig.setJobExecutorAcquireExclusiveOverProcessHierarchies(true);

        // a deployed root Process which delegates to a subProcess (cardinality 2). See ExclusiveOverProcessHierarchiesScenario
        var rootProcesses = runtimeService.createProcessInstanceQuery()
                .processDefinitionKey("rootProcess_7.20")
                .processInstanceBusinessKey("withMultipleHierarchies")
                .list();

        assertThat(rootProcesses).hasSize(2);

        // 4 jobs are created (2 for each root process due to cardinality)
        var jobs = managementService.createJobQuery()
                .processDefinitionKey("subProcess_7.20")
                .list();

        assertThat(jobs).hasSize(4);

        // all 4 jobs of the root process instances should have null rootProcessInstanceId since it didn't exist in the schema
        assertThat(jobs.get(0).getRootProcessInstanceId()).isNull();
        assertThat(jobs.get(1).getRootProcessInstanceId()).isNull();
        assertThat(jobs.get(2).getRootProcessInstanceId()).isNull();
        assertThat(jobs.get(3).getRootProcessInstanceId()).isNull();

        var piJobs = assertProcessInstanceJobs(4, "subProcess_7.20", null);

        int allJobsSize = getAllJobsSize();

        // when

        // The result of the AcquireJobsCmd (Component responsible for the job acquisition)
        AcquiredJobs result = commandExecutor.execute(new AcquireJobsCmd(jobExecutor, allJobsSize));

        var jobIdBatches = result.getJobIdBatches();

        var job1 = piJobs.subList(0, 1);
        var job2 = piJobs.subList(1, 2);
        var job3 = piJobs.subList(2, 3);
        var job4 = piJobs.subList(3, 4);

        // then

        // contains the subprocess job ids in separate lists (batches)
        // They will be executed in parallel as the legacy behaviour of multiple hierarchies
        assertThat(jobIdBatches).contains(job1, job2, job3, job4);
    }

    protected List<String> assertProcessInstanceJobs(int nJobs, String processDefinitionKey, String rootProcessInstanceId) {
        return assertProcessInstanceJobs(null, nJobs, processDefinitionKey, rootProcessInstanceId);
    }

    protected List<String> assertProcessInstanceJobs(ProcessInstance pi, int nJobs, String processDefinitionKey,
                                                     String rootProcessInstanceId) {
        var jobQuery = managementService.createJobQuery();

        if (pi != null) {
            jobQuery.processInstanceId(pi.getProcessInstanceId());
        }

        if (processDefinitionKey != null) {
            jobQuery.processDefinitionKey(processDefinitionKey);
        }

        var jobs = jobQuery.list();

        assertThat(jobs).hasSize(nJobs);

        assertThat(jobs)
                .extracting(Job::getRootProcessInstanceId)
                .containsOnly(rootProcessInstanceId);

        return jobs.stream()
                .map(Job::getId)
                .collect(Collectors.toList());
    }

    protected int getAllJobsSize() {
        var allJobs = managementService.createJobQuery().list();
        return allJobs.size();
    }

    protected void storeOriginalStateBeforeTest(ProcessEngine engine) {
        this.isJobExecutorAcquireExclusiveOverProcessHierarchies = engineConfig.isJobExecutorAcquireExclusiveOverProcessHierarchies();
        this.jobState = JobState.ofAllProcessEngineJobs(engine);
    }

    protected void restoreOriginalStateAfterTest() {
        this.engineConfig.setJobExecutorAcquireExclusiveOverProcessHierarchies(isJobExecutorAcquireExclusiveOverProcessHierarchies);
        this.jobState.restoreLockedJobs();
    }

}