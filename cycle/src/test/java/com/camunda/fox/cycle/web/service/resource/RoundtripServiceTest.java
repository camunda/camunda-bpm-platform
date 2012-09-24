package com.camunda.fox.cycle.web.service.resource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Date;

import javax.inject.Inject;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.camunda.fox.cycle.connector.Connector;
import com.camunda.fox.cycle.connector.ConnectorNode;
import com.camunda.fox.cycle.connector.ConnectorNodeType;
import com.camunda.fox.cycle.connector.ConnectorRegistry;
import com.camunda.fox.cycle.connector.vfs.VfsConnector;
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
public class RoundtripServiceTest {

  @Inject
  private RoundtripRepository roundtripRepository;

  @Inject
  private RoundtripService roundtripService;
  
  @Inject
  private ConnectorRegistry connectorRegistry;

  private ConnectorNode rightNode;

  private ConnectorNode leftNode;

  private Connector vfsConnector;
  
  @Before
  public void before() throws FileNotFoundException, Exception {
    vfsConnector = connectorRegistry.getConnector(1);
    assertEquals(VfsConnector.class.getName(), vfsConnector.getConfiguration().getConnectorClass());
    
    vfsConnector.deleteNode("foo/foo");
    rightNode = vfsConnector.createNode("foo/foo", "Impl", ConnectorNodeType.ANY_FILE);
    vfsConnector.updateContent(rightNode, new FileInputStream(IoUtil.getFile("com/camunda/fox/cycle/roundtrip/collaboration_impl.bpmn")));
    
    vfsConnector.deleteNode("foo/bar");
    leftNode = vfsConnector.createNode("foo/bar", "Modeler", ConnectorNodeType.ANY_FILE);
    vfsConnector.updateContent(leftNode, new FileInputStream(IoUtil.getFile("com/camunda/fox/cycle/roundtrip/collaboration.bpmn")));
  }
  
  @After
  public void after() {
    // Remove all entities
    roundtripRepository.deleteAll();
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
    
    assertTrue(IoUtil.toString(vfsConnector.getContent(leftNode)).contains("activiti:class=\"java.lang.Object\""));
  }
  
  @Test
  public void shouldSynchronizeLeftToRight() throws FileNotFoundException, Exception {
    RoundtripDTO testRoundtrip = getTestRoundtrip();
    roundtripService.doSynchronize(SyncMode.LEFT_TO_RIGHT, testRoundtrip.getId());
    
    assertTrue(!IoUtil.toString(vfsConnector.getContent(rightNode)).contains("activiti:class=\"java.lang.Object\""));
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
    ConnectorNodeDTO rhsNode = new ConnectorNodeDTO("foo/foo", "Impl", 1l);

    rhs.setModeler("Fox designer");
    rhs.setConnectorNode(rhsNode);

    BpmnDiagramDTO lhs = new BpmnDiagramDTO();
    ConnectorNodeDTO lhsNode = new ConnectorNodeDTO("foo/bar", "Modeler", 1l);

    lhs.setModeler("Another Modeler");
    lhs.setConnectorNode(lhsNode);

    dto.setRightHandSide(rhs);
    dto.setLeftHandSide(lhs);

    return dto;
  }
}
