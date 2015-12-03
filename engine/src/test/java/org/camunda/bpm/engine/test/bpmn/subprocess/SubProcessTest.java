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

package org.camunda.bpm.engine.test.bpmn.subprocess;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.CollectionUtil;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.bpmn.subprocess.util.GetActInstanceDelegate;
import org.camunda.bpm.engine.test.util.ActivityInstanceAssert;


/**
 * @author Joram Barrez
 * @author Falko Menge
 */
public class SubProcessTest extends PluggableProcessEngineTestCase {

  @Deployment
  public void testSimpleSubProcess() {

    // After staring the process, the task in the subprocess should be active
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("simpleSubProcess");
    Task subProcessTask = taskService.createTaskQuery()
                                                   .processInstanceId(pi.getId())
                                                   .singleResult();
    assertEquals("Task in subprocess", subProcessTask.getName());

    // we have 3 levels in the activityInstance:
    // pd
    ActivityInstance rootActivityInstance = runtimeService.getActivityInstance(pi.getProcessInstanceId());
    assertEquals(pi.getProcessDefinitionId(), rootActivityInstance.getActivityId());
    //subprocess
    assertEquals(1, rootActivityInstance.getChildActivityInstances().length);
    ActivityInstance subProcessInstance = rootActivityInstance.getChildActivityInstances()[0];
    assertEquals("subProcess", subProcessInstance.getActivityId());
    // usertask
    assertEquals(1, subProcessInstance.getChildActivityInstances().length);
    ActivityInstance userTaskInstance = subProcessInstance.getChildActivityInstances()[0];
    assertEquals("subProcessTask", userTaskInstance.getActivityId());

    // After completing the task in the subprocess,
    // the subprocess scope is destroyed and the complete process ends
    taskService.complete(subProcessTask.getId());
    assertNull(runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).singleResult());
  }

  /**
   * Same test case as before, but now with all automatic steps
   */
  @Deployment
  public void testSimpleAutomaticSubProcess() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("simpleSubProcessAutomatic");
    assertTrue(pi.isEnded());
    assertProcessEnded(pi.getId());
  }

  @Deployment
  public void testSimpleSubProcessWithTimer() {

    Date startTime = new Date();

    // After staring the process, the task in the subprocess should be active
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("simpleSubProcess");
    Task subProcessTask = taskService.createTaskQuery()
                                                   .processInstanceId(pi.getId())
                                                   .singleResult();
    assertEquals("Task in subprocess", subProcessTask.getName());

    // we have 3 levels in the activityInstance:
    // pd
    ActivityInstance rootActivityInstance = runtimeService.getActivityInstance(pi.getProcessInstanceId());
    assertEquals(pi.getProcessDefinitionId(), rootActivityInstance.getActivityId());
    //subprocess
    assertEquals(1, rootActivityInstance.getChildActivityInstances().length);
    ActivityInstance subProcessInstance = rootActivityInstance.getChildActivityInstances()[0];
    assertEquals("subProcess", subProcessInstance.getActivityId());
    // usertask
    assertEquals(1, subProcessInstance.getChildActivityInstances().length);
    ActivityInstance userTaskInstance = subProcessInstance.getChildActivityInstances()[0];
    assertEquals("subProcessTask", userTaskInstance.getActivityId());

    // Setting the clock forward 2 hours 1 second (timer fires in 2 hours) and fire up the job executor
    ClockUtil.setCurrentTime(new Date(startTime.getTime() + (2 * 60 * 60 * 1000 ) + 1000));
    waitForJobExecutorToProcessAllJobs(5000L);

    // The subprocess should be left, and the escalated task should be active
    Task escalationTask = taskService.createTaskQuery()
                                                   .processInstanceId(pi.getId())
                                                   .singleResult();
    assertEquals("Fix escalated problem", escalationTask.getName());
  }

  /**
   * A test case that has a timer attached to the subprocess,
   * where 2 concurrent paths are defined when the timer fires.
   */
  @Deployment
  public void IGNORE_testSimpleSubProcessWithConcurrentTimer() {

    // After staring the process, the task in the subprocess should be active
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("simpleSubProcessWithConcurrentTimer");
    TaskQuery taskQuery = taskService
      .createTaskQuery()
      .processInstanceId(pi.getId())
      .orderByTaskName()
      .asc();

    Task subProcessTask = taskQuery.singleResult();
    assertEquals("Task in subprocess", subProcessTask.getName());

    // When the timer is fired (after 2 hours), two concurrent paths should be created
    Job job = managementService.createJobQuery().singleResult();
    managementService.executeJob(job.getId());

    List<Task> tasksAfterTimer = taskQuery.list();
    assertEquals(2, tasksAfterTimer.size());
    Task taskAfterTimer1 = tasksAfterTimer.get(0);
    Task taskAfterTimer2 = tasksAfterTimer.get(1);
    assertEquals("Task after timer 1", taskAfterTimer1.getName());
    assertEquals("Task after timer 2", taskAfterTimer2.getName());

    // Completing the two tasks should end the process instance
    taskService.complete(taskAfterTimer1.getId());
    taskService.complete(taskAfterTimer2.getId());
    assertProcessEnded(pi.getId());
  }

  /**
   * Test case where the simple sub process of previous test cases
   * is nested within another subprocess.
   */
  @Deployment
  public void testNestedSimpleSubProcess() {

    // Start and delete a process with a nested subprocess when it is not yet ended
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("nestedSimpleSubProcess", CollectionUtil.singletonMap("someVar", "abc"));
    runtimeService.deleteProcessInstance(pi.getId(), "deleted");

    // After staring the process, the task in the inner subprocess must be active
    pi = runtimeService.startProcessInstanceByKey("nestedSimpleSubProcess");
    Task subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("Task in subprocess", subProcessTask.getName());

    // now we have 4 levels in the activityInstance:
    // pd
    ActivityInstance rootActivityInstance = runtimeService.getActivityInstance(pi.getProcessInstanceId());
    assertEquals(pi.getProcessDefinitionId(), rootActivityInstance.getActivityId());
    //subprocess1
    assertEquals(1, rootActivityInstance.getChildActivityInstances().length);
    ActivityInstance subProcessInstance1 = rootActivityInstance.getChildActivityInstances()[0];
    assertEquals("outerSubProcess", subProcessInstance1.getActivityId());
    //subprocess2
    assertEquals(1, rootActivityInstance.getChildActivityInstances().length);
    ActivityInstance subProcessInstance2 = subProcessInstance1.getChildActivityInstances()[0];
    assertEquals("innerSubProcess", subProcessInstance2.getActivityId());
    // usertask
    assertEquals(1, subProcessInstance2.getChildActivityInstances().length);
    ActivityInstance userTaskInstance = subProcessInstance2.getChildActivityInstances()[0];
    assertEquals("innerSubProcessTask", userTaskInstance.getActivityId());

    // After completing the task in the subprocess,
    // both subprocesses are destroyed and the task after the subprocess should be active
    taskService.complete(subProcessTask.getId());
    Task taskAfterSubProcesses = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertNotNull(taskAfterSubProcesses);
    assertEquals("Task after subprocesses", taskAfterSubProcesses.getName());
    taskService.complete(taskAfterSubProcesses.getId());
    assertProcessEnded(pi.getId());
  }

  @Deployment
  public void testNestedSimpleSubprocessWithTimerOnInnerSubProcess() {
    Date startTime = new Date();

    // After staring the process, the task in the subprocess should be active
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("nestedSubProcessWithTimer");
    Task subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("Task in subprocess", subProcessTask.getName());

    // now we have 4 levels in the activityInstance:
    // pd
    ActivityInstance rootActivityInstance = runtimeService.getActivityInstance(pi.getProcessInstanceId());
    assertEquals(pi.getProcessDefinitionId(), rootActivityInstance.getActivityId());
    //subprocess1
    assertEquals(1, rootActivityInstance.getChildActivityInstances().length);
    ActivityInstance subProcessInstance1 = rootActivityInstance.getChildActivityInstances()[0];
    assertEquals("outerSubProcess", subProcessInstance1.getActivityId());
    //subprocess2
    assertEquals(1, rootActivityInstance.getChildActivityInstances().length);
    ActivityInstance subProcessInstance2 = subProcessInstance1.getChildActivityInstances()[0];
    assertEquals("innerSubProcess", subProcessInstance2.getActivityId());
    // usertask
    assertEquals(1, subProcessInstance2.getChildActivityInstances().length);
    ActivityInstance userTaskInstance = subProcessInstance2.getChildActivityInstances()[0];
    assertEquals("innerSubProcessTask", userTaskInstance.getActivityId());

    // Setting the clock forward 1 hour 1 second (timer fires in 1 hour) and fire up the job executor
    ClockUtil.setCurrentTime(new Date(startTime.getTime() + ( 60 * 60 * 1000 ) + 1000));
    waitForJobExecutorToProcessAllJobs(5000L);

    // The inner subprocess should be destoyed, and the escalated task should be active
    Task escalationTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("Escalated task", escalationTask.getName());

    // now we have 3 levels in the activityInstance:
    // pd
    rootActivityInstance = runtimeService.getActivityInstance(pi.getProcessInstanceId());
    assertEquals(pi.getProcessDefinitionId(), rootActivityInstance.getActivityId());
    //subprocess1
    assertEquals(1, rootActivityInstance.getChildActivityInstances().length);
    subProcessInstance1 = rootActivityInstance.getChildActivityInstances()[0];
    assertEquals("outerSubProcess", subProcessInstance1.getActivityId());
    //subprocess2
    assertEquals(1, rootActivityInstance.getChildActivityInstances().length);
    ActivityInstance escalationTaskInst = subProcessInstance1.getChildActivityInstances()[0];
    assertEquals("escalationTask", escalationTaskInst.getActivityId());

    // Completing the escalated task, destroys the outer scope and activates the task after the subprocess
    taskService.complete(escalationTask.getId());
    Task taskAfterSubProcess = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("Task after subprocesses", taskAfterSubProcess.getName());
  }

  /**
   * Test case where the simple sub process of previous test cases
   * is nested within two other sub processes
   */
  @Deployment
  public void testDoubleNestedSimpleSubProcess() {
    // After staring the process, the task in the inner subprocess must be active
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("nestedSimpleSubProcess");
    Task subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("Task in subprocess", subProcessTask.getName());

    // After completing the task in the subprocess,
    // both subprocesses are destroyed and the task after the subprocess should be active
    taskService.complete(subProcessTask.getId());
    Task taskAfterSubProcesses = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("Task after subprocesses", taskAfterSubProcesses.getName());
  }

  @Deployment
  public void testSimpleParallelSubProcess() {

    // After starting the process, the two task in the subprocess should be active
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("simpleParallelSubProcess");
    List<Task> subProcessTasks = taskService.createTaskQuery().processInstanceId(pi.getId()).orderByTaskName().asc().list();

    // Tasks are ordered by name (see query)
    Task taskA = subProcessTasks.get(0);
    Task taskB = subProcessTasks.get(1);
    assertEquals("Task A", taskA.getName());
    assertEquals("Task B", taskB.getName());

    ActivityInstance rootActivityInstance = runtimeService.getActivityInstance(pi.getProcessInstanceId());
    assertEquals(pi.getProcessDefinitionId(), rootActivityInstance.getActivityId());
    //subprocess1
    assertEquals(1, rootActivityInstance.getChildActivityInstances().length);
    ActivityInstance subProcessInstance = rootActivityInstance.getChildActivityInstances()[0];
    assertEquals("subProcess", subProcessInstance.getActivityId());
    // 2 tasks are present
    assertEquals(2, subProcessInstance.getChildActivityInstances().length);

    // Completing both tasks, should destroy the subprocess and activate the task after the subprocess
    taskService.complete(taskA.getId());

    rootActivityInstance = runtimeService.getActivityInstance(pi.getProcessInstanceId());
    assertEquals(pi.getProcessDefinitionId(), rootActivityInstance.getActivityId());
    subProcessInstance = rootActivityInstance.getChildActivityInstances()[0];
    // 1 task + 1 join
    assertEquals(2, subProcessInstance.getChildActivityInstances().length);

    taskService.complete(taskB.getId());
    Task taskAfterSubProcess = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("Task after sub process", taskAfterSubProcess.getName());
  }

  @Deployment
  public void testSimpleParallelSubProcessWithTimer() {

    // After staring the process, the tasks in the subprocess should be active
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("simpleParallelSubProcessWithTimer");
    List<Task> subProcessTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).orderByTaskName().asc().list();

    // Tasks are ordered by name (see query)
    Task taskA = subProcessTasks.get(0);
    Task taskB = subProcessTasks.get(1);
    assertEquals("Task A", taskA.getName());
    assertEquals("Task B", taskB.getName());

    Job job = managementService
      .createJobQuery()
      .processInstanceId(processInstance.getId())
      .singleResult();

    managementService.executeJob(job.getId());

    // The inner subprocess should be destoyed, and the tsk after the timer should be active
    Task taskAfterTimer = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals("Task after timer", taskAfterTimer.getName());

    // Completing the task after the timer ends the process instance
    taskService.complete(taskAfterTimer.getId());
    assertProcessEnded(processInstance.getId());
  }

  @Deployment
  public void testTwoSubProcessInParallel() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("twoSubProcessInParallel");
    TaskQuery taskQuery = taskService
      .createTaskQuery()
      .processInstanceId(pi.getId())
      .orderByTaskName()
      .asc();
    List<Task> tasks = taskQuery.list();

    // After process start, both tasks in the subprocesses should be active
    assertEquals("Task in subprocess A", tasks.get(0).getName());
    assertEquals("Task in subprocess B", tasks.get(1).getName());

    // validate activity instance tree
    ActivityInstance rootActivityInstance = runtimeService.getActivityInstance(pi.getProcessInstanceId());
    assertEquals(pi.getProcessDefinitionId(), rootActivityInstance.getActivityId());
    assertEquals(2, rootActivityInstance.getChildActivityInstances().length);
    ActivityInstance[] childActivityInstances = rootActivityInstance.getChildActivityInstances();
    for (ActivityInstance activityInstance : childActivityInstances) {
      assertTrue(Arrays.asList(new String[]{"subProcessA", "subProcessB"}).contains(activityInstance.getActivityId()));
      ActivityInstance[] subProcessChildren = activityInstance.getChildActivityInstances();
      assertEquals(1, subProcessChildren.length);
      assertTrue(Arrays.asList(new String[]{"subProcessATask", "subProcessBTask"}).contains(subProcessChildren[0].getActivityId()));
    }

    // Completing both tasks should active the tasks outside the subprocesses
    taskService.complete(tasks.get(0).getId());

    tasks = taskQuery.list();
    assertEquals("Task after subprocess A", tasks.get(0).getName());
    assertEquals("Task in subprocess B", tasks.get(1).getName());

    taskService.complete(tasks.get(1).getId());

    tasks = taskQuery.list();

    assertEquals("Task after subprocess A", tasks.get(0).getName());
    assertEquals("Task after subprocess B", tasks.get(1).getName());

    // Completing these tasks should end the process
    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());

    assertProcessEnded(pi.getId());
  }

  @Deployment
  public void testTwoSubProcessInParallelWithinSubProcess() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("twoSubProcessInParallelWithinSubProcess");
    TaskQuery taskQuery = taskService
      .createTaskQuery()
      .processInstanceId(pi.getId())
      .orderByTaskName()
      .asc();
    List<Task> tasks = taskQuery.list();

    // After process start, both tasks in the subprocesses should be active
    Task taskA = tasks.get(0);
    Task taskB = tasks.get(1);
    assertEquals("Task in subprocess A", taskA.getName());
    assertEquals("Task in subprocess B", taskB.getName());

    // validate activity instance tree
    ActivityInstance rootActivityInstance = runtimeService.getActivityInstance(pi.getProcessInstanceId());
    ActivityInstanceAssert.assertThat(rootActivityInstance)
    .hasStructure(
        ActivityInstanceAssert
        .describeActivityInstanceTree(pi.getProcessDefinitionId())
          .beginScope("outerSubProcess")
            .beginScope("subProcessA")
              .activity("subProcessATask")
            .endScope()
            .beginScope("subProcessB")
              .activity("subProcessBTask")
        .done());

    // Completing both tasks should active the tasks outside the subprocesses
    taskService.complete(taskA.getId());
    taskService.complete(taskB.getId());

    Task taskAfterSubProcess = taskQuery.singleResult();
    assertEquals("Task after subprocess", taskAfterSubProcess.getName());

    // Completing this task should end the process
    taskService.complete(taskAfterSubProcess.getId());
    assertProcessEnded(pi.getId());
  }

  @Deployment
  public void testTwoNestedSubProcessesInParallelWithTimer() {

//    Date startTime = new Date();

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("nestedParallelSubProcessesWithTimer");
    TaskQuery taskQuery = taskService
      .createTaskQuery()
      .processInstanceId(pi.getId())
      .orderByTaskName()
      .asc();
    List<Task> tasks = taskQuery.list();

    // After process start, both tasks in the subprocesses should be active
    Task taskA = tasks.get(0);
    Task taskB = tasks.get(1);
    assertEquals("Task in subprocess A", taskA.getName());
    assertEquals("Task in subprocess B", taskB.getName());

    // Firing the timer should destroy all three subprocesses and activate the task after the timer
//    ClockUtil.setCurrentTime(new Date(startTime.getTime() + (2 * 60 * 60 * 1000 ) + 1000));
//    waitForJobExecutorToProcessAllJobs(5000L, 50L);
    Job job = managementService.createJobQuery().singleResult();
    managementService.executeJob(job.getId());

    Task taskAfterTimer = taskQuery.singleResult();
    assertEquals("Task after timer", taskAfterTimer.getName());

    // Completing the task should end the process instance
    taskService.complete(taskAfterTimer.getId());
    assertProcessEnded(pi.getId());
  }

  /**
   * @see http://jira.codehaus.org/browse/ACT-1072
   */
  @Deployment
  public void testNestedSimpleSubProcessWithoutEndEvent() {
    testNestedSimpleSubProcess();
  }

  /**
   * @see http://jira.codehaus.org/browse/ACT-1072
   */
  @Deployment
  public void testSimpleSubProcessWithoutEndEvent() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testSimpleSubProcessWithoutEndEvent");
    assertProcessEnded(pi.getId());
  }

  /**
   * @see http://jira.codehaus.org/browse/ACT-1072
   */
  @Deployment
  public void testNestedSubProcessesWithoutEndEvents() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testNestedSubProcessesWithoutEndEvents");
    assertProcessEnded(pi.getId());
  }

  @Deployment
  // SEE https://app.camunda.com/jira/browse/CAM-2169
  public void testActivityInstanceTreeNestedCmd() {
    GetActInstanceDelegate.activityInstance = null;
    runtimeService.startProcessInstanceByKey("process");

    ActivityInstance activityInstance = GetActInstanceDelegate.activityInstance;

    assertNotNull(activityInstance);
    ActivityInstance subProcessInstance = activityInstance.getChildActivityInstances()[0];
    assertNotNull(subProcessInstance);
    assertEquals("SubProcess_1", subProcessInstance.getActivityId());

    ActivityInstance serviceTaskInstance = subProcessInstance.getChildActivityInstances()[0];
    assertNotNull(serviceTaskInstance);
    assertEquals("ServiceTask_1", serviceTaskInstance.getActivityId());
  }

  @Deployment
  // SEE https://app.camunda.com/jira/browse/CAM-2169
  public void testActivityInstanceTreeNestedCmdAfterTx() {
    GetActInstanceDelegate.activityInstance = null;
    runtimeService.startProcessInstanceByKey("process");

    // send message
    runtimeService.correlateMessage("message");

    ActivityInstance activityInstance = GetActInstanceDelegate.activityInstance;

    assertNotNull(activityInstance);
    ActivityInstance subProcessInstance = activityInstance.getChildActivityInstances()[0];
    assertNotNull(subProcessInstance);
    assertEquals("SubProcess_1", subProcessInstance.getActivityId());

    ActivityInstance serviceTaskInstance = subProcessInstance.getChildActivityInstances()[0];
    assertNotNull(serviceTaskInstance);
    assertEquals("ServiceTask_1", serviceTaskInstance.getActivityId());
  }

  public void testConcurrencyInSubProcess() {

    org.camunda.bpm.engine.repository.Deployment deployment =
      repositoryService.createDeployment()
                  .addClasspathResource("org/camunda/bpm/engine/test/bpmn/subprocess/SubProcessTest.fixSystemFailureProcess.bpmn20.xml")
                  .deploy();

    // After staring the process, both tasks in the subprocess should be active
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("fixSystemFailure");
    List<Task> tasks = taskService.createTaskQuery()
                                  .processInstanceId(pi.getId())
                                  .orderByTaskName()
                                  .asc()
                                  .list();

    // Tasks are ordered by name (see query)
    assertEquals(2, tasks.size());
    Task investigateHardwareTask = tasks.get(0);
    Task investigateSoftwareTask = tasks.get(1);
    assertEquals("Investigate hardware", investigateHardwareTask.getName());
    assertEquals("Investigate software", investigateSoftwareTask.getName());

    // Completing both the tasks finishes the subprocess and enables the task after the subprocess
    taskService.complete(investigateHardwareTask.getId());
    taskService.complete(investigateSoftwareTask.getId());

    Task writeReportTask = taskService
      .createTaskQuery()
      .processInstanceId(pi.getId())
      .singleResult();
    assertEquals("Write report", writeReportTask.getName());

    // Clean up
    repositoryService.deleteDeployment(deployment.getId(), true);
  }
}
