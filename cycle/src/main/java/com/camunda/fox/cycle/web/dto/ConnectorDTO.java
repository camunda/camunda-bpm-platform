package com.camunda.fox.cycle.web.dto;

import com.camunda.fox.cycle.api.connector.Connector;


public class ConnectorDTO {
  private String name;
  
  public ConnectorDTO(Connector connector) {
    this.name = connector.getName();
  }
  
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
}
