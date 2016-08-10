package org.camunda.bpm.engine.test.bpmn.async;

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static org.camunda.bpm.engine.test.bpmn.async.RetryCmdDeployment.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Askar Akhmerov
 */
@RunWith(Parameterized.class)
public class FoxJobRetryCmdEventsTest {

  public static final long ONE = 1l;
  public ProcessEngineRule engineRule = new ProcessEngineRule();
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);


  @Parameterized.Parameter
  public RetryCmdDeployment deployment;

  @Parameterized.Parameters(name = "deployment {index}")
  public static Collection<RetryCmdDeployment[]> scenarios() {
    return RetryCmdDeployment.asParameters(
        deployment()
            .withEventProcess(prepareSignalEventProcess(),prepareSignalFailure()),
        deployment()
            .withEventProcess(prepareMessageEventProcess(), prepareMessageFailure()),
        deployment()
            .withEventProcess(prepareEscalationEventProcess()),
        deployment()
            .withEventProcess(prepareCompensationEventProcess())
    );
  }

  private Deployment currentDeployment;

  @Before
  public void setUp () {
    currentDeployment = testRule.deploy(deployment.getBpmnModelInstances());
  }

  @Test
  public void testFailedIntermediateThrowingSignalEventAsync () {
    ProcessInstance pi = engineRule.getRuntimeService().startProcessInstanceByKey(RetryCmdDeployment.PROCESS_ID);
    assertJobRetriesForActivity(pi, FAILING_EVENT);
  }

  @After
  public void tearDown() {
    engineRule.getRepositoryService().deleteDeployment(currentDeployment.getId(),true,true);
  }

  protected void assertJobRetriesForActivity(ProcessInstance pi, String activityId) {
    assertThat(pi,is(notNullValue()));

    waitForExecutedJobWithRetriesLeft(4);
    stillOneJobWithExceptionAndRetriesLeft();

    Job job = fetchJob(pi.getProcessInstanceId());
    assertThat(job,is(notNullValue()));
    assertThat(pi.getProcessInstanceId(), is(job.getProcessInstanceId()));

    assertThat(job.getRetries(),is(4));

    ExecutionEntity execution = fetchExecutionEntity(pi.getProcessInstanceId(), activityId);
    assertThat(execution,is(notNullValue()));

    waitForExecutedJobWithRetriesLeft(3);

    job = refreshJob(job.getId());
    assertThat(job.getRetries(),is(3));
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertThat(execution.getActivityId(),is(activityId));

    waitForExecutedJobWithRetriesLeft(2);

    job = refreshJob(job.getId());
    assertThat(job.getRetries(),is(2));
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertThat(execution.getActivityId(),is(activityId));

    waitForExecutedJobWithRetriesLeft(1);

    job = refreshJob(job.getId());
    assertThat(job.getRetries(),is(1));
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertThat(execution.getActivityId(),is(activityId));

    waitForExecutedJobWithRetriesLeft(0);

    job = refreshJob(job.getId());
    assertThat(job.getRetries(),is(0));
    assertThat(engineRule.getManagementService().createJobQuery().withException().count(),is(ONE));
    assertThat(engineRule.getManagementService().createJobQuery().withRetriesLeft().count(),is(0l));
    assertThat(engineRule.getManagementService().createJobQuery().noRetriesLeft().count(),is(ONE));

    execution = refreshExecutionEntity(execution.getId());
    assertThat(execution.getActivityId(),is(activityId));
  }

  protected void waitForExecutedJobWithRetriesLeft(int retriesLeft, String jobId) {
    JobQuery jobQuery = engineRule.getManagementService().createJobQuery();

    if (jobId != null) {
      jobQuery.jobId(jobId);
    }

    Job job = jobQuery.singleResult();

    try {
      engineRule.getManagementService().executeJob(job.getId());
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

  protected void stillOneJobWithExceptionAndRetriesLeft() {
    assertThat(engineRule.getManagementService().createJobQuery().withException().count(),is(ONE));
    assertThat(engineRule.getManagementService().createJobQuery().withRetriesLeft().count(),is(ONE));
  }

  protected Job fetchJob(String processInstanceId) {
    return engineRule.getManagementService().createJobQuery().processInstanceId(processInstanceId).singleResult();
  }

  protected ExecutionEntity fetchExecutionEntity(String processInstanceId, String activityId) {
    return (ExecutionEntity) engineRule.getRuntimeService().createExecutionQuery()
        .processInstanceId(processInstanceId).activityId(activityId).singleResult();
  }

  protected Job refreshJob(String jobId) {
    return engineRule.getManagementService().createJobQuery().jobId(jobId).singleResult();
  }

  protected ExecutionEntity refreshExecutionEntity(String executionId) {
    return (ExecutionEntity) engineRule.getRuntimeService().createExecutionQuery().executionId(executionId).singleResult();
  }
}
