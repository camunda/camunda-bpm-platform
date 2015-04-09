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
package org.camunda.bpm.engine.test.authorization;

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.CREATE;
import static org.camunda.bpm.engine.authorization.Permissions.CREATE_INSTANCE;
import static org.camunda.bpm.engine.authorization.Permissions.DELETE;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.READ_TASK;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE_TASK;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_INSTANCE;
import static org.camunda.bpm.engine.authorization.Resources.TASK;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.IdentityLinkType;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;

/**
 * @author Roman Smirnov
 *
 */
public class TaskAuthorizationTest extends AuthorizationTest {

  protected static final String PROCESS_KEY = "oneTaskProcess";
  protected static final String CASE_KEY = "oneTaskCase";
  protected static final String DEMO_ASSIGNEE_PROCESS_KEY = "demoAssigneeProcess";
  protected static final String CANDIDATE_USERS_PROCESS_KEY = "candidateUsersProcess";
  protected static final String CANDIDATE_GROUPS_PROCESS_KEY = "candidateGroupsProcess";

  protected String deploymentId;

  public void setUp() {
    deploymentId = repositoryService
      .createDeployment()
      .addClasspathResource("org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
      .addClasspathResource("org/camunda/bpm/engine/test/authorization/oneTaskCase.cmmn")
      .addClasspathResource("org/camunda/bpm/engine/test/authorization/oneTaskProcess.bpmn20.xml")
      .addClasspathResource("org/camunda/bpm/engine/test/authorization/candidateUsersProcess.bpmn20.xml")
      .addClasspathResource("org/camunda/bpm/engine/test/authorization/candidateGroupsProcess.bpmn20.xml")
      .deploy()
      .getId();
  }

  public void tearDown() {
    super.tearDown();
    repositoryService.deleteDeployment(deploymentId, true);
  }

  // task query ///////////////////////////////////////////////////////

  public void testQueryWithTaskInsideProcessWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);

    // when
    TaskQuery query = taskService.createTaskQuery();

