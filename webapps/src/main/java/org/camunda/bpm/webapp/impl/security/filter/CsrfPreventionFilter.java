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
 *              | cookie                          |
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
 *              | cookie                         \| X-CSRF-Token
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

  protected static final Pattern DEFAULT_ENTRY_URL_PATTERN = Pattern.compile("^/api/admin/auth/user/.+/login/(cockpit|tasklist|admin|welcome|webapps)$");

  private final Set<String> entryPoints = new HashSet<String>();

  private URL targetOrigin;

  private int tokenCacheSize = 5;

  @Override public void init(FilterConfig filterConfig) throws ServletException {
    super.init(filterConfig);
    try {
      String targetOrigin = filterConfig.getInitParameter("targetOrigin");
      if (!isBlank(targetOrigin)) {
        setTargetOrigin(targetOrigin);
      }

      String tokenCacheSize = filterConfig.getInitParameter("tokenCacheSize");
      if (!isBlank(tokenCacheSize)) {
        int cacheSize = Integer.valueOf(tokenCacheSize);
        if (cacheSize > 0) {
          setTokenCacheSize(cacheSize);
        } else {
          throw new ServletException("CSRFPreventionFilter: Invalid CSRF Token cache size.");
        }
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
    HttpSession session = request.getSession();

    LRUCache<String> lruTokenCache = (session != null)?
      (LRUCache<String>) session.getAttribute(CsrfConstants.CSRF_TOKEN_SESSION_ATTR_NAME) : null;
    String cookieToken = retrieveCookieToken(request);

    if (lruTokenCache == null || cookieToken == null || !lruTokenCache.contains(cookieToken)) {
      response.sendError(getDenyStatus(), "CSRFPreventionFilter: incorrect or missing token in request.");
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
        response.sendError(HttpServletResponse.SC_FORBIDDEN,
          "CSRFPreventionFilter: ORIGIN and REFERER request headers are not present.");
        return false;
      }
    }

    //Compare the source against the expected target origin
    URL sourceURL = new URL(source);
    if (!this.targetOrigin.getProtocol().equals(sourceURL.getProtocol())
      || !this.targetOrigin.getHost().equals(sourceURL.getHost())
      || this.targetOrigin.getPort() != sourceURL.getPort()) {
      //If any part of the URL doesn't match, an error is reported
      response.sendError(HttpServletResponse.SC_FORBIDDEN,
          String.format("CSRFPreventionFilter: Protocol/Host/Port does not fully match: (%s != %s) ",
            this.targetOrigin, sourceURL));
      return false;
    }

    return true;
  }

  // The Token is sent through a Cookie, or if not possible, as a Request Header.
  protected String retrieveCookieToken(HttpServletRequest request) {
    String token = null;

    Cookie[] cookies = request.getCookies();
    for (Cookie cookie : cookies) {
      if (cookie.getName().equals(CsrfConstants.CSRF_TOKEN_COOKIE_NAME)) {
        token = cookie.getValue();
      }
    }

    // not really possible atm, but a good fallback practice
    if (token == null || token.isEmpty()) {
      token = request.getHeader(CsrfConstants.CSRF_TOKEN_HEADER_NAME);
    }

    return token;
  }

  // If the Request is a Fetch request, a new Token needs to be provided with the response.
  protected void fetchToken(HttpServletRequest request, HttpServletResponse response) {
    HttpSession session = request.getSession();

    LRUCache<String> lruTokenCache = (session.getAttribute(CsrfConstants.CSRF_TOKEN_SESSION_ATTR_NAME) != null)?
      (LRUCache<String>) session.getAttribute(CsrfConstants.CSRF_TOKEN_SESSION_ATTR_NAME) : new LRUCache<String>(this.tokenCacheSize);

    String token = lruTokenCache.getLatestToken();
    if (!lruTokenCache.isLatestTokenValid(500L)) {
      token = generateToken();
      lruTokenCache.add(token);
    }

    session.setAttribute(CsrfConstants.CSRF_TOKEN_SESSION_ATTR_NAME, lruTokenCache);
    Cookie csrfCookie = new Cookie(CsrfConstants.CSRF_TOKEN_COOKIE_NAME, token);
    csrfCookie.setPath("/camunda");
    response.addCookie(csrfCookie);
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

  /**
   * Sets the number of previously issued tokens that will be cached on a LRU
   * basis to support parallel requests, limited use of the refresh and back
   * in the browser and similar behaviors that may result in the submission
   * of a previous token rather than the current one. If not set, the default
   * value of 5 will be used.
   *
   * @param tokenCacheSize    The number of tokens to cache
   */
  public void setTokenCacheSize(int tokenCacheSize) {
    this.tokenCacheSize = tokenCacheSize;
  }

  protected static class LRUCache<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    // Although the internal implementation uses a Map, this cache
    // implementation is only concerned with the keys.
    private T latestToken;
    private long latestTokenCreationTime;
    private int cacheSize;
    private final Map<T,Long> cache;

    public LRUCache(final int cacheSize) {
      this.latestToken = null;
      this.latestTokenCreationTime = 0L;
      this.cacheSize = cacheSize;
      this.cache = new LinkedHashMap<T,Long>() {

        private static final long serialVersionUID = 1L;

        @Override
        protected boolean removeEldestEntry(Map.Entry<T,Long> eldest) {
          return size() > cacheSize;
        }
      };
    }

    public boolean isLatestTokenValid(long ttl) {
      synchronized (cache) {
        long now = Calendar.getInstance().getTimeInMillis();
        if (cache.size() >= cacheSize && (now - latestTokenCreationTime < ttl)) {
          return true;
        }

        return false;
      }
    }

    public void add(T key) {
      synchronized (cache) {
        latestToken = key;
        latestTokenCreationTime = Calendar.getInstance().getTimeInMillis();
        cache.put(latestToken, latestTokenCreationTime);
      }
    }

    public boolean contains(T key) {
      synchronized (cache) {
        return cache.containsKey(key);
      }
    }

    public T getLatestToken() {
      return latestToken;
    }
  }
}
