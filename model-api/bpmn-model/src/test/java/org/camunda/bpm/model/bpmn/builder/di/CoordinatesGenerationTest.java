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

  @Test
  public void shouldPlaceBoundaryEventForTask() {
    ProcessBuilder builder = Bpmn.createExecutableProcess();

    instance = builder
        .startEvent(START_EVENT_ID)
        .userTask(USER_TASK_ID)
        .boundaryEvent("boundary")
        .sequenceFlowId(SEQUENCE_FLOW_ID)
        .endEvent(END_EVENT_ID)
        .moveToActivity(USER_TASK_ID)
        .endEvent()
        .done();

    Bounds boundaryEventBounds = findBpmnShape("boundary").getBounds();
    assertShapeCoordinates(boundaryEventBounds, 218, 140);

  }

  @Test
  public void shouldPlaceFollowingFlowNodeProperlyForTask() {
    ProcessBuilder builder = Bpmn.createExecutableProcess();

    instance = builder
        .startEvent(START_EVENT_ID)
        .userTask(USER_TASK_ID)
        .boundaryEvent("boundary")
        .sequenceFlowId(SEQUENCE_FLOW_ID)
        .endEvent(END_EVENT_ID)
        .moveToActivity(USER_TASK_ID)
        .endEvent()
        .done();

    Bounds endEventBounds = findBpmnShape(END_EVENT_ID).getBounds();
    assertShapeCoordinates(endEventBounds, 304, 190);

    Collection<Waypoint> sequenceFlowWaypoints = findBpmnEdge(SEQUENCE_FLOW_ID).getWaypoints();
    Iterator<Waypoint> iterator = sequenceFlowWaypoints.iterator();

    Waypoint waypoint = iterator.next();
    assertWaypointCoordinates(waypoint, 236, 176);

    while(iterator.hasNext()){
      waypoint = iterator.next();
    }

    assertWaypointCoordinates(waypoint, 304, 208);
  }

  @Test
  public void shouldPlaceTwoBoundaryEventsForTask() {
    ProcessBuilder builder = Bpmn.createExecutableProcess();

    instance = builder
        .startEvent(START_EVENT_ID)
        .userTask(USER_TASK_ID)
        .boundaryEvent("boundary1")
        .sequenceFlowId(SEQUENCE_FLOW_ID)
        .endEvent(END_EVENT_ID)
        .moveToActivity(USER_TASK_ID)
        .endEvent()
        .moveToActivity(USER_TASK_ID)
        .boundaryEvent("boundary2")
        .done();

    Bounds boundaryEvent1Bounds = findBpmnShape("boundary1").getBounds();
    assertShapeCoordinates(boundaryEvent1Bounds, 218, 140);

    Bounds boundaryEvent2Bounds = findBpmnShape("boundary2").getBounds();
    assertShapeCoordinates(boundaryEvent2Bounds, 254, 140);

  }

  @Test
  public void shouldPlaceThreeBoundaryEventsForTask() {
    ProcessBuilder builder = Bpmn.createExecutableProcess();

    instance = builder
        .startEvent(START_EVENT_ID)
        .userTask(USER_TASK_ID)
        .boundaryEvent("boundary1")
        .sequenceFlowId(SEQUENCE_FLOW_ID)
        .endEvent(END_EVENT_ID)
        .moveToActivity(USER_TASK_ID)
        .endEvent()
        .moveToActivity(USER_TASK_ID)
        .boundaryEvent("boundary2")
        .moveToActivity(USER_TASK_ID)
        .boundaryEvent("boundary3")
        .done();

    Bounds boundaryEvent1Bounds = findBpmnShape("boundary1").getBounds();
    assertShapeCoordinates(boundaryEvent1Bounds, 218, 140);

    Bounds boundaryEvent2Bounds = findBpmnShape("boundary2").getBounds();
    assertShapeCoordinates(boundaryEvent2Bounds, 254, 140);

    Bounds boundaryEvent3Bounds = findBpmnShape("boundary3").getBounds();
    assertShapeCoordinates(boundaryEvent3Bounds, 182, 140);

  }

  @Test
  public void shouldPlaceManyBoundaryEventsForTask() {
    ProcessBuilder builder = Bpmn.createExecutableProcess();

    instance = builder
        .startEvent(START_EVENT_ID)
        .userTask(USER_TASK_ID)
        .boundaryEvent("boundary1")
        .sequenceFlowId(SEQUENCE_FLOW_ID)
        .endEvent(END_EVENT_ID)
        .moveToActivity(USER_TASK_ID)
        .endEvent()
        .moveToActivity(USER_TASK_ID)
        .boundaryEvent("boundary2")
        .moveToActivity(USER_TASK_ID)
        .boundaryEvent("boundary3")
        .moveToActivity(USER_TASK_ID)
        .boundaryEvent("boundary4")
        .done();

    Bounds boundaryEvent1Bounds = findBpmnShape("boundary1").getBounds();
    assertShapeCoordinates(boundaryEvent1Bounds, 218, 140);

    Bounds boundaryEvent2Bounds = findBpmnShape("boundary2").getBounds();
    assertShapeCoordinates(boundaryEvent2Bounds, 254, 140);

    Bounds boundaryEvent3Bounds = findBpmnShape("boundary3").getBounds();
    assertShapeCoordinates(boundaryEvent3Bounds, 182, 140);

    Bounds boundaryEvent4Bounds = findBpmnShape("boundary4").getBounds();
    assertShapeCoordinates(boundaryEvent4Bounds, 218, 140);

  }

  @Test
  public void shouldPlaceBoundaryEventForSubProcess() {
    ProcessBuilder builder = Bpmn.createExecutableProcess();

    instance = builder
        .startEvent(START_EVENT_ID)
        .subProcess(SUB_PROCESS_ID)
        .boundaryEvent("boundary")
        .sequenceFlowId(SEQUENCE_FLOW_ID)
        .endEvent(END_EVENT_ID)
        .moveToActivity(SUB_PROCESS_ID)
        .endEvent()
        .done();

    Bounds boundaryEventBounds = findBpmnShape("boundary").getBounds();
    assertShapeCoordinates(boundaryEventBounds, 343, 200);

  }

  @Test
  public void shouldPlaceFollowingFlowNodeForSubProcess() {
    ProcessBuilder builder = Bpmn.createExecutableProcess();

    instance = builder
        .startEvent(START_EVENT_ID)
        .subProcess(SUB_PROCESS_ID)
        .boundaryEvent("boundary")
        .sequenceFlowId(SEQUENCE_FLOW_ID)
        .endEvent(END_EVENT_ID)
        .moveToActivity(SUB_PROCESS_ID)
        .endEvent()
        .done();

    Bounds endEventBounds = findBpmnShape(END_EVENT_ID).getBounds();
    assertShapeCoordinates(endEventBounds, 429, 250);

    Collection<Waypoint> sequenceFlowWaypoints = findBpmnEdge(SEQUENCE_FLOW_ID).getWaypoints();
    Iterator<Waypoint> iterator = sequenceFlowWaypoints.iterator();

    Waypoint waypoint = iterator.next();
    assertWaypointCoordinates(waypoint, 361, 236);

    while(iterator.hasNext()){
      waypoint = iterator.next();
    }

    assertWaypointCoordinates(waypoint, 429, 268);
  }


  @Test
  public void shouldPlaceTwoBoundaryEventsForSubProcess() {
    ProcessBuilder builder = Bpmn.createExecutableProcess();

    instance = builder
        .startEvent(START_EVENT_ID)
        .subProcess(SUB_PROCESS_ID)
        .boundaryEvent("boundary1")
        .moveToActivity(SUB_PROCESS_ID)
        .boundaryEvent("boundary2")
        .moveToActivity(SUB_PROCESS_ID)
        .endEvent()
        .done();

    Bounds boundaryEvent1Bounds = findBpmnShape("boundary1").getBounds();
    assertShapeCoordinates(boundaryEvent1Bounds, 343, 200);

    Bounds boundaryEvent2Bounds = findBpmnShape("boundary2").getBounds();
    assertShapeCoordinates(boundaryEvent2Bounds, 379, 200);

  }

  @Test
  public void shouldPlaceThreeBoundaryEventsForSubProcess() {
    ProcessBuilder builder = Bpmn.createExecutableProcess();

    instance = builder
        .startEvent(START_EVENT_ID)
        .subProcess(SUB_PROCESS_ID)
        .boundaryEvent("boundary1")
        .moveToActivity(SUB_PROCESS_ID)
        .boundaryEvent("boundary2")
        .moveToActivity(SUB_PROCESS_ID)
        .boundaryEvent("boundary3")
        .moveToActivity(SUB_PROCESS_ID)
        .endEvent()
        .done();

    Bounds boundaryEvent1Bounds = findBpmnShape("boundary1").getBounds();
    assertShapeCoordinates(boundaryEvent1Bounds, 343, 200);

    Bounds boundaryEvent2Bounds = findBpmnShape("boundary2").getBounds();
    assertShapeCoordinates(boundaryEvent2Bounds, 379, 200);

    Bounds boundaryEvent3Bounds = findBpmnShape("boundary3").getBounds();
    assertShapeCoordinates(boundaryEvent3Bounds, 307, 200);
  }

  @Test
  public void shouldPlaceManyBoundaryEventsForSubProcess() {
    ProcessBuilder builder = Bpmn.createExecutableProcess();

    instance = builder
        .startEvent(START_EVENT_ID)
        .subProcess(SUB_PROCESS_ID)
        .boundaryEvent("boundary1")
        .moveToActivity(SUB_PROCESS_ID)
        .boundaryEvent("boundary2")
        .moveToActivity(SUB_PROCESS_ID)
        .boundaryEvent("boundary3")
        .moveToActivity(SUB_PROCESS_ID)
        .boundaryEvent("boundary4")
        .moveToActivity(SUB_PROCESS_ID)
        .endEvent()
        .done();

    Bounds boundaryEvent1Bounds = findBpmnShape("boundary1").getBounds();
    assertShapeCoordinates(boundaryEvent1Bounds, 343, 200);

    Bounds boundaryEvent2Bounds = findBpmnShape("boundary2").getBounds();
    assertShapeCoordinates(boundaryEvent2Bounds, 379, 200);

    Bounds boundaryEvent3Bounds = findBpmnShape("boundary3").getBounds();
    assertShapeCoordinates(boundaryEvent3Bounds, 307, 200);

    Bounds boundaryEvent4Bounds = findBpmnShape("boundary4").getBounds();
    assertShapeCoordinates(boundaryEvent4Bounds, 343, 200);
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
