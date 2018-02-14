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
package org.camunda.bpm.engine.test.api.authorization.batch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.history.CleanableHistoricBatchReportResult;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationTestBaseRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

/**
 * @author Thorben Lindhauer
 *
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoricBatchQueryAuthorizationTest {

  public ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule();
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  public AuthorizationTestBaseRule authRule = new AuthorizationTestBaseRule(engineRule);
  public ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(bootstrapRule).around(engineRule).around(authRule).around(testHelper);

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  protected MigrationPlan migrationPlan;
  protected Batch batch1;
  protected Batch batch2;

  @Before
  public void setUp() {
    authRule.createUserAndGroup("user", "group");
  }

  @Before
  public void deployProcessesAndCreateMigrationPlan() {
    ProcessDefinition sourceDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    migrationPlan = engineRule.getRuntimeService().createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
      .mapEqualActivities()
      .build();

    ProcessInstance pi = engineRule.getRuntimeService().startProcessInstanceById(sourceDefinition.getId());

    batch1 = engineRule.getRuntimeService()
      .newMigration(migrationPlan)
      .processInstanceIds(Arrays.asList(pi.getId()))
      .executeAsync();

    batch2 = engineRule.getRuntimeService()
        .newMigration(migrationPlan)
        .processInstanceIds(Arrays.asList(pi.getId()))
        .executeAsync();
  }

  @After
  public void tearDown() {
    authRule.deleteUsersAndGroups();
    removeAllRunningAndHistoricBatches();
    engineRule.getProcessEngineConfiguration().setBatchOperationHistoryTimeToLive(null);
    engineRule.getProcessEngineConfiguration().setBatchOperationsForHistoryCleanup(null);
  }

  private void removeAllRunningAndHistoricBatches() {
    HistoryService historyService = engineRule.getHistoryService();
    ManagementService managementService = engineRule.getManagementService();

    for (Batch batch : managementService.createBatchQuery().list()) {
      managementService.deleteBatch(batch.getId(), true);
    }

    // remove history of completed batches
    for (HistoricBatch historicBatch : historyService.createHistoricBatchQuery().list()) {
      historyService.deleteHistoricBatch(historicBatch.getId());
    }
  }

  @Test
  public void testQueryList() {
    // given
    authRule.createGrantAuthorization(Resources.BATCH, batch1.getId(), "user", Permissions.READ_HISTORY);

    // when
    authRule.enableAuthorization("user");
    List<HistoricBatch> batches = engineRule.getHistoryService().createHistoricBatchQuery().list();
    authRule.disableAuthorization();

    // then
    Assert.assertEquals(1, batches.size());
    Assert.assertEquals(batch1.getId(), batches.get(0).getId());
  }

  @Test
  public void testQueryCount() {
    // given
    authRule.createGrantAuthorization(Resources.BATCH, batch1.getId(), "user", Permissions.READ_HISTORY);

    // when
    authRule.enableAuthorization("user");
    long count = engineRule.getHistoryService().createHistoricBatchQuery().count();
    authRule.disableAuthorization();

    // then
    Assert.assertEquals(1, count);
  }

  @Test
  public void testQueryNoAuthorizations() {
    // when
    authRule.enableAuthorization("user");
    long count = engineRule.getHistoryService().createHistoricBatchQuery().count();
    authRule.disableAuthorization();

    // then
    Assert.assertEquals(0, count);
  }

  @Test
  public void testQueryListAccessAll() {
    // given
    authRule.createGrantAuthorization(Resources.BATCH, "*", "user", Permissions.READ_HISTORY);

    // when
    authRule.enableAuthorization("user");
    List<HistoricBatch> batches = engineRule.getHistoryService().createHistoricBatchQuery().list();
    authRule.disableAuthorization();

    // then
    Assert.assertEquals(2, batches.size());
  }

  @Test
  public void testQueryListMultiple() {
    // given
    authRule.createGrantAuthorization(Resources.BATCH, "*", "user", Permissions.READ_HISTORY);
    authRule.createGrantAuthorization(Resources.BATCH, batch1.getId(), "user", Permissions.READ_HISTORY);

    // when
    authRule.enableAuthorization("user");
    List<HistoricBatch> batches = engineRule.getHistoryService().createHistoricBatchQuery().list();
    authRule.disableAuthorization();

    // then
    Assert.assertEquals(2, batches.size());
  }

  @Test
  public void testHistoryCleanupReportQueryWithPermissions() {
    // given
    authRule.createGrantAuthorization(Resources.BATCH, "*", "user", Permissions.READ_HISTORY);
    String migrationOperationsTTL = "P0D";
    prepareBatch(migrationOperationsTTL);

    authRule.enableAuthorization("user");
    CleanableHistoricBatchReportResult result = engineRule.getHistoryService().createCleanableHistoricBatchReport().singleResult();
    authRule.disableAuthorization();

    assertNotNull(result);
    checkResultNumbers(result, 1, 1, 0);
  }

  @Test
  public void testHistoryCleanupReportQueryWithoutPermission() {
    // given
    String migrationOperationsTTL = "P0D";
    prepareBatch(migrationOperationsTTL);
    // then
    thrown.expect(AuthorizationException.class);

    authRule.enableAuthorization("user");
    try {
      // when
      engineRule.getHistoryService().createCleanableHistoricBatchReport().list();
    } finally {
      authRule.disableAuthorization();
    }
  }

  private void prepareBatch(String migrationOperationsTTL) {
    engineRule.getProcessEngineConfiguration().setAuthorizationEnabled(false);
    Map<String, String> map = new HashMap<String, String>();
    map.put("instance-migration", migrationOperationsTTL);
    engineRule.getProcessEngineConfiguration().setBatchOperationsForHistoryCleanup(map);
    engineRule.getProcessEngineConfiguration().initHistoryCleanup();

    Date startDate = ClockUtil.getCurrentTime();
    ClockUtil.setCurrentTime(DateUtils.addDays(startDate, -11));
    String batchId = createBatch();
    ClockUtil.setCurrentTime(DateUtils.addDays(startDate, -7));

    engineRule.getManagementService().deleteBatch(batchId, false);

    engineRule.getProcessEngineConfiguration().setAuthorizationEnabled(true);
  }

  private void checkResultNumbers(CleanableHistoricBatchReportResult result, int expectedCleanable, int expectedFinished, Integer expectedTTL) {
    assertEquals(expectedCleanable, result.getCleanableBatchesCount());
    assertEquals(expectedFinished, result.getFinishedBatchesCount());
    assertEquals(expectedTTL, result.getHistoryTimeToLive());
  }


  private String createBatch() {
    ProcessDefinition sourceDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan plan = engineRule.getRuntimeService().createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
      .mapEqualActivities()
      .build();

    ProcessInstance pi = engineRule.getRuntimeService().startProcessInstanceById(sourceDefinition.getId());

     Batch batch = engineRule.getRuntimeService()
      .newMigration(plan)
      .processInstanceIds(Arrays.asList(pi.getId()))
      .executeAsync();

     return batch.getId();
  }
}
