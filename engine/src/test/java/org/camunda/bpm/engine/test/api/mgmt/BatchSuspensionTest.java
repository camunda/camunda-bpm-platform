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
package org.camunda.bpm.engine.test.api.mgmt;

import static org.camunda.bpm.engine.EntityTypes.BATCH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.history.UserOperationLogQuery;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.AbstractSetBatchStateCmd;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.runtime.migration.MigrationTestRule;
import org.camunda.bpm.engine.test.api.runtime.migration.batch.BatchMigrationHelper;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class BatchSuspensionTest {

  public static final String USER_ID = "userId";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected MigrationTestRule migrationRule = new MigrationTestRule(engineRule);
  protected BatchMigrationHelper helper = new BatchMigrationHelper(engineRule, migrationRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(migrationRule);

  protected RuntimeService runtimeService;
  protected ManagementService managementService;
  protected HistoryService historyService;
  protected IdentityService identityService;

  protected int defaultBatchJobsPerSeed;

  @Before
  public void initServices() {
    runtimeService = engineRule.getRuntimeService();
    managementService = engineRule.getManagementService();
    historyService = engineRule.getHistoryService();
    identityService = engineRule.getIdentityService();
  }

  @Before
  public void saveAndReduceBatchJobsPerSeed() {
    ProcessEngineConfigurationImpl configuration = engineRule.getProcessEngineConfiguration();
    defaultBatchJobsPerSeed = configuration.getBatchJobsPerSeed();
    // reduce number of batch jobs per seed to not have to create a lot of instances
    configuration.setBatchJobsPerSeed(1);
  }

  @After
  public void removeBatches() {
    helper.removeAllRunningAndHistoricBatches();
  }

  @After
  public void resetBatchJobsPerSeed() {
    engineRule.getProcessEngineConfiguration()
      .setBatchJobsPerSeed(defaultBatchJobsPerSeed);
  }

  @Test
  public void shouldSuspendBatch() {
    // given
    Batch batch = helper.migrateProcessInstancesAsync(1);

    // when
    managementService.suspendBatchById(batch.getId());

    // then
    batch = managementService.createBatchQuery().batchId(batch.getId()).singleResult();
    assertTrue(batch.isSuspended());
  }

  @Test
  public void shouldFailWhenSuspendingUsingUnknownId() {
    try {
      managementService.suspendBatchById("unknown");
      fail("Exception expected");
    }
    catch (BadUserRequestException e) {
      assertThat(e.getMessage(), CoreMatchers.containsString("Batch for id 'unknown' cannot be found"));
    }
  }

  @Test
  public void shouldFailWhenSuspendingUsingNullId() {
    try {
      managementService.suspendBatchById(null);
      fail("Exception expected");
    }
    catch (BadUserRequestException e) {
      assertThat(e.getMessage(), CoreMatchers.containsString("batch id is null"));
    }
  }

  @Test
  public void shouldSuspendSeedJobAndDefinition() {
    // given
    Batch batch = helper.migrateProcessInstancesAsync(1);

    // when
    managementService.suspendBatchById(batch.getId());

    // then
    JobDefinition seedJobDefinition = helper.getSeedJobDefinition(batch);
    assertTrue(seedJobDefinition.isSuspended());

    Job seedJob = helper.getSeedJob(batch);
    assertTrue(seedJob.isSuspended());
  }

  @Test
  public void shouldCreateSuspendedSeedJob() {
    // given
    Batch batch = helper.migrateProcessInstancesAsync(2);
    managementService.suspendBatchById(batch.getId());

    // when
    helper.executeSeedJob(batch);

    // then
    Job seedJob = helper.getSeedJob(batch);
    assertTrue(seedJob.isSuspended());
  }

  @Test
  public void shouldSuspendMonitorJobAndDefinition() {
    // given
    Batch batch = helper.migrateProcessInstancesAsync(1);
    helper.executeSeedJob(batch);

    // when
    managementService.suspendBatchById(batch.getId());

    // then
    JobDefinition monitorJobDefinition = helper.getMonitorJobDefinition(batch);
    assertTrue(monitorJobDefinition.isSuspended());

    Job monitorJob = helper.getMonitorJob(batch);
    assertTrue(monitorJob.isSuspended());
  }

  @Test
  public void shouldCreateSuspendedMonitorJob() {
    // given
    Batch batch = helper.migrateProcessInstancesAsync(1);
    managementService.suspendBatchById(batch.getId());

    // when
    helper.executeSeedJob(batch);

    // then
    Job monitorJob = helper.getMonitorJob(batch);
    assertTrue(monitorJob.isSuspended());
  }

  @Test
  public void shouldSuspendExecutionJobsAndDefinition() {
    // given
    Batch batch = helper.migrateProcessInstancesAsync(1);
    helper.executeSeedJob(batch);

    // when
    managementService.suspendBatchById(batch.getId());

    // then
    JobDefinition migrationJobDefinition = helper.getExecutionJobDefinition(batch);
    assertTrue(migrationJobDefinition.isSuspended());

    Job migrationJob = helper.getExecutionJobs(batch).get(0);
    assertTrue(migrationJob.isSuspended());
  }

  @Test
  public void shouldCreateSuspendedExecutionJobs() {
    // given
    Batch batch = helper.migrateProcessInstancesAsync(1);
    managementService.suspendBatchById(batch.getId());

    // when
    helper.executeSeedJob(batch);

    // then
    Job migrationJob = helper.getExecutionJobs(batch).get(0);
    assertTrue(migrationJob.isSuspended());
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void shouldCreateUserOperationLogForBatchSuspension() {
    // given
    Batch batch = helper.migrateProcessInstancesAsync(1);

    // when
    identityService.setAuthenticatedUserId(USER_ID);
    managementService.suspendBatchById(batch.getId());
    identityService.clearAuthentication();

    // then
    UserOperationLogEntry entry = historyService.createUserOperationLogQuery()
      .singleResult();

    assertNotNull(entry);
    assertEquals(batch.getId(), entry.getBatchId());
    assertEquals(AbstractSetBatchStateCmd.SUSPENSION_STATE_PROPERTY, entry.getProperty());
    assertNull(entry.getOrgValue());
    assertEquals(SuspensionState.SUSPENDED.getName(), entry.getNewValue());
  }

  @Test
  public void shouldActivateBatch() {
    // given
    Batch batch = helper.migrateProcessInstancesAsync(1);
    managementService.suspendBatchById(batch.getId());

    // when
    managementService.activateBatchById(batch.getId());

    // then
    batch = managementService.createBatchQuery().batchId(batch.getId()).singleResult();
    assertFalse(batch.isSuspended());
  }

  @Test
  public void shouldFailWhenActivatingUsingUnknownId() {
    try {
      managementService.activateBatchById("unknown");
      fail("Exception expected");
    }
    catch (BadUserRequestException e) {
      assertThat(e.getMessage(), CoreMatchers.containsString("Batch for id 'unknown' cannot be found"));
    }
  }

  @Test
  public void shouldFailWhenActivatingUsingNullId() {
    try {
      managementService.activateBatchById(null);
      fail("Exception expected");
    }
    catch (BadUserRequestException e) {
      assertThat(e.getMessage(), CoreMatchers.containsString("batch id is null"));
    }
  }

  @Test
  public void shouldActivateSeedJobAndDefinition() {
    // given
    Batch batch = helper.migrateProcessInstancesAsync(1);
    managementService.suspendBatchById(batch.getId());

    // when
    managementService.activateBatchById(batch.getId());

    // then
    JobDefinition seedJobDefinition = helper.getSeedJobDefinition(batch);
    assertFalse(seedJobDefinition.isSuspended());

    Job seedJob = helper.getSeedJob(batch);
    assertFalse(seedJob.isSuspended());
  }

  @Test
  public void shouldCreateActivatedSeedJob() {
    // given
    Batch batch = helper.migrateProcessInstancesAsync(2);

    // when
    helper.executeSeedJob(batch);

    // then
    Job seedJob = helper.getSeedJob(batch);
    assertFalse(seedJob.isSuspended());
  }

  @Test
  public void shouldActivateMonitorJobAndDefinition() {
    // given
    Batch batch = helper.migrateProcessInstancesAsync(1);
    managementService.suspendBatchById(batch.getId());
    helper.executeSeedJob(batch);

    // when
    managementService.activateBatchById(batch.getId());

    // then
    JobDefinition monitorJobDefinition = helper.getMonitorJobDefinition(batch);
    assertFalse(monitorJobDefinition.isSuspended());

    Job monitorJob = helper.getMonitorJob(batch);
    assertFalse(monitorJob.isSuspended());
  }

  @Test
  public void shouldCreateActivatedMonitorJob() {
    // given
    Batch batch = helper.migrateProcessInstancesAsync(1);

    // when
    helper.executeSeedJob(batch);

    // then
    Job monitorJob = helper.getMonitorJob(batch);
    assertFalse(monitorJob.isSuspended());
  }

  @Test
  public void shouldActivateExecutionJobsAndDefinition() {
    // given
    Batch batch = helper.migrateProcessInstancesAsync(1);
    managementService.suspendBatchById(batch.getId());
    helper.executeSeedJob(batch);

    // when
    managementService.activateBatchById(batch.getId());

    // then
    JobDefinition migrationJobDefinition = helper.getExecutionJobDefinition(batch);
    assertFalse(migrationJobDefinition.isSuspended());

    Job migrationJob = helper.getExecutionJobs(batch).get(0);
    assertFalse(migrationJob.isSuspended());
  }

  @Test
  public void shouldCreateActivatedExecutionJobs() {
    // given
    Batch batch = helper.migrateProcessInstancesAsync(1);

    // when
    helper.executeSeedJob(batch);

    // then
    Job migrationJob = helper.getExecutionJobs(batch).get(0);
    assertFalse(migrationJob.isSuspended());
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void shouldCreateUserOperationLogForBatchActivation() {
    // given
    Batch batch = helper.migrateProcessInstancesAsync(1);
    managementService.suspendBatchById(batch.getId());

    // when
    identityService.setAuthenticatedUserId(USER_ID);
    managementService.activateBatchById(batch.getId());
    identityService.clearAuthentication();

    // then
    UserOperationLogEntry entry = historyService.createUserOperationLogQuery()
      .singleResult();

    assertNotNull(entry);
    assertEquals(batch.getId(), entry.getBatchId());
    assertEquals(AbstractSetBatchStateCmd.SUSPENSION_STATE_PROPERTY, entry.getProperty());
    assertNull(entry.getOrgValue());
    assertEquals(SuspensionState.ACTIVE.getName(), entry.getNewValue());
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void testUserOperationLogQueryByBatchEntityType() {
    // given
    Batch batch1 = helper.migrateProcessInstancesAsync(1);
    Batch batch2 = helper.migrateProcessInstancesAsync(1);

    // when
    identityService.setAuthenticatedUserId(USER_ID);
    managementService.suspendBatchById(batch1.getId());
    managementService.suspendBatchById(batch2.getId());
    managementService.activateBatchById(batch1.getId());
    identityService.clearAuthentication();

    // then
    UserOperationLogQuery query = historyService.createUserOperationLogQuery().entityType(BATCH);
    assertEquals(3, query.count());
    assertEquals(3, query.list().size());
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void testUserOperationLogQueryByBatchId() {
    // given
    Batch batch1 = helper.migrateProcessInstancesAsync(1);
    Batch batch2 = helper.migrateProcessInstancesAsync(1);

    // when
    identityService.setAuthenticatedUserId(USER_ID);
    managementService.suspendBatchById(batch1.getId());
    managementService.suspendBatchById(batch2.getId());
    managementService.activateBatchById(batch1.getId());
    identityService.clearAuthentication();

    // then
    UserOperationLogQuery query = historyService.createUserOperationLogQuery().batchId(batch1.getId());
    assertEquals(2, query.count());
    assertEquals(2, query.list().size());

    query = historyService.createUserOperationLogQuery().batchId(batch2.getId());
    assertEquals(1, query.count());
    assertEquals(1, query.list().size());
  }

}
