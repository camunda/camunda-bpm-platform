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
package org.camunda.bpm.engine.test.api.history;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.DecisionService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceQuery;
import org.camunda.bpm.engine.impl.batch.BatchSeedJobHandler;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.runtime.BatchHelper;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class BatchHistoricDecisionInstanceDeletionTest {

  protected static String DECISION = "decision";

  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(rule);
  protected BatchDeletionHelper helper = new BatchDeletionHelper(rule);

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testRule);

  private int defaultBatchJobsPerSeed;
  private int defaultInvocationsPerBatchJob;

  protected DecisionService decisionService;
  protected HistoryService historyService;

  protected List<String> decisionInstanceIds;

  @Before
  public void setup() {
    historyService = rule.getHistoryService();
    decisionService = rule.getDecisionService();
    decisionInstanceIds = new ArrayList<String>();
  }

  @Before
  public void storeEngineSettings() {
    ProcessEngineConfigurationImpl configuration = rule.getProcessEngineConfiguration();
    defaultBatchJobsPerSeed = configuration.getBatchJobsPerSeed();
    defaultInvocationsPerBatchJob = configuration.getInvocationsPerBatchJob();
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
    ProcessEngineConfigurationImpl configuration = rule.getProcessEngineConfiguration();
    configuration.setBatchJobsPerSeed(defaultBatchJobsPerSeed);
    configuration.setInvocationsPerBatchJob(defaultInvocationsPerBatchJob);
  }

  @After
  public void removeBatches() {
    helper.removeAllRunningAndHistoricBatches();
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
    // then
    thrown.expect(BadUserRequestException.class);

    // when
    historyService.deleteHistoricDecisionInstancesAsync((List<String>) null, null);
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
    // then
    thrown.expect(BadUserRequestException.class);

    // when
    historyService.deleteHistoricDecisionInstancesAsync((HistoricDecisionInstanceQuery) null, null);
  }

  @Test
  public void createBatchDeletionByInvalidQueryByKey() {
    // given
    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery().decisionDefinitionKey("foo");

    // then
    thrown.expect(BadUserRequestException.class);

    // when
    historyService.deleteHistoricDecisionInstancesAsync(query, null);
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
    assertNull(seedJob.getDuedate());
    assertNull(seedJob.getDeploymentId());
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
    assertNull(seedJob.getDuedate());
    assertNull(seedJob.getDeploymentId());
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
    assertNull(seedJob.getDuedate());
    assertNull(seedJob.getDeploymentId());
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
      assertNull(deletionJob.getDuedate());
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
      assertNull(deletionJob.getDuedate());
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
      assertNull(deletionJob.getDuedate());
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
    helper.executeSeedJob(batch);

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
    helper.executeSeedJob(batch);

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
    helper.executeSeedJob(batch);

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

    helper.executeSeedJob(batch);
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

    helper.executeSeedJob(batch);
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

    helper.executeSeedJob(batch);
    List<Job> deletionJobs = helper.getExecutionJobs(batch);

    // when
    for (Job deletionJob : deletionJobs) {
      helper.executeJob(deletionJob);
    }

    // then
    assertEquals(0, historyService.createHistoricDecisionInstanceQuery().count());
  }

  protected void assertBatchCreated(Batch batch, int decisionInstanceCount) {
    assertNotNull(batch);
    assertNotNull(batch.getId());
    assertEquals(Batch.TYPE_HISTORIC_DECISION_INSTANCE_DELETION, batch.getType());
    assertEquals(decisionInstanceCount, batch.getTotalJobs());
    assertEquals(defaultBatchJobsPerSeed, batch.getBatchJobsPerSeed());
    assertEquals(defaultInvocationsPerBatchJob, batch.getInvocationsPerBatchJob());
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
