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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.FilterService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.List;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.history.HistoricTaskInstance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
public class HistoricTaskInstanceQueryOrTest {

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

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
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set endOr() before or()");

    historyService.createHistoricTaskInstanceQuery()
      .or()
      .endOr()
      .endOr();
  }

  @Test
  public void shouldThrowExceptionByNesting() {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set or() within 'or' query");

    historyService.createHistoricTaskInstanceQuery()
      .or()
        .or()
        .endOr()
      .endOr()
      .or()
      .endOr();
  }

  @Test
  public void shouldThrowExceptionByWithCandidateGroupsApplied() {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set withCandidateGroups() within 'or' query");

    historyService.createHistoricTaskInstanceQuery()
      .or()
        .withCandidateGroups()
      .endOr();
  }

  @Test
  public void shouldThrowExceptionByWithoutCandidateGroupsApplied() {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set withoutCandidateGroups() within 'or' query");

    historyService.createHistoricTaskInstanceQuery()
      .or()
        .withoutCandidateGroups()
      .endOr();
  }

  @Test
  public void shouldThrowExceptionOnOrderByTenantId() {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set orderByTenantId() within 'or' query");

    historyService.createHistoricTaskInstanceQuery()
      .or()
        .orderByTenantId()
      .endOr();
  }

  @Test
  public void shouldThrowExceptionOnOrderByTaskId() {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set orderByTaskId() within 'or' query");

    historyService.createHistoricTaskInstanceQuery()
        .or()
          .orderByTaskId()
        .endOr();
  }

  @Test
  public void shouldThrowExceptionOnOrderByHistoricActivityInstanceId() {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set orderByHistoricActivityInstanceId() within 'or' query");

    historyService.createHistoricTaskInstanceQuery()
        .or()
          .orderByHistoricActivityInstanceId()
        .endOr();
  }

  @Test
  public void shouldThrowExceptionOnOrderByProcessDefinitionId() {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set orderByProcessDefinitionId() within 'or' query");

    historyService.createHistoricTaskInstanceQuery()
        .or()
          .orderByProcessDefinitionId()
        .endOr();
  }

  @Test
  public void shouldThrowExceptionOnOrderByProcessInstanceId() {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set orderByProcessInstanceId() within 'or' query");

    historyService.createHistoricTaskInstanceQuery()
        .or()
          .orderByProcessInstanceId()
        .endOr();
  }

  @Test
  public void shouldThrowExceptionOnOrderByExecutionId() {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set orderByExecutionId() within 'or' query");

    historyService.createHistoricTaskInstanceQuery()
        .or()
          .orderByExecutionId()
        .endOr();
  }

  @Test
  public void shouldThrowExceptionOnOrderByHistoricTaskInstanceDuration() {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set orderByHistoricTaskInstanceDuration() within 'or' query");

    historyService.createHistoricTaskInstanceQuery()
        .or()
          .orderByHistoricTaskInstanceDuration()
        .endOr();
  }

  @Test
  public void shouldThrowExceptionOnOrderByHistoricTaskInstanceEndTime() {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set orderByHistoricTaskInstanceEndTime() within 'or' query");

    historyService.createHistoricTaskInstanceQuery()
        .or()
          .orderByHistoricTaskInstanceEndTime()
        .endOr();
  }

  @Test
  public void shouldThrowExceptionOnOrderByHistoricActivityInstanceStartTime() {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set orderByHistoricTaskInstanceEndTime() within 'or' query");

    historyService.createHistoricTaskInstanceQuery()
        .or()
          .orderByHistoricTaskInstanceEndTime()
        .endOr();
  }

  @Test
  public void shouldThrowExceptionOnOrderByTaskName() {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set orderByTaskName() within 'or' query");

    historyService.createHistoricTaskInstanceQuery()
        .or()
          .orderByTaskName()
        .endOr();
  }

  @Test
  public void shouldThrowExceptionOnOrderByTaskDescription() {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set orderByTaskDescription() within 'or' query");

    historyService.createHistoricTaskInstanceQuery()
        .or()
          .orderByTaskDescription()
        .endOr();
  }

  @Test
  public void shouldThrowExceptionOnOrderByTaskAssignee() {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set orderByTaskAssignee() within 'or' query");

    historyService.createHistoricTaskInstanceQuery()
        .or()
          .orderByTaskAssignee()
        .endOr();
  }

  @Test
  public void shouldThrowExceptionOnOrderByTaskOwner() {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set orderByTaskOwner() within 'or' query");

    historyService.createHistoricTaskInstanceQuery()
        .or()
          .orderByTaskOwner()
        .endOr();
  }

  @Test
  public void shouldThrowExceptionOnOrderByTaskDueDate() {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set orderByTaskDueDate() within 'or' query");

    historyService.createHistoricTaskInstanceQuery()
        .or()
          .orderByTaskDueDate()
        .endOr();
  }

  @Test
  public void shouldThrowExceptionOnOrderByTaskFollowUpDate() {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set orderByTaskFollowUpDate() within 'or' query");

    historyService.createHistoricTaskInstanceQuery()
        .or()
          .orderByTaskFollowUpDate()
        .endOr();
  }

  @Test
  public void shouldThrowExceptionOnOrderByDeleteReason() {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set orderByDeleteReason() within 'or' query");

    historyService.createHistoricTaskInstanceQuery()
        .or()
          .orderByDeleteReason()
        .endOr();
  }

  @Test
  public void shouldThrowExceptionOnOrderByTaskDefinitionKey() {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set orderByTaskDefinitionKey() within 'or' query");

    historyService.createHistoricTaskInstanceQuery()
        .or()
          .orderByTaskDefinitionKey()
        .endOr();
  }

  @Test
  public void shouldThrowExceptionOnOrderByTaskPriority() {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set orderByTaskPriority() within 'or' query");

    historyService.createHistoricTaskInstanceQuery()
        .or()
          .orderByTaskPriority()
        .endOr();
  }

  @Test
  public void shouldThrowExceptionOnOrderByCaseDefinitionId() {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set orderByCaseDefinitionId() within 'or' query");

    historyService.createHistoricTaskInstanceQuery()
        .or()
          .orderByCaseDefinitionId()
        .endOr();
  }

  @Test
  public void shouldThrowExceptionOnOrderByCaseInstanceId() {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set orderByCaseInstanceId() within 'or' query");

    historyService.createHistoricTaskInstanceQuery()
        .or()
          .orderByCaseInstanceId()
        .endOr();
  }

  @Test
  public void shouldThrowExceptionOnOrderByCaseExecutionId() {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set orderByCaseExecutionId() within 'or' query");

    historyService.createHistoricTaskInstanceQuery()
        .or()
          .orderByCaseExecutionId()
        .endOr();
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

    assertEquals(2, historyService.createHistoricTaskInstanceQuery()
      .or()
        .taskDueDate(dates.get("date"))
        .taskDueBefore(dates.get("oneHourAgo"))
      .endOr()
      .count());

    assertEquals(2, historyService.createHistoricTaskInstanceQuery()
      .or()
        .taskDueDate(dates.get("date"))
        .taskDueAfter(dates.get("oneHourLater"))
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
        .taskDueBefore(dates.get("oneHourLater"))
        .taskDueAfter(dates.get("oneHourAgo"))
      .endOr()
      .count());

    assertEquals(3, historyService.createHistoricTaskInstanceQuery()
      .or()
        .taskDueDate(dates.get("date"))
        .taskDueBefore(dates.get("oneHourAgo"))
        .taskDueAfter(dates.get("oneHourLater"))
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