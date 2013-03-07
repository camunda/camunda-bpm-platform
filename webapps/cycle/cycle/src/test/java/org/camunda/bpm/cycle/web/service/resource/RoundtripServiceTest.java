package org.camunda.bpm.cycle.web.service.resource;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.camunda.bpm.cycle.connector.Connector;
import org.camunda.bpm.cycle.connector.ConnectorNode;
import org.camunda.bpm.cycle.connector.ConnectorNodeType;
import org.camunda.bpm.cycle.connector.ConnectorRegistry;
import org.camunda.bpm.cycle.connector.ContentInformation;
import org.camunda.bpm.cycle.connector.test.util.ConnectorConfigurationProvider;
import org.camunda.bpm.cycle.connector.test.util.RepositoryUtil;
import org.camunda.bpm.cycle.connector.vfs.VfsConnector;
import org.camunda.bpm.cycle.entity.BpmnDiagram;
import org.camunda.bpm.cycle.entity.ConnectorConfiguration;
import org.camunda.bpm.cycle.entity.Roundtrip;
import org.camunda.bpm.cycle.entity.Roundtrip.SyncMode;
import org.camunda.bpm.cycle.repository.RoundtripRepository;
import org.camunda.bpm.cycle.util.IoUtil;
import org.camunda.bpm.cycle.web.dto.BpmnDiagramDTO;
import org.camunda.bpm.cycle.web.dto.BpmnDiagramStatusDTO;
import org.camunda.bpm.cycle.web.dto.ConnectorNodeDTO;
import org.camunda.bpm.cycle.web.dto.RoundtripDTO;
import org.camunda.bpm.cycle.web.service.resource.BpmnDiagramService;
import org.camunda.bpm.cycle.web.service.resource.RoundtripService;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 *
 * @author nico.rehwaldt
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
  locations = {"classpath:/spring/test-*.xml"}
)
public class RoundtripServiceTest {
  
  // statically cached connector
  private static Connector connector;
  
  @Inject
  private ConnectorConfigurationProvider configurationProvider;
  
  @Inject
  private ConnectorRegistry connectorRegistry;

  @Inject
  private RoundtripRepository roundtripRepository;

  @Inject
  private RoundtripService roundtripService;
  
  @Inject
  private BpmnDiagramService diagramService;

  private ConnectorNode rightNode;

  private ConnectorNode leftNode;

  @Test
  public void shouldCreateRoundtrip() throws Exception {
    // given
    RoundtripDTO data = createTestRoundtripDTOWithDetails();

    // when
    RoundtripDTO createdData = roundtripService.create(data);

    // then
    assertThat(createdData.getId()).isNotNull();
  }

  @Test
  public void shouldUpdateRoundtripDetails() throws Exception {
    // given
    RoundtripDTO roundtrip = createAndFlushRoundtrip();

    // then
    assertThat(roundtrip.getLeftHandSide()).isNotNull();
    assertThat(roundtrip.getRightHandSide()).isNotNull();
    assertThat(roundtrip.getLeftHandSide().getId()).isNotNull();
    assertThat(roundtrip.getRightHandSide().getId()).isNotNull();
  }

  @Test
  public void shouldNotServeNonExistingDiagram() throws Exception {

    // given
    RoundtripDTO roundtrip = createAndFlushRoundtrip();
    BpmnDiagramDTO leftHandSide = roundtrip.getLeftHandSide();

    try {
      // when
      diagramService.getImage(leftHandSide.getId());

      fail("expected NOT_FOUND");
    } catch (WebApplicationException e) {
      // then
      // expected
    }
  }

  @Test
  public void shouldServeExistingDiagram() throws Exception {

    // given
    RoundtripDTO roundtrip = createAndFlushRoundtrip();
    BpmnDiagramDTO rightHandSide = roundtrip.getRightHandSide();

    // when
    Response response = diagramService.getImage(rightHandSide.getId());
    
    // then
    assertThat(response.getEntity()).isInstanceOf(byte[].class);
  }

