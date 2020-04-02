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
package org.camunda.bpm.model.bpmn;

import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.Gateway;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.camunda.bpm.model.xml.type.ModelElementType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * @author Sebastian Menski
 */
public class QueryTest {

  private static BpmnModelInstance modelInstance;
  private static Query<FlowNode> startSucceeding;
  private static Query<FlowNode> gateway1Succeeding;
  private static Query<FlowNode> gateway2Succeeding;

  @BeforeClass
  public static void createModelInstance() {
    modelInstance = Bpmn.createProcess()
      .startEvent().id("start")
      .userTask().id("user")
      .parallelGateway().id("gateway1")
        .serviceTask()
        .endEvent()
      .moveToLastGateway()
        .parallelGateway().id("gateway2")
          .userTask()
          .endEvent()
        .moveToLastGateway()
          .serviceTask()
          .endEvent()
        .moveToLastGateway()
          .scriptTask()
          .endEvent()
      .done();

    startSucceeding = ((FlowNode) modelInstance.getModelElementById("start")).getSucceedingNodes();
    gateway1Succeeding = ((FlowNode) modelInstance.getModelElementById("gateway1")).getSucceedingNodes();
    gateway2Succeeding = ((FlowNode) modelInstance.getModelElementById("gateway2")).getSucceedingNodes();

  }

  @AfterClass
  public static void validateModelInstance() {
    Bpmn.validateModel(modelInstance);
  }

  @Test
  public void testList() {
    assertThat(startSucceeding.list()).hasSize(1);
    assertThat(gateway1Succeeding.list()).hasSize(2);
    assertThat(gateway2Succeeding.list()).hasSize(3);
  }

  @Test
  public void testCount() {
    assertThat(startSucceeding.count()).isEqualTo(1);
    assertThat(gateway1Succeeding.count()).isEqualTo(2);
    assertThat(gateway2Succeeding.count()).isEqualTo(3);
  }

  @Test
  public void testFilterByType() {
    ModelElementType taskType = modelInstance.getModel().getType(Task.class);
    ModelElementType gatewayType = modelInstance.getModel().getType(Gateway.class);

    assertThat(startSucceeding.filterByType(taskType).list()).hasSize(1);
    assertThat(startSucceeding.filterByType(gatewayType).list()).hasSize(0);

    assertThat(gateway1Succeeding.filterByType(taskType).list()).hasSize(1);
    assertThat(gateway1Succeeding.filterByType(gatewayType).list()).hasSize(1);

    assertThat(gateway2Succeeding.filterByType(taskType).list()).hasSize(3);
    assertThat(gateway2Succeeding.filterByType(gatewayType).list()).hasSize(0);
  }

  @Test
  public void testSingleResult() {
    assertThat(startSucceeding.singleResult().getId()).isEqualTo("user");
    try {
      gateway1Succeeding.singleResult();
      fail("gateway1 has more than one succeeding flow node");
    }
    catch (Exception e) {
      assertThat(e).isInstanceOf(BpmnModelException.class).hasMessageEndingWith("<2>");
    }
    try {
      gateway2Succeeding.singleResult();
      fail("gateway2 has more than one succeeding flow node");
    }
    catch (Exception e) {
      assertThat(e).isInstanceOf(BpmnModelException.class).hasMessageEndingWith("<3>");
    }
  }
}
