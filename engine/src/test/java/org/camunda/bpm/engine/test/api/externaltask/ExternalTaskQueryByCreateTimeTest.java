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

package org.camunda.bpm.engine.test.api.externaltask;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ClockTestUtil;
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
public class ExternalTaskQueryByCreateTimeTest {

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain chain = RuleChain.outerRule(engineRule).around(testHelper);

  protected ProcessEngine engine;

  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;
  protected ManagementService managementService;
  protected HistoryService historyService;
  protected TaskService taskService;
  protected CaseService caseService;

  @Before
  public void setup() {
    engine = engineRule.getProcessEngine();
    repositoryService = engineRule.getRepositoryService();
    runtimeService = engineRule.getRuntimeService();
    managementService = engineRule.getManagementService();
    historyService = engineRule.getHistoryService();
    taskService = engineRule.getTaskService();
    caseService = engineRule.getCaseService();

    // given four process definitions with one external task each, external tasks have priorities 4, 3, 0, and 0
    deployProcessesWithExternalTasks();
  }

  @After
  public void tearDown() {
    ClockUtil.reset();
  }

  @Test
  public void shouldHaveNonNullCreateTime() {
    // given
    runtimeService.startProcessInstanceByKey("process1");

    // when
    var result = engineRule.getExternalTaskService()
        .createExternalTaskQuery()
        .list();

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getCreateTime()).isNotNull();
  }

  @Test
  public void shouldProduceEventWithCreateTimeValue() {
    // given
    runtimeService.startProcessInstanceByKey("process1");

    var extTask = engineRule.getExternalTaskService()
        .createExternalTaskQuery()
        .singleResult();

    // when
    var result = historyService.createHistoricExternalTaskLogQuery().list();

    // then
    assertThat(result.size()).isEqualTo(1);

    var historyEventTimestamp = result.get(0).getTimestamp();

    assertThat(extTask.getCreateTime()).isEqualTo(historyEventTimestamp);
  }

  @Test
  public void shouldReturnTasksInDescOrder() {
    // given
    startProcessInstanceAfter("process1", 1);
    startProcessInstanceAfter("process2", 1);

    // when
    var result = engineRule.getExternalTaskService()
        .createExternalTaskQuery()

        .orderByCreateTime().desc()
        .list();

    // then
    assertThat(result.size()).isEqualTo(2);

    var extTask1 = result.get(0);
    var extTask2 = result.get(1);

    assertThat(extTask2.getCreateTime())
        .isBefore(extTask1.getCreateTime());
  }

  @Test
  public void shouldReturnTasksInAscOrder() {
    // given
    startProcessInstanceAfter("process1", 1);
    startProcessInstanceAfter("process2", 1);

    // when
    var result = engineRule.getExternalTaskService()
        .createExternalTaskQuery()

        .orderByCreateTime().asc()
        .list();

    // then
    assertThat(result.size()).isEqualTo(2);

    var extTask1 = result.get(0);
    var extTask2 = result.get(1);

    assertThat(extTask1.getCreateTime())
        .isBefore(extTask2.getCreateTime());
  }

  // Multi-Level Sorting with CreateTime & Priority

  @Test
  public void shouldReturnTasksInCreateTimeAscOrderOnPriorityEquality() {
    // given
    startProcessInstanceAfter("process1", 1);
    startProcessInstanceAfter("process2", 1);
    startProcessInstanceAfter("process3", 1);
    startProcessInstanceAfter("process4", 1);

    // when
    var result = engineRule.getExternalTaskService()
        .createExternalTaskQuery()

        .orderByPriority().desc()
        .orderByCreateTime().asc()

        .list();

    // then
    assertThat(result.size()).isEqualTo(4);

    assertThat(result.get(0).getActivityId()).isEqualTo("task1");
    assertThat(result.get(1).getActivityId()).isEqualTo("task2");
    assertThat(result.get(2).getActivityId()).isEqualTo("task3");
    assertThat(result.get(3).getActivityId()).isEqualTo("task4");
  }

  @Test
  public void shouldReturnTasksInCreateTimeDescOrderOnPriorityEquality() {
    // given
    startProcessInstanceAfter("process1", 1);
    startProcessInstanceAfter("process2", 1);
    startProcessInstanceAfter("process3", 1);
    startProcessInstanceAfter("process4", 1);

    // when
    var result = engineRule.getExternalTaskService()
        .createExternalTaskQuery()

        .orderByPriority().desc()
        .orderByCreateTime().desc()

        .list();

    // then
    assertThat(result.size()).isEqualTo(4);

    assertThat(result.get(0).getActivityId()).isEqualTo("task1"); // due to priority DESC
    assertThat(result.get(1).getActivityId()).isEqualTo("task2");
    assertThat(result.get(2).getActivityId()).isEqualTo("task4"); // due to CreateTime DESC
    assertThat(result.get(3).getActivityId()).isEqualTo("task3");
  }

  @Test
  public void shouldReturnTasksInPriorityAscOnCreateTimeEquality() {
    var now = ClockTestUtil.setClockToDateWithoutMilliseconds();

    // given
    startProcessInstanceWithDate("process1", now);
    startProcessInstanceWithDate("process2", now);

    startProcessInstanceAfter("process3", 1);
    startProcessInstanceAfter("process4", 1);

    // when
    var result = engineRule.getExternalTaskService()
        .createExternalTaskQuery()

        .orderByCreateTime().asc()
        .orderByPriority().asc()

        .list();

    // then
    assertThat(result.size()).isEqualTo(4);

    assertThat(result.get(0).getActivityId()).isEqualTo("task2"); // due to CreateTime Equality, priority ASC
    assertThat(result.get(1).getActivityId()).isEqualTo("task1");

    assertThat(result.get(2).getActivityId()).isEqualTo("task3");
    assertThat(result.get(3).getActivityId()).isEqualTo("task4");
  }

  @Test
  public void shouldReturnTasksInPriorityDescOnCreateTimeEquality() {
    var now = ClockTestUtil.setClockToDateWithoutMilliseconds();

    // given
    startProcessInstanceWithDate("process1", now);
    startProcessInstanceWithDate("process2", now);

    startProcessInstanceAfter("process3", 1);
    startProcessInstanceAfter("process4", 1);

    // when
    var result = engineRule.getExternalTaskService()
        .createExternalTaskQuery()

        .orderByCreateTime().asc()
        .orderByPriority().desc()

        .list();

    // then
    assertThat(result.size()).isEqualTo(4);

    assertThat(result.get(0).getActivityId()).isEqualTo("task1"); // due to CreateTime equality, priority DESC
    assertThat(result.get(1).getActivityId()).isEqualTo("task2");

    assertThat(result.get(2).getActivityId()).isEqualTo("task3");
    assertThat(result.get(3).getActivityId()).isEqualTo("task4");
  }

  private void deployProcessesWithExternalTasks() {
    var process1 = createProcessWithTask("process1", "task1", "topic1", "4");
    var process2 = createProcessWithTask("process2", "task2", "topic2", "3");
    var process3 = createProcessWithTask("process3", "task3", "topic3", "0");
    var process4 = createProcessWithTask("process4", "task4", "topic4", "0");

    testHelper.deploy(process1, process2, process3, process4);
  }

  private void startProcessInstanceWithDate(String processKey, Date fixedDate) {
    ClockUtil.setCurrentTime(fixedDate);
    runtimeService.startProcessInstanceByKey(processKey);
  }

  private void startProcessInstanceAfter(String processKey, long minutes) {
    ClockTestUtil.incrementClock(minutes * 60_000);
    runtimeService.startProcessInstanceByKey(processKey);
  }

  private BpmnModelInstance createProcessWithTask(String processId, String taskId, String topic, String priority) {
    return Bpmn.createExecutableProcess(processId)
        .startEvent()
        .serviceTask(taskId).camundaExternalTask(topic).camundaTaskPriority(priority)
        .endEvent()
        .done();
  }
}
