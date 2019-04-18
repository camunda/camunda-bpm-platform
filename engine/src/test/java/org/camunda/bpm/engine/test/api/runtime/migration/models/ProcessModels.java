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

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.ProcessBuilder;

/**
 * @author Thorben Lindhauer
 *
 */
public class ProcessModels {

  public static final String PROCESS_KEY = "Process";

  public static ProcessBuilder newModel() {
    return newModel(PROCESS_KEY);
  }

  public static ProcessBuilder newModel(String processKey) {
    return Bpmn.createExecutableProcess(processKey);
  }

  public static ProcessBuilder newModel(int processNumber) {
    return newModel(PROCESS_KEY + processNumber);
  }

  public static final String USER_TASK_ID = "userTask";
  public static final BpmnModelInstance ONE_TASK_PROCESS =
      newModel()
      .startEvent("startEvent")
      .userTask(USER_TASK_ID).name("User Task")
      .endEvent("endEvent")
      .done();

  public static final BpmnModelInstance ONE_TASK_PROCESS_WITH_DOCUMENTATION =
      modify(ONE_TASK_PROCESS)
          .addDocumentation("This is a documentation!");

  public static final BpmnModelInstance TWO_TASKS_PROCESS =
      newModel()
      .startEvent("startEvent")
      .userTask("userTask1")
      .sequenceFlowId("flow1")
      .userTask("userTask2")
      .endEvent("endEvent")
      .done();

  public static final BpmnModelInstance SUBPROCESS_PROCESS =
    newModel()
      .startEvent()
      .subProcess("subProcess")
        .embeddedSubProcess()
          .startEvent("subProcessStart")
          .userTask(USER_TASK_ID).name("User Task")
          .endEvent("subProcessEnd")
        .subProcessDone()
      .endEvent()
      .done();

  public static final BpmnModelInstance DOUBLE_SUBPROCESS_PROCESS =
    newModel()
      .startEvent()
      .subProcess("outerSubProcess")
       .embeddedSubProcess()
         .startEvent()
         .subProcess("innerSubProcess")
           .embeddedSubProcess()
             .startEvent()
             .userTask(USER_TASK_ID).name("User Task")
             .endEvent()
           .subProcessDone()
           .endEvent()
         .subProcessDone()
      .endEvent()
      .done();

  public static final BpmnModelInstance DOUBLE_PARALLEL_SUBPROCESS_PROCESS =
      newModel()
        .startEvent()
        .subProcess("outerSubProcess")
         .embeddedSubProcess()
           .startEvent()
           .parallelGateway("fork")
           .subProcess("innerSubProcess1")
             .embeddedSubProcess()
               .startEvent()
               .userTask("userTask1").name("User Task 1")
               .endEvent()
           .subProcessDone()
           .endEvent()
           .moveToLastGateway()
           .subProcess("innerSubProcess2")
             .embeddedSubProcess()
               .startEvent()
               .userTask("userTask2").name("User Task 2")
               .endEvent()
           .subProcessDone()
           .endEvent()
       .subProcessDone()
       .endEvent()
       .done();

  public static final BpmnModelInstance TRIPLE_SUBPROCESS_PROCESS =
      newModel()
        .startEvent()
        .subProcess("subProcess1")
         .embeddedSubProcess()
           .startEvent()
           .subProcess("subProcess2")
             .embeddedSubProcess()
               .startEvent()
               .subProcess("subProcess3")
                 .embeddedSubProcess()
                   .startEvent()
                   .userTask(USER_TASK_ID).name("User Task")
                   .endEvent()
               .subProcessDone()
               .endEvent()
             .subProcessDone()
             .endEvent()
           .subProcessDone()
        .endEvent()
        .done();

  public static final BpmnModelInstance ONE_RECEIVE_TASK_PROCESS =
    newModel()
      .startEvent()
      .receiveTask("receiveTask")
        .message("Message")
      .endEvent()
      .done();

  public static final BpmnModelInstance PARALLEL_GATEWAY_PROCESS =
    newModel()
      .startEvent()
      .parallelGateway("fork")
      .userTask("userTask1").name("User Task 1")
      .endEvent()
      .moveToLastGateway()
      .userTask("userTask2").name("User Task 2")
      .endEvent()
      .done();

