package com.camunda.fox.security;


import com.camunda.fox.cycle.entity.User;

/**
 * Represents a currently logged in user identity
 * 
 * @author nico.rehwaldt
 */
public class UserIdentity {
  
  private String name;
  private Long id;
  private boolean admin;
  
  private User user;

  public UserIdentity(String name) {
    this.name = name;
  }
  
  public UserIdentity(User user) {
    this.name = user.getName();
    this.id = user.getId();
    this.admin = user.isAdmin();
    
    this.user = user;
  }

  public User getUser() {
    return user;
  }

  public String getName() {
    return name;
  }
  
  public long getId() {
    return id;
  }
  
  public boolean isAdmin() {
    return admin;
  }
}
