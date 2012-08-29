package com.camunda.fox.cycle.impl.connector.signavio;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.client.core.BaseClientResponse;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.springframework.stereotype.Component;

import com.camunda.fox.cycle.api.connector.Connector;
import com.camunda.fox.cycle.api.connector.ConnectorNode;
import com.camunda.fox.cycle.api.connector.ConnectorNode.ConnectorNodeType;
import com.camunda.fox.cycle.api.connector.Secured;
import com.camunda.fox.cycle.exception.RepositoryException;
import com.camunda.fox.cycle.exception.RepositoryNodeNotFoundException;

@Component
public class SignavioConnector extends Connector {

  public final static String CONFIG_KEY_SIGNAVIO_BASE_URL = "signavioBaseUrl";

  // JSON properties/objects
  private static final String JSON_REP_OBJ = "rep";
  private static final String JSON_REL_PROP = "rel";
  private static final String JSON_HREF_PROP = "href";
  private static final String JSON_NAME_PROP = "name";
  private static final String JSON_TITLE_PROP = "title";
  // JSON values
  private static final String JSON_DIR_VALUE = "dir";
  private static final String JSON_MOD_VALUE = "mod";

  private static final String REPOSITORY_BACKEND_URL_SUFFIX = "p/";
  private static final String MODEL_URL_SUFFIX = "/model";
  private static final String DIRECTORY_URL_SUFFIX = "/directory";

  private SignavioClient signavioClient;

  @Override
  public void login(String username, String password) {
    this.initializeSignavioClient();

    SignavioLoginForm loginForm = new SignavioLoginForm(username, password, "true");
    Response response = this.signavioClient.login(loginForm);
    
    String responseResult = this.extractResponseResult(response);
    if (responseResult == null || responseResult.equals("")) {
      throw new RepositoryException("No Security Token received. The user name and/or password might be incorrect.");
    }
    Matcher matcher = Pattern.compile("<div id=\"warning\">([^<]+)</div>").matcher(responseResult);
    if (matcher.find()) {
      String errorMessage = matcher.group(1);
      throw new RepositoryException(errorMessage);
    }
  }
  
  private void initializeSignavioClient() {
    if (this.signavioClient == null) {
      ResteasyProviderFactory providerFactory = ResteasyProviderFactory.getInstance();
      RegisterBuiltin.register(providerFactory);

      String signavioURL = (String) this.getConfigValue(CONFIG_KEY_SIGNAVIO_BASE_URL);
      if (signavioURL.endsWith("/")) {
        signavioURL = signavioURL + REPOSITORY_BACKEND_URL_SUFFIX; 
      } else {
        signavioURL = signavioURL + "/" + REPOSITORY_BACKEND_URL_SUFFIX;
      }
      this.signavioClient = ProxyFactory.create(SignavioClient.class, signavioURL);
    }
  }

  @Secured
  @Override
  public List<ConnectorNode> getChildren(ConnectorNode parent) {
    this.login("test@camunda.com", "testtest1"); // TODO: Remove it!
    
    List<ConnectorNode> nodes = new ArrayList<ConnectorNode>();
    try {
      this.initializeSignavioClient();
      String result = this.signavioClient.getChildren(parent.getPath());
      JSONArray jsonArray = new JSONArray(result);
      for (int i = 0; i < jsonArray.length(); i++) {
        JSONObject jsonObj = jsonArray.getJSONObject(i);
        
        ConnectorNode newNode = null;
        String relProp = jsonObj.getString(JSON_REL_PROP);
        if (relProp.equals(JSON_DIR_VALUE)) {
          newNode = this.createFolderNode(jsonObj);
          
        } else if (relProp.equals(JSON_MOD_VALUE)) {
          newNode = this.createModelNode(jsonObj);
        }
        if (newNode != null) {
          newNode.setPath("/" + newNode.getName());
          nodes.add(newNode);
        }
      }
      
    } catch (Exception e) {
      throw new RepositoryNodeNotFoundException(this.getName(), ConnectorNode.class, parent.getName(), e);
    }
    return nodes;
  }
  
  private ConnectorNode createFolderNode(JSONObject jsonObj) throws JSONException {
    ConnectorNode newNode = new ConnectorNode();
    newNode.setType(ConnectorNodeType.FOLDER);
    
    this.extractNodeName(jsonObj, newNode);
    
    String href = jsonObj.getString(JSON_HREF_PROP);
    href = href.replace(DIRECTORY_URL_SUFFIX, "");
    newNode.setName(href.substring(1, href.length()));
    
    return newNode;
  }
  
  private ConnectorNode createModelNode(JSONObject jsonObj) throws JSONException {
    ConnectorNode newNode = new ConnectorNode();
    newNode.setType(ConnectorNodeType.FILE);

    this.extractNodeName(jsonObj, newNode);
    
    String href = jsonObj.getString(JSON_HREF_PROP);
    href = href.replace(MODEL_URL_SUFFIX, "");
    newNode.setName(href.substring(1, href.length()));
    
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
      return new String(data, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RepositoryException(e.getMessage(), e);
    }
  }

  @Override
  public InputStream getContent(ConnectorNode node) {
    this.login("test@camunda.com", "testtest"); // TODO: Remove it!
    this.initializeSignavioClient();
    
    return this.signavioClient.getContent(node.getName());
  }
  
  private void releaseClientConnection(Response response) {
    if (response instanceof ClientResponse<?>) {
      ClientResponse<?> r = (ClientResponse<?>) response;
      r.releaseConnection();
    }
  }

}
