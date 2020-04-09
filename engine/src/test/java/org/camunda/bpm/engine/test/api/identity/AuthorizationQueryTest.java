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
import static org.junit.Assert.fail;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Daniel Meyer
 *
 */
public class AuthorizationQueryTest extends PluggableProcessEngineTest {

  @Before
  public void setUp() throws Exception {


    Resource resource1 = TestResource.RESOURCE1;
    Resource resource2 = TestResource.RESOURCE2;

    createAuthorization("user1", null, resource1, "resource1-1", TestPermissions.ACCESS);
    createAuthorization("user1", null, resource2, "resource2-1", TestPermissions.DELETE);
    createAuthorization("user2", null, resource1, "resource1-2");
    createAuthorization("user3", null, resource2, "resource2-1", TestPermissions.READ, TestPermissions.UPDATE);

    createAuthorization(null, "group1", resource1, "resource1-1");
    createAuthorization(null, "group1", resource1, "resource1-2", TestPermissions.UPDATE);
    createAuthorization(null, "group2", resource2, "resource2-2", TestPermissions.READ, TestPermissions.UPDATE);
    createAuthorization(null, "group3", resource2, "resource2-3", TestPermissions.DELETE);

  }
  @After
  public void tearDown() throws Exception {
    List<Authorization> list = authorizationService.createAuthorizationQuery().list();
    for (Authorization authorization : list) {
      authorizationService.deleteAuthorization(authorization.getId());
    }

  }

  protected void createAuthorization(String userId, String groupId, Resource resourceType, String resourceId, Permission... permissions) {

    Authorization authorization = authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
    authorization.setUserId(userId);
    authorization.setGroupId(groupId);
    authorization.setResource(resourceType);
    authorization.setResourceId(resourceId);

    for (Permission permission : permissions) {
      authorization.addPermission(permission);
    }

    authorizationService.saveAuthorization(authorization);
  }

  @Test
  public void testValidQueryCounts() {

    Resource resource1 = TestResource.RESOURCE1;
    Resource resource2 = TestResource.RESOURCE2;
    Resource nonExisting = new NonExistingResource("non-existing", 102);

    // query by user id
    assertEquals(2, authorizationService.createAuthorizationQuery().userIdIn("user1").count());
    assertEquals(1, authorizationService.createAuthorizationQuery().userIdIn("user2").count());
    assertEquals(1, authorizationService.createAuthorizationQuery().userIdIn("user3").count());
    assertEquals(3, authorizationService.createAuthorizationQuery().userIdIn("user1", "user2").count());
    assertEquals(0, authorizationService.createAuthorizationQuery().userIdIn("non-existing").count());

    // query by group id
    assertEquals(2, authorizationService.createAuthorizationQuery().groupIdIn("group1").count());
    assertEquals(1, authorizationService.createAuthorizationQuery().groupIdIn("group2").count());
    assertEquals(1, authorizationService.createAuthorizationQuery().groupIdIn("group3").count());
    assertEquals(3, authorizationService.createAuthorizationQuery().groupIdIn("group1", "group2").count());
    assertEquals(0, authorizationService.createAuthorizationQuery().groupIdIn("non-existing").count());

    // query by resource type
    assertEquals(4, authorizationService.createAuthorizationQuery().resourceType(resource1).count());
    assertEquals(0, authorizationService.createAuthorizationQuery().resourceType(nonExisting).count());
    assertEquals(4, authorizationService.createAuthorizationQuery().resourceType(resource1.resourceType()).count());
    assertEquals(0, authorizationService.createAuthorizationQuery().resourceType(nonExisting.resourceType()).count());

    // query by resource id
    assertEquals(2, authorizationService.createAuthorizationQuery().resourceId("resource1-2").count());
    assertEquals(0, authorizationService.createAuthorizationQuery().resourceId("non-existing").count());

    // query by permission
    assertEquals(1, authorizationService.createAuthorizationQuery().hasPermission(TestPermissions.ACCESS).count());
    assertEquals(2, authorizationService.createAuthorizationQuery().hasPermission(TestPermissions.DELETE).count());
    assertEquals(2, authorizationService.createAuthorizationQuery().hasPermission(TestPermissions.READ).count());
    assertEquals(3, authorizationService.createAuthorizationQuery().hasPermission(TestPermissions.UPDATE).count());
    // multiple permissions at the same time
    assertEquals(2, authorizationService.createAuthorizationQuery().hasPermission(TestPermissions.READ).hasPermission(TestPermissions.UPDATE).count());
    assertEquals(2, authorizationService.createAuthorizationQuery().hasPermission(TestPermissions.UPDATE).hasPermission(TestPermissions.READ).count());
    assertEquals(0, authorizationService.createAuthorizationQuery().hasPermission(TestPermissions.READ).hasPermission(TestPermissions.ACCESS).count());

    // user id & resource type
    assertEquals(1, authorizationService.createAuthorizationQuery().userIdIn("user1").resourceType(resource1).count());
    assertEquals(0, authorizationService.createAuthorizationQuery().userIdIn("user1").resourceType(nonExisting).count());

    // group id & resource type
    assertEquals(1, authorizationService.createAuthorizationQuery().groupIdIn("group2").resourceType(resource2).count());
    assertEquals(0, authorizationService.createAuthorizationQuery().groupIdIn("group1").resourceType(nonExisting).count());
  }

