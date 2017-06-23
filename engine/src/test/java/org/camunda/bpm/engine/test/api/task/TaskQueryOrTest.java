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

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Tassilo Weidner
 */
public class TaskQueryOrTest {

  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule();

  protected RuntimeService runtimeService;
  protected TaskService taskService;
  protected CaseService caseService;
  protected RepositoryService repositoryService;
  protected FilterService filterService;

  @Before
  public void init() {
    runtimeService = rule.getRuntimeService();
    taskService = rule.getTaskService();
    caseService = rule.getCaseService();
    repositoryService = rule.getRepositoryService();
    filterService = rule.getFilterService();
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
    try {
      taskService.createTaskQuery()
        .startOr()
          .taskName("aTaskName")
        .endOr()
        .endOr();
      fail("expected exception");
    } catch (ProcessEngineException e) { }
  }

  @Test
  public void shouldThrowExceptionByNesting() {
    try {
      taskService.createTaskQuery()
        .startOr()
          .taskName("aTaskName")
          .startOr()
            .taskName("anotherTaskName")
          .endOr()
        .endOr()
        .startOr()
          .taskName("aTaskName")
        .endOr();
      fail("expected exception");
    } catch (ProcessEngineException e) { }
  }

  @Test
  public void shouldThrowExceptionByEmptyOrQuery() {
    try {
      taskService.createTaskQuery()
        .startOr()
          .taskName("aTaskName")
        .endOr()
        .startOr()
        .endOr();
      fail("expected exception");
    } catch (ProcessEngineException e) { }
  }

  @Test
  public void shouldThrowExceptionByWithCandidateGroupsApplied() {
    try {
      taskService.createTaskQuery()
        .startOr()
          .taskName("aTaskName")
        .endOr()
        .startOr()
          .withCandidateGroups()
        .endOr();
      fail("expected exception");
    } catch (ProcessEngineException e) { }
  }

  @Test
  public void shouldThrowExceptionByWithoutCandidateGroupsApplied() {
    try {
      taskService.createTaskQuery()
        .startOr()
          .taskName("aTaskName")
        .endOr()
        .startOr()
          .withoutCandidateGroups()
        .endOr();
      fail("expected exception");
    } catch (ProcessEngineException e) { }
  }

  @Test
  public void shouldThrowExceptionByWithCandidateUsersApplied() {
    try {
      taskService.createTaskQuery()
        .startOr()
          .taskName("aTaskName")
        .endOr()
        .startOr()
          .withCandidateUsers()
        .endOr();
      fail("expected exception");
    } catch (ProcessEngineException e) { }
  }

  @Test
  public void shouldThrowExceptionByWithoutCandidateUsersApplied() {
    try {
      taskService.createTaskQuery()
        .startOr()
          .taskName("aTaskName")
        .endOr()
        .startOr()
          .withoutCandidateUsers()
        .endOr();
      fail("expected exception");
    } catch (ProcessEngineException e) { }
  }

  @Test
  public void shouldThrowExceptionByOrderingApplied() {
    try {
      taskService.createTaskQuery()
        .startOr()
          .taskName("aTaskName")
        .endOr()
        .startOr()
          .taskName("Task")
          .orderByCaseExecutionId()
        .endOr();
      fail("expected exception");
    } catch (ProcessEngineException e) { }
  }

  @Test
  public void shouldThrowExceptionByInitializeFormKeysInOrQuery() {
    try {
      taskService.createTaskQuery()
        .startOr()
          .initializeFormKeys()
        .endOr();
      fail("expected exception");
    } catch (ProcessEngineException e) { }
  }

  @Test
  public void shouldReturnNoTasksWithTaskCandidateUserAndOrTaskCandidateGroup() {
    // given
    Task task1 = taskService.newTask();
    taskService.saveTask(task1);
    taskService.addCandidateUser(task1.getId(), "aCandidateUser");

    Task task2 = taskService.newTask();
    taskService.saveTask(task2);
    taskService.addCandidateGroup(task1.getId(), "aCandidateGroup");

    // when
    List<Task> tasks = taskService.createTaskQuery()
      .taskCandidateUser("aCandidateUser")
      .startOr()
        .taskCandidateGroup("aCandidateGroup")
      .endOr()
      .list();

    // then
    assertEquals(0, tasks.size());
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
      .startOr()
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
      .startOr()
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
      .startOr()
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
      .startOr()
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
      .startOr()
        .taskName("aTaskName")
        .taskDescription("aTaskDescription")
      .endOr()
      .list();

    // then
    assertEquals(2, tasks.size());
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
      .startOr()
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
      .startOr()
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
      .startOr()
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
      .startOr()
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
      .startOr()
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
      .startOr()
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
      .startOr()
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
      .startOr()
        .taskName("aTaskName")
      .endOr()
      .startOr()
        .taskNameLike("anotherTaskName")
      .endOr();

    // when
    TaskQueryImpl result =  (TaskQueryImpl)((TaskQueryImpl)extendedQuery).extend(extendingQuery);

    // then
    assertEquals("sales", result.getCandidateGroup());
    assertEquals("aTaskName", result.getOrQueries().get(0).getName());
    assertEquals("anotherTaskName", result.getOrQueries().get(1).getNameLike());
  }

  @Test
  public void shouldReturnTasksByExtendingQuery_OrInExtendedQuery() {
    // given
    TaskQuery extendedQuery = taskService.createTaskQuery()
      .startOr()
        .taskName("aTaskName")
      .endOr()
      .startOr()
        .taskNameLike("anotherTaskName")
      .endOr();

    TaskQuery extendingQuery = taskService.createTaskQuery()
      .taskCandidateGroup("aCandidateGroup");

    // when
    TaskQueryImpl result =  (TaskQueryImpl)((TaskQueryImpl)extendedQuery).extend(extendingQuery);

    // then
    assertEquals("aTaskName", result.getOrQueries().get(0).getName());
    assertEquals("anotherTaskName", result.getOrQueries().get(1).getNameLike());
    assertEquals("aCandidateGroup", result.getCandidateGroup());
  }

  @Test
  public void shouldReturnTasksByExtendingQuery_OrInBothExtendedAndExtendingQuery() {
    // given
    TaskQuery extendedQuery = taskService.createTaskQuery()
      .startOr()
        .taskName("aTaskName")
      .endOr()
      .startOr()
        .taskNameLike("anotherTaskName")
      .endOr();

    TaskQuery extendingQuery = taskService.createTaskQuery()
      .startOr()
        .taskCandidateGroup("aCandidateGroup")
      .endOr()
      .startOr()
        .taskCandidateUser("aCandidateUser")
      .endOr();

    // when
    TaskQueryImpl result =  (TaskQueryImpl)((TaskQueryImpl)extendedQuery).extend(extendingQuery);

    // then
    assertEquals("aTaskName", result.getOrQueries().get(0).getName());
    assertEquals("anotherTaskName", result.getOrQueries().get(1).getNameLike());
    assertEquals("aCandidateGroup", result.getOrQueries().get(2).getCandidateGroup());
    assertEquals("aCandidateUser", result.getOrQueries().get(3).getCandidateUser());
  }

}
