/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.model.bpmn;

import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.bpmndi.*;
import org.camunda.bpm.model.bpmn.instance.dc.Font;
import org.camunda.bpm.model.bpmn.instance.di.DiagramElement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.*;

/**
 * @author Sebastian Menski
 */
public class BpmnDiTest {

  private BpmnModelInstance modelInstance;
  private Collaboration collaboration;
  private Participant participant;
  private Process process;
  private StartEvent startEvent;
  private ServiceTask serviceTask;
  private ExclusiveGateway exclusiveGateway;
  private SequenceFlow sequenceFlow;
  private MessageFlow messageFlow;
  private DataInputAssociation dataInputAssociation;
  private Association association;
  private EndEvent endEvent;

  @Before
  public void parseModel() {
    modelInstance = Bpmn.readModelFromStream(getClass().getResourceAsStream(getClass().getSimpleName() + ".xml"));
    collaboration = modelInstance.getModelElementById(COLLABORATION_ID);
    participant = modelInstance.getModelElementById(PARTICIPANT_ID + 1);
    process = modelInstance.getModelElementById(PROCESS_ID + 1);
    serviceTask = modelInstance.getModelElementById(SERVICE_TASK_ID);
    exclusiveGateway = modelInstance.getModelElementById(EXCLUSIVE_GATEWAY);
    startEvent = modelInstance.getModelElementById(START_EVENT_ID + 2);
    sequenceFlow = modelInstance.getModelElementById(SEQUENCE_FLOW_ID + 3);
    messageFlow = modelInstance.getModelElementById(MESSAGE_FLOW_ID);
    dataInputAssociation = modelInstance.getModelElementById(DATA_INPUT_ASSOCIATION_ID);
    association = modelInstance.getModelElementById(ASSOCIATION_ID);
    endEvent = modelInstance.getModelElementById(END_EVENT_ID + 2);
  }

  @Test
  public void testBpmnDiagram() {
    Collection<BpmnDiagram> diagrams = modelInstance.getModelElementsByType(BpmnDiagram.class);
    assertThat(diagrams).hasSize(1);
    BpmnDiagram diagram = diagrams.iterator().next();
    assertThat(diagram.getBpmnPlane()).isNotNull();
    assertThat(diagram.getBpmnPlane().getBpmnElement()).isEqualTo(collaboration);
    assertThat(diagram.getBpmnLabelStyles()).hasSize(1);
  }

  @Test
  public void testBpmnPane() {
    DiagramElement diagramElement = collaboration.getDiagramElement();
    assertThat(diagramElement)
      .isNotNull()
      .isInstanceOf(BpmnPlane.class);
    BpmnPlane bpmnPlane = (BpmnPlane) diagramElement;
    assertThat(bpmnPlane.getBpmnElement()).isEqualTo(collaboration);
    assertThat(bpmnPlane.getChildElementsByType(DiagramElement.class)).isNotEmpty();
  }

  @Test
  public void testBpmnLabelStyle() {
    BpmnLabelStyle labelStyle = modelInstance.getModelElementsByType(BpmnLabelStyle.class).iterator().next();
    Font font = labelStyle.getFont();
    assertThat(font).isNotNull();
    assertThat(font.getName()).isEqualTo("Arial");
    assertThat(font.getSize()).isEqualTo(8.0);
    assertThat(font.isBold()).isTrue();
    assertThat(font.isItalic()).isFalse();
    assertThat(font.isStrikeThrough()).isFalse();
    assertThat(font.isUnderline()).isFalse();
  }

  @Test
  public void testBpmnShape() {
    BpmnShape shape = serviceTask.getDiagramElement();
    assertThat(shape.getBpmnElement()).isEqualTo(serviceTask);
    assertThat(shape.getBpmnLabel()).isNull();
    assertThat(shape.isExpanded()).isFalse();
    assertThat(shape.isHorizontal()).isFalse();
    assertThat(shape.isMarkerVisible()).isFalse();
    assertThat(shape.isMessageVisible()).isFalse();
    assertThat(shape.getParticipantBandKind()).isNull();
    assertThat(shape.getChoreographyActivityShape()).isNull();
  }

  @Test
  public void testBpmnLabel() {
    BpmnShape shape = startEvent.getDiagramElement();
    assertThat(shape.getBpmnElement()).isEqualTo(startEvent);
    assertThat(shape.getBpmnLabel()).isNotNull();

    BpmnLabel label = shape.getBpmnLabel();
    assertThat(label.getLabelStyle()).isNull();
    assertThat(label.getBounds()).isNotNull();
  }

  @Test
  public void testBpmnEdge() {
    BpmnEdge edge = sequenceFlow.getDiagramElement();
    assertThat(edge.getBpmnElement()).isEqualTo(sequenceFlow);
    assertThat(edge.getBpmnLabel()).isNull();
    assertThat(edge.getMessageVisibleKind()).isNull();
    assertThat(edge.getSourceElement()).isInstanceOf(BpmnShape.class);
    assertThat(((BpmnShape) edge.getSourceElement()).getBpmnElement()).isEqualTo(startEvent);
    assertThat(edge.getTargetElement()).isInstanceOf(BpmnShape.class);
    assertThat(((BpmnShape) edge.getTargetElement()).getBpmnElement()).isEqualTo(endEvent);
  }

  @Test
  public void testDiagramElementTypes() {
    assertThat(collaboration.getDiagramElement()).isInstanceOf(BpmnPlane.class);
    assertThat(process.getDiagramElement()).isNull();
    assertThat(participant.getDiagramElement()).isInstanceOf(BpmnShape.class);
    assertThat(participant.getDiagramElement()).isInstanceOf(BpmnShape.class);
    assertThat(startEvent.getDiagramElement()).isInstanceOf(BpmnShape.class);
    assertThat(serviceTask.getDiagramElement()).isInstanceOf(BpmnShape.class);
    assertThat(exclusiveGateway.getDiagramElement()).isInstanceOf(BpmnShape.class);
    assertThat(endEvent.getDiagramElement()).isInstanceOf(BpmnShape.class);
    assertThat(sequenceFlow.getDiagramElement()).isInstanceOf(BpmnEdge.class);
    assertThat(messageFlow.getDiagramElement()).isInstanceOf(BpmnEdge.class);
    assertThat(dataInputAssociation.getDiagramElement()).isInstanceOf(BpmnEdge.class);
    assertThat(association.getDiagramElement()).isInstanceOf(BpmnEdge.class);
  }

  @Test
  public void shouldNotRemoveBpmElementReference() {
    assertThat(startEvent.getOutgoing()).contains(sequenceFlow);
    assertThat(endEvent.getIncoming()).contains(sequenceFlow);

    BpmnEdge edge = sequenceFlow.getDiagramElement();
    assertThat(edge.getBpmnElement()).isEqualTo(sequenceFlow);

    startEvent.getOutgoing().remove(sequenceFlow);
    endEvent.getIncoming().remove(sequenceFlow);

    assertThat(startEvent.getOutgoing()).doesNotContain(sequenceFlow);
    assertThat(endEvent.getIncoming()).doesNotContain(sequenceFlow);

    assertThat(edge.getBpmnElement()).isEqualTo(sequenceFlow);
  }

  @After
  public void validateModel() {
    Bpmn.validateModel(modelInstance);
  }

}
