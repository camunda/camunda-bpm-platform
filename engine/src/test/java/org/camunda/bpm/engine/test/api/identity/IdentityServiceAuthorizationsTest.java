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

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.identity.Authorization;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.Permissions;
import org.camunda.bpm.engine.identity.Resources;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.persistence.entity.GroupEntity;
import org.camunda.bpm.engine.impl.persistence.entity.UserEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;

/**
 * @author Daniel Meyer
 *
 */
public class IdentityServiceAuthorizationsTest extends PluggableProcessEngineTestCase {

  private final static String jonny2 = "jonny2";

  @Override
  protected void tearDown() throws Exception {
    processEngineConfiguration.setAuthorizationChecksEnabled(false);
    cleanupAfterTest();
    super.tearDown();
  }
  
  public void testUserCreateAuthorizations() {

    processEngineConfiguration.setAuthorizationChecksEnabled(true);
    identityService.setAuthenticatedUserId(jonny2);
    
    // add base permission which allows nobody to create users:
    Authorization basePerms = authorizationService.createNewAuthorization();
    basePerms.setUserId(Authorization.ANY);
    basePerms.setResourceType(Resources.USER.getId());
    basePerms.setResourceId(Authorization.ANY);
    basePerms.addPermission(Permissions.ALL); // add all then remove 'crate'
    basePerms.removePermission(Permissions.CREATE);
    authorizationService.saveAuthorization(basePerms);
    
    try {
      identityService.newUser("jonny1");
      fail("exception expected");
      
    } catch (AuthorizationException e) {
      assertEquals(Permissions.CREATE.getName(), e.getViolatedPermissionName());
      assertEquals(jonny2, e.getUserId());
      assertEquals(Resources.USER.getName(), e.getResourceType());
      assertEquals(null, e.getResourceId());
    }
      
    // circumvent auth check to get new transient userobject
    User newUser = new UserEntity("jonny1");    
    
    try {
      identityService.saveUser(newUser);
      fail("exception expected");
      
    } catch (AuthorizationException e) {
      assertEquals(Permissions.CREATE.getName(), e.getViolatedPermissionName());
      assertEquals(jonny2, e.getUserId());
      assertEquals(Resources.USER.getName(), e.getResourceType());
      assertEquals(null, e.getResourceId());
    }    
  }
    
  public void testUserDeleteAuthorizations() {
    
    // crate user while still in god-mode:
    User jonny1 = identityService.newUser("jonny1");
    identityService.saveUser(jonny1);    
    
    // turn on authorization
    processEngineConfiguration.setAuthorizationChecksEnabled(true);    
    identityService.setAuthenticatedUserId(jonny2);
    
    // create global auth
    Authorization basePerms = authorizationService.createNewAuthorization();
    basePerms.setUserId(Authorization.ANY);
    basePerms.setResourceType(Resources.USER.getId());
    basePerms.setResourceId(Authorization.ANY);
    basePerms.addPermission(Permissions.ALL);
    basePerms.removePermission(Permissions.DELETE); // revoke delete
    authorizationService.saveAuthorization(basePerms);
        
    try {
      identityService.deleteUser("jonny1");
      fail("exception expected");
      
    } catch (AuthorizationException e) {
      assertEquals(Permissions.DELETE.getName(), e.getViolatedPermissionName());
      assertEquals(jonny2, e.getUserId());
      assertEquals(Resources.USER.getName(), e.getResourceType());
      assertEquals("jonny1", e.getResourceId());
    }
  }

