package com.camunda.fox.cycle.web.dto;

/**
 * This is a data object which exposes the current user to the 
 * client. 
 * 
 * @author nico.rehwaldt
 */
public class CurrentUserDTO {

  private long id;
  private String name;
  private boolean adminRole = false;

  public CurrentUserDTO(long id, String name, boolean adminRole) {
    this.id = id;
    this.name = name;
    this.adminRole = adminRole;
  }
  
  public String getName() {
    return name;
  }

  public Long getId() {
    return id;
  }
  
  public boolean isAdminRole() {
    return adminRole;
  }
}
