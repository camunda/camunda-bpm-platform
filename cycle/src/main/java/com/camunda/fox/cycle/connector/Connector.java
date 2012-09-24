package com.camunda.fox.cycle.connector;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.camunda.fox.cycle.entity.ConnectorConfiguration;


public abstract class Connector {
  
  private ConnectorConfiguration configuration;
  
  public abstract List<ConnectorNode> getChildren(ConnectorNode parent);
  
  public abstract ConnectorNode getRoot();
  
  public abstract ConnectorNode getNode(String id);
  
  /**
   * Get the contents of the given node as an input stream.
   * 
   * Implementations must handle the node correctly, 
   * especially concerning the nodes type accessible via {@link ConnectorNode#getType()}. 
   * 
   * @param node
   * 
   * @return the input stream of the file
   */
  public abstract InputStream getContent(ConnectorNode node);

  /**
   * Returns a {@link ContentInformation} for the given connector node.
   * May never return null or throw an exception.
   * 
   * @param node
   * @return 
   */
  public abstract ContentInformation getContentInformation(ConnectorNode node);
  
  public abstract ConnectorNode createNode(String id, String label, ConnectorNodeType type);
  
  public abstract void deleteNode(String id);
  
  public abstract ContentInformation updateContent(ConnectorNode node, InputStream newContent) throws Exception;
  
  public void login(String userName, String password) {
  }

  public void dispose() {
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
  
  public void init(ConnectorConfiguration config) { }
  
  public void init() {
    init(configuration);
  }
  
  public Long getId() {
    return getConfiguration().getId();
  }
}
