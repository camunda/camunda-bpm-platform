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

import org.springframework.boot.context.properties.NestedConfigurationProperty;

import static org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties.joinOn;

public class WebappProperty {

  public static final String DEFAULT_APP_PATH = "/camunda";

  public static final String PREFIX = CamundaBpmProperties.PREFIX + ".webapp";

  protected boolean indexRedirectEnabled = true;

  protected String webjarClasspath = "/META-INF/resources/webjars/camunda";

  protected String securityConfigFile = "/securityFilterRules.json";

  protected String applicationPath = DEFAULT_APP_PATH;

  @NestedConfigurationProperty
  private CsrfProperties csrf = new CsrfProperties();
  
  @NestedConfigurationProperty
  private SessionCookieProperties sessionCookie = new SessionCookieProperties();

  @NestedConfigurationProperty
  protected HeaderSecurityProperties headerSecurity = new HeaderSecurityProperties();

  @NestedConfigurationProperty
  protected AuthenticationProperties auth = new AuthenticationProperties();

  public boolean isIndexRedirectEnabled() {
    return indexRedirectEnabled;
  }

  public void setIndexRedirectEnabled(boolean indexRedirectEnabled) {
    this.indexRedirectEnabled = indexRedirectEnabled;
  }

  public String getWebjarClasspath() {
    return webjarClasspath;
  }

  public void setWebjarClasspath(String webjarClasspath) {
    this.webjarClasspath = webjarClasspath;
  }

  public String getSecurityConfigFile() {
    return securityConfigFile;
  }

  public void setSecurityConfigFile(String securityConfigFile) {
    this.securityConfigFile = securityConfigFile;
  }

  public String getApplicationPath() {
    return applicationPath;
  }

  public void setApplicationPath(String applicationPath) {
    this.applicationPath = sanitizeApplicationPath(applicationPath);
  }

  protected String sanitizeApplicationPath(String applicationPath) {
    if (applicationPath == null || applicationPath.isEmpty()) {
      return "";
    }

    if (!applicationPath.startsWith("/")) {
      applicationPath = "/" + applicationPath;
    }

    if (applicationPath.endsWith("/")) {
      applicationPath = applicationPath.substring(0, applicationPath.length() - 1);
    }

    return applicationPath;
  }

  public CsrfProperties getCsrf() {
    return csrf;
  }

  public void setCsrf(CsrfProperties csrf) {
    this.csrf = csrf;
  }
  
  public SessionCookieProperties getSessionCookie() {
    return sessionCookie;
  }
  
  public void setSessionCookie(SessionCookieProperties sessionCookie) {
    this.sessionCookie = sessionCookie;
  }

  public HeaderSecurityProperties getHeaderSecurity() {
    return headerSecurity;
  }

  public void setHeaderSecurity(HeaderSecurityProperties headerSecurity) {
    this.headerSecurity = headerSecurity;
  }

  public AuthenticationProperties getAuth() {
    return auth;
  }

  public void setAuth(AuthenticationProperties authentication) {
    this.auth = authentication;
  }

  @Override
  public String toString() {
    return joinOn(this.getClass())
      .add("indexRedirectEnabled=" + indexRedirectEnabled)
      .add("webjarClasspath='" + webjarClasspath + '\'')
      .add("securityConfigFile='" + securityConfigFile + '\'')
      .add("webappPath='" + applicationPath + '\'')
      .add("csrf='" + csrf + '\'')
      .add("headerSecurityProperties='" + headerSecurity + '\'')
      .add("sessionCookie='" + sessionCookie + '\'')
      .add("auth='" + auth + '\'')
      .toString();
  }
}
