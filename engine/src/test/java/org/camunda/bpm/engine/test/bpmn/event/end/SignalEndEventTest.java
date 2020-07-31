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
package org.camunda.bpm.engine.test.bpmn.event.end;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.Test;

/**
 * @author Kristin Polenz
 */
public class SignalEndEventTest extends PluggableProcessEngineTest {

  @Deployment
  @Test
  public void testCatchSignalEndEventInEmbeddedSubprocess() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("catchSignalEndEventInEmbeddedSubprocess");
    assertNotNull(processInstance);

    // After process start, usertask in subprocess should exist
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("subprocessTask", task.getName());

    // After task completion, signal end event is reached and caught
    taskService.complete(task.getId());

    task = taskService.createTaskQuery().singleResult();
    assertEquals("task after catching the signal", task.getName());

    taskService.complete(task.getId());
    testRule.assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/bpmn/event/end/SignalEndEventTest.catchSignalEndEvent.bpmn20.xml",
      "org/camunda/bpm/engine/test/bpmn/event/end/SignalEndEventTest.processWithSignalEndEvent.bpmn20.xml"
    })
  @Test
  public void testCatchSignalEndEventInCallActivity() throws Exception {
    // first, start process to wait of the signal event
    ProcessInstance processInstanceCatchEvent = runtimeService.startProcessInstanceByKey("catchSignalEndEvent");
    assertNotNull(processInstanceCatchEvent);

    // now we have a subscription for the signal event:
    assertEquals(1, runtimeService.createEventSubscriptionQuery().count());
    assertEquals("alert", runtimeService.createEventSubscriptionQuery().singleResult().getEventName());

    // start process which throw the signal end event
    ProcessInstance processInstanceEndEvent = runtimeService.startProcessInstanceByKey("processWithSignalEndEvent");
    assertNotNull(processInstanceEndEvent);
    testRule.assertProcessEnded(processInstanceEndEvent.getId());

    // user task of process catchSignalEndEvent
    assertEquals(1, taskService.createTaskQuery().count());
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("taskAfterSignalCatch", task.getTaskDefinitionKey());

    // complete user task
    taskService.complete(task.getId());

    testRule.assertProcessEnded(processInstanceCatchEvent.getId());
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/event/signal/testPropagateOutputVariablesWhileThrowSignal.bpmn20.xml",
                            "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEndEventTest.parent.bpmn20.xml" })
  @Test
  public void testPropagateOutputVariablesWhileThrowSignal() {
    // given
    Map<String,Object> variables = new HashMap<String, Object>();
    variables.put("input", 42);
    String processInstanceId = runtimeService.startProcessInstanceByKey("SignalParentProcess", variables).getId();

    // when
    String id = taskService.createTaskQuery().taskName("ut2").singleResult().getId();
    taskService.complete(id);

    // then
    checkOutput(processInstanceId);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/event/signal/testPropagateOutputVariablesWhileThrowSignal2.bpmn20.xml",
                            "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEndEventTest.parent.bpmn20.xml" })
  @Test
  public void testPropagateOutputVariablesWhileThrowSignal2() {
    // given
    Map<String,Object> variables = new HashMap<String, Object>();
    variables.put("input", 42);
    String processInstanceId = runtimeService.startProcessInstanceByKey("SignalParentProcess", variables).getId();

    // when
    String id = taskService.createTaskQuery().taskName("inside subprocess").singleResult().getId();
    taskService.complete(id);

    // then
    checkOutput(processInstanceId);
  }

  protected void checkOutput(String processInstanceId) {
    assertEquals(1, taskService.createTaskQuery().taskName("task after catched signal").count());
    // and set the output variable of the called process to the process
    assertNotNull(runtimeService.getVariable(processInstanceId, "cancelReason"));
    assertEquals(42, runtimeService.getVariable(processInstanceId, "input"));
  }
}
