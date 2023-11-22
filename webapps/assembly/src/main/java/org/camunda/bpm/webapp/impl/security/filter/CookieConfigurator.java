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
package org.camunda.bpm.webapp.impl.security.filter;

import java.util.Arrays;

import javax.servlet.FilterConfig;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.webapp.impl.security.filter.util.CookieConstants;
import org.camunda.bpm.webapp.impl.util.ServletFilterUtil;

public class CookieConfigurator {

  protected static final String ENABLE_SECURE_PARAM = "enableSecureCookie";
  protected static final String ENABLE_SAME_SITE_PARAM = "enableSameSiteCookie";
  protected static final String SAME_SITE_OPTION_PARAM = "sameSiteCookieOption";
  protected static final String SAME_SITE_VALUE_PARAM = "sameSiteCookieValue";

  protected boolean isSecureCookieEnabled;
  protected boolean isSameSiteCookieEnabled;
  protected String sameSiteCookieValue;
  protected String cookieName;

  public void parseParams(FilterConfig filterConfig) {

    String enableSecureCookie = filterConfig.getInitParameter(ENABLE_SECURE_PARAM);
    if (!ServletFilterUtil.isEmpty(enableSecureCookie)) {
      isSecureCookieEnabled = Boolean.parseBoolean(enableSecureCookie);
    }

    String cookieNameInput = filterConfig.getInitParameter("cookieName");
    if (!isBlank(cookieNameInput)) {
      cookieName = cookieNameInput;
    }

    String enableSameSiteCookie = filterConfig.getInitParameter(ENABLE_SAME_SITE_PARAM);
    if (!ServletFilterUtil.isEmpty(enableSameSiteCookie)) {
      isSameSiteCookieEnabled = Boolean.parseBoolean(enableSameSiteCookie);
    } else {
      isSameSiteCookieEnabled = true; // default
    }

    String sameSiteCookieValue = filterConfig.getInitParameter(SAME_SITE_VALUE_PARAM);
    String sameSiteCookieOption = filterConfig.getInitParameter(SAME_SITE_OPTION_PARAM);

    if (!ServletFilterUtil.isEmpty(sameSiteCookieValue) && !ServletFilterUtil.isEmpty(sameSiteCookieOption)) {
      throw new ProcessEngineException("Please either configure " + SAME_SITE_OPTION_PARAM +
        " or " + SAME_SITE_VALUE_PARAM + ".");

    } else if (!ServletFilterUtil.isEmpty(sameSiteCookieValue)) {
      this.sameSiteCookieValue = sameSiteCookieValue;

    } else if (!ServletFilterUtil.isEmpty(sameSiteCookieOption)) {

      if (SameSiteOption.LAX.compareTo(sameSiteCookieOption)) {
        this.sameSiteCookieValue = SameSiteOption.LAX.getValue();

      } else if (SameSiteOption.STRICT.compareTo(sameSiteCookieOption)) {
        this.sameSiteCookieValue = SameSiteOption.STRICT.getValue();

      } else {
        throw new ProcessEngineException("For " + SAME_SITE_OPTION_PARAM + " param, please configure one of the " +
          "following options: " + Arrays.toString(SameSiteOption.values()));

      }

    } else { // default
      this.sameSiteCookieValue = SameSiteOption.LAX.getValue();

    }
  }

  public String getConfig() {
    return getConfig(null);
  }

  public String getConfig(String currentHeader) {
    StringBuilder stringBuilder = new StringBuilder(currentHeader == null ? "" : currentHeader);

    if (isSameSiteCookieEnabled) {
      if (currentHeader == null || !CookieConstants.SAME_SITE_FIELD_NAME_REGEX.matcher(currentHeader).find()) {
        stringBuilder
          .append(CookieConstants.SAME_SITE_FIELD_NAME)
          .append(sameSiteCookieValue);
      }
    }

    if (isSecureCookieEnabled) {
      if (currentHeader == null || !CookieConstants.SECURE_FLAG_NAME_REGEX.matcher(currentHeader).find()) {
        stringBuilder.append(CookieConstants.SECURE_FLAG_NAME);
      }
    }

    return stringBuilder.toString();
  }

  public String getCookieName(String defaultName) {
    return isBlank(cookieName) ? defaultName : cookieName;
  }

  protected boolean isBlank(String s) {
    return s == null || s.trim().isEmpty();
  }

  public enum SameSiteOption {

    LAX("Lax"),
    STRICT("Strict");

    protected final String value;

    SameSiteOption(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    public String getName() {
      return this.name();
    }

    public boolean compareTo(String value) {
      return this.value.equalsIgnoreCase(value);
    }

  }

}
