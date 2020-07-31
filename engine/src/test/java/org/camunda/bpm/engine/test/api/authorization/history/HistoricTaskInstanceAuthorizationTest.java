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
import static org.camunda.bpm.engine.authorization.Permissions.ALL;
import static org.camunda.bpm.engine.authorization.Permissions.DELETE_HISTORY;
import static org.camunda.bpm.engine.authorization.Permissions.READ_HISTORY;
import static org.camunda.bpm.engine.authorization.Resources.HISTORIC_PROCESS_INSTANCE;
import static org.camunda.bpm.engine.authorization.Resources.HISTORIC_TASK;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.TASK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.HistoricProcessInstancePermissions;
import org.camunda.bpm.engine.authorization.HistoricTaskPermissions;
import org.camunda.bpm.engine.authorization.MissingAuthorization;
import org.camunda.bpm.engine.authorization.ProcessDefinitionPermissions;
import org.camunda.bpm.engine.authorization.TaskPermissions;
import org.camunda.bpm.engine.history.DurationReportResult;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstanceQuery;
import org.camunda.bpm.engine.history.HistoricTaskInstanceReportResult;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.query.PeriodUnit;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.authorization.AuthorizationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
public class HistoricTaskInstanceAuthorizationTest extends AuthorizationTest {

  protected static final String PROCESS_KEY = "oneTaskProcess";
  protected static final String MESSAGE_START_PROCESS_KEY = "messageStartProcess";
  protected static final String CASE_KEY = "oneTaskCase";

  protected String deploymentId;

  @Before
  public void setUp() throws Exception {
    deploymentId = testRule.deploy(
        "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/api/authorization/messageStartEventProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/api/authorization/oneTaskCase.cmmn").getId();
    super.setUp();

  }

  @After
  public void tearDown() {
    super.tearDown();
    processEngineConfiguration.setEnableHistoricInstancePermissions(false);
  }

  // historic task instance query (standalone task) ///////////////////////////////////////

  @Test
  public void testQueryAfterStandaloneTask() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    // when
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    // then
    verifyQueryResults(query, 1);

