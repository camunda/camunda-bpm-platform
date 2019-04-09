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
import org.camunda.bpm.engine.impl.cfg.TransactionListener;
import org.camunda.bpm.engine.impl.cfg.TransactionState;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.test.jobexecutor.ControllableJobExecutor;

/**
 * @author Tassilo Weidner
 */
public class CompetingHistoryCleanupAcquisitionTest extends ConcurrencyTestCase {

  protected ControllableJobExecutor jobExecutor = new ControllableJobExecutor();
  protected ThreadControl acquisitionThread;

  protected void setUp() throws Exception {
    super.setUp();

    acquisitionThread = jobExecutor.getAcquisitionThreadControl();
    acquisitionThread.reportInterrupts();

    jobExecutor.start();
  }

  protected void tearDown() throws Exception {
    clearDatabase();

    super.tearDown();
  }

  public void testCompetingHistoryCleanupAcquisition() {
    String jobId = historyService.cleanUpHistoryAsync(true).getId();

    ThreadControl cleanupThread = executeControllableCommand(new CleanupThread(jobId));

    acquisitionThread.waitForSync();

    cleanupThread.waitForSync();

    // perform acquisition
    acquisitionThread.makeContinue();

    cleanupThread.makeContinueAndWaitForSync();

    // perform rescheduling
    cleanupThread.makeContinue();

    jobExecutor.shutdown();
  }

  public class CleanupThread extends ControllableCommand<Void> {

    protected String jobId;

    protected CleanupThread(String jobId) {
      this.jobId = jobId;
    }

    public Void execute(CommandContext commandContext) {
      monitor.sync();

      managementService.executeJob(jobId);

      commandContext.getTransactionContext()
        .addTransactionListener(TransactionState.COMMITTING, new TransactionListener() {
          public void execute(CommandContext commandContext) {
            monitor.sync();
          }
        });
      
      return null;
    }

  }

  // helpers ///////////////////////////////////////////////////////////////////////////////////////////////////////////

  protected void initializeProcessEngine() {
    processEngineConfiguration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
      .createProcessEngineConfigurationFromResource("camunda.cfg.xml");

    jobExecutor.setMaxJobsPerAcquisition(1);
    processEngineConfiguration.setJobExecutor(jobExecutor);

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

}
