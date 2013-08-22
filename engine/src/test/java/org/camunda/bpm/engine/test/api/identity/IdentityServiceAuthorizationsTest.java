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
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Resources.GROUP;
import static org.camunda.bpm.engine.authorization.Resources.GROUP_MEMBERSHIP;
import static org.camunda.bpm.engine.authorization.Resources.USER;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.persistence.entity.GroupEntity;
import org.camunda.bpm.engine.impl.persistence.entity.UserEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.junit.Assert;

/**
 * @author Daniel Meyer
 *
 */
public class IdentityServiceAuthorizationsTest extends PluggableProcessEngineTestCase {

  private final static String jonny2 = "jonny2";

  @Override
  protected void tearDown() throws Exception {
    processEngineConfiguration.setAuthorizationEnabled(false);
    cleanupAfterTest();
    super.tearDown();
  }

  public void testUserCreateAuthorizations() {

    // add base permission which allows nobody to create users:
    Authorization basePerms = authorizationService.createNewAuthorization(AUTH_TYPE_GLOBAL);
    basePerms.setResource(USER);
    basePerms.setResourceId(ANY);
    basePerms.addPermission(ALL); // add all then remove 'crate'
    basePerms.removePermission(CREATE);
    authorizationService.saveAuthorization(basePerms);

    processEngineConfiguration.setAuthorizationEnabled(true);
    identityService.setAuthenticatedUserId(jonny2);

    try {
      identityService.newUser("jonny1");
      fail("exception expected");

    } catch (AuthorizationException e) {
      assertEquals(CREATE.getName(), e.getViolatedPermissionName());
      assertEquals(jonny2, e.getUserId());
      assertEquals(USER.resourceName(), e.getResourceType());
      assertEquals(null, e.getResourceId());
    }

    // circumvent auth check to get new transient userobject
    User newUser = new UserEntity("jonny1");

    try {
      identityService.saveUser(newUser);
      fail("exception expected");

    } catch (AuthorizationException e) {
      assertEquals(CREATE.getName(), e.getViolatedPermissionName());
      assertEquals(jonny2, e.getUserId());
      assertEquals(USER.resourceName(), e.getResourceType());
      assertEquals(null, e.getResourceId());
    }
  }

  public void testUserDeleteAuthorizations() {

    // crate user while still in god-mode:
    User jonny1 = identityService.newUser("jonny1");
    identityService.saveUser(jonny1);

    // create global auth
    Authorization basePerms = authorizationService.createNewAuthorization(AUTH_TYPE_GLOBAL);
    basePerms.setResource(USER);
    basePerms.setResourceId(ANY);
    basePerms.addPermission(ALL);
    basePerms.removePermission(DELETE); // revoke delete
    authorizationService.saveAuthorization(basePerms);

    // turn on authorization
    processEngineConfiguration.setAuthorizationEnabled(true);
    identityService.setAuthenticatedUserId(jonny2);

    try {
      identityService.deleteUser("jonny1");
      fail("exception expected");

    } catch (AuthorizationException e) {
      assertEquals(DELETE.getName(), e.getViolatedPermissionName());
      assertEquals(jonny2, e.getUserId());
      assertEquals(USER.resourceName(), e.getResourceType());
      assertEquals("jonny1", e.getResourceId());
    }
  }

  public void testUserUpdateAuthorizations() {

    // crate user while still in god-mode:
    User jonny1 = identityService.newUser("jonny1");
    identityService.saveUser(jonny1);

    // create global auth
    Authorization basePerms = authorizationService.createNewAuthorization(AUTH_TYPE_GLOBAL);
    basePerms.setResource(USER);
    basePerms.setResourceId(ANY);
    basePerms.addPermission(ALL);
    basePerms.removePermission(UPDATE); // revoke update
    authorizationService.saveAuthorization(basePerms);

    // turn on authorization
    processEngineConfiguration.setAuthorizationEnabled(true);
    identityService.setAuthenticatedUserId(jonny2);

    // fetch user:
    jonny1 = identityService.createUserQuery().singleResult();
    jonny1.setFirstName("Jonny");

    try {
      identityService.saveUser(jonny1);
      fail("exception expected");

    } catch (AuthorizationException e) {
      assertEquals(UPDATE.getName(), e.getViolatedPermissionName());
      assertEquals(jonny2, e.getUserId());
      assertEquals(USER.resourceName(), e.getResourceType());
      assertEquals("jonny1", e.getResourceId());
    }

    // but I can create a new user:
    User jonny3 = identityService.newUser("jonny3");
    identityService.saveUser(jonny3);

  }