  @Test
  public void testValidQueryLists() {

    Resource resource1 = TestResource.RESOURCE1;
    Resource resource2 = TestResource.RESOURCE2;
    Resource nonExisting = new NonExistingResource("non-existing", 102);

    // query by user id
    assertEquals(2, authorizationService.createAuthorizationQuery().userIdIn("user1").list().size());
    assertEquals(1, authorizationService.createAuthorizationQuery().userIdIn("user2").list().size());
    assertEquals(1, authorizationService.createAuthorizationQuery().userIdIn("user3").list().size());
    assertEquals(3, authorizationService.createAuthorizationQuery().userIdIn("user1", "user2").list().size());
    assertEquals(0, authorizationService.createAuthorizationQuery().userIdIn("non-existing").list().size());

    // query by group id
    assertEquals(2, authorizationService.createAuthorizationQuery().groupIdIn("group1").list().size());
    assertEquals(1, authorizationService.createAuthorizationQuery().groupIdIn("group2").list().size());
    assertEquals(1, authorizationService.createAuthorizationQuery().groupIdIn("group3").list().size());
    assertEquals(3, authorizationService.createAuthorizationQuery().groupIdIn("group1", "group2").list().size());
    assertEquals(0, authorizationService.createAuthorizationQuery().groupIdIn("non-existing").list().size());

    // query by resource type
    assertEquals(4, authorizationService.createAuthorizationQuery().resourceType(resource1).list().size());
    assertEquals(0, authorizationService.createAuthorizationQuery().resourceType(nonExisting).list().size());

    // query by resource id
    assertEquals(2, authorizationService.createAuthorizationQuery().resourceId("resource1-2").list().size());
    assertEquals(0, authorizationService.createAuthorizationQuery().resourceId("non-existing").list().size());

    // query by permission
    assertEquals(1, authorizationService.createAuthorizationQuery().hasPermission(TestPermissions.ACCESS).list().size());
    assertEquals(2, authorizationService.createAuthorizationQuery().hasPermission(TestPermissions.DELETE).list().size());
    assertEquals(2, authorizationService.createAuthorizationQuery().hasPermission(TestPermissions.READ).list().size());
    assertEquals(3, authorizationService.createAuthorizationQuery().hasPermission(TestPermissions.UPDATE).list().size());
    // multiple permissions at the same time
    assertEquals(2, authorizationService.createAuthorizationQuery().hasPermission(TestPermissions.READ).hasPermission(TestPermissions.UPDATE).list().size());
    assertEquals(2, authorizationService.createAuthorizationQuery().hasPermission(TestPermissions.UPDATE).hasPermission(TestPermissions.READ).list().size());
    assertEquals(0, authorizationService.createAuthorizationQuery().hasPermission(TestPermissions.READ).hasPermission(TestPermissions.ACCESS).list().size());

    // user id & resource type
    assertEquals(1, authorizationService.createAuthorizationQuery().userIdIn("user1").resourceType(resource1).list().size());
    assertEquals(0, authorizationService.createAuthorizationQuery().userIdIn("user1").resourceType(nonExisting).list().size());

    // group id & resource type
    assertEquals(1, authorizationService.createAuthorizationQuery().groupIdIn("group2").resourceType(resource2).list().size());
    assertEquals(0, authorizationService.createAuthorizationQuery().groupIdIn("group1").resourceType(nonExisting).list().size());
  }

