/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.api.runtime.migration.models;

import org.camunda.bpm.model.bpmn.AssociationDirection;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Activity;
import org.camunda.bpm.model.bpmn.instance.Association;
import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.camunda.bpm.model.bpmn.instance.BoundaryEvent;
import org.camunda.bpm.model.bpmn.instance.CompensateEventDefinition;
import org.camunda.bpm.model.bpmn.instance.Event;
import org.camunda.bpm.model.bpmn.instance.ThrowEvent;
import org.camunda.bpm.model.bpmn.instance.UserTask;

/**
 * @author Thorben Lindhauer
 *
 */
public class CompensationModels {

  public static final BpmnModelInstance ONE_COMPENSATION_TASK_MODEL = ProcessModels.newModel()
    .startEvent()
    .userTask("userTask1")
    .userTask("userTask2")
    .intermediateThrowEvent("compensationEvent")
      .compensateEventDefinition()
      .waitForCompletion(true)
      .compensateEventDefinitionDone()
    .endEvent()
    .done();

  static {
    addUserTaskCompensationHandler(ONE_COMPENSATION_TASK_MODEL, "userTask1", "compensationHandler");
  }

  public static final BpmnModelInstance COMPENSATION_END_EVENT_MODEL = ProcessModels.newModel()
      .startEvent()
      .userTask("userTask1")
      .userTask("userTask2")
      .endEvent("compensationEvent")
        .compensateEventDefinition()
        .waitForCompletion(true)
      .done();

  static {
    addUserTaskCompensationHandler(COMPENSATION_END_EVENT_MODEL, "userTask1", "compensationHandler");
  }

  public static final BpmnModelInstance TRANSACTION_COMPENSATION_MODEL = TransactionModels.CANCEL_BOUNDARY_EVENT.clone();
  static {
    addUserTaskCompensationHandler(TRANSACTION_COMPENSATION_MODEL, "userTask", "compensationHandler");
  }

  protected static void addUserTaskCompensationHandler(BpmnModelInstance modelInstance, String activityId, String compensationHandlerId) {

    Activity activity = modelInstance.getModelElementById(activityId);
    BaseElement scope = (BaseElement) activity.getParentElement();

    UserTask compensationHandler = modelInstance.newInstance(UserTask.class);
    compensationHandler.setId(compensationHandlerId);
    compensationHandler.setForCompensation(true);
    scope.addChildElement(compensationHandler);

    BoundaryEvent compensationBoundaryEvent = modelInstance.newInstance(BoundaryEvent.class);
    compensationBoundaryEvent.setAttachedTo(activity);
    compensationBoundaryEvent
      .builder()
      .compensateEventDefinition();
    scope.addChildElement(compensationBoundaryEvent);

    Association association = modelInstance.newInstance(Association.class);
    association.setAssociationDirection(AssociationDirection.One);
    association.setSource(compensationBoundaryEvent);
    association.setTarget(compensationHandler);
    scope.addChildElement(association);

  }
}
