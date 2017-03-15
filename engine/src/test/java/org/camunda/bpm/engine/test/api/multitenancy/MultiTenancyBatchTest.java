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
package org.camunda.bpm.engine.test.api.multitenancy;

import static java.util.Collections.singletonList;

import java.util.List;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.runtime.migration.batch.BatchMigrationHelper;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Thorben Lindhauer
 *
 */
public class MultiTenancyBatchTest {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain defaultRuleChin = RuleChain.outerRule(engineRule).around(testHelper);

  protected BatchMigrationHelper batchHelper = new BatchMigrationHelper(engineRule);

  protected ManagementService managementService;
  protected HistoryService historyService;
  protected IdentityService identityService;

  protected ProcessDefinition tenant1Definition;
  protected ProcessDefinition tenant2Definition;
  protected ProcessDefinition sharedDefinition;

  @Before
  public void initServices() {
    managementService = engineRule.getManagementService();
    historyService = engineRule.getHistoryService();
    identityService = engineRule.getIdentityService();
  }

  @Before
  public void deployProcesses() {
    sharedDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    tenant1Definition = testHelper.deployForTenantAndGetDefinition(TENANT_ONE, ProcessModels.ONE_TASK_PROCESS);
    tenant2Definition = testHelper.deployForTenantAndGetDefinition(TENANT_TWO, ProcessModels.ONE_TASK_PROCESS);
  }

  @After
  public void removeBatches() {
    batchHelper.removeAllRunningAndHistoricBatches();
  }

  /**
   * Source: no tenant id
   * Target: no tenant id
   */
  @Test
  public void testBatchTenantIdCase1() {
    // given
    Batch batch = batchHelper.migrateProcessInstanceAsync(sharedDefinition, sharedDefinition);

    // then
    Assert.assertNull(batch.getTenantId());
  }

  /**
   * Source: tenant 1
   * Target: no tenant id
   */
  @Test
  public void testBatchTenantIdCase2() {
    // given
    Batch batch = batchHelper.migrateProcessInstanceAsync(tenant1Definition, sharedDefinition);

    // then
    Assert.assertEquals(TENANT_ONE, batch.getTenantId());
  }

