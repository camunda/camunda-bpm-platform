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

import java.util.List;

import org.camunda.bpm.engine.identity.Group;


/**
 * @author Daniel Meyer
 * 
 */
public class LdapGroupQueryTest extends LdapIdentityProviderTest {

  public void testQueryNoFilter() {
    List<Group> groupList = identityService.createGroupQuery().list();
    
    assertEquals(5, groupList.size());
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
    assertEquals(2, list.size());
    list = identityService.createGroupQuery().groupMember("oscar").list();
    assertEquals(1, list.size());
    list = identityService.createGroupQuery().groupMember("ruecker").list();
    assertEquals(3, list.size());
    list = identityService.createGroupQuery().groupMember("non-existing").list();
    assertEquals(0, list.size());
  }

  public void testFilterByGroupMemberSpecialCharacter() {
    List<Group> list = identityService.createGroupQuery().groupMember("david(IT)").list();
    assertEquals(1, list.size());
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

}
