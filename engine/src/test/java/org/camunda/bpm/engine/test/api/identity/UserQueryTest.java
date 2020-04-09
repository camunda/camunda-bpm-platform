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
package org.camunda.bpm.engine.test.api.identity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.identity.UserQuery;
import org.camunda.bpm.engine.impl.persistence.entity.UserEntity;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * @author Joram Barrez
 */
public class UserQueryTest extends PluggableProcessEngineTest {

  @Before
  public void setUp() throws Exception {


    createUser("kermit", "Kermit_", "The_frog", "kermit_@muppetshow.com");
    createUser("fozzie", "Fozzie", "Bear", "fozzie@muppetshow.com");
    createUser("gonzo", "Gonzo", "The great", "gonzo@muppetshow.com");

    identityService.saveGroup(identityService.newGroup("muppets"));
    identityService.saveGroup(identityService.newGroup("frogs"));

    identityService.saveTenant(identityService.newTenant("tenant"));

    identityService.createMembership("kermit", "muppets");
    identityService.createMembership("kermit", "frogs");
    identityService.createMembership("fozzie", "muppets");
    identityService.createMembership("gonzo", "muppets");

    identityService.createTenantUserMembership("tenant", "kermit");
  }

  private User createUser(String id, String firstName, String lastName, String email) {
    User user = identityService.newUser(id);
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setEmail(email);
    identityService.saveUser(user);
    return user;
  }

  @After
  public void tearDown() throws Exception {
    identityService.deleteUser("kermit");
    identityService.deleteUser("fozzie");
    identityService.deleteUser("gonzo");

    identityService.deleteGroup("muppets");
    identityService.deleteGroup("frogs");

    identityService.deleteTenant("tenant");


  }

  @Test
  public void testQueryByNoCriteria() {
    UserQuery query = identityService.createUserQuery();
    verifyQueryResults(query, 3);
  }

  @Test
  public void testQueryById() {
    UserQuery query = identityService.createUserQuery().userId("kermit");
    verifyQueryResults(query, 1);
  }

  @Test
  public void testQueryByInvalidId() {
    UserQuery query = identityService.createUserQuery().userId("invalid");
    verifyQueryResults(query, 0);

    try {
      identityService.createUserQuery().userId(null).singleResult();
      fail();
    } catch (ProcessEngineException e) { }
  }

  @Test
  public void testQueryByFirstName() {
    UserQuery query = identityService.createUserQuery().userFirstName("Gonzo");
    verifyQueryResults(query, 1);

    User result = query.singleResult();
    assertEquals("gonzo", result.getId());
  }

  @Test
  public void testQueryByInvalidFirstName() {
    UserQuery query = identityService.createUserQuery().userFirstName("invalid");
    verifyQueryResults(query, 0);

    try {
      identityService.createUserQuery().userFirstName(null).singleResult();
      fail();
    } catch (ProcessEngineException e) { }
  }

  @Test
  public void testQueryByFirstNameLike() {
    UserQuery query = identityService.createUserQuery().userFirstNameLike("%o%");
    verifyQueryResults(query, 2);

    query = identityService.createUserQuery().userFirstNameLike("Ker%");
    verifyQueryResults(query, 1);

    identityService.createUserQuery().userFirstNameLike("%mit\\_");
    verifyQueryResults(query, 1);
  }

  @Test
  public void testQueryByInvalidFirstNameLike() {
    UserQuery query = identityService.createUserQuery().userFirstNameLike("%mispiggy%");
    verifyQueryResults(query, 0);

    try {
      identityService.createUserQuery().userFirstNameLike(null).singleResult();
      fail();
    } catch (ProcessEngineException e) { }
  }

  @Test
  public void testQueryByLastName() {
    UserQuery query = identityService.createUserQuery().userLastName("Bear");
    verifyQueryResults(query, 1);

    User result = query.singleResult();
    assertEquals("fozzie", result.getId());
  }

  @Test
  public void testQueryByInvalidLastName() {
    UserQuery query = identityService.createUserQuery().userLastName("invalid");
    verifyQueryResults(query, 0);

    try {
      identityService.createUserQuery().userLastName(null).singleResult();
      fail();
    } catch (ProcessEngineException e) { }
  }

  @Test
  public void testQueryByLastNameLike() {
    UserQuery query = identityService.createUserQuery().userLastNameLike("%\\_frog%");
    verifyQueryResults(query, 1);

    query = identityService.createUserQuery().userLastNameLike("%ea%");
    verifyQueryResults(query, 2);
  }

  @Test
  public void testQueryByInvalidLastNameLike() {
    UserQuery query = identityService.createUserQuery().userLastNameLike("%invalid%");
    verifyQueryResults(query, 0);

    try {
      identityService.createUserQuery().userLastNameLike(null).singleResult();
      fail();
    } catch (ProcessEngineException e) { }
  }

  @Test
  public void testQueryByEmail() {
    UserQuery query = identityService.createUserQuery().userEmail("kermit_@muppetshow.com");
    verifyQueryResults(query, 1);
  }

  @Test
  public void testQueryByInvalidEmail() {
    UserQuery query = identityService.createUserQuery().userEmail("invalid");
    verifyQueryResults(query, 0);

    try {
      identityService.createUserQuery().userEmail(null).singleResult();
      fail();
    } catch (ProcessEngineException e) { }
  }

  @Test
  public void testQueryByEmailLike() {
    UserQuery query = identityService.createUserQuery().userEmailLike("%muppetshow.com");
    verifyQueryResults(query, 3);

    query = identityService.createUserQuery().userEmailLike("%kermit\\_%");
    verifyQueryResults(query, 1);
  }

