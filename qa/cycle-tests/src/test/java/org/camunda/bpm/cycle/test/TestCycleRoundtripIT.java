package org.camunda.bpm.cycle.test;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import junit.framework.Assert;

import org.camunda.bpm.cycle.connector.Connector;
import org.camunda.bpm.cycle.connector.ConnectorLoginMode;
import org.camunda.bpm.cycle.connector.ConnectorNode;
import org.camunda.bpm.cycle.connector.ConnectorNodeType;
import org.camunda.bpm.cycle.connector.signavio.SignavioConnector;
import org.camunda.bpm.cycle.connector.svn.SvnConnector;
import org.camunda.bpm.cycle.connector.vfs.VfsConnector;
import org.camunda.bpm.cycle.entity.ConnectorConfiguration;
import org.camunda.bpm.cycle.util.IoUtil;
import org.camunda.bpm.cycle.web.dto.BpmnDiagramDTO;
import org.camunda.bpm.cycle.web.dto.ConnectorConfigurationDTO;
import org.camunda.bpm.cycle.web.dto.ConnectorNodeDTO;
import org.camunda.bpm.cycle.web.dto.RoundtripDTO;
import org.camunda.bpm.cycle.web.dto.SynchronizationResultDTO;
import org.camunda.bpm.cycle.web.dto.SynchronizationResultDTO.SynchronizationStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

@RunWith(Parameterized.class)
public class TestCycleRoundtripIT extends AbstractCycleIT {
  
  private static final File TARGET_DIRECTORY = new File("target/cycle-repository");
  private static final String TMP_DIR_BASE = "cycle-integration-test";
  
  // signavio connector configuration
  private static final String SIGNAVIO_BASE_URL = "http://vm2.camunda.com:8080";
  private static final String SIGNAVIO_GLOBAL_USER = "test@camunda.com";
  private static final String SIGNAVIO_GLOBAL_PWD = "testtest";
  
  // svn connector configuration
  private static final String SVN_REPOSITORY_PATH = "https://svn.camunda.com/sandbox2";
  private static final String SVN_GLOBAL_USER = "hudson-test";
  private static final String SVN_GLOBAL_PWD = "2KamD3Lo";
  
  private static final String LHS_PROCESS_DIAGRAM = "/org/camunda/bpm/cycle/roundtrip/repository/test-lhs.bpmn";
  private static final String RHS_PROCESS_DIAGRAM = "/org/camunda/bpm/cycle/roundtrip/repository/test-rhs.bpmn";
  
  private RoundtripDTO roundtripDTO;
  private Long connectorId;
  
  private ConnectorConfiguration connectorConfiguration;
  private Connector connector;
  private ConnectorNodeDTO connectorNodeParentFolder;
  private String tmpDirName;
    
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
    
    ConnectorConfiguration signavioConnectorConfiguration = new ConnectorConfiguration();
    signavioConnectorConfiguration.setConnectorName("SignavioConnector");
    signavioConnectorConfiguration.setName("SignavioConnector");
    signavioConnectorConfiguration.setLoginMode(ConnectorLoginMode.GLOBAL);
    signavioConnectorConfiguration.setGlobalUser(SIGNAVIO_GLOBAL_USER);
    signavioConnectorConfiguration.setGlobalPassword(SIGNAVIO_GLOBAL_PWD);
    signavioConnectorConfiguration.getProperties().put(SignavioConnector.CONFIG_KEY_SIGNAVIO_BASE_URL, SIGNAVIO_BASE_URL);
    signavioConnectorConfiguration.setConnectorClass(SignavioConnector.class.getName());
    
    ConnectorConfiguration svnConnectorConfiguration = new ConnectorConfiguration();
    svnConnectorConfiguration.setConnectorName("SvnConnector");
    svnConnectorConfiguration.setName("SvnConnector");
    svnConnectorConfiguration.setLoginMode(ConnectorLoginMode.GLOBAL);
    svnConnectorConfiguration.setGlobalUser(SVN_GLOBAL_USER);
    svnConnectorConfiguration.setGlobalPassword(SVN_GLOBAL_PWD);
    svnConnectorConfiguration.getProperties().put(SvnConnector.CONFIG_KEY_REPOSITORY_PATH, SVN_REPOSITORY_PATH);
    svnConnectorConfiguration.setConnectorClass(SvnConnector.class.getName());
    
