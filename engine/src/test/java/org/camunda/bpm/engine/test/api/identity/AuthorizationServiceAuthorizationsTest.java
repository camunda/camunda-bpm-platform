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

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GLOBAL;
import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;
import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_REVOKE;
import static org.camunda.bpm.engine.authorization.Permissions.ALL;
import static org.camunda.bpm.engine.authorization.Permissions.CREATE;
import static org.camunda.bpm.engine.authorization.Permissions.DELETE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Resources.AUTHORIZATION;
import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationTestUtil.assertExceptionInfo;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.MissingAuthorization;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;

/**
 * <p>Ensures authorizations are properly
 * enforced by the {@link AuthorizationService}</p>
 *
 * @author Daniel Meyer
 *
 */
public class AuthorizationServiceAuthorizationsTest extends PluggableProcessEngineTestCase {

  private final static String jonny2 = "jonny2";

  @Override
  protected void tearDown() throws Exception {
    processEngineConfiguration.setAuthorizationEnabled(false);
    cleanupAfterTest();
    super.tearDown();
  }

  public void testCreateAuthorization() {

    // add base permission which allows nobody to create authorizations
    Authorization basePerms = authorizationService.createNewAuthorization(AUTH_TYPE_GLOBAL);
    basePerms.setResource(AUTHORIZATION);
    basePerms.setResourceId(ANY);
    basePerms.addPermission(ALL); // add all then remove 'create'
    basePerms.removePermission(CREATE);
    authorizationService.saveAuthorization(basePerms);

    // now enable authorizations:
    processEngineConfiguration.setAuthorizationEnabled(true);
    identityService.setAuthenticatedUserId(jonny2);

    try {
      // we cannot create another authorization
      authorizationService.createNewAuthorization(AUTH_TYPE_GLOBAL);
      fail("exception expected");

    } catch (AuthorizationException e) {
      assertEquals(1, e.getMissingAuthorizations().size());
      MissingAuthorization info = e.getMissingAuthorizations().get(0);
      assertEquals(jonny2, e.getUserId());
      assertExceptionInfo(CREATE.getName(), AUTHORIZATION.resourceName(), null, info);
    }

    // circumvent auth check to get new transient object
    Authorization authorization = new AuthorizationEntity(AUTH_TYPE_REVOKE);
    authorization.setUserId("someUserId");
    authorization.setResource(Resources.APPLICATION);

    try {
      authorizationService.saveAuthorization(authorization);
      fail("exception expected");

    } catch (AuthorizationException e) {
      assertEquals(1, e.getMissingAuthorizations().size());
      MissingAuthorization info = e.getMissingAuthorizations().get(0);
      assertEquals(jonny2, e.getUserId());
      assertExceptionInfo(CREATE.getName(), AUTHORIZATION.resourceName(), null, info);
    }
  }

  public void testDeleteAuthorization() {

    // create global auth
    Authorization basePerms = authorizationService.createNewAuthorization(AUTH_TYPE_GLOBAL);
    basePerms.setResource(AUTHORIZATION);
    basePerms.setResourceId(ANY);
    basePerms.addPermission(ALL);
    basePerms.removePermission(DELETE); // revoke delete
    authorizationService.saveAuthorization(basePerms);

    // turn on authorization
    processEngineConfiguration.setAuthorizationEnabled(true);
    identityService.setAuthenticatedUserId(jonny2);

    try {
      // try to delete authorization
      authorizationService.deleteAuthorization(basePerms.getId());
      fail("exception expected");

    } catch (AuthorizationException e) {
      assertEquals(1, e.getMissingAuthorizations().size());
      MissingAuthorization info = e.getMissingAuthorizations().get(0);
      assertEquals(jonny2, e.getUserId());
      assertExceptionInfo(DELETE.getName(), AUTHORIZATION.resourceName(), basePerms.getId(), info);
    }
  }

  public void testUserUpdateAuthorizations() {

    // create global auth
    Authorization basePerms = authorizationService.createNewAuthorization(AUTH_TYPE_GLOBAL);
    basePerms.setResource(AUTHORIZATION);
    basePerms.setResourceId(ANY);
    basePerms.addPermission(ALL);
    basePerms.removePermission(UPDATE); // revoke update
    authorizationService.saveAuthorization(basePerms);

    // turn on authorization
    processEngineConfiguration.setAuthorizationEnabled(true);
    identityService.setAuthenticatedUserId(jonny2);

    // fetch authhorization
    basePerms = authorizationService.createAuthorizationQuery().singleResult();
    // make some change to the perms
    basePerms.addPermission(ALL);

    try {
      authorizationService.saveAuthorization(basePerms);
      fail("exception expected");

    } catch (AuthorizationException e) {
      assertEquals(1, e.getMissingAuthorizations().size());
      MissingAuthorization info = e.getMissingAuthorizations().get(0);
      assertEquals(jonny2, e.getUserId());
      assertExceptionInfo(UPDATE.getName(), AUTHORIZATION.resourceName(), basePerms.getId(), info);
    }

    // but we can create a new auth
    Authorization newAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    newAuth.setUserId("jonny2");
    newAuth.setResource(AUTHORIZATION);
    newAuth.setResourceId(ANY);
    newAuth.addPermission(ALL);
    authorizationService.saveAuthorization(newAuth);

  }

  public void testAuthorizationQueryAuthorizations() {

    // we are jonny2
    String authUserId = "jonny2";
    identityService.setAuthenticatedUserId(authUserId);

    // create new auth wich revokes read access on auth
    Authorization basePerms = authorizationService.createNewAuthorization(AUTH_TYPE_GLOBAL);
    basePerms.setResource(AUTHORIZATION);
    basePerms.setResourceId(ANY);
    authorizationService.saveAuthorization(basePerms);

    // I can see it
    assertEquals(1, authorizationService.createAuthorizationQuery().count());

    // now enable checks
    processEngineConfiguration.setAuthorizationEnabled(true);

    // I can't see it
    assertEquals(0, authorizationService.createAuthorizationQuery().count());

  }

  protected void cleanupAfterTest() {
    for (Authorization authorization : authorizationService.createAuthorizationQuery().list()) {
      authorizationService.deleteAuthorization(authorization.getId());
    }
  }

}
