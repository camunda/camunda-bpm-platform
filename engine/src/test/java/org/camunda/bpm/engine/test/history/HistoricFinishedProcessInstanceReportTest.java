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

package org.camunda.bpm.engine.test.history;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricFinishedProcessInstanceReportResult;
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
public class HistoricFinishedProcessInstanceReportTest {
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(testRule).around(engineRule);

  protected ProcessEngineConfiguration processEngineConfiguration;
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
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    taskService = engineRule.getTaskService();
    repositoryService = engineRule.getRepositoryService();
    runtimeService = engineRule.getRuntimeService();

    testRule.deploy(createProcessWithUserTask(PROCESS_DEFINITION_KEY));
  }

  @After
  public void cleanUp() {
    List<Task> list = taskService.createTaskQuery().list();
    for (Task task : list) {
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
  public void testComplex() {
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
        List<HistoricFinishedProcessInstanceReportResult> reportResults = historyService.createHistoricFinishedProcessInstanceReport().count();

        // then
        assertEquals(3, reportResults.size());
        for (HistoricFinishedProcessInstanceReportResult result : reportResults) {
          if (result.getProcessDefinitionKey().equals(PROCESS_DEFINITION_KEY)) {
            checkResultNumbers(result, 10, 20);
          } else if (result.getProcessDefinitionKey().equals(THIRD_PROCESS_DEFINITION_KEY)) {
            checkResultNumbers(result, 0, 10);
          } else if (result.getProcessDefinitionKey().equals(FOURTH_PROCESS_DEFINITION_KEY)) {
            checkResultNumbers(result, 10, 10);
          }
        }

  }

  private void checkResultNumbers(HistoricFinishedProcessInstanceReportResult result, int expectedCleanable, int expectedFinished) {
    assertEquals(expectedCleanable, result.getCleanableProcessInstanceCount().longValue());
    assertEquals(expectedFinished, result.getFinishedProcessInstanceCount().longValue());
  }

  @Test
  public void testAllCleanable() {
    // given
    prepareProcessInstances(PROCESS_DEFINITION_KEY, -6, 5, 10);

    // when
    List<HistoricFinishedProcessInstanceReportResult> reportResults = historyService.createHistoricFinishedProcessInstanceReport().count();

    // then
    assertEquals(1, reportResults.size());
    boolean entityFound = false;

    for (HistoricFinishedProcessInstanceReportResult result : reportResults) {
      if (result.getProcessDefinitionKey().equals(PROCESS_DEFINITION_KEY)) {
        checkResultNumbers(result, 10, 10);
        entityFound = true;
        break;
      }
    }

    assertTrue(entityFound);
  }

  @Test
  public void testPartCleanable() {
    // given
    prepareProcessInstances(PROCESS_DEFINITION_KEY, -6, 5, 5);
    prepareProcessInstances(PROCESS_DEFINITION_KEY, 0, 5, 5);

    // when
    List<HistoricFinishedProcessInstanceReportResult> reportResults = historyService.createHistoricFinishedProcessInstanceReport().count();

    // then
    assertEquals(1, reportResults.size());
    boolean entityFound = false;

    for (HistoricFinishedProcessInstanceReportResult result : reportResults) {
      if (result.getProcessDefinitionKey().equals(PROCESS_DEFINITION_KEY)) {
        checkResultNumbers(result, 5, 10);
        entityFound = true;
        break;
      }
    }

    assertTrue(entityFound);
  }

  @Test
  public void testZeroTTL() {
    // given
    prepareProcessInstances(PROCESS_DEFINITION_KEY, -6, 0, 5);
    prepareProcessInstances(PROCESS_DEFINITION_KEY, 0, 0, 5);

    // when
    List<HistoricFinishedProcessInstanceReportResult> reportResults = historyService.createHistoricFinishedProcessInstanceReport().count();

    // then
    assertEquals(1, reportResults.size());
    boolean entityFound = false;

    for (HistoricFinishedProcessInstanceReportResult result : reportResults) {
      if (result.getProcessDefinitionKey().equals(PROCESS_DEFINITION_KEY)) {
        checkResultNumbers(result, 10, 10);
        entityFound = true;
        break;
      }
    }

    assertTrue(entityFound);
  }

  @Test
  public void testNullTTL() {
    // given
    prepareProcessInstances(PROCESS_DEFINITION_KEY, -6, null, 5);
    prepareProcessInstances(PROCESS_DEFINITION_KEY, 0, null, 5);

    // when
    List<HistoricFinishedProcessInstanceReportResult> reportResults = historyService.createHistoricFinishedProcessInstanceReport().count();

    // then
    assertEquals(1, reportResults.size());
    boolean entityFound = false;

    for (HistoricFinishedProcessInstanceReportResult result : reportResults) {
      if (result.getProcessDefinitionKey().equals(PROCESS_DEFINITION_KEY)) {
        checkResultNumbers(result, 0, 10);
        entityFound = true;
        break;
      }
    }

    assertTrue(entityFound);
  }
}
