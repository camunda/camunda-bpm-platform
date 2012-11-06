package com.camunda.fox.cycle.connector.signavio;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.plexus.util.IOUtil;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ClientResponseFailure;
import org.jboss.resteasy.client.core.BaseClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.spi.interception.ClientExecutionContext;
import org.jboss.resteasy.spi.interception.ClientExecutionInterceptor;
import org.springframework.stereotype.Component;

import com.camunda.fox.cycle.connector.Connector;
import com.camunda.fox.cycle.connector.ConnectorNode;
import com.camunda.fox.cycle.connector.ConnectorNodeType;
import com.camunda.fox.cycle.connector.ContentInformation;
import com.camunda.fox.cycle.connector.Secured;
import com.camunda.fox.cycle.entity.ConnectorConfiguration;
import com.camunda.fox.cycle.exception.CycleException;
import com.camunda.fox.cycle.util.IoUtil;

@Component
@Path("signavio")
public class SignavioConnector extends Connector {

  public final static String CONFIG_KEY_SIGNAVIO_BASE_URL = "signavioBaseUrl";
  
  private static final String WARNING_SNIPPET = "<div id=\"warning\">([^<]+)</div>";
  
  // JSON properties/objects
  private static final String JSON_REP_OBJ = "rep";
  private static final String JSON_REL_PROP = "rel";
  private static final String JSON_HREF_PROP = "href";
  private static final String JSON_NAME_PROP = "name";
  private static final String JSON_TITLE_PROP = "title";
  private static final String JSON_PARENT_PROP = "parent";
  private static final String JSON_PARENT_NAME_PROP = "parentName";
  private static final String JSON_TYPE_PROP = "type";
  private static final String JSON_UPDATED_PROP = "updated";
  
  // JSON values
  private static final String JSON_DIR_VALUE = "dir";
  private static final String JSON_MOD_VALUE = "mod";
  private static final String JSON_PRIVATE_VALUE = "private";
  private static final String JSON_TYPE_BPMN20_VALUE = "BPMN 2.0";
  
  private static final String REPOSITORY_BACKEND_URL_SUFFIX = "p/";
  private static final String MODEL_URL_SUFFIX = "model";
  private static final String DIRECTORY_URL_SUFFIX = "directory";
  private static final String BPMN2_0_IMPORT_SUFFIX = "bpmn2_0-import";
  
  private static final String SLASH_CHAR = "/";
  private static final String MODEL_NAME_TEMPLATE = "cycle-import_";
  
  private static final String BPMN2_0_FILE_PROP = "bpmn2_0file";
  private static final String X_SIGNAVIO_ID_PROP = "x-signavio-id";

  private static final String UTF_8 = "UTF-8";

  private static Logger logger = Logger.getLogger(SignavioConnector.class.getName());
  
  private SignavioClient signavioClient;
  private boolean loggedIn = false;

  private ApacheHttpClient4Executor httpClient4Executor;
  private String securityToken;

  @Override
  public void login(String username, String password) {
    SignavioLoginForm loginForm = new SignavioLoginForm(username, password, "true");
    Response response = this.signavioClient.login(loginForm);
    
    String responseResult = this.extractResponseResult(response);
    if (responseResult == null || responseResult.equals("")) {
      throw new CycleException("Failed to login to connector. The user name and/or password might be incorrect.");
    }
    Matcher matcher = Pattern.compile(WARNING_SNIPPET).matcher(responseResult);
    if (matcher.find()) {
      String errorMessage = matcher.group(1);
      throw new CycleException(errorMessage);
    }
    
    if (responseResult.matches("[a-f0-9]{32}")) {
      this.securityToken = responseResult;
      logger.fine("SecurityToken: " + this.securityToken);
    }
    
    this.loggedIn = true;
  }
  
  @Override
  public boolean needsLogin() {
    return !loggedIn;
  }
  
