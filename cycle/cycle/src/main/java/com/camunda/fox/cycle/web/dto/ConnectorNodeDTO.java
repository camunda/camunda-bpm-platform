package com.camunda.fox.cycle.web.dto;

import java.util.ArrayList;
import java.util.List;

import com.camunda.fox.cycle.connector.ConnectorNode;
import com.camunda.fox.cycle.connector.ConnectorNodeType;


public class ConnectorNodeDTO {

  private String id;
  private String label;
  
  private ConnectorNodeType type;
  
  private Long connectorId;
  
  public ConnectorNodeDTO() {
  }

  public ConnectorNodeDTO(ConnectorNode connectorNode) {
    id = connectorNode.getId();
    label = connectorNode.getLabel();
    type = connectorNode.getType();
    connectorId = connectorNode.getConnectorId();
  }

  public ConnectorNodeDTO(String id, String label, long connectorId) {
    this.id = id;
    this.label = label;
    this.connectorId = connectorId;
  }
  
  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }
  
  public String getLabel() {
    return label;
  }
  
  public void setLabel(String label) {
    this.label = label;
  }

  public ConnectorNodeType getType() {
    return type;
  }

  public void setType(ConnectorNodeType type) {
    this.type = type;
  }

  public Long getConnectorId() {
    return connectorId;
  }

  public void setConnectorId(Long connectorId) {
    this.connectorId = connectorId;
  }

  public ConnectorNode toConnectorNode() {
    return new ConnectorNode(id, label, connectorId, type);
  }

  /**
   * Wraps a list of connector nodes as a list of the respective connector node data objects
   * 
   * @param nodes
   * @return 
   */
  public static List<ConnectorNodeDTO> wrapAll(List<ConnectorNode> nodes) {
    List<ConnectorNodeDTO> dtos = new ArrayList<ConnectorNodeDTO>();
    for (ConnectorNode tn: nodes) {
      dtos.add(new ConnectorNodeDTO(tn));
    }
    
    return dtos;
  }
}
