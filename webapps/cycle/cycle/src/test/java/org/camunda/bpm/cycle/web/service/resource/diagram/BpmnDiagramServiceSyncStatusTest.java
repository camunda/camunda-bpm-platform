package org.camunda.bpm.cycle.web.service.resource.diagram;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;

import java.util.Date;

import javax.inject.Inject;

import org.camunda.bpm.cycle.connector.ConnectorCache;
import org.camunda.bpm.cycle.connector.ConnectorRegistry;
import org.camunda.bpm.cycle.connector.test.util.DummyConnector;
import org.camunda.bpm.cycle.entity.BpmnDiagram;
import org.camunda.bpm.cycle.repository.BpmnDiagramRepository;
import org.camunda.bpm.cycle.web.dto.BpmnDiagramStatusDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kubek2k.springockito.annotations.ReplaceWithMock;
import org.kubek2k.springockito.annotations.SpringockitoContextLoader;
import org.kubek2k.springockito.annotations.experimental.DirtiesMocks;
import org.kubek2k.springockito.annotations.experimental.DirtiesMocksTestContextListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;


/**
 *
 * @author nico.rehwaldt
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
  loader = SpringockitoContextLoader.class,
  locations = { "classpath:/spring/mock/test-context.xml", "classpath:/spring/mock/test-persistence.xml" }
)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, DirtiesMocksTestContextListener.class})
public class BpmnDiagramServiceSyncStatusTest extends AbstractDiagramServiceTest {

  @Inject
  @ReplaceWithMock
  private ConnectorRegistry registry;

  @Inject
  @ReplaceWithMock
  private DummyConnector connector;

  @Inject
  private BpmnDiagramRepository bpmnDiagramRepository;

  @Test
  @DirtiesMocks
  public void shouldServeIsUnavailable() {
    BpmnDiagram diagram = diagramLastModified(now());
    
    // given
    doReturn(null).when(registry).getConnector(DIAGRAM_NODE.getConnectorId());
    
    // when
    BpmnDiagramStatusDTO status = bpmnDiagramService.synchronizationStatus(diagram.getId());

    // then
    assertThat(status.getStatus()).isEqualTo(BpmnDiagram.Status.UNAVAILABLE);
  }
  
  @Test
  @DirtiesMocks
  public void shouldServeInSync() {
    Date now = now();
    
    // init diagram
    BpmnDiagram diagram = new BpmnDiagram("camunda modeler", DIAGRAM_NODE);
    diagram.setLastModified(now);
    diagram.setLastSync(now);
    bpmnDiagramRepository.saveAndFlush(diagram);

    // given
    ConnectorCache connectorCache = new ConnectorCache();
    connectorCache.put(DIAGRAM_NODE.getConnectorId(), connector);

    ConnectorRegistry connectorRegistry = new ConnectorRegistry();
    connectorRegistry.setConnectorCache(connectorCache);
    bpmnDiagramService.setConnectorRegistry(connectorRegistry);
    doReturn(contentInformationLastModified(now)).when(connector).getContentInformation(DIAGRAM_NODE);
    
    // when
    BpmnDiagramStatusDTO status = bpmnDiagramService.synchronizationStatus(diagram.getId());

    // then
    assertThat(status.getStatus()).isEqualTo(BpmnDiagram.Status.SYNCED);
  }
}
