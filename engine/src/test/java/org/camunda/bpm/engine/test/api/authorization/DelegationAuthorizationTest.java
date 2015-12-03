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
package org.camunda.bpm.engine.test.api.authorization;

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.CREATE;
import static org.camunda.bpm.engine.authorization.Permissions.CREATE_INSTANCE;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_INSTANCE;
import static org.camunda.bpm.engine.authorization.Resources.TASK;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.form.StartFormData;
import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.runtime.VariableInstanceQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.api.authorization.service.ExecuteCommandDelegate;
import org.camunda.bpm.engine.test.api.authorization.service.ExecuteCommandListener;
import org.camunda.bpm.engine.test.api.authorization.service.ExecuteCommandTaskListener;
import org.camunda.bpm.engine.test.api.authorization.service.ExecuteQueryDelegate;
import org.camunda.bpm.engine.test.api.authorization.service.ExecuteQueryListener;
import org.camunda.bpm.engine.test.api.authorization.service.ExecuteQueryTaskListener;
import org.camunda.bpm.engine.test.api.authorization.service.MyDelegationService;
import org.camunda.bpm.engine.test.api.authorization.service.MyFormFieldValidator;
import org.camunda.bpm.engine.test.api.authorization.service.MyServiceTaskActivityBehaviorExecuteCommand;
import org.camunda.bpm.engine.test.api.authorization.service.MyServiceTaskActivityBehaviorExecuteQuery;
import org.camunda.bpm.engine.test.api.authorization.service.MyTaskService;

/**
 * @author Roman Smirnov
 *
 */
public class DelegationAuthorizationTest extends AuthorizationTest {

  public static final String DEFAULT_PROCESS_KEY = "process";

  protected void setUp() throws Exception {
    super.setUp();
    MyDelegationService.clearProperties();
    processEngineConfiguration.setAuthorizationEnabledForCustomCode(false);
  }

  @Deployment
  public void testJavaDelegateExecutesQueryAfterUserCompletesTask() {
    // given
    startProcessInstancesByKey(DEFAULT_PROCESS_KEY, 5);
    String taskId = selectAnyTask().getId();
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    taskService.complete(taskId);

    // then
    assertNotNull(MyDelegationService.CURRENT_AUTHENTICATION);
    assertEquals(userId, MyDelegationService.CURRENT_AUTHENTICATION.getUserId());

    assertEquals(Long.valueOf(5), MyDelegationService.INSTANCES_COUNT);
  }

