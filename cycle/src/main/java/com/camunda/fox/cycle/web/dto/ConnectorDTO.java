package com.camunda.fox.cycle.web.dto;

import com.camunda.fox.cycle.api.connector.Connector;


public class ConnectorDTO {
  private String connectorId;
  private String name;
  
  public ConnectorDTO(Connector connector) {
    this.name = connector.getName();
    this.setConnectorId(connector.getConnectorId());
  }
  
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getConnectorId() {
    return connectorId;
  }

  public void setConnectorId(String connectorId) {
    this.connectorId = connectorId;
  }

}
