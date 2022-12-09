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
package org.camunda.bpm.webapp.impl.util;

import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.webapp.impl.security.auth.AuthenticationFilter;
import org.camunda.bpm.webapp.impl.security.auth.UserAuthenticationResource;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.Date;

/**
 * With Camunda Platform 7.13 we introduced the application path prefix /camunda to Spring Boot.
 * The application path is set in Spring Boot's servlet context and is consumed by filters and
 * servlets of the Camunda Platform Webapp. This util class holds the methods to get and set the
 * application path.
 */
public class ServletContextUtil {

  protected static final String APP_PATH_ATTR_NAME =
    "org.camunda.bpm.spring.boot.starter.webapp.applicationPath";

  protected static final String SUCCESSFUL_ET_ATTR_NAME =
    "org.camunda.bpm.webapp.telemetry.data.stored";

  protected static final String AUTH_CACHE_TTL_ATTR_NAME =
    "org.camunda.bpm.webapp.auth.cache.ttl";

  /**
   * Consumed by Camunda Platform CE & EE Webapp:
   * Retrieves the application path from Spring Boot's servlet context.
   *
   * @param servletContext that holds the application path
   * @return a non-empty <code>String</code> containing the application path or an empty
   * <code>String</code> when no application path was set.
   */
  public static String getAppPath(ServletContext servletContext) {
    String applicationPath = (String) servletContext.getAttribute(APP_PATH_ATTR_NAME);

    if (applicationPath == null) {
      return "";

    } else {
      return applicationPath;

    }
  }

  /**
   * Sets an application path into Spring Boot's servlet context.
   *
   * @param applicationPath to be set into Spring Boot's servlet context
   * @param servletContext of Spring Boot the application path should be set into
   */
  public static void setAppPath(String applicationPath, ServletContext servletContext) {
    servletContext.setAttribute(APP_PATH_ATTR_NAME, applicationPath);
  }

  /**
   * @return whether the web application has already successfully been sent to
   *         the engine as telemetry info or not.
   */
  public static boolean isTelemetryDataSentAlready(String webappName, String engineName, ServletContext servletContext) {
    return servletContext.getAttribute(buildTelemetrySentAttribute(webappName, engineName)) != null;
  }

  /**
   * Marks the web application as successfully sent to the engine as telemetry
   * info
   */
  public static void setTelemetryDataSent(String webappName, String engineName, ServletContext servletContext) {
    servletContext.setAttribute(buildTelemetrySentAttribute(webappName, engineName), true);
  }

  protected static String buildTelemetrySentAttribute(String webappName, String engineName) {
    return SUCCESSFUL_ET_ATTR_NAME + "." + webappName + "." + engineName;
  }

  /**
   * Sets {@param cacheTimeToLive} in the {@link AuthenticationFilter} to be used on initial login authentication.
   * See {@link AuthenticationFilter#doFilter(ServletRequest, ServletResponse, FilterChain)}
   */
  public static void setCacheTTLForLogin(long cacheTimeToLive, ServletContext servletContext) {
    servletContext.setAttribute(AUTH_CACHE_TTL_ATTR_NAME, cacheTimeToLive);
  }

  /**
   * Returns {@code authCacheValidationTime} from servlet context to be used on initial login authentication.
   * See {@link UserAuthenticationResource#doLogin(String, String, String, String)}
   */
  public static Date getAuthCacheValidationTime(ServletContext servletContext) {
    Long cacheTimeToLive = (Long) servletContext.getAttribute(AUTH_CACHE_TTL_ATTR_NAME);

    if (cacheTimeToLive != null) {
      return new Date(ClockUtil.getCurrentTime().getTime() + cacheTimeToLive);

    } else {
      return null;

    }
  }

}
