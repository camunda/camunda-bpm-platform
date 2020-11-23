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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;

import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.identity.PasswordPolicy;
import org.camunda.bpm.engine.identity.PasswordPolicyResult;
import org.camunda.bpm.engine.identity.PasswordPolicyRule;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.identity.DefaultPasswordPolicyImpl;
import org.camunda.bpm.engine.impl.identity.PasswordPolicyDigitRuleImpl;
import org.camunda.bpm.engine.impl.identity.PasswordPolicyLengthRuleImpl;
import org.camunda.bpm.engine.impl.identity.PasswordPolicyLowerCaseRuleImpl;
import org.camunda.bpm.engine.impl.identity.PasswordPolicySpecialCharacterRuleImpl;
import org.camunda.bpm.engine.impl.identity.PasswordPolicyUpperCaseRuleImpl;
import org.camunda.bpm.engine.impl.identity.PasswordPolicyUserDataRuleImpl;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Miklas Boskamp
 */
public class DefaultPasswordPolicyTest {

  @Rule
  public ProcessEngineRule rule = new ProvidedProcessEngineRule();

  protected IdentityService identityService;

  // enforces a minimum length of 10 characters, at least one upper, one
  // lower case, one digit and one special character
  protected PasswordPolicy policy = new DefaultPasswordPolicyImpl();

  @Before
  public void init() {
    identityService = rule.getIdentityService();

    rule.getProcessEngineConfiguration()
      .setPasswordPolicy(new DefaultPasswordPolicyImpl())
      .setEnablePasswordPolicy(true);
  }

  @After
  public void resetProcessEngineConfig() {
    rule.getProcessEngineConfiguration()
      .setPasswordPolicy(null)
      .setEnablePasswordPolicy(false);
  }

  @Test
  public void testGoodPassword() {
    PasswordPolicyResult result = identityService.checkPasswordAgainstPolicy(policy, "LongPas$w0rd");
    assertThat(result.getViolatedRules().size(), is(0));
    assertThat(result.getFulfilledRules().size(), is(6));
    assertThat(result.isValid(), is(true));
  }

  @Test
  public void shouldCheckValidPassword_WithoutPassingPolicy() {
    // given

    // when
    PasswordPolicyResult result = identityService.checkPasswordAgainstPolicy("LongPas$w0rd");

    // then
    assertThat(result, notNullValue());
  }

  @Test
  public void testPasswordWithoutLowerCase() {
    PasswordPolicyResult result = identityService.checkPasswordAgainstPolicy(policy, "LONGPAS$W0RD");
    checkThatPasswordWasInvalid(result);

    PasswordPolicyRule rule = result.getViolatedRules().get(0);
    assertThat(rule.getPlaceholder(), is(PasswordPolicyLowerCaseRuleImpl.PLACEHOLDER));
    assertThat(rule, instanceOf(PasswordPolicyLowerCaseRuleImpl.class));
  }

  @Test
  public void testPasswordWithoutUpperCase() {
    PasswordPolicyResult result = identityService.checkPasswordAgainstPolicy(policy, "longpas$w0rd");
    checkThatPasswordWasInvalid(result);

    PasswordPolicyRule rule = result.getViolatedRules().get(0);
    assertThat(rule.getPlaceholder(), is(PasswordPolicyUpperCaseRuleImpl.PLACEHOLDER));
    assertThat(rule, instanceOf(PasswordPolicyUpperCaseRuleImpl.class));
  }

  @Test
  public void testPasswordWithoutSpecialChar() {
    PasswordPolicyResult result = identityService.checkPasswordAgainstPolicy(policy, "LongPassw0rd");
    checkThatPasswordWasInvalid(result);

    PasswordPolicyRule rule = result.getViolatedRules().get(0);
    assertThat(rule.getPlaceholder(), is(PasswordPolicySpecialCharacterRuleImpl.PLACEHOLDER));
    assertThat(rule, instanceOf(PasswordPolicySpecialCharacterRuleImpl.class));
  }

  @Test
  public void testPasswordWithoutDigit() {
    PasswordPolicyResult result = identityService.checkPasswordAgainstPolicy(policy, "LongPas$word");
    checkThatPasswordWasInvalid(result);

    PasswordPolicyRule rule = result.getViolatedRules().get(0);
    assertThat(rule.getPlaceholder(), is(PasswordPolicyDigitRuleImpl.PLACEHOLDER));
    assertThat(rule, instanceOf(PasswordPolicyDigitRuleImpl.class));
  }

