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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.joda.time.LocalDateTime;
import org.junit.Test;

/**
 * @author Joram Barrez
 */
public class BoundaryTimerEventTest extends PluggableProcessEngineTest {

  /*
   * Test for when multiple boundary timer events are defined on the same user
   * task
   *
   * Configuration: - timer 1 -> 2 hours -> secondTask - timer 2 -> 1 hour ->
   * thirdTask - timer 3 -> 3 hours -> fourthTask
   *
   * See process image next to the process xml resource
   */
  @Deployment
  @Test
  public void testMultipleTimersOnUserTask() {

    // Set the clock fixed
    Date startTime = new Date();

    // After process start, there should be 3 timers created
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("multipleTimersOnUserTask");
    JobQuery jobQuery = managementService.createJobQuery().processInstanceId(pi.getId());
    List<Job> jobs = jobQuery.list();
    assertEquals(3, jobs.size());

    // After setting the clock to time '1 hour and 5 seconds', the second timer should fire
    ClockUtil.setCurrentTime(new Date(startTime.getTime() + ((60 * 60 * 1000) + 5000)));
    testRule.waitForJobExecutorToProcessAllJobs(5000L);
    assertEquals(0L, jobQuery.count());

    // which means that the third task is reached
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("Third Task", task.getName());
  }

  @Deployment
  @Test
  public void testTimerOnNestingOfSubprocesses() {

    runtimeService.startProcessInstanceByKey("timerOnNestedSubprocesses");
    List<Task> tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    assertEquals(2, tasks.size());
    assertEquals("Inner subprocess task 1", tasks.get(0).getName());
    assertEquals("Inner subprocess task 2", tasks.get(1).getName());

    Job timer = managementService.createJobQuery().timers().singleResult();
    managementService.executeJob(timer.getId());

    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("task outside subprocess", task.getName());
  }

  @Deployment
  @Test
  public void testExpressionOnTimer(){
    // Set the clock fixed
    Date startTime = new Date();

    HashMap<String, Object> variables = new HashMap<String, Object>();
    variables.put("duration", "PT1H");

    // After process start, there should be a timer created
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testExpressionOnTimer", variables);

    JobQuery jobQuery = managementService.createJobQuery().processInstanceId(pi.getId());
    List<Job> jobs = jobQuery.list();
    assertEquals(1, jobs.size());

    // After setting the clock to time '1 hour and 5 seconds', the second timer should fire
    ClockUtil.setCurrentTime(new Date(startTime.getTime() + ((60 * 60 * 1000) + 5000)));
    testRule.waitForJobExecutorToProcessAllJobs(5000L);
    assertEquals(0L, jobQuery.count());

    // which means the process has ended
    testRule.assertProcessEnded(pi.getId());
  }

