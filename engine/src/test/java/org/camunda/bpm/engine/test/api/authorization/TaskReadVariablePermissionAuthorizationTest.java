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
package org.camunda.bpm.engine.test.api.authorization;

import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Resources.HISTORIC_TASK;
import static org.camunda.bpm.engine.authorization.Resources.TASK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.HistoricTaskPermissions;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.authorization.TaskPermissions;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.IdentityLinkType;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class TaskReadVariablePermissionAuthorizationTest {

  protected static final String AUTHORIZATION_TYP_HISTORIC = "historicAuthorization";
  protected static final String AUTHORIZATION_TYP_RUNTIME = "runtimeAuthorization";

  private static final String PROCESS_KEY = "oneTaskProcess";
  private static final String DEMO = "demo";
  private static final String ACCOUNTING_GROUP = "accounting";
  protected static String userId = "test";

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected AuthorizationTestRule authRule = new AuthorizationTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(authRule);

  private ProcessEngineConfigurationImpl processEngineConfiguration;
  private IdentityService identityService;
  private AuthorizationService authorizationService;
  private TaskService taskService;
  private RuntimeService runtimeService;

  private boolean enforceSpecificVariablePermission;
  protected boolean enableHistoricInstancePermissions;

  protected String authorizationType;

  @Parameterized.Parameters(name = "{0}")
  public static Collection<String> scenarios() {
    return Arrays.asList(AUTHORIZATION_TYP_HISTORIC, AUTHORIZATION_TYP_RUNTIME);
  }

  public TaskReadVariablePermissionAuthorizationTest(String authorizationType) {
    this.authorizationType = authorizationType;
  }

  @Before
  public void init() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    identityService = engineRule.getIdentityService();
    authorizationService = engineRule.getAuthorizationService();
    taskService = engineRule.getTaskService();
    runtimeService = engineRule.getRuntimeService();

    enforceSpecificVariablePermission = processEngineConfiguration.isEnforceSpecificVariablePermission();
    processEngineConfiguration.setEnforceSpecificVariablePermission(true);
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    User user = identityService.newUser(userId);
    identityService.saveUser(user);
    identityService.setAuthenticatedUserId(userId);
    authRule.createGrantAuthorization(Resources.AUTHORIZATION, "*", userId, Permissions.CREATE);
  }

  @After
  public void cleanUp() {
    authRule.disableAuthorization();
    for (User user : identityService.createUserQuery().list()) {
      identityService.deleteUser(user.getId());
    }
    for (Authorization authorization : authorizationService.createAuthorizationQuery().userIdIn(DEMO).list()) {
      authorizationService.deleteAuthorization(authorization.getId());
    }
    for (Authorization authorization : authorizationService.createAuthorizationQuery().groupIdIn(ACCOUNTING_GROUP).list()) {
      authorizationService.deleteAuthorization(authorization.getId());
    }
    processEngineConfiguration.setEnforceSpecificVariablePermission(enforceSpecificVariablePermission);
    processEngineConfiguration.setEnableHistoricInstancePermissions(enableHistoricInstancePermissions);
  }

  // TaskService#saveTask() ///////////////////////////////////

  @Test
  public void testSaveStandaloneTaskAndCheckAssigneePermissions() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    Task task = selectSingleTask();
    task.setAssignee(DEMO);

    authRule.createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    taskService.saveTask(task);

    // then
    task = selectSingleTask();
    assertNotNull(task);
    assertEquals(DEMO, task.getAssignee());
    verifyUserAuthorization(DEMO);
    taskService.deleteTask(taskId, true);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testSaveProcessTaskAndCheckAssigneePermissions() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    Task task = selectSingleTask();
    task.setAssignee(DEMO);

    authRule.createGrantAuthorization(TASK, task.getId(), userId, UPDATE);

    // when
    taskService.saveTask(task);

    // then
    task = selectSingleTask();
    assertNotNull(task);
    assertEquals(DEMO, task.getAssignee());
    verifyUserAuthorization(DEMO);
  }

  // TaskService#setOwner() ///////////////////////////////////

  @Test
  public void testStandaloneTaskSetOwnerAndCheckOwnerPermissions() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    authRule.createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    taskService.setOwner(taskId, DEMO);

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals(DEMO, task.getOwner());
    verifyUserAuthorization(DEMO);

    taskService.deleteTask(taskId, true);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testProcessTaskSetOwnerAndCheckOwnerPermissions() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    authRule.createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    taskService.setOwner(taskId, DEMO);

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals(DEMO, task.getOwner());
    verifyUserAuthorization(DEMO);
  }

  // TaskService#addUserIdentityLink() ///////////////////////////////////

  @Test
  public void testStandaloneTaskAddUserIdentityLinkAndUserOwnerPermissions() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    authRule.createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    taskService.addUserIdentityLink(taskId, DEMO, IdentityLinkType.CANDIDATE);

    // then
    authRule.disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    authRule.disableAuthorization();

    assertNotNull(linksForTask);
    assertEquals(1, linksForTask.size());

    IdentityLink identityLink = linksForTask.get(0);
    assertNotNull(identityLink);

    assertEquals(DEMO, identityLink.getUserId());
    assertEquals(IdentityLinkType.CANDIDATE, identityLink.getType());
    verifyUserAuthorization(DEMO);

    taskService.deleteTask(taskId, true);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testProcessTaskAddUserIdentityLinkWithUpdatePersmissionOnTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    authRule.createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    taskService.addUserIdentityLink(taskId, DEMO, IdentityLinkType.CANDIDATE);

    // then
    authRule.disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    authRule.disableAuthorization();

    assertNotNull(linksForTask);
    assertEquals(1, linksForTask.size());

    IdentityLink identityLink = linksForTask.get(0);
    assertNotNull(identityLink);

    assertEquals(DEMO, identityLink.getUserId());
    assertEquals(IdentityLinkType.CANDIDATE, identityLink.getType());
    verifyUserAuthorization(DEMO);
  }

  // TaskService#addGroupIdentityLink() ///////////////////////////////////

  @Test
  public void testStandaloneTaskAddGroupIdentityLink() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    authRule.createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    taskService.addGroupIdentityLink(taskId, ACCOUNTING_GROUP, IdentityLinkType.CANDIDATE);

    // then
    authRule.disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    authRule.disableAuthorization();

    assertNotNull(linksForTask);
    assertEquals(1, linksForTask.size());

    IdentityLink identityLink = linksForTask.get(0);
    assertNotNull(identityLink);

    assertEquals(ACCOUNTING_GROUP, identityLink.getGroupId());
    assertEquals(IdentityLinkType.CANDIDATE, identityLink.getType());

    verifyGroupAuthorization(ACCOUNTING_GROUP);

    taskService.deleteTask(taskId, true);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testProcessTaskAddGroupIdentityLinkWithUpdatePersmissionOnTask() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    String taskId = selectSingleTask().getId();

    authRule.createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    taskService.addGroupIdentityLink(taskId, ACCOUNTING_GROUP, IdentityLinkType.CANDIDATE);

    // then
    authRule.disableAuthorization();
    List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    authRule.disableAuthorization();

    assertNotNull(linksForTask);
    assertEquals(1, linksForTask.size());

    IdentityLink identityLink = linksForTask.get(0);
    assertNotNull(identityLink);

    assertEquals(ACCOUNTING_GROUP, identityLink.getGroupId());
    assertEquals(IdentityLinkType.CANDIDATE, identityLink.getType());
    verifyGroupAuthorization(ACCOUNTING_GROUP);
  }

  protected void createTask(final String taskId) {
    authRule.disableAuthorization();
    Task task = taskService.newTask(taskId);
    taskService.saveTask(task);
    authRule.enableAuthorization(userId);
  }

  protected Task selectSingleTask() {
    authRule.disableAuthorization();
    Task task = taskService.createTaskQuery().singleResult();
    authRule.enableAuthorization(userId);
    return task;
  }

  protected void startProcessInstanceByKey(String processKey) {
    authRule.disableAuthorization();
    runtimeService.startProcessInstanceByKey(processKey);
    authRule.enableAuthorization(userId);
  }

  protected void verifyUserAuthorization(String userId) {
    authRule.disableAuthorization();

    if (AUTHORIZATION_TYP_RUNTIME.equals(authorizationType)) {
      System.out.println("RUNTIME!");
      Authorization runtimeUserAuthorization = authorizationService.createAuthorizationQuery()
          .resourceType(TASK)
          .userIdIn(userId)
          .singleResult();

      assertNotNull(runtimeUserAuthorization);
      verifyReadVariablePermission(runtimeUserAuthorization, TaskPermissions.READ_VARIABLE);

    } else if (AUTHORIZATION_TYP_HISTORIC.equals(authorizationType)) {
      System.out.println("HISTORY!");

      Authorization historyUserAuthorization = authorizationService.createAuthorizationQuery()
          .resourceType(HISTORIC_TASK)
          .userIdIn(userId)
          .singleResult();

      assertNotNull(historyUserAuthorization);
      verifyReadVariablePermission(historyUserAuthorization, HistoricTaskPermissions.READ_VARIABLE);

    } else {
      throw new RuntimeException("auth type not found");

    }
  }

  protected void verifyGroupAuthorization(String groupId) {
    authRule.disableAuthorization();

    if (AUTHORIZATION_TYP_RUNTIME.equals(authorizationType)) {

      Authorization runtimeGroupAuthorization = authorizationService.createAuthorizationQuery()
          .resourceType(TASK)
          .groupIdIn(groupId).singleResult();
      assertNotNull(runtimeGroupAuthorization);
      verifyReadVariablePermission(runtimeGroupAuthorization, TaskPermissions.READ_VARIABLE);

    } else if (AUTHORIZATION_TYP_HISTORIC.equals(authorizationType)) {

      Authorization historyGroupAuthorization = authorizationService.createAuthorizationQuery()
          .resourceType(HISTORIC_TASK)
          .groupIdIn(groupId).singleResult();
      assertNotNull(historyGroupAuthorization);
      verifyReadVariablePermission(historyGroupAuthorization, HistoricTaskPermissions.READ_VARIABLE);

    } else {
      throw new RuntimeException("auth type not found");

    }
  }

  protected void verifyReadVariablePermission(Authorization groupAuthorization,
                                              Permission expectedPermission) {
    Permission[] permissions = groupAuthorization.getPermissions(new Permission[] { expectedPermission });
    assertNotNull(permissions);
    assertEquals(expectedPermission, permissions[0]);
  }

}
