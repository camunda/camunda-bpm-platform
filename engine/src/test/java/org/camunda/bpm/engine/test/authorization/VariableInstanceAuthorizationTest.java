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

import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.READ_INSTANCE;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_INSTANCE;
import static org.camunda.bpm.engine.authorization.Resources.TASK;

import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.runtime.VariableInstanceQuery;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;

/**
 * @author Roman Smirnov
 *
 */
public class VariableInstanceAuthorizationTest extends AuthorizationTest {

  protected static final String PROCESS_KEY = "oneTaskProcess";
  protected static final String CASE_KEY = "oneTaskCase";
  protected static final String VARIABLE_NAME = "aVariableName";
  protected static final String VARIABLE_VALUE = "aVariableValue";

  protected String deploymentId;

  public void setUp() {
    deploymentId = repositoryService
      .createDeployment()
      .addClasspathResource("org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
      .addClasspathResource("org/camunda/bpm/engine/test/authorization/oneTaskCase.cmmn")
      .deploy()
      .getId();
  }

  public void tearDown() {
    super.tearDown();
    repositoryService.deleteDeployment(deploymentId, true);
  }

  public void testProcessVariableQueryWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY, getVariables());

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    // then
    verifyQueryResults(query, 0);
  }

  public void testCaseVariableQueryWithoutAuthorization () {
    // given
    createCaseInstanceByKey(CASE_KEY, getVariables());

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    // then
    verifyQueryResults(query, 1);
  }

  public void testProcessLocalTaskVariableQueryWithoutAuthorization () {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    setTaskVariableLocal(taskId, VARIABLE_NAME, VARIABLE_VALUE);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    // then
    verifyQueryResults(query, 0);
  }

  public void testCaseLocalTaskVariableQueryWithoutAuthorization () {
    // given
    createCaseInstanceByKey(CASE_KEY);
    String taskId = selectSingleTask().getId();
    setTaskVariableLocal(taskId, VARIABLE_NAME, VARIABLE_VALUE);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    // then
    verifyQueryResults(query, 1);
  }

  public void testStandaloneTaskVariableQueryWithoutAuthorization() {
    // given
    String taskId = "myTask";
    createTask(taskId);
    setTaskVariable(taskId, VARIABLE_NAME, VARIABLE_VALUE);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    // then
    verifyQueryResults(query, 0);

    deleteTask(taskId, true);
  }

  public void testProcessVariableQueryWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();

    Authorization authorization = createGrantAuthorization(PROCESS_INSTANCE, processInstanceId);
    authorization.setUserId(userId);
    authorization.addPermission(READ);
    saveAuthorization(authorization);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    // then
    verifyQueryResults(query, 1);

    VariableInstance variable = query.singleResult();
    assertNotNull(variable);
    assertEquals(processInstanceId, variable.getProcessInstanceId());
  }

  public void testProcessVariableQueryWithReadInstancesPermissionOnOneTaskProcess() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();

    Authorization authorization = createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY);
    authorization.setUserId(userId);
    authorization.addPermission(READ_INSTANCE);
    saveAuthorization(authorization);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    // then
    verifyQueryResults(query, 1);

    VariableInstance variable = query.singleResult();
    assertNotNull(variable);
    assertEquals(processInstanceId, variable.getProcessInstanceId());
  }

  public void testProcessLocalTaskVariableQueryWithReadPermissionOnTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    setTaskVariableLocal(taskId, VARIABLE_NAME, VARIABLE_VALUE);

    Authorization authorization = createGrantAuthorization(TASK, taskId);
    authorization.setUserId(userId);
    authorization.addPermission(READ);
    saveAuthorization(authorization);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    // then
    verifyQueryResults(query, 0);
  }

  public void testProcessLocalTaskVariableQueryWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    String taskId = selectSingleTask().getId();
    setTaskVariableLocal(taskId, VARIABLE_NAME, VARIABLE_VALUE);

    Authorization authorization = createGrantAuthorization(PROCESS_INSTANCE, processInstanceId);
    authorization.setUserId(userId);
    authorization.addPermission(READ);
    saveAuthorization(authorization);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    // then
    verifyQueryResults(query, 1);

    VariableInstance variable = query.singleResult();
    assertNotNull(variable);
    assertEquals(processInstanceId, variable.getProcessInstanceId());
  }

  public void testProcessLocalTaskVariableQueryWithReadPermissionOnOneProcessTask() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    String taskId = selectSingleTask().getId();
    setTaskVariableLocal(taskId, VARIABLE_NAME, VARIABLE_VALUE);

    Authorization authorization = createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY);
    authorization.setUserId(userId);
    authorization.addPermission(READ_INSTANCE);
    saveAuthorization(authorization);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    // then
    verifyQueryResults(query, 1);

    VariableInstance variable = query.singleResult();
    assertNotNull(variable);
    assertEquals(processInstanceId, variable.getProcessInstanceId());
  }

  public void testStandaloneTaskVariableQueryWithReadPermissionOnTask() {
    // given
    String taskId = "myTask";
    createTask(taskId);
    setTaskVariable(taskId, VARIABLE_NAME, VARIABLE_VALUE);

    Authorization authorization = createGrantAuthorization(TASK, taskId);
    authorization.setUserId(userId);
    authorization.addPermission(READ);
    saveAuthorization(authorization);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    // then
    verifyQueryResults(query, 1);

    deleteTask(taskId, true);
  }

  protected VariableMap getVariables() {
    return Variables.createVariables().putValue(VARIABLE_NAME, VARIABLE_VALUE);
  }

  protected void verifyQueryResults(VariableInstanceQuery query, int countExpected) {
    verifyQueryResults((AbstractQuery<?, ?>) query, countExpected);
  }

}
