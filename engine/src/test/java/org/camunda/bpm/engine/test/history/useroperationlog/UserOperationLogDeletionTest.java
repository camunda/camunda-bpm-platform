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
package org.camunda.bpm.engine.test.history.useroperationlog;

import static org.camunda.bpm.engine.history.UserOperationLogEntry.CATEGORY_OPERATOR;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_DELETE_HISTORY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.history.UserOperationLogQuery;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.ResetDmnConfigUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class UserOperationLogDeletionTest extends AbstractUserOperationLogTest {

  public static final String DECISION_SINGLE_OUTPUT_DMN = "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.decisionSingleOutput.dmn11.xml";
  public static final String DECISION_DEFINITION_KEY = "testDecision";

  protected static final String PROCESS_PATH = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml";
  protected static final String PROCESS_KEY = "oneTaskProcess";

  @Before
  public void setUp() throws Exception {
    DefaultDmnEngineConfiguration dmnEngineConfiguration =
        processEngineConfiguration.getDmnEngineConfiguration();

    ResetDmnConfigUtil.reset(dmnEngineConfiguration)
        .enableFeelLegacyBehavior(true)
        .init();


  }

  @After
  public void tearDown() throws Exception {
    DefaultDmnEngineConfiguration dmnEngineConfiguration =
        processEngineConfiguration.getDmnEngineConfiguration();

    ResetDmnConfigUtil.reset(dmnEngineConfiguration)
        .enableFeelLegacyBehavior(false)
        .init();


  }

  @Deployment(resources = PROCESS_PATH)
  @Test
  public void testDeleteProcessTaskKeepTaskOperationLog() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);

    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.setAssignee(taskId, "demo");
    taskService.complete(taskId);

    UserOperationLogQuery query = historyService
        .createUserOperationLogQuery()
        .taskId(taskId);
    assertEquals(2, query.count());

    // when
    historyService.deleteHistoricTaskInstance(taskId);

    // then
    assertEquals(4, query.count());

    UserOperationLogEntry entry = historyService.createUserOperationLogQuery()
      .operationType(OPERATION_TYPE_DELETE_HISTORY)
      .taskId(taskId)
      .property("nrOfInstances")
      .singleResult();
    assertEquals(CATEGORY_OPERATOR, entry.getCategory());
  }

  @Test
  public void testDeleteStandaloneTaskKeepUserOperationLog() {
    // given
    String taskId = "my-task";
    Task task = taskService.newTask(taskId);
    taskService.saveTask(task);

    taskService.setAssignee(taskId, "demo");
    taskService.complete(taskId);

    UserOperationLogQuery query = historyService
        .createUserOperationLogQuery()
        .taskId(taskId);
    assertEquals(3, query.count());

    // when
    historyService.deleteHistoricTaskInstance(taskId);

    // then
    assertEquals(5, query.count());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testDeleteCaseTaskKeepUserOperationLog() {
    // given
    caseService
      .withCaseDefinitionByKey("oneTaskCase")
      .create();

    caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.setAssignee(taskId, "demo");
    taskService.complete(taskId);

    UserOperationLogQuery query = historyService
        .createUserOperationLogQuery()
        .taskId(taskId);
    assertEquals(2, query.count());

    // when
    historyService.deleteHistoricTaskInstance(taskId);

    // then
    assertEquals(4, query.count());
  }

  @Deployment(resources = PROCESS_PATH)
  @Test
  public void testDeleteProcessInstanceKeepUserOperationLog() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();

    runtimeService.suspendProcessInstanceById(processInstanceId);
    runtimeService.activateProcessInstanceById(processInstanceId);

    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);

    UserOperationLogQuery query = historyService
        .createUserOperationLogQuery()
        .processInstanceId(processInstanceId);
    assertEquals(4, query.count());

    // when
    historyService.deleteHistoricProcessInstance(processInstanceId);

    // then
    assertEquals(4, query.count());

    UserOperationLogEntry entry = historyService.createUserOperationLogQuery()
      .operationType(OPERATION_TYPE_DELETE_HISTORY)
      .property("nrOfInstances")
      .singleResult();

    assertNotNull(entry);
    assertEquals(CATEGORY_OPERATOR, entry.getCategory());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testDeleteCaseInstanceKeepUserOperationLog() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("oneTaskCase")
        .create()
        .getId();

    caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);

    caseService.closeCaseInstance(caseInstanceId);

    UserOperationLogQuery query = historyService
        .createUserOperationLogQuery()
        .caseInstanceId(caseInstanceId)
        .entityType(EntityTypes.TASK);
    assertEquals(1, query.count());

    // when
    historyService.deleteHistoricCaseInstance(caseInstanceId);

    // then
    assertEquals(1, query.count());
    
    UserOperationLogEntry entry = historyService.createUserOperationLogQuery()
        .operationType(OPERATION_TYPE_DELETE_HISTORY)
        .singleResult();

    assertNotNull(entry);
    assertEquals(CATEGORY_OPERATOR, entry.getCategory());
  }

  @Deployment(resources = PROCESS_PATH)
  @Test
  public void testDeleteProcessDefinitionKeepUserOperationLog() {
    // given
    String processDefinitionId = repositoryService
        .createProcessDefinitionQuery()
        .singleResult()
        .getId();

    String processInstanceId = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();

    runtimeService.suspendProcessInstanceById(processInstanceId);

    UserOperationLogQuery query = historyService
        .createUserOperationLogQuery()
        .processInstanceId(processInstanceId);
    assertEquals(2, query.count());

    // when
    repositoryService.deleteProcessDefinition(processDefinitionId, true);

    // then new log is created and old stays
    assertEquals(2, query.count());
  }

  @Test
  public void testDeleteProcessDefinitionsByKey() {
    // given
    for (int i = 0; i < 3; i++) {
      testRule.deploy(repositoryService.createDeployment()
        .addClasspathResource(PROCESS_PATH));
    }

    // when
    repositoryService.deleteProcessDefinitions()
      .byKey(PROCESS_KEY)
      .withoutTenantId()
      .delete();

    // then
    assertUserOperationLogs();
  }

  @Test
  public void testDeleteProcessDefinitionsByKeyCascading() {
    // given
    for (int i = 0; i < 3; i++) {
      testRule.deploy(repositoryService.createDeployment()
                          .addClasspathResource(PROCESS_PATH));
    }

    // when
    repositoryService.deleteProcessDefinitions()
      .byKey(PROCESS_KEY)
      .withoutTenantId()
      .cascade()
      .delete();

    // then
    assertUserOperationLogs();
  }

  @Test
  public void testDeleteProcessDefinitionsByIds() {
    // given
    for (int i = 0; i < 3; i++) {
      testRule.deploy(repositoryService.createDeployment()
                          .addClasspathResource(PROCESS_PATH));
    }

    // when
    repositoryService.deleteProcessDefinitions()
      .byIds(findProcessDefinitionIdsByKey(PROCESS_KEY))
      .delete();

    // then
    assertUserOperationLogs();
  }

  @Test
  public void testDeleteProcessDefinitionsByIdsCascading() {
    // given
    for (int i = 0; i < 3; i++) {
      testRule.deploy(repositoryService.createDeployment()
                          .addClasspathResource(PROCESS_PATH));
    }

    // when
    repositoryService.deleteProcessDefinitions()
      .byIds(findProcessDefinitionIdsByKey(PROCESS_KEY))
      .cascade()
      .delete();

    // then
    assertUserOperationLogs();
  }

  @Deployment(resources = PROCESS_PATH)
  @Test
  public void testDeleteDeploymentKeepUserOperationLog() {
    // given
    String deploymentId = repositoryService
        .createDeploymentQuery()
        .singleResult()
        .getId();

    String processDefinitionId = repositoryService
        .createProcessDefinitionQuery()
        .singleResult()
        .getId();

    repositoryService.suspendProcessDefinitionById(processDefinitionId);

    UserOperationLogQuery query = historyService
        .createUserOperationLogQuery()
        .processDefinitionId(processDefinitionId);
    assertEquals(2, query.count());

    // when
    repositoryService.deleteDeployment(deploymentId, true);

    // then
    assertEquals(2, query.count());
  }

  @Deployment(resources = { DECISION_SINGLE_OUTPUT_DMN })
  @Test
  public void testDeleteDecisionInstanceByDecisionDefinition() {

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("input1", "test");
    decisionService.evaluateDecisionTableByKey(DECISION_DEFINITION_KEY, variables);

    String decisionDefinitionId = repositoryService.createDecisionDefinitionQuery().decisionDefinitionKey(DECISION_DEFINITION_KEY).singleResult().getId();
    historyService.deleteHistoricDecisionInstanceByDefinitionId(decisionDefinitionId);

    List<UserOperationLogEntry> userOperationLogEntries = historyService.createUserOperationLogQuery()
      .operationType(OPERATION_TYPE_DELETE_HISTORY)
      .property("nrOfInstances")
      .list();

    assertEquals(1, userOperationLogEntries.size());

    UserOperationLogEntry entry = userOperationLogEntries.get(0);
    assertEquals("1", entry.getNewValue());
    assertEquals(CATEGORY_OPERATOR, entry.getCategory());
  }

  @Deployment(resources = { DECISION_SINGLE_OUTPUT_DMN })
  @Test
  public void testDeleteDecisionInstanceById() {

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("input1", "test");
    decisionService.evaluateDecisionTableByKey(DECISION_DEFINITION_KEY, variables);

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().singleResult();
    historyService.deleteHistoricDecisionInstanceByInstanceId(historicDecisionInstance.getId());

    List<UserOperationLogEntry> userOperationLogEntries = historyService.createUserOperationLogQuery()
      .operationType(OPERATION_TYPE_DELETE_HISTORY)
      .property("nrOfInstances")
      .list();

    assertEquals(1, userOperationLogEntries.size());

    UserOperationLogEntry entry = userOperationLogEntries.get(0);
    assertEquals("1", entry.getNewValue());
    assertEquals(CATEGORY_OPERATOR, entry.getCategory());
  }

  public void assertUserOperationLogs() {
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();

    UserOperationLogQuery userOperationLogQuery = historyService
      .createUserOperationLogQuery()
      .operationType(UserOperationLogEntry.OPERATION_TYPE_DELETE);

    List<UserOperationLogEntry> userOperationLogs = userOperationLogQuery.list();

    assertEquals(3, userOperationLogs.size());

    for (ProcessDefinition processDefinition: processDefinitions) {
      UserOperationLogEntry userOperationLogEntry = userOperationLogQuery
        .deploymentId(processDefinition.getDeploymentId()).singleResult();

      assertEquals(EntityTypes.PROCESS_DEFINITION, userOperationLogEntry.getEntityType());
      assertEquals(processDefinition.getId(), userOperationLogEntry.getProcessDefinitionId());
      assertEquals(processDefinition.getKey(), userOperationLogEntry.getProcessDefinitionKey());
      assertEquals(processDefinition.getDeploymentId(), userOperationLogEntry.getDeploymentId());

      assertEquals(UserOperationLogEntry.OPERATION_TYPE_DELETE, userOperationLogEntry.getOperationType());

      assertEquals("cascade", userOperationLogEntry.getProperty());
      assertFalse(Boolean.valueOf(userOperationLogEntry.getOrgValue()));
      assertTrue(Boolean.valueOf(userOperationLogEntry.getNewValue()));

      assertEquals(USER_ID, userOperationLogEntry.getUserId());
      
      assertEquals(UserOperationLogEntry.CATEGORY_TASK_WORKER, userOperationLogEntry.getCategory());
      
      assertNull(userOperationLogEntry.getJobDefinitionId());
      assertNull(userOperationLogEntry.getProcessInstanceId());
      assertNull(userOperationLogEntry.getCaseInstanceId());
      assertNull(userOperationLogEntry.getCaseDefinitionId());
    }

    assertEquals(6, historyService.createUserOperationLogQuery().count());
  }

  private String[] findProcessDefinitionIdsByKey(String processDefinitionKey) {
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
      .processDefinitionKey(processDefinitionKey).list();
    List<String> processDefinitionIds = new ArrayList<String>();
    for (ProcessDefinition processDefinition: processDefinitions) {
      processDefinitionIds.add(processDefinition.getId());
    }

    return processDefinitionIds.toArray(new String[0]);
  }

}
