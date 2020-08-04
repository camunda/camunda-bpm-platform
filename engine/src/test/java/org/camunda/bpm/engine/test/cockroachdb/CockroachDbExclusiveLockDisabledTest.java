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
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.camunda.bpm.engine.CrdbTransactionRetryException;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.BootstrapEngineCommand;
import org.camunda.bpm.engine.impl.HistoryLevelSetupCommand;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.DeployCmd;
import org.camunda.bpm.engine.impl.cmd.HistoryCleanupCmd;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandInvocationContext;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.repository.DeploymentBuilderImpl;
import org.camunda.bpm.engine.impl.test.RequiredDatabase;
import org.camunda.bpm.engine.impl.test.TestHelper;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.concurrency.ConcurrencyTestHelper;
import org.camunda.bpm.engine.test.concurrency.ConcurrentDeploymentTest;
import org.camunda.bpm.engine.test.concurrency.ConcurrentHistoryCleanupTest;
import org.camunda.bpm.engine.test.concurrency.ConcurrentHistoryLevelTest;
import org.camunda.bpm.engine.test.concurrency.ConcurrentInstallationIdInitializationTest;
import org.camunda.bpm.engine.test.concurrency.ConcurrentProcessEngineJobExecutorHistoryCleanupJobTest;
import org.camunda.bpm.engine.test.concurrency.ConcurrentTelemetryConfigurationTest;
import org.camunda.bpm.engine.test.jobexecutor.ControllableJobExecutor;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

@RequiredDatabase(includes = DbSqlSessionFactory.CRDB)
public class CockroachDbExclusiveLockDisabledTest extends ConcurrencyTestHelper {

  protected static final String PROCESS_ENGINE_NAME = "retriableBootstrapProcessEngine";
  protected static final int COMMAND_RETRIES = 3;
  protected final BpmnModelInstance PROCESS_WITH_USERTASK = Bpmn.createExecutableProcess("process")
      .startEvent()
      .userTask()
      .endEvent()
      .done();

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(
    c -> c.setCommandRetries(COMMAND_RETRIES).setJobExecutor(new ControllableJobExecutor()));
  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected RepositoryService repositoryService;
  protected ManagementService managementService;
  protected HistoryService historyService;

  @Before
  public void setUp() throws Exception {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    repositoryService = engineRule.getRepositoryService();
    managementService = engineRule.getManagementService();
    historyService = engineRule.getHistoryService();

    TestHelper.deleteInstallationId(processEngineConfiguration);
    TestHelper.deleteTelemetryProperty(processEngineConfiguration);
    TestHelper.deleteHistoryLevel(processEngineConfiguration);
  }