  @Test
  public void testQueryByInvalidEmailLike() {
    UserQuery query = identityService.createUserQuery().userEmailLike("%invalid%");
    verifyQueryResults(query, 0);

    try {
      identityService.createUserQuery().userEmailLike(null).singleResult();
      fail();
    } catch (ProcessEngineException e) { }
  }

  @Test
  public void testQuerySorting() {
    // asc
    assertEquals(3, identityService.createUserQuery().orderByUserId().asc().count());
    assertEquals(3, identityService.createUserQuery().orderByUserEmail().asc().count());
    assertEquals(3, identityService.createUserQuery().orderByUserFirstName().asc().count());
    assertEquals(3, identityService.createUserQuery().orderByUserLastName().asc().count());

    // desc
    assertEquals(3, identityService.createUserQuery().orderByUserId().desc().count());
    assertEquals(3, identityService.createUserQuery().orderByUserEmail().desc().count());
    assertEquals(3, identityService.createUserQuery().orderByUserFirstName().desc().count());
    assertEquals(3, identityService.createUserQuery().orderByUserLastName().desc().count());

    // Combined with criteria
    UserQuery query = identityService.createUserQuery().userLastNameLike("%ea%").orderByUserFirstName().asc();
    List<User> users = query.list();
    assertEquals(2,users.size());
    assertEquals("Fozzie", users.get(0).getFirstName());
    assertEquals("Gonzo", users.get(1).getFirstName());
  }

  @Test
  public void testQueryInvalidSortingUsage() {
    try {
      identityService.createUserQuery().orderByUserId().list();
      fail();
    } catch (ProcessEngineException e) {}

    try {
      identityService.createUserQuery().orderByUserId().orderByUserEmail().list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  @Test
  public void testQueryByMemberOfGroup() {
    UserQuery query = identityService.createUserQuery().memberOfGroup("muppets");
    verifyQueryResults(query, 3);

    query = identityService.createUserQuery().memberOfGroup("frogs");
    verifyQueryResults(query, 1);

    User result = query.singleResult();
    assertEquals("kermit", result.getId());
  }

  @Test
  public void testQueryByInvalidMemberOfGoup() {
    UserQuery query = identityService.createUserQuery().memberOfGroup("invalid");
    verifyQueryResults(query, 0);

    try {
      identityService.createUserQuery().memberOfGroup(null).list();
      fail();
    } catch (ProcessEngineException e) { }
  }

  @Test
  public void testQueryByMemberOfTenant() {
    UserQuery query = identityService.createUserQuery().memberOfTenant("nonExisting");
    verifyQueryResults(query, 0);

    query = identityService.createUserQuery().memberOfTenant("tenant");
    verifyQueryResults(query, 1);

    User result = query.singleResult();
    assertEquals("kermit", result.getId());
  }

  private void verifyQueryResults(UserQuery query, int countExpected) {
    assertEquals(countExpected, query.list().size());
    assertEquals(countExpected, query.count());

    if (countExpected == 1) {
      assertNotNull(query.singleResult());
    } else if (countExpected > 1){
      verifySingleResultFails(query);
    } else if (countExpected == 0) {
      assertNull(query.singleResult());
    }
  }

  private void verifySingleResultFails(UserQuery query) {
    try {
      query.singleResult();
      fail();
    } catch (ProcessEngineException e) {}
  }

  @Test
  public void testQueryByIdIn() {

    // empty list
    assertTrue(identityService.createUserQuery().userIdIn("a", "b").list().isEmpty());


    // collect all ids
    List<User> list = identityService.createUserQuery().list();
    String[] ids = new String[list.size()];
    for (int i = 0; i < ids.length; i++) {
      ids[i] = list.get(i).getId();
    }

    List<User> idInList = identityService.createUserQuery().userIdIn(ids).list();
    for (User user : idInList) {
      boolean found = false;
      for (User otherUser : list) {
        if(otherUser.getId().equals(user.getId())) {
          found = true; break;
        }
      }
      if(!found) {
        fail("Expected to find user "+user);
      }
    }
  }

  @Test
  public void testNativeQuery() {
    String tablePrefix = processEngineConfiguration.getDatabaseTablePrefix();
    // just test that the query will be constructed and executed, details are tested in the TaskQueryTest
    assertEquals(tablePrefix + "ACT_ID_USER", managementService.getTableName(UserEntity.class));

    long userCount = identityService.createUserQuery().count();

    assertEquals(userCount, identityService.createNativeUserQuery().sql("SELECT * FROM " + managementService.getTableName(UserEntity.class)).list().size());
    assertEquals(userCount, identityService.createNativeUserQuery().sql("SELECT count(*) FROM " + managementService.getTableName(UserEntity.class)).count());
  }

  @Test
  public void testNativeQueryOrLike() {
    String searchPattern = "%frog";

    String fromWhereClauses = String.format("FROM %s WHERE FIRST_ LIKE #{searchPattern} OR LAST_ LIKE #{searchPattern} OR EMAIL_ LIKE #{searchPattern}",
        managementService.getTableName(UserEntity.class));

    assertEquals(1, identityService.createNativeUserQuery().sql("SELECT * " + fromWhereClauses).parameter("searchPattern", searchPattern).list().size());
    assertEquals(1, identityService.createNativeUserQuery().sql("SELECT count(*) " + fromWhereClauses).parameter("searchPattern", searchPattern).count());
  }

  @Test
  public void testNativeQueryPaging() {
    assertEquals(2, identityService.createNativeUserQuery().sql("SELECT * FROM " + managementService.getTableName(UserEntity.class)).listPage(1, 2).size());
    assertEquals(1, identityService.createNativeUserQuery().sql("SELECT * FROM " + managementService.getTableName(UserEntity.class)).listPage(2, 1).size());
  }

}
