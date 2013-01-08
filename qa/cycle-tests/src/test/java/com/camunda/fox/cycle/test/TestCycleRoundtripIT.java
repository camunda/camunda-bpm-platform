package com.camunda.fox.cycle.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.camunda.fox.cycle.connector.Connector;
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
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

@RunWith(Parameterized.class)
public class TestCycleRoundtripIT extends AbstractCycleIT {
  
  private static final File TARGET_DIRECTORY = new File("target/cycle-repository");
  private static final String TMP_DIR_NAME = "cycle-integration-test";
  private static final String LHS_PROCESS_DIAGRAM = "/com/camunda/fox/cycle/roundtrip/repository/test-lhs.bpmn";
  private static final String RHS_PROCESS_DIAGRAM = "/com/camunda/fox/cycle/roundtrip/repository/test-rhs.bpmn";
  
  private RoundtripDTO roundtripDTO;
  private Long connectorId;
  
  private ConnectorConfiguration connectorConfiguration;
  
  
  public TestCycleRoundtripIT(ConnectorConfiguration connectorConfiguration, Connector connector) {
    this.connectorConfiguration = connectorConfiguration;
    this.connector = connector;
  }
  
  @Parameters
  public static List<Object[]> data() {
    ConnectorConfiguration vfsConnectorConfiguration = new ConnectorConfiguration();
    vfsConnectorConfiguration.setConnectorName("FileSystemConnector");
    vfsConnectorConfiguration.setName("FileSystemConnector");
    vfsConnectorConfiguration.setLoginMode(ConnectorLoginMode.LOGIN_NOT_REQUIRED);
    vfsConnectorConfiguration.getProperties().put(VfsConnector.BASE_PATH_KEY, TARGET_DIRECTORY.getAbsolutePath());
    vfsConnectorConfiguration.setConnectorClass(VfsConnector.class.getName());
    
    return Arrays.asList(new Object[][] {
            { vfsConnectorConfiguration, new VfsConnector() }
    });
  }
  
  @Before
  public void testCycleDeployment() throws Exception {
    init();
    createConnector();
    createRoundtripWithDetails();
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
  
  @After
  public void afterEveryTest() throws Exception {
    cleanUp();
  }
  
  // *********************************** private methods *************************************//
  private void createConnector() {
    WebResource webResource = client.resource(CYCLE_BASE_PATH+"app/secured/resource/connector/configuration");
    
    ClientResponse clientResponse = webResource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).post(ClientResponse.class, ConnectorConfigurationDTO.wrap(connectorConfiguration));
    ConnectorConfigurationDTO entity = clientResponse.getEntity(ConnectorConfigurationDTO.class);
    
    connectorId = entity.getConnectorId();
    Assert.assertNotNull(connectorId);

    int status = clientResponse.getStatus();
    clientResponse.close();
    Assert.assertEquals(Status.OK.getStatusCode(), status);
    
    // init local instance of VfsConnector 
    // (required for creating connector nodes in VFS_DIRECTORY)
    connector.setConfiguration(connectorConfiguration);
    connector.init();
    
  }
  
  private void createRoundtripWithDetails() throws Exception {
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
    
    ConnectorNode diagramNode = createConnectorNode(lhsConnectorNodeParentFolder, LHS_PROCESS_DIAGRAM);
    leftHandSide.setConnectorNode(new ConnectorNodeDTO(diagramNode));
    
    // update roundtrip details RHS
    BpmnDiagramDTO rightHandSide = new BpmnDiagramDTO();
    rightHandSide.setModeler("rhs-modeler");
    ConnectorNodeDTO rhsConnectorNodeParentFolder = createConnectorNodeParentFolder();
    
    diagramNode = createConnectorNode(rhsConnectorNodeParentFolder, RHS_PROCESS_DIAGRAM);
    rightHandSide.setConnectorNode(new ConnectorNodeDTO(diagramNode)); 
    
    roundtripDTO.setLeftHandSide(leftHandSide);
    roundtripDTO.setRightHandSide(rightHandSide);
    
    clientResponse = webResource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).post(ClientResponse.class, roundtripDTO);
    status = clientResponse.getStatus();
    clientResponse.close();
    Assert.assertEquals(Status.OK.getStatusCode(), status);
  }
  
  private ConnectorNodeDTO createConnectorNodeParentFolder() {
    ConnectorNode connectorParentNode = connector.createNode(connector.getRoot().getId(), TMP_DIR_NAME, ConnectorNodeType.FOLDER);
    connectorParentNode.setConnectorId(connectorId);
    ConnectorNodeDTO connectorParentNodeDTO = new ConnectorNodeDTO(connectorParentNode);
    return connectorParentNodeDTO;
  }
  
  private ConnectorNode createConnectorNode(ConnectorNodeDTO connectorNodeParentFolder, String processDiagramPath) throws Exception {
    InputStream modelInputStream = IoUtil.readFileAsInputStream(processDiagramPath);
    String label = processDiagramPath.substring(processDiagramPath.lastIndexOf("/") + 1, processDiagramPath.length());
    ConnectorNode connectorNode = connector.createNode(connectorNodeParentFolder.getId(), label, ConnectorNodeType.BPMN_FILE);
    connectorNode.setConnectorId(connectorId);
    connector.updateContent(connectorNode, modelInputStream);
    return connectorNode;
  }
  
  private void cleanTargetDirectory(File directory) throws IOException {
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
  
  private void cleanUp() throws Exception {
    // login with created user
    executeCycleLogin();
    deleteRoundtrip();
    deleteConnector();
    deleteUser();
    cleanTargetDirectory(TARGET_DIRECTORY);
    defaultHttpClient.getConnectionManager().shutdown();
  }
}
