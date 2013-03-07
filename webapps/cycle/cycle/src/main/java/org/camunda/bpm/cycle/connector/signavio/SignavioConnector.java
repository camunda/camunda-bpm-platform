package org.camunda.bpm.cycle.connector.signavio;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.stereotype.Component;

import org.camunda.bpm.cycle.configuration.CycleConfiguration;
import org.camunda.bpm.cycle.connector.Connector;
import org.camunda.bpm.cycle.connector.ConnectorNode;
import org.camunda.bpm.cycle.connector.ConnectorNodeType;
import org.camunda.bpm.cycle.connector.ContentInformation;
import org.camunda.bpm.cycle.connector.Secured;
import org.camunda.bpm.cycle.entity.ConnectorConfiguration;
import org.camunda.bpm.cycle.exception.CycleException;
import org.camunda.bpm.cycle.http.client.HttpResponseException;
import org.camunda.bpm.cycle.util.IoUtil;

@Component
public class SignavioConnector extends Connector {
  
  // custom config properties
  public final static String CONFIG_KEY_SIGNAVIO_BASE_URL = "signavioBaseUrl";
  public final static String CONFIG_KEY_PROXY_URL = "proxyUrl";
  public final static String CONFIG_KEY_PROXY_USERNAME = "proxyUsername";
  public final static String CONFIG_KEY_PROXY_PASSWORD = "proxyPassword";
  
  // JSON properties/objects
  private static final String JSON_REL_PROP = "rel";
  
  // JSON values
  private static final String JSON_DIR_VALUE = "dir";
  private static final String JSON_MOD_VALUE = "mod";
  
  private static final String MODEL_NAME_TEMPLATE = "cycle-import_";
  
  private static final String UTF_8 = "UTF-8";

  private static Logger logger = Logger.getLogger(SignavioConnector.class.getName());
  
  private SignavioClient signavioClient;
  private boolean loggedIn = false;
  
  @Inject 
  private CycleConfiguration cycleConfiguration;

  @Override
  public void login(String username, String password) {
    if (getSignavioClient() == null) {
      ConnectorConfiguration connectorConfiguration = getConfiguration();
      init(connectorConfiguration);
    }
    getSignavioClient().login(username, password);
    loggedIn = true;
  }

  @Override
  public boolean needsLogin() {
    return !loggedIn;
  }
  
  @Override
  public void init(ConnectorConfiguration config) {
    try {
      if (getSignavioClient() == null) {
        
        String defaultCommitMessage = getDefaultCommitMessage();
        
        signavioClient = new SignavioClient(getConfiguration().getName(),
                                            getConfiguration().getProperties().get(CONFIG_KEY_SIGNAVIO_BASE_URL),
                                            getConfiguration().getProperties().get(CONFIG_KEY_PROXY_URL),
                                            getConfiguration().getProperties().get(CONFIG_KEY_PROXY_USERNAME),
                                            getConfiguration().getProperties().get(CONFIG_KEY_PROXY_PASSWORD),
                                            defaultCommitMessage);
      }
    } catch (URISyntaxException e) {
      throw new CycleException("Unable to initialize Signavio REST client for connector '" + getConfiguration().getName() + "'!", e);
    }
  }

  protected String getDefaultCommitMessage() {
    if(cycleConfiguration != null) {
      return cycleConfiguration.getDefaultCommitMessage();
    } else {
      return "";
    }
  }

  @Override
  public void dispose() {
    if (getSignavioClient() != null) {
      getSignavioClient().dispose();
      signavioClient = null;
      loggedIn = false;
    }
  }

  // Connector API methods //////////////////////////////////////////////
  
  @Override
  public void deleteNode(final ConnectorNode node, String message) {
    // message is ignored by signavio connector
    
    executeCommand(new Command<Void>("delete node") {
      
      @Override
      public Void execute() throws Exception {
        getSignavioClient().delete(extractType(node), node.getId());
        return null;
      }
    });
  }

  @Override
  public ConnectorNode createNode(final String parentId, final String label, final ConnectorNodeType type, final String message) {
    
    return executeCommand(new Command<ConnectorNode>("create node") {
      
      @Override
      public ConnectorNode execute() throws Exception {
        InputStream emptyJson = null;
        try {
          String response = "";
          ConnectorNode result = null;
          switch (type) {
            case FOLDER:
              response = getSignavioClient().createFolder(label, parentId);
              result = createFolderNode(new JSONObject(response));
              break;
            case BPMN_FILE:
              response = getSignavioClient().createModel(parentId, label, message);
              result = createFileNode(new JSONObject(response));
              break;
          }
          
          if (result != null) {
            result.setConnectorId(getId());
          }
          return result;
        } finally {
          IoUtil.closeSilently(emptyJson);
        }
      }
    });
  }
  
