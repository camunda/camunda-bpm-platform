package com.camunda.fox.cycle.connector;

import java.io.InputStream;
import java.util.List;

import com.camunda.fox.cycle.entity.ConnectorConfiguration;


public abstract class Connector {
  
  private ConnectorConfiguration configuration;
  
  public abstract List<ConnectorNode> getChildren(ConnectorNode parent);
  
  public abstract ConnectorNode getRoot();
  
  /**
   * Returns a {@link ConnectorNode} to the assigned <code>id</code>. If
   * a {@link ConnectorNode} could not be found, the value <code>null</code>
   * will be returned. 
   * @param id Represents the id of a {@link ConnectorNode} to search.
   * @return A {@link ConnectorNode} to the assigned <code>id</code> or null if no
   * {@link ConnectorNode} found.
   */
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
  
  public abstract ConnectorNode createNode(String parentId, String label, ConnectorNodeType type, String message);
  
  public abstract void deleteNode(ConnectorNode node, String message);
  
  public abstract ContentInformation updateContent(ConnectorNode node, InputStream newContent, String message) throws Exception;
  
  public void login(String userName, String password) {
  }

  public void dispose() {
  }

  public abstract boolean needsLogin();

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
  
  public abstract boolean isSupportsCommitMessage();
}
