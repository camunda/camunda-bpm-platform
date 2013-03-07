package org.camunda.bpm.cycle.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.camunda.bpm.cycle.connector.ConnectorNode;
import org.camunda.bpm.cycle.connector.ConnectorNodeType;


/**
 * Represents a BPMN 2.0 diagram used in a roundtrip.
 * 
 * @author nico.rehwaldt
 */
@Entity
@Table(name="cy_bpmn_diagram")
public class BpmnDiagram extends AbstractEntity {
  
  private static final long serialVersionUID = 1L;
  
  /**
   * This enum represents the status of a
   * BPMN 2.0 diagram from the perspective of the application. 
   * 
   * A diagram may have the following states
   * 
   * UNSPECIFIED: 
   *    the status is not currently known, because it 
   *    was never checked or is about to be checked#
   * 
   * UNAVAILABLE: 
   *    the status is unavailable because the connector is not available, 
   *    the connection to the connector cannot be established or the 
   *    diagram is missing in the connector
   * 
   * OUT_OF_SYNC:
   *    the diagram is available but out of sync, that is 
   *    changed since it was last handled by cycle
   * 
   * SYNCHED: 
   *    the diagram is totally fine and thus is cycle
   */
  public static enum Status {
    UNSPECIFIED,
    UNAVAILABLE, 
    OUT_OF_SYNC, 
    SYNCED
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

  public BpmnDiagram(String modeler, ConnectorNode connectorNode) {
    this.modeler = modeler;
    this.diagramPath = connectorNode.getId();
    this.connectorId = connectorNode.getConnectorId();
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
