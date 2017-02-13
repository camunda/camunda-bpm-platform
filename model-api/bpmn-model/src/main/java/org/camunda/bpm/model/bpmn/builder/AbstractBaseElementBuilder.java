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

import java.util.Collection;
import java.util.Iterator;

import org.camunda.bpm.model.bpmn.BpmnModelException;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Activity;
import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.camunda.bpm.model.bpmn.instance.BpmnModelElementInstance;
import org.camunda.bpm.model.bpmn.instance.CompensateEventDefinition;
import org.camunda.bpm.model.bpmn.instance.Definitions;
import org.camunda.bpm.model.bpmn.instance.Error;
import org.camunda.bpm.model.bpmn.instance.ErrorEventDefinition;
import org.camunda.bpm.model.bpmn.instance.Escalation;
import org.camunda.bpm.model.bpmn.instance.EscalationEventDefinition;
import org.camunda.bpm.model.bpmn.instance.Event;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.Gateway;
import org.camunda.bpm.model.bpmn.instance.Message;
import org.camunda.bpm.model.bpmn.instance.MessageEventDefinition;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.Signal;
import org.camunda.bpm.model.bpmn.instance.SignalEventDefinition;
import org.camunda.bpm.model.bpmn.instance.SubProcess;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnEdge;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnPlane;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnShape;
import org.camunda.bpm.model.bpmn.instance.dc.Bounds;
import org.camunda.bpm.model.bpmn.instance.di.Waypoint;

/**
 * @author Sebastian Menski
 */
public abstract class AbstractBaseElementBuilder<B extends AbstractBaseElementBuilder<B, E>, E extends BaseElement> extends AbstractBpmnModelElementBuilder<B, E> {

  protected double SPACE = 50;

  protected AbstractBaseElementBuilder(BpmnModelInstance modelInstance, E element, Class<?> selfType) {
    super(modelInstance, element, selfType);
  }

  protected <T extends BpmnModelElementInstance> T createInstance(Class<T> typeClass) {
    return modelInstance.newInstance(typeClass);
  }

  protected <T extends BaseElement> T createInstance(Class<T> typeClass, String identifier) {
    T instance = createInstance(typeClass);
    if (identifier != null) {
      instance.setId(identifier);
    }
    return instance;
  }

  protected <T extends BpmnModelElementInstance> T createChild(Class<T> typeClass) {
    return createChild(element, typeClass);
  }

  protected <T extends BaseElement> T createChild(Class<T> typeClass, String identifier) {
    return createChild(element, typeClass, identifier);
  }

  protected <T extends BpmnModelElementInstance> T createChild(BpmnModelElementInstance parent, Class<T> typeClass) {
    T instance = createInstance(typeClass);
    parent.addChildElement(instance);
    return instance;
  }

  protected <T extends BaseElement> T createChild(BpmnModelElementInstance parent, Class<T> typeClass, String identifier) {
    T instance = createInstance(typeClass, identifier);
    parent.addChildElement(instance);
    return instance;
  }

  protected <T extends BpmnModelElementInstance> T createSibling(Class<T> typeClass) {
    T instance = createInstance(typeClass);
    element.getParentElement().addChildElement(instance);
    return instance;
  }

  protected <T extends BaseElement> T createSibling(Class<T> typeClass, String identifier) {
    T instance = createInstance(typeClass, identifier);
    element.getParentElement().addChildElement(instance);
    return instance;
  }

  protected <T extends BpmnModelElementInstance> T getCreateSingleChild(Class<T> typeClass) {
    return getCreateSingleChild(element, typeClass);
  }

  protected <T extends BpmnModelElementInstance> T getCreateSingleChild(BpmnModelElementInstance parent, Class<T> typeClass) {
    Collection<T> childrenOfType = parent.getChildElementsByType(typeClass);
    if (childrenOfType.isEmpty()) {
      return createChild(parent, typeClass);
    }
    else {
      if (childrenOfType.size() > 1) {
        throw new BpmnModelException("Element " + parent + " of type " +
            parent.getElementType().getTypeName() + " has more than one child element of type " +
            typeClass.getName());
      }
      else {
        return childrenOfType.iterator().next();
      }
    }
  }

  protected <T extends BpmnModelElementInstance> T getCreateSingleExtensionElement(Class<T> typeClass) {
    ExtensionElements extensionElements = getCreateSingleChild(ExtensionElements.class);
    return getCreateSingleChild(extensionElements, typeClass);
  }

