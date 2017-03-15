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

import static org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.batch.BatchSeedJobHandler;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.MigrationTestRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.bpmn.multiinstance.DelegateEvent;
import org.camunda.bpm.engine.test.bpmn.multiinstance.DelegateExecutionListener;
import org.camunda.bpm.engine.test.util.ClockTestUtil;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class BatchMigrationTest {

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected MigrationTestRule migrationRule = new MigrationTestRule(engineRule);
  protected BatchMigrationHelper helper = new BatchMigrationHelper(engineRule, migrationRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(migrationRule).around(testRule);

  protected ProcessEngineConfigurationImpl configuration;
  protected RuntimeService runtimeService;
  protected ManagementService managementService;
  protected HistoryService historyService;

  protected int defaultBatchJobsPerSeed;
  protected int defaultInvocationsPerBatchJob;

  @Before
  public void initServices() {
    runtimeService = engineRule.getRuntimeService();
    managementService = engineRule.getManagementService();
    historyService = engineRule.getHistoryService();
  }

  @After
  public void removeBatches() {
    helper.removeAllRunningAndHistoricBatches();
  }

  @After
  public void resetClock() {
    ClockUtil.reset();
  }

  @Before
  public void storeEngineSettings() {
    ProcessEngineConfigurationImpl configuration = engineRule.getProcessEngineConfiguration();
    defaultBatchJobsPerSeed = configuration.getBatchJobsPerSeed();
    defaultInvocationsPerBatchJob = configuration.getInvocationsPerBatchJob();
  }

  @After
  public void restoreEngineSettings() {
    ProcessEngineConfigurationImpl configuration = engineRule.getProcessEngineConfiguration();
    configuration.setBatchJobsPerSeed(defaultBatchJobsPerSeed);
    configuration.setInvocationsPerBatchJob(defaultInvocationsPerBatchJob);
  }


  @Test
  public void testNullMigrationPlan() {
    try {
      runtimeService.newMigration(null).processInstanceIds(Collections.singletonList("process")).executeAsync();
      fail("Should not succeed");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("migration plan is null"));
    }
  }

  @Test
  public void testNullProcessInstanceIdsList() {
    ProcessDefinition testProcessDefinition = migrationRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(testProcessDefinition.getId(), testProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    try {
      runtimeService.newMigration(migrationPlan).processInstanceIds((List<String>) null).executeAsync();
      fail("Should not succeed");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("process instance ids is empty"));
    }
  }

  @Test
  public void testProcessInstanceIdsListWithNullValue() {
    ProcessDefinition testProcessDefinition = migrationRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(testProcessDefinition.getId(), testProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    try {
      runtimeService.newMigration(migrationPlan).processInstanceIds(Arrays.asList("foo", null, "bar")).executeAsync();
      fail("Should not succeed");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("process instance ids contains null value"));
    }
  }

  @Test
  public void testEmptyProcessInstanceIdsList() {
    ProcessDefinition testProcessDefinition = migrationRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(testProcessDefinition.getId(), testProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    try {
      runtimeService.newMigration(migrationPlan).processInstanceIds(Collections.<String>emptyList()).executeAsync();
      fail("Should not succeed");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("process instance ids is empty"));
    }
  }

  @Test
  public void testNullProcessInstanceIdsArray() {
    ProcessDefinition testProcessDefinition = migrationRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(testProcessDefinition.getId(), testProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    try {
      runtimeService.newMigration(migrationPlan).processInstanceIds((String[]) null).executeAsync();
      fail("Should not be able to migrate");
    }
    catch (ProcessEngineException e) {
      assertThat(e.getMessage(), CoreMatchers.containsString("process instance ids is empty"));
    }
  }

  @Test
  public void testProcessInstanceIdsArrayWithNullValue() {
    ProcessDefinition testProcessDefinition = migrationRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(testProcessDefinition.getId(), testProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    try {
      runtimeService.newMigration(migrationPlan).processInstanceIds("foo", null, "bar").executeAsync();
      fail("Should not be able to migrate");
    }
    catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("process instance ids contains null value"));
    }
  }

  @Test
  public void testNullProcessInstanceQuery() {
    ProcessDefinition testProcessDefinition = migrationRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(testProcessDefinition.getId(), testProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    try {
      runtimeService.newMigration(migrationPlan).processInstanceQuery(null).executeAsync();
      fail("Should not succeed");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("process instance ids is empty"));
    }
  }

  @Test
  public void testEmptyProcessInstanceQuery() {
    ProcessDefinition testProcessDefinition = migrationRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(testProcessDefinition.getId(), testProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    ProcessInstanceQuery emptyProcessInstanceQuery = runtimeService.createProcessInstanceQuery();
    assertEquals(0, emptyProcessInstanceQuery.count());

    try {
      runtimeService.newMigration(migrationPlan).processInstanceQuery(emptyProcessInstanceQuery).executeAsync();
      fail("Should not succeed");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("process instance ids is empty"));
    }
  }

  @Test
  public void testBatchCreation() {
    // when
    Batch batch = helper.migrateProcessInstancesAsync(15);

    // then a batch is created
    assertBatchCreated(batch, 15);
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
    JobDefinition migrationJobDefinition = helper.getExecutionJobDefinition(batch);
    assertNotNull(migrationJobDefinition);
    assertEquals(Batch.TYPE_PROCESS_INSTANCE_MIGRATION, migrationJobDefinition.getJobType());

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
    List<Job> migrationJobs = helper.getExecutionJobs(batch);
    assertEquals(0, migrationJobs.size());
  }

  @Test
  public void testMigrationJobsCreation() {
    // reduce number of batch jobs per seed to not have to create a lot of instances
    engineRule.getProcessEngineConfiguration().setBatchJobsPerSeed(10);

    Batch batch = helper.migrateProcessInstancesAsync(20);
    JobDefinition seedJobDefinition = helper.getSeedJobDefinition(batch);
    JobDefinition migrationJobDefinition = helper.getExecutionJobDefinition(batch);
    String sourceDeploymentId = helper.getSourceProcessDefinition().getDeploymentId();

    // when
    helper.executeSeedJob(batch);

    // then there exist migration jobs
    List<Job> migrationJobs = helper.getJobsForDefinition(migrationJobDefinition);
    assertEquals(10, migrationJobs.size());

    for (Job migrationJob : migrationJobs) {
      assertEquals(migrationJobDefinition.getId(), migrationJob.getJobDefinitionId());
      assertNull(migrationJob.getDuedate());
      assertEquals(sourceDeploymentId, migrationJob.getDeploymentId());
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
    List<Job> migrationJobs = helper.getExecutionJobs(batch);

    // when
    for (Job migrationJob : migrationJobs) {
      helper.executeJob(migrationJob);
    }

    // then all process instances where migrated
    assertEquals(0, helper.countSourceProcessInstances());
    assertEquals(10, helper.countTargetProcessInstances());

    // and the no migration jobs exist
    assertEquals(0, helper.getExecutionJobs(batch).size());

    // but a monitor job exists
    assertNotNull(helper.getMonitorJob(batch));
  }

  @Test
  public void testMigrationJobsExecutionByJobExecutorWithAuthorizationEnabledAndTenant() {
    ProcessEngineConfigurationImpl processEngineConfiguration = engineRule.getProcessEngineConfiguration();

    processEngineConfiguration.setAuthorizationEnabled(true);

    try {
      Batch batch = helper.migrateProcessInstancesAsyncForTenant(10, "someTenantId");
      helper.executeSeedJob(batch);

      testRule.waitForJobExecutorToProcessAllJobs();

      // then all process instances where migrated
      assertEquals(0, helper.countSourceProcessInstances());
      assertEquals(10, helper.countTargetProcessInstances());

    } finally {
      processEngineConfiguration.setAuthorizationEnabled(false);
    }

  }

  @Test
  public void testNumberOfJobsCreatedBySeedJobPerInvocation() {
    // reduce number of batch jobs per seed to not have to create a lot of instances
    int batchJobsPerSeed = 10;
    engineRule.getProcessEngineConfiguration().setBatchJobsPerSeed(10);

    Batch batch = helper.migrateProcessInstancesAsync(batchJobsPerSeed * 2 + 4);

    // when
    helper.executeSeedJob(batch);

    // then the default number of jobs was created
    assertEquals(batch.getBatchJobsPerSeed(), helper.getExecutionJobs(batch).size());

    // when the seed job is executed a second time
    helper.executeSeedJob(batch);

    // then the same amount of jobs was created
    assertEquals(2 * batch.getBatchJobsPerSeed(), helper.getExecutionJobs(batch).size());

    // when the seed job is executed a third time
    helper.executeSeedJob(batch);

    // then the all jobs where created
    assertEquals(2 * batch.getBatchJobsPerSeed() + 4, helper.getExecutionJobs(batch).size());

    // and the seed job is removed
    assertNull(helper.getSeedJob(batch));
  }

  @Test
  public void testDefaultBatchConfiguration() {
    ProcessEngineConfigurationImpl configuration = engineRule.getProcessEngineConfiguration();
    assertEquals(100, configuration.getBatchJobsPerSeed());
    assertEquals(1, configuration.getInvocationsPerBatchJob());
    assertEquals(30, configuration.getBatchPollTime());
  }

  @Test
  public void testCustomNumberOfJobsCreateBySeedJob() {
    ProcessEngineConfigurationImpl configuration = engineRule.getProcessEngineConfiguration();
    configuration.setBatchJobsPerSeed(2);
    configuration.setInvocationsPerBatchJob(5);

    // when
    Batch batch = helper.migrateProcessInstancesAsync(20);

    // then the configuration was saved in the batch job
    assertEquals(2, batch.getBatchJobsPerSeed());
    assertEquals(5, batch.getInvocationsPerBatchJob());

    // and the size was correctly calculated
    assertEquals(4, batch.getTotalJobs());

    // when the seed job is executed
    helper.executeSeedJob(batch);

    // then there exist the first batch of migration jobs
    assertEquals(2, helper.getExecutionJobs(batch).size());

    // when the seed job is executed a second time
    helper.executeSeedJob(batch);

    // then the full batch of migration jobs exist
    assertEquals(4, helper.getExecutionJobs(batch).size());

    // and the seed job is removed
    assertNull(helper.getSeedJob(batch));
  }

  @Test
  public void testMonitorJobPollingForCompletion() {
    Batch batch = helper.migrateProcessInstancesAsync(10);

    // when the seed job creates the monitor job
    Date createDate = ClockTestUtil.setClockToDateWithoutMilliseconds();
    helper.executeSeedJob(batch);

    // then the monitor job has a no due date set
    Job monitorJob = helper.getMonitorJob(batch);
    assertNotNull(monitorJob);
    assertNull(monitorJob.getDuedate());

    // when the monitor job is executed
    helper.executeMonitorJob(batch);

    // then the monitor job has a due date of the default batch poll time
    monitorJob = helper.getMonitorJob(batch);
    Date dueDate = helper.addSeconds(createDate, 30);
    assertEquals(dueDate, monitorJob.getDuedate());
  }

  @Test
  public void testMonitorJobRemovesBatchAfterCompletion() {
    Batch batch = helper.migrateProcessInstancesAsync(10);
    helper.executeSeedJob(batch);
    helper.executeJobs(batch);

    // when
    helper.executeMonitorJob(batch);

    // then the batch was completed and removed
    assertEquals(0, managementService.createBatchQuery().count());

    // and the seed jobs was removed
    assertEquals(0, managementService.createJobQuery().count());
  }

  @Test
  public void testBatchDeletionWithCascade() {
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
  public void testBatchDeletionWithoutCascade() {
    Batch batch = helper.migrateProcessInstancesAsync(10);
    helper.executeSeedJob(batch);

    // when
    managementService.deleteBatch(batch.getId(), false);

    // then the batch was deleted
    assertEquals(0, managementService.createBatchQuery().count());

    // and the seed and migration job definition were deleted
    assertEquals(0, managementService.createJobDefinitionQuery().count());

    // and the seed job and migration jobs were deleted
    assertEquals(0, managementService.createJobQuery().count());
  }

  @Test
  public void testBatchWithFailedSeedJobDeletionWithCascade() {
    Batch batch = helper.migrateProcessInstancesAsync(2);

    // create incident
    Job seedJob = helper.getSeedJob(batch);
    managementService.setJobRetries(seedJob.getId(), 0);

    // when
    managementService.deleteBatch(batch.getId(), true);

    // then the no historic incidents exists
    long historicIncidents = historyService.createHistoricIncidentQuery().count();
    assertEquals(0, historicIncidents);
  }

  @Test
  public void testBatchWithFailedMigrationJobDeletionWithCascade() {
    Batch batch = helper.migrateProcessInstancesAsync(2);
    helper.executeSeedJob(batch);

    // create incidents
    List<Job> migrationJobs = helper.getExecutionJobs(batch);
    for (Job migrationJob : migrationJobs) {
      managementService.setJobRetries(migrationJob.getId(), 0);
    }

    // when
    managementService.deleteBatch(batch.getId(), true);

    // then the no historic incidents exists
    long historicIncidents = historyService.createHistoricIncidentQuery().count();
    assertEquals(0, historicIncidents);
  }

  @Test
  public void testBatchWithFailedMonitorJobDeletionWithCascade() {
    Batch batch = helper.migrateProcessInstancesAsync(2);
    helper.executeSeedJob(batch);

    // create incident
    Job monitorJob = helper.getMonitorJob(batch);
    managementService.setJobRetries(monitorJob.getId(), 0);

    // when
    managementService.deleteBatch(batch.getId(), true);

    // then the no historic incidents exists
    long historicIncidents = historyService.createHistoricIncidentQuery().count();
    assertEquals(0, historicIncidents);
  }

  @Test
  public void testBatchExecutionFailureWithMissingProcessInstance() {
    Batch batch = helper.migrateProcessInstancesAsync(2);
    helper.executeSeedJob(batch);

    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().list();
    String deletedProcessInstanceId = processInstances.get(0).getId();

    // when
    runtimeService.deleteProcessInstance(deletedProcessInstanceId, "test");
    helper.executeJobs(batch);

    // then the remaining process instance was migrated
    assertEquals(0, helper.countSourceProcessInstances());
    assertEquals(1, helper.countTargetProcessInstances());

    // and one batch job failed and has 2 retries left
    List<Job> migrationJobs = helper.getExecutionJobs(batch);
    assertEquals(1, migrationJobs.size());

    Job failedJob = migrationJobs.get(0);
    assertEquals(2, failedJob.getRetries());
    assertThat(failedJob.getExceptionMessage(), startsWith("ENGINE-23003"));
    assertThat(failedJob.getExceptionMessage(), containsString("Process instance '" + deletedProcessInstanceId + "' cannot be migrated"));
  }

  @Test
  public void testBatchCreationWithProcessInstanceQuery() {
    RuntimeService runtimeService = engineRule.getRuntimeService();
    int processInstanceCount = 15;

    ProcessDefinition sourceProcessDefinition = migrationRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = migrationRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    for (int i = 0; i < processInstanceCount; i++) {
      runtimeService.startProcessInstanceById(sourceProcessDefinition.getId());
    }

    MigrationPlan migrationPlan = engineRule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    ProcessInstanceQuery sourceProcessInstanceQuery = runtimeService.createProcessInstanceQuery().processDefinitionId(sourceProcessDefinition.getId());
    assertEquals(processInstanceCount, sourceProcessInstanceQuery.count());

    // when
    Batch batch = runtimeService.newMigration(migrationPlan)
      .processInstanceQuery(sourceProcessInstanceQuery)
      .executeAsync();

    // then a batch is created
    assertBatchCreated(batch, processInstanceCount);
  }

  @Test
  public void testBatchCreationWithOverlappingProcessInstanceIdsAndQuery() {
    RuntimeService runtimeService = engineRule.getRuntimeService();
    int processInstanceCount = 15;

    ProcessDefinition sourceProcessDefinition = migrationRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = migrationRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    List<String> processInstanceIds = new ArrayList<String>();
    for (int i = 0; i < processInstanceCount; i++) {
      processInstanceIds.add(
        runtimeService.startProcessInstanceById(sourceProcessDefinition.getId()).getId()
      );
    }

    MigrationPlan migrationPlan = engineRule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    ProcessInstanceQuery sourceProcessInstanceQuery = runtimeService.createProcessInstanceQuery().processDefinitionId(sourceProcessDefinition.getId());
    assertEquals(processInstanceCount, sourceProcessInstanceQuery.count());

    // when
    Batch batch = runtimeService.newMigration(migrationPlan)
      .processInstanceIds(processInstanceIds)
      .processInstanceQuery(sourceProcessInstanceQuery)
      .executeAsync();

    // then a batch is created
    assertBatchCreated(batch, processInstanceCount);
  }

  @Test
  public void testListenerInvocationForNewlyCreatedScope() {
    // given
    DelegateEvent.clearEvents();

    ProcessDefinition sourceProcessDefinition = migrationRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = migrationRule.deployAndGetDefinition(modify(ProcessModels.SUBPROCESS_PROCESS)
      .activityBuilder("subProcess")
      .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_START, DelegateExecutionListener.class.getName())
      .done()
    );

    MigrationPlan migrationPlan = engineRule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());

    Batch batch = engineRule.getRuntimeService().newMigration(migrationPlan)
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .executeAsync();
    helper.executeSeedJob(batch);

    // when
    helper.executeJobs(batch);

    // then
    List<DelegateEvent> recordedEvents = DelegateEvent.getEvents();
    assertEquals(1, recordedEvents.size());

    DelegateEvent event = recordedEvents.get(0);
    assertEquals(targetProcessDefinition.getId(), event.getProcessDefinitionId());
    assertEquals("subProcess", event.getCurrentActivityId());

    DelegateEvent.clearEvents();
  }

  @Test
  public void testSkipListenerInvocationForNewlyCreatedScope() {
    // given
    DelegateEvent.clearEvents();

    ProcessDefinition sourceProcessDefinition = migrationRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = migrationRule.deployAndGetDefinition(modify(ProcessModels.SUBPROCESS_PROCESS)
      .activityBuilder("subProcess")
      .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_START, DelegateExecutionListener.class.getName())
      .done()
    );

    MigrationPlan migrationPlan = engineRule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());

    Batch batch = engineRule.getRuntimeService().newMigration(migrationPlan)
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .skipCustomListeners()
      .executeAsync();
    helper.executeSeedJob(batch);

    // when
    helper.executeJobs(batch);

    // then
    assertEquals(0, DelegateEvent.getEvents().size());
  }

  @Test
  public void testIoMappingInvocationForNewlyCreatedScope() {
    // given
    ProcessDefinition sourceProcessDefinition = migrationRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = migrationRule.deployAndGetDefinition(modify(ProcessModels.SUBPROCESS_PROCESS)
      .activityBuilder("subProcess")
      .camundaInputParameter("foo", "bar")
      .done()
    );

    MigrationPlan migrationPlan = engineRule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());

    Batch batch = engineRule.getRuntimeService().newMigration(migrationPlan)
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .executeAsync();
    helper.executeSeedJob(batch);

    // when
    helper.executeJobs(batch);

    // then
    VariableInstance inputVariable = engineRule.getRuntimeService().createVariableInstanceQuery().singleResult();
    Assert.assertNotNull(inputVariable);
    assertEquals("foo", inputVariable.getName());
    assertEquals("bar", inputVariable.getValue());

    ActivityInstance activityInstance = engineRule.getRuntimeService().getActivityInstance(processInstance.getId());
    assertEquals(activityInstance.getActivityInstances("subProcess")[0].getId(), inputVariable.getActivityInstanceId());
  }

  @Test
  public void testSkipIoMappingInvocationForNewlyCreatedScope() {
 // given
    ProcessDefinition sourceProcessDefinition = migrationRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = migrationRule.deployAndGetDefinition(modify(ProcessModels.SUBPROCESS_PROCESS)
      .activityBuilder("subProcess")
      .camundaInputParameter("foo", "bar")
      .done()
    );

    MigrationPlan migrationPlan = engineRule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());

    Batch batch = engineRule.getRuntimeService().newMigration(migrationPlan)
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .skipIoMappings()
      .executeAsync();
    helper.executeSeedJob(batch);

    // when
    helper.executeJobs(batch);

    // then
    assertEquals(0, engineRule.getRuntimeService().createVariableInstanceQuery().count());
  }

  @Test
  public void testUpdateEventTrigger() {
    // given
    String newMessageName = "newMessage";

    ProcessDefinition sourceProcessDefinition = migrationRule.deployAndGetDefinition(ProcessModels.ONE_RECEIVE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = migrationRule.deployAndGetDefinition(modify(ProcessModels.ONE_RECEIVE_TASK_PROCESS)
      .renameMessage("Message", newMessageName)
    );

    ProcessInstance processInstance = runtimeService.startProcessInstanceById(sourceProcessDefinition.getId());
    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .updateEventTriggers()
      .build();

    Batch batch = runtimeService.newMigration(migrationPlan)
      .processInstanceIds(Collections.singletonList(processInstance.getId()))
      .executeAsync();

    helper.executeSeedJob(batch);

    // when
    helper.executeJobs(batch);

    // then the message event subscription's event name was changed
    EventSubscription eventSubscription = runtimeService.createEventSubscriptionQuery().singleResult();
    assertEquals(newMessageName, eventSubscription.getEventName());
  }

  @Test
  public void testDeleteBatchJobManually() {
    // given
    Batch batch = helper.createMigrationBatchWithSize(1);
    helper.executeSeedJob(batch);

    JobEntity migrationJob = (JobEntity) helper.getExecutionJobs(batch).get(0);
    String byteArrayId = migrationJob.getJobHandlerConfigurationRaw();

    ByteArrayEntity byteArrayEntity = engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired()
      .execute(new GetByteArrayCommand(byteArrayId));
    assertNotNull(byteArrayEntity);

    // when
    managementService.deleteJob(migrationJob.getId());

    // then
    byteArrayEntity = engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired()
      .execute(new GetByteArrayCommand(byteArrayId));
    assertNull(byteArrayEntity);
  }

  @Test
  public void testMigrateWithVarargsArray() {
    ProcessDefinition sourceDefinition = migrationRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetDefinition = migrationRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
      .mapEqualActivities()
      .build();

    ProcessInstance processInstance1 = runtimeService.startProcessInstanceById(sourceDefinition.getId());
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceById(sourceDefinition.getId());

    // when
    Batch batch = runtimeService.newMigration(migrationPlan)
      .processInstanceIds(processInstance1.getId(), processInstance2.getId())
      .executeAsync();

    helper.executeSeedJob(batch);
    helper.executeJobs(batch);
    helper.executeMonitorJob(batch);

    // then
    Assert.assertEquals(2, runtimeService.createProcessInstanceQuery()
        .processDefinitionId(targetDefinition.getId()).count());
  }

  protected void assertBatchCreated(Batch batch, int processInstanceCount) {
    assertNotNull(batch);
    assertNotNull(batch.getId());
    assertEquals("instance-migration", batch.getType());
    assertEquals(processInstanceCount, batch.getTotalJobs());
    assertEquals(defaultBatchJobsPerSeed, batch.getBatchJobsPerSeed());
    assertEquals(defaultInvocationsPerBatchJob, batch.getInvocationsPerBatchJob());
  }

  public class GetByteArrayCommand implements Command<ByteArrayEntity> {

    protected String byteArrayId;

    public GetByteArrayCommand(String byteArrayId) {
      this.byteArrayId = byteArrayId;
    }

    public ByteArrayEntity execute(CommandContext commandContext) {
      return (ByteArrayEntity) commandContext.getDbEntityManager()
        .selectOne("selectByteArray", byteArrayId);
    }

  }

}
