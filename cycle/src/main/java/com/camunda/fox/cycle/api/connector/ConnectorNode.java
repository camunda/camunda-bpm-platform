package com.camunda.fox.cycle.api.connector;


public class ConnectorNode {
  private String path;
  private String name;
  
  
  public ConnectorNode() {
  }
  
  public ConnectorNode(String path, String name) {
    this.setPath(path);
    this.setName(name);
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
}
