package com.camunda.fox.security;

import com.camunda.fox.cycle.security.PrincipalHolder;
import java.security.Principal;

import org.springframework.stereotype.Component;

@Component
public class SecurityContext {
  public Principal getPrincipal() {
    return PrincipalHolder.getPrincipal();
  }
}
