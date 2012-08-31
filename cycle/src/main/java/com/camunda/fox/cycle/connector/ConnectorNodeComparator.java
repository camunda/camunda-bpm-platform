package com.camunda.fox.cycle.connector;

import java.util.Comparator;

import com.camunda.fox.cycle.api.connector.ConnectorNode;

/**
 * Compares connector knodes between each other.
 * 
 * Directory > File
 * Names: alphabetical order
 * 
 * @author nico.rehwaldt
 */
public class ConnectorNodeComparator implements Comparator<ConnectorNode> {

  @Override
  public int compare(ConnectorNode o1, ConnectorNode o2) {
    if (o1.getType() != o2.getType()) {
      return o1.getType() == ConnectorNode.ConnectorNodeType.FOLDER ? -1 : 1;
    }
    
    return o1.getLabel().compareToIgnoreCase(o2.getLabel());
  }
}