  @After
  public void tearDown() throws Exception {
    testRule.deleteHistoryCleanupJobs();
    processEngineConfiguration.getCommandExecutorTxRequired().execute((Command<Void>) commandContext -> {

      commandContext.getMeterLogManager().deleteAll();
      List<Job> jobs = managementService.createJobQuery().list();
      if (jobs.size() > 0) {
        String jobId = jobs.get(0).getId();
        commandContext.getJobManager().deleteJob((JobEntity) jobs.get(0));
        commandContext.getHistoricJobLogManager().deleteHistoricJobLogByJobId(jobId);
      }
      commandContext.getHistoricJobLogManager().deleteHistoricJobLogsByHandlerType("history-cleanup");

      return null;
    });
    for(org.camunda.bpm.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
    processEngineConfiguration.getDeploymentCache().purgeCache();
    closeDownProcessEngine();
  }

  /**
   * See {@link ConcurrentDeploymentTest#testVersioning()}
   * for the test case without retries, and with an exclusive lock.
   */
  @Test
  public void shouldRetryDeployCmdWithoutExclusiveLock() throws InterruptedException {
    // given
    DeploymentBuilder deploymentOne = createDeploymentBuilder();
    DeploymentBuilder deploymentTwo = createDeploymentBuilder();

    // STEP 1: bring two threads to a point where they have
    // 1) started a new transaction
    // 2) are ready to deploy
    ControllableDeployCommand deployCommand1 = new ControllableDeployCommand(deploymentOne);
    ControllableDeployCommand deployCommand2 = new ControllableDeployCommand(deploymentTwo);
    ConcurrencyTestHelper.ThreadControl thread1 = executeControllableCommand(deployCommand1);
    thread1.waitForSync();

    ConcurrencyTestHelper.ThreadControl thread2 = executeControllableCommand(deployCommand2);
    thread2.reportInterrupts();
    thread2.waitForSync();

    // STEP 2: make Thread 1 proceed and wait until it has deployed but not yet committed
    // -> will still hold the exclusive lock
    thread1.makeContinue();
    thread1.waitForSync();

    // when
    // STEP 3: make Thread 2 continue
    // -> it will attempt a deployment
    thread2.makeContinue();

    // wait for 2 seconds
    Thread.sleep(2000);

    // STEP 4: allow Thread 1 to terminate
    // -> Thread 1 will commit
    thread1.waitUntilDone();

    // then
    // STEP 5: wait for Thread 2 to fail on flush and retry
    thread2.waitForSync();
    thread2.waitUntilDone(true);

    // ensure that although both transactions were run concurrently, the process definitions have different versions
    List<ProcessDefinition> processDefinitions = repositoryService
      .createProcessDefinitionQuery()
      .orderByProcessDefinitionVersion()
      .asc()
      .list();

    Assert.assertThat(processDefinitions.size(), is(2));
    Assert.assertThat(processDefinitions.get(0).getVersion(), is(1));
    Assert.assertThat(processDefinitions.get(1).getVersion(), is(2));

    // ensure that the first deploy command was only executed once
    assertThat(deployCommand1.getTries()).isEqualTo(1);
    // while the second deploy command failed with an OLE, and was retried
    assertThat(deployCommand2.getTries()).isEqualTo(2);
  }

  /**
   * See {@link ConcurrentTelemetryConfigurationTest}
   * for the test case without retries, and with an exclusive lock.
   */
  @Test
  public void shouldEnableTelemetryWithoutExclusiveLock() throws InterruptedException {
    // given
    // two concurrent commands to create a new telemetry property
    ControllableUpdateTelemetrySetupCommand telemetrySetupCommand1 = new ControllableUpdateTelemetrySetupCommand();
    ControllableUpdateTelemetrySetupCommand telemetrySetupCommand2 = new ControllableUpdateTelemetrySetupCommand();

    ConcurrencyTestHelper.ThreadControl thread1 = executeControllableCommand(telemetrySetupCommand1);
    thread1.reportInterrupts();
    thread1.waitForSync();

    ConcurrencyTestHelper.ThreadControl thread2 = executeControllableCommand(telemetrySetupCommand2);
    thread2.reportInterrupts();
    thread2.waitForSync();

    // the first command initializes the property
    thread1.makeContinue();
    thread1.waitForSync();

    // the second command initializes the property
    thread2.makeContinue();

    Thread.sleep(2000);

    // the first commands flushes its result
    thread1.waitUntilDone();

    // when
    // the second command attempts to flush its result
    thread2.waitForSync();
    // fails, and retries
    thread2.waitUntilDone(true);

    // then
    // the first command shouldn't fail at all
    assertNull(thread1.getException());
    // the second command should fail with an OLE, but it should be
    // caught by the CrdbTransactionRetryInterceptor
    assertNull(thread2.getException());
    // the telemetry property is successfully set
    assertThat(managementService.isTelemetryEnabled()).isFalse();

    // the first command was only executed once
    assertThat(telemetrySetupCommand1.getTries()).isOne();
    // but the second failed with an OLE, and was retried
    assertThat(telemetrySetupCommand2.getTries()).isEqualTo(2);
  }

  /**
   * See {@link ConcurrentInstallationIdInitializationTest}
   * for the test case without retries, and with an exclusive lock.
   */
  @Test
  public void shouldSetInstallationIdWithoutExclusiveLock() throws InterruptedException {
    // given
    // two concurrent commands to set an installation id
    ControllableInstallationIdInitializationCommand initializationCommand1 =
      new ControllableInstallationIdInitializationCommand();
    ControllableInstallationIdInitializationCommand initializationCommand2 =
      new ControllableInstallationIdInitializationCommand();
    ConcurrencyTestHelper.ThreadControl thread1 = executeControllableCommand(initializationCommand1);
    thread1.reportInterrupts();
    thread1.waitForSync();

    ConcurrencyTestHelper.ThreadControl thread2 = executeControllableCommand(initializationCommand2);
    thread2.reportInterrupts();
    thread2.waitForSync();

    // the first command initializes an installation id property
    thread1.makeContinue();
    thread1.waitForSync();

    // the second command initializes an installation id property
    thread2.makeContinue();

    Thread.sleep(2000);

    // the first command flushes its installation id
    thread1.waitUntilDone();
    String firstInstallationId = processEngineConfiguration.getInstallationId();

    // when
    // the second command attempts to flush its installation id
    thread2.waitForSync();
    // fails, and is retried
    thread2.waitUntilDone(true);

    // then
    // the fist command shouldn't fail
    assertNull(thread1.getException());
    // the second command should fail, but the OLE should be caught
    assertNull(thread2.getException());

    // the first command was only executed once
    assertThat(initializationCommand1.getTries()).isOne();
    // but the second failed with an OLE, and was retried
    assertThat(initializationCommand2.getTries()).isEqualTo(2);

    // the installation id of the second command is the last one
    String secondInstallationId = processEngineConfiguration.getInstallationId();
    assertThat(secondInstallationId).isNotEmpty();
    assertThat(secondInstallationId).isNotEqualTo(firstInstallationId);
    assertThat(secondInstallationId).matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}");
  }