  public void testGroupCreateAuthorizations() {

    // add base permission which allows nobody to create groups:
    Authorization basePerms = authorizationService.createNewAuthorization(AUTH_TYPE_GLOBAL);
    basePerms.setResource(GROUP);
    basePerms.setResourceId(ANY);
    basePerms.addPermission(ALL); // add all then remove 'crate'
    basePerms.removePermission(CREATE);
    authorizationService.saveAuthorization(basePerms);

    processEngineConfiguration.setAuthorizationEnabled(true);
    identityService.setAuthenticatedUserId(jonny2);

    try {
      identityService.newGroup("group1");
      fail("exception expected");

    } catch (AuthorizationException e) {
      assertEquals(CREATE.getName(), e.getViolatedPermissionName());
      assertEquals(jonny2, e.getUserId());
      assertEquals(GROUP.resourceName(), e.getResourceType());
      assertEquals(null, e.getResourceId());
    }

    // circumvent auth check to get new transient userobject
    Group group = new GroupEntity("group1");

    try {
      identityService.saveGroup(group);
      fail("exception expected");

    } catch (AuthorizationException e) {
      assertEquals(CREATE.getName(), e.getViolatedPermissionName());
      assertEquals(jonny2, e.getUserId());
      assertEquals(GROUP.resourceName(), e.getResourceType());
      assertEquals(null, e.getResourceId());
    }
  }

  public void testGroupDeleteAuthorizations() {

    // crate group while still in god-mode:
    Group group1 = identityService.newGroup("group1");
    identityService.saveGroup(group1);

    // create global auth
    Authorization basePerms = authorizationService.createNewAuthorization(AUTH_TYPE_GLOBAL);
    basePerms.setResource(GROUP);
    basePerms.setResourceId(ANY);
    basePerms.addPermission(ALL);
    basePerms.removePermission(DELETE); // revoke delete
    authorizationService.saveAuthorization(basePerms);

    // turn on authorization
    processEngineConfiguration.setAuthorizationEnabled(true);
    identityService.setAuthenticatedUserId(jonny2);

    try {
      identityService.deleteGroup("group1");
      fail("exception expected");

    } catch (AuthorizationException e) {
      assertEquals(DELETE.getName(), e.getViolatedPermissionName());
      assertEquals(jonny2, e.getUserId());
      assertEquals(GROUP.resourceName(), e.getResourceType());
      assertEquals("group1", e.getResourceId());
    }

  }


  public void testGroupUpdateAuthorizations() {

    // crate group while still in god-mode:
    Group group1 = identityService.newGroup("group1");
    identityService.saveGroup(group1);

    // create global auth
    Authorization basePerms = authorizationService.createNewAuthorization(AUTH_TYPE_GLOBAL);
    basePerms.setResource(GROUP);
    basePerms.setResourceId(ANY);
    basePerms.addPermission(ALL);
    basePerms.removePermission(UPDATE); // revoke update
    authorizationService.saveAuthorization(basePerms);

    // turn on authorization
    processEngineConfiguration.setAuthorizationEnabled(true);
    identityService.setAuthenticatedUserId(jonny2);

    // fetch user:
    group1 = identityService.createGroupQuery().singleResult();
    group1.setName("Group 1");

    try {
      identityService.saveGroup(group1);
      fail("exception expected");

    } catch (AuthorizationException e) {
      assertEquals(UPDATE.getName(), e.getViolatedPermissionName());
      assertEquals(jonny2, e.getUserId());
      assertEquals(GROUP.resourceName(), e.getResourceType());
      assertEquals("group1", e.getResourceId());
    }

    // but I can create a new group:
    Group group2 = identityService.newGroup("group2");
    identityService.saveGroup(group2);

  }

  public void testMembershipCreateAuthorizations() {

    User jonny1 = identityService.newUser("jonny1");
    identityService.saveUser(jonny1);

    Group group1 = identityService.newGroup("group1");
    identityService.saveGroup(group1);

    // add base permission which allows nobody to add users to groups
    Authorization basePerms = authorizationService.createNewAuthorization(AUTH_TYPE_GLOBAL);
    basePerms.setResource(GROUP_MEMBERSHIP);
    basePerms.setResourceId(ANY);
    basePerms.addPermission(ALL); // add all then remove 'crate'
    basePerms.removePermission(CREATE);
    authorizationService.saveAuthorization(basePerms);

    processEngineConfiguration.setAuthorizationEnabled(true);
    identityService.setAuthenticatedUserId(jonny2);

    try {
      identityService.createMembership("jonny1", "group1");
      fail("exception expected");

    } catch (AuthorizationException e) {
      assertEquals(CREATE.getName(), e.getViolatedPermissionName());
      assertEquals(jonny2, e.getUserId());
      assertEquals(GROUP_MEMBERSHIP.resourceName(), e.getResourceType());
      assertEquals("group1", e.getResourceId());
    }
  }

