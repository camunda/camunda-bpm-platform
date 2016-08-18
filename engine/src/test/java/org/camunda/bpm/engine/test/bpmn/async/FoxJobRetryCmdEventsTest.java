package org.camunda.bpm.engine.test.bpmn.async;

import static org.camunda.bpm.engine.test.bpmn.async.RetryCmdDeployment.deployment;
import static org.camunda.bpm.engine.test.bpmn.async.RetryCmdDeployment.prepareCompensationEventProcess;
import static org.camunda.bpm.engine.test.bpmn.async.RetryCmdDeployment.prepareEscalationEventProcess;
import static org.camunda.bpm.engine.test.bpmn.async.RetryCmdDeployment.prepareMessageEventProcess;
import static org.camunda.bpm.engine.test.bpmn.async.RetryCmdDeployment.prepareSignalEventProcess;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.Job;
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

/**
 * @author Askar Akhmerov
 */
@RunWith(Parameterized.class)
public class FoxJobRetryCmdEventsTest {

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
            .withEventProcess(prepareSignalEventProcess()),
        deployment()
            .withEventProcess(prepareMessageEventProcess()),
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
    assertJobRetries(pi);
  }

  @After
  public void tearDown() {
    engineRule.getRepositoryService().deleteDeployment(currentDeployment.getId(),true,true);
  }

  protected void assertJobRetries(ProcessInstance pi) {
    assertThat(pi,is(notNullValue()));

    Job job = fetchJob(pi.getProcessInstanceId());

    try {
      engineRule.getManagementService().executeJob(job.getId());
    } catch (Exception e) {
    }

    // update job
    job = fetchJob(pi.getProcessInstanceId());
    assertThat(job.getRetries(),is(4));
  }

  protected Job fetchJob(String processInstanceId) {
    return engineRule.getManagementService().createJobQuery().processInstanceId(processInstanceId).singleResult();
  }


}
