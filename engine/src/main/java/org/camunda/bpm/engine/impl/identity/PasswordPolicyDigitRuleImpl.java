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

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.identity.PasswordPolicyRule;

/**
 * @author Miklas Boskamp
 */
public class PasswordPolicyDigitRuleImpl implements PasswordPolicyRule {

  public static final String PLACEHOLDER = DefaultPasswordPolicyImpl.PLACEHOLDER_PREFIX + "DIGIT";
  
  protected int minDigit;

  public PasswordPolicyDigitRuleImpl(int minDigit) {
    this.minDigit = minDigit;
  }

  @Override
  public String getPlaceholder() {
    return PasswordPolicyDigitRuleImpl.PLACEHOLDER;
  }

  @Override
  public Map<String, String> getParameters() {
    Map<String, String> parameter = new HashMap<String, String>();
    parameter.put("minDigit", "" + this.minDigit);
    return parameter;
  }
  
  @Override
  public boolean execute(String password) {
    int digitCount = 0;
    for (Character c : password.toCharArray()) {
      if (Character.isDigit(c)) {
        digitCount++;
      }
      if (digitCount >= this.minDigit) {
        return true;
      }
    }
    return false;
  }
}