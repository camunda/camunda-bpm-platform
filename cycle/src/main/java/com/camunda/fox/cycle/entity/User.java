package com.camunda.fox.cycle.entity;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="cy_user")
public class User extends AbstractEntity {

  private static final long serialVersionUID = 1L;

  private String name;
  
  private String email;
  
  private String password;
  
  @OneToMany
  private List<ConnectorCredentials> connectorCredentials;

  private boolean isAdmin;
  
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public List<ConnectorCredentials> getConnectorCredentials() {
    return connectorCredentials;
  }

  public void setConnectorCredentials(List<ConnectorCredentials> connectorCredentials) {
    this.connectorCredentials = connectorCredentials;
  }
  
  public boolean isAdmin() {
    return isAdmin;
  }
  
  public void setAdmin(boolean isAdmin) {
    this.isAdmin = isAdmin;
  }
  
}
