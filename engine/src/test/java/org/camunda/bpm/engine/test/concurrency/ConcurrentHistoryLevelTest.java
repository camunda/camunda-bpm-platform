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

import static org.assertj.core.api.Assumptions.assumeThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;

import org.camunda.bpm.engine.CrdbTransactionRetryException;
import org.camunda.bpm.engine.impl.HistoryLevelSetupCommand;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.test.RequiredDatabase;
import org.camunda.bpm.engine.impl.test.TestHelper;
import org.camunda.bpm.engine.test.util.DatabaseHelper;
import org.junit.Before;
import org.junit.Test;

/**
 * <p>Tests cluster scenario with two nodes trying to write the history level property in parallel.</p>
 *
 * <p><b>Note:</b> the test is not executed on H2 because it doesn't support the
 * exclusive lock on table.</p>
 *
 */
public class ConcurrentHistoryLevelTest extends ConcurrencyTestCase {

  @Before
  public void setUp() throws Exception {
    TestHelper.deleteHistoryLevel(processEngineConfiguration);
  }

  @Test
  @RequiredDatabase(excludes = { DbSqlSessionFactory.H2, DbSqlSessionFactory.MARIADB })
  public void test() throws InterruptedException {
    Integer transactionIsolationLevel = DatabaseHelper.getTransactionIsolationLevel(processEngineConfiguration);
    assumeThat((transactionIsolationLevel != null && !transactionIsolationLevel.equals(Connection.TRANSACTION_READ_COMMITTED)));
    ThreadControl thread1 = executeControllableCommand(new ControllableUpdateHistoryLevelCommand());
    thread1.reportInterrupts();
    thread1.waitForSync();

    ThreadControl thread2 = executeControllableCommand(new ControllableUpdateHistoryLevelCommand());
    thread2.reportInterrupts();
    thread2.waitForSync();

    thread1.makeContinue();
    thread1.waitForSync();

    thread2.makeContinue();

    Thread.sleep(2000);

    thread1.waitUntilDone();

    thread2.waitForSync();
    thread2.waitUntilDone();

    assertNull(thread1.getException());
    Throwable thread2Exception = thread2.getException();
    if (testRule.isOptimisticLockingExceptionSuppressible()) {
      assertNull(thread2Exception);
    } else {
      // on CRDB, the pessimistic lock is disabled and the concurrent transaction
      // with fail with a CrdbTransactionRetryException and will be retried. However,
      // by default, the CRDB-related `commandRetries` property is set to 0, so retryable commands
      // will still re-throw the `CrdbTransactionRetryException` to the caller and fail.
      assertThat(thread2Exception).isInstanceOf(CrdbTransactionRetryException.class);
    }
    HistoryLevel historyLevel = processEngineConfiguration.getHistoryLevel();
    assertEquals("full", historyLevel.getName());
  }

  protected static class ControllableUpdateHistoryLevelCommand extends ControllableCommand<Void> {

    public Void execute(CommandContext commandContext) {

      monitor.sync(); // thread will block here until makeContinue() is called from main thread

      new HistoryLevelSetupCommand().execute(commandContext);

      monitor.sync(); // thread will block here until waitUntilDone() is called form main thread

      return null;
    }

  }
}
