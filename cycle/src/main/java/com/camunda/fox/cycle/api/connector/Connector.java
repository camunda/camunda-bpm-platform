package com.camunda.fox.cycle.api.connector;

import java.io.InputStream;
import java.util.List;

import com.camunda.fox.cycle.entity.ConnectorConfiguration;


public abstract class Connector {
  
  private String connectorId;
  private String name;
  private ConnectorConfiguration configuration;
  
  public abstract List<ConnectorNode> getChildren(ConnectorNode parent);
  
  public abstract ConnectorNode getRoot();
  
  public abstract InputStream getContent(ConnectorNode node);

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
  /**
   * The ConnectorID corresponds to the defining connectors in the spring context, each connector has a singleton instance
   * from which we get the class for our new session instances. We are using the name of this defining bean as the connector id.
   * This is not meant to be shown to the user!
   * 
   * @return
   */
  public String getConnectorId() {
    return connectorId;
  }

  public void setConnectorId(String connectorId) {
    this.connectorId = connectorId;
  }
  
  public void login(String userName, String password) {
  }
  
  public ConnectorConfiguration getConfiguration() {
    return configuration;
  }

  public void setConfiguration(ConnectorConfiguration configuration) {
    this.configuration = configuration;
  }
}
