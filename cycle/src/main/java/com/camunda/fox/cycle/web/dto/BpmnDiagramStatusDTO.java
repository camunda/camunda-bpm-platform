package com.camunda.fox.cycle.web.dto;

import java.util.Date;

import com.camunda.fox.cycle.entity.BpmnDiagram;
import com.camunda.fox.cycle.entity.BpmnDiagram.Status;

/**
 * This data object exposes a {@link BpmnDiagram}`s sync status.
 * 
 * @author nico.rehwaldt
 */
public class BpmnDiagramStatusDTO {
  
  private long diagramId;
  
  private Date lastModified;
  
  private Status status;
  
  private Date lastUpdated;
  
  public BpmnDiagramStatusDTO() { }
  
  public BpmnDiagramStatusDTO(BpmnDiagram diagram) {
    this.diagramId = diagram.getId();
    this.lastModified = diagram.getLastModified();
    this.status = diagram.getStatus();
  }
  
  public BpmnDiagramStatusDTO(long diagramId, Status status, Date lastModified) {
    this.diagramId = diagramId;
    this.lastModified = lastModified;
    this.status = status;
  }

  public long getDiagramId() {
    return diagramId;
  }

  public Status getStatus() {
    return status;
  }

  public Date getLastModified() {
    return lastModified;
  }

  public void setDiagramId(long diagramId) {
    this.diagramId = diagramId;
  }

  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public Date getLastUpdated() {
    return lastUpdated;
  }

  public void setLastUpdated(Date lastUpdated) {
    this.lastUpdated = lastUpdated;
  }
}
