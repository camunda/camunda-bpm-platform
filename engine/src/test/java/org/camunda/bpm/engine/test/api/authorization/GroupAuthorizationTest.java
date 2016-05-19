/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
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
import org.mockito.ArgumentCaptor;

public class GroupAuthorizationTest extends AuthorizationTest {

  public static final String testUserId = "testUser";
  public static final List<String> testGroupIds = Arrays.asList("testGroup1", "testGroup2", "testGroup3");

  @Override
  protected void setUp() throws Exception {
    createUser(testUserId);
    for (String testGroupId : testGroupIds) {
      createGroupAndAddUser(testGroupId, testUserId);
    }

    identityService.setAuthentication(testUserId, testGroupIds);
    processEngineConfiguration.setAuthorizationEnabled(true);
  }


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
        assertThat(authorizationCheck.getAuthGroupIds(), containsInAnyOrder(testGroupIds.toArray()));

        return null;
      }
    });
  }

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
