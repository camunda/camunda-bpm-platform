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
package org.camunda.bpm.engine.test.standalone.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.FilterService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
public class HistoricTaskInstanceQueryOrTest {

  @Rule
  public ProcessEngineRule processEngineRule = new ProvidedProcessEngineRule();

  protected HistoryService historyService;
  protected RuntimeService runtimeService;
  protected TaskService taskService;
  protected CaseService caseService;
  protected RepositoryService repositoryService;
  protected FilterService filterService;

  @Before
  public void init() {
    historyService = processEngineRule.getHistoryService();
    runtimeService = processEngineRule.getRuntimeService();
    taskService = processEngineRule.getTaskService();
    caseService = processEngineRule.getCaseService();
    repositoryService = processEngineRule.getRepositoryService();
    filterService = processEngineRule.getFilterService();
  }

  @After
  public void tearDown() {
    for (org.camunda.bpm.engine.repository.Deployment deployment:
      repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }

    for (HistoricTaskInstance task: historyService.createHistoricTaskInstanceQuery().list()) {
      taskService.deleteTask(task.getId(), true);
    }
  }

  @Test
  public void shouldThrowExceptionByMissingStartOr() {

    // when/then
    assertThatThrownBy(() -> historyService.createHistoricTaskInstanceQuery()
        .or()
        .endOr()
        .endOr())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage: cannot set endOr() before or()");
  }

  @Test
  public void shouldThrowExceptionByNesting() {

    // when/then
    assertThatThrownBy(() -> historyService.createHistoricTaskInstanceQuery()
      .or()
        .or()
        .endOr()
      .endOr()
      .or()
      .endOr())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage: cannot set or() within 'or' query");
  }

  @Test
  public void shouldThrowExceptionByWithCandidateGroupsApplied() {

    // when/then
    assertThatThrownBy(() -> historyService.createHistoricTaskInstanceQuery()
      .or()
        .withCandidateGroups()
      .endOr())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage: cannot set withCandidateGroups() within 'or' query");
  }

  @Test
  public void shouldThrowExceptionByWithoutCandidateGroupsApplied() {

    // when/then
    assertThatThrownBy(() -> historyService.createHistoricTaskInstanceQuery()
      .or()
        .withoutCandidateGroups()
      .endOr())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage: cannot set withoutCandidateGroups() within 'or' query");
  }

  @Test
  public void shouldThrowExceptionOnOrderByTenantId() {

    // when/then
    assertThatThrownBy(() -> historyService.createHistoricTaskInstanceQuery()
      .or()
        .orderByTenantId()
      .endOr())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage: cannot set orderByTenantId() within 'or' query");
  }

  @Test
  public void shouldThrowExceptionOnOrderByTaskId() {

    // when/then
    assertThatThrownBy(() -> historyService.createHistoricTaskInstanceQuery()
      .or()
        .orderByTaskId()
      .endOr())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage: cannot set orderByTaskId() within 'or' query");

  }

  @Test
  public void shouldThrowExceptionOnOrderByHistoricActivityInstanceId() {

    // when/then
    assertThatThrownBy(() -> historyService.createHistoricTaskInstanceQuery()
      .or()
        .orderByHistoricActivityInstanceId()
      .endOr())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage: cannot set orderByHistoricActivityInstanceId() within 'or' query");
  }

  @Test
  public void shouldThrowExceptionOnOrderByProcessDefinitionId() {

    // when/then
    assertThatThrownBy(() -> historyService.createHistoricTaskInstanceQuery()
      .or()
        .orderByProcessDefinitionId()
      .endOr())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage: cannot set orderByProcessDefinitionId() within 'or' query");
  }

  @Test
  public void shouldThrowExceptionOnOrderByProcessInstanceId() {

    // when/then
    assertThatThrownBy(() -> historyService.createHistoricTaskInstanceQuery()
      .or()
        .orderByProcessInstanceId()
      .endOr())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage: cannot set orderByProcessInstanceId() within 'or' query");
  }

  @Test
  public void shouldThrowExceptionOnOrderByExecutionId() {

    // when/then
    assertThatThrownBy(() -> historyService.createHistoricTaskInstanceQuery()
      .or()
        .orderByExecutionId()
      .endOr())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage: cannot set orderByExecutionId() within 'or' query");
  }

