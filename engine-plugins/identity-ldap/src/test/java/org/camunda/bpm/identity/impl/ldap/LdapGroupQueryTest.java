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
package org.camunda.bpm.identity.impl.ldap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Resources.GROUP;
import static org.camunda.bpm.identity.ldap.util.LdapTestUtilities.checkPagingResults;
import static org.camunda.bpm.identity.ldap.util.LdapTestUtilities.testGroupPaging;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.identity.ldap.util.LdapTestEnvironment;
import org.camunda.bpm.identity.ldap.util.LdapTestEnvironmentRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;


public class LdapGroupQueryTest {

  @ClassRule
  public static LdapTestEnvironmentRule ldapRule = new LdapTestEnvironmentRule();
  @Rule
  public ProcessEngineRule engineRule = new ProcessEngineRule();

  ProcessEngineConfiguration processEngineConfiguration;
  IdentityService identityService;
  AuthorizationService authorizationService;
  LdapTestEnvironment ldapTestEnvironment;

  @Before
  public void setup() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    identityService = engineRule.getIdentityService();
    authorizationService = engineRule.getAuthorizationService();
    ldapTestEnvironment = ldapRule.getLdapTestEnvironment();
  }

  @Test
  public void testCountGroups() {
    // given

    // when
    GroupQuery groupQuery = identityService.createGroupQuery();

    // then
    assertThat(groupQuery.listPage(0, Integer.MAX_VALUE).size()).isEqualTo(6);
    assertThat(groupQuery.count()).isEqualTo(6);
  }

  @Test
  public void testQueryNoFilter() {
    // given

    // when
    List<Group> groupList = identityService.createGroupQuery().list();

    // then
    assertThat(groupList.size()).isEqualTo(6);
  }

  @Test
  public void testFilterByGroupId() {
    // given

    // when
    Group group = identityService.createGroupQuery().groupId("management").singleResult();

    // then
    assertThat(group).isNotNull();
    // validate result
    assertThat(group.getId()).isEqualTo("management");
    assertThat(group.getName()).isEqualTo("management");
  }

  @Test
  public void testFilterByNonexistingGroupId() {
    // given

    // when
    Group group = identityService.createGroupQuery().groupId("whatever").singleResult();

    // then
    assertThat(group).isNull();
  }

  @Test
  public void testFilterByGroupIdIn() {
    // given

    // when
    List<Group> groups = identityService.createGroupQuery().groupIdIn("external", "management").list();

    // then
    assertThat(groups.size()).isEqualTo(2);
    assertThat(groups).extracting("id").containsOnly("external", "management");
  }

  @Test
  public void testFilterByGroupName() {
    // given

    // when
    Group group = identityService.createGroupQuery().groupName("management").singleResult();

    // then
    assertThat(group).isNotNull();
    // validate result
    assertThat(group.getId()).isEqualTo("management");
    assertThat(group.getName()).isEqualTo("management");
  }

  @Test
  public void testFilterByNonexistingGroupName() {
    // given

    // when
    Group group = identityService.createGroupQuery().groupName("whatever").singleResult();

    // then
    assertThat(group).isNull();
  }

  @Test
  public void testFilterByGroupNameLikeTrailingWildcard() {
    // given

    // when
    Group group = identityService.createGroupQuery().groupNameLike("manage*").singleResult();

    // then
    assertThat(group).isNotNull();
    assertThat(group.getId()).isEqualTo("management");
    assertThat(group.getName()).isEqualTo("management");
  }

  @Test
  public void testFilterByGroupNameLikeLeadingWildcard() {
    // given

    // when
    Group group = identityService.createGroupQuery().groupNameLike("*agement").singleResult();

    // then
    assertThat(group).isNotNull();
    assertThat(group.getId()).isEqualTo("management");
    assertThat(group.getName()).isEqualTo("management");
  }

  @Test
  public void testFilterByGroupNameLikeLeadingAndTrailingWildCard() {
    // given

    // when
    Group group = identityService.createGroupQuery().groupNameLike("*ageme*").singleResult();

    // then
    assertThat(group).isNotNull();
    assertThat(group.getId()).isEqualTo("management");
    assertThat(group.getName()).isEqualTo("management");
  }

  @Test
  public void testFilterByGroupNameLikeMIddleWildCard() {
    // given

    // when
    Group group = identityService.createGroupQuery().groupNameLike("man*nt").singleResult();

    // then
    assertThat(group).isNotNull();
    assertThat(group.getId()).isEqualTo("management");
    assertThat(group.getName()).isEqualTo("management");
  }

  @Test
  public void testFilterByNonexistingGroupNameLike() {
    // given

    // when
    Group group = identityService.createGroupQuery().groupNameLike("what*").singleResult();

    // then
    assertThat(group).isNull();
  }

  @Test
  public void testFilterByGroupNameLikeConvertFromDbWildcard() {
    // given

    // when using the SQL wildcard (%) instead of LDAP (*)
    Group group = identityService.createGroupQuery().groupNameLike("manage%").singleResult();

    // then
    assertThat(group).isNotNull();
    assertThat(group.getId()).isEqualTo("management");
    assertThat(group.getName()).isEqualTo("management");
  }

  @Test
  public void testFilterByGroupMember() {
    // given

    // when
    List<Group> list = identityService.createGroupQuery().groupMember("daniel").list();

    // then
    assertThat(list).hasSize(3);
    assertThat(list).extracting("name").containsOnly("development", "management", "all");
  }

  @Test
  public void testFilterByNonexistingGroupMember() {
    // given

    // when
    List<Group> list = identityService.createGroupQuery().groupMember("non-existing").list();

    // then
    assertThat(list).isEmpty();
  }

  @Test
  public void testFilterByGroupMemberSpecialCharacter() {
    // given

    // when
    List<Group> list = identityService.createGroupQuery().groupMember("david(IT)").list();

    // then
    assertThat(list).hasSize(2);
    assertThat(list).extracting("name").containsOnly("sales", "all");
  }

  @Test
  public void testFilterByGroupMemberPosix() {
    // given
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

    // when I query for groups by group member
    LdapGroupQuery query = new LdapGroupQuery();
    query.groupMember("jonny");

    // then the full DN is requested. This is the default behavior.
    String filter = session.getGroupSearchFilter(query);
    assertThat(filter).isEqualTo("(&(someFilter)(memberUid=jonny, fullDn))");

    // If I turn on posix groups
    ldapConfiguration.setUsePosixGroups(true);

    //  then the filter string does not contain the full DN for the
    // user but the simple (unqualified) userId as provided in the query
    filter = session.getGroupSearchFilter(query);
    assertThat(filter).isEqualTo("(&(someFilter)(memberUid=jonny))");
  }


  @Test
  public void testPagination() {
    testGroupPaging(identityService);
  }

  @Test
  public void testPaginationWithAuthenticatedUser() {
    createGrantAuthorization(GROUP, "management", "oscar", READ);
    createGrantAuthorization(GROUP, "consulting", "oscar", READ);
    createGrantAuthorization(GROUP, "external", "oscar", READ);

    try {
      processEngineConfiguration.setAuthorizationEnabled(true);

      identityService.setAuthenticatedUserId("oscar");

      Set<String> groupNames = new HashSet<>();
      List<Group> groups = identityService.createGroupQuery().listPage(0, 2);
      assertThat(groups).hasSize(2);
      checkPagingResults(groupNames, groups.get(0).getId(), groups.get(1).getId());

      groups = identityService.createGroupQuery().listPage(2, 2);
      assertThat(groups).hasSize(1);
      assertThat(groupNames).doesNotContain(groups.get(0).getId());
      groupNames.add(groups.get(0).getId());

      groups = identityService.createGroupQuery().listPage(4, 2);
      assertThat(groups).hasSize(0);

      identityService.setAuthenticatedUserId("daniel");

      groups = identityService.createGroupQuery().listPage(0, 2);
      assertThat(groups).hasSize(0);

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
