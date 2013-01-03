package com.camunda.fox.engine.test.cmd;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.JobQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.junit.Before;

public class FoxJobRetryCmdTest extends PluggableActivitiTestCase {

  private static final long FIVE_MINUTES = 302 * 1000;
  private Date currentDate;

  @Before
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    this.currentDate = new Date();
  }

  private void waitForJobExecutorHelper(final int retriesLeft) {

    waitForJobExecutorOnCondition(15000L, 200L, new Callable<Boolean>() {

      public Boolean call() throws Exception {
        List<Job> jobs = FoxJobRetryCmdTest.this.managementService.createJobQuery().list();
        return jobs.size() == 1 && jobs.get(0).getRetries() == retriesLeft;
      }

    });
  }
  
  private ExecutionEntity refreshExecutionEntity(String executionId) {
    return (ExecutionEntity) this.runtimeService.createExecutionQuery().executionId(executionId).singleResult();
  }
  
  private ExecutionEntity fetchExecutionEntity(String processInstanceId) {
    return (ExecutionEntity) this.runtimeService.createExecutionQuery().processInstanceId(processInstanceId).singleResult();
  }

  private Job refreshJob(String jobId) {
    return this.managementService.createJobQuery().jobId(jobId).singleResult();
  }

  private Job fetchJob(String processInstanceId) {
    return this.managementService.createJobQuery().processInstanceId(processInstanceId).singleResult();
  }

  private void setCurrentTime() {
    this.currentDate = new Date(this.currentDate.getTime() + FIVE_MINUTES);
    ClockUtil.setCurrentTime(this.currentDate);
  }

  private void stillOneWithExceptionAndRetriesLeft() {
    assertEquals(1, this.managementService.createJobQuery().withException().count());
    assertEquals(1, this.managementService.createJobQuery().withRetriesLeft().count());
  }

  @Deployment(resources = { "com/camunda/fox/engine/test/cmd/FoxJobRetryCmdTest.testFailedServiceTask.bpmn20.xml" })
  public void testFailedServiceTask() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedServiceTask");
    assertNotNull(pi);

    this.waitForJobExecutorHelper(4);
    this.stillOneWithExceptionAndRetriesLeft();

    Job job = this.fetchJob(pi.getProcessInstanceId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());

    assertEquals(4, job.getRetries());
    
    ExecutionEntity execution = this.fetchExecutionEntity(pi.getProcessInstanceId());
    assertEquals("failingServiceTask", execution.getActivityId());

    this.setCurrentTime();
    this.waitForJobExecutorHelper(3);

    job = this.refreshJob(job.getId());
    assertEquals(3, job.getRetries());
    this.stillOneWithExceptionAndRetriesLeft();
    
    execution = this.refreshExecutionEntity(execution.getId());
    assertEquals("failingServiceTask", execution.getActivityId());

    this.setCurrentTime();
    this.waitForJobExecutorHelper(2);

    job = this.refreshJob(job.getId());
    assertEquals(2, job.getRetries());
    this.stillOneWithExceptionAndRetriesLeft();
    
    execution = this.refreshExecutionEntity(execution.getId());
    assertEquals("failingServiceTask", execution.getActivityId());

    this.setCurrentTime();
    this.waitForJobExecutorHelper(1);

    job = this.refreshJob(job.getId());
    assertEquals(1, job.getRetries());
    this.stillOneWithExceptionAndRetriesLeft();

    execution = this.refreshExecutionEntity(execution.getId());
    assertEquals("failingServiceTask", execution.getActivityId());
    
    this.setCurrentTime();
    this.waitForJobExecutorHelper(0);

    job = this.refreshJob(job.getId());
    assertEquals(0, job.getRetries());
    assertEquals(1, this.managementService.createJobQuery().withException().count());
    assertEquals(0, this.managementService.createJobQuery().withRetriesLeft().count());
    
    execution = this.refreshExecutionEntity(execution.getId());
    assertEquals("failingServiceTask", execution.getActivityId());

  }

  @Deployment(resources = { "com/camunda/fox/engine/test/cmd/FoxJobRetryCmdTest.testFailedUserTask.bpmn20.xml" })
  public void testFailedUserTask() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedUserTask");
    assertNotNull(pi);

    this.waitForJobExecutorHelper(4);
    this.stillOneWithExceptionAndRetriesLeft();

    Job job = this.fetchJob(pi.getProcessInstanceId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());

    assertEquals(4, job.getRetries());
    
    ExecutionEntity execution = this.fetchExecutionEntity(pi.getProcessInstanceId());
    assertEquals("failingUserTask", execution.getActivityId());

    this.setCurrentTime();
    this.waitForJobExecutorHelper(3);

    job = this.refreshJob(job.getId());
    assertEquals(3, job.getRetries());
    this.stillOneWithExceptionAndRetriesLeft();

    execution = this.refreshExecutionEntity(execution.getId());
    assertEquals("failingUserTask", execution.getActivityId());
    
    this.setCurrentTime();
    this.waitForJobExecutorHelper(2);

    job = this.refreshJob(job.getId());
    assertEquals(2, job.getRetries());
    this.stillOneWithExceptionAndRetriesLeft();
    
    execution = this.refreshExecutionEntity(execution.getId());
    assertEquals("failingUserTask", execution.getActivityId());

    this.setCurrentTime();
    this.waitForJobExecutorHelper(1);

    job = this.refreshJob(job.getId());
    assertEquals(1, job.getRetries());
    this.stillOneWithExceptionAndRetriesLeft();
    
    execution = this.refreshExecutionEntity(execution.getId());
    assertEquals("failingUserTask", execution.getActivityId());

    this.setCurrentTime();
    this.waitForJobExecutorHelper(0);

    job = this.refreshJob(job.getId());
    assertEquals(0, job.getRetries());
    assertEquals(1, this.managementService.createJobQuery().withException().count());
    assertEquals(0, this.managementService.createJobQuery().withRetriesLeft().count());
    
    execution = this.refreshExecutionEntity(execution.getId());
    assertEquals("failingUserTask", execution.getActivityId());

  }
  
  @Deployment(resources = { "com/camunda/fox/engine/test/cmd/FoxJobRetryCmdTest.testFailedBusinessRuleTask.bpmn20.xml" })
  public void testFailedBusinessRuleTask() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedBusinessRuleTask");
    assertNotNull(pi);

    this.waitForJobExecutorHelper(4);
    this.stillOneWithExceptionAndRetriesLeft();
    
    ExecutionEntity execution = this.fetchExecutionEntity(pi.getProcessInstanceId());
    assertEquals("failingBusinessRuleTask", execution.getActivityId());

    Job job = this.fetchJob(pi.getProcessInstanceId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());

    assertEquals(4, job.getRetries());

    this.setCurrentTime();
    this.waitForJobExecutorHelper(3);

    job = this.refreshJob(job.getId());
    assertEquals(3, job.getRetries());
    this.stillOneWithExceptionAndRetriesLeft();
    
    execution = this.refreshExecutionEntity(execution.getId());
    assertEquals("failingBusinessRuleTask", execution.getActivityId());

    this.setCurrentTime();
    this.waitForJobExecutorHelper(2);

    job = this.refreshJob(job.getId());
    assertEquals(2, job.getRetries());
    this.stillOneWithExceptionAndRetriesLeft();
    
    execution = this.refreshExecutionEntity(execution.getId());
    assertEquals("failingBusinessRuleTask", execution.getActivityId());

    this.setCurrentTime();
    this.waitForJobExecutorHelper(1);

    job = this.refreshJob(job.getId());
    assertEquals(1, job.getRetries());
    this.stillOneWithExceptionAndRetriesLeft();
    
    execution = this.refreshExecutionEntity(execution.getId());
    assertEquals("failingBusinessRuleTask", execution.getActivityId());

    this.setCurrentTime();
    this.waitForJobExecutorHelper(0);

    job = this.refreshJob(job.getId());
    assertEquals(0, job.getRetries());
    assertEquals(1, this.managementService.createJobQuery().withException().count());
    assertEquals(0, this.managementService.createJobQuery().withRetriesLeft().count());

    execution = this.refreshExecutionEntity(execution.getId());
    assertEquals("failingBusinessRuleTask", execution.getActivityId());
    
  }
  
  @Deployment(resources = { "com/camunda/fox/engine/test/cmd/FoxJobRetryCmdTest.testFailedCallActivity.bpmn20.xml" })
  public void testFailedCallActivity() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedCallActivity");
    assertNotNull(pi);

    this.waitForJobExecutorHelper(4);
    this.stillOneWithExceptionAndRetriesLeft();
    
    ExecutionEntity execution = this.fetchExecutionEntity(pi.getProcessInstanceId());
    assertEquals("failingCallActivity", execution.getActivityId());

    Job job = this.fetchJob(pi.getProcessInstanceId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());

    assertEquals(4, job.getRetries());

    this.setCurrentTime();
    this.waitForJobExecutorHelper(3);

    job = this.refreshJob(job.getId());
    assertEquals(3, job.getRetries());
    this.stillOneWithExceptionAndRetriesLeft();

    execution = this.refreshExecutionEntity(execution.getId());
    assertEquals("failingCallActivity", execution.getActivityId());
    
    this.setCurrentTime();
    this.waitForJobExecutorHelper(2);

    job = this.refreshJob(job.getId());
    assertEquals(2, job.getRetries());
    this.stillOneWithExceptionAndRetriesLeft();
    
    execution = this.refreshExecutionEntity(execution.getId());
    assertEquals("failingCallActivity", execution.getActivityId());

    this.setCurrentTime();
    this.waitForJobExecutorHelper(1);

    job = this.refreshJob(job.getId());
    assertEquals(1, job.getRetries());
    this.stillOneWithExceptionAndRetriesLeft();
    
    execution = this.refreshExecutionEntity(execution.getId());
    assertEquals("failingCallActivity", execution.getActivityId());

    this.setCurrentTime();
    this.waitForJobExecutorHelper(0);

    job = this.refreshJob(job.getId());
    assertEquals(0, job.getRetries());
    assertEquals(1, this.managementService.createJobQuery().withException().count());
    assertEquals(0, this.managementService.createJobQuery().withRetriesLeft().count());
    
    execution = this.refreshExecutionEntity(execution.getId());
    assertEquals("failingCallActivity", execution.getActivityId());

  }
  
  @Deployment(resources = { "com/camunda/fox/engine/test/cmd/FoxJobRetryCmdTest.testFailedScriptTask.bpmn20.xml" })
  public void testFailedScriptTask() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedScriptTask");
    assertNotNull(pi);

    this.waitForJobExecutorHelper(4);
    this.stillOneWithExceptionAndRetriesLeft();
    
    ExecutionEntity execution = this.fetchExecutionEntity(pi.getProcessInstanceId());
    assertEquals("failingScriptTask", execution.getActivityId());

    Job job = this.fetchJob(pi.getProcessInstanceId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());

    assertEquals(4, job.getRetries());

    this.setCurrentTime();
    this.waitForJobExecutorHelper(3);

    job = this.refreshJob(job.getId());
    assertEquals(3, job.getRetries());
    this.stillOneWithExceptionAndRetriesLeft();
    
    execution = this.refreshExecutionEntity(execution.getId());
    assertEquals("failingScriptTask", execution.getActivityId());

    this.setCurrentTime();
    this.waitForJobExecutorHelper(2);

    job = this.refreshJob(job.getId());
    assertEquals(2, job.getRetries());
    this.stillOneWithExceptionAndRetriesLeft();

    execution = this.refreshExecutionEntity(execution.getId());
    assertEquals("failingScriptTask", execution.getActivityId());
    
    this.setCurrentTime();
    this.waitForJobExecutorHelper(1);

    job = this.refreshJob(job.getId());
    assertEquals(1, job.getRetries());
    this.stillOneWithExceptionAndRetriesLeft();
    
    execution = this.refreshExecutionEntity(execution.getId());
    assertEquals("failingScriptTask", execution.getActivityId());

    this.setCurrentTime();
    this.waitForJobExecutorHelper(0);

    job = this.refreshJob(job.getId());
    assertEquals(0, job.getRetries());
    assertEquals(1, this.managementService.createJobQuery().withException().count());
    assertEquals(0, this.managementService.createJobQuery().withRetriesLeft().count());
    
    execution = this.refreshExecutionEntity(execution.getId());
    assertEquals("failingScriptTask", execution.getActivityId());

  }

  @Deployment(resources = { "com/camunda/fox/engine/test/cmd/FoxJobRetryCmdTest.testFailedSendTask.bpmn20.xml" })
  public void testFailedSendTask() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedSendTask");
    assertNotNull(pi);
    
    this.waitForJobExecutorHelper(4);
    this.stillOneWithExceptionAndRetriesLeft();
    
    ExecutionEntity execution = this.fetchExecutionEntity(pi.getProcessInstanceId());
    assertEquals("failingSendTask", execution.getActivityId());

    Job job = this.fetchJob(pi.getProcessInstanceId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());

    assertEquals(4, job.getRetries());

    this.setCurrentTime();
    this.waitForJobExecutorHelper(3);

    job = this.refreshJob(job.getId());
    assertEquals(3, job.getRetries());
    this.stillOneWithExceptionAndRetriesLeft();
    
    execution = this.refreshExecutionEntity(execution.getId());
    assertEquals("failingSendTask", execution.getActivityId());

    this.setCurrentTime();
    this.waitForJobExecutorHelper(2);

    job = this.refreshJob(job.getId());
    assertEquals(2, job.getRetries());
    this.stillOneWithExceptionAndRetriesLeft();
    
    execution = this.refreshExecutionEntity(execution.getId());
    assertEquals("failingSendTask", execution.getActivityId());

    this.setCurrentTime();
    this.waitForJobExecutorHelper(1);

    job = this.refreshJob(job.getId());
    assertEquals(1, job.getRetries());
    this.stillOneWithExceptionAndRetriesLeft();

    execution = this.refreshExecutionEntity(execution.getId());
    assertEquals("failingSendTask", execution.getActivityId());
    
    this.setCurrentTime();
    this.waitForJobExecutorHelper(0);

    job = this.refreshJob(job.getId());
    assertEquals(0, job.getRetries());
    assertEquals(1, this.managementService.createJobQuery().withException().count());
    assertEquals(0, this.managementService.createJobQuery().withRetriesLeft().count());
    
    execution = this.refreshExecutionEntity(execution.getId());
    assertEquals("failingSendTask", execution.getActivityId());

  }
  
  @Deployment(resources = { "com/camunda/fox/engine/test/cmd/FoxJobRetryCmdTest.testFailedSubProcess.bpmn20.xml" })
  public void testFailedSubProcess() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedSubProcess");
    assertNotNull(pi);
    
    this.waitForJobExecutorHelper(4);
    this.stillOneWithExceptionAndRetriesLeft();
    
    ExecutionEntity execution = this.fetchExecutionEntity(pi.getProcessInstanceId());
    assertEquals("failingSubProcess", execution.getActivityId());

    Job job = this.fetchJob(pi.getProcessInstanceId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());

    assertEquals(4, job.getRetries());

    this.setCurrentTime();
    this.waitForJobExecutorHelper(3);

    job = this.refreshJob(job.getId());
    assertEquals(3, job.getRetries());
    this.stillOneWithExceptionAndRetriesLeft();
    
    execution = this.refreshExecutionEntity(execution.getId());
    assertEquals("failingSubProcess", execution.getActivityId());

    this.setCurrentTime();
    this.waitForJobExecutorHelper(2);

    job = this.refreshJob(job.getId());
    assertEquals(2, job.getRetries());
    this.stillOneWithExceptionAndRetriesLeft();
    
    execution = this.refreshExecutionEntity(execution.getId());
    assertEquals("failingSubProcess", execution.getActivityId());

    this.setCurrentTime();
    this.waitForJobExecutorHelper(1);

    job = this.refreshJob(job.getId());
    assertEquals(1, job.getRetries());
    this.stillOneWithExceptionAndRetriesLeft();
    
    execution = this.refreshExecutionEntity(execution.getId());
    assertEquals("failingSubProcess", execution.getActivityId());

    this.setCurrentTime();
    this.waitForJobExecutorHelper(0);

    job = this.refreshJob(job.getId());
    assertEquals(0, job.getRetries());
    assertEquals(1, this.managementService.createJobQuery().withException().count());
    assertEquals(0, this.managementService.createJobQuery().withRetriesLeft().count());
    
    execution = this.refreshExecutionEntity(execution.getId());
    assertEquals("failingSubProcess", execution.getActivityId());

  }
  
  @Deployment(resources = { "com/camunda/fox/engine/test/cmd/FoxJobRetryCmdTest.testFailedTask.bpmn20.xml" })
  public void testFailedTask() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedTask");
    assertNotNull(pi);
    
    this.waitForJobExecutorHelper(4);
    this.stillOneWithExceptionAndRetriesLeft();
    
    ExecutionEntity execution = this.fetchExecutionEntity(pi.getProcessInstanceId());
    assertEquals("failingTask", execution.getActivityId());

    Job job = this.fetchJob(pi.getProcessInstanceId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());

    assertEquals(4, job.getRetries());

    this.setCurrentTime();
    this.waitForJobExecutorHelper(3);

    job = this.refreshJob(job.getId());
    assertEquals(3, job.getRetries());
    this.stillOneWithExceptionAndRetriesLeft();
    
    execution = this.refreshExecutionEntity(execution.getId());
    assertEquals("failingTask", execution.getActivityId());

    this.setCurrentTime();
    this.waitForJobExecutorHelper(2);

    job = this.refreshJob(job.getId());
    assertEquals(2, job.getRetries());
    this.stillOneWithExceptionAndRetriesLeft();
    
    execution = this.refreshExecutionEntity(execution.getId());
    assertEquals("failingTask", execution.getActivityId());

    this.setCurrentTime();
    this.waitForJobExecutorHelper(1);

    job = this.refreshJob(job.getId());
    assertEquals(1, job.getRetries());
    this.stillOneWithExceptionAndRetriesLeft();
    
    execution = this.refreshExecutionEntity(execution.getId());
    assertEquals("failingTask", execution.getActivityId());

    this.setCurrentTime();
    this.waitForJobExecutorHelper(0);

    job = this.refreshJob(job.getId());
    assertEquals(0, job.getRetries());
    assertEquals(1, this.managementService.createJobQuery().withException().count());
    assertEquals(0, this.managementService.createJobQuery().withRetriesLeft().count());
    
    execution = this.refreshExecutionEntity(execution.getId());
    assertEquals("failingTask", execution.getActivityId());

  }
  
  @Deployment(resources = { "com/camunda/fox/engine/test/cmd/FoxJobRetryCmdTest.testFailedTransaction.bpmn20.xml" })
  public void testFailedTransaction() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedTask");
    assertNotNull(pi);
    
    this.waitForJobExecutorHelper(4);
    this.stillOneWithExceptionAndRetriesLeft();
    
    ExecutionEntity execution = this.fetchExecutionEntity(pi.getProcessInstanceId());
    assertEquals("failingTransaction", execution.getActivityId());

    Job job = this.fetchJob(pi.getProcessInstanceId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());

    assertEquals(4, job.getRetries());

    this.setCurrentTime();
    this.waitForJobExecutorHelper(3);

    job = this.refreshJob(job.getId());
    assertEquals(3, job.getRetries());
    this.stillOneWithExceptionAndRetriesLeft();
    
    execution = this.refreshExecutionEntity(execution.getId());
    assertEquals("failingTransaction", execution.getActivityId());

    this.setCurrentTime();
    this.waitForJobExecutorHelper(2);

    job = this.refreshJob(job.getId());
    assertEquals(2, job.getRetries());
    this.stillOneWithExceptionAndRetriesLeft();
    
    execution = this.refreshExecutionEntity(execution.getId());
    assertEquals("failingTransaction", execution.getActivityId());

    this.setCurrentTime();
    this.waitForJobExecutorHelper(1);

    job = this.refreshJob(job.getId());
    assertEquals(1, job.getRetries());
    this.stillOneWithExceptionAndRetriesLeft();
    
    execution = this.refreshExecutionEntity(execution.getId());
    assertEquals("failingTransaction", execution.getActivityId());

    this.setCurrentTime();
    this.waitForJobExecutorHelper(0);

    job = this.refreshJob(job.getId());
    assertEquals(0, job.getRetries());
    assertEquals(1, this.managementService.createJobQuery().withException().count());
    assertEquals(0, this.managementService.createJobQuery().withRetriesLeft().count());
    
    execution = this.refreshExecutionEntity(execution.getId());
    assertEquals("failingTransaction", execution.getActivityId());

  }
  
  @Deployment(resources = { "com/camunda/fox/engine/test/cmd/FoxJobRetryCmdTest.testFailedReceiveTask.bpmn20.xml" })
  public void testFailedReceiveTask() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedReceiveTask");
    assertNotNull(pi);
    
    this.waitForJobExecutorHelper(4);
    this.stillOneWithExceptionAndRetriesLeft();
    
    ExecutionEntity execution = this.fetchExecutionEntity(pi.getProcessInstanceId());
    assertEquals("failingReceiveTask", execution.getActivityId());

    Job job = this.fetchJob(pi.getProcessInstanceId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());

    assertEquals(4, job.getRetries());

    this.setCurrentTime();
    this.waitForJobExecutorHelper(3);

    job = this.refreshJob(job.getId());
    assertEquals(3, job.getRetries());
    this.stillOneWithExceptionAndRetriesLeft();
    
    execution = this.refreshExecutionEntity(execution.getId());
    assertEquals("failingReceiveTask", execution.getActivityId());

    this.setCurrentTime();
    this.waitForJobExecutorHelper(2);

    job = this.refreshJob(job.getId());
    assertEquals(2, job.getRetries());
    this.stillOneWithExceptionAndRetriesLeft();

    execution = this.refreshExecutionEntity(execution.getId());
    assertEquals("failingReceiveTask", execution.getActivityId());
    
    this.setCurrentTime();
    this.waitForJobExecutorHelper(1);

    job = this.refreshJob(job.getId());
    assertEquals(1, job.getRetries());
    this.stillOneWithExceptionAndRetriesLeft();

    execution = this.refreshExecutionEntity(execution.getId());
    assertEquals("failingReceiveTask", execution.getActivityId());
    
    this.setCurrentTime();
    this.waitForJobExecutorHelper(0);

    job = this.refreshJob(job.getId());
    assertEquals(0, job.getRetries());
    assertEquals(1, this.managementService.createJobQuery().withException().count());
    assertEquals(0, this.managementService.createJobQuery().withRetriesLeft().count());
    
    execution = this.refreshExecutionEntity(execution.getId());
    assertEquals("failingReceiveTask", execution.getActivityId());

  }
  