  @Test
  public void shouldThrowExceptionOnOrderByHistoricTaskInstanceDuration() {

    // when/then
    assertThatThrownBy(() -> historyService.createHistoricTaskInstanceQuery()
      .or()
        .orderByHistoricTaskInstanceDuration()
      .endOr())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage: cannot set orderByHistoricTaskInstanceDuration() within 'or' query");
  }

  @Test
  public void shouldThrowExceptionOnOrderByHistoricTaskInstanceEndTime() {

    // when/then
    assertThatThrownBy(() -> historyService.createHistoricTaskInstanceQuery()
      .or()
        .orderByHistoricTaskInstanceEndTime()
      .endOr())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage: cannot set orderByHistoricTaskInstanceEndTime() within 'or' query");
  }

  @Test
  public void shouldThrowExceptionOnOrderByHistoricActivityInstanceStartTime() {

    // when/then
    assertThatThrownBy(() -> historyService.createHistoricTaskInstanceQuery()
      .or()
        .orderByHistoricTaskInstanceEndTime()
      .endOr())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage: cannot set orderByHistoricTaskInstanceEndTime() within 'or' query");
  }

  @Test
  public void shouldThrowExceptionOnOrderByTaskName() {

    // when/then
    assertThatThrownBy(() -> historyService.createHistoricTaskInstanceQuery()
      .or()
        .orderByTaskName()
      .endOr())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage: cannot set orderByTaskName() within 'or' query");
  }

  @Test
  public void shouldThrowExceptionOnOrderByTaskDescription() {

    // when/then
    assertThatThrownBy(() -> historyService.createHistoricTaskInstanceQuery()
      .or()
        .orderByTaskDescription()
      .endOr())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage: cannot set orderByTaskDescription() within 'or' query");
  }

  @Test
  public void shouldThrowExceptionOnOrderByTaskAssignee() {

    // when/then
    assertThatThrownBy(() -> historyService.createHistoricTaskInstanceQuery()
      .or()
        .orderByTaskAssignee()
      .endOr())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage: cannot set orderByTaskAssignee() within 'or' query");
  }

  @Test
  public void shouldThrowExceptionOnOrderByTaskOwner() {

    // when/then
    assertThatThrownBy(() -> historyService.createHistoricTaskInstanceQuery()
      .or()
        .orderByTaskOwner()
      .endOr())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage: cannot set orderByTaskOwner() within 'or' query");
  }

  @Test
  public void shouldThrowExceptionOnOrderByTaskDueDate() {

    // when/then
    assertThatThrownBy(() -> historyService.createHistoricTaskInstanceQuery()
      .or()
        .orderByTaskDueDate()
      .endOr())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage: cannot set orderByTaskDueDate() within 'or' query");
  }

  @Test
  public void shouldThrowExceptionOnOrderByTaskFollowUpDate() {

    // when/then
    assertThatThrownBy(() -> historyService.createHistoricTaskInstanceQuery()
      .or()
        .orderByTaskFollowUpDate()
      .endOr())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage: cannot set orderByTaskFollowUpDate() within 'or' query");
  }

  @Test
  public void shouldThrowExceptionOnOrderByDeleteReason() {

    // when/then
    assertThatThrownBy(() -> historyService.createHistoricTaskInstanceQuery()
      .or()
        .orderByDeleteReason()
      .endOr())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage: cannot set orderByDeleteReason() within 'or' query");
  }

  @Test
  public void shouldThrowExceptionOnOrderByTaskDefinitionKey() {

    // when/then
    assertThatThrownBy(() -> historyService.createHistoricTaskInstanceQuery()
      .or()
        .orderByTaskDefinitionKey()
      .endOr())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage: cannot set orderByTaskDefinitionKey() within 'or' query");

  }

  @Test
  public void shouldThrowExceptionOnOrderByTaskPriority() {

    // when/then
    assertThatThrownBy(() -> historyService.createHistoricTaskInstanceQuery()
      .or()
        .orderByTaskPriority()
      .endOr())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage: cannot set orderByTaskPriority() within 'or' query");
  }

  @Test
  public void shouldThrowExceptionOnOrderByCaseDefinitionId() {

    // when/then
    assertThatThrownBy(() -> historyService.createHistoricTaskInstanceQuery()
      .or()
        .orderByCaseDefinitionId()
      .endOr())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage: cannot set orderByCaseDefinitionId() within 'or' query");
  }

