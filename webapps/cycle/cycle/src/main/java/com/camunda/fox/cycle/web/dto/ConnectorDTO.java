package com.camunda.fox.cycle.web.dto;

import com.camunda.fox.cycle.connector.Connector;

/**
 * Dataobject exposing connector details
 * 
 * @author nico.rehwaldt
 */
public class ConnectorDTO {
  
  private Long connectorId;
  private String name;
  private boolean isSupportsCommitMessage;
  
  public ConnectorDTO() { }
  
  public ConnectorDTO(Connector connector) {
    this.connectorId = connector.getId();
    this.name = connector.getConfiguration().getName();
    this.isSupportsCommitMessage = connector.isSupportsCommitMessage();
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
  
  public boolean isSupportsCommitMessage() {
    return isSupportsCommitMessage;
  }
  
  public void setSupportsCommitMessage(boolean isSupportsCommitMessage) {
    this.isSupportsCommitMessage = isSupportsCommitMessage;
  }
}
