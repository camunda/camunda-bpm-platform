package com.camunda.fox.cycle.web.service.resource;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.Map;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kubek2k.springockito.annotations.SpringockitoContextLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.camunda.fox.cycle.api.connector.Connector;
import com.camunda.fox.cycle.api.connector.ConnectorNode;
import com.camunda.fox.cycle.api.connector.ConnectorNode.ConnectorNodeType;
import com.camunda.fox.cycle.connector.ConnectorRegistry;
import com.camunda.fox.cycle.connector.VfsConnector;
import com.camunda.fox.cycle.entity.Roundtrip;
import com.camunda.fox.cycle.repository.RoundtripRepository;
import com.camunda.fox.cycle.util.IoUtil;
import com.camunda.fox.cycle.web.dto.BpmnDiagramDTO;
import com.camunda.fox.cycle.web.dto.RoundtripDTO;

/**
 *
 * @author nico.rehwaldt
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
  loader = SpringockitoContextLoader.class, 
  locations = {"classpath:/spring/test-*.xml"}
)
public class RoundtripServiceTest {

  @Inject
  private RoundtripRepository roundtripRepository;

  @Inject
  private RoundtripService roundtripService;
  
  @Inject
  private ConnectorRegistry connectorRegistry;

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

  @Test
  public void shouldUpdateRoundtripDetails() throws Exception {
    // given
    Roundtrip r = roundtripRepository.saveAndFlush(new Roundtrip("Test Roundtrip"));
    RoundtripDTO data = createTestRoundtripDTOWithDetails();
    data.setId(r.getId());
    
    // when
    RoundtripDTO createdData = roundtripService.updateDetails(data);
    
    // then
    assertThat(createdData.getLeftHandSide(), is(notNullValue()));
    assertThat(createdData.getRightHandSide(), is(notNullValue()));
    assertThat(createdData.getLeftHandSide().getId(), is(notNullValue()));
    assertThat(createdData.getRightHandSide().getId(), is(notNullValue()));
  }
  
  @Test
  public void shouldSynchronizeLeftToRight() throws FileNotFoundException, Exception {
    Connector vfsConnector = connectorRegistry.getSessionConnectorMap().get(1l);
    assertEquals(VfsConnector.class.getName(), vfsConnector.getConfiguration().getConnectorClass());
    vfsConnector.deleteNode("foo/foo");
    ConnectorNode rightNode = vfsConnector.createNode("foo/foo", "Impl", ConnectorNodeType.FILE);
    vfsConnector.updateContent(rightNode, new FileInputStream(IoUtil.getFile("com/camunda/fox/cycle/service/roundtrip/collaboration_impl.bpmn")));
    
    ConnectorNode leftNode = vfsConnector.createNode("foo/bar", "Modeler", ConnectorNodeType.FILE);
    vfsConnector.updateContent(leftNode, new FileInputStream(IoUtil.getFile("com/camunda/fox/cycle/service/roundtrip/collaboration_impl.bpmn")));
    
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
    rhs.setDiagramPath("foo/foo");
    rhs.setLabel("Impl");
    rhs.setConnectorId(1l);
    rhs.setModeler("Fox designer");
    
    BpmnDiagramDTO lhs = new BpmnDiagramDTO();
    
    lhs.setDiagramPath("foo/bar");
    lhs.setLabel("Modeler");
    lhs.setConnectorId(1l);
    lhs.setModeler("Another Modeler");
    
    dto.setRightHandSide(rhs);
    dto.setLeftHandSide(lhs);
    
    return dto;
  }
}
