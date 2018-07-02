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

package org.camunda.bpm.webapp.impl.security.filter;

import org.camunda.bpm.webapp.impl.security.filter.util.CsrfConstants;
import org.jboss.com.sun.corba.se.spi.protocol.CorbaServerRequestDispatcher;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

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
public class CsrfPreventionFilter extends BaseCsrfPreventionFilter {

  protected static final Pattern NON_MODIFYING_METHODS_PATTERN = Pattern.compile("GET|HEAD|OPTIONS");

  protected static final Pattern DEFAULT_ENTRY_URL_PATTERN = Pattern.compile("^/api/admin/auth/user/.+/login/(cockpit|tasklist|admin|welcome)$");

  private final Set<String> entryPoints = new HashSet<String>();

  private URL targetOrigin;

  @Override public void init(FilterConfig filterConfig) throws ServletException {
    super.init(filterConfig);
    try {
      String targetOrigin = filterConfig.getInitParameter("targetOrigin");
      if (!isBlank(targetOrigin)) {
        setTargetOrigin(targetOrigin);
      }

      String customEntryPoints = filterConfig.getInitParameter("entryPoints");
      if (!isBlank(customEntryPoints)) {
        setEntryPoints(customEntryPoints);
      }
    } catch (MalformedURLException e) {
      throw new ServletException("CSRFPreventionFilter: Could not read target origin URL: " + e.getMessage());
    }
  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

    HttpServletRequest request = (HttpServletRequest) servletRequest;
    HttpServletResponse response = (HttpServletResponse) servletResponse;

    boolean isNonModifyingRequest = isNonModifyingRequest(request);

    if (!isNonModifyingRequest) {
      // Not a fetch request -> validate token
      boolean isTokenValid = doSameOriginStandardHeadersVerification(request, response)
        && doTokenValidation(request, response);

      if (!isTokenValid) {
        return;
      }
    }

    if (isNonModifyingRequest){
      // Fetch request -> provide new token
      fetchToken(request, response);
    }

    filterChain.doFilter(request, response);
  }

  // Validate request token value with session token values
  protected boolean doTokenValidation(HttpServletRequest request, HttpServletResponse response) throws IOException {

    String tokenHeader = getCSRFTokenHeader(request);
    if (isBlank(tokenHeader)) {
      response.setHeader(CsrfConstants.CSRF_TOKEN_HEADER_NAME, CsrfConstants.CSRF_TOKEN_HEADER_REQUIRED);
      response.sendError(getDenyStatus(), "CSRFPreventionFilter: Token provided via HTTP Header is absent/empty.");
      return false;
    }

    HttpSession session = request.getSession();
    String tokenSession = (String) session.getAttribute(CsrfConstants.CSRF_TOKEN_SESSION_ATTR_NAME);
    if (isBlank(tokenSession) || !tokenSession.equals(tokenHeader)) {
      response.sendError(getDenyStatus(), "CSRFPreventionFilter: Invalid HTTP Header Token.");
      return false;
    }

    return true;
  }

  protected boolean doSameOriginStandardHeadersVerification(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // if target origin is not set, skip Same Origin with Standard Headers Verification
    if (targetOrigin == null) {
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
    if (!this.targetOrigin.getProtocol().equals(sourceURL.getProtocol())
      || !this.targetOrigin.getHost().equals(sourceURL.getHost())
      || this.targetOrigin.getPort() != sourceURL.getPort()) {
      //If any part of the URL doesn't match, an error is reported
      response.sendError(HttpServletResponse.SC_FORBIDDEN, String.format("CSRFPreventionFilter: Protocol/Host/Port does not fully match: (%s != %s) ", this.targetOrigin, sourceURL));
      return false;
    }

    return true;
  }

  protected Cookie getCSRFCookie(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (cookie.getName().equals(CsrfConstants.CSRF_TOKEN_COOKIE_NAME)) {
          return cookie;
        }
      }
    }

    return new Cookie(CsrfConstants.CSRF_TOKEN_COOKIE_NAME, null);
  }

  protected String getCSRFTokenHeader(HttpServletRequest request) {
    return request.getHeader(CsrfConstants.CSRF_TOKEN_HEADER_NAME);
  }

  // If the Request is a Fetch request, a new Token needs to be provided with the response.
  protected void fetchToken(HttpServletRequest request, HttpServletResponse response) {
    HttpSession session = request.getSession();

    if (session.getAttribute(CsrfConstants.CSRF_TOKEN_SESSION_ATTR_NAME) == null) {
      String token = generateToken();

      Cookie csrfCookie = getCSRFCookie(request);
      csrfCookie.setValue(token);
      csrfCookie.setPath(request.getContextPath());

      session.setAttribute(CsrfConstants.CSRF_TOKEN_SESSION_ATTR_NAME, token);
      response.addCookie(csrfCookie);
      response.setHeader(CsrfConstants.CSRF_TOKEN_HEADER_NAME, token);
    }
  }

  // A non-modifying request is one that is either a 'HTTP GET' request,
  // or is allowed explicitly through the 'entryPoints' parameter in the web.xml
  protected boolean isNonModifyingRequest(HttpServletRequest request) {
    return NON_MODIFYING_METHODS_PATTERN.matcher(request.getMethod()).matches()
        || DEFAULT_ENTRY_URL_PATTERN.matcher(getRequestedPath(request)).matches()
        || entryPoints.contains(getRequestedPath(request));
  }

  private String getRequestedPath(HttpServletRequest request) {
    String path = request.getServletPath();

    if (request.getPathInfo() != null) {
      path = path + request.getPathInfo();
    }

    return path;
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
   * @param entryPoints   Comma separated list of URLs to be configured as
   *                      entry points.
   */
  public void setEntryPoints(String entryPoints) {
    this.entryPoints.addAll(parseURLs(entryPoints));
  }

  private Set<String> parseURLs(String urlString) {
    Set<String> urlSet = new HashSet<String>();

    if (urlString != null && !urlString.isEmpty()) {
      String values[] = urlString.split(",");
      for (String value : values) {
        urlSet.add(value.trim());
      }
    }

    return urlSet;
  }
}
