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

import static org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;

/**
 * @author Thorben Lindhauer
 *
 */
public class EventSubProcessModels {

  public static final String MESSAGE_NAME = "Message";
  public static final String SIGNAL_NAME = "Signal";

  public static final BpmnModelInstance MESSAGE_EVENT_SUBPROCESS_PROCESS = modify(ProcessModels.ONE_TASK_PROCESS)
      .addSubProcessTo(ProcessModels.PROCESS_KEY)
      .id("eventSubProcess")
      .triggerByEvent()
      .embeddedSubProcess()
      .startEvent("eventSubProcessStart").message(MESSAGE_NAME)
      .userTask("eventSubProcessTask")
      .endEvent()
      .subProcessDone()
      .done();

  public static final BpmnModelInstance TIMER_EVENT_SUBPROCESS_PROCESS = modify(ProcessModels.ONE_TASK_PROCESS)
      .addSubProcessTo(ProcessModels.PROCESS_KEY)
      .id("eventSubProcess")
      .triggerByEvent()
      .embeddedSubProcess()
      .startEvent("eventSubProcessStart").timerWithDuration("PT10M")
      .userTask("eventSubProcessTask")
      .endEvent()
      .subProcessDone()
      .done();

  public static final BpmnModelInstance SIGNAL_EVENT_SUBPROCESS_PROCESS = modify(ProcessModels.ONE_TASK_PROCESS)
      .addSubProcessTo(ProcessModels.PROCESS_KEY)
      .id("eventSubProcess")
      .triggerByEvent()
      .embeddedSubProcess()
      .startEvent("eventSubProcessStart").signal(SIGNAL_NAME)
      .userTask("eventSubProcessTask")
      .endEvent()
      .subProcessDone()
      .done();

  public static final BpmnModelInstance ESCALATION_EVENT_SUBPROCESS_PROCESS = modify(ProcessModels.ONE_TASK_PROCESS)
      .addSubProcessTo(ProcessModels.PROCESS_KEY)
      .id("eventSubProcess")
      .triggerByEvent()
      .embeddedSubProcess()
      .startEvent("eventSubProcessStart").escalation()
      .userTask("eventSubProcessTask")
      .endEvent()
      .subProcessDone()
      .done();

  public static final BpmnModelInstance ERROR_EVENT_SUBPROCESS_PROCESS = modify(ProcessModels.ONE_TASK_PROCESS)
      .addSubProcessTo(ProcessModels.PROCESS_KEY)
      .id("eventSubProcess")
      .triggerByEvent()
      .embeddedSubProcess()
      .startEvent("eventSubProcessStart").error()
      .userTask("eventSubProcessTask")
      .endEvent()
      .subProcessDone()
      .done();

  public static final BpmnModelInstance COMPENSATE_EVENT_SUBPROCESS_PROCESS = modify(ProcessModels.SUBPROCESS_PROCESS)
      .addSubProcessTo("subProcess")
      .id("eventSubProcess")
      .triggerByEvent()
      .embeddedSubProcess()
      .startEvent("eventSubProcessStart").compensation()
      .userTask("eventSubProcessTask")
      .endEvent()
      .subProcessDone()
      .done();

  public static final BpmnModelInstance NESTED_EVENT_SUB_PROCESS_PROCESS = modify(ProcessModels.SUBPROCESS_PROCESS)
      .addSubProcessTo("subProcess")
      .id("eventSubProcess")
      .triggerByEvent()
      .embeddedSubProcess()
      .startEvent("eventSubProcessStart").message(MESSAGE_NAME)
      .userTask("eventSubProcessTask")
      .endEvent()
      .subProcessDone()
      .done();
}