  public void testMembershipDeleteAuthorizations() {

    User jonny1 = identityService.newUser("jonny1");
    identityService.saveUser(jonny1);

    Group group1 = identityService.newGroup("group1");
    identityService.saveGroup(group1);

    // add base permission which allows nobody to add users to groups
    Authorization basePerms = authorizationService.createNewAuthorization(AUTH_TYPE_GLOBAL);
    basePerms.setResource(GROUP_MEMBERSHIP);
    basePerms.setResourceId(ANY);
    basePerms.addPermission(ALL); // add all then remove 'delete'
    basePerms.removePermission(DELETE);
    authorizationService.saveAuthorization(basePerms);

    processEngineConfiguration.setAuthorizationEnabled(true);
    identityService.setAuthenticatedUserId(jonny2);

    try {
      identityService.deleteMembership("jonny1", "group1");
      fail("exception expected");

    } catch (AuthorizationException e) {
      assertEquals(DELETE.getName(), e.getViolatedPermissionName());
      assertEquals(jonny2, e.getUserId());
      assertEquals(GROUP_MEMBERSHIP.resourceName(), e.getResourceType());
      assertEquals("group1", e.getResourceId());
    }
  }

  public void testUserQueryAuthorizations() {

    // we are jonny2
    String authUserId = "jonny2";
    identityService.setAuthenticatedUserId(authUserId);

    // create new user jonny1
    User jonny1 = identityService.newUser("jonny1");
    identityService.saveUser(jonny1);

    // set base permission for all users (no-one has any permissions on users)
    Authorization basePerms = authorizationService.createNewAuthorization(AUTH_TYPE_GLOBAL);
    basePerms.setResource(USER);
    basePerms.setResourceId(ANY);
    authorizationService.saveAuthorization(basePerms);

    // now enable checks
    processEngineConfiguration.setAuthorizationEnabled(true);

    // we cannot fetch the user
    assertNull(identityService.createUserQuery().singleResult());
    assertEquals(0, identityService.createUserQuery().count());

    processEngineConfiguration.setAuthorizationEnabled(false);

    // now we add permission for jonny2 to read the user:
    Authorization ourPerms = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    ourPerms.setUserId(authUserId);
    ourPerms.setResource(USER);
    ourPerms.setResourceId(ANY);
    ourPerms.addPermission(READ);
    authorizationService.saveAuthorization(ourPerms);

    processEngineConfiguration.setAuthorizationEnabled(true);

    // now we can fetch the user
    assertNotNull(identityService.createUserQuery().singleResult());
    assertEquals(1, identityService.createUserQuery().count());

    // change the base permission:
    processEngineConfiguration.setAuthorizationEnabled(false);
    basePerms = authorizationService.createAuthorizationQuery().resourceType(USER).userIdIn("*").singleResult();
    basePerms.addPermission(READ);
    authorizationService.saveAuthorization(basePerms);
    processEngineConfiguration.setAuthorizationEnabled(true);

    // we can still fetch the user
    assertNotNull(identityService.createUserQuery().singleResult());
    assertEquals(1, identityService.createUserQuery().count());


    // revoke permission for jonny2:
    processEngineConfiguration.setAuthorizationEnabled(false);
    ourPerms = authorizationService.createAuthorizationQuery().resourceType(USER).userIdIn(authUserId).singleResult();
    ourPerms.removePermission(READ);
    authorizationService.saveAuthorization(ourPerms);

    Authorization revoke = authorizationService.createNewAuthorization(AUTH_TYPE_REVOKE);
    revoke.setUserId(authUserId);
    revoke.setResource(USER);
    revoke.setResourceId(ANY);
    revoke.removePermission(READ);
    authorizationService.saveAuthorization(revoke);
    processEngineConfiguration.setAuthorizationEnabled(true);

    // now we cannot fetch the user
    assertNull(identityService.createUserQuery().singleResult());
    assertEquals(0, identityService.createUserQuery().count());


    // delete our perms
    processEngineConfiguration.setAuthorizationEnabled(false);
    authorizationService.deleteAuthorization(ourPerms.getId());
    authorizationService.deleteAuthorization(revoke.getId());
    processEngineConfiguration.setAuthorizationEnabled(true);

    // now the base permission applies and grants us read access
    assertNotNull(identityService.createUserQuery().singleResult());
    assertEquals(1, identityService.createUserQuery().count());

  }