  public void testUserUpdateAuthorizations() {
    
    // crate user while still in god-mode:
    User jonny1 = identityService.newUser("jonny1");
    identityService.saveUser(jonny1);    
    
    // turn on authorization
    processEngineConfiguration.setAuthorizationChecksEnabled(true);    
    identityService.setAuthenticatedUserId(jonny2);
    
    // create global auth
    Authorization basePerms = authorizationService.createNewAuthorization();
    basePerms.setUserId(Authorization.ANY);
    basePerms.setResourceType(Resources.USER.getId());
    basePerms.setResourceId(Authorization.ANY);
    basePerms.addPermission(Permissions.ALL);
    basePerms.removePermission(Permissions.UPDATE); // revoke update
    authorizationService.saveAuthorization(basePerms);
        
    // fetch user:
    jonny1 = identityService.createUserQuery().singleResult();
    jonny1.setFirstName("Jonny");
    
    try {
      identityService.saveUser(jonny1);
      fail("exception expected");
      
    } catch (AuthorizationException e) {
      assertEquals(Permissions.UPDATE.getName(), e.getViolatedPermissionName());
      assertEquals(jonny2, e.getUserId());
      assertEquals(Resources.USER.getName(), e.getResourceType());
      assertEquals("jonny1", e.getResourceId());
    }
    
    // but I can create a new user:
    User jonny3 = identityService.newUser("jonny3");
    identityService.saveUser(jonny3);
              
  }
  
  public void testGroupCreateAuthorizations() {

    processEngineConfiguration.setAuthorizationChecksEnabled(true);
    identityService.setAuthenticatedUserId(jonny2);
    
    // add base permission which allows nobody to create groups:
    Authorization basePerms = authorizationService.createNewAuthorization();
    basePerms.setUserId(Authorization.ANY);
    basePerms.setResourceType(Resources.GROUP.getId());
    basePerms.setResourceId(Authorization.ANY);
    basePerms.addPermission(Permissions.ALL); // add all then remove 'crate'
    basePerms.removePermission(Permissions.CREATE);
    authorizationService.saveAuthorization(basePerms);
    
    try {
      identityService.newGroup("group1");
      fail("exception expected");
      
    } catch (AuthorizationException e) {
      assertEquals(Permissions.CREATE.getName(), e.getViolatedPermissionName());
      assertEquals(jonny2, e.getUserId());
      assertEquals(Resources.GROUP.getName(), e.getResourceType());
      assertEquals(null, e.getResourceId());
    }
      
    // circumvent auth check to get new transient userobject
    Group group = new GroupEntity("group1");    
    
    try {
      identityService.saveGroup(group);
      fail("exception expected");
      
    } catch (AuthorizationException e) {
      assertEquals(Permissions.CREATE.getName(), e.getViolatedPermissionName());
      assertEquals(jonny2, e.getUserId());
      assertEquals(Resources.GROUP.getName(), e.getResourceType());
      assertEquals(null, e.getResourceId());
    }    
  }
    
  public void testGroupDeleteAuthorizations() {
    
    // crate group while still in god-mode:
    Group group1 = identityService.newGroup("group1");
    identityService.saveGroup(group1);    
    
    // turn on authorization
    processEngineConfiguration.setAuthorizationChecksEnabled(true);    
    identityService.setAuthenticatedUserId(jonny2);
    
    // create global auth
    Authorization basePerms = authorizationService.createNewAuthorization();
    basePerms.setUserId(Authorization.ANY);
    basePerms.setResourceType(Resources.GROUP.getId());
    basePerms.setResourceId(Authorization.ANY);
    basePerms.addPermission(Permissions.ALL);
    basePerms.removePermission(Permissions.DELETE); // revoke delete
    authorizationService.saveAuthorization(basePerms);
        
    try {
      identityService.deleteGroup("group1");
      fail("exception expected");
      
    } catch (AuthorizationException e) {
      assertEquals(Permissions.DELETE.getName(), e.getViolatedPermissionName());
      assertEquals(jonny2, e.getUserId());
      assertEquals(Resources.GROUP.getName(), e.getResourceType());
      assertEquals("group1", e.getResourceId());
    }
              
  }
  

