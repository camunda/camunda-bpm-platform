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
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Response;

import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.webapp.impl.security.filter.util.CookieConstants;
import org.camunda.bpm.webapp.impl.security.filter.util.CsrfConstants;
import org.camunda.bpm.webapp.impl.util.ServletContextUtil;

/**
 * Provides basic CSRF protection implementing a Same Origin Standard Header verification (step 1)
 * and a Synchronization Token with a cookie-stored token on the front-end.
 *
 * <pre>
 * Positive scenario:
 *           Client                            Server
 *              |                                 |
 *              | GET Fetch Request              \| JSESSIONID
 *              |---------------------------------| X-CSRF-Token
 *              |                                /| pair generation
 *              |/Response to Fetch Request       |
 *              |---------------------------------|
 * JSESSIONID   |\                                |
 * X-CSRF-Token |                                 |
 * pair cached  | POST Request with valid token  \| JSESSIONID
 *              | header                          |
 *              |---------------------------------| X-CSRF-Token
 *              |                                /| pair validation
 *              |/ Response to POST Request       |
 *              |---------------------------------|
 *              |\                                |
 *
 * Negative scenario:
 *           Client                            Server
 *              |                                 |
 *              | POST Request without token      | JSESSIONID
 *              | header                         \| X-CSRF-Token
 *              |---------------------------------| pair validation
 *              |                                /|
 *              |/Request is rejected             |
 *              |---------------------------------|
 *              |\                                |
 *
 *           Client                            Server
 *              |                                 |
 *              | POST Request with invalid token\| JSESSIONID
 *              |---------------------------------| X-CSRF-Token
 *              |                                /| pair validation
 *              |/Request is rejected             |
 *              |---------------------------------|
 *              |\                                |
 * </pre>
 *
 * <i>Parts of this code were ported from the <code>CsrfPreventionFilter</code> class
 * of Apache Tomcat. Furthermore, the <code>RestCsrfPreventionFilter</code> class from
 * the same codebase was used as a guideline.</i>
 *
 * @author Nikola Koevski
 */
public class CsrfPreventionFilter implements Filter {

  private String randomClass = SecureRandom.class.getName();

  private Random randomSource;

  private URL targetOrigin;

  private int denyStatus = HttpServletResponse.SC_FORBIDDEN;

  protected final Set<String> entryPoints = new HashSet<>();

  protected CookieConfigurator cookieConfigurator = new CookieConfigurator();

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    try {

      String newRandomClass = filterConfig.getInitParameter("randomClass");
      if (!isBlank(newRandomClass)) {
        setRandomClass(newRandomClass);
      }

      Class<?> clazz = Class.forName(randomClass);
      randomSource = (Random) clazz.getConstructor().newInstance();

      String targetOrigin = filterConfig.getInitParameter("targetOrigin");
      if (!isBlank(targetOrigin)) {
        setTargetOrigin(targetOrigin);
      }

      String customDenyStatus = filterConfig.getInitParameter("denyStatus");
      if (!isBlank(customDenyStatus)) {
        setDenyStatus(Integer.valueOf(customDenyStatus));
      }

      String customEntryPoints = filterConfig.getInitParameter("entryPoints");
      if (!isBlank(customEntryPoints)) {
        setEntryPoints(customEntryPoints);
      }

      cookieConfigurator.parseParams(filterConfig);

    } catch (ClassNotFoundException e) {
      throw new ServletException("Cannot instantiate CSRF Prevention filter: Random class not found.", e);
    } catch (InstantiationException e) {
      throw new ServletException("Cannot instantiate CSRF Prevention filter: cannot instantiate provided Random class", e);
    } catch (InvocationTargetException e) {
      throw new ServletException("Cannot instantiate CSRF Prevention filter: cannot instantiate provided Random class", e);
    } catch (NoSuchMethodException e) {
      throw new ServletException("Cannot instantiate CSRF Prevention filter: cannot instantiate provided Random class", e);
    } catch (IllegalAccessException e) {
      throw new ServletException("Cannot instantiate CSRF Prevention filter: Random class constructor not accessible", e);
    } catch (MalformedURLException e) {
      throw new ServletException("CSRFPreventionFilter: Could not read target origin URL: " + e.getMessage());
    }
  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

    HttpServletRequest request = (HttpServletRequest) servletRequest;
    HttpServletResponse response = (HttpServletResponse) servletResponse;

    if (!isNonModifyingRequest(request)) {
      boolean isTokenValid = doSameOriginStandardHeadersVerification(request, response)
        && doTokenValidation(request, response);

      if (!isTokenValid) {
        return;
      }
    } else {
      // Fetch request -> provide new token
      setCSRFToken(request, response);
    }

