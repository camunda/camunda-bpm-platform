package com.camunda.fox.cycle.connector.test.util;

import java.io.InputStream;
import java.util.List;

import org.springframework.stereotype.Component;

import com.camunda.fox.cycle.connector.Connector;
import com.camunda.fox.cycle.connector.ConnectorNode;
import com.camunda.fox.cycle.connector.ConnectorNodeType;
import com.camunda.fox.cycle.connector.ContentInformation;

/**
 * For testing / mocking purposes.
 * 
 * @author christian.lipphardt@camunda.com
 */
@Component
public class DummyConnector extends Connector {

  @Override
  public List<ConnectorNode> getChildren(ConnectorNode parent) {
    return null;
  }

  @Override
  public ConnectorNode getRoot() {
    return null;
  }

  @Override
  public ConnectorNode getNode(String id) {
    return null;
  }

  @Override
  public InputStream getContent(ConnectorNode node) {
    return null;
  }

  @Override
  public ContentInformation getContentInformation(ConnectorNode node) {
    return null;
  }

  @Override
  public ConnectorNode createNode(String parentId, String label, ConnectorNodeType type, String message) {
    return null;
  }

  @Override
  public void deleteNode(ConnectorNode node, String message) {
    
  }

  @Override
  public ContentInformation updateContent(ConnectorNode node, InputStream newContent, String message) throws Exception {
    return null;
  }

  @Override
  public boolean needsLogin() {
    return false;
  }

  @Override
  public boolean isSupportsCommitMessage() {
    return false;
  }

}
