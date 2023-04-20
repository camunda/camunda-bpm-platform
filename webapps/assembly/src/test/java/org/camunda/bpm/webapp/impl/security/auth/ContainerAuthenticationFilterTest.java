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
package org.camunda.bpm.webapp.impl.security.auth;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.identity.TenantQuery;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.identity.UserQuery;
import org.camunda.bpm.engine.impl.AuthorizationServiceImpl;
import org.camunda.bpm.engine.impl.IdentityServiceImpl;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.security.auth.ProcessEngineAuthenticationFilter;
import org.camunda.bpm.engine.rest.security.auth.impl.ContainerBasedAuthenticationProvider;
import org.camunda.bpm.engine.rest.spi.ProcessEngineProvider;
import org.camunda.bpm.engine.rest.spi.impl.MockedProcessEngineProvider;
import org.camunda.bpm.webapp.impl.util.ProcessEngineUtil;
import org.camunda.bpm.webapp.impl.util.ServletContextUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

@RunWith(Parameterized.class)
public class ContainerAuthenticationFilterTest {

  protected static final String SERVICE_PATH = "/camunda";

  private Authentications authentications;

  protected AuthorizationService authorizationService;
  protected IdentityService identityService;

  protected Filter authenticationFilter;

  protected String requestUrl;
  protected String engineName;
  protected boolean alreadyAuthenticated;
  protected boolean authenticationExpected;

  protected ProcessEngine currentEngine;

  private MockedStatic<AuthenticationUtil> authenticationUtilMockedStatic;
  private MockedStatic<ProcessEngineUtil> processEngineUtilMockedStatic;

  public ContainerAuthenticationFilterTest(String requestUrl, String engineName, boolean alreadyAuthenticated, boolean authenticationExpected) {
    this.requestUrl = requestUrl;
    this.engineName = engineName;
    if (engineName == null) {
      this.engineName = "default";
    }
    this.alreadyAuthenticated = alreadyAuthenticated;
    this.authenticationExpected = authenticationExpected;
  }

  @Parameters
  public static Collection<Object[]> getRequestUrls() {
    return Arrays.asList(new Object[][]{
        {"/app/cockpit/default/", "default", false, true},
        {"/app/cockpit/engine2/", "engine2", false, true},
        {"/api/cockpit/plugin/some-plugin/default/process-instance", "default", false, true},
        {"/api/cockpit/plugin/some-plugin/static/process-instance", null, false, false},

        {"/app/cockpit/default/", "default", true, false},
        {"/app/cockpit/engine2/", "engine2", true, false},
        {"/api/cockpit/plugin/some-plugin/default/process-instance", "default", true, false},
        {"/api/cockpit/plugin/some-plugin/static/process-instance", null, true, false},

        {"/app/tasklist/default/", "default", false, true},
        {"/app/tasklist/engine2/", "engine2", false, true},
        {"/api/tasklist/plugin/some-plugin/default/process-instance", "default", false, true},
        {"/api/tasklist/plugin/some-plugin/static/process-instance", null, false, false},

        {"/app/tasklist/default/", "default", true, false},
        {"/app/tasklist/engine2/", "engine2", true, false},
        {"/api/tasklist/plugin/some-plugin/default/process-instance", "default", true, false},
        {"/api/tasklist/plugin/some-plugin/static/process-instance", null, true, false},

        {"/app/admin/default/", "default", false, true},
        {"/app/admin/engine2/", "engine2", false, true},
        {"/api/admin/plugin/some-plugin/default/process-instance", "default", false, true},
        {"/api/admin/plugin/some-plugin/static/process-instance", null, false, false},

        {"/app/admin/default/", "default", true, false},
        {"/app/admin/engine2/", "engine2", true, false},
        {"/api/admin/plugin/some-plugin/default/process-instance", "default", true, false},
        {"/api/admin/plugin/some-plugin/static/process-instance", null, true, false},

        {"/app/welcome/default/", "default", false, true},
        {"/app/welcome/engine2/", "engine2", false, true},
        {"/api/welcome/plugin/some-plugin/default/process-instance", "default", false, true},
        {"/api/welcome/plugin/some-plugin/static/process-instance", null, false, false},

        {"/api/engine/engine/default/process-instance", "default", false, true},
        {"/api/engine/engine/engine2/process-instance", "engine2", false, true},

        {"/api/engine/engine/default/process-instance", "default", true, false},
        {"/api/engine/engine/engine2/process-instance", "engine2", true, false},

        {"/lib/deps.js", null, false, false},
        {"/app/cockpit/styles/styles.css", null, false, false},
        {"/api/admin/auth/user/default", null, false, false},

        {"/lib/deps.js", null, true, false},
        {"/app/cockpit/styles/styles.css", null, true, false},
        {"/api/admin/auth/user/default", null, true, false}
    });
  }

  @Before
  public void setup() throws ServletException {
    setupProcessEngine();
    setupAuthentications();
    setupFilter();
  }

