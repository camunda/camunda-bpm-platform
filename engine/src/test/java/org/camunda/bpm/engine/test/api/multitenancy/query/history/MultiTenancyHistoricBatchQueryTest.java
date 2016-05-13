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
package org.camunda.bpm.engine.test.api.multitenancy.query.history;

import static java.util.Collections.singletonList;

import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.historicBatchByTenantId;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.inverted;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.verifySorting;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.repository.ProcessDefinition;
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
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class MultiTenancyHistoricBatchQueryTest {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain defaultRuleChin = RuleChain.outerRule(engineRule).around(testHelper);

  protected BatchMigrationHelper batchHelper = new BatchMigrationHelper(engineRule);

  protected HistoryService historyService;
  protected IdentityService identityService;

  protected Batch sharedBatch;
  protected Batch tenant1Batch;
  protected Batch tenant2Batch;

  @Before
  public void initServices() {
    historyService = engineRule.getHistoryService();
    identityService = engineRule.getIdentityService();
  }

  @Before
  public void deployProcesses() {
    ProcessDefinition sharedDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition tenant1Definition = testHelper.deployForTenantAndGetDefinition(TENANT_ONE, ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition tenant2Definition = testHelper.deployForTenantAndGetDefinition(TENANT_TWO, ProcessModels.ONE_TASK_PROCESS);

    sharedBatch = batchHelper.migrateProcessInstanceAsync(sharedDefinition, sharedDefinition);
    tenant1Batch = batchHelper.migrateProcessInstanceAsync(tenant1Definition, tenant1Definition);
    tenant2Batch = batchHelper.migrateProcessInstanceAsync(tenant2Definition, tenant2Definition);
  }

  @After
  public void removeBatches() {
    batchHelper.removeAllRunningAndHistoricBatches();
  }

  @Test
  public void testHistoricBatchQueryNoAuthenticatedTenant() {
    // given
    identityService.setAuthentication("user", null, null);

    // when
    List<HistoricBatch> batches = historyService.createHistoricBatchQuery().list();

    // then
    Assert.assertEquals(1, batches.size());
    Assert.assertEquals(sharedBatch.getId(), batches.get(0).getId());

    Assert.assertEquals(1, historyService.createHistoricBatchQuery().count());

    identityService.clearAuthentication();
  }

  @Test
  public void testHistoricBatchQueryAuthenticatedTenant() {
    // given
    identityService.setAuthentication("user", null, singletonList(TENANT_ONE));

    // when
    List<HistoricBatch> batches = historyService.createHistoricBatchQuery().list();

    // then
    Assert.assertEquals(2, batches.size());
    assertBatches(batches, tenant1Batch.getId(), sharedBatch.getId());

    Assert.assertEquals(2, historyService.createHistoricBatchQuery().count());

    identityService.clearAuthentication();
  }

  @Test
  public void testHistoricBatchQueryAuthenticatedTenants() {
    // given
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    // when
    List<HistoricBatch> batches = historyService.createHistoricBatchQuery().list();

    // then
    Assert.assertEquals(3, batches.size());

    Assert.assertEquals(3, historyService.createHistoricBatchQuery().count());

    identityService.clearAuthentication();
  }

  @Test
  public void testDeleteHistoricBatch() {
    // given
    identityService.setAuthentication("user", null, singletonList(TENANT_ONE));

    // when
    historyService.deleteHistoricBatch(tenant1Batch.getId());

    // then
    identityService.clearAuthentication();
    Assert.assertEquals(2, historyService.createHistoricBatchQuery().count());
  }

  @Test
  public void testDeleteHistoricBatchFailsWithWrongTenant() {
    // given
    identityService.setAuthentication("user", null, singletonList(TENANT_ONE));

    // when
    try {
      historyService.deleteHistoricBatch(tenant2Batch.getId());
      Assert.fail("exception expected");
    }
    catch (ProcessEngineException e) {
      // then
      Assert.assertThat(e.getMessage(), CoreMatchers
        .containsString("Cannot delete historic batch '"+ tenant2Batch.getId() 
        +"' because it belongs to no authenticated tenant"));
    }

    identityService.clearAuthentication();
  }


  @Test
  public void testHistoricBatchQueryFilterByTenant() {
    // when
    HistoricBatch returnedBatch = historyService.createHistoricBatchQuery().tenantIdIn(TENANT_ONE).singleResult();

    // then
    Assert.assertNotNull(returnedBatch);
    Assert.assertEquals(tenant1Batch.getId(), returnedBatch.getId());
  }

  @Test
  public void testHistoricBatchQueryFilterByTenants() {
    // when
    List<HistoricBatch> returnedBatches = historyService.createHistoricBatchQuery()
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
  public void testHistoricBatchQueryFilterWithoutTenantId() {
    // when
    HistoricBatch returnedBatch = historyService.createHistoricBatchQuery().withoutTenantId().singleResult();

    // then
    Assert.assertNotNull(returnedBatch);
    Assert.assertEquals(sharedBatch.getId(), returnedBatch.getId());
  }

  @Test
  public void testBatchQueryFailOnNullTenantIdCase1() {

    String[] tenantIds = null;
    try {
      historyService.createHistoricBatchQuery().tenantIdIn(tenantIds);
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
      historyService.createHistoricBatchQuery().tenantIdIn(tenantIds);
      Assert.fail("exception expected");
    }
    catch (NullValueException e) {
      // happy path
    }
  }

  @Test
  public void testOrderByTenantIdAsc() {

    // when
    List<HistoricBatch> orderedBatches = historyService.createHistoricBatchQuery().orderByTenantId().asc().list();

    // then
    verifySorting(orderedBatches, historicBatchByTenantId());
  }

  @Test
  public void testOrderByTenantIdDesc() {

    // when
    List<HistoricBatch> orderedBatches = historyService.createHistoricBatchQuery().orderByTenantId().desc().list();

    // then
    verifySorting(orderedBatches, inverted(historicBatchByTenantId()));
  }

  protected void assertBatches(List<HistoricBatch> actualBatches, String... expectedIds) {
    Assert.assertEquals(expectedIds.length, actualBatches.size());

    Set<String> actualIds = new HashSet<String>();
    for (HistoricBatch batch : actualBatches) {
      actualIds.add(batch.getId());
    }

    for (String expectedId : expectedIds) {
      Assert.assertTrue(actualIds.contains(expectedId));
    }
  }
}
