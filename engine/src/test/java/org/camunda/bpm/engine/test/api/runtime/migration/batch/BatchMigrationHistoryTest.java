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
package org.camunda.bpm.engine.test.api.runtime.migration.batch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.impl.batch.BatchMonitorJobHandler;
import org.camunda.bpm.engine.impl.batch.BatchSeedJobHandler;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.runtime.migration.MigrationTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class BatchMigrationHistoryTest {

  protected static final Date START_DATE = new Date(1457326800000L);

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected MigrationTestRule migrationRule = new MigrationTestRule(engineRule);
  protected BatchMigrationHelper helper = new BatchMigrationHelper(engineRule, migrationRule);

  protected ProcessEngineConfigurationImpl configuration;
  protected RuntimeService runtimeService;
  protected ManagementService managementService;
  protected HistoryService historyService;

  protected ProcessDefinition sourceProcessDefinition;
  protected ProcessDefinition targetProcessDefinition;

  protected boolean defaultEnsureJobDueDateSet;

  @Parameterized.Parameter(0)
  public boolean ensureJobDueDateSet;

  @Parameterized.Parameter(1)
  public Date currentTime;

  @Parameterized.Parameters(name = "Job DueDate is set: {0}")
  public static Collection<Object[]> scenarios() throws ParseException {
    return Arrays.asList(new Object[][] {
      { false, null },
      { true, START_DATE }
    });
  }

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(migrationRule);

  @Before
  public void initServices() {
    runtimeService = engineRule.getRuntimeService();
    managementService = engineRule.getManagementService();
    historyService = engineRule.getHistoryService();
  }

  @Before
  public void setupConfiguration() {
    configuration = engineRule.getProcessEngineConfiguration();
    defaultEnsureJobDueDateSet = configuration.isEnsureJobDueDateNotNull();
    configuration.setEnsureJobDueDateNotNull(ensureJobDueDateSet);
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
    helper.removeAllRunningAndHistoricBatches();
  }

  @After
  public void resetConfiguration() {
    configuration.setEnsureJobDueDateNotNull(defaultEnsureJobDueDateSet);
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
    assertEquals(batch.getTotalJobs(), historicBatch.getTotalJobs());
    assertEquals(batch.getBatchJobsPerSeed(), historicBatch.getBatchJobsPerSeed());
    assertEquals(batch.getInvocationsPerBatchJob(), historicBatch.getInvocationsPerBatchJob());
    assertEquals(batch.getSeedJobDefinitionId(), historicBatch.getSeedJobDefinitionId());
    assertEquals(batch.getMonitorJobDefinitionId(), historicBatch.getMonitorJobDefinitionId());
    assertEquals(batch.getBatchJobDefinitionId(), historicBatch.getBatchJobDefinitionId());
    assertEquals(START_DATE, historicBatch.getStartTime());
    assertEquals(batch.getStartTime(), historicBatch.getStartTime());
    assertEquals(batch.getExecutionStartTime(), historicBatch.getExecutionStartTime());
    assertNull(historicBatch.getEndTime());
  }

  @Test
  public void testHistoricBatchCompletion() {
    Batch batch = helper.migrateProcessInstancesAsync(1);
    helper.completeSeedJobs(batch);
    helper.executeJobs(batch);

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
    assertEquals(helper.sourceProcessDefinition.getDeploymentId(), jobLog.getDeploymentId());
    assertNull(jobLog.getProcessDefinitionId());
    assertNull(jobLog.getExecutionId());
    assertEquals(currentTime, jobLog.getJobDueDate());

    // when the seed job is executed
    Date executionDate = helper.addSecondsToClock(12);
    helper.completeSeedJobs(batch);

    // then a new historic job log exists for the seed job
    jobLog = helper.getHistoricSeedJobLog(batch).get(1);
    assertNotNull(jobLog);
    assertTrue(jobLog.isSuccessLog());
    assertEquals(batch.getSeedJobDefinitionId(), jobLog.getJobDefinitionId());
    assertEquals(BatchSeedJobHandler.TYPE, jobLog.getJobDefinitionType());
    assertEquals(batch.getId(), jobLog.getJobDefinitionConfiguration());
    assertEquals(executionDate, jobLog.getTimestamp());
    assertEquals(helper.sourceProcessDefinition.getDeploymentId(), jobLog.getDeploymentId());
    assertNull(jobLog.getProcessDefinitionId());
    assertNull(jobLog.getExecutionId());
    assertEquals(currentTime, jobLog.getJobDueDate());

  }

  @Test
  public void testHistoricMonitorJobLog() {
    Batch batch = helper.migrateProcessInstancesAsync(1);

    // when the seed job is executed
    helper.completeSeedJobs(batch);

    Job monitorJob = helper.getMonitorJob(batch);
    List<HistoricJobLog> jobLogs = helper.getHistoricMonitorJobLog(batch, monitorJob);
    assertEquals(1, jobLogs.size());

    // then a creation historic job log exists for the monitor job without due date
    HistoricJobLog jobLog = jobLogs.get(0);
    assertCommonMonitorJobLogProperties(batch, jobLog);
    assertTrue(jobLog.isCreationLog());
    assertEquals(START_DATE, jobLog.getTimestamp());
    assertEquals(currentTime, jobLog.getJobDueDate());

    // when the monitor job is executed
    Date executionDate = helper.addSecondsToClock(15);
    Date monitorJobDueDate = helper.addSeconds(executionDate, 30);
    helper.executeMonitorJob(batch);

    jobLogs = helper.getHistoricMonitorJobLog(batch, monitorJob);
    assertEquals(2, jobLogs.size());

    // then a success job log was created for the last monitor job
    jobLog = jobLogs.get(1);
    assertCommonMonitorJobLogProperties(batch, jobLog);
    assertTrue(jobLog.isSuccessLog());
    assertEquals(executionDate, jobLog.getTimestamp());
    assertEquals(currentTime, jobLog.getJobDueDate());

    // and a creation job log for the new monitor job was created with due date
    monitorJob = helper.getMonitorJob(batch);
    jobLogs = helper.getHistoricMonitorJobLog(batch, monitorJob);
    assertEquals(1, jobLogs.size());

    jobLog = jobLogs.get(0);
    assertCommonMonitorJobLogProperties(batch, jobLog);
    assertTrue(jobLog.isCreationLog());
    assertEquals(executionDate, jobLog.getTimestamp());
    assertEquals(monitorJobDueDate, jobLog.getJobDueDate());

    // when the migration and monitor jobs are executed
    executionDate = helper.addSecondsToClock(15);
    helper.executeJobs(batch);
    helper.executeMonitorJob(batch);

    jobLogs = helper.getHistoricMonitorJobLog(batch, monitorJob);
    assertEquals(2, jobLogs.size());

    // then a success job log was created for the last monitor job
    jobLog = jobLogs.get(1);
    assertCommonMonitorJobLogProperties(batch, jobLog);
    assertTrue(jobLog.isSuccessLog());
    assertEquals(executionDate, jobLog.getTimestamp());
    assertEquals(monitorJobDueDate, jobLog.getJobDueDate());
  }

  @Test
  public void testHistoricBatchJobLog() {
    Batch batch = helper.migrateProcessInstancesAsync(1);
    helper.completeSeedJobs(batch);

    String sourceDeploymentId = helper.getSourceProcessDefinition().getDeploymentId();

    // when
    Date executionDate = helper.addSecondsToClock(12);
    helper.executeJobs(batch);

    // then a historic job log exists for the batch job
    HistoricJobLog jobLog = helper.getHistoricBatchJobLog(batch).get(0);
    assertNotNull(jobLog);
    assertTrue(jobLog.isCreationLog());
    assertEquals(batch.getBatchJobDefinitionId(), jobLog.getJobDefinitionId());
    assertEquals(Batch.TYPE_PROCESS_INSTANCE_MIGRATION, jobLog.getJobDefinitionType());
    assertEquals(batch.getId(), jobLog.getJobDefinitionConfiguration());
    assertEquals(START_DATE, jobLog.getTimestamp());
    assertEquals(sourceDeploymentId, jobLog.getDeploymentId());
    assertNull(jobLog.getProcessDefinitionId());
    assertNull(jobLog.getExecutionId());
    assertEquals(currentTime, jobLog.getJobDueDate());

    jobLog = helper.getHistoricBatchJobLog(batch).get(1);
    assertNotNull(jobLog);
    assertTrue(jobLog.isSuccessLog());
    assertEquals(batch.getBatchJobDefinitionId(), jobLog.getJobDefinitionId());
    assertEquals(Batch.TYPE_PROCESS_INSTANCE_MIGRATION, jobLog.getJobDefinitionType());
    assertEquals(batch.getId(), jobLog.getJobDefinitionConfiguration());
    assertEquals(executionDate, jobLog.getTimestamp());
    assertEquals(sourceDeploymentId, jobLog.getDeploymentId());
    assertNull(jobLog.getProcessDefinitionId());
    assertNull(jobLog.getExecutionId());
    assertEquals(currentTime, jobLog.getJobDueDate());
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
    helper.completeSeedJobs(batch);

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
    helper.completeSeedJobs(batch);

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
    helper.completeSeedJobs(batch);
    helper.executeJobs(batch);
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

  @Test
  public void testHistoricSeedJobIncidentDeletion() {
    // given
    Batch batch = helper.migrateProcessInstancesAsync(1);

    Job seedJob = helper.getSeedJob(batch);
    managementService.setJobRetries(seedJob.getId(), 0);

    managementService.deleteBatch(batch.getId(), false);

    // when
    historyService.deleteHistoricBatch(batch.getId());

    // then the historic incident was deleted
    long historicIncidents = historyService.createHistoricIncidentQuery().count();
    assertEquals(0, historicIncidents);
  }

  @Test
  public void testHistoricMonitorJobIncidentDeletion() {
    // given
    Batch batch = helper.migrateProcessInstancesAsync(1);

    helper.completeSeedJobs(batch);
    Job monitorJob = helper.getMonitorJob(batch);
    managementService.setJobRetries(monitorJob.getId(), 0);

    managementService.deleteBatch(batch.getId(), false);

    // when
    historyService.deleteHistoricBatch(batch.getId());

    // then the historic incident was deleted
    long historicIncidents = historyService.createHistoricIncidentQuery().count();
    assertEquals(0, historicIncidents);
  }

  @Test
  public void testHistoricBatchJobLogIncidentDeletion() {
    // given
    Batch batch = helper.migrateProcessInstancesAsync(3);

    helper.completeSeedJobs(batch);
    helper.failExecutionJobs(batch, 3);

    managementService.deleteBatch(batch.getId(), false);

    // when
    historyService.deleteHistoricBatch(batch.getId());

    // then the historic incident was deleted
    long historicIncidents = historyService.createHistoricIncidentQuery().count();
    assertEquals(0, historicIncidents);
  }

  protected void assertCommonMonitorJobLogProperties(Batch batch, HistoricJobLog jobLog) {
    assertNotNull(jobLog);
    assertEquals(batch.getMonitorJobDefinitionId(), jobLog.getJobDefinitionId());
    assertEquals(BatchMonitorJobHandler.TYPE, jobLog.getJobDefinitionType());
    assertEquals(batch.getId(), jobLog.getJobDefinitionConfiguration());
    assertNull(jobLog.getDeploymentId());
    assertNull(jobLog.getProcessDefinitionId());
    assertNull(jobLog.getExecutionId());
  }


}
