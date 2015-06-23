package org.camunda.bpm.engine.test.jobexecutor;

import java.io.FileNotFoundException;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.test.AbstractProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.junit.After;
import org.junit.Before;

public abstract class AbstractJobExecutorAcquireJobsTest extends AbstractProcessEngineTestCase {

  protected static final Date START_TIME = new Date(1430134560000l);

  protected void initializeProcessEngine() {
    ProcessEngineConfiguration processEngineConfiguration;
    try {
      processEngineConfiguration = ProcessEngineConfiguration
        .createProcessEngineConfigurationFromResource("camunda.cfg.xml");
    } catch (RuntimeException ex) {
      if (ex.getCause() != null && ex.getCause() instanceof FileNotFoundException) {
        processEngineConfiguration = ProcessEngineConfiguration
          .createProcessEngineConfigurationFromResource("activiti.cfg.xml");
      } else {
        throw ex;
      }
    }

    // only set values if required otherwise test default behavior
    if (isJobExecutorPreferTimerJobs()) {
      processEngineConfiguration.setJobExecutorPreferTimerJobs(true);
    }

    if (isJobExecutorPreferOldJobs()) {
      processEngineConfiguration.setJobExecutorAcquireByDueDate(true);
    }

    if (isJobExecutorAcquireByPriority()) {
      processEngineConfiguration.setProducePrioritizedJobs(true);
      processEngineConfiguration.setJobExecutorAcquireByPriority(true);
    }

    configure(processEngineConfiguration);

    processEngine = processEngineConfiguration.buildProcessEngine();
  }

  protected void configure(ProcessEngineConfiguration configuration) {
    // may be overriden for additional test-specific configuration
  }

  protected void closeDownProcessEngine() {
    super.closeDownProcessEngine();
    processEngine.close();
    ProcessEngines.unregister(processEngine);
    processEngine = null;
  }

  protected boolean isJobExecutorPreferTimerJobs() {
    return false;
  }

  protected boolean isJobExecutorPreferOldJobs() {
    return false;
  }

  protected boolean isJobExecutorAcquireByPriority() {
    return false;
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    // set clock to an initial time without milliseconds
    ClockUtil.setCurrentTime(START_TIME);
  }

  @After
  public void tearDown() throws Exception {
    processEngineConfiguration.setJobExecutorPreferTimerJobs(false);
    processEngineConfiguration.setJobExecutorAcquireByDueDate(false);
    ClockUtil.reset();
    super.tearDown();
  }

  protected List<JobEntity> findAcquirableJobs() {
    return processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<List<JobEntity>>() {

      @Override
      public List<JobEntity> execute(CommandContext commandContext) {
        return commandContext
          .getJobManager()
          .findNextJobsToExecute(new Page(0, 100));
      }
    });
  }

  protected void incrementClock(long seconds) {
    long time = ClockUtil.getCurrentTime().getTime();
    ClockUtil.setCurrentTime(new Date(time + seconds * 1000));
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
