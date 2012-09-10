package com.camunda.fox.cycle.web.dto;

import java.util.Date;

import com.camunda.fox.cycle.entity.BpmnDiagram;
import com.camunda.fox.cycle.entity.BpmnDiagram.Status;

/**
 * This is a data object which exposes a {@link BpmnDiagram} to the client via rest.
 * 
 * @author nico.rehwaldt
 */
public class BpmnDiagramDTO {
  
  private Long id;
  private String modeler;
  private String diagramPath;
  private Long connectorId;
  private String label;
  private Date lastModified;

  private Status status;
  
  public BpmnDiagramDTO() { }
  
  public BpmnDiagramDTO(BpmnDiagram diagram) {
    this.id = diagram.getId();
    this.modeler = diagram.getModeler();
    this.status = diagram.getStatus();
    this.diagramPath = diagram.getDiagramPath();
    this.connectorId = diagram.getConnectorId();
    this.label = diagram.getLabel();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }
  
  public String getModeler() {
    return modeler;
  }

  public void setModeler(String modeler) {
    this.modeler = modeler;
  }

  public Status getStatus() {
    return status;
  }
  
  public String getDiagramPath() {
    return diagramPath;
  }

  public void setDiagramPath(String diagramPath) {
    this.diagramPath = diagramPath;
  }

  public void setStatus(Status status) {
    this.status = status;
  }
  
  /**
   * Wraps a bpmn diagram as a data object
   * @param diagram
   * @return 
   */
  public static BpmnDiagramDTO wrap(BpmnDiagram diagram) {
    return new BpmnDiagramDTO(diagram);
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
}
