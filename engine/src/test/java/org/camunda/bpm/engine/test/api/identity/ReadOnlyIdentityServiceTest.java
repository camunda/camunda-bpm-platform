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

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.impl.persistence.entity.TenantEntity;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Daniel Meyer
 *
 */
public class ReadOnlyIdentityServiceTest {

  protected static final String CONFIGURATION_RESOURCE = "org/camunda/bpm/engine/test/api/identity/read.only.identity.service.camunda.cfg.xml";

  @Rule
  public ProcessEngineRule engineRule = new ProcessEngineRule(CONFIGURATION_RESOURCE, true);

  protected IdentityService identityService;

  @Before
  public void setUp() {
    identityService = engineRule.getIdentityService();

    assertTrue(identityService.isReadOnly());
  }

  @Test
  public void unsupportedMethodsForUser() {
    try {
      identityService.newUser("whatever");
      fail("exception expected");
    } catch (UnsupportedOperationException e) {
      assertThat(e.getMessage(), containsString("This identity service implementation is read-only."));
    }

    try {
      identityService.saveUser(null);
      fail("exception expected");
    } catch (UnsupportedOperationException e) {
      assertThat(e.getMessage(), containsString("This identity service implementation is read-only."));
    }

    try {
      identityService.deleteUser("whatever");
      fail("exception expected");
    } catch (UnsupportedOperationException e) {
      assertThat(e.getMessage(), containsString("This identity service implementation is read-only."));
    }
  }

  @Test
  public void unsupportedMethodsForGroup() {
    try {
      identityService.newGroup("whatever");
      fail("exception expected");
    } catch (UnsupportedOperationException e) {
      assertThat(e.getMessage(), containsString("This identity service implementation is read-only."));
    }

    try {
      identityService.saveGroup(null);
      fail("exception expected");
    } catch (UnsupportedOperationException e) {
      assertThat(e.getMessage(), containsString("This identity service implementation is read-only."));
    }

    try {
      identityService.deleteGroup("whatever");
      fail("exception expected");
    } catch (UnsupportedOperationException e) {
      assertThat(e.getMessage(), containsString("This identity service implementation is read-only."));
    }
  }

  @Test
  public void unsupportedMethodsForMembership() {
    try {
      identityService.createMembership("whatever", "this won't work");
      fail("exception expected");
    } catch (UnsupportedOperationException e) {
      assertThat(e.getMessage(), containsString("This identity service implementation is read-only."));
    }

    try {
      identityService.deleteMembership("whatever", "this won't work");
      fail("exception expected");
    } catch (UnsupportedOperationException e) {
      assertThat(e.getMessage(), containsString("This identity service implementation is read-only."));
    }
  }

  @Test
  public void unsupportedMethodsForTenant() {
    try {
      identityService.newTenant("whatever");
      fail("exception expected");
    } catch (UnsupportedOperationException e) {
      assertThat(e.getMessage(), containsString("This identity service implementation is read-only."));
    }

    try {
      identityService.saveTenant(new TenantEntity());
      fail("exception expected");
    } catch (UnsupportedOperationException e) {
      assertThat(e.getMessage(), containsString("This identity service implementation is read-only."));
    }

    try {
      identityService.deleteTenant("whatever");
      fail("exception expected");
    } catch (UnsupportedOperationException e) {
      assertThat(e.getMessage(), containsString("This identity service implementation is read-only."));
    }
  }

  @Test
  public void supportedMethods() {
    identityService.checkPassword("user", "password");
    identityService.createUserQuery().list();
    identityService.createGroupQuery().list();
    identityService.createTenantQuery().list();
  }

}
