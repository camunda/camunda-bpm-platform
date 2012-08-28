package com.camunda.fox.cycle.entity;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Represents a BPMN 2.0 diagram used in a roundtrip.
 * 
 * @author nico.rehwaldt
 */
@Entity
public class BpmnDiagram {
  
  public static enum Status {
    UNSPECIFIED,
    OUT_OF_SYNC, 
    SYNCED, 
    WARNING
  }
  
  @Id
  @GeneratedValue
  private Long id;
  
  private String modeller;
  
  private String diagramPath;
  
  @Enumerated(EnumType.STRING)
  private Status status;
  
  public BpmnDiagram() { }
  
  public BpmnDiagram(String modeller, String diagramPath) {
    this.modeller = modeller;
    this.diagramPath = diagramPath;
  }

  public String getDiagramPath() {
    return diagramPath;
  }

  public void setDiagramPath(String diagramPath) {
    this.diagramPath = diagramPath;
  }

  public Long getId() {
    return id;
  }

  public void setModeller(String modeller) {
    this.modeller = modeller;
  }

  public String getModeller() {
    return modeller;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }
}
