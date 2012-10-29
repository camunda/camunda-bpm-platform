package com.camunda.fox.cycle.web.service.resource;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.camunda.fox.cycle.connector.Connector;
import com.camunda.fox.cycle.connector.ConnectorNode;
import com.camunda.fox.cycle.connector.ConnectorNodeType;
import com.camunda.fox.cycle.connector.ConnectorRegistry;
import com.camunda.fox.cycle.connector.ContentInformation;
import com.camunda.fox.cycle.entity.Roundtrip;
import com.camunda.fox.cycle.entity.Roundtrip.SyncMode;
import com.camunda.fox.cycle.repository.RoundtripRepository;
import com.camunda.fox.cycle.util.IoUtil;
import com.camunda.fox.cycle.web.dto.BpmnDiagramDTO;
import com.camunda.fox.cycle.web.dto.ConnectorNodeDTO;
import com.camunda.fox.cycle.web.dto.RoundtripDTO;

/**
 *
 * @author nico.rehwaldt
 */
public abstract class AbstractRoundtripServiceTest {

  @Inject
  private RoundtripRepository roundtripRepository;

  @Inject
  private RoundtripService roundtripService;
  
  @Inject
  private BpmnDiagramService diagramService;
  
  @Inject
  private ConnectorRegistry connectorRegistry;

  private ConnectorNode rightNode;

  private ConnectorNode leftNode;

  private Connector connector;

  @Before
  public void before() throws FileNotFoundException, Exception {
    ensureConnectorInitialized();

    connector.createNode("/", "foo", ConnectorNodeType.FOLDER);

    ConnectorNode rightNodeImg = connector.createNode("/foo", "Impl.png", ConnectorNodeType.PNG_FILE);
    importNode(rightNodeImg, "com/camunda/fox/cycle/roundtrip/repository/test-rhs.png");

    rightNode = connector.createNode("/foo", "Impl.bpmn", ConnectorNodeType.ANY_FILE);
    importNode(rightNode, "com/camunda/fox/cycle/roundtrip/repository/test-rhs.bpmn");

    leftNode = connector.createNode("/foo", "Modeler.bpmn", ConnectorNodeType.ANY_FILE);
    importNode(leftNode, "com/camunda/fox/cycle/roundtrip/repository/test-lhs.bpmn");
  }

  @After
  public void after() {
    // Remove all entities
    roundtripRepository.deleteAll();

    connector.deleteNode(new ConnectorNode("foo/Impl.png"));
    connector.deleteNode(new ConnectorNode("foo/Impl.bpmn"));
    connector.deleteNode(new ConnectorNode("foo/Modeler.bpmn"));
    connector.deleteNode(new ConnectorNode("foo"));
  }

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
    
    // when
    roundtripService.doSynchronize(SyncMode.RIGHT_TO_LEFT, testRoundtrip.getId());
    Response imageResponse = diagramService.getImage(rightHandSide.getId());
    
    // then
    assertThat(imageResponse.getStatus()).isEqualTo(200);
    assertThat(IoUtil.toString(connector.getContent(leftNode))).contains("activiti:class=\"java.lang.Object\"");

  }

  @Test
  public void shouldSynchronizeLeftToRight() throws FileNotFoundException, Exception {
    RoundtripDTO testRoundtrip = createAndFlushRoundtrip();
    BpmnDiagramDTO rightHandSide = testRoundtrip.getRightHandSide();
    
    // when
    roundtripService.doSynchronize(SyncMode.LEFT_TO_RIGHT, testRoundtrip.getId());
    
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
  public void shouldDeleteRoundtrip() {
    RoundtripDTO roundtripDTO = createTestRoundtripDTO();
    roundtripDTO = roundtripService.create(roundtripDTO);
    
    Roundtrip roundtrip = roundtripRepository.findById(roundtripDTO.getId());
    Assert.assertNotNull(roundtrip);
    
    roundtripRepository.delete(roundtrip.getId());
    
    Roundtrip deletedRoundtrip = roundtripRepository.findById(roundtrip.getId());
    Assert.assertNull(deletedRoundtrip);
  }

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
  
  protected ConnectorRegistry getConnectorRegistry() {
    return connectorRegistry;
  }

  protected Connector getConnector() {
    return connector;
  }
  
  protected void setConnector(Connector connector) {
    this.connector = connector;
  }
  
  
  // Abstract methods ////////////////////////////////////////////
  
  protected abstract void ensureConnectorInitialized() throws Exception;

  protected ContentInformation importNode(ConnectorNode node, String classPathEntry) throws Exception {
    return connector.updateContent(node, new FileInputStream(IoUtil.getFile(classPathEntry)));
  }
}