  /**
   * See {@link ConcurrentHistoryCleanupTest}
   * for the test case without retries, and with an exclusive lock.
   */
  @Test
  public void shouldReconfigureHistoryCleanupJobWithoutExclusiveLock() throws InterruptedException {
    // given
    ControllableHistoryCleanupCommand historyCleanupCommand1 = new ControllableHistoryCleanupCommand();
    ControllableHistoryCleanupCommand historyCleanupCommand2 = new ControllableHistoryCleanupCommand();

    // first thread that executes a HistoryCleanupCmd
    ThreadControl thread1 = executeControllableCommand(historyCleanupCommand1);
    thread1.waitForSync();

    // second thread that executes a HistoryCleanupCmd
    ThreadControl thread2 = executeControllableCommand(historyCleanupCommand2);
    thread2.reportInterrupts();
    thread2.waitForSync();

    // first thread executes the job, reconfigures the next one and waits to flush to the db
    thread1.makeContinue();
    thread1.waitForSync();

    // second thread executes the job, reconfigures the next one and waits to flush to the db
    thread2.makeContinue();

    Thread.sleep(2000);

    // first thread flushes the changes to the db
    thread1.waitUntilDone();

    //only one history cleanup job exists -> no exception
    List<Job> historyCleanupJobs = historyService.findHistoryCleanupJobs();
    assertEquals(1, historyCleanupJobs.size());
    Job firstHistoryCleanupJob = historyCleanupJobs.get(0);

    // second thread attempts to flush, fails and retries
    thread2.waitForSync();
    thread2.waitUntilDone(true);

    // the OLE was caught by the CrdbTransactionRetryInterceptor
    assertNull(thread2.getException());
    // and the command was retried
    assertEquals(2, ((ControllableHistoryCleanupCommand) controllableCommands.get(1)).getTries());

    //still, only one history cleanup job exists -> no exception
    historyCleanupJobs = historyService.findHistoryCleanupJobs();
    assertEquals(1, historyCleanupJobs.size());

    // however, thread2 successfully reconfigured the HistoryCleanupJob
    Job secondHistoryCleanupJob = historyCleanupJobs.get(0);
    assertTrue(secondHistoryCleanupJob.getDuedate().after(firstHistoryCleanupJob.getDuedate()));

    assertThat(historyCleanupCommand1.getTries()).isOne();
    assertThat(historyCleanupCommand2.getTries()).isEqualTo(2);
  }

