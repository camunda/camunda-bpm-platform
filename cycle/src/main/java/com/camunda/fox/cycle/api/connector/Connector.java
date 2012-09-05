package com.camunda.fox.cycle.api.connector;

import java.io.InputStream;
import java.util.List;

import com.camunda.fox.cycle.entity.ConnectorConfiguration;


public abstract class Connector {
  private ConnectorConfiguration configuration;
  
  public abstract List<ConnectorNode> getChildren(ConnectorNode parent);
  
  public abstract ConnectorNode getRoot();
  
  public abstract ConnectorNode getNode(String id);
  
  public abstract InputStream getContent(ConnectorNode node);
  
  public abstract void updateContent(ConnectorNode node, String newContent) throws Exception;
  
  public void login(String userName, String password) {
  }
  
  public boolean needsLogin() {
    return false;
  }

  public ConnectorConfiguration getConfiguration() {
    return configuration;
  }

  public void setConfiguration(ConnectorConfiguration configuration) {
    this.configuration = configuration;
  }
  
  public void init(ConnectorConfiguration config) {
  }
  
  public void createModel() throws Exception {
  }
  
  public Long getId() {
    return getConfiguration().getId();
  }
  
}
