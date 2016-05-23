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
package org.camunda.bpm.engine.test.api.cfg;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.AuthorizationCheck;
import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Daniel Meyer
 *
 */
public class AuthorizationCheckRevokesCfgTest {

  private static final List<String> AUTHENTICATED_GROUPS = Arrays.asList("aGroup");
  private static final String AUTHENTICATED_USER_ID = "userId";

  CommandContext mockedCmdContext;
  ProcessEngineConfigurationImpl mockedConfiguration;
  AuthorizationManager authorizationManager;
  DbEntityManager mockedEntityManager;

  @Before
  public void setup() {

    mockedCmdContext = mock(CommandContext.class);
    mockedConfiguration = mock(ProcessEngineConfigurationImpl.class);
    authorizationManager = spy(new AuthorizationManager());
    mockedEntityManager = mock(DbEntityManager.class);

    when(mockedCmdContext.getSession(eq(DbEntityManager.class))).thenReturn(mockedEntityManager);

    when(authorizationManager.filterAuthenticatedGroupIds(eq(AUTHENTICATED_GROUPS))).thenReturn(AUTHENTICATED_GROUPS);
    when(mockedCmdContext.getAuthentication()).thenReturn(new Authentication(AUTHENTICATED_USER_ID, AUTHENTICATED_GROUPS));
    when(mockedCmdContext.isAuthorizationCheckEnabled()).thenReturn(true);
    when(mockedConfiguration.isAuthorizationEnabled()).thenReturn(true);

    Context.setCommandContext(mockedCmdContext);
    Context.setProcessEngineConfiguration(mockedConfiguration);
  }

  @After
  public void cleanup() {
    Context.removeCommandContext();
    Context.removeProcessEngineConfiguration();
  }

  @Test
  public void shouldUseCfgValue_always() {
    final ListQueryParameterObject query = new ListQueryParameterObject();
    final AuthorizationCheck authCheck = query.getAuthCheck();

    // given
    when(mockedConfiguration.getAuthorizationCheckRevokes()).thenReturn(ProcessEngineConfiguration.AUTHORIZATION_CHECK_REVOKE_ALWAYS);

    // if
    authorizationManager.configureQuery(query);

    // then
    assertEquals(true, authCheck.isRevokeAuthorizationCheckEnabled());
    verifyNoMoreInteractions(mockedEntityManager);
  }

  @Test
  public void shouldUseCfgValue_never() {
    final ListQueryParameterObject query = new ListQueryParameterObject();
    final AuthorizationCheck authCheck = query.getAuthCheck();

    // given
    when(mockedConfiguration.getAuthorizationCheckRevokes()).thenReturn(ProcessEngineConfiguration.AUTHORIZATION_CHECK_REVOKE_NEVER);

    // if
    authorizationManager.configureQuery(query);

    // then
    assertEquals(false, authCheck.isRevokeAuthorizationCheckEnabled());
    verify(mockedEntityManager, never()).selectBoolean(eq("selectRevokeAuthorization"), any());
    verifyNoMoreInteractions(mockedEntityManager);
  }

  @Test
  public void shouldCheckDbForCfgValue_auto() {
    final ListQueryParameterObject query = new ListQueryParameterObject();
    final AuthorizationCheck authCheck = query.getAuthCheck();

    final HashMap<String, Object> expectedQueryParams = new HashMap<String, Object>();
    expectedQueryParams.put("userId", AUTHENTICATED_USER_ID);
    expectedQueryParams.put("authGroupIds", AUTHENTICATED_GROUPS);

    // given
    when(mockedConfiguration.getAuthorizationCheckRevokes()).thenReturn(ProcessEngineConfiguration.AUTHORIZATION_CHECK_REVOKE_AUTO);
    when(mockedEntityManager.selectBoolean(eq("selectRevokeAuthorization"), eq(expectedQueryParams))).thenReturn(true);

    // if
    authorizationManager.configureQuery(query);

    // then
    assertEquals(true, authCheck.isRevokeAuthorizationCheckEnabled());
    verify(mockedEntityManager, times(1)).selectBoolean(eq("selectRevokeAuthorization"), eq(expectedQueryParams));
  }

  @Test
  public void shouldCheckDbForCfgValueWithNoRevokes_auto() {
    final ListQueryParameterObject query = new ListQueryParameterObject();
    final AuthorizationCheck authCheck = query.getAuthCheck();

    final HashMap<String, Object> expectedQueryParams = new HashMap<String, Object>();
    expectedQueryParams.put("userId", AUTHENTICATED_USER_ID);
    expectedQueryParams.put("authGroupIds", AUTHENTICATED_GROUPS);

    // given
    when(mockedConfiguration.getAuthorizationCheckRevokes()).thenReturn(ProcessEngineConfiguration.AUTHORIZATION_CHECK_REVOKE_AUTO);
    when(mockedEntityManager.selectBoolean(eq("selectRevokeAuthorization"), eq(expectedQueryParams))).thenReturn(false);

    // if
    authorizationManager.configureQuery(query);

    // then
    assertEquals(false, authCheck.isRevokeAuthorizationCheckEnabled());
    verify(mockedEntityManager, times(1)).selectBoolean(eq("selectRevokeAuthorization"), eq(expectedQueryParams));
  }

  @Test
  public void shouldCheckDbForCfgCaseInsensitive() {
    final ListQueryParameterObject query = new ListQueryParameterObject();
    final AuthorizationCheck authCheck = query.getAuthCheck();

    final HashMap<String, Object> expectedQueryParams = new HashMap<String, Object>();
    expectedQueryParams.put("userId", AUTHENTICATED_USER_ID);
    expectedQueryParams.put("authGroupIds", AUTHENTICATED_GROUPS);

    // given
    when(mockedConfiguration.getAuthorizationCheckRevokes()).thenReturn("AuTo");
    when(mockedEntityManager.selectBoolean(eq("selectRevokeAuthorization"), eq(expectedQueryParams))).thenReturn(true);

    // if
    authorizationManager.configureQuery(query);

    // then
    assertEquals(true, authCheck.isRevokeAuthorizationCheckEnabled());
    verify(mockedEntityManager, times(1)).selectBoolean(eq("selectRevokeAuthorization"), eq(expectedQueryParams));
  }

  @Test
  public void shouldCacheCheck() {
    final ListQueryParameterObject query = new ListQueryParameterObject();
    final AuthorizationCheck authCheck = query.getAuthCheck();

    final HashMap<String, Object> expectedQueryParams = new HashMap<String, Object>();
    expectedQueryParams.put("userId", AUTHENTICATED_USER_ID);
    expectedQueryParams.put("authGroupIds", AUTHENTICATED_GROUPS);

    // given
    when(mockedConfiguration.getAuthorizationCheckRevokes()).thenReturn(ProcessEngineConfiguration.AUTHORIZATION_CHECK_REVOKE_AUTO);
    when(mockedEntityManager.selectBoolean(eq("selectRevokeAuthorization"), eq(expectedQueryParams))).thenReturn(true);

    // if
    authorizationManager.configureQuery(query);
    authorizationManager.configureQuery(query);

    // then
    assertEquals(true, authCheck.isRevokeAuthorizationCheckEnabled());
    verify(mockedEntityManager, times(1)).selectBoolean(eq("selectRevokeAuthorization"), eq(expectedQueryParams));
  }

  @Test
  public void testAutoIsDefault() {
    assertEquals(ProcessEngineConfiguration.AUTHORIZATION_CHECK_REVOKE_AUTO, new StandaloneProcessEngineConfiguration().getAuthorizationCheckRevokes());
  }

}
