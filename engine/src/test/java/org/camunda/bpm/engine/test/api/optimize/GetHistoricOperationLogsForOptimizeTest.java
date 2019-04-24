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
package org.camunda.bpm.engine.test.api.optimize;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.OptimizeService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Attachment;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static junit.framework.TestCase.assertTrue;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.CATEGORY_TASK_WORKER;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_ASSIGN;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_CLAIM;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_COMPLETE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class GetHistoricOperationLogsForOptimizeTest {

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testHelper);

  private OptimizeService optimizeService;

  protected String userId = "test";

  private IdentityService identityService;
  private RuntimeService runtimeService;
  private AuthorizationService authorizationService;
  private TaskService taskService;


  @Before
  public void init() {
    ProcessEngineConfigurationImpl config =
      engineRule.getProcessEngineConfiguration();
    optimizeService = config.getOptimizeService();
    identityService = engineRule.getIdentityService();
    runtimeService = engineRule.getRuntimeService();
    authorizationService = engineRule.getAuthorizationService();
    taskService = engineRule.getTaskService();

    createUser(userId);
    identityService.setAuthenticatedUserId(userId);
  }

  @After
  public void cleanUp() {
    for (User user : identityService.createUserQuery().list()) {
      identityService.deleteUser(user.getId());
    }
    for (Group group : identityService.createGroupQuery().list()) {
      identityService.deleteGroup(group.getId());
    }
    for (Authorization authorization : authorizationService.createAuthorizationQuery().list()) {
      authorizationService.deleteAuthorization(authorization.getId());
    }
    ClockUtil.reset();
    identityService.clearAuthentication();
  }

  @Test
  public void getHistoricUserOperationLogs() {
     // given
    BpmnModelInstance simpleDefinition = Bpmn.createExecutableProcess("process")
      .startEvent("startEvent")
      .userTask("userTask")
        .name("task")
      .endEvent("endEvent")
      .done();
    testHelper.deploy(simpleDefinition);
    runtimeService.startProcessInstanceByKey("process");
    claimAllUserTasks();

    // when
    List<UserOperationLogEntry> userOperationsLog =
      optimizeService.getHistoricUserOperationLogs(pastDate(), null, 10);

    // then
    assertThat(userOperationsLog.size(), is(1));
    assertThatTasksHaveAllImportantInformation(userOperationsLog.get(0));
  }

  @Test
  public void occurredAfterParameterWorks() {
    // given
    BpmnModelInstance simpleDefinition = Bpmn.createExecutableProcess("process")
      .startEvent()
      .userTask("userTask")
        .camundaAssignee(userId)
      .endEvent()
      .done();
    testHelper.deploy(simpleDefinition);
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    engineRule.getRuntimeService().startProcessInstanceByKey("process");
    unclaimAllUserTasks();

    Date nowPlus2Seconds = new Date(now.getTime() + 2000L);
    ClockUtil.setCurrentTime(nowPlus2Seconds);
    claimAllUserTasks();

    Date nowPlus4Seconds = new Date(now.getTime() + 4000L);
    ClockUtil.setCurrentTime(nowPlus4Seconds);
    completeAllUserTasks();

    // when
    List<UserOperationLogEntry> userOperationsLog =
      optimizeService.getHistoricUserOperationLogs(now, null, 10);

    // then
    Set<String> allowedOperationsTypes = new HashSet<>(Arrays.asList(OPERATION_TYPE_CLAIM, OPERATION_TYPE_COMPLETE));
    assertThat(userOperationsLog.size(), is(2));
    assertTrue(allowedOperationsTypes.contains(userOperationsLog.get(0).getOperationType()));
    assertTrue(allowedOperationsTypes.contains(userOperationsLog.get(1).getOperationType()));
  }

  @Test
  public void occurredAtParameterWorks() {
    // given
    BpmnModelInstance simpleDefinition = Bpmn.createExecutableProcess("process")
      .startEvent()
      .userTask("userTask")
      .endEvent()
      .done();
    testHelper.deploy(simpleDefinition);
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    engineRule.getRuntimeService().startProcessInstanceByKey("process");
    claimAllUserTasks();

    Date nowPlus2Seconds = new Date(now.getTime() + 2000L);
    ClockUtil.setCurrentTime(nowPlus2Seconds);
    completeAllUserTasks();

    // when
    List<UserOperationLogEntry> userOperationsLog =
      optimizeService.getHistoricUserOperationLogs(null, now, 10);

    // then
    assertThat(userOperationsLog.size(), is(1));
    assertThat(userOperationsLog.get(0).getOperationType(), is(OPERATION_TYPE_CLAIM));
    assertThat(userOperationsLog.get(0).getCategory(), is(CATEGORY_TASK_WORKER));
  }

  @Test
  public void occurredAfterAndOccurredAtParameterWorks() {
    // given
    BpmnModelInstance simpleDefinition = Bpmn.createExecutableProcess("process")
      .startEvent()
      .userTask("userTask")
      .endEvent()
      .done();
    testHelper.deploy(simpleDefinition);
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    engineRule.getRuntimeService().startProcessInstanceByKey("process");
    claimAllUserTasks();

    Date nowPlus2Seconds = new Date(now.getTime() + 2000L);
    ClockUtil.setCurrentTime(nowPlus2Seconds);
    completeAllUserTasks();

    // when
    List<UserOperationLogEntry> userOperationsLog =
      optimizeService.getHistoricUserOperationLogs(now, now, 10);

    // then
    assertThat(userOperationsLog.size(), is(0));
  }
