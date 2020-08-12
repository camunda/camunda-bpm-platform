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

import javax.servlet.ServletContext;

/**
 * With Camunda BPM 7.13 we introduced the application path prefix /camunda to Spring Boot.
 * The application path is set in Spring Boot's servlet context and is consumed by filters and
 * servlets of the Camunda BPM Webapp. This util class holds the methods to get and set the
 * application path.
 */
public class ServletContextUtil {

  protected static final String APP_PATH_ATTR_NAME =
    "org.camunda.bpm.spring.boot.starter.webapp.applicationPath";

  /**
   * Consumed by Camunda BPM CE & EE Webapp:
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

}
