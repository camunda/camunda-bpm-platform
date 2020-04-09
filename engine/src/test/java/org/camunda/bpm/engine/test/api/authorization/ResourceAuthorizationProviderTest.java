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

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;
import static org.camunda.bpm.engine.authorization.Permissions.ALL;
import static org.camunda.bpm.engine.authorization.Resources.AUTHORIZATION;
import static org.camunda.bpm.engine.authorization.Resources.TASK;
import static org.camunda.bpm.engine.authorization.Resources.USER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collections;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.task.IdentityLinkType;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class ResourceAuthorizationProviderTest {

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(
      "org/camunda/bpm/engine/test/api/authorization/resource.authorization.provider.camunda.cfg.xml");
  @Rule
  public ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected IdentityService identityService;
  protected AuthorizationService authorizationService;
  protected TaskService taskService;

  protected String userId = "test";
  protected String groupId = "accounting";
  protected User user;
  protected Group group;

  @Before
  public void setUp() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    processEngineConfiguration.setResourceAuthorizationProvider(new MyResourceAuthorizationProvider());

    identityService = engineRule.getIdentityService();
    taskService = engineRule.getTaskService();
    authorizationService = engineRule.getAuthorizationService();

    user = createUser(userId);
    group = createGroup(groupId);

    identityService.createMembership(userId, groupId);

    identityService.setAuthentication(userId, Collections.singletonList(groupId));
    processEngineConfiguration.setAuthorizationEnabled(true);
  }

  @After
  public void tearDown() {
    processEngineConfiguration.setAuthorizationEnabled(false);
    for (User user : identityService.createUserQuery().list()) {
      identityService.deleteUser(user.getId());
    }
    for (Group group : identityService.createGroupQuery().list()) {
      identityService.deleteGroup(group.getId());
    }
    for (Authorization authorization : authorizationService.createAuthorizationQuery().list()) {
      authorizationService.deleteAuthorization(authorization.getId());
    }
  }

  @Test
  public void testNewTaskAssignee() {
    // given
    MyResourceAuthorizationProvider.clearProperties();

    createGrantAuthorization(TASK, ANY, ALL, userId);

    String taskId = "myTask";
    Task newTask = taskService.newTask(taskId);
    taskService.saveTask(newTask);

    // when (1)
    taskService.setAssignee(taskId, "demo");

    // then (1)
    assertNull(MyResourceAuthorizationProvider.OLD_ASSIGNEE);
    assertEquals("demo", MyResourceAuthorizationProvider.NEW_ASSIGNEE);

    MyResourceAuthorizationProvider.clearProperties();

    // when (2)
    taskService.setAssignee(taskId, userId);

    // then (2)
    assertEquals("demo", MyResourceAuthorizationProvider.OLD_ASSIGNEE);
    assertEquals(userId, MyResourceAuthorizationProvider.NEW_ASSIGNEE);

    taskService.deleteTask(taskId, true);
  }

  @Test
  public void testNewTaskOwner() {
    // given
    MyResourceAuthorizationProvider.clearProperties();

    createGrantAuthorization(TASK, ANY, ALL, userId);

    String taskId = "myTask";
    Task newTask = taskService.newTask(taskId);
    taskService.saveTask(newTask);

    // when (1)
    taskService.setOwner(taskId, "demo");

    // then (1)
    assertNull(MyResourceAuthorizationProvider.OLD_OWNER);
    assertEquals("demo", MyResourceAuthorizationProvider.NEW_OWNER);

    MyResourceAuthorizationProvider.clearProperties();

    // when (2)
    taskService.setOwner(taskId, userId);

    // then (2)
    assertEquals("demo", MyResourceAuthorizationProvider.OLD_OWNER);
    assertEquals(userId, MyResourceAuthorizationProvider.NEW_OWNER);

    taskService.deleteTask(taskId, true);
  }

  @Test
  public void testAddCandidateUser() {
    // given
    MyResourceAuthorizationProvider.clearProperties();

    createGrantAuthorization(TASK, ANY, ALL, userId);

    String taskId = "myTask";
    Task newTask = taskService.newTask(taskId);
    taskService.saveTask(newTask);

    // when
    taskService.addCandidateUser(taskId, "demo");

    // then
    assertEquals(IdentityLinkType.CANDIDATE, MyResourceAuthorizationProvider.ADD_USER_IDENTITY_LINK_TYPE);
    assertEquals("demo", MyResourceAuthorizationProvider.ADD_USER_IDENTITY_LINK_USER);

    taskService.deleteTask(taskId, true);
  }

  @Test
  public void testAddUserIdentityLink() {
    // given
    MyResourceAuthorizationProvider.clearProperties();

    createGrantAuthorization(TASK, ANY, ALL, userId);

    String taskId = "myTask";
    Task newTask = taskService.newTask(taskId);
    taskService.saveTask(newTask);

    // when
    taskService.addUserIdentityLink(taskId, "demo", "myIdentityLink");

    // then
    assertEquals("myIdentityLink", MyResourceAuthorizationProvider.ADD_USER_IDENTITY_LINK_TYPE);
    assertEquals("demo", MyResourceAuthorizationProvider.ADD_USER_IDENTITY_LINK_USER);

    taskService.deleteTask(taskId, true);
  }

  @Test
  public void testAddCandidateGroup() {
    // given
    MyResourceAuthorizationProvider.clearProperties();

    createGrantAuthorization(TASK, ANY, ALL, userId);

    String taskId = "myTask";
    Task newTask = taskService.newTask(taskId);
    taskService.saveTask(newTask);

    // when
    taskService.addCandidateGroup(taskId, "management");

    // then
    assertEquals(IdentityLinkType.CANDIDATE, MyResourceAuthorizationProvider.ADD_GROUP_IDENTITY_LINK_TYPE);
    assertEquals("management", MyResourceAuthorizationProvider.ADD_GROUP_IDENTITY_LINK_GROUP);

    taskService.deleteTask(taskId, true);
  }

  @Test
  public void testAddGroupIdentityLink() {
    // given
    MyResourceAuthorizationProvider.clearProperties();

    createGrantAuthorization(TASK, ANY, ALL, userId);

    String taskId = "myTask";
    Task newTask = taskService.newTask(taskId);
    taskService.saveTask(newTask);

    // when
    taskService.addGroupIdentityLink(taskId, "management", "myIdentityLink");

    // then
    assertEquals("myIdentityLink", MyResourceAuthorizationProvider.ADD_GROUP_IDENTITY_LINK_TYPE);
    assertEquals("management", MyResourceAuthorizationProvider.ADD_GROUP_IDENTITY_LINK_GROUP);

    taskService.deleteTask(taskId, true);
  }

  @Test
  public void testDeleteUserIdentityLink() {
    // given
    MyResourceAuthorizationProvider.clearProperties();

    createGrantAuthorization(TASK, ANY, ALL, userId);

    String taskId = "myTask";
    Task newTask = taskService.newTask(taskId);
    taskService.saveTask(newTask);
    taskService.addCandidateUser(taskId, "demo");

    // when
    taskService.deleteCandidateUser(taskId, "demo");

    // then
    assertEquals(IdentityLinkType.CANDIDATE, MyResourceAuthorizationProvider.DELETE_USER_IDENTITY_LINK_TYPE);
    assertEquals("demo", MyResourceAuthorizationProvider.DELETE_USER_IDENTITY_LINK_USER);

    taskService.deleteTask(taskId, true);
  }

  @Test
  public void testDeleteGroupIdentityLink() {
    // given
    MyResourceAuthorizationProvider.clearProperties();

    createGrantAuthorization(TASK, ANY, ALL, userId);

    String taskId = "myTask";
    Task newTask = taskService.newTask(taskId);
    taskService.saveTask(newTask);
    taskService.addCandidateGroup(taskId, "management");

    // when
    taskService.deleteCandidateGroup(taskId, "management");

    // then
    assertEquals(IdentityLinkType.CANDIDATE, MyResourceAuthorizationProvider.DELETE_GROUP_IDENTITY_LINK_TYPE);
    assertEquals("management", MyResourceAuthorizationProvider.DELETE_GROUP_IDENTITY_LINK_GROUP);

    taskService.deleteTask(taskId, true);
  }

  // user ////////////////////////////////////////////////////////////////

  protected User createUser(String userId) {
    User user = identityService.newUser(userId);
    identityService.saveUser(user);

    // give user all permission to manipulate authorizations
    Authorization authorization = createGrantAuthorization(AUTHORIZATION, ANY);
    authorization.setUserId(userId);
    authorization.addPermission(ALL);
    saveAuthorization(authorization);

    // give user all permission to manipulate users
    authorization = createGrantAuthorization(USER, ANY);
    authorization.setUserId(userId);
    authorization.addPermission(Permissions.ALL);
    saveAuthorization(authorization);

    return user;
  }

  // group //////////////////////////////////////////////////////////////

  protected Group createGroup(String groupId) {
    Group group = identityService.newGroup(groupId);
    identityService.saveGroup(group);
    return group;
  }

  // authorization ///////////////////////////////////////////////////////

  protected void createGrantAuthorization(Resource resource, String resourceId, Permission permission, String userId) {
    Authorization authorization = createGrantAuthorization(resource, resourceId);
    authorization.setUserId(userId);
    authorization.addPermission(permission);
    saveAuthorization(authorization);
  }

  protected Authorization createGrantAuthorization(Resource resource, String resourceId) {
    Authorization authorization = createAuthorization(AUTH_TYPE_GRANT, resource, resourceId);
    return authorization;
  }

  protected Authorization createAuthorization(int type, Resource resource, String resourceId) {
    Authorization authorization = authorizationService.createNewAuthorization(type);

    authorization.setResource(resource);
    if (resourceId != null) {
      authorization.setResourceId(resourceId);
    }

    return authorization;
  }

  protected void saveAuthorization(Authorization authorization) {
    authorizationService.saveAuthorization(authorization);
  }

}
