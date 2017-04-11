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
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.bpmn.executionlistener.RecorderExecutionListener;
import org.camunda.bpm.engine.test.bpmn.executionlistener.RecorderExecutionListener.RecordedEvent;
import org.camunda.bpm.engine.test.bpmn.tasklistener.util.RecorderTaskListener;
import org.camunda.bpm.engine.test.util.ExecutionTree;
import org.camunda.bpm.engine.variable.Variables;

/**
 * @author Thorben Lindhauer
 *
 */
public class ProcessInstanceModificationTest extends PluggableProcessEngineTestCase {

  protected static final String PARALLEL_GATEWAY_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.parallelGateway.bpmn20.xml";
  protected static final String EXCLUSIVE_GATEWAY_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.exclusiveGateway.bpmn20.xml";
  protected static final String SUBPROCESS_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.subprocess.bpmn20.xml";
  protected static final String SUBPROCESS_LISTENER_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.subprocessListeners.bpmn20.xml";
  protected static final String SUBPROCESS_BOUNDARY_EVENTS_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.subprocessBoundaryEvents.bpmn20.xml";
  protected static final String ONE_SCOPE_TASK_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.oneScopeTaskProcess.bpmn20.xml";
  protected static final String TRANSITION_LISTENER_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.transitionListeners.bpmn20.xml";
  protected static final String TASK_LISTENER_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.taskListeners.bpmn20.xml";
  protected static final String IO_MAPPING_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.ioMapping.bpmn20.xml";
  protected static final String IO_MAPPING_ON_SUB_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.ioMappingOnSubProcess.bpmn20.xml";
  protected static final String IO_MAPPING_ON_SUB_PROCESS_AND_NESTED_SUB_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.ioMappingOnSubProcessNested.bpmn20.xml";
  protected static final String LISTENERS_ON_SUB_PROCESS_AND_NESTED_SUB_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.listenersOnSubProcessNested.bpmn20.xml";
  protected static final String DOUBLE_NESTED_SUB_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.doubleNestedSubprocess.bpmn20.xml";
  protected static final String TRANSACTION_WITH_COMPENSATION_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.testTransactionWithCompensation.bpmn20.xml";
  protected static final String CALL_ACTIVITY_PARENT_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.testCancelCallActivityParentProcess.bpmn";
  protected static final String CALL_ACTIVITY_CHILD_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.testCancelCallActivityChildProcess.bpmn";

  @Deployment(resources = PARALLEL_GATEWAY_PROCESS)
  public void testCancellation() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("parallelGateway");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService.createProcessInstanceModification(processInstance.getId()).cancelActivityInstance(getInstanceIdForActivity(tree, "task1")).execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(describeActivityInstanceTree(processInstance.getProcessDefinitionId()).activity("task2").done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree).matches(describeExecutionTree("task2").scope().done());