  @Secured
  @Override
  public List<ConnectorNode> getChildren(final ConnectorNode parent) {
    
    return executeCommand(new Command<List<ConnectorNode>>("get children") {
      
      @Override
      public List<ConnectorNode> execute() throws Exception {
        
        List<ConnectorNode> nodes = new ArrayList<ConnectorNode>();
        String result = getSignavioClient().getChildren(parent.getId());
        JSONArray jsonArray = new JSONArray(result);
        for (int i = 0; i < jsonArray.length(); i++) {
          JSONObject jsonObj = jsonArray.getJSONObject(i);

          ConnectorNode newNode = null;
          String relProp = jsonObj.getString(JSON_REL_PROP);
          if (relProp.equals(JSON_DIR_VALUE)) {
            newNode = createFolderNode(jsonObj);
          } else if (relProp.equals(JSON_MOD_VALUE)) {
            newNode = createFileNode(jsonObj);
          }
          if (newNode != null) {
            newNode.setConnectorId(getId());  
            nodes.add(newNode);
          }
        }
        
        return nodes;
      }
    });
  }

  @Secured
  @Override
  public InputStream getContent(final ConnectorNode node) {
    return executeCommand(new Command<InputStream>("get content information") {
      @Override
      public InputStream execute() throws Exception {
        ConnectorNodeType type = node.getType();

        switch (type) {
        case PNG_FILE:
          return wrapStream(getSignavioClient().getPngContent(node.getId()));

        default:
          return wrapStream(getSignavioClient().getXmlContent(node.getId()));
        }
      }
    });
  }

  @Secured
  @Override
  public ConnectorNode getRoot() {
    ConnectorNode rootNode = new ConnectorNode(SignavioClient.SLASH_CHAR, SignavioClient.SLASH_CHAR);
    rootNode.setType(ConnectorNodeType.FOLDER);
    return rootNode;
  }

  @Secured
  @Override
  public ContentInformation updateContent(final ConnectorNode node, final InputStream newContent, final String message) throws Exception {
    return executeCommand(new Command<ContentInformation>("get content information") {

      @Override
      public ContentInformation execute() throws Exception {
        ConnectorNode privateFolder = getPrivateFolder();
        ConnectorNode importedModel = importContent(privateFolder, IoUtil.toString(newContent, UTF_8));
        String json = getSignavioClient().getModelAsJson(importedModel.getId());
        String svg = getSignavioClient().getModelAsSVG(importedModel.getId());
        deleteNode(importedModel, null);

        ConnectorNode parent = getParent(node);
        
        getSignavioClient().updateModel(node.getId(), node.getLabel(), json, svg, parent.getId(), message);

        return getContentInformation(node);
      }
    });
  }

  @Override
  @Secured
  public ContentInformation getContentInformation(final ConnectorNode node) {
    return executeCommand(new Command<ContentInformation>("get content information") {

      @Override
      public ContentInformation execute() throws Exception {
        return new ContentInformation(isContentAvailable(node), getLastModifiedDate(node));
      }
    });
  }

  // private or protected utilities /////////////////////////////////////////

  private ConnectorNode createFolderNode(JSONObject jsonObj) {
    ConnectorNode node = new ConnectorNode();
    node.setType(ConnectorNodeType.FOLDER);
    node.setLabel(SignavioJson.extractNodeName(jsonObj));
    node.setId(SignavioJson.extractDirectoryId(jsonObj));
    
    return node;
  }
  
  private ConnectorNode createFileNode(JSONObject jsonObj) {
    ConnectorNode node = new ConnectorNode();
    node.setLabel(SignavioJson.extractNodeName(jsonObj));
    node.setId(SignavioJson.extractModelId(jsonObj));
    node.setType(SignavioJson.extractModelContentType(jsonObj));
    node.setMessage(SignavioJson.extractModelComment(jsonObj));
    
    return node;
  }
  
  private Date getLastModifiedDate(ConnectorNode node) {
    Date lastModifiedDate = null;
    
    try {
      String info = getSignavioClient().getInfo(SignavioClient.MODEL_URL_SUFFIX, node.getId());
      String updated = SignavioJson.extractLastModifiedDateFromInfo(info);
      if (updated != null) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss Z");
        lastModifiedDate = dateFormatter.parse(updated);
      }
    } catch (Exception e) {
      logger.log(Level.WARNING, "Could not get last modified date for " + node);
    }
    