  @Test
  public void shouldServeValidDiagramStatus() throws Exception {

    // given
    RoundtripDTO roundtrip = createAndFlushRoundtrip();
    BpmnDiagramDTO leftHandSide = roundtrip.getLeftHandSide();
    
    try {
      // when
      diagramService.getImage(leftHandSide.getId());
      
      fail("expected NOT_FOUND");
    } catch (WebApplicationException e) {
      // then
      // expected
    }
  }

  @Test
  public void shouldSynchronizeRightToLeft() throws FileNotFoundException, Exception {
    RoundtripDTO testRoundtrip = createAndFlushRoundtrip();
    BpmnDiagramDTO rightHandSide = testRoundtrip.getRightHandSide();
    
    roundtripService.doSynchronize(SyncMode.RIGHT_TO_LEFT, testRoundtrip.getId(), null);
    Response imageResponse = diagramService.getImage(rightHandSide.getId());
    
    // then
    assertThatIsInSync(testRoundtrip.getRightHandSide());
    assertThatIsInSync(testRoundtrip.getLeftHandSide());
    
    assertThat(imageResponse.getStatus()).isEqualTo(200);
    assertThat(IoUtil.toString(connector.getContent(leftNode))).contains("activiti:class=\"java.lang.Object\"");
  }

  @Test
  public void shouldSynchronizeLeftToRight() throws FileNotFoundException, Exception {
    RoundtripDTO testRoundtrip = createAndFlushRoundtrip();
    BpmnDiagramDTO rightHandSide = testRoundtrip.getRightHandSide();
    
    // needs to be here because invoke of diagramService.getImage(...), expects
    // the image date to be 'now' + 5 secs. 'now' is set in roundtripService.doSynchronize(...)
    Thread.sleep(6000);
    
    roundtripService.doSynchronize(SyncMode.LEFT_TO_RIGHT, testRoundtrip.getId(), null);
    
    assertThatIsInSync(testRoundtrip.getRightHandSide());
    assertThatIsInSync(testRoundtrip.getLeftHandSide());
    
    try {
      diagramService.getImage(rightHandSide.getId());
      fail("Expected out of date diagram image");
    } catch (WebApplicationException e) {
      // then (1)
      // expected
    }
    
    // then (2)
    assertThat(IoUtil.toString(connector.getContent(rightNode))).doesNotContain("activiti:class=\"java.lang.Object\"");
  }

  @Test
  @Ignore
  public void shouldTestCreateModelLeftToRight() {
    fail("no test");
  }

  @Test
  @Ignore
  public void shouldTestCreateModelRightToLeft() {
    fail("no test");
  }

  @Test
  public void shouldDeleteRoundtrip() {
    RoundtripDTO roundtripDTO = createTestRoundtripDTO();
    roundtripDTO = roundtripService.create(roundtripDTO);
    
    Roundtrip roundtrip = roundtripRepository.findById(roundtripDTO.getId());
    Assert.assertNotNull(roundtrip);
    
    roundtripRepository.delete(roundtrip.getId());
    
    Roundtrip deletedRoundtrip = roundtripRepository.findById(roundtrip.getId());
    Assert.assertNull(deletedRoundtrip);
  }

  // Assertions //////////////////////////////////////////////////
  
  private void assertThatIsInSync(BpmnDiagramDTO diagram) {
    BpmnDiagramStatusDTO rhsAsyncSyncStatus = diagramService.synchronizationStatus(diagram.getId());
    assertThat(rhsAsyncSyncStatus.getStatus()).isEqualTo(BpmnDiagram.Status.SYNCED);
  }

  // Test data generation //////////////////////////////////////// 
  
  private RoundtripDTO createAndFlushRoundtrip() {
    Roundtrip r = roundtripRepository.saveAndFlush(new Roundtrip("Test Roundtrip"));
    RoundtripDTO data = createTestRoundtripDTOWithDetails();
    data.setId(r.getId());
    return roundtripService.updateDetails(data);
  }

  private RoundtripDTO createTestRoundtripDTO() {
    RoundtripDTO dto = new RoundtripDTO();
    dto.setLastSync(new Date());
    dto.setName("Test Roundtrip");
    return dto;
  }

