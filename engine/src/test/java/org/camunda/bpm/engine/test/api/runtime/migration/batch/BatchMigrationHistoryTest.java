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
package org.camunda.bpm.engine.test.api.runtime.migration.batch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.impl.batch.BatchMonitorJobHandler;
import org.camunda.bpm.engine.impl.batch.BatchSeedJobHandler;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.migration.batch.MigrationBatchJobHandler;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.MigrationTestRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class BatchMigrationHistoryTest {

  protected static final Date START_DATE = new Date(1457326800000L);

  protected ProcessEngineRule engineRule = new ProcessEngineRule(true);
  protected MigrationTestRule migrationRule = new MigrationTestRule(engineRule);
  protected BatchMigrationHelper helper = new BatchMigrationHelper(engineRule, migrationRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(migrationRule);

  protected ProcessEngineConfigurationImpl configuration;
  protected RuntimeService runtimeService;
  protected ManagementService managementService;
  protected HistoryService historyService;

  protected ProcessDefinition sourceProcessDefinition;
  protected ProcessDefinition targetProcessDefinition;

  @Before
  public void initServices() {
    runtimeService = engineRule.getRuntimeService();
    managementService = engineRule.getManagementService();
    historyService = engineRule.getHistoryService();
  }

  @Before
  public void setClock() {
    ClockUtil.setCurrentTime(START_DATE);
  }

  @After
  public void resetClock() {
    ClockUtil.reset();
  }

  @After
  public void removeBatches() {
    for (Batch batch : managementService.createBatchQuery().list()) {
      managementService.deleteBatch(batch.getId(), true);
    }

    // remove history of completed batches
    for (HistoricBatch historicBatch : historyService.createHistoricBatchQuery().list()) {
      historyService.deleteHistoricBatch(historicBatch.getId());
    }
  }

  @Test
  public void testHistoricBatchCreation() {
    // when
    Batch batch = helper.migrateProcessInstancesAsync(10);

    // then a historic batch was created
    HistoricBatch historicBatch = helper.getHistoricBatch(batch);
    assertNotNull(historicBatch);
    assertEquals(batch.getId(), historicBatch.getId());
    assertEquals(batch.getType(), historicBatch.getType());
    assertEquals(batch.getSize(), historicBatch.getSize());
    assertEquals(batch.getBatchJobsPerSeed(), historicBatch.getBatchJobsPerSeed());
    assertEquals(batch.getInvocationsPerBatchJob(), historicBatch.getInvocationsPerBatchJob());
    assertEquals(batch.getSeedJobDefinitionId(), historicBatch.getSeedJobDefinitionId());
    assertEquals(batch.getMonitorJobDefinitionId(), historicBatch.getMonitorJobDefinitionId());
    assertEquals(batch.getBatchJobDefinitionId(), historicBatch.getBatchJobDefinitionId());
    assertEquals(START_DATE, historicBatch.getStartTime());
    assertNull(historicBatch.getEndTime());
  }

  @Test
  public void testHistoricBatchCompletion() {
    Batch batch = helper.migrateProcessInstancesAsync(1);
    helper.executeSeedJob(batch);
    helper.executeMigrationJobs(batch);

    Date endDate = helper.addSecondsToClock(12);

    // when
    helper.executeMonitorJob(batch);

    // then the historic batch has an end time set
    HistoricBatch historicBatch = helper.getHistoricBatch(batch);
    assertNotNull(historicBatch);
    assertEquals(endDate, historicBatch.getEndTime());
  }

  @Test
  public void testHistoricSeedJobLog() {
    // when
    Batch batch = helper.migrateProcessInstancesAsync(1);

    // then a historic job log exists for the seed job
    HistoricJobLog jobLog = helper.getHistoricSeedJobLog(batch).get(0);
    assertNotNull(jobLog);
    assertTrue(jobLog.isCreationLog());
    assertEquals(batch.getSeedJobDefinitionId(), jobLog.getJobDefinitionId());
    assertEquals(BatchSeedJobHandler.TYPE, jobLog.getJobDefinitionType());
    assertEquals(batch.getId(), jobLog.getJobDefinitionConfiguration());
    assertEquals(START_DATE, jobLog.getTimestamp());
    assertNull(jobLog.getDeploymentId());
    assertNull(jobLog.getProcessDefinitionId());
    assertNull(jobLog.getExecutionId());
    assertNull(jobLog.getJobDueDate());

    // when the seed job is executed
    Date executionDate = helper.addSecondsToClock(12);
    helper.executeSeedJob(batch);

    // then a new historic job log exists for the seed job
    jobLog = helper.getHistoricSeedJobLog(batch).get(1);
    assertNotNull(jobLog);
    assertTrue(jobLog.isSuccessLog());
    assertEquals(batch.getSeedJobDefinitionId(), jobLog.getJobDefinitionId());
    assertEquals(BatchSeedJobHandler.TYPE, jobLog.getJobDefinitionType());
    assertEquals(batch.getId(), jobLog.getJobDefinitionConfiguration());
    assertEquals(executionDate, jobLog.getTimestamp());
    assertNull(jobLog.getDeploymentId());
    assertNull(jobLog.getProcessDefinitionId());
    assertNull(jobLog.getExecutionId());
    assertNull(jobLog.getJobDueDate());

  }

  @Test
  public void testHistoricMonitorJobLog() {
    Batch batch = helper.migrateProcessInstancesAsync(1);

    // when
    helper.executeSeedJob(batch);

    // then a historic job log exists for the monitor job
    HistoricJobLog jobLog = helper.getHistoricMonitorJobLog(batch).get(0);
    assertNotNull(jobLog);
    assertTrue(jobLog.isCreationLog());
    assertEquals(batch.getMonitorJobDefinitionId(), jobLog.getJobDefinitionId());
    assertEquals(BatchMonitorJobHandler.TYPE, jobLog.getJobDefinitionType());
    assertEquals(batch.getId(), jobLog.getJobDefinitionConfiguration());
    assertEquals(START_DATE, jobLog.getTimestamp());
    assertNull(jobLog.getDeploymentId());
    assertNull(jobLog.getProcessDefinitionId());
    assertNull(jobLog.getExecutionId());
    assertEquals(helper.addSeconds(START_DATE, 30), jobLog.getJobDueDate());

    // when the migration and monitor job are executed
    Date executionDate = helper.addSecondsToClock(15);
    helper.executeMigrationJobs(batch);
    helper.executeMonitorJob(batch);

    // then the an new job log was created
    jobLog = helper.getHistoricMonitorJobLog(batch).get(1);
    assertNotNull(jobLog);
    assertTrue(jobLog.isSuccessLog());
    assertEquals(batch.getMonitorJobDefinitionId(), jobLog.getJobDefinitionId());
    assertEquals(BatchMonitorJobHandler.TYPE, jobLog.getJobDefinitionType());
    assertEquals(batch.getId(), jobLog.getJobDefinitionConfiguration());
    assertEquals(executionDate, jobLog.getTimestamp());
    assertNull(jobLog.getDeploymentId());
    assertNull(jobLog.getProcessDefinitionId());
    assertNull(jobLog.getExecutionId());
    assertEquals(helper.addSeconds(START_DATE, 30), jobLog.getJobDueDate());
  }

  @Test
  public void testHistoricBatchJobLog() {
    Batch batch = helper.migrateProcessInstancesAsync(1);
    helper.executeSeedJob(batch);

    // when
    Date executionDate = helper.addSecondsToClock(12);
    helper.executeMigrationJobs(batch);

    // then a historic job log exists for the batch job
    HistoricJobLog jobLog = helper.getHistoricBatchJobLog(batch).get(0);
    assertNotNull(jobLog);
    assertTrue(jobLog.isCreationLog());
    assertEquals(batch.getBatchJobDefinitionId(), jobLog.getJobDefinitionId());
    assertEquals(Batch.TYPE_PROCESS_INSTANCE_MIGRATION, jobLog.getJobDefinitionType());
    assertEquals(batch.getId(), jobLog.getJobDefinitionConfiguration());
    assertEquals(START_DATE, jobLog.getTimestamp());
    assertNull(jobLog.getDeploymentId());
    assertNull(jobLog.getProcessDefinitionId());
    assertNull(jobLog.getExecutionId());
    assertNull(jobLog.getJobDueDate());

    jobLog = helper.getHistoricBatchJobLog(batch).get(1);
    assertNotNull(jobLog);
    assertTrue(jobLog.isSuccessLog());
    assertEquals(batch.getBatchJobDefinitionId(), jobLog.getJobDefinitionId());
    assertEquals(Batch.TYPE_PROCESS_INSTANCE_MIGRATION, jobLog.getJobDefinitionType());
    assertEquals(batch.getId(), jobLog.getJobDefinitionConfiguration());
    assertEquals(executionDate, jobLog.getTimestamp());
    assertNull(jobLog.getDeploymentId());
    assertNull(jobLog.getProcessDefinitionId());
    assertNull(jobLog.getExecutionId());
    assertNull(jobLog.getJobDueDate());
  }

  @Test
  public void testHistoricBatchForBatchDeletion() {
    Batch batch = helper.migrateProcessInstancesAsync(1);

    // when
    Date deletionDate = helper.addSecondsToClock(12);
    managementService.deleteBatch(batch.getId(), false);

    // then the end time was set for the historic batch
    HistoricBatch historicBatch = helper.getHistoricBatch(batch);
    assertNotNull(historicBatch);
    assertEquals(deletionDate, historicBatch.getEndTime());
  }

  @Test
  public void testHistoricSeedJobLogForBatchDeletion() {
    Batch batch = helper.migrateProcessInstancesAsync(1);

    // when
    Date deletionDate = helper.addSecondsToClock(12);
    managementService.deleteBatch(batch.getId(), false);

    // then a deletion historic job log was added
    HistoricJobLog jobLog = helper.getHistoricSeedJobLog(batch).get(1);
    assertNotNull(jobLog);
    assertTrue(jobLog.isDeletionLog());
    assertEquals(deletionDate, jobLog.getTimestamp());
  }

  @Test
  public void testHistoricMonitorJobLogForBatchDeletion() {
    Batch batch = helper.migrateProcessInstancesAsync(1);
    helper.executeSeedJob(batch);

    // when
    Date deletionDate = helper.addSecondsToClock(12);
    managementService.deleteBatch(batch.getId(), false);

    // then a deletion historic job log was added
    HistoricJobLog jobLog = helper.getHistoricMonitorJobLog(batch).get(1);
    assertNotNull(jobLog);
    assertTrue(jobLog.isDeletionLog());
    assertEquals(deletionDate, jobLog.getTimestamp());
  }

  @Test
  public void testHistoricBatchJobLogForBatchDeletion() {
    Batch batch = helper.migrateProcessInstancesAsync(1);
    helper.executeSeedJob(batch);

    // when
    Date deletionDate = helper.addSecondsToClock(12);
    managementService.deleteBatch(batch.getId(), false);

    // then a deletion historic job log was added
    HistoricJobLog jobLog = helper.getHistoricBatchJobLog(batch).get(1);
    assertNotNull(jobLog);
    assertTrue(jobLog.isDeletionLog());
    assertEquals(deletionDate, jobLog.getTimestamp());
  }

  @Test
  public void testDeleteHistoricBatch() {
    Batch batch = helper.migrateProcessInstancesAsync(1);
    helper.executeSeedJob(batch);
    helper.executeMigrationJobs(batch);
    helper.executeMonitorJob(batch);

    // when
    HistoricBatch historicBatch = helper.getHistoricBatch(batch);
    historyService.deleteHistoricBatch(historicBatch.getId());

    // then the historic batch was removed and all job logs
    assertNull(helper.getHistoricBatch(batch));
    assertTrue(helper.getHistoricSeedJobLog(batch).isEmpty());
    assertTrue(helper.getHistoricMonitorJobLog(batch).isEmpty());
    assertTrue(helper.getHistoricBatchJobLog(batch).isEmpty());
  }

}
