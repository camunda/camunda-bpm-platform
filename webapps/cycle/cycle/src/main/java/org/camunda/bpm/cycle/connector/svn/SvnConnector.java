package org.camunda.bpm.cycle.connector.svn;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.camunda.bpm.cycle.configuration.CycleConfiguration;
import org.camunda.bpm.cycle.connector.Connector;
import org.camunda.bpm.cycle.connector.ConnectorNode;
import org.camunda.bpm.cycle.connector.ConnectorNodeType;
import org.camunda.bpm.cycle.connector.ContentInformation;
import org.camunda.bpm.cycle.connector.Secured;
import org.camunda.bpm.cycle.connector.Threadsafe;
import org.camunda.bpm.cycle.entity.ConnectorConfiguration;
import org.camunda.bpm.cycle.exception.CycleException;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNDirEntry;
import org.tigris.subversion.svnclientadapter.ISVNProperty;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.tigris.subversion.svnclientadapter.commandline.CmdLineClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.svnkit.SvnKitClientAdapter;
import org.tigris.subversion.svnclientadapter.svnkit.SvnKitClientAdapterFactory;



public class SvnConnector extends Connector {
  
  public final static String CONFIG_KEY_REPOSITORY_PATH = "repositoryPath";
  public final static String CONFIG_KEY_TEMPORARY_FILE_STORE = "temporaryFileStore";
  public final static String DEFAULT_CONFIG_KEY_TEMPORARY_FILE_STORE = "/tmp";
  
  private static final String SLASH_CHAR = "/";
  
  private static Logger logger = Logger.getLogger(SvnConnector.class.getName());
  
  private String baseTemporaryFileStore;
  private String baseUrl;
  
  private ISVNClientAdapter svnClientAdapter;
  private ReentrantLock transactionLock = new ReentrantLock();

  private boolean loggedIn;
  
  @Inject
  private CycleConfiguration cycleConfiguration;
  
  static {
    setupFactories();
  }
  
  private static void setupFactories() {
    boolean initialized = false;
    try {
      SvnKitClientAdapterFactory.setup();
      initialized = true;
    } catch (SVNClientException e) {
      logger.log(Level.INFO, "Cannot initialize the SvnKitClientAdapterFactory.");
    }

    if (!initialized) {
      try {
        CmdLineClientAdapterFactory.setup();
      } catch (SVNClientException e) {
        logger.log(Level.INFO, "Cannot initialize the CmdLineClientAdapterFactory.");
      }
    }
  }
  
  @Override
  public void init(ConnectorConfiguration config) {
    if (svnClientAdapter == null) {
      System.setProperty("svnkit.http.methods","Basic,NTLM");
      svnClientAdapter = new SvnKitClientAdapter();
    }
    
    baseTemporaryFileStore = config.getProperties().get(CONFIG_KEY_TEMPORARY_FILE_STORE);
    
    if (baseTemporaryFileStore == null) {
      baseTemporaryFileStore = DEFAULT_CONFIG_KEY_TEMPORARY_FILE_STORE;
    }
    // Load temporary file store path from system property, e.g. ${user.home} if it is passed
    if (baseTemporaryFileStore.matches("\\$\\{.*\\}")) {
      String systemProperty = baseTemporaryFileStore.substring(2, baseTemporaryFileStore.length() - 1);
      String systemPropertyValue = System.getProperty(baseTemporaryFileStore.substring(2, baseTemporaryFileStore.length() - 1));
      if (systemPropertyValue != null) {
        try {
          baseTemporaryFileStore = new File(systemPropertyValue).toURI().toString();
          logger.info("Loading temporary file store path from system property " + systemProperty + ": " + baseTemporaryFileStore);
        } catch (Exception e) {
          logger.log(Level.WARNING, "Could not read temporary file store path from system property "+  baseTemporaryFileStore);
          baseTemporaryFileStore = DEFAULT_CONFIG_KEY_TEMPORARY_FILE_STORE;
        }
      }
    }
    baseUrl = getConfiguration().getProperties().get(CONFIG_KEY_REPOSITORY_PATH);
  }

  @Override
  public boolean needsLogin() {
    return !loggedIn;
  }
  
  @Threadsafe
  @Override
  public void login(String userName, String password) {
    svnClientAdapter.setUsername(userName);
    svnClientAdapter.setPassword(password);
    loggedIn = true;
  }
  
  private SVNUrl createSvnUrl(ConnectorNode node) throws Exception {
    return createSvnUrl(node.getId());
  }
  
  private SVNUrl createSvnUrl(String id) throws Exception {
    if (!baseUrl.endsWith("/") && !id.startsWith("/")) {
      id = "/" + id;
    }
    String result = baseUrl + id;
    if (result.endsWith("//")) {
      result = result.substring(0, result.length() - 1);
    }
    return new SVNUrl(result);
  }

