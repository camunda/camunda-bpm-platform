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
package org.camunda.bpm.engine.test.api.identity;

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GLOBAL;
import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;
import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_REVOKE;
import static org.camunda.bpm.engine.test.api.identity.TestPermissions.ALL;
import static org.camunda.bpm.engine.test.api.identity.TestPermissions.CREATE;
import static org.camunda.bpm.engine.test.api.identity.TestPermissions.DELETE;
import static org.camunda.bpm.engine.test.api.identity.TestPermissions.READ;
import static org.camunda.bpm.engine.test.api.identity.TestPermissions.UPDATE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Stefan Hentschel.
 */
public class AuthorizationServiceWithEnabledAuthorizationTest extends PluggableProcessEngineTest {

  @Before
  public void setUp() throws Exception {

    processEngineConfiguration.setAuthorizationEnabled(true);
  }

  @After
  public void tearDown() throws Exception {
    processEngineConfiguration.setAuthorizationEnabled(false);
    cleanupAfterTest();

  }

  @Test
  public void testAuthorizationCheckEmptyDb() {
    Resource resource1 = TestResource.RESOURCE1;
    Resource resource2 = TestResource.RESOURCE2;

    List<String> jonnysGroups = Arrays.asList("sales", "marketing");
    List<String> someOneElsesGroups = Collections.singletonList("marketing");

    // if no authorizations are in Db, nothing is authorized
    assertFalse(authorizationService.isUserAuthorized("jonny", jonnysGroups, ALL, resource1));
    assertFalse(authorizationService.isUserAuthorized("someone", someOneElsesGroups, CREATE, resource2));
    assertFalse(authorizationService.isUserAuthorized("someone else", null, DELETE, resource1));
    assertFalse(authorizationService.isUserAuthorized("jonny", jonnysGroups, ALL, resource1, "someId"));
    assertFalse(authorizationService.isUserAuthorized("someone", someOneElsesGroups, CREATE, resource2, "someId"));
    assertFalse(authorizationService.isUserAuthorized("someone else", null, DELETE, resource1, "someOtherId"));
  }

  @Test
  public void testUserOverrideGlobalGrantAuthorizationCheck() {
    Resource resource1 = TestResource.RESOURCE1;

    // create global authorization which grants all permissions to all users  (on resource1):
    Authorization globalGrant = authorizationService.createNewAuthorization(AUTH_TYPE_GLOBAL);
    globalGrant.setResource(resource1);
    globalGrant.setResourceId(ANY);
    globalGrant.addPermission(ALL);
    authorizationService.saveAuthorization(globalGrant);

    // revoke READ for jonny
    Authorization localRevoke = authorizationService.createNewAuthorization(AUTH_TYPE_REVOKE);
    localRevoke.setUserId("jonny");
    localRevoke.setResource(resource1);
    localRevoke.setResourceId(ANY);
    localRevoke.removePermission(READ);
    authorizationService.saveAuthorization(localRevoke);

    List<String> jonnysGroups = Arrays.asList("sales", "marketing");
    List<String> someOneElsesGroups = Collections.singletonList("marketing");

    // jonny does not have ALL permissions
    assertFalse(authorizationService.isUserAuthorized("jonny", null, ALL, resource1));
    assertFalse(authorizationService.isUserAuthorized("jonny", jonnysGroups, ALL, resource1));
    // jonny can't read
    assertFalse(authorizationService.isUserAuthorized("jonny", null, READ, resource1));
    assertFalse(authorizationService.isUserAuthorized("jonny", jonnysGroups, READ, resource1));
    // someone else can
    assertTrue(authorizationService.isUserAuthorized("someone else", null, ALL, resource1));
    assertTrue(authorizationService.isUserAuthorized("someone else", someOneElsesGroups, READ, resource1));
    assertTrue(authorizationService.isUserAuthorized("someone else", null, ALL, resource1));
    assertTrue(authorizationService.isUserAuthorized("someone else", someOneElsesGroups, READ, resource1));
    // jonny can still delete
    assertTrue(authorizationService.isUserAuthorized("jonny", null, DELETE, resource1));
    assertTrue(authorizationService.isUserAuthorized("jonny", jonnysGroups, DELETE, resource1));
  }

