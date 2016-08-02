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
package org.camunda.bpm.engine.test.history.useroperationlog;

import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_ASSIGN;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_CLAIM;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_COMPLETE;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_DELEGATE;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_RESOLVE;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_SET_OWNER;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_SET_PRIORITY;
import static org.camunda.bpm.engine.impl.persistence.entity.TaskEntity.ASSIGNEE;
import static org.camunda.bpm.engine.impl.persistence.entity.TaskEntity.DELEGATION;
import static org.camunda.bpm.engine.impl.persistence.entity.TaskEntity.DELETE;
import static org.camunda.bpm.engine.impl.persistence.entity.TaskEntity.OWNER;
import static org.camunda.bpm.engine.impl.persistence.entity.TaskEntity.PRIORITY;

import java.util.Date;
import java.util.HashMap;

import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.history.UserOperationLogQuery;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Danny Gräf
 */
public class UserOperationLogTaskTest extends AbstractUserOperationLogTest {

  protected ProcessDefinition processDefinition;
  protected ProcessInstance process;
  protected Task task;

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testCreateAndCompleteTask() {
    startTestProcess();

    // expect: no entry for the task creation by process engine
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();
    assertEquals(0, query.count());

    completeTestProcess();

    // expect: one entry for the task completion
    query = queryOperationDetails(OPERATION_TYPE_COMPLETE);
    assertEquals(1, query.count());
    UserOperationLogEntry complete = query.singleResult();
    assertEquals(DELETE, complete.getProperty());
    assertTrue(Boolean.parseBoolean(complete.getNewValue()));
  }

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

    completeTestProcess();
  }

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

    completeTestProcess();
  }

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
  }

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

    completeTestProcess();
  }

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

    completeTestProcess();
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testResolveTask() {
    startTestProcess();

    // then: resolve the task
    taskService.resolveTask(task.getId());

    // expect: one entry for the resolving
    UserOperationLogQuery query = queryOperationDetails(OPERATION_TYPE_RESOLVE);
    assertEquals(1, query.count());

    // assert: details
    assertEquals(DelegationState.RESOLVED.toString(), query.singleResult().getNewValue());

    completeTestProcess();
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testSubmitTaskForm_Complete() {
    startTestProcess();

    formService.submitTaskForm(task.getId(), new HashMap<String, Object>());

    // expect: two entries for the resolving (delegation and assignee changed)
    UserOperationLogQuery query = queryOperationDetails(OPERATION_TYPE_COMPLETE);
    assertEquals(1, query.count());

    // assert: delete
    assertFalse(Boolean.parseBoolean(query.property("delete").singleResult().getOrgValue()));
    assertTrue(Boolean.parseBoolean(query.property("delete").singleResult().getNewValue()));

    assertProcessEnded(process.getId());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testSubmitTaskForm_Resolve() {
    startTestProcess();

    taskService.delegateTask(task.getId(), "demo");

    formService.submitTaskForm(task.getId(), new HashMap<String, Object>());

    // expect: two entries for the resolving (delegation and assignee changed)
    UserOperationLogQuery query = queryOperationDetails(OPERATION_TYPE_RESOLVE);
    assertEquals(2, query.count());

    // assert: delegation
    assertEquals(DelegationState.PENDING.toString(), query.property("delegation").singleResult().getOrgValue());
    assertEquals(DelegationState.RESOLVED.toString(), query.property("delegation").singleResult().getNewValue());

    // assert: assignee
    assertEquals("demo", query.property("assignee").singleResult().getOrgValue());
    assertEquals(null, query.property("assignee").singleResult().getNewValue());

    completeTestProcess();
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testCompleteCaseExecution() {
    // given
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    String caseInstanceId = caseService
        .withCaseDefinition(caseDefinitionId)
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

    assertEquals(caseDefinitionId, entry.getCaseDefinitionId());
    assertEquals(caseInstanceId, entry.getCaseInstanceId());
    assertEquals(humanTaskId, entry.getCaseExecutionId());
    assertEquals(deploymentId, entry.getDeploymentId());

    assertFalse(Boolean.valueOf(entry.getOrgValue()));
    assertTrue(Boolean.valueOf(entry.getNewValue()));
    assertEquals(DELETE, entry.getProperty());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testKeepOpLogEntriesOnUndeployment() {
    // given
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
    assertEquals(3, query.count());
    assertEquals(1, query.operationType(UserOperationLogEntry.OPERATION_TYPE_SUSPEND).count());
    assertEquals(1, query.operationType(UserOperationLogEntry.OPERATION_TYPE_RESOLVE).count());
    assertEquals(1, query.operationType(UserOperationLogEntry.OPERATION_TYPE_DELETE).count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testDeleteOpLogEntry() {
    // given
    startTestProcess();

    // an op log instance is created
    taskService.resolveTask(task.getId());
    UserOperationLogEntry opLogEntry = historyService.createUserOperationLogQuery().singleResult();

    // when the op log instance is deleted
    historyService.deleteUserOperationLogEntry(opLogEntry.getId());

    // then it should be removed from the database
    assertEquals(0, historyService.createUserOperationLogQuery().count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
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
  public void testDeleteOpLogNonExstingEntry() {
    // given
    startTestProcess();

    // an op log instance is created
    taskService.resolveTask(task.getId());

    // when a non-existing id is used
    historyService.deleteUserOperationLogEntry("a non existing id");

    // then no op log entry should have been deleted
    assertEquals(1, historyService.createUserOperationLogQuery().count());
  }

  @Deployment
  public void testOnlyTaskCompletionIsLogged() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.complete(taskId);

    // then
    assertTrue((Boolean) runtimeService.getVariable(processInstanceId, "taskListenerCalled"));
    assertTrue((Boolean) runtimeService.getVariable(processInstanceId, "serviceTaskCalled"));

    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    assertEquals(1, query.count());

    UserOperationLogEntry log = query.singleResult();
    assertEquals("process", log.getProcessDefinitionKey());
    assertEquals(processInstanceId, log.getProcessInstanceId());
    assertEquals(deploymentId, log.getDeploymentId());
    assertEquals(taskId, log.getTaskId());
    assertEquals(UserOperationLogEntry.OPERATION_TYPE_COMPLETE, log.getOperationType());
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
    assertProcessEnded(process.getId());
  }

}
