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
package org.camunda.bpm.engine.test.api.runtime.migration;

import java.util.Collection;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.AbstractActivityBuilder;
import org.camunda.bpm.model.bpmn.builder.AbstractBaseElementBuilder;
import org.camunda.bpm.model.bpmn.builder.AbstractFlowNodeBuilder;
import org.camunda.bpm.model.bpmn.builder.CallActivityBuilder;
import org.camunda.bpm.model.bpmn.builder.EndEventBuilder;
import org.camunda.bpm.model.bpmn.builder.IntermediateCatchEventBuilder;
import org.camunda.bpm.model.bpmn.builder.ServiceTaskBuilder;
import org.camunda.bpm.model.bpmn.builder.StartEventBuilder;
import org.camunda.bpm.model.bpmn.builder.SubProcessBuilder;
import org.camunda.bpm.model.bpmn.builder.UserTaskBuilder;
import org.camunda.bpm.model.bpmn.instance.Activity;
import org.camunda.bpm.model.bpmn.instance.Association;
import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.camunda.bpm.model.bpmn.instance.BpmnModelElementInstance;
import org.camunda.bpm.model.bpmn.instance.Definitions;
import org.camunda.bpm.model.bpmn.instance.Documentation;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.Message;
import org.camunda.bpm.model.bpmn.instance.MultiInstanceLoopCharacteristics;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.Signal;
import org.camunda.bpm.model.bpmn.instance.SubProcess;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnEdge;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnShape;
import org.camunda.bpm.model.xml.Model;
import org.camunda.bpm.model.xml.instance.DomDocument;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.type.ModelElementType;
import org.camunda.bpm.model.xml.validation.ModelElementValidator;
import org.camunda.bpm.model.xml.validation.ValidationResults;

public class ModifiableBpmnModelInstance implements BpmnModelInstance {

  protected BpmnModelInstance modelInstance;

  public ModifiableBpmnModelInstance(BpmnModelInstance modelInstance) {
    this.modelInstance = modelInstance;
  }

  /**
   * Copies the argument; following modifications are not applied to the original model instance
   */
  public static ModifiableBpmnModelInstance modify(BpmnModelInstance modelInstance) {
    return new ModifiableBpmnModelInstance(modelInstance.clone());
  }

  /**
   * wraps the argument; following modifications are applied to the original model instance
   */
  public static ModifiableBpmnModelInstance wrap(BpmnModelInstance modelInstance) {
    return new ModifiableBpmnModelInstance(modelInstance);
  }

  public Definitions getDefinitions() {
    return modelInstance.getDefinitions();
  }

  public void setDefinitions(Definitions definitions) {
    modelInstance.setDefinitions(definitions);
  }

  @Override
  public BpmnModelInstance clone() {
    return modelInstance.clone();
  }

  public DomDocument getDocument() {
    return modelInstance.getDocument();
  }

  public ModelElementInstance getDocumentElement() {
    return modelInstance.getDocumentElement();
  }

  public void setDocumentElement(ModelElementInstance documentElement) {
    modelInstance.setDocumentElement(documentElement);
  }

  public <T extends ModelElementInstance> T newInstance(Class<T> type) {
    return modelInstance.newInstance(type);
  }

  @Override
  public <T extends ModelElementInstance> T newInstance(Class<T> aClass, String s) {
    return modelInstance.newInstance(aClass, s);
  }

  public <T extends ModelElementInstance> T newInstance(ModelElementType type) {
    return modelInstance.newInstance(type);
  }

  @Override
  public <T extends ModelElementInstance> T newInstance(ModelElementType modelElementType, String s) {
    return modelInstance.newInstance(modelElementType, s);
  }

  public Model getModel() {
    return modelInstance.getModel();
  }

  public <T extends ModelElementInstance> T getModelElementById(String id) {
    return modelInstance.getModelElementById(id);
  }

  public Collection<ModelElementInstance> getModelElementsByType(ModelElementType referencingType) {
    return modelInstance.getModelElementsByType(referencingType);
  }

  public <T extends ModelElementInstance> Collection<T> getModelElementsByType(Class<T> referencingClass) {
    return modelInstance.getModelElementsByType(referencingClass);
  }

  @SuppressWarnings("unchecked")
  public <T extends AbstractBaseElementBuilder> T getBuilderForElementById(String id, Class<T> builderClass) {
    BaseElement modelElementById = modelInstance.getModelElementById(id);
    return (T) modelElementById.builder();
  }

  public AbstractActivityBuilder activityBuilder(String activityId) {
    return getBuilderForElementById(activityId, AbstractActivityBuilder.class);
  }

  public AbstractFlowNodeBuilder flowNodeBuilder(String flowNodeId) {
    return getBuilderForElementById(flowNodeId, AbstractFlowNodeBuilder.class);
  }

  public UserTaskBuilder userTaskBuilder(String userTaskId) {
    return getBuilderForElementById(userTaskId, UserTaskBuilder.class);
  }

  public ServiceTaskBuilder serviceTaskBuilder(String serviceTaskId) {
    return getBuilderForElementById(serviceTaskId, ServiceTaskBuilder.class);
  }

  public CallActivityBuilder callActivityBuilder(String callActivityId) {
    return getBuilderForElementById(callActivityId, CallActivityBuilder.class);
  }

  public IntermediateCatchEventBuilder intermediateCatchEventBuilder(String eventId) {
    return getBuilderForElementById(eventId, IntermediateCatchEventBuilder.class);
  }

