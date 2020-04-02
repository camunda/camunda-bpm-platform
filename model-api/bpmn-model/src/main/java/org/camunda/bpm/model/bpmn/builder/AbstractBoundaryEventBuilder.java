/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
import org.camunda.bpm.model.bpmn.instance.*;
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
    return error(errorCode, null);
  }

  /**
   * Sets an error definition for the given error code. If already an error
   * with this code exists it will be used, otherwise a new error with the 
   * given error message is created.
   *
   * @param errorCode the code of the error
   * @param errorMessage the error message that is used when a new error needs
   *        to be created
   * @return the builder object
   */
  public B error(String errorCode, String errorMessage) {
    ErrorEventDefinition errorEventDefinition = createErrorEventDefinition(errorCode, errorMessage);
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
  protected void setCoordinates(BpmnShape shape) {
    BpmnShape source = findBpmnShape(element);
    Bounds shapeBounds = shape.getBounds();

    double x = 0;
    double y = 0;

    if (source != null) {
      Bounds sourceBounds = source.getBounds();

      double sourceX = sourceBounds.getX();
      double sourceWidth = sourceBounds.getWidth();
      double sourceY = sourceBounds.getY();
      double sourceHeight = sourceBounds.getHeight();
      double targetHeight = shapeBounds.getHeight();

      x = sourceX + sourceWidth + SPACE / 4;
      y = sourceY + sourceHeight - targetHeight / 2 + SPACE;
    }

    shapeBounds.setX(x);
    shapeBounds.setY(y);
  }

  @Override
  protected void setWaypointsWithSourceAndTarget(BpmnEdge edge, FlowNode edgeSource, FlowNode edgeTarget) {
    BpmnShape source = findBpmnShape(edgeSource);
    BpmnShape target = findBpmnShape(edgeTarget);

    if (source != null && target != null) {
      Bounds sourceBounds = source.getBounds();
      Bounds targetBounds = target.getBounds();

      double sourceX = sourceBounds.getX();
      double sourceY = sourceBounds.getY();
      double sourceWidth = sourceBounds.getWidth();
      double sourceHeight = sourceBounds.getHeight();

      double targetX = targetBounds.getX();
      double targetY = targetBounds.getY();
      double targetHeight = targetBounds.getHeight();

      Waypoint w1 = createInstance(Waypoint.class);
      w1.setX(sourceX + sourceWidth / 2);
      w1.setY(sourceY + sourceHeight);

      Waypoint w2 = createInstance(Waypoint.class);
      w2.setX(sourceX + sourceWidth / 2);
      w2.setY(sourceY + sourceHeight + SPACE);

      Waypoint w3 = createInstance(Waypoint.class);
      w3.setX(targetX);
      w3.setY(targetY + targetHeight / 2);

      edge.addChildElement(w1);
      edge.addChildElement(w2);
      edge.addChildElement(w3);
    }
  }
}
