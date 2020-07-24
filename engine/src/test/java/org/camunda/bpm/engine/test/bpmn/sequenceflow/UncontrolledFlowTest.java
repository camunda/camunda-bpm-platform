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
package org.camunda.bpm.engine.test.bpmn.sequenceflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.Test;

/**
 * Tests things that BPMN describes as 'uncontrolled flow':
 * Activities with more than one incoming sequence flow or with more than one
 * outgoing flow.
 *
 * @author Thorben Lindhauer
 */
public class UncontrolledFlowTest extends PluggableProcessEngineTest {

  @Deployment
  @Test
  public void testSubProcessTwoOutgoingFlowsCorrelateMessage() {
    // given a process instance
    runtimeService.startProcessInstanceByKey("process");

    // that leaves the sub process via two outgoing sequence flows
    Task innerTask = taskService.createTaskQuery().singleResult();
    taskService.complete(innerTask.getId());

    // then there are two tasks after the sub process
    assertEquals(2, taskService.createTaskQuery().count());
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey("outerTask1").count());
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey("outerTask2").count());

    // and then the message for the event subprocess cannot be delivered
    try {
      runtimeService.correlateMessage("Message1");
      fail("should not succeed");
    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("Cannot correlate message 'Message1'", e.getMessage());
    }
  }

  @Deployment
  @Test
  public void testSubProcessTwoOutgoingFlowsEndProcess() {
    // given a process instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    Task innerTask = taskService.createTaskQuery().singleResult();

    // when the subprocess completes and is left via two outgoing sequence
    // flows that point to end events
    taskService.complete(innerTask.getId());

    // then the process instance is finished
    testRule.assertProcessEnded(processInstance.getId());
  }
}
