package com.camunda.fox.cycle.connector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.VFS;

import com.camunda.fox.cycle.api.connector.Connector;
import com.camunda.fox.cycle.api.connector.ConnectorNode;
import com.camunda.fox.cycle.api.connector.ConnectorNode.ConnectorNodeType;

public class VfsConnector extends Connector {

  @Override
  public List<ConnectorNode> getChildren(ConnectorNode parent) {
    List<ConnectorNode> nodes = new ArrayList<ConnectorNode>();
    
    try {
      FileSystemManager fsManager = VFS.getManager();
      FileObject fileObject;
      
      String path = parent.getPath();
      
      fileObject = fsManager.resolveFile( "file://"+System.getProperty("user.home")+File.separatorChar + path);

      if (fileObject.getType() == FileType.FILE) {
        return nodes;
      }
      
      FileObject[] children = fileObject.getChildren();
      
      for ( FileObject file : children )
      {
          String baseName = file.getName().getBaseName();
          ConnectorNode node = new ConnectorNode(parent.getPath()+File.separatorChar+baseName, baseName);
          if (file.getType() == FileType.FILE) {
            node.setType(ConnectorNodeType.FILE);
          }
          nodes.add(node);
      }
      
      return nodes;
      
    } catch (FileSystemException e) {
      throw new RuntimeException(e);
    }

  }
}
