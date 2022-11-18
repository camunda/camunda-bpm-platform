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
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.identity.UserQuery;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.identity.ldap.util.LdapTestEnvironment;
import org.camunda.bpm.identity.ldap.util.LdapTestEnvironmentRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

public class LdapUserLargeQueryTest {

  @ClassRule
  public static LdapTestEnvironmentRule ldapRule = new LdapTestEnvironmentRule().additionalNumberOfUsers(80).additionnalNumberOfGroups(5).additionalNumberOfRoles(5); // Attention, stay under 80, there is a limitation in the query on 100
  @Rule
  public ProcessEngineRule engineRule = new ProcessEngineRule("camunda.ldap.pages.cfg.xml"); // pageSize = 3 in this configuration

  ProcessEngineConfiguration processEngineConfiguration;
  IdentityService identityService;
  LdapTestEnvironment ldapTestEnvironment;

  @Before
  public void setup() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    identityService = engineRule.getIdentityService();
    ldapTestEnvironment = ldapRule.getLdapTestEnvironment();
  }

  @Test
  public void testAllUsersQuery() {
    List<User> listUsers = identityService.createUserQuery().list();

    // In this group, we expect more than a page size
    assertThat(listUsers).hasSize(ldapTestEnvironment.getTotalNumberOfUsersCreated());
  }

  @Test
  public void testPagesAllUsersQuery() {
    List<User> listUsers = identityService.createUserQuery().list();

    assertThat(listUsers).hasSize(ldapTestEnvironment.getTotalNumberOfUsersCreated());

    // ask 3 pages
    for (int firstResult = 0; firstResult < 10; firstResult += 4) {
      List<User> listPages = identityService.createUserQuery().listPage(firstResult, 5);
      for (int i = 0; i < listPages.size(); i++) {
        assertThat(listPages.get(i).getId()).isEqualTo(listUsers.get(firstResult + i).getId());
        assertThat(listPages.get(i).getLastName()).isEqualTo(listUsers.get(firstResult + i).getLastName());
      }

    }
  }

  @Test
  public void testQueryPaging() {
    UserQuery query = identityService.createUserQuery();

    assertThat(query.listPage(0, Integer.MAX_VALUE)).hasSize(92);

    // Verifying the un-paged results
    assertThat(query.count()).isEqualTo(92);
    assertThat(query.list()).hasSize(92);

    // Verifying paged results
    assertThat(query.listPage(0, 2)).hasSize(2);
    assertThat(query.listPage(2, 2)).hasSize(2);
    assertThat(query.listPage(4, 3)).hasSize(3);
    assertThat(query.listPage(91, 3)).hasSize(1);
    assertThat(query.listPage(91, 1)).hasSize(1);

    // Verifying odd usages
    assertThat(query.listPage(-1, -1)).hasSize(0);
    assertThat(query.listPage(92, 2)).hasSize(0); // 92 is the last index with a result
    assertThat(query.listPage(0, 93)).hasSize(92); // there are only 92 groups
  }

}
