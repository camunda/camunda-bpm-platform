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
package org.camunda.bpm.identity.impl.ldap;

import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Resources.GROUP;
import static org.camunda.bpm.engine.authorization.Resources.USER;

import java.util.List;

import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.test.ResourceProcessEngineTestCase;

/**
 * @author Roman Smirnov
 *
 */
public class LdapDisableAuthorizationCheckTest extends ResourceProcessEngineTestCase {

  public LdapDisableAuthorizationCheckTest() {
    super("camunda.ldap.disable.authorization.check.cfg.xml");
  }

  protected static LdapTestEnvironment ldapTestEnvironment;

  protected void setUp() throws Exception {
    if(ldapTestEnvironment == null) {
      ldapTestEnvironment = new LdapTestEnvironment();
      ldapTestEnvironment.init();
    }
    super.setUp();
  }

  protected void tearDown() throws Exception {
    if(ldapTestEnvironment != null) {
      ldapTestEnvironment.shutdown();
      ldapTestEnvironment = null;
    }
    super.tearDown();
  }

  public void testUserQueryPagination() {
    List<User> users = identityService.createUserQuery().listPage(0, 2);
    assertEquals(2, users.size());

    assertEquals("roman", users.get(0).getId());
    assertEquals("robert", users.get(1).getId());

    users = identityService.createUserQuery().listPage(2, 2);
    assertEquals(2, users.size());

    assertEquals("daniel", users.get(0).getId());
    assertEquals("oscar", users.get(1).getId());

    users = identityService.createUserQuery().listPage(4, 2);
    assertEquals(2, users.size());

    assertEquals("monster", users.get(0).getId());
    assertEquals("david(IT)", users.get(1).getId());

    users = identityService.createUserQuery().listPage(6, 2);
    assertEquals(2, users.size());

    assertEquals("ruecker", users.get(0).getId());
    assertEquals("fozzie", users.get(1).getId());

    users = identityService.createUserQuery().listPage(8, 2);
    assertEquals(0, users.size());
  }

  public void testUserQueryPaginationWithAuthenticatedUserWithoutAuthorizations() {
    try {
      processEngineConfiguration.setAuthorizationEnabled(true);

      identityService.setAuthenticatedUserId("oscar");

      List<User> users = identityService.createUserQuery().listPage(0, 2);
      assertEquals(2, users.size());

      assertEquals("roman", users.get(0).getId());
      assertEquals("robert", users.get(1).getId());

      users = identityService.createUserQuery().listPage(2, 2);
      assertEquals(2, users.size());

      assertEquals("daniel", users.get(0).getId());
      assertEquals("oscar", users.get(1).getId());

      users = identityService.createUserQuery().listPage(4, 2);
      assertEquals(2, users.size());

      assertEquals("monster", users.get(0).getId());
      assertEquals("david(IT)", users.get(1).getId());

      users = identityService.createUserQuery().listPage(6, 2);
      assertEquals(2, users.size());

      assertEquals("ruecker", users.get(0).getId());
      assertEquals("fozzie", users.get(1).getId());

      users = identityService.createUserQuery().listPage(8, 2);
      assertEquals(0, users.size());

    } finally {
      processEngineConfiguration.setAuthorizationEnabled(false);
      identityService.clearAuthentication();
    }
  }

  public void testUserQueryPaginationWithAuthenticatedUserWithAuthorizations() {
    createGrantAuthorization(USER, "roman", "oscar", READ);
    createGrantAuthorization(USER, "daniel", "oscar", READ);
    createGrantAuthorization(USER, "monster", "oscar", READ);
    createGrantAuthorization(USER, "ruecker", "oscar", READ);

    try {
      processEngineConfiguration.setAuthorizationEnabled(true);

      identityService.setAuthenticatedUserId("oscar");

      List<User> users = identityService.createUserQuery().listPage(0, 2);
      assertEquals(2, users.size());

      assertEquals("roman", users.get(0).getId());
      assertEquals("robert", users.get(1).getId());

      users = identityService.createUserQuery().listPage(2, 2);
      assertEquals(2, users.size());

      assertEquals("daniel", users.get(0).getId());
      assertEquals("oscar", users.get(1).getId());

      users = identityService.createUserQuery().listPage(4, 2);
      assertEquals(2, users.size());

      assertEquals("monster", users.get(0).getId());
      assertEquals("david(IT)", users.get(1).getId());

      users = identityService.createUserQuery().listPage(6, 2);
      assertEquals(2, users.size());

      assertEquals("ruecker", users.get(0).getId());
      assertEquals("fozzie", users.get(1).getId());

      users = identityService.createUserQuery().listPage(8, 2);
      assertEquals(0, users.size());

    } finally {
      processEngineConfiguration.setAuthorizationEnabled(false);
      identityService.clearAuthentication();

      for (Authorization authorization : authorizationService.createAuthorizationQuery().list()) {
        authorizationService.deleteAuthorization(authorization.getId());
      }

    }
  }

