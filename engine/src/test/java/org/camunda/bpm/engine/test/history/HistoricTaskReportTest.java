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

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.history.HistoricTaskInstanceReportResult;
import org.camunda.bpm.engine.impl.util.ClockUtil;
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

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Stefan Hentschel.
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoricTaskReportTest {

  public ProcessEngineRule processEngineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule processEngineTestRule = new ProcessEngineTestRule(processEngineRule);

  @Rule
  public RuleChain ruleChain = RuleChain
    .outerRule(processEngineTestRule)
    .around(processEngineRule);

  protected ProcessEngineConfiguration processEngineConfiguration;
  protected HistoryService historyService;

  protected static final String PROCESS_DEFINITION_KEY = "HISTORIC_TASK_INST_REPORT";
  protected static final String ANOTHER_PROCESS_DEFINITION_KEY = "ANOTHER_HISTORIC_TASK_INST_REPORT";


  @Before
  public void setUp() {
    historyService = processEngineRule.getHistoryService();
    processEngineConfiguration = processEngineRule.getProcessEngineConfiguration();

    processEngineTestRule.deploy(createProcessWithUserTask(PROCESS_DEFINITION_KEY));
    processEngineTestRule.deploy(createProcessWithUserTask(ANOTHER_PROCESS_DEFINITION_KEY));
  }

  @After
  public void cleanUp() {
    List<Task> list = processEngineRule.getTaskService().createTaskQuery().list();
    for( Task task : list ) {
      processEngineRule.getTaskService().deleteTask(task.getId(), true);
    }
  }

  @Test
  public void testHistoricTaskInstanceReportQuery() {
    // given
    startAndCompleteProcessInstance(PROCESS_DEFINITION_KEY, 2016, 7, 14, 12, 1);
    startAndCompleteProcessInstance(PROCESS_DEFINITION_KEY, 2016, 7, 14, 12, 1);
    startAndCompleteProcessInstance(ANOTHER_PROCESS_DEFINITION_KEY, 2016, 7, 14, 12, 1);
    startAndCompleteProcessInstance(ANOTHER_PROCESS_DEFINITION_KEY, 2016, 7, 14, 12, 1);

    processEngineTestRule.deploy(createProcessWithUserTask(PROCESS_DEFINITION_KEY));
    startAndCompleteProcessInstance(PROCESS_DEFINITION_KEY, 2016, 7, 14, 12, 1);

    // when
    List<HistoricTaskInstanceReportResult> historicTaskInstanceReportResults = historyService.createHistoricTaskInstanceReport().countByTaskDefinitionKey();

    // then
    assertEquals(3, historicTaskInstanceReportResults.size());
    assertEquals(2, historicTaskInstanceReportResults.get(0).getCount(), 0);
  }

  @Test
  public void testHistoricTaskInstanceReportGroupedByProcessDefinitionKey() {
    // given
    startAndCompleteProcessInstance(PROCESS_DEFINITION_KEY, 2016, 7, 14, 12, 1);
    startAndCompleteProcessInstance(PROCESS_DEFINITION_KEY, 2016, 7, 14, 12, 1);
    startAndCompleteProcessInstance(ANOTHER_PROCESS_DEFINITION_KEY, 2016, 7, 14, 12, 1);
    startAndCompleteProcessInstance(ANOTHER_PROCESS_DEFINITION_KEY, 2016, 7, 14, 12, 1);

    processEngineTestRule.deploy(createProcessWithUserTask(PROCESS_DEFINITION_KEY));
    startAndCompleteProcessInstance(PROCESS_DEFINITION_KEY, 2016, 7, 14, 12, 1);

    // when
    List<HistoricTaskInstanceReportResult> historicTaskInstanceReportResults = historyService
      .createHistoricTaskInstanceReport()
      .countByProcessDefinitionKey();

    // then
    assertEquals(2, historicTaskInstanceReportResults.size());
  }

  @Test
  public void testHistoricTaskInstanceReportWithCompletedAfterDate() {
    // given
    startAndCompleteProcessInstance(PROCESS_DEFINITION_KEY, 2016, 7, 14, 12, 1);
    startAndCompleteProcessInstance(PROCESS_DEFINITION_KEY, 2016, 8, 14, 12, 1);
    startAndCompleteProcessInstance(ANOTHER_PROCESS_DEFINITION_KEY, 2016, 7, 14, 12, 1);

    // when
    Calendar calendar = Calendar.getInstance();
    calendar.set(2016, 11, 14, 12, 5);

    List<HistoricTaskInstanceReportResult> historicTaskInstanceReportResults = historyService
      .createHistoricTaskInstanceReport()
      .completedAfter(calendar.getTime())
      .countByProcessDefinitionKey();

    // then
    assertEquals(1, historicTaskInstanceReportResults.size());
    assertEquals(1, historicTaskInstanceReportResults.get(0).getCount(), 0);
  }

  @Test
  public void testHistoricTaskInstanceReportWithCompletedBeforeDate() {
    // given
    startAndCompleteProcessInstance(PROCESS_DEFINITION_KEY, 2016, 7, 14, 12, 1);
    startAndCompleteProcessInstance(PROCESS_DEFINITION_KEY, 2016, 8, 14, 12, 1);
    startAndCompleteProcessInstance(ANOTHER_PROCESS_DEFINITION_KEY, 2016, 7, 14, 12, 1);

    // when
    Calendar calendar = Calendar.getInstance();
    calendar.set(2016, 11, 14, 12, 5);

    List<HistoricTaskInstanceReportResult> historicTaskInstanceReportResults = historyService
      .createHistoricTaskInstanceReport()
      .completedBefore(calendar.getTime())
      .countByProcessDefinitionKey();

    // then
    assertEquals(2, historicTaskInstanceReportResults.size());
    assertEquals(1, historicTaskInstanceReportResults.get(0).getCount(), 0);
  }

  @Test
  public void testCompletedAfterWithNullValue() {
    try {
      historyService
        .createHistoricTaskInstanceReport()
        .completedAfter(null)
        .countByProcessDefinitionKey();

      fail("Expected NotValidException");
    } catch( NotValidException nve) {
      assertTrue(nve.getMessage().contains("completedAfter"));
    }
  }

  @Test
  public void testCompletedBeforeWithNullValue() {
    try {
      historyService
        .createHistoricTaskInstanceReport()
        .completedBefore(null)
        .countByProcessDefinitionKey();

      fail("Expected NotValidException");
    } catch( NotValidException nve) {
      assertTrue(nve.getMessage().contains("completedBefore"));
    }
  }

  protected BpmnModelInstance createProcessWithUserTask(String key) {
    double random = Math.random();
    return Bpmn.createExecutableProcess(key)
      .startEvent()
        .userTask(key + "_" + random + "_task1")
          .name(key + " Task 1")
      .endEvent()
      .done();
  }

  protected void completeTask(String pid) {
    Task task = processEngineRule.getTaskService().createTaskQuery().processInstanceId(pid).singleResult();
    processEngineRule.getTaskService().complete(task.getId());
  }

  protected void setCurrentTime(int year, int month, int dayOfMonth, int hourOfDay, int minute) {
    Calendar calendar = Calendar.getInstance();
    // Calendars month start with 0 = January
    calendar.set(year, month - 1, dayOfMonth, hourOfDay, minute);
    ClockUtil.setCurrentTime(calendar.getTime());
  }

  protected void addToCalendar(int field, int month) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(ClockUtil.getCurrentTime());
    calendar.add(field, month);
    ClockUtil.setCurrentTime(calendar.getTime());
  }

  protected void startAndCompleteProcessInstance(String key, int year, int month, int dayOfMonth, int hourOfDay, int minute) {
    setCurrentTime(year, month, dayOfMonth , hourOfDay, minute);

    ProcessInstance pi = processEngineRule.getRuntimeService().startProcessInstanceByKey(key);

    addToCalendar(Calendar.MONTH, 5);
    completeTask(pi.getId());

    ClockUtil.reset();
  }
}
