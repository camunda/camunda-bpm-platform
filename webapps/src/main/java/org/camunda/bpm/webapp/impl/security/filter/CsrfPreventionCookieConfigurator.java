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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.webapp.impl.security.filter.util.CsrfConstants;

import javax.servlet.FilterConfig;
import java.util.Arrays;

public class CsrfPreventionCookieConfigurator {

  protected static final String ENABLE_SECURE_PARAM = "enableSecureCookie";
  protected static final String ENABLE_SAME_SITE_PARAM = "enableSameSiteCookie";
  protected static final String SAME_SITE_OPTION_PARAM = "sameSiteCookieOption";
  protected static final String SAME_SITE_VALUE_PARAM = "sameSiteCookieValue";

  protected boolean isSecureCookieEnabled;
  protected boolean isSameSiteCookieEnabled;

  protected String sameSiteCookieValue;

  public void parseParams(FilterConfig filterConfig) {

    String enableSecureCookie = filterConfig.getInitParameter(ENABLE_SECURE_PARAM);
    if (!isEmpty(enableSecureCookie)) {
      isSecureCookieEnabled = Boolean.valueOf(enableSecureCookie);
    }

    String enableSameSiteCookie = filterConfig.getInitParameter(ENABLE_SAME_SITE_PARAM);
    if (!isEmpty(enableSameSiteCookie)) {
      isSameSiteCookieEnabled = Boolean.valueOf(enableSameSiteCookie);
    } else {
      isSameSiteCookieEnabled = true; // default
    }

    String sameSiteCookieValue = filterConfig.getInitParameter(SAME_SITE_VALUE_PARAM);
    String sameSiteCookieOption = filterConfig.getInitParameter(SAME_SITE_OPTION_PARAM);

    if (!isEmpty(sameSiteCookieValue) && !isEmpty(sameSiteCookieOption)) {
      throw new ProcessEngineException("Please either configure " + SAME_SITE_OPTION_PARAM +
        " or " + SAME_SITE_VALUE_PARAM + ".");

    } else if (!isEmpty(sameSiteCookieValue)) {
      this.sameSiteCookieValue = sameSiteCookieValue;

    } else if (!isEmpty(sameSiteCookieOption)) {

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
    StringBuilder stringBuilder = new StringBuilder();

    if (isSameSiteCookieEnabled) {
      stringBuilder
        .append(CsrfConstants.CSRF_SAME_SITE_FIELD_NAME)
        .append(sameSiteCookieValue);
    }

    if (isSecureCookieEnabled) {
      stringBuilder.append(CsrfConstants.CSRF_SECURE_FLAG_NAME);
    }

    return stringBuilder.toString();
  }

  protected boolean isEmpty(String string) {
    return string == null || string.trim().isEmpty();
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
