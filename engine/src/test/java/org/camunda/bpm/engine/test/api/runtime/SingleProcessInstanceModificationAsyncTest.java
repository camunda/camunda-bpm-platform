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
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.ProcessDefinition;
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
import org.junit.After;

/**
 * @author Yana Vasileva
 *
 */
public class SingleProcessInstanceModificationAsyncTest extends PluggableProcessEngineTestCase {

  protected static final String PARALLEL_GATEWAY_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.parallelGateway.bpmn20.xml";
  protected static final String EXCLUSIVE_GATEWAY_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.exclusiveGateway.bpmn20.xml";
  protected static final String SUBPROCESS_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.subprocess.bpmn20.xml";
  protected static final String ONE_SCOPE_TASK_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.oneScopeTaskProcess.bpmn20.xml";
  protected static final String TRANSITION_LISTENER_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.transitionListeners.bpmn20.xml";
  protected static final String TASK_LISTENER_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.taskListeners.bpmn20.xml";
  protected static final String IO_MAPPING_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.ioMapping.bpmn20.xml";
  protected static final String CALL_ACTIVITY_PARENT_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.testCancelCallActivityParentProcess.bpmn";
  protected static final String CALL_ACTIVITY_CHILD_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.testCancelCallActivityChildProcess.bpmn";

  @After
  public void tearDown() {
    List<Batch> batches = managementService.createBatchQuery().list();
    for (Batch batch : batches) {
      managementService.deleteBatch(batch.getId(), true);
    }

    List<Job> jobs = managementService.createJobQuery().list();
    for (Job job : jobs) {
      managementService.deleteJob(job.getId());
    }
  }

  @Deployment(resources = PARALLEL_GATEWAY_PROCESS)
  public void testTheDeploymentIdIsSet() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("parallelGateway");
    String processDefinitionId = processInstance.getProcessDefinitionId();
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    Batch modificationBatch = runtimeService
        .createProcessInstanceModification(processInstance.getId())
        .cancelActivityInstance(getInstanceIdForActivity(tree, "task1"))
        .executeAsync();
    assertNotNull(modificationBatch);
    Job job = managementService.createJobQuery().jobDefinitionId(modificationBatch.getSeedJobDefinitionId()).singleResult();
    // seed job
    managementService.executeJob(job.getId());