//
  @Test
  public void maxResultsParameterWorks() {
     // given
    BpmnModelInstance simpleDefinition = Bpmn.createExecutableProcess("process")
      .startEvent()
      .userTask()
      .userTask()
      .userTask()
      .endEvent()
      .done();
    testHelper.deploy(simpleDefinition);
    engineRule.getRuntimeService().startProcessInstanceByKey("process");
    claimAndCompleteAllUserTasks();
    claimAndCompleteAllUserTasks();
    claimAndCompleteAllUserTasks();

    // when
    List<UserOperationLogEntry> userOperationsLog =
      optimizeService.getHistoricUserOperationLogs(pastDate(), null, 3);

    // then
    assertThat(userOperationsLog.size(), is(3));
  }

  @Test
  public void resultIsSortedByTimestamp() {
    // given
    BpmnModelInstance simpleDefinition = Bpmn.createExecutableProcess("process")
      .startEvent()
      .userTask("userTask")
        .camundaAssignee(userId)
      .endEvent()
      .done();
    testHelper.deploy(simpleDefinition);
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    engineRule.getRuntimeService().startProcessInstanceByKey("process");
    unclaimAllUserTasks();

    Date nowPlus2Seconds = new Date(now.getTime() + 2000L);
    ClockUtil.setCurrentTime(nowPlus2Seconds);
    claimAllUserTasks();

    Date nowPlus4Seconds = new Date(now.getTime() + 4000L);
    ClockUtil.setCurrentTime(nowPlus4Seconds);
    completeAllUserTasks();

    // when
    List<UserOperationLogEntry> userOperationsLog =
      optimizeService.getHistoricUserOperationLogs(pastDate(), null, 4);

    // then
    assertThat(userOperationsLog.size(), is(3));
    assertThat(userOperationsLog.get(0).getOperationType(), is(OPERATION_TYPE_ASSIGN));
    assertThat(userOperationsLog.get(1).getOperationType(), is(OPERATION_TYPE_CLAIM));
    assertThat(userOperationsLog.get(2).getOperationType(), is(OPERATION_TYPE_COMPLETE));
  }

  @Test
  public void fetchOnlyUserTaskBasedLogEntries() {
     // given
    BpmnModelInstance simpleDefinition = Bpmn.createExecutableProcess("process")
      .startEvent("startEvent")
      .userTask()
      .endEvent()
      .done();
    testHelper.deploy(simpleDefinition);
    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceByKey("process");
    createLogEntriesThatShouldNotBeReturned(processInstance.getId());
    assertThat(engineRule.getHistoryService().createUserOperationLogQuery().count(), greaterThan(0L));

    // when
    List<UserOperationLogEntry> userOperationsLog =
      optimizeService.getHistoricUserOperationLogs(pastDate(), null, 10);

    // then
    assertThat(userOperationsLog.size(), is(0));
  }

  private void createLogEntriesThatShouldNotBeReturned(String processInstanceId) {
    ClockUtil.setCurrentTime(new Date());

    String processTaskId = taskService.createTaskQuery().singleResult().getId();

    // create and remove some links
    taskService.addCandidateUser(processTaskId, "er");
    taskService.deleteCandidateUser(processTaskId, "er");
    taskService.addCandidateGroup(processTaskId, "wir");
    taskService.deleteCandidateGroup(processTaskId, "wir");

    // assign and reassign the owner
    taskService.setOwner(processTaskId, "icke");

    // change priority of task
    taskService.setPriority(processTaskId, 10);

    // add and delete an attachment
    Attachment attachment = taskService.createAttachment(
      "image/ico",
      processTaskId,
      processInstanceId,
      "favicon.ico",
      "favicon",
      "http://camunda.com/favicon.ico"
    );
    taskService.deleteAttachment(attachment.getId());
    runtimeService.deleteProcessInstance(processInstanceId, "that's why");

    // create a standalone userTask
    Task userTask = taskService.newTask();
    userTask.setName("to do");
    taskService.saveTask(userTask);

    // change some properties manually to create an update event
    ClockUtil.setCurrentTime(new Date());
    userTask.setDescription("desc");
    userTask.setOwner("icke");
    userTask.setAssignee("er");
    userTask.setDueDate(new Date());
    taskService.saveTask(userTask);

    taskService.deleteTask(userTask.getId(), true);
  }


  private Date pastDate() {
    return new Date(2L);
  }

  private void claimAndCompleteAllUserTasks() {
    List<Task> list = taskService.createTaskQuery().list();
    for (Task task : list) {
      taskService.claim(task.getId(), userId);
      taskService.complete(task.getId());
    }
  }

  private void completeAllUserTasks() {
    List<Task> list = taskService.createTaskQuery().list();
    for (Task task : list) {
      taskService.complete(task.getId());
    }
  }

  private void claimAllUserTasks() {
    List<Task> list = taskService.createTaskQuery().list();
    for (Task task : list) {
      taskService.claim(task.getId(), userId);
    }
  }

  private void unclaimAllUserTasks() {
    List<Task> list = taskService.createTaskQuery().list();
    for (Task task : list) {
      taskService.setAssignee(task.getId(), null);
    }
  }

  protected void createUser(String userId) {
    User user = identityService.newUser(userId);
    identityService.saveUser(user);
  }

  private void assertThatTasksHaveAllImportantInformation(UserOperationLogEntry userOperationLogEntry) {
    assertThat(userOperationLogEntry, notNullValue());
    assertThat(userOperationLogEntry.getId(), notNullValue());
    assertThat(userOperationLogEntry.getOperationType(), is(OPERATION_TYPE_CLAIM));
    assertThat(userOperationLogEntry.getOrgValue(), nullValue());
    assertThat(userOperationLogEntry.getNewValue(), is(userId));
    assertThat(userOperationLogEntry.getTimestamp(), notNullValue());
    assertThat(userOperationLogEntry.getProcessDefinitionKey(), is("process"));
    assertThat(userOperationLogEntry.getProcessDefinitionId(), notNullValue());
    assertThat(userOperationLogEntry.getUserId(), is(userId));
    assertThat(userOperationLogEntry.getTaskId(), is(taskService.createTaskQuery().singleResult().getId()));
    assertThat(userOperationLogEntry.getCategory(), is(CATEGORY_TASK_WORKER));
  }

}
