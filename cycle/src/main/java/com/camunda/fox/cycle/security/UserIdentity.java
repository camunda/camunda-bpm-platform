package com.camunda.fox.cycle.security;


import com.camunda.fox.cycle.entity.User;

/**
 *
 * @author nico.rehwaldt
 */
public class UserIdentity {
  
  private final User user;

  public UserIdentity(User user) {
    this.user = user;
  }

  public User getUser() {
    return user;
  }

  public String getName() {
    return user.getName();
  }
  
  public long getId() {
    return user.getId();
  }
  
  public boolean isAdmin() {
    return user.isAdmin();
  }
}
