package com.camunda.fox.cycle.web.service.resource.diagram;

import java.util.Date;

import javax.inject.Inject;

import org.junit.After;
import com.camunda.fox.cycle.connector.ConnectorNode;
import com.camunda.fox.cycle.connector.ContentInformation;
import com.camunda.fox.cycle.entity.BpmnDiagram;
import com.camunda.fox.cycle.repository.BpmnDiagramRepository;
import com.camunda.fox.cycle.web.service.resource.BpmnDiagramService;

/**
 *
 * @author nico.rehwaldt
 */
public abstract class AbstractDiagramServiceTest {

  protected static ConnectorNode DIAGRAM_NODE = new ConnectorNode("//mydiagram.bpmn", "my diagram.bpmn", 1l);

  @Inject
  protected BpmnDiagramService bpmnDiagramService;

  @Inject
  protected BpmnDiagramRepository bpmnDiagramRepository;

  @After
  public void after() {
    bpmnDiagramRepository.deleteAll();
  }

  protected BpmnDiagram diagramLastModified(Date date) {
    
    BpmnDiagram diagram = new BpmnDiagram("fox modeler", DIAGRAM_NODE);
    diagram.setLastModified(date);
    
    bpmnDiagramRepository.saveAndFlush(diagram);
    
    return diagram;
  }

  protected ContentInformation nonExistingContentInformation() {
    return new ContentInformation(false, null);
  }

  protected ContentInformation contentInformationLastModified(Date date) {
    return new ContentInformation(true, date);
  }

  protected Date now() {
    return new Date();
  }

  protected Date earlier() {
    return new Date(System.currentTimeMillis() - 20000);
  }
}
