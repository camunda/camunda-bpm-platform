package com.camunda.fox.cycle.web.service.resource.diagram;

import static org.mockito.BDDMockito.given;


import javax.inject.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kubek2k.springockito.annotations.ReplaceWithMock;
import org.kubek2k.springockito.annotations.SpringockitoContextLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.fest.assertions.api.Assertions.*;

import com.camunda.fox.cycle.connector.ConnectorRegistry;
import com.camunda.fox.cycle.entity.BpmnDiagram;
import com.camunda.fox.cycle.repository.BpmnDiagramRepository;
import com.camunda.fox.cycle.web.dto.BpmnDiagramStatusDTO;

/**
 *
 * @author nico.rehwaldt
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
  loader = SpringockitoContextLoader.class,
  locations = { "classpath:/spring/mock/test-context.xml", "classpath:/spring/mock/test-persistence.xml" }
)
public class BpmnDiagramServiceSyncStatusTest extends AbstractDiagramServiceTest {

  @Inject
  @ReplaceWithMock
  private ConnectorRegistry registry;
  
  @Inject
  private BpmnDiagramRepository bpmnDiagramRepository;

  @Test
  public void shouldServeInSync() {
    BpmnDiagram diagram = diagramLastModified(now());
    
    // given
    given(registry.getConnector(DIAGRAM_NODE.getConnectorId())).willReturn(null);
    
    // when
    BpmnDiagramStatusDTO status = bpmnDiagramService.synchronizationStatus(diagram.getId());

    // then
    assertThat(status.getStatus()).isEqualTo(BpmnDiagram.Status.UNAVAILABLE);
  }
}
