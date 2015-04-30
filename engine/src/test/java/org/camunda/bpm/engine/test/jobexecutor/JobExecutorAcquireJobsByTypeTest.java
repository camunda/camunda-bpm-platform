package org.camunda.bpm.engine.test.jobexecutor;

import java.util.List;

import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.Deployment;

public class JobExecutorAcquireJobsByTypeTest extends AbstractJobExecutorAcquireJobsTest {

  protected boolean isJobExecutorPreferTimerJobs() {
    return true;
  }

  protected boolean isJobExecutorPreferOldJobs() {
    return false;
  }

  public void testProcessEngineConfiguration() {
    ProcessEngineConfigurationImpl configuration = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration();
    assertTrue(configuration.isJobExecutorPreferTimerJobs());
    assertFalse(configuration.isJobExecutorAcquireByDueDate());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/jobexecutor/simpleAsyncProcess.bpmn20.xml")
  public void testMessageJobHasNoDueDateSet() {
    runtimeService.startProcessInstanceByKey("simpleAsyncProcess");

    Job job = managementService.createJobQuery().singleResult();
    assertNull(job.getDuedate());
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/jobexecutor/simpleAsyncProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/jobexecutor/processWithTimerCatch.bpmn20.xml"
  })
  public void testTimerJobsArePreferred() {
    // first start process with timer job
    runtimeService.startProcessInstanceByKey("testProcess");
    // then start process with async task
    runtimeService.startProcessInstanceByKey("simpleAsyncProcess");
    // then start process with timer job
    runtimeService.startProcessInstanceByKey("testProcess");
    // and another process with async task
    runtimeService.startProcessInstanceByKey("simpleAsyncProcess");

    // increment clock so that timer events are acquirable
    incrementClock(70);

    List<JobEntity> acquirableJobs = findAcquirableJobs();
    assertEquals(4, acquirableJobs.size());
    assertTrue(acquirableJobs.get(0) instanceof TimerEntity);
    assertTrue(acquirableJobs.get(1) instanceof TimerEntity);
    assertTrue(acquirableJobs.get(2) instanceof MessageEntity);
    assertTrue(acquirableJobs.get(3) instanceof MessageEntity);
  }

}