  public StartEventBuilder startEventBuilder(String eventId) {
    return getBuilderForElementById(eventId, StartEventBuilder.class);
  }

  public EndEventBuilder endEventBuilder(String eventId) {
    return getBuilderForElementById(eventId, EndEventBuilder.class);
  }

  public ModifiableBpmnModelInstance changeElementId(String oldId, String newId) {
    BaseElement element = getModelElementById(oldId);
    element.setId(newId);
    return this;
  }

  public ModifiableBpmnModelInstance changeElementName(String elementId, String newName) {
    FlowElement flowElement = getModelElementById(elementId);
    flowElement.setName(newName);
    return this;
  }

  public ModifiableBpmnModelInstance removeChildren(String elementId) {
    BaseElement element = getModelElementById(elementId);

    Collection<BaseElement> children = element.getChildElementsByType(BaseElement.class);
    for (BaseElement child : children) {
      element.removeChildElement(child);
    }

    return this;
  }

  public ModifiableBpmnModelInstance renameMessage(String oldMessageName, String newMessageName) {
    Collection<Message> messages = modelInstance.getModelElementsByType(Message.class);

    for (Message message : messages) {
      if (message.getName().equals(oldMessageName)) {
        message.setName(newMessageName);
      }
    }

    return this;
  }

  public ModifiableBpmnModelInstance addDocumentation(String content) {
    Collection<Process> processes = modelInstance.getModelElementsByType(Process.class);
    Documentation documentation = modelInstance.newInstance(Documentation.class);
    documentation.setTextContent(content);
    for (Process process : processes) {
      process.addChildElement(documentation);
    }
    return this;
  }

  public ModifiableBpmnModelInstance renameSignal(String oldSignalName, String newSignalName) {
    Collection<Signal> signals = modelInstance.getModelElementsByType(Signal.class);

    for (Signal signal : signals) {
      if (signal.getName().equals(oldSignalName)) {
        signal.setName(newSignalName);
      }
    }

    return this;
  }

  public ModifiableBpmnModelInstance swapElementIds(String firstElementId, String secondElementId) {
    BaseElement firstElement = getModelElementById(firstElementId);
    BaseElement secondElement = getModelElementById(secondElementId);

    secondElement.setId("___TEMP___ID___");
    firstElement.setId(secondElementId);
    secondElement.setId(firstElementId);

    return this;
  }

  public SubProcessBuilder addSubProcessTo(String parentId) {
    SubProcess eventSubProcess = modelInstance.newInstance(SubProcess.class);

    BpmnModelElementInstance parent = getModelElementById(parentId);
    parent.addChildElement(eventSubProcess);

    return eventSubProcess.builder();
  }

  public ModifiableBpmnModelInstance removeFlowNode(String flowNodeId) {
    FlowNode flowNode = getModelElementById(flowNodeId);
    ModelElementInstance scope = flowNode.getParentElement();

    for (SequenceFlow outgoingFlow : flowNode.getOutgoing()) {
      removeBpmnEdge(outgoingFlow);
      scope.removeChildElement(outgoingFlow);
    }
    for (SequenceFlow incomingFlow : flowNode.getIncoming()) {
      removeBpmnEdge(incomingFlow);
      scope.removeChildElement(incomingFlow);
    }
    Collection<Association> associations = scope.getChildElementsByType(Association.class);
    for (Association association : associations) {
      if (flowNode.equals(association.getSource()) || flowNode.equals(association.getTarget())) {
        removeBpmnEdge(association);
        scope.removeChildElement(association);
      }
    }

    removeBpmnShape(flowNode);
    scope.removeChildElement(flowNode);

    return this;
  }

  protected void removeBpmnEdge(BaseElement element) {
    Collection<BpmnEdge> edges = modelInstance.getModelElementsByType(BpmnEdge.class);
    for (BpmnEdge edge : edges) {
      if (edge.getBpmnElement().equals(element)) {
        ModelElementInstance bpmnPlane = edge.getParentElement();
        bpmnPlane.removeChildElement(edge);
        break;
      }
    }
  }

  protected void removeBpmnShape(FlowNode flowNode) {
    Collection<BpmnShape> bpmnShapes = modelInstance.getModelElementsByType(BpmnShape.class);
    for (BpmnShape shape : bpmnShapes) {
      if (shape.getBpmnElement().equals(flowNode)) {
        ModelElementInstance bpmnPlane = shape.getParentElement();
        bpmnPlane.removeChildElement(shape);
        break;
      }
    }
  }

  public ModifiableBpmnModelInstance asyncBeforeInnerMiActivity(String activityId) {
    Activity activity = modelInstance.getModelElementById(activityId);

    MultiInstanceLoopCharacteristics miCharacteristics = (MultiInstanceLoopCharacteristics) activity.getUniqueChildElementByType(MultiInstanceLoopCharacteristics.class);
    miCharacteristics.setCamundaAsyncBefore(true);

    return this;
  }

  public ModifiableBpmnModelInstance asyncAfterInnerMiActivity(String activityId) {
    Activity activity = modelInstance.getModelElementById(activityId);

    MultiInstanceLoopCharacteristics miCharacteristics = (MultiInstanceLoopCharacteristics) activity.getUniqueChildElementByType(MultiInstanceLoopCharacteristics.class);
    miCharacteristics.setCamundaAsyncAfter(true);

    return this;
  }

  public ValidationResults validate(Collection<ModelElementValidator<?>> validators) {
    return null;
  }

}
