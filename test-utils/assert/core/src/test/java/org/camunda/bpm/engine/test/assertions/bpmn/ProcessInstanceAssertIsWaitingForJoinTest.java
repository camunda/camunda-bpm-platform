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
package org.camunda.bpm.engine.test.assertions.bpmn;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.complete;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.task;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.assertions.helpers.Failure;
import org.camunda.bpm.engine.test.assertions.helpers.ProcessAssertTestCase;
import org.junit.Rule;
import org.junit.Test;

public class ProcessInstanceAssertIsWaitingForJoinTest extends ProcessAssertTestCase {

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  @Test
  @Deployment(resources = "bpmn/ProcessInstanceAssert-isWaitingForJoinAt.bpmn")
  public void testIsWaitingForJoinAt() {
    // given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-isWaitingForJoinAt"
    );
    assertThat(processInstance).isWaitingAt("UserTask1", "UserTask2");

    // when
    complete(task("UserTask1"));

    // Then
    assertThat(processInstance).isWaitingAt("UserTask2", "JoinGateway");
  }

  @Test
  @Deployment(resources = "bpmn/ProcessInstanceAssert-isWaitingForJoinAt.bpmn")
  public void testIsNotWaitingForJoin() {
    // when
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-isWaitingForJoinAt"
    );

    // then
    assertThat(processInstance).isNotWaitingAt("JoinGateway");
  }

  @Test
  @Deployment(resources = "bpmn/ProcessInstanceAssert-isWaitingForJoinAt2.bpmn")
  public void testNestedJoinGateways() {
    // given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-isWaitingForJoinAt2"
    );
    assertThat(processInstance).isWaitingAt("Task1", "Task2", "Task3", "Task4");
    complete(task("Task1"));

    // when
    complete(task("Task3"));

    // then
    assertThat(processInstance).isWaitingAt("JoinGateway1");
    assertThat(processInstance).isWaitingAt("JoinGateway2");

    // and when
    complete(task("Task2"));

    // then
    assertThat(processInstance).isWaitingAt("JoinGateway2");
    assertThat(processInstance).isWaitingAt("JoinGateway3");
  }

  @Test
  @Deployment(resources = "bpmn/ProcessInstanceAssert-isWaitingForJoinAt3.bpmn")
  public void testNestedInclusiveGatewaysAll() {
    // given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-isWaitingForJoinAt3"
    );
    assertThat(processInstance).isWaitingAt("Task1", "Task2", "Task3");

    // when
    complete(task("Task1"));

    // then
    assertThat(processInstance).isWaitingAt("JoinInclusiveGateway1");

    // and when
    complete(task("Task3"));

    // then
    assertThat(processInstance).isWaitingAt("JoinInclusiveGateway2");
  }

  @Test
  @Deployment(resources = "bpmn/ProcessInstanceAssert-isWaitingForJoinAt.bpmn")
  public void testIsNotWaitingForJoinFails() {
    // given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-isWaitingForJoinAt"
    );
    assertThat(processInstance).isWaitingAt("UserTask1", "UserTask2");
    complete(task("UserTask1"));

    // then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).isNotWaitingAt("JoinGateway");
      }
    }, "NOT to be waiting at [");
  }

  @Test
  @Deployment(resources = "bpmn/ProcessInstanceAssert-isWaitingForJoinAt.bpmn")
  public void testIsWaitingForJoinAtNull() {
    // given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-isWaitingForJoinAt");

    // then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).isWaitingAt(null);
      }
    }, "Expecting list of activityIds not to be null, not to be empty");
  }

  @Test
  @Deployment(resources = "bpmn/ProcessInstanceAssert-isWaitingForJoinAt.bpmn")
  public void testIsWaitingForJoinAtWrongActivityId() {
    // given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-isWaitingForJoinAt"
    );
    assertThat(processInstance).isWaitingAt("UserTask1", "UserTask2");
    complete(task("UserTask1"));

    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).isWaitingAt("WrongGateway");
      }
    }, "is actually waiting at [");

  }

  @Test
  @Deployment(resources = "bpmn/ProcessInstanceAssert-isWaitingForJoinAt.bpmn")
  public void testProcessWithJoinInCompletedProcessInstance() {
    // given
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-isWaitingForJoinAt"
    );
    assertThat(processInstance).isWaitingAt("UserTask1", "UserTask2");
    complete(task("UserTask1"));
    complete(task("UserTask2"));

    // Then
    expect(new Failure() {
      @Override
      public void when() {
        assertThat(processInstance).isWaitingAt("JoinGateway");
      }
    }, "already finished");
  }
}
