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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Resources.USER;
import static org.camunda.bpm.identity.ldap.util.LdapTestUtilities.checkPagingResults;
import static org.camunda.bpm.identity.ldap.util.LdapTestUtilities.testUserPaging;
import static org.camunda.bpm.identity.ldap.util.LdapTestUtilities.testUserPagingWithMemberOfGroup;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.identity.UserQuery;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.identity.ldap.util.LdapTestEnvironment;
import org.camunda.bpm.identity.ldap.util.LdapTestEnvironmentRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

public class LdapUserQueryTest {

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
  public void testCountUsers() {
    // given

    // when
    UserQuery userQuery = identityService.createUserQuery();

    // then
    assertThat(userQuery.listPage(0, Integer.MAX_VALUE)).hasSize(12);
    assertThat(userQuery.count()).isEqualTo(12);
  }

  @Test
  public void testQueryNoFilter() {
    // given

    // when
    List<User> result = identityService.createUserQuery().list();

    // then
    assertThat(result).hasSize(ldapTestEnvironment.getTotalNumberOfUsersCreated());
  }

  @Test
  public void testFilterByUserId() {
    // given

    // when
    User user = identityService.createUserQuery().userId("oscar").singleResult();

    // then
    assertThat(user).isNotNull();

    // validate user
    assertThat(user.getId()).isEqualTo("oscar");
    assertThat(user.getFirstName()).isEqualTo("Oscar");
    assertThat(user.getLastName()).isEqualTo("The Crouch");
    assertThat(user.getEmail()).isEqualTo("oscar@camunda.org");
  }

  @Test
  public void testFilterByNonexistentUserId() {
    // given

    // when
    User user = identityService.createUserQuery().userId("non-existing").singleResult();

    // then
    assertThat(user).isNull();
  }

  @Test
  public void testFilterByUserIdIn() {
    // given

    // when
    List<User> users = identityService.createUserQuery().userIdIn("oscar", "monster").list();

    // then
    assertThat(users).hasSize(2);
    assertThat(users).extracting("id").containsOnly("oscar", "monster");
  }

  @Test
  public void testFilterByNonExistingUserIdIn() {
    // given

    // when
    List<User> users = identityService.createUserQuery().userIdIn("oscar", "monster", "daniel", "non-existing").list();

    // then
    assertThat(users).isNotNull();
    assertThat(users).hasSize(3);
    assertThat(users).extracting("id").containsOnly("oscar", "monster", "daniel");
  }

  @Test
  public void testFilterByUserIdWithCapitalization() {
    try {
      // given
      processEngineConfiguration.setAuthorizationEnabled(true);
      identityService.setAuthenticatedUserId("Oscar");

      // when
      User user = identityService.createUserQuery().userId("Oscar").singleResult();

      // then
      assertThat(user).isNotNull();

      // validate user
      assertThat(user.getId()).isEqualTo("oscar");
      assertThat(user.getFirstName()).isEqualTo("Oscar");
      assertThat(user.getLastName()).isEqualTo("The Crouch");
      assertThat(user.getEmail()).isEqualTo("oscar@camunda.org");
    } finally {
      processEngineConfiguration.setAuthorizationEnabled(false);
      identityService.clearAuthentication();
    }
  }

  @Test
  public void testFilterByFirstname() {
    // given

    // when
    User user = identityService.createUserQuery().userFirstName("Oscar").singleResult();

    // then
    assertThat(user).isNotNull();
    assertThat(user.getFirstName()).isEqualTo("Oscar");
  }

  @Test
  public void testFilterByNonexistingFirstname() {
    // given

    // when
    User user = identityService.createUserQuery().userFirstName("non-existing").singleResult();

    // then
    assertThat(user).isNull();
  }

