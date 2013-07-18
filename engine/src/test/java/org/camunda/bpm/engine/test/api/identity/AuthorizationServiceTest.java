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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.identity.Authorization;
import org.camunda.bpm.engine.identity.Permissions;
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
    
    // initially, no authorization exists:
    assertEquals(0, authorizationService.createAuthorizationQuery().count());
    
    // simple create / delete with userId    
    Authorization authorization = authorizationService.createNewAuthorization();
    authorization.setUserId("aUserId");
    authorization.setResourceType("processDefinition");
    
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
    
    // initially, no authorization exists:
    assertEquals(0, authorizationService.createAuthorizationQuery().count());
    
    // simple create / delete with userId    
    Authorization authorization = authorizationService.createNewAuthorization();
    authorization.setGroupId("aGroupId");
    authorization.setResourceType("processDefinition");
    
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
        
    // case 1: no user id & no group id ////////////
    
    Authorization authorization = authorizationService.createNewAuthorization();    
    authorization.setResourceType("processDefinition");
    
    try {
      authorizationService.saveAuthorization(authorization);
      fail("exception expected"); 
    } catch(ProcessEngineException e) {
      assertTrue(e.getMessage().contains("Authorization must either have a 'userId' or a 'groupId'."));
    }

    // case 2: both user id & group id ////////////
    
    authorization = authorizationService.createNewAuthorization();
    authorization.setGroupId("someId");
    authorization.setUserId("someOtherId");
    authorization.setResourceType("processDefinition");
    
    try {
      authorizationService.saveAuthorization(authorization);
      fail("exception expected"); 
    } catch(ProcessEngineException e) {
      assertTrue(e.getMessage().contains("Authorization cannot define 'userId' or a 'groupId' at the same time."));
    }
    
    // case 3: no resourceType ////////////
    
    authorization = authorizationService.createNewAuthorization();
    authorization.setUserId("someId");
    
    try {
      authorizationService.saveAuthorization(authorization);
      fail("exception expected"); 
    } catch(ProcessEngineException e) {
      assertTrue(e.getMessage().contains("Authorization 'resourceType' cannot be null."));
    }
    
    // case 4: no permissions /////////////////
    
    authorization = authorizationService.createNewAuthorization();
    authorization.setUserId("someId");
    
    try {
      authorizationService.saveAuthorization(authorization);
      fail("exception expected"); 
    } catch(ProcessEngineException e) {
      assertTrue(e.getMessage().contains("Authorization 'resourceType' cannot be null."));
    }   
  }
  
  public void testUniqueUserConstraints() {
    
    Authorization authorization1 = authorizationService.createNewAuthorization();
    Authorization authorization2 = authorizationService.createNewAuthorization();
    
    authorization1.setResourceType("someType");
    authorization1.setResourceId("someId");
    authorization1.setUserId("someUser");
    
    authorization2.setResourceType("someType");
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
    
  }
  
  public void testUniqueGroupConstraints() {
    
    Authorization authorization1 = authorizationService.createNewAuthorization();
    Authorization authorization2 = authorizationService.createNewAuthorization();
    
    authorization1.setResourceType("someType");
    authorization1.setResourceId("someId");
    authorization1.setGroupId("someGroup");
    
    authorization2.setResourceType("someType");
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
    
  }
  
  public void testUpdateNewAuthorization() {
        
    Authorization authorization = authorizationService.createNewAuthorization();
    authorization.setUserId("aUserId");
    authorization.setResourceType("aResourceType");
    authorization.setResourceId("aResourceId");
    authorization.addPermission(Permissions.ACCESS);
    
    // save the authorization
    authorizationService.saveAuthorization(authorization);
    
    // validate authorization
    Authorization savedAuthorization = authorizationService.createAuthorizationQuery().singleResult();
    assertEquals("aUserId", savedAuthorization.getUserId());
    assertEquals("aResourceType", savedAuthorization.getResourceType());
    assertEquals("aResourceId", savedAuthorization.getResourceId());
    assertTrue(savedAuthorization.hasPermission(Permissions.ACCESS));
    
    // update authorization
    authorization.setUserId("anotherUserId");
    authorization.setResourceType("anotherResourceType");
    authorization.setResourceId("anotherResourceId");
    authorization.addPermission(Permissions.DELETE);    
    authorizationService.saveAuthorization(authorization);
    
    // validate authorization updated
    savedAuthorization = authorizationService.createAuthorizationQuery().singleResult();
    assertEquals("anotherUserId", savedAuthorization.getUserId());
    assertEquals("anotherResourceType", savedAuthorization.getResourceType());
    assertEquals("anotherResourceId", savedAuthorization.getResourceId());
    assertTrue(savedAuthorization.hasPermission(Permissions.ACCESS));
    assertTrue(savedAuthorization.hasPermission(Permissions.DELETE));
        
  }
  
  public void testUpdatePersistentAuthorization() {
    
    Authorization authorization = authorizationService.createNewAuthorization();
    authorization.setUserId("aUserId");
    authorization.setResourceType("aResourceType");
    authorization.setResourceId("aResourceId");
    authorization.addPermission(Permissions.ACCESS);
    
    // save the authorization
    authorizationService.saveAuthorization(authorization);
    
    // validate authorization
    Authorization savedAuthorization = authorizationService.createAuthorizationQuery().singleResult();
    assertEquals("aUserId", savedAuthorization.getUserId());
    assertEquals("aResourceType", savedAuthorization.getResourceType());
    assertEquals("aResourceId", savedAuthorization.getResourceId());
    assertTrue(savedAuthorization.hasPermission(Permissions.ACCESS));
    
    // update authorization
    savedAuthorization.setUserId("anotherUserId");
    savedAuthorization.setResourceType("anotherResourceType");
    savedAuthorization.setResourceId("anotherResourceId");
    savedAuthorization.addPermission(Permissions.DELETE);    
    authorizationService.saveAuthorization(savedAuthorization);
    
    // validate authorization updated
    savedAuthorization = authorizationService.createAuthorizationQuery().singleResult();
    assertEquals("anotherUserId", savedAuthorization.getUserId());
    assertEquals("anotherResourceType", savedAuthorization.getResourceType());
    assertEquals("anotherResourceId", savedAuthorization.getResourceId());
    assertTrue(savedAuthorization.hasPermission(Permissions.ACCESS));
    assertTrue(savedAuthorization.hasPermission(Permissions.DELETE));
        
  }
    
  public void testPermissions() {
    
    Authorization authorization = authorizationService.createNewAuthorization();

    assertEquals(0, authorization.getPermissions());    
    
    assertFalse(authorization.hasPermission(Permissions.ACCESS));
    assertFalse(authorization.hasPermission(Permissions.DELETE));
    assertFalse(authorization.hasPermission(Permissions.READ));
    assertFalse(authorization.hasPermission(Permissions.UPDATE));
    
    authorization.addPermission(Permissions.ACCESS);
    assertTrue(authorization.hasPermission(Permissions.ACCESS));
    assertFalse(authorization.hasPermission(Permissions.DELETE));
    assertFalse(authorization.hasPermission(Permissions.READ));
    assertFalse(authorization.hasPermission(Permissions.UPDATE));
    
    authorization.addPermission(Permissions.DELETE);
    assertTrue(authorization.hasPermission(Permissions.ACCESS));
    assertTrue(authorization.hasPermission(Permissions.DELETE));
    assertFalse(authorization.hasPermission(Permissions.READ));
    assertFalse(authorization.hasPermission(Permissions.UPDATE));
   
    authorization.addPermission(Permissions.READ);
    assertTrue(authorization.hasPermission(Permissions.ACCESS));
    assertTrue(authorization.hasPermission(Permissions.DELETE));
    assertTrue(authorization.hasPermission(Permissions.READ));
    assertFalse(authorization.hasPermission(Permissions.UPDATE));
    
    authorization.addPermission(Permissions.UPDATE);
    assertTrue(authorization.hasPermission(Permissions.ACCESS));
    assertTrue(authorization.hasPermission(Permissions.DELETE));
    assertTrue(authorization.hasPermission(Permissions.READ));
    assertTrue(authorization.hasPermission(Permissions.UPDATE));
    
    authorization.removePermission(Permissions.ACCESS);
    assertFalse(authorization.hasPermission(Permissions.ACCESS));
    assertTrue(authorization.hasPermission(Permissions.DELETE));
    assertTrue(authorization.hasPermission(Permissions.READ));
    assertTrue(authorization.hasPermission(Permissions.UPDATE));
    
    authorization.removePermission(Permissions.DELETE);
    assertFalse(authorization.hasPermission(Permissions.ACCESS));
    assertFalse(authorization.hasPermission(Permissions.DELETE));
    assertTrue(authorization.hasPermission(Permissions.READ));
    assertTrue(authorization.hasPermission(Permissions.UPDATE));
    
    authorization.removePermission(Permissions.READ);
    assertFalse(authorization.hasPermission(Permissions.ACCESS));
    assertFalse(authorization.hasPermission(Permissions.DELETE));
    assertFalse(authorization.hasPermission(Permissions.READ));
    assertTrue(authorization.hasPermission(Permissions.UPDATE));
    
    authorization.removePermission(Permissions.UPDATE);
    assertFalse(authorization.hasPermission(Permissions.ACCESS));
    assertFalse(authorization.hasPermission(Permissions.DELETE));
    assertFalse(authorization.hasPermission(Permissions.READ));
    assertFalse(authorization.hasPermission(Permissions.UPDATE));
    
  }
  
  public void testAuthorizationCheckEmptyDb() {
    TestResource resource1 = new TestResource("resource1");
    TestResource resource2 = new TestResource("resource2");
    
    // if no authorizations are in Db, nothing is authorized
    assertFalse(authorizationService.isUserAuthorized("jonny", null, Permissions.ALL, resource1));
    assertFalse(authorizationService.isUserAuthorized("someone", null, Permissions.CREATE, resource2));
    assertFalse(authorizationService.isUserAuthorized("someone else", null, Permissions.DELETE, resource1));
    assertFalse(authorizationService.isUserAuthorized("jonny", null, Permissions.ALL, resource1, "someId"));
    assertFalse(authorizationService.isUserAuthorized("someone", null, Permissions.CREATE, resource2, "someId"));
    assertFalse(authorizationService.isUserAuthorized("someone else", null, Permissions.DELETE, resource1, "someOtherId"));
    
  }
  
  public void testGlobalGrantAuthorizationCheck() {
    TestResource resource1 = new TestResource("resource1");

    // create global authorization which grants all permissions to all users  (on resource1):
    Authorization globalAuth = authorizationService.createNewAuthorization();
    globalAuth.setUserId(Authorization.ANY);
    globalAuth.setResourceType(resource1.getId());
    globalAuth.setResourceId(Authorization.ANY);
    globalAuth.addPermission(Permissions.ALL);    
    authorizationService.saveAuthorization(globalAuth);
    
    // this authorizes any user to do anything in this resource:
    assertTrue(authorizationService.isUserAuthorized("jonny", null, Permissions.ALL, resource1));    
    assertTrue(authorizationService.isUserAuthorized("someone", null, Permissions.CREATE, resource1));
    assertTrue(authorizationService.isUserAuthorized("someone else", null, Permissions.DELETE, resource1));
    assertTrue(authorizationService.isUserAuthorized("jonny", null, Permissions.ALL, resource1, "someId"));
    assertTrue(authorizationService.isUserAuthorized("someone", null, Permissions.CREATE, resource1, "someId"));
    assertTrue(authorizationService.isUserAuthorized("someone else", null, Permissions.DELETE, resource1, "someOtherId"));
  }
  
  public void testUserOverrideGlobalGrantAuthorizationCheck() {
    TestResource resource1 = new TestResource("resource1");

    // create global authorization which grants all permissions to all users  (on resource1):
    Authorization globalGrant = authorizationService.createNewAuthorization();
    globalGrant.setUserId(Authorization.ANY);
    globalGrant.setResourceType(resource1.getId());
    globalGrant.setResourceId(Authorization.ANY);
    globalGrant.addPermission(Permissions.ALL);    
    authorizationService.saveAuthorization(globalGrant);
    
    // revoke READ for jonny
    Authorization localRevoke = authorizationService.createNewAuthorization();
    localRevoke.setUserId("jonny");
    localRevoke.setResourceType(resource1.getId());
    localRevoke.setResourceId(Authorization.ANY);
    localRevoke.addPermission(Permissions.ALL);
    localRevoke.removePermission(Permissions.READ);
    authorizationService.saveAuthorization(localRevoke);
    
    // jonny does not have ALL permissions
    assertFalse(authorizationService.isUserAuthorized("jonny", null, Permissions.ALL, resource1));
    // jonny can't read
    assertFalse(authorizationService.isUserAuthorized("jonny", null, Permissions.READ, resource1));
    // someone else can
    assertTrue(authorizationService.isUserAuthorized("someone else", null, Permissions.ALL, resource1));
    assertTrue(authorizationService.isUserAuthorized("someone else", null, Permissions.READ, resource1));
    // jonny can still delete
    assertTrue(authorizationService.isUserAuthorized("jonny", null, Permissions.DELETE, resource1));            
  }
  
  public void testUserOverrideGlobalRevokeAuthorizationCheck() {
    TestResource resource1 = new TestResource("resource1");

    // create global authorization which grants all permissions to all users  (on resource1):
    Authorization globalGrant = authorizationService.createNewAuthorization();
    globalGrant.setUserId(Authorization.ANY);
    globalGrant.setResourceType(resource1.getId());
    globalGrant.setResourceId(Authorization.ANY);
    globalGrant.removePermission(Permissions.ALL);    
    authorizationService.saveAuthorization(globalGrant);
    
    // add READ for jonny
    Authorization localRevoke = authorizationService.createNewAuthorization();
    localRevoke.setUserId("jonny");
    localRevoke.setResourceType(resource1.getId());
    localRevoke.setResourceId(Authorization.ANY);
    localRevoke.addPermission(Permissions.READ);
    authorizationService.saveAuthorization(localRevoke);
    
    // jonny does not have ALL permissions
    assertFalse(authorizationService.isUserAuthorized("jonny", null, Permissions.ALL, resource1));
    // jonny can read
    assertTrue(authorizationService.isUserAuthorized("jonny", null, Permissions.READ, resource1));
    // jonny can't delete
    assertFalse(authorizationService.isUserAuthorized("jonny", null, Permissions.DELETE, resource1));
    
    // someone else can do nothing
    assertFalse(authorizationService.isUserAuthorized("someone else", null, Permissions.ALL, resource1));
    assertFalse(authorizationService.isUserAuthorized("someone else", null, Permissions.READ, resource1));
    assertFalse(authorizationService.isUserAuthorized("someone else", null, Permissions.DELETE, resource1));
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
