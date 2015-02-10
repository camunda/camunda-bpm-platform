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

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.TransitionInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.examples.bpmn.executionlistener.RecorderExecutionListener;
import org.camunda.bpm.engine.test.examples.bpmn.executionlistener.RecorderExecutionListener.RecordedEvent;
import org.camunda.bpm.engine.test.util.ExecutionTree;
import org.camunda.bpm.engine.variable.Variables;

/**
 * @author Thorben Lindhauer
 *
 */
public class ProcessInstanceModificationAsyncTest extends PluggableProcessEngineTestCase {

  protected static final String EXCLUSIVE_GATEWAY_ASYNC_BEFORE_TASK_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.exclusiveGatewayAsyncTask.bpmn20.xml";

  protected static final String ASYNC_BEFORE_ONE_TASK_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.asyncBeforeOneTaskProcess.bpmn20.xml";
  protected static final String ASYNC_BEFORE_ONE_SCOPE_TASK_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.asyncBeforeOneScopeTaskProcess.bpmn20.xml";

  protected static final String NESTED_ASYNC_BEFORE_TASK_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.nestedParallelAsyncBeforeOneTaskProcess.bpmn20.xml";
  protected static final String NESTED_ASYNC_BEFORE_SCOPE_TASK_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.nestedParallelAsyncBeforeOneScopeTaskProcess.bpmn20.xml";
  protected static final String NESTED_PARALLEL_ASYNC_BEFORE_SCOPE_TASK_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.nestedParallelAsyncBeforeConcurrentScopeTaskProcess.bpmn20.xml";
  protected static final String NESTED_ASYNC_BEFORE_IO_LISTENER_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.nestedParallelAsyncBeforeOneTaskProcessIoAndListeners.bpmn20.xml";

  protected static final String ASYNC_AFTER_ONE_TASK_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.asyncAfterOneTaskProcess.bpmn20.xml";

  protected static final String NESTED_ASYNC_AFTER_TASK_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.nestedParallelAsyncAfterOneTaskProcess.bpmn20.xml";
  protected static final String NESTED_ASYNC_AFTER_END_EVENT_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.nestedParallelAsyncAfterEndEventProcess.bpmn20.xml";

  @Deployment(resources = EXCLUSIVE_GATEWAY_ASYNC_BEFORE_TASK_PROCESS)
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

