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

  public CurrentUserDTO(long id, String name) {
    this.id = id;
    this.name = name;
  }
  
  public String getName() {
    return name;
  }

  public Long getId() {
    return id;
  }
}
