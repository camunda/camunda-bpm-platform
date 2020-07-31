package org.camunda.bpm.engine.test.cockroachdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.FetchExternalTasksCmd;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.externaltask.TopicFetchInstruction;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.test.RequiredDatabase;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.concurrency.CompetingExternalTaskFetchingTest;
import org.camunda.bpm.engine.test.concurrency.CompetingJobAcquisitionTest;
import org.camunda.bpm.engine.test.concurrency.ConcurrencyTestHelper;
import org.camunda.bpm.engine.test.jobexecutor.ControllableJobExecutor;
import org.camunda.bpm.engine.test.jobexecutor.RecordingAcquireJobsRunnable.RecordedWaitEvent;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

@RequiredDatabase(includes = DbSqlSessionFactory.CRDB)
public class CockroachDBRetriesTest extends ConcurrencyTestHelper {

  private static final int COMMAND_RETRIES = 3;
  private static final int DEFAULT_NUM_JOBS_TO_ACQUIRE = 3;
  
  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(
      c -> c.setCommandRetries(COMMAND_RETRIES).setJobExecutor(new ControllableJobExecutor()));
  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected ControllableJobExecutor jobExecutor1;
  protected ControllableJobExecutor jobExecutor2;

  protected ThreadControl acquisitionThread1;
  protected ThreadControl acquisitionThread2;
  
  @Before
  public void setUp() throws Exception {
    processEngineConfiguration = (ProcessEngineConfigurationImpl) engineRule.getProcessEngine().getProcessEngineConfiguration();
    
    // two job executors with the default settings
    jobExecutor1 = (ControllableJobExecutor)
        processEngineConfiguration.getJobExecutor();
    jobExecutor1.setMaxJobsPerAcquisition(DEFAULT_NUM_JOBS_TO_ACQUIRE);
    acquisitionThread1 = jobExecutor1.getAcquisitionThreadControl();

    jobExecutor2 = new ControllableJobExecutor((ProcessEngineImpl) engineRule.getProcessEngine());
    jobExecutor2.setMaxJobsPerAcquisition(DEFAULT_NUM_JOBS_TO_ACQUIRE);
    acquisitionThread2 = jobExecutor2.getAcquisitionThreadControl();
  }

  @After
  public void tearDown() throws Exception {
    jobExecutor1.shutdown();
    jobExecutor2.shutdown();
  }
  