  /**
   * See {@link ConcurrentProcessEngineJobExecutorHistoryCleanupJobTest#testConcurrentHistoryCleanupJobReconfigurationExecution()}
   * for the test case without retries, and with an exclusive lock.
   */
  @Test
  public void shouldRetryTxToBootstrapConcurrentProcessEngineWithoutExclusiveLock() throws InterruptedException {
    // given
    historyService.cleanUpHistoryAsync(true);

    ThreadControl thread1 = executeControllableCommand(new ControllableJobExecutionCommand());
    thread1.reportInterrupts();
    thread1.waitForSync();

    ControllableProcessEngineBootstrapCommand bootstrapCommand = new ControllableProcessEngineBootstrapCommand();
    ThreadControl thread2 = executeControllableCommand(bootstrapCommand);
    thread2.reportInterrupts();
    thread2.waitForSync();

    // the first Bootstrap Engine cmd performs its initialization steps
    thread1.makeContinue();
    thread1.waitForSync();

    // the second Bootstrap Engine cmd performs its initialization steps
    thread2.makeContinue();

    Thread.sleep(2000);

    // the first Bootstrap Engine cmd flushes its changes
    thread1.waitUntilDone();

    // when
    // the second Process Engine Bootstrap Command attempts to flush
    thread2.waitForSync();
    // it fails, and is retried
    thread2.waitUntilDone(true);

    assertNull(thread1.getException());
    assertNull(thread2.getException());

    // When CockroachDB is used, the CrdbTransactionRetryException is caught by
    // the CrdbTransactionRetryInterceptor and the command is retried
    assertThat(bootstrapCommand.getContextSpy().getThrowable()).isNull();
    assertThat(bootstrapCommand.getTries()).isEqualTo(2);

    // the Process Engine is successfully registered even when run on CRDB
    // since the OLE is caught and handled during the Process Engine Bootstrap command
    assertNotNull(ProcessEngines.getProcessEngines().get(PROCESS_ENGINE_NAME));
  }

  /**
   * See {@link ConcurrentHistoryLevelTest}
   * for the test case without retries, and with an exclusive lock.
   */
  @Test
  public void shouldUpdateHistoryLevelWithoutExclusiveLock() throws InterruptedException {
    // given
    ControllableUpdateHistoryLevelCommand historyLevelCommand1 = new ControllableUpdateHistoryLevelCommand();
    ControllableUpdateHistoryLevelCommand historyLevelCommand2 = new ControllableUpdateHistoryLevelCommand();
    ThreadControl thread1 = executeControllableCommand(historyLevelCommand1);
    thread1.reportInterrupts();
    thread1.waitForSync();

    ThreadControl thread2 = executeControllableCommand(historyLevelCommand2);
    thread2.reportInterrupts();
    thread2.waitForSync();

    // the first command determines the history level
    thread1.makeContinue();
    thread1.waitForSync();

    // the second command determines the history level
    thread2.makeContinue();

    Thread.sleep(2000);

    // the first command flushes the history level to the database
    thread1.waitUntilDone();

    // when
    // the second command attempts to flush the history level to the database
    thread2.waitForSync();
    // it fails, and is retried
    thread2.waitUntilDone(true);

    // then
    assertNull(thread1.getException());
    assertNull(thread2.getException());

    // the first command is only executed once
    assertThat(historyLevelCommand1.getTries()).isOne();
    // but the second encounters an OLE and is retried
    assertThat(historyLevelCommand2.getTries()).isEqualTo(2);

    // but the history level is correct
    HistoryLevel historyLevel = processEngineConfiguration.getHistoryLevel();
    assertEquals("full", historyLevel.getName());
  }

