package org.camunda.bpm.engine.test.jobexecutor;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.commons.testing.ProcessEngineLoggingRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class JobAcquisitionLoggingTest {

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule().watch("org.camunda.bpm.engine.jobexecutor", Level.DEBUG);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule).around(loggingRule);

  private RuntimeService runtimeService;
  private ManagementService managementService;
  private ProcessEngineConfigurationImpl processEngineConfiguration;

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    managementService = engineRule.getProcessEngine().getManagementService();
  }

  @After
  public void tearDown() {
    List<Job> jobs = managementService.createJobQuery().processDefinitionKey("simpleAsyncProcess").list();

    // remove simple async process jobs
    for (Job job : jobs) {
      managementService.deleteJob(job.getId());
    }
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/jobexecutor/simpleAsyncProcess.bpmn20.xml" })
  public void shouldLogJobsAttemptingToAcquire() {
    // given five jobs
    for (int i = 0; i < 5; i++) {
      runtimeService.startProcessInstanceByKey("simpleAsyncProcess");
    }

    // given max-job-acquisition threshold to three
    processEngineConfiguration.getJobExecutor().setMaxJobsPerAcquisition(3);

    // when executing the jobs
    processEngineConfiguration.getJobExecutor().start();
    testRule.waitForJobExecutorToProcessAllJobs();
    processEngineConfiguration.getJobExecutor().shutdown();

    // look for log where it states that "acquiring three jobs"
    List<ILoggingEvent> filteredLogList = loggingRule.getFilteredLog("Attempting to acquire 3 jobs for the process engine 'default'");

    // then find the expected logs
    assertThat(filteredLogList.size()).isGreaterThan(4);
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/jobexecutor/simpleAsyncProcess.bpmn20.xml" })
  public void shouldLogFailedAcquisitionLocks() {
    // given five jobs
    for (int i = 0; i < 5; i++) {
      runtimeService.startProcessInstanceByKey("simpleAsyncProcess");
    }

    // when executing the jobs
    processEngineConfiguration.getJobExecutor().start();
    testRule.waitForJobExecutorToProcessAllJobs();
    processEngineConfiguration.getJobExecutor().shutdown();

    List<ILoggingEvent> filteredLogList = loggingRule.getFilteredLog("Jobs failed to Lock during Acquisition of jobs for the process engine 'default' : 0");

    // then get logs that states there were no faults during job acquisition locks
    assertThat(filteredLogList.size()).isGreaterThan(4);
  }
}
