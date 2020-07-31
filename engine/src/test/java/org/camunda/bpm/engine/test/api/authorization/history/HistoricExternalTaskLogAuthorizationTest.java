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
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.test.api.runtime.migration.models.builder.DefaultExternalTaskModelBuilder.DEFAULT_PROCESS_KEY;
import static org.camunda.bpm.engine.test.api.runtime.migration.models.builder.DefaultExternalTaskModelBuilder.DEFAULT_TOPIC;
import static org.camunda.bpm.engine.test.api.runtime.migration.models.builder.DefaultExternalTaskModelBuilder.createDefaultExternalTaskModel;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.authorization.HistoricProcessInstancePermissions;
import org.camunda.bpm.engine.authorization.ProcessDefinitionPermissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.history.HistoricExternalTaskLogQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.authorization.AuthorizationTest;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoricExternalTaskLogAuthorizationTest extends AuthorizationTest {

  protected final String WORKER_ID = "aWorkerId";
  protected final long LOCK_DURATION = 5 * 60L * 1000L;
  protected final String ERROR_DETAILS = "These are the error details!";
  protected final String ANOTHER_PROCESS_KEY = "AnotherProcess";

  @Before
  public void setUp() throws Exception {
    BpmnModelInstance defaultModel = createDefaultExternalTaskModel().build();
    BpmnModelInstance modifiedModel = createDefaultExternalTaskModel().processKey(ANOTHER_PROCESS_KEY).build();
    testRule.deploy(defaultModel, modifiedModel);
    super.setUp();
  }

  @After
  public void tearDown() {
    super.tearDown();
    processEngineConfiguration.setEnableHistoricInstancePermissions(false);
  }

  @Test
  public void testSimpleQueryWithoutAuthorization() {
    // given
    startProcessInstanceByKey(DEFAULT_PROCESS_KEY);

    // when
    HistoricExternalTaskLogQuery query = historyService.createHistoricExternalTaskLogQuery();

    // then
    verifyQueryResults(query, 0);
  }

  @Test
  public void testSimpleQueryWithHistoryReadPermissionOnProcessDefinition() {

    // given
    startProcessInstanceByKey(DEFAULT_PROCESS_KEY);
    createGrantAuthorization(PROCESS_DEFINITION, DEFAULT_PROCESS_KEY, userId, READ_HISTORY);

    // when
    HistoricExternalTaskLogQuery query = historyService.createHistoricExternalTaskLogQuery();

    // then
    verifyQueryResults(query, 1);
  }

  @Test
  public void testSimpleQueryWithHistoryReadPermissionOnAnyProcessDefinition() {

    // given
    startProcessInstanceByKey(DEFAULT_PROCESS_KEY);
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    HistoricExternalTaskLogQuery query = historyService.createHistoricExternalTaskLogQuery();

    // then
    verifyQueryResults(query, 1);
  }

  @Test
  public void testSimpleQueryWithMultipleAuthorizations() {
    // given
    startProcessInstanceByKey(DEFAULT_PROCESS_KEY);
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);
    createGrantAuthorization(PROCESS_DEFINITION, DEFAULT_PROCESS_KEY, userId, READ_HISTORY);

    // when
    HistoricExternalTaskLogQuery query = historyService.createHistoricExternalTaskLogQuery();

    // then
    verifyQueryResults(query, 1);
  }

  @Test
  public void testQueryWithoutAuthorization() {
    // given
    startThreeProcessInstancesDeleteOneAndCompleteTwoWithFailure();

    // when
    HistoricExternalTaskLogQuery query = historyService.createHistoricExternalTaskLogQuery();

    // then
    verifyQueryResults(query, 0);
  }

  @Test
  public void testQueryWithHistoryReadPermissionOnOneProcessDefinition() {
    // given
    startThreeProcessInstancesDeleteOneAndCompleteTwoWithFailure();
    createGrantAuthorization(PROCESS_DEFINITION, DEFAULT_PROCESS_KEY, userId, READ_HISTORY);

    // when
    HistoricExternalTaskLogQuery query = historyService.createHistoricExternalTaskLogQuery();

    // then
    verifyQueryResults(query, 6);
  }

  @Test
  public void testQueryWithHistoryReadPermissionOnAnyProcessDefinition() {
    // given
    startThreeProcessInstancesDeleteOneAndCompleteTwoWithFailure();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    HistoricExternalTaskLogQuery query = historyService.createHistoricExternalTaskLogQuery();

    // then
    verifyQueryResults(query, 8);
  }

  @Test
  public void testGetErrorDetailsWithoutAuthorization() {
    // given
    startThreeProcessInstancesDeleteOneAndCompleteTwoWithFailure();

    disableAuthorization();
    String failedHistoricExternalTaskLogId = historyService
      .createHistoricExternalTaskLogQuery()
      .failureLog()
      .list()
      .get(0)
      .getId();
    enableAuthorization();

    try {
      // when
      String stacktrace = historyService.getHistoricExternalTaskLogErrorDetails(failedHistoricExternalTaskLogId);
      fail("Exception expected: It should not be possible to retrieve the error details");
    } catch (AuthorizationException e) {
      // then
      String exceptionMessage = e.getMessage();
      testRule.assertTextPresent(userId, exceptionMessage);
      testRule.assertTextPresent(READ_HISTORY.getName(), exceptionMessage);
      testRule.assertTextPresent(DEFAULT_PROCESS_KEY, exceptionMessage);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), exceptionMessage);
    }
  }

  @Test
  public void testGetErrorDetailsWithHistoryReadPermissionOnProcessDefinition() {

    // given
    startThreeProcessInstancesDeleteOneAndCompleteTwoWithFailure();
    createGrantAuthorization(PROCESS_DEFINITION, DEFAULT_PROCESS_KEY, userId, READ_HISTORY);

    String failedHistoricExternalTaskLogId = historyService
      .createHistoricExternalTaskLogQuery()
      .failureLog()
      .list()
      .get(0)
      .getId();

    // when
    String stacktrace = historyService.getHistoricExternalTaskLogErrorDetails(failedHistoricExternalTaskLogId);

    // then
    assertNotNull(stacktrace);
    assertEquals(ERROR_DETAILS, stacktrace);
  }

  @Test
  public void testGetErrorDetailsWithHistoryReadPermissionOnProcessAnyDefinition() {

    // given
    startThreeProcessInstancesDeleteOneAndCompleteTwoWithFailure();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    String failedHistoricExternalTaskLogId = historyService
      .createHistoricExternalTaskLogQuery()
      .failureLog()
      .list()
      .get(0)
      .getId();

    // when
    String stacktrace = historyService.getHistoricExternalTaskLogErrorDetails(failedHistoricExternalTaskLogId);

    // then
    assertNotNull(stacktrace);
    assertEquals(ERROR_DETAILS, stacktrace);
  }

  @Test
  public void testCheckNonePermissionOnHistoricProcessInstance() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String processInstanceId = startProcessAndExecuteJob(DEFAULT_PROCESS_KEY)
        .getProcessInstanceId();

    createGrantAuthorization(Resources.HISTORIC_PROCESS_INSTANCE, processInstanceId, userId,
        HistoricProcessInstancePermissions.NONE);

    // when
    HistoricExternalTaskLogQuery query = historyService.createHistoricExternalTaskLogQuery();

    // then
    assertThat(query.list()).isEmpty();
  }

  @Test
  public void testCheckReadPermissionOnHistoricProcessInstance() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String processInstanceId = startProcessAndExecuteJob(DEFAULT_PROCESS_KEY)
        .getProcessInstanceId();

    createGrantAuthorization(Resources.HISTORIC_PROCESS_INSTANCE, processInstanceId, userId,
        HistoricProcessInstancePermissions.READ);

    // when
    HistoricExternalTaskLogQuery query = historyService.createHistoricExternalTaskLogQuery();

    // then
    assertThat(query.list())
        .extracting("processInstanceId")
        .containsExactly(processInstanceId);
  }

  @Test
  public void testCheckNoneOnHistoricProcessInstanceAndReadHistoryPermissionOnProcessDefinition() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String processInstanceId = startProcessAndExecuteJob(DEFAULT_PROCESS_KEY)
        .getProcessInstanceId();

    createGrantAuthorization(Resources.HISTORIC_PROCESS_INSTANCE, processInstanceId, userId,
        HistoricProcessInstancePermissions.NONE);
    createGrantAuthorization(PROCESS_DEFINITION, DEFAULT_PROCESS_KEY, userId,
        ProcessDefinitionPermissions.READ_HISTORY);

    // when
    HistoricExternalTaskLogQuery query = historyService.createHistoricExternalTaskLogQuery();

    // then
    assertThat(query.list())
        .extracting("processInstanceId")
        .containsExactly(processInstanceId);
  }

  @Test
  public void testCheckReadOnHistoricProcessInstanceAndNonePermissionOnProcessDefinition() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String processInstanceId = startProcessAndExecuteJob(DEFAULT_PROCESS_KEY)
        .getProcessInstanceId();

    createGrantAuthorization(Resources.HISTORIC_PROCESS_INSTANCE, processInstanceId, userId,
        HistoricProcessInstancePermissions.READ);
    createGrantAuthorization(PROCESS_DEFINITION, DEFAULT_PROCESS_KEY, userId,
        ProcessDefinitionPermissions.NONE);

    // when
    HistoricExternalTaskLogQuery query = historyService.createHistoricExternalTaskLogQuery();

    // then
    assertThat(query.list())
        .extracting("processInstanceId")
        .containsExactly(processInstanceId);
  }

  @Test
  public void testHistoricProcessInstancePermissionsAuthorizationDisabled() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String processInstanceId = startProcessAndExecuteJob(DEFAULT_PROCESS_KEY)
        .getProcessInstanceId();

    disableAuthorization();

    // when
    HistoricExternalTaskLogQuery query = historyService.createHistoricExternalTaskLogQuery();

    // then
    assertThat(query.list())
        .extracting("processInstanceId")
        .containsExactly(processInstanceId);
  }

  protected void startThreeProcessInstancesDeleteOneAndCompleteTwoWithFailure() {
    disableAuthorization();
    ProcessInstance pi1 = startProcessInstanceByKey(DEFAULT_PROCESS_KEY);
    ProcessInstance pi2 = startProcessInstanceByKey(DEFAULT_PROCESS_KEY);
    ProcessInstance pi3 = startProcessInstanceByKey(ANOTHER_PROCESS_KEY);

    completeExternalTaskWithFailure(pi1);
    completeExternalTaskWithFailure(pi2);

    runtimeService.deleteProcessInstance(pi3.getId(), "Dummy reason for deletion!");
    enableAuthorization();
  }

  protected void completeExternalTaskWithFailure(ProcessInstance pi) {
    ExternalTask task = externalTaskService
      .createExternalTaskQuery()
      .processInstanceId(pi.getId())
      .singleResult();
    completeExternalTaskWithFailure(task.getId());
  }

  protected void completeExternalTaskWithFailure(String externalTaskId) {
    List<LockedExternalTask> list = externalTaskService.fetchAndLock(5, WORKER_ID, false)
      .topic(DEFAULT_TOPIC, LOCK_DURATION)
      .execute();
    externalTaskService.handleFailure(externalTaskId, WORKER_ID, "This is an error!", ERROR_DETAILS, 1, 0L);
    externalTaskService.complete(externalTaskId, WORKER_ID);
    // unlock the remaining tasks
    for (LockedExternalTask lockedExternalTask : list) {
      if (!lockedExternalTask.getId().equals(externalTaskId)) {
        externalTaskService.unlock(lockedExternalTask.getId());
      }
    }
  }

}
