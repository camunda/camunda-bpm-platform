package org.camunda.bpm.model.bpmn.builder.di;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.END_EVENT_ID;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.SEND_TASK_ID;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.SEQUENCE_FLOW_ID;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.SERVICE_TASK_ID;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.START_EVENT_ID;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.SUB_PROCESS_ID;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.TASK_ID;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.USER_TASK_ID;

import java.util.Collection;
import java.util.Iterator;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.ProcessBuilder;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnEdge;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnShape;
import org.camunda.bpm.model.bpmn.instance.dc.Bounds;
import org.camunda.bpm.model.bpmn.instance.di.Waypoint;
import org.junit.Test;

public class CoordinatesGenerationTest {

  private BpmnModelInstance instance;

  @Test
  public void shouldPlaceStartEvent() {

    ProcessBuilder builder = Bpmn.createExecutableProcess();

    instance = builder
        .startEvent(START_EVENT_ID)
        .done();

    Bounds startBounds = findBpmnShape(START_EVENT_ID).getBounds();
    assertShapeBounds(startBounds, 100, 100);
  }

  @Test
  public void shouldPlaceUserTask() {

    ProcessBuilder builder = Bpmn.createExecutableProcess();

    instance = builder
        .startEvent(START_EVENT_ID)
        .sequenceFlowId(SEQUENCE_FLOW_ID)
        .userTask(USER_TASK_ID)
        .done();

    Bounds userTaskBounds = findBpmnShape(USER_TASK_ID).getBounds();
    Collection<Waypoint> sequenceFlowWaypoints = findBpmnEdge(SEQUENCE_FLOW_ID).getWaypoints();

    assertShapeBounds(userTaskBounds, 186, 78);
    assertEdgeBounds(136, 118, 186, 118, sequenceFlowWaypoints);

  }

  @Test
  public void shouldPlaceSendTask() {

    ProcessBuilder builder = Bpmn.createExecutableProcess();

    instance = builder
        .startEvent(START_EVENT_ID)
        .sequenceFlowId(SEQUENCE_FLOW_ID)
        .sendTask(SEND_TASK_ID)
        .done();

    Bounds sendTaskBounds = findBpmnShape(SEND_TASK_ID).getBounds();
    Collection<Waypoint> sequenceFlowWaypoints = findBpmnEdge(SEQUENCE_FLOW_ID).getWaypoints();

    assertShapeBounds(sendTaskBounds, 186, 78);
    assertEdgeBounds(136, 118, 186, 118, sequenceFlowWaypoints);

  }

  @Test
  public void shouldPlaceServiceTask() {

    ProcessBuilder builder = Bpmn.createExecutableProcess();

    instance = builder
        .startEvent(START_EVENT_ID)
        .sequenceFlowId(SEQUENCE_FLOW_ID)
        .serviceTask(SERVICE_TASK_ID)
        .done();

    Bounds serviceTaskBounds = findBpmnShape(SERVICE_TASK_ID).getBounds();
    Collection<Waypoint> sequenceFlowWaypoints = findBpmnEdge(SEQUENCE_FLOW_ID).getWaypoints();

    assertShapeBounds(serviceTaskBounds, 186, 78);
    assertEdgeBounds(136, 118, 186, 118, sequenceFlowWaypoints);

  }

  @Test
  public void shouldPlaceReceiveTask() {

    ProcessBuilder builder = Bpmn.createExecutableProcess();

    instance = builder
        .startEvent(START_EVENT_ID)
        .sequenceFlowId(SEQUENCE_FLOW_ID)
        .receiveTask(TASK_ID)
        .done();

    Bounds receiveTaskBounds = findBpmnShape(TASK_ID).getBounds();
    Collection<Waypoint> sequenceFlowWaypoints = findBpmnEdge(SEQUENCE_FLOW_ID).getWaypoints();

    assertShapeBounds(receiveTaskBounds, 186, 78);
    assertEdgeBounds(136, 118, 186, 118, sequenceFlowWaypoints);

  }