  @Override
  public void init(ConnectorConfiguration config) {
    if (this.signavioClient == null) {
      ResteasyProviderFactory providerFactory = ResteasyProviderFactory.getInstance();
      RegisterBuiltin.register(providerFactory);

      String signavioURL = (String) config.getProperties().get(CONFIG_KEY_SIGNAVIO_BASE_URL);
      if (signavioURL.endsWith(SLASH_CHAR)) {
        signavioURL = signavioURL + REPOSITORY_BACKEND_URL_SUFFIX; 
      } else {
        signavioURL = signavioURL + SLASH_CHAR + REPOSITORY_BACKEND_URL_SUFFIX;
      }
      
      // Use Thread safe connection manager, prevents abortion of ctx.proceed in interceptor if multiple requests are done at once
      DefaultHttpClient client = new DefaultHttpClient();
      final ClientConnectionManager mgr = client.getConnectionManager();
      final HttpParams params = client.getParams();
      
      final ThreadSafeClientConnManager connectionManager = new ThreadSafeClientConnManager(mgr.getSchemeRegistry(), 5000, TimeUnit.MILLISECONDS);
      
      HttpConnectionParams.setConnectionTimeout(params, 3000);
      HttpConnectionParams.setStaleCheckingEnabled(params, true);
      HttpConnectionParams.setLinger(params, 5000);
      connectionManager.setDefaultMaxPerRoute(5);
      
      final DefaultHttpClient signavioHttpClient = new DefaultHttpClient(connectionManager, params);
      httpClient4Executor = new ApacheHttpClient4Executor(signavioHttpClient);
      signavioHttpClient.setReuseStrategy(new NoConnectionReuseStrategy());
      
      ClientRequestFactory factory = null;
      try {
        factory = new ClientRequestFactory(httpClient4Executor, new URI(signavioURL));
      } catch (Exception e) {
        throw new CycleException("The connection to the signavio client could not be initialized.", e);
      }
      factory.getPrefixInterceptors().registerInterceptor(new ClientExecutionInterceptor() {
        
        @SuppressWarnings("rawtypes")
        @Override
        public ClientResponse execute(ClientExecutionContext ctx) throws Exception {
          connectionManager.closeExpiredConnections();
          connectionManager.closeIdleConnections(2000, TimeUnit.MILLISECONDS);
          
          String uri = "";
          ClientRequest request = ctx.getRequest();
          uri = request.getUri().toString();
          logger.fine("Sending request to " + uri);
          logger.fine("Request: " + request.getHeaders()+ "," + request.getBody());
          if (SignavioConnector.this.securityToken != null) {
            request.header(X_SIGNAVIO_ID_PROP, SignavioConnector.this.securityToken);
          }
          
          ClientResponse<?> response =  ctx.proceed();
          logger.fine("Received response from " + uri + " with status " + response.getStatus());
          return response;
        }
      });
      this.signavioClient = factory.createProxy(SignavioClient.class, signavioURL);
    }
  }

  @Override
  public void dispose() {
    if (this.httpClient4Executor != null) {
      this.httpClient4Executor.getHttpClient().getConnectionManager().shutdown();
      this.httpClient4Executor = null;
      this.signavioClient = null;
      this.loggedIn = false;
    }
  }

  // Connector API methods //////////////////////////////////////////////
  
  @Override
  public void deleteNode(final ConnectorNode node) {
    
    executeCommand(new Command<Void>("delete node") {
      
      @Override
      public Void execute() throws Exception {
        signavioClient.delete(extractType(node), node.getId());
        return null;
      }
    });
  }