  public void testUserQueryAuthorizationsMultipleGroups() {

    // we are jonny2
    String authUserId = "jonny2";
    identityService.setAuthenticatedUserId(authUserId);

    User demo = identityService.newUser("demo");
    identityService.saveUser(demo);

    User mary = identityService.newUser("mary");
    identityService.saveUser(mary);

    User peter = identityService.newUser("peter");
    identityService.saveUser(peter);

    User john = identityService.newUser("john");
    identityService.saveUser(john);

    Group sales = identityService.newGroup("sales");
    identityService.saveGroup(sales);

    Group accounting = identityService.newGroup("accounting");
    identityService.saveGroup(accounting);

    Group management = identityService.newGroup("management");
    identityService.saveGroup(management);

    identityService.createMembership("demo", "sales");
    identityService.createMembership("demo", "accounting");
    identityService.createMembership("demo", "management");

    identityService.createMembership("john", "sales");
    identityService.createMembership("mary", "accounting");
    identityService.createMembership("peter", "management");

    Authorization demoAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    demoAuth.setUserId("demo");
    demoAuth.setResource(USER);
    demoAuth.setResourceId("demo");
    demoAuth.addPermission(ALL);
    authorizationService.saveAuthorization(demoAuth);

    Authorization johnAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    johnAuth.setUserId("john");
    johnAuth.setResource(USER);
    johnAuth.setResourceId("john");
    johnAuth.addPermission(ALL);
    authorizationService.saveAuthorization(johnAuth);

    Authorization maryAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    maryAuth.setUserId("mary");
    maryAuth.setResource(USER);
    maryAuth.setResourceId("mary");
    maryAuth.addPermission(ALL);
    authorizationService.saveAuthorization(maryAuth);

    Authorization peterAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    peterAuth.setUserId("peter");
    peterAuth.setResource(USER);
    peterAuth.setResourceId("peter");
    peterAuth.addPermission(ALL);
    authorizationService.saveAuthorization(peterAuth);

    Authorization accAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    accAuth.setGroupId("accounting");
    accAuth.setResource(GROUP);
    accAuth.setResourceId("accounting");
    accAuth.addPermission(READ);
    authorizationService.saveAuthorization(accAuth);

    Authorization salesAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    salesAuth.setGroupId("sales");
    salesAuth.setResource(GROUP);
    salesAuth.setResourceId("sales");
    salesAuth.addPermission(READ);
    authorizationService.saveAuthorization(salesAuth);

    Authorization manAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    manAuth.setGroupId("management");
    manAuth.setResource(GROUP);
    manAuth.setResourceId("management");
    manAuth.addPermission(READ);
    authorizationService.saveAuthorization(manAuth);

    Authorization salesDemoAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    salesDemoAuth.setGroupId("sales");
    salesDemoAuth.setResource(USER);
    salesDemoAuth.setResourceId("demo");
    salesDemoAuth.addPermission(READ);
    authorizationService.saveAuthorization(salesDemoAuth);

    Authorization salesJohnAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    salesJohnAuth.setGroupId("sales");
    salesJohnAuth.setResource(USER);
    salesJohnAuth.setResourceId("john");
    salesJohnAuth.addPermission(READ);
    authorizationService.saveAuthorization(salesJohnAuth);

    Authorization manDemoAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    manDemoAuth.setGroupId("management");
    manDemoAuth.setResource(USER);
    manDemoAuth.setResourceId("demo");
    manDemoAuth.addPermission(READ);
    authorizationService.saveAuthorization(manDemoAuth);

    Authorization manPeterAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    manPeterAuth.setGroupId("management");
    manPeterAuth.setResource(USER);
    manPeterAuth.setResourceId("peter");
    manPeterAuth.addPermission(READ);
    authorizationService.saveAuthorization(manPeterAuth);

    Authorization accDemoAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    accDemoAuth.setGroupId("accounting");
    accDemoAuth.setResource(USER);
    accDemoAuth.setResourceId("demo");
    accDemoAuth.addPermission(READ);
    authorizationService.saveAuthorization(accDemoAuth);

    Authorization accMaryAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    accMaryAuth.setGroupId("accounting");
    accMaryAuth.setResource(USER);
    accMaryAuth.setResourceId("mary");
    accMaryAuth.addPermission(READ);
    authorizationService.saveAuthorization(accMaryAuth);

    List<String> groups = new ArrayList<String>();
    groups.add("management");
    groups.add("accounting");
    groups.add("sales");

    identityService.setAuthentication("demo", groups);

    processEngineConfiguration.setAuthorizationEnabled(true);

    List<User> salesUser = identityService.createUserQuery().memberOfGroup("sales").list();
    assertEquals(2, salesUser.size());

    for (User user : salesUser) {
      if (!user.getId().equals("demo") && !user.getId().equals("john")) {
        Assert.fail("Unexpected user for group sales: " + user.getId());
      }
    }

    List<User> accountingUser = identityService.createUserQuery().memberOfGroup("accounting").list();
    assertEquals(2, accountingUser.size());

    for (User user : accountingUser) {
      if (!user.getId().equals("demo") && !user.getId().equals("mary")) {
        Assert.fail("Unexpected user for group accounting: " + user.getId());
      }
    }

    List<User> managementUser = identityService.createUserQuery().memberOfGroup("management").list();
    assertEquals(2, managementUser.size());

    for (User user : managementUser) {
      if (!user.getId().equals("demo") && !user.getId().equals("peter")) {
        Assert.fail("Unexpected user for group managment: " + user.getId());
      }
    }
  }

