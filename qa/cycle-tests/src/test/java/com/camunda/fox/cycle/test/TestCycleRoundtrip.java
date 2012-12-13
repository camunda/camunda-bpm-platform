package com.camunda.fox.cycle.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.camunda.fox.cycle.connector.ConnectorLoginMode;
import com.camunda.fox.cycle.connector.ConnectorNode;
import com.camunda.fox.cycle.connector.ConnectorNodeType;
import com.camunda.fox.cycle.connector.vfs.VfsConnector;
import com.camunda.fox.cycle.entity.ConnectorConfiguration;
import com.camunda.fox.cycle.util.IoUtil;
import com.camunda.fox.cycle.web.dto.BpmnDiagramDTO;
import com.camunda.fox.cycle.web.dto.ConnectorConfigurationDTO;
import com.camunda.fox.cycle.web.dto.ConnectorNodeDTO;
import com.camunda.fox.cycle.web.dto.RoundtripDTO;
import com.camunda.fox.cycle.web.dto.SynchronizationResultDTO;
import com.camunda.fox.cycle.web.dto.SynchronizationResultDTO.SynchronizationStatus;
import com.camunda.fox.cycle.web.dto.UserDTO;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;

public class TestCycleRoundtrip {
  
  private static final File VFS_DIRECTORY = new File("target/vfs-repository");
  private static final String TMP_DIR_NAME = "cycle-integration-test";
  private static final String LHS_PROCESS_DIAGRAM = "/com/camunda/fox/cycle/roundtrip/repository/test-lhs.bpmn";
  private static final String RHS_PROCESS_DIAGRAM = "/com/camunda/fox/cycle/roundtrip/repository/test-rhs.bpmn";
  
  private static final String USER_ID = "1";
  
  private static final String HOST_NAME = "localhost";
  private static String httpPort = "8080";
  private static final String CYCLE_BASE_PATH = "http://" + HOST_NAME + ":"+httpPort+"/cycle/";

  private static ApacheHttpClient4 client;
  private static VfsConnector vfsConnector;
  private static RoundtripDTO roundtripDTO;
  private static DefaultHttpClient defaultHttpClient;
  private static Long vfsConnectorId;
  
  @BeforeClass
  public static void testCycleDeployment() throws Exception {
//    String profile = System.getProperty("profile");
//    if (profile != null && !profile.isEmpty()) {
//      if (profile.equals("glassfish")) {
//        httpPort = "28080";
//      } else if (profile.equals("jboss")) {
//        httpPort = "19099";
//      } else if (profile.equals("was")) {
//        // TODO
//      }
//      System.out.println("******************HTTP PORT: " + httpPort);
//    }
    
    ClientConfig clientConfig = new DefaultApacheHttpClient4Config();
    clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
    client = ApacheHttpClient4.create(clientConfig);
    
    defaultHttpClient = (DefaultHttpClient) client.getClientHandler().getHttpClient();
    
//    cleanUp();
    
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
    
    if (success) {
      createInitialUserAndLogin();
      createVfsConnector();
      createRoundtripWithDetails();
    } else {
      Assert.fail("Cycle is not available! Check cycle deployment.");
    }
  }
  
