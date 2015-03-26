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
package org.camunda.bpm.engine.test.api.identity;

import static org.camunda.bpm.engine.authorization.Authorization.*;
import static org.camunda.bpm.engine.authorization.Resources.*;
import static org.camunda.bpm.engine.authorization.Permissions.*;

import java.util.List;

import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.impl.cfg.auth.DefaultAuthorizationProvider;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;

/**
 * <p>Test authorizations provided by {@link DefaultAuthorizationProvider}</p>
 *
 * @author Daniel Meyer
 *
 */
public class DefaultAuthorizationProviderTest extends PluggableProcessEngineTestCase {

  protected void setUp() throws Exception {
    // we are jonny
    identityService.setAuthenticatedUserId("jonny");
    // make sure we can do stuff:
    Authorization jonnyIsGod = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    jonnyIsGod.setUserId("jonny");
    jonnyIsGod.setResource(USER);
    jonnyIsGod.setResourceId(ANY);
    jonnyIsGod.addPermission(ALL);
    authorizationService.saveAuthorization(jonnyIsGod);

    jonnyIsGod = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    jonnyIsGod.setUserId("jonny");
    jonnyIsGod.setResource(GROUP);
    jonnyIsGod.setResourceId(ANY);
    jonnyIsGod.addPermission(ALL);
    authorizationService.saveAuthorization(jonnyIsGod);

    jonnyIsGod = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    jonnyIsGod.setUserId("jonny");
    jonnyIsGod.setResource(AUTHORIZATION);
    jonnyIsGod.setResourceId(ANY);
    jonnyIsGod.addPermission(ALL);
    authorizationService.saveAuthorization(jonnyIsGod);

    // enable authorizations
    processEngineConfiguration.setAuthorizationEnabled(true);
    super.setUp();
  }

  protected void tearDown() throws Exception {
    processEngineConfiguration.setAuthorizationEnabled(false);
    List<Authorization> jonnysAuths = authorizationService.createAuthorizationQuery().userIdIn("jonny").list();
    for (Authorization authorization : jonnysAuths) {
      authorizationService.deleteAuthorization(authorization.getId());
    }
    super.tearDown();
  }

  public void testCreateUser() {
    // initially there are no authorizations for jonny2:
    assertEquals(0, authorizationService.createAuthorizationQuery().userIdIn("jonny2").count());

    // create new user
    identityService.saveUser(identityService.newUser("jonny2"));

    // now there is an authorization for jonny2 which grants him ALL permissions on himself
    Authorization authorization = authorizationService.createAuthorizationQuery().userIdIn("jonny2").singleResult();
    assertNotNull(authorization);
    assertEquals(AUTH_TYPE_GRANT, authorization.getAuthorizationType());
    assertEquals(USER.resourceType(), authorization.getResourceType());
    assertEquals("jonny2", authorization.getResourceId());
    assertTrue(authorization.isPermissionGranted(ALL));

    // delete the user
    identityService.deleteUser("jonny2");

    // the authorization is deleted as well:
    assertEquals(0, authorizationService.createAuthorizationQuery().userIdIn("jonny2").count());
  }

  public void testCreateGroup() {
    // initially there are no authorizations for group "sales":
    assertEquals(0, authorizationService.createAuthorizationQuery().groupIdIn("sales").count());

    // create new group
    identityService.saveGroup(identityService.newGroup("sales"));

    // now there is an authorization for sales which grants all members READ permissions
    Authorization authorization = authorizationService.createAuthorizationQuery().groupIdIn("sales").singleResult();
    assertNotNull(authorization);
    assertEquals(AUTH_TYPE_GRANT, authorization.getAuthorizationType());
    assertEquals(GROUP.resourceType(), authorization.getResourceType());
    assertEquals("sales", authorization.getResourceId());
    assertTrue(authorization.isPermissionGranted(READ));

    // delete the group
    identityService.deleteGroup("sales");

    // the authorization is deleted as well:
    assertEquals(0, authorizationService.createAuthorizationQuery().groupIdIn("sales").count());
  }

}