  protected static class ControllableUpdateHistoryLevelCommand extends ControllableCommand<Void> {

    protected HistoryLevelSetupCommand historyLevelSetupCommand;
    protected int tries;

    public ControllableUpdateHistoryLevelCommand() {
      this.historyLevelSetupCommand = new HistoryLevelSetupCommand();
      this.tries = 0;
    }

    public Void execute(CommandContext commandContext) {

      monitor.sync();  // thread will block here until makeContinue() is called form main thread

      tries++;
      historyLevelSetupCommand.execute(commandContext);

      monitor.sync(); // thread will block here until waitUntilDone() is called form main thread

      return null;
    }

    @Override
    public boolean isRetryable() {
      return historyLevelSetupCommand.isRetryable();
    }

    public int getTries() {
      return tries;
    }
  }

  protected static class ControllableJobExecutionCommand extends ControllableCommand<Void> {

    @Override
    public Void execute(CommandContext commandContext) {

      monitor.sync();

      List<Job> historyCleanupJobs = commandContext.getProcessEngineConfiguration()
          .getHistoryService()
          .findHistoryCleanupJobs();

      for (Job job : historyCleanupJobs) {
        commandContext.getProcessEngineConfiguration().getManagementService().executeJob(job.getId());
      }

      monitor.sync();

      return null;
    }
  }

  protected static class ControllableProcessEngineBootstrapCommand extends ControllableCommand<Void> {

    protected ControllableBootstrapEngineCommand bootstrapCommand;

    public ControllableProcessEngineBootstrapCommand() {
      this.bootstrapCommand = new ControllableBootstrapEngineCommand(this.monitor);
    }

    @Override
    public Void execute(CommandContext commandContext) {

      ProcessEngineConfigurationImpl processEngineConfiguration = ((ProcessEngineConfigurationImpl)
          ProcessEngineConfiguration
              .createProcessEngineConfigurationFromResource("camunda.cfg.xml"))
          .setCommandRetries(COMMAND_RETRIES)
          .setProcessEngineName(PROCESS_ENGINE_NAME);
      processEngineConfiguration.setProcessEngineBootstrapCommand(bootstrapCommand);

      processEngineConfiguration.buildProcessEngine();

      return null;
    }

    public int getTries() {
      return bootstrapCommand.getTries();
    }

    public CommandInvocationContext getContextSpy() {
      return bootstrapCommand.getSpy();
    }
  }

  protected static class ControllableBootstrapEngineCommand extends BootstrapEngineCommand {

    protected final ThreadControl monitor;
    protected CommandInvocationContext spy;
    protected int tries;

    public ControllableBootstrapEngineCommand(ThreadControl threadControl) {
      this.monitor = threadControl;
      this.tries = 0;
    }

    @Override
    protected void createHistoryCleanupJob(CommandContext commandContext) {

      monitor.sync();

      tries++;
      super.createHistoryCleanupJob(commandContext);
      spy = Context.getCommandInvocationContext();

      monitor.sync();
    }

    public int getTries() {
      return tries;
    }

    @Override
    public boolean isRetryable() {
      return super.isRetryable();
    }

    public CommandInvocationContext getSpy() {
      return spy;
    }
  }

  protected static class ControllableHistoryCleanupCommand extends ControllableCommand<Void> {

    protected int tries;
    protected HistoryCleanupCmd historyCleanupCmd;

    public ControllableHistoryCleanupCommand() {
      this.tries = 0;
      this.historyCleanupCmd = new HistoryCleanupCmd(true);
    }

    public Void execute(CommandContext commandContext) {
      monitor.sync();  // thread will block here until makeContinue() is called form main thread

      historyCleanupCmd.execute(commandContext);

      // increment command retries;
      tries++;

      monitor.sync();  // thread will block here until waitUntilDone() is called form main thread

      return null;
    }

