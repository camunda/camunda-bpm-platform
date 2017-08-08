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
package org.camunda.bpm.identity.impl.ldap;

import java.util.HashSet;
import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Resources.GROUP;

import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.identity.Group;
import static org.camunda.bpm.identity.impl.ldap.LdapTestUtilities.checkPagingResults;
import static org.camunda.bpm.identity.impl.ldap.LdapTestUtilities.testGroupPaging;


/**
 * @author Daniel Meyer
 *
 */
public class LdapGroupQueryTest extends LdapIdentityProviderTest {

  public void testQueryNoFilter() {
    List<Group> groupList = identityService.createGroupQuery().list();

    assertEquals(6, groupList.size());
  }

  public void testFilterByGroupId() {
    Group group = identityService.createGroupQuery().groupId("management").singleResult();
    assertNotNull(group);

    // validate result
    assertEquals("management", group.getId());
    assertEquals("management", group.getName());

    group = identityService.createGroupQuery().groupId("whatever").singleResult();
    assertNull(group);
  }

  public void testFilterByGroupIdIn() {
    List<Group> groups = identityService.createGroupQuery()
      .groupIdIn("external", "management")
      .list();

    assertEquals(2, groups.size());
    for (Group group : groups) {
      if (!group.getId().equals("external") && !group.getId().equals("management")) {
        fail();
      }
    }
  }

  public void testFilterByGroupName() {
    Group group = identityService.createGroupQuery().groupName("management").singleResult();
    assertNotNull(group);

    // validate result
    assertEquals("management", group.getId());
    assertEquals("management", group.getName());

    group = identityService.createGroupQuery().groupName("whatever").singleResult();
    assertNull(group);
  }

  public void testFilterByGroupNameLike() {
    Group group = identityService.createGroupQuery().groupNameLike("manage*").singleResult();
    assertNotNull(group);

    // validate result
    assertEquals("management", group.getId());
    assertEquals("management", group.getName());

    group = identityService.createGroupQuery().groupNameLike("what*").singleResult();
    assertNull(group);
  }

  public void testFilterByGroupMember() {
    List<Group> list = identityService.createGroupQuery().groupMember("daniel").list();
    assertEquals(3, list.size());
    list = identityService.createGroupQuery().groupMember("oscar").list();
    assertEquals(2, list.size());
    list = identityService.createGroupQuery().groupMember("ruecker").list();
    assertEquals(4, list.size());
    list = identityService.createGroupQuery().groupMember("non-existing").list();
    assertEquals(0, list.size());
  }

  public void testFilterByGroupMemberSpecialCharacter() {
    List<Group> list = identityService.createGroupQuery().groupMember("david(IT)").list();
    assertEquals(2, list.size());
  }

  public void testFilterByGroupMemberPosix() {

    // by default the configuration does not use posix groups
    LdapConfiguration ldapConfiguration = new LdapConfiguration();
    ldapConfiguration.setGroupMemberAttribute("memberUid");
    ldapConfiguration.setGroupSearchFilter("(someFilter)");

    LdapIdentityProviderSession session = new LdapIdentityProviderSession(ldapConfiguration) {
      // mock getDnForUser
      protected String getDnForUser(String userId) {
        return userId+ ", fullDn";
      }
    };

    // if I query for groups by group member
    LdapGroupQuery query = new LdapGroupQuery();
    query.groupMember("jonny");

    // then the full DN is requested. This is the default behavior.
    String filter = session.getGroupSearchFilter(query);
    assertEquals("(&(someFilter)(memberUid=jonny, fullDn))", filter);

    // If I turn on posix groups
    ldapConfiguration.setUsePosixGroups(true);

    //  then the filter string does not contain the full DN for the
    // user but the simple (unqualified) userId as provided in the query
    filter = session.getGroupSearchFilter(query);
    assertEquals("(&(someFilter)(memberUid=jonny))", filter);

  }


  public void testPagination() {
    testGroupPaging(identityService);
  }

  public void testPaginationWithAuthenticatedUser() {
    createGrantAuthorization(GROUP, "management", "oscar", READ);
    createGrantAuthorization(GROUP, "consulting", "oscar", READ);
    createGrantAuthorization(GROUP, "external", "oscar", READ);

    try {
      processEngineConfiguration.setAuthorizationEnabled(true);

      identityService.setAuthenticatedUserId("oscar");

      Set<String> groupNames = new HashSet<String>();
      List<Group> groups = identityService.createGroupQuery().listPage(0, 2);
      assertEquals(2, groups.size());
      checkPagingResults(groupNames, groups.get(0).getId(), groups.get(1).getId());

      groups = identityService.createGroupQuery().listPage(2, 2);
      assertEquals(1, groups.size());
      assertFalse(groupNames.contains(groups.get(0).getId()));
      groupNames.add(groups.get(0).getId());

      groups = identityService.createGroupQuery().listPage(4, 2);
      assertEquals(0, groups.size());

      identityService.setAuthenticatedUserId("daniel");

      groups = identityService.createGroupQuery().listPage(0, 2);
      assertEquals(0, groups.size());

    } finally {
      processEngineConfiguration.setAuthorizationEnabled(false);
      identityService.clearAuthentication();

      for (Authorization authorization : authorizationService.createAuthorizationQuery().list()) {
        authorizationService.deleteAuthorization(authorization.getId());
      }

    }
  }

  protected void createGrantAuthorization(Resource resource, String resourceId, String userId, Permission... permissions) {
    Authorization authorization = createAuthorization(AUTH_TYPE_GRANT, resource, resourceId);
    authorization.setUserId(userId);
    for (Permission permission : permissions) {
      authorization.addPermission(permission);
    }
    authorizationService.saveAuthorization(authorization);
  }

  protected Authorization createAuthorization(int type, Resource resource, String resourceId) {
    Authorization authorization = authorizationService.createNewAuthorization(type);

    authorization.setResource(resource);
    if (resourceId != null) {
      authorization.setResourceId(resourceId);
    }

    return authorization;
  }

}