  @Deployment
  public void testJavaDelegateExecutesCommandAfterUserCompletesTask() {
    // given
    startProcessInstanceByKey(DEFAULT_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    taskService.complete(taskId);

    // then
    assertNotNull(MyDelegationService.CURRENT_AUTHENTICATION);
    assertEquals(userId, MyDelegationService.CURRENT_AUTHENTICATION.getUserId());

    disableAuthorization();
    assertEquals(2, runtimeService.createProcessInstanceQuery().count());
    enableAuthorization();
  }

  @Deployment
  public void testJavaDelegateExecutesQueryAfterUserCompletesTaskAsDelegateExpression() {
    // given
    processEngineConfiguration.getBeans().put("myDelegate", new ExecuteQueryDelegate());

    startProcessInstancesByKey(DEFAULT_PROCESS_KEY, 5);
    String taskId = selectAnyTask().getId();
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    taskService.complete(taskId);

    // then
    assertNotNull(MyDelegationService.CURRENT_AUTHENTICATION);
    assertEquals(userId, MyDelegationService.CURRENT_AUTHENTICATION.getUserId());

    assertEquals(Long.valueOf(5), MyDelegationService.INSTANCES_COUNT);
  }

  @Deployment
  public void testJavaDelegateExecutesCommandAfterUserCompletesTaskAsDelegateExpression() {
    // given
    processEngineConfiguration.getBeans().put("myDelegate", new ExecuteCommandDelegate());

    startProcessInstanceByKey(DEFAULT_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    taskService.complete(taskId);

    // then
    assertNotNull(MyDelegationService.CURRENT_AUTHENTICATION);
    assertEquals(userId, MyDelegationService.CURRENT_AUTHENTICATION.getUserId());

    disableAuthorization();
    assertEquals(2, runtimeService.createProcessInstanceQuery().count());
    enableAuthorization();
  }

  @Deployment
  public void testJavaDelegateExecutesQueryAfterUserCompletesTaskAsExpression() {
    // given
    processEngineConfiguration.getBeans().put("myDelegate", new ExecuteQueryDelegate());

    startProcessInstancesByKey(DEFAULT_PROCESS_KEY, 5);
    String taskId = selectAnyTask().getId();
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    taskService.complete(taskId);

    // then
    assertNotNull(MyDelegationService.CURRENT_AUTHENTICATION);
    assertEquals(userId, MyDelegationService.CURRENT_AUTHENTICATION.getUserId());

    assertEquals(Long.valueOf(5), MyDelegationService.INSTANCES_COUNT);
  }

  @Deployment
  public void testJavaDelegateExecutesCommandAfterUserCompletesTaskAsExpression() {
    // given
    processEngineConfiguration.getBeans().put("myDelegate", new ExecuteCommandDelegate());

    startProcessInstanceByKey(DEFAULT_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    taskService.complete(taskId);

    // then
    assertNotNull(MyDelegationService.CURRENT_AUTHENTICATION);
    assertEquals(userId, MyDelegationService.CURRENT_AUTHENTICATION.getUserId());

    disableAuthorization();
    assertEquals(2, runtimeService.createProcessInstanceQuery().count());
    enableAuthorization();
  }

  @Deployment
  public void testCustomActivityBehaviorExecutesQueryAfterUserCompletesTask() {
    // given
    startProcessInstancesByKey(DEFAULT_PROCESS_KEY, 5);
    String taskId = selectAnyTask().getId();
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    taskService.complete(taskId);

    // then
    assertNotNull(MyDelegationService.CURRENT_AUTHENTICATION);
    assertEquals(userId, MyDelegationService.CURRENT_AUTHENTICATION.getUserId());

    assertEquals(Long.valueOf(5), MyDelegationService.INSTANCES_COUNT);
  }

  @Deployment
  public void testCustomActivityBehaviorExecutesCommandAfterUserCompletesTask() {
    // given
    startProcessInstanceByKey(DEFAULT_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    taskService.complete(taskId);

    // then
    assertNotNull(MyDelegationService.CURRENT_AUTHENTICATION);
    assertEquals(userId, MyDelegationService.CURRENT_AUTHENTICATION.getUserId());

    disableAuthorization();
    assertEquals(2, runtimeService.createProcessInstanceQuery().count());
    enableAuthorization();
  }

  @Deployment
  public void testCustomActivityBehaviorExecutesQueryAfterUserCompletesTaskAsDelegateExpression() {
    // given
    processEngineConfiguration.getBeans().put("myBehavior", new MyServiceTaskActivityBehaviorExecuteQuery());

    startProcessInstancesByKey(DEFAULT_PROCESS_KEY, 5);
    String taskId = selectAnyTask().getId();
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    taskService.complete(taskId);

    // then
    assertNotNull(MyDelegationService.CURRENT_AUTHENTICATION);
    assertEquals(userId, MyDelegationService.CURRENT_AUTHENTICATION.getUserId());

    assertEquals(Long.valueOf(5), MyDelegationService.INSTANCES_COUNT);
  }

  @Deployment
  public void testCustomActivityBehaviorExecutesCommandAfterUserCompletesTaskAsDelegateExpression() {
    // given
    processEngineConfiguration.getBeans().put("myBehavior", new MyServiceTaskActivityBehaviorExecuteCommand());

    startProcessInstanceByKey(DEFAULT_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    taskService.complete(taskId);

    // then
    assertNotNull(MyDelegationService.CURRENT_AUTHENTICATION);
    assertEquals(userId, MyDelegationService.CURRENT_AUTHENTICATION.getUserId());

    disableAuthorization();
    assertEquals(2, runtimeService.createProcessInstanceQuery().count());
    enableAuthorization();
  }

  @Deployment
  public void testSignallableActivityBehaviorAsClass() {
    // given
    startProcessInstancesByKey(DEFAULT_PROCESS_KEY, 4);
    String processInstanceId = startProcessInstanceByKey(DEFAULT_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    // when
    runtimeService.signal(processInstanceId);

    // then
    assertNotNull(MyDelegationService.CURRENT_AUTHENTICATION);
    assertEquals(userId, MyDelegationService.CURRENT_AUTHENTICATION.getUserId());

    assertEquals(Long.valueOf(5), MyDelegationService.INSTANCES_COUNT);
  }

  @Deployment
  public void testSignallableActivityBehaviorAsDelegateExpression() {
    // given
    processEngineConfiguration.getBeans().put("activityBehavior", new MyServiceTaskActivityBehaviorExecuteQuery());

    startProcessInstancesByKey(DEFAULT_PROCESS_KEY, 4);
    String processInstanceId = startProcessInstanceByKey(DEFAULT_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    // when
    runtimeService.signal(processInstanceId);

    // then
    assertNotNull(MyDelegationService.CURRENT_AUTHENTICATION);
    assertEquals(userId, MyDelegationService.CURRENT_AUTHENTICATION.getUserId());

    assertEquals(Long.valueOf(5), MyDelegationService.INSTANCES_COUNT);
  }

  @Deployment
  public void testExecutionListenerExecutesQueryAfterUserCompletesTask() {
    // given
    startProcessInstancesByKey(DEFAULT_PROCESS_KEY, 5);
    String taskId = selectAnyTask().getId();
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    taskService.complete(taskId);

    // then
    assertNotNull(MyDelegationService.CURRENT_AUTHENTICATION);
    assertEquals(userId, MyDelegationService.CURRENT_AUTHENTICATION.getUserId());

    assertEquals(Long.valueOf(5), MyDelegationService.INSTANCES_COUNT);
  }

  @Deployment
  public void testExecutionListenerExecutesCommandAfterUserCompletesTask() {
    // given
    startProcessInstanceByKey(DEFAULT_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    taskService.complete(taskId);

    // then
    assertNotNull(MyDelegationService.CURRENT_AUTHENTICATION);
    assertEquals(userId, MyDelegationService.CURRENT_AUTHENTICATION.getUserId());

    disableAuthorization();
    assertEquals(2, runtimeService.createProcessInstanceQuery().count());
    enableAuthorization();
  }

  @Deployment
  public void testExecutionListenerExecutesQueryAfterUserCompletesTaskAsDelegateExpression() {
    // given
    processEngineConfiguration.getBeans().put("myListener", new ExecuteQueryListener());

    startProcessInstancesByKey(DEFAULT_PROCESS_KEY, 5);
    String taskId = selectAnyTask().getId();
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    taskService.complete(taskId);

    // then
    assertNotNull(MyDelegationService.CURRENT_AUTHENTICATION);
    assertEquals(userId, MyDelegationService.CURRENT_AUTHENTICATION.getUserId());

    assertEquals(Long.valueOf(5), MyDelegationService.INSTANCES_COUNT);
  }

  @Deployment
  public void testExecutionListenerExecutesCommandAfterUserCompletesTaskAsDelegateExpression() {
    // given
    processEngineConfiguration.getBeans().put("myListener", new ExecuteCommandListener());

    startProcessInstanceByKey(DEFAULT_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    taskService.complete(taskId);

    // then
    assertNotNull(MyDelegationService.CURRENT_AUTHENTICATION);
    assertEquals(userId, MyDelegationService.CURRENT_AUTHENTICATION.getUserId());

    disableAuthorization();
    assertEquals(2, runtimeService.createProcessInstanceQuery().count());
    enableAuthorization();
  }

  @Deployment
  public void testExecutionListenerExecutesQueryAfterUserCompletesTaskAsExpression() {
    // given
    processEngineConfiguration.getBeans().put("myListener", new ExecuteQueryListener());

    startProcessInstancesByKey(DEFAULT_PROCESS_KEY, 5);
    String taskId = selectAnyTask().getId();
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    taskService.complete(taskId);

    // then
    assertNotNull(MyDelegationService.CURRENT_AUTHENTICATION);
    assertEquals(userId, MyDelegationService.CURRENT_AUTHENTICATION.getUserId());

    assertEquals(Long.valueOf(5), MyDelegationService.INSTANCES_COUNT);
  }

  @Deployment
  public void testExecutionListenerExecutesCommandAfterUserCompletesTaskAsExpression() {
    // given
    processEngineConfiguration.getBeans().put("myListener", new ExecuteCommandListener());

    startProcessInstanceByKey(DEFAULT_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    taskService.complete(taskId);

    // then
    assertNotNull(MyDelegationService.CURRENT_AUTHENTICATION);
    assertEquals(userId, MyDelegationService.CURRENT_AUTHENTICATION.getUserId());

    disableAuthorization();
    assertEquals(2, runtimeService.createProcessInstanceQuery().count());
    enableAuthorization();
  }

  @Deployment
  public void testTaskListenerExecutesQueryAfterUserCompletesTask() {
    // given
    startProcessInstancesByKey(DEFAULT_PROCESS_KEY, 5);
    String taskId = selectAnyTask().getId();
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    taskService.complete(taskId);

    // then
    assertNotNull(MyDelegationService.CURRENT_AUTHENTICATION);
    assertEquals(userId, MyDelegationService.CURRENT_AUTHENTICATION.getUserId());

    assertEquals(Long.valueOf(5), MyDelegationService.INSTANCES_COUNT);
  }

  @Deployment
  public void testTaskListenerExecutesCommandAfterUserCompletesTask() {
    // given
    startProcessInstanceByKey(DEFAULT_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    taskService.complete(taskId);

    // then
    assertNotNull(MyDelegationService.CURRENT_AUTHENTICATION);
    assertEquals(userId, MyDelegationService.CURRENT_AUTHENTICATION.getUserId());

    disableAuthorization();
    assertEquals(2, runtimeService.createProcessInstanceQuery().count());
    enableAuthorization();
  }

  @Deployment
  public void testTaskListenerExecutesQueryAfterUserCompletesTaskAsDelegateExpression() {
    // given
    processEngineConfiguration.getBeans().put("myListener", new ExecuteQueryTaskListener());

    startProcessInstancesByKey(DEFAULT_PROCESS_KEY, 5);
    String taskId = selectAnyTask().getId();
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    taskService.complete(taskId);

    // then
    assertNotNull(MyDelegationService.CURRENT_AUTHENTICATION);
    assertEquals(userId, MyDelegationService.CURRENT_AUTHENTICATION.getUserId());

    assertEquals(Long.valueOf(5), MyDelegationService.INSTANCES_COUNT);
  }

  @Deployment
  public void testTaskListenerExecutesCommandAfterUserCompletesTaskAsDelegateExpression() {
    // given
    processEngineConfiguration.getBeans().put("myListener", new ExecuteCommandTaskListener());

    startProcessInstanceByKey(DEFAULT_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    taskService.complete(taskId);

    // then
    assertNotNull(MyDelegationService.CURRENT_AUTHENTICATION);
    assertEquals(userId, MyDelegationService.CURRENT_AUTHENTICATION.getUserId());

    disableAuthorization();
    assertEquals(2, runtimeService.createProcessInstanceQuery().count());
    enableAuthorization();
  }

  @Deployment
  public void testTaskListenerExecutesQueryAfterUserCompletesTaskAsExpression() {
    // given
    processEngineConfiguration.getBeans().put("myListener", new ExecuteQueryTaskListener());

    startProcessInstancesByKey(DEFAULT_PROCESS_KEY, 5);
    String taskId = selectAnyTask().getId();
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    taskService.complete(taskId);

    // then
    assertNotNull(MyDelegationService.CURRENT_AUTHENTICATION);
    assertEquals(userId, MyDelegationService.CURRENT_AUTHENTICATION.getUserId());

    assertEquals(Long.valueOf(5), MyDelegationService.INSTANCES_COUNT);
  }

  @Deployment
  public void testTaskListenerExecutesCommandAfterUserCompletesTaskAsExpression() {
    // given
    processEngineConfiguration.getBeans().put("myListener", new ExecuteCommandTaskListener());

    startProcessInstanceByKey(DEFAULT_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    taskService.complete(taskId);

    // then
    assertNotNull(MyDelegationService.CURRENT_AUTHENTICATION);
    assertEquals(userId, MyDelegationService.CURRENT_AUTHENTICATION.getUserId());

    disableAuthorization();
    assertEquals(2, runtimeService.createProcessInstanceQuery().count());
    enableAuthorization();
  }

  @Deployment
  public void testTaskAssigneeExpression() {
    // given
    processEngineConfiguration.getBeans().put("myTaskService", new MyTaskService());

    startProcessInstancesByKey(DEFAULT_PROCESS_KEY, 5);
    String taskId = selectAnyTask().getId();
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    taskService.complete(taskId);

    // then
    assertNotNull(MyDelegationService.CURRENT_AUTHENTICATION);
    assertEquals(userId, MyDelegationService.CURRENT_AUTHENTICATION.getUserId());

    assertEquals(Long.valueOf(5), MyDelegationService.INSTANCES_COUNT);
  }

  @Deployment
  public void testScriptTaskExecutesQueryAfterUserCompletesTask() {
    // given
    startProcessInstancesByKey(DEFAULT_PROCESS_KEY, 5);
    Task task = selectAnyTask();

    String taskId = task.getId();
    String processInstanceId = task.getProcessInstanceId();

    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    taskService.complete(taskId);

    // then
    disableAuthorization();

    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .processInstanceIdIn(processInstanceId);

    VariableInstance variableUser = query
        .variableName("userId")
        .singleResult();
    assertNotNull(variableUser);
    assertEquals(userId, variableUser.getValue());

    VariableInstance variableCount = query
        .variableName("count")
        .singleResult();
    assertNotNull(variableCount);
    assertEquals(5l, variableCount.getValue());

    enableAuthorization();
  }

  @Deployment
  public void testScriptTaskExecutesCommandAfterUserCompletesTask() {
    // given
    String processInstanceId = startProcessInstanceByKey(DEFAULT_PROCESS_KEY).getId();
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    taskService.complete(taskId);

    // then
    disableAuthorization();

    VariableInstance variableUser = runtimeService
        .createVariableInstanceQuery()
        .processInstanceIdIn(processInstanceId)
        .variableName("userId")
        .singleResult();
    assertNotNull(variableUser);
    assertEquals(userId, variableUser.getValue());

    assertEquals(2, runtimeService.createProcessInstanceQuery().count());

    enableAuthorization();
  }

  @Deployment
  public void testScriptExecutionListenerExecutesQueryAfterUserCompletesTask() {
    // given
    startProcessInstancesByKey(DEFAULT_PROCESS_KEY, 5);
    Task task = selectAnyTask();

    String taskId = task.getId();
    String processInstanceId = task.getProcessInstanceId();

    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    taskService.complete(taskId);

    // then
    disableAuthorization();

    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .processInstanceIdIn(processInstanceId);

    VariableInstance variableUser = query
        .variableName("userId")
        .singleResult();
    assertNotNull(variableUser);
    assertEquals(userId, variableUser.getValue());

    VariableInstance variableCount = query
        .variableName("count")
        .singleResult();
    assertNotNull(variableCount);
    assertEquals(5l, variableCount.getValue());

    enableAuthorization();
  }

  @Deployment
  public void testScriptExecutionListenerExecutesCommandAfterUserCompletesTask() {
    // given
    String processInstanceId = startProcessInstanceByKey(DEFAULT_PROCESS_KEY).getId();
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    taskService.complete(taskId);

    // then
    disableAuthorization();

    VariableInstance variableUser = runtimeService
        .createVariableInstanceQuery()
        .processInstanceIdIn(processInstanceId)
        .variableName("userId")
        .singleResult();
    assertNotNull(variableUser);
    assertEquals(userId, variableUser.getValue());

    assertEquals(2, runtimeService.createProcessInstanceQuery().count());

    enableAuthorization();
  }

  @Deployment
  public void testScriptTaskListenerExecutesQueryAfterUserCompletesTask() {
    // given
    startProcessInstancesByKey(DEFAULT_PROCESS_KEY, 5);
    Task task = selectAnyTask();

    String taskId = task.getId();
    String processInstanceId = task.getProcessInstanceId();

    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    taskService.complete(taskId);

    // then
    disableAuthorization();

    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .processInstanceIdIn(processInstanceId);

    VariableInstance variableUser = query
        .variableName("userId")
        .singleResult();
    assertNotNull(variableUser);
    assertEquals(userId, variableUser.getValue());

    VariableInstance variableCount = query
        .variableName("count")
        .singleResult();
    assertNotNull(variableCount);
    assertEquals(5l, variableCount.getValue());

    enableAuthorization();
  }

  @Deployment
  public void testScriptTaskListenerExecutesCommandAfterUserCompletesTask() {
    // given
    String processInstanceId = startProcessInstanceByKey(DEFAULT_PROCESS_KEY).getId();
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    taskService.complete(taskId);

    // then
    disableAuthorization();

    VariableInstance variableUser = runtimeService
        .createVariableInstanceQuery()
        .processInstanceIdIn(processInstanceId)
        .variableName("userId")
        .singleResult();
    assertNotNull(variableUser);
    assertEquals(userId, variableUser.getValue());

    assertEquals(2, runtimeService.createProcessInstanceQuery().count());

    enableAuthorization();
  }

  @Deployment
  public void testScriptConditionExecutesQueryAfterUserCompletesTask() {
    // given
    startProcessInstancesByKey(DEFAULT_PROCESS_KEY, 5);
    Task task = selectAnyTask();

    String taskId = task.getId();
    String processInstanceId = task.getProcessInstanceId();

    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    taskService.complete(taskId);

    // then
    disableAuthorization();

    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .processInstanceIdIn(processInstanceId);

    VariableInstance variableUser = query
        .variableName("userId")
        .singleResult();
    assertNotNull(variableUser);
    assertEquals(userId, variableUser.getValue());

    VariableInstance variableCount = query
        .variableName("count")
        .singleResult();
    assertNotNull(variableCount);
    assertEquals(5l, variableCount.getValue());

    enableAuthorization();
  }

  @Deployment
  public void testScriptConditionExecutesCommandAfterUserCompletesTask() {
    // given
    String processInstanceId = startProcessInstanceByKey(DEFAULT_PROCESS_KEY).getId();
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    taskService.complete(taskId);

    // then
    disableAuthorization();

    VariableInstance variableUser = runtimeService
        .createVariableInstanceQuery()
        .processInstanceIdIn(processInstanceId)
        .variableName("userId")
        .singleResult();
    assertNotNull(variableUser);
    assertEquals(userId, variableUser.getValue());

    assertEquals(2, runtimeService.createProcessInstanceQuery().count());

    enableAuthorization();
  }

  @Deployment
  public void testScriptIoMappingExecutesQueryAfterUserCompletesTask() {
    // given
    startProcessInstancesByKey(DEFAULT_PROCESS_KEY, 5);
    Task task = selectAnyTask();

    String taskId = task.getId();
    String processInstanceId = task.getProcessInstanceId();

    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    taskService.complete(taskId);

    // then
    disableAuthorization();

    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .processInstanceIdIn(processInstanceId);

    VariableInstance variableUser = query
        .variableName("userId")
        .singleResult();
    assertNotNull(variableUser);
    assertEquals(userId, variableUser.getValue());

    VariableInstance variableCount = query
        .variableName("count")
        .singleResult();
    assertNotNull(variableCount);
    assertEquals(5l, variableCount.getValue());

    enableAuthorization();
  }

  @Deployment
  public void testScriptIoMappingExecutesCommandAfterUserCompletesTask() {
    // given
    String processInstanceId = startProcessInstanceByKey(DEFAULT_PROCESS_KEY).getId();
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    taskService.complete(taskId);

    // then
    disableAuthorization();

    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .processInstanceIdIn(processInstanceId);

    VariableInstance variableUser = query
        .variableName("userId")
        .singleResult();
    assertNotNull(variableUser);
    assertEquals(userId, variableUser.getValue());

    VariableInstance variableCount = query
        .variableName("count")
        .singleResult();
    assertNotNull(variableCount);
    assertEquals(1l, variableCount.getValue());

    assertEquals(2, runtimeService.createProcessInstanceQuery().count());

    enableAuthorization();
  }

  @Deployment
  public void testCustomStartFormHandlerExecutesQuery() {
    // given
    startProcessInstancesByKey(DEFAULT_PROCESS_KEY, 5);

    String processDefinitionId = selectProcessDefinitionByKey(DEFAULT_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, DEFAULT_PROCESS_KEY, userId, READ);

    // when
    StartFormData startFormData = formService.getStartFormData(processDefinitionId);

    // then
    assertNotNull(startFormData);

    assertNotNull(MyDelegationService.CURRENT_AUTHENTICATION);
    assertEquals(userId, MyDelegationService.CURRENT_AUTHENTICATION.getUserId());

    assertEquals(Long.valueOf(5), MyDelegationService.INSTANCES_COUNT);
  }

  @Deployment
  public void testCustomTaskFormHandlerExecutesQuery() {
    // given
    startProcessInstancesByKey(DEFAULT_PROCESS_KEY, 5);

    String taskId = selectAnyTask().getId();
    createGrantAuthorization(TASK, taskId, userId, READ);

    // when
    TaskFormData taskFormData = formService.getTaskFormData(taskId);

    // then
    assertNotNull(taskFormData);

    assertNotNull(MyDelegationService.CURRENT_AUTHENTICATION);
    assertEquals(userId, MyDelegationService.CURRENT_AUTHENTICATION.getUserId());

    assertEquals(Long.valueOf(5), MyDelegationService.INSTANCES_COUNT);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/authorization/DelegationAuthorizationTest.testCustomStartFormHandlerExecutesQuery.bpmn20.xml"})
  public void testSubmitCustomStartFormHandlerExecutesQuery() {
    // given
    startProcessInstancesByKey(DEFAULT_PROCESS_KEY, 5);

    String processDefinitionId = selectProcessDefinitionByKey(DEFAULT_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, DEFAULT_PROCESS_KEY, userId, CREATE_INSTANCE);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, CREATE);

    // when
    formService.submitStartForm(processDefinitionId, null);

    // then
    assertNotNull(MyDelegationService.CURRENT_AUTHENTICATION);
    assertEquals(userId, MyDelegationService.CURRENT_AUTHENTICATION.getUserId());

    assertEquals(Long.valueOf(5), MyDelegationService.INSTANCES_COUNT);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/authorization/DelegationAuthorizationTest.testCustomTaskFormHandlerExecutesQuery.bpmn20.xml"})
  public void testSubmitCustomTaskFormHandlerExecutesQuery() {
    // given
    startProcessInstancesByKey(DEFAULT_PROCESS_KEY, 5);

    String taskId = selectAnyTask().getId();
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    formService.submitTaskForm(taskId, null);

    // then
    assertNotNull(MyDelegationService.CURRENT_AUTHENTICATION);
    assertEquals(userId, MyDelegationService.CURRENT_AUTHENTICATION.getUserId());

    assertEquals(Long.valueOf(5), MyDelegationService.INSTANCES_COUNT);
  }

  @Deployment
  public void testCustomFormFieldValidator() {
    // given
    startProcessInstancesByKey(DEFAULT_PROCESS_KEY, 5);

    String taskId = selectAnyTask().getId();
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    formService.submitTaskForm(taskId, null);

    // then
    assertNotNull(MyDelegationService.CURRENT_AUTHENTICATION);
    assertEquals(userId, MyDelegationService.CURRENT_AUTHENTICATION.getUserId());

    assertEquals(Long.valueOf(5), MyDelegationService.INSTANCES_COUNT);
  }

  @Deployment
  public void testCustomFormFieldValidatorAsDelegateExpression() {
    // given
    processEngineConfiguration.getBeans().put("myValidator", new MyFormFieldValidator());

    startProcessInstancesByKey(DEFAULT_PROCESS_KEY, 5);

    String taskId = selectAnyTask().getId();
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    formService.submitTaskForm(taskId, null);

    // then
    assertNotNull(MyDelegationService.CURRENT_AUTHENTICATION);
    assertEquals(userId, MyDelegationService.CURRENT_AUTHENTICATION.getUserId());

    assertEquals(Long.valueOf(5), MyDelegationService.INSTANCES_COUNT);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/authorization/DelegationAuthorizationTest.testJavaDelegateExecutesQueryAfterUserCompletesTask.bpmn20.xml"})
  public void testPerformAuthorizationCheckByExecutingQuery() {
    // given
    processEngineConfiguration.setAuthorizationEnabledForCustomCode(true);

    startProcessInstancesByKey(DEFAULT_PROCESS_KEY, 5);
    String taskId = selectAnyTask().getId();
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    taskService.complete(taskId);

    // then
    assertNotNull(MyDelegationService.CURRENT_AUTHENTICATION);
    assertEquals(userId, MyDelegationService.CURRENT_AUTHENTICATION.getUserId());

    assertEquals(Long.valueOf(0), MyDelegationService.INSTANCES_COUNT);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/authorization/DelegationAuthorizationTest.testJavaDelegateExecutesCommandAfterUserCompletesTask.bpmn20.xml"})
  public void testPerformAuthorizationCheckByExecutingCommand() {
    // given
    processEngineConfiguration.setAuthorizationEnabledForCustomCode(true);

    startProcessInstanceByKey(DEFAULT_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    try {
      // when
      taskService.complete(taskId);
      fail("Exception expected: It should not be possible to execute the command inside JavaDelegate");
    } catch (AuthorizationException e) {
    }

    // then
    assertNotNull(MyDelegationService.CURRENT_AUTHENTICATION);
    assertEquals(userId, MyDelegationService.CURRENT_AUTHENTICATION.getUserId());

    disableAuthorization();
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());
    enableAuthorization();
  }

  @Deployment
  public void testTaskListenerOnCreateAssignsTask() {
    // given
    String processInstanceId = startProcessInstanceByKey(DEFAULT_PROCESS_KEY).getId();
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when (1)
    taskService.complete(taskId);

    // then (1)
    identityService.clearAuthentication();
    identityService.setAuthentication("demo", null);

    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    // when (2)
    taskService.complete(task.getId());

    // then (2)
    assertProcessEnded(processInstanceId);
  }

  // helper /////////////////////////////////////////////////////////////////////////

  protected void startProcessInstancesByKey(String key, int count) {
    for (int i = 0; i < count; i++) {
      startProcessInstanceByKey(key);
    }
  }

  protected Task selectAnyTask() {
    disableAuthorization();
    Task task = taskService.createTaskQuery().listPage(0, 1).get(0);
    enableAuthorization();
    return task;
  }

}
