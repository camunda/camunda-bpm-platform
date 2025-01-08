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

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManagerFactory;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;
import org.camunda.bpm.engine.impl.test.RequiredDatabase;
import org.camunda.bpm.engine.runtime.Job;
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
 *  @author Philipp Ossler
 */
public class JdbcStatementTimeoutTest extends ConcurrencyTestHelper {

  private static final int STATEMENT_TIMEOUT_IN_SECONDS = 1;
  // some databases (like mysql and oracle) need more time to cancel the statement
  private static final int TEST_TIMEOUT_IN_MILLIS = 10000;
  private static final String JOB_ENTITY_ID = "42";

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(configuration ->
          configuration.setJdbcStatementTimeout(STATEMENT_TIMEOUT_IN_SECONDS));
  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  private ConcurrencyTestHelper.ThreadControl thread1;
  private ConcurrencyTestHelper.ThreadControl thread2;

  @Before
  public void setUp() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
  }


  @After
  public void tearDown() throws Exception {
    if (thread1 != null) {
      thread1.waitUntilDone();
      deleteJobEntities();
    }

    // wait for all spawned threads to end
    for (ConcurrencyTestCase.ControllableCommand<?> controllableCommand : controllableCommands) {
      ConcurrencyTestHelper.ThreadControl threadControl = controllableCommand.monitor;
      threadControl.executingThread.interrupt();
      threadControl.executingThread.join();
    }

    // clear the test thread's interruption state
    Thread.interrupted();
  }

  @Test
  @RequiredDatabase(excludes = { DbSqlSessionFactory.MARIADB,
      DbSqlSessionFactory.DB2,
      DbSqlSessionFactory.H2})
  public void testTimeoutOnUpdate() {
    createJobEntity();

    thread1 = executeControllableCommand(new UpdateJobCommand("p1"));
    // wait for thread 1 to perform UPDATE
    thread1.waitForSync();

    thread2 = executeControllableCommand(new UpdateJobCommand("p2"));
    // wait for thread 2 to perform UPDATE
    thread2.waitForSync();

    // perform FLUSH for thread 1 (but no commit of transaction)
    thread1.makeContinue();
    // wait for thread 1 to perform FLUSH
    thread1.waitForSync();

    // perform FLUSH for thread 2
    thread2.reportInterrupts();
    thread2.makeContinue();
    // wait for thread 2 to cancel FLUSH because of timeout
    thread2.waitForSync(TEST_TIMEOUT_IN_MILLIS);

    assertNotNull("expected timeout exception", thread2.getException());
  }

  private void createJobEntity() {
    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<JobEntity>() {

      @Override
      public JobEntity execute(CommandContext commandContext) {
        MessageEntity jobEntity = new MessageEntity();
        jobEntity.setId(JOB_ENTITY_ID);
        jobEntity.insert();

        return jobEntity;
      }
    });
  }

  private void deleteJobEntities() {
    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {

      public Void execute(CommandContext commandContext) {
        List<Job> jobs = commandContext.getDbEntityManager().createJobQuery().list();
        for (Job job : jobs) {
          commandContext.getJobManager().deleteJob((JobEntity) job, false);
        }

        for (HistoricJobLog jobLog : commandContext.getDbEntityManager().createHistoricJobLogQuery().list()) {
          commandContext.getHistoricJobLogManager().deleteHistoricJobLogById(jobLog.getId());
        }

        return null;
      }

    });
  }

  static class UpdateJobCommand extends ControllableCommand<Void> {

    protected String lockOwner;

    public UpdateJobCommand(String lockOwner) {
      this.lockOwner = lockOwner;
    }

    public Void execute(CommandContext commandContext) {
      DbEntityManagerFactory dbEntityManagerFactory = new DbEntityManagerFactory(Context.getProcessEngineConfiguration().getIdGenerator());
      DbEntityManager entityManager = dbEntityManagerFactory.openSession();

      JobEntity job = entityManager.selectById(JobEntity.class, JOB_ENTITY_ID);
      job.setLockOwner(lockOwner);
      entityManager.forceUpdate(job);

      monitor.sync();

      // flush the changed entity and create a lock for the table
      entityManager.flush();

      monitor.sync();

      // commit transaction and remove the lock
      commandContext.getTransactionContext().commit();

      return null;
    }

  }
}
