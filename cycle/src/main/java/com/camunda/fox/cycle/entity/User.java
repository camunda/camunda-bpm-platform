package com.camunda.fox.cycle.entity;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="cy_user")
public class User extends AbstractEntity {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private String name;
  
  @OneToMany
  private List<ConnectorCredentials> connectorCredentials;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<ConnectorCredentials> getConnectorCredentials() {
    return connectorCredentials;
  }

  public void setConnectorCredentials(List<ConnectorCredentials> connectorCredentials) {
    this.connectorCredentials = connectorCredentials;
  }
}
