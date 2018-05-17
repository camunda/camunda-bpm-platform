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
import static org.camunda.bpm.engine.authorization.Permissions.ACCESS;
import static org.camunda.bpm.engine.authorization.Permissions.ALL;
import static org.camunda.bpm.engine.authorization.Permissions.CREATE;
import static org.camunda.bpm.engine.authorization.Permissions.DELETE;
import static org.camunda.bpm.engine.authorization.Permissions.NONE;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Resources.DASHBOARD;
import static org.camunda.bpm.engine.authorization.Resources.REPORT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.test.api.authorization.MyResourceAuthorizationProvider;

/**
 * @author Daniel Meyer
 *
 */
public class AuthorizationServiceTest extends PluggableProcessEngineTestCase {

  protected String userId = "test";
  protected String groupId = "accounting";

  @Override
  protected void tearDown() throws Exception {
    cleanupAfterTest();
    super.tearDown();
  }

  public void testGlobalAuthorizationType() {
    Authorization globalAuthorization = authorizationService.createNewAuthorization(AUTH_TYPE_GLOBAL);
    // I can set userId = null
    globalAuthorization.setUserId(null);
    // I can set userId = ANY
    globalAuthorization.setUserId(ANY);

    try {
      // I cannot set anything else:
      globalAuthorization.setUserId("something");
      fail("exception expected");

    } catch (Exception e) {
      assertTextPresent("ENGINE-03028 Illegal value 'something' for userId for GLOBAL authorization. Must be '*'", e.getMessage());

    }

    // I can set groupId = null
    globalAuthorization.setGroupId(null);

    try {
      // I cannot set anything else:
      globalAuthorization.setGroupId("something");
      fail("exception expected");

    } catch (Exception e) {
      assertTextPresent("ENGINE-03027 Cannot use 'groupId' for GLOBAL authorization", e.getMessage());
    }
  }

  public void testGrantAuthorizationType() {
    Authorization grantAuthorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    // I can set userId = null
    grantAuthorization.setUserId(null);
    // I can set userId = ANY
    grantAuthorization.setUserId(ANY);
    // I can set anything else:
    grantAuthorization.setUserId("something");
    // I can set groupId = null
    grantAuthorization.setGroupId(null);
    // I can set anything else:
    grantAuthorization.setGroupId("something");
  }

  public void testRevokeAuthorizationType() {
    Authorization revokeAuthorization = authorizationService.createNewAuthorization(AUTH_TYPE_REVOKE);
    // I can set userId = null
    revokeAuthorization.setUserId(null);
    // I can set userId = ANY
    revokeAuthorization.setUserId(ANY);
    // I can set anything else:
    revokeAuthorization.setUserId("something");
    // I can set groupId = null
    revokeAuthorization.setGroupId(null);
    // I can set anything else:
    revokeAuthorization.setGroupId("something");
  }

  public void testDeleteNonExistingAuthorization() {

    try {
      authorizationService.deleteAuthorization("nonExisiting");
      fail();
    } catch (Exception e) {
      assertTextPresent("Authorization for Id 'nonExisiting' does not exist: authorization is null", e.getMessage());
    }

  }

  public void testCreateAuthorizationWithUserId() {

    TestResource resource1 = new TestResource("resource1",100);

    // initially, no authorization exists:
    assertEquals(0, authorizationService.createAuthorizationQuery().count());

    // simple create / delete with userId
    Authorization authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    authorization.setUserId("aUserId");
    authorization.setResource(resource1);

    // save the authorization
    authorizationService.saveAuthorization(authorization);
    // authorization exists
    assertEquals(1, authorizationService.createAuthorizationQuery().count());
    // delete the authorization
    authorizationService.deleteAuthorization(authorization.getId());
    // it's gone
    assertEquals(0, authorizationService.createAuthorizationQuery().count());

  }

  public void testCreateAuthorizationWithGroupId() {

    TestResource resource1 = new TestResource("resource1",100);

    // initially, no authorization exists:
    assertEquals(0, authorizationService.createAuthorizationQuery().count());

    // simple create / delete with userId
    Authorization authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    authorization.setGroupId("aGroupId");
    authorization.setResource(resource1);

    // save the authorization
    authorizationService.saveAuthorization(authorization);
    // authorization exists
    assertEquals(1, authorizationService.createAuthorizationQuery().count());
    // delete the authorization
    authorizationService.deleteAuthorization(authorization.getId());
    // it's gone
    assertEquals(0, authorizationService.createAuthorizationQuery().count());

  }

