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
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.junit.Assert.assertNull;

import java.sql.Connection;

import org.camunda.bpm.engine.CrdbTransactionRetryException;
import org.camunda.bpm.engine.impl.BootstrapEngineCommand;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.test.RequiredDatabase;
import org.camunda.bpm.engine.impl.test.TestHelper;
import org.camunda.bpm.engine.test.util.DatabaseHelper;
import org.junit.Before;
import org.junit.Test;

/**
 * <p>Tests cluster scenario with two nodes trying to store telemetry property in parallel.</p>
 *
 * <p><b>Note:</b> the test is not executed on H2 because it doesn't support the
 * exclusive lock on table.</p>
 *
 */
public class ConcurrentTelemetryConfigurationTest extends ConcurrencyTestCase {

  @Before
  public void setUp() {
    TestHelper.deleteTelemetryProperty(processEngineConfiguration);
  }

  @Test
  @RequiredDatabase(excludes = {DbSqlSessionFactory.H2, DbSqlSessionFactory.MARIADB})
  public void testEnableTelemetryWithoutConcurrencyIssue() throws InterruptedException {

    Integer transactionIsolationLevel = DatabaseHelper.getTransactionIsolationLevel(processEngineConfiguration);
    assumeThat((transactionIsolationLevel != null && !transactionIsolationLevel.equals(Connection.TRANSACTION_READ_COMMITTED)));

    ThreadControl thread1 = executeControllableCommand(new ControllableUpdateTelemetrySetupCommand(false));
    thread1.reportInterrupts();
    thread1.waitForSync();

    ThreadControl thread2 = executeControllableCommand(new ControllableUpdateTelemetrySetupCommand(false));
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
    if (testRule.isOptimisticLockingExceptionSuppressible()) {
      assertNull(thread2.getException());
      assertThat(managementService.isTelemetryEnabled()).isNull();
    } else {
      // When CockroachDB is used, the CrdbTransactionRetryException can't be ignored,
      // if retries = 0 and the ProcessEngineBootstrapCommand (telemetry initialization)
      // must be manually retried
      assertThat(thread2.getException()).isInstanceOf(CrdbTransactionRetryException.class);
      assertThat(managementService.isTelemetryEnabled()).isNull();
    }
  }

  protected static class ControllableUpdateTelemetrySetupCommand extends ControllableCommand<Void> {

    boolean telemetryEnabled;

    public ControllableUpdateTelemetrySetupCommand(boolean telemetryEnabled) {
      this.telemetryEnabled = telemetryEnabled;
    }

    public Void execute(CommandContext commandContext) {

      monitor.sync(); // thread will block here until makeContinue() is called from main thread

      new BootstrapEngineCommand().initializeTelemetryProperty(commandContext);

      monitor.sync(); // thread will block here until waitUntilDone() is called form main thread

      return null;
    }

  }
}
