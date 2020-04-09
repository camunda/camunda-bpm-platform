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
package org.camunda.bpm.engine.test.bpmn.async;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.AcquirableJobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.MessageEventDefinition;
import org.junit.Assert;
import org.junit.Test;

public class FoxJobRetryCmdTest extends PluggableProcessEngineTest {

  SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/async/FoxJobRetryCmdTest.testFailedServiceTask.bpmn20.xml" })
  @Test
  public void testFailedServiceTask() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedServiceTask");

    assertJobRetriesForActivity(pi, "failingServiceTask");
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/async/FoxJobRetryCmdTest.testFailedUserTask.bpmn20.xml" })
  @Test
  public void testFailedUserTask() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedUserTask");

    assertJobRetriesForActivity(pi, "failingUserTask");
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/async/FoxJobRetryCmdTest.testFailedBusinessRuleTask.bpmn20.xml" })
  @Test
  public void testFailedBusinessRuleTask() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedBusinessRuleTask");

    assertJobRetriesForActivity(pi, "failingBusinessRuleTask");
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/async/FoxJobRetryCmdTest.testFailedCallActivity.bpmn20.xml" })
  @Test
  public void testFailedCallActivity() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedCallActivity");

    assertJobRetriesForActivity(pi, "failingCallActivity");
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/async/FoxJobRetryCmdTest.testFailedScriptTask.bpmn20.xml" })
  @Test
  public void testFailedScriptTask() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedScriptTask");

    assertJobRetriesForActivity(pi, "failingScriptTask");
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/async/FoxJobRetryCmdTest.testFailedSendTask.bpmn20.xml" })
  @Test
  public void testFailedSendTask() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedSendTask");

    assertJobRetriesForActivity(pi, "failingSendTask");
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/async/FoxJobRetryCmdTest.testFailedSubProcess.bpmn20.xml" })
  @Test
  public void testFailedSubProcess() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedSubProcess");

    assertJobRetriesForActivity(pi, "failingSubProcess");
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/async/FoxJobRetryCmdTest.testFailedTask.bpmn20.xml" })
  @Test
  public void testFailedTask() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedTask");

    assertJobRetriesForActivity(pi, "failingTask");
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/async/FoxJobRetryCmdTest.testFailedTransaction.bpmn20.xml" })
  @Test
  public void testFailedTransaction() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedTask");

    assertJobRetriesForActivity(pi, "failingTransaction");
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/async/FoxJobRetryCmdTest.testFailedReceiveTask.bpmn20.xml" })
  @Test
  public void testFailedReceiveTask() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedReceiveTask");

    assertJobRetriesForActivity(pi, "failingReceiveTask");
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/async/FoxJobRetryCmdTest.testFailedBoundaryTimerEvent.bpmn20.xml" })
  @Test
  public void testFailedBoundaryTimerEvent() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedBoundaryTimerEvent");

    assertJobRetriesForActivity(pi, "userTask");
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/async/FoxJobRetryCmdTest.testFailedIntermediateCatchingTimerEvent.bpmn20.xml" })
  @Test
  public void testFailedIntermediateCatchingTimerEvent() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedIntermediateCatchingTimerEvent");

