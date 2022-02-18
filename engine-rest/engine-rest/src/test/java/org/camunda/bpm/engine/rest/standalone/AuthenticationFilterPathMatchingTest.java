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
package org.camunda.bpm.engine.rest.standalone;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.identity.TenantQuery;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.AuthorizationServiceImpl;
import org.camunda.bpm.engine.impl.IdentityServiceImpl;
import org.camunda.bpm.engine.impl.digest._apacheCommonsCodec.Base64;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.security.auth.ProcessEngineAuthenticationFilter;
import org.camunda.bpm.engine.rest.security.auth.impl.HttpBasicAuthenticationProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(Parameterized.class)
public class AuthenticationFilterPathMatchingTest extends AbstractRestServiceTest {

  protected static final String SERVICE_PATH = TEST_RESOURCE_ROOT_PATH;

  protected AuthorizationService authorizationServiceMock;
  protected IdentityService identityServiceMock;
  protected RepositoryService repositoryServiceMock;

  protected User userMock;
  protected List<String> groupIds;
  protected List<String> tenantIds;

  protected Filter authenticationFilter;

  protected String servletPath;
  protected String requestUrl;
  protected String engineName;
  protected boolean authenticationExpected;

  protected ProcessEngine currentEngine;

  /**
   * Makes a request against the url SERVICE_PATH + 'servletPath' + 'requestUrl' and depending on the 'authenticationExpected' value,
   * asserts that authentication was carried out (or not) against the engine named 'engineName'
   */
  public AuthenticationFilterPathMatchingTest(String servletPath, String requestUrl, String engineName, boolean authenticationExpected) {
    this.servletPath = servletPath;
    this.requestUrl = requestUrl;
    this.engineName = engineName;
    if (engineName == null) {
      this.engineName = "default";
    }
    this.authenticationExpected = authenticationExpected;
  }

  @Parameters
  public static Collection<Object[]> getRequestUrls() {
    return Arrays.asList(new Object[][]{
        {"", "/engine/default/process-definition/and/a/longer/path", "default", true},
        {"", "/engine/default/process-definition/and/a/longer/path", "default", true},
        {"", "/engine/default/process-definition", "default", true},
        {"", "/engine/someOtherEngine/process-definition", "someOtherEngine", true},
        {"", "/engine/default/", "default", true},
        {"", "/engine/default", "default", true},
        {"", "/process-definition", "default", true},
        {"", "/engine", null, false},
        {"", "/engine/", null, false},
        {"", "/", "default", true},
        {"", "", "default", true},
        {"/someservlet", "/engine/someengine/process-definition", "someengine", true}
    });
  }

  @Before
  public void setup() throws ServletException {
    currentEngine = getProcessEngine(engineName);

    authorizationServiceMock = mock(AuthorizationServiceImpl.class);
    identityServiceMock = mock(IdentityServiceImpl.class);
    repositoryServiceMock = mock(RepositoryService.class);

    when(currentEngine.getAuthorizationService()).thenReturn(authorizationServiceMock);
    when(currentEngine.getIdentityService()).thenReturn(identityServiceMock);

    // for authentication
    userMock = MockProvider.createMockUser();

    List<Group> groupMocks = MockProvider.createMockGroups();
    groupIds = setupGroupQueryMock(groupMocks);

    List<Tenant> tenantMocks = Collections.singletonList(MockProvider.createMockTenant());
    tenantIds = setupTenantQueryMock(tenantMocks);

    GroupQuery mockGroupQuery = mock(GroupQuery.class);

    when(identityServiceMock.createGroupQuery()).thenReturn(mockGroupQuery);
    when(mockGroupQuery.groupMember(anyString())).thenReturn(mockGroupQuery);
    when(mockGroupQuery.list()).thenReturn(groupMocks);

    setupFilter();
  }

  protected List<String> setupGroupQueryMock(List<Group> groups) {
    GroupQuery mockGroupQuery = mock(GroupQuery.class);

    when(identityServiceMock.createGroupQuery()).thenReturn(mockGroupQuery);
    when(mockGroupQuery.groupMember(anyString())).thenReturn(mockGroupQuery);
    when(mockGroupQuery.list()).thenReturn(groups);

    List<String> groupIds = new ArrayList<String>();
    for (Group groupMock : groups) {
      groupIds.add(groupMock.getId());
    }
    return groupIds;
  }

  protected List<String> setupTenantQueryMock(List<Tenant> tenants) {
    TenantQuery mockTenantQuery = mock(TenantQuery.class);

    when(identityServiceMock.createTenantQuery()).thenReturn(mockTenantQuery);
    when(mockTenantQuery.userMember(anyString())).thenReturn(mockTenantQuery);
    when(mockTenantQuery.includingGroupsOfUser(anyBoolean())).thenReturn(mockTenantQuery);
    when(mockTenantQuery.list()).thenReturn(tenants);

    List<String> tenantIds = new ArrayList<String>();
    for(Tenant tenant: tenants) {
      tenantIds.add(tenant.getId());
    }
    return tenantIds;
  }

  protected void setupFilter() throws ServletException {
    MockFilterConfig config = new MockFilterConfig();
    config.addInitParameter(ProcessEngineAuthenticationFilter.AUTHENTICATION_PROVIDER_PARAM, HttpBasicAuthenticationProvider.class.getName());
    authenticationFilter = new ProcessEngineAuthenticationFilter();
    authenticationFilter.init(config);
  }

  protected void applyFilter(MockHttpServletRequest request, MockHttpServletResponse response, String username, String password) throws IOException, ServletException {
    String credentials = username + ":" + password;
    request.addHeader("Authorization", "Basic " + new String(Base64.encodeBase64(credentials.getBytes())));
    FilterChain filterChain = new MockFilterChain();

    authenticationFilter.doFilter(request, response, filterChain);
  }

  @Test
  public void testHttpBasicAuthenticationCheck() throws IOException, ServletException {
    if (authenticationExpected) {
      when(identityServiceMock.checkPassword(MockProvider.EXAMPLE_USER_ID, MockProvider.EXAMPLE_USER_PASSWORD)).thenReturn(true);
    }

    MockHttpServletResponse response = new MockHttpServletResponse();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI(SERVICE_PATH + servletPath + requestUrl);
    request.setContextPath(SERVICE_PATH);
    request.setServletPath(servletPath);
    applyFilter(request, response, MockProvider.EXAMPLE_USER_ID, MockProvider.EXAMPLE_USER_PASSWORD);

    Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());

    if (authenticationExpected) {
      verify(identityServiceMock).setAuthentication(MockProvider.EXAMPLE_USER_ID, groupIds, tenantIds);
      verify(identityServiceMock).clearAuthentication();

    } else {
      verify(identityServiceMock, never()).setAuthentication(any(String.class), anyList(), anyList());
      verify(identityServiceMock, never()).clearAuthentication();
    }
  }


}
