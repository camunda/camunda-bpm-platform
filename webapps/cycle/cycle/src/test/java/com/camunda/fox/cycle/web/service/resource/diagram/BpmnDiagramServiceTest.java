package com.camunda.fox.cycle.web.service.resource.diagram;

import static org.mockito.BDDMockito.given;
import static org.fest.assertions.api.Assertions.*;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.kubek2k.springockito.annotations.ReplaceWithMock;
import org.kubek2k.springockito.annotations.SpringockitoContextLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.camunda.fox.cycle.connector.ConnectorNodeType;
import com.camunda.fox.cycle.entity.BpmnDiagram;
import com.camunda.fox.cycle.web.service.resource.ConnectorService;

import static org.fest.assertions.api.Assertions.*;
import org.junit.Ignore;

/**
 *
 * @author nico.rehwaldt
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
  loader = SpringockitoContextLoader.class,
  locations = { "classpath:/spring/mock/test-context.xml", "classpath:/spring/mock/test-persistence.xml" }
)
@Ignore
public class BpmnDiagramServiceTest extends AbstractDiagramServiceTest {

  @Inject
  @ReplaceWithMock
  private ConnectorService connectorService;

  @Test
  public void shouldNotServeImageIfImageIsOutOfDate() {
    BpmnDiagram diagram = diagramLastModified(now());
    
    // given
    given(connectorService.getContentInfo(DIAGRAM_NODE.getConnectorId(), DIAGRAM_NODE.getId(), ConnectorNodeType.PNG_FILE)).willReturn(contentInformationLastModified(earlier()));
    given(connectorService.getTypedContent(DIAGRAM_NODE.getConnectorId(), DIAGRAM_NODE.getId(), ConnectorNodeType.PNG_FILE)).willReturn(Response.ok().build());
    
    try {
      // when
      bpmnDiagramService.getImage(diagram.getId());
      
      fail("Expected web application exception");
    } catch (WebApplicationException e) {
      // then
      Response response = e.getResponse();
      assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }
  }

  @Test
  public void shouldServeImageIfNotOutOfDate() {
    BpmnDiagram diagram = diagramLastModified(earlier());
    
    // given
    given(connectorService.getContentInfo(DIAGRAM_NODE.getConnectorId(), DIAGRAM_NODE.getId(), ConnectorNodeType.PNG_FILE)).willReturn(contentInformationLastModified(now()));
    given(connectorService.getTypedContent(DIAGRAM_NODE.getConnectorId(), DIAGRAM_NODE.getId(), ConnectorNodeType.PNG_FILE)).willReturn(Response.ok().build());
    
    // when
    Object result = bpmnDiagramService.getImage(diagram.getId());
    
    assertThat(result).isInstanceOf(Response.class);
    
    Response response = (Response) result;
    assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
  }

  @Test
  public void shouldNotServeImageMissing() {
    BpmnDiagram diagram = diagramLastModified(earlier());

    // given
    given(connectorService.getContentInfo(DIAGRAM_NODE.getConnectorId(), DIAGRAM_NODE.getId(), ConnectorNodeType.PNG_FILE)).willReturn(nonExistingContentInformation());

    try {
      // when
      bpmnDiagramService.getImage(diagram.getId());

      fail("Expected web application exception");
    } catch (WebApplicationException e) {
      // then
      Response response = e.getResponse();
      assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }
  }
}
