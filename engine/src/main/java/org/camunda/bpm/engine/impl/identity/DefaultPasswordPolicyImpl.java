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
package org.camunda.bpm.engine.impl.identity;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.identity.PasswordPolicy;
import org.camunda.bpm.engine.identity.PasswordPolicyRule;

/**
 * @author Miklas Boskamp
 */
public class DefaultPasswordPolicyImpl implements PasswordPolicy {
  
  protected static final String PLACEHOLDER_PREFIX = "PASSWORD_POLICY_";

  // password length
  public static final int MIN_LENGTH = 10;
  // password complexity
  public static final int MIN_LOWERCASE = 1;
  public static final int MIN_UPPERCASE = 1;
  public static final int MIN_DIGIT = 1;
  public static final int MIN_SPECIAL = 1;

  protected final List<PasswordPolicyRule> rules = new ArrayList<>();

  public DefaultPasswordPolicyImpl() {
    rules.add(new PasswordPolicyUserDataRuleImpl());
    rules.add(new PasswordPolicyLengthRuleImpl(MIN_LENGTH));
    rules.add(new PasswordPolicyLowerCaseRuleImpl(MIN_LOWERCASE));
    rules.add(new PasswordPolicyUpperCaseRuleImpl(MIN_UPPERCASE));
    rules.add(new PasswordPolicyDigitRuleImpl(MIN_DIGIT));
    rules.add(new PasswordPolicySpecialCharacterRuleImpl(MIN_SPECIAL));
  }

  @Override
  public List<PasswordPolicyRule> getRules() {
    return this.rules;
  }
}