package com.camunda.fox.cycle.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.camunda.fox.cycle.connector.ConnectorLoginMode;

@Entity
@Table(name = "cy_connector_config")
public class ConnectorConfiguration extends AbstractEntity {

  private static final long serialVersionUID = 1L;

  @Column
  private String globalUser;
  
  @Column
  private String globalPassword;
  
  /**
   * Name of the configuration
   */
  @Column
  private String name;
  
  /**
   * Name of the backing connector (e.g. VFS connector)
   */
  @Column
  private String connectorName;

  /**
   * Class name of the backing connector (e.g. some.package.VFSConnector)
   */
  @Column
  private String connectorClass;
  
  @Enumerated(EnumType.STRING)
  private ConnectorLoginMode loginMode;

  @ElementCollection(fetch=FetchType.EAGER)
  @MapKeyColumn(name = "name")
  @Column(name = "value")
  @CollectionTable(name = "cy_connector_attributes", joinColumns = @JoinColumn(name = "configuration_id"))
  private Map<String, String> properties = new HashMap<String, String>();

  @OneToMany(cascade={ CascadeType.PERSIST, CascadeType.REMOVE }, mappedBy="connectorConfiguration")
  private List<ConnectorCredentials> credentials;
  
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

  public String getConnectorClass() {
    return connectorClass;
  }

  public void setConnectorClass(String connectorClass) {
    this.connectorClass = connectorClass;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
  public ConnectorLoginMode getLoginMode() {
    return loginMode;
  }

  public void setLoginMode(ConnectorLoginMode loginMode) {
    this.loginMode = loginMode;
  }
  
  public void setConnectorName(String connectorName) {
    this.connectorName = connectorName;
  }

  public String getConnectorName() {
    return connectorName;
  }
}