  @Test
  public void shouldPlaceManualTask() {

    ProcessBuilder builder = Bpmn.createExecutableProcess();

    instance = builder
        .startEvent(START_EVENT_ID)
        .sequenceFlowId(SEQUENCE_FLOW_ID)
        .manualTask(TASK_ID)
        .done();

    Bounds manualTaskBounds = findBpmnShape(TASK_ID).getBounds();
    Collection<Waypoint> sequenceFlowWaypoints = findBpmnEdge(SEQUENCE_FLOW_ID).getWaypoints();

    assertShapeBounds(manualTaskBounds, 186, 78);
    assertEdgeBounds(136, 118, 186, 118, sequenceFlowWaypoints);

  }

  @Test
  public void shouldPlaceBusinessRuleTask() {

    ProcessBuilder builder = Bpmn.createExecutableProcess();

    instance = builder
        .startEvent(START_EVENT_ID)
        .sequenceFlowId(SEQUENCE_FLOW_ID)
        .businessRuleTask(TASK_ID)
        .done();

    Bounds businessRuleTaskBounds = findBpmnShape(TASK_ID).getBounds();
    Collection<Waypoint> sequenceFlowWaypoints = findBpmnEdge(SEQUENCE_FLOW_ID).getWaypoints();

    assertShapeBounds(businessRuleTaskBounds, 186, 78);
    assertEdgeBounds(136, 118, 186, 118, sequenceFlowWaypoints);

  }

  @Test
  public void shouldPlaceScriptTask() {

    ProcessBuilder builder = Bpmn.createExecutableProcess();

    instance = builder
        .startEvent(START_EVENT_ID)
        .sequenceFlowId(SEQUENCE_FLOW_ID)
        .scriptTask(TASK_ID)
        .done();

    Bounds scriptTaskBounds = findBpmnShape(TASK_ID).getBounds();
    Collection<Waypoint> sequenceFlowWaypoints = findBpmnEdge(SEQUENCE_FLOW_ID).getWaypoints();

    assertShapeBounds(scriptTaskBounds, 186, 78);
    assertEdgeBounds(136, 118, 186, 118, sequenceFlowWaypoints);

  }

  @Test
  public void shouldPlaceCatchingIntermediateEvent() {

    ProcessBuilder builder = Bpmn.createExecutableProcess();

    instance = builder
        .startEvent(START_EVENT_ID)
        .sequenceFlowId(SEQUENCE_FLOW_ID)
        .intermediateCatchEvent("id")
        .done();

    Bounds catchEventBounds = findBpmnShape("id").getBounds();
    Collection<Waypoint> sequenceFlowWaypoints = findBpmnEdge(SEQUENCE_FLOW_ID).getWaypoints();

    assertShapeBounds(catchEventBounds, 186, 100);
    assertEdgeBounds(136, 118, 186, 118, sequenceFlowWaypoints);

  }

  @Test
  public void shouldPlaceThrowingIntermediateEvent() {

    ProcessBuilder builder = Bpmn.createExecutableProcess();

    instance = builder
        .startEvent(START_EVENT_ID)
        .sequenceFlowId(SEQUENCE_FLOW_ID)
        .intermediateThrowEvent("id")
        .done();

    Bounds throwEventBounds = findBpmnShape("id").getBounds();
    Collection<Waypoint> sequenceFlowWaypoints = findBpmnEdge(SEQUENCE_FLOW_ID).getWaypoints();

    assertShapeBounds(throwEventBounds, 186, 100);
    assertEdgeBounds(136, 118, 186, 118, sequenceFlowWaypoints);

  }

