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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.cmd.DeleteJobsCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.runtime.ExecutionQuery;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.runtime.ProcessInstantiationBuilder;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.ProcessBuilder;
import org.joda.time.LocalDateTime;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Joram Barrez
 */
public class StartTimerEventTest extends PluggableProcessEngineTest {

  @Deployment
  @Test
  public void testDurationStartTimerEvent() throws Exception {
    // Set the clock fixed
    Date startTime = new Date();

    // After process start, there should be timer created
    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(1, jobQuery.count());

    // After setting the clock to time '50minutes and 5 seconds', the second
    // timer should fire
    ClockUtil.setCurrentTime(new Date(startTime.getTime() + ((50 * 60 * 1000) + 5000)));

    executeAllJobs();

    executeAllJobs();

    List<ProcessInstance> pi = runtimeService.createProcessInstanceQuery().processDefinitionKey("startTimerEventExample").list();
    assertEquals(1, pi.size());

    assertEquals(0, jobQuery.count());

  }

  @Deployment
  @Test
  public void testFixedDateStartTimerEvent() throws Exception {

    // After process start, there should be timer created
    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(1, jobQuery.count());

    ClockUtil.setCurrentTime(new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse("15/11/2036 11:12:30"));
    executeAllJobs();

    List<ProcessInstance> pi = runtimeService.createProcessInstanceQuery().processDefinitionKey("startTimerEventExample").list();
    assertEquals(1, pi.size());

    assertEquals(0, jobQuery.count());

  }

  // FIXME: This test likes to run in an endless loop when invoking the
  // waitForJobExecutorOnCondition method
  @Deployment
  @Ignore
  @Test
  public void testCycleDateStartTimerEvent() throws Exception {
    ClockUtil.setCurrentTime(new Date());

    // After process start, there should be timer created
    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(1, jobQuery.count());

    final ProcessInstanceQuery piq = runtimeService.createProcessInstanceQuery().processDefinitionKey("startTimerEventExample");

    assertEquals(0, piq.count());

    moveByMinutes(5);
    executeAllJobs();
    assertEquals(1, piq.count());
    assertEquals(1, jobQuery.count());

    moveByMinutes(5);
    executeAllJobs();
    assertEquals(1, piq.count());

    assertEquals(1, jobQuery.count());
    // have to manually delete pending timer
//    cleanDB();

  }

  private void moveByMinutes(int minutes) throws Exception {
    ClockUtil.setCurrentTime(new Date(ClockUtil.getCurrentTime().getTime() + ((minutes * 60 * 1000) + 5000)));
  }

  @Deployment
  @Test
  public void testCycleWithLimitStartTimerEvent() throws Exception {
    ClockUtil.setCurrentTime(new Date());

    // After process start, there should be timer created
    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(1, jobQuery.count());

    // ensure that the deployment Id is set on the new job
    Job job = jobQuery.singleResult();
    assertNotNull(job.getDeploymentId());

    final ProcessInstanceQuery piq = runtimeService.createProcessInstanceQuery().processDefinitionKey("startTimerEventExampleCycle");

    assertEquals(0, piq.count());

    moveByMinutes(5);
    executeAllJobs();
    assertEquals(1, piq.count());
    assertEquals(1, jobQuery.count());

    // ensure that the deployment Id is set on the new job
    job = jobQuery.singleResult();
    assertNotNull(job.getDeploymentId());

    moveByMinutes(5);
    executeAllJobs();
    assertEquals(2, piq.count());
    assertEquals(0, jobQuery.count());

  }

  @Deployment
  @Test
  public void testPriorityInTimerCycleEvent() throws Exception {
    ClockUtil.setCurrentTime(new Date());

    // After process start, there should be timer created
    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(1, jobQuery.count());

    // ensure that the deployment Id is set on the new job
    Job job = jobQuery.singleResult();
    assertNotNull(job.getDeploymentId());
    assertEquals(9999, job.getPriority());

    final ProcessInstanceQuery piq = runtimeService.createProcessInstanceQuery()
      .processDefinitionKey("startTimerEventExampleCycle");

    assertEquals(0, piq.count());

    moveByMinutes(5);
    executeAllJobs();
    assertEquals(1, piq.count());
    assertEquals(1, jobQuery.count());

    // ensure that the deployment Id is set on the new job
    job = jobQuery.singleResult();
    assertNotNull(job.getDeploymentId());

    // second job should have the same priority
    assertEquals(9999, job.getPriority());
  }

  @Deployment
  @Test
  public void testExpressionStartTimerEvent() throws Exception {
    // ACT-1415: fixed start-date is an expression
    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(1, jobQuery.count());

    ClockUtil.setCurrentTime(new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse("15/11/2036 11:12:30"));
    executeAllJobs();

    List<ProcessInstance> pi = runtimeService.createProcessInstanceQuery().processDefinitionKey("startTimerEventExample").list();
    assertEquals(1, pi.size());

    assertEquals(0, jobQuery.count());
  }
  