  /**
   * See {@link CompetingJobAcquisitionTest#testCompetingJobAcquisitions} for the test
   * case without retries
   */
  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/jobexecutor/simpleAsyncProcess.bpmn20.xml")
  public void shouldRetryJobAcquisition() {

    // given
    int numJobs = DEFAULT_NUM_JOBS_TO_ACQUIRE + 1;
    for (int i = 0; i < numJobs; i++) {
      engineRule.getRuntimeService().startProcessInstanceByKey("simpleAsyncProcess").getId();
    }

    jobExecutor1.start();
    jobExecutor2.start();

    // both acquisition threads wait before acquiring something
    acquisitionThread1.waitForSync();
    acquisitionThread2.waitForSync();
    
    // both threads run the job acquisition query and should get overlapping results (3 out of 4 jobs)
    acquisitionThread1.makeContinueAndWaitForSync();
    acquisitionThread2.makeContinueAndWaitForSync();
    
    // thread1 flushes and commits first (success)
    acquisitionThread1.makeContinueAndWaitForSync();
    
    // when
    // acquisition fails => retry interceptor kicks in and retries command => waiting again before acquisition
    acquisitionThread2.makeContinueAndWaitForSync();

    // thread2 immediately acquires again and commits
    acquisitionThread2.makeContinueAndWaitForSync();
    acquisitionThread2.makeContinueAndWaitForSync();
    
    // then 
    // all jobs have been executed
    long currentJobs = engineRule.getManagementService().createJobQuery().active().count();
    assertThat(currentJobs).isEqualTo(0);
    
    // and thread2 has no reported failure
    assertThat(acquisitionThread2.getException()).isNull();
    
    List<RecordedWaitEvent> jobAcquisition2WaitEvents = jobExecutor2.getAcquireJobsRunnable().getWaitEvents();
    
    // and only one cycle of job acquisition was made (the wait event is from after the acquisition finished)
    assertThat(jobAcquisition2WaitEvents).hasSize(1); 
    Exception acquisitionException = jobAcquisition2WaitEvents.get(0).getAcquisitionException();
    
    // and the exception never bubbled up to the job executor (i.e. the retry was transparent)
    assertThat(acquisitionException).isNull(); 
  }
  
  
  /**
   * See {@link CompetingExternalTaskFetchingTest#testCompetingExternalTaskFetching()}
   * for the test case without retries.
   */
  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/concurrency/CompetingExternalTaskFetchingTest.testCompetingExternalTaskFetching.bpmn20.xml")
  public void shouldRetryExternalTaskFetchAndLock() {
    // given
    RuntimeService runtimeService = engineRule.getRuntimeService();
    
    int numTasksToFetch = 3;
    int numExternalTasks = numTasksToFetch + 1;
    
    for (int i = 0; i < numExternalTasks; i++) {
      runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    }

    ThreadControl thread1 = executeControllableCommand(new ControlledFetchAndLockCommand(numTasksToFetch, "thread1", "externalTaskTopic"));
    ThreadControl thread2 = executeControllableCommand(new ControlledFetchAndLockCommand(numTasksToFetch, "thread2", "externalTaskTopic"));

    // thread1 and thread2 begin their transactions and fetch tasks
    thread1.waitForSync();
    thread2.waitForSync();
    thread1.makeContinueAndWaitForSync();
    thread2.makeContinueAndWaitForSync();
    
    // thread1 commits
    thread1.waitUntilDone();

    // when
    // thread2 flushes and fails => leads to retry
    thread2.waitUntilDone(true);
    
    // then
    List<ExternalTask> tasks = engineRule.getExternalTaskService().createExternalTaskQuery().list();
    List<ExternalTask> thread1Tasks = tasks.stream()
        .filter(t -> "thread1".equals(t.getWorkerId())).collect(Collectors.toList());
    List<ExternalTask> thread2Tasks = tasks.stream()
        .filter(t -> "thread2".equals(t.getWorkerId())).collect(Collectors.toList());
    
    assertThat(tasks).hasSize(numExternalTasks);
    assertThat(thread1Tasks).hasSize(numTasksToFetch);
    assertThat(thread2Tasks).hasSize(numExternalTasks - numTasksToFetch);
  }
  
  @Test
  public void shouldRetryDeployCmd() {
    fail("implement");
  }

  @Test
  public void shouldRetryEngineBootstrapCmd() {
    fail("implement");
  }
  
  @Test
  public void shouldNotRetryCommandByDefault() {
    fail("implement");
  }
  
  private static class ControlledFetchAndLockCommand extends ControllableCommand<List<LockedExternalTask>> {

    private FetchExternalTasksCmd wrappedCmd;
    
    public ControlledFetchAndLockCommand(int numTasks, String workerId, String topic) {
      Map<String, TopicFetchInstruction> instructions = new HashMap<String, TopicFetchInstruction>();

      TopicFetchInstruction instruction = new TopicFetchInstruction(topic, 10000L);
      instructions.put(topic, instruction);
      
      this.wrappedCmd = new FetchExternalTasksCmd(workerId, numTasks, instructions);
    }
    
    @Override
    public List<LockedExternalTask> execute(CommandContext commandContext) {
      monitor.sync();

      List<LockedExternalTask> tasks = wrappedCmd.execute(commandContext);
      
      monitor.sync();
        
      return tasks;
    }
    
    @Override
    public boolean isRetryable() {
      return wrappedCmd.isRetryable();
    }
    
  }
  
}
