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
package org.camunda.bpm.engine.test.standalone.passwordPolicy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.impl.pwpolicy.DefaultPasswordPolicyImpl;
import org.camunda.bpm.engine.impl.pwpolicy.PasswordPolicyChecker;
import org.camunda.bpm.engine.impl.pwpolicy.PasswordPolicyDigitRuleImpl;
import org.camunda.bpm.engine.impl.pwpolicy.PasswordPolicyException;
import org.camunda.bpm.engine.impl.pwpolicy.PasswordPolicyLengthRuleImpl;
import org.camunda.bpm.engine.impl.pwpolicy.PasswordPolicyLowerCaseRuleImpl;
import org.camunda.bpm.engine.impl.pwpolicy.PasswordPolicySpecialCharacterRuleImpl;
import org.camunda.bpm.engine.impl.pwpolicy.PasswordPolicyUpperCaseRuleImpl;
import org.camunda.bpm.engine.pwpolicy.PasswordPolicyRule;
import org.junit.Test;

/**
 * @author Miklas Boskamp
 */
public class DefaultPasswordPolicyTest {

  @Test
  public void testDefaultPasswordPolicy() {
    // enforces a minimum length of 10 characters, at least one upper and one
    // lower case, one digit and one special character
    DefaultPasswordPolicyImpl policy = new DefaultPasswordPolicyImpl();

    // should all fail
    List<String> nonCompliantPasswords = Arrays.asList("password", "Password", "LongPassword", "LongPassw0rd");
    for (String pw : nonCompliantPasswords) {
      try {
        PasswordPolicyChecker.checkPassword(policy, pw);
        assertTrue(false);
      } catch (PasswordPolicyException e) {
        checkPolicyRules(e.getPolicyRules());
      }
    }

    // should pass
    assertTrue(PasswordPolicyChecker.checkPassword(policy, "LongPas$w0rd"));
  }

  private void checkPolicyRules(List<PasswordPolicyRule> rules) {
    assertEquals(5, rules.size());

    for (PasswordPolicyRule rule : rules) {
      if (PasswordPolicyLengthRuleImpl.class.isAssignableFrom(rule.getClass())) {
        assertEquals(PasswordPolicyLengthRuleImpl.placeholder, rule.getPlaceholder());
        assertEquals("10", ((PasswordPolicyLengthRuleImpl) rule).getParameter().get("minLength"));

      } else if (PasswordPolicyLowerCaseRuleImpl.class.isAssignableFrom(rule.getClass())) {
        assertEquals(PasswordPolicyLowerCaseRuleImpl.placeholder, rule.getPlaceholder());
        assertEquals("1", ((PasswordPolicyLowerCaseRuleImpl) rule).getParameter().get("minLowerCase"));

      } else if (PasswordPolicyUpperCaseRuleImpl.class.isAssignableFrom(rule.getClass())) {
        assertEquals(PasswordPolicyUpperCaseRuleImpl.placeholder, rule.getPlaceholder());
        assertEquals("1", ((PasswordPolicyUpperCaseRuleImpl) rule).getParameter().get("minUpperCase"));

      } else if (PasswordPolicyDigitRuleImpl.class.isAssignableFrom(rule.getClass())) {
        assertEquals(PasswordPolicyDigitRuleImpl.placeholder, rule.getPlaceholder());
        assertEquals("1", ((PasswordPolicyDigitRuleImpl) rule).getParameter().get("minDigit"));

      } else if (PasswordPolicySpecialCharacterRuleImpl.class.isAssignableFrom(rule.getClass())) {
        assertEquals(PasswordPolicySpecialCharacterRuleImpl.placeholder, rule.getPlaceholder());
        assertEquals("1", ((PasswordPolicySpecialCharacterRuleImpl) rule).getParameter().get("minSpecial"));
      }
    }

  }
}