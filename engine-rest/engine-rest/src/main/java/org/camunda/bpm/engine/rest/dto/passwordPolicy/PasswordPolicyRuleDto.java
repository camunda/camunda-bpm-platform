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
package org.camunda.bpm.engine.rest.dto.passwordPolicy;

import java.util.Map;

import org.camunda.bpm.engine.pwpolicy.PasswordPolicyRule;

/**
 * @author Miklas Boskamp
 */
public class PasswordPolicyRuleDto {
  private String placeholder;
  private Map<String, String> parameter;

  // transformers

  public static PasswordPolicyRuleDto fromRule(PasswordPolicyRule rule) {
    PasswordPolicyRuleDto dto = new PasswordPolicyRuleDto();
    dto.setPlaceholder(rule.getPlaceholder());
    dto.setParameter(rule.getParameter());
    return dto;
  }

  // getters / setters

  public String getPlaceholder() {
    return placeholder;
  }

  public void setPlaceholder(String placeholder) {
    this.placeholder = placeholder;
  }

  public Map<String, String> getParameter() {
    return parameter;
  }

  public void setParameter(Map<String, String> parameter) {
    this.parameter = parameter;
  }

}