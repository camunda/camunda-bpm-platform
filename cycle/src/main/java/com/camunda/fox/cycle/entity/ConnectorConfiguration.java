package com.camunda.fox.cycle.entity;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "cy_connector_config", uniqueConstraints = { @UniqueConstraint(columnNames = { "connectorId", "id" }) })
public class ConnectorConfiguration extends AbstractEntity {

  private static final long serialVersionUID = 1L;

  private String connectorId;

  private String globalUser;
  private String globalPassword;

  @ElementCollection
  @MapKeyColumn(name = "name")
  @Column(name = "value")
  @CollectionTable(name = "cy_connector_attributes", joinColumns = @JoinColumn(name = "configuration_id"))
  private
  Map<String, String> properties = new HashMap<String, String>();

  public String getGlobalUser() {
    return globalUser;
  }

  public void setGlobalUser(String globalUser) {
    this.globalUser = globalUser;
  }

  public String getGlobalPassword() {
    return globalPassword;
  }

  public void setGlobalPassword(String globalPassword) {
    this.globalPassword = globalPassword;
  }

  public String getConnectorId() {
    return connectorId;
  }

  public void setConnectorId(String connectorId) {
    this.connectorId = connectorId;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

}
