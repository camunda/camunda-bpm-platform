package com.camunda.fox.cycle.web.service.resource;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.hamcrest.Matchers.*;

import java.util.Date;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kubek2k.springockito.annotations.ReplaceWithMock;
import org.kubek2k.springockito.annotations.SpringockitoContextLoader;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.camunda.fox.cycle.connector.ConnectorNode;
import com.camunda.fox.cycle.connector.ConnectorNodeType;
import com.camunda.fox.cycle.connector.ContentInformation;
import com.camunda.fox.cycle.entity.BpmnDiagram;
import com.camunda.fox.cycle.repository.BpmnDiagramRepository;

/**
 *
 * @author nico.rehwaldt
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
  loader = SpringockitoContextLoader.class,
  locations = { "classpath:/spring/mock/test-context.xml", "classpath:/spring/test-persistence.xml" }
)
public class BpmnDiagramServiceTest {

  @Inject
  @ReplaceWithMock
  private ConnectorService connectorService;
  
  @Inject
  private BpmnDiagramService bpmnDiagramService;
  
  @Inject
  private BpmnDiagramRepository bpmnDiagramRepository;
  
  @After
  public void after() {
    bpmnDiagramRepository.deleteAll();
  }

  private static ConnectorNode DIAGRAM_NODE = new ConnectorNode("//mydiagram.bpmn", "my diagram.bpmn", 1l);
  
  @Test
  public void shouldNotServeImageIfImageIsOutOfDate() {
    BpmnDiagram diagram = diagramLastModified(now());
    
    // given
    given(connectorService.getContentInfo(DIAGRAM_NODE.getConnectorId(), DIAGRAM_NODE.getId(), ConnectorNodeType.PNG_FILE)).willReturn(contentInformationLastModified(earlier()));
    given(connectorService.getTypedContent(DIAGRAM_NODE.getConnectorId(), DIAGRAM_NODE.getId(), ConnectorNodeType.PNG_FILE)).willReturn(Response.ok().build());
    
    // when
    Object result = bpmnDiagramService.getImage(diagram.getId());
    
    // then
    assertThat(result, is(instanceOf(Response.class)));
    
    Response response = (Response) result;
    assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
  }
  
  @Test
  public void shouldServeImageIfNotOutOfDate() {
    BpmnDiagram diagram = diagramLastModified(earlier());
    
    // given
    given(connectorService.getContentInfo(DIAGRAM_NODE.getConnectorId(), DIAGRAM_NODE.getId(), ConnectorNodeType.PNG_FILE)).willReturn(contentInformationLastModified(now()));
    given(connectorService.getTypedContent(DIAGRAM_NODE.getConnectorId(), DIAGRAM_NODE.getId(), ConnectorNodeType.PNG_FILE)).willReturn(Response.ok().build());
    
    // when
    Object result = bpmnDiagramService.getImage(diagram.getId());
    
    assertThat(result, is(instanceOf(Response.class)));
    
    Response response = (Response) result;
    assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
  }

  @Test
  public void shouldNotServeImageMissing() {
    BpmnDiagram diagram = diagramLastModified(earlier());
    
    // given
    given(connectorService.getContentInfo(DIAGRAM_NODE.getConnectorId(), DIAGRAM_NODE.getId(), ConnectorNodeType.PNG_FILE)).willReturn(nonExistingContentInformation());
    
    // when
    Object result = bpmnDiagramService.getImage(diagram.getId());
    
    assertThat(result, is(instanceOf(Response.class)));
    
    Response response = (Response) result;
    assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
  }
  
  private BpmnDiagram diagramLastModified(Date date) {
    
    BpmnDiagram diagram = new BpmnDiagram("fox modeler", DIAGRAM_NODE);
    diagram.setLastModified(date);
    
    bpmnDiagramRepository.saveAndFlush(diagram);
    
    return diagram;
  }

  private ContentInformation nonExistingContentInformation() {
    return new ContentInformation(false, null);
  }

  private ContentInformation contentInformationLastModified(Date date) {
    return new ContentInformation(true, date);
  }

  private Date now() {
    return new Date();
  }

  private Date earlier() {
    return new Date(System.currentTimeMillis() - 20000);
  }
}
