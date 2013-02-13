package com.camunda.fox.security.aspect;


import com.camunda.fox.security.UnauthorizedException;
import com.camunda.fox.security.MissingPrivilegesException;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import com.camunda.fox.security.SecurityContext;
import com.camunda.fox.security.UserIdentity;

/**
 * 
 * @author nico.rehwaldt
 */
@Component
@Aspect
public class SecurityAspect {
  
  @Inject
  private SecurityContext securityContext;
  
  @Before("@annotation(rolesAllowed)")
  private void checkRolesAllowed(JoinPoint jp, RolesAllowed rolesAllowed) throws Throwable {
    UserIdentity user = securityContext.getUserIdentity();
    
    if (user == null) {
      throw new UnauthorizedException("not authenticated");
    }
    
    for (String role: rolesAllowed.value()) {
      if (role.equals("admin") && !user.isAdmin()) {
        throw new MissingPrivilegesException("admin privileges required");
      }
    }
  }
}
