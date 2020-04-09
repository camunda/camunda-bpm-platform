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

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_ASSIGN;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_CLAIM;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_COMPLETE;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_DELEGATE;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_DELETE;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_RESOLVE;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_SET_OWNER;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_SET_PRIORITY;
import static org.camunda.bpm.engine.impl.persistence.entity.TaskEntity.ASSIGNEE;
import static org.camunda.bpm.engine.impl.persistence.entity.TaskEntity.DELEGATION;
import static org.camunda.bpm.engine.impl.persistence.entity.TaskEntity.DELETE;
import static org.camunda.bpm.engine.impl.persistence.entity.TaskEntity.OWNER;
import static org.camunda.bpm.engine.impl.persistence.entity.TaskEntity.PRIORITY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.HashMap;

import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.history.UserOperationLogQuery;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;

/**
 * @author Danny Gräf
 */
public class UserOperationLogTaskTest extends AbstractUserOperationLogTest {

  protected ProcessDefinition processDefinition;
  protected ProcessInstance process;
  protected Task task;

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testCreateAndCompleteTask() {
    startTestProcess();

    // expect: one entry for process instance creation,
    //         no entry for the task creation by process engine
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();
    assertEquals(1, query.count());

    completeTestProcess();

    // expect: one entry for the task completion
    query = queryOperationDetails(OPERATION_TYPE_COMPLETE);
    assertEquals(1, query.count());
    UserOperationLogEntry complete = query.singleResult();
    assertEquals(DELETE, complete.getProperty());
    assertTrue(Boolean.parseBoolean(complete.getNewValue()));
    assertEquals(UserOperationLogEntry.CATEGORY_TASK_WORKER, complete.getCategory());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testAssignTask() {
    startTestProcess();

    // then: assign the task
    taskService.setAssignee(task.getId(), "icke");

    // expect: one entry for the task assignment
    UserOperationLogQuery query = queryOperationDetails(OPERATION_TYPE_ASSIGN);
    assertEquals(1, query.count());

    // assert: details
    UserOperationLogEntry assign = query.singleResult();
    assertEquals(ASSIGNEE, assign.getProperty());
    assertEquals("icke", assign.getNewValue());
    assertEquals(UserOperationLogEntry.CATEGORY_TASK_WORKER, assign.getCategory());

    completeTestProcess();
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testChangeTaskOwner() {
    startTestProcess();

    // then: change the task owner
    taskService.setOwner(task.getId(), "icke");

    // expect: one entry for the owner change
    UserOperationLogQuery query = queryOperationDetails(OPERATION_TYPE_SET_OWNER);
    assertEquals(1, query.count());

    // assert: details
    UserOperationLogEntry change = query.singleResult();
    assertEquals(OWNER, change.getProperty());
    assertEquals("icke", change.getNewValue());
    assertEquals(UserOperationLogEntry.CATEGORY_TASK_WORKER, change.getCategory());

    completeTestProcess();
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testSetPriority() {
    startTestProcess();

    // then: set the priority of the task to 10
    taskService.setPriority(task.getId(), 10);

    // expect: one entry for the priority update
    UserOperationLogQuery query = queryOperationDetails(OPERATION_TYPE_SET_PRIORITY);
    assertEquals(1, query.count());

    // assert: correct priority set
    UserOperationLogEntry userOperationLogEntry = query.singleResult();
    assertEquals(PRIORITY, userOperationLogEntry.getProperty());
    // note: 50 is the default task priority
    assertEquals(50, Integer.parseInt(userOperationLogEntry.getOrgValue()));
    assertEquals(10, Integer.parseInt(userOperationLogEntry.getNewValue()));
    // assert: correct category set
    assertEquals(UserOperationLogEntry.CATEGORY_TASK_WORKER, userOperationLogEntry.getCategory());

    // move clock by 5 minutes
    Date date = DateTimeUtil.now().plusMinutes(5).toDate();
    ClockUtil.setCurrentTime(date);

    // then: set priority again
    taskService.setPriority(task.getId(), 75);

    // expect: one entry for the priority update
    query = queryOperationDetails(OPERATION_TYPE_SET_PRIORITY);
    assertEquals(2, query.count());

    // assert: correct priority set
    userOperationLogEntry = query.orderByTimestamp().asc().list().get(1);
    assertEquals(PRIORITY, userOperationLogEntry.getProperty());
    assertEquals(10, Integer.parseInt(userOperationLogEntry.getOrgValue()));
    assertEquals(75, Integer.parseInt(userOperationLogEntry.getNewValue()));
    assertEquals(UserOperationLogEntry.CATEGORY_TASK_WORKER, userOperationLogEntry.getCategory());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testClaimTask() {
    startTestProcess();

    // then: claim a new the task
    taskService.claim(task.getId(), "icke");

    // expect: one entry for the claim
    UserOperationLogQuery query = queryOperationDetails(OPERATION_TYPE_CLAIM);
    assertEquals(1, query.count());

    // assert: details
    UserOperationLogEntry claim = query.singleResult();
    assertEquals(ASSIGNEE, claim.getProperty());
    assertEquals("icke", claim.getNewValue());
    assertEquals(UserOperationLogEntry.CATEGORY_TASK_WORKER, claim.getCategory());

    completeTestProcess();
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testDelegateTask() {
    startTestProcess();

    // then: delegate the assigned task
    taskService.claim(task.getId(), "icke");
    taskService.delegateTask(task.getId(), "er");

    // expect: three entries for the delegation
    UserOperationLogQuery query = queryOperationDetails(OPERATION_TYPE_DELEGATE);
    assertEquals(3, query.count());

    // assert: details
    assertEquals("icke", queryOperationDetails(OPERATION_TYPE_DELEGATE, OWNER).singleResult().getNewValue());
    assertEquals("er", queryOperationDetails(OPERATION_TYPE_DELEGATE, ASSIGNEE).singleResult().getNewValue());
    assertEquals(DelegationState.PENDING.toString(), queryOperationDetails(OPERATION_TYPE_DELEGATE, DELEGATION).singleResult().getNewValue());
    assertEquals(UserOperationLogEntry.CATEGORY_TASK_WORKER, queryOperationDetails(OPERATION_TYPE_DELEGATE, DELEGATION).singleResult().getCategory());

    completeTestProcess();
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testResolveTask() {
    startTestProcess();

    // then: resolve the task
    taskService.resolveTask(task.getId());

    // expect: one entry for the resolving
    UserOperationLogQuery query = queryOperationDetails(OPERATION_TYPE_RESOLVE);
    assertEquals(1, query.count());

    // assert: details
    UserOperationLogEntry log = query.singleResult();
    assertEquals(DelegationState.RESOLVED.toString(), log.getNewValue());
    assertEquals(UserOperationLogEntry.CATEGORY_TASK_WORKER, log.getCategory());

    completeTestProcess();
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testSubmitTaskForm_Complete() {
    startTestProcess();

    formService.submitTaskForm(task.getId(), new HashMap<String, Object>());

    // expect: one entry for the completion
    UserOperationLogQuery query = queryOperationDetails(OPERATION_TYPE_COMPLETE);
    assertEquals(1, query.count());

    // assert: delete
    UserOperationLogEntry log = query.property("delete").singleResult();
    assertFalse(Boolean.parseBoolean(log.getOrgValue()));
    assertTrue(Boolean.parseBoolean(log.getNewValue()));
    assertEquals(UserOperationLogEntry.CATEGORY_TASK_WORKER, log.getCategory());

    testRule.assertProcessEnded(process.getId());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testSubmitTaskForm_Resolve() {
    startTestProcess();

    taskService.delegateTask(task.getId(), "demo");

    formService.submitTaskForm(task.getId(), new HashMap<String, Object>());

    // expect: two entries for the resolving (delegation and assignee changed)
    UserOperationLogQuery query = queryOperationDetails(OPERATION_TYPE_RESOLVE);
    assertEquals(2, query.count());

    // assert: delegation
    UserOperationLogEntry log = query.property("delegation").singleResult();
    assertEquals(DelegationState.PENDING.toString(), log.getOrgValue());
    assertEquals(DelegationState.RESOLVED.toString(), log.getNewValue());
    assertEquals(UserOperationLogEntry.CATEGORY_TASK_WORKER, log.getCategory());

    // assert: assignee
    log = query.property("assignee").singleResult();
    assertEquals("demo", log.getOrgValue());
    assertEquals(null, log.getNewValue());
    assertEquals(UserOperationLogEntry.CATEGORY_TASK_WORKER, log.getCategory());

    completeTestProcess();
  }

  @Test
  public void testDeleteTask() {
    // given
    Task task = taskService.newTask();
    taskService.saveTask(task);

    // when
    taskService.deleteTask(task.getId());

    // then
    UserOperationLogQuery query = queryOperationDetails(OPERATION_TYPE_DELETE);
    assertEquals(1, query.count());

    // assert: details
    UserOperationLogEntry log = query.singleResult();
    assertThat(log.getProperty()).isEqualTo("delete");
    assertThat(log.getOrgValue()).isEqualTo("false");
    assertThat(log.getNewValue()).isEqualTo("true");
    assertThat(log.getCategory()).isEqualTo(UserOperationLogEntry.CATEGORY_TASK_WORKER);

    historyService.deleteHistoricTaskInstance(task.getId());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testCompleteTask() {
    // given
    startTestProcess();

    // when
    taskService.complete(task.getId());

    // then
    UserOperationLogQuery query = queryOperationDetails(OPERATION_TYPE_COMPLETE);
    assertEquals(1, query.count());

    // assert: details
    UserOperationLogEntry log = query.singleResult();
    assertThat(log.getProperty()).isEqualTo("delete");
    assertThat(log.getOrgValue()).isEqualTo("false");
    assertThat(log.getNewValue()).isEqualTo("true");
    assertThat(log.getCategory()).isEqualTo(UserOperationLogEntry.CATEGORY_TASK_WORKER);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testCompleteCaseExecution() {
    // given
    CaseDefinition caseDefinition = repositoryService
        .createCaseDefinitionQuery()
        .singleResult();

    String caseInstanceId = caseService
        .withCaseDefinition(caseDefinition.getId())
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    caseService
      .withCaseExecution(humanTaskId)
      .complete();

    // then
    UserOperationLogQuery query = queryOperationDetails(OPERATION_TYPE_COMPLETE);

    assertEquals(1, query.count());

    UserOperationLogEntry entry = query.singleResult();
    assertNotNull(entry);

    assertEquals(caseDefinition.getId(), entry.getCaseDefinitionId());
    assertEquals(caseInstanceId, entry.getCaseInstanceId());
    assertEquals(humanTaskId, entry.getCaseExecutionId());
    assertEquals(caseDefinition.getDeploymentId(), entry.getDeploymentId());

    assertFalse(Boolean.valueOf(entry.getOrgValue()));
    assertTrue(Boolean.valueOf(entry.getNewValue()));
    assertEquals(DELETE, entry.getProperty());
    assertEquals(UserOperationLogEntry.CATEGORY_TASK_WORKER, entry.getCategory());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testKeepOpLogEntriesOnUndeployment() {
    // given
    String deploymentId = repositoryService.createDeploymentQuery().singleResult().getId();
    startTestProcess();
    // an op log entry directly related to the process instance is created
    taskService.resolveTask(task.getId());

    // and an op log entry with indirect reference to the process instance is created
    runtimeService.suspendProcessInstanceByProcessDefinitionId(processDefinition.getId());

    // when
    // the deployment is deleted with cascade
    repositoryService.deleteDeployment(deploymentId, true);

    // then
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();
    assertEquals(4, query.count());
    assertEquals(1, query.operationType(UserOperationLogEntry.OPERATION_TYPE_CREATE).count());
    assertEquals(1, query.operationType(UserOperationLogEntry.OPERATION_TYPE_SUSPEND).count());
    assertEquals(1, query.operationType(UserOperationLogEntry.OPERATION_TYPE_RESOLVE).count());
    assertEquals(1, query.operationType(UserOperationLogEntry.OPERATION_TYPE_DELETE).count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteOpLogEntry() {
    // given
    startTestProcess();

    // an op log instance is created
    taskService.resolveTask(task.getId());
    UserOperationLogEntry opLogEntry = historyService
            .createUserOperationLogQuery()
            .entityType(EntityTypes.TASK)
            .singleResult();

    // when the op log instance is deleted
    historyService.deleteUserOperationLogEntry(opLogEntry.getId());

    // then it should be removed from the database
    assertEquals(0, historyService
            .createUserOperationLogQuery()
            .entityType(EntityTypes.TASK)
            .count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteOpLogEntryWithNullArgument() {
    // given
    startTestProcess();

    // an op log instance is created
    taskService.resolveTask(task.getId());

    // when null is used as deletion parameter
    try {
      historyService.deleteUserOperationLogEntry(null);
      fail("exeception expected");
    } catch (NotValidException e) {
      // then there should be an exception that signals an illegal input
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteOpLogNonExstingEntry() {
    // given
    startTestProcess();

    // an op log instance is created
    taskService.resolveTask(task.getId());
    assertEquals(2, historyService.createUserOperationLogQuery().count());

    // when a non-existing id is used
    historyService.deleteUserOperationLogEntry("a non existing id");

    // then no op log entry should have been deleted (process instance creation+ resolve task)
    assertEquals(2, historyService.createUserOperationLogQuery().count());
  }

  @Deployment
  @Test
  public void testOnlyTaskCompletionIsLogged() {
    // given
    String deploymentId = repositoryService.createDeploymentQuery().singleResult().getId();
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.complete(taskId);

    // then
    assertTrue((Boolean) runtimeService.getVariable(processInstanceId, "taskListenerCalled"));
    assertTrue((Boolean) runtimeService.getVariable(processInstanceId, "serviceTaskCalled"));

    // Filter only task entities, as the process start is also recorded
    UserOperationLogQuery query = historyService
            .createUserOperationLogQuery()
            .entityType(EntityTypes.TASK);

    assertEquals(1, query.count());

    UserOperationLogEntry log = query.singleResult();
    assertEquals("process", log.getProcessDefinitionKey());
    assertEquals(processInstanceId, log.getProcessInstanceId());
    assertEquals(deploymentId, log.getDeploymentId());
    assertEquals(taskId, log.getTaskId());
    assertEquals(UserOperationLogEntry.OPERATION_TYPE_COMPLETE, log.getOperationType());
    assertEquals(UserOperationLogEntry.CATEGORY_TASK_WORKER, log.getCategory());
  }

  protected void startTestProcess() {
    processDefinition = repositoryService
        .createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess").singleResult();

    process = runtimeService.startProcessInstanceById(processDefinition.getId());
    task = taskService.createTaskQuery().singleResult();
  }

  protected UserOperationLogQuery queryOperationDetails(String type) {
    return historyService.createUserOperationLogQuery().operationType(type);
  }

  protected UserOperationLogQuery queryOperationDetails(String type, String property) {
    return historyService.createUserOperationLogQuery().operationType(type).property(property);
  }

  protected void completeTestProcess() {
    taskService.complete(task.getId());
    testRule.assertProcessEnded(process.getId());
  }

}
