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
package org.camunda.bpm.engine.impl.pwpolicy;

import org.camunda.bpm.engine.pwpolicy.PasswordPolicy;
import org.camunda.bpm.engine.pwpolicy.PasswordPolicyRule;

/**
 * @author Miklas Boskamp
 */
public class PasswordPolicyChecker {

  /**
   * Check a given password against a given {@link PasswordPolicy}. If the
   * password does not match a policy {@link PasswordPolicyRule rules} a
   * {@link PasswordPolicyException} is thrown. If the password passes all
   * {@link PasswordPolicyRule rules} the method returns <code>true</code>.
   * 
   * @param policy
   *          the {@link PasswordPolicy} against which the password is tested
   * @param password
   *          the password that should be tested
   * @return <code>true</code> if the password is policy-compliant (if not an
   *         exception is thrown)
   */
  public static boolean checkPassword(PasswordPolicy policy, String password) {
    if (policy != null) {
      if (password == null) {
        throw new PasswordPolicyException(policy.getRules());
      }
      for (PasswordPolicyRule rule : policy.getRules()) {
        if (!rule.execute(password)) {
          throw new PasswordPolicyException(policy.getRules());
        }
      }
    }
    return true;
  }
}