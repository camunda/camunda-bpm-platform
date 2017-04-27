/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.concurrency;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.HistoryCleanupCmd;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.runtime.Job;

/**
 * <p>Tests the call to history cleanup simultaneously.</p>
 *
 * <p><b>Note:</b> the test is not executed on H2 because it doesn't support the
 * exclusive lock on table.</p>
 *
 * @author Svetlana Dorokhova
 */
public class ConcurrentHistoryCleanupTest extends ConcurrencyTestCase {

  @Override
  public void tearDown() throws Exception {
    ((ProcessEngineConfigurationImpl)processEngine.getProcessEngineConfiguration()).getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {

        List<Job> jobs = processEngine.getManagementService().createJobQuery().list();
        if (jobs.size() > 0) {
          assertEquals(1, jobs.size());
          String jobId = jobs.get(0).getId();
          commandContext.getJobManager().deleteJob((JobEntity) jobs.get(0));
          commandContext.getHistoricJobLogManager().deleteHistoricJobLogByJobId(jobId);
        }

        return null;
      }
    });
    super.tearDown();
  }

  @Override
  protected void runTest() throws Throwable {
    final Integer transactionIsolationLevel = getTransactionIsolationLevel();

    if (DbSqlSessionFactory.H2.equals(getDatabaseType()) || DbSqlSessionFactory.MARIADB.equals(getDatabaseType()) || (transactionIsolationLevel != null && !transactionIsolationLevel.equals(Connection.TRANSACTION_READ_COMMITTED))) {
      // skip test method - if database is H2
    } else {
      // invoke the test method
      super.runTest();
    }
  }

  private String getDatabaseType() {
    return processEngineConfiguration.getDbSqlSessionFactory().getDatabaseType();
  }

  private Integer getTransactionIsolationLevel() {
    final Integer[] transactionIsolation = new Integer[1];
    ((ProcessEngineConfigurationImpl)processEngine.getProcessEngineConfiguration()).getCommandExecutorTxRequired().execute(new Command<Object>() {
      @Override
      public Object execute(CommandContext commandContext) {
        try {
          transactionIsolation[0] = commandContext.getDbSqlSession().getSqlSession().getConnection().getTransactionIsolation();
        } catch (SQLException e) {

        }
        return null;
      }
    });
    return transactionIsolation[0];
  }

  public void testRunTwoHistoryCleanups() throws InterruptedException {
    ThreadControl thread1 = executeControllableCommand(new ControllableHistoryCleanupCommand());
    thread1.waitForSync();

    ThreadControl thread2 = executeControllableCommand(new ControllableHistoryCleanupCommand());
    thread2.waitForSync();

    thread1.makeContinue();
    thread1.waitForSync();

    thread2.makeContinue();

    Thread.sleep(2000);

    thread1.waitUntilDone();

    thread2.waitForSync();
    thread2.waitUntilDone();

    //only one history cleanup job exists -> no exception
    Job historyCleanupJob = processEngine.getHistoryService().findHistoryCleanupJob();
    assertNotNull(historyCleanupJob);

    assertNull(thread1.getException());
    assertNull(thread2.getException());

  }

  protected static class ControllableHistoryCleanupCommand extends ControllableCommand<Void> {

    public Void execute(CommandContext commandContext) {
      monitor.sync();  // thread will block here until makeContinue() is called form main thread

      new HistoryCleanupCmd(true).execute(commandContext);

      monitor.sync();  // thread will block here until waitUntilDone() is called form main thread

      return null;
    }

  }

}