  @Test
  public void shouldThrowExceptionOnOrderByCaseInstanceId() {

    // when/then
    assertThatThrownBy(() -> historyService.createHistoricTaskInstanceQuery()
      .or()
        .orderByCaseInstanceId()
      .endOr())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage: cannot set orderByCaseInstanceId() within 'or' query");
  }

  @Test
  public void shouldThrowExceptionOnOrderByCaseExecutionId() {

    // when/then
    assertThatThrownBy(() -> historyService.createHistoricTaskInstanceQuery()
      .or()
        .orderByCaseExecutionId()
      .endOr())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage: cannot set orderByCaseExecutionId() within 'or' query");
  }

  @Test
  public void shouldReturnHistoricTasksWithEmptyOrQuery() {
    // given
    taskService.saveTask(taskService.newTask());
    taskService.saveTask(taskService.newTask());

    // when
    List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
      .or()
      .endOr()
      .list();

    // then
    assertEquals(2, tasks.size());
  }

  @Test
  public void shouldReturnHistoricTasksWithTaskNameOrTaskDescription() {
    // given
    Task task1 = taskService.newTask();
    task1.setName("aTaskName");
    taskService.saveTask(task1);

    Task task2 = taskService.newTask();
    task2.setDescription("aTaskDescription");
    taskService.saveTask(task2);

    // when
    List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
      .or()
        .taskName("aTaskName")
        .taskDescription("aTaskDescription")
      .endOr()
      .list();

    // then
    assertEquals(2, tasks.size());
  }

  @Test
  public void shouldReturnHistoricTasksWithMultipleOrCriteria() {
    // given
    Task task1 = taskService.newTask();
    task1.setName("aTaskName");
    taskService.saveTask(task1);

    Task task2 = taskService.newTask();
    task2.setDescription("aTaskDescription");
    taskService.saveTask(task2);

    Task task3 = taskService.newTask();
    taskService.saveTask(task3);

    Task task4 = taskService.newTask();
    task4.setPriority(5);
    taskService.saveTask(task4);

    Task task5 = taskService.newTask();
    task5.setOwner("aTaskOwner");
    taskService.saveTask(task5);

    // when
    List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
      .or()
        .taskName("aTaskName")
        .taskDescription("aTaskDescription")
        .taskId(task3.getId())
        .taskPriority(5)
        .taskOwner("aTaskOwner")
      .endOr()
      .list();

    // then
    assertEquals(5, tasks.size());
  }

  @Test
  public void shouldReturnHistoricTasksFilteredByMultipleOrAndCriteria() {
    // given
    Task task1 = taskService.newTask();
    task1.setPriority(4);
    taskService.saveTask(task1);

    Task task2 = taskService.newTask();
    task2.setName("aTaskName");
    task2.setOwner("aTaskOwner");
    task2.setAssignee("aTaskAssignee");
    task2.setPriority(4);
    taskService.saveTask(task2);

    Task task3 = taskService.newTask();
    task3.setName("aTaskName");
    task3.setOwner("aTaskOwner");
    task3.setAssignee("aTaskAssignee");
    task3.setPriority(4);
    task3.setDescription("aTaskDescription");
    taskService.saveTask(task3);

    Task task4 = taskService.newTask();
    task4.setOwner("aTaskOwner");
    task4.setAssignee("aTaskAssignee");
    task4.setPriority(4);
    task4.setDescription("aTaskDescription");
    taskService.saveTask(task4);

    Task task5 = taskService.newTask();
    task5.setDescription("aTaskDescription");
    task5.setOwner("aTaskOwner");
    taskService.saveTask(task5);

    // when
    List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
      .or()
        .taskName("aTaskName")
        .taskDescription("aTaskDescription")
        .taskId(task3.getId())
      .endOr()
      .taskOwner("aTaskOwner")
      .taskPriority(4)
      .taskAssignee("aTaskAssignee")
      .list();

    // then
    assertEquals(3, tasks.size());
  }

