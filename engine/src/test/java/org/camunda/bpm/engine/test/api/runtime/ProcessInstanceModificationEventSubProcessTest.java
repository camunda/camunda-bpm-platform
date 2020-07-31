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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.Job;
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
public class ProcessInstanceModificationEventSubProcessTest extends PluggableProcessEngineTest {

  protected static final String INTERRUPTING_EVENT_SUBPROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.interruptingEventSubProcess.bpmn20.xml";
  protected static final String NON_INTERRUPTING_EVENT_SUBPROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.nonInterruptingEventSubProcess.bpmn20.xml";
  protected static final String INTERRUPTING_EVENT_SUBPROCESS_INSIDE_SUBPROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.interruptingEventSubProcessInsideSubProcess.bpmn20.xml";
  protected static final String NON_INTERRUPTING_EVENT_SUBPROCESS_INSIDE_SUBPROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.nonInterruptingEventSubProcessInsideSubProcess.bpmn20.xml";
  protected static final String CANCEL_AND_RESTART = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationEventSubProcessTest.testCancelAndRestart.bpmn20.xml";

  @Deployment(resources = INTERRUPTING_EVENT_SUBPROCESS)
  @Test
  public void testStartBeforeTaskInsideEventSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("eventSubProcessTask")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("task1")
        .beginScope("eventSubProcess")
          .activity("eventSubProcessTask")
        .endScope()
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child("task1").concurrent().noScope().up()
          .child(null).concurrent().noScope()
            .child("eventSubProcessTask").scope()
        .done());

    completeTasksInOrder("task1", "task2", "eventSubProcessTask");
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = INTERRUPTING_EVENT_SUBPROCESS)
  @Test
  public void testStartBeforeTaskInsideEventSubProcessAndCancelTaskOutsideEventSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .cancelActivityInstance(getInstanceIdForActivity(tree, "task1"))
      .startBeforeActivity("eventSubProcessTask")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .beginScope("eventSubProcess")
          .activity("eventSubProcessTask")
        .endScope()
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child("eventSubProcessTask").scope()
        .done());

    completeTasksInOrder("eventSubProcessTask");
    testRule.assertProcessEnded(processInstanceId);

  }

  @Deployment(resources = INTERRUPTING_EVENT_SUBPROCESS)
  @Test
  public void testStartBeforeStartEventInsideEventSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("eventProcessStart")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .beginScope("eventSubProcess")
          .activity("eventSubProcessTask")
        .endScope()
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child("eventSubProcessTask").scope()
        .done());

    completeTasksInOrder("eventSubProcessTask");
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = INTERRUPTING_EVENT_SUBPROCESS)
  @Test
  public void testStartBeforeEventSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("eventSubProcess")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .beginScope("eventSubProcess")
          .activity("eventSubProcessTask")
        .endScope()
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child("eventSubProcessTask").scope()
        .done());

    completeTasksInOrder("eventSubProcessTask");
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = NON_INTERRUPTING_EVENT_SUBPROCESS)
  @Test
  public void testStartBeforeTaskInsideNonInterruptingEventSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("eventSubProcessTask")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("task1")
        .beginScope("eventSubProcess")
          .activity("eventSubProcessTask")
        .endScope()
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child("task1").concurrent().noScope().up()
          .child(null).concurrent().noScope()
            .child("eventSubProcessTask").scope()
        .done());

    completeTasksInOrder("task1", "eventSubProcessTask", "task2");
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = NON_INTERRUPTING_EVENT_SUBPROCESS)
  @Test
  public void testStartBeforeTaskInsideNonInterruptingEventSubProcessAndCancelTaskOutsideEventSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .cancelActivityInstance(getInstanceIdForActivity(tree, "task1"))
      .startBeforeActivity("eventSubProcessTask")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .beginScope("eventSubProcess")
          .activity("eventSubProcessTask")
        .endScope()
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child("eventSubProcessTask").scope()
        .done());

    completeTasksInOrder("eventSubProcessTask");
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = NON_INTERRUPTING_EVENT_SUBPROCESS)
  @Test
  public void testStartBeforeStartEventInsideNonInterruptingEventSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("eventProcessStart")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("task1")
        .beginScope("eventSubProcess")
          .activity("eventSubProcessTask")
        .endScope()
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child("task1").concurrent().noScope().up()
          .child(null).concurrent().noScope()
            .child("eventSubProcessTask").scope()
        .done());

    completeTasksInOrder("task1", "task2", "eventSubProcessTask");
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = NON_INTERRUPTING_EVENT_SUBPROCESS)
  @Test
  public void testStartBeforeNonInterruptingEventSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("eventSubProcess")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("task1")
        .beginScope("eventSubProcess")
          .activity("eventSubProcessTask")
        .endScope()
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child("task1").concurrent().noScope().up()
          .child(null).concurrent().noScope()
            .child("eventSubProcessTask").scope()
        .done());

    completeTasksInOrder("task1", "eventSubProcessTask", "task2");
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = INTERRUPTING_EVENT_SUBPROCESS_INSIDE_SUBPROCESS)
  @Test
  public void testStartBeforeTaskInsideEventSubProcessInsideSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("eventSubProcessTask")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("task1")
          .beginScope("subProcess")
            .beginScope("eventSubProcess")
              .activity("eventSubProcessTask")
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child("task1").concurrent().noScope().up()
          .child(null).concurrent().noScope()
            .child(null).scope()
              .child("eventSubProcessTask").scope()
        .done());

    completeTasksInOrder("task1", "eventSubProcessTask", "task2");
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = INTERRUPTING_EVENT_SUBPROCESS_INSIDE_SUBPROCESS)
  @Test
  public void testStartBeforeStartEventInsideEventSubProcessInsideSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("eventProcessStart")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("task1")
          .beginScope("subProcess")
            .beginScope("eventSubProcess")
              .activity("eventSubProcessTask")
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child("task1").concurrent().noScope().up()
          .child(null).concurrent().noScope()
            .child(null).scope()
              .child("eventSubProcessTask").scope()
        .done());

    completeTasksInOrder("eventSubProcessTask", "task1", "task2");
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = INTERRUPTING_EVENT_SUBPROCESS_INSIDE_SUBPROCESS)
  @Test
  public void testStartBeforeEventSubProcessInsideSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("eventSubProcess")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("task1")
          .beginScope("subProcess")
            .beginScope("eventSubProcess")
              .activity("eventSubProcessTask")
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child("task1").concurrent().noScope().up()
          .child(null).concurrent().noScope()
            .child(null).scope()
              .child("eventSubProcessTask").scope()
        .done());

    completeTasksInOrder("task1", "eventSubProcessTask", "task2");
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = INTERRUPTING_EVENT_SUBPROCESS_INSIDE_SUBPROCESS)
  @Test
  public void testStartBeforeTaskInsideEventSubProcessInsideSubProcessTask2ShouldStay() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("eventSubProcessTask")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .beginScope("subProcess")
          .activity("task2")
          .beginScope("eventSubProcess")
            .activity("eventSubProcessTask")
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child(null).scope()
            .child("task2").concurrent().noScope().up()
            .child(null).concurrent().noScope()
              .child("eventSubProcessTask").scope()
        .done());

    completeTasksInOrder("task2", "eventSubProcessTask");
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = INTERRUPTING_EVENT_SUBPROCESS_INSIDE_SUBPROCESS)
  @Test
  public void testStartBeforeStartEventInsideEventSubProcessInsideSubProcessTask2ShouldBeCancelled() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("eventProcessStart")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .beginScope("subProcess")
          .beginScope("eventSubProcess")
            .activity("eventSubProcessTask")
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child(null).scope()
            .child("eventSubProcessTask").scope()
        .done());

    completeTasksInOrder("eventSubProcessTask");
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = INTERRUPTING_EVENT_SUBPROCESS_INSIDE_SUBPROCESS)
  @Test
  public void testStartBeforeEventSubProcessInsideSubProcessTask2ShouldBeCancelled() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("eventSubProcess")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .beginScope("subProcess")
          .beginScope("eventSubProcess")
            .activity("eventSubProcessTask")
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child(null).scope()
            .child("eventSubProcessTask").scope()
        .done());

    completeTasksInOrder("eventSubProcessTask");
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = NON_INTERRUPTING_EVENT_SUBPROCESS_INSIDE_SUBPROCESS)
  @Test
  public void testStartBeforeTaskInsideNonInterruptingEventSubProcessInsideSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("eventSubProcessTask")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("task1")
        .beginScope("subProcess")
          .beginScope("eventSubProcess")
            .activity("eventSubProcessTask")
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child("task1").concurrent().noScope().up()
          .child(null).concurrent().noScope()
            .child(null).scope()
              .child("eventSubProcessTask").scope()
        .done());

    completeTasksInOrder("task1", "eventSubProcessTask", "task2");
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = NON_INTERRUPTING_EVENT_SUBPROCESS_INSIDE_SUBPROCESS)
  @Test
  public void testStartBeforeStartEventInsideNonInterruptingEventSubProcessInsideSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("eventProcessStart")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("task1")
        .beginScope("subProcess")
          .beginScope("eventSubProcess")
            .activity("eventSubProcessTask")
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child("task1").concurrent().noScope().up()
          .child(null).concurrent().noScope()
            .child(null).scope()
              .child("eventSubProcessTask").scope()
        .done());

    completeTasksInOrder("task1", "task2", "eventSubProcessTask");
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = NON_INTERRUPTING_EVENT_SUBPROCESS_INSIDE_SUBPROCESS)
  @Test
  public void testStartBeforeNonInterruptingEventSubProcessInsideSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("eventSubProcess")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("task1")
        .beginScope("subProcess")
          .beginScope("eventSubProcess")
            .activity("eventSubProcessTask")
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child("task1").concurrent().noScope().up()
          .child(null).concurrent().noScope()
            .child(null).scope()
              .child("eventSubProcessTask").scope()
        .done());

    completeTasksInOrder("task1", "task2", "eventSubProcessTask");
    testRule.assertProcessEnded(processInstanceId);

  }

  @Deployment(resources = NON_INTERRUPTING_EVENT_SUBPROCESS_INSIDE_SUBPROCESS)
  @Test
  public void testStartBeforeTaskInsideNonInterruptingEventSubProcessInsideSubProcessTask2ShouldStay() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("eventSubProcessTask")
      .execute();


    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .beginScope("subProcess")
          .activity("task2")
          .beginScope("eventSubProcess")
            .activity("eventSubProcessTask")
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child(null).scope()
            .child("task2").concurrent().noScope().up()
            .child(null).concurrent().noScope()
              .child("eventSubProcessTask").scope()
        .done());

    completeTasksInOrder("task2", "eventSubProcessTask");
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = NON_INTERRUPTING_EVENT_SUBPROCESS_INSIDE_SUBPROCESS)
  @Test
  public void testStartBeforeStartEventInsideNonInterruptingEventSubProcessInsideSubProcessTask2ShouldStay() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("eventProcessStart")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .beginScope("subProcess")
          .activity("task2")
          .beginScope("eventSubProcess")
            .activity("eventSubProcessTask")
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child(null).scope()
            .child("task2").concurrent().noScope().up()
            .child(null).concurrent().noScope()
              .child("eventSubProcessTask").scope()
        .done());

    completeTasksInOrder("task2", "eventSubProcessTask");
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = NON_INTERRUPTING_EVENT_SUBPROCESS_INSIDE_SUBPROCESS)
  @Test
  public void testStartBeforeNonInterruptingEventSubProcessInsideSubProcessTask2ShouldStay() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = processInstance.getId();

    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);

    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("eventSubProcess")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .beginScope("subProcess")
            .activity("task2")
            .beginScope("eventSubProcess")
              .activity("eventSubProcessTask")
        .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child(null).scope()
            .child("task2").concurrent().noScope().up()
            .child(null).concurrent().noScope()
              .child("eventSubProcessTask").scope()
        .done());

    completeTasksInOrder("task2", "eventSubProcessTask");
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment
  @Test
  public void testTimerJobPreservationOnCancellationAndStart() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("timerEventSubProcess");

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    Job timerJob = managementService.createJobQuery().singleResult();
    assertNotNull(timerJob);

    // when the process instance is bare intermediately due to cancellation
    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "task"))
      .startBeforeActivity("task")
      .execute();

    // then it is still the same job

    Job remainingTimerJob = managementService.createJobQuery().singleResult();
    assertNotNull(remainingTimerJob);

    assertEquals(timerJob.getId(), remainingTimerJob.getId());
    assertEquals(timerJob.getDuedate(), remainingTimerJob.getDuedate());

  }


  @Deployment(resources = CANCEL_AND_RESTART)
  @Test
  public void testProcessInstanceModificationInEventSubProcessCancellationAndRestart() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("ProcessWithEventSubProcess");

    // assume
    Task task = taskService.createTaskQuery()
        .processInstanceId(processInstance.getId())
        .taskDefinitionKey("UserTaskEventSubProcess")
        .singleResult();
    assertNotNull(task);

    // when
    runtimeService.createProcessInstanceModification(processInstance.getId())
      .cancelAllForActivity("UserTaskEventSubProcess")
      .startAfterActivity("UserTaskEventSubProcess")
      .execute();

    assertNull(runtimeService.createProcessInstanceQuery().singleResult());
  }

  protected String getInstanceIdForActivity(ActivityInstance activityInstance, String activityId) {
    ActivityInstance instance = getChildInstanceForActivity(activityInstance, activityId);
    if (instance != null) {
      return instance.getId();
    }
    return null;
  }

  /**
   * Important that only the direct children are considered here. If you change this,
   * the test assertions are not as tight anymore.
   */
  protected ActivityInstance getChildInstanceForActivity(ActivityInstance activityInstance, String activityId) {
    for (ActivityInstance childInstance : activityInstance.getChildActivityInstances()) {
      if (childInstance.getActivityId().equals(activityId)) {
        return childInstance;
      }
    }

    return null;
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
