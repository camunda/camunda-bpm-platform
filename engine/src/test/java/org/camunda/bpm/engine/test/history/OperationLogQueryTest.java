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
package org.camunda.bpm.engine.test.history;

import static org.camunda.bpm.engine.history.UserOperationLogEntry.ENTITY_TYPE_ATTACHMENT;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.ENTITY_TYPE_IDENTITY_LINK;
import static org.camunda.bpm.engine.EntityTypes.PROCESS_INSTANCE;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.ENTITY_TYPE_TASK;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_ACTIVATE;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_ADD_ATTACHMENT;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_ADD_GROUP_LINK;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_ADD_USER_LINK;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_CREATE;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_DELETE;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_DELETE_ATTACHMENT;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_DELETE_GROUP_LINK;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_DELETE_USER_LINK;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_SET_PRIORITY;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_SUSPEND;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_UPDATE;
import static org.camunda.bpm.engine.impl.persistence.entity.TaskEntity.ASSIGNEE;
import static org.camunda.bpm.engine.impl.persistence.entity.TaskEntity.OWNER;

import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.history.UserOperationLogQuery;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Attachment;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Danny Gräf
 */
public class OperationLogQueryTest extends PluggableProcessEngineTestCase {

  private ProcessInstance process;
  private Task userTask;
  private Execution execution;
  private String processTaskId;