  @Test
  public void shouldReturnHistoricTasksFilteredByMultipleOrQueries() {
    // given
    Task task1 = taskService.newTask();
    task1.setName("aTaskName");
    taskService.saveTask(task1);

    Task task2 = taskService.newTask();
    task2.setName("aTaskName");
    task2.setDescription("aTaskDescription");
    taskService.saveTask(task2);

    Task task3 = taskService.newTask();
    task3.setName("aTaskName");
    task3.setDescription("aTaskDescription");
    task3.setOwner("aTaskOwner");
    taskService.saveTask(task3);

    Task task4 = taskService.newTask();
    task4.setName("aTaskName");
    task4.setDescription("aTaskDescription");
    task4.setOwner("aTaskOwner");
    task4.setAssignee("aTaskAssignee");
    taskService.saveTask(task4);

    Task task5 = taskService.newTask();
    task5.setName("aTaskName");
    task5.setDescription("aTaskDescription");
    task5.setOwner("aTaskOwner");
    task5.setAssignee("aTaskAssignee");
    task5.setPriority(4);
    taskService.saveTask(task5);

    Task task6 = taskService.newTask();
    task6.setName("aTaskName");
    task6.setDescription("aTaskDescription");
    task6.setOwner("aTaskOwner");
    task6.setAssignee("aTaskAssignee");
    task6.setPriority(4);
    taskService.saveTask(task6);

    // when
    List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
      .or()
        .taskName("aTaskName")
        .taskDescription("aTaskDescription")
      .endOr()
      .or()
        .taskName("aTaskName")
        .taskDescription("aTaskDescription")
        .taskAssignee("aTaskAssignee")
      .endOr()
      .or()
        .taskName("aTaskName")
        .taskDescription("aTaskDescription")
        .taskOwner("aTaskOwner")
        .taskAssignee("aTaskAssignee")
      .endOr()
      .or()
        .taskAssignee("aTaskAssignee")
        .taskPriority(4)
      .endOr()
      .list();

    // then
    assertEquals(3, tasks.size());
  }

  @Test
  public void shouldReturnHistoricTasksWhereSameCriterionWasAppliedThreeTimesInOneQuery() {
    // given
    Task task1 = taskService.newTask();
    task1.setName("task1");
    taskService.saveTask(task1);

    Task task2 = taskService.newTask();
    task2.setName("task2");
    taskService.saveTask(task2);

    Task task3 = taskService.newTask();
    task3.setName("task3");
    taskService.saveTask(task3);

    // when
    List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
      .or()
        .taskName("task1")
        .taskName("task2")
        .taskName("task3")
      .endOr()
      .list();

    // then
    assertEquals(1, tasks.size());
  }

  @Test
  public void shouldReturnHistoricTasksWithActivityInstanceIdInOrTaskId() {
    // given
    BpmnModelInstance aProcessDefinition = Bpmn.createExecutableProcess("aProcessDefinition")
      .startEvent()
        .userTask()
      .endEvent()
      .done();

    repositoryService
      .createDeployment()
      .addModelInstance("foo.bpmn", aProcessDefinition)
      .deploy();

    ProcessInstance processInstance1 = runtimeService
      .startProcessInstanceByKey("aProcessDefinition");

    String activityInstanceId = runtimeService.getActivityInstance(processInstance1.getId())
      .getChildActivityInstances()[0].getId();

    Task task2 = taskService.newTask();
    taskService.saveTask(task2);

    // when
    List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
      .or()
        .activityInstanceIdIn(activityInstanceId)
        .taskId(task2.getId())
      .endOr()
      .list();

    // then
    assertEquals(2, tasks.size());
  }

