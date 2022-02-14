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

import org.camunda.bpm.engine.CrdbTransactionRetryException;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.impl.BootstrapEngineCommand;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * The test covers the following scenario:
 *
 * 1. An initial Process Engine creates a HistoryCleanupJob.
 * 2. A failure on the HistoryCleanupJob is simulated by setting an exception stack trace.
 * 3. The first Process Engine is started and the HistoryCleanupJob is reconfigured and the thread BLOCKS.
 * 3. The second Process Engine is started;
 * 3.1 The HistoryCleanupJob is read into the cache and the thread BLOCKS.
 * 4. The first Process Engine flushes the reconfigured HistoryCleanupJob to the DB;
 * 5. The second Process Engine reconfigures the HistoryCleanupJob:
 * 5.1 It attempts to flush the changes to the DB;
 * 5.2 An OptimisticLockingException is thrown, and the OptimisticLockingListener ignores it.
 * 6. The second Process Engine successfully reconfigures the HistoryCleanupJob.
 */
public class ConcurrentHistoryCleanupReconfigureTest extends ConcurrencyTestHelper {

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(configuration ->
      configuration.setHistoryCleanupBatchWindowStartTime("12:00"));
  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);
  
  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected HistoryService historyService;

  @Before
  public void initializeProcessEngine() {
    processEngineConfiguration =engineRule.getProcessEngineConfiguration();
    historyService = engineRule.getHistoryService();
  }

  @After
  public void tearDown() throws Exception {
    testRule.deleteHistoryCleanupJobs();
    clearDatabase();
  }

  @Test
  public void testReconfigureCleanupJobs() {
    // given
    // create cleanup job
    String jobId = historyService.cleanUpHistoryAsync(true).getId();

    // make job fail
    makeEverLivingJobFail(jobId);

    ThreadControl engineOne = executeControllableCommand(new EngineOne());
    engineOne.reportInterrupts();
    ThreadControl engineTwo = executeControllableCommand(new EngineTwo());
    engineTwo.reportInterrupts();

    engineTwo.waitForSync(); // job is fetched

    engineOne.makeContinue(); // reconfigure job & flush
    engineOne.join();

    // then
    engineTwo.makeContinue(); // reconfigure job & flush
    engineTwo.join();

    // there were no failures for the first job reconfiguration
    assertThat(engineOne.getException()).isNull();
    if (testRule.isOptimisticLockingExceptionSuppressible()) {
      assertThat(engineTwo.getException()).isNull();
    } else {
      // on CockroachDB, a concurrent reconfiguration of the HistoryCleanupJobs will result
      // in an un-ignorable CrdbTransactionRetryException and the whole command will need to be retried.
      // By default, the CRDB-related `commandRetries` property is 0. So any retryable commands,
      // like the `BootstrapEngineCommand` will not be retried, but report a `CrdbTransactionRetryException`
      // to the caller.
      assertThat(engineTwo.getException()).isInstanceOf(CrdbTransactionRetryException.class);
    }
  }

  public class EngineOne extends ControllableCommand<Void> {

    public Void execute(CommandContext commandContext) {
      new BootstrapEngineCommand().execute(commandContext);

      return null;
    }

  }

  public class EngineTwo extends ControllableCommand<Void> {

    public Void execute(CommandContext commandContext) {
      historyService.findHistoryCleanupJobs();

      monitor.sync();

      new BootstrapEngineCommand().execute(commandContext);

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

  protected void makeEverLivingJobFail(final String jobId) {
    processEngineConfiguration.getCommandExecutorTxRequired().execute((Command<Void>) commandContext -> {

      JobEntity job = commandContext.getJobManager().findJobById(jobId);

      job.setExceptionStacktrace("foo");

      return null;
    });
  }

}
