package com.camunda.fox.cycle.connector;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.VFS;

import com.camunda.fox.cycle.api.connector.Connector;
import com.camunda.fox.cycle.api.connector.ConnectorNode;
import com.camunda.fox.cycle.api.connector.ConnectorNode.ConnectorNodeType;
import com.camunda.fox.cycle.exception.CycleException;

public class VfsConnector extends Connector {

  public static final String BASE_PATH = "file://"+System.getProperty("user.home")+File.separatorChar;

  @Override
  public List<ConnectorNode> getChildren(ConnectorNode parent) {
    List<ConnectorNode> nodes = new ArrayList<ConnectorNode>();
    
    try {
      FileSystemManager fsManager = VFS.getManager();
      FileObject fileObject;
      
      fileObject = fsManager.resolveFile(BASE_PATH + parent.getPath());

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
          node.setLastModified(new Date(file.getContent().getLastModifiedTime()));
          nodes.add(node);
      }
      
      return nodes;
      
    } catch (FileSystemException e) {
      throw new CycleException(e);
    }

  }

  @Override
  public InputStream getContent(ConnectorNode node) {
    try {
      FileSystemManager fsManager = VFS.getManager();
      FileObject fileObject;
      
      fileObject = fsManager.resolveFile(BASE_PATH + node.getPath());

      if (fileObject.getType() != FileType.FILE) {
        throw new CycleException("Cant cant content of non-file node");
      }
      
      return fileObject.getContent().getInputStream();
      
    } catch (FileSystemException e) {
      throw new CycleException(e);
    }
  }
}