  // normalize timestamps for databases which do not provide millisecond presision.
  private Date today = new Date((ClockUtil.getCurrentTime().getTime() / 1000) * 1000);
  private Date tomorrow = new Date(((ClockUtil.getCurrentTime().getTime() + 86400000) / 1000) * 1000);
  private Date yesterday = new Date(((ClockUtil.getCurrentTime().getTime() - 86400000) / 1000) * 1000);

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testQuery() {
    createLogEntries();

    // expect: all entries can be fetched
    assertEquals(17, query().count());

    // entity type
    assertEquals(11, query().entityType(ENTITY_TYPE_TASK).count());
    assertEquals(4, query().entityType(ENTITY_TYPE_IDENTITY_LINK).count());
    assertEquals(2, query().entityType(ENTITY_TYPE_ATTACHMENT).count());
    assertEquals(0, query().entityType("unknown entity type").count());

    // operation type
    assertEquals(1, query().operationType(OPERATION_TYPE_CREATE).count());
    assertEquals(1, query().operationType(OPERATION_TYPE_SET_PRIORITY).count());
    assertEquals(4, query().operationType(OPERATION_TYPE_UPDATE).count());
    assertEquals(1, query().operationType(OPERATION_TYPE_ADD_USER_LINK).count());
    assertEquals(1, query().operationType(OPERATION_TYPE_DELETE_USER_LINK).count());
    assertEquals(1, query().operationType(OPERATION_TYPE_ADD_GROUP_LINK).count());
    assertEquals(1, query().operationType(OPERATION_TYPE_DELETE_GROUP_LINK).count());
    assertEquals(1, query().operationType(OPERATION_TYPE_ADD_ATTACHMENT).count());
    assertEquals(1, query().operationType(OPERATION_TYPE_DELETE_ATTACHMENT).count());

    // process and execution reference
    assertEquals(11, query().processDefinitionId(process.getProcessDefinitionId()).count());
    assertEquals(11, query().processInstanceId(process.getId()).count());
    assertEquals(11, query().executionId(execution.getId()).count());

    // task reference
    assertEquals(11, query().taskId(processTaskId).count());
    assertEquals(6, query().taskId(userTask.getId()).count());

    // user reference
    assertEquals(11, query().userId("icke").count()); // not includes the create operation called by the process
    assertEquals(6, query().userId("er").count());

    // operation ID
    UserOperationLogQuery updates = query().operationType(OPERATION_TYPE_UPDATE);
    String updateOperationId = updates.list().get(0).getOperationId();
    assertEquals(updates.count(), query().operationId(updateOperationId).count());

    // changed properties
    assertEquals(3, query().property(ASSIGNEE).count());
    assertEquals(2, query().property(OWNER).count());

    // ascending order results by time
    List<UserOperationLogEntry> ascLog = query().orderByTimestamp().asc().list();
    for (int i = 0; i < 4; i++) {
      assertTrue(yesterday.getTime()<=ascLog.get(i).getTimestamp().getTime());
    }
    for (int i = 4; i < 12; i++) {
      assertTrue(today.getTime()<=ascLog.get(i).getTimestamp().getTime());
    }
    for (int i = 12; i < 16; i++) {
      assertTrue(tomorrow.getTime()<=ascLog.get(i).getTimestamp().getTime());
    }

    // descending order results by time
    List<UserOperationLogEntry> descLog = query().orderByTimestamp().desc().list();
    for (int i = 0; i < 4; i++) {
      assertTrue(tomorrow.getTime()<=descLog.get(i).getTimestamp().getTime());
    }
    for (int i = 4; i < 11; i++) {
      assertTrue(today.getTime()<=descLog.get(i).getTimestamp().getTime());
    }
    for (int i = 11; i < 15; i++) {
      assertTrue(yesterday.getTime()<=descLog.get(i).getTimestamp().getTime());
    }

    // filter by time, created yesterday
    assertEquals(4, query().beforeTimestamp(today).count());
    // filter by time, created today and before
    assertEquals(12, query().beforeTimestamp(tomorrow).count());
    // filter by time, created today and later
    assertEquals(13, query().afterTimestamp(yesterday).count());
    // filter by time, created tomorrow
    assertEquals(5, query().afterTimestamp(today).count());

    // remove log entries of manually created tasks
    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Object>() {
      @Override
      public Object execute(CommandContext commandContext) {
        commandContext.getHistoricTaskInstanceManager().deleteHistoricTaskInstanceById(userTask.getId());
        return null;
      }
    });
  }

  private UserOperationLogQuery query() {
    return historyService.createUserOperationLogQuery();
  }

  /**
   * start process and operate on userTask to create some log entries for the query tests
   */
  private void createLogEntries() {
    ClockUtil.setCurrentTime(yesterday);

    // create a process with a userTask and work with it
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    execution = processEngine.getRuntimeService().createExecutionQuery().processInstanceId(process.getId()).singleResult();
    processTaskId = taskService.createTaskQuery().singleResult().getId();

    // user "icke" works on the process userTask
    identityService.setAuthenticatedUserId("icke");

    // create and remove some links
    taskService.addCandidateUser(processTaskId, "er");
    taskService.deleteCandidateUser(processTaskId, "er");
    taskService.addCandidateGroup(processTaskId, "wir");
    taskService.deleteCandidateGroup(processTaskId, "wir");

    // assign and reassign the userTask
    ClockUtil.setCurrentTime(today);
    taskService.setOwner(processTaskId, "icke");
    taskService.claim(processTaskId, "icke");
    taskService.setAssignee(processTaskId, "er");

    // change priority of task
    taskService.setPriority(processTaskId, 10);

    // add and delete an attachment
    Attachment attachment = taskService.createAttachment("image/ico", processTaskId, process.getId(), "favicon.ico", "favicon", "http://camunda.com/favicon.ico");
    taskService.deleteAttachment(attachment.getId());

    // complete the userTask to finish the process
    taskService.complete(processTaskId);
    assertProcessEnded(process.getId());

    // user "er" works on the process userTask
    identityService.setAuthenticatedUserId("er");

    // create a standalone userTask
    userTask = taskService.newTask();
    userTask.setName("to do");
    taskService.saveTask(userTask);

    // change some properties manually to create an update event
    ClockUtil.setCurrentTime(tomorrow);
    userTask.setDescription("desc");
    userTask.setOwner("icke");
    userTask.setAssignee("er");
    userTask.setDueDate(new Date());
    taskService.saveTask(userTask);

    // complete the userTask
    taskService.complete(userTask.getId());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testQueryProcessInstanceOperationsById() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    runtimeService.suspendProcessInstanceById(process.getId());
    runtimeService.activateProcessInstanceById(process.getId());

    runtimeService.deleteProcessInstance(process.getId(), "a delete reason");

    // then
    assertEquals(3, query().entityType(PROCESS_INSTANCE).count());

    UserOperationLogEntry deleteEntry = query()
        .entityType(PROCESS_INSTANCE)
        .processInstanceId(process.getId())
        .operationType(OPERATION_TYPE_DELETE)
        .singleResult();

    assertNotNull(deleteEntry);
    assertEquals(process.getId(), deleteEntry.getProcessInstanceId());
    assertNull(deleteEntry.getProcessDefinitionId());
    assertNull(deleteEntry.getProcessDefinitionKey());

    UserOperationLogEntry suspendEntry = query()
        .entityType(PROCESS_INSTANCE)
        .processInstanceId(process.getId())
        .operationType(OPERATION_TYPE_SUSPEND)
        .singleResult();

    assertNotNull(suspendEntry);
    assertEquals(process.getId(), suspendEntry.getProcessInstanceId());
    assertNull(suspendEntry.getProcessDefinitionId());
    assertNull(suspendEntry.getProcessDefinitionKey());

    assertEquals("suspensionState", suspendEntry.getProperty());
    assertEquals("suspended", suspendEntry.getNewValue());
    assertNull(suspendEntry.getOrgValue());

    UserOperationLogEntry activateEntry = query()
        .entityType(PROCESS_INSTANCE)
        .processInstanceId(process.getId())
        .operationType(OPERATION_TYPE_ACTIVATE)
        .singleResult();

    assertNotNull(activateEntry);
    assertEquals(process.getId(), activateEntry.getProcessInstanceId());
    assertNull(activateEntry.getProcessDefinitionId());
    assertNull(activateEntry.getProcessDefinitionKey());

    assertEquals("suspensionState", activateEntry.getProperty());
    assertEquals("active", activateEntry.getNewValue());
    assertNull(activateEntry.getOrgValue());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testQueryProcessInstanceOperationsByProcessDefinitionId() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    runtimeService.suspendProcessInstanceByProcessDefinitionId(process.getProcessDefinitionId());
    runtimeService.activateProcessInstanceByProcessDefinitionId(process.getProcessDefinitionId());

    // then
    assertEquals(2, query().entityType(PROCESS_INSTANCE).count());

    UserOperationLogEntry suspendEntry = query()
        .entityType(PROCESS_INSTANCE)
        .processDefinitionId(process.getProcessDefinitionId())
        .operationType(OPERATION_TYPE_SUSPEND)
        .singleResult();

    assertNotNull(suspendEntry);
    assertEquals(process.getProcessDefinitionId(), suspendEntry.getProcessDefinitionId());
    assertNull(suspendEntry.getProcessInstanceId());
    assertNull(suspendEntry.getProcessDefinitionKey());

    assertEquals("suspensionState", suspendEntry.getProperty());
    assertEquals("suspended", suspendEntry.getNewValue());
    assertNull(suspendEntry.getOrgValue());

    UserOperationLogEntry activateEntry = query()
        .entityType(PROCESS_INSTANCE)
        .processDefinitionId(process.getProcessDefinitionId())
        .operationType(OPERATION_TYPE_ACTIVATE)
        .singleResult();

    assertNotNull(activateEntry);
    assertNull(activateEntry.getProcessInstanceId());
    assertNull(activateEntry.getProcessDefinitionKey());
    assertEquals(process.getProcessDefinitionId(), activateEntry.getProcessDefinitionId());

    assertEquals("suspensionState", activateEntry.getProperty());
    assertEquals("active", activateEntry.getNewValue());
    assertNull(activateEntry.getOrgValue());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testQueryProcessInstanceOperationsByProcessDefinitionKey() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    runtimeService.suspendProcessInstanceByProcessDefinitionKey("oneTaskProcess");
    runtimeService.activateProcessInstanceByProcessDefinitionKey("oneTaskProcess");

    // then
    assertEquals(2, query().entityType(PROCESS_INSTANCE).count());

    UserOperationLogEntry suspendEntry = query()
        .entityType(PROCESS_INSTANCE)
        .processDefinitionKey("oneTaskProcess")
        .operationType(OPERATION_TYPE_SUSPEND)
        .singleResult();

    assertNotNull(suspendEntry);
    assertNull(suspendEntry.getProcessInstanceId());
    assertNull(suspendEntry.getProcessDefinitionId());
    assertEquals("oneTaskProcess", suspendEntry.getProcessDefinitionKey());

    assertEquals("suspensionState", suspendEntry.getProperty());
    assertEquals("suspended", suspendEntry.getNewValue());
    assertNull(suspendEntry.getOrgValue());

    UserOperationLogEntry activateEntry = query()
        .entityType(PROCESS_INSTANCE)
        .processDefinitionKey("oneTaskProcess")
        .operationType(OPERATION_TYPE_ACTIVATE)
        .singleResult();

    assertNotNull(activateEntry);
    assertNull(activateEntry.getProcessInstanceId());
    assertNull(activateEntry.getProcessDefinitionId());
    assertEquals("oneTaskProcess", activateEntry.getProcessDefinitionKey());

    assertEquals("suspensionState", activateEntry.getProperty());
    assertEquals("active", activateEntry.getNewValue());
    assertNull(activateEntry.getOrgValue());

    // clear op log
    historyService.deleteUserOperationLogEntry(suspendEntry.getId());
    historyService.deleteUserOperationLogEntry(activateEntry.getId());

  }

  /**
   * CAM-1930: add assertions for additional op log entries here
   */
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testQueryProcessDefinitionOperationsById() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    repositoryService.suspendProcessDefinitionById(process.getProcessDefinitionId(), true, null);
    repositoryService.activateProcessDefinitionById(process.getProcessDefinitionId(), true, null);

    // then
    assertEquals(2, query().entityType(PROCESS_INSTANCE).count());

    UserOperationLogEntry suspendEntry = query()
        .entityType(PROCESS_INSTANCE)
        .processDefinitionId(process.getProcessDefinitionId())
        .operationType(OPERATION_TYPE_SUSPEND)
        .singleResult();

    assertNotNull(suspendEntry);
    assertNull(suspendEntry.getProcessInstanceId());
    assertEquals(process.getProcessDefinitionId(), suspendEntry.getProcessDefinitionId());
    assertNull(suspendEntry.getProcessDefinitionKey());

    assertEquals("suspensionState", suspendEntry.getProperty());
    assertEquals("suspended", suspendEntry.getNewValue());
    assertNull(suspendEntry.getOrgValue());

    UserOperationLogEntry activateEntry = query()
        .entityType(PROCESS_INSTANCE)
        .processDefinitionId(process.getProcessDefinitionId())
        .operationType(OPERATION_TYPE_ACTIVATE)
        .singleResult();

    assertNotNull(activateEntry);
    assertNull(activateEntry.getProcessInstanceId());
    assertEquals(process.getProcessDefinitionId(), activateEntry.getProcessDefinitionId());
    assertNull(activateEntry.getProcessDefinitionKey());

    assertEquals("suspensionState", activateEntry.getProperty());
    assertEquals("active", activateEntry.getNewValue());
    assertNull(activateEntry.getOrgValue());
  }

  /**
   * CAM-1930: add assertions for additional op log entries here
   */
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testQueryProcessDefinitionOperationsByKey() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    repositoryService.suspendProcessDefinitionByKey("oneTaskProcess", true, null);
    repositoryService.activateProcessDefinitionByKey("oneTaskProcess", true, null);

    // then
    assertEquals(2, query().entityType(PROCESS_INSTANCE).count());

    UserOperationLogEntry suspendEntry = query()
        .entityType(PROCESS_INSTANCE)
        .processDefinitionKey("oneTaskProcess")
        .operationType(OPERATION_TYPE_SUSPEND)
        .singleResult();

    assertNotNull(suspendEntry);
    assertNull(suspendEntry.getProcessInstanceId());
    assertNull(suspendEntry.getProcessDefinitionId());
    assertEquals("oneTaskProcess", suspendEntry.getProcessDefinitionKey());

    assertEquals("suspensionState", suspendEntry.getProperty());
    assertEquals("suspended", suspendEntry.getNewValue());
    assertNull(suspendEntry.getOrgValue());

    UserOperationLogEntry activateEntry = query()
        .entityType(PROCESS_INSTANCE)
        .processDefinitionKey("oneTaskProcess")
        .operationType(OPERATION_TYPE_ACTIVATE)
        .singleResult();

    assertNotNull(activateEntry);
    assertNull(activateEntry.getProcessInstanceId());
    assertNull(activateEntry.getProcessDefinitionId());
    assertEquals("oneTaskProcess", activateEntry.getProcessDefinitionKey());

    assertEquals("suspensionState", activateEntry.getProperty());
    assertEquals("active", activateEntry.getNewValue());
    assertNull(activateEntry.getOrgValue());

    // clear op log
    historyService.deleteUserOperationLogEntry(suspendEntry.getId());
    historyService.deleteUserOperationLogEntry(activateEntry.getId());

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testQueryByCaseDefinitionId() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
       .withCaseDefinition(caseDefinitionId)
       .create();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(caseExecutionId)
      .manualStart();

    Task task = taskService.createTaskQuery().singleResult();

    assertNotNull(task);

    // when
    taskService.setAssignee(task.getId(), "demo");

    // then

    UserOperationLogQuery query = historyService
      .createUserOperationLogQuery()
      .caseDefinitionId(caseDefinitionId);

    verifyQueryResults(query, 1);
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testQueryByCaseInstanceId() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    String caseInstanceId = caseService
       .withCaseDefinition(caseDefinitionId)
       .create()
       .getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(caseExecutionId)
      .manualStart();

    Task task = taskService.createTaskQuery().singleResult();

    assertNotNull(task);

    // when
    taskService.setAssignee(task.getId(), "demo");

    // then

    UserOperationLogQuery query = historyService
      .createUserOperationLogQuery()
      .caseInstanceId(caseInstanceId);

    verifyQueryResults(query, 1);
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testQueryByCaseExecutionId() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
       .withCaseDefinition(caseDefinitionId)
       .create();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(caseExecutionId)
      .manualStart();

    Task task = taskService.createTaskQuery().singleResult();

    assertNotNull(task);

    // when
    taskService.setAssignee(task.getId(), "demo");

    // then

    UserOperationLogQuery query = historyService
      .createUserOperationLogQuery()
      .caseExecutionId(caseExecutionId);

    verifyQueryResults(query, 1);
  }

  private void verifyQueryResults(UserOperationLogQuery query, int countExpected) {
    assertEquals(countExpected, query.list().size());
    assertEquals(countExpected, query.count());

    if (countExpected == 1) {
      assertNotNull(query.singleResult());
    } else if (countExpected > 1){
      verifySingleResultFails(query);
    } else if (countExpected == 0) {
      assertNull(query.singleResult());
    }
  }

  private void verifySingleResultFails(UserOperationLogQuery query) {
    try {
      query.singleResult();
      fail();
    } catch (ProcessEngineException e) {}
  }

}
