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
package org.camunda.bpm.engine.test.api.history;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.DecisionService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceQuery;
import org.camunda.bpm.engine.impl.batch.BatchSeedJobHandler;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.runtime.BatchHelper;
import org.camunda.bpm.engine.test.util.AssertUtil;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class BatchHistoricDecisionInstanceDeletionTest {

  protected static String DECISION = "decision";
  protected static final Date TEST_DATE = new Date(1457326800000L);

  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(rule);
  protected BatchDeletionHelper helper = new BatchDeletionHelper(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testRule);

  private int defaultBatchJobsPerSeed;
  private int defaultInvocationsPerBatchJob;
  private boolean defaultEnsureJobDueDateSet;

  protected ProcessEngineConfigurationImpl configuration;
  protected DecisionService decisionService;
  protected HistoryService historyService;

  protected List<String> decisionInstanceIds;

  @Parameterized.Parameter(0)
  public boolean ensureJobDueDateSet;

  @Parameterized.Parameter(1)
  public Date currentTime;

  @Parameterized.Parameters(name = "Job DueDate is set: {0}")
  public static Collection<Object[]> scenarios() throws ParseException {
    return Arrays.asList(new Object[][] {
      { false, null },
      { true, TEST_DATE }
    });
  }

  @Before
  public void setup() {
    ClockUtil.setCurrentTime(TEST_DATE);
    historyService = rule.getHistoryService();
    decisionService = rule.getDecisionService();
    decisionInstanceIds = new ArrayList<>();
  }

  @Before
  public void storeEngineSettings() {
    configuration = rule.getProcessEngineConfiguration();
    defaultEnsureJobDueDateSet = configuration.isEnsureJobDueDateNotNull();
    defaultBatchJobsPerSeed = configuration.getBatchJobsPerSeed();
    defaultInvocationsPerBatchJob = configuration.getInvocationsPerBatchJob();
    configuration.setEnsureJobDueDateNotNull(ensureJobDueDateSet);
  }

  @Before
  public void executeDecisionInstances() {
    testRule.deploy("org/camunda/bpm/engine/test/api/dmn/Example.dmn");

    VariableMap variables = Variables.createVariables()
        .putValue("status", "silver")
        .putValue("sum", 723);

    for (int i = 0; i < 10; i++) {
      decisionService.evaluateDecisionByKey(DECISION).variables(variables).evaluate();
    }

    List<HistoricDecisionInstance> decisionInstances = historyService.createHistoricDecisionInstanceQuery().list();
    for(HistoricDecisionInstance decisionInstance : decisionInstances) {
      decisionInstanceIds.add(decisionInstance.getId());
    }
  }

  @After
  public void restoreEngineSettings() {
    configuration.setBatchJobsPerSeed(defaultBatchJobsPerSeed);
    configuration.setInvocationsPerBatchJob(defaultInvocationsPerBatchJob);
    configuration.setEnsureJobDueDateNotNull(defaultEnsureJobDueDateSet);
  }

  @After
  public void removeBatches() {
    helper.removeAllRunningAndHistoricBatches();
    ClockUtil.reset();
  }

  @Test
  public void createBatchDeletionByIds() {
    // when
    Batch batch = historyService.deleteHistoricDecisionInstancesAsync(decisionInstanceIds, null);

    // then
    assertBatchCreated(batch, 10);
  }

  @Test
  public void createBatchDeletionByInvalidIds() {
    // when/then
    assertThatThrownBy(() -> historyService.deleteHistoricDecisionInstancesAsync((List<String>) null, null))
      .isInstanceOf(BadUserRequestException.class);
  }

  @Test
  public void createBatchDeletionByQuery() {
    // given
    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery().decisionDefinitionKey(DECISION);

    // when
    Batch batch = historyService.deleteHistoricDecisionInstancesAsync(query, null);

    // then
    assertBatchCreated(batch, 10);
  }

  @Test
  public void createBatchDeletionByInvalidQuery() {
    // when/then
    assertThatThrownBy(() -> historyService.deleteHistoricDecisionInstancesAsync((HistoricDecisionInstanceQuery) null, null))
      .isInstanceOf(BadUserRequestException.class);
  }

  @Test
  public void createBatchDeletionByInvalidQueryByKey() {
    // given
    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery().decisionDefinitionKey("foo");

    // when/then
    assertThatThrownBy(() -> historyService.deleteHistoricDecisionInstancesAsync(query, null))
      .isInstanceOf(BadUserRequestException.class);
  }

  @Test
  public void createBatchDeletionByIdsAndQuery() {
    // given
    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery().decisionDefinitionKey(DECISION);

    // when
    Batch batch = historyService.deleteHistoricDecisionInstancesAsync(decisionInstanceIds, query, null);

    // then
    assertBatchCreated(batch, 10);
  }

  @Test
  public void createSeedJobByIds() {
    // when
    Batch batch = historyService.deleteHistoricDecisionInstancesAsync(decisionInstanceIds, null);

    // then there exists a seed job definition with the batch id as
    // configuration
    JobDefinition seedJobDefinition = helper.getSeedJobDefinition(batch);
    assertNotNull(seedJobDefinition);
    assertNotNull(seedJobDefinition.getDeploymentId());
    assertEquals(batch.getId(), seedJobDefinition.getJobConfiguration());
    assertEquals(BatchSeedJobHandler.TYPE, seedJobDefinition.getJobType());

    // and there exists a deletion job definition
    JobDefinition deletionJobDefinition = helper.getExecutionJobDefinition(batch);
    assertNotNull(deletionJobDefinition);
    assertEquals(Batch.TYPE_HISTORIC_DECISION_INSTANCE_DELETION, deletionJobDefinition.getJobType());

    // and a seed job with no relation to a process or execution etc.
    Job seedJob = helper.getSeedJob(batch);
    assertNotNull(seedJob);
    assertEquals(seedJobDefinition.getId(), seedJob.getJobDefinitionId());
    assertEquals(currentTime, seedJob.getDuedate());
    assertEquals(seedJobDefinition.getDeploymentId(), seedJob.getDeploymentId());
    assertNull(seedJob.getProcessDefinitionId());
    assertNull(seedJob.getProcessDefinitionKey());
    assertNull(seedJob.getProcessInstanceId());
    assertNull(seedJob.getExecutionId());

    // but no deletion jobs where created
    List<Job> deletionJobs = helper.getExecutionJobs(batch);
    assertEquals(0, deletionJobs.size());
  }

  @Test
  public void createSeedJobByQuery() {
    // given
    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery().decisionDefinitionKey(DECISION);

    // when
    Batch batch = historyService.deleteHistoricDecisionInstancesAsync(decisionInstanceIds, query, null);

    // then there exists a seed job definition with the batch id as
    // configuration
    JobDefinition seedJobDefinition = helper.getSeedJobDefinition(batch);
    assertNotNull(seedJobDefinition);
    assertNotNull(seedJobDefinition.getDeploymentId());
    assertEquals(batch.getId(), seedJobDefinition.getJobConfiguration());
    assertEquals(BatchSeedJobHandler.TYPE, seedJobDefinition.getJobType());

    // and there exists a deletion job definition
    JobDefinition deletionJobDefinition = helper.getExecutionJobDefinition(batch);
    assertNotNull(deletionJobDefinition);
    assertEquals(Batch.TYPE_HISTORIC_DECISION_INSTANCE_DELETION, deletionJobDefinition.getJobType());

    // and a seed job with no relation to a process or execution etc.
    Job seedJob = helper.getSeedJob(batch);
    assertNotNull(seedJob);
    assertEquals(seedJobDefinition.getId(), seedJob.getJobDefinitionId());
    assertEquals(currentTime, seedJob.getDuedate());
    assertEquals(seedJobDefinition.getDeploymentId(), seedJob.getDeploymentId());
    assertNull(seedJob.getProcessDefinitionId());
    assertNull(seedJob.getProcessDefinitionKey());
    assertNull(seedJob.getProcessInstanceId());
    assertNull(seedJob.getExecutionId());

    // but no deletion jobs where created
    List<Job> deletionJobs = helper.getExecutionJobs(batch);
    assertEquals(0, deletionJobs.size());
  }

  @Test
  public void createSeedJobByIdsAndQuery() {
    // given
    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery().decisionDefinitionKey(DECISION);

    // when
    Batch batch = historyService.deleteHistoricDecisionInstancesAsync(query, null);

    // then there exists a seed job definition with the batch id as
    // configuration
    JobDefinition seedJobDefinition = helper.getSeedJobDefinition(batch);
    assertNotNull(seedJobDefinition);
    assertNotNull(seedJobDefinition.getDeploymentId());
    assertEquals(batch.getId(), seedJobDefinition.getJobConfiguration());
    assertEquals(BatchSeedJobHandler.TYPE, seedJobDefinition.getJobType());

    // and there exists a deletion job definition
    JobDefinition deletionJobDefinition = helper.getExecutionJobDefinition(batch);
    assertNotNull(deletionJobDefinition);
    assertEquals(Batch.TYPE_HISTORIC_DECISION_INSTANCE_DELETION, deletionJobDefinition.getJobType());

    // and a seed job with no relation to a process or execution etc.
    Job seedJob = helper.getSeedJob(batch);
    assertNotNull(seedJob);
    assertEquals(seedJobDefinition.getId(), seedJob.getJobDefinitionId());
    assertEquals(currentTime, seedJob.getDuedate());
    assertEquals(seedJobDefinition.getDeploymentId(), seedJob.getDeploymentId());
    assertNull(seedJob.getProcessDefinitionId());
    assertNull(seedJob.getProcessDefinitionKey());
    assertNull(seedJob.getProcessInstanceId());
    assertNull(seedJob.getExecutionId());

    // but no deletion jobs where created
    List<Job> deletionJobs = helper.getExecutionJobs(batch);
    assertEquals(0, deletionJobs.size());
  }

  @Test
  public void createDeletionJobsByIds() {
    // given
    rule.getProcessEngineConfiguration().setBatchJobsPerSeed(5);

    Batch batch = historyService.deleteHistoricDecisionInstancesAsync(decisionInstanceIds, null);

    JobDefinition seedJobDefinition = helper.getSeedJobDefinition(batch);
    JobDefinition deletionJobDefinition = helper.getExecutionJobDefinition(batch);;

    // when
    helper.executeSeedJob(batch);

    // then
    List<Job> deletionJobs = helper.getJobsForDefinition(deletionJobDefinition);
    assertEquals(5, deletionJobs.size());

    for (Job deletionJob : deletionJobs) {
      assertEquals(deletionJobDefinition.getId(), deletionJob.getJobDefinitionId());
      assertEquals(currentTime, deletionJob.getDuedate());
      assertNull(deletionJob.getProcessDefinitionId());
      assertNull(deletionJob.getProcessDefinitionKey());
      assertNull(deletionJob.getProcessInstanceId());
      assertNull(deletionJob.getExecutionId());
    }

    // and the seed job still exists
    Job seedJob = helper.getJobForDefinition(seedJobDefinition);
    assertNotNull(seedJob);
  }

  @Test
  public void createDeletionJobsByIdsInDifferentDeployments() {
    // given a second deployment and instances
    executeDecisionInstances();

    // assume
    List<DecisionDefinition> definitions = rule.getRepositoryService().createDecisionDefinitionQuery().orderByDecisionDefinitionVersion().asc().list();
    assertEquals(2, definitions.size());
    String deploymentIdOne = definitions.get(0).getDeploymentId();
    String deploymentIdTwo = definitions.get(1).getDeploymentId();

    Batch batch = historyService.deleteHistoricDecisionInstancesAsync(decisionInstanceIds, null);
    Job seedJob = helper.getSeedJob(batch);
    assertEquals(deploymentIdOne, seedJob.getDeploymentId());

    // when
    helper.executeSeedJob(batch);

    // then there is a second seed job with the same deployment id
    Job seedJobTwo = helper.getSeedJob(batch);
    assertNotNull(seedJobTwo);
    assertEquals(seedJob.getDeploymentId(), seedJobTwo.getDeploymentId());

    // when
    helper.executeSeedJob(batch);

    // then there is no seed job anymore and 10 deletion jobs for every deployment exist
    assertNull(helper.getSeedJob(batch));
    List<Job> deletionJobs = helper.getExecutionJobs(batch);
    assertEquals(20, deletionJobs.size());
    assertEquals(10L, getJobCountByDeployment(deletionJobs, deploymentIdOne));
    assertEquals(10L, getJobCountByDeployment(deletionJobs, deploymentIdTwo));
  }

  @Test
  public void createDeletionJobsByIdsWithDeletedDeployment() {
    // given a second deployment and instances
    executeDecisionInstances();

    List<DecisionDefinition> definitions = rule.getRepositoryService().createDecisionDefinitionQuery().orderByDecisionDefinitionVersion().asc().list();
    assertEquals(2, definitions.size());
    String deploymentIdOne = definitions.get(0).getDeploymentId();
    String deploymentIdTwo = definitions.get(1).getDeploymentId();

    // ... and the second deployment is deleted
    rule.getRepositoryService().deleteDeployment(deploymentIdTwo);

    Batch batch = historyService.deleteHistoricDecisionInstancesAsync(decisionInstanceIds, null);
    Job seedJob = helper.getSeedJob(batch);
    assertEquals(deploymentIdOne, seedJob.getDeploymentId());

    // when
    helper.executeSeedJob(batch);

    // then there is a second seed job with the same deployment id
    Job seedJobTwo = helper.getSeedJob(batch);
    assertNotNull(seedJobTwo);
    assertEquals(seedJob.getDeploymentId(), seedJobTwo.getDeploymentId());

    // when
    helper.executeSeedJob(batch);

    // then there is no seed job anymore
    assertNull(helper.getSeedJob(batch));

    // ...and 10 deletion jobs for the first deployment and 10 jobs for no deployment exist
    List<Job> deletionJobs = helper.getExecutionJobs(batch);
    assertEquals(20, deletionJobs.size());
    assertEquals(10L, getJobCountByDeployment(deletionJobs, deploymentIdOne));
    assertEquals(10L, getJobCountByDeployment(deletionJobs, null));

    // cleanup
    helper.executeJobs(batch);
  }

  @Test
  public void createDeletionJobsByQuery() {
    // given
    rule.getProcessEngineConfiguration().setBatchJobsPerSeed(5);

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery().decisionDefinitionKey(DECISION);

    Batch batch = historyService.deleteHistoricDecisionInstancesAsync(query, null);

    JobDefinition seedJobDefinition = helper.getSeedJobDefinition(batch);
    JobDefinition deletionJobDefinition = helper.getExecutionJobDefinition(batch);;

    // when
    helper.executeSeedJob(batch);

    // then
    List<Job> deletionJobs = helper.getJobsForDefinition(deletionJobDefinition);
    assertEquals(5, deletionJobs.size());

    for (Job deletionJob : deletionJobs) {
      assertEquals(deletionJobDefinition.getId(), deletionJob.getJobDefinitionId());
      assertEquals(currentTime, deletionJob.getDuedate());
      assertNull(deletionJob.getProcessDefinitionId());
      assertNull(deletionJob.getProcessDefinitionKey());
      assertNull(deletionJob.getProcessInstanceId());
      assertNull(deletionJob.getExecutionId());
    }

    // and the seed job still exists
    Job seedJob = helper.getJobForDefinition(seedJobDefinition);
    assertNotNull(seedJob);
  }

  @Test
  public void createDeletionJobsByIdsAndQuery() {
    // given
    rule.getProcessEngineConfiguration().setBatchJobsPerSeed(5);

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery().decisionDefinitionKey(DECISION);

    Batch batch = historyService.deleteHistoricDecisionInstancesAsync(decisionInstanceIds, query, null);

    JobDefinition seedJobDefinition = helper.getSeedJobDefinition(batch);
    JobDefinition deletionJobDefinition = helper.getExecutionJobDefinition(batch);;

    // when
    helper.executeSeedJob(batch);

    // then
    List<Job> deletionJobs = helper.getJobsForDefinition(deletionJobDefinition);
    assertEquals(5, deletionJobs.size());

    for (Job deletionJob : deletionJobs) {
      assertEquals(deletionJobDefinition.getId(), deletionJob.getJobDefinitionId());
      assertEquals(currentTime, deletionJob.getDuedate());
      assertNull(deletionJob.getProcessDefinitionId());
      assertNull(deletionJob.getProcessDefinitionKey());
      assertNull(deletionJob.getProcessInstanceId());
      assertNull(deletionJob.getExecutionId());
    }

    // and the seed job still exists
    Job seedJob = helper.getJobForDefinition(seedJobDefinition);
    assertNotNull(seedJob);
  }

  @Test
  public void createMonitorJobByIds() {
    // given
    Batch batch = historyService.deleteHistoricDecisionInstancesAsync(decisionInstanceIds, null);

    // when
    helper.completeSeedJobs(batch);

    // then the seed job definition still exists but the seed job is removed
    JobDefinition seedJobDefinition = helper.getSeedJobDefinition(batch);
    assertNotNull(seedJobDefinition);

    Job seedJob = helper.getSeedJob(batch);
    assertNull(seedJob);

    // and a monitor job definition and job exists
    JobDefinition monitorJobDefinition = helper.getMonitorJobDefinition(batch);
    assertNotNull(monitorJobDefinition);

    Job monitorJob = helper.getMonitorJob(batch);
    assertNotNull(monitorJob);
  }

  @Test
  public void createMonitorJobByQuery() {
    // given
    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery().decisionDefinitionKey(DECISION);
    Batch batch = historyService.deleteHistoricDecisionInstancesAsync(query, null);

    // when
    helper.completeSeedJobs(batch);

    // then the seed job definition still exists but the seed job is removed
    JobDefinition seedJobDefinition = helper.getSeedJobDefinition(batch);
    assertNotNull(seedJobDefinition);

    Job seedJob = helper.getSeedJob(batch);
    assertNull(seedJob);

    // and a monitor job definition and job exists
    JobDefinition monitorJobDefinition = helper.getMonitorJobDefinition(batch);
    assertNotNull(monitorJobDefinition);

    Job monitorJob = helper.getMonitorJob(batch);
    assertNotNull(monitorJob);
  }

  @Test
  public void createMonitorJobByIdsAndQuery() {
    // given
    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery().decisionDefinitionKey(DECISION);
    Batch batch = historyService.deleteHistoricDecisionInstancesAsync(decisionInstanceIds, query, null);

    // when
    helper.completeSeedJobs(batch);

    // then the seed job definition still exists but the seed job is removed
    JobDefinition seedJobDefinition = helper.getSeedJobDefinition(batch);
    assertNotNull(seedJobDefinition);

    Job seedJob = helper.getSeedJob(batch);
    assertNull(seedJob);

    // and a monitor job definition and job exists
    JobDefinition monitorJobDefinition = helper.getMonitorJobDefinition(batch);
    assertNotNull(monitorJobDefinition);

    Job monitorJob = helper.getMonitorJob(batch);
    assertNotNull(monitorJob);
  }

  @Test
  public void deleteInstancesByIds() {
    // given
    Batch batch = historyService.deleteHistoricDecisionInstancesAsync(decisionInstanceIds, null);

    helper.completeSeedJobs(batch);
    List<Job> deletionJobs = helper.getExecutionJobs(batch);

    // when
    for (Job deletionJob : deletionJobs) {
      helper.executeJob(deletionJob);
    }

    // then
    assertEquals(0, historyService.createHistoricDecisionInstanceQuery().count());
    assertEquals(0, historyService.createHistoricDecisionInstanceQuery().count());
  }

  @Test
  public void deleteInstancesByQuery() {
    // given
    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery().decisionDefinitionKey(DECISION);
    Batch batch = historyService.deleteHistoricDecisionInstancesAsync(query, null);

    helper.completeSeedJobs(batch);
    List<Job> deletionJobs = helper.getExecutionJobs(batch);

    // when
    for (Job deletionJob : deletionJobs) {
      helper.executeJob(deletionJob);
    }

    // then
    assertEquals(0, historyService.createHistoricDecisionInstanceQuery().count());
  }

  @Test
  public void deleteInstancesByIdsAndQuery() {
    // given
    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery().decisionDefinitionKey(DECISION);
    Batch batch = historyService.deleteHistoricDecisionInstancesAsync(decisionInstanceIds, query, null);

    helper.completeSeedJobs(batch);
    List<Job> deletionJobs = helper.getExecutionJobs(batch);

    // when
    for (Job deletionJob : deletionJobs) {
      helper.executeJob(deletionJob);
    }

    // then
    assertEquals(0, historyService.createHistoricDecisionInstanceQuery().count());
  }

  @Test
  public void shouldSetInvocationsPerBatchType() {
    // given
    configuration.getInvocationsPerBatchJobByBatchType()
        .put(Batch.TYPE_HISTORIC_DECISION_INSTANCE_DELETION, 42);

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery()
        .decisionDefinitionKey(DECISION);

    // when
    Batch batch = historyService.deleteHistoricDecisionInstancesAsync(query, null);

    // then
    Assertions.assertThat(batch.getInvocationsPerBatchJob()).isEqualTo(42);

    // clear
    configuration.setInvocationsPerBatchJobByBatchType(new HashMap<>());
  }

  @Test
  public void shouldSetExecutionStartTimeInBatchAndHistory() {
    // given
    ClockUtil.setCurrentTime(TEST_DATE);
    Batch batch = historyService.deleteHistoricDecisionInstancesAsync(decisionInstanceIds, null);
    helper.executeSeedJob(batch);
    List<Job> executionJobs = helper.getExecutionJobs(batch);

    // when
    helper.executeJob(executionJobs.get(0));

    // then
    HistoricBatch historicBatch = historyService.createHistoricBatchQuery().singleResult();
    batch = rule.getManagementService().createBatchQuery().singleResult();

    Assertions.assertThat(batch.getExecutionStartTime()).isEqualToIgnoringMillis(TEST_DATE);
    Assertions.assertThat(historicBatch.getExecutionStartTime()).isEqualToIgnoringMillis(TEST_DATE);
  }

  protected void assertBatchCreated(Batch batch, int decisionInstanceCount) {
    assertNotNull(batch);
    assertNotNull(batch.getId());
    assertEquals(Batch.TYPE_HISTORIC_DECISION_INSTANCE_DELETION, batch.getType());
    assertEquals(decisionInstanceCount, batch.getTotalJobs());
    assertEquals(defaultBatchJobsPerSeed, batch.getBatchJobsPerSeed());
    assertEquals(defaultInvocationsPerBatchJob, batch.getInvocationsPerBatchJob());
  }

  protected long getJobCountByDeployment(List<Job> jobs, String deploymentId) {
    return jobs.stream().filter(j -> Objects.equals(deploymentId, j.getDeploymentId())).count();
  }

  class BatchDeletionHelper extends BatchHelper {

    public BatchDeletionHelper(ProcessEngineRule engineRule) {
      super(engineRule);
    }

    public JobDefinition getExecutionJobDefinition(Batch batch) {
      return engineRule.getManagementService().createJobDefinitionQuery()
          .jobDefinitionId(batch.getBatchJobDefinitionId())
          .jobType(Batch.TYPE_HISTORIC_DECISION_INSTANCE_DELETION)
          .singleResult();
    }
  }

}