  @Test
  public void testShortPassword() {
    PasswordPolicyResult result = identityService.checkPasswordAgainstPolicy(policy, "Pas$w0rd");
    checkThatPasswordWasInvalid(result);

    PasswordPolicyRule rule = result.getViolatedRules().get(0);
    assertThat(rule.getPlaceholder(), is(PasswordPolicyLengthRuleImpl.PLACEHOLDER));
    assertThat(rule, instanceOf(PasswordPolicyLengthRuleImpl.class));
  }

  @Test
  public void shouldThrowNullValueException_policyNull() {
    // given

    // when/then
    assertThatThrownBy(() -> identityService.checkPasswordAgainstPolicy(null, "Pas$w0rd"))
      .isInstanceOf(NullValueException.class)
      .hasMessageContaining("policy is null");
  }

  @Test
  public void shouldThrowNullValueException_passwordNull() {
    // given

    // when/then
    assertThatThrownBy(() -> identityService.checkPasswordAgainstPolicy(policy, null))
      .isInstanceOf(NullValueException.class)
      .hasMessageContaining("password is null");
  }

  @Test
  public void shouldGetPasswordPolicy() {
    // given

    // then
    PasswordPolicy passwordPolicy = identityService.getPasswordPolicy();

    // when
    assertThat(passwordPolicy, notNullValue());
  }

  @Test
  public void shouldUpdateUserDetailsWithoutPolicyCheck() {
    // given
    // first, create a new user
    User user = identityService.newUser("johndoe");
    user.setFirstName("John");
    user.setLastName("Doe");
    user.setEmail("john@doe.com");
    user.setPassword("Passw0rds!");
    identityService.saveUser(user);

    // when
    // fetch and update the user
    user = identityService.createUserQuery().userId("johndoe").singleResult();
    user.setEmail("jane@donnel.com");
    user.setFirstName("Jane");
    user.setLastName("Donnel");
    identityService.saveUser(user);

    // then
    user = identityService.createUserQuery().userId("johndoe").singleResult();
    assertThat(user.getFirstName(), is("Jane"));
    assertThat(user.getLastName(), is("Donnel"));
    assertThat(user.getEmail(), is("jane@donnel.com"));
    assertThat(identityService.checkPassword("johndoe", "Passw0rds!"), is(true));

    identityService.deleteUser(user.getId());
  }

  @Test
  public void shouldCheckUserRuleWithPolicyPassed() {
    // given
    User user = identityService.newUser("myUserId");
    String candidatePassword = "myUserId";

    // when
    PasswordPolicyResult result = identityService.checkPasswordAgainstPolicy(policy, candidatePassword, user);

    // then
    Assertions.assertThat(result.getViolatedRules())
        .extracting("placeholder")
        .contains(PasswordPolicyUserDataRuleImpl.PLACEHOLDER);
  }

  @Test
  public void shouldCheckPasswordNull() {
    // given
    User user = identityService.newUser("myUserId");
    String candidatePassword = null;

    // when/then
    assertThatThrownBy(() -> identityService.checkPasswordAgainstPolicy(candidatePassword, user))
      .isInstanceOf(NullValueException.class)
      .hasMessageContaining("password is null");
  }

  @Test
  public void shouldCheckPasswordEmpty() {
    // given
    User user = identityService.newUser("myUserId");
    String candidatePassword = "";

    // when
    PasswordPolicyResult result =
        identityService.checkPasswordAgainstPolicy(candidatePassword, user);

    // then
    Assertions.assertThat(result.getFulfilledRules())
        .extracting("placeholder")
        .contains(PasswordPolicyUserDataRuleImpl.PLACEHOLDER);
  }

  @Test
  public void shouldCheckUserNull() {
    // given
    User user = null;
    String candidatePassword = "my-password";

    // when
    PasswordPolicyResult result =
        identityService.checkPasswordAgainstPolicy(candidatePassword, user);

    // then
    Assertions.assertThat(result.getFulfilledRules())
        .extracting("placeholder")
        .contains(PasswordPolicyUserDataRuleImpl.PLACEHOLDER);
  }

  private void checkThatPasswordWasInvalid(PasswordPolicyResult result) {
    assertThat(result.getViolatedRules().size(), is(1));
    assertThat(result.getFulfilledRules().size(), is(5));
    assertThat(result.isValid(), is(false));
  }
}