  public void testInvalidCreateAuthorization() {

    TestResource resource1 = new TestResource("resource1",100);

    // case 1: no user id & no group id ////////////

    Authorization authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    authorization.setResource(resource1);

    try {
      authorizationService.saveAuthorization(authorization);
      fail("exception expected");
    } catch(ProcessEngineException e) {
      assertTrue(e.getMessage().contains("Authorization must either have a 'userId' or a 'groupId'."));
    }

    // case 2: both user id & group id ////////////

    authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    authorization.setGroupId("someId");
    authorization.setUserId("someOtherId");
    authorization.setResource(resource1);

    try {
      authorizationService.saveAuthorization(authorization);
      fail("exception expected");
    } catch(ProcessEngineException e) {
      assertTextPresent("Authorization must either have a 'userId' or a 'groupId'.", e.getMessage());
    }

    // case 3: no resourceType ////////////

    authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    authorization.setUserId("someId");

    try {
      authorizationService.saveAuthorization(authorization);
      fail("exception expected");
    } catch(ProcessEngineException e) {
      assertTrue(e.getMessage().contains("Authorization 'resourceType' cannot be null."));
    }

    // case 4: no permissions /////////////////

    authorization = authorizationService.createNewAuthorization(AUTH_TYPE_REVOKE);
    authorization.setUserId("someId");

    try {
      authorizationService.saveAuthorization(authorization);
      fail("exception expected");
    } catch(ProcessEngineException e) {
      assertTrue(e.getMessage().contains("Authorization 'resourceType' cannot be null."));
    }
  }

  public void testUniqueUserConstraints() {

    TestResource resource1 = new TestResource("resource1",100);

    Authorization authorization1 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    Authorization authorization2 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);

    authorization1.setResource(resource1);
    authorization1.setResourceId("someId");
    authorization1.setUserId("someUser");

    authorization2.setResource(resource1);
    authorization2.setResourceId("someId");
    authorization2.setUserId("someUser");

    // the first one can be saved
    authorizationService.saveAuthorization(authorization1);

    // the second one cannot
    try {
      authorizationService.saveAuthorization(authorization2);
      fail("exception expected");
    } catch(ProcessEngineException e) {
      //expected
    }

    // but I can add a AUTH_TYPE_REVOKE auth

    Authorization authorization3 = authorizationService.createNewAuthorization(AUTH_TYPE_REVOKE);

    authorization3.setResource(resource1);
    authorization3.setResourceId("someId");
    authorization3.setUserId("someUser");

    authorizationService.saveAuthorization(authorization3);

    // but not a second

    Authorization authorization4 = authorizationService.createNewAuthorization(AUTH_TYPE_REVOKE);

    authorization4.setResource(resource1);
    authorization4.setResourceId("someId");
    authorization4.setUserId("someUser");

