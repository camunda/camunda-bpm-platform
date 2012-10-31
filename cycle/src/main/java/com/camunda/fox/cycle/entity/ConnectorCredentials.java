package com.camunda.fox.cycle.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="cy_connector_cred")
public class ConnectorCredentials extends AbstractEntity {
  
  private static final long serialVersionUID = 1L;
  
  private String user;
  private String password;
  
  @ManyToOne
  private ConnectorConfiguration connectorConfiguration;
  
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
  
  public ConnectorConfiguration getConnectorConfiguration() {
    return connectorConfiguration;
  }
  
  public void setConnectorConfiguration(ConnectorConfiguration connectorConfiguration) {
    this.connectorConfiguration = connectorConfiguration;
  }
  
}
