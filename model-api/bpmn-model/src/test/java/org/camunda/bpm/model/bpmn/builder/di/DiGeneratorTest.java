package org.camunda.bpm.model.bpmn.builder.di;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.BOUNDARY_ID;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.CATCH_ID;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.CONDITION_ID;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.END_EVENT_ID;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.SEND_TASK_ID;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.SERVICE_TASK_ID;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.START_EVENT_ID;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.SUB_PROCESS_ID;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.TASK_ID;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.TEST_CONDITION;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.USER_TASK_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.ProcessBuilder;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnDiagram;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnShape;
import org.junit.After;
import org.junit.Test;

public class DiGeneratorTest {

  private BpmnModelInstance instance;

  @After
  public void validateModel() throws IOException {
    if (instance != null) {
      Bpmn.validateModel(instance);
    }
  }

  @Test
  public void shouldGeneratePlaneForProcess() {

    // when
    instance = Bpmn.createExecutableProcess().done();

    // then
    // BPMNDiagram exists
    Collection<BpmnDiagram> bpmnDiagrams = instance.getModelElementsByType(BpmnDiagram.class);
    assertEquals(1, bpmnDiagrams.size());

    BpmnDiagram diagram = bpmnDiagrams.iterator().next();
    assertNotNull(diagram.getId());

    assertNotNull(diagram.getBpmnPlane());

  }

  @Test
  public void shouldGenerateShapeForStartEvent() {

    // given
    ProcessBuilder processBuilder = Bpmn.createExecutableProcess();

    // when
    instance = processBuilder
                  .startEvent(START_EVENT_ID)
                  .endEvent(END_EVENT_ID)
                  .done();

    // then
    Collection<BpmnShape> allShapes = instance.getModelElementsByType(BpmnShape.class);
    assertEquals(2, allShapes.size());

    BpmnShape bpmnShapeStart = findBpmnShape(START_EVENT_ID);
    assertNotNull(bpmnShapeStart);
    assertEventSize(bpmnShapeStart);
  }

  @Test
  public void shouldGenerateShapeForUserTask() {

    // given
    ProcessBuilder processBuilder = Bpmn.createExecutableProcess();

    // when
    instance = processBuilder
                  .startEvent(START_EVENT_ID)
                  .userTask(USER_TASK_ID)
                  .done();

    // then
    assertTaskShapeProperties(USER_TASK_ID);
  }

  @Test
  public void shouldGenerateShapeForSendTask() {

    // given
    ProcessBuilder processBuilder = Bpmn.createExecutableProcess();

    // when
    instance = processBuilder
                  .startEvent(START_EVENT_ID)
                  .sendTask(SEND_TASK_ID)
                  .done();

    // then
    assertTaskShapeProperties(SEND_TASK_ID);
  }

  @Test
  public void shouldGenerateShapeForServiceTask() {

    // given
    ProcessBuilder processBuilder = Bpmn.createExecutableProcess();

    // when
    instance = processBuilder
                   .startEvent(START_EVENT_ID)
                   .serviceTask(SERVICE_TASK_ID)
                   .done();

    // then
    assertTaskShapeProperties(SERVICE_TASK_ID);
  }

  @Test
  public void shouldGenerateShapeForReceiveTask() {

    // given
    ProcessBuilder processBuilder = Bpmn.createExecutableProcess();

    // when
    instance = processBuilder
                  .startEvent(START_EVENT_ID)
                  .receiveTask(TASK_ID)
                  .done();

    // then
    assertTaskShapeProperties(TASK_ID);
  }

  @Test
  public void shouldGenerateShapeForManualTask() {

    // given
    ProcessBuilder processBuilder = Bpmn.createExecutableProcess();

    // when
    instance = processBuilder
                  .startEvent(START_EVENT_ID)
                  .manualTask(TASK_ID)
                  .done();

    // then
    assertTaskShapeProperties(TASK_ID);
  }

  @Test
  public void shouldGenerateShapeForBusinessRuleTask() {

    // given
    ProcessBuilder processBuilder = Bpmn.createExecutableProcess();

    // when
    instance = processBuilder
                  .startEvent(START_EVENT_ID)
                  .businessRuleTask(TASK_ID)
                  .done();

    // then
    assertTaskShapeProperties(TASK_ID);
  }

  @Test
  public void shouldGenerateShapeForScriptTask() {

    // given
    ProcessBuilder processBuilder = Bpmn.createExecutableProcess();

    // when
    instance = processBuilder
                  .startEvent(START_EVENT_ID)
                  .scriptTask(TASK_ID)
                  .done();

    // then
    assertTaskShapeProperties(TASK_ID);
  }