  @Deployment
  @Test
  public void testRecalculateUnchangedExpressionOnTimerCurrentDateBased(){
    // Set the clock fixed
    Date startTime = new Date();

    HashMap<String, Object> variables = new HashMap<String, Object>();
    variables.put("duedate", "PT1H");

    // After process start, there should be a timer created
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testExpressionOnTimer", variables);

    JobQuery jobQuery = managementService.createJobQuery().processInstanceId(pi.getId());
    List<Job> jobs = jobQuery.list();
    assertEquals(1, jobs.size());
    Job job = jobs.get(0);
    Date oldDate = job.getDuedate();

    // After recalculation of the timer, the job's duedate should be changed
    Date currentTime = new Date(startTime.getTime() + TimeUnit.MINUTES.toMillis(5));
    ClockUtil.setCurrentTime(currentTime);
    managementService.recalculateJobDuedate(job.getId(), false);
    Job jobUpdated = jobQuery.singleResult();
    assertEquals(job.getId(), jobUpdated.getId());
    assertNotEquals(oldDate, jobUpdated.getDuedate());
    assertTrue(oldDate.before(jobUpdated.getDuedate()));
    Date expectedDate = LocalDateTime.fromDateFields(currentTime).plusHours(1).toDate();
    assertThat(jobUpdated.getDuedate()).isCloseTo(expectedDate, 1000l);

    // After setting the clock to time '1 hour and 6 min', the second timer should fire
    ClockUtil.setCurrentTime(new Date(startTime.getTime() + TimeUnit.HOURS.toMillis(1L) + TimeUnit.MINUTES.toMillis(6L)));
    testRule.waitForJobExecutorToProcessAllJobs(5000L);
    assertEquals(0L, jobQuery.count());

    // which means the process has ended
    testRule.assertProcessEnded(pi.getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/timer/BoundaryTimerEventTest.testRecalculateUnchangedExpressionOnTimerCurrentDateBased.bpmn20.xml")
  @Test
  public void testRecalculateUnchangedExpressionOnTimerCreationDateBased(){
    // Set the clock fixed
    Date startTime = new Date();

    HashMap<String, Object> variables = new HashMap<String, Object>();
    variables.put("duedate", "PT1H");

    // After process start, there should be a timer created
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testExpressionOnTimer", variables);

    JobQuery jobQuery = managementService.createJobQuery().processInstanceId(pi.getId());
    List<Job> jobs = jobQuery.list();
    assertEquals(1, jobs.size());
    Job job = jobs.get(0);

    // After recalculation of the timer, the job's duedate should be based on the creation date
    ClockUtil.setCurrentTime(new Date(startTime.getTime() + TimeUnit.SECONDS.toMillis(5)));
    managementService.recalculateJobDuedate(job.getId(), true);
    Job jobUpdated = jobQuery.singleResult();
    assertEquals(job.getId(), jobUpdated.getId());
    Date expectedDate = LocalDateTime.fromDateFields(jobUpdated.getCreateTime()).plusHours(1).toDate();
    assertEquals(expectedDate, jobUpdated.getDuedate());

    // After setting the clock to time '1 hour and 15 seconds', the second timer should fire
    ClockUtil.setCurrentTime(new Date(startTime.getTime() + TimeUnit.HOURS.toMillis(1L) + TimeUnit.SECONDS.toMillis(15L)));
    testRule.waitForJobExecutorToProcessAllJobs(5000L);
    assertEquals(0L, jobQuery.count());

    // which means the process has ended
    testRule.assertProcessEnded(pi.getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/timer/BoundaryTimerEventTest.testRecalculateUnchangedExpressionOnTimerCurrentDateBased.bpmn20.xml")
  @Test
  public void testRecalculateChangedExpressionOnTimerCurrentDateBased(){
    // Set the clock fixed
    Date startTime = new Date();

    HashMap<String, Object> variables = new HashMap<String, Object>();
    variables.put("duedate", "PT1H");

    // After process start, there should be a timer created
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testExpressionOnTimer", variables);

    JobQuery jobQuery = managementService.createJobQuery().processInstanceId(pi.getId());
    List<Job> jobs = jobQuery.list();
    assertEquals(1, jobs.size());
    Job job = jobs.get(0);
    Date oldDate = job.getDuedate();
    ClockUtil.offset(2000L);

    // After recalculation of the timer, the job's duedate should be changed
    managementService.recalculateJobDuedate(job.getId(), false);
    Job jobUpdated = jobQuery.singleResult();
    assertEquals(job.getId(), jobUpdated.getId());
    assertNotEquals(oldDate, jobUpdated.getDuedate());
    assertTrue(oldDate.before(jobUpdated.getDuedate()));

    // After setting the clock to time '16 minutes', the timer should fire
    ClockUtil.setCurrentTime(new Date(startTime.getTime() + TimeUnit.HOURS.toMillis(2L)));
    testRule.waitForJobExecutorToProcessAllJobs(5000L);
    assertEquals(0L, jobQuery.count());

    // which means the process has ended
    testRule.assertProcessEnded(pi.getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/timer/BoundaryTimerEventTest.testRecalculateUnchangedExpressionOnTimerCurrentDateBased.bpmn20.xml")
  @Test
  public void testRecalculateChangedExpressionOnTimerCreationDateBased(){
    // Set the clock fixed
    Date startTime = new Date();

    HashMap<String, Object> variables = new HashMap<String, Object>();
    variables.put("duedate", "PT1H");

    // After process start, there should be a timer created
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testExpressionOnTimer", variables);

    JobQuery jobQuery = managementService.createJobQuery().processInstanceId(pi.getId());
    List<Job> jobs = jobQuery.list();
    assertEquals(1, jobs.size());
    Job job = jobs.get(0);
    Date oldDate = job.getDuedate();

    // After recalculation of the timer, the job's duedate should be the same
    runtimeService.setVariable(pi.getId(), "duedate", "PT15M");
    managementService.recalculateJobDuedate(job.getId(), true);
    Job jobUpdated = jobQuery.singleResult();
    assertEquals(job.getId(), jobUpdated.getId());
    assertNotEquals(oldDate, jobUpdated.getDuedate());
    assertEquals(LocalDateTime.fromDateFields(jobUpdated.getCreateTime()).plusMinutes(15).toDate(), jobUpdated.getDuedate());

    // After setting the clock to time '16 minutes', the timer should fire
    ClockUtil.setCurrentTime(new Date(startTime.getTime() + TimeUnit.MINUTES.toMillis(16L)));
    testRule.waitForJobExecutorToProcessAllJobs(5000L);
    assertEquals(0L, jobQuery.count());

    // which means the process has ended
    testRule.assertProcessEnded(pi.getId());
  }

  @Deployment
  @Test
  public void testTimerInSingleTransactionProcess() {
    // make sure that if a PI completes in single transaction, JobEntities associated with the execution are deleted.
    // broken before 5.10, see ACT-1133
    runtimeService.startProcessInstanceByKey("timerOnSubprocesses");
    assertEquals(0, managementService.createJobQuery().count());
  }

  @Deployment
  @Test
  public void testRepeatingTimerWithCancelActivity() {
    runtimeService.startProcessInstanceByKey("repeatingTimerAndCallActivity");
    assertEquals(1, managementService.createJobQuery().count());
    assertEquals(1, taskService.createTaskQuery().count());

    // Firing job should cancel the user task, destroy the scope,
    // re-enter the task and recreate the task. A new timer should also be created.
    // This didn't happen before 5.11 (new jobs kept being created). See ACT-1427
    Job job = managementService.createJobQuery().singleResult();
    managementService.executeJob(job.getId());
    assertEquals(1, managementService.createJobQuery().count());
    assertEquals(1, taskService.createTaskQuery().count());
  }

  @Deployment
  @Test
  public void testMultipleOutgoingSequenceFlows() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("interruptingTimer");

    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);

    managementService.executeJob(job.getId());

    TaskQuery taskQuery = taskService.createTaskQuery();
    assertEquals(2, taskQuery.count());

    List<Task> tasks = taskQuery.list();

    for (Task task : tasks) {
      taskService.complete(task.getId());
    }

    testRule.assertProcessEnded(pi.getId());
  }

  @Deployment
  @Test
  public void testMultipleOutgoingSequenceFlowsOnSubprocess() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("interruptingTimer");

    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);

    managementService.executeJob(job.getId());

    TaskQuery taskQuery = taskService.createTaskQuery();
    assertEquals(2, taskQuery.count());

    List<Task> tasks = taskQuery.list();

    for (Task task : tasks) {
      taskService.complete(task.getId());
    }

    testRule.assertProcessEnded(pi.getId());
  }

  @Deployment
  @Test
  public void testMultipleOutgoingSequenceFlowsOnSubprocessMi() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("interruptingTimer");

    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);

    managementService.executeJob(job.getId());

    TaskQuery taskQuery = taskService.createTaskQuery();
    assertEquals(2, taskQuery.count());

    List<Task> tasks = taskQuery.list();

    for (Task task : tasks) {
      taskService.complete(task.getId());
    }

    testRule.assertProcessEnded(pi.getId());
  }

  @Deployment
  @Test
  public void testInterruptingTimerDuration() {

    // Start process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("escalationExample");

    // There should be one task, with a timer : first line support
    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("First line support", task.getName());

    // Manually execute the job
    Job timer = managementService.createJobQuery().singleResult();
    managementService.executeJob(timer.getId());

    // The timer has fired, and the second task (secondlinesupport) now exists
    task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("Handle escalated issue", task.getName());
  }

}