  @Deployment
  @Test
  public void testRecalculateExpressionStartTimerEvent() throws Exception {
    // given
    JobQuery jobQuery = managementService.createJobQuery();
    ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery().processDefinitionKey("startTimerEventExample");
    assertEquals(1, jobQuery.count());
    assertEquals(0, processInstanceQuery.count());
    
    Job job = jobQuery.singleResult();
    Date oldDate = job.getDuedate();
    
    // when
    moveByMinutes(2);
    Date currentTime = ClockUtil.getCurrentTime();
    managementService.recalculateJobDuedate(job.getId(), false);
    
    // then
    assertEquals(1, jobQuery.count());
    assertEquals(0, processInstanceQuery.count());
    
    Date newDate = jobQuery.singleResult().getDuedate();
    assertNotEquals(oldDate, newDate);
    assertTrue(oldDate.before(newDate));
    Date expectedDate = LocalDateTime.fromDateFields(currentTime).plusHours(2).toDate();
    assertThat(newDate).isCloseTo(expectedDate, 1000l);

    // move the clock forward 2 hours and 2 min
    moveByMinutes(122);
    executeAllJobs();

    List<ProcessInstance> pi = processInstanceQuery.list();
    assertEquals(1, pi.size());

    assertEquals(0, jobQuery.count());
  }
  
  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/timer/StartTimerEventTest.testRecalculateExpressionStartTimerEvent.bpmn20.xml")
  @Test
  public void testRecalculateUnchangedExpressionStartTimerEventCreationDateBased() throws Exception {
    // given
    JobQuery jobQuery = managementService.createJobQuery();
    ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery().processDefinitionKey("startTimerEventExample");
    assertEquals(1, jobQuery.count());
    assertEquals(0, processInstanceQuery.count());
    
    // when
    moveByMinutes(1);
    managementService.recalculateJobDuedate(jobQuery.singleResult().getId(), true);
    
    // then due date should be based on the creation time
    assertEquals(1, jobQuery.count());
    assertEquals(0, processInstanceQuery.count());
    
    Job jobUpdated = jobQuery.singleResult();
    Date expectedDate = LocalDateTime.fromDateFields(jobUpdated.getCreateTime()).plusHours(2).toDate();
    assertEquals(expectedDate, jobUpdated.getDuedate());

    // move the clock forward 2 hours and 1 minute
    moveByMinutes(121);
    executeAllJobs();

    List<ProcessInstance> pi = processInstanceQuery.list();
    assertEquals(1, pi.size());

    assertEquals(0, jobQuery.count());
  }

  @Deployment
  @Test
  public void testVersionUpgradeShouldCancelJobs() throws Exception {
    ClockUtil.setCurrentTime(new Date());

    // After process start, there should be timer created
    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(1, jobQuery.count());

    // we deploy new process version, with some small change
    InputStream in = getClass().getResourceAsStream("StartTimerEventTest.testVersionUpgradeShouldCancelJobs.bpmn20.xml");
    String process = new String(IoUtil.readInputStream(in, "")).replaceAll("beforeChange", "changed");
    IoUtil.closeSilently(in);
    in = new ByteArrayInputStream(process.getBytes());
    String id = repositoryService.createDeployment().addInputStream("StartTimerEventTest.testVersionUpgradeShouldCancelJobs.bpmn20.xml", in).deploy().getId();
    IoUtil.closeSilently(in);

    assertEquals(1, jobQuery.count());

    moveByMinutes(5);
    executeAllJobs();
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processDefinitionKey("startTimerEventExample").singleResult();
    String pi = processInstance.getProcessInstanceId();
    assertEquals("changed", runtimeService.getActiveActivityIds(pi).get(0));

    assertEquals(1, jobQuery.count());

//    cleanDB();
    repositoryService.deleteDeployment(id, true);
  }

  @Deployment
  @Test
  public void testTimerShouldNotBeRecreatedOnDeploymentCacheReboot() {

    // Just to be sure, I added this test. Sounds like something that could
    // easily happen
    // when the order of deploy/parsing is altered.

    // After process start, there should be timer created
    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(1, jobQuery.count());

    // Reset deployment cache
    processEngineConfiguration.getDeploymentCache().discardProcessDefinitionCache();

    // Start one instance of the process definition, this will trigger a cache
    // reload
    runtimeService.startProcessInstanceByKey("startTimer");

    // No new jobs should have been created
    assertEquals(1, jobQuery.count());
  }

  // Test for ACT-1533
  @Test
  public void testTimerShouldNotBeRemovedWhenUndeployingOldVersion() throws Exception {
    // Deploy test process
    InputStream in = getClass().getResourceAsStream("StartTimerEventTest.testTimerShouldNotBeRemovedWhenUndeployingOldVersion.bpmn20.xml");
    String process = new String(IoUtil.readInputStream(in, ""));
    IoUtil.closeSilently(in);

    in = new ByteArrayInputStream(process.getBytes());
    String firstDeploymentId = repositoryService.createDeployment().addInputStream("StartTimerEventTest.testVersionUpgradeShouldCancelJobs.bpmn20.xml", in)
        .deploy().getId();
    IoUtil.closeSilently(in);

    // After process start, there should be timer created
    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(1, jobQuery.count());

    // we deploy new process version, with some small change
    String processChanged = process.replaceAll("beforeChange", "changed");
    in = new ByteArrayInputStream(processChanged.getBytes());
    String secondDeploymentId = repositoryService.createDeployment().addInputStream("StartTimerEventTest.testVersionUpgradeShouldCancelJobs.bpmn20.xml", in)
        .deploy().getId();
    IoUtil.closeSilently(in);
    assertEquals(1, jobQuery.count());

    // Remove the first deployment
    repositoryService.deleteDeployment(firstDeploymentId, true);

    // The removal of an old version should not affect timer deletion
    // ACT-1533: this was a bug, and the timer was deleted!
    assertEquals(1, jobQuery.count());

    // Cleanup
    cleanDB();
    repositoryService.deleteDeployment(secondDeploymentId, true);
  }

  @Deployment
  @Test
  public void testStartTimerEventInEventSubProcess() {
    DummyServiceTask.wasExecuted = false;

    // start process instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startTimerEventInEventSubProcess");

    // check if execution exists
    ExecutionQuery executionQuery = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId());
    assertEquals(1, executionQuery.count());

    // check if user task exists
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(processInstance.getId());
    assertEquals(1, taskQuery.count());

    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(1, jobQuery.count());
    // execute existing timer job
    managementService.executeJob(managementService.createJobQuery().list().get(0).getId());
    assertEquals(0, jobQuery.count());

    assertEquals(true, DummyServiceTask.wasExecuted);

    // check if user task doesn't exist because timer start event is
    // interrupting
    assertEquals(0, taskQuery.count());

