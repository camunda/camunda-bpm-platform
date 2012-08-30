package com.camunda.fox.cycle.security;

import java.security.Principal;

import org.springframework.stereotype.Component;

@Component
public class SecurityContext {
  public Principal getPrincipal() {
    return PrincipalHolder.getPrincipal();
  }
}
