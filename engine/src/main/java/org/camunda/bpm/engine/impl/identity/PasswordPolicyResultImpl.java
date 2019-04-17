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

import java.util.List;

import org.camunda.bpm.engine.identity.PasswordPolicyResult;
import org.camunda.bpm.engine.identity.PasswordPolicyRule;

/**
 * @author Miklas Boskamp
 *
 */
public class PasswordPolicyResultImpl implements PasswordPolicyResult {

  protected List<PasswordPolicyRule> violatedRules;
  protected List<PasswordPolicyRule> fulfilledRules;

  public PasswordPolicyResultImpl(List<PasswordPolicyRule> violatedRules, List<PasswordPolicyRule> fulfilledRules) {
    this.violatedRules = violatedRules;
    this.fulfilledRules = fulfilledRules;
  }

  public boolean isValid() {
    return violatedRules == null || violatedRules.size() == 0;
  }

  public List<PasswordPolicyRule> getViolatedRules() {
    return violatedRules;
  }

  public void setViolatedRules(List<PasswordPolicyRule> violatedRules) {
    this.violatedRules = violatedRules;
  }

  public List<PasswordPolicyRule> getFulfilledRules() {
    return fulfilledRules;
  }

  public void setFulfilledRules(List<PasswordPolicyRule> fulfilledRules) {
    this.fulfilledRules = fulfilledRules;
  }
}