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
package org.camunda.bpm.engine.test.api.runtime.migration.util;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.test.api.runtime.migration.MigrationTestRule;
import org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

/**
 * @author Thorben Lindhauer
 *
 */
public class MessageEventFactory implements BpmnEventFactory {

  public static final String MESSAGE_NAME = "message";

  @Override
  public MigratingBpmnEventTrigger addBoundaryEvent(ProcessEngine engine, BpmnModelInstance modelInstance, String activityId, String boundaryEventId) {
    ModifiableBpmnModelInstance.wrap(modelInstance)
      .activityBuilder(activityId)
      .boundaryEvent(boundaryEventId)
        .message(MESSAGE_NAME)
      .done();

    MessageTrigger trigger = new MessageTrigger();
    trigger.engine = engine;
    trigger.messageName = MESSAGE_NAME;
    trigger.activityId = boundaryEventId;

    return trigger;
  }

  @Override
  public MigratingBpmnEventTrigger addEventSubProcess(ProcessEngine engine, BpmnModelInstance modelInstance, String parentId, String subProcessId, String startEventId) {
    ModifiableBpmnModelInstance.wrap(modelInstance)
      .addSubProcessTo(parentId)
      .id(subProcessId)
      .triggerByEvent()
      .embeddedSubProcess()
        .startEvent(startEventId).message(MESSAGE_NAME)
      .subProcessDone()
      .done();

    MessageTrigger trigger = new MessageTrigger();
    trigger.engine = engine;
    trigger.messageName = MESSAGE_NAME;
    trigger.activityId = startEventId;

    return trigger;
  }


  protected static class MessageTrigger implements MigratingBpmnEventTrigger {

    protected ProcessEngine engine;
    protected String activityId;
    protected String messageName;

    @Override
    public void trigger(String processInstanceId) {
      engine.getRuntimeService().createMessageCorrelation(messageName)
        .processInstanceId(processInstanceId)
        .correlateWithResult();
    }

    @Override
    public void assertEventTriggerMigrated(MigrationTestRule migrationContext, String targetActivityId) {
      migrationContext.assertEventSubscriptionMigrated(activityId, targetActivityId, messageName);
    }

    @Override
    public MigratingBpmnEventTrigger inContextOf(String newActivityId) {
      MessageTrigger newTrigger = new MessageTrigger();
      newTrigger.activityId = newActivityId;
      newTrigger.engine = engine;
      newTrigger.messageName = messageName;
      return newTrigger;
    }

  }

}
