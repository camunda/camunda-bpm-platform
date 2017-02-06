package org.camunda.bpm.model.bpmn.builder.di;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.SEQUENCE_FLOW_ID;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.START_EVENT_ID;
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
  public void shouldPlaceStartEvent(){

    ProcessBuilder builder = Bpmn.createExecutableProcess();

    instance = builder
        .startEvent(START_EVENT_ID)
        .done();

    Bounds startBounds = findBpmnShape(START_EVENT_ID).getBounds();
    assertShapeBounds(startBounds, 100, 82);
  }

  @Test
  public void shouldPlaceUserTask(){

    ProcessBuilder builder = Bpmn.createExecutableProcess();

    instance = builder
        .startEvent(START_EVENT_ID)
        .sequenceFlowId(SEQUENCE_FLOW_ID)
        .userTask(USER_TASK_ID)
        .done();

    Bounds startBounds = findBpmnShape(START_EVENT_ID).getBounds();
    Bounds userTaskBounds = findBpmnShape(USER_TASK_ID).getBounds();
    Collection<Waypoint> sequenceFlowWaypoints = findBpmnEdge(SEQUENCE_FLOW_ID).getWaypoints();

    assertShapeBounds(userTaskBounds, 186, 60);
    assertEdgeBounds(startBounds, userTaskBounds, sequenceFlowWaypoints);

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

  protected void assertEdgeBounds(Bounds sourceBounds, Bounds targetBounds, Collection<Waypoint> edgeWaypoints){
    Iterator<Waypoint> iterator = edgeWaypoints.iterator();
    Waypoint tmp = iterator.next();

    assertThat(sourceBounds.getX() + sourceBounds.getWidth()).isEqualTo(tmp.getX());
    assertThat(sourceBounds.getY() + sourceBounds.getHeight()/2).isEqualTo(tmp.getY());

    tmp = iterator.next();

    while(iterator.hasNext()){
      tmp = iterator.next();
    }
    assertThat(targetBounds.getX()).isEqualTo(tmp.getX());
    assertThat(targetBounds.getY() + targetBounds.getHeight()/2).isEqualTo(tmp.getY());
  }
}
