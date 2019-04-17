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
package org.camunda.bpm.engine.rest.dto.identity;

import org.camunda.bpm.engine.identity.PasswordPolicyResult;
import org.camunda.bpm.engine.identity.PasswordPolicyRule;

/**
 * @author Miklas Boskamp
 *
 */
public class CheckPasswordPolicyResultDto extends PasswordPolicyDto {

  protected boolean valid = true;

  public static CheckPasswordPolicyResultDto fromPasswordPolicyResult(PasswordPolicyResult result) {
    CheckPasswordPolicyResultDto dto = new CheckPasswordPolicyResultDto();
    for (PasswordPolicyRule rule : result.getFulfilledRules()) {
      dto.rules.add(new CheckPasswordPolicyRuleDto(rule, true));
    }
    if(result.getViolatedRules().size() > 0) {
      dto.valid = false;
      for (PasswordPolicyRule rule : result.getViolatedRules()) {
        dto.rules.add(new CheckPasswordPolicyRuleDto(rule, false));
      }
    }
    return dto;
  }

  public boolean isValid() {
    return valid;
  }

  public void setValid(boolean valid) {
    this.valid = valid;
  }
}