  public void testGroupUpdateAuthorizations() {
    
    // crate group while still in god-mode:
    Group group1 = identityService.newGroup("group1");
    identityService.saveGroup(group1);     
    
    // turn on authorization
    processEngineConfiguration.setAuthorizationChecksEnabled(true);    
    identityService.setAuthenticatedUserId(jonny2);
    
    // create global auth
    Authorization basePerms = authorizationService.createNewAuthorization();
    basePerms.setUserId(Authorization.ANY);
    basePerms.setResourceType(Resources.GROUP.getId());
    basePerms.setResourceId(Authorization.ANY);
    basePerms.addPermission(Permissions.ALL);
    basePerms.removePermission(Permissions.UPDATE); // revoke update
    authorizationService.saveAuthorization(basePerms);
        
    // fetch user:
    group1 = identityService.createGroupQuery().singleResult();
    group1.setName("Group 1");
    
    try {
      identityService.saveGroup(group1);
      fail("exception expected");
      
    } catch (AuthorizationException e) {
      assertEquals(Permissions.UPDATE.getName(), e.getViolatedPermissionName());
      assertEquals(jonny2, e.getUserId());
      assertEquals(Resources.GROUP.getName(), e.getResourceType());
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

    processEngineConfiguration.setAuthorizationChecksEnabled(true);
    identityService.setAuthenticatedUserId(jonny2);
        
    // add base permission which allows nobody to add users to groups
    Authorization basePerms = authorizationService.createNewAuthorization();
    basePerms.setUserId(Authorization.ANY);
    basePerms.setResourceType(Resources.GROUP_MEMBERSHIP.getId());
    basePerms.setResourceId(Authorization.ANY);
    basePerms.addPermission(Permissions.ALL); // add all then remove 'crate'
    basePerms.removePermission(Permissions.CREATE);
    authorizationService.saveAuthorization(basePerms);
    
    try {
      identityService.createMembership("jonny1", "group1");
      fail("exception expected");
      
    } catch (AuthorizationException e) {
      assertEquals(Permissions.CREATE.getName(), e.getViolatedPermissionName());
      assertEquals(jonny2, e.getUserId());
      assertEquals(Resources.GROUP_MEMBERSHIP.getName(), e.getResourceType());
      assertEquals("group1", e.getResourceId());
    }      
  }
  
  public void testMembershipDeleteAuthorizations() {

    User jonny1 = identityService.newUser("jonny1");
    identityService.saveUser(jonny1);    
    
    Group group1 = identityService.newGroup("group1");
    identityService.saveGroup(group1);   

    processEngineConfiguration.setAuthorizationChecksEnabled(true);
    identityService.setAuthenticatedUserId(jonny2);
        
    // add base permission which allows nobody to add users to groups
    Authorization basePerms = authorizationService.createNewAuthorization();
    basePerms.setUserId(Authorization.ANY);
    basePerms.setResourceType(Resources.GROUP_MEMBERSHIP.getId());
    basePerms.setResourceId(Authorization.ANY);
    basePerms.addPermission(Permissions.ALL); // add all then remove 'delete'
    basePerms.removePermission(Permissions.DELETE);
    authorizationService.saveAuthorization(basePerms);
    
    try {
      identityService.deleteMembership("jonny1", "group1");
      fail("exception expected");
      
    } catch (AuthorizationException e) {
      assertEquals(Permissions.DELETE.getName(), e.getViolatedPermissionName());
      assertEquals(jonny2, e.getUserId());
      assertEquals(Resources.GROUP_MEMBERSHIP.getName(), e.getResourceType());
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
    
    // now enable checks
    processEngineConfiguration.setAuthorizationChecksEnabled(true);

    // set base permission for all users (no-one has any permissions on users)
    Authorization basePerms = authorizationService.createNewAuthorization();
    basePerms.setUserId(Authorization.ANY);
    basePerms.setResourceType(Resources.USER.getId());
    basePerms.setResourceId(Authorization.ANY);
    authorizationService.saveAuthorization(basePerms);
    
    // we cannot fetch the user
    assertNull(identityService.createUserQuery().singleResult());
    assertEquals(0, identityService.createUserQuery().count());
    
    // now we add permission for jonny2 to read the user:
    Authorization ourPerms = authorizationService.createNewAuthorization();
    ourPerms.setUserId(authUserId);
    ourPerms.setResourceType("user");
    ourPerms.setResourceId(Authorization.ANY);
    ourPerms.addPermission(Permissions.READ);
    authorizationService.saveAuthorization(ourPerms);
    
    // now we can fetch the user
    assertNotNull(identityService.createUserQuery().singleResult());
    assertEquals(1, identityService.createUserQuery().count());
    
    // change the base permission:
    basePerms = authorizationService.createAuthorizationQuery().resourceType("user").userIdIn("*").singleResult();
    basePerms.addPermission(Permissions.READ);
    authorizationService.saveAuthorization(basePerms);
    
    // we can still fetch the user
    assertNotNull(identityService.createUserQuery().singleResult());
    assertEquals(1, identityService.createUserQuery().count());
    
    // revoke permission for jonny2:
    ourPerms = authorizationService.createAuthorizationQuery().resourceType("user").userIdIn(authUserId).singleResult();
    ourPerms.removePermission(Permissions.READ);
    authorizationService.saveAuthorization(ourPerms);
    
    // now we cannot fetch the user
    assertNull(identityService.createUserQuery().singleResult());
    assertEquals(0, identityService.createUserQuery().count());
    
    // delete our perms
    authorizationService.deleteAuthorization(ourPerms.getId());
    
    // now the base permission applies and grants us read access
    assertNotNull(identityService.createUserQuery().singleResult());
    assertEquals(1, identityService.createUserQuery().count());
    
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
    
    // now enable checks
    processEngineConfiguration.setAuthorizationChecksEnabled(true);

    // set base permission for all users (no-one has any permissions on groups)
    Authorization basePerms = authorizationService.createNewAuthorization();
    basePerms.setUserId(Authorization.ANY);
    basePerms.setResourceType(Resources.GROUP.getId());
    basePerms.setResourceId(Authorization.ANY);
    authorizationService.saveAuthorization(basePerms);
    
    // we cannot fetch the group
    assertNull(identityService.createGroupQuery().singleResult());
    assertEquals(0, identityService.createGroupQuery().count());
    
    // now we add permission for jonny2 to read the group:
    Authorization ourPerms = authorizationService.createNewAuthorization();
    ourPerms.setUserId(authUserId);
    ourPerms.setResourceType(Resources.GROUP.getId());
    ourPerms.setResourceId(Authorization.ANY);
    ourPerms.addPermission(Permissions.READ);
    authorizationService.saveAuthorization(ourPerms);
    
    // now we can fetch the group
    assertNotNull(identityService.createGroupQuery().singleResult());
    assertEquals(1, identityService.createGroupQuery().count());
    
    // change the base permission:
    basePerms = authorizationService.createAuthorizationQuery().resourceType(Resources.GROUP.getId()).userIdIn("*").singleResult();
    basePerms.addPermission(Permissions.READ);
    authorizationService.saveAuthorization(basePerms);
    
    // we can still fetch the group
    assertNotNull(identityService.createGroupQuery().singleResult());
    assertEquals(1, identityService.createGroupQuery().count());
    
    // revoke permission for jonny2:
    ourPerms = authorizationService.createAuthorizationQuery().resourceType(Resources.GROUP.getId()).userIdIn(authUserId).singleResult();
    ourPerms.removePermission(Permissions.READ);
    authorizationService.saveAuthorization(ourPerms);
    
    // now we cannot fetch the group
    assertNull(identityService.createGroupQuery().singleResult());
    assertEquals(0, identityService.createGroupQuery().count());
    
    // delete our perms
    authorizationService.deleteAuthorization(ourPerms.getId());
    
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