  protected Message findMessageForName(String messageName) {
    Collection<Message> messages = modelInstance.getModelElementsByType(Message.class);
    for (Message message : messages) {
      if (messageName.equals(message.getName())) {
        // return already existing message for message name
        return message;
      }
    }

    // create new message for non existing message name
    Definitions definitions = modelInstance.getDefinitions();
    Message message = createChild(definitions, Message.class);
    message.setName(messageName);

    return message;
  }

  protected MessageEventDefinition createMessageEventDefinition(String messageName) {
    Message message = findMessageForName(messageName);
    MessageEventDefinition messageEventDefinition = createInstance(MessageEventDefinition.class);
    messageEventDefinition.setMessage(message);
    return messageEventDefinition;
  }

  protected MessageEventDefinition createEmptyMessageEventDefinition() {
    return createInstance(MessageEventDefinition.class);
  }

  protected Signal findSignalForName(String signalName) {
    Collection<Signal> signals = modelInstance.getModelElementsByType(Signal.class);
    for (Signal signal : signals) {
      if (signalName.equals(signal.getName())) {
        // return already existing signal for signal name
        return signal;
      }
    }

    // create new signal for non existing signal name
    Definitions definitions = modelInstance.getDefinitions();
    Signal signal = createChild(definitions, Signal.class);
    signal.setName(signalName);

    return signal;
  }

  protected SignalEventDefinition createSignalEventDefinition(String signalName) {
    Signal signal = findSignalForName(signalName);
    SignalEventDefinition signalEventDefinition = createInstance(SignalEventDefinition.class);
    signalEventDefinition.setSignal(signal);
    return signalEventDefinition;
  }

  protected ErrorEventDefinition findErrorDefinitionForCode(String errorCode) {
    Collection<ErrorEventDefinition> definitions = modelInstance.getModelElementsByType(ErrorEventDefinition.class);
    for(ErrorEventDefinition definition: definitions) {
      Error error = definition.getError();
      if(error != null && error.getErrorCode().equals(errorCode)) {
          return definition;
      }
    }
    return null;
  }

  protected Error findErrorForNameAndCode(String errorCode) {
    Collection<Error> errors = modelInstance.getModelElementsByType(Error.class);
    for (Error error : errors) {
      if (errorCode.equals(error.getErrorCode())) {
        // return already existing error
        return error;
      }
    }

    // create new error
    Definitions definitions = modelInstance.getDefinitions();
    Error error = createChild(definitions, Error.class);
    error.setErrorCode(errorCode);

    return error;
  }

  protected ErrorEventDefinition createEmptyErrorEventDefinition() {
    ErrorEventDefinition errorEventDefinition = createInstance(ErrorEventDefinition.class);
    return errorEventDefinition;
  }

  protected ErrorEventDefinition createErrorEventDefinition(String errorCode) {
    Error error = findErrorForNameAndCode(errorCode);
    ErrorEventDefinition errorEventDefinition = createInstance(ErrorEventDefinition.class);
    errorEventDefinition.setError(error);
    return errorEventDefinition;
  }

  protected Escalation findEscalationForCode(String escalationCode) {
    Collection<Escalation> escalations = modelInstance.getModelElementsByType(Escalation.class);
    for (Escalation escalation : escalations) {
      if (escalationCode.equals(escalation.getEscalationCode())) {
          // return already existing escalation
          return escalation;
      }
    }

    Definitions definitions = modelInstance.getDefinitions();
    Escalation escalation = createChild(definitions, Escalation.class);
    escalation.setEscalationCode(escalationCode);
    return escalation;
  }

  protected EscalationEventDefinition createEscalationEventDefinition(String escalationCode) {
    Escalation escalation = findEscalationForCode(escalationCode);
    EscalationEventDefinition escalationEventDefinition = createInstance(EscalationEventDefinition.class);
    escalationEventDefinition.setEscalation(escalation);
    return escalationEventDefinition;
  }

  protected CompensateEventDefinition createCompensateEventDefinition() {
    CompensateEventDefinition compensateEventDefinition = createInstance(CompensateEventDefinition.class);
    return compensateEventDefinition;
  }


