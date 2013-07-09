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

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.identity.Authorization;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;

/**
 * @author Daniel Meyer
 *
 */
public class AuthorizationQueryTest extends PluggableProcessEngineTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    createAuthorization("user1", null, "resource1", "resource1-1", Authorization.PERMISSION_TYPE_ACCESS);
    createAuthorization("user1", null, "resource2", "resource2-1", Authorization.PERMISSION_TYPE_DELETE);
    createAuthorization("user2", null, "resource1", "resource1-2");
    createAuthorization("user3", null, "resource2", "resource2-1", Authorization.PERMISSION_TYPE_READ, Authorization.PERMISSION_TYPE_WRITE);
    
    createAuthorization(null, "group1", "resource1", "resource1-1");
    createAuthorization(null, "group1", "resource1", "resource1-2", Authorization.PERMISSION_TYPE_WRITE);
    createAuthorization(null, "group2", "resource2", "resource2-2", Authorization.PERMISSION_TYPE_READ, Authorization.PERMISSION_TYPE_WRITE);
    createAuthorization(null, "group3", "resource2", "resource2-3", Authorization.PERMISSION_TYPE_DELETE);
  }
  
  protected void tearDown() throws Exception {
    List<Authorization> list = authorizationService.createAuthorizationQuery().list();
    for (Authorization authorization : list) {
      authorizationService.deleteAuthorization(authorization.getId());
    }
    super.tearDown();
  }

  protected void createAuthorization(String userId, String groupId, String resourceType, String resourceId, int... permissions) {
    
    Authorization authorization = authorizationService.createNewAuthorization();
    authorization.setUserId(userId);
    authorization.setGroupId(groupId);
    authorization.setResourceType(resourceType);
    authorization.setResourceId(resourceId);
    
    for (int permission : permissions) {
      authorization.addPermission(permission);
    }
    
    authorizationService.saveAuthorization(authorization);   
  }
  
  public void testValidQueryCounts() {
    
    // query by user id
    assertEquals(2, authorizationService.createAuthorizationQuery().userIdIn("user1").count());
    assertEquals(1, authorizationService.createAuthorizationQuery().userIdIn("user2").count());
    assertEquals(1, authorizationService.createAuthorizationQuery().userIdIn("user3").count());
    assertEquals(3, authorizationService.createAuthorizationQuery().userIdIn("user1", "user2").count());
    assertEquals(0, authorizationService.createAuthorizationQuery().userIdIn("non-existing").count());
    
    // query by group id
    assertEquals(2, authorizationService.createAuthorizationQuery().groupIdIn("group1").count());
    assertEquals(1, authorizationService.createAuthorizationQuery().groupIdIn("group2").count());
    assertEquals(1, authorizationService.createAuthorizationQuery().groupIdIn("group3").count());
    assertEquals(3, authorizationService.createAuthorizationQuery().groupIdIn("group1", "group2").count());
    assertEquals(0, authorizationService.createAuthorizationQuery().groupIdIn("non-existing").count());
    
    // query by resource type
    assertEquals(4, authorizationService.createAuthorizationQuery().resourceType("resource1").count());
    assertEquals(0, authorizationService.createAuthorizationQuery().resourceType("non-existing").count());
    
    // query by resource id
    assertEquals(2, authorizationService.createAuthorizationQuery().resourceId("resource1-2").count());
    assertEquals(0, authorizationService.createAuthorizationQuery().resourceId("non-existing").count());
    
    // query by permission
    assertEquals(1, authorizationService.createAuthorizationQuery().hasPermission(Authorization.PERMISSION_TYPE_ACCESS).count());
    assertEquals(2, authorizationService.createAuthorizationQuery().hasPermission(Authorization.PERMISSION_TYPE_DELETE).count());
    assertEquals(2, authorizationService.createAuthorizationQuery().hasPermission(Authorization.PERMISSION_TYPE_READ).count());
    assertEquals(3, authorizationService.createAuthorizationQuery().hasPermission(Authorization.PERMISSION_TYPE_WRITE).count());
    // multiple permissions at the same time
    assertEquals(2, authorizationService.createAuthorizationQuery().hasPermission(Authorization.PERMISSION_TYPE_READ).hasPermission(Authorization.PERMISSION_TYPE_WRITE).count());
    assertEquals(2, authorizationService.createAuthorizationQuery().hasPermission(Authorization.PERMISSION_TYPE_WRITE).hasPermission(Authorization.PERMISSION_TYPE_READ).count());
    assertEquals(0, authorizationService.createAuthorizationQuery().hasPermission(Authorization.PERMISSION_TYPE_READ).hasPermission(Authorization.PERMISSION_TYPE_ACCESS).count());
    
    // user id & resource type
    assertEquals(1, authorizationService.createAuthorizationQuery().userIdIn("user1").resourceType("resource1").count());
    assertEquals(0, authorizationService.createAuthorizationQuery().userIdIn("user1").resourceType("non-existing").count());
    
    // group id & resource type
    assertEquals(1, authorizationService.createAuthorizationQuery().groupIdIn("group2").resourceType("resource2").count());
    assertEquals(0, authorizationService.createAuthorizationQuery().groupIdIn("group1").resourceType("non-existing").count());
  }
  
  public void testValidQueryLists() {
    
    // query by user id
    assertEquals(2, authorizationService.createAuthorizationQuery().userIdIn("user1").list().size());
    assertEquals(1, authorizationService.createAuthorizationQuery().userIdIn("user2").list().size());
    assertEquals(1, authorizationService.createAuthorizationQuery().userIdIn("user3").list().size());
    assertEquals(3, authorizationService.createAuthorizationQuery().userIdIn("user1", "user2").list().size());
    assertEquals(0, authorizationService.createAuthorizationQuery().userIdIn("non-existing").list().size());
    
    // query by group id
    assertEquals(2, authorizationService.createAuthorizationQuery().groupIdIn("group1").list().size());
    assertEquals(1, authorizationService.createAuthorizationQuery().groupIdIn("group2").list().size());
    assertEquals(1, authorizationService.createAuthorizationQuery().groupIdIn("group3").list().size());
    assertEquals(3, authorizationService.createAuthorizationQuery().groupIdIn("group1", "group2").list().size());
    assertEquals(0, authorizationService.createAuthorizationQuery().groupIdIn("non-existing").list().size());
    
    // query by resource type
    assertEquals(4, authorizationService.createAuthorizationQuery().resourceType("resource1").list().size());
    assertEquals(0, authorizationService.createAuthorizationQuery().resourceType("non-existing").list().size());
    
    // query by resource id
    assertEquals(2, authorizationService.createAuthorizationQuery().resourceId("resource1-2").list().size());
    assertEquals(0, authorizationService.createAuthorizationQuery().resourceId("non-existing").list().size());
    
    // query by permission
    assertEquals(1, authorizationService.createAuthorizationQuery().hasPermission(Authorization.PERMISSION_TYPE_ACCESS).list().size());
    assertEquals(2, authorizationService.createAuthorizationQuery().hasPermission(Authorization.PERMISSION_TYPE_DELETE).list().size());
    assertEquals(2, authorizationService.createAuthorizationQuery().hasPermission(Authorization.PERMISSION_TYPE_READ).list().size());
    assertEquals(3, authorizationService.createAuthorizationQuery().hasPermission(Authorization.PERMISSION_TYPE_WRITE).list().size());
    // multiple permissions at the same time
    assertEquals(2, authorizationService.createAuthorizationQuery().hasPermission(Authorization.PERMISSION_TYPE_READ).hasPermission(Authorization.PERMISSION_TYPE_WRITE).list().size());
    assertEquals(2, authorizationService.createAuthorizationQuery().hasPermission(Authorization.PERMISSION_TYPE_WRITE).hasPermission(Authorization.PERMISSION_TYPE_READ).list().size());
    assertEquals(0, authorizationService.createAuthorizationQuery().hasPermission(Authorization.PERMISSION_TYPE_READ).hasPermission(Authorization.PERMISSION_TYPE_ACCESS).list().size());
    
    // user id & resource type
    assertEquals(1, authorizationService.createAuthorizationQuery().userIdIn("user1").resourceType("resource1").list().size());
    assertEquals(0, authorizationService.createAuthorizationQuery().userIdIn("user1").resourceType("non-existing").list().size());
    
    // group id & resource type
    assertEquals(1, authorizationService.createAuthorizationQuery().groupIdIn("group2").resourceType("resource2").list().size());
    assertEquals(0, authorizationService.createAuthorizationQuery().groupIdIn("group1").resourceType("non-existing").list().size());
  }
  
  public void testOrderByQueries() {
    
    List<Authorization> list = authorizationService.createAuthorizationQuery().orderByResourceType().asc().list();
    assertEquals("resource1", list.get(0).getResourceType());
    assertEquals("resource1", list.get(1).getResourceType());
    assertEquals("resource1", list.get(2).getResourceType());
    assertEquals("resource1", list.get(3).getResourceType());
    assertEquals("resource2", list.get(4).getResourceType());
    assertEquals("resource2", list.get(5).getResourceType());
    assertEquals("resource2", list.get(6).getResourceType());
    assertEquals("resource2", list.get(7).getResourceType());
    
    list = authorizationService.createAuthorizationQuery().orderByResourceType().desc().list();
    assertEquals("resource2", list.get(0).getResourceType());
    assertEquals("resource2", list.get(1).getResourceType());
    assertEquals("resource2", list.get(2).getResourceType());
    assertEquals("resource2", list.get(3).getResourceType());
    assertEquals("resource1", list.get(4).getResourceType());
    assertEquals("resource1", list.get(5).getResourceType());
    assertEquals("resource1", list.get(6).getResourceType());
    assertEquals("resource1", list.get(7).getResourceType());
    
    list = authorizationService.createAuthorizationQuery().orderByResourceId().asc().list();
    assertEquals("resource1-1", list.get(0).getResourceId());
    assertEquals("resource1-1", list.get(1).getResourceId());
    assertEquals("resource1-2", list.get(2).getResourceId());
    assertEquals("resource1-2", list.get(3).getResourceId());
    assertEquals("resource2-1", list.get(4).getResourceId());
    assertEquals("resource2-1", list.get(5).getResourceId());
    assertEquals("resource2-2", list.get(6).getResourceId());
    assertEquals("resource2-3", list.get(7).getResourceId());
    
    list = authorizationService.createAuthorizationQuery().orderByResourceId().desc().list();
    assertEquals("resource2-3", list.get(0).getResourceId());
    assertEquals("resource2-2", list.get(1).getResourceId());
    assertEquals("resource2-1", list.get(2).getResourceId());
    assertEquals("resource2-1", list.get(3).getResourceId());
    assertEquals("resource1-2", list.get(4).getResourceId());
    assertEquals("resource1-2", list.get(5).getResourceId());
    assertEquals("resource1-1", list.get(6).getResourceId());
    assertEquals("resource1-1", list.get(7).getResourceId());
    
  }
  
  public void testInvalidOrderByQueries() {
    try {
      authorizationService.createAuthorizationQuery().orderByResourceType().list();
      fail("Exception expected");
    } catch(ProcessEngineException e) {
      assertTextPresent("Invalid query: call asc() or desc() after using orderByXX()", e.getMessage());
    }
    
    try {
      authorizationService.createAuthorizationQuery().orderByResourceId().list();
      fail("Exception expected");
    } catch(ProcessEngineException e) {
      assertTextPresent("Invalid query: call asc() or desc() after using orderByXX()", e.getMessage());
    }
    
    try {
      authorizationService.createAuthorizationQuery().orderByResourceId().orderByResourceType().list();
      fail("Exception expected");
    } catch(ProcessEngineException e) {
      assertTextPresent("Invalid query: call asc() or desc() after using orderByXX()", e.getMessage());
    }
    
    try {
      authorizationService.createAuthorizationQuery().orderByResourceType().orderByResourceId().list();
      fail("Exception expected");
    } catch(ProcessEngineException e) {
      assertTextPresent("Invalid query: call asc() or desc() after using orderByXX()", e.getMessage());
    }
  }
  
  public void testInvalidQueries() {
    
    // cannot query for user id and group id at the same time
    
    try { 
      authorizationService.createAuthorizationQuery().groupIdIn("a").userIdIn("b").count();
    } catch(ProcessEngineException e) {
      assertTextPresent("Cannot query for user and group authorizations at the same time.", e.getMessage());
    }
    
    try { 
      authorizationService.createAuthorizationQuery().userIdIn("b").groupIdIn("a").count();
    } catch(ProcessEngineException e) {
      assertTextPresent("Cannot query for user and group authorizations at the same time.", e.getMessage());
    }
    
  }
  
}
