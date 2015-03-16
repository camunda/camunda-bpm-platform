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
import org.camunda.bpm.engine.test.util.ExecutionTree;

/**
 * @author Thorben Lindhauer
 *
 */
public class ProcessInstanceModificationAsyncTest extends PluggableProcessEngineTestCase {

  protected static final String EXCLUSIVE_GATEWAY_ASYNC_TASK_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.exclusiveGatewayAsyncTask.bpmn20.xml";
  protected static final String NESTED_ASYNC_TASK_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.nestedParallelAsyncOneTaskProcess.bpmn20.xml";
  protected static final String NESTED_ASYNC_SCOPE_TASK_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.nestedParallelAsyncOneScopeTaskProcess.bpmn20.xml";
  protected static final String NESTED_PARALLEL_ASYNC_SCOPE_TASK_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.nestedParallelAsyncConcurrentScopeTaskProcess.bpmn20.xml";


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

  @Deployment(resources = NESTED_ASYNC_TASK_PROCESS)
  public void testCancelParentScopeOfAsyncActivity() {
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

  @Deployment(resources = NESTED_ASYNC_SCOPE_TASK_PROCESS)
  public void testCancelParentScopeOfAsyncScopeActivity() {
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

  @Deployment(resources = NESTED_PARALLEL_ASYNC_SCOPE_TASK_PROCESS)
  public void testCancelParentScopeOfParallelAsyncScopeActivity() {
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

  @Deployment(resources = NESTED_ASYNC_TASK_PROCESS)
  public void testCancelAsyncActivityFails() {
    // given a process instance with an async task in a subprocess
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nestedOneTaskProcess");

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    // then cancelling the async task is not possible
    try {
      runtimeService.createProcessInstanceModification(processInstance.getId())
        .cancelActivityInstance(getChildTransitionInstanceForTargetActivity(tree, "innerTask").getId())
        .execute();
      fail("should not succeed");
    } catch (ProcessEngineException e) {
      assertTextPresent("activityInstance is null", e.getMessage());
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

  protected TransitionInstance getChildTransitionInstanceForTargetActivity(ActivityInstance activityInstance, String targetActivityId) {
    for (TransitionInstance childTransitionInstance : activityInstance.getChildTransitionInstances()) {
      if (targetActivityId.equals(childTransitionInstance.getTargetActivityId())) {
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
