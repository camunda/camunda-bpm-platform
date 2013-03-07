package org.camunda.bpm.cycle.repository;

import static org.fest.assertions.api.Assertions.*;

import javax.inject.Inject;

import org.camunda.bpm.cycle.entity.BpmnDiagram;
import org.camunda.bpm.cycle.entity.Roundtrip;
import org.camunda.bpm.cycle.repository.BpmnDiagramRepository;
import org.camunda.bpm.cycle.repository.RoundtripRepository;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kubek2k.springockito.annotations.SpringockitoContextLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 *
 * @author nico.rehwaldt
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
  loader = SpringockitoContextLoader.class, 
  locations = {"classpath:/spring/context.xml", "classpath:/spring/test-*.xml"}
)
public class BpmnDiagramRepositoryTest {

  @Inject
  private RoundtripRepository roundtripRepository;
  @Inject
  private BpmnDiagramRepository bpmnDiagramRepository;

  @After
  public void after() {
    // Remove all entities
    roundtripRepository.deleteAll();
    bpmnDiagramRepository.deleteAll();
  }

  @Test
  public void shouldDeleteAll() throws Exception {
  // starts an h2 server
  // Server server = Server.createWebServer(new String[] {""}).start();
    
    // given
    BpmnDiagram d1 = new BpmnDiagram("modeler1", "/", 1L);
    BpmnDiagram d2 = new BpmnDiagram("modeler2", "//", 2L);
    BpmnDiagram d3 = new BpmnDiagram("modeler3", "///", 3L);
    
    Roundtrip r1 = new Roundtrip("TestRoundtrip");
    r1.setLeftHandSide(d1);
    Roundtrip r2 = new Roundtrip("TestRoundtrip2");
    r2.setLeftHandSide(d2);
    Roundtrip r3 = new Roundtrip("TestRoundtrip3");
    r3.setLeftHandSide(d3);
    
    // when
    roundtripRepository.saveAndFlush(r1);
    roundtripRepository.saveAndFlush(r2);
    roundtripRepository.saveAndFlush(r3);
    
    assertThat(bpmnDiagramRepository.findAllModelerNames()).hasSize(3);

    roundtripRepository.delete(r1);
    roundtripRepository.delete(r2);
    roundtripRepository.delete(r3);
    
    // then
    assertThat(roundtripRepository.findAll()).isEmpty();
    assertThat(bpmnDiagramRepository.findAll()).isEmpty();
  }

}
