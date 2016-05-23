/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.engine.test.api.runtime.migration.batch;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.impl.batch.BatchMonitorJobHandler;
import org.camunda.bpm.engine.impl.batch.BatchSeedJobHandler;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.MigrationTestRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;

public class BatchMigrationHelper {

  protected ProcessEngineRule engineRule;
  protected MigrationTestRule migrationRule;

  public ProcessDefinition sourceProcessDefinition;
  public ProcessDefinition targetProcessDefinition;

  public BatchMigrationHelper(ProcessEngineRule engineRule, MigrationTestRule migrationRule) {
    this.engineRule = engineRule;
    this.migrationRule = migrationRule;
  }

  public BatchMigrationHelper(ProcessEngineRule engineRule) {
    this(engineRule, null);
  }

  public ProcessDefinition getSourceProcessDefinition() {
    return sourceProcessDefinition;
  }

  public ProcessDefinition getTargetProcessDefinition() {
    return targetProcessDefinition;
  }

  public Batch createMigrationBatchWithSize(int batchSize) {
    int invocationsPerBatchJob = ((ProcessEngineConfigurationImpl) engineRule.getProcessEngine().getProcessEngineConfiguration()).getInvocationsPerBatchJob();
    return migrateProcessInstancesAsync(invocationsPerBatchJob * batchSize);
  }

  public Batch migrateProcessInstancesAsync(int numberOfProcessInstances) {
    sourceProcessDefinition = migrationRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    targetProcessDefinition = migrationRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    return migrateProcessInstancesAsync(numberOfProcessInstances, sourceProcessDefinition, targetProcessDefinition);
  }

  public Batch migrateProcessInstancesAsyncForTenant(int numberOfProcessInstances, String tenantId) {
    sourceProcessDefinition = migrationRule.deployForTenantAndGetDefinition(tenantId, ProcessModels.ONE_TASK_PROCESS);
    targetProcessDefinition = migrationRule.deployForTenantAndGetDefinition(tenantId, ProcessModels.ONE_TASK_PROCESS);
    return migrateProcessInstancesAsync(numberOfProcessInstances, sourceProcessDefinition, targetProcessDefinition);
  }

  public Batch migrateProcessInstanceAsync(ProcessDefinition sourceProcessDefinition, ProcessDefinition targetProcessDefinition) {
    return migrateProcessInstancesAsync(1, sourceProcessDefinition, targetProcessDefinition);
  }

  public Batch migrateProcessInstancesAsync(int numberOfProcessInstances, ProcessDefinition sourceProcessDefinition, ProcessDefinition targetProcessDefinition) {
    RuntimeService runtimeService = engineRule.getRuntimeService();

    List<String> processInstanceIds = new ArrayList<String>(numberOfProcessInstances);
    for (int i = 0; i < numberOfProcessInstances; i++) {
      processInstanceIds.add(
        runtimeService.startProcessInstanceById(sourceProcessDefinition.getId()).getId());
    }

    MigrationPlan migrationPlan = engineRule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    return runtimeService.newMigration(migrationPlan).processInstanceIds(processInstanceIds).executeAsync();
  }

  public Job getJobForDefinition(JobDefinition jobDefinition) {
    if (jobDefinition != null) {
      return engineRule.getManagementService()
        .createJobQuery().jobDefinitionId(jobDefinition.getId()).singleResult();
    }
    else {
      return null;
    }
  }

  public List<Job> getJobsForDefinition(JobDefinition jobDefinition) {
    return engineRule.getManagementService()
      .createJobQuery().jobDefinitionId(jobDefinition.getId()).list();
  }

  public void executeJob(Job job) {
    assertNotNull("Job to execute does not exist", job);
    try {
      engineRule.getManagementService().executeJob(job.getId());
    }
    catch (Exception e) {
      // ignore
    }
  }

  public JobDefinition getSeedJobDefinition(Batch batch) {
    return engineRule.getManagementService()
      .createJobDefinitionQuery().jobDefinitionId(batch.getSeedJobDefinitionId()).jobType(BatchSeedJobHandler.TYPE).singleResult();
  }

  public Job getSeedJob(Batch batch) {
    return getJobForDefinition(getSeedJobDefinition(batch));
  }

  public void executeSeedJob(Batch batch) {
    executeJob(getSeedJob(batch));
  }

  public JobDefinition getMonitorJobDefinition(Batch batch) {
    return engineRule.getManagementService()
      .createJobDefinitionQuery().jobDefinitionId(batch.getMonitorJobDefinitionId()).jobType(BatchMonitorJobHandler.TYPE).singleResult();
  }

  public Job getMonitorJob(Batch batch) {
    return getJobForDefinition(getMonitorJobDefinition(batch));
  }

  public void executeMonitorJob(Batch batch) {
    executeJob(getMonitorJob(batch));
  }

