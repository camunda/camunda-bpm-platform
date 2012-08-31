package com.camunda.fox.cycle.entity;

import java.io.Serializable;

import javax.persistence.Embeddable;

/**
 * Represents a BPMN 2.0 diagram used in a roundtrip.
 * 
 * @author nico.rehwaldt
 */
@Embeddable
public class PersistentConnectorNode implements Serializable {
  
  private static final long serialVersionUID = 1L;
  
  private String nodeId;
  
  private String label;
  
  private Long connectorId;
  
  public PersistentConnectorNode() { }
  
  public PersistentConnectorNode(String nodeId, String label, Long connectorId) {
    this.nodeId = nodeId;
    this.label = label;
    this.connectorId = connectorId;
  }

  public String getLabel() {
    return label;
  }

  public String getNodeId() {
    return nodeId;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public void setNodeId(String nodeId) {
    this.nodeId = nodeId;
  }

  public Long getConnectorId() {
    return connectorId;
  }
  
  public void setConnectorId(Long connectorId) {
    this.connectorId = connectorId;
  }
}
