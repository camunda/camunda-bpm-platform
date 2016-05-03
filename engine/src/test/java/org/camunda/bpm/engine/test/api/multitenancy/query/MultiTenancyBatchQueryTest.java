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
package org.camunda.bpm.engine.test.api.multitenancy.query;

import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.batchByTenantId;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.inverted;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.verifySorting;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.BatchStatistics;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.api.runtime.migration.batch.BatchMigrationHelper;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
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
public class MultiTenancyBatchQueryTest {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain defaultRuleChin = RuleChain.outerRule(engineRule).around(testHelper);

  protected BatchMigrationHelper batchHelper = new BatchMigrationHelper(engineRule);

  protected ManagementService managementService;
  protected RuntimeService runtimeService;
  protected HistoryService historyService;
  protected IdentityService identityService;

  protected Batch sharedBatch;
  protected Batch tenant1Batch;
  protected Batch tenant2Batch;

  @Before
  public void initServices() {
    managementService= engineRule.getManagementService();
    runtimeService = engineRule.getRuntimeService();
    historyService = engineRule.getHistoryService();
    identityService = engineRule.getIdentityService();
  }

  @Before
  public void deployProcesses() {
    ProcessDefinition sharedDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition tenant1Definition = testHelper.deployForTenantAndGetDefinition(TENANT_ONE, ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition tenant2Definition = testHelper.deployForTenantAndGetDefinition(TENANT_TWO, ProcessModels.ONE_TASK_PROCESS);

    sharedBatch = createInstanceAndStartBatchMigration(sharedDefinition);
    tenant1Batch = createInstanceAndStartBatchMigration(tenant1Definition);
    tenant2Batch = createInstanceAndStartBatchMigration(tenant2Definition);
  }

  @After
  public void removeBatches() {
    HistoryService historyService = engineRule.getHistoryService();

    for (Batch batch : managementService.createBatchQuery().list()) {
      managementService.deleteBatch(batch.getId(), true);
    }

    // remove history of completed batches
    for (HistoricBatch historicBatch : historyService.createHistoricBatchQuery().list()) {
      historyService.deleteHistoricBatch(historicBatch.getId());
    }
  }

  @Test
  public void testBatchQueryNoAuthenticatedTenant() {
    // given
    identityService.setAuthentication("user", null, null);

    // then
    List<Batch> batches = managementService.createBatchQuery().list();
    Assert.assertEquals(1, batches.size());
    Assert.assertEquals(sharedBatch.getId(), batches.get(0).getId());

    Assert.assertEquals(1, managementService.createBatchQuery().count());

    identityService.clearAuthentication();
  }

  @Test
  public void testBatchQueryAuthenticatedTenant() {
    // given
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    // when
    List<Batch> batches = managementService.createBatchQuery().list();

    // then
    Assert.assertEquals(2, batches.size());
    assertBatches(batches, tenant1Batch.getId(), sharedBatch.getId());

    Assert.assertEquals(2, managementService.createBatchQuery().count());

    identityService.clearAuthentication();
  }

  @Test
  public void testBatchQueryAuthenticatedTenants() {
    // given
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    // when
    List<Batch> batches = managementService.createBatchQuery().list();

    // then
    Assert.assertEquals(3, batches.size());
    Assert.assertEquals(3, managementService.createBatchQuery().count());

    identityService.clearAuthentication();
  }

  @Test
  public void testBatchStatisticsNoAuthenticatedTenant() {
    // given
    identityService.setAuthentication("user", null, null);

    // when
    List<BatchStatistics> statistics = managementService.createBatchStatisticsQuery().list();

    // then
    Assert.assertEquals(1, statistics.size());
    Assert.assertEquals(sharedBatch.getId(), statistics.get(0).getId());

    Assert.assertEquals(1, managementService.createBatchStatisticsQuery().count());

    identityService.clearAuthentication();
  }

  @Test
  public void testBatchStatisticsAuthenticatedTenant() {
    // given
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    // when
    List<BatchStatistics> statistics = managementService.createBatchStatisticsQuery().list();

    // then
    Assert.assertEquals(2, statistics.size());

    Assert.assertEquals(2, managementService.createBatchStatisticsQuery().count());

    identityService.clearAuthentication();
  }

  @Test
  public void testBatchStatisticsAuthenticatedTenants() {
    // given
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    // then
    List<BatchStatistics> statistics = managementService.createBatchStatisticsQuery().list();
    Assert.assertEquals(3, statistics.size());

    Assert.assertEquals(3, managementService.createBatchStatisticsQuery().count());

    identityService.clearAuthentication();
  }

  @Test
  public void testBatchQueryFilterByTenant() {
    // when
    Batch returnedBatch = managementService.createBatchQuery().tenantIdIn(TENANT_ONE).singleResult();

    // then
    Assert.assertNotNull(returnedBatch);
    Assert.assertEquals(tenant1Batch.getId(), returnedBatch.getId());
  }

  @Test
  public void testBatchQueryFilterByTenants() {
    // when
    List<Batch> returnedBatches = managementService.createBatchQuery()
      .tenantIdIn(TENANT_ONE, TENANT_TWO)
      .orderByTenantId()
      .asc()
      .list();

    // then
    Assert.assertEquals(2, returnedBatches.size());
    Assert.assertEquals(tenant1Batch.getId(), returnedBatches.get(0).getId());
    Assert.assertEquals(tenant2Batch.getId(), returnedBatches.get(1).getId());
  }

  @Test
  public void testBatchQueryFilterWithoutTenantId() {
    // when
    Batch returnedBatch = managementService.createBatchQuery().withoutTenantId().singleResult();

    // then
    Assert.assertNotNull(returnedBatch);
    Assert.assertEquals(sharedBatch.getId(), returnedBatch.getId());
  }

  @Test
  public void testBatchQueryFailOnNullTenantIdCase1() {

    String[] tenantIds = null;
    try {
      managementService.createBatchQuery().tenantIdIn(tenantIds);
      Assert.fail("exception expected");
    }
    catch (NullValueException e) {
      // happy path
    }
  }

  @Test
  public void testBatchQueryFailOnNullTenantIdCase2() {

    String[] tenantIds = new String[]{ null };
    try {
      managementService.createBatchQuery().tenantIdIn(tenantIds);
      Assert.fail("exception expected");
    }
    catch (NullValueException e) {
      // happy path
    }
  }

  @Test
  public void testOrderByTenantIdAsc() {

    // when
    List<Batch> orderedBatches = managementService.createBatchQuery().orderByTenantId().asc().list();

    // then
    verifySorting(orderedBatches, batchByTenantId());
  }

  @Test
  public void testOrderByTenantIdDesc() {

    // when
    List<Batch> orderedBatches = managementService.createBatchQuery().orderByTenantId().desc().list();

    // then
    verifySorting(orderedBatches, inverted(batchByTenantId()));
  }

  // TODO: auch noch für statistics query bauen

  protected Batch createInstanceAndStartBatchMigration(ProcessDefinition processDefinition) {
    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceById(processDefinition.getId());

    MigrationPlan migrationPlan = engineRule.getRuntimeService()
      .createMigrationPlan(processDefinition.getId(), processDefinition.getId())
      .mapEqualActivities()
      .build();

    Batch batch = engineRule.getRuntimeService()
      .newMigration(migrationPlan)
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .executeAsync();

    return batch;
  }


  protected void assertBatches(List<? extends Batch> actualBatches, String... expectedIds) {
    Assert.assertEquals(expectedIds.length, actualBatches.size());

    Set<String> actualIds = new HashSet<String>();
    for (Batch batch : actualBatches) {
      actualIds.add(batch.getId());
    }

    for (String expectedId : expectedIds) {
      Assert.assertTrue(actualIds.contains(expectedId));
    }
  }
}
