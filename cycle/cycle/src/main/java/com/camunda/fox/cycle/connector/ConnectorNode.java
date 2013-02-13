package com.camunda.fox.cycle.connector;

import java.io.Serializable;
import java.util.Date;

public class ConnectorNode implements Serializable {

  private static final long serialVersionUID = 1L;

  protected String id;

  // Long because it may be null
  private Long connectorId;

  private String label;

  private Date created;
  private Date lastModified;
  
  private String message;

  private ConnectorNodeType type = ConnectorNodeType.UNSPECIFIED;

  public ConnectorNode() {
  }

  public ConnectorNode(String id) {
    this.id = id;
  }

  public ConnectorNode(String id, String label) {
    this(id);
    
    this.label = label;
  }

  public ConnectorNode(String id, ConnectorNodeType type) {
    this(id);
    
    this.type = type;
  }

  public ConnectorNode(String id, String label, Long connectorId) {
    this(id, label);
    
    this.connectorId = connectorId;
  }

  public ConnectorNode(String id, String label, Long connectorId, ConnectorNodeType type) {
    this(id, label);
    
    this.connectorId = connectorId;
    this.type = type;
  }
  
  public ConnectorNode(String id, String label, ConnectorNodeType type) {
    this(id, label);
    
    this.type = type;
  }
  
  public ConnectorNode(String id, String label, Long connectorId, ConnectorNodeType type, String message) {
    this(id, label, connectorId, type);
    this.message = message;
  }

  /**
   * Return the id of this node. May contain different representations, 
   * depending on the corresponding connector
   * 
   * @return
   */
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  /**
   * The label of the node for display purposes in the user interface
   * @return
   */
  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public ConnectorNodeType getType() {
    return type;
  }

  public void setType(ConnectorNodeType type) {
    this.type = type;
  }

  public Date getCreated() {
    return created;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public Date getLastModified() {
    return lastModified;
  }

  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }

  public Long getConnectorId() {
    return connectorId;
  }

  public void setConnectorId(Long connectorId) {
    this.connectorId = connectorId;
  }
  
  /**
   * @return commit message
   */
  public String getMessage() {
    return message;
  }
  
  public void setMessage(String message) {
    this.message = message;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ConnectorNode other = (ConnectorNode) obj;
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else
    if (!id.equals(other.id)) {
      return false;
    }
    return true;
  }

  public boolean isDirectory() {
    return type != null && type.isDirectory();
  }
  
  @Override
  public String toString() {
    return "ConnectorNode [id=" + id + ", connectorId=" + connectorId + ", label=" + label + "]";
  }
  
}
