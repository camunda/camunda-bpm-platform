package org.camunda.bpm.engine.test.jobexecutor;

import static org.camunda.bpm.engine.test.util.ClockTestUtil.incrementClock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Before;
import org.junit.Test;

public class JobExecutorAcquireJobsByPriorityAndDueDateTest extends AbstractJobExecutorAcquireJobsTest {

  @Before
  public void prepareProcessEngineConfiguration() {
    configuration.setJobExecutorAcquireByPriority(true);
    configuration.setJobExecutorAcquireByDueDate(true);
  }

  @Test
  public void testProcessEngineConfiguration() {
    assertFalse(configuration.isJobExecutorPreferTimerJobs());
    assertTrue(configuration.isJobExecutorAcquireByDueDate());
    assertTrue(configuration.isJobExecutorAcquireByPriority());
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/jobexecutor/jobPrioProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/jobexecutor/timerJobPrioProcess.bpmn20.xml"
  })
  public void testAcquisitionByPriorityAndDueDate() {
    // job with priority 10
    String instance1 = startProcess("jobPrioProcess", "task1");

    // job with priority 5
    incrementClock(1);
    String instance2 = startProcess("jobPrioProcess", "task2");

    // job with priority 10
    incrementClock(1);
    String instance3 = startProcess("jobPrioProcess", "task1");

    // job with priority 5
    incrementClock(1);
    String instance4 = startProcess("jobPrioProcess", "task2");

    List<JobEntity> acquirableJobs = findAcquirableJobs();
    assertEquals(4, acquirableJobs.size());
    assertEquals(instance1, acquirableJobs.get(0).getProcessInstanceId());
    assertEquals(instance3, acquirableJobs.get(1).getProcessInstanceId());
    assertEquals(instance2, acquirableJobs.get(2).getProcessInstanceId());
    assertEquals(instance4, acquirableJobs.get(3).getProcessInstanceId());
  }

}