  @Test
  public void testGroupOverrideGlobalGrantAuthorizationCheck() {
    Resource resource1 = TestResource.RESOURCE1;

    // create global authorization which grants all permissions to all users  (on resource1):
    Authorization globalGrant = authorizationService.createNewAuthorization(AUTH_TYPE_GLOBAL);
    globalGrant.setResource(resource1);
    globalGrant.setResourceId(ANY);
    globalGrant.addPermission(ALL);
    authorizationService.saveAuthorization(globalGrant);

    // revoke READ for group "sales"
    Authorization groupRevoke = authorizationService.createNewAuthorization(AUTH_TYPE_REVOKE);
    groupRevoke.setGroupId("sales");
    groupRevoke.setResource(resource1);
    groupRevoke.setResourceId(ANY);
    groupRevoke.removePermission(READ);
    authorizationService.saveAuthorization(groupRevoke);

    List<String> jonnysGroups = Arrays.asList("sales", "marketing");
    List<String> someOneElsesGroups = Collections.singletonList("marketing");

    // jonny does not have ALL permissions if queried with groups
    assertFalse(authorizationService.isUserAuthorized("jonny", jonnysGroups, ALL, resource1));
    // if queried without groups he has
    assertTrue(authorizationService.isUserAuthorized("jonny", null, ALL, resource1));

    // jonny can't read if queried with groups
    assertFalse(authorizationService.isUserAuthorized("jonny", jonnysGroups, READ, resource1));
    // if queried without groups he has
    assertTrue(authorizationService.isUserAuthorized("jonny", null, READ, resource1));

    // someone else who is in group "marketing" but but not "sales" can
    assertTrue(authorizationService.isUserAuthorized("someone else", someOneElsesGroups, ALL, resource1));
    assertTrue(authorizationService.isUserAuthorized("someone else", someOneElsesGroups, READ, resource1));
    assertTrue(authorizationService.isUserAuthorized("someone else", null, ALL, resource1));
    assertTrue(authorizationService.isUserAuthorized("someone else", null, READ, resource1));
    // he could'nt if he were in jonny's groups
    assertFalse(authorizationService.isUserAuthorized("someone else", jonnysGroups, ALL, resource1));
    assertFalse(authorizationService.isUserAuthorized("someone else", jonnysGroups, READ, resource1));

    // jonny can still delete
    assertTrue(authorizationService.isUserAuthorized("jonny", jonnysGroups, DELETE, resource1));
    assertTrue(authorizationService.isUserAuthorized("jonny", null, DELETE, resource1));
  }

  @Test
  public void testUserOverrideGlobalRevokeAuthorizationCheck() {
    Resource resource1 = TestResource.RESOURCE1;

    // create global authorization which revokes all permissions to all users  (on resource1):
    Authorization globalGrant = authorizationService.createNewAuthorization(AUTH_TYPE_GLOBAL);
    globalGrant.setResource(resource1);
    globalGrant.setResourceId(ANY);
    globalGrant.removePermission(ALL);
    authorizationService.saveAuthorization(globalGrant);

    // add READ for jonny
    Authorization localRevoke = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    localRevoke.setUserId("jonny");
    localRevoke.setResource(resource1);
    localRevoke.setResourceId(ANY);
    localRevoke.addPermission(READ);
    authorizationService.saveAuthorization(localRevoke);

    // jonny does not have ALL permissions
    assertFalse(authorizationService.isUserAuthorized("jonny", null, ALL, resource1));
    // jonny can read
    assertTrue(authorizationService.isUserAuthorized("jonny", null, READ, resource1));
    // jonny can't delete
    assertFalse(authorizationService.isUserAuthorized("jonny", null, DELETE, resource1));

    // someone else can't do anything
    assertFalse(authorizationService.isUserAuthorized("someone else", null, ALL, resource1));
    assertFalse(authorizationService.isUserAuthorized("someone else", null, READ, resource1));
    assertFalse(authorizationService.isUserAuthorized("someone else", null, DELETE, resource1));
  }

