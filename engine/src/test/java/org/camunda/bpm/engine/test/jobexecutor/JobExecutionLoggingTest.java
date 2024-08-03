package org.camunda.bpm.engine.test.jobexecutor;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.jobexecutor.CallerRunsRejectedJobsHandler;
import org.camunda.bpm.engine.impl.jobexecutor.DefaultJobExecutor;
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
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

public class JobExecutionLoggingTest {

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
    List<Job> simpleAsyncProcessJobs = managementService.createJobQuery().processDefinitionKey("simpleAsyncProcess").list();
    List<Job> testProcessJobs = managementService.createJobQuery().processDefinitionKey("testProcess").list();

    // remove simple async process jobs
    for (Job job : simpleAsyncProcessJobs) {
      managementService.deleteJob(job.getId());
    }

    // remove test process jobs
    for (Job job : testProcessJobs) {
      managementService.deleteJob(job.getId());
    }
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/jobexecutor/simpleAsyncProcess.bpmn20.xml"})
  public void shouldLogJobsQueuedForExecution() {
    // replace job executor with one that has custom threadpool executor settings
    JobExecutionLoggingTest.TestJobExecutor testJobExecutor = new JobExecutionLoggingTest.TestJobExecutor();
    testJobExecutor.setMaxJobsPerAcquisition(10);
    processEngineConfiguration.setJobExecutor(testJobExecutor);
    testJobExecutor.registerProcessEngine(processEngineConfiguration.getProcessEngine());

    // given five jobs
    for (int i = 0; i < 5; i++) {
      runtimeService.startProcessInstanceByKey("simpleAsyncProcess");
    }

    // when executing the jobs
    processEngineConfiguration.getJobExecutor().start();
    testRule.waitForJobExecutorToProcessAllJobs(5000L);
    processEngineConfiguration.getJobExecutor().shutdown();

    // look for filled queue logs
    List<ILoggingEvent> filteredLogList = loggingRule.getFilteredLog("Jobs currently in queue to be executed for the process engine 'default' is 2 out of the max queue size : 2");

    // then 3 instances of filled queue logs will be available as there is 2 additional threads possible to reach max-pool-size
    assertThat(filteredLogList.size()).isEqualTo(3);
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/jobexecutor/simpleAsyncProcess.bpmn20.xml"})
  public void shouldLogJobsInExecution() {
    // replace job executor with one that has custom threadpool executor settings
    JobExecutionLoggingTest.TestJobExecutor testJobExecutor = new JobExecutionLoggingTest.TestJobExecutor();
    testJobExecutor.setMaxJobsPerAcquisition(10);
    processEngineConfiguration.setJobExecutor(testJobExecutor);
    testJobExecutor.registerProcessEngine(processEngineConfiguration.getProcessEngine());

    // given five jobs
    for (int i = 0; i < 5; i++) {
      runtimeService.startProcessInstanceByKey("simpleAsyncProcess");
    }

    // when executing the jobs
    processEngineConfiguration.getJobExecutor().start();
    testRule.waitForJobExecutorToProcessAllJobs(5000L);
    processEngineConfiguration.getJobExecutor().shutdown();

    // look for 3 jobs in execution
    List<ILoggingEvent> filteredLogList = loggingRule.getFilteredLog("Jobs currently in execution for the process engine 'default' : 3");

    // then one count of 'three jobs in execution' will be available as 2 out of 5 jobs should be queued.
    assertThat(filteredLogList.size()).isEqualTo(1);
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/jobexecutor/simpleAsyncProcess.bpmn20.xml"})
  public void shouldLogAvailableJobExecutionThreads() {
    // replace job executor with one that has custom threadpool executor settings
    JobExecutionLoggingTest.TestJobExecutor testJobExecutor = new JobExecutionLoggingTest.TestJobExecutor();
    testJobExecutor.setMaxJobsPerAcquisition(10);
    processEngineConfiguration.setJobExecutor(testJobExecutor);
    testJobExecutor.registerProcessEngine(processEngineConfiguration.getProcessEngine());

    // given five jobs
    for (int i = 0; i < 5; i++) {
      runtimeService.startProcessInstanceByKey("simpleAsyncProcess");
    }

    // when executing the jobs
    processEngineConfiguration.getJobExecutor().start();
    testRule.waitForJobExecutorToProcessAllJobs(5000L);
    processEngineConfiguration.getJobExecutor().shutdown();

    // look for 2 count of available job execution threads
    List<ILoggingEvent> filteredLogList = loggingRule.getFilteredLog("Available job execution threads for the process engine 'default' : 2");

    // then 2 threads will be available for 3 number of times because of queuing action post the core-pool-size limit is met
    assertThat(filteredLogList.size()).isEqualTo(3);
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/jobexecutor/delegateThrowsException.bpmn20.xml" })
  public void shouldLogJobExecutionRejections() {
    // given three jobs
    for (int i = 0; i < 3; i++) {
      runtimeService.startProcessInstanceByKey("testProcess");
    }

    // replace job executor with one that rejects all jobs
    JobExecutionLoggingTest.RejectionJobExecutor rejectionExecutor = new JobExecutionLoggingTest.RejectionJobExecutor();
    processEngineConfiguration.setJobExecutor(rejectionExecutor);
    rejectionExecutor.registerProcessEngine(processEngineConfiguration.getProcessEngine());

    // when executing the jobs
    processEngineConfiguration.getJobExecutor().start();
    testRule.waitForJobExecutorToProcessAllJobs(5000L);
    processEngineConfiguration.getJobExecutor().shutdown();

    // look for job execution rejection job count with one job
    List<ILoggingEvent> filteredLogList = loggingRule.getFilteredLog("Jobs execution rejections for the process engine 'default' : 1");

    // then there exist job execution rejection logs with job count as one
    assertThat(filteredLogList.size()).isGreaterThan(0);
  }

  public static class TestJobExecutor extends DefaultJobExecutor {

    protected int queueSize = 2;
    protected int corePoolSize = 1;
    protected int maxPoolSize = 3;
    protected BlockingQueue<Runnable> threadPoolQueue;
    public TestJobExecutor() {
      threadPoolQueue = new ArrayBlockingQueue<>(queueSize);
      threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 0L, TimeUnit.MILLISECONDS, threadPoolQueue);
      threadPoolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
    }
  }

  public static class RejectionJobExecutor extends TestJobExecutor {
    public RejectionJobExecutor() {
      threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 0L, TimeUnit.MILLISECONDS, threadPoolQueue) {
        @Override
        public void execute(Runnable command) {
          throw new RejectedExecutionException();
        }
      };
      rejectedJobsHandler = new CallerRunsRejectedJobsHandler();
    }
  }

}