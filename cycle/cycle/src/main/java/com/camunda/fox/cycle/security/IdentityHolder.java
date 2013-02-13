package com.camunda.fox.cycle.security;


import com.camunda.fox.security.UserIdentity;


public class IdentityHolder {
  
  private static ThreadLocal<UserIdentity> holder = new ThreadLocal<UserIdentity>();
  
  public static void setIdentity(UserIdentity identity) {
    holder.set(identity);
  }

  public static UserIdentity getIdentity() {
    return holder.get();
  }

  public static void clear() {
    holder.remove();    
  }
}
