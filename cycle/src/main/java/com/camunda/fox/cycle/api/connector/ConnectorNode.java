package com.camunda.fox.cycle.api.connector;

import java.util.Date;


public class ConnectorNode implements Comparable<ConnectorNode> {
  
  protected String id;
  protected String label;
  protected ConnectorNodeType type = ConnectorNodeType.FOLDER;
  protected Date created;
  private Date lastModified;
  
  public enum ConnectorNodeType {
    FILE,
    FOLDER
  }
  
  public ConnectorNode() {
  }
  
  public ConnectorNode(String id) {
    this.setId(id);
  }
  
  public ConnectorNode(String id, String label) {
    this.setId(id);
    this.setLabel(label);
  }
  
  /**
   * ID of this node, may contain different representations, depending on the corresponding connector
   * @return
   */
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
  
  /**
   * The label of the node for UI purposes
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

  @Override
  public int compareTo(ConnectorNode o) {
    return o.label.compareTo(label);
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
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ConnectorNode other = (ConnectorNode) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }
  
}
