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
  private String user;
  private String password;
  private Long connectorId;

  public ConnectorCredentialsDTO(ConnectorCredentials connectorCredentials) {
    this.id = connectorCredentials.getId();
    this.user = connectorCredentials.getUser();
    this.password = connectorCredentials.getPassword();
    this.connectorId = connectorCredentials.getConnectorConfiguration().getId();
  }
  
  public Long getId() {
    return id;
  }
  
  public String getUser() {
    return user;
  }
  
  public void setUser(String user) {
    this.user = user;
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
