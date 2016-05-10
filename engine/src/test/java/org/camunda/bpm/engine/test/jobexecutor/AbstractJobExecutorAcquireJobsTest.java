package org.camunda.bpm.engine.test.jobexecutor;

import java.util.List;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ClockTestUtil;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

public abstract class AbstractJobExecutorAcquireJobsTest {

  @Rule
  public ProcessEngineRule rule = new ProvidedProcessEngineRule();

  protected ManagementService managementService;
  protected RuntimeService runtimeService;

  protected ProcessEngineConfigurationImpl configuration;

  private boolean jobExecutorAcquireByDueDate;
  private boolean jobExecutorAcquireByPriority;
  private boolean jobExecutorPreferTimerJobs;

  @Before
  public void initServices() {
    runtimeService = rule.getRuntimeService();
    managementService = rule.getManagementService();
  }

  @Before
  public void saveProcessEngineConfiguration() {
    configuration = (ProcessEngineConfigurationImpl) rule.getProcessEngine().getProcessEngineConfiguration();
    jobExecutorAcquireByDueDate = configuration.isJobExecutorAcquireByDueDate();
    jobExecutorAcquireByPriority = configuration.isJobExecutorAcquireByPriority();
    jobExecutorPreferTimerJobs = configuration.isJobExecutorPreferTimerJobs();
  }

  @Before
  public void setClock() {
    ClockTestUtil.setClockToDateWithoutMilliseconds();
  }

  @After
  public void restoreProcessEngineConfiguration() {
    configuration.setJobExecutorAcquireByDueDate(jobExecutorAcquireByDueDate);
    configuration.setJobExecutorAcquireByPriority(jobExecutorAcquireByPriority);
    configuration.setJobExecutorPreferTimerJobs(jobExecutorPreferTimerJobs);
  }

  @After
  public void resetClock() {
    ClockUtil.reset();
  }

  protected List<JobEntity> findAcquirableJobs() {
    return configuration.getCommandExecutorTxRequired().execute(new Command<List<JobEntity>>() {

      @Override
      public List<JobEntity> execute(CommandContext commandContext) {
        return commandContext
          .getJobManager()
          .findNextJobsToExecute(new Page(0, 100));
      }
    });
  }

  protected String startProcess(String processDefinitionKey, String activity) {
    return runtimeService
      .createProcessInstanceByKey(processDefinitionKey)
      .startBeforeActivity(activity)
      .execute().getId();
  }

  protected void startProcess(String processDefinitionKey, String activity, int times) {
    for (int i = 0; i < times; i++) {
      startProcess(processDefinitionKey, activity);
    }
  }

}
