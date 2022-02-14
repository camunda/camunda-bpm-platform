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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.authorization.Authorization;
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

public class IdentityServiceTenantTest {

  protected static final String USER_ONE = "user1";
  protected static final String USER_TWO = "user2";

  protected static final String GROUP_ONE = "group1";
  protected static final String GROUP_TWO = "group2";

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  private final String INVALID_ID_MESSAGE = "%s has an invalid id: '%s' is not a valid resource identifier.";

  @Rule
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected IdentityService identityService;
  protected ProcessEngine processEngine;

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

    if (processEngine != null) {
      for (Tenant deleteTenant : processEngine.getIdentityService().createTenantQuery().list()) {
        processEngine.getIdentityService().deleteTenant(deleteTenant.getId());
      }
      for (Authorization authorization : processEngine.getAuthorizationService().createAuthorizationQuery().list()) {
        processEngine.getAuthorizationService().deleteAuthorization(authorization.getId());
      }

      processEngine.close();
      ProcessEngines.unregister(processEngine);
      processEngine = null;
    }
  }

  @Test
  public void createTenant() {
    Tenant tenant = identityService.newTenant(TENANT_ONE);
    tenant.setName("Tenant");
    identityService.saveTenant(tenant);

    tenant = identityService.createTenantQuery().singleResult();
    assertThat(tenant).isNotNull();
    assertThat(tenant.getId()).isEqualTo(TENANT_ONE);
    assertThat(tenant.getName()).isEqualTo("Tenant");
  }

  @Test
  public void createExistingTenant() {
    Tenant tenant = identityService.newTenant(TENANT_ONE);
    tenant.setName("Tenant");
    identityService.saveTenant(tenant);

    Tenant secondTenant = identityService.newTenant(TENANT_ONE);
    secondTenant.setName("Tenant");
    try {
      identityService.saveTenant(secondTenant);
      fail("BadUserRequestException is expected");
    } catch (Exception ex) {
      if (!(ex instanceof BadUserRequestException)) {
        fail("BadUserRequestException is expected, but another exception was received:  " + ex);
      }
      assertEquals("The tenant already exists", ex.getMessage());
    }
  }

  @Test
  public void updateTenant() {
    // create
    Tenant tenant = identityService.newTenant(TENANT_ONE);
    tenant.setName("Tenant");
    identityService.saveTenant(tenant);

    // update
    tenant = identityService.createTenantQuery().singleResult();
    assertThat(tenant).isNotNull();

    tenant.setName("newName");
    identityService.saveTenant(tenant);

    tenant = identityService.createTenantQuery().singleResult();
    assertEquals("newName", tenant.getName());
  }

  @Test
  public void testInvalidTenantId() {
    String invalidId = "john's tenant";
    Tenant tenant = identityService.newTenant(invalidId);
    try {
      identityService.saveTenant(tenant);
      fail("Invalid tenant id exception expected!");
    } catch (ProcessEngineException ex) {
      assertEquals(String.format(INVALID_ID_MESSAGE, "Tenant", invalidId), ex.getMessage());
    }
  }

  @Test
  public void testInvalidTenantIdOnUpdate() {
    String invalidId = "john's tenant";
    try {
      Tenant updatedTenant = identityService.newTenant("john");
      updatedTenant.setId(invalidId);
      identityService.saveTenant(updatedTenant);

      fail("Invalid tenant id exception expected!");
    } catch (ProcessEngineException ex) {
      assertEquals(String.format(INVALID_ID_MESSAGE, "Tenant", invalidId), ex.getMessage());
    }
  }

  @Test
  public void testCustomCreateTenantWhitelistPattern() {
    processEngine = ProcessEngineConfiguration
      .createProcessEngineConfigurationFromResource("org/camunda/bpm/engine/test/api/identity/generic.resource.id.whitelist.camunda.cfg.xml")
      .buildProcessEngine();
    processEngine.getProcessEngineConfiguration().setTenantResourceWhitelistPattern("[a-zA-Z]+");

    String invalidId = "john's tenant";

    Tenant tenant = processEngine.getIdentityService().newTenant(invalidId);
    try {
      processEngine.getIdentityService().saveTenant(tenant);
      fail("Invalid tenant id exception expected!");
    } catch (ProcessEngineException ex) {
      assertEquals(String.format(INVALID_ID_MESSAGE, "Tenant", invalidId), ex.getMessage());
    }
  }

  @Test
  public void testCustomTenantWhitelistPattern() {
    processEngine = ProcessEngineConfiguration
      .createProcessEngineConfigurationFromResource("org/camunda/bpm/engine/test/api/identity/generic.resource.id.whitelist.camunda.cfg.xml")
      .buildProcessEngine();
    processEngine.getProcessEngineConfiguration().setTenantResourceWhitelistPattern("[a-zA-Z]+");

    String validId = "johnsTenant";
    String invalidId = "john!@#$%";

    try {
      Tenant tenant = processEngine.getIdentityService().newTenant(validId);
      tenant.setId(invalidId);
      processEngine.getIdentityService().saveTenant(tenant);

      fail("Invalid tenant id exception expected!");
    } catch (ProcessEngineException ex) {
      assertEquals(String.format(INVALID_ID_MESSAGE, "Tenant", invalidId), ex.getMessage());
    }
  }

  @Test
  public void deleteTenant() {
    // create
    Tenant tenant = identityService.newTenant(TENANT_ONE);
    identityService.saveTenant(tenant);

    TenantQuery query = identityService.createTenantQuery();
    assertThat(query.count()).isEqualTo(1L);

    identityService.deleteTenant("nonExisting");
    assertThat(query.count()).isEqualTo(1L);

    identityService.deleteTenant(TENANT_ONE);
    assertThat(query.count()).isEqualTo(0L);
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

    // fail to update old revision
    tenant2.setName("other name");

    assertThatThrownBy(() -> identityService.saveTenant(tenant2))
      .isInstanceOf(ProcessEngineException.class);
  }

  @Test
  public void createTenantWithGenericResourceId() {
    processEngine = ProcessEngineConfiguration
      .createProcessEngineConfigurationFromResource("org/camunda/bpm/engine/test/api/identity/generic.resource.id.whitelist.camunda.cfg.xml")
      .buildProcessEngine();

    Tenant tenant = processEngine.getIdentityService().newTenant("*");

    assertThatThrownBy(() -> processEngine.getIdentityService().saveTenant(tenant))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("has an invalid id: id cannot be *. * is a reserved identifier.");
  }

  @Test
  public void createTenantMembershipUnexistingTenant() {
    User user = identityService.newUser(USER_ONE);
    identityService.saveUser(user);

    assertThatThrownBy(() -> identityService.createTenantUserMembership("nonExisting", user.getId()))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("No tenant found with id 'nonExisting'.");
  }

  @Test
  public void createTenantMembershipUnexistingUser() {
    Tenant tenant = identityService.newTenant(TENANT_ONE);
    identityService.saveTenant(tenant);

    assertThatThrownBy(() -> identityService.createTenantUserMembership(tenant.getId(), "nonExisting"))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("No user found with id 'nonExisting'.");
  }

  @Test
  public void createTenantMembershipUnexistingGroup() {
    Tenant tenant = identityService.newTenant(TENANT_ONE);
    identityService.saveTenant(tenant);

    assertThatThrownBy(() -> identityService.createTenantGroupMembership(tenant.getId(), "nonExisting"))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("No group found with id 'nonExisting'.");

  }

  @Test
  public void createTenantUserMembershipAlreadyExisting() {
    Tenant tenant = identityService.newTenant(TENANT_ONE);
    identityService.saveTenant(tenant);

    User user = identityService.newUser(USER_ONE);
    identityService.saveUser(user);

    identityService.createTenantUserMembership(TENANT_ONE, USER_ONE);

    assertThatThrownBy(() -> identityService.createTenantUserMembership(TENANT_ONE, USER_ONE))
      .isInstanceOf(ProcessEngineException.class);
  }

  @Test
  public void createTenantGroupMembershipAlreadyExisting() {
    Tenant tenant = identityService.newTenant(TENANT_ONE);
    identityService.saveTenant(tenant);

    Group group = identityService.newGroup(GROUP_ONE);
    identityService.saveGroup(group);

    identityService.createTenantGroupMembership(TENANT_ONE, GROUP_ONE);

    assertThatThrownBy(() -> identityService.createTenantGroupMembership(TENANT_ONE, GROUP_ONE))
      .isInstanceOf(ProcessEngineException.class);
  }

  @Test
  public void deleteTenantUserMembership() {
    Tenant tenant = identityService.newTenant(TENANT_ONE);
    identityService.saveTenant(tenant);

    User user = identityService.newUser(USER_ONE);
    identityService.saveUser(user);

    identityService.createTenantUserMembership(TENANT_ONE, USER_ONE);

    TenantQuery query = identityService.createTenantQuery().userMember(USER_ONE);
    assertThat(query.count()).isEqualTo(1L);

    identityService.deleteTenantUserMembership("nonExisting", USER_ONE);
    assertThat(query.count()).isEqualTo(1L);

    identityService.deleteTenantUserMembership(TENANT_ONE, "nonExisting");
    assertThat(query.count()).isEqualTo(1L);

    identityService.deleteTenantUserMembership(TENANT_ONE, USER_ONE);
    assertThat(query.count()).isEqualTo(0L);
  }

  @Test
  public void deleteTenantGroupMembership() {
    Tenant tenant = identityService.newTenant(TENANT_ONE);
    identityService.saveTenant(tenant);

    Group group = identityService.newGroup(GROUP_ONE);
    identityService.saveGroup(group);

    identityService.createTenantGroupMembership(TENANT_ONE, GROUP_ONE);

    TenantQuery query = identityService.createTenantQuery().groupMember(GROUP_ONE);
    assertThat(query.count()).isEqualTo(1L);

    identityService.deleteTenantGroupMembership("nonExisting", GROUP_ONE);
    assertThat(query.count()).isEqualTo(1L);

    identityService.deleteTenantGroupMembership(TENANT_ONE, "nonExisting");
    assertThat(query.count()).isEqualTo(1L);

    identityService.deleteTenantGroupMembership(TENANT_ONE, GROUP_ONE);
    assertThat(query.count()).isEqualTo(0L);
  }

  @Test
  public void deleteTenantMembershipsWileDeleteUser() {
    Tenant tenant = identityService.newTenant(TENANT_ONE);
    identityService.saveTenant(tenant);

    User user = identityService.newUser(USER_ONE);
    identityService.saveUser(user);

    identityService.createTenantUserMembership(TENANT_ONE, USER_ONE);

    TenantQuery query = identityService.createTenantQuery().userMember(USER_ONE);
    assertThat(query.count()).isEqualTo(1L);

    identityService.deleteUser(USER_ONE);
    assertThat(query.count()).isEqualTo(0L);
  }

  @Test
  public void deleteTenantMembershipsWhileDeleteGroup() {
    Tenant tenant = identityService.newTenant(TENANT_ONE);
    identityService.saveTenant(tenant);

    Group group = identityService.newGroup(GROUP_ONE);
    identityService.saveGroup(group);

    identityService.createTenantGroupMembership(TENANT_ONE, GROUP_ONE);

    TenantQuery query = identityService.createTenantQuery().groupMember(GROUP_ONE);
    assertThat(query.count()).isEqualTo(1L);

    identityService.deleteGroup(GROUP_ONE);
    assertThat(query.count()).isEqualTo(0L);
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
    assertThat(userQuery.count()).isEqualTo(1L);
    assertThat(groupQuery.count()).isEqualTo(1L);

    identityService.deleteTenant(TENANT_ONE);
    assertThat(userQuery.count()).isEqualTo(0L);
    assertThat(groupQuery.count()).isEqualTo(0L);
  }

}