  public static final BpmnModelInstance PARALLEL_SUBPROCESS_PROCESS =
      newModel()
        .startEvent()
        .parallelGateway("fork")
        .subProcess("subProcess1")
          .embeddedSubProcess()
            .startEvent()
            .userTask("userTask1")
            .endEvent()
          .subProcessDone()
        .endEvent()
        .moveToLastGateway()
        .subProcess("subProcess2")
          .embeddedSubProcess()
            .startEvent()
            .userTask("userTask2")
            .endEvent()
          .subProcessDone()
        .endEvent()
        .done();

  public static final BpmnModelInstance PARALLEL_DOUBLE_SUBPROCESS_PROCESS =
    newModel()
      .startEvent()
      .parallelGateway("fork")
      .subProcess("subProcess1")
        .embeddedSubProcess()
          .startEvent()
          .subProcess("nestedSubProcess1")
            .embeddedSubProcess()
              .startEvent()
              .userTask("userTask1")
              .endEvent()
            .subProcessDone()
        .endEvent()
      .subProcessDone()
    .endEvent()
    .moveToLastGateway()
    .subProcess("subProcess2")
      .embeddedSubProcess()
        .startEvent()
          .subProcess("nestedSubProcess2")
            .embeddedSubProcess()
              .startEvent()
              .userTask("userTask2")
              .endEvent()
            .subProcessDone()
        .endEvent()
      .subProcessDone()
    .endEvent()
    .done();

  public static final BpmnModelInstance PARALLEL_TASK_AND_SUBPROCESS_PROCESS =
      newModel()
        .startEvent()
        .parallelGateway("fork")
        .subProcess("subProcess")
          .embeddedSubProcess()
            .startEvent()
            .userTask("userTask1")
            .endEvent()
          .subProcessDone()
        .endEvent()
        .moveToLastGateway()
        .userTask("userTask2")
        .endEvent()
        .done();

  public static final BpmnModelInstance PARALLEL_GATEWAY_SUBPROCESS_PROCESS =
    newModel()
      .startEvent()
      .subProcess("subProcess")
        .embeddedSubProcess()
          .startEvent()
          .parallelGateway("fork")
          .userTask("userTask1").name("User Task 1")
          .endEvent()
          .moveToLastGateway()
          .userTask("userTask2").name("User Task 2")
        .subProcessDone()
      .endEvent()
      .done();

  public static final BpmnModelInstance SCOPE_TASK_PROCESS = modify(ONE_TASK_PROCESS)
    .activityBuilder(USER_TASK_ID)
    .camundaInputParameter("foo", "bar")
    .done();

  public static final BpmnModelInstance SCOPE_TASK_SUBPROCESS_PROCESS = modify(SUBPROCESS_PROCESS)
    .activityBuilder(USER_TASK_ID)
    .camundaInputParameter("foo", "bar")
    .done();

  public static final BpmnModelInstance PARALLEL_SCOPE_TASKS = modify(PARALLEL_GATEWAY_PROCESS)
    .activityBuilder("userTask1")
    .camundaInputParameter("foo", "bar")
    .moveToActivity("userTask2")
    .camundaInputParameter("foo", "bar")
    .done();

  public static final BpmnModelInstance PARALLEL_SCOPE_TASKS_SUB_PROCESS = modify(PARALLEL_GATEWAY_SUBPROCESS_PROCESS)
    .activityBuilder("userTask1")
    .camundaInputParameter("foo", "bar")
    .moveToActivity("userTask2")
    .camundaInputParameter("foo", "bar")
    .done();

  public static final BpmnModelInstance UNSUPPORTED_ACTIVITIES = Bpmn.createExecutableProcess(PROCESS_KEY)
    .startEvent("startEvent")
    .businessRuleTask("decisionTask")
      .camundaDecisionRef("testDecision")
    .intermediateThrowEvent("throwEvent")
      .message("Message")
    .serviceTask("serviceTask")
      .camundaExpression("${true}")
    .sendTask("sendTask")
      .camundaExpression("${true}")
    .scriptTask("scriptTask")
      .scriptText("foo")
    .endEvent("endEvent")
    .done();

  public static BpmnModelInstance oneTaskProcess(int processNumber) {
    return newModel(processNumber)
        .startEvent()
        .userTask(USER_TASK_ID)
        .endEvent()
        .done();
  }

}
