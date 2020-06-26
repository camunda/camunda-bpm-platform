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

import org.camunda.bpm.engine.identity.PasswordPolicyRule;
import org.camunda.bpm.engine.identity.User;

import java.util.Map;

public class PasswordPolicyUserDataRuleImpl implements PasswordPolicyRule {

  public static final String PLACEHOLDER = DefaultPasswordPolicyImpl.PLACEHOLDER_PREFIX + "USER_DATA";

  @Override
  public String getPlaceholder() {
    return PasswordPolicyUserDataRuleImpl.PLACEHOLDER;
  }

  @Override
  public Map<String, String> getParameters() {
    return null;
  }

  @Override
  public boolean execute(String password) {
    return false;
  }

  @Override
  public boolean execute(String candidatePassword, User user) {
    if (candidatePassword.isEmpty() || user == null) {
      return true;

    } else {
        candidatePassword = upperCase(candidatePassword);

        String id = upperCase(user.getId());
        String firstName = upperCase(user.getFirstName());
        String lastName = upperCase(user.getLastName());
        String email = upperCase(user.getEmail());

        return !(isNotBlank(id) && candidatePassword.contains(id) ||
            isNotBlank(firstName) && candidatePassword.contains(firstName) ||
            isNotBlank(lastName) && candidatePassword.contains(lastName) ||
            isNotBlank(email) && candidatePassword.contains(email));

    }
  }

  public String upperCase(String string) {
    return string == null ? null : string.toUpperCase();
  }

  public boolean isNotBlank(String value) {
    return value != null && !value.isEmpty();
  }

}