  /**
   * Sets the identifier of the element.
   *
   * @param identifier  the identifier to set
   * @return the builder object
   */
  public B id(String identifier) {
    element.setId(identifier);
    return myself;
  }

  /**
   * Add an extension element to the element.
   *
   * @param extensionElement  the extension element to add
   * @return the builder object
   */
  public B addExtensionElement(BpmnModelElementInstance extensionElement) {
    ExtensionElements extensionElements = getCreateSingleChild(ExtensionElements.class);
    extensionElements.addChildElement(extensionElement);
    return myself;
  }

  public BpmnShape createBpmnShape(FlowNode node) {
    BpmnPlane bpmnPlane = findBpmnPlane();
    if (bpmnPlane != null) {
      BpmnShape bpmnShape = createInstance(BpmnShape.class);
      bpmnShape.setBpmnElement(node);
      Bounds nodeBounds = createInstance(Bounds.class);

      if (node instanceof SubProcess) {
        bpmnShape.setExpanded(true);
        nodeBounds.setWidth(350);
        nodeBounds.setHeight(200);
      } else if (node instanceof Activity) {
        nodeBounds.setWidth(100);
        nodeBounds.setHeight(80);
      } else if (node instanceof Event) {
        nodeBounds.setWidth(36);
        nodeBounds.setHeight(36);
      } else if (node instanceof Gateway) {
        nodeBounds.setWidth(50);
        nodeBounds.setHeight(50);
      }

      nodeBounds.setX(0);
      nodeBounds.setY(0);

      bpmnShape.addChildElement(nodeBounds);
      bpmnPlane.addChildElement(bpmnShape);

      return bpmnShape;
    }
    return null;
  }

  protected void setTargetCoordinates(BpmnShape targetBpmnShape) {
    Bounds elemBounds = findBpmnShape(element).getBounds();
    Bounds bounds = targetBpmnShape.getBounds();

    double x = elemBounds.getX() + elemBounds.getWidth() + SPACE;

    double y = 0;
    Collection<SequenceFlow> outgoing = ((FlowNode) element).getOutgoing();
    if (outgoing.size() == 0) {
      y = elemBounds.getY() + elemBounds.getHeight() / 2 - bounds.getHeight() / 2;
    }
    else {
      SequenceFlow last = (SequenceFlow) outgoing.toArray()[outgoing.size()-1];
      FlowNode target = last.getTarget();
      BpmnShape lastBpmnShape = findBpmnShape(target);
      y = lastBpmnShape.getBounds().getY() + lastBpmnShape.getBounds().getHeight() + SPACE;
    }

    bounds.setX(x);
    bounds.setY(y);
  }

  public BpmnEdge createBpmnEdge(SequenceFlow sequenceFlow) {
    BpmnPlane bpmnPlane = findBpmnPlane();
    if (bpmnPlane != null) {


       BpmnEdge edge = createInstance(BpmnEdge.class);
       edge.setBpmnElement(sequenceFlow);
       setWaypoints(edge);

       bpmnPlane.addChildElement(edge);
       return edge;
    }
    return null;

  }


  protected void setWaypoints(BpmnEdge edge) {
    SequenceFlow sequenceFlow = (SequenceFlow) edge.getBpmnElement();

    Bounds elemBounds = findBpmnShape(sequenceFlow.getSource()).getBounds();
    Bounds targetBounds = findBpmnShape(sequenceFlow.getTarget()).getBounds();

    Waypoint w1 = createInstance(Waypoint.class);

    if (sequenceFlow.getSource().getOutgoing().size() == 1) {
      w1.setX(elemBounds.getX() + elemBounds.getWidth());
      w1.setY(elemBounds.getY() + elemBounds.getHeight() / 2);

      edge.addChildElement(w1);
    }
    else{
      w1.setX(elemBounds.getX() + elemBounds.getWidth() / 2);
      w1.setY(elemBounds.getY() + elemBounds.getHeight());

      edge.addChildElement(w1);

      Waypoint w2 = createInstance(Waypoint.class);
      w2.setX(elemBounds.getX() + elemBounds.getWidth() / 2);
      w2.setY(targetBounds.getY() + targetBounds.getHeight() / 2);

      edge.addChildElement(w2);
    }

    Waypoint w3 = createInstance(Waypoint.class);
    w3.setX(targetBounds.getX());
    w3.setY(targetBounds.getY() + targetBounds.getHeight() / 2);

    edge.addChildElement(w3);
  }