    deleteTask(taskId, true);
  }

  // historic task instance query (process task) //////////////////////////////////////////

  @Test
  public void testSimpleQueryWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);

    // when
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    // then
    verifyQueryResults(query, 0);
  }

  @Test
  public void testSimpleQueryWithReadHistoryPermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY);

    // when
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    // then
    verifyQueryResults(query, 1);
  }

  @Test
  public void testSimpleQueryWithReadHistoryPermissionOnAnyProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    // then
    verifyQueryResults(query, 1);
  }

  @Test
  public void testSimpleQueryWithMultiple() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY);

    // when
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    // then
    verifyQueryResults(query, 1);
  }

  // historic task instance query (multiple process instances) ////////////////////////

  @Test
  public void testQueryWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);

    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);

    // when
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    // then
    verifyQueryResults(query, 0);
  }

  @Test
  public void testQueryWithReadHistoryPermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);

    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY);

    // when
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    // then
    verifyQueryResults(query, 3);
  }

  @Test
  public void testQueryWithReadHistoryPermissionOnAnyProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);

    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    // then
    verifyQueryResults(query, 7);
  }

  // historic task instance query (case task) ///////////////////////////////////////

  @Test
  public void testQueryAfterCaseTask() {
    // given
    testRule.createCaseInstanceByKey(CASE_KEY);

    // when
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    // then
    verifyQueryResults(query, 1);
  }

  // historic task instance query (mixed tasks) ////////////////////////////////////

  @Test
  public void testMixedQueryWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);

    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);

    createTask("one");
    createTask("two");
    createTask("three");
    createTask("four");
    createTask("five");

    testRule.createCaseInstanceByKey(CASE_KEY);
    testRule.createCaseInstanceByKey(CASE_KEY);

    // when
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    // then
    verifyQueryResults(query, 7);

    deleteTask("one", true);
    deleteTask("two", true);
    deleteTask("three", true);
    deleteTask("four", true);
    deleteTask("five", true);
  }

  @Test
  public void testMixedQueryWithReadHistoryPermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);

    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);

    createTask("one");
    createTask("two");
    createTask("three");
    createTask("four");
    createTask("five");

    testRule.createCaseInstanceByKey(CASE_KEY);
    testRule.createCaseInstanceByKey(CASE_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY);

    // when
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    // then
    verifyQueryResults(query, 10);

    deleteTask("one", true);
    deleteTask("two", true);
    deleteTask("three", true);
    deleteTask("four", true);
    deleteTask("five", true);
  }

  @Test
  public void testMixedQueryWithReadHistoryPermissionOnAnyProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);

    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);

    createTask("one");
    createTask("two");
    createTask("three");
    createTask("four");
    createTask("five");

    testRule.createCaseInstanceByKey(CASE_KEY);
    testRule.createCaseInstanceByKey(CASE_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    // then
    verifyQueryResults(query, 14);

    deleteTask("one", true);
    deleteTask("two", true);
    deleteTask("three", true);
    deleteTask("four", true);
    deleteTask("five", true);
  }

  // delete deployment (cascade = false)

  @Test
  public void testQueryAfterDeletingDeployment() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY);

    disableAuthorization();
    List<Task> tasks = taskService.createTaskQuery().list();
    for (Task task : tasks) {
      taskService.complete(task.getId());
    }
    enableAuthorization();

    disableAuthorization();
    repositoryService.deleteDeployment(deploymentId);
    enableAuthorization();

    // when
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    // then
    verifyQueryResults(query, 3);

    disableAuthorization();
    List<HistoricProcessInstance> instances = historyService.createHistoricProcessInstanceQuery().list();
    for (HistoricProcessInstance instance : instances) {
      historyService.deleteHistoricProcessInstance(instance.getId());
    }
    enableAuthorization();
  }

  // delete historic task (standalone task) ///////////////////////

  @Test
  public void testDeleteStandaloneTask() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    // when
    historyService.deleteHistoricTaskInstance(taskId);

    // then
    disableAuthorization();
    HistoricTaskInstanceQuery query = historyService
        .createHistoricTaskInstanceQuery()
        .taskId(taskId);
    verifyQueryResults(query, 0);
    enableAuthorization();

    deleteTask(taskId, true);
  }

  // delete historic task (process task) ///////////////////////

  @Test
  public void testDeleteProcessTaskWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    try {
      // when
      historyService.deleteHistoricTaskInstance(taskId);
      fail("Exception expected: It should not be possible to delete the historic task instance");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(DELETE_HISTORY.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
  public void testDeleteProcessTaskWithDeleteHistoryPermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, DELETE_HISTORY);

    // when
    historyService.deleteHistoricTaskInstance(taskId);

    // then
    disableAuthorization();
    HistoricTaskInstanceQuery query = historyService
        .createHistoricTaskInstanceQuery()
        .taskId(taskId);
    verifyQueryResults(query, 0);
    enableAuthorization();
  }

  @Test
  public void testDeleteProcessTaskWithDeleteHistoryPermissionOnAnyProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, DELETE_HISTORY);

    // when
    historyService.deleteHistoricTaskInstance(taskId);

    // then
    disableAuthorization();
    HistoricTaskInstanceQuery query = historyService
        .createHistoricTaskInstanceQuery()
        .taskId(taskId);
    verifyQueryResults(query, 0);
    enableAuthorization();
  }

  @Test
  public void testDeleteHistoricTaskInstanceAfterDeletingDeployment() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, DELETE_HISTORY);

    disableAuthorization();
    repositoryService.deleteDeployment(deploymentId);
    enableAuthorization();

    // when
    historyService.deleteHistoricTaskInstance(taskId);

    // then
    disableAuthorization();
    HistoricTaskInstanceQuery query = historyService
        .createHistoricTaskInstanceQuery()
        .taskId(taskId);
    verifyQueryResults(query, 0);
    enableAuthorization();

    disableAuthorization();
    historyService.deleteHistoricProcessInstance(processInstanceId);
    enableAuthorization();
  }

  @Test
  public void testHistoricTaskInstanceDurationReportWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    try {
      // when
      historyService
              .createHistoricTaskInstanceReport()
              .duration(PeriodUnit.MONTH);
      fail("Exception expected: It should not be possible to create a historic task instance report");
    } catch (AuthorizationException e) {
      // then
      List<MissingAuthorization> missingAuthorizations = e.getMissingAuthorizations();
      assertEquals(1, missingAuthorizations.size());

      MissingAuthorization missingAuthorization = missingAuthorizations.get(0);
      assertEquals(READ_HISTORY.toString(), missingAuthorization.getViolatedPermissionName());
      assertEquals(PROCESS_DEFINITION.resourceName(), missingAuthorization.getResourceType());
      assertEquals(ANY, missingAuthorization.getResourceId());
    }
  }

  @Test
  public void testHistoricTaskInstanceReportWithHistoryReadPermissionOnAny() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    List<DurationReportResult> result = historyService
            .createHistoricTaskInstanceReport()
            .duration(PeriodUnit.MONTH);

    // then
    assertEquals(1, result.size());
  }

  @Test
  public void testHistoricTaskInstanceCountByProcessDefinitionReportWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    try {
      // when
      historyService
          .createHistoricTaskInstanceReport()
          .countByProcessDefinitionKey();
      fail("Exception expected: It should not be possible " +
          "to create a historic task instance report");
    } catch (AuthorizationException e) {
      // then
      List<MissingAuthorization> missingAuthorizations = e.getMissingAuthorizations();
      assertEquals(1, missingAuthorizations.size());

      MissingAuthorization missingAuthorization = missingAuthorizations.get(0);
      assertEquals(READ_HISTORY.toString(), missingAuthorization.getViolatedPermissionName());
      assertEquals(PROCESS_DEFINITION.resourceName(), missingAuthorization.getResourceType());
      assertEquals(ANY, missingAuthorization.getResourceId());
    }
  }

  @Test
  public void testHistoricTaskInstanceReportGroupedByProcessDefinitionKeyWithHistoryReadPermissionOnAny() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    List<HistoricTaskInstanceReportResult> result = historyService
            .createHistoricTaskInstanceReport()
            .countByProcessDefinitionKey();

    // then
    assertEquals(1, result.size());
  }

  @Test
  public void testHistoricTaskInstanceGroupedByTaskNameReportWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    try {
      // when
      historyService
          .createHistoricTaskInstanceReport()
          .countByTaskName();
      fail("Exception expected: It should not be possible " +
          "to create a historic task instance report");
    } catch (AuthorizationException e) {
      // then
      List<MissingAuthorization> missingAuthorizations = e.getMissingAuthorizations();
      assertEquals(1, missingAuthorizations.size());

      MissingAuthorization missingAuthorization = missingAuthorizations.get(0);
      assertEquals(READ_HISTORY.toString(), missingAuthorization.getViolatedPermissionName());
      assertEquals(PROCESS_DEFINITION.resourceName(), missingAuthorization.getResourceType());
      assertEquals(ANY, missingAuthorization.getResourceId());
    }
  }

  @Test
  public void testHistoricTaskInstanceGroupedByTaskNameReportWithHistoryReadPermissionOnAny() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    List<HistoricTaskInstanceReportResult> result = historyService
            .createHistoricTaskInstanceReport()
            .countByTaskName();

    // then
    assertEquals(1, result.size());
  }

  @Test
  public void testCheckAllHistoricTaskPermissions() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    // when
    createGrantAuthorization(HISTORIC_TASK, ANY, userId, HistoricTaskPermissions.ALL);

    // then
    assertTrue(authorizationService.isUserAuthorized(userId, null,
        HistoricTaskPermissions.NONE, HISTORIC_TASK));

    assertTrue(authorizationService.isUserAuthorized(userId, null,
        HistoricTaskPermissions.READ, HISTORIC_TASK));

    assertTrue(authorizationService.isUserAuthorized(userId, null,
        HistoricTaskPermissions.READ_VARIABLE, HISTORIC_TASK));

    assertTrue(authorizationService.isUserAuthorized(userId, null,
        HistoricTaskPermissions.ALL, HISTORIC_TASK));
  }

  @Test
  public void testCheckReadHistoricTaskPermissions() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    // when
    createGrantAuthorization(HISTORIC_TASK, ANY, userId, HistoricTaskPermissions.READ);

    // then
    assertTrue(authorizationService.isUserAuthorized(userId, null,
        HistoricTaskPermissions.NONE, HISTORIC_TASK));

    assertTrue(authorizationService.isUserAuthorized(userId, null,
        HistoricTaskPermissions.READ, HISTORIC_TASK));
  }

  @Test
  public void testCheckNoneHistoricTaskPermission() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    // when
    createGrantAuthorization(HISTORIC_TASK, ANY, userId, HistoricTaskPermissions.NONE);

    // then
    assertTrue(authorizationService.isUserAuthorized(userId, null,
        HistoricTaskPermissions.NONE, HISTORIC_TASK));
  }

  @Test
  public void testCheckNonePermissionOnHistoricTask() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(HISTORIC_TASK, taskId, userId, HistoricTaskPermissions.NONE);

    // when
    List<HistoricTaskInstance> result = historyService.createHistoricTaskInstanceQuery().list();

    // then
    assertEquals(0, result.size());
  }

  @Test
  public void testCheckReadPermissionOnHistoricTask() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(HISTORIC_TASK, taskId, userId, HistoricTaskPermissions.READ);

    // when
    List<HistoricTaskInstance> result = historyService.createHistoricTaskInstanceQuery().list();

    // then
    assertEquals(1, result.size());
  }

  @Test
  public void testCheckReadPermissionOnStandaloneHistoricTask() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String taskId = "aTaskId";
    createTask(taskId);

    createGrantAuthorization(HISTORIC_TASK, taskId, userId, HistoricTaskPermissions.READ);

    // when
    List<HistoricTaskInstance> result = historyService.createHistoricTaskInstanceQuery().list();

    // then
    assertEquals(1, result.size());

    // clear
    deleteTask(taskId, true);
  }

  @Test
  public void testCheckNonePermissionOnStandaloneHistoricTask() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String taskId = "aTaskId";
    createTask(taskId);

    createGrantAuthorization(HISTORIC_TASK, taskId, userId, HistoricTaskPermissions.NONE);

    // when
    List<HistoricTaskInstance> result = historyService.createHistoricTaskInstanceQuery().list();

    // then
    assertEquals(0, result.size());

    // clear
    deleteTask(taskId, true);
  }

  @Test
  public void testCheckReadPermissionOnCompletedHistoricTask() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorization(HISTORIC_TASK, taskId, userId, HistoricTaskPermissions.READ);

    // when
    List<HistoricTaskInstance> result = historyService.createHistoricTaskInstanceQuery().list();

    // then
    assertEquals(1, result.size());
  }

  @Test
  public void testCheckNonePermissionOndHistoricTaskAndReadHistoryPermissionOnProcessDefinition() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorization(HISTORIC_TASK, taskId, userId, HistoricTaskPermissions.NONE);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY);

    // when
    List<HistoricTaskInstance> result = historyService.createHistoricTaskInstanceQuery().list();

    // then
    assertEquals(1, result.size());
  }

  @Test
  public void testCheckReadPermissionOndHistoricTaskAndNonePermissionOnProcessDefinition() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorization(HISTORIC_TASK, taskId, userId, HistoricTaskPermissions.READ);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId,
        ProcessDefinitionPermissions.NONE);

    // when
    List<HistoricTaskInstance> result = historyService.createHistoricTaskInstanceQuery().list();

    // then
    assertEquals(1, result.size());
  }

  @Test
  public void testHistoricTaskPermissionsAuthorizationDisabled() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    startProcessInstanceByKey(PROCESS_KEY);

    disableAuthorization();

    // when
    List<HistoricTaskInstance> result = historyService.createHistoricTaskInstanceQuery().list();

    // then
    assertEquals(1, result.size());
  }

  @Test
  public void testHistoricTaskReadPermissionGrantedWhenAssign() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    disableAuthorization();
    createGrantAuthorization(TASK, taskId, userId, TaskPermissions.TASK_ASSIGN);
    enableAuthorization();

    taskService.setAssignee(taskId, userId);

    // when
    List<HistoricTaskInstance> result = historyService.createHistoricTaskInstanceQuery().list();

    // then
    assertEquals(1, result.size());
  }

  @Test
  public void testHistoricTaskReadPermissionGrantedWhenAddingIdentityLinkOnStandaloneTask() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String taskId = "aTaskId";
    createTask(taskId);

    disableAuthorization();
    createGrantAuthorization(TASK, taskId, userId, TaskPermissions.TASK_ASSIGN);
    enableAuthorization();

    taskService.setAssignee(taskId, userId);

    // when
    List<HistoricTaskInstance> result = historyService.createHistoricTaskInstanceQuery().list();

    // then
    assertEquals(1, result.size());

    // clear
    deleteTask(taskId, true);
  }

  @Test
  public void testHistoricTaskReadPermissionGrantedWhenSettingOwner() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    disableAuthorization();
    createGrantAuthorization(TASK, taskId, userId, TaskPermissions.TASK_ASSIGN);
    enableAuthorization();

    taskService.setOwner(taskId, userId);

    // when
    List<HistoricTaskInstance> result = historyService.createHistoricTaskInstanceQuery().list();

    // then
    assertEquals(1, result.size());
  }

  @Test
  public void testHistoricTaskReadPermissionGrantedWhenSettingCandidateUser() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    disableAuthorization();
    createGrantAuthorization(TASK, taskId, userId, TaskPermissions.TASK_ASSIGN);
    enableAuthorization();

    taskService.addCandidateUser(taskId, userId);

    // when
    List<HistoricTaskInstance> result = historyService.createHistoricTaskInstanceQuery().list();

    // then
    assertEquals(1, result.size());
  }

  @Test
  public void testHistoricTaskReadPermissionGrantedWhenSettingCandidateGroup() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    disableAuthorization();
    createGrantAuthorization(TASK, taskId, userId, TaskPermissions.TASK_ASSIGN);
    enableAuthorization();

    taskService.addCandidateGroup(taskId, groupId);

    // when
    List<HistoricTaskInstance> result = historyService.createHistoricTaskInstanceQuery().list();

    // then
    assertEquals(1, result.size());
  }

  @Test
  public void testStandaloneTaskClearHistoricAuthorization() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String taskId = "myTask";
    createTask(taskId);
    createGrantAuthorization(TASK, taskId, userId, ALL);
    createGrantAuthorization(HISTORIC_TASK, taskId, userId, ALL);

    disableAuthorization();
    Authorization authorization = authorizationService.createAuthorizationQuery()
        .resourceType(HISTORIC_TASK)
        .resourceId(taskId)
        .singleResult();
    enableAuthorization();
    assertNotNull(authorization);

    // when
    historyService.deleteHistoricTaskInstance(taskId);

    // then
    disableAuthorization();
    authorization = authorizationService.createAuthorizationQuery()
        .resourceType(HISTORIC_TASK)
        .resourceId(taskId)
        .singleResult();
    enableAuthorization();

    assertNull(authorization);

    // clear
    taskService.deleteTask(taskId);
  }

  @Test
  public void testProcessTaskClearHistoricAuthorization() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, ALL);
    createGrantAuthorization(HISTORIC_TASK, taskId, userId, ALL);

    disableAuthorization();
    Authorization authorization = authorizationService.createAuthorizationQuery()
        .resourceType(HISTORIC_TASK)
        .resourceId(taskId)
        .singleResult();
    enableAuthorization();
    assertNotNull(authorization);

    taskService.complete(taskId);

    // when
    historyService.deleteHistoricTaskInstance(taskId);

    // then
    disableAuthorization();
    authorization = authorizationService.createAuthorizationQuery()
        .resourceType(HISTORIC_TASK)
        .resourceId(taskId)
        .singleResult();
    enableAuthorization();

    assertNull(authorization);

    // clear
    taskService.deleteTask(taskId);
  }

  @Test
  public void testCheckNonePermissionOnHistoricProcessInstance() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();

    createGrantAuthorization(HISTORIC_PROCESS_INSTANCE, processInstanceId, userId,
        HistoricProcessInstancePermissions.NONE);

    // when
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    // then
    assertThat(query.list()).isEmpty();
  }

  @Test
  public void testCheckReadPermissionOnHistoricProcessInstance() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();

    createGrantAuthorization(HISTORIC_PROCESS_INSTANCE, processInstanceId, userId,
        HistoricProcessInstancePermissions.READ);

    // when
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    // then
    assertThat(query.list())
        .extracting("processInstanceId")
        .containsExactly(processInstanceId);
  }

  @Test
  public void testCheckReadPermissionOnCompletedHHistoricProcessInstance() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorization(HISTORIC_PROCESS_INSTANCE, processInstanceId, userId,
        HistoricProcessInstancePermissions.READ);

    // when
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    // then
    assertThat(query.list())
        .extracting("processInstanceId")
        .containsExactly(processInstanceId);
  }

  @Test
  public void testCheckNoneOnHistoricProcessInstanceAndReadHistoryPermissionOnProcessDefinition() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorization(HISTORIC_PROCESS_INSTANCE, processInstanceId, userId,
        HistoricProcessInstancePermissions.NONE);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY);

    // when
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    // then
    assertThat(query.list())
        .extracting("processInstanceId")
        .containsExactly(processInstanceId);
  }

  @Test
  public void testCheckReadPermissionOnHistoricProcessInstanceAndNonePermissionOnProcessDefinition() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorization(HISTORIC_PROCESS_INSTANCE, processInstanceId, userId,
        HistoricProcessInstancePermissions.READ);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId,
        ProcessDefinitionPermissions.NONE);

    // when
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    // then
    assertThat(query.list())
        .extracting("processInstanceId")
        .containsExactly(processInstanceId);
  }

  // helper ////////////////////////////////////////////////////////

  protected void verifyQueryResults(HistoricTaskInstanceQuery query, int countExpected) {
    verifyQueryResults((AbstractQuery<?, ?>) query, countExpected);
  }

}
