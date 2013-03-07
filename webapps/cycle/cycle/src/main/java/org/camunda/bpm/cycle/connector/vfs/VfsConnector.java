package org.camunda.bpm.cycle.connector.vfs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileNotFolderException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.VFS;
import org.camunda.bpm.cycle.connector.Connector;
import org.camunda.bpm.cycle.connector.ConnectorNode;
import org.camunda.bpm.cycle.connector.ConnectorNodeType;
import org.camunda.bpm.cycle.connector.ContentInformation;
import org.camunda.bpm.cycle.connector.Secured;
import org.camunda.bpm.cycle.connector.util.ConnectorNodeComparator;
import org.camunda.bpm.cycle.entity.ConnectorConfiguration;
import org.camunda.bpm.cycle.exception.CycleException;
import org.camunda.bpm.cycle.util.IoUtil;


public class VfsConnector extends Connector {

  public static final String BASE_PATH_KEY = "BASE_PATH";
  public static final String DEFAULT_BASE_PATH = "file://" + System.getProperty("user.home") + File.separatorChar + "cycle" + File.separatorChar;
  
  private static Logger logger = Logger.getLogger(VfsConnector.class.getSimpleName());

  private String basePath;

  @Secured
  @Override
  public List<ConnectorNode> getChildren(ConnectorNode node) {
    List<ConnectorNode> nodes = new ArrayList<ConnectorNode>();
    
    try {
      FileObject fileObject = getFileObject(node);

      if (fileObject.getType() == FileType.FILE) {
        return Collections.<ConnectorNode>emptyList();
      }
      
      FileObject[] children = fileObject.getChildren();
      
      for (FileObject file: children) {
        if (canAddFile(file)) {
          String baseName = file.getName().getBaseName();
          ConnectorNode child = new ConnectorNode(node.getId() + "/" + baseName, baseName, getId());
          child.setType(extractFileType(file));
          
          /**
           * it's not possible to get last modified date from symlinks
           */
          try {
            child.setLastModified(new Date(file.getContent().getLastModifiedTime()));
          } catch (Exception exception) {
            logger.fine("Could not set last modified time");
          }
          
          nodes.add(child);
        }
      }

      // Sort
      Collections.sort(nodes, new ConnectorNodeComparator());
      return nodes;
    } catch (FileSystemException e) {
      throw new CycleException(e);
    }
  }
  
  /**
   * If <code>file.getType() == FILE</code>, then the file can be added to list of
   * childs.<br>
   * If <code>file.getType() == FOLDER</code>, then it will check whether the children
   * of the assigned <code>file</code> can be loaded, when not <code>false</code> will
   * be returned otherwise <code>true</code>.
   */
  private boolean canAddFile(FileObject file) throws FileSystemException {
    FileType fileType = file.getType();
    if (fileType == FileType.FILE) {
      return true;
    }
    if (fileType == FileType.FOLDER) {
      try {
        file.getChildren();
        return true;
      } catch (FileNotFolderException e) {
        return false;
      }
    }
    return false;
  }

  @Secured
  @Override
  public InputStream getContent(ConnectorNode node) {
    try {
      FileObject file = getFileObject(node);
      // may only return the contents of existing objects
      if (!file.exists()) {
        return null;
      }
      
      // may only return the contents of files
      if (file.getType() != FileType.FILE) {
        return null;
      }
      
      InputStream is = file.getContent().getInputStream();
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      
      IOUtils.copy(is, os);
      IOUtils.closeQuietly(is);
      
      return new ByteArrayInputStream(os.toByteArray());
    } catch (IOException e) {
      throw new CycleException(e);
    }
  }

  @Override
  public void init(ConnectorConfiguration config) {
    basePath = config.getProperties().get(BASE_PATH_KEY);
    
    // Load base path from system property, e.g. ${user.home} if it is passed
    if (basePath.matches("\\$\\{.*\\}")) {
      String systemProperty = basePath.substring(2, basePath.length() - 1);
      String systemPropertyValue = System.getProperty(basePath.substring(2, basePath.length() - 1));
      if (systemPropertyValue != null) {
        try {
          basePath = new File(systemPropertyValue).toURI().toString();
          logger.info("Loading base path from system property " + systemProperty + ": " + basePath);
        } catch (Exception e) {
          logger.log(Level.WARNING, "Could not read base path from system property "+  basePath);
          basePath = DEFAULT_BASE_PATH;
        }
      }
    }
  }

  @Secured
  @Override
  public ConnectorNode getRoot() {
    ConnectorNode rootNode = new ConnectorNode("/", "/");
    rootNode.setType(ConnectorNodeType.FOLDER);
    return rootNode;
  }

