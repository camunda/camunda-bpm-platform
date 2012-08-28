package com.camunda.fox.cycle.api.connector;

import java.util.List;


public abstract class Connector {
  private String connectorId;
  private String name;
  
  public abstract List<ConnectorNode> getChildren(ConnectorFolder folder);

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
}
