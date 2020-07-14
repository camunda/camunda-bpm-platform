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

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.BootstrapEngineCommand;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.TransactionState;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.runtime.Job;

public class ConcurrentHistoryCleanupUpdateOfFailingJobTest extends ConcurrencyTestCase {

  protected int retries = 5;

  @Override
  protected void initializeProcessEngine() {
    ProcessEngineConfigurationImpl pec = ((ProcessEngineConfigurationImpl) ProcessEngineConfiguration
        .createProcessEngineConfigurationFromResource("camunda.cfg.xml"));
    pec.setHistoryCleanupBatchWindowStartTime("00:00");
    processEngine = pec.buildProcessEngine();
    initializeServices();
  }

  @Override
  protected void closeDownProcessEngine() {
    processEngine.close();
    processEngine = null;
    super.closeDownProcessEngine();
  }

  @Override
  protected void deleteHistoryCleanupJobs() {
    String cleanUpJobId = historyService.findHistoryCleanupJobs().get(0).getId();

    processEngineConfiguration.getCommandExecutorTxRequired().<Void>execute(c -> {
      JobEntity cleanUpJob = c.getJobManager().findJobById(cleanUpJobId);
      cleanUpJob.delete();
      c.getHistoricJobLogManager().deleteHistoricJobLogByJobId(cleanUpJobId);

      return null;
    });

  }

  public void testFailedHistoryCleanupJobUpdate() throws InterruptedException {
    // given configured History cleanup

    String cleanUpJobId = historyService.findHistoryCleanupJobs().get(0).getId();

    processEngineConfiguration.getCommandExecutorTxRequired().<Void>execute(c -> {
      // add failure to the history cleanup job
      JobEntity cleanupJob = c.getJobManager().findJobById(cleanUpJobId);
      cleanupJob.setExceptionStacktrace("foo");

      return null;
    });

    ThreadControl threadOne = executeControllableCommand(new JobUpdateCmd(cleanUpJobId));

    ThreadControl threadTwo = executeControllableCommand(new ControllableBootstrap());
    threadTwo.reportInterrupts();
    threadOne.waitForSync();
    threadTwo.waitForSync();

    threadOne.makeContinue();
    threadOne.waitForSync();

    threadTwo.makeContinue();
    Thread.sleep(3000); // wait a bit until t2 is blocked during the flush

    threadOne.waitUntilDone(); // let t1 commit, unblocking t2

    threadTwo.waitUntilDone(); // continue with t2, expected to roll back

    // then
    assertThat(threadTwo.getException()).isNull();

    Job cleanupJob = historyService.findHistoryCleanupJobs().get(0);
    assertThat(cleanupJob.getRetries()).isEqualTo(retries);

    String stacktrace = managementService.getJobExceptionStacktrace(cleanupJob.getId());
    assertThat(stacktrace).isEqualTo("foo");
  }

  public class ControllableBootstrap extends ControllableCommand<Void> {

    public Void execute(CommandContext commandContext) {

      monitor.sync();
      new BootstrapEngineCommand().execute(commandContext);
      return null;
    }

  }
  
  public class JobUpdateCmd extends ControllableCommand<Void> {
    
    private String jobId;
    
    public JobUpdateCmd(String jobId) {
      this.jobId = jobId;
    }

    public Void execute(CommandContext commandContext) {

      commandContext.getTransactionContext().addTransactionListener(TransactionState.COMMITTING, cc -> monitor.sync());
      monitor.sync();
      JobEntity job = commandContext.getJobManager().findJobById(jobId);
      job.setRetries(retries); // for reproducing the problem, it is important that 
                               // this tx does not delete the exception stack trace
      return null;
    }

  }
}