  @Deployment(resources = NESTED_ASYNC_BEFORE_TASK_PROCESS)
  public void testCancelParentScopeOfAsyncBeforeActivity() {
    // given a process instance with an async task in a subprocess
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nestedOneTaskProcess");

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    // when I cancel the subprocess
    runtimeService.createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "subProcess"))
      .execute();

    // then the process instance is in a valid state
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstance.getId());
    assertNotNull(updatedTree);

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("outerTask")
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree("outerTask").scope()
      .done());

    completeTasksInOrder("outerTask");
    assertProcessEnded(processInstance.getId());

  }

  @Deployment(resources = NESTED_ASYNC_BEFORE_SCOPE_TASK_PROCESS)
  public void testCancelParentScopeOfAsyncBeforeScopeActivity() {
    // given a process instance with an async task in a subprocess
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nestedOneTaskProcess");

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    // when I cancel the subprocess
    runtimeService.createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "subProcess"))
      .execute();

    // then the process instance is in a valid state
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstance.getId());
    assertNotNull(updatedTree);

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("outerTask")
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree("outerTask").scope()
      .done());

    completeTasksInOrder("outerTask");
    assertProcessEnded(processInstance.getId());

  }

  @Deployment(resources = NESTED_PARALLEL_ASYNC_BEFORE_SCOPE_TASK_PROCESS)
  public void testCancelParentScopeOfParallelAsyncBeforeScopeActivity() {
    // given a process instance with two concurrent async scope tasks in a subprocess
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nestedConcurrentTasksProcess");

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    // when I cancel the subprocess
    runtimeService.createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(getInstanceIdForActivity(tree, "subProcess"))
      .execute();

    // then the process instance is in a valid state
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstance.getId());
    assertNotNull(updatedTree);

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("outerTask")
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree("outerTask").scope()
      .done());

    completeTasksInOrder("outerTask");
    assertProcessEnded(processInstance.getId());

  }

  @Deployment(resources = NESTED_ASYNC_BEFORE_TASK_PROCESS)
  public void testCancelAsyncActivityInstanceFails() {
    // given a process instance with an async task in a subprocess
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nestedOneTaskProcess");

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    // the the async task is not an activity instance so it cannot be cancelled as follows
    try {
      runtimeService.createProcessInstanceModification(processInstance.getId())
        .cancelActivityInstance(getChildTransitionInstanceForTargetActivity(tree, "innerTask").getId())
        .execute();
      fail("should not succeed");
    } catch (ProcessEngineException e) {
      assertTextPresent("activityInstance is null", e.getMessage());
    }
  }

  @Deployment(resources = NESTED_ASYNC_BEFORE_TASK_PROCESS)
  public void testCancelAsyncBeforeTransitionInstance() {
    // given a process instance with an async task in a subprocess
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nestedOneTaskProcess");

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    assertEquals(1, managementService.createJobQuery().count());

    // when the async task is cancelled via cancelTransitionInstance
    runtimeService.createProcessInstanceModification(processInstance.getId())
      .cancelTransitionInstance(getChildTransitionInstanceForTargetActivity(tree, "innerTask").getId())
      .execute();

    // then the job has been removed
    assertEquals(0, managementService.createJobQuery().count());

    // and the activity instance and execution trees match
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .activity("outerTask")
        .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree("outerTask").scope()
    .done());

    // and the process can be completed successfully
    completeTasksInOrder("outerTask");
    assertProcessEnded(processInstance.getId());
  }


  @Deployment(resources = ASYNC_BEFORE_ONE_TASK_PROCESS)
  public void testCancelAsyncBeforeTransitionInstanceEndsProcessInstance() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService.createProcessInstanceModification(processInstanceId)
      .cancelTransitionInstance(getChildTransitionInstanceForTargetActivity(tree, "theTask").getId())
      .execute();

    // then the process instance has ended
    assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = ASYNC_BEFORE_ONE_SCOPE_TASK_PROCESS)
  public void testCancelAsyncBeforeScopeTransitionInstanceEndsProcessInstance() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService.createProcessInstanceModification(processInstanceId)
      .cancelTransitionInstance(getChildTransitionInstanceForTargetActivity(tree, "theTask").getId())
      .execute();

    // then the process instance has ended
    assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = ASYNC_BEFORE_ONE_TASK_PROCESS)
  public void testCancelAndStartAsyncBeforeTransitionInstance() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    Job asyncJob = managementService.createJobQuery().singleResult();

    // when cancelling the only transition instance in the process and immediately starting it again
    runtimeService.createProcessInstanceModification(processInstanceId)
      .cancelTransitionInstance(getChildTransitionInstanceForTargetActivity(tree, "theTask").getId())
      .startBeforeActivity("theTask")
      .execute();

    // then the activity instance tree should be as before
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .transition("theTask")
        .done());

    // and the async job should be a new one
    Job newAsyncJob = managementService.createJobQuery().singleResult();
    assertFalse(asyncJob.getId().equals(newAsyncJob.getId()));

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree("theTask").scope()
    .done());

    // and the process can be completed successfully
    executeAvailableJobs();
    completeTasksInOrder("theTask");
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = NESTED_PARALLEL_ASYNC_BEFORE_SCOPE_TASK_PROCESS)
  public void testCancelNestedConcurrentTransitionInstance() {
    // given a process instance with an instance of outerTask and two asynchronous tasks nested
    // in a subprocess
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nestedConcurrentTasksProcess");
    String processInstanceId = processInstance.getId();

    // when one of the inner transition instances is cancelled
    ActivityInstance tree = runtimeService.getActivityInstance(processInstanceId);

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelTransitionInstance(getChildTransitionInstanceForTargetActivity(tree, "innerTask1").getId())
      .execute();

    // then the activity instance and execution trees should match
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .activity("outerTask")
          .beginScope("subProcess")
            .transition("innerTask2")
        .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope()
        .child("outerTask").concurrent().noScope().up()
        .child(null).concurrent().noScope()
          .child("innerTask2").scope()
    .done());

    // and the job for innerTask2 should still be there and assigned to the correct execution
    Job innerTask2Job = managementService.createJobQuery().singleResult();
    assertNotNull(innerTask2Job);

    Execution innerTask2Execution = runtimeService.createExecutionQuery().activityId("innerTask2").singleResult();
    assertNotNull(innerTask2Execution);

    assertEquals(innerTask2Job.getExecutionId(), innerTask2Execution.getId());

    // and completing the process should succeed
    completeTasksInOrder("outerTask");
    managementService.executeJob(innerTask2Job.getId());
    completeTasksInOrder("innerTask2");

    assertProcessEnded(processInstanceId);
  }

  @Deployment(resources = NESTED_PARALLEL_ASYNC_BEFORE_SCOPE_TASK_PROCESS)
  public void testCancelNestedConcurrentTransitionInstanceWithConcurrentScopeTask() {
    // given a process instance where the job for innerTask2 is already executed
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nestedConcurrentTasksProcess");
    String processInstanceId = processInstance.getId();

    Job innerTask2Job = managementService.createJobQuery().activityId("innerTask2").singleResult();
    assertNotNull(innerTask2Job);
    managementService.executeJob(innerTask2Job.getId());

    // when the transition instance to innerTask1 is cancelled
    ActivityInstance tree = runtimeService.getActivityInstance(processInstanceId);

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelTransitionInstance(getChildTransitionInstanceForTargetActivity(tree, "innerTask1").getId())
      .execute();

    // then the activity instance and execution tree should match
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .activity("outerTask")
          .beginScope("subProcess")
            .activity("innerTask2")
        .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope()
        .child("outerTask").concurrent().noScope().up()
        .child(null).concurrent().noScope()
          .child(null).scope()
            .child("innerTask2").scope()
    .done());

    // and there should be no job for innerTask1 anymore
    assertEquals(0, managementService.createJobQuery().activityId("innerTask1").count());

    // and completing the process should succeed
    completeTasksInOrder("innerTask2", "outerTask");

    assertProcessEnded(processInstanceId);
  }

  /**
   * TODO: re-add when CAM-3707 and CAM-3708 are fixed
   */
  @Deployment(resources = NESTED_ASYNC_BEFORE_IO_LISTENER_PROCESS)
  public void FAILING_testCancelTransitionInstanceShouldNotInvokeIoMappingAndListenersOfTargetActivity() {
    RecorderExecutionListener.clear();

    // given a process instance with an async task in a subprocess
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nestedOneTaskProcess",
        Variables.createVariables().putValue("listener", new RecorderExecutionListener()));

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    assertEquals(1, managementService.createJobQuery().count());

    // when the async task is cancelled via cancelTransitionInstance
    runtimeService.createProcessInstanceModification(processInstance.getId())
      .cancelTransitionInstance(getChildTransitionInstanceForTargetActivity(tree, "innerTask").getId())
      .execute();

    // then no io mapping is executed and no end listener is executed
    assertTrue(RecorderExecutionListener.getRecordedEvents().isEmpty());
    assertEquals(0, runtimeService.createVariableInstanceQuery().variableName("outputMappingExecuted").count());

    // and the process can be completed successfully
    completeTasksInOrder("outerTask");
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = NESTED_ASYNC_AFTER_TASK_PROCESS)
  public void testCancelAsyncAfterTransitionInstance() {
    // given a process instance with an asyncAfter task in a subprocess
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nestedOneTaskProcess");

    Task innerTask = taskService.createTaskQuery().taskDefinitionKey("innerTask").singleResult();
    assertNotNull(innerTask);
    taskService.complete(innerTask.getId());

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    assertEquals(1, managementService.createJobQuery().count());

    // when the async task is cancelled via cancelTransitionInstance
    runtimeService.createProcessInstanceModification(processInstance.getId())
      .cancelTransitionInstance(getChildTransitionInstanceForTargetActivity(tree, "innerTask").getId())
      .execute();

    // then the job has been removed
    assertEquals(0, managementService.createJobQuery().count());

    // and the activity instance and execution trees match
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .activity("outerTask")
        .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree("outerTask").scope()
    .done());

    // and the process can be completed successfully
    completeTasksInOrder("outerTask");
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = NESTED_ASYNC_AFTER_END_EVENT_PROCESS)
  public void testCancelAsyncAfterEndEventTransitionInstance() {
    // given a process instance with an asyncAfter end event in a subprocess
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nestedAsyncEndEventProcess");

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    assertEquals(1, managementService.createJobQuery().count());

    // when the async task is cancelled via cancelTransitionInstance
    runtimeService.createProcessInstanceModification(processInstance.getId())
      .cancelTransitionInstance(getChildTransitionInstanceForTargetActivity(tree, "subProcessEnd").getId())
      .execute();

    // then the job has been removed
    assertEquals(0, managementService.createJobQuery().count());

    // and the activity instance and execution trees match
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .activity("outerTask")
        .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree("outerTask").scope()
    .done());

    // and the process can be completed successfully
    completeTasksInOrder("outerTask");
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = ASYNC_AFTER_ONE_TASK_PROCESS)
  public void testCancelAsyncAfterTransitionInstanceEndsProcessInstance() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String processInstanceId = processInstance.getId();

    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    taskService.complete(task.getId());

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService.createProcessInstanceModification(processInstanceId)
      .cancelTransitionInstance(getChildTransitionInstanceForTargetActivity(tree, "theTask").getId())
      .execute();

    // then the process instance has ended
    assertProcessEnded(processInstanceId);
  }

  @Deployment
  public void testCancelAsyncAfterTransitionInstanceInvokesParentListeners() {
    RecorderExecutionListener.clear();

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nestedOneTaskProcess",
        Variables.createVariables().putValue("listener", new RecorderExecutionListener()));
    String processInstanceId = processInstance.getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService.createProcessInstanceModification(processInstanceId)
      .cancelTransitionInstance(getChildTransitionInstanceForTargetActivity(tree, "subProcessEnd").getId())
      .execute();

    assertEquals(1, RecorderExecutionListener.getRecordedEvents().size());
    RecordedEvent event = RecorderExecutionListener.getRecordedEvents().get(0);
    assertEquals("subProcess", event.getActivityId());

    RecorderExecutionListener.clear();
  }

  @Deployment(resources = NESTED_ASYNC_BEFORE_TASK_PROCESS)
  public void testCancelAllCancelsTransitionInstances() {
    // given a process instance with an async task in a subprocess
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nestedOneTaskProcess");

    assertEquals(1, managementService.createJobQuery().count());

    // when the async task is cancelled via cancelAll
    runtimeService.createProcessInstanceModification(processInstance.getId())
      .cancelAllForActivity("innerTask")
      .execute();

    // then the job has been removed
    assertEquals(0, managementService.createJobQuery().count());

    // and the activity instance and execution trees match
    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .activity("outerTask")
        .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), processEngine);

    assertThat(executionTree)
    .matches(
      describeExecutionTree("outerTask").scope()
    .done());

    // and the process can be completed successfully
    completeTasksInOrder("outerTask");
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

  protected TransitionInstance getChildTransitionInstanceForTargetActivity(ActivityInstance activityInstance, String targetActivityId) {
    for (TransitionInstance childTransitionInstance : activityInstance.getChildTransitionInstances()) {
      if (targetActivityId.equals(childTransitionInstance.getActivityId())) {
        return childTransitionInstance;
      }
    }

    for (ActivityInstance childInstance : activityInstance.getChildActivityInstances()) {
      TransitionInstance instance = getChildTransitionInstanceForTargetActivity(childInstance, targetActivityId);
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