  protected BpmnPlane findBpmnPlane() {
    Collection<BpmnPlane> planes = modelInstance.getModelElementsByType(BpmnPlane.class);
    return planes.iterator().next();
  }

  protected BpmnShape findBpmnShape(BaseElement node) {
    Collection<BpmnShape> allShapes = modelInstance.getModelElementsByType(BpmnShape.class);

    Iterator<BpmnShape> iterator = allShapes.iterator();
    while (iterator.hasNext()) {
      BpmnShape shape = iterator.next();
      if (shape.getBpmnElement().equals(node)) {
        return shape;
      }
    }
    return null;
  }

  protected BpmnEdge findBpmnEdge(BaseElement sequenceFlow){
    Collection<BpmnEdge> allEdges = modelInstance.getModelElementsByType(BpmnEdge.class);
    Iterator<BpmnEdge> iterator = allEdges.iterator();

    while (iterator.hasNext()) {
      BpmnEdge edge = iterator.next();
      if(edge.getBpmnElement().equals(sequenceFlow)) {
        return edge;
      }
    }
    return null;
  }

  protected void adjustSubProcess(BpmnShape newShape) {
    BaseElement newElement = newShape.getBpmnElement();
    Bounds newShapeBounds = newShape.getBounds();
    Bounds subProcessBounds = null;
    BpmnShape subProcess = null;
    double newWidth = 0;
    double newHeight = 0;
    double delta = 0;

    while (newElement.getParentElement() instanceof SubProcess) {

      subProcess = findBpmnShape((BaseElement) newElement.getParentElement());
      subProcessBounds = subProcess.getBounds();
      newWidth = newShapeBounds.getX() + newShapeBounds.getWidth() + SPACE;
      newHeight = newShapeBounds.getY() + newShapeBounds.getHeight() + SPACE;
      delta = subProcessBounds.getWidth();

      if (newShapeBounds.getY().equals(subProcessBounds.getY())) {
        subProcessBounds.setY(subProcessBounds.getY() - SPACE);
        subProcessBounds.setHeight(subProcessBounds.getHeight() + SPACE);
      }

      if (newWidth >= subProcessBounds.getX() + subProcessBounds.getWidth()) {
        newWidth = newWidth - subProcessBounds.getX();
        subProcessBounds.setWidth(newWidth);
      }

      if (newHeight >= subProcessBounds.getY() + subProcessBounds.getHeight()) {
        newHeight = newHeight - subProcessBounds.getY();
        subProcessBounds.setHeight(newHeight);
      }

      delta = Math.abs(delta - subProcessBounds.getWidth());
      newElement = subProcess.getBpmnElement();
      newShapeBounds = subProcessBounds;

      if (delta != 0) {
        shiftFollowingShapes(delta, (FlowNode) newElement, newShapeBounds);
      }
    }
  }

  protected void shiftFollowingShapes(double delta, FlowNode subProcess, Bounds subProcessBounds) {
    Collection<SequenceFlow> outgoings = subProcess.getOutgoing();
    BpmnShape followNode = null;
    Bounds followNodeBounds = null;
    SequenceFlow tmpSeq = null;
    Collection<Waypoint> waypoints = null;
    Waypoint tmpWay = null;


    if (outgoings.size() > 0) {
      Iterator<SequenceFlow> iteratorSeq = outgoings.iterator();

      while (iteratorSeq.hasNext()) {
        tmpSeq = iteratorSeq.next();
        waypoints = findBpmnEdge(tmpSeq).getWaypoints();
        subProcessBounds = findBpmnShape(tmpSeq.getSource()).getBounds();
        followNode = findBpmnShape(tmpSeq.getTarget());
        followNodeBounds = followNode.getBounds();
        followNodeBounds.setX(followNodeBounds.getX() + delta);
        Iterator<Waypoint> iteratorWay = waypoints.iterator();

        while (iteratorWay.hasNext()) {
          tmpWay = iteratorWay.next();
          tmpWay.setX(tmpWay.getX() + delta);
        }

        shiftFollowingShapes(delta, (FlowNode) followNode.getBpmnElement(), followNodeBounds);
        adjustSubProcess(followNode);
      }
    }
  }

  public void setSpace(double space) {
    SPACE = space;
  }
}
