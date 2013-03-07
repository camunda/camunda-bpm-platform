package org.camunda.bpm.cycle.connector.util;

import java.util.Comparator;

import org.camunda.bpm.cycle.connector.ConnectorNode;



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
  public int compare(ConnectorNode n1, ConnectorNode n2) {
    
    // If both have the same type
    if ((n1.isDirectory() && n2.isDirectory()) ||
        (!n1.isDirectory() && !n2.isDirectory())) {
      
      return n1.getLabel().compareToIgnoreCase(n2.getLabel());
    }
    
    return n1.isDirectory() ? -1 : 1;
  }
}
