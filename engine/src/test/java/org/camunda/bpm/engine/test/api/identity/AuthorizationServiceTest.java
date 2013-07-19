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

import static org.camunda.bpm.engine.identity.Authorization.ANY;
import static org.camunda.bpm.engine.identity.Permissions.ACCESS;
import static org.camunda.bpm.engine.identity.Permissions.ALL;
import static org.camunda.bpm.engine.identity.Permissions.CREATE;
import static org.camunda.bpm.engine.identity.Permissions.DELETE;
import static org.camunda.bpm.engine.identity.Permissions.READ;
import static org.camunda.bpm.engine.identity.Permissions.UPDATE;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.identity.Authorization;
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
    authorization.addPermission(ACCESS);
    
    // save the authorization
    authorizationService.saveAuthorization(authorization);
    
    // validate authorization
    Authorization savedAuthorization = authorizationService.createAuthorizationQuery().singleResult();
    assertEquals("aUserId", savedAuthorization.getUserId());
    assertEquals("aResourceType", savedAuthorization.getResourceType());
    assertEquals("aResourceId", savedAuthorization.getResourceId());
    assertTrue(savedAuthorization.hasPermission(ACCESS));
    
    // update authorization
    authorization.setUserId("anotherUserId");
    authorization.setResourceType("anotherResourceType");
    authorization.setResourceId("anotherResourceId");
    authorization.addPermission(DELETE);    
    authorizationService.saveAuthorization(authorization);
    
    // validate authorization updated
    savedAuthorization = authorizationService.createAuthorizationQuery().singleResult();
    assertEquals("anotherUserId", savedAuthorization.getUserId());
    assertEquals("anotherResourceType", savedAuthorization.getResourceType());
    assertEquals("anotherResourceId", savedAuthorization.getResourceId());
    assertTrue(savedAuthorization.hasPermission(ACCESS));
    assertTrue(savedAuthorization.hasPermission(DELETE));
        
  }
  
  public void testUpdatePersistentAuthorization() {
    
    Authorization authorization = authorizationService.createNewAuthorization();
    authorization.setUserId("aUserId");
    authorization.setResourceType("aResourceType");
    authorization.setResourceId("aResourceId");
    authorization.addPermission(ACCESS);
    
    // save the authorization
    authorizationService.saveAuthorization(authorization);
    
    // validate authorization
    Authorization savedAuthorization = authorizationService.createAuthorizationQuery().singleResult();
    assertEquals("aUserId", savedAuthorization.getUserId());
    assertEquals("aResourceType", savedAuthorization.getResourceType());
    assertEquals("aResourceId", savedAuthorization.getResourceId());
    assertTrue(savedAuthorization.hasPermission(ACCESS));
    
    // update authorization
    savedAuthorization.setUserId("anotherUserId");
    savedAuthorization.setResourceType("anotherResourceType");
    savedAuthorization.setResourceId("anotherResourceId");
    savedAuthorization.addPermission(DELETE);    
    authorizationService.saveAuthorization(savedAuthorization);
    
    // validate authorization updated
    savedAuthorization = authorizationService.createAuthorizationQuery().singleResult();
    assertEquals("anotherUserId", savedAuthorization.getUserId());
    assertEquals("anotherResourceType", savedAuthorization.getResourceType());
    assertEquals("anotherResourceId", savedAuthorization.getResourceId());
    assertTrue(savedAuthorization.hasPermission(ACCESS));
    assertTrue(savedAuthorization.hasPermission(DELETE));
        
  }
    
  public void testPermissions() {
    
    Authorization authorization = authorizationService.createNewAuthorization();

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
    TestResource resource1 = new TestResource("resource1");
    TestResource resource2 = new TestResource("resource2");
    
    // if no authorizations are in Db, nothing is authorized
    assertFalse(authorizationService.isUserAuthorized("jonny", null, ALL, resource1));
    assertFalse(authorizationService.isUserAuthorized("someone", null, CREATE, resource2));
    assertFalse(authorizationService.isUserAuthorized("someone else", null, DELETE, resource1));
    assertFalse(authorizationService.isUserAuthorized("jonny", null, ALL, resource1, "someId"));
    assertFalse(authorizationService.isUserAuthorized("someone", null, CREATE, resource2, "someId"));
    assertFalse(authorizationService.isUserAuthorized("someone else", null, DELETE, resource1, "someOtherId"));
    
  }
  
  public void testGlobalGrantAuthorizationCheck() {
    TestResource resource1 = new TestResource("resource1");

    // create global authorization which grants all permissions to all users  (on resource1):
    Authorization globalAuth = authorizationService.createNewAuthorization();
    globalAuth.setUserId(ANY);
    globalAuth.setResourceType(resource1.resourceType());
    globalAuth.setResourceId(ANY);
    globalAuth.addPermission(ALL);    
    authorizationService.saveAuthorization(globalAuth);
    
    // this authorizes any user to do anything in this resource:
    assertTrue(authorizationService.isUserAuthorized("jonny", null, ALL, resource1));    
    assertTrue(authorizationService.isUserAuthorized("someone", null, CREATE, resource1));
    assertTrue(authorizationService.isUserAuthorized("someone else", null, DELETE, resource1));
    assertTrue(authorizationService.isUserAuthorized("jonny", null, ALL, resource1, "someId"));
    assertTrue(authorizationService.isUserAuthorized("someone", null, CREATE, resource1, "someId"));
    assertTrue(authorizationService.isUserAuthorized("someone else", null, DELETE, resource1, "someOtherId"));
  }
  
  public void testUserOverrideGlobalGrantAuthorizationCheck() {
    TestResource resource1 = new TestResource("resource1");

    // create global authorization which grants all permissions to all users  (on resource1):
    Authorization globalGrant = authorizationService.createNewAuthorization();
    globalGrant.setUserId(ANY);
    globalGrant.setResourceType(resource1.resourceType());
    globalGrant.setResourceId(ANY);
    globalGrant.addPermission(ALL);    
    authorizationService.saveAuthorization(globalGrant);
    
    // revoke READ for jonny
    Authorization localRevoke = authorizationService.createNewAuthorization();
    localRevoke.setUserId("jonny");
    localRevoke.setResourceType(resource1.resourceType());
    localRevoke.setResourceId(ANY);
    localRevoke.addPermission(ALL);
    localRevoke.removePermission(READ);
    authorizationService.saveAuthorization(localRevoke);
    
    // jonny does not have ALL permissions
    assertFalse(authorizationService.isUserAuthorized("jonny", null, ALL, resource1));
    // jonny can't read
    assertFalse(authorizationService.isUserAuthorized("jonny", null, READ, resource1));
    // someone else can
    assertTrue(authorizationService.isUserAuthorized("someone else", null, ALL, resource1));
    assertTrue(authorizationService.isUserAuthorized("someone else", null, READ, resource1));
    // jonny can still delete
    assertTrue(authorizationService.isUserAuthorized("jonny", null, DELETE, resource1));            
  }
  
  public void testUserOverrideGlobalRevokeAuthorizationCheck() {
    TestResource resource1 = new TestResource("resource1");

    // create global authorization which grants all permissions to all users  (on resource1):
    Authorization globalGrant = authorizationService.createNewAuthorization();
    globalGrant.setUserId(ANY);
    globalGrant.setResourceType(resource1.resourceType());
    globalGrant.setResourceId(ANY);
    globalGrant.removePermission(ALL);    
    authorizationService.saveAuthorization(globalGrant);
    
    // add READ for jonny
    Authorization localRevoke = authorizationService.createNewAuthorization();
    localRevoke.setUserId("jonny");
    localRevoke.setResourceType(resource1.resourceType());
    localRevoke.setResourceId(ANY);
    localRevoke.addPermission(READ);
    authorizationService.saveAuthorization(localRevoke);
    
    // jonny does not have ALL permissions
    assertFalse(authorizationService.isUserAuthorized("jonny", null, ALL, resource1));
    // jonny can read
    assertTrue(authorizationService.isUserAuthorized("jonny", null, READ, resource1));
    // jonny can't delete
    assertFalse(authorizationService.isUserAuthorized("jonny", null, DELETE, resource1));
    
    // someone else can do nothing
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
