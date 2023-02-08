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
package org.camunda.bpm.engine.test.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.BatchHelper;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.junit.After;

/**
 * @author Askar Akhmerov
 */
public abstract class AbstractAsyncOperationsTest {

  public static final String ONE_TASK_PROCESS = "oneTaskProcess";
  public static final String TESTING_INSTANCE_DELETE = "testing instance delete";

  protected RuntimeService runtimeService;
  protected ManagementService managementService;
  protected HistoryService historyService;

  protected BatchHelper helper;

  protected ProcessEngineConfigurationImpl engineConfiguration;

  protected int defaultBatchJobsPerSeed;
  protected int defaultInvocationsPerBatchJob;

  protected void initDefaults(ProcessEngineRule engineRule) {
    runtimeService = engineRule.getRuntimeService();
    managementService = engineRule.getManagementService();
    historyService = engineRule.getHistoryService();

    engineConfiguration = engineRule.getProcessEngineConfiguration();

    // save defaults
    defaultBatchJobsPerSeed = engineConfiguration.getBatchJobsPerSeed();
    defaultInvocationsPerBatchJob = engineConfiguration.getInvocationsPerBatchJob();
  }

  @After
  public void cleanUpBatches() {
    managementService.createBatchQuery().list().forEach(b -> managementService.deleteBatch(b.getId(), true));

    historyService.createHistoricBatchQuery().list().forEach(b -> historyService.deleteHistoricBatch(b.getId()));

    // restore default settings
    engineConfiguration.setBatchJobsPerSeed(defaultBatchJobsPerSeed);
    engineConfiguration.setInvocationsPerBatchJob(defaultInvocationsPerBatchJob);
  }

  protected List<String> getJobIdsByDeployment(List<Job> jobs, String deploymentId) {
    return jobs.stream().filter(j -> deploymentId.equals(j.getDeploymentId())).map(Job::getId).collect(Collectors.toList());
  }

  protected void completeSeedJobs(Batch batch) {
    while (getSeedJob(batch) != null) {
      executeSeedJob(batch);
    }
  }

  protected void executeSeedJob(Batch batch) {
    Job seedJob = getSeedJob(batch);
    assertNotNull(seedJob);
    managementService.executeJob(seedJob.getId());
  }

  protected void executeSeedJobs(Batch batch, int expectedSeedJobsCount) {
    for (int i = 0; i < expectedSeedJobsCount; i++) {
      executeSeedJob(batch);
    }
    assertNull(getSeedJob(batch));
  }

  protected Job getSeedJob(Batch batch) {
    String seedJobDefinitionId = batch.getSeedJobDefinitionId();
    Job seedJob = managementService.createJobQuery().jobDefinitionId(seedJobDefinitionId).singleResult();
    return seedJob;
  }

  /**
   * Execute all batch jobs of batch once and collect exceptions during job execution.
   *
   * @param batch the batch for which the batch jobs should be executed
   * @return the caught exceptions of the batch job executions, is empty if non where thrown
   */
  protected List<Exception> executeBatchJobs(Batch batch) {
    String batchJobDefinitionId = batch.getBatchJobDefinitionId();
    List<Job> batchJobs = managementService.createJobQuery().jobDefinitionId(batchJobDefinitionId).list();
    assertThat(batchJobs).isNotEmpty();

    List<Exception> caughtExceptions = new ArrayList<>();

    for (Job batchJob : batchJobs) {
      try {
        managementService.executeJob(batchJob.getId());
      } catch (Exception e) {
        caughtExceptions.add(e);
      }
    }

    return caughtExceptions;
  }

  protected List<String> startTestProcesses(int numberOfProcesses) {
    ArrayList<String> ids = new ArrayList<>();

    for (int i = 0; i < numberOfProcesses; i++) {
      ids.add(runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS).getProcessInstanceId());
    }

    return ids;
  }

  protected void assertHistoricTaskDeletionPresent(List<String> processIds, String deleteReason, ProcessEngineTestRule testRule) {
    if (!testRule.isHistoryLevelNone()) {

      for (String processId : processIds) {
        HistoricTaskInstance historicTaskInstance = historyService
            .createHistoricTaskInstanceQuery()
            .processInstanceId(processId)
            .singleResult();

        assertThat(historicTaskInstance.getDeleteReason()).isEqualTo(deleteReason);
      }
    }
  }

  protected void assertHistoricBatchExists(ProcessEngineTestRule testRule) {
    if (testRule.isHistoryLevelFull()) {
      assertThat(historyService.createHistoricBatchQuery().count()).isEqualTo(1L);
    }
  }

}