  @Test
  public void testOrderByQueries() {

    Resource resource1 = TestResource.RESOURCE1;
    Resource resource2 = TestResource.RESOURCE2;

    List<Authorization> list = authorizationService.createAuthorizationQuery().orderByResourceType().asc().list();
    assertEquals(resource1.resourceType(), list.get(0).getResourceType());
    assertEquals(resource1.resourceType(), list.get(1).getResourceType());
    assertEquals(resource1.resourceType(), list.get(2).getResourceType());
    assertEquals(resource1.resourceType(), list.get(3).getResourceType());
    assertEquals(resource2.resourceType(), list.get(4).getResourceType());
    assertEquals(resource2.resourceType(), list.get(5).getResourceType());
    assertEquals(resource2.resourceType(), list.get(6).getResourceType());
    assertEquals(resource2.resourceType(), list.get(7).getResourceType());

    list = authorizationService.createAuthorizationQuery().orderByResourceType().desc().list();
    assertEquals(resource2.resourceType(), list.get(0).getResourceType());
    assertEquals(resource2.resourceType(), list.get(1).getResourceType());
    assertEquals(resource2.resourceType(), list.get(2).getResourceType());
    assertEquals(resource2.resourceType(), list.get(3).getResourceType());
    assertEquals(resource1.resourceType(), list.get(4).getResourceType());
    assertEquals(resource1.resourceType(), list.get(5).getResourceType());
    assertEquals(resource1.resourceType(), list.get(6).getResourceType());
    assertEquals(resource1.resourceType(), list.get(7).getResourceType());

    list = authorizationService.createAuthorizationQuery().orderByResourceId().asc().list();
    assertEquals("resource1-1", list.get(0).getResourceId());
    assertEquals("resource1-1", list.get(1).getResourceId());
    assertEquals("resource1-2", list.get(2).getResourceId());
    assertEquals("resource1-2", list.get(3).getResourceId());
    assertEquals("resource2-1", list.get(4).getResourceId());
    assertEquals("resource2-1", list.get(5).getResourceId());
    assertEquals("resource2-2", list.get(6).getResourceId());
    assertEquals("resource2-3", list.get(7).getResourceId());

    list = authorizationService.createAuthorizationQuery().orderByResourceId().desc().list();
    assertEquals("resource2-3", list.get(0).getResourceId());
    assertEquals("resource2-2", list.get(1).getResourceId());
    assertEquals("resource2-1", list.get(2).getResourceId());
    assertEquals("resource2-1", list.get(3).getResourceId());
    assertEquals("resource1-2", list.get(4).getResourceId());
    assertEquals("resource1-2", list.get(5).getResourceId());
    assertEquals("resource1-1", list.get(6).getResourceId());
    assertEquals("resource1-1", list.get(7).getResourceId());

  }

  @Test
  public void testInvalidOrderByQueries() {
    try {
      authorizationService.createAuthorizationQuery().orderByResourceType().list();
      fail("Exception expected");
    } catch(ProcessEngineException e) {
      testRule.assertTextPresent("Invalid query: call asc() or desc() after using orderByXX()", e.getMessage());
    }

    try {
      authorizationService.createAuthorizationQuery().orderByResourceId().list();
      fail("Exception expected");
    } catch(ProcessEngineException e) {
      testRule.assertTextPresent("Invalid query: call asc() or desc() after using orderByXX()", e.getMessage());
    }

    try {
      authorizationService.createAuthorizationQuery().orderByResourceId().orderByResourceType().list();
      fail("Exception expected");
    } catch(ProcessEngineException e) {
      testRule.assertTextPresent("Invalid query: call asc() or desc() after using orderByXX()", e.getMessage());
    }

    try {
      authorizationService.createAuthorizationQuery().orderByResourceType().orderByResourceId().list();
      fail("Exception expected");
    } catch(ProcessEngineException e) {
      testRule.assertTextPresent("Invalid query: call asc() or desc() after using orderByXX()", e.getMessage());
    }
  }

  @Test
  public void testInvalidQueries() {

    // cannot query for user id and group id at the same time

    try {
      authorizationService.createAuthorizationQuery().groupIdIn("a").userIdIn("b").count();
    } catch(ProcessEngineException e) {
      testRule.assertTextPresent("Cannot query for user and group authorizations at the same time.", e.getMessage());
    }

    try {
      authorizationService.createAuthorizationQuery().userIdIn("b").groupIdIn("a").count();
    } catch(ProcessEngineException e) {
      testRule.assertTextPresent("Cannot query for user and group authorizations at the same time.", e.getMessage());
    }

  }

  class NonExistingResource implements Resource {

    protected int id;
    protected String name;

    public NonExistingResource(String name, int id) {
      this.name = name;
      this.id = id;
    }

    public String resourceName() {
      return name;
    }

    public int resourceType() {
      return id;
    }

  }

}