  @Test
  public void testNullAuthorizationCheckUserGroup() {
    try {
      authorizationService.isUserAuthorized(null, null, UPDATE, TestResource.RESOURCE1);
      fail("Expected NullValueException");
    } catch (NullValueException e) {
      assertTrue(e.getMessage().contains("Authorization must have a 'userId' or/and a 'groupId'"));
    }
  }

  @Test
  public void testNullAuthorizationCheckPermission() {
    try {
      authorizationService.isUserAuthorized("jonny", null, null, TestResource.RESOURCE1);
      fail("Expected NullValueException");
    } catch (NullValueException e) {
      assertTrue(e.getMessage().contains("Invalid permission for an authorization"));
    }
  }

  @Test
  public void testNullAuthorizationCheckResource() {
    try {
      authorizationService.isUserAuthorized("jonny", null, UPDATE, null);
      fail("Expected NullValueException");
    } catch (NullValueException e) {
      assertTrue(e.getMessage().contains("Invalid resource for an authorization"));
    }
  }

  @Test
  public void testUserOverrideGroupOverrideGlobalAuthorizationCheck() {
    Resource resource1 = TestResource.RESOURCE1;

    // create global authorization which grants all permissions to all users  (on resource1):
    Authorization globalGrant = authorizationService.createNewAuthorization(AUTH_TYPE_GLOBAL);
    globalGrant.setResource(resource1);
    globalGrant.setResourceId(ANY);
    globalGrant.addPermission(ALL);
    authorizationService.saveAuthorization(globalGrant);

    // revoke READ for group "sales"
    Authorization groupRevoke = authorizationService.createNewAuthorization(AUTH_TYPE_REVOKE);
    groupRevoke.setGroupId("sales");
    groupRevoke.setResource(resource1);
    groupRevoke.setResourceId(ANY);
    groupRevoke.removePermission(READ);
    authorizationService.saveAuthorization(groupRevoke);

    // add READ for jonny
    Authorization userGrant = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    userGrant.setUserId("jonny");
    userGrant.setResource(resource1);
    userGrant.setResourceId(ANY);
    userGrant.addPermission(READ);
    authorizationService.saveAuthorization(userGrant);

    List<String> jonnysGroups = Arrays.asList("sales", "marketing");
    List<String> someOneElsesGroups = Collections.singletonList("marketing");

    // jonny can read
    assertTrue(authorizationService.isUserAuthorized("jonny", jonnysGroups, READ, resource1));
    assertTrue(authorizationService.isUserAuthorized("jonny", null, READ, resource1));

    // someone else in the same groups cannot
    assertFalse(authorizationService.isUserAuthorized("someone else", jonnysGroups, READ, resource1));

    // someone else in different groups can
    assertTrue(authorizationService.isUserAuthorized("someone else", someOneElsesGroups, READ, resource1));
  }

  @Test
  public void testEnabledAuthorizationCheck() {
    // given
    Resource resource1 = TestResource.RESOURCE1;

    // when
    boolean isAuthorized = authorizationService.isUserAuthorized("jonny", null, UPDATE, resource1);

    // then
    assertFalse(isAuthorized);
  }

  protected void cleanupAfterTest() {
    for (User user : identityService.createUserQuery().list()) {
      identityService.deleteUser(user.getId());
    }
    for (Authorization authorization : authorizationService.createAuthorizationQuery().list()) {
      authorizationService.deleteAuthorization(authorization.getId());
    }
  }
}
