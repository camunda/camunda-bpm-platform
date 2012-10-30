package com.camunda.fox.cycle.web.dto;

import java.util.ArrayList;
import java.util.List;
import com.camunda.fox.cycle.entity.User;

/**
 *
 * @author nico.rehwaldt
 */
public class ConnectorCredentialDTO {
  
  private Long id;
  private String name;
  private String email;

  private String password;

  public ConnectorCredentialDTO(User user) {
    this.id = user.getId();
    this.name = user.getName();
    this.email = user.getEmail();
  }
  
  // static helpers /////////////////////////////////////////
  
  public static ConnectorCredentialDTO wrap(User user) {
    return new ConnectorCredentialDTO(user);
  }

  public static List<ConnectorCredentialDTO> wrapAll(List<User> users) {
    ArrayList<ConnectorCredentialDTO> dtos = new ArrayList<ConnectorCredentialDTO>();
    
    for (User user: users) {
      dtos.add(ConnectorCredentialDTO.wrap(user));
    }
    
    return dtos;
  }
}
