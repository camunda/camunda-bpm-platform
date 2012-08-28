package com.camunda.fox.cycle.connector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

import com.camunda.fox.cycle.api.connector.Connector;
import com.camunda.fox.cycle.api.connector.ConnectorFolder;
import com.camunda.fox.cycle.api.connector.ConnectorNode;

public class VfsConnector extends Connector {

  @Override
  public List<ConnectorNode> getChildren(ConnectorFolder folder) {
    List<ConnectorNode> nodes = new ArrayList<ConnectorNode>();
    
    try {
      FileSystemManager fsManager = VFS.getManager();
      FileObject jarFile;
      
      String path = folder.getPath();
      if (path.equals("root")) {
        path = "";
      }
      
      jarFile = fsManager.resolveFile( "file://"+System.getProperty("user.home")+File.pathSeparator + path);
      // List the children of the Jar file
      FileObject[] children = jarFile.getChildren();
      for ( int i = 0; i < children.length; i++ )
      {
          String baseName = children[ i ].getName().getBaseName();
          nodes.add(new ConnectorNode(folder.getPath()+File.pathSeparator+baseName, baseName));
      }
      
      return nodes;
      
    } catch (FileSystemException e) {
      e.printStackTrace();
      return nodes;
    }

  }
}
