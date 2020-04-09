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

import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.bpmn.executionlistener.RecorderExecutionListener;
import org.camunda.bpm.engine.test.bpmn.executionlistener.RecorderExecutionListener.RecordedEvent;
import org.camunda.bpm.engine.test.util.ExecutionTree;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Test;

/**
 * Tests cancellation of four basic patterns of active activities in a scope:
 * <ul>
 *  <li>single, non-scope activity
 *  <li>single, scope activity
 *  <li>two concurrent non-scope activities
 *  <li>two concurrent scope activities
 * </ul>
 *
 * @author Thorben Lindhauer
 */
public class ProcessInstanceModificationCancellationTest extends PluggableProcessEngineTest {

  // the four patterns as described above
  protected static final String ONE_TASK_PROCESS = "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml";
  protected static final String ONE_SCOPE_TASK_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.oneScopeTaskProcess.bpmn20.xml";
  protected static final String CONCURRENT_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.parallelGateway.bpmn20.xml";
  protected static final String CONCURRENT_SCOPE_TASKS_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.parallelGatewayScopeTasks.bpmn20.xml";

  // the four patterns nested in a subprocess and with an outer parallel task
  protected static final String NESTED_PARALLEL_ONE_TASK_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.nestedParallelOneTaskProcess.bpmn20.xml";
  protected static final String NESTED_PARALLEL_ONE_SCOPE_TASK_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.nestedParallelOneScopeTaskProcess.bpmn20.xml";
  protected static final String NESTED_PARALLEL_CONCURRENT_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.nestedParallelGateway.bpmn20.xml";
  protected static final String NESTED_PARALLEL_CONCURRENT_SCOPE_TASKS_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.nestedParallelGatewayScopeTasks.bpmn20.xml";

  protected static final String LISTENER_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.listenerProcess.bpmn20.xml";
  protected static final String FAILING_OUTPUT_MAPPINGS_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.failingOutputMappingProcess.bpmn20.xml";

  protected static final String INTERRUPTING_EVENT_SUBPROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.interruptingEventSubProcess.bpmn20.xml";

  protected static final String CALL_ACTIVITY_PROCESS = "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testCallSimpleSubProcess.bpmn20.xml";
  protected static final String SIMPLE_SUBPROCESS = "org/camunda/bpm/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml";
  protected static final String TWO_SUBPROCESSES = "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testTwoSubProcesses.bpmn20.xml";
  protected static final String NESTED_CALL_ACTIVITY = "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testNestedCallActivity.bpmn20.xml";




