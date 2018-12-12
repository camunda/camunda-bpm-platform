/*
 * Copyright © 2013-2018 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.history.CleanableHistoricProcessInstanceReport;
import org.camunda.bpm.engine.history.CleanableHistoricProcessInstanceReportResult;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class CleanableHistoricProcessInstanceReportTest {
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(testRule).around(engineRule);

  protected HistoryService historyService;
  protected TaskService taskService;
  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;

  protected static final String PROCESS_DEFINITION_KEY = "HISTORIC_INST";
  protected static final String SECOND_PROCESS_DEFINITION_KEY = "SECOND_HISTORIC_INST";
  protected static final String THIRD_PROCESS_DEFINITION_KEY = "THIRD_HISTORIC_INST";
  protected static final String FOURTH_PROCESS_DEFINITION_KEY = "FOURTH_HISTORIC_INST";

  @Before
  public void setUp() {
    historyService = engineRule.getHistoryService();
    taskService = engineRule.getTaskService();
    repositoryService = engineRule.getRepositoryService();
    runtimeService = engineRule.getRuntimeService();

    testRule.deploy(createProcessWithUserTask(PROCESS_DEFINITION_KEY));
  }

  @After
  public void cleanUp() {
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().list();
    for (ProcessInstance processInstance : processInstances) {
      runtimeService.deleteProcessInstance(processInstance.getId(), null, true, true);
    }

    List<Task> tasks = taskService.createTaskQuery().list();
    for (Task task : tasks) {
      taskService.deleteTask(task.getId(), true);
    }

    List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();
    for (HistoricProcessInstance historicProcessInstance : historicProcessInstances) {
      historyService.deleteHistoricProcessInstance(historicProcessInstance.getId());
    }
  }

  protected BpmnModelInstance createProcessWithUserTask(String key) {
    return Bpmn.createExecutableProcess(key)
        .startEvent()
        .userTask(key + "_task1")
          .name(key + " Task 1")
        .endEvent()
        .done();
  }

  protected void prepareProcessInstances(String key, int daysInThePast, Integer historyTimeToLive, int instanceCount) {
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().processDefinitionKey(key).list();
    assertEquals(1, processDefinitions.size());
    repositoryService.updateProcessDefinitionHistoryTimeToLive(processDefinitions.get(0).getId(), historyTimeToLive);

    Date oldCurrentTime = ClockUtil.getCurrentTime();
    ClockUtil.setCurrentTime(DateUtils.addDays(oldCurrentTime, daysInThePast));

    List<String> processInstanceIds = new ArrayList<String>();
    for (int i = 0; i < instanceCount; i++) {
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(key);
      processInstanceIds.add(processInstance.getId());
    }
    runtimeService.deleteProcessInstances(processInstanceIds, null, true, true);

    ClockUtil.setCurrentTime(oldCurrentTime);
  }

  @Test
  public void testReportComplex() {
    testRule.deploy(createProcessWithUserTask(SECOND_PROCESS_DEFINITION_KEY));
    testRule.deploy(createProcessWithUserTask(THIRD_PROCESS_DEFINITION_KEY));
    testRule.deploy(createProcessWithUserTask(FOURTH_PROCESS_DEFINITION_KEY));
    // given
    prepareProcessInstances(PROCESS_DEFINITION_KEY, 0, 5, 10);
    prepareProcessInstances(PROCESS_DEFINITION_KEY, -6, 5, 10);
    prepareProcessInstances(SECOND_PROCESS_DEFINITION_KEY, -6, 5, 10);
    prepareProcessInstances(THIRD_PROCESS_DEFINITION_KEY, -6, null, 10);
    prepareProcessInstances(FOURTH_PROCESS_DEFINITION_KEY, -6, 0, 10);

    repositoryService.deleteProcessDefinition(
        repositoryService.createProcessDefinitionQuery().processDefinitionKey(SECOND_PROCESS_DEFINITION_KEY).singleResult().getId(), false);

    // when
    List<CleanableHistoricProcessInstanceReportResult> reportResults = historyService.createCleanableHistoricProcessInstanceReport().list();
    CleanableHistoricProcessInstanceReportResult secondReportResult = historyService.createCleanableHistoricProcessInstanceReport().processDefinitionIdIn(repositoryService.createProcessDefinitionQuery().processDefinitionKey(THIRD_PROCESS_DEFINITION_KEY).singleResult().getId()).singleResult();
    CleanableHistoricProcessInstanceReportResult thirdReportResult = historyService.createCleanableHistoricProcessInstanceReport().processDefinitionKeyIn(FOURTH_PROCESS_DEFINITION_KEY).singleResult();

    // then
    assertEquals(3, reportResults.size());
    for (CleanableHistoricProcessInstanceReportResult result : reportResults) {
      if (result.getProcessDefinitionKey().equals(PROCESS_DEFINITION_KEY)) {
        checkResultNumbers(result, 10, 20);
      } else if (result.getProcessDefinitionKey().equals(THIRD_PROCESS_DEFINITION_KEY)) {
        checkResultNumbers(result, 0, 10);
      } else if (result.getProcessDefinitionKey().equals(FOURTH_PROCESS_DEFINITION_KEY)) {
        checkResultNumbers(result, 10, 10);
      }
    }
    checkResultNumbers(secondReportResult, 0, 10);
    checkResultNumbers(thirdReportResult, 10, 10);
  }

  private void checkResultNumbers(CleanableHistoricProcessInstanceReportResult result, int expectedCleanable, int expectedFinished) {
    assertEquals(expectedCleanable, result.getCleanableProcessInstanceCount());
    assertEquals(expectedFinished, result.getFinishedProcessInstanceCount());
  }

  @Test
  public void testReportWithAllCleanableInstances() {
    // given
    prepareProcessInstances(PROCESS_DEFINITION_KEY, -6, 5, 10);

    // when
    List<CleanableHistoricProcessInstanceReportResult> reportResults = historyService.createCleanableHistoricProcessInstanceReport().list();
    long count = historyService.createCleanableHistoricProcessInstanceReport().count();

    // then
    assertEquals(1, reportResults.size());
    assertEquals(1, count);

    checkResultNumbers(reportResults.get(0), 10, 10);
  }

  @Test
  public void testReportWithPartiallyCleanableInstances() {
    // given
    prepareProcessInstances(PROCESS_DEFINITION_KEY, -6, 5, 5);
    prepareProcessInstances(PROCESS_DEFINITION_KEY, 0, 5, 5);

    // when
    List<CleanableHistoricProcessInstanceReportResult> reportResults = historyService.createCleanableHistoricProcessInstanceReport().list();

    // then
    assertEquals(1, reportResults.size());

    checkResultNumbers(reportResults.get(0), 5, 10);
  }

  @Test
  public void testReportWithZeroHistoryTTL() {
    // given
    prepareProcessInstances(PROCESS_DEFINITION_KEY, -6, 0, 5);
    prepareProcessInstances(PROCESS_DEFINITION_KEY, 0, 0, 5);

    // when
    CleanableHistoricProcessInstanceReportResult result = historyService.createCleanableHistoricProcessInstanceReport().singleResult();

    // then
    checkResultNumbers(result, 10, 10);
  }

  @Test
  public void testReportWithNullHistoryTTL() {
    // given
    prepareProcessInstances(PROCESS_DEFINITION_KEY, -6, null, 5);
    prepareProcessInstances(PROCESS_DEFINITION_KEY, 0, null, 5);

    // when
    List<CleanableHistoricProcessInstanceReportResult> reportResults = historyService.createCleanableHistoricProcessInstanceReport().list();

    // then
    assertEquals(1, reportResults.size());

    checkResultNumbers(reportResults.get(0), 0, 10);
  }

  @Test
  public void testReportByInvalidProcessDefinitionId() {
    CleanableHistoricProcessInstanceReport report = historyService.createCleanableHistoricProcessInstanceReport();

    try {
      report.processDefinitionIdIn(null);
      fail("Expected NotValidException");
    } catch (NotValidException e) {
      // expected
    }

    try {
      report.processDefinitionIdIn("abc", null, "def");
      fail("Expected NotValidException");
    } catch (NotValidException e) {
      // expected
    }
  }

  @Test
  public void testReportByInvalidProcessDefinitionKey() {
    CleanableHistoricProcessInstanceReport report = historyService.createCleanableHistoricProcessInstanceReport();

    try {
      report.processDefinitionKeyIn(null);
      fail("Expected NotValidException");
    } catch (NotValidException e) {
      // expected
    }

    try {
      report.processDefinitionKeyIn("abc", null, "def");
      fail("Expected NotValidException");
    } catch (NotValidException e) {
      // expected
    }
  }

  @Test
  public void testReportCompact() {
    // given
    List<ProcessDefinition> pdList = repositoryService.createProcessDefinitionQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).list();
    assertEquals(1, pdList.size());
    runtimeService.startProcessInstanceById(pdList.get(0).getId());

    List<CleanableHistoricProcessInstanceReportResult> resultWithZeros = historyService.createCleanableHistoricProcessInstanceReport().list();
    assertEquals(1, resultWithZeros.size());
    assertEquals(0, resultWithZeros.get(0).getFinishedProcessInstanceCount());

    // when
    long resultCountWithoutZeros = historyService.createCleanableHistoricProcessInstanceReport().compact().count();

    // then
    assertEquals(0, resultCountWithoutZeros);
  }

  @Test
  public void testReportOrderByFinishedAsc() {
    testRule.deploy(createProcessWithUserTask(SECOND_PROCESS_DEFINITION_KEY));
    testRule.deploy(createProcessWithUserTask(THIRD_PROCESS_DEFINITION_KEY));
    // given
    prepareProcessInstances(SECOND_PROCESS_DEFINITION_KEY, -6, 5, 6);
    prepareProcessInstances(PROCESS_DEFINITION_KEY, -6, 5, 4);
    prepareProcessInstances(THIRD_PROCESS_DEFINITION_KEY, -6, 5, 8);

    // when
    List<CleanableHistoricProcessInstanceReportResult> reportResult = historyService
      .createCleanableHistoricProcessInstanceReport()
      .orderByFinished()
      .asc()
      .list();

    // then
    assertEquals(3, reportResult.size());
    assertEquals(PROCESS_DEFINITION_KEY, reportResult.get(0).getProcessDefinitionKey());
    assertEquals(SECOND_PROCESS_DEFINITION_KEY, reportResult.get(1).getProcessDefinitionKey());
    assertEquals(THIRD_PROCESS_DEFINITION_KEY, reportResult.get(2).getProcessDefinitionKey());
  }

  @Test
  public void testReportOrderByFinishedDesc() {
    testRule.deploy(createProcessWithUserTask(SECOND_PROCESS_DEFINITION_KEY));
    testRule.deploy(createProcessWithUserTask(THIRD_PROCESS_DEFINITION_KEY));
    // given
    prepareProcessInstances(SECOND_PROCESS_DEFINITION_KEY, -6, 5, 6);
    prepareProcessInstances(PROCESS_DEFINITION_KEY, -6, 5, 4);
    prepareProcessInstances(THIRD_PROCESS_DEFINITION_KEY, -6, 5, 8);

    // when
    List<CleanableHistoricProcessInstanceReportResult> reportResult = historyService
      .createCleanableHistoricProcessInstanceReport()
      .orderByFinished()
      .desc()
      .list();

    // then
    assertEquals(3, reportResult.size());
    assertEquals(THIRD_PROCESS_DEFINITION_KEY, reportResult.get(0).getProcessDefinitionKey());
    assertEquals(SECOND_PROCESS_DEFINITION_KEY, reportResult.get(1).getProcessDefinitionKey());
    assertEquals(PROCESS_DEFINITION_KEY, reportResult.get(2).getProcessDefinitionKey());
  }
}
