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

import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.CancelEventDefinition;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

/**
 * @author Thorben Lindhauer
 *
 */
public class TransactionModels {

  public static final BpmnModelInstance ONE_TASK_TRANSACTION = ProcessModels.newModel()
    .startEvent()
    .transaction("transaction")
      .embeddedSubProcess()
      .startEvent()
      .userTask("userTask")
      .endEvent("transactionEndEvent")
    .transactionDone()
    .endEvent()
    .done();

  public static final BpmnModelInstance CANCEL_BOUNDARY_EVENT = modify(ONE_TASK_TRANSACTION)
    .activityBuilder("transaction")
    .boundaryEvent("boundaryEvent")
    .userTask("afterBoundaryTask")
    .endEvent()
    .done();

  static {
    makeCancelEvent(CANCEL_BOUNDARY_EVENT, "transactionEndEvent");
    makeCancelEvent(CANCEL_BOUNDARY_EVENT, "boundaryEvent");
  }

  protected static void makeCancelEvent(BpmnModelInstance model, String eventId) {
    ModelElementInstance element = model.getModelElementById(eventId);

    CancelEventDefinition eventDefinition = model.newInstance(CancelEventDefinition.class);
    element.addChildElement(eventDefinition);
  }
}
