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
package org.camunda.bpm.engine.test.api.multitenancy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_FULL;
import java.util.Arrays;
import java.util.Date;

import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Attachment;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

@RequiredHistoryLevel(HISTORY_FULL)
public class MultiTenancyUserOperationLogTest {

  protected static final String USER_ID = "aUserId";
  protected static final String USER_WITHOUT_TENANT = "aUserId1";
  // TODO
  // protected static final String TENANT_TWO = "tenant2";
  protected static final String TENANT_ONE = "tenant1";
  protected static final String PROCESS_NAME = "process";
  protected static final String TASK_ID = "aTaskId";
  protected static final String AN_ANNOTATION = "anAnnotation";

  protected static final BpmnModelInstance MODEL = Bpmn.createExecutableProcess(PROCESS_NAME)
      .startEvent().userTask(TASK_ID).done();

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected ProcessEngineConfiguration processEngineConfiguration;
  protected TaskService taskService;
  protected HistoryService historyService;
  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;
  protected IdentityService identityService;

  @Before
  public void init() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    taskService = engineRule.getTaskService();
    historyService = engineRule.getHistoryService();
    repositoryService = engineRule.getRepositoryService();
    runtimeService = engineRule.getRuntimeService();
    identityService = engineRule.getIdentityService();
  }

  @Test
  public void shouldSetAnnotationOpLogWithTenant() {
    // given
    testRule.deployForTenant(TENANT_ONE, MODEL);
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));

    runtimeService.startProcessInstanceByKey(PROCESS_NAME);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    taskService.complete(taskId);

    UserOperationLogEntry singleResult = historyService.createUserOperationLogQuery().entityType(EntityTypes.TASK).singleResult();

    // when
    historyService.setAnnotationForOperationLogById(singleResult.getOperationId(), AN_ANNOTATION);
    singleResult = historyService.createUserOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_SET_ANNOTATION)
        .singleResult();

    // then
    assertThat(singleResult.getTenantId()).isEqualTo(TENANT_ONE);
  }

  @Test
  public void shouldClearAnnotationOpLogWithTenant() {
    testRule.deployForTenant(TENANT_ONE, MODEL);
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));

    runtimeService.startProcessInstanceByKey(PROCESS_NAME);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    taskService.complete(taskId);

    UserOperationLogEntry singleResult = historyService.createUserOperationLogQuery().entityType(EntityTypes.TASK).singleResult();

    historyService.setAnnotationForOperationLogById(singleResult.getOperationId(), AN_ANNOTATION);

    // when
    historyService.clearAnnotationForOperationLogById(singleResult.getOperationId());
    singleResult = historyService.createUserOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_CLEAR_ANNOTATION)
        .singleResult();

    // then
    assertThat(singleResult.getTenantId()).isEqualTo(TENANT_ONE);
  }

  @Test
  public void shouldSetAnnotationToIncidentWithTenant() {
    // given
    testRule.deployForTenant(TENANT_ONE, MODEL);
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_NAME);
    Incident incident = runtimeService.createIncident("foo", processInstance.getId(), TASK_ID, "bar");


    // when
    runtimeService.setAnnotationForIncidentById(incident.getId(), AN_ANNOTATION);
    UserOperationLogEntry singleResult = historyService.createUserOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_SET_ANNOTATION)
        .singleResult();

    // then

    assertThat(singleResult.getTenantId()).isEqualTo(TENANT_ONE);
  }

  @Test
  public void shouldClearAnnotationToIncidentWithTenant() {
    // given
    testRule.deployForTenant(TENANT_ONE, MODEL);
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_NAME);
    Incident incident = runtimeService.createIncident("foo", processInstance.getId(), TASK_ID, "bar");


    // when
    runtimeService.clearAnnotationForIncidentById(incident.getId());
    UserOperationLogEntry singleResult = historyService.createUserOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_CLEAR_ANNOTATION)
        .singleResult();

    // then

    assertThat(singleResult.getTenantId()).isEqualTo(TENANT_ONE);
  }

  /**
   * start process and operate on userTask to create some log entries for the query tests
   */
  private void createLogEntries() {
//    ClockUtil.setCurrentTime(yesterday);

    // create a process with a userTask and work with it
    ProcessInstance process = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Execution execution = runtimeService.createExecutionQuery().processInstanceId(process.getId()).singleResult();
    String processTaskId = taskService.createTaskQuery().singleResult().getId();

    // user "icke" works on the process userTask
    identityService.setAuthenticatedUserId("icke");

    // create and remove some links
    taskService.addCandidateUser(processTaskId, "er");
    taskService.deleteCandidateUser(processTaskId, "er");
    taskService.addCandidateGroup(processTaskId, "wir");
    taskService.deleteCandidateGroup(processTaskId, "wir");

    // assign and reassign the userTask
//    ClockUtil.setCurrentTime(today);
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
    testRule.assertProcessEnded(process.getId());

    // user "er" works on the process userTask
    identityService.setAuthenticatedUserId("er");

    // create a standalone userTask
    Task userTask = taskService.newTask();
    userTask.setName("to do");
    taskService.saveTask(userTask);

    // change some properties manually to create an update event
//    ClockUtil.setCurrentTime(tomorrow);
    userTask.setDescription("desc");
    userTask.setOwner("icke");
    userTask.setAssignee("er");
    userTask.setDueDate(new Date());
    taskService.saveTask(userTask);

    // complete the userTask
    taskService.complete(userTask.getId());
  }

}
