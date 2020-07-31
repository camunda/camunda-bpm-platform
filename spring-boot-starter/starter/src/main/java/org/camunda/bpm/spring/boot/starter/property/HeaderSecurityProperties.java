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
package org.camunda.bpm.spring.boot.starter.property;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import static org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties.joinOn;

public class HeaderSecurityProperties {

  protected boolean xssProtectionDisabled = false;
  protected String xssProtectionOption;
  protected String xssProtectionValue;

  protected boolean contentSecurityPolicyDisabled = false;
  protected String contentSecurityPolicyValue;

  protected boolean contentTypeOptionsDisabled = false;
  protected String contentTypeOptionsValue;

  protected boolean hstsDisabled = true;
  protected boolean hstsIncludeSubdomainsDisabled = true;
  protected String hstsMaxAge;
  protected String hstsValue;

  public Map<String, String> getInitParams() {
    Map<String, String> initParams = new HashMap<>();

    if (xssProtectionDisabled) { // default is false
      initParams.put("xssProtectionDisabled", String.valueOf(xssProtectionDisabled));
    }
    if (StringUtils.isNotBlank(xssProtectionOption)) {
      initParams.put("xssProtectionOption", xssProtectionOption);
    }
    if (StringUtils.isNotBlank(xssProtectionValue)) {
      initParams.put("xssProtectionValue", xssProtectionValue);
    }

    if (contentSecurityPolicyDisabled) { // default is false
      initParams.put("contentSecurityPolicyDisabled", String.valueOf(contentSecurityPolicyDisabled));
    }
    if (StringUtils.isNotBlank(contentSecurityPolicyValue)) {
      initParams.put("contentSecurityPolicyValue", contentSecurityPolicyValue);
    }

    if (contentTypeOptionsDisabled) { // default is false
      initParams.put("contentTypeOptionsDisabled", String.valueOf(contentTypeOptionsDisabled));
    }
    if (StringUtils.isNotBlank(contentTypeOptionsValue)) {
      initParams.put("contentTypeOptionsValue", contentTypeOptionsValue);
    }

    if (!hstsDisabled) { // default is true
      initParams.put("hstsDisabled", String.valueOf(false));
    }
    if (StringUtils.isNotBlank(hstsMaxAge)) {
      initParams.put("hstsMaxAge", hstsMaxAge);
    }
    if (!hstsIncludeSubdomainsDisabled) { // default is true
      initParams.put("hstsIncludeSubdomainsDisabled", String.valueOf(false));
    }
    if (StringUtils.isNotBlank(hstsValue)) {
      initParams.put("hstsValue", hstsValue);
    }

    return initParams;
  }

  public boolean isXssProtectionDisabled() {
    return xssProtectionDisabled;
  }

  public void setXssProtectionDisabled(boolean xssProtectionDisabled) {
    this.xssProtectionDisabled = xssProtectionDisabled;
  }

  public String getXssProtectionOption() {
    return xssProtectionOption;
  }

  public void setXssProtectionOption(String xssProtectionOption) {
    this.xssProtectionOption = xssProtectionOption;
  }

  public String getXssProtectionValue() {
    return xssProtectionValue;
  }

  public void setXssProtectionValue(String xssProtectionValue) {
    this.xssProtectionValue = xssProtectionValue;
  }

  public boolean isContentSecurityPolicyDisabled() {
    return contentSecurityPolicyDisabled;
  }

  public void setContentSecurityPolicyDisabled(boolean contentSecurityPolicyDisabled) {
    this.contentSecurityPolicyDisabled = contentSecurityPolicyDisabled;
  }

  public String getContentSecurityPolicyValue() {
    return contentSecurityPolicyValue;
  }

  public void setContentSecurityPolicyValue(String contentSecurityPolicyValue) {
    this.contentSecurityPolicyValue = contentSecurityPolicyValue;
  }

  public boolean isContentTypeOptionsDisabled() {
    return contentTypeOptionsDisabled;
  }

  public void setContentTypeOptionsDisabled(boolean contentTypeOptionsDisabled) {
    this.contentTypeOptionsDisabled = contentTypeOptionsDisabled;
  }

  public String getContentTypeOptionsValue() {
    return contentTypeOptionsValue;
  }

  public void setContentTypeOptionsValue(String contentTypeOptionsValue) {
    this.contentTypeOptionsValue = contentTypeOptionsValue;
  }

  public boolean isHstsDisabled() {
    return hstsDisabled;
  }

  public void setHstsDisabled(boolean hstsDisabled) {
    this.hstsDisabled = hstsDisabled;
  }

  public boolean isHstsIncludeSubdomainsDisabled() {
    return hstsIncludeSubdomainsDisabled;
  }

  public void setHstsIncludeSubdomainsDisabled(boolean hstsIncludeSubdomainsDisabled) {
    this.hstsIncludeSubdomainsDisabled = hstsIncludeSubdomainsDisabled;
  }

  public String getHstsValue() {
    return hstsValue;
  }

  public void setHstsValue(String hstsValue) {
    this.hstsValue = hstsValue;
  }

  public String getHstsMaxAge() {
    return hstsMaxAge;
  }

  public void setHstsMaxAge(String hstsMaxAge) {
    this.hstsMaxAge = hstsMaxAge;
  }

  @Override
  public String toString() {
    StringJoiner joinedString = joinOn(this.getClass())

      .add("xssProtectionDisabled=" + xssProtectionDisabled)
      .add("xssProtectionOption=" + xssProtectionOption)
      .add("xssProtectionValue=" + xssProtectionValue)

      .add("contentSecurityPolicyDisabled=" + contentSecurityPolicyDisabled)
      .add("contentSecurityPolicyValue=" + contentSecurityPolicyValue)

      .add("contentTypeOptionsDisabled=" + contentTypeOptionsDisabled)
      .add("contentTypeOptionsValue=" + contentTypeOptionsValue)

      .add("hstsDisabled=" + hstsDisabled)
      .add("hstsMaxAge=" + hstsMaxAge)
      .add("hstsIncludeSubdomainsDisabled=" + hstsIncludeSubdomainsDisabled)
      .add("hstsValue=" + hstsValue);

    return joinedString.toString();
  }
}
