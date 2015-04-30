package org.camunda.bpm.engine.test.jobexecutor;

import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.Deployment;

public class JobExecutorAcquireJobsDefaultTest extends AbstractJobExecutorAcquireJobsTest {

  public void testProcessEngineConfiguration() {
    ProcessEngineConfigurationImpl configuration = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration();
    assertFalse(configuration.isJobExecutorPreferTimerJobs());
    assertFalse(configuration.isJobExecutorAcquireByDueDate());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/jobexecutor/simpleAsyncProcess.bpmn20.xml")
  public void testMessageJobHasNoDueDateSet() {
    runtimeService.startProcessInstanceByKey("simpleAsyncProcess");

    Job job = managementService.createJobQuery().singleResult();
    assertNull(job.getDuedate());
  }

}
