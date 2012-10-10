package com.camunda.fox.cycle.web.dto;

import com.camunda.fox.cycle.connector.Connector;
import com.camunda.fox.cycle.connector.ConnectorLoginMode;

/**
 * Dataobject exposing connector details
 * 
 * @author nico.rehwaldt
 */
public class ConnectorDTO {
  
  private Long connectorId;
  private String name;
  private String loginMode;
  private String connectorName;
  private String user;
  private String password;
  
  public ConnectorDTO(Connector connector) {
    this.connectorId = connector.getConfiguration().getId();
    this.name = connector.getConfiguration().getLabel();
    if (connector.getConfiguration().getLoginMode() != null) {
      this.loginMode = connector.getConfiguration().getLoginMode().name();
    } else {
      this.loginMode = ConnectorLoginMode.LOGIN_NOT_REQUIRED.name();
    }
    this.connectorName = connector.getConfiguration().getLabel();
    this.user = connector.getConfiguration().getGlobalUser();
    this.password = connector.getConfiguration().getGlobalPassword();
  }
  
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Long getConnectorId() {
    return connectorId;
  }

  public void setConnectorId(Long connectorId) {
    this.connectorId = connectorId;
  }
  
  public String getConnectorName() {
    return connectorName;
  }
  
  public void setConnectorName(String connectorName) {
    this.connectorName = connectorName;
  }
  
  public String getLoginMode() {
    return loginMode;
  }
  
  public void setLoginMode(String loginMode) {
    this.loginMode = loginMode;
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
}
