package com.camunda.fox.security;

import com.camunda.fox.cycle.security.IdentityHolder;

import org.springframework.stereotype.Component;

@Component
public class SecurityContext {
  
  public UserIdentity getUserIdentity() {
    return IdentityHolder.getIdentity();
  }
}
