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
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * @author Joram Barrez
 */
public class GroupQueryTest extends PluggableProcessEngineTest {

  @Before
  public void setUp() throws Exception {


    createGroup("muppets", "Muppet show characters_", "user");
    createGroup("frogs", "Famous frogs", "user");
    createGroup("mammals", "Famous mammals from eighties", "user");
    createGroup("admin", "Administrators", "security");

    identityService.saveUser(identityService.newUser("kermit"));
    identityService.saveUser(identityService.newUser("fozzie"));
    identityService.saveUser(identityService.newUser("mispiggy"));

    identityService.saveTenant(identityService.newTenant("tenant"));

    identityService.createMembership("kermit", "muppets");
    identityService.createMembership("fozzie", "muppets");
    identityService.createMembership("mispiggy", "muppets");

    identityService.createMembership("kermit", "frogs");

    identityService.createMembership("fozzie", "mammals");
    identityService.createMembership("mispiggy", "mammals");

    identityService.createMembership("kermit", "admin");

    identityService.createTenantGroupMembership("tenant", "frogs");

  }

  private Group createGroup(String id, String name, String type) {
    Group group = identityService.newGroup(id);
    group.setName(name);
    group.setType(type);
    identityService.saveGroup(group);
    return group;
  }

  @After
  public void tearDown() throws Exception {
    identityService.deleteUser("kermit");
    identityService.deleteUser("fozzie");
    identityService.deleteUser("mispiggy");

    identityService.deleteGroup("muppets");
    identityService.deleteGroup("mammals");
    identityService.deleteGroup("frogs");
    identityService.deleteGroup("admin");

    identityService.deleteTenant("tenant");


  }

  @Test
  public void testQueryById() {
    GroupQuery query = identityService.createGroupQuery().groupId("muppets");
    verifyQueryResults(query, 1);
  }

