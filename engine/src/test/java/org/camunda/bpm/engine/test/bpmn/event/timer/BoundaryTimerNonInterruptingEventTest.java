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
package org.camunda.bpm.engine.test.bpmn.event.timer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ClockTestUtil;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Joram Barrez
 */
public class BoundaryTimerNonInterruptingEventTest {

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testHelper);

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected RuntimeService runtimeService;
  protected ManagementService managementService;
  protected TaskService taskService;

  @Before
  public void setUp() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    runtimeService = engineRule.getRuntimeService();
    managementService = engineRule.getManagementService();
    taskService = engineRule.getTaskService();
  }

  @After
  public void tearDown() {
    ClockUtil.reset();
  }

  @Deployment
  @Test
  public void testMultipleTimersOnUserTask() {
    // Set the clock fixed
    Date startTime = new Date();

    // After process start, there should be 3 timers created
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("nonInterruptingTimersOnUserTask");
    Task task1 = taskService.createTaskQuery().singleResult();
    assertEquals("First Task", task1.getName());

    JobQuery jobQuery = managementService.createJobQuery().processInstanceId(pi.getId());
    List<Job> jobs = jobQuery.list();
    assertEquals(2, jobs.size());

    // After setting the clock to time '1 hour and 5 seconds', the first timer should fire
    ClockUtil.setCurrentTime(new Date(startTime.getTime() + ((60 * 60 * 1000) + 5000)));
    testHelper.waitForJobExecutorToProcessAllJobs(5000L);

    // we still have one timer more to fire
    assertEquals(1L, jobQuery.count());

    // and we are still in the first state, but in the second state as well!
    assertEquals(2L, taskService.createTaskQuery().count());
    List<Task> taskList = taskService.createTaskQuery().orderByTaskName().desc().list();
    assertEquals("First Task", taskList.get(0).getName());
    assertEquals("Escalation Task 1", taskList.get(1).getName());

    // complete the task and end the forked execution
    taskService.complete(taskList.get(1).getId());

    // but we still have the original executions
    assertEquals(1L, taskService.createTaskQuery().count());
    assertEquals("First Task", taskService.createTaskQuery().singleResult().getName());

    // After setting the clock to time '2 hour and 5 seconds', the second timer should fire
    ClockUtil.setCurrentTime(new Date(startTime.getTime() + ((2 * 60 * 60 * 1000) + 5000)));
    testHelper.waitForJobExecutorToProcessAllJobs(5000L);

    // no more timers to fire
    assertEquals(0L, jobQuery.count());

    // and we are still in the first state, but in the next escalation state as well
    assertEquals(2L, taskService.createTaskQuery().count());
    taskList = taskService.createTaskQuery().orderByTaskName().desc().list();
    assertEquals("First Task", taskList.get(0).getName());
    assertEquals("Escalation Task 2", taskList.get(1).getName());

    // This time we end the main task
    taskService.complete(taskList.get(0).getId());

    // but we still have the escalation task
    assertEquals(1L, taskService.createTaskQuery().count());
    Task escalationTask = taskService.createTaskQuery().singleResult();
    assertEquals("Escalation Task 2", escalationTask.getName());

    taskService.complete(escalationTask.getId());

    // now we are really done :-)
    testHelper.assertProcessEnded(pi.getId());
  }

  @Deployment
  @Test
  public void testTimerOnMiUserTask() {

    // After process start, there should be 1 timer created
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("nonInterruptingTimersOnUserTask");
    List<Task> taskList = taskService.createTaskQuery().list();
    assertEquals(5, taskList.size());
    for (Task task : taskList) {
      assertEquals("First Task", task.getName());
    }

    Job job = managementService.createJobQuery()
        .processInstanceId(pi.getId())
        .singleResult();
    assertNotNull(job);

    // execute the timer
    managementService.executeJob(job.getId());

    // now there are 6 tasks
    taskList = taskService.createTaskQuery()
        .orderByTaskName()
        .asc()
        .list();
    assertEquals(6, taskList.size());

    // first task is the escalation task
    Task escalationTask = taskList.remove(0);
    assertEquals("Escalation Task 1", escalationTask.getName());
    // complete it
    taskService.complete(escalationTask.getId());

    // now complete the remaining tasks
    for (Task task : taskList) {
      taskService.complete(task.getId());
    }

    // process instance is ended
    testHelper.assertProcessEnded(pi.getId());

  }

  @Deployment
  @Test
  public void testJoin() {
    // After process start, there should be 3 timers created
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testJoin");
    Task task1 = taskService.createTaskQuery().singleResult();
    assertEquals("Main Task", task1.getName());

    Job job = managementService.createJobQuery().processInstanceId(pi.getId()).singleResult();
    assertNotNull(job);

    managementService.executeJob(job.getId());

    // we now have both tasks
    assertEquals(2L, taskService.createTaskQuery().count());

    // end the first
    taskService.complete(task1.getId());

    // we now have one task left
    assertEquals(1L, taskService.createTaskQuery().count());
    Task task2 = taskService.createTaskQuery().singleResult();
    assertEquals("Escalation Task", task2.getName());

    // complete the task, the parallel gateway should fire
    taskService.complete(task2.getId());

    // and the process has ended
    testHelper.assertProcessEnded(pi.getId());
  }

  @Deployment
  @Test
  public void testTimerOnConcurrentMiTasks() {

    // After process start, there should be 1 timer created
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("timerOnConcurrentMiTasks");
    List<Task> taskList = taskService.createTaskQuery()
        .orderByTaskName()
        .desc()
        .list();
    assertEquals(6, taskList.size());
    Task secondTask = taskList.remove(0);
    assertEquals("Second Task", secondTask.getName());
    for (Task task : taskList) {
      assertEquals("First Task", task.getName());
    }

    Job job = managementService.createJobQuery()
        .processInstanceId(pi.getId())
        .singleResult();
    assertNotNull(job);

    // execute the timer
    managementService.executeJob(job.getId());

    // now there are 7 tasks
    taskList = taskService.createTaskQuery()
        .orderByTaskName()
        .asc()
        .list();
    assertEquals(7, taskList.size());

    // first task is the escalation task
    Task escalationTask = taskList.remove(0);
    assertEquals("Escalation Task 1", escalationTask.getName());
    // complete it
    taskService.complete(escalationTask.getId());

    // now complete the remaining tasks
    for (Task task : taskList) {
      taskService.complete(task.getId());
    }

    // process instance is ended
    testHelper.assertProcessEnded(pi.getId());
  }

  @Deployment
  @Test
  public void testTimerOnConcurrentTasks() {
    String procId = runtimeService.startProcessInstanceByKey("nonInterruptingOnConcurrentTasks").getId();
    assertEquals(2, taskService.createTaskQuery().count());

    Job timer = managementService.createJobQuery().singleResult();
    managementService.executeJob(timer.getId());
    assertEquals(3, taskService.createTaskQuery().count());

    // Complete task that was reached by non interrupting timer
    Task task = taskService.createTaskQuery().taskDefinitionKey("timerFiredTask").singleResult();
    taskService.complete(task.getId());
    assertEquals(2, taskService.createTaskQuery().count());

    // Complete other tasks
    for (Task t : taskService.createTaskQuery().list()) {
      taskService.complete(t.getId());
    }
    testHelper.assertProcessEnded(procId);
  }

  // Difference with previous test: now the join will be reached first
  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/event/timer/BoundaryTimerNonInterruptingEventTest.testTimerOnConcurrentTasks.bpmn20.xml"})
  @Test
  public void testTimerOnConcurrentTasks2() {
    String procId = runtimeService.startProcessInstanceByKey("nonInterruptingOnConcurrentTasks").getId();
    assertEquals(2, taskService.createTaskQuery().count());

    Job timer = managementService.createJobQuery().singleResult();
    managementService.executeJob(timer.getId());
    assertEquals(3, taskService.createTaskQuery().count());

    // Complete 2 tasks that will trigger the join
    Task task = taskService.createTaskQuery().taskDefinitionKey("firstTask").singleResult();
    taskService.complete(task.getId());
    task = taskService.createTaskQuery().taskDefinitionKey("secondTask").singleResult();
    taskService.complete(task.getId());
    assertEquals(1, taskService.createTaskQuery().count());

    // Finally, complete the task that was created due to the timer
    task = taskService.createTaskQuery().taskDefinitionKey("timerFiredTask").singleResult();
    taskService.complete(task.getId());

    testHelper.assertProcessEnded(procId);
  }

  @Deployment
  @Test
  public void testTimerWithCycle() throws Exception {
    runtimeService.startProcessInstanceByKey("nonInterruptingCycle").getId();
    TaskQuery tq = taskService.createTaskQuery().taskDefinitionKey("timerFiredTask");
    assertEquals(0, tq.count());
    moveByHours(1);
    assertEquals(1, tq.count());
    moveByHours(1);
    assertEquals(2, tq.count());

    Task task = taskService.createTaskQuery().taskDefinitionKey("task").singleResult();
    taskService.complete(task.getId());

    moveByHours(1);
    assertEquals(2, tq.count());
  }

  /*
   * see http://jira.codehaus.org/browse/ACT-1173
   */
  @Deployment
  @Test
  public void testTimerOnEmbeddedSubprocess() {
    String id = runtimeService.startProcessInstanceByKey("nonInterruptingTimerOnEmbeddedSubprocess").getId();

    TaskQuery tq = taskService.createTaskQuery().taskAssignee("kermit");

    assertEquals(1, tq.count());

    // Simulate timer
    Job timer = managementService.createJobQuery().singleResult();
    managementService.executeJob(timer.getId());

    tq = taskService.createTaskQuery().taskAssignee("kermit");

    assertEquals(2, tq.count());

    List<Task> tasks = tq.list();

    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());

    testHelper.assertProcessEnded(id);
  }

  @Deployment
  /*
   * see http://jira.codehaus.org/browse/ACT-1106
   */
  @Test
  public void testReceiveTaskWithBoundaryTimer(){
    HashMap<String, Object> variables = new HashMap<>();
    variables.put("timeCycle", "R/PT1H");

    // After process start, there should be a timer created
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("nonInterruptingCycle",variables);

    JobQuery jobQuery = managementService.createJobQuery().processInstanceId(pi.getId());
    List<Job> jobs = jobQuery.list();
    assertEquals(1, jobs.size());

    // The Execution Query should work normally and find executions in state "task"
    List<Execution> executions = runtimeService.createExecutionQuery()
                                               .activityId("task")
                                               .list();
    assertEquals(1, executions.size());
    List<String> activeActivityIds = runtimeService.getActiveActivityIds(executions.get(0).getId());
    assertEquals(1, activeActivityIds.size());
    assertEquals("task", activeActivityIds.get(0));

    runtimeService.signal(executions.get(0).getId());

//    // After setting the clock to time '1 hour and 5 seconds', the second timer should fire
//    ClockUtil.setCurrentTime(new Date(startTime.getTime() + ((60 * 60 * 1000) + 5000)));
//    testRule.waitForJobExecutorToProcessAllJobs(5000L);
//    assertEquals(0L, jobQuery.count());

    // which means the process has ended
    testHelper.assertProcessEnded(pi.getId());
  }

  @Deployment
  @Test
  public void testTimerOnConcurrentSubprocess() {
    String procId = runtimeService.startProcessInstanceByKey("testTimerOnConcurrentSubprocess").getId();
    assertEquals(4, taskService.createTaskQuery().count());

    Job timer = managementService.createJobQuery().singleResult();
    managementService.executeJob(timer.getId());
    assertEquals(5, taskService.createTaskQuery().count());

    // Complete 4 tasks that will trigger the join
    Task task = taskService.createTaskQuery().taskDefinitionKey("sub1task1").singleResult();
    taskService.complete(task.getId());
    task = taskService.createTaskQuery().taskDefinitionKey("sub1task2").singleResult();
    taskService.complete(task.getId());
    task = taskService.createTaskQuery().taskDefinitionKey("sub2task1").singleResult();
    taskService.complete(task.getId());
    task = taskService.createTaskQuery().taskDefinitionKey("sub2task2").singleResult();
    taskService.complete(task.getId());
    assertEquals(1, taskService.createTaskQuery().count());

    // Finally, complete the task that was created due to the timer
    task = taskService.createTaskQuery().taskDefinitionKey("timerFiredTask").singleResult();
    taskService.complete(task.getId());

    testHelper.assertProcessEnded(procId);
  }

  @Deployment(resources="org/camunda/bpm/engine/test/bpmn/event/timer/BoundaryTimerNonInterruptingEventTest.testTimerOnConcurrentSubprocess.bpmn20.xml")
  @Test
  public void testTimerOnConcurrentSubprocess2() {
    String procId = runtimeService.startProcessInstanceByKey("testTimerOnConcurrentSubprocess").getId();
    assertEquals(4, taskService.createTaskQuery().count());

    Job timer = managementService.createJobQuery().singleResult();
    managementService.executeJob(timer.getId());
    assertEquals(5, taskService.createTaskQuery().count());

    Task task = taskService.createTaskQuery().taskDefinitionKey("sub1task1").singleResult();
    taskService.complete(task.getId());
    task = taskService.createTaskQuery().taskDefinitionKey("sub1task2").singleResult();
    taskService.complete(task.getId());

    // complete the task that was created due to the timer
    task = taskService.createTaskQuery().taskDefinitionKey("timerFiredTask").singleResult();
    taskService.complete(task.getId());

    task = taskService.createTaskQuery().taskDefinitionKey("sub2task1").singleResult();
    taskService.complete(task.getId());
    task = taskService.createTaskQuery().taskDefinitionKey("sub2task2").singleResult();
    taskService.complete(task.getId());
    assertEquals(0, taskService.createTaskQuery().count());

    testHelper.assertProcessEnded(procId);
  }

  @Deployment
  @Test
  public void testMultipleOutgoingSequenceFlows() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("nonInterruptingTimer");

    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);

    managementService.executeJob(job.getId());

    TaskQuery taskQuery = taskService.createTaskQuery();
    assertEquals(3, taskQuery.count());

    List<Task> tasks = taskQuery.list();

    for (Task task : tasks) {
      taskService.complete(task.getId());
    }

    testHelper.assertProcessEnded(pi.getId());
  }

  @Deployment
  @Test
  public void testMultipleOutgoingSequenceFlowsOnSubprocess() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("nonInterruptingTimer");

    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);

    managementService.executeJob(job.getId());

    Task task = taskService.createTaskQuery().taskDefinitionKey("innerTask1").singleResult();
    assertNotNull(task);
    taskService.complete(task.getId());

    task = taskService.createTaskQuery().taskDefinitionKey("innerTask2").singleResult();
    assertNotNull(task);
    taskService.complete(task.getId());

    task = taskService.createTaskQuery().taskDefinitionKey("timerFiredTask1").singleResult();
    assertNotNull(task);
    taskService.complete(task.getId());

    task = taskService.createTaskQuery().taskDefinitionKey("timerFiredTask2").singleResult();
    assertNotNull(task);
    taskService.complete(task.getId());

    testHelper.assertProcessEnded(pi.getId());

    // Case 2: fire outer tasks first

    pi = runtimeService.startProcessInstanceByKey("nonInterruptingTimer");

    job = managementService.createJobQuery().singleResult();
    assertNotNull(job);

    managementService.executeJob(job.getId());

    task = taskService.createTaskQuery().taskDefinitionKey("timerFiredTask1").singleResult();
    assertNotNull(task);
    taskService.complete(task.getId());

    task = taskService.createTaskQuery().taskDefinitionKey("timerFiredTask2").singleResult();
    assertNotNull(task);
    taskService.complete(task.getId());

    task = taskService.createTaskQuery().taskDefinitionKey("innerTask1").singleResult();
    assertNotNull(task);
    taskService.complete(task.getId());

    task = taskService.createTaskQuery().taskDefinitionKey("innerTask2").singleResult();
    assertNotNull(task);
    taskService.complete(task.getId());

    testHelper.assertProcessEnded(pi.getId());
  }

  @Deployment
  @Test
  public void testMultipleOutgoingSequenceFlowsOnSubprocessMi() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("nonInterruptingTimer");

    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);

    managementService.executeJob(job.getId());

    TaskQuery taskQuery = taskService.createTaskQuery();
    assertEquals(10, taskQuery.count());

    List<Task> tasks = taskQuery.list();

    for (Task task : tasks) {
      taskService.complete(task.getId());
    }

    testHelper.assertProcessEnded(pi.getId());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/event/timer/BoundaryTimerNonInterruptingEventTest.testTimerWithCycle.bpmn20.xml"})
  @Test
  public void testTimeCycle() {
    // given
    runtimeService.startProcessInstanceByKey("nonInterruptingCycle");

    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(1, jobQuery.count());
    String jobId = jobQuery.singleResult().getId();

    // when
    managementService.executeJob(jobId);

    // then
    assertEquals(1, jobQuery.count());

    String anotherJobId = jobQuery.singleResult().getId();
    assertFalse(jobId.equals(anotherJobId));
  }

  @Deployment
  @Test
  public void testFailingTimeCycle() {
    // given
    runtimeService.startProcessInstanceByKey("process");

    JobQuery failedJobQuery = managementService.createJobQuery();
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(1, jobQuery.count());

    String jobId = jobQuery.singleResult().getId();
    failedJobQuery.jobId(jobId);

    // when (1)
    try {
      managementService.executeJob(jobId);
      fail();
    } catch (Exception e) {
      // expected
    }

    // then (1)
    Job failedJob = failedJobQuery.singleResult();
    assertEquals(2, failedJob.getRetries());

    // a new timer job has been created
    assertEquals(2, jobQuery.count());

    assertEquals(1, managementService.createJobQuery().withException().count());
    assertEquals(0, managementService.createJobQuery().noRetriesLeft().count());
    assertEquals(2, managementService.createJobQuery().withRetriesLeft().count());

    // when (2)
    try {
      managementService.executeJob(jobId);
    } catch (Exception e) {
      // expected
    }

    // then (2)
    failedJob = failedJobQuery.singleResult();
    assertEquals(1, failedJob.getRetries());

    // there are still two jobs
    assertEquals(2, jobQuery.count());

    assertEquals(1, managementService.createJobQuery().withException().count());
    assertEquals(0, managementService.createJobQuery().noRetriesLeft().count());
    assertEquals(2, managementService.createJobQuery().withRetriesLeft().count());
  }

  @Deployment
  @Test
  public void testUpdateTimerRepeat() {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    Calendar currentTime = Calendar.getInstance();
    ClockUtil.setCurrentTime(currentTime.getTime());

    // GIVEN
    // Start process instance with a non-interrupting boundary timer event
    // on a user task
    runtimeService.startProcessInstanceByKey("timerRepeat");

    // there should be a single user task for the process instance
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(1, tasks.size());
    assertEquals("User Waiting", tasks.get(0).getName());

    // there should be a single timer job (R5/PT1H)
    TimerEntity timerJob = (TimerEntity) managementService.createJobQuery().singleResult();
    assertNotNull(timerJob);
    assertEquals("R5/" + sdf.format(ClockUtil.getCurrentTime()) + "/PT1H", timerJob.getRepeat());

    // WHEN
    // we update the repeat property of the timer job
    processEngineConfiguration.getCommandExecutorTxRequired().execute((Command<Void>) commandContext -> {

      TimerEntity timerEntity = (TimerEntity) commandContext.getProcessEngineConfiguration()
        .getManagementService()
        .createJobQuery()
        .singleResult();

      // update repeat property
      timerEntity.setRepeat("R3/PT3H");

      return null;
    });

    // THEN
    // the timer job should be updated
    TimerEntity updatedTimerJob = (TimerEntity) managementService.createJobQuery().singleResult();
    assertEquals("R3/PT3H", updatedTimerJob.getRepeat());

    currentTime.add(Calendar.HOUR, 1);
    ClockUtil.setCurrentTime(currentTime.getTime());
    managementService.executeJob(timerJob.getId());

    // and when the timer executes, there should be 2 user tasks waiting
    tasks = taskService.createTaskQuery().orderByTaskCreateTime().asc().list();
    assertEquals(2, tasks.size());
    assertEquals("User Waiting", tasks.get(0).getName());
    assertEquals("Timer Fired", tasks.get(1).getName());

    // finally, the second timer job should have a DueDate in 3 hours instead of 1 hour
    // and its repeat property should be the one we updated
    TimerEntity secondTimerJob = (TimerEntity) managementService.createJobQuery().singleResult();
    currentTime.add(Calendar.HOUR, 3);
    assertEquals("R3/PT3H", secondTimerJob.getRepeat());
    assertEquals(sdf.format(currentTime.getTime()), sdf.format(secondTimerJob.getDuedate()));
  }

  @Test
  public void shouldExecuteTimerJobOnOrAfterDueDate() {
    // given
    Date currentTime = ClockTestUtil.setClockToDateWithoutMilliseconds();
    Date timerDueDate = Date.from(currentTime.toInstant().plusMillis(3000L));

    BpmnModelInstance instance = Bpmn.createExecutableProcess("timerProcess")
                                     .startEvent()
                                       .camundaAsyncBefore()
                                     .userTask("user-task-with-timer")
                                       .boundaryEvent("non-interuption-timer")
                                         .cancelActivity(false)
                                         .timerWithDuration("R/PT3S")
                                       .endEvent()
                                       .moveToActivity("user-task-with-timer")
                                     .endEvent()
                                     .done();
    testHelper.deploy(instance);
    runtimeService.startProcessInstanceByKey("timerProcess");

    // when
    testHelper.waitForJobExecutorToProcessAllJobs(6000L);

    // then
    Job timerJob = managementService.createJobQuery()
                                    .timers()
                                    .activityId("non-interuption-timer")
                                    .singleResult();
    Task userTask = taskService.createTaskQuery().singleResult();

    // assert that the timer job is not acquirable
    assertThat(userTask).isNotNull();
    assertThat(timerJob).isNotNull();
    assertThat(timerJob.getDuedate()).isEqualTo(timerDueDate);
  }

  @Test(timeout = 10000L)
  public void shouldExecuteTimeoutListenerJobOnOrAfterDueDate() {
    // given
    Date currentTime = ClockTestUtil.setClockToDateWithoutMilliseconds();
    Date timerDueDate = Date.from(currentTime.toInstant().plusMillis(3000L));

    BpmnModelInstance instance = Bpmn.createExecutableProcess("timoutProcess")
                                     .startEvent()
                                       .camundaAsyncBefore()
                                     .userTask("user-task-with-timer")
                                       .camundaTaskListenerExpressionTimeoutWithCycle(
                                           TaskListener.EVENTNAME_TIMEOUT,
                                           "${true}",
                                           "R/PT3S")
                                     .endEvent()
                                     .done();
    testHelper.deploy(instance);
    runtimeService.startProcessInstanceByKey("timoutProcess");

    // when
    testHelper.waitForJobExecutorToProcessAllJobs(6000L);

    // then
    Job timerJob = managementService.createJobQuery()
                                    .timers()
                                    .singleResult();
    Task userTask = taskService.createTaskQuery().singleResult();

    // assert that the timer job is not acquirable
    assertThat(userTask).isNotNull();
    assertThat(timerJob).isNotNull();
    assertThat(timerJob.getDuedate()).isEqualTo(timerDueDate);
  }

  //we cannot use waitForExecutor... method since there will always be one job left
  private void moveByHours(int hours) throws Exception {
    ClockUtil.setCurrentTime(new Date(ClockUtil.getCurrentTime().getTime() + ((hours * 60 * 1000 * 60) + 5000)));
    JobExecutor jobExecutor = processEngineConfiguration.getJobExecutor();
    jobExecutor.start();
    Thread.sleep(1000);
    jobExecutor.shutdown();
  }
}
