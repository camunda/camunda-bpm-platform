package com.camunda.fox.cycle.test;

import java.io.File;
import java.io.InputStream;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import junit.framework.Assert;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

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
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;

@RunWith(Arquillian.class)
public class TestCycleRoundtrip {
  
  private static final File VFS_DIRECTORY = new File("target/vfs-repository");
  private static final String TMP_DIR_NAME = "cycle-integration-test";
  private static final String LHS_PROCESS_DIAGRAM = "/com/camunda/fox/cycle/roundtrip/repository/test-lhs.bpmn";
  private static final String RHS_PROCESS_DIAGRAM = "/com/camunda/fox/cycle/roundtrip/repository/test-rhs.bpmn";

  private static int count = 0;
  private static Client client;
  
  private static VfsConnector vfsConnector;
  private static RoundtripDTO roundtripDTO; 
  
  @BeforeClass
  public static void testCycleDeployment() throws Exception {
    ClientConfig clientConfig = new DefaultApacheHttpClient4Config();
    clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
    client = ApacheHttpClient4.create(clientConfig); 

    WebResource webResource = client.resource("http://localhost:19099/cycle/");
    ClientResponse clientResponse = webResource.get(ClientResponse.class);
    int status = clientResponse.getStatus();
    clientResponse.close();
    
    if (status == Status.NOT_FOUND.getStatusCode()) {
      if (count == 20) {
        Assert.fail("Cycle is not available. Please check if the deployment was successfully!");
      }
      Thread.sleep(2000);
      testCycleDeployment();
      count++;
    } else {
      Assert.assertEquals(Status.OK.getStatusCode(), status);
      testCreateInitialUserAndLogin();
      testCreateVfsConnector();
      testCreateRoundtripWithDetails();
    }
  }
  
  @Test
  public void testLeftToRightSynchronisation() throws Exception {
    WebResource webResource = client.resource("http://localhost:19099/cycle/app/secured/resource/roundtrip/"+roundtripDTO.getId()+"/sync?syncMode=LEFT_TO_RIGHT");
      
    ClientResponse clientResponse = webResource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).post(ClientResponse.class);
    SynchronizationStatus synchronizationStatus = clientResponse.getEntity(SynchronizationResultDTO.class).getStatus();
    Assert.assertEquals("SYNC_SUCCESS", synchronizationStatus.SYNC_SUCCESS.toString());
    int status = clientResponse.getStatus();
    clientResponse.close();
    Assert.assertEquals(Status.OK.getStatusCode(), status);
  }
  
  @Test
  public void testRightToLeftSynchronisation() throws Exception {
    WebResource webResource = client.resource("http://localhost:19099/cycle/app/secured/resource/roundtrip/"+roundtripDTO.getId()+"/sync?syncMode=RIGHT_TO_LEFT");
      
    ClientResponse clientResponse = webResource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).post(ClientResponse.class);
    SynchronizationStatus synchronizationStatus = clientResponse.getEntity(SynchronizationResultDTO.class).getStatus();
    Assert.assertEquals("SYNC_SUCCESS", synchronizationStatus.SYNC_SUCCESS.toString());
    int status = clientResponse.getStatus();
    clientResponse.close();
    Assert.assertEquals(Status.OK.getStatusCode(), status);
  }  
  
  private static void testCreateInitialUserAndLogin() {
    // create initial user
    WebResource webResource = client.resource("http://localhost:19099/cycle/app/first-time-setup");
    
    UserDTO userDTO = new UserDTO();
    userDTO.setName("test");
    userDTO.setPassword("test");
    userDTO.setEmail("test@camunda.com");
    userDTO.setAdmin(true);
    
    ClientResponse clientResponse = webResource.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, userDTO);
    int status = clientResponse.getStatus();
    clientResponse.close();
    Assert.assertEquals(Status.OK.getStatusCode(), status);
    
    // login with created user
    webResource = client.resource("http://localhost:19099/cycle/j_security_check");
    Form loginForm = new Form();
    loginForm.add("j_username", "test");
    loginForm.add("j_password", "test");
    
    clientResponse = webResource.type(MediaType.APPLICATION_FORM_URLENCODED).post(ClientResponse.class, loginForm);
    status = clientResponse.getStatus();
    clientResponse.close();
    Assert.assertEquals(302, status);
  }
  
  private static void testCreateVfsConnector() {
    WebResource webResource = client.resource("http://localhost:19099/cycle/app/secured/resource/connector/configuration");
    
    ConnectorConfiguration connectorConfiguration = new ConnectorConfiguration();
    connectorConfiguration.setConnectorName("FileSystemConnector");
    connectorConfiguration.setLoginMode(ConnectorLoginMode.LOGIN_NOT_REQUIRED);
    connectorConfiguration.getProperties().put(VfsConnector.BASE_PATH_KEY, VFS_DIRECTORY.getAbsolutePath());
    connectorConfiguration.setConnectorClass(VfsConnector.class.getSimpleName());
    
    ClientResponse clientResponse = webResource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).post(ClientResponse.class, ConnectorConfigurationDTO.wrap(connectorConfiguration));
    int status = clientResponse.getStatus();
    clientResponse.close();
    Assert.assertEquals(Status.OK.getStatusCode(), status);
    
    vfsConnector = new VfsConnector();
    vfsConnector.setConfiguration(connectorConfiguration);
    vfsConnector.init();
  }
  
  private static void testCreateRoundtripWithDetails() throws Exception {
    WebResource webResource = client.resource("http://localhost:19099/cycle/app/secured/resource/roundtrip");

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
    webResource = client.resource("http://localhost:19099/cycle/app/secured/resource/roundtrip/"+roundtripDTO.getId()+"/details");
    
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
    ConnectorNodeDTO connectorParentNodeDTO = new ConnectorNodeDTO(connectorParentNode);
    return connectorParentNodeDTO;
  }
  
  private static void createConnectorNode(ConnectorNodeDTO connectorNodeParentFolder, String processDiagramPath) throws Exception {
    InputStream modelInputStream = IoUtil.readFileAsInputStream(processDiagramPath);
    String label = processDiagramPath.substring(processDiagramPath.lastIndexOf("/") + 1, processDiagramPath.length());
    ConnectorNode connectorNode = vfsConnector.createNode(connectorNodeParentFolder.getId(), label, ConnectorNodeType.BPMN_FILE);
    vfsConnector.updateContent(connectorNode, modelInputStream);
  }
  
}
