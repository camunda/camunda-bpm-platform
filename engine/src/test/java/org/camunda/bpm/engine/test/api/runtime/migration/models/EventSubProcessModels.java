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

/**
 * @author Thorben Lindhauer
 *
 */
public class EventSubProcessModels {

  public static final String MESSAGE_NAME = "Message";
  public static final String SIGNAL_NAME = "Signal";
  public static final String VAR_CONDITION = "${any=='any'}";
  public static final String FALSE_CONDITION = "${false}";

  public static final String EVENT_SUB_PROCESS_TASK_ID = "eventSubProcessTask";
  public static final String EVENT_SUB_PROCESS_ID = "eventSubProcess";
  public static final String EVENT_SUB_PROCESS_START_ID = "eventSubProcessStart";
  public static final String SUB_PROCESS_ID = "subProcess";
  public static final String USER_TASK_ID = "userTask";

  public static final BpmnModelInstance CONDITIONAL_EVENT_SUBPROCESS_PROCESS = modify(ProcessModels.ONE_TASK_PROCESS)
    .addSubProcessTo(ProcessModels.PROCESS_KEY)
    .id(EVENT_SUB_PROCESS_ID)
    .triggerByEvent()
    .embeddedSubProcess()
      .startEvent(EVENT_SUB_PROCESS_START_ID)
      .condition(VAR_CONDITION)
      .userTask(EVENT_SUB_PROCESS_TASK_ID)
      .endEvent()
    .subProcessDone()
    .done();


  public static final BpmnModelInstance FALSE_CONDITIONAL_EVENT_SUBPROCESS_PROCESS = modify(ProcessModels.ONE_TASK_PROCESS)
    .addSubProcessTo(ProcessModels.PROCESS_KEY)
    .id(EVENT_SUB_PROCESS_ID)
    .triggerByEvent()
    .embeddedSubProcess()
      .startEvent(EVENT_SUB_PROCESS_START_ID)
      .condition(FALSE_CONDITION)
      .userTask(EVENT_SUB_PROCESS_TASK_ID)
      .endEvent()
    .subProcessDone()
    .done();


  public static final BpmnModelInstance MESSAGE_EVENT_SUBPROCESS_PROCESS = modify(ProcessModels.ONE_TASK_PROCESS)
      .addSubProcessTo(ProcessModels.PROCESS_KEY)
        .id(EVENT_SUB_PROCESS_ID)
        .triggerByEvent()
        .embeddedSubProcess()
          .startEvent(EVENT_SUB_PROCESS_START_ID).message(MESSAGE_NAME)
          .userTask(EVENT_SUB_PROCESS_TASK_ID)
          .endEvent()
      .subProcessDone()
      .done();

  public static final BpmnModelInstance MESSAGE_INTERMEDIATE_EVENT_SUBPROCESS_PROCESS = ProcessModels.newModel()
      .startEvent()
        .subProcess(EVENT_SUB_PROCESS_ID)
        .embeddedSubProcess()
          .startEvent()
          .intermediateCatchEvent("catchMessage")
            .message(MESSAGE_NAME)
          .userTask("userTask")
          .endEvent()
        .subProcessDone()
      .endEvent()
      .done();

  public static final BpmnModelInstance TIMER_EVENT_SUBPROCESS_PROCESS = modify(ProcessModels.ONE_TASK_PROCESS)
      .addSubProcessTo(ProcessModels.PROCESS_KEY)
      .id(EVENT_SUB_PROCESS_ID)
      .triggerByEvent()
      .embeddedSubProcess()
        .startEvent(EVENT_SUB_PROCESS_START_ID).timerWithDuration("PT10M")
        .userTask(EVENT_SUB_PROCESS_TASK_ID)
        .endEvent()
      .subProcessDone()
      .done();

  public static final BpmnModelInstance SIGNAL_EVENT_SUBPROCESS_PROCESS = modify(ProcessModels.ONE_TASK_PROCESS)
      .addSubProcessTo(ProcessModels.PROCESS_KEY)
      .id(EVENT_SUB_PROCESS_ID)
      .triggerByEvent()
      .embeddedSubProcess()
        .startEvent(EVENT_SUB_PROCESS_START_ID).signal(SIGNAL_NAME)
        .userTask(EVENT_SUB_PROCESS_TASK_ID)
        .endEvent()
      .subProcessDone()
      .done();

  public static final BpmnModelInstance ESCALATION_EVENT_SUBPROCESS_PROCESS = modify(ProcessModels.ONE_TASK_PROCESS)
      .addSubProcessTo(ProcessModels.PROCESS_KEY)
      .id(EVENT_SUB_PROCESS_ID)
      .triggerByEvent()
      .embeddedSubProcess()
        .startEvent(EVENT_SUB_PROCESS_START_ID).escalation()
        .userTask(EVENT_SUB_PROCESS_TASK_ID)
        .endEvent()
      .subProcessDone()
      .done();

  public static final BpmnModelInstance ERROR_EVENT_SUBPROCESS_PROCESS = modify(ProcessModels.ONE_TASK_PROCESS)
      .addSubProcessTo(ProcessModels.PROCESS_KEY)
      .id(EVENT_SUB_PROCESS_ID)
      .triggerByEvent()
      .embeddedSubProcess()
      .startEvent(EVENT_SUB_PROCESS_START_ID).error()
      .userTask(EVENT_SUB_PROCESS_TASK_ID)
      .endEvent()
      .subProcessDone()
      .done();

  public static final BpmnModelInstance COMPENSATE_EVENT_SUBPROCESS_PROCESS = modify(ProcessModels.SUBPROCESS_PROCESS)
      .addSubProcessTo(SUB_PROCESS_ID)
      .id(EVENT_SUB_PROCESS_ID)
      .triggerByEvent()
      .embeddedSubProcess()
        .startEvent(EVENT_SUB_PROCESS_START_ID).compensation()
        .userTask(EVENT_SUB_PROCESS_TASK_ID)
        .endEvent()
      .subProcessDone()
      .done();

  public static final BpmnModelInstance NESTED_EVENT_SUB_PROCESS_PROCESS = modify(ProcessModels.SUBPROCESS_PROCESS)
      .addSubProcessTo(SUB_PROCESS_ID)
      .id(EVENT_SUB_PROCESS_ID)
      .triggerByEvent()
      .embeddedSubProcess()
        .startEvent(EVENT_SUB_PROCESS_START_ID).message(MESSAGE_NAME)
        .userTask(EVENT_SUB_PROCESS_TASK_ID)
        .endEvent()
      .subProcessDone()
      .done();
}
