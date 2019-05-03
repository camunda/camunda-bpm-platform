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
package org.camunda.bpm.engine.impl.identity;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.identity.PasswordPolicyRule;

/**
 * @author Miklas Boskamp
 */
public class PasswordPolicyLengthRuleImpl implements PasswordPolicyRule {

  public static final String PLACEHOLDER = DefaultPasswordPolicyImpl.PLACEHOLDER_PREFIX + "LENGTH";
  
  protected int minLength;

  public PasswordPolicyLengthRuleImpl(int minLength) {
    this.minLength = minLength;
  }

  @Override
  public String getPlaceholder() {
    return PasswordPolicyLengthRuleImpl.PLACEHOLDER;
  }

  @Override
  public Map<String, String> getParameters() {
    Map<String, String> parameter = new HashMap<String, String>();
    parameter.put("minLength", "" + this.minLength);
    return parameter;
  }

  @Override
  public boolean execute(String password) {
    return password.length() >= this.minLength;
  }
}