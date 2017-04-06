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

import java.util.HashSet;
import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Resources.USER;

import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.identity.User;
import static org.camunda.bpm.identity.impl.ldap.LdapTestUtilities.checkPagingResults;
import static org.camunda.bpm.identity.impl.ldap.LdapTestUtilities.testUserPaging;
import static org.camunda.bpm.identity.impl.ldap.LdapTestUtilities.testUserPagingWithMemberOfGroup;

/**
 * @author Daniel Meyer
 *
 */
public class LdapUserQueryTest extends LdapIdentityProviderTest {

  public void testQueryNoFilter() {
    List<User> result = identityService.createUserQuery().list();
    assertEquals(12, result.size());
  }

  public void testFilterByUserId() {
    User user = identityService.createUserQuery().userId("oscar").singleResult();
    assertNotNull(user);

    // validate user
    assertEquals("oscar", user.getId());
    assertEquals("Oscar", user.getFirstName());
    assertEquals("The Crouch", user.getLastName());
    assertEquals("oscar@camunda.org", user.getEmail());


    user = identityService.createUserQuery().userId("non-existing").singleResult();
    assertNull(user);
  }

  public void testFilterByUserIdIn() {
    List<User> users = identityService.createUserQuery().userIdIn("oscar", "monster").list();
    assertNotNull(users);
    assertEquals(2, users.size());

    users = identityService.createUserQuery().userIdIn("oscar", "monster", "daniel").list();
    assertNotNull(users);
    assertEquals(3, users.size());

    users = identityService.createUserQuery().userIdIn("oscar", "monster", "daniel", "non-existing").list();
    assertNotNull(users);
    assertEquals(3, users.size());
  }

  public void testFilterByFirstname() {
    User user = identityService.createUserQuery().userFirstName("Oscar").singleResult();
    assertNotNull(user);

    user = identityService.createUserQuery().userFirstName("non-existing").singleResult();
    assertNull(user);
  }

  public void testFilterByFirstnameLike() {
    User user = identityService.createUserQuery().userFirstNameLike("Osc*").singleResult();
    assertNotNull(user);

    user = identityService.createUserQuery().userFirstNameLike("non-exist*").singleResult();
    assertNull(user);
  }

  public void testFilterByLastname() {
    User user = identityService.createUserQuery().userLastName("The Crouch").singleResult();
    assertNotNull(user);

    user = identityService.createUserQuery().userLastName("non-existing").singleResult();
    assertNull(user);
  }

  public void testFilterByLastnameLike() {
    User user = identityService.createUserQuery().userLastNameLike("The Cro*").singleResult();
    assertNotNull(user);
    user = identityService.createUserQuery().userLastNameLike("The C*").singleResult();
    assertNotNull(user);

    user = identityService.createUserQuery().userLastNameLike("non-exist*").singleResult();
    assertNull(user);
  }

  public void testFilterByEmail() {
    User user = identityService.createUserQuery().userEmail("oscar@camunda.org").singleResult();
    assertNotNull(user);

    user = identityService.createUserQuery().userEmail("non-exist*").singleResult();
    assertNull(user);
  }

  public void testFilterByEmailLike() {
    User user = identityService.createUserQuery().userEmailLike("oscar@*").singleResult();
    assertNotNull(user);

    user = identityService.createUserQuery().userEmailLike("non-exist*").singleResult();
    assertNull(user);
  }

  public void testFilterByGroupId() {
    List<User> result = identityService.createUserQuery().memberOfGroup("development").list();
    assertEquals(3, result.size());
  }

  public void testFilterByGroupIdAndFirstname() {
    List<User> result = identityService.createUserQuery()
        .memberOfGroup("development")
        .userFirstName("Oscar")
        .list();
    assertEquals(1, result.size());
  }

  public void testFilterByGroupIdAndId() {
    List<User> result = identityService.createUserQuery()
        .memberOfGroup("development")
        .userId("oscar")
        .list();
    assertEquals(1, result.size());
  }

  public void testFilterByGroupIdAndLastname() {
    List<User> result = identityService.createUserQuery()
        .memberOfGroup("development")
        .userLastName("The Crouch")
        .list();
    assertEquals(1, result.size());
  }

  public void testFilterByGroupIdAndEmail() {
    List<User> result = identityService.createUserQuery()
        .memberOfGroup("development")
        .userEmail("oscar@camunda.org")
        .list();
    assertEquals(1, result.size());
  }

  public void testFilterByGroupIdAndEmailLike() {
    List<User> result = identityService.createUserQuery()
        .memberOfGroup("development")
        .userEmailLike("*@camunda.org")
        .list();
    assertEquals(3, result.size());
  }

  public void testFilterByGroupIdAndIdForDnUsingCn() {
    List<User> result = identityService.createUserQuery()
        .memberOfGroup("external")
        .userId("fozzie")
        .list();
    assertEquals(1, result.size());
  }

  public void testAuthenticatedUserSeesHimself() {
    try {
      processEngineConfiguration.setAuthorizationEnabled(true);

      identityService.setAuthenticatedUserId("non-existing");
      assertEquals(0, identityService.createUserQuery().count());

      identityService.setAuthenticatedUserId("oscar");
      assertEquals(1, identityService.createUserQuery().count());

    } finally {
      processEngineConfiguration.setAuthorizationEnabled(false);
      identityService.clearAuthentication();
    }
  }

  public void testPagination() {
    testUserPaging(identityService);
  }

  public void testPaginationWithMemberOfGroup() {
    testUserPagingWithMemberOfGroup(identityService);
  }

  public void testPaginationWithAuthenticatedUser() {
    createGrantAuthorization(USER, "roman", "oscar", READ);
    createGrantAuthorization(USER, "daniel", "oscar", READ);
    createGrantAuthorization(USER, "monster", "oscar", READ);
    createGrantAuthorization(USER, "ruecker", "oscar", READ);

    try {
      processEngineConfiguration.setAuthorizationEnabled(true);

      identityService.setAuthenticatedUserId("oscar");

      Set<String> userNames = new HashSet<String>();
      List<User> users = identityService.createUserQuery().listPage(0, 2);
      assertEquals(2, users.size());
      checkPagingResults(userNames, users.get(0).getId(), users.get(1).getId());

      users = identityService.createUserQuery().listPage(2, 2);
      assertEquals(2, users.size());
      checkPagingResults(userNames, users.get(0).getId(), users.get(1).getId());

      users = identityService.createUserQuery().listPage(4, 2);
      assertEquals(1, users.size());
      assertFalse(userNames.contains(users.get(0).getId()));
      userNames.add(users.get(0).getId());

      identityService.setAuthenticatedUserId("daniel");

      users = identityService.createUserQuery().listPage(0, 2);
      assertEquals(1, users.size());

      assertEquals("daniel", users.get(0).getId());

      users = identityService.createUserQuery().listPage(2, 2);
      assertEquals(0, users.size());

    } finally {
      processEngineConfiguration.setAuthorizationEnabled(false);
      identityService.clearAuthentication();

      for (Authorization authorization : authorizationService.createAuthorizationQuery().list()) {
        authorizationService.deleteAuthorization(authorization.getId());
      }

    }
  }

  public void testNativeQueryFail() {
    try {
      identityService.createNativeUserQuery();
      fail("Native queries are not supported in LDAP case.");
    } catch (BadUserRequestException ex) {
      assertTrue("Wrong exception", ex.getMessage().contains("Native user queries are not supported for LDAP"));
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