  @Test
  public void shouldTestDueDateCombinations() throws ParseException {
    HashMap<String, Date> dates = createFollowUpAndDueDateTasks();
    taskService.saveTask(taskService.newTask());

    assertEquals(2, historyService.createHistoricTaskInstanceQuery()
      .or()
        .taskDueDate(dates.get("date"))
        .taskDueBefore(dates.get("oneHourAgo"))
      .endOr()
      .count());

    assertEquals(3, historyService.createHistoricTaskInstanceQuery()
        .or()
          .taskDueDate(dates.get("date"))
          .taskDueBefore(dates.get("oneHourAgo"))
          .withoutTaskDueDate()
        .endOr()
        .count());

    assertEquals(2, historyService.createHistoricTaskInstanceQuery()
      .or()
        .taskDueDate(dates.get("date"))
        .taskDueAfter(dates.get("oneHourLater"))
      .endOr()
      .count());

    assertEquals(3, historyService.createHistoricTaskInstanceQuery()
        .or()
          .taskDueDate(dates.get("date"))
          .taskDueAfter(dates.get("oneHourLater"))
          .withoutTaskDueDate()
        .endOr()
        .count());

    assertEquals(2, historyService.createHistoricTaskInstanceQuery()
      .or()
        .taskDueBefore(dates.get("oneHourAgo"))
        .taskDueAfter(dates.get("oneHourLater"))
      .endOr()
      .count());

    assertEquals(3, historyService.createHistoricTaskInstanceQuery()
        .or()
          .taskDueBefore(dates.get("oneHourAgo"))
          .taskDueAfter(dates.get("oneHourLater"))
          .withoutTaskDueDate()
        .endOr()
        .count());

    assertEquals(3, historyService.createHistoricTaskInstanceQuery()
      .or()
        .taskDueBefore(dates.get("oneHourLater"))
        .taskDueAfter(dates.get("oneHourAgo"))
      .endOr()
      .count());

    assertEquals(4, historyService.createHistoricTaskInstanceQuery()
        .or()
          .taskDueBefore(dates.get("oneHourLater"))
          .taskDueAfter(dates.get("oneHourAgo"))
          .withoutTaskDueDate()
        .endOr()
        .count());

    assertEquals(3, historyService.createHistoricTaskInstanceQuery()
      .or()
        .taskDueDate(dates.get("date"))
        .taskDueBefore(dates.get("oneHourAgo"))
        .taskDueAfter(dates.get("oneHourLater"))
      .endOr()
      .count());

    assertEquals(4, historyService.createHistoricTaskInstanceQuery()
        .or()
          .taskDueDate(dates.get("date"))
          .taskDueBefore(dates.get("oneHourAgo"))
          .taskDueAfter(dates.get("oneHourLater"))
          .withoutTaskDueDate()
        .endOr()
        .count());
  }

  @Test
  public void shouldTestFollowUpDateCombinations() throws ParseException {
    HashMap<String, Date> dates = createFollowUpAndDueDateTasks();

    assertEquals(2, historyService.createHistoricTaskInstanceQuery()
      .or()
        .taskFollowUpDate(dates.get("date"))
        .taskFollowUpBefore(dates.get("oneHourAgo"))
      .endOr()
      .count());

    assertEquals(2, historyService.createHistoricTaskInstanceQuery()
      .or()
        .taskFollowUpDate(dates.get("date"))
        .taskFollowUpAfter(dates.get("oneHourLater"))
      .endOr()
      .count());

    assertEquals(2, historyService.createHistoricTaskInstanceQuery()
      .or()
        .taskFollowUpBefore(dates.get("oneHourAgo"))
        .taskFollowUpAfter(dates.get("oneHourLater"))
      .endOr()
      .count());

    assertEquals(3, historyService.createHistoricTaskInstanceQuery()
      .or()
        .taskFollowUpBefore(dates.get("oneHourLater"))
        .taskFollowUpAfter(dates.get("oneHourAgo"))
      .endOr()
      .count());

    assertEquals(3, historyService.createHistoricTaskInstanceQuery()
      .or()
        .taskFollowUpDate(dates.get("date"))
        .taskFollowUpBefore(dates.get("oneHourAgo"))
        .taskFollowUpAfter(dates.get("oneHourLater"))
      .endOr()
      .count());

    // followUp before or null
    taskService.saveTask(taskService.newTask());

    assertEquals(4, historyService.createHistoricTaskInstanceQuery().count());
  }