    // complete the process
    completeTasksInOrder("task2");
    assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = PARALLEL_GATEWAY_PROCESS)
  public void testCancellationThatEndsProcessInstance() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("parallelGateway");

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService.createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "task1"))
      .cancelActivityInstance(getInstanceIdForActivity(tree, "task2"))
      .execute();
      
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = PARALLEL_GATEWAY_PROCESS)
  public void testCancellationWithWrongProcessInstanceId() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("parallelGateway");

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    try {
      runtimeService.createProcessInstanceModification("foo")
        .cancelActivityInstance(getInstanceIdForActivity(tree, "task1"))
        .cancelActivityInstance(getInstanceIdForActivity(tree, "task2"))
        .execute();
      assertProcessEnded(processInstance.getId());

    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), startsWith("ENGINE-13036"));
      assertThat(e.getMessage(), containsString("Process instance '" + "foo" + "' cannot be modified"));
    }
  }

  @Deployment(resources = EXCLUSIVE_GATEWAY_PROCESS)
  public void testStartBefore() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exclusiveGateway");
    String processInstanceId = processInstance.getId();

    runtimeService.createProcessInstanceModification(processInstance.getId()).startBeforeActivity("task2").execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(describeActivityInstanceTree(processInstance.getProcessDefinitionId()).activity("task1").activity("task2").done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
        .matches(describeExecutionTree(null).scope().child("task1").concurrent().noScope().up().child("task2").concurrent().noScope().done());

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

    runtimeService.createProcessInstanceModification(processInstance.getId()).startBeforeActivity("task2", tree.getId()).execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(describeActivityInstanceTree(processInstance.getProcessDefinitionId()).activity("task1").activity("task2").done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
        .matches(describeExecutionTree(null).scope().child("task1").concurrent().noScope().up().child("task2").concurrent().noScope().done());

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

    runtimeService.createProcessInstanceModification(processInstance.getId()).startBeforeActivity("subProcess").execute();

    // when I start the inner subprocess task without explicit ancestor
    try {
      runtimeService.createProcessInstanceModification(processInstance.getId()).startBeforeActivity("innerSubProcessTask").execute();
      // then the command fails
      fail("should not succeed because the ancestors are ambiguous");
    } catch (ProcessEngineException e) {
      // happy path
    }

    // when I start the inner subprocess task with an explicit ancestor activity
    // instance id
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    ActivityInstance randomSubProcessInstance = getChildInstanceForActivity(updatedTree, "subProcess");

    // then the command suceeds
    runtimeService.createProcessInstanceModification(processInstanceId).startBeforeActivity("innerSubProcessTask", randomSubProcessInstance.getId()).execute();

    // and the trees are correct
    updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree)
        .hasStructure(describeActivityInstanceTree(processInstance.getProcessDefinitionId()).beginScope("subProcess").activity("subProcessTask").endScope()
            .beginScope("subProcess").activity("subProcessTask").beginScope("innerSubProcess").activity("innerSubProcessTask").done());

    ActivityInstance innerSubProcessInstance = getChildInstanceForActivity(updatedTree, "innerSubProcess");
    assertEquals(randomSubProcessInstance.getId(), innerSubProcessInstance.getParentActivityInstanceId());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree).matches(describeExecutionTree(null).scope().child(null).concurrent().noScope().child("subProcessTask").scope().up().up()
        .child(null).concurrent().noScope().child(null).scope().child("subProcessTask").concurrent().noScope().up().child(null).concurrent().noScope()
        .child("innerSubProcessTask").scope().done());

    assertEquals(3, taskService.createTaskQuery().count());

    // complete the process
    completeTasksInOrder("subProcessTask", "subProcessTask", "innerSubProcessTask", "innerSubProcessTask", "innerSubProcessTask");
    assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = DOUBLE_NESTED_SUB_PROCESS)
  public void testStartBeforeWithInvalidAncestorInstanceId() {
    // given two instances of the outer subprocess
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("doubleNestedSubprocess");
    String processInstanceId = processInstance.getId();

    try {
      runtimeService.createProcessInstanceModification(processInstance.getId()).startBeforeActivity("subProcess", "noValidActivityInstanceId").execute();
      fail();
    } catch (NotValidException e) {
      // happy path
      assertTextPresent("Cannot perform instruction: " + "Start before activity 'subProcess' with ancestor activity instance 'noValidActivityInstanceId'; "
          + "Ancestor activity instance 'noValidActivityInstanceId' does not exist", e.getMessage());
    }

    try {
      runtimeService.createProcessInstanceModification(processInstance.getId()).startBeforeActivity("subProcess", null).execute();
      fail();
    } catch (NotValidException e) {
      // happy path
      assertTextPresent("ancestorActivityInstanceId is null", e.getMessage());
    }

    ActivityInstance tree = runtimeService.getActivityInstance(processInstanceId);
    String subProcessTaskId = getInstanceIdForActivity(tree, "subProcessTask");

    try {
      runtimeService.createProcessInstanceModification(processInstance.getId()).startBeforeActivity("subProcess", subProcessTaskId).execute();
      fail("should not succeed because subProcessTask is a child of subProcess");
    } catch (NotValidException e) {
      // happy path
      assertTextPresent("Cannot perform instruction: " + "Start before activity 'subProcess' with ancestor activity instance '" + subProcessTaskId + "'; "
          + "Scope execution for '" + subProcessTaskId + "' cannot be found in parent hierarchy of flow element 'subProcess'", e.getMessage());
    }
  }

  @Deployment(resources = EXCLUSIVE_GATEWAY_PROCESS)
  public void testStartBeforeNonExistingActivity() {
    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("exclusiveGateway");

    try {
      // when
      runtimeService.createProcessInstanceModification(instance.getId()).startBeforeActivity("someNonExistingActivity").execute();
      fail("should not succeed");
    } catch (NotValidException e) {
      // then
      assertTextPresentIgnoreCase("element 'someNonExistingActivity' does not exist in process ", e.getMessage());
    }
  }

  /**
   * CAM-3718
   */
  @Deployment(resources = EXCLUSIVE_GATEWAY_PROCESS)
  public void testEndProcessInstanceIntermediately() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exclusiveGateway");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstanceId);

    runtimeService.createProcessInstanceModification(processInstance.getId()).cancelActivityInstance(getInstanceIdForActivity(tree, "task1"))
        .startAfterActivity("task1").startBeforeActivity("task1").execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);

    assertThat(updatedTree).hasStructure(describeActivityInstanceTree(processInstance.getProcessDefinitionId()).activity("task1").done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree).matches(describeExecutionTree("task1").scope().done());

    assertEquals(1, taskService.createTaskQuery().count());

    // complete the process
    completeTasksInOrder("task1");
    assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = EXCLUSIVE_GATEWAY_PROCESS)
  public void testStartTransition() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exclusiveGateway");
    String processInstanceId = processInstance.getId();

    runtimeService.createProcessInstanceModification(processInstance.getId()).startTransition("flow4").execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(describeActivityInstanceTree(processInstance.getProcessDefinitionId()).activity("task1").activity("task2").done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
        .matches(describeExecutionTree(null).scope().child("task1").concurrent().noScope().up().child("task2").concurrent().noScope().done());

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

    runtimeService.createProcessInstanceModification(processInstance.getId()).startTransition("flow4", tree.getId()).execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(describeActivityInstanceTree(processInstance.getProcessDefinitionId()).activity("task1").activity("task2").done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
        .matches(describeExecutionTree(null).scope().child("task1").concurrent().noScope().up().child("task2").concurrent().noScope().done());

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

    runtimeService.createProcessInstanceModification(processInstance.getId()).startBeforeActivity("subProcess").execute();

    // when I start the inner subprocess task without explicit ancestor
    try {
      runtimeService.createProcessInstanceModification(processInstance.getId()).startTransition("flow5").execute();
      // then the command fails
      fail("should not succeed because the ancestors are ambiguous");
    } catch (ProcessEngineException e) {
      // happy path
    }

    // when I start the inner subprocess task with an explicit ancestor activity
    // instance id
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    ActivityInstance randomSubProcessInstance = getChildInstanceForActivity(updatedTree, "subProcess");

    // then the command suceeds
    runtimeService.createProcessInstanceModification(processInstanceId).startTransition("flow5", randomSubProcessInstance.getId()).execute();

    // and the trees are correct
    updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree)
        .hasStructure(describeActivityInstanceTree(processInstance.getProcessDefinitionId()).beginScope("subProcess").activity("subProcessTask").endScope()
            .beginScope("subProcess").activity("subProcessTask").beginScope("innerSubProcess").activity("innerSubProcessTask").done());

    ActivityInstance innerSubProcessInstance = getChildInstanceForActivity(updatedTree, "innerSubProcess");
    assertEquals(randomSubProcessInstance.getId(), innerSubProcessInstance.getParentActivityInstanceId());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree).matches(describeExecutionTree(null).scope().child(null).concurrent().noScope().child("subProcessTask").scope().up().up()
        .child(null).concurrent().noScope().child(null).scope().child("subProcessTask").concurrent().noScope().up().child(null).concurrent().noScope()
        .child("innerSubProcessTask").scope().done());

    assertEquals(3, taskService.createTaskQuery().count());

    // complete the process
    completeTasksInOrder("subProcessTask", "subProcessTask", "innerSubProcessTask", "innerSubProcessTask", "innerSubProcessTask");
    assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = DOUBLE_NESTED_SUB_PROCESS)
  public void testStartTransitionWithInvalidAncestorInstanceId() {
    // given two instances of the outer subprocess
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("doubleNestedSubprocess");
    String processInstanceId = processInstance.getId();

    try {
      runtimeService.createProcessInstanceModification(processInstance.getId()).startTransition("flow5", "noValidActivityInstanceId").execute();
      fail();
    } catch (NotValidException e) {
      // happy path
      assertTextPresent("Cannot perform instruction: " + "Start transition 'flow5' with ancestor activity instance 'noValidActivityInstanceId'; "
          + "Ancestor activity instance 'noValidActivityInstanceId' does not exist", e.getMessage());
    }

    try {
      runtimeService.createProcessInstanceModification(processInstance.getId()).startTransition("flow5", null).execute();
      fail();
    } catch (NotValidException e) {
      // happy path
      assertTextPresent("ancestorActivityInstanceId is null", e.getMessage());
    }

    ActivityInstance tree = runtimeService.getActivityInstance(processInstanceId);
    String subProcessTaskId = getInstanceIdForActivity(tree, "subProcessTask");

    try {
      runtimeService.createProcessInstanceModification(processInstance.getId()).startTransition("flow5", subProcessTaskId).execute();
      fail("should not succeed because subProcessTask is a child of subProcess");
    } catch (NotValidException e) {
      // happy path
      assertTextPresent("Cannot perform instruction: " + "Start transition 'flow5' with ancestor activity instance '" + subProcessTaskId + "'; "
          + "Scope execution for '" + subProcessTaskId + "' cannot be found in parent hierarchy of flow element 'flow5'", e.getMessage());
    }
  }

  @Deployment(resources = EXCLUSIVE_GATEWAY_PROCESS)
  public void testStartTransitionCase2() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exclusiveGateway");
    String processInstanceId = processInstance.getId();

    runtimeService.createProcessInstanceModification(processInstance.getId()).startTransition("flow2").execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(describeActivityInstanceTree(processInstance.getProcessDefinitionId()).activity("task1").activity("task1").done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
        .matches(describeExecutionTree(null).scope().child("task1").concurrent().noScope().up().child("task1").concurrent().noScope().done());

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
      runtimeService.createProcessInstanceModification(processInstanceId).startTransition("invalidFlowId").execute();

      fail("should not suceed");

    } catch (ProcessEngineException e) {
      // happy path
      assertTextPresent("Cannot perform instruction: " + "Start transition 'invalidFlowId'; " + "Element 'invalidFlowId' does not exist in process '"
          + processInstance.getProcessDefinitionId() + "'", e.getMessage());
    }
  }

  @Deployment(resources = EXCLUSIVE_GATEWAY_PROCESS)
  public void testStartAfter() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exclusiveGateway");
    String processInstanceId = processInstance.getId();

    runtimeService.createProcessInstanceModification(processInstance.getId()).startAfterActivity("theStart").execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(describeActivityInstanceTree(processInstance.getProcessDefinitionId()).activity("task1").activity("task1").done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
        .matches(describeExecutionTree(null).scope().child("task1").concurrent().noScope().up().child("task1").concurrent().noScope().done());

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

    runtimeService.createProcessInstanceModification(processInstance.getId()).startAfterActivity("theStart", tree.getId()).execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(describeActivityInstanceTree(processInstance.getProcessDefinitionId()).activity("task1").activity("task1").done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree)
        .matches(describeExecutionTree(null).scope().child("task1").concurrent().noScope().up().child("task1").concurrent().noScope().done());

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

    runtimeService.createProcessInstanceModification(processInstance.getId()).startBeforeActivity("subProcess").execute();

    // when I start the inner subprocess task without explicit ancestor
    try {
      runtimeService.createProcessInstanceModification(processInstance.getId()).startAfterActivity("innerSubProcessStart").execute();
      // then the command fails
      fail("should not succeed because the ancestors are ambiguous");
    } catch (ProcessEngineException e) {
      // happy path
    }

    // when I start the inner subprocess task with an explicit ancestor activity
    // instance id
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    ActivityInstance randomSubProcessInstance = getChildInstanceForActivity(updatedTree, "subProcess");

    // then the command suceeds
    runtimeService.createProcessInstanceModification(processInstanceId).startAfterActivity("innerSubProcessStart", randomSubProcessInstance.getId()).execute();

    // and the trees are correct
    updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree)
        .hasStructure(describeActivityInstanceTree(processInstance.getProcessDefinitionId()).beginScope("subProcess").activity("subProcessTask").endScope()
            .beginScope("subProcess").activity("subProcessTask").beginScope("innerSubProcess").activity("innerSubProcessTask").done());

    ActivityInstance innerSubProcessInstance = getChildInstanceForActivity(updatedTree, "innerSubProcess");
    assertEquals(randomSubProcessInstance.getId(), innerSubProcessInstance.getParentActivityInstanceId());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree).matches(describeExecutionTree(null).scope().child(null).concurrent().noScope().child("subProcessTask").scope().up().up()
        .child(null).concurrent().noScope().child(null).scope().child("subProcessTask").concurrent().noScope().up().child(null).concurrent().noScope()
        .child("innerSubProcessTask").scope().done());

    assertEquals(3, taskService.createTaskQuery().count());

    // complete the process
    completeTasksInOrder("subProcessTask", "subProcessTask", "innerSubProcessTask", "innerSubProcessTask", "innerSubProcessTask");
    assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = DOUBLE_NESTED_SUB_PROCESS)
  public void testStartAfterWithInvalidAncestorInstanceId() {
    // given two instances of the outer subprocess
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("doubleNestedSubprocess");
    String processInstanceId = processInstance.getId();

    try {
      runtimeService.createProcessInstanceModification(processInstance.getId()).startAfterActivity("innerSubProcessStart", "noValidActivityInstanceId")
          .execute();
      fail();
    } catch (NotValidException e) {
      // happy path
      assertTextPresent(
          "Cannot perform instruction: " + "Start after activity 'innerSubProcessStart' with ancestor activity instance 'noValidActivityInstanceId'; "
              + "Ancestor activity instance 'noValidActivityInstanceId' does not exist",
          e.getMessage());
    }

    try {
      runtimeService.createProcessInstanceModification(processInstance.getId()).startAfterActivity("innerSubProcessStart", null).execute();
      fail();
    } catch (NotValidException e) {
      // happy path
      assertTextPresent("ancestorActivityInstanceId is null", e.getMessage());
    }

    ActivityInstance tree = runtimeService.getActivityInstance(processInstanceId);
    String subProcessTaskId = getInstanceIdForActivity(tree, "subProcessTask");

    try {
      runtimeService.createProcessInstanceModification(processInstance.getId()).startAfterActivity("innerSubProcessStart", subProcessTaskId).execute();
      fail("should not succeed because subProcessTask is a child of subProcess");
    } catch (NotValidException e) {
      // happy path
      assertTextPresent("Cannot perform instruction: " + "Start after activity 'innerSubProcessStart' with ancestor activity instance '" + subProcessTaskId
          + "'; " + "Scope execution for '" + subProcessTaskId + "' cannot be found in parent hierarchy of flow element 'flow5'", e.getMessage());
    }
  }

  @Deployment(resources = EXCLUSIVE_GATEWAY_PROCESS)
  public void testStartAfterActivityAmbiguousTransitions() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exclusiveGateway");
    String processInstanceId = processInstance.getId();

    try {
      runtimeService.createProcessInstanceModification(processInstanceId).startAfterActivity("fork").execute();

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
      runtimeService.createProcessInstanceModification(processInstanceId).startAfterActivity("theEnd").execute();

      fail("should not suceed since 'theEnd' has no outgoing sequence flow");

    } catch (ProcessEngineException e) {
      // happy path
      assertTextPresent("activity has no outgoing sequence flow to take", e.getMessage());
    }
  }

  @Deployment(resources = EXCLUSIVE_GATEWAY_PROCESS)
  public void testStartAfterNonExistingActivity() {
    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("exclusiveGateway");

    try {
      // when
      runtimeService.createProcessInstanceModification(instance.getId()).startAfterActivity("someNonExistingActivity").execute();
      fail("should not succeed");
    } catch (NotValidException e) {
      // then
      assertTextPresentIgnoreCase("Cannot perform instruction: " + "Start after activity 'someNonExistingActivity'; "
          + "Activity 'someNonExistingActivity' does not exist: activity is null", e.getMessage());
    }
  }

  @Deployment(resources = ONE_SCOPE_TASK_PROCESS)
  public void testScopeTaskStartBefore() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String processInstanceId = processInstance.getId();

    runtimeService.createProcessInstanceModification(processInstance.getId()).startBeforeActivity("theTask").execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(describeActivityInstanceTree(processInstance.getProcessDefinitionId()).activity("theTask").activity("theTask").done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree).matches(describeExecutionTree(null).scope().child(null).concurrent().noScope().child("theTask").scope().up().up().child(null)
        .concurrent().noScope().child("theTask").scope().done());

    assertEquals(2, taskService.createTaskQuery().count());
    completeTasksInOrder("theTask", "theTask");
    assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = ONE_SCOPE_TASK_PROCESS)
  public void testScopeTaskStartAfter() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String processInstanceId = processInstance.getId();

    // when starting after the task, essentially nothing changes in the process
    // instance
    runtimeService.createProcessInstanceModification(processInstance.getId()).startAfterActivity("theTask").execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(describeActivityInstanceTree(processInstance.getProcessDefinitionId()).activity("theTask").done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree).matches(describeExecutionTree(null).scope().child("theTask").scope().done());

    // when starting after the start event, regular concurrency happens
    runtimeService.createProcessInstanceModification(processInstance.getId()).startAfterActivity("theStart").execute();

    updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(describeActivityInstanceTree(processInstance.getProcessDefinitionId()).activity("theTask").activity("theTask").done());

    executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree).matches(describeExecutionTree(null).scope().child(null).concurrent().noScope().child("theTask").scope().up().up().child(null)
        .concurrent().noScope().child("theTask").scope().done());

    completeTasksInOrder("theTask", "theTask");
    assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = SUBPROCESS_BOUNDARY_EVENTS_PROCESS)
  public void testStartBeforeEventSubscription() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("subprocess");

    runtimeService.createProcessInstanceModification(processInstance.getId()).startBeforeActivity("innerTask").execute();

    // then two timer jobs should have been created
    assertEquals(2, managementService.createJobQuery().count());
    Job innerJob = managementService.createJobQuery().activityId("innerTimer").singleResult();
    assertNotNull(innerJob);
    assertEquals(runtimeService.createExecutionQuery().activityId("innerTask").singleResult().getId(), innerJob.getExecutionId());

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

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("subprocess",
        Collections.<String, Object> singletonMap("listener", new RecorderExecutionListener()));

    String processInstanceId = processInstance.getId();

    assertTrue(RecorderExecutionListener.getRecordedEvents().isEmpty());

    runtimeService.createProcessInstanceModification(processInstance.getId()).startBeforeActivity("innerTask").execute();

    // assert activity instance tree
    ActivityInstance activityInstanceTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(activityInstanceTree);
    assertEquals(processInstanceId, activityInstanceTree.getProcessInstanceId());

    assertThat(activityInstanceTree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId()).activity("outerTask").beginScope("subProcess").activity("innerTask").done());

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

    runtimeService.createProcessInstanceModification(processInstance.getId()).cancelActivityInstance(innerTaskInstance.getId()).execute();

    assertEquals(2, RecorderExecutionListener.getRecordedEvents().size());
  }

  @Deployment(resources = SUBPROCESS_LISTENER_PROCESS)
  public void testSkipListenerInvocation() {
    RecorderExecutionListener.clear();

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("subprocess",
        Collections.<String, Object> singletonMap("listener", new RecorderExecutionListener()));

    String processInstanceId = processInstance.getId();

    assertTrue(RecorderExecutionListener.getRecordedEvents().isEmpty());

    // when I start an activity with "skip listeners" setting
    runtimeService.createProcessInstanceModification(processInstanceId).startBeforeActivity("innerTask").execute(true, false);

    // then no listeners are invoked
    assertTrue(RecorderExecutionListener.getRecordedEvents().isEmpty());

    // when I cancel an activity with "skip listeners" setting
    ActivityInstance activityInstanceTree = runtimeService.getActivityInstance(processInstanceId);

    runtimeService.createProcessInstanceModification(processInstance.getId())
        .cancelActivityInstance(getChildInstanceForActivity(activityInstanceTree, "innerTask").getId()).execute(true, false);

    // then no listeners are invoked
    assertTrue(RecorderExecutionListener.getRecordedEvents().isEmpty());

    // when I cancel an activity that ends the process instance
    runtimeService.createProcessInstanceModification(processInstance.getId())
        .cancelActivityInstance(getChildInstanceForActivity(activityInstanceTree, "outerTask").getId()).execute(true, false);

    // then no listeners are invoked
    assertTrue(RecorderExecutionListener.getRecordedEvents().isEmpty());
  }

  @Deployment(resources = TASK_LISTENER_PROCESS)
  public void testSkipTaskListenerInvocation() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskListenerProcess",
        Collections.<String, Object> singletonMap("listener", new RecorderTaskListener()));

    String processInstanceId = processInstance.getId();

    RecorderTaskListener.clear();

    // when I start an activity with "skip listeners" setting
    runtimeService.createProcessInstanceModification(processInstanceId).startBeforeActivity("task").execute(true, false);

    // then no listeners are invoked
    assertTrue(RecorderTaskListener.getRecordedEvents().isEmpty());

    // when I cancel an activity with "skip listeners" setting
    ActivityInstance activityInstanceTree = runtimeService.getActivityInstance(processInstanceId);

    runtimeService.createProcessInstanceModification(processInstance.getId())
        .cancelActivityInstance(getChildInstanceForActivity(activityInstanceTree, "task").getId()).execute(true, false);

    // then no listeners are invoked
    assertTrue(RecorderTaskListener.getRecordedEvents().isEmpty());
  }

  @Deployment(resources = IO_MAPPING_PROCESS)
  public void testSkipIoMappings() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("ioMappingProcess");

    // when I start task2
    runtimeService.createProcessInstanceModification(processInstance.getId()).startBeforeActivity("task2").execute(false, true);

    // then the input mapping should not have executed
    Execution task2Execution = runtimeService.createExecutionQuery().activityId("task2").singleResult();
    assertNotNull(task2Execution);

    assertNull(runtimeService.getVariable(task2Execution.getId(), "inputMappingExecuted"));

    // when I cancel task2
    runtimeService.createProcessInstanceModification(processInstance.getId()).cancelAllForActivity("task2").execute(false, true);

    // then the output mapping should not have executed
    assertNull(runtimeService.getVariable(processInstance.getId(), "outputMappingExecuted"));
  }

  @Deployment(resources = IO_MAPPING_ON_SUB_PROCESS)
  public void testSkipIoMappingsOnSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    runtimeService.createProcessInstanceModification(processInstance.getId()).startBeforeActivity("boundaryEvent").execute(false, true);

    // then the output mapping should not have executed
    assertNull(runtimeService.getVariable(processInstance.getId(), "outputMappingExecuted"));
  }

  /**
   * should also skip io mappings that are defined on already instantiated
   * ancestor scopes and that may be executed due to the ancestor scope
   * completing within the modification command.
   */
  @Deployment(resources = IO_MAPPING_ON_SUB_PROCESS_AND_NESTED_SUB_PROCESS)
  public void testSkipIoMappingsOnSubProcessNested() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    runtimeService.createProcessInstanceModification(processInstance.getId()).startBeforeActivity("boundaryEvent").execute(false, true);

    // then the output mapping should not have executed
    assertNull(runtimeService.getVariable(processInstance.getId(), "outputMappingExecuted"));
  }

  @Deployment(resources = LISTENERS_ON_SUB_PROCESS_AND_NESTED_SUB_PROCESS)
  public void testSkipListenersOnSubProcessNested() {
    RecorderExecutionListener.clear();

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process",
        Variables.createVariables().putValue("listener", new RecorderExecutionListener()));

    runtimeService.createProcessInstanceModification(processInstance.getId()).startBeforeActivity("boundaryEvent").execute(true, false);

    assertProcessEnded(processInstance.getId());

    // then the output mapping should not have executed
    assertTrue(RecorderExecutionListener.getRecordedEvents().isEmpty());
  }

  @Deployment(resources = TRANSITION_LISTENER_PROCESS)
  public void testStartTransitionListenerInvocation() {
    RecorderExecutionListener.clear();

    ProcessInstance instance = runtimeService.startProcessInstanceByKey("transitionListenerProcess",
        Variables.createVariables().putValue("listener", new RecorderExecutionListener()));

    runtimeService.createProcessInstanceModification(instance.getId()).startTransition("flow2").execute();

    // transition listener should have been invoked
    List<RecordedEvent> events = RecorderExecutionListener.getRecordedEvents();
    assertEquals(1, events.size());

    RecordedEvent event = events.get(0);
    assertEquals("flow2", event.getTransitionId());

    RecorderExecutionListener.clear();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(instance.getId());
    assertNotNull(updatedTree);
    assertEquals(instance.getId(), updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(describeActivityInstanceTree(instance.getProcessDefinitionId()).activity("task1").activity("task2").done());

    ExecutionTree executionTree = ExecutionTree.forExecution(instance.getId(), processEngine);

    assertThat(executionTree)
        .matches(describeExecutionTree(null).scope().child("task1").concurrent().noScope().up().child("task2").concurrent().noScope().done());

    completeTasksInOrder("task1", "task2", "task2");
    assertProcessEnded(instance.getId());
  }

  @Deployment(resources = TRANSITION_LISTENER_PROCESS)
  public void testStartAfterActivityListenerInvocation() {
    RecorderExecutionListener.clear();

    ProcessInstance instance = runtimeService.startProcessInstanceByKey("transitionListenerProcess",
        Variables.createVariables().putValue("listener", new RecorderExecutionListener()));

    runtimeService.createProcessInstanceModification(instance.getId()).startTransition("flow2").execute();

    // transition listener should have been invoked
    List<RecordedEvent> events = RecorderExecutionListener.getRecordedEvents();
    assertEquals(1, events.size());

    RecordedEvent event = events.get(0);
    assertEquals("flow2", event.getTransitionId());

    RecorderExecutionListener.clear();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(instance.getId());
    assertNotNull(updatedTree);
    assertEquals(instance.getId(), updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(describeActivityInstanceTree(instance.getProcessDefinitionId()).activity("task1").activity("task2").done());

    ExecutionTree executionTree = ExecutionTree.forExecution(instance.getId(), processEngine);

    assertThat(executionTree)
        .matches(describeExecutionTree(null).scope().child("task1").concurrent().noScope().up().child("task2").concurrent().noScope().done());

    completeTasksInOrder("task1", "task2", "task2");
    assertProcessEnded(instance.getId());
  }

  @Deployment(resources = EXCLUSIVE_GATEWAY_PROCESS)
  public void testStartBeforeWithVariables() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exclusiveGateway");

    runtimeService.createProcessInstanceModification(processInstance.getId()).startBeforeActivity("task2").setVariable("procInstVar", "procInstValue")
        .setVariableLocal("localVar", "localValue").setVariables(Variables.createVariables().putValue("procInstMapVar", "procInstMapValue"))
        .setVariablesLocal(Variables.createVariables().putValue("localMapVar", "localMapValue")).execute();

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstance.getId());
    assertNotNull(updatedTree);
    assertThat(updatedTree).hasStructure(describeActivityInstanceTree(processInstance.getProcessDefinitionId()).activity("task1").activity("task2").done());

    ActivityInstance task2Instance = getChildInstanceForActivity(updatedTree, "task2");
    assertNotNull(task2Instance);
    assertEquals(1, task2Instance.getExecutionIds().length);
    String task2ExecutionId = task2Instance.getExecutionIds()[0];

    assertEquals(4, runtimeService.createVariableInstanceQuery().count());
    assertEquals("procInstValue", runtimeService.getVariableLocal(processInstance.getId(), "procInstVar"));
    assertEquals("localValue", runtimeService.getVariableLocal(task2ExecutionId, "localVar"));
    assertEquals("procInstMapValue", runtimeService.getVariableLocal(processInstance.getId(), "procInstMapVar"));
    assertEquals("localMapValue", runtimeService.getVariableLocal(task2ExecutionId, "localMapVar"));

    completeTasksInOrder("task1", "task2");
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = EXCLUSIVE_GATEWAY_PROCESS)
  public void testCancellationAndStartBefore() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exclusiveGateway");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService.createProcessInstanceModification(processInstance.getId()).cancelActivityInstance(getInstanceIdForActivity(tree, "task1"))
        .startBeforeActivity("task2").execute();

    ActivityInstance activityInstanceTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(activityInstanceTree);
    assertEquals(processInstanceId, activityInstanceTree.getProcessInstanceId());

    assertThat(activityInstanceTree).hasStructure(describeActivityInstanceTree(processInstance.getProcessDefinitionId()).activity("task2").done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree).matches(describeExecutionTree("task2").scope().done());

    completeTasksInOrder("task2");
    assertProcessEnded(processInstanceId);
  }

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
    runtimeService.createProcessInstanceModification(processInstance.getId()).cancelActivityInstance(getInstanceIdForActivity(tree, "innerTask2")).execute();

    // then the innerTask compensation should be removed
    assertEquals(0, runtimeService.createEventSubscriptionQuery().count());
  }

  @Deployment
  public void testCompensationCreation() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensationProcess");

    runtimeService.createProcessInstanceModification(processInstance.getId()).startBeforeActivity("innerTask").execute();

    Execution task2Execution = runtimeService.createExecutionQuery().activityId("innerTask").singleResult();
    Task task = taskService.createTaskQuery().executionId(task2Execution.getId()).singleResult();
    assertNotNull(task);

    taskService.complete(task.getId());
    assertEquals(3, runtimeService.createEventSubscriptionQuery().count());

    // trigger compensation
    Task outerTask = taskService.createTaskQuery().taskDefinitionKey("outerTask").singleResult();
    assertNotNull(outerTask);
    taskService.complete(outerTask.getId());

    // then there are two compensation tasks and the afterSubprocessTask:
    assertEquals(3, taskService.createTaskQuery().count());
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey("innerAfterBoundaryTask").count());
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey("outerAfterBoundaryTask").count());
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey("taskAfterSubprocess").count());

    // complete process
    completeTasksInOrder("taskAfterSubprocess", "innerAfterBoundaryTask", "outerAfterBoundaryTask");

    assertProcessEnded(processInstance.getId());
  }

  @Deployment
  public void testNoCompensationCreatedOnCancellation() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensationProcess");
    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    // one on outerTask, one on innerTask
    assertEquals(2, taskService.createTaskQuery().count());

    // when inner task is cancelled
    runtimeService.createProcessInstanceModification(processInstance.getId()).cancelActivityInstance(getInstanceIdForActivity(tree, "innerTask")).execute();

    // then no compensation event subscription exists
    assertEquals(0, runtimeService.createEventSubscriptionQuery().count());

    // and the compensation throw event does not trigger compensation handlers
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    assertEquals("outerTask", task.getTaskDefinitionKey());

    taskService.complete(task.getId());

    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = TRANSACTION_WITH_COMPENSATION_PROCESS)
  public void testStartActivityInTransactionWithCompensation() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    completeTasksInOrder("userTask");

    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("undoTask", task.getTaskDefinitionKey());

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree)
        .hasStructure(describeActivityInstanceTree(processInstance.getProcessDefinitionId()).beginScope("tx").activity("txEnd").activity("undoTask").done());

    runtimeService.createProcessInstanceModification(processInstance.getId()).startBeforeActivity("userTask").execute();

    tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(describeActivityInstanceTree(processInstance.getProcessDefinitionId()).beginScope("tx").activity("txEnd").activity("undoTask")
        .activity("userTask").done());

    completeTasksInOrder("userTask");

    tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree)
        .hasStructure(describeActivityInstanceTree(processInstance.getProcessDefinitionId()).beginScope("tx").activity("txEnd").activity("undoTask").done());

    Task newTask = taskService.createTaskQuery().singleResult();
    assertNotSame(task.getId(), newTask.getId());

    completeTasksInOrder("undoTask", "afterCancel");
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = TRANSACTION_WITH_COMPENSATION_PROCESS)
  public void testStartActivityWithAncestorInTransactionWithCompensation() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    completeTasksInOrder("userTask");

    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("undoTask", task.getTaskDefinitionKey());

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree)
        .hasStructure(describeActivityInstanceTree(processInstance.getProcessDefinitionId()).beginScope("tx").activity("txEnd").activity("undoTask").done());

    runtimeService.createProcessInstanceModification(processInstance.getId()).startBeforeActivity("userTask", processInstance.getId()).execute();

    completeTasksInOrder("userTask");

    tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(describeActivityInstanceTree(processInstance.getProcessDefinitionId()).beginScope("tx").activity("txEnd").activity("undoTask")
        .endScope().beginScope("tx").activity("txEnd").activity("undoTask").done());

    completeTasksInOrder("undoTask", "undoTask", "afterCancel", "afterCancel");
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = TRANSACTION_WITH_COMPENSATION_PROCESS)
  public void testStartAfterActivityDuringCompensation() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    completeTasksInOrder("userTask");

    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("undoTask", task.getTaskDefinitionKey());

    runtimeService.createProcessInstanceModification(processInstance.getId()).startAfterActivity("userTask").execute();

    task = taskService.createTaskQuery().singleResult();
    assertEquals("afterCancel", task.getTaskDefinitionKey());

    completeTasksInOrder("afterCancel");
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = TRANSACTION_WITH_COMPENSATION_PROCESS)
  public void testCancelCompensatingTask() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");
    completeTasksInOrder("userTask");

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService.createProcessInstanceModification(processInstance.getId()).cancelActivityInstance(getInstanceIdForActivity(tree, "undoTask")).execute();

    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = TRANSACTION_WITH_COMPENSATION_PROCESS)
  public void testCancelCompensatingTaskAndStartActivity() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");
    completeTasksInOrder("userTask");

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService.createProcessInstanceModification(processInstance.getId()).cancelActivityInstance(getInstanceIdForActivity(tree, "undoTask"))
        .startBeforeActivity("userTask").execute();

    tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(describeActivityInstanceTree(processInstance.getProcessDefinitionId()).beginScope("tx").activity("userTask").done());

    completeTasksInOrder("userTask", "undoTask", "afterCancel");

    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = TRANSACTION_WITH_COMPENSATION_PROCESS)
  public void testCancelCompensatingTaskAndStartActivityWithAncestor() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");
    completeTasksInOrder("userTask");

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService.createProcessInstanceModification(processInstance.getId()).cancelActivityInstance(getInstanceIdForActivity(tree, "undoTask"))
        .startBeforeActivity("userTask", processInstance.getId()).execute();

    tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(describeActivityInstanceTree(processInstance.getProcessDefinitionId()).beginScope("tx").activity("userTask").done());

    completeTasksInOrder("userTask", "undoTask", "afterCancel");

    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = TRANSACTION_WITH_COMPENSATION_PROCESS)
  public void testStartActivityAndCancelCompensatingTask() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");
    completeTasksInOrder("userTask");

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService.createProcessInstanceModification(processInstance.getId()).startBeforeActivity("userTask")
        .cancelActivityInstance(getInstanceIdForActivity(tree, "undoTask")).execute();

    tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(describeActivityInstanceTree(processInstance.getProcessDefinitionId()).beginScope("tx").activity("userTask").done());

    completeTasksInOrder("userTask", "undoTask", "afterCancel");

    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = TRANSACTION_WITH_COMPENSATION_PROCESS)
  public void testStartCompensatingTask() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    runtimeService.createProcessInstanceModification(processInstance.getId()).startBeforeActivity("undoTask").execute();

    completeTasksInOrder("undoTask");

    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("userTask", task.getTaskDefinitionKey());

    completeTasksInOrder("userTask", "undoTask", "afterCancel");

    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = TRANSACTION_WITH_COMPENSATION_PROCESS)
  public void testStartAdditionalCompensatingTaskAndCompleteOldCompensationTask() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");
    completeTasksInOrder("userTask");

    Task firstUndoTask = taskService.createTaskQuery().singleResult();

    runtimeService.createProcessInstanceModification(processInstance.getId()).startBeforeActivity("undoTask").execute();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(describeActivityInstanceTree(processInstance.getProcessDefinitionId()).beginScope("tx").activity("txEnd").activity("undoTask")
        .activity("undoTask").done());

    taskService.complete(firstUndoTask.getId());

    Task secondUndoTask = taskService.createTaskQuery().taskDefinitionKey("undoTask").singleResult();
    assertNull(secondUndoTask);

    completeTasksInOrder("afterCancel");

    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = TRANSACTION_WITH_COMPENSATION_PROCESS)
  public void testStartAdditionalCompensatingTaskAndCompleteNewCompensatingTask() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");
    completeTasksInOrder("userTask");

    Task firstUndoTask = taskService.createTaskQuery().taskDefinitionKey("undoTask").singleResult();

    runtimeService.createProcessInstanceModification(processInstance.getId()).startBeforeActivity("undoTask").setVariableLocal("new", true).execute();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(describeActivityInstanceTree(processInstance.getProcessDefinitionId()).beginScope("tx").activity("txEnd").activity("undoTask")
        .activity("undoTask").done());

    String taskExecutionId = runtimeService.createExecutionQuery().variableValueEquals("new", true).singleResult().getId();
    Task secondUndoTask = taskService.createTaskQuery().executionId(taskExecutionId).singleResult();

    assertNotNull(secondUndoTask);
    assertNotSame(firstUndoTask.getId(), secondUndoTask.getId());
    taskService.complete(secondUndoTask.getId());

    tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree)
        .hasStructure(describeActivityInstanceTree(processInstance.getProcessDefinitionId()).beginScope("tx").activity("txEnd").activity("undoTask").done());

    completeTasksInOrder("undoTask", "afterCancel");

    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = TRANSACTION_WITH_COMPENSATION_PROCESS)
  public void testStartCompensationBoundary() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    try {
      runtimeService.createProcessInstanceModification(processInstance.getId()).startBeforeActivity("compensateBoundaryEvent").execute();

      fail("should not succeed");
    } catch (ProcessEngineException e) {
      assertTextPresent("compensation boundary event", e.getMessage());
    }

    try {
      runtimeService.createProcessInstanceModification(processInstance.getId()).startAfterActivity("compensateBoundaryEvent").execute();

      fail("should not succeed");
    } catch (ProcessEngineException e) {
      assertTextPresent("no outgoing sequence flow", e.getMessage());
    }
  }

  @Deployment(resources = TRANSACTION_WITH_COMPENSATION_PROCESS)
  public void testStartCancelEndEvent() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");
    completeTasksInOrder("userTask");

    runtimeService.createProcessInstanceModification(processInstance.getId()).startBeforeActivity("txEnd").execute();

    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("afterCancel", task.getTaskDefinitionKey());

    taskService.complete(task.getId());

    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = TRANSACTION_WITH_COMPENSATION_PROCESS)
  public void testStartCancelBoundaryEvent() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");
    completeTasksInOrder("userTask");

    runtimeService.createProcessInstanceModification(processInstance.getId()).startBeforeActivity("catchCancelTx").execute();

    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("afterCancel", task.getTaskDefinitionKey());

    taskService.complete(task.getId());

    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = TRANSACTION_WITH_COMPENSATION_PROCESS)
  public void testStartTaskAfterCancelBoundaryEvent() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");
    completeTasksInOrder("userTask");

    runtimeService.createProcessInstanceModification(processInstance.getId()).startBeforeActivity("afterCancel").execute();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(describeActivityInstanceTree(processInstance.getProcessDefinitionId()).beginScope("tx").activity("txEnd").activity("undoTask")
        .endScope().activity("afterCancel").done());

    completeTasksInOrder("afterCancel", "undoTask", "afterCancel");

    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = EXCLUSIVE_GATEWAY_PROCESS)
  public void testCancelNonExistingActivityInstance() {
    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("exclusiveGateway");

    // when - then throw exception
    try {
      runtimeService.createProcessInstanceModification(instance.getId()).cancelActivityInstance("nonExistingActivityInstance").execute();
      fail("should not succeed");
    } catch (NotValidException e) {
      assertTextPresent("Cannot perform instruction: Cancel activity instance 'nonExistingActivityInstance'; "
          + "Activity instance 'nonExistingActivityInstance' does not exist", e.getMessage());
    }

  }

  @Deployment(resources = EXCLUSIVE_GATEWAY_PROCESS)
  public void testCancelNonExistingTranisitionInstance() {
    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("exclusiveGateway");

    // when - then throw exception
    try {
      runtimeService.createProcessInstanceModification(instance.getId()).cancelTransitionInstance("nonExistingActivityInstance").execute();
      fail("should not succeed");
    } catch (NotValidException e) {
      assertTextPresent("Cannot perform instruction: Cancel transition instance 'nonExistingActivityInstance'; "
          + "Transition instance 'nonExistingActivityInstance' does not exist", e.getMessage());
    }

  }

  @Deployment(resources = { CALL_ACTIVITY_PARENT_PROCESS, CALL_ACTIVITY_CHILD_PROCESS })
  public void FAILING_testCancelCallActivityInstance() {
    // given
    ProcessInstance parentprocess = runtimeService.startProcessInstanceByKey("parentprocess");
    ProcessInstance subProcess = runtimeService.createProcessInstanceQuery().processDefinitionKey("subprocess").singleResult();

    ActivityInstance subProcessActivityInst = runtimeService.getActivityInstance(subProcess.getId());

    // when
    runtimeService.createProcessInstanceModification(subProcess.getId()).startBeforeActivity("childEnd", subProcess.getId())
        .cancelActivityInstance(getInstanceIdForActivity(subProcessActivityInst, "innerTask")).execute();

    // then
    assertProcessEnded(parentprocess.getId());
  }

  public void testModifyNullProcessInstance() {
    try {
      runtimeService.createProcessInstanceModification(null).startBeforeActivity("someActivity").execute();
      fail("should not succeed");
    } catch (NotValidException e) {
      assertTextPresent("processInstanceId is null", e.getMessage());
    }
  }

  // TODO: check if starting with a non-existing activity/transition id is
  // handled properly

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
