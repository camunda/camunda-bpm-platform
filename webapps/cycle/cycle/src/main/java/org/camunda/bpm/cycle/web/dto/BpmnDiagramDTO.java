package org.camunda.bpm.cycle.web.dto;

import java.util.Date;

import org.camunda.bpm.cycle.entity.BpmnDiagram;


/**
 * This is a data object which exposes a {@link BpmnDiagram} to the client via rest.
 * 
 * @author nico.rehwaldt
 */
public class BpmnDiagramDTO {
  
  private Long id;
  private String modeler;
  private String label;
  private Date lastSync;
  
  private BpmnDiagramStatusDTO syncStatus; 
  private ConnectorNodeDTO connectorNode;
  
  public BpmnDiagramDTO() { }
  
  public BpmnDiagramDTO(BpmnDiagram diagram) {
    this.id = diagram.getId();
    this.modeler = diagram.getModeler();
    
    this.label = diagram.getLabel();
    this.lastSync = diagram.getLastSync();
    
    this.syncStatus = new BpmnDiagramStatusDTO(diagram);
    
    this.connectorNode = diagram.getConnectorNode() != null ? new ConnectorNodeDTO(diagram.getConnectorNode()) : null;
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

  public BpmnDiagramStatusDTO getSyncStatus() {
    return syncStatus;
  }

  public void setSyncStatus(BpmnDiagramStatusDTO syncStatus) {
    this.syncStatus = syncStatus;
  }

  public ConnectorNodeDTO getConnectorNode() {
    return connectorNode;
  }

  public void setConnectorNode(ConnectorNodeDTO connectorNode) {
    this.connectorNode = connectorNode;
  }

  /**
   * Wraps a bpmn diagram as a data object
   * @param diagram
   * @return 
   */
  public static BpmnDiagramDTO wrap(BpmnDiagram diagram) {
    return new BpmnDiagramDTO(diagram);
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public Date getLastSync() {
    return lastSync;
  }

  public void setLastSync(Date lastSync) {
    this.lastSync = lastSync;
  }
}
