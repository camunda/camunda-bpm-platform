package com.camunda.fox.cycle.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

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
  
  private Date lastModified;
  
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
}
