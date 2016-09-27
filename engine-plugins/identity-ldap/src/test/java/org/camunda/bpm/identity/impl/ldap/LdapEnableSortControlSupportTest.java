/*
 * Copyright 2016 camunda services GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.test.ResourceProcessEngineTestCase;

/**
 * Represents a test case where the sortControlSupport property is enabled.
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class LdapEnableSortControlSupportTest extends ResourceProcessEngineTestCase {

  public LdapEnableSortControlSupportTest() {
    super("camunda.ldap.enable.sort.control.support.cfg.xml");
  }

  protected static LdapTestEnvironment ldapTestEnvironment;

  @Override
  protected void setUp() throws Exception {
    if(ldapTestEnvironment == null) {
      ldapTestEnvironment = new LdapTestEnvironment();
      ldapTestEnvironment.init();
    }
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



  public void testOrderByUserFirstName() {
    List<User> orderedUsers = identityService.createUserQuery().orderByUserLastName().asc().list();
    List<User> userList = identityService.createUserQuery().list();

    Collections.sort(userList, new Comparator<User>() {
      @Override
      public int compare(User o1, User o2) {
        return o1.getLastName().compareToIgnoreCase(o2.getLastName());
      }
    });

    int len = orderedUsers.size();
    for (int i = 0; i < len; i++) {
      assertEquals("Index: " + i, orderedUsers.get(i).getLastName(), userList.get(i).getLastName());
    }
  }
}