  @Test
  public void shouldGenerateShapeForCatchingIntermediateEvent() {

    // given
    ProcessBuilder processBuilder = Bpmn.createExecutableProcess();

    // when
    instance = processBuilder
                  .startEvent(START_EVENT_ID)
                  .intermediateCatchEvent(CATCH_ID)
                  .endEvent(END_EVENT_ID)
                  .done();

    // then
    Collection<BpmnShape> allShapes = instance.getModelElementsByType(BpmnShape.class);
    assertEquals(3, allShapes.size());

    BpmnShape bpmnShapeEvent = findBpmnShape(CATCH_ID);
    assertThat(bpmnShapeEvent).isNotNull();
    assertEventSize(bpmnShapeEvent);
  }

  @Test
  public void shouldGenerateShapeForBoundaryIntermediateEvent() {

    // given
    ProcessBuilder processBuilder = Bpmn.createExecutableProcess();

    // when
    instance = processBuilder
                  .startEvent(START_EVENT_ID)
                  .userTask(USER_TASK_ID)
                  .endEvent(END_EVENT_ID)
                    .moveToActivity(USER_TASK_ID)
                      .boundaryEvent(BOUNDARY_ID)
                        .conditionalEventDefinition(CONDITION_ID)
                          .condition(TEST_CONDITION)
                        .conditionalEventDefinitionDone()
                      .endEvent()
                 .done();

    // then
    Collection<BpmnShape> allShapes = instance.getModelElementsByType(BpmnShape.class);
    assertEquals(5, allShapes.size());

    BpmnShape bpmnShapeEvent = findBpmnShape(BOUNDARY_ID);
    assertNotNull(bpmnShapeEvent);
    assertEventSize(bpmnShapeEvent);
  }

  @Test
  public void shouldGenerateShapeForThrowingIntermediateEvent() {

    // given
    ProcessBuilder processBuilder = Bpmn.createExecutableProcess();

    // when
    instance = processBuilder
                  .startEvent(START_EVENT_ID)
                  .intermediateThrowEvent("inter")
                  .endEvent(END_EVENT_ID).done();

    // then
    Collection<BpmnShape> allShapes = instance.getModelElementsByType(BpmnShape.class);
    assertEquals(3, allShapes.size());

    BpmnShape bpmnShapeEvent = findBpmnShape("inter");
    assertNotNull(bpmnShapeEvent);
    assertEventSize(bpmnShapeEvent);
  }

  @Test
  public void shouldGenerateShapeForBlankSubProcess() {

    // given
    ProcessBuilder processBuilder = Bpmn.createExecutableProcess();

    // when
    instance = processBuilder
                  .startEvent(START_EVENT_ID)
                  .subProcess(SUB_PROCESS_ID)
                  .endEvent(END_EVENT_ID)
                  .done();

    // then
    Collection<BpmnShape> allShapes = instance.getModelElementsByType(BpmnShape.class);
    assertEquals(3, allShapes.size());

    BpmnShape bpmnShapeSubProcess = findBpmnShape(SUB_PROCESS_ID);
    assertNotNull(bpmnShapeSubProcess);
    assertSubProcessSize(bpmnShapeSubProcess);
    assertTrue(bpmnShapeSubProcess.isExpanded());
  }

  @Test
  public void shouldGenerateShapeForSubProcess() {

    // given
    ProcessBuilder processBuilder = Bpmn.createExecutableProcess();

    // when
    instance = processBuilder
                  .startEvent(START_EVENT_ID)
                  .subProcess(SUB_PROCESS_ID)
                    .embeddedSubProcess()
                      .startEvent("innerStartEvent")
                      .userTask("innerUserTask")
                      .endEvent("innerEndEvent")
                    .subProcessDone()
                  .endEvent(END_EVENT_ID)
                  .done();

    // then
    Collection<BpmnShape> allShapes = instance.getModelElementsByType(BpmnShape.class);
    assertEquals(6, allShapes.size());

    BpmnShape content;
    content= findBpmnShape("innerStartEvent");
    assertNotNull(content);
    assertEventSize(content);

    content = findBpmnShape("innerUserTask");
    assertNotNull(content);
    assertActivitySize(content);

    content = findBpmnShape("innerEndEvent");
    assertNotNull(content);
    assertEventSize(content);

    BpmnShape bpmnShapeSubProcess = findBpmnShape(SUB_PROCESS_ID);
    assertNotNull(bpmnShapeSubProcess);
    assertTrue(bpmnShapeSubProcess.isExpanded());
  }