  @Test
  public void testLeftToRightSynchronisation() throws Exception {
    WebResource webResource = client.resource(CYCLE_BASE_PATH+"app/secured/resource/roundtrip/"+roundtripDTO.getId()+"/sync?syncMode=LEFT_TO_RIGHT");
      
    ClientResponse clientResponse = webResource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).post(ClientResponse.class);
    SynchronizationStatus synchronizationStatus = clientResponse.getEntity(SynchronizationResultDTO.class).getStatus();
    Assert.assertEquals(SynchronizationStatus.SYNC_SUCCESS, synchronizationStatus);
    int status = clientResponse.getStatus();
    clientResponse.close();
    Assert.assertEquals(Status.OK.getStatusCode(), status);
  }
  
  @Test
  public void testRightToLeftSynchronisation() throws Exception {
    WebResource webResource = client.resource(CYCLE_BASE_PATH+"app/secured/resource/roundtrip/"+roundtripDTO.getId()+"/sync?syncMode=RIGHT_TO_LEFT");
      
    ClientResponse clientResponse = webResource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).post(ClientResponse.class);
    SynchronizationStatus synchronizationStatus = clientResponse.getEntity(SynchronizationResultDTO.class).getStatus();
    Assert.assertEquals(SynchronizationStatus.SYNC_SUCCESS, synchronizationStatus);
    int status = clientResponse.getStatus();
    clientResponse.close();
    Assert.assertEquals(Status.OK.getStatusCode(), status);
  }
  
  @AfterClass
  public static void cleanUp() throws Exception {
    // login with created user
    executeCycleLogin();
    deleteRoundtrip();
    deleteConnector();
    deleteUser();
    cleanVfsTargetDirectory(VFS_DIRECTORY);
    defaultHttpClient.getConnectionManager().shutdown();
  }
  
  // *********************************** private methods *************************************//
  private static int executeCycleLogin() throws Exception {
    HttpPost httpPost = new HttpPost(CYCLE_BASE_PATH+"j_security_check");
    List<NameValuePair> parameterList = new ArrayList<NameValuePair>();
    parameterList.add(new BasicNameValuePair("j_username", "test"));
    parameterList.add(new BasicNameValuePair("j_password", "test"));

    httpPost.setEntity(new UrlEncodedFormEntity(parameterList, "UTF-8"));
    HttpResponse httpResponse = defaultHttpClient.execute(httpPost);
    int status = httpResponse.getStatusLine().getStatusCode();
    httpResponse.getEntity().getContent().close();
    return status;
  }
  
  private static void createInitialUserAndLogin() throws Exception {
    // create initial user
    WebResource webResource = client.resource(CYCLE_BASE_PATH+"app/first-time-setup");

    UserDTO userDTO = new UserDTO();
    userDTO.setName("test");
    userDTO.setPassword("test");
    userDTO.setEmail("test@camunda.com");
    userDTO.setAdmin(true);
    
    ClientResponse clientResponse = webResource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).post(ClientResponse.class, userDTO);
    int status = clientResponse.getStatus();
    clientResponse.close();
    Assert.assertEquals(Status.OK.getStatusCode(), status);
    
    // login with created user
    status = executeCycleLogin();
    Assert.assertEquals(302, status);
  }
  
  private static void createVfsConnector() {
    WebResource webResource = client.resource(CYCLE_BASE_PATH+"app/secured/resource/connector/configuration");
    
    ConnectorConfiguration connectorConfiguration = new ConnectorConfiguration();
    connectorConfiguration.setConnectorName("FileSystemConnector");
    connectorConfiguration.setName("FileSystemConnector");
    connectorConfiguration.setLoginMode(ConnectorLoginMode.LOGIN_NOT_REQUIRED);
    connectorConfiguration.getProperties().put(VfsConnector.BASE_PATH_KEY, VFS_DIRECTORY.getAbsolutePath());
    connectorConfiguration.setConnectorClass(VfsConnector.class.getName());
    
    ClientResponse clientResponse = webResource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).post(ClientResponse.class, ConnectorConfigurationDTO.wrap(connectorConfiguration));
    ConnectorConfigurationDTO entity = clientResponse.getEntity(ConnectorConfigurationDTO.class);
    
    vfsConnectorId = entity.getConnectorId();
    Assert.assertNotNull(vfsConnectorId);

    int status = clientResponse.getStatus();
    clientResponse.close();
    Assert.assertEquals(Status.OK.getStatusCode(), status);
    
    // init local instance of VfsConnector 
    // (required for creating connector nodes in VFS_DIRECTORY)
    vfsConnector = new VfsConnector();
    vfsConnector.setConfiguration(connectorConfiguration);
    vfsConnector.init();
    
  }
  
  private static void createRoundtripWithDetails() throws Exception {
    WebResource webResource = client.resource(CYCLE_BASE_PATH+"app/secured/resource/roundtrip");

    // create roundtrip
    roundtripDTO = new RoundtripDTO();
    roundtripDTO.setName("test");
    
    ClientResponse clientResponse = webResource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).post(ClientResponse.class, roundtripDTO);
    roundtripDTO = clientResponse.getEntity(RoundtripDTO.class);
    Assert.assertNotNull(roundtripDTO.getId());
    int status = clientResponse.getStatus();
    clientResponse.close();
    Assert.assertEquals(Status.OK.getStatusCode(), status);

    // update roundtrip details LHS
    webResource = client.resource(CYCLE_BASE_PATH+"app/secured/resource/roundtrip/"+roundtripDTO.getId()+"/details");
    
    BpmnDiagramDTO leftHandSide = new BpmnDiagramDTO();
    leftHandSide.setModeler("lhs-modeler");
    ConnectorNodeDTO lhsConnectorNodeParentFolder = createConnectorNodeParentFolder();
    leftHandSide.setConnectorNode(lhsConnectorNodeParentFolder);
    
    createConnectorNode(lhsConnectorNodeParentFolder, LHS_PROCESS_DIAGRAM);
    
    // update roundtrip details RHS
    BpmnDiagramDTO rightHandSide = new BpmnDiagramDTO();
    rightHandSide.setModeler("rhs-modeler");
    ConnectorNodeDTO rhsConnectorNodeParentFolder = createConnectorNodeParentFolder();
    rightHandSide.setConnectorNode(rhsConnectorNodeParentFolder);
    
    createConnectorNode(rhsConnectorNodeParentFolder, RHS_PROCESS_DIAGRAM);
    
    roundtripDTO.setLeftHandSide(leftHandSide);
    roundtripDTO.setRightHandSide(rightHandSide);
    
    clientResponse = webResource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).post(ClientResponse.class, roundtripDTO);
    status = clientResponse.getStatus();
    clientResponse.close();
    Assert.assertEquals(Status.OK.getStatusCode(), status);
  }
  
  private static ConnectorNodeDTO createConnectorNodeParentFolder() {
    ConnectorNode connectorParentNode = vfsConnector.createNode(vfsConnector.getRoot().getId(), TMP_DIR_NAME, ConnectorNodeType.FOLDER);
    connectorParentNode.setConnectorId(vfsConnectorId);
    ConnectorNodeDTO connectorParentNodeDTO = new ConnectorNodeDTO(connectorParentNode);
    return connectorParentNodeDTO;
  }
  
  private static void createConnectorNode(ConnectorNodeDTO connectorNodeParentFolder, String processDiagramPath) throws Exception {
    InputStream modelInputStream = IoUtil.readFileAsInputStream(processDiagramPath);
    String label = processDiagramPath.substring(processDiagramPath.lastIndexOf("/") + 1, processDiagramPath.length());
    ConnectorNode connectorNode = vfsConnector.createNode(connectorNodeParentFolder.getId(), label, ConnectorNodeType.BPMN_FILE);
    connectorNode.setConnectorId(vfsConnectorId);
    vfsConnector.updateContent(connectorNode, modelInputStream);
  }
  
  private static void cleanVfsTargetDirectory(File directory) throws IOException {
    if (directory.exists()) {
      if (directory.isDirectory()) {
        FileUtils.deleteDirectory(directory);
      } else {
        throw new IllegalArgumentException("Not a directory: " + directory);
      }
    }
    if (!directory.mkdirs()) {
      throw new IllegalArgumentException("Could not clean: " + directory);
    }
  }
  
  private static void deleteUser() throws Exception {
    WebResource webResource = client.resource(CYCLE_BASE_PATH+"app/secured/resource/user");
    ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
    List<Map> users = response.getEntity(List.class);
    response.close();
    for (Map userDTO : users) {
      webResource = client.resource(CYCLE_BASE_PATH+"app/secured/resource/user"+userDTO.get("id"));
      ClientResponse clientResponse = webResource.delete(ClientResponse.class);
      clientResponse.close();
    }
    
    ClientResponse clientResponse = webResource.delete(ClientResponse.class);
    clientResponse.close();
  }
  
  private static void deleteRoundtrip() {
    WebResource webResource = client.resource(CYCLE_BASE_PATH+"app/secured/resource/roundtrip");
    ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
    List<Map> roundtrips = response.getEntity(List.class);
    response.close();
    for (Map roundtripDTO : roundtrips) {
      webResource = client.resource(CYCLE_BASE_PATH+"app/secured/resource/roundtrip/"+roundtripDTO.get("id"));
      ClientResponse clientResponse = webResource.delete(ClientResponse.class);
      clientResponse.close();
    }
    
    
  }
  
  private static void deleteConnector() {
    
    WebResource webResource = client.resource(CYCLE_BASE_PATH+"app/secured/resource/connector/configuration");
    ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
    List<Map> entity = response.getEntity(List.class);
    response.close();
    for (Map<String,Object> connectorConfigurationDTO : entity) {
      webResource = client.resource(CYCLE_BASE_PATH+"app/secured/resource/connector/configuration"+connectorConfigurationDTO.get("connectorId"));
      ClientResponse clientResponse = webResource.delete(ClientResponse.class);
      clientResponse.close();
    }
    
    if(vfsConnector != null) {
      vfsConnector.dispose();
    }
  }
}
