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

package org.camunda.bpm.engine.test.api.runtime.migration;

import java.util.Collection;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.AbstractActivityBuilder;
import org.camunda.bpm.model.bpmn.builder.AbstractBaseElementBuilder;
import org.camunda.bpm.model.bpmn.builder.AbstractFlowNodeBuilder;
import org.camunda.bpm.model.bpmn.builder.SubProcessBuilder;
import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.camunda.bpm.model.bpmn.instance.BpmnModelElementInstance;
import org.camunda.bpm.model.bpmn.instance.Definitions;
import org.camunda.bpm.model.bpmn.instance.SubProcess;
import org.camunda.bpm.model.xml.Model;
import org.camunda.bpm.model.xml.instance.DomDocument;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.type.ModelElementType;

public class ModifiableBpmnModelInstance implements BpmnModelInstance {

  protected BpmnModelInstance modelInstance;

  public ModifiableBpmnModelInstance(BpmnModelInstance modelInstance) {
    this.modelInstance = modelInstance;
  }

  public static ModifiableBpmnModelInstance modify(BpmnModelInstance modelInstance) {
    return new ModifiableBpmnModelInstance(modelInstance.clone());
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

  public <T extends ModelElementInstance> T newInstance(ModelElementType type) {
    return modelInstance.newInstance(type);
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

  public ModifiableBpmnModelInstance changeElementId(String oldId, String newId) {
    BaseElement element = getModelElementById(oldId);
    element.setId(newId);
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

  public ModifiableBpmnModelInstance addMessageBoundaryEvent(String activityId, String messageName) {
    return addMessageBoundaryEvent(activityId, null, messageName);
  }

  public ModifiableBpmnModelInstance addMessageBoundaryEvent(String activityId, String boundaryId, String messageName) {
    getBuilderForElementById(activityId, AbstractActivityBuilder.class)
      .boundaryEvent(boundaryId).message(messageName)
      .endEvent();

    return this;
  }

  public ModifiableBpmnModelInstance addMessageBoundaryEventWithUserTask(String activityId, String messageName, String userTaskId) {
    return addMessageBoundaryEventWithUserTask(activityId, null, messageName, userTaskId);
  }

  public ModifiableBpmnModelInstance addMessageBoundaryEventWithUserTask(String activityId, String boundaryId, String messageName, String userTaskId) {
    getBuilderForElementById(activityId, AbstractActivityBuilder.class)
      .boundaryEvent(boundaryId).message(messageName)
      .userTask(userTaskId)
      .endEvent();

    return this;
  }

  public ModifiableBpmnModelInstance addSignalBoundaryEvent(String activityId, String signalName) {
    return addSignalBoundaryEvent(activityId, null, signalName);
  }

  public ModifiableBpmnModelInstance addSignalBoundaryEvent(String activityId, String boundaryId, String signalName) {
    getBuilderForElementById(activityId, AbstractActivityBuilder.class)
      .boundaryEvent(boundaryId).signal(signalName)
      .endEvent();

    return this;
  }

  public ModifiableBpmnModelInstance addSignalBoundaryEventWithUserTask(String activityId, String signalName, String userTaskId) {
    return addSignalBoundaryEventWithUserTask(activityId, null, signalName, userTaskId);
  }

  public ModifiableBpmnModelInstance addSignalBoundaryEventWithUserTask(String activityId, String boundaryId, String signalName, String userTaskId) {
    getBuilderForElementById(activityId, AbstractActivityBuilder.class)
      .boundaryEvent(boundaryId).signal(signalName)
      .userTask(userTaskId)
      .endEvent();

    return this;
  }

  public ModifiableBpmnModelInstance addTimerDateBoundaryEvent(String activityId, String timerDate) {
    return addTimerDateBoundaryEvent(activityId, null, timerDate);
  }

  public ModifiableBpmnModelInstance addTimerDateBoundaryEvent(String activityId, String boundaryId, String timerDate) {
    getBuilderForElementById(activityId, AbstractActivityBuilder.class)
      .boundaryEvent(boundaryId).timerWithDate(timerDate)
      .endEvent();

    return this;
  }

  public ModifiableBpmnModelInstance addTimerDateBoundaryEventWithUserTask(String activityId, String timerDate, String userTaskId) {
    return addTimerDateBoundaryEventWithUserTask(activityId, null, timerDate, userTaskId);
  }

  public ModifiableBpmnModelInstance addTimerDateBoundaryEventWithUserTask(String activityId, String boundaryId, String timerDate, String userTaskId) {
    getBuilderForElementById(activityId, AbstractActivityBuilder.class)
      .boundaryEvent(boundaryId).timerWithDate(timerDate)
      .userTask(userTaskId)
      .endEvent();

    return this;
  }

  public ModifiableBpmnModelInstance addTimerCycleBoundaryEvent(String activityId, String timerCycle) {
    return addTimerCycleBoundaryEvent(activityId, null, timerCycle);
  }

  public ModifiableBpmnModelInstance addTimerCycleBoundaryEvent(String activityId, String boundaryId, String timerCycle) {
    getBuilderForElementById(activityId, AbstractActivityBuilder.class)
      .boundaryEvent(boundaryId).timerWithCycle(timerCycle)
      .endEvent();

    return this;
  }

  public ModifiableBpmnModelInstance addTimerCycleBoundaryEventWithUserTask(String activityId, String timerCycle, String userTaskId) {
    return addTimerCycleBoundaryEventWithUserTask(activityId, null, timerCycle, userTaskId);
  }

  public ModifiableBpmnModelInstance addTimerCycleBoundaryEventWithUserTask(String activityId, String boundaryId, String timerCycle, String userTaskId) {
    getBuilderForElementById(activityId, AbstractActivityBuilder.class)
      .boundaryEvent(boundaryId).timerWithCycle(timerCycle)
      .userTask(userTaskId)
      .endEvent();

    return this;
  }

  public ModifiableBpmnModelInstance addTimerDurationBoundaryEvent(String activityId, String timerDuration) {
    return addTimerDurationBoundaryEventWithUserTask(activityId, null, timerDuration);
  }

  public ModifiableBpmnModelInstance addTimerDurationBoundaryEvent(String activityId, String boundaryId, String timerDuration) {
    getBuilderForElementById(activityId, AbstractActivityBuilder.class)
      .boundaryEvent(boundaryId).timerWithDuration(timerDuration)
      .endEvent();

    return this;
  }

  public ModifiableBpmnModelInstance addTimerDurationBoundaryEventWithUserTask(String activityId, String timerDuration, String userTaskId) {
    return addTimerDurationBoundaryEventWithUserTask(activityId, null, timerDuration, userTaskId);
  }

  public ModifiableBpmnModelInstance addTimerDurationBoundaryEventWithUserTask(String activityId, String boundaryId, String timerDuration, String userTaskId) {
    getBuilderForElementById(activityId, AbstractActivityBuilder.class)
      .boundaryEvent(boundaryId).timerWithDuration(timerDuration)
      .userTask(userTaskId)
      .endEvent();

    return this;
  }

  public ModifiableBpmnModelInstance addErrorBoundaryEvent(String activityId, String errorCode) {
    return addErrorBoundaryEvent(activityId, null, errorCode);
  }

  public ModifiableBpmnModelInstance addErrorBoundaryEvent(String activityId, String boundaryId, String errorCode) {
    getBuilderForElementById(activityId, AbstractActivityBuilder.class)
      .boundaryEvent(boundaryId).error(errorCode)
      .endEvent();

    return this;
  }

  public ModifiableBpmnModelInstance addErrorBoundaryEventWithUserTask(String activityId, String errorCode, String userTaskId) {
    return addErrorBoundaryEventWithUserTask(activityId, null, errorCode, userTaskId);
  }

  public ModifiableBpmnModelInstance addErrorBoundaryEventWithUserTask(String activityId, String boundaryId, String errorCode, String userTaskId) {
    getBuilderForElementById(activityId, AbstractActivityBuilder.class)
      .boundaryEvent(boundaryId).error(errorCode)
      .userTask(userTaskId)
      .endEvent();

    return this;
  }

  public ModifiableBpmnModelInstance addEscalationBoundaryEvent(String activityId, String escalationCode) {
    return addEscalationBoundaryEvent(activityId, null, escalationCode);
  }

  public ModifiableBpmnModelInstance addEscalationBoundaryEvent(String activityId, String boundaryId, String escalationCode) {
    getBuilderForElementById(activityId, AbstractActivityBuilder.class)
      .boundaryEvent(boundaryId).escalation(escalationCode)
      .endEvent();

    return this;
  }

  public ModifiableBpmnModelInstance addEscalationBoundaryEventWithUserTask(String activityId, String escalationCode, String userTaskId) {
    return addEscalationBoundaryEventWithUserTask(activityId, null, escalationCode, userTaskId);
  }

  public ModifiableBpmnModelInstance addEscalationBoundaryEventWithUserTask(String activityId, String boundaryId, String escalationCode, String userTaskId) {
    getBuilderForElementById(activityId, AbstractActivityBuilder.class)
      .boundaryEvent(boundaryId).escalation(escalationCode)
      .userTask(userTaskId)
      .endEvent();

    return this;
  }

  public ModifiableBpmnModelInstance addCamundaInputParameter(String activityId, String name, String value) {
    getBuilderForElementById(activityId, AbstractActivityBuilder.class)
      .camundaInputParameter(name, value);

    return this;
  }

  public ModifiableBpmnModelInstance addCamundaOutputParameter(String activityId, String name, String value) {
    getBuilderForElementById(activityId, AbstractActivityBuilder.class)
      .camundaOutputParameter(name, value);

    return this;
  }

  public ModifiableBpmnModelInstance addCamundaExecutionListenerClass(String activityId, String eventName, String className) {
    getBuilderForElementById(activityId, AbstractFlowNodeBuilder.class)
      .camundaExecutionListenerClass(eventName, className);

    return this;
  }

  public SubProcessBuilder addSubProcessToParent(String parentId) {
    SubProcess eventSubProcess = modelInstance.newInstance(SubProcess.class);

    BpmnModelElementInstance parent = getModelElementById(parentId);
    parent.addChildElement(eventSubProcess);

    return eventSubProcess.builder();
  }

}
