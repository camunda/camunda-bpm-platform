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
import static org.camunda.bpm.engine.authorization.Resources.TENANT;
import static org.camunda.bpm.engine.authorization.Resources.TENANT_MEMBERSHIP;
import static org.camunda.bpm.engine.authorization.Resources.USER;
import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationTestUtil.assertExceptionInfo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Groups;
import org.camunda.bpm.engine.authorization.MissingAuthorization;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.identity.TenantQuery;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationEntity;
import org.camunda.bpm.engine.impl.persistence.entity.GroupEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TenantEntity;
import org.camunda.bpm.engine.impl.persistence.entity.UserEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.ClockUtil;
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
    basePerms.addPermission(ALL); // add all then remove 'create'
    basePerms.removePermission(CREATE);
    authorizationService.saveAuthorization(basePerms);

    processEngineConfiguration.setAuthorizationEnabled(true);
    identityService.setAuthenticatedUserId(jonny2);

    try {
      identityService.newUser("jonny1");
      fail("exception expected");

    } catch (AuthorizationException e) {
      assertEquals(1, e.getMissingAuthorizations().size());
      MissingAuthorization info = e.getMissingAuthorizations().get(0);
      assertEquals(jonny2, e.getUserId());
      assertExceptionInfo(CREATE.getName(), USER.resourceName(), null, info);
    }

    // circumvent auth check to get new transient userobject
    User newUser = new UserEntity("jonny1");

    try {
      identityService.saveUser(newUser);
      fail("exception expected");

    } catch (AuthorizationException e) {
      assertEquals(1, e.getMissingAuthorizations().size());
      MissingAuthorization info = e.getMissingAuthorizations().get(0);
      assertEquals(jonny2, e.getUserId());
      assertExceptionInfo(CREATE.getName(), USER.resourceName(), null, info);
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
      assertEquals(1, e.getMissingAuthorizations().size());
      MissingAuthorization info = e.getMissingAuthorizations().get(0);
      assertEquals(jonny2, e.getUserId());
      assertExceptionInfo(DELETE.getName(), USER.resourceName(), "jonny1", info);
    }
  }

  public void testTenantAuthorizationAfterDeleteUser() {
    // given jonny2 who is allowed to do user operations
    User jonny = identityService.newUser(jonny2);
    identityService.saveUser(jonny);

    grantPermissions();

    // turn on authorization
    processEngineConfiguration.setAuthorizationEnabled(true);
    identityService.setAuthenticatedUserId(jonny2);

    // create user
    User jonny1 = identityService.newUser("jonny1");
    identityService.saveUser(jonny1);
    String jonny1Id = jonny1.getId();

    // and tenant
    String tenant1 = "tenant1";
    Tenant tenant = identityService.newTenant(tenant1);
    identityService.saveTenant(tenant);
    identityService.createTenantUserMembership(tenant1, jonny1Id);

    // assume
    TenantQuery query = identityService.createTenantQuery().userMember(jonny1Id);
    assertThat(query.count(), is(1L));

    // when
    identityService.deleteUser(jonny1Id);

    // turn off authorization
    processEngineConfiguration.setAuthorizationEnabled(false);

    // then
    assertThat(query.count(), is(0L));
    assertThat(authorizationService.createAuthorizationQuery().resourceType(TENANT).userIdIn(jonny1Id).count(), is(0L));
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
      assertEquals(1, e.getMissingAuthorizations().size());
      MissingAuthorization info = e.getMissingAuthorizations().get(0);
      assertEquals(jonny2, e.getUserId());
      assertExceptionInfo(UPDATE.getName(), USER.resourceName(), "jonny1", info);
    }

    // but I can create a new user:
    User jonny3 = identityService.newUser("jonny3");
    identityService.saveUser(jonny3);

  }

  public void testUserUnlock() throws ParseException {

    // crate user while still in god-mode:
    String userId = "jonny";
    User jonny = identityService.newUser(userId);
    jonny.setPassword("xxx");
    identityService.saveUser(jonny);

    lockUser(userId, "invalid pwd");

    // assume
    int maxNumOfAttempts = 10;
    UserEntity lockedUser = (UserEntity) identityService.createUserQuery().userId(jonny.getId()).singleResult();
    assertNotNull(lockedUser);
    assertNotNull(lockedUser.getLockExpirationTime());
    assertEquals(maxNumOfAttempts, lockedUser.getAttempts());


    // create global auth
    Authorization basePerms = authorizationService.createNewAuthorization(AUTH_TYPE_GLOBAL);
    basePerms.setResource(USER);
    basePerms.setResourceId(ANY);
    basePerms.addPermission(ALL);
    authorizationService.saveAuthorization(basePerms);

    // set auth
    processEngineConfiguration.setAuthorizationEnabled(true);
    identityService.setAuthentication("admin", Collections.singletonList(Groups.CAMUNDA_ADMIN), null);

    // when
    identityService.unlockUser(lockedUser.getId());

    // then
    lockedUser = (UserEntity) identityService.createUserQuery().userId(jonny.getId()).singleResult();
    assertNotNull(lockedUser);
    assertNull(lockedUser.getLockExpirationTime());
    assertEquals(0, lockedUser.getAttempts());
  }

  public void testUserUnlockWithoutAuthorization() throws ParseException {

    // crate user while still in god-mode:
    String userId = "jonny";
    User jonny = identityService.newUser(userId);
    jonny.setPassword("xxx");
    identityService.saveUser(jonny);

    lockUser(userId, "invalid pwd");

    // assume
    int maxNumOfAttempts = 10;
    UserEntity lockedUser = (UserEntity) identityService.createUserQuery().userId(jonny.getId()).singleResult();
    assertNotNull(lockedUser);
    assertNotNull(lockedUser.getLockExpirationTime());
    assertEquals(maxNumOfAttempts, lockedUser.getAttempts());

    processEngineConfiguration.setAuthorizationEnabled(true);
    identityService.setAuthentication("admin", null, null);

    // when
    try {
      identityService.unlockUser(lockedUser.getId());
      fail("expected exception");
    } catch (AuthorizationException e) {
      assertTrue(e.getMessage().contains("Required authenticated group 'camunda-admin'."));
    }

    // return to god-mode
    processEngineConfiguration.setAuthorizationEnabled(false);

    // then
    int maxNumOfLoginAttempts = 10;
    lockedUser = (UserEntity) identityService.createUserQuery().userId(jonny.getId()).singleResult();
    assertNotNull(lockedUser);
    assertNotNull(lockedUser.getLockExpirationTime());
    assertEquals(maxNumOfLoginAttempts, lockedUser.getAttempts());
  }

  public void testGroupCreateAuthorizations() {

    // add base permission which allows nobody to create groups:
    Authorization basePerms = authorizationService.createNewAuthorization(AUTH_TYPE_GLOBAL);
    basePerms.setResource(GROUP);
    basePerms.setResourceId(ANY);
    basePerms.addPermission(ALL); // add all then remove 'create'
    basePerms.removePermission(CREATE);
    authorizationService.saveAuthorization(basePerms);

    processEngineConfiguration.setAuthorizationEnabled(true);
    identityService.setAuthenticatedUserId(jonny2);

    try {
      identityService.newGroup("group1");
      fail("exception expected");

    } catch (AuthorizationException e) {
      assertEquals(1, e.getMissingAuthorizations().size());
      MissingAuthorization info = e.getMissingAuthorizations().get(0);
      assertEquals(jonny2, e.getUserId());
      assertExceptionInfo(CREATE.getName(), GROUP.resourceName(), null, info);
    }

    // circumvent auth check to get new transient userobject
    Group group = new GroupEntity("group1");

    try {
      identityService.saveGroup(group);
      fail("exception expected");

    } catch (AuthorizationException e) {
      assertEquals(1, e.getMissingAuthorizations().size());
      MissingAuthorization info = e.getMissingAuthorizations().get(0);
      assertEquals(jonny2, e.getUserId());
      assertExceptionInfo(CREATE.getName(), GROUP.resourceName(), null, info);
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
      assertEquals(1, e.getMissingAuthorizations().size());
      MissingAuthorization info = e.getMissingAuthorizations().get(0);
      assertEquals(jonny2, e.getUserId());
      assertExceptionInfo(DELETE.getName(), GROUP.resourceName(), "group1", info);
    }

  }

  public void testTenantAuthorizationAfterDeleteGroup() {
    // given jonny2 who is allowed to do group operations
    User jonny = identityService.newUser(jonny2);
    identityService.saveUser(jonny);

    grantPermissions();

    // turn on authorization
    processEngineConfiguration.setAuthorizationEnabled(true);
    identityService.setAuthenticatedUserId(jonny2);

    // create group
    Group group1 = identityService.newGroup("group1");
    identityService.saveGroup(group1);

    // and tenant
    String tenant1 = "tenant1";
    Tenant tenant = identityService.newTenant(tenant1);
    identityService.saveTenant(tenant);
    identityService.createTenantGroupMembership(tenant1, "group1");

    // assume
    TenantQuery query = identityService.createTenantQuery().groupMember("group1");
    assertThat(query.count(), is(1L));

    // when
    identityService.deleteGroup("group1");

    // turn off authorization
    processEngineConfiguration.setAuthorizationEnabled(false);

    // then
    assertThat(query.count(), is(0L));
    assertThat(authorizationService.createAuthorizationQuery().resourceType(TENANT).groupIdIn("group1").count(), is(0L));
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
      assertEquals(1, e.getMissingAuthorizations().size());
      MissingAuthorization info = e.getMissingAuthorizations().get(0);
      assertEquals(jonny2, e.getUserId());
      assertExceptionInfo(UPDATE.getName(), GROUP.resourceName(), "group1", info);
    }

    // but I can create a new group:
    Group group2 = identityService.newGroup("group2");
    identityService.saveGroup(group2);

  }

  public void testTenantCreateAuthorizations() {

    // add base permission which allows nobody to create tenants:
    Authorization basePerms = authorizationService.createNewAuthorization(AUTH_TYPE_GLOBAL);
    basePerms.setResource(TENANT);
    basePerms.setResourceId(ANY);
    basePerms.addPermission(ALL); // add all then remove 'create'
    basePerms.removePermission(CREATE);
    authorizationService.saveAuthorization(basePerms);

    processEngineConfiguration.setAuthorizationEnabled(true);
    identityService.setAuthenticatedUserId(jonny2);

    try {
      identityService.newTenant("tenant");

      fail("exception expected");
    } catch (AuthorizationException e) {
      assertEquals(1, e.getMissingAuthorizations().size());
      MissingAuthorization info = e.getMissingAuthorizations().get(0);
      assertEquals(jonny2, e.getUserId());
      assertExceptionInfo(CREATE.getName(), TENANT.resourceName(), null, info);
    }

    // circumvent auth check to get new transient userobject
    Tenant tenant = new TenantEntity("tenant");

    try {
      identityService.saveTenant(tenant);
      fail("exception expected");

    } catch (AuthorizationException e) {
      assertEquals(1, e.getMissingAuthorizations().size());
      MissingAuthorization info = e.getMissingAuthorizations().get(0);
      assertEquals(jonny2, e.getUserId());
      assertExceptionInfo(CREATE.getName(), TENANT.resourceName(), null, info);
    }
  }

  public void testTenantDeleteAuthorizations() {

    // create tenant
    Tenant tenant = new TenantEntity("tenant");
    identityService.saveTenant(tenant);

    // create global auth
    Authorization basePerms = authorizationService.createNewAuthorization(AUTH_TYPE_GLOBAL);
    basePerms.setResource(TENANT);
    basePerms.setResourceId(ANY);
    basePerms.addPermission(ALL);
    basePerms.removePermission(DELETE); // revoke delete
    authorizationService.saveAuthorization(basePerms);

    // turn on authorization
    processEngineConfiguration.setAuthorizationEnabled(true);
    identityService.setAuthenticatedUserId(jonny2);

    try {
      identityService.deleteTenant("tenant");
      fail("exception expected");

    } catch (AuthorizationException e) {
      assertEquals(1, e.getMissingAuthorizations().size());
      MissingAuthorization info = e.getMissingAuthorizations().get(0);
      assertEquals(jonny2, e.getUserId());
      assertExceptionInfo(DELETE.getName(), TENANT.resourceName(), "tenant", info);
    }
  }

  public void testTenantUpdateAuthorizations() {

    // create tenant
    Tenant tenant = new TenantEntity("tenant");
    identityService.saveTenant(tenant);

    // create global auth
    Authorization basePerms = authorizationService.createNewAuthorization(AUTH_TYPE_GLOBAL);
    basePerms.setResource(TENANT);
    basePerms.setResourceId(ANY);
    basePerms.addPermission(ALL);
    basePerms.removePermission(UPDATE); // revoke update
    authorizationService.saveAuthorization(basePerms);

    // turn on authorization
    processEngineConfiguration.setAuthorizationEnabled(true);
    identityService.setAuthenticatedUserId(jonny2);

    // fetch user:
    tenant = identityService.createTenantQuery().singleResult();
    tenant.setName("newName");

    try {
      identityService.saveTenant(tenant);

      fail("exception expected");
    } catch (AuthorizationException e) {
      assertEquals(1, e.getMissingAuthorizations().size());
      MissingAuthorization info = e.getMissingAuthorizations().get(0);
      assertEquals(jonny2, e.getUserId());
      assertExceptionInfo(UPDATE.getName(), TENANT.resourceName(), "tenant", info);
    }

    // but I can create a new tenant:
    Tenant newTenant = identityService.newTenant("newTenant");
    identityService.saveTenant(newTenant);
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
      assertEquals(1, e.getMissingAuthorizations().size());
      MissingAuthorization info = e.getMissingAuthorizations().get(0);
      assertEquals(jonny2, e.getUserId());
      assertExceptionInfo(CREATE.getName(), GROUP_MEMBERSHIP.resourceName(), "group1", info);
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
      assertEquals(1, e.getMissingAuthorizations().size());
      MissingAuthorization info = e.getMissingAuthorizations().get(0);
      assertEquals(jonny2, e.getUserId());
      assertExceptionInfo(DELETE.getName(), GROUP_MEMBERSHIP.resourceName(), "group1", info);
    }
  }

  public void testTenantUserMembershipCreateAuthorizations() {

    User jonny1 = identityService.newUser("jonny1");
    identityService.saveUser(jonny1);

    Tenant tenant1 = identityService.newTenant("tenant1");
    identityService.saveTenant(tenant1);

    // add base permission which allows nobody to create memberships
    Authorization basePerms = authorizationService.createNewAuthorization(AUTH_TYPE_GLOBAL);
    basePerms.setResource(TENANT_MEMBERSHIP);
    basePerms.setResourceId(ANY);
    basePerms.addPermission(ALL); // add all then remove 'create'
    basePerms.removePermission(CREATE);
    authorizationService.saveAuthorization(basePerms);

    processEngineConfiguration.setAuthorizationEnabled(true);
    identityService.setAuthenticatedUserId(jonny2);

    try {
      identityService.createTenantUserMembership("tenant1", "jonny1");
      fail("exception expected");

    } catch (AuthorizationException e) {
      assertEquals(1, e.getMissingAuthorizations().size());
      MissingAuthorization info = e.getMissingAuthorizations().get(0);
      assertEquals(jonny2, e.getUserId());
      assertExceptionInfo(CREATE.getName(), TENANT_MEMBERSHIP.resourceName(), "tenant1", info);
    }
  }

  public void testTenantGroupMembershipCreateAuthorizations() {

    Group group1 = identityService.newGroup("group1");
    identityService.saveGroup(group1);

    Tenant tenant1 = identityService.newTenant("tenant1");
    identityService.saveTenant(tenant1);

    // add base permission which allows nobody to create memberships
    Authorization basePerms = authorizationService.createNewAuthorization(AUTH_TYPE_GLOBAL);
    basePerms.setResource(TENANT_MEMBERSHIP);
    basePerms.setResourceId(ANY);
    basePerms.addPermission(ALL); // add all then remove 'create'
    basePerms.removePermission(CREATE);
    authorizationService.saveAuthorization(basePerms);

    processEngineConfiguration.setAuthorizationEnabled(true);
    identityService.setAuthenticatedUserId(jonny2);

    try {
      identityService.createTenantGroupMembership("tenant1", "group1");
      fail("exception expected");

    } catch (AuthorizationException e) {
      assertEquals(1, e.getMissingAuthorizations().size());
      MissingAuthorization info = e.getMissingAuthorizations().get(0);
      assertEquals(jonny2, e.getUserId());
      assertExceptionInfo(CREATE.getName(), TENANT_MEMBERSHIP.resourceName(), "tenant1", info);
    }
  }

  public void testTenantUserMembershipDeleteAuthorizations() {

    User jonny1 = identityService.newUser("jonny1");
    identityService.saveUser(jonny1);

    Tenant tenant1 = identityService.newTenant("tenant1");
    identityService.saveTenant(tenant1);

    // add base permission which allows nobody to delete memberships
    Authorization basePerms = authorizationService.createNewAuthorization(AUTH_TYPE_GLOBAL);
    basePerms.setResource(TENANT_MEMBERSHIP);
    basePerms.setResourceId(ANY);
    basePerms.addPermission(ALL); // add all then remove 'delete'
    basePerms.removePermission(DELETE);
    authorizationService.saveAuthorization(basePerms);

    processEngineConfiguration.setAuthorizationEnabled(true);
    identityService.setAuthenticatedUserId(jonny2);

    try {
      identityService.deleteTenantUserMembership("tenant1", "jonny1");
      fail("exception expected");

    } catch (AuthorizationException e) {
      assertEquals(1, e.getMissingAuthorizations().size());
      MissingAuthorization info = e.getMissingAuthorizations().get(0);
      assertEquals(jonny2, e.getUserId());
      assertExceptionInfo(DELETE.getName(), TENANT_MEMBERSHIP.resourceName(), "tenant1", info);
    }
  }

  public void testTenanGroupMembershipDeleteAuthorizations() {

    Group group1 = identityService.newGroup("group1");
    identityService.saveGroup(group1);

    Tenant tenant1 = identityService.newTenant("tenant1");
    identityService.saveTenant(tenant1);

    // add base permission which allows nobody to delete memberships
    Authorization basePerms = authorizationService.createNewAuthorization(AUTH_TYPE_GLOBAL);
    basePerms.setResource(TENANT_MEMBERSHIP);
    basePerms.setResourceId(ANY);
    basePerms.addPermission(ALL); // add all then remove 'delete'
    basePerms.removePermission(DELETE);
    authorizationService.saveAuthorization(basePerms);

    processEngineConfiguration.setAuthorizationEnabled(true);
    identityService.setAuthenticatedUserId(jonny2);

    try {
      identityService.deleteTenantGroupMembership("tenant1", "group1");
      fail("exception expected");

    } catch (AuthorizationException e) {
      assertEquals(1, e.getMissingAuthorizations().size());
      MissingAuthorization info = e.getMissingAuthorizations().get(0);
      assertEquals(jonny2, e.getUserId());
      assertExceptionInfo(DELETE.getName(), TENANT_MEMBERSHIP.resourceName(), "tenant1", info);
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

  public void testTenantQueryAuthorizations() {
    // we are jonny2
    String authUserId = "jonny2";
    identityService.setAuthenticatedUserId(authUserId);

    // create new user jonny1
    User jonny1 = identityService.newUser("jonny1");
    identityService.saveUser(jonny1);
    // create new tenant
    Tenant tenant = identityService.newTenant("tenant");
    identityService.saveTenant(tenant);

    // set base permission for all users (no-one has any permissions on tenants)
    Authorization basePerms = authorizationService.createNewAuthorization(AUTH_TYPE_GLOBAL);
    basePerms.setResource(TENANT);
    basePerms.setResourceId(ANY);
    authorizationService.saveAuthorization(basePerms);

    // now enable checks
    processEngineConfiguration.setAuthorizationEnabled(true);

    // we cannot fetch the tenants
    assertEquals(0, identityService.createTenantQuery().count());

    // now we add permission for jonny2 to read the tenants:
    processEngineConfiguration.setAuthorizationEnabled(false);
    Authorization ourPerms = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    ourPerms.setUserId(authUserId);
    ourPerms.setResource(TENANT);
    ourPerms.setResourceId(ANY);
    ourPerms.addPermission(READ);
    authorizationService.saveAuthorization(ourPerms);
    processEngineConfiguration.setAuthorizationEnabled(true);

    // now we can fetch the tenants
    assertEquals(1, identityService.createTenantQuery().count());

    // change the base permission:
    processEngineConfiguration.setAuthorizationEnabled(false);
    basePerms = authorizationService.createAuthorizationQuery().resourceType(TENANT).userIdIn("*").singleResult();
    basePerms.addPermission(READ);
    authorizationService.saveAuthorization(basePerms);
    processEngineConfiguration.setAuthorizationEnabled(true);

    // we can still fetch the tenants
    assertEquals(1, identityService.createTenantQuery().count());

    // revoke permission for jonny2:
    processEngineConfiguration.setAuthorizationEnabled(false);
    ourPerms = authorizationService.createAuthorizationQuery().resourceType(TENANT).userIdIn(authUserId).singleResult();
    ourPerms.removePermission(READ);
    authorizationService.saveAuthorization(ourPerms);

    Authorization revoke = authorizationService.createNewAuthorization(AUTH_TYPE_REVOKE);
    revoke.setUserId(authUserId);
    revoke.setResource(TENANT);
    revoke.setResourceId(ANY);
    revoke.removePermission(READ);
    authorizationService.saveAuthorization(revoke);
    processEngineConfiguration.setAuthorizationEnabled(true);

    // now we cannot fetch the tenants
    assertEquals(0, identityService.createTenantQuery().count());

    // delete our permissions
    processEngineConfiguration.setAuthorizationEnabled(false);
    authorizationService.deleteAuthorization(ourPerms.getId());
    authorizationService.deleteAuthorization(revoke.getId());
    processEngineConfiguration.setAuthorizationEnabled(true);

    // now the base permission applies and grants us read access
    assertEquals(1, identityService.createTenantQuery().count());
  }

  protected void lockUser(String userId, String invalidPassword) throws ParseException {
    Date now = ClockUtil.getCurrentTime();
    try {
      for (int i = 0; i <= 11; i++) {
        assertFalse(identityService.checkPassword(userId, invalidPassword));
        now = DateUtils.addMinutes(ClockUtil.getCurrentTime(), 1);
        ClockUtil.setCurrentTime(now);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  protected void grantPermissions() {
    AuthorizationEntity userAdminAuth = new AuthorizationEntity(AUTH_TYPE_GLOBAL);
    userAdminAuth.setResource(USER);
    userAdminAuth.setResourceId(ANY);
    userAdminAuth.addPermission(ALL);
    authorizationService.saveAuthorization(userAdminAuth);

    userAdminAuth = new AuthorizationEntity(AUTH_TYPE_GLOBAL);
    userAdminAuth.setResource(GROUP);
    userAdminAuth.setResourceId(ANY);
    userAdminAuth.addPermission(ALL);
    authorizationService.saveAuthorization(userAdminAuth);

    userAdminAuth = new AuthorizationEntity(AUTH_TYPE_GLOBAL);
    userAdminAuth.setResource(TENANT);
    userAdminAuth.setResourceId(ANY);
    userAdminAuth.addPermission(ALL);
    authorizationService.saveAuthorization(userAdminAuth);

    userAdminAuth = new AuthorizationEntity(AUTH_TYPE_GLOBAL);
    userAdminAuth.setResource(TENANT_MEMBERSHIP);
    userAdminAuth.setResourceId(ANY);
    userAdminAuth.addPermission(ALL);
    authorizationService.saveAuthorization(userAdminAuth);
  }

  protected void cleanupAfterTest() {
    for (Group group : identityService.createGroupQuery().list()) {
      identityService.deleteGroup(group.getId());
    }
    for (User user : identityService.createUserQuery().list()) {
      identityService.deleteUser(user.getId());
    }
    for (Tenant tenant : identityService.createTenantQuery().list()) {
      identityService.deleteTenant(tenant.getId());
    }
    for (Authorization authorization : authorizationService.createAuthorizationQuery().list()) {
      authorizationService.deleteAuthorization(authorization.getId());
    }
  }

}
