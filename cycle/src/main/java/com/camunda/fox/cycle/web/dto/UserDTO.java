package com.camunda.fox.cycle.web.dto;

import java.util.ArrayList;
import java.util.List;
import com.camunda.fox.cycle.entity.User;

/**
 *
 * @author nico.rehwaldt
 */
public class UserDTO {
  
  private Long id;
  private String name;
  private String email;

  public UserDTO(User user) {
    this.id = user.getId();
    this.name = user.getName();
    this.email = user.getEmail();
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setEmail(String email) {
    this.email = email;
  }
  
  // static helpers /////////////////////////////////////////
  
  public static UserDTO wrap(User user) {
    return new UserDTO(user);
  }

  public static List<UserDTO> wrapAll(List<User> users) {
    ArrayList<UserDTO> dtos = new ArrayList<UserDTO>();
    
    for (User user: users) {
      dtos.add(UserDTO.wrap(user));
    }
    
    return dtos;
  }
}
