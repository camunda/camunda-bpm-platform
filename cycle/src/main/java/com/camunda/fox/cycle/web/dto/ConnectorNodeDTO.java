package com.camunda.fox.cycle.web.dto;

import java.util.ArrayList;
import java.util.List;

import com.camunda.fox.cycle.api.connector.ConnectorNode;
import com.camunda.fox.cycle.api.connector.ConnectorNode.ConnectorNodeType;


public class ConnectorNodeDTO {

  private String id;
  private String label;
  private String path;
  private ConnectorNodeType type;

  public ConnectorNodeDTO() {
  }

  public ConnectorNodeDTO(ConnectorNode connectorNode) {
    id = connectorNode.getName();
    label = connectorNode.getLabel();
    path = connectorNode.getPath();
    setType(connectorNode.getType());
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

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }
  
  public ConnectorNodeType getType() {
    return type;
  }

  public void setType(ConnectorNodeType type) {
    this.type = type;
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
