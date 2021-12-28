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

import static org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties.joinOn;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class SessionCookieProperties {

  protected boolean enableSecureCookie = false;
  protected boolean enableSameSiteCookie = true;
  protected String sameSiteCookieOption;
  protected String sameSiteCookieValue;
  protected String cookieName;

  public boolean isEnableSecureCookie() {
    return enableSecureCookie;
  }

  public void setEnableSecureCookie(boolean enableSecureCookie) {
    this.enableSecureCookie = enableSecureCookie;
  }

  public boolean isEnableSameSiteCookie() {
    return enableSameSiteCookie;
  }

  public void setEnableSameSiteCookie(boolean enableSameSiteCookie) {
    this.enableSameSiteCookie = enableSameSiteCookie;
  }

  public String getSameSiteCookieOption() {
    return sameSiteCookieOption;
  }

  public void setSameSiteCookieOption(String sameSiteCookieOption) {
    this.sameSiteCookieOption = sameSiteCookieOption;
  }

  public String getSameSiteCookieValue() {
    return sameSiteCookieValue;
  }

  public void setSameSiteCookieValue(String sameSiteCookieValue) {
    this.sameSiteCookieValue = sameSiteCookieValue;
  }
  
  public String getCookieName() {
    return cookieName;
  }

  public void setCookieName(String cookieName) {
    this.cookieName = cookieName;
  }

  public Map<String, String> getInitParams() {
    Map<String, String> initParams = new HashMap<>();

    if (enableSecureCookie) { // only add param if it's true; default is false
      initParams.put("enableSecureCookie", String.valueOf(enableSecureCookie));
    }

    if (!enableSameSiteCookie) { // only add param if it's false; default is true
      initParams.put("enableSameSiteCookie", String.valueOf(enableSameSiteCookie));
    }

    if (StringUtils.isNotBlank(sameSiteCookieOption)) {
      initParams.put("sameSiteCookieOption", sameSiteCookieOption);
    }

    if (StringUtils.isNotBlank(sameSiteCookieValue)) {
      initParams.put("sameSiteCookieValue", sameSiteCookieValue);
    }
    
    if (StringUtils.isNotBlank(cookieName)) {
      initParams.put("cookieName", cookieName);
    }

    return initParams;
  }

  @Override
  public String toString() {
    return joinOn(this.getClass())
      .add("enableSecureCookie='" + enableSecureCookie + '\'')
      .add("enableSameSiteCookie='" + enableSameSiteCookie + '\'')
      .add("sameSiteCookieOption='" + sameSiteCookieOption + '\'')
      .add("sameSiteCookieValue='" + sameSiteCookieValue + '\'')
      .add("cookieName='" + cookieName + '\'')
      .toString();
  }
}
