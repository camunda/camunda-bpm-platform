/* Licensed under the Apache License, Version 2.0 (the "License");
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

package org.camunda.bpm.engine.test.api.task;

import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.FilterService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.TaskQueryImpl;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Tassilo Weidner
 */
public class TaskQueryOrTest {

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  protected RuntimeService runtimeService;
  protected TaskService taskService;
  protected CaseService caseService;
  protected RepositoryService repositoryService;
  protected FilterService filterService;

  @Before
  public void init() {
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

    for (Task task: taskService.createTaskQuery().list()) {
      taskService.deleteTask(task.getId(), true);
    }
  }

  @Test
  public void shouldThrowExceptionByMissingStartOr() {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set endOr() before or()");

    taskService.createTaskQuery()
      .or()
      .endOr()
      .endOr();
  }

  @Test
  public void shouldThrowExceptionByNesting() {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set or() within 'or' query");

    taskService.createTaskQuery()
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

    taskService.createTaskQuery()
      .or()
        .withCandidateGroups()
      .endOr();
  }

  @Test
  public void shouldThrowExceptionByWithoutCandidateGroupsApplied() {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set withoutCandidateGroups() within 'or' query");

    taskService.createTaskQuery()
      .or()
        .withoutCandidateGroups()
      .endOr();
  }

  @Test
  public void shouldThrowExceptionByWithCandidateUsersApplied() {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set withCandidateUsers() within 'or' query");

    taskService.createTaskQuery()
      .or()
        .withCandidateUsers()
      .endOr();
  }

  @Test
  public void shouldThrowExceptionByWithoutCandidateUsersApplied() {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set withoutCandidateUsers() within 'or' query");

    taskService.createTaskQuery()
      .or()
        .withoutCandidateUsers()
      .endOr();
  }

  @Test
  public void shouldThrowExceptionByOrderingApplied() {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set orderByCaseExecutionId() within 'or' query");

    taskService.createTaskQuery()
      .or()
        .orderByCaseExecutionId()
      .endOr();
  }

  @Test
  public void shouldThrowExceptionByInitializeFormKeysInOrQuery() {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Invalid query usage: cannot set initializeFormKeys() within 'or' query");

    taskService.createTaskQuery()
      .or()
        .initializeFormKeys()
      .endOr();
  }

  @Test
  public void shouldReturnNoTasksWithTaskCandidateUserAndOrTaskCandidateGroup() {
    // given
    Task task1 = taskService.newTask();
    taskService.saveTask(task1);
    taskService.addCandidateUser(task1.getId(), "aCandidateUser");

    Task task2 = taskService.newTask();
    taskService.saveTask(task2);
    taskService.addCandidateGroup(task2.getId(), "aCandidateGroup");

    // when
    List<Task> tasks = taskService.createTaskQuery()
      .taskCandidateUser("aCandidateUser")
      .or()
        .taskCandidateGroup("aCandidateGroup")
      .endOr()
      .list();

    // then
    assertEquals(0, tasks.size());
  }

  @Test
  public void shouldReturnTasksWithEmptyOrQuery() {
    // given
    taskService.saveTask(taskService.newTask());
    taskService.saveTask(taskService.newTask());

    // when
    List<Task> tasks = taskService.createTaskQuery()
      .or()
      .endOr()
      .list();

    // then
    assertEquals(2, tasks.size());
  }

  @Test
  public void shouldReturnTasksWithTaskCandidateUserOrTaskCandidateGroup() {
    // given
    Task task1 = taskService.newTask();
    taskService.saveTask(task1);
    taskService.addCandidateUser(task1.getId(), "John Doe");

    Task task2 = taskService.newTask();
    taskService.saveTask(task2);
    taskService.addCandidateGroup(task2.getId(), "Controlling");

    // when
    List<Task> tasks = taskService.createTaskQuery()
      .or()
        .taskCandidateUser("John Doe")
        .taskCandidateGroup("Controlling")
      .endOr()
      .list();

    // then
    assertEquals(2, tasks.size());
  }

