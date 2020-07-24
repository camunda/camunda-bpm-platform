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
package org.camunda.bpm.engine.test.api.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

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
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class BatchModificationHistoryTest {

  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(rule);
  protected BatchModificationHelper helper = new BatchModificationHelper(rule);

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected RuntimeService runtimeService;
  protected BpmnModelInstance instance;

  private int defaultBatchJobsPerSeed;
  private int defaultInvocationsPerBatchJob;
  private boolean defaultEnsureJobDueDateSet;

  protected static final Date START_DATE = new Date(1457326800000L);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testRule);

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

  @Before
  public void setClock() {
    ClockUtil.setCurrentTime(START_DATE);
  }

  @Before
  public void createBpmnModelInstance() {
    this.instance = Bpmn.createExecutableProcess("process1")
        .startEvent("start")
        .userTask("user1")
        .sequenceFlowId("seq")
        .userTask("user2")
        .endEvent("end")
        .done();
  }

  @Before
  public void initServices() {
    runtimeService = rule.getRuntimeService();
  }

  @Before
  public void storeEngineSettings() {
    processEngineConfiguration = rule.getProcessEngineConfiguration();
    defaultBatchJobsPerSeed = processEngineConfiguration.getBatchJobsPerSeed();
    defaultInvocationsPerBatchJob = processEngineConfiguration.getInvocationsPerBatchJob();
    defaultEnsureJobDueDateSet = processEngineConfiguration.isEnsureJobDueDateNotNull();
    processEngineConfiguration.setEnsureJobDueDateNotNull(ensureJobDueDateSet);
  }

  @After
  public void removeInstanceIds() {
    helper.currentProcessInstances = new ArrayList<String>();
  }

  @After
  public void removeBatches() {
    helper.removeAllRunningAndHistoricBatches();
  }

  @After
  public void restoreEngineSettings() {
    processEngineConfiguration.setBatchJobsPerSeed(defaultBatchJobsPerSeed);
    processEngineConfiguration.setInvocationsPerBatchJob(defaultInvocationsPerBatchJob);
    processEngineConfiguration.setEnsureJobDueDateNotNull(defaultEnsureJobDueDateSet);
  }

  @After
  public void resetClock() {
    ClockUtil.reset();
  }

  @Test
  public void testHistoricBatchCreation() {
    // when
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    Batch batch = helper.startAfterAsync("process1", 10, "user1", processDefinition.getId());

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
    assertNull(historicBatch.getEndTime());
  }

  @Test
  public void testHistoricBatchCompletion() {
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    Batch batch = helper.startAfterAsync("process1", 1, "user1", processDefinition.getId());
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
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    Batch batch = helper.cancelAllAsync("process1", 1, "user1", processDefinition.getId());

    // then a historic job log exists for the seed job
    HistoricJobLog jobLog = helper.getHistoricSeedJobLog(batch).get(0);
    assertNotNull(jobLog);
    assertTrue(jobLog.isCreationLog());
    assertEquals(batch.getSeedJobDefinitionId(), jobLog.getJobDefinitionId());
    assertEquals(BatchSeedJobHandler.TYPE, jobLog.getJobDefinitionType());
    assertEquals(batch.getId(), jobLog.getJobDefinitionConfiguration());
    assertEquals(START_DATE, jobLog.getTimestamp());
    assertEquals(processDefinition.getDeploymentId(), jobLog.getDeploymentId());
    assertNull(jobLog.getProcessDefinitionId());
    assertNull(jobLog.getExecutionId());
    assertEquals(currentTime, jobLog.getJobDueDate());

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
    assertEquals(processDefinition.getDeploymentId(), jobLog.getDeploymentId());
    assertNull(jobLog.getProcessDefinitionId());
    assertNull(jobLog.getExecutionId());
    assertEquals(currentTime, jobLog.getJobDueDate());

  }

  @Test
  public void testHistoricMonitorJobLog() {
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    Batch batch = helper.startAfterAsync("process1", 1, "user1", processDefinition.getId());

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

    // when the modification and monitor jobs are executed
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
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    Batch batch = helper.startAfterAsync("process1", 1, "user1", processDefinition.getId());
    helper.completeSeedJobs(batch);

    // when
    Date executionDate = helper.addSecondsToClock(12);
    helper.executeJobs(batch);

    // then a historic job log exists for the batch job
    HistoricJobLog jobLog = helper.getHistoricBatchJobLog(batch).get(0);
    assertNotNull(jobLog);
    assertTrue(jobLog.isCreationLog());
    assertEquals(batch.getBatchJobDefinitionId(), jobLog.getJobDefinitionId());
    assertEquals(Batch.TYPE_PROCESS_INSTANCE_MODIFICATION, jobLog.getJobDefinitionType());
    assertEquals(batch.getId(), jobLog.getJobDefinitionConfiguration());
    assertEquals(START_DATE, jobLog.getTimestamp());
    assertEquals(processDefinition.getDeploymentId(), jobLog.getDeploymentId());
    assertNull(jobLog.getProcessDefinitionId());
    assertNull(jobLog.getExecutionId());
    assertEquals(currentTime, jobLog.getJobDueDate());

    jobLog = helper.getHistoricBatchJobLog(batch).get(1);
    assertNotNull(jobLog);
    assertTrue(jobLog.isSuccessLog());
    assertEquals(batch.getBatchJobDefinitionId(), jobLog.getJobDefinitionId());
    assertEquals(Batch.TYPE_PROCESS_INSTANCE_MODIFICATION, jobLog.getJobDefinitionType());
    assertEquals(batch.getId(), jobLog.getJobDefinitionConfiguration());
    assertEquals(executionDate, jobLog.getTimestamp());
    assertEquals(processDefinition.getDeploymentId(), jobLog.getDeploymentId());
    assertNull(jobLog.getProcessDefinitionId());
    assertNull(jobLog.getExecutionId());
    assertEquals(currentTime, jobLog.getJobDueDate());
  }

  @Test
  public void testHistoricBatchForBatchDeletion() {
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    Batch batch = helper.startTransitionAsync("process1", 1, "seq", processDefinition.getId());

    // when
    Date deletionDate = helper.addSecondsToClock(12);
    rule.getManagementService().deleteBatch(batch.getId(), false);

    // then the end time was set for the historic batch
    HistoricBatch historicBatch = helper.getHistoricBatch(batch);
    assertNotNull(historicBatch);
    assertEquals(deletionDate, historicBatch.getEndTime());
  }

  @Test
  public void testHistoricSeedJobLogForBatchDeletion() {
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    Batch batch = helper.startBeforeAsync("process1", 1, "user1", processDefinition.getId());

    // when
    Date deletionDate = helper.addSecondsToClock(12);
    rule.getManagementService().deleteBatch(batch.getId(), false);

    // then a deletion historic job log was added
    HistoricJobLog jobLog = helper.getHistoricSeedJobLog(batch).get(1);
    assertNotNull(jobLog);
    assertTrue(jobLog.isDeletionLog());
    assertEquals(deletionDate, jobLog.getTimestamp());
  }

  @Test
  public void testHistoricMonitorJobLogForBatchDeletion() {
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    Batch batch = helper.startAfterAsync("process1", 1, "user1", processDefinition.getId());
    helper.completeSeedJobs(batch);

    // when
    Date deletionDate = helper.addSecondsToClock(12);
    rule.getManagementService().deleteBatch(batch.getId(), false);

    // then a deletion historic job log was added
    HistoricJobLog jobLog = helper.getHistoricMonitorJobLog(batch).get(1);
    assertNotNull(jobLog);
    assertTrue(jobLog.isDeletionLog());
    assertEquals(deletionDate, jobLog.getTimestamp());
  }

  @Test
  public void testHistoricBatchJobLogForBatchDeletion() {
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    Batch batch = helper.startBeforeAsync("process1", 1, "user2", processDefinition.getId());
    helper.completeSeedJobs(batch);

    // when
    Date deletionDate = helper.addSecondsToClock(12);
    rule.getManagementService().deleteBatch(batch.getId(), false);

    // then a deletion historic job log was added
    HistoricJobLog jobLog = helper.getHistoricBatchJobLog(batch).get(1);
    assertNotNull(jobLog);
    assertTrue(jobLog.isDeletionLog());
    assertEquals(deletionDate, jobLog.getTimestamp());
  }

  @Test
  public void testDeleteHistoricBatch() {
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    Batch batch = helper.startTransitionAsync("process1", 1, "seq", processDefinition.getId());
    helper.completeSeedJobs(batch);
    helper.executeJobs(batch);
    helper.executeMonitorJob(batch);

    // when
    HistoricBatch historicBatch = helper.getHistoricBatch(batch);
    rule.getHistoryService().deleteHistoricBatch(historicBatch.getId());

    // then the historic batch was removed and all job logs
    assertNull(helper.getHistoricBatch(batch));
    assertTrue(helper.getHistoricSeedJobLog(batch).isEmpty());
    assertTrue(helper.getHistoricMonitorJobLog(batch).isEmpty());
    assertTrue(helper.getHistoricBatchJobLog(batch).isEmpty());
  }

  @Test
  public void testHistoricSeedJobIncidentDeletion() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    Batch batch = helper.startBeforeAsync("process1", 1, "user2", processDefinition.getId());

    Job seedJob = helper.getSeedJob(batch);
    rule.getManagementService().setJobRetries(seedJob.getId(), 0);

    rule.getManagementService().deleteBatch(batch.getId(), false);

    // when
    rule.getHistoryService().deleteHistoricBatch(batch.getId());

    // then the historic incident was deleted
    long historicIncidents = rule.getHistoryService().createHistoricIncidentQuery().count();
    assertEquals(0, historicIncidents);
  }

  @Test
  public void testHistoricMonitorJobIncidentDeletion() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    Batch batch = helper.startTransitionAsync("process1", 1, "seq", processDefinition.getId());

    helper.completeSeedJobs(batch);
    Job monitorJob = helper.getMonitorJob(batch);
    rule.getManagementService().setJobRetries(monitorJob.getId(), 0);

    rule.getManagementService().deleteBatch(batch.getId(), false);

    // when
    rule.getHistoryService().deleteHistoricBatch(batch.getId());

    // then the historic incident was deleted
    long historicIncidents = rule.getHistoryService().createHistoricIncidentQuery().count();
    assertEquals(0, historicIncidents);
  }

  @Test
  public void testHistoricBatchJobLogIncidentDeletion() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    Batch batch = helper.startAfterAsync("process1", 3, "user1", processDefinition.getId());

    helper.completeSeedJobs(batch);
    helper.failExecutionJobs(batch, 3);

    rule.getManagementService().deleteBatch(batch.getId(), false);

    // when
    rule.getHistoryService().deleteHistoricBatch(batch.getId());

    // then the historic incident was deleted
    long historicIncidents = rule.getHistoryService().createHistoricIncidentQuery().count();
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
