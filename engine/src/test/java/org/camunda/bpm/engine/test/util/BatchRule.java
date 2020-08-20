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
package org.camunda.bpm.engine.test.util;

import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.util.ArrayList;
import java.util.List;

import static org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl.DEFAULT_INVOCATIONS_PER_BATCH_JOB;

public class BatchRule extends TestWatcher {

  protected ProcessEngineRule engineRule;
  protected ProcessEngineTestRule engineTestRule;

  public BatchRule(ProcessEngineRule engineRule, ProcessEngineTestRule engineTestRule) {
    this.engineRule = engineRule;
    this.engineTestRule = engineTestRule;
  }

  protected void finished(Description description) {
    engineRule.getProcessEngineConfiguration()
        .setInvocationsPerBatchJob(DEFAULT_INVOCATIONS_PER_BATCH_JOB);
    ClockUtil.reset();
    clearDatabase();
  }

  protected List<String> batchIds = new ArrayList<>();

  public void clearDatabase() {
    if (!batchIds.isEmpty()) {
      for (String batchId : batchIds) {
        HistoricBatch historicBatch = engineRule.getHistoryService().createHistoricBatchQuery()
            .batchId(batchId)
            .singleResult();

        if (historicBatch != null) {
          engineRule.getHistoryService().deleteHistoricBatch(historicBatch.getId());
        }
      }
    }
  }

  public void syncExec(Batch batch) {
    syncExec(batch, true);
  }

  public void syncExec(Batch batch, boolean isClear) {
    if (isClear) {
      batchIds.add(batch.getId());
    }

    executeSeedJobs(batch);

    List<Job> jobs = getExecutionJobs(batch);
    for (Job job : jobs) {
      engineRule.getManagementService().executeJob(job.getId());
    }

    engineRule.getManagementService().executeJob(
        getJobForDefinition(batch.getMonitorJobDefinitionId()).getId());
  }

  public void executeSeedJobs(Batch batch) {
    while (getSeedJob(batch) != null) {
      engineRule.getManagementService().executeJob(getSeedJob(batch).getId());
    }
  }

  public Job getSeedJob(Batch batch) {
    return getJobForDefinition(batch.getSeedJobDefinitionId());
  }

  protected Job getJobForDefinition(String definitionId) {
    return engineRule.getManagementService().createJobQuery()
        .jobDefinitionId(definitionId)
        .singleResult();
  }

  public List<Job> getExecutionJobs(Batch batch) {
    return engineRule.getManagementService().createJobQuery()
        .jobDefinitionId(batch.getBatchJobDefinitionId())
        .list();
  }

}