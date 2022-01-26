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
package org.camunda.bpm.run.property;

public class CamundaBpmRunCorsProperty {

  public static final String PREFIX = CamundaBpmRunProperties.PREFIX + ".cors";
  public static final String DEFAULT_ORIGINS = "*";
  public static final String DEFAULT_HTTP_METHODS = "GET,POST,HEAD,OPTIONS,PUT,DELETE";

  // Duplicate the default values of the following CorsFilter properties,
  // to ensure they are not changed by a (Tomcat) version bump
  public static final String DEFAULT_PREFLIGHT_MAXAGE = "1800";
  public static final String DEFAULT_ALLOWED_HTTP_HEADERS = "Origin,Accept,X-Requested-With,Content-Type,Access-Control-Request-Method,Access-Control-Request-Headers";
  public static final String DEFAULT_EXPOSED_HEADERS = "";
  public static final boolean DEFAULT_ALLOW_CREDENTIALS = false;

  boolean enabled;

  // CORS properties
  String allowedOrigins;
  String allowedHeaders;
  String exposedHeaders;
  boolean allowCredentials;
  String preflightMaxAge;


  public CamundaBpmRunCorsProperty() {
    this.allowedOrigins = DEFAULT_ORIGINS;
    this.allowedHeaders = DEFAULT_ALLOWED_HTTP_HEADERS;
    this.exposedHeaders = DEFAULT_EXPOSED_HEADERS;
    this.allowCredentials = DEFAULT_ALLOW_CREDENTIALS;
    this.preflightMaxAge =DEFAULT_PREFLIGHT_MAXAGE;
  }

  public boolean isEnabled() {
    return this.enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getAllowedOrigins() {
    if(enabled) {
      return allowedOrigins == null ? DEFAULT_ORIGINS : allowedOrigins;
    }
    return null;
  }

  public void setAllowedOrigins(String allowedOrigins) {
    this.allowedOrigins = allowedOrigins;
  }

  public boolean getAllowCredentials() {
    return allowCredentials;
  }

  public void setAllowCredentials(boolean allowCredentials) {
    this.allowCredentials = allowCredentials;
  }

  public String getAllowedHeaders() {
    return allowedHeaders;
  }

  public void setAllowedHeaders(String allowedHeaders) {
    this.allowedHeaders = allowedHeaders;
  }

  public String getExposedHeaders() {
    return exposedHeaders;
  }

  public void setExposedHeaders(String exposedHeaders) {
    this.exposedHeaders = exposedHeaders;
  }

  public String getPreflightMaxAge() {
    return preflightMaxAge;
  }

  public void setPreflightMaxAge(String preflightMaxAge) {
    this.preflightMaxAge = preflightMaxAge;
  }

  @Override
  public String toString() {
    return "CamundaBpmRunCorsProperty [" +
        "enabled=" + enabled +
        ", allowCredentials=" + allowCredentials +
        ", allowedOrigins=" + allowedOrigins +
        ", allowedHeaders=" + allowedHeaders +
        ", exposedHeaders=" + exposedHeaders +
        ", preflightMaxAge=" + preflightMaxAge +
        ']';
  }
}