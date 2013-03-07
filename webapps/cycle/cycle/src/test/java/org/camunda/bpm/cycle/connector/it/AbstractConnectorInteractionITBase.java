package org.camunda.bpm.cycle.connector.it;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.camunda.bpm.cycle.connector.Connector;
import org.camunda.bpm.cycle.connector.ConnectorNode;
import org.camunda.bpm.cycle.connector.ConnectorRegistry;
import org.camunda.bpm.cycle.connector.signavio.SignavioConnector;
import org.camunda.bpm.cycle.connector.svn.SvnConnector;
import org.camunda.bpm.cycle.connector.test.util.TestHelper;
import org.camunda.bpm.cycle.connector.vfs.VfsConnector;
import org.camunda.bpm.cycle.entity.BpmnDiagram;
import org.camunda.bpm.cycle.entity.ConnectorConfiguration;
import org.camunda.bpm.cycle.entity.Roundtrip.SyncMode;
import org.camunda.bpm.cycle.repository.BpmnDiagramRepository;
import org.camunda.bpm.cycle.repository.RoundtripRepository;
import org.camunda.bpm.cycle.util.IoUtil;
import org.camunda.bpm.cycle.web.dto.BpmnDiagramDTO;
import org.camunda.bpm.cycle.web.dto.BpmnDiagramStatusDTO;
import org.camunda.bpm.cycle.web.dto.RoundtripDTO;
import org.camunda.bpm.cycle.web.dto.SynchronizationResultDTO;
import org.camunda.bpm.cycle.web.dto.SynchronizationResultDTO.SynchronizationStatus;
import org.camunda.bpm.cycle.web.service.resource.BpmnDiagramService;
import org.camunda.bpm.cycle.web.service.resource.RoundtripService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * Abstract test class for interaction tests between two {@link Connector}s.
 * Subclass it and implement the abstract methods with your {@link Connector}s.
 * If you want to use your own left/right hand side models, overwrite {@link #getLhsModelLocation()}/{@link #getRhsModelLocation()}. 
 * See {@link Signavio2SvnIT} for usage example.
 * 
 * @author christian.lipphardt@camunda.com
 */
public abstract class AbstractConnectorInteractionITBase {

  protected static Logger log = Logger.getLogger(AbstractConnectorInteractionITBase.class.getSimpleName());
  
  public final String PARENT_FOLDER = "Cycle-ConnectorInteraction-" + getClass().getSimpleName() + "-" + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
  
  @Inject
  private ConnectorRegistry connectorRegistry;
  @Inject
  protected RoundtripService roundtripService;
  @Inject
  protected RoundtripRepository roundtripRepository;
  @Inject
  protected BpmnDiagramService bpmnDiagramService;
  @Inject
  protected BpmnDiagramRepository bpmnDiagramRepository;

  protected ConnectorNode lhsConnectorNodeParent;
  protected ConnectorNode lhsConnectorNode;
  protected ConnectorNode rhsConnectorNodeParent;
  protected ConnectorNode rhsConnectorNode;
  
  protected SignavioConnector signavioConnector;
  protected SvnConnector svnConnector;
  protected VfsConnector vfsConnector;
  
  protected TestHelper testHelper;
  
  @Before
  public void init() throws Exception {
    testHelper = new TestHelper(roundtripService);
    initConnectors();
    initConnectorNodes();
  }
  
  @After
  public void cleanup() {
    try {
      getLhsConnector().deleteNode(lhsConnectorNodeParent, null);
    } catch (Exception e) {
      log.log(Level.WARNING, "Unable to free used resources (" + lhsConnectorNodeParent.getLabel() + ") from connector (" + getLhsConnector().getClass() + ").", e);
    }
    try {
      getRhsConnector().deleteNode(rhsConnectorNodeParent, null);
    } catch (Exception e) {
      log.log(Level.WARNING, "Unable to free used resources (" + rhsConnectorNodeParent.getLabel() + ") from connector (" + getRhsConnector().getClass() + ").", e);
    }
    try {
      roundtripRepository.deleteAll();
      bpmnDiagramRepository.deleteAll();
      connectorRegistry.getCache().dispose();
    } catch (Exception e) {
      log.log(Level.WARNING, "Unable to clean up repositories/connectors.", e);
    }
  }
  
//------------------------------- TESTS ------------------------------//
  
  @Test
  public void smokeConnectorsShouldBeReachable() {
    // lhs connector is reachable
    ConnectorNode lhsRootNode = getLhsConnector().getRoot();
    assertNotNull(lhsRootNode);
    assertNotNull(getLhsConnector().getChildren(lhsRootNode));
    
    // rhs connector is reachable
    ConnectorNode rhsRootNode = getRhsConnector().getRoot();
    assertNotNull(rhsRootNode);
    assertNotNull(getRhsConnector().getChildren(rhsRootNode));
  }
  
  @Test
  public void shouldSyncLeftToRight() throws IOException {
    // given
    RoundtripDTO roundtrip = testHelper.createTestRoundtripWithBothSides(lhsConnectorNode, rhsConnectorNode);
    
    // when
    SynchronizationResultDTO synchronizationResultDTO = roundtripService.doSynchronize(SyncMode.LEFT_TO_RIGHT, roundtrip.getId(), null);
    
    // then
    assertEquals(SynchronizationStatus.SYNC_SUCCESS, synchronizationResultDTO.getStatus());
    assertThatIsInSync(roundtrip.getRightHandSide());
    assertThatIsInSync(roundtrip.getLeftHandSide());
    ConnectorNode rhsConnectorNode = roundtrip.getRightHandSide().getConnectorNode().toConnectorNode();
    String rhsDiagram = IoUtil.toString(getRhsConnector().getContent(rhsConnectorNode));
    assertThat(rhsDiagram).contains("<process id=\"sid-6536fc32-7a01-41df-95d4-6979a8fd20ad\"");
    assertThat(rhsDiagram).doesNotContain("activiti:class=\"java.lang.Object\"");
    assertThat(rhsDiagram).doesNotContain("<process id=\"sid-062b5a42-bdea-463c-907e-cd675f7dfa04\"");
  }
  
  @Test
  public void shouldSyncRightToLeft() throws IOException {
    // given
    RoundtripDTO roundtrip = testHelper.createTestRoundtripWithBothSides(lhsConnectorNode, rhsConnectorNode);
    
    // when
    SynchronizationResultDTO synchronizationResultDTO = roundtripService.doSynchronize(SyncMode.RIGHT_TO_LEFT, roundtrip.getId(), null);
    
    // then
    assertEquals(SynchronizationStatus.SYNC_SUCCESS, synchronizationResultDTO.getStatus());
    assertThatIsInSync(roundtrip.getRightHandSide());
    assertThatIsInSync(roundtrip.getLeftHandSide());
    ConnectorNode lhsConnectorNode = roundtrip.getLeftHandSide().getConnectorNode().toConnectorNode();
    String lhsDiagram = IoUtil.toString(getLhsConnector().getContent(lhsConnectorNode));
    assertThat(lhsDiagram).contains("<process id=\"sid-6536fc32-7a01-41df-95d4-6979a8fd20ad\"");
    assertThat(lhsDiagram).contains("activiti:class=\"java.lang.Object\"");
    assertThat(lhsDiagram).contains("<process id=\"sid-062b5a42-bdea-463c-907e-cd675f7dfa04\"");
  }
  
  @Test
  public void shouldCreateLeftToRightWithPoolExtraction() throws IOException {
    // given
    RoundtripDTO roundtrip = testHelper.createTestRoundtripWithLHS(lhsConnectorNode);
    
    // when
    roundtrip = roundtripService.create(roundtrip.getId(),
            roundtrip.getLeftHandSide().getLabel(),
            SyncMode.LEFT_TO_RIGHT,
            "RHS",
            getRhsConnector().getId(),
            rhsConnectorNodeParent.getId(),
            null);
    
    // then
    assertThatIsInSync(roundtrip.getRightHandSide());
    assertThatIsInSync(roundtrip.getLeftHandSide());
    ConnectorNode rhsConnectorNode = roundtrip.getRightHandSide().getConnectorNode().toConnectorNode();
    String rhsDiagram = IoUtil.toString(getRhsConnector().getContent(rhsConnectorNode));
    assertThat(rhsDiagram).isNotEmpty();
    assertThat(rhsDiagram).contains("<process id=\"sid-6536fc32-7a01-41df-95d4-6979a8fd20ad\"");
  }
  
  @Test
  public void shouldCreateRightToLeftWithPoolExtraction() throws IOException {
    // given
    RoundtripDTO roundtrip = testHelper.createTestRoundtripWithRHS(rhsConnectorNode);
    
    // when
    roundtrip = roundtripService.create(roundtrip.getId(),
            roundtrip.getRightHandSide().getLabel(),
            SyncMode.RIGHT_TO_LEFT,
            "LHS",
            getLhsConnector().getId(),
            lhsConnectorNodeParent.getId(),
            null);
    
    // then
    assertThatIsInSync(roundtrip.getRightHandSide());
    assertThatIsInSync(roundtrip.getLeftHandSide());
    ConnectorNode lhsConnectorNode = roundtrip.getLeftHandSide().getConnectorNode().toConnectorNode();
    assertThat(IoUtil.toString(getLhsConnector().getContent(lhsConnectorNode)))
              .contains("activiti:class=\"java.lang.Object\"");
  }
  
//------------------------------- IMPORTANT GETTER / SETTER ------------------------------//
  
  /**
   * Returns the right hand side connector to be tested.
   */
  public abstract Connector getLhsConnector();
  
  /**
   * Returns the left hand side connector to be tested.
   */
  public abstract Connector getRhsConnector();

  /**
   * Overwrite to specify an own model for the left hand side.
   */
  public String getLhsModelLocation() {
    return "/org/camunda/bpm/cycle/roundtrip/repository/test-lhs.bpmn";
  }
  
  /**
   * Overwrite to specify an own model for the right hand side.
   */
  public String getRhsModelLocation() {
    return "/org/camunda/bpm/cycle/roundtrip/repository/test-rhs.bpmn";
  }
  
  protected SignavioConnector getSignavioConnector() {
    return signavioConnector;
  }
  
  protected SvnConnector getSvnConnector() {
    return svnConnector;
  }
  
  protected VfsConnector getVfsConnector() {
    return vfsConnector;
  }
  
  protected ConnectorNode getLhsConnectorNodeParent() {
    return lhsConnectorNodeParent;
  }
  
  protected ConnectorNode getRhsConnectorNodeParent() {
    return rhsConnectorNodeParent;
  }
  
  protected ConnectorNode getLhsConnectorNode() {
    return lhsConnectorNode;
  }
  
  protected ConnectorNode getRhsConnectorNode() {
    return rhsConnectorNode;
  }
  
//------------------------------- ASSERTIONS ------------------------------//  

  protected void assertThatIsInSync(BpmnDiagramDTO diagram) {
    BpmnDiagramStatusDTO diagramSyncStatus = bpmnDiagramService.synchronizationStatus(diagram.getId());
    assertThat(diagramSyncStatus.getStatus()).isEqualTo(BpmnDiagram.Status.SYNCED);
  }
  
//------------------------------- TEST DATA INITIALIZATION------------------------------//
  
  private void initConnectors() throws Exception {
    signavioConnector = ensureConnectorInitialized(SignavioConnector.class);
    svnConnector = ensureConnectorInitialized(SvnConnector.class);
    vfsConnector = ensureConnectorInitialized(VfsConnector.class);
  }
  
  private void initConnectorNodes() throws Exception {
    lhsConnectorNodeParent = TestHelper.createConnectorNodeParentFolder(getLhsConnector(), PARENT_FOLDER);
    rhsConnectorNodeParent = TestHelper.createConnectorNodeParentFolder(getRhsConnector(), PARENT_FOLDER);
    lhsConnectorNode = TestHelper.createConnectorNode(getLhsConnector(), lhsConnectorNodeParent, getLhsModelLocation());
    rhsConnectorNode = TestHelper.createConnectorNode(getRhsConnector(), rhsConnectorNodeParent, getRhsModelLocation());
  }
  
  @SuppressWarnings("unchecked")
  private <T> T ensureConnectorInitialized(Class<T> cls) throws Exception {
    List<ConnectorConfiguration> configurations = connectorRegistry.getConnectorConfigurations((Class< ? extends Connector>) cls);
    if (configurations.isEmpty()) {
      throw new RuntimeException("No connector configured for " + cls);
    }

    return (T) connectorRegistry.getConnector((Class< ? extends Connector>) cls);
  }
}