    return lastModifiedDate;
  }
  
  private boolean isContentAvailable(ConnectorNode node) {
    InputStream in = null;
    boolean result = false;
    
    try {
      in = getContent(node);
      if (in != null) {
        result = true;
      }
    } catch (Exception e) {
      logger.log(Level.WARNING, "No content available for " + node, e);
    } finally {
      IoUtil.closeSilently(in);
    }
    
    return result;
  }
  
  private InputStream wrapStream(InputStream inputStream) {
    try {
      return new ByteArrayInputStream(IoUtil.readInputStream(inputStream, "PNG_or_XML_InputStream"));
    } finally {
      IoUtil.closeSilently(inputStream);
    }
  }
  
  private ConnectorNode getParent(ConnectorNode node) {
    try {
      String info = getSignavioClient().getInfo(extractType(node), node.getId());
      String parentId = SignavioJson.extractParentIdFromInfo(info);
      String parentName = SignavioJson.extractParentNameFromInfo(info);
      ConnectorNode result = new ConnectorNode(parentId, parentName);
      result.setType(ConnectorNodeType.FOLDER);
      return result;
    } catch (Exception e) {
      throw new CycleException("The parent of node '" + node.getLabel() + "' could not be determined.", e);
    }
  }
  
  @Secured
  public ConnectorNode getPrivateFolder() {
    try {
      String children = getSignavioClient().getChildren(getRoot().getId());
      return new ConnectorNode(SignavioJson.extractPrivateFolderId(children),
                               SignavioJson.extractPrivateFolderName(children),
                               ConnectorNodeType.FOLDER);
    } catch (RuntimeException e) {
      throw new CycleException("The private folder could not be determined.", e);
    }
  }
  
  protected ConnectorNode importContent(ConnectorNode parent, String content) throws Exception {
    String modelName = MODEL_NAME_TEMPLATE + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
    return importContent(parent, content, modelName);
  }
  
  protected ConnectorNode importContent(ConnectorNode parent, String content, final String modelName) throws Exception {
    getSignavioClient().importBpmnXml(parent.getId(), content, modelName);
    
    return getChildNodeByName(parent, modelName);
  }
  
  @Secured
  public List<ConnectorNode> importSignavioArchive(ConnectorNode parentFolder, String signavioArchive) throws Exception {
    getSignavioClient().importSignavioArchive(parentFolder.getId(), signavioArchive);
    
    return getChildren(parentFolder);
  }
  
  private ConnectorNode getChildNodeByName(ConnectorNode parent, String nodeName) {
    List<ConnectorNode> children = getChildren(parent);
    for (ConnectorNode connectorNode : children) {
      if (connectorNode.getLabel().equals(nodeName)) {
        return connectorNode;
      }
    }
    throw new CycleException("A node named '" + nodeName + "' could not be found in '" + parent.getLabel() + "'.");
  }
  
  private String extractType(ConnectorNode node) {
    ConnectorNodeType t = node.getType();
    
    if (t != null) {
      if (t.isFile()) {
        return SignavioClient.MODEL_URL_SUFFIX;
      } else 
      if (t.equals(ConnectorNodeType.FOLDER)) {
        return SignavioClient.DIRECTORY_URL_SUFFIX;
      }
    }
    
    throw new CycleException("The type of the selected node '" + node.getLabel() + "' could not be determined, so that the parent could not be loaded.");
  }

  protected SignavioClient getSignavioClient() {
    return signavioClient;
  }
  
  @Override
  public ConnectorNode getNode(String id) {
    throw new UnsupportedOperationException();
  }
  
  public boolean isSupportsCommitMessage() {
    return true;
  }

  // Signavio Connector command execution ///////////////////////////////////////
  
  /**
   * Execute a command and catch / process a number of specific errors
   * 
   * @param <T>
   * @param command
   * @return 
   */
  protected <T> T executeCommand(Command<T> command) {
    try {
      T result = command.execute();
      return result;
    } catch (HttpResponseException e) {
      if (e.getStatusCode() == 401) {
        throw new CycleException("Failed to authenticate (Status 401)", e);
      } else {
        throw new CycleException("Could not execute action", e);
      }
    } catch (Exception e) {
      throw new CycleException("Could not perform operation " + command.getOperation(), e);
    }
  }
  
  /**
   * Command to be executed in {@link SignavioConnector#executeCommand(org.camunda.bpm.cycle.connector.signavio.SignavioConnector.Command) }.
   * TODO do we want this for all connectors? thinking about future connectors like git
   * @param <T> 
   */
  protected abstract static class Command<T> {

    private String operation;
    
    protected Command(String operation) {
      this.operation = operation;
    }

    public String getOperation() {
      return operation;
    }

    /**
     * Execute a signavio action
     * @return 
     */
    public abstract T execute() throws Exception;
  }
}
