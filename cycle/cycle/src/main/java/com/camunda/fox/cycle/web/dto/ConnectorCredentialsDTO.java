package com.camunda.fox.cycle.web.dto;

import java.util.ArrayList;
import java.util.List;

import com.camunda.fox.cycle.entity.ConnectorCredentials;

/**
 *
 * @author nico.rehwaldt
 */
public class ConnectorCredentialsDTO {
  
  private long id;
  
  private String username;
  private String password;
  
  private long connectorId = -1;
  private long userId = -1;

  public ConnectorCredentialsDTO() { }
  
  public ConnectorCredentialsDTO(ConnectorCredentials connectorCredentials) {
    this.id = connectorCredentials.getId();
    this.username = connectorCredentials.getUsername();
    this.connectorId = connectorCredentials.getConnectorConfiguration().getId();
    this.userId = connectorCredentials.getUser().getId();
  }
  
  public long getId() {
    return id;
  }

  public void setId(long id) {
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
  
  public long getConnectorId() {
    return connectorId;
  }

  public void setConnectorId(long connectorId) {
    this.connectorId = connectorId;
  }
  
  public long getUserId() {
    return userId;
  }
  
  public void setUserId(long userId) {
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
