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
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;

/**
 * @author Daniel Meyer
 *
 */
public class AuthorizationServiceTest extends PluggableProcessEngineTestCase {
  
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
    
    authorizationService.deleteAuthorization(authorization1.getId());
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
    
    authorizationService.deleteAuthorization(authorization1.getId());
  }
  
  public void testUpdateNewAuthorization() {
        
    Authorization authorization = authorizationService.createNewAuthorization();
    authorization.setUserId("aUserId");
    authorization.setResourceType("aResourceType");
    authorization.setResourceId("aResourceId");
    authorization.addPermission(Authorization.PERMISSION_TYPE_ACCESS);
    
    // save the authorization
    authorizationService.saveAuthorization(authorization);
    
    // validate authorization
    Authorization savedAuthorization = authorizationService.createAuthorizationQuery().singleResult();
    assertEquals("aUserId", savedAuthorization.getUserId());
    assertEquals("aResourceType", savedAuthorization.getResourceType());
    assertEquals("aResourceId", savedAuthorization.getResourceId());
    assertTrue(savedAuthorization.hasPermission(Authorization.PERMISSION_TYPE_ACCESS));
    
    // update authorization
    authorization.setUserId("anotherUserId");
    authorization.setResourceType("anotherResourceType");
    authorization.setResourceId("anotherResourceId");
    authorization.addPermission(Authorization.PERMISSION_TYPE_DELETE);    
    authorizationService.saveAuthorization(authorization);
    
    // validate authorization updated
    savedAuthorization = authorizationService.createAuthorizationQuery().singleResult();
    assertEquals("anotherUserId", savedAuthorization.getUserId());
    assertEquals("anotherResourceType", savedAuthorization.getResourceType());
    assertEquals("anotherResourceId", savedAuthorization.getResourceId());
    assertTrue(savedAuthorization.hasPermission(Authorization.PERMISSION_TYPE_ACCESS));
    assertTrue(savedAuthorization.hasPermission(Authorization.PERMISSION_TYPE_DELETE));
        
    // delete the authorization
    authorizationService.deleteAuthorization(authorization.getId());
  }
  
  public void testUpdatePersistentAuthorization() {
    
    Authorization authorization = authorizationService.createNewAuthorization();
    authorization.setUserId("aUserId");
    authorization.setResourceType("aResourceType");
    authorization.setResourceId("aResourceId");
    authorization.addPermission(Authorization.PERMISSION_TYPE_ACCESS);
    
    // save the authorization
    authorizationService.saveAuthorization(authorization);
    
    // validate authorization
    Authorization savedAuthorization = authorizationService.createAuthorizationQuery().singleResult();
    assertEquals("aUserId", savedAuthorization.getUserId());
    assertEquals("aResourceType", savedAuthorization.getResourceType());
    assertEquals("aResourceId", savedAuthorization.getResourceId());
    assertTrue(savedAuthorization.hasPermission(Authorization.PERMISSION_TYPE_ACCESS));
    
    // update authorization
    savedAuthorization.setUserId("anotherUserId");
    savedAuthorization.setResourceType("anotherResourceType");
    savedAuthorization.setResourceId("anotherResourceId");
    savedAuthorization.addPermission(Authorization.PERMISSION_TYPE_DELETE);    
    authorizationService.saveAuthorization(savedAuthorization);
    
    // validate authorization updated
    savedAuthorization = authorizationService.createAuthorizationQuery().singleResult();
    assertEquals("anotherUserId", savedAuthorization.getUserId());
    assertEquals("anotherResourceType", savedAuthorization.getResourceType());
    assertEquals("anotherResourceId", savedAuthorization.getResourceId());
    assertTrue(savedAuthorization.hasPermission(Authorization.PERMISSION_TYPE_ACCESS));
    assertTrue(savedAuthorization.hasPermission(Authorization.PERMISSION_TYPE_DELETE));
        
    // delete the authorization
    authorizationService.deleteAuthorization(authorization.getId());
    
  }
    
  public void testPermissions() {
    
    Authorization authorization = authorizationService.createNewAuthorization();

    assertEquals(0, authorization.getPermissions());    
    
    assertFalse(authorization.hasPermission(Authorization.PERMISSION_TYPE_ACCESS));
    assertFalse(authorization.hasPermission(Authorization.PERMISSION_TYPE_DELETE));
    assertFalse(authorization.hasPermission(Authorization.PERMISSION_TYPE_READ));
    assertFalse(authorization.hasPermission(Authorization.PERMISSION_TYPE_WRITE));
    
    authorization.addPermission(Authorization.PERMISSION_TYPE_ACCESS);
    assertTrue(authorization.hasPermission(Authorization.PERMISSION_TYPE_ACCESS));
    assertFalse(authorization.hasPermission(Authorization.PERMISSION_TYPE_DELETE));
    assertFalse(authorization.hasPermission(Authorization.PERMISSION_TYPE_READ));
    assertFalse(authorization.hasPermission(Authorization.PERMISSION_TYPE_WRITE));
    
    authorization.addPermission(Authorization.PERMISSION_TYPE_DELETE);
    assertTrue(authorization.hasPermission(Authorization.PERMISSION_TYPE_ACCESS));
    assertTrue(authorization.hasPermission(Authorization.PERMISSION_TYPE_DELETE));
    assertFalse(authorization.hasPermission(Authorization.PERMISSION_TYPE_READ));
    assertFalse(authorization.hasPermission(Authorization.PERMISSION_TYPE_WRITE));
   
    authorization.addPermission(Authorization.PERMISSION_TYPE_READ);
    assertTrue(authorization.hasPermission(Authorization.PERMISSION_TYPE_ACCESS));
    assertTrue(authorization.hasPermission(Authorization.PERMISSION_TYPE_DELETE));
    assertTrue(authorization.hasPermission(Authorization.PERMISSION_TYPE_READ));
    assertFalse(authorization.hasPermission(Authorization.PERMISSION_TYPE_WRITE));
    
    authorization.addPermission(Authorization.PERMISSION_TYPE_WRITE);
    assertTrue(authorization.hasPermission(Authorization.PERMISSION_TYPE_ACCESS));
    assertTrue(authorization.hasPermission(Authorization.PERMISSION_TYPE_DELETE));
    assertTrue(authorization.hasPermission(Authorization.PERMISSION_TYPE_READ));
    assertTrue(authorization.hasPermission(Authorization.PERMISSION_TYPE_WRITE));
    
    authorization.removePermission(Authorization.PERMISSION_TYPE_ACCESS);
    assertFalse(authorization.hasPermission(Authorization.PERMISSION_TYPE_ACCESS));
    assertTrue(authorization.hasPermission(Authorization.PERMISSION_TYPE_DELETE));
    assertTrue(authorization.hasPermission(Authorization.PERMISSION_TYPE_READ));
    assertTrue(authorization.hasPermission(Authorization.PERMISSION_TYPE_WRITE));
    
    authorization.removePermission(Authorization.PERMISSION_TYPE_DELETE);
    assertFalse(authorization.hasPermission(Authorization.PERMISSION_TYPE_ACCESS));
    assertFalse(authorization.hasPermission(Authorization.PERMISSION_TYPE_DELETE));
    assertTrue(authorization.hasPermission(Authorization.PERMISSION_TYPE_READ));
    assertTrue(authorization.hasPermission(Authorization.PERMISSION_TYPE_WRITE));
    
    authorization.removePermission(Authorization.PERMISSION_TYPE_READ);
    assertFalse(authorization.hasPermission(Authorization.PERMISSION_TYPE_ACCESS));
    assertFalse(authorization.hasPermission(Authorization.PERMISSION_TYPE_DELETE));
    assertFalse(authorization.hasPermission(Authorization.PERMISSION_TYPE_READ));
    assertTrue(authorization.hasPermission(Authorization.PERMISSION_TYPE_WRITE));
    
    authorization.removePermission(Authorization.PERMISSION_TYPE_WRITE);
    assertFalse(authorization.hasPermission(Authorization.PERMISSION_TYPE_ACCESS));
    assertFalse(authorization.hasPermission(Authorization.PERMISSION_TYPE_DELETE));
    assertFalse(authorization.hasPermission(Authorization.PERMISSION_TYPE_READ));
    assertFalse(authorization.hasPermission(Authorization.PERMISSION_TYPE_WRITE));
    
  }


}
