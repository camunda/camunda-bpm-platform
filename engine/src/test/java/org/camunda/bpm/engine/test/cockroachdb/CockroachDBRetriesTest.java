/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.cockroachdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.CrdbTransactionRetryException;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.impl.BootstrapEngineCommand;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.FetchExternalTasksCmd;
import org.camunda.bpm.engine.impl.cmd.SetJobDefinitionPriorityCmd;
import org.camunda.bpm.engine.impl.cmd.SuspendJobDefinitionCmd;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.externaltask.TopicFetchInstruction;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.management.UpdateJobDefinitionSuspensionStateBuilderImpl;
import org.camunda.bpm.engine.impl.test.RequiredDatabase;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.concurrency.CompetingExternalTaskFetchingTest;
import org.camunda.bpm.engine.test.concurrency.CompetingJobAcquisitionTest;
import org.camunda.bpm.engine.test.concurrency.ConcurrencyTestHelper;
import org.camunda.bpm.engine.test.concurrency.ConcurrentJobExecutorTest;
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

  protected static final int DEFAULT_NUM_JOBS_TO_ACQUIRE = 3;
  protected static final int COMMAND_RETRIES = 3;
  protected static final String PROCESS_ENGINE_NAME = "failingProcessEngine";

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(
      c -> c.setCommandRetries(COMMAND_RETRIES));
  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected ControllableJobExecutor jobExecutor1;
  protected ControllableJobExecutor jobExecutor2;

  protected ThreadControl acquisitionThread1;
  protected ThreadControl acquisitionThread2;

  protected RuntimeService runtimeService;
  protected ManagementService managementService;

  @Before
  public void setUp() throws Exception {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    runtimeService = engineRule.getRuntimeService();
    managementService = engineRule.getManagementService();

    // two job executors with the default settings
    jobExecutor1 = new ControllableJobExecutor((ProcessEngineImpl) engineRule.getProcessEngine());
    jobExecutor1.setMaxJobsPerAcquisition(DEFAULT_NUM_JOBS_TO_ACQUIRE);
    acquisitionThread1 = jobExecutor1.getAcquisitionThreadControl();
    processEngineConfiguration.setJobExecutor(jobExecutor1);

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

  /**
   * See {@link ConcurrentJobExecutorTest#testCompletingUpdateJobDefinitionPriorityDuringExecution()}
   * for the test case without retries.
   */
  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/jobexecutor/simpleAsyncProcess.bpmn20.xml")
  public void shouldRetryJobExecutionTxAfterJobPriorityOLE() {
    // given
    // several running process instances with an async continuation
    int numJobs = DEFAULT_NUM_JOBS_TO_ACQUIRE + 1;
    for (int i = 0; i < numJobs; i++) {
      engineRule.getRuntimeService().startProcessInstanceByKey("simpleAsyncProcess");
    }
    // and a job definition
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    jobExecutor1.start();
    acquisitionThread1.waitForSync();
    ThreadControl jobPriorityThread = executeControllableCommand(new ControllableJobDefinitionPriorityCommand(jobDefinition.getId(), 42L, true));
    jobPriorityThread.waitForSync();

    // the acquisition thread acquires the jobs
    acquisitionThread1.makeContinueAndWaitForSync();

    // the job priority thread updates the job priority and flushes the change
    jobPriorityThread.makeContinue();
    jobPriorityThread.waitUntilDone(true);

    // when
    // the job acquisition thread attempts to flush the job locks it fails
    acquisitionThread1.makeContinueAndWaitForSync();

    // thread2 immediately acquires again and commits
    acquisitionThread1.makeContinueAndWaitForSync();
    acquisitionThread1.makeContinueAndWaitForSync();
    acquisitionThread1.ignoreFutureSyncs();

    // then
    // the jobs have been executed and only one is remaining
    Job currentJob = engineRule.getManagementService().createJobQuery().active().singleResult();
    assertThat(currentJob).isNotNull();
    // the job priority has changed
    assertEquals(42, currentJob.getPriority());
    // and thread2 has no reported failure
    assertThat(acquisitionThread1.getException()).isNull();
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/jobexecutor/simpleAsyncProcess.bpmn20.xml")
  public void shouldRetryAcquisitionJobTxAfterJobSuspensionOLE() {
    // given
    String processDefinitionKey = "simpleAsyncProcess";
    // a running process instance with an async continuation
    runtimeService.startProcessInstanceByKey(processDefinitionKey);
    // a waiting acquisition and a waiting suspension
    jobExecutor1.start();
    acquisitionThread1.reportInterrupts();
    acquisitionThread1.waitForSync();
    ThreadControl jobSuspensionThread = executeControllableCommand(new ControllableJobSuspensionCommand(processDefinitionKey));
    jobSuspensionThread.reportInterrupts();
    jobSuspensionThread.waitForSync();

    // the acquisition thread acquires the jobs
    acquisitionThread1.makeContinueAndWaitForSync();

    // when the job suspension thread completes the suspension
    jobSuspensionThread.makeContinue();
    jobSuspensionThread.waitUntilDone(true);

    // then
    // the acquisition thread fails to flush the job locks
    acquisitionThread1.makeContinueAndWaitForSync();

    // the acquisition thread immediately acquires again and commits
    acquisitionThread1.makeContinueAndWaitForSync();
    acquisitionThread1.makeContinueAndWaitForSync();

    // the suspension state doesn't fail at all
    assertNull(jobSuspensionThread.getException());
    // and the acquisition will not fail with optimistic locking since it was retried
    assertNull(acquisitionThread1.getException());
    // and no jobs were executed since they are suspended
    long jobCount = managementService.createJobQuery().suspended().count();
    assertThat(jobCount).isOne();
  }

  @Test
  public void shouldRethrowBootstrapEngineOleWhenRetriesAreExhausted() {
    // given
    // a bootstrap command failing with a CrdbTransactionRetryException
    FailingProcessEngineBootstrapCommand bootstrapCommand = new FailingProcessEngineBootstrapCommand();
    ProcessEngineConfigurationImpl processEngineConfiguration = ((ProcessEngineConfigurationImpl)
        ProcessEngineConfiguration
            .createProcessEngineConfigurationFromResource("camunda.cfg.xml"))
        .setCommandRetries(COMMAND_RETRIES)
        .setProcessEngineName(PROCESS_ENGINE_NAME);
    processEngineConfiguration.setProcessEngineBootstrapCommand(bootstrapCommand);

    // when/then
    // a CrdbTransactionRetryException is re-thrown to the caller
    assertThatThrownBy(() -> processEngineConfiguration.buildProcessEngine())
      .isInstanceOf(CrdbTransactionRetryException.class);

    // since the Command retries were exausted
    assertThat(bootstrapCommand.getTries()).isEqualTo(4);
  }

  @Test
  public void shouldNotRetryCommandByDefault() {
    // given
    // a regular, non-retryable command that throws a CrdbTransactionRetryException
    CrdbFailingCommand failingCommand = new CrdbFailingCommand();

    // when/then
    // a CrdbTransactionRetryException should be reported to the caller of the command
    assertThatThrownBy(() -> processEngineConfiguration.getCommandExecutorTxRequired().execute(failingCommand))
      .isInstanceOf(CrdbTransactionRetryException.class)
      .hasMessageContaining("Does not retry");

    // and
    // also the command should be executed only once
    assertThat(failingCommand.getTries()).isEqualTo(1);
  }

  protected static class ControlledFetchAndLockCommand extends ControllableCommand<List<LockedExternalTask>> {

    protected FetchExternalTasksCmd wrappedCmd;

    public ControlledFetchAndLockCommand(int numTasks, String workerId, String topic) {
      Map<String, TopicFetchInstruction> instructions = new HashMap<>();

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

  protected static class FailingProcessEngineBootstrapCommand  extends BootstrapEngineCommand {

    protected int tries;

    public FailingProcessEngineBootstrapCommand() {
      this.tries = 0;
    }

    @Override
    public Void execute(CommandContext commandContext) {

      tries++;
      throw new CrdbTransactionRetryException("The Process Engine Bootstrap has failed.");

    }

    public int getTries() {
      return tries;
    }

    @Override
    public boolean isRetryable() {
      return super.isRetryable();
    }
  }

  protected static class CrdbFailingCommand implements Command<Void> {

    protected int tries = 0;

    @Override
    public Void execute(CommandContext commandContext) {
      tries++;
      throw new CrdbTransactionRetryException("Does not retry");
    }

    public int getTries() {
      return tries;
    }
  }

  protected class ControllableJobDefinitionPriorityCommand extends ControllableCommand<Void> {

    protected SetJobDefinitionPriorityCmd jobDefinitionPriorityCmd;

    public ControllableJobDefinitionPriorityCommand(String jobDefinitionId, Long priority, boolean cascade) {
      this.jobDefinitionPriorityCmd = new SetJobDefinitionPriorityCmd(jobDefinitionId, priority, cascade);
    }

    @Override
    public Void execute(CommandContext commandContext) {

      monitor.sync();

      jobDefinitionPriorityCmd.execute(commandContext);

      monitor.sync();

      return null;
    }
  }

  protected class ControllableJobSuspensionCommand extends ControllableCommand<Void> {

    protected SuspendJobDefinitionCmd suspendJobDefinitionCmd;

    public ControllableJobSuspensionCommand(String processDefinitionKey) {
      UpdateJobDefinitionSuspensionStateBuilderImpl builder = new UpdateJobDefinitionSuspensionStateBuilderImpl()
          .byProcessDefinitionKey(processDefinitionKey)
          .includeJobs(true);

      this.suspendJobDefinitionCmd = new SuspendJobDefinitionCmd(builder);
    }

    @Override
    public Void execute(CommandContext commandContext) {

      monitor.sync();

      suspendJobDefinitionCmd.execute(commandContext);

      monitor.sync();

      return null;
    }
  }
}
