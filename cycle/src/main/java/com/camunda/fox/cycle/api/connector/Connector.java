package com.camunda.fox.cycle.api.connector;

import java.util.List;


public abstract class Connector {
  public abstract List<ConnectorNode> getChildren(ConnectorFolder folder);
  public abstract String getName();
}
