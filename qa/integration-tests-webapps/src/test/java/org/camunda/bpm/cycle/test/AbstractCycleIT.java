package org.camunda.bpm.cycle.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.junit.Assert;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.camunda.bpm.AbstractWebappIntegrationTest;
import org.camunda.bpm.cycle.web.dto.UserDTO;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;


public abstract class AbstractCycleIT extends AbstractWebappIntegrationTest {

  protected String getApplicationContextPath() {
    return "cycle/";
  }

  public void login(String username, String password) throws Exception {
    HttpPost httpPost = new HttpPost(APP_BASE_PATH+"j_security_check");
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
    WebResource webResource = client.resource(APP_BASE_PATH+"app/first-time-setup");
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
    WebResource webResource = client.resource(APP_BASE_PATH+"app/secured/resource/user");
    ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
    List<Map> users = response.getEntity(List.class);
    response.close();
    for (Map userDTO : users) {
      deleteUser(String.valueOf(userDTO.get("id")));
    }
  }

  public void deleteUser(String userId) {
    WebResource webResource = client.resource(APP_BASE_PATH+"app/secured/resource/user/"+userId);
    ClientResponse clientResponse = webResource.delete(ClientResponse.class);
    clientResponse.close();
  }

  public void deleteAllRoundtrips() {
    WebResource webResource = client.resource(APP_BASE_PATH+"app/secured/resource/roundtrip/");
    ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
    List<Map> roundtrips = response.getEntity(List.class);
    response.close();
    for (Map roundtripDTO : roundtrips) {
      deleteRoundtrip(String.valueOf(roundtripDTO.get("id")));
    }
  }

  public void deleteRoundtrip(String roundtripId) {
    WebResource webResource = client.resource(APP_BASE_PATH+"app/secured/resource/roundtrip/"+roundtripId);
    ClientResponse clientResponse = webResource.delete(ClientResponse.class);
    clientResponse.close();
  }

  public void deleteAllConnectors() {
    WebResource webResource = client.resource(APP_BASE_PATH+"app/secured/resource/connector/configuration");
    ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
    List<Map> entity = response.getEntity(List.class);
    response.close();
    for (Map<String,Object> connectorConfigurationDTO : entity) {
      deleteConnector(String.valueOf(connectorConfigurationDTO.get("connectorId")));
    }
  }

  public void deleteConnector(String connectorId) {
    WebResource webResource = client.resource(APP_BASE_PATH+"app/secured/resource/connector/configuration"+connectorId);
    ClientResponse clientResponse = webResource.delete(ClientResponse.class);
    clientResponse.close();
  }
}