  @Deployment(resources = ONE_TASK_PROCESS)
  @Test
  public void testCancellationInOneTaskProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "theTask"))
      .execute();

    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = ONE_TASK_PROCESS)
  @Test
  public void testCancelAllInOneTaskProcess() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String processInstanceId = processInstance.getId();

    // two instance of theTask
    runtimeService.createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("theTask")
      .execute();

    // when
    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .cancelAllForActivity("theTask")
      .execute();

    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = ONE_TASK_PROCESS)
  @Test
  public void testCancellationAndCreationInOneTaskProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "theTask"))
      .startBeforeActivity("theTask")
      .execute();

    testRule.assertProcessNotEnded(processInstanceId);

    // assert activity instance
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());
    assertEquals(tree.getId(), updatedTree.getId());
    assertTrue(!getInstanceIdForActivity(tree, "theTask").equals(getInstanceIdForActivity(updatedTree, "theTask")));

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("theTask")
      .done());

    // assert executions
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree("theTask").scope()
        .done());

    // assert successful completion of process
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = ONE_TASK_PROCESS)
  @Test
  public void testCreationAndCancellationInOneTaskProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("theTask")
      .cancelActivityInstance(getInstanceIdForActivity(tree, "theTask"))
      .execute();

    testRule.assertProcessNotEnded(processInstanceId);

    // assert activity instance
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());
    assertTrue(!getInstanceIdForActivity(tree, "theTask").equals(getInstanceIdForActivity(updatedTree, "theTask")));

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("theTask")
      .done());

    // assert executions
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree("theTask").scope()
        .done());

    // assert successful completion of process
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = ONE_SCOPE_TASK_PROCESS)
  @Test
  public void testCancellationInOneScopeTaskProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "theTask"))
      .execute();

    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = ONE_SCOPE_TASK_PROCESS)
  @Test
  public void testCancelAllInOneScopeTaskProcess() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String processInstanceId = processInstance.getId();

    // two instances of theTask
    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("theTask")
      .execute();

    // then
    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelAllForActivity("theTask")
      .execute();

    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = ONE_SCOPE_TASK_PROCESS)
  @Test
  public void testCancellationAndCreationInOneScopeTaskProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "theTask"))
      .startBeforeActivity("theTask")
      .execute();

    testRule.assertProcessNotEnded(processInstanceId);

    // assert activity instance
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());
    assertTrue(!getInstanceIdForActivity(tree, "theTask").equals(getInstanceIdForActivity(updatedTree, "theTask")));

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("theTask")
      .done());

    // assert executions
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope()
        .child("theTask").scope()
      .done());

    // assert successful completion of process
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = ONE_SCOPE_TASK_PROCESS)
  @Test
  public void testCreationAndCancellationInOneScopeTaskProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("theTask")
      .cancelActivityInstance(getInstanceIdForActivity(tree, "theTask"))
      .execute();

    testRule.assertProcessNotEnded(processInstanceId);

    // assert activity instance
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());
    assertTrue(!getInstanceIdForActivity(tree, "theTask").equals(getInstanceIdForActivity(updatedTree, "theTask")));

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("theTask")
      .done());

    // assert executions
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope()
        .child("theTask").scope()
      .done());

    // assert successful completion of process
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = CONCURRENT_PROCESS)
  @Test
  public void testCancellationInConcurrentProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("parallelGateway");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "task1"))
      .execute();

    testRule.assertProcessNotEnded(processInstanceId);

    // assert activity instance
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("task2")
      .done());

    // assert executions
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree("task2").scope()
      .done());

    // assert successful completion of process
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = CONCURRENT_PROCESS)
  @Test
  public void testCancelAllInConcurrentProcess() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("parallelGateway");
    String processInstanceId = processInstance.getId();

    // two instances in task1
    runtimeService.createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("task1")
      .execute();

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelAllForActivity("task1")
      .execute();

    testRule.assertProcessNotEnded(processInstanceId);

    // assert activity instance
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("task2")
      .done());

    // assert executions
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree("task2").scope()
      .done());

    // assert successful completion of process
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());
    testRule.assertProcessEnded(processInstanceId);
  }


  @Deployment(resources = CONCURRENT_PROCESS)
  @Test
  public void testCancellationAndCreationInConcurrentProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("parallelGateway");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "task1"))
      .startBeforeActivity("task1")
      .execute();

    testRule.assertProcessNotEnded(processInstanceId);

    // assert activity instance
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());
    assertTrue(!getInstanceIdForActivity(tree, "task1").equals(getInstanceIdForActivity(updatedTree, "task1")));

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("task1")
        .activity("task2")
      .done());

    // assert executions
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope()
        .child("task1").noScope().concurrent().up()
        .child("task2").noScope().concurrent()
      .done());

    // assert successful completion of process
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(2, tasks.size());

    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = CONCURRENT_PROCESS)
  @Test
  public void testCreationAndCancellationInConcurrentProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("parallelGateway");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("task1")
      .cancelActivityInstance(getInstanceIdForActivity(tree, "task1"))
      .execute();

    testRule.assertProcessNotEnded(processInstanceId);

    // assert activity instance
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());
    assertTrue(!getInstanceIdForActivity(tree, "task1").equals(getInstanceIdForActivity(updatedTree, "task1")));

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("task1")
        .activity("task2")
      .done());

    // assert executions
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope()
        .child("task1").noScope().concurrent().up()
        .child("task2").noScope().concurrent()
      .done());

    // assert successful completion of process
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(2, tasks.size());

    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = CONCURRENT_SCOPE_TASKS_PROCESS)
  @Test
  public void testCancellationInConcurrentScopeTasksProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("parallelGateway");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "task1"))
      .execute();

    testRule.assertProcessNotEnded(processInstanceId);

    // assert activity instance
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());
    assertTrue(!getInstanceIdForActivity(tree, "task1").equals(getInstanceIdForActivity(updatedTree, "task1")));

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("task2")
      .done());

    // assert executions
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope()
        .child("task2").scope()
      .done());

    // assert successful completion of process
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = CONCURRENT_SCOPE_TASKS_PROCESS)
  @Test
  public void testCancelAllInConcurrentScopeTasksProcess() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("parallelGateway");
    String processInstanceId = processInstance.getId();

    // two instances of task1
    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("task1")
      .execute();


    // when
    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelAllForActivity("task1")
      .execute();

    testRule.assertProcessNotEnded(processInstanceId);

    // assert activity instance
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("task2")
      .done());

    // assert executions
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope()
        .child("task2").scope()
      .done());

    // assert successful completion of process
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = CONCURRENT_SCOPE_TASKS_PROCESS)
  @Test
  public void testCancellationAndCreationInConcurrentScopeTasksProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("parallelGateway");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "task1"))
      .startBeforeActivity("task1")
      .execute();

    testRule.assertProcessNotEnded(processInstanceId);

    // assert activity instance
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());
    assertTrue(!getInstanceIdForActivity(tree, "task1").equals(getInstanceIdForActivity(updatedTree, "task1")));

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("task1")
        .activity("task2")
      .done());

    // assert executions
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope()
        .child(null).noScope().concurrent()
          .child("task1").scope().up().up()
        .child(null).noScope().concurrent()
          .child("task2").scope()
      .done());

    // assert successful completion of process
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(2, tasks.size());

    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = CONCURRENT_SCOPE_TASKS_PROCESS)
  @Test
  public void testCreationAndCancellationInConcurrentScopeTasksProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("parallelGateway");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("task1")
      .cancelActivityInstance(getInstanceIdForActivity(tree, "task1"))
      .execute();

    testRule.assertProcessNotEnded(processInstanceId);

    // assert activity instance
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());
    assertTrue(!getInstanceIdForActivity(tree, "task1").equals(getInstanceIdForActivity(updatedTree, "task1")));

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("task1")
        .activity("task2")
      .done());

    // assert executions
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope()
        .child(null).noScope().concurrent()
          .child("task1").scope().up().up()
        .child(null).noScope().concurrent()
          .child("task2").scope()
      .done());

    // assert successful completion of process
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(2, tasks.size());

    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = NESTED_PARALLEL_ONE_TASK_PROCESS)
  @Test
  public void testCancellationInNestedOneTaskProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nestedOneTaskProcess");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "innerTask"))
      .execute();

    testRule.assertProcessNotEnded(processInstanceId);

    // assert activity instance
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("outerTask")
      .done());

    // assert executions
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree("outerTask").scope()
        .done());

    // assert successful completion of process
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());
    testRule.assertProcessEnded(processInstanceId);

    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = NESTED_PARALLEL_ONE_TASK_PROCESS)
  @Test
  public void testScopeCancellationInNestedOneTaskProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nestedOneTaskProcess");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "subProcess"))
      .execute();

    testRule.assertProcessNotEnded(processInstanceId);

    // assert activity instance
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("outerTask")
      .done());

    // assert executions
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree("outerTask").scope()
      .done());

    // assert successful completion of process
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = NESTED_PARALLEL_ONE_TASK_PROCESS)
  @Test
  public void testCancellationAndCreationInNestedOneTaskProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nestedOneTaskProcess");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "innerTask"))
      .startBeforeActivity("innerTask")
      .execute();

    testRule.assertProcessNotEnded(processInstanceId);

    // assert activity instance
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());
    assertTrue(!getInstanceIdForActivity(tree, "innerTask").equals(getInstanceIdForActivity(updatedTree, "innerTask")));

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("outerTask")
        .beginScope("subProcess")
          .activity("innerTask")
      .done());

    // assert executions
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope()
      .child("outerTask").concurrent().noScope().up()
      .child(null).concurrent().noScope()
        .child("innerTask").scope()
      .done());

    // assert successful completion of process
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(2, tasks.size());

    for (Task task : tasks) {
      taskService.complete(task.getId());
    }
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = NESTED_PARALLEL_ONE_TASK_PROCESS)
  @Test
  public void testCreationAndCancellationInNestedOneTaskProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nestedOneTaskProcess");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("innerTask")
      .cancelActivityInstance(getInstanceIdForActivity(tree, "innerTask"))
      .execute();

    testRule.assertProcessNotEnded(processInstanceId);

    // assert activity instance
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());
    assertTrue(!getInstanceIdForActivity(tree, "innerTask").equals(getInstanceIdForActivity(updatedTree, "innerTask")));

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("outerTask")
        .beginScope("subProcess")
          .activity("innerTask")
      .done());

    // assert executions
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope()
        .child("outerTask").concurrent().noScope().up()
        .child(null).concurrent().noScope()
          .child("innerTask").scope()
      .done());

    // assert successful completion of process
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(2, tasks.size());

    for (Task task : tasks) {
      taskService.complete(task.getId());
    }
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = NESTED_PARALLEL_ONE_SCOPE_TASK_PROCESS)
  @Test
  public void testCancellationInNestedOneScopeTaskProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nestedOneScopeTaskProcess");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "innerTask"))
      .execute();

    testRule.assertProcessNotEnded(processInstanceId);

    // assert activity instance
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("outerTask")
      .done());

    // assert executions
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree("outerTask").scope()
      .done());

    // assert successful completion of process
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = NESTED_PARALLEL_ONE_SCOPE_TASK_PROCESS)
  @Test
  public void testScopeCancellationInNestedOneScopeTaskProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nestedOneScopeTaskProcess");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "subProcess"))
      .execute();

    testRule.assertProcessNotEnded(processInstanceId);

    // assert activity instance
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("outerTask")
      .done());

    // assert executions
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree("outerTask").scope()
      .done());

    // assert successful completion of process
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = NESTED_PARALLEL_ONE_SCOPE_TASK_PROCESS)
  @Test
  public void testCancellationAndCreationInNestedOneScopeTaskProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nestedOneScopeTaskProcess");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "innerTask"))
      .startBeforeActivity("innerTask")
      .execute();

    testRule.assertProcessNotEnded(processInstanceId);

    // assert activity instance
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());
    assertTrue(!getInstanceIdForActivity(tree, "innerTask").equals(getInstanceIdForActivity(updatedTree, "innerTask")));

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("outerTask")
        .beginScope("subProcess")
          .activity("innerTask")
      .done());

    // assert executions
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope()
      .child("outerTask").concurrent().noScope().up()
      .child(null).concurrent().noScope()
        .child(null).scope()
          .child("innerTask").scope()
      .done());

    // assert successful completion of process
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(2, tasks.size());

    for (Task task : tasks) {
      taskService.complete(task.getId());
    }
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = NESTED_PARALLEL_ONE_SCOPE_TASK_PROCESS)
  @Test
  public void testCreationAndCancellationInNestedOneScopeTaskProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nestedOneScopeTaskProcess");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("innerTask")
      .cancelActivityInstance(getInstanceIdForActivity(tree, "innerTask"))
      .execute();

    testRule.assertProcessNotEnded(processInstanceId);

    // assert activity instance
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());
    assertTrue(!getInstanceIdForActivity(tree, "innerTask").equals(getInstanceIdForActivity(updatedTree, "innerTask")));

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("outerTask")
        .beginScope("subProcess")
          .activity("innerTask")
      .done());

    // assert executions
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope()
        .child("outerTask").concurrent().noScope().up()
        .child(null).concurrent().noScope()
          .child(null).scope()
            .child("innerTask").scope()
      .done());

    // assert successful completion of process
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(2, tasks.size());

    for (Task task : tasks) {
      taskService.complete(task.getId());
    }
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = NESTED_PARALLEL_CONCURRENT_PROCESS)
  @Test
  public void testCancellationInNestedConcurrentProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nestedParallelGateway");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "innerTask1"))
      .execute();

    testRule.assertProcessNotEnded(processInstanceId);

    // assert activity instance
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("outerTask")
        .beginScope("subProcess")
          .activity("innerTask2")
      .done());

    // assert executions
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope()
        .child("outerTask").concurrent().noScope().up()
        .child(null).concurrent().noScope()
          .child("innerTask2").scope()
      .done());

    // assert successful completion of process
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(2, tasks.size());
    for (Task task : tasks) {
      taskService.complete(task.getId());
    }
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = NESTED_PARALLEL_CONCURRENT_PROCESS)
  @Test
  public void testScopeCancellationInNestedConcurrentProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nestedParallelGateway");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "subProcess"))
      .execute();

    testRule.assertProcessNotEnded(processInstanceId);

    // assert activity instance
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("outerTask")
      .done());

    // assert executions
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree("outerTask").scope()
      .done());

    // assert successful completion of process
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    taskService.complete(task.getId());
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = NESTED_PARALLEL_CONCURRENT_PROCESS)
  @Test
  public void testCancellationAndCreationInNestedConcurrentProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nestedParallelGateway");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "innerTask1"))
      .startBeforeActivity("innerTask1")
      .execute();

    testRule.assertProcessNotEnded(processInstanceId);

    // assert activity instance
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());
    assertTrue(!getInstanceIdForActivity(tree, "innerTask1").equals(getInstanceIdForActivity(updatedTree, "innerTask1")));

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("outerTask")
        .beginScope("subProcess")
          .activity("innerTask1")
          .activity("innerTask2")
      .done());

    // assert executions
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope()
        .child("outerTask").noScope().concurrent().up()
        .child(null).noScope().concurrent()
          .child(null).scope()
            .child("innerTask1").noScope().concurrent().up()
            .child("innerTask2").noScope().concurrent()
      .done());

    // assert successful completion of process
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(3, tasks.size());
    for (Task task : tasks) {
      taskService.complete(task.getId());
    }
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = NESTED_PARALLEL_CONCURRENT_PROCESS)
  @Test
  public void testCreationAndCancellationInNestedConcurrentProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nestedParallelGateway");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("innerTask1")
      .cancelActivityInstance(getInstanceIdForActivity(tree, "innerTask1"))
      .execute();

    testRule.assertProcessNotEnded(processInstanceId);

    // assert activity instance
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());
    assertTrue(!getInstanceIdForActivity(tree, "innerTask1").equals(getInstanceIdForActivity(updatedTree, "innerTask1")));

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("outerTask")
        .beginScope("subProcess")
          .activity("innerTask1")
          .activity("innerTask2")
      .done());

    // assert executions
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope()
        .child("outerTask").noScope().concurrent().up()
        .child(null).noScope().concurrent()
          .child(null).scope()
            .child("innerTask1").noScope().concurrent().up()
            .child("innerTask2").noScope().concurrent()
      .done());

    // assert successful completion of process
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(3, tasks.size());
    for (Task task : tasks) {
      taskService.complete(task.getId());
    }
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = NESTED_PARALLEL_CONCURRENT_SCOPE_TASKS_PROCESS)
  @Test
  public void testCancellationInNestedConcurrentScopeTasksProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nestedParallelGatewayScopeTasks");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "innerTask1"))
      .execute();

    testRule.assertProcessNotEnded(processInstanceId);

    // assert activity instance
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("outerTask")
        .beginScope("subProcess")
          .activity("innerTask2")
      .done());

    // assert executions
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope()
        .child("outerTask").concurrent().noScope().up()
        .child(null).concurrent().noScope()
          .child(null).scope()
            .child("innerTask2").scope()
      .done());

    // assert successful completion of process
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(2, tasks.size());
    for (Task task : tasks) {
      taskService.complete(task.getId());
    }
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = NESTED_PARALLEL_CONCURRENT_SCOPE_TASKS_PROCESS)
  @Test
  public void testScopeCancellationInNestedConcurrentScopeTasksProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nestedParallelGatewayScopeTasks");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "subProcess"))
      .execute();

    testRule.assertProcessNotEnded(processInstanceId);

    // assert activity instance
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("outerTask")
      .done());

    // assert executions
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree("outerTask").scope()
      .done());

    // assert successful completion of process
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    taskService.complete(task.getId());
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = NESTED_PARALLEL_CONCURRENT_SCOPE_TASKS_PROCESS)
  @Test
  public void testCancellationAndCreationInNestedConcurrentScopeTasksProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nestedParallelGatewayScopeTasks");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "innerTask1"))
      .startBeforeActivity("innerTask1")
      .execute();

    testRule.assertProcessNotEnded(processInstanceId);

    // assert activity instance
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());
    assertTrue(!getInstanceIdForActivity(tree, "innerTask1").equals(getInstanceIdForActivity(updatedTree, "innerTask1")));

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("outerTask")
        .beginScope("subProcess")
          .activity("innerTask1")
          .activity("innerTask2")
      .done());

    // assert executions
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope()
        .child("outerTask").concurrent().noScope().up()
        .child(null).concurrent().noScope()
          .child(null).scope()
            .child(null).concurrent().noScope()
              .child("innerTask1").scope().up().up()
            .child(null).concurrent().noScope()
              .child("innerTask2").scope()
      .done());

    // assert successful completion of process
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(3, tasks.size());

    for (Task task : tasks) {
      taskService.complete(task.getId());
    }
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = NESTED_PARALLEL_CONCURRENT_SCOPE_TASKS_PROCESS)
  @Test
  public void testCreationAndCancellationInNestedConcurrentScopeTasksProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nestedParallelGatewayScopeTasks");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("innerTask1")
      .cancelActivityInstance(getInstanceIdForActivity(tree, "innerTask1"))
      .execute();

    testRule.assertProcessNotEnded(processInstanceId);

    // assert activity instance
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());
    assertTrue(!getInstanceIdForActivity(tree, "innerTask1").equals(getInstanceIdForActivity(updatedTree, "innerTask1")));

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("outerTask")
        .beginScope("subProcess")
          .activity("innerTask1")
          .activity("innerTask2")
      .done());

    // assert executions
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope()
        .child("outerTask").concurrent().noScope().up()
        .child(null).noScope().concurrent()
          .child(null).scope()
            .child(null).concurrent().noScope()
              .child("innerTask1").scope().up().up()
            .child(null).concurrent().noScope()
              .child("innerTask2").scope()
      .done());

    // assert successful completion of process
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(3, tasks.size());

    for (Task task : tasks) {
      taskService.complete(task.getId());
    }
    testRule.assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = LISTENER_PROCESS)
  @Test
  public void testEndListenerInvocation() {
    RecorderExecutionListener.clear();

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
        "listenerProcess",
        Collections.<String, Object>singletonMap("listener", new RecorderExecutionListener()));

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    // when one inner task is cancelled
    runtimeService.createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "innerTask1"))
      .execute();

    assertEquals(1, RecorderExecutionListener.getRecordedEvents().size());
    RecordedEvent innerTask1EndEvent = RecorderExecutionListener.getRecordedEvents().get(0);
    assertEquals(ExecutionListener.EVENTNAME_END, innerTask1EndEvent.getEventName());
    assertEquals("innerTask1", innerTask1EndEvent.getActivityId());
    assertEquals(getInstanceIdForActivity(tree, "innerTask1"), innerTask1EndEvent.getActivityInstanceId());

    // when the second inner task is cancelled
    RecorderExecutionListener.clear();
    runtimeService.createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "innerTask2"))
      .execute();

    assertEquals(2, RecorderExecutionListener.getRecordedEvents().size());
    RecordedEvent innerTask2EndEvent = RecorderExecutionListener.getRecordedEvents().get(0);
    assertEquals(ExecutionListener.EVENTNAME_END, innerTask2EndEvent.getEventName());
    assertEquals("innerTask2", innerTask2EndEvent.getActivityId());
    assertEquals(getInstanceIdForActivity(tree, "innerTask2"), innerTask2EndEvent.getActivityInstanceId());

    RecordedEvent subProcessEndEvent = RecorderExecutionListener.getRecordedEvents().get(1);
    assertEquals(ExecutionListener.EVENTNAME_END, subProcessEndEvent.getEventName());
    assertEquals("subProcess", subProcessEndEvent.getActivityId());
    assertEquals(getInstanceIdForActivity(tree, "subProcess"), subProcessEndEvent.getActivityInstanceId());

    // when the outer task is cancelled (and so the entire process)
    RecorderExecutionListener.clear();
    runtimeService.createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "outerTask"))
      .execute();

    assertEquals(2, RecorderExecutionListener.getRecordedEvents().size());
    RecordedEvent outerTaskEndEvent = RecorderExecutionListener.getRecordedEvents().get(0);
    assertEquals(ExecutionListener.EVENTNAME_END, outerTaskEndEvent.getEventName());
    assertEquals("outerTask", outerTaskEndEvent.getActivityId());
    assertEquals(getInstanceIdForActivity(tree, "outerTask"), outerTaskEndEvent.getActivityInstanceId());

    RecordedEvent processEndEvent = RecorderExecutionListener.getRecordedEvents().get(1);
    assertEquals(ExecutionListener.EVENTNAME_END, processEndEvent.getEventName());
    assertNull(processEndEvent.getActivityId());
    assertEquals(tree.getId(), processEndEvent.getActivityInstanceId());

    RecorderExecutionListener.clear();
  }

  /**
   * Tests the case that an output mapping exists that expects variables
   * that do not exist yet when the activities are cancelled
   */
  @Deployment(resources = FAILING_OUTPUT_MAPPINGS_PROCESS)
  @Test
  public void testSkipOutputMappingsOnCancellation() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingOutputMappingProcess");

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    // then executing the following cancellations should not fail because
    // it skips the output mapping
    // cancel inner task
    runtimeService.createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "innerTask"))
      .execute(false, true);

    // cancel outer task
    runtimeService.createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "outerTask"))
      .execute(false, true);

    testRule.assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = INTERRUPTING_EVENT_SUBPROCESS)
  @Test
  public void testProcessInstanceEventSubscriptionsPreservedOnIntermediateCancellation() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    // event subscription for the event subprocess
    EventSubscription subscription = runtimeService.createEventSubscriptionQuery().singleResult();
    assertNotNull(subscription);
    assertEquals(processInstance.getId(), subscription.getProcessInstanceId());

    // when I execute cancellation and then start, such that the intermediate state of the process instance
    // has no activities
    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService.createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "task1"))
      .startBeforeActivity("task1")
      .execute();

    // then the message event subscription remains (i.e. it is not deleted and later re-created)
    EventSubscription updatedSubscription = runtimeService.createEventSubscriptionQuery().singleResult();
    assertNotNull(updatedSubscription);
    assertEquals(subscription.getId(), updatedSubscription.getId());
    assertEquals(subscription.getProcessInstanceId(), updatedSubscription.getProcessInstanceId());
  }

  @Deployment(resources = ONE_TASK_PROCESS)
  @Test
  public void testProcessInstanceVariablesPreservedOnIntermediateCancellation() {
    ProcessInstance processInstance = runtimeService
        .startProcessInstanceByKey("oneTaskProcess", Variables.createVariables().putValue("var", "value"));

    // when I execute cancellation and then start, such that the intermediate state of the process instance
    // has no activities
    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService.createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "theTask"))
      .startBeforeActivity("theTask")
      .execute();

    // then the process instance variables remain
    Object variable = runtimeService.getVariable(processInstance.getId(), "var");
    assertNotNull(variable);
    assertEquals("value", variable);
  }

  public String getInstanceIdForActivity(ActivityInstance activityInstance, String activityId) {
    ActivityInstance instance = getChildInstanceForActivity(activityInstance, activityId);
    if (instance != null) {
      return instance.getId();
    }
    return null;
  }

  public ActivityInstance getChildInstanceForActivity(ActivityInstance activityInstance, String activityId) {
    if (activityId.equals(activityInstance.getActivityId())) {
      return activityInstance;
    }

    for (ActivityInstance childInstance : activityInstance.getChildActivityInstances()) {
      ActivityInstance instance = getChildInstanceForActivity(childInstance, activityId);
      if (instance != null) {
        return instance;
      }
    }

    return null;
  }

  /**
   * Test case for checking cancellation of process instances in call activity subprocesses
   *
   * Test should propagate upward and destroy all process instances
   *
   */
  @Deployment(resources = {
    SIMPLE_SUBPROCESS,
    CALL_ACTIVITY_PROCESS
  })
  @Test
  public void testCancellationInCallActivitySubProcess() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callSimpleSubProcess");
    String processInstanceId = processInstance.getId();

    // one task in the subprocess should be active after starting the process instance
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task taskBeforeSubProcess = taskQuery.singleResult();

    // Completing the task continues the process which leads to calling the subprocess
    taskService.complete(taskBeforeSubProcess.getId());
    Task taskInSubProcess = taskQuery.singleResult();


    List<ProcessInstance> instanceList = runtimeService.createProcessInstanceQuery().list();
    assertNotNull(instanceList);
    assertEquals(2, instanceList.size());

    ActivityInstance tree = runtimeService.getActivityInstance(taskInSubProcess.getProcessInstanceId());
    // when
    runtimeService
      .createProcessInstanceModification(taskInSubProcess.getProcessInstanceId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "task"))
      .execute();


    // then
    testRule.assertProcessEnded(processInstanceId);

    // How many process Instances
    instanceList = runtimeService.createProcessInstanceQuery().list();
    assertNotNull(instanceList);
    assertEquals(0, instanceList.size());
  }

  @Deployment(resources = {
    SIMPLE_SUBPROCESS,
    CALL_ACTIVITY_PROCESS
  })
  @Test
  public void testCancellationAndRestartInCallActivitySubProcess() {
    // given
    runtimeService.startProcessInstanceByKey("callSimpleSubProcess");

    // one task in the subprocess should be active after starting the process instance
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task taskBeforeSubProcess = taskQuery.singleResult();

    // Completing the task continues the process which leads to calling the subprocess
    taskService.complete(taskBeforeSubProcess.getId());
    Task taskInSubProcess = taskQuery.singleResult();


    List<ProcessInstance> instanceList = runtimeService.createProcessInstanceQuery().list();
    assertNotNull(instanceList);
    assertEquals(2, instanceList.size());

    ActivityInstance tree = runtimeService.getActivityInstance(taskInSubProcess.getProcessInstanceId());
    // when
    runtimeService
      .createProcessInstanceModification(taskInSubProcess.getProcessInstanceId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "task"))
      .startBeforeActivity("task")
      .execute();

    // then
    // How many process Instances
    instanceList = runtimeService.createProcessInstanceQuery().list();
    assertEquals(2, instanceList.size());
  }

  /**
   * Test case for checking cancellation of process instances in call activity subprocesses
   *
   * Test that upward cancellation respects other process instances
   *
   */
  @Deployment(resources = {
    SIMPLE_SUBPROCESS,
    TWO_SUBPROCESSES
  })
  @Test
  public void testSingleCancellationWithTwoSubProcess() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callTwoSubProcesses");
    List<ProcessInstance> instanceList = runtimeService.createProcessInstanceQuery().list();
    assertNotNull(instanceList);
    assertEquals(3, instanceList.size());

    List<Task> taskList = taskService.createTaskQuery().list();
    assertNotNull(taskList);
    assertEquals(2, taskList.size());

    List<String> activeActivityIds = runtimeService.getActiveActivityIds(processInstance.getProcessInstanceId());
    assertNotNull(activeActivityIds);
    assertEquals(2, activeActivityIds.size());

    ActivityInstance tree = runtimeService.getActivityInstance(taskList.get(0).getProcessInstanceId());

    // when
    runtimeService
      .createProcessInstanceModification(taskList.get(0).getProcessInstanceId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "task"))
      .execute();

    // then

    // How many process Instances
    instanceList = runtimeService.createProcessInstanceQuery().list();
    assertNotNull(instanceList);
    assertEquals(2, instanceList.size());

    // How man call activities
    activeActivityIds = runtimeService.getActiveActivityIds(processInstance.getProcessInstanceId());
    assertNotNull(activeActivityIds);
    assertEquals(1, activeActivityIds.size());
  }

  /**
   * Test case for checking deletion of process instances in nested call activity subprocesses
   *
   * Checking that nested call activities will propagate upward over multiple nested levels
   *
   */
  @Deployment(resources = {
    SIMPLE_SUBPROCESS,
    NESTED_CALL_ACTIVITY,
    CALL_ACTIVITY_PROCESS
  })
  @Test
  public void testCancellationMultilevelProcessInstanceInCallActivity() {
    // given
    runtimeService.startProcessInstanceByKey("nestedCallActivity");

    // one task in the subprocess should be active after starting the process instance
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task taskBeforeSubProcess = taskQuery.singleResult();

    // Completing the task continues the process which leads to calling the subprocess
    taskService.complete(taskBeforeSubProcess.getId());
    Task taskInSubProcess = taskQuery.singleResult();

    // Completing the task continues the sub process which leads to calling the deeper subprocess
    taskService.complete(taskInSubProcess.getId());
    Task taskInNestedSubProcess = taskQuery.singleResult();

    List<ProcessInstance> instanceList = runtimeService.createProcessInstanceQuery().list();
    assertNotNull(instanceList);
    assertEquals(3, instanceList.size());

    ActivityInstance tree = runtimeService.getActivityInstance(taskInNestedSubProcess.getProcessInstanceId());

    // when
    runtimeService
      .createProcessInstanceModification(taskInNestedSubProcess.getProcessInstanceId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "task"))
      .execute();

    // then
    // How many process Instances
    instanceList = runtimeService.createProcessInstanceQuery().list();
    assertNotNull(instanceList);
    assertEquals(0, instanceList.size());
  }

}