    // check if execution doesn't exist because timer start event is
    // interrupting
    assertEquals(0, executionQuery.count());

    ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId());
    assertEquals(0, processInstanceQuery.count());
  }

  @Test
  @Deployment
  public void shouldEvaluateExpressionStartTimerEventInEventSubprocess() {
    // given
    ProcessInstantiationBuilder builder = runtimeService.createProcessInstanceByKey("shouldEvaluateExpressionStartTimerEventInEventSubprocess")
        .setVariable("duration", "PT5M");

    // when
    ProcessInstance processInstance = builder.startBeforeActivity("processUserTask").execute();

    // then
    ProcessInstance startedProcessInstance = runtimeService.createProcessInstanceQuery().singleResult();
    // make sure process instance was started
    assertThat(processInstance.getId()).isEqualTo(startedProcessInstance.getId());
    
  }

  @Deployment
  @Test
  public void testNonInterruptingStartTimerEventInEventSubProcess() {
    DummyServiceTask.wasExecuted = false;

    // start process instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nonInterruptingStartTimerEventInEventSubProcess");

    // check if execution exists
    ExecutionQuery executionQuery = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId());
    assertEquals(1, executionQuery.count());

    // check if user task exists
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(processInstance.getId());
    assertEquals(1, taskQuery.count());

    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(1, jobQuery.count());
    // execute existing job timer
    managementService.executeJob(managementService.createJobQuery().list().get(0).getId());
    assertEquals(0, jobQuery.count());

    assertEquals(true, DummyServiceTask.wasExecuted);

    // check if user task still exists because timer start event is non
    // interrupting
    assertEquals(1, taskQuery.count());

    // check if execution still exists because timer start event is non
    // interrupting
    assertEquals(1, executionQuery.count());

    ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId());
    assertEquals(1, processInstanceQuery.count());
  }

  @Deployment
  @Test
  public void testStartTimerEventSubProcessInSubProcess() {
    DummyServiceTask.wasExecuted = false;

    // start process instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startTimerEventSubProcessInSubProcess");

    // check if execution exists
    ExecutionQuery executionQuery = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId());
    assertEquals(2, executionQuery.count());

    // check if user task exists
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(processInstance.getId());
    assertEquals(1, taskQuery.count());

    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(1, jobQuery.count());
    // execute existing timer job
    managementService.executeJob(managementService.createJobQuery().list().get(0).getId());
    assertEquals(0, jobQuery.count());

    assertEquals(true, DummyServiceTask.wasExecuted);

    // check if user task doesn't exist because timer start event is
    // interrupting
    assertEquals(0, taskQuery.count());

    // check if execution doesn't exist because timer start event is
    // interrupting
    assertEquals(0, executionQuery.count());

    ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId());
    assertEquals(0, processInstanceQuery.count());

  }

  @Deployment
  @Test
  public void testNonInterruptingStartTimerEventSubProcessInSubProcess() {
    DummyServiceTask.wasExecuted = false;

    // start process instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nonInterruptingStartTimerEventSubProcessInSubProcess");

    // check if execution exists
    ExecutionQuery executionQuery = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId());
    assertEquals(2, executionQuery.count());

    // check if user task exists
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(processInstance.getId());
    assertEquals(1, taskQuery.count());

    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(1, jobQuery.count());
    // execute existing timer job
    managementService.executeJob(jobQuery.list().get(0).getId());
    assertEquals(0, jobQuery.count());

    assertEquals(true, DummyServiceTask.wasExecuted);

    // check if user task still exists because timer start event is non
    // interrupting
    assertEquals(1, taskQuery.count());

    // check if execution still exists because timer start event is non
    // interrupting
    assertEquals(2, executionQuery.count());

    ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId());
    assertEquals(1, processInstanceQuery.count());

  }

  @Deployment
  @Test
  public void testStartTimerEventWithTwoEventSubProcesses() {
    // start process instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startTimerEventWithTwoEventSubProcesses");

    // check if execution exists
    ExecutionQuery executionQuery = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId());
    assertEquals(1, executionQuery.count());

    // check if user task exists
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(processInstance.getId());
    assertEquals(1, taskQuery.count());

    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(2, jobQuery.count());
    // get all timer jobs ordered by dueDate
    List<Job> orderedJobList = jobQuery.orderByJobDuedate().asc().list();
    // execute first timer job
    managementService.executeJob(orderedJobList.get(0).getId());
    assertEquals(0, jobQuery.count());

    // check if user task doesn't exist because timer start event is
    // interrupting
    assertEquals(0, taskQuery.count());

    // check if execution doesn't exist because timer start event is
    // interrupting
    assertEquals(0, executionQuery.count());

    // check if process instance doesn't exist because timer start event is
    // interrupting
    ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId());
    assertEquals(0, processInstanceQuery.count());

  }

  @Deployment
  @Test
  public void testNonInterruptingStartTimerEventWithTwoEventSubProcesses() {
    DummyServiceTask.wasExecuted = false;

    // start process instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nonInterruptingStartTimerEventWithTwoEventSubProcesses");

    // check if execution exists
    ExecutionQuery executionQuery = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId());
    assertEquals(1, executionQuery.count());

    // check if user task exists
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(processInstance.getId());
    assertEquals(1, taskQuery.count());

    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(2, jobQuery.count());
    // get all timer jobs ordered by dueDate
    List<Job> orderedJobList = jobQuery.orderByJobDuedate().asc().list();
    // execute first timer job
    managementService.executeJob(orderedJobList.get(0).getId());
    assertEquals(1, jobQuery.count());

    assertEquals(true, DummyServiceTask.wasExecuted);

    DummyServiceTask.wasExecuted = false;

    // check if user task still exists because timer start event is non
    // interrupting
    assertEquals(1, taskQuery.count());

    // check if execution still exists because timer start event is non
    // interrupting
    assertEquals(1, executionQuery.count());

    // execute second timer job
    managementService.executeJob(orderedJobList.get(1).getId());
    assertEquals(0, jobQuery.count());

    assertEquals(true, DummyServiceTask.wasExecuted);

    // check if user task still exists because timer start event is non
    // interrupting
    assertEquals(1, taskQuery.count());

    // check if execution still exists because timer event is non interrupting
    assertEquals(1, executionQuery.count());

    ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId());
    assertEquals(1, processInstanceQuery.count());

  }

  @Deployment
  @Test
  public void testStartTimerEventSubProcessWithUserTask() {
    // start process instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startTimerEventSubProcessWithUserTask");

    // check if user task exists
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(processInstance.getId());
    assertEquals(1, taskQuery.count());

    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(2, jobQuery.count());
    // get all timer jobs ordered by dueDate
    List<Job> orderedJobList = jobQuery.orderByJobDuedate().asc().list();
    // execute first timer job
    managementService.executeJob(orderedJobList.get(0).getId());
    assertEquals(0, jobQuery.count());

    // check if user task of event subprocess named "subProcess" exists
    assertEquals(1, taskQuery.count());
    assertEquals("subprocessUserTask", taskQuery.list().get(0).getTaskDefinitionKey());

    // check if process instance exists because subprocess named "subProcess" is
    // already running
    ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId());
    assertEquals(1, processInstanceQuery.count());

  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/event/timer/simpleProcessWithCallActivity.bpmn20.xml",
      "org/camunda/bpm/engine/test/bpmn/event/timer/StartTimerEventTest.testStartTimerEventWithTwoEventSubProcesses.bpmn20.xml" })
  @Test
  public void testStartTimerEventSubProcessCalledFromCallActivity() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("calledProcess", "startTimerEventWithTwoEventSubProcesses");
    // start process instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("simpleCallActivityProcess", variables);

    // check if execution exists
    ExecutionQuery executionQuery = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId());
    assertEquals(2, executionQuery.count());

    // check if user task exists
    TaskQuery taskQuery = taskService.createTaskQuery();
    assertEquals(1, taskQuery.count());

    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(2, jobQuery.count());
    // get all timer jobs ordered by dueDate
    List<Job> orderedJobList = jobQuery.orderByJobDuedate().asc().list();
    // execute first timer job
    managementService.executeJob(orderedJobList.get(0).getId());
    assertEquals(0, jobQuery.count());

    // check if user task doesn't exist because timer start event is
    // interrupting
    assertEquals(0, taskQuery.count());

    // check if execution doesn't exist because timer start event is
    // interrupting
    assertEquals(0, executionQuery.count());

    // check if process instance doesn't exist because timer start event is
    // interrupting
    ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId());
    assertEquals(0, processInstanceQuery.count());

  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/event/timer/simpleProcessWithCallActivity.bpmn20.xml",
      "org/camunda/bpm/engine/test/bpmn/event/timer/StartTimerEventTest.testNonInterruptingStartTimerEventWithTwoEventSubProcesses.bpmn20.xml" })
  @Test
  public void testNonInterruptingStartTimerEventSubProcessesCalledFromCallActivity() {
    DummyServiceTask.wasExecuted = false;

    // start process instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nonInterruptingStartTimerEventWithTwoEventSubProcesses");

    // check if execution exists
    ExecutionQuery executionQuery = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId());
    assertEquals(1, executionQuery.count());

    // check if user task exists
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(processInstance.getId());
    assertEquals(1, taskQuery.count());

    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(2, jobQuery.count());
    // get all timer jobs ordered by dueDate
    List<Job> orderedJobList = jobQuery.orderByJobDuedate().asc().list();
    // execute first timer job
    managementService.executeJob(orderedJobList.get(0).getId());
    assertEquals(1, jobQuery.count());

    assertEquals(true, DummyServiceTask.wasExecuted);

    DummyServiceTask.wasExecuted = false;

    // check if user task still exists because timer start event is non
    // interrupting
    assertEquals(1, taskQuery.count());

    // check if execution still exists because timer start event is non
    // interrupting
    assertEquals(1, executionQuery.count());

    // execute second timer job
    managementService.executeJob(orderedJobList.get(1).getId());
    assertEquals(0, jobQuery.count());

    assertEquals(true, DummyServiceTask.wasExecuted);

    // check if user task still exists because timer start event is non
    // interrupting
    assertEquals(1, taskQuery.count());

    // check if execution still exists because timer event is non interrupting
    assertEquals(1, executionQuery.count());

    ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId());
    assertEquals(1, processInstanceQuery.count());

  }

  @Deployment
  @Test
  public void testStartTimerEventSubProcessInMultiInstanceSubProcess() {
    DummyServiceTask.wasExecuted = false;

    // start process instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startTimerEventSubProcessInMultiInstanceSubProcess");

    // check if user task exists
    TaskQuery taskQuery = taskService.createTaskQuery();
    assertEquals(1, taskQuery.count());

    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(1, jobQuery.count());
    String jobIdFirstLoop = jobQuery.list().get(0).getId();
    // execute timer job
    managementService.executeJob(jobIdFirstLoop);

    assertEquals(true, DummyServiceTask.wasExecuted);
    DummyServiceTask.wasExecuted = false;

    // execute multiInstance loop number 2
    assertEquals(1, taskQuery.count());
    assertEquals(1, jobQuery.count());
    String jobIdSecondLoop = jobQuery.list().get(0).getId();
    assertNotSame(jobIdFirstLoop, jobIdSecondLoop);
    // execute timer job
    managementService.executeJob(jobIdSecondLoop);

    assertEquals(true, DummyServiceTask.wasExecuted);

    // multiInstance loop finished
    assertEquals(0, jobQuery.count());

    // check if user task doesn't exist because timer start event is
    // interrupting
    assertEquals(0, taskQuery.count());

    // check if process instance doesn't exist because timer start event is
    // interrupting
    testRule.assertProcessEnded(processInstance.getId());
  }

  @Deployment
  @Test
  public void testNonInterruptingStartTimerEventInMultiInstanceEventSubProcess() {
    DummyServiceTask.wasExecuted = false;

    // start process instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nonInterruptingStartTimerEventInMultiInstanceEventSubProcess");

    // execute multiInstance loop number 1

    // check if user task exists
    TaskQuery taskQuery = taskService.createTaskQuery();
    assertEquals(1, taskQuery.count());

    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(1, jobQuery.count());
    String jobIdFirstLoop = jobQuery.list().get(0).getId();
    // execute timer job
    managementService.executeJob(jobIdFirstLoop);

    assertEquals(true, DummyServiceTask.wasExecuted);
    DummyServiceTask.wasExecuted = false;

    assertEquals(1, taskQuery.count());
    // complete existing task to start new execution for multi instance loop
    // number 2
    taskService.complete(taskQuery.list().get(0).getId());

    // execute multiInstance loop number 2
    assertEquals(1, taskQuery.count());
    assertEquals(1, jobQuery.count());
    String jobIdSecondLoop = jobQuery.list().get(0).getId();
    assertNotSame(jobIdFirstLoop, jobIdSecondLoop);
    // execute timer job
    managementService.executeJob(jobIdSecondLoop);

    assertEquals(true, DummyServiceTask.wasExecuted);

    // multiInstance loop finished
    assertEquals(0, jobQuery.count());

    // check if user task doesn't exist because timer start event is
    // interrupting
    assertEquals(1, taskQuery.count());

    // check if process instance doesn't exist because timer start event is
    // interrupting
    ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId());
    assertEquals(1, processInstanceQuery.count());

  }

  @Deployment
  @Test
  public void testStartTimerEventSubProcessInParallelMultiInstanceSubProcess() {
    DummyServiceTask.wasExecuted = false;

    // start process instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startTimerEventSubProcessInParallelMultiInstanceSubProcess");

    // check if execution exists
    ExecutionQuery executionQuery = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId());
    assertEquals(6, executionQuery.count());

    // check if user task exists
    TaskQuery taskQuery = taskService.createTaskQuery();
    assertEquals(2, taskQuery.count());

    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(2, jobQuery.count());
    // execute timer job
    for (Job job : jobQuery.list()) {
      managementService.executeJob(job.getId());

      assertEquals(true, DummyServiceTask.wasExecuted);
      DummyServiceTask.wasExecuted = false;
    }

    // check if user task doesn't exist because timer start event is
    // interrupting
    assertEquals(0, taskQuery.count());

    // check if execution doesn't exist because timer start event is
    // interrupting
    assertEquals(0, executionQuery.count());

    // check if process instance doesn't exist because timer start event is
    // interrupting
    ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId());
    assertEquals(0, processInstanceQuery.count());

  }

  @Deployment
  @Test
  public void testNonInterruptingStartTimerEventSubProcessWithParallelMultiInstance() {
    DummyServiceTask.wasExecuted = false;

    // start process instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nonInterruptingParallelMultiInstance");

    // check if execution exists
    ExecutionQuery executionQuery = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId());
    assertEquals(6, executionQuery.count());

    // check if user task exists
    TaskQuery taskQuery = taskService.createTaskQuery();
    assertEquals(2, taskQuery.count());

    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(2, jobQuery.count());
    // execute all timer jobs
    for (Job job : jobQuery.list()) {
      managementService.executeJob(job.getId());

      assertEquals(true, DummyServiceTask.wasExecuted);
      DummyServiceTask.wasExecuted = false;
    }

    assertEquals(0, jobQuery.count());

    // check if user task doesn't exist because timer start event is
    // interrupting
    assertEquals(2, taskQuery.count());

    // check if execution doesn't exist because timer start event is
    // interrupting
    assertEquals(6, executionQuery.count());

    // check if process instance doesn't exist because timer start event is
    // interrupting
    ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId());
    assertEquals(1, processInstanceQuery.count());

  }

  /**
   * test scenario: - start process instance with multiInstance sequential -
   * execute interrupting timer job of event subprocess - execute non
   * interrupting timer boundary event of subprocess
   */
  @Deployment
  @Test
  public void testStartTimerEventSubProcessInMultiInstanceSubProcessWithNonInterruptingBoundaryTimerEvent() {
    DummyServiceTask.wasExecuted = false;

    // start process instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    // check if user task exists
    TaskQuery taskQuery = taskService.createTaskQuery();
    assertEquals(1, taskQuery.count());

    JobQuery jobQuery = managementService.createJobQuery();
    // 1 start timer job and 1 boundary timer job
    assertEquals(2, jobQuery.count());
    // execute interrupting start timer event subprocess job
    managementService.executeJob(jobQuery.orderByJobDuedate().asc().list().get(1).getId());

    assertEquals(true, DummyServiceTask.wasExecuted);

    // after first interrupting start timer event sub process execution
    // multiInstance loop number 2
    assertEquals(1, taskQuery.count());
    assertEquals(2, jobQuery.count());

    // execute non interrupting boundary timer job
    managementService.executeJob(jobQuery.orderByJobDuedate().asc().list().get(0).getId());

    // after non interrupting boundary timer job execution
    assertEquals(1, jobQuery.count());
    assertEquals(1, taskQuery.count());
    ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId());
    assertEquals(1, processInstanceQuery.count());

  }

  /**
   * test scenario: - start process instance with multiInstance sequential -
   * execute interrupting timer job of event subprocess - execute interrupting
   * timer boundary event of subprocess
   */
  @Deployment
  @Test
  public void testStartTimerEventSubProcessInMultiInstanceSubProcessWithInterruptingBoundaryTimerEvent() {
    DummyServiceTask.wasExecuted = false;

    // start process instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    // execute multiInstance loop number 1
    // check if execution exists

    // check if user task exists
    TaskQuery taskQuery = taskService.createTaskQuery();
    assertEquals(1, taskQuery.count());

    JobQuery jobQuery = managementService.createJobQuery();
    // 1 start timer job and 1 boundary timer job
    assertEquals(2, jobQuery.count());
    // execute interrupting start timer event subprocess job
    managementService.executeJob(jobQuery.orderByJobDuedate().asc().list().get(1).getId());

    assertEquals(true, DummyServiceTask.wasExecuted);

    // after first interrupting start timer event sub process execution
    // multiInstance loop number 2
    assertEquals(1, taskQuery.count());
    assertEquals(2, jobQuery.count());

    // execute interrupting boundary timer job
    managementService.executeJob(jobQuery.orderByJobDuedate().asc().list().get(0).getId());

    // after interrupting boundary timer job execution
    assertEquals(0, jobQuery.count());
    assertEquals(0, taskQuery.count());

    testRule.assertProcessEnded(processInstance.getId());

  }

  @Deployment
  @Test
  public void testNonInterruptingStartTimerEventSubProcessInMultiInstanceSubProcessWithInterruptingBoundaryTimerEvent() {
    DummyServiceTask.wasExecuted = false;

    // start process instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    // execute multiInstance loop number 1
    // check if execution exists
    ExecutionQuery executionQuery = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId());
    assertEquals(3, executionQuery.count());

    // check if user task exists
    TaskQuery taskQuery = taskService.createTaskQuery();
    assertEquals(1, taskQuery.count());

    JobQuery jobQuery = managementService.createJobQuery();
    // 1 start timer job and 1 boundary timer job
    assertEquals(2, jobQuery.count());
    // execute non interrupting start timer event subprocess job
    managementService.executeJob(jobQuery.orderByJobDuedate().asc().list().get(1).getId());

    assertEquals(true, DummyServiceTask.wasExecuted);

    // complete user task to finish execution of first multiInstance loop
    assertEquals(1, taskQuery.count());
    taskService.complete(taskQuery.list().get(0).getId());

    // after first non interrupting start timer event sub process execution
    // multiInstance loop number 2
    assertEquals(1, taskQuery.count());
    assertEquals(2, jobQuery.count());

    // execute interrupting boundary timer job
    managementService.executeJob(jobQuery.orderByJobDuedate().asc().list().get(0).getId());

    // after interrupting boundary timer job execution
    assertEquals(0, jobQuery.count());
    assertEquals(0, taskQuery.count());
    assertEquals(0, executionQuery.count());
    ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId());
    assertEquals(0, processInstanceQuery.count());

  }

  /**
   * test scenario: - start process instance with multiInstance parallel -
   * execute interrupting timer job of event subprocess - execute non
   * interrupting timer boundary event of subprocess
   */
  @Deployment
  @Test
  public void testStartTimerEventSubProcessInParallelMultiInstanceSubProcessWithNonInterruptingBoundaryTimerEvent() {
    DummyServiceTask.wasExecuted = false;

    // start process instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    // execute multiInstance loop number 1
    // check if execution exists
    ExecutionQuery executionQuery = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId());
    assertEquals(6, executionQuery.count());

    // check if user task exists
    TaskQuery taskQuery = taskService.createTaskQuery();
    assertEquals(2, taskQuery.count());

    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(3, jobQuery.count());

    // execute interrupting timer job
    managementService.executeJob(jobQuery.orderByJobDuedate().asc().list().get(1).getId());

    assertEquals(true, DummyServiceTask.wasExecuted);

    // after interrupting timer job execution
    assertEquals(2, jobQuery.count());
    assertEquals(1, taskQuery.count());
    assertEquals(5, executionQuery.count());

    // execute non interrupting boundary timer job
    managementService.executeJob(jobQuery.orderByJobDuedate().asc().list().get(0).getId());

    // after non interrupting boundary timer job execution
    assertEquals(1, jobQuery.count());
    assertEquals(1, taskQuery.count());
    assertEquals(5, executionQuery.count());

    ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId());
    assertEquals(1, processInstanceQuery.count());

  }

  /**
   * test scenario: - start process instance with multiInstance parallel -
   * execute interrupting timer job of event subprocess - execute interrupting
   * timer boundary event of subprocess
   */
  @Deployment
  @Test
  public void testStartTimerEventSubProcessInParallelMultiInstanceSubProcessWithInterruptingBoundaryTimerEvent() {
    // start process instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    // execute multiInstance loop number 1
    // check if execution exists
    ExecutionQuery executionQuery = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId());
    assertEquals(6, executionQuery.count());

    // check if user task exists
    TaskQuery taskQuery = taskService.createTaskQuery();
    assertEquals(2, taskQuery.count());

    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(3, jobQuery.count());

    // execute interrupting timer job
    managementService.executeJob(jobQuery.orderByJobDuedate().asc().list().get(1).getId());

    // after interrupting timer job execution
    assertEquals(2, jobQuery.count());
    assertEquals(1, taskQuery.count());
    assertEquals(5, executionQuery.count());

    // execute interrupting boundary timer job
    managementService.executeJob(jobQuery.orderByJobDuedate().asc().list().get(0).getId());

    // after interrupting boundary timer job execution
    assertEquals(0, jobQuery.count());
    assertEquals(0, taskQuery.count());
    assertEquals(0, executionQuery.count());

    testRule.assertProcessEnded(processInstance.getId());

  }

  /**
   * test scenario: - start process instance with multiInstance parallel -
   * execute non interrupting timer job of event subprocess - execute
   * interrupting timer boundary event of subprocess
   */
  @Deployment
  @Test
  public void testNonInterruptingStartTimerEventSubProcessInParallelMiSubProcessWithInterruptingBoundaryTimerEvent() {
    DummyServiceTask.wasExecuted = false;

    // start process instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    // execute multiInstance loop number 1
    // check if execution exists
    ExecutionQuery executionQuery = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId());
    assertEquals(6, executionQuery.count());

    // check if user task exists
    TaskQuery taskQuery = taskService.createTaskQuery();
    assertEquals(2, taskQuery.count());

    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(3, jobQuery.count());

    // execute non interrupting timer job
    managementService.executeJob(jobQuery.orderByJobDuedate().asc().list().get(1).getId());

    assertEquals(true, DummyServiceTask.wasExecuted);

    // after non interrupting timer job execution
    assertEquals(2, jobQuery.count());
    assertEquals(2, taskQuery.count());
    assertEquals(6, executionQuery.count());

    // execute interrupting boundary timer job
    managementService.executeJob(jobQuery.orderByJobDuedate().asc().list().get(0).getId());

    // after interrupting boundary timer job execution
    assertEquals(0, jobQuery.count());
    assertEquals(0, taskQuery.count());
    assertEquals(0, executionQuery.count());

    testRule.assertProcessEnded(processInstance.getId());

    // start process instance again and
    // test if boundary events deleted after all tasks are completed
    processInstance = runtimeService.startProcessInstanceByKey("process");
    jobQuery = managementService.createJobQuery();
    assertEquals(3, jobQuery.count());

    assertEquals(2, taskQuery.count());
    // complete all existing tasks
    for (Task task : taskQuery.list()) {
      taskService.complete(task.getId());
    }

    assertEquals(0, jobQuery.count());
    assertEquals(0, taskQuery.count());
    assertEquals(0, executionQuery.count());

    testRule.assertProcessEnded(processInstance.getId());

  }

  @Deployment
  @Test
  public void testTimeCycle() {
    // given
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
  
  @Test
  public void testRecalculateTimeCycleExpressionCurrentDateBased() throws Exception {
    // given
    Mocks.register("cycle", "R/PT15M");

    ProcessBuilder processBuilder = Bpmn.createExecutableProcess("process");

    BpmnModelInstance modelInstance = processBuilder
      .startEvent().timerWithCycle("${cycle}")
        .userTask("aTaskName")
      .endEvent()
      .done();

    testRule.deploy(repositoryService.createDeployment()
      .addModelInstance("process.bpmn", modelInstance));
    
    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(1, jobQuery.count());

    Job job = jobQuery.singleResult();
    String jobId = job.getId();
    Date oldDuedate = job.getDuedate();

    // when
    moveByMinutes(1);
    managementService.recalculateJobDuedate(jobId, false);

    // then
    Job jobUpdated = jobQuery.singleResult();
    assertEquals(jobId, jobUpdated.getId());
    assertNotEquals(oldDuedate, jobUpdated.getDuedate());
    assertTrue(oldDuedate.before(jobUpdated.getDuedate()));
    
    // when
    Mocks.register("cycle", "R/PT10M");
    managementService.recalculateJobDuedate(jobId, false);

    // then
    jobUpdated = jobQuery.singleResult();
    assertEquals(jobId, jobUpdated.getId());
    assertNotEquals(oldDuedate, jobUpdated.getDuedate());
    assertTrue(oldDuedate.after(jobUpdated.getDuedate()));
    
    Mocks.reset();
  }
  
  @Test
  public void testRecalculateTimeCycleExpressionCreationDateBased() throws Exception {
    // given
    Mocks.register("cycle", "R/PT15M");

    ProcessBuilder processBuilder = Bpmn.createExecutableProcess("process");

    BpmnModelInstance modelInstance = processBuilder
      .startEvent().timerWithCycle("${cycle}")
        .userTask("aTaskName")
      .endEvent()
      .done();

    testRule.deploy(repositoryService.createDeployment()
      .addModelInstance("process.bpmn", modelInstance));
    
    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(1, jobQuery.count());

    Job job = jobQuery.singleResult();
    String jobId = job.getId();
    Date oldDuedate = job.getDuedate();

    // when
    moveByMinutes(1);
    managementService.recalculateJobDuedate(jobId, true);

    // then
    Job jobUpdated = jobQuery.singleResult();
    assertEquals(jobId, jobUpdated.getId());
    Date expectedDate = LocalDateTime.fromDateFields(jobUpdated.getCreateTime()).plusMinutes(15).toDate();
    assertEquals(expectedDate, jobUpdated.getDuedate());
    
    // when
    Mocks.register("cycle", "R/PT10M");
    managementService.recalculateJobDuedate(jobId, true);

    // then
    jobUpdated = jobQuery.singleResult();
    assertEquals(jobId, jobUpdated.getId());
    assertNotEquals(oldDuedate, jobUpdated.getDuedate());
    assertTrue(oldDuedate.after(jobUpdated.getDuedate()));
    expectedDate = LocalDateTime.fromDateFields(jobUpdated.getCreateTime()).plusMinutes(10).toDate();
    assertEquals(expectedDate, jobUpdated.getDuedate());
    
    Mocks.reset();
  }
  
  @Deployment
  @Test
  public void testFailingTimeCycle() throws Exception {
    // given
    JobQuery query = managementService.createJobQuery();
    JobQuery failedJobQuery = managementService.createJobQuery();

    // a job to start a process instance
    assertEquals(1, query.count());

    String jobId = query.singleResult().getId();
    failedJobQuery.jobId(jobId);

    moveByMinutes(5);

    // when (1)
    try {
      managementService.executeJob(jobId);
    } catch (Exception e) {
      // expected
    }

    // then (1)
    Job failedJob = failedJobQuery.singleResult();
    assertEquals(2, failedJob.getRetries());

    // a new timer job has been created
    assertEquals(2, query.count());

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
    assertEquals(2, query.count());

    assertEquals(1, managementService.createJobQuery().withException().count());
    assertEquals(0, managementService.createJobQuery().noRetriesLeft().count());
    assertEquals(2, managementService.createJobQuery().withRetriesLeft().count());
  }

  @Deployment
  @Test
  public void testNonInterruptingTimeCycleInEventSubProcess() {
    // given
    runtimeService.startProcessInstanceByKey("process");

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

  @Test
  public void testInterruptingWithDurationExpression() {
    // given
    Mocks.register("duration", "PT60S");

    ProcessBuilder processBuilder = Bpmn.createExecutableProcess("process");

    BpmnModelInstance modelInstance = processBuilder
      .startEvent().timerWithDuration("${duration}")
        .userTask("aTaskName")
      .endEvent()
      .done();

    testRule.deploy(repositoryService.createDeployment()
      .addModelInstance("process.bpmn", modelInstance));

    // when
    String jobId = managementService.createJobQuery()
      .singleResult()
      .getId();

    managementService.executeJob(jobId);

    // then
    assertEquals(1, taskService.createTaskQuery().taskName("aTaskName").list().size());

    // cleanup
    Mocks.reset();
  }

  @Test
  public void testInterruptingWithDurationExpressionInEventSubprocess() {
    // given
    ProcessBuilder processBuilder = Bpmn.createExecutableProcess("process");

    BpmnModelInstance modelInstance = processBuilder
      .startEvent()
        .userTask()
      .endEvent()
      .done();

    processBuilder.eventSubProcess()
      .startEvent().timerWithDuration("${duration}")
        .userTask("taskInSubprocess")
      .endEvent();

    testRule.deploy(repositoryService.createDeployment()
      .addModelInstance("process.bpmn", modelInstance));

    // when
    runtimeService.startProcessInstanceByKey("process",
      Variables.createVariables()
        .putValue("duration", "PT60S"));

    String jobId = managementService.createJobQuery()
      .singleResult()
      .getId();

    managementService.executeJob(jobId);

    // then
    assertEquals(1, taskService.createTaskQuery().taskName("taskInSubprocess").list().size());
  }

  @Test
  public void testNonInterruptingWithDurationExpressionInEventSubprocess() {
    // given
    ProcessBuilder processBuilder = Bpmn.createExecutableProcess("process");

    BpmnModelInstance modelInstance = processBuilder
      .startEvent()
        .userTask()
      .endEvent().done();

    processBuilder.eventSubProcess()
      .startEvent().interrupting(false).timerWithDuration("${duration}")
        .userTask("taskInSubprocess")
      .endEvent();

    testRule.deploy(repositoryService.createDeployment()
      .addModelInstance("process.bpmn", modelInstance));

    // when
    runtimeService.startProcessInstanceByKey("process",
      Variables.createVariables()
        .putValue("duration", "PT60S"));

    String jobId = managementService.createJobQuery()
      .singleResult()
      .getId();

    managementService.executeJob(jobId);

    // then
    assertEquals(1, taskService.createTaskQuery().taskName("taskInSubprocess").list().size());
  }
  
  @Test
  public void testRecalculateNonInterruptingWithUnchangedDurationExpressionInEventSubprocessCurrentDateBased() throws Exception {
    // given
    ProcessBuilder processBuilder = Bpmn.createExecutableProcess("process");

    BpmnModelInstance modelInstance = processBuilder
      .startEvent()
        .userTask()
      .endEvent().done();

    processBuilder.eventSubProcess()
      .startEvent().interrupting(false).timerWithDuration("${duration}")
        .userTask("taskInSubprocess")
      .endEvent();

    testRule.deploy(repositoryService.createDeployment()
      .addModelInstance("process.bpmn", modelInstance));

    runtimeService.startProcessInstanceByKey("process", 
        Variables.createVariables().putValue("duration", "PT70S"));
    
    JobQuery jobQuery = managementService.createJobQuery();
    Job job = jobQuery.singleResult();
    String jobId = job.getId();
    Date oldDueDate = job.getDuedate();
    
    // when
    moveByMinutes(2);
    Date currentTime = ClockUtil.getCurrentTime();
    managementService.recalculateJobDuedate(jobId, false);

    // then
    assertEquals(1L, jobQuery.count());
    Date newDuedate = jobQuery.singleResult().getDuedate();
    assertNotEquals(oldDueDate, newDuedate);
    assertTrue(oldDueDate.before(newDuedate));
    Date expectedDate = LocalDateTime.fromDateFields(currentTime).plusSeconds(70).toDate();
    assertThat(newDuedate).isCloseTo(expectedDate, 1000l);
    
    managementService.executeJob(jobId);
    assertEquals(1, taskService.createTaskQuery().taskName("taskInSubprocess").list().size());
  }
  
  @Test
  public void testRecalculateNonInterruptingWithChangedDurationExpressionInEventSubprocessCreationDateBased() throws Exception {
    // given
    ProcessBuilder processBuilder = Bpmn.createExecutableProcess("process");

    BpmnModelInstance modelInstance = processBuilder
      .startEvent()
        .userTask()
      .endEvent().done();

    processBuilder.eventSubProcess()
      .startEvent().interrupting(false).timerWithDuration("${duration}")
        .userTask("taskInSubprocess")
      .endEvent();

    testRule.deploy(repositoryService.createDeployment()
      .addModelInstance("process.bpmn", modelInstance));

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process", 
        Variables.createVariables().putValue("duration", "PT60S"));
    
    JobQuery jobQuery = managementService.createJobQuery();
    Job job = jobQuery.singleResult();
    String jobId = job.getId();
    Date oldDueDate = job.getDuedate();
    
    // when
    runtimeService.setVariable(pi.getId(), "duration", "PT2M");
    managementService.recalculateJobDuedate(jobId, true);

    // then
    assertEquals(1L, jobQuery.count());
    Date newDuedate = jobQuery.singleResult().getDuedate();
    Date expectedDate = LocalDateTime.fromDateFields(jobQuery.singleResult().getCreateTime()).plusMinutes(2).toDate();
    assertTrue(oldDueDate.before(newDuedate));
    assertTrue(expectedDate.equals(newDuedate));
    
    managementService.executeJob(jobId);
    assertEquals(1, taskService.createTaskQuery().taskName("taskInSubprocess").list().size());
  }

  @Deployment
  @Test
  public void testNonInterruptingFailingTimeCycleInEventSubProcess() {
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

  // util methods ////////////////////////////////////////

  /**
   * executes all jobs in this threads until they are either done or retries are
   * exhausted.
   */
  protected void executeAllJobs() {
    String nextJobId = getNextExecutableJobId();

    while (nextJobId != null) {
      try {
        managementService.executeJob(nextJobId);
      } catch (Throwable t) { /* ignore */
      }
      nextJobId = getNextExecutableJobId();
    }

  }

  protected String getNextExecutableJobId() {
    List<Job> jobs = managementService.createJobQuery().executable().listPage(0, 1);
    if (jobs.size() == 1) {
      return jobs.get(0).getId();
    } else {
      return null;
    }
  }

  private void cleanDB() {
    String jobId = managementService.createJobQuery().singleResult().getId();
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    commandExecutor.execute(new DeleteJobsCmd(jobId, true));
  }

}