  @Test
  public void shouldReturnTasksWithTaskCandidateUserOrTaskCandidateGroupWithIncludeAssignedTasks() {
    // given
    Task task1 = taskService.newTask();
    taskService.saveTask(task1);
    taskService.addCandidateUser(task1.getId(), "John Doe");
    taskService.setAssignee(task1.getId(), "John Doe");

    Task task2 = taskService.newTask();
    taskService.saveTask(task2);
    taskService.addCandidateGroup(task2.getId(), "Controlling");

    // when
    List<Task> tasks = taskService.createTaskQuery()
      .or()
        .taskCandidateUser("John Doe")
        .taskCandidateGroup("Controlling")
        .includeAssignedTasks()
      .endOr()
      .list();

    // then
    assertEquals(2, tasks.size());
  }

  @Test
  public void shouldReturnTasksWithTaskCandidateUserOrTaskCandidateGroupIn() {
    // given
    Task task1 = taskService.newTask();
    taskService.saveTask(task1);
    taskService.addCandidateUser(task1.getId(), "John Doe");

    Task task2 = taskService.newTask();
    taskService.saveTask(task2);
    taskService.addCandidateGroup(task2.getId(), "Controlling");

    Task task3 = taskService.newTask();
    taskService.saveTask(task3);
    taskService.addCandidateGroup(task3.getId(), "Sales");

    // when
    List<Task> tasks = taskService.createTaskQuery()
      .or()
        .taskCandidateUser("John Doe")
        .taskCandidateGroupIn(Arrays.asList("Controlling", "Sales"))
      .endOr()
      .list();

    // then
    assertEquals(3, tasks.size());
  }

  @Test
  public void shouldReturnTasksWithTaskCandidateGroupOrTaskCandidateGroupIn() {
    // given
    Task task1 = taskService.newTask();
    taskService.saveTask(task1);
    taskService.addCandidateGroup(task1.getId(), "Accounting");

    Task task2 = taskService.newTask();
    taskService.saveTask(task2);
    taskService.addCandidateGroup(task2.getId(), "Controlling");

    Task task3 = taskService.newTask();
    taskService.saveTask(task3);
    taskService.addCandidateGroup(task3.getId(), "Sales");

    // when
    List<Task> tasks = taskService.createTaskQuery()
      .or()
        .taskCandidateGroup("Accounting")
        .taskCandidateGroupIn(Arrays.asList("Controlling", "Sales"))
      .endOr()
      .list();

    // then
    assertEquals(3, tasks.size());
  }

  @Test
  public void shouldReturnTasksWithTaskNameOrTaskDescription() {
    // given
    Task task1 = taskService.newTask();
    task1.setName("aTaskName");
    taskService.saveTask(task1);

    Task task2 = taskService.newTask();
    task2.setDescription("aTaskDescription");
    taskService.saveTask(task2);

    // when
    List<Task> tasks = taskService.createTaskQuery()
      .or()
        .taskName("aTaskName")
        .taskDescription("aTaskDescription")
      .endOr()
      .list();

    // then
    assertEquals(2, tasks.size());
  }

