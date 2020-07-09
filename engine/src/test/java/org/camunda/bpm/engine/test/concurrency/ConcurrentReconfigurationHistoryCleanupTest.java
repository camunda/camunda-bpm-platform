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

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.camunda.bpm.engine.impl.BootstrapEngineCommand;
import org.camunda.bpm.engine.impl.cfg.TransactionListener;
import org.camunda.bpm.engine.impl.cfg.TransactionState;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.runtime.Job;

public class ConcurrentReconfigurationHistoryCleanupTest extends ConcurrencyTestCase {

  protected AtomicReference<JobEntity> job = new AtomicReference<>();

  protected void tearDown() throws Exception {
    if (job.get() != null) {
      processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {
        public Void execute(CommandContext commandContext) {
          JobEntity jobEntity = job.get();

          jobEntity.setRevision(2);

          commandContext.getJobManager().deleteJob(jobEntity);
          commandContext.getByteArrayManager().deleteByteArrayById(jobEntity.getExceptionByteArrayId());
          commandContext.getHistoricJobLogManager().deleteHistoricJobLogByJobId(jobEntity.getId());

          return null;
        }
      });
    }

    super.tearDown();
  }

  public void testThrowOleDuringDeletionOfJobStacktraceTest() {
    // given
    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        // created history cleanup job
        processEngineConfiguration.setHistoryCleanupBatchWindowStartTime("00:00");
        processEngineConfiguration.initHistoryCleanup();
        new BootstrapEngineCommand().execute(commandContext);

        return null;
      }
    });

    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        // add failure to the history cleanup job
        List<Job> jobs = processEngineConfiguration.getHistoryService().findHistoryCleanupJobs();
        ((JobEntity) jobs.get(0)).setExceptionStacktrace("foo");

        return null;
      }
    });

    ThreadControl threadOne = executeControllableCommand(new ControllableBootstrap());

    ThreadControl threadTwo = executeControllableCommand(new ControllableBootstrap());
    threadTwo.reportInterrupts();
    threadOne.waitForSync();
    threadTwo.waitForSync();

    threadTwo.makeContinue();
    threadTwo.waitForSync();

    threadOne.makeContinue();
    threadOne.waitForSync();

    threadOne.waitUntilDone();

    threadTwo.waitUntilDone();

    // then
    assertThat(threadTwo.getException().getMessage())
        .contains("Entity was updated by another transaction concurrently.");
  }

  public class ControllableBootstrap extends ControllableCommand<Void> {

    public Void execute(CommandContext commandContext) {

      SyncTransactionListener syncListener = new SyncTransactionListener(monitor);

      commandContext.getTransactionContext().addTransactionListener(TransactionState.COMMITTING, syncListener);
      monitor.sync();
      new BootstrapEngineCommand().execute(commandContext);
      return null;
    }

  }

  public class SyncTransactionListener implements TransactionListener {

    ThreadControl monitor;

    public SyncTransactionListener(ThreadControl monitor) {
      super();
      this.monitor = monitor;
    }

    public void execute(CommandContext commandContext) {
      monitor.sync();
    }
  }
}