    @Override
    public boolean isRetryable() {
      return historyCleanupCmd.isRetryable();
    }

    public int getTries() {
      return tries;
    }
  }

  protected static class ControllableInstallationIdInitializationCommand extends ConcurrencyTestHelper.ControllableCommand<Void> {

    protected int tries;
    protected BootstrapEngineCommand bootstrapEngineCommand;

    public ControllableInstallationIdInitializationCommand() {
      this.tries = 0;
      this.bootstrapEngineCommand = new BootstrapEngineCommand();
    }

    public Void execute(CommandContext commandContext) {

      monitor.sync(); // thread will block here until makeContinue() is called form main thread

      tries++;
      bootstrapEngineCommand.initializeInstallationId(commandContext);

      monitor.sync(); // thread will block here until waitUntilDone() is called form main thread

      return null;
    }

    @Override
    public boolean isRetryable() {
      return bootstrapEngineCommand.isRetryable();
    }

    public int getTries() {
      return tries;
    }
  }

  protected static class ControllableUpdateTelemetrySetupCommand extends ConcurrencyTestHelper.ControllableCommand<Void> {

    protected int tries;
    protected BootstrapEngineCommand bootstrapEngineCommand;

    public ControllableUpdateTelemetrySetupCommand() {
      this.tries = 0;
      this.bootstrapEngineCommand = new BootstrapEngineCommand();
    }

    public Void execute(CommandContext commandContext) {

      monitor.sync(); // thread will block here until makeContinue() is called form main thread

      tries++;
      bootstrapEngineCommand.initializeTelemetryProperty(commandContext);

      monitor.sync(); // thread will block here until waitUntilDone() is called form main thread

      return null;
    }

    @Override
    public boolean isRetryable() {
      return bootstrapEngineCommand.isRetryable();
    }

    public int getTries() {
      return tries;
    }
  }

  protected static class ControllableDeployCommand extends ConcurrencyTestHelper.ControllableCommand<Void> {

    protected final DeploymentBuilder deploymentBuilder;
    protected DeployCmd deployCmd;
    protected int tries;

    public ControllableDeployCommand(DeploymentBuilder deploymentBuilder) {
      this.deploymentBuilder = deploymentBuilder;
      this.deployCmd = new DeployCmd((DeploymentBuilderImpl) deploymentBuilder);
      this.tries = 0;
    }

    public Void execute(CommandContext commandContext) {
      monitor.sync();  // thread will block here until makeContinue() is called form main thread

      tries++;
      deployCmd.execute(commandContext);

      monitor.sync();  // thread will block here until waitUntilDone() is called form main thread

      return null;
    }

    public int getTries() {
      return tries;
    }

    @Override
    public boolean isRetryable() {
      return deployCmd.isRetryable();
    }
  }

  protected DeploymentBuilder createDeploymentBuilder() {
    return new DeploymentBuilderImpl(null)
      .name("some-deployment-name")
      .addModelInstance("foo.bpmn", PROCESS_WITH_USERTASK);
  }

  protected void closeDownProcessEngine() {
    final ProcessEngine otherProcessEngine = ProcessEngines.getProcessEngine(PROCESS_ENGINE_NAME);
    if (otherProcessEngine != null) {

      ((ProcessEngineConfigurationImpl)otherProcessEngine.getProcessEngineConfiguration())
          .getCommandExecutorTxRequired()
          .execute((Command<Void>) commandContext -> {

            List<Job> jobs = otherProcessEngine.getManagementService().createJobQuery().list();
            if (jobs.size() > 0) {
              assertEquals(1, jobs.size());
              String jobId = jobs.get(0).getId();
              commandContext.getJobManager().deleteJob((JobEntity) jobs.get(0));
              commandContext.getHistoricJobLogManager().deleteHistoricJobLogByJobId(jobId);
            }

            return null;
          });

      otherProcessEngine.close();
      ProcessEngines.unregister(otherProcessEngine);
    }
  }
}