    assertJobRetriesForActivity(pi, "failingTimerEvent");
  }

  @Deployment
  @Test
  public void testFailingMultiInstanceBody() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failingMultiInstance");

    // multi-instance body of task
    assertJobRetriesForActivity(pi, "task" + BpmnParse.MULTI_INSTANCE_BODY_ID_SUFFIX);
  }

  @Deployment
  @Test
  public void testFailingMultiInstanceInnerActivity() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failingMultiInstance");

    // inner activity of multi-instance body
    assertJobRetriesForActivity(pi, "task");
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/async/FoxJobRetryCmdTest.testBrokenFoxJobRetryValue.bpmn20.xml" })
  @Test
  public void testBrokenFoxJobRetryValue() {
    Job job = managementService.createJobQuery().list().get(0);
    assertNotNull(job);
    assertEquals(3, job.getRetries());

    waitForExecutedJobWithRetriesLeft(0, job.getId());
    job = refreshJob(job.getId());
    assertEquals(0, job.getRetries());
    assertEquals(1, managementService.createJobQuery().noRetriesLeft().count());
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/async/FoxJobRetryCmdTest.testFailedStartTimerEvent.bpmn20.xml" })
  @Test
  public void testFailedTimerStartEvent() {
    // After process start, there should be timer created
    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(1, jobQuery.count());

    Job job = managementService.createJobQuery().list().get(0);
    assertNotNull(job);
    String jobId = job.getId();

    waitForExecutedJobWithRetriesLeft(4, jobId);
    stillOneJobWithExceptionAndRetriesLeft(jobId);

    job = refreshJob(jobId);
    assertNotNull(job);

    assertEquals(4, job.getRetries());

    waitForExecutedJobWithRetriesLeft(3, jobId);

    job = refreshJob(jobId);
    assertEquals(3, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft(jobId);

    waitForExecutedJobWithRetriesLeft(2, jobId);

    job = refreshJob(jobId);
    assertEquals(2, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft(jobId);

    waitForExecutedJobWithRetriesLeft(1, jobId);

    job = refreshJob(jobId);
    assertEquals(1, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft(jobId);

    waitForExecutedJobWithRetriesLeft(0, jobId);

    job = refreshJob(jobId);
    assertEquals(0, job.getRetries());
    assertEquals(1, managementService.createJobQuery().withException().count());
    assertEquals(0, managementService.createJobQuery().jobId(jobId).withRetriesLeft().count());
    assertEquals(1, managementService.createJobQuery().noRetriesLeft().count());
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/async/FoxJobRetryCmdTest.testFailedIntermediateThrowingSignalEvent.bpmn20.xml",
      "org/camunda/bpm/engine/test/bpmn/async/FoxJobRetryCmdTest.failingSignalStart.bpmn20.xml" })
  public void FAILING_testFailedIntermediateThrowingSignalEvent() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedIntermediateThrowingSignalEvent");

    assertJobRetriesForActivity(pi, "failingSignalEvent");
  }

  @Deployment
  @Test
  public void testRetryOnTimerStartEventInEventSubProcess() {
    runtimeService.startProcessInstanceByKey("process").getId();

    Job job = managementService.createJobQuery().singleResult();

    assertEquals(3, job.getRetries());

    try {
      managementService.executeJob(job.getId());
      fail();
    } catch (Exception e) {
      // expected
    }

    job = managementService.createJobQuery().singleResult();

    assertEquals(4, job.getRetries());
  }

  @Test
  public void testRetryOnServiceTaskLikeMessageThrowEvent() {
    // given
    BpmnModelInstance bpmnModelInstance = Bpmn.createExecutableProcess("process")
        .startEvent()
        .intermediateThrowEvent()
          .camundaAsyncBefore()
          .camundaFailedJobRetryTimeCycle("R10/PT5S")
          .messageEventDefinition("messageDefinition")
            .message("message")
          .messageEventDefinitionDone()
        .endEvent()
        .done();

    MessageEventDefinition messageDefinition = bpmnModelInstance.getModelElementById("messageDefinition");
    messageDefinition.setCamundaClass(FailingDelegate.class.getName());

   testRule.deploy(bpmnModelInstance);

    runtimeService.startProcessInstanceByKey("process");

    Job job = managementService.createJobQuery().singleResult();

    // when job fails
    try {
      managementService.executeJob(job.getId());
    } catch (Exception e) {
      // ignore
    }

    // then
    job = managementService.createJobQuery().singleResult();
    Assert.assertEquals(9, job.getRetries());
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/async/FoxJobRetryCmdTest.testFailedServiceTask.bpmn20.xml" })
  public void FAILING_testFailedRetryWithTimeShift() throws ParseException {
    // set date to hour before time shift (2015-10-25T03:00:00 CEST =>
    // 2015-10-25T02:00:00 CET)
    Date tenMinutesBeforeTimeShift = createDateFromLocalString("2015-10-25T02:50:00 CEST");
    Date fiveMinutesBeforeTimeShift = createDateFromLocalString("2015-10-25T02:55:00 CEST");
    Date twoMinutesBeforeTimeShift = createDateFromLocalString("2015-10-25T02:58:00 CEST");
    ClockUtil.setCurrentTime(tenMinutesBeforeTimeShift);

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedServiceTask");
    assertNotNull(pi);

    // a job is acquirable
    List<AcquirableJobEntity> acquirableJobs = findAndLockAcquirableJobs();
    assertEquals(1, acquirableJobs.size());

    // execute job
    waitForExecutedJobWithRetriesLeft(4);

    // the job lock time is after the current time but before the time shift
    JobEntity job = (JobEntity) fetchJob(pi.getProcessInstanceId());
    assertTrue(tenMinutesBeforeTimeShift.before(job.getLockExpirationTime()));
    assertEquals(fiveMinutesBeforeTimeShift, job.getLockExpirationTime());
    assertTrue(twoMinutesBeforeTimeShift.after(job.getLockExpirationTime()));

    // the job is not acquirable
    acquirableJobs = findAndLockAcquirableJobs();
    assertEquals(0, acquirableJobs.size());

    // set clock to two minutes before time shift
    ClockUtil.setCurrentTime(twoMinutesBeforeTimeShift);

    // the job is now acquirable
    acquirableJobs = findAndLockAcquirableJobs();
    assertEquals(1, acquirableJobs.size());

    // execute job
    waitForExecutedJobWithRetriesLeft(3);

    // the job lock time is after the current time
    job = (JobEntity) refreshJob(job.getId());
    assertTrue(twoMinutesBeforeTimeShift.before(job.getLockExpirationTime()));

    // the job is not acquirable
    acquirableJobs = findAndLockAcquirableJobs();
    assertEquals("Job shouldn't be acquirable", 0, acquirableJobs.size());

    ClockUtil.reset();
  }

  @Test
  public void testFailedJobRetryTimeCycleWithExpression() {
    BpmnModelInstance bpmnModelInstance = Bpmn.createExecutableProcess("process")
        .startEvent()
        .serviceTask()
          .camundaClass("foo")
          .camundaAsyncBefore()
          .camundaFailedJobRetryTimeCycle("${var}")
        .endEvent()
        .done();

   testRule.deploy(bpmnModelInstance);

    runtimeService.startProcessInstanceByKey("process", Variables.createVariables().putValue("var", "R10/PT5M"));

    Job job = managementService.createJobQuery().singleResult();

    // when job fails
    try {
      managementService.executeJob(job.getId());
    } catch (Exception e) {
      // ignore
    }

    // then
    job = managementService.createJobQuery().singleResult();
    Assert.assertEquals(9, job.getRetries());
  }

  @Test
  public void testFailedJobRetryTimeCycleWithUndefinedVar() {
    BpmnModelInstance bpmnModelInstance = Bpmn.createExecutableProcess("process")
        .startEvent()
        .serviceTask()
          .camundaClass("foo")
          .camundaAsyncBefore()
          .camundaFailedJobRetryTimeCycle("${var}")
        .endEvent()
        .done();

   testRule.deploy(bpmnModelInstance);

    runtimeService.startProcessInstanceByKey("process");

    Job job = managementService.createJobQuery().singleResult();

    // when job fails
    try {
      managementService.executeJob(job.getId());
    } catch (Exception e) {
      // ignore
    }

    // then
    job = managementService.createJobQuery().singleResult();
    Assert.assertEquals(2, job.getRetries()); // default behaviour
  }

  @Test
  public void testFailedJobRetryTimeCycleWithChangingExpression() throws ParseException {
    BpmnModelInstance bpmnModelInstance = Bpmn.createExecutableProcess("process")
        .startEvent()
        .serviceTask()
          .camundaClass("foo")
          .camundaAsyncBefore()
          .camundaFailedJobRetryTimeCycle("${var}")
        .endEvent()
        .done();

    Date startDate = simpleDateFormat.parse("2017-01-01T09:55:00");
    ClockUtil.setCurrentTime(startDate);

   testRule.deploy(bpmnModelInstance);

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process", Variables.createVariables().putValue("var", "R10/PT5M"));

    startDate = simpleDateFormat.parse("2017-01-01T10:00:00");
    ClockUtil.setCurrentTime(startDate);

    Job job = managementService.createJobQuery().singleResult();

    // when
    try {
      managementService.executeJob(job.getId());
    } catch (Exception e) {
      // ignore
    }

    job = managementService.createJobQuery().singleResult();
    Assert.assertEquals(9, job.getRetries());

    startDate = simpleDateFormat.parse("2017-01-01T10:05:00");
    ClockUtil.setCurrentTime(startDate);

    runtimeService.setVariable(pi.getProcessInstanceId(), "var", "R10/PT10M");

    try {
      managementService.executeJob(job.getId());
    } catch (Exception e) {
      // ignore
    }

    //then
    Date expectedDate = simpleDateFormat.parse("2017-01-01T10:15:00");
    Date duedateTime = (managementService.createJobQuery().singleResult()).getDuedate();
    assertEquals(expectedDate, duedateTime);
  }

  @Test
  public void testRetryOnTimerStartEventWithExpression() {
    BpmnModelInstance bpmnModelInstance = Bpmn.createExecutableProcess("process")
        .startEvent()
          .camundaFailedJobRetryTimeCycle("${var}")
          .timerWithDuration("PT5M")
        .serviceTask()
          .camundaClass("bar")
        .endEvent()
        .done();

   testRule.deploy(bpmnModelInstance);

    Job job = managementService.createJobQuery().singleResult();

    // when job fails
    try {
      managementService.executeJob(job.getId());
    } catch (Exception e) {
      // ignore
    }

    // then
    job = managementService.createJobQuery().singleResult();
    Assert.assertEquals(2, job.getRetries()); // default behaviour
  }

  @Test
  public void testRetryOnAsyncStartEvent() throws Exception {
    BpmnModelInstance bpmnModelInstance = Bpmn.createExecutableProcess("process")
        .startEvent()
          .camundaAsyncBefore()
          .camundaFailedJobRetryTimeCycle("R5/PT5M")
        .serviceTask()
          .camundaClass("bar")
        .endEvent()
        .done();

   testRule.deploy(bpmnModelInstance);

    Date startDate = simpleDateFormat.parse("2018-01-01T10:00:00");
    ClockUtil.setCurrentTime(startDate);

    runtimeService.startProcessInstanceByKey("process");
    Job job = managementService.createJobQuery().singleResult();

    // assume
    Assert.assertEquals(3, job.getRetries());

    // when job fails
    try {
      managementService.executeJob(job.getId());
    } catch (Exception e) {
      // ignore
    }

    // then
    job = managementService.createJobQuery().singleResult();
    Assert.assertEquals(4, job.getRetries());

    Date expectedDate = simpleDateFormat.parse("2018-01-01T10:05:00");
    assertEquals(expectedDate, job.getDuedate());
  }

  @Test
  public void testIntermediateCatchEvent() throws Exception {
    BpmnModelInstance bpmnModelInstance = Bpmn.createExecutableProcess("process")
        .startEvent()
        .intermediateCatchEvent()
          .message("foo")
          .camundaAsyncBefore()
          .camundaFailedJobRetryTimeCycle("R5/PT5M")
          .camundaExecutionListenerClass("start", "foo")
        .endEvent()
        .done();

   testRule.deploy(bpmnModelInstance);

    Date startDate = simpleDateFormat.parse("2018-01-01T10:00:00");
    ClockUtil.setCurrentTime(startDate);

    runtimeService.startProcessInstanceByKey("process");
    Job job = managementService.createJobQuery().singleResult();

    // assume
    Assert.assertEquals(3, job.getRetries());

    // when job fails
    try {
      managementService.executeJob(job.getId());
    } catch (Exception e) {
      // ignore
    }

    // then
    job = managementService.createJobQuery().singleResult();
    Assert.assertEquals(4, job.getRetries());

    Date expectedDate = simpleDateFormat.parse("2018-01-01T10:05:00");
    assertEquals(expectedDate, job.getDuedate());
  }

  @Test
  public void testEndEvent() throws Exception {
    BpmnModelInstance bpmnModelInstance = Bpmn.createExecutableProcess("process")
        .startEvent()
        .endEvent()
          .camundaAsyncBefore()
          .camundaFailedJobRetryTimeCycle("R5/PT5M")
          .camundaExecutionListenerClass("start", "foo")
        .done();

   testRule.deploy(bpmnModelInstance);

    Date startDate = simpleDateFormat.parse("2018-01-01T10:00:00");
    ClockUtil.setCurrentTime(startDate);

    runtimeService.startProcessInstanceByKey("process");
    Job job = managementService.createJobQuery().singleResult();

    // assume
    Assert.assertEquals(3, job.getRetries());

    // when job fails
    try {
      managementService.executeJob(job.getId());
    } catch (Exception e) {
      // ignore
    }

    // then
    job = managementService.createJobQuery().singleResult();
    Assert.assertEquals(4, job.getRetries());

    Date expectedDate = simpleDateFormat.parse("2018-01-01T10:05:00");
    assertEquals(expectedDate, job.getDuedate());
  }

  @Test
  public void testExclusiveGateway() throws Exception {
    BpmnModelInstance bpmnModelInstance = Bpmn.createExecutableProcess("process")
        .startEvent()
        .exclusiveGateway()
          .camundaAsyncBefore()
          .camundaFailedJobRetryTimeCycle("R5/PT5M")
          .camundaExecutionListenerClass("start", "foo")
        .endEvent()
        .done();

   testRule.deploy(bpmnModelInstance);

    Date startDate = simpleDateFormat.parse("2018-01-01T10:00:00");
    ClockUtil.setCurrentTime(startDate);

    runtimeService.startProcessInstanceByKey("process");
    Job job = managementService.createJobQuery().singleResult();

    // assume
    Assert.assertEquals(3, job.getRetries());

    // when job fails
    try {
      managementService.executeJob(job.getId());
    } catch (Exception e) {
      // ignore
    }

    // then
    job = managementService.createJobQuery().singleResult();
    Assert.assertEquals(4, job.getRetries());

    Date expectedDate = simpleDateFormat.parse("2018-01-01T10:05:00");
    assertEquals(expectedDate, job.getDuedate());
  }

  @Test
  public void testInclusiveGateway() throws Exception {
    BpmnModelInstance bpmnModelInstance = Bpmn.createExecutableProcess("process")
        .startEvent()
        .inclusiveGateway()
          .camundaAsyncBefore()
          .camundaFailedJobRetryTimeCycle("R5/PT5M")
          .camundaExecutionListenerClass("start", "foo")
        .endEvent()
        .done();

   testRule.deploy(bpmnModelInstance);

    Date startDate = simpleDateFormat.parse("2018-01-01T10:00:00");
    ClockUtil.setCurrentTime(startDate);

    runtimeService.startProcessInstanceByKey("process");
    Job job = managementService.createJobQuery().singleResult();

    // assume
    Assert.assertEquals(3, job.getRetries());

    // when job fails
    try {
      managementService.executeJob(job.getId());
    } catch (Exception e) {
      // ignore
    }

    // then
    job = managementService.createJobQuery().singleResult();
    Assert.assertEquals(4, job.getRetries());

    Date expectedDate = simpleDateFormat.parse("2018-01-01T10:05:00");
    assertEquals(expectedDate, job.getDuedate());
  }

  @Test
  public void testEventBasedGateway() throws Exception {
    BpmnModelInstance bpmnModelInstance = Bpmn.createExecutableProcess("process")
        .startEvent()
        .eventBasedGateway()
          .camundaAsyncBefore()
          .camundaFailedJobRetryTimeCycle("R5/PT5M")
          .camundaExecutionListenerClass("start", "foo")
        .intermediateCatchEvent()
          .condition("${true}")
        .endEvent()
        .done();

   testRule.deploy(bpmnModelInstance);

    Date startDate = simpleDateFormat.parse("2018-01-01T10:00:00");
    ClockUtil.setCurrentTime(startDate);

    runtimeService.startProcessInstanceByKey("process");
    Job job = managementService.createJobQuery().singleResult();

    // assume
    Assert.assertEquals(3, job.getRetries());

    // when job fails
    try {
      managementService.executeJob(job.getId());
    } catch (Exception e) {
      // ignore
    }

    // then
    job = managementService.createJobQuery().singleResult();
    Assert.assertEquals(4, job.getRetries());

    Date expectedDate = simpleDateFormat.parse("2018-01-01T10:05:00");
    assertEquals(expectedDate, job.getDuedate());
  }

  @Test
  public void testParallelGateway() throws Exception {
    BpmnModelInstance bpmnModelInstance = Bpmn.createExecutableProcess("process")
        .startEvent()
        .parallelGateway()
          .camundaAsyncBefore()
          .camundaFailedJobRetryTimeCycle("R5/PT5M")
          .camundaExecutionListenerClass("start", "foo")
        .endEvent()
        .done();

   testRule.deploy(bpmnModelInstance);

    Date startDate = simpleDateFormat.parse("2018-01-01T10:00:00");
    ClockUtil.setCurrentTime(startDate);

    runtimeService.startProcessInstanceByKey("process");
    Job job = managementService.createJobQuery().singleResult();

    // assume
    Assert.assertEquals(3, job.getRetries());

    // when job fails
    try {
      managementService.executeJob(job.getId());
    } catch (Exception e) {
      // ignore
    }

    // then
    job = managementService.createJobQuery().singleResult();
    Assert.assertEquals(4, job.getRetries());

    Date expectedDate = simpleDateFormat.parse("2018-01-01T10:05:00");
    assertEquals(expectedDate, job.getDuedate());
  }

  @Test
  public void testFailingIntermidiateBoundaryTimerJobWithCustomRetries() throws ParseException {
    try {
      // given
      BpmnModelInstance bpmnModelInstance = Bpmn.createExecutableProcess("process")
        .startEvent()
        .userTask("wait")
          .boundaryEvent("timer")
          .cancelActivity(false)
          .timerWithCycle("R4/PT1M")
          .camundaFailedJobRetryTimeCycle("R2/PT10M")
        .serviceTask("failing")
          .camundaClass("foo")
        .endEvent()
        .done();

     testRule.deploy(bpmnModelInstance);

      Date startDate = simpleDateFormat.parse("2019-01-01T10:00:00");
      ClockUtil.setCurrentTime(startDate);

      runtimeService.startProcessInstanceByKey("process");

      ClockUtil.setCurrentTime(simpleDateFormat.parse("2019-01-01T10:01:01"));

      // when the first timer is triggered
      Job firstJob = managementService.createJobQuery().singleResult();
      try {
        managementService.executeJob(firstJob.getId());
      } catch (Exception e) {
        // ignore
      }

      // then a second job will be created for the second timer
      List<Job> jobs = managementService.createJobQuery().list();
      assertEquals(2, jobs.size());
      for (Job job : jobs) {
        if (job.getRetries() == 1) { // the first job already failed once
          Date expectedDate = simpleDateFormat.parse("2019-01-01T10:11:01");
          assertEquals(expectedDate, job.getDuedate());
          assertNull(((JobEntity) job).getLockExpirationTime());
        } else if (job.getRetries() == 3) { // the second job is not triggered yet
          Date expectedDate = simpleDateFormat.parse("2019-01-01T10:02:00");
          assertEquals(expectedDate, job.getDuedate());
          assertNull(((JobEntity) job).getLockExpirationTime());
        } else {
          fail("Unexpected job");
        }
      }
    } finally {
      ClockUtil.reset();
    }
  }

  @Test
  public void testExecuteSecondJobWhenJobFailedWithCustomJobRetriesInSameProcess() {
    // given
    BpmnModelInstance bpmnModelInstance = Bpmn.createExecutableProcess("process")
      .startEvent()
      .parallelGateway("gwt")
        .serviceTask("failing")
          .camundaClass("foo")
          .camundaAsyncBefore()
          .camundaFailedJobRetryTimeCycle("R2/PT5M")
      .moveToNode("gwt")
        .userTask("beforePassing")
        .serviceTask("passing")
          .camundaExpression("${true}")
          .camundaAsyncBefore()
        .userTask("afterPassing")
      .done();

   testRule.deploy(bpmnModelInstance);

    runtimeService.startProcessInstanceByKey("process");

    Job job = managementService.createJobQuery().singleResult();
    try {
      managementService.executeJob(job.getId());
    } catch (Exception e) {
      // ignore
    }

    Task task = taskService.createTaskQuery().taskDefinitionKey("beforePassing").singleResult();
    taskService.complete(task.getId());

    // when one failed job and one passing are present
    // only the passing should be executed
    testRule.waitForJobExecutorToProcessAllJobs(5000);

    // then the passing service task has been executed
    task = taskService.createTaskQuery().taskDefinitionKey("afterPassing").singleResult();
    assertNotNull(task);
    // and the failing job still have one retry left
    Job failedJob = managementService.createJobQuery().singleResult();
    assertEquals(1, failedJob.getRetries());
    assertNull(((JobEntity) failedJob).getLockExpirationTime());
  }

  protected void assertJobRetriesForActivity(ProcessInstance pi, String activityId) {
    assertNotNull(pi);

    waitForExecutedJobWithRetriesLeft(4);
    stillOneJobWithExceptionAndRetriesLeft();

    Job job = fetchJob(pi.getProcessInstanceId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());

    assertEquals(4, job.getRetries());

    ExecutionEntity execution = fetchExecutionEntity(pi.getProcessInstanceId(), activityId);
    assertNotNull(execution);

    waitForExecutedJobWithRetriesLeft(3);

    job = refreshJob(job.getId());
    assertEquals(3, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertEquals(activityId, execution.getActivityId());

    waitForExecutedJobWithRetriesLeft(2);

    job = refreshJob(job.getId());
    assertEquals(2, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertEquals(activityId, execution.getActivityId());

    waitForExecutedJobWithRetriesLeft(1);

    job = refreshJob(job.getId());
    assertEquals(1, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertEquals(activityId, execution.getActivityId());

    waitForExecutedJobWithRetriesLeft(0);

    job = refreshJob(job.getId());
    assertEquals(0, job.getRetries());
    assertEquals(1, managementService.createJobQuery().withException().count());
    assertEquals(0, managementService.createJobQuery().withRetriesLeft().count());
    assertEquals(1, managementService.createJobQuery().noRetriesLeft().count());

    execution = refreshExecutionEntity(execution.getId());
    assertEquals(activityId, execution.getActivityId());
  }

  protected void waitForExecutedJobWithRetriesLeft(int retriesLeft, String jobId) {
    JobQuery jobQuery = managementService.createJobQuery();

    if (jobId != null) {
      jobQuery.jobId(jobId);
    }

    Job job = jobQuery.singleResult();

    try {
      managementService.executeJob(job.getId());
    } catch (Exception e) {
    }

    // update job
    job = jobQuery.singleResult();

    if (job.getRetries() != retriesLeft) {
      waitForExecutedJobWithRetriesLeft(retriesLeft, jobId);
    }
  }

  protected void waitForExecutedJobWithRetriesLeft(final int retriesLeft) {
    waitForExecutedJobWithRetriesLeft(retriesLeft, null);
  }

  protected ExecutionEntity refreshExecutionEntity(String executionId) {
    return (ExecutionEntity) runtimeService.createExecutionQuery().executionId(executionId).singleResult();
  }

  protected ExecutionEntity fetchExecutionEntity(String processInstanceId, String activityId) {
    return (ExecutionEntity) runtimeService.createExecutionQuery().processInstanceId(processInstanceId).activityId(activityId).singleResult();
  }

  protected Job refreshJob(String jobId) {
    return managementService.createJobQuery().jobId(jobId).singleResult();
  }

  protected Job fetchJob(String processInstanceId) {
    return managementService.createJobQuery().processInstanceId(processInstanceId).singleResult();
  }

  protected void stillOneJobWithExceptionAndRetriesLeft(String jobId) {
    assertEquals(1, managementService.createJobQuery().jobId(jobId).withException().count());
    assertEquals(1, managementService.createJobQuery().jobId(jobId).withRetriesLeft().count());
  }

  protected void stillOneJobWithExceptionAndRetriesLeft() {
    assertEquals(1, managementService.createJobQuery().withException().count());
    assertEquals(1, managementService.createJobQuery().withRetriesLeft().count());
  }

  protected Date createDateFromLocalString(String dateString) throws ParseException {
    // Format: 2015-10-25T02:50:00 CEST
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss z", Locale.US);
    return dateFormat.parse(dateString);
  }

  protected List<AcquirableJobEntity> findAndLockAcquirableJobs() {
    return processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<List<AcquirableJobEntity>>() {

      @Override
      public List<AcquirableJobEntity> execute(CommandContext commandContext) {
        List<AcquirableJobEntity> jobs = commandContext.getJobManager().findNextJobsToExecute(new Page(0, 100));
        for (AcquirableJobEntity job : jobs) {
          job.setLockOwner("test");
        }
        return jobs;
      }
    });
  }

}
