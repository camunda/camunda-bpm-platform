package org.camunda.bpm.webapp.impl.security.auth;

import java.io.IOException;
import java.security.Principal;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Providers;

import org.camunda.bpm.engine.rest.util.ProvidersUtil;

/**
 * This Servlet filter relies on the Servlet container (application server) to
 * authenticate a user and only forward a request to the application upon
 * successful authentication.
 * 
 * It passes the username provided by the container through the Servlet API into
 * the Servlet session used by the Camunda REST API.
 *
 * @author Eberhard Heber
 * @author Falko Menge
 */
public class ContainerBasedAuthenticationFilter implements Filter {

  public static Pattern APP_PATTERN = Pattern.compile("/app/(cockpit|admin|tasklist|welcome)/([^/]+)/");
  public static Pattern API_ENGINE_PATTERN = Pattern.compile("/api/engine/engine/([^/]+)/.*");
  public static Pattern API_STATIC_PLUGIN_PATTERN = Pattern.compile("/api/(cockpit|admin|tasklist|welcome)/plugin/[^/]+/static/.*");
  public static Pattern API_PLUGIN_PATTERN = Pattern.compile("/api/(cockpit|admin|tasklist|welcome)/plugin/[^/]+/([^/]+)/.*");

  protected AuthenticationService userAuthentications;

  public void init(FilterConfig filterConfig) throws ServletException {
    userAuthentications = new AuthenticationService();
  }

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    final HttpServletRequest req = (HttpServletRequest) request;
    doAuthentication(req);
    chain.doFilter(request, response);
  }

  public void destroy() {
  }

  protected void doAuthentication(HttpServletRequest request) {
    // get authentication from session
    Authentications authentications = Authentications.getFromSession(request.getSession());

    String username = getUserName(request);
    String engineName = getEngineName(request);

    if (username != null && engineName != null && !isAuthenticated(authentications, engineName, username)) {
      Authentication authentication = createAuthentication(username, engineName);
      authentications.addAuthentication(authentication);
    }

  }

  protected String getUserName(HttpServletRequest request) {
    Principal principal = request.getUserPrincipal();

    String username = null;
    if (principal != null) {
      username = principal.getName();

      if (username != null && username.isEmpty()) {
        username = null;
      }
    }

    return username;
  }

  protected String getRequestUri(HttpServletRequest request) {
    String contextPath = request.getContextPath();
    return request.getRequestURI().substring(contextPath.length());
  }

  protected String getEngineName(HttpServletRequest request) {
    String requestUri = getRequestUri(request);

    Matcher appMatcher = APP_PATTERN.matcher(requestUri);
    if (appMatcher.matches()) {
      return appMatcher.group(2);
    }

    Matcher apiEngineMatcher = API_ENGINE_PATTERN.matcher(requestUri);
    if (apiEngineMatcher.matches()) {
      return apiEngineMatcher.group(1);
    }

    Matcher apiStaticPluginPattern = API_STATIC_PLUGIN_PATTERN.matcher(requestUri);
    if (request.getMethod().equals("GET") && apiStaticPluginPattern.matches()) {
      return null;
    }

    Matcher apiPluginPattern = API_PLUGIN_PATTERN.matcher(requestUri);
    if (apiPluginPattern.matches()) {
      return apiPluginPattern.group(2);
    }

    return null;
  }

  protected boolean isAuthenticated(Authentications authentications, String engineName, String username) {
    // For each process engine, there can be at most one authentication active in a given session.
    Authentication authentication = authentications.getAuthenticationForProcessEngine(engineName);
    return isAuthenticated(authentication, engineName, username);
  }

  protected boolean isAuthenticated(Authentication authentication, String engineName, String username) {
    return authentication != null && authentication.getProcessEngineName().equals(engineName) && authentication.getIdentityId().equals(username);
  }

  protected Authentication createAuthentication(String username, String engineName) {
    return userAuthentications.createAuthenticate(engineName, username);
  }

}