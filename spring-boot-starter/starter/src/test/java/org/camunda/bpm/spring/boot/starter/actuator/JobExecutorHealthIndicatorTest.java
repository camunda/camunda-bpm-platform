package org.camunda.bpm.spring.boot.starter.actuator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.spring.boot.starter.actuator.JobExecutorHealthIndicator.Details;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

@RunWith(MockitoJUnitRunner.class)
public class JobExecutorHealthIndicatorTest {

  private static final String LOCK_OWNER = "lockowner";
  private static final int LOCK_TIME_IN_MILLIS = 5;
  private static final int MAX_JOBS_PER_ACQUISITION = 6;
  private static final String JOB_EXECUTOR_NAME = "job executor name";
  private static final int WAIT_TIME_IN_MILLIS = 7;
  private static final List<ProcessEngineImpl> PROCESS_ENGINES = new ArrayList<ProcessEngineImpl>();
  private static final String PROCESS_ENGINE_NAME = "process engine name";

  static {
    ProcessEngineImpl processEngineImpl = mock(ProcessEngineImpl.class);
    when(processEngineImpl.getName()).thenReturn(PROCESS_ENGINE_NAME);
    PROCESS_ENGINES.add(processEngineImpl);
  }

  @Mock
  private JobExecutor jobExecutor;

  @Before
  public void init() {
    when(jobExecutor.getLockOwner()).thenReturn(LOCK_OWNER);
    when(jobExecutor.getLockTimeInMillis()).thenReturn(LOCK_TIME_IN_MILLIS);
    when(jobExecutor.getMaxJobsPerAcquisition()).thenReturn(MAX_JOBS_PER_ACQUISITION);
    when(jobExecutor.getName()).thenReturn(JOB_EXECUTOR_NAME);
    when(jobExecutor.getWaitTimeInMillis()).thenReturn(WAIT_TIME_IN_MILLIS);
    when(jobExecutor.getProcessEngines()).thenReturn(PROCESS_ENGINES);
  }

  @Test(expected = NullPointerException.class)
  public void nullTest() {
    new JobExecutorHealthIndicator(null);
  }

  @Test
  public void upTest() {
    when(jobExecutor.isActive()).thenReturn(true);
    JobExecutorHealthIndicator indicator = new JobExecutorHealthIndicator(jobExecutor);
    Health health = indicator.health();
    assertEquals(Status.UP, health.getStatus());
    assertDetails(health);
  }

  @Test
  public void downTest() {
    when(jobExecutor.isActive()).thenReturn(false);
    JobExecutorHealthIndicator indicator = new JobExecutorHealthIndicator(jobExecutor);
    Health health = indicator.health();
    assertEquals(Status.DOWN, health.getStatus());
    assertDetails(health);
  }

  private void assertDetails(Health health) {
    Details details = (Details) health.getDetails().get("jobExecutor");
    assertEquals(LOCK_OWNER, details.getLockOwner());
    assertEquals(LOCK_TIME_IN_MILLIS, details.getLockTimeInMillis());
    assertEquals(MAX_JOBS_PER_ACQUISITION, details.getMaxJobsPerAcquisition());
    assertEquals(JOB_EXECUTOR_NAME, details.getName());
    assertEquals(WAIT_TIME_IN_MILLIS, details.getWaitTimeInMillis());
    assertEquals(PROCESS_ENGINES.size(), details.getProcessEngineNames().size());
    assertEquals(PROCESS_ENGINE_NAME, details.getProcessEngineNames().iterator().next());
  }
}