    filterChain.doFilter(request, response);
  }

  /**
   * Validates the provided CSRF token value from
   * the request with the session CSRF token value.
   *
   * @param request
   * @param response
   * @return true if the token is valid
   * @throws IOException
   */
  protected boolean doTokenValidation(HttpServletRequest request, HttpServletResponse response) throws IOException {

    HttpSession session = request.getSession();
    String tokenHeader = getCSRFTokenHeader(request);
    String tokenSession = (String) getCSRFTokenSession(session);
    boolean isValid = true;

    if (isBlank(tokenHeader)) {
      session.invalidate();
      response.setHeader(CsrfConstants.CSRF_TOKEN_HEADER_NAME, CsrfConstants.CSRF_TOKEN_HEADER_REQUIRED);
      response.sendError(getDenyStatus(), "CSRFPreventionFilter: Token provided via HTTP Header is absent/empty.");
      isValid = false;
    } else if (isBlank(tokenSession) || !tokenSession.equals(tokenHeader)) {
      session.invalidate();
      response.sendError(getDenyStatus(), "CSRFPreventionFilter: Invalid HTTP Header Token.");
      isValid = false;
    }

    return isValid;
  }

  /**
   * Validates if the Origin/Referer header matches the provided target origin.
   *
   * @param request
   * @param response
   * @return true if the values match
   * @throws IOException
   */
  protected boolean doSameOriginStandardHeadersVerification(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // if target origin is not set, skip Same Origin with Standard Headers Verification
    if (getTargetOrigin() == null) {
      return true;
    }

    String source = request.getHeader("Origin");
    if (this.isBlank(source)) {
      //If empty then fallback on "Referer" header
      source = request.getHeader("Referer");
      //If this one is empty too, an error is reported
      if (this.isBlank(source)) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRFPreventionFilter: ORIGIN and REFERER request headers are not present.");
        return false;
      }
    }

    //Compare the source against the expected target origin
    URL sourceURL = new URL(source);
    if (!getTargetOrigin().getProtocol().equals(sourceURL.getProtocol())
      || !getTargetOrigin().getHost().equals(sourceURL.getHost())
      || getTargetOrigin().getPort() != sourceURL.getPort()) {

      //If any part of the URL doesn't match, an error is reported
      response.sendError(HttpServletResponse.SC_FORBIDDEN, String.format("CSRFPreventionFilter: Protocol/Host/Port does not fully match: (%s != %s) ", getTargetOrigin(), sourceURL));
      return false;
    }

    return true;
  }

  /**
   * Generates a new CSRF Token which is persisted in the session.
   * How the token is forwarded to the client and how it will be
   * persisted there is not covered by this method.
   *
   * @param request
   * @return the token string for client side handling
   */
  protected void setCSRFToken(HttpServletRequest request, HttpServletResponse response) {
    HttpSession session = request.getSession();
    Object sessionMutex = getSessionMutex(session);

    if (session.getAttribute(CsrfConstants.CSRF_TOKEN_SESSION_ATTR_NAME) == null) {

      synchronized (sessionMutex) {

        if (session.getAttribute(CsrfConstants.CSRF_TOKEN_SESSION_ATTR_NAME) == null) {
          String token = generateCSRFToken();

          String cookieName = cookieConfigurator.getCookieName(CsrfConstants.CSRF_TOKEN_DEFAULT_COOKIE_NAME);
          String csrfCookieValue = cookieName + "=" + token;

          String cookiePath = getCookiePath(request);
          csrfCookieValue += CsrfConstants.CSRF_PATH_FIELD_NAME + cookiePath;

          session.setAttribute(CsrfConstants.CSRF_TOKEN_SESSION_ATTR_NAME, token);

          csrfCookieValue += cookieConfigurator.getConfig();

          response.addHeader(CookieConstants.SET_COOKIE_HEADER_NAME, csrfCookieValue);

          response.setHeader(CsrfConstants.CSRF_TOKEN_HEADER_NAME, token);
        }
      }
    }
  }

  protected String getCookiePath(HttpServletRequest request) {
    ServletContext servletContext = request.getServletContext();
    String applicationPath = ServletContextUtil.getAppPath(servletContext);

    String contextPath = request.getContextPath();
    String cookiePath = contextPath + applicationPath;

    if (!cookiePath.isEmpty()) {
      return cookiePath;

    } else {
      return "/";

    }
  }

  public URL getTargetOrigin() {
    return targetOrigin;
  }

  /**
   * Target origin is the application expected deployment domain, i.e. the domain
   * name through which the webapps are accessed. If nothing is set, the "Same Origin
   * with Standard Headers" verification is not performed.
   *
   * @param targetOrigin The application's domain name together with the protocol
   *                     and port (ex. http://example.com:8080)
   * @throws MalformedURLException
   */
  public void setTargetOrigin(String targetOrigin) throws MalformedURLException {
    this.targetOrigin = new URL(targetOrigin);
  }

  /**
   * Entry points are URLs that will not be tested for the presence of a valid
   * token. They are used to provide a way to navigate back to a protected
   * application after navigating away from it. Entry points will be limited
   * to HTTP GET requests and should not trigger any security sensitive
   * actions.
   *
   * @param entryPoints
   *            Comma separated list of URLs to be configured as
   *            entry points.
   */
  public void setEntryPoints(String entryPoints) {
    this.entryPoints.addAll(parseURLs(entryPoints));
  }

  /**
   * @return the response status code that is used to reject a denied request.
   */
  public int getDenyStatus() {
    return denyStatus;
  }

  /**
   * Sets the response status code that is used to reject denied request.
   * If none is set, the default value of 403 will be used.
   *
   * @param denyStatus
   *            HTTP status code
   */
  public void setDenyStatus(int denyStatus) {
    this.denyStatus = denyStatus;
  }

  public String getRandomClass() {
    return randomClass;
  }

  /**
   * Sets the name of the class to use to generate tokens. The class must
   * be an instance of `java.util.Random`. If not set, the default value
   * of `java.security.SecureRandom` will be used.
   *
   * @param randomClass
   *            The name of the class
   */
  public void setRandomClass(String randomClass) {
    this.randomClass = randomClass;
  }

  @Override
  public void destroy() {
  }

  /**
   * Determine if the request a non-modifying request. A non-modifying
   * request is one that is either a 'HTTP GET/OPTIONS/HEAD' request, or
   * is allowed explicitly through the 'entryPoints' parameter in the web.xml
   *
   * @return true if the request is a non-modifying request
   * */
  protected boolean isNonModifyingRequest(HttpServletRequest request) {
    return CsrfConstants.CSRF_NON_MODIFYING_METHODS_PATTERN.matcher(request.getMethod()).matches()
      || entryPoints.contains(getRequestedPath(request));
  }

  /**
   * Generate a one-time token for authenticating subsequent
   * requests.
   *
   * @return the generated token
   */
  protected String generateCSRFToken() {
    byte random[] = new byte[16];

    // Render the result as a String of hexadecimal digits
    StringBuilder buffer = new StringBuilder();

    randomSource.nextBytes(random);

    for (int j = 0; j < random.length; j++) {
      byte b1 = (byte) ((random[j] & 0xf0) >> 4);
      byte b2 = (byte) (random[j] & 0x0f);
      if (b1 < 10) {
        buffer.append((char) ('0' + b1));
      } else {
        buffer.append((char) ('A' + (b1 - 10)));
      }
      if (b2 < 10) {
        buffer.append((char) ('0' + b2));
      } else {
        buffer.append((char) ('A' + (b2 - 10)));
      }
    }

    return buffer.toString();
  }

  private Object getCSRFTokenSession(HttpSession session) {
    return session.getAttribute(CsrfConstants.CSRF_TOKEN_SESSION_ATTR_NAME);
  }

  private String getCSRFTokenHeader(HttpServletRequest request) {
    return request.getHeader(CsrfConstants.CSRF_TOKEN_HEADER_NAME);
  }

  private Object getSessionMutex(HttpSession session) {
    if (session == null) {
      throw new InvalidRequestException(Response.Status.BAD_REQUEST, "HttpSession is missing");
    }

    Object mutex =  session.getAttribute(CsrfConstants.CSRF_SESSION_MUTEX);
    if (mutex == null) {
      mutex = session;
    }

    return mutex;
  }

  private boolean isBlank(String s) {
    return s == null || s.trim().isEmpty();
  }

  private String getRequestedPath(HttpServletRequest request) {
    String path = request.getServletPath();

    if (request.getPathInfo() != null) {
      path = path + request.getPathInfo();
    }

    return path;
  }

  private Set<String> parseURLs(String urlString) {
    Set<String> urlSet = new HashSet<>();

    if (urlString != null && !urlString.isEmpty()) {
      String values[] = urlString.split(",");
      for (String value : values) {
        urlSet.add(value.trim());
      }
    }

    return urlSet;
  }
}
