package org.camunda.bpm.engine.test.cmd;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;

public class FoxJobRetryCmdTest extends PluggableProcessEngineTestCase {

  protected void waitForExecutedJobWithRetriesLeft(int retriesLeft, String jobId) {
    JobQuery jobQuery = managementService.createJobQuery();

    if (jobId != null) {
      jobQuery.jobId(jobId);
    }

    Job job = jobQuery.singleResult();

    try {
      managementService.executeJob(job.getId());
    } catch (Exception e) {}

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

  protected ExecutionEntity fetchExecutionEntity(String processInstanceId) {
    return (ExecutionEntity) runtimeService.createExecutionQuery().processInstanceId(processInstanceId).singleResult();
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

  @Deployment(resources = { "org/camunda/bpm/engine/test/cmd/FoxJobRetryCmdTest.testFailedServiceTask.bpmn20.xml" })
  public void testFailedServiceTask() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedServiceTask");
    assertNotNull(pi);

    waitForExecutedJobWithRetriesLeft(4);
    stillOneJobWithExceptionAndRetriesLeft();

    Job job = fetchJob(pi.getProcessInstanceId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());

    assertEquals(4, job.getRetries());

    ExecutionEntity execution = fetchExecutionEntity(pi.getProcessInstanceId());
    assertEquals("failingServiceTask", execution.getActivityId());

    waitForExecutedJobWithRetriesLeft(3);

    job = refreshJob(job.getId());
    assertEquals(3, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingServiceTask", execution.getActivityId());

    waitForExecutedJobWithRetriesLeft(2);

    job = refreshJob(job.getId());
    assertEquals(2, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingServiceTask", execution.getActivityId());

    waitForExecutedJobWithRetriesLeft(1);

    job = refreshJob(job.getId());
    assertEquals(1, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingServiceTask", execution.getActivityId());

    waitForExecutedJobWithRetriesLeft(0);

    job = refreshJob(job.getId());
    assertEquals(0, job.getRetries());
    assertEquals(1, managementService.createJobQuery().withException().count());
    assertEquals(0, managementService.createJobQuery().withRetriesLeft().count());
    assertEquals(1, managementService.createJobQuery().noRetriesLeft().count());

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingServiceTask", execution.getActivityId());

  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/cmd/FoxJobRetryCmdTest.testFailedUserTask.bpmn20.xml" })
  public void testFailedUserTask() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedUserTask");
    assertNotNull(pi);

    waitForExecutedJobWithRetriesLeft(4);
    stillOneJobWithExceptionAndRetriesLeft();

    Job job = fetchJob(pi.getProcessInstanceId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());

    assertEquals(4, job.getRetries());

    ExecutionEntity execution = fetchExecutionEntity(pi.getProcessInstanceId());
    assertEquals("failingUserTask", execution.getActivityId());

    waitForExecutedJobWithRetriesLeft(3);

    job = refreshJob(job.getId());
    assertEquals(3, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingUserTask", execution.getActivityId());

    waitForExecutedJobWithRetriesLeft(2);

    job = refreshJob(job.getId());
    assertEquals(2, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingUserTask", execution.getActivityId());

    waitForExecutedJobWithRetriesLeft(1);

    job = refreshJob(job.getId());
    assertEquals(1, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingUserTask", execution.getActivityId());

    waitForExecutedJobWithRetriesLeft(0);

    job = refreshJob(job.getId());
    assertEquals(0, job.getRetries());
    assertEquals(1, managementService.createJobQuery().withException().count());
    assertEquals(0, managementService.createJobQuery().withRetriesLeft().count());
    assertEquals(1, managementService.createJobQuery().noRetriesLeft().count());

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingUserTask", execution.getActivityId());

  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/cmd/FoxJobRetryCmdTest.testFailedBusinessRuleTask.bpmn20.xml" })
  public void testFailedBusinessRuleTask() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedBusinessRuleTask");
    assertNotNull(pi);

    waitForExecutedJobWithRetriesLeft(4);
    stillOneJobWithExceptionAndRetriesLeft();

    ExecutionEntity execution = fetchExecutionEntity(pi.getProcessInstanceId());
    assertEquals("failingBusinessRuleTask", execution.getActivityId());

    Job job = fetchJob(pi.getProcessInstanceId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());

    assertEquals(4, job.getRetries());

    waitForExecutedJobWithRetriesLeft(3);

    job = refreshJob(job.getId());
    assertEquals(3, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingBusinessRuleTask", execution.getActivityId());

    waitForExecutedJobWithRetriesLeft(2);

    job = refreshJob(job.getId());
    assertEquals(2, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingBusinessRuleTask", execution.getActivityId());

    waitForExecutedJobWithRetriesLeft(1);

    job = refreshJob(job.getId());
    assertEquals(1, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingBusinessRuleTask", execution.getActivityId());

    waitForExecutedJobWithRetriesLeft(0);

    job = refreshJob(job.getId());
    assertEquals(0, job.getRetries());
    assertEquals(1, managementService.createJobQuery().withException().count());
    assertEquals(0, managementService.createJobQuery().withRetriesLeft().count());
    assertEquals(1, managementService.createJobQuery().noRetriesLeft().count());

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingBusinessRuleTask", execution.getActivityId());

  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/cmd/FoxJobRetryCmdTest.testFailedCallActivity.bpmn20.xml" })
  public void testFailedCallActivity() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedCallActivity");
    assertNotNull(pi);

    waitForExecutedJobWithRetriesLeft(4);
    stillOneJobWithExceptionAndRetriesLeft();

    ExecutionEntity execution = fetchExecutionEntity(pi.getProcessInstanceId());
    assertEquals("failingCallActivity", execution.getActivityId());

    Job job = fetchJob(pi.getProcessInstanceId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());

    assertEquals(4, job.getRetries());

    waitForExecutedJobWithRetriesLeft(3);

    job = refreshJob(job.getId());
    assertEquals(3, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingCallActivity", execution.getActivityId());

    waitForExecutedJobWithRetriesLeft(2);

    job = refreshJob(job.getId());
    assertEquals(2, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingCallActivity", execution.getActivityId());

    waitForExecutedJobWithRetriesLeft(1);

    job = refreshJob(job.getId());
    assertEquals(1, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingCallActivity", execution.getActivityId());

    waitForExecutedJobWithRetriesLeft(0);

    job = refreshJob(job.getId());
    assertEquals(0, job.getRetries());
    assertEquals(1, managementService.createJobQuery().withException().count());
    assertEquals(0, managementService.createJobQuery().withRetriesLeft().count());
    assertEquals(1, managementService.createJobQuery().noRetriesLeft().count());

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingCallActivity", execution.getActivityId());

  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/cmd/FoxJobRetryCmdTest.testFailedScriptTask.bpmn20.xml" })
  public void testFailedScriptTask() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedScriptTask");
    assertNotNull(pi);

    waitForExecutedJobWithRetriesLeft(4);
    stillOneJobWithExceptionAndRetriesLeft();

    ExecutionEntity execution = fetchExecutionEntity(pi.getProcessInstanceId());
    assertEquals("failingScriptTask", execution.getActivityId());

    Job job = fetchJob(pi.getProcessInstanceId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());

    assertEquals(4, job.getRetries());

    waitForExecutedJobWithRetriesLeft(3);

    job = refreshJob(job.getId());
    assertEquals(3, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingScriptTask", execution.getActivityId());

    waitForExecutedJobWithRetriesLeft(2);

    job = refreshJob(job.getId());
    assertEquals(2, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingScriptTask", execution.getActivityId());

    waitForExecutedJobWithRetriesLeft(1);

    job = refreshJob(job.getId());
    assertEquals(1, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingScriptTask", execution.getActivityId());

    waitForExecutedJobWithRetriesLeft(0);

    job = refreshJob(job.getId());
    assertEquals(0, job.getRetries());
    assertEquals(1, managementService.createJobQuery().withException().count());
    assertEquals(0, managementService.createJobQuery().withRetriesLeft().count());
    assertEquals(1, managementService.createJobQuery().noRetriesLeft().count());

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingScriptTask", execution.getActivityId());

  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/cmd/FoxJobRetryCmdTest.testFailedSendTask.bpmn20.xml" })
  public void testFailedSendTask() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedSendTask");
    assertNotNull(pi);

    waitForExecutedJobWithRetriesLeft(4);
    stillOneJobWithExceptionAndRetriesLeft();

    ExecutionEntity execution = fetchExecutionEntity(pi.getProcessInstanceId());
    assertEquals("failingSendTask", execution.getActivityId());

    Job job = fetchJob(pi.getProcessInstanceId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());

    assertEquals(4, job.getRetries());

    waitForExecutedJobWithRetriesLeft(3);

    job = refreshJob(job.getId());
    assertEquals(3, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingSendTask", execution.getActivityId());

    waitForExecutedJobWithRetriesLeft(2);

    job = refreshJob(job.getId());
    assertEquals(2, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingSendTask", execution.getActivityId());

    waitForExecutedJobWithRetriesLeft(1);

    job = refreshJob(job.getId());
    assertEquals(1, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingSendTask", execution.getActivityId());

    waitForExecutedJobWithRetriesLeft(0);

    job = refreshJob(job.getId());
    assertEquals(0, job.getRetries());
    assertEquals(1, managementService.createJobQuery().withException().count());
    assertEquals(0, managementService.createJobQuery().withRetriesLeft().count());
    assertEquals(1, managementService.createJobQuery().noRetriesLeft().count());

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingSendTask", execution.getActivityId());

  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/cmd/FoxJobRetryCmdTest.testFailedSubProcess.bpmn20.xml" })
  public void testFailedSubProcess() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedSubProcess");
    assertNotNull(pi);

    waitForExecutedJobWithRetriesLeft(4);
    stillOneJobWithExceptionAndRetriesLeft();

    ExecutionEntity execution = fetchExecutionEntity(pi.getProcessInstanceId());
    assertEquals("failingSubProcess", execution.getActivityId());

    Job job = fetchJob(pi.getProcessInstanceId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());

    assertEquals(4, job.getRetries());

    waitForExecutedJobWithRetriesLeft(3);

    job = refreshJob(job.getId());
    assertEquals(3, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingSubProcess", execution.getActivityId());

    waitForExecutedJobWithRetriesLeft(2);

    job = refreshJob(job.getId());
    assertEquals(2, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingSubProcess", execution.getActivityId());

    waitForExecutedJobWithRetriesLeft(1);

    job = refreshJob(job.getId());
    assertEquals(1, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingSubProcess", execution.getActivityId());

    waitForExecutedJobWithRetriesLeft(0);

    job = refreshJob(job.getId());
    assertEquals(0, job.getRetries());
    assertEquals(1, managementService.createJobQuery().withException().count());
    assertEquals(0, managementService.createJobQuery().withRetriesLeft().count());
    assertEquals(1, managementService.createJobQuery().noRetriesLeft().count());

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingSubProcess", execution.getActivityId());

  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/cmd/FoxJobRetryCmdTest.testFailedTask.bpmn20.xml" })
  public void testFailedTask() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedTask");
    assertNotNull(pi);

    waitForExecutedJobWithRetriesLeft(4);
    stillOneJobWithExceptionAndRetriesLeft();

    ExecutionEntity execution = fetchExecutionEntity(pi.getProcessInstanceId());
    assertEquals("failingTask", execution.getActivityId());

    Job job = fetchJob(pi.getProcessInstanceId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());

    assertEquals(4, job.getRetries());

    waitForExecutedJobWithRetriesLeft(3);

    job = refreshJob(job.getId());
    assertEquals(3, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingTask", execution.getActivityId());

    waitForExecutedJobWithRetriesLeft(2);

    job = refreshJob(job.getId());
    assertEquals(2, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingTask", execution.getActivityId());

    waitForExecutedJobWithRetriesLeft(1);

    job = refreshJob(job.getId());
    assertEquals(1, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingTask", execution.getActivityId());

    waitForExecutedJobWithRetriesLeft(0);

    job = refreshJob(job.getId());
    assertEquals(0, job.getRetries());
    assertEquals(1, managementService.createJobQuery().withException().count());
    assertEquals(0, managementService.createJobQuery().withRetriesLeft().count());
    assertEquals(1, managementService.createJobQuery().noRetriesLeft().count());

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingTask", execution.getActivityId());

  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/cmd/FoxJobRetryCmdTest.testFailedTransaction.bpmn20.xml" })
  public void testFailedTransaction() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedTask");
    assertNotNull(pi);

    waitForExecutedJobWithRetriesLeft(4);
    stillOneJobWithExceptionAndRetriesLeft();

    ExecutionEntity execution = fetchExecutionEntity(pi.getProcessInstanceId());
    assertEquals("failingTransaction", execution.getActivityId());

    Job job = fetchJob(pi.getProcessInstanceId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());

    assertEquals(4, job.getRetries());

    waitForExecutedJobWithRetriesLeft(3);

    job = refreshJob(job.getId());
    assertEquals(3, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingTransaction", execution.getActivityId());

    waitForExecutedJobWithRetriesLeft(2);

    job = refreshJob(job.getId());
    assertEquals(2, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingTransaction", execution.getActivityId());

    waitForExecutedJobWithRetriesLeft(1);

    job = refreshJob(job.getId());
    assertEquals(1, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingTransaction", execution.getActivityId());

    waitForExecutedJobWithRetriesLeft(0);

    job = refreshJob(job.getId());
    assertEquals(0, job.getRetries());
    assertEquals(1, managementService.createJobQuery().withException().count());
    assertEquals(0, managementService.createJobQuery().withRetriesLeft().count());
    assertEquals(1, managementService.createJobQuery().noRetriesLeft().count());

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingTransaction", execution.getActivityId());

  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/cmd/FoxJobRetryCmdTest.testFailedReceiveTask.bpmn20.xml" })
  public void testFailedReceiveTask() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedReceiveTask");
    assertNotNull(pi);

    waitForExecutedJobWithRetriesLeft(4);
    stillOneJobWithExceptionAndRetriesLeft();

    ExecutionEntity execution = fetchExecutionEntity(pi.getProcessInstanceId());
    assertEquals("failingReceiveTask", execution.getActivityId());

    Job job = fetchJob(pi.getProcessInstanceId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());

    assertEquals(4, job.getRetries());

    waitForExecutedJobWithRetriesLeft(3);

    job = refreshJob(job.getId());
    assertEquals(3, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingReceiveTask", execution.getActivityId());

    waitForExecutedJobWithRetriesLeft(2);

    job = refreshJob(job.getId());
    assertEquals(2, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingReceiveTask", execution.getActivityId());

    waitForExecutedJobWithRetriesLeft(1);

    job = refreshJob(job.getId());
    assertEquals(1, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingReceiveTask", execution.getActivityId());

    waitForExecutedJobWithRetriesLeft(0);

    job = refreshJob(job.getId());
    assertEquals(0, job.getRetries());
    assertEquals(1, managementService.createJobQuery().withException().count());
    assertEquals(0, managementService.createJobQuery().withRetriesLeft().count());
    assertEquals(1, managementService.createJobQuery().noRetriesLeft().count());

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingReceiveTask", execution.getActivityId());

  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/cmd/FoxJobRetryCmdTest.testFailedStartTimerEvent.bpmn20.xml" })
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

  @Deployment(resources = { "org/camunda/bpm/engine/test/cmd/FoxJobRetryCmdTest.testFailedBoundaryTimerEvent.bpmn20.xml" })
  public void testFailedBoundaryTimerEvent() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedBoundaryTimerEvent");
    assertNotNull(pi);

    waitForExecutedJobWithRetriesLeft(4);
    stillOneJobWithExceptionAndRetriesLeft();

    Job job = fetchJob(pi.getProcessInstanceId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());

    assertEquals(4, job.getRetries());

    waitForExecutedJobWithRetriesLeft(3);
    stillOneJobWithExceptionAndRetriesLeft();

    job = refreshJob(job.getId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());

    assertEquals(3, job.getRetries());

    waitForExecutedJobWithRetriesLeft(2);
    stillOneJobWithExceptionAndRetriesLeft();

    job = refreshJob(job.getId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());

    assertEquals(2, job.getRetries());

    waitForExecutedJobWithRetriesLeft(1);
    stillOneJobWithExceptionAndRetriesLeft();

    job = refreshJob(job.getId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());

    assertEquals(1, job.getRetries());

    waitForExecutedJobWithRetriesLeft(0);

    job = refreshJob(job.getId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());

    assertEquals(0, job.getRetries());
    assertEquals(1, managementService.createJobQuery().noRetriesLeft().count());

  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/cmd/FoxJobRetryCmdTest.testFailedIntermediateCatchingTimerEvent.bpmn20.xml" })
  public void testFailedIntermediateCatchingTimerEvent() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedIntermediateCatchingTimerEvent");
    assertNotNull(pi);

    waitForExecutedJobWithRetriesLeft(4);
    stillOneJobWithExceptionAndRetriesLeft();

    Job job = fetchJob(pi.getProcessInstanceId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());

    assertEquals(4, job.getRetries());

    waitForExecutedJobWithRetriesLeft(3);
    stillOneJobWithExceptionAndRetriesLeft();

    job = refreshJob(job.getId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());

    assertEquals(3, job.getRetries());

    waitForExecutedJobWithRetriesLeft(2);
    stillOneJobWithExceptionAndRetriesLeft();

    job = refreshJob(job.getId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());

    assertEquals(2, job.getRetries());

    waitForExecutedJobWithRetriesLeft(1);
    stillOneJobWithExceptionAndRetriesLeft();

    job = refreshJob(job.getId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());

    assertEquals(1, job.getRetries());

    waitForExecutedJobWithRetriesLeft(0);

    job = refreshJob(job.getId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());

    assertEquals(0, job.getRetries());
    assertEquals(1, managementService.createJobQuery().noRetriesLeft().count());
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/cmd/FoxJobRetryCmdTest.testBrokenFoxJobRetryValue.bpmn20.xml" })
  public void testBrokenFoxJobRetryValue() {
    Job job = managementService.createJobQuery().list().get(0);
    assertNotNull(job);
    assertEquals(3, job.getRetries());

    waitForExecutedJobWithRetriesLeft(0, job.getId());
    job = refreshJob(job.getId());
    assertEquals(0, job.getRetries());
    assertEquals(1, managementService.createJobQuery().noRetriesLeft().count());
  }

  @Deployment
  public void FAILING_testRetryOnTimerStartEventInEventSubProcess() {
    runtimeService.startProcessInstanceByKey("process").getId();

    Job job = managementService
        .createJobQuery()
        .singleResult();

    assertEquals(3, job.getRetries());

    try {
      managementService.executeJob(job.getId());
      fail();
    } catch (Exception e) {
      // expected
    }

    job = managementService
        .createJobQuery()
        .singleResult();

    assertEquals(4, job.getRetries());
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/cmd/FoxJobRetryCmdTest.testFailedServiceTask.bpmn20.xml" })
  public void FAILING_testFailedRetryWithTimeShift() throws ParseException {
    // set date to hour before time shift (2015-10-25T03:00:00 CEST => 2015-10-25T02:00:00 CET)
    Date tenMinutesBeforeTimeShift = createDateFromLocalString("2015-10-25T02:50:00 CEST");
    Date fiveMinutesBeforeTimeShift = createDateFromLocalString("2015-10-25T02:55:00 CEST");
    Date twoMinutesBeforeTimeShift = createDateFromLocalString("2015-10-25T02:58:00 CEST");
    ClockUtil.setCurrentTime(tenMinutesBeforeTimeShift);

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedServiceTask");
    assertNotNull(pi);

    // a job is acquirable
    List<JobEntity> acquirableJobs = findAndLockAcquirableJobs();
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

  protected Date createDateFromLocalString(String dateString) throws ParseException {
    // Format: 2015-10-25T02:50:00 CEST
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss z", Locale.US);
    return dateFormat.parse(dateString);
  }

  protected List<JobEntity> findAndLockAcquirableJobs() {
    return processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<List<JobEntity>>() {

      @Override
      public List<JobEntity> execute(CommandContext commandContext) {
        List<JobEntity> jobs = commandContext
          .getJobManager()
          .findNextJobsToExecute(new Page(0, 100));
        for (JobEntity job : jobs) {
          job.setLockOwner("test");
        }
        return jobs;
      }
    });
  }

}
