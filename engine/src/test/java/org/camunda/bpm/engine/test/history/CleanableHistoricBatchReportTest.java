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
package org.camunda.bpm.engine.test.history;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.history.CleanableHistoricBatchReportResult;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.runtime.BatchModificationHelper;
import org.camunda.bpm.engine.test.api.runtime.migration.MigrationTestRule;
import org.camunda.bpm.engine.test.api.runtime.migration.batch.BatchMigrationHelper;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class CleanableHistoricBatchReportTest {

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule();

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);
  protected MigrationTestRule migrationRule = new MigrationTestRule(engineRule);
  protected BatchMigrationHelper migrationHelper = new BatchMigrationHelper(engineRule, migrationRule);
  protected BatchModificationHelper modificationHelper = new BatchModificationHelper(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(testRule).around(engineRule).around(migrationRule);

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected HistoryService historyService;
  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;
  protected ManagementService managementService;

  @Before
  public void setUp() {
    historyService = engineRule.getHistoryService();
    processEngineConfiguration = (ProcessEngineConfigurationImpl)bootstrapRule.getProcessEngine().getProcessEngineConfiguration();
    repositoryService = engineRule.getRepositoryService();
    runtimeService = engineRule.getRuntimeService();
    managementService = engineRule.getManagementService();
  }

  @After
  public void cleanUp() {
    ClockUtil.reset();
    migrationHelper.removeAllRunningAndHistoricBatches();
    processEngineConfiguration.setBatchOperationHistoryTimeToLive(null);
    processEngineConfiguration.setBatchOperationsForHistoryCleanup(null);
  }

  @Test
  public void testReportMixedConfiguration() {
    Map<String, String> map = new HashMap<>();
    int modOperationsTTL = 20;
    map.put("instance-modification", "P20D");
    int defaultTTL = 5;
    processEngineConfiguration.setBatchOperationHistoryTimeToLive("P5D");
    processEngineConfiguration.setBatchOperationsForHistoryCleanup(map);
    processEngineConfiguration.initHistoryCleanup();

    Date startDate = new Date();
    int daysInThePast = -11;
    ClockUtil.setCurrentTime(DateUtils.addDays(startDate, daysInThePast));

    Batch modificationBatch = createModificationBatch();
    List<String> batchIds = new ArrayList<>();
    batchIds.add(modificationBatch.getId());

    int migrationCountBatch = 10;
    List<String> batchIds1 = new ArrayList<>();
    batchIds1.addAll(createMigrationBatchList(migrationCountBatch));

    int cancelationCountBatch = 20;
    List<String> batchIds2 = new ArrayList<>();
    batchIds2.addAll(createCancelationBatchList(cancelationCountBatch));


    ClockUtil.setCurrentTime(DateUtils.addDays(startDate, -8));

    for (String batchId : batchIds) {
      managementService.deleteBatch(batchId, false);
    }

    ClockUtil.setCurrentTime(DateUtils.addDays(startDate, -2));

    for (int i = 0; i < 4; i++) {
      managementService.deleteBatch(batchIds1.get(i), false);
    }
    ClockUtil.setCurrentTime(DateUtils.addDays(startDate, -7));
    for (int i = 6; i < batchIds1.size(); i++) {
      managementService.deleteBatch(batchIds1.get(i), false);
    }

    ClockUtil.setCurrentTime(DateUtils.addDays(startDate, -10));
    for (int i = 0; i < 7; i++) {
      managementService.deleteBatch(batchIds2.get(i), false);
    }
    ClockUtil.setCurrentTime(DateUtils.addDays(startDate, -5));
    for (int i = 7; i < 11; i++) {
      managementService.deleteBatch(batchIds2.get(i), false);
    }
    ClockUtil.setCurrentTime(DateUtils.addDays(startDate, -1));
    for (int i = 13; i < batchIds2.size(); i++) {
      managementService.deleteBatch(batchIds2.get(i), false);
    }

    ClockUtil.setCurrentTime(DateUtils.addSeconds(startDate, 1));

    // when
    List<HistoricBatch> historicList = historyService.createHistoricBatchQuery().list();
    assertEquals(31, historicList.size());

    List<CleanableHistoricBatchReportResult> list = historyService.createCleanableHistoricBatchReport().list();
    assertEquals(3, list.size());
    for (CleanableHistoricBatchReportResult result : list) {
      if (result.getBatchType().equals("instance-migration")) {
        checkResultNumbers(result, 4, 8, defaultTTL);
      } else if (result.getBatchType().equals("instance-modification")) {
        checkResultNumbers(result, 0, 1, modOperationsTTL);
      } else if (result.getBatchType().equals("instance-deletion")) {
        checkResultNumbers(result, 11, 18, defaultTTL);
      }
    }
  }

  @Test
  public void testReportNoDefaultConfiguration() {
    Map<String, String> map = new HashMap<>();
    int modOperationsTTL = 5;
    map.put("instance-modification", "P5D");
    int delOperationsTTL = 7;
    map.put("instance-deletion", "P7D");
    processEngineConfiguration.setBatchOperationsForHistoryCleanup(map);
    processEngineConfiguration.initHistoryCleanup();
    assertNull(processEngineConfiguration.getBatchOperationHistoryTimeToLive());

    Date startDate = new Date();
    int daysInThePast = -11;
    ClockUtil.setCurrentTime(DateUtils.addDays(startDate, daysInThePast));

    Batch modificationBatch = createModificationBatch();
    List<String> batchIds = new ArrayList<>();
    batchIds.add(modificationBatch.getId());

    int migrationCountBatch = 10;
    List<String> batchIds1 = new ArrayList<>();
    batchIds1.addAll(createMigrationBatchList(migrationCountBatch));

    int cancelationCountBatch = 20;
    List<String> batchIds2 = new ArrayList<>();
    batchIds2.addAll(createCancelationBatchList(cancelationCountBatch));


    ClockUtil.setCurrentTime(DateUtils.addDays(startDate, -8));

    for (String batchId : batchIds) {
      managementService.deleteBatch(batchId, false);
    }

    ClockUtil.setCurrentTime(DateUtils.addDays(startDate, -2));

    for (int i = 0; i < 4; i++) {
      managementService.deleteBatch(batchIds1.get(i), false);
    }
    ClockUtil.setCurrentTime(DateUtils.addDays(startDate, -7));
    for (int i = 6; i < batchIds1.size(); i++) {
      managementService.deleteBatch(batchIds1.get(i), false);
    }

    ClockUtil.setCurrentTime(DateUtils.addDays(startDate, -10));
    for (int i = 0; i < 7; i++) {
      managementService.deleteBatch(batchIds2.get(i), false);
    }
    ClockUtil.setCurrentTime(DateUtils.addDays(startDate, -5));
    for (int i = 7; i < 11; i++) {
      managementService.deleteBatch(batchIds2.get(i), false);
    }
    ClockUtil.setCurrentTime(DateUtils.addDays(startDate, -1));
    for (int i = 13; i < batchIds2.size(); i++) {
      managementService.deleteBatch(batchIds2.get(i), false);
    }

    ClockUtil.setCurrentTime(DateUtils.addSeconds(startDate, 1));

    // when
    List<HistoricBatch> historicList = historyService.createHistoricBatchQuery().list();
    assertEquals(31, historicList.size());

    List<CleanableHistoricBatchReportResult> list = historyService.createCleanableHistoricBatchReport().list();
    assertEquals(3, list.size());
    for (CleanableHistoricBatchReportResult result : list) {
      if (result.getBatchType().equals("instance-migration")) {
        checkResultNumbers(result, 0, 8, null);
      } else if (result.getBatchType().equals("instance-modification")) {
        checkResultNumbers(result, 1, 1, modOperationsTTL);
      } else if (result.getBatchType().equals("instance-deletion")) {
        checkResultNumbers(result, delOperationsTTL, 18, delOperationsTTL);
      }
    }
  }

  @Test
  public void testReportNoTTLConfiguration() {
    processEngineConfiguration.initHistoryCleanup();
    assertNull(processEngineConfiguration.getBatchOperationHistoryTimeToLive());

    Date startDate = new Date();
    int daysInThePast = -11;
    ClockUtil.setCurrentTime(DateUtils.addDays(startDate, daysInThePast));

    int cancelationCountBatch = 20;
    List<String> batchIds2 = new ArrayList<>();
    batchIds2.addAll(createCancelationBatchList(cancelationCountBatch));

    ClockUtil.setCurrentTime(DateUtils.addDays(startDate, -10));
    for (int i = 0; i < 7; i++) {
      managementService.deleteBatch(batchIds2.get(i), false);
    }
    ClockUtil.setCurrentTime(DateUtils.addDays(startDate, -5));
    for (int i = 7; i < 11; i++) {
      managementService.deleteBatch(batchIds2.get(i), false);
    }
    ClockUtil.setCurrentTime(DateUtils.addDays(startDate, -1));
    for (int i = 13; i < batchIds2.size(); i++) {
      managementService.deleteBatch(batchIds2.get(i), false);
    }

    ClockUtil.setCurrentTime(DateUtils.addSeconds(startDate, 1));

    // when
    List<HistoricBatch> historicList = historyService.createHistoricBatchQuery().list();
    assertEquals(20, historicList.size());

    assertEquals(1, historyService.createCleanableHistoricBatchReport().count());
    checkResultNumbers(historyService.createCleanableHistoricBatchReport().singleResult(), 0, 18, null);
  }

  @Test
  public void testReportZeroTTL() {
    Map<String, String> map = new HashMap<>();
    int modOperationsTTL = 0;
    map.put("instance-modification", "P0D");
    processEngineConfiguration.setBatchOperationsForHistoryCleanup(map);
    processEngineConfiguration.initHistoryCleanup();

    Date startDate = ClockUtil.getCurrentTime();
    int daysInThePast = -11;
    ClockUtil.setCurrentTime(DateUtils.addDays(startDate, daysInThePast));

    Batch modificationBatch = createModificationBatch();
    ClockUtil.setCurrentTime(DateUtils.addDays(startDate, -7));

    managementService.deleteBatch(modificationBatch.getId(), false);

    CleanableHistoricBatchReportResult result = historyService.createCleanableHistoricBatchReport().singleResult();
    assertNotNull(result);
    checkResultNumbers(result, 1, 1, modOperationsTTL);
  }

  @Test
  public void testReportOrderByFinishedProcessInstance() {
    processEngineConfiguration.setBatchOperationHistoryTimeToLive("P5D");
    processEngineConfiguration.initHistoryCleanup();
    assertNotNull(processEngineConfiguration.getBatchOperationHistoryTimeToLive());

    Date startDate = new Date();
    int daysInThePast = -11;
    ClockUtil.setCurrentTime(DateUtils.addDays(startDate, daysInThePast));

    List<String> batchIds = new ArrayList<>();

    Batch modificationBatch = createModificationBatch();
    batchIds.add(modificationBatch.getId());

    int migrationCountBatch = 10;
    batchIds.addAll(createMigrationBatchList(migrationCountBatch));

    int cancelationCountBatch = 20;
    batchIds.addAll(createCancelationBatchList(cancelationCountBatch));

    ClockUtil.setCurrentTime(DateUtils.addDays(startDate, -8));

    for (String batchId : batchIds) {
      managementService.deleteBatch(batchId, false);
    }

    ClockUtil.setCurrentTime(DateUtils.addSeconds(startDate, 1));

    // assume
    List<HistoricBatch> historicList = historyService.createHistoricBatchQuery().list();
    assertEquals(31, historicList.size());

    // then
    List<CleanableHistoricBatchReportResult> reportResultAsc = historyService
        .createCleanableHistoricBatchReport()
        .orderByFinishedBatchOperation()
        .asc()
        .list();
    assertEquals(3, reportResultAsc.size());
    assertEquals("instance-modification", reportResultAsc.get(0).getBatchType());
    assertEquals("instance-migration", reportResultAsc.get(1).getBatchType());
    assertEquals("instance-deletion", reportResultAsc.get(2).getBatchType());

    List<CleanableHistoricBatchReportResult> reportResultDesc = historyService
        .createCleanableHistoricBatchReport()
        .orderByFinishedBatchOperation()
        .desc()
        .list();
    assertEquals(3, reportResultDesc.size());
    assertEquals("instance-deletion", reportResultDesc.get(0).getBatchType());
    assertEquals("instance-migration", reportResultDesc.get(1).getBatchType());
    assertEquals("instance-modification", reportResultDesc.get(2).getBatchType());
  }

  private void checkResultNumbers(CleanableHistoricBatchReportResult result, int expectedCleanable, int expectedFinished, Integer expectedTTL) {
    assertEquals(expectedCleanable, result.getCleanableBatchesCount());
    assertEquals(expectedFinished, result.getFinishedBatchesCount());
    assertEquals(expectedTTL, result.getHistoryTimeToLive());
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

  private List<String> createMigrationBatchList(int migrationCountBatch) {
    List<String> batchIds = new ArrayList<>();
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
    BpmnModelInstance instance = createModelInstance();
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    String pId = runtimeService.startProcessInstanceById(processDefinition.getId()).getId();
    List<String> batchIds = new ArrayList<>();
    for (int i = 0; i < cancelationCountBatch; i++) {
      batchIds.add(runtimeService.deleteProcessInstancesAsync(Arrays.asList(pId), "create-deletion-batch").getId());
    }
    return batchIds;
  }

}
