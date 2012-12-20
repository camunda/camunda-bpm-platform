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
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ConnectorNode getRoot() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ConnectorNode getNode(String id) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public InputStream getContent(ConnectorNode node) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ContentInformation getContentInformation(ConnectorNode node) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ConnectorNode createNode(String parentId, String label, ConnectorNodeType type) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void deleteNode(ConnectorNode node) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public ContentInformation updateContent(ConnectorNode node, InputStream newContent) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

}
