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
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.impl.batch.BatchEntity;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricIncidentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricJobLogEventEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.management.Metrics;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.runtime.BatchModificationHelper;
import org.camunda.bpm.engine.test.api.runtime.migration.MigrationTestRule;
import org.camunda.bpm.engine.test.api.runtime.migration.batch.BatchMigrationHelper;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoryCleanupHistoricBatchTest {

  public ProcessEngineRule engineRule = new ProcessEngineRule(true);
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);
  protected MigrationTestRule migrationRule = new MigrationTestRule(engineRule);
  protected BatchMigrationHelper migrationHelper = new BatchMigrationHelper(engineRule, migrationRule);
  protected BatchModificationHelper modificationHelper = new BatchModificationHelper(engineRule);

  private static final String DEFAULT_TTL_DAYS = "P5D";

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule).around(migrationRule);

  protected RuntimeService runtimeService;
  protected HistoryService historyService;
  protected ManagementService managementService;
  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
    historyService = engineRule.getHistoryService();
    managementService = engineRule.getManagementService();
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
  }

  @After
  public void clearDatabase() {
    migrationHelper.removeAllRunningAndHistoricBatches();

    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {

        List<Job> jobs = managementService.createJobQuery().list();
        if (jobs.size() > 0) {
          assertEquals(1, jobs.size());
          String jobId = jobs.get(0).getId();
          commandContext.getJobManager().deleteJob((JobEntity) jobs.get(0));
          commandContext.getHistoricJobLogManager().deleteHistoricJobLogByJobId(jobId);
        }

        List<HistoricIncident> historicIncidents = historyService.createHistoricIncidentQuery().list();
        for (HistoricIncident historicIncident : historicIncidents) {
          commandContext.getDbEntityManager().delete((HistoricIncidentEntity) historicIncident);
        }

        commandContext.getMeterLogManager().deleteAll();

        return null;
      }
    });
  }

  @After
  public void resetConfiguration() {
    processEngineConfiguration.setBatchOperationHistoryTimeToLive(null);
    processEngineConfiguration.setBatchOperationsForHistoryCleanup(null);
  }

  @Test
  public void testCleanupHistoricBatch() {
    initBatchOperationHistoryTimeToLive(DEFAULT_TTL_DAYS);
    int daysInThePast = -11;

    // given
    prepareHistoricBatches(3, daysInThePast);
    List<HistoricBatch> historicList = historyService.createHistoricBatchQuery().list();
    assertEquals(3, historicList.size());

    // when
    String jobId = historyService.cleanUpHistoryAsync(true).getId();

    managementService.executeJob(jobId);

    // then
    assertEquals(0, historyService.createHistoricBatchQuery().count());
  }

  @Test
  public void testCleanupHistoricJobLog() {
    initBatchOperationHistoryTimeToLive(DEFAULT_TTL_DAYS);
    int daysInThePast = -11;

    // given
    prepareHistoricBatches(1, daysInThePast);
    HistoricBatch batch = historyService.createHistoricBatchQuery().singleResult();
    String batchId = batch.getId();

    // when
    String jobId = historyService.cleanUpHistoryAsync(true).getId();

    managementService.executeJob(jobId);

    // then
    assertEquals(0, historyService.createHistoricBatchQuery().count());
    assertEquals(0, historyService.createHistoricJobLogQuery().jobDefinitionConfiguration(batchId).count());
  }

  @Test
  public void testCleanupHistoricIncident() {
    initBatchOperationHistoryTimeToLive(DEFAULT_TTL_DAYS);
    ClockUtil.setCurrentTime(DateUtils.addDays(new Date(), -11));

    BatchEntity batch = (BatchEntity) createFailingMigrationBatch();

    migrationHelper.executeSeedJob(batch);

    List<Job> list = managementService.createJobQuery().list();
    for (Job job : list) {
      if (((JobEntity) job).getJobHandlerType().equals("instance-migration")) {
        managementService.setJobRetries(job.getId(), 1);
      }
    }
    migrationHelper.executeJobs(batch);

    List<String> byteArrayIds = findExceptionByteArrayIds();

    ClockUtil.setCurrentTime(DateUtils.addDays(new Date(), -10));
    managementService.deleteBatch(batch.getId(), false);
    ClockUtil.setCurrentTime(new Date());

    // given
    HistoricBatch historicBatch = historyService.createHistoricBatchQuery().singleResult();
    String batchId = historicBatch.getId();

    // when
    String jobId = historyService.cleanUpHistoryAsync(true).getId();

    managementService.executeJob(jobId);

    assertEquals(0, historyService.createHistoricBatchQuery().count());
    assertEquals(0, historyService.createHistoricJobLogQuery().jobDefinitionConfiguration(batchId).count());
    assertEquals(0, historyService.createHistoricIncidentQuery().count());
    verifyByteArraysWereRemoved(byteArrayIds.toArray(new String[] {}));
  }

  @Test
  public void testHistoryCleanupBatchMetrics() {
    initBatchOperationHistoryTimeToLive(DEFAULT_TTL_DAYS);
    // given
    int daysInThePast = -11;
    int batchesCount = 5;
    prepareHistoricBatches(batchesCount, daysInThePast);

    // when
    String jobId = historyService.cleanUpHistoryAsync(true).getId();

    managementService.executeJob(jobId);

    // then
    final long removedBatches = managementService.createMetricsQuery().name(Metrics.HISTORY_CLEANUP_REMOVED_BATCH_INSTANCES).sum();

    assertEquals(batchesCount, removedBatches);
  }

  @Test
  public void testBatchOperationTypeConfigurationOnly() {
    Map<String, String> map = new HashMap<String, String>();
    map.put("instance-migration", "P2D");
    map.put("instance-deletion", DEFAULT_TTL_DAYS);
    processEngineConfiguration.setBatchOperationHistoryTimeToLive(null);
    processEngineConfiguration.setBatchOperationsForHistoryCleanup(map);
    processEngineConfiguration.initHistoryCleanup();

    assertNull(processEngineConfiguration.getBatchOperationHistoryTimeToLive());

    Date startDate = ClockUtil.getCurrentTime();
    int daysInThePast = -11;
    ClockUtil.setCurrentTime(DateUtils.addDays(startDate, daysInThePast));

    List<String> batchIds = new ArrayList<String>();

    int migrationCountBatch = 10;
    batchIds.addAll(createMigrationBatchList(migrationCountBatch));

    int cancelationCountBatch = 20;
    batchIds.addAll(createCancelationBatchList(cancelationCountBatch));

    ClockUtil.setCurrentTime(DateUtils.addDays(startDate, -7));

    for (String batchId : batchIds) {
      managementService.deleteBatch(batchId, false);
    }

    ClockUtil.setCurrentTime(new Date());

    // when
    List<HistoricBatch> historicList = historyService.createHistoricBatchQuery().list();
    assertEquals(30, historicList.size());
    String jobId = historyService.cleanUpHistoryAsync(true).getId();

    managementService.executeJob(jobId);

    // then
    assertEquals(0,  historyService.createHistoricBatchQuery().count());
    for (String batchId : batchIds) {
      assertEquals(0, historyService.createHistoricJobLogQuery().jobDefinitionConfiguration(batchId).count());
    }
  }

  @Test
  public void testMixedConfiguration() {
    Map<String, String> map = new HashMap<String, String>();
    map.put("instance-modification", "P20D");
    processEngineConfiguration.setBatchOperationHistoryTimeToLive(DEFAULT_TTL_DAYS);
    processEngineConfiguration.setBatchOperationsForHistoryCleanup(map);
    processEngineConfiguration.initHistoryCleanup();

    Date startDate = ClockUtil.getCurrentTime();
    int daysInThePast = -11;
    ClockUtil.setCurrentTime(DateUtils.addDays(startDate, daysInThePast));

    Batch modificationBatch = createModificationBatch();
    List<String> batchIds = new ArrayList<String>();
    batchIds.add(modificationBatch.getId());

    int migrationCountBatch = 10;
    batchIds.addAll(createMigrationBatchList(migrationCountBatch));

    int cancelationCountBatch = 20;
    batchIds.addAll(createCancelationBatchList(cancelationCountBatch));

    ClockUtil.setCurrentTime(DateUtils.addDays(startDate, -8));

    for (String batchId : batchIds) {
      managementService.deleteBatch(batchId, false);
    }

    ClockUtil.setCurrentTime(new Date());

    // when
    List<HistoricBatch> historicList = historyService.createHistoricBatchQuery().list();
    assertEquals(31, historicList.size());
    String jobId = historyService.cleanUpHistoryAsync(true).getId();

    managementService.executeJob(jobId);

    // then
    HistoricBatch modificationHistoricBatch = historyService.createHistoricBatchQuery().singleResult(); // the other batches should be cleaned
    assertEquals(modificationBatch.getId(), modificationHistoricBatch.getId());
    assertEquals(2, historyService.createHistoricJobLogQuery().jobDefinitionConfiguration(modificationBatch.getId()).count());
    batchIds.remove(modificationBatch.getId());
    for (String batchId : batchIds) {
      assertEquals(0, historyService.createHistoricJobLogQuery().jobDefinitionConfiguration(batchId).count());
    }
  }

  @Test
  public void testWrongGlobalConfiguration() {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid value");
    processEngineConfiguration.setBatchOperationHistoryTimeToLive("PD");
    processEngineConfiguration.initHistoryCleanup();
  }

  @Test
  public void testWrongSpecificConfiguration() {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid value");
    Map<String, String> map = new HashMap<String, String>();
    map.put("instance-modification", "PD");
    processEngineConfiguration.setBatchOperationHistoryTimeToLive("P5D");
    processEngineConfiguration.setBatchOperationsForHistoryCleanup(map);
    processEngineConfiguration.initHistoryCleanup();
  }

  @Test
  public void testWrongGlobalConfigurationNegativeTTL() {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid value");
    processEngineConfiguration.setBatchOperationHistoryTimeToLive("P-1D");
    processEngineConfiguration.initHistoryCleanup();
  }

  @Test
  public void testWrongSpecificConfigurationNegativeTTL() {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid value");
    Map<String, String> map = new HashMap<String, String>();
    map.put("instance-modification", "P-5D");
    processEngineConfiguration.setBatchOperationHistoryTimeToLive("P5D");
    processEngineConfiguration.setBatchOperationsForHistoryCleanup(map);
    processEngineConfiguration.initHistoryCleanup();
  }

  private void initBatchOperationHistoryTimeToLive(String days) {
    processEngineConfiguration.setBatchOperationHistoryTimeToLive(days);
    processEngineConfiguration.initHistoryCleanup();
  }

  private BpmnModelInstance createModelInstance() {
    BpmnModelInstance instance = Bpmn.createExecutableProcess("process")
        .startEvent("start")
        .userTask("userTask1")
        .sequenceFlowId("seq")
        .userTask("userTask2")
        .endEvent("end")
        .done();
    return instance;
  }

  private void prepareHistoricBatches(int batchesCount, int daysInThePast) {
    Date startDate = ClockUtil.getCurrentTime();
    ClockUtil.setCurrentTime(DateUtils.addDays(startDate, daysInThePast));

    List<Batch> list = new ArrayList<Batch>();
    for (int i = 0; i < batchesCount; i++) {
      list.add(migrationHelper.migrateProcessInstancesAsync(1));
    }

    for (Batch batch : list) {
      migrationHelper.executeSeedJob(batch);
      migrationHelper.executeJobs(batch);

      ClockUtil.setCurrentTime(DateUtils.addDays(startDate, ++daysInThePast));
      migrationHelper.executeMonitorJob(batch);
    }

    ClockUtil.setCurrentTime(new Date());
  }

  private Batch createFailingMigrationBatch() {
    BpmnModelInstance instance = createModelInstance();

    ProcessDefinition sourceProcessDefinition = migrationRule.deployAndGetDefinition(instance);
    ProcessDefinition targetProcessDefinition = migrationRule.deployAndGetDefinition(instance);

    MigrationPlan migrationPlan = runtimeService
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapEqualActivities()
        .build();

    ProcessInstance processInstance = runtimeService.startProcessInstanceById(sourceProcessDefinition.getId());

    Batch batch = runtimeService.newMigration(migrationPlan).processInstanceIds(Arrays.asList(processInstance.getId(), "unknownId")).executeAsync();
    return batch;
  }

  private List<String> createMigrationBatchList(int migrationCountBatch) {
    List<String> batchIds = new ArrayList<String>();
    for (int i = 0; i < migrationCountBatch; i++) {
      batchIds.add(migrationHelper.migrateProcessInstancesAsync(1).getId());
    }
    return batchIds;
  }

  private Batch createModificationBatch() {
    BpmnModelInstance instance = createModelInstance();
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    Batch modificationBatch = modificationHelper.startAfterAsync("process", 1, "userTask1", processDefinition.getId());
    return modificationBatch;
  }

  private List<String> createCancelationBatchList(int cancelationCountBatch) {
    List<String> batchIds = new ArrayList<String>();
    for (int i = 0; i < cancelationCountBatch; i++) {
      batchIds.add(runtimeService.deleteProcessInstancesAsync(Arrays.asList("unknownId"), "create-deletion-batch").getId());
    }
    return batchIds;
  }

  private void verifyByteArraysWereRemoved(final String... errorDetailsByteArrayIds) {
    engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        for (String errorDetailsByteArrayId : errorDetailsByteArrayIds) {
          assertNull(commandContext.getDbEntityManager().selectOne("selectByteArray", errorDetailsByteArrayId));
        }
        return null;
      }
    });
  }

  private List<String> findExceptionByteArrayIds() {
    List<String> exceptionByteArrayIds = new ArrayList<String>();
    List<HistoricJobLog> historicJobLogs = historyService.createHistoricJobLogQuery().list();
    for (HistoricJobLog historicJobLog : historicJobLogs) {
      HistoricJobLogEventEntity historicJobLogEventEntity = (HistoricJobLogEventEntity) historicJobLog;
      if (historicJobLogEventEntity.getExceptionByteArrayId() != null) {
        exceptionByteArrayIds.add(historicJobLogEventEntity.getExceptionByteArrayId());
      }
    }
    return exceptionByteArrayIds;
  }

}
