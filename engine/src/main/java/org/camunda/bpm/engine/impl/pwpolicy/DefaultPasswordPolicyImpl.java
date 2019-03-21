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

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.pwpolicy.PasswordPolicy;
import org.camunda.bpm.engine.pwpolicy.PasswordPolicyRule;

/**
 * @author Miklas Boskamp
 */
public class DefaultPasswordPolicyImpl implements PasswordPolicy {

  // password length
  public static final int MIN_LENGTH = 10;
  // password complexity
  public static final int MIN_LOWERCASE = 1;
  public static final int MIN_UPPERCSE = 1;
  public static final int MIN_DIGIT = 1;
  public static final int MIN_SPECIAL = 1;

  private final List<PasswordPolicyRule> rules = new ArrayList<PasswordPolicyRule>();

  public DefaultPasswordPolicyImpl() {
    rules.add(new PasswordPolicyLengthRuleImpl(MIN_LENGTH));
    rules.add(new PasswordPolicyLowerCaseRuleImpl(MIN_LOWERCASE));
    rules.add(new PasswordPolicyUpperCaseRuleImpl(MIN_UPPERCSE));
    rules.add(new PasswordPolicyDigitRuleImpl(MIN_DIGIT));
    rules.add(new PasswordPolicySpecialCharacterRuleImpl(MIN_SPECIAL));
  }

  @Override
  public List<PasswordPolicyRule> getRules() {
    return this.rules;
  }
}