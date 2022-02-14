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
package org.camunda.bpm.client.util;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.ProcessBuilder;
import org.camunda.bpm.model.bpmn.instance.ExclusiveGateway;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;

public class ProcessModels {

  public static final String PROCESS_KEY = "process";
  public static final String PROCESS_KEY_2 = "process2";
  public static final String EXTERNAL_TASK_ID = "externalTask";
  public static final String EXTERNAL_TASK_ONE_ID = "externalTask1";
  public static final String EXTERNAL_TASK_TWO_ID = "externalTask2";
  public static final String USER_TASK_ID = "userTask1";
  public static final String USER_TASK_2_ID = "userTask2";
  public static final String USER_TASK_AFTER_BPMN_ERROR = "userTaskAfterBpmnError";
  public static final String EXTERNAL_TASK_TOPIC_FOO = "foo";
  public static final String EXTERNAL_TASK_TOPIC_BAR = "bar";
  public static final long EXTERNAL_TASK_PRIORITY = 4711L;
  public static final String PROCESS_DEFINITION_VERSION_TAG = "versionTag";

  public static ProcessBuilder newModel() {
    return newModel(PROCESS_KEY);
  }

  public static ProcessBuilder newModel(String processKey) {
    return Bpmn.createExecutableProcess(processKey);
  }

  public static BpmnModelInstance createProcessWithExclusiveGateway(String processKey, String condition) {
    BpmnModelInstance modelInstance = newModel(processKey)
        .startEvent()
        .serviceTask()
          .camundaExternalTask(EXTERNAL_TASK_TOPIC_FOO)
        .exclusiveGateway("gtw")
          .sequenceFlowId("flow1")
          .condition("cond", condition)
          .userTask(USER_TASK_ID)
          .endEvent()
        .moveToLastGateway()
          .sequenceFlowId("flow2")
          .userTask(USER_TASK_2_ID)
          .endEvent()
        .done();

    SequenceFlow sequenceFlow = (SequenceFlow) modelInstance.getModelElementById("flow2");
    ExclusiveGateway exclusiveGateway = (ExclusiveGateway) modelInstance.getModelElementById("gtw");
    exclusiveGateway.setDefault(sequenceFlow);

    return modelInstance;
  }

  public static final BpmnModelInstance TWO_EXTERNAL_TASK_PROCESS =
      newModel()
      .startEvent("startEvent")
      .serviceTask(EXTERNAL_TASK_ONE_ID)
        .camundaExternalTask(EXTERNAL_TASK_TOPIC_FOO)
      .serviceTask(EXTERNAL_TASK_TWO_ID)
        .camundaExternalTask(EXTERNAL_TASK_TOPIC_BAR)
      .endEvent("endEvent")
      .done();

  public static final BpmnModelInstance TWO_PRIORITISED_EXTERNAL_TASKS_PROCESS =
      newModel()
          .startEvent("startEvent")
            .parallelGateway("parallelGateway")
            .serviceTask(EXTERNAL_TASK_ONE_ID)
              .camundaExternalTask(EXTERNAL_TASK_TOPIC_FOO)
                .camundaTaskPriority(String.valueOf(EXTERNAL_TASK_PRIORITY))
            .endEvent("endEvent1")
            .moveToLastGateway()
            .serviceTask(EXTERNAL_TASK_TWO_ID)
              .camundaExternalTask(EXTERNAL_TASK_TOPIC_FOO)
                .camundaTaskPriority(String.valueOf(EXTERNAL_TASK_PRIORITY + 1000L))
            .endEvent("endEvent2")
            .done();

  public static final BpmnModelInstance ONE_EXTERNAL_TASK_WITH_OUTPUT_PARAM_PROCESS =
      newModel(PROCESS_KEY_2)
      .startEvent("startEvent")
      .serviceTask(EXTERNAL_TASK_ID)
        .camundaExternalTask(EXTERNAL_TASK_TOPIC_FOO)
        .camundaOutputParameter("bar", "${foo}")
      .userTask(USER_TASK_ID)
      .endEvent("endEvent")
      .done();

  public static final BpmnModelInstance ONE_EXTERNAL_TASK_WITH_VERSION_TAG =
      newModel(PROCESS_DEFINITION_VERSION_TAG)
      .camundaVersionTag(PROCESS_DEFINITION_VERSION_TAG)
      .startEvent()
      .serviceTask(EXTERNAL_TASK_ID)
        .camundaExternalTask(EXTERNAL_TASK_TOPIC_FOO)
      .endEvent()
      .done();

  public static final BpmnModelInstance BPMN_ERROR_EXTERNAL_TASK_PROCESS =
      newModel()
      .startEvent()
      .serviceTask(EXTERNAL_TASK_ID)
        .camundaExternalTask(EXTERNAL_TASK_TOPIC_FOO)
        .camundaTaskPriority(String.valueOf(EXTERNAL_TASK_PRIORITY))
      .userTask(USER_TASK_ID)
      .endEvent()
      .moveToActivity(EXTERNAL_TASK_ID)
      .boundaryEvent("catchBPMNError")
        .error("500")
      .userTask(USER_TASK_AFTER_BPMN_ERROR)
      .endEvent()
      .done();

  public static final BpmnModelInstance BPMN_ERROR_EXTERNAL_TASK_WITH_OUTPUT_MAPPING_PROCESS =
      newModel()
      .startEvent()
      .serviceTask(EXTERNAL_TASK_ID)
        .camundaExternalTask(EXTERNAL_TASK_TOPIC_FOO)
        .camundaTaskPriority(String.valueOf(EXTERNAL_TASK_PRIORITY))
        .camundaOutputParameter("bar", "${foo}")
        .camundaErrorEventDefinition().id("id").error("500", "errorMessage").expression("${true}").errorEventDefinitionDone()
      .userTask(USER_TASK_ID)
      .endEvent()
      .moveToActivity(EXTERNAL_TASK_ID)
      .boundaryEvent("catchBPMNError")
        .error("500").name("errorName")
      .userTask(USER_TASK_AFTER_BPMN_ERROR)
      .endEvent()
      .done();
}
