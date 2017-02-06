package org.camunda.bpm.model.bpmn.builder.di;

import static org.camunda.bpm.model.bpmn.BpmnTestConstants.END_EVENT_ID;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.SEQUENCE_FLOW_ID;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.START_EVENT_ID;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.USER_TASK_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.ProcessBuilder;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnEdge;
import org.junit.After;
import org.junit.Test;

public class DiGeneratorForSequenceFlowsTest {

  private BpmnModelInstance instance;

  @After
  public void validateModel() throws IOException {
    if (instance != null) {
      Bpmn.validateModel(instance);
    }
  }

  @Test
  public void shouldGenerateEdgeForSequenceFlow() {

    ProcessBuilder builder = Bpmn.createExecutableProcess();

    instance = builder
                  .startEvent(START_EVENT_ID)
                  .sequenceFlowId(SEQUENCE_FLOW_ID)
                  .endEvent(END_EVENT_ID)
                  .done();

    Collection<BpmnEdge> allEdges = instance.getModelElementsByType(BpmnEdge.class);
    assertEquals(1, allEdges.size());

    assertBpmnEdgeExists(SEQUENCE_FLOW_ID);
  }

  @Test
  public void shouldGenerateEdgesForSequenceFlowsUsingGateway() {

    ProcessBuilder builder = Bpmn.createExecutableProcess();

    instance = builder
        .startEvent(START_EVENT_ID)
        .sequenceFlowId("s1")
        .parallelGateway("gateway")
        .sequenceFlowId("s2")
        .endEvent("e1")
        .moveToLastGateway()
        .sequenceFlowId("s3")
        .endEvent("e2")
        .done();

    Collection<BpmnEdge> allEdges = instance.getModelElementsByType(BpmnEdge.class);
    assertEquals(3, allEdges.size());

    assertBpmnEdgeExists("s1");
    assertBpmnEdgeExists("s2");
    assertBpmnEdgeExists("s3");
  }

  @Test
  public void shouldGenerateEdgesWhenUsingMoveToActivity() {

    ProcessBuilder builder = Bpmn.createExecutableProcess();

    instance = builder
        .startEvent(START_EVENT_ID)
        .sequenceFlowId("s1")
        .exclusiveGateway()
        .sequenceFlowId("s2")
        .userTask(USER_TASK_ID)
        .sequenceFlowId("s3")
        .endEvent("e1")
        .moveToActivity(USER_TASK_ID)
        .sequenceFlowId("s4")
        .endEvent("e2")
        .done();

    Collection<BpmnEdge> allEdges = instance.getModelElementsByType(BpmnEdge.class);
    assertEquals(4, allEdges.size());

    assertBpmnEdgeExists("s1");
    assertBpmnEdgeExists("s2");
    assertBpmnEdgeExists("s3");
    assertBpmnEdgeExists("s4");
  }

  @Test
  public void shouldGenerateEdgesWhenUsingMoveToNode() {

    ProcessBuilder builder = Bpmn.createExecutableProcess();

    instance = builder
        .startEvent(START_EVENT_ID)
        .sequenceFlowId("s1")
        .exclusiveGateway()
        .sequenceFlowId("s2")
        .userTask(USER_TASK_ID)
        .sequenceFlowId("s3")
        .endEvent("e1")
        .moveToNode(USER_TASK_ID)
        .sequenceFlowId("s4")
        .endEvent("e2")
        .done();

    Collection<BpmnEdge> allEdges = instance.getModelElementsByType(BpmnEdge.class);
    assertEquals(4, allEdges.size());

    assertBpmnEdgeExists("s1");
    assertBpmnEdgeExists("s2");
    assertBpmnEdgeExists("s3");
    assertBpmnEdgeExists("s4");
  }

  @Test
  public void shouldGenerateEdgesWhenUsingConnectTo() {

    ProcessBuilder builder = Bpmn.createExecutableProcess();

    instance = builder
        .startEvent(START_EVENT_ID)
        .sequenceFlowId("s1")
        .exclusiveGateway("gateway")
        .sequenceFlowId("s2")
        .userTask(USER_TASK_ID)
        .sequenceFlowId("s3")
        .endEvent(END_EVENT_ID)
        .moveToNode(USER_TASK_ID)
        .sequenceFlowId("s4")
        .connectTo("gateway")
        .done();

    Collection<BpmnEdge> allEdges = instance.getModelElementsByType(BpmnEdge.class);
    assertEquals(4, allEdges.size());

    assertBpmnEdgeExists("s1");
    assertBpmnEdgeExists("s2");
    assertBpmnEdgeExists("s3");
    assertBpmnEdgeExists("s4");
  }

  protected BpmnEdge findBpmnEdge(String sequenceFlowId) {
    Collection<BpmnEdge> allEdges = instance.getModelElementsByType(BpmnEdge.class);
    Iterator<BpmnEdge> iterator = allEdges.iterator();

    while (iterator.hasNext()) {
      BpmnEdge edge = iterator.next();
      if(edge.getBpmnElement().getId().equals(sequenceFlowId)) {
        return edge;
      }
    }
    return null;
  }

  protected void assertBpmnEdgeExists(String id) {
    BpmnEdge edge = findBpmnEdge(id);
    assertNotNull(edge);
  }
}
