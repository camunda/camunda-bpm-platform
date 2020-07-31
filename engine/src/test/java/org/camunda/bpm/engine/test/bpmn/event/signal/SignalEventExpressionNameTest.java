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
package org.camunda.bpm.engine.test.bpmn.event.signal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.HashMap;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ExecutionQuery;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.Test;

/**
 * @author Johannes Heinemann
 */
public class SignalEventExpressionNameTest extends PluggableProcessEngineTest {

  @Deployment
  @Test
  public void testSignalCatchIntermediate() {

    // given
    HashMap<String, Object> variables = new HashMap<String, Object>();
    variables.put("var", "TestVar");

    // when
    runtimeService.startProcessInstanceByKey("catchSignal", variables);

    // then
    assertEquals(1, runtimeService.createEventSubscriptionQuery().eventType("signal").eventName("alert-TestVar").count());
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventExpressionNameTest.testSignalCatchIntermediate.bpmn20.xml"})
  @Test
  public void testSignalCatchIntermediateActsOnEventReceive() {

    // given
    HashMap<String, Object> variables = new HashMap<String, Object>();
    variables.put("var", "TestVar");

    // when
    runtimeService.startProcessInstanceByKey("catchSignal", variables);
    runtimeService.signalEventReceived("alert-TestVar");

    // then
    assertEquals(0, runtimeService.createEventSubscriptionQuery().eventType("signal").eventName("alert-TestVar").count());
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventExpressionNameTest.testSignalCatchIntermediate.bpmn20.xml",
      "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventExpressionNameTest.testSignalThrowIntermediate.bpmn20.xml"})
  @Test
  public void testSignalThrowCatchIntermediate() {

    // given
    HashMap<String, Object> variables = new HashMap<String, Object>();
    variables.put("var", "TestVar");

    // when
    runtimeService.startProcessInstanceByKey("catchSignal", variables);
    assertEquals(1, runtimeService.createEventSubscriptionQuery().eventType("signal").eventName("alert-TestVar").count());
    runtimeService.startProcessInstanceByKey("throwSignal", variables);

    // then
    assertEquals(0, runtimeService.createEventSubscriptionQuery().eventType("signal").eventName("alert-${var}").count());
    assertEquals(0, runtimeService.createEventSubscriptionQuery().eventType("signal").eventName("alert-TestVar").count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventExpressionNameTest.testSignalCatchIntermediate.bpmn20.xml",
      "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventExpressionNameTest.testSignalThrowEnd.bpmn20.xml"})
  @Test
  public void testSignalThrowEndCatchIntermediate() {

    // given
    HashMap<String, Object> variables = new HashMap<String, Object>();
    variables.put("var", "TestVar");

    // when
    runtimeService.startProcessInstanceByKey("catchSignal", variables);
    assertEquals(1, runtimeService.createEventSubscriptionQuery().eventType("signal").eventName("alert-TestVar").count());
    runtimeService.startProcessInstanceByKey("throwEndSignal", variables);

    // then
    assertEquals(0, runtimeService.createEventSubscriptionQuery().eventType("signal").eventName("alert-${var}").count());
    assertEquals(0, runtimeService.createEventSubscriptionQuery().eventType("signal").eventName("alert-TestVar").count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }


  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventExpressionNameTest.testSignalCatchBoundary.bpmn20.xml",
      "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventExpressionNameTest.testSignalThrowIntermediate.bpmn20.xml"})
  @Test
  public void testSignalCatchBoundary() {

    // given
    HashMap<String, Object> variables = new HashMap<String, Object>();
    variables.put("var", "TestVar");
    runtimeService.startProcessInstanceByKey("catchSignal", variables);
    assertEquals(1, runtimeService.createEventSubscriptionQuery().eventType("signal").eventName("alert-TestVar").count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    // when
    runtimeService.startProcessInstanceByKey("throwSignal", variables);

    // then
    assertEquals(0, runtimeService.createEventSubscriptionQuery().eventType("signal").eventName("alert-TestVar").count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventExpressionNameTest.testSignalStartEvent.bpmn20.xml"})
  @Test
  public void testSignalStartEvent() {

    // given
    assertEquals(1, runtimeService.createEventSubscriptionQuery().eventType("signal").eventName("alert-foo").count());
    assertEquals(0, taskService.createTaskQuery().count());

    // when
    runtimeService.signalEventReceived("alert-foo");

    // then
    // the signal should start a new process instance
    assertEquals(1, taskService.createTaskQuery().count());
  }

  @Deployment
  @Test
  public void testSignalStartEventInEventSubProcess() {

    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("signalStartEventInEventSubProcess");
    // check if execution exists
    ExecutionQuery executionQuery = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId());
    assertEquals(1, executionQuery.count());
    // check if user task exists
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(processInstance.getId());
    assertEquals(1, taskQuery.count());

    // when
    runtimeService.signalEventReceived("alert-foo");

    // then
    assertEquals(true, DummyServiceTask.wasExecuted);
    // check if user task doesn't exist because signal start event is interrupting
    taskQuery = taskService.createTaskQuery().processInstanceId(processInstance.getId());
    assertEquals(0, taskQuery.count());
    // check if execution doesn't exist because signal start event is interrupting
    executionQuery = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId());
    assertEquals(0, executionQuery.count());
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventExpressionNameTest.testSignalStartEvent.bpmn20.xml",
      "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventExpressionNameTest.throwAlertSignalAsync.bpmn20.xml"})
  @Test
  public void testAsyncSignalStartEvent() {
    ProcessDefinition catchingProcessDefinition = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("startBySignal")
        .singleResult();

    // given a process instance that throws a signal asynchronously
    runtimeService.startProcessInstanceByKey("throwSignalAsync");
    // with an async job to trigger the signal event
    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);

    // when the job is executed
    managementService.executeJob(job.getId());

    // then there is a process instance
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertNotNull(processInstance);
    assertEquals(catchingProcessDefinition.getId(), processInstance.getProcessDefinitionId());

    // and a task
    assertEquals(1, taskService.createTaskQuery().count());
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventExpressionNameTest.testSignalCatchIntermediate.bpmn20.xml"})
  @Test
  public void testSignalExpressionErrorHandling() {

    String expectedErrorMessage = "Unknown property used in expression: alert-${var}. Cannot resolve identifier 'var'";

    // given an empty variable mapping
    HashMap<String, Object> variables = new HashMap<String, Object>();

    try {
      // when starting the process
      runtimeService.startProcessInstanceByKey("catchSignal", variables);

      fail("exception expected: " + expectedErrorMessage);
    } catch (ProcessEngineException e) {
      // then the expression cannot be resolved and no signal should be available
      assertEquals(0, runtimeService.createEventSubscriptionQuery().eventType("signal").count());
    }
  }

}
