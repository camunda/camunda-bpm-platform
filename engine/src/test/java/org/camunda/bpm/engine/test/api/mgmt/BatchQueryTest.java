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
package org.camunda.bpm.engine.test.api.mgmt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.batchById;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.inverted;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.verifySorting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.BatchQuery;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.MigrationTestRule;
import org.camunda.bpm.engine.test.api.runtime.migration.batch.BatchMigrationHelper;
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
public class BatchQueryTest {

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected MigrationTestRule migrationRule = new MigrationTestRule(engineRule);
  protected BatchMigrationHelper helper = new BatchMigrationHelper(engineRule, migrationRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(migrationRule);

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
    helper.removeAllRunningAndHistoricBatches();
    ClockUtil.reset();
  }

  @Test
  public void testBatchQuery() {
    // given
    Batch batch1 = helper.migrateProcessInstancesAsync(1);
    Batch batch2 = helper.migrateProcessInstancesAsync(1);

    // when
    List<Batch> list = managementService.createBatchQuery().list();

    // then
    Assert.assertEquals(2, list.size());

    List<String> batchIds = new ArrayList<>();
    for (Batch resultBatch : list) {
      batchIds.add(resultBatch.getId());
    }

    Assert.assertTrue(batchIds.contains(batch1.getId()));
    Assert.assertTrue(batchIds.contains(batch2.getId()));
  }

  @Test
  public void testBatchQueryResult() {
    // given
    ClockUtil.setCurrentTime(new Date());
    Batch batch = helper.migrateProcessInstancesAsync(1);

    // when
    Batch resultBatch = managementService.createBatchQuery().singleResult();

    // then
    Assert.assertNotNull(batch);

    Assert.assertEquals(batch.getId(), resultBatch.getId());
    Assert.assertEquals(batch.getBatchJobDefinitionId(), resultBatch.getBatchJobDefinitionId());
    Assert.assertEquals(batch.getMonitorJobDefinitionId(), resultBatch.getMonitorJobDefinitionId());
    Assert.assertEquals(batch.getSeedJobDefinitionId(), resultBatch.getSeedJobDefinitionId());
    Assert.assertEquals(batch.getTenantId(), resultBatch.getTenantId());
    Assert.assertEquals(batch.getType(), resultBatch.getType());
    Assert.assertEquals(batch.getBatchJobsPerSeed(), resultBatch.getBatchJobsPerSeed());
    Assert.assertEquals(batch.getInvocationsPerBatchJob(), resultBatch.getInvocationsPerBatchJob());
    Assert.assertEquals(batch.getTotalJobs(), resultBatch.getTotalJobs());
    Assert.assertEquals(batch.getJobsCreated(), resultBatch.getJobsCreated());
    Assert.assertEquals(batch.isSuspended(), resultBatch.isSuspended());
    Assertions.assertThat(batch.getStartTime()).isEqualToIgnoringMillis(resultBatch.getStartTime());
    Assertions.assertThat(batch.getStartTime()).isEqualToIgnoringMillis(ClockUtil.getCurrentTime());
  }

  @Test
  public void testBatchQueryById() {
    // given
    Batch batch1 = helper.migrateProcessInstancesAsync(1);
    helper.migrateProcessInstancesAsync(1);

    // when
    Batch resultBatch = managementService.createBatchQuery().batchId(batch1.getId()).singleResult();

    // then
    Assert.assertNotNull(resultBatch);
    Assert.assertEquals(batch1.getId(), resultBatch.getId());
  }

  @Test
  public void testBatchQueryByIdNull() {
    try {
      managementService.createBatchQuery().batchId(null).singleResult();
      Assert.fail("exception expected");
    }
    catch (NullValueException e) {
      assertThat(e.getMessage()).contains("Batch id is null");
    }
  }

  @Test
  public void testBatchQueryByType() {
    // given
    Batch batch1 = helper.migrateProcessInstancesAsync(1);
    helper.migrateProcessInstancesAsync(1);

    // when
    long count = managementService.createBatchQuery().type(batch1.getType()).count();

    // then
    Assert.assertEquals(2, count);
  }

