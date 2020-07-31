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
import static org.camunda.bpm.engine.authorization.ProcessDefinitionPermissions.READ_HISTORY_VARIABLE;
import static org.camunda.bpm.engine.authorization.Resources.HISTORIC_PROCESS_INSTANCE;
import static org.camunda.bpm.engine.authorization.Resources.HISTORIC_TASK;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.authorization.HistoricProcessInstancePermissions;
import org.camunda.bpm.engine.authorization.HistoricTaskPermissions;
import org.camunda.bpm.engine.authorization.ProcessDefinitionPermissions;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.impl.AbstractQuery;
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
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoricVariableInstanceAuthorizationTest extends AuthorizationTest {

  protected static final String PROCESS_KEY = "oneTaskProcess";
  protected static final String MESSAGE_START_PROCESS_KEY = "messageStartProcess";
  protected static final String CASE_KEY = "oneTaskCase";

  protected boolean ensureSpecificVariablePermission;
  protected String deploymentId;

  @Before
  public void setUp() throws Exception {
    deploymentId = testRule.deploy(
        "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/api/authorization/messageStartEventProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/api/authorization/oneTaskCase.cmmn")
            .getId();

    ensureSpecificVariablePermission = processEngineConfiguration.isEnforceSpecificVariablePermission();
    super.setUp();
  }

  @After
  public void tearDown() {
    super.tearDown();
    processEngineConfiguration.setEnableHistoricInstancePermissions(false);
    processEngineConfiguration.setEnforceSpecificVariablePermission(ensureSpecificVariablePermission);
  }

  // historic variable instance query (standalone task) /////////////////////////////////////////////

  @Test
  public void testQueryAfterStandaloneTaskVariables() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    disableAuthorization();
    taskService.setVariables(taskId, getVariables());
    enableAuthorization();

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    verifyQueryResults(query, 1);

    deleteTask(taskId, true);
  }

  // historic variable instance query (process variables) ///////////////////////////////////////////

  @Test
  public void testSimpleQueryWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY, getVariables());

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    verifyQueryResults(query, 0);
  }

  @Test
  public void testSimpleQueryWithReadHistoryPermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY);

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    verifyQueryResults(query, 1);
  }

  @Test
  public void testSimpleQueryWithReadHistoryPermissionOnAnyProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    verifyQueryResults(query, 1);
  }

  @Test
  public void testSimpleQueryWithMultiple() {
    // given
    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY);

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    verifyQueryResults(query, 1);
  }

  @Test
  public void testSimpleQueryWithReadHistoryVariablePermissionOnProcessDefinition() {
    // given
    setReadHistoryVariableAsDefaultReadPermission();

    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY_VARIABLE);

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    verifyQueryResults(query, 1);
  }

  @Test
  public void testSimpleQueryWithReadHistoryVariablePermissionOnAnyProcessDefinition() {
    setReadHistoryVariableAsDefaultReadPermission();

    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY_VARIABLE);

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    verifyQueryResults(query, 1);
  }

  @Test
  public void testSimpleQueryWithMultipleReadHistoryVariable() {
    setReadHistoryVariableAsDefaultReadPermission();

    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY_VARIABLE);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY_VARIABLE);

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    verifyQueryResults(query, 1);
  }

  // historic variable instance query (multiple process instances) ////////////////////////

  @Test
  public void testQueryWithoutAuthorization() {
    startMultipleProcessInstances();

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    verifyQueryResults(query, 0);
  }

  @Test
  public void testQueryWithReadHistoryPermissionOnProcessDefinition() {
    startMultipleProcessInstances();

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY);

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    verifyQueryResults(query, 3);
  }

  @Test
  public void testQueryWithReadHistoryPermissionOnAnyProcessDefinition() {
    // given
    startMultipleProcessInstances();

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    verifyQueryResults(query, 7);
  }

  @Test
  public void testQueryWithReadHistoryVariablePermissionOnProcessDefinition() {
    setReadHistoryVariableAsDefaultReadPermission();

    startMultipleProcessInstances();

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY_VARIABLE);

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    verifyQueryResults(query, 3);
  }

  @Test
  public void testQueryWithReadHistoryVariablePermissionOnAnyProcessDefinition() {
    setReadHistoryVariableAsDefaultReadPermission();

    startMultipleProcessInstances();

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY_VARIABLE);

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    verifyQueryResults(query, 7);
  }

  // historic variable instance query (case variables) /////////////////////////////////////////////

  @Test
  public void testQueryAfterCaseVariables() {
    // given
    createCaseInstanceByKey(CASE_KEY, getVariables());

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    verifyQueryResults(query, 1);
  }

  // historic variable instance query (mixed variables) ////////////////////////////////////

  @Test
  public void testMixedQueryWithoutAuthorization() {
    startMultipleProcessInstances();

    setupMultipleMixedVariables();

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

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
    startMultipleProcessInstances();

    setupMultipleMixedVariables();

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY);

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

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
    startMultipleProcessInstances();

    setupMultipleMixedVariables();

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    verifyQueryResults(query, 14);

    deleteTask("one", true);
    deleteTask("two", true);
    deleteTask("three", true);
    deleteTask("four", true);
    deleteTask("five", true);
  }

  @Test
  public void testMixedQueryWithReadHistoryVariablePermissionOnProcessDefinition() {
    setReadHistoryVariableAsDefaultReadPermission();

    startMultipleProcessInstances();

    setupMultipleMixedVariables();

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY_VARIABLE);

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    verifyQueryResults(query, 10);

    deleteTask("one", true);
    deleteTask("two", true);
    deleteTask("three", true);
    deleteTask("four", true);
    deleteTask("five", true);
  }

  @Test
  public void testMixedQueryWithReadHistoryVariablePermissionOnAnyProcessDefinition() {
    setReadHistoryVariableAsDefaultReadPermission();

    startMultipleProcessInstances();

    setupMultipleMixedVariables();

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY_VARIABLE);

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

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
    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    startProcessInstanceByKey(PROCESS_KEY, getVariables());
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
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    verifyQueryResults(query, 3);

    cleanUpAfterDeploymentDeletion();
  }

  @Test
  public void testQueryAfterDeletingDeploymentWithReadHistoryVariable() {
    setReadHistoryVariableAsDefaultReadPermission();

    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY_VARIABLE);

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
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    verifyQueryResults(query, 3);

    cleanUpAfterDeploymentDeletion();
  }

  // delete historic variable instance (process variables) /////////////////////////////////////////////
  @Test
  public void testDeleteHistoricProcessVariableInstanceWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    
    disableAuthorization();
    String variableInstanceId = historyService.createHistoricVariableInstanceQuery().singleResult().getId();
    assertEquals(1L, historyService.createHistoricDetailQuery().count());
    enableAuthorization();

    try {
      // when
      historyService.deleteHistoricVariableInstance(variableInstanceId);
      fail("Exception expected: It should not be possible to delete the historic variable instance");
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
  public void testDeleteHistoricProcessVariableInstanceWithDeleteHistoryPermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, DELETE_HISTORY);
    
    disableAuthorization();
    String variableInstanceId = historyService.createHistoricVariableInstanceQuery().singleResult().getId();
    assertEquals(1L, historyService.createHistoricDetailQuery().count());
    enableAuthorization();

    try {
      // when
      historyService.deleteHistoricVariableInstance(variableInstanceId);
    } catch (AuthorizationException e) {
      fail("It should be possible to delete the historic variable instance with granted permissions");
    }
    // then
    verifyVariablesDeleted();
  }

  // delete deployment (cascade = false)
  @Test
  public void testDeleteHistoricProcessVariableInstanceAfterDeletingDeployment() {
    // given
    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();
    
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, DELETE_HISTORY);
    
    disableAuthorization();
    repositoryService.deleteDeployment(deploymentId);
    String variableInstanceId = historyService.createHistoricVariableInstanceQuery().singleResult().getId();
    assertEquals(1L, historyService.createHistoricDetailQuery().count());
    enableAuthorization();

    try {
      // when
      historyService.deleteHistoricVariableInstance(variableInstanceId);
    } catch (AuthorizationException e) {
      fail("It should be possible to delete the historic variable instance with granted permissions after the process definition is deleted");
    }
    // then
    verifyVariablesDeleted();
    cleanUpAfterDeploymentDeletion();
  }
  
  // delete historic variable instance (case variables) /////////////////////////////////////////////
  @Test
  public void testDeleteHistoricCaseVariableInstance() {
    // given
    createCaseInstanceByKey(CASE_KEY, getVariables());
    
    disableAuthorization();
    String variableInstanceId = historyService.createHistoricVariableInstanceQuery().singleResult().getId();
    assertEquals(1L, historyService.createHistoricDetailQuery().count());
    enableAuthorization();

    // when
    historyService.deleteHistoricVariableInstance(variableInstanceId);

    // then
    verifyVariablesDeleted();
  }
  
  // delete historic variable instance (task variables) /////////////////////////////////////////////
  @Test
  public void testDeleteHistoricStandaloneTaskVariableInstance() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    disableAuthorization();
    taskService.setVariables(taskId, getVariables());
    String variableInstanceId = historyService.createHistoricVariableInstanceQuery().singleResult().getId();
    assertEquals(1L, historyService.createHistoricDetailQuery().count());
    enableAuthorization();

    // when
    historyService.deleteHistoricVariableInstance(variableInstanceId);

    // then
    verifyVariablesDeleted();
    deleteTask(taskId, true);
    
    // XXX if CAM-6570 is implemented, there should be a check for variables of standalone tasks here as well
  }
  
  // delete historic variable instances (process variables) /////////////////////////////////////////////
  @Test
  public void testDeleteHistoricProcessVariableInstancesWithoutAuthorization() {
    // given
    ProcessInstance instance = startProcessInstanceByKey(PROCESS_KEY, getVariables());
    verifyVariablesCreated();
    
    try {
      // when
      historyService.deleteHistoricVariableInstancesByProcessInstanceId(instance.getId());
      fail("Exception expected: It should not be possible to delete the historic variable instance");
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
  public void testDeleteHistoricProcessVariableInstancesWithDeleteHistoryPermissionOnProcessDefinition() {
    // given
    ProcessInstance instance = startProcessInstanceByKey(PROCESS_KEY, getVariables());
    verifyVariablesCreated();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, DELETE_HISTORY);
    
    try {
      // when
      historyService.deleteHistoricVariableInstancesByProcessInstanceId(instance.getId());
    } catch (AuthorizationException e) {
      fail("It should be possible to delete the historic variable instance with granted permissions");
    }
    // then
    verifyVariablesDeleted();
  }
  
  // delete deployment (cascade = false)
  @Test
  public void testDeleteHistoricProcessVariableInstancesAfterDeletingDeployment() {
    // given
    ProcessInstance instance = startProcessInstanceByKey(PROCESS_KEY, getVariables());
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();
    
    verifyVariablesCreated();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, DELETE_HISTORY);
    
    disableAuthorization();
    repositoryService.deleteDeployment(deploymentId);
    enableAuthorization();

    try {
      // when
      historyService.deleteHistoricVariableInstancesByProcessInstanceId(instance.getId());
    } catch (AuthorizationException e) {
      fail("It should be possible to delete the historic variable instance with granted permissions after the process definition is deleted");
    }
    
    // then
    verifyVariablesDeleted();
    cleanUpAfterDeploymentDeletion();
  }

  // helper ////////////////////////////////////////////////////////

  protected void verifyQueryResults(HistoricVariableInstanceQuery query, int countExpected) {
    verifyQueryResults((AbstractQuery<?, ?>) query, countExpected);
  }
  
  protected void verifyVariablesDeleted() {
    disableAuthorization();
    assertEquals(0L, historyService.createHistoricVariableInstanceQuery().count());
    assertEquals(0L, historyService.createHistoricDetailQuery().count());
    enableAuthorization();
  }
  
  protected void verifyVariablesCreated() {
    disableAuthorization();
    assertEquals(1L, historyService.createHistoricVariableInstanceQuery().count());
    assertEquals(1L, historyService.createHistoricDetailQuery().count());
    enableAuthorization();
  }
  
  protected void cleanUpAfterDeploymentDeletion() {
    disableAuthorization();
    List<HistoricProcessInstance> instances = historyService.createHistoricProcessInstanceQuery().list();
    for (HistoricProcessInstance instance : instances) {
      historyService.deleteHistoricProcessInstance(instance.getId());
    }
    enableAuthorization();
  }

  protected void startMultipleProcessInstances() {
    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    startProcessInstanceByKey(PROCESS_KEY, getVariables());

    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY, getVariables());
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY, getVariables());
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY, getVariables());
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY, getVariables());
  }

  protected void setupMultipleMixedVariables() {
    createTask("one");
    createTask("two");
    createTask("three");
    createTask("four");
    createTask("five");

    disableAuthorization();
    taskService.setVariables("one", getVariables());
    taskService.setVariables("two", getVariables());
    taskService.setVariables("three", getVariables());
    taskService.setVariables("four", getVariables());
    taskService.setVariables("five", getVariables());
    enableAuthorization();

    createCaseInstanceByKey(CASE_KEY, getVariables());
    createCaseInstanceByKey(CASE_KEY, getVariables());
  }

  @Test
  public void testCheckNonePermissionOnHistoricTask() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.setVariable(taskId, "foo", "bar");
    enableAuthorization();

    createGrantAuthorization(HISTORIC_TASK, taskId, userId, HistoricTaskPermissions.NONE);

    // when
    List<HistoricVariableInstance> result = historyService.createHistoricVariableInstanceQuery()
        .list();

    // then
    assertEquals(0, result.size());
  }

  @Test
  public void testCheckReadPermissionOnHistoricTask() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.setVariable(taskId, "foo", "bar");
    enableAuthorization();

    createGrantAuthorization(HISTORIC_TASK, taskId, userId, HistoricTaskPermissions.READ);

    // when
    List<HistoricVariableInstance> result = historyService.createHistoricVariableInstanceQuery()
        .list();

    // then
    assertEquals(1, result.size());
  }

  @Test
  public void testCheckReadPermissionOnStandaloneHistoricTask() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String taskId = "aTaskId";
    createTask(taskId);
    disableAuthorization();
    taskService.setVariable(taskId, "foo", "bar");
    enableAuthorization();

    createGrantAuthorization(HISTORIC_TASK, taskId, userId, HistoricTaskPermissions.READ);

    // when
    List<HistoricVariableInstance> result = historyService.createHistoricVariableInstanceQuery()
        .list();

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
    disableAuthorization();
    taskService.setVariable(taskId, "foo", "bar");
    enableAuthorization();

    createGrantAuthorization(HISTORIC_TASK, taskId, userId, HistoricTaskPermissions.NONE);

    // when
    List<HistoricVariableInstance> result = historyService.createHistoricVariableInstanceQuery()
        .list();

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
    taskService.setVariable(taskId, "foo", "bar");
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorization(HISTORIC_TASK, taskId, userId, HistoricTaskPermissions.READ);

    // when
    List<HistoricVariableInstance> result = historyService.createHistoricVariableInstanceQuery()
        .list();

    // then
    assertEquals(1, result.size());
  }

  @Test
  public void testCheckNonePermissionOnHistoricTaskAndReadHistoryPermissionOnProcessDefinition() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.setVariable(taskId, "foo", "bar");
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorization(HISTORIC_TASK, taskId, userId, HistoricTaskPermissions.NONE);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY);

    // when
    List<HistoricVariableInstance> result = historyService.createHistoricVariableInstanceQuery()
        .list();

    // then
    assertEquals(1, result.size());
  }

  @Test
  public void testCheckReadPermissionOnHistoricTaskAndNonePermissionOnProcessDefinition() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.setVariable(taskId, "foo", "bar");
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorization(HISTORIC_TASK, taskId, userId, HistoricTaskPermissions.READ);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId,
        ProcessDefinitionPermissions.NONE);

    // when
    List<HistoricVariableInstance> result = historyService.createHistoricVariableInstanceQuery()
        .list();

    // then
    assertEquals(1, result.size());
  }

  @Test
  public void testCheckReadVariablePermissionOnHistoricTask() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);
    processEngineConfiguration.setEnforceSpecificVariablePermission(true);

    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(HISTORIC_TASK, taskId, userId, HistoricTaskPermissions.READ_VARIABLE);

    // when
    List<HistoricVariableInstance> result = historyService.createHistoricVariableInstanceQuery()
        .list();

    // then
    assertEquals(1, result.size());
  }

  @Test
  public void testOnlyReadPermissionOnHistoricTask() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);
    processEngineConfiguration.setEnforceSpecificVariablePermission(true);

    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(HISTORIC_TASK, taskId, userId, HistoricTaskPermissions.READ);

    // when
    List<HistoricVariableInstance> result = historyService.createHistoricVariableInstanceQuery()
        .list();

    // then
    assertEquals(0, result.size());
  }

  @Test
  public void testIgnoreReadVariablePermissionOnHistoricTask() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);
    processEngineConfiguration.setEnforceSpecificVariablePermission(false);

    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    String taskId = selectSingleTask().getId();

    createGrantAuthorization(HISTORIC_TASK, taskId, userId, HistoricTaskPermissions.READ_VARIABLE);

    // when
    List<HistoricVariableInstance> result = historyService.createHistoricVariableInstanceQuery()
        .list();

    // then
    assertEquals(0, result.size());
  }

  @Test
  public void testCheckReadVariablePermissionOnStandaloneHistoricTask() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);
    processEngineConfiguration.setEnforceSpecificVariablePermission(true);

    String taskId = "aTaskId";
    createTask(taskId);
    disableAuthorization();
    taskService.setVariable(taskId, "foo", "bar");
    enableAuthorization();

    createGrantAuthorization(HISTORIC_TASK, taskId, userId, HistoricTaskPermissions.READ_VARIABLE);

    // when
    List<HistoricVariableInstance> result = historyService.createHistoricVariableInstanceQuery()
        .list();

    // then
    assertEquals(1, result.size());

    // clear
    deleteTask(taskId, true);
  }

  @Test
  public void testCheckReadVariablePermissionOnCompletedHistoricTask() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);
    processEngineConfiguration.setEnforceSpecificVariablePermission(true);

    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorization(HISTORIC_TASK, taskId, userId, HistoricTaskPermissions.READ_VARIABLE);

    // when
    List<HistoricVariableInstance> result = historyService.createHistoricVariableInstanceQuery()
        .list();

    // then
    assertEquals(1, result.size());
  }

  @Test
  public void testCheckReadVariablePermissionOnHistoricTaskAndNonePermissionOnProcessDefinition() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);
    processEngineConfiguration.setEnforceSpecificVariablePermission(true);

    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorization(HISTORIC_TASK, taskId, userId, HistoricTaskPermissions.READ_VARIABLE);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId,
        ProcessDefinitionPermissions.NONE);

    // when
    List<HistoricVariableInstance> result = historyService.createHistoricVariableInstanceQuery()
        .list();

    // then
    assertEquals(1, result.size());
  }

  @Test
  public void testCheckNonePermissionOnHistoricTaskAndReadHistoryVariablePermissionOnProcessDefinition() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);
    processEngineConfiguration.setEnforceSpecificVariablePermission(true);

    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorization(HISTORIC_TASK, taskId, userId, HistoricTaskPermissions.NONE);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY_VARIABLE);

    // when
    List<HistoricVariableInstance> result = historyService.createHistoricVariableInstanceQuery()
        .list();

    // then
    assertEquals(1, result.size());
  }

  @Test
  public void testHistoricTaskPermissionsAuthorizationDisabled() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    disableAuthorization();

    taskService.setVariable(taskId, "foo", "bar");

    // when
    List<HistoricVariableInstance> result = historyService.createHistoricVariableInstanceQuery().list();

    // then
    assertEquals(1, result.size());
  }

  @Test
  public void testCheckNonePermissionOnHistoricProcessInstance() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.setVariable(taskId, "foo", "bar");
    enableAuthorization();

    createGrantAuthorization(HISTORIC_PROCESS_INSTANCE, processInstanceId, userId,
        HistoricProcessInstancePermissions.NONE);

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    assertThat(query.list()).isEmpty();
  }

  @Test
  public void testCheckReadPermissionOnHistoricProcessInstance_GlobalVariable() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    disableAuthorization();
    runtimeService.setVariable(processInstanceId, "foo", "bar");
    enableAuthorization();

    createGrantAuthorization(HISTORIC_PROCESS_INSTANCE, processInstanceId, userId,
        HistoricProcessInstancePermissions.READ);

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    assertThat(query.list())
        .extracting("processInstanceId")
        .containsExactly(processInstanceId);
  }

  @Test
  public void testCheckReadPermissionOnHistoricProcessInstance_LocalVariable() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.setVariable(taskId, "foo", "bar");
    enableAuthorization();

    createGrantAuthorization(HISTORIC_PROCESS_INSTANCE, processInstanceId, userId,
        HistoricProcessInstancePermissions.READ);

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

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
    taskService.setVariable(taskId, "foo", "bar");
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorization(HISTORIC_PROCESS_INSTANCE, processInstanceId, userId,
        HistoricProcessInstancePermissions.READ);

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

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
    taskService.setVariable(taskId, "foo", "bar");
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorization(HISTORIC_PROCESS_INSTANCE, processInstanceId, userId,
        HistoricProcessInstancePermissions.NONE);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY);

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

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
    taskService.setVariable(taskId, "foo", "bar");
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorization(HISTORIC_PROCESS_INSTANCE, processInstanceId, userId,
        HistoricProcessInstancePermissions.READ);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId,
        ProcessDefinitionPermissions.NONE);

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    assertThat(query.list())
        .extracting("processInstanceId")
        .containsExactly(processInstanceId);
  }

  protected void setReadHistoryVariableAsDefaultReadPermission() {
    processEngineConfiguration.setEnforceSpecificVariablePermission(true);
  }

}
