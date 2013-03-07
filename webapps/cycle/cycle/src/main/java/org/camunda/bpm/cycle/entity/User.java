package org.camunda.bpm.cycle.entity;

import java.util.List;

import javax.persistence.CascadeType;
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
  
  @OneToMany(cascade={ CascadeType.ALL }, orphanRemoval=true, mappedBy="user")
  private List<ConnectorCredentials> connectorCredentials;

  private boolean admin;

  public User() {
  }

  public User(String name, boolean admin) {
    this.name = name;
    this.admin = admin;
  }
  
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

  public boolean isAdmin() {
    return admin;
  }

  public void setAdmin(boolean admin) {
    this.admin = admin;
  }

  public List<ConnectorCredentials> getConnectorCredentials() {
    return connectorCredentials;
  }

  public void setConnectorCredentials(List<ConnectorCredentials> connectorCredentials) {
    this.connectorCredentials = connectorCredentials;
  }
}