  protected void setupProcessEngine() {
    final ProcessEngineProvider provider = new MockedProcessEngineProvider();
    currentEngine = provider.getProcessEngine(engineName);

    authorizationService = mock(AuthorizationServiceImpl.class);
    identityService = mock(IdentityServiceImpl.class);

    when(currentEngine.getAuthorizationService()).thenReturn(authorizationService);
    when(currentEngine.getIdentityService()).thenReturn(identityService);
    processEngineUtilMockedStatic = mockStatic(ProcessEngineUtil.class);
    processEngineUtilMockedStatic.when(() -> ProcessEngineUtil.lookupProcessEngine(any())).thenReturn(currentEngine);

    User mockedUser = mock(User.class);
    when(mockedUser.getId()).thenReturn(MockProvider.EXAMPLE_USER_ID);

    UserQuery mockUserQuery = mock(UserQuery.class);
    when(identityService.createUserQuery()).thenReturn(mockUserQuery);
    when(mockUserQuery.userId(any())).thenReturn(mockUserQuery);
    when(mockUserQuery.singleResult()).thenReturn(mockedUser);

    GroupQuery mockGroupQuery = mock(GroupQuery.class);
    when(identityService.createGroupQuery()).thenReturn(mockGroupQuery);
    when(mockGroupQuery.groupMember(any())).thenReturn(mockGroupQuery);
    when(mockGroupQuery.list()).thenReturn(new ArrayList<Group>());

    TenantQuery mockTenantQuery = mock(TenantQuery.class);
    when(identityService.createTenantQuery()).thenReturn(mockTenantQuery);
    when(mockTenantQuery.userMember(any())).thenReturn(mockTenantQuery);
    when(mockTenantQuery.includingGroupsOfUser(anyBoolean())).thenReturn(mockTenantQuery);
    when(mockTenantQuery.list()).thenReturn(new ArrayList<Tenant>());
  }

  protected void setupAuthentications() {
    Authentications.clearCurrent();
    authenticationUtilMockedStatic = mockStatic(AuthenticationUtil.class);
    authentications = mock(Authentications.class);
    authenticationUtilMockedStatic.when(() -> AuthenticationUtil.getAuthsFromSession(any())).thenReturn(authentications);
    UserAuthentication authentication = mock(UserAuthentication.class);
    authenticationUtilMockedStatic
      .when(() -> AuthenticationUtil.createAuthentication(any(ProcessEngine.class), any(), any(), any()))
      .thenReturn(authentication);
  }

  protected void setupFilter() throws ServletException {
    MockFilterConfig config = new MockFilterConfig();
    config.addInitParameter(ProcessEngineAuthenticationFilter.AUTHENTICATION_PROVIDER_PARAM, ContainerBasedAuthenticationProvider.class.getName());
    authenticationFilter = new ContainerBasedAuthenticationFilter();
    authenticationFilter.init(config);
  }

  protected void applyFilter(MockHttpServletRequest request, MockHttpServletResponse response, String username) throws IOException, ServletException {
    Principal principal = mock(Principal.class);
    when(principal.getName()).thenReturn(username);
    request.setUserPrincipal(principal);
    request.setMethod("GET");
    FilterChain filterChain = new MockFilterChain();
    authenticationFilter.doFilter(request, response, filterChain);
  }

  @After
  public void teardown() {
    authenticationUtilMockedStatic.close();
    processEngineUtilMockedStatic.close();
  }

  @Test
  public void shouldCheckCustomApplicationPath() throws IOException, ServletException {
    testContainerAuthenticationCheck("/my-custom/application/path");
  }

  @Test
  public void shouldCheckEmptyApplicationPath() throws IOException, ServletException {
    testContainerAuthenticationCheck("");
  }

  public void testContainerAuthenticationCheck(String applicationPath) throws IOException, ServletException {
    if (alreadyAuthenticated) {
      Authentication authentication = mock(Authentication.class);
      when(authentication.getProcessEngineName()).thenReturn(engineName);
      when(authentication.getIdentityId()).thenReturn(MockProvider.EXAMPLE_USER_ID);

      when(authentications.getAuthenticationForProcessEngine(anyString())).thenReturn(authentication);
    }
    else {
      when(authentications.getAuthenticationForProcessEngine(anyString())).thenReturn(null);
    }

    MockHttpServletResponse response = new MockHttpServletResponse();
    MockHttpServletRequest request = null;

    if (!applicationPath.isEmpty()) {
      MockServletContext mockServletContext = new MockServletContext();
      request = new MockHttpServletRequest(mockServletContext);
      requestUrl = applicationPath + requestUrl;
      ServletContextUtil.setAppPath(applicationPath, mockServletContext);

    } else {
      request = new MockHttpServletRequest();

    }

    request.setRequestURI(SERVICE_PATH  + requestUrl);
    request.setContextPath(SERVICE_PATH);
    applyFilter(request, response, MockProvider.EXAMPLE_USER_ID);

    Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());

    if (authenticationExpected) {
      verify(authentications).addOrReplace(any(UserAuthentication.class));

    } else {
      verify(authentications, never()).addOrReplace(any(UserAuthentication.class));
    }
  }
}

