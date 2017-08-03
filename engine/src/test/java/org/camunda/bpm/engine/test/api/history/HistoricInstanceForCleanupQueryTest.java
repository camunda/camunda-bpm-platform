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

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.time.DateUtils;
import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.impl.batch.history.HistoricBatchEntity;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.history.event.HistoricDecisionInstanceEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricDecisionInstanceManager;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.metrics.Meter;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricBatchManager;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricCaseInstanceEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricCaseInstanceManager;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricProcessInstanceManager;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.runtime.migration.MigrationTestRule;
import org.camunda.bpm.engine.test.api.runtime.migration.batch.BatchMigrationHelper;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoricInstanceForCleanupQueryTest {

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);
  protected MigrationTestRule migrationRule = new MigrationTestRule(engineRule);
  protected BatchMigrationHelper helper = new BatchMigrationHelper(engineRule, migrationRule);

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Rule public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule).around(migrationRule);

  private HistoryService historyService;
  private RuntimeService runtimeService;
  private ManagementService managementService;
  private CaseService caseService;
  private ProcessEngineConfigurationImpl processEngineConfiguration;

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
    historyService = engineRule.getHistoryService();
    managementService = engineRule.getManagementService();
    caseService = engineRule.getCaseService();
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
  }

  @After
  public void clearDatabase() {
    helper.removeAllRunningAndHistoricBatches();

    clearMetrics();
  }

  protected void clearMetrics() {
    Collection<Meter> meters = processEngineConfiguration.getMetricsRegistry().getMeters().values();
    for (Meter meter : meters) {
      meter.getAndClear();
    }
    managementService.deleteMetrics(null);
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testSortHistoricProcessInstancesForCleanup() {

    final String processDefinitionKey = "oneTaskProcess";

    startAndDeleteProcessInstance(processDefinitionKey, -11);
    startAndDeleteProcessInstance(processDefinitionKey, -10);
    startAndDeleteProcessInstance(processDefinitionKey, -12);

    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {

        HistoricProcessInstanceManager historicProcessInstanceManager = commandContext.getHistoricProcessInstanceManager();
        List<String> historicProcessInstanceIds = historicProcessInstanceManager.findHistoricProcessInstanceIdsForCleanup(7);
        assertEquals(3, historicProcessInstanceIds.size());
        HistoricProcessInstanceEntity historicProcessInstance1 = historicProcessInstanceManager.findHistoricProcessInstance(historicProcessInstanceIds.get(0));
        HistoricProcessInstanceEntity historicProcessInstance2 = historicProcessInstanceManager.findHistoricProcessInstance(historicProcessInstanceIds.get(1));
        HistoricProcessInstanceEntity historicProcessInstance3 = historicProcessInstanceManager.findHistoricProcessInstance(historicProcessInstanceIds.get(2));

        assertTrue(historicProcessInstance1.getEndTime().before(historicProcessInstance2.getEndTime()));
        assertTrue(historicProcessInstance2.getEndTime().before(historicProcessInstance3.getEndTime()));

        return null;
      }
    });
  }

  private void startAndDeleteProcessInstance(String processDefinitionKey, int daysToAdd) {
    Date oldCurrentTime = ClockUtil.getCurrentTime();
    ClockUtil.setCurrentTime(DateUtils.addDays(oldCurrentTime, daysToAdd));
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey(processDefinitionKey);
    runtimeService.deleteProcessInstances(Arrays.asList(processInstance1.getId()), null, true, true);
    ClockUtil.setCurrentTime(oldCurrentTime);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/dmn/Example.dmn")
  public void testSortHistoricDecisionInstancesForCleanup() {

    final String decisionDefinitionKey1 = "decision";

    evaluateDecisionDefinition(decisionDefinitionKey1, -11);
    evaluateDecisionDefinition(decisionDefinitionKey1, -10);
    evaluateDecisionDefinition(decisionDefinitionKey1, -12);

    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {

        HistoricDecisionInstanceManager historicDecisionInstanceManager = commandContext.getHistoricDecisionInstanceManager();
        List<String> historicDecisionInstanceIds = historicDecisionInstanceManager.findHistoricDecisionInstanceIdsForCleanup(7);
        assertEquals(3, historicDecisionInstanceIds.size());

        HistoricDecisionInstanceEntity instance0 = historicDecisionInstanceManager.findHistoricDecisionInstance(historicDecisionInstanceIds.get(0));
        HistoricDecisionInstanceEntity instance1 = historicDecisionInstanceManager.findHistoricDecisionInstance(historicDecisionInstanceIds.get(1));
        HistoricDecisionInstanceEntity instance2 = historicDecisionInstanceManager.findHistoricDecisionInstance(historicDecisionInstanceIds.get(2));

        assertTrue(instance0.getEvaluationTime().before(instance1.getEvaluationTime()));
        assertTrue(instance1.getEvaluationTime().before(instance2.getEvaluationTime()));

        return null;
      }
    });
  }

  private void evaluateDecisionDefinition(String decisionDefinitionKey, int daysToAdd) {
    Date oldCurrentTime = ClockUtil.getCurrentTime();
    ClockUtil.setCurrentTime(DateUtils.addDays(oldCurrentTime, daysToAdd));
    engineRule.getDecisionService().evaluateDecisionByKey(decisionDefinitionKey).variables(getDMNVariables()).evaluate();
    ClockUtil.setCurrentTime(oldCurrentTime);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/cmmn/oneTaskCaseWithHistoryTimeToLive.cmmn")
  public void testSortHistoricCaseInstancesForCleanup() {

    final String caseDefinitionKey1 = "case";

    startAndCloseCaseInstance(caseDefinitionKey1, -11);
    startAndCloseCaseInstance(caseDefinitionKey1, -10);
    startAndCloseCaseInstance(caseDefinitionKey1, -12);

    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {

        HistoricCaseInstanceManager historicCaseInstanceManager = commandContext.getHistoricCaseInstanceManager();
        List<String> historicCaseInstanceIds = historicCaseInstanceManager.findHistoricCaseInstanceIdsForCleanup(7);
        assertEquals(3, historicCaseInstanceIds.size());
        HistoricCaseInstanceEntity instance0 = historicCaseInstanceManager.findHistoricCaseInstance(historicCaseInstanceIds.get(0));
        HistoricCaseInstanceEntity instance1 = historicCaseInstanceManager.findHistoricCaseInstance(historicCaseInstanceIds.get(1));
        HistoricCaseInstanceEntity instance2 = historicCaseInstanceManager.findHistoricCaseInstance(historicCaseInstanceIds.get(2));
        assertTrue(instance0.getCloseTime().before(instance1.getCloseTime()));
        assertTrue(instance1.getCloseTime().before(instance2.getCloseTime()));

        return null;
      }
    });
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSortHistoricBatchesForCleanup() {
    Date startDate = ClockUtil.getCurrentTime();
    int daysInThePast = -11;
    ClockUtil.setCurrentTime(DateUtils.addDays(startDate, daysInThePast));

    // given
    List<Batch> list = Arrays.asList(helper.migrateProcessInstancesAsync(1), helper.migrateProcessInstancesAsync(1), helper.migrateProcessInstancesAsync(1));

    String batchType = list.get(0).getType();
    final Map<String, Integer> batchOperationsMap = new HashedMap();
    batchOperationsMap.put(batchType, 4);

    for (Batch batch : list) {
      helper.executeSeedJob(batch);
      helper.executeJobs(batch);

      ClockUtil.setCurrentTime(DateUtils.addDays(startDate, ++daysInThePast));
      helper.executeMonitorJob(batch);
    }

    ClockUtil.setCurrentTime(new Date());
    // when
    List<HistoricBatch> historicList = historyService.createHistoricBatchQuery().list();
    assertEquals(3, historicList.size());

    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {

        HistoricBatchManager historicBatchManager = commandContext.getHistoricBatchManager();
        List<String> ids = historicBatchManager.findHistoricBatchIdsForCleanup(7, batchOperationsMap);
        assertEquals(3, ids.size());
        HistoricBatchEntity instance0 = historicBatchManager.findHistoricBatchById(ids.get(0));
        HistoricBatchEntity instance1 = historicBatchManager.findHistoricBatchById(ids.get(1));
        HistoricBatchEntity instance2 = historicBatchManager.findHistoricBatchById(ids.get(2));
        assertTrue(instance0.getEndTime().before(instance1.getEndTime()));
        assertTrue(instance1.getEndTime().before(instance2.getEndTime()));

        return null;
      }
    });
  }

  private void startAndCloseCaseInstance(String caseDefinitionKey, int daysToAdd) {
    Date oldCurrentTime = ClockUtil.getCurrentTime();
    ClockUtil.setCurrentTime(DateUtils.addDays(oldCurrentTime, daysToAdd));
    CaseInstance caseInstance1 = caseService.createCaseInstanceByKey(caseDefinitionKey);
    caseService.terminateCaseExecution(caseInstance1.getId());
    caseService.closeCaseInstance(caseInstance1.getId());
    ClockUtil.setCurrentTime(oldCurrentTime);
  }

  protected VariableMap getDMNVariables() {
    return Variables.createVariables().putValue("status", "silver").putValue("sum", 723);
  }

}