  public JobDefinition getMigrationJobDefinition(Batch batch) {
    return engineRule.getManagementService()
      .createJobDefinitionQuery().jobDefinitionId(batch.getBatchJobDefinitionId()).jobType(Batch.TYPE_PROCESS_INSTANCE_MIGRATION).singleResult();
  }

  public List<Job> getMigrationJobs(Batch batch) {
    return getJobsForDefinition(getMigrationJobDefinition(batch));
  }

  public void executeMigrationJobs(Batch batch) {
    for (Job migrationJob : getMigrationJobs(batch)) {
      executeJob(migrationJob);
    }
  }

  public void completeBatch(Batch batch) {
    completeSeedJobs(batch);
    completeMigrationJobs(batch);
    completeMonitorJobs(batch);
  }

  public void completeSeedJobs(Batch batch) {
    while (getSeedJob(batch) != null) {
      executeSeedJob(batch);
    }
  }

  public void completeMigrationJobs(Batch batch) {
    while (!getMigrationJobs(batch).isEmpty()) {
      executeMigrationJobs(batch);
    }
  }

  public void completeMigrationJobs(Batch batch, int count) {
    List<Job> migrationJobs = getMigrationJobs(batch);
    assertTrue(migrationJobs.size() >= count);
    for (int i = 0; i < count; i++) {
      executeJob(migrationJobs.get(i));
    }
  }

  public void failMigrationJobs(Batch batch, int count) {
    setRetries(batch, count, 0);
  }

  public void setRetries(Batch batch, int count, int retries) {
    List<Job> migrationJobs = getMigrationJobs(batch);
    assertTrue(migrationJobs.size() >= count);

    ManagementService managementService = engineRule.getManagementService();
    for (int i = 0; i < count; i++) {
      managementService.setJobRetries(migrationJobs.get(i).getId(), retries);
    }

  }

  public void completeMonitorJobs(Batch batch) {
    while (getMonitorJob(batch) != null) {
      executeMonitorJob(batch);
    }
  }

  public HistoricBatch getHistoricBatch(Batch batch) {
    return engineRule.getHistoryService()
      .createHistoricBatchQuery()
      .batchId(batch.getId())
      .singleResult();
  }

  public List<HistoricJobLog> getHistoricSeedJobLog(Batch batch) {
    return engineRule.getHistoryService()
      .createHistoricJobLogQuery()
      .jobDefinitionId(batch.getSeedJobDefinitionId())
      .orderPartiallyByOccurrence()
      .asc()
      .list();
  }

  public List<HistoricJobLog> getHistoricMonitorJobLog(Batch batch) {
    return engineRule.getHistoryService()
      .createHistoricJobLogQuery()
      .jobDefinitionId(batch.getMonitorJobDefinitionId())
      .orderPartiallyByOccurrence()
      .asc()
      .list();
  }

  public List<HistoricJobLog> getHistoricMonitorJobLog(Batch batch, Job monitorJob) {
    return engineRule.getHistoryService()
      .createHistoricJobLogQuery()
      .jobDefinitionId(batch.getMonitorJobDefinitionId())
      .jobId(monitorJob.getId())
      .orderPartiallyByOccurrence()
      .asc()
      .list();
  }

  public List<HistoricJobLog> getHistoricBatchJobLog(Batch batch) {
    return engineRule.getHistoryService()
      .createHistoricJobLogQuery()
      .jobDefinitionId(batch.getBatchJobDefinitionId())
      .orderPartiallyByOccurrence()
      .asc()
      .list();
  }

  public long countSourceProcessInstances() {
    return engineRule.getRuntimeService()
      .createProcessInstanceQuery().processDefinitionId(sourceProcessDefinition.getId()).count();
  }

  public long countTargetProcessInstances() {
    return engineRule.getRuntimeService()
      .createProcessInstanceQuery().processDefinitionId(targetProcessDefinition.getId()).count();
  }

  public Date addSeconds(Date date, int seconds) {
    return new Date(date.getTime() + seconds * 1000);
  }

  public Date addSecondsToClock(int seconds) {
    Date newDate = addSeconds(ClockUtil.getCurrentTime(), seconds);
    ClockUtil.setCurrentTime(newDate);
    return newDate;
  }

  /**
   * Remove all batches and historic batches. Usually called in {@link org.junit.After} method.
   */
  public void removeAllRunningAndHistoricBatches() {
    HistoryService historyService = engineRule.getHistoryService();
    ManagementService managementService = engineRule.getManagementService();

    for (Batch batch : managementService.createBatchQuery().list()) {
      managementService.deleteBatch(batch.getId(), true);
    }

    // remove history of completed batches
    for (HistoricBatch historicBatch : historyService.createHistoricBatchQuery().list()) {
      historyService.deleteHistoricBatch(historicBatch.getId());
    }

  }

}
