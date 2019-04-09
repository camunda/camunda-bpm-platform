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

import org.camunda.bpm.model.bpmn.BpmnModelInstance;

/**
 * @author Thorben Lindhauer
 *
 */
public class MessageReceiveModels {

  public static final String MESSAGE_NAME = "Message";

  public static final BpmnModelInstance ONE_RECEIVE_TASK_PROCESS = ProcessModels.newModel()
    .startEvent()
    .receiveTask("receiveTask")
      .message(MESSAGE_NAME)
    .userTask("userTask")
    .endEvent()
    .done();

  public static final BpmnModelInstance SUBPROCESS_RECEIVE_TASK_PROCESS = ProcessModels.newModel()
      .startEvent()
      .subProcess()
      .embeddedSubProcess()
        .startEvent()
        .receiveTask("receiveTask")
          .message(MESSAGE_NAME)
        .userTask("userTask")
        .endEvent()
      .subProcessDone()
      .endEvent()
      .done();

  public static final BpmnModelInstance ONE_MESSAGE_CATCH_PROCESS = ProcessModels.newModel()
    .startEvent()
    .intermediateCatchEvent("messageCatch")
      .message(MESSAGE_NAME)
    .userTask("userTask")
    .endEvent()
    .done();

  public static final BpmnModelInstance MESSAGE_START_PROCESS = ProcessModels.newModel()
    .startEvent("startEvent")
      .message(MESSAGE_NAME)
    .userTask("userTask")
    .endEvent()
    .done();
}