  /**
   * Source: no tenant id
   * Target: tenant 1
   */
  @Test
  public void testBatchTenantIdCase3() {
    // given
    Batch batch = batchHelper.migrateProcessInstanceAsync(sharedDefinition, tenant1Definition);

    // then
    Assert.assertNull(batch.getTenantId());
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void testHistoricBatchTenantId() {
    // given
    batchHelper.migrateProcessInstanceAsync(tenant1Definition, tenant1Definition);

    // then
    HistoricBatch historicBatch = historyService.createHistoricBatchQuery().singleResult();
    Assert.assertEquals(TENANT_ONE, historicBatch.getTenantId());
  }

  @Test
  public void testBatchJobDefinitionsTenantId() {
    // given
    Batch batch = batchHelper.migrateProcessInstanceAsync(tenant1Definition, tenant1Definition);

    // then
    JobDefinition migrationJobDefinition = batchHelper.getExecutionJobDefinition(batch);
    Assert.assertEquals(TENANT_ONE, migrationJobDefinition.getTenantId());

    JobDefinition monitorJobDefinition = batchHelper.getMonitorJobDefinition(batch);
    Assert.assertEquals(TENANT_ONE, monitorJobDefinition.getTenantId());

    JobDefinition seedJobDefinition = batchHelper.getSeedJobDefinition(batch);
    Assert.assertEquals(TENANT_ONE, seedJobDefinition.getTenantId());
  }

  @Test
  public void testBatchJobsTenantId() {
    // given
    Batch batch = batchHelper.migrateProcessInstanceAsync(tenant1Definition, tenant1Definition);

    // then
    Job seedJob = batchHelper.getSeedJob(batch);
    Assert.assertEquals(TENANT_ONE, seedJob.getTenantId());

    batchHelper.executeSeedJob(batch);

    List<Job> migrationJob = batchHelper.getExecutionJobs(batch);
    Assert.assertEquals(TENANT_ONE, migrationJob.get(0).getTenantId());

    Job monitorJob = batchHelper.getMonitorJob(batch);
    Assert.assertEquals(TENANT_ONE, monitorJob.getTenantId());
  }

  @Test
  public void testDeleteBatch() {
    // given
    Batch batch = batchHelper.migrateProcessInstanceAsync(tenant1Definition, tenant1Definition);

    // when
    identityService.setAuthentication("user", null, singletonList(TENANT_ONE));
    managementService.deleteBatch(batch.getId(), true);
    identityService.clearAuthentication();

    // then
    Assert.assertEquals(0, managementService.createBatchQuery().count());
  }

  @Test
  public void testDeleteBatchFailsWithWrongTenant() {
    // given
    Batch batch = batchHelper.migrateProcessInstanceAsync(tenant2Definition, tenant2Definition);

    // when
    identityService.setAuthentication("user", null, singletonList(TENANT_ONE));
    try {
      managementService.deleteBatch(batch.getId(), true);
      Assert.fail("exception expected");
    }
    catch (ProcessEngineException e) {
      // then
      Assert.assertThat(e.getMessage(), CoreMatchers.containsString("Cannot delete batch '"
        + batch.getId() + "' because it belongs to no authenticated tenant"));
    }
    finally {
      identityService.clearAuthentication();
    }
  }

  @Test
  public void testSuspendBatch() {
    // given
    Batch batch = batchHelper.migrateProcessInstanceAsync(tenant1Definition, tenant1Definition);

    // when
    identityService.setAuthentication("user", null, singletonList(TENANT_ONE));
    managementService.suspendBatchById(batch.getId());
    identityService.clearAuthentication();

    // then
    batch = managementService.createBatchQuery().batchId(batch.getId()).singleResult();
    Assert.assertTrue(batch.isSuspended());
  }

  @Test
  public void testSuspendBatchFailsWithWrongTenant() {
    // given
    Batch batch = batchHelper.migrateProcessInstanceAsync(tenant2Definition, tenant2Definition);

    // when
    identityService.setAuthentication("user", null, singletonList(TENANT_ONE));
    try {
      managementService.suspendBatchById(batch.getId());
      Assert.fail("exception expected");
    }
    catch (ProcessEngineException e) {
      // then
      Assert.assertThat(e.getMessage(), CoreMatchers.containsString("Cannot suspend batch '"
      + batch.getId() +"' because it belongs to no authenticated tenant"));
    }
    finally {
      identityService.clearAuthentication();
    }
  }

  @Test
  public void testActivateBatch() {
    // given
    Batch batch = batchHelper.migrateProcessInstanceAsync(tenant1Definition, tenant1Definition);
    managementService.suspendBatchById(batch.getId());

    // when
    identityService.setAuthentication("user", null, singletonList(TENANT_ONE));
    managementService.activateBatchById(batch.getId());
    identityService.clearAuthentication();

    // then
    batch = managementService.createBatchQuery().batchId(batch.getId()).singleResult();
    Assert.assertFalse(batch.isSuspended());
  }

  @Test
  public void testActivateBatchFailsWithWrongTenant() {
    // given
    Batch batch = batchHelper.migrateProcessInstanceAsync(tenant2Definition, tenant2Definition);
    managementService.suspendBatchById(batch.getId());

    // when
    identityService.setAuthentication("user", null, singletonList(TENANT_ONE));
    try {
      managementService.activateBatchById(batch.getId());
      Assert.fail("exception expected");
    }
    catch (ProcessEngineException e) {
      // then
      Assert.assertThat(e.getMessage(), CoreMatchers.containsString("Cannot activate batch '"
      + batch.getId() + "' because it belongs to no authenticated tenant"));
    }
    finally {
      identityService.clearAuthentication();
    }
  }

}
