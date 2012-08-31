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
}
