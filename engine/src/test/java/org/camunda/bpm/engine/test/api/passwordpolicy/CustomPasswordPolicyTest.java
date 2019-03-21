/*
 * Copyright © 2013-2019 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package org.camunda.bpm.engine.test.api.passwordpolicy;

import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.pwpolicy.PasswordPolicyException;
import org.camunda.bpm.engine.impl.test.ResourceProcessEngineTestCase;
import org.camunda.bpm.engine.pwpolicy.PasswordPolicy;

/**
 * @author Miklas Boskamp
 */
public class CustomPasswordPolicyTest extends ResourceProcessEngineTestCase {

  public CustomPasswordPolicyTest() {
    super("org/camunda/bpm/engine/test/api/passwordpolicy/passwordpolicy.custom.camunda.cfg.xml");
  }

  @Override
  protected void tearDown() throws Exception {
    identityService.deleteUser("user");
  }

  public void testCustomPasswordPolicy() {
    PasswordPolicy policy = processEngineConfiguration.getPasswordPolicy();
    assertTrue(policy.getClass().isAssignableFrom(CustomPasswordPolicyImpl.class));
    assertEquals(2, policy.getRules().size());

    User user = identityService.newUser("user");

    // check password blacklist rule is invoked
    for (String password : CustomPasswordPolicyImpl.passwordBlacklist) {
      try {
        user.setPassword(password);
        identityService.saveUser(user);
        fail();
      } catch (PasswordPolicyException e) {
        assertEquals(policy.getRules(), e.getPolicyRules());
      }
    }

    // check consecutive digit rule is invoked
    try {
      user.setPassword("password42");
      identityService.saveUser(user);
      fail();
    } catch (PasswordPolicyException e) {
      assertEquals(policy.getRules(), e.getPolicyRules());
    }

    // check user is saved when password is policy compliant
    user.setPassword("this should work");
    identityService.saveUser(user);
  }
}