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

import static org.camunda.bpm.engine.query.PeriodUnit.MONTH;
import static org.camunda.bpm.engine.query.PeriodUnit.QUARTER;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.history.DurationReportResult;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceReport;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.query.PeriodUnit;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

/**
 * @author Roman Smirnov
 *
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
public class HistoricProcessInstanceDurationReportTest extends PluggableProcessEngineTestCase {

  private Random random = new Random();

  public void testDurationReportByMonth() {
    // given
    deployment(createProcessWithUserTask("process"));

    DurationReportResultAssertion assertion = createReportScenario()
      .periodUnit(MONTH)
      // period: 01 (January)
      .startAndCompleteProcessInstance("process", 2016, 0, 1, 10, 0) // 01.01.2016 10:00
      .startAndCompleteProcessInstance("process", 2016, 0, 1, 10, 0) // 01.01.2016 10:00
      .startAndCompleteProcessInstance("process", 2016, 0, 1, 10, 0) // 01.01.2016 10:00
      .done();

    // when
    List<DurationReportResult> result = historyService
        .createHistoricProcessInstanceReport()
        .duration(MONTH);

    // then
    assertThat(result).matches(assertion);
  }

  public void testTwoInstancesInSamePeriodByMonth() {
    // given
    deployment(createProcessWithUserTask("process"));

    DurationReportResultAssertion assertion = createReportScenario()
        .periodUnit(MONTH)
        // period: 01 (January)
        .startAndCompleteProcessInstance("process", 2016, 0, 1, 10, 0) // 01.01.2016 10:00
        .startAndCompleteProcessInstance("process", 2016, 0, 15, 10, 0) // 15.01.2016 10:00
        .startAndCompleteProcessInstance("process", 2016, 0, 15, 10, 0) // 15.01.2016 10:00
        .done();

    // when
    List<DurationReportResult> result = historyService
        .createHistoricProcessInstanceReport()
        .duration(MONTH);

    // then
    assertThat(result).matches(assertion);
  }

  public void testDurationReportInDifferentPeriodsByMonth() {
    // given
    deployment(createProcessWithUserTask("process"));

    DurationReportResultAssertion assertion = createReportScenario()
        .periodUnit(MONTH)
        // period: 11 (November)
        .startAndCompleteProcessInstance("process", 2015, 10, 1, 10, 0) // 01.11.2015 10:00
        // period: 12 (December)
        .startAndCompleteProcessInstance("process", 2015, 11, 1, 10, 0) // 01.12.2015 10:00
        // period: 01 (January)
        .startAndCompleteProcessInstance("process", 2016, 0, 1, 10, 0) // 01.01.2016 10:00
        // period: 02 (February)
        .startAndCompleteProcessInstance("process", 2016, 1, 1, 10, 0) // 01.02.2016 10:00
        // period: 03 (March)
        .startAndCompleteProcessInstance("process", 2016, 2, 1, 10, 0) // 01.03.2016 10:00
        // period: 04 (April)
        .startAndCompleteProcessInstance("process", 2016, 3, 1, 10, 0) // 01.04.2016 10:00
        // period: 05 (May)
        .startAndCompleteProcessInstance("process", 2016, 4, 1, 10, 0) // 01.05.2016 10:00
        // period: 06 (June)
        .startAndCompleteProcessInstance("process", 2016, 5, 1, 10, 0) // 01.06.2016 10:00
        // period: 07 (July)
        .startAndCompleteProcessInstance("process", 2016, 6, 1, 10, 0) // 01.07.2016 10:00
        // period: 08 (August)
        .startAndCompleteProcessInstance("process", 2016, 7, 1, 10, 0) // 01.08.2016 10:00
        // period: 09 (September)
        .startAndCompleteProcessInstance("process", 2016, 8, 1, 10, 0) // 01.09.2016 10:00
        // period: 10 (October)
        .startAndCompleteProcessInstance("process", 2016, 9, 1, 10, 0) // 01.10.2016 10:00
        .done();

    // when
    List<DurationReportResult> result = historyService
        .createHistoricProcessInstanceReport()
        .duration(MONTH);

    // then
    assertThat(result).matches(assertion);
  }

  public void testSamePeriodDifferentYearByMonth() {
    // given
    deployment(createProcessWithUserTask("process"));

    DurationReportResultAssertion assertion = createReportScenario()
        .periodUnit(MONTH)
        // period: 01 (January)
        .startAndCompleteProcessInstance("process", 2015, 1, 1, 10, 0) // 01.01.2015 10:00
        .startAndCompleteProcessInstance("process", 2016, 1, 1, 10, 0) // 01.01.2016 10:00
        .done();

    // when
    List<DurationReportResult> result = historyService
        .createHistoricProcessInstanceReport()
        .duration(MONTH);

    // then
    assertThat(result).matches(assertion);
  }

  public void testDurationReportByQuarter() {
    // given
    deployment(createProcessWithUserTask("process"));

    DurationReportResultAssertion assertion = createReportScenario()
        .periodUnit(QUARTER)
        // period: 2. quarter
        .startAndCompleteProcessInstance("process", 2016, 3, 1, 10, 0) // 01.04.2016 10:00
        .done();

    // when
    List<DurationReportResult> result = historyService
        .createHistoricProcessInstanceReport()
        .duration(QUARTER);

    // then
    assertThat(result).matches(assertion);
  }

  public void testTwoInstancesInSamePeriodDurationReportByQuarter() {
    // given
    deployment(createProcessWithUserTask("process"));

    DurationReportResultAssertion assertion = createReportScenario()
        .periodUnit(QUARTER)
        // period: 2. quarter
        .startAndCompleteProcessInstance("process", 2016, 3, 1, 10, 0) // 01.04.2016 10:00
        .startAndCompleteProcessInstance("process", 2016, 5, 1, 10, 0) // 01.05.2016 10:00
        .done();

    // when
    List<DurationReportResult> result = historyService
        .createHistoricProcessInstanceReport()
        .duration(QUARTER);

    // then
    assertThat(result).matches(assertion);
  }

  public void testDurationReportInDifferentPeriodsByQuarter() {
    // given
    deployment(createProcessWithUserTask("process"));

    DurationReportResultAssertion assertion = createReportScenario()
        .periodUnit(QUARTER)
        // period: 4. quarter (2015)
        .startAndCompleteProcessInstance("process", 2015, 10, 1, 10, 0) // 01.11.2015 10:00
        .startAndCompleteProcessInstance("process", 2015, 11, 1, 10, 0) // 01.12.2015 10:00
        // period: 1. quarter (2016)
        .startAndCompleteProcessInstance("process", 2016, 1, 1, 10, 0) // 01.02.2016 10:00
        .startAndCompleteProcessInstance("process", 2015, 2, 1, 10, 0) // 01.03.2016 10:00
        // period: 2. quarter (2016)
        .startAndCompleteProcessInstance("process", 2015, 3, 1, 10, 0) // 01.04.2016 10:00
        .startAndCompleteProcessInstance("process", 2015, 5, 1, 10, 0) // 01.06.2016 10:00
        // period: 3. quarter (2016)
        .startAndCompleteProcessInstance("process", 2015, 6, 1, 10, 0) // 01.07.2016 10:00
        .startAndCompleteProcessInstance("process", 2015, 7, 1, 10, 0) // 01.08.2016 10:00
        .done();

    // when
    List<DurationReportResult> result = historyService
        .createHistoricProcessInstanceReport()
        .duration(QUARTER);

    // then
    assertThat(result).matches(assertion);
  }

  public void testSamePeriodDifferentYearByQuarter() {
    // given
    deployment(createProcessWithUserTask("process"));

    DurationReportResultAssertion assertion = createReportScenario()
        .periodUnit(QUARTER)
        // period: 1. quarter
        .startAndCompleteProcessInstance("process", 2015, 1, 1, 10, 0) // 01.01.2015 10:00
        .startAndCompleteProcessInstance("process", 2016, 1, 1, 10, 0) // 01.01.2016 10:00
        .done();

    // when
    List<DurationReportResult> result = historyService
        .createHistoricProcessInstanceReport()
        .duration(QUARTER);

    // then
    assertThat(result).matches(assertion);
  }

  public void testReportByInvalidPeriodUnit() {
    HistoricProcessInstanceReport report = historyService.createHistoricProcessInstanceReport();

    try {
      report.duration(null);
      fail();
    } catch (NotValidException e) {}
  }

  public void testReportByStartedBeforeByMonth() {
    // given
    deployment(createProcessWithUserTask("process"));

    DurationReportResultAssertion assertion = createReportScenario()
      .periodUnit(MONTH)
      .startAndCompleteProcessInstance("process", 2016, 0, 15, 10, 0) // 15.01.2016 10:00
      .done();

    // start a second process instance
    createReportScenario()
        .startAndCompleteProcessInstance("process", 2016, 3, 1, 10, 0) // 01.04.2016 10:00
        .done();

    Calendar calendar = Calendar.getInstance();
    calendar.set(2016, 0, 16, 0, 0, 0);

    // when
    List<DurationReportResult> result = historyService
        .createHistoricProcessInstanceReport()
        .startedBefore(calendar.getTime())
        .duration(MONTH);

    // then
    assertThat(result).matches(assertion);
  }

  public void testReportByStartedBeforeByQuarter() {
    // given
    deployment(createProcessWithUserTask("process"));

    DurationReportResultAssertion assertion = createReportScenario()
      .periodUnit(QUARTER)
      .startAndCompleteProcessInstance("process", 2016, 0, 15, 10, 0) // 15.01.2016 10:00
      .startAndCompleteProcessInstance("process", 2016, 0, 15, 10, 0) // 15.01.2016 10:00
      .startAndCompleteProcessInstance("process", 2016, 0, 15, 10, 0) // 15.01.2016 10:00
      .done();

    // start a second process instance
    createReportScenario()
        .startAndCompleteProcessInstance("process", 2016, 3, 1, 10, 0) // 01.04.2016 10:00
        .startAndCompleteProcessInstance("process", 2016, 3, 1, 10, 0) // 01.04.2016 10:00
        .startAndCompleteProcessInstance("process", 2016, 3, 1, 10, 0) // 01.04.2016 10:00
        .done();

    Calendar calendar = Calendar.getInstance();
    calendar.set(2016, 0, 16, 0, 0, 0);

    // when
    List<DurationReportResult> result = historyService
        .createHistoricProcessInstanceReport()
        .startedBefore(calendar.getTime())
        .duration(QUARTER);

    // then
    assertThat(result).matches(assertion);
  }

  public void testReportByInvalidStartedBefore() {
    HistoricProcessInstanceReport report = historyService.createHistoricProcessInstanceReport();

    try {
      report.startedBefore(null);
      fail();
    } catch (NotValidException e) {}
  }

  public void testReportByStartedAfterByMonth() {
    // given
    deployment(createProcessWithUserTask("process"));

    createReportScenario()
      .startAndCompleteProcessInstance("process", 2015, 11, 15, 10, 0) // 15.12.2015 10:00
      .done();

    DurationReportResultAssertion assertion = createReportScenario()
        .periodUnit(MONTH)
        .startAndCompleteProcessInstance("process", 2016, 3, 1, 10, 0) // 01.04.2016 10:00
        .done();

    Calendar calendar = Calendar.getInstance();
    calendar.set(2016, 0, 1, 0, 0, 0);

    // when
    List<DurationReportResult> result = historyService
        .createHistoricProcessInstanceReport()
        .startedAfter(calendar.getTime())
        .duration(MONTH);

    // then
    assertThat(result).matches(assertion);
  }

  public void testReportByStartedAfterByQuarter() {
    // given
    deployment(createProcessWithUserTask("process"));

    createReportScenario()
      .startAndCompleteProcessInstance("process", 2015, 11, 15, 10, 0) // 15.12.2015 10:00
      .done();

    DurationReportResultAssertion assertion = createReportScenario()
        .periodUnit(QUARTER)
        .startAndCompleteProcessInstance("process", 2016, 3, 1, 10, 0) // 01.04.2016 10:00
        .done();

    Calendar calendar = Calendar.getInstance();
    calendar.set(2016, 0, 1, 0, 0, 0);

    // when
    List<DurationReportResult> result = historyService
        .createHistoricProcessInstanceReport()
        .startedAfter(calendar.getTime())
        .duration(QUARTER);

    // then
    assertThat(result).matches(assertion);
  }

  public void testReportByInvalidStartedAfter() {
    HistoricProcessInstanceReport report = historyService.createHistoricProcessInstanceReport();

    try {
      report.startedAfter(null);
      fail();
    } catch (NotValidException e) {}
  }

  public void testReportByStartedAfterAndStartedBeforeByMonth() {
    // given
    deployment(createProcessWithUserTask("process"));

    DurationReportResultAssertion assertion = createReportScenario()
        .periodUnit(MONTH)
        .startAndCompleteProcessInstance("process", 2016, 1, 15, 10, 0) // 15.02.2016 10:00
        .startAndCompleteProcessInstance("process", 2016, 2, 1, 10, 0) // 01.03.2016 10:00
        .done();

    createReportScenario()
        .startAndCompleteProcessInstance("process", 2016, 3, 15, 10, 0) // 15.04.2016 10:00
        .done();

    Calendar calendar = Calendar.getInstance();
    calendar.set(2016, 0, 1, 0, 0, 0);
    Date after = calendar.getTime();
    calendar.set(2016, 2, 31, 23, 59, 59);
    Date before = calendar.getTime();

    // when
    List<DurationReportResult> result = historyService
        .createHistoricProcessInstanceReport()
        .startedAfter(after)
        .startedBefore(before)
        .duration(MONTH);

    // then
    assertThat(result).matches(assertion);
  }

  public void testReportByStartedAfterAndStartedBeforeByQuarter() {
    // given
    deployment(createProcessWithUserTask("process"));

    DurationReportResultAssertion assertion = createReportScenario()
        .periodUnit(QUARTER)
        .startAndCompleteProcessInstance("process", 2016, 1, 15, 10, 0) // 15.02.2016 10:00
        .startAndCompleteProcessInstance("process", 2016, 2, 1, 10, 0) // 01.03.2016 10:00
        .done();

    createReportScenario()
        .startAndCompleteProcessInstance("process", 2016, 3, 15, 10, 0) // 15.04.2016 10:00
        .done();

    Calendar calendar = Calendar.getInstance();
    calendar.set(2016, 0, 1, 0, 0, 0);
    Date after = calendar.getTime();
    calendar.set(2016, 2, 31, 23, 59, 59);
    Date before = calendar.getTime();

    // when
    List<DurationReportResult> result = historyService
        .createHistoricProcessInstanceReport()
        .startedAfter(after)
        .startedBefore(before)
        .duration(QUARTER);

    // then
    assertThat(result).matches(assertion);
  }

  public void testReportWithExcludingConditions() {
    // given
    deployment(createProcessWithUserTask("process"));

    runtimeService.startProcessInstanceByKey("process");
    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);

    Calendar hourAgo = Calendar.getInstance();
    hourAgo.add(Calendar.HOUR_OF_DAY, -1);

    Calendar hourFromNow = Calendar.getInstance();
    hourFromNow.add(Calendar.HOUR_OF_DAY, 1);

    // when
    List<DurationReportResult> result = historyService
        .createHistoricProcessInstanceReport()
        .startedAfter(hourFromNow.getTime())
        .startedBefore(hourAgo.getTime())
        .duration(MONTH);

    // then
    assertEquals(0, result.size());
  }

  public void testReportByProcessDefinitionIdByMonth() {
    // given
    deployment(createProcessWithUserTask("process1"), createProcessWithUserTask("process2"));

    String processDefinitionId1 = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("process1")
        .singleResult()
        .getId();

    DurationReportResultAssertion assertion = createReportScenario()
        .periodUnit(MONTH)
        .startAndCompleteProcessInstance("process1", 2016, 1, 15, 10, 0) // 15.02.2016 10:00
        .done();

    createReportScenario()
        .startAndCompleteProcessInstance("process2", 2016, 3, 15, 10, 0) // 15.04.2016 10:00
        .done();

    // when
    List<DurationReportResult> result = historyService
        .createHistoricProcessInstanceReport()
        .processDefinitionIdIn(processDefinitionId1)
        .duration(MONTH);

    // then
    assertThat(result).matches(assertion);
  }

  public void testReportByProcessDefinitionIdByQuarter() {
    // given
    deployment(createProcessWithUserTask("process1"), createProcessWithUserTask("process2"));

    String processDefinitionId1 = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("process1")
        .singleResult()
        .getId();

    DurationReportResultAssertion assertion = createReportScenario()
        .periodUnit(QUARTER)
        .startAndCompleteProcessInstance("process1", 2016, 1, 15, 10, 0) // 15.02.2016 10:00
        .done();

    createReportScenario()
        .startAndCompleteProcessInstance("process2", 2016, 3, 15, 10, 0) // 15.04.2016 10:00
        .done();

    // when
    List<DurationReportResult> result = historyService
        .createHistoricProcessInstanceReport()
        .processDefinitionIdIn(processDefinitionId1)
        .duration(QUARTER);

    // then
    assertThat(result).matches(assertion);
  }

  public void testReportByMultipleProcessDefinitionIdByMonth() {
    // given
    deployment(createProcessWithUserTask("process1"), createProcessWithUserTask("process2"));

    String processDefinitionId1 = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("process1")
        .singleResult()
        .getId();

    String processDefinitionId2 = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("process2")
        .singleResult()
        .getId();

    DurationReportResultAssertion assertion = createReportScenario()
        .periodUnit(MONTH)
        .startAndCompleteProcessInstance("process1", 2016, 1, 15, 10, 0) // 15.02.2016 10:00
        .startAndCompleteProcessInstance("process2", 2016, 3, 15, 10, 0) // 15.04.2016 10:00
        .done();

    // when
    List<DurationReportResult> result = historyService
        .createHistoricProcessInstanceReport()
        .processDefinitionIdIn(processDefinitionId1, processDefinitionId2)
        .duration(MONTH);

    // then
    assertThat(result).matches(assertion);
  }

  public void testReportByMultipleProcessDefinitionIdByQuarter() {
    // given
    deployment(createProcessWithUserTask("process1"), createProcessWithUserTask("process2"));

    String processDefinitionId1 = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("process1")
        .singleResult()
        .getId();

    String processDefinitionId2 = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("process2")
        .singleResult()
        .getId();

    DurationReportResultAssertion assertion = createReportScenario()
        .periodUnit(QUARTER)
        .startAndCompleteProcessInstance("process1", 2016, 1, 15, 10, 0) // 15.02.2016 10:00
        .startAndCompleteProcessInstance("process2", 2016, 3, 15, 10, 0) // 15.04.2016 10:00
        .done();

    // when
    List<DurationReportResult> result = historyService
        .createHistoricProcessInstanceReport()
        .processDefinitionIdIn(processDefinitionId1, processDefinitionId2)
        .duration(QUARTER);

    // then
    assertThat(result).matches(assertion);
  }

  public void testReportByInvalidProcessDefinitionId() {
    HistoricProcessInstanceReport report = historyService.createHistoricProcessInstanceReport();

    try {
      report.processDefinitionIdIn((String) null);
    } catch (NotValidException e) {}

    try {
      report.processDefinitionIdIn("abc", (String) null, "def");
    } catch (NotValidException e) {}
  }

  public void testReportByProcessDefinitionKeyByMonth() {
    // given
    deployment(createProcessWithUserTask("process1"), createProcessWithUserTask("process2"));

    DurationReportResultAssertion assertion = createReportScenario()
        .periodUnit(MONTH)
        .startAndCompleteProcessInstance("process1", 2016, 1, 15, 10, 0) // 15.02.2016 10:00
        .done();

    createReportScenario()
        .startAndCompleteProcessInstance("process2", 2016, 3, 15, 10, 0) // 15.04.2016 10:00
        .done();

    // when
    List<DurationReportResult> result = historyService
        .createHistoricProcessInstanceReport()
        .processDefinitionKeyIn("process1")
        .duration(MONTH);

    // then
    assertThat(result).matches(assertion);
  }

  public void testReportByProcessDefinitionKeyByQuarter() {
    // given
    deployment(createProcessWithUserTask("process1"), createProcessWithUserTask("process2"));

    DurationReportResultAssertion assertion = createReportScenario()
        .periodUnit(QUARTER)
        .startAndCompleteProcessInstance("process1", 2016, 1, 15, 10, 0) // 15.02.2016 10:00
        .done();

    createReportScenario()
        .startAndCompleteProcessInstance("process2", 2016, 3, 15, 10, 0) // 15.04.2016 10:00
        .done();

    // when
    List<DurationReportResult> result = historyService
        .createHistoricProcessInstanceReport()
        .processDefinitionKeyIn("process1")
        .duration(QUARTER);

    // then
    assertThat(result).matches(assertion);
  }

  public void testReportByMultipleProcessDefinitionKeyByMonth() {
    // given
    deployment(createProcessWithUserTask("process1"), createProcessWithUserTask("process2"));

    DurationReportResultAssertion assertion = createReportScenario()
        .periodUnit(MONTH)
        .startAndCompleteProcessInstance("process1", 2016, 1, 15, 10, 0) // 15.02.2016 10:00
        .startAndCompleteProcessInstance("process2", 2016, 3, 15, 10, 0) // 15.04.2016 10:00
        .done();

    // when
    List<DurationReportResult> result = historyService
        .createHistoricProcessInstanceReport()
        .processDefinitionKeyIn("process1", "process2")
        .duration(MONTH);

    // then
    assertThat(result).matches(assertion);
  }

  public void testReportByMultipleProcessDefinitionKeyByQuarter() {
    // given
    deployment(createProcessWithUserTask("process1"), createProcessWithUserTask("process2"));

    DurationReportResultAssertion assertion = createReportScenario()
        .periodUnit(QUARTER)
        .startAndCompleteProcessInstance("process1", 2016, 1, 15, 10, 0) // 15.02.2016 10:00
        .startAndCompleteProcessInstance("process2", 2016, 3, 15, 10, 0) // 15.04.2016 10:00
        .done();

    // when
    List<DurationReportResult> result = historyService
        .createHistoricProcessInstanceReport()
        .processDefinitionKeyIn("process1", "process2")
        .duration(QUARTER);

    // then
    assertThat(result).matches(assertion);
  }

  public void testReportByInvalidProcessDefinitionKey() {
    HistoricProcessInstanceReport report = historyService.createHistoricProcessInstanceReport();

    try {
      report.processDefinitionKeyIn((String) null);
    } catch (NotValidException e) {}

    try {
      report.processDefinitionKeyIn("abc", (String) null, "def");
    } catch (NotValidException e) {}
  }

  protected BpmnModelInstance createProcessWithUserTask(String key) {
    return Bpmn.createExecutableProcess(key)
      .startEvent()
      .userTask()
      .endEvent()
    .done();
  }

  protected class DurationReportScenarioBuilder {

    protected PeriodUnit periodUnit = MONTH;

    protected DurationReportResultAssertion assertion = new DurationReportResultAssertion();

    public DurationReportScenarioBuilder periodUnit(PeriodUnit periodUnit) {
      this.periodUnit = periodUnit;
      assertion.setPeriodUnit(periodUnit);
      return this;
    }

    protected void setCurrentTime(int year, int month, int dayOfMonth, int hourOfDay, int minute) {
      Calendar calendar = Calendar.getInstance();
      calendar.set(year, month, dayOfMonth, hourOfDay, minute);
      ClockUtil.setCurrentTime(calendar.getTime());
    }

    protected void addToCalendar(int field, int month) {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(ClockUtil.getCurrentTime());
      calendar.add(field, month);
      ClockUtil.setCurrentTime(calendar.getTime());
    }

    public DurationReportScenarioBuilder startAndCompleteProcessInstance(String key, int year, int month, int dayOfMonth, int hourOfDay, int minute) {
      setCurrentTime(year, month, dayOfMonth, hourOfDay, minute);

      ProcessInstance pi = runtimeService.startProcessInstanceByKey(key);

      int period = month;
      if (periodUnit == QUARTER) {
        period = month / 3;
      }
      assertion.addDurationReportResult(period+1, pi.getId());

      addToCalendar(Calendar.MONTH, 5);
      addToCalendar(Calendar.SECOND, random.nextInt(60));
      Task task = taskService.createTaskQuery()
          .processInstanceId(pi.getId())
          .singleResult();
      taskService.complete(task.getId());

      return this;
    }

    public DurationReportResultAssertion done() {
      return assertion;
    }

  }

  protected class DurationReportResultAssertion {

    protected PeriodUnit periodUnit = MONTH;
    protected Map<Integer, Set<String>> periodToProcessInstancesMap = new HashMap<Integer, Set<String>>();

    public DurationReportResultAssertion addDurationReportResult(int period, String processInstanceId) {
      Set<String> processInstances = periodToProcessInstancesMap.get(period);
      if (processInstances == null) {
        processInstances = new HashSet<String>();
        periodToProcessInstancesMap.put(period, processInstances);
      }
      processInstances.add(processInstanceId);
      return this;
    }

    public DurationReportResultAssertion setPeriodUnit(PeriodUnit periodUnit) {
      this.periodUnit = periodUnit;
      return this;
    }

    public void assertReportResults(List<DurationReportResult> actual) {
      assertEquals("Report size", periodToProcessInstancesMap.size(), actual.size());

      for (DurationReportResult reportResult : actual) {
        assertEquals("Period unit", periodUnit, reportResult.getPeriodUnit());

        int period = reportResult.getPeriod();
        Set<String> processInstancesInPeriod = periodToProcessInstancesMap.get(period);
        assertNotNull("Unexpected report for period " + period, processInstancesInPeriod);

        List<HistoricProcessInstance> historicProcessInstances = historyService
            .createHistoricProcessInstanceQuery()
            .processInstanceIds(processInstancesInPeriod)
            .finished()
            .list();

        long max = 0;
        long min = 0;
        long sum = 0;

        for (int i = 0; i < historicProcessInstances.size(); i++) {
          HistoricProcessInstance historicProcessInstance = historicProcessInstances.get(i);
          Long duration = historicProcessInstance.getDurationInMillis();
          sum = sum + duration;
          max = i > 0 ? Math.max(max, duration) : duration;
          min = i > 0 ? Math.min(min, duration) : duration;
        }

        long avg = sum / historicProcessInstances.size();

        assertEquals("maximum", max, reportResult.getMaximum());
        assertEquals("minimum", min, reportResult.getMinimum());
        assertEquals("average", avg, reportResult.getAverage(), 1);
      }
    }

  }

  protected class DurationReportResultAssert {

    protected List<DurationReportResult> actual;

    public DurationReportResultAssert(List<DurationReportResult> actual) {
      this.actual = actual;
    }

    public void matches(DurationReportResultAssertion assertion) {
      assertion.assertReportResults(actual);
    }

  }

  protected DurationReportScenarioBuilder createReportScenario() {
    return new DurationReportScenarioBuilder();
  }

  protected DurationReportResultAssert assertThat(List<DurationReportResult> actual) {
    return new DurationReportResultAssert(actual);
  }

}
