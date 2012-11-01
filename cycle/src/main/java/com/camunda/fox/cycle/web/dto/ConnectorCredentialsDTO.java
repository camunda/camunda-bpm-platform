package com.camunda.fox.cycle.web.dto;

import java.util.ArrayList;
import java.util.List;

import com.camunda.fox.cycle.entity.ConnectorCredentials;

/**
 *
 * @author nico.rehwaldt
 */
public class ConnectorCredentialsDTO {
  
  private Long id;
  private String username;
  private String password;
  private Long connectorId;
  private Long userId;

  public ConnectorCredentialsDTO() { }
  
  public ConnectorCredentialsDTO(ConnectorCredentials connectorCredentials) {
    this.id = connectorCredentials.getId();
    this.username = connectorCredentials.getUsername();
    this.password = connectorCredentials.getPassword();
    this.connectorId = connectorCredentials.getConnectorConfiguration().getId();
    this.userId = connectorCredentials.getUser().getId();
  }
  
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }
  
  public String getUsername() {
    return username;
  }
  
  public void setUsername(String user) {
    this.username = user;
  }
  
  public String getPassword() {
    return password;
  }
  
  public void setPassword(String password) {
    this.password = password;
  }
  
  public Long getConnectorId() {
    return connectorId;
  }

  public void setConnectorId(Long connectorId) {
    this.connectorId = connectorId;
  }
  
  public Long getUserId() {
    return userId;
  }
  
  public void setUserId(Long userId) {
    this.userId = userId;
  }
  
  // static helpers /////////////////////////////////////////
  
  public static ConnectorCredentialsDTO wrap(ConnectorCredentials connectorCredentials) {
    return new ConnectorCredentialsDTO(connectorCredentials);
  }
  
  public static List<ConnectorCredentialsDTO> wrapAll(List<ConnectorCredentials> connectorCredentials) {
    ArrayList<ConnectorCredentialsDTO> dtos = new ArrayList<ConnectorCredentialsDTO>();
    
    for (ConnectorCredentials cc: connectorCredentials) {
      dtos.add(ConnectorCredentialsDTO.wrap(cc));
    }
    
    return dtos;
  }
}
