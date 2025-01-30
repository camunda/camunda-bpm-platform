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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

import java.util.Date;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.webapp.impl.util.ServletContextUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Thorben Lindhauer
 *
 */
public class UserAuthenticationResourceTest {

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule("camunda-test-engine.cfg.xml");

  protected ProcessEngine processEngine;
  protected ProcessEngineConfiguration processEngineConfiguration;
  protected IdentityService identityService;
  protected AuthorizationService authorizationService;

  @Before
  public void setUp() {
    this.processEngine = processEngineRule.getProcessEngine();
    this.processEngineConfiguration = processEngine.getProcessEngineConfiguration();
    this.identityService = processEngine.getIdentityService();
    this.authorizationService = processEngine.getAuthorizationService();
  }

  @After
  public void tearDown() {
    ClockUtil.reset();
    processEngineConfiguration.setAuthorizationEnabled(false);

    for (User user : identityService.createUserQuery().list()) {
      identityService.deleteUser(user.getId());
    }
    for (Authorization authorization : authorizationService.createAuthorizationQuery().list()) {
      authorizationService.deleteAuthorization(authorization.getId());
    }

    clearAuthentication();
  }

  @Test
  public void testAuthorizationCheckGranted() {
    // given
    User jonny = identityService.newUser("jonny");
    jonny.setPassword("jonnyspassword");
    identityService.saveUser(jonny);

    Authorization authorization = authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
    authorization.setResource(Resources.APPLICATION);
    authorization.setResourceId("tasklist");
    authorization.setPermissions(new Permissions[] {Permissions.ACCESS});
    authorization.setUserId(jonny.getId());
    authorizationService.saveAuthorization(authorization);

    processEngineConfiguration.setAuthorizationEnabled(true);
    setAuthentication("jonny", "webapps-test-engine");

    // when
    UserAuthenticationResource authResource = new UserAuthenticationResource();
    authResource.request = new MockHttpServletRequest();
    Response response = authResource.doLogin("webapps-test-engine", "tasklist", "jonny", "jonnyspassword");

    // then
    Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void testSessionRevalidationOnAuthorization() {
    // given
    User jonny = identityService.newUser("jonny");
    jonny.setPassword("jonnyspassword");
    identityService.saveUser(jonny);

    Authorization authorization = authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
    authorization.setResource(Resources.APPLICATION);
    authorization.setResourceId("tasklist");
    authorization.setPermissions(new Permissions[] {Permissions.ACCESS});
    authorization.setUserId(jonny.getId());
    authorizationService.saveAuthorization(authorization);

    processEngineConfiguration.setAuthorizationEnabled(true);
    setAuthentication("jonny", "webapps-test-engine");

    // when
    UserAuthenticationResource authResource = new UserAuthenticationResource();
    authResource.request = new MockHttpServletRequest();
    String oldSessionId = authResource.request.getSession().getId();

    // first login session
    Response response = authResource.doLogin("webapps-test-engine", "tasklist", "jonny", "jonnyspassword");
    String newSessionId = authResource.request.getSession().getId();

    authResource.doLogout("webapps-test-engine");

    // second login session
    response = authResource.doLogin("webapps-test-engine", "tasklist", "jonny", "jonnyspassword");
    String newestSessionId = authResource.request.getSession().getId();

    // then
    Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
    Assert.assertNotEquals(oldSessionId, newSessionId);
    Assert.assertNotEquals(newSessionId, newestSessionId);
  }

  @Test
  public void testAuthorizationCheckNotGranted() {
    // given
    User jonny = identityService.newUser("jonny");
    jonny.setPassword("jonnyspassword");
    identityService.saveUser(jonny);

    processEngineConfiguration.setAuthorizationEnabled(true);
    setAuthentication("jonny", "webapps-test-engine");

    // when
    UserAuthenticationResource authResource = new UserAuthenticationResource();
    authResource.request = new MockHttpServletRequest();
    Response response = authResource.doLogin("webapps-test-engine", "tasklist", "jonny", "jonnyspassword");

    // then
    Assert.assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());
  }

  @Test
  public void testAuthorizationCheckDeactivated() {
    // given
    User jonny = identityService.newUser("jonny");
    jonny.setPassword("jonnyspassword");
    identityService.saveUser(jonny);

    processEngineConfiguration.setAuthorizationEnabled(false);
    setAuthentication("jonny", "webapps-test-engine");

    // when
    UserAuthenticationResource authResource = new UserAuthenticationResource();
    authResource.request = new MockHttpServletRequest();
    Response response = authResource.doLogin("webapps-test-engine", "tasklist", "jonny", "jonnyspassword");

    // then
    Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldSetAuthCacheValidationTime() {
    // given
    ClockUtil.setCurrentTime(ClockUtil.getCurrentTime());
    User jonny = identityService.newUser("jonny");
    jonny.setPassword("jonnyspassword");
    identityService.saveUser(jonny);

    MockHttpServletRequest request = new MockHttpServletRequest();
    ServletContextUtil.setCacheTTLForLogin(1000 * 60 * 5, request.getServletContext());

    // when
    UserAuthenticationResource authResource = new UserAuthenticationResource();
    authResource.request = request;
    authResource.doLogin("webapps-test-engine", "tasklist", "jonny", "jonnyspassword");

    // then
    UserAuthentication userAuthentication = AuthenticationUtil.getAuthsFromSession(request.getSession())
      .getAuthentications()
      .get(0);
    assertThat(userAuthentication.getCacheValidationTime())
      .isEqualTo(new Date(ClockUtil.getCurrentTime().getTime() + 1000 * 60 * 5));
  }

  @Test
  public void shouldReturnUnauthorizedOnNullAuthentication() {
    // given
    User jonny = identityService.newUser("jonny");
    jonny.setPassword("jonnyspassword");
    identityService.saveUser(jonny);
    UserAuthenticationResource authResource = new UserAuthenticationResource();
    authResource.request = new MockHttpServletRequest();

    try (MockedStatic<AuthenticationUtil> authenticationUtilMock = mockStatic(AuthenticationUtil.class)) {
      authenticationUtilMock.when(() -> AuthenticationUtil.createAuthentication("webapps-test-engine", "jonny")).thenReturn(null);

      // when
      Response response = authResource.doLogin("webapps-test-engine", "tasklist", "jonny", "jonnyspassword");

      // then
      Assert.assertEquals(Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }
  }

  protected void setAuthentication(String user, String engineName) {
    Authentications authentications = new Authentications();
    authentications.addOrReplace(new UserAuthentication(user, engineName));
    Authentications.setCurrent(authentications);
  }

  protected void clearAuthentication() {
    Authentications.clearCurrent();
  }


}
