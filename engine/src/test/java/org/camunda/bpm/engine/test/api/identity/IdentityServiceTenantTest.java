/* Licensed under the Apache License, Version 2.0 (the "License");
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

package org.camunda.bpm.engine.test.api.identity;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.identity.TenantQuery;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.identity.UserQuery;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class IdentityServiceTenantTest {

  protected static final String USER_ONE = "user1";
  protected static final String USER_TWO = "user2";

  protected static final String GROUP_ONE = "group1";
  protected static final String GROUP_TWO = "group2";

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  @Rule
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  protected IdentityService identityService;

  @Before
  public void initService() {
    identityService = engineRule.getIdentityService();
  }

  @After
  public void cleanUp() {
    identityService.deleteTenant(TENANT_ONE);
    identityService.deleteTenant(TENANT_TWO);

    identityService.deleteGroup(GROUP_ONE);
    identityService.deleteGroup(GROUP_TWO);

    identityService.deleteUser(USER_ONE);
    identityService.deleteUser(USER_TWO);
  }

  @Test
  public void createTenant() {
    Tenant tenant = identityService.newTenant(TENANT_ONE);
    tenant.setName("Tenant");
    identityService.saveTenant(tenant);

    tenant = identityService.createTenantQuery().singleResult();
    assertThat(tenant, is(notNullValue()));
    assertThat(tenant.getId(), is(TENANT_ONE));
    assertThat(tenant.getName(), is("Tenant"));
  }

  @Test
  public void updateTenant() {
    // create
    Tenant tenant = identityService.newTenant(TENANT_ONE);
    tenant.setName("Tenant");
    identityService.saveTenant(tenant);

    // update
    tenant = identityService.createTenantQuery().singleResult();
    assertThat(tenant, is(notNullValue()));

    tenant.setName("newName");
    identityService.saveTenant(tenant);

    tenant = identityService.createTenantQuery().singleResult();
    assertEquals("newName", tenant.getName());
  }

  @Test
  public void deleteTenant() {
    // create
    Tenant tenant = identityService.newTenant(TENANT_ONE);
    identityService.saveTenant(tenant);

    TenantQuery query = identityService.createTenantQuery();
    assertThat(query.count(), is(1L));

    identityService.deleteTenant("nonExisting");
    assertThat(query.count(), is(1L));

    identityService.deleteTenant(TENANT_ONE);
    assertThat(query.count(), is(0L));
  }

  @Test
  public void updateTenantOptimisticLockingException() {
    // create
    Tenant tenant = identityService.newTenant(TENANT_ONE);
    identityService.saveTenant(tenant);

    Tenant tenant1 = identityService.createTenantQuery().singleResult();
    Tenant tenant2 = identityService.createTenantQuery().singleResult();

    // update
    tenant1.setName("name");
    identityService.saveTenant(tenant1);

    thrown.expect(ProcessEngineException.class);

    // fail to update old revision
    tenant2.setName("other name");
    identityService.saveTenant(tenant2);
  }

  @Test
  public void createTenantWithGenericResourceId() {
    Tenant tenant = identityService.newTenant("*");

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("has an invalid id: id cannot be *. * is a reserved identifier.");

    identityService.saveTenant(tenant);
  }

  @Test
  public void createTenantMembershipUnexistingTenant() {
    User user = identityService.newUser(USER_ONE);
    identityService.saveUser(user);

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("No tenant found with id 'nonExisting'.");

    identityService.createTenantUserMembership("nonExisting", user.getId());
  }

  @Test
  public void createTenantMembershipUnexistingUser() {
    Tenant tenant = identityService.newTenant(TENANT_ONE);
    identityService.saveTenant(tenant);

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("No user found with id 'nonExisting'.");

    identityService.createTenantUserMembership(tenant.getId(), "nonExisting");
  }

  @Test
  public void createTenantMembershipUnexistingGroup() {
    Tenant tenant = identityService.newTenant(TENANT_ONE);
    identityService.saveTenant(tenant);

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("No group found with id 'nonExisting'.");

    identityService.createTenantGroupMembership(tenant.getId(), "nonExisting");
  }

  @Test
  public void createTenantUserMembershipAlreadyExisting() {
    Tenant tenant = identityService.newTenant(TENANT_ONE);
    identityService.saveTenant(tenant);

    User user = identityService.newUser(USER_ONE);
    identityService.saveUser(user);

    identityService.createTenantUserMembership(TENANT_ONE, USER_ONE);

    thrown.expect(ProcessEngineException.class);

    identityService.createTenantUserMembership(TENANT_ONE, USER_ONE);
  }

  @Test
  public void createTenantGroupMembershipAlreadyExisting() {
    Tenant tenant = identityService.newTenant(TENANT_ONE);
    identityService.saveTenant(tenant);

    Group group = identityService.newGroup(GROUP_ONE);
    identityService.saveGroup(group);

    identityService.createTenantGroupMembership(TENANT_ONE, GROUP_ONE);

    thrown.expect(ProcessEngineException.class);

    identityService.createTenantGroupMembership(TENANT_ONE, GROUP_ONE);
  }

  @Test
  public void deleteTenantUserMembership() {
    Tenant tenant = identityService.newTenant(TENANT_ONE);
    identityService.saveTenant(tenant);

    User user = identityService.newUser(USER_ONE);
    identityService.saveUser(user);

    identityService.createTenantUserMembership(TENANT_ONE, USER_ONE);

    TenantQuery query = identityService.createTenantQuery().userMember(USER_ONE);
    assertThat(query.count(), is(1L));

    identityService.deleteTenantUserMembership("nonExisting", USER_ONE);
    assertThat(query.count(), is(1L));

    identityService.deleteTenantUserMembership(TENANT_ONE, "nonExisting");
    assertThat(query.count(), is(1L));

    identityService.deleteTenantUserMembership(TENANT_ONE, USER_ONE);
    assertThat(query.count(), is(0L));
  }

  @Test
  public void deleteTenantGroupMembership() {
    Tenant tenant = identityService.newTenant(TENANT_ONE);
    identityService.saveTenant(tenant);

    Group group = identityService.newGroup(GROUP_ONE);
    identityService.saveGroup(group);

    identityService.createTenantGroupMembership(TENANT_ONE, GROUP_ONE);

    TenantQuery query = identityService.createTenantQuery().groupMember(GROUP_ONE);
    assertThat(query.count(), is(1L));

    identityService.deleteTenantGroupMembership("nonExisting", GROUP_ONE);
    assertThat(query.count(), is(1L));

    identityService.deleteTenantGroupMembership(TENANT_ONE, "nonExisting");
    assertThat(query.count(), is(1L));

    identityService.deleteTenantGroupMembership(TENANT_ONE, GROUP_ONE);
    assertThat(query.count(), is(0L));
  }

  @Test
  public void deleteTenantMembershipsWileDeleteUser() {
    Tenant tenant = identityService.newTenant(TENANT_ONE);
    identityService.saveTenant(tenant);

    User user = identityService.newUser(USER_ONE);
    identityService.saveUser(user);

    identityService.createTenantUserMembership(TENANT_ONE, USER_ONE);

    TenantQuery query = identityService.createTenantQuery().userMember(USER_ONE);
    assertThat(query.count(), is(1L));

    identityService.deleteUser(USER_ONE);
    assertThat(query.count(), is(0L));
  }

  @Test
  public void deleteTenantMembershipsWhileDeleteGroup() {
    Tenant tenant = identityService.newTenant(TENANT_ONE);
    identityService.saveTenant(tenant);

    Group group = identityService.newGroup(GROUP_ONE);
    identityService.saveGroup(group);

    identityService.createTenantGroupMembership(TENANT_ONE, GROUP_ONE);

    TenantQuery query = identityService.createTenantQuery().groupMember(GROUP_ONE);
    assertThat(query.count(), is(1L));

    identityService.deleteGroup(GROUP_ONE);
    assertThat(query.count(), is(0L));
  }

  @Test
  public void deleteTenantMembershipsOfTenant() {
    Tenant tenant = identityService.newTenant(TENANT_ONE);
    identityService.saveTenant(tenant);

    User user = identityService.newUser(USER_ONE);
    identityService.saveUser(user);

    Group group = identityService.newGroup(GROUP_ONE);
    identityService.saveGroup(group);

    identityService.createTenantUserMembership(TENANT_ONE, USER_ONE);
    identityService.createTenantGroupMembership(TENANT_ONE, GROUP_ONE);

    UserQuery userQuery = identityService.createUserQuery().memberOfTenant(TENANT_ONE);
    GroupQuery groupQuery = identityService.createGroupQuery().memberOfTenant(TENANT_ONE);
    assertThat(userQuery.count(), is(1L));
    assertThat(groupQuery.count(), is(1L));

    identityService.deleteTenant(TENANT_ONE);
    assertThat(userQuery.count(), is(0L));
    assertThat(groupQuery.count(), is(0L));
  }

}