  @Test
  public void shouldPlaceEndEvent() {

    ProcessBuilder builder = Bpmn.createExecutableProcess();

    instance = builder
        .startEvent(START_EVENT_ID)
        .sequenceFlowId(SEQUENCE_FLOW_ID)
        .endEvent(END_EVENT_ID)
        .done();

    Bounds endEventBounds = findBpmnShape(END_EVENT_ID).getBounds();
    Collection<Waypoint> sequenceFlowWaypoints = findBpmnEdge(SEQUENCE_FLOW_ID).getWaypoints();

    assertShapeBounds(endEventBounds, 186, 100);
    assertEdgeBounds(136, 118, 186, 118, sequenceFlowWaypoints);

  }

  @Test
  public void shouldPlaceCallActivity() {

    ProcessBuilder builder = Bpmn.createExecutableProcess();

    instance = builder
        .startEvent(START_EVENT_ID)
        .sequenceFlowId(SEQUENCE_FLOW_ID)
        .callActivity("id")
        .done();

    Bounds callActivityBounds = findBpmnShape("id").getBounds();
    Collection<Waypoint> sequenceFlowWaypoints = findBpmnEdge(SEQUENCE_FLOW_ID).getWaypoints();

    assertShapeBounds(callActivityBounds, 186, 78);
    assertEdgeBounds(136, 118, 186, 118, sequenceFlowWaypoints);

  }

  @Test
  public void shouldPlaceGateway() {

    ProcessBuilder builder = Bpmn.createExecutableProcess();

    instance = builder
        .startEvent(START_EVENT_ID)
        .sequenceFlowId(SEQUENCE_FLOW_ID)
        .exclusiveGateway("id")
        .done();

    Bounds gatewayBounds = findBpmnShape("id").getBounds();
    Collection<Waypoint> sequenceFlowWaypoints = findBpmnEdge(SEQUENCE_FLOW_ID).getWaypoints();

    assertShapeBounds(gatewayBounds, 186, 93);
    assertEdgeBounds(136, 118, 186, 118, sequenceFlowWaypoints);

  }

  @Test
  public void shouldPlaceBlankSubProcess() {

    ProcessBuilder builder = Bpmn.createExecutableProcess();

    instance = builder
        .startEvent(START_EVENT_ID)
        .sequenceFlowId(SEQUENCE_FLOW_ID)
        .subProcess(SUB_PROCESS_ID)
        .done();

    Bounds subProcessBounds = findBpmnShape(SUB_PROCESS_ID).getBounds();
    Collection<Waypoint> sequenceFlowWaypoints = findBpmnEdge(SEQUENCE_FLOW_ID).getWaypoints();

    assertShapeBounds(subProcessBounds, 186, 18);
    assertEdgeBounds(136, 118, 186, 118, sequenceFlowWaypoints);

  }

  protected BpmnShape findBpmnShape(String id) {
    Collection<BpmnShape> allShapes = instance.getModelElementsByType(BpmnShape.class);

    Iterator<BpmnShape> iterator = allShapes.iterator();
    while (iterator.hasNext()) {
      BpmnShape shape = iterator.next();
      if (shape.getBpmnElement().getId().equals(id)) {
        return shape;
      }
    }
    return null;
  }

  protected BpmnEdge findBpmnEdge(String sequenceFlowId){
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

  protected void assertShapeBounds(Bounds bounds, double x, double y){
    assertThat(bounds.getX()).isEqualTo(x);
    assertThat(bounds.getY()).isEqualTo(y);
  }

  protected void assertEdgeBounds(double x1, double y1, double x2, double y2, Collection<Waypoint> edgeWaypoints){
    Iterator<Waypoint> iterator = edgeWaypoints.iterator();
    Waypoint tmp = iterator.next();

    assertThat(x1).isEqualTo(tmp.getX());
    assertThat(y1).isEqualTo(tmp.getY());

    tmp = iterator.next();

    while(iterator.hasNext()){
      tmp = iterator.next();
    }
    assertThat(x2).isEqualTo(tmp.getX());
    assertThat(y2).isEqualTo(tmp.getY());
  }
}
