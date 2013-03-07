package org.camunda.bpm.security.web;

import static org.camunda.bpm.security.web.util.WebUtil.isAjax;
import static org.camunda.bpm.security.web.util.WebUtil.isGET;
import static org.camunda.bpm.security.web.util.WebUtil.isPOST;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.camunda.bpm.cycle.security.IdentityHolder;
import org.camunda.bpm.security.UserIdentity;
import org.camunda.bpm.security.service.SecurityService;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;


/**
 *
 * @author nico.rehwaldt
 */
public class SecurityFilter implements Filter {
  
  private static Logger log = Logger.getLogger(SecurityFilter.class.getName());

  private WebApplicationContext context;
 
  public static final String IDENTITY_SESSION_KEY = "org.camunda.bpm.SecurityFilter.SESSION_KEY";
  public static final String PRE_AUTHENTICATION_URL = "org.camunda.bpm.SecurityFilter.LAST_REQUEST_URI";
  
  static final String NOP = "NOP";
  
  @Override
  public void init(FilterConfig config) throws ServletException {
    context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
  }

  @Override
  public void destroy() {
    
  }

  void setWebApplicationContext(WebApplicationContext context) {
    this.context = context;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    doFilterSecure((HttpServletRequest) request, (HttpServletResponse) response, chain);
  }
  
  void doFilterSecure(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
    UserIdentity identity = getAuthenticatedIdentity(request);
    
    // is the current user authenticated?
    // if yes, make that information available
    if (identity != null) {
      request = wrapAuthenticated(request, identity);
      IdentityHolder.setIdentity(null);
    } 
    // if not, perform security check which may result in a redirect
    else {
      String uri = performSecurityCheck(request.getRequestURI(), request, response);
      if (uri != null) {

        // handle special do nothing actions
        // needed in case of ajax requests where only a 
        // response status is returned
        if (uri.equals(NOP)) {
          return;
        }

        boolean forward = false;

        if (uri.startsWith("forward:")) {
          uri = uri.substring("forward:".length());
          forward = true;
        }

        uri = uri.replace("app:", request.getContextPath() + "/");
        if (forward) {
          request.getRequestDispatcher(uri).forward(request, response);
        } else {
          response.sendRedirect(uri);
        }
        return;
      }
    }
    chain.doFilter(request, response);
  }

  String performSecurityCheck(String requestUri, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    if (requiresAuthentication(requestUri)) {
      if (isAjax(request)) {
        response.sendError(401, "Authorization required");
        return NOP;
      } else {
        if (isGET(request)) {
          request.getSession().setAttribute(PRE_AUTHENTICATION_URL, request.getRequestURI());
        }
        return "forward:/app/login";
      }
    } else if (isLoginRequest(request)) {
      try {
        if (login(request,response)) {
          String preLoginUrl = (String) request.getSession().getAttribute(PRE_AUTHENTICATION_URL);
          if (preLoginUrl != null) {
            return preLoginUrl;
          } else {
            return "app:app/secured/view/index";
          }
        } else {
          return "app:app/login/error";
        }
      } catch(Exception e) {
        log.log(Level.SEVERE, e.getMessage(), e);
        return null;
      }
    } else if (isLogoutRequest(request)) {
      logout(request);
      return "app:app/login/loggedOut";
    }
    
    return null;
  }
  
  protected UserIdentity getAuthenticatedIdentity(HttpServletRequest request) {
    return (UserIdentity) request.getSession().getAttribute(IDENTITY_SESSION_KEY);
  }

  protected void setAuthenticatedIdentity(HttpServletRequest request, UserIdentity identity) {
    request.getSession().setAttribute(IDENTITY_SESSION_KEY, identity);
  }
  
  protected boolean isLoginRequest(HttpServletRequest request) {
    return requestUriMatches(request, "j_security_check") && isPOST(request);
  }

  private boolean isLogoutRequest(HttpServletRequest request) {
    return requestUriMatches(request, "app/login/logout");
  }

  protected boolean login(HttpServletRequest request, HttpServletResponse response) {
    String userName = request.getParameter("j_username");
    String password = request.getParameter("j_password");

    if (userName == null || password == null) {
      return false;
    }
    
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

  private boolean requiresAuthentication(String uri) {
    return uri.matches(".*/app/secured/.*");
  }

  private HttpServletRequest wrapAuthenticated(HttpServletRequest request, UserIdentity identity) {
    return new SecurityWrappedRequest(request, identity);
  }
  
  private boolean requestUriMatches(HttpServletRequest request, String uri) {
    return request.getRequestURI().matches(request.getContextPath() + "/" + uri);
  }
}