  @Test
  public void testQueryByInvalidId() {
    GroupQuery query = identityService.createGroupQuery().groupId("invalid");
    verifyQueryResults(query, 0);

    try {
      identityService.createGroupQuery().groupId(null).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  @Test
  public void testQueryByIdIn() {
    // empty list
    assertTrue(identityService.createGroupQuery().groupIdIn("a", "b").list().isEmpty());

    // collect all ids
    List<Group> list = identityService.createGroupQuery().list();
    String[] ids = new String[list.size()];
    for (int i = 0; i < ids.length; i++) {
      ids[i] = list.get(i).getId();
    }

    List<Group> idInList = identityService.createGroupQuery().groupIdIn(ids).list();
    assertEquals(list.size(), idInList.size());
    for (Group group : idInList) {
      boolean found = false;
      for (Group otherGroup : list) {
        if(otherGroup.getId().equals(group.getId())) {
          found = true; break;
        }
      }
      if(!found) {
        fail("Expected to find group " + group);
      }
    }
  }

  @Test
  public void testQueryByName() {
    GroupQuery query = identityService.createGroupQuery().groupName("Muppet show characters_");
    verifyQueryResults(query, 1);

    query = identityService.createGroupQuery().groupName("Famous frogs");
    verifyQueryResults(query, 1);
  }

  @Test
  public void testQueryByInvalidName() {
    GroupQuery query = identityService.createGroupQuery().groupName("invalid");
    verifyQueryResults(query, 0);

    try {
      identityService.createGroupQuery().groupName(null).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  @Test
  public void testQueryByNameLike() {
    GroupQuery query = identityService.createGroupQuery().groupNameLike("%Famous%");
    verifyQueryResults(query, 2);

    query = identityService.createGroupQuery().groupNameLike("Famous%");
    verifyQueryResults(query, 2);

    query = identityService.createGroupQuery().groupNameLike("%show%");
    verifyQueryResults(query, 1);

    query = identityService.createGroupQuery().groupNameLike("%ters\\_");
    verifyQueryResults(query, 1);
  }

  @Test
  public void testQueryByInvalidNameLike() {
    GroupQuery query = identityService.createGroupQuery().groupNameLike("%invalid%");
    verifyQueryResults(query, 0);

    try {
      identityService.createGroupQuery().groupNameLike(null).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  @Test
  public void testQueryByType() {
    GroupQuery query = identityService.createGroupQuery().groupType("user");
    verifyQueryResults(query, 3);

    query = identityService.createGroupQuery().groupType("admin");
    verifyQueryResults(query, 0);
  }

  @Test
  public void testQueryByInvalidType() {
    GroupQuery query = identityService.createGroupQuery().groupType("invalid");
    verifyQueryResults(query, 0);

    try {
      identityService.createGroupQuery().groupType(null).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  @Test
  public void testQueryByMember() {
    GroupQuery query = identityService.createGroupQuery().groupMember("fozzie");
    verifyQueryResults(query, 2);

    query = identityService.createGroupQuery().groupMember("kermit");
    verifyQueryResults(query, 3);

    query = query.orderByGroupId().asc();
    List<Group> groups = query.list();
    assertEquals(3, groups.size());
    assertEquals("admin", groups.get(0).getId());
    assertEquals("frogs", groups.get(1).getId());
    assertEquals("muppets", groups.get(2).getId());

    query = query.groupType("user");
    groups = query.list();
    assertEquals(2, groups.size());
    assertEquals("frogs", groups.get(0).getId());
    assertEquals("muppets", groups.get(1).getId());
  }

  @Test
  public void testQueryByInvalidMember() {
    GroupQuery query = identityService.createGroupQuery().groupMember("invalid");
    verifyQueryResults(query, 0);

    try {
      identityService.createGroupQuery().groupMember(null).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  @Test
  public void testQueryByMemberOfTenant() {
    GroupQuery query = identityService.createGroupQuery().memberOfTenant("nonExisting");
    verifyQueryResults(query, 0);

    query = identityService.createGroupQuery().memberOfTenant("tenant");
    verifyQueryResults(query, 1);

    Group group = query.singleResult();
    assertEquals("frogs", group.getId());
  }

  @Test
  public void testQuerySorting() {
    // asc
    assertEquals(4, identityService.createGroupQuery().orderByGroupId().asc().count());
    assertEquals(4, identityService.createGroupQuery().orderByGroupName().asc().count());
    assertEquals(4, identityService.createGroupQuery().orderByGroupType().asc().count());

    // desc
    assertEquals(4, identityService.createGroupQuery().orderByGroupId().desc().count());
    assertEquals(4, identityService.createGroupQuery().orderByGroupName().desc().count());
    assertEquals(4, identityService.createGroupQuery().orderByGroupType().desc().count());

    // Multiple sortings
    GroupQuery query = identityService.createGroupQuery().orderByGroupType().asc().orderByGroupName().desc();
    List<Group> groups = query.list();
    assertEquals(4, query.count());

    assertEquals("security", groups.get(0).getType());
    assertEquals("user", groups.get(1).getType());
    assertEquals("user", groups.get(2).getType());
    assertEquals("user", groups.get(3).getType());

    assertEquals("admin", groups.get(0).getId());
    assertEquals("muppets", groups.get(1).getId());
    assertEquals("mammals", groups.get(2).getId());
    assertEquals("frogs", groups.get(3).getId());
  }

  @Test
  public void testQueryInvalidSortingUsage() {
    try {
      identityService.createGroupQuery().orderByGroupId().list();
      fail();
    } catch (ProcessEngineException e) {}

    try {
      identityService.createGroupQuery().orderByGroupId().orderByGroupName().list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  private void verifyQueryResults(GroupQuery query, int countExpected) {
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

  private void verifySingleResultFails(GroupQuery query) {
    try {
      query.singleResult();
      fail();
    } catch (ProcessEngineException e) {}
  }

}
