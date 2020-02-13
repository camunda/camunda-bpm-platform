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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties.joinOn;

/**
 * @author Nikola Koevski
 */
public class CsrfProperties {

  private String targetOrigin = null;
  private Integer denyStatus = null;
  private String randomClass = null;
  private List<String> entryPoints = new ArrayList<>();
  protected boolean enableSecureCookie = false;
  protected boolean enableSameSiteCookie = true;
  protected String sameSiteCookieOption;
  protected String sameSiteCookieValue;
  protected String cookieName;

  public String getTargetOrigin() {
    return targetOrigin;
  }

  public void setTargetOrigin(String targetOrigin) {
    this.targetOrigin = targetOrigin;
  }

  public Integer getDenyStatus() {
    return denyStatus;
  }

  public void setDenyStatus(Integer denyStatus) {
    this.denyStatus = denyStatus;
  }

  public String getRandomClass() {
    return randomClass;
  }

  public void setRandomClass(String randomClass) {
    this.randomClass = randomClass;
  }

  public List<String> getEntryPoints() {
    return entryPoints;
  }

  public void setEntryPoints(List<String> entryPoints) {
    this.entryPoints = entryPoints;
  }

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

    if (StringUtils.isNotBlank(targetOrigin)) {
      initParams.put("targetOrigin", targetOrigin);
    }

    if (denyStatus != null) {
      initParams.put("denyStatus", denyStatus.toString());
    }

    if (StringUtils.isNotBlank(randomClass)) {
      initParams.put("randomClass", randomClass);
    }

    if (!entryPoints.isEmpty()) {
      initParams.put("entryPoints", StringUtils.join(entryPoints, ","));
    }

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
      .add("targetOrigin=" + targetOrigin)
      .add("denyStatus='" + denyStatus + '\'')
      .add("randomClass='" + randomClass + '\'')
      .add("entryPoints='" + entryPoints + '\'')
      .add("enableSecureCookie='" + enableSecureCookie + '\'')
      .add("enableSameSiteCookie='" + enableSameSiteCookie + '\'')
      .add("sameSiteCookieOption='" + sameSiteCookieOption + '\'')
      .add("sameSiteCookieValue='" + sameSiteCookieValue + '\'')
      .add("cookieName='" + cookieName + '\'')
      .toString();
  }
}
