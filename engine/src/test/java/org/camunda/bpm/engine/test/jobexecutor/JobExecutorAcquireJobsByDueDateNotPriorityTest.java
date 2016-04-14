package org.camunda.bpm.engine.test.jobexecutor;

import static org.camunda.bpm.engine.test.util.ClockTestUtil.incrementClock;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Before;
import org.junit.Test;

public class JobExecutorAcquireJobsByDueDateNotPriorityTest extends AbstractJobExecutorAcquireJobsTest {

  @Before
  public void prepareProcessEngineConfiguration() {
    configuration.setJobExecutorAcquireByDueDate(true);
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/jobexecutor/jobPrioProcess.bpmn20.xml")
  public void testJobPriorityIsNotConsidered() {
    // prio 5
    String instance1 = startProcess("jobPrioProcess", "task2");

    // prio 10
    incrementClock(1);
    String instance2 = startProcess("jobPrioProcess", "task1");

    // prio 5
    incrementClock(1);
    String instance3 = startProcess("jobPrioProcess", "task2");

    // prio 10
    incrementClock(1);
    String instance4 = startProcess("jobPrioProcess", "task1");

    List<JobEntity> acquirableJobs = findAcquirableJobs();
    assertEquals(4, acquirableJobs.size());

    assertEquals(5, (int) acquirableJobs.get(0).getPriority());
    assertEquals(instance1, acquirableJobs.get(0).getProcessInstanceId());
    assertEquals(10, (int) acquirableJobs.get(1).getPriority());
    assertEquals(instance2, acquirableJobs.get(1).getProcessInstanceId());
    assertEquals(5, (int) acquirableJobs.get(2).getPriority());
    assertEquals(instance3, acquirableJobs.get(2).getProcessInstanceId());
    assertEquals(10, (int) acquirableJobs.get(3).getPriority());
    assertEquals(instance4, acquirableJobs.get(3).getProcessInstanceId());
  }

}