  @Override
  public ConnectorNode createNode(final String parentId, final String label, final ConnectorNodeType type) {
    
    return executeCommand(new Command<ConnectorNode>("create node") {
      
      @Override
      public ConnectorNode execute() throws Exception {
        InputStream emptyJson = null;
        try {
          String response = "";
          ConnectorNode result = null;
          switch (type) {
            case FOLDER:
              SignavioCreateFolderForm form = new SignavioCreateFolderForm(label, "", parentId);
              response = signavioClient.createFolder(form);
              result = createFolderNode(new JSONObject(response));
              break;
            case BPMN_FILE:
              SignavioCreateModelForm newModelForm = new SignavioCreateModelForm();

              newModelForm.setId(UUID.randomUUID().toString().replace("-", ""));
              newModelForm.setName(label);
              newModelForm.setComment("");
              newModelForm.setDescription("");
              newModelForm.setParent(parentId);

              emptyJson = getClass().getClassLoader().getResourceAsStream("com/camunda/fox/cycle/connector/emptyProcessModelTemplate.json");

              newModelForm.setJsonXml(new String(IoUtil.readInputStream(emptyJson, "emptyProcessModelTemplate.json"), UTF_8));
              newModelForm.setSVG_XML("<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:oryx=\"http://oryx-editor.org\" id=\"sid-80D82B67-3B30-4B35-A6CB-16EEE17A719F\" width=\"50\" height=\"50\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:svg=\"http://www.w3.org/2000/svg\"><defs/><g stroke=\"black\" font-family=\"Verdana, sans-serif\" font-size-adjust=\"none\" font-style=\"normal\" font-variant=\"normal\" font-weight=\"normal\" line-heigth=\"normal\" font-size=\"12\"><g class=\"stencils\" transform=\"translate(25, 25)\"><g class=\"me\"/><g class=\"children\"/><g class=\"edge\"/></g></g></svg>");

              response = signavioClient.createModel(newModelForm);
              result = createFileNode(new JSONObject(response));
              break;
          }
          
          if (result != null) {
            result.setConnectorId(getConfiguration().getId());
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
        String result = signavioClient.getChildren(parent.getId());
        JSONArray jsonArray = new JSONArray(result);
        for (int i = 0; i < jsonArray.length(); i++) {
          JSONObject jsonObj = jsonArray.getJSONObject(i);

          ConnectorNode newNode = null;
          String relProp = jsonObj.getString(JSON_REL_PROP);
          if (relProp.equals(JSON_DIR_VALUE)) {
            newNode = createFolderNode(jsonObj);
            nodes.add(newNode);
          } else if (relProp.equals(JSON_MOD_VALUE)) {
            newNode = createFileNode(jsonObj);
            nodes.add(newNode);
          }
          if (newNode != null) {
            newNode.setConnectorId(getConfiguration().getId());  
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
          return wrapStream(signavioClient.getPngContent(node.getId()));

        default:
          return wrapStream(signavioClient.getContent(node.getId()));
        }
      }
    });
  }

  @Secured
  @Override
  public ConnectorNode getRoot() {
    ConnectorNode rootNode = new ConnectorNode(SLASH_CHAR, SLASH_CHAR);
    rootNode.setType(ConnectorNodeType.FOLDER);
    return rootNode;
  }

  @Secured
  @Override
  public ContentInformation updateContent(final ConnectorNode node, final InputStream newContent) throws Exception {
    return executeCommand(new Command<ContentInformation>("get content information") {

      @Override
      public ContentInformation execute() throws Exception {
        ConnectorNode privateFolder = getPrivateFolder();
        ConnectorNode importedModel = importContent(privateFolder, IOUtil.toString(newContent, UTF_8));
        String json = signavioClient.getJson(importedModel.getId());
        String svg = signavioClient.getSVG(importedModel.getId());
        deleteNode(importedModel);

        ConnectorNode parent = getParent(node);
        saveModel(parent, node.getId(), node.getLabel(), json, svg, "", "", true);

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

  private ConnectorNode createFolderNode(JSONObject jsonObj) throws JSONException {
    ConnectorNode node = new ConnectorNode();
    node.setType(ConnectorNodeType.FOLDER);
    node.setLabel(extractNodeName(jsonObj));
    
    String href = jsonObj.getString(JSON_HREF_PROP);
    href = href.replace(SLASH_CHAR + DIRECTORY_URL_SUFFIX, "");
    node.setId(href);
    
    return node;
  }
  
  private ConnectorNode createFileNode(JSONObject jsonObj) throws JSONException {
    ConnectorNode node = new ConnectorNode();
    node.setLabel(extractNodeName(jsonObj));
    
    String href = jsonObj.getString(JSON_HREF_PROP);
    href = href.replace(SLASH_CHAR + MODEL_URL_SUFFIX, "");
    node.setId(href);
    
    node.setType(extractContentType(jsonObj));
    return node;
  }
  
  private String extractNodeName(JSONObject jsonObj) throws JSONException {
    String label = "";
    JSONObject repJsonObj = jsonObj.getJSONObject(JSON_REP_OBJ);
    if (repJsonObj.has(JSON_NAME_PROP)) {
      label = repJsonObj.getString(JSON_NAME_PROP);
    } else if (repJsonObj.has(JSON_TITLE_PROP)) {
      label = repJsonObj.getString(JSON_TITLE_PROP);
    }
    
    return label;
  }
  
  private ConnectorNodeType extractContentType(JSONObject jsonObj) throws JSONException {
    JSONObject repJsonObj = jsonObj.getJSONObject(JSON_REP_OBJ);
    
    if (repJsonObj.has(JSON_TYPE_PROP) && JSON_TYPE_BPMN20_VALUE.equals(repJsonObj.getString(JSON_TYPE_PROP))) {
      return ConnectorNodeType.BPMN_FILE;
    } else {
      return ConnectorNodeType.ANY_FILE;
    }
  }
  
  @SuppressWarnings("unchecked")
  private String extractResponseResult(Response response) {
    BaseClientResponse<String> r = (BaseClientResponse<String>) response;
    byte[] data = r.getEntity(String.class).getBytes();
    this.releaseClientConnection(response);
    
    try {
      return new String(data, UTF_8);
    } catch (UnsupportedEncodingException e) {
      throw new CycleException(e.getMessage(), e);
    }
  }

  private Date getLastModifiedDate(ConnectorNode node) {
    try {
      String info = signavioClient.getInfo(MODEL_URL_SUFFIX, node.getId());
      JSONObject jsonObj = new JSONObject(info);
      String updated = jsonObj.getString(JSON_UPDATED_PROP);
      if (updated != null) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss Z");
        Date lastModifiedDate = dateFormatter.parse(updated);
        return lastModifiedDate;
      }
      
      return null;
      
    } catch (Exception e) {
      logger.log(Level.WARNING, "Could not get last modified date for " + node);
      return null;
    }
  }
  
  private boolean isContentAvailable(ConnectorNode node) {
    try {
      boolean result = false;
      InputStream stream = getContent(node);
      if (stream != null) {
        result = true;
      }
      IoUtil.closeSilently(stream);
      return result;
    } catch (Exception e) {
      return false;
    }
  }
  
  private InputStream wrapStream(InputStream inputStream) {
    try {
      return new ByteArrayInputStream(IOUtils.toByteArray(inputStream));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }finally{
      IOUtils.closeQuietly(inputStream);
    }
  }
  
  private void releaseClientConnection(Response response) {
    if (response instanceof ClientResponse<?>) {
      ClientResponse<?> r = (ClientResponse<?>) response;
      r.releaseConnection();
    }
  }
  
  private ConnectorNode getParent(ConnectorNode node) {
    try {
      String info = this.signavioClient.getInfo(this.extractType(node), node.getId());
      JSONObject jsonObj = new JSONObject(info);
      String parentId = jsonObj.getString(JSON_PARENT_PROP).replace(SLASH_CHAR + DIRECTORY_URL_SUFFIX, "");
      String parentName = jsonObj.getString(JSON_PARENT_NAME_PROP);
      ConnectorNode result = new ConnectorNode(parentId, parentName);
      result.setType(ConnectorNodeType.FOLDER);
      return result;
    } catch (Exception e) {
      throw new CycleException("The parent of node '" + node.getLabel() + "' could not be determined.", e);
    }
  }
  
  protected ConnectorNode getPrivateFolder() {
    try {
      String children = signavioClient.getChildren(this.getRoot().getId());
      JSONArray jsonArray = new JSONArray(children);
      for (int i = 0; i < jsonArray.length(); i++) {
        JSONObject jsonObj = jsonArray.getJSONObject(i);
        String rel = jsonObj.getString(JSON_REL_PROP);
        if (rel.equals(JSON_DIR_VALUE)) {
          JSONObject repJsonObj = jsonObj.getJSONObject(JSON_REP_OBJ);
          String type = repJsonObj.getString(JSON_TYPE_PROP);
          if (type.equals(JSON_PRIVATE_VALUE)) {
            ConnectorNode folder = new ConnectorNode();
            
            String href = jsonObj.getString(JSON_HREF_PROP);
            href = href.replace(SLASH_CHAR + DIRECTORY_URL_SUFFIX, "");
            folder.setId(href);
            folder.setType(ConnectorNodeType.FOLDER);
            folder.setLabel(extractNodeName(jsonObj));
            
            return folder;
          }
        }
      }
      // Could not determine private folder
      throw new CycleException("The private folder could not be determined.");
    } catch (JSONException e) {
      throw new CycleException("The private folder could not be determined.", e);
    }
  }
  
  protected ConnectorNode importContent(ConnectorNode parent, String content) throws Exception {
    String modelName = MODEL_NAME_TEMPLATE + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
    return this.importContent(parent, content, modelName);
  }
  
  protected ConnectorNode importContent(ConnectorNode parent, String content, final String modelName) throws Exception {
    HttpClient httpClient = this.httpClient4Executor.getHttpClient();
    String signavioURL = this.getConfiguration().getProperties().get(CONFIG_KEY_SIGNAVIO_BASE_URL);
    if (signavioURL.endsWith(SLASH_CHAR)) {
      signavioURL = signavioURL + REPOSITORY_BACKEND_URL_SUFFIX;
    } else {
      signavioURL = signavioURL + SLASH_CHAR + REPOSITORY_BACKEND_URL_SUFFIX;
    }
    HttpPost post = new HttpPost(signavioURL + BPMN2_0_IMPORT_SUFFIX);
    post.addHeader(X_SIGNAVIO_ID_PROP, this.securityToken);
    
    MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
    // creating a temporary file
    File tmpfile = File.createTempFile(modelName, ".xml");
    OutputStream os = new BufferedOutputStream(new FileOutputStream(tmpfile));
    InputStream is = new ByteArrayInputStream(content.getBytes(UTF_8));
    IoUtil.copyBytes(is, os);
    os.flush();
    os.close();
    entity.addPart(BPMN2_0_FILE_PROP, new FileBody(tmpfile){public String getFilename() { return modelName;};});
    entity.addPart(DIRECTORY_URL_SUFFIX, new StringBody(SLASH_CHAR + DIRECTORY_URL_SUFFIX + parent.getId(), Charset.forName(UTF_8)));
    post.setEntity(entity);
    
    HttpResponse postResponse = httpClient.execute(post);
    
    InputStream inputStream = postResponse.getEntity().getContent();
    StringWriter writer = new StringWriter();
    IOUtils.copy(inputStream, writer, UTF_8);
    String responseStream = writer.toString();
    
    if (responseStream != null && !responseStream.startsWith("[true]") && !responseStream.contains("\"errors\":[]")) {
      throw new CycleException("BPMN XML could not be imported because of model errors: " + responseStream); 
    }
    
    // check if something went wrong on Signavio side
    if (postResponse.getStatusLine().getStatusCode() >= 400) {
      logger.severe("Import of BPMN XML failed in Signavio.");
      logger.severe("Error response from server: " + EntityUtils.toString(postResponse.getEntity(), "UTF-8"));
      throw new CycleException("BPMN XML could not be imported: " + content);
    }

    return this.getChildNodeByName(parent, modelName);
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
        return MODEL_URL_SUFFIX;
      } else 
      if (t.equals(ConnectorNodeType.FOLDER)) {
        return DIRECTORY_URL_SUFFIX;
      }
    }
    
    throw new CycleException("The type of the selected node '" + node.getLabel() + "' could not be determined, so that the parent could not be loaded.");
  }

  private void saveModel(ConnectorNode parentFolder, String id, String modelName, String json, String svg,  String comment, String description, boolean update) throws Exception {
    SignavioCreateModelForm newModelForm = new SignavioCreateModelForm();
    
    if (id.startsWith(SLASH_CHAR)) {
      id = id.substring(1);
    }
    newModelForm.setId(id);

    if (modelName == null) {
      modelName = "";
    }
    newModelForm.setName(modelName);
    
    newModelForm.setJsonXml(new JSONObject(json).toString());
    
    if (svg == null || svg.isEmpty()) {
      svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:oryx=\"http://oryx-editor.org\" id=\"sid-80D82B67-3B30-4B35-A6CB-16EEE17A719F\" width=\"50\" height=\"50\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:svg=\"http://www.w3.org/2000/svg\"><defs/><g stroke=\"black\" font-family=\"Verdana, sans-serif\" font-size-adjust=\"none\" font-style=\"normal\" font-variant=\"normal\" font-weight=\"normal\" line-heigth=\"normal\" font-size=\"12\"><g class=\"stencils\" transform=\"translate(25, 25)\"><g class=\"me\"/><g class=\"children\"/><g class=\"edge\"/></g></g></svg>";
    }
    newModelForm.setSVG_XML(svg);
    
    if (comment == null) {
      comment = "";
    }
    newModelForm.setComment(comment);
    
    if (description == null) {
      description = "";
    }
    newModelForm.setDescription(description);
    
    newModelForm.setParent(parentFolder.getId());
    
    if (update) {
      this.signavioClient.updateModel(id, newModelForm);
    } else {
      this.signavioClient.createModel(newModelForm);
    }
  }
  
  protected SignavioClient getSignavioClient() {
    return this.signavioClient;
  }
  
  protected ApacheHttpClient4Executor getHttpClient4Executor() {
    return this.httpClient4Executor;
  }

  @Override
  public ConnectorNode getNode(String id) {
    throw new UnsupportedOperationException();
  }

  
  protected String getSecurityToken() {
    return securityToken;
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
    } catch (ClientResponseFailure e) {
      if (e.getResponse().getStatus() == 401) {
        throw new CycleException("Failed to authenticate (Status 401)");
      } else {
        throw new CycleException("Could not execute action", e);
      }
    } catch (Exception e) {
      throw new CycleException("Could not perform operation " + command.getOperation(), e);
    }
  }
  
  /**
   * Command to be executed in {@link SignavioConnector#executeCommand(com.camunda.fox.cycle.connector.signavio.SignavioConnector.Command) }.
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
