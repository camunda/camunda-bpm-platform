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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotSame;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;

/**
 * Contains some test utilities to test the Ldap plugin.
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public final class LdapTestUtilities {

  public static void checkPagingResults(Set<String> results, String result1, String result2) {
    assertNotSame(result1, result2);
    assertFalse(results.contains(result1));
    results.add(result1);
    assertFalse(results.contains(result2));
    results.add(result2);
  }

  public static void testGroupPaging(IdentityService identityService) {
    Set<String> groupNames = new HashSet<String>();
    List<Group> groups = identityService.createGroupQuery().listPage(0, 2);
    assertEquals(2, groups.size());
    checkPagingResults(groupNames, groups.get(0).getId(), groups.get(1).getId());

    groups = identityService.createGroupQuery().listPage(2, 2);
    assertEquals(2, groups.size());
    checkPagingResults(groupNames, groups.get(0).getId(), groups.get(1).getId());

    groups = identityService.createGroupQuery().listPage(4, 2);
    assertEquals(2, groups.size());
    assertFalse(groupNames.contains(groups.get(0).getId()));
    groupNames.add(groups.get(0).getId());

    groups = identityService.createGroupQuery().listPage(6, 2);
    assertEquals(0, groups.size());
  }

  public static void testUserPaging(IdentityService identityService) {
    Set<String> userNames = new HashSet<String>();
    List<User> users = identityService.createUserQuery().listPage(0, 2);
    assertEquals(2, users.size());
    checkPagingResults(userNames, users.get(0).getId(), users.get(1).getId());

    users = identityService.createUserQuery().listPage(2, 2);
    assertEquals(2, users.size());
    checkPagingResults(userNames, users.get(0).getId(), users.get(1).getId());

    users = identityService.createUserQuery().listPage(4, 2);
    assertEquals(2, users.size());
    checkPagingResults(userNames, users.get(0).getId(), users.get(1).getId());

    users = identityService.createUserQuery().listPage(6, 2);
    assertEquals(2, users.size());
    checkPagingResults(userNames, users.get(0).getId(), users.get(1).getId());

    users = identityService.createUserQuery().listPage(12, 2);
    assertEquals(0, users.size());
  }

  public static void testUserPagingWithMemberOfGroup(IdentityService identityService) {
    Set<String> userNames = new HashSet<String>();
    List<User> users = identityService.createUserQuery().memberOfGroup("all").listPage(0, 2);
    assertEquals(2, users.size());
    checkPagingResults(userNames, users.get(0).getId(), users.get(1).getId());

    users = identityService.createUserQuery().memberOfGroup("all").listPage(2, 2);
    assertEquals(2, users.size());
    checkPagingResults(userNames, users.get(0).getId(), users.get(1).getId());

    users = identityService.createUserQuery().memberOfGroup("all").listPage(4, 2);
    assertEquals(2, users.size());
    checkPagingResults(userNames, users.get(0).getId(), users.get(1).getId());

    users = identityService.createUserQuery().memberOfGroup("all").listPage(11, 2);
    assertEquals(1, users.size());
    assertFalse(userNames.contains(users.get(0).getId()));

    users = identityService.createUserQuery().memberOfGroup("all").listPage(12, 2);
    assertEquals(0, users.size());
  }
}
