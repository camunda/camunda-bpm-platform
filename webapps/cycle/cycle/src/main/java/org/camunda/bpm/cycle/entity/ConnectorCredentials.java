package org.camunda.bpm.cycle.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="cy_connector_cred")
public class ConnectorCredentials extends AbstractEntity {
  
  private static final long serialVersionUID = 1L;
  
  private String username;
  private String password;
  
  @ManyToOne
  private ConnectorConfiguration connectorConfiguration;
  
  @ManyToOne
  private User user;

  public ConnectorCredentials() {
  }

  public ConnectorCredentials(String username, String password, ConnectorConfiguration configuration, User user) {
    this.username = username;
    this.password = password;
    this.connectorConfiguration = configuration;
    this.user = user;
  }
  
  public String getUsername() {
    return username;
  }
  
  public void setUsername(String username) {
    this.username = username;
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
  
  public User getUser() {
    return user;
  }
  
  public void setUser(User user) {
    this.user = user;
  }
  
}