  @Test
  public void testFilterByFirstnameLikeTrailingWildcard() {
    // given

    // when
    User user = identityService.createUserQuery().userFirstNameLike("Osc*").singleResult();

    // then
    assertThat(user).isNotNull();
    assertThat(user.getFirstName()).isEqualTo("Oscar");
  }

  @Test
  public void testFilterByFirstnameLikeLeadingWildcard() {
    // given

    // when
    User user = identityService.createUserQuery().userFirstNameLike("*car").singleResult();

    // then
    assertThat(user).isNotNull();
    assertThat(user.getFirstName()).isEqualTo("Oscar");
  }

  @Test
  public void testFilterByFirstnameLikeLeadingAndTrailingWildcard() {
    // given

    // when
    User user = identityService.createUserQuery().userFirstNameLike("*sca*").singleResult();

    // then
    assertThat(user).isNotNull();
    assertThat(user.getFirstName()).isEqualTo("Oscar");
  }

  @Test
  public void testFilterByFirstnameLikeMiddleWildcard() {
    // given

    // when
    User user = identityService.createUserQuery().userFirstNameLike("O*ar").singleResult();

    // then
    assertThat(user).isNotNull();
    assertThat(user.getFirstName()).isEqualTo("Oscar");
  }

  @Test
  public void testFilterByFirstnameLikeConvertFromDbWildcard() {
    // given

    // when using the SQL wildcard (%) instead of LDAP (*)
    User user = identityService.createUserQuery().userFirstNameLike("Osc%").singleResult();

    // then
    assertThat(user).isNotNull();
    assertThat(user.getFirstName()).isEqualTo("Oscar");
  }

  @Test
  public void testFilterByNonexistingFirstnameLike() {
    // given

    // when
    User user = identityService.createUserQuery().userFirstNameLike("non-exist*").singleResult();

    // then
    assertThat(user).isNull();
  }

  @Test
  public void testFilterByLastname() {
    // given

    // when
    User user = identityService.createUserQuery().userLastName("The Crouch").singleResult();

    // then
    assertThat(user).isNotNull();
    assertThat(user.getLastName()).isEqualTo("The Crouch");
  }

  @Test
  public void testFilterByNonexistingLastname() {
    // given

    // when
    User user = identityService.createUserQuery().userLastName("non-existing").singleResult();

    // then
    assertThat(user).isNull();
  }

  @Test
  public void testFilterByLastnameLikeTrailingWildcard() {
    // given

    // when
    User user = identityService.createUserQuery().userLastNameLike("The Cro*").singleResult();

    // then
    assertThat(user).isNotNull();
    assertThat(user.getLastName()).isEqualTo("The Crouch");
  }

  @Test
  public void testFilterByLastnameLikeLeadingWildcard() {
    // given

    // when
    User user = identityService.createUserQuery().userLastNameLike("* Crouch").singleResult();

    // then
    assertThat(user).isNotNull();
    assertThat(user.getLastName()).isEqualTo("The Crouch");
  }

  @Test
  public void testFilterByLastnameLikeLeadingAndTrailingWildcard() {
    // given

    // when
    User user = identityService.createUserQuery().userLastNameLike("* Cro*").singleResult();

    // then
    assertThat(user).isNotNull();
    assertThat(user.getLastName()).isEqualTo("The Crouch");
  }

  @Test
  public void testFilterByLastnameLikeMiddleWildcard() {
    // given

    // when
    User user = identityService.createUserQuery().userLastNameLike("The *uch").singleResult();

    // then
    assertThat(user).isNotNull();
    assertThat(user.getLastName()).isEqualTo("The Crouch");
  }

  @Test
  public void testFilterByNonexistingLastnameLike() {
    // given

    // when
    User user = identityService.createUserQuery().userLastNameLike("non-exist*").singleResult();

    // then
    assertThat(user).isNull();
  }

