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

import static org.junit.Assert.assertEquals;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.UserOperationLogQuery;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class UserOperationLogWithoutUserTest extends PluggableProcessEngineTest {

  protected static final String PROCESS_PATH = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml";
  protected static final String PROCESS_KEY = "oneTaskProcess";

  @Deployment(resources = PROCESS_PATH)
  @Test
  public void testCompleteTask() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.complete(taskId);

    // then
    verifyNoUserOperationLogged();
  }

  @Deployment(resources = PROCESS_PATH)
  @Test
  public void testAssignTask() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.setAssignee(taskId, "demo");

    // then
    verifyNoUserOperationLogged();
  }

  @Deployment(resources = PROCESS_PATH)
  @Test
  public void testClaimTask() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.claim(taskId, "demo");

    // then
    verifyNoUserOperationLogged();
  }

  @Test
  public void testCreateTask() {
    // when
    Task task = taskService.newTask("a-task-id");
    taskService.saveTask(task);

    // then
    verifyNoUserOperationLogged();

    taskService.deleteTask("a-task-id", true);
  }

  @Deployment(resources = PROCESS_PATH)
  @Test
  public void testDelegateTask() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.delegateTask(taskId, "demo");

    // then
    verifyNoUserOperationLogged();
  }

  @Deployment(resources = PROCESS_PATH)
  @Test
  public void testResolveTask() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.resolveTask(taskId);

    // then
    verifyNoUserOperationLogged();
  }

  @Deployment(resources = PROCESS_PATH)
  @Test
  public void testSetOwnerTask() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.setOwner(taskId, "demo");

    // then
    verifyNoUserOperationLogged();
  }

  @Deployment(resources = PROCESS_PATH)
  @Test
  public void testSetPriorityTask() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.setPriority(taskId, 60);

    // then
    verifyNoUserOperationLogged();
  }

  @Deployment(resources = PROCESS_PATH)
  @Test
  public void testUpdateTask() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().singleResult();
    task.setCaseInstanceId("a-case-instance-id");

    // when
    taskService.saveTask(task);

    // then
    verifyNoUserOperationLogged();
  }

  @Deployment(resources = PROCESS_PATH)
  @Test
  public void testActivateProcessInstance() {
    // given
    String id = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();

    // when
    runtimeService.activateProcessInstanceById(id);

    // then
    verifyNoUserOperationLogged();
  }

  @Deployment(resources = PROCESS_PATH)
  @Test
  public void testSuspendProcessInstance() {
    // given
    String id = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();

    // when
    runtimeService.suspendProcessInstanceById(id);

    // then
    verifyNoUserOperationLogged();
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml")
  @Test
  public void testActivateJobDefinition() {
    // given
    runtimeService.startProcessInstanceByKey("oneFailingServiceTaskProcess");
    String id = managementService.createJobDefinitionQuery().singleResult().getId();

    // when
    managementService.activateJobByJobDefinitionId(id);

    // then
    verifyNoUserOperationLogged();
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml")
  @Test
  public void testSuspendJobDefinition() {
    // given
    runtimeService.startProcessInstanceByKey("oneFailingServiceTaskProcess");
    String id = managementService.createJobDefinitionQuery().singleResult().getId();

    // when
    managementService.suspendJobByJobDefinitionId(id);

    // then
    verifyNoUserOperationLogged();
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml")
  @Test
  public void testActivateJob() {
    // given
    runtimeService.startProcessInstanceByKey("oneFailingServiceTaskProcess");
    String id = managementService.createJobQuery().singleResult().getId();

    // when
    managementService.activateJobById(id);

    // then
    verifyNoUserOperationLogged();
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml")
  @Test
  public void testSuspendJob() {
    // given
    runtimeService.startProcessInstanceByKey("oneFailingServiceTaskProcess");
    String id = managementService.createJobQuery().singleResult().getId();

    // when
    managementService.suspendJobById(id);

    // then
    verifyNoUserOperationLogged();
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml")
  @Test
  public void testSetJobRetries() {
    // given
    runtimeService.startProcessInstanceByKey("oneFailingServiceTaskProcess");
    String id = managementService.createJobQuery().singleResult().getId();

    // when
    managementService.setJobRetries(id, 5);

    // then
    verifyNoUserOperationLogged();
  }

  @Deployment(resources = PROCESS_PATH)
  @Test
  public void testActivateProcessDefinition() {
    // when
    repositoryService.activateProcessDefinitionByKey(PROCESS_KEY);

    // then
    verifyNoUserOperationLogged();
  }

  @Deployment(resources = PROCESS_PATH)
  @Test
  public void testSuspendProcessDefinition() {
    // when
    repositoryService.suspendProcessDefinitionByKey(PROCESS_KEY);

    // then
    verifyNoUserOperationLogged();
  }

  @Deployment(resources = PROCESS_PATH)
  @Test
  public void testModifyProcessInstance() {
    // given
    String id = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();

    // when
    runtimeService
      .createProcessInstanceModification(id)
      .cancelAllForActivity("theTask")
      .execute();

    // then
    verifyNoUserOperationLogged();
  }

  @Deployment(resources = PROCESS_PATH)
  @Test
  public void testSetVariable() {
    // given
    String id = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();

    // when
    runtimeService.setVariable(id, "aVariable", "aValue");

    // then
    verifyNoUserOperationLogged();
  }

  @Deployment(resources = PROCESS_PATH)
  @Test
  public void testRemoveVariable() {
    // given
    String id = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();
    runtimeService.setVariable(id, "aVariable", "aValue");

    // when
    runtimeService.removeVariable(id, "aVariable");

    // then
    verifyNoUserOperationLogged();
  }
  
  @Deployment(resources = PROCESS_PATH)
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Test
  public void testDeleteHistoricVariable() {
    // given
    String id = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();
    runtimeService.setVariable(id, "aVariable", "aValue");
    runtimeService.deleteProcessInstance(id, "none");
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().count());
    String historicVariableId = historyService.createHistoricVariableInstanceQuery().singleResult().getId();
    
    // when
    historyService.deleteHistoricVariableInstance(historicVariableId);

    // then
    assertEquals(0, historyService.createHistoricVariableInstanceQuery().count());
    verifyNoUserOperationLogged();
  }
  
  @Deployment(resources = PROCESS_PATH)
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Test
  public void testDeleteAllHistoricVariables() {
    // given
    String id = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();
    runtimeService.setVariable(id, "aVariable", "aValue");
    runtimeService.deleteProcessInstance(id, "none");
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().count());
    
    // when
    historyService.deleteHistoricVariableInstancesByProcessInstanceId(id);

    // then
    assertEquals(0, historyService.createHistoricVariableInstanceQuery().count());
    verifyNoUserOperationLogged();
  }
  
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Test
  public void testQueryDeleteVariableHistoryOperationOnCase() {
    // given
    CaseInstance caseInstance = caseService.createCaseInstanceByKey("oneTaskCase");
    caseService.setVariable(caseInstance.getId(), "myVariable", 1);
    caseService.setVariable(caseInstance.getId(), "myVariable", 2);
    caseService.setVariable(caseInstance.getId(), "myVariable", 3);
    HistoricVariableInstance variableInstance = historyService.createHistoricVariableInstanceQuery().singleResult();
    
    // when
    historyService.deleteHistoricVariableInstance(variableInstance.getId());

    // then
    verifyNoUserOperationLogged();
  }
  
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Test
  public void testQueryDeleteVariableHistoryOperationOnStandaloneTask() {
    // given
    Task task = taskService.newTask();
    taskService.saveTask(task);
    taskService.setVariable(task.getId(), "testVariable", "testValue");
    taskService.setVariable(task.getId(), "testVariable", "testValue2");
    HistoricVariableInstance variableInstance = historyService.createHistoricVariableInstanceQuery().singleResult();
    
    // when
    historyService.deleteHistoricVariableInstance(variableInstance.getId());
    
    // then
    verifyNoUserOperationLogged();
    
    taskService.deleteTask(task.getId(), true);
  }

  protected void verifyNoUserOperationLogged() {
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();
    assertEquals(0, query.count());
  }

}