  @Test
  public void shouldQueryStartedBeforeOrAfter() {
    // given
    Date dateOne = new Date(1363607000000L);
    ClockUtil.setCurrentTime(dateOne);

    Task taskOne = taskService.newTask();
    taskService.saveTask(taskOne);

    Date dateTwo = new Date(dateOne.getTime() + 7000000);
    ClockUtil.setCurrentTime(dateTwo);

    Task taskTwo = taskService.newTask();
    taskService.saveTask(taskTwo);

    // when
    List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
        .or()
          .startedBefore(new Date(dateOne.getTime() + 1000))
          .startedAfter(new Date(dateTwo.getTime() - 1000))
        .endOr()
        .list();

    // then
    assertThat(tasks.size()).isEqualTo(2);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void shouldQueryStandaloneOrEmbeddedTaskByProcessDefinitionKey() {
    // given
    Task taskOne = taskService.newTask();
    taskService.saveTask(taskOne);

    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
        .or()
          .taskId(taskOne.getId())
          .processDefinitionKey("oneTaskProcess")
        .endOr()
        .list();

    // then
    assertThat(tasks.size()).isEqualTo(2);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void shouldQueryStandaloneOrEmbeddedTaskByProcessInstanceId() {
    // given
    Task taskOne = taskService.newTask();
    taskService.saveTask(taskOne);

    runtimeService.startProcessInstanceByKey("oneTaskProcess", "aBusinessKey");

    // when
    List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
        .or()
          .taskId(taskOne.getId())
          .processInstanceBusinessKey("aBusinessKey")
        .endOr()
        .list();

    // then
    assertThat(tasks.size()).isEqualTo(2);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn")
  public void shouldQueryStandaloneOrEmbeddedTaskByCaseDefinitionId() {
    // given
    Task taskOne = taskService.newTask();
    taskService.saveTask(taskOne);

    caseService.createCaseInstanceByKey("oneTaskCase");

    // when
    List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
        .or()
          .taskId(taskOne.getId())
          .caseDefinitionKey("oneTaskCase")
        .endOr()
        .list();

    // then
    assertThat(tasks.size()).isEqualTo(2);
  }

  @Test
  public void shouldQueryFinishedBeforeOrAfter() {
    // given
    Date dateOne = new Date(1363607000000L);
    ClockUtil.setCurrentTime(dateOne);

    Task taskOne = taskService.newTask();
    taskService.saveTask(taskOne);
    taskService.complete(taskOne.getId());

    Date dateTwo = new Date(dateOne.getTime() + 7000000);
    ClockUtil.setCurrentTime(dateTwo);

    Task taskTwo = taskService.newTask();
    taskService.saveTask(taskTwo);
    taskService.complete(taskTwo.getId());

    // when
    List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
        .or()
          .finishedBefore(new Date(dateOne.getTime() + 1000))
          .finishedAfter(new Date(dateTwo.getTime() - 1000))
        .endOr()
        .list();

    // then
    assertThat(tasks.size()).isEqualTo(2);
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void shouldReturnHistoricTasksWithHadCandidateUserOrHadCandidateGroup() {
    // given
    Task task1 = taskService.newTask();
    taskService.saveTask(task1);
    taskService.addCandidateUser(task1.getId(), "USER_TEST");

    Task task2 = taskService.newTask();
    taskService.saveTask(task2);
    taskService.addCandidateGroup(task2.getId(), "GROUP_TEST");

    // when
    List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
      .or()
        .taskHadCandidateUser("USER_TEST")
        .taskHadCandidateGroup("GROUP_TEST")
      .endOr()
      .list();

    // then
    assertThat(tasks).hasSize(2);
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void shouldReturnHistoricTasksWithCandidateCandidateUserInvolvedOrCandidateGroupInvolved() {
    // given
    Task task1 = taskService.newTask();
    taskService.saveTask(task1);
    taskService.addCandidateUser(task1.getId(), "USER_TEST");

    Task task2 = taskService.newTask();
    taskService.saveTask(task2);
    taskService.addCandidateGroup(task2.getId(), "GROUP_TEST");

    // when
    List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
      .or()
        .taskInvolvedUser("USER_TEST")
        .taskInvolvedGroup("GROUP_TEST")
      .endOr()
      .list();

    // then
    assertThat(tasks).hasSize(2);
  }

  public HashMap<String, Date> createFollowUpAndDueDateTasks() throws ParseException {
    final Date date = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse("27/07/2017 01:12:13"),
      oneHourAgo = new Date(date.getTime() - 60 * 60 * 1000),
      oneHourLater = new Date(date.getTime() + 60 * 60 * 1000);

    Task taskDueBefore = taskService.newTask();
    taskDueBefore.setFollowUpDate(new Date(oneHourAgo.getTime() - 1000));
    taskDueBefore.setDueDate(new Date(oneHourAgo.getTime() - 1000));
    taskService.saveTask(taskDueBefore);

    Task taskDueDate = taskService.newTask();
    taskDueDate.setFollowUpDate(date);
    taskDueDate.setDueDate(date);
    taskService.saveTask(taskDueDate);

    Task taskDueAfter = taskService.newTask();
    taskDueAfter.setFollowUpDate(new Date(oneHourLater.getTime() + 1000));
    taskDueAfter.setDueDate(new Date(oneHourLater.getTime() + 1000));
    taskService.saveTask(taskDueAfter);

    assertEquals(3, historyService.createHistoricTaskInstanceQuery().count());

    return new HashMap<String, Date>() {{
      put("date", date);
      put("oneHourAgo", oneHourAgo);
      put("oneHourLater", oneHourLater);
    }};
  }
}