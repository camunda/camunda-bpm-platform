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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.camunda.bpm.webapp.impl.security.filter.util.CookieConstants;

public class SessionCookieFilter implements Filter {

  protected CookieConfigurator cookieConfigurator = new CookieConfigurator();

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    cookieConfigurator.parseParams(filterConfig);
  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
      throws IOException, ServletException {
    if ((servletRequest instanceof HttpServletRequest) && (servletResponse instanceof HttpServletResponse)) {
      // create a session if none exists yet
      ((HttpServletRequest) servletRequest).getSession();
      // execute filter chain with a response wrapper that handles sameSite attributes
      filterChain.doFilter(servletRequest, new SameSiteResponseProxy((HttpServletResponse) servletResponse));
    }
  }

  @Override
  public void destroy() {
  }

  protected class SameSiteResponseProxy extends HttpServletResponseWrapper {

    protected HttpServletResponse response;

    public SameSiteResponseProxy(HttpServletResponse resp) {
      super(resp);
      response = resp;
    }

    @Override
    public void sendError(int sc) throws IOException {
      appendSameSiteIfMissing();
      super.sendError(sc);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
      appendSameSiteIfMissing();
      super.sendError(sc, msg);
    }

    @Override
    public void sendRedirect(String location) throws IOException {
      appendSameSiteIfMissing();
      super.sendRedirect(location);
    }

    @Override
    public PrintWriter getWriter() throws IOException {
      appendSameSiteIfMissing();
      return super.getWriter();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
      appendSameSiteIfMissing();
      return super.getOutputStream();
    }

    protected void appendSameSiteIfMissing() {
      Collection<String> cookieHeaders = response.getHeaders(CookieConstants.SET_COOKIE_HEADER_NAME);
      boolean firstHeader = true;
      String cookieHeaderStart = cookieConfigurator.getCookieName("JSESSIONID") + "=";
      for (String cookieHeader : cookieHeaders) {
        if (cookieHeader.startsWith(cookieHeaderStart)) {
          cookieHeader = cookieConfigurator.getConfig(cookieHeader);
        } 
        if (firstHeader) {
          response.setHeader(CookieConstants.SET_COOKIE_HEADER_NAME, cookieHeader);
          firstHeader = false;
        } else {
          response.addHeader(CookieConstants.SET_COOKIE_HEADER_NAME, cookieHeader);
        }
      }
    }
  }
}