  @Threadsafe
  @Secured
  @Override
  public List<ConnectorNode> getChildren(ConnectorNode parent) {
    try {
      List<ConnectorNode> nodes = new ArrayList<ConnectorNode>();
      SVNUrl svnUrl = createSvnUrl(parent);
      ISVNDirEntry[] entries = svnClientAdapter.getList(svnUrl, SVNRevision.HEAD, false);
      for (ISVNDirEntry currentEntry : entries) {
        String id = parent.getId();
        if (!id.endsWith(SLASH_CHAR)) {
          id = id + SLASH_CHAR;
        }
        id = id + currentEntry.getPath();
        
        ConnectorNode newNode = new ConnectorNode(id);
        decorateConnectorNode(newNode, currentEntry);
        
        nodes.add(newNode);
      }
      return nodes;
    } catch (Exception e) {
      logger.log(Level.FINER, "Cannot get children for node " + parent.getId(), e);
      throw new CycleException("Children for SVN connector '" + getConfiguration().getName() + "' could not be loaded from repository '" + parent.getId() + "'.", e);
    }
    
  }
  
  @Secured
  @Override
  public ConnectorNode getRoot() {
    ConnectorNode rootNode = new ConnectorNode("/", "/");
    rootNode.setType(ConnectorNodeType.FOLDER);
    return rootNode;
  }

  @Threadsafe
  @Secured
  @Override
  public ConnectorNode getNode(String id) {
    try {
      SVNUrl svnUrl = createSvnUrl(id);
      ISVNDirEntry entry = svnClientAdapter.getDirEntry(svnUrl, SVNRevision.HEAD);
      
      if (entry != null) {
        ConnectorNode node = new ConnectorNode(id);
        node.setMessage(extractCommitMessage(svnUrl, entry));
        return decorateConnectorNode(node, entry);
      }
    } catch (Exception e) {
      logger.log(Level.FINER, "Cannot get node '" + id + "' in Svn '" + getId() + "'.", e);
    }
    return null;
  }

  protected String extractCommitMessage(SVNUrl svnUrl, ISVNDirEntry entry) throws SVNClientException {
    ISVNProperty[] properties = svnClientAdapter.getRevProperties(svnUrl, entry.getLastChangedRevision());
    for (int i = 0; i < properties.length; i++) {
      ISVNProperty prop = properties[i];
      if("svn:log".equals(prop.getName())) {
        return prop.getValue();
      }
    }
    return null;
  }
  
  private ConnectorNode decorateConnectorNode(ConnectorNode node, ISVNDirEntry entry) {
    node.setLabel(entry.getPath());
    node.setLastModified(entry.getLastChangedDate());
    node.setConnectorId(getId());
    node.setType(extractFileType(entry));    
    
    return node;
  }
  
  private ConnectorNodeType extractFileType(ISVNDirEntry entry) {
    if (entry.getNodeKind() != SVNNodeKind.FILE) {
      // TODO: What is with the other types?
      return ConnectorNodeType.FOLDER;
    }
    
    String name = entry.getPath();
    if (name.endsWith(".xml") || name.endsWith(".bpmn")) {
      return ConnectorNodeType.BPMN_FILE;
    } else 
    if (name.endsWith(".png")) {
      return ConnectorNodeType.PNG_FILE;
    } else {
      return ConnectorNodeType.ANY_FILE;
    }
  }

  @Threadsafe
  @Secured
  @Override
  public ConnectorNode createNode(String parentId, String label, ConnectorNodeType type, String message) {
    File temporaryFileStore = null;

    try {

      if (type == null || type == ConnectorNodeType.UNSPECIFIED) {
        throw new IllegalArgumentException("Must specify a valid node type");
      }

      beginTransaction();

      String id = "";
      if (!parentId.endsWith("/") && !label.startsWith("/")) {
        parentId = parentId + "/";
      }
      id = parentId + label;

      String parentFolder = extractParentFolder(id);
      temporaryFileStore = getTemporaryFileStore(parentFolder + File.separator + UUID.randomUUID().toString());

      SVNUrl svnUrl = createSvnUrl(parentFolder);
      checkout(svnUrl, temporaryFileStore);
      
      File newFile = new File(temporaryFileStore + File.separator + label);
      if (type != ConnectorNodeType.FOLDER) {
        newFile.createNewFile();
        svnClientAdapter.addFile(newFile);
      } else if (type == ConnectorNodeType.FOLDER) {
        newFile.mkdir();
        svnClientAdapter.addDirectory(newFile, true);
      }
      
      String defaultMessage = getDefaultCommitMessage("Created node '" + label + "' in '" + parentFolder + "' using camunda cycle.");
      if(message == null || message.length()==0) {
        message = defaultMessage;
      }
      
      commit(new File[] {temporaryFileStore}, message);

      return new ConnectorNode(id, label, getId() , type, message);
    } catch (Exception e) {
      logger.log(Level.FINER, "Error while creating node '" + label + "'.", e);
      throw new CycleException(e);
    } finally {
      stopTransaction();

      if (temporaryFileStore != null) {
        deleteRecursively(temporaryFileStore);
      }
    }
  }