  public void testGroupQueryAuthorizations() {

    // we are jonny2
    String authUserId = "jonny2";
    identityService.setAuthenticatedUserId(authUserId);

    // create new user jonny1
    User jonny1 = identityService.newUser("jonny1");
    identityService.saveUser(jonny1);
    // create new group
    Group group1 = identityService.newGroup("group1");
    identityService.saveGroup(group1);

    // set base permission for all users (no-one has any permissions on groups)
    Authorization basePerms = authorizationService.createNewAuthorization(AUTH_TYPE_GLOBAL);
    basePerms.setResource(GROUP);
    basePerms.setResourceId(ANY);
    authorizationService.saveAuthorization(basePerms);

    // now enable checks
    processEngineConfiguration.setAuthorizationEnabled(true);

    // we cannot fetch the group
    assertNull(identityService.createGroupQuery().singleResult());
    assertEquals(0, identityService.createGroupQuery().count());

    // now we add permission for jonny2 to read the group:
    processEngineConfiguration.setAuthorizationEnabled(false);
    Authorization ourPerms = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    ourPerms.setUserId(authUserId);
    ourPerms.setResource(GROUP);
    ourPerms.setResourceId(ANY);
    ourPerms.addPermission(READ);
    authorizationService.saveAuthorization(ourPerms);
    processEngineConfiguration.setAuthorizationEnabled(true);

    // now we can fetch the group
    assertNotNull(identityService.createGroupQuery().singleResult());
    assertEquals(1, identityService.createGroupQuery().count());

    // change the base permission:
    processEngineConfiguration.setAuthorizationEnabled(false);
    basePerms = authorizationService.createAuthorizationQuery().resourceType(GROUP).userIdIn("*").singleResult();
    basePerms.addPermission(READ);
    authorizationService.saveAuthorization(basePerms);
    processEngineConfiguration.setAuthorizationEnabled(true);

    // we can still fetch the group
    assertNotNull(identityService.createGroupQuery().singleResult());
    assertEquals(1, identityService.createGroupQuery().count());

    // revoke permission for jonny2:
    processEngineConfiguration.setAuthorizationEnabled(false);
    ourPerms = authorizationService.createAuthorizationQuery().resourceType(GROUP).userIdIn(authUserId).singleResult();
    ourPerms.removePermission(READ);
    authorizationService.saveAuthorization(ourPerms);

    Authorization revoke = authorizationService.createNewAuthorization(AUTH_TYPE_REVOKE);
    revoke.setUserId(authUserId);
    revoke.setResource(GROUP);
    revoke.setResourceId(ANY);
    revoke.removePermission(READ);
    authorizationService.saveAuthorization(revoke);
    processEngineConfiguration.setAuthorizationEnabled(true);

    // now we cannot fetch the group
    assertNull(identityService.createGroupQuery().singleResult());
    assertEquals(0, identityService.createGroupQuery().count());

    // delete our perms
    processEngineConfiguration.setAuthorizationEnabled(false);
    authorizationService.deleteAuthorization(ourPerms.getId());
    authorizationService.deleteAuthorization(revoke.getId());
    processEngineConfiguration.setAuthorizationEnabled(true);

    // now the base permission applies and grants us read access
    assertNotNull(identityService.createGroupQuery().singleResult());
    assertEquals(1, identityService.createGroupQuery().count());

  }

  protected void cleanupAfterTest() {
    for (Group group : identityService.createGroupQuery().list()) {
      identityService.deleteGroup(group.getId());
    }
    for (User user : identityService.createUserQuery().list()) {
      identityService.deleteUser(user.getId());
    }
    for (Authorization authorization : authorizationService.createAuthorizationQuery().list()) {
      authorizationService.deleteAuthorization(authorization.getId());
    }
  }

}