    try {
      authorizationService.saveAuthorization(authorization4);
      fail("exception expected");
    } catch(Exception e) {
      //expected
    }
  }

  public void testUniqueGroupConstraints() {

    TestResource resource1 = new TestResource("resource1",100);

    Authorization authorization1 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    Authorization authorization2 = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);

    authorization1.setResource(resource1);
    authorization1.setResourceId("someId");
    authorization1.setGroupId("someGroup");

    authorization2.setResource(resource1);
    authorization2.setResourceId("someId");
    authorization2.setGroupId("someGroup");

    // the first one can be saved
    authorizationService.saveAuthorization(authorization1);

    // the second one cannot
    try {
      authorizationService.saveAuthorization(authorization2);
      fail("exception expected");
    } catch(Exception e) {
      //expected
    }

    // but I can add a AUTH_TYPE_REVOKE auth

    Authorization authorization3 = authorizationService.createNewAuthorization(AUTH_TYPE_REVOKE);

    authorization3.setResource(resource1);
    authorization3.setResourceId("someId");
    authorization3.setGroupId("someGroup");

    authorizationService.saveAuthorization(authorization3);

    // but not a second

    Authorization authorization4 = authorizationService.createNewAuthorization(AUTH_TYPE_REVOKE);

    authorization4.setResource(resource1);
    authorization4.setResourceId("someId");
    authorization4.setGroupId("someGroup");

    try {
      authorizationService.saveAuthorization(authorization4);
      fail("exception expected");
    } catch(Exception e) {
      //expected
    }

  }

  public void testGlobalUniqueConstraints() {

    TestResource resource1 = new TestResource("resource1",100);

    Authorization authorization1 = authorizationService.createNewAuthorization(AUTH_TYPE_GLOBAL);
    Authorization authorization2 = authorizationService.createNewAuthorization(AUTH_TYPE_GLOBAL);

    authorization1.setResource(resource1);
    authorization1.setResourceId("someId");

    authorization2.setResource(resource1);
    authorization2.setResourceId("someId");

    // the first one can be saved
    authorizationService.saveAuthorization(authorization1);

    // the second one cannot
    try {
      authorizationService.saveAuthorization(authorization2);
      fail("exception expected");
    } catch(Exception e) {
      //expected
    }
  }

  public void testUpdateNewAuthorization() {

    TestResource resource1 = new TestResource("resource1",100);
    TestResource resource2 = new TestResource("resource1",101);

    Authorization authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    authorization.setUserId("aUserId");
    authorization.setResource(resource1);
    authorization.setResourceId("aResourceId");
    authorization.addPermission(ACCESS);

    // save the authorization
    authorizationService.saveAuthorization(authorization);

    // validate authorization
    Authorization savedAuthorization = authorizationService.createAuthorizationQuery().singleResult();
    assertEquals("aUserId", savedAuthorization.getUserId());
    assertEquals(resource1.resourceType(), savedAuthorization.getResourceType());
    assertEquals("aResourceId", savedAuthorization.getResourceId());
    assertTrue(savedAuthorization.isPermissionGranted(ACCESS));

    // update authorization
    authorization.setUserId("anotherUserId");
    authorization.setResource(resource2);
    authorization.setResourceId("anotherResourceId");
    authorization.addPermission(DELETE);
    authorizationService.saveAuthorization(authorization);

    // validate authorization updated
    savedAuthorization = authorizationService.createAuthorizationQuery().singleResult();
    assertEquals("anotherUserId", savedAuthorization.getUserId());
    assertEquals(resource2.resourceType(), savedAuthorization.getResourceType());
    assertEquals("anotherResourceId", savedAuthorization.getResourceId());
    assertTrue(savedAuthorization.isPermissionGranted(ACCESS));
    assertTrue(savedAuthorization.isPermissionGranted(DELETE));

  }

  public void testUpdatePersistentAuthorization() {

    TestResource resource1 = new TestResource("resource1",100);
    TestResource resource2 = new TestResource("resource1",101);

    Authorization authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    authorization.setUserId("aUserId");
    authorization.setResource(resource1);
    authorization.setResourceId("aResourceId");
    authorization.addPermission(ACCESS);

    // save the authorization
    authorizationService.saveAuthorization(authorization);

    // validate authorization
    Authorization savedAuthorization = authorizationService.createAuthorizationQuery().singleResult();
    assertEquals("aUserId", savedAuthorization.getUserId());
    assertEquals(resource1.resourceType(), savedAuthorization.getResourceType());
    assertEquals("aResourceId", savedAuthorization.getResourceId());
    assertTrue(savedAuthorization.isPermissionGranted(ACCESS));

    // update authorization
    savedAuthorization.setUserId("anotherUserId");
    savedAuthorization.setResource(resource2);
    savedAuthorization.setResourceId("anotherResourceId");
    savedAuthorization.addPermission(DELETE);
    authorizationService.saveAuthorization(savedAuthorization);

    // validate authorization updated
    savedAuthorization = authorizationService.createAuthorizationQuery().singleResult();
    assertEquals("anotherUserId", savedAuthorization.getUserId());
    assertEquals(resource2.resourceType(), savedAuthorization.getResourceType());
    assertEquals("anotherResourceId", savedAuthorization.getResourceId());
    assertTrue(savedAuthorization.isPermissionGranted(ACCESS));
    assertTrue(savedAuthorization.isPermissionGranted(DELETE));

  }

  public void testPermissions() {

    Authorization authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);

    assertEquals(1, authorization.getPermissions(Permissions.values()).length);

    assertFalse(authorization.isPermissionGranted(ACCESS));
    assertFalse(authorization.isPermissionGranted(DELETE));
    assertFalse(authorization.isPermissionGranted(READ));
    assertFalse(authorization.isPermissionGranted(UPDATE));

    authorization.addPermission(ACCESS);
    assertTrue(authorization.isPermissionGranted(ACCESS));
    assertFalse(authorization.isPermissionGranted(DELETE));
    assertFalse(authorization.isPermissionGranted(READ));
    assertFalse(authorization.isPermissionGranted(UPDATE));

    authorization.addPermission(DELETE);
    assertTrue(authorization.isPermissionGranted(ACCESS));
    assertTrue(authorization.isPermissionGranted(DELETE));
    assertFalse(authorization.isPermissionGranted(READ));
    assertFalse(authorization.isPermissionGranted(UPDATE));

    authorization.addPermission(READ);
    assertTrue(authorization.isPermissionGranted(ACCESS));
    assertTrue(authorization.isPermissionGranted(DELETE));
    assertTrue(authorization.isPermissionGranted(READ));
    assertFalse(authorization.isPermissionGranted(UPDATE));

    authorization.addPermission(UPDATE);
    assertTrue(authorization.isPermissionGranted(ACCESS));
    assertTrue(authorization.isPermissionGranted(DELETE));
    assertTrue(authorization.isPermissionGranted(READ));
    assertTrue(authorization.isPermissionGranted(UPDATE));

    authorization.removePermission(ACCESS);
    assertFalse(authorization.isPermissionGranted(ACCESS));
    assertTrue(authorization.isPermissionGranted(DELETE));
    assertTrue(authorization.isPermissionGranted(READ));
    assertTrue(authorization.isPermissionGranted(UPDATE));

    authorization.removePermission(DELETE);
    assertFalse(authorization.isPermissionGranted(ACCESS));
    assertFalse(authorization.isPermissionGranted(DELETE));
    assertTrue(authorization.isPermissionGranted(READ));
    assertTrue(authorization.isPermissionGranted(UPDATE));

    authorization.removePermission(READ);
    assertFalse(authorization.isPermissionGranted(ACCESS));
    assertFalse(authorization.isPermissionGranted(DELETE));
    assertFalse(authorization.isPermissionGranted(READ));
    assertTrue(authorization.isPermissionGranted(UPDATE));

    authorization.removePermission(UPDATE);
    assertFalse(authorization.isPermissionGranted(ACCESS));
    assertFalse(authorization.isPermissionGranted(DELETE));
    assertFalse(authorization.isPermissionGranted(READ));
    assertFalse(authorization.isPermissionGranted(UPDATE));

  }

  public void testGrantAuthPermissions() {

    AuthorizationEntity authorization = new AuthorizationEntity(AUTH_TYPE_GRANT);
    assertFalse(authorization.isPermissionGranted(ALL));
    assertTrue(authorization.isPermissionGranted(NONE));
    List<Permission> perms = Arrays.asList(authorization.getPermissions(Permissions.values()));
    assertTrue(perms.contains(NONE));
    assertEquals(1, perms.size());

    authorization.addPermission(READ);
    perms = Arrays.asList(authorization.getPermissions(Permissions.values()));
    assertTrue(perms.contains(NONE));
    assertTrue(perms.contains(READ));
    assertEquals(2, perms.size());
    assertTrue(authorization.isPermissionGranted(READ));
    assertTrue(authorization.isPermissionGranted(NONE)); // (none is always granted => you are always authorized to do nothing)

    try {
      authorization.isPermissionRevoked(READ);
      fail("Exception expected");
    } catch (IllegalStateException e) {
      assertTextPresent("ENGINE-03026 Method 'isPermissionRevoked' cannot be used for authorization with type 'GRANT'.", e.getMessage());
    }

  }

  public void testGlobalAuthPermissions() {

    AuthorizationEntity authorization = new AuthorizationEntity(AUTH_TYPE_GRANT);
    assertFalse(authorization.isPermissionGranted(ALL));
    assertTrue(authorization.isPermissionGranted(NONE));
    List<Permission> perms = Arrays.asList(authorization.getPermissions(Permissions.values()));
    assertTrue(perms.contains(NONE));
    assertEquals(1, perms.size());

    authorization.addPermission(READ);
    perms = Arrays.asList(authorization.getPermissions(Permissions.values()));
    assertTrue(perms.contains(NONE));
    assertTrue(perms.contains(READ));
    assertEquals(2, perms.size());
    assertTrue(authorization.isPermissionGranted(READ));
    assertTrue(authorization.isPermissionGranted(NONE)); // (none is always granted => you are always authorized to do nothing)

    try {
      authorization.isPermissionRevoked(READ);
      fail("Exception expected");
    } catch (IllegalStateException e) {
      assertTextPresent("ENGINE-03026 Method 'isPermissionRevoked' cannot be used for authorization with type 'GRANT'.", e.getMessage());
    }

  }

  public void testRevokeAuthPermissions() {

    AuthorizationEntity authorization = new AuthorizationEntity(AUTH_TYPE_REVOKE);
    assertFalse(authorization.isPermissionRevoked(ALL));
    List<Permission> perms = Arrays.asList(authorization.getPermissions(Permissions.values()));
    assertEquals(0, perms.size());

    authorization.removePermission(READ);
    perms = Arrays.asList(authorization.getPermissions(Permissions.values()));
    assertTrue(perms.contains(READ));
    assertTrue(perms.contains(ALL));
    assertEquals(2, perms.size());

    try {
      authorization.isPermissionGranted(READ);
      fail("Exception expected");
    } catch (IllegalStateException e) {
      assertTextPresent("ENGINE-03026 Method 'isPermissionGranted' cannot be used for authorization with type 'REVOKE'.", e.getMessage());
    }

  }

  public void testGlobalGrantAuthorizationCheck() {
    TestResource resource1 = new TestResource("resource1",100);

    // create global authorization which grants all permissions to all users (on resource1):
    Authorization globalAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GLOBAL);
    globalAuth.setResource(resource1);
    globalAuth.setResourceId(ANY);
    globalAuth.addPermission(ALL);
    authorizationService.saveAuthorization(globalAuth);

    List<String> jonnysGroups = Arrays.asList(new String[]{"sales", "marketing"});
    List<String> someOneElsesGroups = Arrays.asList(new String[]{"marketing"});

    // this authorizes any user to do anything in this resource:
    assertTrue(authorizationService.isUserAuthorized("jonny", null, ALL, resource1));
    assertTrue(authorizationService.isUserAuthorized("jonny", jonnysGroups, ALL, resource1));
    assertTrue(authorizationService.isUserAuthorized("someone", null, CREATE, resource1));
    assertTrue(authorizationService.isUserAuthorized("someone", someOneElsesGroups, CREATE, resource1));
    assertTrue(authorizationService.isUserAuthorized("someone else", null, DELETE, resource1));
    assertTrue(authorizationService.isUserAuthorized("jonny", null, ALL, resource1, "someId"));
    assertTrue(authorizationService.isUserAuthorized("jonny", jonnysGroups, ALL, resource1, "someId"));
    assertTrue(authorizationService.isUserAuthorized("someone", null, CREATE, resource1, "someId"));
    assertTrue(authorizationService.isUserAuthorized("someone else", null, DELETE, resource1, "someOtherId"));
  }

  public void testDisabledAuthorizationCheck() {
    // given
    TestResource resource1 = new TestResource("resource1", 100);

    // when
    boolean isAuthorized = authorizationService.isUserAuthorized("jonny", null, UPDATE, resource1);

    // then
    assertTrue(isAuthorized);
  }

  public void testConcurrentIsUserAuthorized() throws Exception {
    int threadCount = 2;
    int invocationCount = 500;
    ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

    try {
      ArrayList<Callable<Exception>> callables = new ArrayList<Callable<Exception>>();

      for (int i = 0; i < invocationCount; i++) {
        callables.add(new Callable<Exception>() {
          public Exception call() throws Exception {
            try {
              authorizationService.isUserAuthorized(null, null, null, null, null);
            }
            catch (Exception e) {
              return e;
            }
            return null;
          }
        });
      }

      List<Future<Exception>> futures = executorService.invokeAll(callables);

      for (Future<Exception> future : futures) {
        Exception exception = future.get();
        if (exception != null) {
          fail("No exception expected: " + exception.getMessage());
        }
      }

    }
    finally {
      // reset original logging level
      executorService.shutdownNow();
      executorService.awaitTermination(10, TimeUnit.SECONDS);
    }

  }

  public void testReportResourceAuthorization() {
    MyResourceAuthorizationProvider.clearProperties();

    Authorization authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    authorization.setUserId(userId);
    authorization.addPermission(ALL);
    authorization.setResource(REPORT);
    authorization.setResourceId(ANY);
    authorizationService.saveAuthorization(authorization);

    assertEquals(true, authorizationService.isUserAuthorized(userId, Arrays.asList(groupId), ALL, REPORT));
  }

  public void testDashboardResourceAuthorization() {
    MyResourceAuthorizationProvider.clearProperties();

    Authorization authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    authorization.setUserId(userId);
    authorization.addPermission(ALL);
    authorization.setResource(DASHBOARD);
    authorization.setResourceId(ANY);
    authorizationService.saveAuthorization(authorization);

    assertEquals(true, authorizationService.isUserAuthorized(userId, Arrays.asList(groupId), ALL, DASHBOARD));
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
