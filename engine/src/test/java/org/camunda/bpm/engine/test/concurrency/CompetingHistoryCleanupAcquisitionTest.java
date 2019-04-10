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

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandInterceptor;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.jobexecutor.ControllableJobExecutor;

import java.util.Collections;
import java.util.Date;

import static org.apache.commons.lang3.time.DateUtils.addDays;
import static org.apache.commons.lang3.time.DateUtils.addSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupJobHandlerConfiguration.START_DELAY;

/**
 * @author Tassilo Weidner
 */
public class CompetingHistoryCleanupAcquisitionTest extends ConcurrencyTestCase {

  protected final Date CURRENT_DATE = new Date(1363608000000L);

  protected static ThreadControl cleanupThread = null;

  protected static ThreadLocal<Boolean> syncBeforeFlush = new ThreadLocal<>();

  protected ControllableJobExecutor jobExecutor = new ControllableJobExecutor();

  protected ThreadControl acquisitionThread;

  protected void setUp() throws Exception {
    super.setUp();

    acquisitionThread = jobExecutor.getAcquisitionThreadControl();
    acquisitionThread.reportInterrupts();

    ClockUtil.setCurrentTime(CURRENT_DATE);
  }

  protected void tearDown() throws Exception {
    if (jobExecutor.isActive()) {
      jobExecutor.shutdown();
    }

    jobExecutor.resetOleThrown();

    clearDatabase();

    ClockUtil.reset();

    super.tearDown();
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

  protected void initializeProcessEngine() {
    processEngineConfiguration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
      .createProcessEngineConfigurationFromResource("camunda.cfg.xml");

    jobExecutor.setMaxJobsPerAcquisition(1);
    processEngineConfiguration.setJobExecutor(jobExecutor);
    processEngineConfiguration.setHistoryCleanupBatchWindowStartTime("12:00");

    processEngineConfiguration.setCustomPostCommandInterceptorsTxRequiresNew(Collections.<CommandInterceptor>singletonList(new CommandInterceptor() {
      @Override
      public <T> T execute(Command<T> command) {

        T executed = next.execute(command);
        if(syncBeforeFlush.get() != null && syncBeforeFlush.get()) {
          cleanupThread.sync();
        }

        return executed;
      }
    }));

    processEngine = processEngineConfiguration.buildProcessEngine();
  }

  protected void clearDatabase() {
    deleteHistoryCleanupJobs();

    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {

        commandContext.getMeterLogManager()
          .deleteAll();

        commandContext.getHistoricJobLogManager()
          .deleteHistoricJobLogsByHandlerType("history-cleanup");

        return null;
      }
    });
  }

  protected void lockEverLivingJob(final String jobId) {
    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {
      @Override
      public Void execute(CommandContext commandContext) {

        JobEntity job = commandContext.getJobManager().findJobById(jobId);

        job.setLockOwner("foo");

        job.setLockExpirationTime(addDays(CURRENT_DATE, 10));

        return null;
      }
    });
  }

}
