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

import org.camunda.bpm.engine.impl.test.ResourceProcessEngineTestCase;

/**
 * @author Daniel Meyer
 * 
 */
public class ReadOnlyIdentityServiceTest extends ResourceProcessEngineTestCase {

  public ReadOnlyIdentityServiceTest() {
    super("org/camunda/bpm/engine/test/api/identity/read.only.identity.service.camunda.cfg.xml");
  }

  @Override
  protected void closeDownProcessEngine() {
    processEngine.close();
    super.closeDownProcessEngine();
  }

  public void testUnsupportedMethods() {
    assertTrue(identityService.isReadOnly());

    try {
      identityService.newUser("whatever");
      fail("exception expected");
    } catch (UnsupportedOperationException e) {
      assertTextPresent("This identity service implementation is read-only.", e.getMessage());
    }

    try {
      identityService.saveUser(null);
      fail("exception expected");
    } catch (UnsupportedOperationException e) {
      assertTextPresent("This identity service implementation is read-only.", e.getMessage());
    }

    try {
      identityService.deleteUser("whatever");
      fail("exception expected");
    } catch (UnsupportedOperationException e) {
      assertTextPresent("This identity service implementation is read-only.", e.getMessage());
    }

    try {
      identityService.newGroup("whatever");
      fail("exception expected");
    } catch (UnsupportedOperationException e) {
      assertTextPresent("This identity service implementation is read-only.", e.getMessage());
    }

    try {
      identityService.saveGroup(null);
      fail("exception expected");
    } catch (UnsupportedOperationException e) {
      assertTextPresent("This identity service implementation is read-only.", e.getMessage());
    }

    try {
      identityService.deleteGroup("whatever");
      fail("exception expected");
    } catch (UnsupportedOperationException e) {
      assertTextPresent("This identity service implementation is read-only.", e.getMessage());
    }

    try {
      identityService.createMembership("whatever", "this won't work");
      fail("exception expected");
    } catch (UnsupportedOperationException e) {
      assertTextPresent("This identity service implementation is read-only.", e.getMessage());
    }

    try {
      identityService.deleteMembership("whatever", "this won't work");
      fail("exception expected");
    } catch (UnsupportedOperationException e) {
      assertTextPresent("This identity service implementation is read-only.", e.getMessage());
    }
  }

  public void testSupportedMethods() {
    assertTrue(identityService.isReadOnly());

    identityService.checkPassword("user", "password");
    identityService.createUserQuery().list();
    identityService.createGroupQuery().list();

  }

}
