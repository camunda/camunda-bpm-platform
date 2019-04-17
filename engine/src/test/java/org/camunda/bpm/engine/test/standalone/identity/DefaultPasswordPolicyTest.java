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
package org.camunda.bpm.engine.test.standalone.identity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.identity.CheckPasswordAgainstPolicyResult;
import org.camunda.bpm.engine.identity.PasswordPolicy;
import org.camunda.bpm.engine.identity.PasswordPolicyRule;
import org.camunda.bpm.engine.impl.identity.DefaultPasswordPolicyImpl;
import org.camunda.bpm.engine.impl.identity.PasswordPolicyDigitRuleImpl;
import org.camunda.bpm.engine.impl.identity.PasswordPolicyLengthRuleImpl;
import org.camunda.bpm.engine.impl.identity.PasswordPolicyLowerCaseRuleImpl;
import org.camunda.bpm.engine.impl.identity.PasswordPolicySpecialCharacterRuleImpl;
import org.camunda.bpm.engine.impl.identity.PasswordPolicyUpperCaseRuleImpl;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Miklas Boskamp
 */
public class DefaultPasswordPolicyTest {

  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  protected IdentityService identityService;

  // enforces a minimum length of 10 characters, at least one upper, one
  // lower case, one digit and one special character
  protected PasswordPolicy policy = new DefaultPasswordPolicyImpl();

  @Before
  public void init() {
    identityService = rule.getIdentityService();
  }

  @Test
  public void testGoodPassword() {
    CheckPasswordAgainstPolicyResult result = identityService.checkPasswordAgainstPolicy(policy, "LongPas$w0rd");
    assertThat(result.getViolatedRules().size(), is(0));
    assertThat(result.getFulfilledRules().size(), is(5));
    assertThat(result.isValid(), is(true));
  }

  @Test
  public void testPasswordWithoutLowerCase() {
    CheckPasswordAgainstPolicyResult result = identityService.checkPasswordAgainstPolicy(policy, "LONGPAS$W0RD");
    checkThatPasswordWasInvalid(result);

    PasswordPolicyRule rule = result.getViolatedRules().get(0);
    assertThat(rule.getPlaceholder(), is(PasswordPolicyLowerCaseRuleImpl.PLACEHOLDER));
    assertThat(rule, instanceOf(PasswordPolicyLowerCaseRuleImpl.class));
  }

  @Test
  public void testPasswordWithoutUpperCase() {
    CheckPasswordAgainstPolicyResult result = identityService.checkPasswordAgainstPolicy(policy, "longpas$w0rd");
    checkThatPasswordWasInvalid(result);

    PasswordPolicyRule rule = result.getViolatedRules().get(0);
    assertThat(rule.getPlaceholder(), is(PasswordPolicyUpperCaseRuleImpl.PLACEHOLDER));
    assertThat(rule, instanceOf(PasswordPolicyUpperCaseRuleImpl.class));
  }

  @Test
  public void testPasswordWithoutSpecialChar() {
    CheckPasswordAgainstPolicyResult result = identityService.checkPasswordAgainstPolicy(policy, "LongPassw0rd");
    checkThatPasswordWasInvalid(result);

    PasswordPolicyRule rule = result.getViolatedRules().get(0);
    assertThat(rule.getPlaceholder(), is(PasswordPolicySpecialCharacterRuleImpl.PLACEHOLDER));
    assertThat(rule, instanceOf(PasswordPolicySpecialCharacterRuleImpl.class));
  }

  @Test
  public void testPasswordWithoutDigit() {
    CheckPasswordAgainstPolicyResult result = identityService.checkPasswordAgainstPolicy(policy, "LongPas$word");
    checkThatPasswordWasInvalid(result);

    PasswordPolicyRule rule = result.getViolatedRules().get(0);
    assertThat(rule.getPlaceholder(), is(PasswordPolicyDigitRuleImpl.PLACEHOLDER));
    assertThat(rule, instanceOf(PasswordPolicyDigitRuleImpl.class));
  }

  @Test
  public void testShortPassword() {
    CheckPasswordAgainstPolicyResult result = identityService.checkPasswordAgainstPolicy(policy, "Pas$w0rd");
    checkThatPasswordWasInvalid(result);

    PasswordPolicyRule rule = result.getViolatedRules().get(0);
    assertThat(rule.getPlaceholder(), is(PasswordPolicyLengthRuleImpl.PLACEHOLDER));
    assertThat(rule, instanceOf(PasswordPolicyLengthRuleImpl.class));
  }

  @Test
  public void shouldThrowNullValueException_policyNull() {
    // given

    // then
    thrown.expectMessage("policy is null");
    thrown.expect(NullValueException.class);

    // when
    identityService.checkPasswordAgainstPolicy(null, "Pas$w0rd");
  }

  @Test
  public void shouldThrowNullValueException_passwordNull() {
    // given

    // then
    thrown.expectMessage("password is null");
    thrown.expect(NullValueException.class);

    // when
    identityService.checkPasswordAgainstPolicy(policy, null);
  }

  private void checkThatPasswordWasInvalid(CheckPasswordAgainstPolicyResult result) {
    assertThat(result.getViolatedRules().size(), is(1));
    assertThat(result.getFulfilledRules().size(), is(4));
    assertThat(result.isValid(), is(false));
  }
}