  @Threadsafe
  @Secured
  @Override
  public void deleteNode(ConnectorNode node, String message) {
    String id = node.getId();
    try {
      SVNUrl svnUrl = createSvnUrl(id);
      String defaultMessage = getDefaultCommitMessage("Removed '" + id + "' using camunda cycle.");
      if(message == null || message.length()==0) {
        message = defaultMessage;
      }
      svnClientAdapter.remove(new SVNUrl[] {svnUrl}, message);
    } catch (Exception e) {
      logger.log(Level.FINER, "Error while deleting node '" + id + "'.", e);
      throw new CycleException(e);
    }
  }

  protected String getDefaultCommitMessage(String string) {
    if(cycleConfiguration != null) {
      return cycleConfiguration.getDefaultCommitMessage();
    } else {
      return string;
    }
  }

  @Threadsafe
  @Secured
  @Override
  public ContentInformation updateContent(ConnectorNode node, InputStream newContent, String message) {
    File temporaryFileStore = null;

    try {
      beginTransaction();

      temporaryFileStore = getTemporaryFileStore(UUID.randomUUID().toString());
      String parentFolderId = extractParentFolder(node);
      
      SVNUrl svnUrl = createSvnUrl(parentFolderId);
      checkout(svnUrl, temporaryFileStore);
      
      File file = new File(temporaryFileStore.getAbsolutePath() + File.separator + node.getLabel());
      
      BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
      IOUtils.copy(newContent, bos);
      bos.flush();
      bos.close();
      
      String defaultMessage = getDefaultCommitMessage("Updated file '" + node.getLabel() + "' in '" + parentFolderId + "' using camunda cycle");
      if(message == null || message.length()==0) {
        message = defaultMessage;
      }
      
      commit(new File[] {temporaryFileStore}, message);

      return getContentInformation(node);
    } catch (Exception e) {
      logger.log(Level.FINER, "Error while updating file '" + node.getLabel() + "' in '" + extractParentFolder(node) + "'.", e);
      throw new CycleException(e);
    } finally {
      stopTransaction();

      if (temporaryFileStore != null) {
        deleteRecursively(temporaryFileStore);
      }
    }
  }
  
  private void beginTransaction() {
    transactionLock.lock();
  }
  
  private void stopTransaction() {
    while (true) {
      try {
        transactionLock.unlock();
      } catch (Exception e) {
        break;
      }
    }
  }
  
  private File getTemporaryFileStore(String withSubFolder) {
    return new File(baseTemporaryFileStore + File.separator + withSubFolder);
  }
  
  private void checkout(SVNUrl source, File target) {
    try {
      svnClientAdapter.checkout(source, target, SVNRevision.HEAD, false);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Could not checkout from svn repository '" + source + "' to the following destination '" + target.getAbsolutePath() + "'.", e);
      throw new CycleException(e);
    }
  }
  
  private void commit(File[] sources, String comment) {
    try {
      svnClientAdapter.commit(sources, comment, true);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Could not commit changes in '" + sources[0] + "'.", e);
      throw new CycleException(e);
    }
  }

  private String extractParentFolder(ConnectorNode node) {
    return extractParentFolder(node.getId());
  }
  
  private String extractParentFolder(String nodeId) {
    String parentFolderId = "";
    if (nodeId.contains("/")) {
      parentFolderId = nodeId.substring(0, nodeId.lastIndexOf("/"));
    }
    return parentFolderId;
  }
  
  private boolean deleteRecursively(File file) {
    if (!file.exists()) {
      return false;
    }
    if (file.isFile()) {
      return file.delete();
    }
    boolean result = true;
    File[] children = file.listFiles();
    for (int i = 0; i < children.length; i++) {
      result &= deleteRecursively(children[i]);
    }
    result &= file.delete();
    return result;
  }

  @Threadsafe
  @Secured
  @Override
  public InputStream getContent(ConnectorNode node) {
    try {
      return svnClientAdapter.getContent(createSvnUrl(getTypedNodeSpecificPath(node)), SVNRevision.HEAD);
    } catch (Exception e) {
      if (node.getType() == ConnectorNodeType.PNG_FILE) {
        return null;
      }
      throw new CycleException(e);
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
  
  @Threadsafe
  @Secured
  @Override
  public ContentInformation getContentInformation(ConnectorNode node) {
    ConnectorNode reloadedNode = null;
    try {
      // Try to load the current state (last modified date etc.) of the assigned node.
      reloadedNode = getNode(getTypedNodeSpecificPath(node));
    } catch (Exception e) {
      return ContentInformation.notFound();
    }
    
    if (reloadedNode == null) {
      return ContentInformation.notFound();
    }
    
    if (reloadedNode.getType() == ConnectorNodeType.FOLDER) {
      throw new IllegalArgumentException("Can only get content information from files"); 
    }
    
    return new ContentInformation(true, reloadedNode.getLastModified());
  }
  
  @Override
  public void dispose() {
    if (svnClientAdapter != null) {
      svnClientAdapter.dispose();
      svnClientAdapter = null;
    }
    loggedIn = false;
  }
  
  @Override
  public boolean isSupportsCommitMessage() {
    return true;
  }
}
