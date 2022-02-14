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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertTrue;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Daniel Meyer
 *
 */
public class ReadOnlyIdentityServiceTest {

  protected static final String CONFIGURATION_RESOURCE = "org/camunda/bpm/engine/test/api/identity/read.only.identity.service.camunda.cfg.xml";

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(CONFIGURATION_RESOURCE);

  @Rule
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);

  protected IdentityService identityService;

  @Before
  public void setUp() {
    identityService = engineRule.getIdentityService();

    assertTrue(identityService.isReadOnly());
  }

  @Test
  public void newUser() {
    // when/then
    assertThatThrownBy(() -> identityService.newUser("user"))
      .isInstanceOf(UnsupportedOperationException.class)
      .hasMessageContaining("This identity service implementation is read-only.");
  }

  @Test
  public void saveUser() {
    // when/then
    assertThatThrownBy(() -> identityService.saveUser(null))
      .isInstanceOf(UnsupportedOperationException.class)
      .hasMessageContaining("This identity service implementation is read-only.");
  }

  @Test
  public void deleteUser() {
    // when/then
    assertThatThrownBy(() -> identityService.deleteUser("user"))
      .isInstanceOf(UnsupportedOperationException.class)
      .hasMessageContaining("This identity service implementation is read-only.");
  }

  @Test
  public void newGroup() {
    // when/then
    assertThatThrownBy(() -> identityService.newGroup("group"))
      .isInstanceOf(UnsupportedOperationException.class)
      .hasMessageContaining("This identity service implementation is read-only.");
  }

  @Test
  public void saveGroup() {
    // when/then
    assertThatThrownBy(() -> identityService.saveGroup(null))
      .isInstanceOf(UnsupportedOperationException.class)
      .hasMessageContaining("This identity service implementation is read-only.");
  }

  @Test
  public void deleteGroup() {
    // when/then
    assertThatThrownBy(() -> identityService.deleteGroup("group"))
      .isInstanceOf(UnsupportedOperationException.class)
      .hasMessageContaining("This identity service implementation is read-only.");
  }

  @Test
  public void newTenant() {
    // when/then
    assertThatThrownBy(() -> identityService.newTenant("tenant"))
      .isInstanceOf(UnsupportedOperationException.class)
      .hasMessageContaining("This identity service implementation is read-only.");
  }

  @Test
  public void saveTenant() {
    // when/then
    assertThatThrownBy(() -> identityService.saveTenant(null))
      .isInstanceOf(UnsupportedOperationException.class)
      .hasMessageContaining("This identity service implementation is read-only.");
  }

  @Test
  public void deleteTenant() {
    // when/then
    assertThatThrownBy(() -> identityService.deleteTenant("tenant"))
      .isInstanceOf(UnsupportedOperationException.class)
      .hasMessageContaining("This identity service implementation is read-only.");
  }

  @Test
  public void createGroupMembership() {
    // when/then
    assertThatThrownBy(() -> identityService.createMembership("user", "group"))
      .isInstanceOf(UnsupportedOperationException.class)
      .hasMessageContaining("This identity service implementation is read-only.");
  }

  @Test
  public void deleteGroupMembership() {
    // when/then
    assertThatThrownBy(() -> identityService.deleteMembership("user", "group"))
      .isInstanceOf(UnsupportedOperationException.class)
      .hasMessageContaining("This identity service implementation is read-only.");
  }

  @Test
  public void createTenantUserMembership() {
    // when/then
    assertThatThrownBy(() -> identityService.createTenantUserMembership("tenant", "user"))
      .isInstanceOf(UnsupportedOperationException.class)
      .hasMessageContaining("This identity service implementation is read-only.");
  }

  @Test
  public void createTenantGroupMembership() {
    // when/then
    assertThatThrownBy(() -> identityService.createTenantGroupMembership("tenant", "group"))
      .isInstanceOf(UnsupportedOperationException.class)
      .hasMessageContaining("This identity service implementation is read-only.");
  }

  @Test
  public void deleteTenantUserMembership() {
    // when/then
    assertThatThrownBy(() -> identityService.deleteTenantUserMembership("tenant", "user"))
      .isInstanceOf(UnsupportedOperationException.class)
      .hasMessageContaining("This identity service implementation is read-only.");
  }

  @Test
  public void deleteTenantGroupMembership() {
    // when/then
    assertThatThrownBy(() -> identityService.deleteTenantGroupMembership("tenant", "group"))
      .isInstanceOf(UnsupportedOperationException.class)
      .hasMessageContaining("This identity service implementation is read-only.");
  }

  @Test
  public void checkPassword() {
    identityService.checkPassword("user", "password");
  }

  @Test
  public void createQuery() {
    identityService.createUserQuery().list();
    identityService.createGroupQuery().list();
    identityService.createTenantQuery().list();
  }

}