  @Test
  public void testFilterByLastnameLikeConvertFromDbWildcard() {
    // given

    // when using the SQL wildcard (%) instead of LDAP (*)
    User user = identityService.createUserQuery().userLastNameLike("The Cro%").singleResult();

    // then
    assertThat(user).isNotNull();
    assertThat(user.getLastName()).isEqualTo("The Crouch");
  }

  @Test
  public void testFilterByEmail() {
    // given

    // when
    User user = identityService.createUserQuery().userEmail("oscar@camunda.org").singleResult();

    // then
    assertThat(user).isNotNull();
    assertThat(user.getEmail()).isEqualTo("oscar@camunda.org");
  }

  @Test
  public void testFilterByNonexistingEmail() {
    // given

    // when
    User user = identityService.createUserQuery().userEmail("non-exist").singleResult();

    // then
    assertThat(user).isNull();
  }

  @Test
  public void testFilterByEmailLikeTrailingWildCard() {
    // given

    // when
    User user = identityService.createUserQuery().userEmailLike("oscar@*").singleResult();

    // then
    assertThat(user).isNotNull();
    assertThat(user.getEmail()).isEqualTo("oscar@camunda.org");
  }

  @Test
  public void testFilterByEmailLikeLeadingWildCard() {
    // given

    // when
    User user = identityService.createUserQuery().userEmailLike("*car@camunda.org").singleResult();

    // then
    assertThat(user).isNotNull();
    assertThat(user.getEmail()).isEqualTo("oscar@camunda.org");
  }

  @Test
  public void testFilterByEmailLikeLeadingAndTrailingWildCard() {
    // given

    // when
    User user = identityService.createUserQuery().userEmailLike("*car@*").singleResult();

    // then
    assertThat(user).isNotNull();
    assertThat(user.getEmail()).isEqualTo("oscar@camunda.org");
  }

  @Test
  public void testFilterByEmailLikeMiddleWildCard() {
    // given

    // when
    User user = identityService.createUserQuery().userEmailLike("oscar@*.org").singleResult();

    // then
    assertThat(user).isNotNull();
    assertThat(user.getEmail()).isEqualTo("oscar@camunda.org");
  }

  @Test
  public void testFilterByNonexistingEmailLike() {
    // given

    // when
    User user = identityService.createUserQuery().userEmailLike("non-exist*").singleResult();

    // then
    assertThat(user).isNull();
  }

  @Test
  public void testFilterByEmailLikeConvertFromDbWildcard() {
    // given

    // when using the SQL wildcard (%) instead of LDAP (*)
    User user = identityService.createUserQuery().userEmailLike("oscar@%").singleResult();

    // then
    assertThat(user).isNotNull();
    assertThat(user.getEmail()).isEqualTo("oscar@camunda.org");
  }

  @Test
  public void testFilterByGroupId() {
    // given

    // when
    List<User> result = identityService.createUserQuery().memberOfGroup("development").list();

    // then
    assertThat(result).hasSize(3);
    assertThat(result).extracting("id").containsOnly("roman", "daniel", "oscar");
  }

