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

import java.sql.Connection;

import org.camunda.bpm.engine.impl.BootstrapEngineCommand;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.test.TestHelper;
import org.camunda.bpm.engine.test.util.DatabaseHelper;

/**
 * <p>Tests cluster scenario with two nodes trying to store telemetry property in parallel.</p>
 *
 * <p><b>Note:</b> the test is not executed on H2 because it doesn't support the
 * exclusive lock on table.</p>
 *
 */
public class ConcurrentTelemetryConfigurationTest extends ConcurrencyTestCase {

  @Override
  protected void runTest() throws Throwable {
    final Integer transactionIsolationLevel = DatabaseHelper.getTransactionIsolationLevel(processEngineConfiguration);
    String databaseType = DatabaseHelper.getDatabaseType(processEngineConfiguration);

    if ( DbSqlSessionFactory.H2.equals(databaseType) ||  DbSqlSessionFactory.MARIADB.equals(databaseType)
        || (transactionIsolationLevel != null && !transactionIsolationLevel.equals(Connection.TRANSACTION_READ_COMMITTED))) {
      // skip test method - if database is H2
    } else {
      // clean up the db property
      TestHelper.deleteTelemetryProperty(processEngineConfiguration);
      // invoke the test method
      super.runTest();
    }
  }

  public void testEnableTelemetryWithoutConcurancyIssue() throws InterruptedException {
    ThreadControl thread1 = executeControllableCommand(new ControllableUpdateTelemetrySetupCommand(false));
    thread1.waitForSync();

    ThreadControl thread2 = executeControllableCommand(new ControllableUpdateTelemetrySetupCommand(false));
    thread2.waitForSync();

    thread1.makeContinue();
    thread1.waitForSync();

    thread2.makeContinue();

    Thread.sleep(2000);

    thread1.waitUntilDone();

    thread2.waitForSync();
    thread2.waitUntilDone();

    assertNull(thread1.exception);
    assertNull(thread2.exception);
    assertThat(managementService.isTelemetryEnabled()).isFalse();
  }

  protected static class ControllableUpdateTelemetrySetupCommand extends ControllableCommand<Void> {

    boolean telemetryEnabled;

    public ControllableUpdateTelemetrySetupCommand(boolean telemetryEnabled) {
      this.telemetryEnabled = telemetryEnabled;
    }

    public Void execute(CommandContext commandContext) {

      monitor.sync(); // thread will block here until makeContinue() is called form main thread

      new BootstrapEngineCommand().initializeTelemetryProperty(commandContext);

      monitor.sync(); // thread will block here until waitUntilDone() is called form main thread

      return null;
    }

  }
}
