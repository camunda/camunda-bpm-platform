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
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;

/**
 * @author Daniel Meyer
 *
 */
public class AuthorizationServiceTest extends PluggableProcessEngineTestCase {
  
  @Override
  protected void tearDown() throws Exception {
    cleanupAfterTest();
    super.tearDown();
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
      assertTrue(e.getMessage().contains("Authorization cannot define 'userId' or a 'groupId' at the same time."));
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
    } catch(Exception e) {
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
    assertTrue(savedAuthorization.hasPermission(ACCESS));
    
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
    assertTrue(savedAuthorization.hasPermission(ACCESS));
    assertTrue(savedAuthorization.hasPermission(DELETE));
        
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
    assertTrue(savedAuthorization.hasPermission(ACCESS));
    
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
    assertTrue(savedAuthorization.hasPermission(ACCESS));
    assertTrue(savedAuthorization.hasPermission(DELETE));
        
  }
    
  public void testPermissions() {
    
    Authorization authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);

    assertEquals(0, authorization.getPermissions());    
    
    assertFalse(authorization.hasPermission(ACCESS));
    assertFalse(authorization.hasPermission(DELETE));
    assertFalse(authorization.hasPermission(READ));
    assertFalse(authorization.hasPermission(UPDATE));
    
    authorization.addPermission(ACCESS);
    assertTrue(authorization.hasPermission(ACCESS));
    assertFalse(authorization.hasPermission(DELETE));
    assertFalse(authorization.hasPermission(READ));
    assertFalse(authorization.hasPermission(UPDATE));
    
    authorization.addPermission(DELETE);
    assertTrue(authorization.hasPermission(ACCESS));
    assertTrue(authorization.hasPermission(DELETE));
    assertFalse(authorization.hasPermission(READ));
    assertFalse(authorization.hasPermission(UPDATE));
   
    authorization.addPermission(READ);
    assertTrue(authorization.hasPermission(ACCESS));
    assertTrue(authorization.hasPermission(DELETE));
    assertTrue(authorization.hasPermission(READ));
    assertFalse(authorization.hasPermission(UPDATE));
    
    authorization.addPermission(UPDATE);
    assertTrue(authorization.hasPermission(ACCESS));
    assertTrue(authorization.hasPermission(DELETE));
    assertTrue(authorization.hasPermission(READ));
    assertTrue(authorization.hasPermission(UPDATE));
    
    authorization.removePermission(ACCESS);
    assertFalse(authorization.hasPermission(ACCESS));
    assertTrue(authorization.hasPermission(DELETE));
    assertTrue(authorization.hasPermission(READ));
    assertTrue(authorization.hasPermission(UPDATE));
    
    authorization.removePermission(DELETE);
    assertFalse(authorization.hasPermission(ACCESS));
    assertFalse(authorization.hasPermission(DELETE));
    assertTrue(authorization.hasPermission(READ));
    assertTrue(authorization.hasPermission(UPDATE));
    
    authorization.removePermission(READ);
    assertFalse(authorization.hasPermission(ACCESS));
    assertFalse(authorization.hasPermission(DELETE));
    assertFalse(authorization.hasPermission(READ));
    assertTrue(authorization.hasPermission(UPDATE));
    
    authorization.removePermission(UPDATE);
    assertFalse(authorization.hasPermission(ACCESS));
    assertFalse(authorization.hasPermission(DELETE));
    assertFalse(authorization.hasPermission(READ));
    assertFalse(authorization.hasPermission(UPDATE));
    
  }
  
  public void testAuthorizationCheckEmptyDb() {
    TestResource resource1 = new TestResource("resource1",100);
    TestResource resource2 = new TestResource("resource2",101);
    
    List<String> jonnysGroups = Arrays.asList(new String[]{"sales", "marketing"});
    List<String> someOneElsesGroups = Arrays.asList(new String[]{"marketing"});
    
    // if no authorizations are in Db, everything is authorized
    assertTrue(authorizationService.isUserAuthorized("jonny", jonnysGroups, ALL, resource1));
    assertTrue(authorizationService.isUserAuthorized("someone", someOneElsesGroups, CREATE, resource2));
    assertTrue(authorizationService.isUserAuthorized("someone else", null, DELETE, resource1));
    assertTrue(authorizationService.isUserAuthorized("jonny", jonnysGroups, ALL, resource1, "someId"));
    assertTrue(authorizationService.isUserAuthorized("someone", someOneElsesGroups, CREATE, resource2, "someId"));
    assertTrue(authorizationService.isUserAuthorized("someone else", null, DELETE, resource1, "someOtherId"));
    
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
  
  public void testUserOverrideGlobalGrantAuthorizationCheck() {
    TestResource resource1 = new TestResource("resource1",100);

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
    
    List<String> jonnysGroups = Arrays.asList(new String[]{"sales", "marketing"});
    List<String> someOneElsesGroups = Arrays.asList(new String[]{"marketing"});
    
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
  
  public void testGroupOverrideGlobalGrantAuthorizationCheck() {
    TestResource resource1 = new TestResource("resource1",100);

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
        
    List<String> jonnysGroups = Arrays.asList(new String[]{"sales", "marketing"});
    List<String> someOneElsesGroups = Arrays.asList(new String[]{"marketing"});
    
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
  
  public void testUserOverrideGroupOverrideGlobalAuthorizationCheck() {
    TestResource resource1 = new TestResource("resource1",100);

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
        
    List<String> jonnysGroups = Arrays.asList(new String[]{"sales", "marketing"});
    List<String> someOneElsesGroups = Arrays.asList(new String[]{"marketing"});
    
    // jonny can read
    assertTrue(authorizationService.isUserAuthorized("jonny", jonnysGroups, READ, resource1));
    assertTrue(authorizationService.isUserAuthorized("jonny", null, READ, resource1));

    // someone else in the same groups cannot
    assertFalse(authorizationService.isUserAuthorized("someone else", jonnysGroups, READ, resource1));
    
    // someone else in different groups can
    assertTrue(authorizationService.isUserAuthorized("someone else", someOneElsesGroups, READ, resource1));
    
  }
  
  public void testUserOverrideGlobalRevokeAuthorizationCheck() {
    TestResource resource1 = new TestResource("resource1", 100);

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
    
  protected void cleanupAfterTest() {
    for (User user : identityService.createUserQuery().list()) {
      identityService.deleteUser(user.getId());      
    }
    for (Authorization authorization : authorizationService.createAuthorizationQuery().list()) {
      authorizationService.deleteAuthorization(authorization.getId());
    }
  }


}