    // then
    verifyQueryResults(query, 0);
  }

  public void testQueryWithTaskInsideProcessWithReadPermissionOnTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    Authorization authorization = createGrantAuthorization(TASK, taskId);
    authorization.setUserId(userId);
    authorization.addPermission(READ);
    saveAuthorization(authorization);

    // when
    TaskQuery query = taskService.createTaskQuery();

    // then
    verifyQueryResults(query, 1);
  }

  public void testQueryWithTaskInsideProcessWithReadPermissionOnOneTaskProcess() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);

    Authorization authorization = createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY);
    authorization.setUserId(userId);
    authorization.addPermission(READ_TASK);
    saveAuthorization(authorization);

    // when
    TaskQuery query = taskService.createTaskQuery();

    // then
    verifyQueryResults(query, 1);
  }

  public void testQueryWithTaskInsideCaseWithoutAuthorization() {
    // given
    createCaseInstanceByKey(CASE_KEY);

    // when
    TaskQuery query = taskService.createTaskQuery();

    // then
    verifyQueryResults(query, 1);
  }

  public void testQueryWithStandaloneTaskWithoutAuthorization() {
    // given
    String taskId = "newTask";
    createTask(taskId);

    Authorization authorization = createGrantAuthorization(TASK, taskId);
    authorization.setUserId(userId);
    authorization.addPermission(READ);
    saveAuthorization(authorization);

    // when
    TaskQuery query = taskService.createTaskQuery();

    // then
    verifyQueryResults(query, 1);

    deleteTask(taskId, true);
  }

  public void testQueryWithStandaloneTaskWithReadPermissionOnTask() {
    // given
    String taskId = "newTask";
    createTask(taskId);

    // when
    TaskQuery query = taskService.createTaskQuery();

    // then
    verifyQueryResults(query, 0);

    deleteTask(taskId, true);
  }

  // new task /////////////////////////////////////////////////////////////

  public void testNewTaskWithoutAuthorization() {
    // given

    try {
      // when
      taskService.newTask();
      fail("Exception expected: It should not be possible to create a new task.");
    } catch (AuthorizationException e) {

      // then
      assertTextPresent("The user with id 'test' does not have 'CREATE' permission on resource 'Task'", e.getMessage());
    }
  }

  public void testNewTask() {
    // given
    createGrantAuthorization(TASK, ANY, CREATE, userId);

    // when
    Task task = taskService.newTask();

    // then
    assertNotNull(task);
  }

  // save task (insert) //////////////////////////////////////////////////////////

  public void testSaveTaskInsertWithoutAuthorization() {
    // given
    TaskEntity task = TaskEntity.create();

    try {
      // when
      taskService.saveTask(task);
      fail("Exception expected: It should not be possible to save a task.");
    } catch (AuthorizationException e) {

      // then
      assertTextPresent("The user with id 'test' does not have 'CREATE' permission on resource 'Task'", e.getMessage());
    }
  }

  public void testSaveTaskInsert() {
    // given
    TaskEntity task = TaskEntity.create();
    task.setAssignee("demo");

    createGrantAuthorization(TASK, ANY, CREATE, userId);

    // when
    taskService.saveTask(task);

    // then
    task = (TaskEntity) selectSingleTask();
    assertNotNull(task);
    assertEquals("demo", task.getAssignee());

    String taskId = task.getId();
    deleteTask(taskId, true);
  }

  // save (standalone) task (update) //////////////////////////////////////////////////////////

  public void testSaveStandaloneTaskUpdateWithoutAuthorization() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    Task task = selectSingleTask();

    try {
      // when
      taskService.saveTask(task);
      fail("Exception expected: It should not be possible to save a task.");
    } catch (AuthorizationException e) {

      // then
      assertTextPresent("The user with id 'test' does not have 'UPDATE' permission on resource 'myTask' of type 'Task'", e.getMessage());
    }

    deleteTask(taskId, true);
  }

  public void testSaveStandaloneTaskUpdate() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    Task task = selectSingleTask();
    task.setAssignee("demo");

    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.saveTask(task);

    // then
    task = selectSingleTask();
    assertNotNull(task);
    assertEquals("demo", task.getAssignee());

    deleteTask(taskId, true);
  }

  // save (process) task (update) //////////////////////////////////////////////////////////

  public void testSaveProcessTaskUpdateWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    Task task = selectSingleTask();

    try {
      // when
      taskService.saveTask(task);
      fail("Exception expected: It should not be possible to save a task.");
    } catch (AuthorizationException e) {

      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(task.getId(), message);
      assertTextPresent(TASK.resourceName(), message);
      assertTextPresent(UPDATE_TASK.getName(), message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testSaveProcessTaskUpdateWithUpdatePermissionOnTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    Task task = selectSingleTask();
    task.setAssignee("demo");

    createGrantAuthorization(TASK, task.getId(), UPDATE, userId);

    // when
    taskService.saveTask(task);

    // then
    task = selectSingleTask();
    assertNotNull(task);
    assertEquals("demo", task.getAssignee());
  }

  public void testSaveProcessTaskUpdateWithUpdatePermissionOnAnyTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    Task task = selectSingleTask();
    task.setAssignee("demo");

    createGrantAuthorization(TASK, ANY, UPDATE, userId);

    // when
    taskService.saveTask(task);

    // then
    task = selectSingleTask();
    assertNotNull(task);
    assertEquals("demo", task.getAssignee());
  }

  public void testSaveProcessTaskUpdateWithUpdateTasksPermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    Task task = selectSingleTask();
    task.setAssignee("demo");

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, UPDATE_TASK, userId);

    // when
    taskService.saveTask(task);

    // then
    task = selectSingleTask();
    assertNotNull(task);
    assertEquals("demo", task.getAssignee());
  }

  // save (case) task (update) //////////////////////////////////////////////////////////

  public void testSaveCaseTaskUpdate() {
    // given
    createCaseInstanceByKey(CASE_KEY);
    Task task = selectSingleTask();
    task.setAssignee("demo");

    // when
    taskService.saveTask(task);

    // then
    task = selectSingleTask();
    assertNotNull(task);
    assertEquals("demo", task.getAssignee());
  }

  // delete task ///////////////////////////////////////////////////////////////////////

  public void testDeleteTaskWithoutAuthorization() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    try {
      // when
      taskService.deleteTask(taskId);
      fail("Exception expected: It should not be possible to delete a task.");
    } catch (AuthorizationException e) {
      // then
      assertTextPresent("The user with id 'test' does not have 'DELETE' permission on resource 'myTask' of type 'Task'", e.getMessage());
    }

    deleteTask(taskId, true);
  }

  public void testDeleteTask() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    createGrantAuthorization(TASK, taskId, DELETE, userId);

    // when
    taskService.deleteTask(taskId);

    // then
    Task task = selectSingleTask();
    assertNull(task);

    // triggers a db clean up
    deleteTask(taskId, true);
  }

  // delete tasks ///////////////////////////////////////////////////////////////////////

  public void testDeleteTasksWithoutAuthorization() {
    // given
    String firstTaskId = "myTask1";
    createTask(firstTaskId);
    String secondTaskId = "myTask2";
    createTask(secondTaskId);

    try {
      // when
      taskService.deleteTasks(Arrays.asList(firstTaskId, secondTaskId));
      fail("Exception expected: It should not be possible to delete tasks.");
    } catch (AuthorizationException e) {
      // then
      assertTextPresent("The user with id 'test' does not have 'DELETE' permission on resource 'myTask1' of type 'Task'", e.getMessage());
    }

    deleteTask(firstTaskId, true);
    deleteTask(secondTaskId, true);
  }

  public void testDeleteTasksWithDeletePermissionOnFirstTask() {
    // given
    String firstTaskId = "myTask1";
    createTask(firstTaskId);
    createGrantAuthorization(TASK, firstTaskId, DELETE, userId);

    String secondTaskId = "myTask2";
    createTask(secondTaskId);

    try {
      // when
      taskService.deleteTasks(Arrays.asList(firstTaskId, secondTaskId));
      fail("Exception expected: It should not be possible to delete tasks.");
    } catch (AuthorizationException e) {
      // then
      assertTextPresent("The user with id 'test' does not have 'DELETE' permission on resource 'myTask2' of type 'Task'", e.getMessage());
    }

    deleteTask(firstTaskId, true);
    deleteTask(secondTaskId, true);
  }

  public void testDeleteTasks() {
    // given
    String firstTaskId = "myTask1";
    createTask(firstTaskId);
    String secondTaskId = "myTask2";
    createTask(secondTaskId);

    createGrantAuthorization(TASK, ANY, DELETE, userId);

    // when
    taskService.deleteTasks(Arrays.asList(firstTaskId, secondTaskId));

    // then
    Task task = selectSingleTask();
    assertNull(task);

    // triggers a db clean up
    deleteTask(firstTaskId, true);
    deleteTask(secondTaskId, true);
  }

  // set assignee on standalone task /////////////////////////////////////////////

  public void testStandaloneTaskSetAssigneeWithoutAuthorization() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    try {
      // when
      taskService.setAssignee(taskId, "demo");
      fail("Exception expected: It should not be possible to set an assignee");
    } catch (AuthorizationException e) {
      // then
      assertTextPresent("The user with id 'test' does not have 'UPDATE' permission on resource 'myTask' of type 'Task'", e.getMessage());
    }

    deleteTask(taskId, true);
  }

  public void testStandaloneTaskSetAssignee() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.setAssignee(taskId, "demo");

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("demo", task.getAssignee());

    deleteTask(taskId, true);
  }

  // set assignee on process task /////////////////////////////////////////////

  public void testProcessTaskSetAssigneeWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    try {
      // when
      taskService.setAssignee(taskId, "demo");
      fail("Exception expected: It should not be possible to set an assignee");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(taskId, message);
      assertTextPresent(TASK.resourceName(), message);
      assertTextPresent(UPDATE_TASK.getName(), message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testProcessTaskSetAssigneeWithUpdatePersmissionOnTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.setAssignee(taskId, "demo");

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("demo", task.getAssignee());
  }

  public void testProcessTaskSetAssigneeWithUpdatePersmissionOnAnyTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, ANY, UPDATE, userId);

    // when
    taskService.setAssignee(taskId, "demo");

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("demo", task.getAssignee());
  }

  public void testProcessTaskSetAssigneeWithUpdateTasksPersmissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, UPDATE_TASK, userId);

    // when
    taskService.setAssignee(taskId, "demo");

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("demo", task.getAssignee());
  }

  public void testProcessTaskSetAssignee() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, taskId, UPDATE, userId);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, UPDATE_TASK, userId);

    // when
    taskService.setAssignee(taskId, "demo");

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("demo", task.getAssignee());
  }

  // set assignee on case task /////////////////////////////////////////////

  public void testCaseTaskSetAssignee() {
    // given
    createCaseInstanceByKey(CASE_KEY);
    String taskId = selectSingleTask().getId();

    // when
    taskService.setAssignee(taskId, "demo");

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("demo", task.getAssignee());
  }

  // set owner on standalone task /////////////////////////////////////////////

  public void testStandaloneTaskSetOwnerWithoutAuthorization() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    try {
      // when
      taskService.setOwner(taskId, "demo");
      fail("Exception expected: It should not be possible to set an owner");
    } catch (AuthorizationException e) {
      // then
      assertTextPresent("The user with id 'test' does not have 'UPDATE' permission on resource 'myTask' of type 'Task'", e.getMessage());
    }

    deleteTask(taskId, true);
  }

  public void testStandaloneTaskSetOwner() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.setOwner(taskId, "demo");

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("demo", task.getOwner());

    deleteTask(taskId, true);
  }

  // set owner on process task /////////////////////////////////////////////

  public void testProcessTaskSetOwnerWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    try {
      // when
      taskService.setOwner(taskId, "demo");
      fail("Exception expected: It should not be possible to set an owner");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(taskId, message);
      assertTextPresent(TASK.resourceName(), message);
      assertTextPresent(UPDATE_TASK.getName(), message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testProcessTaskSetOwnerWithUpdatePersmissionOnTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.setOwner(taskId, "demo");

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("demo", task.getOwner());
  }

  public void testProcessTaskSetOwnerWithUpdatePersmissionOnAnyTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, ANY, UPDATE, userId);

    // when
    taskService.setOwner(taskId, "demo");

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("demo", task.getOwner());
  }

  public void testProcessTaskSetOwnerWithUpdateTasksPersmissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, UPDATE_TASK, userId);

    // when
    taskService.setOwner(taskId, "demo");

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("demo", task.getOwner());
  }

  public void testProcessTaskSetOwner() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, taskId, UPDATE, userId);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, UPDATE_TASK, userId);

    // when
    taskService.setOwner(taskId, "demo");

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("demo", task.getOwner());
  }

  // set owner on case task /////////////////////////////////////////////

  public void testCaseTaskSetOwner() {
    // given
    createCaseInstanceByKey(CASE_KEY);
    String taskId = selectSingleTask().getId();

    // when
    taskService.setOwner(taskId, "demo");

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("demo", task.getOwner());
  }

  // add candidate user ((standalone) task) /////////////////////////////////////////////

  public void testStandaloneTaskAddCandidateUserWithoutAuthorization() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    try {
      // when
      taskService.addCandidateUser(taskId, "demo");
      fail("Exception expected: It should not be possible to add a candidate user");
    } catch (AuthorizationException e) {
      // then
      assertTextPresent("The user with id 'test' does not have 'UPDATE' permission on resource 'myTask' of type 'Task'", e.getMessage());
    }

    deleteTask(taskId, true);
  }

  public void testStandaloneTaskAddCandidateUser() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.addCandidateUser(taskId, "demo");

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertEquals(1, linksForTask.size());

    IdentityLink identityLink = linksForTask.get(0);
    assertNotNull(identityLink);

    assertEquals("demo", identityLink.getUserId());
    assertEquals(IdentityLinkType.CANDIDATE, identityLink.getType());

    deleteTask(taskId, true);
  }

  // add candidate user ((process) task) /////////////////////////////////////////////

  public void testProcessTaskAddCandidateUserWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    try {
      // when
      taskService.addCandidateUser(taskId, "demo");
      fail("Exception expected: It should not be possible to add a candidate user");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(taskId, message);
      assertTextPresent(TASK.resourceName(), message);
      assertTextPresent(UPDATE_TASK.getName(), message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testProcessTaskAddCandidateUserWithUpdatePersmissionOnTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.addCandidateUser(taskId, "demo");

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertEquals(1, linksForTask.size());

    IdentityLink identityLink = linksForTask.get(0);
    assertNotNull(identityLink);

    assertEquals("demo", identityLink.getUserId());
    assertEquals(IdentityLinkType.CANDIDATE, identityLink.getType());
  }

  public void testProcessTaskAddCandidateUserWithUpdatePersmissionOnAnyTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, ANY, UPDATE, userId);

    // when
    taskService.addCandidateUser(taskId, "demo");

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertEquals(1, linksForTask.size());

    IdentityLink identityLink = linksForTask.get(0);
    assertNotNull(identityLink);

    assertEquals("demo", identityLink.getUserId());
    assertEquals(IdentityLinkType.CANDIDATE, identityLink.getType());
  }

  public void testProcessTaskAddCandidateUserWithUpdateTasksPersmissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, UPDATE_TASK, userId);

    // when
    taskService.addCandidateUser(taskId, "demo");

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertEquals(1, linksForTask.size());

    IdentityLink identityLink = linksForTask.get(0);
    assertNotNull(identityLink);

    assertEquals("demo", identityLink.getUserId());
    assertEquals(IdentityLinkType.CANDIDATE, identityLink.getType());
  }

  public void testProcessTaskAddCandidateUser() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, taskId, UPDATE, userId);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, UPDATE_TASK, userId);

    // when
    taskService.addCandidateUser(taskId, "demo");

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertEquals(1, linksForTask.size());

    IdentityLink identityLink = linksForTask.get(0);
    assertNotNull(identityLink);

    assertEquals("demo", identityLink.getUserId());
    assertEquals(IdentityLinkType.CANDIDATE, identityLink.getType());
  }

  // add candidate user ((case) task) /////////////////////////////////////////////

  public void testCaseTaskAddCandidateUser() {
    // given
    createCaseInstanceByKey(CASE_KEY);
    String taskId = selectSingleTask().getId();

    // when
    taskService.addCandidateUser(taskId, "demo");

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertEquals(1, linksForTask.size());

    IdentityLink identityLink = linksForTask.get(0);
    assertNotNull(identityLink);

    assertEquals("demo", identityLink.getUserId());
    assertEquals(IdentityLinkType.CANDIDATE, identityLink.getType());
  }

  // add candidate group ((standalone) task) /////////////////////////////////////////////

  public void testStandaloneTaskAddCandidateGroupWithoutAuthorization() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    try {
      // when
      taskService.addCandidateGroup(taskId, "accounting");
      fail("Exception expected: It should not be possible to add a candidate group");
    } catch (AuthorizationException e) {
      // then
      assertTextPresent("The user with id 'test' does not have 'UPDATE' permission on resource 'myTask' of type 'Task'", e.getMessage());
    }

    deleteTask(taskId, true);
  }

  public void testStandaloneTaskAddCandidateGroup() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.addCandidateGroup(taskId, "accounting");

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertEquals(1, linksForTask.size());

    IdentityLink identityLink = linksForTask.get(0);
    assertNotNull(identityLink);

    assertEquals("accounting", identityLink.getGroupId());
    assertEquals(IdentityLinkType.CANDIDATE, identityLink.getType());

    deleteTask(taskId, true);
  }

  // add candidate group ((process) task) /////////////////////////////////////////////

  public void testProcessTaskAddCandidateGroupWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    try {
      // when
      taskService.addCandidateGroup(taskId, "accounting");
      fail("Exception expected: It should not be possible to add a candidate group");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(taskId, message);
      assertTextPresent(TASK.resourceName(), message);
      assertTextPresent(UPDATE_TASK.getName(), message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testProcessTaskAddCandidateGroupWithUpdatePersmissionOnTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.addCandidateGroup(taskId, "accounting");

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertEquals(1, linksForTask.size());

    IdentityLink identityLink = linksForTask.get(0);
    assertNotNull(identityLink);

    assertEquals("accounting", identityLink.getGroupId());
    assertEquals(IdentityLinkType.CANDIDATE, identityLink.getType());
  }

  public void testProcessTaskAddCandidateGroupWithUpdatePersmissionOnAnyTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, ANY, UPDATE, userId);

    // when
    taskService.addCandidateGroup(taskId, "accounting");

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertEquals(1, linksForTask.size());

    IdentityLink identityLink = linksForTask.get(0);
    assertNotNull(identityLink);

    assertEquals("accounting", identityLink.getGroupId());
    assertEquals(IdentityLinkType.CANDIDATE, identityLink.getType());
  }

  public void testProcessTaskAddCandidateGroupWithUpdateTasksPersmissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, UPDATE_TASK, userId);

    // when
    taskService.addCandidateGroup(taskId, "accounting");

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertEquals(1, linksForTask.size());

    IdentityLink identityLink = linksForTask.get(0);
    assertNotNull(identityLink);

    assertEquals("accounting", identityLink.getGroupId());
    assertEquals(IdentityLinkType.CANDIDATE, identityLink.getType());
  }

  public void testProcessTaskAddCandidateGroup() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, taskId, UPDATE, userId);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, UPDATE_TASK, userId);

    // when
    taskService.addCandidateGroup(taskId, "accounting");

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertEquals(1, linksForTask.size());

    IdentityLink identityLink = linksForTask.get(0);
    assertNotNull(identityLink);

    assertEquals("accounting", identityLink.getGroupId());
    assertEquals(IdentityLinkType.CANDIDATE, identityLink.getType());
  }

  // add candidate group ((case) task) /////////////////////////////////////////////

  public void testCaseTaskAddCandidateGroup() {
    // given
    createCaseInstanceByKey(CASE_KEY);
    String taskId = selectSingleTask().getId();

    // when
    taskService.addCandidateGroup(taskId, "accounting");

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertEquals(1, linksForTask.size());

    IdentityLink identityLink = linksForTask.get(0);
    assertNotNull(identityLink);

    assertEquals("accounting", identityLink.getGroupId());
    assertEquals(IdentityLinkType.CANDIDATE, identityLink.getType());
  }

  // add user identity link ((standalone) task) /////////////////////////////////////////////

  public void testStandaloneTaskAddUserIdentityLinkWithoutAuthorization() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    try {
      // when
      taskService.addUserIdentityLink(taskId, "demo", IdentityLinkType.CANDIDATE);
      fail("Exception expected: It should not be possible to add an user identity link");
    } catch (AuthorizationException e) {
      // then
      assertTextPresent("The user with id 'test' does not have 'UPDATE' permission on resource 'myTask' of type 'Task'", e.getMessage());
    }

    deleteTask(taskId, true);
  }

  public void testStandaloneTaskAddUserIdentityLink() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.addUserIdentityLink(taskId, "demo", IdentityLinkType.CANDIDATE);

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertEquals(1, linksForTask.size());

    IdentityLink identityLink = linksForTask.get(0);
    assertNotNull(identityLink);

    assertEquals("demo", identityLink.getUserId());
    assertEquals(IdentityLinkType.CANDIDATE, identityLink.getType());

    deleteTask(taskId, true);
  }

  // add user identity link ((process) task) /////////////////////////////////////////////

  public void testProcessTaskAddUserIdentityLinkWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    try {
      // when
      taskService.addUserIdentityLink(taskId, "demo", IdentityLinkType.CANDIDATE);
      fail("Exception expected: It should not be possible to add an user identity link");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(taskId, message);
      assertTextPresent(TASK.resourceName(), message);
      assertTextPresent(UPDATE_TASK.getName(), message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testProcessTaskAddUserIdentityLinkWithUpdatePersmissionOnTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.addUserIdentityLink(taskId, "demo", IdentityLinkType.CANDIDATE);

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertEquals(1, linksForTask.size());

    IdentityLink identityLink = linksForTask.get(0);
    assertNotNull(identityLink);

    assertEquals("demo", identityLink.getUserId());
    assertEquals(IdentityLinkType.CANDIDATE, identityLink.getType());
  }

  public void testProcessTaskAddUserIdentityLinkWithUpdatePersmissionOnAnyTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, ANY, UPDATE, userId);

    // when
    taskService.addUserIdentityLink(taskId, "demo", IdentityLinkType.CANDIDATE);

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertEquals(1, linksForTask.size());

    IdentityLink identityLink = linksForTask.get(0);
    assertNotNull(identityLink);

    assertEquals("demo", identityLink.getUserId());
    assertEquals(IdentityLinkType.CANDIDATE, identityLink.getType());
  }

  public void testProcessTaskAddUserIdentityLinkWithUpdateTasksPersmissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, UPDATE_TASK, userId);

    // when
    taskService.addUserIdentityLink(taskId, "demo", IdentityLinkType.CANDIDATE);

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertEquals(1, linksForTask.size());

    IdentityLink identityLink = linksForTask.get(0);
    assertNotNull(identityLink);

    assertEquals("demo", identityLink.getUserId());
    assertEquals(IdentityLinkType.CANDIDATE, identityLink.getType());
  }

  public void testProcessTaskAddUserIdentityLink() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, taskId, UPDATE, userId);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, UPDATE_TASK, userId);

    // when
    taskService.addUserIdentityLink(taskId, "demo", IdentityLinkType.CANDIDATE);

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertEquals(1, linksForTask.size());

    IdentityLink identityLink = linksForTask.get(0);
    assertNotNull(identityLink);

    assertEquals("demo", identityLink.getUserId());
    assertEquals(IdentityLinkType.CANDIDATE, identityLink.getType());
  }

  // add user identity link ((case) task) /////////////////////////////////////////////

  public void testCaseTaskAddUserIdentityLink() {
    // given
    createCaseInstanceByKey(CASE_KEY);
    String taskId = selectSingleTask().getId();

    // when
    taskService.addUserIdentityLink(taskId, "demo", IdentityLinkType.CANDIDATE);

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertEquals(1, linksForTask.size());

    IdentityLink identityLink = linksForTask.get(0);
    assertNotNull(identityLink);

    assertEquals("demo", identityLink.getUserId());
    assertEquals(IdentityLinkType.CANDIDATE, identityLink.getType());
  }

  // add group identity link ((standalone) task) /////////////////////////////////////////////

  public void testStandaloneTaskAddGroupIdentityLinkWithoutAuthorization() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    try {
      // when
      taskService.addGroupIdentityLink(taskId, "accounting", IdentityLinkType.CANDIDATE);
      fail("Exception expected: It should not be possible to add a group identity link");
    } catch (AuthorizationException e) {
      // then
      assertTextPresent("The user with id 'test' does not have 'UPDATE' permission on resource 'myTask' of type 'Task'", e.getMessage());
    }

    deleteTask(taskId, true);
  }

  public void testStandaloneTaskAddGroupIdentityLink() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.addGroupIdentityLink(taskId, "accounting", IdentityLinkType.CANDIDATE);

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertEquals(1, linksForTask.size());

    IdentityLink identityLink = linksForTask.get(0);
    assertNotNull(identityLink);

    assertEquals("accounting", identityLink.getGroupId());
    assertEquals(IdentityLinkType.CANDIDATE, identityLink.getType());

    deleteTask(taskId, true);
  }

  // add group identity link ((process) task) /////////////////////////////////////////////

  public void testProcessTaskAddGroupIdentityLinkWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    try {
      // when
      taskService.addGroupIdentityLink(taskId, "accounting", IdentityLinkType.CANDIDATE);
      fail("Exception expected: It should not be possible to add a group identity link");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(taskId, message);
      assertTextPresent(TASK.resourceName(), message);
      assertTextPresent(UPDATE_TASK.getName(), message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testProcessTaskAddGroupIdentityLinkWithUpdatePersmissionOnTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.addGroupIdentityLink(taskId, "accounting", IdentityLinkType.CANDIDATE);

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertEquals(1, linksForTask.size());

    IdentityLink identityLink = linksForTask.get(0);
    assertNotNull(identityLink);

    assertEquals("accounting", identityLink.getGroupId());
    assertEquals(IdentityLinkType.CANDIDATE, identityLink.getType());
  }

  public void testProcessTaskAddGroupIdentityLinkWithUpdatePersmissionOnAnyTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, ANY, UPDATE, userId);

    // when
    taskService.addGroupIdentityLink(taskId, "accounting", IdentityLinkType.CANDIDATE);

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertEquals(1, linksForTask.size());

    IdentityLink identityLink = linksForTask.get(0);
    assertNotNull(identityLink);

    assertEquals("accounting", identityLink.getGroupId());
    assertEquals(IdentityLinkType.CANDIDATE, identityLink.getType());
  }

  public void testProcessTaskAddGroupIdentityLinkWithUpdateTasksPersmissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, UPDATE_TASK, userId);

    // when
    taskService.addGroupIdentityLink(taskId, "accounting", IdentityLinkType.CANDIDATE);

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertEquals(1, linksForTask.size());

    IdentityLink identityLink = linksForTask.get(0);
    assertNotNull(identityLink);

    assertEquals("accounting", identityLink.getGroupId());
    assertEquals(IdentityLinkType.CANDIDATE, identityLink.getType());
  }

  public void testProcessTaskAddGroupIdentityLink() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, taskId, UPDATE, userId);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, UPDATE_TASK, userId);

    // when
    taskService.addGroupIdentityLink(taskId, "accounting", IdentityLinkType.CANDIDATE);

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertEquals(1, linksForTask.size());

    IdentityLink identityLink = linksForTask.get(0);
    assertNotNull(identityLink);

    assertEquals("accounting", identityLink.getGroupId());
    assertEquals(IdentityLinkType.CANDIDATE, identityLink.getType());
  }

  // add group identity link ((case) task) /////////////////////////////////////////////

  public void testCaseTaskAddGroupIdentityLink() {
    // given
    createCaseInstanceByKey(CASE_KEY);
    String taskId = selectSingleTask().getId();

    // when
    taskService.addGroupIdentityLink(taskId, "accounting", IdentityLinkType.CANDIDATE);

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertEquals(1, linksForTask.size());

    IdentityLink identityLink = linksForTask.get(0);
    assertNotNull(identityLink);

    assertEquals("accounting", identityLink.getGroupId());
    assertEquals(IdentityLinkType.CANDIDATE, identityLink.getType());
  }

  // delete candidate user ((standalone) task) /////////////////////////////////////////////

  public void testStandaloneTaskDeleteCandidateUserWithoutAuthorization() {
    // given
    String taskId = "myTask";
    createTask(taskId);
    addCandidateUser(taskId, "demo");

    try {
      // when
      taskService.deleteCandidateUser(taskId, "demo");
      fail("Exception expected: It should not be possible to delete a candidate user");
    } catch (AuthorizationException e) {
      // then
      assertTextPresent("The user with id 'test' does not have 'UPDATE' permission on resource 'myTask' of type 'Task'", e.getMessage());
    }

    deleteTask(taskId, true);
  }

  public void testStandaloneTaskDeleteCandidateUser() {
    // given
    String taskId = "myTask";
    createTask(taskId);
    addCandidateUser(taskId, "demo");

    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.deleteCandidateUser(taskId, "demo");

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertTrue(linksForTask.isEmpty());

    deleteTask(taskId, true);
  }

  // delete candidate user ((process) task) /////////////////////////////////////////////

  public void testProcessTaskDeleteCandidateUserWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    addCandidateUser(taskId, "demo");

    try {
      // when
      taskService.deleteCandidateUser(taskId, "demo");
      fail("Exception expected: It should not be possible to delete a candidate user");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(taskId, message);
      assertTextPresent(TASK.resourceName(), message);
      assertTextPresent(UPDATE_TASK.getName(), message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testProcessTaskDeleteCandidateUserWithUpdatePersmissionOnTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    addCandidateUser(taskId, "demo");

    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.deleteCandidateUser(taskId, "demo");

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertTrue(linksForTask.isEmpty());
  }

  public void testProcessTaskDeleteCandidateUserWithUpdatePersmissionOnAnyTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    addCandidateUser(taskId, "demo");

    createGrantAuthorization(TASK, ANY, UPDATE, userId);

    // when
    taskService.deleteCandidateUser(taskId, "demo");

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertTrue(linksForTask.isEmpty());
  }

  public void testProcessTaskDeleteCandidateUserWithUpdateTasksPersmissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    addCandidateUser(taskId, "demo");

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, UPDATE_TASK, userId);

    // when
    taskService.deleteCandidateUser(taskId, "demo");

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertTrue(linksForTask.isEmpty());
  }

  public void testProcessTaskDeleteCandidateUser() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    addCandidateUser(taskId, "demo");

    createGrantAuthorization(TASK, taskId, UPDATE, userId);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, UPDATE_TASK, userId);

    // when
    taskService.deleteCandidateUser(taskId, "demo");

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertTrue(linksForTask.isEmpty());
  }

  // delete candidate user ((case) task) /////////////////////////////////////////////

  public void testCaseTaskDeleteCandidateUser() {
    // given
    createCaseInstanceByKey(CASE_KEY);
    String taskId = selectSingleTask().getId();
    addCandidateUser(taskId, "demo");

    // when
    taskService.deleteCandidateUser(taskId, "demo");

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertTrue(linksForTask.isEmpty());
  }

  // delete candidate group ((standalone) task) /////////////////////////////////////////////

  public void testStandaloneTaskDeleteCandidateGroupWithoutAuthorization() {
    // given
    String taskId = "myTask";
    createTask(taskId);
    addCandidateGroup(taskId, "accounting");

    try {
      // when
      taskService.deleteCandidateGroup(taskId, "accounting");
      fail("Exception expected: It should not be possible to delete a candidate group");
    } catch (AuthorizationException e) {
      // then
      assertTextPresent("The user with id 'test' does not have 'UPDATE' permission on resource 'myTask' of type 'Task'", e.getMessage());
    }

    deleteTask(taskId, true);
  }

  public void testStandaloneTaskDeleteCandidateGroup() {
    // given
    String taskId = "myTask";
    createTask(taskId);
    addCandidateGroup(taskId, "accounting");

    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.deleteCandidateGroup(taskId, "accounting");

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertTrue(linksForTask.isEmpty());

    deleteTask(taskId, true);
  }

  // delete candidate group ((process) task) /////////////////////////////////////////////

  public void testProcessTaskDeleteCandidateGroupWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    addCandidateGroup(taskId, "accounting");

    try {
      // when
      taskService.deleteCandidateGroup(taskId, "accounting");
      fail("Exception expected: It should not be possible to delete a candidate group");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(taskId, message);
      assertTextPresent(TASK.resourceName(), message);
      assertTextPresent(UPDATE_TASK.getName(), message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testProcessTaskDeleteCandidateGroupWithUpdatePersmissionOnTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    addCandidateGroup(taskId, "accounting");

    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.deleteCandidateGroup(taskId, "accounting");

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertTrue(linksForTask.isEmpty());
  }

  public void testProcessTaskDeleteCandidateGroupWithUpdatePersmissionOnAnyTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    addCandidateGroup(taskId, "accounting");

    createGrantAuthorization(TASK, ANY, UPDATE, userId);

    // when
    taskService.deleteCandidateGroup(taskId, "accounting");

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertTrue(linksForTask.isEmpty());
  }

  public void testProcessTaskDeleteCandidateGroupWithUpdateTasksPersmissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    addCandidateGroup(taskId, "accounting");

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, UPDATE_TASK, userId);

    // when
    taskService.deleteCandidateGroup(taskId, "accounting");

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertTrue(linksForTask.isEmpty());
  }

  public void testProcessTaskDeleteCandidateGroup() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    addCandidateGroup(taskId, "accounting");

    createGrantAuthorization(TASK, taskId, UPDATE, userId);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, UPDATE_TASK, userId);

    // when
    taskService.deleteCandidateGroup(taskId, "accounting");

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertTrue(linksForTask.isEmpty());
  }

  // delete candidate group ((case) task) /////////////////////////////////////////////

  public void testCaseTaskDeleteCandidateGroup() {
    // given
    createCaseInstanceByKey(CASE_KEY);
    String taskId = selectSingleTask().getId();
    addCandidateGroup(taskId, "accounting");

    // when
    taskService.deleteCandidateGroup(taskId, "accounting");

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertTrue(linksForTask.isEmpty());
  }

  // delete user identity link ((standalone) task) /////////////////////////////////////////////

  public void testStandaloneTaskDeleteUserIdentityLinkWithoutAuthorization() {
    // given
    String taskId = "myTask";
    createTask(taskId);
    addCandidateUser(taskId, "demo");

    try {
      // when
      taskService.deleteUserIdentityLink(taskId, "demo", IdentityLinkType.CANDIDATE);
      fail("Exception expected: It should not be possible to delete an user identity link");
    } catch (AuthorizationException e) {
      // then
      assertTextPresent("The user with id 'test' does not have 'UPDATE' permission on resource 'myTask' of type 'Task'", e.getMessage());
    }

    deleteTask(taskId, true);
  }

  public void testStandaloneTaskDeleteUserIdentityLink() {
    // given
    String taskId = "myTask";
    createTask(taskId);
    addCandidateUser(taskId, "demo");

    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.deleteUserIdentityLink(taskId, "demo", IdentityLinkType.CANDIDATE);

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertTrue(linksForTask.isEmpty());

    deleteTask(taskId, true);
  }

  // delete user identity link ((process) task) /////////////////////////////////////////////

  public void testProcessTaskDeleteUserIdentityLinkWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    addCandidateUser(taskId, "demo");

    try {
      // when
      taskService.deleteUserIdentityLink(taskId, "demo", IdentityLinkType.CANDIDATE);
      fail("Exception expected: It should not be possible to delete an user identity link");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(taskId, message);
      assertTextPresent(TASK.resourceName(), message);
      assertTextPresent(UPDATE_TASK.getName(), message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testProcessTaskDeleteUserIdentityLinkWithUpdatePersmissionOnTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    addCandidateUser(taskId, "demo");

    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.deleteUserIdentityLink(taskId, "demo", IdentityLinkType.CANDIDATE);

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertTrue(linksForTask.isEmpty());
  }

  public void testProcessTaskDeleteUserIdentityLinkWithUpdatePersmissionOnAnyTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    addCandidateUser(taskId, "demo");

    createGrantAuthorization(TASK, ANY, UPDATE, userId);

    // when
    taskService.deleteUserIdentityLink(taskId, "demo", IdentityLinkType.CANDIDATE);

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertTrue(linksForTask.isEmpty());
  }

  public void testProcessTaskDeleteUserIdentityLinkWithUpdateTasksPersmissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    addCandidateUser(taskId, "demo");

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, UPDATE_TASK, userId);

    // when
    taskService.deleteUserIdentityLink(taskId, "demo", IdentityLinkType.CANDIDATE);

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertTrue(linksForTask.isEmpty());
  }

  public void testProcessTaskDeleteUserIdentityLink() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    addCandidateUser(taskId, "demo");

    createGrantAuthorization(TASK, taskId, UPDATE, userId);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, UPDATE_TASK, userId);

    // when
    taskService.deleteUserIdentityLink(taskId, "demo", IdentityLinkType.CANDIDATE);

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertTrue(linksForTask.isEmpty());
  }

  // delete user identity link ((case) task) /////////////////////////////////////////////

  public void testCaseTaskDeleteUserIdentityLink() {
    // given
    createCaseInstanceByKey(CASE_KEY);
    String taskId = selectSingleTask().getId();
    addCandidateUser(taskId, "demo");

    // when
    taskService.deleteUserIdentityLink(taskId, "demo", IdentityLinkType.CANDIDATE);

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertTrue(linksForTask.isEmpty());
  }

  // delete group identity link ((standalone) task) /////////////////////////////////////////////

  public void testStandaloneTaskDeleteGroupIdentityLinkWithoutAuthorization() {
    // given
    String taskId = "myTask";
    createTask(taskId);
    addCandidateGroup(taskId, "accounting");

    try {
      // when
      taskService.deleteGroupIdentityLink(taskId, "accounting", IdentityLinkType.CANDIDATE);
      fail("Exception expected: It should not be possible to delete a group identity link");
    } catch (AuthorizationException e) {
      // then
      assertTextPresent("The user with id 'test' does not have 'UPDATE' permission on resource 'myTask' of type 'Task'", e.getMessage());
    }

    deleteTask(taskId, true);
  }

  public void testStandaloneTaskDeleteGroupIdentityLink() {
    // given
    String taskId = "myTask";
    createTask(taskId);
    addCandidateGroup(taskId, "accounting");

    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.deleteGroupIdentityLink(taskId, "accounting", IdentityLinkType.CANDIDATE);

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertTrue(linksForTask.isEmpty());

    deleteTask(taskId, true);
  }

  // delete group identity link ((process) task) /////////////////////////////////////////////

  public void testProcessTaskDeleteGroupIdentityLinkWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    addCandidateGroup(taskId, "accounting");

    try {
      // when
      taskService.deleteGroupIdentityLink(taskId, "accounting", IdentityLinkType.CANDIDATE);
      fail("Exception expected: It should not be possible to delete a group identity link");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(taskId, message);
      assertTextPresent(TASK.resourceName(), message);
      assertTextPresent(UPDATE_TASK.getName(), message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testProcessTaskDeleteGroupIdentityLinkWithUpdatePersmissionOnTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    addCandidateGroup(taskId, "accounting");

    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.deleteGroupIdentityLink(taskId, "accounting", IdentityLinkType.CANDIDATE);

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertTrue(linksForTask.isEmpty());
  }

  public void testProcessTaskDeleteGroupIdentityLinkWithUpdatePersmissionOnAnyTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    addCandidateGroup(taskId, "accounting");

    createGrantAuthorization(TASK, ANY, UPDATE, userId);

    // when
    taskService.deleteGroupIdentityLink(taskId, "accounting", IdentityLinkType.CANDIDATE);

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertTrue(linksForTask.isEmpty());
  }

  public void testProcessTaskDeleteGroupIdentityLinkWithUpdateTasksPersmissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    addCandidateGroup(taskId, "accounting");

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, UPDATE_TASK, userId);

    // when
    taskService.deleteGroupIdentityLink(taskId, "accounting", IdentityLinkType.CANDIDATE);

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertTrue(linksForTask.isEmpty());
  }

  public void testProcessTaskDeleteGroupIdentityLink() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    addCandidateGroup(taskId, "accounting");

    createGrantAuthorization(TASK, taskId, UPDATE, userId);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, UPDATE_TASK, userId);

    // when
    taskService.deleteGroupIdentityLink(taskId, "accounting", IdentityLinkType.CANDIDATE);

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertTrue(linksForTask.isEmpty());
  }

  // delete group identity link ((case) task) /////////////////////////////////////////////

  public void testCaseTaskDeleteGroupIdentityLink() {
    // given
    createCaseInstanceByKey(CASE_KEY);
    String taskId = selectSingleTask().getId();
    addCandidateGroup(taskId, "accounting");

    // when
    taskService.deleteGroupIdentityLink(taskId, "accounting", IdentityLinkType.CANDIDATE);

    // then
    disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    enableAuthorization();

    assertNotNull(linksForTask);
    assertTrue(linksForTask.isEmpty());
  }

  // get identity links ((standalone) task) ////////////////////////////////////////////////

  public void testStandaloneTaskGetIdentityLinksWithoutAuthorization() {
    // given
    String taskId = "myTask";
    createTask(taskId);
    addCandidateUser(taskId, "demo");

    try {
      // when
      taskService.getIdentityLinksForTask(taskId);
      fail("Exception expected: It should not be possible to get identity links");
    } catch (AuthorizationException e) {
      // then
      assertTextPresent("The user with id 'test' does not have 'READ' permission on resource 'myTask' of type 'Task'", e.getMessage());
    }

    deleteTask(taskId, true);
  }

  public void testStandaloneTaskGetIdentityLinks() {
    // given
    String taskId = "myTask";
    createTask(taskId);
    addCandidateUser(taskId, "demo");

    createGrantAuthorization(TASK, taskId, READ, userId);

    // when
    List<IdentityLink> identityLinksForTask = taskService.getIdentityLinksForTask(taskId);

    // then
    assertNotNull(identityLinksForTask);
    assertFalse(identityLinksForTask.isEmpty());

    deleteTask(taskId, true);
  }

  // get identity links ((process) task) ////////////////////////////////////////////////

  public void testProcessTaskGetIdentityLinksWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    addCandidateUser(taskId, "demo");

    try {
      // when
      taskService.getIdentityLinksForTask(taskId);
      fail("Exception expected: It should not be possible to get the identity links");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(taskId, message);
      assertTextPresent(TASK.resourceName(), message);
      assertTextPresent(READ_TASK.getName(), message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testProcessTaskGetIdentityLinksWithReadPersmissionOnTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    addCandidateUser(taskId, "demo");

    createGrantAuthorization(TASK, taskId, READ, userId);

    // when
    List<IdentityLink> identityLinksForTask = taskService.getIdentityLinksForTask(taskId);

    // then
    assertNotNull(identityLinksForTask);
    assertFalse(identityLinksForTask.isEmpty());
  }

  public void testProcessTaskGetIdentityLinksWithReadPersmissionOnAnyTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    addCandidateUser(taskId, "demo");

    createGrantAuthorization(TASK, ANY, READ, userId);

    // when
    List<IdentityLink> identityLinksForTask = taskService.getIdentityLinksForTask(taskId);

    // then
    assertNotNull(identityLinksForTask);
    assertFalse(identityLinksForTask.isEmpty());
  }

  public void testProcessTaskGetIdentityLinksWithReadTasksPersmissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    addCandidateUser(taskId, "demo");

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, READ_TASK, userId);

    // when
    List<IdentityLink> identityLinksForTask = taskService.getIdentityLinksForTask(taskId);

    // then
    assertNotNull(identityLinksForTask);
    assertFalse(identityLinksForTask.isEmpty());
  }

  public void testProcessTaskGetIdentityLinks() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    addCandidateUser(taskId, "demo");

    createGrantAuthorization(TASK, taskId, READ, userId);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, READ_TASK, userId);

    // when
    List<IdentityLink> identityLinksForTask = taskService.getIdentityLinksForTask(taskId);

    // then
    assertNotNull(identityLinksForTask);
    assertFalse(identityLinksForTask.isEmpty());
  }

  // get identity links ((case) task) ////////////////////////////////////////////////

  public void testCaseTaskGetIdentityLinks() {
    // given
    createCaseInstanceByKey(CASE_KEY);
    String taskId = selectSingleTask().getId();
    addCandidateUser(taskId, "demo");

    // when
    List<IdentityLink> identityLinksForTask = taskService.getIdentityLinksForTask(taskId);

    // then
    assertNotNull(identityLinksForTask);
    assertFalse(identityLinksForTask.isEmpty());
  }

  // claim (standalone) task ////////////////////////////////////////////////////////////

  public void testStandaloneTaskClaimTaskWithoutAuthorization() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    try {
      // when
      taskService.claim(taskId, "demo");
      fail("Exception expected: It should not be possible to claim the task.");
    } catch (AuthorizationException e) {
      // then
      assertTextPresent("The user with id 'test' does not have 'UPDATE' permission on resource 'myTask' of type 'Task'.", e.getMessage());
    }

    deleteTask(taskId, true);
  }

  public void testStandaloneTaskClaimTask() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.claim(taskId, "demo");

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("demo", task.getAssignee());

    deleteTask(taskId, true);
  }

  // claim (process) task ////////////////////////////////////////////////////////////

  public void testProcessTaskClaimTaskWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    try {
      // when
      taskService.claim(taskId, "demo");
      fail("Exception expected: It should not be possible to claim the task");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(taskId, message);
      assertTextPresent(TASK.resourceName(), message);
      assertTextPresent(UPDATE_TASK.getName(), message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testProcessTaskClaimTaskWithUpdatePermissionOnTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.claim(taskId, "demo");

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("demo", task.getAssignee());
  }

  public void testProcessTaskClaimTaskWithUpdatePermissionOnAnyTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, ANY, UPDATE, userId);

    // when
    taskService.claim(taskId, "demo");

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("demo", task.getAssignee());
  }

  public void testProcessTaskClaimTaskWithUpdateTasksPermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, UPDATE_TASK, userId);

    // when
    taskService.claim(taskId, "demo");

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("demo", task.getAssignee());
  }

  public void testProcessTaskClaimTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, taskId, UPDATE, userId);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, UPDATE_TASK, userId);

    // when
    taskService.claim(taskId, "demo");

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("demo", task.getAssignee());
  }

  // claim (case) task ////////////////////////////////////////////////////////////

  public void testCaseTaskClaimTask() {
    // given
    createCaseInstanceByKey(CASE_KEY);
    String taskId = selectSingleTask().getId();

    // when
    taskService.claim(taskId, "demo");

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("demo", task.getAssignee());
  }

  // complete (standalone) task ////////////////////////////////////////////////////////////

  public void testStandaloneTaskCompleteTaskWithoutAuthorization() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    try {
      // when
      taskService.complete(taskId);
      fail("Exception expected: It should not be possible to complete a task");
    } catch (AuthorizationException e) {
      // then
      assertTextPresent("The user with id 'test' does not have 'UPDATE' permission on resource 'myTask' of type 'Task'.", e.getMessage());
    }

    deleteTask(taskId, true);
  }

  public void testStandaloneTaskCompleteTask() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.complete(taskId);

    // then
    Task task = selectSingleTask();
    assertNull(task);

    historyService.deleteHistoricTaskInstance(taskId);
  }

  // complete (process) task ////////////////////////////////////////////////////////////

  public void testProcessTaskCompleteTaskWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    try {
      // when
      taskService.complete(taskId);
      fail("Exception expected: It should not be possible to complete a task");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(taskId, message);
      assertTextPresent(TASK.resourceName(), message);
      assertTextPresent(UPDATE_TASK.getName(), message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testProcessTaskCompleteTaskWithUpdatePermissionOnTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.complete(taskId);

    // then
    Task task = selectSingleTask();
    assertNull(task);
  }

  public void testProcessTaskCompleteTaskWithUpdatePermissionOnAnyTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, ANY, UPDATE, userId);

    // when
    taskService.complete(taskId);

    // then
    Task task = selectSingleTask();
    assertNull(task);
  }

  public void testProcessTaskCompleteTaskWithUpdateTasksPermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, UPDATE_TASK, userId);

    // when
    taskService.complete(taskId);

    // then
    Task task = selectSingleTask();
    assertNull(task);
  }

  public void testProcessTaskCompleteTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, taskId, UPDATE, userId);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, UPDATE_TASK, userId);

    // when
    taskService.complete(taskId);

    // then
    Task task = selectSingleTask();
    assertNull(task);
  }

  // complete (case) task ////////////////////////////////////////////////////////////

  public void testCaseTaskCompleteTask() {
    // given
    createCaseInstanceByKey(CASE_KEY);
    String taskId = selectSingleTask().getId();

    // when
    taskService.complete(taskId);

    // then
    Task task = selectSingleTask();
    assertNull(task);
  }

  // delegate (standalone) task ///////////////////////////////////////////////////////

  public void testStandaloneTaskDelegateTaskWithoutAuthorization() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    try {
      // when
      taskService.delegateTask(taskId, "demo");
      fail("Exception expected: It should not be possible to delegate a task");
    } catch (AuthorizationException e) {
      // then
      assertTextPresent("The user with id 'test' does not have 'UPDATE' permission on resource 'myTask' of type 'Task'.", e.getMessage());
    }

    deleteTask(taskId, true);
  }

  public void testStandaloneTaskDelegateTask() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.delegateTask(taskId, "demo");

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("demo", task.getAssignee());

    deleteTask(taskId, true);
  }

  // delegate (process) task ///////////////////////////////////////////////////////////

  public void testProcessTaskDelegateTaskWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    try {
      // when
      taskService.delegateTask(taskId, "demo");
      fail("Exception expected: It should not be possible to delegate a task");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(taskId, message);
      assertTextPresent(TASK.resourceName(), message);
      assertTextPresent(UPDATE_TASK.getName(), message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testProcessTaskDelegateTaskWithUpdatePermissionOnTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.delegateTask(taskId, "demo");

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("demo", task.getAssignee());
  }

  public void testProcessTaskDelegateTaskWithUpdatePermissionOnAnyTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, ANY, UPDATE, userId);

    // when
    taskService.delegateTask(taskId, "demo");

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("demo", task.getAssignee());
  }

  public void testProcessTaskDelegateTaskWithUpdateTasksPermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, UPDATE_TASK, userId);

    // when
    taskService.delegateTask(taskId, "demo");

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("demo", task.getAssignee());
  }

  public void testProcessTaskDelegateTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, taskId, UPDATE, userId);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, UPDATE_TASK, userId);

    // when
    taskService.delegateTask(taskId, "demo");

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("demo", task.getAssignee());
  }

  // delegate (case) task /////////////////////////////////////////////////////////////////

  public void testCaseTaskDelegateTask() {
    // given
    createCaseInstanceByKey(CASE_KEY);
    String taskId = selectSingleTask().getId();

    // when
    taskService.delegateTask(taskId, "demo");

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("demo", task.getAssignee());
  }

  // resolve (standalone) task ///////////////////////////////////////////////////////

  public void testStandaloneTaskResolveTaskWithoutAuthorization() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    try {
      // when
      taskService.resolveTask(taskId);
      fail("Exception expected: It should not be possible to resolve a task");
    } catch (AuthorizationException e) {
      // then
      assertTextPresent("The user with id 'test' does not have 'UPDATE' permission on resource 'myTask' of type 'Task'.", e.getMessage());
    }

    deleteTask(taskId, true);
  }

  public void testStandaloneTaskResolveTask() {
    // given
    String taskId = "myTask";
    createTask(taskId);
    setAssignee(taskId, userId);
    delegateTask(taskId, "demo");

    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.resolveTask(taskId);

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals(userId, task.getAssignee());

    deleteTask(taskId, true);
  }

  // delegate (process) task ///////////////////////////////////////////////////////////

  public void testProcessTaskResolveTaskWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    try {
      // when
      taskService.resolveTask(taskId);
      fail("Exception expected: It should not be possible to resolve a task");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(taskId, message);
      assertTextPresent(TASK.resourceName(), message);
      assertTextPresent(UPDATE_TASK.getName(), message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testProcessTaskResolveTaskWithUpdatePermissionOnTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, userId);
    delegateTask(taskId, "demo");

    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.resolveTask(taskId);

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals(userId, task.getAssignee());
  }

  public void testProcessTaskResolveTaskWithUpdatePermissionOnAnyTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, userId);
    delegateTask(taskId, "demo");

    createGrantAuthorization(TASK, ANY, UPDATE, userId);

    // when
    taskService.resolveTask(taskId);

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals(userId, task.getAssignee());
  }

  public void testProcessTaskResolveTaskWithUpdateTasksPermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, userId);
    delegateTask(taskId, "demo");

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, UPDATE_TASK, userId);

    // when
    taskService.resolveTask(taskId);

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals(userId, task.getAssignee());
  }

  public void testProcessTaskResolveTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, userId);
    delegateTask(taskId, "demo");

    createGrantAuthorization(TASK, taskId, UPDATE, userId);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, UPDATE_TASK, userId);

    // when
    taskService.resolveTask(taskId);

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals(userId, task.getAssignee());
  }

  // delegate (case) task /////////////////////////////////////////////////////////////////

  public void testCaseTaskResolveTask() {
    // given
    createCaseInstanceByKey(CASE_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, userId);
    delegateTask(taskId, "demo");

    // when
    taskService.resolveTask(taskId);

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals(userId, task.getAssignee());
  }

  // set priority on standalone task /////////////////////////////////////////////

  public void testStandaloneTaskSetPriorityWithoutAuthorization() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    try {
      // when
      taskService.setPriority(taskId, 80);
      fail("Exception expected: It should not be possible to set a priority");
    } catch (AuthorizationException e) {
      // then
      assertTextPresent("The user with id 'test' does not have 'UPDATE' permission on resource 'myTask' of type 'Task'", e.getMessage());
    }

    deleteTask(taskId, true);
  }

  public void testStandaloneTaskSetPriority() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.setPriority(taskId, 80);

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals(80, task.getPriority());

    deleteTask(taskId, true);
  }

  // set priority on process task /////////////////////////////////////////////

  public void testProcessTaskSetPriorityWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    try {
      // when
      taskService.setPriority(taskId, 80);
      fail("Exception expected: It should not be possible to set a priority");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(taskId, message);
      assertTextPresent(TASK.resourceName(), message);
      assertTextPresent(UPDATE_TASK.getName(), message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testProcessTaskSetPriorityWithUpdatePersmissionOnTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.setPriority(taskId, 80);

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals(80, task.getPriority());
  }

  public void testProcessTaskSetPriorityWithUpdatePersmissionOnAnyTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, ANY, UPDATE, userId);

    // when
    taskService.setPriority(taskId, 80);

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals(80, task.getPriority());
  }

  public void testProcessTaskSetPriorityWithUpdateTasksPersmissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, UPDATE_TASK, userId);

    // when
    taskService.setPriority(taskId, 80);

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals(80, task.getPriority());
  }

  public void testProcessTaskSetPriority() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, taskId, UPDATE, userId);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, UPDATE_TASK, userId);

    // when
    taskService.setPriority(taskId, 80);

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals(80, task.getPriority());
  }

  // set priority on case task /////////////////////////////////////////////

  public void testCaseTaskSetPriority() {
    // given
    createCaseInstanceByKey(CASE_KEY);
    String taskId = selectSingleTask().getId();

    // when
    taskService.setPriority(taskId, 80);

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals(80, task.getPriority());
  }

  // get sub tasks ((standalone) task) ////////////////////////////////////

  public void testStandaloneTaskGetSubTasksWithoutAuthorization() {
    // given
    String parentTaskId = "parentTaskId";
    createTask(parentTaskId);

    disableAuthorization();
    Task sub1 = taskService.newTask("sub1");
    sub1.setParentTaskId(parentTaskId);
    taskService.saveTask(sub1);

    Task sub2 = taskService.newTask("sub2");
    sub2.setParentTaskId(parentTaskId);
    taskService.saveTask(sub2);
    enableAuthorization();

    // when
    List<Task> subTasks = taskService.getSubTasks(parentTaskId);

    // then
    assertTrue(subTasks.isEmpty());

    deleteTask(parentTaskId, true);
  }

  public void testStandaloneTaskGetSubTasksWithReadPermissionOnSub1() {
    // given
    String parentTaskId = "parentTaskId";
    createTask(parentTaskId);

    disableAuthorization();
    Task sub1 = taskService.newTask("sub1");
    sub1.setParentTaskId(parentTaskId);
    taskService.saveTask(sub1);

    Task sub2 = taskService.newTask("sub2");
    sub2.setParentTaskId(parentTaskId);
    taskService.saveTask(sub2);
    enableAuthorization();

    createGrantAuthorization(TASK, "sub1", READ, userId);

    // when
    List<Task> subTasks = taskService.getSubTasks(parentTaskId);

    // then
    assertFalse(subTasks.isEmpty());
    assertEquals(1, subTasks.size());

    assertEquals("sub1", subTasks.get(0).getId());

    deleteTask(parentTaskId, true);
  }

  public void testStandaloneTaskGetSubTasks() {
    // given
    String parentTaskId = "parentTaskId";
    createTask(parentTaskId);

    disableAuthorization();
    Task sub1 = taskService.newTask("sub1");
    sub1.setParentTaskId(parentTaskId);
    taskService.saveTask(sub1);

    Task sub2 = taskService.newTask("sub2");
    sub2.setParentTaskId(parentTaskId);
    taskService.saveTask(sub2);
    enableAuthorization();

    createGrantAuthorization(TASK, ANY, READ, userId);

    // when
    List<Task> subTasks = taskService.getSubTasks(parentTaskId);

    // then
    assertFalse(subTasks.isEmpty());
    assertEquals(2, subTasks.size());

    deleteTask(parentTaskId, true);
  }

  // get sub tasks ((process) task) ////////////////////////////////////

  public void testProcessTaskGetSubTasksWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String parentTaskId = selectSingleTask().getId();

    disableAuthorization();
    Task sub1 = taskService.newTask("sub1");
    sub1.setParentTaskId(parentTaskId);
    taskService.saveTask(sub1);

    Task sub2 = taskService.newTask("sub2");
    sub2.setParentTaskId(parentTaskId);
    taskService.saveTask(sub2);
    enableAuthorization();

    // when
    List<Task> subTasks = taskService.getSubTasks(parentTaskId);

    // then
    assertTrue(subTasks.isEmpty());
  }

  public void testProcessTaskGetSubTasksWithReadPermissionOnSub1() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String parentTaskId = selectSingleTask().getId();

    disableAuthorization();
    Task sub1 = taskService.newTask("sub1");
    sub1.setParentTaskId(parentTaskId);
    taskService.saveTask(sub1);

    Task sub2 = taskService.newTask("sub2");
    sub2.setParentTaskId(parentTaskId);
    taskService.saveTask(sub2);
    enableAuthorization();

    createGrantAuthorization(TASK, "sub1", READ, userId);

    // when
    List<Task> subTasks = taskService.getSubTasks(parentTaskId);

    // then
    assertFalse(subTasks.isEmpty());
    assertEquals(1, subTasks.size());

    assertEquals("sub1", subTasks.get(0).getId());
  }

  public void testProcessTaskGetSubTasks() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String parentTaskId = selectSingleTask().getId();

    disableAuthorization();
    Task sub1 = taskService.newTask("sub1");
    sub1.setParentTaskId(parentTaskId);
    taskService.saveTask(sub1);

    Task sub2 = taskService.newTask("sub2");
    sub2.setParentTaskId(parentTaskId);
    taskService.saveTask(sub2);
    enableAuthorization();

    createGrantAuthorization(TASK, ANY, READ, userId);

    // when
    List<Task> subTasks = taskService.getSubTasks(parentTaskId);

    // then
    assertFalse(subTasks.isEmpty());
    assertEquals(2, subTasks.size());
  }

  // get sub tasks ((case) task) ////////////////////////////////////

  public void testCaseTaskGetSubTasksWithoutAuthorization() {
    // given
    createCaseInstanceByKey(CASE_KEY);
    String parentTaskId = selectSingleTask().getId();

    disableAuthorization();
    Task sub1 = taskService.newTask("sub1");
    sub1.setParentTaskId(parentTaskId);
    taskService.saveTask(sub1);

    Task sub2 = taskService.newTask("sub2");
    sub2.setParentTaskId(parentTaskId);
    taskService.saveTask(sub2);
    enableAuthorization();

    // when
    List<Task> subTasks = taskService.getSubTasks(parentTaskId);

    // then
    assertTrue(subTasks.isEmpty());
  }

  public void testCaseTaskGetSubTasksWithReadPermissionOnSub1() {
    // given
    createCaseInstanceByKey(CASE_KEY);
    String parentTaskId = selectSingleTask().getId();

    disableAuthorization();
    Task sub1 = taskService.newTask("sub1");
    sub1.setParentTaskId(parentTaskId);
    taskService.saveTask(sub1);

    Task sub2 = taskService.newTask("sub2");
    sub2.setParentTaskId(parentTaskId);
    taskService.saveTask(sub2);
    enableAuthorization();

    createGrantAuthorization(TASK, "sub1", READ, userId);

    // when
    List<Task> subTasks = taskService.getSubTasks(parentTaskId);

    // then
    assertFalse(subTasks.isEmpty());
    assertEquals(1, subTasks.size());

    assertEquals("sub1", subTasks.get(0).getId());
  }

  public void testCaseTaskGetSubTasks() {
    // given
    createCaseInstanceByKey(CASE_KEY);
    String parentTaskId = selectSingleTask().getId();

    disableAuthorization();
    Task sub1 = taskService.newTask("sub1");
    sub1.setParentTaskId(parentTaskId);
    taskService.saveTask(sub1);

    Task sub2 = taskService.newTask("sub2");
    sub2.setParentTaskId(parentTaskId);
    taskService.saveTask(sub2);
    enableAuthorization();

    createGrantAuthorization(TASK, ANY, READ, userId);

    // when
    List<Task> subTasks = taskService.getSubTasks(parentTaskId);

    // then
    assertFalse(subTasks.isEmpty());
    assertEquals(2, subTasks.size());
  }

  // clear authorization ((standalone) task) ////////////////////////

  public void testStandaloneTaskClearAuthorization() {
    // given
    String taskId = "myTask";
    createTask(taskId);
    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    disableAuthorization();
    Authorization authorization = authorizationService
        .createAuthorizationQuery()
        .resourceId(taskId)
        .singleResult();
    enableAuthorization();
    assertNotNull(authorization);

    // when
    taskService.complete(taskId);

    // then
    disableAuthorization();
    authorization = authorizationService
        .createAuthorizationQuery()
        .resourceId(taskId)
        .singleResult();
    enableAuthorization();

    assertNull(authorization);

    deleteTask(taskId, true);
  }

  // clear authorization ((process) task) ////////////////////////

  public void testProcessTaskClearAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    disableAuthorization();
    Authorization authorization = authorizationService
        .createAuthorizationQuery()
        .resourceId(taskId)
        .singleResult();
    enableAuthorization();
    assertNotNull(authorization);

    // when
    taskService.complete(taskId);

    // then
    disableAuthorization();
    authorization = authorizationService
        .createAuthorizationQuery()
        .resourceId(taskId)
        .singleResult();
    enableAuthorization();

    assertNull(authorization);
  }

  // set assignee -> an authorization is available (standalone task) /////////////////////////////////////////

  public void testStandaloneTaskSetAssigneeCreateNewAuthorization() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.setAssignee(taskId, "demo");

    // then
    disableAuthorization();
    Authorization authorization = authorizationService
      .createAuthorizationQuery()
      .userIdIn("demo")
      .singleResult();
    enableAuthorization();

    assertNotNull(authorization);
    assertEquals(TASK.resourceType(), authorization.getResourceType());
    assertEquals(taskId, authorization.getResourceId());
    assertTrue(authorization.isPermissionGranted(READ));
    assertTrue(authorization.isPermissionGranted(UPDATE));

    deleteTask(taskId, true);
  }

  public void testStandaloneTaskSetAssigneeUpdateAuthorization() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    createGrantAuthorization(TASK, taskId, UPDATE, userId);
    createGrantAuthorization(TASK, taskId, DELETE, "demo");

    // when
    taskService.setAssignee(taskId, "demo");

    // then
    disableAuthorization();
    Authorization authorization = authorizationService
      .createAuthorizationQuery()
      .userIdIn("demo")
      .singleResult();
    enableAuthorization();

    assertNotNull(authorization);
    assertEquals(TASK.resourceType(), authorization.getResourceType());
    assertEquals(taskId, authorization.getResourceId());
    assertTrue(authorization.isPermissionGranted(READ));
    assertTrue(authorization.isPermissionGranted(UPDATE));

    deleteTask(taskId, true);
  }

  public void testStandaloneTaskSetAssigneeToNullAuthorizationStillAvailable() {
    // given
    String taskId = "myTask";
    createTask(taskId);
    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // set assignee to demo -> an authorization for demo is available
    taskService.setAssignee(taskId, "demo");

    // when
    taskService.setAssignee(taskId, null);

    // then
    // authorization for demo is still available
    disableAuthorization();
    Authorization authorization = authorizationService
      .createAuthorizationQuery()
      .userIdIn("demo")
      .singleResult();
    enableAuthorization();

    assertNotNull(authorization);
    assertEquals(TASK.resourceType(), authorization.getResourceType());
    assertEquals(taskId, authorization.getResourceId());
    assertTrue(authorization.isPermissionGranted(READ));
    assertTrue(authorization.isPermissionGranted(UPDATE));

    deleteTask(taskId, true);
  }

  public void testQueryStandaloneTaskSetAssignee() {
    // given
    String taskId = "myTask";
    createTask(taskId);
    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // set assignee to demo -> an authorization for demo is available
    taskService.setAssignee(taskId, "demo");

    identityService.clearAuthentication();
    identityService.setAuthentication("demo", null);

    // when
    Task task = taskService.createTaskQuery().singleResult();

    // then
    assertNotNull(task);
    assertEquals(taskId, task.getId());

    identityService.clearAuthentication();
    identityService.setAuthentication(userId, Arrays.asList(groupId));
    deleteTask(taskId, true);
  }

  public void testStandaloneTaskSetAssigneeOutsideCommandContextInsert() {
    // given
    String taskId = "myTask";
    createGrantAuthorization(TASK, ANY, CREATE, userId);
    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    Task task = taskService.newTask(taskId);
    task.setAssignee("demo");

    // when
    taskService.saveTask(task);

    // then
    disableAuthorization();
    Authorization authorization = authorizationService
      .createAuthorizationQuery()
      .userIdIn("demo")
      .singleResult();
    enableAuthorization();

    assertNotNull(authorization);
    assertEquals(TASK.resourceType(), authorization.getResourceType());
    assertEquals(taskId, authorization.getResourceId());
    assertTrue(authorization.isPermissionGranted(READ));
    assertTrue(authorization.isPermissionGranted(UPDATE));

    deleteTask(taskId, true);
  }

  public void testStandaloneTaskSetAssigneeOutsideCommandContextSave() {
    // given
    String taskId = "myTask";
    createTask(taskId);
    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    Task task = selectSingleTask();

    task.setAssignee("demo");

    // when
    taskService.saveTask(task);

    // then
    disableAuthorization();
    Authorization authorization = authorizationService
      .createAuthorizationQuery()
      .userIdIn("demo")
      .singleResult();
    enableAuthorization();

    assertNotNull(authorization);
    assertEquals(TASK.resourceType(), authorization.getResourceType());
    assertEquals(taskId, authorization.getResourceId());
    assertTrue(authorization.isPermissionGranted(READ));
    assertTrue(authorization.isPermissionGranted(UPDATE));

    deleteTask(taskId, true);
  }

  // set assignee -> an authorization is available (process task) /////////////////////////////////////////

  public void testProcessTaskSetAssigneeCreateNewAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.setAssignee(taskId, "demo");

    // then
    disableAuthorization();
    Authorization authorization = authorizationService
      .createAuthorizationQuery()
      .userIdIn("demo")
      .singleResult();
    enableAuthorization();

    assertNotNull(authorization);
    assertEquals(TASK.resourceType(), authorization.getResourceType());
    assertEquals(taskId, authorization.getResourceId());
    assertTrue(authorization.isPermissionGranted(READ));
    assertTrue(authorization.isPermissionGranted(UPDATE));
  }

  public void testProcessTaskSetAssigneeUpdateAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, taskId, UPDATE, userId);
    createGrantAuthorization(TASK, taskId, DELETE, "demo");

    // when
    taskService.setAssignee(taskId, "demo");

    // then
    disableAuthorization();
    Authorization authorization = authorizationService
      .createAuthorizationQuery()
      .userIdIn("demo")
      .singleResult();
    enableAuthorization();

    assertNotNull(authorization);
    assertEquals(TASK.resourceType(), authorization.getResourceType());
    assertEquals(taskId, authorization.getResourceId());
    assertTrue(authorization.isPermissionGranted(READ));
    assertTrue(authorization.isPermissionGranted(UPDATE));
  }

  public void testProcessTaskSetAssigneeToNullAuthorizationStillAvailable() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // set assignee to demo -> an authorization for demo is available
    taskService.setAssignee(taskId, "demo");

    // when
    taskService.setAssignee(taskId, null);

    // then
    // authorization for demo is still available
    disableAuthorization();
    Authorization authorization = authorizationService
      .createAuthorizationQuery()
      .userIdIn("demo")
      .singleResult();
    enableAuthorization();

    assertNotNull(authorization);
    assertEquals(TASK.resourceType(), authorization.getResourceType());
    assertEquals(taskId, authorization.getResourceId());
    assertTrue(authorization.isPermissionGranted(READ));
    assertTrue(authorization.isPermissionGranted(UPDATE));
  }

  public void testQueryProcessTaskSetAssignee() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // set assignee to demo -> an authorization for demo is available
    taskService.setAssignee(taskId, "demo");

    identityService.clearAuthentication();
    identityService.setAuthentication("demo", null);

    // when
    Task task = taskService.createTaskQuery().singleResult();

    // then
    assertNotNull(task);
    assertEquals(taskId, task.getId());

    identityService.clearAuthentication();
    identityService.setAuthentication(userId, Arrays.asList(groupId));
  }

  public void testProcessTaskAssignee() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, DEMO_ASSIGNEE_PROCESS_KEY, CREATE_INSTANCE, userId);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, CREATE, userId);

    // when
    runtimeService.startProcessInstanceByKey(DEMO_ASSIGNEE_PROCESS_KEY);

    // then
    // an authorization for demo has been created
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    Authorization authorization = authorizationService
      .createAuthorizationQuery()
      .userIdIn("demo")
      .singleResult();
    enableAuthorization();

    assertNotNull(authorization);
    assertEquals(TASK.resourceType(), authorization.getResourceType());
    assertEquals(taskId, authorization.getResourceId());
    assertTrue(authorization.isPermissionGranted(READ));
    assertTrue(authorization.isPermissionGranted(UPDATE));

    // demo is able to retrieve the task
    identityService.clearAuthentication();
    identityService.setAuthentication("demo", null);

    Task task = taskService.createTaskQuery().singleResult();

    assertNotNull(task);
    assertEquals(taskId, task.getId());

    identityService.clearAuthentication();
    identityService.setAuthentication(userId, Arrays.asList(groupId));
  }

  // set assignee -> should not create an authorization (case task) /////////////////////////////////////////

  public void testCaseTaskSetAssigneeNoAuthorization() {
    // given
    createCaseInstanceByKey(CASE_KEY);
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.setAssignee(taskId, "demo");

    // then
    disableAuthorization();
    Authorization authorization = authorizationService
      .createAuthorizationQuery()
      .userIdIn("demo")
      .singleResult();
    enableAuthorization();

    assertNull(authorization);
  }

  // set owner -> an authorization is available (standalone task) /////////////////////////////////////////

  public void testStandaloneTaskSetOwnerCreateNewAuthorization() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.setOwner(taskId, "demo");

    // then
    disableAuthorization();
    Authorization authorization = authorizationService
      .createAuthorizationQuery()
      .userIdIn("demo")
      .singleResult();
    enableAuthorization();

    assertNotNull(authorization);
    assertEquals(TASK.resourceType(), authorization.getResourceType());
    assertEquals(taskId, authorization.getResourceId());
    assertTrue(authorization.isPermissionGranted(READ));
    assertTrue(authorization.isPermissionGranted(UPDATE));

    deleteTask(taskId, true);
  }

  public void testStandaloneTaskSetOwnerUpdateAuthorization() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    createGrantAuthorization(TASK, taskId, UPDATE, userId);
    createGrantAuthorization(TASK, taskId, DELETE, "demo");

    // when
    taskService.setOwner(taskId, "demo");

    // then
    disableAuthorization();
    Authorization authorization = authorizationService
      .createAuthorizationQuery()
      .userIdIn("demo")
      .singleResult();
    enableAuthorization();

    assertNotNull(authorization);
    assertEquals(TASK.resourceType(), authorization.getResourceType());
    assertEquals(taskId, authorization.getResourceId());
    assertTrue(authorization.isPermissionGranted(READ));
    assertTrue(authorization.isPermissionGranted(UPDATE));

    deleteTask(taskId, true);
  }

  public void testQueryStandaloneTaskSetOwner() {
    String taskId = "myTask";
    createTask(taskId);
    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // set owner to demo -> an authorization for demo is available
    taskService.setOwner(taskId, "demo");

    identityService.clearAuthentication();
    identityService.setAuthentication("demo", null);

    // when
    Task task = taskService.createTaskQuery().singleResult();

    // then
    assertNotNull(task);
    assertEquals(taskId, task.getId());

    identityService.clearAuthentication();
    identityService.setAuthentication(userId, Arrays.asList(groupId));
    deleteTask(taskId, true);
  }

  public void testStandaloneTaskSetOwnerOutsideCommandContextInsert() {
    // given
    String taskId = "myTask";
    createGrantAuthorization(TASK, ANY, CREATE, userId);
    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    Task task = taskService.newTask(taskId);
    task.setOwner("demo");

    // when
    taskService.saveTask(task);

    // then
    disableAuthorization();
    Authorization authorization = authorizationService
      .createAuthorizationQuery()
      .userIdIn("demo")
      .singleResult();
    enableAuthorization();

    assertNotNull(authorization);
    assertEquals(TASK.resourceType(), authorization.getResourceType());
    assertEquals(taskId, authorization.getResourceId());
    assertTrue(authorization.isPermissionGranted(READ));
    assertTrue(authorization.isPermissionGranted(UPDATE));

    deleteTask(taskId, true);
  }

  public void testStandaloneTaskSetOwnerOutsideCommandContextSave() {
    // given
    String taskId = "myTask";
    createTask(taskId);
    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    Task task = selectSingleTask();

    task.setOwner("demo");

    // when
    taskService.saveTask(task);

    // then
    disableAuthorization();
    Authorization authorization = authorizationService
      .createAuthorizationQuery()
      .userIdIn("demo")
      .singleResult();
    enableAuthorization();

    assertNotNull(authorization);
    assertEquals(TASK.resourceType(), authorization.getResourceType());
    assertEquals(taskId, authorization.getResourceId());
    assertTrue(authorization.isPermissionGranted(READ));
    assertTrue(authorization.isPermissionGranted(UPDATE));

    deleteTask(taskId, true);
  }

  // set owner -> an authorization is available (process task) /////////////////////////////////////////

  public void testProcessTaskSetOwnerCreateNewAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.setOwner(taskId, "demo");

    // then
    disableAuthorization();
    Authorization authorization = authorizationService
      .createAuthorizationQuery()
      .userIdIn("demo")
      .singleResult();
    enableAuthorization();

    assertNotNull(authorization);
    assertEquals(TASK.resourceType(), authorization.getResourceType());
    assertEquals(taskId, authorization.getResourceId());
    assertTrue(authorization.isPermissionGranted(READ));
    assertTrue(authorization.isPermissionGranted(UPDATE));
  }

  public void testProcessTaskSetOwnerUpdateAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, taskId, UPDATE, userId);
    createGrantAuthorization(TASK, taskId, DELETE, "demo");

    // when
    taskService.setOwner(taskId, "demo");

    // then
    disableAuthorization();
    Authorization authorization = authorizationService
      .createAuthorizationQuery()
      .userIdIn("demo")
      .singleResult();
    enableAuthorization();

    assertNotNull(authorization);
    assertEquals(TASK.resourceType(), authorization.getResourceType());
    assertEquals(taskId, authorization.getResourceId());
    assertTrue(authorization.isPermissionGranted(READ));
    assertTrue(authorization.isPermissionGranted(UPDATE));
  }

  public void testQueryProcessTaskSetOwner() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // set owner to demo -> an authorization for demo is available
    taskService.setOwner(taskId, "demo");

    identityService.clearAuthentication();
    identityService.setAuthentication("demo", null);

    // when
    Task task = taskService.createTaskQuery().singleResult();

    // then
    assertNotNull(task);
    assertEquals(taskId, task.getId());

    identityService.clearAuthentication();
    identityService.setAuthentication(userId, Arrays.asList(groupId));
  }

  // set owner -> should not create an authorization  (case task) /////////////////////////////////

  public void testCaseTaskSetOwnerNoAuthorization() {
    // given
    createCaseInstanceByKey(CASE_KEY);
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.setOwner(taskId, "demo");

    // then
    disableAuthorization();
    Authorization authorization = authorizationService
      .createAuthorizationQuery()
      .userIdIn("demo")
      .singleResult();
    enableAuthorization();

    assertNull(authorization);
  }

  // add candidate user -> an authorization is available (standalone task) /////////////////

  public void testStandaloneTaskAddCandidateUserCreateNewAuthorization() {
    // given
    String taskId = "myTask";
    createTask(taskId);
    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.addCandidateUser(taskId, "demo");

    // then
    disableAuthorization();
    Authorization authorization = authorizationService
      .createAuthorizationQuery()
      .userIdIn("demo")
      .singleResult();
    enableAuthorization();

    assertNotNull(authorization);
    assertEquals(TASK.resourceType(), authorization.getResourceType());
    assertEquals(taskId, authorization.getResourceId());
    assertTrue(authorization.isPermissionGranted(READ));
    assertTrue(authorization.isPermissionGranted(UPDATE));

    deleteTask(taskId, true);
  }

  public void testStandaloneTaskAddCandidateUserUpdateAuthorization() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    createGrantAuthorization(TASK, taskId, UPDATE, userId);
    createGrantAuthorization(TASK, taskId, DELETE, "demo");

    // when
    taskService.addCandidateUser(taskId, "demo");

    // then
    disableAuthorization();
    Authorization authorization = authorizationService
      .createAuthorizationQuery()
      .userIdIn("demo")
      .singleResult();
    enableAuthorization();

    assertNotNull(authorization);
    assertEquals(TASK.resourceType(), authorization.getResourceType());
    assertEquals(taskId, authorization.getResourceId());
    assertTrue(authorization.isPermissionGranted(READ));
    assertTrue(authorization.isPermissionGranted(UPDATE));

    deleteTask(taskId, true);
  }

  public void testQueryStandaloneTaskAddCandidateUser() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // add candidate user -> an authorization for demo is available
    taskService.addCandidateUser(taskId, "demo");

    identityService.clearAuthentication();
    identityService.setAuthentication("demo", null);

    // when
    Task task = taskService.createTaskQuery().singleResult();

    // then
    assertNotNull(task);
    assertEquals(taskId, task.getId());

    identityService.clearAuthentication();
    identityService.setAuthentication(userId, Arrays.asList(groupId));
    deleteTask(taskId, true);
  }

  // add candidate user -> an authorization is available (process task) ////////////////////

  public void testProcessTaskAddCandidateUserCreateNewAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.addCandidateUser(taskId, "demo");

    // then
    disableAuthorization();
    Authorization authorization = authorizationService
      .createAuthorizationQuery()
      .userIdIn("demo")
      .singleResult();
    enableAuthorization();

    assertNotNull(authorization);
    assertEquals(TASK.resourceType(), authorization.getResourceType());
    assertEquals(taskId, authorization.getResourceId());
    assertTrue(authorization.isPermissionGranted(READ));
    assertTrue(authorization.isPermissionGranted(UPDATE));
  }

  public void testProcessTaskAddCandidateUserUpdateAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, taskId, UPDATE, userId);
    createGrantAuthorization(TASK, taskId, DELETE, "demo");

    // when
    taskService.addCandidateUser(taskId, "demo");

    // then
    disableAuthorization();
    Authorization authorization = authorizationService
      .createAuthorizationQuery()
      .userIdIn("demo")
      .singleResult();
    enableAuthorization();

    assertNotNull(authorization);
    assertEquals(TASK.resourceType(), authorization.getResourceType());
    assertEquals(taskId, authorization.getResourceId());
    assertTrue(authorization.isPermissionGranted(READ));
    assertTrue(authorization.isPermissionGranted(UPDATE));
  }

  public void testQueryProcessTaskAddCandidateUser() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // add candidate user -> an authorization for demo is available
    taskService.addCandidateUser(taskId, "demo");

    identityService.clearAuthentication();
    identityService.setAuthentication("demo", null);

    // when
    Task task = taskService.createTaskQuery().singleResult();

    // then
    assertNotNull(task);
    assertEquals(taskId, task.getId());

    identityService.clearAuthentication();
    identityService.setAuthentication(userId, Arrays.asList(groupId));
  }

  public void testProcessTaskCandidateUsers() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, CANDIDATE_USERS_PROCESS_KEY, CREATE_INSTANCE, userId);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, CREATE, userId);

    // when
    runtimeService.startProcessInstanceByKey(CANDIDATE_USERS_PROCESS_KEY);

    // then
    // an authorization for demo has been created
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    Authorization authorization = authorizationService
      .createAuthorizationQuery()
      .userIdIn("demo")
      .singleResult();
    enableAuthorization();

    assertNotNull(authorization);
    assertEquals(TASK.resourceType(), authorization.getResourceType());
    assertEquals(taskId, authorization.getResourceId());
    assertTrue(authorization.isPermissionGranted(READ));
    assertTrue(authorization.isPermissionGranted(UPDATE));

    // an authorization for test has been created
    disableAuthorization();
    authorization = authorizationService
      .createAuthorizationQuery()
      .userIdIn("test")
      .resourceId(taskId)
      .singleResult();
    enableAuthorization();

    assertNotNull(authorization);
    assertEquals(TASK.resourceType(), authorization.getResourceType());
    assertEquals(taskId, authorization.getResourceId());
    assertTrue(authorization.isPermissionGranted(READ));
    assertTrue(authorization.isPermissionGranted(UPDATE));

    // demo is able to retrieve the task
    identityService.clearAuthentication();
    identityService.setAuthentication("demo", null);

    Task task = taskService.createTaskQuery().singleResult();

    assertNotNull(task);
    assertEquals(taskId, task.getId());

    // test is able to retrieve the task
    identityService.clearAuthentication();
    identityService.setAuthentication(userId, Arrays.asList(groupId));

    task = taskService.createTaskQuery().singleResult();

    assertNotNull(task);
    assertEquals(taskId, task.getId());
  }

  // add candidate user -> should not create an authorization  (case task) /////////////////////////////////

  public void testCaseTaskAddCandidateUserNoAuthorization() {
    // given
    createCaseInstanceByKey(CASE_KEY);
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.addCandidateUser(taskId, "demo");

    // then
    disableAuthorization();
    Authorization authorization = authorizationService
      .createAuthorizationQuery()
      .userIdIn("demo")
      .singleResult();
    enableAuthorization();

    assertNull(authorization);
  }

  // add candidate group -> an authorization is available (standalone task) /////////////////

  public void testStandaloneTaskAddCandidateGroupCreateNewAuthorization() {
    // given
    String taskId = "myTask";
    createTask(taskId);
    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.addCandidateGroup(taskId, "management");

    // then
    disableAuthorization();
    Authorization authorization = authorizationService
      .createAuthorizationQuery()
      .groupIdIn("management")
      .singleResult();
    enableAuthorization();

    assertNotNull(authorization);
    assertEquals(TASK.resourceType(), authorization.getResourceType());
    assertEquals(taskId, authorization.getResourceId());
    assertTrue(authorization.isPermissionGranted(READ));
    assertTrue(authorization.isPermissionGranted(UPDATE));

    deleteTask(taskId, true);
  }

  public void testStandaloneTaskAddCandidateGroupUpdateAuthorization() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    createGrantAuthorization(TASK, taskId, UPDATE, userId);
    createGrantAuthorization(TASK, taskId, DELETE, "demo");

    // when
    taskService.addCandidateGroup(taskId, "management");

    // then
    disableAuthorization();
    Authorization authorization = authorizationService
      .createAuthorizationQuery()
      .groupIdIn("management")
      .singleResult();
    enableAuthorization();

    assertNotNull(authorization);
    assertEquals(TASK.resourceType(), authorization.getResourceType());
    assertEquals(taskId, authorization.getResourceId());
    assertTrue(authorization.isPermissionGranted(READ));
    assertTrue(authorization.isPermissionGranted(UPDATE));

    deleteTask(taskId, true);
  }

  public void testQueryStandaloneTaskAddCandidateGroup() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // add candidate group -> an authorization for group management is available
    taskService.addCandidateGroup(taskId, "management");

    identityService.clearAuthentication();
    identityService.setAuthentication("demo", Arrays.asList("management"));

    // when
    Task task = taskService.createTaskQuery().singleResult();

    // then
    assertNotNull(task);
    assertEquals(taskId, task.getId());

    identityService.clearAuthentication();
    identityService.setAuthentication(userId, Arrays.asList(groupId));
    deleteTask(taskId, true);
  }

  // add candidate group -> an authorization is available (process task) ////////////////////

  public void testProcessTaskAddCandidateGroupCreateNewAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.addCandidateGroup(taskId, "management");

    // then
    disableAuthorization();
    Authorization authorization = authorizationService
      .createAuthorizationQuery()
      .groupIdIn("management")
      .singleResult();
    enableAuthorization();

    assertNotNull(authorization);
    assertEquals(TASK.resourceType(), authorization.getResourceType());
    assertEquals(taskId, authorization.getResourceId());
    assertTrue(authorization.isPermissionGranted(READ));
    assertTrue(authorization.isPermissionGranted(UPDATE));
  }

  public void testProcessTaskAddCandidateGroupUpdateAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, taskId, UPDATE, userId);
    createGrantAuthorization(TASK, taskId, DELETE, "demo");

    // when
    taskService.addCandidateGroup(taskId, "management");

    // then
    disableAuthorization();
    Authorization authorization = authorizationService
      .createAuthorizationQuery()
      .groupIdIn("management")
      .singleResult();
    enableAuthorization();

    assertNotNull(authorization);
    assertEquals(TASK.resourceType(), authorization.getResourceType());
    assertEquals(taskId, authorization.getResourceId());
    assertTrue(authorization.isPermissionGranted(READ));
    assertTrue(authorization.isPermissionGranted(UPDATE));
  }

  public void testQueryProcessTaskAddCandidateGroup() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // add candidate group -> an authorization for group management is available
    taskService.addCandidateGroup(taskId, "management");

    identityService.clearAuthentication();
    identityService.setAuthentication("demo", Arrays.asList("management"));

    // when
    Task task = taskService.createTaskQuery().singleResult();

    // then
    assertNotNull(task);
    assertEquals(taskId, task.getId());

    identityService.clearAuthentication();
    identityService.setAuthentication(userId, Arrays.asList(groupId));
  }

  public void testProcessTaskCandidateGroups() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, CANDIDATE_GROUPS_PROCESS_KEY, CREATE_INSTANCE, userId);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, CREATE, userId);

    // when
    runtimeService.startProcessInstanceByKey(CANDIDATE_GROUPS_PROCESS_KEY);

    // then
    // an authorization for management has been created
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    Authorization authorization = authorizationService
      .createAuthorizationQuery()
      .groupIdIn("management")
      .singleResult();
    enableAuthorization();

    assertNotNull(authorization);
    assertEquals(TASK.resourceType(), authorization.getResourceType());
    assertEquals(taskId, authorization.getResourceId());
    assertTrue(authorization.isPermissionGranted(READ));
    assertTrue(authorization.isPermissionGranted(UPDATE));

    // an authorization for accounting has been created
    disableAuthorization();
    authorization = authorizationService
      .createAuthorizationQuery()
      .groupIdIn("accounting")
      .singleResult();
    enableAuthorization();

    assertNotNull(authorization);
    assertEquals(TASK.resourceType(), authorization.getResourceType());
    assertEquals(taskId, authorization.getResourceId());
    assertTrue(authorization.isPermissionGranted(READ));
    assertTrue(authorization.isPermissionGranted(UPDATE));

    // management is able to retrieve the task
    identityService.clearAuthentication();
    identityService.setAuthentication("demo", Arrays.asList("management"));

    Task task = taskService.createTaskQuery().singleResult();

    assertNotNull(task);
    assertEquals(taskId, task.getId());

    // accounting is able to retrieve the task
    identityService.clearAuthentication();
    identityService.setAuthentication(userId, Arrays.asList(groupId));

    task = taskService.createTaskQuery().singleResult();

    assertNotNull(task);
    assertEquals(taskId, task.getId());
  }

  // add candidate group -> should not create an authorization (case task) /////////////////////////////////

  public void testCaseTaskAddCandidateGroupNoAuthorization() {
    // given
    createCaseInstanceByKey(CASE_KEY);
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(TASK, taskId, UPDATE, userId);

    // when
    taskService.addCandidateGroup(taskId, "management");

    // then
    disableAuthorization();
    Authorization authorization = authorizationService
      .createAuthorizationQuery()
      .groupIdIn("management")
      .singleResult();
    enableAuthorization();

    assertNull(authorization);
  }

  // helper ////////////////////////////////////////////////////////////////////////////////

  protected void verifyQueryResults(TaskQuery query, int countExpected) {
    verifyQueryResults((AbstractQuery<?, ?>) query, countExpected);
  }

}
