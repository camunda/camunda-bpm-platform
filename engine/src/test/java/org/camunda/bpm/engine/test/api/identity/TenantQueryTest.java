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

import java.util.List;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.identity.TenantQuery;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class TenantQueryTest {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  protected static final String USER = "user";
  protected static final String GROUP = "group";

  @Rule
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected IdentityService identityService;

  @Before
  public void setUp() {
    identityService = engineRule.getIdentityService();

    createTenant(TENANT_ONE, "Tenant_1");
    createTenant(TENANT_TWO, "Tenant_2");

    User user = identityService.newUser(USER);
    identityService.saveUser(user);

    Group group = identityService.newGroup(GROUP);
    identityService.saveGroup(group);

    identityService.createMembership(USER, GROUP);

    identityService.createTenantUserMembership(TENANT_ONE, USER);
    identityService.createTenantGroupMembership(TENANT_TWO, GROUP);
  }

  @Test
  public void queryById() {
    TenantQuery query = identityService.createTenantQuery().tenantId(TENANT_ONE);

    assertThat(query.count()).isEqualTo(1L);
    assertThat(query.list()).hasSize(1);

    Tenant tenant = query.singleResult();
    assertThat(tenant).isNotNull();
    assertThat(tenant.getName()).isEqualTo("Tenant_1");
  }

  @Test
  public void queryByNonExistingId() {
    TenantQuery query = identityService.createTenantQuery().tenantId("nonExisting");

    assertThat(query.count()).isEqualTo(0L);
  }

  @Test
  public void queryByIdIn() {
    TenantQuery query = identityService.createTenantQuery();

    assertThat(query.tenantIdIn("non", "existing").count()).isEqualTo(0L);
    assertThat(query.tenantIdIn(TENANT_ONE, TENANT_TWO).count()).isEqualTo(2L);
  }

  @Test
  public void queryByName() {
    TenantQuery query = identityService.createTenantQuery();

    assertThat(query.tenantName("nonExisting").count()).isEqualTo(0L);
    assertThat(query.tenantName("Tenant_1").count()).isEqualTo(1L);
    assertThat(query.tenantName("Tenant_2").count()).isEqualTo(1L);
  }

  @Test
  public void queryByNameLike() {
    TenantQuery query = identityService.createTenantQuery();

    assertThat(query.tenantNameLike("%nonExisting%").count()).isEqualTo(0L);
    assertThat(query.tenantNameLike("%Tenant\\_1%").count()).isEqualTo(1L);
    assertThat(query.tenantNameLike("%Tenant%").count()).isEqualTo(2L);
  }

  @Test
  public void queryByUser() {
    TenantQuery query = identityService.createTenantQuery();

    assertThat(query.userMember("nonExisting").count()).isEqualTo(0L);
    assertThat(query.userMember(USER).count()).isEqualTo(1L);
    assertThat(query.userMember(USER).tenantId(TENANT_ONE).count()).isEqualTo(1L);
  }

  @Test
  public void queryByGroup() {
    TenantQuery query = identityService.createTenantQuery();

    assertThat(query.groupMember("nonExisting").count()).isEqualTo(0L);
    assertThat(query.groupMember(GROUP).count()).isEqualTo(1L);
    assertThat(query.groupMember(GROUP).tenantId(TENANT_TWO).count()).isEqualTo(1L);
  }

  @Test
  public void queryByUserIncludingGroups() {
    TenantQuery query = identityService.createTenantQuery().userMember(USER);

    assertThat(query.includingGroupsOfUser(false).count()).isEqualTo(1L);
    assertThat(query.includingGroupsOfUser(true).count()).isEqualTo(2L);
  }

  @Test
  public void queryOrderById() {
    // ascending
    List<Tenant> tenants = identityService.createTenantQuery().orderByTenantId().asc().list();
    assertThat(tenants.size()).isEqualTo(2);

    assertThat(tenants.get(0).getId()).isEqualTo(TENANT_ONE);
    assertThat(tenants.get(1).getId()).isEqualTo(TENANT_TWO);

    // descending
    tenants = identityService.createTenantQuery().orderByTenantId().desc().list();

    assertThat(tenants.get(0).getId()).isEqualTo(TENANT_TWO);
    assertThat(tenants.get(1).getId()).isEqualTo(TENANT_ONE);
  }

  @Test
  public void queryOrderByName() {
    // ascending
    List<Tenant> tenants = identityService.createTenantQuery().orderByTenantName().asc().list();
    assertThat(tenants.size()).isEqualTo(2);

    assertThat(tenants.get(0).getName()).isEqualTo("Tenant_1");
    assertThat(tenants.get(1).getName()).isEqualTo("Tenant_2");

    // descending
    tenants = identityService.createTenantQuery().orderByTenantName().desc().list();

    assertThat(tenants.get(0).getName()).isEqualTo("Tenant_2");
    assertThat(tenants.get(1).getName()).isEqualTo("Tenant_1");
  }

  protected Tenant createTenant(String id, String name) {
    Tenant tenant = engineRule.getIdentityService().newTenant(id);
    tenant.setName(name);
    identityService.saveTenant(tenant);

    return tenant;
  }

  @After
  public void tearDown() throws Exception {
    identityService.deleteTenant(TENANT_ONE);
    identityService.deleteTenant(TENANT_TWO);

    identityService.deleteUser(USER);
    identityService.deleteGroup(GROUP);
  }

}
