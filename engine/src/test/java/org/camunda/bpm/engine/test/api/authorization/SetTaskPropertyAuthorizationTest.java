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

package org.camunda.bpm.engine.test.api.authorization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.TASK_ASSIGN;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE_TASK;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.TASK;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.util.CleanupTask;
import org.camunda.bpm.engine.test.util.ObjectProperty;
import org.camunda.bpm.engine.test.util.TriConsumer;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SetTaskPropertyAuthorizationTest extends AuthorizationTest {

  protected static final String PROCESS_KEY = "oneTaskProcess";

  @Rule
  public TestName name = new TestName();

  protected final String operationName;
  protected final TriConsumer<TaskService, String, Object> operation;
  protected final String taskId;
  protected final Object value;

  protected boolean deleteTask;

  @Parameters(name = "{0}")
  public static List<Object[]> data() {
    TriConsumer<TaskService, String, Object> setName = (taskService, taskId, value) -> taskService.setName(taskId, (String) value);
    TriConsumer<TaskService, String, Object> setDescription = (taskService, taskId, value) -> taskService.setDescription(taskId, (String) value);
    TriConsumer<TaskService, String, Object> setDueDate = (taskService, taskId, value) -> taskService.setDueDate(taskId, (Date) value);
    TriConsumer<TaskService, String, Object> setFollowUpDate = (taskService, taskId, value) -> taskService.setFollowUpDate(taskId, (Date) value);

    return Arrays.asList(new Object[][] {
        { "setName", setName, "taskId", "name" },
        { "setDescription", setDescription, "taskId", "description" },
        { "setDueDate", setDueDate, "taskId",  DateTime.now().toDate()},
        { "setFollowUpDate", setFollowUpDate, "taskId", DateTime.now().toDate() }
    });
  }

  public SetTaskPropertyAuthorizationTest(String operationName, TriConsumer<TaskService, String, Object> operation, String taskId, Object value) {
    this.operationName = operationName;
    this.operation = operation;
    this.taskId = taskId;
    this.value = value;
  }

  @Before
  public void setUp() throws Exception {
    testRule.deploy("org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml");
    super.setUp();
    deleteTask = getAnnotation(name.getMethodName(), CleanupTask.class) != null;
  }

  @After
  public void tearDown() {
    super.tearDown();
    if (deleteTask) {
      deleteTask(taskId, true);
    }
  }

  @Test
  @CleanupTask
  public void setOperationWithoutAuthorization() {
    // given
    createTask(taskId);

    try {
      // when
      operation.accept(taskService, taskId, value);
      fail("Exception expected: It should not be possible to " + operationName);
    } catch (AuthorizationException e) {
      // then
      testRule.assertTextPresent("The user with id '" + userId + "' does not have one of the following permissions: 'TASK_ASSIGN'", e.getMessage());
    }
  }

  @Test
  @CleanupTask
  public void setOperationStandalone() {
    // given
    createTask(taskId);
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    operation.accept(taskService, taskId, value);

    // then
    Task task = selectSingleTask();

    assertThat(task).isNotNull();
    assertHasPropertyValue(task, operationName, value);
  }

  @Test
  @CleanupTask
  public void setOperationWithTaskAssignPermission() {
    // given
    createTask(taskId);
    createGrantAuthorization(TASK, taskId, userId, TASK_ASSIGN);

    // when
    operation.accept(taskService, taskId, value);

    // then
    Task task = selectSingleTask();

    assertThat(task).isNotNull();
    assertHasPropertyValue(task, operationName, value);
  }

  @Test
  public void processWithTaskAssignPermissionOnTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, taskId, userId, TASK_ASSIGN);

    // when
    operation.accept(taskService, taskId, value);

    // then
    Task task = selectSingleTask();

    assertThat(task).isNotNull();
    assertHasPropertyValue(task, operationName, value);
  }

  @Test
  public void processTaskOperationWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    try {
      // when
      operation.accept(taskService, taskId, value);
      fail("Exception expected: It should not be possible to " + operationName);
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(UPDATE.getName(), message);
      testRule.assertTextPresent(taskId, message);
      testRule.assertTextPresent(TASK.resourceName(), message);
      testRule.assertTextPresent(UPDATE_TASK.getName(), message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
  public void processWithUpdatePermissionOnAnyTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, ANY, userId, UPDATE);

    // when
    operation.accept(taskService, taskId, value);

    // then
    Task task = selectSingleTask();

    assertThat(task).isNotNull();
    assertHasPropertyValue(task, operationName, value);
  }

  @Test
  public void processWithTaskAssignPermissionOnAnyTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, ANY, userId, TASK_ASSIGN);

    // when
    operation.accept(taskService, taskId, value);

    // then
    Task task = selectSingleTask();

    assertThat(task).isNotNull();
    assertHasPropertyValue(task, operationName, value);
  }

  @Test
  public void processWithUpdateTasksPermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_TASK);

    // when
    operation.accept(taskService, taskId, value);

    // then
    Task task = selectSingleTask();

    assertThat(task).isNotNull();
    assertHasPropertyValue(task, operationName, value);
  }

  @Test
  public void processWithTaskAssignPermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, TASK_ASSIGN);

    // when
    operation.accept(taskService, taskId, value);

    // then
    Task task = selectSingleTask();

    assertThat(task).isNotNull();
    assertHasPropertyValue(task, operationName, value);
  }

  @Test
  public void processTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, taskId, userId, UPDATE);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_TASK);

    // when
    operation.accept(taskService, taskId, value);

    // then
    Task task = selectSingleTask();

    assertThat(task).isNotNull();
    assertHasPropertyValue(task, operationName, value);
  }

  @Test
  public void processWithTaskAssignPermission() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(TASK, taskId, userId, TASK_ASSIGN);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, TASK_ASSIGN);

    // when
    operation.accept(taskService, taskId, value);

    // then
    Task task = selectSingleTask();

    assertThat(task).isNotNull();
    assertHasPropertyValue(task, operationName, value);
  }

  protected void assertHasPropertyValue(Task task, String operationName, Object expectedValue) {
    try {
      Object value = ObjectProperty.ofSetterMethod(task, operationName).getValue();

      assertThat(value).isEqualTo(expectedValue);
    } catch (Exception e) {
      fail("Failed to assert property for operationName=" + operationName + " due to : " + e.getMessage());
    }
  }
  protected <T extends Annotation> T getAnnotation(String methodName, Class<T> annotation) {
    try {
      String methodWithoutParamsName = methodName.split("\\[")[0];
      Method method = this.getClass().getMethod(methodWithoutParamsName);
      return method.getAnnotation(annotation);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }
}