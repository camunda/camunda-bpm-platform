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
package org.camunda.bpm.webapp.impl.security.auth;

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
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

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
    setAuthentication("jonny", "UserAuthenticationResourceTest-engine");

    // when
    UserAuthenticationResource authResource = new UserAuthenticationResource();
    Response response = authResource.doLogin("UserAuthenticationResourceTest-engine", "tasklist", "jonny", "jonnyspassword");

    // then
    Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void testAuthorizationCheckNotGranted() {
    // given
    User jonny = identityService.newUser("jonny");
    jonny.setPassword("jonnyspassword");
    identityService.saveUser(jonny);

    processEngineConfiguration.setAuthorizationEnabled(true);
    setAuthentication("jonny", "UserAuthenticationResourceTest-engine");

    // when
    UserAuthenticationResource authResource = new UserAuthenticationResource();
    Response response = authResource.doLogin("UserAuthenticationResourceTest-engine", "tasklist", "jonny", "jonnyspassword");

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
    setAuthentication("jonny", "UserAuthenticationResourceTest-engine");

    // when
    UserAuthenticationResource authResource = new UserAuthenticationResource();
    Response response = authResource.doLogin("UserAuthenticationResourceTest-engine", "tasklist", "jonny", "jonnyspassword");

    // then
    Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
  }

  protected void setAuthentication(String user, String engineName) {
    Authentications authentications = new Authentications();
    authentications.addAuthentication(new Authentication("jonny", "UserAuthenticationResourceTest-engine"));
    Authentications.setCurrent(authentications);
  }

  protected void clearAuthentication() {
    Authentications.clearCurrent();
  }


}
