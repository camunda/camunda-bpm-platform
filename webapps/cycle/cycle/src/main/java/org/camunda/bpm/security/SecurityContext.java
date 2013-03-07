package org.camunda.bpm.security;


import org.camunda.bpm.cycle.security.IdentityHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityContext {
  
  public UserIdentity getUserIdentity() {
    return IdentityHolder.getIdentity();
  }
}