  @Test
  public void testBatchQueryByNonExistingType() {
    // given
    helper.migrateProcessInstancesAsync(1);

    // when
    long count = managementService.createBatchQuery().type("foo").count();

    // then
    Assert.assertEquals(0, count);
  }

  @Test
  public void testBatchQueryByTypeNull() {
    try {
      managementService.createBatchQuery().type(null).singleResult();
      Assert.fail("exception expected");
    }
    catch (NullValueException e) {
      assertThat(e.getMessage()).contains("Type is null");
    }
  }

  @Test
  public void testBatchQueryCount() {
    // given
    helper.migrateProcessInstancesAsync(1);
    helper.migrateProcessInstancesAsync(1);

    // when
    long count = managementService.createBatchQuery().count();

    // then
    Assert.assertEquals(2, count);
  }

  @Test
  public void testBatchQueryOrderByIdAsc() {
    // given
    helper.migrateProcessInstancesAsync(1);
    helper.migrateProcessInstancesAsync(1);

    // when
    List<Batch> orderedBatches = managementService.createBatchQuery().orderById().asc().list();

    // then
    verifySorting(orderedBatches, batchById());
  }

  @Test
  public void testBatchQueryOrderByIdDec() {
    // given
    helper.migrateProcessInstancesAsync(1);
    helper.migrateProcessInstancesAsync(1);

    // when
    List<Batch> orderedBatches = managementService.createBatchQuery().orderById().desc().list();

    // then
    verifySorting(orderedBatches, inverted(batchById()));
  }

  @Test
  public void testBatchQueryOrderingPropertyWithoutOrder() {
    try {
      managementService.createBatchQuery().orderById().singleResult();
      Assert.fail("exception expected");
    }
    catch (NotValidException e) {
      assertThat(e.getMessage()).contains("Invalid query: "
          + "call asc() or desc() after using orderByXX()");
    }
  }

  @Test
  public void testBatchQueryOrderWithoutOrderingProperty() {
    try {
      managementService.createBatchQuery().asc().singleResult();
      Assert.fail("exception expected");
    }
    catch (NotValidException e) {
      assertThat(e.getMessage()).contains("You should call any of the orderBy methods "
          + "first before specifying a direction");
    }
  }

  @Test
  public void testBatchQueryBySuspendedBatches() {
    // given
    Batch batch1 = helper.migrateProcessInstancesAsync(1);
    Batch batch2 = helper.migrateProcessInstancesAsync(1);
    helper.migrateProcessInstancesAsync(1);

    // when
    managementService.suspendBatchById(batch1.getId());
    managementService.suspendBatchById(batch2.getId());
    managementService.activateBatchById(batch1.getId());

    // then
    BatchQuery query = managementService.createBatchQuery().suspended();
    Assert.assertEquals(1, query.count());
    Assert.assertEquals(1, query.list().size());
    Assert.assertEquals(batch2.getId(), query.singleResult().getId());
  }

  @Test
  public void testBatchQueryByActiveBatches() {
    // given
    Batch batch1 = helper.migrateProcessInstancesAsync(1);
    Batch batch2 = helper.migrateProcessInstancesAsync(1);
    Batch batch3 = helper.migrateProcessInstancesAsync(1);

    // when
    managementService.suspendBatchById(batch1.getId());
    managementService.suspendBatchById(batch2.getId());
    managementService.activateBatchById(batch1.getId());

    // then
    BatchQuery query = managementService.createBatchQuery().active();
    Assert.assertEquals(2, query.count());
    Assert.assertEquals(2, query.list().size());

    List<String> foundIds = new ArrayList<>();
    for (Batch batch : query.list()) {
      foundIds.add(batch.getId());
    }
    assertThat(foundIds).contains(
      batch1.getId(),
      batch3.getId()
    );
  }

}
