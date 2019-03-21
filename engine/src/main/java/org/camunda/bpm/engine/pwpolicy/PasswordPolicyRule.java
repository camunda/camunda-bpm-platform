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
package org.camunda.bpm.engine.pwpolicy;

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
   * String that can be used by a front end to display a description of this
   * rule. The actual description text must be managed by the front end.
   * 
   * @return the placeholder for the description text.
   */
  public String getPlaceholder();

  /**
   * Additional parameter that can be used by the front end to display a
   * meaningful description.
   * 
   * @return a map of parameter
   */
  public Map<String, String> getParameter();

  /**
   * Checks the given password against this rule.
   * 
   * @param password
   *          the string that should be checked
   * @return <code>true</code> if the given password matches this rule.
   *         <code>false</code> if the given password is not compliant with this
   *         rule.
   */
  public boolean execute(String password);
}