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

import java.util.List;

/**
 * The result of a password which was checked against a {@link PasswordPolicy}.
 * 
 * @author Miklas Boskamp
 *
 */
public interface PasswordPolicyResult {

  /**
   * Indicator of the overall result of the check.
   * 
   * @return <code>true</code> if all rules passed, <code>false</code> if at
   *         least one rule was violated.
   */
  public boolean isValid();

  /**
   * List of all rules that were violated during the check.
   * 
   * @return all violated rules.
   */
  public List<PasswordPolicyRule> getViolatedRules();

  /**
   * List of all rules that were fulfilled during the check.
   * 
   * @return all fulfilled rules.
   */
  public List<PasswordPolicyRule> getFulfilledRules();
}
