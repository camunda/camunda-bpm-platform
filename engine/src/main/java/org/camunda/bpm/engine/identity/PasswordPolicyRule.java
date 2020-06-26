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
package org.camunda.bpm.engine.identity;

import org.camunda.bpm.engine.IdentityService;

import java.util.Map;

/**
 * Describes a rule of a {@link PasswordPolicy}. All rules attached to a
 * {@link PasswordPolicy} must be matched by passwords for engine-managed users
 * to be policy compliant.
 * 
 * @author Miklas Boskamp
 */
public interface PasswordPolicyRule {

  /**
   * Placeholder string that can be used to display a description of this rule.
   * The actual description text must be managed on the calling side.
   * 
   * @return the placeholder for the description text.
   */
  String getPlaceholder();

  /**
   * Additional parameter that can be used to display a meaningful description.
   * 
   * @return a map of parameters
   */
  Map<String, String> getParameters();

  /**
   * Checks the given password against this rule.
   *
   * <p><strong>Heads-up:</strong> The return value is not respected when
   * {@link PasswordPolicyRule#execute(String, User)} is implemented.</p>
   * 
   * @param candidatePassword
   *          which is checked against a password policy
   * @return <code>true</code> if the given password matches this rule.
   *         <code>false</code> if the given password is not compliant with this
   *         rule.
   */
  boolean execute(String candidatePassword);

  /**
   * Checks the given password and the user against this rule.
   *
   * @param candidatePassword
   *          which is checked against a password policy
   * @param user
   *          to be taken into account when checking the candidate password. Can be {@code null}
   *          when {@link IdentityService#checkPassword(String, String)} is called.
   * @return <code>true</code> if the given password matches this rule.
   *         <code>false</code> if the given password is not compliant with this
   *         rule.
   */
  default boolean execute(String candidatePassword, User user) {
    return execute(candidatePassword);
  }

}