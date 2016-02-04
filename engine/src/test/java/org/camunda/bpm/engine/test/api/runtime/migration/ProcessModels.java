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
package org.camunda.bpm.engine.test.api.runtime.migration;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.ProcessBuilder;
import org.camunda.bpm.model.bpmn.builder.UserTaskBuilder;
import org.camunda.bpm.model.bpmn.instance.UserTask;

/**
 * @author Thorben Lindhauer
 *
 */
public class ProcessModels {

  public static final String PROCESS_KEY = "Process";


  protected static ProcessBuilder newModel() {
    return Bpmn.createExecutableProcess(PROCESS_KEY);
  }

  public static final BpmnModelInstance ONE_TASK_PROCESS =
      newModel()
      .startEvent()
      .userTask("userTask").name("User Task")
      .endEvent()
      .done();

  public static final BpmnModelInstance SUBPROCESS_PROCESS =
    newModel()
      .startEvent()
      .subProcess("subProcess")
        .embeddedSubProcess()
          .startEvent()
          .userTask("userTask").name("User Task")
          .endEvent()
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
             .userTask("userTask").name("User Task")
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
                   .userTask("userTask").name("User Task")
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
      .parallelGateway()
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

  public static final BpmnModelInstance SCOPE_TASK_PROCESS = ONE_TASK_PROCESS.clone()
    .<UserTask>getModelElementById("userTask").builder()
      .camundaInputParameter("foo", "bar")
    .done();

  public static final BpmnModelInstance SCOPE_TASK_SUBPROCESS_PROCESS = SUBPROCESS_PROCESS.clone()
    .<UserTask>getModelElementById("userTask").builder()
      .camundaInputParameter("foo", "bar")
    .done();

  public static final BpmnModelInstance PARALLEL_SCOPE_TASKS = PARALLEL_GATEWAY_PROCESS.clone()
    .<UserTask>getModelElementById("userTask1").builder()
      .camundaInputParameter("foo", "bar")
    .<UserTaskBuilder>moveToActivity("userTask2")
      .camundaInputParameter("foo", "bar")
    .done();

  public static final BpmnModelInstance PARALLEL_SCOPE_TASKS_SUB_PROCESS = PARALLEL_GATEWAY_SUBPROCESS_PROCESS.clone()
    .<UserTask>getModelElementById("userTask1").builder()
      .camundaInputParameter("foo", "bar")
    .<UserTaskBuilder>moveToActivity("userTask2")
      .camundaInputParameter("foo", "bar")
    .done();


  public static final BpmnModelInstance UNSUPPORTED_ACTIVITIES = Bpmn.createExecutableProcess(PROCESS_KEY)
    .startEvent()
    .businessRuleTask("decisionTask")
      .camundaDecisionRef("testDecision")
    .intermediateCatchEvent("catch")
      .message("Message")
    .intermediateThrowEvent("throw")
      .message("Message")
    .endEvent()
    .done();

}
