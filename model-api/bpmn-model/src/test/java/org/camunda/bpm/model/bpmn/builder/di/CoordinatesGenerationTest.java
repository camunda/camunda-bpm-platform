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
    assertShapeCoordinates(startBounds, 100, 100);
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
    assertShapeCoordinates(userTaskBounds, 186, 78);

    Collection<Waypoint> sequenceFlowWaypoints = findBpmnEdge(SEQUENCE_FLOW_ID).getWaypoints();
    Iterator<Waypoint> iterator = sequenceFlowWaypoints.iterator();

    Waypoint waypoint = iterator.next();
    assertWaypointCoordinates(waypoint, 136, 118);

    while(iterator.hasNext()){
      waypoint = iterator.next();
    }

    assertWaypointCoordinates(waypoint, 186, 118);

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
    assertShapeCoordinates(sendTaskBounds, 186, 78);

    Collection<Waypoint> sequenceFlowWaypoints = findBpmnEdge(SEQUENCE_FLOW_ID).getWaypoints();
    Iterator<Waypoint> iterator = sequenceFlowWaypoints.iterator();

    Waypoint waypoint = iterator.next();
    assertWaypointCoordinates(waypoint, 136, 118);

    while(iterator.hasNext()){
      waypoint = iterator.next();
    }

    assertWaypointCoordinates(waypoint, 186, 118);

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
    assertShapeCoordinates(serviceTaskBounds, 186, 78);

    Collection<Waypoint> sequenceFlowWaypoints = findBpmnEdge(SEQUENCE_FLOW_ID).getWaypoints();
    Iterator<Waypoint> iterator = sequenceFlowWaypoints.iterator();

    Waypoint waypoint = iterator.next();
    assertWaypointCoordinates(waypoint, 136, 118);

    while(iterator.hasNext()){
      waypoint = iterator.next();
    }

    assertWaypointCoordinates(waypoint, 186, 118);

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
    assertShapeCoordinates(receiveTaskBounds, 186, 78);

    Collection<Waypoint> sequenceFlowWaypoints = findBpmnEdge(SEQUENCE_FLOW_ID).getWaypoints();
    Iterator<Waypoint> iterator = sequenceFlowWaypoints.iterator();

    Waypoint waypoint = iterator.next();
    assertWaypointCoordinates(waypoint, 136, 118);

    while(iterator.hasNext()){
      waypoint = iterator.next();
    }

    assertWaypointCoordinates(waypoint, 186, 118);

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
    assertShapeCoordinates(manualTaskBounds, 186, 78);

    Collection<Waypoint> sequenceFlowWaypoints = findBpmnEdge(SEQUENCE_FLOW_ID).getWaypoints();
    Iterator<Waypoint> iterator = sequenceFlowWaypoints.iterator();

    Waypoint waypoint = iterator.next();
    assertWaypointCoordinates(waypoint, 136, 118);

    while(iterator.hasNext()){
      waypoint = iterator.next();
    }

    assertWaypointCoordinates(waypoint, 186, 118);

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
    assertShapeCoordinates(businessRuleTaskBounds, 186, 78);

    Collection<Waypoint> sequenceFlowWaypoints = findBpmnEdge(SEQUENCE_FLOW_ID).getWaypoints();
    Iterator<Waypoint> iterator = sequenceFlowWaypoints.iterator();

    Waypoint waypoint = iterator.next();
    assertWaypointCoordinates(waypoint, 136, 118);

    while(iterator.hasNext()){
      waypoint = iterator.next();
    }

    assertWaypointCoordinates(waypoint, 186, 118);

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
    assertShapeCoordinates(scriptTaskBounds, 186, 78);

    Collection<Waypoint> sequenceFlowWaypoints = findBpmnEdge(SEQUENCE_FLOW_ID).getWaypoints();
    Iterator<Waypoint> iterator = sequenceFlowWaypoints.iterator();

    Waypoint waypoint = iterator.next();
    assertWaypointCoordinates(waypoint, 136, 118);

    while(iterator.hasNext()){
      waypoint = iterator.next();
    }

    assertWaypointCoordinates(waypoint, 186, 118);

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
    assertShapeCoordinates(catchEventBounds, 186, 100);

    Collection<Waypoint> sequenceFlowWaypoints = findBpmnEdge(SEQUENCE_FLOW_ID).getWaypoints();
    Iterator<Waypoint> iterator = sequenceFlowWaypoints.iterator();

    Waypoint waypoint = iterator.next();
    assertWaypointCoordinates(waypoint, 136, 118);

    while(iterator.hasNext()){
      waypoint = iterator.next();
    }

    assertWaypointCoordinates(waypoint, 186, 118);

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
    assertShapeCoordinates(throwEventBounds, 186, 100);

    Collection<Waypoint> sequenceFlowWaypoints = findBpmnEdge(SEQUENCE_FLOW_ID).getWaypoints();
    Iterator<Waypoint> iterator = sequenceFlowWaypoints.iterator();

    Waypoint waypoint = iterator.next();
    assertWaypointCoordinates(waypoint, 136, 118);

    while(iterator.hasNext()){
      waypoint = iterator.next();
    }

    assertWaypointCoordinates(waypoint, 186, 118);

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
    assertShapeCoordinates(endEventBounds, 186, 100);

    Collection<Waypoint> sequenceFlowWaypoints = findBpmnEdge(SEQUENCE_FLOW_ID).getWaypoints();
    Iterator<Waypoint> iterator = sequenceFlowWaypoints.iterator();

    Waypoint waypoint = iterator.next();
    assertWaypointCoordinates(waypoint, 136, 118);

    while(iterator.hasNext()){
      waypoint = iterator.next();
    }

    assertWaypointCoordinates(waypoint, 186, 118);

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
    assertShapeCoordinates(callActivityBounds, 186, 78);

    Collection<Waypoint> sequenceFlowWaypoints = findBpmnEdge(SEQUENCE_FLOW_ID).getWaypoints();
    Iterator<Waypoint> iterator = sequenceFlowWaypoints.iterator();

    Waypoint waypoint = iterator.next();
    assertWaypointCoordinates(waypoint, 136, 118);

    while(iterator.hasNext()){
      waypoint = iterator.next();
    }

    assertWaypointCoordinates(waypoint, 186, 118);

  }

  @Test
  public void shouldPlaceExclusiveGateway() {

    ProcessBuilder builder = Bpmn.createExecutableProcess();

    instance = builder
        .startEvent(START_EVENT_ID)
        .sequenceFlowId(SEQUENCE_FLOW_ID)
        .exclusiveGateway("id")
        .done();

    Bounds gatewayBounds = findBpmnShape("id").getBounds();
    assertShapeCoordinates(gatewayBounds, 186, 93);

    Collection<Waypoint> sequenceFlowWaypoints = findBpmnEdge(SEQUENCE_FLOW_ID).getWaypoints();
    Iterator<Waypoint> iterator = sequenceFlowWaypoints.iterator();

    Waypoint waypoint = iterator.next();
    assertWaypointCoordinates(waypoint, 136, 118);

    while(iterator.hasNext()){
      waypoint = iterator.next();
    }

    assertWaypointCoordinates(waypoint, 186, 118);

  }

  @Test
  public void shouldPlaceInclusiveGateway() {

    ProcessBuilder builder = Bpmn.createExecutableProcess();

    instance = builder
        .startEvent(START_EVENT_ID)
        .sequenceFlowId(SEQUENCE_FLOW_ID)
        .inclusiveGateway("id")
        .done();

    Bounds gatewayBounds = findBpmnShape("id").getBounds();
    assertShapeCoordinates(gatewayBounds, 186, 93);

    Collection<Waypoint> sequenceFlowWaypoints = findBpmnEdge(SEQUENCE_FLOW_ID).getWaypoints();
    Iterator<Waypoint> iterator = sequenceFlowWaypoints.iterator();

    Waypoint waypoint = iterator.next();
    assertWaypointCoordinates(waypoint, 136, 118);

    while(iterator.hasNext()){
      waypoint = iterator.next();
    }

    assertWaypointCoordinates(waypoint, 186, 118);

  }

  @Test
  public void shouldPlaceParallelGateway() {

    ProcessBuilder builder = Bpmn.createExecutableProcess();

    instance = builder
        .startEvent(START_EVENT_ID)
        .sequenceFlowId(SEQUENCE_FLOW_ID)
        .parallelGateway("id")
        .done();

    Bounds gatewayBounds = findBpmnShape("id").getBounds();
    assertShapeCoordinates(gatewayBounds, 186, 93);

    Collection<Waypoint> sequenceFlowWaypoints = findBpmnEdge(SEQUENCE_FLOW_ID).getWaypoints();
    Iterator<Waypoint> iterator = sequenceFlowWaypoints.iterator();

    Waypoint waypoint = iterator.next();
    assertWaypointCoordinates(waypoint, 136, 118);

    while(iterator.hasNext()){
      waypoint = iterator.next();
    }

    assertWaypointCoordinates(waypoint, 186, 118);

  }

  @Test
  public void shouldPlaceEventBasedGateway() {

    ProcessBuilder builder = Bpmn.createExecutableProcess();

    instance = builder
        .startEvent(START_EVENT_ID)
        .sequenceFlowId(SEQUENCE_FLOW_ID)
        .eventBasedGateway()
          .id("id")
        .done();

    Bounds gatewayBounds = findBpmnShape("id").getBounds();
    assertShapeCoordinates(gatewayBounds, 186, 93);

    Collection<Waypoint> sequenceFlowWaypoints = findBpmnEdge(SEQUENCE_FLOW_ID).getWaypoints();
    Iterator<Waypoint> iterator = sequenceFlowWaypoints.iterator();

    Waypoint waypoint = iterator.next();
    assertWaypointCoordinates(waypoint, 136, 118);

    while(iterator.hasNext()){
      waypoint = iterator.next();
    }

    assertWaypointCoordinates(waypoint, 186, 118);

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
    assertShapeCoordinates(subProcessBounds, 186, 18);

    Collection<Waypoint> sequenceFlowWaypoints = findBpmnEdge(SEQUENCE_FLOW_ID).getWaypoints();
    Iterator<Waypoint> iterator = sequenceFlowWaypoints.iterator();

    Waypoint waypoint = iterator.next();
    assertWaypointCoordinates(waypoint, 136, 118);

    while(iterator.hasNext()){
      waypoint = iterator.next();
    }

    assertWaypointCoordinates(waypoint, 186, 118);

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

  protected void assertShapeCoordinates(Bounds bounds, double x, double y){
    assertThat(bounds.getX()).isEqualTo(x);
    assertThat(bounds.getY()).isEqualTo(y);
  }


  protected void assertWaypointCoordinates(Waypoint waypoint, double x, double y){
    assertThat(x).isEqualTo(waypoint.getX());
    assertThat(y).isEqualTo(waypoint.getY());
  }
}
