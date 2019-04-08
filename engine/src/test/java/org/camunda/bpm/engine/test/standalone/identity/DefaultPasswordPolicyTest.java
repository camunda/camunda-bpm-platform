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
package org.camunda.bpm.engine.test.standalone.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.identity.PasswordPolicyRule;
import org.camunda.bpm.engine.impl.identity.DefaultPasswordPolicyImpl;
import org.camunda.bpm.engine.impl.identity.PasswordPolicyDigitRuleImpl;
import org.camunda.bpm.engine.impl.identity.PasswordPolicyException;
import org.camunda.bpm.engine.impl.identity.PasswordPolicyLengthRuleImpl;
import org.camunda.bpm.engine.impl.identity.PasswordPolicyLowerCaseRuleImpl;
import org.camunda.bpm.engine.impl.identity.PasswordPolicySpecialCharacterRuleImpl;
import org.camunda.bpm.engine.impl.identity.PasswordPolicyUpperCaseRuleImpl;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Miklas Boskamp
 */
public class DefaultPasswordPolicyTest {

  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule();

  @Test
  public void testDefaultPasswordPolicy() {
    IdentityService identityService = rule.getIdentityService();

    // enforces a minimum length of 10 characters, at least one upper and one
    // lower case, one digit and one special character
    DefaultPasswordPolicyImpl policy = new DefaultPasswordPolicyImpl();

    // should all fail
    List<String> nonCompliantPasswords = Arrays.asList("password", "Password", "LongPassword", "LongPassw0rd");
    for (String pw : nonCompliantPasswords) {
      try {
        identityService.checkPasswordAgainstPolicy(policy, pw);
        fail("exception expected");
      } catch (PasswordPolicyException e) {
        checkPolicyRules(e.getPolicyRules());
      }
    }

    // should pass
    assertThat(identityService.checkPasswordAgainstPolicy(policy, "LongPas$w0rd"));
  }

  private void checkPolicyRules(List<PasswordPolicyRule> rules) {
    assertThat(rules.size()).isEqualTo(5);

    for (PasswordPolicyRule rule : rules) {
      if (PasswordPolicyLengthRuleImpl.class.isAssignableFrom(rule.getClass())) {
        assertThat(rule.getPlaceholder()).isEqualTo(PasswordPolicyLengthRuleImpl.PLACEHOLDER);
        assertThat(((PasswordPolicyLengthRuleImpl) rule).getParameters().get("minLength")).isEqualTo("10");

      } else if (PasswordPolicyLowerCaseRuleImpl.class.isAssignableFrom(rule.getClass())) {
        assertThat(rule.getPlaceholder()).isEqualTo(PasswordPolicyLowerCaseRuleImpl.PLACEHOLDER);
        assertThat(((PasswordPolicyLowerCaseRuleImpl) rule).getParameters().get("minLowerCase")).isEqualTo("1");

      } else if (PasswordPolicyUpperCaseRuleImpl.class.isAssignableFrom(rule.getClass())) {
        assertThat(rule.getPlaceholder()).isEqualTo(PasswordPolicyUpperCaseRuleImpl.PLACEHOLDER);
        assertThat(((PasswordPolicyUpperCaseRuleImpl) rule).getParameters().get("minUpperCase")).isEqualTo("1");

      } else if (PasswordPolicyDigitRuleImpl.class.isAssignableFrom(rule.getClass())) {
        assertThat(rule.getPlaceholder()).isEqualTo(PasswordPolicyDigitRuleImpl.PLACEHOLDER);
        assertThat(((PasswordPolicyDigitRuleImpl) rule).getParameters().get("minDigit")).isEqualTo("1");

      } else if (PasswordPolicySpecialCharacterRuleImpl.class.isAssignableFrom(rule.getClass())) {
        assertThat(rule.getPlaceholder()).isEqualTo(PasswordPolicySpecialCharacterRuleImpl.PLACEHOLDER);
        assertThat(((PasswordPolicySpecialCharacterRuleImpl) rule).getParameters().get("minSpecial")).isEqualTo("1");
      }
    }
  }
}