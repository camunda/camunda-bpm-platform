package com.camunda.fox.cycle.impl.connector.signavio;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
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
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.EntityUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.BaseClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.spi.interception.ClientExecutionContext;
import org.jboss.resteasy.spi.interception.ClientExecutionInterceptor;
import org.springframework.stereotype.Component;

import com.camunda.fox.cycle.api.connector.Connector;
import com.camunda.fox.cycle.api.connector.ConnectorNode;
import com.camunda.fox.cycle.api.connector.ConnectorNode.ConnectorNodeType;
import com.camunda.fox.cycle.api.connector.Secured;
import com.camunda.fox.cycle.entity.ConnectorConfiguration;
import com.camunda.fox.cycle.exception.RepositoryException;
import com.camunda.fox.cycle.util.IoUtil;

@Component
public class SignavioConnector extends Connector {

  public final static String CONFIG_KEY_SIGNAVIO_BASE_URL = "signavioBaseUrl";
  
  private static Logger log = Logger.getLogger(SignavioConnector.class.getName());
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
  // JSON values
  private static final String JSON_DIR_VALUE = "dir";
  private static final String JSON_MOD_VALUE = "mod";
  private static final String JSON_PRIVATE_VALUE = "private";

  private static final String REPOSITORY_BACKEND_URL_SUFFIX = "p/";
  private static final String MODEL_URL_SUFFIX = "model";
  private static final String DIRECTORY_URL_SUFFIX = "directory";
  private static final String BPMN2_0_IMPORT_SUFFIX = "bpmn2_0-import";
  
  private static final String SLASH_CHAR = "/";
  private static final String MODEL_NAME_TEMPLATE = "cycle-import_";
  
  private static final String BPMN2_0_FILE_PROP = "bpmn2_0file";

  private static final String UTF_8 = "UTF-8";
  
  private SignavioClient signavioClient;
  private boolean loggedIn = false;

  private ApacheHttpClient4Executor httpClient4Executor;

  @Override
  public void login(String username, String password) {
    SignavioLoginForm loginForm = new SignavioLoginForm(username, password, "true");
    Response response = this.signavioClient.login(loginForm);
    
    String responseResult = this.extractResponseResult(response);
    if (responseResult == null || responseResult.equals("")) {
      throw new RepositoryException("Failed to login to connector. The user name and/or password might be incorrect.");
    }
    Matcher matcher = Pattern.compile(WARNING_SNIPPET).matcher(responseResult);
    if (matcher.find()) {
      String errorMessage = matcher.group(1);
      throw new RepositoryException(errorMessage);
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
      
      httpClient4Executor = new ApacheHttpClient4Executor();
      
      ClientRequestFactory factory = null;
      try {
        factory = new ClientRequestFactory(httpClient4Executor, new URI(signavioURL));
      } catch (Exception e) {
        throw new RepositoryException("The connection to the signavio client could not be initialized.", e);
      }
      factory.getPrefixInterceptors().registerInterceptor(new ClientExecutionInterceptor() {
        
        @SuppressWarnings("rawtypes")
        @Override
        public ClientResponse execute(ClientExecutionContext ctx) throws Exception {
          // TODO: Logging
//          ClientRequest request = ctx.getRequest();
          ClientResponse<?> response =  ctx.proceed();
          return response;
        }
      });
      
      this.signavioClient = factory.createProxy(SignavioClient.class, signavioURL);
    }
  }

  @Secured
  @Override
  public List<ConnectorNode> getChildren(ConnectorNode parent) {
    List<ConnectorNode> nodes = new ArrayList<ConnectorNode>();
    try {
      String result = this.signavioClient.getChildren(parent.getId());
      JSONArray jsonArray = new JSONArray(result);
      for (int i = 0; i < jsonArray.length(); i++) {
        JSONObject jsonObj = jsonArray.getJSONObject(i);
        
        ConnectorNode newNode = null;
        String relProp = jsonObj.getString(JSON_REL_PROP);
        if (relProp.equals(JSON_DIR_VALUE)) {
          newNode = this.createFolderNode(jsonObj);
          nodes.add(newNode);
        } else if (relProp.equals(JSON_MOD_VALUE)) {
          newNode = this.createModelNode(jsonObj);
          nodes.add(newNode);
        }
      }
    } catch (Exception e) {
      throw new RepositoryException("Children for Signavio connector '" + this.getConfiguration().getLabel() + "' could not be loaded in repository '" + parent.getId() + "'.", e);
    }
    return nodes;
  }
  
  private ConnectorNode createFolderNode(JSONObject jsonObj) throws JSONException {
    ConnectorNode newNode = new ConnectorNode();
    newNode.setType(ConnectorNodeType.FOLDER);
    
    this.extractNodeName(jsonObj, newNode);
    
    String href = jsonObj.getString(JSON_HREF_PROP);
    href = href.replace(SLASH_CHAR + DIRECTORY_URL_SUFFIX, "");
    newNode.setId(href);
    
    return newNode;
  }
  
  private ConnectorNode createModelNode(JSONObject jsonObj) throws JSONException {
    ConnectorNode newNode = new ConnectorNode();
    newNode.setType(ConnectorNodeType.FILE);

    this.extractNodeName(jsonObj, newNode);
    
    String href = jsonObj.getString(JSON_HREF_PROP);
    href = href.replace(SLASH_CHAR + MODEL_URL_SUFFIX, "");
    newNode.setId(href);
    
    return newNode;
  }
  
