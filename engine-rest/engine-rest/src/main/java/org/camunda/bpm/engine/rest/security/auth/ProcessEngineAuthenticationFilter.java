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
package org.camunda.bpm.engine.rest.security.auth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.rest.dto.ExceptionDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.impl.NamedProcessEngineRestServiceImpl;
import org.camunda.bpm.engine.rest.util.EngineUtil;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * <p>
 * Servlet filter to plug in authentication.
 * </p>
 *
 * <p>Valid init-params:</p>
 * <table>
 * <thead>
 *   <tr><th>Parameter</th><th>Required</th><th>Expected value</th></tr>
 * <thead>
 * <tbody>
 *    <tr><td>{@value #AUTHENTICATION_PROVIDER_PARAM}</td><td>yes</td><td>An implementation of {@link AuthenticationProvider}</td></tr>
 *    <tr>
 *      <td>{@value #SERVLET_PATH_PREFIX}</td>
 *      <td>no</td>
 *      <td>The expected servlet path. Should only be set, if the underlying JAX-RS application is not deployed as a servlet (e.g. Resteasy allows deployments
 *      as a servlet filter). Value has to match what would be the {@link HttpServletRequest#getServletPath()} if it was deployed as a servlet.</td></tr>
 * </tbody>
 * </table>
 *
 * @author Thorben Lindhauer
 */
public class ProcessEngineAuthenticationFilter implements Filter {

  // regexes for urls that may be accessed unauthorized
  protected static final Pattern[] WHITE_LISTED_URL_PATTERNS = new Pattern[] {
    Pattern.compile("^" + NamedProcessEngineRestServiceImpl.PATH + "/?")
  };

  protected static final Pattern ENGINE_REQUEST_URL_PATTERN = Pattern.compile("^" + NamedProcessEngineRestServiceImpl.PATH + "/(.*?)(/|$)");
  protected static final String DEFAULT_ENGINE_NAME = "default";

  // init params
  public static final String AUTHENTICATION_PROVIDER_PARAM = "authentication-provider";
  public static final String SERVLET_PATH_PREFIX = "rest-url-pattern-prefix";

  protected AuthenticationProvider authenticationProvider;
  protected String servletPathPrefix;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    String authenticationProviderClassName = filterConfig.getInitParameter(AUTHENTICATION_PROVIDER_PARAM);

    if (authenticationProviderClassName == null) {
      throw new ServletException("Cannot instantiate authentication filter: no authentication provider set. init-param " + AUTHENTICATION_PROVIDER_PARAM + " missing");
    }

    try {
      Class<?> authenticationProviderClass = Class.forName(authenticationProviderClassName);
      authenticationProvider = (AuthenticationProvider) authenticationProviderClass.newInstance();
    } catch (ClassNotFoundException e) {
      throw new ServletException("Cannot instantiate authentication filter: authentication provider not found", e);
    } catch (InstantiationException e) {
      throw new ServletException("Cannot instantiate authentication filter: cannot instantiate authentication provider", e);
    } catch (IllegalAccessException e) {
      throw new ServletException("Cannot instantiate authentication filter: constructor not accessible", e);
    } catch (ClassCastException e) {
      throw new ServletException("Cannot instantiate authentication filter: authentication provider does not implement interface " +
          AuthenticationProvider.class.getName(), e);
    }

    servletPathPrefix = filterConfig.getInitParameter(SERVLET_PATH_PREFIX);
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {

    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse resp = (HttpServletResponse) response;

    String servletPath = servletPathPrefix;
    if (servletPath == null) {
      servletPath = req.getServletPath();
    }
    String requestUrl = req.getRequestURI().substring(req.getContextPath().length() + servletPath.length());

    boolean requiresEngineAuthentication = requiresEngineAuthentication(requestUrl);

    if (!requiresEngineAuthentication) {
      chain.doFilter(request, response);
      return;
    }

    String engineName = extractEngineName(requestUrl);
    ProcessEngine engine = getAddressedEngine(engineName);

    if (engine == null) {
      resp.setStatus(Status.NOT_FOUND.getStatusCode());
      ExceptionDto exceptionDto = new ExceptionDto();
      exceptionDto.setType(InvalidRequestException.class.getSimpleName());
      exceptionDto.setMessage("Process engine " + engineName + " not available");
      ObjectMapper objectMapper = new ObjectMapper();

      resp.setContentType(MediaType.APPLICATION_JSON);
      objectMapper.writer().writeValue(resp.getWriter(), exceptionDto);
      resp.getWriter().flush();

      return;
    }

    AuthenticationResult authenticationResult = authenticationProvider.extractAuthenticatedUser(req, engine);

    if (authenticationResult.isAuthenticated()) {
      try {
        String authenticatedUser = authenticationResult.getAuthenticatedUser();
        List<String> groups = authenticationResult.getGroups();
        List<String> tenants = authenticationResult.getTenants();
        setAuthenticatedUser(engine, authenticatedUser, groups, tenants);
        chain.doFilter(request, response);
      } finally {
        clearAuthentication(engine);
      }
    } else {
      resp.setStatus(Status.UNAUTHORIZED.getStatusCode());
      authenticationProvider.augmentResponseByAuthenticationChallenge(resp, engine);
    }

  }

  @Override
  public void destroy() {

  }

  protected void setAuthenticatedUser(ProcessEngine engine, String userId, List<String> groupIds, List<String> tenantIds) {
    if (groupIds == null) {
      groupIds = getGroupsOfUser(engine, userId);
    }

    if (tenantIds == null) {
      tenantIds = getTenantsOfUser(engine, userId);
    }

    engine.getIdentityService().setAuthentication(userId, groupIds, tenantIds);
  }

  protected List<String> getGroupsOfUser(ProcessEngine engine, String userId) {
    List<Group> groups = engine.getIdentityService().createGroupQuery()
      .groupMember(userId)
      .list();

    List<String> groupIds = new ArrayList<String>();
    for (Group group : groups) {
      groupIds.add(group.getId());
    }
    return groupIds;
  }

  protected List<String> getTenantsOfUser(ProcessEngine engine, String userId) {
    List<Tenant> tenants = engine.getIdentityService().createTenantQuery()
      .userMember(userId)
      .includingGroupsOfUser(true)
      .list();

    List<String> tenantIds = new ArrayList<String>();
    for(Tenant tenant : tenants) {
      tenantIds.add(tenant.getId());
    }
    return tenantIds;
  }

  protected void clearAuthentication(ProcessEngine engine) {
    engine.getIdentityService().clearAuthentication();
  }

  protected boolean requiresEngineAuthentication(String requestUrl) {
    for (Pattern whiteListedUrlPattern : WHITE_LISTED_URL_PATTERNS) {
      Matcher matcher = whiteListedUrlPattern.matcher(requestUrl);
      if (matcher.matches()) {
        return false;
      }
    }

    return true;
  }

  /**
   * May not return null
   */
  protected String extractEngineName(String requestUrl) {

    Matcher matcher = ENGINE_REQUEST_URL_PATTERN.matcher(requestUrl);

    if (matcher.find()) {
      return matcher.group(1);
    } else {
      // any request that does not match a specific engine and is not an /engine request
      // is mapped to the default engine
      return DEFAULT_ENGINE_NAME;
    }
  }

  protected ProcessEngine getAddressedEngine(String engineName) {
    return EngineUtil.lookupProcessEngine(engineName);
  }

}
