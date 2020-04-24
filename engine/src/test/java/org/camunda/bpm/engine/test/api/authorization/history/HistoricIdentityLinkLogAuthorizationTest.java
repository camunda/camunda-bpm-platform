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
package org.camunda.bpm.engine.test.api.authorization.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.READ_HISTORY;
import static org.camunda.bpm.engine.authorization.Resources.HISTORIC_PROCESS_INSTANCE;
import static org.camunda.bpm.engine.authorization.Resources.HISTORIC_TASK;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.authorization.HistoricProcessInstancePermissions;
import org.camunda.bpm.engine.authorization.HistoricTaskPermissions;
import org.camunda.bpm.engine.authorization.ProcessDefinitionPermissions;
import org.camunda.bpm.engine.history.HistoricIdentityLinkLog;
import org.camunda.bpm.engine.history.HistoricIdentityLinkLogQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.authorization.AuthorizationTest;

import java.util.List;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoricIdentityLinkLogAuthorizationTest extends AuthorizationTest {

  protected static final String ONE_PROCESS_KEY = "demoAssigneeProcess";
  protected static final String CASE_KEY = "oneTaskCase";
  protected String deploymentId;

  @Override
  public void setUp() throws Exception {
    deploymentId = createDeployment(null, "org/camunda/bpm/engine/test/api/authorization/oneTaskProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/api/authorization/oneTaskCase.cmmn").getId();
    super.setUp();
  }

  @Override
  public void tearDown() {
    super.tearDown();
    deleteDeployment(deploymentId);
    processEngineConfiguration.setEnableHistoricInstancePermissions(false);
  }

  // historic identity link query (standalone task) - Authorization

  public void testQueryForStandaloneTaskHistoricIdentityLinkWithoutAuthrorization() {
    // given
    disableAuthorization();

    Task taskAssignee = taskService.newTask("newTask");
    taskAssignee.setAssignee("aUserId");
    taskService.saveTask(taskAssignee);

    enableAuthorization();

    // when
    HistoricIdentityLinkLogQuery query = historyService.createHistoricIdentityLinkLogQuery();

    // then
    verifyQueryResults(query, 1);

    disableAuthorization();
    taskService.deleteTask("newTask", true);
    enableAuthorization();
  }

  public void testQueryForTaskHistoricIdentityLinkWithoutUserPermission() {
    // given
    disableAuthorization();
    startProcessInstanceByKey(ONE_PROCESS_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // if
    identityService.setAuthenticatedUserId("aAssignerId");
    taskService.addCandidateUser(taskId, "aUserId");

    enableAuthorization();

    // when
    HistoricIdentityLinkLogQuery query = historyService.createHistoricIdentityLinkLogQuery();

    // then
    verifyQueryResults(query, 0);
  }

  public void testQueryForTaskHistoricIdentityLinkWithUserPermission() {
    // given
    disableAuthorization();
    startProcessInstanceByKey(ONE_PROCESS_KEY);

    // if
    createGrantAuthorization(PROCESS_DEFINITION, ONE_PROCESS_KEY, userId, READ_HISTORY);

    enableAuthorization();
    // when
    HistoricIdentityLinkLogQuery query = historyService.createHistoricIdentityLinkLogQuery();

    // then
    verifyQueryResults(query, 1);
  }

  public void testQueryWithMultiple() {
    // given
    disableAuthorization();
    startProcessInstanceByKey(ONE_PROCESS_KEY);

    // if
    createGrantAuthorization(PROCESS_DEFINITION, ONE_PROCESS_KEY, userId, READ_HISTORY);
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    enableAuthorization();
    // when
    HistoricIdentityLinkLogQuery query = historyService.createHistoricIdentityLinkLogQuery();

    // then
    verifyQueryResults(query, 1);
  }

  public void testQueryCaseTask() {
    // given
    createCaseInstanceByKey(CASE_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // if
    identityService.setAuthenticatedUserId("aAssignerId");
    taskService.addCandidateUser(taskId, "aUserId");
    enableAuthorization();

    // when
    HistoricIdentityLinkLogQuery query = historyService.createHistoricIdentityLinkLogQuery();

    // then
    verifyQueryResults(query, 1);
  }

  public void testMixedQuery() {

    disableAuthorization();
    // given
    startProcessInstanceByKey(ONE_PROCESS_KEY);
    startProcessInstanceByKey(ONE_PROCESS_KEY);
    startProcessInstanceByKey(ONE_PROCESS_KEY);

    createCaseInstanceByKey(CASE_KEY);
    taskService.addCandidateUser(taskService.createTaskQuery().list().get(3).getId(), "dUserId");
    createCaseInstanceByKey(CASE_KEY);
    taskService.addCandidateUser(taskService.createTaskQuery().list().get(4).getId(), "eUserId");

    createTaskAndAssignUser("one");
    createTaskAndAssignUser("two");
    createTaskAndAssignUser("three");
    createTaskAndAssignUser("four");
    createTaskAndAssignUser("five");

    enableAuthorization();

    // when
    HistoricIdentityLinkLogQuery query = historyService.createHistoricIdentityLinkLogQuery();

    // then
    verifyQueryResults(query, 7);

    disableAuthorization();

    query = historyService.createHistoricIdentityLinkLogQuery();
    // then
    verifyQueryResults(query, 10);

    // if
    createGrantAuthorization(PROCESS_DEFINITION, ONE_PROCESS_KEY, userId, READ_HISTORY);
    enableAuthorization();
    query = historyService.createHistoricIdentityLinkLogQuery();

    // then
    verifyQueryResults(query, 10);

    deleteTask("one", true);
    deleteTask("two", true);
    deleteTask("three", true);
    deleteTask("four", true);
    deleteTask("five", true);
  }

  public void testCheckNonePermissionOnHistoricTask() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    startProcessInstanceByKey(ONE_PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(HISTORIC_TASK, taskId, userId, HistoricTaskPermissions.NONE);

    // when
    List<HistoricIdentityLinkLog> result = historyService.createHistoricIdentityLinkLogQuery()
        .list();

    // then
    assertEquals(0, result.size());
  }

  public void testCheckReadPermissionOnHistoricTask() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    startProcessInstanceByKey(ONE_PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(HISTORIC_TASK, taskId, userId, HistoricTaskPermissions.READ);

    // when
    List<HistoricIdentityLinkLog> result = historyService.createHistoricIdentityLinkLogQuery()
        .list();

    // then
    assertEquals(1, result.size());
  }

  public void testCheckReadPermissionOnStandaloneHistoricTask() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String taskId = "aTaskId";
    createTask(taskId);

    disableAuthorization();
    taskService.setAssignee(taskId, userId);
    enableAuthorization();

    createGrantAuthorization(HISTORIC_TASK, taskId, userId, HistoricTaskPermissions.READ);

    // when
    List<HistoricIdentityLinkLog> result = historyService.createHistoricIdentityLinkLogQuery()
        .list();

    // then
    assertEquals(1, result.size());

    // clear
    deleteTask(taskId, true);
  }

  public void testCheckNonePermissionOnStandaloneHistoricTask() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String taskId = "aTaskId";
    createTask(taskId);
    disableAuthorization();
    taskService.setAssignee(taskId, userId);
    enableAuthorization();

    createGrantAuthorization(HISTORIC_TASK, taskId, userId, HistoricTaskPermissions.NONE);

    // when
    List<HistoricIdentityLinkLog> result = historyService.createHistoricIdentityLinkLogQuery()
        .list();

    // then
    assertEquals(0, result.size());

    // clear
    deleteTask(taskId, true);
  }

  public void testCheckReadPermissionOnCompletedHistoricTask() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    startProcessInstanceByKey(ONE_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorization(HISTORIC_TASK, taskId, userId, HistoricTaskPermissions.READ);

    // when
    List<HistoricIdentityLinkLog> result = historyService.createHistoricIdentityLinkLogQuery()
        .list();

    // then
    assertEquals(1, result.size());
  }

  public void testCheckNonePermissionOnHistoricTaskAndReadHistoryPermissionOnProcessDefinition() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    startProcessInstanceByKey(ONE_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorization(HISTORIC_TASK, taskId, userId, HistoricTaskPermissions.NONE);
    createGrantAuthorization(PROCESS_DEFINITION, ONE_PROCESS_KEY, userId, READ_HISTORY);

    // when
    List<HistoricIdentityLinkLog> result = historyService.createHistoricIdentityLinkLogQuery()
        .list();

    // then
    assertEquals(1, result.size());
  }

  public void testCheckReadPermissionOnHistoricTaskAndNonePermissionOnProcessDefinition() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    startProcessInstanceByKey(ONE_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorization(HISTORIC_TASK, taskId, userId, HistoricTaskPermissions.READ);
    createGrantAuthorization(PROCESS_DEFINITION, ONE_PROCESS_KEY, userId,
        ProcessDefinitionPermissions.NONE);

    // when
    List<HistoricIdentityLinkLog> result = historyService.createHistoricIdentityLinkLogQuery()
        .list();

    // then
    assertEquals(1, result.size());
  }

  public void testHistoricTaskPermissionsAuthorizationDisabled() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    startProcessInstanceByKey(ONE_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    disableAuthorization();

    taskService.setVariable(taskId, "foo", "bar");

    // when
    List<HistoricIdentityLinkLog> result = historyService.createHistoricIdentityLinkLogQuery()
        .list();

    // then
    assertEquals(1, result.size());
  }

  public void testCheckNonePermissionOnHistoricProcessInstance() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String processInstanceId = startProcessInstanceByKey(ONE_PROCESS_KEY).getId();

    createGrantAuthorization(HISTORIC_PROCESS_INSTANCE, processInstanceId, userId,
        HistoricProcessInstancePermissions.NONE);

    // when
    HistoricIdentityLinkLogQuery query = historyService.createHistoricIdentityLinkLogQuery();

    // then
    assertThat(query.list()).isEmpty();
  }

  public void testCheckReadPermissionOnHistoricProcessInstance() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String processInstanceId = startProcessInstanceByKey(ONE_PROCESS_KEY).getId();

    createGrantAuthorization(HISTORIC_PROCESS_INSTANCE, processInstanceId, userId,
        HistoricProcessInstancePermissions.READ);

    // when
    HistoricIdentityLinkLogQuery query = historyService.createHistoricIdentityLinkLogQuery();

    // then
    assertThat(query.list())
        .extracting("rootProcessInstanceId")
        .containsExactly(processInstanceId);
  }

  public void testCheckReadPermissionOnCompletedHistoricProcessInstance() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String processInstanceId = startProcessInstanceByKey(ONE_PROCESS_KEY).getId();
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorization(HISTORIC_PROCESS_INSTANCE, processInstanceId, userId,
        HistoricProcessInstancePermissions.READ);

    // when
    HistoricIdentityLinkLogQuery query = historyService.createHistoricIdentityLinkLogQuery();

    // then
    assertThat(query.list())
        .extracting("rootProcessInstanceId")
        .containsExactly(processInstanceId);
  }

  public void testCheckNoneOnHistoricProcessInstanceAndReadHistoryPermissionOnProcessDefinition() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String processInstanceId = startProcessInstanceByKey(ONE_PROCESS_KEY).getId();
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorization(HISTORIC_PROCESS_INSTANCE, processInstanceId, userId,
        HistoricProcessInstancePermissions.NONE);
    createGrantAuthorization(PROCESS_DEFINITION, ONE_PROCESS_KEY, userId, READ_HISTORY);

    // when
    HistoricIdentityLinkLogQuery query = historyService.createHistoricIdentityLinkLogQuery();

    // then
    assertThat(query.list())
        .extracting("rootProcessInstanceId")
        .containsExactly(processInstanceId);
  }

  public void testCheckReadOnHistoricProcessInstanceAndNonePermissionOnProcessDefinition() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String processInstanceId = startProcessInstanceByKey(ONE_PROCESS_KEY).getId();
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorization(HISTORIC_PROCESS_INSTANCE, processInstanceId, userId,
        HistoricProcessInstancePermissions.READ);
    createGrantAuthorization(PROCESS_DEFINITION, ONE_PROCESS_KEY, userId,
        ProcessDefinitionPermissions.NONE);

    // when
    HistoricIdentityLinkLogQuery query = historyService.createHistoricIdentityLinkLogQuery();

    // then
    assertThat(query.list())
        .extracting("rootProcessInstanceId")
        .containsExactly(processInstanceId);
  }

  public void createTaskAndAssignUser(String taskId) {
    Task task = taskService.newTask(taskId);
    task.setAssignee("demo");
    taskService.saveTask(task);
  }

}
