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
import static org.camunda.bpm.engine.authorization.Permissions.DELETE_HISTORY;
import static org.camunda.bpm.engine.authorization.Permissions.READ_HISTORY;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.authorization.AuthorizationQuery;
import org.camunda.bpm.engine.authorization.HistoricProcessInstancePermissions;
import org.camunda.bpm.engine.authorization.MissingAuthorization;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.ProcessDefinitionPermissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.history.CleanableHistoricProcessInstanceReportResult;
import org.camunda.bpm.engine.history.DurationReportResult;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.query.PeriodUnit;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
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
public class HistoricProcessInstanceAuthorizationTest extends AuthorizationTest {

  protected static final String PROCESS_KEY = "oneTaskProcess";
  protected static final String MESSAGE_START_PROCESS_KEY = "messageStartProcess";

  protected String deploymentId;

  @Before
  public void setUp() throws Exception {
    deploymentId = testRule.deploy(
        "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/api/authorization/messageStartEventProcess.bpmn20.xml")
            .getId();
    super.setUp();
  }

  @After
  public void tearDown() {
    super.tearDown();
    processEngineConfiguration.setEnableHistoricInstancePermissions(false);
  }

  // historic process instance query //////////////////////////////////////////////////////////

  @Test
  public void testSimpleQueryWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);

    // when
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // then
    verifyQueryResults(query, 0);
  }

  @Test
  public void testSimpleQueryWithReadHistoryPermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY);

    // when
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // then
    verifyQueryResults(query, 1);

    HistoricProcessInstance instance = query.singleResult();
    assertNotNull(instance);
    assertEquals(processInstanceId, instance.getId());
  }

  @Test
  public void testSimpleQueryWithReadHistoryPermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // then
    verifyQueryResults(query, 1);

    HistoricProcessInstance instance = query.singleResult();
    assertNotNull(instance);
    assertEquals(processInstanceId, instance.getId());
  }

  @Test
  public void testSimpleQueryWithMultiple() {
    // given
    startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY);

    // when
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // then
    verifyQueryResults(query, 1);
  }

  // historic process instance query (multiple process instances) ////////////////////////

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
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

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
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

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
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // then
    verifyQueryResults(query, 7);
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
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // then
    verifyQueryResults(query, 3);

    disableAuthorization();
    List<HistoricProcessInstance> instances = historyService.createHistoricProcessInstanceQuery().list();
    for (HistoricProcessInstance instance : instances) {
      historyService.deleteHistoricProcessInstance(instance.getId());
    }
    enableAuthorization();
  }

  // delete historic process instance //////////////////////////////

  @Test
  public void testDeleteHistoricProcessInstanceWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    try {
      // when
      historyService.deleteHistoricProcessInstance(processInstanceId);
      fail("Exception expected: It should not be possible to delete the historic process instance");
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
  public void testDeleteHistoricProcessInstanceWithDeleteHistoryPermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, DELETE_HISTORY);

    // when
    historyService.deleteHistoricProcessInstance(processInstanceId);

    // then
    disableAuthorization();
    long count = historyService
        .createHistoricProcessInstanceQuery()
        .processInstanceId(processInstanceId)
        .count();
    assertEquals(0, count);
    enableAuthorization();
  }

  @Test
  public void testDeleteHistoricProcessInstanceWithDeleteHistoryPermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, DELETE_HISTORY);

    // when
    historyService.deleteHistoricProcessInstance(processInstanceId);

    // then
    disableAuthorization();
    long count = historyService
        .createHistoricProcessInstanceQuery()
        .processInstanceId(processInstanceId)
        .count();
    assertEquals(0, count);
    enableAuthorization();
  }

  @Test
  public void testDeleteHistoricProcessInstanceAfterDeletingDeployment() {
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
    historyService.deleteHistoricProcessInstance(processInstanceId);

    // then
    disableAuthorization();
    long count = historyService
        .createHistoricProcessInstanceQuery()
        .processInstanceId(processInstanceId)
        .count();
    assertEquals(0, count);
    enableAuthorization();
  }

  // create historic process instance report

  @Test
  public void testHistoricProcessInstanceReportWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    try {
      // when
      historyService
          .createHistoricProcessInstanceReport()
          .duration(PeriodUnit.MONTH);
      fail("Exception expected: It should not be possible to create a historic process instance report");
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
  public void testHistoricProcessInstanceReportWithHistoryReadPermissionOnAny() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    List<DurationReportResult> result = historyService
        .createHistoricProcessInstanceReport()
        .duration(PeriodUnit.MONTH);

    // then
    assertEquals(1, result.size());
  }

  @Test
  public void testReportWithoutQueryCriteriaAndAnyReadHistoryPermission() {
    // given
    ProcessInstance processInstance1 = startProcessInstanceByKey(PROCESS_KEY);
    ProcessInstance processInstance2 = startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    disableAuthorization();
    runtimeService.deleteProcessInstance(processInstance1.getProcessInstanceId(), "");
    runtimeService.deleteProcessInstance(processInstance2.getProcessInstanceId(), "");
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, "*", userId, READ_HISTORY);

    // when
    List<DurationReportResult> result = historyService
        .createHistoricProcessInstanceReport()
        .duration(PeriodUnit.MONTH);

    // then
    assertEquals(1, result.size());
  }

  @Test
  public void testReportWithoutQueryCriteriaAndNoReadHistoryPermission() {
    // given
    ProcessInstance processInstance1 = startProcessInstanceByKey(PROCESS_KEY);
    ProcessInstance processInstance2 = startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    disableAuthorization();
    runtimeService.deleteProcessInstance(processInstance1.getProcessInstanceId(), "");
    runtimeService.deleteProcessInstance(processInstance2.getProcessInstanceId(), "");
    enableAuthorization();

    // when
    try {
      historyService
        .createHistoricProcessInstanceReport()
        .duration(PeriodUnit.MONTH);

      // then
      fail("Exception expected: It should not be possible to create a historic process instance report");
    } catch (AuthorizationException e) {

    }
  }

  @Test
  public void testReportWithQueryCriterionProcessDefinitionKeyInAndReadHistoryPermission() {
    // given
    ProcessInstance processInstance1 = startProcessInstanceByKey(PROCESS_KEY);
    ProcessInstance processInstance2 = startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    disableAuthorization();
    runtimeService.deleteProcessInstance(processInstance1.getProcessInstanceId(), "");
    runtimeService.deleteProcessInstance(processInstance2.getProcessInstanceId(), "");
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY);
    createGrantAuthorization(PROCESS_DEFINITION, MESSAGE_START_PROCESS_KEY, userId, READ_HISTORY);

    // when
    List<DurationReportResult> result = historyService
      .createHistoricProcessInstanceReport()
      .processDefinitionKeyIn(PROCESS_KEY, MESSAGE_START_PROCESS_KEY)
      .duration(PeriodUnit.MONTH);

    // then
    assertEquals(1, result.size());
  }

  @Test
  public void testReportWithQueryCriterionProcessDefinitionKeyInAndMissingReadHistoryPermission() {
    // given
    ProcessInstance processInstance1 = startProcessInstanceByKey(PROCESS_KEY);
    ProcessInstance processInstance2 = startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    disableAuthorization();
    runtimeService.deleteProcessInstance(processInstance1.getProcessInstanceId(), "");
    runtimeService.deleteProcessInstance(processInstance2.getProcessInstanceId(), "");
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY);

    // when
    try {
      historyService
        .createHistoricProcessInstanceReport()
        .processDefinitionKeyIn(PROCESS_KEY, MESSAGE_START_PROCESS_KEY)
        .duration(PeriodUnit.MONTH);

      // then
      fail("Exception expected: It should not be possible to create a historic process instance report");
    } catch (AuthorizationException e) {

    }
  }

  @Test
  public void testReportWithQueryCriterionProcessDefinitionIdInAndReadHistoryPermission() {
    // given
    ProcessInstance processInstance1 = startProcessInstanceByKey(PROCESS_KEY);
    ProcessInstance processInstance2 = startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    disableAuthorization();
    runtimeService.deleteProcessInstance(processInstance1.getProcessInstanceId(), "");
    runtimeService.deleteProcessInstance(processInstance2.getProcessInstanceId(), "");
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY);
    createGrantAuthorization(PROCESS_DEFINITION, MESSAGE_START_PROCESS_KEY, userId, READ_HISTORY);

    // when
    List<DurationReportResult> result = historyService
      .createHistoricProcessInstanceReport()
      .processDefinitionIdIn(processInstance1.getProcessDefinitionId(), processInstance2.getProcessDefinitionId())
      .duration(PeriodUnit.MONTH);

    // then
    assertEquals(1, result.size());
  }

  @Test
  public void testReportWithQueryCriterionProcessDefinitionIdInAndMissingReadHistoryPermission() {
    // given
    ProcessInstance processInstance1 = startProcessInstanceByKey(PROCESS_KEY);
    ProcessInstance processInstance2 = startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    disableAuthorization();
    runtimeService.deleteProcessInstance(processInstance1.getProcessInstanceId(), "");
    runtimeService.deleteProcessInstance(processInstance2.getProcessInstanceId(), "");
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY);

    // when
    try {
      historyService
        .createHistoricProcessInstanceReport()
        .processDefinitionIdIn(processInstance1.getProcessDefinitionId(), processInstance2.getProcessDefinitionId())
        .duration(PeriodUnit.MONTH);

      // then
      fail("Exception expected: It should not be possible to create a historic process instance report");
    } catch (AuthorizationException e) {

    }
  }

  @Test
  public void testReportWithMixedQueryCriteriaAndReadHistoryPermission() {
    // given
    ProcessInstance processInstance1 = startProcessInstanceByKey(PROCESS_KEY);
    ProcessInstance processInstance2 = startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    disableAuthorization();
    runtimeService.deleteProcessInstance(processInstance1.getProcessInstanceId(), "");
    runtimeService.deleteProcessInstance(processInstance2.getProcessInstanceId(), "");
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY);
    createGrantAuthorization(PROCESS_DEFINITION, MESSAGE_START_PROCESS_KEY, userId, READ_HISTORY);

    // when
    List<DurationReportResult> result = historyService
      .createHistoricProcessInstanceReport()
      .processDefinitionKeyIn(PROCESS_KEY)
      .processDefinitionIdIn(processInstance2.getProcessDefinitionId())
      .duration(PeriodUnit.MONTH);

    // then
    assertEquals(0, result.size());
  }

  @Test
  public void testReportWithMixedQueryCriteriaAndMissingReadHistoryPermission() {
    // given
    ProcessInstance processInstance1 = startProcessInstanceByKey(PROCESS_KEY);
    ProcessInstance processInstance2 = startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    disableAuthorization();
    runtimeService.deleteProcessInstance(processInstance1.getProcessInstanceId(), "");
    runtimeService.deleteProcessInstance(processInstance2.getProcessInstanceId(), "");
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY);

    // when
    try {
    historyService
      .createHistoricProcessInstanceReport()
      .processDefinitionKeyIn(PROCESS_KEY)
      .processDefinitionIdIn(processInstance2.getProcessDefinitionId())
      .duration(PeriodUnit.MONTH);

      // then
      fail("Exception expected: It should not be possible to create a historic process instance report");
    } catch (AuthorizationException e) {

    }
  }

  @Test
  public void testReportWithQueryCriterionProcessInstanceIdInWrongProcessDefinitionId() {
    // when
    List<DurationReportResult> result = historyService
      .createHistoricProcessInstanceReport()
      .processDefinitionIdIn("aWrongProcessDefinitionId")
      .duration(PeriodUnit.MONTH);

    // then
    assertEquals(0, result.size());
  }

  @Test
  public void testHistoryCleanupReportWithPermissions() {
    // given
    prepareProcessInstances(PROCESS_KEY, -6, 5, 10);

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, Permissions.READ, Permissions.READ_HISTORY);
    createGrantAuthorizationGroup(PROCESS_DEFINITION, PROCESS_KEY, groupId, Permissions.READ, Permissions.READ_HISTORY);

    List<CleanableHistoricProcessInstanceReportResult> reportResults = historyService.createCleanableHistoricProcessInstanceReport().list();

    // then
    assertEquals(1, reportResults.size());
    assertEquals(10, reportResults.get(0).getCleanableProcessInstanceCount());
    assertEquals(10, reportResults.get(0).getFinishedProcessInstanceCount());
  }

  @Test
  public void testHistoryCleanupReportWithReadPermissionOnly() {
    // given
    prepareProcessInstances(PROCESS_KEY, -6, 5, 10);

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, Permissions.READ);

    // when
    List<CleanableHistoricProcessInstanceReportResult> reportResults = historyService.createCleanableHistoricProcessInstanceReport().list();

    // then
    assertEquals(0, reportResults.size());
  }

  @Test
  public void testHistoryCleanupReportWithReadHistoryPermissionOnly() {
    // given
    prepareProcessInstances(PROCESS_KEY, -6, 5, 10);

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, Permissions.READ_HISTORY);

    // when
    List<CleanableHistoricProcessInstanceReportResult> reportResults = historyService.createCleanableHistoricProcessInstanceReport().list();

    // then
    assertEquals(0, reportResults.size());
  }

  @Test
  public void testHistoryCleanupReportWithoutPermissions() {
    // given
    prepareProcessInstances(PROCESS_KEY, -6, 5, 10);

    // when
    List<CleanableHistoricProcessInstanceReportResult> reportResults = historyService.createCleanableHistoricProcessInstanceReport().list();

    // then
    assertEquals(0, reportResults.size());
  }

  @Test
  public void testCheckAllHistoricProcessInstancePermissions() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    // when
    createGrantAuthorization(Resources.HISTORIC_PROCESS_INSTANCE, ANY, userId,
        HistoricProcessInstancePermissions.ALL);

    // then
    assertThat(authorizationService.isUserAuthorized(userId, null,
        HistoricProcessInstancePermissions.NONE, Resources.HISTORIC_PROCESS_INSTANCE)).isTrue();

    assertThat(authorizationService.isUserAuthorized(userId, null,
        HistoricProcessInstancePermissions.READ, Resources.HISTORIC_PROCESS_INSTANCE)).isTrue();

    assertThat(authorizationService.isUserAuthorized(userId, null,
        HistoricProcessInstancePermissions.ALL, Resources.HISTORIC_PROCESS_INSTANCE)).isTrue();
  }

  @Test
  public void testCheckReadHistoricProcessInstancePermissions() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    // when
    createGrantAuthorization(Resources.HISTORIC_PROCESS_INSTANCE, ANY, userId,
        HistoricProcessInstancePermissions.READ);

    // then
    assertThat(authorizationService.isUserAuthorized(userId, null,
        HistoricProcessInstancePermissions.NONE, Resources.HISTORIC_PROCESS_INSTANCE)).isTrue();

    assertThat(authorizationService.isUserAuthorized(userId, null,
        HistoricProcessInstancePermissions.READ, Resources.HISTORIC_PROCESS_INSTANCE)).isTrue();

    assertThat(authorizationService.isUserAuthorized(userId, null,
        HistoricProcessInstancePermissions.ALL, Resources.HISTORIC_PROCESS_INSTANCE)).isFalse();
  }

  @Test
  public void testCheckNoneHistoricProcessInstancePermission() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    // when
    createGrantAuthorization(Resources.HISTORIC_PROCESS_INSTANCE, ANY, userId,
        HistoricProcessInstancePermissions.NONE);

    // then
    assertThat(authorizationService.isUserAuthorized(userId, null,
        HistoricProcessInstancePermissions.NONE, Resources.HISTORIC_PROCESS_INSTANCE)).isTrue();

    assertThat(authorizationService.isUserAuthorized(userId, null,
        HistoricProcessInstancePermissions.READ, Resources.HISTORIC_PROCESS_INSTANCE)).isFalse();

    assertThat(authorizationService.isUserAuthorized(userId, null,
        HistoricProcessInstancePermissions.ALL, Resources.HISTORIC_PROCESS_INSTANCE)).isFalse();
  }

  @Test
  public void testCheckNonePermissionOnHistoricProcessInstance() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();

    createGrantAuthorization(Resources.HISTORIC_PROCESS_INSTANCE, processInstanceId, userId,
        HistoricProcessInstancePermissions.NONE);

    // when
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // then
    assertThat(query.list()).isEmpty();
  }

  @Test
  public void testCheckReadPermissionOnHistoricProcessInstance() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();

    createGrantAuthorization(Resources.HISTORIC_PROCESS_INSTANCE, processInstanceId, userId,
        HistoricProcessInstancePermissions.READ);

    // when
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // then
    assertThat(query.list())
        .extracting("processInstanceId")
        .containsExactly(processInstanceId);
  }

  @Test
  public void testCheckReadPermissionOnCompletedHistoricProcessInstance() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorization(Resources.HISTORIC_PROCESS_INSTANCE, processInstanceId, userId,
        HistoricProcessInstancePermissions.READ);

    // when
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

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

    createGrantAuthorization(Resources.HISTORIC_PROCESS_INSTANCE, processInstanceId, userId,
        HistoricProcessInstancePermissions.NONE);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY);

    // when
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // then
    assertThat(query.list())
        .extracting("processInstanceId")
        .containsExactly(processInstanceId);
  }

  @Test
  public void testCheckReadOnHistoricProcessInstanceAndNonePermissionOnProcessDefinition() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorization(Resources.HISTORIC_PROCESS_INSTANCE, processInstanceId, userId,
        HistoricProcessInstancePermissions.READ);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId,
        ProcessDefinitionPermissions.NONE);

    // when
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // then
    assertThat(query.list())
        .extracting("processInstanceId")
        .containsExactly(processInstanceId);
  }

  @Test
  public void testHistoricProcessInstancePermissionsAuthorizationDisabled() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getProcessInstanceId();

    disableAuthorization();

    // when
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // then
    assertThat(query.list())
        .extracting("processInstanceId")
        .containsExactly(processInstanceId);
  }

  @Test
  public void testDeleteHistoricAuthorizationRelatedToHistoricProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();

    String taskId = selectSingleTask().getId();

    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, DELETE_HISTORY);

    createGrantAuthorization(Resources.HISTORIC_PROCESS_INSTANCE, processInstanceId, userId,
        HistoricProcessInstancePermissions.READ);

    // assume
    AuthorizationQuery authorizationQuery = authorizationService.createAuthorizationQuery()
        .resourceType(Resources.HISTORIC_PROCESS_INSTANCE)
        .resourceId(processInstanceId);

    assertThat(authorizationQuery.list())
        .extracting("resourceId")
        .containsExactly(processInstanceId);

    // when
    historyService.deleteHistoricProcessInstance(processInstanceId);

    // then
    authorizationQuery = authorizationService.createAuthorizationQuery()
        .resourceType(Resources.HISTORIC_PROCESS_INSTANCE)
        .resourceId(processInstanceId);

    assertThat(authorizationQuery.list()).isEmpty();
  }

  // helper ////////////////////////////////////////////////////////

  protected void verifyQueryResults(HistoricProcessInstanceQuery query, int countExpected) {
    verifyQueryResults((AbstractQuery<?, ?>) query, countExpected);
  }

  protected void prepareProcessInstances(String key, int daysInThePast, Integer historyTimeToLive, int instanceCount) {
    ProcessDefinition processDefinition = selectProcessDefinitionByKey(key);
    disableAuthorization();
    repositoryService.updateProcessDefinitionHistoryTimeToLive(processDefinition.getId(), historyTimeToLive);
    enableAuthorization();

    Date oldCurrentTime = ClockUtil.getCurrentTime();
    ClockUtil.setCurrentTime(DateUtils.addDays(new Date(), daysInThePast));

    List<String> processInstanceIds = new ArrayList<String>();
    for (int i = 0; i < instanceCount; i++) {
      ProcessInstance processInstance = startProcessInstanceByKey(key);
      processInstanceIds.add(processInstance.getId());
    }

    disableAuthorization();
    runtimeService.deleteProcessInstances(processInstanceIds, null, true, true);
    enableAuthorization();

    ClockUtil.setCurrentTime(oldCurrentTime);
  }

}
