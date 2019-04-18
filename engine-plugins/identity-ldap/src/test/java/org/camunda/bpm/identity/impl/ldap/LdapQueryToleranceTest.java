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

import java.util.List;

import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.test.ResourceProcessEngineTestCase;
import org.junit.Assert;


/**
 * @author Thorben Lindhauer
 *
 */
public class LdapQueryToleranceTest extends ResourceProcessEngineTestCase {

  private LdapTestEnvironment ldapTestEnvironment;

  public LdapQueryToleranceTest() {
    super("invalid-id-attributes.cfg.xml");
  }

  @Override
  protected void setUp() throws Exception {
    ldapTestEnvironment = new LdapTestEnvironment();
    ldapTestEnvironment.init();
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    if(ldapTestEnvironment != null) {
      ldapTestEnvironment.shutdown();
      ldapTestEnvironment = null;
    }
    super.tearDown();
  }

  public void testNotReturnGroupsWithNullId() throws Exception
  {
    // given
    // LdapTestEnvironment creates six groups by default;
    // these won't return a group id, because they do not have the group id attribute
    // defined in the ldap plugin config
    // the plugin should not return such groups and instead log an error

    // when
    List<Group> groups = processEngine.getIdentityService().createGroupQuery().list();
    long count = processEngine.getIdentityService().createGroupQuery().count();

    // then
    // groups with id null were not returned
    Assert.assertEquals(0, groups.size());
    Assert.assertEquals(0, count);
  }

  public void testNotReturnUsersWithNullId() throws Exception
  {
    // given
    // LdapTestEnvironment creates six groups by default;
    // these won't return a group id, because they do not have the group id attribute
    // defined in the ldap plugin config
    // the plugin should not return such groups and instead log an error

    // when
    List<User> users = processEngine.getIdentityService().createUserQuery().list();
    long count = processEngine.getIdentityService().createGroupQuery().count();

    // then
    // groups with id null were not returned
    Assert.assertEquals(0, users.size());
    Assert.assertEquals(0, count);
  }
}
