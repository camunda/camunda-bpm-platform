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

package org.camunda.bpm.model.bpmn.builder;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.BoundaryEvent;
import org.camunda.bpm.model.bpmn.instance.ErrorEventDefinition;
import org.camunda.bpm.model.bpmn.instance.EscalationEventDefinition;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnEdge;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnShape;
import org.camunda.bpm.model.bpmn.instance.dc.Bounds;
import org.camunda.bpm.model.bpmn.instance.di.Waypoint;

/**
 * @author Sebastian Menski
 */
public abstract class AbstractBoundaryEventBuilder<B extends AbstractBoundaryEventBuilder<B>> extends AbstractCatchEventBuilder<B, BoundaryEvent> {

  protected AbstractBoundaryEventBuilder(BpmnModelInstance modelInstance, BoundaryEvent element, Class<?> selfType) {
    super(modelInstance, element, selfType);
  }

  /**
   * Set if the boundary event cancels the attached activity.
   *
   * @param cancelActivity true if the boundary event cancels the activiy, false otherwise
   * @return the builder object
   */
  public B cancelActivity(Boolean cancelActivity) {
    element.setCancelActivity(cancelActivity);

    return myself;
  }

  /**
   * Sets a catch all error definition.
   *
   * @return the builder object
   */
  public B error() {
    ErrorEventDefinition errorEventDefinition = createInstance(ErrorEventDefinition.class);
    element.getEventDefinitions().add(errorEventDefinition);

    return myself;
  }

  /**
   * Sets an error definition for the given error code. If already an error
   * with this code exists it will be used, otherwise a new error is created.
   *
   * @param errorCode the code of the error
   * @return the builder object
   */
  public B error(String errorCode) {
    ErrorEventDefinition errorEventDefinition = createErrorEventDefinition(errorCode);
    element.getEventDefinitions().add(errorEventDefinition);

    return myself;
  }

  /**
   * Creates an error event definition with an unique id
   * and returns a builder for the error event definition.
   *
   * @return the error event definition builder object
   */
  public ErrorEventDefinitionBuilder errorEventDefinition(String id) {
    ErrorEventDefinition errorEventDefinition = createEmptyErrorEventDefinition();
    if (id != null) {
      errorEventDefinition.setId(id);
    }

    element.getEventDefinitions().add(errorEventDefinition);
    return new ErrorEventDefinitionBuilder(modelInstance, errorEventDefinition);
  }

  /**
   * Creates an error event definition
   * and returns a builder for the error event definition.
   *
   * @return the error event definition builder object
   */
  public ErrorEventDefinitionBuilder errorEventDefinition() {
    ErrorEventDefinition errorEventDefinition = createEmptyErrorEventDefinition();
    element.getEventDefinitions().add(errorEventDefinition);
    return new ErrorEventDefinitionBuilder(modelInstance, errorEventDefinition);
  }

  /**
   * Sets a catch all escalation definition.
   *
   * @return the builder object
   */
  public B escalation() {
    EscalationEventDefinition escalationEventDefinition = createInstance(EscalationEventDefinition.class);
    element.getEventDefinitions().add(escalationEventDefinition);

    return myself;
  }

  /**
   * Sets an escalation definition for the given escalation code. If already an escalation
   * with this code exists it will be used, otherwise a new escalation is created.
   *
   * @param escalationCode the code of the escalation
   * @return the builder object
   */
  public B escalation(String escalationCode) {
    EscalationEventDefinition escalationEventDefinition = createEscalationEventDefinition(escalationCode);
    element.getEventDefinitions().add(escalationEventDefinition);

    return myself;
  }


  @Override
  protected void setTargetCoordinates(BpmnShape targetBpmnShape) {
    Bounds elemBounds = findBpmnShape(element).getBounds();
    Bounds targetBounds = targetBpmnShape.getBounds();
    double x = elemBounds.getX() + elemBounds.getWidth() + SPACE;
    double y = elemBounds.getY() + elemBounds.getHeight() / 2 - targetBounds.getHeight() / 2 + SPACE;
    targetBounds.setX(x);
    targetBounds.setY(y);
  }

  @Override
  protected void setWaypoints(BpmnEdge edge) {
    Bounds elemBounds = findBpmnShape(( (SequenceFlow) edge.getBpmnElement()).getSource()).getBounds();
    Bounds targetBounds = findBpmnShape(( (SequenceFlow) edge.getBpmnElement()).getTarget()).getBounds();

    Waypoint w1 = createInstance(Waypoint.class);
    w1.setX(elemBounds.getX() + elemBounds.getWidth() / 2);
    w1.setY(elemBounds.getY() + elemBounds.getHeight());

    Waypoint w2 = createInstance(Waypoint.class);
    w2.setX(elemBounds.getX() + elemBounds.getWidth() / 2);
    w2.setY(elemBounds.getY() + elemBounds.getHeight() / 2 + SPACE);

    Waypoint w3 = createInstance(Waypoint.class);
    w3.setX(targetBounds.getX());
    w3.setY(targetBounds.getY() + targetBounds.getHeight() / 2);

    edge.addChildElement(w1);
    edge.addChildElement(w2);
    edge.addChildElement(w3);
 }
}
