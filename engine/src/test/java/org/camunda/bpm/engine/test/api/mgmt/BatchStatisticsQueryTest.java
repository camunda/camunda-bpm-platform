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
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.batchStatisticsById;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.batchStatisticsByStartTime;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.inverted;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.verifySorting;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.BatchStatistics;
import org.camunda.bpm.engine.batch.BatchStatisticsQuery;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.MigrationTestRule;
import org.camunda.bpm.engine.test.api.runtime.migration.batch.BatchMigrationHelper;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class BatchStatisticsQueryTest {

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected MigrationTestRule migrationRule = new MigrationTestRule(engineRule);
  protected BatchMigrationHelper helper = new BatchMigrationHelper(engineRule, migrationRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(migrationRule);

  protected ManagementService managementService;
  protected int defaultBatchJobsPerSeed;

  @Before
  public void initServices() {
    managementService = engineRule.getManagementService();
  }

  @Before
  public void saveAndReduceBatchJobsPerSeed() {
    ProcessEngineConfigurationImpl configuration = engineRule.getProcessEngineConfiguration();
    defaultBatchJobsPerSeed = configuration.getBatchJobsPerSeed();
    // reduce number of batch jobs per seed to not have to create a lot of instances
    configuration.setBatchJobsPerSeed(10);
  }

  @After
  public void resetBatchJobsPerSeed() {
    engineRule.getProcessEngineConfiguration()
      .setBatchJobsPerSeed(defaultBatchJobsPerSeed);
    ClockUtil.reset();
  }

  @After
  public void removeBatches() {
    helper.removeAllRunningAndHistoricBatches();
  }

  @Test
  public void testQuery() {
    List<BatchStatistics> statistics = managementService.createBatchStatisticsQuery().list();
    assertEquals(0, statistics.size());

    Batch batch1 = helper.createMigrationBatchWithSize(1);

    statistics = managementService.createBatchStatisticsQuery().list();
    assertEquals(1, statistics.size());
    assertEquals(batch1.getId(), statistics.get(0).getId());

    Batch batch2 = helper.createMigrationBatchWithSize(1);
    Batch batch3 = helper.createMigrationBatchWithSize(1);

    statistics = managementService.createBatchStatisticsQuery().list();
    assertEquals(3, statistics.size());

    helper.completeBatch(batch1);
    helper.completeBatch(batch3);

    statistics = managementService.createBatchStatisticsQuery().list();
    assertEquals(1, statistics.size());
    assertEquals(batch2.getId(), statistics.get(0).getId());

    helper.completeBatch(batch2);

    statistics = managementService.createBatchStatisticsQuery().list();
    assertEquals(0, statistics.size());
  }

  @Test
  public void testQueryCount() {
    long count = managementService.createBatchStatisticsQuery().count();
    assertEquals(0, count);

    Batch batch1 = helper.createMigrationBatchWithSize(1);

    count = managementService.createBatchStatisticsQuery().count();
    assertEquals(1, count);

    Batch batch2 = helper.createMigrationBatchWithSize(1);
    Batch batch3 = helper.createMigrationBatchWithSize(1);

    count = managementService.createBatchStatisticsQuery().count();
    assertEquals(3, count);

    helper.completeBatch(batch1);
    helper.completeBatch(batch3);

    count = managementService.createBatchStatisticsQuery().count();
    assertEquals(1, count);

    helper.completeBatch(batch2);

    count = managementService.createBatchStatisticsQuery().count();
    assertEquals(0, count);
  }

  @Test
  public void testQueryById() {
    // given
    helper.createMigrationBatchWithSize(1);
    Batch batch = helper.createMigrationBatchWithSize(1);

    // when
    BatchStatistics statistics = managementService.createBatchStatisticsQuery()
      .batchId(batch.getId())
      .singleResult();

    // then
    assertEquals(batch.getId(), statistics.getId());
  }

  @Test
  public void testQueryByNullId() {
    try {
      managementService.createBatchStatisticsQuery()
        .batchId(null)
        .singleResult();
      Assert.fail("exception expected");
    }
    catch (NullValueException e) {
      Assert.assertThat(e.getMessage(), CoreMatchers.containsString("Batch id is null"));
    }
  }

  @Test
  public void testQueryByUnknownId() {
    // given
    helper.createMigrationBatchWithSize(1);
    helper.createMigrationBatchWithSize(1);

    // when
    List<BatchStatistics> statistics = managementService.createBatchStatisticsQuery()
      .batchId("unknown")
      .list();

    // then
    assertEquals(0, statistics.size());
  }

  @Test
  public void testQueryByType() {
    // given
    helper.createMigrationBatchWithSize(1);
    helper.createMigrationBatchWithSize(1);

    // when
    List<BatchStatistics> statistics = managementService.createBatchStatisticsQuery()
      .type(Batch.TYPE_PROCESS_INSTANCE_MIGRATION)
      .list();

    // then
    assertEquals(2, statistics.size());
  }

  @Test
  public void testQueryByNullType() {
    try {
      managementService.createBatchStatisticsQuery()
        .type(null)
        .list();
      Assert.fail("exception expected");
    }
    catch (NullValueException e) {
      Assert.assertThat(e.getMessage(), CoreMatchers.containsString("Type is null"));
    }
  }

  @Test
  public void testQueryByUnknownType() {
    // given
    helper.createMigrationBatchWithSize(1);
    helper.createMigrationBatchWithSize(1);

    // when
    List<BatchStatistics> statistics = managementService.createBatchStatisticsQuery()
      .type("unknown")
      .list();

    // then
    assertEquals(0, statistics.size());
  }

  @Test
  public void testQueryOrderByIdAsc() {
    // given
    helper.migrateProcessInstancesAsync(1);
    helper.migrateProcessInstancesAsync(1);

    // when
    List<BatchStatistics> statistics = managementService.createBatchStatisticsQuery()
      .orderById().asc()
      .list();

    // then
    verifySorting(statistics, batchStatisticsById());
  }

  @Test
  public void testQueryOrderByIdDec() {
    // given
    helper.migrateProcessInstancesAsync(1);
    helper.migrateProcessInstancesAsync(1);

    // when
    List<BatchStatistics> statistics = managementService.createBatchStatisticsQuery()
      .orderById().desc()
      .list();

    // then
    verifySorting(statistics, inverted(batchStatisticsById()));
  }

  @Test
  public void testQueryOrderByStartTimeAsc() {
    // given
    helper.migrateProcessInstancesAsync(1);
    final long oneHour = 60 * 60 * 1000L;
    ClockUtil.setCurrentTime(new Date(ClockUtil.getCurrentTime().getTime() + oneHour));
    helper.migrateProcessInstancesAsync(1);

    // when
    List<BatchStatistics> statistics = managementService.createBatchStatisticsQuery()
      .orderByStartTime().asc()
      .list();

    // then
    verifySorting(statistics, batchStatisticsByStartTime());
  }

  @Test
  public void testQueryOrderByStartTimeDesc() {
    // given
    helper.migrateProcessInstancesAsync(1);
    final long oneHour = 60 * 60 * 1000L;
    ClockUtil.setCurrentTime(new Date(ClockUtil.getCurrentTime().getTime() + oneHour));
    helper.migrateProcessInstancesAsync(1);

    // when
    List<BatchStatistics> statistics = managementService.createBatchStatisticsQuery()
      .orderByStartTime().desc()
      .list();

    // then
    verifySorting(statistics, inverted(batchStatisticsByStartTime()));
  }

  @Test
  public void testQueryOrderingPropertyWithoutOrder() {
    try {
      managementService.createBatchStatisticsQuery()
        .orderById()
        .list();
      Assert.fail("exception expected");
    }
    catch (NotValidException e) {
      Assert.assertThat(e.getMessage(), CoreMatchers.containsString("Invalid query: "
        + "call asc() or desc() after using orderByXX()"));
    }
  }

  @Test
  public void testQueryOrderWithoutOrderingProperty() {
    try {
      managementService.createBatchStatisticsQuery()
        .asc()
        .list();
      Assert.fail("exception expected");
    }
    catch (NotValidException e) {
      Assert.assertThat(e.getMessage(), CoreMatchers.containsString("You should call any of the orderBy methods "
        + "first before specifying a direction"));
    }
  }

  @Test
  public void testStatisticsNoExecutionJobsGenerated() {
    // given
    helper.createMigrationBatchWithSize(3);

    // when
    BatchStatistics batchStatistics = managementService.createBatchStatisticsQuery()
      .singleResult();

    // then
    assertEquals(3, batchStatistics.getTotalJobs());
    assertEquals(0, batchStatistics.getJobsCreated());
    assertEquals(3, batchStatistics.getRemainingJobs());
    assertEquals(0, batchStatistics.getCompletedJobs());
    assertEquals(0, batchStatistics.getFailedJobs());
  }

  @Test
  public void testStatisticsMostExecutionJobsGenerated() {
    // given
    Batch batch = helper.createMigrationBatchWithSize(13);

    // when
    helper.executeJob(helper.getSeedJob(batch));

    // then
    BatchStatistics batchStatistics = managementService.createBatchStatisticsQuery()
      .singleResult();

    assertEquals(13, batchStatistics.getTotalJobs());
    assertEquals(10, batchStatistics.getJobsCreated());
    assertEquals(13, batchStatistics.getRemainingJobs());
    assertEquals(0, batchStatistics.getCompletedJobs());
    assertEquals(0, batchStatistics.getFailedJobs());
  }

  @Test
  public void testStatisticsAllExecutionJobsGenerated() {
    // given
    Batch batch = helper.createMigrationBatchWithSize(3);

    // when
    helper.completeSeedJobs(batch);

    // then
    BatchStatistics batchStatistics = managementService.createBatchStatisticsQuery()
      .singleResult();

    assertEquals(3, batchStatistics.getTotalJobs());
    assertEquals(3, batchStatistics.getJobsCreated());
    assertEquals(3, batchStatistics.getRemainingJobs());
    assertEquals(0, batchStatistics.getCompletedJobs());
    assertEquals(0, batchStatistics.getFailedJobs());
  }

  @Test
  public void testStatisticsOneCompletedJob() {
    // given
    Batch batch = helper.createMigrationBatchWithSize(3);

    // when
    helper.completeSeedJobs(batch);
    helper.completeJobs(batch, 1);

    // then
    BatchStatistics batchStatistics = managementService.createBatchStatisticsQuery()
      .singleResult();

    assertEquals(3, batchStatistics.getTotalJobs());
    assertEquals(3, batchStatistics.getJobsCreated());
    assertEquals(2, batchStatistics.getRemainingJobs());
    assertEquals(1, batchStatistics.getCompletedJobs());
    assertEquals(0, batchStatistics.getFailedJobs());
  }

  @Test
  public void testStatisticsOneFailedJob() {
    // given
    Batch batch = helper.createMigrationBatchWithSize(3);

    // when
    helper.completeSeedJobs(batch);
    helper.failExecutionJobs(batch, 1);

    // then
    BatchStatistics batchStatistics = managementService.createBatchStatisticsQuery()
      .singleResult();

    assertEquals(3, batchStatistics.getTotalJobs());
    assertEquals(3, batchStatistics.getJobsCreated());
    assertEquals(3, batchStatistics.getRemainingJobs());
    assertEquals(0, batchStatistics.getCompletedJobs());
    assertEquals(1, batchStatistics.getFailedJobs());
  }

  @Test
  public void testStatisticsOneCompletedAndOneFailedJob() {
    // given
    Batch batch = helper.createMigrationBatchWithSize(3);

    // when
    helper.completeSeedJobs(batch);
    helper.completeJobs(batch, 1);
    helper.failExecutionJobs(batch, 1);

    // then
    BatchStatistics batchStatistics = managementService.createBatchStatisticsQuery()
      .singleResult();

    assertEquals(3, batchStatistics.getTotalJobs());
    assertEquals(3, batchStatistics.getJobsCreated());
    assertEquals(2, batchStatistics.getRemainingJobs());
    assertEquals(1, batchStatistics.getCompletedJobs());
    assertEquals(1, batchStatistics.getFailedJobs());
  }

  @Test
  public void testStatisticsRetriedFailedJobs() {
    // given
    Batch batch = helper.createMigrationBatchWithSize(3);

    // when
    helper.completeSeedJobs(batch);
    helper.failExecutionJobs(batch, 3);

    // then
    BatchStatistics batchStatistics = managementService.createBatchStatisticsQuery()
      .singleResult();

    assertEquals(3, batchStatistics.getTotalJobs());
    assertEquals(3, batchStatistics.getJobsCreated());
    assertEquals(3, batchStatistics.getRemainingJobs());
    assertEquals(0, batchStatistics.getCompletedJobs());
    assertEquals(3, batchStatistics.getFailedJobs());

    // when
    helper.setRetries(batch, 3, 1);
    helper.completeJobs(batch, 3);

    // then
    batchStatistics = managementService.createBatchStatisticsQuery()
      .singleResult();

    assertEquals(3, batchStatistics.getTotalJobs());
    assertEquals(3, batchStatistics.getJobsCreated());
    assertEquals(0, batchStatistics.getRemainingJobs());
    assertEquals(3, batchStatistics.getCompletedJobs());
    assertEquals(0, batchStatistics.getFailedJobs());
  }

  @Test
  public void testStatisticsWithDeletedJobs() {
    // given
    Batch batch = helper.createMigrationBatchWithSize(13);

    // when
    helper.executeJob(helper.getSeedJob(batch));
    deleteMigrationJobs(batch);

    // then
    BatchStatistics batchStatistics = managementService.createBatchStatisticsQuery()
      .singleResult();

    assertEquals(13, batchStatistics.getTotalJobs());
    assertEquals(10, batchStatistics.getJobsCreated());
    assertEquals(3, batchStatistics.getRemainingJobs());
    assertEquals(10, batchStatistics.getCompletedJobs());
    assertEquals(0, batchStatistics.getFailedJobs());
  }

  @Test
  public void testStatisticsWithNotAllGeneratedAndAlreadyCompletedAndFailedJobs() {
    // given
    Batch batch = helper.createMigrationBatchWithSize(13);

    // when
    helper.executeJob(helper.getSeedJob(batch));
    helper.completeJobs(batch, 2);
    helper.failExecutionJobs(batch, 2);

    // then
    BatchStatistics batchStatistics = managementService.createBatchStatisticsQuery()
      .singleResult();

    assertEquals(13, batchStatistics.getTotalJobs());
    assertEquals(10, batchStatistics.getJobsCreated());
    assertEquals(11, batchStatistics.getRemainingJobs());
    assertEquals(2, batchStatistics.getCompletedJobs());
    assertEquals(2, batchStatistics.getFailedJobs());
  }

  @Test
  public void testMultipleBatchesStatistics() {
    // given
    Batch batch1 = helper.createMigrationBatchWithSize(3);
    Batch batch2 = helper.createMigrationBatchWithSize(13);
    Batch batch3 = helper.createMigrationBatchWithSize(15);

    // when
    helper.executeJob(helper.getSeedJob(batch2));
    helper.completeJobs(batch2, 2);
    helper.failExecutionJobs(batch2, 3);

    helper.executeJob(helper.getSeedJob(batch3));
    deleteMigrationJobs(batch3);
    helper.executeJob(helper.getSeedJob(batch3));
    helper.completeJobs(batch3, 2);
    helper.failExecutionJobs(batch3, 3);

    // then
    List<BatchStatistics> batchStatisticsList = managementService.createBatchStatisticsQuery()
      .list();

    for (BatchStatistics batchStatistics : batchStatisticsList) {
      if (batch1.getId().equals(batchStatistics.getId())) {
        // batch 1
        assertEquals(3, batchStatistics.getTotalJobs());
        assertEquals(0, batchStatistics.getJobsCreated());
        assertEquals(3, batchStatistics.getRemainingJobs());
        assertEquals(0, batchStatistics.getCompletedJobs());
        assertEquals(0, batchStatistics.getFailedJobs());
      }
      else if (batch2.getId().equals(batchStatistics.getId())) {
        // batch 2
        assertEquals(13, batchStatistics.getTotalJobs());
        assertEquals(10, batchStatistics.getJobsCreated());
        assertEquals(11, batchStatistics.getRemainingJobs());
        assertEquals(2, batchStatistics.getCompletedJobs());
        assertEquals(3, batchStatistics.getFailedJobs());
      }
      else if (batch3.getId().equals(batchStatistics.getId())) {
        // batch 3
        assertEquals(15, batchStatistics.getTotalJobs());
        assertEquals(15, batchStatistics.getJobsCreated());
        assertEquals(3, batchStatistics.getRemainingJobs());
        assertEquals(12, batchStatistics.getCompletedJobs());
        assertEquals(3, batchStatistics.getFailedJobs());
      }
    }
  }

  @Test
  public void testStatisticsSuspend() {
    // given
    Batch batch = helper.migrateProcessInstancesAsync(1);

    // when
    managementService.suspendBatchById(batch.getId());

    // then
    BatchStatistics batchStatistics = managementService.createBatchStatisticsQuery().batchId(batch.getId()).singleResult();

    assertTrue(batchStatistics.isSuspended());
  }

  @Test
  public void testStatisticsActivate() {
    // given
    Batch batch = helper.migrateProcessInstancesAsync(1);
    managementService.suspendBatchById(batch.getId());

    // when
    managementService.activateBatchById(batch.getId());

    // then
    BatchStatistics batchStatistics = managementService.createBatchStatisticsQuery().batchId(batch.getId()).singleResult();

    assertFalse(batchStatistics.isSuspended());
  }

  @Test
  public void testStatisticsQueryBySuspendedBatches() {
    // given
    Batch batch1 = helper.migrateProcessInstancesAsync(1);
    Batch batch2 = helper.migrateProcessInstancesAsync(1);
    helper.migrateProcessInstancesAsync(1);

    // when
    managementService.suspendBatchById(batch1.getId());
    managementService.suspendBatchById(batch2.getId());
    managementService.activateBatchById(batch1.getId());

    // then
    BatchStatisticsQuery query = managementService.createBatchStatisticsQuery().suspended();
    Assert.assertEquals(1, query.count());
    Assert.assertEquals(1, query.list().size());
    Assert.assertEquals(batch2.getId(), query.singleResult().getId());
  }

  @Test
  public void testStatisticsQueryByActiveBatches() {
    // given
    Batch batch1 = helper.migrateProcessInstancesAsync(1);
    Batch batch2 = helper.migrateProcessInstancesAsync(1);
    Batch batch3 = helper.migrateProcessInstancesAsync(1);

    // when
    managementService.suspendBatchById(batch1.getId());
    managementService.suspendBatchById(batch2.getId());
    managementService.activateBatchById(batch1.getId());

    // then
    BatchStatisticsQuery query = managementService.createBatchStatisticsQuery().active();
    Assert.assertEquals(2, query.count());
    Assert.assertEquals(2, query.list().size());

    List<String> foundIds = new ArrayList<String>();
    for (Batch batch : query.list()) {
      foundIds.add(batch.getId());
    }
    assertThat(foundIds).contains(
      batch1.getId(),
      batch3.getId()
    );
  }

  @Test
  public void shouldQueryByFailures() {
    // given
    Batch batch1 = helper.migrateProcessInstancesAsync(2);
    Batch batch2 = helper.migrateProcessInstancesAsync(1);
    helper.executeSeedJob(batch1);
    helper.executeSeedJob(batch2);
    helper.failExecutionJobs(batch1, 1);

    // when
    final long total = managementService.createBatchStatisticsQuery().count();
    BatchStatisticsQuery queryWithFailures = managementService.createBatchStatisticsQuery().withFailures();
    BatchStatisticsQuery queryWithoutFailures = managementService.createBatchStatisticsQuery().withoutFailures();

    // then
    assertThat(total).isEqualTo(2);

    final List<BatchStatistics> batchWithFailures = queryWithFailures.list();
    assertThat(batchWithFailures).hasSize(1);
    assertThat(queryWithFailures.count()).isEqualTo(1);
    final BatchStatistics batch1Statistics = batchWithFailures.get(0);
    assertThat(batch1Statistics.getId()).isEqualTo(batch1.getId());
    assertThat(batch1Statistics.getFailedJobs()).isEqualTo(1);
    assertThat(batch1Statistics.getTotalJobs()).isEqualTo(2);

    final List<BatchStatistics> batchWithoutFailures = queryWithoutFailures.list();
    assertThat(batchWithoutFailures).hasSize(1);
    assertThat(queryWithoutFailures.count()).isEqualTo(1);
    final BatchStatistics batch2Statistics = batchWithoutFailures.get(0);
    assertThat(batch2Statistics.getId()).isEqualTo(batch2.getId());
    assertThat(batch2Statistics.getFailedJobs()).isZero();
    assertThat(batch2Statistics.getTotalJobs()).isEqualTo(1);
  }

  @Test
  public void shouldQueryByStartedAfter() {
    // given
    final long oneMin = 60 * 1000L;
    ClockUtil.setCurrentTime(ClockUtil.getCurrentTime());
    final Date oneMinLater = new Date(ClockUtil.getCurrentTime().getTime() + oneMin);
    final Date oneMinEarlier= new Date(ClockUtil.getCurrentTime().getTime() - oneMin);
    final Batch batch = helper.migrateProcessInstancesAsync(1);

    // when
    BatchStatisticsQuery query1 = managementService.createBatchStatisticsQuery().startedAfter(oneMinEarlier);
    BatchStatisticsQuery query2 = managementService.createBatchStatisticsQuery().startedAfter(oneMinLater);

    // then
    final List<BatchStatistics> batchStatistics = query1.list();
    assertThat(batchStatistics.get(0).getId()).isEqualTo(batch.getId());
    assertThat(batchStatistics.get(0).getStartTime()).isEqualToIgnoringMillis(ClockUtil.getCurrentTime());
    assertThat(batchStatistics).hasSize(1);
    assertThat(query1.count()).isEqualTo(1);

    assertThat(query2.count()).isEqualTo(0);
    assertThat(query2.list()).hasSize(0);
  }

  @Test
  public void shouldQueryByStartedBefore() {
    // given
    final long oneMin = 60 * 1000L;
    ClockUtil.setCurrentTime(ClockUtil.getCurrentTime());
    final Date oneMinLater = new Date(ClockUtil.getCurrentTime().getTime() + oneMin);
    final Date oneMinEarlier= new Date(ClockUtil.getCurrentTime().getTime() - oneMin);
    final Batch batch = helper.migrateProcessInstancesAsync(1);

    // when
    BatchStatisticsQuery query1 = managementService.createBatchStatisticsQuery().startedBefore(oneMinEarlier);
    BatchStatisticsQuery query2 = managementService.createBatchStatisticsQuery().startedBefore(oneMinLater);

    // then
    assertThat(query1.count()).isEqualTo(0);
    assertThat(query1.list()).hasSize(0);

    final List<BatchStatistics> batchStatistics = query2.list();
    assertThat(batchStatistics.get(0).getId()).isEqualTo(batch.getId());
    assertThat(batchStatistics.get(0).getStartTime()).isEqualToIgnoringMillis(ClockUtil.getCurrentTime());
    assertThat(batchStatistics).hasSize(1);
    assertThat(query2.count()).isEqualTo(1);
  }

  @Test
  public void shouldQueryByCreatedBy() {
    // given
    engineRule.getIdentityService().setAuthenticatedUserId("user1");
    final Batch batch1 = helper.migrateProcessInstancesAsync(1);
    engineRule.getIdentityService().setAuthenticatedUserId("user2");
    final Batch batch2 = helper.migrateProcessInstancesAsync(1);

    // when
    BatchStatisticsQuery query1 = managementService.createBatchStatisticsQuery().createdBy("user1");
    BatchStatisticsQuery query2 = managementService.createBatchStatisticsQuery().createdBy("user2");
    BatchStatisticsQuery query3 = managementService.createBatchStatisticsQuery().createdBy("user3");

    // then
    final BatchStatistics user1Batch = query1.list().get(0);
    assertThat(user1Batch.getId()).isEqualTo(batch1.getId());
    assertThat(user1Batch.getCreateUserId()).isEqualTo(batch1.getCreateUserId());
    assertThat(query1.list()).hasSize(1);
    assertThat(query1.count()).isEqualTo(1);

    final BatchStatistics user2Batch = query2.list().get(0);
    assertThat(user2Batch.getId()).isEqualTo(batch2.getId());
    assertThat(user2Batch.getCreateUserId()).isEqualTo(batch2.getCreateUserId());
    assertThat(query2.list()).hasSize(1);
    assertThat(query2.count()).isEqualTo(1);

    assertThat(query3.list()).hasSize(0);
    assertThat(query3.count()).isEqualTo(0);
  }

  protected void deleteMigrationJobs(Batch batch) {
    for (Job migrationJob: helper.getExecutionJobs(batch)) {
      managementService.deleteJob(migrationJob.getId());
    }
  }

}