    return Arrays.asList(new Object[][] {
            { vfsConnectorConfiguration, new VfsConnector() },
            { signavioConnectorConfiguration, new SignavioConnector() },
            { svnConnectorConfiguration, new SvnConnector() }
    });
  }
  
  @Before
  public void init() throws Exception {
    initTmpDir();
    connectToCycleService();
    createInitialUserAndLogin();
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
  public void cleanup() throws Exception {    
    deleteAllRoundtrips();
    deleteAllConnectors();
    
    deleteConnectorParentFolder();
    
    if(connector != null) {
      connector.dispose();
    }
    
    deleteAllUsers();
    defaultHttpClient.getConnectionManager().shutdown();
  }
  
  // *********************************** private methods *************************************//
    
  private void createConnector() {
    WebResource webResource = client.resource(CYCLE_BASE_PATH+"app/secured/resource/connector/configuration");
    
    ConnectorConfigurationDTO connectorConfigurationDTO = ConnectorConfigurationDTO.wrap(connectorConfiguration);
    connectorConfigurationDTO.setPassword(connectorConfiguration.getGlobalPassword());
    
    ClientResponse clientResponse = webResource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).post(ClientResponse.class, connectorConfigurationDTO);
    ConnectorConfigurationDTO entity = clientResponse.getEntity(ConnectorConfigurationDTO.class);
    
    connectorId = entity.getConnectorId();
    Assert.assertNotNull(connectorId);

    int status = clientResponse.getStatus();
    clientResponse.close();
    Assert.assertEquals(Status.OK.getStatusCode(), status);
    
    // init local instance of current connector 
    // (required for creating connector nodes in TARGET_DIRECTORY)
    connector.setConfiguration(connectorConfiguration);
    connector.init();
    
    if (connector.needsLogin()) {
      connector.login(connectorConfiguration.getGlobalUser(), connectorConfiguration.getGlobalPassword());
    }
    
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
    connectorNodeParentFolder = createConnectorNodeParentFolder();
    
    ConnectorNode diagramNode = createConnectorNode(connectorNodeParentFolder, LHS_PROCESS_DIAGRAM);
    leftHandSide.setConnectorNode(new ConnectorNodeDTO(diagramNode));
    
    // update roundtrip details RHS
    BpmnDiagramDTO rightHandSide = new BpmnDiagramDTO();
    rightHandSide.setModeler("rhs-modeler");
    
    diagramNode = createConnectorNode(connectorNodeParentFolder, RHS_PROCESS_DIAGRAM);
    rightHandSide.setConnectorNode(new ConnectorNodeDTO(diagramNode)); 
    
    roundtripDTO.setLeftHandSide(leftHandSide);
    roundtripDTO.setRightHandSide(rightHandSide);
    
    clientResponse = webResource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).post(ClientResponse.class, roundtripDTO);
    status = clientResponse.getStatus();
    clientResponse.close();
    Assert.assertEquals(Status.OK.getStatusCode(), status);
  }
  
  private ConnectorNodeDTO createConnectorNodeParentFolder() {
    ConnectorNode connectorParentNode;
    if (connector instanceof SignavioConnector) {
      SignavioConnector signavioConnector = (SignavioConnector) connector;
      connectorParentNode = connector.createNode(signavioConnector.getPrivateFolder().getId(), tmpDirName, ConnectorNodeType.FOLDER, null);
    } else {
      connectorParentNode = connector.createNode(connector.getRoot().getId(), tmpDirName, ConnectorNodeType.FOLDER, null);
    }
    connectorParentNode.setConnectorId(connectorId);
    ConnectorNodeDTO connectorParentNodeDTO = new ConnectorNodeDTO(connectorParentNode);
    return connectorParentNodeDTO;
  }
  
  private ConnectorNode createConnectorNode(ConnectorNodeDTO connectorNodeParentFolder, String processDiagramPath) throws Exception {
    InputStream modelInputStream = IoUtil.readFileAsInputStream(processDiagramPath);
    String label = processDiagramPath.substring(processDiagramPath.lastIndexOf("/") + 1, processDiagramPath.length());
    ConnectorNode connectorNode = connector.createNode(connectorNodeParentFolder.getId(), label, ConnectorNodeType.BPMN_FILE, null);
    connectorNode.setConnectorId(connectorId);
    connector.updateContent(connectorNode, modelInputStream, null);
    return connectorNode;
  }
  
  private void deleteConnectorParentFolder() {
      if (connectorNodeParentFolder != null) {
        connector.deleteNode(connectorNodeParentFolder.toConnectorNode(), null);
      }
  }
  
  private void initTmpDir() {
    tmpDirName = TMP_DIR_BASE + UUID.randomUUID().toString();    
  }
  
}
