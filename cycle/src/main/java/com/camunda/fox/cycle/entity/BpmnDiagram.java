package com.camunda.fox.cycle.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.camunda.fox.cycle.connector.ConnectorNode;
import com.camunda.fox.cycle.connector.ConnectorNodeType;

/**
 * Represents a BPMN 2.0 diagram used in a roundtrip.
 * 
 * @author nico.rehwaldt
 */
@Entity
public class BpmnDiagram extends AbstractEntity {
  
  private static final long serialVersionUID = 1L;
  
  public static enum Status {
    UNSPECIFIED,
    UNAVAILABLE, 
    OUT_OF_SYNC, 
    SYNCED, 
    WARNING
  }
  
  private String modeler;
  
  private String diagramPath;
  
  @Enumerated(EnumType.STRING)
  private Status status;

  private Long connectorId;
  
  private String label;
  
  @Temporal(TemporalType.TIMESTAMP)
  private Date lastModified;
  
  @Temporal(TemporalType.TIMESTAMP)
  private Date lastSync;
  
  public BpmnDiagram() { }
  
  public BpmnDiagram(String modeler, String diagramPath, Long connectorId) {
    this.modeler = modeler;
    this.diagramPath = diagramPath;
    this.connectorId = connectorId;
  }

  public String getDiagramPath() {
    return diagramPath;
  }

  public void setDiagramPath(String diagramPath) {
    this.diagramPath = diagramPath;
  }

  public void setModeler(String modeler) {
    this.modeler = modeler;
  }

  public String getModeler() {
    return modeler;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public Long getConnectorId() {
    return connectorId;
  }

  public void setConnectorId(Long connectorId) {
    this.connectorId = connectorId;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public Date getLastModified() {
    return lastModified;
  }

  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }

  public Date getLastSync() {
    return lastSync;
  }

  public void setLastSync(Date lastSync) {
    this.lastSync = lastSync;
  }
  
  /**
   * Returns the connector node stored in this diagram or null if none is stored
   * 
   * @return 
   */
  public ConnectorNode getConnectorNode() {
    if (diagramPath != null) {
      return new ConnectorNode(diagramPath, label, connectorId, ConnectorNodeType.BPMN_FILE);
    } else {
      return null;
    }
  }
}
