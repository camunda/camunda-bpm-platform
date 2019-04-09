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

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.DELETE_HISTORY;
import static org.camunda.bpm.engine.authorization.Permissions.READ_HISTORY;
import static org.camunda.bpm.engine.authorization.ProcessDefinitionPermissions.READ_HISTORY_VARIABLE;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;

import java.util.List;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.authorization.AuthorizationTest;

/**
 * @author Roman Smirnov
 *
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoricVariableInstanceAuthorizationTest extends AuthorizationTest {

  protected static final String PROCESS_KEY = "oneTaskProcess";
  protected static final String MESSAGE_START_PROCESS_KEY = "messageStartProcess";
  protected static final String CASE_KEY = "oneTaskCase";

  protected String deploymentId;
  protected boolean ensureSpecificVariablePermission;

  @Override
  public void setUp() throws Exception {
    deploymentId = createDeployment(null,
        "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/api/authorization/messageStartEventProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/api/authorization/oneTaskCase.cmmn").getId();

    ensureSpecificVariablePermission = processEngineConfiguration.isEnforceSpecificVariablePermission();

    super.setUp();
  }

  @Override
  public void tearDown() {
    super.tearDown();
    deleteDeployment(deploymentId);

    processEngineConfiguration.setEnforceSpecificVariablePermission(ensureSpecificVariablePermission);
  }

  // historic variable instance query (standalone task) /////////////////////////////////////////////

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

  public void testSimpleQueryWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY, getVariables());

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    verifyQueryResults(query, 0);
  }

  public void testSimpleQueryWithReadHistoryPermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY);

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    verifyQueryResults(query, 1);
  }

  public void testSimpleQueryWithReadHistoryPermissionOnAnyProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    verifyQueryResults(query, 1);
  }

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

  public void testSimpleQueryWithReadHistoryVariablePermissionOnAnyProcessDefinition() {
    setReadHistoryVariableAsDefaultReadPermission();

    startProcessInstanceByKey(PROCESS_KEY, getVariables());
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY_VARIABLE);

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    verifyQueryResults(query, 1);
  }

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

  public void testQueryWithoutAuthorization() {
    startMultipleProcessInstances();

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    verifyQueryResults(query, 0);
  }

  public void testQueryWithReadHistoryPermissionOnProcessDefinition() {
    startMultipleProcessInstances();

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY);

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    verifyQueryResults(query, 3);
  }

  public void testQueryWithReadHistoryPermissionOnAnyProcessDefinition() {
    // given
    startMultipleProcessInstances();

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    verifyQueryResults(query, 7);
  }

  public void testQueryWithReadHistoryVariablePermissionOnProcessDefinition() {
    setReadHistoryVariableAsDefaultReadPermission();

    startMultipleProcessInstances();

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_HISTORY_VARIABLE);

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    verifyQueryResults(query, 3);
  }

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

  public void testQueryAfterCaseVariables() {
    // given
    createCaseInstanceByKey(CASE_KEY, getVariables());

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    verifyQueryResults(query, 1);
  }

  // historic variable instance query (mixed variables) ////////////////////////////////////

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
      assertTextPresent(userId, message);
      assertTextPresent(DELETE_HISTORY.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }
  
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
      assertTextPresent(userId, message);
      assertTextPresent(DELETE_HISTORY.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

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

  protected void setReadHistoryVariableAsDefaultReadPermission() {
    processEngineConfiguration.setEnforceSpecificVariablePermission(true);
  }

}