  @Test
  public void shouldReturnTasksWithMultipleOrCriteria() {
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
    List<Task> tasks = taskService.createTaskQuery()
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
  public void shouldReturnTasksFilteredByMultipleOrAndCriteria() {
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
    List<Task> tasks = taskService.createTaskQuery()
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
  public void shouldReturnTasksFilteredByMultipleOrQueries() {
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
    List<Task> tasks = taskService.createTaskQuery()
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
  public void shouldReturnTasksWhereSameCriterionWasAppliedThreeTimesInOneQuery() {
    // given
    Task task1 = taskService.newTask();
    taskService.saveTask(task1);
    taskService.addCandidateGroup(task1.getId(), "Accounting");

    Task task2 = taskService.newTask();
    taskService.saveTask(task2);
    taskService.addCandidateGroup(task2.getId(), "Controlling");

    Task task3 = taskService.newTask();
    taskService.saveTask(task3);
    taskService.addCandidateGroup(task3.getId(), "Sales");

    // when
    List<Task> tasks = taskService.createTaskQuery()
      .or()
        .taskCandidateGroup("Accounting")
        .taskCandidateGroup("Controlling")
        .taskCandidateGroup("Sales")
      .endOr()
      .list();

    // then
    assertEquals(1, tasks.size());
  }

  @Test
  public void shouldReturnTasksWithTaskVariableValueEqualsOrTaskVariableValueGreaterThan() {
    // given
    Task task1 = taskService.newTask();
    taskService.saveTask(task1);
    taskService.setVariable(task1.getId(),"aLongValue", 789L);

    Task task2 = taskService.newTask();
    taskService.saveTask(task2);
    taskService.setVariable(task2.getId(),"anEvenLongerValue", 1000L);

    // when
    TaskQuery query = taskService.createTaskQuery()
      .or()
        .taskVariableValueEquals("aLongValue", 789L)
        .taskVariableValueGreaterThan("anEvenLongerValue", 999L)
      .endOr();

    // then
    assertEquals(2, query.count());
  }

  @Test
  public void shouldInitializeFormKeys() {
    // given
    BpmnModelInstance aProcessDefinition = Bpmn.createExecutableProcess("aProcessDefinition")
      .startEvent()
        .userTask()
          .camundaFormKey("aFormKey")
      .endEvent()
      .done();

    repositoryService
      .createDeployment()
      .addModelInstance("foo.bpmn", aProcessDefinition)
      .deploy();

    ProcessInstance processInstance1 = runtimeService
      .startProcessInstanceByKey("aProcessDefinition");

    BpmnModelInstance anotherProcessDefinition = Bpmn.createExecutableProcess("anotherProcessDefinition")
      .startEvent()
        .userTask()
          .camundaFormKey("anotherFormKey")
      .endEvent()
      .done();

    repositoryService
      .createDeployment()
      .addModelInstance("foo.bpmn", anotherProcessDefinition)
      .deploy();

    ProcessInstance processInstance2 = runtimeService
      .startProcessInstanceByKey("anotherProcessDefinition");

    // when
    List<Task> tasks = taskService.createTaskQuery()
      .or()
        .processDefinitionId(processInstance1.getProcessDefinitionId())
        .processInstanceId(processInstance2.getId())
      .endOr()
      .initializeFormKeys()
      .list();

    // then
    assertEquals(2, tasks.size());
    assertEquals("aFormKey", tasks.get(0).getFormKey());
    assertEquals("anotherFormKey", tasks.get(1).getFormKey());
  }

  @Test
  public void shouldReturnTasksWithProcessDefinitionNameOrProcessDefinitionKey() {
    // given
    BpmnModelInstance aProcessDefinition = Bpmn.createExecutableProcess("aProcessDefinition")
      .name("process1")
      .startEvent()
        .userTask()
      .endEvent()
      .done();

    repositoryService
      .createDeployment()
      .addModelInstance("foo.bpmn", aProcessDefinition)
      .deploy();

    runtimeService.startProcessInstanceByKey("aProcessDefinition");

    BpmnModelInstance anotherProcessDefinition = Bpmn.createExecutableProcess("anotherProcessDefinition")
      .startEvent()
        .userTask()
      .endEvent()
      .done();

     repositoryService
       .createDeployment()
       .addModelInstance("foo.bpmn", anotherProcessDefinition)
       .deploy();

    runtimeService.startProcessInstanceByKey("anotherProcessDefinition");

    // when
    List<Task> tasks = taskService.createTaskQuery()
      .or()
        .processDefinitionName("process1")
        .processDefinitionKey("anotherProcessDefinition")
      .endOr()
      .list();

    // then
    assertEquals(2, tasks.size());
  }

  @Test
  public void shouldReturnTasksWithProcessInstanceBusinessKeyOrProcessInstanceBusinessKeyLike() {
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

    runtimeService
      .startProcessInstanceByKey("aProcessDefinition", "aBusinessKey");

    BpmnModelInstance anotherProcessDefinition = Bpmn.createExecutableProcess("anotherProcessDefinition")
      .startEvent()
        .userTask()
      .endEvent()
      .done();

     repositoryService
       .createDeployment()
       .addModelInstance("foo.bpmn", anotherProcessDefinition)
       .deploy();

    runtimeService
      .startProcessInstanceByKey("anotherProcessDefinition", "anotherBusinessKey");

    // when
    List<Task> tasks = taskService.createTaskQuery()
      .or()
        .processInstanceBusinessKey("aBusinessKey")
        .processInstanceBusinessKeyLike("anotherBusinessKey")
      .endOr()
      .list();

    // then
    assertEquals(2, tasks.size());
  }

  @Test
  @Deployment(resources={
    "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn",
    "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase2.cmmn"})
  public void shouldReturnTasksWithCaseDefinitionKeyCaseDefinitionName() {
    // given
    String caseDefinitionId1 = repositoryService
      .createCaseDefinitionQuery()
      .caseDefinitionKey("oneTaskCase")
      .singleResult()
      .getId();

    caseService
      .withCaseDefinition(caseDefinitionId1)
      .create();

    String caseDefinitionId2 = repositoryService
      .createCaseDefinitionQuery()
      .caseDefinitionKey("oneTaskCase2")
      .singleResult()
      .getId();

    caseService
      .withCaseDefinition(caseDefinitionId2)
      .create();

    // when
    List<Task> tasks = taskService.createTaskQuery()
      .or()
        .caseDefinitionKey("oneTaskCase")
        .caseDefinitionName("One")
      .endOr()
      .list();

    // then
    assertEquals(2, tasks.size());
  }

  @Test
  @Deployment(resources={
    "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn",
    "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase2.cmmn"})
  public void shouldReturnTasksWithCaseInstanceBusinessKeyOrCaseInstanceBusinessKeyLike() {
    // given
    String caseDefinitionId1 = repositoryService
      .createCaseDefinitionQuery()
      .caseDefinitionKey("oneTaskCase")
      .singleResult()
      .getId();

    CaseInstance caseInstance1 = caseService
      .withCaseDefinition(caseDefinitionId1)
      .businessKey("aBusinessKey")
      .create();

    String caseDefinitionId2 = repositoryService
      .createCaseDefinitionQuery()
      .caseDefinitionKey("oneTaskCase2")
      .singleResult()
      .getId();

    CaseInstance caseInstance2 = caseService
      .withCaseDefinition(caseDefinitionId2)
      .businessKey("anotherBusinessKey")
      .create();

    // when
    List<Task> tasks = taskService.createTaskQuery()
      .or()
        .caseInstanceBusinessKey(caseInstance1.getBusinessKey())
        .caseInstanceBusinessKeyLike(caseInstance2.getBusinessKey())
      .endOr()
      .list();

    // then
    assertEquals(2, tasks.size());
  }

  @Test
  public void shouldReturnTasksWithActivityInstanceIdInOrTaskId() {
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
    List<Task> tasks = taskService.createTaskQuery()
      .or()
        .activityInstanceIdIn(activityInstanceId)
        .taskId(task2.getId())
      .endOr()
      .list();

    // then
    assertEquals(2, tasks.size());
  }

  @Test
  public void shouldReturnTasksByExtendingQuery_OrInExtendingQuery() {
    // given
    TaskQuery extendedQuery = taskService.createTaskQuery()
      .taskCandidateGroup("sales");

    TaskQuery extendingQuery = taskService.createTaskQuery()
      .or()
        .taskName("aTaskName")
      .endOr()
      .or()
        .taskNameLike("anotherTaskName")
      .endOr();

    // when
    TaskQueryImpl result =  (TaskQueryImpl)((TaskQueryImpl)extendedQuery).extend(extendingQuery);

    // then
    assertEquals("sales", result.getCandidateGroup());
    assertEquals("aTaskName", result.getQueries().get(1).getName());
    assertEquals("anotherTaskName", result.getQueries().get(2).getNameLike());
  }

  @Test
  public void shouldReturnTasksByExtendingQuery_OrInExtendedQuery() {
    // given
    TaskQuery extendedQuery = taskService.createTaskQuery()
      .or()
        .taskName("aTaskName")
      .endOr()
      .or()
        .taskNameLike("anotherTaskName")
      .endOr();

    TaskQuery extendingQuery = taskService.createTaskQuery()
      .taskCandidateGroup("aCandidateGroup");

    // when
    TaskQueryImpl result =  (TaskQueryImpl)((TaskQueryImpl)extendedQuery).extend(extendingQuery);

    // then
    assertEquals("aTaskName", result.getQueries().get(1).getName());
    assertEquals("anotherTaskName", result.getQueries().get(2).getNameLike());
    assertEquals("aCandidateGroup", result.getCandidateGroup());
  }

  @Test
  public void shouldReturnTasksByExtendingQuery_OrInBothExtendedAndExtendingQuery() {
    // given
    TaskQuery extendedQuery = taskService.createTaskQuery()
      .or()
        .taskName("aTaskName")
      .endOr()
      .or()
        .taskNameLike("anotherTaskName")
      .endOr();

    TaskQuery extendingQuery = taskService.createTaskQuery()
      .or()
        .taskCandidateGroup("aCandidateGroup")
      .endOr()
      .or()
        .taskCandidateUser("aCandidateUser")
      .endOr();

    // when
    TaskQueryImpl result =  (TaskQueryImpl)((TaskQueryImpl)extendedQuery).extend(extendingQuery);

    // then
    assertEquals("aTaskName", result.getQueries().get(1).getName());
    assertEquals("anotherTaskName", result.getQueries().get(2).getNameLike());
    assertEquals("aCandidateGroup", result.getQueries().get(3).getCandidateGroup());
    assertEquals("aCandidateUser", result.getQueries().get(4).getCandidateUser());
  }

  @Test
  public void shouldTestDueDateCombinations() throws ParseException {
    HashMap<String, Date> dates = createFollowUpAndDueDateTasks();

    assertEquals(2, taskService.createTaskQuery()
      .or()
        .dueDate(dates.get("date"))
        .dueBefore(dates.get("oneHourAgo"))
      .endOr()
      .count());

    assertEquals(2, taskService.createTaskQuery()
      .or()
        .dueDate(dates.get("date"))
        .dueAfter(dates.get("oneHourLater"))
      .endOr()
      .count());

    assertEquals(2, taskService.createTaskQuery()
      .or()
        .dueBefore(dates.get("oneHourAgo"))
        .dueAfter(dates.get("oneHourLater"))
      .endOr()
      .count());

    assertEquals(3, taskService.createTaskQuery()
      .or()
        .dueBefore(dates.get("oneHourLater"))
        .dueAfter(dates.get("oneHourAgo"))
      .endOr()
      .count());

    assertEquals(3, taskService.createTaskQuery()
      .or()
        .dueDate(dates.get("date"))
        .dueBefore(dates.get("oneHourAgo"))
        .dueAfter(dates.get("oneHourLater"))
      .endOr()
      .count());
  }

  @Test
  public void shouldTestFollowUpDateCombinations() throws ParseException {
    HashMap<String, Date> dates = createFollowUpAndDueDateTasks();

    assertEquals(2, taskService.createTaskQuery()
      .or()
        .followUpDate(dates.get("date"))
        .followUpBefore(dates.get("oneHourAgo"))
      .endOr()
      .count());

    assertEquals(2, taskService.createTaskQuery()
      .or()
        .followUpDate(dates.get("date"))
        .followUpAfter(dates.get("oneHourLater"))
      .endOr()
      .count());

    assertEquals(2, taskService.createTaskQuery()
      .or()
        .followUpBefore(dates.get("oneHourAgo"))
        .followUpAfter(dates.get("oneHourLater"))
      .endOr()
      .count());

    assertEquals(3, taskService.createTaskQuery()
      .or()
        .followUpBefore(dates.get("oneHourLater"))
        .followUpAfter(dates.get("oneHourAgo"))
      .endOr()
      .count());

    assertEquals(3, taskService.createTaskQuery()
      .or()
        .followUpDate(dates.get("date"))
        .followUpBefore(dates.get("oneHourAgo"))
        .followUpAfter(dates.get("oneHourLater"))
      .endOr()
      .count());

    // followUp before or null
    taskService.saveTask(taskService.newTask());

    assertEquals(4, taskService.createTaskQuery().count());

    assertEquals(3, taskService.createTaskQuery()
      .or()
        .followUpDate(dates.get("date"))
        .followUpBeforeOrNotExistent(dates.get("oneHourAgo"))
      .endOr()
      .count());

    assertEquals(3, taskService.createTaskQuery()
      .or()
        .followUpBeforeOrNotExistent(dates.get("oneHourAgo"))
        .followUpAfter(dates.get("oneHourLater"))
      .endOr()
      .count());

    assertEquals(4, taskService.createTaskQuery()
      .or()
        .followUpBeforeOrNotExistent(dates.get("oneHourLater"))
        .followUpAfter(dates.get("oneHourAgo"))
      .endOr()
      .count());

    assertEquals(4, taskService.createTaskQuery()
      .or()
        .followUpDate(dates.get("date"))
        .followUpBeforeOrNotExistent(dates.get("oneHourAgo"))
        .followUpAfter(dates.get("oneHourLater"))
      .endOr()
      .count());
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

    assertEquals(3, taskService.createTaskQuery().count());

    return new HashMap<String, Date>() {{
      put("date", date);
      put("oneHourAgo", oneHourAgo);
      put("oneHourLater", oneHourLater);
    }};
  }

}
