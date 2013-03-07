package org.camunda.bpm.security.web;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.camunda.bpm.security.UserIdentity;


/**
 *
 * @author nico.rehwaldt
 */
public class SecurityWrappedRequest extends HttpServletRequestWrapper {
  
  private final UserIdentity identity;
  
  private final UserPrincipal principal;

  public SecurityWrappedRequest(HttpServletRequest request, UserIdentity identity) {
    super(request);
    
    this.identity = identity;
    
    this.principal = new UserPrincipal(identity.getName());
  }

  @Override
  public boolean isUserInRole(String role) {
    if ("user".equals(role)) {
      return true;
    }
    
    if ("admin".equals(role)) {
      return identity.isAdmin();
    } else {
      return false;
    }
  }

  @Override
  public Principal getUserPrincipal() {
    return principal;
  }
  
  private static class UserPrincipal implements Principal {

    private final String name;

    public UserPrincipal(String name) {
      this.name = name;
    }
    @Override
    public String getName() {
      return name;
    }
    
  }
}
