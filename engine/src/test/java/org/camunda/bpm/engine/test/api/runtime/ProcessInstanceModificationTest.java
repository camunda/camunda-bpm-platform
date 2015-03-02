/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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

import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.examples.bpmn.executionlistener.RecorderExecutionListener;
import org.camunda.bpm.engine.test.examples.bpmn.executionlistener.RecorderExecutionListener.RecordedEvent;
import org.camunda.bpm.engine.test.examples.bpmn.tasklistener.RecorderTaskListener;
import org.camunda.bpm.engine.test.util.ExecutionTree;
import org.camunda.bpm.engine.variable.Variables;

/**
 * @author Thorben Lindhauer
 *
 */
public class ProcessInstanceModificationTest extends PluggableProcessEngineTestCase {

  protected static final String PARALLEL_GATEWAY_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.parallelGateway.bpmn20.xml";
  protected static final String EXCLUSIVE_GATEWAY_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.exclusiveGateway.bpmn20.xml";
  protected static final String EXCLUSIVE_GATEWAY_ASYNC_TASK_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.exclusiveGatewayAsyncTask.bpmn20.xml";
  protected static final String SUBPROCESS_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.subprocess.bpmn20.xml";
  protected static final String SUBPROCESS_LISTENER_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.subprocessListeners.bpmn20.xml";
  protected static final String SUBPROCESS_BOUNDARY_EVENTS_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.subprocessBoundaryEvents.bpmn20.xml";
  protected static final String ONE_SCOPE_TASK_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.oneScopeTaskProcess.bpmn20.xml";
  protected static final String TRANSITION_LISTENER_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.transitionListeners.bpmn20.xml";
  protected static final String TASK_LISTENER_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.taskListeners.bpmn20.xml";
  protected static final String IO_MAPPING_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.ioMapping.bpmn20.xml";
  protected static final String DOUBLE_NESTED_SUB_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.doubleNestedSubprocess.bpmn20.xml";

