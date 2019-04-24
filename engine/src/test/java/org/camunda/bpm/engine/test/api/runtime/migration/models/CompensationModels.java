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
package org.camunda.bpm.engine.test.api.runtime.migration.models;

import static org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;

import org.camunda.bpm.model.bpmn.AssociationDirection;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Association;
import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.camunda.bpm.model.bpmn.instance.BoundaryEvent;
import org.camunda.bpm.model.bpmn.instance.UserTask;

/**
 * @author Thorben Lindhauer
 *
 */
public class CompensationModels {

  public static final BpmnModelInstance ONE_COMPENSATION_TASK_MODEL = ProcessModels.newModel()
    .startEvent()
    .userTask("userTask1")
      .boundaryEvent("compensationBoundary")
      .compensateEventDefinition()
      .compensateEventDefinitionDone()
    .moveToActivity("userTask1")
    .userTask("userTask2")
    .intermediateThrowEvent("compensationEvent")
      .compensateEventDefinition()
      .waitForCompletion(true)
      .compensateEventDefinitionDone()
    .endEvent()
    .done();
  static {
    addUserTaskCompensationHandler(ONE_COMPENSATION_TASK_MODEL, "compensationBoundary", "compensationHandler");
  }

public static final BpmnModelInstance COMPENSATION_ONE_TASK_SUBPROCESS_MODEL =  ProcessModels.newModel()
      .startEvent()
      .subProcess("subProcess")
        .embeddedSubProcess()
        .startEvent()
        .userTask("userTask1")
          .boundaryEvent("compensationBoundary")
          .compensateEventDefinition()
          .compensateEventDefinitionDone()
        .moveToActivity("userTask1")
        .endEvent()
      .subProcessDone()
      .userTask("userTask2")
      .intermediateThrowEvent("compensationEvent")
        .compensateEventDefinition()
        .waitForCompletion(true)
        .compensateEventDefinitionDone()
      .endEvent()
      .done();
  static {
    addUserTaskCompensationHandler(COMPENSATION_ONE_TASK_SUBPROCESS_MODEL, "compensationBoundary", "compensationHandler");
  }

  public static final BpmnModelInstance COMPENSATION_TWO_TASKS_SUBPROCESS_MODEL = ProcessModels.newModel()
      .startEvent()
      .subProcess("subProcess")
        .embeddedSubProcess()
        .startEvent()
        .userTask("userTask1")
          .boundaryEvent("compensationBoundary")
          .compensateEventDefinition()
          .compensateEventDefinitionDone()
        .moveToActivity("userTask1")
        .userTask("userTask2")
        .endEvent("subProcessEnd")
      .subProcessDone()
      .intermediateThrowEvent("compensationEvent")
        .compensateEventDefinition()
        .waitForCompletion(true)
        .compensateEventDefinitionDone()
      .endEvent()
      .done();

  static {
    addUserTaskCompensationHandler(COMPENSATION_TWO_TASKS_SUBPROCESS_MODEL, "compensationBoundary", "compensationHandler");
  }

  public static final BpmnModelInstance DOUBLE_SUBPROCESS_MODEL = ProcessModels.newModel()
        .startEvent()
        .subProcess("outerSubProcess")
          .embeddedSubProcess()
          .startEvent()
          .subProcess("innerSubProcess")
            .embeddedSubProcess()
            .startEvent()
            .userTask("userTask1")
              .boundaryEvent("compensationBoundary")
              .compensateEventDefinition()
              .compensateEventDefinitionDone()
            .moveToActivity("userTask1")
            .endEvent()
          .subProcessDone()
          .endEvent()
        .subProcessDone()
        .userTask("userTask2")
        .intermediateThrowEvent("compensationEvent")
          .compensateEventDefinition()
          .waitForCompletion(true)
          .compensateEventDefinitionDone()
        .endEvent()
        .done();
  static {
    CompensationModels.addUserTaskCompensationHandler(DOUBLE_SUBPROCESS_MODEL, "compensationBoundary", "compensationHandler");
  }

  public static final BpmnModelInstance COMPENSATION_END_EVENT_MODEL = ProcessModels.newModel()
      .startEvent()
      .userTask("userTask1")
        .boundaryEvent("compensationBoundary")
        .compensateEventDefinition()
        .compensateEventDefinitionDone()
      .moveToActivity("userTask1")
      .userTask("userTask2")
      .endEvent("compensationEvent")
        .compensateEventDefinition()
        .waitForCompletion(true)
      .done();

  static {
    addUserTaskCompensationHandler(COMPENSATION_END_EVENT_MODEL, "compensationBoundary", "compensationHandler");
  }

  public static final BpmnModelInstance TRANSACTION_COMPENSATION_MODEL = modify(TransactionModels.CANCEL_BOUNDARY_EVENT)
    .activityBuilder("userTask")
      .boundaryEvent("compensationBoundary")
      .compensateEventDefinition()
      .compensateEventDefinitionDone()
    .done();
  static {
    addUserTaskCompensationHandler(TRANSACTION_COMPENSATION_MODEL, "compensationBoundary", "compensationHandler");
  }

  public static final BpmnModelInstance COMPENSATION_EVENT_SUBPROCESS_MODEL = modify(COMPENSATION_ONE_TASK_SUBPROCESS_MODEL)
    .addSubProcessTo("subProcess")
      .id("eventSubProcess")
      .triggerByEvent()
      .embeddedSubProcess()
      .startEvent("eventSubProcessStart")
        .compensateEventDefinition()
        .compensateEventDefinitionDone()
      .userTask("eventSubProcessTask")
      .intermediateThrowEvent("eventSubProcessCompensationEvent")
        .compensateEventDefinition()
        .waitForCompletion(true)
        .compensateEventDefinitionDone()
      .endEvent()
      .endEvent()
    .done();

  public static void addUserTaskCompensationHandler(BpmnModelInstance modelInstance, String boundaryEventId, String compensationHandlerId) {

    BoundaryEvent boundaryEvent = modelInstance.getModelElementById(boundaryEventId);
    BaseElement scope = (BaseElement) boundaryEvent.getParentElement();

    UserTask compensationHandler = modelInstance.newInstance(UserTask.class);
    compensationHandler.setId(compensationHandlerId);
    compensationHandler.setForCompensation(true);
    scope.addChildElement(compensationHandler);

    Association association = modelInstance.newInstance(Association.class);
    association.setAssociationDirection(AssociationDirection.One);
    association.setSource(boundaryEvent);
    association.setTarget(compensationHandler);
    scope.addChildElement(association);

  }


}
