package com.camunda.fox.tasklist;

import java.io.IOException;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.impl.identity.Authentication;

import com.camunda.fox.tasklist.api.TaskListIdentity;

@WebFilter(filterName = "SignedInFilter", urlPatterns = { "/app/*" })
public class SignedInFilter implements Filter {

  private final static Logger log = Logger.getLogger(SignedInFilter.class.getCanonicalName());

  @Inject
  private TaskListIdentity identity;

  @Override
  public void destroy() {
  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
    log.fine("Checking if user is signed in");
    if (!identity.isSignedIn()) {
      log.fine("Redirecting to sign in page");
      ((HttpServletResponse) servletResponse).sendRedirect(((HttpServletRequest) servletRequest).getContextPath() + "/signin.jsf?faces-redirect=true");
    } else {
      log.fine("User is signed in");
      filterChain.doFilter(servletRequest, servletResponse);
      notifyActivitiAuthenticator();
    }
  }

  private void notifyActivitiAuthenticator() {
    // do this for convenience to allow to use the default initiator mechanism: http://www.activiti.org/userguide/index.html#bpmnStartEvents
    log.fine("Set User in Activiti Authentication module to allow to capture inititator");    
    Authentication.setAuthenticatedUserId(identity.getCurrentUser().getUsername());    
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }

}