  @Deployment(resources = PARALLEL_GATEWAY_PROCESS)
  public void testCancellation() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("parallelGateway");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "task1"))
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("task2")
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree("task2").scope()
        .done());

    // complete the process
    completeTasksInOrder("task2");
    assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = PARALLEL_GATEWAY_PROCESS)
  public void testCancellationThatEndsProcessInstance() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("parallelGateway");

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "task1"))
      .cancelActivityInstance(getInstanceIdForActivity(tree, "task2"))
      .execute();

    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = EXCLUSIVE_GATEWAY_PROCESS)
  public void testStartBefore() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exclusiveGateway");
    String processInstanceId = processInstance.getId();

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("task2")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("task1")
        .activity("task2")
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope()
        .child("task1").concurrent().noScope().up()
        .child("task2").concurrent().noScope()
      .done());

    assertEquals(2, taskService.createTaskQuery().count());

    // complete the process
    completeTasksInOrder("task1", "task2");
    assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = EXCLUSIVE_GATEWAY_PROCESS)
  public void testStartBeforeWithAncestorInstanceId() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exclusiveGateway");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstanceId);

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("task2", tree.getId())
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("task1")
        .activity("task2")
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope()
        .child("task1").concurrent().noScope().up()
        .child("task2").concurrent().noScope()
      .done());

    assertEquals(2, taskService.createTaskQuery().count());

    // complete the process
    completeTasksInOrder("task1", "task2");
    assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = DOUBLE_NESTED_SUB_PROCESS)
  public void testStartBeforeWithAncestorInstanceIdTwoScopesUp() {
    // given two instances of the outer subprocess
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("doubleNestedSubprocess");
    String processInstanceId = processInstance.getId();

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("subProcess")
      .execute();

    // when I start the inner subprocess task without explicit ancestor
    try {
      runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("innerSubProcessTask")
      .execute();
      // then the command fails
      fail("should not succeed because the ancestors are ambiguous");
    } catch (ProcessEngineException e) {
      // happy path
    }

    // when I start the inner subprocess task with an explicit ancestor activity instance id
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    ActivityInstance randomSubProcessInstance = getChildInstanceForActivity(updatedTree, "subProcess");

    // then the command suceeds
    runtimeService.createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("innerSubProcessTask", randomSubProcessInstance.getId())
      .execute();

    // and the trees are correct
    updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    // TODO: re-add when instance tree algorithm is fixed
//    assertThat(updatedTree).hasStructure(
//      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
//        .beginScope("subProcess")
//          .activity("subProcessTask")
//        .endScope()
//        .beginScope("subProcess")
//          .activity("subProcessTask")
//          .beginScope("innerSubProcess")
//            .activity("innerSubProcessTask")
//      .done());
//
//    ActivityInstance innerSubProcessInstance = getChildInstanceForActivity(updatedTree, "innerSubProcess");
//    assertEquals(randomSubProcessInstance.getId(), innerSubProcessInstance.getParentActivityInstanceId());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope()
        .child(null).concurrent().noScope()
          .child("subProcessTask").scope().up().up()
        .child(null).concurrent().noScope()
          .child(null).scope()
            .child("subProcessTask").concurrent().noScope().up()
            .child(null).concurrent().noScope()
              .child("innerSubProcessTask").scope()
      .done());

    assertEquals(3, taskService.createTaskQuery().count());

    // complete the process
    completeTasksInOrder("subProcessTask", "subProcessTask",
        "innerSubProcessTask", "innerSubProcessTask", "innerSubProcessTask");
    assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = DOUBLE_NESTED_SUB_PROCESS)
  public void testStartBeforeWithInvalidAncestorInstanceId() {
    // given two instances of the outer subprocess
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("doubleNestedSubprocess");
    String processInstanceId = processInstance.getId();


    try {
      runtimeService
        .createProcessInstanceModification(processInstance.getId())
        .startBeforeActivity("subProcess", "noValidActivityInstanceId")
        .execute();
      fail();
    } catch (ProcessEngineException e) {
      // happy path
    }

    try {
      runtimeService
        .createProcessInstanceModification(processInstance.getId())
        .startBeforeActivity("subProcess", null)
        .execute();
      fail();
    } catch (ProcessEngineException e) {
      // happy path
    }

    ActivityInstance tree = runtimeService.getActivityInstance(processInstanceId);

    try {
      runtimeService
        .createProcessInstanceModification(processInstance.getId())
        .startBeforeActivity("subProcess", getInstanceIdForActivity(tree, "subProcessTask"))
        .execute();
      fail("should not succeed because subProcessTask is a child of subProcess");
    } catch (ProcessEngineException e) {
      // happy path
    }
  }

  @Deployment(resources = EXCLUSIVE_GATEWAY_PROCESS)
  public void testStartTransition() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exclusiveGateway");
    String processInstanceId = processInstance.getId();

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startTransition("flow4")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("task1")
        .activity("task2")
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope()
        .child("task1").concurrent().noScope().up()
        .child("task2").concurrent().noScope()
      .done());

    assertEquals(2, taskService.createTaskQuery().count());

    // complete the process
    completeTasksInOrder("task1", "task2");
    assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = EXCLUSIVE_GATEWAY_PROCESS)
  public void testStartTransitionWithAncestorInstanceId() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exclusiveGateway");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstanceId);

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startTransition("flow4", tree.getId())
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("task1")
        .activity("task2")
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope()
        .child("task1").concurrent().noScope().up()
        .child("task2").concurrent().noScope()
      .done());

    assertEquals(2, taskService.createTaskQuery().count());

    // complete the process
    completeTasksInOrder("task1", "task2");
    assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = DOUBLE_NESTED_SUB_PROCESS)
  public void testStartTransitionWithAncestorInstanceIdTwoScopesUp() {
    // given two instances of the outer subprocess
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("doubleNestedSubprocess");
    String processInstanceId = processInstance.getId();

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("subProcess")
      .execute();

    // when I start the inner subprocess task without explicit ancestor
    try {
      runtimeService
        .createProcessInstanceModification(processInstance.getId())
        .startTransition("flow5")
        .execute();
      // then the command fails
      fail("should not succeed because the ancestors are ambiguous");
    } catch (ProcessEngineException e) {
      // happy path
    }

    // when I start the inner subprocess task with an explicit ancestor activity instance id
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    ActivityInstance randomSubProcessInstance = getChildInstanceForActivity(updatedTree, "subProcess");

    // then the command suceeds
    runtimeService.createProcessInstanceModification(processInstanceId)
      .startTransition("flow5", randomSubProcessInstance.getId())
      .execute();

    // and the trees are correct
    updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    // TODO: re-add when instance tree algorithm is fixed
//    assertThat(updatedTree).hasStructure(
//      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
//        .beginScope("subProcess")
//          .activity("subProcessTask")
//        .endScope()
//        .beginScope("subProcess")
//          .activity("subProcessTask")
//          .beginScope("innerSubProcess")
//            .activity("innerSubProcessTask")
//      .done());
//
//    ActivityInstance innerSubProcessInstance = getChildInstanceForActivity(updatedTree, "innerSubProcess");
//    assertEquals(randomSubProcessInstance.getId(), innerSubProcessInstance.getParentActivityInstanceId());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope()
        .child(null).concurrent().noScope()
          .child("subProcessTask").scope().up().up()
        .child(null).concurrent().noScope()
          .child(null).scope()
            .child("subProcessTask").concurrent().noScope().up()
            .child(null).concurrent().noScope()
              .child("innerSubProcessTask").scope()
      .done());

    assertEquals(3, taskService.createTaskQuery().count());

    // complete the process
    completeTasksInOrder("subProcessTask", "subProcessTask",
        "innerSubProcessTask", "innerSubProcessTask", "innerSubProcessTask");
    assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = DOUBLE_NESTED_SUB_PROCESS)
  public void testStartTransitionWithInvalidAncestorInstanceId() {
    // given two instances of the outer subprocess
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("doubleNestedSubprocess");
    String processInstanceId = processInstance.getId();


    try {
      runtimeService
        .createProcessInstanceModification(processInstance.getId())
        .startTransition("flow5", "noValidActivityInstanceId")
        .execute();
      fail();
    } catch (ProcessEngineException e) {
      // happy path
    }

    try {
      runtimeService
        .createProcessInstanceModification(processInstance.getId())
        .startTransition("flow5", null)
        .execute();
      fail();
    } catch (ProcessEngineException e) {
      // happy path
    }

    ActivityInstance tree = runtimeService.getActivityInstance(processInstanceId);

    try {
      runtimeService
        .createProcessInstanceModification(processInstance.getId())
        .startTransition("flow5", getInstanceIdForActivity(tree, "subProcessTask"))
        .execute();
      fail("should not succeed because subProcessTask is a child of subProcess");
    } catch (ProcessEngineException e) {
      // happy path
    }
  }

  @Deployment(resources = EXCLUSIVE_GATEWAY_PROCESS)
  public void testStartTransitionCase2() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exclusiveGateway");
    String processInstanceId = processInstance.getId();

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startTransition("flow2")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("task1")
        .activity("task1")
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope()
        .child("task1").concurrent().noScope().up()
        .child("task1").concurrent().noScope()
      .done());

    assertEquals(2, taskService.createTaskQuery().count());

    // complete the process
    completeTasksInOrder("task1", "task1");
    assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = EXCLUSIVE_GATEWAY_PROCESS)
  public void testStartTransitionInvalidTransitionId() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exclusiveGateway");
    String processInstanceId = processInstance.getId();

    try {
      runtimeService
        .createProcessInstanceModification(processInstanceId)
        .startTransition("invalidFlowId")
        .execute();

      fail("should not suceed");

    } catch (ProcessEngineException e) {
      // happy path
    }
  }

  @Deployment(resources = EXCLUSIVE_GATEWAY_PROCESS)
  public void testStartAfter() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exclusiveGateway");
    String processInstanceId = processInstance.getId();

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startAfterActivity("theStart")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("task1")
        .activity("task1")
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope()
        .child("task1").concurrent().noScope().up()
        .child("task1").concurrent().noScope()
      .done());

    assertEquals(2, taskService.createTaskQuery().count());

    // complete the process
    completeTasksInOrder("task1", "task1");
    assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = EXCLUSIVE_GATEWAY_PROCESS)
  public void testStartAfterWithAncestorInstanceId() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exclusiveGateway");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstanceId);

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startAfterActivity("theStart", tree.getId())
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("task1")
        .activity("task1")
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope()
        .child("task1").concurrent().noScope().up()
        .child("task1").concurrent().noScope()
      .done());

    assertEquals(2, taskService.createTaskQuery().count());

    // complete the process
    completeTasksInOrder("task1", "task1");
    assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = DOUBLE_NESTED_SUB_PROCESS)
  public void testStartAfterWithAncestorInstanceIdTwoScopesUp() {
    // given two instances of the outer subprocess
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("doubleNestedSubprocess");
    String processInstanceId = processInstance.getId();

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("subProcess")
      .execute();

    // when I start the inner subprocess task without explicit ancestor
    try {
      runtimeService
        .createProcessInstanceModification(processInstance.getId())
        .startAfterActivity("innerSubProcessStart")
        .execute();
      // then the command fails
      fail("should not succeed because the ancestors are ambiguous");
    } catch (ProcessEngineException e) {
      // happy path
    }

    // when I start the inner subprocess task with an explicit ancestor activity instance id
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    ActivityInstance randomSubProcessInstance = getChildInstanceForActivity(updatedTree, "subProcess");

    // then the command suceeds
    runtimeService.createProcessInstanceModification(processInstanceId)
      .startAfterActivity("innerSubProcessStart", randomSubProcessInstance.getId())
      .execute();

    // and the trees are correct
    updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    // TODO: re-add when instance tree algorithm is fixed
//    assertThat(updatedTree).hasStructure(
//      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
//        .beginScope("subProcess")
//          .activity("subProcessTask")
//        .endScope()
//        .beginScope("subProcess")
//          .activity("subProcessTask")
//          .beginScope("innerSubProcess")
//            .activity("innerSubProcessTask")
//      .done());
//
//    ActivityInstance innerSubProcessInstance = getChildInstanceForActivity(updatedTree, "innerSubProcess");
//    assertEquals(randomSubProcessInstance.getId(), innerSubProcessInstance.getParentActivityInstanceId());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope()
        .child(null).concurrent().noScope()
          .child("subProcessTask").scope().up().up()
        .child(null).concurrent().noScope()
          .child(null).scope()
            .child("subProcessTask").concurrent().noScope().up()
            .child(null).concurrent().noScope()
              .child("innerSubProcessTask").scope()
      .done());

    assertEquals(3, taskService.createTaskQuery().count());

    // complete the process
    completeTasksInOrder("subProcessTask", "subProcessTask",
        "innerSubProcessTask", "innerSubProcessTask", "innerSubProcessTask");
    assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = DOUBLE_NESTED_SUB_PROCESS)
  public void testStartAfterWithInvalidAncestorInstanceId() {
    // given two instances of the outer subprocess
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("doubleNestedSubprocess");
    String processInstanceId = processInstance.getId();


    try {
      runtimeService
        .createProcessInstanceModification(processInstance.getId())
        .startAfterActivity("innerSubProcessStart", "noValidActivityInstanceId")
        .execute();
      fail();
    } catch (ProcessEngineException e) {
      // happy path
    }

    try {
      runtimeService
        .createProcessInstanceModification(processInstance.getId())
        .startAfterActivity("innerSubProcessStart", null)
        .execute();
      fail();
    } catch (ProcessEngineException e) {
      // happy path
    }

    ActivityInstance tree = runtimeService.getActivityInstance(processInstanceId);

    try {
      runtimeService
        .createProcessInstanceModification(processInstance.getId())
        .startAfterActivity("innerSubProcessStart", getInstanceIdForActivity(tree, "subProcessTask"))
        .execute();
      fail("should not succeed because subProcessTask is a child of subProcess");
    } catch (ProcessEngineException e) {
      // happy path
    }
  }

  @Deployment(resources = EXCLUSIVE_GATEWAY_PROCESS)
  public void testStartAfterActivityAmbiguousTransitions() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exclusiveGateway");
    String processInstanceId = processInstance.getId();

    try {
      runtimeService
        .createProcessInstanceModification(processInstanceId)
        .startAfterActivity("fork")
        .execute();

      fail("should not suceed since 'fork' has more than one outgoing sequence flow");

    } catch (ProcessEngineException e) {
      // happy path
      assertTextPresent("activity has more than one outgoing sequence flow", e.getMessage());
    }
  }

  @Deployment(resources = EXCLUSIVE_GATEWAY_PROCESS)
  public void testStartAfterActivityNoOutgoingTransitions() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exclusiveGateway");
    String processInstanceId = processInstance.getId();

    try {
      runtimeService
        .createProcessInstanceModification(processInstanceId)
        .startAfterActivity("theEnd")
        .execute();

      fail("should not suceed since 'theEnd' has no outgoing sequence flow");

    } catch (ProcessEngineException e) {
      // happy path
      assertTextPresent("activity has no outgoing sequence flow to take", e.getMessage());
    }
  }

  @Deployment(resources = ONE_SCOPE_TASK_PROCESS)
  public void testScopeTaskStartBefore() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String processInstanceId = processInstance.getId();

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("theTask")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("theTask")
        .activity("theTask")
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope()
        .child(null).concurrent().noScope()
          .child("theTask").scope().up().up()
        .child(null).concurrent().noScope()
          .child("theTask").scope()
      .done());

    assertEquals(2, taskService.createTaskQuery().count());
    completeTasksInOrder("theTask", "theTask");
    assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = ONE_SCOPE_TASK_PROCESS)
  public void testScopeTaskStartAfter() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String processInstanceId = processInstance.getId();

    // when starting after the task, essentially nothing changes in the process instance
    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startAfterActivity("theTask")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("theTask")
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope()
        .child("theTask").scope()
      .done());

    // when starting after the start event, regular concurrency happens
    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startAfterActivity("theStart")
      .execute();

    updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("theTask")
        .activity("theTask")
      .done());

    executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope()
        .child(null).concurrent().noScope()
          .child("theTask").scope().up().up()
        .child(null).concurrent().noScope()
          .child("theTask").scope()
      .done());

    completeTasksInOrder("theTask", "theTask");
    assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = EXCLUSIVE_GATEWAY_ASYNC_TASK_PROCESS)
  public void testStartBeforeAsync() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exclusiveGateway");
    String processInstanceId = processInstance.getId();

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("task2")
      .execute();

    // the task does not yet exist because it is started asynchronously
    Task task = taskService.createTaskQuery().taskDefinitionKey("task2").singleResult();
    assertNull(task);

    // and there is no activity instance for task2 yet
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("task1")
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope()
        .child("task1").concurrent().noScope().up()
        .child("task2").concurrent().noScope()
      .done());

    // when the async job is executed
    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);
    executeAvailableJobs();

    // then there is the task
    task = taskService.createTaskQuery().taskDefinitionKey("task2").singleResult();
    assertNotNull(task);

    // and there is an activity instance for task2
    updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("task1")
        .activity("task2")
      .done());

    completeTasksInOrder("task1", "task2");
    assertProcessEnded(processInstanceId);
  }

  /**
   * starting after a task should not respect that tasks asyncAfter setting
   */
  @Deployment
  public void testStartAfterAsync() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exclusiveGateway");
    String processInstanceId = processInstance.getId();

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startAfterActivity("task2")
      .execute();

    // there is now a job for the end event after task2
    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);

    Execution jobExecution = runtimeService.createExecutionQuery().activityId("end2").executionId(job.getExecutionId()).singleResult();
    assertNotNull(jobExecution);

    // end process
    completeTasksInOrder("task1");
    managementService.executeJob(job.getId());
    assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = SUBPROCESS_BOUNDARY_EVENTS_PROCESS)
  public void testStartBeforeEventSubscription() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("subprocess");

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("innerTask")
      .execute();

    // then two timer jobs should have been created
    assertEquals(2, managementService.createJobQuery().count());
    Job innerJob = managementService.createJobQuery().activityId("innerTimer").singleResult();
    assertNotNull(innerJob);
    assertEquals(runtimeService.createExecutionQuery().activityId("innerTask").singleResult().getId(),
        innerJob.getExecutionId());

    Job outerJob = managementService.createJobQuery().activityId("outerTimer").singleResult();
    assertNotNull(outerJob);

    // when executing the jobs
    managementService.executeJob(innerJob.getId());

    Task innerBoundaryTask = taskService.createTaskQuery().taskDefinitionKey("innerAfterBoundaryTask").singleResult();
    assertNotNull(innerBoundaryTask);

    managementService.executeJob(outerJob.getId());

    Task outerBoundaryTask = taskService.createTaskQuery().taskDefinitionKey("outerAfterBoundaryTask").singleResult();
    assertNotNull(outerBoundaryTask);


  }

  @Deployment(resources = SUBPROCESS_LISTENER_PROCESS)
  public void testActivityExecutionListenerInvocation() {
    RecorderExecutionListener.clear();

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
        "subprocess",
        Collections.<String, Object>singletonMap("listener", new RecorderExecutionListener()));

    String processInstanceId = processInstance.getId();

    assertTrue(RecorderExecutionListener.getRecordedEvents().isEmpty());

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("innerTask")
      .execute();

    // assert activity instance tree
    ActivityInstance activityInstanceTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(activityInstanceTree);
    assertEquals(processInstanceId, activityInstanceTree.getProcessInstanceId());

    assertThat(activityInstanceTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("outerTask")
        .beginScope("subProcess")
          .activity("innerTask")
      .done());

    // assert listener invocations
    List<RecordedEvent> recordedEvents = RecorderExecutionListener.getRecordedEvents();
    assertEquals(2, recordedEvents.size());

    ActivityInstance subprocessInstance = getChildInstanceForActivity(activityInstanceTree, "subProcess");
    ActivityInstance innerTaskInstance = getChildInstanceForActivity(subprocessInstance, "innerTask");

    RecordedEvent firstEvent = recordedEvents.get(0);
    RecordedEvent secondEvent = recordedEvents.get(1);

    assertEquals("subProcess", firstEvent.getActivityId());
    assertEquals(subprocessInstance.getId(), firstEvent.getActivityInstanceId());
    assertEquals(ExecutionListener.EVENTNAME_START, secondEvent.getEventName());

    assertEquals("innerTask", secondEvent.getActivityId());
    assertEquals(innerTaskInstance.getId(), secondEvent.getActivityInstanceId());
    assertEquals(ExecutionListener.EVENTNAME_START, secondEvent.getEventName());

    RecorderExecutionListener.clear();

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(innerTaskInstance.getId())
      .execute();

    assertEquals(2, RecorderExecutionListener.getRecordedEvents().size());
  }

  @Deployment(resources = SUBPROCESS_LISTENER_PROCESS)
  public void testSkipListenerInvocation() {
    RecorderExecutionListener.clear();

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
        "subprocess",
        Collections.<String, Object>singletonMap("listener", new RecorderExecutionListener()));

    String processInstanceId = processInstance.getId();

    assertTrue(RecorderExecutionListener.getRecordedEvents().isEmpty());

    // when I start an activity with "skip listeners" setting
    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("innerTask")
      .execute(true, false);

    // then no listeners are invoked
    assertTrue(RecorderExecutionListener.getRecordedEvents().isEmpty());

    // when I cancel an activity with "skip listeners" setting
    ActivityInstance activityInstanceTree = runtimeService.getActivityInstance(processInstanceId);

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(getChildInstanceForActivity(activityInstanceTree, "innerTask").getId())
      .execute(true, false);

    // then no listeners are invoked
    assertTrue(RecorderExecutionListener.getRecordedEvents().isEmpty());

    // when I cancel an activity that ends the process instance
    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(getChildInstanceForActivity(activityInstanceTree, "outerTask").getId())
      .execute(true, false);

    // then no listeners are invoked
    assertTrue(RecorderExecutionListener.getRecordedEvents().isEmpty());
  }

  @Deployment(resources = TASK_LISTENER_PROCESS)
  public void FAILING_testSkipTaskListenerInvocation() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
        "taskListenerProcess",
        Collections.<String, Object>singletonMap("listener", new RecorderTaskListener()));

    String processInstanceId = processInstance.getId();

    RecorderTaskListener.clear();

    // when I start an activity with "skip listeners" setting
    runtimeService
      .createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("task")
      .execute(true, false);

    // then no listeners are invoked
    assertTrue(RecorderTaskListener.getRecordedEvents().isEmpty());

    // when I cancel an activity with "skip listeners" setting
    ActivityInstance activityInstanceTree = runtimeService.getActivityInstance(processInstanceId);

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(getChildInstanceForActivity(activityInstanceTree, "task").getId())
      .execute(true, false);

    // then no listeners are invoked
    assertTrue(RecorderTaskListener.getRecordedEvents().isEmpty());
  }

  @Deployment(resources = IO_MAPPING_PROCESS)
  public void testSkipIoMappings() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("ioMappingProcess");

    // when I start task2
    runtimeService.createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("task2")
      .execute(false, true);

    // then the input mapping should not have executed
    Execution task2Execution = runtimeService.createExecutionQuery().activityId("task2").singleResult();
    assertNotNull(task2Execution);

    assertNull(runtimeService.getVariable(task2Execution.getId(), "inputMappingExecuted"));

    // when I cancel task2
    runtimeService.createProcessInstanceModification(processInstance.getId())
      .cancelAllInActivity("task2")
      .execute(false, true);

    // then the output mapping should not have executed
    assertNull(runtimeService.getVariable(processInstance.getId(), "outputMappingExecuted"));
  }

  @Deployment(resources = TRANSITION_LISTENER_PROCESS)
  public void testStartTransitionListenerInvocation() {
    RecorderExecutionListener.clear();

    ProcessInstance instance = runtimeService.startProcessInstanceByKey("transitionListenerProcess",
        Variables.createVariables().putValue("listener", new RecorderExecutionListener()));

    runtimeService.createProcessInstanceModification(instance.getId())
      .startTransition("flow2")
      .execute();

    // transition listener should have been invoked
    List<RecordedEvent> events = RecorderExecutionListener.getRecordedEvents();
    assertEquals(1, events.size());

    RecordedEvent event = events.get(0);
    assertEquals("flow2", event.getTransitionId());

    RecorderExecutionListener.clear();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(instance.getId());
    assertNotNull(updatedTree);
    assertEquals(instance.getId(), updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(instance.getProcessDefinitionId())
        .activity("task1")
        .activity("task2")
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(instance.getId(), processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child("task1").concurrent().noScope().up()
          .child("task2").concurrent().noScope()
        .done());

    completeTasksInOrder("task1", "task2", "task2");
    assertProcessEnded(instance.getId());
  }

  @Deployment(resources = TRANSITION_LISTENER_PROCESS)
  public void testStartAfterActivityListenerInvocation() {
    RecorderExecutionListener.clear();

    ProcessInstance instance = runtimeService.startProcessInstanceByKey("transitionListenerProcess",
        Variables.createVariables().putValue("listener", new RecorderExecutionListener()));

    runtimeService.createProcessInstanceModification(instance.getId())
      .startTransition("flow2")
      .execute();

    // transition listener should have been invoked
    List<RecordedEvent> events = RecorderExecutionListener.getRecordedEvents();
    assertEquals(1, events.size());

    RecordedEvent event = events.get(0);
    assertEquals("flow2", event.getTransitionId());

    RecorderExecutionListener.clear();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(instance.getId());
    assertNotNull(updatedTree);
    assertEquals(instance.getId(), updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(instance.getProcessDefinitionId())
        .activity("task1")
        .activity("task2")
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(instance.getId(), processEngine);

    assertThat(executionTree)
      .matches(
        describeExecutionTree(null).scope()
          .child("task1").concurrent().noScope().up()
          .child("task2").concurrent().noScope()
        .done());

    completeTasksInOrder("task1", "task2", "task2");
    assertProcessEnded(instance.getId());
  }

  @Deployment(resources = EXCLUSIVE_GATEWAY_PROCESS)
  public void testStartBeforeWithVariables() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exclusiveGateway");

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("task2")
      .setVariable("procInstVar", "procInstValue")
      .setVariableLocal("localVar", "localValue")
      .execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstance.getId());
    assertNotNull(updatedTree);
    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .activity("task1")
          .activity("task2")
        .done());

    ActivityInstance task2Instance = getChildInstanceForActivity(updatedTree, "task2");
    assertNotNull(task2Instance);
    assertEquals(1, task2Instance.getExecutionIds().length);
    String task2ExecutionId = task2Instance.getExecutionIds()[0];

    assertEquals("procInstValue", runtimeService.getVariableLocal(processInstance.getId(), "procInstVar"));
    assertEquals("localValue", runtimeService.getVariableLocal(task2ExecutionId, "localVar"));

    completeTasksInOrder("task1", "task2");
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = EXCLUSIVE_GATEWAY_PROCESS)
  public void testCancellationAndStartBefore() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exclusiveGateway");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "task1"))
      .startBeforeActivity("task2")
      .execute();

    ActivityInstance activityInstanceTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(activityInstanceTree);
    assertEquals(processInstanceId, activityInstanceTree.getProcessInstanceId());

    assertThat(activityInstanceTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("task2")
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree("task2").scope()
      .done());

    completeTasksInOrder("task2");
    assertProcessEnded(processInstanceId);
  }

  // TODO: what happens with compensation?
  // Scenario: Subprocess with two activities
  // activity 1 is executed successfully, has compensation
  // activity 2 is entered and then cancelled, such that the complete subprocess
  // cancels
  // => should compensation of activity 1 be later on possible? yes
  @Deployment
  public void testCompensationRemovalOnCancellation() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensationProcess");

    Execution taskExecution = runtimeService.createExecutionQuery().activityId("innerTask").singleResult();
    Task task = taskService.createTaskQuery().executionId(taskExecution.getId()).singleResult();
    assertNotNull(task);

    taskService.complete(task.getId());
    // there should be a compensation event subscription for innerTask now
    assertEquals(1, runtimeService.createEventSubscriptionQuery().count());

    // when innerTask2 is cancelled
    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "innerTask2"))
      .execute();

    // then the innerTask compensation should be removed
    assertEquals(0, runtimeService.createEventSubscriptionQuery().count());
  }

  @Deployment
  public void testCompensationCreation() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensationProcess");

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("innerTask")
      .execute();

    Execution task2Execution = runtimeService.createExecutionQuery().activityId("innerTask").singleResult();
    Task task = taskService.createTaskQuery().executionId(task2Execution.getId()).singleResult();
    assertNotNull(task);

    taskService.complete(task.getId());
    assertEquals(2, runtimeService.createEventSubscriptionQuery().count());

    // trigger compensation
    Task outerTask = taskService.createTaskQuery().taskDefinitionKey("outerTask").singleResult();
    assertNotNull(outerTask);
    taskService.complete(outerTask.getId());

    // then there are two compensation tasks and the afterSubprocessTask:
    // TODO: due to CAM-3628, there are only two tasks. the subprocess compensation task misses
    assertEquals(2, taskService.createTaskQuery().count());
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey("innerAfterBoundaryTask").count());
//    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey("outerAfterBoundaryTask").count());
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey("taskAfterSubprocess").count());

    // complete process
    completeTasksInOrder("taskAfterSubprocess", "innerAfterBoundaryTask");

    assertProcessEnded(processInstance.getId());
  }

  @Deployment
  public void testNoCompensationCreatedOnCancellation() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensationProcess");
    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    // one on outerTask, one on innerTask
    assertEquals(2, taskService.createTaskQuery().count());


    // when inner task is cancelled
    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "innerTask"))
      .execute();

    // then no compensation event subscription exists
    assertEquals(0, runtimeService.createEventSubscriptionQuery().count());

    // and the compensation throw event does not trigger compensation handlers
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    assertEquals("outerTask", task.getTaskDefinitionKey());

    taskService.complete(task.getId());

    assertProcessEnded(processInstance.getId());
  }

  protected String getInstanceIdForActivity(ActivityInstance activityInstance, String activityId) {
    ActivityInstance instance = getChildInstanceForActivity(activityInstance, activityId);
    if (instance != null) {
      return instance.getId();
    }
    return null;
  }

  protected ActivityInstance getChildInstanceForActivity(ActivityInstance activityInstance, String activityId) {
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

  protected void completeTasksInOrder(String... taskNames) {
    for (String taskName : taskNames) {
      // complete any task with that name
      List<Task> tasks = taskService.createTaskQuery().taskDefinitionKey(taskName).listPage(0, 1);
      assertTrue("task for activity " + taskName + " does not exist", !tasks.isEmpty());
      taskService.complete(tasks.get(0).getId());
    }
  }
}
