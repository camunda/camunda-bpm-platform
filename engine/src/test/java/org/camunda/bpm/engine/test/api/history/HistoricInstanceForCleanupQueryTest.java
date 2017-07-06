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
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricCaseInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.history.event.HistoricDecisionInstanceEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricDecisionInstanceManager;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.metrics.Meter;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricCaseInstanceEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricCaseInstanceManager;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricProcessInstanceManager;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoricInstanceForCleanupQueryTest {

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

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
    testRule.deploy("org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml", "org/camunda/bpm/engine/test/api/dmn/Example.dmn",
        "org/camunda/bpm/engine/test/api/cmmn/oneTaskCaseWithHistoryTimeToLive.cmmn");
  }

  @After
  public void clearDatabase() {

    List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();
    for (HistoricProcessInstance historicProcessInstance : historicProcessInstances) {
      historyService.deleteHistoricProcessInstance(historicProcessInstance.getId());
    }

    List<HistoricDecisionInstance> historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery().list();
    for (HistoricDecisionInstance historicDecisionInstance : historicDecisionInstances) {
      historyService.deleteHistoricDecisionInstanceByInstanceId(historicDecisionInstance.getId());
    }

    List<HistoricCaseInstance> historicCaseInstances = historyService.createHistoricCaseInstanceQuery().list();
    for (HistoricCaseInstance historicCaseInstance : historicCaseInstances) {
      historyService.deleteHistoricCaseInstance(historicCaseInstance.getId());
    }

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
  public void testSortHistoricProcessInstancesForCleanup() {
    Date oldCurrentTime = ClockUtil.getCurrentTime();
    ClockUtil.setCurrentTime(DateUtils.addDays(oldCurrentTime, -11));

    final String processDefinitionKey1 = "firstProcess";
    BpmnModelInstance model1 = Bpmn.createExecutableProcess(processDefinitionKey1)
        .camundaHistoryTimeToLive(5)
        .startEvent()
          .userTask("userTask")
        .endEvent()
        .done();
    testRule.deploy(model1);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey(processDefinitionKey1);
    runtimeService.deleteProcessInstances(Arrays.asList(processInstance1.getId()), null, true, true);

    ClockUtil.setCurrentTime(DateUtils.addDays(oldCurrentTime, -10));

    final String processDefinitionKey2 = "secondProcess";
    BpmnModelInstance model2 = Bpmn.createExecutableProcess(processDefinitionKey2)
        .camundaHistoryTimeToLive(5)
        .startEvent()
          .userTask("userTask")
        .endEvent()
        .done();
    testRule.deploy(model2);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey(processDefinitionKey2);
    runtimeService.deleteProcessInstances(Arrays.asList(processInstance2.getId()), null, true, true);

    ClockUtil.setCurrentTime(DateUtils.addDays(oldCurrentTime, -12));

    final String processDefinitionKey3 = "thirdProcess";
    BpmnModelInstance model3 = Bpmn.createExecutableProcess(processDefinitionKey3)
        .camundaHistoryTimeToLive(5)
        .startEvent()
          .userTask("userTask")
        .endEvent()
        .done();
    testRule.deploy(model3);
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey(processDefinitionKey3);
    runtimeService.deleteProcessInstances(Arrays.asList(processInstance3.getId()), null, true, true);

    ClockUtil.setCurrentTime(oldCurrentTime);

    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {

        HistoricProcessInstanceManager historicProcessInstanceManager = commandContext.getHistoricProcessInstanceManager();
        List<String> historicProcessInstanceIds = historicProcessInstanceManager.findHistoricProcessInstanceIdsForCleanup(7);
        assertEquals(3, historicProcessInstanceIds.size());
        HistoricProcessInstanceEntity historicProcessInstance = historicProcessInstanceManager.findHistoricProcessInstance(historicProcessInstanceIds.get(0));
        assertEquals(historicProcessInstance.getProcessDefinitionKey(), processDefinitionKey3);
        historicProcessInstance = historicProcessInstanceManager.findHistoricProcessInstance(historicProcessInstanceIds.get(1));
        assertEquals(historicProcessInstance.getProcessDefinitionKey(), processDefinitionKey1);
        historicProcessInstance = historicProcessInstanceManager.findHistoricProcessInstance(historicProcessInstanceIds.get(2));
        assertEquals(historicProcessInstance.getProcessDefinitionKey(), processDefinitionKey2);

        return null;
      }
    });
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/dmn/Example.dmn")
  public void testSortHistoricDecisionInstancesForCleanup() {
    Date oldCurrentTime = ClockUtil.getCurrentTime();
    ClockUtil.setCurrentTime(DateUtils.addDays(oldCurrentTime, -11));

    final String decisionDefinitionKey1 = "decision";

    engineRule.getDecisionService().evaluateDecisionByKey(decisionDefinitionKey1).variables(getDMNVariables()).evaluate();

    ClockUtil.setCurrentTime(DateUtils.addDays(oldCurrentTime, -10));

    engineRule.getDecisionService().evaluateDecisionByKey(decisionDefinitionKey1).variables(getDMNVariables()).evaluate();

    ClockUtil.setCurrentTime(DateUtils.addDays(oldCurrentTime, -12));

    engineRule.getDecisionService().evaluateDecisionByKey(decisionDefinitionKey1).variables(getDMNVariables()).evaluate();

    ClockUtil.setCurrentTime(oldCurrentTime);

    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {

        HistoricDecisionInstanceManager historicDecisionInstanceManager = commandContext.getHistoricDecisionInstanceManager();
        List<String> historicDecisionInstanceIds = historicDecisionInstanceManager.findHistoricDecisionInstanceIdsForCleanup(7);
        assertEquals(3, historicDecisionInstanceIds.size());
        HistoricDecisionInstanceEntity instance0 = historicDecisionInstanceManager.findHistoricDecisionInstance(historicDecisionInstanceIds.get(0));
        HistoricDecisionInstanceEntity instance1 = historicDecisionInstanceManager.findHistoricDecisionInstance(historicDecisionInstanceIds.get(1));
        HistoricDecisionInstanceEntity instance2 = historicDecisionInstanceManager.findHistoricDecisionInstance(historicDecisionInstanceIds.get(2));
        assertTrue(instance0.getEvaluationTime().compareTo(instance1.getEvaluationTime()) < 0);
        assertTrue(instance1.getEvaluationTime().compareTo(instance2.getEvaluationTime()) < 0);

        return null;
      }
    });
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/cmmn/oneTaskCaseWithHistoryTimeToLive.cmmn")
  public void testSortHistoricCaseInstancesForCleanup() {
    Date oldCurrentTime = ClockUtil.getCurrentTime();
    ClockUtil.setCurrentTime(DateUtils.addDays(oldCurrentTime, -11));

    final String caseDefinitionKey1 = "case";

    CaseInstance caseInstance1 = caseService.createCaseInstanceByKey(caseDefinitionKey1);
    caseService.terminateCaseExecution(caseInstance1.getId());
    caseService.closeCaseInstance(caseInstance1.getId());

    ClockUtil.setCurrentTime(DateUtils.addDays(oldCurrentTime, -10));

    CaseInstance caseInstance2 = caseService.createCaseInstanceByKey(caseDefinitionKey1);
    caseService.terminateCaseExecution(caseInstance2.getId());
    caseService.closeCaseInstance(caseInstance2.getId());

    ClockUtil.setCurrentTime(DateUtils.addDays(oldCurrentTime, -12));

    CaseInstance caseInstance3 = caseService.createCaseInstanceByKey(caseDefinitionKey1);
    caseService.terminateCaseExecution(caseInstance3.getId());
    caseService.closeCaseInstance(caseInstance3.getId());

    ClockUtil.setCurrentTime(oldCurrentTime);

    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {

        HistoricCaseInstanceManager historicCaseInstanceManager = commandContext.getHistoricCaseInstanceManager();
        List<String> historicCaseInstanceIds = historicCaseInstanceManager.findHistoricCaseInstanceIdsForCleanup(7);
        assertEquals(3, historicCaseInstanceIds.size());
        HistoricCaseInstanceEntity instance0 = historicCaseInstanceManager.findHistoricCaseInstance(historicCaseInstanceIds.get(0));
        HistoricCaseInstanceEntity instance1 = historicCaseInstanceManager.findHistoricCaseInstance(historicCaseInstanceIds.get(1));
        HistoricCaseInstanceEntity instance2 = historicCaseInstanceManager.findHistoricCaseInstance(historicCaseInstanceIds.get(2));
        assertTrue(instance0.getCloseTime().compareTo(instance1.getCloseTime()) < 0);
        assertTrue(instance1.getCloseTime().compareTo(instance2.getCloseTime()) < 0);

        return null;
      }
    });
  }

  protected VariableMap getDMNVariables() {
    return Variables.createVariables().putValue("status", "silver").putValue("sum", 723);
  }

}