  @Test
  public void shouldGenerateShapeForEventSubProcess(){
 // given
    ProcessBuilder processBuilder = Bpmn.createExecutableProcess();

    // when
    instance = processBuilder
                  .startEvent(START_EVENT_ID)
                  .endEvent(END_EVENT_ID)
                  .subProcess(SUB_PROCESS_ID)
                    .triggerByEvent()
                      .embeddedSubProcess()
                        .startEvent("innerStartEvent")
                        .endEvent("innerEndEvent")
                      .subProcessDone()
                    .done();

 // then
    Collection<BpmnShape> allShapes = instance.getModelElementsByType(BpmnShape.class);
    assertEquals(5, allShapes.size());

    BpmnShape content;
    content= findBpmnShape("innerStartEvent");
    assertNotNull(content);
    assertEventSize(content);

    content = findBpmnShape("innerEndEvent");
    assertNotNull(content);
    assertEventSize(content);

    BpmnShape bpmnShapeEventSubProcess = findBpmnShape(SUB_PROCESS_ID);
    assertNotNull(bpmnShapeEventSubProcess);
    assertTrue(bpmnShapeEventSubProcess.isExpanded());
  }

  @Test
  public void shouldGenerateShapeForParallelGateway() {

    // given
    ProcessBuilder processBuilder = Bpmn.createExecutableProcess();

    // when
    instance = processBuilder
                  .startEvent(START_EVENT_ID)
                  .parallelGateway("and")
                  .endEvent(END_EVENT_ID)
                  .done();

    // then
    assertGatewayShapeProperties("and");
  }

  @Test
  public void shouldGenerateShapeForInclusiveGateway() {

    // given
    ProcessBuilder processBuilder = Bpmn.createExecutableProcess();

    // when
    instance = processBuilder
                  .startEvent(START_EVENT_ID)
                  .inclusiveGateway("inclusive")
                  .endEvent(END_EVENT_ID)
                  .done();

    // then
    assertGatewayShapeProperties("inclusive");
  }

  @Test
  public void shouldGenerateShapeForExclusiveGateway() {

    // given
    ProcessBuilder processBuilder = Bpmn.createExecutableProcess();

    // when
    instance = processBuilder
                  .startEvent(START_EVENT_ID)
                  .exclusiveGateway("or")
                  .endEvent(END_EVENT_ID)
                  .done();

    // then
    assertGatewayShapeProperties("or");
  }

  @Test
  public void shouldGenerateShapeForEndEvent() {

    // given
    ProcessBuilder processBuilder = Bpmn.createExecutableProcess();

    // when
    instance = processBuilder
                  .startEvent(START_EVENT_ID)
                  .endEvent(END_EVENT_ID)
                  .done();

    // then
    Collection<BpmnShape> allShapes = instance.getModelElementsByType(BpmnShape.class);
    assertEquals(2, allShapes.size());

    BpmnShape bpmnShapeEvent = findBpmnShape(END_EVENT_ID);
    assertNotNull(bpmnShapeEvent);
    assertEventSize(bpmnShapeEvent);
  }

  public void assertTaskShapeProperties(String id) {
    Collection<BpmnShape> allShapes = instance.getModelElementsByType(BpmnShape.class);
    assertEquals(2, allShapes.size());

    //
    BpmnShape bpmnShapeTask = findBpmnShape(id);
    assertNotNull(bpmnShapeTask);
    assertActivitySize(bpmnShapeTask);
  }

  public void assertGatewayShapeProperties(String id) {
    Collection<BpmnShape> allShapes = instance.getModelElementsByType(BpmnShape.class);
    assertEquals(3, allShapes.size());

    BpmnShape bpmnShapeGateway = findBpmnShape(id);
    assertNotNull(bpmnShapeGateway);
    assertGatewaySize(bpmnShapeGateway);
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

  protected void assertEventSize(BpmnShape shape){
    assertSize(shape, 36, 36);
  }

  protected void assertGatewaySize(BpmnShape shape){
    assertSize(shape, 50, 50);
  }

  protected void assertSubProcessSize(BpmnShape shape) {
    assertSize(shape, 200, 350);
  }

  protected void assertActivitySize(BpmnShape shape) {
    assertSize(shape, 80, 100);
  }

  protected void assertSize(BpmnShape shape, int height, int width) {
    assertThat(shape.getBounds().getHeight()).isEqualTo(height);
    assertThat(shape.getBounds().getWidth()).isEqualTo(width);
  }

}
