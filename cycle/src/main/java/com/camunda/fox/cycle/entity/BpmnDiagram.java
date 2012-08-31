package com.camunda.fox.cycle.entity;

import javax.persistence.Embedded;
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
  
  @Embedded
  private PersistentConnectorNode diagramPath;
  
  @Enumerated(EnumType.STRING)
  private Status status;
  
  public BpmnDiagram() { }
  
  public BpmnDiagram(String modeler, PersistentConnectorNode diagramPath) {
    this.modeler = modeler;
    this.diagramPath = diagramPath;
  }

  public PersistentConnectorNode getDiagramPath() {
    return diagramPath;
  }

  public void setDiagramPath(PersistentConnectorNode diagramPath) {
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
}
