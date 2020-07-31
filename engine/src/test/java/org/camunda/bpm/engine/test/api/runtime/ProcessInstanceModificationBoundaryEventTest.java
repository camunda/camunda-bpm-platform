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
package org.camunda.bpm.engine.test.api.runtime;

import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.describeActivityInstanceTree;
import static org.camunda.bpm.engine.test.util.ExecutionAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ExecutionAssert.describeExecutionTree;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.ExecutionTree;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class ProcessInstanceModificationBoundaryEventTest extends PluggableProcessEngineTest {

  protected static final String INTERRUPTING_BOUNDARY_EVENT = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.interruptingBoundaryEvent.bpmn20.xml";
  protected static final String NON_INTERRUPTING_BOUNDARY_EVENT = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.nonInterruptingBoundaryEvent.bpmn20.xml";

  protected static final String INTERRUPTING_BOUNDARY_EVENT_INSIDE_SUBPROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.interruptingBoundaryEventInsideSubProcess.bpmn20.xml";
  protected static final String NON_INTERRUPTING_BOUNDARY_EVENT_INSIDE_SUBPROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.nonInterruptingBoundaryEventInsideSubProcess.bpmn20.xml";

  protected static final String INTERRUPTING_BOUNDARY_EVENT_ON_SUBPROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.interruptingBoundaryEventOnSubProcess.bpmn20.xml";
  protected static final String NON_INTERRUPTING_BOUNDARY_EVENT_ON_SUBPROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.nonInterruptingBoundaryEventOnSubProcess.bpmn20.xml";

  protected static final String INTERRUPTING_BOUNDARY_EVENT_WITH_PARALLEL_GATEWAY = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.interruptingBoundaryEventWithParallelGateway.bpmn20.xml";
  protected static final String NON_INTERRUPTING_BOUNDARY_EVENT_WITH_PARALLEL_GATEWAY = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.nonInterruptingBoundaryEventWithParallelGateway.bpmn20.xml";

  protected static final String INTERRUPTING_BOUNDARY_EVENT_WITH_PARALLEL_GATEWAY_INSIDE_SUB_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.interruptingBoundaryEventWithParallelGatewayInsideSubProcess.bpmn20.xml";
  protected static final String NON_INTERRUPTING_BOUNDARY_EVENT_WITH_PARALLEL_GATEWAY_INSIDE_SUB_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.nonInterruptingBoundaryEventWithParallelGatewayInsideSubProcess.bpmn20.xml";

  @Deployment(resources = INTERRUPTING_BOUNDARY_EVENT)
  @Test
  public void testTask1AndStartBeforeTaskAfterBoundaryEvent() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("taskAfterBoundaryEvent")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("task1")
        .activity("taskAfterBoundaryEvent")
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child("taskAfterBoundaryEvent").concurrent().noScope().up()
          .child(null).concurrent().noScope()
            .child("task1").scope()
        .done());

    completeTasksInOrder("task1", "task2", "taskAfterBoundaryEvent");
    testRule.assertProcessEnded(processInstanceId);

  }

  @Deployment(resources = INTERRUPTING_BOUNDARY_EVENT)
  @Test
  public void testTask1AndStartBeforeBoundaryEvent() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("boundaryEvent")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .activity("taskAfterBoundaryEvent")
        .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree("taskAfterBoundaryEvent").scope()
      .done());

    completeTasksInOrder("taskAfterBoundaryEvent");
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = INTERRUPTING_BOUNDARY_EVENT)
  @Test
  public void testTask2AndStartBeforeTaskAfterBoundaryEvent() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("taskAfterBoundaryEvent")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .activity("task2")
          .activity("taskAfterBoundaryEvent")
        .done());


    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child("taskAfterBoundaryEvent").concurrent().noScope().up()
          .child("task2").concurrent().noScope()
        .done());

    completeTasksInOrder("task2", "taskAfterBoundaryEvent");
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = INTERRUPTING_BOUNDARY_EVENT)
  @Test
  public void testTask2AndStartBeforeBoundaryEvent() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("boundaryEvent")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .activity("task2")
          .activity("taskAfterBoundaryEvent")
        .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child("taskAfterBoundaryEvent").concurrent().noScope().up()
          .child("task2").concurrent().noScope()
        .done());

    completeTasksInOrder("task2", "taskAfterBoundaryEvent");
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = NON_INTERRUPTING_BOUNDARY_EVENT)
  @Test
  public void testTask1AndStartBeforeTaskAfterNonInterruptingBoundaryEvent() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("taskAfterBoundaryEvent")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .activity("task1")
          .activity("taskAfterBoundaryEvent")
        .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child("taskAfterBoundaryEvent").concurrent().noScope().up()
          .child(null).concurrent().noScope()
            .child("task1").scope()
        .done());

    completeTasksInOrder("task1", "taskAfterBoundaryEvent", "task2");
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = NON_INTERRUPTING_BOUNDARY_EVENT)
  @Test
  public void testTask1AndStartBeforeNonInterruptingBoundaryEvent() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("boundaryEvent")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .activity("task1")
          .activity("taskAfterBoundaryEvent")
        .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child("taskAfterBoundaryEvent").concurrent().noScope().up()
          .child(null).concurrent().noScope()
            .child("task1").scope()
        .done());

    completeTasksInOrder("task1", "taskAfterBoundaryEvent", "task2");
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = NON_INTERRUPTING_BOUNDARY_EVENT)
  @Test
  public void testTask2AndStartBeforeTaskAfterNonInterruptingBoundaryEvent() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("taskAfterBoundaryEvent")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .activity("task2")
          .activity("taskAfterBoundaryEvent")
        .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child("taskAfterBoundaryEvent").concurrent().noScope().up()
          .child("task2").concurrent().noScope()
        .done());

    completeTasksInOrder("task2", "taskAfterBoundaryEvent");
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = NON_INTERRUPTING_BOUNDARY_EVENT)
  @Test
  public void testTask2AndStartBeforeNonInterruptingBoundaryEvent() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("boundaryEvent")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .activity("task2")
          .activity("taskAfterBoundaryEvent")
        .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child("taskAfterBoundaryEvent").concurrent().noScope().up()
          .child("task2").concurrent().noScope()
        .done());

    completeTasksInOrder("task2", "taskAfterBoundaryEvent");
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = INTERRUPTING_BOUNDARY_EVENT_INSIDE_SUBPROCESS)
  @Test
  public void testTask1AndStartBeforeTaskAfterBoundaryEventInsideSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("innerTaskAfterBoundaryEvent")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .beginScope("subProcess")
            .activity("innerTask1")
            .activity("innerTaskAfterBoundaryEvent")
        .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child(null).scope()
            .child("innerTaskAfterBoundaryEvent").concurrent().noScope().up()
            .child(null).concurrent().noScope()
              .child("innerTask1").scope()
        .done());

    completeTasksInOrder("innerTask1", "innerTaskAfterBoundaryEvent", "innerTask2");
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = INTERRUPTING_BOUNDARY_EVENT_INSIDE_SUBPROCESS)
  @Test
  public void testTask1AndStartBeforeBoundaryEventInsideSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("innerBoundaryEvent")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .beginScope("subProcess")
            .activity("innerTaskAfterBoundaryEvent")
        .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child("innerTaskAfterBoundaryEvent").scope()
        .done());

    completeTasksInOrder("innerTaskAfterBoundaryEvent");
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = NON_INTERRUPTING_BOUNDARY_EVENT_INSIDE_SUBPROCESS)
  @Test
  public void testTask1AndStartBeforeTaskAfterNonInterruptingBoundaryEventInsideSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("innerTaskAfterBoundaryEvent")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .beginScope("subProcess")
            .activity("innerTask1")
            .activity("innerTaskAfterBoundaryEvent")
        .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child(null).scope()
            .child("innerTaskAfterBoundaryEvent").concurrent().noScope().up()
            .child(null).concurrent().noScope()
              .child("innerTask1").scope()
        .done());

    completeTasksInOrder("innerTask1", "innerTaskAfterBoundaryEvent", "innerTask2");
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = NON_INTERRUPTING_BOUNDARY_EVENT_INSIDE_SUBPROCESS)
  @Test
  public void testTask1AndStartBeforeNonInterruptingBoundaryEventInsideSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("innerBoundaryEvent")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .beginScope("subProcess")
            .activity("innerTask1")
            .activity("innerTaskAfterBoundaryEvent")
        .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child(null).scope()
            .child("innerTaskAfterBoundaryEvent").concurrent().noScope().up()
            .child(null).concurrent().noScope()
              .child("innerTask1").scope()
        .done());

    completeTasksInOrder("innerTask1", "innerTask2", "innerTaskAfterBoundaryEvent");
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = INTERRUPTING_BOUNDARY_EVENT_INSIDE_SUBPROCESS)
  @Test
  public void testTask2AndStartBeforeTaskAfterBoundaryEventInsideSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("innerTaskAfterBoundaryEvent")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .beginScope("subProcess")
            .activity("innerTask2")
            .activity("innerTaskAfterBoundaryEvent")
        .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child(null).scope()
            .child("innerTaskAfterBoundaryEvent").concurrent().noScope().up()
            .child("innerTask2").concurrent().noScope()
        .done());

    completeTasksInOrder("innerTask2", "innerTaskAfterBoundaryEvent");
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = INTERRUPTING_BOUNDARY_EVENT_INSIDE_SUBPROCESS)
  @Test
  public void testTask2AndStartBeforeBoundaryEventInsideSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("innerBoundaryEvent")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .beginScope("subProcess")
            .activity("innerTask2")
            .activity("innerTaskAfterBoundaryEvent")
        .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child(null).scope()
            .child("innerTaskAfterBoundaryEvent").concurrent().noScope().up()
            .child("innerTask2").concurrent().noScope()
        .done());

    completeTasksInOrder("innerTask2", "innerTaskAfterBoundaryEvent");
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = NON_INTERRUPTING_BOUNDARY_EVENT_INSIDE_SUBPROCESS)
  @Test
  public void testTask2AndStartBeforeTaskAfterNonInterruptingBoundaryEventInsideSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("innerTaskAfterBoundaryEvent")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .beginScope("subProcess")
            .activity("innerTask2")
            .activity("innerTaskAfterBoundaryEvent")
        .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child(null).scope()
            .child("innerTaskAfterBoundaryEvent").concurrent().noScope().up()
            .child("innerTask2").concurrent().noScope()
        .done());

    completeTasksInOrder("innerTask2", "innerTaskAfterBoundaryEvent");
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = NON_INTERRUPTING_BOUNDARY_EVENT_INSIDE_SUBPROCESS)
  @Test
  public void testTask2AndStartBeforeNonInterruptingBoundaryEventInsideSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("innerBoundaryEvent")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .beginScope("subProcess")
            .activity("innerTask2")
            .activity("innerTaskAfterBoundaryEvent")
        .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child(null).scope()
            .child("innerTaskAfterBoundaryEvent").concurrent().noScope().up()
            .child("innerTask2").concurrent().noScope()
        .done());

    completeTasksInOrder("innerTask2", "innerTaskAfterBoundaryEvent");
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = INTERRUPTING_BOUNDARY_EVENT_ON_SUBPROCESS)
  @Test
  public void testStartBeforeTaskAfterBoundaryEventOnSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("taskAfterBoundaryEvent")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .beginScope("subProcess")
            .activity("innerTask")
          .endScope()
          .activity("taskAfterBoundaryEvent")
        .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child("taskAfterBoundaryEvent").concurrent().noScope().up()
          .child(null).concurrent().noScope()
            .child("innerTask").scope()
        .done());

    completeTasksInOrder("innerTask", "taskAfterBoundaryEvent");
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = INTERRUPTING_BOUNDARY_EVENT_ON_SUBPROCESS)
  @Test
  public void testStartBeforeBoundaryEventOnSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("boundaryEvent")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .activity("taskAfterBoundaryEvent")
        .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree("taskAfterBoundaryEvent").scope()
        .done());

    completeTasksInOrder("taskAfterBoundaryEvent");
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = NON_INTERRUPTING_BOUNDARY_EVENT_ON_SUBPROCESS)
  @Test
  public void testStartBeforeTaskAfterNonInterruptingBoundaryEventOnSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("taskAfterBoundaryEvent")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .beginScope("subProcess")
            .activity("innerTask")
          .endScope()
          .activity("taskAfterBoundaryEvent")
        .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child("taskAfterBoundaryEvent").concurrent().noScope().up()
          .child(null).concurrent().noScope()
            .child("innerTask").scope()
        .done());

    completeTasksInOrder("innerTask", "taskAfterBoundaryEvent");
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = NON_INTERRUPTING_BOUNDARY_EVENT_ON_SUBPROCESS)
  @Test
  public void testStartBeforeNonInterruptingBoundaryEventOnSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("taskAfterBoundaryEvent")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .beginScope("subProcess")
            .activity("innerTask")
          .endScope()
          .activity("taskAfterBoundaryEvent")
        .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child("taskAfterBoundaryEvent").concurrent().noScope().up()
          .child(null).concurrent().noScope()
            .child("innerTask").scope()
        .done());

    completeTasksInOrder("innerTask", "taskAfterBoundaryEvent");
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = INTERRUPTING_BOUNDARY_EVENT_WITH_PARALLEL_GATEWAY)
  @Test
  public void testStartBeforeInterruptingBoundaryEvent() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("boundaryEvent")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .activity("task1")
          .activity("taskAfterBoundaryEvent")
        .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child("task1").concurrent().noScope().up()
          .child("taskAfterBoundaryEvent").concurrent().noScope()
        .done());

    completeTasksInOrder("task1", "taskAfterBoundaryEvent");
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = NON_INTERRUPTING_BOUNDARY_EVENT_WITH_PARALLEL_GATEWAY)
  @Test
  public void testStartBeforeNonInterruptingBoundaryEvent() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("boundaryEvent")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .activity("task1")
          .activity("task2")
          .activity("taskAfterBoundaryEvent")
        .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child("task1").concurrent().noScope().up()
          .child(null).concurrent().noScope()
            .child("task2").scope().up().up()
          .child("taskAfterBoundaryEvent").concurrent().noScope()
        .done());

    completeTasksInOrder("task1", "task2", "taskAfterBoundaryEvent");
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = INTERRUPTING_BOUNDARY_EVENT_WITH_PARALLEL_GATEWAY_INSIDE_SUB_PROCESS)
  @Test
  public void testStartBeforeInterruptingBoundaryEventInsideSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("boundaryEvent")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .beginScope("subProcess")
            .activity("task1")
            .activity("taskAfterBoundaryEvent")
          .endScope()
        .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child(null).scope()
            .child("task1").concurrent().noScope().up()
            .child("taskAfterBoundaryEvent").concurrent().noScope()
        .done());

    completeTasksInOrder("task1", "taskAfterBoundaryEvent");
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = NON_INTERRUPTING_BOUNDARY_EVENT_WITH_PARALLEL_GATEWAY_INSIDE_SUB_PROCESS)
  @Test
  public void testStartBeforeNonInterruptingBoundaryEventInsideSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("boundaryEvent")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .beginScope("subProcess")
            .activity("task1")
            .activity("task2")
            .activity("taskAfterBoundaryEvent")
          .endScope()
        .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child(null).scope()
            .child("task1").concurrent().noScope().up()
            .child(null).concurrent().noScope()
              .child("task2").scope().up().up()
            .child("taskAfterBoundaryEvent").concurrent().noScope()
        .done());

    completeTasksInOrder("task1", "task2", "taskAfterBoundaryEvent");
    testRule.assertProcessEnded(processInstanceId);
  }

  protected void completeTasksInOrder(String... taskNames) {
    for (String taskName : taskNames) {
      // complete any task with that name
      List<Task> tasks = taskService.createTaskQuery().taskDefinitionKey(taskName).listPage(0, 1);
      assertTrue("task for activity " + taskName + " does not exist", !tasks.isEmpty());
      taskService.complete(tasks.get(0).getId());
    }
  }

}