  @Secured
  @Override
  public ContentInformation updateContent(ConnectorNode node, InputStream newContent, String message)  throws Exception {
    
    // message is ignored    
    
    try {
      FileObject fileObject = getFileObject(node);
      
      if (!fileObject.exists()) {
        throw new CycleException("File '" + node.getLabel() + "' does not exist.");
      }
      
      if (fileObject.getType() != FileType.FILE) {
        throw new CycleException("Unable to update content of file '" + node.getLabel() + "': Assigned file is not a file.");
      }

      FileContent content = fileObject.getContent();
      OutputStream os = content.getOutputStream();
      IOUtils.copy(newContent, os);
      os.flush();
      IoUtil.closeSilently(os);
      content.close();

      return getContentInformation(node);
    } catch (FileSystemException e) {
      throw new CycleException("Could not update file contents", e);
    }
  }

  @Override
  public ConnectorNode getNode(String id) {
    try {
      FileSystemManager fsManager = VFS.getManager();
      FileObject file = fsManager.resolveFile(this.createPath(id));
      
      if (!file.exists()) {
        return null;
      }
      
      String baseName = file.getName().getBaseName();
      ConnectorNode node = new ConnectorNode(id, baseName);
      node.setType(extractFileType(file));
      
      return node;
    } catch (Exception e) {
      throw new CycleException(e);
    }
  }

  @Override
  public ConnectorNode createNode(String parentId, String label, ConnectorNodeType type, String message) {
    // message is ignored
    
    if (type == null || type == ConnectorNodeType.UNSPECIFIED) {
      throw new IllegalArgumentException("Must specify a valid node type");
    }
    
    try {
      String id = "";
      if (!parentId.endsWith("/") && !label.startsWith("/")) {
        parentId = parentId + "/";
      }
      id = parentId + label;
      
      FileSystemManager fsManager = VFS.getManager();
      FileObject file = fsManager.resolveFile(this.createPath(id));
      
      if (type.isFile()) {
        file.createFile();
      } else {
        file.createFolder();
      }
      
      return new ConnectorNode(id, label, type);
    } catch (Exception e) {
      throw new CycleException(e);
    }
  }

  @Override
  public void deleteNode(ConnectorNode node, String message) {
    // message is ignored
    
    try {
      FileSystemManager fsManager = VFS.getManager();
      FileObject fileObject;

      fileObject = fsManager.resolveFile(this.createPath(node.getId()));
      fileObject.delete();

    } catch (Exception e) {
      throw new CycleException(e);
    }
  }
  
  private String createPath(String pathSuffix) {
    if (!basePath.endsWith("/") && !pathSuffix.startsWith("/")) {
      pathSuffix = "/" + pathSuffix;
    }
    return basePath + pathSuffix;
  }
  
  @Secured
  @Override
  public ContentInformation getContentInformation(ConnectorNode node) {
    try {
      return getContentInformation(getFileObject(node));
    } catch (FileSystemException e) {
      throw new RuntimeException("Content information unavailable", e);
    }
  }

  private ContentInformation getContentInformation(FileObject file) throws FileSystemException {
    if (!file.exists()) {
      return ContentInformation.notFound();
    } else
    if (file.getType() != FileType.FILE) {
      throw new IllegalArgumentException("Can only get content information from files");
    } else {
      return new ContentInformation(true, getLastModifiedDate(file));
    }
  }

  /**
   * Returns the last modified date of a file object or null if 
   * the object denotes a directory.
   * 
   * @param file
   * @return the last modified date or null
   * 
   * @throws FileSystemException 
   */
  private Date getLastModifiedDate(FileObject file) throws FileSystemException {
    if (file.getType() != FileType.FILE) {
      return null;
    } else {
      return new Date(file.getContent().getLastModifiedTime());
    }
  }
  
  /**
   * Returns a {@link FileObject} from the underlaying file system. 
   * 
   * Never returns null but throws a {@link FileSystemException} if the file system cannot be accessed.
   * 
   * @param path
   * @return the file object
   * 
   * @throws FileSystemException 
   */
  private FileObject getFileObject(ConnectorNode node) throws FileSystemException {
    String path = getTypedNodeSpecificPath(node);
    
    FileSystemManager fsManager = VFS.getManager();
    return fsManager.resolveFile(this.createPath(path));
  }
  
  private ConnectorNodeType extractFileType(FileObject file) throws FileSystemException {
    
    if (file.getType() != FileType.FILE) {
      // TODO: What is with the other types?
      return ConnectorNodeType.FOLDER;
    }
    
    String name = file.getName().getBaseName();
    if (name.endsWith(".xml") || name.endsWith(".bpmn")) {
      return ConnectorNodeType.BPMN_FILE;
    } else 
    if (name.endsWith(".png")) {
      return ConnectorNodeType.PNG_FILE;
    } else {
      return ConnectorNodeType.ANY_FILE;
    }
  }

  /**
   * Returns a file path specific to the given node type.
   * May override the current file name with something apropriate.
   * 
   * @param node
   * @return 
   */
  private String getTypedNodeSpecificPath(ConnectorNode node) {
    String path = node.getId();
    switch (node.getType()) {
      case PNG_FILE:
        int pointIndex = path.lastIndexOf(".");
        if (pointIndex != -1) {
          return path.substring(0, pointIndex) + ".png";
        } else {
          return path + ".png";
        }
      default: 
        return path;
    }
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
