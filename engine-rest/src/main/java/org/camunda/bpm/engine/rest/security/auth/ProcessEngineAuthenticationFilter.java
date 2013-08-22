/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.Collection;
import java.util.Enumeration;
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
import org.camunda.bpm.engine.rest.ProcessEngineRestService;
import org.camunda.bpm.engine.rest.dto.ExceptionDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.util.EngineUtil;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * <p>
 * Servlet filter to plug in authentication. Expects an init-param {@link ProcessEngineAuthenticationFilter#AUTHENTICATION_PROVIDER_PARAM} that
 * provides a class that implements {@link AuthenticationProvider}.
 * </p>
 *
 * @author Thorben Lindhauer
 */
public class ProcessEngineAuthenticationFilter implements Filter {

  // regexes for urls that may be accessed unauthorized
  protected static final String[] WHITE_LISTED_URL_PATTERNS = new String[] {
    "^" + ProcessEngineRestService.PATH + "/?"
  };

  protected static final String ENGINE_REQUEST_URL_PATTERN = "^" + ProcessEngineRestService.PATH + "/(.*?)(/|$)";
  protected static final String DEFAULT_ENGINE_NAME = "default";

  // init params
  public static final String AUTHENTICATION_PROVIDER_PARAM = "authentication-provider";
  public static final String SERVLET_PATH_PREFIX = "rest-url-pattern-prefix";

  protected AuthenticationProvider authenticationProvider;
  protected String servletPathPrefix;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    String authenticationProviderClassName = filterConfig.getInitParameter(AUTHENTICATION_PROVIDER_PARAM);

    try {
      Class<?> authenticationProviderClass = Class.forName(authenticationProviderClassName);
      authenticationProvider = (AuthenticationProvider) authenticationProviderClass.newInstance();
    } catch (ClassNotFoundException e) {
      new ServletException("Cannot instantiate authentication filter: authentication provider not found", e);
    } catch (InstantiationException e) {
      new ServletException("Cannot instantiate authentication filter: cannot instantiate authentication provider", e);
    } catch (IllegalAccessException e) {
      new ServletException("Cannot instantiate authentication filter: constructor not accessible", e);
    } catch (ClassCastException e) {
      new ServletException("Cannot instantiate authentication filter: authentication provider does not implement interface " +
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
      setAuthenticatedUser(engine, authenticationResult.getAuthenticatedUser());
      try {
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

  protected void setAuthenticatedUser(ProcessEngine engine, String userId) {
    // get user's groups
    final List<Group> groupList = engine.getIdentityService().createGroupQuery()
      .groupMember(userId)
      .list();

    // transform into array of strings:
    List<String> groupIds = new ArrayList<String>();

    for (Group group : groupList) {
      groupIds.add(group.getId());
    }

    engine.getIdentityService().setAuthentication(userId, groupIds);
  }

  protected void clearAuthentication(ProcessEngine engine) {
    engine.getIdentityService().clearAuthentication();
  }

  protected boolean requiresEngineAuthentication(String requestUrl) {
    for (String whiteListedUrlPattern : WHITE_LISTED_URL_PATTERNS) {
      Pattern pattern = Pattern.compile(whiteListedUrlPattern);
      Matcher matcher = pattern.matcher(requestUrl);
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

    Pattern pattern = Pattern.compile(ENGINE_REQUEST_URL_PATTERN);
    Matcher matcher = pattern.matcher(requestUrl);

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
