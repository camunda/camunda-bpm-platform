package org.camunda.bpm.cycle.test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import junit.framework.Assert;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.camunda.bpm.cycle.util.IoUtil;
import org.camunda.bpm.cycle.web.dto.UserDTO;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;


public abstract class AbstractCycleIT {
  
  private final static Logger log = Logger.getLogger(TestCycleRoundtripIT.class.getName());  
  
  private static final String HOST_NAME = "localhost";
  private String httpPort;
  public String CYCLE_BASE_PATH;
  
  public ApacheHttpClient4 client;
  public DefaultHttpClient defaultHttpClient;
    
  public void connectToCycleService() throws Exception {
    Properties properties = new Properties();
    
    InputStream propertiesStream = null;
    try {
      propertiesStream = TestCycleRoundtripIT.class.getResourceAsStream("testconfig.properties");
      properties.load(propertiesStream);
      httpPort = (String) properties.get("cycle.http.port");
    } finally {
      IoUtil.closeSilently(propertiesStream);
    }
    
    CYCLE_BASE_PATH = "http://" + HOST_NAME + ":"+httpPort+"/cycle/";
    log.info("Connecting to cycle at "+CYCLE_BASE_PATH);
    
    ClientConfig clientConfig = new DefaultApacheHttpClient4Config();
    clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
    client = ApacheHttpClient4.create(clientConfig);
    
    defaultHttpClient = (DefaultHttpClient) client.getClientHandler().getHttpClient();
    
    // waiting for cycle webapp to become available
    boolean success = false;
    for (int i = 0; i <= 30; i++) {
      try {
        WebResource webResource = client.resource(CYCLE_BASE_PATH);
        ClientResponse clientResponse = webResource.get(ClientResponse.class);
        int status = clientResponse.getStatus();
        clientResponse.close();
        if (status == Status.OK.getStatusCode()) {
          success = true;
          break;
        }
      } catch (Exception e) {
        // do nothing
      }
      
      Thread.sleep(2000);
    }
    
    if (!success) {      
      Assert.fail("Could not connect to cycle service at "+CYCLE_BASE_PATH+". Did cycle not deploy correctly? Check application server logs for details.");
    }
  }
  
  public void login(String username, String password) throws Exception {
    HttpPost httpPost = new HttpPost(CYCLE_BASE_PATH+"j_security_check");
    List<NameValuePair> parameterList = new ArrayList<NameValuePair>();
    parameterList.add(new BasicNameValuePair("j_username", username));
    parameterList.add(new BasicNameValuePair("j_password", password));

    httpPost.setEntity(new UrlEncodedFormEntity(parameterList, "UTF-8"));
    HttpResponse httpResponse = defaultHttpClient.execute(httpPost);
    int status = httpResponse.getStatusLine().getStatusCode();
    httpResponse.getEntity().getContent().close();
    
    Assert.assertEquals(302, status);        
  }
  
  public void createInitialUserAndLogin() throws Exception {
    // create initial user
    WebResource webResource = client.resource(CYCLE_BASE_PATH+"app/first-time-setup");
    String username = "test";
    String password = "test";

    UserDTO userDTO = new UserDTO();
    userDTO.setName(username);
    userDTO.setPassword(password);
    userDTO.setEmail("test@camunda.com");
    userDTO.setAdmin(true);
    
    ClientResponse clientResponse = webResource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).post(ClientResponse.class, userDTO);
    int status = clientResponse.getStatus();
    clientResponse.close();
    Assert.assertEquals(Status.OK.getStatusCode(), status);
    
    // login with created user
    login(username, password);
  }
  
  public void deleteAllUsers() throws Exception {
    WebResource webResource = client.resource(CYCLE_BASE_PATH+"app/secured/resource/user");
    ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
    List<Map> users = response.getEntity(List.class);
    response.close();
    for (Map userDTO : users) {
      deleteUser(String.valueOf(userDTO.get("id")));
    }    
  }

  public void deleteUser(String userId) {
    WebResource webResource = client.resource(CYCLE_BASE_PATH+"app/secured/resource/user/"+userId);
    ClientResponse clientResponse = webResource.delete(ClientResponse.class);
    clientResponse.close();
  }
  
  public void deleteAllRoundtrips() {
    WebResource webResource = client.resource(CYCLE_BASE_PATH+"app/secured/resource/roundtrip/");
    ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
    List<Map> roundtrips = response.getEntity(List.class);
    response.close();
    for (Map roundtripDTO : roundtrips) {
      deleteRoundtrip(String.valueOf(roundtripDTO.get("id")));
    }
  }

  public void deleteRoundtrip(String roundtripId) {
    WebResource webResource = client.resource(CYCLE_BASE_PATH+"app/secured/resource/roundtrip/"+roundtripId);
    ClientResponse clientResponse = webResource.delete(ClientResponse.class);
    clientResponse.close();
  }
  
  public void deleteAllConnectors() {
    WebResource webResource = client.resource(CYCLE_BASE_PATH+"app/secured/resource/connector/configuration");
    ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
    List<Map> entity = response.getEntity(List.class);
    response.close();
    for (Map<String,Object> connectorConfigurationDTO : entity) {
      deleteConnector(String.valueOf(connectorConfigurationDTO.get("connectorId")));
    }    
  }

  public void deleteConnector(String connectorId) {
    WebResource webResource = client.resource(CYCLE_BASE_PATH+"app/secured/resource/connector/configuration"+connectorId);
    ClientResponse clientResponse = webResource.delete(ClientResponse.class);
    clientResponse.close();
  }
}
