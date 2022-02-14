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
package org.camunda.bpm.engine.test.bpmn.async;

import static org.assertj.core.api.Assertions.fail;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.describeActivityInstanceTree;
import static org.camunda.bpm.engine.test.util.ExecutionAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ExecutionAssert.describeExecutionTree;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.ExecutionTree;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.Assert;
import org.junit.Test;

public class AsyncStartEventTest extends PluggableProcessEngineTest {

  @Deployment
  @Test
  public void testAsyncStartEvent() {
    runtimeService.startProcessInstanceByKey("asyncStartEvent");

    Task task = taskService.createTaskQuery().singleResult();
    Assert.assertNull("The user task should not have been reached yet", task);

    Assert.assertEquals(1, runtimeService.createExecutionQuery().activityId("startEvent").count());

    testRule.executeAvailableJobs();
    task = taskService.createTaskQuery().singleResult();

    Assert.assertEquals(0, runtimeService.createExecutionQuery().activityId("startEvent").count());

    Assert.assertNotNull("The user task should have been reached", task);
  }

  @Deployment
  @Test
  public void testAsyncStartEventListeners() {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("asyncStartEvent");

    Assert.assertNull(runtimeService.getVariable(instance.getId(), "listener"));

    testRule.executeAvailableJobs();

    Assert.assertNotNull(runtimeService.getVariable(instance.getId(), "listener"));
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/async/AsyncStartEventTest.testAsyncStartEvent.bpmn20.xml")
  @Test
  public void testAsyncStartEventActivityInstance() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("asyncStartEvent");

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .transition("startEvent")
        .done());
  }

  @Deployment
  @Test
  public void testMultipleAsyncStartEvents() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("foo", "bar");
    runtimeService.correlateMessage("newInvoiceMessage", new HashMap<String, Object>(), variables);

    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    testRule.executeAvailableJobs();

    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    assertEquals("taskAfterMessageStartEvent", task.getTaskDefinitionKey());

    taskService.complete(task.getId());

    // assert process instance is ended
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/async/AsyncStartEventTest.testCallActivity-super.bpmn20.xml",
      "org/camunda/bpm/engine/test/bpmn/async/AsyncStartEventTest.testCallActivity-sub.bpmn20.xml"
  })
  @Test
  public void testCallActivity() {
    runtimeService.startProcessInstanceByKey("super");

    ProcessInstance pi = runtimeService
        .createProcessInstanceQuery()
        .processDefinitionKey("sub")
        .singleResult();

    assertTrue(pi instanceof ExecutionEntity);

    assertEquals("theSubStart", ((ExecutionEntity)pi).getActivityId());

  }

  @Deployment
  @Test
  public void testAsyncSubProcessStartEvent() {
    runtimeService.startProcessInstanceByKey("process");

    Task task = taskService.createTaskQuery().singleResult();
    assertNull("The subprocess user task should not have been reached yet", task);

    assertEquals(1, runtimeService.createExecutionQuery().activityId("StartEvent_2").count());

    testRule.executeAvailableJobs();
    task = taskService.createTaskQuery().singleResult();

    assertEquals(0, runtimeService.createExecutionQuery().activityId("StartEvent_2").count());
    assertNotNull("The subprocess user task should have been reached", task);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/async/AsyncStartEventTest.testAsyncSubProcessStartEvent.bpmn")
  @Test
  public void testAsyncSubProcessStartEventActivityInstance() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .beginScope("SubProcess_1")
            .transition("StartEvent_2")
        .done());
  }

  @Deployment
  @Test
  public void shouldRunAfterMessageStartInEventSubprocess() {
    // given
    // instance is waiting in async before on start event
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();
    Job job = managementService.createJobQuery().singleResult();

    // an event sub process is triggered before the job is executed
    runtimeService.createMessageCorrelation("start_sub")
      .processInstanceId(processInstanceId)
      .correlate();

    // when the job is executed
    managementService.executeJob(job.getId());

    // then
    // the user task after the async continuation is reached successfully
    Task task = taskService.createTaskQuery().singleResult();

    assertEquals(0, runtimeService.createExecutionQuery().activityId("StartEvent_1").count());
    assertNotNull("The user task should have been reached", task);

    // and the event sub process is still active
    String processDefinitionId = repositoryService.createProcessDefinitionQuery().singleResult().getId();

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);
    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
        .child("user-task").concurrent().noScope().up()
        .child(null).concurrent().noScope()
          .child(null).scope()
            .child("external-task").scope()
      .done());

    ActivityInstance activityInstanceTree = runtimeService.getActivityInstance(processInstanceId);
    assertThat(activityInstanceTree).hasStructure(
        describeActivityInstanceTree(processDefinitionId)
          .activity("user-task")
          .beginScope("sub-process")
            .activity("external-task")
          .done());
  }
}
