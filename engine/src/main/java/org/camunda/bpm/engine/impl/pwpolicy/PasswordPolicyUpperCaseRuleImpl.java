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

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.pwpolicy.PasswordPolicyRule;

/**
 * @author Miklas Boskamp
 */
public class PasswordPolicyUpperCaseRuleImpl implements PasswordPolicyRule {

  public static final String placeholder = "UPPERCASE";
  
  private int minUpperCase;

  public PasswordPolicyUpperCaseRuleImpl(int minUpperCase) {
    this.minUpperCase = minUpperCase;
  }

  @Override
  public String getPlaceholder() {
    return PasswordPolicyUpperCaseRuleImpl.placeholder;
  }

  @Override
  public Map<String, String> getParameter() {
    Map<String, String> parameter = new HashMap<String, String>();
    parameter.put("minUpperCase", "" + this.minUpperCase);
    return parameter;
  }

  @Override
  public boolean execute(String password) {
    int lowerCaseCount = 0;
    for (Character c : password.toCharArray()) {
      if (Character.isUpperCase(c)) {
        lowerCaseCount++;
      }
      if (lowerCaseCount >= this.minUpperCase) {
        return true;
      }
    }
    return false;
  }
}
