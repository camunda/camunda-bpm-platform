package com.camunda.fox.cycle.web.dto;

import com.camunda.fox.cycle.api.connector.Connector;


public class ConnectorDTO {
  private Long connectorId;
  private String name;
  
  public ConnectorDTO(Connector connector) {
    this.setConnectorId(connector.getConfiguration().getId());
    this.setName(connector.getConfiguration().getLabel());
  }
  
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Long getConnectorId() {
    return connectorId;
  }

  public void setConnectorId(Long connectorId) {
    this.connectorId = connectorId;
  }

}
