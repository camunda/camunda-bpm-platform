package com.camunda.fox.cycle.security;

import java.security.Principal;


public class PrincipalHolder {
  private static ThreadLocal<Principal> holder = new ThreadLocal<Principal>();
  
  public static void setPrincipal(Principal p) {
    holder.set(p);
  }

  public static Principal getPrincipal() {
    return holder.get();
  }
  
}