  @Test
  public void testFilterByGroupIdAndFirstname() {
    // given

    // when
    List<User> result = identityService.createUserQuery()
            .memberOfGroup("development")
            .userFirstName("Oscar")
            .list();

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getFirstName()).isEqualTo("Oscar");
  }

  @Test
  public void testFilterByGroupIdAndId() {
    // given

    // when
    List<User> result = identityService.createUserQuery()
            .memberOfGroup("development")
            .userId("oscar")
            .list();

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo("oscar");
  }

  @Test
  public void testFilterByGroupIdAndLastname() {
    // given

    // when
    List<User> result = identityService.createUserQuery()
            .memberOfGroup("development")
            .userLastName("The Crouch")
            .list();

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getLastName()).isEqualTo("The Crouch");
  }

  @Test
  public void testFilterByGroupIdAndEmail() {
    // given

    // when
    List<User> result = identityService.createUserQuery()
            .memberOfGroup("development")
            .userEmail("oscar@camunda.org")
            .list();

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getEmail()).isEqualTo("oscar@camunda.org");
  }

  @Test
  public void testFilterByGroupIdAndEmailLike() {
    // given

    // when
    List<User> result = identityService.createUserQuery()
            .memberOfGroup("development")
            .userEmailLike("*@camunda.org")
            .list();

    // then
    assertThat(result).hasSize(3);
    assertThat(result).extracting("email").containsOnly("daniel@camunda.org", "roman@camunda.org", "oscar@camunda.org");
  }

  @Test
  public void testFilterByGroupIdAndIdForDnUsingCn() {
    // given

    // when
    List<User> result = identityService.createUserQuery()
            .memberOfGroup("external")
            .userId("fozzie")
            .list();

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo("fozzie");
  }

  @Test
  public void testAuthenticatedUserSeesThemselve() {
    try {
      // given
      processEngineConfiguration.setAuthorizationEnabled(true);
      identityService.setAuthenticatedUserId("oscar");

      // when
      User user = identityService.createUserQuery().singleResult();

      // then
      assertThat(user).isNotNull();
      assertThat(user.getId()).isEqualTo("oscar");
    } finally {
      processEngineConfiguration.setAuthorizationEnabled(false);
      identityService.clearAuthentication();
    }
  }

  @Test
  public void testNonexistingAuthenticatedUserDoesNotSeeThemselve() {
    try {
      // given
      processEngineConfiguration.setAuthorizationEnabled(true);
      identityService.setAuthenticatedUserId("non-existing");

      // when
      User user = identityService.createUserQuery().singleResult();

      // then
      assertThat(user).isNull();
    } finally {
      processEngineConfiguration.setAuthorizationEnabled(false);
      identityService.clearAuthentication();
    }
  }

  @Test
  public void testPagination() {
    testUserPaging(identityService, ldapTestEnvironment);
  }

  @Test
  public void testPaginationWithMemberOfGroup() {
    testUserPagingWithMemberOfGroup(identityService);
  }

  @Test
  public void testPaginationWithAuthenticatedUser() {
    createGrantAuthorization(USER, "roman", "oscar", READ);
    createGrantAuthorization(USER, "daniel", "oscar", READ);
    createGrantAuthorization(USER, "monster", "oscar", READ);
    createGrantAuthorization(USER, "ruecker", "oscar", READ);

    try {
      processEngineConfiguration.setAuthorizationEnabled(true);

      identityService.setAuthenticatedUserId("oscar");

      Set<String> userNames = new HashSet<>();
      List<User> users = identityService.createUserQuery().listPage(0, 2);
      assertThat(users).hasSize(2);
      checkPagingResults(userNames, users.get(0).getId(), users.get(1).getId());

      users = identityService.createUserQuery().listPage(2, 2);
      assertThat(users).hasSize(2);
      checkPagingResults(userNames, users.get(0).getId(), users.get(1).getId());

      users = identityService.createUserQuery().listPage(4, 2);
      assertThat(users).hasSize(1);
      assertThat(userNames).doesNotContain(users.get(0).getId());
      userNames.add(users.get(0).getId());

      identityService.setAuthenticatedUserId("daniel");

      users = identityService.createUserQuery().listPage(0, 2);
      assertThat(users).hasSize(1);

      assertThat(users.get(0).getId()).isEqualTo("daniel");

      users = identityService.createUserQuery().listPage(2, 2);
      assertThat(users).hasSize(0);

    } finally {
      processEngineConfiguration.setAuthorizationEnabled(false);
      identityService.clearAuthentication();

      for (Authorization authorization : authorizationService.createAuthorizationQuery().list()) {
        authorizationService.deleteAuthorization(authorization.getId());
      }

    }
  }

  @Test
  public void testNativeQueryFail() {
    assertThatThrownBy(() -> identityService.createNativeUserQuery())
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("Native user queries are not supported for LDAP");
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
