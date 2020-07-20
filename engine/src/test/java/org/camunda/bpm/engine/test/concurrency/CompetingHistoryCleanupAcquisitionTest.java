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
package org.camunda.bpm.engine.test.concurrency;

import static org.apache.commons.lang3.time.DateUtils.addDays;
import static org.apache.commons.lang3.time.DateUtils.addSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupJobHandlerConfiguration.START_DELAY;

import java.util.Collections;
import java.util.Date;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandInterceptor;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.jobexecutor.ControllableJobExecutor;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Tassilo Weidner
 */
public class CompetingHistoryCleanupAcquisitionTest extends ConcurrencyTestHelper {

  protected ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(this::configureEngine);
  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(bootstrapRule).around(engineRule).around(testRule);

  protected HistoryService historyService;
  protected ManagementService managementService;

  protected final Date CURRENT_DATE = new Date(1363608000000L);

  protected static ThreadControl cleanupThread = null;

  protected static ThreadLocal<Boolean> syncBeforeFlush = new ThreadLocal<>();

  protected ControllableJobExecutor jobExecutor;

  protected ThreadControl acquisitionThread;

  @Before
  public void setUp() throws Exception {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    historyService = engineRule.getHistoryService();
    managementService = engineRule.getManagementService();

    acquisitionThread = jobExecutor.getAcquisitionThreadControl();
    acquisitionThread.reportInterrupts();

    ClockUtil.setCurrentTime(CURRENT_DATE);
  }

  @After
  public void tearDown() throws Exception {
    if (jobExecutor.isActive()) {
      jobExecutor.shutdown();
    }

    jobExecutor.resetOleThrown();

    clearDatabase();

    ClockUtil.reset();
  }

  public void configureEngine(ProcessEngineConfigurationImpl configuration) {
    jobExecutor = new ControllableJobExecutor();
    jobExecutor.setMaxJobsPerAcquisition(1);
    configuration.setJobExecutor(jobExecutor);
    configuration.setHistoryCleanupBatchWindowStartTime("12:00");

    configuration.setCustomPostCommandInterceptorsTxRequiresNew(Collections.singletonList(new CommandInterceptor() {
      @Override
      public <T> T execute(Command<T> command) {

        T executed = next.execute(command);
        if(syncBeforeFlush.get() != null && syncBeforeFlush.get()) {
          cleanupThread.sync();
        }

        return executed;
      }
    }));
  }
  /**
   * Problem
   *
   * GIVEN
   * Within the Execution TX the job lock was removed
   *
   * WHEN
   * 1) the acquisition thread tries to lock the job
   * 2) the cleanup scheduler reschedules the job
   *
   * THEN
   * The acquisition fails due to an Optimistic Locking Exception
   */
  @Test
  public void testAcquiringEverLivingJobSucceeds() {
    // given
    jobExecutor.indicateOptimisticLockingException();

    String jobId = historyService.cleanUpHistoryAsync(true).getId();

    lockEverLivingJob(jobId);

    cleanupThread = executeControllableCommand(new CleanupThread(jobId));

    cleanupThread.waitForSync(); // wait before flush of execution
    cleanupThread.makeContinueAndWaitForSync(); // flush execution and wait before flush of rescheduler

    jobExecutor.start();

    acquisitionThread.waitForSync();
    acquisitionThread.makeContinueAndWaitForSync(); // wait before flush of acquisition

    // when
    cleanupThread.makeContinue(); // flush rescheduler

    cleanupThread.join();

    acquisitionThread.makeContinueAndWaitForSync(); // flush acquisition

    Job job = managementService.createJobQuery().jobId(jobId).singleResult();

    // then
    assertThat(job.getDuedate()).isEqualTo(addSeconds(CURRENT_DATE, START_DELAY));
    assertThat(jobExecutor.isOleThrown()).isFalse();
  }

  /**
   * Problem
   *
   * GIVEN
   * Within the Execution TX the job lock was removed
   *
   * WHEN
   * 1) the cleanup scheduler reschedules the job
   * 2) the acquisition thread tries to lock the job
   *
   * THEN
   * The cleanup scheduler fails to reschedule the job due to an Optimistic Locking Exception
   */
  @Test
  public void testReschedulingEverLivingJobSucceeds() {
    // given
    String jobId = historyService.cleanUpHistoryAsync(true).getId();

    lockEverLivingJob(jobId);

    cleanupThread = executeControllableCommand(new CleanupThread(jobId));

    cleanupThread.waitForSync(); // wait before flush of execution
    cleanupThread.makeContinueAndWaitForSync(); // flush execution and wait before flush of rescheduler

    jobExecutor.start();

    acquisitionThread.waitForSync();
    acquisitionThread.makeContinueAndWaitForSync();

    // when
    acquisitionThread.makeContinueAndWaitForSync(); // flush acquisition

    cleanupThread.makeContinue(); // flush rescheduler

    cleanupThread.join();


    Job job = managementService.createJobQuery().jobId(jobId).singleResult();

    // then
    assertThat(job.getDuedate()).isEqualTo(addSeconds(CURRENT_DATE, START_DELAY));
  }

  public class CleanupThread extends ControllableCommand<Void> {

    protected String jobId;

    protected CleanupThread(String jobId) {
      this.jobId = jobId;
    }

    public Void execute(CommandContext commandContext) {
      syncBeforeFlush.set(true);

      managementService.executeJob(jobId);
      
      return null;
    }

  }

  // helpers ///////////////////////////////////////////////////////////////////////////////////////////////////////////

  protected void clearDatabase() {
    testRule.deleteHistoryCleanupJobs();

    processEngineConfiguration.getCommandExecutorTxRequired().execute((Command<Void>) commandContext -> {

      commandContext.getMeterLogManager()
        .deleteAll();

      commandContext.getHistoricJobLogManager()
        .deleteHistoricJobLogsByHandlerType("history-cleanup");

      return null;
    });
  }

  protected void lockEverLivingJob(final String jobId) {
    processEngineConfiguration.getCommandExecutorTxRequired().execute((Command<Void>) commandContext -> {

      JobEntity job = commandContext.getJobManager().findJobById(jobId);

      job.setLockOwner("foo");

      job.setLockExpirationTime(addDays(CURRENT_DATE, 10));

      return null;
    });
  }

}
