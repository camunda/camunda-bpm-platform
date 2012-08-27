package com.camunda.fox.cycle.api.connector;

import java.util.List;


public interface Connector {
  List<ConnectorNode> getChildren(ConnectorFolder folder);
}
