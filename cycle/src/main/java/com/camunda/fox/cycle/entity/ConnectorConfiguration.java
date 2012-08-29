package com.camunda.fox.cycle.entity;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;

@Entity
@Table(name="cy_connector_config")
public class ConnectorConfiguration {
  
  @Id 
  protected long id;
  
  private String globalUser;
  private String globalPassword;
  
  @ElementCollection
  @MapKeyColumn(name="name")
  @Column(name="value")
  @CollectionTable(name="cy_connector_attributes", joinColumns=@JoinColumn(name="configuration_id"))
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
  
}
