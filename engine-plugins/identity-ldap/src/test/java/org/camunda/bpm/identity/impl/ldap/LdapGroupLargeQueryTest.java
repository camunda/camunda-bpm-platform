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

import java.util.List;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.identity.ldap.util.LdapTestEnvironment;
import org.camunda.bpm.identity.ldap.util.LdapTestEnvironmentRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

public class LdapGroupLargeQueryTest {

  @ClassRule
  public static LdapTestEnvironmentRule ldapRule = new LdapTestEnvironmentRule().additionalNumberOfUsers(5).additionnalNumberOfGroups(5).additionalNumberOfRoles(80); // the TestEnvironment creates groups for roles. Attention, stay under 80, there is a limitation in the query on 100

  @Rule
  public ProcessEngineRule engineRule = new ProcessEngineRule("camunda.ldap.pages.cfg.xml"); // pageSize = 3 in this configuration

  IdentityService identityService;
  LdapTestEnvironment ldapTestEnvironment;

  @Before
  public void setup() {
    identityService = engineRule.getIdentityService();
    ldapTestEnvironment = ldapRule.getLdapTestEnvironment();
  }

  @Test
  public void testAllGroupsQuery() {
    List<Group> listGroups = identityService.createGroupQuery().list();

    // In this group, we expect more than a page size
    // Attention, in the test environment, a Role is implemented by a groupOfNames. the groupQuery search for groups
    // So the comparison must be done via the Roles name
    assertThat(listGroups).hasSize(ldapTestEnvironment.getTotalNumberOfRolesCreated());
  }

  @Test
  public void testPagesAllGroupsQuery() {
    List<Group> listGroups = identityService.createGroupQuery().list();

    assertThat(listGroups).hasSize(ldapTestEnvironment.getTotalNumberOfRolesCreated());

    // ask 3 pages
    for (int firstResult = 2; firstResult < 10; firstResult += 4) {
      List<Group> listPages = identityService.createGroupQuery().listPage(firstResult, 5);
      assertThat(listPages.size()).as("Maximum 5 results expected").isLessThanOrEqualTo(5);

      for (int i = 0; i < listPages.size(); i++) {
        assertThat(listPages.get(i).getId()).isEqualTo(listGroups.get(firstResult + i).getId());
        assertThat(listPages.get(i).getName()).isEqualTo(listGroups.get(firstResult + i).getName());
      }

    }
  }

  @Test
  public void testQueryPaging() {
    GroupQuery query = identityService.createGroupQuery();

    assertThat(query.listPage(0, Integer.MAX_VALUE)).hasSize(86);

    // Verifying the un-paged results
    assertThat(query.count()).isEqualTo(86);
    assertThat(query.list()).hasSize(86);

    // Verifying paged results
    assertThat(query.listPage(0, 2)).hasSize(2);
    assertThat(query.listPage(2, 2)).hasSize(2);
    assertThat(query.listPage(4, 3)).hasSize(3);
    assertThat(query.listPage(85, 3)).hasSize(1);
    assertThat(query.listPage(85, 1)).hasSize(1);

    // Verifying odd usages
    assertThat(query.listPage(-1, -1)).hasSize(0);
    assertThat(query.listPage(86, 2)).hasSize(0); // 86 is the last index with a result
    assertThat(query.listPage(0, 87)).hasSize(86); // there are only 86 groups
  }

}
