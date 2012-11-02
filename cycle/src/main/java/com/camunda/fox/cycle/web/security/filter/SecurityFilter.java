package com.camunda.fox.cycle.web.security.filter;

import com.camunda.fox.cycle.security.SecurityService;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.camunda.fox.cycle.security.UserIdentity;

/**
 *
 * @author nico.rehwaldt
 */
public class SecurityFilter implements Filter {

  private WebApplicationContext context;

  private static final String IDENTITY_SESSION_KEY = "com.camunda.fox.SecurityFilter.SESSION_KEY";
  private static final String PRE_AUTHENTICATION_URL = "com.camunda.fox.SecurityFilter.LAST_REQUEST_URI";
  
  @Override
  public void init(FilterConfig config) throws ServletException {
    context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
  }

  @Override
  public void destroy() {
    
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    performSecurityCheck((HttpServletRequest) request, (HttpServletResponse) response, chain);
  }

  private void performSecurityCheck(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
    
    UserIdentity identity = getAuthenticatedIdentity(request);
    
    if (identity != null) {
      request = wrapAuthenticated(request, identity);
    } else
    if (requiresAuthentication(request)) {
      if (isGET(request)) {
        request.getSession().setAttribute(PRE_AUTHENTICATION_URL, request.getRequestURI());
      }
      sendRedirect(request, response, "app/login");
      return;
    } else
    if (isLoginRequest(request)) {
      if (login(request)) {
        String preLoginUrl = (String) request.getSession().getAttribute(PRE_AUTHENTICATION_URL);
        if (preLoginUrl != null) {
          response.sendRedirect(preLoginUrl);
        } else {
          sendRedirect(request, response, "app/view/secured/index");
        }
      } else {
        sendRedirect(request, response, "app/view/login/error");
      }
      
      return;
    } else
    if (isLogoutRequest(request)) {
      logout(request);
    }
    
    // no redirect ...
    chain.doFilter(request, response);
  }
  
  protected UserIdentity getAuthenticatedIdentity(HttpServletRequest request) {
    return (UserIdentity) request.getSession().getAttribute(IDENTITY_SESSION_KEY);
  }

  protected void setAuthenticatedIdentity(HttpServletRequest request, UserIdentity identity) {
    request.getSession().setAttribute(IDENTITY_SESSION_KEY, identity);
  }
  
  private boolean isGET(HttpServletRequest request) {
    return "GET".equals(request.getMethod());
  }
  
  private boolean isPOST(HttpServletRequest request) {
    return "POST".equals(request.getMethod());
  }
  
  protected boolean isLoginRequest(HttpServletRequest request) {
    return requestUriMatches(request, "j_security_check") && isPOST(request);
  }

  private boolean isLogoutRequest(HttpServletRequest request) {
    return requestUriMatches(request, "app/login/logout");
  }

  protected boolean login(HttpServletRequest request) {
    String userName = request.getParameter("j_username");
    String password = request.getParameter("j_password");
    
    SecurityService securityService = context.getBean(SecurityService.class);
    UserIdentity identity = securityService.login(userName, password);
    if (identity != null) {
      setAuthenticatedIdentity(request, identity);
      return true;
    } else {
      return false;
    }
  }

  private void logout(HttpServletRequest request) {
    request.getSession().invalidate();
  }

  private boolean requiresAuthentication(HttpServletRequest request) {
    return requestUriMatches(request, "app/secured/.*");
  }

  private HttpServletRequest wrapAuthenticated(HttpServletRequest request, UserIdentity identity) {
    return new SecurityWrappedRequest(request, identity);
  }
  
  private boolean requestUriMatches(HttpServletRequest request, String uri) {
    return request.getRequestURI().matches(request.getContextPath() + "/" + uri);
  }

  private void sendRedirect(HttpServletRequest request, HttpServletResponse response, String uri) throws IOException {
    response.sendRedirect(request.getContextPath() + "/" + uri);
  }
  
}
