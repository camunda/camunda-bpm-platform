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
public class GatewayModels {

  public static final BpmnModelInstance PARALLEL_GW = ProcessModels.newModel()
      .startEvent()
      .parallelGateway("fork")
      .userTask("parallel1")
      .parallelGateway("join")

      .moveToNode("fork")
      .userTask("parallel2")
      .connectTo("join")

      .userTask("afterJoin")
      .endEvent()
      .done();

  public static final BpmnModelInstance PARALLEL_GW_IN_SUBPROCESS = ProcessModels.newModel()
      .startEvent()

      .subProcess("subProcess")
      .embeddedSubProcess()
      .startEvent()

        .parallelGateway("fork")
        .userTask("parallel1")
        .parallelGateway("join")

        .moveToNode("fork")
        .userTask("parallel2")
        .connectTo("join")

        .userTask("afterJoin")
        .endEvent()

      .subProcessDone()
      .endEvent()
      .done();

  public static final BpmnModelInstance INCLUSIVE_GW = ProcessModels.newModel()
      .startEvent()
      .inclusiveGateway("fork")
      .userTask("parallel1")
      .inclusiveGateway("join")

      .moveToNode("fork")
      .userTask("parallel2")
      .connectTo("join")

      .userTask("afterJoin")
      .endEvent()
      .done();

  public static final BpmnModelInstance INCLUSIVE_GW_IN_SUBPROCESS = ProcessModels.newModel()
      .startEvent()

      .subProcess()
      .embeddedSubProcess()
      .startEvent()

        .inclusiveGateway("fork")
        .userTask("parallel1")
        .inclusiveGateway("join")

        .moveToNode("fork")
        .userTask("parallel2")
        .connectTo("join")

        .userTask("afterJoin")
        .endEvent()

      .subProcessDone()
      .endEvent()
      .done();
}
