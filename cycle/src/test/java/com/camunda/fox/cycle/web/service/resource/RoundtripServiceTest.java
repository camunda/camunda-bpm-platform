package com.camunda.fox.cycle.web.service.resource;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.camunda.fox.cycle.connector.Connector;
import com.camunda.fox.cycle.connector.ConnectorNode;
import com.camunda.fox.cycle.connector.ConnectorNodeType;
import com.camunda.fox.cycle.connector.ConnectorRegistry;
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
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
  locations = {"classpath:/spring/test-*.xml"}
)
public abstract class RoundtripServiceTest {

  @Inject
  private RoundtripRepository roundtripRepository;

  @Inject
  private RoundtripService roundtripService;
  
  @Inject
  private ConnectorRegistry connectorRegistry;

  private ConnectorNode rightNode;

  private ConnectorNode leftNode;

  private Connector connector;
  
  @Before
  public void before() throws FileNotFoundException, Exception {
    initConnector();
    
    connector.createNode("/", "foo", ConnectorNodeType.FOLDER);
    
    rightNode = connector.createNode("/foo", "Impl", ConnectorNodeType.ANY_FILE);
    connector.updateContent(rightNode, new FileInputStream(IoUtil.getFile("com/camunda/fox/cycle/roundtrip/collaboration_impl.bpmn")));
    
    leftNode = connector.createNode("/foo", "Modeler", ConnectorNodeType.ANY_FILE);
    connector.updateContent(leftNode, new FileInputStream(IoUtil.getFile("com/camunda/fox/cycle/roundtrip/collaboration.bpmn")));
  }
  
  @After
  public void after() {
    // Remove all entities
    roundtripRepository.deleteAll();
    
    connector.deleteNode(new ConnectorNode("foo/Impl"));
    connector.deleteNode(new ConnectorNode("foo/Modeler"));
    connector.deleteNode(new ConnectorNode("foo"));
  }

  @Test
  public void shouldCreateRoundtrip() throws Exception {
    // given
    RoundtripDTO data = createTestRoundtripDTOWithDetails();
    
    // when
    RoundtripDTO createdData = roundtripService.create(data);
   
    // then
    assertThat(createdData.getId(), notNullValue());
  }
  
  private RoundtripDTO getTestRoundtrip() {
    Roundtrip r = roundtripRepository.saveAndFlush(new Roundtrip("Test Roundtrip"));
    RoundtripDTO data = createTestRoundtripDTOWithDetails();
    data.setId(r.getId());
    return roundtripService.updateDetails(data);
  }

  @Test
  public void shouldUpdateRoundtripDetails() throws Exception {
    // given
    RoundtripDTO roundtrip = getTestRoundtrip();
    
    // then
    assertThat(roundtrip.getLeftHandSide(), is(notNullValue()));
    assertThat(roundtrip.getRightHandSide(), is(notNullValue()));
    assertThat(roundtrip.getLeftHandSide().getId(), is(notNullValue()));
    assertThat(roundtrip.getRightHandSide().getId(), is(notNullValue()));
  }
  
  @Test
  public void shouldSynchronizeRightToLeft() throws FileNotFoundException, Exception {
    RoundtripDTO testRoundtrip = getTestRoundtrip();
    roundtripService.doSynchronize(SyncMode.RIGHT_TO_LEFT, testRoundtrip.getId());
    
    assertTrue(IoUtil.toString(connector.getContent(leftNode)).contains("activiti:class=\"java.lang.Object\""));
  }
  
  @Test
  public void shouldSynchronizeLeftToRight() throws FileNotFoundException, Exception {
    RoundtripDTO testRoundtrip = getTestRoundtrip();
    roundtripService.doSynchronize(SyncMode.LEFT_TO_RIGHT, testRoundtrip.getId());
    
    assertTrue(!IoUtil.toString(connector.getContent(rightNode)).contains("activiti:class=\"java.lang.Object\""));
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

  private RoundtripDTO createTestRoundtripDTO() {
    RoundtripDTO dto = new RoundtripDTO();
    dto.setLastSync(new Date());
    dto.setName("Test Roundtrip");
    return dto;
  }

  private RoundtripDTO createTestRoundtripDTOWithDetails() {
    RoundtripDTO dto = createTestRoundtripDTO();

    BpmnDiagramDTO rhs = new BpmnDiagramDTO();
    ConnectorNodeDTO rhsNode = new ConnectorNodeDTO("foo/Impl", "Impl", connector.getId());

    rhs.setModeler("Fox designer");
    rhs.setConnectorNode(rhsNode);

    BpmnDiagramDTO lhs = new BpmnDiagramDTO();
    ConnectorNodeDTO lhsNode = new ConnectorNodeDTO("foo/Modeler", "Modeler", connector.getId());

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
  
  protected abstract void initConnector();
}