  public void testGroupQueryPagination() {
    List<Group> groups = identityService.createGroupQuery().listPage(0, 2);
    assertEquals(2, groups.size());

    assertEquals("management", groups.get(0).getId());
    assertEquals("development", groups.get(1).getId());

    groups = identityService.createGroupQuery().listPage(2, 2);
    assertEquals(2, groups.size());

    assertEquals("consulting", groups.get(0).getId());
    assertEquals("sales", groups.get(1).getId());

    groups = identityService.createGroupQuery().listPage(4, 2);
    assertEquals(1, groups.size());

    assertEquals("external", groups.get(0).getId());

    groups = identityService.createGroupQuery().listPage(6, 2);
    assertEquals(0, groups.size());
  }

  public void testGroupQueryPaginationWithAuthenticatedUserWithoutAuthorizations() {
    try {
      processEngineConfiguration.setAuthorizationEnabled(true);

      identityService.setAuthenticatedUserId("oscar");

      List<Group> groups = identityService.createGroupQuery().listPage(0, 2);
      assertEquals(2, groups.size());

      assertEquals("management", groups.get(0).getId());
      assertEquals("development", groups.get(1).getId());

      groups = identityService.createGroupQuery().listPage(2, 2);
      assertEquals(2, groups.size());

      assertEquals("consulting", groups.get(0).getId());
      assertEquals("sales", groups.get(1).getId());

      groups = identityService.createGroupQuery().listPage(4, 2);
      assertEquals(1, groups.size());

      assertEquals("external", groups.get(0).getId());

      groups = identityService.createGroupQuery().listPage(6, 2);
      assertEquals(0, groups.size());

    } finally {
      processEngineConfiguration.setAuthorizationEnabled(false);
      identityService.clearAuthentication();
    }
  }

  public void testGroupQueryPaginationWithAuthenticatedUserWithAuthorizations() {
    createGrantAuthorization(GROUP, "management", "oscar", READ);
    createGrantAuthorization(GROUP, "consulting", "oscar", READ);
    createGrantAuthorization(GROUP, "external", "oscar", READ);

    try {
      processEngineConfiguration.setAuthorizationEnabled(true);

      identityService.setAuthenticatedUserId("oscar");

      List<Group> groups = identityService.createGroupQuery().listPage(0, 2);
      assertEquals(2, groups.size());

      assertEquals("management", groups.get(0).getId());
      assertEquals("development", groups.get(1).getId());

      groups = identityService.createGroupQuery().listPage(2, 2);
      assertEquals(2, groups.size());

      assertEquals("consulting", groups.get(0).getId());
      assertEquals("sales", groups.get(1).getId());

      groups = identityService.createGroupQuery().listPage(4, 2);
      assertEquals(1, groups.size());

      assertEquals("external", groups.get(0).getId());

      groups = identityService.createGroupQuery().listPage(6, 2);
      assertEquals(0, groups.size());

    } finally {
      processEngineConfiguration.setAuthorizationEnabled(false);
      identityService.clearAuthentication();

      for (Authorization authorization : authorizationService.createAuthorizationQuery().list()) {
        authorizationService.deleteAuthorization(authorization.getId());
      }

    }
  }

  protected void createGrantAuthorization(Resource resource, String resourceId, String userId, Permission... permissions) {
    Authorization authorization = createAuthorization(AUTH_TYPE_GRANT, resource, resourceId);
    authorization.setUserId(userId);
    for (Permission permission : permissions) {
      authorization.addPermission(permission);
    }
    authorizationService.saveAuthorization(authorization);
  }

  protected Authorization createAuthorization(int type, Resource resource, String resourceId) {
    Authorization authorization = authorizationService.createNewAuthorization(type);

    authorization.setResource(resource);
    if (resourceId != null) {
      authorization.setResourceId(resourceId);
    }

    return authorization;
  }

}