  private RoundtripDTO createTestRoundtripDTOWithDetails() {
    RoundtripDTO dto = createTestRoundtripDTO();

    BpmnDiagramDTO rhs = new BpmnDiagramDTO();
    ConnectorNodeDTO rhsNode = new ConnectorNodeDTO("foo/Impl.bpmn", "Impl", connector.getId());

    rhs.setModeler("Fox designer");
    rhs.setConnectorNode(rhsNode);

    BpmnDiagramDTO lhs = new BpmnDiagramDTO();
    ConnectorNodeDTO lhsNode = new ConnectorNodeDTO("foo/Modeler.bpmn", "Modeler", connector.getId());

    lhs.setModeler("Another Modeler");
    lhs.setConnectorNode(lhsNode);

    dto.setRightHandSide(rhs);
    dto.setLeftHandSide(lhs);

    return dto;
  }

  // Test bootstraping //////////////////////////////////////// 

  private static final File VFS_DIRECTORY = new File("target/vfs-repository");

  private static Class<? extends Connector> CONNECTOR_CLS = VfsConnector.class;

  /**
   * For tests only, ensures that the connector is initialized and the 
   * connector cache contains it
   * 
   * @throws Exception 
   */
  protected void ensureConnectorInitialized() throws Exception {
    List<ConnectorConfiguration> configurations = connectorRegistry.getConnectorConfigurations(CONNECTOR_CLS);
    if (configurations.isEmpty()) {
      throw new RuntimeException("No connector configured for " + CONNECTOR_CLS);
    }

    ConnectorConfiguration config = configurations.get(0);

    // put mock connector to registry
    connectorRegistry.getCache().put(config.getId(), connector);

    // fake some connector properties
    connector.getConfiguration().setId(config.getId());
    connector.getConfiguration().setConnectorClass(config.getConnectorClass());
  }

  @Before
  public void before() throws FileNotFoundException, Exception {
    configurationProvider.ensurePersisted();
    ensureConnectorInitialized();
    
    connector.createNode("/", "foo", ConnectorNodeType.FOLDER, null);

    ConnectorNode rightNodeImg = connector.createNode("/foo", "Impl.png", ConnectorNodeType.PNG_FILE, null);
    importNode(rightNodeImg, "org/camunda/bpm/cycle/roundtrip/repository/test-rhs.png");

    rightNode = connector.createNode("/foo", "Impl.bpmn", ConnectorNodeType.ANY_FILE, null);
    importNode(rightNode, "org/camunda/bpm/cycle/roundtrip/repository/test-rhs.bpmn");

    leftNode = connector.createNode("/foo", "Modeler.bpmn", ConnectorNodeType.ANY_FILE, null);
    importNode(leftNode, "org/camunda/bpm/cycle/roundtrip/repository/test-lhs.bpmn");
  }

  @After
  public void after() {
    // Remove all entities
    roundtripRepository.deleteAll();

    connector.deleteNode(new ConnectorNode("foo/Impl.png"),null);
    connector.deleteNode(new ConnectorNode("foo/Impl.bpmn"),null);
    connector.deleteNode(new ConnectorNode("foo/Modeler.bpmn"),null);
    connector.deleteNode(new ConnectorNode("foo"),null);
  }

  @BeforeClass
  public static void beforeClass() throws Exception {
    ConnectorConfiguration config = new ConnectorConfiguration();

    String url = RepositoryUtil.createVFSRepository(VFS_DIRECTORY);

    config.getProperties().put(VfsConnector.BASE_PATH_KEY, url);

    // NOT a spring bean!
    connector = new VfsConnector();
    connector.setConfiguration(config);
    connector.init();
  }

  @AfterClass
  public static void afterClass() throws Exception {
    connector.dispose();
    RepositoryUtil.clean(VFS_DIRECTORY);
  }

  // Utility methods /////////////////////////////
  
  protected ContentInformation importNode(ConnectorNode node, String classPathEntry) throws Exception {
    return connector.updateContent(node, new FileInputStream(IoUtil.getFile(classPathEntry)),null);
  }
}
