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

import static org.junit.Assert.assertTrue;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  protected IdentityService identityService;

  @Before
  public void setUp() {
    identityService = engineRule.getIdentityService();

    assertTrue(identityService.isReadOnly());
  }

  @Test
  public void newUser() {
    thrown.expect(UnsupportedOperationException.class);
    thrown.expectMessage("This identity service implementation is read-only.");

    identityService.newUser("user");
  }

  @Test
  public void saveUser() {
    thrown.expect(UnsupportedOperationException.class);
    thrown.expectMessage("This identity service implementation is read-only.");

    identityService.saveUser(null);
  }

  @Test
  public void deleteUser() {
    thrown.expect(UnsupportedOperationException.class);
    thrown.expectMessage("This identity service implementation is read-only.");

    identityService.deleteUser("user");
  }

  @Test
  public void newGroup() {
    thrown.expect(UnsupportedOperationException.class);
    thrown.expectMessage("This identity service implementation is read-only.");

    identityService.newGroup("group");
  }

  @Test
  public void saveGroup() {
    thrown.expect(UnsupportedOperationException.class);
    thrown.expectMessage("This identity service implementation is read-only.");

    identityService.saveGroup(null);
  }

  @Test
  public void deleteGroup() {
    thrown.expect(UnsupportedOperationException.class);
    thrown.expectMessage("This identity service implementation is read-only.");

    identityService.deleteGroup("group");
  }

  @Test
  public void newTenant() {
    thrown.expect(UnsupportedOperationException.class);
    thrown.expectMessage("This identity service implementation is read-only.");

    identityService.newTenant("tenant");
  }

  @Test
  public void saveTenant() {
    thrown.expect(UnsupportedOperationException.class);
    thrown.expectMessage("This identity service implementation is read-only.");

    identityService.saveTenant(null);
  }

  @Test
  public void deleteTenant() {
    thrown.expect(UnsupportedOperationException.class);
    thrown.expectMessage("This identity service implementation is read-only.");

    identityService.deleteTenant("tenant");
  }

  @Test
  public void createGroupMembership() {
    thrown.expect(UnsupportedOperationException.class);
    thrown.expectMessage("This identity service implementation is read-only.");

    identityService.createMembership("user", "group");
  }

  @Test
  public void deleteGroupMembership() {
    thrown.expect(UnsupportedOperationException.class);
    thrown.expectMessage("This identity service implementation is read-only.");

    identityService.deleteMembership("user", "group");
  }

  @Test
  public void createTenantUserMembership() {
    thrown.expect(UnsupportedOperationException.class);
    thrown.expectMessage("This identity service implementation is read-only.");

    identityService.createTenantUserMembership("tenant", "user");
  }

  @Test
  public void createTenantGroupMembership() {
    thrown.expect(UnsupportedOperationException.class);
    thrown.expectMessage("This identity service implementation is read-only.");

    identityService.createTenantGroupMembership("tenant", "group");
  }

  @Test
  public void deleteTenantUserMembership() {
    thrown.expect(UnsupportedOperationException.class);
    thrown.expectMessage("This identity service implementation is read-only.");

    identityService.deleteTenantUserMembership("tenant", "user");
  }

  @Test
  public void deleteTenantGroupMembership() {
    thrown.expect(UnsupportedOperationException.class);
    thrown.expectMessage("This identity service implementation is read-only.");

    identityService.deleteTenantGroupMembership("tenant", "group");
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
