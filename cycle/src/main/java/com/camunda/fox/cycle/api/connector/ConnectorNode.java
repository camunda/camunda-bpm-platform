package com.camunda.fox.cycle.api.connector;

import java.util.Date;


public class ConnectorNode {
  protected String path;
  protected String name;
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
  
  public ConnectorNode(String path, String name) {
    this.setPath(path);
    this.setName(name);
    this.setLabel(this.getName());
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }
  
  /**
   * Internal Name of this node, must be unique for the hierarchy level
   * @return
   */
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
  /**
   * The displayed (e.g. for UI purposes) name of this node 
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

}
