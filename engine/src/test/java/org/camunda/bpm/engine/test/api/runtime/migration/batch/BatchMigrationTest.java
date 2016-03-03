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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.impl.batch.BatchSeedJobHandler;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.migration.batch.MigrationBatchJobHandler;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.MigrationTestRule;
import org.camunda.bpm.engine.test.api.runtime.migration.ProcessModels;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class BatchMigrationTest {

  protected ProcessEngineRule engineRule = new ProcessEngineRule(true);
  protected MigrationTestRule migrationRule = new MigrationTestRule(engineRule);
  protected BatchMigrationHelper helper = new BatchMigrationHelper(engineRule, migrationRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(migrationRule);

  protected ProcessEngineConfigurationImpl configuration;
  protected RuntimeService runtimeService;
  protected ManagementService managementService;
  protected HistoryService historyService;

  @Before
  public void initServices() {
    runtimeService = engineRule.getRuntimeService();
    managementService = engineRule.getManagementService();
    historyService = engineRule.getHistoryService();
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

  @After
  public void resetClock() {
    ClockUtil.reset();
  }

  @Test
  public void testNullMigrationPlan() {
    try {
      runtimeService.executeMigrationPlan(null).processInstanceIds(Collections.singletonList("process")).executeAsync();
      fail("Should not succeed");
    }
    catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("migration plan is null"));
    }
  }

  @Test
  public void testNullProcessInstanceIds() {
    ProcessDefinition testProcessDefinition = migrationRule.deploy(ProcessModels.ONE_TASK_PROCESS);
    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(testProcessDefinition.getId(), testProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    try {
      runtimeService.executeMigrationPlan(migrationPlan).processInstanceIds(null).executeAsync();
      fail("Should not succeed");
    }
    catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("process instance ids is null"));
    }
  }

  @Test
  public void testEmptyProcessInstanceIds() {
    ProcessDefinition testProcessDefinition = migrationRule.deploy(ProcessModels.ONE_TASK_PROCESS);
    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(testProcessDefinition.getId(), testProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    try {
      runtimeService.executeMigrationPlan(migrationPlan).processInstanceIds(Collections.<String>emptyList()).executeAsync();
      fail("Should not succeed");
    }
    catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("process instance ids is empty"));
    }
  }

  @Test
  public void testBatchCreation() {
    // when
    Batch batch = helper.migrateProcessInstancesAsync(15);

    // then a batch is created
    assertNotNull(batch);
    assertNotNull(batch.getId());
    assertEquals("instance-migration", batch.getType());
    assertEquals(15, batch.getSize());
    assertEquals(10, batch.getBatchJobsPerSeed());
    assertEquals(1, batch.getInvocationsPerBatchJob());
  }

  @Test
  public void testSeedJobCreation() {
    // when
    Batch batch = helper.migrateProcessInstancesAsync(10);

    // then there exists a seed job definition with the batch id as configuration
    JobDefinition seedJobDefinition = helper.getSeedJobDefinition(batch);
    assertNotNull(seedJobDefinition);
    assertEquals(batch.getId(), seedJobDefinition.getJobConfiguration());
    assertEquals(BatchSeedJobHandler.TYPE, seedJobDefinition.getJobType());

    // and there exists a migration job definition
    JobDefinition migrationJobDefinition = helper.getMigrationJobDefinition(batch);
    assertNotNull(migrationJobDefinition);
    assertEquals(MigrationBatchJobHandler.TYPE, migrationJobDefinition.getJobType());

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

    // but no migration jobs where created
    List<Job> migrationJobs = helper.getMigrationJobs(batch);
    assertEquals(0, migrationJobs.size());
  }

  @Test
  public void testMigrationJobsCreation() {
    Batch batch = helper.migrateProcessInstancesAsync(20);
    JobDefinition seedJobDefinition = helper.getSeedJobDefinition(batch);
    JobDefinition migrationJobDefinition = helper.getMigrationJobDefinition(batch);

    // when
    helper.executeSeedJob(batch);

    // then there exist migration jobs
    List<Job> migrationJobs = helper.getJobsForDefinition(migrationJobDefinition);
    assertEquals(10, migrationJobs.size());

    for (Job migrationJob : migrationJobs) {
      assertEquals(migrationJobDefinition.getId(), migrationJob.getJobDefinitionId());
      assertNull(migrationJob.getDuedate());
      assertNull(migrationJob.getDeploymentId());
      assertNull(migrationJob.getProcessDefinitionId());
      assertNull(migrationJob.getProcessDefinitionKey());
      assertNull(migrationJob.getProcessInstanceId());
      assertNull(migrationJob.getExecutionId());
    }

    // and the seed job still exists
    Job seedJob = helper.getJobForDefinition(seedJobDefinition);
    assertNotNull(seedJob);
  }

  @Test
  public void testMonitorJobCreation() {
    Batch batch = helper.migrateProcessInstancesAsync(10);

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
  public void testMigrationJobsExecution() {
    Batch batch = helper.migrateProcessInstancesAsync(10);
    helper.executeSeedJob(batch);
    List<Job> migrationJobs = helper.getMigrationJobs(batch);

    // when
    for (Job migrationJob : migrationJobs) {
      helper.executeJob(migrationJob);
    }

    // then all process instances where migrated
    assertEquals(0, helper.countSourceProcessInstances());
    assertEquals(10, helper.countTargetProcessInstances());

    // and the no migration jobs exist
    assertEquals(0, helper.getMigrationJobs(batch).size());

    // but a monitor job exists
    assertNotNull(helper.getMonitorJob(batch));
  }

  @Test
  public void testNumberOfJobsCreatedBySeedJobPerInvocation() {
    int batchJobsPerSeed = ((ProcessEngineConfigurationImpl) engineRule.getProcessEngine().getProcessEngineConfiguration()).getBatchJobsPerSeed();
    Batch batch = helper.migrateProcessInstancesAsync(batchJobsPerSeed * 2 + 4);

    // when
    helper.executeSeedJob(batch);

    // then the default number of jobs was created
    assertEquals(batch.getBatchJobsPerSeed(), helper.getMigrationJobs(batch).size());

    // when the seed job is executed a second time
    helper.executeSeedJob(batch);

    // then the same amount of jobs was created
    assertEquals(2 * batch.getBatchJobsPerSeed(), helper.getMigrationJobs(batch).size());

    // when the seed job is executed a third time
    helper.executeSeedJob(batch);

    // then the all jobs where created
    assertEquals(2 * batch.getBatchJobsPerSeed() + 4, helper.getMigrationJobs(batch).size());

    // and the seed job is removed
    assertNull(helper.getSeedJob(batch));
  }

  @Test
  public void testCustomNumberOfJobsCreateBySeedJob() {
    ProcessEngineConfigurationImpl configuration = (ProcessEngineConfigurationImpl) engineRule.getProcessEngine().getProcessEngineConfiguration();
    int defaultBatchJobsPerSeed = configuration.getBatchJobsPerSeed();
    int defaultInvocationsPerBatchJob = configuration.getInvocationsPerBatchJob();
    configuration.setBatchJobsPerSeed(2);
    configuration.setInvocationsPerBatchJob(5);

    // when
    Batch batch = helper.migrateProcessInstancesAsync(20);

    // then the configuration was saved in the batch job
    assertEquals(2, batch.getBatchJobsPerSeed());
    assertEquals(5, batch.getInvocationsPerBatchJob());

    // when the seed job is executed
    helper.executeSeedJob(batch);

    // then there exist the first batch of migration jobs
    assertEquals(2, helper.getMigrationJobs(batch).size());

    // when the seed job is executed a second time
    helper.executeSeedJob(batch);

    // then the full batch of migration jobs exist
    assertEquals(4, helper.getMigrationJobs(batch).size());

    // and the seed job is removed
    assertNull(helper.getSeedJob(batch));

    // reset configuration
    configuration.setBatchJobsPerSeed(defaultBatchJobsPerSeed);
    configuration.setInvocationsPerBatchJob(defaultInvocationsPerBatchJob);
  }

  @Test
  public void testSeedJobPollingForCompletion() {
    Batch batch = helper.migrateProcessInstancesAsync(10);

    // when
    Date createDate = new Date(1457326800000L);
    ClockUtil.setCurrentTime(createDate);
    helper.executeSeedJob(batch);

    // then the monitor job has a due date of the default batch poll time
    Job monitorJob = helper.getMonitorJob(batch);
    Date dueDate = helper.addSeconds(createDate, 30);
    assertEquals(dueDate, monitorJob.getDuedate());
  }

  @Test
  public void testMonitorJobRemovesBatchAfterCompletion() {
    Batch batch = helper.migrateProcessInstancesAsync(10);
    helper.executeSeedJob(batch);
    helper.executeMigrationJobs(batch);

    // when
    helper.executeMonitorJob(batch);

    // then the batch was completed and removed
    assertEquals(0, managementService.createBatchQuery().count());

    // and the seed jobs was removed
    assertEquals(0, managementService.createJobQuery().count());
  }

  @Test
  public void testBatchDeletion() {
    Batch batch = helper.migrateProcessInstancesAsync(10);
    helper.executeSeedJob(batch);

    // when
    managementService.deleteBatch(batch.getId(), true);

    // then the batch was deleted
    assertEquals(0, managementService.createBatchQuery().count());

    // and the seed and migration job definition were deleted
    assertEquals(0, managementService.createJobDefinitionQuery().count());

    // and the seed job and migration jobs were deleted
    assertEquals(0, managementService.createJobQuery().count());
  }

  @Test
  public void testBatchExecutionFailureWithMissingProcessInstance() {
    Batch batch = helper.migrateProcessInstancesAsync(2);
    helper.executeSeedJob(batch);

    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().list();
    String deletedProcessInstanceId = processInstances.get(0).getId();

    // when
    runtimeService.deleteProcessInstance(deletedProcessInstanceId, "test");
    helper.executeMigrationJobs(batch);

    // then the remaining process instance was migrated
    assertEquals(0, helper.countSourceProcessInstances());
    assertEquals(1, helper.countTargetProcessInstances());

    // and one batch job failed and has 2 retries left
    List<Job> migrationJobs = helper.getMigrationJobs(batch);
    assertEquals(1, migrationJobs.size());

    Job failedJob = migrationJobs.get(0);
    assertEquals(2, failedJob.getRetries());
    assertThat(failedJob.getExceptionMessage(), startsWith("ENGINE-23003"));
    assertThat(failedJob.getExceptionMessage(), containsString("Process instance '" + deletedProcessInstanceId + "' cannot be migrated"));
  }

}
