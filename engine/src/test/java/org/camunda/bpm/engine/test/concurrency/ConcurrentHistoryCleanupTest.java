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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assume.assumeTrue;
import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.util.List;

import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.impl.cmd.HistoryCleanupCmd;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.test.RequiredDatabase;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.util.DatabaseHelper;
import org.junit.After;
import org.junit.Test;

/**
 * <p>Tests the call to history cleanup simultaneously.</p>
 *
 * <p><b>Note:</b> the test is not executed on H2 because it doesn't support the
 * exclusive lock on table.</p>
 *
 * @author Svetlana Dorokhova
 */
public class ConcurrentHistoryCleanupTest extends ConcurrencyTestCase {

  @After
  public void tearDown() throws Exception {
    processEngineConfiguration.getCommandExecutorTxRequired().execute((Command<Void>) commandContext -> {

      List<Job> jobs = processEngine.getManagementService().createJobQuery().list();
      if (jobs.size() > 0) {
        String jobId = jobs.get(0).getId();
        commandContext.getJobManager().deleteJob((JobEntity) jobs.get(0));
        commandContext.getHistoricJobLogManager().deleteHistoricJobLogByJobId(jobId);
      }

      return null;
    });

  }

  @Test
  @RequiredDatabase(excludes = { DbSqlSessionFactory.MARIADB, DbSqlSessionFactory.H2 })
  public void testRunTwoHistoryCleanups() throws InterruptedException {
    final Integer transactionIsolationLevel = DatabaseHelper.getTransactionIsolationLevel(processEngineConfiguration);
    assumeTrue((transactionIsolationLevel != null && !transactionIsolationLevel.equals(Connection.TRANSACTION_READ_COMMITTED)));

    ThreadControl thread1 = executeControllableCommand(new ControllableHistoryCleanupCommand());
    thread1.waitForSync();

    ThreadControl thread2 = executeControllableCommand(new ControllableHistoryCleanupCommand());
    thread2.reportInterrupts();
    thread2.waitForSync();

    thread1.makeContinue();
    thread1.waitForSync();

    thread2.makeContinue();

    Thread.sleep(2000);

    thread1.waitUntilDone();

    thread2.waitForSync();
    thread2.waitUntilDone();

    //only one history cleanup job exists -> no exception
    List<Job> historyCleanupJobs = processEngine.getHistoryService().findHistoryCleanupJobs();
    assertFalse(historyCleanupJobs.isEmpty());
    assertEquals(1, historyCleanupJobs.size());

    assertNull(thread1.getException());
    if (testRule.isOptimisticLockingExceptionSuppressible()) {
      assertNull(thread2.getException());
    } else {
      assertThat(thread2.getException()).isInstanceOf(OptimisticLockingException.class);
    }

  }

  protected static class ControllableHistoryCleanupCommand extends ControllableCommand<Void> {

    public Void execute(CommandContext commandContext) {
      monitor.sync();  // thread will block here until makeContinue() is called from main thread

      new HistoryCleanupCmd(true).execute(commandContext);

      monitor.sync();  // thread will block here until waitUntilDone() is called form main thread

      return null;
    }

  }

}
