package com.camunda.fox.cycle.connector;

import java.util.List;

import com.camunda.fox.cycle.api.connector.Connector;
import com.camunda.fox.cycle.api.connector.ConnectorFolder;
import com.camunda.fox.cycle.api.connector.ConnectorNode;

public class VfsConnector extends Connector {

  @Override
  public List<ConnectorNode> getChildren(ConnectorFolder folder) {
    return null;
  }

}