    for (Job pending : managementService.createJobQuery().jobDefinitionId(modificationBatch.getBatchJobDefinitionId()).list()) {
      managementService.executeJob(pending.getId());
      assertEquals(processDefinition.getDeploymentId(), pending.getDeploymentId());
    }
  }

  @Deployment(resources = PARALLEL_GATEWAY_PROCESS)
  public void testCancellation() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("parallelGateway");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    Batch modificationBatch = runtimeService
        .createProcessInstanceModification(processInstance.getId())
        .cancelActivityInstance(getInstanceIdForActivity(tree, "task1"))
        .executeAsync();
    assertNotNull(modificationBatch);
    executeSeedAndBatchJobs(modificationBatch);

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

    Batch modificationBatch = runtimeService.createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "task1"))
      .cancelActivityInstance(getInstanceIdForActivity(tree, "task2"))
      .executeAsync();
    assertNotNull(modificationBatch);
    executeSeedAndBatchJobs(modificationBatch);

    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = PARALLEL_GATEWAY_PROCESS)
  public void testCancellationWithWrongProcessInstanceId() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("parallelGateway");

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    try {
      Batch modificationBatch = runtimeService.createProcessInstanceModification("foo")
        .cancelActivityInstance(getInstanceIdForActivity(tree, "task1"))
        .cancelActivityInstance(getInstanceIdForActivity(tree, "task2"))
        .executeAsync();
      assertNotNull(modificationBatch);
      executeSeedAndBatchJobs(modificationBatch);
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

    Batch modificationBatch = runtimeService
        .createProcessInstanceModification(processInstance.getId())
        .startBeforeActivity("task2")
        .executeAsync();
    assertNotNull(modificationBatch);
    executeSeedAndBatchJobs(modificationBatch);

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

    Batch modificationBatch = runtimeService
        .createProcessInstanceModification(processInstance.getId())
        .startBeforeActivity("task2", tree.getId())
        .executeAsync();
    assertNotNull(modificationBatch);
    executeSeedAndBatchJobs(modificationBatch);

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
  public void testStartBeforeNonExistingActivity() {
    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("exclusiveGateway");

    try {
      // when
      Batch modificationBatch = runtimeService.createProcessInstanceModification(instance.getId()).startBeforeActivity("someNonExistingActivity").executeAsync();
      assertNotNull(modificationBatch);
      executeSeedAndBatchJobs(modificationBatch);
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

    Batch modificationBatch = runtimeService.createProcessInstanceModification(processInstance.getId())
        .cancelActivityInstance(getInstanceIdForActivity(tree, "task1"))
        .startAfterActivity("task1")
        .startBeforeActivity("task1")
        .executeAsync();
    assertNotNull(modificationBatch);
    executeSeedAndBatchJobs(modificationBatch);

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

    Batch modificationBatch = runtimeService
        .createProcessInstanceModification(processInstance.getId())
        .startTransition("flow4")
        .executeAsync();
    assertNotNull(modificationBatch);
    executeSeedAndBatchJobs(modificationBatch);

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

    Batch modificationBatch = runtimeService
        .createProcessInstanceModification(processInstance.getId())
        .startTransition("flow4", tree.getId())
        .executeAsync();
    assertNotNull(modificationBatch);
    executeSeedAndBatchJobs(modificationBatch);

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
  public void testStartTransitionInvalidTransitionId() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exclusiveGateway");
    String processInstanceId = processInstance.getId();

    try {
      Batch modificationBatch = runtimeService.createProcessInstanceModification(processInstanceId).startTransition("invalidFlowId").executeAsync();
      assertNotNull(modificationBatch);
      executeSeedAndBatchJobs(modificationBatch);

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

    Batch modificationBatch = runtimeService
        .createProcessInstanceModification(processInstance.getId())
        .startAfterActivity("theStart")
        .executeAsync();
    assertNotNull(modificationBatch);
    executeSeedAndBatchJobs(modificationBatch);

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

    Batch modificationBatch = runtimeService
        .createProcessInstanceModification(processInstance.getId())
        .startAfterActivity("theStart", tree.getId())
        .executeAsync();
    assertNotNull(modificationBatch);
    executeSeedAndBatchJobs(modificationBatch);

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
  public void testStartAfterActivityAmbiguousTransitions() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exclusiveGateway");
    String processInstanceId = processInstance.getId();

    try {
      Batch modificationBatch = runtimeService
          .createProcessInstanceModification(processInstanceId)
          .startAfterActivity("fork")
          .executeAsync();
      assertNotNull(modificationBatch);
      executeSeedAndBatchJobs(modificationBatch);
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
      Batch modificationBatch = runtimeService
          .createProcessInstanceModification(processInstanceId)
          .startAfterActivity("theEnd")
          .executeAsync();
      assertNotNull(modificationBatch);
      executeSeedAndBatchJobs(modificationBatch);
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
      Batch modificationBatch = runtimeService
          .createProcessInstanceModification(instance.getId())
          .startAfterActivity("someNonExistingActivity")
          .executeAsync();
      assertNotNull(modificationBatch);
      executeSeedAndBatchJobs(modificationBatch);
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

    Batch modificationBatch = runtimeService
        .createProcessInstanceModification(processInstance.getId())
        .startBeforeActivity("theTask")
        .executeAsync();
    assertNotNull(modificationBatch);
    executeSeedAndBatchJobs(modificationBatch);

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
    Batch modificationBatch = runtimeService
        .createProcessInstanceModification(processInstance.getId())
        .startAfterActivity("theTask")
        .executeAsync();
    assertNotNull(modificationBatch);
    executeSeedAndBatchJobs(modificationBatch);

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(describeActivityInstanceTree(processInstance.getProcessDefinitionId()).activity("theTask").done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree).matches(describeExecutionTree(null).scope().child("theTask").scope().done());

    // when starting after the start event, regular concurrency happens
    Batch modificationBatch2 = runtimeService.createProcessInstanceModification(processInstance.getId()).startAfterActivity("theStart").executeAsync();
    assertNotNull(modificationBatch2);
    executeSeedAndBatchJobs(modificationBatch2);

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

  @Deployment(resources = TASK_LISTENER_PROCESS)
  public void testSkipTaskListenerInvocation() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskListenerProcess", "brum",
        Collections.<String, Object> singletonMap("listener", new RecorderTaskListener()));

    String processInstanceId = processInstance.getId();

    RecorderTaskListener.clear();

    // when I start an activity with "skip listeners" setting
    Batch modificationBatch =  runtimeService
        .createProcessInstanceModification(processInstanceId)
        .startBeforeActivity("task")
        .executeAsync(true, false);
    assertNotNull(modificationBatch);
    executeSeedAndBatchJobs(modificationBatch);

    // then no listeners are invoked
    assertTrue(RecorderTaskListener.getRecordedEvents().isEmpty());

    // when I cancel an activity with "skip listeners" setting
    ActivityInstance activityInstanceTree = runtimeService.getActivityInstance(processInstanceId);

    Batch batch = runtimeService
        .createProcessInstanceModification(processInstance.getId())
        .cancelActivityInstance(getChildInstanceForActivity(activityInstanceTree, "task").getId())
        .executeAsync(true, false);
    assertNotNull(batch);
    executeSeedAndBatchJobs(batch);

    // then no listeners are invoked
    assertTrue(RecorderTaskListener.getRecordedEvents().isEmpty());
  }

  @Deployment(resources = IO_MAPPING_PROCESS)
  public void testSkipIoMappings() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("ioMappingProcess");

    // when I start task2
    Batch modificationBatch = runtimeService
        .createProcessInstanceModification(processInstance.getId())
        .startBeforeActivity("task2")
        .executeAsync(false, true);
    assertNotNull(modificationBatch);
    executeSeedAndBatchJobs(modificationBatch);

    // then the input mapping should not have executed
    Execution task2Execution = runtimeService.createExecutionQuery().activityId("task2").singleResult();
    assertNotNull(task2Execution);

    assertNull(runtimeService.getVariable(task2Execution.getId(), "inputMappingExecuted"));

    // when I cancel task2
    Batch modificationBatch2 = runtimeService.createProcessInstanceModification(processInstance.getId()).cancelAllForActivity("task2").executeAsync(false, true);
    assertNotNull(modificationBatch2);
    executeSeedAndBatchJobs(modificationBatch2);

    // then the output mapping should not have executed
    assertNull(runtimeService.getVariable(processInstance.getId(), "outputMappingExecuted"));
  }

  @Deployment(resources = TRANSITION_LISTENER_PROCESS)
  public void testStartTransitionListenerInvocation() {
    RecorderExecutionListener.clear();

    ProcessInstance instance = runtimeService.startProcessInstanceByKey("transitionListenerProcess",
        Variables.createVariables().putValue("listener", new RecorderExecutionListener()));

    Batch modificationBatch = runtimeService
        .createProcessInstanceModification(instance.getId())
        .startTransition("flow2")
        .executeAsync();
    assertNotNull(modificationBatch);
    executeSeedAndBatchJobs(modificationBatch);

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

    Batch modificationBatch = runtimeService
        .createProcessInstanceModification(instance.getId())
        .startTransition("flow2")
        .executeAsync();
    assertNotNull(modificationBatch);
    executeSeedAndBatchJobs(modificationBatch);

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
  public void testCancellationAndStartBefore() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exclusiveGateway");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    Batch modificationBatch = runtimeService.createProcessInstanceModification(processInstance.getId())
        .cancelActivityInstance(getInstanceIdForActivity(tree, "task1"))
        .startBeforeActivity("task2")
        .executeAsync();
    assertNotNull(modificationBatch);
    executeSeedAndBatchJobs(modificationBatch);

    ActivityInstance activityInstanceTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(activityInstanceTree);
    assertEquals(processInstanceId, activityInstanceTree.getProcessInstanceId());

    assertThat(activityInstanceTree).hasStructure(describeActivityInstanceTree(processInstance.getProcessDefinitionId()).activity("task2").done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree).matches(describeExecutionTree("task2").scope().done());

    completeTasksInOrder("task2");
    assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = EXCLUSIVE_GATEWAY_PROCESS)
  public void testCancelNonExistingActivityInstance() {
    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("exclusiveGateway");

    // when - then throw exception
    try {
      Batch modificationBatch = runtimeService
          .createProcessInstanceModification(instance.getId())
          .cancelActivityInstance("nonExistingActivityInstance")
          .executeAsync();
      assertNotNull(modificationBatch);
      executeSeedAndBatchJobs(modificationBatch);
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
      Batch modificationBatch = runtimeService
          .createProcessInstanceModification(instance.getId())
          .cancelTransitionInstance("nonExistingActivityInstance")
          .executeAsync();
      assertNotNull(modificationBatch);
      executeSeedAndBatchJobs(modificationBatch);
      fail("should not succeed");
    } catch (NotValidException e) {
      assertTextPresent("Cannot perform instruction: Cancel transition instance 'nonExistingActivityInstance'; "
          + "Transition instance 'nonExistingActivityInstance' does not exist", e.getMessage());
    }

  }

  @Deployment(resources = { CALL_ACTIVITY_PARENT_PROCESS, CALL_ACTIVITY_CHILD_PROCESS })
  public void testCancelCallActivityInstance() {
    // given
    ProcessInstance parentprocess = runtimeService.startProcessInstanceByKey("parentprocess");
    ProcessInstance subProcess = runtimeService.createProcessInstanceQuery().processDefinitionKey("subprocess").singleResult();

    ActivityInstance subProcessActivityInst = runtimeService.getActivityInstance(subProcess.getId());

    // when
    Batch modificationBatch = runtimeService.createProcessInstanceModification(subProcess.getId())
        .startBeforeActivity("childEnd", subProcess.getId())
        .cancelActivityInstance(getInstanceIdForActivity(subProcessActivityInst, "innerTask"))
        .executeAsync();
    assertNotNull(modificationBatch);
    executeSeedAndBatchJobs(modificationBatch);

    // then
    assertProcessEnded(parentprocess.getId());
  }

  public void testModifyNullProcessInstance() {
    try {
      Batch modificationBatch = runtimeService
          .createProcessInstanceModification(null)
          .startBeforeActivity("someActivity")
          .executeAsync();
      assertNotNull(modificationBatch);
      executeSeedAndBatchJobs(modificationBatch);
      fail("should not succeed");
    } catch (NotValidException e) {
      assertTextPresent("processInstanceId is null", e.getMessage());
    }
  }

  protected void executeSeedAndBatchJobs(Batch batch) {
    Job job = managementService.createJobQuery().jobDefinitionId(batch.getSeedJobDefinitionId()).singleResult();
    // seed job
    managementService.executeJob(job.getId());


    for (Job pending : managementService.createJobQuery().jobDefinitionId(batch.getBatchJobDefinitionId()).list()) {
      managementService.executeJob(pending.getId());
    }
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