  private void extractNodeName(JSONObject jsonObj, ConnectorNode node) throws JSONException {
    String label = "";
    JSONObject repJsonObj = jsonObj.getJSONObject(JSON_REP_OBJ);
    if (repJsonObj.has(JSON_NAME_PROP)) {
      label = repJsonObj.getString(JSON_NAME_PROP);
    } else if (repJsonObj.has(JSON_TITLE_PROP)) {
      label = repJsonObj.getString(JSON_TITLE_PROP);
    }
    node.setLabel(label);
  }
  
  @SuppressWarnings("unchecked")
  private String extractResponseResult(Response response) {
    BaseClientResponse<String> r = (BaseClientResponse<String>) response;
    byte[] data = r.getEntity(String.class).getBytes();
    this.releaseClientConnection(response);
    
    try {
      return new String(data, UTF_8);
    } catch (UnsupportedEncodingException e) {
      throw new RepositoryException(e.getMessage(), e);
    }
  }

  @Secured
  @Override
  public InputStream getContent(ConnectorNode node) {
    return this.signavioClient.getContent(node.getId());
  }
  
  private void releaseClientConnection(Response response) {
    if (response instanceof ClientResponse<?>) {
      ClientResponse<?> r = (ClientResponse<?>) response;
      r.releaseConnection();
    }
  }

  @Secured
  @Override
  public ConnectorNode getRoot() {
    ConnectorNode rootNode = new ConnectorNode(SLASH_CHAR, SLASH_CHAR);
    rootNode.setType(ConnectorNodeType.FOLDER);
    return rootNode;
  }

  @Secured
  public void updateContent(ConnectorNode node, String newContent) throws Exception {
    ConnectorNode privateFolder = this.getPrivateFolder();
    ConnectorNode importedModel = this.importContent(privateFolder, newContent);
    String json = this.signavioClient.getJson(importedModel.getId());
    String svg = this.signavioClient.getSVG(importedModel.getId());
    this.deleteNode(importedModel);
    
    ConnectorNode parent = this.getParent(node);
    this.saveModel(parent, node.getId(), node.getLabel(), json, svg, "", "", true);
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
      throw new RepositoryException("The parent of node '" + node.getLabel() + "' could not be determined.", e);
    }
  }
  
  protected ConnectorNode getPrivateFolder() {
    try {
      String children = this.signavioClient.getChildren(this.getRoot().getId());
      JSONArray jsonArray = new JSONArray(children);
      for (int i = 0; i < jsonArray.length(); i++) {
        JSONObject jsonObj = jsonArray.getJSONObject(i);
        String rel = jsonObj.getString(JSON_REL_PROP);
        if (rel.equals(JSON_DIR_VALUE)) {
          JSONObject repJsonObj = jsonObj.getJSONObject(JSON_REP_OBJ);
          String type = repJsonObj.getString(JSON_TYPE_PROP);
          if (type.equals(JSON_PRIVATE_VALUE)) {
            ConnectorNode privateFolder = new ConnectorNode();
            this.extractNodeName(jsonObj, privateFolder);
            
            String href = jsonObj.getString(JSON_HREF_PROP);
            href = href.replace(SLASH_CHAR + DIRECTORY_URL_SUFFIX, "");
            privateFolder.setId(href);
            privateFolder.setType(ConnectorNodeType.FOLDER);
            return privateFolder;
          }
        }
      }
    } catch (JSONException e) {
      throw new RepositoryException("The private folder could not be determined.", e);
    }
    throw new RepositoryException("The private folder could not be determined.");
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
      throw new RepositoryException("BPMN XML could not be imported because of model errors: " + responseStream); 
    }
    
    // check if something went wrong on Signavio side
    if (postResponse.getStatusLine().getStatusCode() >= 400) {
      log.severe("Import of BPMN XML failed in Signavio.");
      log.severe("Error response from server: " + EntityUtils.toString(postResponse.getEntity(), "UTF-8"));
      throw new RepositoryException("BPMN XML could not be imported: " + content);
    }

    return this.getChildNodeByName(parent, modelName);
  }
  
  private ConnectorNode getChildNodeByName(ConnectorNode parent, String nodeName) {
    List<ConnectorNode> children = this.getChildren(parent);
    for (ConnectorNode connectorNode : children) {
      if (connectorNode.getLabel().equals(nodeName)) {
        return connectorNode;
      }
    }
    throw new RepositoryException("A node named '" + nodeName + "' could not be found in '" + parent.getLabel() + "'.");
  }
  
  private void deleteNode(ConnectorNode node) {
    this.signavioClient.delete(this.extractType(node), node.getId());
  }
  
  private String extractType(ConnectorNode node) {
    if (node.getType() != null && node.getType().equals(ConnectorNodeType.FILE)) {
      return MODEL_URL_SUFFIX;
    } else if (node.getType().equals(ConnectorNodeType.FOLDER)) {
      return DIRECTORY_URL_SUFFIX;
    } else {
      throw new RepositoryException("The type of the selected node '" + node.getLabel() + "' could not be determined, so that the parent could not be loaded.");
    }
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
  
}
