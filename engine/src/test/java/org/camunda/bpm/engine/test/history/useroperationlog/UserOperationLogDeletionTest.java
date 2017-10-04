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

import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.history.UserOperationLogQuery;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Roman Smirnov
 *
 */
public class UserOperationLogDeletionTest extends AbstractUserOperationLogTest {

  protected static final String PROCESS_PATH = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml";
  protected static final String PROCESS_KEY = "oneTaskProcess";

  @Deployment(resources = PROCESS_PATH)
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
    assertEquals(2, query.count());
  }

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
    assertEquals(3, query.count());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testDeleteCaseTaskKeepUserOperationLog() {
    // given
    caseService
      .withCaseDefinitionByKey("oneTaskCase")
      .create();

    String caseExecutionId = caseService
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
    assertEquals(2, query.count());
  }

  @Deployment(resources = PROCESS_PATH)
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
    assertEquals(3, query.count());

    // when
    historyService.deleteHistoricProcessInstance(processInstanceId);

    // then
    assertEquals(3, query.count());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testDeleteCaseInstanceKeepUserOperationLog() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("oneTaskCase")
        .create()
        .getId();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);

    caseService.closeCaseInstance(caseInstanceId);

    UserOperationLogQuery query = historyService
        .createUserOperationLogQuery()
        .caseInstanceId(caseInstanceId);
    assertEquals(1, query.count());

    // when
    historyService.deleteHistoricCaseInstance(caseInstanceId);

    // then
    assertEquals(1, query.count());
  }

  @Deployment(resources = PROCESS_PATH)
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
    assertEquals(1, query.count());

    // when
    repositoryService.deleteProcessDefinition(processDefinitionId, true);

    // then new log is created and old stays
    assertEquals(1, query.count());
  }

  public void testDeleteProcessDefinitionsByKey() {
    // given
    for (int i = 0; i < 3; i++) {
      deploymentId = repositoryService.createDeployment()
        .addClasspathResource(PROCESS_PATH)
        .deploy().getId();
      deploymentIds.add(deploymentId);
    }

    // when
    repositoryService.deleteProcessDefinitions()
      .byKey(PROCESS_KEY)
      .withoutTenantId()
      .delete();

    // then
    assertUserOperationLogs();
  }

  public void testDeleteProcessDefinitionsByKeyCascading() {
    // given
    for (int i = 0; i < 3; i++) {
      deploymentId = repositoryService.createDeployment()
        .addClasspathResource(PROCESS_PATH)
        .deploy().getId();
      deploymentIds.add(deploymentId);
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

  public void testDeleteProcessDefinitionsByIds() {
    // given
    for (int i = 0; i < 3; i++) {
      deploymentId = repositoryService.createDeployment()
        .addClasspathResource(PROCESS_PATH)
        .deploy().getId();
      deploymentIds.add(deploymentId);
    }

    // when
    repositoryService.deleteProcessDefinitions()
      .byIds(findProcessDefinitionIdsByKey(PROCESS_KEY))
      .delete();

    // then
    assertUserOperationLogs();
  }

  public void testDeleteProcessDefinitionsByIdsCascading() {
    // given
    for (int i = 0; i < 3; i++) {
      deploymentId = repositoryService.createDeployment()
        .addClasspathResource(PROCESS_PATH)
        .deploy().getId();
      deploymentIds.add(deploymentId);
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
    assertEquals(1, query.count());

    // when
    repositoryService.deleteDeployment(deploymentId, true);

    // then
    assertEquals(1, query.count());
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