//  @Deployment(resources = { "com/camunda/fox/engine/test/cmd/FoxJobRetryCmdTest.testFailedSignalEvent.bpmn20.xml" })
//  public void testFailedSignalEvent() {
//    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedSignalEvent");
//    runtimeService.startProcessInstanceByKey("catchedSignal");
//    assertNotNull(pi);
//    
//    this.waitForJobExecutorHelper(4);
//    this.stillOneWithExceptionAndRetriesLeft();
//    
//    ExecutionEntity execution = this.fetchExecutionEntity(pi.getProcessInstanceId());
//    assertEquals("failingSignalEvent", execution.getActivityId());
//
//    Job job = this.fetchJob(pi.getProcessInstanceId());
//    assertNotNull(job);
//    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());
//
//    assertEquals(4, job.getRetries());
//
//    this.setCurrentTime();
//    this.waitForJobExecutorHelper(3);
//
//    job = this.refreshJob(job.getId());
//    assertEquals(3, job.getRetries());
//    this.stillOneWithExceptionAndRetriesLeft();
//    
//    execution = this.refreshExecutionEntity(execution.getId());
//    assertEquals("failingSignalEvent", execution.getActivityId());
//
//    this.setCurrentTime();
//    this.waitForJobExecutorHelper(2);
//
//    job = this.refreshJob(job.getId());
//    assertEquals(2, job.getRetries());
//    this.stillOneWithExceptionAndRetriesLeft();
//
//    execution = this.refreshExecutionEntity(execution.getId());
//    assertEquals("failingSignalEvent", execution.getActivityId());
//    
//    this.setCurrentTime();
//    this.waitForJobExecutorHelper(1);
//
//    job = this.refreshJob(job.getId());
//    assertEquals(1, job.getRetries());
//    this.stillOneWithExceptionAndRetriesLeft();
//
//    execution = this.refreshExecutionEntity(execution.getId());
//    assertEquals("failingSignalEvent", execution.getActivityId());
//    
//    this.setCurrentTime();
//    this.waitForJobExecutorHelper(0);
//
//    job = this.refreshJob(job.getId());
//    assertEquals(0, job.getRetries());
//    assertEquals(1, this.managementService.createJobQuery().withException().count());
//    assertEquals(0, this.managementService.createJobQuery().withRetriesLeft().count());
//    
//    execution = this.refreshExecutionEntity(execution.getId());
//    assertEquals("failingSignalEvent", execution.getActivityId());
//
//  }
  
  @Deployment(resources = { "com/camunda/fox/engine/test/cmd/FoxJobRetryCmdTest.testFailedStartTimerEvent.bpmn20.xml" })
  public void testFailedTimerStartEvent() {
    // After process start, there should be timer created
    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(1, jobQuery.count());

    // After setting the clock to time '50minutes and 5 seconds', the second timer should fire
    this.currentDate = new Date(this.currentDate.getTime() + ((50 * 60 * 1000) + 5000));
    ClockUtil.setCurrentTime(this.currentDate);
    
    Job job = this.managementService.createJobQuery().list().get(0);
    assertNotNull(job);
    
    this.waitForJobExecutorHelper(4);
    this.stillOneWithExceptionAndRetriesLeft();
    
    job = this.refreshJob(job.getId());
    assertNotNull(job);

    assertEquals(4, job.getRetries());

    this.setCurrentTime();
    this.waitForJobExecutorHelper(3);

    job = this.refreshJob(job.getId());
    assertEquals(3, job.getRetries());
    this.stillOneWithExceptionAndRetriesLeft();
    
    this.setCurrentTime();
    this.waitForJobExecutorHelper(2);

    job = this.refreshJob(job.getId());
    assertEquals(2, job.getRetries());
    this.stillOneWithExceptionAndRetriesLeft();

    this.setCurrentTime();
    this.waitForJobExecutorHelper(1);

    job = this.refreshJob(job.getId());
    assertEquals(1, job.getRetries());
    this.stillOneWithExceptionAndRetriesLeft();

    this.setCurrentTime();
    this.waitForJobExecutorHelper(0);

    job = this.refreshJob(job.getId());
    assertEquals(0, job.getRetries());
    assertEquals(1, this.managementService.createJobQuery().withException().count());
    assertEquals(0, this.managementService.createJobQuery().withRetriesLeft().count());
  }
  
  @Deployment(resources = { "com/camunda/fox/engine/test/cmd/FoxJobRetryCmdTest.testFailedBoundaryTimerEvent.bpmn20.xml" })
  public void testFailedBoundaryTimerEvent() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedBoundaryTimerEvent");
    assertNotNull(pi);
    
    this.currentDate = new Date(this.currentDate.getTime() + ((60 * 60 * 1000) + 5000));
    ClockUtil.setCurrentTime(this.currentDate);
    
    this.waitForJobExecutorHelper(4);
    this.stillOneWithExceptionAndRetriesLeft();
    
    Job job = this.fetchJob(pi.getProcessInstanceId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());
    
    assertEquals(4, job.getRetries());
    
    this.setCurrentTime();
    this.waitForJobExecutorHelper(3);
    this.stillOneWithExceptionAndRetriesLeft();
    
    job = this.refreshJob(job.getId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());
    
    assertEquals(3, job.getRetries());
    
    this.setCurrentTime();
    this.waitForJobExecutorHelper(2);
    this.stillOneWithExceptionAndRetriesLeft();
    
    job = this.refreshJob(job.getId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());
    
    assertEquals(2, job.getRetries());
    
    this.setCurrentTime();
    this.waitForJobExecutorHelper(1);
    this.stillOneWithExceptionAndRetriesLeft();
    
    job = this.refreshJob(job.getId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());
    
    assertEquals(1, job.getRetries());
    
    this.setCurrentTime();
    this.waitForJobExecutorHelper(0);
    
    job = this.refreshJob(job.getId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());
    
    assertEquals(0, job.getRetries());

  }
  
  @Deployment(resources = { "com/camunda/fox/engine/test/cmd/FoxJobRetryCmdTest.testFailedIntermediateCatchingTimerEvent.bpmn20.xml" })
  public void testFailedIntermediateCatchingTimerEvent() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedIntermediateCatchingTimerEvent");
    assertNotNull(pi);
    
    this.currentDate = new Date(this.currentDate.getTime() + ((60 * 60 * 1000) + 5000));
    ClockUtil.setCurrentTime(this.currentDate);
    
    this.waitForJobExecutorHelper(4);
    this.stillOneWithExceptionAndRetriesLeft();
    
    Job job = this.fetchJob(pi.getProcessInstanceId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());
    
    assertEquals(4, job.getRetries());
    
    this.setCurrentTime();
    this.waitForJobExecutorHelper(3);
    this.stillOneWithExceptionAndRetriesLeft();
    
    job = this.refreshJob(job.getId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());
    
    assertEquals(3, job.getRetries());
    
    this.setCurrentTime();
    this.waitForJobExecutorHelper(2);
    this.stillOneWithExceptionAndRetriesLeft();
    
    job = this.refreshJob(job.getId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());
    
    assertEquals(2, job.getRetries());
    
    this.setCurrentTime();
    this.waitForJobExecutorHelper(1);
    this.stillOneWithExceptionAndRetriesLeft();
    
    job = this.refreshJob(job.getId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());
    
    assertEquals(1, job.getRetries());
    
    this.setCurrentTime();
    this.waitForJobExecutorHelper(0);
    
    job = this.refreshJob(job.getId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());
    
    assertEquals(0, job.getRetries());

  }

}
