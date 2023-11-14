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
package org.camunda.bpm.spring.boot.starter.webapp.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Servlet filter to ensure app paths always have a trailing slash. Before Spring Boot 3, missing trailing slashes
 * were handled automatically. This filter ensures the pre Spring Boot 3 behavior.
 *
 * @see https://github.com/spring-projects/spring-framework/issues/28552
 */
public class MapTrailingSlashFilter implements Filter {

  public static final List<String> REDIRECT_PATHS = List.of("/app", "/app/cockpit", "/app/admin", "/app/tasklist", "/app/welcome");

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    String requestURI = ((HttpServletRequest) request).getRequestURI();

    for (String path : REDIRECT_PATHS) {
      if(requestURI.endsWith(path)) {
        requestURI += "/";
        ((HttpServletResponse) response).sendRedirect(requestURI);
        return;
      }
    }
    chain.doFilter(request, response);
  }

}
