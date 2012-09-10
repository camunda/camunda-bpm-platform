package com.camunda.fox.cycle.web.dto;

import java.util.List;

import com.camunda.fox.cycle.api.connector.Connector;
import com.camunda.fox.cycle.api.connector.Connector.ConnectorContentType;


public class ConnectorDTO {
  
  private Long connectorId;
  private String name;
  private List<ConnectorContentType> supportedTypes;
  
  public ConnectorDTO(Connector connector) {
    this.connectorId = connector.getConfiguration().getId();
    this.name = connector.getConfiguration().getLabel();
    this.setSupportedTypes(connector.getSupportedTypes());
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

  public List<ConnectorContentType> getSupportedTypes() {
    return supportedTypes;
  }

  public void setSupportedTypes(List<ConnectorContentType> supportedTypes) {
    this.supportedTypes = supportedTypes;
  }

}
