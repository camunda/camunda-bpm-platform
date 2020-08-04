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

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_CLEANUP_STRATEGY_END_TIME_BASED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.CrdbTransactionRetryException;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.impl.BootstrapEngineCommand;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandInvocationContext;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * <p>Tests a concurrent attempt of a bootstrapping Process Engine to reconfigure
 * the HistoryCleanupJob while the JobExecutor tries to execute it.</p>
 *
 * The steps are the following:
 *
 *  1. The (History Cleanup) JobExecution thread is started, and stopped before the job is executed.
 *  2. The Process Engine Bootstrap thread is started, and stopped before the HistoryCleanupJob is reconfigured.
 *  3. The JobExecution thread executes the HistoryCleanupJob and stops before flushing.
 *  4. The Process Engine Bootstrap thread reconfigures the HistoryCleanupJob and stops before flushing.
 *  5. The JobExecution thread flushes the update to the HistoryCleanupJob.
 *  6. The Process Engine Bootstrap thread attempts to flush the reconfigured HistoryCleanupJob.
 *  6.1 An OptimisticLockingException is thrown due to the concurrent JobExecution
 *      thread update to the HistoryCleanupJob.
 *  6.2 The OptimisticLockingListener registered with
 *      the <code>BootstrapEngineCommand#createHistoryCleanupJob()</code> suppresses the exception.
 *  6.3 In case the OptimisticLockingListener didn't handle the OLE,
 *      it's still caught and logged in <code>ProcessEngineImpl#executeSchemaOperations()</code>
 *  7. The Process Engine Bootstrap thread successfully builds and registers the new Process Engine.
 *
 *
 * @author Nikola Koevski
 */
public class ConcurrentProcessEngineJobExecutorHistoryCleanupJobTest extends ConcurrencyTestCase {

  private static final String PROCESS_ENGINE_NAME = "historyCleanupJobEngine";

  @Before
  public void setUp() throws Exception {

    // Ensure that current time is outside batch window
    Calendar timeOfDay = Calendar.getInstance();
    timeOfDay.set(Calendar.HOUR_OF_DAY, 17);
    ClockUtil.setCurrentTime(timeOfDay.getTime());

    processEngineConfiguration.setHistoryCleanupStrategy(HISTORY_CLEANUP_STRATEGY_END_TIME_BASED);
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

  @After
  public void tearDown() throws Exception {
    processEngineConfiguration.getCommandExecutorTxRequired().execute((Command<Void>) commandContext -> {

      List<Job> jobs = processEngine.getManagementService().createJobQuery().list();
      if (jobs.size() > 0) {
        assertEquals(1, jobs.size());
        String jobId = jobs.get(0).getId();
        commandContext.getJobManager().deleteJob((JobEntity) jobs.get(0));
        commandContext.getHistoricJobLogManager().deleteHistoricJobLogByJobId(jobId);
      }

      return null;
    });
    ClockUtil.setCurrentTime(new Date());
    closeDownProcessEngine();
  }

  @Test
  public void testConcurrentHistoryCleanupJobReconfigurationExecution() throws InterruptedException {

    processEngine.getHistoryService().cleanUpHistoryAsync(true);

    ThreadControl thread1 = executeControllableCommand(new ControllableJobExecutionCommand());
    thread1.reportInterrupts();
    thread1.waitForSync();

    ControllableProcessEngineBootstrapCommand bootstrapCommand = new ControllableProcessEngineBootstrapCommand();
    ThreadControl thread2 = executeControllableCommand(bootstrapCommand);
    thread2.reportInterrupts();
    thread2.waitForSync();

    thread1.makeContinue();
    thread1.waitForSync();

    thread2.makeContinue();

    Thread.sleep(2000);

    thread1.waitUntilDone();

    thread2.waitForSync();
    thread2.waitUntilDone(true);

    assertNull(thread1.getException());

    if (testRule.isOptimisticLockingExceptionSuppressible()) {
      assertNull(thread2.getException());
      assertNull(bootstrapCommand.getContextSpy().getThrowable());

      // the Process Engine is successfully registered even when run on CRDB
      // since the OLE is caught and handled during the Process Engine Bootstrap command
      assertNotNull(ProcessEngines.getProcessEngines().get(PROCESS_ENGINE_NAME));
    } else {
      // When CockroachDB is used, the CrdbTransactionRetryException can't be ignored, if retries = 0
      // and the ProcessEngineBootstrapCommand must be manually retried
      assertThat(thread2.getException()).isInstanceOf(CrdbTransactionRetryException.class);
      // and the process engine is not registered
      assertThat(ProcessEngines.getProcessEngines().keySet()).doesNotContain(PROCESS_ENGINE_NAME);
    }
  }

  protected static class ControllableProcessEngineBootstrapCommand extends ControllableCommand<Void> {

    protected ControllableBootstrapEngineCommand bootstrapCommand;
    
    @Override
    public Void execute(CommandContext commandContext) {

      bootstrapCommand = new ControllableBootstrapEngineCommand(this.monitor);

      ProcessEngineConfiguration processEngineConfiguration = ProcessEngineConfiguration
        .createProcessEngineConfigurationFromResource("org/camunda/bpm/engine/test/concurrency/historycleanup.camunda.cfg.xml");


      processEngineConfiguration.setProcessEngineBootstrapCommand(bootstrapCommand);

      processEngineConfiguration.setProcessEngineName(PROCESS_ENGINE_NAME);
      processEngineConfiguration.buildProcessEngine();

      return null;
    }
    
    public CommandInvocationContext getContextSpy() {
      return bootstrapCommand.getSpy();
    }
  }

  protected static class ControllableJobExecutionCommand extends ControllableCommand<Void> {

    @Override
    public Void execute(CommandContext commandContext) {

      monitor.sync();

      List<Job> historyCleanupJobs = commandContext.getProcessEngineConfiguration().getHistoryService().findHistoryCleanupJobs();

      for (Job job : historyCleanupJobs) {
        commandContext.getProcessEngineConfiguration().getManagementService().executeJob(job.getId());
      }

      monitor.sync();

      return null;
    }
  }

  protected static class ControllableBootstrapEngineCommand extends BootstrapEngineCommand implements Command<Void> {

    protected final ThreadControl monitor;
    protected CommandInvocationContext spy;

    public ControllableBootstrapEngineCommand(ThreadControl threadControl) {
      this.monitor = threadControl;
    }

    @Override
    protected void createHistoryCleanupJob(CommandContext commandContext) {

      monitor.sync();

      super.createHistoryCleanupJob(commandContext);
      spy = Context.getCommandInvocationContext();

      monitor.sync();
    }
    
    public CommandInvocationContext getSpy() {
      return spy;
    }
  }
}
