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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.impl.TaskQueryImpl;
import org.camunda.bpm.engine.impl.db.AuthorizationCheck;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.Session;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class GroupAuthorizationTest extends AuthorizationTest {

  public static final String testUserId = "testUser";
  public static final List<String> testGroupIds = Arrays.asList("testGroup1", "testGroup2", "testGroup3");

  @Before
  public void setUp() throws Exception {
    createUser(testUserId);
    for (String testGroupId : testGroupIds) {
      createGroupAndAddUser(testGroupId, testUserId);
    }

    identityService.setAuthentication(testUserId, testGroupIds);
    processEngineConfiguration.setAuthorizationEnabled(true);
  }


  @Test
  public void testTaskQueryWithoutGroupAuthorizations() {
    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        AuthorizationManager authorizationManager = spyOnSession(commandContext, AuthorizationManager.class);

        TaskQueryImpl taskQuery = (TaskQueryImpl) spy(processEngine.getTaskService().createTaskQuery());
        AuthorizationCheck authCheck = spy(new AuthorizationCheck());
        when(taskQuery.getAuthCheck()).thenReturn(authCheck);

        taskQuery.list();

        verify(authorizationManager, atLeastOnce()).filterAuthenticatedGroupIds(eq(testGroupIds));
        verify(authCheck).setAuthGroupIds(eq(Collections.<String>emptyList()));

        return null;
      }
    });
  }

  @Test
  public void testTaskQueryWithOneGroupAuthorization() {
    createGroupGrantAuthorization(Resources.TASK, Authorization.ANY, testGroupIds.get(0));

    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        AuthorizationManager authorizationManager = spyOnSession(commandContext, AuthorizationManager.class);

        TaskQueryImpl taskQuery = (TaskQueryImpl) spy(processEngine.getTaskService().createTaskQuery());
        AuthorizationCheck authCheck = spy(new AuthorizationCheck());
        when(taskQuery.getAuthCheck()).thenReturn(authCheck);

        taskQuery.list();

        verify(authorizationManager, atLeastOnce()).filterAuthenticatedGroupIds(eq(testGroupIds));
        verify(authCheck).setAuthGroupIds(eq(testGroupIds.subList(0, 1)));

        return null;
      }
    });
  }

  @Test
  public void testTaskQueryWithGroupAuthorization() {
    for (String testGroupId : testGroupIds) {
      createGroupGrantAuthorization(Resources.TASK, Authorization.ANY, testGroupId);
    }

    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        AuthorizationManager authorizationManager = spyOnSession(commandContext, AuthorizationManager.class);

        TaskQueryImpl taskQuery = (TaskQueryImpl) spy(processEngine.getTaskService().createTaskQuery());
        AuthorizationCheck authCheck = spy(new AuthorizationCheck());
        when(taskQuery.getAuthCheck()).thenReturn(authCheck);

        taskQuery.list();

        verify(authorizationManager, atLeastOnce()).filterAuthenticatedGroupIds(eq(testGroupIds));
        verify(authCheck, atLeastOnce()).setAuthGroupIds((List<String>) argThat(containsInAnyOrder(testGroupIds.toArray())));

        return null;
      }
    });
  }

  @Test
  public void testTaskQueryWithUserWithoutGroups() {
    identityService.setAuthentication(testUserId, null);

    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        AuthorizationManager authorizationManager = spyOnSession(commandContext, AuthorizationManager.class);

        TaskQueryImpl taskQuery = (TaskQueryImpl) spy(processEngine.getTaskService().createTaskQuery());
        AuthorizationCheck authCheck = spy(new AuthorizationCheck());
        when(taskQuery.getAuthCheck()).thenReturn(authCheck);

        taskQuery.list();

        verify(authorizationManager, atLeastOnce()).filterAuthenticatedGroupIds(eq((List<String>) null));
        verify(authCheck).setAuthGroupIds(eq(Collections.<String>emptyList()));

        return null;
      }
    });
  }

  @Test
  public void testCheckAuthorizationWithoutGroupAuthorizations() {
    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        AuthorizationManager authorizationManager = spyOnSession(commandContext, AuthorizationManager.class);
        DbEntityManager dbEntityManager = spyOnSession(commandContext, DbEntityManager.class);

        authorizationService.isUserAuthorized(testUserId, testGroupIds, Permissions.READ, Resources.TASK);

        verify(authorizationManager, atLeastOnce()).filterAuthenticatedGroupIds(eq(testGroupIds));

        ArgumentCaptor<AuthorizationCheck> authorizationCheckArgument = ArgumentCaptor.forClass(AuthorizationCheck.class);
        verify(dbEntityManager).selectBoolean(eq("isUserAuthorizedForResource"), authorizationCheckArgument.capture());

        AuthorizationCheck authorizationCheck = authorizationCheckArgument.getValue();
        assertTrue(authorizationCheck.getAuthGroupIds().isEmpty());

        return null;
      }
    });
  }

  @Test
  public void testCheckAuthorizationWithOneGroupAuthorizations() {
    createGroupGrantAuthorization(Resources.TASK, Authorization.ANY, testGroupIds.get(0));

    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        AuthorizationManager authorizationManager = spyOnSession(commandContext, AuthorizationManager.class);
        DbEntityManager dbEntityManager = spyOnSession(commandContext, DbEntityManager.class);

        authorizationService.isUserAuthorized(testUserId, testGroupIds, Permissions.READ, Resources.TASK);

        verify(authorizationManager, atLeastOnce()).filterAuthenticatedGroupIds(eq(testGroupIds));

        ArgumentCaptor<AuthorizationCheck> authorizationCheckArgument = ArgumentCaptor.forClass(AuthorizationCheck.class);
        verify(dbEntityManager).selectBoolean(eq("isUserAuthorizedForResource"), authorizationCheckArgument.capture());

        AuthorizationCheck authorizationCheck = authorizationCheckArgument.getValue();
        assertEquals(testGroupIds.subList(0, 1), authorizationCheck.getAuthGroupIds());

        return null;
      }
    });
  }

  @Test
  public void testCheckAuthorizationWithGroupAuthorizations() {
    for (String testGroupId : testGroupIds) {
      createGroupGrantAuthorization(Resources.TASK, Authorization.ANY, testGroupId);
    }

    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        AuthorizationManager authorizationManager = spyOnSession(commandContext, AuthorizationManager.class);
        DbEntityManager dbEntityManager = spyOnSession(commandContext, DbEntityManager.class);

        authorizationService.isUserAuthorized(testUserId, testGroupIds, Permissions.READ, Resources.TASK);

        verify(authorizationManager, atLeastOnce()).filterAuthenticatedGroupIds(eq(testGroupIds));

        ArgumentCaptor<AuthorizationCheck> authorizationCheckArgument = ArgumentCaptor.forClass(AuthorizationCheck.class);
        verify(dbEntityManager).selectBoolean(eq("isUserAuthorizedForResource"), authorizationCheckArgument.capture());

        AuthorizationCheck authorizationCheck = authorizationCheckArgument.getValue();
        assertThat(authorizationCheck.getAuthGroupIds()).containsExactlyInAnyOrderElementsOf(testGroupIds);

        return null;
      }
    });
  }

  @Test
  public void testCheckAuthorizationWithUserWithoutGroups() {
    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        AuthorizationManager authorizationManager = spyOnSession(commandContext, AuthorizationManager.class);
        DbEntityManager dbEntityManager = spyOnSession(commandContext, DbEntityManager.class);

        authorizationService.isUserAuthorized(testUserId, null, Permissions.READ, Resources.TASK);

        verify(authorizationManager, atLeastOnce()).filterAuthenticatedGroupIds(eq((List<String>) null));

        ArgumentCaptor<AuthorizationCheck> authorizationCheckArgument = ArgumentCaptor.forClass(AuthorizationCheck.class);
        verify(dbEntityManager).selectBoolean(eq("isUserAuthorizedForResource"), authorizationCheckArgument.capture());

        AuthorizationCheck authorizationCheck = authorizationCheckArgument.getValue();
        assertTrue(authorizationCheck.getAuthGroupIds().isEmpty());

        return null;
      }
    });
  }

  @Test
  public void testCheckAuthorizationForNullHostileListOfGroups() {
    // given
    identityService.clearAuthentication();

    BpmnModelInstance process = Bpmn.createExecutableProcess("process").startEvent()
      .userTask("foo")
      .endEvent()
      .done();

    testRule.deploy(process);

    runtimeService.startProcessInstanceByKey("process");

    // a group authorization
    createGroupGrantAuthorization(Resources.TASK, Authorization.ANY, testGroupIds.get(0));

    // a user authorization (i.e. no group id set)
    // this authorization is important to reproduce the bug in CAM-14306
    createGrantAuthorization(Resources.TASK, Authorization.ANY, testUserId, Permissions.READ);

    List<String> groupIds = new NullHostileList<>(testGroupIds);

    // when
    boolean isAuthorized = authorizationService.isUserAuthorized(testUserId, groupIds, Permissions.READ, Resources.TASK);

    // then
    assertThat(isAuthorized).isTrue();
  }

  protected class NullHostileList<E> extends ArrayList<E> {

    public NullHostileList(Collection<E> other) {
      super(other);
    }

    @Override
    public boolean contains(Object o) {
      // lists that behave similar:
      // List.of (Java 9+) and List.copyOf (Java 10+)
      if (o == null) {
        throw new NullPointerException();
      }
      return super.contains(o);
    }
  }

  protected void createGroupGrantAuthorization(Resource resource, String resourceId, String groupId, Permission... permissions) {
    Authorization authorization = createGrantAuthorization(resource, resourceId);
    authorization.setGroupId(groupId);
    for (Permission permission : permissions) {
      authorization.addPermission(permission);
    }
    saveAuthorization(authorization);
  }

  protected void createGroupAndAddUser(String groupId, String userId) {
    createGroup(groupId);
    identityService.createMembership(userId, groupId);
  }

  protected <T extends Session> T spyOnSession(CommandContext commandContext, Class<T> sessionClass) {
    T manager = commandContext.getSession(sessionClass);
    T spy = spy(manager);
    commandContext.getSessions().put(sessionClass, spy);

    return spy;
  }

}
