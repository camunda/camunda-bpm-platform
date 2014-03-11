package org.camunda.bpm.engine.test.cmd;

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;

public class FoxJobRetryCmdTest extends PluggableProcessEngineTestCase {

  private void waitForExecutedJobWithRetriesLeft(final int retriesLeft) {

    Job job = managementService.createJobQuery().singleResult();

    try {
      managementService.executeJob(job.getId());
    } catch (Exception e) {}

    // update job
    job = managementService.createJobQuery().singleResult();

    if (job.getRetries() != retriesLeft) {
      waitForExecutedJobWithRetriesLeft(retriesLeft);
    }
  }

  private ExecutionEntity refreshExecutionEntity(String executionId) {
    return (ExecutionEntity) runtimeService.createExecutionQuery().executionId(executionId).singleResult();
  }

  private ExecutionEntity fetchExecutionEntity(String processInstanceId) {
    return (ExecutionEntity) runtimeService.createExecutionQuery().processInstanceId(processInstanceId).singleResult();
  }

  private Job refreshJob(String jobId) {
    return managementService.createJobQuery().jobId(jobId).singleResult();
  }

  private Job fetchJob(String processInstanceId) {
    return managementService.createJobQuery().processInstanceId(processInstanceId).singleResult();
  }

  private void stillOneJobWithExceptionAndRetriesLeft() {
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

    waitForExecutedJobWithRetriesLeft(4);
    stillOneJobWithExceptionAndRetriesLeft();

    job = refreshJob(job.getId());
    assertNotNull(job);

    assertEquals(4, job.getRetries());

    waitForExecutedJobWithRetriesLeft(3);

    job = refreshJob(job.getId());
    assertEquals(3, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft();

    waitForExecutedJobWithRetriesLeft(2);

    job = refreshJob(job.getId());
    assertEquals(2, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft();

    waitForExecutedJobWithRetriesLeft(1);

    job = refreshJob(job.getId());
    assertEquals(1, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft();

    waitForExecutedJobWithRetriesLeft(0);

    job = refreshJob(job.getId());
    assertEquals(0, job.getRetries());
    assertEquals(1, managementService.createJobQuery().withException().count());
    assertEquals(0, managementService.createJobQuery().withRetriesLeft().count());
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

    waitForExecutedJobWithRetriesLeft(0);
    job = refreshJob(job.getId());
    assertEquals(0, job.getRetries());
    assertEquals(1, managementService.createJobQuery().noRetriesLeft().count());
  }

}
